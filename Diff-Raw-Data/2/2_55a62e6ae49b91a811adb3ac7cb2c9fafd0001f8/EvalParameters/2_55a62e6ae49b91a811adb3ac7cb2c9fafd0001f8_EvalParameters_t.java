 package eval.expEvalV3;
 
 import java.nio.ByteBuffer;
 
 import eval.Weight;
 
 import state4.State4;
 
 public final class EvalParameters {
 	
 	/** controls score granularity, must be power of 2*/
 	public int granularity = 8;
 
 	//-------------------------------------------------
 	//general weights
 	
 	public int[] materialWeights;
 	/** mobility weights, indexed [piece-type][movement]*/
 	public Weight[][] mobilityWeights;
 	public Weight tempo;
 	public Weight bishopPair;
 	
 	//-------------------------------------------------
 	//king danger
 	
 	/** danger for attacks on the squares around the king, indexed [piece-type]*/
 	public int[] dangerKingAttacks;
 	/** king danger index, indexed [danger]*/
 	public Weight[] kingDangerValues;
 	/** king danger based off location of the king*/
 	public int[][] kingDangerSquares;
 	public int contactCheckQueen;
 	public int contactCheckRook;
 	public int queenCheck;
 	public int rookCheck;
 	public int knightCheck;
 	public int bishopCheck;
 	/** pawn shelter, indexed [pawnCol==kingCol? 1: 0][pawnRow] */
 	public int[][] pawnShelter = new int[0][0];
 	/** pawn shelter, indexed [type][enemyPawn dist from king]
 	 * <p> type = 0:no allied pawn on file; 1=allied pawn on file;
 	 * 2=allied pawn blocking enemy pawn */
 	public int[][] pawnStorm = new int[0][0];
 	
 	//-------------------------------------------------
 	//pawns weights
 	
 	/** passed pawn weight, indexed [row] from white perspective*/
 	public Weight[][] passedPawnRowWeight;
 	/** doubled pawns penalty, indexed [opposed-flag][col]
 	 * <p> note, this value will be added FOR EACH pawn in a row with 2 or more pawns*/
 	public Weight[][] doubledPawns;
 	/** weight for pawns with no supportind pawn in either adjacent col, indexed [opposed-flag][col]*/
 	public Weight[][] isolatedPawns;
 	/** weight for backwards pawns, indexed [opposed-flag][col]*/
 	public Weight[][] backwardPawns;
 	/** pawn chain weights by file*/
 	public Weight[] pawnChain;
 	
 	@Override
 	public String toString(){
 		String s = "";
 		
 		final String[] names = new String[7];
 		names[State4.PIECE_TYPE_EMPTY] = "";
 		names[State4.PIECE_TYPE_BISHOP] = "bishop";
 		names[State4.PIECE_TYPE_ROOK] = "rook";
 		names[State4.PIECE_TYPE_KNIGHT] = "knight";
 		names[State4.PIECE_TYPE_KING] = "king";
 		names[State4.PIECE_TYPE_QUEEN] = "queen";
 		names[State4.PIECE_TYPE_PAWN] = "pawn";
 		
 		s += "material weights:\n";
 		for(int a = 1; a < 7; a++){
 			s += "\t"+names[a]+" = "+materialWeights[a]+"\n";
 		}
 
 		s += "mobility weights:\n";
 		for(int a = 1; a < 7; a++){
 			s += "\t"+names[a]+" = ";
 			for(int q = 0; q < mobilityWeights[a].length; q++){
 				s += q+":"+mobilityWeights[a][q]+", ";
 			}
 			s += "\n";
 		}
 		
 		s += "danger king attacks:\n";
 		for(int a = 1; a < 7; a++){
 			s += "\t"+names[a]+" = "+dangerKingAttacks[a]+"\n";
 		}
 		
 		s += "king danger function:\n";
 		for(int a = 0; a < kingDangerValues.length; a++){
 			s += a+":"+kingDangerValues[a]+", ";
 		}
 		s += "\n";
 		
 		s += "king danger squares (white perspective):\n";
 		for(int a = 0; a < 7; a++){
 			for(int q = 0; q < 8; q++){
 				s += kingDangerSquares[0][a*8+q]+"\t";
 			}
 			s += "\n";
 		}
 		
 		s += "passed pawn row weight (white perspective):\n";
 		for(int a = 1; a < 7; a++){
 			s += a+":"+passedPawnRowWeight[0][a]+", ";
 		}
 		s += "\n";
 		
 		s += "isolated pawn weight, unopposed (by col):\n";
 		for(int a = 0; a < 8; a++){
 			s += a+":"+isolatedPawns[0][a]+", ";
 		}
 		s += "\n";
 		
 		s += "isolated pawn weight, opposed (by col):\n";
 		for(int a = 0; a < 8; a++){
 			s += a+":"+isolatedPawns[1][a]+", ";
 		}
 		s += "\n";
 		
 		s += "doubled pawn weight, unopposed (by col):\n";
 		for(int a = 0; a < 8; a++){
 			s += a+":"+doubledPawns[0][a]+", ";
 		}
 		s += "\n";
 		s += "doubled pawn weight, opposed (by col):\n";
 		for(int a = 0; a < 8; a++){
 			s += a+":"+doubledPawns[1][a]+", ";
 		}
 		s += "\n";
 		
 		s += "backward pawn weight, unopposed (by col):\n";
 		for(int a = 0; a < 8; a++){
 			s += a+":"+backwardPawns[0][a]+", ";
 		}
 		s += "\n";
 		s += "backward pawn weight, opposed (by col):\n";
 		for(int a = 0; a < 8; a++){
 			s += a+":"+backwardPawns[1][a]+", ";
 		}
 		s += "\n";
 		
 		s += "pawn chain weight:\n";
 		for(int a = 0; a < 8; a++){
 			s += a+":"+pawnChain[a]+", ";
 		}
 		s += "\n";
 		
 		final String[] pawnShelterNames = new String[]{"adj king file", "in king file"};
 		s += "pawn shelter:\n";
 		for(int q = 0; q < 2; q++){
 			s += "  "+pawnShelterNames[q]+": ";
 			for(int a = 0; a < 7; a++){
 				s += a+":"+pawnShelter[q][a]+", ";
 			}
 			s+="\n";
 		}
 		
 		final String[] pawnStormNames = new String[]{"no allied", "allied", "blocked enemy"};
 		s += "pawn storm:\n";
 		for(int q = 0; q < 3; q++){
 			s += "  "+pawnStormNames[q]+": ";
 			for(int a = 0; a < 6; a++){
 				s += a+":"+pawnStorm[q][a]+", ";
 			}
 			s+="\n";
 		}
 		
 		s += "tempo = "+tempo+"\n";
 		s += "bishop pair = "+bishopPair+"\n";
 		
 		return s;
 	}
 	
 	public void write(final ByteBuffer b){
 
 		//general values
 		
 		for(int a = 0; a < 7; a++) b.putShort((short)materialWeights[a]);
 		
 		writeMatrix(mobilityWeights, b);
 		
 		tempo.writeWeight(b);
 		bishopPair.writeWeight(b);
 		
 		//king values
 
 		for(int a = 0; a < 7; a++) b.putShort((short)dangerKingAttacks[a]);
 		
 		b.putShort((short)kingDangerValues.length);
 		for(int a = 0; a < kingDangerValues.length; a++) kingDangerValues[a].writeWeight(b);
 		
 		for(int a = 0; a < 2; a++){
 			for(int q = 0; q < 64; q++){
 				b.putShort((short)kingDangerSquares[a][q]);
 			}
 		}
 		
 		b.putShort((short)contactCheckQueen);
 		b.putShort((short)contactCheckRook);
 		b.putShort((short)queenCheck);
 		b.putShort((short)rookCheck);
 		b.putShort((short)knightCheck);
 		b.putShort((short)bishopCheck);
 		
 		writeIntMatrix(pawnShelter, b);
 		writeIntMatrix(pawnStorm, b);		
 		
 		//pawn values
 		
 		writeMatrix(passedPawnRowWeight, b);
 		writeMatrix(doubledPawns, b);
 		writeMatrix(isolatedPawns, b);
 		writeMatrix(backwardPawns, b);
 		writeArray(pawnChain, b);
 	}
 	
 	public void read(final ByteBuffer b){
 
 		materialWeights = new int[7];
 		for(int a = 0; a < 7; a++) materialWeights[a] = b.getShort();
 
 		mobilityWeights = readMatrix(b);
 
 		tempo = Weight.readWeight(b);
 		bishopPair = Weight.readWeight(b);
 
 		//king values
 
 		dangerKingAttacks = new int[7];
 		for(int a = 0; a < 7; a++) dangerKingAttacks[a] = b.getShort();
 
 		kingDangerValues = new Weight[b.getShort()];
 		for(int a = 0; a < kingDangerValues.length; a++) kingDangerValues[a] = Weight.readWeight(b);
 		
 		kingDangerSquares = new int[2][64];
 		for(int a = 0; a < 2; a++){
 			for(int q = 0; q < 64; q++){
 				kingDangerSquares[a][q] = b.getShort();
 			}
 		}
 		
 		contactCheckQueen = b.getShort();
 		contactCheckRook = b.getShort();
 		queenCheck = b.getShort();
 		rookCheck = b.getShort();
 		knightCheck = b.getShort();
 		bishopCheck = b.getShort();
 
 		pawnShelter = readIntMatrix(b);
 		pawnStorm = readIntMatrix(b);
 		
 		//pawn values
 		
 		passedPawnRowWeight = readMatrix(b);
 		doubledPawns = readMatrix(b);
 		isolatedPawns = readMatrix(b);
 		backwardPawns = readMatrix(b);
 		pawnChain = readArray(b);
 	}
 	
 	private static void writeIntMatrix(final int[][] w, final ByteBuffer b){
		b.putShort((short)w.length);
 		for(int a = 0; a < w.length; a++){
 			b.putShort((short)w[a].length);
 			for(int q = 0; q < w[a].length; q++){
 				b.putInt(w[a][q]);
 			}
 		}
 	}
 	
 	private static int[][] readIntMatrix(final ByteBuffer b){
 		int[][] w = new int[b.getShort()][];
 		for(int a = 0; a < w.length; a++){
 			w[a] = new int[b.getShort()];
 			for(int q = 0; q < w[a].length; q++){
 				w[a][q] = b.getInt();
 			}
 		}
 		return w;
 	}
 	
 	private static void writeMatrix(final Weight[][] w, final ByteBuffer b){
 		b.putShort((short)w.length);
 		for(int a = 0; a < w.length; a++){
 			b.putShort((short)w[a].length);
 			for(int q = 0; q < w[a].length; q++){
 				w[a][q].writeWeight(b);
 			}
 		}
 	}
 	
 	private static Weight[][] readMatrix(final ByteBuffer b){
 		Weight[][] w = new Weight[b.getShort()][];
 		for(int a = 0; a < w.length; a++){
 			w[a] = new Weight[b.getShort()];
 			for(int q = 0; q < w[a].length; q++){
 				w[a][q] = Weight.readWeight(b);
 			}
 		}
 		return w;
 	}
 	
 	private static void writeArray(final Weight[] w, final ByteBuffer b){
 		b.putShort((short)w.length);
 		for(int a = 0; a < w.length; a++){
 			w[a].writeWeight(b);
 		}
 	}
 	
 	private static Weight[] readArray(final ByteBuffer b){
 		final Weight[] w = new Weight[b.getShort()];
 		for(int a = 0; a < w.length; a++){
 			w[a] = Weight.readWeight(b);
 		}
 		return w;
 	}
 }
