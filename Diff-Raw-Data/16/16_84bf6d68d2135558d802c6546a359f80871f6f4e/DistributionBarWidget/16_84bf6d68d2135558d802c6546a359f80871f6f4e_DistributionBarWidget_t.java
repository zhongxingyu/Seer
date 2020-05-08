 package com.vaadin.training.bugrap.client.components.distbar;
 
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 
 public class DistributionBarWidget extends FlowPanel {
 
    public static final int MAX_LABEL_WIDTH_PERCENTS = 95;
     private final Label closedLabel;
     private final Label inProgressLabel;
     private final Label unassignedLabel;
     private int closed = 0;
     private int inProgress = 0;
     private int unassigned = 0;
 
     public static final int UNIT_WIDTH = 30;
 
     public DistributionBarWidget() {
         setStyleName("distribution-bar");
 
         closedLabel = createLabel();
         closedLabel.addStyleName("closed");
         closedLabel.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
 
             }
         });
         add(closedLabel);
 
         inProgressLabel = createLabel();
         inProgressLabel.addStyleName("in-progress");
         add(inProgressLabel);
 
         unassignedLabel = createLabel();
         unassignedLabel.addStyleName("unassigned");
         add(unassignedLabel);
     }
 
     private Label createLabel() {
         Label label = new Label();
         label.addStyleName("partition");
         return label;
     }
 
     public void setClosed(int closed) {
         this.closed = closed;
         closedLabel.setText(String.valueOf(closed));
 
         updateWidth();
     }
 
     public void setInProgress(int inProgress) {
         this.inProgress = inProgress;
         inProgressLabel.setText(String.valueOf(inProgress));
 
         updateWidth();
     }
 
     public void setUnassigned(int unassigned) {
         this.unassigned = unassigned;
         unassignedLabel.setText(String.valueOf(unassigned));
 
         updateWidth();
     }
 
     private void updateWidth() {
         int availableWidth = getOffsetWidth();
 
         int closedWidth = UNIT_WIDTH * closed;
         int inProgressWidth = UNIT_WIDTH * inProgress;
         int unassignedWidth = UNIT_WIDTH * unassigned;
 
         int totalCalculatedWidth = closedWidth + inProgressWidth + unassignedWidth;
 
         if (totalCalculatedWidth > availableWidth) {
             closedLabel.getElement().getStyle().setProperty("minWidth", UNIT_WIDTH, Style.Unit.PX);
            closedLabel.getElement().getStyle().setProperty("maxWidth", MAX_LABEL_WIDTH_PERCENTS, Style.Unit.PCT);
             inProgressLabel.getElement().getStyle().setProperty("minWidth", UNIT_WIDTH, Style.Unit.PX);
            inProgressLabel.getElement().getStyle().setProperty("maxWidth", MAX_LABEL_WIDTH_PERCENTS, Style.Unit.PCT);
             unassignedLabel.getElement().getStyle().setProperty("minWidth", UNIT_WIDTH, Style.Unit.PX);
            unassignedLabel.getElement().getStyle().setProperty("maxWidth", MAX_LABEL_WIDTH_PERCENTS, Style.Unit.PCT);
 
             closedLabel.getElement().getStyle().setWidth(100 * closedWidth / (double) totalCalculatedWidth, Style.Unit.PCT);
             inProgressLabel.getElement().getStyle().setWidth(100 * inProgressWidth / (double) totalCalculatedWidth, Style.Unit.PCT);
             unassignedLabel.getElement().getStyle().setWidth(100 * unassignedWidth / (double) totalCalculatedWidth, Style.Unit.PCT);
         } else {
             closedLabel.setWidth(closedWidth + "px");
             inProgressLabel.setWidth(inProgressWidth + "px");
             unassignedLabel.setWidth(unassignedWidth + "px");
         }
     }
 
     public void setClosedListener(ClickHandler clickHandler) {
         closedLabel.addClickHandler(clickHandler);
     }
 
     public void setInProgressListener(ClickHandler clickHandler) {
         inProgressLabel.addClickHandler(clickHandler);
     }
 
     public void setUnassignedListener(ClickHandler clickHandler) {
         unassignedLabel.addClickHandler(clickHandler);
     }
 }
