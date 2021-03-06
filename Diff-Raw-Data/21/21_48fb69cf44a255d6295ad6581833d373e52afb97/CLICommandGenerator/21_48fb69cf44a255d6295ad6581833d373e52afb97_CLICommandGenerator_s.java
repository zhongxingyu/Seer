 package com.genericworkflownodes.knime.execution.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.knime.core.node.NodeLogger;
 
 import com.genericworkflownodes.knime.cliwrapper.CLIElement;
 import com.genericworkflownodes.knime.cliwrapper.CLIMapping;
 import com.genericworkflownodes.knime.config.INodeConfiguration;
import com.genericworkflownodes.knime.config.IPluginConfiguration;
 import com.genericworkflownodes.knime.config.INodeConfigurationStore;
 import com.genericworkflownodes.knime.config.PlainNodeConfigurationWriter;
 import com.genericworkflownodes.knime.execution.ICommandGenerator;
 import com.genericworkflownodes.knime.parameter.BoolParameter;
 
 public class CLICommandGenerator implements ICommandGenerator {
 
 	protected static final NodeLogger logger = NodeLogger
 			.getLogger(CLICommandGenerator.class);
 
 	private INodeConfiguration nodeConfig;
 	private INodeConfigurationStore configStore;
 
 	@Override
 	public List<String> generateCommands(INodeConfiguration nodeConfiguration,
 			INodeConfigurationStore configStore,
 			IPluginConfiguration pluginConfiguration, File workingDirectory)
 			throws Exception {
 
 		// export the node configuration as plain text, for debugging and
 		// logging
 		exportPlainConfiguration(configStore, workingDirectory);
 
 		// ease the passing around of variables
 		this.nodeConfig = nodeConfiguration;
 		this.configStore = configStore;
 
 		List<String> commands;
 
 		try {
 			commands = processCLI();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			this.nodeConfig = null;
 			this.configStore = null;
 		}
 
 		return commands;
 	}
 
 	/**
	 * @return
 	 * @throws Exception
 	 */
 	private List<String> processCLI() throws Exception {
 		List<String> commands = new ArrayList<String>();
 
 		for (CLIElement cliElement : nodeConfig.getCLI().getCLIElement()) {
 			logger.info("CLIElement: " + cliElement.getOptionIdentifier());
 
 			if (!"".equals(cliElement.getOptionIdentifier())
 					&& cliElement.getMapping().size() == 0) {
 				// simple fixed argument for the command line, no mapping to
 				// params given
				commands.add(cliElement.getOptionIdentifier());
 			} else if (isMappedToBooleanParameter(cliElement)) {
 				// it is mapped to bool
 				handleBooleanParameter(commands, cliElement);
 			} else {
 
 				List<List<String>> extractedParameterValues = extractParamterValues(cliElement);
 				validateExtractedParameters(extractedParameterValues);
 
 				// we only add those parameters to the command line if they
 				// contain any values, this removes optional parameters if they
 				// were not set
 				if (extractedParameterValues.size() != 0) {
 					expandParameters(extractedParameterValues, cliElement,
 							commands);
 				}
 			}
 		}
 		return commands;
 	}
 
 	/**
 	 * Add the extracted parameter values to the command line.
 	 * 
 	 * @note This method requires, that all contained lists have the same size.
 	 * 
 	 * @param extractedParameterValues
 	 * @param cliElement
 	 * @param commands
 	 */
 	private void expandParameters(List<List<String>> extractedParameterValues,
 			CLIElement cliElement, List<String> commands) {
 		// since we assume that the outer list is not empty this will always
 		// work
 		int listSize = extractedParameterValues.get(0).size();
 
 		// in each iteration we expand the i-th element of each internal list to
 		// the command line prefixed with the cliElement optionIdentifier (if it
 		// has one)
 		for (int i = 0; i < listSize; ++i) {
 			// add the command prefix in each iteration
 			if (!"".equals(cliElement.getOptionIdentifier())) {
 				commands.add(cliElement.getOptionIdentifier());
 			}
 
 			// add the actual values
 			for (List<String> innerList : extractedParameterValues) {
 				commands.add(innerList.get(i));
 			}
 		}
 	}
 
 	/**
 	 * Given the provided list of parameter values this method should ensure
 	 * that all mapped lists have the same length.
 	 * 
 	 * @param extractedParameterValues
 	 * @throws Exception
 	 *             If not all contained lists have the same size.
 	 */
 	private void validateExtractedParameters(
 			List<List<String>> extractedParameterValues) throws Exception {
 
 		int currentSize = -1;
 		for (List<String> currentList : extractedParameterValues) {
 			if (currentSize != -1 && currentSize != currentList.size()) {
 				throw new Exception(
 						"All mapped value lists must have the same size.");
 			}
 			currentSize = currentList.size();
 		}
 	}
 
 	/**
 	 * Given the cliElement create a list containing for each mapped parameter a
 	 * list with the mapped values.
 	 * 
 	 * @param cliElement
 	 *            The current cliElement.
 	 * @return
 	 */
 	private List<List<String>> extractParamterValues(CLIElement cliElement) {
 
 		List<List<String>> extractedParameterValues = new ArrayList<List<String>>();
 
 		for (CLIMapping cliMapping : cliElement.getMapping()) {
 			if (configStore.getParameterKeys()
 					.contains(cliMapping.getRefName())) {
 				extractedParameterValues.add(configStore
 						.getMultiParameterValue(cliMapping.getRefName()));
 			}
 		}
 
 		return extractedParameterValues;
 	}
 
 	/**
 	 * Returns true if the given CLIElement maps to a boolean parameter.
 	 * 
 	 * @param cliElement
 	 * @return
 	 */
 	private boolean isMappedToBooleanParameter(CLIElement cliElement) {
 		return cliElement.getMapping().size() == 1
 				&& nodeConfig.getParameter(cliElement.getMapping().get(0)
 						.getRefName()) instanceof BoolParameter;
 	}
 
 	/**
 	 * Interpret boolean parameter on the command line -> if true, add text
 	 * field, if false, do not add text field.
 	 * 
 	 * @param commands
 	 *            The list of commands that will be executed later.
 	 * @param cliElement
 	 *            The currently interpreted clielement.
 	 */
 	private void handleBooleanParameter(List<String> commands,
 			CLIElement cliElement) {
 		if (((BoolParameter) nodeConfig.getParameter(cliElement.getMapping()
 				.get(0).getRefName())).getValue()) {
 			commands.add(cliElement.getOptionIdentifier());
 		}
 	}
 
 	/**
 	 * Exports all configuration settings to the working directory.
 	 * 
 	 * @param configStore
 	 * @throws IOException
 	 */
 	private void exportPlainConfiguration(INodeConfigurationStore configStore,
 			File workingDirectory) throws IOException {
 		PlainNodeConfigurationWriter writer = new PlainNodeConfigurationWriter();
 		configStore.setParameterValue("jobdir",
 				workingDirectory.getCanonicalPath());
 		writer.init(configStore);
 		writer.write(workingDirectory.getAbsolutePath() + File.separator
 				+ "params.ini");
 	}
 }
