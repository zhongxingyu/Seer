 package runs;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import constraint.MustHaveCourse;
 import constraint.NoConflicts;
 import constraint.NoCourseAtThisTime;
 import constraint.PreferedGeneralCourseTime;
 
 import schedule.CourseBlock;
 import schedule.CourseTime;
 import schedule.DayOfWeek;
 import student.Student;
 
 public class Run07Generator {
 
 	public static ArrayList<Student> generateStudents() {
 		ArrayList<Student> students = new ArrayList<>();
 		
		for (int i=0; i<100; i++){
 		Student student1 = new Student();
 		student1.addConstraint(new MustHaveCourse("CSI2520"));
 		student1.addConstraint(new MustHaveCourse("SEG2506"));
 		student1.addConstraint(new MustHaveCourse("MAT2722"));
 		student1.addConstraint(new MustHaveCourse("MAT1724"));
 		student1.addConstraint(new MustHaveCourse("ELG1010"));
 		student1.addConstraint(new PreferedGeneralCourseTime(8, 30, 17, 30));
 		student1.addConstraint(new NoConflicts());
 		students.add(student1);
 		
 		Student student2 = new Student();
 		student2.addConstraint(new MustHaveCourse("CSI2520"));
 		student2.addConstraint(new MustHaveCourse("SEG2506"));
 		student2.addConstraint(new MustHaveCourse("FRA1529"));
 		student2.addConstraint(new MustHaveCourse("SEG2911"));
 		student2.addConstraint(new PreferedGeneralCourseTime(11, 30, 22, 00));
 		student2.addConstraint(new NoConflicts());
 		students.add(student2);
 	
 		Student student3 = new Student();
 		student3.addConstraint(new MustHaveCourse("CSI2520"));
 		student3.addConstraint(new MustHaveCourse("MAT2722"));
 		student3.addConstraint(new MustHaveCourse("SEG2911"));
 		student3.addConstraint(new MustHaveCourse("MAT1724"));
 		student3.addConstraint(new PreferedGeneralCourseTime(11, 30, 22, 00));
 		student3.addConstraint(new NoConflicts());
 		students.add(student3);
 		
 		Student student4 = new Student();
 		student4.addConstraint(new MustHaveCourse("CSI2520"));
 		student4.addConstraint(new MustHaveCourse("SEG2506"));
 		student4.addConstraint(new MustHaveCourse("MAT1724"));
 		student4.addConstraint(new MustHaveCourse("SEG2591"));
 		student4.addConstraint(new MustHaveCourse("MAT2722"));
 		student4.addConstraint(new PreferedGeneralCourseTime(8, 30, 22, 30));
 		student4.addConstraint(new NoCourseAtThisTime(11, 00, 13, 00, DayOfWeek.FRIDAY));
 		student4.addConstraint(new NoConflicts());
 		students.add(student4);
 
 		Student student5 = new Student();
 		student5.addConstraint(new MustHaveCourse("MAT1724"));
 		student5.addConstraint(new MustHaveCourse("SEG2506"));
 		student5.addConstraint(new MustHaveCourse("CEG1500"));
 		student5.addConstraint(new MustHaveCourse("FRA1529"));
 		student5.addConstraint(new MustHaveCourse("MAT2722"));
 		student5.addConstraint(new MustHaveCourse("ELG1010"));
 		student5.addConstraint(new PreferedGeneralCourseTime(10, 30, 22, 30));
 		student5.addConstraint(new NoCourseAtThisTime(19, 00, 22, 00, DayOfWeek.MONDAY));
 		student5.addConstraint(new NoConflicts());
 		students.add(student5);
 		
 		Student student6 = new Student();
 		student6.addConstraint(new MustHaveCourse("MAT1724"));
 		student6.addConstraint(new MustHaveCourse("ELG1010"));
 		student6.addConstraint(new PreferedGeneralCourseTime(10, 30, 22, 30));
 		student6.addConstraint(new NoCourseAtThisTime(19, 00, 22, 00, DayOfWeek.WEDNESDAY));
 		student6.addConstraint(new NoConflicts());
 		students.add(student6);
		}
 		
 		return students;		
 	}
 
 	public static ArrayList<CourseBlock> generateBlocksBasedOnStudents(ArrayList<Student> students){
 		HashMap<String, Integer> mustHaveCourses = new HashMap<>();
 		
 		for (Student student: students){
 			for (String course: student.getWantedCourses()){
 				mustHaveCourses.put(course, null);
 			}
 		}
 		
 		ArrayList<CourseBlock> blocks = new ArrayList<>();
 		
 		for (String course: mustHaveCourses.keySet()){
 			blocks.add(new CourseBlock(course, "LEC1", "A", null));
 			blocks.add(new CourseBlock(course, "LEC2", "A", null));
 			blocks.add(new CourseBlock(course, "TUT", "A", null));
 			blocks.add(new CourseBlock(course, "LAB", "A", null));
 		}
 		
 		return blocks;
 	}
 
 	public static CourseTime generateRandomTime(Random rng) {
 		return new CourseTime(
 				DayOfWeek.getDayOnNumber(rng.nextInt(5)), 
 				rng.nextInt(24), 
 				rng.nextInt(2)*30, 
 				90);
 	}
 }
