 package org.drools.guvnor.decisiontable.client.widget;
 
 import org.drools.guvnor.decisiontable.client.widget.resources.CellTableResource;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ScrollEvent;
 import com.google.gwt.event.dom.client.ScrollHandler;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Vertical implementation of a Decision Table where rules are represented as
  * rows and the definition (meta, conditions and actions are represented as
  * columns.
  * 
  * @author manstis
  * 
  */
 public class VerticalDecisionTableWidget extends DecisionTableWidget {
 
     // Styles may differ for Vertical and Horizontal Decision Tables if we have
     // borders to seperate columns (which becomes rows in a Horizontal Decision
     // Table)
    protected CellTableResource resource = GWT.create(CellTableResource.class);
 
     /*
      * (non-Javadoc)
      * 
      * @see org.drools.guvnor.decisiontable.client.widget.DecisionTableWidget#
      * getMainPanel()
      */
     @Override
     protected Panel getMainPanel() {
 	if (this.mainPanel == null) {
 	    this.mainPanel = new HorizontalPanel();
 	}
 	return this.mainPanel;
     }
 
     @Override
     protected Panel getBodyPanel() {
 	if (this.bodyPanel == null) {
 	    this.bodyPanel = new VerticalPanel();
 	}
 	return this.bodyPanel;
     }
 
     @Override
     protected MergableGridWidget getGridWidget() {
 	if (this.gridWidget == null) {
 	    this.gridWidget = new VerticalMergableGridWidget(this, resource);
 	}
 	return this.gridWidget;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.drools.guvnor.decisiontable.client.widget.DecisionTableWidget#
      * getHeaderWidget()
      */
     @Override
     protected DecisionTableHeaderWidget getHeaderWidget() {
 	if (this.headerWidget == null) {
 	    this.headerWidget = new VerticalDecisionTableHeaderWidget(this);
 	}
 	return this.headerWidget;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.drools.guvnor.decisiontable.client.widget.DecisionTableWidget#
      * getSidebarWidget()
      */
     @Override
     protected DecisionTableSidebarWidget getSidebarWidget() {
 	if (this.sidebarWidget == null) {
 	    this.sidebarWidget = new VerticalDecisionTableSidebarWidget(this);
 	}
 	return this.sidebarWidget;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.drools.guvnor.decisiontable.client.widget.DecisionTableWidget#
      * getScrollHandler()
      */
     @Override
     protected ScrollHandler getScrollHandler() {
 	return new ScrollHandler() {
 
 	    @Override
 	    public void onScroll(ScrollEvent event) {
 		headerWidget.setScrollPosition(scrollPanel
 			.getHorizontalScrollPosition());
 		sidebarWidget
 			.setScrollPosition(scrollPanel.getScrollPosition());
 	    }
 	};
     }
 
 }
