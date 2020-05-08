 /*
  * Copyright © 2012 Sebastian Hoß <mail@shoss.de>
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
  */
 package com.github.sebhoss.contract.lifecycle;
 
 import com.github.sebhoss.common.annotation.CompilerWarnings;
 import com.github.sebhoss.contract.annotation.Contract;
 import com.github.sebhoss.contract.verifier.ContractVerifier;
 import com.github.sebhoss.contract.verifier.ContractVerifierFactory;
 import com.github.sebhoss.contract.verifier.ContractVerifierFactory.ContractVerifierBuilder;
 
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.reflect.MethodSignature;
 
 /**
  * Spring AspectJ-based implementation of the {@link ContractLifecycle}.
  */
 public final class SpringContractLifecycle extends ContractLifecycle {
 
     private final ProceedingJoinPoint pjp;
     private final Contract            contract;
 
     /**
      * @param pjp
      *            The AspectJ join point to use.
      * @param contract
      *            The contract to verify.
      * @param contractVerifierFactory
      *            The contract verifier factory to use.
      */
     public SpringContractLifecycle(final ProceedingJoinPoint pjp, final Contract contract,
             final ContractVerifierFactory contractVerifierFactory) {
         super(contractVerifierFactory);
         this.pjp = pjp;
         this.contract = contract;
     }
 
     @Override
     @SuppressWarnings(CompilerWarnings.NULL)
     protected ContractVerifier createVerifier() {
        final ContractVerifierBuilder builder = contractVerifierFactory.createContractVerifier();
         final MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
 
         builder.method(methodSignature.getMethod());
         final String[] parameterNames = methodSignature.getParameterNames();
         if (parameterNames != null) {
             builder.parameterNames(parameterNames);
         }
         builder.instance(pjp.getThis());
         builder.arguments(pjp.getArgs());
         builder.contract(contract);
 
         return builder.get();
     }
 
     @Override
     @SuppressWarnings(CompilerWarnings.NULL)
     protected Object executeMethod() throws Throwable {
         return pjp.proceed();
     }
 
 }
