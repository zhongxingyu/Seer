 import java.util.ArrayList;
 
 /**
  * Abstract class to handle a page.
  * 
  * Options:
  * s: to select
  * p: previous page
  * n: next page
  * q: to exit
  * 
  * @author George Coomber and Michael Feist
  */
 
 abstract public class PageView implements Callback
 {
 	ArrayList<DatabaseRow> rows;
 	
 	abstract public void view();
 	abstract public void getRowInfo(String id);
 	
 	public void pageView(int pageSize)
 	{
 		boolean run = true;
 		int pageNumber = 1;
 		int i = 0;
 		while (run)
 		{
 			while (i < pageSize*pageNumber && i < rows.size())
 			{
 				System.out.println((i + 1) + ": " + rows.get(i).toString());
 				i++;
 			}
 			
 			System.out.println("\nSearch Result Options");
 			System.out.println("s: to select");
 			System.out.println("p: previous page");
 			System.out.println("n: next page");
 			System.out.println("q: to exit");
 			
 			String command = Menu.getKeyBoard();
 			
 			switch (command.toCharArray()[0])
 			{
 				case 's':
 					System.out.println("Enter the row number:");
 					String input = Menu.getKeyBoard();
 					
					Integer adNumber = Integer.valueOf(input);
 					
					if (adNumber == null)
 					{
 						System.out.println("Error: Not an Integer.");
						return;
 					}
 					
 					int row = adNumber.intValue();
 					
 					if (row >= 1 && row <= rows.size())
 					{
 						getRowInfo(rows.get(row - 1).getID());
 					}
 					
 					i = pageSize*(pageNumber - 1);
 					break;
 					
 				case 'p':
 					if (pageNumber > 1)
 					{
 						pageNumber--;
 						i = pageSize*(pageNumber - 1);
 					} else
 					{
 						i = pageSize*(pageNumber - 1);
 					}
 					break;
 					
 				case 'n':
 					if (i < rows.size())
 					{
 						i = pageSize*pageNumber;
 						pageNumber++;
 					} else
 					{
 						i = pageSize*(pageNumber - 1);
 					}
 					break;
 					
 				case 'q':
 					run = false;
 					break;
 					
 				default:
 					i = pageSize*(pageNumber - 1);
 					System.out.println("Error: Unknown command.");
 					break;
 			}
 		}
 	}
 	
 	@Override
 	public int callback() {
 		view();
 		return Callback.OK;
 	}
 }
