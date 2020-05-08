 package tajmi.functional.instances.cdk;
 
 import tajmi.functional.interfaces.Fun;
 import org.openscience.cdk.Molecule;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.io.IChemObjectWriter;
 import org.openscience.cdk.io.WriterFactory;
 import org.openscience.cdk.io.formats.IChemFormat;
 import java.io.BufferedOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 
 /**
 * <code> MoleculeWriter :: IChemFormat format -> String filename -> IAtomContainer molecule -> IO Void </code>
  * @author badi
  */
 public class MoleculeWriter implements Fun {
 
     String filename;
     IChemFormat format;
     IAtomContainer molecule;
 
     public Fun copy() {
         return new MoleculeWriter().curry(filename).curry(format).curry(molecule);
     }
 
     public Fun curry(Object arg) {
        if (format == null) {
             format = (IChemFormat) arg;
        } else if (filename == null) {
            filename = (String) arg;
         } else if (molecule == null) {
             molecule = (IAtomContainer) arg;
         }
 
         return this;
     }
 
     public Void call() throws CDKException, FileNotFoundException {
 
         IChemObjectWriter writer = new WriterFactory().createWriter(format);
         writer.setWriter(new BufferedOutputStream(new FileOutputStream(filename + "." + format.getPreferredNameExtension())));
         writer.write(new Molecule(molecule));
 
         return null;
     }
 }
