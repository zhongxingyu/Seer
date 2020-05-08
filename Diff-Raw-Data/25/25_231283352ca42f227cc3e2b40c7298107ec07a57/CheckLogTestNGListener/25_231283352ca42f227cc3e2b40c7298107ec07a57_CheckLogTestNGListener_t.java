 package com.redhat.qe.tools.checklog;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.testng.ISuite;
 import org.testng.ISuiteListener;
 import org.testng.ITestContext;
 import org.testng.ITestListener;
 import org.testng.ITestResult;
 /**
  * TestNG listener that intercepts {@link LogFileReader} watcher on test method calls.
  * Watching and checking log file can be enabled either globally by setting <b>com.redhat.qe.tools.remote.log.check</b> property, 
  * that will result to global log checker defined by {@link LogFile} defaults.<br>
  * And/or you can enable it  on test class or method by adding {@link CheckLog} annotation.
  * @see CheckLog
  * @author lzoubek@redhat.com
  *
  */
 public class CheckLogTestNGListener implements ITestListener,ISuiteListener {
 
 	protected static Logger log = Logger.getLogger(CheckLogTestNGListener.class.getName());
 	
 	private static final Pattern envVarPattern = Pattern.compile("\\$\\{env\\:([^\\}]+)\\}");
 	private static final Pattern systemPropPattern = Pattern.compile("\\$\\{([^\\}]+)\\}");
 	private RemoteLogHandle classWatcher = null;
 	@Override
 	public void onStart(ISuite arg0) {
 
 	}
 
 	@Override
 	public void onFinish(ISuite arg0) {	
 	}
 
 	@Override
 	public void onStart(ITestContext context) {
 	
 		
 	}
 
 	@Override
 	public void onTestFailure(ITestResult result) {
 		checkLogs(result);
 	}
 
 	@Override
 	public void onFinish(ITestContext context) {
 		
 	}
 
 	@Override
 	public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
 		disconnectWatcher(classWatcher);		
 	}
 
 	@Override
 	public void onTestSkipped(ITestResult arg0) {
 		disconnectWatcher(classWatcher);
 		
 	}
 
 	/**
 	 * at this time we have to find out whether to create log watcher or not
 	 */
 	@Override
 	public void onTestStart(ITestResult result) {		
 		Class<?> klass = result.getTestClass().getRealClass();
 		// find our annotation on method level
 		CheckLog check = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(CheckLog.class);
 		List<CheckLog> checks = getClassAnnotations(klass, null);
 		if (check!=null) {
 			checks.add(0, check);
 		}
 		RemoteLogHandle watcher = create(checks);
 		if (watcher!=null && watcher.isEnabled() && isEnabledGlobal()) {
 			log.fine("Enabling checker "+watcher.toString()+ " for class "+klass.getCanonicalName());
 			classWatcher = watcher;
 			watcher.watch();
 		}
 
 	}
 
 	@Override
 	public void onTestSuccess(ITestResult result) {
 		checkLogs(result);
 	}
 	
 	private void checkLogs(ITestResult result) {
 		try {			
 			// class/method watcher has always higher priority
 			RemoteLogHandle watcher = classWatcher;			
 			if (watcher!=null && watcher.isEnabled()) {
 				// we check only successful tests and also failed when enabled
 				if (result.isSuccess() || (result.getStatus() == ITestResult.FAILURE && watcher.isAssertFailed())) {
 					StringBuilder message = new StringBuilder();
 					for (LogFileReader rla : watcher.getLogs()) {
 						log.fine("Examining "+rla.toString()+"...");
 						List<String> errorLines = rla.filteredLines();
 						if (!errorLines.isEmpty()) {
 							log.warning("Founds lines matching ["+rla.getFilter()+"] in "+rla.toString()+" , seting test result as FAILED");
 							message.append(rla.toString()+":\n");
 							message.append(linesToStr(errorLines)+"\n");
 						}
 					}
 					if (message.length()>0) {
 						result.setStatus(ITestResult.FAILURE);
						result.setThrowable(new ErrorLineFoundException("Following error lines were found in\n"+message.toString(),result.getThrowable()));
 					}
 				}
 			}
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		disconnectWatcher(classWatcher);
 	}
 	
 	private static String substValues(String value) {
 		Matcher m = envVarPattern.matcher(value);
 		while (m.find()) {
 			String repl = System.getenv(m.group(1));
 			if (repl==null) {
 				repl = "{env:"+m.group(1)+"}";
 			}
 			value = value.replaceAll(envVar(m.group(1)).toString(),Matcher.quoteReplacement(repl));
 		}
 		m = systemPropPattern.matcher(value);
 		while (m.find()) {
 			value = value.replaceAll(
 				sysProp(m.group(1)).toString(),Matcher.quoteReplacement(System.getProperty(m.group(1), "{" + m.group(1) + "}")));
 		}
 		return value;	
 	}
 	private static Pattern envVar(String value) {
 		value = value.replaceAll("\\.", "\\\\.");
 		return Pattern.compile("\\$\\{env\\:"+value+"\\}");
 	}
 	private static Pattern sysProp(String value) {
 		value = value.replaceAll("\\.", "\\\\.");
 		return Pattern.compile("\\$\\{"+value+"\\}");
 	}
 	/**
 	 * finds {@link CheckLog} annotation in given class or recursive in super classes 
 	 * @param klass
 	 * @return
 	 */
 	private CheckLog getClassAnnotation(Class<?> klass) {
 		if (klass==null || Object.class.equals(klass)) {
 			return null;
 		}
 		CheckLog check = klass.getAnnotation(CheckLog.class);
 		if (check!=null) {
 			return check;
 		}		
 		return getClassAnnotation(klass.getSuperclass());
 	}
 	
 	/**
 	 * finds all {@link CheckLog} annotations in given class and all super classes 
 	 * @param klass
 	 * @return
 	 */
 	private List<CheckLog> getClassAnnotations(Class<?> klass, List<CheckLog> list) {
 		if (list==null) {
 			list = new ArrayList<CheckLog>();
 		}
 		if (klass==null || Object.class.equals(klass)) {
 			return list;
 		}
 		CheckLog check = klass.getAnnotation(CheckLog.class);
 		if (check!=null) {
 			list.add(check);
 		}		
 		return getClassAnnotations(klass.getSuperclass(),list);
 	}
 	
 	private LogFile findReferencedLogFile(String refId, List<CheckLog> checks) {
 		for (CheckLog check : checks) {
 			for (LogFile lf : check.logs()) {
 				if (lf.id().equals(refId)) {
 					return lf;
 				}
 			}
 		}
 		return null;
 	}
 	
 	private Object getCorrectValue(Object defVal, Object referenced, Object val) {
 		if (defVal.equals(val)) {
 			return referenced;
 		}
 		return val;
 	}
 	
 	private LogFileReader createFromLogFileAnnotation(LogFile rl,List<CheckLog> checks) {
 		LogFile referenced = null;
 		LogFile defValues = getClassAnnotation(DefaultAnnotationValues.class).logs()[0];
 		if (!"".equals(rl.refId())) {
 			// this logfile references another one, let's find it
 			referenced = findReferencedLogFile(rl.refId(),checks);
 			if (referenced == null) {
 				log.warning("Unable to find referenced LogFile refid="+rl.refId());
 				referenced = rl;
 			}						
 		} else {
 			referenced = rl;
 		}		
 		String user = (String)getCorrectValue(defValues.user(), referenced.user(), rl.user());
 		String host = (String)getCorrectValue(defValues.host(), referenced.host(), rl.host());
 		String pass = (String)getCorrectValue(defValues.pass(), referenced.pass(), rl.pass());
 		String keyfile = (String)getCorrectValue(defValues.keyfile(), referenced.keyfile(), rl.keyfile());
 		String logFile = (String)getCorrectValue(defValues.logFile(), referenced.logFile(), rl.logFile());
 		String failExpr = (String)getCorrectValue(defValues.failOn(), referenced.failOn(), rl.failOn());
 		String ignoreExpr = (String)getCorrectValue(defValues.ignore(), referenced.ignore(), rl.ignore());
 	return CheckLogTestNGListener.createLogReader(user, host, pass, keyfile, logFile,failExpr, ignoreExpr);
 
 	}
 	/**
 	 * creates new instance of agent log based on {@link CheckLog} annotations
 	 */
 	private RemoteLogHandle create(List<CheckLog> checks) {
 		if (checks.size()==0) {
 			return null;
 		}
 		// first item is always the main
 		CheckLog check = checks.get(0);
 		if (!check.enabled()) {
 			// user requires to turn off checker
 			return new RemoteLogHandle(null,false,false);
 		}		
 		RemoteLogHandle inst = new RemoteLogHandle("class",true,check.assertFailed());
 		for (LogFile rl : check.logs()) {
 			LogFileReader rla = createFromLogFileAnnotation(rl,checks);
 			if (rla!=null) {
 				inst.getLogs().add(rla);
 			}
 		}		
 		return inst;
 	}
 	private void disconnectWatcher(RemoteLogHandle watcher) {
 		if (watcher!=null && watcher.getLogs()!=null) {
 			watcher.disconnect();
 			watcher.setEnabled(false);
 		}
 		watcher = null;
 	}
 	private String linesToStr(List<String> lines) {
 		StringBuilder sb = new StringBuilder();
 		for (String line : lines) {
 			sb.append(line+"\n");
 		}
 		return sb.toString();
 	}
 	private static LogFileReader createLogReader(String user,String host, String pass, String key, String logFile, String filter, String ignore) {
 		LogFileReader inst = null;
 		try {
 			inst = new LogFileReader(substValues(user), substValues(host), substValues(pass), substValues(key), substValues(logFile));
 			inst.setFilter(substValues(filter));
 			inst.setIgnore(substValues(ignore));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return inst;
 	}
 	private boolean isEnabledGlobal() {
 		return !System.getProperty("com.redhat.qe.tools.checklog.disabled","false").toLowerCase().equals("true");
 	}
 	/**
 	 * this handles watcher together with a flag whether it's enabled or not
 	 * @author lzoubek
 	 *
 	 */
 	private static class RemoteLogHandle {
 		private final List<LogFileReader> logs = new ArrayList<LogFileReader>();
 		private boolean enabled = true;
 		private boolean assertFailed = true;
 		private final String level;
 		private RemoteLogHandle(String level,boolean enabled, boolean assertFailed) {
 			this.enabled = enabled;
 			this.level = level;
 			this.assertFailed = assertFailed;
 		}
 
 		public boolean isAssertFailed() {
 			return assertFailed;
 		}
 
 		public void setEnabled(boolean enabled) {
 			this.enabled = enabled;
 		}
 		public boolean isEnabled() {
 			return enabled;
 		}
 		public List<LogFileReader> getLogs() {
 			return logs;
 		}
 		public void disconnect() {
 			for (LogFileReader rla : getLogs()) {
 				rla.disconnect();
 			}
 		}
 		@Override
 		public String toString() {
 			StringBuilder sb = new StringBuilder("[level="+level+",enabled="+isEnabled());
 			for (LogFileReader rla : getLogs()) {
 				sb.append(","+rla.toString());
 			}
 			sb.append("]");
 			return sb.toString();
 		}
 
 		public void watch() {			
 			for (LogFileReader rla : getLogs()) {
 				try {
 					rla.watch();
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		}
 	}
 	@CheckLog(logs={@LogFile})
 	private static class DefaultAnnotationValues {
 		
 	}
 }
