 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * Represents a fuzzy set on which operations can be performed.  
  * <p>
  * Sets are represented by a belonging function over real numbers.
  * This function is computed using linear interpolation in between known points where its value is known.
  * Those points are implemented via the {@link element} class. Out of bounds values are specified as well.
  * <p>
  * Using out of bounds values is a design choice. An other possibiliy could have been using the most extreme point a the value out of bounds.
  * This however cannot be so easily mixed with complements, unless ad hoc points very close to the extremes are added.
  * <p>
  * The user can specify a name for the set. We tried to make names play well with the various operations available.
  * <p>   
  * A step parameter is available when discretization becomes necessary (for the probabilistic t-norms and when applying a function to a set). 
  * Without some discretization, those operations would lose the nice curvy look they give sets !
  *
  */
 public class set{
 	public String name; 
 	public ArrayList<element> elements;
 	public double min, max, step=0.1;
 	public double vLeft = 0; // values outside of bounds
 	public double vRight = 0; 
 
 
     /**
     * Class constructor with almost a full specification of the set. Note extremum elements will be added at bounds if not present. 
     * @param  elements  The elements describing the belonging function
     * @param  min  the lower bound for the belonging function
     * @param  max  the upper bound for the belonging function
     * @param  name the chosen name for the set 
  	*/
 	public set(ArrayList<element> elements, double min, double max, String name){ // Doesn't support values != 0 outside of bounds
 	    this.elements = elements;
 	    Collections.sort(elements);
 	    // Bounds
 	    element first = elements.get(0);
 	    element last  = elements.get(this.length()-1);
 	    if(first.x > min){
 	    	elements.add(0, new element(min, vLeft));
 	    } else if(first.x < min){
 		    	System.out.println("La borne inférieure est supérieure à un des élements");
 	    }
 	    if(last.x < max){
 	    	elements.add(new element(max, vRight));
 	    } else if(last.x > max){
 		    	System.out.println("La borne supérieure est inférieure à un des élements");
 	    }
 	    this.min = min;
 	    this.vLeft = vLeft;
 	    this.vRight = vRight;
 	    this.max = max;
 	    this.name = name;
 	}
  	/**
     * Class constructor enabling hard copy.
     */
 	public set(set another){ // pour faire une vrai copie !
     	this.name = another.name;
     	ArrayList<element> elementsCopy = new ArrayList<element>();
     	int i=0;
     	for(element e : another.elements){
     		elementsCopy.add(i, new element(e.x, e.y));
     		i=i+1;
     	}
     	this.elements = elementsCopy;
     	this.min = another.min;
     	this.max = another.max;
     	this.step = another.step;
   	}
 
  	/**
     * Method for accessing the number of elements describing the set.
     * @return  The number of elements in the set
     */
 	public int length(){
 	    return elements.size();
 	};
  	/**
     * Method used to pretty print the set.
     */
 	public String toString(){
 		String out = name+"\n";
 		out = out + this.length()+" points\n";
 		out = out + "inf:" + this.min + "  (" + this.vLeft + ")\n";
 		out = out + "sup:" + this.max + "  (" + this.vRight + ")\n";
 		for (element e : elements){
 			out = out + e.toString()+"\n"; // elements.toString() suffit 
 		}
 		return out;
 	}
 
  	/**
     * Method used to compute the belonging function at a given value.
     * Linear interpolation is used inside of bounds.
     * Beware, this function uses a scan of the set for the interpolation.
     * Dichotomy could be better for 1-point computations, but we chose something better for typicall use cases
     * @param  x  The x-axis value
     * @return  Belonging value at x
     */
 	public double valueAt(double x){
 	    return valueAt(x, 0);
 	}
  	/**
     * Method used to compute the belonging function at a given value.
     * To use linear interpolation, we need first to place x between two elements of the set.
     * Scanning the whole set is a possible choice as the set is sorted. However the leads to O(n) search complexity.
    * A dichotomy search is possible, yielding some O(ln(n)) but it is not the best choice for typicall use cases.
     * Indeed, calls to valueAt are repeated with increasing x values whenever we perfom operations on sets.
     * We make it possible to hint the element at which the scan begins. This gives in effect a O(1) complexity for most calls.
     * @param  x  The x-axis value
     * @param  startIndex  When computing the belonging function.
     * @return  Belonging value at x
     */
 	public double valueAt(double x, int startIndex){
 		double x1,x2,y1,y2;
 		if(x > max) {return vRight;};
 		if(x < min) {return vLeft;};	
 		
 		int i = Math.max(0,startIndex);
 		do{ // scan elements
 			x1 = elements.get(i).x ;
 			x2 = elements.get(i+1).x;
 			if(x2>=x){
 				if(x1<=x){ // when x is between two elements
 					y1 = elements.get(i).y;
 					y2 = elements.get(i+1).y;
 	    			return (y2-y1)/(x2-x1) * (x-x1) + y1; // interpolate
 				} else{
 					System.out.println("Error : x2<x1 ??"); // throw an exception ?!
 				}
 			}
 			i = i + 1; 
 		} while(x>x2); 
 	    return 0; // throw an exception ?!
 	}
  	/**
     * Blindly computes values of the belonging function at multiple x values.
     * @param  xx  x-axis values
     * @return  Belonging values at x
     */
 	public ArrayList<Double> valueAt(ArrayList<Double> xx){
 		ArrayList<Double> values = new ArrayList<Double>(); 
 		for(double x : xx){
 			values.add(valueAt(x));
 		}
 		return values;
 	}
 
  	/**
     * Returns the complement of a set.
     * @param  A  a set
     * @return  A's complement set
     */
 	public static set complementaire(set A){
 		set Ac = new set(A);
 		Ac.name = "c("+Ac.name+")";
 		for(element e : Ac.elements){
 			e.y = 1 - e.y;
 		}
 		Ac.vLeft = 1- Ac.vLeft;
 		Ac.vRight = 1 - Ac.vRight;
 	    return Ac;
 	}
 
  	/**
     * Applies a t-conorm or a t-norm object to two set. 
     * <p>
     * The sets being sorted, we blend them together find all different x values by iterating through both sets at once.
     * This ensures O(n) complexity for intersection or union operations.
     * @param  A  first set
     * @param  B  second set
     * @param  n  A chosen tnorm object
     * @return  A and B blended by the t-norm
     */
 	private static ArrayList<element> applyConorm(set A, set B, tnorm n){ // returns elements in common for A and B
 		ArrayList<element> elements = new ArrayList<element>();	
 		element a; //current element of A
 		element b;
 		element e = new element(0, 0); // new element to be added to n(A,B)
 		int i=0; // current A index 
 		int j=0; 
 		// YES, it would have been better to use iterators over both sets. Much better.
 		int a_after_b; // a.x after b.x ? 0-1
 		int Adone = 0; // have we finished iterating through A ?
 		int Bdone = 0; 
 		while(Adone==0 || Bdone==0){ // until both sets are iterated through
 			a = A.elements.get(i - Adone);
 			b = B.elements.get(j - Bdone);
 			a_after_b = a.compareTo(b);
 			if(Adone==1){ // if A done, then finish visiting B
 				e.x = b.x;
 				e.y = n.compute(A.valueAt(e.x, i-1-Adone),B.valueAt(e.x, j-1-Bdone));
 				elements.add(new element(e.x, e.y));
 				j = j + 1;
 			} else if(Bdone==1){ // if B done, then finish visiting A
 				e.x = a.x;
 				e.y = n.compute(A.valueAt(e.x, i-1-Adone),B.valueAt(e.x, j-1-Bdone));
 				elements.add(new element(e.x, e.y));
 				i = i + 1;
 			} else{
 					if(a_after_b>0){ // then b has some catching up to do
 					e.x = b.x;
 					e.y = n.compute(A.valueAt(e.x, i-1-Adone),B.valueAt(e.x, j-1-Bdone));
 					elements.add(new element(e.x, e.y));
 					j = j + 1;
 				} else if (a_after_b<0) {
 					e.x = a.x;
 					e.y = n.compute(A.valueAt(e.x, i-1-Adone),B.valueAt(e.x, j-1-Bdone));
 					elements.add(new element(e.x, e.y));
 					i = i + 1;			 
 				} else {
 					e.x = a.x;
 					e.y = n.compute(A.valueAt(e.x, i-1-Adone),B.valueAt(e.x, j-1-Bdone));
 					i = i + 1;
 					j = j + 1;
 					elements.add(new element(e.x, e.y));
 					System.out.print(e);
 				}
 			}
 			Adone = i>A.length()-1? 1:0;
 			Bdone = j>B.length()-1? 1:0;
 		}
 		System.out.print(elements+"\n");
 		return elements;
 	}
  	/**
     * Computes the union of two set. 
     * The sets being sorted, we blend them together find all different x values by iterating through both sets at once.
     * This ensures O(n) complexity for intersection or union operations.
     * @param  A  first set
     * @param  B  second set
     * @param  method  method choosen proba, lukas or zadeh
     * @return  The union of A and B according to the chosen t-conorm 
     */	
 	public static set union(set A, set B, String method){
 		String new_name = '(' + A.name + ") U (" + B.name + ')';
 		double new_min = Math.min(A.min, B.min);
 		double new_max = Math.max(A.max, B.max);
 		double new_step = Math.min(A.step, B.step);
 		tnorm n;
 		switch(method){ // chose the 
 			case "proba": n = new tconorm_proba(); A = set.discretize(A, A.step); B = set.discretize(B, B.step); break;
 			case "zadeh": n = new tconorm_zadeh(); break;
 			case "lukas": n = new tconorm_lukas(); break;
 			default: n = new tconorm_zadeh(); System.out.println("Defaulting to zadeh t-conorm");break;
 		}
 		ArrayList<element> elements = applyConorm(A, B, n);
 		set AuB = new set(elements, new_min, new_max, new_name);
 		AuB.vLeft = n.compute(A.vLeft,B.vLeft);
 		AuB.vRight = n.compute(A.vRight,B.vRight);
 	    return AuB;
 	}
  	/**
     * Computes the intersection of two set. 
     * The sets being sorted, we blend them together find all different x values by iterating through both sets at once.
     * This ensures O(n) complexity for intersection or union operations.
     * @param  A  first set
     * @param  B  second set
     * @param  method  method choosen proba, lukas or zadeh
     * @return  The intersection of A and B according to the chosen t-norm 
     */	
 	public static set intersection(set A, set B, String method){
 		String new_name = '(' + A.name + ") n (" + B.name + ')';
 		double new_min = Math.min(A.min, B.min);
 		double new_max = Math.max(A.max, B.max);
 		double new_step = Math.min(A.step, B.step);
 		tnorm n;
 		switch(method){
 			case "proba": n = new tconorm_proba(); A = set.discretize(A, A.step); B = set.discretize(B, B.step); break;
 			case "zadeh": n = new tnorm_zadeh(); break;
 			case "lukas": n = new tnorm_lukas(); break;
 			default: n = new tnorm_zadeh(); System.out.println("Defaulting to zadeh t-norm");break;
 		}
 		ArrayList<element> elements = applyConorm(A, B, n);
 		set AnB = new set(elements, new_min, new_max, new_name);
 		AnB.vLeft = n.compute(A.vLeft,B.vLeft);
 		AnB.vRight = n.compute(A.vRight,B.vRight);
 	    return AnB;
 	}
  	/**
     * Slices a set from min to max in close-by elements in O(n). Note : elements will change and go from A.min to A.max by A.step.
     * @param  A  set to be discretized
     * @param  step  The discretization step. Would could have used A.step...
     * @return    A more refined set. 
     */	
 	public static set discretize(set A, double step){
 		element e;
 		set dA = new set(A);
 		dA.elements.clear();
 		int k = Math.min(1, A.length()-1); // next A element
 		for(double i=A.min; i<=A.max; i=i+step){
 				while(A.elements.get(k).x>i) {k=k+1;} // so as not to scan A each call to valueAt...
 				e = new element(i, A.valueAt(i, Math.max(0,k-1)));
 				dA.elements.add(e);	
 		}
 		return dA;
 	}
  	/**
     * Apply a function to a set.
     * <p>
     * First the bounds of the image set are computed by running computing the bounds of f(A). 
     * Then a new set is created in between those bounds, and belonging values are computed according to fuzzy set usual choices at each step in between.
    	* How well the new bounds behave with all kind of function remains to be seen
    	* <p>
    	* This function is not the most efficient possible...
     * With some regularity on f, it could be possible to compute A values for f-1(A) elements without scanning A each time.
     * At least a dichotomy search could prove useful ! [TODO some other time]
     * @param  A set to be discretized
     * @param  f  A function implementing the IMapping interface
     * @param  step Step parameter
     */	
 	public static set apply(set A, IMapping f, double step){ 
 		double v;
 		element el;
 		double fmin = f.compute(A.elements.get(0).x);
 		double fmax = f.compute(A.elements.get(0).x);
 		for(element e : set.discretize(A,step).elements){ // first get bounds
 			v = f.compute(e.x);
 			if(e.y>0){
 				if(v>fmax){fmax = v;};
 				if(v<fmin){fmin = v;};				
 			}
 		}
 		ArrayList<element> elements = new ArrayList<element>(); // then create a new set between those bounds 
 		ArrayList<Double> antecedents;
 		ArrayList<Double> values;
 		double padding = (fmax-fmin)*0.1;
 		fmax = fmax + padding; // it's better not to be too rigourous with bounds so as not to miss stuff
 		fmin = fmin - padding;
 		for(double i=fmin; i<=fmax; i=i+step){ 
 			antecedents = f.reverse(i); 
 			if(!antecedents.isEmpty()){  // and compute some values ! 
 				values = A.valueAt(antecedents); // A dichotomie scan could prove useful here ! Or some better...
 				v = Collections.max(values);
 				el = new element(i, v);
 				elements.add(el);
 			}
 		};
 		String newName = f.toString()+"("+A.name+")";
 		set fA = new set(elements, fmin, fmax, newName);
 	    return fA;
 	}
 }
