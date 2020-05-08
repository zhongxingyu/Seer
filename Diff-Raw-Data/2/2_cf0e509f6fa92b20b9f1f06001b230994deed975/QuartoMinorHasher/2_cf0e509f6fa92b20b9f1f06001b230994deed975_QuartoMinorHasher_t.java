 package edu.berkeley.gamesman.hasher;
 
 import java.util.Arrays;
 
 import edu.berkeley.gamesman.util.qll.QuickLinkedList;
 
 public class QuartoMinorHasher {
 	private static long pick(int n, int k) {
 		if (k == 0)
 			return 1L;
 		else
 			return n * pick(n - 1, k - 1);
 	}
 
 	private class Position {
 		private final Position[] inner;
 		private final long offset;
 		private final long numHashes;
 
 		Position(QuickLinkedList<Piece> unused, boolean[] fixedWall,
 				long offset, int remainingPieces) {
 			this.offset = offset;
 			if (remainingPieces == 0
 					|| (fixedWall[0] && fixedWall[1] && fixedWall[2])) {
 				inner = null;
 				numHashes = pick(unused.size(), remainingPieces);
 			} else {
 				inner = new Position[16];
 				QuickLinkedList<Piece>.QLLIterator pieceIter = unused
 						.iterator();
 				long numHashes = 0L;
 				while (pieceIter.hasNext()) {
 					Piece p = pieceIter.next();
 					if (isLowest(p, fixedWall)) {
 						boolean[] newFixedWall = fixedWall.clone();
 						for (int i = 0; i < 3; i++) {
 							if (p.get(i) < p.get(i + 1))
 								newFixedWall[i] = true;
 						}
 						int pNum = p.pieceNum;
 						pieceIter.remove();
 						inner[pNum] = new Position(unused, newFixedWall,
 								offset, remainingPieces - 1);
 						offset += inner[pNum].numHashes;
 						numHashes += inner[pNum].numHashes;
 						pieceIter.add(p);
 					}
 				}
 				this.numHashes = numHashes;
 			}
 		}
 
 		private boolean isLowest(Piece p, boolean[] fixedWall) {
 			for (int i = 0; i < 3; i++) {
 				if (p.get(i) > p.get(i + 1) && !fixedWall[i])
 					return false;
 			}
 			return true;
 		}
 	}
 
 	private class Rotation implements Cloneable {
 		private final int places[];
 		private final boolean[] fixedWall;
 
 		private Rotation() {
 			places = new int[] { 0, 1, 2, 3 };
 			fixedWall = new boolean[3];
 		}
 
 		private Rotation(Rotation r) {
 			places = r.places.clone();
 			fixedWall = r.fixedWall.clone();
 		}
 
 		private void dropState(Piece p) {
 			for (int i = 0; i < 3; i++) {
 				if (!fixedWall[i]) {
 					if (p.get(i) > p.get(i + 1)) {
 						makeSwitch(p, i, i + 1);
 						if (i > 0)
 							i -= 2;
 					}
 				}
 			}
 			for (int i = 0; i < 3; i++) {
 				fixedWall[i] |= p.get(i) < p.get(i + 1);
 			}
 		}
 
 		private void makeSwitch(Piece p, int i1, int i2) {
 			int n1 = p.get(i1), n2 = p.get(i2);
 			p.set(i1, n2);
 			p.set(i2, n1);
 			int t = places[i1];
 			places[i1] = places[i2];
 			places[i2] = t;
 		}
 
 		@Override
 		public Rotation clone() {
 			return new Rotation(this);
 		}
 
 		@Override
 		public String toString() {
 			return Arrays.toString(places);
 		}
 	}
 
 	private class Piece {
 		int pieceNum;
 
 		public Piece(int i) {
 			pieceNum = i;
 		}
 
 		private void applyRotation(Rotation r) {
 			int newNum = 0;
 			for (int i = 0; i < 4; i++) {
 				newNum = QuartoMinorHasher.set(newNum, i, get(r.places[i]));
 			}
 			pieceNum = newNum;
 		}
 
 		public void reverseRotation(Rotation r) {
 			int newNum = 0;
 			for (int i = 0; i < 4; i++) {
 				newNum = QuartoMinorHasher.set(newNum, r.places[i], get(i));
 			}
 			pieceNum = newNum;
 		}
 
 		private int get(int i) {
 			return QuartoMinorHasher.get(pieceNum, i);
 		}
 
 		private void set(int i, int n) {
 			pieceNum = QuartoMinorHasher.set(pieceNum, i, n);
 		}
 
 		@Override
 		public String toString() {
 			StringBuilder sb = new StringBuilder(4);
			for (int i = 3; i >= 0; i--) {
 				sb.append(get(i));
 			}
 			return sb.toString();
 		}
 	}
 
 	private static int set(int num, int i, int n) {
 		if (get(num, i) != n)
 			return num ^ (1 << i);
 		else
 			return num;
 	}
 
 	private static int get(int num, int i) {
 		return (num >> i) & 1;
 	}
 
 	private final Position[] tierTables = new Position[17];
 
 	public QuartoMinorHasher() {
 		QuickLinkedList<Piece> unused = new QuickLinkedList<Piece>();
 		for (int i = 1; i < 16; i++) {
 			unused.add(new Piece(i));
 		}
 		for (int i = 1; i <= 16; i++) {
 			tierTables[i] = new Position(unused, new boolean[] { false, false,
 					false }, 0L, i - 1);
 		}
 	}
 
 	public long numHashesForTier(int tier) {
 		return tierTables[tier].numHashes;
 	}
 
 	private long hash(int[] board) {
 		Piece[] myBoard = new Piece[board.length];
 		for (int i = 0; i < board.length; i++) {
 			myBoard[i] = new Piece(board[i] ^ board[0]);
 		}
 		Rotation rot = new Rotation();
 		Position p = tierTables[board.length];
 		int i;
 		for (i = 1; i < board.length; i++) {
 			if (p.inner == null)
 				break;
 			myBoard[i].applyRotation(rot);
 			rot.dropState(myBoard[i]);
 			p = p.inner[myBoard[i].pieceNum];
 			if (p == null)
 				throw new NullPointerException();
 		}
 		long hash = p.offset;
 		for (; i < board.length; i++) {
 			myBoard[i].applyRotation(rot);
 			int num = myBoard[i].pieceNum;
 			for (int k = 0; k < i; k++)
 				if (myBoard[k].pieceNum < myBoard[i].pieceNum)
 					num--;
 			hash += num * pick(16 - i - 1, board.length - i - 1);
 		}
 		return hash;
 	}
 
 	public void unhash(long hash, int[] board) {
 		Position p = tierTables[board.length];
 		boolean[] used = new boolean[16];
 		board[0] = 0;
 		used[0] = true;
 		int i;
 		for (i = 1; i < board.length; i++) {
 			if (p.inner == null)
 				break;
 			Position nextP = null;
 			int nextK = -1;
 			for (int k = 0; k < 16; k++) {
 				if (p.inner[k] != null) {
 					if (p.inner[k].offset <= hash) {
 						nextP = p.inner[k];
 						nextK = k;
 					} else
 						break;
 				}
 			}
 			board[i] = nextK;
 			used[nextK] = true;
 			p = nextP;
 		}
 		hash -= p.offset;
 		for (; i < board.length; i++) {
 			long div = pick(16 - i - 1, board.length - i - 1);
 			int nextNum = (int) (hash / div);
 			hash = hash % div;
 			for (int k = 0; k <= nextNum; k++) {
 				if (used[k])
 					nextNum++;
 			}
 			board[i] = nextNum;
 			used[nextNum] = true;
 		}
 	}
 
 	public static void main(String[] args) {
 		QuartoMinorHasher qmh = new QuartoMinorHasher();
 		long[] sizes = new long[17];
 		for (int i = 1; i <= 16; i++) {
 			sizes[i] = qmh.numHashesForTier(i);
 		}
 		System.out.println(Arrays.toString(sizes));
 		int[] b = new int[5];
 		for (long i = 0L; i < 1575; i++) {
 			qmh.unhash(i, b);
 			System.out.println(Arrays.toString(b));
 		}
 	}
 }
