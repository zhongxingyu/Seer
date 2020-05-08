 /**
  * 
  */
 package methods.evolution;
 
 import java.util.Iterator;
 import java.util.Random;
 
 import libs.Input;
 import libs.Point;
 import libs.Range;
 import libs.Result;
 import libs.functions.Function;
 import methods.Method;
 
 /**
  * @author iskoulis ekontogi
  *
  */
 public class GenericEvolution extends Method{
 	
 	public static Result run(Input input){
 		EvInput in = (EvInput) input;
 		int steps = 0;
 		Population pop = initialize(in.func, in.range, in.pSize, in.dim);
 		Citizen best = pop.best();
 //		System.out.println(pop);
 		steps += in.pSize;
 		while((in.steps > steps) && (best.getValue() > in.prec)) {
 //			System.out.println("Selecting");
 			pop = in.s.select(pop);
 //			System.out.println(pop);
 //			System.out.println("Recombining");
 			pop = in.r.recombine(pop);
 //			System.out.println(pop);
 //			System.out.println("Mutating");
 			in.m.mutate(pop);
 //			System.out.println(pop);
 //			System.out.println("Evaluating");
 			pop = evaluate(pop, in);
 //			System.out.println(pop);
 			steps += in.pSize;
 			if (pop.best().getValue() < best.value) {
 				best = pop.best();
 //				System.out.println(best);
 			}
 //			System.out.println("---------------------");
 		}
 		
 		Result res = new Result(best.p, best.getValue(), steps);
 		return res;
 	}
 	
 	/**
 	 * Initialize Population
 	 *
 	 */
	public static Population initialize(Function f, Range r, int size, int dim) {
 		Random rand = new Random(System.currentTimeMillis());
 		Population pop = new Population();
 		for (int i = 0; i < size; i++) {
 			double[] p = new double[dim];
 			for (int j = 0; j < dim; j++) {
 				p[j] = rand.nextDouble()*(r.max[j]-r.min[j]) + r.min[j];
 			}
 			Point pt = new Point(p);
 			double value = f.f(pt);
 			pop.add(pt, value);
 		}
 		return pop;
 	}
 	
 	/**
 	 * Evaluates Population
 	 *
 	 */
 	private static Population evaluate(Population p, Input in) {
 		Population pop = new Population();
 		Iterator<Citizen> it = p.getItems().iterator();
 		for (int i = 0; i < p.size(); i++) {
 			Citizen c = it.next();
 			double v = in.func.f(c.p);
 			pop.add(c.p, v);
 		}
 		return pop;
 	}
 }
