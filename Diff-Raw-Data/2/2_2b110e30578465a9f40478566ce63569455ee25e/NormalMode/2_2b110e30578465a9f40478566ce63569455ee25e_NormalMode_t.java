 /**
  * EPLIC - A Tool to Assist Locating Interested Code.
  * Copyright (C) 2013 Frank Wang <eternnoir@gmail.com>
  * 
  * This file is part of EPLIC.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eplic.core.mode;
 
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.ILineBreakpoint;
 import org.eclipse.debug.core.model.IStackFrame;
 import org.eclipse.debug.core.model.IVariable;
 
 import eplic.core.breakpoint.BreakpointManager;
 import eplic.core.breakpoint.breakpointRecoder;
 import eplic.core.eventHandler.EventCenter;
 /**
  * Normal Mode , EPLIC啟動後預設進入此mode
  * 此mode紀錄使用者所操作不感興趣的行為
  * 
  * @author FrankWang
  *
  */
 public class NormalMode extends AbstractMode {
 	
 	private breakpointRecoder _bpr;
 	private IDebugTarget[] _debugTargets = null;
 	public NormalMode(){
 		_bpr = new breakpointRecoder();
 		_modeType = 1;
 	}
 	public void onBreakPointTriggered(IVariable[] variables,
 			IBreakpoint breakpoint, IStackFrame[] stacks) {
 		_debugTargets = DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
 		ILineBreakpoint lineBreakpoint = (ILineBreakpoint) breakpoint;
 		try{
 			IMarker m =lineBreakpoint.getMarker();
 			if(m!=null)
 			_bpr.addBreakPoint(lineBreakpoint);
 			lineBreakpoint.setEnabled(false); 			//for performance
 			this.cont();
 		}
 		catch(Exception ex){
 			System.err.print("get breakpoint info error");
 		}
 	}
 	
 	public void onTargetTerminated() {
 		AbstractMode im = EventCenter.getInstance().getInsMode();
 		if(im == null){
 			System.err.print("Can't get instertedMode");
 			return;
 		}
 		BreakpointManager.getInstance().diffResult(this._bpr.getBPS(),im.getBreakPointRecorder().getBPS());
 
 	}
 	
 	public breakpointRecoder getBreakPointRecorder(){
 		assert(_bpr != null);
 		return _bpr;
 	}
 	/**
	 * cont debugger
 	 */
 	public void cont(){
 		for(IDebugTarget debugTarget : _debugTargets){
 			try {
 				if(debugTarget.canResume()){
 					debugTarget.resume();
 				}
 
 			} catch (DebugException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	@Override
 	public void init() {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void switchMode() {
 		if(EventCenter.getInstance().getInsMode()==null){
 			EventCenter.getInstance().setIntMode(new InterestedMode());
 		}
 		else{
 			
 		}
 		
 	}
 	
 }
