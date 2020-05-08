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
 
 import uk.ac.ebi.fg.annotare2.magetabcheck.ServiceUnavailableException;
 
 import java.util.Collection;
 
 /**
  * @author Olga Melnichuk
  */
 public interface EfoService {
 
     public static final String HTS_EXPERIMENT_TYPES = "EFO_0003740";
 
     public static final String MA_EXPERIMENT_TYPES = "EFO_0002696";
 
     public static final String LIBRARY_CONSTRUCTION_PROTOCOL = "EFO_0004184";
 
     public static final String SEQUENCING_PROTOCOL = "EFO_0004170";
 
     public static final String BIOLOGICAL_VARIATION_DESINGS = "EFO_0004667";
 
     public static final String METHODOLOGICAL_VARIATION_DESIGNS = "EFO_0004669";
 
     public static final String BIOMOLECULAR_ANNOTATION_DESIGNS = "EFO_0004665";
 
     public static final EfoService UNAVAILABLE = new EfoService() {
         @Override
         public String findHtsInvestigationType(String name) {
             throw unavailable();
         }
 
         @Override
         public String findMaInvestigationType(String name) {
             throw unavailable();
         }
 
         @Override
         public boolean isLibraryConstructionProtocol(String accession, String name) {
             throw unavailable();
         }
 
         @Override
         public boolean isSequencingProtocol(String accession, String name) {
             throw unavailable();
         }
 
         @Override
         public Collection<String> getSubTermsOf(String accession) {
             throw unavailable();
         }
 
         private ServiceUnavailableException unavailable() {
            throw unavailable();
         }
     };
 
     /**
      * Looks through the all descendants of {@value #HTS_EXPERIMENT_TYPES} term and returns
      * accession of the term which name equals to the given one.
      *
      * @param name name of the term to find
      * @return term accession or <code>null</code> if term was not found
      */
     String findHtsInvestigationType(String name);
 
     /**
      * Looks through the all descendants of {@value #MA_EXPERIMENT_TYPES} term and returns
      * accession of the term which name equals to the given one.
      *
      * @param name name of the term to find
      * @return term accession or <code>null</code> if term was not found
      */
     String findMaInvestigationType(String name);
 
     /**
      * Checks if the given term accession and name correspond to the existed EFO term located in the
      * {@value #LIBRARY_CONSTRUCTION_PROTOCOL} branch. At least on of arguments (accession or name) should be not null.
      *
      * @param accession a term accession
      * @param name      a term name
      * @return <code>true</code> if
      */
     boolean isLibraryConstructionProtocol(String accession, String name);
 
     /**
      * Checks if the given term accession and name correspond to the existed EFO term located in the
      * {@value #LIBRARY_CONSTRUCTION_PROTOCOL} branch. At least on of arguments (accession or name) should be not null.
      *
      * @param accession a term accession
      * @param name      a term name
      * @return <code>true</code> if
      */
     boolean isSequencingProtocol(String accession, String name);
 
     /**
      * Returns names of all child terms.
      *
      * @param accession a term accession to get children from
      * @return a collection of term names
      */
     Collection<String> getSubTermsOf(String accession);
 }
