 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.data.scan.datawriter;
 
 import gda.device.DeviceException;
 import gda.jython.InterfaceProvider;
 import gda.scan.IScanDataPoint;
 
 import java.util.ArrayList;
 import java.util.Vector;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DatapointCompletingDataWriter extends DataWriterBase implements DataWriter {
 
 	private DataWriter sink;
 	
 	ExecutorService datapointCompleterSingleThreadpool;
 
 	private Throwable exception;
 	
 	private static final Logger logger = LoggerFactory.getLogger(DatapointCompletingDataWriter.class);
 
 	public DatapointCompletingDataWriter() {
 		datapointCompleterSingleThreadpool = Executors.newFixedThreadPool(1);
 	}
 	
 	private boolean pointContainsCallablePosition(IScanDataPoint point) {
 		Vector<Object> positions = point.getPositions();
 		for (Object position : positions) {
 			if (position instanceof Callable<?>) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void addData(IScanDataPoint point) throws Exception {
 		throwException();
 		super.addData(point); // runs extenders
 		if (pointContainsCallablePosition(point)) {
 			DatapointCompleterTask task = new DatapointCompleterTask(sink, point, this);
 			datapointCompleterSingleThreadpool.execute(task);
 
 		} else {
 			sink.addData(point);
 		}
 	}
 
 	synchronized void setExceptionAndShutdownNow(Throwable e) {
 		exception = e;
 		logger.info("Storing an Exception caught computing a point: " + e.getMessage());
 		shutdownNow();
 	}
 	
 	/**
 	 * If an exception has been caught then throw it.
 	 * 
 	 * @throws DeviceException
 	 */
 	private void throwException() throws Exception {
 		if (exception != null){
 			throw new Exception("Exception caught completing datapoint in datawriter", exception);
 		}
 	}
 	
 	private void shutdownNow() {
		if (!datapointCompleterSingleThreadpool.isShutdown()) {
			logger.info("Shutting down datapointCompleterExecutor NOW.");
			datapointCompleterSingleThreadpool.shutdownNow();
		}
 	}
 	
 	private void shutdown() {
//		InterfaceProvider.getTerminalPrinter().print("Shutting down datapointCompleterThreadPool gently.");
		if (datapointCompleterSingleThreadpool.isShutdown())
			return;
		logger.debug("Shutting down datapointCompleterThreadPool gently.");
 		datapointCompleterSingleThreadpool.shutdown();
 		try {
 			datapointCompleterSingleThreadpool.awaitTermination(10, TimeUnit.MINUTES);
 		} catch (InterruptedException e) {
 			logger.warn("InterruptedException during shutdown - there may be orphaned threads", e.getStackTrace());
 		}
		logger.debug("Shutting down datapointCompleterThreadPool gently - completed");
 		}
 		
 	@Override
 	public void completeCollection() throws Exception {
		logger.debug("DAtapointcmDW complete collection is called");
 		throwException();
 		shutdown();
		logger.debug("DAtapointcmDW  sink complete collection is called");
 		sink.completeCollection();
 	}
 
 	@Override
 	public String getCurrentFileName() {
 		return sink.getCurrentFileName();
 	}
 
 	@Override
 	public String getCurrentScanIdentifier() {
 		return sink.getCurrentScanIdentifier();
 	}
 
 	@Override
 	public ArrayList<String> getHeader() {
 		return sink.getHeader();
 	}
 
 	@Override
 	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
 		sink.removeDataWriterExtender(dataWriterExtender);
 	}
 
 	@Override
 	public void setHeader(ArrayList<String> header) {
 		sink.setHeader(header);
 	}
 
 	@Override
 	public void setHeader(String header) {
 		sink.setHeader(header);
 	}
 
 	public void setDatawriter(DataWriter datawriter) {
 		this.sink = datawriter;
 	}
 
 	public DataWriter getDatawriter() {
 		return sink;
 	}
 
 	
 	class DatapointCompleterTask implements Runnable {
 
 		private IScanDataPoint point;
 
 		private DataWriter taskSink;
 
 		private final DatapointCompletingDataWriter owner;
 
 		public DatapointCompleterTask(DataWriter sink, IScanDataPoint point, DatapointCompletingDataWriter owner) {
 			this.taskSink = sink;
 			this.point = point;
 			this.owner = owner;
 		}
 
 		@Override
 		public void run() {
 		
 			Vector<Object> positions = point.getPositions();
 			for (int i = 0; i < positions.size(); i++) {
 				if (positions.get(i) instanceof Callable<?>) {
 					Object pos;
 					try {
 						pos = ((Callable<?>) positions.get(i)).call();
 						positions.set(i, pos);
 					} catch (Exception e) {
 						owner.setExceptionAndShutdownNow(e);
 						return;
 					}
 				}
 			}
 			try {
 				InterfaceProvider.getTerminalPrinter().print("Writing data point " + point.getCurrentPointNumber());
 				taskSink.addData(point);
 			} catch (Exception e) {
 				owner.setExceptionAndShutdownNow(e);
 				return;
 			}
 		}
 
 	}
 
 
 	@Override
 	public void configureScanNumber(Long scanNumber) throws Exception {
 		sink.configureScanNumber(scanNumber);
 	}
 	
 }
