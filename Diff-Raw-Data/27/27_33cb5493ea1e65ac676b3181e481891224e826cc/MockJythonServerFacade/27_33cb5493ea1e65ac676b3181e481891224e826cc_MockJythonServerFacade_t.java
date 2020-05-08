 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
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
 
 package gda.jython;
 
 import gda.device.DeviceException;
 import gda.jython.authenticator.UserAuthentication;
 import gda.jython.batoncontrol.ClientDetails;
 import gda.observable.IObserver;
 import gda.observable.ObservableComponent;
 import gda.scan.IScanDataPoint;
 import gda.scan.ScanBase;
 import gda.util.LibGdaCommon;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Mock implementation of interfaces usually provided by JythonServerFacade to be used when running tests outside of gda
  * This implementation is used if you set the property JythonServerFacade.dummy to true
  * 
  */
 public class MockJythonServerFacade implements IScanStatusHolder, ICommandRunner, ITerminalPrinter,
 		ICurrentScanController, IJythonNamespace, IAuthorisationHolder, IScanDataPointProvider ,
		IScriptController, IPanicStop, IBatonStateProvider, JSFObserver, AliasedCommandProvider, IJythonServerNotifer{
 	private static final Logger logger = LoggerFactory.getLogger(MockJythonServerFacade.class);
 	
 	private String terminalOutput = "";
 	
 	public String getTerminalOutput() {
 		return terminalOutput;
 	}
 
 	public void setTerminalOutput(String terminalOutput) {
 		this.terminalOutput = terminalOutput;
 	}
 
 	@Override
 	public void runCommand(String command) {
 		logger.info("MockJythonServerFacade - runCommand " + command);
 	}
 
 	@Override
 	public void haltCurrentScan() {
 	}
 
 	@Override
 	public void print(String text) {
 		logger.info(text);
 		terminalOutput += text + "\n";
 	}
 
 	volatile int scanStatus = Jython.IDLE;
 
 	@Override
 	public void setScanStatus(int newStatus) {
 		scanStatus = newStatus;
 	}
 
 	@Override
 	public int getScanStatus() {
 		return scanStatus;
 	}
 
 	@Override
 	public void pauseCurrentScan() {
 	}
 
 	@Override
 	public void restartCurrentScan() {
 	}
 
 	@Override
 	public void resumeCurrentScan() {
 	}
 
 	@Override
 	public Object getFromJythonNamespace(String objectName) {
 		return hashTable.get(objectName);
 	}
 
 	HashMap<String, Object> hashTable = new HashMap<String, Object>();
 
 	@Override
 	public void placeInJythonNamespace(String objectName, Object obj) {
 		hashTable.put(objectName, obj);
 	}
 
 	private int authorisationLevel=0;
 
 	@Override
 	public int getAuthorisationLevel() {
 		return authorisationLevel;
 	}
 
 
 	@Override
 	public int getAuthorisationLevelAtRegistration() {
 		return authorisationLevel;
 	}
 
 	/**
 	 * Allows tests to change authorisation level returned by getAuthorisationLevel
 	 * @param authorisationLevel
 	 */
 	public void setAuthorisationLevel(int authorisationLevel) {
 		this.authorisationLevel = authorisationLevel;
 	}
 
 	ObservableComponent scanDataPointObservers = new ObservableComponent();
 	@Override
 	public void addIScanDataPointObserver(IScanDataPointObserver anObserver) {
 		scanDataPointObservers.addIObserver(anObserver);
 	}
 
 	@Override
 	public void deleteIScanDataPointObserver(IScanDataPointObserver anObserver) {
 		scanDataPointObservers.deleteIObserver(anObserver);
 	}
 
 	@Override
 	public IScanDataPoint getLastScanDataPoint() {
 		return lastScanDataPoint;
 	}
 
 	volatile int scriptStatus = Jython.IDLE;
 
 	private IScanDataPoint lastScanDataPoint=null;
 
 	@Override
 	public void setScriptStatus(int newStatus) {
 		scanStatus = newStatus;
 	}
 
 	@Override
 	public int getScriptStatus() {
 		return scanStatus;
 	}
 
 	@Override
 	public void panicStop() {
 		ScriptBase.setInterrupted(true);
 		ScanBase.setInterrupted(true);
 		scanStatus = Jython.IDLE;
 		scriptStatus = Jython.IDLE;
 	}
 
 	@Override
 	public void pauseCurrentScript() {
 	}
 
 	@Override
 	public void resumeCurrentScript() {
 	}
 
 	
 	@Override
 	public void runCommand(String command, String scanObserver) {
 	}
 
 	@Override
 	public void haltCurrentScript() {
 	}
 
 	@Override
 	public void update(Object dataSource, Object data) {
 		if( data instanceof IScanDataPoint){
 			lastScanDataPoint = (IScanDataPoint)data;
 			scanDataPointObservers.notifyIObservers(dataSource, data);
 		}
 	}
 
 	@Override
 	public void addBatonChangedObserver(IObserver anObserver) {
 		
 	}
 
 	@Override
 	public boolean amIBatonHolder() {
 		return true;
 	}
 
 	@Override
 	public void assignBaton(int index) {
 	}
 
 	@Override
 	public void deleteBatonChangedObserver(IObserver anObserver) {
 	}
 
 	ClientDetails myDetails;
 	{
 		final String username = UserAuthentication.getUsername();
 		final String fullName = LibGdaCommon.getFullNameOfUser(username);
 		myDetails = new ClientDetails(0, username, fullName, "pc012345", 3, true, "0-0");
 	}
 	
 	ClientDetails [] others = new ClientDetails[]{new ClientDetails(1, "A.N. Other", "A.N. Other", "pc012345", 3, false, "0-0")};
 
 	public String evaluateCommandResult = "";
 	
 	@Override
 	public ClientDetails getBatonHolder() {
 		return myDetails;
 	}
 	
 	@Override
 	public ClientDetails[] getOtherClientInformation() {
 		return others;
 	}
 
 	@Override
 	public boolean isBatonHeld() {
 		return true;
 	}
 
 	@Override
 	public void returnBaton() {
 	}
 
 	@Override
 	public void addIObserver(IObserver anIObserver) {
 	}
 
 	@Override
 	public void deleteIObserver(IObserver anIObserver) {
 	}
 
 	@Override
 	public void deleteIObservers() {
 	}
 
 	@Override
 	public String evaluateCommand(String command) {
 		return evaluateCommandResult ;
 	}
 
 
 	@Override
 	public void changeVisitID(String visitID) {
 		myDetails.setVisitID(visitID);
 	}
 
 	@Override
 	public void revertToOriginalUser() {
 	}
 
 	@Override
 	public Vector<String> getAliasedCommands() {
 		return new Vector<String>();
 	}
 
 	@Override
 	public Vector<String> getAliasedVarargCommands() {
 		return new Vector<String>();
 	}
 
 	@Override
 	public ClientDetails getMyDetails() {
 		return myDetails;
 	}
 
 	@Override
 	public boolean requestBaton() {
 		return true;
 	}
 
 	@Override
 	public void sendMessage(String message) {
 	}
 	
 	@Override
 	public List<UserMessage> getMessageHistory() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean switchUser(String username, String password) {
 		return false;
 	}
 
 	@Override
 	public Map<String, Object> getAllFromJythonNamespace() throws DeviceException {
 		SortedSet<String> set = new TreeSet<String>(hashTable.keySet()); 
 		LinkedHashMap <String,Object> output = new LinkedHashMap <String,Object>();
 		for (String objName : set) {
 			output.put(objName, hashTable.get(objName));
 		}
 		return output;
 	}
 
 	@Override
 	public void runScript(File script, String sourceName) {
 	}
 
 	@Override
 	public boolean runsource(String command, String source) {
 		return true;
 	}
 
 	@Override
 	public String locateScript(String scriptToRun) {
 		// TODO Auto-generated method stub
 		return null;
	}

	@Override
	public void notifyServer(Object source, Object data) {
		update(source, data);		
 	}	
 }
