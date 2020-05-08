 package org.rubypeople.rdt.internal.core.search;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.rubypeople.rdt.core.ElementChangedEvent;
 import org.rubypeople.rdt.core.IElementChangedListener;
 import org.rubypeople.rdt.core.IParent;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyElementDelta;
 import org.rubypeople.rdt.core.IRubyModel;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.core.ISourceFolderRoot;
 import org.rubypeople.rdt.core.IType;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.internal.core.Openable;
 import org.rubypeople.rdt.internal.core.RubyModelManager;
 
 public class ExperimentalIndex implements IElementChangedListener {
 
 	private static ExperimentalIndex fgInstance;
 	private static Map<IPath, SearchDocument> documents;
 	private static HandleFactory factory = new HandleFactory();
 
 	private ExperimentalIndex() {
 		documents = new HashMap<IPath, SearchDocument>();
 	}
 
 	public void elementChanged(ElementChangedEvent event) {
 		processDelta(event.getDelta());
 	}
 	
	// FIXME We're ding poor man's scoping by passing in the script. We should actually create scope classes which could tell if a document fell in our out of it...
 	public static Set<String> getTypeNames(IRubyScript script) {
 		return getElementNames(IRubyElement.TYPE, script);
 	}
 
 	public static Set<String> getConstantNames(IRubyScript script) {
 		return getElementNames(IRubyElement.CONSTANT, script);
 	}
 
 	private static Set<String> getElementNames(int type, IRubyScript script) {
 		Set<String> names = new HashSet<String>();
 		Collection<SearchDocument> documents = getDocumentsInScope(script);
 		for (SearchDocument doc : documents) {
 			Set<String> elements = doc.getElementNamesOfType(type);
 			for (String element : elements) {
 				names.add(element);
 			}
 		}
 		return names;
 	}
 
 	private static Collection<SearchDocument> getDocumentsInScope(IRubyScript script) {
 		try {
 			Set<SearchDocument> matches = new HashSet<SearchDocument>();
 			IRubyProject project = script.getRubyProject();
 			ISourceFolderRoot[] roots = project.getSourceFolderRoots();
 			for (IPath path : documents.keySet()) {
 				// If path is in loadpath of script's project, add it
 				for (int i = 0; i < roots.length; i++) {
 					if (roots[i].getPath().isPrefixOf(path)) matches.add(documents.get(path));
 				}
 			}
 			return matches;
 		} catch (RubyModelException e) {
 			// ignore?
 			return documents.values();
 		}
 	}
 
 	public static Set<IType> findType(String name) {
 		Set<IType> types = new HashSet<IType>();
 		for (SearchDocument doc : documents.values()) {
 			IType type = doc.findType(name);
 			if (type != null)
 				types.add(type);
 		}
 		return types;
 	}
 
 	public static Set<String> getGlobalNames(IRubyScript script) {
 		return getElementNames(IRubyElement.GLOBAL, script);
 	}
 
 	private void processDelta(IRubyElementDelta delta) {
 		IRubyElement element = delta.getElement();
 		switch (delta.getKind()) {
 		case IRubyElementDelta.CHANGED:
 			IRubyElementDelta[] children = delta.getAffectedChildren();
 			for (int i = 0, length = children.length; i < length; i++) {
 				IRubyElementDelta child = children[i];
 				this.processDelta(child);
 			}
 			break;
 		case IRubyElementDelta.REMOVED:
 			removeElement(element);
 			break;
 		case IRubyElementDelta.ADDED:
 			addElement(element);
 			break;
 		}
 	}
 
 	void removeElement(IRubyElement element) {
 		if ((element.isType(IRubyElement.RUBY_MODEL)) || 
 				(element.isType(IRubyElement.RUBY_PROJECT)) ||
 				(element.isType(IRubyElement.SCRIPT)) ||
 				(element.isType(IRubyElement.SOURCE_FOLDER_ROOT)) ||
 				(element.isType(IRubyElement.SOURCE_FOLDER))) return;
 		SearchDocument doc = documents.get(element.getPath());
 		if (doc == null)
 			return;
 		doc.removeElement(element);
 		if (doc.isEmpty())
 			documents.remove(element.getPath());
 	}
 
 	void addElement(IRubyElement element) {
 		if ((element.isType(IRubyElement.RUBY_MODEL)) || 
 			(element.isType(IRubyElement.RUBY_PROJECT)) ||
 			(element.isType(IRubyElement.SCRIPT)) ||
 			(element.isType(IRubyElement.SOURCE_FOLDER_ROOT)) ||
 			(element.isType(IRubyElement.SOURCE_FOLDER))) return;
 		SearchDocument doc = documents.get(element.getPath());
 		if (doc == null) {
 			doc = new SearchDocument(element.getPath());
 			documents.put(element.getPath(), doc);
 		}
 		doc.addElement(element);
 	}
 
 	public static ExperimentalIndex instance() {
 		if (fgInstance == null) {
 			fgInstance = new ExperimentalIndex();
 		}
 		return fgInstance;
 	}
 
 	public static void start() {
 		Job job = new ExperimentalIndexJob(instance());
 		job.schedule();
 	}
 
 	private static class ExperimentalIndexJob extends Job {
 		private ExperimentalIndex index;
 
 		public ExperimentalIndexJob(ExperimentalIndex index) {
 			super("Search Index Job");
 			this.index = index;
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			// TODO Load up saved data if there is any, rather than starting
 			// over
 			// TODO Clear saved state if user cleans a project
 			// TODO Save state after a run
 			IRubyModel model = RubyModelManager.getRubyModelManager().getRubyModel();
 			addChildren(model);
 			return Status.OK_STATUS;
 		}
 
 		private void addChildren(IParent parent) {
 			try {
 				IRubyElement[] children = parent.getChildren();
 				for (int i = 0; i < children.length; i++) {
 					index.addElement(children[i]);
 					if (children[i] instanceof IParent) {
 						IParent newParent = (IParent) children[i];
 						addChildren(newParent);
 					}
 				}
 			} catch (RubyModelException e) {
 				RubyCore.log(e);
 			}
 		}
 
 	}
 
 	private class SearchDocument {
 		private static final String SEPARATOR = "/";
 		private List<String> indices = new ArrayList<String>();
 		private IPath path;
 		private IRubyScript script;
 
 		SearchDocument(IPath path) {
 			this.path = path;
 		}
 
 		public Set<String> getElementNamesOfType(int type) {
 			Set<String> names = new HashSet<String>();
 			for (String indexKey : indices) {
 				if (getTypeFromKey(indexKey) != type) continue;
 				names.add(getNameFromKey(indexKey));
 			}
 			return names;
 		}
 
 		public List<IRubyElement> getElementsOfType(int type) {
 			IRubyScript script = getScript();
 			return getChildrenOfType(script, type);
 		}
 
 		private IRubyScript getScript() {
 			if (this.script == null) {
 				Openable openable = factory.createOpenable(path.toString());
 				this.script = (IRubyScript) openable;
 			}
 			return this.script;
 		}	
 
 		private List<IRubyElement> getChildrenOfType(IParent parent, int type) {
 			List<IRubyElement> elements = new ArrayList<IRubyElement>();
 			if (parent == null) return elements;
 			try {
 				IRubyElement[] children = parent.getChildren();
 				if (children == null)
 					return elements;
 				for (int i = 0; i < children.length; i++) {
 					if (children[i].isType(type))
 						elements.add(children[i]);
 					if (children[i] instanceof IParent) {
 						IParent childParent = (IParent) children[i];
 						elements.addAll(getChildrenOfType(childParent, type));
 					}
 				}
 			} catch (RubyModelException e) {
 				// ignore
 			}
 			return elements;
 		}
 
 		public boolean isEmpty() {
 			return indices.isEmpty();
 		}
 
 		public void removeElement(IRubyElement element) {
 			indices.remove(createKey(element));
 		}
 
 		private String createKey(IRubyElement element) {
 			return createKey(element.getElementType(), element.getElementName());
 		}
 
 		private String createKey(int type, String name) {
 			return type + SEPARATOR + name;
 		}
 
 		public void addElement(IRubyElement element) {
 			indices.add(createKey(element));
 		}
 
 		public IType findType(String name) {
 			return (IType) findElement(createKey(IRubyElement.TYPE, name));
 		}
 
 		private IRubyElement findElement(String key) {
 			for (String indexKey : indices) {
 				if (!indexKey.equals(key))
 					continue;
 				IRubyScript script = getScript();
 				List<IRubyElement> children = getChildrenOfType(script, getTypeFromKey(key));
 				for (IRubyElement element : children) {
 					if (element.getElementName().equals(getNameFromKey(key)))
 						return element;
 				}
 			}
 			return null;
 		}
 
 		private String getNameFromKey(String key) {
 			String[] parts = key.split(SEPARATOR);
 			return parts[1];
 		}
 
 		private int getTypeFromKey(String key) {
 			String[] parts = key.split(SEPARATOR);
 			return Integer.parseInt(parts[0]);
 		}
 	}
 }
