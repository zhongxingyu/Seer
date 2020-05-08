 package mitzi;
 
 import java.util.LinkedList;
 
 /*
  * Size of the class:
  * Header:	   8 bytes
  * short :     2 bytes
  * Boolean:    16 bytes (8 header + 1 boolean + 7 round up to multiple of 8)
  * boolean:    1 byte
  * 2*byte:	   2 bytes
  * Flag: 	   4 bytes (like int? )
  * IMove:      16 bytes ( 8 header + 2 short+ 2 short + 4 Piece (like int) + 0 round up to multiple of 8)
  * long:       8 bytes
  * linkedList: 24 + (8 + 24 + 16)k = 24 + 48k bytes  
  * 				(list: 8 header + 8 reference to first elem + 4 size + 8k reference to each node + 4 round up 24+ 8k)
  * 				 (node: 8 header + 8 reference to next node + 8 reference to class = 24)
  * 				 (class: 16 byte)	
  * 
  * round up:   7 byte
  * 	total size: 88 + 48k bytes. 
  * 
  * k = 3 : 232 bytes
  * k = 5 : 328 bytes
  * k =10 : 568 bytes
  * 
  * lets assume that a single analysis need about  500 bytes.
  * Then a 500 MB TranspositionTable can hold about 1 Mio entries.
  * 
  * http://www.javamex.com/tutorials/memory/object_memory_usage.shtml
  * http://www.sandeshshrestha.com/blog/memory-used-by-java-data-types/
  */
 
 public final class AnalysisResult {
 
 	/**
 	 * The boards score in centipawns.
 	 */
 	public short score;
 
 	/**
 	 * If true, the board is a stalemate position. I. e. no moves are possible
 	 * but there is no check. If null, then it has not been analyzed.
 	 */
 	public Boolean is_stalemate;
 
 	/**
 	 * If the evaluation method considers this board to be in an unstable state
 	 * and recommends a deeper evalutation or is simply not sure, this is set to
 	 * true.
 	 */
 	public boolean needs_deeper;
 	/**
 	 * The distance to (complete) search depth at which this result was
 	 * obtained.
 	 */
 	public byte plys_to_eval0 = 0;
 
 	/**
 	 * The distance to selective search depth at which this result was obtained.
 	 */
 	public byte plys_to_seldepth = 0;
 
 	/**
 	 * The state of the result in alpha-beta search: exact, fail-high or
 	 * fail-low
 	 */
 	public Flag flag;
 
 	/**
 	 * The best move from current board.
 	 */
 	public IMove best_move;
 
 	/**
 	 * Since AnalysisResults are stored in the Transposition Tables
 	 * (ResultCache), it is important to ensure that the AnalysisResult
 	 * corresponding to the actual position should be used, if there are
 	 * collisions with hashvalues. Therefore a second one (this one) is created
 	 * to identify the position and these problems unlikely.
 	 **/
 	public long hashvalue;
 
 	/**
 	 * A sorted list of the better moves in reverse order, i.e. the last moves
 	 * are better, then the first ones.
 	 */
 	public LinkedList<IMove> best_moves = new LinkedList<IMove>();
 
 	AnalysisResult(int score, Boolean is_stalemate, boolean needs_deeper,
 			int plys_to_eval0, int plys_to_seldepth, Flag flag) {
 		this.score = (short) score;
 		this.is_stalemate = is_stalemate;
 		this.needs_deeper = needs_deeper;
 		this.plys_to_eval0 = (byte) plys_to_eval0;
 		this.plys_to_seldepth = (byte) plys_to_seldepth;
 		this.flag = flag;
 	}
 
 	AnalysisResult(int score, Boolean is_stalemate, boolean needs_deeper,
 			int plys_to_eval0, int plys_to_seldepth, Flag flag,
 			IMove best_move, long hashvalue) {
 		this.score = (short) score;
 		this.is_stalemate = is_stalemate;
 		this.needs_deeper = needs_deeper;
 		this.plys_to_eval0 = (byte) plys_to_eval0;
 		this.plys_to_seldepth = (byte) plys_to_seldepth;
 		this.flag = flag;
 		this.best_move = best_move;
 		this.hashvalue = hashvalue;
 	}
 
 	/**
 	 * computes a copy of the analysis result without the list of good moves and
 	 * the hashvalue.
 	 * 
 	 * @return a copy without some elements.
 	 */
 	public AnalysisResult tinyCopy() {
 		return new AnalysisResult(score, is_stalemate, needs_deeper,
 				plys_to_eval0, plys_to_seldepth, null, best_move, 0);
 	}
 
 	/**
 	 * sets all values of analysis result except the list of good moves and the
 	 * hashvalue.
 	 * 
 	 * @param score
 	 *            the new score
 	 * @param is_stalemate
 	 *            the new status of is_stalemate
 	 * @param needs_deeper
 	 *            the new status of needs_deeper
 	 * @param plys_to_eval0
 	 *            the new number of plys to base case.
 	 * @param plys_to_seldepth
 	 *            the new number of plys to base case of selective depth.
 	 * @param flag
 	 *            the new flag
 	 * @param best_move
 	 *            the new best move.
 	 */
 	public void tinySet(int score, boolean is_stalemate, boolean needs_deeper,
 			int plys_to_eval0, int plys_to_seldepth, Flag flag, IMove best_move) {
 		this.score = (short) score;
 		this.is_stalemate = is_stalemate;
 		this.needs_deeper = needs_deeper;
 		this.plys_to_eval0 = (byte) plys_to_eval0;
 		this.plys_to_seldepth = (byte) plys_to_seldepth;
 		this.flag = flag;
 		this.best_move = best_move;
 	}
 
 	/**
 	 * sets all values of analysis result except the list of good moves and the
 	 * hashvalue.
 	 * 
 	 * @param ar
 	 *            the new analysis result
 	 */
 	public void tinySet(AnalysisResult ar) {
 		tinySet(ar.score, ar.is_stalemate, ar.needs_deeper, ar.plys_to_eval0,
 				ar.plys_to_seldepth, ar.flag, ar.best_move);
 	}
 
 	/**
 	 * enables a comparison of two results.
 	 * 
 	 * @param o
 	 *            the other result.
 	 * @return 0 if there are the same or have the same value, 1 if the actual
 	 *         one is more valuable then the other one, -1 else.
 	 */
 	public int compareQualityTo(AnalysisResult o) {
 		if (o == null)
 			throw new NullPointerException();
 
 		if (this == o)
 			return 0;
 
 		// (deeper results)
 		if (this.plys_to_eval0 > o.plys_to_eval0)
 			return 1;
 
 		if (this.plys_to_eval0 == o.plys_to_eval0
 				&& this.plys_to_seldepth == o.plys_to_seldepth)
 			return 0;
 		
 		return -1;
 	}
 
 	/**
 	 * returns the Principal Variation, i.e. a sequence of best moves moves. It
 	 * may be cut off, if entries in the ResultCache are overridden.
 	 * 
 	 * @param pos
 	 *            the position, where the PV starts
 	 * @return a linked list with the PV
 	 */
	public LinkedList<IMove> getPV(IPosition pos, int counter) {
 		LinkedList<IMove> pv = new LinkedList<IMove>();
 		IPosition best_child;
 		AnalysisResult ar;
		if (best_move != null && counter >=0) {
 			pv.add(best_move);
 			best_child = pos.doMove(best_move).new_position;
 			ar = ResultCache.getResult(best_child);
			counter--;
 			if (ar != null)
				pv.addAll(ar.getPV(best_child, counter));
 		}
 		return pv;
 	}
 
 	@Override
 	public String toString() {
 		return "cp: " + score + " depth: " + plys_to_eval0
 				+ (flag != null ? " flag: " + flag : "");
 	}
 }
