 package tailor.engine;
 
 import org.junit.Test;
 
 import tailor.condition.DistanceBoundCondition;
 import tailor.description.AtomDescription;
 import tailor.description.ChainDescription;
 import tailor.description.Description;
 import tailor.description.DescriptionFactory;
 import tailor.description.GroupDescription;
 import tailor.description.ProteinDescription;
 
 public class CatmatTest {
     
     @Test
     public void catmatThree() {
         String filename = "structures/2bop.pdb";
         
         DescriptionFactory factory = new DescriptionFactory();
         factory.addResidues(4);
         ChainDescription carbonylOxygenI = 
             factory.getChainDescription("A").getPath(0, "O");
         ChainDescription carbonylOxygenIPlusTwo = 
             factory.getChainDescription("A").getPath(2, "O");
         ChainDescription waterChain = new ChainDescription("W");
         GroupDescription water = new GroupDescription("HOH");
         waterChain.addGroupDescription(water);
         AtomDescription waterOxygen = new AtomDescription("O");
         water.addAtomDescription(waterOxygen);
         
         Description description = factory.getProduct();
         description.addSubDescription(waterChain);
         description.addCondition(
                 new DistanceBoundCondition(
                        "i.O-W", carbonylOxygenI, waterOxygen, 3.5, 1));
         description.addCondition(
                 new DistanceBoundCondition(
                        "i+2.O-W", carbonylOxygenIPlusTwo, waterOxygen, 3.5, 1));
         description.addMeasure(factory.createPhiMeasure("phi2", 2));
         description.addMeasure(factory.createPhiMeasure("psi2", 2));
         Run run = new Run(filename);
         run.addDescription((ProteinDescription)description);
         
         Engine engine = EngineFactory.getEngine(description);
         engine.run(run);
 
     }
 
 }
