 package org.obudget.client;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 
 class Application implements ValueChangeHandler<String> {
 
 	private BudgetLines mChildrenBudgetLines = new BudgetLines();
 	private BudgetLines  mHistoricBudgetLines = new BudgetLines();
 	private ResultGrid mResultsGrid;
 	private PieCharter mPieCharter;
 	private HTML mBreadcrumbs;
 	private TimeLineCharter mTimeLineCharter;
 	private String mCode = null;
 	private Integer mYear = null;
 	private ListBox mYearSelection;
 	private SuggestBox mSearchBox;
 	private Label mSummary1;
 	private Label mSummary2;
 	private HTML mSummary3;
 	private BudgetNews mBudgetNews;
 	private HTML mCheatSheet;
 	
 	static private Application mInstance = null;
 	private static boolean mEmbedded = false;
 	
 	static public Application getInstance() {
 		if ( mInstance == null ) {
 			mInstance = new Application();
 			mInstance.init();
 		}
 		return mInstance;
 	}
 	
 	public void init() {
 		TotalBudget.getInstance();
 		
 		mResultsGrid = new ResultGrid();
		mResultsGrid.setWidth("60%");
 
 		mPieCharter = new PieCharter(this, mEmbedded);
 		mPieCharter.setWidth("600px");
 		mPieCharter.setHeight("300px");
 		
 		mTimeLineCharter = new TimeLineCharter(this, mEmbedded);
 		mTimeLineCharter.setWidth("600px");
 		mTimeLineCharter.setHeight("300px");
 		
 		mBreadcrumbs = new HTML("");
 		mBreadcrumbs.setHeight("20px");
 		mBreadcrumbs.setWidth("700px");
 		
 		mYearSelection = new ListBox();
 		mYearSelection.addChangeHandler( new ChangeHandler() {
 			@Override
 			public void onChange(ChangeEvent event) {
 				Integer index = mYearSelection.getSelectedIndex();
 				String yearStr = mYearSelection.getItemText(index);
 				Integer year = Integer.parseInt(yearStr);
 				selectYear(year);
 			}
 		});
 		
 		mSearchBox = new SuggestBox(new BudgetSuggestionOracle());
 		mSearchBox.setWidth("300px");
 		mSearchBox.addSelectionHandler( new SelectionHandler<Suggestion>() {
 			@Override
 			public void onSelection(SelectionEvent<Suggestion> event) {
 				final BudgetSuggestion bs = (BudgetSuggestion) event.getSelectedItem();
 				
 				BudgetAPICaller api = new BudgetAPICaller();
 				api.setCode(bs.getCode());
 				api.setParameter("depth", "0");
 				api.setParameter("text", bs.getTitle());
 				
 				api.go( new BudgetAPICallback() {
 					
 					@Override
 					public void onSuccess(JSONArray data) {
 						if ( data.size() > 0 ) {
 							Integer year = (int) data.get(0).isObject().get("year").isNumber().doubleValue();
 							newCodeAndYear(bs.getCode(),year);
 						}
 					}
 				});
 				
 			}
 		});
 
 		mSummary1 = new Label();
 		mSummary2 = new Label();
 		mSummary3 = new HTML();
 		
 		mBudgetNews = new BudgetNews();
 		
 		mCheatSheet = new HTML("(הסברים)");
 		final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
 		simplePopup.setWidth("400px");
 		HTML simplePopupContents = new HTML( "<h4>מונחון מקוצר</h4>"+
 											 "<lu>"+
 											 "<li><b>נטו</b>: <u>תקציב הוצאה נטו</u> – הסכום המותר להוצאה בשנה כלשהי כפי שמפורט בחוק התקציב. תקציב זה מכונה גם \"תקציב המזומנים\".</li>"+
 											 "<li><b>ברוטו</b>: <u>תקציב ההוצאה נטו</u> בתוספת <u>תקציב ההוצאה המותנית בהכנסה</u> – תקציב נוסף המותר בהוצאה, ובלבד שיתקבלו תקבולים למימון ההוצאה מגורמים חוץ-ממשלתיים. תקבולים אלו אינם כוללים אגרה המשולמת לאוצר המדינה שהוטלה על-פי חיקוק שנחקק אחרי תחילת שנת הכספים 1992, ואינה כוללת הכנסה שמקורה במלווה (חוץ מתקציבי פיתוח וחשבון הון).</li>"+
 											 "<li><b>הקצאה</b>: <u>תקציב מקורי</u> – התקציב שאושר בכנסת במסגרת חוק התקציב. ייתכנו הבדלים בין הצעת התקציב לבין התקציב שיאושר בכנסת בסופו של דבר.</li>"+
 											 "<li><b>הקצאה מעודכנת</b>: <u>תקציב על שינוייו</u> – תקציב המדינה עשוי להשתנות במהלך השנה. שינויים אלו כוללים תוספות, הפחתות והעברות תקציביות בין סעיפי תקציב (באישור ועדת הכספים של הכנסת). נוסף על כך, פעמים רבות מועברים עודפים מחויבים משנה קודמת הנכללים בתקציב זה. רוב השינויים בתקציב דורשים את אישורה של ועדת הכספים של הכנסת. התקציב בסוף השנה הכולל את השינויים שנעשו בו במהלך השנה נקרא התקציב על שינוייו או התקציב המאושר.</li>"+
 											 "<li><b>שימוש</b>:  <u>ביצוע</u> – התקציב שכבר נוצל ושולם בפועל על-ידי החשב.</li>"+
 											 "</lu>"+
 											 "<br/>"+
 											 "<a href='http://www.knesset.gov.il/mmm/data/docs/m02217.doc'>מקור</a>");
 		simplePopupContents.setStyleName("obudget-cheatsheet-popup");
 		simplePopup.setWidget( simplePopupContents );
 		mCheatSheet.addClickHandler( new ClickHandler() {			
 			@Override
 			public void onClick(ClickEvent event) {
 	            Widget source = (Widget) event.getSource();
 	            int left = source.getAbsoluteLeft() + 10;
 	            int top = source.getAbsoluteTop() + 10;
 	            simplePopup.setPopupPosition(left, top);
 	            simplePopup.show();				
 			}
 		});
 		
 		History.addValueChangeHandler( this );
 	}
 	
 	public ResultGrid getResultsGrid() {
 		return mResultsGrid;
 	}
 
 	public PieCharter getPieCharter() {
 		return mPieCharter;
 	}
 
 	public void selectYear( Integer year ) {
 		if ( mCode != null ) {
 			selectBudgetCode(mCode, year);
 		}
 	}
 
 	public void selectBudgetCode( String code, Integer year ) {
 		mCode = code;
 		mYear = year;
 		
 		BudgetAPICaller generalInfo = new BudgetAPICaller();
 		
 		// Load single record
 		generalInfo.setCode(code);
 		generalInfo.setParameter("year", year.toString() );
 		generalInfo.setParameter("depth", "0");
 		generalInfo.go( new BudgetAPICallback() {
 			
 			@Override
 			public void onSuccess(JSONArray data) {
 				if ( data.size() < 1 ) {
 					return;
 				}
 				
 				JSONObject firstResult = data.get(0).isObject(); 
 				
 				String title = firstResult.get("title").isString().stringValue();
 				String code = firstResult.get("budget_id").isString().stringValue();
 				mSearchBox.setValue(title);
 
 				//mYearSelection.setSelectedIndex( mYear - 1992 );
 
 				mBudgetNews.update("\""+title+"\"");
 				
 				Window.setTitle("תקציב המדינה - "+title+" ("+mYear+")");
 				mSummary1.setText( "לתקציב "+title+" הוקצו בשנת");
 				final Integer revisedSum;
 				final String revisedSumType;
 				if ( (firstResult.get("gross_amount_revised") != null) &&
 					 (firstResult.get("gross_amount_revised").isNumber() != null) ) {
 					revisedSumType = "gross";
 				} else {
 					revisedSumType = "net";
 				}
 				if ( ( firstResult.get(revisedSumType+"_amount_revised") != null ) && 
 					   firstResult.get(revisedSumType+"_amount_revised").isNumber() != null ) {
 					revisedSum = (int) firstResult.get(revisedSumType+"_amount_revised").isNumber().doubleValue();				
 					mSummary2.setText( "סה\"כ "+NumberFormat.getDecimalFormat().format(revisedSum)+",000 \u20aa" + (revisedSumType == "net" ? " (נטו)" : ""));
 				} else {
 					revisedSum = null;
 				}
 				
 				mSummary3.setHTML("");							
 				if ( firstResult.get("parent") != null ) {
 					JSONArray parents = firstResult.get("parent").isArray();	
 					if ( parents.size() > 0 ) {
 						final String parentCode = parents.get(0).isObject().get("budget_id").isString().stringValue();
 						final String parentTitle = parents.get(0).isObject().get("title").isString().stringValue();
 						
 						BudgetAPICaller percent = new BudgetAPICaller();
 						percent.setCode(parentCode);
 						percent.setParameter("year", mYear.toString());
 						percent.setParameter("depth", "0");
 						percent.go( new BudgetAPICallback() {						
 							@Override
 							public void onSuccess(JSONArray data) {
 								if ( (data.get(0).isObject().get(revisedSumType+"_amount_revised") != null) && 
 									 (data.get(0).isObject().get(revisedSumType+"_amount_revised").isNumber() != null) ) {
 									double percent = revisedSum / data.get(0).isObject().get(revisedSumType+"_amount_revised").isNumber().doubleValue();
 									mSummary3.setHTML( "שהם "+NumberFormat.getPercentFormat().format(percent)+" מתקציב <a href='#"+hashForCode(parentCode)+"'>"+parentTitle+"</a>");
 								}
 							}
 						});
 					}			
 				
 					String breadcrumbs = "";
 					for ( int i = 0 ; i < parents.size() ; i++ ) {
 						String ptitle = parents.get(i).isObject().get("title").isString().stringValue();
 						String pcode = parents.get(i).isObject().get("budget_id").isString().stringValue();
 						breadcrumbs = "<a href='#"+hashForCode(pcode)+"'>"+ptitle+"</a> ("+pcode+") "+"&nbsp;&gt;&nbsp;" + breadcrumbs;
 					}
 					breadcrumbs += "<a href='#"+hashForCode(code)+"'>"+title+"</a> ("+code+")";
 					mBreadcrumbs.setHTML(breadcrumbs);			
 				}
 			}
 		});
 
 		// Load all children
 		BudgetAPICaller childrenLines = new BudgetAPICaller();
 		childrenLines.setCode(code);
 		childrenLines.setParameter("year", year.toString());
 		childrenLines.setParameter("depth", "1");
 		childrenLines.go( new BudgetAPICallback() {
 			
 			@Override
 			public void onSuccess(JSONArray data) {
 				mChildrenBudgetLines.parseJson(data);
 				mResultsGrid.handleData(mChildrenBudgetLines);
 				mPieCharter.handleData(mChildrenBudgetLines);				
 			}
 		});
 		
 		// Load same record, over the years
 		BudgetAPICaller historicLines = new BudgetAPICaller();
 		historicLines.setCode(code);
 		historicLines.setParameter("depth", "0");
 		historicLines.go( new BudgetAPICallback() {
 			
 			@Override
 			public void onSuccess(JSONArray data) {
 				mHistoricBudgetLines.parseJson(data);
 
 				mYearSelection.clear();
 				Integer selectedIndex = 0;
 				for ( BudgetLine bl : mHistoricBudgetLines ) {
 					String year = bl.getYear().toString();
 					mYearSelection.addItem( year );
 					if (year.equals( mYear.toString() )) {
 						selectedIndex = mYearSelection.getItemCount() - 1;
 					}
 				}
 				mYearSelection.setSelectedIndex( selectedIndex );
 
 				mTimeLineCharter.handleData(mHistoricBudgetLines);				
 			}
 		});
 		
 	}
 
 	public TimeLineCharter getTimeLineCharter() {
 		return mTimeLineCharter;
 	}
 
 	public String hash( String code, Integer year ) {
 		return code+","+year+","+mTimeLineCharter.getState()+","+mPieCharter.getState()+","+mResultsGrid.getState();
 	}
 
 	public String hashForCode( String code ) {
 		return hash( code, mYear );
 	}
 
 	public void newYear( Integer year ) {
 		newCodeAndYear( mCode, year);
 	}
 
 	public void newCodeAndYear( String code, Integer year ) {
 		mYear = year;
 		mCode = code;
 		History.newItem(hash(mCode, mYear));
 	}
 	
 	public void stateChanged() {
 		History.newItem(hash(mCode, mYear),false);
 	}
 
 	@Override
 	public void onValueChange(ValueChangeEvent<String> event) {
 		String hash = event.getValue();
 		String[] parts = hash.split(",");
 		
 		if ( parts.length == 12 ) {
 			String code = parts[0];
 			Integer year = Integer.decode(parts[1]);
 			try {
 				Integer timeLineDataType = Integer.decode(parts[2]);
 				Integer timeLineChartSelect0 = Integer.decode(parts[3]);
 				Integer timeLineChartSelect1 = Integer.decode(parts[4]);
 				Integer timeLineChartSelect2 = Integer.decode(parts[5]);
 				Integer timeLineChartSelect3 = Integer.decode(parts[6]);
 				Integer timeLineChartSelect4 = Integer.decode(parts[7]);
 				Integer timeLineChartSelect5 = Integer.decode(parts[8]);
 				Integer pieChartDataType = Integer.decode(parts[9]);
 				Integer pieChartNet= Integer.decode(parts[10]);
 				Integer resultsGridNet= Integer.decode(parts[11]);
 				selectBudgetCode(code, year);
 				mTimeLineCharter.setState( timeLineDataType,
 										   timeLineChartSelect0, 
 										   timeLineChartSelect1, 
 										   timeLineChartSelect2, 
 										   timeLineChartSelect3, 
 										   timeLineChartSelect4, 
 										   timeLineChartSelect5 );
 				mPieCharter.setState( pieChartDataType,
 									  pieChartNet );
 				mResultsGrid.setState( resultsGridNet );
 			} catch (Exception e){
				newCodeAndYear("00", 2009);
 			}
 		} else {
			newCodeAndYear("00", 2009);
 		}
 	}
 
 	public Widget getYearSelection() {
 		return mYearSelection;
 	}
 
 	public Widget getSearchBox() {
 		return mSearchBox;
 	}
 
 	public Widget getBreadcrumbs() {
 		return mBreadcrumbs;
 	}
 
 	public Widget getSummary1() {
 		return mSummary1;
 	}
 
 	public Widget getSummary2() {
 		return mSummary2;
 	}
 
 	public Widget getSummary3() {
 		return mSummary3;
 	}
 
 	public Widget getBudgetNews() {
 		return mBudgetNews;
 	}
 
 	public Widget getCheatSheet() {
 		return mCheatSheet;
 	}
 
 	public static void setEmbedded( boolean embedded ) {
 		mEmbedded  = embedded;
 	}
 
 }
