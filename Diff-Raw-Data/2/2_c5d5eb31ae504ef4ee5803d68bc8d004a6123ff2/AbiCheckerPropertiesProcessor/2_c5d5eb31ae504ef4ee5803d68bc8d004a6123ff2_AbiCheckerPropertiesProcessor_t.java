 /**
  * Copyright (c) 2012-2013 by European Organization for Nuclear Research (CERN)
  * Author: Justin Salmon <jsalmon@cern.ch>
  *
  * XRootD is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * XRootD is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with XRootD.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ch.cern.dss.teamcity.server;
 
 import ch.cern.dss.teamcity.common.AbiCheckerConstants;
 import jetbrains.buildServer.serverSide.InvalidProperty;
 import jetbrains.buildServer.serverSide.PropertiesProcessor;
 import jetbrains.buildServer.util.PropertiesUtil;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * Build runner properties validator.
  */
 public class AbiCheckerPropertiesProcessor implements PropertiesProcessor {
 
     /**
      * Validate the properties (settings) that the user specified in the web UI. The method determines which fields
      * should not be empty, and checks the existence and permissions of given executable files.
      *
      * @param properties the map of properties passed from the web UI form.
      *
      * @return a collection of InvalidProperties. Each invalid property has a key (which matches that given as the name
      *         attribute of the corresponding web UI element) and a message describing the reason for invalidity.
      */
     @Override
     public Collection<InvalidProperty> process(Map<String, String> properties) {
         final Collection<InvalidProperty> result = new ArrayList<InvalidProperty>();
 
         if (PropertiesUtil.isEmptyOrNull(properties.get(AbiCheckerConstants.REFERENCE_TAG))) {
             result.add(new InvalidProperty(AbiCheckerConstants.REFERENCE_TAG,
                    "Cannot reference a build type with no tagged builds"));
         }
 
         String executablePath = properties.get(AbiCheckerConstants.EXECUTABLE_PATH);
         if (PropertiesUtil.isEmptyOrNull(executablePath)) {
             result.add(new InvalidProperty(AbiCheckerConstants.EXECUTABLE_PATH,
                     "Path to abi-compliance-checker executable must be specified"));
         } else {
             File executableFile = new File(executablePath);
             if (!executableFile.exists() || !executableFile.canExecute()) {
                 result.add(new InvalidProperty(AbiCheckerConstants.EXECUTABLE_PATH,
                         "The given path doesn't exist, or is not executable"));
             }
         }
 
         if (PropertiesUtil.isEmptyOrNull(properties.get(AbiCheckerConstants.ARTIFACT_FILE_PATTERN))) {
             result.add(new InvalidProperty(AbiCheckerConstants.ARTIFACT_FILE_PATTERN,
                     "At least one artifact must be specified"));
         }
 
         if (PropertiesUtil.isEmptyOrNull(properties.get(AbiCheckerConstants.HEADER_FILE_PATTERN))) {
             result.add(new InvalidProperty(AbiCheckerConstants.HEADER_FILE_PATTERN,
                     "Path to at least one header inside artifact must be specified"));
         }
 
         if (PropertiesUtil.isEmptyOrNull(properties.get(AbiCheckerConstants.LIBRARY_FILE_PATTERN))) {
             result.add(new InvalidProperty(AbiCheckerConstants.LIBRARY_FILE_PATTERN,
                     "Path to at least one library inside artifact must be specified"));
         }
 
         return result;
     }
 }
