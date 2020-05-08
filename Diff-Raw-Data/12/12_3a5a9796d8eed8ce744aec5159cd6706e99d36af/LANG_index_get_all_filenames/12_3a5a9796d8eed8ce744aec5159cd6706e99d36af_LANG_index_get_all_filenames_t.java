 package org.spoofax.interpreter.library.language;
 
 import java.util.Collection;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 
 /**
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class LANG_index_get_all_filenames extends AbstractPrimitive {
 
 	private static String NAME = "LANG_index_all_filenames";
 	
 	private final SemanticIndexManager index;
 	
 	public LANG_index_get_all_filenames(SemanticIndexManager index) {
		super(NAME, 0, 0);
 		this.index = index;
 	}
 
 	@Override
 	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
 		Collection<SemanticIndexFile> allFiles = index.getCurrent().getAllFiles();
 		ITermFactory factory = env.getFactory();
 		IStrategoList results = factory.makeList();
 		for (SemanticIndexFile file : allFiles) {
 			String uri = index.getCurrent().fromFileURI(file.getURI());
 			IStrategoString result = factory.makeString(uri);
 			results = factory.makeListCons(result, results);
 		}
 		env.setCurrent(results);
 		return true;
 	}
 }
