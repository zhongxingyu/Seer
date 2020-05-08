 package apps.pgggppg.util;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import apps.pgggppg.compilation.NativePropNetStateMachine;
 import apps.team.Connect4Machine;
 
 import util.game.Game;
 import util.game.GameRepository;
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.Role;
 import util.statemachine.StateMachine;
 import util.statemachine.implementation.propnet.PropNetStateMachine;
 import util.statemachine.implementation.prover.ProverStateMachine;
 
 public class MachineEvaluator {
 	
 	public static void main(String[] args) {
 		String gameName = "ticTacToe";
 		GameRepository repository = GameRepository.getDefaultRepository(); 
 		Game game = repository.getGame(gameName);
 		
 		StateMachine[] machines = new StateMachine[] {
 			//new ProverStateMachine(),
 			//new PropNetStateMachine(),
 			//new Connect4Machine(),
 			//new ProverStateMachine()
 		};
 		int depth = 5;
 		int N = 10;
 		
 		MachineEvaluator eval = new MachineEvaluator();
 		for (StateMachine machine: machines) {
 			machine.initialize(game.getRules());
 			for (int i = 0; i < N; i++) {
 				eval.evaluate(machine, depth);
 			}
 		}
 	}
 
 	private Set<MachineState> cache = new HashSet<MachineState>();
 	private StateMachine machine;
 	private int stateCount;
 	private Role role;
 	private Random rand = new Random();
 	
 	private int goalSum = 0;
 	private int goalCount = 0;
 	
 	public void evaluate(StateMachine machine, int depth) {
 		this.machine = machine;
 		stateCount = 0;
 		goalSum = 0;
 		goalCount = 0;
 		cache.clear();
 		role = machine.getRoles().get(1);
 		long start = System.currentTimeMillis();
 		
 		try {
 			evaluate(machine.getInitialState(), depth);
 			for (int i = 0; i < 1000; i++) {
 				//performDepthCharge(machine.getInitialState());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		long end = System.currentTimeMillis();
 		System.out.println("states: " + stateCount);
 		System.out.println("total time: " + (end - start)/1000.0 + " seconds");
 		System.out.println("avg: " + (end - start) * 1000.0 / stateCount + " us per state");
 		if (goalCount > 0) System.out.println("avg goal: " + (goalSum * 1.0 / goalCount));
 		System.out.println();
 	}
 	
 	private void evaluate(MachineState state, int depth) throws Exception {
 		stateCount++;
 		// base case
 		if (depth == 0) {
 			return;
 		}
 		
 		if (cache.contains(state)) {
 			return;
 		}
 		
 		cache.add(state);
 		
 		// terminal
 		if (machine.isTerminal(state)) {
 			goalSum += machine.getGoal(state, role);
 			goalCount++;
 			return;
 		}
 		
 		Map<Move, List<MachineState>> moves = machine.getNextStates(state, role);
 		for (Move move: moves.keySet()) {
 			for (MachineState next: moves.get(move)) {
 				evaluate(next, depth - 1);
 			}
 		}
 	}
 	
 	private void performDepthCharge(MachineState state) throws Exception {
 		while (!machine.isTerminal(state)) {
 			List<MachineState> states = machine.getNextStates(state);
 			state = states.get(rand.nextInt(states.size()));
 			stateCount++;
 		}
 		goalCount++;
 		goalSum += machine.getGoal(state, role);
 		System.out.println(goalSum * 1.0 / goalCount);
 	}
 }
