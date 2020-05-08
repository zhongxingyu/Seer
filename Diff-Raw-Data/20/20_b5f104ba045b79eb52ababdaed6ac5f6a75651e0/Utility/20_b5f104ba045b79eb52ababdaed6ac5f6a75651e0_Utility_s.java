 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.applications.rainbow.utilities.xsltransform;
 
 import java.io.File;
 import java.util.Map;
 
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import net.sf.okapi.applications.rainbow.utilities.BaseUtility;
 import net.sf.okapi.applications.rainbow.utilities.ISimpleUtility;
 import net.sf.okapi.common.ConfigurationString;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 
 public class Utility extends BaseUtility implements ISimpleUtility {
 
 	private Source xsltInput;
 	private Map<String, String> paramList;
 	private Transformer trans;
 	private Parameters params;
 	
 	public Utility () {
 		params = new Parameters();
 	}
 	
 	public String getName () {
 		return "oku_xsltransform"; //$NON-NLS-1$
 	}
 	
 	public void preprocess () {
 		try {
 			ConfigurationString cfgString = new ConfigurationString(
 				params.paramList);
 			paramList = cfgString.toMap();
 			
 			// Create the source for the XSLT
 			xsltInput = new javax.xml.transform.stream.StreamSource(
 				new File(params.xsltPath.replace(VAR_PROJDIR, projectDir)));
 			
 			// Create an instance of TransformerFactory
 			javax.xml.transform.TransformerFactory fact =
 				javax.xml.transform.TransformerFactory.newInstance();
 			trans = fact.newTransformer(xsltInput);
 		}
 		catch ( TransformerConfigurationException e ) {
 			throw new RuntimeException(Res.getString("utility.errorInXSLT"), e); //$NON-NLS-1$
 		}
 	}
 	
 	public void postprocess () {
 		// Nothing to do
 	}
 
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasParameters () {
 		return true;
 	}
 
 	public boolean isFilterDriven () {
 		return false;
 	}
 
 	public boolean needsRoots () {
 		return false;
 	}
 
 	public void setParameters (IParameters paramsObject) {
 		params = (Parameters)paramsObject;
 	}
 
 	public int requestInputCount () {
 		return 3; // between 1 and 3, depending on the template
 	}
 
 	public void processInput () {
 		try {
 			trans.reset();
 			fillParameters();
 			// Create the source for the XML input
 			Source xmlInput = new javax.xml.transform.stream.StreamSource(
 				new File(getInputPath(0)));
 			// Create the output
 			Result result = new javax.xml.transform.stream.StreamResult(
 				new File(getOutputPath(0)));
 			trans.transform(xmlInput, result);
 		}
 		catch ( TransformerException e ) {
 			throw new RuntimeException(Res.getString("utility.errorTransforming"), e); //$NON-NLS-1$
 		}
 	}
 
 	private void fillParameters () {
 		trans.clearParameters();
 		String value;
 		for ( String key : paramList.keySet() ) {
 			value = paramList.get(key).replace("${SrcLang}", srcLang); //$NON-NLS-1$
 			value = value.replace("${TrgLang}", trgLang); //$NON-NLS-1$
 			value = value.replace("${Input1}", Util.makeURIFromPath(getInputPath(0))); //$NON-NLS-1$
 			value = value.replace("${Output1}", Util.makeURIFromPath(getOutputPath(0))); //$NON-NLS-1$
 			if ( getInputPath(1) != null ) {
 				value = value.replace("${Input2}", Util.makeURIFromPath(getInputPath(1))); //$NON-NLS-1$
 				value = value.replace("${Output2}", Util.makeURIFromPath(getOutputPath(1))); //$NON-NLS-1$
 			}
 			if ( getInputPath(2) != null ) {
 				value = value.replace("${Input3}", Util.makeURIFromPath(getInputPath(2))); //$NON-NLS-1$
 				value = value.replace("${Output3}", Util.makeURIFromPath(getOutputPath(2))); //$NON-NLS-1$
 			}
 			trans.setParameter(key, value);
 		}
 	}
 
 }
