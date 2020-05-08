 package edu.berkeley.gamesman.hasher;
 
 
 import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.Hasher;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * The UniformPieceHasher is a perfect hash given a 1-dimensional board with a fixed list of possible pieces
  * Note that this is only a perfect hash if every location can contain any piece
  * As an example, tic-tac-toe is /not/ a UniformPieceHasher-style game as (|X|-|O|) E {0,1}
  * but this hasher could represent the board XXO-XXO-XXO
  * Use AlternatingRearrangerHasher instead
  * 
  * Few games are like this, so it is mostly a utility hasher used by other hashers piecewise.
  * @author Steven Schlansker
  * @see AlternatingRearrangerHasher
  */
 public final class UniformPieceHasher extends Hasher<char[]> {
 	
 	private char[] parr;
 	HashMap<Character, BigInteger> lookup;
 	BigInteger plen;
 	
 	@Override
 	public void setGame(Game<char[]> game, char[] p){
 		super.setGame(game,p);
 		
 
 		parr = p;
 		lookup = new HashMap<Character,BigInteger>();
 		for(int i = 0; i < p.length; i++)
 			lookup.put(p[i], BigInteger.valueOf(i));
 		plen = BigInteger.valueOf(parr.length);
 	}
 	
 	@Override
 	public BigInteger hash(char[] board, int l) {
 		BigInteger hash = BigInteger.ZERO;
 		for(int i = 0; i < l; i++)
 				hash = hash.multiply(plen).add(lookup.get(board[i]));
 		Util.debug("UPH hashes "+Arrays.toString(board)+" to "+hash);
 		return hash;
 	}
 
 	@Override
 	public char[] unhash(BigInteger ahash, int l) {
 		BigInteger hash = ahash;
 		char[] ret = new char[l];
 		
 		for(int i = l-1; i >= 0;i--){
 				ret[i] = parr[hash.mod(plen).intValue()];
 				hash = hash.divide(plen);
 		}
 		return ret;
 	}
 
 	@Override
 	public BigInteger maxHash(int boardlen) {
 		BigInteger hash = BigInteger.ZERO;
 		for(int i = 0; i < boardlen; i++)
 				hash = hash.multiply(plen).add(lookup.get(parr[parr.length-1]));
 		return hash;
 	}
 
 	@Override
 	public String describe() {
 		return "UPH"+Arrays.toString(parr);
 	}
 	
 }
