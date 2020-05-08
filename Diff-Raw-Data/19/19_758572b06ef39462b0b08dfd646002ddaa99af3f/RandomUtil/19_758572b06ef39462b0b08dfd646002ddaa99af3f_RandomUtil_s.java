 /**
  * Java Modular Image Synthesis Toolkit (JMIST)
  * Copyright (C) 2008-2013 Bradley W. Kimmel
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package ca.eandb.jmist.framework.random;
 
 import ca.eandb.jmist.framework.Random;
 import ca.eandb.jmist.math.Box2;
 import ca.eandb.jmist.math.Box3;
 import ca.eandb.jmist.math.Interval;
 import ca.eandb.jmist.math.MathUtil;
 import ca.eandb.jmist.math.Point2;
 import ca.eandb.jmist.math.Point3;
 import ca.eandb.jmist.math.PolarCoordinates;
 import ca.eandb.jmist.math.Sphere;
 import ca.eandb.jmist.math.SphericalCoordinates;
 import ca.eandb.jmist.math.Vector2;
 import ca.eandb.jmist.math.Vector3;
 
 
 
 /**
  * Static utility methods for random algorithms.
  * @author Brad Kimmel
  */
 public final class RandomUtil {
 
 	public static SphericalCoordinates diffuse(Random random) {
 		return diffuse(random.next(), random.next());
 	}
 
 	public static SphericalCoordinates diffuse(double ru, double rv) {
 
 		assert(0.0 <= ru && ru <= 1.0);
 		assert(0.0 <= rv && rv <= 1.0);
 
 		return new SphericalCoordinates(
 				Math.acos(Math.sqrt(1.0 - ru)),
 				2.0 * Math.PI * rv
 		);
 
 	}
 
 	public static SphericalCoordinates uniformOnUpperHemisphere(Random random) {
 		return uniformOnUpperHemisphere(1.0, random.next(), random.next());
 	}
 
 	public static SphericalCoordinates uniformOnUpperHemisphere(double radius, Random random) {
 		return uniformOnUpperHemisphere(radius, random.next(), random.next());
 	}
 
 	public static SphericalCoordinates uniformOnUpperHemisphere(double radius, double ru, double rv) {
 
 		// TODO implement this directly so it's more efficient.
 		SphericalCoordinates result = uniformOnSphere(radius, ru, rv);
 
 		if (result.polar() > (Math.PI / 2.0))
 			result = new SphericalCoordinates(Math.PI - result.polar(), result.azimuthal(), radius);
 
 		return result;
 
 	}
 
 	public static SphericalCoordinates uniformOnSphere(Random random) {
 		return uniformOnSphere(1.0, random.next(), random.next());
 	}
 
 	public static SphericalCoordinates uniformOnSphere(double radius, Random random) {
 		return uniformOnSphere(radius, random.next(), random.next());
 	}
 	
 	public static SphericalCoordinates uniformOnSphere(double ru, double rv) {
 		return uniformOnSphere(1.0, ru, rv);
 	}
 
 	public static SphericalCoordinates uniformOnSphere(double radius, double ru, double rv) {
 
 		assert(0.0 <= ru && ru <= 1.0);
 		assert(0.0 <= rv && rv <= 1.0);
 
 		return new SphericalCoordinates(
 				Math.acos(2.0 * ru - 1.0),
 				2.0 * Math.PI * rv,
 				radius
 		);
 
 	}
 	
 	public static SphericalCoordinates uniformInsideSphere(Random random) {
 		return uniformInsideSphere(1.0, random.next(), random.next(), random.next());
 	}
 	
 	public static SphericalCoordinates uniformInsideSphere(double radius, Random random) {
 		return uniformInsideSphere(radius, random.next(), random.next(), random.next());
 	}
 	
 	public static SphericalCoordinates uniformInsideSphere(double ru, double rv, double rw) {
 		return uniformInsideSphere(1.0, ru, rv, rw);
 	}
 	
 	public static SphericalCoordinates uniformInsideSphere(double radius, double ru, double rv, double rw) {
 
 		assert(0.0 <= ru && ru <= 1.0);
 		assert(0.0 <= rv && rv <= 1.0);
 		assert(0.0 <= rw && rw <= 1.0);
 
 		return new SphericalCoordinates(
 				Math.acos(2.0 * ru - 1.0),
 				2.0 * Math.PI * rv,
 				radius * Math.cbrt(rw)
 		);
 
 	}
 
 	public static PolarCoordinates uniformOnDisc(Random random) {
 		return uniformOnDisc(1.0, random.next(), random.next());
 	}
 
 	public static PolarCoordinates uniformOnDisc(double radius, Random random) {
 		return uniformOnDisc(radius, random.next(), random.next());
 	}
 	
 	public static PolarCoordinates uniformOnDisc(double ru, double rv) {
 		return uniformOnDisc(1.0, ru, rv);
 	}
 
 	public static PolarCoordinates uniformOnDisc(double radius, double ru, double rv) {
 
 		assert(0.0 <= ru && ru <= 1.0);
 		assert(0.0 <= rv && rv <= 1.0);
 
 		return new PolarCoordinates(
 				2.0 * Math.PI * ru,
 				radius * Math.sqrt(rv)
 		);
 
 	}
 
 	public static Point3 uniformOnTriangle(Point3 a, Point3 b, Point3 c, Random random) {
 		return uniformOnTriangle(a, b, c, random.next(), random.next());
 	}
 
 	public static Point3 uniformOnTriangle(Point3 a, Point3 b, Point3 c, double alpha, double beta) {
 		if (alpha + beta > 1.0) {
 			alpha = 1.0 - alpha;
 			beta = 1.0 - beta;
 		}
 		Vector3 ab = a.vectorTo(b).times(alpha);
 		Vector3 ac = a.vectorTo(c).times(beta);
 		return a.plus(ab).plus(ac);
 	}
 
 	public static Point2 uniformOnTriangle(Point2 a, Point2 b, Point2 c, Random random) {
 		return uniformOnTriangle(a, b, c, random.next(), random.next());
 	}
 
 	public static Point2 uniformOnTriangle(Point2 a, Point2 b, Point2 c, double alpha, double beta) {
 		if (alpha + beta > 1.0) {
 			alpha = 1.0 - alpha;
 			beta = 1.0 - beta;
 		}
 		Vector2 ab = a.vectorTo(b).times(alpha);
 		Vector2 ac = a.vectorTo(c).times(beta);
 		return a.plus(ab).plus(ac);
 	}
 	
 	public static boolean bernoulli(double probability, Random random) {
 		return bernoulli(probability, random.next());
 	}
 
 	public static boolean bernoulli(double probability, double seed) {
 		return seed < probability;
 	}
 	
 	private static void bp() {}
 	
 	public static boolean bernoulli(double probability, SeedReference ref) {
 		if (ref.seed < probability) {
 			ref.seed /= probability;
 			if (ref.seed < 0.0 || ref.seed > 1.0) bp();
 			return true;
 		} else {
 			ref.seed = (ref.seed - probability) / (1.0 - probability);
 			if (ref.seed < 0.0 || ref.seed > 1.0) bp();
 			return false;
 		}
 	}
 
 	public static boolean coin(Random random) {
 		return coin(random.next());
 	}
 
 	public static boolean coin(double seed) {
 		return seed < 0.5;
 	}
 	
 	public static boolean coin(SeedReference ref) {
 		if (ref.seed < 0.5) {
 			ref.seed *= 2;
 			if (ref.seed < 0.0 || ref.seed > 1.0) bp();
 			return true;
 		} else {
 			ref.seed = (ref.seed * 2) - 1;
 			if (ref.seed < 0.0 || ref.seed > 1.0) bp();
 			return false;
 		}
 	}
 
 	public static int categorical(double[] weights, Random random) {
 		return categorical(weights, random.next());
 	}
	
 	public static int categorical(double[] weights, double seed) {
 
		double	x		= seed / MathUtil.sum(weights);
 		double	mark	= 0.0;
 
 		for (int i = 0; i < weights.length; i++) {
 			if (x < (mark += weights[i])) {
 				return i;
 			}
 		}
 
 		return weights.length - 1;
 
 	}
	
 	public static int categorical(double[] weights, SeedReference ref) {
 
		ref.seed /= MathUtil.sum(weights);
		
 		int n = weights.length - 1;
 		for (int i = 0; i < n; i++) {
 			if (ref.seed < weights[i]) {
 				ref.seed /= weights[i];
 				if (ref.seed < 0.0 || ref.seed > 1.0) bp();
 				return i;
 			}
 			ref.seed -= weights[i];
 		}
		
 		ref.seed /= weights[n];
 		if (ref.seed < 0.0 || ref.seed > 1.0) bp();
 		return n;
		
 	}
 
 	public static int discrete(int minimum, int maximum, Random random) {
 		return discrete(minimum, maximum, random.next());
 	}
 
 	public static int discrete(int minimum, int maximum, double seed) {
 		return minimum + (int) Math.floor(seed * (double) (maximum - minimum + 1));
 	}
 	
 	public static int discrete(int minimum, int maximum, SeedReference ref) {
 		double offset;
 		
 		ref.seed *= (double) (maximum - minimum + 1);
 		offset = Math.floor(ref.seed);
 		ref.seed -= offset;
 		
 		if (ref.seed < 0.0 || ref.seed > 1.0) bp();
 		return minimum + (int) offset;
 	}
 
 	public static double canonical(Random random) {
 		return random.next();
 	}
 
 	public static Point2 canonical2(Random random) {
 		return new Point2(random.next(), random.next());
 	}
 
 	public static Point3 canonical3(Random random) {
 		return new Point3(random.next(), random.next(), random.next());
 	}
 
 	public static double uniform(double minimum, double maximum, double seed) {
 		return minimum + seed * (maximum - minimum);
 	}
 
 	public static double uniform(double minimum, double maximum, Random random) {
 		return uniform(minimum, maximum, random.next());
 	}
 
 	public static double uniform(Interval I, Random random) {
 		return uniform(I, random.next());
 	}
 
 	public static double uniform(Interval I, double seed) {
 		return uniform(I.minimum(), I.maximum(), seed);
 	}
 
 	public static Point2 uniform(Box2 box, Random random) {
 		return uniform(box, random.next(), random.next());
 	}
 	
 	public static Point2 uniform(Box2 box, double ru, double rv) {
 		return new Point2(
 				uniform(box.minimumX(), box.maximumX(), ru),
 				uniform(box.minimumY(), box.maximumY(), rv));		
 	}
 	
 	public static Point3 uniform(Box3 box, Random random) {
 		return uniform(box, random.next(), random.next(), random.next());
 	}
 
 	public static Point3 uniform(Box3 box, double ru, double rv, double rw) {
 		return new Point3(
 				uniform(box.minimumX(), box.maximumX(), ru),
 				uniform(box.minimumY(), box.maximumY(), rv),
 				uniform(box.minimumZ(), box.maximumZ(), rw));
 	}
 
 }
