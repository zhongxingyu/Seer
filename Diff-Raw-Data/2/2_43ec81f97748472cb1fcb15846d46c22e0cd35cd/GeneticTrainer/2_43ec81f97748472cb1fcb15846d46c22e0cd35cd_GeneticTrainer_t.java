 package util.genetic;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import util.genetic.mutatorV1.MutatorV1;
 
 import eval.expEvalV3.DefaultEvalWeights;
 import eval.expEvalV3.EvalParameters;
 
 public final class GeneticTrainer {
 	public static class Entity{
 		private static long idIndex = 0;
 		EvalParameters p;
 		final AtomicInteger wins = new AtomicInteger();
 		final AtomicInteger losses = new AtomicInteger();
 		final AtomicInteger draws = new AtomicInteger();
 		private final long id;
 		Entity(){
 			id = idIndex++;
 		}
 		public double score(){
 			final double score = wins.get()+draws.get()/2.;
 			return score/totalGames();
 		}
 		public int totalGames(){
 			return wins.get() + losses.get() + draws.get();
 		}
 		public String toString(){
 			return "(w,l,d)=("+wins.get()+","+losses.get()+","+draws.get()+"), id="+id;
 		}
 	}
 
 	private final static Comparator<Entity> sortBestFirst = new Comparator<GeneticTrainer.Entity>() {
 		public int compare(Entity e1, Entity e2) {
 			if(e1 == null){
 				return 1;
 			} else if(e2 == null){
 				return -1;
 			}
 			
 			if(e1.score() > e2.score()){
 				return -1;
 			} else if(e1.score() < e2.score()){
 				return 1;
 			} else{
 				return e1.totalGames() > e2.totalGames()? -1: 1;
 			}
 		}
 	};
 	
 	private final static ByteBuffer b = ByteBuffer.allocate(1<<15);
 	
 	public static void main(String[] args) throws Exception{
 
 		final int tests = 1; //games to play per simulation step
 		final int threads = 4;
 		final long time = 30;
 		final int hashSize = 18;
 		final int popSize = 30;
 		final int cullSize = max((int)(popSize*.05+.5), 1); //number of entries to cull
 		final int minGames = 5; //min games before entry can be culled
 		final Mutator m = new MutatorV1();
 		final int mutations = 6;
 		final double gameCutoffPercent = .2; //only play games against the top X percent of solutions
 		final double reproduceCutoffPercent = .4; //only clone and mutate entites in top X percent of solutions
 		
 		final File file = new File("genetic-results/genetic-results-v13");
 		if(1==2&& file.exists()){
 			System.out.println("log file already exists, exiting");
 			System.exit(0);
 		}
 		@SuppressWarnings("resource")
 		final FileChannel f = new FileOutputStream(file).getChannel();
 		
 		final Entity[] population = new Entity[popSize];
 		final GameQueue q = new GameQueue(threads, time, hashSize);
 		
 		for(int a = 0; a < population.length; a++){
 			population[a] = new Entity();
 			population[a].p = DefaultEvalWeights.defaultEval();
 			if(Math.random() < .95) m.mutate(population[a].p, mutations);
 		}
 		
 		for(int i = 0; ; i++){
 			simulate(population, q, tests, gameCutoffPercent);
 			List<Integer> culled = cull(population, cullSize, minGames);
 			generate(culled, population, m, mutations, reproduceCutoffPercent);
 
 			System.out.println("completed iteration "+i);
 			
 			//record data
 			Entity e;
 			final Queue<Entity> tempq = new PriorityQueue<Entity>(population.length, sortBestFirst);
 			for(int a = 0; a < population.length; a++) tempq.add(population[a]);
 			while(tempq.size() > 0){
 				if((e = tempq.poll()).totalGames() >= minGames){
 					System.out.println("recording best, id="+e.id);
 					System.out.println(e.p);
 					b.clear();
 					b.putInt(i); //put iteration count
 					e.p.write(b);
 					b.limit(b.position());
 					b.rewind();
 					f.write(b);
 					break;
 				}
 			}
 		}
 	}
 	
 	public static int max(final int i1, final int i2){
 		return i1 > i2? i1: i2;
 	}
 	
 	/** runs the simulation, accumulating a score for each entity*/
 	public static void simulate(final Entity[] population, final GameQueue q, final int tests, final double gameCutoffPercent){
 		final List<Entity> sorted = new ArrayList<Entity>();
 		for(int a = 0; a < population.length; a++) sorted.add(population[a]);
 		Collections.sort(sorted, sortBestFirst);
 		
 		for(int a = 0; a < population.length; a++){
 			
 			for(int w = 0; w < tests; w++){
 				int index;
 				//while((index = (int)(Math.random()*population.length*gameCutoffPercent)) == a);
 				while(sorted.get(index = (int)(Math.random()*population.length*gameCutoffPercent)) == population[a]);
 				
 				//assert population[a] != null && population[index] != null;
 				
 				final GameQueue.Game g = new GameQueue.Game(population[a], sorted.get(index));
 				q.submit(g);
 			}
 		}
 		
 		//wait for queued games to finish
 		final int total = tests*population.length;
 		int prevCompleted = -1;
 		int mod = max((int)(total*.1), 1);
 		while(q.getOutstandingJobs() > 0){
 			try{
 				Thread.sleep(500);
 			} catch(InterruptedException e){}
 			if(total-q.getOutstandingJobs() != prevCompleted){
 				prevCompleted = total-q.getOutstandingJobs();
 				if((prevCompleted % mod) == 0) System.out.println("completed "+prevCompleted+" / "+total);
 			}
 		}
 		System.out.println("games complete!");
 	}
 	
 	/** culls bad solutions from population, returns list of culled indeces*/
 	public static List<Integer> cull(final Entity[] population, int cullSize, final int minGames){
 		final Comparator<Entity> c = new Comparator<GeneticTrainer.Entity>() {
 			public int compare(Entity e1, Entity e2) {
 				if(e1.score() < e2.score()){
 					return -1;
 				} else if(e1.score() > e2.score()){
 					return 1;
 				} else{
 					return e1.totalGames() < e2.totalGames()? -1: 1;
 				}
 			}
 		};
 		final Queue<Entity> q = new PriorityQueue<Entity>(population.length, c);
 		for(int a = 0; a < population.length; a++) q.add(population[a]);
 		
 		final boolean print = true;
 		
 		List<Integer> l = new ArrayList<Integer>();
 		while(q.size() > 0 && l.size() < cullSize){
 			//count as cull even if lowest scoring not eligible for cull
 			//(prevents good solutions from being culled if no eligible bad solutions)
 			cullSize--;
 			Entity e = q.poll();
 			if(print) System.out.print(e);
 			if(e.totalGames() >= minGames){
 				if(print) System.out.println(" -- culled");
 				for(int w = 0; w < population.length; w++){
 					if(population[w] == e){
 						population[w] = null;
 						l.add(w);
 					}
 				}
 			} else{
 				if(print) System.out.println();
 			}
 		}
 		
 		while(q.size() > 0){
 			if(print) System.out.println(q.poll());
 		}
 		
 		return l;
 	}
 	
 	/** generates new solutions from population , replacing culled entries*/
 	public static void generate(final List<Integer> culled, final Entity[] population, final Mutator m,
 			final int mutations, final double reproduceCutoffPercent){
 		final List<Entity> sorted = new ArrayList<Entity>();
 		for(int a = 0; a < population.length; a++) sorted.add(population[a]);
 		Collections.sort(sorted, sortBestFirst);
 		
 		for(int index: culled){
 			int r; //index of entity to clone and mutate
 			while(sorted.get(r = (int)(Math.random()*population.length*reproduceCutoffPercent)) == null);
 			
 			b.clear();
			sorted.get(r).p.write(b);
 			b.rewind();
 			final EvalParameters p = new EvalParameters();
 			p.read(b);
 			m.mutate(p, mutations); 
 			Entity temp = new Entity();
 			temp.p = p;
 			
 			population[index] = temp;
 		}
 	}
 }
