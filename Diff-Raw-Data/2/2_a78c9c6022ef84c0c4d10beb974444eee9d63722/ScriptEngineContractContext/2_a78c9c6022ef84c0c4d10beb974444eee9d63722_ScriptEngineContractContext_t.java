 /*
  * Copyright © 2012 Sebastian Hoß <mail@shoss.de>
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
  */
 package com.github.sebhoss.contract.verifier;
 
 import javax.inject.Inject;
 import javax.script.ScriptEngine;
 import javax.script.ScriptException;
 
 import com.github.sebhoss.contract.annotation.Clause;
 
 /**
  * {@link ScriptEngine}-based implementation of the {@link ContractContext}.
  */
 public final class ScriptEngineContractContext implements ContractContext {
 
     private final ScriptEngine engine;
 
     /**
      * @param engine
      *            The ScriptEngine to use.
      */
     @Inject
     public ScriptEngineContractContext(final ScriptEngine engine) {
         this.engine = engine;
     }
 
     @Override
     public void setInvocationResult(final Object invocationResult) {
         engine.put(Clause.RETURN, invocationResult);
     }
 
     @Override
     public boolean isInViolationWith(final Clause clause) {
         Object contractValidated;
 
         try {
             contractValidated = engine.eval(clause.value());
         } catch (final ScriptException exception) {
             throw new ContractContextException(exception);
         }
 
        return Boolean.FALSE.equals(contractValidated);
     }
 
 }
