 package org.hackystat.projectbrowser.page.sensordata;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 
 public class SdtSummaryPanelLink extends Link {
   
   /** For serialization. */
   private static final long serialVersionUID = 1L;
 
   public SdtSummaryPanelLink(String id, String count) {
     super(id);
     add(new Label("count", count));
   }
 
   @Override
   public void onClick() {
     //do nothing.
   }
 }
