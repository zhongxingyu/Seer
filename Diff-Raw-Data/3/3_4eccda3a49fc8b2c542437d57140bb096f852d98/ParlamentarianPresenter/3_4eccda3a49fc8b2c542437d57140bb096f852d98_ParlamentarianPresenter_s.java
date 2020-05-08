 package cl.votainteligente.inspector.client.presenters;
 
 import cl.votainteligente.inspector.client.InlineHyperLinkCellData;
 import cl.votainteligente.inspector.client.MultipleInlineHyperLinkCell;
 import cl.votainteligente.inspector.client.MultipleInlineHyperLinkCellData;
 import cl.votainteligente.inspector.client.i18n.ApplicationMessages;
 import cl.votainteligente.inspector.client.services.ParlamentarianCommentServiceAsync;
 import cl.votainteligente.inspector.client.services.ParlamentarianServiceAsync;
 import cl.votainteligente.inspector.client.uihandlers.ParlamentarianUiHandlers;
 import cl.votainteligente.inspector.model.*;
 import cl.votainteligente.inspector.shared.NotificationEvent;
 import cl.votainteligente.inspector.shared.NotificationEventParams;
 import cl.votainteligente.inspector.shared.NotificationEventType;
 
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
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.inject.Inject;
 
 import java.util.*;
 
 public class ParlamentarianPresenter extends Presenter<ParlamentarianPresenter.MyView, ParlamentarianPresenter.MyProxy> implements ParlamentarianUiHandlers {
 	public static final String PLACE = "parlamentarian";
 	public static final String PARAM_PARLAMENTARIAN_ID = "parlamentarianId";
 	public static final String VOTAINTELIGENTE_PARLIAMENTARIAN_URL = "http://legislativo.votainteligente.cl/Parlamentario/show/id_parlamentario/";
 
 	public interface MyView extends View, HasUiHandlers<ParlamentarianUiHandlers> {
 		void clearParlamentarianData();
 		void setParlamentarianName(String parlamentarianName);
 		void setParlamentarianDescription(String parlamentarianDescription);
 		void setParlamentarianBirthDate(String parlamentarianBirthDate);
 		void setParlamentarianPermanentCommissions(String parlamentarianPermanentCommissions);
 		void setParlamentarianSpecialCommissions(String parlamentarianSpecialCommissions);
 		void setParlamentarianParty(String parlamentarianParty);
 		void setParlamentarianImage(String url);
 		void setInterestDeclarationLink(String interestDeclarationLink);
 		void setPatrimonyDeclarationLink(String patrimonyDeclarationLink);
 		void setReportConflictLink(String href);
 		CellTable<Society> getSocietyTable();
 		CellTable<Stock> getStockTable();
 		CellTable<ParlamentarianComment> getParlamentarianCommentTable();
 		void setConsistencyChartData(Map<String, Double> chartData);
 		void setPerAreaChartData(Map<String, Double> categoryChartData);
 		void setparliamentarianUrlToVotainteligente(String hrefToVotainteligente, String messageToVotainteligente);
 	}
 
 	@ProxyStandard
 	@NameToken(PLACE)
 	public interface MyProxy extends ProxyPlace<ParlamentarianPresenter> {
 	}
 
 	@Inject
 	private ApplicationMessages applicationMessages;
 	@Inject
 	private PlaceManager placeManager;
 	@Inject
 	private ParlamentarianServiceAsync parlamentarianService;
 	@Inject
 	private ParlamentarianCommentServiceAsync parlamentarianCommentService;
 	private Long parlamentarianId;
 	private Parlamentarian parlamentarian;
 	private Boolean interestDeclaration;
 	private Boolean patrimonyDeclaration;
 	private ListDataProvider<Society> societyData;
 	private ListDataProvider<Stock> stockData;
 	private ListDataProvider<ParlamentarianComment> parlamentarianCommentData;
 
 	@Inject
 	public ParlamentarianPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
 		super(eventBus, view, proxy);
 		getView().setUiHandlers(this);
 	}
 
 	@Override
 	protected void onReset() {
 	}
 
 	@Override
 	protected void onReveal() {
 		initSocietyTableColumns();
 		initStockTableColumns();
		initParlamentarianCommentTableColumns();
 		societyData = new ListDataProvider<Society>();
 		societyData.addDataDisplay(getView().getSocietyTable());
 		stockData = new ListDataProvider<Stock>();
 		stockData.addDataDisplay(getView().getStockTable());
 		parlamentarianCommentData = new ListDataProvider<ParlamentarianComment>();
 		parlamentarianCommentData.addDataDisplay(getView().getParlamentarianCommentTable());
 		getView().clearParlamentarianData();
 
 		if (parlamentarianId != null) {
 			getParlamentarian(parlamentarianId);
 		}
 		getView().setparliamentarianUrlToVotainteligente(VOTAINTELIGENTE_PARLIAMENTARIAN_URL + parlamentarianId, applicationMessages.getGeneralViewParliamentarianOnVotainteligente());
 		showReportConflictForm();
 	}
 
 	@Override
 	protected void revealInParent() {
 		fireEvent(new RevealContentEvent(MainPresenter.SLOT_MAIN_CONTENT, this));
 	}
 
 	@Override
 	public void prepareFromRequest(PlaceRequest placeRequest) {
 		super.prepareFromRequest(placeRequest);
 
 		try {
 			parlamentarianId = Long.parseLong(placeRequest.getParameter(PARAM_PARLAMENTARIAN_ID, null));
 		} catch (NumberFormatException nfe) {
 			parlamentarianId = null;
 		}
 	}
 
 	public Long getParlamentarianId() {
 		return parlamentarianId;
 	}
 
 	public void setParlamentarianId(Long parlamentarianId) {
 		this.parlamentarianId = parlamentarianId;
 	}
 
 	public void getParlamentarian(Long parlamentarianId) {
 		parlamentarianService.getParlamentarian(parlamentarianId, new AsyncCallback<Parlamentarian>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorParlamentarian());
 			}
 
 			@Override
 			public void onSuccess(Parlamentarian result) {
 				parlamentarian = result;
 
 				getParlamentarianComments();
 
 				getView().setParlamentarianName(parlamentarian.toString());
 
 				if (parlamentarian.getImage() != null) {
 					getView().setParlamentarianImage("images/parlamentarian/large/" + parlamentarian.getImage());
 				} else {
 					getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 				}
 
 				if (parlamentarian.getParlamentarianType() != null) {
 					if (parlamentarian.getDistrict() != null) {
 						getView().setParlamentarianDescription(parlamentarian.getParlamentarianType().toString() + " " + applicationMessages.getGeneralBy() + " " + parlamentarian.getDistrict().toString());
 					} else {
 						getView().setParlamentarianDescription(parlamentarian.getParlamentarianType().toString());
 					}
 				}
 
 				if (parlamentarian.getBirthDate() != null) {
 					getView().setParlamentarianBirthDate(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).format(parlamentarian.getBirthDate()));
 				}
 
 				if (parlamentarian.getPermanentCommissions() != null && !parlamentarian.getPermanentCommissions().isEmpty()) {
 					StringBuilder sb = new StringBuilder();
 					Iterator<Commission> iterator = parlamentarian.getPermanentCommissions().iterator();
 
 					while (iterator.hasNext()) {
 						sb.append(iterator.next().toString());
 
 						if (iterator.hasNext()) {
 							sb.append(", ");
 						} else {
 							sb.append(".");
 						}
 					}
 
 					getView().setParlamentarianPermanentCommissions(sb.toString());
 				}
 
 				if (parlamentarian.getSpecialCommissions() != null && !parlamentarian.getSpecialCommissions().isEmpty()) {
 					StringBuilder sb = new StringBuilder();
 					Iterator<Commission> iterator = parlamentarian.getSpecialCommissions().iterator();
 
 					while (iterator.hasNext()) {
 						sb.append(iterator.next().toString());
 
 						if (iterator.hasNext()) {
 							sb.append(", ");
 						} else {
 							sb.append(".");
 						}
 					}
 
 					getView().setParlamentarianSpecialCommissions(sb.toString());
 				}
 
 				if (parlamentarian.getParty() != null) {
 					getView().setParlamentarianParty(parlamentarian.getParty().toString());
 				}
 
 				if (parlamentarian.getInterestDeclarationFile() != null) {
 					interestDeclaration = true;
 					getView().setInterestDeclarationLink(parlamentarian.getInterestDeclarationFile());
 				} else {
 					interestDeclaration = false;
 				}
 
 				if (parlamentarian.getPatrimonyDeclarationFile() != null) {
 					patrimonyDeclaration = true;
 					getView().setPatrimonyDeclarationLink(parlamentarian.getPatrimonyDeclarationFile());
 				} else {
 					patrimonyDeclaration = false;
 				}
 
 				societyData = new ListDataProvider<Society>(new ArrayList<Society>(result.getSocieties().keySet()));
 				societyData.addDataDisplay(getView().getSocietyTable());
 
 				stockData = new ListDataProvider<Stock>(new ArrayList<Stock>(result.getStocks().keySet()));
 				stockData.addDataDisplay(getView().getStockTable());
 
 				Double reportedSocieties = 0d;
 				Double unreportedSocieties = 0d;
 
 				for (Boolean reported : parlamentarian.getSocieties().values()) {
 					if (reported) {
 						reportedSocieties++;
 					} else {
 						unreportedSocieties++;
 					}
 				}
 
 				Map<String, Double> chartData = new HashMap<String, Double>();
 				chartData.put(applicationMessages.getSocietyReported(), 100d * reportedSocieties / (reportedSocieties + unreportedSocieties));
 				chartData.put(applicationMessages.getSocietyUnreported(), 100d * unreportedSocieties / (reportedSocieties + unreportedSocieties));
 				getView().setConsistencyChartData(chartData);
 
 				Map<String, Double> categoryChartData = new HashMap<String, Double>();
 				Double numCategories = 0d;
 
 				for (Society parliamentarianSociety : parlamentarian.getSocieties().keySet()) {
 					for (Category category : parliamentarianSociety.getCategories()) {
 						Double currentCount = categoryChartData.get(category.getName());
 
 						if (currentCount == null) {
 							currentCount = 0d;
 						}
 
 						categoryChartData.put(category.getName(), ++currentCount);
 						numCategories++;
 					}
 				}
 
 				for (Stock parliamentarianStock : parlamentarian.getStocks().keySet()) {
 					for (Category category : parliamentarianStock.getCategories()) {
 						Double currentCount = categoryChartData.get(category.getName());
 
 						if (currentCount == null) {
 							currentCount = 0d;
 						}
 
 						categoryChartData.put(category.getName(), ++currentCount);
 						numCategories++;
 					}
 				}
 
 				for (String categoryName : categoryChartData.keySet()) {
 					categoryChartData.put(categoryName, 100d * categoryChartData.get(categoryName) / numCategories);
 				}
 				getView().setPerAreaChartData(categoryChartData);
 			}
 		});
 	}
 
 	public void getParlamentarianComments() {
 		parlamentarianCommentService.getAllParlamentarianComments(parlamentarianId, new AsyncCallback<List<ParlamentarianComment>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorParlamentarianCommentList());
 			}
 
 			@Override
 			public void onSuccess(List<ParlamentarianComment> result) {
 				if (result != null) {
 					parlamentarianCommentData = new ListDataProvider<ParlamentarianComment>(result);
 					parlamentarianCommentData.addDataDisplay(getView().getParlamentarianCommentTable());
 				}
 			}
 		});
 	}
 
 	private void initSocietyTableColumns() {
 
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
 
 				while (iterator.hasNext()) {
 					category = iterator.next();
 					placeRequest = new PlaceRequest(HomePresenter.PLACE);
 					placeRequest = placeRequest.with(HomePresenter.PARAM_CATEGORY_ID, category.getId().toString());
 					href = placeManager.buildHistoryToken(placeRequest);
 
 					InlineHyperLinkCellData param = new InlineHyperLinkCellData();
 					if (iterator.hasNext()) {
 						param.setValue(category.getName() + ", ");
 					} else {
 						param.setValue(category.getName() + ".");
 					}
 
 					param.setHref(href);
 					param.setStyleNames("");
 					params.add(param);
 				}
 				MultipleInlineHyperLinkCellData cellData = new MultipleInlineHyperLinkCellData();
 				cellData.setCellData(params);
 				return cellData;
 			}
 		};
 
 		// Adds categories column to table
 		getView().getSocietyTable().addColumn(categoriesColumn, applicationMessages.getGeneralCategory());
 
 		TextColumn<Society> societyLegalNameColumn = new TextColumn<Society>() {
 			@Override
 			public String getValue(Society society) {
 				return society.getName();
 			}
 		};
 
 		getView().getSocietyTable().addColumn(societyLegalNameColumn, applicationMessages.getSocietyLegalName());
 
 		Column<Society, String> societyReportedThisColumn = new Column<Society, String>(new ImageCell()){
 			@Override
 			public String getValue(Society society) {
 				Boolean declared = parlamentarian.getSocieties().get(society);
 
 				if (declared != null && declared) {
 					return "images/declare_yes.png";
 				} else {
 					return "images/declare_no.png";
 				}
 			}
 		};
 
 		getView().getSocietyTable().addColumn(societyReportedThisColumn, applicationMessages.getSocietyReportedThis());
 
 		TextColumn<Society> societyIsInConflictColumn = new TextColumn<Society>() {
 			@Override
 			public String getValue(Society society) {
 				for (Category category : society.getCategories()) {
 					for (Bill bill : parlamentarian.getAuthoredBills()) {
 						if (bill.getCategories().contains(category)) {
 							return applicationMessages.getGeneralYes();
 						}
 					}
 
 					for (Bill bill : parlamentarian.getVotedBills()) {
 						if (bill.getCategories().contains(category)) {
 							return applicationMessages.getGeneralYes();
 						}
 					}
 				}
 
 				return applicationMessages.getGeneralNo();
 			}
 		};
 
 		getView().getSocietyTable().addColumn(societyIsInConflictColumn, applicationMessages.getSocietyPotentialConflict());
 
 		Column<Society, Society> viewSocietyColumn = new Column<Society, Society>(new ActionCell<Society>("", new ActionCell.Delegate<Society>() {
 
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
 						return "<img style=\"cursor: pointer;\" src=\"images/more.png\"/>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Society getValue(Society society) {
 				return society;
 			}
 		};
 
 		getView().getSocietyTable().addColumn(viewSocietyColumn, applicationMessages.getSocietyViewMore());
 	}
 
 	private void initStockTableColumns() {
 
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
 
 				while (iterator.hasNext()) {
 					category = iterator.next();
 					placeRequest = new PlaceRequest(HomePresenter.PLACE);
 					placeRequest = placeRequest.with(HomePresenter.PARAM_CATEGORY_ID, category.getId().toString());
 					href = placeManager.buildHistoryToken(placeRequest);
 
 					InlineHyperLinkCellData param = new InlineHyperLinkCellData();
 					if (iterator.hasNext()) {
 						param.setValue(category.getName() + ", ");
 					} else {
 						param.setValue(category.getName() + ".");
 					}
 
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
 
 		TextColumn<Stock> stockLegalNameColumn = new TextColumn<Stock>() {
 			@Override
 			public String getValue(Stock stock) {
 				return stock.getName();
 			}
 		};
 
 		getView().getStockTable().addColumn(stockLegalNameColumn, applicationMessages.getStockLegalName());
 
 		Column<Stock, String> stockReportedThisColumn = new Column<Stock, String>(new ImageCell()){
 			@Override
 			public String getValue(Stock stock) {
 				Boolean declared = parlamentarian.getStocks().get(stock);
 
 				if (declared != null && declared) {
 					return "images/declare_yes.png";
 				} else {
 					return "images/declare_no.png";
 				}
 			}
 		};
 
 		getView().getStockTable().addColumn(stockReportedThisColumn, applicationMessages.getStockReportedThis());
 
 		TextColumn<Stock> stockIsInConflictColumn = new TextColumn<Stock>() {
 			@Override
 			public String getValue(Stock stock) {
 				for (Category category : stock.getCategories()) {
 					for (Bill bill : parlamentarian.getAuthoredBills()) {
 						if (bill.getCategories().contains(category)) {
 							return applicationMessages.getGeneralYes();
 						}
 					}
 
 					for (Bill bill : parlamentarian.getVotedBills()) {
 						if (bill.getCategories().contains(category)) {
 							return applicationMessages.getGeneralYes();
 						}
 					}
 				}
 
 				return applicationMessages.getGeneralNo();
 			}
 		};
 
 		getView().getStockTable().addColumn(stockIsInConflictColumn, applicationMessages.getStockPotentialConflict());
 
 		Column<Stock, Stock> viewStockColumn = new Column<Stock, Stock>(new ActionCell<Stock>("", new ActionCell.Delegate<Stock>() {
 
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
 						return "<img style=\"cursor: pointer;\" src=\"images/more.png\"/>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Stock getValue(Stock stock) {
 				return stock;
 			}
 		};
 
 		getView().getStockTable().addColumn(viewStockColumn, applicationMessages.getStockViewMore());
 	}
 
 	private void initParlamentarianCommentTableColumns() {
 
 		while (getView().getParlamentarianCommentTable().getColumnCount() > 0) {
 			getView().getParlamentarianCommentTable().removeColumn(0);
 		}
 
 		TextColumn<ParlamentarianComment> subjectColumn = new TextColumn<ParlamentarianComment>() {
 			@Override
 			public String getValue(ParlamentarianComment parlamentarianComment) {
 				return parlamentarianComment.getSubject();
 			}
 		};
 
 		getView().getParlamentarianCommentTable().addColumn(subjectColumn);
 
 		// Sets selection model for each row
 		final SingleSelectionModel<ParlamentarianComment> selectionModel = new SingleSelectionModel<ParlamentarianComment>(ParlamentarianComment.KEY_PROVIDER);
 		getView().getParlamentarianCommentTable().setSelectionModel(selectionModel);
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			public void onSelectionChange(SelectionChangeEvent event) {
 				if (selectionModel.getSelectedObject() != null) {
 					PlaceRequest placeRequest = new PlaceRequest(ParlamentarianCommentDisplayPresenter.PLACE);
 					placeManager.revealPlace(placeRequest.with(ParlamentarianCommentDisplayPresenter.PARAM_PARLAMENTARIAN_COMMENT_ID, selectionModel.getSelectedObject().getId().toString()));
 				}
 			}
 		});
 	}
 
 	@Override
 	public void showNotification(String message, NotificationEventType type) {
 		NotificationEventParams params = new NotificationEventParams();
 		params.setMessage(message);
 		params.setType(type);
 		params.setDuration(NotificationEventParams.DURATION_NORMAL);
 		fireEvent(new NotificationEvent(params));
 	}
 
 	@Override
 	public Boolean getInterestDeclaration() {
 		return interestDeclaration;
 	}
 
 	@Override
 	public Boolean getPatrimonyDeclaration() {
 		return patrimonyDeclaration;
 	}
 
 	public void showReportConflictForm() {
 		if (parlamentarianId != null) {
 			PlaceRequest placeRequest = new PlaceRequest(ReportConflictPresenter.PLACE)
 			.with(ReportConflictPresenter.PARAM_PARLAMENTARIAN_ID, parlamentarianId.toString());
 			String href = placeManager.buildHistoryToken(placeRequest);
 			getView().setReportConflictLink("#" + href);
 		} else {
 			getView().setReportConflictLink(null);
 		}
 	}
 }
