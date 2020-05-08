 /*
  * Created on 11. jan.. 2007
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.library.ssl;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.Tools;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class SSL_read_term_from_stream extends AbstractPrimitive {
 
     SSL_read_term_from_stream() {
         super("SSL_read_term_from_stream", 0, 1);
     }
     
     @Override
     public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
             throws InterpreterException {
         
         if(!Tools.isTermInt(tvars[0]))
             return false;
         
         // TODO: optimize - use memory-mapped I/O for reading terms?
         //       PushBackInputStream.read() is very inefficient;
         //       why not create our own implementation based on memory-mapped I/O?
         //
         // UNDONE: tricky: detecting if it's a BAF term in TermFactory...
         //
         // FileChannel channel = or.getIOAgent().getInputChannel(Tools.asJavaInt(tvars[0]));
         // ChannelPushbackInputStream reader = new ChannelPushbackInputStream(channel);
         SSLLibrary or = (SSLLibrary) env.getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
         InputStream is = or.getIOAgent().internalGetInputStream(Tools.asJavaInt(tvars[0]));
         if(is == null)
             return false;
 
         try {
             env.setCurrent(env.getFactory().parseFromStream(is));
         } catch(IOException e) {
             or.getIOAgent().printError("SSL_read_term_from_stream: " + e.getMessage());
             return false;
         }
         return true;
     }
 
 }
