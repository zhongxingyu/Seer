 package org.obudget.client;
 
 import java.util.LinkedList;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
 import com.google.gwt.visualization.client.DataTable;
 import com.google.gwt.visualization.client.LegendPosition;
 import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
 import com.google.gwt.visualization.client.visualizations.AreaChart;
 
 class TimeLineCharter extends Composite {
 	private VerticalPanel mPanel;
 	private Application mApp;
 	private ToggleButton mInfButton;
 	private ToggleButton mOrigButton;
 	private ToggleButton mPercentButton;
 	private HorizontalPanel mDataTypePanel;
 	private LayoutPanel mChartPanel;
 	private LinkedList<BudgetLine> mList;
 	private ToggleButton mAllocatedButton;
 	private ToggleButton mRevisedButton;
 	private ToggleButton mUsedButton;
 
 	public TimeLineCharter( Application app ) {
 		mApp = app;
 		mPanel = new VerticalPanel();
 		
 		mInfButton = new ToggleButton("מדדי");
 		mInfButton.setWidth("30px");
 		mInfButton.setDown(true);
 		mInfButton.addClickHandler( new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				if ( mInfButton.isDown() ) {
 					mOrigButton.setDown(false);
 					mPercentButton.setDown(false);
 					redrawChart();
 				} else {
 					mInfButton.setDown(true);
 				}
 			}
 		});
 
 		mOrigButton = new ToggleButton("שקלי");
 		mOrigButton.setWidth("30px");
 		mOrigButton.addClickHandler( new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				if ( mOrigButton.isDown() ) {
 					mInfButton.setDown(false);
 					mPercentButton.setDown(false);
 					redrawChart();
 				} else {
 					mOrigButton.setDown(true);
 				}
 			}
 		});
 
 		mPercentButton = new ToggleButton("אחוזי");
 		mPercentButton.setWidth("30px");
 		mPercentButton.addClickHandler( new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				if ( mPercentButton.isDown() ) {
 					mOrigButton.setDown(false);
 					mInfButton.setDown(false);
 					redrawChart();
 				} else {
 					mPercentButton.setDown(true);
 				}
 			}
 		});
 		
 		mDataTypePanel = new HorizontalPanel();
 		LayoutPanel spacerPanel = new LayoutPanel();
 		spacerPanel.setWidth("260px");
 		mDataTypePanel.add(spacerPanel);
 		mDataTypePanel.add(mInfButton);
 		mDataTypePanel.add(mOrigButton);
 		mDataTypePanel.add(mPercentButton);
 		mPanel.add(mDataTypePanel);
 		
 		mChartPanel = new LayoutPanel();
 		mChartPanel.setHeight("260px");
 		mChartPanel.setWidth("390px");
 		mPanel.add(mChartPanel);
 
 		mAllocatedButton = new ToggleButton("הקצאה");
 		mAllocatedButton.addClickHandler( new ClickHandler() {			
 			@Override
 			public void onClick(ClickEvent event) {
 				redrawChart();				
 			}
 		});
 		mRevisedButton = new ToggleButton("הקצאה מעודכנת");
 		mRevisedButton.addClickHandler( new ClickHandler() {			
 			@Override
 			public void onClick(ClickEvent event) {
 				redrawChart();				
 			}
 		});
 		mRevisedButton.setDown(true);
 		mUsedButton = new ToggleButton("שימוש");
 		mUsedButton.addClickHandler( new ClickHandler() {			
 			@Override
 			public void onClick(ClickEvent event) {
 				redrawChart();				
 			}
 		});
		mUsedButton.setDown(true);
 
 		HorizontalPanel mDataFieldPanel = new HorizontalPanel();
 		mDataFieldPanel.add( mAllocatedButton );
 		mDataFieldPanel.add( mRevisedButton );
 		mDataFieldPanel.add( mUsedButton );
 
 		mPanel.add(mDataFieldPanel);
 		
		mPanel.setWidth("385px");

 		initWidget(mPanel);
 	}
 	
 	public void handleData( LinkedList<BudgetLine> list ) {
		if ( list.size() == 0 ) {
			mChartPanel.clear();
			return; 
		}
 		mList = list;
 		redrawChart();
 	}
 	
 	private void redrawChart() {
 
 		AreaChart.Options options = AreaChart.Options.create();
		options.setWidth(385);
 		options.setHeight(260);
 		//options.setTitle( list.get(0).getTitle() + " - " + "הקצאה באלפי \u20AA, מותאם לאינפלציה" );
 		//options.setTitleX("שנה");
 		options.setLegend(LegendPosition.BOTTOM);
 		options.setAxisFontSize(10);
 		
 		DataTable data = DataTable.create();
 	    data.addColumn(ColumnType.STRING, "Year");
 	    int column;
 	    boolean used = mUsedButton.isDown();
 	    boolean revised = mRevisedButton.isDown();
 	    boolean allocated = mAllocatedButton.isDown();
 	    if ( used    )   { data.addColumn(ColumnType.NUMBER, "שימוש בפועל" ); }
 	    if ( revised )   { data.addColumn(ColumnType.NUMBER, "הקצאה מעודכנת" ); }
 	    if ( allocated ) { data.addColumn(ColumnType.NUMBER, "הקצאת תקציב" ); }
 	    
 	    data.addRows(mList.size());
 	    for ( int i = 0 ; i < mList.size() ; i ++ ) {
 		    data.setValue(mList.size()-i-1, 0, mList.get(i).getYear().toString() );
 		    if ( mInfButton.isDown() ) {
 		    	column = 1;
 		    	if ( used      ) { data.setValue(mList.size()-i-1, column, mList.get(i).getInfUsed() );      column++; }
 		    	if ( revised   ) { data.setValue(mList.size()-i-1, column, mList.get(i).getInfRevised());    column++; }
 			    if ( allocated ) { data.setValue(mList.size()-i-1, column, mList.get(i).getInfAllocated() ); column++; }
 				options.setTitleY("אלפי \u20AA");
 		    } else if ( mOrigButton.isDown() ) {
 		    	column = 1;
 		    	if ( used      ) { data.setValue(mList.size()-i-1, column, mList.get(i).getUsed() );      column++; }
 		    	if ( revised   ) { data.setValue(mList.size()-i-1, column, mList.get(i).getRevised());    column++; }
 		    	if ( allocated ) { data.setValue(mList.size()-i-1, column, mList.get(i).getAllocated() ); column++; }
 				options.setTitleY("אלפי \u20AA");
 		    } else if ( mPercentButton.isDown() ) {
 		    	column = 1;
 		    	if ( used      ) { data.setValue(mList.size()-i-1, column, mList.get(i).getPercentUsed() );      column++; }
 		    	if ( revised   ) { data.setValue(mList.size()-i-1, column, mList.get(i).getPercentRevised() );   column++; }
 		    	if ( allocated ) { data.setValue(mList.size()-i-1, column, mList.get(i).getPercentAllocated() ); column++; }
 				options.setTitleY("אחוזים");
 		    } 
 	    }
 		AreaChart areachart = new AreaChart( data, options );
 		mChartPanel.add(areachart);
 	}
 }
