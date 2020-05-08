 package main.app;
 
 import org.drools.planner.config.XmlSolverFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.*;
 import org.drools.planner.core.Solver;
 import org.drools.planner.core.score.director.ScoreDirector;
 
 
 import main.domain.*;
 import main.data.*;
 
 public class FLMPlannerHelloWorld {
 
 	public static final String SOLVER_CONFIG = "/FLMPlannerSolverConfig.xml";
 	
 	public static void runData(String inFile, String outFile) {
 		ImportData importer = new ImportData();
 		//importer.initialtest();
 		importer.importFromXLS(inFile);
 		DataStorage storage = importer.getStorage();
 		
 		long startTimeCounter = System.currentTimeMillis();
 		
 		XmlSolverFactory solverFactory = new XmlSolverFactory();
         solverFactory.configure(SOLVER_CONFIG);
         Solver solver = solverFactory.buildSolver();
         
         PlannerSolution initialSolution = new PlannerSolution(storage.scheduleList,storage.classroomList,storage.dayList);
         solver.setPlanningProblem(initialSolution);
         solver.solve();
         
         ScoreDirector scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
 
         long elapsedTimeMillis = System.currentTimeMillis()-startTimeCounter;
         
         PlannerSolution solvedSolution = (PlannerSolution) solver.getBestSolution();
         
         System.out.println(solvedSolution.getScore());
        System.out.println("Elapsed time: " + elapsedTimeMillis/(60*1000F) + "min "+ elapsedTimeMillis/1000F + "sec");
         
         scoreDirector.setWorkingSolution(solvedSolution);
         scoreDirector.calculateScore();
         
         /*List<Schedule> listSch = solvedSolution.getScheduleList();
         listSch.get(6).setDay(storage.dayList.get(3));
         listSch.get(6).setClassroom(storage.classroomList.get(0));
         solvedSolution.setScheduleList(listSch);
         System.out.println(solvedSolution.getScore());
         
         scoreDirector.setWorkingSolution(solvedSolution);
         scoreDirector.calculateScore();*/
         //scoreDirector.
         
         ExportData exporter = new ExportData(solvedSolution.getScheduleList());
         
         //exporter.showInitialTestResult();
         
         //System.out.println("Export to XLS: " + exporter.exportToXLS(outFile));
         System.out.println("Export to XLS: " + exporter.exportToXLS_debug(outFile));
         
         //System.out.println(solvedSolution.getScheduleList().get(6).conflictDayCheck(solvedSolution.getScheduleList().get(7)));
 	}
 
 	
 	
 	public static void main(String[] args)
 	{
 		runData(args[0],args[1]);
 	}
 
 }
