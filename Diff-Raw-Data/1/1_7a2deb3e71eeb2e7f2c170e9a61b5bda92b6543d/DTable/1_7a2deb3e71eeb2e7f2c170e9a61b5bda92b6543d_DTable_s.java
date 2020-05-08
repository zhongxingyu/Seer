 package gipfj;
 
 /**
  * A depth two transposition table with depth-based replacement scheme - new
  * nodes are always accepted, and nodes that were searched deeper replace
  * short-depth nodes. (this idea comes from <i>Replacement Schemes for
  * Transposition Tables</i>)
  * 
  * @author msto
  * 
  */
 public class DTable {
     private final int size;
     private final int shift_cut;
     private int count_null;
     private int count_first;
     private int count_second;
     private int count_csecond;
     private int count_cneither;
     private int count_cfirst;
     private int count_cnull;
 
     private DTable(int sexp) {
         // 48 bytes/elem; 4 bytes at reference; "approximation" of overhead.
         long max_size = (1 << sexp) * 100;
         long omax = max_size;
         int osexp = sexp;
         Runtime foo = Runtime.getRuntime();
         foo.gc();
         long safe_size = foo.maxMemory() - foo.totalMemory() + foo.freeMemory()
                 - (1 << 26); // safety margin
         boolean c = false;
         while (safe_size < max_size && sexp >= 5) {
             c = true;
             sexp--;
             max_size = (1 << sexp) * 100;
         }
         if (c) {
             System.out
                     .format("&& DTable init warning: when full, table would have used %d bytes.\n",
                             omax);
             System.out.format(
                     "&& Estimated free bytes: %d; Overflow (bytes): %d\n",
                     safe_size, omax - safe_size);
             System.out.format("&& Size exponent reduced from %d to %d\n",
                     osexp, sexp);
             System.out.format("&& Free memory remaining: %d bytes\n", safe_size
                     - max_size);
         }
 
         size = 1 << sexp;
         shift_cut = 32 - sexp;
         store = new Entry[size];
 
         count_null = 0;
         count_first = 0;
         count_second = 0;
 
         count_cnull = 0;
         count_cfirst = 0;
         count_csecond = 0;
         count_cneither = 0;
     }
 
     private Entry[] store;
 
     private void add(Compression.CGS in, int depth, int rank) {
         // what about in.hashCode() >>> shift_cut;
         int index = in.hashCode() >>> shift_cut;
         // if (index < 0) {
         // index = -2 * index - 1;
         // } else {
         // index = 2 * index;
         // }
         Entry ff = store[index];
         Entry n = new Entry(in, depth, rank);
         if (ff == null) {
             store[index] = n;
         } else {
             // first entry always has greater depth;
             // second entry is pushed out
             if (n.depth > ff.depth) {
                 store[index] = n;
                 n.second = ff;
             } else {
                 ff.second = n;
             }
 
         }
     }
 
     /**
      * This does not do a memcheck. You might as well make a new instance.
      * 
      */
     private void load() {
         if (store == null) {
             store = new Entry[size];
         }
     }
 
     private void empty() {
         count_null = 0;
         count_first = 0;
         count_second = 0;
         count_cnull = 0;
         count_cfirst = 0;
         count_csecond = 0;
         count_cneither = 0;
         store = null;
         System.gc();
     }
 
     private void analyze() {
         int empty = 0;
         int single = 0;
         int full = 0;
 
         for (Entry e : store) {
             if (e == null) {
                 empty++;
             } else if (e.second == null) {
                 single++;
             } else {
                 full++;
             }
         }
         System.out.println("== Depth 2 table analysis results:");
         System.out.format("== Search: fail - %d; first %d; second %d\n",
                 count_null, count_first, count_second);
         System.out.format(
                 "== Change: empty - %d; first %d; second %d; neither %d\n",
                 count_cnull, count_cfirst, count_csecond, count_cneither);
         System.out.format("== State: empty %d; single entry %d; full %d\n",
                 empty, single, full);
     }
 
     private Long geta(Compression.CGS in) {
         int index = in.hashCode() >>> shift_cut;
         Entry f = store[index];
         if (f == null) {
             count_null++;
             return null;
         }
 
         if (f.equals(in)) {
             count_first++;
             return (long) f.rank;
         } else if (f.second != null && f.second.equals(in)) {
             count_second++;
             return (long) f.second.rank;
         }
         return null;
     }
 
     private Long getd(Compression.CGS in, byte depth) {
         int index = in.hashCode() >>> shift_cut;
         Entry f = store[index];
         if (f == null) {
             count_null++;
             return null;
         }
 
         if (f.equals(in) && f.depth == depth) {
             count_first++;
             return (long) f.rank;
         } else if (f.second != null && f.second.equals(in)
                 && f.second.depth == depth) {
             count_second++;
             return (long) f.second.rank;
         }
         return null;
     }
 
     private void change(Compression.CGS in, byte depth, int rank) {
         int index = in.hashCode() >>> shift_cut;
         Entry ff = store[index];
         if (ff == null) {
             count_cnull++;
             add(in, depth, rank);
             return;
         }
 
         if (ff.equals(in)) {
             count_cfirst++;
             ff.rank = rank;
             ff.depth = depth;
         } else if (ff.second != null && ff.second.equals(in)) {
             count_csecond++;
             ff.second.rank = rank;
             ff.second.depth = depth;
         } else {
             count_cneither++;
             add(in, depth, rank);
         }
     }
 
     public static DTable dmake(long size) {
         return new DTable((int) size);
     }
 
     public static void dadd(DTable d, Compression.CGS gs, long depth, long rank) {
         d.add(gs, (int) depth, (int) rank);
     }
 
     /**
      * Depth need not match.
      * 
      * @param d
      * @param gs
      * @return
      */
     public static Long dgeta(DTable d, Compression.CGS gs) {
         return d.geta(gs);
     }
 
     /**
      * Depth _must_ match.
      * 
      * @param d
      * @param gs
      * @param depth
      * @return
      */
     public static Long dgetd(DTable d, Compression.CGS gs, long depth) {
         return d.getd(gs, (byte) depth);
     }
 
     /**
      * Depth need not match;
      * 
      * @param d
      * @param gs
      * @param depth
      * @param rank
      */
     public static void dchange(DTable d, Compression.CGS gs, long depth,
             long rank) {
         d.change(gs, (byte) depth, (int) rank);
     }
 
     public static void dclear(DTable d) {
         d.empty();
         d.load();
     }
 
     public static void dempty(DTable d) {
         d.empty();
     }
 
     public static void dload(DTable d) {
         d.load();
     }
 
     public static void danalyze(DTable d) {
         d.analyze();
     }
 
     private static class Entry {
         public int hc;
         public int rank;
         public byte depth;
         // this will be null for the second depth entry - we could remove it and
         // make a 2ndEntry class
         public Entry second;
 
         public byte d0;
         public byte d1;
         public byte d2;
         public byte d3;
         public byte d4;
         public byte d5;
         public byte d6;
         public byte d7;
         public byte d8;
         public byte d9;
         public byte d10;
         public byte d11;
         public byte d12;
         public byte d13;
 
         public Entry(Compression.CGS g, int depth, int rank) {
             hc = g.hc;
             byte[] q = g.d;
             this.depth = (byte) depth;
             this.rank = rank; // force short??
             // o yay! 27 entries
 
             // regexes!
             d0 = q[0];
             d1 = q[1];
             d2 = q[2];
             d3 = q[3];
             d4 = q[4];
             d5 = q[5];
             d6 = q[6];
             d7 = q[7];
             d8 = q[8];
             d9 = q[9];
             d10 = q[10];
             d11 = q[11];
             d12 = q[12];
             d13 = q[13];
         }
 
         public boolean equals(Compression.CGS in) {
             if (in.hc != hc)
                 return false;
 
             byte[] q = in.d;
             return (d4 == q[4]) && (d5 == q[5]) && (d6 == q[6]) && (d7 == q[7])
                     && (d8 == q[8]) && (d9 == q[9]) && (d10 == q[10])
                     && (d11 == q[11]) && (d12 == q[12]) && (d13 == q[13])
                     && (d0 == q[0]) && (d1 == q[1]) && (d2 == q[2])
                     && (d3 == q[3]);
         }
     }
 }
