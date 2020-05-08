 package pl.poznan.put.cs.bioserver.comparison;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.biojava.bio.structure.Atom;
 import org.biojava.bio.structure.Calc;
 import org.biojava.bio.structure.SVDSuperimposer;
 import org.biojava.bio.structure.Structure;
 import org.biojava.bio.structure.StructureException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pl.poznan.put.cs.bioserver.alignment.AlignmentOutput;
 import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
 import pl.poznan.put.cs.bioserver.helper.Helper;
 import pl.poznan.put.cs.bioserver.helper.PdbManager;
 
 /**
  * Implementation of RMSD global similarity measure.
  * 
  * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
  */
 public class RMSD extends GlobalComparison {
     private static final Logger LOGGER = LoggerFactory.getLogger(RMSD.class);
 
     /**
      * A command line wrapper to calculate RMSD for given structures. It outputs
      * the upper half of the dissimilarity matrix. For example, for 4 structures
      * the output will like this:
      * 
      * OK 1-vs-2 1-vs-3 1-vs-4 2-vs-3 2-vs-4 3-vs-4
      * 
      * @param args
      *            A list of paths to PDB files.
      */
     public static void main(String[] args) {
         if (args.length < 2) {
             System.out.println("ERROR");
             System.out.println("Incorrect number of arguments provided");
             return;
         }
         List<Structure> list = new ArrayList<>();
         for (String arg : args) {
             list.add(PdbManager.loadStructure(new File(arg)));
         }
 
         RMSD rmsd = new RMSD();
         double[][] compare = rmsd.compare(
                 list.toArray(new Structure[list.size()]), null);
         System.out.println("OK");
         for (double[] element : compare) {
             System.out.println(Arrays.toString(element));
         }
     }
 
     /**
      * Compare two given structures. By default, do not try to align based on
      * atoms, but if impossible to compare then try the alignment.
      * 
      * @param s1
      *            First structure.
      * @param s2
      *            Second structure.
      * @return RMSD.
      */
     @Override
     public double compare(Structure s1, Structure s2)
             throws IncomparableStructuresException {
         RMSD.LOGGER.debug("Comparing: " + s1.getPDBCode() + " and "
                 + s2.getPDBCode());
 
         if (Helper.isNucleicAcid(s1) != Helper.isNucleicAcid(s2)) {
             return Double.NaN;
         }
 
         try {
             Structure[] structures = new Structure[] { s1.clone(), s2.clone() };
             Atom[][] atoms = Helper.getCommonAtomArray(structures[0],
                     structures[1], false);
             if (atoms == null || atoms[0].length != atoms[1].length) {
                 RMSD.LOGGER.info("Atom sets have different sizes. Must use "
                         + "alignment before calculating RMSD");
                AlignmentOutput output = StructureAligner.align(structures[0],
                        structures[1]);
                 return output.getAFPChain().getTotalRmsdOpt();
             }
 
             RMSD.LOGGER.debug("Atom set size: " + atoms[0].length);
             SVDSuperimposer superimposer = new SVDSuperimposer(atoms[0],
                     atoms[1]);
             Calc.rotate(structures[1], superimposer.getRotation());
             Calc.shift(structures[1], superimposer.getTranslation());
             return SVDSuperimposer.getRMS(atoms[0], atoms[1]);
         } catch (StructureException e) {
             RMSD.LOGGER.error("Failed to compare structures", e);
             throw new IncomparableStructuresException(e);
         }
     }
 }
