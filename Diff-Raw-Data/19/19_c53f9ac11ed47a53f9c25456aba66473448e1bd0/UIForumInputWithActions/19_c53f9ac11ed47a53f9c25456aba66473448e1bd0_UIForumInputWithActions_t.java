 /***************************************************************************
  * Copyright (C) 2003-2008 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  ***************************************************************************/
 package org.exoplatform.forum.webui.popup;
 
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import org.exoplatform.forum.ForumUtils;
 import org.exoplatform.webui.application.WebuiRequestContext;
 import org.exoplatform.webui.core.UIComponent;
 import org.exoplatform.webui.form.UIForm;
 import org.exoplatform.webui.form.UIFormDateTimeInput;
 import org.exoplatform.webui.form.UIFormInputBase;
 import org.exoplatform.webui.form.UIFormInputSet;
 /**
  * Created by The eXo Platform SAS
  * Author : Vu Duy Tu
  *          tu.duy@exoplatform.com
  * Jun 9, 2008 - 10:31:58 AM  
  */
 public class UIForumInputWithActions extends UIFormInputSet {
 
   Map<String, List<ActionData>> actionField = new HashMap<String, List<ActionData>> () ;
   public UIForumInputWithActions(String id) {
     super.setId(id) ;
   }
   
   public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
     actionField.put(fieldName, actions) ;
   }
   
   @SuppressWarnings("unchecked")
   public void processRender(WebuiRequestContext context) throws Exception {
     if(getComponentConfig() != null) {
       super.processRender(context) ;
       return ;
     }
     UIForm uiForm = getAncestorOfType(UIForm.class);
     Writer w = context.getWriter() ;
     w.write("<div id=\"" + getId() + "\" class=\"UIFormInputSet " + getId() + "\">") ;
     w.write("<table class=\"UIFormGrid\">") ;
     ResourceBundle res = context.getApplicationResourceBundle() ;
     
     for(UIComponent inputEntry :  getChildren()) {
     	if(!inputEntry.isRendered()) continue ;
       String label ;
       try {
         label = uiForm.getLabel(res, inputEntry.getId());
         if(inputEntry instanceof UIFormInputBase) ((UIFormInputBase)inputEntry).setLabel(label);
       } catch(MissingResourceException ex){
         label = inputEntry.getId() ;
         System.err.println("\n "+uiForm.getId()+".label." + inputEntry.getId()+" not found value");
       }
       w.write("<tr>") ;
       w.write("<td class=\"FieldLabel\">") ; w.write(label); w.write("</td>") ;
       w.write("<td class=\"FieldComponent\">") ; renderUIComponent(inputEntry) ; 
       List<ActionData> actions = actionField.get(inputEntry.getName()) ;
       if(actions != null) {
         for(ActionData action : actions) {
           String actionLabel ;
           try{
             actionLabel = uiForm.getLabel(res, "action." + action.getActionName())  ;
           }catch(MissingResourceException ex) {
             actionLabel = action.getActionName() ;
           }
           String actionLink ;
           if(action.getActionParameter() != null) {
             actionLink = ((UIComponent)getParent()).event(action.getActionListener(), action.getActionParameter()) ;
           }else {
             actionLink = ((UIComponent)getParent()).event(action.getActionListener()) ;
           }
           w.write("<a title=\"" + actionLabel + "\" href=\"" + actionLink +"\">") ;
           if(action.getActionType() == ActionData.TYPE_ICON) {
             w.write("<img src=\"/eXoResources/skin/DefaultSkin/background/Blank.gif\" class=\"" + action.getCssIconClass()+"\"/>") ;
             if(action.isShowLabel) w.write(ForumUtils.getSubString(actionLabel, 30)) ;
           }else if(action.getActionType() == ActionData.TYPE_LINK){
             w.write(ForumUtils.getSubString(actionLabel, 30)) ;
           }else if(action.getActionType() == ActionData.TYPE_ATT){
           	String size = "";
           	if(actionLabel.lastIndexOf("(") > 0) {
           		size = actionLabel.substring(actionLabel.lastIndexOf("(")) ;
           		actionLabel = actionLabel.substring(0,actionLabel.lastIndexOf("(")) ;
           	}
           	String type = "";
           	int dot = actionLabel.lastIndexOf(".");
           	if(dot > 0) {
           		type = actionLabel.substring(dot) ;
           		actionLabel = actionLabel.substring(0,dot) ;
           	}
           	actionLabel = ForumUtils.getSubString(actionLabel, 30) + type + size ;
           	w.write("<img src=\"/eXoResources/skin/DefaultSkin/background/Blank.gif\" class=\"" + action.getCssIconClass()+"\"/>") ;
           	if(action.isShowLabel)w.write(actionLabel) ;
           }
           w.write("</a>") ; w.write("&nbsp;") ; 
           if(action.isBreakLine()) w.write("<br/>") ; 
         }
       }
       w.write("</td>") ;
       w.write("</tr>") ;
     }
     w.write("</table>") ;
     w.write("</div>") ;    
   }
   
   public UIFormDateTimeInput getUIFormDateTimeInput(String name) {
   	return (UIFormDateTimeInput) findComponentById(name) ;
   }
   
   static public class ActionData {
     final public static int TYPE_ICON = 0 ;
     final public static int TYPE_LINK = 1 ;
     final public static int TYPE_ATT = 2 ;
     
     private int actionType = 0 ;
     private String actionName ;
     private String actionListener ;
     private String actionParameter = null ;
     private String cssIconClass = "AddNewNodeIcon" ;
     private boolean isShowLabel = false ;
     private boolean isBreakLine = false ;
     
     public void setActionType(int actionType) { this.actionType = actionType ; }
     public int getActionType() { return actionType; }
     
     public void setActionName(String actionName) { this.actionName = actionName; }
     public String getActionName() { return actionName; }
     
     public void setActionListener(String actionListener) { this.actionListener = actionListener; }
     public String getActionListener() { return actionListener; }
     
     public void setActionParameter(String actionParameter) { this.actionParameter = actionParameter ; }
     public String getActionParameter() { return actionParameter ; }
     
     public void setCssIconClass(String cssIconClass) { this.cssIconClass = cssIconClass; }
     public String getCssIconClass() { return cssIconClass; }
     
     public void setShowLabel(boolean isShowLabel) { this.isShowLabel = isShowLabel ; }
     public boolean isShowLabel() { return isShowLabel ; }
     
     public void setBreakLine(boolean isBreakLine) { this.isBreakLine = isBreakLine ; }
     public boolean isBreakLine() { return isBreakLine ; }
   }
 }
