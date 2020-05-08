 package org.araqne.logdb.msgbus;
 
 import java.util.Date;
 import java.util.Map;
 
 import org.araqne.log.api.Log;
 import org.araqne.log.api.LogPipe;
 import org.araqne.log.api.LogTransformer;
 import org.araqne.log.api.Logger;
 import org.araqne.log.api.LoggerEventListener;
 import org.araqne.log.api.LoggerFactory;
 import org.araqne.log.api.LoggerStartReason;
 import org.araqne.log.api.LoggerStatus;
 import org.araqne.log.api.LoggerStopReason;
 
 class DummyLogger implements Logger {
 
 	@Override
 	public String getFullName() {
 		return null;
 	}
 
 	@Override
 	public String getNamespace() {
 		return null;
 	}
 
 	@Override
 	public String getName() {
 		return null;
 	}
 
 	@Override
 	public String getFactoryFullName() {
 		return null;
 	}
 
 	@Override
 	public String getFactoryName() {
 		return null;
 	}
 
 	@Override
 	public String getFactoryNamespace() {
 		return null;
 	}
 
 	@Override
 	public String getDescription() {
 		return null;
 	}
 
 	@Override
 	public boolean isPassive() {
 		return false;
 	}
 
 	@Override
 	public Date getLastStartDate() {
 		return null;
 	}
 
 	@Override
 	public Date getLastRunDate() {
 		return null;
 	}
 
 	@Override
 	public Date getLastLogDate() {
 		return null;
 	}
 
 	@Override
 	public Date getLastWriteDate() {
 		return null;
 	}
 
 	@Override
 	public Log getLastLog() {
 		return null;
 	}
 
 	@Override
 	public LoggerStopReason getStopReason() {
 		return null;
 	}
 
 	@Override
 	public long getDropCount() {
 		return 0;
 	}
 
 	@Override
 	public long getLogCount() {
 		return 0;
 	}
 
 	@Override
 	public long getUpdateCount() {
 		return 0;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return false;
 	}
 
 	@Override
 	public boolean isRunning() {
 		return false;
 	}
 
 	@Override
 	public boolean isPending() {
 		return false;
 	}
 
 	@Override
 	public void setPending(boolean pending) {
 	}
 
 	@Override
 	public boolean isManualStart() {
 		return false;
 	}
 
 	@Override
 	public void setManualStart(boolean manualStart) {
 	}
 
 	@Override
 	public LoggerStatus getStatus() {
 		return null;
 	}
 
 	@Override
 	public int getInterval() {
 		return 0;
 	}
 
 	@Override
 	public void start(LoggerStartReason reason) {
 	}
 
 	@Override
 	public void start(LoggerStartReason reason, int interval) {
 	}
 
 	@Override
 	public void stop(LoggerStopReason reason) {
 	}
 
 	@Override
 	public void stop(LoggerStopReason reason, int maxWaitTime) {
 	}
 
 	@Override
 	public void addLogPipe(LogPipe pipe) {
 	}
 
 	@Override
 	public void removeLogPipe(LogPipe pipe) {
 	}
 
 	@Override
 	public void addEventListener(LoggerEventListener callback) {
 	}
 
 	@Override
 	public void removeEventListener(LoggerEventListener callback) {
 	}
 
 	@Override
 	public void clearEventListeners() {
 	}
 
 	@Override
 	public void updateConfigs(Map<String, String> config) {
 	}
 
 	@Override
 	public Map<String, String> getConfig() {
 		return null;
 	}
 
 	@Override
 	public Map<String, String> getConfigs() {
 		return null;
 	}
 
 	@Override
 	public Map<String, Object> getStates() {
 		return null;
 	}
 
 	@Override
 	public void setStates(Map<String, Object> state) {
 	}
 
 	@Override
 	public void reloadStates() {
 	}
 
 	@Override
 	public void resetStates() {
 	}
 
 	@Override
 	public LogTransformer getTransformer() {
 		return null;
 	}
 
 	@Override
 	public void setTransformer(LogTransformer transformer) {
 	}
 
 	@Override
 	public LoggerFactory getFactory() {
 		return null;
 	}
 }
