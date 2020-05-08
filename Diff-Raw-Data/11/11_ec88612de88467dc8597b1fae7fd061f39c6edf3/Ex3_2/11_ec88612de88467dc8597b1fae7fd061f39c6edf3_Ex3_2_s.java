 /*
 	Copyright 2012 Milian Wolff <mail@milianw.de>
 	
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
 import java.awt.Checkbox;
 import java.awt.CheckboxGroup;
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.Box;
 import javax.swing.Timer;
 
 import jv.geom.PgPointSet;
 import jv.geom.PgPolygonSet;
 import jv.geom.PgVectorField;
 import jv.number.PuDouble;
 import jv.object.PsUpdateIf;
 import jv.project.PgGeometryIf;
 import jv.project.PvCameraIf;
 import jv.project.PvDisplayIf;
 import jv.project.PvGeometryListenerIf;
 import jv.project.PvPickEvent;
 import jv.project.PvPickListenerIf;
 import jv.vecmath.PdMatrix;
 import jv.vecmath.PdVector;
 import jvx.surface.PgDomain;
 import jvx.surface.PgDomainDescr;
 import jvx.vector.PwLIC;
 
 /**
  * Solution to fourth exercise of the second project
  * 
  * @author		Milian Wolff
  * @version		10.01.2012, 1.00 created
  */
 public class Ex3_2
 	extends ProjectBase
 	implements PvGeometryListenerIf, PsUpdateIf, PvPickListenerIf, ItemListener, ActionListener
 {
 	private PwLIC m_lic;
 	private PgDomain m_domain;
 	private PgVectorField m_vec;
 	private VectorField m_field;
 	private VectorFieldPanel m_singularityPanel;
 	private Checkbox m_add;
 	private Checkbox m_remove;
 	private Checkbox m_select;
 	private PuDouble m_flowRotate;
 	private Checkbox m_flowReflect;
 	private Timer m_timer;
 	private PgPointSet m_singularities;
 	private PgPolygonSet m_majorSeparatrices;
 	private Button m_clear;
 	private PgPolygonSet m_minorSeparatrices;
 	private Checkbox m_showSeparatrices;
 
 	public static void main(String[] args)
 	{
 		new Ex3_2(args);
 	}
 
 	public Ex3_2(String[] args)
 	{
 		super(args, "SciVis - Project 3 - Exercise 2 - Milian Wolff");
 
 		m_timer = new Timer(100, this);
 
 		m_disp.setMajorMode(PvDisplayIf.MODE_INITIAL_PICK);
 
 		m_field = new VectorField();
 		m_field.setParent(this);
 		m_disp.addGeometry(m_field.termBasePoints());
 
 		// listener
 		m_disp.addGeometryListener(this);
 
 		m_domain = new PgDomain(2);
 		m_domain.setName("Domain for Vector Field");
 		m_domain.setDimOfElements(3);
 		m_domain.showVectorFields(false);
 		m_domain.showEdges(false);
 		
 		PgDomainDescr descr = m_domain.getDescr();
 		descr.setSize( -10., -10., 10., 10.);
 		descr.setDiscr(10, 10);
 		m_domain.compute();
 		
 		m_vec = new PgVectorField(2);
 		m_vec.setName("Vector Field");
 		m_vec.setBasedOn(PgVectorField.VERTEX_BASED);
 		m_vec.setNumVectors(m_domain.getNumVertices());
 		m_vec.setGeometry(m_domain);
 		m_vec.setGlobalVectorColor(Color.BLACK);
 		m_domain.addVectorField(m_vec);
 		
 		m_lic = new PwLIC();
 		m_lic.setGeometry(m_domain);
 		m_lic.setStandalone(false);
 		m_lic.setFast(true);
 		m_lic.setLICSize(50);
 		m_lic.setParent(this);
 
 		m_disp.selectCamera(PvCameraIf.CAMERA_ORTHO_XY);
 		m_disp.addGeometry(m_domain);
 		m_disp.update(m_domain);
 
 		m_disp.addPickListener(this);
 		m_disp.fit();
 
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridwidth = 3;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weighty = 0;
 
 		c.fill = GridBagConstraints.CENTER;
 		m_panel.add(boldLabel("Flow Direction"), c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridy++;
 
 		m_flowReflect = new Checkbox("Reflect Flow", false);
 		m_flowReflect.addItemListener(this);
 		m_panel.add(m_flowReflect, c);
 		c.gridy++;
 
 		m_flowRotate = new PuDouble("Rotate Flow", this);
 		m_flowRotate.setBounds(0, 360);
 		m_panel.add(m_flowRotate.getInfoPanel(), c);
 		c.gridy++;
 
 		c.fill = GridBagConstraints.CENTER;
 		m_panel.add(boldLabel("Action"), c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridy++;
 
 		CheckboxGroup group = new CheckboxGroup();
 		m_add = new Checkbox("Add", group, true);
 		m_add.addItemListener(this);
 		m_panel.add(m_add, c);
 		c.gridy++;
 
 		m_remove = new Checkbox("Remove", group, false);
 		m_remove.addItemListener(this);
 		m_panel.add(m_remove, c);
 		c.gridy++;
 
 		m_select = new Checkbox("Select", group, false);
 		m_select.addItemListener(this);
 		m_panel.add(m_select, c);
 		c.gridy++;
 
 		m_clear = new Button("Clear");
 		m_clear.addActionListener(this);
 		m_panel.add(m_clear, c);
 		c.gridy++;
 
 		c.fill = GridBagConstraints.CENTER;
 		m_panel.add(boldLabel("Separatrices"), c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridy++;
 
 		m_showSeparatrices = new Checkbox("Show Separatrices", true);
 		m_showSeparatrices.addItemListener(this);
 		m_panel.add(m_showSeparatrices, c);
 		c.gridy++;
 
 		c.fill = GridBagConstraints.CENTER;
 		m_panel.add(boldLabel("Singularity"), c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridy++;
 
 		m_singularityPanel = new VectorFieldPanel();
 		m_panel.add(m_singularityPanel, c);
 		c.gridy++;
 
 		c.weighty = 1;
 		m_panel.add(Box.createVerticalBox(), c);
 		m_disp.selectGeometry(m_field.termBasePoints());
 		m_disp.fit();
 
 		updateVectorField_internal();
 
 		m_frame.setSize(1000, 800);
 		m_frame.setVisible(true);
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		Object source = e.getSource();
 		if (source == m_add) {
 			m_singularityPanel.setTypeChangeEnabled(true);
 			m_disp.setMajorMode(PvDisplayIf.MODE_INITIAL_PICK);
 			System.out.println("click into the display to add a feature");
 		} else if (source == m_remove) {
 			m_singularityPanel.setTypeChangeEnabled(false);
 			m_disp.setMajorMode(PvDisplayIf.MODE_PICK);
 			System.out.println("click near a feature to remove it");
 		} else if (source == m_select) {
 			m_singularityPanel.setTypeChangeEnabled(false);
 			m_disp.setMajorMode(PvDisplayIf.MODE_PICK);
 			System.out.println("click near a feature to select it");
 		} else if (source == m_flowReflect) {
 			updateVectorField();
 		} else if (source == m_showSeparatrices) {
 			if (m_showSeparatrices.getState()) {
 				m_disp.addGeometry(m_majorSeparatrices);
 				m_disp.addGeometry(m_minorSeparatrices);
 			} else {
 				m_disp.removeGeometry(m_majorSeparatrices);
 				m_disp.removeGeometry(m_minorSeparatrices);
 			}
 			m_disp.update(m_majorSeparatrices);
 			m_disp.update(m_minorSeparatrices);
 		} else {
 			assert false : "Unhandled item changed: " + source;
 		}
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		Object source = e.getSource();
 		if (source == m_clear) {
 			m_field.clear();
 			updateVectorField();
 		} else {
 			assert e.getSource() == m_timer;
 			m_timer.stop();
 			if (!m_lic.isComputingLIC()) {
 				updateVectorField_internal();
 			} else {
 				m_lic.stopLIC();
 				updateVectorField();
 			}
 		}
 	}
 	/**
 	 * delay actual recomputation
 	 */
 	public void updateVectorField() {
 		m_timer.restart();
 	}
 	/**
 	 * (Re)Compute vector field.
 	 */
 	public void updateVectorField_internal() {
 		PdMatrix A = new PdMatrix(2, 2);
 		double theta = Math.toRadians(m_flowRotate.getValue());
 		A.setEntry(0, 0, Math.cos(theta));
 		A.setEntry(0, 1, -Math.sin(theta));
 		A.setEntry(1, 0, Math.sin(theta));
 		A.setEntry(1, 1, Math.cos(theta));
 		if (m_flowReflect.getState()) {
 			A.setEntry(0, 1, A.getEntry(0, 1) * -1);
 			A.setEntry(1, 1, A.getEntry(1, 1) * -1);
 		}
 
 		for(int i = 0; i < m_domain.getNumVertices(); ++i) {
 			PdVector pos = m_domain.getVertex(i);
 			PdVector vec = m_field.evaluate(pos);
 			vec.leftMultMatrix(A);
 			m_vec.setVector(i, vec);
 			assert m_vec.getVector(i).getSize() == 2 : m_vec.getVector(i).getSize();
 		}
 		m_vec.update(m_vec);
 		m_lic.startLIC();
 		assert m_vec != null;
 		assert m_domain != null;
 		InterpolatedField field = new InterpolatedField(m_domain, m_vec);
 
 		if (m_singularities != null) {
 			m_disp.removeGeometry(m_singularities);
 		}
 
 		m_singularities = new PgPointSet(2);
 		m_singularities.setName("Calculated Singularities");
 		m_singularities.showVertices(true);
 		m_singularities.setGlobalVertexSize(3.0);
 		m_singularities.showVertexColors(true);
 
 		if (m_majorSeparatrices != null && m_disp.containsGeometry(m_majorSeparatrices)) {
 			m_disp.removeGeometry(m_majorSeparatrices);
 		}
 		m_majorSeparatrices = new PgPolygonSet(2);
 		m_majorSeparatrices.setName("Major Separatrices");
 		m_majorSeparatrices.showVertices(true);
 		m_majorSeparatrices.setGlobalVertexColor(Color.cyan);
 		m_majorSeparatrices.setGlobalPolygonColor(Color.cyan);
 
 		if (m_minorSeparatrices != null && m_disp.containsGeometry(m_minorSeparatrices)) {
 			m_disp.removeGeometry(m_minorSeparatrices);
 		}
 		m_minorSeparatrices = new PgPolygonSet(2);
 		m_minorSeparatrices.setName("Minor Separatrices");
 		m_minorSeparatrices.showVertices(true);
 		m_minorSeparatrices.setGlobalVertexColor(Color.orange);
 		m_minorSeparatrices.setGlobalPolygonColor(Color.orange);
 
 		LineTracer trace = new ClassicalRungeKuttaTracer(field);
 
 		int i = 0;
 		for(Singularity singularity : field.findSingularities()) {
 			m_singularities.addVertex(singularity.position);
 			Color c = null;
 			switch(singularity.type) {
 			case Saddle:
 				c = Color.blue;
 				// trace separatrices
 				final double stepSize = 0.5;
 				// small offset so it's not directly at the singularity
				final double initialOffset = 0.01;
 
 				for(int minor = 0; minor < 2; ++minor) {
 				for(int down = 0; down < 2; ++down) {
 						PdVector y0 = PdVector.blendNew(1, singularity.position,
														initialOffset * (down == 1 ? -1 : +1),
 														singularity.eigenVectors.getRow(minor));
 						trace.trace(minor == 1 ? m_minorSeparatrices : m_majorSeparatrices,
									y0, 1000, stepSize * (minor == 1 ? -1 : +1));
 					}
 				}
 				break;
 			case Sink:
 				c = Color.red;
 				break;
 			case Source:
 				c = Color.green;
 				break;
 			}
 			m_singularities.setVertexColor(i, c);
 			++i;
 		}
 		assert m_singularities.getNumVertices() == field.findSingularities().size();
 		System.out.println("singularities found: " + m_singularities.getNumVertices());
 		m_disp.addGeometry(m_singularities);
 		if (m_showSeparatrices.getState()) {
 			m_disp.addGeometry(m_majorSeparatrices);
 			m_disp.addGeometry(m_minorSeparatrices);
 		}
 		m_disp.selectGeometry(m_field.termBasePoints());
 	}
 
 	@Override
 	public boolean update(Object event) {
 		if (event == m_lic) {
 			m_disp.update(m_domain);
 			return true;
 		} else if (event == m_field) {
 			updateVectorField();
 			return true;
 		} else if (event == m_flowRotate) {
 			updateVectorField();
 			return true;
 		}
 		return false;
 	}
 	@Override
 	public PsUpdateIf getFather() {
 		return null;
 	}
 	@Override
 	public void setParent(PsUpdateIf parent) {
 		// TODO Auto-generated method stub
 		System.err.println("set parent: " + parent);
 	}
 
 	@Override
 	public void dragDisplay(PvPickEvent pos) {
 		// ignore this
 	}
 
 	@Override
 	public void dragInitial(PvPickEvent pos) {
 		// ignored
 	}
 
 	@Override
 	public void dragVertex(PgGeometryIf geom, int index, PdVector vertex) {
 		// ignored
 		if (geom == m_field.termBasePoints()) {
 			m_field.getTerm(index).setBase(geom.getVertex(index));
 		}
 	}
 
 	@Override
 	public void markVertices(PvPickEvent pos) {
 		// ignored
 	}
 
 	@Override
 	public void pickDisplay(PvPickEvent pos) {
 		// ignored
 	}
 
 	@Override
 	public void pickInitial(PvPickEvent pos) {
 		Term term = m_singularityPanel.createTerm(pos.getVertex());
 		m_field.addTerm(term);
 		m_singularityPanel.setTerm(term);
 	}
 
 	@Override
 	public void pickVertex(PgGeometryIf geom, int index, PdVector vertex) {
 		if (geom == m_field.termBasePoints()) {
 			if (m_remove.getState()) {
 				m_field.removeTerm(index);
 				m_singularityPanel.setTerm(null);
 			} else {
 				assert m_select.getState();
 				m_singularityPanel.setTerm(m_field.getTerm(index));
 			}
 		}
 	}
 
 	@Override
 	public void unmarkVertices(PvPickEvent pos) {
 		// ignored
 	}
 }
