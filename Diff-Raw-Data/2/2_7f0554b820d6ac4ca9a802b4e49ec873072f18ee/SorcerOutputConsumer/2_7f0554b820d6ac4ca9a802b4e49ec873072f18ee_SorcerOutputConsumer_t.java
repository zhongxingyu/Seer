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
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author Rafał Krupiński
  */
 public class SorcerOutputConsumer implements OutputConsumer {
     private static final Logger log = LoggerFactory.getLogger(SorcerOutputConsumer.class);
     private Pattern pattern = Pattern.compile("Started (\\d+)/(\\d+) services; (\\d+) errors");
 
     @Override
     public boolean consume(String line) {
         Matcher m = pattern.matcher(line);
         if (!m.find()) {
             return true;
         }
         String started = m.group(1);
         String all = m.group(2);
         String errors = m.group(3);
        log.debug("Started {} of {} with {} errors", started, all, errors);
         if (!"0".equals(errors))
             throw new IllegalArgumentException("Errors while starting services");
         return !started.equals(all);
     }
 }
