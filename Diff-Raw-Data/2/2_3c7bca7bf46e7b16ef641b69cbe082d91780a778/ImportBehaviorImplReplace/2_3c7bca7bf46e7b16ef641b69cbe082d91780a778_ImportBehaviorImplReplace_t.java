 /**
  * $Id: ImportBehaviorImplReplace.java,v 1.1.1.1 2007/08/01 19:11:14 kasiedu Exp $
  * $Name:  $
  * 
  * Copyright (c) 2003  University of Massachusetts Boston
  *
  * Authors: Jacob K Asiedu
  *
  * This file is part of the UMB Electronic Field Guide.
  * UMB Electronic Field Guide is free software; you can redistribute it
  * and/or modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2, or
  * (at your option) any later version.
  *
  * UMB Electronic Field Guide is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the UMB Electronic Field Guide; see the file COPYING.
  * If not, write to:
  * Free Software Foundation, Inc.
  * 59 Temple Place, Suite 330
  * Boston, MA 02111-1307
  * USA
  */
 /**
  * A temporary object used in some of the stack operations Should be extended to
  * implement equals and hashcode if it is used as part of a Collection.
  */
 package project.efg.client.impl.gui;
 
 import javax.swing.JOptionPane;
 
 import org.apache.log4j.Logger;
 
 import project.efg.client.factory.gui.SpringGUIFactory;
 import project.efg.client.interfaces.gui.EFGDatasourceObjectListInterface;
 import project.efg.client.interfaces.gui.EFGDatasourceObjectStateInterface;
 import project.efg.client.interfaces.gui.ImportBehavior;
 import project.efg.client.interfaces.nogui.EFGDatasourceObjectInterface;
 import project.efg.util.interfaces.EFGImportConstants;
 
 /**
  * @author kasiedu
  *
  */
 public class ImportBehaviorImplReplace extends ImportBehavior {
 	static Logger log = null;
 	static {
 		try {
 			log = Logger.getLogger(ImportBehaviorImplReplace.class);
 		} catch (Exception ee) {
 		}
 	}
 	/**
 	 * 
 	 */
 	public ImportBehaviorImplReplace(EFGDatasourceObjectListInterface lists,
 			EFGDatasourceObjectInterface obj) {
 		super(lists, obj);
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see project.efg.Imports.efgInterface.ImportIntoDatabase#importIntoDatabase(project.efg.Imports.efgInterface.EFGDatasourceObjectListInterface, project.efg.util.interfaces.EFGDatasourceObjectInterface, java.lang.String)
 	 */
 	public EFGDatasourceObjectStateInterface importIntoDatabase() {
 		String message = null;
 		EFGDatasourceObjectStateInterface state =
 			SpringGUIFactory.getFailureObject();//use a fctory
 		
 		if (this.lists.getCount() > 0) {
 			String [] possibleValues = this.getAlphabeticallySortedList();
 			Object selectedValue =  JOptionPane.showInputDialog(null, "Select one", "Input",
 					JOptionPane.INFORMATION_MESSAGE, null, possibleValues,
 					possibleValues[0]);
 			if (selectedValue != null) {
 				log.debug("Selectedvalue: " + selectedValue.toString());
 				
 				int selected = JOptionPane.showConfirmDialog(null,
						"Current data will be replaced with the update. Proceed?" , "Update Datasource", JOptionPane.YES_NO_OPTION);
 				if(selected == JOptionPane.NO_OPTION){
 					return state;
 				}
 				this.obj.setTemplateDisplayName(selectedValue.toString()
 						.trim());
 				log.debug("DisplayName is: " + this.obj.getDisplayName());
 				this.obj.setDisplayName(selectedValue.toString()
 						.trim());
 				boolean bool = this.lists.addEFGDatasourceObject(this.obj, this);
 				if(bool){
 					state = SpringGUIFactory.getSuccessObject(); 
 					this.responseMessage.append(this.obj.getDisplayName() + " ");
 					message = EFGImportConstants.EFGProperties
 					.getProperty("SynopticKeyTree.update.success");
 					this.responseMessage.append(message);
 					log.debug(this.responseMessage.toString());
 				}
 			}
 			else{
 				state =SpringGUIFactory.getNeutralObject(); 
 				log.debug("Selectedvalue is null");
 				message = EFGImportConstants.EFGProperties
 					.getProperty("SynopticKeyTree.terminateImport");
 			this.responseMessage.append(message);
 			log.error(this.responseMessage.toString());
 			}
 		}
 		else{
 			message = EFGImportConstants.EFGProperties
 			.getProperty("SynopticKeyTree.updateTerminate");
 			this.responseMessage.append(message);
 			log.debug(this.responseMessage.toString());
 		}
 		this.obj.setState(state);
 		return state;
 	}
 }
