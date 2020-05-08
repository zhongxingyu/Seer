 /*
  * Copyright (c) 2009, Julian Gosnell
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.dada.ltw;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Aspect
 public class RelationalAspect {
 
	public static final Map<String, Map<Integer, Integer>> model = new HashMap<String, Map<Integer,Integer>>();
 
 	protected Map<Integer, Integer> ensureTable(String fieldName) {
 		Map<Integer, Integer> table = model.get(fieldName);
 		if (table==null) {
 			model.put(fieldName, table = new HashMap<Integer, Integer>());
 		}
 		return table;
 	}
 
     private static final Logger LOG = LoggerFactory.getLogger(RelationalAspect.class);
 
 	@Around("getterPointcut()")
     public Object getterAdvice(ProceedingJoinPoint pjp) throws Throwable {
 		String fieldName = fieldName(pjp);
 		//Map<Integer, Integer> table = ensureTable(fieldName);
 		Identifiable target = (Identifiable)pjp.getTarget();
 		Identifiable value = (Identifiable)pjp.proceed();
 		LOG.info("get: " + target + "." + fieldName + " = " + value);
 		return value;
     }
 
 	private String fieldName(ProceedingJoinPoint pjp) {
 		return pjp.getSignature().getName().substring(3);
 	}
 
 	@Around("setterPointcut()")
     public Object setterAdvice(ProceedingJoinPoint pjp) throws Throwable {
 		Identifiable target = (Identifiable)pjp.getTarget();
 		Identifiable value = (Identifiable)pjp.getArgs()[0];
         try {
     		String fieldName = fieldName(pjp);
     		Map<Integer, Integer> table = ensureTable(fieldName);
 			table.put(target.getId(), value.getId());
             return pjp.proceed();
         } finally {
             LOG.info("set: " + target + "." + fieldName(pjp)+" = "+value);
         }
     }
 
     @Pointcut("execution(public org.dada.ltw.Identifiable+ org.dada.ltw.Identifiable+.get*())")
     public void getterPointcut() {}
 
     @Pointcut("execution(public void org.dada.ltw.Identifiable+.set*(org.dada.ltw.Identifiable+))")
     public void setterPointcut() {}
 
 }
