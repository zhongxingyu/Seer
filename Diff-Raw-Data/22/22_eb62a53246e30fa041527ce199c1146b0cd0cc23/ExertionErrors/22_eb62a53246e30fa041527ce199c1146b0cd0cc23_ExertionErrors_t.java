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
 
 package sorcer.junit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sorcer.core.context.ThrowableTrace;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Rafał Krupiński
  */
 public class ExertionErrors {
     private static final Logger log = LoggerFactory.getLogger(ExertionErrors.class);
 
     static public void check(List<ThrowableTrace> errors) throws Exception {
         if (errors == null || errors.isEmpty())
             return;
 
        List<Throwable> exceptions = new ArrayList<Throwable>(errors.size());
 
         for (ThrowableTrace error : errors) {
            Throwable et = error.getThrowable();
            String msg = error.message;
            Throwable t = msg == null ? et : new Exception(msg, et);
             exceptions.add(t);
             log.error("Exertion Error", t);
         }
         throw new MultiException(exceptions);
     }
 
    public static class MultiException extends Exception {
        private List<Throwable> errors;
 
        public MultiException(List<Throwable> others) {
             super("Exceptions found in the Context", others.iterator().next());
            errors = others;
        }

        public List<Throwable> getErrors() {
            return errors;
         }
     }
 }
