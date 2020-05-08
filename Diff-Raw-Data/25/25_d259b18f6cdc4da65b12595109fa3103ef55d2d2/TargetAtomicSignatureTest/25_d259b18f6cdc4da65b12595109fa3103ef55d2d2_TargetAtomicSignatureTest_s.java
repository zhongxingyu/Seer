 package test_signature;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.interfaces.IAtom;
 import org.openscience.cdk.interfaces.IBond;
 import org.openscience.cdk.interfaces.IChemObjectBuilder;
 import org.openscience.cdk.interfaces.IMolecule;
 import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
 import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
 import org.openscience.cdk.smiles.SmilesGenerator;
 import org.openscience.cdk.templates.MoleculeFactory;
 
 import org.openscience.cdk.signature.AtomSignature;
 import org.openscience.cdk.signature.MoleculeSignature;
 import org.openscience.cdk.signature.TargetAtomicSignature;
 
 public class TargetAtomicSignatureTest extends AbstractSignatureTest {
     
     public static IChemObjectBuilder builder =
         NoNotificationChemObjectBuilder.getInstance();
     
     public static void addHydrogens(IMolecule mol, IAtom atom, int n) {
         for (int i = 0; i < n; i++) {
             IAtom h = builder.newAtom("H");
             mol.addAtom(h);
             mol.addBond(builder.newBond(atom, h));
         }
     }
     
     public static IMolecule makeTreeMolecule() {
         IMolecule propane = MoleculeFactory.makeAlkane(3);
         TargetAtomicSignatureTest.addHydrogens(propane, propane.getAtom(0), 3);
         TargetAtomicSignatureTest.addHydrogens(propane, propane.getAtom(1), 2);
         TargetAtomicSignatureTest.addHydrogens(propane, propane.getAtom(2), 3);
         return propane;
     }
     
     public static IMolecule makeRingMolecule() {
         IMolecule cyclohexane = MoleculeFactory.makeCyclohexane();
         for (int i = 0; i < 6; i++) {
             TargetAtomicSignatureTest.addHydrogens(
                     cyclohexane, cyclohexane.getAtom(i), 2);
         }
         return cyclohexane;
     }
     
   
     
     public static void printSmiles(IMolecule mol) {
         SmilesGenerator generator = new SmilesGenerator();
         System.out.println(generator.createSMILES(mol));
     }
     
     public static void checkMolecule(String sigString, IMolecule expected) {
         TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
         IMolecule actual = sig.toMolecule();
         try {
             boolean isIsomorph = 
                 UniversalIsomorphismTester.isIsomorph(expected, actual);
             Assert.assertEquals(true, isIsomorph);
         } catch (CDKException c) {
             
         }
     }
     
     public static void testRoundtrip(String expected) {
         TargetAtomicSignature sig = new TargetAtomicSignature(expected);
         String actual = sig.toString();
         Assert.assertEquals("labelled roundtrip failed", expected, actual);
     }
     
     @Test
     public void reconstructingMultipleBondTargetSignature() {
         String signature = "[C]([C](=[C][C,1])=[C,1]([H])[H])";
         TargetAtomicSignature tas = new TargetAtomicSignature(signature);
         IMolecule mol = tas.toMolecule();
         print(mol);
     }
     
     @Test
     public void multipleBondTest() {
         String signatureString = "[C](=[C]([C,1])[C,1](=[C,1]))";
         TargetAtomicSignature tas = new TargetAtomicSignature(signatureString);
         Assert.assertEquals(signatureString, tas.toString());
     }
     
     @Test
     public void multipleBondSubSignaturesTest() {
         String signatureString = "[C]([C](=[C][C,1])=[C,1]([H])[H])";
         TargetAtomicSignature tas = new TargetAtomicSignature(signatureString);
         List<String> subsignatures = tas.getSignatureStringsFromRootChildren(1);
         for (String subSignature : subsignatures) {
             System.out.println(subSignature);
         }
     }
     
     @Test
     public void paperTest() {
         // the example shown in Figure 4 of Faulon's enumeration paper
         IMolecule paperExample = builder.newMolecule();
         IAtom carbon1 = builder.newAtom("C");
         paperExample.addAtom(carbon1);
         IAtom carbon2 = builder.newAtom("C");
         paperExample.addAtom(carbon2);
         IAtom carbon3 = builder.newAtom("C");
         paperExample.addAtom(carbon3);
         IAtom carbon4 = builder.newAtom("C");
         paperExample.addAtom(carbon4);
         paperExample.addBond(0, 1, IBond.Order.SINGLE);
         paperExample.addBond(1, 2, IBond.Order.SINGLE);
         paperExample.addBond(2, 3, IBond.Order.SINGLE);
         TargetAtomicSignatureTest.addHydrogens(paperExample, carbon1, 3);
         TargetAtomicSignatureTest.addHydrogens(paperExample, carbon2, 2);
         TargetAtomicSignatureTest.addHydrogens(paperExample, carbon3, 2);
         
         // check that the height-2 signature is correct
         String height2Signature = "[C]([C]([C][H][H])[C]([H][H][H])[H][H])";
         AtomSignature signature = new AtomSignature(1, 2, paperExample);
         String canonicalSignatureString = signature.toCanonicalString(); 
         Assert.assertEquals(height2Signature, canonicalSignatureString);
         
         // check the signatures formed from the children of the root
         String[] subsignatures = new String[] {"[C]([C][C][H][H])",
                 "[C]([C][H][H][H])", "[H]([C])", "[H]([C])" };
         TargetAtomicSignature targetSignature = 
             new TargetAtomicSignature(height2Signature);
         List<String> subsignatureList = 
             targetSignature.getSignatureStringsFromRootChildren(1); 
         for (String subsignature : subsignatures) {
             Assert.assertTrue(subsignatureList.contains(subsignature));
         }
         
     }
     
     @Test
     public void testCageMolecule() {
         IMolecule molecule = AbstractSignatureTest.makeCage();
         String sigString = "[C]([C]([C,2]([C]([C,3][C,4]))[C]([C,5]" +
                            "[C,3]([C,6]([C,1]))))[C]([C]([C,7][C]" +
                            "([C,1][C,8]))[C,5]([C,8]([C,6])))[C]([C,2]" +
                            "[C,7]([C,4]([C,1]))))";
         TargetAtomicSignatureTest.testRoundtrip(sigString);
         TargetAtomicSignatureTest.checkMolecule(sigString, molecule);
     }
     
     @Test
     public void testTreeMolecule() {
          IMolecule molecule = TargetAtomicSignatureTest.makeTreeMolecule();
          String sigString = "[C]([H][H][H][C]([H][H][C]([H][H][H])))";
          TargetAtomicSignatureTest.checkMolecule(sigString, molecule);
     }
     
     @Test
     public void testCyclicMolecule() {
          IMolecule molecule = TargetAtomicSignatureTest.makeRingMolecule();
          String sigString = "[H]([C]([C]([C]([C,1]([H][H])[H][H])[H][H])" +
                             "[C]([C]([C,1][H][H])[H][H])[H]))";
          TargetAtomicSignatureTest.checkMolecule(sigString, molecule);
     }
     
     @Test
     public void labelledRoundtrip() {
         String expected = "[C]([C,1][C,2][C,3])";
         TargetAtomicSignatureTest.testRoundtrip(expected);
     }
     
     @Test
     public void roundtrip() {
         String expected = "[C]([H][C]([H][H][H])[C]([H][H][C])[C]([H][C][C]))";
         TargetAtomicSignature sig = new TargetAtomicSignature(expected);
         String actual = sig.toString();
         Assert.assertEquals("roundtrip failed", expected, actual);
     }
     
     @Test
     public void signatureStringsFromRootChildren() {
         // cubane
         String sigString = "[C]([C]([C,2]([C,3])[C,4]([C,3]))" +
                            "[C]([C,1]([C,3])[C,4])[C]([C,1][C,2]))";
         int height = 2;
         
         TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
         MoleculeSignature sigSig = new MoleculeSignature(sig.toMolecule());
         String expected = sigSig.toCanonicalSignatureString(height);
         // every sub-signature of height 2 for cubane's children of the
         // root node is equal to the subsignature of height 2 for cubane
         for (String actual : sig.getSignatureStringsFromRootChildren(height)) {
             Assert.assertEquals(expected, actual);
         }
     }
     
     @Test
     public void subSignatureFromRootChild() {
         String sigString = "[A]([B]([C]([D]))[E]([F]([G])[H]))";
         TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
         int child = 0;
         int height = 2;
         String expected = "[B]([C]([D])[A]([E]))";
         String actual = sig.getSignatureString(child, height);
 
         Assert.assertEquals(expected, actual);
     }
     
     @Test
     public void subSignature() {
         String sigString = "[A]([B]([C]([D]))[E]([F]([G])[H]))";
         TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
         int height = 2;
         String expected = "[A]([B]([C])[E]([F][H]))";
         String actual = sig.getSubSignature(height);
         
         Assert.assertEquals(expected, actual);
     }
 
 }
