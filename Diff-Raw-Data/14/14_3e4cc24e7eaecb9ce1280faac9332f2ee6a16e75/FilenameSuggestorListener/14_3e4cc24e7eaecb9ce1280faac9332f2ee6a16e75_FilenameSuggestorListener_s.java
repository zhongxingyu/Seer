 /*******************************************************************************
  * Poor Man's CMS (pmcms) - A very basic CMS generating static html pages.
  * http://pmcms.sourceforge.net
  * Copyright (C) 2004-2013 by Thilo Schwarz
  * 
  * == BEGIN LICENSE ==
  * 
  * Licensed under the terms of any of the following licenses at your
  * choice:
  * 
  *  - GNU General Public License Version 2 or later (the "GPL")
  *    http://www.gnu.org/licenses/gpl.html
  * 
  *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl.html
  * 
  *  - Mozilla Public License Version 1.1 or later (the "MPL")
  *    http://www.mozilla.org/MPL/MPL-1.1.html
  * 
  * == END LICENSE ==
  ******************************************************************************/
 package de.thischwa.pmcms.gui.dialog.pojo;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * {@link ModifyListenerClearErrorMessages} to suggest a filename depending on a title.
  *
  * @version $Id: FilenameSuggestorListener.java 2210 2012-06-17 13:01:49Z th-schwarz $
  * @author <a href="mailto:th-schwarz@users.sourceforge.net">Thilo Schwarz</a>
  */
 public class FilenameSuggestorListener extends ModifyListenerClearErrorMessages {
 	private Text filenameText;
 	private Collection<String> forbiddenValues;
 	private Map<String, String> keyMapping; // just lowercased
 	private final static String unkownKeyMap = "_";
 	
 	
 	public FilenameSuggestorListener(DialogCreator dialogCreator, Text filenameText, Collection<String> forbiddenValues) {
 		super(dialogCreator);
 		this.filenameText = filenameText;
 		this.forbiddenValues = forbiddenValues;
 		
 		//fill the keyMapping
 		keyMapping = new HashMap<String, String>();
 		for(char c='a'; c<='z'; c++) {
 			keyMapping.put(String.valueOf(c), String.valueOf(c));
 			keyMapping.put(String.valueOf(c).toUpperCase(), String.valueOf(c));
 		}
 		for(char c='0'; c<='9'; c++)
 			keyMapping.put(String.valueOf(c), String.valueOf(c));
		keyMapping.put("", "ae");
		keyMapping.put("", "ue");
		keyMapping.put("", "oe");
		keyMapping.put("", "ae");
		keyMapping.put("", "ue");
		keyMapping.put("", "oe");
		keyMapping.put("", "ss");
 		
 		keyMapping.put("-", "-");
 		keyMapping.put("_", "_");
 		keyMapping.put(" ", "-");
 	}
 
 
 	@Override
 	public void modifyText(ModifyEvent e) {
 		super.modifyText(e);
 		Text titleText = (Text)e.widget;
 		String filename = suggest(titleText.getText());
 		filenameText.setText(filename);
 	}
 	
 	private String suggest(String title) {
 		if(StringUtils.isBlank(title))
 			return "";
 		
 		// build the filename
 		StringBuilder sb = new StringBuilder();
 		for(int i=0; i<title.length(); i++) {
 			char c = title.charAt(i);
 			String transChar = keyMapping.get(String.valueOf(c));
 			if(transChar == null)
 				transChar = unkownKeyMap;
 			sb.append(transChar);
 		}
 
 		// check, if filename can be used
 		String newFilename = sb.toString();
 		if(canBeUsed(newFilename))
 			return newFilename;
 		
 		// build unique name
 		int i = 1;
 		String tmp;
 		do {
 			tmp = String.format("%s_%s", newFilename, String.valueOf(i));
 		} while(!canBeUsed(tmp));
 		return tmp;
 	}
 	
 	private boolean canBeUsed(String filename) {
 		return (CollectionUtils.isEmpty(forbiddenValues) || !forbiddenValues.contains(filename));
 	}
 }
