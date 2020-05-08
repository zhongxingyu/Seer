 package beans;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Random;
 
 public class Question
 	{
 		private static final int INITIAL_CAPACITY = 1;
 		int number, flag;
 		boolean prev_ques;
 		private String question, o1, o2, o3, o4, answer, user_ans;
 		private int ques_level;
 		private HashMap<Integer, Integer> previouslyGeneratedQuestionNumbers;
 		private int min = 1;
 		private int totlaLimit;
 
 		private Test test;
 
 		public void setTest(String testName) throws ClassNotFoundException, SQLException
 			{
 				String listOfAvailableTables[];
 				DatabaseConnection databaseConnection = new DatabaseConnection("stud");
 				ResultSet resultSet;
 				resultSet=databaseConnection.executeQuery("select count(*)"
 						+ " from Test_Tables where TestName='" + testName + "'");
 				resultSet.next();
 				listOfAvailableTables = new String[Integer
 						.parseInt(resultSet.getString(1))];
 				resultSet=databaseConnection.executeQuery("select * from Test_Tables "
 						+ "where TestName='" + testName + "'");
 				int counter = 0;
 				while (resultSet.next())
 					{
 						listOfAvailableTables[counter++] = resultSet
 								.getString("TableName");
 					}
 				databaseConnection.disconnect();
 				test = new Test(listOfAvailableTables);
 			}
 		public Question(String testName) throws ClassNotFoundException, SQLException
 			{
 				ques_level = min;
 				prev_ques = false;
 				previouslyGeneratedQuestionNumbers = new HashMap<Integer, Integer>(
 						INITIAL_CAPACITY);
 				setTest(testName);
 				totlaLimit = test.getTotalLimit();
 			}
 
 		public String getUser_ans()
 			{
 				return user_ans;
 			}
 
 		public boolean generate_ques(boolean correct)
 			{
 				ques_level = 1;
 				Random random = new Random();
 				// Generate Question Number within limits
 				for (number = random.nextInt(totlaLimit); !insert(number); number = random
 						.nextInt(totlaLimit))
 					;
 				try
 					{
 						test.getRowFromTable(number, this);
 					}
 				catch (Exception exception)
 					{
 						exception.printStackTrace();
 						return false;
 					}
 				return true;
 			}
 
 		public void setUser_ans(String user_ans)
 			{
 				this.user_ans = user_ans;
 			}
 
 		public String getTable()
 			{
 				return test.getQuestionDetails();
 			}
 
 		public void setTable(String table)
 			{
 			}
 
 		public boolean insert(int key)
 			{
                if(key ==0)
                    return false;
 				try
 					{
 						if (!previouslyGeneratedQuestionNumbers.containsKey(new Integer(
 								key)))
 							{
 								previouslyGeneratedQuestionNumbers.put(new Integer(key),
 										new Integer(key));
 								return true;
 							}
 					}
 				catch (NullPointerException e)
 					{
 						e.printStackTrace();
 					}
 				return false;
 			}
 
 		public void setNumber(int number)
 			{
 				this.number = number;
 			}
 
 		public int getFlag()
 			{
 				return flag;
 			}
 
 		public void setFlag(int flag)
 			{
 				this.flag = flag;
 			}
 
 		public String getQuestion()
 			{
 				return question;
 			}
 
 		public void setQuestion(String question)
 			{
 				this.question = question;
 			}
 
 		public String getO1()
 			{
 				return o1;
 			}
 
 		public void setO1(String o1)
 			{
 				this.o1 = o1;
 			}
 
 		public String getO2()
 			{
 				return o2;
 			}
 
 		public void setO2(String o2)
 			{
 				this.o2 = o2;
 			}
 
 		public String getO3()
 			{
 				return o3;
 			}
 
 		public void setO3(String o3)
 			{
 				this.o3 = o3;
 			}
 
 		public String getO4()
 			{
 				return o4;
 			}
 
 		public void setO4(String o4)
 			{
 				this.o4 = o4;
 			}
 
 		public String getAnswer()
 			{
 				return answer;
 			}
 
 		public void setAnswer(String answer)
 			{
 				this.answer = answer;
 			}
 
 		public int getQues_level()
 			{
 				return ques_level;
 			}
 
 	}
