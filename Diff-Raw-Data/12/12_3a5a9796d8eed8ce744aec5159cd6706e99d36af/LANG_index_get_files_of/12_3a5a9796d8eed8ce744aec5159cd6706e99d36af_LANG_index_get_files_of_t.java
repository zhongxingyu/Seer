 package org.spoofax.interpreter.library.language;
 
 import static org.spoofax.interpreter.core.Tools.isTermAppl;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 /**
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class LANG_index_get_files_of extends AbstractPrimitive {
 
 	private static String NAME = "LANG_index_get_file_of";
 	
 	private final SemanticIndexManager index;
 	
 	public LANG_index_get_files_of(SemanticIndexManager index) {
 		super(NAME, 0, 1);
 		this.index = index;
 	}
 
 	/**
 	 * Returns [] if URI not in index.
 	 */
 	@Override
 	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
 		if (isTermAppl(tvars[0])) {
 			IStrategoAppl template = (IStrategoAppl) tvars[0];
 
 			IStrategoList results = env.getFactory().makeList();
			SemanticIndexEntry entry;
			for (entry = index.getCurrent().getEntries(template); entry != null; entry = entry.getNext()) {
 				String file = index.getCurrent().fromFileURI(entry.getFile().getURI());
 				IStrategoTerm result = env.getFactory().makeString(file);
 				results = env.getFactory().makeListCons(result, results);
 			}
 			
 			env.setCurrent(results);
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
