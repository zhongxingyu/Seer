 package es.icarto.gvsig.catastro;
 
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import com.hardcode.gdbms.engine.values.Value;
 import com.iver.andami.PluginServices;
 import com.iver.andami.plugins.Extension;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.layers.FLayer;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.gui.cad.CADTool;
 import com.iver.cit.gvsig.gui.cad.tools.AreaCADTool;
 import com.iver.cit.gvsig.gui.cad.tools.CutPolygonCADTool;
 import com.iver.cit.gvsig.gui.cad.tools.JoinCADTool;
 import com.iver.cit.gvsig.gui.cad.tools.RedigitalizePolygonCADTool;
 import com.iver.cit.gvsig.listeners.CADListenerManager;
 import com.iver.cit.gvsig.listeners.EndGeometryListener;
 
 import es.icarto.gvsig.catastro.evaluator.ConstruccionActionsEvaluator;
 import es.icarto.gvsig.catastro.evaluator.ConstruccionRulesEvaluator;
 import es.icarto.gvsig.catastro.evaluator.ManzanaActionsEvaluator;
 import es.icarto.gvsig.catastro.evaluator.ManzanaRulesEvaluator;
import es.icarto.gvsig.catastro.evaluator.PredioActionsDeslindeEvaluator;
 import es.icarto.gvsig.catastro.evaluator.PredioActionsDivideEvaluator;
 import es.icarto.gvsig.catastro.evaluator.PredioRulesDivideEvaluator;
 import es.icarto.gvsig.catastro.evaluator.PredioRulesFusionEvaluator;
 import es.icarto.gvsig.catastro.evaluator.actions.PredioCalculateNewID;
 import es.icarto.gvsig.catastro.utils.Preferences;
 import es.icarto.gvsig.catastro.utils.TOCLayerManager;
 import es.icarto.gvsig.catastro.utils.ToggleEditing;
 
 public class ActionDispatcherExtension extends Extension implements
 	EndGeometryListener {
 
     private static final int NO_ACTION = -1;
     private final int ACTION_CALCULATE_NEW_PREDIO_ID = 0;
     private final int ACTION_CHECK_RULES_FOR_DIVIDING_PREDIO = 1;
     private final int ACTION_CHECK_RULES_FOR_MERGING_PREDIO = 2;
     private static final int ACTION_CHECK_RULES_FOR_NEW_MANZANA = 3;
     private static final int ACTION_CHECK_RULES_FOR_NEW_CONSTRUCCION = 4;
     private static final int ACTION_CHECK_RULES_FOR_MODIFYING_CONSTRUCCION = 5;
     private static final int ACTION_DESLINDE_PREDIO_WITH_MANZANA = 6;
     private int idNewPredio = -1;
 
     @Override
     public void initialize() {
 	CADListenerManager.removeEndGeometryListener("catastro");
 	CADListenerManager.addEndGeometryListener("catastro", this);
     }
 
     @Override
     public void execute(String actionCommand) {
 	// nothing to do
     }
 
     @Override
     public boolean isEnabled() {
 	return false;
     }
 
     @Override
     public boolean isVisible() {
 	return false;
     }
 
     @Override
     public void endGeometry(FLayer layer, String cadToolKey) {
 
 	CADTool cadTool = CADExtension.getCADTool();
 	int action = getAction(layer, cadToolKey, cadTool);
 	ToggleEditing te = new ToggleEditing();
 	TOCLayerManager tocLayerManager = new TOCLayerManager();
 
 	if (action == ACTION_CALCULATE_NEW_PREDIO_ID) {
 	    IRowEdited selectedRow = ((CutPolygonCADTool) cadTool)
 		    .getSelectedRow();
 	    PredioCalculateNewID calculator = new PredioCalculateNewID(
 		    (FLyrVect) layer, selectedRow);
 	    Value[] values = null;
 	    if (calculator.execute()) {
 		values = calculator.getAttributes();
 		idNewPredio = Integer.parseInt(values[7].toString());
 	    }
 	    ((CutPolygonCADTool) cadTool).setParametrizableValues(values);
 	} else if (action == ACTION_CHECK_RULES_FOR_DIVIDING_PREDIO) {
 	    ArrayList<IGeometry> geoms = ((CutPolygonCADTool) cadTool)
 		    .getGeometriesCreated();
 	    PredioRulesDivideEvaluator predioRulesEvaluator = new PredioRulesDivideEvaluator(
 		    geoms);
 	    if (!predioRulesEvaluator.isOK()) {
 		if (tocLayerManager.isPrediosLayerInEdition()) {
 		    te.stopEditing(layer, true); // don't save changes
 		}
 		JOptionPane.showMessageDialog(null, predioRulesEvaluator
 			.getErrorMessage(), "Divide predio",
 			JOptionPane.WARNING_MESSAGE);
 	    } else {
 		int option = JOptionPane.showConfirmDialog(null, PluginServices
 			.getText(this, "save_predio_confirm"), "Divide predio",
 			JOptionPane.YES_NO_OPTION,
 			JOptionPane.QUESTION_MESSAGE, null);
 		if (option == JOptionPane.OK_OPTION) {
 		    PredioActionsDivideEvaluator predioActionsEvaluator = new PredioActionsDivideEvaluator(
 			    geoms, idNewPredio);
 		    predioActionsEvaluator.execute();
 		    if (tocLayerManager.isPrediosLayerInEdition()) {
 			te.stopEditing(layer, false); // save changes
 		    }
 		} else {
 		    if (tocLayerManager.isPrediosLayerInEdition()) {
 			te.stopEditing(layer, true); // don't save changes
 		    }
 		}
 	    }
 	} else if (action == ACTION_CHECK_RULES_FOR_MERGING_PREDIO) {
 	    ArrayList<IGeometry> geoms = new ArrayList<IGeometry>();
 	    IGeometry finalGeometry = ((JoinCADTool) cadTool)
 		    .getJoinedGeometry();
 	    geoms.add(finalGeometry);
 	    PredioRulesFusionEvaluator fusionPrediosRulesEvaluator = new PredioRulesFusionEvaluator(
 		    geoms);
 	    if (fusionPrediosRulesEvaluator.isOK()) {
 		// TODO: save previous actions
 		// te.stopEditing(layer, false);
 	    } else {
 		// te.stopEditing(layer, true);
 		JOptionPane.showMessageDialog(null, fusionPrediosRulesEvaluator
 			.getErrorMessage(), "Fusin Predios",
 			JOptionPane.WARNING_MESSAGE);
 	    }
 	} else if (action == ACTION_CHECK_RULES_FOR_NEW_MANZANA) {
 	    IGeometry insertedGeometry = ((AreaCADTool) cadTool)
 		    .getInsertedGeometry();
 	    int rowIndex = ((AreaCADTool) cadTool).getVirtualIndex();
 	    ManzanaRulesEvaluator manzanaRulesEvaluator = new ManzanaRulesEvaluator(
 		    insertedGeometry);
 	    if (!manzanaRulesEvaluator.isOK()) {
 		if (tocLayerManager.isManzanaLayerInEdition()) {
 		    te.stopEditing(layer, true);
 		}
 		JOptionPane.showMessageDialog(null, manzanaRulesEvaluator
 			.getErrorMessage(), "Alta Manzana",
 			JOptionPane.WARNING_MESSAGE);
 	    } else {
 		int option = JOptionPane.showConfirmDialog(null, PluginServices
 			.getText(this, "save_manzana_confirm"),
 			"Crear Manzana", JOptionPane.YES_NO_OPTION,
 			JOptionPane.QUESTION_MESSAGE, null);
 		if (option == JOptionPane.OK_OPTION) {
 		    ManzanaActionsEvaluator manzanaActionsEvaluator = new ManzanaActionsEvaluator(
 			    (FLyrVect) layer, rowIndex);
 		    manzanaActionsEvaluator.execute();
 		}
 		if (tocLayerManager.isManzanaLayerInEdition()) {
 		    // TODO: save previous actions
 		    te.stopEditing(layer, false);
 		}
 	    }
 	} else if (action == ACTION_CHECK_RULES_FOR_NEW_CONSTRUCCION) {
 	    IGeometry insertedGeometry = ((AreaCADTool) cadTool)
 		    .getInsertedGeometry();
 	    int rowIndex = ((AreaCADTool) cadTool).getVirtualIndex();
 	    ConstruccionRulesEvaluator construccionRulesEvaluator = new ConstruccionRulesEvaluator(
 		    insertedGeometry);
 	    if (!construccionRulesEvaluator.isOK()) {
 		if (tocLayerManager.isConstruccionesLayerInEdition()) {
 		    te.stopEditing(layer, true); // don't save values
 		}
 		JOptionPane.showMessageDialog(null, construccionRulesEvaluator
 			.getErrorMessage(), "Alta Construccin",
 			JOptionPane.WARNING_MESSAGE);
 	    } else {
 		int option = JOptionPane.showConfirmDialog(null, PluginServices
 			.getText(this, "save_construccion_confirm"),
 			"Alta Construccin", JOptionPane.YES_NO_OPTION,
 			JOptionPane.QUESTION_MESSAGE, null);
 		if (option == JOptionPane.OK_OPTION) {
 		    ConstruccionActionsEvaluator construccionActionsEvaluator = new ConstruccionActionsEvaluator(
 			    (FLyrVect) layer, rowIndex);
 		    construccionActionsEvaluator.execute();
 		    // TODO: Launch Form
 		    if (tocLayerManager.isConstruccionesLayerInEdition()) {
 			te.stopEditing(layer, false); // save values
 		    }
 		} else {
 		    if (tocLayerManager.isConstruccionesLayerInEdition()) {
 			te.stopEditing(layer, true); // don't save values
 		    }
 		}
 	    }
 	} else if (action == ACTION_CHECK_RULES_FOR_MODIFYING_CONSTRUCCION) {
 	    IGeometry insertedGeometry = ((RedigitalizePolygonCADTool) cadTool)
 		    .getgeometryResulting();
 	    ConstruccionRulesEvaluator construccionRulesEvaluator = new ConstruccionRulesEvaluator(
 		    insertedGeometry);
 	    if (!construccionRulesEvaluator.isOK()) {
 		if (tocLayerManager.isConstruccionesLayerInEdition()) {
 		    te.stopEditing(layer, true); // don't save values
 		}
 		JOptionPane.showMessageDialog(null, construccionRulesEvaluator
 			.getErrorMessage(), "Alta Construccin",
 			JOptionPane.WARNING_MESSAGE);
 	    } else {
 		int option = JOptionPane.showConfirmDialog(null, PluginServices
 			.getText(this, "save_construccion_confirm"),
 			"Alta Construccin", JOptionPane.YES_NO_OPTION,
 			JOptionPane.QUESTION_MESSAGE, null);
 		if (option == JOptionPane.OK_OPTION) {
 		    if (tocLayerManager.isConstruccionesLayerInEdition()) {
 			te.stopEditing(layer, false); // save values
 		    }
 		} else {
 		    if (tocLayerManager.isConstruccionesLayerInEdition()) {
 			te.stopEditing(layer, true); // don't save values
 		    }
 		}
 	    }
 	} else if (action == ACTION_DESLINDE_PREDIO_WITH_MANZANA) {
 	    IGeometry newPredioGeometry = ((CutPolygonCADTool) cadTool)
 		    .getRemainingGeometry();
	    PredioActionsDeslindeEvaluator predioActionsDeslindeEvaluator = new PredioActionsDeslindeEvaluator(
 		    newPredioGeometry);
	    ArrayList<String> errorMessages = predioActionsDeslindeEvaluator
		    .execute();
	    if (errorMessages.size() == 0) {
		te
			.stopEditing(
				tocLayerManager
					.getLayerByName(Preferences.MANZANAS_LAYER_NAME),
				false);
		te.stopEditing(tocLayerManager
			.getLayerByName(Preferences.PREDIOS_LAYER_NAME), false);
	    } else {
		te.stopEditing(tocLayerManager
			.getLayerByName(Preferences.MANZANAS_LAYER_NAME), true);
		te.stopEditing(tocLayerManager
			.getLayerByName(Preferences.MANZANAS_LAYER_NAME), true);
	    }
 	}
     }
 
     private int getAction(FLayer layer, String cadToolKey, CADTool cadTool) {
 	if ((cadToolKey
 		.equalsIgnoreCase(CutPolygonCADTool.CUT_END_FIRST_POLYGON))
 		&& (cadTool instanceof CutPolygonCADTool)
 		&& (layer instanceof FLyrVect)) {
 	    return ACTION_CALCULATE_NEW_PREDIO_ID;
 	} else if ((cadToolKey.equalsIgnoreCase(CutPolygonCADTool.CUT_END))
 		&& (cadTool instanceof CutPolygonCADTool)
 		&& (layer instanceof FLyrVect)) {
 	    return ACTION_CHECK_RULES_FOR_DIVIDING_PREDIO;
 	} else if (cadToolKey.equalsIgnoreCase(JoinCADTool.JOIN_ACTION_COMMAND)
 		&& (cadTool instanceof JoinCADTool)
 		&& (layer instanceof FLyrVect)) {
 	    return ACTION_CHECK_RULES_FOR_MERGING_PREDIO;
 	} else if (cadToolKey.equalsIgnoreCase(AreaCADTool.AREA_ACTION_COMMAND)
 		&& (cadTool instanceof AreaCADTool)
 		&& (layer instanceof FLyrVect)
 		&& layer.getName().compareToIgnoreCase(
 			Preferences.MANZANAS_LAYER_NAME) == 0) {
 	    return ACTION_CHECK_RULES_FOR_NEW_MANZANA;
 	} else if (cadToolKey.equalsIgnoreCase(AreaCADTool.AREA_ACTION_COMMAND)
 		&& (cadTool instanceof AreaCADTool)
 		&& (layer instanceof FLyrVect)
 		&& layer.getName().compareToIgnoreCase(
 			Preferences.CONSTRUCCIONES_LAYER_NAME) == 0) {
 	    return ACTION_CHECK_RULES_FOR_NEW_CONSTRUCCION;
 	} else if (cadToolKey
 		.equalsIgnoreCase(RedigitalizePolygonCADTool.REDIGITALIZE_ACTION_COMMAND)
 		&& (cadTool instanceof RedigitalizePolygonCADTool)
 		&& (layer instanceof FLyrVect)
 		&& layer.getName().compareToIgnoreCase(
 			Preferences.CONSTRUCCIONES_LAYER_NAME) == 0) {
 	    return ACTION_CHECK_RULES_FOR_MODIFYING_CONSTRUCCION;
 	} else if (cadToolKey
 		.equalsIgnoreCase(CutPolygonCADTool.CUT_ACTION_COMMAND)
 		&& (cadTool instanceof CutPolygonCADTool)
 		&& (layer instanceof FLyrVect)) {
 	    return ACTION_DESLINDE_PREDIO_WITH_MANZANA;
 	}
 	return NO_ACTION;
     }
 
 }
