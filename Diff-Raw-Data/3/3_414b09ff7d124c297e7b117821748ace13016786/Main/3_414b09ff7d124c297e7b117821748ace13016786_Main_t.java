 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Set;
 
 import core.Action;
 import core.Board;
 import core.Piece;
 
 
 public class Main {
 
 	static public Action getAction(BufferedReader reader) throws Exception {
 		String position = reader.readLine();
 		String nextPiece = reader.readLine();
 		Piece piece = Piece.stringToPeace(nextPiece);
 		int x = -1, y = -1;
 		ArrayList<String> inputs = new ArrayList<String>();
 		for (String string : position.split(" ")) {
 			if (!string.trim().isEmpty()) {
 				inputs.add(string);
 			}
 		} 
 		if (inputs.size() == 1) {
 			x = Integer.parseInt(inputs.get(0)) / 10;
 			y = Integer.parseInt(inputs.get(0)) - (x * 10);
 		} else if (inputs.size() == 2) {
 			x = Integer.parseInt(inputs.get(0));
 			y = Integer.parseInt(inputs.get(1));
 		}
 		if(x >= 4 || x < 0 || y >= 4 || y < 0) {
 			throw new Exception();
 		}
 		return new Action(piece, x, y);
 
 	}
 
 	public static void main(String[] args) throws Exception {
 
 		int rounds = 1;
 		boolean quiet = false;
 
 		//TODO: rewrite args parsing
 		String command1 = "java " + args[0];
 		String command2 = "java " + args[1];
 
 
 		for (int i = 0; i < args.length; i++) {
 			if (args[i].trim().equals("-n")) {
 				rounds = Integer.parseInt(args[++i]);
 			} else if (args[i].trim().equals("-q") || args[i].trim().equals("--quiet")) {
 				quiet = true;
 			}
 		}
 		System.out.println("command1: " + command1);
 		System.out.println("command2: " + command2);
 		int[] results = new int[3];
 		for (int i = 0; i < rounds; i++) {
 			
 			Process p1=Runtime.getRuntime().exec(command1); 
 			Process p2=Runtime.getRuntime().exec(command2); 
 			BufferedReader[] readers = new BufferedReader[2];
 			readers[0] = new BufferedReader(new InputStreamReader(p1.getInputStream())); 
 			readers[1] = new BufferedReader(new InputStreamReader(p2.getInputStream())); 
 			BufferedWriter[] writers = new BufferedWriter[2];
 			writers[0] = new BufferedWriter(new OutputStreamWriter(p1.getOutputStream())); 
 			writers[1] = new BufferedWriter(new OutputStreamWriter(p2.getOutputStream())); 
 
 			Board board = new Board();
 			Set<Piece> set = Piece.getPieceSet();
 
 			writers[0].write(".\n");
 			writers[0].flush();
 			String firstPiece = readers[0].readLine();
 			Piece piece = Piece.stringToPeace(firstPiece);
 			if(piece == null){ throw new Exception("First player chose " + firstPiece + ". Could not parse.");}
 			writers[1].write(piece.toString() + "\n");
 			writers[1].flush();
			set.remove(piece);
			
 			int turn = 0;
 			while (true) {
 				turn++;
 				Action action = getAction(readers[turn%2]);
 				if (!board.isEmpty(action.x, action.y)) {
 					throw new Exception("Wrong position " + action.x + " " + action.y);				
 				}
 				board.setPiece(piece, action.x, action.y);
 				piece = action.piece;
 				if (!quiet) {
 					System.out.println(board);
 				}
 				writeAction(writers[(turn+1) % 2], action);
 				if (board.gameOver()) {
 					writers[turn%2].write("Victory\n");
 					writers[(turn+1)%2].write("Defeat\n");
 					p1.destroy();
 					p2.destroy();
 					results[turn%2]++;
 					break;
 				}
 				if(set.isEmpty()){
 					writers[0].write("Tie\n");
 					writers[1].write("Tie\n");
 					p1.destroy();
 					p2.destroy();
 					results[2]++;
 					break;
 				}
 				if (!set.remove(action.piece)) {
 					throw new Exception("Piece not in set: " + action.piece.toString() + " " + set.toString());
 				}
 			}
 		}
 		// printing the results
 		System.out.println("******* results *******");
 		System.out.println(results[0] + "\t: 1. player");
 		System.out.println(results[1] + "\t: 2. player");
 		System.out.println(results[2] + "\t: ties");
 	}
 
 	private static void writeAction(BufferedWriter writer, Action action) throws IOException {
 		writer.write(action.x + " " + action.y + "\n");
 		writer.write(action.piece + "\n");
 		writer.flush();
 	}
 }
