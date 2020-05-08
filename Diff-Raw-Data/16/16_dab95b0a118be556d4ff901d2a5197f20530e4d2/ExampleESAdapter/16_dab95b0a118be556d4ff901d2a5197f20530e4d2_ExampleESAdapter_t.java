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
 package gil.io.example;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import gil.common.InvalidParameterException;
 import gil.common.Parameters;
 import gil.core.Command;
 import gil.core.CommandDescriptor;
 import gil.core.ParameterDescriptor;
 import gil.core.SoftwareInfo;
 import gil.core.SignalMetadata;
 import gil.core.SimState;
 import gil.core.SystemStatus;
 import gil.io.ESAdapterCapabilities;
 import gil.io.IExternalSystemAdapter;
 import gil.common.IProgressEventListener;
 import gil.common.ProgressChangedEventArgs;
 import gil.common.Result;
 
 /**
  * This class exemplifies the implementation of the IExternalSystemAdapter interface. It works in pair with the
  * ExamplePMAdapter. Please see IExternalSystemAdapter.java for implementation details.
  * @author Göran Larsson @ LearningWell AB
  */
 public class ExampleESAdapter implements IExternalSystemAdapter {
 
     private ArrayList<IProgressEventListener> _progressListeners = new ArrayList<IProgressEventListener>();
 
     private static final String CURRENT_VERSION = "0.3.4";
 
     // The org.apache.log4j.Logger shall be used for logging if the GIL runtime shall be able to 
     // manage the log output. Always use the adapter class name to retrieve a logger from the log4j framework.
     private static Logger _logger = Logger.getLogger(ExampleESAdapter.class);
 
     // Definitions of the signals to be transferred to the external system from the process model.
     private SignalMetadata[] _signalsToES;
     
     // Definitions of the signals to be transferred from the external system to the process model.
     private SignalMetadata[] _signalsToPM;
 
     private int _currentState = SimState.UNKNOWN;
 
     private int _operatingFrequency = 0;
 
     public void setup(SignalMetadata[] signalsToES, SignalMetadata[] signalsToPM, Parameters config) throws IOException, InvalidParameterException {
         _logger.info("Setup called.");
         _signalsToES = signalsToES;
         _signalsToPM = signalsToPM;
         _operatingFrequency = config.getIntParam("operatingFrequency");
     }
 
     public void tearDown() {
         _logger.info("TearDown called.");
     }
 
     public boolean connect() throws IOException {
         _logger.info("Connect called.");
         return true;
     }
 
     public void disconnect() {
         _logger.info("Disconnect called.");
     }
 
     public Result writeSignalData(ByteBuffer values) throws IOException {
         StringBuilder sb = new StringBuilder();
         sb.append("Values written to ES: ");
         for (SignalMetadata smd : _signalsToES) {
             for (int elm = 0; elm < smd.getLength(); elm++) {
                 appendValueToLogOutput(smd.getPMSignalID(), values.getFloat(), sb);
             }
         }
         _logger.info(sb.toString());
         return new Result(true);
     }
 
     private void appendValueToLogOutput(String signalID, float value, StringBuilder sb) {
         sb.append(signalID);
         sb.append('=');
         sb.append(value);
         sb.append(' ');
     }
 
    private long  _lastTransfer = 0;
     public Result readSignalData(ByteBuffer destBuf) throws IOException {
        long t = (System.currentTimeMillis() / 1000);
        if (t == _lastTransfer)
            return null;
        _lastTransfer = t;
         int i = 0;
         for (SignalMetadata smd : _signalsToPM) {
             for (int elm = 0; elm < smd.getLength(); elm++) {
                destBuf.putFloat((float)((++i) + _lastTransfer % 10 + 10));
             }
        }        
         return new Result(true);
     }
 
 
     public SoftwareInfo[] getInfo() {
         // This example returns just a single SoftwareInfo structure. In a real world implementation the 
         // software version of the external system and other relevant subsystems may be added.
         return new SoftwareInfo[] {
                 new SoftwareInfo(ExampleESAdapter.class.getSimpleName(),
                 "The example adapter to aid development of an IExternalSystemAdapter",
                 CURRENT_VERSION,
                 "LearningWell"),
                 new SoftwareInfo("Stub external system",
                 "A non existing external system",
                 "1.0.0",
                 "LearningWell"),
         };
     }
 
     public SystemStatus getStatus() {
         return new SystemStatus(SystemStatus.OK, "The ES is OK");
     }
 
     public int getOperatingFrequency() {        
         return _operatingFrequency;
     }
 
     public int getState() {
         return _currentState;
     }
 
     public Result executeSimCommand(Command cmd) throws IOException {
         _logger.info("Executing SimCommand: " +  cmd.toString());
 
         if (cmd.isCommand("LOAD_IC")) {
             fakeLoadIC(cmd.getParameter("icNo"));
         }
         else if (cmd.isCommand("RUN")) {
             _currentState = SimState.RUN;
         }
         else if (cmd.isCommand("FREEZE")) {
             _currentState = SimState.FREEZE;
         }
         return new Result(true);
     }
 
     public boolean addProgressChangeListener(IProgressEventListener listener) {
         _progressListeners.add(listener);
         return true; // return true to indicate that the listener has been added and will bi notified on
                       // progress changes and/or
     }
 
     public CommandDescriptor[] availableControlCommands() {
         return new CommandDescriptor[] {
             new CommandDescriptor("SayHello", "Says hello to the given person",
                 new ParameterDescriptor[]
                     {
                         new ParameterDescriptor("firstname", "The first name of the person", false),
                         new ParameterDescriptor("surname", "Probably the family name of the person", false)
                     }
             ),
             new CommandDescriptor("SayBye", "Says bye",
                 new ParameterDescriptor[0]
             )
         };
     }
 
     public Map<String, String> invokeControlCommand(final Command command) throws IllegalArgumentException {
                 
         if (command.isCommand("SayHello")) {
             
             _logger.info("********* Hello " + command.getParameter("firstname") + " " + command.getParameter("surname"));
             HashMap<String, String> res =  new HashMap<String, String>() {{
                 put("description", "I have said hello to the requested person");
                 put("firstname", command.getParameter("firstname"));
                 put("surname", command.getParameter("surname"));
             }};
             return res;
         }
         else if (command.isCommand("SayBye")) {
             _logger.info("********* Bye *********");
             return new HashMap<String, String>() {{
                 put("description", "Said bye");
             }};
         }
         throw new IllegalArgumentException("Unknown control command: " + command);
     }
 
     public Result timeStepControl() {
         return new Result(true);
     }
 
     private void fakeLoadIC(final String icNo) {
         int progress = 0;
         try {
             do {
                 progress += 10;
                 notifyProgress(progress, "Loading IC no " + icNo, false);
                 Thread.sleep(1000);
             } while(progress < 100);
         } catch (InterruptedException ex) {}
 
         // The last progress change notification shall indicate that the activity is completed
         notifyProgress(100, "", true);
     }
     private void notifyProgress(int progress, String message, boolean isDone) {
         ProgressChangedEventArgs args = new ProgressChangedEventArgs(progress, message, isDone);
         _logger.info(message + "  " + progress + "% completed");
         for (IProgressEventListener l : _progressListeners) {
             l.progressChanged(args);
         }
     }
 
     public ESAdapterCapabilities getCapabilities() {                       
         ESAdapterCapabilities capabilities = new ESAdapterCapabilities();
         capabilities.canShutDownAndPowerUp = false;
 
         return capabilities;
     }
 
     public void powerUp() throws IOException, UnsupportedOperationException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void shutDown() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 }
