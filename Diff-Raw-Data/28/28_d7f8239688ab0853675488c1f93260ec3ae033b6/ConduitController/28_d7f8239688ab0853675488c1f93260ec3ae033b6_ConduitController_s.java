 package com.cj.scmconduit.server.conduit;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import com.cj.scmconduit.core.BzrP4Conduit;
 import com.cj.scmconduit.core.Conduit;
 import com.cj.scmconduit.core.GitP4Conduit;
 import com.cj.scmconduit.core.p4.P4Credentials;
 import com.cj.scmconduit.core.util.CommandRunner;
 import com.cj.scmconduit.core.util.CommandRunnerImpl;
 import com.cj.scmconduit.server.api.ConduitType;
 import com.cj.scmconduit.server.conduit.PushSession.PushStrategy;
 import com.cj.scmconduit.server.fs.TempDirAllocator;
 
 public class ConduitController implements Pusher {
 	private final PrintStream out;
 	private final URI publicUri;
 	private final File pathOnDisk;
 	private final List<PushRequest> requests = new LinkedList<PushRequest>();
 	private final Conduit conduit;
 	private final CommandRunner shell;
 	private final PushStrategy pushStrategy;
 	private final Map<Integer, PushSession> pushes = new HashMap<Integer, PushSession>();
 	private final TempDirAllocator temps;
 	private final ConduitType type;
 	
 	private ConduitState state = ConduitState.IDLE;
 	private String error;
 	
 	public ConduitController(PrintStream out, URI publicUri, File pathOnDisk, TempDirAllocator temps) {
 		super();
 		this.out = out;
 		this.shell = new CommandRunnerImpl(out, out);
 		this.publicUri = publicUri;
 		this.pathOnDisk = pathOnDisk;
 		this.temps = temps;
 		
 		if(new File(pathOnDisk, ".bzr").exists()){
 			type = ConduitType.BZR;
 			pushStrategy = new BzrPushStrategy();
 			conduit = new BzrP4Conduit(pathOnDisk, shell, out);
 		}else if(new File(pathOnDisk, ".git").exists()){
 			type = ConduitType.GIT;
 			pushStrategy = new GitPushStrategy();
 			conduit = new GitP4Conduit(pathOnDisk, shell, out);
 		}else{
 			throw new RuntimeException("Not sure what kind of conduit this is: " + pathOnDisk);
 		}
 	}
 	
 	public ConduitType type() {
 		return type;
 	}
 	
 	public String error(){
 		return error;
 	}
 	
 	public void delete(){
 		conduit.delete();
 	}
 	
 	public ConduitState state() {
 		return state;
 	}
 	
 	public int queueLength(){
 		return requests.size();
 	}
 	
 	public int backlogSize(){
 		return conduit.backlogSize();
 	}
 	
 	public Long currentP4Changelist(){
 		return conduit.currentP4Changelist();
 	}
 	
 	public String p4Path(){
 		return conduit.p4Path();
 	}
 	
 	public synchronized PushSession newSession(){
 		Integer id = findAvailableId();
 		out.println("id: " + id);
 		
 		PushSession session = new PushSession(id, publicUri, pathOnDisk, temps.newTempDir(), pushStrategy, shell);
 		pushes.put(session.id(), session);
 		return session;
 	}
 	
 	private Integer findAvailableId() {
 		Integer id = null;
 		while(id==null || pushes.get(id)!=null){
 			id = new Random().nextInt(65000-1000) + 1000;
 		}
 		return id;
 	}
 
 	private static class PushRequest {
 		final File location;
 		final PushListener listener;
 		final P4Credentials credentials;
 		
 		public PushRequest(File location, PushListener listener, P4Credentials credentials) {
 			super();
 			this.location = location;
 			this.listener = listener;
 			this.credentials = credentials;
 		}
 
 	}
 
 	public void submitPush(File location, final P4Credentials credentials, PushListener listener){
 		synchronized(requests){
 			requests.add(new PushRequest(location, listener, credentials));
 		}
 	}
 
 	private PushRequest popNextRequest(){
 		synchronized(requests){
 			if(requests.size()>0){
 				PushRequest r = requests.get(0);
 				requests.remove(0);
 				return r;
 			}else{
 				return null;
 			}
 		}
 	}
 	
 	private boolean keepRunning = true;
 	private Thread t;
 	
 	public void start(){
 		t = new Thread(){
 			public void run() {
 				while(keepRunning){
 					try {
 						state = ConduitState.POLLING;
 						pumpIn();
 						PushRequest request = popNextRequest();
 						if(request!=null){
 							state = ConduitState.SENDING;
 							out.println("Handling request: " + request);
 							handle(request);
 						}else{
 							out.println("Sleeping");
 							state = ConduitState.IDLE;
							Thread.sleep(5000);
 						}

 					} catch (Exception e) {
 						error = stackTrace(e);
 						out.println("ERROR IN CONDUIT " + pathOnDisk + "\n" + error);
 						e.printStackTrace(out);
						state = ConduitState.ERROR;
						break;
 					}
 				}
 			}
 		};
 		t.start();
 	}

 	private void handle(PushRequest request) {
 		try {
 			final P4Credentials credentials = request.credentials;
 			String source = request.location.getAbsolutePath();
 			out.println("Pulling from " + source);
 			boolean changesWerePulled = conduit.pull(source, credentials);
 			if(changesWerePulled){
 				out.println("Committing");
 				conduit.commit(credentials);
 				request.listener.pushSucceeded();
 			}else{
 				request.listener.nothingToPush();
 			}
 		} catch (Exception e) {
 			out.println("There was an error: " + e.getMessage());
 			e.printStackTrace(System.out);
 			
 			List<Throwable> errors = new ArrayList<Throwable>();
 			errors.add(e);
 			
 			try{
 				conduit.rollback();
 			}catch(Throwable e2){
 				e2.printStackTrace();
 				errors.add(e2);
 			}
 			
 			StringBuilder text = new StringBuilder("Error:");
 			for(Throwable t : errors){
 				text.append('\n');
 				text.append(stackTrace(t));
 			}
 			request.listener.pushFailed(text.toString());
 			return;
 		}
 	}
 
 	private String stackTrace(Throwable t){
 		try {
 			StringWriter s = new StringWriter();
 			PrintWriter w = new PrintWriter(s);
 			t.printStackTrace(w);
 			s.flush();
 			s.close();
 			return s.toString();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private void pumpIn() throws Exception{
 		out.println("Pumping conduit " + pathOnDisk.getAbsolutePath());
 		conduit.push();
 	}
 
 	public void stop() {
 		try {
 			keepRunning = false;
 			t.join();
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
