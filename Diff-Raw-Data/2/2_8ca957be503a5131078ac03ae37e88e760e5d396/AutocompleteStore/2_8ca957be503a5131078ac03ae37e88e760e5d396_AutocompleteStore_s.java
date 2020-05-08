 package uk.ac.ebi.arrayexpress.utils.autocompletion;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 /*
  * Copyright 2009-2010 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 public class AutocompleteStore
 {
     private SetTrie trie;
     private HashMap<String, AutocompleteData> objects;
 
     private class AutocompleteComparator implements Comparator<String>
     {
         public int compare(String s1, String s2)
         {
             int pos1 = s1.lastIndexOf('|');
             int pos2 = s2.lastIndexOf('|');
 
             String text1 = s1.substring(0, (-1 != pos1 ? pos1 : s1.length() ));
             String text2 = s2.substring(0, (-1 != pos2 ? pos2 : s2.length() ));
 
             int comp = text1.compareTo(text2);
             if (0 == comp) {
                 if (-1 == pos1 && -1 != pos2) {
                     comp = -1;
                 } else if (-1 != pos1 && -1 == pos2) {
                     comp = 1;
                 } else {
                     comp = s1.charAt(pos1 + 1) - s2.charAt(pos2 + 1);
                 }
             }
         return comp;
         }
     }
 
     public AutocompleteStore()
     {
         this.trie = new SetTrie(new AutocompleteComparator());
         this.objects = new HashMap<String, AutocompleteData>();
     }
 
     public void clear()
     {
         trie.clear();
         objects.clear();
     }
     
     public void addData( AutocompleteData data )
     {
         String key = data.getText() + "|" + data.getDataType() + "_" + data.getData();
 
         this.trie.add(key);
         this.objects.put(key, data);
     }
 
     public List<AutocompleteData> findCompletions( String prefix, String fieldName, Integer limit )
     {
         List<AutocompleteData> comps = new ArrayList<AutocompleteData>();
         if ("".equals(fieldName) || -1 == " assaycount samplecount rawcount fgemcount efcount sacount miamescore ".indexOf(" " + fieldName + " ")) {
             List<String> matches = trie.findCompletions(prefix);
 
             for (String key : matches) {
                 AutocompleteData data = this.objects.get(key);
                 boolean shouldAdd = false;
                 if ("".equals(fieldName)) {
                     // in this case we put "keyword" data, EFO and fieldNames, EFO will override keywords
                     if (AutocompleteData.DATA_TEXT == data.getDataType() && "keywords".equals(data.getData())
                             || AutocompleteData.DATA_TEXT != data.getDataType()) {
                         shouldAdd = true;
                     }
                 } else {
                     if ((AutocompleteData.DATA_TEXT == data.getDataType() && fieldName.equals(data.getData()))
                             || (-1 != "sa efv exptype".indexOf(fieldName) && AutocompleteData.DATA_EFO_NODE == data.getDataType())) {
                         shouldAdd = true;
                     }
                 }
                 // so if we want to add this match, check if we have the same text in the list already
                 // if we do, then either discard this one (if it's from the index), or swap with the match (if efo)
                 // or still add (field)
                 if (shouldAdd) {
                     for (int compIndex = 0; compIndex < comps.size(); ++compIndex) {
                         if (comps.get(compIndex).getText().equals(data.getText())) {
                             if (AutocompleteData.DATA_EFO_NODE == comps.get(compIndex).getDataType() && AutocompleteData.DATA_TEXT == data.getDataType()) {
                                 shouldAdd = false;
                                 break;
                             } else if (AutocompleteData.DATA_TEXT == comps.get(compIndex).getDataType() && AutocompleteData.DATA_EFO_NODE == data.getDataType()) {
                                 comps.set(compIndex, data);
                                 shouldAdd = false;
                                 break;
                             }
                         }
                     }
                     if (shouldAdd) {
                         comps.add(data);
                     }
                 }
                 if (null != limit && comps.size() == limit) {
                     break;
                 }
             }
         }
         return comps;
     }
 }
