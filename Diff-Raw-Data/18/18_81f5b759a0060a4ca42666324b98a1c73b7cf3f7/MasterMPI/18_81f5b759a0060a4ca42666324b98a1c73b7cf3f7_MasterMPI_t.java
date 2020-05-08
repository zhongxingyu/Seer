 package it.uniroma3.dia.alfred.mpi.runner;
 
 import it.uniroma3.dia.alfred.mpi.OutputParser;
 import it.uniroma3.dia.alfred.mpi.model.ConfigHolder;
 import it.uniroma3.dia.alfred.mpi.model.serializer.ConfigHolderSerializable;
 import it.uniroma3.dia.alfred.mpi.runner.MPIConstants.AbortReason;
 import it.uniroma3.dia.alfred.mpi.runner.MPIConstants.TagValue;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializerFactory.Config;
 
 import mpi.MPI;
 import mpi.MPIException;
 
 import com.google.common.collect.Lists;
 
 class MasterMPI {
 	private static Logger currentLogger = Logger.getLogger(MasterMPI.class);
 	
 	private MasterMPI() {}
 
 	public static void run(List<ConfigHolder> inputConfigs, int processCountWithoutMaster) throws MPIException {
 		List<Integer> confPerWorker = Lists.newArrayList();
 		int atLeast = inputConfigs.size() / processCountWithoutMaster;
 		int workRest = inputConfigs.size() % processCountWithoutMaster;
 		
 		currentLogger.debug("Workload: " + inputConfigs.size() + "- PCount:" + processCountWithoutMaster + "- Min work: " +  atLeast + "- Rest: " + workRest);
 		// System.out.println("Workload: " + inputConfigs.size() + "- PCount:" + processCountWithoutMaster + "- Min work: " +  atLeast + "- Rest: " + workRest);
 		
 		for(int i = 0; i < processCountWithoutMaster; ++i) {
 			if (workRest != 0) {
 				confPerWorker.add(atLeast + 1);
 				workRest = workRest - 1;
 			} else {
 				confPerWorker.add(atLeast);
 			}
 		}
 		
 		currentLogger.debug("Process[" + MPI.COMM_WORLD.Rank() + "]:send: " + confPerWorker);
 		// System.out.println("Process[" + MPI.COMM_WORLD.Rank() + "]:send: " + confPerWorker);
 		int ackProcessesShare = sendWorkShares(processCountWithoutMaster, confPerWorker);
 		if (ackProcessesShare != inputConfigs.size()) {
 			RunAlfred.abort(AbortReason.WORK_SEND_ACK);
 		}
 		
 		currentLogger.info("Process[" + MPI.COMM_WORLD.Rank() + "]:Sending stuff");
 		// System.out.println("Process[" + MPI.COMM_WORLD.Rank() + "]:Dumping and sending stuff");
 //		RunAlfred.dumpConf(MPI.COMM_WORLD.Rank(), inputConfigs);
 		
 		sendConfs(confPerWorker, inputConfigs);
 		
 		// Synchro after thread execution
 		MPI.COMM_WORLD.Barrier();
 		
 		// Recv boolean results
 		List<Boolean> processesResult = recvBooleanResults(processCountWithoutMaster);
 		
 		// System.out.println("Process[" + MPI.COMM_WORLD.Rank() + "]:Results: " + processesResult);
 		// Do smth meaningful with results
 		
 		writeFinalConfiguration(processesResult, inputConfigs);
 	}
 
 	private static int sendWorkShares(int processCountWithoutMaster, List<Integer> confPerWorker) {
 		int[] messageSend = new int[1];
 		int[] messageRecv = new int[1];
 		messageSend[0] = 0;
 		messageRecv[0] = 0;		
 		
 		for(int i = 0; i < processCountWithoutMaster; ++i) {
 			messageSend[0] = confPerWorker.get(i);
 			try {
 				MPI.COMM_WORLD.Send(messageSend, 0, 1, MPI.INT, i + 1, TagValue.TAG_SIZE_CONF.getValue());
 			} catch (MPIException e) {
 				currentLogger.error("Process[Master]:sendWorkShares()", e);
 				// e.printStackTrace();
 				RunAlfred.abort(AbortReason.WORK_SEND);
 			}
 		}
 		
 		messageSend[0] = 0;
 		messageRecv[0] = 0;
 		try {
 			MPI.COMM_WORLD.Reduce(messageSend, 0, messageRecv, 0, 1, MPI.INT, MPI.SUM, MPIConstants.MASTER);
 		} catch (MPIException e) {
 			currentLogger.error("Process[Master]:sendWorkShares()", e);
 			e.printStackTrace();
 			RunAlfred.abort(AbortReason.WORK_SEND_ACK);
 		}
 		
 		return messageRecv[0];
 	}
 	
 	private static void sendConfs(List<Integer> confPerWorker, List<ConfigHolder> inputConfigs) {
 		int slaveId = 1;
 		int confSent = 0;
 		int nextLimit = 0;
 		char[] localBuffer;
 		int[] messageSend = new int[1];
 		
 		for(Integer slaveSend: confPerWorker) {
 //			System.out.println("Process[" + MPI.COMM_WORLD.Rank() + "]: Begin slave id: " + slaveId);
 			
 			nextLimit = confSent + slaveSend;
 			for(int i = confSent; i < nextLimit; ++i, ++confSent) {
 				localBuffer = ConfigHolderSerializable.toJson(inputConfigs.get(i)).toCharArray();
 				messageSend[0] = localBuffer.length;
 		
 //				System.out.println("Process[" + MPI.COMM_WORLD.Rank() + "]: Sending to " + slaveId + " conf id: " + inputConfigs.get(i).getUid() + " - len: " + localBuffer.length);
 				try {
 					MPI.COMM_WORLD.Send(messageSend, 0, 1, MPI.INT, slaveId, TagValue.TAG_CONF_LEN.getValue());
 					MPI.COMM_WORLD.Send(localBuffer, 0, localBuffer.length, MPI.CHAR, slaveId, TagValue.TAG_CONF_DATA.getValue());
 				} catch (MPIException e) {
 					currentLogger.error("Process[Master]:sendConfs()", e);
 					// e.printStackTrace();
 					RunAlfred.abort(AbortReason.WORK_SEND_DATA);
 				}
 				
 //				System.out.println("Process[" + MPI.COMM_WORLD.Rank() + "]: Done conf id: " + inputConfigs.get(i).getUid());
 			}
 			
 //			System.out.println("Process[" + MPI.COMM_WORLD.Rank() + "]: Done slave id: " + slaveId);
 			slaveId = slaveId + 1;
 		}
 	}
 	
 	private static List<Boolean> recvBooleanResults(int processCountWithoutMaster) {
 		int[] messageRecv = new int[1];
 		byte[] bprocessesResult = new byte[1];
 		List<Boolean> processesResult = Lists.newArrayList();
 		
 		for(int id = 1; id <= processCountWithoutMaster; ++id) {
 
 			try {
 				MPI.COMM_WORLD.Recv(messageRecv, 0, 1, MPI.INT, id, TagValue.TAG_CONF_RESULTS_SIZE.getValue());
 				bprocessesResult = new byte[messageRecv[0]];
 				MPI.COMM_WORLD.Recv(bprocessesResult, 0, messageRecv[0], MPI.BYTE, id, TagValue.TAG_CONF_RESULTS.getValue());
 			} catch (MPIException e) {
 				currentLogger.error("Process[Master]:recvBooleanResults()", e);
 				// e.printStackTrace();
 				Arrays.fill(bprocessesResult, (byte)0);
 			}
 			
 			for(byte bCurr: bprocessesResult) {
 				processesResult.add(bCurr == 1);
 			}
 		}
 		
 		return processesResult;
 	}
 
 	private static void writeFinalConfiguration(List<Boolean> resultBool, List<ConfigHolder> lstCfgHolder) {
		int totalCfgCount = 0;
		for (ConfigHolder cfgStuff: lstCfgHolder) {
			totalCfgCount += cfgStuff.getAssociatedDomain().getXPathNames().size();
		}
		
		if ( resultBool.size() != totalCfgCount ) {
 			currentLogger.error("Discrepancy between sizes");
 		}
 		
		for(int i = 0; i < Math.min(resultBool.size(), totalCfgCount); ++i) {
 			currentLogger.info("Process[Master]: Result for " +lstCfgHolder.get(i).getUid() + " = " + resultBool.get(i));
 		}
 		
 		boolean bResult = OutputParser.parse(lstCfgHolder);
 		if (bResult) {
 			currentLogger.info("Process[Master]: Saved results");
 		} else {
 			currentLogger.error("Process[Master]: Error saving results");
 		}
 	}
 }
