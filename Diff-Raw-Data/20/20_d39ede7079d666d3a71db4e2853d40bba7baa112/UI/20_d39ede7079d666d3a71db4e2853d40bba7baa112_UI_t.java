 import java.util.LinkedList;
 import java.util.Scanner;
 
 public class UI
 {
 	private GradeSystems aGradeSystem;
 	private Grades aGrads;
 
 	public UI() throws Exception
 	/*
 	 * try 
 	 * 1.call GradeSystems() toغc aGradeSystem
 	 * 
 	 * 2.loop1 until Q (Quit)
 	 * 1.	promptID() to get user ID  JID Q (ϥ)H 
 	 * 2.	checkID() to see if the ID is in aGradeSystem
 	 * 3.	showWelcomeMsg()      ex. Welcome§
 	 * 4.	loop2 until E (Exit)
 	 * promptCommand() to prompt for input command 
 	 *    end loop2
 	 * end loop1
 	 * 
 	 * 3.showFinishMsg()           F
 	 * end try
 	 * finally { } // do nothing
 	 * 
 	 */
 	{
 		try
 		{
 			aGradeSystem = new GradeSystems();
 
 			String inputLoop1 = null;
 			do
 			{
 				inputLoop1 = promptID();
 
 				if (inputLoop1.equals("Q"))
 				{
 					break;
 				}
 				checkID(inputLoop1);
 
 				showWelcomeMsg();
 
 				promptCommand();
 
 			} while (true);
 
 			showFinishMsg();
 		}
 
 		finally
 		{
 		} // do nothing
 	}
 
 	private void showWelcomeMsg()
	/*show the welcome message*/
 	{
 		System.out.printf("Welcome %s\n", aGrads.getName());
 	}
 
 	public void promptCommand() throws NoSuchCommandExceptions
 	/*
 	 * try
 	 * 1. prompt user for inputCommand
 	 * 2. if inputCommand is not G (Grade),R (Rank), W (Weights), or E (Exit),
 	 *  throws NoSuchCommandException
 	 * 3. if inputCommand is E (Exit) then break
 	 *  else: G aGradeSystem.showGrade(), R showRank(), W updateWeights() end if
 	 * end of try
 	 * finally {} // do nothing
 	 */
 	{
 		try
 		{
 			do
 			{
 				System.out.printf("JO\t1) G ܦZ (Grade)\n");
 				System.out.printf("\t2) R ܱƦW (Rank)\n");
 				System.out.printf("\t3) W st (Weight)\n");
 				System.out.printf("\t4) E } (Exit) \n");
 
 				Scanner input = new Scanner(System.in);
 				String inputCommand = input.nextLine();
 
 				if (!inputCommand.equals("G") && !inputCommand.equals("R") && !inputCommand.equals("W") && !inputCommand.equals("E"))
 				{
 					throw new NoSuchCommandExceptions(inputCommand);
 				}
 
 				else if (inputCommand.equals("E"))
 				{
 					break;
 				}
 
 				else if (inputCommand.equals("G"))
 				{
 					aGradeSystem.showGrade(aGrads.getId());
 				}
 
 				else if (inputCommand.equals("R"))
 				{
 					aGradeSystem.showRank(aGrads.getId());
 				}
 
 				else if (inputCommand.equals("W"))
 				{
 					aGradeSystem.updateWeights();
 				}
 			} while (true);
 		}
 		finally
 		{
 		} // do nothing
 
 	}
 
 	public String promptID()
 	/*
 	 * 1.print JID Q (ϥ)H
 	 * 2.^ǨϥΪ̿JString
 	 */
 	{
 		System.out.println("JID Q (ϥ)H");
 		Scanner input = new Scanner(System.in);
 		String input_string = input.nextLine();
 		return input_string;
 	}
 
 	public Boolean checkID(String ID) throws NoSuchIDExceptions
 	/*
 	checkID (ID) throws NoSuchIDExceptions return Boolean
 	parameter: ID   a user ID  ex: 123456789
 	time:     O(n)  n is  aGradeSystem ZH
 	
 	try 
 		1.check if the ID is in aGradeSystem
 		2.if not, throw an object of NoSuchIDExceptions
 		3.return true
 	end of try
 	finally{} // do nothing
 	 */
 	{
 		Boolean check = false;
 
 		try
 		{
 			LinkedList<Grades> aList = aGradeSystem.getaList();
 			for (int i = 0; i < aList.size(); i++)
 			{
 				if (aList.get(i).getId().equals(ID))
 				{
 					aGrads = aList.get(i);
 					check = true;
 				}
 			}
 			if (check == false)
 			{
 				throw new NoSuchIDExceptions(ID);
 			}
 			return check;
 		}
 		finally
 		{
 		} // do nothing
 	}
 
 	public void showFinishMsg()
 	/*
 	 * 1.print F
 	 */
 	{
 		System.out.println("F");
 	}
 
 }
