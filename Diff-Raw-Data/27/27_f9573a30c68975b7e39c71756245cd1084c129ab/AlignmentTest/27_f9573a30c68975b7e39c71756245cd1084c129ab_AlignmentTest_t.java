 package pl.poznan.put.cs.bioserver.sandbox;
 
 import java.io.IOException;
import java.util.Arrays;
 import java.util.List;
 
 import org.biojava.bio.structure.Atom;
 import org.biojava.bio.structure.Structure;
 import org.biojava.bio.structure.StructureException;
 import org.biojava.bio.structure.align.StrucAligParameters;
 import org.biojava.bio.structure.align.StructureAlignment;
 import org.biojava.bio.structure.align.StructurePairAligner;
 import org.biojava.bio.structure.align.ce.CeMain;
 import org.biojava.bio.structure.align.fatcat.FatCatRigid;
 import org.biojava.bio.structure.align.model.AFPChain;
 import org.biojava.bio.structure.io.PDBFileReader;
 
 import pl.poznan.put.cs.bioserver.helper.Helper;
 
 public class AlignmentTest {
     public static void main(String[] args) {
         try {
             String p1 = "/home/tzok/pdb/puzzles/Challenge1/models/1_bujnicki_1.pdb";
             String p2 = "/home/tzok/pdb/puzzles/Challenge1/models/1_major_1.pdb";
 
             p1 = "/home/tzok/pdb/3CFM.pdb";
             p2 = "/home/tzok/pdb/3I9I.pdb";
 
             PDBFileReader reader = new PDBFileReader();
             Structure s1 = reader.getStructure(p1);
             Structure s2 = reader.getStructure(p2);
 
             Atom[][] atoms = new Atom[2][];
             // atoms = Helper.getCommonAtomArray(s1, s2, false);
             // if (atoms == null) {
             // atoms = Helper.getCommonAtomArray(s1, s2, true);
             // }
 
             List<Atom> list;
             list = Helper.getAtomArray(s1, new String[] { "P", "CA" });
             for (Atom a : list) {
                 if (a.getName().equals("P")) {
                     a.setName("CA");
                     a.setFullName(" CA ");
                 }
             }
             atoms[0] = list.toArray(new Atom[list.size()]);
 
             list = Helper.getAtomArray(s2, new String[] { "P", "CA" });
             for (Atom a : list) {
                 if (a.getName().equals("P")) {
                     a.setName("CA");
                     a.setFullName(" CA ");
                 }
             }
             atoms[1] = list.toArray(new Atom[list.size()]);
 
             AFPChain align;
             StructureAlignment ce = new CeMain();
             StructureAlignment fatcat = new FatCatRigid();
 
             long start;
             start = System.currentTimeMillis();
             align = ce.align(atoms[0], atoms[1]);
             System.out.println(align.toCE(atoms[0], atoms[1]));
             System.out.println(align.toFatcat(atoms[0], atoms[1]));
             System.out.println("TIME=" + (System.currentTimeMillis() - start));
 
             start = System.currentTimeMillis();
             align = fatcat.align(atoms[0], atoms[1]);
             System.out.println(align.toCE(atoms[0], atoms[1]));
             System.out.println(align.toFatcat(atoms[0], atoms[1]));
             System.out.println("TIME=" + (System.currentTimeMillis() - start));
 
             StructurePairAligner aligner = new StructurePairAligner();
             start = System.currentTimeMillis();
             aligner.align(atoms[0], atoms[1], new StrucAligParameters());
            System.out.println(Arrays.toString(aligner.getAlignments()));
             System.out.println("TIME=" + (System.currentTimeMillis() - start));
         } catch (IOException | StructureException e) {
             e.printStackTrace();
         }
     }
 }
