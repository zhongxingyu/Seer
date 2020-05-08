 
 package tajmi.instances.cdk.som;
 
 import org.junit.Test;
 
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.Molecule;
 import tajmi.som.Field;
 
 import java.util.*;
 import tajmi.abstracts.som.InitFunc;
 import tajmi.abstracts.som.NeighborhoodFunc;
 import tajmi.instances.som.Gaussian2DNeighborhoodFunc;
 import tajmi.som.Position;
 
 /**
  *
  * @author badi
  */
 public class AtomContainerUpdateFuncTest {
 
     @Test
     public void AtomContainerUpdateFunc() {
         IAtomContainer atom = new Molecule();
 
        List<IAtomContainer> input_data = new LinkedList();
         for (int i = 0; i < 9; i++)
             input_data.add(atom);
 
         Random rand = new Random(42);
 
         InitFunc initf = new InitFunc() {
 
             @Override
             public List<IAtomContainer> call() {
                return getSeed();
             }
         };
 
         initf.params(input_data, rand);
         Field field = new Field(10, 1, initf);
 
         NeighborhoodFunc neighbourhoodf = new Gaussian2DNeighborhoodFunc();
         new AtomContainerUpdateFunc().params(field, atom, new Position(0, 0), 1.0, neighbourhoodf).call();
 
         System.out.println("AtomContainerUpdateFuncTest");
     }
 
 }
