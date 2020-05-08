 package tailor.description;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 import tailor.condition.DistanceBoundCondition;
 import tailor.datasource.PDBFileList;
 import tailor.datasource.Structure;
 import tailor.engine.Match;
 import tailor.engine.SingleChainEngine;
 import tailor.measure.DistanceMeasure;
 
 public class DescriptionFactoryTests {
     
     public void setupFactory(DescriptionFactory factory) {
         factory.addChainToProtein("A");
         
         factory.addResidueToChain("A", "GLY");
         factory.addResidueToChain("A", "LYS");
         
         factory.addAtomToResidue("A", 0, "N");
         factory.addAtomToResidue("A", 1, "O");
     }
     
     @Test
     public void makeProduct() {
         DescriptionFactory factory = new DescriptionFactory("Motif");
         setupFactory(factory);
         
         ProteinDescription description = factory.getProduct();
         ProteinDescription a = description.getPath("A", "GLY", "N");
         ProteinDescription b = description.getPath("A", "LYS", "O");
         
         System.out.println(a.toPathString());
         System.out.println(b.toPathString());
     }
     
     @Test
     public void makeBasicCondition() {
         DescriptionFactory factory = new DescriptionFactory("Motif");
         setupFactory(factory);
         
         ProteinDescription description = factory.getProduct();
         
         ChainDescription chain = description.getChainDescription("A");
         ChainDescription a = chain.getPath("GLY", "N");
         ChainDescription b = chain.getPath("LYS", "O");
         
         DistanceBoundCondition bound = 
             new DistanceBoundCondition("NODistance", a, b, 4, 2);
         chain.addGroupCondition(bound);
         
         DistanceMeasure measure = new DistanceMeasure(a, b);
         
         // TODO : factor below code into separate test? 
         String path = "structures/1a2p.pdb";
         try {
             PDBFileList fileList = new PDBFileList(path, new String[]{});
             while (fileList.hasNext()) {
                 Structure structure = fileList.next();
                 System.err.println("Structure " + structure.getId());
                 SingleChainEngine engine = new SingleChainEngine();
                Structure chainA = structure.getSubStructureAtIndex(0);
                for (Match match : engine.scan(chain, chainA)) {
                     System.out.println(match.toString() 
                             + " "  + measure.measure(match));
                 }
             }
         } catch (IOException ioe) {
             System.err.println(ioe);
         } catch (DescriptionException de) {
             System.err.println(de);
         }
     }
 
 }
