 /*
  * #%L
  * Java code for use with WiscScan.
  * %%
 * Copyright (C) 2008 - 2014 Board of Regents of the University of
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
 
 import java.io.IOException;
 
 import javax.xml.transform.Templates;
 
 import loci.common.xml.XMLTools;
 
 /**
  * Helper class for working with XML stylesheet transforms.
  *
  * @author Curtis Rueden
  * @author Abhinav Tallavajhula
  */
 public class XSLTTools {
 
 	/** Transforms the given XML string using the specified XSLT stylesheet. */
 	public static String transformXML(final String xml,
 		final String pathToStylesheet)
 	{
 		final Templates stylesheet =
 			XMLTools.getStylesheet(pathToStylesheet, null);
 		try {
 			return XMLTools.transformXML(xml, stylesheet);
 		}
 		catch (final IOException exc) {
 			return null;
 		}
 	}
 
 }
