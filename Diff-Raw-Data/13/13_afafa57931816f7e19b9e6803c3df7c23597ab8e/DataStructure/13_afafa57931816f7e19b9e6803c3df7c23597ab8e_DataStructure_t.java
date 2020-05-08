 package com.crypticbit.javelin.js;
 
 import java.io.*;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jgrapht.DirectedGraph;
 import org.jgrapht.graph.DefaultEdge;
 
 import com.crypticbit.javelin.diff.ThreeWayDiff;
 import com.crypticbit.javelin.js.convert.VisitorException;
 import com.crypticbit.javelin.store.CasKasStore;
 import com.crypticbit.javelin.store.Digest;
 import com.crypticbit.javelin.store.Identity;
 import com.crypticbit.javelin.store.StoreException;
 import com.crypticbit.javelin.store.cas.PersistableResource;
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonSyntaxException;
 import com.jayway.jsonpath.Filter;
 import com.jayway.jsonpath.HackedJsonPath;
 import com.jayway.jsonpath.internal.PathToken;
 import com.jayway.jsonpath.internal.PathTokenizer;
 import com.jayway.jsonpath.internal.filter.PathTokenFilter;
 import com.jayway.jsonpath.spi.JsonProvider;
 import com.jayway.jsonpath.spi.JsonProviderFactory;
 
 import difflib.PatchFailedException;
 
 // what is immutable? commit? element? anchors?
 // multiple anchors?
 // FIXME tidy up exceptions
 public class DataStructure {
 
     private static final Logger LOG = Logger.getLogger("com.crypticbit.javelin.js");
 
     /** The current head of the Json data structure */
     private ExtendedAnchor<CommitDao> selectedAnchor;
     private ExtendedAnchor<LabelsDao> labelsAnchor;
     private JsonElement element;
     /**
      * The last known head of the actual data structure, set after a read or write operation
      */
     private Commit commit;
     /** The underlying data store */
     private CasKasStore store;
     private JsonStoreAdapterFactory jsonFactory;
 
     /**
      * Create a new json data structure with a random anchor, which can be retrieved using <code>getCommitAnchor</code>
      * 
      * @throws VisitorException
      * @throws StoreException
      */
     public DataStructure(CasKasStore store) throws StoreException, VisitorException {
 	setup(store);
 	labelsAnchor = new ExtendedAnchor<LabelsDao>(jsonFactory, LabelsDao.class);
 	LabelsDao labels = new LabelsDao();
 	selectedAnchor = labels.addCommitAnchor("HEAD", jsonFactory);
 	labelsAnchor.writeEndPoint(store, labels);
 
 	   // see if this works - FIXME
	// labelsAnchor.readEndPoint(store).getCommitAnchor("HEAD",jsonFactory).readEndPoint(store);
 
     }
 
     /**
      * Create a new json data structure - by selecting a label from an existing ds by name
      * 
      * @throws Error
      * @throws VisitorException
      * @throws StoreException
      * @throws JsonSyntaxException
      */
     DataStructure(CasKasStore store, Identity labelsAddress, String label) throws JsonSyntaxException, StoreException,
 	    VisitorException, Error {
 	setup(store);
 	this.labelsAnchor = new ExtendedAnchor<LabelsDao>(labelsAddress, jsonFactory, LabelsDao.class);
 	if (labelsAnchor.readEndPoint(store).hasCommitAnchor(label)) {
 	    this.selectedAnchor = labelsAnchor.getEndPoint().getCommitAnchor(label, jsonFactory);
 	}
 	else {
 	    // FIXME exception handling, and thought about changing to unknown branch
 	    throw new Error("Labels don't exist: " + labelsAnchor.getEndPoint());
 	    // this.selectedAnchor = labels.addCommitAnchor(label);
 	}
     }
 
     /*
      * public DataStructure(CasKasStore store, Identity identity) { this(store); this.selectedAnchor = new
      * Anchor(identity); }
      */
 
     /**
      * Create a new branch. Same DS
      */
     private DataStructure(DataStructure parent) throws StoreException {
 	setup(parent.store);
 	this.labelsAnchor = parent.labelsAnchor;
 	this.selectedAnchor = new ExtendedAnchor<>(store, parent.selectedAnchor, jsonFactory, CommitDao.class);
 
     }
 
     public DataStructure branch() throws StoreException {
 	return new DataStructure(this);
     }
 
     // FIXME - what about persisting shared labels? Would they be betetr stored
     // as our own mergable data structure?
     /*
      * public DataStructure branch(String newLabel) throws StoreException { return new DataStructure(store, labels,
      * newLabel); }
      */
 
     public synchronized DataStructure checkout() throws StoreException, JsonSyntaxException, VisitorException {
 	commit = new Commit(selectedAnchor.readEndPoint(store), selectedAnchor.getDestination(), jsonFactory);
 	element = commit.getElement();
 	if (LOG.isLoggable(Level.FINER)) {
 	    LOG.log(Level.FINER, "Reading commit: " + commit);
 	}
 	return this;
     }
 
     public synchronized DataStructure commit() throws StoreException, VisitorException {
 	Identity write = jsonFactory.getJsonElementAdapter().write(element);
 	createCommit(write, selectedAnchor.getDestination());
 	return checkout();
     }
 
     public void exportAll(OutputStream outputStream) throws JsonSyntaxException, StoreException, VisitorException,
 	    IOException {
 	
 	ObjectOutputStream oos = new ObjectOutputStream(outputStream);
 	Set<Identity> tempCas = new HashSet<>();
 	Set<Identity> tempKas = new HashSet<>();
 	
 	oos.writeObject(labelsAnchor.getAddress());
 	
 	LabelsDao labels = labelsAnchor.readEndPoint(store);
 	for (String label : labels.getLabels()) {
 	    System.out.println("Processing label "+label);
 	    Identity commitAddress = labels.getCommitAnchor(label, jsonFactory).getAddress();
 	    tempKas.add(commitAddress);
 	    CommitDao commitDao = labels.getCommitAnchor(label, jsonFactory).readEndPoint(store);
 	    Identity destination = labels.getCommitAnchor(label, jsonFactory).read(store);
 	    System.out.println("dest="+destination+","+commitDao);
 	    Commit c = new Commit(commitDao, destination, jsonFactory);
 	    DirectedGraph<Commit, DefaultEdge> x = c.getAsGraphToRoot();
 
 	    for (Commit v :  x.vertexSet()) {
 		tempCas.addAll(v.getAllIdentities());
 		System.out.println("Commit="+v.getIdentity2()+","+v.toString()+","+store.get(v.getIdentity2()));
 	    }
 	    
 	   
 	}
 	tempKas.add(labelsAnchor.getAddress());
 	tempCas.add(labelsAnchor.getDestination());
 	
 	System.out.println("cas: "+tempCas);
 	System.out.println("kas: "+tempKas);
 	
 	System.out.println("all: "+store.list());
 	
 	Set<Identity> missing = new HashSet(store.list());
 	missing.removeAll(tempCas);
 	missing.removeAll(tempKas);
 	
 	
 	for(Identity i : missing) {
 	    System.out.println(i+"="+store.get(i));
 	    
 	}
 	
 	Map<Identity, PersistableResource> casRresult = new HashMap<>();
 	for (Identity i : tempCas) {
 	    casRresult.put(i, store.get(i));
 	}
 	oos.writeObject(casRresult);
 
 	Map<Identity, PersistableResource> kasResult = new HashMap<>();
 	for (Identity i : tempKas) {
 	    kasResult.put(i, store.get(i));
 	}
 	oos.writeObject(kasResult);
 
     }
 
     public Commit getCommit() {
 	return commit;
     }
 
     /*
      * public Identity getCommitAnchor() { return selectedAnchor.getDigest(); }
      */
 
     public Identity getLabelsAddress() {
 	return labelsAnchor.getAddress();
     }
 
     public void importAll(InputStream inputStream) throws IOException, ClassNotFoundException,
 	    StoreException, JsonSyntaxException, VisitorException {
 	ObjectInputStream ois = new ObjectInputStream(inputStream);
 
 
 	Identity labelsAddress = (Identity) ois.readObject();
 	    ExtendedAnchor<LabelsDao> importedLabels = new ExtendedAnchor<>(labelsAddress, jsonFactory, LabelsDao.class);
 	    LabelsDao localLabels = labelsAnchor.readEndPoint(store);
 
 
 	// copy all cas elements
 	Map<Identity, PersistableResource> casResult = (Map<Identity, PersistableResource>) ois.readObject();
 	for (Entry<Identity, PersistableResource> x : casResult.entrySet()) {
 	    System.out.println("Wrote cas: "+x.getKey());
 	    Identity idOfValueWrittenToStore = store.store(x.getValue());
 	    if (!idOfValueWrittenToStore.equals(x.getKey())) {
 		throw new IllegalStateException("The entry " + x.getKey() + " produced a new key on store to local ("
 			+ idOfValueWrittenToStore + ")");
 	    }
 	}
 
 	// copy all kas elements
 	Map<Identity, PersistableResource> kasResult = (Map<Identity, PersistableResource>) ois.readObject();
 	for (Entry<Identity, PersistableResource> x : kasResult.entrySet()) {
 	    System.out.println("Wrote kas: "+x.getKey());
 	    // FIXME - what should I check against?
 	    // FIXME store.get(x.getKey()).getBytes()) very cumbersome
 	    if(store.check(x.getKey()))             {
 		    System.out.println("Over-writing:" +x.getKey());
 		    store.store(x.getKey(),new Digest(store.get(x.getKey()).getBytes()),new Digest(x.getValue().getBytes()));
 	    }else
 		store.store(x.getKey(),null,new Digest(x.getValue().getBytes()));
 	    	System.out.println("Got :"+new Digest(store.get(x.getKey()).getBytes()));
 	}
 
 	    // merge labels
 
 	    for (String importedLabel : importedLabels.readEndPoint(store).getLabels()) {
 		    if (localLabels.hasCommitAnchor(importedLabel)) {
			    System.out.println("Merging label "+importedLabel+ " from "+localLabels);
 			    ExtendedAnchor<CommitDao> commitAnchor = localLabels.getCommitAnchor(importedLabel, jsonFactory);
 			    commitAnchor.read(store);
 			    commitAnchor.writeEndPoint(store,
 				      importedLabels.readEndPoint(store).getCommitAnchor(importedLabel, jsonFactory).readEndPoint(store));
 		    }    else
 			    localLabels.addAnchor(importedLabel, importedLabels.readEndPoint(store).getCommitAnchor(importedLabel, jsonFactory));
 	    }
 
     }
 
     public Object lazyRead() throws JsonSyntaxException, StoreException, VisitorException {
 	return commit.getObject();
     }
 
     public synchronized DataStructure merge(DataStructure other) throws JsonSyntaxException, StoreException,
 	    PatchFailedException, VisitorException {
 	ThreeWayDiff patch = commit.createChangeSet(other.commit);
 	Identity valueIdentity = jsonFactory.getJsonObjectAdapter().write(patch.apply());
 	createCommit(valueIdentity, selectedAnchor.getDestination(), other.selectedAnchor.getDestination());
 	return checkout();
     }
 
     public JsonElement read() {
 	return element;
     }
 
     public void saveLabel(String label) throws StoreException, VisitorException {
 	labelsAnchor.readEndPoint(store).addAnchor(label, this.selectedAnchor);
 	labelsAnchor.writeEndPoint(store, labelsAnchor.getEndPoint());
     }
 
     public DataStructure write(String string) {
 	// FIXME resue Gson
 	element = new Gson().fromJson(string, JsonElement.class);
 	return this;
     }
 
     public void write(String path, String json) throws JsonSyntaxException, StoreException, VisitorException {
 	HackedJsonPath compiledPath = new HackedJsonPath(path, new Filter[] {});
 	// code copied from jsonpath
 	// FIXME reuse JSON
 	// FIXME should this be in JCA?
 	Object jsonObject = new Gson().fromJson(json, Object.class);
 	System.out.println(jsonObject.getClass());
 	if (!(jsonObject instanceof Map) && !(jsonObject instanceof List)) {
 	    throw new IllegalArgumentException("Invalid container object");
 	}
 	JsonProvider jsonProvider = JsonProviderFactory.createProvider();
 	Object originalResult, result;
 	originalResult = result = lazyRead();
 
 	PathTokenizer tokenizer = compiledPath.getTokenizer();
 	PathToken lastToken = tokenizer.removeLastPathToken();
 	for (PathToken pathToken : tokenizer) {
 	    PathTokenFilter filter = pathToken.getFilter();
 	    result = filter.filter(result, jsonProvider);
 	}
 
 	if (lastToken.isArrayIndexToken()) {
 	    ((List) result).set(lastToken.getArrayIndex(), jsonObject);
 	}
 	else {
 	    ((Map) result).put(lastToken.getFragment(), jsonObject);
 	}
 
 	Identity valueIdentity = jsonFactory.getJsonObjectAdapter().write(originalResult);
 	createCommit(valueIdentity, selectedAnchor.getDestination());
 	checkout();
     }
 
     private void setup(CasKasStore store) {
 	this.store = store;
 	jsonFactory = new JsonStoreAdapterFactory(store);
     }
 
     private void createCommit(Identity valueIdentity, Identity... parents) throws StoreException, VisitorException {
 	// FIXME hardcoded user
 	commit = new Commit(selectedAnchor.writeEndPoint(store, new CommitDao(valueIdentity, new Date(), "auser",
 		parents)), selectedAnchor.getDestination(), jsonFactory);
 	if (LOG.isLoggable(Level.FINEST)) {
 	    LOG.log(Level.FINEST, "Updating id -> " + selectedAnchor.getDestination());
 	}
     }
 }
