 package org.spoofax.interpreter.library.language;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.spoofax.terms.TermFactory;
 
 /**
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class SemanticIndexEntryParent extends SemanticIndexEntry {
 
 	public static final IStrategoConstructor CONSTRUCTOR =
 		new TermFactory().makeConstructor("<parent>", 1);
 
 	private final List<SemanticIndexEntry> children =
 		new ArrayList<SemanticIndexEntry>();
 	
 	private transient IStrategoList allDefsCached;
 	
 	protected SemanticIndexEntryParent(IStrategoTerm namespace, IStrategoList id) {
 		super(CONSTRUCTOR, namespace, id, null, null, null);
 	}
 	
 	@Override
 	public IStrategoAppl toTerm(ITermFactory factory) {
 		return null;
 	}
 	
 	public void add(SemanticIndexEntry entry) {
 		allDefsCached = null;
 		children.add(entry);
 	}
 	
 	public void remove(SemanticIndexEntry entry) {
 		allDefsCached = null;
		for (int i = 0; i < children.size(); i++) {
 			if (children.get(i) == entry)
 				children.remove(i--);
 		}
 	}
 	
 	public boolean isEmpty() {
 		return children.isEmpty();
 	}
 	
 	public List<SemanticIndexEntry> getChildren() {
 		return Collections.unmodifiableList(children);
 	}
 	
 	public IStrategoList getAllDefsCached() {
 		return allDefsCached;
 	}
 	
 	public void setAllDefsCached(IStrategoList allDefsCached) {
 		this.allDefsCached = allDefsCached;
 	}
 }
