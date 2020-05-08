 package pacman;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EnumMap;
 import java.util.LinkedList;
 import java.util.Random;
 
 import pacman.controllers.Controller;
 import pacman.controllers.HumanController;
 import pacman.controllers.KeyBoardInput;
 import pacman.controllers.examples.AggressiveGhosts;
 import pacman.controllers.examples.Legacy;
 import pacman.controllers.examples.Legacy2TheReckoning;
 import pacman.controllers.examples.NearestPillPacMan;
 import pacman.controllers.examples.NearestPillPacManVS;
 import pacman.controllers.examples.RandomGhosts;
 import pacman.controllers.examples.RandomNonRevPacMan;
 import pacman.controllers.examples.RandomPacMan;
 import pacman.controllers.examples.StarterGhosts;
 import pacman.controllers.examples.StarterPacMan;
 import pacman.entries.ghosts.MyGhosts;
 import pacman.game.Game;
 import pacman.game.GameView;
 import pacman.game.Constants.GHOST;
 import pacman.game.Constants.MOVE;
 import static pacman.game.Constants.*;
 import edu.ucsc.gameAI.*;
 import edu.ucsc.gameAI.conditions.*;
 import edu.ucsc.gameAI.decisionTrees.binary.BinaryDecision;
 import edu.ucsc.gameAI.fsm.*;
 import edu.ucsc.gameAI.hfsm.HFSM;
 import edu.ucsc.gameAI.hfsm.HState;
 import edu.ucsc.gameAI.hfsm.HTransition;
 import edu.ucsc.gameAI.hfsm.IHFSM;
 import edu.ucsc.gameAI.hfsm.IHState;
 import edu.ucsc.gameAI.hfsm.IHTransition;
import edu.ucsc.gameAI.hfsm.IResult;
 
 /**
  * This class may be used to execute the game in timed or un-timed modes, with or without
  * visuals. Competitors should implement their controllers in game.entries.ghosts and 
  * game.entries.pacman respectively. The skeleton classes are already provided. The package
  * structure should not be changed (although you may create sub-packages in these packages).
  */
 @SuppressWarnings("unused")
 public class Evaluator
 {	
 	boolean bLeftState; // for hfsm test
 	int ix1 = 0;
 	int ix2 = 110;
 	int iy1 = 0;
 	int iy2 = 60;
 	int iy3 = 120;
 	State stateRun;
 	State stateChase;
 	Transition transScared;
 	LinkedList<ITransition> listtscared ;
 	Transition transCool ;
 	LinkedList<ITransition> listtcool ;
 	StateMachine fsm ;
 	IHFSM hfsm ;
 	IHFSM upIsUp ;
 	IHFSM upIsDown ;
 	Collection<IHState> statesLR;
 	Collection<IHState> statesUpIsUp;		
 	Collection<IHState> statesUpIsDown;
 	IHState neutralUU;
 	IHState upUU;
 	IHState downUU;
 	IHState neutralUD;
 	IHState upUD;
 	IHState downUD;
 	IHTransition leftLR;
 	IHTransition rightLR;
 	IHTransition neutralUpUU;
 	IHTransition neutralDownUU;
 	IHTransition downUpUU;
 	IHTransition upDownUU;
 	IHTransition neutralUpUD;
 	IHTransition neutralDownUD;
 	IHTransition upUpUD;
 	IHTransition downDownUD;
 	
 	public Evaluator()
 	{
 		//create fsm
 		stateRun = new State();
 		stateRun.setAction(new GoDownAction());
 		stateChase = new State();
 		stateChase.setAction(new GoUpAction());
 		transScared = new Transition();
 		transScared.setCondition(new PacmanInRegion(ix1,iy1,ix2,iy2)); // top half of the screen
 		transScared.setTargetState(stateRun);
 		listtscared = new LinkedList<ITransition>();
 		listtscared.add(transScared);
 		transCool = new Transition();
 		transCool.setCondition(new PacmanInRegion(ix1,iy2+1,ix2,iy3)); // bottom of screen (screen is (4,4) to (104,116))
 		transCool.setTargetState(stateChase);
 		listtcool = new LinkedList<ITransition>();
 		listtcool.add(transCool);
 		stateRun.setTransitions(listtcool);
 		stateChase.setTransitions(listtscared);
 		fsm = new StateMachine();
 		fsm.setCurrentState(stateChase);
 		
 		//create hfsm
 		hfsm = new HFSM();
 		upIsUp = new HFSM();
 		upIsDown = new HFSM();
 		
 		statesLR = new ArrayList<IHState>();
 		statesUpIsUp = new ArrayList<IHState>();		
 		statesUpIsDown = new ArrayList<IHState>();
 		
 		
 		//create states and add actions
 		neutralUU = new HState("neutralUU");
		//neutralUU.setAction(new NeutralAction());
 		upUU = new HState("upUU");
 		upUU.setAction(new GoUpAction());
 		downUU = new HState("downUU");
 		downUU.setAction(new GoDownAction());
 		
 		neutralUD = new HState("neutralUD");
		//neutralUD.setAction(new NeutralAction());
 		upUD = new HState("upUD");
 		upUD.setAction(new GoUpAction());
 		downUD = new HState("downUD");
 		downUD.setAction(new GoDownAction());
 
 		
 		//create and add transitions to HFMS and states.
 		leftLR = new HTransition(upIsUp, new PacmanLastMove(MOVE.LEFT));
 		rightLR = new HTransition(upIsDown, new PacmanLastMove(MOVE.RIGHT));
 		upIsUp.addTransition(rightLR);
 		upIsDown.addTransition(leftLR);
 		
 		
 		neutralUpUU = new HTransition(upUU, new PacmanLastMove(MOVE.UP));
 		neutralDownUU = new HTransition(downUU, new PacmanLastMove(MOVE.DOWN));
 		neutralUU.addTransition(neutralUpUU);
 		neutralUU.addTransition(neutralDownUU);
 		
 		
 		upDownUU = new HTransition(downUU, new PacmanLastMove(MOVE.DOWN));
 		upUU.addTransition(upDownUU);
 		downUpUU = new HTransition(upUU, new PacmanLastMove(MOVE.UP));
 		downUU.addTransition(downUpUU);
 		
 		neutralUpUD = new HTransition(downUD, new PacmanLastMove(MOVE.UP));
 		neutralDownUD = new HTransition(upUD, new PacmanLastMove(MOVE.DOWN));
 		neutralUD.addTransition(neutralUpUD);
 		neutralUD.addTransition(neutralDownUD);
 		
 		upUpUD = new HTransition(downUD, new PacmanLastMove(MOVE.UP));
 		upUD.addTransition(upUpUD);
 		downDownUD = new HTransition(upUD, new PacmanLastMove(MOVE.DOWN));
 		downUD.addTransition(downDownUD);
 		
 		//add the states to the FSM.
 		statesLR.add(upIsUp);
 		statesLR.add(upIsDown);
 		hfsm.setStates(statesLR);
 		hfsm.setInitialState(upIsUp); // left state
 		
 		statesUpIsUp.add(neutralUU);
 		statesUpIsUp.add(upUU);
 		statesUpIsUp.add(downUU);
 		upIsUp.setStates(statesUpIsUp);
 		upIsUp.setInitialState(neutralUU);
 
 		statesUpIsDown.add(neutralUD);
 		statesUpIsDown.add(upUD);
 		statesUpIsDown.add(downUD);
 		upIsDown.setStates(statesUpIsDown);
 		upIsDown.setInitialState(neutralUD);
 		
 		// for testing hfsm
 		bLeftState = true;
 		
 	}
 	
     // could be a function run every frame of runExperiment, accepting game as a parameter
     public void runUnitTests(Game game, Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController)
     {
     	// test 1: is blinky edible
 		IsEdible edible = new IsEdible(GHOST.BLINKY);
 		// OR if (edible.test(game) == game.isGhostEdible(GHOST.BLINKY))
 		if (edible.test(game) == false)
 		{
 			//System.out.println("Test 1 passed");
 		}
 		else
 		{
 			//System.out.println("Test 1 failed");
 		}
 	
 		
 		// Game State Condition Tests
 		if (new MazeIndex(game.getMazeIndex()).test(game))
 			;//System.out.println("MazeIndex passed");
 		else
 			System.out.println("MazeIndex failed");
 		if (new LevelCount(game.getCurrentLevel()).test(game))
 			;//System.out.println("LevelCount passed");
 		else
 			System.out.println("LevelCount failed");
 		if (new CurrentLevelTime(game.getCurrentLevelTime()-1,game.getCurrentLevelTime()+1).test(game))
 			;//System.out.println("CurrentLevelTime passed");
 		else
 			System.out.println("CurrentLevelTime failed");
 		if (!new TotalTime(game.getTotalTime()-1,game.getTotalTime()+1).test(game))
 			System.out.println("TotalTime failed");
 		if (!new Score(game.getScore()-1,game.getScore()+1).test(game))
 			System.out.println("Score failed");		
 		if (!new GhostEatScore(game.getGhostCurrentEdibleScore()-1,game.getGhostCurrentEdibleScore()+1).test(game))
 			System.out.println("GhostEatScore failed");
 		if (!new TimeOfLastGlobalReversal(game.getTimeOfLastGlobalReversal()-1,game.getTimeOfLastGlobalReversal()+1).test(game))
 			System.out.println("TimeOfLastGlobalReversal failed");
 		if (!(new PacmanWasEaten().test(game) == game.wasPacManEaten()))
 			System.out.println("PacmanWasEaten failed");
 		if (!(new PillWasEaten().test(game) == game.wasPillEaten()))
 			System.out.println("PillWasEaten failed");
 		if (!(new PowerPillWasEaten().test(game) == game.wasPowerPillEaten()))
 			System.out.println("PowerPillWasEaten failed");
 		for (int pill : game.getPillIndices())
 			if (!(new IsPillStillAvailable(pill).test(game) == game.isPillStillAvailable(pill)))
 				System.out.println("IsPillStillAvailable failed");
 		for (int pill : game.getPillIndices())
 			if (!(new IsPowerPillStillAvailable(pill).test(game) == game.isPowerPillStillAvailable(pill)))
 				System.out.println("IsPowerPillStillAvailable failed");
 		for (GHOST ghost : GHOST.values())
 			if (!(new GhostEaten(ghost).test(game) == game.wasGhostEaten(ghost)))
 			//if (!(new GhostEaten(ghost).test(game) == game.isGhostEdible(ghost)))
 				System.out.println("GhostEaten failed");
 		
 		// Ghost State Conditions
 		for (GHOST ghost : GHOST.values())
 		{
 			if (!(new CurrentGhostNodeIndex(ghost, game.getGhostCurrentNodeIndex(ghost))).test(game))
 				System.out.println("CurrentGhostNodeIndex failed");
 			if (!(new EdibleTime(ghost, game.getGhostEdibleTime(ghost)-1,game.getGhostEdibleTime(ghost)+1)).test(game))
 				System.out.println("EdibleTime failed");
 			if (!(new LairTime(ghost, game.getGhostLairTime(ghost)-1,game.getGhostLairTime(ghost)+1)).test(game))
 				System.out.println("GhostEaten failed");
 			if (!(new GhostLastMove(ghost, game.getGhostLastMoveMade(ghost))).test(game))
 				System.out.println("GhostLastMove failed");
 		}
 		
 		// Ms Pac ManState Conditions
 		if (!(new CurrentPacmanNodeIndex(game.getPacmanCurrentNodeIndex())).test(game))
 			System.out.println("CurrentPacmanNodeIndex failed");
 		if (!(new NumberOfLivesRemaining(game.getPacmanNumberOfLivesRemaining()-1,game.getPacmanNumberOfLivesRemaining()+1).test(game)))
 			System.out.println("NumberOfLivesRemaining failed");
 		if (!(new PacmanLastMove(game.getPacmanLastMoveMade()).test(game)))
 			System.out.println("PacmanLastMove failed");
 	
 		// Inference Conditions
 		int px;
 		int py;
 		for (int pill : game.getActivePillsIndices())
 		{
 			px = game.getNodeXCood(pill);
 			py = game.getNodeYCood(pill);
 			if (!(new PillInRegion(px-1,py-1,px+1,py+1).test(game)))
 				System.out.println("PillInRegion failed");
 		}
 		for (int ppill : game.getActivePowerPillsIndices())
 		{
 			px = game.getNodeXCood(ppill);
 			py = game.getNodeYCood(ppill);
 			if (!(new PowerPillInRegion(px-1,py-1,px+1,py+1).test(game)))
 				System.out.println("PowerPillInRegion failed");
 		}
 		for (GHOST ghost : GHOST.values())
 		{
 			int ig = game.getGhostCurrentNodeIndex(ghost);
 			int gx = game.getNodeXCood(ig);
 			int gy = game.getNodeYCood(ig);
 			if (!(new GhostInRegion(gx-1,gy-1,gx+1,gy+1).test(game)))
 				System.out.println("GhostInRegion failed");
 		}
 		int ipac = game.getPacmanCurrentNodeIndex();
 		int pacx = game.getNodeXCood(ipac);
 		int pacy = game.getNodeYCood(ipac);
 		if (!(new PacmanInRegion(pacx-1,pacy-1,pacx+1,pacy+1).test(game)))
 			System.out.println("PacmanLastMove failed");
 		
 		// Decision Tree
 		BinaryDecision root = new BinaryDecision();
 		ipac = game.getPacmanCurrentNodeIndex();
 		pacx = game.getNodeXCood(ipac);
 		pacy = game.getNodeYCood(ipac);
 		root.setCondition(new PacmanInRegion(pacx-1,pacy-1,pacx+1,pacy+1));
 		root.setTrueBranch(new GoUpAction());
 		root.setFalseBranch(new GoDownAction());
 		if (root.makeDecision(game).getClass() != GoUpAction.class)
 			System.out.println("Decision Tree failed");
 			
 		// FSM (up when pacman in the bottom half of screen, down otherwise)
 		
 		Collection<IAction> actions = fsm.update(game);
 		// test result
 		int ip = game.getPacmanCurrentNodeIndex();
 		pacx = game.getNodeXCood(ip);
 		pacy = game.getNodeYCood(ip);
 		if (ix1 <= pacx && ix2 >= pacx &&
 			iy1 <= pacy && iy2 >= pacy &&
 			actions.size() > 0)
 		{
 			if (actions.iterator().next().getClass() != GoDownAction.class)
 				System.out.println("FSM action result failed");
 			if (fsm.getCurrentState() != stateRun)
 				System.out.println("FSM state failed");
 		}
 			
 		// hfsm
 		boolean bCheck = true;
		IResult r = hfsm.update(game);
 		actions = hfsm.update(game).getActions();
 		if (game.getPacmanLastMoveMade() == MOVE.LEFT && !bLeftState)
 		{
 			bLeftState = true;
 			bCheck = false;
 		}
 		if (game.getPacmanLastMoveMade() == MOVE.RIGHT && bLeftState)
 		{
 			bLeftState = false;
 			bCheck = false;
 		}
 		if (actions.size() < 1)
 			bCheck = false;
 		
 		if (bCheck)
 		{
 			IAction act = actions.iterator().next();
 			MOVE mv = act.getMove();
 			if (game.getPacmanLastMoveMade() == MOVE.UP)
 			{
 				if (bLeftState)
 				{
 					if (mv != MOVE.UP)
 						System.out.println("HFSM fail 1");
 				}	
 				else
 				{
 					if (mv != MOVE.DOWN)
 						System.out.println("HFSM fail 2");
 				}
 			}
 			else if (game.getPacmanLastMoveMade() == MOVE.DOWN)
 			{
 				if (bLeftState)
 				{
 					if (mv != MOVE.DOWN)
 						System.out.println("HFSM fail 3");
 				}	
 				else
 				{
 					if (mv != MOVE.UP)
 						System.out.println("HFSM fail 4");
 				}
 			}
 		}
 		
 		// actions
 		if (!(new GoUpAction().getClass() == GoUpAction.class))
 			System.out.println("GoUpAction failed");
 		if (!(new GoLeftAction().getClass() == GoLeftAction.class))
 			System.out.println("GoLeftAction failed");
 		if (!(new GoRightAction().getClass() == GoRightAction.class))
 			System.out.println("GoRightAction failed");
 		if (!(new GoDownAction().getClass() == GoDownAction.class))
 			System.out.println("GoDownAction failed");
 		
 		
 		
 		
     }
 	
 }
