 package com.winvector.anneal;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 
 
 public final class RunAnneal<T extends Comparable<T>> {
 	private final Population<T> shared;
 	
 	public RunAnneal(final Population<T> shared) {
 		this.shared = shared;
 	}
 	
 	private class AnnealJob1 implements Runnable {
 		public final int id;
 		public final int psize;
 		public final Random rand;
 			
 		public AnnealJob1(final int id, final int psize, final Random rand) {
 			this.id = id;
 			this.psize = psize;
 			this.rand = rand;
 		}
 		
 		private void swap(final Population<T> p) {
 			synchronized(shared) {
 				//System.out.println("Runnable " + id + " mixing into main population " + new Date());
 				for(int oi=0;oi<psize;++oi) {
 					if(rand.nextBoolean()) {
 						final int ti = rand.nextInt(shared.population.size());
 						final T or = p.population.get(oi);
 						final double os = p.pscore[oi];
 						p.population.set(oi,shared.population.get(ti));
 						p.pscore[oi] = shared.pscore[ti];
 						shared.population.set(ti,or);
 						shared.pscore[ti] = os;
 					}
 				}
 			}
 		}
 
 		protected double score(final T mi) {
 			return shared.pv.scoreExample(mi);
 		}
 		
 		protected int nInserts(final double score) {
 			final int nInserts = Math.max((int)Math.floor(10.0*score),1);
 			return nInserts;
 		}
 		
 		@Override
 		public void run() {
 			final Population<T> p;
 			synchronized(shared) {
 				System.out.println("anneal Runnable " + id + " start " + new Date());
 				p = new Population<T>(rand,shared,psize);
 			}
 			T donor = p.population.get(rand.nextInt(psize));
 			for(int step=0;step<10*psize;++step) {
 				final Set<T> mutations = shared.pv.mutations(donor,rand);
 				final T d2 = p.population.get(rand.nextInt(psize));
 				final Set<T> children = shared.pv.breed(donor,d2,rand);
 				mutations.addAll(children);
				mutations.add(donor);
 				boolean record = false;
				T bestC = null;
				double bestCscore = 0.0;				
 				final ArrayList<T> goodC = new ArrayList<T>(mutations.size());
 				for(final T mi: mutations) {
 					final double scorem = score(mi);
 					if(scorem>0.0) {
						if((null==bestC)||(scorem>bestCscore)) {
							bestC = mi;
							bestCscore = scorem;
						}
 						goodC.add(mi);
 						if(p.show(mi,scorem)) {
 							record = true;
 							donor = mi;
 							synchronized(shared) {
 								if(shared.show(mi,scorem)) {
 									System.out.println("new record: " + p.bestScore + "\t" + p.best + "\t" + new Date());
 								}
 							}
 						}
 						final int nInserts = nInserts(scorem);
 						for(int insi=0;insi<nInserts;++insi) {
 							final int vi = rand.nextInt(psize);
 							// 	number of insertions*worseodds < 1 to ensure progress
 							if((scorem>p.pscore[vi])||(rand.nextDouble()>0.9)) {
 								p.population.set(vi,mi);
 								p.pscore[vi] = scorem;
 							}
 						}
 					}
 				}
				if((null!=p.best)&&(p.bestScore>0)) {
					final int vi = rand.nextInt(psize);
					p.population.set(vi,p.best);
					p.pscore[vi] = p.bestScore;
				}
				if(null!=bestC) {
					donor = bestC;
				}
 				if(step%1000==0) {
 					if(goodC.size()>0) {
 						donor = goodC.get(rand.nextInt(goodC.size()));
 					} else {
 						donor = p.population.get(rand.nextInt(psize));
 					}
 				}
 				// mix into shared population 
 				if(record||(step%(psize/2))==0) {
 					swap(p);
 				}
 			}
 			swap(p);
 			synchronized(shared) {
 				System.out.println("anneal Runnable " + id + " finish " + new Date());
 			}
 		}
 	}
 	
 	private AnnealJob1 newJob(final int id, final int psize, final Random rand) {
 		return new AnnealJob1(id,psize,rand);
 	}
 	
 	public static <T extends Comparable<T>> T runAnneal(final AnnealAdapter<T> pv, final Collection<T> starts, final int nparallel) throws InterruptedException {
 		System.out.println("start anneal");
 		final Random rand = new Random(235235);
 		final Population<T> shared = new Population<T>(pv,new Random(rand.nextLong()),500000,new ArrayList<T>(starts));
 		final RunAnneal<T> ra = new RunAnneal<T>(shared);
 		final int njobs = 20;
 		if(nparallel>1) {
 			final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(njobs+1);
 			final ThreadPoolExecutor executor = new ThreadPoolExecutor(nparallel,nparallel,100,TimeUnit.SECONDS,queue);
 			for(int i=0;i<njobs;++i) {
 				executor.execute(ra.newJob(i,100000,new Random(rand.nextLong())));
 			}
 			executor.shutdown();
 			executor.awaitTermination(Long.MAX_VALUE,TimeUnit.SECONDS);
 		} else {
 			for(int i=0;i<njobs;++i) {
 				ra.newJob(i,100000,new Random(rand.nextLong())).run();
 			}
 		}
 		System.out.println("done anneal1");
 		return shared.best;
 	}
 
 }
