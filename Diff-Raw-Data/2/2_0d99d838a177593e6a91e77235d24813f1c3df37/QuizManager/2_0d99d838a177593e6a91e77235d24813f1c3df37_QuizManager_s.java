 package com.mick88.superbrain.quizzes;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.michaldabski.msqlite.models.Table;
 import com.mick88.superbrain.DatabaseHelper;
 
 public class QuizManager extends DatabaseHelper
 {
 	public QuizManager(Context context)
 	{
 		super(context);
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db)
 	{
 		super.onCreate(db);
 		createQuizzes(db);
 	}
 	
 	/**
 	 * Generate quizzes and insert them into the database
 	 * @param database
 	 */
 	private void createQuizzes(SQLiteDatabase database)
 	{	
 		List<Quiz> newQuizzes = new ArrayList<Quiz>();
 		newQuizzes.add(new Quiz("Programming", "General")
 				.addQuestion(new Question("How do you break out of a loop?", "break;").setFakeAnswers(new String[] {"continue;", "return;"}))
 				.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 				.addQuestion(new Question("Which function is called first?", "main()").setFakeAnswers(new String[] {"onCreate()", "start()"}))
 				);
 		
 		newQuizzes.add(new Quiz("Programming", "C#")
 		.addQuestion(new Question("Object template is a ", "Class").setFakeAnswers(new String[] {"Method", "Pointer"}))
 		.addQuestion(new Question("Increasing by 1 is", "increment").setFakeAnswers(new String[] {"decrement", "return"}))
 		.addQuestion(new Question("Is called when object is instantiated", "Constructor").setFakeAnswers(new String[] {"main()", "destructor"}))
 		);
 		
 		newQuizzes.add(new Quiz("Mathematics", "Basic operations")
 		.addQuestion(new Question("1+1=", "2").setFakeAnswers(new String[] {"1", "3"}))
 		.addQuestion(new Question("5 x 5", "25").setFakeAnswers(new String[] {"30", "15"}))
 		);
 		
 		newQuizzes.add(new Quiz("Mathematics", "Number Theory")
 		.addQuestion(new Question("1+1=", "2").setFakeAnswers(new String[] {"1", "3"}))
 		.addQuestion(new Question("5 x 5", "25").setFakeAnswers(new String[] {"30", "15"}))
 		);
 		
 		newQuizzes.add(new Quiz("Mathematics", "Statistics")
 		.addQuestion(new Question("1+1=", "2").setFakeAnswers(new String[] {"1", "3"}))
 		.addQuestion(new Question("5 x 5", "25").setFakeAnswers(new String[] {"30", "15"}))
 		);
 		
 		newQuizzes.add(new Quiz("Mathematics", "Algebra")
 		.addQuestion(new Question("1+1=", "2").setFakeAnswers(new String[] {"1", "3"}))
 		.addQuestion(new Question("5 x 5", "25").setFakeAnswers(new String[] {"30", "15"}))
 		);
 		
 		newQuizzes.add(new Quiz("Mathematics", "Sets")
 		.addQuestion(new Question("1+1=", "2").setFakeAnswers(new String[] {"1", "3"}))
 		.addQuestion(new Question("5 x 5", "25").setFakeAnswers(new String[] {"30", "15"}))
 		);
 		
 		newQuizzes.add(new Quiz("Programming", "Android")
 		.addQuestion(new Question("How do you break out of a loop?", "break;").setFakeAnswers(new String[] {"continue;", "return;"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which function is called first?", "main()").setFakeAnswers(new String[] {"onCreate()", "start()"}))
 		);
 		
 		newQuizzes.add(new Quiz("Programming", "Linux Shell")
 		.addQuestion(new Question("How do you break out of a loop?", "break;").setFakeAnswers(new String[] {"continue;", "return;"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which function is called first?", "main()").setFakeAnswers(new String[] {"onCreate()", "start()"}))
 		);
 		
 		newQuizzes.add(new Quiz("Programming", "Basic")
 		.addQuestion(new Question("How do you break out of a loop?", "break;").setFakeAnswers(new String[] {"continue;", "return;"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which function is called first?", "main()").setFakeAnswers(new String[] {"onCreate()", "start()"}))
 		);
 		
 		newQuizzes.add(new Quiz("Programming", "Visual Studio")
 		.addQuestion(new Question("How do you break out of a loop?", "break;").setFakeAnswers(new String[] {"continue;", "return;"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which function is called first?", "main()").setFakeAnswers(new String[] {"onCreate()", "start()"}))
 		);
 		
 		newQuizzes.add(new Quiz("Programming", "Delphi")
 		.addQuestion(new Question("How do you break out of a loop?", "break;").setFakeAnswers(new String[] {"continue;", "return;"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which function is called first?", "main()").setFakeAnswers(new String[] {"onCreate()", "start()"}))
 		);
 		
 		newQuizzes.add(new Quiz("Programming", "Haskell")
 		.addQuestion(new Question("How do you break out of a loop?", "break;").setFakeAnswers(new String[] {"continue;", "return;"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which function is called first?", "main()").setFakeAnswers(new String[] {"onCreate()", "start()"}))
 		);
 		
 		newQuizzes.add(new Quiz("Programming", "Assembly")
 		.addQuestion(new Question("How do you break out of a loop?", "break;").setFakeAnswers(new String[] {"continue;", "return;"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which function is called first?", "main()").setFakeAnswers(new String[] {"onCreate()", "start()"}))
 		);
 		
 		newQuizzes.add(new Quiz("Programming", "C")
 		.addQuestion(new Question("How do you break out of a loop?", "break;").setFakeAnswers(new String[] {"continue;", "return;"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which function is called first?", "main()").setFakeAnswers(new String[] {"onCreate()", "start()"}))
 		);
 		
 		newQuizzes.add(new Quiz("Programming", "Java")
 		.addQuestion(new Question("How do you break out of a loop?", "break;").setFakeAnswers(new String[] {"continue;", "return;"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which function is called first?", "main()").setFakeAnswers(new String[] {"onCreate()", "start()"}))
 		);
 		
 		newQuizzes.add(new Quiz("Programming", "Pascal")
 		.addQuestion(new Question("How do you break out of a loop?", "break;").setFakeAnswers(new String[] {"continue;", "return;"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which of these is not OOP?", "C").setFakeAnswers(new String[] {"C#", "Java"}))
 		.addQuestion(new Question("Which function is called first?", "main()").setFakeAnswers(new String[] {"onCreate()", "start()"}))
 		);
 		
 		newQuizzes.add(new Quiz("HCI", "General")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		newQuizzes.add(new Quiz("HCI", "Week 1")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		newQuizzes.add(new Quiz("HCI", "Week 2")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		newQuizzes.add(new Quiz("HCI", "Week 3")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		newQuizzes.add(new Quiz("HCI", "Week 4")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		newQuizzes.add(new Quiz("HCI", "Week 5")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		newQuizzes.add(new Quiz("General CS", "Linux")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		newQuizzes.add(new Quiz("General CS", "Mac")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		newQuizzes.add(new Quiz("Databases", "General")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		newQuizzes.add(new Quiz("PPD", "File Operations")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		newQuizzes.add(new Quiz("PPD", "Database Operations")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		newQuizzes.add(new Quiz("PPD", "Bitmap encoding")
 		.addQuestion(new Question("Mismatch between a user's goal for action and the means to execute that goal is called", "gulf of execution").setFakeAnswers(new String[] {"gulf of evaluation", "action"}))
 		.addQuestion(new Question("What does HCI stand for?", "Human Computer Interaction").setFakeAnswers(new String[] {"Human Computer Interface", "Human Computer Industry"}))
 		.addQuestion(new Question("Which one of these would NOT be found in a good HCI? ", "A long command line to achieve a function ").setFakeAnswers(new String[] {"Icons that can have specific meanings.", "Common short cuts, like CTRL+Z for undo."}))
 		.addQuestion(new Question("Which of these films uses futuristic HCI?", "Minority Report").setFakeAnswers(new String[] {"Bambi", "Speed"}))
 		);
 		
 		insert(database, Quiz.class, newQuizzes);
 	}
 	
 	public List<Quiz> getQuizzes(String category)
 	{
		return select(Quiz.class, "category='?'", new String[]{category}, null, null);
 	}
 	
 	public String [] getCategories()
 	{
 		final String COL_CATEGORY = "category";
 		SQLiteDatabase database = getReadableDatabase();
 		Cursor cursor = database.query(true, new Table(Quiz.class).getName(), new String[]{COL_CATEGORY}, null, null, COL_CATEGORY, null, COL_CATEGORY, null);
 		String [] categories = new String[cursor.getCount()];
 		int i=0;
 		while (cursor.moveToNext())
 			categories[i++] = cursor.getString(0);
 		database.close();
 		
 		return categories;
 	}
 }
