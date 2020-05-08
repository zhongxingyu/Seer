 package com.pace.base.ui;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafConfigFileNotFoundException;
 import com.pace.base.utility.PafXStream;
 
 
 public class PrintStyles implements IPafMapModelManager {
 
 	private static Logger logger = Logger.getLogger(PrintStyles.class);
	protected Map<String, PrintStyle> printStyles = new HashMap<String, PrintStyle>();
 	protected String projectFolder;
 	public PrintStyles() {
 		super();
 	}
 
 	public PrintStyles(String projectFolder) {
 		this.projectFolder = projectFolder;
 		load();
 	}
 	
 	@Override
 	public void load() {
 		try {
 			PrintStyles ps = (PrintStyles) PafXStream.importObjectFromXml(projectFolder + File.separator + PafBaseConstants.FN_PrintStyles);
 			printStyles = ps.getPrintStyles();
 		} catch (PafConfigFileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 
 	@Override
 	public void save() {
 		PrintStyles ps = new PrintStyles();
 		ps.setPrintStyles(printStyles);
 		PafXStream.exportObjectToXml(ps, projectFolder + File.separator + PafBaseConstants.FN_PrintStyles);
 	}
 
 	
 	@Override
 	public void add(String guid, Object object) {
 		printStyles.put(guid, (PrintStyle) object);
 	}
 
 	@Override
 	public void remove(String guid) {
 		if ( printStyles.containsKey(guid)) {
 			logger.debug("Removing guid: " + guid + " from model");
 			printStyles.remove(guid);
 		} else {
 			logger.debug("Can't remove guid: " + guid
 					+ " from model because view does not exists.");
 		}
 		
 	}
 
 	@Override
 	public String[] getKeys() {
 		String[] guids = null;
 		
 		guids = new String[printStyles.keySet().size()];
 
 		int i = 0;
 
 		for (Object guid : printStyles.keySet()) {
 			guids[i++] = (String) guid;
 		}
 
 		return guids;
 	}
 
 	@Override
 	public Object getItem(String guid) {
 		return printStyles.get(guid);
 	}
 
 	@Override
 	public int getIndex(String guid) {
 		int index = 0;
 
 		for (Object guidValue : printStyles.keySet()) {
 			if (guidValue.equals(guid)) {
 				break;
 			} else {
 				index++;
 			}
 		}
 		
 		return index;
 	}
 
 	@Override
 	public boolean contains(String guid) {
 		return printStyles.containsKey(guid);
 	}
 
 	@Override
 	public void replace(String guid, Object object) {
 		if ( printStyles.containsKey(guid)) {
 			printStyles.put(guid, (PrintStyle) object);
 		}		
 	}
 
 	public Set<String> getKeySet() {
 		return printStyles.keySet();
 	}
 
 	public Map<String, PrintStyle> getPrintStyles() {
 		return this.printStyles;
 	}
 
 	public void setPrintStyles(Map<String, PrintStyle> printStyles) {
 		this.printStyles = printStyles;
 	}
 	
 	public boolean isEmpty() {
 		if ( printStyles.size() > 0 ) {
 			return false;
 		}		
 		return true;
 	}
 	
 	public String[] getSortedKeys() {
 		String[] sortedKeys = this.getKeys();
 		if ( sortedKeys != null ) {
 			Arrays.sort(sortedKeys);
 		}
 		return sortedKeys;
 	}
 
 	public boolean containsIgnoreCase(String arg0) {
 		Object[] objAr = printStyles.keySet().toArray();
 		if ( objAr != null ) {
 			for (Object obj : objAr) {
 				if ( obj instanceof String) {
 					String guid = (String) obj;
 					if ( guid.equalsIgnoreCase(arg0)) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public int size() {
 		return printStyles.size();
 	}
 	
 	public String[] getNames(boolean withDefaultMarker) {
 		return null; //need to be overriden by derived class
 	}
 	
 	public PrintStyle getPrintStyleByGUID( String guid ) {
 		return (PrintStyle)printStyles.get(guid);
 	}
 	
 	public String getNameByGUID( String guid ) {
 		return getPrintStyleByGUID(guid).getName();
 	}
 	
 	public String getGUIDByName( String name ) {
 		return getPrintStyleByName(name).getGUID();
 	}
 
 	public String getProjectFolder() {
 		return this.projectFolder;
 	}
 
 	public void setProjectFolder(String projectFolder) {
 		this.projectFolder = projectFolder;
 	}
 	
 	public boolean isPrintStyleDefaultPrintStyleGivenGUID( String guid ) {
 		PrintStyle printStyle = (PrintStyle)getItem(guid);
 		if( printStyle.getDefaultStyle() )
 			return true;
 		return false;
 	}
 
 	public boolean isPrintStyleDefaultPrintStyleGivenName( String name ) {
 		PrintStyle printStyle = getPrintStyleByName(name);
 		if( printStyle.getDefaultStyle() )
 			return true;
 		return false;
 	}
 	
 	public Boolean findDefaultPrintStyle() {
 		//check if any default print style exists
 		for (String guid : getKeys()) {
 			if ( ((PrintStyle) printStyles.get(guid)).getDefaultStyle() ) {
 				return true;
 			} 
 		}
 		return false;
 	}
 
 	public PrintStyle getDefaultPrintStyle() {
 		for (String guid : getKeys()) {
 			PrintStyle printStyle = (PrintStyle) printStyles.get(guid);
 			if ( printStyle.getDefaultStyle() ) {
 				return printStyle;
 			} 
 		}
 		return null;
 	}
 	
 	public void setDefaultPrintStyle(String name, boolean setDefault) {
 		String guid = getGUIDByName(name);
 		if ( printStyles.containsKey(guid) && setDefault) {
 
 			//unset all of print styles
 			unsetDefaultPrintStyle();
 			
 			//set new default
 			((PrintStyle) printStyles.get(guid)).setDefaultStyle(true);		
 			
 		//if not setting default, just turn it off
 		} else if (! setDefault ) {
 			//unset default print style
 			((PrintStyle) printStyles.get(guid)).setDefaultStyle(false);		
 		}
 		save();
 	}
 	
 	private void unsetDefaultPrintStyle() {
 		//disable old default
 		for (String guid : getKeys()) {
 			
 			if ( ((PrintStyle) printStyles.get(guid)).getDefaultStyle()) {
 				
 				((PrintStyle) printStyles.get(guid)).setDefaultStyle(false);	
 				break;
 			} 
 			
 		}
 	}
 	
 	public void renamePrintStyle(String oldPrintStyleName, String newPrintStyleName) {
 		for (String guid : getKeys()) {			
 			if ( guid instanceof String ) {
 				//if the current key == the old view name.
 				String name = getNameByGUID(guid);
 				if ( name.equalsIgnoreCase(oldPrintStyleName) ) {
 					//get a temporary PafAdminConsoleView object for the current key 
 					PrintStyle printStyle = (PrintStyle) printStyles.get(guid);
 					//set the new name.
 					printStyle.setName(newPrintStyleName);
 					//remove old object from map and put the new one in.
 					remove(guid);
 					//new the temporary object in the map.
 					printStyles.put(guid, printStyle);
 				} 
 			}		
 		}
 		save();
 		load();
 	}
 
 	public PrintStyle getPrintStyleByName( String name ) {
 		for (String guid : getKeys()) {
 			PrintStyle printStyle = (PrintStyle) printStyles.get(guid);
 			if ( printStyle.getName().equalsIgnoreCase(name)) {
 				return printStyle;
 			} 
 		}
 		return null;
 	}
 	
 	public boolean findDulicatePrintStyleName(String name) {
 	    ArrayList<String> printStyleNames = new ArrayList<String>(Arrays.asList(getNames(false))); 
 		if( printStyleNames.contains(name))
 			return true;
 		return false;
 	}
 	
 	public PrintStyle findDuplicatePrintStyle( PrintStyle printStyleSource ) {
 		for (String guid : getKeys()) {
 			PrintStyle printStyle = (PrintStyle) printStyles.get(guid);
 			if ( ! printStyle.getGUID().equals(printStyleSource.getGUID()) 
 					&& printStyle.getName().equalsIgnoreCase(printStyleSource.getName())) {
 				return printStyle;
 			} 
 		}
 		return null;
 	}
 	
 }
