 package uk.ac.ebi.arrayexpress.components;
 
 import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
 import uk.ac.ebi.arrayexpress.utils.autocompletion.AutocompleteData;
 import uk.ac.ebi.arrayexpress.utils.autocompletion.AutocompleteStore;
 import uk.ac.ebi.microarray.ontology.efo.EFONode;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
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
 
 public class Autocompletion extends ApplicationComponent
 {
     private AutocompleteStore autocompleteStore;
 
     private Experiments experiments;
     private SearchEngine search;
     private Ontologies ontologies;
 
     public Autocompletion()
     {
     }
 
     public void initialize() throws Exception
     {
         this.autocompleteStore = new AutocompleteStore();
 
         this.experiments = (Experiments) getComponent("Experiments");
         this.search = (SearchEngine) getComponent("SearchEngine");
         this.ontologies = (Ontologies) getComponent("Ontologies");
     }
 
     public void terminate() throws Exception
     {
     }
 
     public String getKeywords( String prefix, String field, Integer limit )
     {
         StringBuilder sb = new StringBuilder("");
         List<AutocompleteData> matches = getStore().findCompletions(prefix, field, limit);
         for (AutocompleteData match : matches) {
             sb.append(match.getText()).append('|').append(match.getDataType()).append('|').append(match.getData()).append('\n');
         }
         return sb.toString();
     }
 
     public void rebuild() throws IOException
     {
         getStore().clear();
 
         // adding field terms (for all non-numerical fields) and names (if there is a description)
         Set<String> fields = search.getController().getFieldNames(experiments.INDEX_ID);
         for (String field : fields) {
             String fieldTitle = search.getController().getFieldTitle(experiments.INDEX_ID, field);
             if (null != fieldTitle && fieldTitle.length() > 0) {
                 getStore().addData(
                         new AutocompleteData(
                                 field
                                 , AutocompleteData.DATA_FIELD
                                 , fieldTitle
                         )
                 );
             }
             String fieldType = search.getController().getFieldType(experiments.INDEX_ID, field);
             if (null != fieldType && !"integer".equals(fieldType)) {
                 for (String term : search.getController().getTerms(experiments.INDEX_ID, field, "keywords".equals(field) ? 10 : 1)) {
                     getStore().addData(
                             new AutocompleteData(
                                     term
                                     , AutocompleteData.DATA_TEXT
                                     , field
                             )
                     );
                 }
             }
         }
 
        if (null != ontologies) {
             addEfoNodeWithDescendants(ontologies.getOntology().EFO_ROOT_ID);
         }
     }
 
     private void addEfoNodeWithDescendants( String nodeId )
     {
         EFONode node = ontologies.getOntology().getEfoMap().get(nodeId);
         if (null != node) {
             getStore().addData(
                     new AutocompleteData(
                             node.getTerm()
                             , AutocompleteData.DATA_EFO_NODE
                             , node.hasChildren() ? node.getId() : ""
                     )
             );
             /* no synonyms for now
             for (String syn : node.getAlternativeTerms()) {
                 this.autocompleteStore.addData(
                         new AutocompleteData(
                                 syn
                                 , AutocompleteData.DATA_EFO_NODE
                                 , ""
                         )
                 );
             }
             */
             if (node.hasChildren()) {
                 for (EFONode child : node.getChildren()) {
                     addEfoNodeWithDescendants(child.getId());
                 }
             }
         }
     }
 
     private AutocompleteStore getStore()
     {
         return this.autocompleteStore;
     }
 }
