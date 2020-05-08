 package particles;
 
import java.util.ArrayList;
 import java.util.HashSet;
 
 import extra166y.Ops;
 import extra166y.Ops.Generator;
 import extra166y.Ops.Procedure;
 import extra166y.ParallelArray;
 
 public class Particle {
 	public double x, y, m;
 	Particle origin, origin1;
 
 	public void moveTo(double x, double y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	public void vacuouslyNoRace() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle b) {
 			}
 		});
 	}
 
 	public void noRaceOnParameter() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle b) {
 				b.x = 10;
 			}
 		});
 	}
 
 	public void noRaceOnParameterInitializedBefore() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				return new Particle();
 			}
 		});
 
 		particles.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle b) {
 				b.x = 10;
 			}
 		});
 	}
 
 	public void verySimpleRace() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				shared.x = 10;
 				return new Particle();
 			}
 		});
 	}
 
 	public void raceOnParameterInitializedBefore() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle particle = new Particle();
 				particle.origin = shared;
 				return particle;
 			}
 		});
 
 		particles.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle b) {
 				b.origin.x = 10;
 			}
 		});
 	}
 
 	public void noRaceOnANonSharedField() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle particle = new Particle();
 				particle.origin = shared;
 				particle.origin1 = new Particle();
 				particle.origin1.x = 10;
 
 				return particle;
 			}
 		});
 	}
 
 	public void oneCFANeededForNoRaces() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 		shared.moveTo(3, 4);
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle particle = new Particle();
 				particle.moveTo(2, 3);
 				return particle;
 			}
 		});
 	}
 
 	public void twoCFANeededForNoRaces() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 		compute(shared);
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle particle = new Particle();
 				compute(particle);
 				return particle;
 			}
 		});
 	}
 
 	public void recursive() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 		computeRec(shared);
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle particle = new Particle();
 				computeRec(particle);
 				return particle;
 			}
 		});
 	}
 
 	private void compute(Particle particle) {
 		particle.moveTo(2, 3);
 	}
 
 	private void computeRec(Particle particle) {
 		int x = 10 / 7;
 		if (x < 2)
 			computeRec(particle);
 		else
 			compute(particle);
 	}
 
 	public void disambiguateFalseRace() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 		shared.moveTo(3, 4);
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle particle = new Particle();
 				particle.moveTo(2, 3);
 				shared.moveTo(5, 7);
 				return particle;
 			}
 		});
 	}
 
 	public void ignoreFalseRacesInSeqOp() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 
 		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle particle = new Particle();
 				shared.x = 10;
 				particle.origin = shared;
 				particle.origin.y = 10;
 				return particle;
 			}
 		});
 
 		particles.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle b) {
 				b.x = 10;
 			}
 		});
 	}
 
 	public void raceBecauseOfOutsideInterference() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 		shared.moveTo(3, 4);
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				shared.origin = new Particle();
 				shared.origin.x = 10;
 				return new Particle();
 			}
 		});
 	}
 
 	public void raceOnSharedObjectCarriedByArray() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 		shared.moveTo(3, 4);
 
 		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle p = new Particle();
 				shared.origin = new Particle();
 				p.origin = shared;
 				return p;
 			}
 		});
 
 		particles.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle p) {
 				p.origin.origin.moveTo(2, 3);
 			}
 		});
 	}
 
 	public void raceBecauseOfDirectArrayLoad() {
 		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 		particles.getArray()[0] = shared;
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle p = (Particle) particles.getArray()[0];
 				p.x = 10;
 				return new Particle();
 			}
 		});
 	}
 
 	public void raceOnSharedReturnValue() {
 		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				shared.x = 10;
 				return shared;
 			}
 		});
 	}
 
 	public void raceOnDifferntArrayIteration() {
 		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				return new Particle();
 			}
 		});
 
 		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				return particles.getArray()[0];
 			}
 		});
 
 		particles.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle p) {
 				p.x = 10;
 			}
 		});
 	}
 
 	public void noRaceIfFlowSensitive() {
 		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				return new Particle();
 			}
 		});
 
 		particles.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle p) {
 				p.x = 10;
 			}
 		});
 
 		final Particle s = new Particle();
 
 		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				return s;
 			}
 		});
 
 	}
 
 	public void raceOnDifferntArrayIterationOneLoop() {
 		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				return new Particle();
 			}
 		});
 
 		final Particle s = new Particle();
 
 		particles.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle p) {
 				p.origin.x = 10;
 				s.origin = new Particle();
 				p.origin = s.origin;
 			}
 		});
 	}
 
 	public void verySimpleRaceWithIndex() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle shared = new Particle();
 
 		particles.replaceWithMappedIndex(new Ops.IntToObject<Particle>() {
 			@Override
 			public Particle op(int i) {
 				shared.x = 10;
 				return new Particle();
 			}
 		});
 	}
 
 	final static Particle staticShared = new Particle();
 	protected static double df = 1.2;
 
 	public void verySimpleRaceToStaticObject() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithMappedIndex(new Ops.IntToObject<Particle>() {
 			@Override
 			public Particle op(int i) {
 				staticShared.x = 10;
 				return new Particle();
 			}
 		});
 	}
 
 	public void raceOnSharedFromStatic() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithMappedIndex(new Ops.IntToObject<Particle>() {
 			@Override
 			public Particle op(int i) {
 				Particle x = staticShared;
 				x.y = 11;
 				return new Particle();
 			}
 		});
 	}
 
 	public void raceInLibrary() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final HashSet<Particle> sharedSet = new HashSet<Particle>();
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle particle = new Particle();
 				sharedSet.add(particle);
 				sharedSet.size();
 				return particle;
 			}
 		});
 	}
 
 	public void noRaceOnStringConcatenation() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle p = new Particle();
 				@SuppressWarnings("unused")
 				String bla = "tralala" + p;
 				bla += "";
 				return p;
 			}
 		});
 	}
 
 	// should only report one race on "shared.origin = p"
 	public void noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 		final Particle shared = new Particle();
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle p = new Particle();
 				shared.origin = p;
 				p.x = 10;
 				return new Particle();
 			}
 		});
 	}
 
 	public void noRaceWhenPrintln() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				System.out.println("bla");
 				return new Particle();
 			}
 		});
 	}
 
 	public void raceOnArray() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		final Particle[] shared = new Particle[1];
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				shared[0] = new Particle();
 				return new Particle();
 			}
 		});
 	}
 
 	public void raceByWriteOnSomethingInstantiatedInTheMainIteration() {
 		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle particle = new Particle();
 				particle.origin = particles.getArray()[3];
 				return particle;
 			}
 		});
 
 		particles.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle p) {
 				p.x = 10;
 				@SuppressWarnings("unused")
 				double y = p.origin.x;
 				y++;
 			}
 		});
 	}
 
 	public void multipleArrays() {
 		final ParallelArray<Particle> particles1 = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 		final ParallelArray<Particle> particles2 = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles1.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				return new Particle();
 			}
 		});
 
 		particles2.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle particle = new Particle();
 				particle.origin = particles1.getArray()[0];
 				return particle;
 			}
 		});
 
 		particles2.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle p) {
 				p.x = 10;
 				p.origin.y = 11;
 			}
 		});
 	}
 
 	public void multipleArrays1CFANeeded() {
 		final ParallelArray<Particle> particles1 = createArray();
 		final ParallelArray<Particle> particles2 = createArray();
 
 		particles1.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				return new Particle();
 			}
 		});
 
 		particles2.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				Particle particle = new Particle();
 				particle.origin = particles1.getArray()[0];
 				return particle;
 			}
 		});
 
 		particles2.apply(new Ops.Procedure<Particle>() {
 			@Override
 			public void op(Particle p) {
 				p.x = 10;
 				p.origin.y = 11;
 			}
 		});
 	}
 
 	private ParallelArray<Particle> createArray() {
 		return ParallelArray.createUsingHandoff(new Particle[10], ParallelArray.defaultExecutor());
 	}
 
 	protected double forceX;
 	protected double velX;
 	protected double forceY;
 	protected double velY;
 	static double deltaAcc = 1;
 	static double deltaTime = 1;
 
 	Particle c = new Particle();
 	private int noSteps;
 
 	public void example() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithGeneratedValue(new Generator<Particle>() {
 			public Particle op() {
 				Particle p = new Particle();
 				readParticle(p);
 				return p;
 			}
 		});
 
 		for (int i = 0; i < noSteps; i++) {
 
 			// ... compute force ...
 
 			particles.apply(new Procedure<Particle>() {
 				public void op(Particle p) {
 					p.velX += p.forceX * p.m * deltaAcc;
 					p.velY += p.forceY * p.m * deltaAcc;
 					p.x += p.velX * deltaTime;
 					p.y += p.velY * deltaTime;
 				}
 			});
 
 			particles.apply(new Procedure<Particle>() {
 				public void op(Particle p) {
 					Particle oldC = c;
 					c = new Particle();
 					c.m = oldC.m + p.m;
 					c.x = (oldC.x * oldC.m + p.x * p.m) / c.m;
 					c.y = (oldC.y * oldC.m + p.y * p.m) / c.m;
 				}
 			});
 		}
 	}
 
 	private void readParticle(Particle p) {
 	}
 	
 	public void staticMethod() {
 		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
 			@Override
 			public Particle op() {
 				thisisstatic();
 				return new Particle();
 			}
 		});
 	}
 
 	private static void thisisstatic() {
 		staticShared.forceX ++;
 	}
 	
 	static int staticX;
 	
 	public void verySimpleRaceOnStaticField() {
 		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
 				ParallelArray.defaultExecutor());
 
 		particles.replaceWithMappedIndex(new Ops.IntToObject<Particle>() {
 			@Override
 			public Particle op(int i) {
 				staticX = 100;
 				return new Particle();
 			}
 		});
 	}
	
	public void raceOnArrayList() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());
		
		final ArrayList<Integer> someList = new ArrayList<Integer>();

		particles.replaceWithMappedIndex(new Ops.IntToObject<Particle>() {
			@Override
			public Particle op(int i) {
				someList.add(i);
				return new Particle();
			}
		});
	}
 }
