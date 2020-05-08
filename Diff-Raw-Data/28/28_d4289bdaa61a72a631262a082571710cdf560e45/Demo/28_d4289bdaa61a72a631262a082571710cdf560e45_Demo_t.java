 
 import java.util.Scanner;
 
 public class Demo {
	
	public static base_file current;
	public static final POODirectory root = new POODirectory("root");
	
 	public static void main(String argv[])
 	{
 		Scanner input = new Scanner(System.in);
 		String command;
		current = root;
 		boolean leave;
 		
 		System.out.println("you are in root directory");
 		System.out.println("type some command or use help to get more information");
 		
 		while(true)
 		{
 			command = input.nextLine();
			String[] split_command = command.split("[ ]+");
			leave = execute(split_command);
 			if(leave == false)
 				break;
 		}
 		input.close();
 	}
	public static boolean execute(String[] split_command)
 	{
 		base_file next;
 		int id;
 		switch(split_command[0])
 		{
 			case "where":	// where : show the current location
 				current.show_name();
 				break;
 			case "list":	// list : show the items in the current directory/board
 				if(current instanceof POODirectory)
 					((POODirectory) current).show();
 				else if(current instanceof POOBoard)
 					((POOBoard) current).show();
 				else if(current instanceof POOArticle)
 					((POOArticle) current).show();
 				break;
 			case "goto":	// goto directory_name/board_name/article_id : go to directory/board/article
 				if(current instanceof POODirectory)
 				{
 					next = ((POODirectory) current).goto_next_level(split_command[1]);
 					if(next != null)
 						current = next;
					current.show_name();
 				}
 				else if(current instanceof POOBoard)
 				{
 					try{
 						id = Integer.valueOf(split_command[1]);
 					}catch(NumberFormatException err)
 					{
 						break;
 					}
 					next = ((POOBoard) current).goto_next_level(id);
 					if(next != null)
 						current = next;
 				}
 				break;
 			case "up":		// up : go to the upper directory
 				if(current instanceof POODirectory)
 				{
 					next = ((POODirectory) current).go_up();
 					if(next != null)
 						current = next;
 				}
 				else if(current instanceof POOBoard)
 				{
 					next = ((POOBoard) current).go_up();
 					if(next != null)
 						current = next;
 				}
				else if(current instanceof POOArticle)
				{
					next = ((POOArticle) current).go_up();
					if(next != null)
						current = next;
				}
 				break;
 			case "check":	// check article_name : list the articles in the board with the same name
 				if(current instanceof POOBoard)
 					((POOBoard) current).check(split_command[1]);
 				break;
 			case "detail":	// detail id_number : show the detail information of the article
 				try{
 					id = Integer.valueOf(split_command[1]);
 				}catch(NumberFormatException err)
 				{
 					break;
 				}
 				if(current instanceof POOBoard)
 					((POOBoard) current).detail(id);
 				break;
 			case "addboard":	// addboard board_name: add a new board to current directory
 				POOBoard new_board;
 				if(current instanceof POODirectory)
 				{
 					 new_board = new POOBoard(split_command[1]);
 					 new_board.set_upper(((POODirectory) current));
 					 ((POODirectory) current).add(new_board);
 				}
 				break;
 			case "adddir":		// adddir directory_name : add a new directory to current directory
 				POODirectory new_dir;
 				if(current instanceof POODirectory)
 				{
 					 new_dir = new POODirectory(split_command[1]);
 					 new_dir.set_upper(((POODirectory) current));
 					 ((POODirectory) current).add(new_dir);
 				}
 				break;
 			case "addarticle":	// addarticle title author content: add a new article to current board
 				String title = split_command[1];
 				String author = split_command[2];
 				String content = split_command[3];
 				POOArticle new_article;
 				if(current instanceof POOBoard)
 				{
 					new_article = new POOArticle(title, author, content); 
 					((POOBoard) current).add(new_article);
 				}
 				break;
 			case "push":	// push message : push the article
 				if(current instanceof POOArticle)
 					 ((POOArticle) current).push(split_command[1]);
 				break;
 			case "boo":
 				if(current instanceof POOArticle)
 					 ((POOArticle) current).boo(split_command[1]);
 				break;
 			case "arraw":
 				if(current instanceof POOArticle)
 					 ((POOArticle) current).arraw(split_command[1]);
 				break;
 			case "exit":
 				return false;
 			default:
 				;
 		}
 		return true;
 	}
 	
 }
