 /*
  * Copyright © 2012 Sebastian Hoß <mail@shoss.de>
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
  */
 package com.github.sebhoss.contract.lifecycle;
 
 import com.github.sebhoss.common.annotation.CompilerWarnings;
 import com.github.sebhoss.contract.verifier.ContractVerifier;
 import com.github.sebhoss.contract.verifier.ContractVerifierFactory;
 import com.github.sebhoss.contract.verifier.ContractVerifierFactory.ContractVerifierBuilder;
 
 import org.aopalliance.intercept.MethodInvocation;
 
 /**
  * AOP {@link MethodInvocation}-based implementation of the {@link ContractLifecycle}.
  */
 public final class AopLifecycle extends ContractLifecycle {
 
     private final MethodInvocation invocation;
 
     /**
      * @param invocation
      *            The method invocation to use.
      * @param contractVerifierFactory
      *            The contract-verifier factory to use.
      */
     public AopLifecycle(final MethodInvocation invocation, final ContractVerifierFactory contractVerifierFactory) {
         super(contractVerifierFactory);
         this.invocation = invocation;
     }
 
     @Override
     @SuppressWarnings(CompilerWarnings.NULL)
     protected ContractVerifier createVerifier() {
        final ContractVerifierBuilder builder = getContractVerifierFactory().createContractVerifier();
 
         builder.method(invocation.getMethod());
         builder.instance(invocation.getThis());
         builder.arguments(invocation.getArguments());
 
         return builder.get();
     }
 
     @Override
     @SuppressWarnings(CompilerWarnings.NULL)
     protected Object executeMethod() throws Throwable {
         return invocation.proceed();
     }
 
 }
