 package gwt.test.client.presenter;
 
 import gwt.test.client.ChartData;
 import gwt.test.client.ChartDataParams;
 import gwt.test.client.ChartingServiceAsync;
 import gwt.test.client.Presenter;
 import gwt.test.client.StockQuote;
 import gwt.test.client.Symbol;
 import gwt.test.client.TimeFrame;
 import gwt.test.client.event.StockChangeEvent;
 import gwt.test.client.event.StockChangeEventHandler;
 
 import java.util.List;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.IsWidget;
 
 public class StockQuotePresenter implements Presenter {
 
 	  private final ChartingServiceAsync rpcService;
 	  private final HandlerManager eventBus;
 	  private final Display display;
 
 
 	public StockQuotePresenter(ChartingServiceAsync rpcService, HandlerManager eventBus, Display display) {
 		this.rpcService = rpcService;
 		this.eventBus = eventBus;
 		this.display = display;
		this.bind();
 	}
 
 	
 	private void bind() {
 		this.display.getImportButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				rpcService.importStockQuotes(display.getSymbol(), new AsyncCallback<Long>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						display.showDialogBox("Remote Procedure Call - Failure", "serverResponseLabelError", Display.SERVER_ERROR);
 					}
 
 					public void onSuccess(Long result) {
 						display.showDialogBox("Successfuly imported StockQuotes.", "serverResponseLabelSuccess", "Successfuly imported " + result + "StockQuotes");
 					}
 
 				});
 			}});
 		this.display.getShowButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				rpcService.getLastStockQuote(display.getSymbol(), new AsyncCallback<StockQuote>() {
 					public void onFailure(Throwable caught) {
 						display.showDialogBox("Remote Procedure Call - Failure", "serverResponseLabelError", Display.SERVER_ERROR);
 					}
 
 					public void onSuccess(StockQuote result) {
 						display.setStockQuote(result);
 						eventBus.fireEvent(new StockChangeEvent(result));
 					}
 
 				});
 				rpcService.getChartData(display.getChartDataParams(), new AsyncCallback<ChartData>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						display.showDialogBox("Remote Procedure Call - Failure", "serverResponseLabelError", Display.SERVER_ERROR);
 					}
 					@Override
 					public void onSuccess(ChartData result) {
 						display.setChartData(result);
 					}
 				});
 //				chartingService.getChart(sqView.getSymbol(), sqView.getTimeFrame(), new AsyncCallback<String>() {
 //					public void onFailure(Throwable caught) {
 //						sqView.showDialogBox("Remote Procedure Call - Failure", "serverResponseLabelError", StockQuoteView.SERVER_ERROR);
 //					}
 	//
 //					public void onSuccess(String result) {
 //						sqView.setChartLink(result);
 //					}
 //				});
 			}});
 	}
 
 	@Override
 	public void go(HasWidgets container) {
 	    container.clear();
 	    container.add(display.asWidget());
 	}
 
 	public interface Display extends IsWidget {
 
 		public static final String SERVER_ERROR = "An error occurred while "
 				+ "attempting to contact the server. Please check your network " + "connection and try again.";
 
 		public final String SHOW_BUTTON_ID = "sqChartShowButton";
 		public final String IMPORT_BUTTON_ID = "sqChartImportButton";
 
 		void setName(String name);
 
 		void setSymbol(String symbol);
 
 		void setTimeFrame(int itemNo);
 
 		void setStockQuote(StockQuote stockQuote);
 
 		void setSymbols(List<Symbol> symbols);
 
 		void setChartLink(String link);
 
 		void setChartData(ChartData chartData);
 
 		void setPresenter(Presenter presenter);
 
 		String getSymbol();
 
 		TimeFrame getTimeFrame();
 
 		void showDialogBox(String headerText, String style, String content);
 
 		ChartDataParams getChartDataParams();	
 
 		HasClickHandlers getShowButton();
 		
 		HasClickHandlers getImportButton(); 
 		
 	}
 	
 }
