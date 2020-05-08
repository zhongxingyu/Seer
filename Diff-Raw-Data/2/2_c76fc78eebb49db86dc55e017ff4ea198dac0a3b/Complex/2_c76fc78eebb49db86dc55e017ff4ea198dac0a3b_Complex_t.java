 package jscl.math.numeric;
 
 import jscl.math.NotDivisibleException;
 import org.jetbrains.annotations.NotNull;
 
 public final class Complex extends Numeric {
 
 	private final double real, imaginary;
 
 	Complex(double real, double imaginary) {
 		this.real = real;
 		this.imaginary = imaginary;
 	}
 
 	public Complex add(Complex complex) {
 		return new Complex(real + complex.real, imaginary + complex.imaginary);
 	}
 
 	@NotNull
 	public Numeric add(@NotNull Numeric that) {
 		if (that instanceof Complex) {
 			return add((Complex) that);
 		} else if (that instanceof Real) {
 			return add(valueOf(that));
 		} else {
 			return that.valueOf(this).add(that);
 		}
 	}
 
 	public Complex subtract(Complex complex) {
 		return new Complex(real - complex.real, imaginary - complex.imaginary);
 	}
 
 	@NotNull
 	public Numeric subtract(@NotNull Numeric that) {
 		if (that instanceof Complex) {
 			return subtract((Complex) that);
 		} else if (that instanceof Real) {
 			return subtract(valueOf(that));
 		} else {
 			return that.valueOf(this).subtract(that);
 		}
 	}
 
 	public Complex multiply(Complex complex) {
 		return new Complex(real * complex.real - imaginary * complex.imaginary, real * complex.imaginary + imaginary * complex.real);
 	}
 
 	@NotNull
 	public Numeric multiply(@NotNull Numeric that) {
 		if (that instanceof Complex) {
 			return multiply((Complex) that);
 		} else if (that instanceof Real) {
 			return multiply(valueOf(that));
 		} else {
 			return that.multiply(this);
 		}
 	}
 
 	public Complex divide(Complex complex) throws ArithmeticException {
 		return multiply((Complex) complex.inverse());
 	}
 
 	@NotNull
 	public Numeric divide(@NotNull Numeric that) throws NotDivisibleException {
 		if (that instanceof Complex) {
 			return divide((Complex) that);
 		} else if (that instanceof Real) {
 			return divide(valueOf(that));
 		} else {
 			return that.valueOf(this).divide(that);
 		}
 	}
 
 	@NotNull
 	public Numeric negate() {
 		return new Complex(-real, -imaginary);
 	}
 
 	@NotNull
 	@Override
 	public Numeric abs() {
 		final Numeric realSquare = new Real(real).pow(2);
 		final Numeric imaginarySquare = new Real(imaginary).pow(2);
 		final Numeric sum = realSquare.add(imaginarySquare);
 		return sum.sqrt();
 	}
 
 	public int signum() {
 		int result;
 
 		if (real > .0) {
 			result = 1;
 		} else if (real < .0) {
 			result = -1;
 		} else {
 			result = Real.signum(imaginary);
 		}
 
 		return result;
 	}
 
 	public double magnitude() {
 		return Math.sqrt(real * real + imaginary * imaginary);
 	}
 
 	public double magnitude2() {
 		return real * real + imaginary * imaginary;
 	}
 
 	public double angle() {
 		return Math.atan2(imaginary, real);
 	}
 
 	@NotNull
 	public Numeric ln() {
 		if (signum() == 0) {
 			return Real.ZERO.ln();
 		} else {
 			return new Complex(Math.log(magnitude()), angle());
 		}
 	}
 
 	@NotNull
 	public Numeric lg() {
 		if (signum() == 0) {
 			return Real.ZERO.lg();
 		} else {
 			return new Complex(Math.log10(magnitude()), angle());
 		}
 	}
 
 	@NotNull
 	public Numeric exp() {
 		return new Complex(Math.cos(defaultToRad(imaginary)), Math.sin(defaultToRad(imaginary))).multiply(Math.exp(real));
 	}
 
 	@NotNull
 	public Numeric inverse() {
 		return ((Complex) conjugate()).divide(magnitude2());
 	}
 
 	Complex multiply(double d) {
 		return new Complex(real * d, imaginary * d);
 	}
 
 	Complex divide(double d) {
 		return new Complex(real / d, imaginary / d);
 	}
 
 	public Numeric conjugate() {
 		return new Complex(real, -imaginary);
 	}
 
 	public double realPart() {
 		return real;
 	}
 
 	public double imaginaryPart() {
 		return imaginary;
 	}
 
 	public int compareTo(Complex that) {
 		if (imaginary < that.imaginary) {
 			return -1;
 		} else if (imaginary > that.imaginary) {
 			return 1;
 		} else if (imaginary == that.imaginary) {
 			if (real < that.real) {
 				return -1;
 			} else if (real > that.real) {
 				return 1;
 			} else if (real == that.real) {
 				return 0;
 			} else throw new ArithmeticException();
 		} else throw new ArithmeticException();
 	}
 
 	public int compareTo(Numeric that) {
 		if (that instanceof Complex) {
 			return compareTo((Complex) that);
 		} else if (that instanceof Real) {
 			return compareTo(valueOf(that));
 		} else {
 			return that.valueOf(this).compareTo(that);
 		}
 	}
 
 	public Complex copyOf(@NotNull Complex complex) {
 		return new Complex(complex.real, complex.imaginary);
 	}
 
 	@NotNull
 	public Numeric valueOf(@NotNull Numeric numeric) {
 		if (numeric instanceof Complex) {
 			return copyOf((Complex) numeric);
 		} else if (numeric instanceof Real) {
 			Real d = (Real) numeric;
 			return d.toComplex();
 		} else throw new ArithmeticException();
 	}
 
 	@NotNull
 	public static final Complex I = new Complex(0, 1);
 
 	@NotNull
 	public static Complex valueOf(double real, double imaginary) {
 		if (real == 0d && imaginary == 1d) {
 			return I;
 		} else {
 			return new Complex(real, imaginary);
 		}
 	}
 
 	public String toString() {
 		final StringBuilder result = new StringBuilder();
 
 		if (imaginary == 0.) {
 			result.append(toString(real));
 		} else {
 			if (real != 0.) {
 				result.append(toString(real));
 				if (imaginary > 0.) {
 					result.append("+");
 				}
 			}
 
 			if (imaginary != 1.) {
 				if (imaginary == -1.) {
 					result.append("-");
 				} else {
                     if (imaginary < 0.) {
                         final String imagStr = toString(imaginary);
                        // due to rounding we can forget sign (-0.00000000001 can be round to 0 => plus sign would not be added above and no sign will be before i)
                         if (imagStr.startsWith("-")) {
                             result.append(imagStr);
                         } else {
                             result.append("-").append(imagStr);
                         }
                     } else {
                         result.append(toString(imaginary));
                     }
                     result.append("*");
 				}
 			}
 			result.append("i");
 		}
 
 		return result.toString();
 	}
 }
