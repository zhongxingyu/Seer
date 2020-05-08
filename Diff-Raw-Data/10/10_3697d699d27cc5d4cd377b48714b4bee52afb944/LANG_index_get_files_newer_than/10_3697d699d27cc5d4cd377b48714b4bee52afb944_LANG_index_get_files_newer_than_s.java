 package org.spoofax.interpreter.library.language;
 
 import java.util.Date;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 
 /**
  * Gets all files newer than (or equally old as) the specified file,
  * or gets all files if no file with the given name exists.
  * 
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class LANG_index_get_files_newer_than extends AbstractPrimitive {
 
 	private static String NAME = "LANG_index_get_files_newer_than";
 
 	private final SemanticIndexManager index;
 
 	public LANG_index_get_files_newer_than(SemanticIndexManager index) {
 		super(NAME, 0, 1);
 		this.index = index;
 	}
 
 	@Override
 	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
 		SemanticIndex ind = index.getCurrent();
 		SemanticIndexFile file = ind.getFile(tvars[0]);
 		if (file == null || file.getTime() == null) {
 			env.setCurrent(LANG_index_get_all_files.getAllFiles(
 					index.getCurrent(), env.getFactory()));
 		} else {
 			Date time = file.getTime();
 			env.setCurrent(getFilesAfter(env.getFactory(), ind, time));
 		}
 		return true;
 	}
 
 	private static IStrategoList getFilesAfter(ITermFactory factory, SemanticIndex ind, Date time) {
  		IStrategoList results = factory.makeList();
 		for (SemanticIndexFile file : ind.getAllFiles()) {
			if (!file.getTime().before(time)) {
 				results = factory.makeListCons(file.toTerm(factory), results);
 			}
 		}
 		return results;
 	}
 }
