 package org.remus.applet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.mpstore.JsonSerializer;
 import org.mpstore.KeyValuePair;
 import org.mpstore.MPStore;
 import org.mpstore.Serializer;
 import org.remus.CodeFragment;
 import org.remus.RemusPath;
 import org.remus.RemusInstance;
 import org.remus.RemusPipeline;
 import org.remus.WorkDescription;
 import org.remus.WorkReference;
 
 
 public class RemusApplet {
 
 	public static RemusApplet newApplet( String path, CodeFragment code, int type ) {
 		RemusApplet out = new RemusApplet();
 
 		switch (type) {
 		case MAPPER: {
 			out.workGenerator = MapGenerator.class; //new MapperApplet();	
 			break;
 		}
 		case REDUCER: {
 			out.workGenerator = ReduceGenerator.class;	
 			break;
 		}
 		case SPLITTER: {
 			out.workGenerator = SplitGenerator.class;	
 			break;
 		}
 		case MERGER: {
 			out.workGenerator = MergeGenerator.class;	
 			break;
 		}
 		case MATCHER: {
 			out.workGenerator = MatchGenerator.class;	
 			break;
 		}
 		case PIPE: {
 			out.workGenerator = PipeGenerator.class;	
 			break;
 		}
 		}
 		if ( out != null ) {
 			out.code = code;
 			out.path = path;
 			out.type = type;
 			out.inputs = null;
 			out.activeInstances = new LinkedList<RemusInstance>();			
 		}
 		return out;
 	}
 
 	public static final int MAPPER = 1;
 	public static final int MERGER = 2;
 	public static final int MATCHER = 3;
 	public static final int SPLITTER = 4;
 	public static final int REDUCER = 5;
 	public static final int PIPE = 6;
 	public static final int STORE = 7;
 
 	public static final String INIT_OP = "init";
 	public static final String WORKGEN_OP = "workgen";
 	public static final String WORKDONE_OP = "workdone";	
 
 	public static final int INIT_OP_CODE = 0;
 	public static final int WORKGEN_OP_CODE = 1;
 	public static final int WORKDONE_OP_CODE = 2;
 
 	String codeType=null;
 	Class workGenerator = null;
 	String path;
 	List<RemusPath> inputs = null, lInputs = null, rInputs = null;
 	List<String> outputs = null;
 	CodeFragment code;
 	MPStore datastore;
 	int type;
 	protected RemusPipeline pipeline = null;
 	LinkedList<RemusInstance> activeInstances;
 
 	public void addInput( RemusPath in ) {
 		if ( inputs == null )
 			inputs = new ArrayList<RemusPath>();
 		inputs.add(in);
 	}	
 
 	public void addLeftInput( RemusPath in ) {
 		if ( lInputs == null )
 			lInputs = new LinkedList<RemusPath>();
 		lInputs.add(in);
 		addInput(in);
 	}
 
 	public void addRightInput( RemusPath in ) {
 		if ( rInputs == null )
 			rInputs = new LinkedList<RemusPath>();
 		rInputs.add(in);
 		addInput(in);
 	}
 
 	public void addOutput( String name ) {
 		if ( outputs == null ) 
 			outputs = new LinkedList<String>();
 		outputs.add(name);
 	}
 
 	public CodeFragment getCode() {
 		return code;
 	}
 
 	public List<RemusPath> getInputs() {
 		if ( inputs != null )
 			return inputs;
 		return new ArrayList<RemusPath>();
 	}
 
 	public String [] getOutputs() {
 		if ( outputs != null  )
 			return outputs.toArray( new String[0] );
 		return new String[0];
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	public String getSource() {
 		return code.getSource();
 	}
 
 	public void setCodeType(String type) {
 		codeType = type;
 	}
 	
 	public Map getInfo() {
 		Map out = new HashMap();
 		if ( type==MAPPER ) {
 			out.put("mode", "map");
 		}
 		if ( type==REDUCER ) {
 			out.put("mode", "reduce");
 		}
 		if(type==SPLITTER) {
 			out.put("mode", "split");
 		}
 		if(type==PIPE) {
 			out.put("mode", "pipe");
 		}
 		if(type==MERGER) {
 			out.put("mode", "merge");
 		}
 		if(type==MATCHER) {
 			out.put("mode", "match");
 		}
 		if ( outputs != null && outputs.size() > 0 ) {
 			out.put("output", outputs);
 		}
 		return out;
 	}
 
 	public void setPipeline(RemusPipeline remusPipeline) {
 		this.pipeline = remusPipeline;		
 		this.datastore = remusPipeline.getCodeManager().getApp().getDataStore();
 	}
 
 	public RemusPipeline getPipeline() {
 		return this.pipeline;
 	}
 
 
 	public boolean isReady( RemusInstance remusInstance ) {
 		if ( type==STORE )
 			return false;
 		if ( hasInputs() ) {
 			boolean allReady = true;
 			for ( RemusPath iRef : inputs ) {
 				if ( iRef.getInputType() == RemusPath.AppletInput ) {
 					RemusApplet iApplet = getPipeline().getApplet( iRef.getAppletPath() );
 					if ( iApplet != null ) {
 						if ( !iApplet.isComplete(remusInstance) ) {
 							allReady = false;
 						}
 					} else {
 						allReady = false;
 					}
 				} else if ( iRef.getInputType() == RemusPath.DynamicInput ) {
 					if ( datastore.get( getPath() + "@submit", RemusInstance.STATIC_INSTANCE_STR, remusInstance.toString() ) == null ) {
 						allReady = false;
 					}
 				} else if (  iRef.getInputType() == RemusPath.ExternalInput ) {
 				} else if (  iRef.getInputType() == RemusPath.StaticInput ) {
 				} else {				
 					allReady = false;
 				}
 			}
 			return allReady;
 		}		
 		return true;
 	}
 
 
 	public boolean isComplete( RemusInstance remusInstance ) {
 		boolean found = false;
 		for ( Object opStr : datastore.get( getPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR, remusInstance.toString() ) ) {
 			if ( WORKDONE_OP.compareTo((String)opStr) == 0 ) {
 				found = true;
 			}
 		}
 		return found;
 	}
 
 	public boolean hasWorkSet( RemusInstance remusInstance ) {
 		boolean found = false;
 		for ( Object opStr : datastore.get( getPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR, remusInstance.toString() ) ) {
 			if ( WORKGEN_OP.compareTo((String)opStr) == 0 ) {
 				found = true;
 			}
 		}
 		return found;
 	}
 
 	public void setComplete(RemusInstance remusInstance) {
 		datastore.add( getPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR, WORKDONE_OP_CODE, 0, remusInstance.toString(), WORKDONE_OP );
 		datastore.delete( getPath() + "@work", remusInstance.toString() );
 		datastore.delete( getPath() + "@done", remusInstance.toString() );
 	}
 
 	public boolean hasInputs() {
 		if ( inputs == null )
 			return false;
 		return true;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public WorkDescription getWork(RemusInstance inst, long jobID) {
 		WorkDescription out = null;
 		for ( Object obj : datastore.get(getPath() + "@work", inst.toString(), Long.toString(jobID) ) ) {
 			out = (WorkDescription) new WorkDescription(new WorkReference(this, inst, jobID), obj);
 		}
 		return out;
 	}
 
 	public Collection<WorkDescription> getWorkList(RemusInstance inst, int maxListSize) {
 		HashSet<WorkDescription> out = new HashSet<WorkDescription>();
 		if ( !isComplete(inst) ) {
 			if ( isReady(inst)) {
 				if ( !hasWorkSet(inst) ) {
 					generateWork(inst);
 				}
 				int counter = 0;
 				for ( KeyValuePair kv : datastore.listKeyPairs( getPath() + "@work", inst.toString() ) ) {
 					if ( out.size() < maxListSize ) {
 						if ( !datastore.containsKey(getPath() + "@done", inst.toString(), kv.getKey() ) ) {
 							counter++;
 							long jobID = Long.parseLong(kv.getKey());				
 							out.add( new WorkDescription(new WorkReference(this, inst, jobID), kv.getValue()) );
 						}
 					}
 				}
 				if ( out.size() < maxListSize && counter == 0 ) {
 					setComplete(inst);
 				}
 			}
 		}
 
 		return out;
 	}
 
 	public void finishWork(RemusInstance remusInstance, long jobID, String workerName) {
 		datastore.add(getPath() + "@done", remusInstance.toString(), 0L, 0L, Long.toString(jobID), workerName );
 	}
 
 
 
 	private void generateWork(RemusInstance inst) {
 		try {
 			System.err.println("GENERATING WORK");
 			WorkGenerator gen = (WorkGenerator) workGenerator.newInstance();
 			gen.init(this);
 			gen.startWork(inst, 100);
 			//remove traces of a possible previous run
 			//TODO:Make resuming work possible with out delete full workset
 			//datastore.delete( getPath() + "@data", inst.toString() );
 			//datastore.delete( getPath() + "@work", inst.toString() );
 			int counter = 0;
 			boolean hasMore = true;
 			//insert new work into the database in blocks on 1000
 			do {
 				List<KeyValuePair> buffer = new LinkedList<KeyValuePair>();
 				for ( int i =0; i < 1000 && hasMore ; i++) {
 					WorkDescription curWork =gen.nextWork();
 					if ( curWork != null ) {
 						buffer.add( new KeyValuePair(0L, 0L,  Long.toString( curWork.ref.jobID ), curWork.desc) );
 						counter += 1;
 					} else {
 						hasMore = false;
 					}
 				}
 				datastore.add( getPath() + "@work", inst.toString(), buffer );
 			} while ( hasMore );
 			datastore.add( getPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR, WORKGEN_OP_CODE, 0L, inst.toString(), WORKGEN_OP );
 		} catch (InstantiationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IllegalAccessException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 
 	public void addInstance(RemusInstance instance) {
 		if ( !datastore.containsKey(getPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR, instance.toString() ) ) {
 			datastore.add(getPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR, INIT_OP_CODE, 0, instance.toString(), INIT_OP);
 		}
 	}
 
 	public Collection<RemusInstance> getInstanceList() {
 		Collection<RemusInstance> out = new HashSet<RemusInstance>( );
 		for ( String key : datastore.listKeys( getPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR ) ) {
 			out.add( new RemusInstance( key ) );
 		}
 		return out;
 	}
 
 
 	public Collection<RemusInstance> getActiveInstanceList() {
 		Collection<RemusInstance> out = new HashSet<RemusInstance>( );
 		for ( String key : datastore.listKeys( getPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR ) ) {
 			boolean hasDone = false;
 			for ( Object val : datastore.get(getPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR, key ) ) {
 				if ( WORKDONE_OP.compareTo( (String)val) == 0 )
 					hasDone = true;
 			}
 			if ( !hasDone )
 				out.add( new RemusInstance( key ) );				
 		}
 		return out;
 	}
 
 
 	public void deleteInstance(RemusInstance instance) {
 		datastore.delete(getPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR, instance.toString() );		
 		datastore.delete(getPath() + "@work", RemusInstance.STATIC_INSTANCE_STR, instance.toString() );		
 		datastore.delete(getPath() + "@work", instance.toString() );		
 		datastore.delete(getPath() + "@done", instance.toString() );		
 		datastore.delete(getPath() + "@data", instance.toString() );		
 		datastore.delete(getPath() + "@error", instance.toString() );		
 		datastore.delete(getPath() + "@attach", instance.toString() );
 		for ( String subname : getOutputs() ) {
 			datastore.delete( getPath() + "." + subname + "@data", instance.toString() );
 		}		
 		datastore.delete( getPath() + "@done", RemusInstance.STATIC_INSTANCE_STR, instance.toString() );		
 	}
 
 	public void errorWork(RemusInstance inst, long jobID, String workerID, String error) {
 		datastore.add( getPath() + "@error", inst.toString(), jobID, 0L, workerID, error);
 	}
 
	public void formatInput(RemusPath path, InputStream inputStream, Serializer serializer ) {
 		if ( type == STORE ) {
 			if ( codeType.compareTo( "couchdb" ) == 0 ) {
 				try {
 					JsonSerializer json = new JsonSerializer();
 					StringBuilder sb = new StringBuilder();
 					byte [] buffer = new byte[1024];
 					int len;
 					while ((len = inputStream.read(buffer)) > 0) {
 						sb.append( new String(buffer, 0, len ));
 					}
 					Object obj = json.loads(sb.toString());
 					String key = (String) ((Map)obj).get( "_id" );
					datastore.add(getPath() + "@data", path.getInstance(), 0, 0, key, obj);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		} else {
 			try {
 				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
 				String curline = null;
 				List<KeyValuePair> inputList = new ArrayList<KeyValuePair>();
 				while ( (curline = br.readLine() ) != null ) {
 					Map inObj = (Map)serializer.loads(curline);	
 					inputList.add( new KeyValuePair((Long)inObj.get("id"), 
 							(Long)inObj.get("order"), (String)inObj.get("key") , 
 							inObj.get("value") ) );
 				}
				datastore.add( path.getViewPath(), 
						path.getInstance(),
 						inputList );
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 		}
 	}
 
 
 }
