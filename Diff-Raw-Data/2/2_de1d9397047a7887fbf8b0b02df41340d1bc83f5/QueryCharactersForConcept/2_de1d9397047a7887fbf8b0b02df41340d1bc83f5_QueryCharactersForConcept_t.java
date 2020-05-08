 package org.hymao.mx;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.hymao.mx.Vocab.MX;
 import org.hymao.mx.Vocab.OBO_REL;
 import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLAnnotation;
 import org.semanticweb.owlapi.model.OWLAnnotationProperty;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLDataFactory;
 import org.semanticweb.owlapi.model.OWLLiteral;
 import org.semanticweb.owlapi.model.OWLObjectProperty;
 import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.reasoner.NodeSet;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 
 public class QueryCharactersForConcept {
 
     final OWLOntology ontology;
     final OWLOntologyManager manager;
     final OWLDataFactory factory;
     final OWLReasoner reasoner;
     final String conceptURI;
 
     public QueryCharactersForConcept(OWLOntology ontology, String conceptURI) {
         this.ontology = ontology;
         this.manager = ontology.getOWLOntologyManager();
         this.factory = this.manager.getOWLDataFactory();
         this.conceptURI = conceptURI;
         this.reasoner = new ReasonerFactory().createReasoner(this.ontology);
     }
 
     public Collection<String> getCharacters() {
         final Set<String> characterIDs = new HashSet<String>();
         final OWLClass concept = this.factory.getOWLClass(IRI.create(this.conceptURI));
         final OWLObjectProperty hasPart = this.factory.getOWLObjectProperty(IRI.create(OBO_REL.HAS_PART));
         final OWLObjectSomeValuesFrom hasPartQueriedConcept = this.factory.getOWLObjectSomeValuesFrom(hasPart, concept);
         final NodeSet<OWLClass> subclasses = this.reasoner.getSubClasses(hasPartQueriedConcept, false);
         final OWLAnnotationProperty hasMXID = this.factory.getOWLAnnotationProperty(IRI.create(MX.HAS_MX_ID));
         for (OWLClass phenotype : subclasses.getFlattened()) {
             //FIXME this is just returning the MX ID for the phenotype, not its character
             final Set<OWLAnnotation> annotations = phenotype.getAnnotations(this.ontology, hasMXID);
             for (OWLAnnotation annotation : annotations) {
                 if (annotation.getValue() instanceof OWLLiteral) {
                     final OWLLiteral value = (OWLLiteral)(annotation.getValue());
                     characterIDs.add(value.getLiteral());
                 }
             }
         }
         return characterIDs;
     }
 
     /**
      * @param args
      * @throws OWLOntologyCreationException 
      */
     public static void main(String[] args) throws OWLOntologyCreationException {
        final String input = "http://purl.org/obo/owl/HAO#HAO_0000494";
        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("/Users/jim/Desktop/mx_characters.owl"));
        final QueryCharactersForConcept query = new QueryCharactersForConcept(ontology, input);
       System.out.println(query.getCharacters());
     }
 
 }
