 /*
  * Copyright 2011 VMWare.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package demo.vmware.commands;
 
 import org.apache.log4j.Logger;
 import org.springframework.cache.annotation.Cacheable;
 
 /**
 * Implementaton of the interface. It will be coughed up by spring as a proxy if we have the cachable scanner running
  * 
  * @author freemanj
  * 
  */
 public class CommandPowerOfTwoHelper implements ICommandPowerOfTwoHelper {
 
     public static final Logger LOG = Logger.getLogger(CommandPowerOfTwoHelper.class);
 
     /** invocation count used by command to see if we got a cache hit */
     private int hitCount = 0;
 
     /*
      * (non-Javadoc)
      * 
      * @see demo.vmware.commands.ICommandPowerOfTWo#calculateNthPower(java.lang.Integer)
      */
     @Override
     @Cacheable("POWERTABLE")
     public Integer calculateNthPower(Integer targetNumber) {
         LOG.info("Not cached so calculating 2^" + targetNumber);
         hitCount++;
         Double result = Math.pow(2.0, targetNumber);
         return new Integer(result.intValue());
     }
 
     /**
      * returns the invocation count.
      * 
      * @return
      */
     @Override
     public int getHitCount() {
         return hitCount;
     }
 
 }
