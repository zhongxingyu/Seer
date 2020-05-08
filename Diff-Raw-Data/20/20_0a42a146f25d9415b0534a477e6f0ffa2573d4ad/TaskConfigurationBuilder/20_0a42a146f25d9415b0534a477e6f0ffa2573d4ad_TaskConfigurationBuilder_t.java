 /*
  * Copyright 2010, 2011, 2012 mapsforge.org
  *
  * This program is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mapsforge.mapmaker;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.mapsforge.mapmaker.gui.MapFileWizardPage;
 import org.mapsforge.mapmaker.gui.OptionSelectionWizardPage;
 import org.mapsforge.mapmaker.gui.POIWizardPage;
 import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
 
 class TaskConfigurationBuilder {
 	private LinkedList<TaskConfiguration> taskConfigurations;
 	private static final Map<String, String> EMPTY_PIPE_ARGS = new HashMap<String, String>();
 	
 
 	static enum TaskType {
 		READ_BINARY, READ_XML, MAP_WRITER, POI_WRITER;
 	}
 
 	/**
 	 * This multiplicator transforms a raw spinner value (stored as int) to its
 	 * float representation.
 	 */
 	static final float SIMPLIFICATION_FACTOR_MULTIPLICATOR = 0.01f;
 
 	/**
 	 * Default constructor.
 	 */
 	TaskConfigurationBuilder() {
 		this.taskConfigurations = new LinkedList<TaskConfiguration>();
 	}
 
 	/**
 	 * 
 	 * @return All generated task configurations.
 	 */
 	List<TaskConfiguration> getTaskConfigurations() {
 		return this.taskConfigurations;
 	}
 
 	/**
 	 * Constructs a new Osmosis task with given parameters
 	 * 
 	 * @param taskType
 	 *            See {@link TaskType}.
 	 * @param params
 	 *            Array of text parameters as they were used by a command line
 	 *            call.
 	 */
 	void createAndAddTask(TaskType taskType, String... params) {
 		// First task must be a source task
 		if (taskConfigurations.size() == 0) {
 			assert (taskType == TaskType.READ_BINARY || taskType == TaskType.READ_XML);
 		}
 
 		// More than one sink task --> add tee task
 		if (this.taskConfigurations.size() == 2) {
 			createAndInsertTeeTask();
 		}
 
 		String type = taskTypeToOsmosisString(taskType);
 		String id = this.taskConfigurations.size() + "-" + type;
 		Map<String, String> configArgs = new HashMap<String, String>();
 		String defaultArg = null;
 
 		String tag[];
 		for (String p : params) {
 			tag = p.split("=", 2);
 			if (tag.length == 1 && defaultArg == null) {
 				defaultArg = p;
 			}
 			if (tag.length == 2) {
 				configArgs.put(tag[0], tag[1]);
 			}
 		}
 
 		TaskConfiguration config = new TaskConfiguration(id, type,
 				EMPTY_PIPE_ARGS, configArgs, defaultArg);
 		this.taskConfigurations.add(config);
 	}
 
 	/**
 	 * Adds parameters to the last task constructed {@link Byte}
 	 * {@link #createAndAddTask(TaskType, String...)}.
 	 * 
 	 * @param parameters
 	 *            Array of text parameters (key=value) as they were used by a
 	 *            command line call.
 	 */
 	void appendParametersToLastAddedTask(String... parameters) {
 		TaskConfiguration config = this.taskConfigurations.getLast();
 
 		// Create a new task configuration as the current configurations's
 		// values cannot be changed
 		Map<String, String> configArgs = new HashMap<String, String>(
 				config.getConfigArgs());
 		Map<String, String> pipeArgs = new HashMap<String, String>(
 				config.getPipeArgs());
 		String defaultArg = config.getDefaultArg();
 		String id = config.getId();
 		String type = config.getType();
 
 		String tag[];
 		for (String p : parameters) {
 			tag = p.split("=", 2);
 			if (tag.length == 1 && defaultArg == null) {
 				defaultArg = p;
 			}
 			if (tag.length == 2) {
 				configArgs.put(tag[0], tag[1]);
 			}
 		}
 
 		// Replace config with an updated one
 		TaskConfiguration configNew = new TaskConfiguration(id, type, pipeArgs,
 				configArgs, defaultArg);
 		this.taskConfigurations.removeLast();
 		this.taskConfigurations.add(configNew);
 	}
 
 	/**
 	 * Inserts a tee task if more than one writer task exist.
 	 */
 	private void createAndInsertTeeTask() {
 		this.taskConfigurations.add(1, new TaskConfiguration("2-tee", "tee",
 				EMPTY_PIPE_ARGS, EMPTY_PIPE_ARGS, null));
 	}
 
 	/**
 	 * Reads a configuration created by a GUI and stored in an
 	 * {@link IDialogSettings} object and creates matching
 	 * {@link TaskConfiguration}s out of it.
 	 * 
 	 * @param settings
 	 *            Serialized GUI input.
 	 */
 	void buildTaskConfigurationFromDialogSettings(IDialogSettings settings) {
 		IDialogSettings generalSection = settings
 				.getSection(OptionSelectionWizardPage.getSettingsSectionName());
 		IDialogSettings poiSection = settings.getSection(POIWizardPage
 				.getSettingsSectionName());
 		IDialogSettings mapFileSection = settings.getSection(MapFileWizardPage
 				.getSettingsSectionName());
 
 		boolean createPOIs = generalSection.getBoolean("createPOIs");
 		boolean createMapFile = generalSection.getBoolean("createVectorMap");
 
 		String inputFilePath = generalSection.get("inputFilePath");
 		// TODO handle error if file name does not have an extension
 		// defined
 		String inputFileType = inputFilePath.split("\\.(?=[^\\.]*$)")[1];
 
 		// INPUT FILE TASK
 		if (inputFileType.equalsIgnoreCase("pbf")) {
 			createAndAddTask(TaskType.READ_BINARY, inputFilePath);
 		} else if (inputFileType.equalsIgnoreCase("osm")) {
 			createAndAddTask(TaskType.READ_XML, inputFilePath);
 		}
 
 		// POI TASK
 		if (createPOIs) {
 			String categoryConfigPath = poiSection.get("categoryConfigPath");
 			String outputFilePath = poiSection.get("outputFilePath");
 			createAndAddTask(TaskType.POI_WRITER, outputFilePath,
 					"categoryConfigPath=" + categoryConfigPath, "gui-mode=true");
 		}
 
 		// MAP FILE TASK
 		if (createMapFile) {
 			createAndAddTask(
 					TaskType.MAP_WRITER,
 					mapFileSection.get("mapFilePath"),
 					"type="
 							+ (mapFileSection.getBoolean("enableHDDCache") ? "hd"
 									: "ram"),
 					"bbox=" + mapFileSection.get("BBMinLat") + ","
 							+ mapFileSection.get("BBMinLon") + ","
 							+ mapFileSection.get("BBMaxLat") + ","
 							+ mapFileSection.get("BBMaxLon"),
 					"preferred-language="
 							+ mapFileSection.get("preferredLanguage"),
 					"polygon-clipping="
 							+ mapFileSection
 									.getBoolean("enablePolygonClipping"),
 					"way-clipping="
 							+ mapFileSection.getBoolean("enableWayClipping"),
 					"label-position="
 							+ mapFileSection
 									.getBoolean("computeLabelPositions"),
 					"simplification-factor="
 							+ mapFileSection.getInt("simplificationFactor")
 							* SIMPLIFICATION_FACTOR_MULTIPLICATOR,
 					"bbox-enlargement="
 							+ mapFileSection.getInt("BBEnlargement"),
 					"zoom-interval-conf="
 							+ mapFileSection.get("zoomIntervalConfiguration"),
 					"debug-file=" + mapFileSection.get("enableDebugFile"));
 
 			// TODO add gui-mode=true as parameter (hook Osmosis plugin first)
 
 			// optional parameters
 			if (mapFileSection.getBoolean("enableCustomStartPosition")) {
 				appendParametersToLastAddedTask("map-start-position="
 						+ mapFileSection.get("startPositionLat") + ","
 						+ mapFileSection.get("startPositionLon"));
 			}
 
 			if (mapFileSection.getBoolean("enableCustomStartZoomLevel")) {
 				appendParametersToLastAddedTask("map-start-zoom="
 						+ mapFileSection.get("mapZoomLevel"));
 			}
 
 			if (mapFileSection.getBoolean("enableCustomTagConfig")) {
 				appendParametersToLastAddedTask("tag-conf-file="
 						+ mapFileSection.get("tagConfigurationFilePath"));
 			}
 
 			if (mapFileSection.get("comment") != null
 					&& !mapFileSection.get("comment").isEmpty()) {
 				appendParametersToLastAddedTask("comment="
 						+ mapFileSection.get("comment"));
 			}
 
 		}
 	}
 
 	private static String taskTypeToOsmosisString(TaskType taskType) {
 		switch (taskType) {
 		case READ_BINARY:
 			return "rb";
 		case READ_XML:
 			return "rx";
 		case MAP_WRITER:
			// XXX revert back to 'mw' when merging with trunk 
			return "mw2";
 		case POI_WRITER:
 			return "poi-writer";
 		default:
 			return "";
 		}
 	}
 }
