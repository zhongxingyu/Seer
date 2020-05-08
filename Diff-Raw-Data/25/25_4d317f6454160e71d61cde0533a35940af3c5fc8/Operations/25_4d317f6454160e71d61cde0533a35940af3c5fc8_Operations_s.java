 package ru.stanislavburov.android.PGMCalc;
 
 import java.io.Serializable;
 
 import android.widget.Toast;
 
 public class Operations implements Serializable{
 	private static final long serialVersionUID = 1L;
 	private CalcEngine engine=null;
 	Operations(CalcEngine engine) { this.engine = engine; }
 	
     private static double factorial(double n) {
         if (n <= 0) return 1;
         else { return n * factorial(n-1); }
     }
     
     private static double permutation(double n, double k) {
     	double result=1;
     	for(double i=n; i>=n-k+1; i--) { result*=i; }
     	return result;
     }
     
     public class Exception extends java.lang.Exception {
 		private static final long serialVersionUID = 4217359757479231500L;
 		public Exception() { super(); }
     	public Exception(String cause) { super(cause); }
     }
 
     public abstract class UnaryOperation implements Serializable {
 		private static final long serialVersionUID = 1L;
 		protected boolean hugeRelation(double bigNumber, double smallNumber) {
 			double relation = Math.log10(Math.abs(bigNumber/smallNumber));
 			if(relation>15) return true; // 15 means difference more than 15 orders of magnitude, so smallNumber is zero comparing to bigNumber
 			else return false;
 		}
 		public abstract double proceed(double var) throws Exception;
 		public abstract String toString();
 	}
     public class MemoryUnit extends UnaryOperation {
 		private static final long serialVersionUID = 1L;
 		private String description;
     	private double value=0;
     	public MemoryUnit(String description) { this.description=description; }
 		@Override public double proceed(double var) throws Exception { return value; }
 		@Override public String toString() { return description; }
     	public void setValue(double newValue) throws Exception {
     		if(Double.isNaN(newValue)) throw new Exception("Not a number");
     		if(Double.isInfinite(newValue)) throw new Exception("Infinite");
     		this.value=newValue;
     	}
     	public double getValue() { return value; }
     }
     public class PercentSecondPhase extends UnaryOperation {
 		private static final long serialVersionUID = 1L;
 		double value;
     	public void setValue(double v) { value = v; }
 		@Override public double proceed(double var) { return var*value; }
 		@Override public String toString() { return "percent swcond phase"; }
 	}
 	public abstract class BinaryOperation implements Serializable {
 		private static final int MAX_PRECEDENCE = 1000000;
 		public abstract double proceed(double var1, double var2) throws Exception;
 		public abstract int getPrecedence();
 		public abstract String toString();
 	}
 	
 	public PercentSecondPhase percentSecondPhase = new PercentSecondPhase();
 	public UnaryOperation angleConvert = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) {
 			double result = engine.toRadian(var);
 			engine.changeAngleUnit();
 			return engine.fromRadian(result);
 		}
 		@Override public String toString() { return "DRG->"; }
 	},
 	zero = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return 0.0; }
 		@Override public String toString() { return "zero"; }
 	},
 	percentFirstPhase = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return var/100.; }
 		@Override public String toString() { return "percent first phase"; }
 	},
 	factorial = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) throws Exception {
 			if(var<0) throw new Exception("Argument to factorial must be positive");
 			if(var>200) throw new Exception("Too big argument");
 			long var2 = Math.round(var);
 			if(var-var2!=0) throw new Exception("Argument to factorial must be integer");
 			return factorial(var);
 		}
 		@Override public String toString() { return "x!"; }
 	},
 	xinverse = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return 1.0/var; }
 		@Override public String toString() { return "1/x"; }
 	},
 	rand = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.random(); }
 		@Override public String toString() { return "rand"; }
 	},
 	gamma = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double z) {
 			if(z>200) return Double.POSITIVE_INFINITY;
 			if(z<-1000) return Double.NaN;
 			long shift = Math.round(z) - 1;
 			z = z - shift;
 			double result = Math.exp(-z-5.5) * Math.pow(z+5.5, z+0.5) * Math.sqrt(2*Math.PI) / z;
 			double[] p = {1.000000000190015,
 					76.18009172947146,
 					-86.50532032941677,
 					24.01409824083091,
 					-1.231739572450155,
 					1.208650973866179E-03,
 					-5.395239384953E-06};
 			double tmp = p[0];
 			for(int i=1; i<p.length; i++) { tmp += p[i]/(z+i); }
 			result*=tmp;
 			if(shift>0) for(long i=0; i<shift; i++) result*=z+i;
 			else { shift*=-1; for(long i=shift; i>0; i--) result/=z-i; }
 			return result;
 		}
 		@Override public String toString() { return "gamma(x)"; }
 	},
 	modulus = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.abs(var); }
 		@Override public String toString() { return "abs(x)"; }
 	},
 	roundx = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.rint(var); }
 		@Override public String toString() { return "round(x)"; }
 	},
 	xcuberoot = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.cbrt(var); }
 		@Override public String toString() { return "cube root"; }
 	},
 	xcube = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return var*var*var; }
 		@Override public String toString() { return "x*x*x"; }
 	},
 	xsquareroot = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.sqrt(var); }
 		@Override public String toString() { return "sqrt"; }
 	},
 	xsquare = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return var*var; }
 		@Override public String toString() { return "x*x"; }
 	},
 	alog = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.pow(10.0, var); }
 		@Override public String toString() { return "10^x"; }
 	},
 	log = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.log10(var); }
 		@Override public String toString() { return "log"; }
 	},
 	exp = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.pow(Math.E, var); }
 		@Override public String toString() { return "e^x"; }
 	},
 	ln = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.log(var); }
 		@Override public String toString() { return "ln"; }
 	},
 	e = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.E; }
 		@Override public String toString() { return "E"; }
 	},
 	c = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return 299792458; }
 		@Override public String toString() { return "c"; }
 	},
 	h = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return 6.62606957E-34; }
 		@Override public String toString() { return "h"; }
 	},
 	k = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return 1.3806488E-23; }
 		@Override public String toString() { return "k"; }
 	},
 	avogadroConstant = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return 6.02214129E23; }
 		@Override public String toString() { return "Na"; }
 	},
 	elementaryCharge = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return 1.602176565E-19; }
 		@Override public String toString() { return "q(el)"; }
 	},
 	vacuumPermettivity = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return 8.85418781762E-12; }
 		@Override public String toString() { return "e(0)"; }
 	},
 	pi = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.PI; }
 		@Override public String toString() { return "PI"; }
 	},
 	atan = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return engine.fromRadian(Math.atan(var)); }
 		@Override public String toString() { return "atan"; }
 	},
 	acos = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return engine.fromRadian(Math.acos(var)); }
 		@Override public String toString() { return "acos"; }
 	},
 	asin = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return engine.fromRadian(Math.asin(var)); }
 		@Override public String toString() { return "asin"; }
 	},
 	tan = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) {
 			double result = Math.tan(engine.toRadian(var));
 			if(hugeRelation(var, result)) result = 0;
 			return result;
 		}
 		@Override public String toString() { return "tan"; }
 	},
 	cos = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) {
 			double result = Math.cos(engine.toRadian(var)); 
 			if(hugeRelation(var, result)) result = 0;
 			return result;
 		}
 		@Override public String toString() { return "cos"; }
 	},
 	sin = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) {
 			double result = Math.sin(engine.toRadian(var)); 
 			if(hugeRelation(var, result)) result = 0;
 			return result;
 		}
 		@Override public String toString() { return "sin"; }
 	},
 	artanh = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return 0.5*Math.log( (1+var) / (1-var) ); }
 		@Override public String toString() { return "artanh"; }
 	},
 	arcosh = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.log(var + Math.sqrt(var*var-1)); }
 		@Override public String toString() { return "arcosh"; }
 	},
 	arsinh = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.log(var + Math.sqrt(var*var+1)); }
 		@Override public String toString() { return "arsinh"; }
 	},
 	tanh = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.tanh(var); }
 		@Override public String toString() { return "tanh"; }
 	},
 	cosh = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.cosh(var); }
 		@Override public String toString() { return "cosh"; }
 	},
 	sinh = new UnaryOperation() {
 		private static final long serialVersionUID = 1L;
 		@Override public double proceed(double var) { return Math.sinh(var); }
 		@Override public String toString() { return "sinh"; }
 	};
 	
 	public BinaryOperation doNothing = new BinaryOperation() {
		@Override public double proceed(double var1, double var2) { return var1; }  // or var2???
 		@Override public int getPrecedence() { return BinaryOperation.MAX_PRECEDENCE; } 
 		@Override public String toString() { return "doNothing"; }
 	},
 	intDivide = new BinaryOperation() {
 		@Override public double proceed(double n, double k) throws Exception {
 			if(Math.round(n)-n!=0 || Math.round(k)-k!=0) throw new Exception("Arguments to integer division must be integer");
 			long result = Math.round(n) / Math.round(k);
 			return result;
 		}
 		@Override public int getPrecedence() { return 2; }
 		@Override public String toString() { return "integer divide"; }
 	},
 	intRemainder = new BinaryOperation() {
 		@Override public double proceed(double n, double k) throws Exception {
 			if(Math.round(n)-n!=0 || Math.round(k)-k!=0) throw new Exception("Arguments to integer division must be integer");
 			long result = Math.round(n) % Math.round(k);
 			return result;
 		}
 		@Override public int getPrecedence() { return 2; }
 		@Override public String toString() { return "integer remainder"; }
 	},
 	combination = new BinaryOperation() {
 		@Override public double proceed(double n, double k) throws Exception {
 			if(k>n) throw new Exception("First argument must be greater than or equal to second.");
 			if (k<0 || n<0) throw new Exception("Arguments to combination must be positive"); 
 			if(Math.round(n)-n!=0 || Math.round(k)-k!=0) throw new Exception("Arguments to combination must be integer");
 			return permutation(n, k)/factorial(k);
 		}
 		@Override public int getPrecedence() { return 4; }
 		@Override public String toString() { return "nPk"; }
 	},
 	permutation = new BinaryOperation() {
 		@Override public double proceed(double n, double k) throws Exception {
 			if(k>n) throw new Exception("First argument must be greater than or equal to second.");
 			if (k<0 || n<0) throw new Exception("Arguments to permutation must be positive"); 
 			if(Math.round(n)-n!=0 || Math.round(k)-k!=0) throw new Exception("Arguments to permutation must be integer");
 			return permutation(n, k);
 		}
 		@Override public int getPrecedence() { return 4; }
 		@Override public String toString() { return "nPk"; }
 	},
 	xlogy = new BinaryOperation() {
 		@Override public double proceed(double var1, double var2) { return Math.log(var1)/Math.log(var2); }
 		@Override public int getPrecedence() { return 3; }
 		@Override public String toString() { return "x log y"; }
 	},
 	xrooty = new BinaryOperation() {
 		@Override public double proceed(double var1, double var2) { return Math.pow(var1, 1/var2); }
 		@Override public int getPrecedence() { return 3; }
 		@Override public String toString() { return "root"; }
 	},
 	xpowy = new BinaryOperation() {
 		@Override public double proceed(double var1, double var2) { return Math.pow(var1, var2); }
 		@Override public int getPrecedence() { return 3; }
 		@Override public String toString() { return "power"; }
 	},
 	multiply = new BinaryOperation() {
 		@Override public double proceed(double var1, double var2) { return var1 * var2; }
 		@Override public int getPrecedence() { return 2; }
 		@Override public String toString() { return "multiply"; }
 	},
 	divide = new BinaryOperation() {
 		@Override public double proceed(double var1, double var2) { return var1 / var2; }
 		@Override public int getPrecedence() { return 2; }
 		@Override public String toString() { return "divide"; }
 	},
 	plus = new BinaryOperation() {
 		@Override public double proceed(double var1, double var2) { return var1 + var2; }
 		@Override public int getPrecedence() { return 1; }
 		@Override public String toString() { return "plus"; }
 	},
 	minus = new BinaryOperation() {
 		@Override public double proceed(double var1, double var2) { return var1 - var2; }
 		@Override public int getPrecedence() { return 1; }
 		@Override public String toString() { return "minus"; }
 	};
 }
