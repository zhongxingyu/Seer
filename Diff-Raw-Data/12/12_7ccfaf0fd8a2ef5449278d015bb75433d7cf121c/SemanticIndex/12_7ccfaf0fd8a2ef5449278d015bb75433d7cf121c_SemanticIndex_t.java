 package org.spoofax.interpreter.library.language;
 
 import static org.spoofax.interpreter.core.Tools.isTermList;
 import static org.spoofax.terms.Term.termAt;
 import static org.spoofax.terms.Term.tryGetConstructor;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.spoofax.interpreter.library.IOAgent;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.spoofax.terms.TermFactory;
 import org.spoofax.terms.attachments.TermAttachmentSerializer;
 
 /**
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class SemanticIndex {
 	
 	private final Map<SemanticIndexEntry, SemanticIndexEntry> table =
 		new HashMap<SemanticIndexEntry, SemanticIndexEntry>();
 	
 	private final Map<SemanticIndexFile, SemanticIndexFile> files =
 		new HashMap<SemanticIndexFile, SemanticIndexFile>();
 	
 	private static final IStrategoConstructor FILE_ENTRIES_CON =
 			new TermFactory().makeConstructor("FileEntries", 2);
 
 	private IOAgent agent;
 
 	private ITermFactory termFactory;
 	
 	private SemanticIndexEntryFactory factory;
 	
 	private SemanticIndexEntry entryTemplate;
 	
 	public void initialize(ITermFactory factory, IOAgent agent) {
 		this.agent = agent;
 		this.factory = new SemanticIndexEntryFactory(factory);
 		this.termFactory = factory;
 		entryTemplate = new SemanticIndexEntry(
 			factory.makeConstructor("template", 0), factory.makeList(), factory.makeList(),
 			null, null, null);
 	}
 	
 	public void ensureInitialized() {
 		if (factory == null)
 			throw new IllegalStateException("Semantic index not initialized");
 	}
 	
 	public SemanticIndexEntryFactory getFactory() {
 		return factory;
 	}
 	
 	/**
 	 * Adds a new entry to the index.
 	 * 
 	 * @param file  The file to associate the entry with.
 	 *              Must be the result of {@linkplain #getFile()}.
 	 */
 	public void add(IStrategoAppl entry, SemanticIndexFile file) {
 		assert getFile(file) == file : "Files must be maximally shared: use getFile() to get a shared File reference";
 		ensureInitialized();
 		IStrategoTerm contentsType = factory.getEntryContentsType(entry);
 		IStrategoList id = factory.getEntryId(entry);
 		IStrategoTerm namespace = factory.getEntryNamespace(entry);
 		IStrategoTerm contents = factory.getEntryContents(entry);
 		SemanticIndexEntryParent parent = getEntryParentAbove(namespace, id, true);
 		add(factory.createEntry(entry.getConstructor(), namespace, id, contentsType, contents, parent, file), parent);
 	}
 	
 	public void addAll(IStrategoList entries, SemanticIndexFile file) {
 		while (!entries.isEmpty()) {
 			add((IStrategoAppl) entries.head(), file);
 			entries = entries.tail();
 		}
 	}
 	
 	public void add(SemanticIndexEntry entry) {
 		ensureInitialized();
 		add(entry, getEntryParentAbove(entry.getNamespace(), entry.getId(), true));
 	}
 	
 	private void add(SemanticIndexEntry entry, SemanticIndexEntryParent parent) {
 		if (parent != null)
 			parent.add(entry);
 		SemanticIndexEntry existing = table.get(entry);
 		if (existing == null) {
 			table.put(entry, entry);
 			if (entry.getFile() != null)
 				entry.getFile().addEntry(entry);
 		} else {
 			assert !entry.isParent() && existing != entry;
 			existing.getLast().setNext(entry);
 			if (entry.getFile() != null)
 				entry.getFile().addEntry(entry);
 		}
 		if (entry.getFile() != null)
 			entry.getFile().setTime(new Date());
 	}
 	
 	/**
 	 * Removes a single entry (not a list of entries).
 	 */
 	public void remove(SemanticIndexEntry entry) {
 		// Remove from table
 		SemanticIndexEntry head = table.get(entry);
 		if (head == null) {
 		    return;
 		} else if (head == entry) {
 			table.remove(entry);
 			if (entry.getNext() != null)
 				table.put(entry.getNext(), entry.getNext());
 		} else {
 			head.setNext(entry.getNext());
 		}
 		
 		// Remove from parent
 		SemanticIndexEntryParent parent = getEntryParentAbove(entry.getNamespace(), entry.getId(), false);
 		if (parent != null)
 			parent.remove(entry);
 			
 		// Remove from fileTable
 		SemanticIndexFile file = entry.getFile();
 		if (file != null) {
 			file.removeEntry(entry);
 			entry.getFile().setTime(new Date());
 		}
 	}
 	
 	/**
 	 * @param fileTerm  a string or (string, string) tuple with the filename
 	 *                  or the filename and subfilename
 	 */
 	public SemanticIndexFile getFile(IStrategoTerm fileTerm) {
 		return getFile(SemanticIndexFile.fromTerm(agent, fileTerm));
 	}
 	
 	/**
 	 * Gets a {@link SemanticIndexFile} from the index,
 	 * ensuring maximal sharing of index files.
 	 * Creates it if it didn't exist yet.
 	 */
 	private SemanticIndexFile getFile(SemanticIndexFile file) {
 		SemanticIndexFile result = files.get(file);
 		if (result == null) {
 			result = file;
 			files.put(file, file);
 		}
 		return result;
 	}
 	
 	/**
 	 * Returns an entry in the index that matches the given template.
 	 * Note that the result can have a 'tail' with other matching entries.
 	 */
 	public SemanticIndexEntry getEntries(IStrategoAppl template) {
 		ensureInitialized();
 		return getEntries(template.getConstructor(),
 				factory.getEntryNamespace(template),
 				factory.getEntryId(template),
 				factory.getEntryContentsType(template)
 				);
 	}
 	
 	/**
 	 * Returns an entry in the index that matches the given type and id.
 	 * Note that the result can have a 'tail' with other matching entries.
 	 */
 	private SemanticIndexEntry getEntries(IStrategoConstructor constructor, IStrategoTerm namespace, IStrategoList id, IStrategoTerm contentsType) {
 		entryTemplate.internalReinit(constructor, namespace, id, contentsType);
 		return table.get(entryTemplate);
 	}
 	
 	public IStrategoList getEntryChildTerms(IStrategoAppl template) {
 		ensureInitialized();
 		IStrategoConstructor constructor = template.getConstructor();
 		IStrategoTerm namespace = factory.getEntryNamespace(template);
 		SemanticIndexEntryParent parent = getEntryParentAt(namespace, factory.getEntryId(template));
 		if (parent == null)
 			return termFactory.makeList();
 		if (constructor == factory.getDefCon() && parent.getAllDefsCached() != null)
 			return parent.getAllDefsCached();
 		IStrategoList results = termFactory.makeList();
 		for (SemanticIndexEntry entry : parent.getChildren()) {
 			if (entry.getConstructor() == constructor) {
 				assert !entry.isParent();
 				assert entry.getNamespace().match(namespace);
 				results = termFactory.makeListCons(entry.toTerm(factory.getTermFactory()), results);
 			}
 		}
 		if (constructor == factory.getDefCon())
 			parent.setAllDefsCached(results);
 		return results;
 	}
 	
 	public IStrategoList getEntryDescendantTerms(IStrategoAppl template) {
 		ensureInitialized();
 		IStrategoConstructor constructor = template.getConstructor();
 		IStrategoTerm namespace = factory.getEntryNamespace(template);
 		SemanticIndexEntryParent parent = getEntryParentAt(namespace, factory.getEntryId(template));
 		return collectEntryDescendentTerms(parent, constructor, namespace, termFactory.makeList());
 	}
 	
 	private IStrategoList collectEntryDescendentTerms(SemanticIndexEntryParent parent, IStrategoConstructor constructor,
 			IStrategoTerm namespace, IStrategoList results) {
 		for (SemanticIndexEntry entry : parent.getChildren()) {
 			if (entry.getConstructor() == constructor) {
 				assert !entry.isParent();
 				assert entry.getNamespace().match(namespace);
 				results = termFactory.makeListCons(entry.toTerm(factory.getTermFactory()), results);
 			} else if (entry.isParent()) {
 				results = collectEntryDescendentTerms((SemanticIndexEntryParent) entry, constructor, namespace, results);
 			}
 		}
 		return results;
 	}
 	
 	private SemanticIndexEntryParent getEntryParentAbove(IStrategoTerm namespace, IStrategoList id, boolean createNonExistant) {
 		if (id.isEmpty()) {
 			return null;
 		} else {
 			id = id.tail();
 		}
 		SemanticIndexEntryParent result = getEntryParentAt(namespace, id);
 		if (result == null && createNonExistant) {
 			// add initial entry (that stores our time stamp)
 			result = factory.createEntryParent(namespace, id, getEntryParentAbove(namespace, id, true));
 			add(result); // add and recurse for parents
 		}
 		return result;
 	}
 	
 	/**
 	 * Gets the {@link SemanticIndexEntryParent} with the given identifier.
 	 */
 	private SemanticIndexEntryParent getEntryParentAt(IStrategoTerm namespace, IStrategoList id) {
 		return (SemanticIndexEntryParent) getEntries(SemanticIndexEntryParent.CONSTRUCTOR, namespace, id, null);
 	}
 	
 	public void clear() {
 		table.clear();
 		files.clear();
 	}
 	
 	public IStrategoTerm toTerm(boolean includePositions) {
 		ITermFactory terms = factory.getTermFactory();
 		IStrategoList results = terms.makeList();
 		for (SemanticIndexFile file : files.keySet()) {
 			IStrategoList fileResults = SemanticIndexEntry.toTerms(terms, file.getEntries());
 			// TODO: include time stamp for file
 			IStrategoTerm result = terms.makeAppl(FILE_ENTRIES_CON, file.toTerm(terms), fileResults);
 			results = terms.makeListCons(result, results);
 		}
 		
 		if (includePositions) {
 			// TODO: optimize -- store more compact attachments for positions
 			TermFactory simpleFactory = new TermFactory();
 			TermAttachmentSerializer serializer = new TermAttachmentSerializer(simpleFactory);
 			results = (IStrategoList) serializer.toAnnotations(results);
 		}
 		
 		return results;
 	}
 	
 	/**
 	 * Reads an index from a term.
 	 */
 	public static SemanticIndex fromTerm(IStrategoTerm term, ITermFactory factory, IOAgent agent, boolean extractPositions) throws IOException {
 		if (extractPositions) {
 			TermAttachmentSerializer serializer = new TermAttachmentSerializer(factory);
 			term = (IStrategoList) serializer.fromAnnotations(term, false);
 		}
 		
 		if (isTermList(term)) {
 			SemanticIndex result = new SemanticIndex();
 			result.initialize(factory, agent);
 			for (IStrategoList list = (IStrategoList) term; !list.isEmpty(); list = list.tail()) {
 				fromFileEntriesTerm(list.head(), result);
 			}
 			return result;
 		} else {
 			throw new IOException("Expected list of " + FILE_ENTRIES_CON.getName());
 		}
 	}
 	
 	private static void fromFileEntriesTerm(IStrategoTerm fileEntries, SemanticIndex result) throws IOException {
 		if (tryGetConstructor(fileEntries) == FILE_ENTRIES_CON) {
 			try {
				SemanticIndexFile file = result.getFile(termAt(fileEntries, 0));
 				result.addAll((IStrategoList) termAt(fileEntries, 1), file);
 			} catch (IllegalStateException e) {
 				throw new IllegalStateException(e);
 			} catch (RuntimeException e) { // HACK: catch all runtime exceptions
 				throw new IOException("Unexpected exception reading index: " + e);
 			}
 		} else {
 			throw new IOException("Illegal index entry: " + fileEntries);
 		}
 	}
 
 	public void clear(SemanticIndexFile file) {
 		assert files.get(file) == null || files.get(file) == file;
 		
 		List<SemanticIndexEntry> entries = file.getEntries();
 		if (entries.isEmpty()) return;
 		
 		SemanticIndexEntry[] copy = entries.toArray(new SemanticIndexEntry[entries.size()]);
 		for (SemanticIndexEntry entry : copy) {
 		    assert table.get(entry) != null;
 			remove(entry);
 		}
 		
 		assert file.getEntries().isEmpty();
 	}
 	
 	public Collection<SemanticIndexFile> getAllFiles() {
 		return files.values();
 	}
 	
 	@Override
 	public String toString() {
 		return files.keySet().toString();
 	}
 	
 	
 }
