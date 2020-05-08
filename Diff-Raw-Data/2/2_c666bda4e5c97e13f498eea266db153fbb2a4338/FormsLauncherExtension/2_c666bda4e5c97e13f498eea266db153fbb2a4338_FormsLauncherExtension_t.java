 package es.udc.cartolab.gvsig.pmf.forms;
 
 import org.apache.log4j.Logger;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.plugins.Extension;
 import com.iver.andami.ui.mdiManager.IWindow;
 import com.iver.cit.gvsig.fmap.layers.FLayer;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.project.documents.view.gui.BaseView;
 
 import es.udc.cartolab.gvsig.loadData.preferences.LoadDataConfigDialog;
 import es.udc.cartolab.gvsig.navtableforms.AbstractForm;
 import es.udc.cartolab.gvsig.navtableforms.Utils;
 import es.udc.cartolab.gvsig.navtableforms.ormlite.ORMLite;
 import es.udc.cartolab.gvsig.navtableforms.ormlite.ORMLiteDataBase.ORMLiteTable;
 import es.udc.cartolab.gvsig.pmf.preferences.Preferences;
 import es.udc.cartolab.gvsig.tools.CopyFeaturesExtension;
 
 public class FormsLauncherExtension extends Extension {
 
 	FLyrVect layer;
 	BaseView view = null;
 	private String layerName;
 
 	private static Logger logger = Logger.getLogger("PMF");
 
 	public void execute(String actionCommand) {
 
 		layer = getLayerNameFromXML();
 		AbstractForm dialog = null;
 
 		if (layerName.equals("comunidad")) {
 			dialog = new ComunidadForm(layer);
 		} else if (layerName.equals("centros_educativos")) {
 			dialog = new CentroEducativoForm(layer);
 		} else if (layerName.equals("centros_salud")) {
 			dialog = new CentroSaludForm(layer);
 		} else if (layerName.equals("centros_reuniones")) {
 			dialog = new CentroReunionesForm(layer);
 		} else if (layerName.equals("vivienda")) {
 			dialog = new ViviendaForm(layer);
 		} else if (layerName.equals("parcela")) {
 			dialog = new ParcelaForm(layer);
 		} else if (layerName.equals("fuentes_comunitarias")) {
 
 		} else if (layerName.equals("limites_parcela")) {
 
 		}
 
 		if (dialog.init()) {
 			PluginServices.getMDIManager().addWindow(dialog);
 		}
 	}
 
 	private FLyrVect getLayerNameFromXML() {
 		return Utils.getFlyrVect(view, layerName);
 	}
 
 	protected void registerIcons() {
 		PluginServices.getIconTheme().registerDefault(
 				"forms-launcher-icon",
 				this.getClass().getClassLoader()
 				.getResource("images/form.png"));
 	}
 
 	public void initialize() {
 		registerIcons();
 		CopyFeaturesExtension cfe = ((CopyFeaturesExtension) PluginServices.getExtension(CopyFeaturesExtension.class));
		cfe.setDefaultPath(LoadDataConfigDialog.getConfigPath(false));
 	}
 
 	public boolean isEnabled() {
 		IWindow window = PluginServices.getMDIManager().getActiveWindow();
 		boolean isEnabled = false;
 		if (window instanceof BaseView) {
 			view = (BaseView) window;
 			FLayer[] actives = view.getMapControl().getMapContext().getLayers().getActives();
 			if (1 == actives.length){
 				layerName = actives[0].getName();
 				ORMLiteTable table = ORMLite.getDataBaseObject(
 						Preferences.getXMLFileName()).getTable(layerName);
 				if (table != null) {
 					isEnabled = true;
 				}
 			}
 		}
 
 		return isEnabled;
 	}
 
 	public boolean isVisible() {
 		return true;
 	}
 }
