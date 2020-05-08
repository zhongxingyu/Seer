 import java.awt.Button;
 import java.awt.Checkbox;
 import java.awt.CheckboxGroup;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import Jama.Matrix;
 
 import jv.geom.PgElementSet;
 import jv.geom.PgVectorField;
 import jv.project.PgGeometryIf;
 import jv.project.PvGeometryListenerIf;
 import jv.vecmath.PdMatrix;
 import jv.vecmath.PdVector;
 import jvx.numeric.PnJacobi;
 
 class Curvature {
 	public Curvature() {
 		area = 0;
 		meanOp = new PdVector(0, 0, 0);
 		gaussian = 0;
 		// not computed by default
 		B = null;
 	}
 	/**
 	 * mean curvature normal operator
 	 *
 	 * note: not normalized! i.e. misses 1/(2*area)
 	 * see eq. 8.
 	 **/
 	public PdVector meanOp;
 	/**
 	 * gaussian curvature Operator
 	 * note: not normalized! i.e. is just the sum of angles, misses (2pi - ...)/area
 	 * note: in degree!
 	 * @see gaussianCurvature()
 	 **/
 	public double gaussian;
 	/**
 	 * mixed area
 	 * see fig. 4
 	 */
 	public double area;
 	/**
 	 * Symmetric curvature tensor:
 	 *
 	 *  a | b
 	 * ---|---
 	 *  b | c
 	 */
 	public PdMatrix B;
 	public double gaussianCurvature() {
 		return (2.0d * Math.PI - Math.toRadians(gaussian)) / area;
 	}
 	public double meanCurvature() {
 		// note: 1/2 from K as vector, another 1/2 for K_H
 		return 1.0d / (4.0 * area) * meanOp.length();
 	}
 	public double minimumCurvature() {
 		return meanCurvature() - Math.sqrt(delta());
 	}
 	public double maximumCurvature() {
 		return meanCurvature() + Math.sqrt(delta());
 	}
 	public double delta() {
 		return Math.max(0, Math.pow(meanCurvature(), 2) - gaussianCurvature());
 	}
 	/**
 	 * Normalize the mean curvature operator and return it.
 	 *
 	 * According to the paper by Meyer e.a. this is the
 	 * normal of the tangent plane.
	 *
	 * Returns null if mean curvature is zero.
 	 */
 	public PdVector tangentPlaneNormal()
 	{
		if (meanCurvature() == 0) {
			return null;
		}
 		assert meanOp.length() > 0;
 		PdVector n = (PdVector) meanOp.clone();
 		n.normalize();
 		return n;
 	}
 	/**
 	 * Return matrix describing the tangent plane.
 	 * 
 	 * Row 1: tangent plane normal
 	 * Row 2: arbitrary normal to tangent plane normal
 	 * Row 3: cross product of the other two vectors
	 *
	 * Returns null if mean curvature is zero.
 	 */
 	public PdMatrix tangentPlane()
 	{
		if (meanCurvature() == 0) {
			return null;
		}
 		PdVector n = tangentPlaneNormal();
 		PdVector x = PdVector.normalToVectorNew(n);
 		PdVector y = PdVector.crossNew(n, x);
 		PdMatrix ret = new PdMatrix(3, 3);
 		ret.setRow(0, n);
 		ret.setRow(1, x);
 		ret.setRow(2, y);
 		return ret;
 	}
 }
 
 class CotanCache {
 	public CotanCache(int size)
 	{
 		map = new HashMap<Double, Double>(size);
 	}
 	double cotan(double degree)
 	{
 		assert degree > 0 : degree;
 		if (degree == 90) {
 			return 0;
 		}
 		Double val = map.get(degree);
 		if (val == null) {
 			assert Math.tan(Math.toRadians(degree)) != 0 : degree;
 			val = 1.0d / Math.tan(Math.toRadians(degree));
 			assert !val.isNaN() : degree;
 			assert !val.isInfinite() : degree;
 			map.put(degree, val);
 		}
 		return val;
 	}
 	private HashMap<Double, Double> map;
 }
 
 /**
  * Solution to third exercise of second project
  * 
  * @author		Milian Wolff
  * @version		26.11.2011, 1.00 created
  */
 public class Ex2_3 extends ProjectBase implements PvGeometryListenerIf, ItemListener, ActionListener
 {
 	public static void main(String[] args)
 	{
 		new Ex2_3(args);
 	}
 	private Checkbox m_disableCurvature;
 	private Checkbox m_gaussianCurvature;
 	private Checkbox m_meanCurvature;
 	private Checkbox m_minimumCurvature;
 	private Checkbox m_maximumCurvature;
 	private CurvatureType m_curvatureType;
 	private enum CurvatureType {
 		Disable,
 		Mean,
 		Gaussian,
 		Minimum,
 		Maximum
 	}
 	private Checkbox m_curvatureTensor;
 	private boolean m_displayTensor;
 	private TensorType m_tensorType;
 	private enum TensorType {
 		Minor,
 		Major,
 		Both
 	}
 	private Button m_smoothTensor;
 	private Checkbox m_colorByMax;
 	private Checkbox m_colorByDeviation;
 	private Checkbox m_noColors;
 	private ColorType m_colorType;
 	private Checkbox m_minorTensor;
 	private Checkbox m_majorTensor;
 	private Checkbox m_bothTensor;
 	private enum ColorType {
 		NoColors,
 		Maximum,
 		Deviation
 	}
 	public Ex2_3(String[] args)
 	{
 		super(args, "SciVis - Project 2 - Exercise 3 - Milian Wolff");
 		// listener
 		m_disp.addGeometryListener(this);
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridwidth = 1;
 		c.gridx = 0;
 		c.gridy = 0;
 
 		c.gridy++;
 		c.fill = GridBagConstraints.CENTER;
 		Font boldFont = new Font("Dialog", Font.BOLD, 12);
 		Label l = new Label("Curvature");
 		l.setFont(boldFont);
 		m_panel.add(l, c);
 		// curvature method choice
 		CheckboxGroup group = new CheckboxGroup();
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		// disable curvature
 		m_disableCurvature = new Checkbox("Disabled", group, false);
 		m_disableCurvature.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_disableCurvature, c);
 
 		// mean curvature
 		m_meanCurvature = new Checkbox("Mean", group, true);
 		m_meanCurvature.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_meanCurvature, c);
 
 		// gaussian curvature
 		m_gaussianCurvature = new Checkbox("Gaussian", group, false);
 		m_gaussianCurvature.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_gaussianCurvature, c);
 
 		// minimum curvature
 		m_minimumCurvature = new Checkbox("Minimum", group, false);
 		m_minimumCurvature.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_minimumCurvature, c);
 
 		// minimum curvature
 		m_maximumCurvature = new Checkbox("Maximum", group, false);
 		m_maximumCurvature.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_maximumCurvature, c);
 
 		m_curvatureType = CurvatureType.Mean;
 		group.setSelectedCheckbox(m_meanCurvature);
 
 		// curvature tensor
 		c.gridy++;
 		c.fill = GridBagConstraints.CENTER;
 		l = new Label("Curvature Tensor");
 		l.setFont(boldFont);
 		m_panel.add(l, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 
 		// display or hide
 		m_curvatureTensor = new Checkbox("Display", false);
 		m_curvatureTensor.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_curvatureTensor, c);
 
 		// tensor display type
 		CheckboxGroup tensorGroup = new CheckboxGroup();
 
 		// minor only
 		m_minorTensor = new Checkbox("Minor", tensorGroup, false);
 		m_minorTensor.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_minorTensor, c);
 
 		// major only
 		m_majorTensor = new Checkbox("Major", tensorGroup, false);
 		m_majorTensor.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_majorTensor, c);
 
 		// both, minor and major
 		m_bothTensor = new Checkbox("Both", tensorGroup, false);
 		m_bothTensor.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_bothTensor, c);
 
 		m_tensorType = TensorType.Both;
 		tensorGroup.setSelectedCheckbox(m_bothTensor);
 
 		// smooth tensor
 		m_smoothTensor = new Button("Smooth Tensor");
 		m_smoothTensor.setEnabled(false);
 		m_smoothTensor.addActionListener(this);
 		c.gridy++;
 		m_panel.add(m_smoothTensor, c);
 
 		// colorization options
 
 		c.gridy++;
 		c.fill = GridBagConstraints.CENTER;
 		l = new Label("Colorization");
 		l.setFont(boldFont);
 		m_panel.add(l, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		// color method choice
 		CheckboxGroup group2 = new CheckboxGroup();
 
 		// color by maximum
 		m_colorByMax = new Checkbox("Maximum", group2, false);
 		m_colorByMax.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_colorByMax, c);
 
 		// color by deviation
 		m_colorByDeviation = new Checkbox("Deviation", group2, false);
 		m_colorByDeviation.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_colorByDeviation, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 
 		// no colors (required for tensor)
 		m_noColors = new Checkbox("None", group2, false);
 		m_noColors.addItemListener(this);
 		c.gridy++;
 		m_panel.add(m_noColors, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 
 		m_colorType = ColorType.Deviation;
 		group2.setSelectedCheckbox(m_colorByDeviation);
 
 		show();
 	}
 	@Override
 	public void itemStateChanged(ItemEvent e)
 	{
 		Object source = e.getSource();
 		if (source == m_meanCurvature) {
 			m_curvatureType = CurvatureType.Mean;
 		} else if (source == m_gaussianCurvature) {
 			m_curvatureType = CurvatureType.Gaussian;
 		} else if (source == m_curvatureTensor) {
 			m_displayTensor = m_curvatureTensor.getState();
 			m_smoothTensor.setEnabled(m_displayTensor);
 		} else if (source == m_minorTensor) {
 			m_tensorType = TensorType.Minor;
 		} else if (source == m_majorTensor) {
 			m_tensorType = TensorType.Major;
 		} else if (source == m_bothTensor) {
 			m_tensorType = TensorType.Both;
 		} else if (source == m_disableCurvature) {
 			m_curvatureType = CurvatureType.Disable;
 		} else if (source == m_maximumCurvature) {
 			m_curvatureType = CurvatureType.Maximum;
 		} else if (source == m_minimumCurvature) {
 			m_curvatureType = CurvatureType.Minimum;
 		} else if (source == m_colorByDeviation) {
 			m_colorType = ColorType.Deviation;
 		} else if (source == m_colorByMax) {
 			m_colorType = ColorType.Maximum;
 		} else if (source == m_noColors) {
 			m_colorType = ColorType.NoColors;
 		} else {
 			assert false;
 		}
 		updateView();
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		Object source = e.getSource();
 		if (source == m_smoothTensor) {
 			if (currentGeometry() == null) {
 				return;
 			}
 			assert m_lastGeometry == currentGeometry();
 			assert m_lastTensorField != null;
 			smoothTensorField(m_lastGeometry, m_lastCurvature);
 		} else {
 			assert false : "unhandled action source: " + source;
 		}
 	}
 	@Override
 	public void selectGeometry(PgGeometryIf geometry)
 	{
 		updateView();
 	}
 	@Override
 	public void removeGeometry(PgGeometryIf geometry) {
 		if (geometry == m_lastGeometry) {
 			// clear cache
 			m_lastGeometry = null;
 			m_lastCurvature = null;
 			m_lastCornerTable = null;
 			m_lastTensorField = null;
 		}
 		super.removeGeometry(geometry);
 	}
 	/*
 	 * remove other geometries when opening a new one
 	 * @see ProjectBase#addGeometry(jv.project.PgGeometryIf)
 	 */
 	@Override
 	public void addGeometry(PgGeometryIf geometry) {
 		// hide other geometries
 		for(PgGeometryIf other : m_disp.getGeometries()) {
 			if (other == geometry) {
 				continue;
 			} else {
 				System.out.println("removing geometry " + other.getName());
 				m_disp.removeGeometry(other);
 			}
 		}
 	}
 	/**
 	 * @return currently selected PgElementSet or null
 	 */
 	private PgElementSet currentGeometry()
 	{
 		try {
 			return (PgElementSet) m_disp.getSelectedGeometry();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	/**
 	 * update view after settings changed, i.e. new color scheme or similar
 	 */
 	private void updateView()
 	{
 		PgElementSet geometry = currentGeometry();
 		if (geometry == null) {
 			return;
 		}
 		clearCurvature(geometry);
 		switch(m_curvatureType) {
 		case Disable:
 			// nothing to do, we always clear before
 			break;
 		case Gaussian:
 		case Mean:
 		case Minimum:
 		case Maximum:
 			setCurvatureColors(geometry, m_curvatureType, m_colorType, m_displayTensor, m_tensorType);
 			break;
 		}
 	}
 	/**
 	 * Compute tensor fields for given @param geometry, reusing previously
 	 * calculated curvature and corner table.
 	 *
 	 * @param geometry
 	 * @param curvature
 	 * @param cornerTable
 	 * @return array of four vector fields like this: {max, min, -max, -min}
 	 */
 	private PgVectorField[] getCurvatureTensorFields(PgElementSet geometry, Curvature[] curvature,
 									CornerTable cornerTable)
 	{
 		System.out.println("calculating principle curvature directions");
 
 		PgVectorField[] ret = new PgVectorField[4];
 
 		PgVectorField max = new PgVectorField(3);
 		max.setGlobalVectorColor(Color.red);
 		max.showIndividualMaterial(true);
 		max.setGlobalVectorLength(0.01);
 		max.setName("+max");
 		max.setBasedOn(PgVectorField.VERTEX_BASED);
 		max.setNumVectors(geometry.getNumVertices());
 		ret[0] = max;
 
 		PgVectorField min = new PgVectorField(3);
 		min.setGlobalVectorColor(Color.blue);
 		min.showIndividualMaterial(true);
 		min.setGlobalVectorLength(0.01);
 		min.setName("+min");
 		min.setBasedOn(PgVectorField.VERTEX_BASED);
 		min.setNumVectors(geometry.getNumVertices());
 		ret[1] = min;
 
 		Set<Integer> visitedVertices = new HashSet<Integer>(geometry.getNumVertices());
 		for (Corner corner : cornerTable.corners()) {
 			if (!visitedVertices.add(corner.vertex)) {
 				// vertex already handled
 				continue;
 			}
 			Curvature curve = curvature[corner.vertex];
 			if (curve == null) {
 				continue;
 			}
 			// now we find all neighbors and compute:
 			// \kappa_{i,j}^N (see page 13)
 			// \vec{\delta_{i,j}} (see page 14)
 			// to iterate over all neighbors we iterate over the corner table
 			// starting with c.p and then jumping to .o.p of that corner
 			// until we reach c.n and quit
 			PdVector x_i = geometry.getVertex(corner.vertex);
 			PdMatrix tangentPlane = curve.tangentPlane();
			if (tangentPlane == null) {
				System.err.println("skipping zero mean curvature at vertex: " + x_i + ", index: " + corner.vertex);
				continue;
			}
 			PdVector n = tangentPlane.getRow(0);
 			PdVector t1 = tangentPlane.getRow(1);
 			PdVector t2 = tangentPlane.getRow(2);
 			Corner j = corner.prev;
 			ArrayList<Double> kappas = new ArrayList<Double>(5);
 			ArrayList<PdVector> deltas = new ArrayList<PdVector>(5);
 			boolean usePrev = true;
 			while(true) {
 				PdVector x_j = geometry.getVertex(j.vertex);
 				// x_i - x_j
 				PdVector e = PdVector.subNew(x_i, x_j);
 				double e_dot_n = e.dot(n);
 				kappas.add(2.0d * e_dot_n / e.sqrLength());
 				// (e*n)n - e
 				PdVector delta = PdVector.blendNew(e_dot_n, n, -1.0d, e);
 				// ... / |(e*n)n-e|
 				delta.normalize();
 				// now compute coordinates in plane by dotting to t1, t2
 				deltas.add(new PdVector(t1.dot(delta), t2.dot(delta)));
 				if (j.vertex == corner.next.vertex) {
 					// we just handled the last neighbor, c.n - stop now
 					break;
 				} else {
 					if (usePrev) {
 						j = j.prev.opposite;
 						if (j == null) {
 							j = corner.next;
 							usePrev = false;
 						}
 					} else {
 						j = j.next.opposite;
 						if (j == null) {
 							break;
 						}
 					}
 				}
 			}
 			assert kappas.size() == deltas.size();
 			// prepare A x = b, we look for the solution x
 			// each row is: d1^2 2d1d2 d2^2, with d1,d2 being the coeffs of delta_{i,j}
 			Matrix A = new Matrix(kappas.size(), 3);
 			// kappas
 			Matrix b = new Matrix(kappas.size(), 1);
 			for(int i = 0; i < kappas.size(); ++i) {
 				b.set(i, 0, kappas.get(i));
 				PdVector delta = deltas.get(i);
 				double d1 = delta.getEntry(0);
 				double d2 = delta.getEntry(1);
 				A.set(i, 0, d1 * d1);
 				A.set(i, 1, 2.0d * d1 * d2);
 				A.set(i, 2, d1 * d2);
 			}
 			// x is vector of [l, m, n]
 			Matrix x;
 			try {
 				x = A.solve(b);
 			} catch(RuntimeException e) {
 				System.err.println(e.getMessage());
 				System.out.println("fallback to SVD algorithm");
 				// lets try the SVD algo here
 				Matrix AB = new Matrix(kappas.size(), 4);
 				AB.setMatrix(0, kappas.size() - 1, 0, 2, A);
 				AB.setMatrix(0, kappas.size() - 1, 3, 3, b);
 				Matrix V = AB.svd().getV();
 				assert V.getRowDimension() == 4;
 				assert V.getColumnDimension() == 4;
 				x = V.getMatrix(0, 2, 3, 3).times(-V.get(3, 3));
 			}
 			assert x.getRowDimension() == 3;
 			assert x.getColumnDimension() == 1;
 
 			// build curvature matrix
 			PdMatrix B = new PdMatrix(2, 2);
 			curve.B = B;
 			B.setEntry(0, 0, x.get(0, 0));
 			B.setEntry(0, 1, x.get(1, 0));
 			B.setEntry(1, 0, x.get(1, 0));
 			B.setEntry(1, 1, x.get(2, 0));
 			PdVector eigenValues = new PdVector(0, 0);
 			PdVector[] eigenVectors = {new PdVector(0, 0), new PdVector(0, 0)};
 			PnJacobi.computeEigenvectors(B, 2, eigenValues, eigenVectors);
 			///ZOMG! the eigenvalues are not even sorted -.-'
 			int minI, maxI;
 			if (eigenValues.getEntry(0) < eigenValues.getEntry(1)) {
 				minI = 0;
 				maxI = 1;
 			} else {
 				minI = 1;
 				maxI = 0;
 			}
 			assert eigenValues.getEntry(minI) <= eigenValues.getEntry(maxI);
 			// now scale up to 3d for display
 			PdVector bMinDir = eigenVectors[minI];
 			PdVector bMaxDir = eigenVectors[maxI];
 			PdVector minDir = PdVector.blendNew(bMinDir.getEntry(0), t1, bMinDir.getEntry(1), t2);
 			PdVector maxDir = PdVector.blendNew(bMaxDir.getEntry(0), t1, bMaxDir.getEntry(1), t2);
 			min.setVector(corner.vertex, minDir);
 			max.setVector(corner.vertex, maxDir);
 		}
 
 		PgVectorField maxNeg = (PgVectorField) max.clone();
 		maxNeg.multScalar(-1);
 		maxNeg.setName("-max");
 		ret[2] = maxNeg;
 		PgVectorField minNeg = (PgVectorField) min.clone();
 		minNeg.multScalar(-1);
 		minNeg.setName("-min");
 		ret[3] = minNeg;
 
 		return ret;
 	}
 	/**
 	 * compute curvature values of each vertex in @param geometry
 	 *
 	 * @param geometry for which to compute the curvature
 	 * @param cornerTable optional optimization: reuse existing corner table for above @param geometry
 	 * @return array of Curvature objects, one for each vertex in @param geometry
 	 */
 	private Curvature[] getCurvature(PgElementSet geometry, CornerTable cornerTable)
 	{
 		CornerTable table = (cornerTable == null) ? new CornerTable(geometry) : cornerTable;
 		CotanCache cotanCache = new CotanCache(table.size());
 		// iterate over all corners, each time adding the partial 
 		// contribution to the mixed area and mean curvature normal operator
 		// note: each corner is one summand of the sums in eq. 8 / fig 4.
 		// we take xi = corner.vertex
 		// and xj = corner.prev.vertex
 		// hence the angles are:
 		// alpha = angle(corner.next.vertex)
 		// beta = angle(corner.next.opposite.vertex)
 		// note: we must take obtuse triangles into account and
 		// can only sum parts of the voronoi cell up at each time
 		// the e.q. for that is given in sec. 3.3 on page 8
 		Curvature[] vertexMap = new Curvature[geometry.getNumVertices()];
 		// for bad geometries, like the hand
 		HashSet<Integer> blackList = new HashSet<Integer>();
 		for(Corner corner : table.corners()) {
 			Corner cno = corner.next.opposite;
 			if (cno == null) {
 				///TODO: what to do in such cases?
 				continue;
 			}
 
 			//note: alpha, beta, gamma are all in corner.triangle
 			//note: all values are apparently in degrees!
 			// alpha: angle at x_i in T, between AB and AC
 			// compare to angle(P) in paper
 			double alpha = geometry.getVertexAngle(corner.triangle, corner.localVertexIndex);
 			// beta: angle at prev corner, between AB and BC
 			// compare to angle(Q)
 			double beta = geometry.getVertexAngle(corner.triangle, corner.prev.localVertexIndex);
 			// gamma: angle at next corner, between AC and BC
 			// compare to angle(R)
 			double gamma = geometry.getVertexAngle(corner.triangle, corner.next.localVertexIndex);
 
 			if (alpha == 0 || beta == 0 || gamma == 0) {
 				System.err.println("Zero-angle encountered in triangle, skipping: " + corner.triangle);
 				blackList.add(corner.vertex);
 				blackList.add(corner.prev.vertex);
 				blackList.add(corner.next.vertex);
 				continue;
 			}
 			
 			double cotGamma = cotanCache.cotan(gamma);
 
 			// edge between A and B, angle is beta
 			// compare to PQ
 			PdVector AB = PdVector.subNew(geometry.getVertex(corner.vertex),
 											geometry.getVertex(corner.prev.vertex));
 
 			double area = -1;
 			// check for obtuse angle
 			if (alpha >= 90 || beta >= 90 || gamma >= 90) {
 				area = geometry.getAreaOfElement(corner.triangle);
 				assert area > 0;
 				// check if angle of T at x is obtuse
 				if (alpha > 90) {
 					area /= 2.0d;
 				} else {
 					area /= 4.0d;
 				}
 			} else {
 				// voronoi region of x in t:
 				// edge between A and C, angle is gamma
 				// compare to PR
 				PdVector AC = PdVector.subNew(geometry.getVertex(corner.vertex),
 												geometry.getVertex(corner.next.vertex));
 				double cotBeta = cotanCache.cotan(beta);
 				area = 1.0d/8.0d * (AB.sqrLength() * cotGamma + AC.sqrLength() * cotBeta);
 				assert area > 0;
 			}
 
 			Curvature cache = vertexMap[corner.vertex];
 			if (cache == null) {
 				cache = new Curvature();
 				vertexMap[corner.vertex] = cache;
 			}
 			// now e.q. 8, with alpha = our gamma from above, and beta = cnoAngle
 			double cnoAngle = geometry.getVertexAngle(cno.triangle, cno.localVertexIndex);
 			if (cnoAngle == 0) {
 				System.err.println("Zero-Angle encountered in triangle " + cno.triangle + ", vertex: " + corner.vertex);
 				blackList.add(corner.vertex);
 				continue;
 			}
 			double cotCnoAngle = cotanCache.cotan(cnoAngle);
 			cache.meanOp.add(cotGamma + cotCnoAngle, AB);
 			cache.gaussian += alpha;
 			cache.area += area;
 		}
 		for(int i : blackList) {
 			vertexMap[i] = null;
 		}
 		return vertexMap;
 	}
 	private double absMax(double[] l)
 	{
 		assert l.length > 0;
 		double max = l[0];
 		for(int i = 1; i < l.length; ++i) {
 			max = Math.max(max, Math.abs(l[i]));
 		}
 		return max;
 	}
 	/**
 	 * given curvature values @param curvature, set vertex colors
 	 * of @param geometry, by mapping the curvature values to the
 	 * HSV color space. This is done by normalizing the (possibly
 	 * negative values, see @hasNegative) to the range [0-1] by
 	 * first finding the absolute maximum of the curvature values,
 	 * then dividing each entry by that value.
 	 *
 	 * It turns out that this is a not-so-good color scheme,
 	 * see setColorsFromDeviation for a better alternative.
 	 *
 	 * @param geometry
 	 * @param curvature
 	 * @param hasNegative must be true if the values passed can
 	 * be negative, e.g. for gaussian or minimum curvatures
 	 */
 	private void setColorsFromMaxAbs(PgElementSet geometry, double[] curvature, boolean hasNegative)
 	{
 		assert curvature.length == geometry.getNumVertices();
 		// find maximum
 		double max = absMax(curvature);
 		assert max > 0;
 		// assign colors
 		for(int i = 0; i < curvature.length; ++i) {
 			double mean = curvature[i];
 			assert mean <= max;
 			assert mean < Double.POSITIVE_INFINITY;
 			float normalized = (float) (mean / max);
 			if (hasNegative) {
 				assert normalized >= -1;
 				assert normalized <= 1;
 				normalized = (normalized + 1.0f) / 2.0f;
 			} else {
 				assert mean >= 0;
 			}
 			assert normalized >= 0;
 			assert normalized <= 1;
 			geometry.setVertexColor(i,
 					Color.getHSBColor(normalized, 1.0f, 1.0f)
 					);
 		}
 		System.out.println("max curvature: " + max);
 	}
 	/**
 	 * Set vertex colors of @param geometry based on associated entry in @param curvature.
 	 * Again, we get colors from the HSV color space, but this time we normalize
 	 * the values into the range [0-1] by calculating the standard deviation of
 	 * the calculated curvatures. Then we interpolate the values to our desired
 	 * range by capping the value at the tripled standard deviation interval
 	 * around the mean value. Values above or below are set the hue = 0 = 1 = red.
 	 *
 	 * @param geometry
 	 * @param curvature
 	 * @param hasNegative
 	 */
 	private void setColorsFromDeviation(PgElementSet geometry, double[] curvature, boolean hasNegative)
 	{
 		assert curvature.length == geometry.getNumVertices();
 		assert curvature.length > 1;
 		double mean = 0;
 		for(double c : curvature) {
 			mean += c;
 		}
 		mean /= curvature.length;
 		// now calculate variance
 		double variance = 0;
 		for(double c : curvature) {
 			variance += Math.pow(c - mean, 2);
 		}
 		variance /= curvature.length - 1;
 		double standardDeviation = Math.sqrt(variance);
 		// now set colors based on deviation:
 		// zero deviation is hue of 0.5
 		// deviation is normalized to +- 0.5 in the tripled standard deviation interval
 		// anything higher just gets the maximum hue of 1 or 0 (both are red)
 		for(int i = 0; i < curvature.length; ++i) {
 			double c = curvature[i];
 			double deviation = c - mean;
 			float hue = ((float) deviation / (3.0f * (float) standardDeviation) + 1.0f) / 2.0f;
 			if (hue > 1) {
 				hue = 1;
 			} else if (hue < 0) {
 				hue = 0;
 			}
 			geometry.setVertexColor(i, Color.getHSBColor(hue, 1.0f, 1.0f));
 		}
 	}
 	// cache
 	private PgElementSet m_lastGeometry;
 	private Curvature[] m_lastCurvature;
 	private CornerTable m_lastCornerTable;
 	private PgVectorField[] m_lastTensorField;
 	private void setCurvatureColors(PgElementSet geometry, CurvatureType type, ColorType colorType,
 									boolean displayTensor, TensorType tensorType)
 	{
 		assert type == CurvatureType.Mean ||
 				type == CurvatureType.Gaussian ||
 				type == CurvatureType.Minimum ||
 				type == CurvatureType.Maximum;
 
 		CornerTable cornerTable;
 		Curvature[] curvature;
 		boolean wasCached = false;
 		if (m_lastGeometry == geometry) {
 			System.out.println("reusing cached curvature calculations");
 			cornerTable = m_lastCornerTable;
 			curvature = m_lastCurvature;
 			wasCached = true;
 		} else {
 			System.out.println("calculating curvature of geometry " + geometry.getName() + ", type: " + type);
 			cornerTable = new CornerTable(geometry);
 			curvature = getCurvature(geometry, cornerTable);
 			System.out.println("done");
 
 			// cache results
 			m_lastGeometry = geometry;
 			m_lastCurvature = curvature;
 			m_lastCornerTable = cornerTable;
 			// gets updated on-demand, see below
 			m_lastTensorField = null;
 		}
 		System.out.println("setting colors via type: " + colorType);
 		double values[] = new double[curvature.length];
 		double totalGaussian = 0;
 		for (int i = 0; i < curvature.length; ++i) {
 			Curvature c = curvature[i];
 			if (c == null) {
 				values[i] = 0;
 				continue;
 			}
 			assert c.area > 0;
 			if (type == CurvatureType.Mean) {
 				values[i] = c.meanCurvature();
 				assert values[i] >= 0;
 			} else if (type == CurvatureType.Minimum) {
 				values[i] = c.minimumCurvature();
 			} else if (type == CurvatureType.Maximum) {
 				values[i] = c.maximumCurvature();
 				assert values[i] >= 0;
 			} else {
 				assert type == CurvatureType.Gaussian;
 				values[i] = c.gaussianCurvature();
 			}
 			totalGaussian += Math.toRadians(c.gaussian);
 		}
 		boolean hasNegative = type == CurvatureType.Gaussian || type == CurvatureType.Minimum;
 		switch(colorType) {
 		case Deviation:
 			setColorsFromDeviation(geometry, values, hasNegative);
 			break;
 		case Maximum:
 			setColorsFromMaxAbs(geometry, values, hasNegative);
 			break;
 		case NoColors:
 			break;
 		}
 		System.out.println("total gaussian curvature: " + totalGaussian);
 		System.out.println("divided by 2pi: " + (totalGaussian / (2.0d * Math.PI)));
 
 		if (displayTensor) {
 			PgVectorField[] tensorField;
 			if (wasCached && m_lastTensorField != null) {
 				tensorField = m_lastTensorField;
 			} else {
 				tensorField = getCurvatureTensorFields(geometry, curvature, cornerTable);
 				m_lastTensorField = tensorField;
 			}
 			assert tensorField != null;
 			assert tensorField.length == 4;
 			for(int i = 0; i < 4; ++i) {
 				switch (tensorType) {
 				case Major:
 					if (i != 0 && i != 2) {
 						// skip minor
 						continue;
 					}
 					break;
 				case Minor:
 					if (i != 1 && i != 3) {
 						// skip major
 						continue;
 					}
 					break;
 				case Both:
 					// add all
 					break;
 				}
 				geometry.addVectorField(tensorField[i]);
 			}
 			geometry.setGlobalVectorLength(0.05);
 		}
 
 		System.out.println("done, updating display");
 		geometry.showElementColors(true);
 		geometry.showElementFromVertexColors(true);
 		m_disp.update(geometry);
 	}
 	private void smoothTensorField(PgElementSet geometry, Curvature[] curvature)
 	{
 		for(Curvature curve : curvature) {
 			assert curve.B != null;
 			PdMatrix tangentPlane = curve.tangentPlane();
 		}
 	}
 	private void clearCurvature(PgElementSet geometry)
 	{
 		geometry.removeElementColors();
 		geometry.removeVertexColors();
 		geometry.showElementFromVertexColors(false);
 		geometry.removeAllVectorFields();
 		m_disp.update(geometry);
 	}
 }
