 /*
  * Copyright 2014 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sorcer.launcher;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author Rafał Krupiński
  */
 public abstract class SorcerFlavour {
     public abstract String getMainClass();
 
     public List<String> getClassPath() {
         List<String> result = new ArrayList<String>(Arrays.asList(
                 "net.jini:jsk-platform",
                 "net.jini:jsk-lib",
                 "org.apache.river:start",
                 "net.jini.lookup:serviceui",
 
                 "org.rioproject:rio-start",
                 "org.rioproject:rio-platform",
                 "org.rioproject:rio-logging-support",
                 "org.rioproject.resolver:resolver-api",
 
                 "org.sorcersoft.sorcer:sorcer-api",
                 "org.sorcersoft.sorcer:sorcer-resolver",
                 "org.sorcersoft.sorcer:sos-boot",
                "org.sorcersoft.sorcer:sos-rio-start",
                 "org.sorcersoft.sorcer:sos-util",
 
                 "org.codehaus.groovy:groovy-all:2.1.3",
                 "org.apache.commons:commons-lang3:3.1",
 
                 "org.slf4j:slf4j-api",
                 "org.slf4j:jul-to-slf4j:1.7.5",
                 "ch.qos.logback:logback-core:1.0.13",
                 "ch.qos.logback:logback-classic:1.0.13"
         ));
 
         Collections.addAll(result, getFlavourSpecificClassPath());
         return result;
     }
 
     protected abstract String[] getFlavourSpecificClassPath();
 
     public abstract List<String> getDefaultConfigs();
 
     public List<String> getNonResolvableClassPath(){
         return Collections.emptyList();
     }
 
     public abstract OutputConsumer getConsumer();
 }
