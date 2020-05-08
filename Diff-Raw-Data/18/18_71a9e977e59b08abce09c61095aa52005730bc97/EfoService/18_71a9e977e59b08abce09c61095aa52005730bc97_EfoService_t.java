 /*
  * Copyright 2012 EMBL - European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.fg.annotare2.services.efo;
 
 import com.google.common.base.Predicate;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nullable;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 /**
  * @author Olga Melnichuk
  */
 public class EfoService {
 
     private static final Logger log = LoggerFactory.getLogger(EfoService.class);
 
     //TODO move EFO_ID to properties
     private static final String HTS_EXPERIMENT_TYPES = "EFO_0003740";
 
     private static final String MA_EXPERIMENT_TYPES = "EFO_0002696";
 
     private static final String LIBRARY_CONSTRUCTION_PROTOCOL = "EFO_0004184";
 
     private static final String SEQUENCING_PROTOCOL = "EFO_0004170";
 
     private final EfoGraph graph;
 
     @Inject
     public EfoService(@Named("efoCacheDir") String cacheDir,
                       @Named("efoUrl") String efoUrl) {
 
         EfoGraph g = null;
         try {
             File dir = (cacheDir == null ? null : new File(cacheDir));
             g = new EfoLoader(dir).load(new URL(efoUrl));
         } catch (IOException e) {
             log.error("Can't load EFO", e);
         } catch (OWLOntologyCreationException e) {
             log.error("Can't load EFO", e);
         }
 
         graph = g;
     }
 
     public EfoNode findHtsInvestigationType(String term) {
         return findNodeByTerm(HTS_EXPERIMENT_TYPES, term);
     }
 
     public EfoNode findMaInvestigationType(String term) {
         return findNodeByTerm(MA_EXPERIMENT_TYPES, term);
     }
 
     private EfoNode findNodeByTerm(String startEfo, final String term) {
         return findNode(startEfo, new Predicate<EfoNode>() {
             @Override
             public boolean apply(@Nullable EfoNode input) {
                return term.equalsIgnoreCase(input.getTerm());
             }
         });
     }
 
     private EfoNode findNodeById(String startEfo, final String id) {
         return findNode(startEfo, new Predicate<EfoNode>() {
             @Override
             public boolean apply(@Nullable EfoNode input) {
                 return id.equals(input.getId());
             }
         });
     }
 
     private EfoNode findByIdOrTerm(String startEfo, final String id, final String term) {
         if (isNullOrEmpty(id)) {
             if (!isNullOrEmpty(term)) {
                 EfoNode node = findNodeByTerm(startEfo, term);
                 if (node != null) {
                     return node;
                 }
             }
         } else if (isNullOrEmpty(term)) {
             if (!isNullOrEmpty(id)) {
                 EfoNode node = findNodeById(startEfo, id);
                 if (node != null) {
                     return node;
                 }
             }
         } else {
             EfoNode node = findNodeById(startEfo, id);
             if (node != null && term.equals(node.getTerm())) {
                 return node;
             }
         }
         return null;
     }
 
     private EfoNode findNode(String startFromId, Predicate<EfoNode> predicate) {
         EfoNode node = graph.getNodeById(startFromId);
         if (node == null) {
             log.error("EFO node class " + startFromId + "not found");
             return null;
         }
         return findDescendant(node, predicate);
     }
 
     private EfoNode findDescendant(EfoNode node, Predicate<EfoNode> predicate) {
         if (predicate.apply(node)) {
             return node;
         }
 
         for (EfoNode child : node.getChildren()) {
             EfoNode found = findDescendant(child, predicate);
             if (found != null) {
                 return found;
             }
         }
         return null;
     }
 
     public boolean isLibraryConstructionProtocol(String accession, String term) {
         return findByIdOrTerm(LIBRARY_CONSTRUCTION_PROTOCOL, accession, term) != null;
     }
 
     public boolean isSequencingProtocol(String accession, String term) {
         return findByIdOrTerm(SEQUENCING_PROTOCOL, accession, term) != null;
     }
 }
