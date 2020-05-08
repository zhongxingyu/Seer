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
 
 package net.sf.okapi.applications.rainbow.lib;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.sf.okapi.common.IHelp;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IParametersEditor;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.ui.UIUtil;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class FilterAccess {
 	
 	private LinkedHashMap<String, FilterAccessItem> m_htFilters;
 	private String defaultEditor;
 	
 	/**
 	 * Construct a filter settings string.
 	 * @param filterID Filter identifier (cannot be null nor empty).
 	 * @param paramsName Name of the parameters file (can be null or empty).
 	 * @return Filter settings string.
 	 */
 	static public String buildFilterSettingsType1 (String filterID,
 		String paramsName)
 	{
 		String sTmp = filterID;
 		if (( paramsName != null ) && ( paramsName.length() > 0 ))
 			sTmp += (FilterSettingsMarkers.PARAMETERSSEP + paramsName);
 		return sTmp;
 	}
 
 	/**
 	 * Splits a filter settings type 1 string into its different components, including
 	 * the full path of the parameters file.
 	 * A type 1 filter settings string is: filterID@paramatersName
 	 * @param projectParamsFolder The project folder where the parameters files are stored.
 	 * @param filterSettings The setting string to split.
 	 * @return An array of 4 strings: 0=folder, 1=filter id, 2=parameters name
 	 * and 3=full parameters file path (folder + parameters name + extension).
 	 */
 	static public String[] splitFilterSettingsType1 (String projectParamsFolder,
 		String filterSettings)
 	{
 		String[] aOutput = new String[4];
 		for ( int i=0; i<4; i++ ) aOutput[i] = "";
 
 		if (( filterSettings == null ) || ( filterSettings.length() == 0 ))
 			return aOutput;
 
 		// Expand the parameters part into full path
 		aOutput[3] = projectParamsFolder + File.separator + filterSettings
 			+ FilterSettingsMarkers.PARAMETERS_FILEEXT;
 		
 		// Get the directory
 		File F = new File(aOutput[3]);
 		aOutput[0] = F.getParent();
 		String sTmp;
 		if ( aOutput[0] == null ) aOutput[0] = "";
 		if ( aOutput[0].length() > 0 )
 			sTmp = F.getName();
 		else
 			sTmp = aOutput[3];
 
 		// Get the parameters name
 		int n;
 		if ( (n = sTmp.indexOf(FilterSettingsMarkers.PARAMETERSSEP)) > -1 ) {
 			if ( n < sTmp.length()-1 ) {
 				aOutput[2] = sTmp.substring(n+1);
 				aOutput[2] = Utils.removeExtension(aOutput[2]);
 			}
 			sTmp = sTmp.substring(0, n);
 			
 		}
 		else aOutput[3] = "";
 
 		// Get the filter identifier
 		aOutput[1] = Utils.removeExtension(sTmp);
 		
 		return aOutput;
 	}
 
 	/**
 	 * Gets the list of all parameters files in a given folder.
 	 * @param paramsFolder The folder to where to get the files.
 	 * @return The list of filter settings strings corresponding to the 
 	 * parameters files found.
 	 */
 	static public String[] getParametersList (String paramsFolder) {
 		File D = new File(paramsFolder);
 		String aRes[] = D.list(new DefaultFilenameFilter(FilterSettingsMarkers.PARAMETERS_FILEEXT));
 		if ( aRes == null ) aRes = new String[0];
 		// Remove the extensions
 		for ( int i=0; i<aRes.length; i++ ) {
 			aRes[i] = Util.getFilename(aRes[i], false);
 		}
 		return aRes;
 	}
 
 	public FilterAccess () {
 		m_htFilters = new LinkedHashMap<String, FilterAccessItem>();
 	}
 	
 	/**
 	 * Loads the list of accessible filters.
 	 * The list is stored in an XML file of the following format:
 	 * <okapiFilters>
 	 *  <filter id="okf_regex"
 	 *   inputFilterClass="net.sf.okapi.filters.regex.Filter"
 	 *   editorClass="net.sf.okapi.filters.ui.regex.Editor"
 	 *  >Regular Expressions</filter>
 	 * </okapiFilters>
 	 * @param p_sPath Full path of the list file to load.
 	 */
 	public void loadList (String p_sPath) {
 		try {
 			DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
 			Fact.setValidating(false);
 			Document doc = Fact.newDocumentBuilder().parse(new File(p_sPath));
 			NodeList list = doc.getElementsByTagName("filter");
 			m_htFilters.clear();
 			FilterAccessItem item;
 			for ( int i=0; i<list.getLength(); i++ ) {
 				Node node = list.item(i).getAttributes().getNamedItem("id");
 				if ( node == null ) throw new RuntimeException("The attribute 'id' is missing.");
 				item = new FilterAccessItem();
 				item.id = Util.getTextContent(node);
 
 				node = list.item(i).getAttributes().getNamedItem("inputFilterClass");
 				if ( node == null ) throw new RuntimeException("The attribute 'inputFilterClass' is missing.");
 				item.inputFilterClass = Util.getTextContent(node);
 				
 				node = list.item(i).getAttributes().getNamedItem("editorClass");
 				if ( node != null ) item.editorClass = Util.getTextContent(node);
 				node = list.item(i).getAttributes().getNamedItem("editorClass");
 
 				node = list.item(i).getAttributes().getNamedItem("name");
 				if ( node != null ) item.name = Util.getTextContent(node);
 				else item.name = item.id;
 
 				item.description = Util.getTextContent(list.item(i));
 				m_htFilters.put(item.id, item);
 			}
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( ParserConfigurationException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( SAXException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Sets the default program to use to edit parameters file when
 	 * they do not have a dedicated editor.
 	 * @param editor The default program to use to edit parameters files.
 	 */
 	public void setDefaultEditor (String editor) {
 		defaultEditor = editor;
 	}
 	
 	/**
 	 * Loads a filter and its parameters (if necessary). If the filter is the 
 	 * filter currently loaded, it is not re-loaded. 
 	 * @param filterID Identifier of the filter to load.
 	 * @param paramPath Full path of the parameters file to load. Use null
 	 * for not loading any parameters file.
 	 * @param previousInputFilter An optional previous instance of the input filter
 	 * it will be re-used if the one requested is the same. Use null to force
 	 * reloading the filter or if there is no previous instance available.
 	 * @return The filter requested.
 	 */
 	public IFilter loadFilter (String filterID,
 		String paramPath,
 		IFilter previousInputFilter)
 	{
 		IFilter newFilter = previousInputFilter;
 		try {
 			// If the filter ID starts with NNN. (e.g. 123.okf_xml...)
 			// we remove the NNN. part. That part is reserved for multi-file storage info
 			if ( Character.isDigit(filterID.charAt(0)) ) {
 				int n = filterID.indexOf('.');
 				if ( n != -1 ) filterID = filterID.substring(n+1);
 			}
 
 			// Map the ID to the class, and instantiate the filter
 			if ( !m_htFilters.containsKey(filterID) )
 				throw new RuntimeException(String.format(Res.getString("UNDEF_FILTERID"), filterID)); 
 
 			// Load if not already done
 			boolean bLoad = true;
 			if ( previousInputFilter != null ) {
 				String s = previousInputFilter.getClass().getName();
 				bLoad = !s.equals(m_htFilters.get(filterID).inputFilterClass);
 			}
 			if ( bLoad ) {
 				newFilter = (IFilter)Class.forName(m_htFilters.get(filterID).inputFilterClass).newInstance();
 			}
 
 			// Load the parameters
 			IParameters params = newFilter.getParameters();
 			if ( params != null ) { // Not all filters have parameters
 				if (( paramPath != null ) && ( paramPath.length() > 0 )) {
 					params.load(paramPath, false);
 				}
 				else {
 					params.reset();
 				}
 			}
 		}
 		catch ( ClassNotFoundException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( IllegalAccessException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( InstantiationException e ) {
 			throw new RuntimeException(e);
 		}
 		return newFilter;
 	}
 	
 	public IFilter loadFilterFromFilterSettingsType1 (String projectParamsFolder,
 		String filterSettings,
 		IFilter previousInputFilter)
 	{
 		String[] aRes = splitFilterSettingsType1(projectParamsFolder, filterSettings);
 		return loadFilter(aRes[1], aRes[3], previousInputFilter);
 	}
 		
 	public IParametersEditor loadEditor (String filterID) {
 		try {
 			// If the filter ID starts with NNN. (e.g. 123.okf_xml...)
 			// we remove the NNN. part. That part is reserved for multi-file storage info
 			if ( Character.isDigit(filterID.charAt(0)) ) {
 				int n = filterID.indexOf('.');
 				if ( n != -1 ) filterID = filterID.substring(n+1);
 			}
 	
 			// Map the ID to the class, and instantiate the editor
 			if ( !m_htFilters.containsKey(filterID) )
 				throw new RuntimeException(String.format(Res.getString("UNDEF_FILTERID"), filterID));
 			String classPath = m_htFilters.get(filterID).editorClass;
 			if ( classPath == null ) return null;
 			return (IParametersEditor)Class.forName(classPath).newInstance();
 		}
 		catch ( InstantiationException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( IllegalAccessException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( ClassNotFoundException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * Edits the parameters for a given filter. On success the 
 	 * content of the parameters object is changed.
 	 * @param filterID The identifier of the filter to use to edit the parameters.
 	 * @param paramObject The parameter to edit.
 	 * @param uiContext Object used by the editor to integrate itself with the 
 	 * UI that called the method. In this implementation pass the SWT Shell object 
 	 * of the calling application.
 	 * @return True if the edit was successful, false if the use canceled
 	 * or if an error occurred.
 	 */
 	//TODO: Rethink the error handling
 	public boolean editParameters (String filterID,
 		IParameters paramObject,
 		Object uiContext,
 		IHelp helpParam,
 		String paramsPath,
 		String projectDir)
 	{
 		IParametersEditor paramsEditor = loadEditor(filterID);
 		boolean result = false;
 		if ( paramsEditor == null ) {
 			if ( defaultEditor != null ) {
				paramObject.save(paramsPath);
 				// Call the default editor
 				UIUtil.execute(defaultEditor, paramsPath);
 				result = true;
 			}
 		}
 		else result = paramsEditor.edit(paramObject, uiContext, helpParam, projectDir);
 		return result;
 	}
 	
 	public Map<String, FilterAccessItem> getItems () {
 		return m_htFilters;
 	}
 }
