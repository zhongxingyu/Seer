 package pacman.experimentclient;
 
 import static pacman.game.Constants.DELAY;
 
 import java.util.Random;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import pacman.controllers.Controller;
 import pacman.entries.pacman.Parameters;
 import pacman.entries.pacman.MyPacMan;
 import pacman.game.Constants.MOVE;
 import pacman.game.Game;
 
 /**
  * Provides methods for running a game of Ms Pac-Man with the parameters
  * specified by some JavaScript.
  */
 public class ExperimentRunner
 {
 	private ScriptEngine engine;
 	
 	public ExperimentRunner()
 	{
 		ScriptEngineManager factory = new ScriptEngineManager();
         engine = factory.getEngineByName("JavaScript");
 	}
 
 	/**
 	 * Runs a game using the specified script to set parameters, and returns
 	 * the score.
 	 * @param script
 	 * @return The final score of the game.
 	 * @throws ScriptException 
 	 */
 	public int run(String script) throws ScriptException
 	{
 		Parameters parameters = new Parameters();
 		engine.put("parameters", parameters);
 		
 		engine.eval(
 			"importPackage(Packages.pacman.entries.pacman);" +
         	"importPackage(Packages.pacman.entries.pacman.evaluators);" +
         	"importPackage(Packages.pacman.entries.pacman.evaluators.ensemble);" +
         	"importPackage(Packages.pacman.entries.pacman.neuralnetworks);" +
         	"importPackage(Packages.pacman.entries.pacman.neuralnetworks.moveselectionstrategies);" +
         	"importPackage(Packages.pacman.entries.pacman.selectionpolicies);" +
         	"importPackage(Packages.pacman.controllers.examples);" +
         	"with (parameters) { " +
         	script +
         	"}");
 		
 		return run(parameters);
 	}
 	
 	/**
 	 * Runs a game using the specified parameters, and returns the score.
 	 * @param parameters
 	 * @return The final score of the game.
 	 */
 	public int run(Parameters parameters)
 	{
 		Random random = new Random();
 		Game game = new Game(random.nextLong());
 		Controller<MOVE> pacman = new MyPacMan(parameters);
 		
 		while (!game.gameOver())
     	{
     		game.advanceGame(
     			pacman.getMove(game.copy(), System.currentTimeMillis() + DELAY),
     			parameters.opponent.getMove(game.copy(), System.currentTimeMillis() + DELAY)
     		);
     	}
 		
 		return game.getScore();
 	}
 }
