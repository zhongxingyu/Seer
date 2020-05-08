 /*
  * Created on 08.aug.2005
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
  * 
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter.library.ssl;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.Tools;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.library.IOAgent;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class SSL_printnl extends AbstractPrimitive {
 
     protected SSL_printnl() {
         super("SSL_printnl", 0, 2);
     }
 
     @Override
     public boolean call(IContext env, Strategy[] sargs, IStrategoTerm[] targs) throws InterpreterException {
 
        final String output = targs[0].toString();
         IStrategoList l = (IStrategoList) targs[1]; 
 
         StringBuilder sb = new StringBuilder();
         for(int i = 0; i < l.size(); i++) {
             IStrategoTerm t = Tools.termAt(l, i);
 //            if (Tools.isTermAppl(t)) {
 //                IStrategoAppl a = (IStrategoAppl)t;
 //                if (Tools.isCons(a, env))
 //                    break;
 //                    //sb.append(Tools.consToListDeep(env, a));
 //                else if (Tools.isTermString(t))
 //                    sb.append(Tools.javaString(t));
 //                continue;
 //            }
             sb.append(t);
         }
         sb.append("\n");
 
         SSLLibrary or = (SSLLibrary) env.getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
         IOAgent agent = or.getIOAgent();
         
         try {
             if(output.equals("stderr")) {
                 Writer out = agent.getWriter(IOAgent.CONST_STDERR);
                 out.write(sb.toString());
             } else if(output.equals("stdout")) {
                 Writer out = agent.getWriter(IOAgent.CONST_STDOUT);
                 out.write(sb.toString());
             } else {
                 throw new InterpreterException("Unknown output : " + output);
             }
         } catch (IOException e) {
             or.getIOAgent().printError("SSL_println: could not print line (" + e.getMessage() + ")");
         }
         
         return true;
     }
 }
