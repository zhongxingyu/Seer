 package info.emptybrain.myna;
 
 import java.io.*;
 import java.net.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.sql.DataSource;
 import org.mozilla.javascript.*;
 import java.util.*;
 import java.util.regex.*;
 import java.sql.*;
 import org.apache.jcs.*;
 import org.apache.jcs.engine.behavior.*;
 import org.apache.commons.pool.impl.*;
 import org.apache.commons.dbcp.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory; 
 import EDU.oswego.cs.dl.util.concurrent.*;
 import org.openid4java.consumer.ConsumerManager;
 
 /**
 * This class handles the execution of *.sjs and *.ejs files
 *
 */
 public class MynaThread {
 	//static public Log logger = LogFactory.getLog(MynaThread.class);
	static public String version="1.0_alpha_17-3";
 	static public FIFOSemaphore threadPermit;
 	static public FIFOSemaphore manageLocksPermit;
 	static public volatile CopyOnWriteArrayList runningThreads 	= new CopyOnWriteArrayList();
 	static public volatile CopyOnWriteArrayList recentThreads 	= new CopyOnWriteArrayList();
 	
 	static public Hashtable cron = new Hashtable();
 	
 	static public Hashtable dataSources = new Hashtable(); //stores ds properties
 	static public Hashtable javaDataSources = new Hashtable(); //stores dbcp BasicDataSource instances
 		
 	static public Hashtable locks = new Hashtable(); //used by Myna.getLock()
 	static public Hashtable scriptCache = new Hashtable();
 	static public Properties generalProperties = new Properties();
 	static public Hashtable serverVarMap = new Hashtable();//used by $server.get/set
 	
 	public final java.lang.Thread javaThread = java.lang.Thread.currentThread();
 	
 	//static private Hashtable compiledScripts_ = new Hashtable();
 	static private boolean isInitialized = false;
 	static volatile private Scriptable sharedScope_ = null;
 	
 	static public int threadHistorySize=0; // number of completed threads to store in recentThreads. Set with property "thread_history_size"
 	static public java.util.Date serverStarted = new java.util.Date(); //date object representing the time this server was started
 	
 	static public ConsumerManager openidConsumerManager; //initialized in init
 	
 	
 	
 	
 /* End static resources */	
 	
 	public boolean isInitThread = false; //Is this the first thread after server restart?
 	public boolean isWaiting=true;// am I waiting for a thread permit?
 	public boolean isWhiteListedThread=false;// can  I  bypass thread management?
 	public int requestTimeout=0; //maximum time this thread should run. Set in general.properties
 	public boolean shouldDie=false; //if true, this thread will be killed in objserveInstructionCount
 	
 	public boolean inError = false; //Are we currently handling an error?
 	
 	public Hashtable environment = new Hashtable();
 	public Hashtable runtimeStats = new Hashtable();
 	
 	public Context threadContext;
 	public ScriptableObject threadScope;
 	
 	static public String rootDir=null; // system path to the Myna deployment folder
 	static public String rootUrl; // url to the myna root directory from / , not including protocol and server
 	
 	public String requestDir; // system path to the directory containing the originally requested script 
 	
 	public String currentDir; // system path to the directory containing the currently running script
 	public String currentScript; // text of the currently running script
 	
 	public String scriptName; //name of the currently running script
 	public String requestScriptName; // name of the originally requested script
 	
 	
 	public java.util.Date started = new java.util.Date(); //date object representing the time this request was started
 	
 	public StringBuffer generatedContent = new StringBuffer("");
 	
 	public Set uniqueIncludes_ = Collections.synchronizedSet(new HashSet());
 	
 	public Vector threadChain = new Vector(); //history of thread calls. Used to avoid infinite recursion
 
 	/* Custom Context to store execution time and a reference to the MynaThread
 	running in this context. The MynaThread must be set after aquiring a context 
 	for this to work */
 	static class MynaContext extends Context
 	{
 		 MynaThread mynaThread;
 	}
 	static class CustomContextFactory extends ContextFactory{
 		 // Override makeContext()
      protected Context makeContext()
      {
          MynaContext cx = new MynaContext();
          // Make Rhino runtime to call observeInstructionCount
          // each 10000 bytecode instructions
          cx.setInstructionObserverThreshold(10000);
 				 cx.getWrapFactory().setJavaPrimitiveWrap(false);
          return cx;
      }
 
 		 // Override observeInstructionCount(Context, int)
      protected void observeInstructionCount(Context cx, int instructionCount)
      {
 				int timeout=0;
 				long currentTime = System.currentTimeMillis();
 				long startTime =0;
 				MynaContext mcx =null;
 				if (currentTime - MynaThread.serverStarted.getTime() < 30000) return;
 				try{
 					mcx = (MynaContext)cx;
 					if (mcx == null) return;
 					if (mcx.mynaThread == null) return;
 					if (mcx.mynaThread.shouldDie){
 						mcx.reportError("Request killed via Myna Adminstrator"); 
 						return;
 					}
 					
 					startTime =mcx.mynaThread.started.getTime();
 					timeout = mcx.mynaThread.requestTimeout;
 				} catch(Throwable e){return;}
 				
 				
 				if (timeout != 0 && currentTime - startTime > timeout*1000){
 					// More then 10 seconds from Context creation time:
 					// it is time to stop the script.
 					// Throw Error instance to ensure that script will never
 					// get control back through catch or finally.
 					mcx.reportError("Request time of "+ ((currentTime - startTime)/1000) +" seconds exceeded timeout of " + timeout +" seconds.");
 				}
      }
 
 		protected boolean hasFeature(Context cx, int featureIndex)
 		{
       MynaContext mcx =null;
       try{
         mcx = (MynaContext)cx;
         if (mcx.mynaThread == null) mcx =null;
       } catch(Throwable e){}
       
 			if (			featureIndex == Context.FEATURE_DYNAMIC_SCOPE 
 					|| 	featureIndex == Context.FEATURE_LOCATION_INFORMATION_IN_ERROR
 					||		featureIndex == Context.FEATURE_ENHANCED_JAVA_ACCESS
 			) {
 				return true;
 			}
 			if (featureIndex == Context.FEATURE_STRICT_MODE) {
 				//return true;
 				if (MynaThread.generalProperties.getProperty("strict_error_checking").equals("1")){
 					return true;
 				} else {
 					return false;	
 				} 
 			}
 			if (featureIndex == Context.FEATURE_WARNING_AS_ERROR) {
 				return false;
 				/* if (MynaThread.generalProperties.getProperty("strict_error_checking").equals("1")){
 					return true;
 				} else {
 					return false;	
 				} */
 			}
 			
 			if (featureIndex == Context.FEATURE_E4X) {
 				return true;
 			}
 			 
 			
 			return super.hasFeature(cx, featureIndex);
 		}
 	}
 	
 	static class MynaErrorReporter implements ErrorReporter{
 		static final MynaErrorReporter instance = new MynaErrorReporter();
 
 		private boolean forEval;
 		private ErrorReporter chainedReporter;
 	
 		private MynaErrorReporter() { }
 	
 		static ErrorReporter forEval(ErrorReporter reporter)
 		{
 			MynaErrorReporter r = new MynaErrorReporter();
 			r.forEval = true;
 			r.chainedReporter = reporter;
 			return r;
 		}
 	
 		public void warning(String message, String sourceURI, int line,	String lineText, int lineOffset)
 		{
 			if (message.indexOf("Referenced to undefined property \"") == 0) return;
 			if (message.indexOf("Reference to undefined property \"") == 0) return;
 			if (message.indexOf("Code has no side effects") == 0) return;
 			if (MynaThread.generalProperties.getProperty("strict_error_checking").equals("1")){
         /* errorText.append("<p>Stack Trace:<br><pre>");
 				
 				StringWriter traceStringWriter = new StringWriter();
 				PrintWriter tracePrintWriter = new PrintWriter(traceStringWriter);
 				
 				originalException.printStackTrace(tracePrintWriter);
 				errorText.append(traceStringWriter.toString());
 				errorText.append("</pre>"); */
         try {
           MynaContext mcx = (MynaContext) Context.getCurrentContext();
           if (mcx.mynaThread != null)
           mcx.mynaThread.log("WARNING",message,"File: " + sourceURI + "<br>Line: " + line + "<br> column: " + lineOffset +"<br>Contaxt:<br><br>" +lineText);
       } catch (Exception e){
           System.out.println("WARNING"+":"+message+":"+"File: " + sourceURI + "<br>Line: " + line + "<br> column: " + lineOffset +"<br>Contaxt:<br><br>" +lineText); 
       }
 				//throw runtimeError(message, sourceURI, line, lineText, lineOffset);
 			} else {
 				if (chainedReporter != null) {
 					chainedReporter.warning(
 						message, sourceURI, line, lineText, lineOffset);
 				} else {
 					// Do nothing
 				}	
 			}
 		}
 		public void error(String message, String sourceURI, int line, String lineText, int lineOffset)
 		{
 			if (message.indexOf("Code has no side effects") == 0) return;
 			if (message.indexOf("Compilation") == 0) return;
 			if (forEval) {
 				// Assume error message strings that start with "TypeError: "
 				// should become TypeError exceptions. A bit of a hack, but we
 				// don't want to change the ErrorReporter interface.
 				String error = "SyntaxError";
 				final String TYPE_ERROR_NAME = "TypeError";
 				final String DELIMETER = ": ";
 				final String prefix = TYPE_ERROR_NAME + DELIMETER;
 				if (message.startsWith(prefix)) {
 					error = TYPE_ERROR_NAME;
 					message = message.substring(prefix.length());
 				}
 				throw ScriptRuntime.constructError(error, message, sourceURI, 
 												   line, lineText, lineOffset);
 			}
 			if (chainedReporter != null) {
 				
 				chainedReporter.error(
 					message, sourceURI, line, lineText, lineOffset);
 			} else {
 				/* this was a hack for rhino < 1.7. In those versions, with strict 
 					warnigns it was not possible to check for the existance of 
 					an undefined property without triggering the 
 					"Referenced to undefined property" error. Now with 1.7 the
 					hack is no longer necessary
 				*/
 				if (message.indexOf("Referenced to undefined property \"") == 0) return;
 				
 				throw runtimeError(
 					message, sourceURI, line, lineText, lineOffset);
 			}
 		}
 	
 		public EvaluatorException runtimeError(String message, String sourceURI,
 											   int line, String lineText,
 											   int lineOffset)
 		{
 			
 			if (chainedReporter != null) {
 				return chainedReporter.runtimeError(
 					message, sourceURI, line, lineText, lineOffset);
 			} else {
 				return new EvaluatorException(
 					message, sourceURI, line, lineText, lineOffset);
 			}
 		}
 	}
 	
 	static {
 		ContextFactory.initGlobal(new CustomContextFactory());
 	}
 	
 	
 	public MynaThread() throws Exception{
 		
 		//ContextFactory.initGlobal(new CustomContextFactory());
 	}
 	
 	
 	/**
 	* init loads stanard objects (settings, etc)
 	*
 	*/
 	public void init() throws Exception{
 		synchronized (MynaThread.class){
 			try {
 				openidConsumerManager = new ConsumerManager();
 			} catch(Exception e){
 				handleError(e);
 			}
 			
 			loadGeneralProperties();
 			
 			int max_running_threads = Integer.parseInt(generalProperties.getProperty("max_running_threads"));
 			threadPermit = new FIFOSemaphore(max_running_threads);
 			manageLocksPermit = new FIFOSemaphore(1);
 			
 			this.threadHistorySize = Integer.parseInt(generalProperties.getProperty("thread_history_size"));
 			
 			loadDataSources();
 			createSharedScope();
 			this.isInitialized = true;
 		
 		}
 	}
 	
 	public  Scriptable createSharedScope() throws Exception{
 		synchronized (MynaThread.class){
 			runtimeStats.put("currentTask","Building Shared Scope");
 			if (Integer.parseInt(this.generalProperties.getProperty("optimization.level")) != -1
 					&& MynaThread.sharedScope_ != null)
 			{
 				return MynaThread.sharedScope_;
 			}
 			
 			
 			Context cx = this.threadContext = new CustomContextFactory().enter();
 			((MynaContext) (cx)).mynaThread =this;
 			
 			cx.setErrorReporter(new MynaErrorReporter());
 			
 			
 			try{
 				
 				Scriptable sharedScope = MynaThread.sharedScope_ = this.threadScope =  new ImporterTopLevel(cx);
 				//cx.initStandardObjects(null,false);
 				
 				Object server_gateway = Context.javaToJS(this,sharedScope);
 			
 			
 				ScriptableObject.putProperty(sharedScope, "$server_gateway", server_gateway);
 				
 				String standardLibs = MynaThread.generalProperties.getProperty("standard_libs");
 				Object[] existingObjectsArray = ((ScriptableObject)sharedScope).getAllIds();
 				HashSet existingObjects = new HashSet(Arrays.asList(existingObjectsArray));
 				
 				
 				
 				if (standardLibs != null){
 					String[] libPaths=standardLibs.split(",");
 					URI sharedPath = new URI(this.rootDir).resolve("shared/js/"); 
 					URI curUri;
 					for (int x=0; x < libPaths.length;++x){
 						curUri = new URI(libPaths[x]);
 						if (!curUri.isAbsolute()){
 							curUri = sharedPath.resolve(new URI(libPaths[x]));
 						}
 						
 						if (!curUri.isAbsolute() || !new File(curUri).exists()){
 							throw new IOException("Cannot find '" +libPaths[x] +"'  in system root directory or in '"+sharedPath.toString() +"'. See standard_libs in WEB-INF/classes/general.properties.");	
 						}
 						
 						String scriptPath = curUri.toString();
 						int lastSlash = scriptPath.lastIndexOf("/");
 						this.currentDir = new URI(scriptPath.substring(0,lastSlash+1)).toString();
 						String script = readScript(scriptPath);
 						script = translateString(script,scriptPath); 
 						cx.evaluateString(sharedScope, script, scriptPath, 1, null);
 					}
 				}
 			
 				Object[] sharedIds = ((ScriptableObject)sharedScope).getAllIds();
 				
 				//add some other ids we're concerned about
 				Object[] moreIds = {"Array","Object","Date"};
 				int totalIds = sharedIds.length + moreIds.length; 
 				Object[] ids = new Object[totalIds];
 				System.arraycopy(sharedIds, 0, ids, 0, sharedIds.length);
 				System.arraycopy(moreIds, 0, ids, sharedIds.length, moreIds.length);
 
 				
 				
 				for (int x=0;x<ids.length;++x){
 					if (!existingObjects.contains(ids[x])){
 						try{
 							ScriptableObject lib =(ScriptableObject) sharedScope.get(ids[x].toString(),sharedScope);
 							lib.sealObject();
 							try{
 								ScriptableObject proto =(ScriptableObject) lib.get("prototype",lib);
 								proto.sealObject();
 							}catch(Exception e){}// seal what we can
 							
 						}catch(Exception e){}// seal what we can
 					}
 				}
 				MynaThread.sharedScope_ = sharedScope;
 				return sharedScope;
 			} catch (Exception e){
 				this.handleError(e);
 				return MynaThread.sharedScope_; //should never get here but if we do...
 			} finally {
 				cx.exit();
 				
 			}
 		} //end synchronized
 	}
 	
 	
 	/**
 	* entry point for MynaThread
 	* 
 	*
 	*/
 	public void handleRequest (String scriptPath) throws Exception{
 		runningThreads.add(this);
 		runtimeStats.put("threadId",this.toString());
 		runtimeStats.put("started",this.started);
 		
 		int lastSlash = scriptPath.lastIndexOf("/");
 		this.currentDir = new URI(scriptPath.substring(0,lastSlash+1)).toString();
 		this.requestDir = this.currentDir;
 		
 		this.scriptName = scriptPath.substring(lastSlash+1);
 		this.requestScriptName = this.scriptName; 
 		synchronized (MynaThread.class){
 			if (!isInitialized) {
 				this.init();
 				this.isInitThread=true;
 				log("INFO","Starting Myna Application Server","");
 				
 			}
 		}
 		
 		this.requestTimeout= Integer.parseInt(generalProperties.getProperty("request_timeout"));
 		
 		//Scriptable sharedScope = createSharedScope();
 		
 		//reset currentDir after createing shared scope
 		this.currentDir = new URI(scriptPath.substring(0,lastSlash+1)).toString();
 		
 		
 		
 		
 	 	class LocalContextAction implements ContextAction {
 			private MynaThread mt;
 			public LocalContextAction (MynaThread mt){
 				this.mt = mt;
 			}
 			public Object run(Context cx) {
 				try {
 					//bind the current MynaThread to this context
 					((MynaContext) (cx)).mynaThread =mt;
 					Scriptable sharedScope = mt.sharedScope_;
 					
 					//this means there was an error creating the scope, and we just need
 					//to get out of the way and let the error display
 					 
 					if (sharedScope == null) return null;
 					threadContext =cx;
 					cx.setErrorReporter(new MynaErrorReporter());
 					ScriptableObject scope = threadScope = (ScriptableObject) cx.newObject(sharedScope);
 					scope.setPrototype(sharedScope);
 					scope.setParentScope(null);
 					
 					runtimeStats.put("currentTask","Waiting in thread Queue");
 					
 					
 					if (generalProperties.getProperty("thread_whitelist").length() > 0){
 						String[] whitelist=generalProperties.getProperty("thread_whitelist").split(",");
 						int x=0;
 						for (;x<whitelist.length;++x){
 							if (requestDir.matches(whitelist[x])) isWhiteListedThread=true;
 						}
 					}
 					
 					//wait if max threads are already running
 					if (!isWhiteListedThread) threadPermit.acquire();
 					
 					isWaiting=false;
 					try{
 						Object server_gateway = Context.javaToJS(this.mt,scope);
 						ScriptableObject.putProperty(scope, "$server_gateway", server_gateway);
 						URI sharedPath = new URI(rootDir).resolve("shared/js/");
 					
 						//execute script file
 						try{
 							String requestHandler = generalProperties.getProperty("request_handler");
 							URI requestHandlerPath = new URI(requestHandler);
 							if (!requestHandlerPath.isAbsolute()){
 								requestHandlerPath = sharedPath.resolve(requestHandlerPath);
 							}
 							if (!requestHandlerPath.isAbsolute() || !new File(requestHandlerPath).exists()){
 								throw new IOException("Cannot find '" +requestHandlerPath +"' in system root directory or in '"+sharedPath.toString() +"'. See runtime_scripts in WEB-INF/classes/general.properties.");	
 							}
 							executeJsFile(scope, requestHandlerPath.toString());
 							
 						} catch (Exception e){
 							handleError(e);
 						}
 					
 					} catch (Exception e){
 						handleError(e);
 						
 					} finally {
 							//release our threadPermit
 							if (!isWhiteListedThread) threadPermit.release();
 							//remove this thread form the running list
 							runningThreads.remove(this.mt);
 					}
 					return null;
 				} catch (Exception outer){
 					throw new WrappedException(outer);
 				}
 			}
 		}
 		new CustomContextFactory().call(new LocalContextAction(this));  
 	
 		 
 		/* Scriptable sharedScope = sharedScope_;
 		 
 		//this means there was an error creating the scope, and we just need
 		//to get out of the way and let the error display
 		if (sharedScope == null) return;
 		Context cx = this.threadContext = new CustomContextFactory().enter();
 		cx.setErrorReporter(new MynaErrorReporter());
 		ScriptableObject scope = this.threadScope = (ScriptableObject) cx.newObject(sharedScope);
 		scope.setPrototype(sharedScope);
 		scope.setParentScope(null);
 		
 		runtimeStats.put("currentTask","Waiting in thread Queue");
 		
 		
 		if (generalProperties.getProperty("thread_whitelist").length() > 0){
 			String[] whitelist=generalProperties.getProperty("thread_whitelist").split(",");
 			int x=0;
 			for (;x<whitelist.length;++x){
 				if (this.requestDir.matches(whitelist[x])) isWhiteListedThread=true;
 			}
 		}
 		
 		//wait if max threads are already running
 		if (!isWhiteListedThread) threadPermit.acquire();
 		
 		this.isWaiting=false;
 		try{
 			Object server_gateway = Context.javaToJS(this,scope);
 			ScriptableObject.putProperty(scope, "$server_gateway", server_gateway);
 			URI sharedPath = new URI(this.rootDir).resolve("shared/js/");
 		
 			//execute script file
 			try{
 				String requestHandler = this.generalProperties.getProperty("request_handler");
 				URI requestHandlerPath = new URI(requestHandler);
 				if (!requestHandlerPath.isAbsolute()){
 					requestHandlerPath = sharedPath.resolve(requestHandlerPath);
 				}
 				if (!requestHandlerPath.isAbsolute() || !new File(requestHandlerPath).exists()){
 					throw new IOException("Cannot find '" +requestHandlerPath +"' in system root directory or in '"+sharedPath.toString() +"'. See runtime_scripts in WEB-INF/classes/general.properties.");	
 				}
 				this.executeJsFile(scope, requestHandlerPath.toString());
 			} catch (Exception e){
 				this.handleError(e);
 			}
 		
 		} catch (Exception e){
 			this.handleError(e);
 			
 		} finally {
 			//release our threadPermit
 			if (!isWhiteListedThread) threadPermit.release();
 			//remove this thread form the running list
 			runningThreads.remove(this);
 			// Exit from the context.
 			Context.exit(); 
 			
 		} */
 	}
 	
 	public void callFunction (MynaThread parentThread, String f,Object[] args) throws Exception{
 		runningThreads.add(this);
 		runtimeStats.put("threadId",this.toString());
 		runtimeStats.put("started",this.started);
 		
 		this.rootDir = parentThread.rootDir;
 		this.rootUrl = parentThread.rootUrl;
 		this.currentDir = parentThread.currentDir;
 		this.requestDir = parentThread.requestDir;
 		this.scriptName = parentThread.scriptName;
 		this.requestScriptName = parentThread.requestScriptName;
 		this.requestTimeout= Integer.parseInt(generalProperties.getProperty("request_timeout"));
 		
 		
 		
 		this.environment.put("threadFunctionSource",f);
 		this.environment.put("threadFunctionArguments",args);
 		
 	 	class LocalContextAction implements ContextAction {
 			private MynaThread mt;
 			private MynaThread pt;
 			private String f;
 			private Object[] args;
 			public LocalContextAction (MynaThread pt,MynaThread mt,String f,Object[] args){
 				this.mt = mt;
 				this.pt = pt;
 				this.f = f;
 				this.args = args;
 			}
 			public Object run(Context cx) {
 				try {
 					Scriptable sharedScope = mt.sharedScope_;
 										 
 					if (sharedScope == null) return null;
 					threadContext =cx;
 					cx.setErrorReporter(new MynaErrorReporter());
 					ScriptableObject scope = threadScope = (ScriptableObject) cx.newObject(sharedScope);
 					scope.setPrototype(sharedScope);
 					scope.setParentScope(null);
 					//f.setParentScope(scope);
 					//f.setParentScope(null);
 					//f.setParentScope(pt.threadScope);
 					isWaiting=false;
 					try{
 						Object server_gateway = Context.javaToJS(this.mt,scope);
 						ScriptableObject.putProperty(scope, "$server_gateway", server_gateway);
 						//ScriptableObject.putProperty(f.getParentScope(), "$server_gateway", server_gateway);
 						URI sharedPath = new URI(rootDir).resolve("shared/js/");
 						//execute script file
 						try{
 							String requestHandler = generalProperties.getProperty("request_handler");
 							URI requestHandlerPath = new URI(requestHandler);
 							if (!requestHandlerPath.isAbsolute()){
 								requestHandlerPath = sharedPath.resolve(requestHandlerPath);
 							}
 							if (!requestHandlerPath.isAbsolute() || !new File(requestHandlerPath).exists()){
 								throw new IOException("Cannot find '" +requestHandlerPath +"' in system root directory or in '"+sharedPath.toString() +"'. See runtime_scripts in WEB-INF/classes/general.properties.");	
 							}
 							executeJsFile(scope, requestHandlerPath.toString());
 							//return f.call(cx, scope, scope, args);
 						
 						} catch (Exception e){
 							handleError(e);
 						}
 					
 					} catch (Exception e){
 						handleError(e);
 						
 					} finally {
 							//release our threadPermit
 							if (!isWhiteListedThread) threadPermit.release();
 							//remove this thread form the running list
 							runningThreads.remove(this.mt);
 					}
 					return null;
 				} catch (Exception outer){
 					throw new WrappedException(outer);
 				}
 			}
 		}
 		//System.out.println(Thread.currentThread().getId()+":"+this.hashCode());
 		new CustomContextFactory().call(new LocalContextAction(parentThread,this,f,args));  
 	}
 	/**
 	* handles errors during JS execution
 	*
 	*/
 	public void handleError(Exception originalException) throws Exception{
 		/* if (this.inError) return;*/
 		this.inError = true; 
 		System.err.println(originalException);
 		
 		try{
 			if (!this.inError &&(originalException instanceof RhinoException || originalException instanceof EcmaError)){
 				Object exception = Context.javaToJS(originalException,this.threadScope);
 				ScriptableObject.putProperty(this.threadScope, "exception", exception);
 				this.executeJsString(this.threadScope,"$application._onError(exception);","Compile Error");
 				
 				/* RhinoException e = (RhinoException) originalException;
 				
 				if (new String(e.getMessage()).indexOf("___MYNA_ABORT___") == 0) return;
 				
 				String[] lines;
 				
 				StringWriter originalTrace = new StringWriter();
 				PrintWriter pw = new PrintWriter(originalTrace);
 				
 				e.printStackTrace(pw);
 				
 				lines = originalTrace.toString().split("\n");
 				StringBuffer stackTrace = new StringBuffer();
 				StringBuffer level = new StringBuffer();
 				
 				Pattern jsError1 = Pattern.compile("^.*\\(file:(.*?..js)[:|#](\\d*)\\)");
 				String curFileLine="";
 				String lastFileLine="";
 				String originalFile="";
 				String originalLine="";
 				for (int x=0; x < lines.length; ++x){
 					Matcher currentLine = jsError1.matcher(lines[x]);
 					if (currentLine.matches()){
 						
 						curFileLine =  currentLine.group(1) + " at line " + currentLine.group(2);
 						if (originalFile.length() == 0){
 							originalFile = currentLine.group(1);
 							originalLine = currentLine.group(2);
 						}
 						if (!curFileLine.equals(lastFileLine)){
 							level.append("  ");
 							stackTrace.append(level + curFileLine + "\n");
 							lastFileLine = curFileLine;
 						}
 						
 					} 
 				}
 				
 				StringBuffer before = new StringBuffer();
 				String errorLine = new String();
 				StringBuffer after = new StringBuffer();
 				String errorMessage =e.getMessage();
 				
 				this.generatedContent.append(errorMessage.substring(0,errorMessage.indexOf("("))+ " (" +originalFile +":" +originalLine+")" + "<br>");
 								
 				int lineNumber =e.lineNumber();
 				String errorFile = e.sourceName();
 				if (Integer.parseInt(originalLine) != lineNumber){
 					lineNumber =Integer.parseInt(originalLine);
 					errorFile="file:" + originalFile;
 				}
 				
 				try{
 					String errorScript = "\n" + readScript(errorFile );
 					lines = errorScript.split("\n");
 					
 					int context = 5;
 					for (int x=Math.max(0,lineNumber-context); x < Math.min(lines.length, lineNumber + context+1); ++x ){
 						String currentLine = lines[x];
 						currentLine = currentLine.replaceAll("<","&lt;").replaceAll("\t","   ");
 						
 						if (x == lineNumber){
 							errorLine = "<b style='color:red'>" + x + ": " + currentLine + "</b>\n";	
 						} else if (x < lineNumber){
 							before.append(x + ": " + currentLine + "\n");
 						} else if (x > lineNumber ){
 							after.append(x + ": " + currentLine + "\n");
 						}
 					}
 				
 					
 					this.generatedContent.append("<pre>" + before + errorLine + after + "</pre><br>");
 				} catch (Exception d){}
 				
 				this.generatedContent.append("<br>Javascript Stack Trace:<br><pre>" + stackTrace + "</pre><p>");
 				this.generatedContent.append("<br>Full Stack Trace:<br><pre>");
 				
 				StringWriter traceStringWriter = new StringWriter();
 				PrintWriter tracePrintWriter = new PrintWriter(traceStringWriter);
 				
 				e.printStackTrace(tracePrintWriter);
 				this.generatedContent.append(traceStringWriter.toString() +"</pre><p>"); */
 					
 			} else {
 				StringBuffer errorText = new StringBuffer();
 				
 				errorText.append(originalException.getClass().getName() + ": ");
 				errorText.append(originalException.getMessage() + "<br>");
 				errorText.append("<p>Stack Trace:<br><pre>");
 				
 				StringWriter traceStringWriter = new StringWriter();
 				PrintWriter tracePrintWriter = new PrintWriter(traceStringWriter);
 				
 				originalException.printStackTrace(tracePrintWriter);
 				errorText.append(traceStringWriter.toString());
 				errorText.append("</pre>");
 				
 				this.log("ERROR",originalException.getClass().getName() + ": "+ originalException.getMessage(),errorText.toString());
 				this.generatedContent.append(errorText.toString());
 			}
 		} catch (Exception newException){
 			System.err.println(newException);
 			this.log("ERROR","Error parsing exception: " +newException.getClass().getName() + ": "+ newException.getMessage(),newException.toString());
 				
 			throw originalException; //if there is a problem displaying the error, just rethrow the original error.	
 		}
 	}
 	
 	
 	/**
 	* Executes the supplied JavaScript.
 	*
 	*  
 	* @param  scope Top level Javascript scope
 	* @param  script String containing the JavaScript code to execute
 	* @param  scriptPath Filesystem path to the file containg script
 	*/
 	public void executeJsString(Scriptable scope, String script, String scriptPath) throws Exception{
 		Context cx = this.threadContext; 
 		long start=0;
 		long end=0;
 		start = new java.util.Date().getTime();
 		try {
 			int optimizationLevel = Integer.parseInt(this.generalProperties.getProperty("optimization.level"));
 			
 			cx.setOptimizationLevel(optimizationLevel);
 			JCS cache = JCS.getInstance("scriptCache");
 			//scriptPath =getNormalizedPath(scriptPath) 
 			int key = script.hashCode();
 			Script compiled= (Script) cache.get(key);
 			
 			script = translateString(script,scriptPath);
 			this.currentScript = script;
 			
 			if (compiled == null){
 				/* reference to original lexer/parser, just in case the new one becomes problematic */
 					/* script = parseEmbeddedJsBlocks(script);
 					if (scriptPath.matches(".*.ejs")) {
 						script = parseEmbeddedJs(script);
 					} */ 
 					
 				
 				  
 				compiled = cx.compileString(script, scriptPath, 1, null);
 				/*if (optimizationLevel > -1){*/
 					cache.put(key,compiled);
 					//System.out.println(scriptPath.toString() +" =  " + compiled);
 				/*}*/
 			} 
 			//Object server_gateway = Context.javaToJS(this,scope);
 			//ScriptableObject.putProperty(scope, "$server_gateway", server_gateway);
 			compiled.exec(cx,scope);
 		} catch (Exception e){
 			this.handleError(e);
 		}
 		
 	}
 	
 	void executeJsFile(Scriptable scope, String scriptPath) throws Exception{
 		File scriptFile;
 		scriptPath = getNormalizedPath(scriptPath);
 		JCS cache = JCS.getInstance("scriptCache");
 		try {
 			 scriptFile = new File(new URI(scriptPath));
 		} catch (java.lang.IllegalArgumentException e){
 			throw new java.lang.IllegalArgumentException("'" +scriptPath + "' is not a valid URI.");
 		}
 		
 		try {
 			if (cache.get(scriptPath) != null){
 				IElementAttributes attributes = cache.getCacheElement(scriptPath).getElementAttributes();
 				if ( scriptFile.lastModified() > attributes.getCreateTime()){
 					cache.remove(scriptPath); //clear compiled copy when a newer version of the file is available 
 				}
 			}
 		} catch (org.apache.jcs.access.exception.CacheException e){
 			//assume cache exceptions mean it is unavailable	
 		}
 		String script = (String) cache.get(scriptPath); 
 		if (script == null){
 			script = readScript(scriptPath);
 			cache.put(scriptPath,script);
 		}
 		executeJsString(scope, script, scriptPath);
 	}
 	
 	/**
 	* Escapes text so it can be safely inserted into a JS string literal. 
 	*
 	* @param  text text tot escape
 	* @return  escaped text
 	*/
 	public String JSEscape(String text) {
 		return text.replaceAll("\n","\\\\n")
 			.replaceAll("'","\\\\'")
 			.replaceAll("\"","\\\\\\\"");
 	}
 	
 	/**
 	* loads datasource settings into memory
 	*
 	*/
 	public void loadDataSources() throws Exception{
 		File dsDir = this.getNormalizedFile(MynaThread.generalProperties.getProperty("datasource_directory"));
 		dsDir.mkdirs();  //make ds dir if necessary
 		File[] dataSourcesPaths = dsDir.listFiles(new java.io.FileFilter(){
 			public boolean accept(File path){
 				String pathname = path.getName();
 				return (path.getName().substring(pathname.length() -3).equals(".ds"));
 			}
 		});
 		
 		if (dataSourcesPaths != null){
 			for (int x =0; x < dataSourcesPaths.length; ++x){
 				try{
 					loadDataSource(dataSourcesPaths[x],false);
 				} catch (Exception e){
 					handleError(e);
 				} 
 			}
 		}
 		
 		
 	}
 	
 	public void loadDataSource(File path, boolean shouldTest) throws Exception{
 		String dsName = path.getName().substring(0,path.getName().length() -3).toLowerCase();
 		try{
 			Properties ds = new Properties();
 			ds.load(new java.io.FileInputStream(path));
 			
 			if (ds.getProperty("url") == null || ds.getProperty("url").length() == 0) throw new Exception("No url defined");
 			if (ds.getProperty("driver") == null || ds.getProperty("driver").length() == 0) throw new Exception("No driver defined");
 			dataSources.put(dsName,ds);
 			
       /* special handling of file paths */
 			if (
 				!ds.getProperty("type").equals("other") 
 				&& ds.getProperty("location").equals("file")
 				&& ds.getProperty("file") != null
 				&& !new File(ds.getProperty("file")).exists()
 			){
 			 	String newPath = getNormalizedPath(ds.getProperty("file"))
 				.replaceAll("file:","").replaceAll("//","/").replaceAll("%20"," ");
 				ds.setProperty("url",
 					ds.getProperty("url").replaceAll(
 						ds.getProperty("file"),
 						newPath 
 					)
 				);
 			}
 			//register connection pool
 			
 			GenericObjectPool connectionPool = new GenericObjectPool(null);
 			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
 				ds.getProperty("url"), 
 				ds.getProperty("username"),
 				ds.getProperty("password")
 			);
 			PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
 			PoolingDriver driver = new PoolingDriver();
 			driver.setAccessToUnderlyingConnectionAllowed(true);
 			
 			driver.registerPool(dsName,connectionPool);
 			Class.forName(ds.getProperty("driver"));
 			if (shouldTest) {
 				//this line should trigger an error if the datsource is incorrect or unavailable
 				java.sql.DriverManager.getConnection("jdbc:apache:commons:dbcp:" + dsName).close();
 			}
 			
 			BasicDataSource bds = new BasicDataSource();
 			bds.setDriverClassName(ds.getProperty("driver"));
 			bds.setUsername(ds.getProperty("username"));
 			bds.setPassword(ds.getProperty("password"));
 			bds.setUrl(ds.getProperty("url"));
 			this.javaDataSources.put(dsName,bds);
 
 			
 		} catch(Exception e){
 			this.log("ERROR","Error Loading Datasource " + dsName +": " +e.getMessage(),e.toString());
 			
 			//throw new Exception("Error loading Datasource '" + dsName +"' : '" + e.getMessage() + "'");	
 		}
 	}
 	
 	/**
 	* loads general settings into memory
 	*
 	*/
 	public void loadGeneralProperties() throws Exception{
 		this.generalProperties.load(getClass().getResourceAsStream("/general.properties"));
 		boolean propsChanged = false;
 		
 		//set defaults for properties not in previous releases
 		if (generalProperties.getProperty("max_running_threads") == null){
 			generalProperties.setProperty("max_running_threads","5");
 			propsChanged=true;
 		}
 		if (generalProperties.getProperty("request_timeout") == null){
 			generalProperties.setProperty("request_timeout","30");
 			propsChanged=true;
 		}
 		if (generalProperties.getProperty("thread_history_size") == null){
 			generalProperties.setProperty("thread_history_size","20");
 			propsChanged=true;
 		}
 		if (generalProperties.getProperty("thread_whitelist") == null){
 			generalProperties.setProperty("thread_whitelist",".*/myna/administrator/.*");
 			propsChanged=true;
 		}
 		if (generalProperties.getProperty("smtp_host") == null){
 			generalProperties.setProperty("smtp_host","localhost");
 			propsChanged=true;
 		}
 		if (generalProperties.getProperty("server_start_scripts") == null){
 			generalProperties.setProperty("server_start_scripts","libOO/server_start.sjs");
 			propsChanged=true;
 		}
 		if (generalProperties.getProperty("version") == null 
 			|| !generalProperties.getProperty("version").equals(this.version)){
 			generalProperties.setProperty("version",this.version);
 			propsChanged=true;
 		}
 		if (generalProperties.getProperty("request_handler") == null){
 			generalProperties.setProperty("request_handler","libOO/request_handler.sjs");
 			propsChanged=true;
 		}
 		if (generalProperties.getProperty("instance_id") == null){
 			generalProperties.setProperty("instance_id","myna_instance");
 			propsChanged=true;
 		}
 		if (generalProperties.getProperty("instance_purpose") == null){
 			generalProperties.setProperty("instance_purpose","DEV");
 			propsChanged=true;
 		}
 		if (generalProperties.getProperty("administrator_email") == null){
 			generalProperties.setProperty("administrator_email","");
 			propsChanged=true;
 		}
 		if (generalProperties.getProperty("administrator_email_on_error") == null){
 			generalProperties.setProperty("administrator_email_on_error","0");
 			propsChanged=true;
 		}
 		if (propsChanged) saveGeneralProperties();
 	}
 	
 	/**
 	* writes a a simplified log entry from java. 
 	*
 	* @param type String type
 	* @param label String label
 	* @param detail String detail
 	*/
 	public void log(
 			String type,
 			String label,
 			String detail
 	) throws Exception{
 		
 		String requestId = "system";
 		String appName = "system";
 		
 		int logElapsed = 0;
 		long requestElapsed = System.currentTimeMillis() - this.started.getTime();
 		/* java.util.Calendar cal = Calendar.getInstance();
 		cal.setTime( new java.util.Date() ); */
 		java.sql.Timestamp eventTs = new java.sql.Timestamp(new java.util.Date().getTime());
 		this.writeLog(type,label,detail,appName,requestId,requestElapsed,logElapsed,eventTs);
 	}
 	
 	/**
 	* writes a message to myna_log, or standard out if DB is unavailable
 	*
 	* @param type String type
 	* @param label String label
 	* @param detail String detail
 	* @param appName String appName
 	* @param requestId String requestId
 	* @param logElapsed int logElapsed
 	*/
 	public void writeLog(
 			String type,
 			String label,
 			String detail,
 			String appName,
 			String requestId,
 			long requestElapsed,
 			int logElapsed,
 			java.util.Date eventTs
 	) throws Exception{
 		String purpose = generalProperties.getProperty("instance_purpose");
 		String logId = org.safehaus.uuid.UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
 		String instanceId = this.generalProperties.getProperty("instance_id");
 		String hostname = java.net.InetAddress.getLocalHost().getHostName();
 		
 		//java.sql.Timestamp eventTs = new java.sql.Timestamp(cal.getTime().getTime());
 		//long requestElapsed = System.currentTimeMillis() - this.started.getTime();
 		java.sql.Connection con=null;
 		java.sql.PreparedStatement st=null;
 		try {
 			con = java.sql.DriverManager.getConnection("jdbc:apache:commons:dbcp:myna_log");
 			String sql ="insert into myna_log_general"
 				+ "(log_id,request_id,instance_id,hostname,app_name,type,purpose,"
 				+ "label,detail,event_ts,request_elapsed,log_elapsed)"
 				+ "values(?,?,?,?,?,?,?,?,?,?,?,?)";
 			st = con.prepareStatement(sql,java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,java.sql.ResultSet.CONCUR_READ_ONLY);
 		
 			int index =0;
 			st.setString(++index,logId);
 			st.setString(++index,requestId);
 			st.setString(++index,instanceId);
 			st.setString(++index,hostname);
 			st.setString(++index,appName.toLowerCase());
 			st.setString(++index,type.toUpperCase());
 			st.setString(++index,purpose.toUpperCase());
 			st.setString(++index,label);
 			
 			try{ 
 				StringReader r = new StringReader( detail );
 				st.setCharacterStream( index+1, r, detail.length() );
 				++index;
 			} catch(Exception saveError){ 
 				st.setString(++index,detail.substring(0,3999));	
 			} 
 			st.setTimestamp(++index,new java.sql.Timestamp(eventTs.getTime()));
 			st.setLong(++index,requestElapsed);
 			st.setLong(++index,logElapsed);		
 			
 			//try to write oracle clob 
 			st.execute();	
 			if (con.getMetaData().getDatabaseProductName().equals("oracle")){
 				try{
 					this.executeJsString(
 						this.threadScope,
 						"new Myna.Query({ds:'myna_log',sql:'update myna_log_general set "
 						+"detail={detail:clob} where log_id={logid:bigint}',values:{detail:'"
 						+ JSEscape(detail) +"',log_id:" + logId + "}})",
 						"Oracle clob update"
 					);	
 				} catch(Exception oracleErrror){
 					
 				}
 			}
 			
 		}catch (Exception e){
 			System.out.println("Error writing log:" + e.toString() );
 			
 			System.out.println(eventTs 
 				+ ": " + instanceId 
 				+ ": " + appName 
 				+ ": " + label 
 				+ ": " + detail);
 		} finally{
 			if (st != null) st.close();
 			if (con != null) con.close();	
 		}
 	}
 	
 	/**
 	* saves general settings into disk
 	*
 	*/
 	public void saveGeneralProperties() throws Exception{
 		String path;
 		try {
 			path =getClass().getClassLoader().getResource("/general.properties").toString();
 		} catch (NullPointerException np){
 			path =getNormalizedPath("/WEB-INF/classes/general.properties");
 		}
 		URI uri = new URI(path);
 		
 		this.generalProperties.store(new java.io.FileOutputStream(new File(uri)),"Myna General Properties");
 	}
 	
 	/**
 	* Read text content from supplied absolute Filesystem path and returns Serverside JavaScript text.
 	*
 	*
 	* @param  scriptPath Path to script to read
 	* @return  Serverside JavaScript text. If the filename ends in .ejs, content is filtered through parseEmbeddedJs.
 	* @see #parseEmbeddedJs(String content)
 	*/
 	public String readScript(String scriptPath) throws Exception{
 		return org.apache.commons.io.FileUtils.readFileToString(new File(new URI(scriptPath)));
 		//if (true) throw new Exception(scriptPath );	
 		/* FileInputStream fis = new FileInputStream(new File(new URI(scriptPath)));
 		int x= fis.available();
 		byte b[]= new byte[x];
 		fis.read(b);
 		String script = new String(b);
 		fis.close(); 
 		
 		 
 		return  script;*/
 	}
 	
 	
 	public void sealObject(Object obj){
 		((ScriptableObject) obj).sealObject();	
 	}
 	
 	
 	/**
 	* loads and evaluates the supplied ejs or sjs file in the current context
 	*
 	* @param  scriptPath sjs or ejs file to include
 	*/
 	public void include(String scriptPath) throws Exception{
 		include(scriptPath,this.threadScope);
 	}
 	
 	public void include(String scriptPath, Scriptable scope) throws Exception{
 		String originalDir = this.currentDir;
 		String originalScriptName = this.scriptName;
 		
 		String realScriptPath = getNormalizedPath(scriptPath);
 		int lastSlash = realScriptPath.lastIndexOf("/");
 		
 		this.currentDir = realScriptPath.substring(0,lastSlash+1);
 		this.scriptName = realScriptPath.substring(lastSlash+1);
 		
 		this.executeJsFile(scope,realScriptPath);
 		
 		
 		
 		this.currentDir = originalDir ;
 		this.scriptName = originalScriptName;
 		
 	}
 	/**
 	* loads and evaluates the supplied ejs or sjs file in the current context, 
 	* not already included before. Usually for function/object libraries.
 	*
 	* @param  scriptPath sjs or ejs file to include 
 	*/
 	public void includeOnce(String scriptPath) throws Exception{
 		includeOnce(scriptPath,this.threadScope);
 		
 	}
 	
 	public void includeOnce(String scriptPath, Scriptable scope) throws Exception{
 		String realPath = getNormalizedPath(scriptPath); 
 		
 		if (uniqueIncludes_.add(realPath)){
 			include(realPath,scope);	
 		}
 	}
 	
 	/**
 	* if the filename appears to be an absolute path, it is simply returned, otherwise it is 
 	* assumed to be path relative to the directory of the calling script.    
 	*
 	* @param  url  an absolute URL giving the base location of the image
 	* @return      full filessystem path to the requested file
 	*/
 	private String expandPath(String fileName){
 		
 		if (new File(fileName).isAbsolute() ){ //if this is not an absolute path
 			return new String(fileName);
 		} else{
 			return  new String(this.currentDir + fileName);
 		}
 	}
 	
 	public File getNormalizedFile(String path) throws Exception{
 		return new File(new URI(getNormalizedPath(path)));
 	}
 	
 	public String getNormalizedPath(String path) throws Exception{
 		//try to URLencode any weird parts
 		if (path.indexOf("%") == -1){
 			String[] parts =path.split("/");
 			StringBuffer uriString= new StringBuffer();
 			int x=0;
 			path = path.replaceAll(":","___MYNA_COLON___");
 			path = path.replaceAll("/","___MYNA_SLASH___");
 			path = path.replaceAll(" ","___MYNA_SPACE___");
 			path = java.net.URLEncoder.encode(path);
 			path = path.replaceAll("___MYNA_SPACE___","%20");
 			path = path.replaceAll("___MYNA_COLON___",":");
 			path = path.replaceAll("___MYNA_SLASH___","/");
 		}
 		URI uri = new URI(path);
 		
 		URI	rootUri = new URI(this.rootDir);
 		if (this.currentDir == null) this.currentDir = this.rootDir;
 		URI	currentUri = new URI(this.currentDir);
 		
 		if (!uri.isAbsolute() ){ //if this is  an absolute path, i.e. starts with "file://"
 			if (path.charAt(0) == '/'){ //paths that start with slash mean the rootDir
 				uri =  rootUri.resolve(path.substring(1)).normalize();
 			} else { //all other paths are  relative to the current directory 
 				uri = currentUri.resolve(uri).normalize();
 			}
 		}
 		
 		return uri.toString();
 	}
 	
 	public FIFOSemaphore getLock(String name) throws Exception{
 		manageLocksPermit.acquire();
 		try{
 			FIFOSemaphore lock = (FIFOSemaphore) locks.get(name);
 			if (lock == null) locks.put(name, new FIFOSemaphore(1));
 			lock.acquire();
 			return lock;
 		} finally{
 			manageLocksPermit.release();
 		}
 		
 	}
 	
 	/** 
 	* A pre-processor to convert embedded JavaScript (.ejs) and <ejs> blocks into Serverside JavaScript (.sjs) 
 	*
 	* @param  content  String content to translate
 	* @param  scriptPath URI where content was retrieved. used in errors, and for determining content type 
 	* @return  translated content
 	*/
 	public String translateString(String content, String scriptPath) throws Exception{
 		MynaEjsParser parser = new MynaEjsParser();
 		String translated = parser.parseString(content,scriptPath);
 		//log("TRANSLATED",scriptPath,translated);
 		//java.lang.System.out.println(scriptPath);
 		
 		//java.lang.System.out.println(translated);
 		return translated;
 	}
 		/**
 	* A pre-processor to convert embedded JavaScript (.ejs) into Serverside JavaScript (.sjs) 
 	*
 	* @param  content emebeded JavaScript content to translate
 	* @return  translated content
 	*/
 	public String parseEmbeddedJsBlocks(String content) throws Exception{
 		StringBuffer script = new StringBuffer();
 	
 		int length = content.length();
 		
 		int x=0;
 		int startTag;
 		int endTag;
 		int emergencyExit=0;
 		while (x < length)
 		{
 			if (++emergencyExit > 100000) throw new Exception("Infinite loop.");
 			
 			startTag = content.indexOf("<ejs>",x);
 			if (startTag < 0){
 				script.append(content.substring(x));
 				break;
 			}
 			script.append(content.substring(x,startTag));
 			startTag+=5;
 			endTag = content.indexOf("</ejs>",startTag);
 			if (endTag > -1) {
 				script.append("function(){ var originalContent= $res.clear();");
 				script.append(parseEmbeddedJs(content.substring(startTag,endTag)) + "");
 				script.append("var newContent= $res.clear();");
 				script.append("$res.print(originalContent);");
 				script.append("return newContent;}.apply(this)");
 				x= endTag+6;
 				continue;
 			} else {
 				throw new Exception("Missing end </ejs>.");	
 			}
 		}	
 		return script.toString();
 	}
 	
 	/**
 	* A pre-processor to convert embedded JavaScript (.ejs) into Serverside JavaScript (.sjs) 
 	*
 	* @param  content emebeded JavaScript content to translate
 	* @return  translated content
 	*/
 	public String parseEmbeddedJs(String content) throws Exception{
 		StringBuffer textBuffer = new StringBuffer();
 		StringBuffer jsBuffer = new StringBuffer();
 		StringBuffer script = new StringBuffer();
 	
 		int length = content.length();
 		
 		int x=0;
 		int i=0;
 		int nextTag ;
 		int emergencyExit=0;
 		while (x < length)
 		{
 			if (++emergencyExit > 100000) throw new Exception("Infinite loop.");
 			if (x+3 < length && content.substring(x,x+4).equals("<%--") ){ //comment begin
 				//search for end comment
 				int nextIndex = content.indexOf("--%>",x+4);
 				if (nextIndex > -1) {
 					String [] lines  = content.substring(x+4,nextIndex).split("\n");
 					for (i =0;i<lines.length;++i){
 						script.append("/*" + lines[i] +"*/");
 						if (i < lines.length -1) script.append("\n");
 					}
 					x = nextIndex+4;
 					continue;
 				} else {
 					throw new Exception("Missing end comment.");	
 				}
 			} else if (x+2 < length && content.substring(x,x+3).equals("<%=") ){ //evaluate tag begin
 				//search for end of tag
 				int nextIndex = content.indexOf("%>",x+3);
 				if (nextIndex > -1) {
 					script.append("$res.print(String(" + content.substring(x+3,nextIndex) + "));");
 					x = nextIndex+2;
 					continue;
 				} else {
 					throw new Exception("Missing end %>.");	
 				}
 			} else if (x+1 < length && content.substring(x,x+2).equals("<%") ){
 				//search for end of tag
 				int nextIndex = content.indexOf("%>",x+2);
 				if (nextIndex > -1) {
 					script.append(content.substring(x+2,nextIndex) + "\n");
 					x = nextIndex+2;
 					//if (content.substring(x,x+1).equals("\n")) ++x;
 					continue;
 				} else {
 					throw new Exception("Missing end %>.");	
 				}
 			} else if ((nextTag = content.indexOf("<%",x)) >= x ){
 				String [] lines  = content.substring(x,nextTag).split("\n");
 				for (i =0;i<lines.length;++i){
 					script.append("$res.print('" + JSEscape(lines[i]));
 					
 					if (i < lines.length -1) {
 						script.append( "\\n');\n");
 
 					} else {
 						script.append( "');");
 					}
 	
 				}
 				x= nextTag;
 				continue;
 			} else {
 				String [] lines  = content.substring(x,length).split("\n");
 				for (i =0;i<lines.length;++i){
 					script.append("$res.print('" + JSEscape(lines[i]));
 					
 					if (i < lines.length -1) {
 						script.append( "\\n');\n");
 
 					} else {
 						script.append( "');");
 					}
 	
 				}
 				//script.append("$res.print('" + JSEscape(content.substring(x,length)) + "\\n');\n");
 				break;
 			}
 		}
 		return script.toString();
 	}
 	
 	public Object spawn(String func, Object[] args) throws Exception{
 		Runner runner;
 		runner = new Runner(func, args);
 		
 		runner.parentThread = this;
 		String f = (String) this.environment.get("threadFunctionSource");
 		/* if ( this.threadChain.contains(func)){
 			log("ERROR","A thread cannot call itself","Thread Function:<br><textarea rows=10 cols=70>" +f + "</textarea>");
 			System.err.println("A thread cannot call itself: \n" + f);
 			return null;
 		} */
 		if (this.threadChain.size() > 5){
 			log("ERROR","thread chains cannot descend more than 5 levels.","Thread Chain:<br> " +this.threadChain);
 			throw new Exception("thread chains cannot descend more than 5 levels.");
 		} //else {System.err.println("\n\nthis.threadChain="+this.threadChain);}
 		
 		Thread thread = new Thread(runner);
 		thread.start();
 		return thread;
   }
 	
 	class Runner implements Runnable {
 			Runner( String func, Object[] args) {
 					f = func;
 					this.args = args;
 			}
 			public void run()
 			{
 				try {
 					currentThread = new MynaThread();
 					currentThread.environment.put("threadFunctionSource", f);
 					currentThread.threadChain = new Vector(parentThread.threadChain);
 					currentThread.threadChain.add(f);
 					//java.lang.System.out.println("adding: "+ f.hashCode());
 					currentThread.callFunction(parentThread,f,args);
 					
 					String threadId = java.lang.Thread.currentThread().getName();
 					//java.lang.System.out.println("run: "+ threadId);
 					parentThread.environment.put("subthread_" + threadId, currentThread);
 					
 					
 				} catch (Exception e){
 					try {
 						handleError(e);
 					} catch (Exception e2){
 						java.lang.System.out.println(e2);	
 					}
 				}
 				
 			}
 	
 			private String f;
 			private MynaThread parentThread;
 			private Object[] args;
 			public MynaThread currentThread;
 	}
 		
 }
