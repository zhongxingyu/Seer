 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.ac.ebi.mdk.tool.domain.hash;
 
 import org.hamcrest.CoreMatchers;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openscience.cdk.DefaultChemObjectBuilder;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.interfaces.IMolecule;
 import org.openscience.cdk.io.MDLV2000Reader;
 import uk.ac.ebi.mdk.tool.domain.MolecularHashFactory;
 import uk.ac.ebi.mdk.tool.domain.TestMoleculeFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Set;
 
 
 /**
  * @author johnmay
  */
 public class StereoSeedTest {
 
     public StereoSeedTest() {
     }
 
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
 
     @Test
     public void testMoreComplexMolecules() {
 
         IAtomContainer nadgl6p = TestMoleculeFactory.loadMol(getClass(), "ChEBI_15784.mol", "nadgl6p");
         IAtomContainer nadgu6p = TestMoleculeFactory.loadMol(getClass(), "N-ACETYL-D-GALACTOSAMINE-6-PHOSPHATE.mol", "nadgu6p");
 
         MolecularHashFactory factory = MolecularHashFactory.getInstance();
 
         factory.setSeedMethods(SeedFactory.getInstance().getSeeds(AtomicNumberSeed.class,
                                                                   BondOrderSumSeed.class,
                                                                   ConnectedAtomSeed.class));
        Assert.assertThat(factory.getHash(nadgl6p), CoreMatchers.not(factory.getHash(nadgu6p)));
 
 
         factory.addSeedMethod(SeedFactory.getInstance().getSeed(StereoSeed.class));
 
 
         // was having trouble with the graphs being equal
         Assert.assertThat(factory.getHash(nadgl6p), CoreMatchers.not(factory.getHash(nadgu6p)));
 
     }
 
     @Test
     public void testSeed() {
 
         MolecularHashFactory factory = MolecularHashFactory.getInstance();
         factory.setSeedMethods(SeedFactory.getInstance().getSeeds(AtomicNumberSeed.class,
                                                                   BondOrderSumSeed.class,
                                                                   ConnectedAtomSeed.class));
 
         IAtomContainer lalanine = TestMoleculeFactory.lAlanine();
         IAtomContainer dalanine = TestMoleculeFactory.dAlanine();
 
         Assert.assertEquals(factory.getHash(dalanine), factory.getHash(lalanine));
 
         // add the chirality seed
         factory.addSeedMethod(SeedFactory.getInstance().getSeed(StereoSeed.class));
 
         Assert.assertThat(factory.getHash(dalanine), CoreMatchers.not(factory.getHash(lalanine)));
 
 
     }
 
 
     @Test
     public void testWithAlanine() throws Exception {
 
         MolecularHashFactory factory = MolecularHashFactory.getInstance();
 
         IAtomContainer lAlaUp = TestMoleculeFactory.loadMol("l-ala-up.mol", "L-Ala", Boolean.FALSE);
         IAtomContainer lAlaDown = TestMoleculeFactory.loadMol("l-ala-down.mol", "L-Ala", Boolean.FALSE);
 
 
         Set<AtomSeed> seeds = SeedFactory.getInstance().getSeeds(AtomicNumberSeed.class,
                                                                  ConnectedAtomSeed.class,
                                                                  BondOrderSumSeed.class,
                                                                  StereoSeed.class);
 
 
         Assert.assertEquals(factory.getHash(lAlaUp, seeds).hash, factory.getHash(lAlaDown, seeds).hash);
 
 
     }
 
 
     /**
      * Tests that when using the stereo-seed
      *
      * @throws CDKException
      * @throws IOException
      */
     @Test
     public void testStereoAlteration() throws CDKException, IOException {
 
         IMolecule mol1, mol2 = null;
 
         {
             InputStream stream = getClass().getResourceAsStream("C00129.mol");
             MDLV2000Reader reader = new MDLV2000Reader(stream);
             mol1 = reader.read(DefaultChemObjectBuilder.getInstance().newInstance(IMolecule.class));
             reader.close();
         }
         {
             InputStream stream = getClass().getResourceAsStream("C00235.mol");
             MDLV2000Reader reader = new MDLV2000Reader(stream);
             mol2 = reader.read(DefaultChemObjectBuilder.getInstance().newInstance(IMolecule.class));
             reader.close();
         }
         Assert.assertNotNull("Failed to loaded C00129.mol from resource", mol1);
         Assert.assertNotNull("Failed to loaded C00235.mol from resource", mol2);
 
 
         MolecularHashFactory factory = MolecularHashFactory.getInstance();
         Set<AtomSeed> seeds = SeedFactory.getInstance().getSeeds(AtomicNumberSeed.class,
                                                                  ConnectedAtomSeed.class,
                                                                  BondOrderSumSeed.class);
 
 
         Assert.assertNotSame(factory.getHash(mol1, seeds),
                              factory.getHash(mol2, seeds));
 
         seeds.add(SeedFactory.getInstance().getSeed(StereoSeed.class));
 
         Assert.assertNotSame(factory.getHash(mol1, seeds),
                              factory.getHash(mol2, seeds));
 
     }
 
 }
