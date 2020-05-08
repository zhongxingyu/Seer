 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.ac.ebi.chemet.io.sbml;
 
 import java.util.List;
 import java.io.InputStream;
 import javax.xml.stream.XMLStreamException;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import uk.ac.ebi.chemet.entities.reaction.AtomContainerReaction;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author johnmay
  */
 public class ReactionLoaderTest {
 
     public ReactionLoaderTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Test
     public void testSBMLLoader() {
         try {
             InputStream sbmlStream = getClass().getResourceAsStream( "streptomyces-coelicolor-6.2005.xml" );
            ReactionLoader loader = ReactionLoader.getInstance();
             List<AtomContainerReaction> reactions = loader.getReactions( sbmlStream );
             for ( AtomContainerReaction r : reactions ) {
                 System.out.println( r );
             }
         } catch ( XMLStreamException ex ) {
             fail( "Error reading XML Stream : " + ex.getMessage() );
         }
     }
 }
