 package y2012;
 import java.util.*;
 import java.io.*;

 class P5 {
 	public static final int MAXN = 400;
 	
 	static class Pair {
 		public int x, y, nidx;
 		public Integer [] neighbors;
 		public int [] inv;
 		public boolean [] seen;
 		public boolean bound;
 		
 		public Pair (int x, int y) {
 			this.x = x;
 			this.y = y;
 			this.nidx = 0;
 			bound = false;
 			neighbors = new Integer [MAXN];
 			inv = new int [MAXN];
 			seen = new boolean [MAXN];
 		}
 		
 		public void addNeighbor(int b) {
 			neighbors [nidx++] = b;
 		}
 		
 		public String pN() {
 			String s = "";
 			for (int i = 0; i < nidx; i++)
 				s += neighbors [i] + " ";
 			return s;
 		}
 		
 		public void sort() {
 			Arrays.sort (neighbors, 0, nidx, new Comparator<Integer> () {
 				public int compare (Integer a, Integer b) {
 					return Double.compare (angle (coord [a]), angle (coord [b]));
 				}
 			});
 			
 			for (int i = 0; i < nidx; i++)
 				inv [neighbors [i]] = i;
 		}
 		
 		public double angle (Pair other) {
 			double c = cross (x, y, x, maxy + 1, other.x, other.y);
 			return Math.acos (c / Math.abs (maxy - y + 1) / this.dist(other)) + (c < 0 ? 180 : 0);
 		}
 		
 		private static double cross (double ax, double ay, double bx, double by, double cx, double cy) {
 			return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
 		}
 		
 		public double dist (Pair other) {
 			return Math.sqrt ((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y));
 		}
 		
 		@Override
 		public int hashCode() {
 			return x << 16 + y;
 		}
 		
 		@Override
 		public boolean equals (Object other) {
 			if (!(other instanceof Pair)) return false;
 			return x == ((Pair)other).x && y == ((Pair)other).y;
 		}
 	}
 	
 	public static HashMap <Pair, Integer> mp;
 	public static HashMap <Integer, Pair> rmp;
 	public static int idx;
 	public static Pair [] coord;
 	private static int maxx, maxy, minx, miny;
 	public static void main (String [] args) throws IOException {
 		Scanner in = new Scanner (System.in);
 		PrintWriter out = new PrintWriter (System.out, true);
 
 		int t = 0;
 		while (true) {
 			int N = in.nextInt();
 			if (N == 0) break;
 			mp = new HashMap<Pair, Integer>();
 			rmp = new HashMap<Integer, Pair>();
 			idx = 0;
 			
 			coord = new Pair [MAXN];
 			for (int i = 0; i < N; i++) {
 				int ax = in.nextInt(), ay = in.nextInt(), bx = in.nextInt(), by = in.nextInt();
 				int a = getIdx (new Pair (ax, ay)), b = getIdx (new Pair (bx, by));
 				coord [a].addNeighbor(b);
 				coord [b].addNeighbor(a);
 			}
 			maxx = -Integer.MAX_VALUE;
 			maxy = -Integer.MAX_VALUE;
 			minx = Integer.MAX_VALUE;
 			miny = Integer.MAX_VALUE;
 			for (int i = 0; i < idx; i++) {
 				if (coord [i].x > maxx)
 					maxx = coord [i].x;
 				if (coord [i].x < minx)
 					minx = coord [i].x;
 				if (coord [i].y > maxy)
 					maxy = coord [i].y;
 				if (coord [i].y < miny)
 					miny = coord [i].y;
 			}
 			int cur = -1, start = -1;
 			for (int i = 0; i < idx; i++) {
 				if (coord [i].x == maxx || coord [i].x == minx)
 					coord [i].bound = true;
 				if (coord [i].y == maxy || coord [i].y == miny)
 					coord [i].bound = true;
 				if (coord [i].x == minx && coord [i].y == miny)
 					start = cur = i;
 				coord [i].sort();
 				System.out.println (i + " " + coord [i].x + " " + coord [i].y + " " + coord[i].pN());
 			}
 			
 			int [] count = new int [201];
 			do {
 				int idx = coord [cur].y == miny && coord [cur].x != minx ? 1 : 0;
 				int next = coord [cur].neighbors[idx];
 
 				if (coord [cur].seen [idx]) {
 					cur = next;
 					continue;
 				}
 				int sides = 1;
 				coord [cur].seen [idx] = true;
 				int vnext = next, inidx = (coord [next].inv[cur] + 1) % coord [next].nidx;
 				while (!coord [vnext].seen [inidx]) {
 					coord [vnext].seen [inidx] = true;
 					int prev = vnext;
 					vnext = coord [vnext].neighbors [inidx];
 					inidx = (coord [vnext].inv[prev] + 1) % coord [vnext].nidx;
 					sides++;
 				}
 				
 				count [sides]++;
 				
 				cur = next;
 			} while (cur != start);
 			
 			
 			out.printf("Case %d:\n", ++t);
 			int sum = 0;
 			for (int i = 0; i < count.length; i++) {
 				if (count [i] > 0) {
 					out.printf ("  Lots with %d surveyor's lines = %d\n", i, count [i]);
 					sum += count [i];
 				}
 			}
 			out.printf ("Total number of lots = %d\n", sum);
 			out.println();
 		}
 		out.close();
 		System.exit(0);
 	}
 	
 	private static int getIdx (Pair p) {
 		if (mp.containsKey (p)) {
 			return mp.get(p);
 		} else {
 			mp.put(p, idx);
 			coord [idx++] = p;
 			return idx - 1;
 		}
 	}
 }
