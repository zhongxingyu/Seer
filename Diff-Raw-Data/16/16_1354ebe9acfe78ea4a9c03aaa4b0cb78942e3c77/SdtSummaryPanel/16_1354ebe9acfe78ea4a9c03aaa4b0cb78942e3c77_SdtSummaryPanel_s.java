 package org.hackystat.projectbrowser.page.sensordata;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.PropertyModel;
 import org.hackystat.projectbrowser.ProjectBrowserSession;
 
 /**
  * A panel for holding the SDT summary information. 
  * @author Philip Johnson
  */
 public class SdtSummaryPanel extends Panel {
 
   /** For serialization */
   private static final long serialVersionUID = 1L;
   
   //private SensorDataPage page = null;
 
   /**
    * Creates a panel to display summary information. 
    * @param id The wicket ID. 
    * @param page The page associated with this panel.
    */
   public SdtSummaryPanel(String id, SensorDataPage page) {
     super(id);
     //this.page = page;
     SensorDataSession session = ProjectBrowserSession.get().getSensorDataSession();
     SdtSummaryModel model = session.getSdtSummaryModel();
     // Set up the table
    add(new Label("projectName", new PropertyModel(session.getProject(), "name")));
     add(new Label("dateString", new PropertyModel(session, "dateString")));
     add(new ListView("SdtSummaryList", new PropertyModel(model, "SdtList")) {
       /** For serialization */
       private static final long serialVersionUID = 1L;
 
       /** 
        * How to populate the table.
        * @param item The summary item.  
        */
       @Override
       protected void populateItem(ListItem item) {
         SdtSummary summary = (SdtSummary) item.getModelObject();
         String sdtName = summary.getSdtName();
         String tool = summary.getTool();
         String count = String.valueOf(summary.getCount());
         item.add(new Label("sdtName", sdtName));
         item.add(new Label("tool", tool));
         item.add(new SdtSummaryPanelLink("link", count, sdtName, tool) {
           /** For serialization. */
           private static final long serialVersionUID = 1L;
           @Override
           public void onClick() {
             SensorDataSession session = ProjectBrowserSession.get().getSensorDataSession();
             session.getSensorDataDetailsProvider().setSensorDataDetailsProvider(sdtName, tool);
             session.setSdtName(sdtName);
             session.setTool(tool);
           }
         });
       }
     });
   }
 
   /**
    * Display this panel only if the SdtSummaryModel contains information. 
    * @return True if this panel should be displayed.
    */
   @Override
   public boolean isVisible() {
     SensorDataSession session = ProjectBrowserSession.get().getSensorDataSession();
     SdtSummaryModel model = session.getSdtSummaryModel();
     return !model.isEmpty();
   }
 }
