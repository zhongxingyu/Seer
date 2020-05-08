 package com.raygroupintl.vista.tools.returntype;
 
 import com.raygroupintl.m.parsetree.Entry;
 import com.raygroupintl.m.parsetree.Line;
 import com.raygroupintl.m.parsetree.QuitCmd;
 import com.raygroupintl.m.parsetree.data.Block;
 import com.raygroupintl.m.parsetree.data.CallArgument;
 import com.raygroupintl.m.parsetree.data.EntryId;
 import com.raygroupintl.m.parsetree.visitor.BlockRecorder;
import com.raygroupintl.struct.HierarchicalMap;
 
 public class ReturnTypeBR extends BlockRecorder<ReturnType> {
 
 	private int lineLevel;
 	private ReturnTypeENUM returnTypeENUM = null;
 	private ReturnType attachedObject = new ReturnType();;
 	
 	@Override
 	protected void visitLine(Line line) {
 		// TODO Auto-generated method stub
 		super.visitLine(line);
 		lineLevel = line.getLevel();
 	}
 
 	@Override
 	protected void visitQuit(QuitCmd quitCmd) {
 		super.visitQuit(quitCmd);
 		
 //		if (quitCmd.getPostCondition() != null)
 //			System.out.println("has post cond");
 //		System.out.println("line level: " +lineLevel);
 		if (lineLevel >= 1) {
 			return;
 		}
 		if (returnTypeENUM != null && returnTypeENUM == ReturnTypeENUM.INVALID_RETURNS_BOTH)
 			return;
 		
 		if (quitCmd.getArgument() != null) {
 			if (returnTypeENUM == null || returnTypeENUM == ReturnTypeENUM.RETURN_VALUE)
 				returnTypeENUM = ReturnTypeENUM.RETURN_VALUE;
 			else
 				returnTypeENUM = ReturnTypeENUM.INVALID_RETURNS_BOTH;
 		} else {
 			if (returnTypeENUM == null || returnTypeENUM == ReturnTypeENUM.RETURN_NOTHING)
 				returnTypeENUM = ReturnTypeENUM.RETURN_NOTHING;
 			else
 				returnTypeENUM = ReturnTypeENUM.INVALID_RETURNS_BOTH;
 		}
 		
 		attachedObject.setReturnType(returnTypeENUM);
 	}
 
 	@Override
 	protected void postUpdateFanout(EntryId fanout, CallArgument[] callArguments) {
 	}
		
 	@Override
 	protected void visitEntry(Entry entry) {
 //		if (!entry.getKey().contains(":"))
 //			System.out.println("start entry: " +entry.getKey());
 		super.visitEntry(entry);
 		if (entry.isClosed()) { //reset the return type, since the entry being visited has now completed via an unconditional quit
 			attachedObject = new ReturnType();
 			returnTypeENUM = null;
 		}
 //		if (!entry.getKey().contains(":"))
 //			System.out.println("stop entry: " +entry.getKey());
 	}
 
	@Override
	protected Block<ReturnType> getNewBlock(int index, EntryId entryId,
			HierarchicalMap<String, Block<ReturnType>> blocks, String[] params) {
		return new Block<ReturnType>(index, entryId, blocks, attachedObject); //this gets called when a new entry is visited. get it again later via getCurrentBlock()
	}

 }
