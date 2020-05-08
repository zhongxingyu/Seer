 package confdb.data;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 
 
 /**
  * ReferenceContainer
  * ------------------
  * @author Philipp Schieferdecker
  *  
  * Common base class of Path and Sequence.
 */
 abstract
     public class ReferenceContainer extends    DatabaseEntry
                                     implements Comparable<ReferenceContainer>,
 					       Referencable
 {
     //
     // member data
     //
 
     /** name of the container */
     private String name = null;
 
     /** list of contained references */
     protected ArrayList<Reference> entries = new ArrayList<Reference>();
     
     /** references of this path within other paths */
     protected ArrayList<Reference> references = new ArrayList<Reference>();
 
     /** the parent configuration of the container*/
     private IConfiguration config = null;
     
 
     //
     // construction
     //
 
     /** standard constructor */
     public ReferenceContainer(String name)
     {
 	this.name = name.replaceAll("\\W", "");
     }
 
     //
     // abstract member functions
     //
     
     /** insert an entry into container */
     abstract public void insertEntry(int i,Reference reference);
     
     /** check if container contains the specified reference */
     abstract public boolean containsEntry(Reference reference);
 
     /** create a reference of this in a reference container (path/sequence) */
     abstract public Reference createReference(ReferenceContainer container,
 					      int i);
     
     
     //
     // member functions
     //
     
     /** overload toString() */
     public String toString() { return name(); }
 
     /** DatabaseEntry:indicate wether in the DB or changed */
     public boolean hasChanged() 
     {
 	if (databaseId()==0) return true;
 	for (Reference r : entries) {
 	    DatabaseEntry dbentry = (DatabaseEntry)r.parent();
 	    if (dbentry!=null&&dbentry.hasChanged()) {
 		setHasChanged();
 		return true;
 	    }
 	}
 	return false;
     }
     
     /** Comparable: compareTo() */
     public int compareTo(ReferenceContainer rc)
     {
 	return toString().compareTo(rc.toString());
     }
     
     /** get the name of the container */
     public String name() { return name; }
     
     /** set the name of the container */
     public void setName(String name) throws DataException
     {
        name = name.replaceAll("\\W", "");
 	if (config!=null&&!config.isUniqueQualifier(name))
 	    throw new DataException("ReferenceContainer.setName ERROR: '"+
 				    name+"' is not a unique qualifier.");
 	this.name = name;
 	setHasChanged();
     }
     
      /** get parent configuration */
     public IConfiguration config() { return this.config; }
 
     /** set the configuration of this container */
     public void setConfig(IConfiguration config) { this.config = config; }
 
     /** calculate the number of unresolved InputTags */
     public int unresolvedInputTagCount()
     {
 	return unresolvedInputTags().length;
     }
     
     /** get unresolved InputTags */
     public String[] unresolvedInputTags()
     {
 	ArrayList<String> unresolved = new ArrayList<String>();
 	HashSet<String> labels = new HashSet<String>();
 	for (Reference r : entries)
 	    getUnresolvedInputTags(r,labels,unresolved,name());
 	return unresolved.toArray(new String[unresolved.size()]);
     }
 
     /** does this container contain an OutputModule? */
     public boolean hasOutputModule()
     {
 	for (Reference r : entries) {
 	    Referencable parent = r.parent();
 	    if (parent instanceof OutputModule) return true;
 	    else if (parent instanceof ReferenceContainer) {
 		ReferenceContainer container = (ReferenceContainer)parent;
 		if (container.hasOutputModule()) return true;
 	    }
 	}
 	return false;
     }
     
     /** does this container contain an EDProducer? */
     public boolean hasEDProducer()   { return hasModuleOfType("EDProducer"); }
 
     /** does this container contain an EDFilter? */
     public boolean hasEDFilter() { return hasModuleOfType("EDFilter"); }
 
     /** does this container contain an HLTFilter? */
     public boolean hasHLTFilter() { return hasModuleOfType("HLTFilter"); }
     
    /** get entry iterator */
     public Iterator<Reference> entryIterator() { return entries.iterator(); }
 
     /** number of entries */
     public int entryCount() { return entries.size(); }
 
     /** retrieve i-th entry*/
     public Reference entry(int i) { return entries.get(i); }
     
     /** retrieve reference by name */
     public Reference entry(String name)
     {
 	for (Reference e : entries)
 	    if (e.name().equals(name)) return e;
 	return null;
     }
 
     /** index of a certain entry */
     public int indexOfEntry(Reference reference)
     {
 	return entries.indexOf(reference);
     }
     
     /** remove a reference from the container */
     public void removeEntry(Reference reference)
     {
 	int index = entries.indexOf(reference);
 	if (index>=0) {
 	    entries.remove(index);
 	    setHasChanged();
 	}
 	else {
 	    System.out.println("ReferenceContainer.removeEntry FAILED.");
 	}
     }
     
      /** move an entry to a new position within the container */
     public boolean moveEntry(Reference reference,int targetIndex)
     {
 	int currentIndex = entries.indexOf(reference);
 	if (currentIndex<0) return false;
 	if (currentIndex==targetIndex) return true;
 	if (targetIndex>entries.size()) return false;
 	if (currentIndex<targetIndex) targetIndex--;
 	entries.remove(currentIndex);
 	entries.add(targetIndex,reference);
 	setHasChanged();
 	return true;
     }
     
 
     /** create iterator for all contained Paths */
     public Iterator<Path> pathIterator()
     {
 	ArrayList<Path> paths = new ArrayList<Path>();
 	getPathsAmongEntries(entryIterator(),paths);
 	return paths.iterator();
     }
 
     /** create iterator for all contained Sequences */
     public Iterator<Sequence> sequenceIterator()
     {
 	ArrayList<Sequence> sequences = new ArrayList<Sequence>();
 	getSequencesAmongEntries(entryIterator(),sequences);
 	return sequences.iterator();
     }
 
     /** create iterator for all contained Modules */
     public Iterator<ModuleInstance> moduleIterator()
     {
 	ArrayList<ModuleInstance> modules = new ArrayList<ModuleInstance>();
 	getModulesAmongEntries(entryIterator(),modules);
 	return modules.iterator();
     }
     
     /** create iterator for all contained OutputModules */
     public Iterator<OutputModule> outputIterator()
     {
 	ArrayList<OutputModule> outputs = new ArrayList<OutputModule>();
 	getOutputsAmongEntries(entryIterator(),outputs);
 	return outputs.iterator();
     }
     
     /** create iterator for all contained References */
     public Iterator<Reference> recursiveReferenceIterator()
     {
 	ArrayList<Reference> references = new ArrayList<Reference>();
 	getReferences(entryIterator(),references);
 	return references.iterator();
     }
     
 
 
     
     /** number of references */
     public int referenceCount() { return references.size(); }
     
     /** retrieve the i-th reference */
     public Reference reference(int i) { return references.get(i); }
 
     /** test if a specifc reference refers to this entity */
     public boolean isReferencedBy(Reference reference)
     {
 	return references.contains(reference);
     }
 
     /** remove a reference of this */
     public void removeReference(Reference reference)
     {
 	int index = references.indexOf(reference);
 	references.remove(index);
     }
 
    /** get list of parent paths */
     public Path[] parentPaths()
     {
 	ArrayList<Path> list = new ArrayList<Path>();
 	for (int i=0;i<referenceCount();i++) {
 	    Path[] paths = reference(i).parentPaths();
 	    for (Path p : paths) list.add(p);
 	}
 	return list.toArray(new Path[list.size()]);
     }
     
     /** number of unset tracked paremters */
     public int unsetTrackedParameterCount()
     {
 	int result = 0;
 	for (Reference r : entries) {
 	    if (r instanceof ModuleReference) {
 		ModuleReference module = (ModuleReference)r;
 		result += module.unsetTrackedParameterCount();
 	    }
 	}
 	return result;
     }
 
     
     //
     // private member functions
     //
 
     /** does this container contain a module of type 'type' */
     private boolean hasModuleOfType(String type)
     {
 	for (Reference r : entries) {
 	    Referencable parent = r.parent();
 	    if (parent instanceof ModuleInstance) {
 		ModuleInstance module = (ModuleInstance)parent;
 		if (module.template().type().equals(type)) return true;
 	    }
 	    else if (parent instanceof ReferenceContainer) {
 		ReferenceContainer container = (ReferenceContainer)parent;
 		if (container.hasModuleOfType(type)) return true;
 	    }
 	}
 	return false;
     }
 
     /** get unresolved InputTags from a reference, given labels to this point */
     private void getUnresolvedInputTags(Reference r,
 					HashSet<String> labels,
 					ArrayList<String> unresolved,
 					String prefix)
     {
 	if (r instanceof ModuleReference) {
 	    ModuleReference modref = (ModuleReference)r;
 	    ModuleInstance  module = (ModuleInstance)modref.parent();
 	    labels.add(module.name());
 	    Iterator<Parameter> it = module.parameterIterator();
 	    while (it.hasNext()) {
 		Parameter p = it.next();
 		getUnresolvedInputTags(p,labels,unresolved,prefix+"/"+module.name());
 	    }
 	}
 	else if (r instanceof OutputModuleReference) {
 	}
 	else {
 	    ReferenceContainer container = (ReferenceContainer)r.parent();
 	    Iterator<Reference> it = container.entryIterator();
 	    while (it.hasNext()) {
 		Reference entry = it.next();
 		getUnresolvedInputTags(entry,labels,unresolved,prefix+"/"+r.name());
 	    }
 	}
     }
 
     /** get unresolved InputTags from a parameter, given labels to this point */
     private void getUnresolvedInputTags(Parameter p,
 					HashSet<String> labels,
 					ArrayList<String> unresolved,
 					String prefix)
     {
 	if (p instanceof InputTagParameter) {
 	    InputTagParameter itp = (InputTagParameter)p;
 
 	    if (!itp.isValueSet()||
 		itp.label().equals(new String())||
 		itp.label().equals("")||
 		itp.label().equals("rawDataCollector")||
 		itp.label().equals("source")) return;
 	    
 	    if (!labels.contains(itp.label())) {
 		unresolved.add(prefix+"::"+itp.name()+"="+itp.valueAsString());
 	    }
 	}
 	else if (p instanceof VInputTagParameter) {
 	    VInputTagParameter vitp = (VInputTagParameter)p;
 	    for (int i=0;i<vitp.vectorSize();i++) {
 		InputTagParameter itp =
 		    new InputTagParameter("["+(new Integer(i)).toString()+"]",
 					  vitp.value(i).toString(),false);
 		itp.setParent(vitp);
 		getUnresolvedInputTags(itp,labels,unresolved,prefix+"::"+vitp.name());
 	    }
 	}
 	else if (p instanceof PSetParameter) {
 	    PSetParameter pset = (PSetParameter)p;
 	    for (int i=0;i<pset.parameterCount();i++)
 		getUnresolvedInputTags(pset.parameter(i),labels,unresolved,prefix+"::"+pset.name());
 	}
 	else if (p instanceof VPSetParameter) {
 	    VPSetParameter vpset = (VPSetParameter)p;
 	    for (int i=0;i<vpset.parameterSetCount();i++)
 		getUnresolvedInputTags(vpset.parameterSet(i),labels,unresolved,prefix+"::"+vpset.name());
 	}
     }
  
 
     /** add all Path entries to 'paths' array (recursively) */
     private void getPathsAmongEntries(Iterator<Reference> itEntry,
 				      ArrayList<Path> paths)
     {
 	while (itEntry.hasNext()) {
 	    Reference entry = itEntry.next();
 	    if (entry instanceof PathReference) {
 		PathReference ref  = (PathReference)entry;
 		Path          path = (Path)ref.parent();
 		paths.add(path);
 		getPathsAmongEntries(path.entryIterator(),paths);
 	    }
 	    else if (entry instanceof SequenceReference) {
 		Sequence          sequence = (Sequence)entry.parent();
 		getPathsAmongEntries(sequence.entryIterator(),paths);
 	    }
 	}
     }
 
     /** add all Sequence entries to 'sequences' array (recursively) */
     private void getSequencesAmongEntries(Iterator<Reference> itEntry,
 					  ArrayList<Sequence> sequences)
     {
 	while (itEntry.hasNext()) {
 	    Reference entry = itEntry.next();
 	    if (entry instanceof PathReference) {
 		PathReference ref  = (PathReference)entry;
 		Path          path = (Path)ref.parent();
 		getSequencesAmongEntries(path.entryIterator(),sequences);
 	    }
 	    else if (entry instanceof SequenceReference) {
 		Sequence          sequence = (Sequence)entry.parent();
 		sequences.add(sequence);
 		getSequencesAmongEntries(sequence.entryIterator(),sequences);
 	    }
 	}
     }
     
     /** add all Module entries to 'modules' array (recursively) */
     private void getModulesAmongEntries(Iterator<Reference> itEntry,
 					ArrayList<ModuleInstance> modules)
     {
 	while (itEntry.hasNext()) {
 	    Reference entry = itEntry.next();
 	    if (entry instanceof ModuleReference) {
 		ModuleReference ref    = (ModuleReference)entry;
 		ModuleInstance  module = (ModuleInstance)ref.parent();
 		modules.add(module);
 	    }
 	    else if (entry instanceof PathReference) {
 		PathReference ref  = (PathReference)entry;
 		Path          path = (Path)ref.parent();
 		getModulesAmongEntries(path.entryIterator(),modules);
 	    }
 	    else if (entry instanceof SequenceReference) {
 		Sequence          sequence = (Sequence)entry.parent();
 		getModulesAmongEntries(sequence.entryIterator(),modules);
 	    }
 	}
     }
 
 
     /** add all OutputModule entries to 'outputs' array (recursively) */
     private void getOutputsAmongEntries(Iterator<Reference> itEntry,
 					ArrayList<OutputModule> outputs)
     {
 	while (itEntry.hasNext()) {
 	    Reference entry = itEntry.next();
 	    if (entry instanceof OutputModuleReference) {
 		OutputModuleReference ref    = (OutputModuleReference)entry;
 		OutputModule          output = (OutputModule)ref.parent();
 		outputs.add(output);
 	    }
 	    else if (entry instanceof PathReference) {
 		PathReference ref  = (PathReference)entry;
 		Path          path = (Path)ref.parent();
 		getOutputsAmongEntries(path.entryIterator(),outputs);
 	    }
 	    else if (entry instanceof SequenceReference) {
 		Sequence          sequence = (Sequence)entry.parent();
 		getOutputsAmongEntries(sequence.entryIterator(),outputs);
 	    }
 	}
     }
 
 
     /** add all entries to 'references' array (recursively) */
     private void getReferences(Iterator<Reference> itEntry,
 			       ArrayList<Reference> references)
     {
 	while (itEntry.hasNext()) {
 	    Reference entry = itEntry.next();
 	    references.add(entry);
 	    if (entry instanceof PathReference) {
 		Path          path = (Path)entry.parent();
 		getReferences(path.entryIterator(),references);
 	    }
 	    else if (entry instanceof SequenceReference) {
 		Sequence sequence = (Sequence)entry.parent();
 		getReferences(sequence.entryIterator(),references);
 	    }
 	}
     }
     
 }
