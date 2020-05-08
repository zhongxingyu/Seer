 package com.netspective.sparx.report.tabular;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Class for html report actions
  *
  * @author aye
 * $Id: HtmlReportActions.java,v 1.2 2003-07-12 02:14:08 aye.thu Exp $
  */
 public class HtmlReportActions
 {
     private List actions = new ArrayList();
     private Map actionsMap =  new HashMap();
 
     public HtmlReportActions()
     {
     }
 
     /**
      * Gets the all the actions as a list
      * @return
      */
     public List getActions()
     {
         return actions;
     }
 
     public Map getActionMap()
     {
         return actionsMap;
     }
 
     /**
      * Gets a new html report action
      * @return
      */
     public HtmlReportAction createAction()
     {
         return new HtmlReportAction();
     }
 
     public void setActions(List actions)
     {
         this.actions = actions;
     }
 
     /**
      * Gets a report action by index
      * @param type
      * @return
      */
     public HtmlReportAction get(int type)
     {
         return (HtmlReportAction) actions.get(type);
     }
 
     /**
      * Gets a report action by value
      * @return
      */
     public HtmlReportAction get(String value)
     {
         return (HtmlReportAction) actionsMap.get(value);
     }
 
     /**
      * Adds a report action using its type as the index
      * @param action
      */
     public void addAction(HtmlReportAction action)
     {
         actions.add(action);
         actionsMap.put(action.getType().getValue(), action);
     }
 
     public int size()
     {
         return actions.size();
     }
 }
