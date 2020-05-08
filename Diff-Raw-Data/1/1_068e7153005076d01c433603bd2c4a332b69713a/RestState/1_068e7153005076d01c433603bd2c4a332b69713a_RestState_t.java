 package gui;
 
 public class RestState implements IState {
 
 	private Game game;
 	
 	public RestState(Game game)
 	{
 		this.game = game;
 	}
 	
 	@Override
 	public boolean gameOver() {
 		return false;
 	}
 
 	@Override
 	public IState execute() {
 		String output = "You are resting for 5 seconds...";
 		System.out.print(output);
 		
 		int i = 5;
 		while (i > 0) {
 			try {
 				Thread.sleep(1000);
 				i--;
 				System.out.print(StringUtils.repeat("\b", output.length())); // 11 backspace to remove the seconds, does not work in Eclipse
 				output = "You are resting for "+i+" seconds";
 				System.out.print(output);
 			} catch (InterruptedException e) {
 				// Thread had a problem, guess we wont be sleeping for too long
 			}
 		}
 
 		System.out.println("You are now awake!");
 		
 		return new HomeScreenState(game);
 	}
 	
 }
