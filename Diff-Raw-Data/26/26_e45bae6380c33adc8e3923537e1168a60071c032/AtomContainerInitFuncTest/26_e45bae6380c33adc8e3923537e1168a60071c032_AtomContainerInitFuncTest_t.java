 
 package tajmi.instances.cdk.som;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import org.junit.Test;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import tajmi.Universe;
 import tajmi.Util;
 import tajmi.abstracts.som.InitFunc;
 
 /**
  *
  * @author badi
  */
 public class AtomContainerInitFuncTest {
 
     @Test
     public void AtomContainerInitFuncTest() throws FileNotFoundException, IOException, CDKException {
 
         final int POINTS = 2;
         final String DATA_DIR = "test-data";
         File dir = new File(DATA_DIR + File.separator + "hiv1-inhibitors");
         File[] files = dir.listFiles(new FileFilter() {
 
             int chosen = 0;
             public boolean accept(File pathname) {
                 if(chosen < POINTS){
                     chosen++;
                     return pathname.getName().matches("\\w*\\.mol2");
                 } else return false;
             }
         });
 
         List<IAtomContainer> molecs = new LinkedList<IAtomContainer>();
         for (File f : files) {
             IAtomContainer c = Util.readMoleculeFile(f.getAbsolutePath());
             c.setID(f.getName().split(".mol2")[0]);
             molecs.add(c);
         }
 
         for (IAtomContainer c : molecs)
             System.out.print(c.getID() + " ");
         System.out.println();
 
 
         Random r = new Random(42);
         InitFunc initf = new AtomContainerInitFunc().params(molecs, r);
 
         List<IAtomContainer> field = new LinkedList<IAtomContainer>();
        for (int i = 0; i < 100; i++) {
             if (i % 1 == 0)
                 System.out.print(i + " ");
             IAtomContainer c = (IAtomContainer) ((List) initf.call()).get(0);
             field.add(c);
         }
        System.out.println();
 
         for (IAtomContainer c : field){
            System.out.println(c.getID());
            for (IAtomContainer orig : molecs)
                System.out.println("\t" + UniversalIsomorphismTester.isSubgraph(c, orig));
//            System.out.println(c.getID());
         }
         System.out.println();
         System.out.println("AtomContainerInitFuncTest");
     }
 
 
 }
