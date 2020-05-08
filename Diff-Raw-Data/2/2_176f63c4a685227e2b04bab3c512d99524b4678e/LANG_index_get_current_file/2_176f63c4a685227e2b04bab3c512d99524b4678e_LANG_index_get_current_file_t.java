 package org.spoofax.interpreter.library.language;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class LANG_index_get_current_file extends AbstractPrimitive {
 
	private static String NAME = "LANG_index_get_current_file";
 	
 	private final SemanticIndexManager index;
 	
 	public LANG_index_get_current_file(SemanticIndexManager index) {
 		super(NAME, 0, 0);
 		this.index = index;
 	}
 
 	@Override
 	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
 		env.setCurrent(index.getCurrentFile().toTerm(env.getFactory()));
 		return true;
 	}
 }
