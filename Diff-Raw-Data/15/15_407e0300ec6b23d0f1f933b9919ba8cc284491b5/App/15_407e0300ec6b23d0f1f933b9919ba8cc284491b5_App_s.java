 package org.drools.planner.examples.ras2012;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.drools.planner.config.XmlSolverFactory;
 import org.drools.planner.core.Solver;
 import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
 import org.drools.planner.examples.ras2012.util.SolutionIO;
 
 /**
  * Hello world!
  * 
  */
 public class App {
 
     public static void main(final String[] args) throws FileNotFoundException, IOException {
         // read solution
         final File f = new File("src/main/resources/org/drools/planner/examples/ras2012/RDS2.txt");
         final SolutionIO io = new SolutionIO();
         final RAS2012Solution sol = io.read(f);
         // and now start solving
         final XmlSolverFactory configurer = new XmlSolverFactory();
         configurer.configure(App.class.getResourceAsStream("/solverConfig.xml"));
         final Solver solver = configurer.buildSolver();
         solver.setPlanningProblem(sol);
         solver.solve();
         // output the solution
         final File targetFolder = new File("data/solutions");
         if (!targetFolder.exists()) {
             targetFolder.mkdirs();
         }
         final RAS2012Solution solution = (RAS2012Solution) solver.getBestSolution();
         final HardAndSoftScore score = App.recaculateScore(solution);
         if (score.getHardScore() >= 0) { // don't write score that isn't feasible
             io.writeXML(solution, new File(targetFolder, f.getName() + score.getSoftScore()
                     + ".xml"));
             io.writeTex(solution, new File(targetFolder, f.getName() + score.getSoftScore()
                     + ".tex"));
             solution.visualize(new File(targetFolder, f.getName() + ".png"));
         }
     }
 
     private static HardAndSoftScore recaculateScore(final RAS2012Solution solution) {
         final RAS2012ScoreCalculator calc = new RAS2012ScoreCalculator();
         calc.resetWorkingSolution(solution);
         return calc.calculateScore();
     }
 }
