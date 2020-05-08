 import libs.Input;
 import libs.Range;
 import libs.Result;
 import libs.functions.Ackley;
 import libs.functions.Function;
 import methods.evolution.differential.DEInput;
 import methods.evolution.differential.DERecombination;
 import methods.evolution.differential.DifferentialEvolution;
 import methods.evolution.differential.mutation.Best1Bin;
 import methods.evolution.differential.mutation.Best2Bin;
 import methods.evolution.differential.mutation.DEMutation;
 import methods.evolution.differential.mutation.Rand1Bin;
 import methods.evolution.differential.mutation.Rand2Bin;
 import methods.evolution.differential.mutation.Rand2BinStar;
 
 
 
 public class Main {
 
 	/**
 	 * @author iskoulis
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		
 //		Function f;
 //		Input i;
 //		Result res;
 		
 //		f = new Parabola(1);
 //		double min[] = {-4.0};
 //		double max[] = {30.0};
 //		double dir[] = {1};
 //		Range range = new Range(min, max);
 //		i = new GSInput(f, range, 100, Math.pow(10, -6), new Point(dir));
 //		
 //		res = GoldenSection.run(i);
 //		System.out.println(res);
 		
 //---------------------------------------------------
 		
 //		f = new Rosenbrock();
 //		double start[] = {-3.62, -4.23};
 //		double stop[] = {6.37, 5.77};
 //		Range range = new Range(start, stop);
 //		
 //		for (int j = 0; j < 3; j++) {
 //			i = new Input(f, range, (long) Math.pow(10, 3+j));
 //			res = GridSearch.run(i);
 //			System.out.println(res);
 //			res = RandomJump.run(i);
 //			System.out.println(res + "\n");
 //		}
 		
 //---------------------------------------------------
 		
 //		f = new Rosenbrock();
 //		double min[] = {-3.62, -4.23};
 //		double max[] = {6.37, 5.77};
 //		Range range = new Range(min, max);
 //
 //		for (int j = 0; j < 3; j++) {
 //			i = new RWInput(f, range, Math.pow(10, 3+j), 0.2, Math.pow(10,-4), 100);
 //			res = RandomWalk.run(i);
 //			System.out.println(res);
 //		}
 		
 //---------------------------------------------------
 		
 //		f = new Rosenbrock();
 //		double min[] = {-3.62, -4.23};
 //		double max[] = {6.37, 5.77};
 //		Range range = new Range(min, max);
 //		for (int j = 0; j < 3; j++) {
 //			i = new RWEInput(f, range, Math.pow(10, 3+j), 20);
 //			res = RandomWalkExploit.run(i);
 //			System.out.println(res);
 //		}
 		
 //---------------------------------------------------
 		
 //		f = new Rastrigin(10, 2);
 //		double fStar = 0;
 //		double min[] = {-5.12, -5.12};
 //		double max[] = {5.12, 5.12};
 //		Range range = new Range(min, max);
 //		AcceptanceFunction af = new Metropolis();
 //		CoolingSchedule cs = new Bohachevsky(f, fStar, 0.5, 1);
 //		i = new SAInput(f, range, Math.pow(10, 5), af, cs, 1, fStar, Math.pow(10, -2));
 //		
 //		System.out.println();
 //		for (int j = 0; j < 30; j++) {
 //			res = SimAnnealing.run(i);
 //			System.out.println(res);
 //		}
 //		
 //		af = new Barker();
 //		System.out.println();
 //		
 //		for (int j = 0; j < 30; j++) {
 //			res = SimAnnealing.run(i);
 //			System.out.println(res);
 //		}
 //		
 //		i = new SAInput(f, range, Math.pow(10, 5), af, cs, 0, fStar, Math.pow(10, -4));
 //		af = new Metropolis();
 //		System.out.println();
 //		
 //		for (int j = 0; j < 30; j++) {
 //			res = SimAnnealing.run(i);
 //			System.out.println(res);
 //		}
 //		
 //		af = new Barker();
 //		System.out.println();
 //		
 //		for (int j = 0; j < 30; j++) {
 //			res = SimAnnealing.run(i);
 //			System.out.println(res);
 //		}
 		
 //---------------------------------------------------
 		
 //		f = new Rastrigin(10, 2);
 //		double min[] = {-5.12, -5.12};
 //		double max[] = {5.12, 5.12};
 //		Range range = new Range(min, max);
 //		i = new TSInput(f, range, Math.pow(10, 5), 0, Math.pow(10, -3), 10, 10, 100);
 //		
 //		for (int j = 0; j < 30; j++) {
 //			res = TabuSearch.run(i);
 //			System.out.println(res);
 //		}
 		
 //---------------------------------------------------
 //---------------------------------------------------
 
 //		double minimizer[] = {0,0};
 //		
 //		Function f = new Ackley(20, 0.2, (2*Math.PI), 0);
 //		double min[] = {-30, -30};
 //		double max[] = {30, 30};
 		
 //		Function f = new Rastrigin(10, 2);
 //		double min[] = {-5.12, -5.12};
 //		double max[] = {5.12, 5.12};
 		
 
 //		Function f = new Rosenbrock();
 //		double min[] = {-3.62, -4.23};
 //		double max[] = {6.37, 5.77};
 		
 //		Range range = new Range(min, max);
 		
 //		Selection ls_rw = new LinearScaling(new RouletteWheel(), 5, (float)0.25);
 //		Selection ls_tm = new LinearScaling(new Tournament(10), 10, (float)0.25);
 //		Selection lr_rw = new LinearRanking(new RouletteWheel(), 2);
 //		Selection lr_tm = new LinearRanking(new Tournament(10), 2);
 //		Selection ps_rw = new PowerScaling(new RouletteWheel(), (float)-2);
 //		Selection ps_tm = new PowerScaling(new Tournament(10), (float)-2);
 		
 //		Recombination dc = new Discrete();
 //		Recombination rv = new RealValued(range, (float)0);
 		
 		
 
 //		Input in = new EvInput(f, range, Math.pow(10, 6), Math.pow(10, -2), ps_tm, rv, m, 10);
 		
 		
 //		for (int i = 0; i < 30; i++) {
 //			Selection sel = new PowerScaling(new RouletteWheel(), (float)-2);
 //			Selection sel = new PowerScaling(new Tournament(10), (float)-2);
 //			Recombination rc = new Discrete();
 //			Recombination rc = new RealValued(range, (float)0.25);
 //			Mutation m = new RealGaussian(range, (float)0.33, (float)(1-Math.pow(10,-4)));
 //			Input in = new EvInput(f, range, Math.pow(10, 6), Math.pow(10, -2), sel, rc, m, 20);
 //			Result res = GenericEvolution.run(in);
 //			System.out.println(res);
 //			res = GenericEvolution.run(in);
 //			System.out.println(i + "  " + res);
 //			res = GenericEvolution.run(in);
 //			System.out.println(i + "  " + res);
 //		}
<<<<<<< HEAD
 		
 //---------------------------------------------------
 //---------------------------------------------------
 		
 //		Function f = new Ackley(20, 0.2, (2*Math.PI), 0);
 //		double min[] = {-30, -30};
 //		double max[] = {30, 30};
 //		
 //		Range range = new Range(min, max);
 //		
 //		Network net = new RingLatice(1);
 //		
 //		Input in = new PSOInput(f, range, Math.pow(10, 6), Math.pow(10, -2), 20 , (float)0.729, (float)2.05, (float)2.05, net);
 //		Result res = PSO.run(in);
 //		System.out.println(res);
 		
 //---------------------------------------------------
 //---------------------------------------------------
 		Function f = new Ackley(20, 0.2, (2*Math.PI), 0);
 		double min[] = {-30, -30};
 		double max[] = {30, 30};
 		
 		Range range = new Range(min, max);
 		
 		DEMutation m3_b1 = new Best1Bin((float)0.3);
 		DEMutation m5_b1 = new Best1Bin((float)0.5);
 		DEMutation m3_r1 = new Rand1Bin((float)0.3);
 		DEMutation m5_r1 = new Rand1Bin((float)0.5);
 		DEMutation m3_s1 = new Rand2BinStar((float)0.3);
 		DEMutation m5_s1 = new Rand2BinStar((float)0.5);
 		DEMutation m3_b2 = new Best2Bin((float)0.3);
 		DEMutation m5_b2 = new Best2Bin((float)0.5);
 		DEMutation m3_r2 = new Rand2Bin((float)0.3);
 		DEMutation m5_r2 = new Rand2Bin((float)0.5);
 		
 		DERecombination r3 = new DERecombination((float)0.3);
 		DERecombination r5 = new DERecombination((float)0.5);
 		
 		
 		Input in = new DEInput(f, range, Math.pow(10, 6), Math.pow(10, -2), 20, m5_r2, r5);
 		Result res = DifferentialEvolution.run(in);
 		System.out.println(res);
 		
		
=======
>>>>>>> f0faf9fa58e796857dfa178150c60093ef3687a7
 	}
 }
