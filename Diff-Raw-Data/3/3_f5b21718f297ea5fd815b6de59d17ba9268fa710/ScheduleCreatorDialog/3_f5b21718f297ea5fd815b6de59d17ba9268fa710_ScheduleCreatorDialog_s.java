 /*
  * Copyright 2006-2008 Pentaho Corporation.  All rights reserved. 
  * This software was developed by Pentaho Corporation and is provided under the terms 
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
  * this file except in compliance with the license. If you need a copy of the license, 
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
  * BI Platform.  The Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
  * the license for the specific language governing your rights and limitations.
  *
  * @created May 19, 2008
  * 
  */
 package org.pentaho.pac.client.scheduler.view;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
import org.pentaho.pac.client.PentahoAdminConsole;
 import org.pentaho.pac.client.common.ui.dialog.ConfirmDialog;
import org.pentaho.pac.client.i18n.PacLocalizedMessages;
 
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.SourcesTabEvents;
 import com.google.gwt.user.client.ui.TabListener;
 import com.google.gwt.user.client.ui.TabPanel;
 
 public class ScheduleCreatorDialog extends ConfirmDialog {
   private static final String SELECTED = "selected"; //$NON-NLS-1$
   
   public enum TabIndex {
     SCHEDULE( 0, MSGS.schedule() ),
     SCHEDULE_ACTION( 1, MSGS.scheduledAction() );
     
     private TabIndex( int value, String name ) {
       this.value = value;
       this.name = name;
     }
     private int value;
     private String name;
     
     private static TabIndex[] tabIndexAr = {
       SCHEDULE, 
       SCHEDULE_ACTION 
     };
 
     public static TabIndex get(int idx) {
       return tabIndexAr[idx];
     }
     
     public int value() {
       return value;
     }
 
     public String toString() {
       return name;
     }
   }; // end enum
   
   private ScheduleEditor scheduleEditor = new ScheduleEditor();
   private SolutionRepositoryItemPicker solRepItemPicker = new SolutionRepositoryItemPicker();
   private Label scheduleTabLabel = new Label( TabIndex.SCHEDULE.toString() );
   private Label scheduleActionTabLabel = new Label( TabIndex.SCHEDULE_ACTION.toString() );
   private Map<TabIndex, Label> tabLabelMap = new HashMap<TabIndex, Label>();
   private TabPanel tabPanel = new TabPanel();
   
   public ScheduleCreatorDialog() {
     super();
     this.setNoBorderOnClientPanel();
     setTitle( MSGS.scheduleCreator() );
     setClientSize( "475px", "450px" ); //$NON-NLS-1$ //$NON-NLS-2$
 
     solRepItemPicker.setWidth( "100%" ); //$NON-NLS-1$
     solRepItemPicker.setHeight( "100%" ); //$NON-NLS-1$
     
     tabPanel.setStylePrimaryName( "schedulerTabPanel" ); //$NON-NLS-1$
     
     SimplePanel p = new SimplePanel();  // Simple panel required to accomodate an IE7 defect in margin styles
     p.setStyleName( "paddingPanel" ); //$NON-NLS-1$
     p.add(scheduleEditor);
     tabPanel.add( p, scheduleTabLabel );
     
     p = new SimplePanel();  // Simple panel required to accomodate an IE7 defect in margin styles
     p.setStyleName( "paddingPanel" ); //$NON-NLS-1$
     p.add(solRepItemPicker);
     tabPanel.add( p, scheduleActionTabLabel );
     
     scheduleTabLabel.setStylePrimaryName( "tabLabel" ); //$NON-NLS-1$
     scheduleActionTabLabel.setStylePrimaryName( "tabLabel" ); //$NON-NLS-1$
     tabLabelMap.put( TabIndex.SCHEDULE, scheduleTabLabel );
     tabLabelMap.put( TabIndex.SCHEDULE_ACTION, scheduleActionTabLabel );
     
     tabPanel.selectTab( TabIndex.SCHEDULE.value() );
     
     tabPanel.addTabListener( new TabListener() {
       public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
         return true;
       }
       public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
         for ( Map.Entry<TabIndex,Label> me : tabLabelMap.entrySet() ) {
           Label l = me.getValue();
           l.removeStyleDependentName( SELECTED );
         }
         Label l = tabLabelMap.get( TabIndex.get(  tabIndex ) );
         l.addStyleDependentName( SELECTED );
         switch (TabIndex.get( tabIndex) ) {
           case SCHEDULE:
             scheduleEditor.setFocus();
             break;
           case SCHEDULE_ACTION:
             solRepItemPicker.setFocus();
             break;
         }
       }
     });
     
     addWidgetToClientArea( tabPanel );
   }
 
   public ScheduleEditor getScheduleEditor() {
     return scheduleEditor;
   }
 
   public SolutionRepositoryItemPicker getSolutionRepositoryItemPicker() {
     return solRepItemPicker;
   }
   
   public void reset( Date d ) {
     scheduleEditor.reset( d );
     solRepItemPicker.reset();
     
     tabPanel.selectTab( TabIndex.SCHEDULE.value() );
   }
   
   public void setSelectedTab( TabIndex tabKey ) {
     tabPanel.selectTab( tabKey.value() );
   }
   
   public TabIndex getSelectedTab() {
     return TabIndex.get( tabPanel.getTabBar().getSelectedTab() );
   }
   
   public void setTabError( TabIndex tabKey ) {
     tabLabelMap.get(tabKey).setStylePrimaryName( "tabLabelError" ); //$NON-NLS-1$
   }
   
   public void clearTabError() {
     for ( Map.Entry<TabIndex, Label> me : tabLabelMap.entrySet() ) {
       me.getValue().setStylePrimaryName( "tabLabel" ); //$NON-NLS-1$
     }
   }
 }
