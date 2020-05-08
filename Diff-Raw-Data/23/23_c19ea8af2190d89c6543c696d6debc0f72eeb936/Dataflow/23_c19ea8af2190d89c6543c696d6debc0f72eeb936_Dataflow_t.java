 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.LinkedList;
 import java.util.BitSet;
 import java.util.concurrent.LinkedBlockingDeque;
 
 class Random {
 	int w;
 	int z;
 
 	public Random(int seed) {
 		w = seed + 1;
 		z = seed * seed + seed + 2;
 	}
 
 	int nextInt() {
 		z = 36969 * (z & 65535) + (z >> 16);
 		w = 18000 * (w & 65535) + (w >> 16);
 
 		return (z << 16) + w;
 	}
 }
 
 class Vertex {
 	int index;
 	boolean listed;
 	LinkedList<Vertex> pred;
 	LinkedList<Vertex> succ;
 	BitSet in;
 	BitSet out;
 	BitSet use;
 	BitSet def;
 
 	Vertex(int i) {
 		index = i;
 		pred = new LinkedList<Vertex>();
 		succ = new LinkedList<Vertex>();
 		in = new BitSet();
 		out = new BitSet();
 		use = new BitSet();
 		def = new BitSet();
 	}
 
 	void computeIn(LinkedBlockingDeque<Vertex> worklist) {
 		int i;
 		BitSet old;
 		Vertex v;
 		ListIterator<Vertex> iter;
 
 		iter = succ.listIterator();
 
 		while (iter.hasNext()) {
 			v = iter.next();
 			out.or(v.in);
 		}
 
 		old = in;
 
 		// in = use U (out - def)
 
 		in = new BitSet();
 		in.or(out);
 		in.andNot(def);
 		in.or(use);
 
 		if (!in.equals(old)) {
 			iter = pred.listIterator();
 
 			while (iter.hasNext()) {
 				v = iter.next();
 				if (!v.listed) {
 					worklist.addLast(v);
 					v.listed = true;
 				}
 			}
 		}
 	}
 
 	public void print() {
 		int i;
 
 		System.out.print("use[" + index + "] = { ");
 		for (i = 0; i < use.size(); ++i)
 			if (use.get(i))
 				System.out.print("" + i + " ");
 		System.out.println("}");
 		System.out.print("def[" + index + "] = { ");
 		for (i = 0; i < def.size(); ++i)
 			if (def.get(i))
 				System.out.print("" + i + " ");
 		System.out.println("}\n");
 
 		System.out.print("in[" + index + "] = { ");
 		for (i = 0; i < in.size(); ++i)
 			if (in.get(i))
 				System.out.print("" + i + " ");
 		System.out.println("}");
 
 		System.out.print("out[" + index + "] = { ");
 		for (i = 0; i < out.size(); ++i)
 			if (out.get(i))
 				System.out.print("" + i + " ");
 		System.out.println("}\n");
 	}
 
 }
 
 class Dataflow {
 
 	public static void connect(Vertex pred, Vertex succ) {
 		pred.succ.addLast(succ);
 		succ.pred.addLast(pred);
 	}
 
 	public static void generateCFG(Vertex vertex[], int maxsucc, Random r) {
 		int i;
 		int j;
 		int k;
 		int s; // number of successors of a vertex.
 
 		System.out.println("generating CFG...");
 
 		connect(vertex[0], vertex[1]);
 		connect(vertex[0], vertex[2]);
 
 		for (i = 2; i < vertex.length; ++i) {
 			s = (r.nextInt() % maxsucc) + 1;
 			for (j = 0; j < s; ++j) {
 				k = Math.abs(r.nextInt()) % vertex.length;
 				connect(vertex[i], vertex[k]);
 			}
 		}
 	}
 
 	public static void generateUseDef(Vertex vertex[], int nsym, int nactive,
 			Random r) {
 		int i;
 		int j;
 		int sym;
 
 		System.out.println("generating usedefs...");
 
 		for (i = 0; i < vertex.length; ++i) {
 			for (j = 0; j < nactive; ++j) {
 				sym = Math.abs(r.nextInt()) % nsym;
 
 				if (j % 4 != 0) {
 					if (!vertex[i].def.get(sym))
 						vertex[i].use.set(sym);
 				} else {
 					if (!vertex[i].use.get(sym))
 						vertex[i].def.set(sym);
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings( { "unchecked" })
 	public static void liveness(Vertex vertex[], int nthread) {
 		Vertex u;
 		Vertex v;
 		int i;
 		// LinkedBlockingDeque<Vertex>[] worklist;
 		long begin;
 		long end;
 		final LinkedBlockingDeque<Vertex>[] arr;
 
 		System.out.println("computing liveness...");
 
 		begin = System.nanoTime();
 		arr = (LinkedBlockingDeque<Vertex>[]) new LinkedBlockingDeque[nthread];
 
 
 		for (int j = 0; j < nthread; ++j) {
			arr[j] = new LinkedBlockingDeque<Vertex>();
 			int startPos = (vertex.length * j) / nthread;
 			int endPos = (vertex.length * (j + 1)) / nthread;
 			for (; startPos < endPos; ++startPos) {
 				arr[j].addLast(vertex[startPos]);
 				vertex[startPos].listed = true;
 			}
 		}
 
 		for (int k = 0; k < nthread; ++k) {
 			final int fk = k;
 			new Thread() {
 				public void run() {
					System.out.println("Thread-" + fk + " created, worklistsize: " + arr[fk].size());
					while(arr[fk].size() > 0){
						Vertex u = arr[fk].remove();
						u.listed = false;
						u.computeIn(arr[fk]);
					}
 				}
 			}.start();
 		}
 
 		/*
 		 * while (!worklist.isEmpty()) { u = worklist.remove(); u.listed =
 		 * false; u.computeIn(worklist); }
 		 */
		for (int k = 0; k < nthread; ++k) {
			while(arr[k].size() > 0){
				try{
					Thread.sleep(100);
				} catch (Exception e) {}
			}
		}
 
 		end = System.nanoTime();
 
 		System.out.println("T = " + (end - begin) / 1e9 + " s");
 	}
 
 	public static void main(String[] args) {
 		int i;
 		int nsym;
 		int nvertex;
 		int maxsucc;
 		int nactive;
 		int nthread;
 		boolean print;
 		Vertex vertex[];
 		Random r;
 
 		r = new Random(1);
 
 		nsym = Integer.parseInt(args[0]);
 		nvertex = Integer.parseInt(args[1]);
 		maxsucc = Integer.parseInt(args[2]);
 		nactive = Integer.parseInt(args[3]);
 		nthread = Integer.parseInt(args[4]);
 		print = Integer.parseInt(args[5]) != 0;
 
 		System.out.println("nsym = " + nsym);
 		System.out.println("nvertex = " + nvertex);
 		System.out.println("maxsucc = " + maxsucc);
 		System.out.println("nactive = " + nactive);
 
 		if (nthread < 1) {
 			System.out.println("Invalid number of threads.");
 			System.exit(-1); //
 		}
 
 		vertex = new Vertex[nvertex];
 
 		for (i = 0; i < vertex.length; ++i)
 			vertex[i] = new Vertex(i);
 
 		generateCFG(vertex, maxsucc, r);
 		generateUseDef(vertex, nsym, nactive, r);
 		liveness(vertex, nthread);
 
 		if (print)
 			for (i = 0; i < vertex.length; ++i)
 				vertex[i].print();
 	}
 }
