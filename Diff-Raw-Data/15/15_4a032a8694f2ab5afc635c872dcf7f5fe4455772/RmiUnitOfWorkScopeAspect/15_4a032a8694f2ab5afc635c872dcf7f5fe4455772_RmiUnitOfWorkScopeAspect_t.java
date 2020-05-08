 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.rmi;
 
 import org.aspectj.lang.annotation.Aspect;
 
 import de.cosmocode.palava.scope.AbstractUnitOfWorkScopeAspect;
 
 /**
  * An {@link Aspect} which manages {@link de.cosmocode.palava.scope.UnitOfWorkScope}
  * behaviour for remote method invocations.
  *
  * @since 1.3
  * @author Willi Schoenborn
  */
 @Aspect
abstract class RmiUnitOfWorkScopeAspect extends AbstractUnitOfWorkScopeAspect {
 
//    @Override
//    @Pointcut("execution(public * java.rmi.Remote+.*(..)) && !within(de.cosmocode.palava.rmi..*)")
//    protected void unitOfWork() {
//        
//    }
 
 }
 
