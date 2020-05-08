 package tailor.engine;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 import tailor.condition.DistanceBoundCondition;
 import tailor.datasource.PDBFileList;
 import tailor.datasource.Structure;
 import tailor.datasource.StructureSource;
 import tailor.description.ChainDescription;
 import tailor.description.Description;
 import tailor.description.DescriptionException;
 import tailor.description.DescriptionFactory;
 import tailor.description.ProteinDescription;
 import tailor.measure.Measure;
 import tailor.measure.Measurement;
 
 public class SingleChainTest {
     
     @Test
     public void simpleTest() throws IOException, DescriptionException {
         String filename = "structures/2bop.pdb";
         
         DescriptionFactory factory = new DescriptionFactory();
         factory.addResidues(4);
         
         Description description = factory.getProduct(); 
         
         ChainDescription chainD = 
             (ChainDescription) description.getSubDescriptionAt(0);
         chainD.addMeasure(factory.createPhiMeasure("psi2", 2));
         chainD.addMeasure(factory.createPsiMeasure("phi2", 2));
         
         SingleChainEngine engine = new SingleChainEngine();
         StructureSource structureSource = new PDBFileList(filename, null);
         while (structureSource.hasNext()) {
             Structure structure = structureSource.next();
             for (Structure chain : structure) {
                 for (Match match : engine.match(chainD, chain)) {
                     System.out.print(match + " ");
                     for (Measure measure : chainD.getMeasures()) {
                         Measurement measurement = measure.measure(match);
                         System.out.print(measurement + ", ");
                     }
                     System.out.println();
                 }
             }
         }
     }
     
     @Test
     public void simpleHBondTest() {
         String filename = "structures/2bop.pdb";
         
         DescriptionFactory factory = new DescriptionFactory();
         factory.addResidues(4);
         ChainDescription carbonylOxygen = 
             factory.getChainDescription("A").getPath(0, "O");
         ChainDescription amineNitrogen = 
             factory.getChainDescription("A").getPath(3, "N");
         factory.getChainDescription("A").addCondition(
                 new DistanceBoundCondition(
                        "i.O->(i+3).N", carbonylOxygen, amineNitrogen, 3.5, 1));
         
        Description description = factory.getProduct(); 
         description.addMeasure(factory.createPhiMeasure("psi2", 2));
         description.addMeasure(factory.createPsiMeasure("phi2", 2));
         
         Run run = new Run(filename);
         run.addDescription((ProteinDescription)description);
         
         Engine engine = EngineFactory.getEngine(description);
         engine.run(run);
     }
     
     @Test
     public void geometricHBondTest() {
         String filename = "structures/1a2p.pdb";
         
         DescriptionFactory factory = new DescriptionFactory();
         factory.setAddBackboneAmineHydrogens(true);
         factory.addResidues(5);
         // i.O->(i+4).N
         factory.createHBondCondition(3.5, 90, 90, 4, 0);
         
         Description description = factory.getProduct();
         description.addMeasure(factory.createPhiMeasure("psi2", 2));
         description.addMeasure(factory.createPsiMeasure("phi2", 2));
         
         Run run = new Run(filename);
         run.addDescription((ProteinDescription)description);
         
         Engine engine = EngineFactory.getEngine(description);
         engine.run(run);
     }
 
 
 }
