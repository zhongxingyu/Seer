 /*
  * Created on 08.aug.2005
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.library.ssl;
 
 import java.io.PrintStream;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.Tools;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class SSL_fputs extends AbstractPrimitive {
 
     protected SSL_fputs() {
         super("SSL_fputs", 0, 2);
     }
     
     @Override
     public boolean call(IContext env, Strategy[] sargs, IStrategoTerm[] targs) throws InterpreterException {
         
         if(!Tools.isTermString(targs[0]))
             return false;
         if(!(Tools.isTermInt(targs[1])))
             return false;
 
         SSLLibrary or = (SSLLibrary) env.getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
         
        // TODO: Don't use a printstream because of its quiet failing behavior
         PrintStream ous = or.getIOAgent().getOutputStream(Tools.asJavaInt(targs[1]));
         ous.print(Tools.javaString(targs[0]));
        // if (ous.checkError()) return false; // UNDONE: quietly flushes the stream!
         
         env.setCurrent(targs[1]);
         
         return true;
     }
 }
