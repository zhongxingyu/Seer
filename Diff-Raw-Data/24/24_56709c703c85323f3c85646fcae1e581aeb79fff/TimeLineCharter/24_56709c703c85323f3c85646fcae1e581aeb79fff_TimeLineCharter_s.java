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
 	private ToggleButton mNetAllocatedButton;
 	private ToggleButton mNetRevisedButton;
 	private ToggleButton mNetUsedButton;
 	private ToggleButton mGrossAllocatedButton;
 	private ToggleButton mGrossRevisedButton;
 	private ToggleButton mGrossUsedButton;
 
 	public TimeLineCharter( Application app ) {
 		mApp = app;
 		mPanel = new VerticalPanel();
 		
 		mInfButton = new ToggleButton("ריאלי");
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
 
 		mOrigButton = new ToggleButton("נומינלי");
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
 
 		mNetAllocatedButton = new ToggleButton("הקצאה נטו");
		mNetAllocatedButton.setWidth("115px");
 		mNetAllocatedButton.addClickHandler( new ClickHandler() {			
 			@Override
 			public void onClick(ClickEvent event) {
 				redrawChart();				
 			}
 		});
 		mNetRevisedButton = new ToggleButton("הקצאה מעודכנת נטו");
		mNetRevisedButton.setWidth("115px");
 		mNetRevisedButton.addClickHandler( new ClickHandler() {			
 			@Override
 			public void onClick(ClickEvent event) {
 				redrawChart();				
 			}
 		});
 		mNetUsedButton = new ToggleButton("שימוש נטו");
		mNetUsedButton.setWidth("115px");
 		mNetUsedButton.addClickHandler( new ClickHandler() {			
 			@Override
 			public void onClick(ClickEvent event) {
 				redrawChart();				
 			}
 		});
 		mGrossAllocatedButton = new ToggleButton("הקצאה ברוטו");
		mGrossAllocatedButton.setWidth("115px");
 		mGrossAllocatedButton.addClickHandler( new ClickHandler() {			
 			@Override
 			public void onClick(ClickEvent event) {
 				redrawChart();				
 			}
 		});
 		mGrossRevisedButton = new ToggleButton("הקצאה מעודכנת ברוטו");
		mGrossRevisedButton.setWidth("115px");
 		mGrossRevisedButton.addClickHandler( new ClickHandler() {			
 			@Override
 			public void onClick(ClickEvent event) {
 				redrawChart();				
 			}
 		});
 		mGrossRevisedButton.setDown(true);
 		mGrossUsedButton = new ToggleButton("שימוש ברוטו");
		mGrossUsedButton.setWidth("115px");
 		mGrossUsedButton.addClickHandler( new ClickHandler() {			
 			@Override
 			public void onClick(ClickEvent event) {
 				redrawChart();				
 			}
 		});
 		mGrossUsedButton.setDown(true);
 
 		HorizontalPanel mDataFieldPanelNet = new HorizontalPanel();
 		mDataFieldPanelNet.add( mNetAllocatedButton );
 		mDataFieldPanelNet.add( mNetRevisedButton );
 		mDataFieldPanelNet.add( mNetUsedButton );
 		HorizontalPanel mDataFieldPanelGross = new HorizontalPanel();
 		mDataFieldPanelGross.add( mGrossAllocatedButton );
 		mDataFieldPanelGross.add( mGrossRevisedButton );
 		mDataFieldPanelGross.add( mGrossUsedButton );
 
 		mPanel.add(mDataFieldPanelNet);
 		mPanel.add(mDataFieldPanelGross);
 		
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
 	
 	private void setValueIfNotNull( DataTable data, int row, int column, Integer value ) {
 		if ( value == null ) return;
 		data.setValue(row, column, value);
 	}
 
 	private void setValueIfNotNull( DataTable data, int row, int column, Double value ) {
 		if ( value == null ) return;
 		data.setValue(row, column, value);
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
 	    boolean netUsed = mNetUsedButton.isDown();
 	    boolean netRevised = mNetRevisedButton.isDown();
 	    boolean netAllocated = mNetAllocatedButton.isDown();
 	    boolean grossUsed = mGrossUsedButton.isDown();
 	    boolean grossRevised = mGrossRevisedButton.isDown();
 	    boolean grossAllocated = mGrossAllocatedButton.isDown();
 	    if ( netUsed    )   { data.addColumn(ColumnType.NUMBER, "שימוש בפועל - נטו" ); }
 	    if ( netRevised )   { data.addColumn(ColumnType.NUMBER, "הקצאה מעודכנת - נטו" ); }
 	    if ( netAllocated ) { data.addColumn(ColumnType.NUMBER, "הקצאת תקציב - נטו" ); }
 	    if ( grossUsed    )   { data.addColumn(ColumnType.NUMBER, "שימוש בפועל - ברוטו" ); }
 	    if ( grossRevised )   { data.addColumn(ColumnType.NUMBER, "הקצאה מעודכנת - ברוטו" ); }
 	    if ( grossAllocated ) { data.addColumn(ColumnType.NUMBER, "הקצאת תקציב - ברוטו" ); }
 	    
 	    data.addRows(mList.size());
 	    for ( int i = 0 ; i < mList.size() ; i ++ ) {
 		    data.setValue(mList.size()-i-1, 0, mList.get(i).getYear().toString() );
 		    if ( mInfButton.isDown() ) {
 		    	column = 1;
 		    	if ( netUsed        ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.USED, true ) );      column++; }
 		    	if ( netRevised     ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.REVISED, true ) );      column++; }
 		    	if ( netAllocated   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.ALLOCATED, true ) );      column++; }
 		    	if ( grossUsed      ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.USED, false) );      column++; }
 		    	if ( grossRevised   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.REVISED, false) );      column++; }
 		    	if ( grossAllocated ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.ALLOCATED, false ) );      column++; }
 				options.setTitleY("אלפי \u20AA ריאליים");
 		    } else if ( mOrigButton.isDown() ) {
 		    	column = 1;
 		    	if ( netUsed        ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.USED, true ) );      column++; }
 		    	if ( netRevised     ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.REVISED, true ) );      column++; }
 		    	if ( netAllocated   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.ALLOCATED, true ) );      column++; }
 		    	if ( grossUsed      ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.USED, false) );      column++; }
 		    	if ( grossRevised   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.REVISED, false) );      column++; }
 		    	if ( grossAllocated ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.ALLOCATED, false ) );      column++; }
 				options.setTitleY("אלפי \u20AA נומינליים");
 		    } else if ( mPercentButton.isDown() ) {
 		    	column = 1;
 		    	if ( netUsed        ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.USED, true ) );      column++; }
 		    	if ( netRevised     ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.REVISED, true ) );      column++; }
 		    	if ( netAllocated   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.ALLOCATED, true ) );      column++; }
 		    	if ( grossUsed      ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.USED, false) );      column++; }
 		    	if ( grossRevised   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.REVISED, false) );      column++; }
 		    	if ( grossAllocated ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.ALLOCATED, false ) );      column++; }
 				options.setTitleY("אחוזים");
 		    } 
 	    }
 		AreaChart areachart = new AreaChart( data, options );
 		mChartPanel.add(areachart);
 	}
 }
