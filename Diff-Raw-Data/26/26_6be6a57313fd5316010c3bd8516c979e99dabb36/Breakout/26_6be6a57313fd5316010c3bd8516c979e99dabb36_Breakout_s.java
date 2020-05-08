 package breakout;
 
 import java.util.ArrayList;
import javax.swing.Timer;
 
 public class Breakout {
 	private ArrayList<Moveable> moveables; //moveables need to be passed to execute so that objects can do collision detection against them
 	private DrawGroup drawables; //drawables will be looped through and drawn after each main game loop iteration
 	private ArrayList<CommandGroup> commandGroupStack; // list of all command groups for undo and replay
 	private CommandGroup commands; //current working group of commands for a single loop iteration
 	private Board board; //JPanel and JFrame are in here
 	private static final int TIMER_DELAY = 20;
 	public Breakout(Board board){
 		moveables = new ArrayList<Moveable>();
 		drawables = new DrawGroup();
 		commands = new CommandGroup();
 		commandGroupStack = new ArrayList<CommandGroup>();
 		commandGroupStack.add(commands);
 		this.board = board;
 	}
 	public void start(){
 		while(true){
 			commands.execute(moveables);
 			
 			drawables.draw(board.getCanvas());
 			board.getPanel().repaint();
 			try{
 				Thread.sleep(Breakout.TIMER_DELAY);
 			}
 			catch(InterruptedException e){
 				System.out.println(e);
 			}
 		}
 		// LOOP
 		//copy command group
 		//execute command group
 		//push command group to commandGroupStack
 		
 		//on an undo, pop the commandGroupStack by some pre-defined amount and throw away the popped entries
 		//execute undo() on the new top Group and continue
 		
 		//on a start/reset, throw away the whole commandGroupStack, but first call undo on the first element to reset the game state
 		
 		//on a replay, move to the bottom of the commandGroupStack and loop through it calling undo on each group and then sleeping for TIMER_DELAY
 	}
 	
 	public void registerMoveable(Moveable m){
 		this.moveables.add(m);
 	}
 	public void registerDrawable(Drawable d){
 		this.drawables.add(d);
 	}
 	public void unregisterMoveable(Moveable m){
 		this.moveables.remove(m);
 	}
 	public void unregisterDrawable(Drawable d){
 		this.drawables.remove(d);
 	}
 	public void registerCommand(Commandable command){
 		this.commands.addCommand(command);
 	}
 	public void unregisterCommand(Commandable command){
 		this.commands.removeCommand(command);
 	}
 	
 	public void draw(){
 		
 	}
 
 }
 
