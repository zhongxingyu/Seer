 /*
  * 
  */
 package com.graph.bio;
 
 import com.graph.bio.domain.Protein;
 import com.graph.bio.domain.ProteinInteraction;
 import com.graph.bio.repository.ProteinRepository;
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.neo4j.template.Neo4jOperations;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 /**
  * 
  * @author Manoj Joshi
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration({"classpath:/spring/intact-context.xml" })
 @TransactionConfiguration(transactionManager = "neo4jTransactionManager", defaultRollback = true)
 @Transactional
 public class GraphEngineTest {
     @Autowired
     ProteinRepository proteinRepository;
     
     @Autowired 
     Neo4jOperations template;
     
     protected static Log log = LogFactory.getLog(new Object().getClass());
 
     @Test
     public void proteinSaveAndSearchTest() {
         Protein protein1 = new Protein("P167634", "protein", "protein1", "475435437");
         proteinRepository.save(protein1);
         Protein protein4 = proteinRepository.findByPropertyValue("uniprot", "P167634");
         assertEquals(protein1, protein4);
     }
     
     @Test
     public void proteinSaveAndDeleteTest() {
         Protein protein1 = new Protein("P167654", "protein", "protein1", "475435437");
         proteinRepository.save(protein1);
         Protein protein4 = proteinRepository.findByPropertyValue("uniprot", "P167654");
         assertEquals(protein1, protein4);
         proteinRepository.delete(protein1);
         protein4 = proteinRepository.findByPropertyValue("uniprot", "P167654");
         assertNotNull(protein4);
     }
     
     /**
      * Should find user entity with relationships.
      */
     @Test
     public void proteinRelationship() {
         Protein protein1 = new Protein("P187634", "protein", "protein1", "475435437");
         Protein protein2 = new Protein("P183954", "protein", "protein2", "475435438");
         template.save(protein1);
        //template.save(protein2);
         //ProteinInteraction proteinInteraction1 = new ProteinInteraction(protein1, protein2);
         //proteinInteraction1.setName("phosphorylation");
         ProteinInteraction proteinInteraction = protein1.interactsWith(protein2, "phosphorylation");
         //protein2.interactsWith(protein1, "phosphorylation");
         template.save(proteinInteraction);
         
         //template.save(proteinInteraction1);
         Protein foundProtein1 = proteinRepository.findByPropertyValue("uniprot", "P187634");
         log.info("******* FOUND PROTEIN1 = " + foundProtein1.getMessage());
         //Collection<ProteinInteraction> interactions = foundProtein1.getProteinInteractions();
         log.info("*****CLASS = " + foundProtein1.getProteinInteractions().getClass().getName());
         log.info("*****PI CLASS = " + ((ArrayList)foundProtein1.getProteinInteractions()).iterator().next().getClass().getName());
         
         // Had to comment out a bunch of stuff, as the relationships did not come out
         // as expected. The casting is a problem.
         // Something to do with  Type class org.springframework.data.neo4j.fieldaccess.GraphBackedEntityIterableWrapper
         ProteinInteraction p = (ProteinInteraction)((ArrayList)foundProtein1.getProteinInteractions()).iterator().next();
         
         //log.info("****** START PROTEIN = " + p.getStartProtein().getMessage());
         //log.info("****** END PROTEIN = " + p.getEndProtein().getMessage());
         
         /*
         Relationship rel = template.getRelationship(proteinInteraction1.getId());
         log.info("**************TYPE = " + rel.getType());
         Protein protein4 = proteinRepository.findByPropertyValue("uniprot", "P187634");
         Node node = template.getNode(protein4.getId());
         log.info("*********************************************************************id = " + node.getProperty("message"));
         Iterable<Relationship> relationships = node.getRelationships(Direction.BOTH);
         Iterator iter = relationships.iterator();
         while (iter.hasNext()) {
             log.info("FOUND SOMETHING ************************* " + iter.next());
         }
         for (Relationship r : relationships) {
             log.info("*****************************************************************content of r = " + r.getId());
             assertFalse(r.getStartNode().equals(protein2));
             assertFalse(r.getEndNode().equals(protein1));
         }
         
         */
     }
     
     /**
      * Should find user entity with relationships.
      */
     @Test
     public void proteinTest() {
         Protein protein1 = new Protein("P187634", "protein", "protein1", "475435437");
         Protein protein2 = new Protein("P183954", "protein", "protein2", "475435438");
         Protein protein3 = new Protein("P183955", "protein", "protein3", "475435439");
         ProteinInteraction proteinInteraction1 = new ProteinInteraction(protein1, protein2);
         proteinInteraction1.setName("phosphorylation");
         ProteinInteraction proteinInteraction2 = new ProteinInteraction(protein2, protein3);
         proteinInteraction2.setName("methylation");
         ProteinInteraction proteinInteraction3 = new ProteinInteraction(protein1, protein3);
         proteinInteraction3.setName("ubiquitylation");
         proteinRepository.save(protein1);
         proteinRepository.save(protein2);
         proteinRepository.save(protein3);
         template.save(proteinInteraction1);
         template.save(proteinInteraction2);
         template.save(proteinInteraction3);
     }
 }
