 package gutenberg.workers;
 
 import gutenberg.blocs.AssignQuiz;
 import gutenberg.blocs.AssignmentType;
 import gutenberg.blocs.BuildQuiz;
 import gutenberg.blocs.BuildQuizResponse;
 import gutenberg.blocs.DocumentMasterSkeleton;
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.PageType;
 import gutenberg.blocs.QuizType;
 import gutenberg.blocs.StudentType;
 
 public class TestDriver {
 
 	/**
 	 * @param args
 	 */
 	@SuppressWarnings("unused")
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		BuildQuiz request = null;
 		BuildQuizResponse response = null;
 		DocumentMasterSkeleton service = null;
 		try {
 			request = new BuildQuiz();
 			AssignmentType assignment = new AssignmentType();			
 			QuizType quiz = new QuizType();
 			EntryType quizid = new EntryType();
			quizid.setId("quiz60");
 			quiz.setQuiz(quizid);
 			EntryType teacher = new EntryType();
 			teacher.setId("7675");
 			teacher.setName("Yo Teach");
 			EntryType school = new EntryType();
 			school.setId("123");
 			school.setName("Don Bosco School");
 			quiz.setTeacher(teacher);
 			quiz.setSchool(school);
 			PageType[] pages = new PageType[1];
 			pages[0] = new PageType();
 			pages[0].setNumber(1);
 			EntryType[] questions = new EntryType[1];
 			questions[0] = new EntryType();
 			questions[0].setId("1");
 			pages[0].setQuestion(questions);
 			quiz.setPage(pages);
 			EntryType[] students = new EntryType[2];
 			students[0] = new EntryType();
 			students[0].setId("1A");
 			students[0].setName("Student Aman");
 			students[1] = new EntryType();
 			students[1].setId("1B");
 			students[1].setName("Etudiant Azan");
 			EntryType instance = new EntryType();
 			instance.setId("1");
 			assignment.setQuiz(quizid);
 			assignment.setInstance(instance);
 			assignment.setStudents(students);
 			request.setBuildQuiz(quiz);
 			service = new DocumentMasterSkeleton();
 			
 			AssignQuiz assignQuizReq = new AssignQuiz();
 			assignQuizReq.setAssignQuiz(assignment);
 			//service.assignQuiz(assignQuizReq);
 			
 			service.buildQuiz(request);			
 		} catch (Exception e) {
 			
 		}
 
 	}
 
 }
