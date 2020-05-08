 package cl.votainteligente.inspector.client.presenters;
 
 import cl.votainteligente.inspector.client.*;
 import cl.votainteligente.inspector.client.i18n.ApplicationMessages;
 import cl.votainteligente.inspector.client.services.BillServiceAsync;
 import cl.votainteligente.inspector.client.services.ParlamentarianServiceAsync;
 import cl.votainteligente.inspector.client.uihandlers.BillUiHandlers;
 import cl.votainteligente.inspector.model.*;
 import cl.votainteligente.inspector.shared.*;
 
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyStandard;
 import com.gwtplatform.mvp.client.proxy.*;
 
 import com.google.gwt.cell.client.ActionCell;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.ImageCell;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.view.client.*;
 import com.google.inject.Inject;
 
 import java.util.*;
 
 public class BillPresenter extends Presenter<BillPresenter.MyView, BillPresenter.MyProxy> implements BillUiHandlers {
 	public static final String PLACE = "bill";
 	public static final String PARAM_BILL_ID = "billId";
 	public static final String PARAM_PARLAMENTARIAN_ID = "parlamentarianId";
 	public static final String VOTAINTELIGENTE_BILL_URL = "http://legislativo.votainteligente.cl/ProyectoLey/show/id_proyecto_ley/";
 
 	public interface MyView extends View, HasUiHandlers<BillUiHandlers> {
 		void setBillBulletinNumber(String billBulletinNumber);
 		void setBillTitle(String billTitle);
 		void setBillDescription(String billContent);
 		void addBillAuthors(Parlamentarian parlamentarian, String href, Boolean hasNext);
 		void clearBillAuthors();
 		void setBillEntryDate(Date billEntryDate);
 		void setBillInitiativeType(String billInitiativeType);
 		void setBillType(String billType);
 		void setBillOriginChamber(String billOriginChamber);
 		void setBillUrgency(String billUrgency);
 		void setBillStage(String billStage);
 		void addBillCategories(Category category, String href, Boolean hasNext);
 		void clearBillCategories();
 		void setParlamentarianImage(String parlamentarianImageUrl);
 		void setParlamentarianDisplay(String parlamentarianName);
 		CellTable<Parlamentarian> getParlamentarianTable();
 		void setParlamentarianTable(CellTable<Parlamentarian> parlamentarianTable);
 		CellTable<Society> getSocietyTable();
 		CellTable<Stock> getStockTable();
 		void setSocietyTable(CellTable<Society> societyTable);
 		void setStockTable(CellTable<Stock> stockTable);
 		void setbillUrlToVotainteligente(String hrefToVotainteligente, String messageToVotainteligente);
 		void setShare(String href, String billTitle);
 		String getEmptySocietyTableWidget();
 		String getEmptyStockTableWidget();
 	}
 
 	@ProxyStandard
 	@NameToken(PLACE)
 	public interface MyProxy extends ProxyPlace<BillPresenter> {
 	}
 
 	@Inject
 	private ApplicationMessages applicationMessages;
 	@Inject
 	private PlaceManager placeManager;
 	@Inject
 	private BillServiceAsync billService;
 	@Inject
 	private ParlamentarianServiceAsync parlamentarianService;
 
 	private Long billId;
 	private Long parlamentarianId;
 	private Bill selectedBill;
 	private Parlamentarian selectedParlamentarian;
 	private AbstractDataProvider<Parlamentarian> parlamentarianData;
 	private AbstractDataProvider<Society> societyData;
 	private AbstractDataProvider<Stock> stockData;
 
 	@Inject
 	public BillPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
 		super(eventBus, view, proxy);
 		getView().setUiHandlers(this);
 	}
 
 	@Override
 	protected void onReset() {
 	}
 
 	@Override
 	protected void onReveal() {
 		selectedBill = null;
 		parlamentarianData = new ListDataProvider<Parlamentarian>();
 		societyData = new ListDataProvider<Society>();
 		stockData = new ListDataProvider<Stock>();
 		initParlamentarianTable();
 		initSocietyTable();
 		initStockTable();
 		setParlamentarianData(parlamentarianData);
 		setSocietyData(societyData);
 		setStockData(stockData);
 
 		if (billId != null) {
 			showBill();
 		}
 
 		getView().setParlamentarianDisplay(applicationMessages.getGeneralParlamentarian());
 		getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 		getView().setbillUrlToVotainteligente(VOTAINTELIGENTE_BILL_URL+billId, applicationMessages.getGeneralViewProjectOnVotainteligente());
 	}
 
 	@Override
 	protected void revealInParent() {
 		fireEvent(new RevealContentEvent(MainPresenter.SLOT_MAIN_CONTENT, this));
 	}
 
 	@Override
 	public void prepareFromRequest(PlaceRequest placeRequest) {
 		super.prepareFromRequest(placeRequest);
 
 		try {
 			billId = Long.parseLong(placeRequest.getParameter(PARAM_BILL_ID, null));
 		} catch (NumberFormatException nfe) {
 			billId = null;
 		}
 
 		try {
 			parlamentarianId = Long.parseLong(placeRequest.getParameter(PARAM_PARLAMENTARIAN_ID, null));
 		} catch (NumberFormatException nfe) {
 			parlamentarianId = null;
 		}
 	}
 
 	public Long getBillId() {
 		return billId;
 	}
 
 	public void setBillId(Long billId) {
 		this.billId = billId;
 	}
 
 	public Long getParlamentarianId() {
 		return parlamentarianId;
 	}
 
 	public void setParlamentarianId(Long parlamentarianId) {
 		this.parlamentarianId = parlamentarianId;
 	}
 
 	public void setSelectedParlamentarian(Parlamentarian parlamentarian) {
 		this.selectedParlamentarian = parlamentarian;
 	}
 
 	public Parlamentarian getSelectedParlamentarian() {
 		return selectedParlamentarian;
 	}
 
 	public void showBill() {
 		fireEvent(new ShowLoadingEvent());
 		billService.getBill(billId, new AsyncCallback<Bill>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				fireEvent(new HideLoadingEvent());
 				NotificationEventParams params = new NotificationEventParams();
 				params.setMessage(applicationMessages.getErrorBill());
 				params.setType(NotificationEventType.ERROR);
 				params.setDuration(NotificationEventParams.DURATION_SHORT);
 				fireEvent(new NotificationEvent(params));
 			}
 
 			@Override
 			public void onSuccess(Bill result) {
 				if (result != null) {
 					selectedBill = result;
 					getView().setBillBulletinNumber(selectedBill.getBulletinNumber());
 					getView().setBillTitle(selectedBill.getTitle());
 					getView().setBillDescription(selectedBill.getDescription());
 					getView().setBillEntryDate(selectedBill.getEntryDate());
 					getView().setBillInitiativeType(selectedBill.getInitiativeType().getName());
 					getView().setBillType(selectedBill.getBillType().getName());
 					getView().setBillOriginChamber(selectedBill.getOriginChamber().getName());
 					getView().setBillUrgency(selectedBill.getUrgency().getName());
 					getView().setBillStage(selectedBill.getStage().getName());
 
 					Iterator<Category> iterator = selectedBill.getCategories().iterator();
 					getView().clearBillCategories();
 					Category category = null;
 					String href = null;
 
 					while (iterator.hasNext()) {
 						category = iterator.next();
 						PlaceRequest placeRequest = new PlaceRequest(HomePresenter.PLACE).with(HomePresenter.PARAM_CATEGORY_ID, category.getId().toString());
 						href = placeManager.buildHistoryToken(placeRequest);
 						getView().addBillCategories(category, href, iterator.hasNext());
 					}
 
 					if (parlamentarianId != null) {
 						loadSelectedParlamentarian();
 					} else {
 						setSelectedParlamentarian(new Parlamentarian());
 					}
 					getBillAuthors(result);
 					getParlamentarians(result);
 				}
 				getView().setShare(Window.Location.getHref(), selectedBill.getTitle().substring(0, 40));
 				fireEvent(new HideLoadingEvent());
 			}
 		});
 	}
 
 	public void loadSelectedParlamentarian() {
 		fireEvent(new ShowLoadingEvent());
 		parlamentarianService.getParlamentarian(parlamentarianId, new AsyncCallback<Parlamentarian>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				fireEvent(new HideLoadingEvent());
 				NotificationEventParams params = new NotificationEventParams();
 				params.setMessage(applicationMessages.getErrorParlamentarian());
 				params.setType(NotificationEventType.ERROR);
 				params.setDuration(NotificationEventParams.DURATION_SHORT);
 				fireEvent(new NotificationEvent(params));
 			}
 
 			@Override
 			public void onSuccess(Parlamentarian result) {
 				setSelectedParlamentarian(result);
 				showSelectedParlamentarian();
 				fireEvent(new HideLoadingEvent());
 			}
 		});
 	}
 
 	public void showSelectedParlamentarian() {
 		if (selectedParlamentarian != null) {
 			getView().setParlamentarianDisplay(selectedParlamentarian.toString());
 			if (selectedParlamentarian.getImage() != null) {
 				getView().setParlamentarianImage("images/parlamentarian/large/" + selectedParlamentarian.getImage());
 			} else {
 				getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 			}
 			List<Society> societies = new ArrayList<Society>(selectedParlamentarian.getSocieties().keySet());
 			Set<Society> resultSet = new TreeSet<Society>();
 			Set<Category> intersection = new HashSet<Category>();
 
 			for (Society society : societies) {
 				intersection = new HashSet<Category>(society.getCategories());
 				intersection.retainAll(selectedBill.getCategories());
 				if (intersection.size() > 0) {
 					resultSet.add(society);
 				}
 			}
 
 			societies = new ArrayList<Society>(resultSet);
 
 			AbstractDataProvider<Society> data = new ListDataProvider<Society>(societies);
 			setSocietyData(data);
 
 			List<Stock> stocks = new ArrayList<Stock>(selectedParlamentarian.getStocks().keySet());
 			Set<Stock> stockResultSet = new TreeSet<Stock>();
 			Set<Category> stockIntersection = new HashSet<Category>();
 
 			for (Stock stock : stocks) {
 				stockIntersection = new HashSet<Category>(stock.getCategories());
 				stockIntersection.retainAll(selectedBill.getCategories());
 				if (stockIntersection.size() > 0) {
 					stockResultSet.add(stock);
 				}
 			}
 
 			stocks = new ArrayList<Stock>(stockResultSet);
 
 			AbstractDataProvider<Stock> stockData = new ListDataProvider<Stock>(stocks);
 			setStockData(stockData);
 		}
 	}
 
 	public void getParlamentarians(Bill bill) {
 		fireEvent(new ShowLoadingEvent());
 		parlamentarianService.getParlamentariansByBill(bill, new AsyncCallback<List<Parlamentarian>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				fireEvent(new HideLoadingEvent());
 				NotificationEventParams params = new NotificationEventParams();
 				params.setMessage(applicationMessages.getErrorParlamentarianBillSearch());
 				params.setType(NotificationEventType.ERROR);
 				params.setDuration(NotificationEventParams.DURATION_SHORT);
 				fireEvent(new NotificationEvent(params));
 			}
 
 			@Override
 			public void onSuccess(List<Parlamentarian> result) {
 				if (result != null) {
 					ListDataProvider<Parlamentarian> data = new ListDataProvider<Parlamentarian>(result);
 					setParlamentarianData(data);
 				}
 				fireEvent(new HideLoadingEvent());
 			}
 		});
 	}
 
 	public void getBillAuthors(Bill bill) {
 		fireEvent(new ShowLoadingEvent());
 		parlamentarianService.getBillAuthors(bill, new AsyncCallback<List<Parlamentarian>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				fireEvent(new HideLoadingEvent());
 				NotificationEventParams params = new NotificationEventParams();
 				params.setMessage(applicationMessages.getErrorBillAuthors());
 				params.setType(NotificationEventType.ERROR);
 				params.setDuration(NotificationEventParams.DURATION_SHORT);
 				fireEvent(new NotificationEvent(params));
 			}
 
 			@Override
 			public void onSuccess(List<Parlamentarian> result) {
 				if (result != null) {
 					Iterator<Parlamentarian> iterator = result.iterator();
 					getView().clearBillAuthors();
 					Parlamentarian parlamentarian = null;
 					String href = null;
 
 					while (iterator.hasNext()) {
 						parlamentarian = iterator.next();
 						PlaceRequest placeRequest = new PlaceRequest(ParlamentarianPresenter.PLACE).with(HomePresenter.PARAM_PARLAMENTARIAN_ID, parlamentarian.getId().toString());
 						href = placeManager.buildHistoryToken(placeRequest);
 						getView().addBillAuthors(parlamentarian, href, iterator.hasNext());
 					}
 				}
 				fireEvent(new HideLoadingEvent());
 			}
 		});
 	}
 
 	public AbstractDataProvider<Parlamentarian> getParlamentarianData() {
 		return parlamentarianData;
 	}
 
 	public void setParlamentarianData(AbstractDataProvider<Parlamentarian> data) {
 		parlamentarianData = data;
 		parlamentarianData.addDataDisplay(getView().getParlamentarianTable());
 	}
 
 	public AbstractDataProvider<Society> getSocietyData() {
 		return societyData;
 	}
 
 	public void setSocietyData(AbstractDataProvider<Society> data) {
 		societyData = data;
 		societyData.addDataDisplay(getView().getSocietyTable());
 	}
 
 	public AbstractDataProvider<Stock> getStockData() {
 		return stockData;
 	}
 
 	public void setStockData(AbstractDataProvider<Stock> data) {
 		stockData = data;
 		stockData.addDataDisplay(getView().getStockTable());
 	}
 
 	public void initParlamentarianTable() {
 		while (getView().getParlamentarianTable().getColumnCount() > 0) {
 			getView().getParlamentarianTable().removeColumn(0);
 		}
 
 		// Creates image column
 		Column<Parlamentarian, String> imageColumn = new Column<Parlamentarian, String>(new ImageCell()){
 			@Override
 			public String getValue(Parlamentarian parlamentarian) {
 
 				if (parlamentarian.getImage() != null) {
 					return "images/parlamentarian/small/" + parlamentarian.getImage();
 				} else {
 					return "images/parlamentarian/small/avatar.png";
 				}
 			}
 		};
 
 		// Adds image column to table
 		getView().getParlamentarianTable().addColumn(imageColumn, "");
 
 		// Creates name column
 		TextColumn<Parlamentarian> nameColumn = new TextColumn<Parlamentarian>() {
 
 			@Override
 			public String getValue(Parlamentarian parlamentarian) {
 				return parlamentarian.toString();
 			}
 		};
 
 		// Adds name column to table
 		getView().getParlamentarianTable().addColumn(nameColumn, applicationMessages.getGeneralParlamentarian());
 
 		// Creates action profile column
 		Column<Parlamentarian, InlineHyperLinkCellData> profileColumn = new Column<Parlamentarian, InlineHyperLinkCellData>(new InlineHyperLinkCell()) {
 
 			@Override
 			public InlineHyperLinkCellData getValue(Parlamentarian parlamentian) {
 				PlaceRequest placeRequest = new PlaceRequest(ParlamentarianPresenter.PLACE);
 				placeRequest = placeRequest.with(ParlamentarianPresenter.PARAM_PARLAMENTARIAN_ID, parlamentian.getId().toString());
 				String href = placeManager.buildHistoryToken(placeRequest);
 
 				InlineHyperLinkCellData params = new InlineHyperLinkCellData();
 				params.setHref(href);
 				params.setStyleNames("profileButton");
 				return params;
 			}
 		};
 
 		// Adds action profile column to table
 		getView().getParlamentarianTable().addColumn(profileColumn, applicationMessages.getGeneralProfile());
 
 		// Sets selection model for each row
 		final SingleSelectionModel<Parlamentarian> selectionModel = new SingleSelectionModel<Parlamentarian>(Parlamentarian.KEY_PROVIDER);
 		getView().getParlamentarianTable().setSelectionModel(selectionModel);
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			public void onSelectionChange(SelectionChangeEvent event) {
 				if (selectionModel.getSelectedObject() != null) {
 					parlamentarianId = selectionModel.getSelectedObject().getId();
 					setHistoryToken();
					loadSelectedParlamentarian();
 				}
 			}
 		});
 	}
 
 	public void initSocietyTable() {
 		while (getView().getSocietyTable().getColumnCount() > 0) {
 			getView().getSocietyTable().removeColumn(0);
 		}
 
 		// Creates categories column
 		Column<Society, MultipleInlineHyperLinkCellData> categoriesColumn = new Column<Society, MultipleInlineHyperLinkCellData>(new MultipleInlineHyperLinkCell()) {
 
 			@Override
 			public MultipleInlineHyperLinkCellData getValue(Society society) {
 				Iterator<Category> iterator = society.getCategories().iterator();
 				List<InlineHyperLinkCellData> params = new ArrayList<InlineHyperLinkCellData>();
 				PlaceRequest placeRequest = null;
 				String href = null;
 				Category category = null;
 				InlineHyperLinkCellData param;
 
 				while (iterator.hasNext()) {
 					category = iterator.next();
 					placeRequest = new PlaceRequest(HomePresenter.PLACE);
 					placeRequest = placeRequest.with(HomePresenter.PARAM_CATEGORY_ID, category.getId().toString());
 					href = placeManager.buildHistoryToken(placeRequest);
 
 					param = new InlineHyperLinkCellData();
 					param.setValue(category.getName());
 					param.setHref(href);
 					param.setStyleNames("");
 					params.add(param);
 				}
 				MultipleInlineHyperLinkCellData cellData = new MultipleInlineHyperLinkCellData();
 				cellData.setCellData(params);
 				return cellData;
 			}
 		};
 
 		// Adds name column to table
 		getView().getSocietyTable().addColumn(categoriesColumn, applicationMessages.getGeneralCategory());
 
 		// Creates name column
 		TextColumn<Society> nameColumn = new TextColumn<Society>() {
 
 			@Override
 			public String getValue(Society society) {
 				if (society.getFantasyName() != null) {
 					return society.getFantasyName();
 				} else if (society.getName() != null) {
 					return society.getName();
 				} else if (society.getUid() != null) {
 					return society.getUid();
 				}
 				return applicationMessages.getGeneralWithoutInformation();
 			}
 		};
 
 		// Adds name column to table
 		getView().getSocietyTable().addColumn(nameColumn, applicationMessages.getGeneralSocietiesInConflict());
 
 		// Creates reported column
 		Column<Society, String> reportedColumn = new Column<Society, String>(new ImageCell()) {
 
 			@Override
 			public String getValue(Society society) {
 				String reported = "images/declare_no.png";
 
 				if (selectedParlamentarian != null && selectedParlamentarian.getSocieties().containsKey(society)) {
 					if (selectedParlamentarian.getSocieties().get(society) == true) {
 						reported = "images/declare_yes.png";
 					}
 				}
 				return reported;
 			}
 		};
 
 		// Adds name reported to table
 		getView().getSocietyTable().addColumn(reportedColumn, applicationMessages.getSocietyReported());
 
 		// Creates action profile column
 		Column<Society, Society> profileColumn = new Column<Society, Society>(new ActionCell<Society>("", new ActionCell.Delegate<Society>() {
 
 			@Override
 			public void execute(Society society) {
 				PlaceRequest placeRequest = new PlaceRequest(SocietyPresenter.PLACE);
 				placeManager.revealPlace(placeRequest.with(SocietyPresenter.PARAM_SOCIETY_ID, society.getId().toString()));
 			}
 		}) {
 			@Override
 			public void render(Cell.Context context, Society value, SafeHtmlBuilder sb) {
 				sb.append(new SafeHtml() {
 
 					@Override
 					public String asString() {
 						return "<div class=\"glassButton\"></div>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Society getValue(Society society) {
 				return society;
 			}
 		};
 
 		// Adds action profile column to table
 		getView().getSocietyTable().addColumn(profileColumn, applicationMessages.getGeneralViewMore());
 
 		HTMLPanel emptyTableWidget = new HTMLPanel(applicationMessages.getSocietyNoSocietiesFound());
 		emptyTableWidget.addStyleName(getView().getEmptySocietyTableWidget());
 		getView().getSocietyTable().setEmptyTableWidget(emptyTableWidget);
 	}
 
 	public void initStockTable() {
 		while (getView().getStockTable().getColumnCount() > 0) {
 			getView().getStockTable().removeColumn(0);
 		}
 
 		// Creates categories column
 		Column<Stock, MultipleInlineHyperLinkCellData> categoriesColumn = new Column<Stock, MultipleInlineHyperLinkCellData>(new MultipleInlineHyperLinkCell()) {
 
 			@Override
 			public MultipleInlineHyperLinkCellData getValue(Stock stock) {
 				Iterator<Category> iterator = stock.getCategories().iterator();
 				List<InlineHyperLinkCellData> params = new ArrayList<InlineHyperLinkCellData>();
 				PlaceRequest placeRequest = null;
 				String href = null;
 				Category category = null;
 				InlineHyperLinkCellData param;
 
 				while (iterator.hasNext()) {
 					category = iterator.next();
 					placeRequest = new PlaceRequest(HomePresenter.PLACE);
 					placeRequest = placeRequest.with(HomePresenter.PARAM_CATEGORY_ID, category.getId().toString());
 					href = placeManager.buildHistoryToken(placeRequest);
 
 					param = new InlineHyperLinkCellData();
 					param.setValue(category.getName());
 					param.setHref(href);
 					param.setStyleNames("");
 					params.add(param);
 				}
 				MultipleInlineHyperLinkCellData cellData = new MultipleInlineHyperLinkCellData();
 				cellData.setCellData(params);
 				return cellData;
 			}
 		};
 
 		// Adds name column to table
 		getView().getStockTable().addColumn(categoriesColumn, applicationMessages.getGeneralCategory());
 
 		// Creates name column
 		TextColumn<Stock> nameColumn = new TextColumn<Stock>() {
 
 			@Override
 			public String getValue(Stock stock) {
 				if (stock.getFantasyName() != null) {
 					return stock.getFantasyName();
 				} else if (stock.getName() != null) {
 					return stock.getName();
 				}
 				return applicationMessages.getGeneralWithoutInformation();
 			}
 		};
 
 		// Adds name column to table
 		getView().getStockTable().addColumn(nameColumn, applicationMessages.getGeneralStocksInConflict());
 
 		// Creates reported column
 		Column<Stock, String> reportedColumn = new Column<Stock, String>(new ImageCell()) {
 
 			@Override
 			public String getValue(Stock stock) {
 				String reported = "images/declare_no.png";
 
 				if (selectedParlamentarian != null && selectedParlamentarian.getStocks().containsKey(stock)) {
 					if (selectedParlamentarian.getStocks().get(stock) == true) {
 						reported = "images/declare_yes.png";
 					}
 				}
 				return reported;
 			}
 		};
 
 		// Adds name reported to table
 		getView().getStockTable().addColumn(reportedColumn, applicationMessages.getStockReported());
 
 		// Creates action profile column
 		Column<Stock, Stock> profileColumn = new Column<Stock, Stock>(new ActionCell<Stock>("", new ActionCell.Delegate<Stock>() {
 
 			@Override
 			public void execute(Stock stock) {
 				PlaceRequest placeRequest = new PlaceRequest(StockPresenter.PLACE);
 				placeManager.revealPlace(placeRequest.with(StockPresenter.PARAM_STOCK_ID, stock.getId().toString()));
 			}
 		}) {
 			@Override
 			public void render(Cell.Context context, Stock value, SafeHtmlBuilder sb) {
 				sb.append(new SafeHtml() {
 
 					@Override
 					public String asString() {
 						return "<div class=\"glassButton\"></div>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Stock getValue(Stock stock) {
 				return stock;
 			}
 		};
 
 		// Adds action profile column to table
 		getView().getStockTable().addColumn(profileColumn, applicationMessages.getGeneralViewMore());
 
 		HTMLPanel emptyTableWidget = new HTMLPanel(applicationMessages.getStockNoStocksFound());
 		emptyTableWidget.addStyleName(getView().getEmptyStockTableWidget());
 		getView().getStockTable().setEmptyTableWidget(emptyTableWidget);
 	}
 
 	public void setHistoryToken() {
 		if (selectedParlamentarian != null && selectedBill != null) {
 			PlaceRequest placeRequest = null;
 			String href = null;
 
 			placeRequest = new PlaceRequest(BillPresenter.PLACE)
 			.with(BillPresenter.PARAM_BILL_ID, billId.toString())
 			.with(BillPresenter.PARAM_PARLAMENTARIAN_ID, parlamentarianId.toString());
 			href = placeManager.buildHistoryToken(placeRequest);
 			placeManager.revealPlace(placeRequest);
 			History.newItem(href);
 			getView().setShare(Window.Location.getHref(), selectedBill.getTitle().substring(0, 40));
 		}
 	}
 
 	@Override
 	public void showParlamentarianProfile() {
 		if (selectedParlamentarian != null) {
 			PlaceRequest placeRequest = new PlaceRequest(ParlamentarianPresenter.PLACE);
 			placeManager.revealPlace(placeRequest.with(ParlamentarianPresenter.PARAM_PARLAMENTARIAN_ID, selectedParlamentarian.getId().toString()));
 		}
 	}
 }
