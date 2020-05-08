 import java.util.Date;
 import java.util.Random;
 import java.util.PriorityQueue;
 
 public class FuzzTester {
     int m_run_length;
     String m_id;
     Random m_rand;
     BinomialHeap[] m_heaps;
     PriorityQueue<Integer>[] m_queues;
     int m_index;
     static final int ARRAY_SIZE = 1000000;
     static final int LARGEST = 100000;
 
     public FuzzTester(int run_length, Random rand) {
         m_run_length = run_length;
         m_rand = rand;
         m_heaps = new BinomialHeap[ARRAY_SIZE];
         m_queues = new PriorityQueue[ARRAY_SIZE];
     }
 
     public void setId(String id) { m_id = id; }
 
     public int last_size() {
         return m_heaps[m_index - 1].size();
     }
 
     public void insert() {
         int next = m_rand.nextInt(LARGEST);
        System.err.println("insert " + next);
         m_heaps[m_index - 1].insert(next);
         m_queues[m_index - 1].add(next);
     }
 
     public void delete() {
         int last = m_queues[m_index - 1].poll();
        System.err.println("delete " + last);
         assert(last == m_heaps[m_index - 1].findMin());
         m_heaps[m_index - 1].deleteMin();
     }
 
     public void push() {
        System.err.println("push");
         m_queues[m_index] = new PriorityQueue<Integer>();
         m_heaps[m_index] = new BinomialHeap();
         m_index++;
     }
 
     public void pop() {
        System.err.println("pop");
         m_index--;
         m_queues[m_index - 1].addAll(m_queues[m_index]);
         m_heaps[m_index - 1].meld(m_heaps[m_index]);
     }
 
     public void fuzz() throws Exception {
         int i=0;
         try {
             for(;i<m_run_length;i++) {
                 if(m_index == 0 || m_rand.nextInt(4) == 0) {
                     if(m_index < 2 || m_rand.nextBoolean())
                         push();
                     else
                         pop();
                 } else {
                     if(last_size() == 0 || m_rand.nextInt(4) != 0)
                         insert();
                     else if(last_size() < 5 || m_rand.nextBoolean())
                         delete();
                     else for(int j=0;j < 5;j++) delete();
                 }
             }
 
            while(m_index > 0) pop();
             while(last_size() > 0) delete();
         } catch(Exception e) {
             System.out.println(m_id + ": failed at command #" + i);
             throw e;
         }
     }
 
     public static void main(String[] args) throws Exception {
         if(args.length == 0) {
            System.err.println("usage: FuzzTester RUNLENGTH NRUNS SEED");
         }
 
         int run_length = Integer.parseInt(args[0]);
         int count_runs = Integer.parseInt(args[1]);
         long seed = Long.parseLong(args[2]);
         
         FuzzTester fuzzer = new FuzzTester(run_length, new Random(seed));
 
         System.err.println("Fuzzing with seed " + seed + ": doing "			   + count_runs + " runs each of length " + run_length);
 
         for(int i=0; i<count_runs; i++) {
             String id = "Seed " + seed + " Run " + i;
             fuzzer.setId(id);
 
             Date before = new Date();
             fuzzer.fuzz();
             Date after = new Date();
 
             System.out.println(id + ": succeeded. Took " +
 			       (after.getTime() - before.getTime()) + "ms");
         }
     }
 }
