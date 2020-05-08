 /*
  * Copyright 2008 Deputacin Provincial de A Corua
  * Copyright 2009 Deputacin Provincial de Pontevedra
  * Copyright 2010 CartoLab, Universidad de A Corua
  *
  * This file is part of openCADTools, developed by the Cartography
  * Engineering Laboratory of the University of A Corua (CartoLab).
  * http://www.cartolab.es
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
  */
 package com.iver.cit.gvsig.gui.preferences;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.prefs.Preferences;
 
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
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
 import com.iver.cit.gvsig.FollowGeometryExtension;
 import com.iver.cit.gvsig.fmap.MapContext;
 import com.iver.cit.gvsig.fmap.layers.FLayer;
 import com.iver.cit.gvsig.fmap.layers.FLayers;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.layers.SingleLayerIterator;
 import com.iver.cit.gvsig.gui.cad.tools.SelectionCADTool;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 import com.iver.cit.gvsig.project.documents.view.snapping.EIELFinalPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.EIELNearestPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.ISnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.SnapperStatus;
 import com.iver.cit.gvsig.project.documents.view.snapping.snappers.FinalPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.snappers.NearestPointSnapper;
 
 /**
  * @author gvSIG
  * @author Laboratorio de Bases de Datos. Universidad de A Corua
  * @author Cartolab. Universidad de A Corua
  */
 public class EditionPreferencePage extends AbstractPreferencePage {
 	private static Preferences prefs = Preferences.userRoot().node( "cadtooladapter" );
 	private JLabel jLabel = null;
 	private ImageIcon icon;
 
 	private JTextField jTxtTolerance = null;
 
 	private JLabel jLabel1 = null;
 
 	private JSeparator jSeparator = null;
 
 	private JScrollPane jScrollPane = null;
 
 	private JTable jTableSnapping = null;
 
 	private JLabel jLabelCache = null;
 
 	private JPanel jPanelNord = null;
 
 	private JPanel jPanelCache = null;
 	
 	VectorialLayerEdited layerEdited;
 	private JPanel jPanelSnappers = null;
 	private JCheckBox eielVertexEIELSnapCB = new JCheckBox();
 	private JCheckBox eielLineEIELSnapCB = new JCheckBox();
 	// private JCheckBox vertexSnapCB = new JCheckBox();
 	// private JCheckBox lineSnapCB = new JCheckBox();
 	private JCheckBox followGeometryCB = new JCheckBox();
 	private boolean changed = false;
 	
 	//[Cartolab]                                                                                                                                         
     private JCheckBox deleteButtonOptionCB = new JCheckBox();
 
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
 		icon = PluginServices.getIconTheme().get("edition-properties");
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
 		
 		//Add snappers checkboxes 
 		this.add(getJPanelSnappers(), BorderLayout.SOUTH);
 	}
 	
 //  este metodo me devolvera el panel donde se permiten cambiar los snappers                                                                             
     private JPanel getJPanelSnappers() {
             if (jPanelSnappers == null) {
             	JLabel snapperLabel = new JLabel();
             	snapperLabel.setText(PluginServices.getText(this, "capas_edition_snapper"));
                 snapperLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
                 snapperLabel.setPreferredSize(new java.awt.Dimension(500,20));
                 snapperLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 
                 JSeparator separador = new JSeparator();
                 separador.setPreferredSize(new java.awt.Dimension(200,12));
 
                 eielVertexEIELSnapCB.setText(PluginServices.getText(this, "capas_edition_snapper_eiel_punto"));
                 eielLineEIELSnapCB.setText(PluginServices.getText(this, "capas_edition_snapper_eiel_linea"));
                 followGeometryCB.setText(PluginServices.getText(this, "capas_edicion_follow_geometry"));
                 // vertexSnapCB.setText(PluginServices.getText(this, "capas_edition_snapper_punto"));
                 // lineSnapCB.setText(PluginServices.getText(this, "capas_edition_snapper_linea"));
 
                 eielLineEIELSnapCB.addActionListener(new java.awt.event.ActionListener() {
                 	public void actionPerformed(java.awt.event.ActionEvent evt) {
                 		if(eielVertexEIELSnapCB.isSelected()){
                 			addSnapper(new EIELNearestPointSnapper());
                 			SnapperStatus.setVertexActivated(true);
     	                	SnapperStatus.setNearLineActivated(eielLineEIELSnapCB.isSelected());
                 		}else{
                 			deleteSnapper("EIELNearestPoint");
                 			SnapperStatus.setVertexActivated(false);
     	                	SnapperStatus.setNearLineActivated(eielLineEIELSnapCB.isSelected());
                 		}
                 		prefs.putBoolean("snapperFinalPoint", eielVertexEIELSnapCB.isSelected());
                 	}
                 });
                 eielVertexEIELSnapCB.addActionListener(new java.awt.event.ActionListener() {
                 	public void actionPerformed(java.awt.event.ActionEvent evt) {
                 		if(eielLineEIELSnapCB.isSelected()){
                 			addSnapper(new EIELFinalPointSnapper());
                 			SnapperStatus.setNearLineActivated(true);
     	                	SnapperStatus.setVertexActivated(eielVertexEIELSnapCB.isSelected());
                         }else{
                             deleteSnapper("EIELFinalPoint");
                             SnapperStatus.setNearLineActivated(false);
     	                	SnapperStatus.setVertexActivated(eielVertexEIELSnapCB.isSelected());
                         }
                 prefs.putBoolean("snapperNearestPoint", eielLineEIELSnapCB.isSelected());
                     }
                 });
                                 
                 followGeometryCB.addActionListener(new java.awt.event.ActionListener() {
                     public void actionPerformed(java.awt.event.ActionEvent evt) {
                 		FollowGeometryExtension.setActivated(followGeometryCB.isSelected());
                     }
                  });
 //                eielLineEIELSnapCB.addActionListener(new java.awt.event.ActionListener() {
 //                	public void actionPerformed(java.awt.event.ActionEvent evt) {
 //                		if(eielLineEIELSnapCB.isSelected()){
 //                			addSnapper(new EIELNearestPointSnapper());
 //                		}else{
 //                            deleteSnapper("EIELNearestPoint");
 //                		}
 //                    }
 //                });
 //                vertexSnapCB.addActionListener(new java.awt.event.ActionListener() {
 //                	public void actionPerformed(java.awt.event.ActionEvent evt) {
 //                		if(lineSnapCB.isSelected()){
 //                			addSnapper(new FinalPointSnapper());
 //                		}else{	
 //                			deleteSnapper("FinalPoint");
 //                		}
 //                     }	
 //                 });		
 //                lineSnapCB.addActionListener(new java.awt.event.ActionListener() {
 //                	public void actionPerformed(java.awt.event.ActionEvent evt) {
 //                		if(lineSnapCB.isSelected()){
 //                			addSnapper(new NearestPointSnapper());
 //                		}else{
 //                            deleteSnapper("NearestPoint");
 //                        }
 //                    }
 //                });
                 // Set if the BUTTON3 of the mouse delete the last point when editing instead of popMenu()                                           
                 // popMenu() will be available with SHIFT+BUTTON3                                                                                    
                 JLabel deleteButtonLabel = new JLabel();
                 deleteButtonLabel.setText(PluginServices.getText(this, "delete_button_option"));
                 deleteButtonLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
                 deleteButtonLabel.setPreferredSize(new java.awt.Dimension(500,20));
                 deleteButtonLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                 
                 //TODO Language files                                                                                                                
                 deleteButtonOptionCB.setText(PluginServices.getText(this, "set_remove_vertex_option"));
                 //TODO Read current preference                                                                                                       
                 deleteButtonOptionCB.setSelected(true);
                 deleteButtonOptionCB.addActionListener(new java.awt.event.ActionListener() {
                 	public void actionPerformed(java.awt.event.ActionEvent evt) {
                 		prefs.putBoolean("isDeleteButton3", deleteButtonOptionCB.isSelected());
                 	}
                 });
 
                 jPanelSnappers = new JPanel();
                 jPanelSnappers.setLayout(new GridLayout(8,1));
                 jPanelSnappers.add(separador);
                 jPanelSnappers.add(snapperLabel);
                 jPanelSnappers.add(eielVertexEIELSnapCB);
                 jPanelSnappers.add(eielLineEIELSnapCB);
                 jPanelSnappers.add(followGeometryCB);
 //                jPanelSnappers.add(vertexSnapCB);
 //                jPanelSnappers.add(lineSnapCB);
                 jPanelSnappers.add(deleteButtonLabel);
                 jPanelSnappers.add(deleteButtonOptionCB);
                 //jPanelSnappers.add(getJScrollPane(), java.awt.BorderLayout.EAST);                                                                    
            }
            return jPanelSnappers;
     }
 
 
 	
 	protected void deleteSnapper(String string) {
 		// TODO Auto-generated method stub
 		int indice = 0;
 		boolean termine = false;
 		ArrayList listaSnappers = layerEdited.getSnappers();
 		if(string.equals("EIELFinalPoint")){
 			while(!termine && indice < listaSnappers.size()){
 				if((listaSnappers.get(indice) instanceof EIELFinalPointSnapper)){
 					listaSnappers.remove(indice);
 					termine=true;
 				}else{
 					if(indice<listaSnappers.size()-1){
 						indice++;
 					}else{
 						termine=true;
 					}
 				}
 			}
 		}else if(string.equals("EIELNearestPoint")){
 			while(!termine && indice < listaSnappers.size()){
 				if((listaSnappers.get(indice) instanceof EIELNearestPointSnapper)){
 					listaSnappers.remove(indice);
 					termine=true;
 				}else{
 					if(indice<listaSnappers.size()-1){
 						indice++;
 					}else{
 						termine=true;
 					}
 				}
 			}
 //		}else if(string.equals("FinalPoint")){
 //			while(!termine){
 //				if((listaSnappers.get(indice) instanceof FinalPointSnapper)){
 //					listaSnappers.remove(indice);
 //					termine=true;
 //				}else{
 //					if(indice<listaSnappers.size()-1){
 //						indice++;
 //					}else{
 //						termine=true;
 //					}
 //				}
 //			}
 //		}else if(string.equals("NearestPoint")){
 //			while(!termine){
 //				if((listaSnappers.get(indice) instanceof NearestPointSnapper)){
 //					listaSnappers.remove(indice);
 //					termine=true;
 //				}else{
 //					if(indice<listaSnappers.size()-1){
 //						indice++;
 //					}else{
 //						termine=true;
 //					}
 //				}
 //			}
 		}
 	}
 
 	protected void addSnapper(ISnapper snapper) {
 		// TODO Auto-generated method stub
 		ArrayList listaSnappers = layerEdited.getSnappers();
 		if(listaSnappers.size()>0){
 //			en caso de que ya tenga snappers debemos comprobar la prioridad de los existentes para
 //			agregar el nuevo donde corresponda
 			int prioridadSnapper = snapper.getPriority();
 			boolean termine = false;
 			int indice = 0;
 			while(!termine){
 				if(((ISnapper)listaSnappers.get(indice)).getPriority()<prioridadSnapper){
 //					en este caso el nuevo snapper debe ir en esta posicion
 					listaSnappers.add(indice, snapper);
 					termine = true;
 				}else{
 					if(indice==listaSnappers.size()-1){
 //						en este caqso habremos llegado al final de lo snappers sin
 //						encontrar ninguno con menor prioridad, por lo que lo aadiremos al final
 						listaSnappers.add(indice+1, snapper);
 						termine = true;
 					}else{
 						indice++;
 					}
 				}
 			}
 		}else{
 			listaSnappers.add(snapper);
 		}
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
 		TableModel tm = getJTableSnapping().getModel();
 		EditionManager edManager = CADExtension.getEditionManager();
 		FLayer layerActive=layers.getActives()[0];
 		VectorialLayerEdited lyrEd = (VectorialLayerEdited) edManager
 			.getLayerEdited(layerActive);
 		ArrayList layersToSnap=lyrEd.getLayersToSnap();
 		ArrayList layersVect=new ArrayList();
 		for (int i = 0; i < layers.getLayersCount(); i++) {
 			FLayer layer=layers.getLayer(i);
 			if (layer instanceof FLyrVect) {
 				layersVect.add(layer);
 			}
 		}
 		for (int i = 0; i < layersVect.size(); i++) {
 			FLyrVect lv=(FLyrVect)layersVect.get(i);
 			tm.setValueAt(lv.getName(), i, 1);
 			tm.setValueAt(layersToSnap.contains(lv), i, 0);
 			tm.setValueAt(lv.getSpatialCache().getMaxFeatures(), i, 2);
 		}
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
		mapContext.invalidate();
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
 		return icon;
 	}
 
 	public void setMapContext(MapContext mc) {
 		// addLayer(layers);
 		this.mapContext = mc;
 		this.layers = mc.getLayers();
 		MyTableModel tm = new MyTableModel(layers);
 		getJTableSnapping().setModel(tm);
 		getJTxtTolerance().setText(String.valueOf(SelectionCADTool.tolerance));
 		
 		//[LBD]
 //		aqui miraremos la capa que esta activa y editandose para coger sus snappers
 		FLayer[] capasActivas = this.layers.getActives();
 //		ahora buscaremos la primera que se este editando de las activas
 		FLayer capaEnEdicion = null;
 		boolean termine = (capasActivas.length < 1);
 		int indice = 0;
 		while(!termine){
 			if(capasActivas[indice].isEditing()){
 				capaEnEdicion = capasActivas[indice];
 				termine=true;
 			}else{
 				indice++;
 				if(indice==capasActivas.length){
 					termine = true;
 				}
 			}
 		}
 
 
 		if(capaEnEdicion!=null){
 			layerEdited = (VectorialLayerEdited)CADExtension.getEditionManager().getLayerEdited(capaEnEdicion);
 		}
 
 //		ahora aqui comprobamos los snappers activos para la capa en edicion
 //		y activamos los checkboxes correspondientes
 //		if(layerEdited!=null){
 //			ArrayList snappers = layerEdited.getSnappers();
 //            if(snappers!=null){
 //            	for(int i=0; i<snappers.size();i++){
 //            		ISnapper snapper = ((ISnapper)snappers.get(i));
 //            		if(snapper instanceof EIELFinalPointSnapper){
 //            			eielVertexEIELSnapCB.setSelected(true);
 //            		}else if(snapper instanceof EIELNearestPointSnapper){
 //            			eielLineEIELSnapCB.setSelected(true);
 //            		}else if(snapper instanceof FinalPointSnapper){
 //            			vertexSnapCB.setSelected(true);
 //            			}else if(snapper instanceof NearestPointSnapper){
 //            				lineSnapCB.setSelected(true);
 //                        }
 //                    }
 //            	}
 //			}
 			eielVertexEIELSnapCB.setSelected(SnapperStatus.isVertexActivated());
 			eielLineEIELSnapCB.setSelected(SnapperStatus.isNearLineActivated());
 			followGeometryCB.setSelected(FollowGeometryExtension.isActivated());
 			deleteButtonOptionCB.setSelected(prefs.getBoolean("isDeleteButton3", false));
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
 
