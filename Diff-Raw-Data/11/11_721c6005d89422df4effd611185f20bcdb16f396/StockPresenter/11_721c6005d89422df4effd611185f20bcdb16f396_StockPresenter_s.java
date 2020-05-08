 package cl.votainteligente.inspector.client.presenters;
 
 import cl.votainteligente.inspector.client.i18n.ApplicationMessages;
 import cl.votainteligente.inspector.client.services.StockServiceAsync;
 import cl.votainteligente.inspector.client.uihandlers.StockUiHandlers;
 import cl.votainteligente.inspector.model.Stock;
 
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyStandard;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.ProxyPlace;
 import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.inject.Inject;
 
 public class StockPresenter extends Presenter<StockPresenter.MyView, StockPresenter.MyProxy> implements StockUiHandlers {
 	public static final String PLACE = "stock";
 	public static final String PARAM_STOCK_ID = "stockId";
 
 	public interface MyView extends View, HasUiHandlers<StockUiHandlers> {
 		void clearStockData();
 		void setStockName(String stockName);
 		void setStockFantasyName(String stockFantasyName);
 		void setStockInitialQuantity(String stockInitialQuantity);
 		void setStockUnit(String stockUnit);
 		void setStockEmissionDate(String stockEmissionDate);
 		void setStockRemark(String stockRemark);
 		void setStockTotalEquivalentAmount(String stockTotalEquivalentAmount);
 	}
 
 	@ProxyStandard
 	@NameToken(PLACE)
 	public interface MyProxy extends ProxyPlace<StockPresenter> {
 	}
 
 	@Inject
 	private ApplicationMessages applicationMessages;
 	@Inject
 	private StockServiceAsync stockService;
 	private Long stockId;
 	private Stock stock;
 
 	@Inject
 	public StockPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
 		super(eventBus, view, proxy);
 		getView().setUiHandlers(this);
 	}
 
 	@Override
 	protected void onReset() {
 		getView().clearStockData();
 
 		if (stockId != null) {
 			getStock(stockId);
 		}
 	}
 
 	@Override
 	protected void revealInParent() {
 		fireEvent(new RevealContentEvent(MainPresenter.SLOT_POPUP_CONTENT, this));
 	}
 
 	@Override
 	public void prepareFromRequest(PlaceRequest placeRequest) {
 		super.prepareFromRequest(placeRequest);
 
 		try {
 			stockId = Long.parseLong(placeRequest.getParameter(PARAM_STOCK_ID, null));
 		} catch (NumberFormatException nfe) {
 			stockId = null;
 		}
 	}
 
 	public Long getStockId() {
 		return stockId;
 	}
 
 	public void setStockId(Long stockId) {
 		this.stockId = stockId;
 	}
 
 	public void getStock(Long stockId) {
 		stockService.getStock(stockId, new AsyncCallback<Stock>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorStock());
 			}
 
 			@Override
 			public void onSuccess(Stock result) {
				// TODO: parlamentarian data

 				stock = result;
 
 				if (stock.getName() != null) {
 					getView().setStockName(stock.getName());
 				}
 
 				if (stock.getFantasyName() != null) {
 					getView().setStockFantasyName(stock.getFantasyName());
 				}
 
 				if (stock.getInitialQuantity() != null) {
 					getView().setStockInitialQuantity(stock.getInitialQuantity().toString());
 				}
 
 				if (stock.getUnit() != null) {
 					getView().setStockUnit(stock.getUnit());
 				}
 
 				if (stock.getEmissionDate() != null) {
 					getView().setStockEmissionDate(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).format(stock.getEmissionDate()));
 				}
 
 				if (stock.getRemark() != null) {
 					getView().setStockRemark(stock.getRemark());
 				}
 
 				if (stock.getTotalEquivalentAmount() != null) {
 					getView().setStockTotalEquivalentAmount(NumberFormat.getCurrencyFormat().format(stock.getTotalEquivalentAmount()));
 				}
 			}
 		});
 	}
 
 	@Override
 	public void close() {
 		History.back();
 	}
 }
