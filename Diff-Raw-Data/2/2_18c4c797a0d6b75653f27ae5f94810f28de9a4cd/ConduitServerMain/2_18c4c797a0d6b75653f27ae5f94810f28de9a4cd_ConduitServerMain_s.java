 package com.cj.scmconduit.server;
 
 import static org.httpobjects.jackson.JacksonDSL.JacksonJson;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.Appender;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.RollingFileAppender;
 import org.httpobjects.HttpObject;
 import org.httpobjects.Request;
 import org.httpobjects.Response;
 import org.httpobjects.jetty.HttpObjectsJettyHandler;
 import org.httpobjects.util.ClasspathResourceObject;
 import org.httpobjects.util.HttpObjectUtil;
 import org.httpobjects.util.Method;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.ResourceHandler;
 
 import scala.Function1;
 import scala.Tuple2;
 import scala.runtime.AbstractFunction1;
 import scala.runtime.BoxedUnit;
 
 import com.cj.scmconduit.core.BzrP4Conduit;
 import com.cj.scmconduit.core.Conduit;
 import com.cj.scmconduit.core.GitP4Conduit;
 import com.cj.scmconduit.core.p4.ClientSpec;
 import com.cj.scmconduit.core.p4.P4Credentials;
 import com.cj.scmconduit.core.p4.P4DepotAddress;
 import com.cj.scmconduit.core.util.CommandRunner;
 import com.cj.scmconduit.core.util.CommandRunnerImpl;
 import com.cj.scmconduit.server.api.ConduitInfoDto;
 import com.cj.scmconduit.server.api.ConduitType;
 import com.cj.scmconduit.server.api.ConduitsDto;
 import com.cj.scmconduit.server.conduit.ConduitController;
 import com.cj.scmconduit.server.conduit.ConduitState;
 import com.cj.scmconduit.server.config.Config;
 import com.cj.scmconduit.server.fs.TempDirAllocator;
 import com.cj.scmconduit.server.fs.TempDirAllocatorImpl;
 import com.cj.scmconduit.server.jetty.ConduitHandler;
 import com.cj.scmconduit.server.jetty.VFSResource;
 import com.cj.scmconduit.server.util.SelfResettingFileOutputStream;
 
 
 public class ConduitServerMain {
 	public static void main(String[] args) throws Exception {
 		try {
 			setupLogging();
 			doBzrSafetyCheck();
 			
 			Config config = Config.fromArgs(args);
 			
 			new ConduitServerMain(config);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 
 	private static void setupLogging() {
 //		try {
 			BasicConfigurator.resetConfiguration();
 			BasicConfigurator.configure();
 //			RollingFileAppender a = new RollingFileAppender(
 //										new PatternLayout("%p %t %c - %m%n"), 
 //										"scm-conduit.log");
 //			Logger.getRootLogger().addAppender(a);
 			Logger.getRootLogger().setLevel(Level.INFO);
 //		} catch (IOException e) {
 //			throw new RuntimeException(e);
 //		}
 	}
 
 	private static void doBzrSafetyCheck() {
 		File f = new File(System.getProperty("user.dir"));
 		while(f!=null){
 			File dotBzrDir = new File(f, ".bzr"); 
 			if(dotBzrDir.exists()){
 				throw new RuntimeException("Looks like we're being run from under bzr (" + f.getAbsolutePath() + ").  This can be dangerous.  Run me from somewhere else instead.");
 			}
 			f = f.getParentFile();
 		}
 	}
 	
 	private final File tempDirPath;
 	private final File path;
 	private final Server jetty;
 	private final Map<String, String> credentials = new HashMap<String, String>();
 	{
 		credentials.put("someuser", "test");
 	}
 	private final String basePublicUrl;
 	private final P4DepotAddress p4Address;
 	private final TempDirAllocator allocator;
 	private final List<ConduitStuff> conduits = new ArrayList<ConduitServerMain.ConduitStuff>();
 	private final List<ConduitCreationThread> creationThreads = new ArrayList<ConduitServerMain.ConduitCreationThread>();
 	private final Log log;
 	private final Config config;
 	
 	public ConduitServerMain(final Config config) throws Exception {
 		this.log = LogFactory.getLog(getClass());
 		this.config = config;
 		this.path = config.path;
 		this.tempDirPath = new File(this.path, "tmp");
 		if(!this.tempDirPath.exists() || !this.tempDirPath.isDirectory()){
 			throw new IOException("Directory does not exist: " + this.tempDirPath);
 		}
 		
		basePublicUrl = "http://" + config.publicHostname + ":8034";
 		log.info("My public url is " + basePublicUrl);
 		
 		p4Address = new P4DepotAddress(config.p4Address);
 		
 		jetty = new Server(config.port);
 		ResourceHandler defaultHandler = new ResourceHandler();
 		final VFSResource root = new VFSResource("/");
 		defaultHandler.setBaseResource(root);
 		defaultHandler.setWelcomeFiles(new String[]{"hi.txt"});
 		final List<Handler> handlers = new ArrayList<Handler>();
 		handlers.add(defaultHandler);
 		
 		allocator = new TempDirAllocatorImpl(tempDirPath);
 		
 		for(ConduitConfig conduit: findConduits()){
 			if(conduit.localPath.listFiles().length==0){
 				FileUtils.deleteDirectory(conduit.localPath);
 			}else{
 				ConduitStuff stuff = prepareConduit(basePublicUrl, root, allocator, conduit);
 				conduits.add(stuff);
 			}
 		}
 		
 		// Add shared bzr repository
 		root.addVResource("/.bzr", new File(config.path, ".bzr"));
 		
 		
 		HttpObject addConduitPage = new AddConduitResource(new AddConduitResource.Listener() {
 			@Override
 			public void addConduit(final ConduitType type, final String name, final String p4Path, final Integer p4FirstCL) {
 				ConduitCreationThread thread = new ConduitCreationThread(config, type, name, p4Path, p4FirstCL, root);
 				creationThreads.add(thread);
 				thread.start();
 			}
 		});
 		
 		HttpObject depotHandler = new HttpObject("/{conduitName}/{remainder*}", null) {
 			
 			public ConduitStuff findConduitForPath(String conduitName) {
 				ConduitStuff foundConduit = null;
 				for (ConduitStuff conduit : conduits) {
 					final String next = conduit.handler.name;
 					log.info("name: " + next);
 					if (conduitName.equals(next)) {
 						foundConduit = conduit;
 					}
 				}
 				return foundConduit;
 			}
 			
 			public Response relay(Request req, final Method m) {
 				String conduitName = "/" + req.pathVars().valueFor("conduitName");
 				
 				ConduitStuff match = findConduitForPath(conduitName);
 				if(match!=null) return HttpObjectUtil.invokeMethod(match.handler, m, req);
 				else {
 					log.debug("There is no conduit at " + conduitName);
 					return null;
 				}
 				
 			}
 			
 			@Override
 			public Response get(Request req) {
 				return relay(req, Method.GET);
 			}
 			
 			@Override
 			public Response post(Request req) {
 				return relay(req, Method.POST);
 			}
 		};
 		
 		final HttpObject conduitLogResource = new HttpObject("/api/conduits/{conduitName}/log"){
 			@Override
 			public Response get(Request req) {
 				final String conduitName = req.pathVars().valueFor("conduitName");
 				final ConduitStuff conduit = conduitNamed(conduitName);
 				if(conduit==null){
 					return NOT_FOUND();
 				}else{
 					try {
 						final FileInputStream in = new FileInputStream(conduit.log.location);
 						return OK(Bytes("text/plain", in));
 					} catch (FileNotFoundException e) {
 						return INTERNAL_SERVER_ERROR(e);
 					}
 				}
 			}
 		};
 		
 		
 		final HttpObject conduitApiResource = new HttpObject("/api/conduits/{conduitName}"){
 			
 			
 			
 			@Override
 			public Response get(Request req) {
 				final String conduitName = req.pathVars().valueFor("conduitName");
 				final ConduitStuff conduit = conduitNamed(conduitName);
 				if(conduit==null){
 					return NOT_FOUND();
 				}else{
 					return OK(JacksonJson(conduit.toDto()));
 				}
 			}
 			
 			@Override
 			public Response delete(Request req) {
 				final String conduitName = req.pathVars().valueFor("conduitName");
 				final ConduitStuff conduit = conduitNamed(conduitName);
 				if(conduit==null){
 					return NOT_FOUND();
 				}else{
 					try {
 						log.info("Deleting " + conduitName);
 						conduits.remove(conduit);
 						conduit.controller.stop();
 						conduit.controller.delete();
 						return OK(Text("deleted"));
 					} catch (Exception e) {
 						return INTERNAL_SERVER_ERROR(e);
 					}
 					
 				}
 			}
 			
 		};
 		
 		final HttpObject conduitsApiResource = new HttpObject("/api/conduits"){
 			@Override
 			public Response get(Request req) {
 				ConduitsDto conduitsDto = new ConduitsDto();
 				
 				for(ConduitStuff conduit : conduits){
 					conduitsDto.conduits.add(conduit.toDto());
 				}
 				
 				for(ConduitCreationThread next : creationThreads){
 					ConduitInfoDto dto = new ConduitInfoDto();
 					dto.name = next.name;
 					if(next.theConduit==null){
 						dto.status = ConduitState.STARTING;
 					}else{
 						dto.status = ConduitState.BUILDING;
 						dto.backlogSize = next.theConduit.backlogSize();
 						dto.currentP4Changelist = next.theConduit.currentP4Changelist();
 					}
 					
 					conduitsDto.conduits.add(dto);
 				}
 				
 				return OK(JacksonJson(conduitsDto));
 			}
 		};
 		
 		handlers.add(new HttpObjectsJettyHandler(
 							new ClasspathResourceObject("/", "index.html", getClass()), 
 							new ClasspathResourceObject("/submit.py", "submit.py", getClass()), 
 							addConduitPage, 
 							depotHandler, 
 							conduitsApiResource,
 							conduitApiResource,
 							conduitLogResource
 							));
 		
 		jetty.setHandlers(handlers.toArray(new Handler[]{}));
 		jetty.start();
 		
 	}
 	
 	private ConduitStuff conduitNamed(String name){
 		for(ConduitStuff next : conduits){
 			if(next.isNamed(name)){
 				return next;
 			}
 		}
 		
 		return null;
 	}
 	
 	static class ConduitLog {
 		final File location;
 		final PrintStream stream;
 		
 		public ConduitLog(File location, PrintStream stream) {
 			super();
 			this.location = location;
 			this.stream = stream;
 		}
 	}
 	
 	private ConduitLog logStreamForConduit(String name){
 		try {
 			final File f = new File(this.config.path, name + ".log");
 			log.info("Conduit log is at " + f.getAbsolutePath());
 			if(!f.exists() && !f.createNewFile())
 				throw new RuntimeException("Could not create file at " + f.getAbsolutePath());
 			
 			final Long maxLogSizeInBytes = Long.valueOf(1024*1024 *5);
 			final boolean autoFlush = true;
 			final PrintStream out = new PrintStream(
 										new SelfResettingFileOutputStream(f, maxLogSizeInBytes),
 										autoFlush);
 			
 			return new ConduitLog(f, out);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	class ConduitCreationThread extends Thread {
 		final Config config;
 		final ConduitType type;
 		final String name;
 		final String p4Path;
 		final Integer p4FirstCL;
 		final VFSResource root;
 		Conduit theConduit;
 		
 		private ConduitCreationThread(Config config, ConduitType type,
 				String name, String p4Path, Integer p4FirstCL, VFSResource root) {
 			super();
 			this.config = config;
 			this.type = type;
 			this.name = name;
 			this.p4Path = p4Path;
 			this.p4FirstCL = p4FirstCL;
 			this.root = root;
 		}
 
 		public void run() {
 			PrintStream logStream = null;
 			try{
 				final ConduitLog conduitLog = logStreamForConduit(name);
 				logStream = conduitLog.stream;
 				final CommandRunner shell = new CommandRunnerImpl(logStream, logStream);
 				File localPath = new File(config.basePathForNewConduits, name);
 				log.info("I am going to create something at " + localPath);
 				if(localPath.exists()) throw new RuntimeException("There is already a conduit called \"" + name + "\" " +
 						"(given that there is already a directory at " + localPath.getAbsolutePath() + ")");
 				
 				String clientId = config.clientIdPrefix + name;
 				
 				@SuppressWarnings("deprecation")
 				scala.collection.immutable.List<Tuple2<String, String>> view = scala.collection.immutable.List.fromArray(new Tuple2[]{
 						new Tuple2<String, String>(p4Path + "/...", "/...")
 				});
 				
 				ClientSpec spec = new ClientSpec(
 						localPath, 
 						config.p4User, 
 						clientId, 
 						config.publicHostname, 
 						view);
 				
 				P4Credentials credentials = new P4Credentials(config.p4User, "");
 				
 				Function1<Conduit, BoxedUnit> observerFunction = new AbstractFunction1<Conduit, BoxedUnit>(){
 					@Override
 					public BoxedUnit apply(Conduit arg0) {
 						theConduit = arg0;
 						return BoxedUnit.UNIT;
 					}
 				};
 				
 				if(type == ConduitType.GIT){
 					GitP4Conduit.create(p4Address, spec, p4FirstCL, shell, credentials, logStream, observerFunction);
 				}else if(type == ConduitType.BZR){
 					BzrP4Conduit.create(p4Address, spec, p4FirstCL, shell, credentials, logStream, observerFunction);
 				}else{
 					throw new RuntimeException("not sure how to create a \"" + type + "\" conduit");
 				}
 				
 				ConduitConfig conduit = new ConduitConfig("/" + name, localPath);
 				ConduitStuff stuff = prepareConduit(basePublicUrl, root, allocator, conduit);
 				conduits.add(stuff);
 			} catch (Exception e){
 				e.printStackTrace(logStream==null?System.out:logStream);
 				log.error(e);
 				if(theConduit!=null){
 					theConduit.delete();
 				}
 			} finally{
 				creationThreads.remove(this);
 			}
 		}
 	}
 	
 	interface MethodInvoker {
 		Response invoke(Request req, HttpObject o);
 	}
 	
 	class ConduitConfig {
 		
 		public final String hostingPath;
 		
 		public final File localPath;
 		
 		private ConduitConfig(String hostingPath, File localPath) {
 			super();
 			this.hostingPath = hostingPath;
 			this.localPath = localPath;
 		}
 		
 	}
 
 	private List<ConduitConfig> findConduits(){
 		List<ConduitConfig> conduits = new ArrayList<ConduitConfig>();
 		
 		File conduitsDir = new File(path, "conduits");
 		
 		if(!conduitsDir.isDirectory() && !conduitsDir.mkdirs()) throw new RuntimeException("Could not create directory at " + conduitsDir);
 		
 		for(File localPath : conduitsDir.listFiles()){
 			if(localPath.isDirectory() && !localPath.getName().toLowerCase().endsWith(".bak")){
 				String httpPath = "/" + localPath.getName();//.replaceAll(Pattern.quote("-"), "/");
 				
 				conduits.add(new ConduitConfig(httpPath, localPath));
 			}
 		}
 		
 		return conduits;
 	}
 	
 	class ConduitStuff {
 		final String p4path;
 		final ConduitConfig config;
 		final ConduitHandler handler;
 		final ConduitController controller;
 		final ConduitLog log;
 		
 		private ConduitStuff(ConduitConfig config, ConduitHandler handler,
 				ConduitController controller, ConduitLog log) {
 			super();
 			this.config = config;
 			this.handler = handler;
 			this.controller = controller;
 			this.p4path = controller.p4Path();
 			this.log = log;
 		}
 		
 
 		public boolean isNamed(String name) {
 			return handler.name.replaceAll(Pattern.quote("/"), "").equals(name);
 		}
 
 		ConduitInfoDto toDto(){
 			final ConduitStuff conduit = this;
 			final ConduitInfoDto dto = new ConduitInfoDto();
 			dto.readOnlyUrl = basePublicUrl + conduit.config.hostingPath + (new File(conduit.config.localPath, ".git").exists()?"/.git":"");
 			dto.apiUrl = basePublicUrl + conduit.config.hostingPath;
 			dto.p4path = conduit.p4path;
 			dto.name = conduit.config.localPath.getName();
 			dto.queueLength = conduit.controller.queueLength();
 			dto.status = conduit.controller.state();
 			dto.backlogSize = conduit.controller.backlogSize();
 			dto.currentP4Changelist = conduit.controller.currentP4Changelist();
 			dto.error = conduit.controller.error();
 			dto.type = conduit.controller.type();
 			dto.logUrl = basePublicUrl + "/api/conduits" + conduit.config.hostingPath + "/log";
 			
 			return dto;
 		}
 	}
 	
 	private ConduitStuff prepareConduit(final String basePublicUrl, VFSResource root, TempDirAllocator allocator, ConduitConfig conduit) {
 		URI publicUri = URI(basePublicUrl + conduit.hostingPath);
 		ConduitLog log = logStreamForConduit(conduit.localPath.getName());
 		ConduitController controller = new ConduitController(log.stream, publicUri, conduit.localPath, allocator);
 		controller.start();
 		
 		ConduitHandler handler = new ConduitHandler(conduit.hostingPath, controller);
 		
 		// For basic read-only "GET" access
 		this.log.info("Serving " + conduit.localPath + " at " + conduit.hostingPath);
 		root.addVResource(conduit.hostingPath, conduit.localPath);
 		return new ConduitStuff(conduit, handler, controller, log);
 	}
 	
 	URI URI(String uri){
 		try {
 			return new URI(uri);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 }
