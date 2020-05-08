 package finders;
 
 import java.text.DecimalFormat;
 import java.util.Collections;
 import java.util.Vector;
 
 import misc.Differentiate;
 import misc.PolyFunction;
 
 // y = ax^3 + bx^2 + cx + d
 // mgliche nullstellen 1-3
 public class NewtonFinder implements FinderInterface {
 	
 	// precision/depth of recursive newton algorithm
 	private int newtonDepth = 1000;
 	
 	private double a/*, b, c, d*/;
 	
 	public Vector<Double> find(PolyFunction f) throws InvalidFuncException {
 		Vector<Double> results = new Vector<Double>();
 		
 		a = f.getKoeff(3);
 		/*b = f.getKoeff(2);
 		c = f.getKoeff(1);
 		d = f.getKoeff(0);*/
 		
 		// ableiten damit sie quadratisch ist
 		// nullstellen x koordinaten der abgeleiteten funktion
 		// entsprechen den extrema von f
 		PolyFunction fa = Differentiate.differentiate(f);
 		Vector<Double> extrema = null;
 		
 		if (f.getMaxGrade() == 3) {
 			// ableiten damit sie quadratisch ist
 			// nullstellen x koordinaten der abgeleiteten funktion
 			// entsprechen den extrema von f
 			extrema = new QuadraticFinder().find(fa);
 		} else if (f.getMaxGrade() > 3) {
 			// recursion
 			extrema = new NewtonFinder().find(fa);
 			System.out.println(fa);
 			System.out.println(extrema);
 		}
 		
		// ableitung hat keine oder nur eine nullstelle
 		// an irgendeinem ort suchen
 		// f hat nur eine nullstelle
 		if (extrema.size() == 0 || extrema.size() == 1) {
 			results.add(round(newton(f, 1.0, newtonDepth)));
 			return results;
 		}
 		
 		// extrema aufsteigend sortieren
 		Collections.sort(extrema);
 		
 		// positive 3ten grades
 		// erstes extremum ber null
 		// ODER
 		// negative 3ten grades
 		// zweites extremum unter null
 		//
 		// => links suchen
 		Double firstElement = extrema.firstElement();
		if (a > 0.0 && f.calculate(firstElement) > 0.0 ||
			a < 0.0 && f.calculate(firstElement) < 0.0) {
 			results.add(round(newton(f, firstElement - 1, newtonDepth)));
 		}
 		
 		// von erster bis vorletzer nullstelle
 		// immer mit nchster vergleichen
 		for (int i = 0; i < extrema.size() - 1; i++) {
 			Double x1 = extrema.get(i);
 			Double x2 = extrema.get(i + 1);
 			
 			// vorzeichen wechsel
 			// nullstelle suchen
 			if (signChange(f.calculate(x1), f.calculate(x2))) {
 				results.add(round(newton(f, (x1 + x2) / 2.0, newtonDepth)));
 			}
 		}
 		
 		// positive 3ten grades
 		// erstes extremum unter null
 		// ODER
 		// negative 3ten grades
 		// zweites extremum ber null
 		//
 		// => rechts suchen
 		Double lastElement = extrema.lastElement();
		if (a > 0.0 && f.calculate(lastElement) < 0.0 ||
			a < 0.0 && f.calculate(lastElement) > 0.0) {
 			results.add(round(newton(f, lastElement + 1, newtonDepth)));
 		}
 		
 		return results;
 	}
 	
 	// recursive newton
 	// prevValue = startValue
 	// Xn+1 = Xn - (f(Xn) / f'(Xn))
 	public Double newton(PolyFunction f, Double prevValue, Integer depth) {
 		PolyFunction fa = Differentiate.differentiate(f);
 		double x = prevValue - (f.calculate(prevValue) / fa.calculate(prevValue));
 		if (depth > 0) {
 			return newton(f, x, depth - 1);
 		} else {
 			return x;
 		}
 	}
 	
 	// auf 3 stellen runden
 	public Double round(Double value) {
		DecimalFormat df = new DecimalFormat("#.###");
		return Double.valueOf(df.format(value));
 	}
 	
 	// helper function to check if sign changed
 	static public boolean signChange(Double y1, Double y2) {
 		return (y1 < 0 && y2 > 0 ||
 				y1 > 0 && y2 < 0);
 	}
 }
