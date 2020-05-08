 package edu.harvard.cs262.grading;
 
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 
 public class MainSimpleStudentGrade {
 
 	public static void main(String[] args) throws Exception {
 		if (args.length != 5) {
 			System.err.println("usage: graderID submitterID assignmentID string score");
 			System.exit(-1);
 		}
 		
 		// get an instance of a remote server
 		Registry registry = LocateRegistry.getRegistry();
 		GradeCompilerService s = (GradeCompilerService)
 			registry.lookup("GradeCompilerService");
 		
 		// prepare data
 		Student grader = new StudentImpl(Integer.parseInt(args[0]));
 		Student submitter = new StudentImpl(Integer.parseInt(args[1]));
 		Assignment assignment = new AssignmentImpl(Integer.parseInt(args[2]));
 		byte contents[] = args[3].getBytes();
 		Submission submission = new SubmissionImpl(submitter, assignment, contents);
 		Score score = new ScoreImpl(Integer.parseInt(args[4]),100);
 		
		// remote calls
		Grade grade = s.storeGrade(grader, submission, score);		
 		System.out.println(grade.getGrader());
 		System.out.println(grade.getScore());
 		System.out.println(grade.getTimeStamp());
 		
 		// get all grades
 		Map<Submission, List<Grade>> grades = s.getCompiledGrades(assignment);
 		System.out.println(grades);
		
		// get all graders
		Set<Student> graders = s.getGraders(submission);
		System.out.println(graders);
 	}
 
 }
