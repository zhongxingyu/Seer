 /*******************************************************************************
  * Copyright (C) 2007 The University of Manchester   
  * 
  *  Modifications to the initial code base are copyright of their
  *  respective authors, or their employers as appropriate.
  * 
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public License
  *  as published by the Free Software Foundation; either version 2.1 of
  *  the License, or (at your option) any later version.
  *    
  *  This program is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *    
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  ******************************************************************************/
 package org.cagrid.cql.actions;
 
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JOptionPane;
 
 import net.sf.taverna.t2.reference.ExternalReferenceSPI;
 import org.cagrid.cql.CQLActivity;
 import org.cagrid.cql.CQLConfigurationBean;
 import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
 import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
 import org.cagrid.cql.cqlbuilder.gui.CQLBuilderMAIN;
 import org.cagrid.cql.cqlbuilder.gui.SavedQueryWindowState;
 import org.cagrid.cql.cqlbuilder.utils.Utils;
 
 public class CQLActivityConfigurationAction extends ActivityConfigurationAction<CQLActivity, CQLConfigurationBean> {
 
     private static final long serialVersionUID = 2518716617809186972L;
     private final Frame owner;
 
     public CQLActivityConfigurationAction(CQLActivity activity, Frame owner) {
         super(activity);
         this.owner = owner;
     }
 
    
 
     public void actionPerformed(ActionEvent e) {
         System.out.println("entering CQLAcitivtyConfigurationAction.actionPerformed(..)");
         CQLConfigurationBean bean = new CQLConfigurationBean();
         
         String oldSavedQuery = getActivity().getConfiguration().getValue();
         String oldUrl = getActivity().getConfiguration().getServicesUrl();
         boolean oldIsManual = getActivity().getConfiguration().isIsQueryManual();
         
         CQLBuilderMAIN builderWindow = new CQLBuilderMAIN(oldUrl,oldSavedQuery,oldIsManual);
         String newValue = builderWindow.getOutputValueCQLQuery();
         String newUrl = builderWindow.getOutputValueUrl();
         boolean newIsManual = builderWindow.getOutputValueIsManual();        
         System.out.println("NEW VALUE: " + newValue);
         if (newValue != null) {
             bean.setValue(newValue);
             bean.setIsQueryManual(newIsManual);
             bean.setServicesUrl(newUrl);
             
             //TODO set number of input from CQLBuilderMAIN
             System.out.println("configure the input ports");
             List<ActivityInputPortDefinitionBean> inputBeanList = new ArrayList<ActivityInputPortDefinitionBean>();       
             ArrayList<String> varList = Utils.getVariableList(newValue);
             System.out.println("After parsing, get the number of input ports:"+varList.size());
             bean.setNumberOfInput(varList.isEmpty()?0:varList.size());          
             for(int i=0;i<varList.size();i++){
                 ActivityInputPortDefinitionBean inputBean = new ActivityInputPortDefinitionBean();
 		inputBean.setAllowsLiteralValues(true);
 		inputBean.setDepth(0);
 		List<Class<? extends ExternalReferenceSPI>> handledReferenceSchemes = new ArrayList<Class<? extends ExternalReferenceSPI>>();
 		// handledReferenceSchemes.add(FileReference.class);
 		inputBean.setHandledReferenceSchemes(handledReferenceSchemes);
 		List<String> mimeTypes = new ArrayList<String>();
 		mimeTypes.add("text/plain");
 		inputBean.setMimeTypes(mimeTypes);
                 inputBean.setName(varList.get(i));
                 inputBean.setTranslatedElementType(String.class);
                 inputBeanList.add(inputBean);
             }
 
              
             ////////////////////////////////
             bean.setInputPortDefinitions(inputBeanList);
            //System.out.println("there is a input port: "+bean.getInputPortDefinitions().get(0).getName());
             configureActivity(bean);
         }
     }
 }
