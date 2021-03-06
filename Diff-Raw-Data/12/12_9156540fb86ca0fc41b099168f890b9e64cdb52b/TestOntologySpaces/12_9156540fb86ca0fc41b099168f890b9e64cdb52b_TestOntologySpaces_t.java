 package org.apache.stanbol.ontologymanager.ontonet.ontology;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Hashtable;
 
 import org.apache.stanbol.ontologymanager.ontonet.Constants;
 import org.apache.stanbol.ontologymanager.ontonet.api.ONManager;
 import org.apache.stanbol.ontologymanager.ontonet.api.io.OntologyInputSource;
 import org.apache.stanbol.ontologymanager.ontonet.api.io.ParentPathInputSource;
 import org.apache.stanbol.ontologymanager.ontonet.api.io.RootOntologySource;
 import org.apache.stanbol.ontologymanager.ontonet.api.ontology.CoreOntologySpace;
 import org.apache.stanbol.ontologymanager.ontonet.api.ontology.CustomOntologySpace;
 import org.apache.stanbol.ontologymanager.ontonet.api.ontology.OntologySpaceFactory;
 import org.apache.stanbol.ontologymanager.ontonet.api.ontology.OntologySpaceModificationException;
 import org.apache.stanbol.ontologymanager.ontonet.api.ontology.SessionOntologySpace;
 import org.apache.stanbol.ontologymanager.ontonet.api.ontology.UnmodifiableOntologySpaceException;
 import org.apache.stanbol.ontologymanager.ontonet.impl.ONManagerImpl;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.semanticweb.owlapi.model.AddAxiom;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLDataFactory;
 import org.semanticweb.owlapi.model.OWLIndividual;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 
 public class TestOntologySpaces {
 
     public static IRI baseIri = IRI.create(Constants.PEANUTS_MAIN_BASE), baseIri2 = IRI
             .create(Constants.PEANUTS_MINOR_BASE), scopeIri = IRI
             .create("http://kres.iks-project.eu/scope/Peanuts");
 
     private static OWLAxiom linusIsHuman = null;
 
     private static ONManager onm;
 
     private static OWLOntology ont = null, ont2 = null;
 
     private static OntologyInputSource ontSrc, ont2Src, pizzaSrc, colleSrc;
 
     private static OntologySpaceFactory spaceFactory;
 
     private static OntologyInputSource getLocalSource(String resourcePath, OWLOntologyManager mgr) throws OWLOntologyCreationException,
                                                                                                   URISyntaxException {
         URL url = TestOntologySpaces.class.getResource(resourcePath);
         File f = new File(url.toURI());
        return new ParentPathInputSource(f, mgr != null ? mgr : onm.getOntologyManagerFactory()
                .createOntologyManager(true));
     }
 
     @BeforeClass
     public static void setup() throws Exception {
 
         // An ONManagerImpl with no store and default settings
         onm = new ONManagerImpl(null, null, new Hashtable<String,Object>());
         spaceFactory = onm.getOntologySpaceFactory();
         if (spaceFactory == null) fail("Could not instantiate ontology space factory");
 
         OWLOntologyManager mgr = onm.getOntologyManagerFactory().createOntologyManager(true);
         OWLDataFactory df = mgr.getOWLDataFactory();
 
         ont = mgr.createOntology(baseIri);
         ontSrc = new RootOntologySource(ont, null);
         // Let's state that Linus is a human being
         OWLClass cHuman = df.getOWLClass(IRI.create(baseIri + "/" + Constants.humanBeing));
         OWLIndividual iLinus = df.getOWLNamedIndividual(IRI.create(baseIri + "/" + Constants.linus));
         linusIsHuman = df.getOWLClassAssertionAxiom(cHuman, iLinus);
         mgr.applyChange(new AddAxiom(ont, linusIsHuman));
 
         ont2 = mgr.createOntology(baseIri2);
         ont2Src = new RootOntologySource(ont2);
 
         pizzaSrc = getLocalSource("/ontologies/pizza.owl", mgr);
         colleSrc = getLocalSource("/ontologies/odp/collectionentity.owl", mgr);
         ont2Src = new RootOntologySource(ont2, null);
 
     }
 
     @Test
     public void testAddOntology() {
         CustomOntologySpace space = null;
         IRI logicalId = colleSrc.getRootOntology().getOntologyID().getOntologyIRI();
         try {
             space = spaceFactory.createCustomOntologySpace(scopeIri, pizzaSrc);
             space.addOntology(ont2Src);
 
             space.addOntology(colleSrc);
 
         } catch (UnmodifiableOntologySpaceException e) {
             fail("Add operation on " + scopeIri + " custom space was denied due to unexpected lock.");
         }
 
         assertTrue(space.containsOntology(logicalId));
         logicalId = pizzaSrc.getRootOntology().getOntologyID().getOntologyIRI();
         assertTrue(space.containsOntology(logicalId));
     }
 
     @Test
     public void testCoreLock() {
         CoreOntologySpace space = spaceFactory.createCoreOntologySpace(scopeIri, ontSrc);
         space.setUp();
         try {
             space.addOntology(ont2Src);
             fail("Modification was permitted on locked ontology space.");
         } catch (UnmodifiableOntologySpaceException e) {
             assertSame(space, e.getSpace());
         }
     }
 
     @Test
     public void testCreateSpace() throws Exception {
         CustomOntologySpace space = spaceFactory.createCustomOntologySpace(scopeIri, pizzaSrc);
         IRI logicalId = pizzaSrc.getRootOntology().getOntologyID().getOntologyIRI();
         assertTrue(space.containsOntology(logicalId));
     }
 
     @Test
     public void testCustomLock() {
         CustomOntologySpace space = spaceFactory.createCustomOntologySpace(scopeIri, ontSrc);
         space.setUp();
         try {
             space.addOntology(ont2Src);
             fail("Modification was permitted on locked ontology space.");
         } catch (UnmodifiableOntologySpaceException e) {
             assertSame(space, e.getSpace());
         }
     }
 
     @Test
     public void testRemoveCustomOntology() {
         CustomOntologySpace space = null;
         space = spaceFactory.createCustomOntologySpace(scopeIri, pizzaSrc);
         IRI pizzaId = pizzaSrc.getRootOntology().getOntologyID().getOntologyIRI();
         IRI wineId = colleSrc.getRootOntology().getOntologyID().getOntologyIRI();
         try {
             space.addOntology(ontSrc);
             space.addOntology(colleSrc);
             // The other remote ontologies may change base IRI...
             assertTrue(space.containsOntology(ont.getOntologyID().getOntologyIRI())
                        && space.containsOntology(pizzaId) && space.containsOntology(wineId));
             space.removeOntology(pizzaSrc);
             assertFalse(space.containsOntology(pizzaId));
             space.removeOntology(colleSrc);
             assertFalse(space.containsOntology(wineId));
             // OntologyUtils.printOntology(space.getTopOntology(), System.err);
         } catch (UnmodifiableOntologySpaceException e) {
             fail("Modification was disallowed on non-locked ontology space.");
         } catch (OntologySpaceModificationException e) {
             fail("Modification failed on ontology space " + e.getSpace().getID());
         }
     }
 
     @Test
     public void testSessionModification() {
         SessionOntologySpace space = spaceFactory.createSessionOntologySpace(scopeIri);
         space.setUp();
         try {
             // First add an in-memory ontology with a few axioms.
             space.addOntology(ontSrc);
             // Now add a real online ontology
             space.addOntology(pizzaSrc);
             // The in-memory ontology must be in the space.
             assertTrue(space.getOntologies().contains(ont));
             // The in-memory ontology must still have its axioms.
             assertTrue(space.getOntology(ont.getOntologyID().getOntologyIRI()).containsAxiom(linusIsHuman));
             // The top ontology must still have axioms from in-memory
             // ontologies.
             assertTrue(space.getTopOntology().containsAxiom(linusIsHuman));
         } catch (UnmodifiableOntologySpaceException e) {
             fail("Modification was denied on unlocked ontology space.");
         }
     }
 
 }
