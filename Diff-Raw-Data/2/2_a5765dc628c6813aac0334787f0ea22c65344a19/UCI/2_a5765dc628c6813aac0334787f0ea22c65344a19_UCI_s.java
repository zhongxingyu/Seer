 package uci;
 
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import state4.MoveEncoder;
 import state4.State4;
 import uci.UCIMove.MoveType;
 import util.FenParser;
 
 public final class UCI {
 	private UCIEngine engine;
 	private Position pos;
 	
 	private final Thread t = new Thread(){
 		@Override
 		public void run(){
 			Scanner scanner = new Scanner(System.in);
 			
 			while(scanner.hasNextLine()){
 				String interfaceCommand = scanner.nextLine();
 				
 				interfaceCommand = interfaceCommand.replace("\r", "");
 				
 				String[] s = interfaceCommand.split("\\s+");
 				
 				
 				if(s[0].equalsIgnoreCase("uci")){
 					send("id name "+engine.getName());
 					send("id author Jack Crawford");
 					send("uciok");
 				} else if(s[0].equalsIgnoreCase("ucinewgame")){
 					engine.resetEngine();
 					pos = Position.startPos();
 				} else if(s[0].equalsIgnoreCase("position")){
 					if(s[1].equalsIgnoreCase("fen")){
 						Pattern fenSel = Pattern.compile("fen ((.*?\\s+){5}.*?)(\\s+|$)");
 						Matcher temp = fenSel.matcher(interfaceCommand);
 						temp.find();
 						pos = FenParser.parse(temp.group(1));
 					} else if(s[1].equalsIgnoreCase("startpos")){
 						pos = Position.startPos();
 					}
 					
 					Pattern moveSel = Pattern.compile("moves\\s+(.*)");
 					Matcher temp = moveSel.matcher(interfaceCommand);
 					if(temp.find()){
 						int turn = pos.sideToMove;
 						String moves = temp.group(1);
 						String[] ml = moves.split("\\s+");
 						for(int a = 0; a < ml.length; a++){
 							UCIMove m = parseMove(ml[a]);
 							long encoding = 0;
 							if(m.type == UCIMove.MoveType.Normal){
 								encoding = pos.s.executeMove(turn, 1L<<m.move[0], 1L<<m.move[1], m.ptype.getCode());
 							} else if(m.type == UCIMove.MoveType.Null){
 								pos.s.nullMove();
 							}
 							pos.s.resetHistory();
 							turn = 1-turn;
 							pos.fullMoves++;
 							pos.halfMoves = MoveEncoder.getTakenType(encoding) == State4.PIECE_TYPE_EMPTY ||
 									m.type == MoveType.Null? 0: pos.halfMoves+1;
 						}
 						pos.sideToMove = turn;
 					}
 				} else if(s[0].equalsIgnoreCase("isready")){
 					send("readyok");
 				} else if(s[0].equalsIgnoreCase("stop")){
 					engine.stop();
 				} else if(s[0].equalsIgnoreCase("go")){
 					GoParams params = new GoParams(interfaceCommand);
 					engine.go(params, pos);
 				} else if(s[0].equalsIgnoreCase("quit")){
 					break;
 				} else if(s[0].equalsIgnoreCase("print")){ //print state information
 					if(pos == null){
 						System.out.println("no state information");
 					} else{
 						System.out.println("side to move: "+pos.sideToMove);
 						System.out.println(pos.s);
 					}
 				}
 			}
 			scanner.close();
 		}
 	};
 	
 	private void send(String s){
 		System.out.println(s);
 		System.out.flush();
 	}
 	
 	private static UCIMove parseMove(String move){
 		UCIMove m = new UCIMove();
 		if(move.equals("0000")){
 			m.type = UCIMove.MoveType.Null;
 			return m;
 		}
 		
 		m.type = UCIMove.MoveType.Normal;
 		move = move.toLowerCase();
 		m.move[0] = move.charAt(0)-'a'+(move.charAt(1)-'1')*8;
 		m.move[1] = move.charAt(2)-'a'+(move.charAt(3)-'1')*8;
 		if(move.length() == 5){
 			char promotion = move.charAt(4);
 			if(promotion == 'q'){
 				m.ptype = UCIMove.PromotionType.Queen;
 			} else if(promotion == 'b'){
 				m.ptype = UCIMove.PromotionType.Bishop;
 			} else if(promotion == 'r'){
 				m.ptype = UCIMove.PromotionType.Rook;
 			} else if(promotion == 'n'){
 				m.ptype = UCIMove.PromotionType.Knight;
 			}
 		}
 		
 		return m;
 	}
 	
 	public UCI(final int hashSize, final int pawnHashSize){
 		engine = new RibozymeEngine(hashSize, pawnHashSize);
 		t.start();
 	}
 	
 	public static void main(String[] args){
 		int hashSize = 20; //hash size, as a power of 2
 		int pawnHashSize = 16;
 		for(int a = 0; a < args.length; a++){
 			try{
 				if(args[a].equals("--hash")){
 					hashSize = Integer.parseInt(args[++a]);
 				} else if(args[a].equals("--pawnHash")){
 					pawnHashSize = Integer.parseInt(args[++a]);
 				} 
 			} catch(Exception e){
 				System.out.println("error, incorrect args");
 				System.exit(1);
 			}
 		}
 		new UCI(hashSize, pawnHashSize);
 	}
 }
