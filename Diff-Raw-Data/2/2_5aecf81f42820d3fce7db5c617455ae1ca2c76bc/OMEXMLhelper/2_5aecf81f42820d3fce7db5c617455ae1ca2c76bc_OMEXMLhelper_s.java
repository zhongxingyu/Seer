 /*
  * #%L
  * Java code for use with WiscScan.
  * %%
  * Copyright (C) 2008 - 2013 Board of Regents of the University of
  * Wisconsin-Madison.
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 package loci.wiscscan;
 
 import loci.common.services.ServiceFactory;
 import loci.formats.ome.OMEXMLMetadata;
 import loci.formats.services.OMEXMLService;
 
 /**
  * Helper class for working with OME-XML.
  * 
  * @author Curtis Reuden
  * @author Ajeet Vivekanandan
  * @author David Mayer
  */
 public class OMEXMLhelper {
 
 	/**
 	 * Extracts only the OME-XML header and UUID for partial metadata block in
 	 * OME-XML metadata population in images.
 	 */
	public String getBinaryOnlyXML(final String metadataFile, final String uuid) {
 		try {
 			final ServiceFactory sf = new ServiceFactory();
 			final OMEXMLService omexmlService = sf.getInstance(OMEXMLService.class);
 			final OMEXMLMetadata omeMeta = omexmlService.createOMEXMLMetadata();
 			omeMeta.setBinaryOnlyMetadataFile(metadataFile);
 			omeMeta.setBinaryOnlyUUID(uuid);
 			return omexmlService.getOMEXML(omeMeta);
 		}
 		catch (Exception exc) {
 			// exception will be handled by caller
 			return null;
 		}
 	}
 
 }
