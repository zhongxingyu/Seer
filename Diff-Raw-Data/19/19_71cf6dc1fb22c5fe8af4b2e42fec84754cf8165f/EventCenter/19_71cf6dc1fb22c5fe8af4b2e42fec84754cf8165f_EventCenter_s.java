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
 package eplic.core.eventHandler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.debug.core.DebugPlugin;
 
 import eplic.core.mode.AbstractMode;
 import eplic.core.mode.NormalMode;
 import eplic.core.visualization.VisualizerManager;
 /**
  * 
  * EventCenter定義了所有EPLIC會用到的Event
  * 擁有一個BasicEventHandler
  * 
  * @see BasicEventHandler
  * @author FrankWang
  *
  */
 public class EventCenter {
 	private static EventCenter instance = null;
 	private AbstractEventHandler handler;
 	private AbstractMode  _currentMode;
 	private AbstractMode  _normalMode;
 	private AbstractMode  _interestedMode;
 	private AbstractMode  _traceMode;
 	private List<AbstractMode> _modeList;
 	
 	public static EventCenter getInstance() {
 		if (instance == null) {
 			instance = new EventCenter();
 		}
 		return instance;
 	}
 	/**
 	 * constructor
 	 */
 	private EventCenter() {
 		handler = new BasicEventHandler();
 		DebugPlugin.getDefault().addDebugEventListener(handler);
 		_modeList = new ArrayList<AbstractMode>();
 	}
 	/**
 	 * add BreakPointListener
 	 * @param listener
 	 */
 	public void addBreakPointListener(BreakPointListener listener) {
 		handler.addBreakPointListener(listener);
 	}
 	/**
 	 * add TargetTerminationListener
 	 * @param listener
 	 */
 	public void addTargetTerminationListener(TargetTerminationListener listener) {
 		handler.addTargetTerminationListener(listener);
 	}
 	/**
 	 * add TargetCreationListener
 	 * @param listener
 	 */
 	public void addTargetCreationListener(TargetCreationListener listener) {
 		handler.addTargetCreationListener(listener);
 	}
 	/**
 	 * remove BreakPointListener
 	 * @param listener
 	 */
 	public void removeBreakPointListener(BreakPointListener listener) {
 		handler.removeBreakPointListener(listener);
 	}
 	/**
 	 * remove TargetCreationListener
 	 * @param listener
 	 */
 	public void removeTargetCreationListener(TargetCreationListener listener) {
 		handler.removeTargetCreationListener(listener);
 	}
 	/**
 	 * remove TargetTerminationListene
 	 * @param listener
 	 */
 	public void removeTargetTerminationListener(
 			TargetTerminationListener listener) {
 		handler.removeTargetTerminationListener(listener);
 	}
 
 	/**
 	 * remove All BreakPointListene
 	 */
 	public void removeAllBreakPointListener() {
 		handler.removeAllBreakPointListener();
 		handler.addBreakPointListener(VisualizerManager.getInstance());
 	}
 	/**
 	 * remove All TargetCreationListener
 	 */
 	public void removeAllTargetCreationListener() {
 		handler.removeAllTargetCreationListener();
 	}
 	/**
 	 * remove All TargetTerminationListener
 	 */
 	public void removeAllTargetTerminationListener() {
 		handler.removeAllTargetTerminationListener();
 	}
 	
 	public void setNorMode(AbstractMode m){
 		_normalMode = m;
 		this.addBreakPointListener(_normalMode);
 		_currentMode = _normalMode;
 	}
 	public void setIntMode(AbstractMode m){
 		_interestedMode = m;
 		this.removeAllBreakPointListener();
 		this.addBreakPointListener(_interestedMode);
 		this.addTargetTerminationListener(_interestedMode);
 		_currentMode = _interestedMode;
 	}
 	public void setTraMode(AbstractMode m){
 		_traceMode = m;
 		this.removeAllBreakPointListener();
 		this.removeAllTargetTerminationListener();
 		_traceMode.init();
 		_currentMode = _traceMode;
 		this.addBreakPointListener(_traceMode);
 		this.addTargetTerminationListener(_traceMode);
 	}
 	public void setCurrentMode(AbstractMode m){
 		_currentMode = m;
 	}
 	public AbstractMode getNorMode(){
 		return _normalMode;
 	}
 	public AbstractMode getInsMode(){
 		return _interestedMode;
 	}
 	public List<AbstractMode> getModeList(){
 		assert(_modeList != null);
 		return _modeList;
 	}
	public void switchMode(){
 		if(_currentMode == null){
 			this.setNorMode(new NormalMode());
 		}
 		else{
			_currentMode.switchMode();
 		}
 	}
 
 	public int getModeType() {
 		if(_currentMode == null)
 			return 0;
 		else
 			return _currentMode.getMode();
 	}
 	public void reset(){
 		instance = new EventCenter();
 	}
 
 }
