 package br.ufrj.jfirn.intelligent.evaluation;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.math3.analysis.UnivariateFunction;
 import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
 import org.apache.commons.math3.distribution.NormalDistribution;
 import org.apache.commons.math3.util.FastMath;
 
 import br.ufrj.jfirn.common.Point;
 
 /**
  * Bivariate Gaussian Distribution utility functions.
  * 
  * @author Ciro Sobral 
  * @author <a href="mailto:ramiro.p.magalhaes@gmail.com">Ramiro Pereira de Magalhães</a>
  *
  */
 public class BGD {
 	private static final NormalDistribution normal = new NormalDistribution(0, 1);
 	private static final double A[] = {0.3253030, 0.4211071, 0.1334425, 0.006374323};
 	private static final double B[] = {0.1337764, 0.6243247, 1.3425378, 2.2626645};
 
 	private static final double squareHeight = FastMath.scalb(1, -4);
 
 	/**
 	 * As described by Bernt Arne Ødegaard in Financial Numerical Recipes in C++. 
 	 * 
	 * Returns P(X < a, Y < b, c) where X, Y are gaussian random variables N(0, 1)
	 * of the bivariate normal distribution with correlation between X and Y c in [-1, 1].
 	 */
 	public static double cdf(double a, double b, double c) {
 		if (a == Double.NaN || b == Double.NaN || c == Double.NaN) {
 			throw new IllegalArgumentException("Arguments must be a number.");
 		}
 
 		a = handleInfinity(a);
 		b = handleInfinity(b);
 		c = handleInfinity(c);
 
 		if((a <= 0) && (b <= 0) && (c <= 0)) {
 			final double aprime = a/FastMath.sqrt(2d * (1d - c*c));
 			final double bprime = b/FastMath.sqrt(2d * (1d - c*c));
 			double sum = 0;
 			for(int i = 0 ; i < A.length ; i++) {
 				for (int j = 0; j < A.length; j++) {
 					sum += A[i] * A[j] * f(B[i], B[j], aprime, bprime, c);
 				}
 			}
 
 			sum *= FastMath.sqrt(1d - c * c) / FastMath.PI;
 
 			return sum;
 		}
 
 		                     //a or b may be too big and their multiplication may result in NaN.
 		if(c * a * b <= 0) { //c is smaller (between [-1, 1]) and will help not result NaNs. So we multiply c first.
 			if ((a <= 0 ) && (b >= 0) && (c >= 0)) {
 				return normal.cumulativeProbability(a) - cdf(a, -b, -c);  
 			} else if ((a >= 0 ) && (b <= 0) && (c >= 0)) {
 				return normal.cumulativeProbability(b) - cdf(-a, b, -c);  
 			} else if ((a >= 0 ) && (b >= 0) && (c <= 0)) {
 				return normal.cumulativeProbability(a) + normal.cumulativeProbability(b) - 1 + cdf(-a, -b, c);  
 			}
 		} else if(c * a * b >= 0) {
 			final double denum = FastMath.sqrt(a * a - 2d * c * a * b + b * b);
 			final double rho1 = ((c * a - b) * FastMath.signum(a)) / denum;
 			final double rho2 = ((c * b - a) * FastMath.signum(b)) / denum;
 			final double delta = (1d - FastMath.signum(a) * FastMath.signum(b)) / 4d;
 			return cdf(a, 0, rho1) + cdf(b, 0, rho2) - delta;
 		}
 
 		throw new RuntimeException("Should never get here.");
 	}
 
 	//TODO write documentation
 	public static double cdfOfRectangle(double higherX, double higherY, double length, double height, double c) {
 		return BGD.cdf(higherX, higherY, c) - 
 			BGD.cdf(higherX, higherY - height, c) -
 			BGD.cdf(higherX - length, higherY, c) +
 			BGD.cdf(higherX - length, higherY - height, c);
 	}
 
 	//TODO write documentation
 	public static double cdfOfRectangle(Point higherPoint, double length, double height, double c) {
 		return cdfOfRectangle(higherPoint.x(), higherPoint.y(), length, height, c);
 	}
 
 	/**
 	 * Calculates an aproximate value for the BGD inside a certain quadrilateral area delimited
 	 * by points a, b, c and d.
 	 */
 	public static double cdfOfConvexQuadrilaterals(Point a, Point b, Point c, Point d, double correlation) {
 		//We limit this algorithm to values in range [-3, 3] so it runs faster.
 		final Quadrilateral quadrilateral = new Quadrilateral(
 			applyLimitsToPoint(a),
 			applyLimitsToPoint(b),
 			applyLimitsToPoint(c),
 			applyLimitsToPoint(d)
 		);
 
 		double total = 0; //will return this
 
 		double currentY = quadrilateral.getUpperY();
 		while (currentY - squareHeight/2d > quadrilateral.getLowerY()) {
 			final double[] x = quadrilateral.getX(currentY - squareHeight/2d);
 
 			total += BGD.cdfOfRectangle(x[1], currentY, x[1] - x[0], squareHeight, correlation);
 
 			currentY -= squareHeight;
 		}
 
 		return total;
 	}
 
 
 
 	//== This class private parts. Be gentle.
 
 
 
 	/**
 	 * As described by Bernt Arne Ødegaard in Financial Numerical Recipes in C++.
 	 */
 	private static double f(double x, double y, double aprime, double bprime, double c) {
 		return FastMath.exp(aprime * (2d * x - aprime) + bprime * (2d * y - bprime) + 2d * c * (x - aprime) * (y - bprime));
 	}
 
 	/**
 	 * Since the algorithm to calculate the CDF of a bivariate gaussian
 	 * distribution fails with infinity, we change infinity to the
 	 * maximum value a Double can be. This allows the mentioned algorithm
 	 * to provide the appropriate answer.
 	 */
 	private static double handleInfinity(double n) {
 		if (Double.isInfinite(n)) {
 			n = FastMath.copySign(Double.MAX_VALUE, n);
 		}
 
 		return n;
 	}
 
 	/**
 	 * If the parameter's x or y values are bigger than a limit,
 	 * create a new {@link Point} under this limit.
 	 */
 	private static Point applyLimitsToPoint(Point point) {
 		//we'll avoid creating new instances of Point
 		final double limit = 3d;
 		boolean somethingChanged = false;
 		double x = 0, y = 0;
 
 		if (FastMath.abs(point.x()) > limit) {
 			x = FastMath.copySign(limit, point.x());
 			somethingChanged = true;
 		}
 
 		if (FastMath.abs(point.y()) > 3) {
 			y = FastMath.copySign(limit, point.y());
 			somethingChanged = true;
 		}
 
 		if (somethingChanged) {
 			return new Point(x, y);
 		} else {
 			return point;
 		}
 	}
 
 
 
 	/**
 	 * This class helps calculate the CDF of a bivariate gaussian distribution using the
 	 * algorithm in {@link BGD#cdfOfConvexQuadrilaterals(Point, Point, Point, Point, double)}.
 	 * 
 	 * @author <a href="mailto:ramiro.p.magalhaes@gmail.com">Ramiro Pereira de Magalhães</a>
 	 */
 	private static class Quadrilateral {
 		private final double lowerY;
 		private final double upperY;
 
 		/**
 		 * This map defines what functions I should use to get valid x values for a given y inside the
 		 * quadrilateral.
 		 */
 		private final Map<Interval, UnivariateFunction[]> intervalFunction = new HashMap<>(3);
 
 		/**
 		 * Constructs the Quadrilateral from 4 points.
 		 */
 		public Quadrilateral(Point a, Point b, Point c, Point d) {
 			//Fist, get the points sorted in a convenient way.
 			//Notice that, if there are points with the same y, the one with the smaller x goes first in the array
 			final Point ySortedPoints[] = new Point[] {a, b, c, d};
 			Arrays.sort(ySortedPoints, Point.YThenXComparator.instance);
 
 			//we'll need to provide this information later
 			this.lowerY = ySortedPoints[0].y();
 			this.upperY = ySortedPoints[3].y();
 
 			//Now we define which function to use in each limit.
 			//This series of 'ifs' simply set what functions work as limits of the quadrilateral.
 			//There are 5 possibilities of interest: all points have different y positions; 2 points
 			//have the same y and they are not the upper or lower points; 2 points have the same y,
 			//and they are the highest point of the quadrilateral; 2 points have the same y, and
 			//they are the lowest point of the quadrilateral; the object is a horizontal paralelogram,
 			//with 2 points with the same upper y, and 2 points with different lower y.
 			if (ySortedPoints[0].y() != ySortedPoints[1].y() && ySortedPoints[2].y() != ySortedPoints[3].y()) {
 				intervalFunction.put(
 					new Interval(ySortedPoints[0].y(), ySortedPoints[1].y()),
 					new UnivariateFunction[] {
 						getFunction(ySortedPoints[1], ySortedPoints[0]),
 						getFunction(ySortedPoints[2], ySortedPoints[0])
 					}
 				);
 
 				intervalFunction.put(
 					new Interval(ySortedPoints[1].y(), ySortedPoints[2].y()),
 					new UnivariateFunction[] {
 						getFunction(ySortedPoints[3], ySortedPoints[1]),
 						getFunction(ySortedPoints[2], ySortedPoints[0])
 					}
 				);
 
 				intervalFunction.put(
 					new Interval(ySortedPoints[2].y(), ySortedPoints[3].y()),
 					new UnivariateFunction[] {
 						getFunction(ySortedPoints[3], ySortedPoints[2]),
 						getFunction(ySortedPoints[3], ySortedPoints[1])
 					}
 				);
 
 			} else if (ySortedPoints[1].y() == ySortedPoints[2].y() && ySortedPoints[0].y() != ySortedPoints[3].y()) {
 				intervalFunction.put(
 					new Interval(ySortedPoints[0].y(), ySortedPoints[1].y()),
 					new UnivariateFunction[] {
 						getFunction(ySortedPoints[1], ySortedPoints[0]),
 						getFunction(ySortedPoints[2], ySortedPoints[0])
 					}
 				);
 
 				intervalFunction.put(
 					new Interval(ySortedPoints[1].y(), ySortedPoints[3].y()),
 					new UnivariateFunction[] {
 						getFunction(ySortedPoints[3], ySortedPoints[2]),
 						getFunction(ySortedPoints[3], ySortedPoints[1])
 					}
 				);
 
 			} else if (ySortedPoints[0].y() != ySortedPoints[1].y() && ySortedPoints[2].y() == ySortedPoints[3].y()) {
 				intervalFunction.put(
 					new Interval(ySortedPoints[0].y(), ySortedPoints[1].y()),
 					new UnivariateFunction[] {
 						getFunction(ySortedPoints[1], ySortedPoints[0]),
 						getFunction(ySortedPoints[2], ySortedPoints[0])
 					}
 				);
 
 				intervalFunction.put(
 					new Interval(ySortedPoints[1].y(), ySortedPoints[2].y()),
 					new UnivariateFunction[] {
 						getFunction(ySortedPoints[3], ySortedPoints[1]),
 						getFunction(ySortedPoints[2], ySortedPoints[0])
 					}
 				);
 
 			//oh god, what am I doing!!!?!!!
 			} else if (ySortedPoints[0].y() == ySortedPoints[1].y() && ySortedPoints[2].y() != ySortedPoints[3].y()) {
 				intervalFunction.put(
 					new Interval(ySortedPoints[0].y(), ySortedPoints[2].y()),
 					new UnivariateFunction[] {
 						getFunction(ySortedPoints[2], ySortedPoints[0]),
 						getFunction(ySortedPoints[3], ySortedPoints[0])
 					}
 				);
 
 				intervalFunction.put(
 					new Interval(ySortedPoints[2].y(), ySortedPoints[3].y()),
 					new UnivariateFunction[] {
 						getFunction(ySortedPoints[3], ySortedPoints[2]),
 						getFunction(ySortedPoints[3], ySortedPoints[1])
 					}
 				);
 
 			} else if (ySortedPoints[0].y() == ySortedPoints[1].y() && ySortedPoints[2].y() == ySortedPoints[3].y()) {
 				intervalFunction.put(
 					new Interval(ySortedPoints[0].y(), ySortedPoints[3].y()),
 					new UnivariateFunction[] {
 						getFunction(ySortedPoints[2], ySortedPoints[0]),
 						getFunction(ySortedPoints[3], ySortedPoints[1])
 					}
 				);
 
 			} else {
 				throw new RuntimeException("Should NEVER get here");
 			}
 		}
 
 		/**
 		 * Get the left and the right x.
 		 */
 		public double[] getX(double y) {
 			for(Interval interval : intervalFunction.keySet()) {
 				if (interval.contains(y)) {
 					final UnivariateFunction function[] =
 						intervalFunction.get(interval);
 
 					final double[] values = new double[] {
 						function[0].value(y), function[1].value(y)
 					};
 
 					Arrays.sort(values);
 					return values;
 				}
 			}
 
 			return null; //input not in Quadrilateral
 		}
 
 		/**
 		 * Get the lowest y point of this Quadrilateral.
 		 */
 		public double getLowerY() {
 			return lowerY;
 		}
 
 		/**
 		 * Get the highest y point of this Quadrilateral.
 		 */
 		public double getUpperY() {
 			return upperY;
 		}
 
 		/**
 		 * Create an UnivariateFunction that allows us to know the X position
 		 * of a certain Y. Notice: this UnivariateFunction returns the X for
 		 * a certain Y!
 		 */
 		private UnivariateFunction getFunction(Point a, Point b) {
 			double alpha = (a.y() - b.y()) / (a.x() - b.x());
 
 			if (Double.isInfinite(alpha)) {
 				return new PolynomialFunction(new double[]{a.x()});
 			} else {
 				double beta = a.y() - alpha * a.x();
 				return new PolynomialFunction(new double[]{
 					- beta / alpha, 1 / alpha
 				});
 			}
 		}
 
 	}
 
 }
