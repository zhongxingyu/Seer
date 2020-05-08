  package confdb.data;
 
 
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Collections;
 
 
 /**
  * Stream
  * ------
  * @author Philipp Schieferdecker
  *
  * streams contain primary datasets, and thereby implicitely a list of
  * paths.
  */
 public class Stream extends DatabaseEntry implements Comparable<Stream>
 {
     //
     // member data
     //
 
     /** name of the stream */
     private String name;
 
     /** fraction of events to be writte to local disk by SM */
     private double fractionToDisk = 1.0;
     
     /** reference to parent event content */
     private EventContent parentContent = null;
     
     /** associated output module */
     private OutputModule outputModule = null;
 
     /** collection of assigned paths */
     private ArrayList<Path> paths = new ArrayList<Path>();
 
     /** collection of assigned paths */
     private ArrayList<PrimaryDataset> datasets=new ArrayList<PrimaryDataset>();
     
     
     //
     // construction
     //
     
     /** standard constructor */
     public Stream(String name,EventContent parentContent)
     {
 	this.name = name;
 	this.parentContent = parentContent;
 	this.outputModule = new OutputModule("hltOutput"+name,this);
     }
     
     
     //
     // member functions
     //
 
     /** name of this stream */
     public String name() { return name; }
     
     /** retrieve fraction of events to be written to local disc by SM */
     public double fractionToDisk() { return fractionToDisk; }
     
     /** set name of this stream */
     public void setName(String name) { 
 	this.name = name;
        setHasChanged();
 	for (Path p : paths) {
 	    p.setHasChanged();
 	}
     }
     
     /** DatabaseEntry: databaseId() */
     public int databaseId() { return super.databaseId(); }
     
     /** DatabaseEntry: hasChanged() */
     public boolean hasChanged()
     {
 	for (Path p : paths) {
 	    if(p.hasChanged()) {
 		setHasChanged();
 		return super.hasChanged();
 	    }
 	}
 	for (PrimaryDataset pd : datasets) {
 	    if(pd.hasChanged()) {
 		setHasChanged();
 	       	return super.hasChanged();
 	    }
 	}
 	
 	return super.hasChanged();
     }
     
     /** set the fraction of events to be writte to local disk by SM */
     public void setFractionToDisk(double fractionToDisk)
     {
 	if (fractionToDisk>=0.0&&fractionToDisk<=1.0){
 	    this.fractionToDisk = fractionToDisk;
 	    setHasChanged();
 	}
 	else System.err.println("Stream.setFractionToDisk() ERROR: "+
 				"fraction = " + fractionToDisk +
 				" not in [0,1]!");
     }
     
     /** overload 'toString()' */
     public String toString() { return name(); }
 
     /** get parent event content */
     public EventContent parentContent() { return parentContent; }
 
     /** get associated output module */
     public OutputModule outputModule() { return outputModule; }
    
     /** get the parent configuration */
     public IConfiguration config() { return parentContent.config(); }
 
     /** Comparable: compareTo() */
     public int compareTo(Stream s) {return toString().compareTo(s.toString());}
     
     /** number of paths */
     public int pathCount() { return paths.size(); }
     
     /** retrieve i-th path */
     public Path path(int i) { return paths.get(i); }
     
     /** retrieve path by name */
     public Path path(String pathName)
     {
 	for (Path p : paths) if (p.name().equals(pathName)) return p;
 	return null;
     }
     
     /** retrieve index of a given path */
     public int indexOfPath(Path path) { return paths.indexOf(path); }
     
     /** retrieve iterator over paths */
     public Iterator<Path> pathIterator() { return paths.iterator(); }
     
     /** retrieve path iterator (alphabetical order) */
     public Iterator<Path> orderedPathIterator()
     {
 	ArrayList<Path> orderedPaths = new ArrayList<Path>(paths);
 	Collections.sort(orderedPaths);
 	return orderedPaths.iterator();
     }
     
     /** associate another path with this stream */
     public boolean insertPath(Path path)
     {
 	if (paths.indexOf(path)>=0) return false;
 	path.addToContent(parentContent);
 	paths.add(path);
 	setHasChanged();
 	return true;
     }
     
     /** remove a path from this stream */
     public boolean removePath(Path path)
     {
 	int index = paths.indexOf(path);
 	if (index<0) return false;
 	paths.remove(index);
 	setHasChanged();
 	parentContent.removePath(path);
 	Iterator<PrimaryDataset> itPD = datasetIterator();
 	while (itPD.hasNext()) {
 	    PrimaryDataset dataset = itPD.next();
 	    if (dataset.indexOfPath(path)>=0) dataset.removePath(path);
 	}
 	return true;
     }
     
     /** number of paths assigned to a dataset */
     public int assignedPathCount() { return listOfAssignedPaths().size(); }
     
     /** retrieve collection of paths assigned to datasets */
     public ArrayList<Path> listOfAssignedPaths()
     {
 	ArrayList<Path> result = new ArrayList<Path>();
 	Iterator<PrimaryDataset> itPD = datasetIterator();
 	while (itPD.hasNext()) {
 	    PrimaryDataset PD = itPD.next();
 	    Iterator<Path> itP = PD.pathIterator();
 	    while (itP.hasNext()) result.add(itP.next());
 	}
 	return result;
     }
 
 
     /** number of paths NOT assigned to any dataset */
     public int unassignedPathCount() { return listOfUnassignedPaths().size(); }
     
     /** retrieve collection of paths NOT assigned to any dataset */
     public ArrayList<Path> listOfUnassignedPaths()
     {
 	ArrayList<Path> result = new ArrayList<Path>();
 	ArrayList<Path> assigned = listOfAssignedPaths();
 	Iterator<Path> itP = pathIterator();
 	while (itP.hasNext()) {
 	    Path path = itP.next();
 	    if (assigned.indexOf(path)<0) result.add(path);
 	}
 	return result;
     }
 
 
     /** number of primary datasets */
     public int datasetCount() { return datasets.size(); }
     
     /** retrieve i-th primary dataset */
     public PrimaryDataset dataset(int i) { return datasets.get(i); }
 
     /** retrieve primary dataset by name */
     public PrimaryDataset dataset(String datasetName)
     {
 	for (PrimaryDataset pd : datasets)
 	    if (pd.name().equals(datasetName)) return pd;
 	return null;
     }
     
     /** retrieve primary dataset which contains specified path */
     public PrimaryDataset dataset(Path path)
     {
 	for (PrimaryDataset pd : datasets)
 	    if (pd.indexOfPath(path)>=0) return pd;
 	return null;
     }
     
     /** retrieve primary dataset iterator */
     public Iterator<PrimaryDataset> datasetIterator()
     {
 	return datasets.iterator();
     }
 
     /** index of a given primary dataset */
     public int indexOfDataset(PrimaryDataset ds) {return datasets.indexOf(ds);}
     
     /** insert and associate a primary dataset with this stream */
     public PrimaryDataset insertDataset(String datasetName)
     {
         for (PrimaryDataset pd : datasets)
             if (pd.name().equals(datasetName)) {
                 return null;
             }
         PrimaryDataset result = new PrimaryDataset(datasetName, this);
         datasets.add(result);
         setHasChanged();
         return result;
     }
     
     /** remove a dataset from this stream */
     public boolean removeDataset(PrimaryDataset dataset)
     {
 	int index = datasets.indexOf(dataset);
 	if (index<0) return false;
 	datasets.remove(index);
 	setHasChanged();
 	return true;
 
     }
 
     /** remove reference to this stream's outputmodule */
     public void removeOutputModuleReferences()
     {
 	for (int i=outputModule.referenceCount()-1;i>=0;i--) {
 	    outputModule.reference(i).remove();
 	}
     }
     
 }
