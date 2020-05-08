 //---------------------------------------------------------------------------
 // Copyright 2013 PwC
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //---------------------------------------------------------------------------
 
 package com.pwc.us.rgi.m.tool.entry.quit;
 
 import com.pwc.us.rgi.m.parsetree.DoBlock;
 import com.pwc.us.rgi.m.parsetree.ForLoop;
 import com.pwc.us.rgi.m.parsetree.QuitCmd;
 import com.pwc.us.rgi.m.parsetree.data.EntryId;
 import com.pwc.us.rgi.m.parsetree.data.Fanout;
 import com.pwc.us.rgi.m.parsetree.data.FanoutType;
 import com.pwc.us.rgi.m.parsetree.visitor.BlockRecorder;
 import com.pwc.us.rgi.m.struct.CodeLocation;
 import com.pwc.us.rgi.m.tool.entry.CodeLocations;
 
 public class QuitRecorder extends BlockRecorder<Fanout, CodeLocations> {
 	protected CodeLocations getNewBlockData(EntryId entryId, String[] params) {
 		CodeLocations ecls = new CodeLocations(entryId);
 		return ecls;
 	}
 
	private int innerQuit = 0;
 	
 	@Override
 	public void reset() {
 		super.reset();
		this.innerQuit = 0;
 	}
 	
 	@Override
 	protected void visitQuit(QuitCmd quitCmd) {
 		super.visitQuit(quitCmd);
		if (this.innerQuit > 0) return;
 		CodeLocations data = this.getCurrentBlockData();
 		CodeLocation codeLocation = this.getCodeLocation();
 		data.add(codeLocation);
 	}
 
 	@Override
 	protected void visitForLoop(ForLoop forLoop) {
		++this.innerQuit;
 		super.visitForLoop(forLoop);
		--this.innerQuit;
 	}
 	
 	@Override
 	protected void visitDoBlock(DoBlock doBlock) {
		++this.innerQuit;
 		super.visitDoBlock(doBlock);
		--this.innerQuit;
 	}
 
 	@Override
 	protected void updateFanout(EntryId fanoutId, FanoutType type) {
 		if ((type == FanoutType.ASSUMED_GOTO) || (type == FanoutType.GOTO)) {
 			super.updateFanout(fanoutId, type);
 		} 
 	}
 
 	@Override
 	protected Fanout getFanout(EntryId id, FanoutType type) {
 		return new Fanout(id, type);
 	}
 }
