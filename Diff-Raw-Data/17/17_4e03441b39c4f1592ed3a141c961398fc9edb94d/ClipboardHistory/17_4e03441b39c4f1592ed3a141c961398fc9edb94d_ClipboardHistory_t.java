 /**
  * Copyright to the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations under the License.
  */
 
 package b2s.clipboard.history;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 public class ClipboardHistory {
     private LinkedList<String> contents = new LinkedList<String>();
     private final int maxNumberOfContents;
 
     public ClipboardHistory() {
         this(20);
     }
 
     public ClipboardHistory(int maxNumberOfContents) {
         this.maxNumberOfContents = maxNumberOfContents;
     }
 
    public synchronized void add(String text) {
         if (text == null) return;

         if (!contents.contains(text)) {
             contents.addFirst(text);
         } else {
             int index = contents.indexOf(text);
             moveToTop(index);
         }
         if (contents.size() > maxNumberOfContents) {
             contents.removeLast();
         }
     }
 
    public synchronized List<String> toList() {
         return Collections.unmodifiableList(contents);
     }
 
    public synchronized void moveToTop(int row) {
         contents.addFirst(contents.remove(row));
     }
 
    public synchronized boolean hasContents() {
         return contents.size() > 0;
     }
 
    public synchronized String top() {
         return hasContents() ? contents.getFirst() : null;
     }
 }
