 package es.icarto.gvsig.catastro;
 
 import javax.swing.JOptionPane;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.plugins.Extension;
 import es.icarto.gvsig.catastro.constants.ConstantManager;
 import es.icarto.gvsig.catastro.utils.TOCLayerManager;
 import es.icarto.gvsig.catastro.wrappers.InsertAreaWrapper;
 
 public class CatastroManzanaNewExtension extends Extension {
 
<<<<<<< HEAD
    private InsertAreaWrapper insertAreaWrapper;
    private ConstantManager constantManager;
    private TOCLayerManager tocLayerManager;

    @Override
    public void initialize() {
	insertAreaWrapper = new InsertAreaWrapper();
	insertAreaWrapper.initialize();
	constantManager = new ConstantManager();
    }

    @Override
    public void execute(String actionCommand) {
	tocLayerManager = new TOCLayerManager();
	if(constantManager.areConstantsSetForManzana()){
	    tocLayerManager.setActiveAndVisibleLayersForManzanas();
	    insertAreaWrapper.execute(actionCommand);
	} else{
	    Object[] options = { "OK" };
	    JOptionPane.showOptionDialog(null, PluginServices.getText(this, "select_region"), "Warning",
		    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
		    null, options, options[0]);
=======
 	private InsertAreaWrapper insertAreaWrapper;
	private FLayer layer;
 	private ConstantManager constantManager;
 	private TOCLayerManager tocLayerManager;
 
 	@Override
 	public void initialize() {
 		insertAreaWrapper = new InsertAreaWrapper();
 		insertAreaWrapper.initialize();
 		constantManager = new ConstantManager();
 	}
 
 	@Override
 	public void execute(String actionCommand) {
 
 		if (constantManager.areConstantsSetForManzana()) {
 			tocLayerManager = new TOCLayerManager();
 			tocLayerManager.setActiveAndVisibleLayersForManzanas();
 			insertAreaWrapper.execute(actionCommand);
 		} else {
 			Object[] options = { "OK" };
 			JOptionPane.showOptionDialog(null, PluginServices.getText(this,
 					"select_region"), "Warning", JOptionPane.DEFAULT_OPTION,
 					JOptionPane.WARNING_MESSAGE, null, options, options[0]);
 		}
>>>>>>> Added CheckManzanaIsWithinOneRegion rule with tolerance buffer
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return insertAreaWrapper.isEnabled();
 	}
 
 	@Override
 	public boolean isVisible() {
 		return insertAreaWrapper.isVisible();
 	}
 }
