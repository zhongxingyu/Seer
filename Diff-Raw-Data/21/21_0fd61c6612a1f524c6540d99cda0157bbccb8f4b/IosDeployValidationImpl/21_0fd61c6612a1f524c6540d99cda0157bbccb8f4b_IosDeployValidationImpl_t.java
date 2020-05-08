 package com.photon.phresco.framework.param.impl;
 
 import java.io.*;
 import java.util.*;
 
 import javax.xml.parsers.*;
 
 import org.apache.commons.collections.*;
 import org.sonatype.aether.util.*;
 import org.xml.sax.*;
 
 import com.photon.phresco.api.*;
 import com.photon.phresco.commons.model.*;
 import com.photon.phresco.exception.*;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.*;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.*;
import com.photon.phresco.plugins.util.*;
 import com.photon.phresco.util.*;
 
 public class IosDeployValidationImpl implements DynamicParameter {
 	private String BUILD_INFO_FILE_NAME = "build.info";
 	private String DO_NOT_CHECKIN_DIR = "do_not_checkin";
 	private String BUILD = "build";
    private String FALSE = "false";
	private String TRUE = "true";
 	
 	public PossibleValues getValues(Map<String, Object> paramsMap)
 			throws IOException, ParserConfigurationException, SAXException,
 			ConfigurationException, PhrescoException {
 		PossibleValues possibleValues = new PossibleValues();
 		try {
             ApplicationInfo applicationInfo = (ApplicationInfo) paramsMap.get(KEY_APP_INFO);
             String buildNumber = (String) paramsMap.get(KEY_BUILD_NO);
             if (StringUtils.isEmpty(buildNumber)) {
             	throw new PhrescoException("Build number is empty ");
             }
             
             BuildInfo buildInfo = Utility.getBuildInfo(Integer.parseInt(buildNumber), getBuildInfoPath(applicationInfo.getAppDirName()).toString());
             if (buildInfo == null) {
             	throw new PhrescoException("Build info is not found for build number " + buildNumber);
             }
             
             Map<String, Boolean> options = buildInfo.getOptions();
             if (options != null) {
             	boolean createIpa = MapUtils.getBooleanValue(buildInfo.getOptions(), "canCreateIpa");
             	boolean deviceDeploy = MapUtils.getBooleanValue(buildInfo.getOptions(), "deviceDeploy");
             	
             	if (!createIpa && !deviceDeploy) { // if it is simulator, show popup for following dependency
             		Value value = new Value();
                     value.setValue("simulator");
                     value.setDependency("sdkVersion,family,logs,buildNumber");
                     possibleValues.getValue().add(value);
                    // set trigger simulator value
                    setTriggerSimulatorValue(paramsMap, TRUE);
                     return possibleValues;
             	} else { // if it is device, it should return null and should not show any popup
             		Value value = new Value();
                     value.setValue("device");
                     possibleValues.getValue().add(value);
                    // set trigger simulator value
                    setTriggerSimulatorValue(paramsMap, FALSE);
             		return possibleValues;
             	}
             }
             
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return possibleValues;
 	}
 
	private void setTriggerSimulatorValue(Map<String, Object> paramsMap, String triggerSimulator) throws PhrescoException {
    	MojoProcessor mojo = (MojoProcessor) paramsMap.get(KEY_MOJO);
    	String goal = (String) paramsMap.get(KEY_GOAL);
    	Parameter parameter = mojo.getParameter(goal, KEY_TRIGGER_SIMULATOR);
		parameter.setValue(triggerSimulator);
    	mojo.save();
	}
	
 	private StringBuilder getBuildInfoPath(String projectDirectory) {
 	    StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 	    builder.append(projectDirectory);
 	    builder.append(File.separator);
 	    builder.append(DO_NOT_CHECKIN_DIR);
 	    builder.append(File.separator);
 	    builder.append(BUILD);
 	    builder.append(File.separator);
 	    builder.append(BUILD_INFO_FILE_NAME);
 	    return builder;
 	}
 }
