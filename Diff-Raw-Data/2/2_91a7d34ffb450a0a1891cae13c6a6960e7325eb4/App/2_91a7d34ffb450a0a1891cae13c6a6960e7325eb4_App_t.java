 package org.drools.planner.examples.ras2012;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.drools.planner.config.XmlSolverFactory;
 import org.drools.planner.core.Solver;
 import org.drools.planner.examples.ras2012.model.Network;
 import org.drools.planner.examples.ras2012.model.Route;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Hello world!
  * 
  */
 public class App {
 
     private static class RouteVisualizer implements Callable<Boolean> {
 
         private final Route route;
         private final File  file;
 
         public RouteVisualizer(final Route r, final File parentFolder) {
             this.route = r;
             this.file = new File(parentFolder, "route" + r.getId() + ".png");
         }
 
         @Override
         public Boolean call() {
             return this.route.visualize(this.file);
         }
 
     }
 
     private static class VisualizationController implements Callable<Boolean> {
 
         private static final Logger        logger  = LoggerFactory
                                                            .getLogger(VisualizationController.class);
 
         private final Network              network;
         private final File                 file;
         private final Set<Future<Boolean>> futures = new HashSet<Future<Boolean>>();
 
         public VisualizationController(final File dataset, final Network n) {
             this.network = n;
             this.file = dataset;
         }
 
         @Override
         public Boolean call() {
             VisualizationController.logger.info("Started visualization work.");
             // prepare folder structure
             final File parentFolder = new File("data", this.file.getName());
             if (!parentFolder.exists()) {
                 parentFolder.mkdirs();
             }
             // visualize the routes
             final Collection<Route> routes = new LinkedList<Route>();
             routes.addAll(this.network.getAllEastboundRoutes());
             routes.addAll(this.network.getAllWestboundRoutes());
             for (final Route r : routes) {
                 this.futures.add(App.visualizerExecutor
                         .submit(new RouteVisualizer(r, parentFolder)));
             }
             boolean success = true;
             for (final Future<Boolean> future : this.futures) {
                 try {
                     success = future.get() & success;
                 } catch (final Exception e) {
                     VisualizationController.logger.warn("Execution of route visualization failed.",
                             e);
                     success = false;
                 }
             }
             // visualize the network
             success = this.network.visualize(new File(parentFolder, "network.png"));
             VisualizationController.logger.info("Finished visualization work.");
             App.visualizerExecutor.shutdownNow();
             return success;
         }
     }
 
     private static final ExecutorService visualizerExecutor = Executors.newFixedThreadPool(Runtime
                                                                     .getRuntime()
                                                                     .availableProcessors() - 1);
 
     @SuppressWarnings("unused")
     public static void main(final String[] args) throws FileNotFoundException, IOException {
         // read solution
        final File f = new File("src/main/resources/org/drools/planner/examples/ras2012/RDS3.txt");
         final RAS2012Solution sol = new RAS2012ProblemIO().read(f);
         if (false) {
             final Future<Boolean> visualizationSuccess = App.visualizerExecutor
                     .submit(new VisualizationController(f, sol.getNetwork()));
         }
         // and now start solving
         final XmlSolverFactory configurer = new XmlSolverFactory();
         configurer.configure(App.class.getResourceAsStream("/solverConfig.xml"));
         final Solver solver = configurer.buildSolver();
         solver.setPlanningProblem(sol);
         solver.solve();
         System.out.println(solver.getBestSolution());
     }
 }
