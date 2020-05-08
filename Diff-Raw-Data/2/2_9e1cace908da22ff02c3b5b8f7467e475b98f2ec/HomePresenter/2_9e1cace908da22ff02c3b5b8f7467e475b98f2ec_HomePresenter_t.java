 package cl.votainteligente.inspector.client.presenters;
 
 import cl.votainteligente.inspector.client.i18n.ApplicationMessages;
 import cl.votainteligente.inspector.client.services.BillServiceAsync;
 import cl.votainteligente.inspector.client.services.CategoryServiceAsync;
 import cl.votainteligente.inspector.client.services.ParlamentarianServiceAsync;
 import cl.votainteligente.inspector.model.Bill;
 import cl.votainteligente.inspector.model.Category;
 import cl.votainteligente.inspector.model.Parlamentarian;
 
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
 import com.google.gwt.user.cellview.client.*;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.*;
 import com.google.inject.Inject;
 
 import java.util.Comparator;
 import java.util.List;
 
 public class HomePresenter extends Presenter<HomePresenter.MyView, HomePresenter.MyProxy> implements HomePresenterIface {
 	public static final String PLACE = "home";
 
 	public interface MyView extends View {
 		void setPresenter(HomePresenterIface presenter);
 		void getParlamentarianSearch();
 		void getCategorySearch();
 		CellTable<Parlamentarian> getParlamentarianTable();
 		CellTable<Category> getCategoryTable();
 		CellTable<Bill> getBillTable();
 		void setParlamentarianDisplay(String parlamentarianName);
 		void setCategoryDisplay(String categoryName);
 		void setParlamentarianImage(String parlamentarianImage);
 	}
 
 	@ProxyStandard
 	@NameToken(PLACE)
 	public interface MyProxy extends ProxyPlace<HomePresenter> {
 	}
 
 	@Inject
 	private ApplicationMessages applicationMessages;
 	@Inject
 	private PlaceManager placeManager;
 	@Inject
 	private ParlamentarianServiceAsync parlamentarianService;
 	@Inject
 	private CategoryServiceAsync categoryService;
 	@Inject
 	private BillServiceAsync billService;
 
 	private AbstractDataProvider<Parlamentarian> parlamentarianData;
 	private AbstractDataProvider<Category> categoryData;
 	private AbstractDataProvider<Bill> billData;
 	private Parlamentarian selectedParlamentarian;
 	private Category selectedCategory;
 
 	@Inject
 	public HomePresenter(EventBus eventBus, MyView view, MyProxy proxy) {
 		super(eventBus, view, proxy);
 	}
 
 	@Override
 	protected void onBind() {
 		getView().setPresenter(this);
 	}
 
 	@Override
 	protected void onReset() {
 		parlamentarianData = new ListDataProvider<Parlamentarian>();
 		categoryData = new ListDataProvider<Category>();
 		billData = new ListDataProvider<Bill>();
 		selectedParlamentarian = null;
 		selectedCategory = null;
 		initParlamentarianTable();
 		initCategoryTable();
 		initBillTable();
 		initDataLoad();
 		getView().setParlamentarianDisplay(applicationMessages.getGeneralParlamentarian());
 		getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 		getView().setCategoryDisplay(applicationMessages.getGeneralCategory());
 	}
 
 	@Override
 	protected void revealInParent() {
 		fireEvent(new RevealContentEvent(MainPresenter.TYPE_MAIN_CONTENT, this));
 	}
 
 	@Override
 	public void initDataLoad() {
 		parlamentarianService.getAllParlamentarians(new AsyncCallback<List<Parlamentarian>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorParlamentarianList());
 			}
 
 			@Override
 			public void onSuccess(List<Parlamentarian> result) {
 				if (result != null) {
 					ListDataProvider<Parlamentarian> data = new ListDataProvider<Parlamentarian>(result);
 					setParlamentarianData(data);
 				}
 			}
 		});
 
 		categoryService.getAllCategories(new AsyncCallback<List<Category>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorCategoryList());
 			}
 
 			@Override
 			public void onSuccess(List<Category> result) {
 				if (result != null) {
 					ListDataProvider<Category> data = new ListDataProvider<Category>(result);
 					setCategoryData(data);
 				}
 			}
 		});
 		setBillData(new ListDataProvider<Bill>());
 	}
 
 	@Override
 	public void searchParlamentarian(String keyWord) {
 		getView().setParlamentarianDisplay(applicationMessages.getGeneralParlamentarian());
 		getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 		getView().setCategoryDisplay(applicationMessages.getGeneralCategory());
 		parlamentarianService.searchParlamentarian(keyWord, new AsyncCallback<List<Parlamentarian>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorParlamentarianSearch());
 			}
 
 			@Override
 			public void onSuccess(List<Parlamentarian> result) {
 				if (result != null) {
 					ListDataProvider<Parlamentarian> data = new ListDataProvider<Parlamentarian>(result);
 					setParlamentarianData(data);
 					searchCategory(result);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void searchParlamentarian(List<Category> categories) {
 		parlamentarianService.searchParlamentarian(categories, new AsyncCallback<List<Parlamentarian>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorParlamentarianCategorySearch());
 			}
 
 			@Override
 			public void onSuccess(List<Parlamentarian> result) {
 				if (result != null) {
 					ListDataProvider<Parlamentarian> data = new ListDataProvider<Parlamentarian>(result);
 					setParlamentarianData(data);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void searchCategory(String keyWord) {
 		getView().setParlamentarianDisplay(applicationMessages.getGeneralParlamentarian());
 		getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 		getView().setCategoryDisplay(applicationMessages.getGeneralCategory());
 		categoryService.searchCategory(keyWord, new AsyncCallback<List<Category>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorCategorySearch());
 			}
 
 			@Override
 			public void onSuccess(List<Category> result) {
 				if (result != null) {
 					ListDataProvider<Category> data = new ListDataProvider<Category>(result);
 					setCategoryData(data);
 					searchParlamentarian(result);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void searchCategory(List<Parlamentarian> parlamentarians) {
 		categoryService.searchCategory(parlamentarians, new AsyncCallback<List<Category>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorCategoryParlamentarianSearch());
 			}
 
 			@Override
 			public void onSuccess(List<Category> result) {
 				if (result != null) {
 					ListDataProvider<Category> data = new ListDataProvider<Category>(result);
 					setCategoryData(data);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void searchBill(Parlamentarian parlamentarian, Category category) {
 		billService.searchBills(parlamentarian, category, new AsyncCallback<List<Bill>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorBillList());
 			}
 
 			@Override
 			public void onSuccess(List<Bill> result) {
 				if (result != null) {
 					ListDataProvider<Bill> data = new ListDataProvider<Bill>(result);
 					setBillData(data);
 				}
 			}
 		});
 	}
 
 	@Override
 	public AbstractDataProvider<Parlamentarian> getParlamentarianData() {
 		return parlamentarianData;
 	}
 
 	@Override
 	public void setParlamentarianData(AbstractDataProvider<Parlamentarian> data) {
 		parlamentarianData = data;
 		parlamentarianData.addDataDisplay(getView().getParlamentarianTable());
 	}
 
 	@Override
 	public AbstractDataProvider<Category> getCategoryData() {
 		return categoryData;
 	}
 
 	@Override
 	public void setCategoryData(AbstractDataProvider<Category> data) {
 		categoryData = data;
 		categoryData.addDataDisplay(getView().getCategoryTable());
 	}
 
 	@Override
 	public AbstractDataProvider<Bill> getBillData() {
 		return billData;
 	}
 
 	@Override
 	public void setBillData(AbstractDataProvider<Bill> data) {
 		billData = data;
 		billData.addDataDisplay(getView().getBillTable());
 	}
 
 	@Override
 	public void initParlamentarianTable() {
 		while (getView().getParlamentarianTable().getColumnCount() > 0) {
 			getView().getParlamentarianTable().removeColumn(0);
 		}
 
 		// Creates image column
 		Column<Parlamentarian, String> imageColumn = new Column<Parlamentarian, String>(new ImageCell()){
 			@Override
 			public String getValue(Parlamentarian parlamentarian) {
 				return "images/parlamentarian/small/" + parlamentarian.getImage();
 			}
 		};
 
 		// Adds name column to table
 		getView().getParlamentarianTable().addColumn(imageColumn, "");
 
 		// Creates name column
 		TextColumn<Parlamentarian> nameColumn = new TextColumn<Parlamentarian>() {
 			@Override
 			public String getValue(Parlamentarian parlamentarian) {
 				return parlamentarian.getFirstName() + " " + parlamentarian.getLastName();
 			}
 		};
 
 		// Sets sortable name column
 		nameColumn.setSortable(true);
 		ListHandler<Parlamentarian> nameSortHandler = new ListHandler<Parlamentarian>(((ListDataProvider<Parlamentarian>) parlamentarianData).getList());
 		getView().getParlamentarianTable().addColumnSortHandler(nameSortHandler);
 		nameSortHandler.setComparator(nameColumn, new Comparator<Parlamentarian>() {
 			public int compare(Parlamentarian o1, Parlamentarian o2) {
 				return o1.getLastName().compareTo(o2.getLastName());
 			}
 		});
 
 		// Adds name column to table
 		getView().getParlamentarianTable().addColumn(nameColumn, applicationMessages.getGeneralParlamentarian());
 
 		// Creates party column
 		TextColumn<Parlamentarian> partyColumn = new TextColumn<Parlamentarian>() {
 			@Override
 			public String getValue(Parlamentarian parlamentarian) {
 				return parlamentarian.getParty().getName();
 			}
 		};
 
 		// Sets sortable party column
 		partyColumn.setSortable(true);
 		ListHandler<Parlamentarian> partySortHandler = new ListHandler<Parlamentarian>(((ListDataProvider<Parlamentarian>) parlamentarianData).getList());
 		getView().getParlamentarianTable().addColumnSortHandler(partySortHandler);
 		partySortHandler.setComparator(nameColumn, new Comparator<Parlamentarian>() {
 			public int compare(Parlamentarian o1, Parlamentarian o2) {
 				return o1.getParty().getName().compareTo(o2.getParty().getName());
 			}
 		});
 
 		// Adds party column to table
 		getView().getParlamentarianTable().addColumn(partyColumn, applicationMessages.getGeneralParty());
 
 		// Creates action profile column
 		Column<Parlamentarian, Parlamentarian> profileColumn = new Column<Parlamentarian, Parlamentarian>(new ActionCell<Parlamentarian>("", new ActionCell.Delegate<Parlamentarian>() {
 
 			@Override
 			public void execute(Parlamentarian parlamentarian) {
 				PlaceRequest placeRequest = new PlaceRequest(ParlamentarianPresenter.PLACE);
 				placeManager.revealPlace(placeRequest.with(ParlamentarianPresenter.PARAM_PARLAMENTARIAN_ID, parlamentarian.getId().toString()));
 			}
 		}) {
 			@Override
 			public void render(Cell.Context context, Parlamentarian value, SafeHtmlBuilder sb) {
 				sb.append(new SafeHtml() {
 
 					@Override
 					public String asString() {
 						return "<div class=\"profileButton\"></div>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Parlamentarian getValue(Parlamentarian parlamentarian) {
 				return parlamentarian;
 			}
 		};
 
 		// Adds action profile column to table
 		getView().getParlamentarianTable().addColumn(profileColumn, applicationMessages.getGeneralProfile());
 
 		// Sets selection model for each row
 		final SingleSelectionModel<Parlamentarian> selectionModel = new SingleSelectionModel<Parlamentarian>(Parlamentarian.KEY_PROVIDER);
 		getView().getParlamentarianTable().setSelectionModel(selectionModel);
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			public void onSelectionChange(SelectionChangeEvent event) {
 				selectedParlamentarian = selectionModel.getSelectedObject();
 				getView().setParlamentarianDisplay(selectedParlamentarian.getFirstName() + " " + selectedParlamentarian.getLastName());
 				if (selectedParlamentarian.getImage() == null) {
 					getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 				} else {
 					getView().setParlamentarianImage("images/parlamentarian/large/" + selectedParlamentarian.getImage());
 				}
 				setBillTable();
 			}
 		});
 	}
 
 	@Override
 	public void initCategoryTable() {
 		while (getView().getCategoryTable().getColumnCount() > 0) {
 			getView().getCategoryTable().removeColumn(0);
 		}
 
 		// Creates name column
 		TextColumn<Category> nameColumn = new TextColumn<Category>() {
 			@Override
 			public String getValue(Category category) {
 				return category.getName();
 			}
 		};
 
 		// Sets sortable name column
 		nameColumn.setSortable(true);
 		ListHandler<Category> sortHandler = new ListHandler<Category>(((ListDataProvider<Category>) categoryData).getList());
 		getView().getCategoryTable().addColumnSortHandler(sortHandler);
 		sortHandler.setComparator(nameColumn, new Comparator<Category>() {
 			public int compare(Category o1, Category o2) {
 				return o1.getName().compareTo(o2.getName());
 			}
 		});
 
 		// Adds name column to table
 		getView().getCategoryTable().addColumn(nameColumn, applicationMessages.getGeneralCategory());
 
 		// Creates action suscription column
 		Column<Category, Category> suscriptionColumn = new Column<Category, Category>(new ActionCell<Category>("", new ActionCell.Delegate<Category>() {
 
 			@Override
 			public void execute(Category category) {
 				// TODO: add category suscription servlet
 			}
 		}) {
 			@Override
 			public void render(Cell.Context context, Category value, SafeHtmlBuilder sb) {
 				sb.append(new SafeHtml() {
 
 					@Override
 					public String asString() {
 						return "<div class=\"suscribeButtonCategory\"></div>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Category getValue(Category category) {
 				return category;
 			}
 		};
 
 		// Adds action suscription column to table
 		getView().getCategoryTable().addColumn(suscriptionColumn, applicationMessages.getGeneralSusbcribe());
 
 		// Sets selection model for each row
 		final SingleSelectionModel<Category> selectionModel = new SingleSelectionModel<Category>(Category.KEY_PROVIDER);
 		getView().getCategoryTable().setSelectionModel(selectionModel);
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			public void onSelectionChange(SelectionChangeEvent event) {
 				selectedCategory = selectionModel.getSelectedObject();
 				getView().setCategoryDisplay(selectedCategory.getName());
 				setBillTable();
 			}
 		});
 	}
 
 	@Override
 	public void initBillTable() {
 		while (getView().getBillTable().getColumnCount() > 0) {
 			getView().getBillTable().removeColumn(0);
 		}
 
 		// Creates bulletin column
 		TextColumn<Bill> bulletinColumn = new TextColumn<Bill>() {
 			@Override
 			public String getValue(Bill bill) {
 				return bill.getBulletinNumber();
 			}
 		};
 		// Sets sortable bulletin column
 		bulletinColumn.setSortable(true);
 		ListHandler<Bill> bulletinSortHandler = new ListHandler<Bill>(((ListDataProvider<Bill>) billData).getList());
 		getView().getBillTable().addColumnSortHandler(bulletinSortHandler);
 		bulletinSortHandler.setComparator(bulletinColumn, new Comparator<Bill>() {
 			public int compare(Bill o1, Bill o2) {
 				return o1.getBulletinNumber().compareTo(o2.getBulletinNumber());
 			}
 		});
 
 		// Adds bulletin column to table
 		getView().getBillTable().addColumn(bulletinColumn, applicationMessages.getBillBulletin());
 
 		// Creates title column
 		TextColumn<Bill> titleColumn = new TextColumn<Bill>() {
 			@Override
 			public String getValue(Bill bill) {
 				return bill.getTitle();
 			}
 		};
 		// Sets sortable title column
 		titleColumn.setSortable(true);
 		ListHandler<Bill> titleSortHandler = new ListHandler<Bill>(((ListDataProvider<Bill>) billData).getList());
 		getView().getBillTable().addColumnSortHandler(titleSortHandler);
 		titleSortHandler.setComparator(titleColumn, new Comparator<Bill>() {
 			public int compare(Bill o1, Bill o2) {
 				return o1.getTitle().compareTo(o2.getTitle());
 			}
 		});
 
 		// Adds title column to table
 		getView().getBillTable().addColumn(titleColumn, applicationMessages.getBillConflictedBill());
 
 		// Creates isAuthor column
 		Column<Bill, String> isAuthorColumn = new Column<Bill, String>(new ImageCell()){
 			@Override
 			public String getValue(Bill bill) {
 				if (selectedParlamentarian != null) {
 					if (selectedParlamentarian.getAuthoredBills().contains(bill)) {
 						return "images/footprints.png";
 					}
 				}
 				return "images/footprints_hidden.png";
 			}
 		};
 
 		// Adds isAuthor column to table
 		getView().getBillTable().addColumn(isAuthorColumn, applicationMessages.getBillIsAuthoredBill());
 
 		// Creates isVoted column
 		Column<Bill, String> isVotedColumn = new Column<Bill, String>(new ImageCell()){
 			@Override
 			public String getValue(Bill bill) {
 				if (selectedParlamentarian != null) {
 					if (selectedParlamentarian.getVotedBills().contains(bill)) {
 						return "images/footprints.png";
 					}
 				}
 				return "images/footprints_hidden.png";
 			}
 		};
 
 		// Adds isVoted column to table
 		getView().getBillTable().addColumn(isVotedColumn, applicationMessages.getBillVotedInChamber());
 
 		// Creates action suscription column
 		Column<Bill, Bill> suscriptionColumn = new Column<Bill, Bill>(new ActionCell<Bill>("", new ActionCell.Delegate<Bill>() {
 
 			@Override
 			public void execute(Bill bill) {
 				// TODO: add bill suscription servlet
 			}
 		}) {
 			@Override
 			public void render(Cell.Context context, Bill value, SafeHtmlBuilder sb) {
 				sb.append(new SafeHtml() {
 
 					@Override
 					public String asString() {
 						return "<div class=\"suscribeButtonBill\"></div>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Bill getValue(Bill bill) {
 				return bill;
 			}
 		};
 
 		// Adds action suscription column to table
 		getView().getBillTable().addColumn(suscriptionColumn, applicationMessages.getGeneralSusbcribe());
 
 		// Sets selection model for each row
 		final SingleSelectionModel<Bill> selectionModel = new SingleSelectionModel<Bill>(Bill.KEY_PROVIDER);
 		getView().getBillTable().setSelectionModel(selectionModel);
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			public void onSelectionChange(SelectionChangeEvent event) {
				// TODO: Add go to Bill Profile Action on selected row
 			}
 		});
 	}
 
 	@Override
 	public void setBillTable() {
 		if (selectedParlamentarian != null && selectedCategory != null) {
 			searchBill(selectedParlamentarian, selectedCategory);
 		}
 	}
 
 	@Override
 	public void showParlamentarianProfile(){
 		if (selectedParlamentarian != null) {
 			PlaceRequest placeRequest = new PlaceRequest(ParlamentarianPresenter.PLACE);
 			placeManager.revealPlace(placeRequest.with(ParlamentarianPresenter.PARAM_PARLAMENTARIAN_ID, selectedParlamentarian.getId().toString()));
 		}
 	}
 }
