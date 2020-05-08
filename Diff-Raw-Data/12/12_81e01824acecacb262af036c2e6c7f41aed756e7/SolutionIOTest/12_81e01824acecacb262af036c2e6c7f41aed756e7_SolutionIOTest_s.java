 package org.drools.planner.examples.ras2012.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 
 import org.drools.planner.core.score.buildin.hardandsoft.DefaultHardAndSoftScore;
 import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
 import org.drools.planner.examples.ras2012.ProblemSolution;
 import org.drools.planner.examples.ras2012.ScoreCalculator;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 @RunWith(Parameterized.class)
 public class SolutionIOTest {
 
     private static enum SolutionType {
 
         RDS1("/org/drools/planner/examples/ras2012/parser/validParserInput1.txt"), RDS2(
                 "/org/drools/planner/examples/ras2012/parser/validParserInput2.txt"), RDS3(
                 "/org/drools/planner/examples/ras2012/parser/validParserInput3.txt"), TOY(
                 "/org/drools/planner/examples/ras2012/parser/validParserInput4.txt");
 
         private final String definition;
 
         SolutionType(final String definition) {
             this.definition = definition;
         }
 
         public InputStream getDefinition() {
             return SolutionIOTest.class.getResourceAsStream(this.definition);
         }
     }
 
     private static Object[] getResource(final SolutionType solutionType, final int hard,
             final int soft) {
         return new Object[] { solutionType.getDefinition(), solutionType.name() + soft + ".xml",
                 hard, soft };
     }
 
     @Parameters
     public static Collection<Object[]> getResources() {
         final List<Object[]> resources = new LinkedList<>();
        resources.add(SolutionIOTest.getResource(SolutionType.RDS1, 0, -1829));
        resources.add(SolutionIOTest.getResource(SolutionType.RDS1, 0, -5355));
        resources.add(SolutionIOTest.getResource(SolutionType.RDS2, 0, -8255));
        resources.add(SolutionIOTest.getResource(SolutionType.RDS2, 0, -13728));
        resources.add(SolutionIOTest.getResource(SolutionType.RDS3, 0, -11063));
        resources.add(SolutionIOTest.getResource(SolutionType.RDS3, 0, -16108));
         return resources;
     }
 
     private final InputStream      definition;
     private final String           solutionResource;
     private final HardAndSoftScore score;
 
     private static final File      parent = new File("data/tests/"
                                                   + SolutionIOTest.class.getCanonicalName());
 
     public SolutionIOTest(final InputStream solutionDefinition, final String solution,
             final int hardScore, final int softScore) {
         this.definition = solutionDefinition;
         this.solutionResource = solution;
         this.score = DefaultHardAndSoftScore.valueOf(hardScore, softScore);
     }
 
     @SuppressWarnings("resource")
     @Test
     public void testSolutionValidation() throws IOException {
         final SolutionIO io = new SolutionIO();
         final ProblemSolution result = io.read(this.definition,
                 SolutionIOTest.class.getResourceAsStream(this.solutionResource));
         if (!SolutionIOTest.parent.exists()) {
             SolutionIOTest.parent.mkdirs();
         }
         // compare XML results
         final ByteArrayOutputStream baos = new ByteArrayOutputStream();
         io.writeXML(result, baos);
         final String oldContent = new Scanner(
                 SolutionIOTest.class.getResourceAsStream(this.solutionResource))
                 .useDelimiter("\\A").next();
         final String newContent = baos.toString();
         final File f = new File(SolutionIOTest.parent, result.getName() + this.score.getSoftScore()
                 + ".xml");
         io.writeXML(result, f);
         Assert.assertTrue("XML files do not match. Check " + f, newContent.equals(oldContent));
         // compare scores
         Assert.assertEquals("Scores don't match.", this.score,
                 ScoreCalculator.oneTimeCalculation(result));
     }
 }
