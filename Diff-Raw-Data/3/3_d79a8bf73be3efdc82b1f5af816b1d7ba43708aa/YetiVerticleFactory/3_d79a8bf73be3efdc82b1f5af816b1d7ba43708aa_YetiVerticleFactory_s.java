 /*
  * Copyright 2012 Christian Essl
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  */
 
 package org.vertx.java.deploy.impl.yeti;
  
 
 import org.vertx.java.deploy.Verticle;
 import org.vertx.java.deploy.VerticleFactory;
 import org.vertx.java.deploy.impl.VerticleManager;
                               
 /**
  * @author Christian Essl
  */
 public class YetiVerticleFactory implements VerticleFactory {
 
   private VerticleManager mgr;
   
   public YetiVerticleFactory() {
 	  super();
   }
 
   @Override
   public void init(VerticleManager mgr) {
 	  this.mgr = mgr;
   }
 
  @Override
   public String getLanguage() {
 	  return "yeti";
   }
   
  @Override
   public boolean isFactoryFor(String main) {
     return main.endsWith(".yeti"); 
   }
   
  
   public Verticle createVerticle(String main, ClassLoader loader) throws Exception {
 
     //try to get the verticleFactory from loader
     
     VerticleFactory internal = null;
     try {
         internal = (VerticleFactory) (loader
             .loadClass("org.vertx.java.deploy.impl.yeti.YetiInternalVerticleFactory")
             .newInstance());        
     }catch(Exception ex) {
         throw new IllegalStateException("Could not instantiate "
             + "find org.vertx.java.deploy.impl.yeti.YetiInternalVerticleFactory "
             + "on verticles classpath. Please make sure it is included in "
             + "your modules lib or on the verticles -cp classpath reasion: "+ex);
     }
     
     return internal.createVerticle(main,loader);
   }
     
   public void reportException(Throwable t) {
     mgr.getLogger().error("Exception in Yeti verticle script", t);
   }
 }
