 package org.iplantc.core.uicommons.client.appearance.widgets;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 
 /**
  * These are the resources required by default anchor appearances.  Currently, IPLantAnchor and
  * InternalAnchor use this.
  */
 public interface AnchorDefaultResources extends ClientBundle {
 
 	/**
 	 * The CSS associated with the default anchor apppearance
 	 */
 	interface Style extends CssResource {
 
 		/**
 		 * The style attached to the whole anchor including any related text
 		 */
         String anchor();
 
         /**
          * The style attached to an anchor that is in line with other text.
          */
         String inlineAnchor();
         
         /**
          * The style attached to the link within the anchor
         * 
         * @deprecated This is redundant and shouldn't be used.
          */
        @Deprecated
         String anchorText();
 
         /**
          * The additive style attached to the link when the mouse cursor is over top of it.
          * @deprecated This is redundant and shouldn't be used.
          */
         @Deprecated
         String anchorMouseOver();
 
         /**
          * The additive style attached to the link when the mouse cursor is not over top of it.
          * 
          * @deprecated This is redundant and shouldn't be used.
          */
         @Deprecated
         String anchorMouseOut();
     }
 
 	AnchorDefaultResources INSTANCE = GWT.create(AnchorDefaultResources.class);
 	
     @Source("AnchorDefaultStyle.css")
     Style style();
     
 }
