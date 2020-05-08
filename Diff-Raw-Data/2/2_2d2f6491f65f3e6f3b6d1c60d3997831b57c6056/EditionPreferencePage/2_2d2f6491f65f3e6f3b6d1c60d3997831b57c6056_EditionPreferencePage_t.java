 /* gvSIG. Sistema de Informacin Geogrfica de la Generalitat Valenciana
  *
  * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *  Generalitat Valenciana
  *   Conselleria d'Infraestructures i Transport
  *   Av. Blasco Ibez, 50
  *   46010 VALENCIA
  *   SPAIN
  *
  *      +34 963862235
  *   gvsig@gva.es
  *      www.gvsig.gva.es
  *
  *    or
  *
  *   IVER T.I. S.A
  *   Salamanca 50
  *   46005 Valencia
  *   Spain
  *
  *   +34 963163400
  *   dac@iver.es
  */
 package com.iver.cit.gvsig.gui.preferences;
 
 import java.awt.BorderLayout;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableModel;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.preferences.AbstractPreferencePage;
 import com.iver.andami.preferences.StoreException;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.EditionManager;
 import com.iver.cit.gvsig.fmap.MapContext;
 import com.iver.cit.gvsig.fmap.MapControl;
 import com.iver.cit.gvsig.fmap.layers.FLayer;
 import com.iver.cit.gvsig.fmap.layers.FLayers;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.layers.SingleLayerIterator;
 import com.iver.cit.gvsig.gui.cad.tools.SelectionCADTool;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 
 public class EditionPreferencePage extends AbstractPreferencePage {
 	private JLabel jLabel = null;
 
 	private JTextField jTxtTolerance = null;
 
 	private JLabel jLabel1 = null;
 
 	private JSeparator jSeparator = null;
 
 	private JScrollPane jScrollPane = null;
 
 	private JTable jTableSnapping = null;
 
 	private JLabel jLabelCache = null;
 
 	private JPanel jPanelNord = null;
 
 	private JPanel jPanelCache = null;
 	private boolean changed = false;
 
 	private FLayers layers;
 
 	private MapContext mapContext;
 
 	private class MyRecord {
 		public Boolean bSelec = new Boolean(false);
 
 		public String layerName;
 
 		public Integer maxFeat = new Integer(1000);
 	}
 
 	private class MyTableModel extends AbstractTableModel {
 		private ArrayList records = new ArrayList();
 
 		public MyTableModel(FLayers layers) {
 			addLayer(layers);
 		}
 
 		private void addLayer(FLayer lyr) {
 			if (lyr instanceof FLayers) {
 				FLayers lyrGroup = (FLayers) lyr;
 				for (int i = 0; i < lyrGroup.getLayersCount(); i++) {
 					FLayer lyr2 = lyrGroup.getLayer(i);
 					addLayer(lyr2);
 				}
 			} else {
 				if (lyr instanceof FLyrVect) {
 					FLyrVect aux = (FLyrVect) lyr;
 					MyRecord rec = new MyRecord();
 					rec.layerName = lyr.getName();
 					rec.bSelec = new Boolean(aux.isSpatialCacheEnabled());
 					rec.maxFeat = new Integer(aux.getSpatialCache()
 							.getMaxFeatures());
 					records.add(rec);
 				}
 			}
 		}
 
 		public int getColumnCount() {
 			return 3;
 		}
 
 		public int getRowCount() {
 			return records.size();
 		}
 
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			MyRecord rec = (MyRecord) records.get(rowIndex);
 			if (columnIndex == 0)
 				return rec.bSelec;
 			if (columnIndex == 1)
 				return rec.layerName;
 			if (columnIndex == 2)
 				return rec.maxFeat;
 			return null;
 
 		}
 
 		public Class getColumnClass(int c) {
 			if (c == 0)
 				return Boolean.class;
 			if (c == 2)
 				return Integer.class;
 			return String.class;
 		}
 
 		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 			MyRecord rec = (MyRecord) records.get(rowIndex);
 			if (columnIndex == 0)
 				rec.bSelec = (Boolean) aValue;
 			if (columnIndex == 2) {
 				if (aValue != null)
 					rec.maxFeat = (Integer) aValue;
 				else
 					rec.maxFeat = new Integer(0);
 			}
			changed  =true;
 			super.setValueAt(aValue, rowIndex, columnIndex);
 		}
 
 		public boolean isCellEditable(int rowIndex, int columnIndex) {
 			if (columnIndex == 0)
 				return true;
 			if (columnIndex == 2)
 				return true;
 
 			return false;
 		}
 
 		public String getColumnName(int column) {
 			if (column == 0)
 				return PluginServices.getText(this, "Selected");
 			if (column == 1)
 				return PluginServices.getText(this, "LayerName");
 			if (column == 2)
 				return PluginServices.getText(this, "MaxFeaturesEditionCache");
 			return "You shouldn't reach this point";
 
 		}
 
 	}
 
 	/**
 	 * This method initializes
 	 *
 	 */
 	public EditionPreferencePage() {
 		super();
 		initialize();
 	}
 
 	/*
 	 * private void addLayer(FLayer lyr) { if (lyr instanceof FLayers) { FLayers
 	 * lyrGroup = (FLayers) lyr; for (int i=0; i < lyrGroup.getLayersCount();
 	 * i++) { FLayer lyr2 = lyrGroup.getLayer(i); addLayer(lyr2); } } else { if
 	 * (lyr instanceof FLyrVect) { layers.add(lyr); } } }
 	 */
 
 	/**
 	 * This method initializes this
 	 *
 	 */
 	private void initialize() {
 		BorderLayout layout = new BorderLayout();
 		layout.setHgap(20);
 
 		this.setLayout(layout);
 
 		jLabelCache = new JLabel();
 		jLabelCache
 				.setText(PluginServices.getText(this, "capas_edition_cache"));
 		jLabelCache.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
 		jLabelCache.setPreferredSize(new java.awt.Dimension(500,20));
 		jLabelCache.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		jLabel1 = new JLabel();
 		jLabel1.setText("pixels");
 		jLabel1.setBounds(new java.awt.Rectangle(195, 8, 207, 15));
 		jLabel1.setPreferredSize(new java.awt.Dimension(28, 20));
 		jLabel1.setName("jLabel1");
 		jLabel = new JLabel();
 		jLabel.setText("Snap Tolerance:");
 		jLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 		jLabel.setName("jLabel");
 		jLabel.setBounds(new java.awt.Rectangle(15, 8, 122, 15));
 		jLabel.setPreferredSize(new java.awt.Dimension(28, 20));
 		jLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
 
 		this.setSize(new java.awt.Dimension(502,288));
 		this.setPreferredSize(this.getSize());
 		this.add(getJPanelNord(), BorderLayout.NORTH);
 
 		this.add(getJSeparator(), BorderLayout.CENTER);
 
 		this.add(getJPanelCache(), BorderLayout.CENTER);
 
 	}
 
 	public String getID() {
 		return this.getClass().getName();
 	}
 
 	public String getTitle() {
 		return PluginServices.getText(this, "Edition");
 	}
 
 	public JPanel getPanel() {
 		return this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see com.iver.cit.gvsig.gui.preferences.IPreference#initializeValues()
 	 */
 	public void initializeValues() {
 		// /* Vamos a usar esto por ahora as:
 		// * Al abrir el dialogo, miramos las capas que hay
 		// * en edicin y las capas activas.
 		// * Las capas en edicin nos las guardamos para
 		// * fijarles las propiedades, y las que estn activas
 		// * las metemos en la tabla de configuracin de
 		// * snapping.
 		// */
 		// FLyrVect firstLyrVect = null;
 		// for (int i=0; i<layers.getLayersCount(); i++)
 		// {
 		// FLayer aux = layers.getLayer(i);
 		// if (aux.isActive())
 		// if (aux instanceof FLyrVect)
 		// {
 		// firstLyrVect = (FLyrVect) aux;
 		// }
 		// }
 		//
 		// TableModel tm = getJTableSnapping().getModel();
 		// for (int i=0; i < tm.getRowCount(); i++)
 		// {
 		// String layerName = (String) tm.getValueAt(i, 1);
 		// FLayer layer = layers.getLayer(layerName);
 		// FLyrVect lyr = (FLyrVect) layers.getLayer(layerName);
 		// Boolean bUseCache = (Boolean) tm.getValueAt(i,0);
 		// Integer maxFeat = (Integer) tm.getValueAt(i,2);
 		// lyr.setSpatialCacheEnabled(bUseCache.booleanValue());
 		// lyr.setMaxFeaturesInEditionCache(maxFeat.intValue());
 		// }
 		//
 
 	}
 
 	public void storeValues() throws StoreException {
 		TableModel tm = getJTableSnapping().getModel();
 		ArrayList layersToSnap = new ArrayList();
 		for (int i = 0; i < tm.getRowCount(); i++) {
 			String layerName = (String) tm.getValueAt(i, 1);
 			FLyrVect lyr = (FLyrVect) layers.getLayer(layerName);
 			Boolean bUseCache = (Boolean) tm.getValueAt(i, 0);
 			Integer maxFeat = (Integer) tm.getValueAt(i, 2);
 
 			// Decidimos si vamos a habilitar el spatialCache DESPUES, justo
 			// antes de renderizar.
 			// Necesitamos un mtodo que explore las capas en edicin y mire las
 			// capas sobre las
 			// que se necestia el cache. Aqu lo que hacemos es aadir las
 			// seleccionadas a la
 			// lista de capas asociadas al snapping de los temas activos en
 			// edicin.
 			// Lo del mximo de features en cach, tiene que ser para cada capa
 			// distinto. Pero no
 			// puedes "chafar" el que ya hay, porque puedes fastidiar a otra
 			// capa en edicin.
 			// Como mximo, lo que podemos hacer es que si es mayor al que hay,
 			// lo subimos. Si
 			// se solicita uno menor, lo dejamos como est.
 			// Otra opcin sera no hacer caso de esto para cada capa, y ponerlo
 			// de forma global.
 			// lyr.setSpatialCacheEnabled(bUseCache.booleanValue());
 			lyr.setMaxFeaturesInEditionCache(maxFeat.intValue());
 			if (bUseCache.booleanValue())
 				layersToSnap.add(lyr);
 		}
 		SingleLayerIterator it = new SingleLayerIterator(layers);
 		EditionManager edManager = CADExtension.getEditionManager();
 
 		while (it.hasNext()) {
 			FLayer aux = it.next();
 			if (aux instanceof FLyrVect)
 			{
 				FLyrVect lyrVect = (FLyrVect) aux;
 				// Inicializamos todas
 				lyrVect.setSpatialCacheEnabled(false);
 				if (aux.isActive())
 					if (aux.isEditing()) {
 						// Sobre la capa en edicin siempre se puede hacer snapping
 						lyrVect.setSpatialCacheEnabled(true);
 						VectorialLayerEdited lyrEd = (VectorialLayerEdited) edManager
 								.getLayerEdited(aux);
 						lyrEd.setLayersToSnap(layersToSnap);
 
 					}
 			}
 		} // while
 		it.rewind();
 		/*
 		 * Iteramos por las capas en edicin y marcamos aquellas capas que
 		 * necesitan trabajar con el cache habilitado
 		 */
 		while (it.hasNext()) {
 			FLayer aux = it.next();
 			if (aux.isEditing())
 				if (aux instanceof FLyrVect) {
 						VectorialLayerEdited lyrEd = (VectorialLayerEdited) edManager
 								.getLayerEdited(aux);
 						for (int i=0; i<lyrEd.getLayersToSnap().size(); i++)
 						{
 							FLyrVect lyrVect = (FLyrVect) lyrEd.getLayersToSnap().get(i);
 							lyrVect.setSpatialCacheEnabled(true);
 						}
 
 				}
 
 		} // while
 		mapContext.redraw();
 		try{
 			SelectionCADTool.tolerance = Integer.parseInt(getJTxtTolerance().getText());
 
 		}catch (Exception e) {
 			throw new StoreException(PluginServices.getText(this, "tolerancia_incorrecta"),e);
 		}
 	}
 
 	public void initializeDefaults() {
 		getJTxtTolerance().setText("4");
 		TableModel tm = getJTableSnapping().getModel();
 		for (int i = 0; i < tm.getRowCount(); i++) {
 			String layerName = (String) tm.getValueAt(i, 1);
 			FLyrVect lyr = (FLyrVect) layers.getLayer(layerName);
 			Boolean bUseCache = (Boolean) tm.getValueAt(i, 0);
 			Integer maxFeat = (Integer) tm.getValueAt(i, 2);
 			lyr.setSpatialCacheEnabled(bUseCache.booleanValue());
 			lyr.setMaxFeaturesInEditionCache(maxFeat.intValue());
 		}
 
 	}
 
 	public ImageIcon getIcon() {
 		return null;
 	}
 
 	public void setMapContext(MapContext mc) {
 		// addLayer(layers);
 		this.mapContext = mc;
 		this.layers = mc.getLayers();
 		MyTableModel tm = new MyTableModel(layers);
 		getJTableSnapping().setModel(tm);
 		getJTxtTolerance().setText(String.valueOf(SelectionCADTool.tolerance));
 	}
 
 	/**
 	 * This method initializes jTxtTolerance
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getJTxtTolerance() {
 		if (jTxtTolerance == null) {
 			jTxtTolerance = new JTextField();
 			jTxtTolerance.setPreferredSize(new java.awt.Dimension(28, 20));
 			jTxtTolerance.setName("jTxtTolerance");
 			jTxtTolerance.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
 			jTxtTolerance.setText("4");
 			jTxtTolerance.setBounds(new java.awt.Rectangle(142, 8, 39, 15));
 			jTxtTolerance.addKeyListener(new KeyListener() {
                	public void keyPressed(KeyEvent e) { changed = true; }
 				public void keyReleased(KeyEvent e) { changed = true; }
 				public void keyTyped(KeyEvent e){ changed = true; }
 			});
 		}
 		return jTxtTolerance;
 	}
 
 	/**
 	 * This method initializes jSeparator
 	 *
 	 * @return javax.swing.JSeparator
 	 */
 	private JSeparator getJSeparator() {
 		if (jSeparator == null) {
 			jSeparator = new JSeparator();
 			jSeparator.setPreferredSize(new java.awt.Dimension(200,2));
 		}
 		return jSeparator;
 	}
 
 	/**
 	 * This method initializes jScrollPane
 	 *
 	 * @return javax.swing.JScrollPane
 	 */
 	private JScrollPane getJScrollPane() {
 		if (jScrollPane == null) {
 			jScrollPane = new JScrollPane();
 			jScrollPane.setPreferredSize(new java.awt.Dimension(500,419));
 			jScrollPane.setViewportView(getJTableSnapping());
 		}
 		return jScrollPane;
 	}
 
 	/**
 	 * This method initializes jTableSnapping
 	 *
 	 * @return javax.swing.JTable
 	 */
 	private JTable getJTableSnapping() {
 		if (jTableSnapping == null) {
 			jTableSnapping = new JTable();
 			// TableColumnModel cm = new DefaultTableColumnModel();
 			// TableColumn checkCol = new TableColumn(0, 50);
 			// cm.addColumn(checkCol);
 			//
 			// TableColumn layerCol = new TableColumn(1, 250);
 			// cm.addColumn(layerCol);
 			//
 			// TableColumn maxFeatCol = new TableColumn(2, 50);
 			// cm.addColumn(maxFeatCol);
 			//
 			// JTableHeader head = new JTableHeader(cm);
 			// head.setVisible(true);
 			//
 			//
 			// TableModel tm = new DefaultTableModel(4,3);
 			// jTableSnapping.setModel(tm);
 			// jTableSnapping.setTableHeader(head);
 			jTableSnapping.addKeyListener(new KeyListener() {
                	public void keyPressed(KeyEvent e) { changed = true; }
 				public void keyReleased(KeyEvent e) { changed = true; }
 				public void keyTyped(KeyEvent e){ changed = true; }
 			});
 		}
 		return jTableSnapping;
 	}
 
 	/**
 	 * This method initializes jPanelNord
 	 *
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJPanelNord() {
 		if (jPanelNord == null) {
 			jPanelNord = new JPanel();
 			jPanelNord.setLayout(null);
 			jPanelNord
 					.setComponentOrientation(java.awt.ComponentOrientation.UNKNOWN);
 			jPanelNord.setPreferredSize(new java.awt.Dimension(30, 30));
 			jPanelNord.add(jLabel, null);
 			jPanelNord.add(getJTxtTolerance(), null);
 			jPanelNord.add(jLabel1, null);
 
 		}
 		return jPanelNord;
 	}
 
 	/**
 	 * This method initializes jPanelCache
 	 *
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJPanelCache() {
 		if (jPanelCache == null) {
 			jPanelCache = new JPanel();
 			jPanelCache.setLayout(new BorderLayout());
 			jPanelCache.add(jLabelCache, java.awt.BorderLayout.NORTH);
 			jPanelCache.add(getJScrollPane(), java.awt.BorderLayout.EAST);
 		}
 		return jPanelCache;
 	}
 
 	public boolean isValueChanged() {
 		return changed;
 	}
 
 	public void setChangesApplied() {
 		changed = false;
 	}
 
 }  //  @jve:decl-index=0:visual-constraint="14,10"
 
