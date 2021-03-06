 /*
  * Copyright 2008 Nokia Siemens Networks Oyj
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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.netbeans.jemmy.JemmyProperties;
 import org.netbeans.jemmy.TestOut;
 import org.robotframework.javalib.library.AnnotationLibrary;
 import org.robotframework.swing.keyword.timeout.TimeoutKeywords;
 
 public class SwingLibrary extends AnnotationLibrary {
     public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";
    private static final String DEFAULT_PATTERN = "org/robotframework/swing/keyword/**/*.class";
     private AnnotationLibrary annotationLibrary;
 
     public SwingLibrary() {
         this(Collections.<String>emptyList());
     }
 
     public SwingLibrary(final List<String> keywordPatterns) {
         createLibrary(keywordPatterns);
         disableOutput();
         setDefaultTimeouts();
     }
 
     private void createLibrary(final List<String> keywordPatterns) {
        annotationLibrary = new AnnotationLibrary(new ArrayList<String>() {{
            add(DEFAULT_PATTERN);
            addAll(keywordPatterns);
        }});
     }
 
     public Object runKeyword(String keywordName, Object[] args) {
         return annotationLibrary.runKeyword(keywordName, toStrings(args));
     }
 
     public String[] getKeywordArguments(String keywordName) {
         return annotationLibrary.getKeywordArguments(keywordName);
     }
 
     public String getKeywordDocumentation(String keywordName) {
         return annotationLibrary.getKeywordDocumentation(keywordName);
     }
 
     public String[] getKeywordNames() {
         return annotationLibrary.getKeywordNames();
     }
 
     private void setDefaultTimeouts() {
         new TimeoutKeywords().setJemmyTimeouts("5");
     }
 
     private void disableOutput() {
         JemmyProperties.setCurrentOutput(TestOut.getNullOutput());
     }
 
     private Object[] toStrings(Object[] args) {
         Object[] newArgs = new Object[args.length];
         for (int i = 0; i < newArgs.length; i++) {
             if (args[i].getClass().isArray()) {
                 newArgs[i] = args[i];
             } else {
                 newArgs[i] = args[i].toString();
             }
         }
         return newArgs;
     }
 }
