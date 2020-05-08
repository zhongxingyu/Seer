 package org.remus;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.mpstore.AttachStore;
 import org.mpstore.KeyValuePair;
 import org.mpstore.MPStore;
 import org.mpstore.Serializer;
 import org.remus.manage.WorkStatus;
 import org.remus.serverNodes.AttachListView;
 import org.remus.serverNodes.BaseNode;
 import org.remus.serverNodes.PipelineAgentView;
 import org.remus.serverNodes.PipelineErrorView;
 import org.remus.serverNodes.PipelineInstanceListViewer;
 import org.remus.serverNodes.PipelineInstanceView;
 import org.remus.serverNodes.PipelineListView;
 import org.remus.serverNodes.PipelineStatusView;
 import org.remus.serverNodes.ResetInstanceView;
 import org.remus.serverNodes.SubmitView;
 import org.remus.work.RemusApplet;
 import org.remus.work.Submission;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RemusPipeline implements BaseNode {
 
 	public static final String CODE_FIELD = "_code";
 
 	HashMap<String,BaseNode> children;
 
 	private HashMap<String,RemusApplet> members;
 	Map<String, RemusApplet> inputs;
 	String id;
 	MPStore datastore;
 	AttachStore attachStore;
 	RemusApp app;
 
 	private Logger logger;
 	
 	public RemusPipeline(RemusApp app, String id, MPStore datastore, AttachStore attachStore) {
 		
 	    logger = LoggerFactory.getLogger(RemusPipeline.class);
 
 	    
 		this.app = app;
 		members = new HashMap<String,RemusApplet>();
 		children = new HashMap<String, BaseNode>();
 		children.put("@pipeline", new PipelineListView(this) );
 		children.put("@submit", new SubmitView(this) );
 		children.put("@status", new PipelineStatusView(this) );
 		children.put("@instance", new PipelineInstanceListViewer(this) );
 		children.put("@agent", new PipelineAgentView(this) );
 
 		children.put("@error", new PipelineErrorView(this) );
 
 		children.put("@reset", new ResetInstanceView(this) );
 
 		inputs = new HashMap<String, RemusApplet >();
 		this.id = id;
 		this.datastore = datastore;
 		this.attachStore = attachStore;
 		children.put("@attach", new AttachListView(this.attachStore, "/" + id , RemusInstance.STATIC_INSTANCE_STR, null ) );
 	}
 
 	public RemusApp getApp() {
 		return app;
 	}
 
 	public void addApplet(RemusApplet applet) {		
 		applet.setPipeline( this );
 		inputs = null; //invalidate input list
 		members.put(applet.getID(), applet);
 		//children.put(applet.getID(), applet);
 	}
 
 	public Set<WorkStatus> getWorkQueue( ) {
 		if ( inputs == null ) {
 			setupInputs();
 		}
 		Set<WorkStatus> out = new HashSet<WorkStatus>();
 		for ( RemusApplet applet : members.values() ) {
 			out.addAll( applet.getWorkList() );
 		}
 		return out;
 	}
 
 	private void setupInputs() {
 		inputs = new HashMap<String, RemusApplet>();
 		for ( RemusApplet applet : members.values() ) {
 			if ( applet.hasInputs() ) {
 				for ( String iref : applet.getInputs() ) {
 					if ( iref.compareTo("?") != 0 ) {
 						inputs.put(iref, applet);
 					}
 				}
 			}
 		}
 	}
 
 	public Collection<RemusApplet> getMembers() {
 		return members.values();
 	}
 
 	public RemusApplet getApplet(String path) {
 		return members.get(path);
 	}
 
 	public Set<String> getInputs() {
 		if ( inputs == null ) {
 			setupInputs();
 		}
 		return inputs.keySet();
 	}
 	/*
 	public RemusApplet getInputApplet( RemusPath ref ) {
 		if ( inputs == null ) {
 			setupInputs();
 		}
 		return inputs.get(ref);
 	}	
 	 */
 
 	public RemusInstance getInstance(String name) {
 		for ( Object subObject : getDataStore().get( "/" + getID() + "/@submit", RemusInstance.STATIC_INSTANCE_STR, name) ) {
 			Map subMap = (Map) subObject;
 			return new RemusInstance( (String)subMap.get( Submission.InstanceField ) );
 		}
 		for ( Object instObject : getDataStore().get( "/" + getID() + "/@instance", RemusInstance.STATIC_INSTANCE_STR, name) ) {			
 			return  new RemusInstance( name );				
 		}
 		return null;
 	}
 
 	public String getSubKey(RemusInstance inst) {
 		for ( Object instObject : getDataStore().get( "/" + getID() + "/@instance", RemusInstance.STATIC_INSTANCE_STR, inst.toString() ) ) {			
 			return (String)instObject;
 		}
 		return null;
 	}
 
 	public Map getSubmitData(String subKey) {
 		for ( Object subObject : getDataStore().get( "/" + getID() + "/@submit", RemusInstance.STATIC_INSTANCE_STR, subKey) ) {
 			Map subMap = (Map) subObject;
 			return subMap;
 		}
 		return null;
 	}	
 
 	public void deleteInstance(RemusInstance instance) {
 		logger.info( "Deleting Instance " + instance );
 		for ( RemusApplet applet : members.values() ) {
 			applet.deleteInstance(instance);
 		}
 		datastore.delete("/" + getID() + "/@instance", RemusInstance.STATIC_INSTANCE_STR, instance.toString());
 	}
 
 	public boolean isComplete(RemusInstance inst) {
 		boolean done = true;
 		for ( RemusApplet applet : members.values() ) {
 			if ( ! WorkStatus.isComplete(applet, inst) )
 				done = false;
 		}
 		return done;
 	}
 
 	public int appletCount() {
 		return members.size();
 	}
 
 	public MPStore getDataStore() {
 		return datastore;
 	}
 
 	public boolean hasApplet(String appletPath) {
 		return members.containsKey( appletPath ) ;
 	}
 
 	public String getID() {
 		return id;
 	}
 
 	public AttachStore getAttachStore() {
 		return attachStore;
 	}
 
 
 
 	public Iterable<KeyValuePair> getSubmits() {
 		return datastore.listKeyPairs( "/" + getID() + "/@submit", 
 				RemusInstance.STATIC_INSTANCE_STR );
 	}
 
 	@Override
 	public void doDelete(String name, Map params, String workerID) throws FileNotFoundException {
 		//Deletions should be done through one of the sub-views, or in a parent view
 		throw new FileNotFoundException();
 	}
 
 	@Override
 	public void doGet(String name, Map params, String workerID, Serializer serial, OutputStream os)
 			throws FileNotFoundException {
 		for ( KeyValuePair kv : datastore.listKeyPairs( "/" + getID() + "/@submit", RemusInstance.STATIC_INSTANCE_STR ) ) {
 			Map out = new HashMap();
 			out.put(kv.getKey(), kv.getValue() );
 			try {
 				os.write( serial.dumps(out).getBytes() );
 				os.write("\n".getBytes());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void doPut(String name, String workerID, Serializer serial, InputStream is, OutputStream os) throws FileNotFoundException {
 		System.err.println( "PUTTING:" + name );
 	}
 
 	@Override
 	public void doSubmit(String name, String workerID, Serializer serial,
 			InputStream is, OutputStream os) throws FileNotFoundException {
 		// TODO Auto-generated method stub
 
 	}
 
 	class PipelineAttachment implements BaseNode {
 
 		String fileName;
 		PipelineAttachment(String fileName) {
 			this.fileName = fileName;
 		}
 
 		@Override
 		public void doDelete(String name, Map params, String workerID) throws FileNotFoundException { }
 
 		@Override
 		public void doGet(String name, Map params, String workerID,
 				Serializer serial, OutputStream os)
 						throws FileNotFoundException {
 			InputStream fis = attachStore.readAttachement("/" + getID(), RemusInstance.STATIC_INSTANCE_STR, null, fileName);
 			if ( fis != null ) {
 				byte [] buffer = new byte[1024];
 				int len;
 				try {
 					while ( (len = fis.read(buffer)) >= 0 ) {
 						os.write( buffer, 0, len );
 					}
 					os.close();
 					fis.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}			
 			} else {
 				throw new FileNotFoundException();
 			}			
 		}
 
 		@Override
 		public void doPut(String name, String workerID, Serializer serial,
 				InputStream is, OutputStream os) throws FileNotFoundException {}
 
 		@Override
 		public void doSubmit(String name, String workerID, Serializer serial,
 				InputStream is, OutputStream os) throws FileNotFoundException {}
 
 		@Override
 		public BaseNode getChild(String name) {
 			return null;
 		}
 
 	}
 
 
 	@Override
 	public BaseNode getChild(String name) {
 		if ( children.containsKey(name) )
 			return children.get(name);
 
 		for ( Object subObject : datastore.get( "/" + getID() + "/@submit", RemusInstance.STATIC_INSTANCE_STR, name) ) {
 			RemusInstance inst = new RemusInstance( (String)((Map)subObject).get( Submission.InstanceField ) );
 			return new PipelineInstanceView( this, inst  );
 		}
 
 
 		for ( Object subObject : datastore.get( "/" + getID() + "/@instance", RemusInstance.STATIC_INSTANCE_STR, name) ) {
 			RemusInstance inst = new RemusInstance( name );
 			return new PipelineInstanceView( this, inst  );
 		}
 
 		if ( attachStore.hasAttachment( "/" + getID(), RemusInstance.STATIC_INSTANCE_STR, null, name ) ) {
 			return new PipelineAttachment( name );
 		}
 
 		return null;
 	}
 
 
 	public RemusInstance handleSubmission(String key, Map value) {
 
 		RemusInstance inst;
 
 		if ( ((Map)value).containsKey( Submission.AppletField ) ) {
 			List<String> aList = (List)((Map)value).get(Submission.AppletField);
 			inst = setupInstance( key, (Map)value, aList );					
 		} else {
 			inst = setupInstance( key, (Map)value, new LinkedList() );	
 		}					
 
 		//only add the main submission/instance records if they don't already exist
 		//we've already fired off the setupInstance requests to the applets, so if new applets are
 		//to be instanced in an exisiting pipeline instance, they will be, but the original submisison 
 		//will remain
 		if ( ! getDataStore().containsKey( "/" + getID() + "/@submit",
 				RemusInstance.STATIC_INSTANCE_STR, key) ) {
 			((Map)value).put(Submission.SubmitKeyField, key );	
 
 			((Map)value).put(Submission.InstanceField, inst.toString());	
 			getDataStore().add( "/" + getID() + "/@submit", 
 					RemusInstance.STATIC_INSTANCE_STR, 
 					(Long)0L, 
 					(Long)0L, 
 					key,
 					value );
 		}
 		getDataStore().add( "/" + getID() + "/@instance", 
 				RemusInstance.STATIC_INSTANCE_STR, 
 				0L, 0L,
 				inst.toString(),
 				key);			
 		return inst;
 	}
 
 
 	public RemusInstance setupInstance(String name, Map params, List<String> appletList) {
 		logger.info("Init submission " + name );
 		Set<RemusApplet> activeSet = new HashSet<RemusApplet>();
 		RemusInstance inst = new RemusInstance();
 
 		for ( Object subObject : datastore.get( "/" + getID() + "/@submit", RemusInstance.STATIC_INSTANCE_STR, name) ) {
 			inst = new RemusInstance( (String)((Map)subObject).get( Submission.InstanceField ) );
 		}
 
 		for (String sObj : appletList) {
 			RemusApplet applet = getApplet((String)sObj);
 			if ( applet != null ) {
 				activeSet.add(applet);
 				applet.createInstance(name, params, inst);
 			}
 		}
 
 		boolean added = false;
 		do {
 			added = false;
 			for ( RemusApplet applet : getMembers() ) {
 				if ( !activeSet.contains(applet) ) {
 					if (applet.getMode() == RemusApplet.STORE) {
 						if (applet.createInstance(name, params, inst)) {
 							added = true;
 						}
 						activeSet.add(applet);
 					} else {
 						for ( String iRef : applet.getInputs() ) {
 							if ( iRef.compareTo("?") != 0 ) {
 								RemusApplet srcApplet = getApplet( iRef );
 								if (activeSet.contains(srcApplet) ) {
 									if (applet.createInstance(name, params, inst)) {
 										added = true;
 									}
 									activeSet.add(applet);
 								}
 							}
 						}
 					}
 				} 
 			}
 		} while (added);
 		
 		app.getWorkManager().jobScan();
 		app.getWorkManager().workPoll();
 		logger.info("submission " + name + " started as " + inst);
 		return inst;		
 	}
 
 
 
 }
