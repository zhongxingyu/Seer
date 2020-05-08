 package plugins.adufour.connectedcomponents;
 
 import icy.image.IcyBufferedImage;
 import icy.sequence.Sequence;
 import icy.type.DataType;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.vecmath.Point2d;
 import javax.vecmath.Point3d;
 import javax.vecmath.Point3i;
 import javax.vecmath.SingularMatrixException;
 import javax.vecmath.Vector3d;
 
 import plugins.adufour.vars.lang.VarDouble;
 import Jama.EigenvalueDecomposition;
 import Jama.Matrix;
 
 public class ShapeDescriptor
 {
 	/**
 	 * Computes the bounding box of this component, and stores the result into the given arguments
 	 * 
 	 * @param cc
 	 *            the input component
 	 * @param minBounds
 	 *            the first corner of the bounding box in X-Y-Z order (Upper-Left hand-Top)
 	 * @param maxBounds
 	 *            the second corner of the bounding box in X-Y-Z order (Lower-Right hand-Bottom)
 	 */
 	public static void computeBoundingBox(ConnectedComponent cc, Point3i minBounds, Point3i maxBounds)
 	{
 		if (minBounds != null)
 		{
 			minBounds.x = Integer.MAX_VALUE;
 			minBounds.y = Integer.MAX_VALUE;
 			minBounds.z = Integer.MAX_VALUE;
 		}
 		
 		if (maxBounds != null)
 		{
 			maxBounds.x = 0;
 			maxBounds.y = 0;
 			maxBounds.z = 0;
 		}
 		
 		for (Point3i point : cc)
 		{
 			if (minBounds != null)
 			{
 				minBounds.x = Math.min(minBounds.x, point.x);
 				minBounds.y = Math.min(minBounds.y, point.y);
 				minBounds.z = Math.min(minBounds.z, point.z);
 			}
 			if (maxBounds != null)
 			{
 				maxBounds.x = Math.max(maxBounds.x, point.x);
 				maxBounds.y = Math.max(maxBounds.y, point.y);
 				maxBounds.z = Math.max(maxBounds.z, point.z);
 			}
 		}
 	}
 	
 	/**
 	 * Computes the bounding box of this component, and stores the result into the given arguments
 	 * 
 	 * @param cc
 	 *            the input component
 	 * @param bsCenter
 	 *            the computed center of the bounding sphere
 	 * @param maxBounds
 	 *            the computed radius of the bounding sphere
 	 */
 	public static void computeBoundingSphere(ConnectedComponent cc, Point3d bsCenter, VarDouble bsRadius)
 	{
 		bsCenter.set(cc.getMassCenter());
 		bsRadius.setValue(cc.getMaxDistanceTo(bsCenter));
 	}
 	
 	/**
 	 * @param outputSequence
 	 *            (set to null if not wanted) an output sequence to receive the extracted contour
 	 * @return An array containing all the contour pixels of this component
 	 */
 	public static double computePerimeter(ConnectedComponent cc, ArrayList<Point3i> contourPoints, Sequence outputSequence)
 	{
 		double perimeter = 0;
 		
 		if (contourPoints != null) contourPoints.ensureCapacity(cc.getSize() / 2);
 		
 		Point3i min = new Point3i(), max = new Point3i();
 		ShapeDescriptor.computeBoundingBox(cc, min, max);
 		
 		Sequence mask = cc.toSequence();
 		int w = mask.getSizeX();
 		int h = mask.getSizeY();
 		int d = mask.getSizeZ();
 		
 		byte[][] outputMask = null;
 		
 		if (outputSequence != null)
 		{
 			outputSequence.removeAllImage();
 			for (int i = 0; i < d; i++)
 			{
 				outputSequence.setImage(0, i, new IcyBufferedImage(w, h, 1, DataType.UBYTE));
 			}
 			
 			outputMask = outputSequence.getDataXYZAsByte(0, 0);
 		}
 		
 		byte[][] mask_z_xy = mask.getDataXYZAsByte(0, 0);
 		
 		Point3i localP = new Point3i();
 		
 		if (min.z != max.z)
 		{
 			// TODO correct 3D bias w.r.t. digitization + depth resolution
 			mainLoop: for (Point3i p : cc)
 			{
 				localP.sub(p, min);
 				
 				int xy = localP.y * w + localP.x;
 				
 				if (localP.x == 0 || localP.y == 0 || localP.x == w - 1 || localP.y == h - 1 || localP.z == 0 || localP.z == d - 1)
 				{
 					perimeter++;
 					if (contourPoints != null) contourPoints.add(p);
 					if (outputMask != null) outputMask[localP.z][xy] = (byte) 1;
 					continue;
 				}
 				
 				byte[][] neighborhood = new byte[][] { mask_z_xy[localP.z - 1], mask_z_xy[localP.z], mask_z_xy[localP.z + 1] };
 				
 				for (byte[] z : neighborhood)
 					if (z[xy - w] == 0 || z[xy - 1] == 0 || z[xy + 1] == 0 || z[xy + w] == 0 || z[xy - w - 1] == 0 || z[xy - w + 1] == 0 || z[xy + w - 1] == 0 || z[xy + w + 1] == 0)
 					{
 						perimeter++;
 						if (contourPoints != null) contourPoints.add(p);
 						if (outputMask != null) outputMask[localP.z][xy] = (byte) 1;
 						continue mainLoop;
 					}
 				
 				// the top and bottom neighbors were forgotten in the previous loop
 				if (mask_z_xy[localP.z - 1][xy] == 0 || mask_z_xy[localP.z + 1][xy] == 0)
 				{
 					perimeter++;
 					if (contourPoints != null) contourPoints.add(p);
 					if (outputMask != null) outputMask[localP.z][xy] = (byte) 1;
 				}
 			}
 		}
 		else
 		{
 			double _a = 0, _b = 0;
 			
 			for (Point3i p : cc)
 			{
 				localP.sub(p, min);
 				
 				int xy = localP.y * w + localP.x;
 				
 				if (localP.x == 0 || localP.y == 0 || localP.x == w - 1 || localP.y == h - 1)
 				{
 					_a++;
 					perimeter++;
 					if (contourPoints != null) contourPoints.add(p);
 					if (outputMask != null) outputMask[localP.z][xy] = (byte) 1;
 					continue;
 				}
 				
 				byte[] z = mask_z_xy[localP.z];
 				
 				if (z[xy - w] == 0 || z[xy - 1] == 0 || z[xy + 1] == 0 || z[xy + w] == 0)
 				{
 					_a++;
 					perimeter++;
 					if (contourPoints != null) contourPoints.add(p);
 					if (outputMask != null) outputMask[localP.z][xy] = (byte) 1;
 				}
 				else if (z[xy - w - 1] == 0 || z[xy - w + 1] == 0 || z[xy + w - 1] == 0 || z[xy + w + 1] == 0)
 				{
 					_b++;
 					perimeter++;
 					if (contourPoints != null) contourPoints.add(p);
 					if (outputMask != null) outputMask[localP.z][xy] = (byte) 1;
 				}
 			}
 			
 			// adjust the perimeter empirically according to the edge pixel distribution
 			double err = Math.log10(_a + _b) - Math.E;
 			perimeter *= (1 - (_b / _a / 2));
 			if (err > 0) perimeter -= (err * 20);
 		}
 		
 		return perimeter;
 	}
 	
 	/**
 	 * Compute the circularity of the given component, measured as a weighted ratio between the
 	 * component's dimensions.
 	 * 
 	 * @param cc
 	 *            the input component
 	 * @return 1 for a perfect circle (or sphere), and lower than 1 otherwise
 	 */
 	public static double computeSphericity(ConnectedComponent cc)
 	{
 		double dim = is2D(cc) ? 2.0 : 3.0;
 		
 		double area = cc.getSize();
 		double perimeter = computePerimeter(cc, null, null);
 		
 		// some verification code
 		//
 		// double peri_real = Math.PI * (maxBB.x - minBB.x + 1);
 		// System.out.println("p = " + perimeter + ", should be " + peri_real + " => " + (perimeter
 		// / peri_real));
 		//
 		// double surf = (perimeter * perimeter / (Math.PI * 4));
 		// double surf_real = (Math.PI * 0.25 * (maxBB.x - minBB.x + 1) * (maxBB.x - minBB.x + 1));
 		// System.out.println("surface = " + surf + ", should be " + surf_real + " => " + (surf_real
 		// / surf));
 		//
 		// end of the verification code
 		
 		return (Math.pow(Math.PI, 1.0 / dim) / perimeter) * Math.pow(area * dim * 2, (dim - 1) / dim);
 	}
 	
 	/**
 	 * Computes the eccentricity of the given component. This method fits an ellipse with radii a
 	 * and b (in 2D) or an ellipsoid with radii a, b and c (in 3D) and returns in both cases the
 	 * ratio b/a
 	 * 
 	 * @param cc
 	 *            the input component
 	 * @return the ratio b/a, where a and b are the two first largest ellipse radii (there are only
 	 *         two in 2D)
 	 */
 	public static double computeEccentricity(ConnectedComponent cc)
 	{
 		if (is2D(cc))
 		{
 			try
 			{
 				Point2d radii = new Point2d();
 				computeEllipse(cc, null, radii, null, null);
 				return Math.min(radii.x, radii.y) / Math.max(radii.x, radii.y);
 			}
 			catch (RuntimeException e)
 			{
 				// error during the ellipse computation
 				return Double.NaN;
 			}
 		}
 		else
 		{
 			Point3d radii = new Point3d();
			computeEllipse(cc, null, radii, null, null);
			return Math.min(radii.x, radii.y) / Math.max(radii.x, radii.y);
 		}
 	}
 	
 	/**
 	 * Computes the geometric moment of the given component
 	 * 
 	 * @param cc
 	 *            the input component
 	 * @param p
 	 *            the moment order along X
 	 * @param q
 	 *            the moment order along Y
 	 * @param r
 	 *            the moment order along Z (set to 0 if the object is 2D)
 	 * @return the geometric moment
 	 */
 	public static double computeGeometricMoment(ConnectedComponent cc, int p, int q, int r)
 	{
 		double moment = 0;
 		
 		Point3d center = cc.getMassCenter();
 		
 		if (is2D(cc))
 		{
 			for (Point3i point : cc)
 				moment += Math.pow(point.x - center.x, p) * Math.pow(point.y - center.y, q);
 		}
 		else
 		{
 			for (Point3i point : cc)
 				moment += Math.pow(point.x - center.x, p) * Math.pow(point.y - center.y, q) * Math.pow(point.z - center.z, r);
 		}
 		return moment;
 	}
 	
 	/**
 	 * Compute the best fitting ellipsoid for the given component.<br>
 	 * This method is adapted from Yury Petrov's Matlab code and ported to Java by the BoneJ project
 	 * 
 	 * @param cc
 	 *            the component to fit
 	 * @param center
 	 *            (set to null if not wanted) the computed ellipsoid center
 	 * @param radii
 	 *            (set to null if not wanted) the computed ellipsoid radius in each eigen-direction
 	 * @param eigenVectors
 	 *            (set to null if not wanted) the computed ellipsoid eigen-vectors
 	 * @param equation
 	 *            (set to null if not wanted) an array of size 9 containing the compute ellipsoid
 	 *            equation
 	 * @throws IllegalArgumentException
 	 *             if the number of points in the component is too low (minimum is 9)
 	 * @throws SingularMatrixException
 	 *             if the component is flat (i.e. lies in a 2D plane)
 	 */
 	public static void computeEllipse(ConnectedComponent cc, Point3d center, Point3d radii, Vector3d[] eigenVectors, double[] equation) throws IllegalArgumentException
 	{
 		int nPoints = cc.getSize();
 		if (nPoints < 9)
 		{
 			throw new IllegalArgumentException("Too few points; need at least 9 to calculate a unique ellipsoid");
 		}
 		
 		Point3i[] points = cc.getPoints();
 		
 		double[][] d = new double[nPoints][9];
 		for (int i = 0; i < nPoints; i++)
 		{
 			double x = points[i].x;
 			double y = points[i].y;
 			double z = points[i].z;
 			d[i][0] = (x * x);
 			d[i][1] = (y * y);
 			d[i][2] = (z * z);
 			d[i][3] = (2.0D * x * y);
 			d[i][4] = (2.0D * x * z);
 			d[i][5] = (2.0D * y * z);
 			d[i][6] = (2.0D * x);
 			d[i][7] = (2.0D * y);
 			d[i][8] = (2.0D * z);
 		}
 		
 		Matrix D = new Matrix(d);
 		Matrix ones = ones(nPoints, 1);
 		
 		Matrix V;
 		try
 		{
 			V = D.transpose().times(D).inverse().times(D.transpose().times(ones));
 		}
 		catch (RuntimeException e)
 		{
 			throw new SingularMatrixException("The component is most probably flat (i.e. lies in a 2D plane)");
 		}
 		
 		double[] v = V.getColumnPackedCopy();
 		
 		double[][] a = { { v[0], v[3], v[4], v[6] }, { v[3], v[1], v[5], v[7] }, { v[4], v[5], v[2], v[8] }, { v[6], v[7], v[8], -1.0D } };
 		Matrix A = new Matrix(a);
 		Matrix C = A.getMatrix(0, 2, 0, 2).times(-1.0D).inverse().times(V.getMatrix(6, 8, 0, 0));
 		Matrix T = Matrix.identity(4, 4);
 		T.setMatrix(3, 3, 0, 2, C.transpose());
 		Matrix R = T.times(A.times(T.transpose()));
 		double r33 = R.get(3, 3);
 		Matrix R02 = R.getMatrix(0, 2, 0, 2);
 		EigenvalueDecomposition E = new EigenvalueDecomposition(R02.times(-1.0D / r33));
 		Matrix eVal = E.getD();
 		Matrix eVec = E.getV();
 		Matrix diagonal = diag(eVal);
 		
 		if (radii != null) radii.set(Math.sqrt(1.0D / diagonal.get(0, 0)), Math.sqrt(1.0D / diagonal.get(1, 0)), Math.sqrt(1.0D / diagonal.get(2, 0)));
 		
 		if (center != null) center.set(C.get(0, 0), C.get(1, 0), C.get(2, 0));
 		
 		if (eigenVectors != null && eigenVectors.length == 3)
 		{
 			eigenVectors[0] = new Vector3d(eVec.get(0, 0), eVec.get(0, 1), eVec.get(0, 2));
 			eigenVectors[1] = new Vector3d(eVec.get(1, 0), eVec.get(1, 1), eVec.get(1, 2));
 			eigenVectors[2] = new Vector3d(eVec.get(2, 0), eVec.get(2, 1), eVec.get(2, 2));
 		}
 		if (equation != null && equation.length == 9) System.arraycopy(v, 0, equation, 0, v.length);
 	}
 	
 	/**
 	 * 2D direct ellipse fitting.<br>
 	 * (Java port of Chernov's MATLAB implementation of the direct ellipse fit)
 	 * 
 	 * @param cc
 	 *            the component to fit
 	 * @param center
 	 *            (set to null if not wanted) the computed ellipse center
 	 * @param radii
 	 *            (set to null if not wanted) the computed ellipse radius in each eigen-direction
 	 * @param angle
 	 *            (set to null if not wanted) the computed ellipse orientation
 	 * @param equation
 	 *            (set to null if not wanted) a 6-element array, {a b c d f g}, which are the
 	 *            algebraic parameters of the fitting ellipse: <i>ax</i><sup>2</sup> + 2<i>bxy</i> +
 	 *            <i>cy</i><sup>2</sup> +2<i>dx</i> + 2<i>fy</i> + <i>g</i> = 0. The vector <b>A</b>
 	 *            represented in the array is normed, so that ||<b>A</b>||=1.
 	 * @throws RuntimeException
 	 *             if the ellipse calculation fails (e.g. if a singular matrix is detected)
 	 */
 	public static void computeEllipse(ConnectedComponent cc, Point2d center, Point2d radii, VarDouble angle, double[] equation) throws RuntimeException
 	{
 		Point3i[] points = cc.getPoints();
 		Point3d ccenter = cc.getMassCenter();
 		
 		double[][] d1 = new double[cc.getSize()][3];
 		double[][] d2 = new double[cc.getSize()][3];
 		
 		for (int i = 0; i < d1.length; i++)
 		{
 			final double xixC = points[i].x - ccenter.x;
 			final double yiyC = points[i].y - ccenter.y;
 			
 			d1[i][0] = xixC * xixC;
 			d1[i][1] = xixC * yiyC;
 			d1[i][2] = yiyC * yiyC;
 			
 			d2[i][0] = xixC;
 			d2[i][1] = yiyC;
 			d2[i][2] = 1;
 		}
 		
 		Matrix D1 = new Matrix(d1);
 		Matrix D2 = new Matrix(d2);
 		
 		Matrix S1 = D1.transpose().times(D1);
 		
 		Matrix S2 = D1.transpose().times(D2);
 		
 		Matrix S3 = D2.transpose().times(D2);
 		
 		Matrix T = (S3.inverse().times(-1)).times(S2.transpose());
 		
 		Matrix M = S1.plus(S2.times(T));
 		
 		double[][] m = M.getArray();
 		double[][] n = { { m[2][0] / 2, m[2][1] / 2, m[2][2] / 2 }, { -m[1][0], -m[1][1], -m[1][2] }, { m[0][0] / 2, m[0][1] / 2, m[0][2] / 2 } };
 		
 		Matrix N = new Matrix(n);
 		
 		EigenvalueDecomposition E = N.eig();
 		Matrix eVec = E.getV();
 		
 		Matrix R1 = eVec.getMatrix(0, 0, 0, 2);
 		Matrix R2 = eVec.getMatrix(1, 1, 0, 2);
 		Matrix R3 = eVec.getMatrix(2, 2, 0, 2);
 		
 		Matrix cond = (R1.times(4)).arrayTimes(R3).minus(R2.arrayTimes(R2));
 		
 		int _f = 0;
 		for (int i = 0; i < 3; i++)
 		{
 			if (cond.get(0, i) > 0)
 			{
 				_f = i;
 				break;
 			}
 		}
 		Matrix A1 = eVec.getMatrix(0, 2, _f, _f);
 		
 		Matrix A = new Matrix(6, 1);
 		A.setMatrix(0, 2, 0, 0, A1);
 		A.setMatrix(3, 5, 0, 0, T.times(A1));
 		
 		double[] ellipse = A.getColumnPackedCopy();
 		double a4 = ellipse[3] - 2 * ellipse[0] * ccenter.x - ellipse[1] * ccenter.y;
 		double a5 = ellipse[4] - 2 * ellipse[2] * ccenter.y - ellipse[1] * ccenter.x;
 		double a6 = ellipse[5] + ellipse[0] * ccenter.x * ccenter.x + ellipse[2] * ccenter.y * ccenter.y + ellipse[1] * ccenter.x * ccenter.y - ellipse[3] * ccenter.x - ellipse[4] * ccenter.y;
 		A.set(3, 0, a4);
 		A.set(4, 0, a5);
 		A.set(5, 0, a6);
 		A = A.times(1 / A.normF());
 		
 		ellipse = A.getColumnPackedCopy();
 		
 		if (equation != null && equation.length != 6) System.arraycopy(ellipse, 0, equation, 0, 6);
 		
 		// Convert the general ellipse equation ax2 + bxy + cy2 +dx + fy + g = 0
 		// into geometric parameters: center, radii and orientation.
 		final double a = ellipse[0];
 		final double b = ellipse[1] / 2;
 		final double c = ellipse[2];
 		final double d = ellipse[3] / 2;
 		final double f = ellipse[4] / 2;
 		final double g = ellipse[5];
 		
 		// centre
 		final double cX = (c * d - b * f) / (b * b - a * c);
 		final double cY = (a * f - b * d) / (b * b - a * c);
 		
 		// semiaxis length
 		final double af = 2 * (a * f * f + c * d * d + g * b * b - 2 * b * d * f - a * c * g);
 		
 		final double aL = Math.sqrt((af) / ((b * b - a * c) * (Math.sqrt((a - c) * (a - c) + 4 * b * b) - (a + c))));
 		
 		final double bL = Math.sqrt((af) / ((b * b - a * c) * (-Math.sqrt((a - c) * (a - c) + 4 * b * b) - (a + c))));
 		double phi = 0;
 		if (b == 0)
 		{
 			if (a <= c)
 				phi = 0;
 			else if (a > c) phi = Math.PI / 2;
 		}
 		else
 		{
 			if (a < c)
 				phi = Math.atan(2 * b / (a - c)) / 2;
 			else if (a > c) phi = Math.atan(2 * b / (a - c)) / 2 + Math.PI / 2;
 		}
 		
 		if (center != null) center.set(cX, cY);
 		if (radii != null) radii.set(aL, bL);
 		if (angle != null) angle.setValue(phi);
 	}
 	
 	public static boolean is2D(ConnectedComponent cc)
 	{
 		Point3i minBB = new Point3i();
 		Point3i maxBB = new Point3i();
 		computeBoundingBox(cc, minBB, maxBB);
 		
 		return minBB.z == maxBB.z;
 	}
 	
 	private static Matrix diag(Matrix matrix)
 	{
 		int min = Math.min(matrix.getRowDimension(), matrix.getColumnDimension());
 		double[][] diag = new double[min][1];
 		for (int i = 0; i < min; i++)
 		{
 			diag[i][0] = matrix.get(i, i);
 		}
 		return new Matrix(diag);
 	}
 	
 	private static Matrix ones(int m, int n)
 	{
 		double[][] array = new double[m][n];
 		for (double[] row : array)
 			Arrays.fill(row, 1.0);
 		return new Matrix(array, m, n);
 	}
 }
