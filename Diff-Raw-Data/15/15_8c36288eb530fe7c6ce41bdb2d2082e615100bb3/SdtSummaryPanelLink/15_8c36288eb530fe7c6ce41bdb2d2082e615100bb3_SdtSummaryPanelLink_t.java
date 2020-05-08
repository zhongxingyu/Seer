 package org.hackystat.projectbrowser.page.sensordata;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 
/**
 * Provides the link instance for the SdtSummary Panel. 
 * This contains a custom label, which is the count.
 * @author Philip Johnson
 */
 public class SdtSummaryPanelLink extends Link {
   
   /** For serialization. */
   private static final long serialVersionUID = 1L;
 
  /**
   * Creates the link in the SdtSummary Panel.
   * @param id The wicket ID.
   * @param count The count of instances. 
   */
   public SdtSummaryPanelLink(String id, String count) {
     super(id);
     add(new Label("count", count));
   }
 
  /**
   * Don't need to provide this behavior, will be overridden. 
   */
   @Override
   public void onClick() {
     //do nothing.
   }
 }
