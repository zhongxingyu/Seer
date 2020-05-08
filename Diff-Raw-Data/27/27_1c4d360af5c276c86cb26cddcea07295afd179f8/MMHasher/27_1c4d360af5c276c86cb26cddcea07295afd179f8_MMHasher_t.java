 package edu.berkeley.gamesman.hasher;
 
 import edu.berkeley.gamesman.util.Util;
 
 public class MMHasher {
	public MMBoard board;
 
 	public static void main(String[] args) {
 		char[] input = "XXXXXXXOOOOOXOOXOO".toCharArray();
 		MMBoard test = new MMBoard(input);
 		// test.debugPrint();
 		System.out.println("HASHING:");
 		System.out.println(input);
 		System.out.println(test.xMajorHash);
 		System.out.println(test.oMinorHash);
		MMHasher mmh = new MMHasher();
		mmh.unhash(test.hashX, input, 9, 9);
 		System.out.println("UNHASH =");
 		System.out.println(input);
 	}
 
	public long hash(char[] pieces) {
 		if (board == null)
 			board = new MMBoard(pieces);
 		else
 			board.setPieces(pieces);
 		return board.hashX;
 	}
 
	public void unhash(long hash, char[] pieces, int numX, int numO) {
 		int numSpace = pieces.length - numX - numO;
 		long temp = Util.nCr(numO + numSpace, numO);
 		long xMajorHash = (long) hash / temp;
 		long oMinorHash = hash % temp;
 
 		int i = numO + numSpace - 1;
 		int iPtr = i;
 		temp = MMBoard.reverseQuickComb(temp, numO + numSpace, numO, i, numO);
 		while (i >= 0) {
 			if (temp > oMinorHash || numO == 0) {
 				pieces[i] = ' ';
 				temp = MMBoard.reverseQuickComb(temp, i, numO, i - 1, numO);
 			} else {
 				pieces[i] = 'O';
 				oMinorHash -= temp;
 				temp = MMBoard.reverseQuickComb(temp, i, numO, i - 1, numO - 1);
 				numO--;
 			}
 			i--;
 		}
 
 		i = pieces.length - 1;
 		temp = Util.nCr(i, numX);
 		while (i >= 0) {
 			if (temp > xMajorHash || numX == 0) {
 				pieces[i] = pieces[iPtr];
 				temp = MMBoard.reverseQuickComb(temp, i, numX, i - 1, numX);
 				iPtr--;
 			} else {
 				pieces[i] = 'X';
 				xMajorHash -= temp;
 				temp = MMBoard.reverseQuickComb(temp, i, numX, i - 1, numX - 1);
 				numX--;
 			}
 			i--;
 		}
 
 	}
 }
 
 class MMBoard {
 	MMBoardElement first; // first element of the board
 	long xMajorHash;
 	long xMinorHash;
 	long oMajorHash;
 	long oMinorHash;
 	long hashX;
 	long hashO;
 
 	MMBoard(char[] pieces) {
 		setPieces(pieces);
 	}
 
 	void setPieces(char[] pieces) {
 		xMajorHash = 0;
 		xMinorHash = 0;
 		oMajorHash = 0;
 		oMinorHash = 0;
 		int numX = 0;
 		int numO = 0;
 		int nonXCount = -1;
 		int nonOCount = -1;
 
 		MMBoardElement current = null;
 		MMBoardElement last = null;
 		MMBoardElement lastX = null;
 		MMBoardElement lastO = null;
 
 		for (int i = 0; i < pieces.length; i++) {
 			if (i == 0)
 				current = first;
 			else
 				current = current.next;
 			if (current == null)
 				current = new MMBoardElement(pieces[i], i);
 			else
 				current.reset(pieces[i]);
 			if (i == 0) {
 				first = current;
 			}
 			if (last != null) {
 				last.next = current;
 			}
 			current.prev = last;
 			current.prevX = lastX;
 			current.prevO = lastO;
 
 			if (pieces[i] == 'X') {
 				lastX = current;
 				while (last != null && last.nextX == null) {
 					last.nextX = current;
 					last = last.prev;
 				}
 				nonOCount++;
 				numX++;
 			} else if (pieces[i] == 'O') {
 				lastO = current;
 				while (last != null && last.nextO == null) {
 					last.nextO = current;
 					last = last.prev;
 				}
 				nonXCount++;
 				numO++;
 			} else {
 				nonOCount++;
 				nonXCount++;
 			}
 
 			current.numX = numX;
 			current.numO = numO;
 			current.nonXCount = nonXCount;
 			current.nonOCount = nonOCount;
 
 			current.xMajorHash = getPartialXMajorHash(current);
 			current.xMinorHash = getPartialXMinorHash(current);
 			current.oMajorHash = getPartialOMajorHash(current);
 			current.oMinorHash = getPartialOMinorHash(current);
 
 			if (current.value == 'X') {
 				xMajorHash += current.xMajorHash;
 				xMinorHash += current.xMinorHash;
 			} else if (current.value == 'O') {
 				oMajorHash += current.oMajorHash;
 				oMinorHash += current.oMinorHash;
 			}
 			last = current;
 		}
 		hashX = xMajorHash * Util.nCr(nonXCount + 1, numO) + oMinorHash;
 		hashO = oMajorHash * Util.nCr(nonOCount + 1, numX) + xMinorHash;
 	}
 
 	long getPartialXMajorHash(MMBoardElement e) {
 		if (e.numX <= 1) {
 			return smallComb(e.index, e.numX);
 		}
 		return quickComb(e.prev.xMajorHash, e.prev.index, e.prev.numX, e.index,
 				e.numX);
 	}
 
 	long getPartialXMinorHash(MMBoardElement e) {
 		if (e.numX <= 1) {
 			return smallComb(e.nonOCount, e.numX);
 		}
 		return quickComb(e.prev.xMinorHash, e.prev.nonOCount, e.prev.numX,
 				e.nonOCount, e.numX);
 	}
 
 	long getPartialOMajorHash(MMBoardElement e) {
 		if (e.numO <= 1) {
 			return smallComb(e.index, e.numX);
 		}
 		return quickComb(e.prev.oMajorHash, e.prev.index, e.prev.numO, e.index,
 				e.numO);
 	}
 
 	long getPartialOMinorHash(MMBoardElement e) {
 		if (e.numO <= 1) {
 			return smallComb(e.nonXCount, e.numO);
 		}
 		return quickComb(e.prev.oMinorHash, e.prev.nonXCount, e.prev.numO,
 				e.nonXCount, e.numO);
 	}
 
 	long smallComb(int n, int r) {
 		if (n == 0) {
 			if (r == 0) {
 				return 1;
 			}
 			return 0;
 		}
 		return n;
 	}
 
 	long quickComb(long prev, int prevN, int prevR, int n, int r) {
 		if (prevN == n) {
 			return prev;
 		} else {
 			if (n == r) {
 				return 1;
 			}
 			if (prevR == r) // nCr -> n+1Cr
 			{
 				return prev * n / (n - r);
 			} else // nCr -> n+1Cr+1
 			{
 				return prev * n / r;
 			}
 		}
 	}
 
 	static long reverseQuickComb(long prev, int prevN, int prevR, int n, int r) {
 		if (prevN == n) {
 			return prev;
 		} else {
 			if (n == r) {
 				return 1;
 			}
 			if (n < r) {
 				return 0;
 			}
 			if (prevR == r) // nCr -> n-1Cr
 			{
 				return prev * (prevN - prevR) / prevN;
 			} else // nCr -> n-1Cr-1
 			{
 				return prev * prevR / prevN;
 			}
 		}
 	}
 
 	void debugPrint() {
 		MMBoardElement temp = first;
 		while (temp != null) {
 			System.out.println("Index " + temp.index);
 			System.out.println("XMajorHash: " + temp.index + " C " + temp.numX
 					+ " = " + temp.xMajorHash);
 			System.out.println("OMinorHash: " + temp.nonXCount + " C "
 					+ temp.numO + " = " + temp.oMinorHash);
 			System.out.println("OMajorHash: " + temp.index + " C " + temp.numO
 					+ " = " + temp.oMajorHash);
 			System.out.println("XMinorHash: " + temp.nonOCount + " C "
 					+ temp.numX + " = " + temp.xMinorHash);
 			System.out.println("***********************");
 			temp = temp.next;
 		}
 
 		System.out.println("XMajorHash: " + xMajorHash);
 		System.out.println("OMinorHash: " + oMinorHash);
 		System.out.println("Hash: " + hashX);
 		System.out.println("==========");
 		System.out.println("OMajorHash: " + oMajorHash);
 		System.out.println("XMinorHash: " + xMinorHash);
 		System.out.println("Hash: " + hashO);
 	}
 }
 
 class MMBoardElement {
 	char value; // the piece's char representation
 	int index; // position of the piece in respect to the game
 
 	int numX; // number of X's seen including this piece
 	int numO; // number of O's seen including this piece
 	int nonXCount; // number of not X seen so far
 	int nonOCount; // number of not O seen so far
 
 	long xMajorHash;
 	long xMinorHash;
 	long oMajorHash;
 	long oMinorHash;
 
 	MMBoardElement next;
 	MMBoardElement prev;
 
 	MMBoardElement nextX;
 	MMBoardElement prevX;
 
 	MMBoardElement nextO;
 	MMBoardElement prevO;
 
 	MMBoardElement(char value, int index) {
 		this.value = value;
 		this.index = index;
 		next = null;
 		prev = null;
 		nextX = null;
 		prevX = null;
 		nextO = null;
 		prevO = null;
 	}
 
 	public void reset(char value) {
 		this.value = value;
 		nextX = null;
 		prevX = null;
 		nextO = null;
 		prevO = null;
 	}
 
 }
