package testpackage.interfaces;
 
 import java.util.*;
 import testpackage.shared.ship.*;
 
 public class AITester {
 Engine engine ;
 AI ai1;
 AI ai2;
 List<Class<? extends AI>> competitors; 
 
 public AITester(Class<? extends AI> arg1, Class<? extends AI> arg2) {
 	System.err.println(String.format("New Tester: %s vs %s", arg1.getName(), arg2.getName()));
 	competitors = new ArrayList<Class<? extends AI>>();
 	competitors.add(arg1);
 	competitors.add(arg2);
 	System.err.println("AI1 as white");
 	run();
 	Collections.reverse(competitors);
 	System.err.println("AI2 as white");
 	run();
 }
 
 private void run() {
 	int ai1wins = 0;
 	int ai2wins = 0;
 	for (int i=0; i<100; i++) {
 		int winner = doMatch();
 		if (winner == 0)
 			ai1wins++;
 		else if (winner == 1)
 			ai2wins++;
 		else
 			System.err.println("Draw!");
 	}
 
 	System.err.println(competitors.get(0).getName() + ": " + ai1wins);
 	System.err.println(competitors.get(1).getName() + ": " + ai2wins);
 
 }
 
 private int doMatch() {
 engine = new Engine();
 try {
 ai1 = competitors.get(0).getConstructor().newInstance();
 ai2 = competitors.get(1).getConstructor().newInstance();
 } catch (Exception e) {
 	throw new RuntimeException(e);
 }
 
 ai1.setEngine(engine);
 ai2.setEngine(engine);
 int winner = -1;
 
 while (!engine.isFinished()) {
 	if (engine.getState().isPlayerTurn()) {
 		ai1.playAs(0);
 	} else {
 		ai2.playAs(1);
 	}
 	winner = engine.checkWin().playernr;
 }
 
 return winner;
 
 }
 
 public static void main(String[] args) {
 	//new AITester(GoodAI.class, BadAI.class);
 	new AITester(IntelligentAI.class, GoodAI.class);
 }
 
 }
