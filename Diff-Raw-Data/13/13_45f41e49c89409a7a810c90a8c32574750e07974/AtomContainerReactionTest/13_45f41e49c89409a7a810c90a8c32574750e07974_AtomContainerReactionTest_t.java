 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.ac.ebi.chemet.entities.reaction;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openscience.cdk.templates.MoleculeFactory;
 import uk.ac.ebi.chemet.entities.reaction.participant.AtomContainerParticipant;
import uk.ac.ebi.core.CompartmentImplementation;
import uk.ac.ebi.core.reaction.compartment.Membrane;
 import uk.ac.ebi.core.reaction.compartment.Organelle;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static uk.ac.ebi.chemet.TestMoleculeFactory.*;

 
 /**
  *
  * @author johnmay
  */
 public class AtomContainerReactionTest {
 
     private static AtomContainerReaction q1;
 
     private static Integer i = 1;
 
 
     public AtomContainerReactionTest() {
     }
 
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         // query
         q1 = new AtomContainerReaction();
 
         q1.addReactant(_123Triazole(), 1d, Organelle.EXTRACELLULA);
         q1.addReactant(adenine(), 2d, Organelle.CYTOPLASM);
 
         q1.addProduct(benzene(), 2d, Membrane.MITOCHONDRIAL_MEMBRANE);
         q1.addProduct(cyclohexane(), 1d, Organelle.CYTOPLASM);
         q1.addProduct(butan1ol(), 2d, Organelle.CYTOPLASM);
 
     }
 
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
 
     @Test
     public void testSomeMethod() {
     }
 
 
     @Test
     public void testEqualsDifferentSides() {
 // TODO
         // different sides
         System.out.printf("%-120s", "[TEST] Reaction with transposed direction");
 
         AtomContainerReaction r = new AtomContainerReaction();
 
         r.addReactant(benzene(), 2d, Membrane.MITOCHONDRIAL_MEMBRANE);
         r.addReactant(cyclohexane(), 1d, Organelle.CYTOPLASM);
         r.addReactant(butan1ol(), 2d, Organelle.CYTOPLASM);
 
         r.addProduct(_123Triazole(), 1d, Organelle.EXTRACELLULA);
         r.addProduct(adenine(), 2d, Organelle.CYTOPLASM);
 
         assertEquals("FAILED", q1.hashCode(), r.hashCode());
         assertEquals("FAILED", q1, r);
 
         System.out.println("PASSED");
     }
 
 
     @Test
     public void testEqualsDifferentOrganisation() {
 
         System.out.printf("%-120s", "[TEST] Different participant organisation");
 
         // same total molecules but on different sides
         AtomContainerReaction r = new AtomContainerReaction();
         r.addReactant(benzene(), 2d, CompartmentImplementation.MITOCHONDRIAL_MEMBRANE);
         r.addProduct(cyclohexane(), 1d, CompartmentImplementation.CYTOPLASM);
         r.addReactant(_123Triazole(), 1d, CompartmentImplementation.EXTRACELLULA);
         r.addProduct(adenine(), 2d, CompartmentImplementation.CYTOPLASM);
         r.addProduct(butan1ol(), 2d, CompartmentImplementation.CYTOPLASM);
 
         //  System.out.printf( "\t\tq: %10s %s\n" , q1.hashCode() , q1 );
         //  System.out.printf( "\t\tr: %10s %s\n" , r3.hashCode() , r3 );
 
         assertNotSame("FAILED", q1, r);
 
         System.out.println("PASSED");
 
     }
 
 
     @Test
     public void testEqualsDifferentOrg2() {
 
         System.out.printf("%-120s", "[TEST] Different participant organisation");
 
         // different sides one molecule stays the same side
         AtomContainerReaction r = new AtomContainerReaction();
         r.addReactant(benzene(), 2d, CompartmentImplementation.MITOCHONDRIAL_MEMBRANE);
         r.addReactant(cyclohexane(), 1d, CompartmentImplementation.CYTOPLASM);
         r.addProduct(_123Triazole(), 1d, CompartmentImplementation.EXTRACELLULA);
         r.addProduct(adenine(), 2d, CompartmentImplementation.CYTOPLASM);
         r.addProduct(butan1ol(), 2d, CompartmentImplementation.CYTOPLASM); // stays same but others move
 
         // System.out.printf( "\t\tq: %10s %s\n" , q1.hashCode() , q1 );
         // System.out.printf( "\t\tr: %10s %s\n" , r.hashCode() , r );
 
 
         // hash code doesn't need to be the same
         assertNotSame("FAILED", q1, r);
 
         System.out.println("PASSED");
 
 
 
     }
 
 
     @Test
     public void testEqualsOneDifferentMol() {
 
         System.out.printf("%-120s", "[TEST] Different participants");
 
         // one different mol
         AtomContainerReaction r = new AtomContainerReaction();
         r.addProduct(benzene(), 2d, CompartmentImplementation.MITOCHONDRIAL_MEMBRANE);
         r.addProduct(cyclohexane(), 1d, CompartmentImplementation.CYTOPLASM);
         r.addProduct(butan1ol(), 2d, CompartmentImplementation.CYTOPLASM);
         r.addReactant(_124Triazole(), 1d, CompartmentImplementation.EXTRACELLULA); // difference
         r.addReactant(adenine(), 2d, CompartmentImplementation.CYTOPLASM);
 
 //        System.out.printf( "\tvq: %10s %s\n" , q1.hashCode() , q1 );
 //        System.out.printf( "\t\tr: %10s %s\n" , r.hashCode() , r );
 
 
         // different sides one molecule stays the same side
         assertNotSame("FAILED", q1, r);
 
         System.out.println("PASSED");
 
     }
 
 
     @Test
     public void testEqualsOneDiffIsomer() {
 
         System.out.printf("%-120s", "[TEST] Different participants (Isomer butan-1-ol ≠ butan-2ol)");
 
         // one different mol (isomer)
         AtomContainerReaction r = new AtomContainerReaction();
         r.addReactant(_123Triazole(), 1d, CompartmentImplementation.EXTRACELLULA); // difference
         r.addReactant(adenine(), 2d, CompartmentImplementation.CYTOPLASM);
         r.addProduct(benzene(), 2d, CompartmentImplementation.MITOCHONDRIAL_MEMBRANE);
         r.addProduct(cyclohexane(), 1d, CompartmentImplementation.CYTOPLASM);
         r.addProduct(butan2ol(), 2d, CompartmentImplementation.CYTOPLASM); // butan-2-ol ≠ butan-1-ol
 
 //        System.out.printf( "\t\tq: %10s %s\n" , q1.hashCode() , q1 );
 //        System.out.printf( "\t\tr: %10s %s\n" , r.hashCode() , r );
 
 
         // hash code doesn't need to be the same
         assertNotSame("FAILED", q1, r);
 
         System.out.println("PASSED");
 
     }
 
 
     @Test
     public void testEqualsDiffCoef() {
 
         System.out.printf("%-120s", "[TEST] Different participants (coefficients)");
 
         // one molecule with different stoichiometry
         AtomContainerReaction r = new AtomContainerReaction();
         r.addReactant(MoleculeFactory.make123Triazole(), 2d, CompartmentImplementation.EXTRACELLULA); // different stoichiometry
         r.addReactant(MoleculeFactory.makeAdenine(), 2d, CompartmentImplementation.CYTOPLASM);
         r.addProduct(MoleculeFactory.makeBenzene(), 2d, CompartmentImplementation.MITOCHONDRIAL_MEMBRANE);
         r.addProduct(MoleculeFactory.makeCyclohexane(), 1d, CompartmentImplementation.CYTOPLASM);
         r.addProduct(butan1ol(), 2d, CompartmentImplementation.CYTOPLASM);
 
 //        System.out.printf( "\t\tq: %10s %s\n" , q1.hashCode() , q1 );
 //        System.out.printf( "\t\tr: %10s %s\n" , r.hashCode() , r );
 
         // hash code doesn't need to be the same
         assertNotSame("FAILED", q1, r);
 
         System.out.println("PASSED");
     }
 
 
     @Test
     public void testEqualsDiffComp() {
 
         System.out.printf("%-120s", "[TEST] Different participants (compartments)");
 
         // one molecule with different compartment
         AtomContainerReaction r = new AtomContainerReaction();
         r.addReactant(_123Triazole(), 2d, CompartmentImplementation.CYTOPLASM); // different compartment
         r.addReactant(adenine(), 2d, CompartmentImplementation.CYTOPLASM);
         r.addProduct(benzene(), 2d, CompartmentImplementation.MITOCHONDRIAL_MEMBRANE);
         r.addProduct(cyclohexane(), 1d, CompartmentImplementation.CYTOPLASM);
         r.addProduct(butan1ol(), 2d, CompartmentImplementation.CYTOPLASM);
 
         //  System.out.printf( "\t\tq: %10s %s\n" , q1.hashCode() , q1 );
         //  System.out.printf( "\t\tr: %10s %s\n" , r.hashCode() , r );
 
         // hash code doesn't need to be the same
         assertNotSame("FAILED", q1, r);
 
         System.out.println("PASSED");
 
     }
 
 
     @Test
     public void testEqualsTransComp() {
 
         System.out.printf("%-120s", "[TEST] Transposed compartments");
 
         AtomContainerReaction r = new AtomContainerReaction();
         r.addReactant(adenine(), 1d, CompartmentImplementation.CYTOPLASM);
         r.addReactant(cyclohexane(), 1d, CompartmentImplementation.EXTRACELLULA);
         r.addProduct(adenine(), 1d, CompartmentImplementation.EXTRACELLULA);
         r.addProduct(cyclohexane(), 1d, CompartmentImplementation.CYTOPLASM);
 
 //        System.out.printf( "\t\treactans: %10s %s\n" , r.getReactantParticipants().hashCode() , StringUtils.join( r.
 //                getReactantParticipants() , " + " ) );
 //        System.out.printf( "\t\tproducts: %10s %s\n" , r.getProductParticipants().hashCode() , StringUtils.join( r.
 //                getProductParticipants() , " + " ) );
 
         // Make sure the hashCode for reactants and products is different
         assertNotSame("FAILED", r.getReactantParticipants().hashCode(), r.getProductParticipants().hashCode());
 
         System.out.println("PASSED");
 
     }
 
 
     @Test
     public void testEqualsMissingCoefComp() {
         System.out.printf("%-120s", "[TEST] Testing absent coefficients/compartments");
 
         /** only with products no coef or compartments **/
         // query
         AtomContainerReaction q2 = new AtomContainerReaction();
         q2.addReactant(_123Triazole(), null, null);
         q2.addReactant(adenine(), null, null);
         q2.addProduct(benzene(), null, null);
         q2.addProduct(cyclohexane(), null, null);
         q2.addProduct(butan1ol(), null, null);
 
         // different sides
         AtomContainerReaction r = new AtomContainerReaction();
         r.addReactant(benzene(), null, null);
         r.addReactant(cyclohexane(), null, null);
         r.addReactant(butan1ol(), null, null);
         r.addProduct(_123Triazole(), null, null);
         r.addProduct(adenine(), null, null);
 
         //     System.out.printf( "\t\tq: %10s %s\n" , q2.hashCode() , q2 );
         //     System.out.printf( "\t\tr: %10s %s\n" , r.hashCode() , r );
 
 
         assertEquals("FAILED", q2.hashCode(), r.hashCode());
         assertEquals("FAILED", q2, r);
 
         System.out.println("PASSED");
     }
 
 
     @Test
     public void testEqualsDiffBonds() {
 
         System.out.printf("%-120s", "[TEST] Testing same connectivity diff bonds (but-1-ene ≠ butane)");
 
         /** only with products no coef or compartments **/
         // query
         AtomContainerReaction q2 = new AtomContainerReaction();
         q2.addReactant(_123Triazole(), null, null);
         q2.addReactant(but1ene(), null, null);
         q2.addProduct(benzene(), null, null);
         q2.addProduct(cyclohexane(), null, null);
 
         // different sides
         AtomContainerReaction r = new AtomContainerReaction();
         r.addReactant(_123Triazole(), null, null);
         r.addReactant(butane(), null, null);
         r.addReactant(benzene(), null, null);
         r.addProduct(cyclohexane(), null, null);
 
         //   System.out.printf( "\t\tq: %10s %s\n" , q2.hashCode() , q2 );
         //   System.out.printf( "\t\tr: %10s %s\n" , r.hashCode() , r );
 
         assertNotSame("FAILED", q2, r);
 
         System.out.println("PASSED");
     }
 
 
     @Test
     public void testGenericADH() {
         System.out.printf("%-120s", "[TEST] Testing generic reaction Alcohol Dehydrogenase");
 
 //        Logger.getLogger(GenericParticipant.class).setLevel(Level.ALL);
 //        Logger.getLogger(AtomContainerParticipant.class).setLevel(Level.ALL);
 //        Logger.getLogger(ParticipantImplementation.class).setLevel(Level.ALL);
 
         /** only with products no coef or compartments **/
         // query
         AtomContainerReaction generic = new AtomContainerReaction();
         generic.addReactant(primary_alcohol(), null, null);
         generic.addReactant(new AtomContainerParticipant(nad_plus()));
         generic.addProduct(aldehyde(), null, null);
         generic.addProduct(new AtomContainerParticipant(hydron()));
         generic.addProduct(new AtomContainerParticipant(nadh()));
 
         // different sides
         AtomContainerReaction r = new AtomContainerReaction();
         r.addReactant(new AtomContainerParticipant(_5bcholestane3a7a26triol()));
         r.addReactant(new AtomContainerParticipant(nad_plus()));
         r.addProduct(new AtomContainerParticipant(_3a7adihydroxy5Bcholestan26al()));
         r.addProduct(new AtomContainerParticipant(hydron()));
         r.addProduct(new AtomContainerParticipant(nadh()));
 
 //        System.out.printf( "\t\tq: %10s %s\n" , generic.hashCode() , generic );
 //        System.out.printf( "\t\tr: %10s %s\n" , r.hashCode() , r );
 
         assertEquals("FAILED", generic, r);
 
         System.out.println("PASSED");
     }
 }
