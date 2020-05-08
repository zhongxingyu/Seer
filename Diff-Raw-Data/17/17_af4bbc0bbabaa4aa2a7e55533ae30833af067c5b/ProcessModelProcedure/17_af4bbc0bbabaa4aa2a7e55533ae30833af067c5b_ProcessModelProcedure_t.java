 /*
     Copyright (C) 2010 LearningWell AB (www.learningwell.com), Kärnkraftsäkerhet och Utbildning AB (www.ksu.se)
 
     This file is part of GIL (Generic Integration Layer).
 
     GIL is free software: you can redistribute it and/or modify
     it under the terms of the GNU Lesser General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     GIL is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public License
     along with GIL.  If not, see <http://www.gnu.org/licenses/>.
 */
 package gil.core;
 
 import gil.io.IPipelineStage;
 import gil.common.Result;
 import gil.common.ValueResult;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import org.apache.log4j.Logger;
 import gil.common.AsyncResult;
 import gil.core.SignalMetadata.DataflowDirection;
 import gil.io.IProcessModelAdapter;
 import gil.common.FrequencySubdivider;
 import gil.common.GILConfiguration;
 import gil.common.IInvokeable;
 import gil.common.Invoker;
 import gil.common.Timeout;
 import gil.core.IProcedure.IState;
 import gil.io.IControlCommandInvokeable;
 
 /**
  * This is the logic separated from the controlling thread to handle the communication with the process model.
  * The method runOnce will typically be called continously from a controlling thread. The driving force of
  * separating the controlling thread from the logic is to make the logic easily testable.
  *
  * Methods in this object are not thread safe unless otherwise noted.
  *
  * @author Göran Larsson @ LearningWell AB
  */
 public class ProcessModelProcedure  {    
     private static Logger _logger = Logger.getLogger(ProcessModelProcedure.class);
     
     private IState _currentState = null;
     
     private final IProcessModelAdapter _pmAdapter;    
     private final FrequencySubdivider _subdivider;
     private final IntegrationContext _context;
     private final ITransferPipeline _pipeline;
     private final int _valuesBufSize;
     private final GILConfiguration _config;
     private final Timeout _idleTransfer;
     Invoker _controlCommandInvoker = new Invoker();
 
     private volatile boolean _reconnect = false;
     private volatile int _droppedProcessModelFrames;
     private volatile int _dataReadFailureCount;
     private volatile int _dataWriteFailureCount;
     private volatile int _commandReadFailureCount;
     private volatile int _writeFrameCount;
     private volatile int _readFrameCount;
 
 
     /**
      * @param pmAdapter The process model boundary class
      * @param esOperatingFrequency The process model operating frequency in Hertz.
      * @param pipeline The pipeline instance through which process data will be fed.
      * @param context The context data to be shared between the {@link ExternalSystemProceedure} and the
      * {@link ProcessModelProcedure}.
      * @param valuesBufSize The number of bytes that must be allocated for signal values read from the process model.
      * @param config Object containing configuration parameters to be used by this object.
      */
     public ProcessModelProcedure(IProcessModelAdapter pmAdapter, int esOperatingFrequency,
             ITransferPipeline pipeline, IntegrationContext context, int valuesBufSize, GILConfiguration config) {
         _pmAdapter = pmAdapter;
         _context = context;
         _pipeline = pipeline;
         _config = config;
         _subdivider = new FrequencySubdivider(esOperatingFrequency);
         _idleTransfer = new Timeout(config.getIdleProcessDataTransferInterval());
         _valuesBufSize = valuesBufSize;
         _currentState = new DisconnectedState();
     }
 
     public void runOnce(long currentTimeInMilliseconds) {
         _currentState = _currentState.handle(currentTimeInMilliseconds);
         _controlCommandInvoker.executeNextCommandInQueue();
     }
 
     public IState currentState() {
         return _currentState;
     }
 
     public Statistics getStatistics() {
         return new Statistics(_droppedProcessModelFrames, _commandReadFailureCount,
                 _dataWriteFailureCount, _dataReadFailureCount, _readFrameCount, _writeFrameCount);
     }
 
     public int getProcessModelState() {
         if (_currentState instanceof ConnectedState)
             return _pmAdapter.getState();
         return SimState.UNKNOWN;
     }
 
     public SystemStatus getProcessModelStatus() {
         if (_currentState instanceof ConnectedState)
             return _pmAdapter.getStatus();
         if (_currentState instanceof ErrorState)
             return new SystemStatus(SystemStatus.NOK, ((ErrorState)_currentState).getCause());
 
         return new SystemStatus(SystemStatus.UNKNOWN, "Not connected");
     }
 
     public void reconnect() {
         _reconnect = true;
     }
 
     public Map<String, String> invokeControlCommand(final String commandID, final Map<String, String> parameters)
             throws InterruptedException, ExecutionException {
         if (commandID.equals("reconnect")) {
             reconnect();
         }
         else {
             return internalInvokeControlCommand(_pmAdapter, commandID, parameters);
         }
         return new HashMap<String, String>();
     }
 
     public Map<String, String> invokePipelineStageControlCommand(final int stageSeqNo, final String commandID,
             final Map<String, String> parameters) throws InterruptedException, ExecutionException {
 
         final List<IPipelineStage> stages = _pipeline.getStages();
 
         if ((stageSeqNo < 0) || (stageSeqNo >= stages.size())) {
             throw new IllegalArgumentException("Invalid pipeline seqNo: " + stageSeqNo);
         }
         return internalInvokeControlCommand(stages.get(stageSeqNo), commandID, parameters);
     }
 
     private Map<String, String> internalInvokeControlCommand(final IControlCommandInvokeable theExecutor, final String commandID, final Map<String, String> parameters) throws ExecutionException, InterruptedException {
         AsyncResult result = _controlCommandInvoker.schedule(new IInvokeable() {
 
             public Object invoke() throws Exception {
                 Command cmd = new Command(commandID, parameters);
                 try {
                     return theExecutor.invokeControlCommand(cmd);
                 } catch (Exception ex) {
                     _logger.error("Error when invoking control command '" + cmd.toString() + "'.", ex);
                     throw ex;
                 }
             }
         });
         return (Map<String, String>) result.get(); // Block until async operation completes
     }
     
     public class DisconnectedState implements IState {
         public IState handle(long currentTimeInMilliseconds)  {
             try {
                 if (!_pmAdapter.connect()) {
                     return this;
                 }
                 ValueResult<SimTime> result = _pmAdapter.getSimTime();
                 if (!result.isSuccess()) {
                     throw new IOException(result.getErrorDescription());
                 }
                 _subdivider.reset(result.getReturnValue());                
                 _idleTransfer.reset(currentTimeInMilliseconds);
                 _idleTransfer.forceTimeout();
                 synchronized(_context) {
                     _context.pendingSimCommands.clear();
                     _context.pendingTransferToES.clear();
                 }
                 _reconnect = false;
                 return new ConnectedState();
             } catch(IOException ex) {
                 _logger.error("Failure when connecting: " + ex.getMessage());
                 _logger.debug(ex.getMessage(), ex);
                 return new ErrorState(ex.getMessage());
             }
         }
     }
 
     public class ConnectedState implements IState {
         public IState handle(long currentTimeInMilliseconds)  {
             try {
                 this.readCommandsFromProcessModelAndConditionallyAddAsPendingCommands();
 
                 ValueResult<SimTime> simTimeResult = _pmAdapter.getSimTime();
                 if (!simTimeResult.isSuccess()) {
                     ++_commandReadFailureCount;
                     _logger.warn("Failure reading process model time: " + simTimeResult.getErrorDescription());
                     return this;
                 }
                 long stepChange = _subdivider.getSystem2TimeStepChange(simTimeResult.getReturnValue());
                 _context.esFrameCount += stepChange;
                 if (stepChange > 0) {
                     this.addPendingTransferToExternalSystem();
                     if (stepChange > 1) {
                         // The process model has advanced more than a single time step since last 
                         // check resulting in lost frames.
                         _droppedProcessModelFrames += stepChange - 1;
                         _readFrameCount += stepChange - 1;
                         _logger.warn(String.format("Dropped %d PM-frame(s) due to PM frame changes.", stepChange - 1));
                     }                    
                     _idleTransfer.reset(currentTimeInMilliseconds);
 
                 }
                 else if (_idleTransfer.isTimeout(currentTimeInMilliseconds)) {
                     this.addPendingTransferToExternalSystem();
                     long timeoutCount = _idleTransfer.reschedule(currentTimeInMilliseconds);
                     if (timeoutCount > 1) {
                         // More than a single timeout has ocurred since last 
                         // check resulting in lost frames.
                         _droppedProcessModelFrames += (timeoutCount - 1);
                         _readFrameCount += (timeoutCount - 1);
                         _logger.warn(String.format("Overrun when transferring data. Dropped %d PM-frame(s)", timeoutCount - 1));
                     }                    
                 }
 
 
                 if (_context.pendingTransferToPM.size() > 0) {
                     this.writeProcessDataToProcessModel();
                 }
             }
             catch (IOException ex) {
                 _logger.error("Failure in communication with process model", ex);
                 _reconnect = true;
             }
             if (_reconnect) {
                 _pmAdapter.disconnect();
                 return new DisconnectedState();
             }
             return this;
         }
         private void readCommandsFromProcessModelAndConditionallyAddAsPendingCommands() throws IOException {
             ValueResult<Command[]> result = _pmAdapter.readSimCommands();
             if (!result.isSuccess()) {
                 ++_commandReadFailureCount;
                 _logger.warn("Failure reading commands: " + result.getErrorDescription());
                 return;
             }
             if (result.getReturnValue().length <= 0) {
                 return;
             }
             synchronized(_context) {
                 _context.pendingSimCommands.addAll(Arrays.asList(result.getReturnValue()));
             }
         }
 
         private void writeProcessDataToProcessModel() throws IOException {
             ByteBuffer valuesToPM;
             synchronized (_context) {
                 valuesToPM = _context.pendingTransferToPM.pollLast();
                 _context.pendingTransferToPM.clear();
             }
 
             if (valuesToPM != null) {
                 _logger.debug("Processing signals transferred to PM");
                 _pipeline.processSignals(valuesToPM, DataflowDirection.ToPM);
                 _logger.debug("Done processing signals transferred to PM");
 
                 Result result = _pmAdapter.writeSignalData(valuesToPM);
                 if (result.isSuccess()) {
                     ++_writeFrameCount;
                 } else {
                     ++_dataWriteFailureCount;
                     _logger.warn("Failure writing signals: " + result.getErrorDescription());                    
                 }
             }
         }
 
         private void addPendingTransferToExternalSystem() throws IOException {
 
             // Must allocate direct since the buffer may be used across boundaries to native code (JNI).
            ByteBuffer valuesBuf = ByteBuffer.allocateDirect(_valuesBufSize);            
            valuesBuf.order(_config.getPMAdapterByteOrder());
 
             Result result = _pmAdapter.readSignalData(valuesBuf);
             if (result.isSuccess()) {
                 synchronized (_context) {
                     _logger.debug("Processing signals transferred to ES");
                     _pipeline.processSignals(valuesBuf, DataflowDirection.ToES);
                     _logger.debug("Done processing signals transferred to ES");
                     valuesBuf.rewind();
                     if (!_context.pendingTransferToES.isEmpty()) {
                         _logger.warn(String.format("Dropped %d PM frames(s) due to still pending transfers.", _context.pendingTransferToES.size()));
                         _droppedProcessModelFrames += _context.pendingTransferToES.size();
                         _context.pendingTransferToES.clear();
                     }
                     ++_readFrameCount;
                     _context.pendingTransferToES.add(valuesBuf);
                 }
             } else {
                 _logger.warn("Failure reading signals: " + result.getErrorDescription());
                 ++_dataReadFailureCount;
             }
         }
     }
 
     public class ErrorState implements IState {
         private String _cause = "";
 
         public ErrorState(String cause) {
             _cause = cause;
         }
 
         public IState handle(long currentTimeInMilliseconds)  {
             if (_reconnect) {
                 return new DisconnectedState();
             }
             return this;
         }
 
         public String getCause() {
             return _cause;
         }
     }
 }
