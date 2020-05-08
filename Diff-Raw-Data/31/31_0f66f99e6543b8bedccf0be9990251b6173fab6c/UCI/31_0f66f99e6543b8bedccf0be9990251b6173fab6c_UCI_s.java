 package uci;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import state4.MoveEncoder;
 import state4.State4;
 import uci.UCIMove.MoveType;
 import util.FenParser;
 
 public final class UCI {
 	private UCIEngine engine = new RibozymeEngine();
 	private FileWriter out;
 	private Position pos;
 	
 	private final Thread t = new Thread(){
 		@Override
 		public void run(){
 			Scanner scanner = new Scanner(System.in);
 			
 			while(scanner.hasNextLine()){
 				String interfaceCommand = scanner.nextLine();
 				debug(interfaceCommand+"\n");
 				
 				interfaceCommand = interfaceCommand.replace("\r", "");
 				
 				debug("parsed command: ");
 				String[] s = interfaceCommand.split("\\s+");
 				for(int i = 0; i < s.length; i++){
 					debug("'"+s[i]+"' ");
 				}
 				debug("\n");
 				
 				
 				if(s[0].equalsIgnoreCase("uci")){
 					debug("processing 'uci' command\n");
 					send("id name ribozyme .1");
 					send("id author ribozyme team");
 					send("uciok");
					//send("\n");
 				} else if(s[0].equalsIgnoreCase("ucinewgame")){
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
 								encoding = pos.s.executeMove(turn, 1L<<m.move[0], 1L<<m.move[1]);
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
 					GoParams params = new GoParams();
 					params.ponder = interfaceCommand.contains("ponder");
 					params.infinite = interfaceCommand.contains("infinite");
 					
 					Matcher temp;
 					
 					Pattern whiteTimeSel = Pattern.compile("wtime\\s+(\\d+)");
 					temp = whiteTimeSel.matcher(interfaceCommand);
 					params.time[0] = temp.find()? Integer.parseInt(temp.group(1)): -1;
 					
 					Pattern blackTimeSel = Pattern.compile("btime\\s+(\\d+)");
 					temp = blackTimeSel.matcher(interfaceCommand);
 					params.time[1] = temp.find()? Integer.parseInt(temp.group(1)): -1;
 					
 					Pattern whiteTimeIncSel = Pattern.compile("winc\\s+(\\d+)");
 					temp = whiteTimeIncSel.matcher(interfaceCommand);
 					params.whiteTimeInc = temp.find()? Integer.parseInt(temp.group(1)): -1;
 					
 					Pattern blackTimeIncSel = Pattern.compile("binc\\s+(\\d+)");
 					temp = blackTimeIncSel.matcher(interfaceCommand);
 					params.blackTimeInc = temp.find()? Integer.parseInt(temp.group(1)): -1;
 					
 					/*Pattern depthSel = Pattern.compile("depth\\s+(\\d+)");
 					temp = depthSel.matcher(interfaceCommand);
 					p.depth = temp.find()? Integer.parseInt(temp.group(1)): -1;
 					*/
 					Pattern moveTimeSel = Pattern.compile("movetime\\s+(\\d+)");
 					temp = moveTimeSel.matcher(interfaceCommand);
 					params.moveTime = temp.find()? Integer.parseInt(temp.group(1)): -1;
 					
 					engine.go(params, pos);
 				} else if(s[0].equalsIgnoreCase("quit")){
 					break;
 				}
 			}
 			scanner.close();
 			try{
 				out.close();
 			} catch(IOException e){
 				e.printStackTrace();
 			}
 		}
 	};
 	
 	private void send(String s){
 		System.out.println(s);
 		System.out.flush();
 		debug("sent: '"+s+"'\n");
 	}
 	
 	private void debug(String s){
 		if(out != null){
 			try{
 				out.write(s);
 				out.flush();
 			} catch(IOException e){}
 		}
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
 	
 	public UCI(){
 		try{
 			out = new FileWriter("uci-log.txt", true);
 			
 			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 			Calendar cal = Calendar.getInstance();
 			out.write("========= "+dateFormat.format(cal.getTime())+" =========\n");
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 		//System.setProperty("line.separator", "\r\n");
 		t.start();
 	}
 	
 	public static void main(String[] args){
 		new UCI();
 	}
 }
