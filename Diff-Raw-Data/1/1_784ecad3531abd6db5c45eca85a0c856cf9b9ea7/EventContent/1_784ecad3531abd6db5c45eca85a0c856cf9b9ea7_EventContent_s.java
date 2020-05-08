 package confdb.data;
 
 
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Collections;
 
 
 /**
  * EventContent
  * ------------
  * @author Philipp Schieferdecker
  *
  * Manage different CMSSW file formats, which Streams & OutputModules
  * are based on.
  */
 public class EventContent extends DatabaseEntry implements Comparable<EventContent>
 {
     //
     // member data
     //
 
     /** name of this event content */
     private String name;
     
     /** collection of output commands */
     private ArrayList<OutputCommand> commands = new ArrayList<OutputCommand>();
     
     /** collection of assigned streams */
     private ArrayList<Stream> streams = new ArrayList<Stream>();
 
     /** parent configuration */
     private IConfiguration config = null;
     
     //
     // construction
     //
     
     /** standard constructor */
     public EventContent(String name)
     {
 	this.name = name;
     }
     
     
     //
     // member functions
     //
     
     /** name of the event content */
     public String name() { return name; }
     
     /** set the name of this event content */
     public void setName(String name) { this.name = name; 
 	setHasChanged();
     }
     
     /** retrieve string representation of this event content */
     public String toString() { return name(); }
     
     /** Comparable: compareTo() */
     public int compareTo(EventContent ec) {return toString().compareTo(ec.toString());}
 
     /** get the parent configuration */
     public IConfiguration config() { return config; }
 
     /** set the parent configuration */
     public void setConfig(IConfiguration config) { this.config = config; }
 
 
     public int databaseId(){
 	return super.databaseId();
     }
 
     public boolean hasChanged(){
 	for (Stream s : streams){
 	    if(s.hasChanged()){
 		setHasChanged();
 		break;
 	    }  
 	}
 	return super.hasChanged();
     }
 
 
     /** number of paths */
     public int pathCount() { return paths().size(); }
 
     /** retrieve i-th path */
     public Path path(int i) { return paths().get(i); }
 
     /** retrieve path by name */
     public Path path(String pathName)
     {
 	Iterator<Path> itP = pathIterator();
 	while (itP.hasNext()) {
 	    Path path = itP.next();
 	    if (path.name().equals(pathName)) return path;
 	}
 	return null;
     }
     
     /** retrieve path iterator */
     public Iterator<Path> pathIterator() { return paths().iterator(); }
 
     /** retrieve path iterator (alphabetical order) */
     public Iterator<Path> orderedPathIterator()
     {
 	ArrayList<Path> orderedPaths = new ArrayList<Path>(paths());
 	Collections.sort(orderedPaths);
 	return orderedPaths.iterator();
     }
 
     /** retrieve index of a given path */
     public int indexOfPath(Path path) { return paths().indexOf(path); }
 
     public void removePath(Path path)
     {
 	if (paths().indexOf(path)>=0) return;
 	Iterator<OutputCommand> itOC = commandIterator();
 	while (itOC.hasNext()) if(itOC.next().parentPath()==path) itOC.remove();
 	path.removeFromContent(this);
 	setHasChanged();
     }
     
     
     /** number of statements */
     public int commandCount() { return commands.size(); }
 
     /** retrieve i-th output command */
     public OutputCommand command(int i)
     {
 	Collections.sort(commands);
 	return commands.get(i);
     }
     
     /** retrieve output command iterator */
     public Iterator<OutputCommand> commandIterator()
     {
 	Collections.sort(commands);
 	return commands.iterator();
     }
     
     /** retrieve index of a given output command */
     public int indexOfCommand(OutputCommand command)
     {
 	Collections.sort(commands);
 	return commands.indexOf(command);
     }
 
     /** insert a output command into event content */
     public boolean insertCommand(OutputCommand command)
     {
 	if (commands.indexOf(command)>=0) {
 	    System.err.println("EventContent.insertCommand WARNING: command '"+
 			       command+"' already in content "+toString()+", skip!");
 	    return false;
 	}
 	if (command.parentPath()!=null&&indexOfPath(command.parentPath())<0) {
 	    System.err.println("EventContent.insertCommand ERROR: path of command '"+
 			       command+"' not in content "+toString()+", skip!");
 	    return false;
 	}
 	commands.add(command);
 	Collections.sort(commands);
 	setHasChanged();
 	return true;
     }
 
     /** remove an output command from this event content */
     public boolean removeCommand(OutputCommand command)
     {
 	int index = commands.indexOf(command);
 	if (index<0) return false;
 	commands.remove(command);
 	return true;
     }
 
     /** get number of commands for a given *stream* */
     public int commandCount(Stream stream)
     {
 	return commands(stream).size();
     }
     
     /** retrive i-th output command for a given *stream* */
     public OutputCommand command(Stream stream, int i)
     {
 	return commands(stream).get(i);
     }
     
     /** retrieve iterator over output commands for a given *stream* */
     public Iterator<OutputCommand> commandIterator(Stream stream)
     {
 	return commands(stream).iterator();
     }
 
 
     /** get number of commands for a given *dataset* */
     public int commandCount(PrimaryDataset dataset)
     {
 	return commands(dataset).size();
     }
     
     /** retrive i-th output command for a given *dataset* */
     public OutputCommand command(PrimaryDataset dataset, int i)
     {
 	return commands(dataset).get(i);
     }
     
     /** retrieve iterator over output commands for a given *dataset* */
     public Iterator<OutputCommand> commandIterator(PrimaryDataset dataset)
     {
 	return commands(dataset).iterator();
     }
 
 
     /** get number of commands for a given *path* */
     public int commandCount(Path path)
     {
 	return commands(path).size();
     }
     
     /** retrive i-th output command for a given *path* */
     public OutputCommand command(Path path, int i)
     {
 	return commands(path).get(i);
     }
     
     /** retrieve iterator over output commands for a given *path* */
     public Iterator<OutputCommand> commandIterator(Path path)
     {
 	return commands(path).iterator();
     }
 
 
     /** number of streams associated with this event content */
     public int streamCount() { return streams.size(); }
 
     /** retrieve i-th stream associated with event content */
     public Stream stream(int i) { return streams.get(i); }
 
     /** retrieve stream with specific name from event content */
     public Stream stream(String streamName) {
 	for (Stream s : streams) if (s.name().equals(streamName)) return s;
 	return null;
     }
     
     /** retrieve stream iterator */
     public Iterator<Stream> streamIterator() { return streams.iterator(); }
 
     /** retrieve index of a given stream */
     public int indexOfStream(Stream stream) { return streams.indexOf(stream); }
 
     /** associate an existing stream with this event content */
     public Stream insertStream(String streamName)
     {
 	Iterator<Stream> itS = streamIterator();
 	while (itS.hasNext())
 	    if (itS.next().name().equals(streamName)) return null;
 	Stream stream = new Stream(streamName,this);
 	streams.add(stream);
 	setHasChanged();
 	return stream;
     }
     
     /** remove a stream from the event content */
     public boolean removeStream(Stream stream)
     {
 	int index = streams.indexOf(stream);
 	if (index<0) return false;
 	stream.removeOutputModuleReferences();
 	streams.remove(index);
 	setHasChanged();
 	return true;
     }
 
 
     /** remove all stream from the event content */
     public void removeStreams()
     {
 
 	for(int i = streamCount()-1;i>=0;i--){
 	    Stream stream = stream(i);
 	    if(stream!=null)
 		removeStream(stream);
 	}
     }
 
     /** retrieve number of associated primary dataset */
     public int datasetCount() { return datasets().size(); }
     
     /** retrieve i-th primary dataset */
     public PrimaryDataset dataset(int i) { return datasets().get(i); }
 
     /** retireve primary dataset by name */
     public PrimaryDataset dataset(String datasetName)
     {
 	Iterator<PrimaryDataset> itPD = datasetIterator();
 	while (itPD.hasNext()) {
 	    PrimaryDataset dataset = itPD.next();
 	    if (dataset.name().equals(datasetName)) return dataset;
 	}
 	return null;
     }
 
     /** retrieve dataset iterator */
     public Iterator<PrimaryDataset> datasetIterator()
     {
 	return datasets().iterator();
     }
     
     /** retrieve index of a given dataset */
     public int indexOfDataset(PrimaryDataset dataset)
     {
 	return datasets().indexOf(dataset);
     }
 
 
     //
     // private memeber functions
     //
 
     /** retrieve list of paths from associated streams */
     private ArrayList<Path> paths()
     {
 	ArrayList<Path> result = new ArrayList<Path>();
 	Iterator<Stream> itS = streamIterator();
 	while (itS.hasNext()) {
 	    Iterator<Path> itP = itS.next().pathIterator();
 	    while (itP.hasNext()) {
 		Path path = itP.next();
 		if (result.indexOf(path)<0) result.add(path);
 	    }
 	}
 	return result;
     }
 
 
     /** retrieve list of datasets from associated streams */
     private ArrayList<PrimaryDataset> datasets()
     {
 	ArrayList<PrimaryDataset> result = new ArrayList<PrimaryDataset>();
 	Iterator<Stream> itS = streamIterator();
 	while (itS.hasNext()) {
 	    Iterator<PrimaryDataset> itPD = itS.next().datasetIterator();
 	    while (itPD.hasNext()) result.add(itPD.next());
 	}
 	return result;
     }
 
     /** retrieve list of output commands associated with given stream */
     private ArrayList<OutputCommand> commands(Stream stream)
     {
 	ArrayList<OutputCommand> result = new ArrayList<OutputCommand>();
 	Iterator<OutputCommand> itOC = commandIterator();
 	while (itOC.hasNext()) {
 	    OutputCommand command = itOC.next();
 	    Path          path = command.parentPath();
 	    if (path==null||stream.indexOfPath(path)>=0) result.add(command);
 	}
 	return result;
     }
 
     /** retrieve list of output commands associated with given dataset */
     private ArrayList<OutputCommand> commands(PrimaryDataset dataset)
     {
 	ArrayList<OutputCommand> result = new ArrayList<OutputCommand>();
 	Iterator<OutputCommand> itOC = commandIterator();
 	while (itOC.hasNext()) {
 	    OutputCommand command = itOC.next();
 	    Path          path    = command.parentPath();
 	    if (path==null||dataset.indexOfPath(path)>=0) result.add(command);
 	}
 	return result;
     }
     
     /** retrieve list of output commands associated with given path */
     private ArrayList<OutputCommand> commands(Path path)
     {
 	ArrayList<OutputCommand> result = new ArrayList<OutputCommand>();
 	Iterator<OutputCommand> itOC = commandIterator();
 	while (itOC.hasNext()) {
 	    OutputCommand command = itOC.next();
 	    if (command.parentPath()==path) result.add(command);
 	}
 	return result;
     }
 
 
 }
