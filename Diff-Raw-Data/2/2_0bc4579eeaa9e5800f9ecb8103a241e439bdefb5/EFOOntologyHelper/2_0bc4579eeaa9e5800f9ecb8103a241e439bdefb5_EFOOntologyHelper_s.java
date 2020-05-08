 package uk.ac.ebi.microarray.ontology.efo;
 
 /**
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
 
 /**
  * @author Anna Zhukova
  */
 
 import uk.ac.ebi.microarray.ontology.OntologyLoader;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class EFOOntologyHelper
 {
     private Map<String, EFONode> efoMap = new HashMap<String, EFONode>();
     private Map<String, Set<String>> partOfIdMap = new HashMap<String, Set<String>>();
 
     /**
      * Constructor loading ontology data.
      *
      * @param ontologyStream InputStream with ontology.
      */
     public EFOOntologyHelper( InputStream ontologyStream )
     {
         OntologyLoader<EFONode> loader = new OntologyLoader<EFONode>(ontologyStream);
         this.efoMap = loader.load(
                 new EFOClassAnnotationVisitor()
                 , new EFOPartOfPropertyVisitor(this.partOfIdMap)
         );
     }
 
     public Map<String, EFONode> getEfoMap()
     {
         return efoMap;
     }
 
     public Map<String, Set<String>> getEfoPartOfIdMap()
     {
         return partOfIdMap;
     }
 
     public Set<String> getChildTerms( String efoId, int includeFlags )
     {
         EFONode node = getEfoMap().get(efoId);
         Set<String> terms = new HashSet<String>();
         if (node.hasChildren()) {
             for (EFONode child : node.getChildren()) {
                 terms.addAll(addChildTerms(child, includeFlags));
             }
         }
         return terms;
     }
 
     private Set<String> addChildTerms( EFONode node, int includeFlags )
     {
         Set<String> terms = new HashSet<String>();
         if (null != node) {
             if ((includeFlags | INCLUDE_ALT_TERMS) > 0) {
                 terms.addAll(node.getAlternativeTerms());
             }
 
             if (node.hasChildren()) {
                 for (EFONode child : node.getChildren()) {
                     terms.addAll(addChildTerms(child, includeFlags));
                 }
             }
 
             if ((includeFlags | INCLUDE_PART_OF_TERMS) > 0) {
                 if (getEfoPartOfIdMap().containsKey(node.getId())) {
                     for (String partOfId : getEfoPartOfIdMap().get(node.getId())) {
                         EFONode partOfNode = getEfoMap().get(partOfId);
                         terms.addAll(addChildTerms(partOfNode, includeFlags));
                     }
                 }
             }
         }
 
         return terms;
     }
 
     public final static String EFO_ROOT_ID = "http://www.ebi.ac.uk/efo/EFO_0000001";
 
     public final static int INCLUDE_ALT_TERMS = 1;
     public final static int INCLUDE_PART_OF_TERMS = 2;
     public final static int INCLUDE_ALL = INCLUDE_ALT_TERMS + INCLUDE_PART_OF_TERMS;
 }
