 /*
  * Copyright © 2012 Sebastian Hoß <mail@shoss.de>
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
  */
 package com.github.sebhoss.contract.lifecycle;
 
 import com.github.sebhoss.contract.verifier.ContractVerifier;
 import com.github.sebhoss.contract.verifier.ContractVerifierFactory;
 
 /**
  * Performs the contract validation lifecycle. Contains the following phases:
  * <ul>
  * <li>Create Verifier</li>
  * <li>Precondition Check</li>
  * <li>Method execution</li>
  * <li>Postcondition Check</li>
  * <li>Return method execution result</li>
  * </ul>
  */
 public abstract class ContractLifecycle {
 
    private final ContractVerifierFactory contractVerifierFactory;
 
     protected ContractLifecycle(final ContractVerifierFactory contractVerifierFactory) {
         this.contractVerifierFactory = contractVerifierFactory;
     }
 
     /**
      * @return The return value of the encapsulated method call.
      * @throws Throwable
      *             In case the method executed throws.
      */
     public final Object performLifecycle() throws Throwable {
         final ContractVerifier contractVerifier = createVerifier();
 
         if (contractVerifier.hasPreconditions()) {
             contractVerifier.verifyPreconditions();
         }
 
         final Object result = executeMethod();
 
         if (contractVerifier.hasPostconditions()) {
             contractVerifier.verifyPostconditions(result);
         }
 
         return result;
     }
 
     protected abstract ContractVerifier createVerifier();
 
     protected abstract Object executeMethod() throws Throwable;
 
    protected ContractVerifierFactory getContractVerifierFactory() {
        return contractVerifierFactory;
    }

 }
