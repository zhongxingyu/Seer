 package com.raygroupintl.vista.tools;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.raygroupintl.m.parsetree.Routine;
 import com.raygroupintl.m.parsetree.data.Block;
 import com.raygroupintl.m.parsetree.data.BlocksInMap;
 import com.raygroupintl.m.parsetree.data.EntryId;
 import com.raygroupintl.m.parsetree.visitor.FanoutRecorder;
 import com.raygroupintl.m.struct.EntryFanoutInfo;
 import com.raygroupintl.m.struct.FanoutTypeENUM;
 import com.raygroupintl.m.token.MVersion;
 import com.raygroupintl.output.FileWrapper;
 import com.raygroupintl.output.TerminalFormatter;
 import com.raygroupintl.vista.repository.RepositoryInfo;
 import com.raygroupintl.vista.tools.fnds.ToolResult;
 import com.raygroupintl.vista.tools.returntype.ReturnType;
 import com.raygroupintl.vista.tools.returntype.ReturnTypeBR;
 import com.raygroupintl.vista.tools.returntype.ReturnTypeENUM;
 import com.raygroupintl.vista.tools.returntype.ReturnTypeToolResults;
 import com.raygroupintl.vista.tools.returntype.RoutineLoader;
 
 public class ReturnTypeTool extends Tool {
 	
 	private RoutineLoader routineLoader;
 
 	protected ReturnTypeTool(CLIParams params) {
 		super(params);
 	}
 
 	//this class should only be responsible for handling invoking the returntype scan for the entries passed in via a param
 	
 	@Override
 	public void run() {
 		FileWrapper fr = getOutputFile();
 		RepositoryInfo ri = getRepositoryInfo();
 		Set<String> routineNames = new TreeSet<String>( getEntriesInString());
 		if (fr == null || ri == null || routineNames == null)
 			return;
 		
 		routineLoader = new RoutineLoader(MVersion.CACHE); //TODO: how is this set, via CLI?
 		//load the routine parse trees for each entry param into a map String<routineName>,parsetree
 		routineLoader.loadRoutines(routineNames);
 
 		for (String routineName : routineNames) {
 			processRoutine(routineName, fr);
 		}
 	}
 
 	private void processRoutine(String routineName, FileWrapper fr) {
 		
 		//1) get all the fanouts from a routine
 		FanoutRecorder fanoutRecorder = new FanoutRecorder();
 		LinkedList<EntryFanoutInfo> entryFanoutsList = fanoutRecorder.getFanouts2(routineLoader.getRoutine(routineName)); //get all fanouts of a given routine
 		
 		//need to transform the local tag calls to qualified fanouts
 		for (EntryFanoutInfo efi : entryFanoutsList) {
 			EntryId fanout = efi.getFanoutTo();
 			if (fanout.getRoutineName() == null)
 				efi.setFanoutTo(new EntryId(routineName, fanout.getTag()));
 		}
 		
 		//2) lookup/parse each return type for that fanout
 		Set<String> fanoutRoutineNames = new HashSet<String>();
 		for (EntryFanoutInfo fanout : entryFanoutsList) {
 			routineLoader.loadRoutine(fanout.getFanoutTo().getRoutineName()); //lookup
 			
 			if (fanout.getFanoutTo().getRoutineName() != null)
 				fanoutRoutineNames.add(fanout.getFanoutTo().getRoutineName());
 		}
 		
 		ReturnTypeBR rtBR = new ReturnTypeBR();
 		Routine[] fanoutRoutines = routineLoader.getSubset(fanoutRoutineNames).toArray(new Routine[0]);
 		
 		//gather only the return types
 		//parse as indicated in (2)
		BlocksInMap<ReturnType> blocksMap = BlocksInMap.getInstance(rtBR, fanoutRoutines); //for every routine that is a fanout, return all the block types
 		////blocksMap.map <String,Blocks> key: Routinename, the routine being a fanout. value: blocks of the routine key
 		//--Blocks is a hashmap wrapper of <String,Block<T>> and links to 1 parent Blocks.. the get can query the parent if not found in the present. key: tag, in the routine, Block, of the tag
 		//----Block contains an attached object, children - ArrayList<Block> (do blocks), siblings - Blocks<T> (siblings in the routine?), fanouts - ArrayList of IndexedFanout (int routineindex, EntryId)		
 		
 		//note: need to map the 'entryFanoutList' to the 'blocksMap'
 
 		//3) iterate each of the original 'entryFanouts' and see if the fanout is a valid call
 		for (EntryFanoutInfo fanout : entryFanoutsList) {
 			fanout.setValid(true);
 			fanout.setFanoutExists(true);
 			Block<ReturnType> blockOfFanout = blocksMap.getBlock(fanout.getFanoutTo());
 			if (blockOfFanout == null) {
 				fanout.setValid(false);
 				fanout.setFanoutExists(false);
 				continue;
 			}
 			fanout.setReturnType(blockOfFanout.getAttachedObject().getReturnType());
 			if (fanout.getFanoutType() == FanoutTypeENUM.DO || fanout.getFanoutType() == FanoutTypeENUM.GOTO) {
 				if (blockOfFanout.getAttachedObject().getReturnType() != ReturnTypeENUM.RETURN_NOTHING) {
 					fanout.setValid(false);
 				}
 			} else if (fanout.getFanoutType() == FanoutTypeENUM.EXTRINSIC) {
 				if (blockOfFanout.getAttachedObject().getReturnType() != ReturnTypeENUM.RETURN_VALUE) {
 					fanout.setValid(false);
 				}
 			}
 			//System.out.println(fanout.getLineLocation()+ " : " +fanout.getFanoutTo() + " : " +fanout.getFanoutType() + " : " + fanout.getReturnType() + " : " +fanout.isValid());
 		}
 		
 		ToolResult resultList = new ReturnTypeToolResults(routineName, entryFanoutsList);
 		if (fr.start()) {
 			TerminalFormatter tf = new TerminalFormatter();
 			tf.setTab(12);
 			resultList.write(fr, tf);
 			fr.stop();
 		}
 	}
 
 }
