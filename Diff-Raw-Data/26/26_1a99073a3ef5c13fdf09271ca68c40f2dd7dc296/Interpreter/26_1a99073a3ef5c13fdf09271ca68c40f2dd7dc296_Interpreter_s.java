 /*
  * Created on 07.aug.2005
  *
  * Copyright (c) 2005-2011, Karl Trygve Kalleberg <karltk near strategoxt dot org>
  *
  * Licensed under the GNU Lesser Lesser General Public License, v2.1.1
  */
 package org.spoofax.interpreter.core;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.spoofax.interpreter.library.IOAgent;
 import org.spoofax.interpreter.library.IOperatorRegistry;
 import org.spoofax.interpreter.library.spx.SPXInterpreterLibrary;
 import org.spoofax.interpreter.library.ssl.SSLLibrary;
 import org.spoofax.interpreter.stratego.SDefT;
 import org.spoofax.interpreter.stratego.StupidFormatter;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.spoofax.terms.TermFactory;
 import org.spoofax.terms.io.binary.TermReader;
 
 /**
  * A Stratego interpreter.
  *
  * @see org.strategoxt.HybridInterpreter   For an efficient hybrid compiler/interpreter.
  *
  * @author Karl Trygve Kalleberg <karltk near strategoxt dot org>
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class Interpreter {
 
     protected final Context context;
     protected final StrategoCoreLoader loader;
 
     public Interpreter() {
         this(new TermFactory());
     }
 
     public Interpreter(ITermFactory factory) {
         this(factory, factory);
     }
 
     public Interpreter(ITermFactory termFactory, ITermFactory programFactory) {
         Context.indentation = 0;
         context = createContext(termFactory, programFactory);
 
         loader = new StrategoCoreLoader(context);
 
        addOperatorRegistry(new SPXInterpreterLibrary());
     }
 
     /**
      * Invokes a strategy.
      *
      * @param name
      *            The name of the strategy to invoke, using its regular
      *            Stratego-based name (e.g., "strategy-name").
      *            If this fails, the strategy is looked up using C-based
      *            naming conventions (e.g., strategy_name_0_0").
      *
      * @throws InterpreterExit
      *             If the interpreter is exited.
      * @throws InterpreterException
      *             In case of an internal error or other interpreter exception.
      *
      * @see #setCurrent(IStrategoTerm)
      *             Sets the input term for the invoked strategy.
      */
     public boolean invoke(String name)
             throws InterpreterErrorExit, InterpreterExit, UndefinedStrategyException, InterpreterException {
 
         SDefT def = lookupUncifiedSVar(name);
 
         if (def == null) {
             throw new UndefinedStrategyException("Definition '" + name + "' not found", name);
         }
 
         return evaluate(def);
     }
 
     /**
      * Evaluates a stratego expression. Must be fully desugared,
      * using C names in SDefTs and SCallTs.
      *
      * @see org.strategoxt.HybridInterpreter#evaluate  A variant of evaluate() with desugaring support.
      */
     public boolean evaluate(IStrategoAppl s)
             throws InterpreterErrorExit, InterpreterExit, UndefinedStrategyException, InterpreterException {
 
         final ITermFactory factory = getFactory();
         final IStrategoConstructor sdefT = factory.makeConstructor("SDefT", 4);
 
         if (s.getConstructor() != sdefT)
             s = factory.makeAppl(sdefT, factory.makeString("interpreter_evaluate_dummy_0_0"), factory.makeList(), factory.makeList(), s);
 
         SDefT def = loader.parseSDefT(s);
         return evaluate(def);
     }
 
     private boolean evaluate(SDefT def) throws InterpreterException {
         StackTracer stackTracer = getContext().getStackTracer();
         stackTracer.push(def.getName());
 
         try {
             boolean success = def.getBody().evaluate(context);
 
             if (success) stackTracer.popOnSuccess();
             else stackTracer.popOnFailure();
 
             return success;
         } catch (InterpreterException e) {
             stackTracer.popOnExit(false);
             throw new InterpreterException("Exception during evaluation", e);
         } catch (RuntimeException e) {
             stackTracer.popOnExit(false);
             throw new InterpreterException("Exception during evaluation", e);
         }
     }
 
     public SDefT lookupUncifiedSVar(String name) {
         try {
             SDefT def = context.lookupSVar(cify(name) + "_0_0");
 
             if (def == null) {
                 def = context.lookupSVar(name);
             }
             return def;
         } catch (InterpreterException e) {
             return null;
         }
     }
 
     /**
      * Rewrite a strategy name based on the C naming conventions,
      * following the stratego-lib term/string/cify strategy.
      */
     public static String cify(String input) {
         return input
             .replace("_", "__")
             .replace("-", "_")
             .replace("'", "_p_")
             .replace("\"", "_q_")
             .replace("\\", "_b_");
     }
 
     public IContext getContext() {
         return context;
     }
 
     protected Context createContext(ITermFactory termFactory, ITermFactory programFactory) {
        return new Context(termFactory, programFactory);
     }
 
 
     public void setCurrent(IStrategoTerm inp) {
         context.setCurrent(inp);
     }
 
     public IStrategoTerm current() {
         return context.current();
     }
 
     public IOAgent getIOAgent() {
         SSLLibrary op = (SSLLibrary) getContext().getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
         return op.getIOAgent();
     }
 
     public void setIOAgent(IOAgent ioAgent) {
         SSLLibrary op = (SSLLibrary) getContext().getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
         op.setIOAgent(ioAgent);
         getContext().getStackTracer().setIOAgent(ioAgent);
     }
 
     /**
      * Resets the state of this interpreter.
      */
     public void reset() {
         // TODO: better way to reset the interpreter state?
         //       the comment at SSLLibrary.init() talks about scoping this?
         SSLLibrary op = (SSLLibrary) getContext().getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
         op.init();
     }
 
     public String prettyPrint() throws InterpreterException {
         StupidFormatter sf = new StupidFormatter();
         SDefT s = context.lookupSVar("main_0_0");
         s.prettyPrint(sf);
         return sf.toString();
     }
 
     public void shutdown() {
         //todo perf: this takes 2 secs overall
         //if(DebugUtil.cleanupInShutdown) {
         //    context.cleanup();
         //}
     }
 
     public ITermFactory getFactory() {
         return context.getFactory();
     }
 
     public ITermFactory getProgramFactory() {
         return context.getProgramFactory();
     }
 
     public final void load(InputStream stream) throws IOException, InterpreterException {
         if (stream == null)
             throw new IOException("Could not load Stratego core input from null stream");
 
        load(new TermReader(context.getProgramFactory()).parseFromStream(stream));
     }
 
     public final void load(String file) throws IOException, InterpreterException {
         load(new TermReader(context.getProgramFactory()).parseFromFile(file));
     }
 
 	public void load(IStrategoTerm term) throws InterpreterException {
 		loader.load(term);
 	}
 
    @Deprecated
    public void addOperatorRegistry(String domainName, IOperatorRegistry or) {
        context.addOperatorRegistry(domainName, or);
    }

     public void addOperatorRegistry(IOperatorRegistry or) {
         context.addOperatorRegistry(or);
     }
 }
