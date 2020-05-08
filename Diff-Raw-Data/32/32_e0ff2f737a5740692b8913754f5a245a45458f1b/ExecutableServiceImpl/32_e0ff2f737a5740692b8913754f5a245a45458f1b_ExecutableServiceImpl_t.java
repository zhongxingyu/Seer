 package org.imirsel.nema.webapp.webflow;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.imirsel.nema.contentrepository.client.CommandLineFormatter;
 import org.imirsel.nema.contentrepository.client.ResourceTypeService;
 import org.imirsel.nema.model.ExecutableBundle;
 import org.imirsel.nema.model.InvalidCommandLineFlagException;
 import org.imirsel.nema.model.JavaPredefinedCommandTemplate;
 import org.imirsel.nema.model.MatlabPredefinedCommandTemplate;
 import org.imirsel.nema.model.Param;
 import org.imirsel.nema.model.Path;
 import org.imirsel.nema.model.SysProperty;
 import org.imirsel.nema.model.VanillaPredefinedCommandTemplate;
 import org.imirsel.nema.webapp.model.DiyJavaTemplate;
 import org.imirsel.nema.webapp.model.DiyMatlabTemplate;
 import org.imirsel.nema.webapp.model.NiceParams;
 import org.imirsel.nema.webapp.model.UploadedExecutableBundle;
 import org.springframework.webflow.core.collection.MutableAttributeMap;
 import org.springframework.webflow.core.collection.ParameterMap;
 
 /**
  * Action class for webflow that generated the executable bundle
  * (task/executable)
  * 
  * @author gzhu1
  * @since 0.6.0
  * 
  */
 public class ExecutableServiceImpl {
 	static private Log logger = LogFactory.getLog(ExecutableServiceImpl.class);
 	private CommandLineFormatter commandLineFormatter;
 	private ResourceTypeService resourceServiceType;
 
 	
 	/**
 	 * set the input template into the webflow scope according to type
 	 * @param input
 	 * @param type
 	 * @param scope
 	 */
 	public void setTemplate(VanillaPredefinedCommandTemplate input,
 			ExecutableBundle.ExecutableType type,
 			MutableAttributeMap scope) {
 		logger.debug("get input template "+type);
 		if (type != null) {
 			switch (type) {
 			case SHELL:
 			case C:
 				scope.put("plainTemplate",input);
 				break;
 			case JAVA:
 				scope.put("javaTemplate",(DiyJavaTemplate) input);
 				break;
 			case MATLAB:
 				scope.put("matlabTemplate",(DiyMatlabTemplate) input);
 				break; 	
 			default:
 
 			}
 		}
 	}
 
 	/**
 	 * return the correct template (one of the three) according to type 
 	 * @param type
 	 * @param plainTemplate
 	 * @param javaTemplate
 	 * @param matlabTemplate
 	 * @return
 	 */
 	public VanillaPredefinedCommandTemplate selectTemplate(
 			ExecutableBundle.ExecutableType type,
 			VanillaPredefinedCommandTemplate plainTemplate,
 			JavaPredefinedCommandTemplate javaTemplate,
 			MatlabPredefinedCommandTemplate matlabTemplate) {
 		logger.debug("generate for output template "+type);
 		switch (type) {
 		case SHELL:
 		case C:
 			return plainTemplate;
 		case JAVA:
 			return javaTemplate;
 		case MATLAB:
 			return matlabTemplate;
 		default:
 			return null;
 		}
 	}
 
 	/**
 	 * generate a {@link NiceParams} object from a list of
 	 * {@org.imirsel.nema.model.Param}.
 	 * 
 	 * @param params
 	 * @return
 	 */
 	public NiceParams getNiceParams(List<Param> params) {
 		return new NiceParams(params);
 	}
 
 	public void generateExecutableBundle(
 			VanillaPredefinedCommandTemplate template,
 			UploadedExecutableBundle executable) {
 
 		executable.setId(UUID.randomUUID().toString());
 		try {
 			String command = commandLineFormatter.getCommandLineString(
 					template, resourceServiceType.getOsDataType(executable
 							.getPreferredOs()), false);
 			executable.setCommandLineFlags(command);
 			logger.debug("generated command flag:" + command);
 		} catch (InvalidCommandLineFlagException e) {
 			logger.error(e, e);
 		}
 		executable.setEnvironmentVariables(template.getEnvironmentMap());
 		logger.debug("set the environment variables (size:"
 				+ executable.getEnvironmentVariables().size() + ")");
 
 	}
 
 	/**
 	 * Set the special fields for JavaTemplate
 	 * 
 	 * @param httpParam
 	 * @param executable
 	 * @param template
 	 */
 	public void setJavaTemplate(ParameterMap httpParam,
 			UploadedExecutableBundle executable,
 			JavaPredefinedCommandTemplate template) {
 
 		List<Path> jarPaths = executable.getJarPaths();
		template.setClasspath(jarPaths);

 		String[] keys = getArray(httpParam, "sysVar");
 		String[] values = getArray(httpParam, "sysValue");
 		List<SysProperty> properties = new ArrayList<SysProperty>();
 		if ((keys != null) && (values != null)) {
 			int length = keys.length;
 			for (int i = 0; i < length; i++) {
 				if ((keys[i] != null) && (keys[i].length() > 0))
 					properties.add(new SysProperty(keys[i], values[i]));
 			}
 			template.setProperties(properties);
 			logger.debug("generate map of size " + length);
 		}
 
 	}
 
 	public UploadedExecutableBundle setExecutable(UploadedExecutableBundle a) {
 		if (a == null)
 			return new UploadedExecutableBundle();
 		else
 			return a;
 	}
 
 	/**
 	 * 
 	 * @param keys
 	 * @param values
 	 * @param inputs
 	 * @param outputs
 	 * @param others
 	 * @return
 	 */
 	private void setCommonTemplate(String[] keys, String[] values,
 			String[] inputs, String[] outputs, String[] others,
 			VanillaPredefinedCommandTemplate template) {
 		if (template == null)
 			throw new NullPointerException("template variable cannot be null");
 		logger.debug("start to populate command template " + output(keys)
 				+ " variable, " + output(inputs) + " inputs, "
 				+ output(outputs) + " outputs, " + output(others) + " other");
 		List<Param> params = new ArrayList<Param>();
 		int i = 0;
 		if (inputs != null)
 			for (String entry : inputs) {
 				if ((entry != null) && (entry.length() != 0))
 					params.add(new Param(entry, true, Param.ParamType.INPUT
 							.getCode(), i++));
 			}
 		if (outputs != null) {
 			i = 0;
 
 			for (String entry : outputs) {
 				if ((entry != null) && (entry.length() != 0))
 					params.add(new Param(entry, true, Param.ParamType.OUTPUT
 							.getCode(), i++));
 			}
 		}
 		if (others != null) {
 			i = 0;
 			for (String entry : others) {
 				if ((entry != null) && (entry.length() != 0))
 					params.add(new Param(entry, false, Param.ParamType.OTHER
 							.getCode(), i++));
 			}
 		}
 		template.setParams(params);
 		Map<String, String> map = new HashMap<String, String>();
 
 		if ((keys != null) && (values != null)) {
 			int length = keys.length;
 			for (i = 0; i < length; i++) {
 				if ((keys[i] != null) && (keys[i].length() > 0))
 					map.put(keys[i], values[i]);
 			}
 			logger.debug("generate map of environment variables size "
 					+ template.getEnvironmentMap().size());
 		}
 		template.setEnvironmentMap(map);
 
 	}
 
 	private String output(String[] array) {
 		return ((array == null) ? "null" : String.valueOf(array.length));
 	}
 
 	/**
 	 * Set the template fields of environment variables and params from Http
 	 * request parameters because their number is not known before-hand and
 	 * cannot be handled by Spring's binding
 	 * 
 	 * @param httpParam
 	 *            objects with http request parameters.
 	 * @param template
 	 *            Template to be filled by the http request parameter value
 	 */
 	public void setCommonTemplate(ParameterMap httpParam,
 			VanillaPredefinedCommandTemplate template) {
 		String[] keys = getArray(httpParam, "variable");
 		String[] values = getArray(httpParam, "value");
 		String[] inputs = getArray(httpParam, "input");
 		String[] others = getArray(httpParam, "other");
 		String[] outputs = getArray(httpParam, "output");
 		setCommonTemplate(keys, values, inputs, outputs, others, template);
 	}
 
 	private String[] getArray(ParameterMap httpParam, String key) {
 		if (httpParam.contains(key)) {
 			String[] values = httpParam.getArray(key);
 			logger.debug("parameter " + key + " map size " + values.length);
 			if (values.length == 0) {
 				values = new String[1];
 				values[0] = httpParam.get(key);
 				logger.debug("find an 1 value parameter " + values[0]);
 			}
 			return values;
 		} else {
 			return null;
 		}
 	}
 
 	public void setCommandLineFormatter(
 			CommandLineFormatter commandLineFormatter) {
 		this.commandLineFormatter = commandLineFormatter;
 	}
 
 	public CommandLineFormatter getCommandLineFormatter() {
 		return commandLineFormatter;
 	}
 
 	public ResourceTypeService getResourceServiceType() {
 		return resourceServiceType;
 	}
 
 	public void setResourceServiceType(ResourceTypeService resourceServiceType) {
 		this.resourceServiceType = resourceServiceType;
 	}
 
 }
