 /*
 	Copyright 2011 Milian Wolff <mail@milianw.de>
 	
 	This program is free software; you can redistribute it and/or
 	modify it under the terms of the GNU General Public License as
 	published by the Free Software Foundation; either version 2 of 
 	the License, or (at your option) any later version.
 	
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 	
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.awt.Button;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.Label;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JComboBox;
 
 import jv.geom.PgElementSet;
 import jv.geom.PgPolygonSet;
 import jv.number.PuDouble;
 import jv.number.PuInteger;
 import jv.project.PgGeometryIf;
 import jv.project.PvCameraEvent;
 import jv.project.PvCameraListenerIf;
 import jv.project.PvDisplayIf;
 import jv.project.PvGeometryListenerIf;
 import jv.project.PvLightIf;
 import jv.vecmath.PdMatrix;
 import jv.vecmath.PdVector;
 import jv.vecmath.PiVector;
 
 
 /**
  * Solution to fourth exercise of the second project
  * 
  * @author		Milian Wolff
  * @version		26.11.2011, 1.00 created
  */
 public class Ex2_4 extends ProjectBase implements PvGeometryListenerIf, ItemListener,
 													ActionListener, PvCameraListenerIf
 {
 	public static void main(String[] args)
 	{
 		new Ex2_4(args);
 	}
 	private Button m_render;
 	private JComboBox m_methodCombo;
 	private Method m_method;
 	private enum Method {
 		MethodA,
 		MethodB
 	}
 	private JComboBox m_tensor;
 	private TensorType m_tensorType;
 	private enum TensorType {
 		Minor,
 		Major
 	}
 	private Button m_smoothTensor;
 	private Button m_resetTensor;
 	private PuInteger m_smoothSteps;
 	private PuDouble m_smoothStepSize;
 	private JComboBox m_weighting;
 	private Curvature.WeightingType m_weightingType;
 	private JComboBox m_smoothing;
 	private Curvature.SmoothingScheme m_smoothingScheme;
 	private Curvature m_lastCurvature;
 	private boolean m_rendering;
 	private DisplayImage m_img;
 	private PgPolygonSet m_lastMajor;
 	private PgPolygonSet m_lastMinor;
 	private PuInteger m_brightnessThreshold;
 	private PuInteger m_darknessThreshold;
 	public Ex2_4(String[] args)
 	{
 		super(args, "SciVis - Project 2 - Exercise 4 - Milian Wolff");
 
 		Font boldFont = new Font("Dialog", Font.BOLD, 12);
 
 		m_rendering = false;
 
 		m_img = new DisplayImage();
 
 		// listener
 		m_disp.addGeometryListener(this);
 		m_disp.addCameraListener(this);
 
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridwidth = 2;
 		c.gridx = 0;
 		c.gridy = 0;
 
 		// reset/recalculate tensor
 		m_render = new Button("Render");
 		m_render.addActionListener(this);
 		m_panel.add(m_render, c);
 
 		c.gridy++;
 		c.fill = GridBagConstraints.CENTER;
 		Label l = new Label("Streamlines");
 		l.setFont(boldFont);
 		m_panel.add(l, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 
 		// curvature method choice
 		c.gridy++;
 		c.gridwidth = 1;
 		m_panel.add(new Label("Method:"), c);
 		c.gridx = 1;
 		m_methodCombo = new JComboBox();
 		m_methodCombo.addItem(Method.MethodA);
 		m_methodCombo.addItem(Method.MethodB);
 		m_method = Method.MethodA;
 		m_methodCombo.setSelectedItem(m_method);
 		m_methodCombo.addItemListener(this);
 		m_panel.add(m_methodCombo, c);
 		c.gridwidth = 2;
 		c.gridx = 0;
 
 		// curvature direction choice
 		c.gridy++;
 		c.gridwidth = 1;
 		m_panel.add(new Label("Direction:"), c);
 		c.gridx = 1;
 		m_tensor = new JComboBox();
 		m_tensor.addItem(TensorType.Minor);
 		m_tensor.addItem(TensorType.Major);
 		m_tensorType = TensorType.Minor;
 		m_tensor.setSelectedItem(m_tensorType);
 		m_tensor.addItemListener(this);
 		m_panel.add(m_tensor, c);
 		c.gridwidth = 2;
 		c.gridx = 0;
 
 		// smoothening
 		c.gridy++;
 		c.fill = GridBagConstraints.CENTER;
 		l = new Label("Smoothening");
 		l.setFont(boldFont);
 		m_panel.add(l, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 
 		// number of steps for smoothing
 		m_smoothSteps = new PuInteger("Steps");
 		m_smoothSteps.init();
 		m_smoothSteps.setBounds(1, 1000);
 		m_smoothSteps.setValue(10);
 		c.gridy++;
 		m_panel.add(m_smoothSteps.getInfoPanel(), c);
 
 		// step size (\Delta t)
 		m_smoothStepSize = new PuDouble("Step size");
 		m_smoothStepSize.init();
 		m_smoothStepSize.setValue(1);
 		m_smoothStepSize.setBounds(0, 10);
 		c.gridy++;
 		m_panel.add(m_smoothStepSize.getInfoPanel(), c);
 
 		// weighting
 		c.gridy++;
 		c.gridwidth = 1;
 		m_panel.add(new Label("Weighting:"), c);
 		c.gridx = 1;
 		m_weighting = new JComboBox();
 		m_weighting.addItemListener(this);
 		m_weighting.addItem(Curvature.WeightingType.Uniform);
 		m_weighting.addItem(Curvature.WeightingType.Cord);
 		m_weighting.addItem(Curvature.WeightingType.Cotangent);
 		m_weighting.addItem(Curvature.WeightingType.MeanValue);
 		m_weightingType = Curvature.WeightingType.Uniform;
 		m_weighting.setSelectedItem(m_weightingType);
 		m_panel.add(m_weighting, c);
 		c.gridx = 0;
 		c.gridwidth = 2;
 
 		// method
 		c.gridy++;
 		c.gridwidth = 1;
 		m_panel.add(new Label("Scheme:"), c);
 		c.gridx = 1;
 		m_smoothing = new JComboBox();
 		m_smoothing.addItemListener(this);
 		m_smoothing.addItem(Curvature.SmoothingScheme.ForwardEuler);
 		m_smoothing.addItem(Curvature.SmoothingScheme.GaussSeidel);
 		m_smoothingScheme = Curvature.SmoothingScheme.GaussSeidel;
 		m_smoothing.setSelectedItem(m_smoothingScheme);
 		m_panel.add(m_smoothing, c);
 		c.gridx = 0;
 		c.gridwidth = 2;
 
 		// smooth tensor
 		c.gridy++;
 		c.gridwidth = 1;
 		c.gridx = 0;
 		c.fill = GridBagConstraints.CENTER;
 		m_smoothTensor = new Button("Smooth Tensor");
 		m_smoothTensor.addActionListener(this);
 		m_panel.add(m_smoothTensor, c);
 
 		// reset/recalculate tensor
 		m_resetTensor = new Button("Reset Tensor");
 		m_resetTensor.addActionListener(this);
 		c.gridx = 1;
 		m_panel.add(m_resetTensor, c);
 		c.gridx = 0;
 		c.gridwidth = 2;
 		c.fill = GridBagConstraints.HORIZONTAL;
 
 		// brightness threshold
 		m_brightnessThreshold = new PuInteger("Bright Above");
 		m_brightnessThreshold.init();
 		m_brightnessThreshold.setValue(230);
 		m_brightnessThreshold.setBounds(0, 255);
 		c.gridy++;
 		m_panel.add(m_brightnessThreshold.getInfoPanel(), c);
 
 		// darkness threshold
 		m_darknessThreshold = new PuInteger("Dark Below");
 		m_darknessThreshold.init();
		m_darknessThreshold.setValue(180);
 		m_darknessThreshold.setBounds(0, 255);
 		c.gridy++;
 		m_panel.add(m_darknessThreshold.getInfoPanel(), c);
 
 		show();
 		m_frame.setBounds(new Rectangle(420, 5, 1024, 550));
 	}
 	@Override
 	public void itemStateChanged(ItemEvent e)
 	{
 		Object source = e.getSource();
 		if (source == m_tensor) {
 			m_tensorType = (TensorType) m_tensor.getSelectedItem();
 		} else if (source == m_weighting) {
 			m_weightingType = (Curvature.WeightingType) m_weighting.getSelectedItem();
 			return;
 		} else if (source == m_smoothing) {
 			m_smoothingScheme = (Curvature.SmoothingScheme) m_smoothing.getSelectedItem();
 		} else if (source == m_methodCombo) {
 			m_method = (Method) m_methodCombo.getSelectedItem();
 		} else {
 			assert false : source;
 		}
 		updateView();
 	}
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		Object source = e.getSource();
 		if (source == m_smoothTensor) {
 			if (m_lastCurvature == null) {
 				return;
 			}
 			m_lastCurvature.smoothTensorField(m_smoothSteps.getValue(), m_smoothStepSize.getValue(),
 												m_weightingType, m_smoothingScheme);
 			m_lastMajor = getTrace(m_lastCurvature, TensorType.Major);
 			m_lastMinor = getTrace(m_lastCurvature, TensorType.Minor);
 			updateView();
 		} else if (source == m_resetTensor) {
 			if (m_lastCurvature == null) {
 				return;
 			}
 			m_lastCurvature.computeCurvatureTensor();
 			m_lastMajor = getTrace(m_lastCurvature, TensorType.Major);
 			m_lastMinor = getTrace(m_lastCurvature, TensorType.Minor);
 			updateView();
 		} else if (source == m_render) {
 			updateView();
 		} else {
 			assert false : "unhandled action source: " + source;
 		}
 	}
 	/*
 	 * remove other geometries when opening a new one
 	 * @see ProjectBase#addGeometry(jv.project.PgGeometryIf)
 	 */
 	@Override
 	public void addGeometry(PgGeometryIf geometry) {
 		if (m_rendering) {
 			return;
 		}
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
 	@Override
 	public void selectGeometry(PgGeometryIf geometry) {
 		updateView();
 	}
 	@Override
 	public void dragCamera(PvCameraEvent event) {
 //		updateView();
 	}
 	@Override
 	public void pickCamera(PvCameraEvent event) {
 //		updateView();
 	}
 	protected BufferedImage renderOffscreen()
 	{
 		assert m_rendering;
 		assert m_disp.isEnabledExternalRendering();
 		assert m_disp.getCanvas().getGraphics() != null;
 		m_disp.update(m_disp.getCanvas().getGraphics());
 		BufferedImage ret = createImage();
 		Graphics2D gfx = ret.createGraphics();
 		m_disp.render();
 		gfx.drawImage(m_disp.getImage(), 0, 0, Color.white, null);
 		return ret;
 	}
 	private BufferedImage createImage()
 	{
 		return new BufferedImage(m_disp.getCanvas().getWidth(),
 								 m_disp.getCanvas().getHeight(),
 								 BufferedImage.TYPE_INT_RGB);
 	}
 	private BufferedImage getSilhouette(PgElementSet geometry)
 	{
 		// always re-compute silhouette, position might have changed
 		// TODO: cache when pos has _not_ changed?
 		PgPolygonSet silhouette = Silhouette.createVertexBasedSilhouette(geometry,
 											m_disp.getCamera().getPosition());
 
 		m_disp.addGeometry(silhouette);
 		m_disp.update(silhouette);
 
 		int oldLightningModel = m_disp.getLightingModel();
 		m_disp.setLightingModel(PvLightIf.MODEL_SURFACE);
 		assert m_disp.containsGeometry(geometry);
 
 		BufferedImage image = renderOffscreen();
 
 		m_disp.removeGeometry(silhouette);
 		m_disp.update(silhouette);
 
 		m_disp.setLightingModel(oldLightningModel);
 		
 		return image;
 	}
 	private BufferedImage getGrayScale(PgElementSet geometry)
 	{
 		m_disp.update(geometry);
 		return renderOffscreen();
 	}
 	private BufferedImage getTraceImage(PgPolygonSet trace)
 	{
 		m_disp.addGeometry(trace);
 		m_disp.update(trace);
 
 		int oldLightningModel = m_disp.getLightingModel();
 		m_disp.setLightingModel(PvLightIf.MODEL_SURFACE);
 
 		BufferedImage image = renderOffscreen();
 
 		m_disp.setLightingModel(oldLightningModel);
 		
 		m_disp.removeGeometry(trace);
 		m_disp.update(trace);
 
 		return image;
 	}
 	private PgPolygonSet getTrace(Curvature curvature, TensorType direction)
 	{
 		PgPolygonSet ret = new PgPolygonSet();
 		PgElementSet geometry = curvature.geometry();
 		ret.setName("trace of " + geometry.getName() + ", dir: " + direction);
 		Curvature.VertexCurvature[] curvatures = curvature.curvatures();
 		geometry.assureElementNormals();
 		for(int i = 0; i < geometry.getNumElements(); ++i) {
 			PiVector vertices = geometry.getElement(i);
 			assert vertices.getSize() == 3;
 			// average global curvature tensors of vertices
 			PdMatrix avg = new PdMatrix(3, 3);
 			for(int j : vertices.getEntries()) {
 				Curvature.VertexCurvature curve = curvatures[j];
 				if (curve == null) {
 					continue;
 				}
 				avg.add(curve.globalCurvature());
 			}
 			avg.multScalar(1.0d/vertices.getSize());
 			// create 2x2 curvature for average tensor at center
 			PdVector normal = geometry.getElementNormal(i);
 			// find triangle-plane vectors
 			PdVector x = PdVector.normalToVectorNew(normal);
 			PdVector y = PdVector.crossNew(normal, x);
 			assert Math.abs(normal.length() - 1) < 1E-10 : normal.length();
 			assert Math.abs(x.length() - 1) < 1E-10 : x.length();
 			assert Math.abs(y.length() - 1) < 1E-10 : y.length();
 			PdMatrix avgLocal = Curvature.toLocalTensor(avg, x, y);
 			// get principle directions of averaged tensor
 			PdMatrix dirs = Curvature.principleDirections2x2(avgLocal);
 			PdVector e1_local = dirs.getRow(direction == TensorType.Minor ? 1 : 0);
 			assert Math.abs(e1_local.length() - 1) < 1E-10 : e1_local.length();
 			// find nearest intersections
 			PdVector x_c = geometry.getCenterOfElement(null, i);
 			PdVector x_c_local = Curvature.toLocalVector(x_c, x, y);
 			// coefficients to e1_local for intersections
 			double alphas[] = new double[3];
 			for(int j = 0; j < 3; ++j) {
 				int a = vertices.getEntry(j);
 				int b = vertices.getEntry(j == 2 ? 0 : j+1);
 				// x_a + a*(x_a - x_b) = x_c + \alpha * e_1
 				PdVector x_a_local = Curvature.toLocalVector(geometry.getVertex(a), x, y);
 				assert x_a_local.getSize() == 2;
 				PdVector x_b_local = Curvature.toLocalVector(geometry.getVertex(b), x, y);
 				assert x_b_local.getSize() == 2;
 				PdMatrix coeffs = new PdMatrix(2, 2);
 				coeffs.setColumn(0, PdVector.subNew(x_a_local, x_b_local));
 				coeffs.setColumn(1, e1_local);
 				PdVector x_a_sub_c = PdVector.subNew(x_a_local, x_c_local);
 				assert x_a_sub_c.getSize() == 2;
 				PdVector solution = Curvature.solveCramer(coeffs, x_a_sub_c);
 				double alpha = solution.getEntry(1);
 				alphas[j] = alpha;
 			}
 			// sort by abs
 			if (Math.abs(alphas[0]) > Math.abs(alphas[1])) {
 				double tmp = alphas[1];
 				alphas[1] = alphas[0];
 				alphas[0] = tmp;
 			}
 			if (Math.abs(alphas[1]) > Math.abs(alphas[2])) {
 				double tmp = alphas[2];
 				alphas[2] = alphas[1];
 				alphas[1] = tmp;
 			}
 			if (Math.abs(alphas[0]) > Math.abs(alphas[1])) {
 				double tmp = alphas[1];
 				alphas[1] = alphas[0];
 				alphas[0] = tmp;
 			}
 			assert Math.abs(alphas[0]) <= Math.abs(alphas[1]);
 			assert Math.abs(alphas[1]) <= Math.abs(alphas[2]);
 
 			// calculate intersection points
 			PdVector e1 = Curvature.toGlobalVector(e1_local, x, y);
 			PdVector i1 = PdVector.blendNew(1.0d, x_c, alphas[0], e1);
 			PdVector i2 = PdVector.blendNew(1.0d, x_c, alphas[1], e1);
 
 			int v1 = ret.addVertex(i1);
 			int v2 = ret.addVertex(i2);
 			ret.addPolygon(new PiVector(v1, v2));
 		}
 		ret.showVertices(false);
 		ret.setGlobalPolygonSize(1.0);
 		return ret;
 	}
 	private int grayScale(int rgb)
 	{
 		Color c = new Color(rgb);
 		return (int) Math.round(0.212671f * c.getRed() + 0.715160f * c.getGreen()
 								+ 0.072169f * c.getBlue());
 	}
 	private void updateView()
 	{
 		PgElementSet geometry = currentGeometry();
 		if (geometry == null || m_rendering) {
 			return;
 		}
 
 		assert !m_rendering;
 		m_rendering = true;
 
 		System.out.println("updating view");
 		if (m_lastCurvature == null || m_lastCurvature.geometry() != geometry) {
 			m_lastCurvature = new Curvature(geometry);
 			m_lastCurvature.computeCurvatureTensor();
 			m_lastCurvature.smoothTensorField(m_smoothSteps.getValue(), m_smoothStepSize.getValue(),
 												m_weightingType, m_smoothingScheme);
 			m_lastMajor = getTrace(m_lastCurvature, TensorType.Major);
 			m_lastMinor = getTrace(m_lastCurvature, TensorType.Minor);
 		}
 
 		m_disp.setExternalRenderSize(m_disp.getSize().width, m_disp.getSize().height);
 		m_disp.setEnabledExternalRendering(true);
 
 		// disable lighting / unwanted settings that might temper with colors
 		boolean wasShowingVertices = geometry.isShowingVertices();
 		geometry.showVertices(false);
 		boolean wasShowingEdges = geometry.isShowingEdges();
 		geometry.showEdges(false);
 		boolean wasShowingElementColors = geometry.isShowingElementColors();
 		geometry.showElementColors(false);
 		Color oldElementColor = geometry.getGlobalElementColor();
 		geometry.setGlobalElementColor(Color.white);
 
 		m_disp.update(geometry);
 		Color oldBackgroundColor = m_disp.getBackgroundColor();
 		m_disp.setBackgroundColor(Color.WHITE);
 		boolean wasShowingBorder = m_disp.hasPaintTag(PvDisplayIf.PAINT_BORDER);
 		m_disp.setPaintTag(PvDisplayIf.PAINT_BORDER, false);
 		final long PAINT_FOCUS = 536870912;
 		boolean wasShowingFocus	= m_disp.hasPaintTag(PAINT_FOCUS);
 		m_disp.setPaintTag(PAINT_FOCUS, false);
		boolean hadAntiAlias = m_disp.isEnabledAntiAlias();
		m_disp.setEnabledAntiAlias(false);
 
 		BufferedImage grayScale = getGrayScale(geometry);
 		BufferedImage silhouette = getSilhouette(geometry);
 		BufferedImage minor = getTraceImage(m_lastMinor);
 		BufferedImage major = getTraceImage(m_lastMajor);
 
 		//composite image
 		BufferedImage compositedImage = createImage();
 
 		final int white = Color.white.getRGB();
 		final int black = Color.black.getRGB();
 		for(int w = 0; w < compositedImage.getWidth(); ++w) {
 			for(int h = 0; h < compositedImage.getHeight(); ++h) {
 				// note: don't compare exactly to black, due to anti-aliasing
 				if (grayScale(silhouette.getRGB(w, h)) < 20) {
 					// always paint silhouette
 					compositedImage.setRGB(w, h, black);
 					continue;
 				}
				boolean isMajor = major.getRGB(w, h) == black;
				boolean isMinor = minor.getRGB(w, h) == black;
 				int grayness = grayScale(grayScale.getRGB(w, h));
 				boolean isBlack = false;
 				if (grayness > m_brightnessThreshold.getValue()) {
 					// bright, paint white
 					isBlack = false;
 				} else if (grayness > m_darknessThreshold.getValue()) {
 					// gray, paint selected
 					isBlack = m_tensorType == TensorType.Major ? isMajor : isMinor;
 				} else {
 					// dark, paint both
 					isBlack = isMajor || isMinor;
 				}
 				compositedImage.setRGB(w, h, isBlack ? black : white);
 			}
 		}
 
 		m_img.setImage(compositedImage);
 
 		// Restore stuff with border, do it after image has been used
 		m_disp.setEnabledExternalRendering(false);
 
 		m_disp.setPaintTag(PAINT_FOCUS, wasShowingFocus);
 		m_disp.setPaintTag(PvDisplayIf.PAINT_BORDER, wasShowingBorder);
 		m_disp.setBackgroundColor(oldBackgroundColor);
		m_disp.setEnabledAntiAlias(hadAntiAlias);
 
 		geometry.showVertices(wasShowingVertices);
 		geometry.showEdges(wasShowingEdges);
 		geometry.showElementColors(wasShowingElementColors);
 		geometry.setGlobalElementColor(oldElementColor);
 
 		m_disp.update(geometry);
 		m_rendering = false;
 	}
 }
