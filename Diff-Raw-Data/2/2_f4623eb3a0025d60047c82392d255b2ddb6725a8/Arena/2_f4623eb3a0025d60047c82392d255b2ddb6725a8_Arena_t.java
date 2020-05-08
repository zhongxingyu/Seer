 package blueBlox;
 
 import java.text.DecimalFormat;
 import java.util.Vector;
 
 import blueBlox.StateGenerator.SG_TYPE;
 
 public class Arena {
 	private Vector<Room> active_rooms = new Vector<Room>();
 	private Vector<Room> dead_rooms = new Vector<Room>();
 
 	private StateGenerator sg = new StateGenerator(SG_TYPE.RANDOM);
 	private int runs;
 
 	public static void main(String args[]){
 		// 1. Add your player into players
 		Vector<IPlayer> players = new Vector<IPlayer>();
 		players.add(new PlayerRandom());
 		players.add(new PlayerSkeleton());
 
 		// 2. Choose a generator - RANDOM, FIXED, FUNC
 		StateGenerator sg = new StateGenerator(SG_TYPE.RANDOM);
 
 		// 3. Let it rip with the number of runs
 		Arena Rumble = new Arena(players,sg);
 		Rumble.run(1000);
 	}
 
 	public Arena(Vector<IPlayer> players, StateGenerator sg){
 		for(int i=0;i<players.size();i++){
 			active_rooms.add(new Room(players.get(i),new State()));
 		}
 		this.sg = sg;
 	}
 
 	private void run(int num_runs){
 		this.runs = num_runs;
 
 		for(int j=0;j<num_runs;j++){
 //			System.out.println(">>>>> Run #"+j+" running with "+active_rooms.size()+" players");
 
 			int turn = 1;
 
 			while(active_rooms.size() != 0) {
 				int piece = sg.nextPiece(turn);
 
 				for(int i=0;i<active_rooms.size();i++){
 					Room r = active_rooms.get(i);
 
 					r.state.setNextPiece(piece);
 					int move = r.player.pickMove(r.state, r.state.legalMoves());
 					r.state.makeMove(move);
 
 					if(r.state.hasLost()){
 						dead_rooms.add(r);
 						r.collectStat();
 						active_rooms.remove(i);
 						i--;
 					}
 				}
 				turn++;
 			}
 			
 			resetRoomsAndSG();
 //			System.out.println("<<<<< Run #"+j+" completed with "+turn+" turns");
 
 		}
 
 		// When all are dead, ting ting ting!
 		printStats();
 	}
 
 	private void printStats(){
 		System.out.println("===== Arena Statistics =====");
 		System.out.println("#Runs : " + this.runs);
 		System.out.println("Generator Type: " + sg.type);
 		System.out.println("\n===== Player Statistics =====");
 		System.out.println("Name\t\tTurns Lasted\tRows Cleared");
 		
 		for(int i=0;i<active_rooms.size();i++){
 			Room r = active_rooms.get(i);
 			String name = r.player.getClass().getSimpleName();
 			double turns = r.avg_turns;
 			double rows_cleared = r.avg_rows;
 			
 			DecimalFormat df = new DecimalFormat("#.#####");
 			
 			System.out.println(name+"\t\t"+df.format(turns)+"\t"+df.format(rows_cleared));
 		}
 	}
 	
 	private void resetRoomsAndSG(){
 		for(int i=0;i<dead_rooms.size();i++){
 			Room r = dead_rooms.get(i);
 			r.reset();
 			active_rooms.add(r);
 		}
 		dead_rooms.clear();
 
 		sg.reset();
 	}
 }
 
 class Room{
 	public IPlayer player;
 	public State state;
 	public int runs = 0;
 	public double avg_turns = 0.0;
 	public double avg_rows = 0.0;
 
 	public Room(IPlayer p, State s){
 		this.player = p;
 		this.state = s;
 	}
 
 	public void collectStat(){
 		if(runs == 0){
 			avg_turns = state.getTurnNumber();
 			avg_rows = state.getRowsCleared();
 			runs++;
 			return;
 		}
 		avg_turns = ((runs * avg_turns) + state.getTurnNumber()) / (runs + 1); 
		avg_rows = ((runs * avg_rows) + state.getRowsCleared()) / (runs + 1); 		
 		runs++;
 	}
 	
 	public void reset(){
 		state = new State();
 	}
 }
