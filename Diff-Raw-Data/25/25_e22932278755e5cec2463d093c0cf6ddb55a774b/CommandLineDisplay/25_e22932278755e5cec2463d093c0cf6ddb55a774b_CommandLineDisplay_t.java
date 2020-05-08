 package display;
 
 import game.Board;
 import game.Colour;
 import game.Piece;
 import game.ReversiPiece;
 
 import java.util.Scanner;
 
 import player.Player;
 
 public class CommandLineDisplay implements Display
 {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see display.Display#printCurrentPlayerTurn(player.Player)
 	 */
 	@Override
 	public void printCurrentPlayerTurn(Player player)
 	{
 		System.out.println(player.getName() + ": turn now.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see display.Display#getPlayerName()
 	 */
 	@SuppressWarnings("resource")
 	@Override
 	public String getPlayerName()
 	{
 		System.out.println("Please enter your name.");
 		return (new Scanner(System.in)).nextLine();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see display.Display#getPlayerColour()
 	 */
 	@Override
 	public String getPlayerColour()
 	{
 		System.out.println("Please enter your colour.");
 		@SuppressWarnings("resource")
 		Scanner in = new Scanner(System.in);
 		String input = in.nextLine();
 		while (!(input.contentEquals("b") || input.contentEquals("w")))
 		{
 			System.out.println("Invalid Input. Please enter b for Black "
 					+ "and w for White.");
 			input = in.nextLine();
 		}
 		return getColour(input);
 	}
 
 	private String getColour(String input)
 	{
 		if (input.contentEquals("b"))
 		{
 			return "BLACK";
 		} else
 		{
 			return "WHITE";
 		}
 	}
 
 	@Override
 	@SuppressWarnings("resource")
 	public int getMoveRow(int min, int max)
 	{
 		System.out.println("Please enter a move. Eg. 1a to move piece in "
 				+ "row 1 and column a.");
 
 		String input = "";
 		while (true)
 		{
 			input = new Scanner(System.in).nextLine();
 			if (validateInput(input)) return -1;
 			System.out.println("Please enter a move. Eg. 1a to move piece in "
 					+ "row 1 and column a.");
 		}
 	}
 	
 	@Override
 	public int getMoveColumn(int min, int max)
 	{
 		return -1;
 		
 	}
 
 	private boolean validateInput(String input)
 	{
 		try 
 		{
 			if (input.charAt(input.length()-1) >= 'a' 
 					&& input.charAt(input.length()-1) <= 'z'
 					&& Integer.parseInt(input.substring(0,input.length()-1))> 0)
 			{
 				return true;
 			}
 		} catch (NumberFormatException e) { return false;}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see display.Display#displayBoard(game.Board)
 	 */
 	@Override
 	public void displayBoard(Board board)
 	{
 		String header = "";
 		String bottomAndTop = " ";
 		for (int i = 0; i < board.getRows(); i++)
 		{
 			bottomAndTop += "____";
 			header += "   " + (char) (i + (int) 'a');
 		}
 		System.out.println(header);
 
 		System.out.println(bottomAndTop + "_");
 		int start = 0;
 		for (Piece[] row : board.getBoard())
 		{
 			System.out.print(start++);
 			for (Piece p : row)
 			{
 				if (p == null)
 				{
 					System.out.print("|   ");
 				} else if (p.getColour() == Colour.BLACK)
 				{
 					System.out.print("| b ");
 				} else if (p.getColour() == Colour.WHITE)
 				{
 					System.out.print("| w ");
 				}
 			}
 			System.out.println("|");
 		}
 		System.out.println(bottomAndTop + "_");
 	}
 
 	public static void main(String[] args)
 	{
 		Display display = new CommandLineDisplay();
 		// String name = display.getPlayerName();
 		// System.out.println(name);
 		// Colour colour = display.getPlayerColour();
 		// System.out.println(colour);
 		Board board = new Board(10, 10);
 		Colour curr = Colour.BLACK;
		for (int r = 0; r < board.getRows(); r+=2)
 		{
 			for (int c = 0; c < board.getColumns(); c++)
 			{
 				board.setPiece(r, c, new ReversiPiece(curr));
 				curr = (curr==Colour.BLACK)? Colour.WHITE : Colour.BLACK;
 			}
 			curr = (curr==Colour.BLACK)? Colour.WHITE : Colour.BLACK;
 		}
 		display.displayBoard(board);
 	}
 }
