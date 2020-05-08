 package edu.ucsf.rbvi.structureViz2.internal.model;
 
 import java.awt.Color;
import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager.ModelType;
 import edu.ucsf.rbvi.structureViz2.internal.port.ListenerThreads;
 
 /**
  * This object maintains the Chimera communication information.
  */
 public class ChimeraManager {
 
 	static private Process chimera;
 	static private ListenerThreads chimeraListenerThreads;
 	static private Map<Integer, ChimeraModel> currentModelsMap;
 
 	private static Logger logger = LoggerFactory
 			.getLogger(edu.ucsf.rbvi.structureViz2.internal.model.ChimeraManager.class);
 
 	private StructureManager structureManager;
 
 	public ChimeraManager(StructureManager structureManager) {
 		this.structureManager = structureManager;
 		chimera = null;
 		chimeraListenerThreads = null;
 		currentModelsMap = new HashMap<Integer, ChimeraModel>();
 	}
 
 	public List<ChimeraModel> getChimeraModels(String modelName, ModelType modelType) {
 		List<ChimeraModel> models = new ArrayList<ChimeraModel>();
 		for (ChimeraModel model : currentModelsMap.values()) {
 			if (modelName.equals(model.getModelName()) && modelType.equals(model.getModelType())) {
 				models.add(model);
 			}
 		}
 		return models;
 	}
 
 	public ChimeraModel getChimeraModel(Integer modelNumber, Integer subModelNumber) {
 		Integer key = ChimUtils.makeModelKey(modelNumber, subModelNumber);
 		if (currentModelsMap.containsKey(key)) {
 			return currentModelsMap.get(key);
 		}
 		return null;
 	}
 
 	public Collection<ChimeraModel> getChimeraModels() {
 		// this method is invoked by the model navigator dialog
 		return currentModelsMap.values();
 	}
 
 	public int getChimeraModelsCount(boolean smiles) {
 		// this method is invokes by the model navigator dialog
 		int counter = currentModelsMap.size();
 		if (smiles) {
 			return counter;
 		}
 
 		for (ChimeraModel model : currentModelsMap.values()) {
 			if (model.getModelType() == ModelType.SMILES) {
 				counter--;
 			}
 		}
 		return counter;
 	}
 
 	public boolean hasChimeraModel(Integer modelNubmer) {
 		return hasChimeraModel(modelNubmer, 0);
 	}
 
 	public boolean hasChimeraModel(Integer modelNubmer, Integer subModelNumber) {
 		return currentModelsMap.containsKey(ChimUtils.makeModelKey(modelNubmer, subModelNumber));
 	}
 
 	public void addChimeraModel(Integer modelNumber, Integer subModelNumber, ChimeraModel model) {
 		currentModelsMap.put(ChimUtils.makeModelKey(modelNumber, subModelNumber), model);
 	}
 
 	public void removeChimeraModel(Integer modelNumber, Integer subModelNumber) {
 		int modelKey = ChimUtils.makeModelKey(modelNumber, subModelNumber);
 		if (currentModelsMap.containsKey(modelKey)) {
 			currentModelsMap.remove(modelKey);
 		}
 	}
 
 	public List<ChimeraModel> openModel(String modelName, ModelType type) {
 		stopListening();
 		List<String> response = null;
 		if (type == StructureManager.ModelType.MODBASE_MODEL) {
 			response = sendChimeraCommand("open modbase:" + modelName, true);
 		} else if (type == StructureManager.ModelType.SMILES) {
 			response = sendChimeraCommand("open smiles:" + modelName, true);
 		} else {
 			response = sendChimeraCommand("open " + modelName, true);
 		}
 		if (response == null) {
 			// something went wrong
 			return null;
 		}
 		List<ChimeraModel> models = new ArrayList<ChimeraModel>();
 		for (String line : response) {
 			System.out.println("response line: " + line);
 			if (line.startsWith("#")) {
 				int[] modelNumbers = ChimUtils.parseModelNumber2(line);
 				if (modelNumbers[1] != 0) {
 					// what to do if multiple models?
 				}
 				int modelNumber = ChimUtils.makeModelKey(modelNumbers[0], modelNumbers[1]);
 				System.out.println("model number for opened structure: " + modelNumber);
 				if (currentModelsMap.containsKey(modelNumber)) {
 					continue;
 				}
 				ChimeraModel newModel = new ChimeraModel(modelName, type, modelNumbers[0], modelNumbers[1]);
 				currentModelsMap.put(modelNumber, newModel);
 				models.add(newModel);
 
 				// get model color
 				newModel.setModelColor(getModelColor(newModel));
 				System.out.println("color: " + newModel.getModelColor().toString());
 				// Get our properties (default color scheme, etc.)
 				// Make the molecule look decent
 				// chimeraSend("repr stick "+newModel.toSpec());
 
 				if (type != StructureManager.ModelType.SMILES) {
 					// Create the information we need for the navigator
 					getResidueInfo(newModel);
 				}
 				System.out.println("#residues: " + newModel.getResidueCount());
 				System.out.println("#chains: " + newModel.getChainCount());
 			}
 		}
 		sendChimeraCommand("focus", false);
 		startListening();
 		return models;
 	}
 
 	public void closeModel(ChimeraModel model) {
 		// int model = structure.modelNumber();
 		// int subModel = structure.subModelNumber();
 		// Integer modelKey = makeModelKey(model, subModel);
 		stopListening();
 		System.out.println("chimera close model " + model.getModelName());
 		if (currentModelsMap.containsKey(ChimUtils.makeModelKey(model.getModelNumber(),
 				model.getSubModelNumber()))) {
 			sendChimeraCommand("close " + model.toSpec(), false);
 			// currentModelNamesMap.remove(model.getModelName());
 			currentModelsMap.remove(ChimUtils.makeModelKey(model.getModelNumber(),
 					model.getSubModelNumber()));
 			// selectionList.remove(chimeraModel);
 		} else {
 			System.out.println("chimera cannot find model");
 		}
 		startListening();
 	}
 
 	public void startListening() {
 		sendChimeraCommand("listen start models; listen start select", false);
 	}
 
 	public void stopListening() {
 		sendChimeraCommand("listen stop models; listen stop select", false);
 	}
 
 	/**
 	 * Select something in Chimera
 	 * 
 	 * @param command
 	 *          the selection command to pass to Chimera
 	 */
 	public void select(String command) {
 		sendChimeraCommand("listen stop select; " + command + "; listen start select", false);
 	}
 
 	public void focus() {
 		sendChimeraCommand("focus", false);
 	}
 
 	public void clearOnChimeraExit() {
 		chimera = null;
 		currentModelsMap.clear();
 		chimeraListenerThreads = null;
 		structureManager.clearOnChimeraExit();
 	}
 
 	public void exitChimera() {
 		if (isChimeraLaunched()) {
 			sendChimeraCommand("stop really", false);
 			chimera.destroy();
 		}
 		clearOnChimeraExit();
 	}
 
 	public Map<Integer, ChimeraModel> getSelectedModels() {
 		Map<Integer, ChimeraModel> selectedModelsMap = new HashMap<Integer, ChimeraModel>();
 		List<String> chimeraReply = sendChimeraCommand("lists level molecule", true);
 		for (String modelLine : chimeraReply) {
 			ChimeraModel chimeraModel = new ChimeraModel(modelLine);
 			Integer modelKey = ChimUtils.makeModelKey(chimeraModel.getModelNumber(),
 					chimeraModel.getSubModelNumber());
 			selectedModelsMap.put(modelKey, chimeraModel);
 		}
 		return selectedModelsMap;
 	}
 
 	public void getSelectedResidues(Map<Integer, ChimeraModel> selectedModelsMap) {
 		List<String> chimeraReply = sendChimeraCommand("lists level residue", true);
 		for (String inputLine : chimeraReply) {
 			ChimeraResidue r = new ChimeraResidue(inputLine);
 			Integer modelKey = ChimUtils.makeModelKey(r.getModelNumber(), r.getSubModelNumber());
 			if (selectedModelsMap.containsKey(modelKey)) {
 				ChimeraModel model = selectedModelsMap.get(modelKey);
 				model.addResidue(r);
 			}
 		}
 	}
 
 	/**
 	 * Return the list of ChimeraModels currently open
 	 * 
 	 * @return List of ChimeraModel's
 	 */
 	public List<ChimeraModel> getModelList() {
 		List<ChimeraModel> modelList = new ArrayList<ChimeraModel>();
 		List<String> list = sendChimeraCommand("listm type molecule", true);
 		if (list != null) {
 			for (String modelLine : list) {
 				ChimeraModel chimeraModel = new ChimeraModel(modelLine);
 				modelList.add(chimeraModel);
 			}
 		}
 		return modelList;
 	}
 
 	/**
 	 * Return the list of depiction presets available from within Chimera. Chimera will return the
 	 * list as a series of lines with the format: Preset type number "description"
 	 * 
 	 * @return list of presets
 	 */
 	public List<String> getPresets() {
 		ArrayList<String> presetList = new ArrayList<String>();
 		for (String preset : sendChimeraCommand("preset list", true)) {
 			preset = preset.substring(7); // Skip over the "Preset"
 			preset = preset.replaceFirst("\"", "(");
 			preset = preset.replaceFirst("\"", ")");
 			// string now looks like: type number (description)
 			presetList.add(preset);
 		}
 		return presetList;
 	}
 
 	public boolean isChimeraLaunched() {
 		// TODO: What is the best way to test if chimera is launched?
 		if (chimera != null && sendChimeraCommand("test", true) != null) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean launchChimera(List<String> chimeraPaths) {
 		// Do nothing if Chimera is already launched
 		if (isChimeraLaunched()) {
 			return true;
 		}
 
 		// Try to launch Chimera (eventually using one of the possible paths)
 		String error = "";
 		// iterate over possible paths for starting Chimera
 		for (String chimeraPath : chimeraPaths) {
			File path = new File(chimeraPath);
			if (!path.canExecute())
				continue;
 			try {
 				List<String> args = new ArrayList<String>();
 				args.add(chimeraPath);
 				args.add("--start");
 				args.add("ReadStdin");
 				ProcessBuilder pb = new ProcessBuilder(args);
 				chimera = pb.start();
				break;
 			} catch (Exception e) {
 				// Chimera could not be started
 				error = e.getMessage();
 			}
 		}
 		// If no error, then Chimera was launched successfully
 		if (error.length() == 0) {
				System.out.println("Starting listener threads");
 			// Initialize the listener threads
 			chimeraListenerThreads = new ListenerThreads(chimera, structureManager);
 			chimeraListenerThreads.start();
 			// Ask Chimera to give us updates
 			startListening();
 			return true;
 		}
 
 		// Tell the suer that Chimera could not be started because of an error
 		System.out.println(error);
 		logger.error(error);
 		return false;
 	}
 
 	/**
 	 * Determine the color that Chimera is using for this model.
 	 * 
 	 * @param model
 	 *          the ChimeraModel we want to get the Color for
 	 * @return the default model Color for this model in Chimera
 	 */
 	public Color getModelColor(ChimeraModel model) {
 		List<String> colorLines = sendChimeraCommand(
 				"listm type molecule attr color spec " + model.toSpec(), true);
 		if (colorLines == null) {
 			return null;
 		}
 		return ChimUtils.parseModelColor((String) colorLines.get(0));
 	}
 
 	/**
 	 * 
 	 * Get information about the residues associated with a model. This uses the Chimera listr
 	 * command. We don't return the resulting residues, but we add the residues to the model.
 	 * 
 	 * @param model
 	 *          the ChimeraModel to get residue information for
 	 * 
 	 */
 	public void getResidueInfo(ChimeraModel model) {
 		int modelNumber = model.getModelNumber();
 		int subModelNumber = model.getSubModelNumber();
 		// Get the list -- it will be in the reply log
 		List<String> reply = sendChimeraCommand("listr spec #" + modelNumber + "." + subModelNumber,
 				true);
 		for (String inputLine : reply) {
 			ChimeraResidue r = new ChimeraResidue(inputLine);
 			if (r.getModelNumber() == modelNumber || r.getSubModelNumber() == subModelNumber) {
 				model.addResidue(r);
 			}
 		}
 	}
 
 	public List<String> sendChimeraCommand(String command, boolean reply) {
 		// if (!isChimeraLaunched()) {
 		// return null;
 		// }
 
 		chimeraListenerThreads.clearResponse(command);
 		String text = command.concat("\n");
 		System.out.println("send command to chimera: " + text);
 		try {
 			// send the command
 			chimera.getOutputStream().write(text.getBytes());
 			chimera.getOutputStream().flush();
 		} catch (IOException e) {
 			// logger.info("Unable to execute command: " + text);
 			// logger.info("Exiting...");
 			System.out.println("Unable to execute command: " + text);
 			clearOnChimeraExit();
 			return null;
 		}
 		if (!reply) {
 			return null;
 		}
 		return chimeraListenerThreads.getResponse(command);
 	}
 
 }
