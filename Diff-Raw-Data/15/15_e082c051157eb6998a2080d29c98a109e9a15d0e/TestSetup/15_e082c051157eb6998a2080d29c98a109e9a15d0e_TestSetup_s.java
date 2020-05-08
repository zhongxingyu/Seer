 package edu.msoe.se2800.h4;
 
 import dagger.ObjectGraph;
 import edu.msoe.se2800.h4.jplot.JPlotController;
 
 import javax.inject.Inject;
 
 public class TestSetup {
     
     @Inject
    private JPlotController mJPlotController;
 
     @Inject
    private IRobotController mRobotController;
     
     public static void run() {
         
      // Setup dependency injection
         ObjectGraph objectGraph = ObjectGraph.create(new LejosModule());
         objectGraph.injectStatics();
         
     }
 
 }
