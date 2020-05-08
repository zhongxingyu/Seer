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
 
 package uk.ac.ebi.fg.annotare2.magetabcheck.modelimpl.limpopo.idf;
 
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.IDF;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.idf.*;
 
 import javax.annotation.Nonnull;
import java.util.*;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.primitives.Ints.max;
 
 /**
  * @author Olga Melnichuk
  */
 public class LimpopoIdfDataProxy implements IdfData {
 
     private final IdfHelper idfHelper;
 
     public LimpopoIdfDataProxy(@Nonnull IDF idf) {
         this.idfHelper = new IdfHelper(idf);
     }
 
     @Override
     public Info getInfo() {
         return new LimpopoBasedInfo(idfHelper);
     }
 
     @Override
     public Collection<Person> getContacts() {
         IDF idf = idfHelper.idf();
         int size = max(size(idf.personFirstName),
                 size(idf.personLastName),
                 size(idf.personEmail),
                 size(idf.personAddress),
                 size(idf.personAffiliation),
                 size(idf.personPhone),
                 size(idf.personMidInitials),
                 size(idf.personFax),
                 size(idf.personRoles));
         List<Person> contacts = newArrayList();
         for (int i = 0; i < size; i++) {
             contacts.add(new LimpopoBasedPerson(idfHelper, i));
         }
         return contacts;
     }
 
     @Override
     public Collection<ExperimentalDesign> getExperimentDesigns() {
         int size = size(idfHelper.idf().experimentalDesign);
         List<ExperimentalDesign> designs = newArrayList();
         for (int i = 0; i < size; i++) {
             designs.add(new LimpopoBasedExperimentalDesign(idfHelper, i));
         }
         return designs;
     }
 
     @Override
     public Collection<ExperimentalFactor> getExperimentalFactors() {
         int size = max(size(idfHelper.idf().experimentalFactorName),
                 size(idfHelper.idf().experimentalFactorType));
         List<ExperimentalFactor> factors = newArrayList();
         for (int i = 0; i < size; i++) {
             factors.add(new LimpopoBasedExperimentalFactor(idfHelper, i));
         }
         return factors;
     }
 
     @Override
     public Collection<QualityControlType> getQualityControlTypes() {
         int size = size(idfHelper.idf().qualityControlType);
         List<QualityControlType> qualityControlTypes = newArrayList();
         for (int i = 0; i < size; i++) {
             qualityControlTypes.add(new LimpopoBasedQualityControlType(idfHelper, i));
         }
         return qualityControlTypes;
     }
 
     @Override
     public Collection<ReplicateType> getReplicateTypes() {
         int size = size(idfHelper.idf().replicateType);
         List<ReplicateType> replicateTypes = newArrayList();
         for (int i = 0; i < size; i++) {
             replicateTypes.add(new LimpopoBasedReplicateType(idfHelper, i));
         }
         return replicateTypes;
     }
 
     @Override
     public Collection<NormalizationType> getNormalizationTypes() {
         int size = size(idfHelper.idf().normalizationType);
         List<NormalizationType> normalizationTypes = newArrayList();
         for (int i = 0; i < size; i++) {
             normalizationTypes.add(new LimpopoBasedNormalizationType(idfHelper, i));
         }
         return normalizationTypes;
     }
 
     @Override
     public Collection<Publication> getPublications() {
         int size = max(size(idfHelper.idf().publicationTitle),
                 size(idfHelper.idf().publicationStatus),
                 size(idfHelper.idf().pubMedId),
                 size(idfHelper.idf().publicationDOI),
                 size(idfHelper.idf().publicationAuthorList));
         List<Publication> publications = newArrayList();
         for (int i = 0; i < size; i++) {
             publications.add(new LimpopoBasedPublication(idfHelper, i));
         }
         return publications;
     }
 
     @Override
     public Collection<Protocol> getProtocols() {
         int size = size(idfHelper.idf().protocolName);
         List<Protocol> protocols = newArrayList();
         for (int i = 0; i < size; i++) {
             protocols.add(new LimpopoBasedProtocol(idfHelper, i));
         }
         return protocols;
     }
 
     @Override
     public Collection<TermSource> getTermSources() {
         return idfHelper.getTermSources();
     }
 
     @Override
     public Collection<Comment> getComments(String type) {
         Map<String, Set<String>> allComments = idfHelper.idf().getComments();
        Set<String> comments = allComments.containsKey(type) ?
                allComments.get(type): Collections.<String>emptySet();
        return createComments(comments, type);
     }
 
     @Override
     public Collection<Comment> getComments() {
         Map<String, Set<String>> allComments = idfHelper.idf().getComments();
         List<Comment> comments = newArrayList();
         for (String type : allComments.keySet()) {
             comments.addAll(createComments(allComments.get(type), type));
         }
         return comments;
     }
 
     private Collection<Comment> createComments(Set<String> values, String type) {
         List<Comment> comments = newArrayList();
         int i = 0;
         for (String value : values) {
             comments.add(new LimpopoBasedComment(idfHelper, i++, type, value));
         }
         return comments;
     }
 
     @Override
     public TermSource getTermSource(String ref) {
         return idfHelper.getTermSource(ref);
     }
 
     private int size(List<String> list) {
         return list == null || list.isEmpty() ? 0 : list.size();
     }
 }
