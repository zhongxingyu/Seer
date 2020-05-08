 /*
  * Created on 08.aug.2005
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.library.ssl;
 
 import java.util.List;
 
 import org.spoofax.interpreter.IConstruct;
 import org.spoofax.interpreter.IContext;
 import org.spoofax.interpreter.InterpreterException;
 import org.spoofax.interpreter.Tools;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.library.ssl.SSL_indexedSet_create.IndexedSet;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class SSL_indexedSet_remove extends AbstractPrimitive {
 
     protected SSL_indexedSet_remove() {
         super("SSL_indexedSet_remove", 0, 2);
     }
 
     public boolean call(IContext env, List<IConstruct> sargs, IStrategoTerm[] targs)
             throws InterpreterException {
 
         if (!(Tools.isTermInt(targs[0])))
             return false;
 
         IndexedSet is = SSLLibrary.instance(env).getIndexedSet(Tools.asJavaInt(targs[0]));
         
         if(is == null)
             return false;
 
        boolean r = is.remove(targs[1]);
         
         env.setCurrent(targs[0]);
        return r;
     }
 }
