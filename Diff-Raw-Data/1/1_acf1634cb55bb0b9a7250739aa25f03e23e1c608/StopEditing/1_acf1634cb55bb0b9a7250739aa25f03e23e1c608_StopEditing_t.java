 package com.iver.cit.gvsig;
 
 import java.awt.Component;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.andami.plugins.Extension;
 import com.iver.andami.plugins.IExtension;
 import com.iver.andami.plugins.status.IExtensionStatus;
 import com.iver.andami.plugins.status.IUnsavedData;
 import com.iver.andami.plugins.status.UnsavedData;
 import com.iver.cit.gvsig.exceptions.layers.CancelEditingLayerException;
 import com.iver.cit.gvsig.exceptions.layers.LegendLayerException;
 import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
 import com.iver.cit.gvsig.exceptions.table.CancelEditingTableException;
 import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
 import com.iver.cit.gvsig.fmap.MapContext;
 import com.iver.cit.gvsig.fmap.MapControl;
 import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
 import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
 import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
 import com.iver.cit.gvsig.fmap.edition.EditionEvent;
 import com.iver.cit.gvsig.fmap.edition.ISpatialWriter;
 import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
 import com.iver.cit.gvsig.fmap.layers.FLayer;
 import com.iver.cit.gvsig.fmap.layers.FLayers;
 import com.iver.cit.gvsig.fmap.layers.FLyrAnnotation;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.layers.LayersIterator;
 import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
 import com.iver.cit.gvsig.fmap.spatialindex.IPersistentSpatialIndex;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 import com.iver.cit.gvsig.project.documents.table.gui.Table;
 import com.iver.cit.gvsig.project.documents.view.IProjectView;
 import com.iver.cit.gvsig.project.documents.view.ProjectView;
 import com.iver.cit.gvsig.project.documents.view.ProjectViewFactory;
 import com.iver.cit.gvsig.project.documents.view.gui.View;
 import com.iver.cit.gvsig.project.documents.view.legend.CreateSpatialIndexMonitorableTask;
 import com.iver.utiles.swing.threads.IMonitorableTask;
 
 /**
  * @author Francisco Jos
  *
  * Cuando un tema se pone en edicin, puede que su driver implemente
  * ISpatialWriter. En ese caso, es capaz de guardarse sobre s mismo. Si no lo
  * implementa, esta opcin estar deshabilitada y la nica posibilidad de
  * guardar este tema ser "Guardando como..."
  */
 public class StopEditing extends Extension {
 	private View vista;
 
 	/**
 	 * @see com.iver.andami.plugins.IExtension#initialize()
 	 */
 	public void initialize() {
 	}
 
 	/**
 	 * @see com.iver.andami.plugins.IExtension#execute(java.lang.String)
 	 */
 	public void execute(String s) {
 		com.iver.andami.ui.mdiManager.IWindow f = PluginServices.getMDIManager()
 				.getActiveWindow();
 
 		vista = (View) f;
 		boolean isStop=false;
 		IProjectView model = vista.getModel();
 		MapContext mapa = model.getMapContext();
 		FLayers layers = mapa.getLayers();
 		EditionManager edMan = CADExtension.getEditionManager();
 		if (s.equals("STOPEDITING")) {
 			FLayer[] actives = layers.getActives();
 			// TODO: Comprobar que solo hay una activa, o al menos
 			// que solo hay una en edicin que est activa, etc, etc
 			for (int i = 0; i < actives.length; i++) {
 				if (actives[i] instanceof FLyrVect && actives[i].isEditing()) {
 					FLyrVect lv = (FLyrVect) actives[i];
 					MapControl mapControl = vista.getMapControl();
 //					VectorialLayerEdited lyrEd = (VectorialLayerEdited)	edMan.getActiveLayerEdited();
 					//lyrEd.clearSelection();
 					isStop=stopEditing(lv, mapControl);
 					if (isStop){
 						lv.removeLayerListener(edMan);
 						if (lv instanceof FLyrAnnotation){
 							FLyrAnnotation lva=(FLyrAnnotation)lv;
 				            lva.setMapping(lva.getMapping());
 						}
 					}
 				}
 			}
 			if (isStop) {
 				vista.getMapControl().setTool("zoomIn");
 				vista.hideConsole();
 				vista.repaintMap();
 				CADExtension.clearView();
 
 			}
 		}
 		PluginServices.getMainFrame().enableControls();
 	}
 
 	/**
 	 * @see com.iver.andami.plugins.IExtension#isEnabled()
 	 */
 	public boolean isEnabled() {
 		FLayer[] lyrs = EditionUtilities.getActiveAndEditedLayers();
 		if (lyrs == null)
 			return false;
 		FLyrVect lyrVect = (FLyrVect) lyrs[0];
 		if (lyrVect.getSource() instanceof VectorialEditableAdapter) {
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * DOCUMENT ME!
 	 */
 	public boolean stopEditing(FLyrVect layer, MapControl mapControl) {
 		VectorialEditableAdapter vea = (VectorialEditableAdapter) layer
 				.getSource();
 		int resp = JOptionPane.NO_OPTION;
 
 		try {
 			if (layer.isWritable()) {
 				resp = JOptionPane.showConfirmDialog((Component) PluginServices
 						.getMainFrame(), PluginServices.getText(this,
 						"realmente_desea_guardar_la_capa")
 						+ " : " + layer.getName()+"?", PluginServices.getText(this,
 						"guardar"), JOptionPane.YES_NO_OPTION);
 				if (resp != JOptionPane.YES_OPTION) { // CANCEL EDITING
 					cancelEdition(layer);
 				} else { // GUARDAMOS EL TEMA
 					saveLayer(layer);
 				}
 
 				vea.getCommandRecord().removeCommandListener(mapControl);
 				layer.setEditing(false);
 				if (layer.isSpatiallyIndexed())
 	            {
 	            	if(vea.getSpatialIndex() != null)
 	                {
 	            		layer.setISpatialIndex(vea.getSpatialIndex());
 	            		if(layer.getISpatialIndex() instanceof IPersistentSpatialIndex)
 	                        ((IPersistentSpatialIndex) layer.getISpatialIndex()).flush();
 //	            		PluginServices.
 //								cancelableBackgroundExecution(new FlushSpatialIndexMonitorableTask(layer));
 
 	                }else {
 	            		PluginServices.
 						cancelableBackgroundExecution(new CreateSpatialIndexMonitorableTask(layer));
 	                }
 	            }
 
 				return true;
 			}
 			// Si no existe writer para la capa que tenemos en edicin
 				resp = JOptionPane
 						.showConfirmDialog(
 								(Component) PluginServices.getMainFrame(),
 								PluginServices
 										.getText(
 												this,
 												"no_existe_writer_para_este_formato_de_capa_o_no_tiene_permisos_de_escritura_los_datos_no_se_guardaran_desea_continuar")
 										+ " : " + layer.getName(),
 								PluginServices.getText(this, "cancelar_edicion"),
 								JOptionPane.YES_NO_OPTION);
 				if (resp == JOptionPane.YES_OPTION) { // CANCEL EDITING
 					cancelEdition(layer);
 					layer.setEditing(false);
 					vea.getCommandRecord().removeCommandListener(mapControl);
 					if (!(layer.getSource().getDriver() instanceof IndexedShpDriver)){
 						VectorialLayerEdited vle=(VectorialLayerEdited)CADExtension.getEditionManager().getLayerEdited(layer);
 						layer.setLegend((IVectorLegend)vle.getLegend());
 					}
 					return true;
 				}
 
 		} catch (LegendLayerException e) {
 			NotificationManager.addError(e);
 		} catch (StartEditionLayerException e) {
 			NotificationManager.addError(e);
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e);
 		} catch (InitializeWriterException e) {
 			NotificationManager.addError(e);
 		} catch (CancelEditingTableException e) {
 			NotificationManager.addError(e);
 		} catch (StopWriterVisitorException e) {
 			NotificationManager.addError(e);
 		} catch (CancelEditingLayerException e) {
 			NotificationManager.addError(e);
 		}
 		return false;
 
 	}
 
 
 	private void saveLayer(FLyrVect layer) throws ReadDriverException, InitializeWriterException, StopWriterVisitorException{
 		layer.setProperty("stoppingEditing",new Boolean(true));
 		VectorialEditableAdapter vea = (VectorialEditableAdapter) layer
 				.getSource();
 
 		ISpatialWriter writer = (ISpatialWriter) vea.getWriter();
 		com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices
 				.getMDIManager().getAllWindows();
 		for (int j = 0; j < views.length; j++) {
 			if (views[j] instanceof Table) {
 				Table table = (Table) views[j];
 				if (table.getModel().getAssociatedTable() != null
 						&& table.getModel().getAssociatedTable().equals(layer)) {
 					table.stopEditingCell();
 				}
 			}
 		}
 		vea.cleanSelectableDatasource();
 		layer.setRecordset(vea.getRecordset()); // Queremos que el recordset del layer
 		// refleje los cambios en los campos.
 		ILayerDefinition lyrDef = EditionUtilities.createLayerDefinition(layer);
 		String aux="FIELDS:";
 		FieldDescription[] flds = lyrDef.getFieldsDesc();
 		for (int i=0; i < flds.length; i++)
 		{
 			aux = aux + ", " + flds[i].getFieldAlias();
 		}
 		System.err.println("Escribiendo la capa " + lyrDef.getName() +
 				" con los campos " + aux);
 		writer.initialize(lyrDef);
 		vea.stopEdition(writer, EditionEvent.GRAPHIC);
 		layer.setProperty("stoppingEditing",new Boolean(false));
 	}
 
 	private void cancelEdition(FLyrVect layer) throws CancelEditingTableException, CancelEditingLayerException {
 		layer.setProperty("stoppingEditing",new Boolean(true));
 		com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices
 				.getMDIManager().getAllWindows();
 		VectorialEditableAdapter vea = (VectorialEditableAdapter) layer
 				.getSource();
 		vea.cancelEdition(EditionEvent.GRAPHIC);
 		for (int j = 0; j < views.length; j++) {
 			if (views[j] instanceof Table) {
 				Table table = (Table) views[j];
 				if (table.getModel().getAssociatedTable() != null
 						&& table.getModel().getAssociatedTable().equals(layer)) {
 					table.cancelEditing();
 				}
 			}
 		}
 		layer.setProperty("stoppingEditing",new Boolean(false));
 	}
 	/**
 	 * @see com.iver.andami.plugins.IExtension#isVisible()
 	 */
 	public boolean isVisible() {
 		if (EditionUtilities.getEditionStatus() == EditionUtilities.EDITION_STATUS_ONE_VECTORIAL_LAYER_ACTIVE_AND_EDITABLE)
 			return true;
 		return false;
 
 	}
 	public IExtensionStatus getStatus() {
 		return new StopEditingStatus();
 	}
 	/**
 	 * Show the dialogs to save the layer without ask if don't like to save.
 	 * @param layer Layer to save.
 	 */
 	public boolean executeSaveLayer(FLyrVect layer ) {
 		EditionManager edMan = CADExtension.getEditionManager();
 		VectorialLayerEdited lyrEd = (VectorialLayerEdited)	edMan.getLayerEdited(layer);
 		boolean isStop=false;
 		try {
 			lyrEd.clearSelection(false);
 
 
 		if (layer.isWritable()) {
 				saveLayer(layer);
 				layer.setEditing(false);
 				if (layer.isSpatiallyIndexed())
 		            {
 		            	if(layer.getISpatialIndex() != null)
 		                {
 		                	PluginServices.
 									cancelableBackgroundExecution(new CreateSpatialIndexMonitorableTask((FLyrVect)layer));
 						}
 		            }
 
 			isStop=true;
 		}else {
 //			 Si no existe writer para la capa que tenemos en edicin
 			int resp = JOptionPane
 					.showConfirmDialog(
 							(Component) PluginServices.getMainFrame(),
 							PluginServices
 									.getText(
 											this,
 											"no_existe_writer_para_este_formato_de_capa_o_no_tiene_permisos_de_escritura_los_datos_no_se_guardaran_desea_continuar")
 									+ " : " + layer.getName(),
 							PluginServices.getText(this, "cancelar_edicion"),
 							JOptionPane.YES_NO_OPTION);
 			if (resp == JOptionPane.YES_OPTION) { // CANCEL EDITING
 				try {
 					cancelEdition(layer);
 					layer.setEditing(false);
 				if (!(layer.getSource().getDriver() instanceof IndexedShpDriver)){
 					VectorialLayerEdited vle=(VectorialLayerEdited)CADExtension.getEditionManager().getLayerEdited(layer);
 					layer.setLegend((IVectorLegend)vle.getLegend());
 				}
 				} catch (CancelEditingTableException e) {
 					PluginServices.getLogger().error(e.getMessage(),e);
 //					NotificationManager.addError(e.getMessage(),e);
 					return isStop;
 				} catch (CancelEditingLayerException e) {
 					PluginServices.getLogger().error(e.getMessage(),e);
 //					NotificationManager.addError(e.getMessage(),e);
 					return isStop;
 				} catch (LegendLayerException e) {
 					PluginServices.getLogger().error(e.getMessage(),e);
 //					NotificationManager.addError(e.getMessage(),e);
 					return isStop;
 				}
 				isStop=true;
 			}
 
 		}
 //		boolean isStop=stopEditing((FLyrVect)layer, null);
 		if (isStop){
 			layer.removeLayerListener(edMan);
 			if (layer instanceof FLyrAnnotation){
 				FLyrAnnotation lva=(FLyrAnnotation)layer;
 	            lva.setMapping(lva.getMapping());
 			}
 			com.iver.andami.ui.mdiManager.IWindow f = PluginServices.getMDIManager()
 			.getActiveWindow();
 			if (f instanceof View) {
 				vista = (View) f;
 				if (vista.getMapControl().getMapContext().getLayers().getLayer(layer.getName()).equals(layer)) {
 					vista.getMapControl().setTool("zoomIn");
 					vista.hideConsole();
 					vista.repaintMap();
 					CADExtension.clearView();
 				}
 			}
 		}
 		} catch (ReadDriverException e1) {
 			PluginServices.getLogger().error(e1.getMessage(),e1);
 //			NotificationManager.addError(e.getMessage(),e);
 		} catch (StartEditionLayerException e) {
 			PluginServices.getLogger().error(e.getMessage(),e);
 //			NotificationManager.addError(e.getMessage(),e);
 		} catch (StopWriterVisitorException e) {
 			PluginServices.getLogger().error(e.getMessage(),e);
 //			NotificationManager.addError(e.getMessage(),e);
 		} catch (InitializeWriterException e) {
 			PluginServices.getLogger().error(e.getMessage(),e);
 //			NotificationManager.addError(e.getMessage(),e);
 		}
 		return isStop;
 	}
 
 	private class UnsavedLayer extends UnsavedData{
 
 		private FLayer layer;
 
 		public UnsavedLayer(IExtension extension) {
 			super(extension);
 		}
 
 		public String getDescription() {
 			return PluginServices.getText(this,"editing_layer_unsaved");
 		}
 
 		public String getResourceName() {
 			return layer.getName();
 		}
 
 
 
 		public boolean saveData() {
 			return executeSaveLayer((FLyrVect)layer);
 		}
 
 		public void setLayer(FLayer layer) {
 			this.layer=layer;
 
 		}
 
 		public ImageIcon getIcon() {
 			return layer.getTocImageIcon();
 		}
 
 	}
 
 	/**
 	 * <p>This class provides the status of extensions.
 	 * If this extension has some unsaved editing layer (and save them), and methods
 	 * to check if the extension has some associated background tasks.
 	 *
 	 * @author Vicente Caballero Navarro
 	 *
 	 */
 	private class StopEditingStatus implements IExtensionStatus {
 		/**
 	     * This method is used to check if this extension has some unsaved editing layer.
 	     *
 	     * @return true if the extension has some unsaved editing layer, false otherwise.
 	     */
 		public boolean hasUnsavedData() {
 			ProjectExtension pe=(ProjectExtension)PluginServices.getExtension(ProjectExtension.class);
 			ProjectView[] views=(ProjectView[])pe.getProject().getDocumentsByType(ProjectViewFactory.registerName).toArray(new ProjectView[0]);
 			for (int i=0;i<views.length;i++) {
 				FLayers layers=views[i].getMapContext().getLayers();
 				LayersIterator iter = getEditingLayer(layers);
 				if (iter.hasNext()) {
 					return true;
 				}
 			}
 			return false;
 		}
 		/**
 	     * This method is used to check if the extension has some associated
 	     * background process which is currently running.
 	     *
 	     * @return true if the extension has some associated background process,
 	     * false otherwise.
 	     */
 		public boolean hasRunningProcesses() {
 			return false;
 		}
 		 /**
 	     * <p>Gets an array of the traceable background tasks associated with this
 	     * extension. These tasks may be tracked, canceled, etc.</p>
 	     *
 	     * @return An array of the associated background tasks, or null in case there is
 	     * no associated background tasks.
 	     */
 		public IMonitorableTask[] getRunningProcesses() {
 			return null;
 		}
 		/**
 	     * <p>Gets an array of the UnsavedData objects, which contain information about
 	     * the unsaved editing layers and allows to save it.</p>
 	     *
 	     * @return An array of the associated unsaved editing layers, or null in case the extension
 	     * has not unsaved editing layers.
 	     */
 		public IUnsavedData[] getUnsavedData() {
 			ProjectExtension pe=(ProjectExtension)PluginServices.getExtension(ProjectExtension.class);
 			ProjectView[] views=(ProjectView[])pe.getProject().getDocumentsByType(ProjectViewFactory.registerName).toArray(new ProjectView[0]);
 			ArrayList unsavedLayers=new ArrayList();
 			for (int i=0;i<views.length;i++) {
 				FLayers layers = views[i].getMapContext().getLayers();
 				LayersIterator iter = getEditingLayer(layers);
 				while (iter.hasNext()){
 					UnsavedLayer ul=new UnsavedLayer(StopEditing.this);
 					ul.setLayer(iter.nextLayer());
 					unsavedLayers.add(ul);
 				}
 			}
 			return (IUnsavedData[])unsavedLayers.toArray(new IUnsavedData[0]);
 		}
 	}
 	private LayersIterator getEditingLayer(FLayers layers){
 		return new LayersIterator(layers){
 			public boolean evaluate(FLayer layer) {
 				return layer.isEditing();
 			}
 		};
 	}
 }
 
