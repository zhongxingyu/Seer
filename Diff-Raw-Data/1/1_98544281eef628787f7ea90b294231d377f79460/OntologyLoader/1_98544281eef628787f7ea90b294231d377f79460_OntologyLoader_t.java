 package uk.ac.ebi.microarray.ontology;
 
 /**
  * Copyright 2009-2010 Microarray Informatics Group, European Bioinformatics Institute
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
 
 import net.sourceforge.fluxion.utils.OWLTransformationException;
 import net.sourceforge.fluxion.utils.OWLUtils;
 import net.sourceforge.fluxion.utils.ReasonerSession;
 import net.sourceforge.fluxion.utils.ReasonerSessionManager;
 import org.semanticweb.owl.apibinding.OWLManager;
 import org.semanticweb.owl.inference.OWLReasoner;
 import org.semanticweb.owl.inference.OWLReasonerException;
 import org.semanticweb.owl.io.StreamInputSource;
 import org.semanticweb.owl.model.*;
 
 import java.io.InputStream;
 import java.util.*;
 
 /**
  * Processes OWL file and creates internal ontology map (node id to node).
  *
  */
 public class OntologyLoader<N extends IOntologyNode>
 {
     private OWLOntology ontology;
     private OWLReasoner reasoner;
     private ReasonerSessionManager sessionManager;
 
     /**
      * Create an instance with ontology read from the given InputStream.
      *
      * @param ontologyStream InputStream with ontology
      */
     public OntologyLoader( InputStream ontologyStream )
     {
         this(new StreamInputSource(ontologyStream));
     }
 
     /**
      * Create an instance with ontology read from the given StreamInputSource.
      *
      * @param ontologyInput StreamInputSource with ontology
      */
     public OntologyLoader( StreamInputSource ontologyInput )
     {
         sessionManager = ReasonerSessionManager.createManager();
        sessionManager.setRecycleAfter(0); // don't cache reasoner resources
         OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
         try {
             ontology = manager.loadOntology(ontologyInput);
         } catch (OWLOntologyCreationException e) {
             throw new RuntimeException("Unable to load ontology", e);
         } finally {
             sessionManager.destroy();
         }
 
     }
 
 
     /**
      * Loads ontology into Map id -> internal node implementation.
      *
      * @param annotationVisitor To visit annotations.
      * @param propertyVisitors  To visit properties.
      * @return Map ontology's been loaded into.
      */
     public Map<String, N> load( IClassAnnotationVisitor<N> annotationVisitor,
                                 IPropertyVisitor<N>... propertyVisitors )
     {
         Map<String, N> ontologyMap = new HashMap<String, N>();
         if (null != sessionManager) {
             ReasonerSession session = sessionManager.acquireReasonerSession(ontology);
             if (null != session) {
                 try {
                     reasoner = session.getReasoner();
                     if (null != reasoner) {
                         try {
                             for (OWLClass cls : ontology.getReferencedClasses()) {
                                 loadClass(cls, annotationVisitor, ontologyMap);
                             }
 
                             for (IPropertyVisitor<N> visitor : propertyVisitors) {
                                 loadProperties(session, visitor, ontologyMap);
                             }
                         } catch (OWLReasonerException e) {
                                 throw new RuntimeException(e);
                         } finally {
                             reasoner.dispose();
                         }
                     }
                 } catch (OWLReasonerException e) {
                     throw new RuntimeException(e);
                 } finally {
                     session.releaseSession();
                 }
             }
         }
         return ontologyMap;
     }
 
     /*
      * Finds all classes with the relationship induced by the property the given visitor is interested in
      * and gives them to the visitor
      */
     private void loadProperties( ReasonerSession session, IPropertyVisitor<N> visitor, Map<String, N> ontologyMap )
     {
         OWLObjectProperty property = getProperty(visitor.getPropertyName());
         if (null != property) {
             for (OWLClass clazz : ontology.getReferencedClasses()) {
                 String id = getId(clazz);
                 N node = ontologyMap.get(id);
                 if (visitor.isInterestedInNode(node)) {
                     Set<OWLRestriction> owlRestrictions;
                     try {
                         owlRestrictions = OWLUtils.keep(session, ontology, clazz, property);
                         for (OWLRestriction restriction : owlRestrictions) {
                             for (OWLClass friend : restriction.getClassesInSignature()) {
                                 String friendId = getId(friend);
                                 visitor.inRelationship(node, ontologyMap.get(friendId));
                             }
                         }
                     } catch (OWLTransformationException e) {
                         throw new RuntimeException(e);
                     }
                 }
             }
         }
     }
 
     /**
      * Finds OWLObjectProperty by name.
      *
      * @param propertyName Name of the property.
      * @return OWLObjectProperty with the specified name.
      */
     public OWLObjectProperty getProperty( String propertyName )
     {
         for (OWLObjectProperty prpt : ontology.getReferencedObjectProperties()) {
             if (prpt.toString().equals(propertyName)) {
                 return prpt;
             }
         }
         return null;
     }
 
     /**
      * Returns class id with special symbols ^*./ removed.
      *
      * @param cls Class that id is looked for.
      * @return Id of the specified class.
      */
     public String getId( OWLClass cls )
     {
         return cls.getURI().getPath().replaceAll("^.*/", "");
     }
 
     /*
      * Loads class and its children and
      * returns respective node if it's not organizational and nodes for its children otherwise.
      * @param clazz Class to load.
      * @param annotationVisitor Visitor for annotations.
      * @return Collection containing the node respective to the specified class if it's not organizational
      * and nodes for its children otherwise.
      * @param ontologyMap Map id -> internal node implementation.
      * @throws OWLReasonerException  If operations with the reasoner fail.
      */
     private Collection<N> loadClass( OWLClass clazz, IClassAnnotationVisitor<N> annotationVisitor,
                                      Map<String, N> ontologyMap )
             throws OWLReasonerException
     {
         if (reasoner.isSatisfiable(clazz)) {
             String id = getId(clazz);
             N node = ontologyMap.get(id);
             if (null == node) {
                 annotationVisitor.newNode();
 
                 for (OWLAnnotation annotation : clazz.getAnnotations(ontology)) {
                     annotation.accept(annotationVisitor);
                 }
                 node = annotationVisitor.getOntologyNode(id);
                 boolean isOrganizational = annotationVisitor.isOrganizational();
                 Set<Set<OWLClass>> children = reasoner.getSubClasses(clazz);
                 for (Set<OWLClass> setOfClasses : children) {
                     for (OWLClass child : setOfClasses) {
                         if (!child.equals(clazz)) {
                             Collection<N> loadedChildren = loadClass(child, annotationVisitor, ontologyMap);
                             annotationVisitor.updateOntologyNode(node, loadedChildren, isOrganizational);
                         }
                     }
                 }
                 if (isOrganizational)
                     return node.getChildren();
                 else {
                     ontologyMap.put(id, node);
                 }
             }
             return Collections.singletonList(node);
         }
         return Collections.EMPTY_LIST;
     }
 
 }
 
