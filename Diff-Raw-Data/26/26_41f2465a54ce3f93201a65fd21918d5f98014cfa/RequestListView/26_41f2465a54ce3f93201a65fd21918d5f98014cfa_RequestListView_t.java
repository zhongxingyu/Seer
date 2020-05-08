 package org.accesointeligente.client.views;
 
 import org.accesointeligente.client.*;
 import org.accesointeligente.client.CustomActionImageCell.Delegate;
 import org.accesointeligente.client.presenters.RequestListPresenter;
 import org.accesointeligente.client.presenters.RequestListPresenterIface;
 import org.accesointeligente.model.Request;
 import org.accesointeligente.model.UserFavoriteRequest;
 import org.accesointeligente.shared.AppPlace;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.uibinder.client.*;
 import com.google.gwt.user.cellview.client.*;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.*;
 import com.google.gwt.view.client.ListDataProvider;
 
 public class RequestListView extends Composite implements RequestListPresenter.Display {
 	private static RequestListViewUiBinder uiBinder = GWT.create(RequestListViewUiBinder.class);
 
 	interface RequestListViewUiBinder extends UiBinder<Widget, RequestListView> {
 	}
 
 	@UiField Anchor requestLink;
 	@UiField Label listTitle;
 	@UiField Anchor searchPanelHandle;
 	@UiField FlowPanel searchPanel;
 	@UiField CellTable<Request> requestTable;
 	@UiField SimplePager requestPager;
 
 	private RequestListPresenterIface presenter;
 
 	public RequestListView() {
 		initWidget(uiBinder.createAndBindUi(this));
 		ResourceBundle.INSTANCE.RequestListView().ensureInjected();
 	}
 
 	@Override
 	public Widget asWidget() {
 		return this;
 	}
 
 	@Override
 	public void setPresenter(RequestListPresenterIface presenter) {
 		this.presenter = presenter;
 	}
 
 	@Override
 	public void displayMessage(String message) {
 		Window.alert(message);
 	}
 
 	@Override
 	public void setListTitle(String title)  {
 		listTitle.setText(title);
 	}
 
 	@Override
 	public void setSearchWidget(Widget widget) {
 		searchPanel.clear();
 		searchPanel.add(widget);
 	}
 
 	@Override
 	public void initTable() {
 		initTableColumns();
 		requestPager.setDisplay(requestTable);
 	}
 
 	@Override
 	public void initTableColumns() {
 		// Status
 		Column<Request, CustomImageCellParams> statusColumn = new Column<Request, CustomImageCellParams>(new CustomImageCell()) {
 			@Override
 			public CustomImageCellParams getValue(Request request) {
 				CustomImageCellParams params = new CustomImageCellParams();
 				params.setUrl(request.getStatus().getUrl());
 				params.setTitle(request.getStatus().getName());
 				params.setStyleNames(ResourceBundle.INSTANCE.RequestListView().reqTableStatus());
 				return params;
 			}
 		};
 		requestTable.addColumn(statusColumn, "Estado");
 
 		// Title
 		Column<Request, AnchorCellParams> titleColumn = new Column<Request, AnchorCellParams>(new AnchorCell()) {
 			@Override
 			public AnchorCellParams getValue(Request request) {
 				AnchorCellParams params = new AnchorCellParams();
 				params.setValue(request.getTitle());
 				params.setUrl("#response?requestId=" + request.getId());
 				params.setStyleNames(ResourceBundle.INSTANCE.RequestListView().reqTableTitle());
 				return params;
 			}
 		};
 		requestTable.addColumn(titleColumn, "Titulo");
 
 		// Institution
 		Column<Request, CustomTextCellParams> institutionColumn = new Column<Request, CustomTextCellParams>(new CustomTextCell()) {
 			@Override
 			public CustomTextCellParams getValue(Request request) {
 				CustomTextCellParams params = new CustomTextCellParams();
 				params.setText(request.getInstitution().getName());
 				params.setStyleNames(ResourceBundle.INSTANCE.RequestListView().reqTableInstitution());
 				return params;
 			}
 		};
 		requestTable.addColumn(institutionColumn, "Organismo");
 
 		// Request Date
 		Column<Request, CustomTextCellParams> requestDateColumn = new Column<Request, CustomTextCellParams>(new CustomTextCell()) {
 			@Override
 			public CustomTextCellParams getValue(Request request) {
 				CustomTextCellParams params = new CustomTextCellParams();
 				params.setText(DateTimeFormat.getFormat("dd/MM/yyyy").format(request.getDate()));
 				params.setStyleNames(ResourceBundle.INSTANCE.RequestListView().reqTableRequestDate());
 				return params;
 			}
 		};
 		requestTable.addColumn(requestDateColumn, "Consulta");
 
 		// Response Date
 		Column<Request, CustomTextCellParams> responseDateColumn = new Column<Request, CustomTextCellParams>(new CustomTextCell()) {
 			@Override
 			public CustomTextCellParams getValue(Request request) {
 				CustomTextCellParams params = new CustomTextCellParams();
 				params.setStyleNames(ResourceBundle.INSTANCE.RequestListView().reqTableResponseDate());
 				try {
 					String responseDate = "Esperando respuesta";
 
 					if (request.getResponse() != null) {
 						responseDate = DateTimeFormat.getFormat("dd/MM/yyyy").format(request.getResponse().getDate());
 					}
 					params.setText(responseDate);
 					return params;
 				} catch (Exception e) {
 					Window.alert(e.getMessage());
 					params.setText("");
 					return params;
 				}
 			}
 		};
 		requestTable.addColumn(responseDateColumn, "Respuesta");
 
 		// View Request
 		Column<Request, CustomActionImageCellParams> viewButtonColumn = new Column<Request, CustomActionImageCellParams>(
 				new CustomActionImageCell<CustomActionImageCellParams>(new Delegate<CustomActionImageCellParams>() {
 
 			public void execute(CustomActionImageCellParams params) {
 				presenter.showRequest(((Request) params.getValue()).getId());
 			}
 		})) {
 			@Override
 			public CustomActionImageCellParams getValue(Request request) {
 				CustomActionImageCellParams params = new CustomActionImageCellParams();
 				params.setUrl("images/reqList/viewMore.png");
 				params.setTitle("Ver +");
 				params.setValue(request);
 				return params;
 			}
 		};
 		requestTable.addColumn(viewButtonColumn, "Ver +");
 	}
 
 	@Override
 	public void initTableFavColumn() {
 		// Favorite Request
 		Column<Request, CustomActionImageCellParams> favButtonColumn = new Column<Request, CustomActionImageCellParams>(
 				new CustomActionImageCell<CustomActionImageCellParams>(new Delegate<CustomActionImageCellParams>() {
 
 			public void execute(CustomActionImageCellParams params) {
 				presenter.requestToggleFavorite((Request) params.getValue());
 			}
 		})) {
 			@Override
 			public CustomActionImageCellParams getValue(Request request) {
 				CustomActionImageCellParams params = new CustomActionImageCellParams();
 				params.setUrl("images/reqList/no-favorite.png");
 				params.setTitle("Seguir");
 
 				for (UserFavoriteRequest favorite : request.getFavorites()) {
 					if (ClientSessionUtil.getUser().equals(favorite.getUser())) {
 						params.setUrl("images/reqList/favorite.png");
 						params.setTitle("Dejar de seguir");
 						break;
 					}
 				}
 
 				params.setValue(request);
 				return params;
 			}
 		};
 		requestTable.addColumn(favButtonColumn, "Seguir");
 	}
 
 	@Override
	public void removeTableFavColumn() {
		requestTable.removeColumn(6);
	}

	@Override
 	public void setRequests(ListDataProvider<Request> data) {
 		data.addDataDisplay(requestTable);
 	}
 
 	@UiFactory
 	SimplePager getPager() {
 		requestPager = new SimplePager(TextLocation.CENTER);
 		return requestPager;
 	}
 
 	@UiHandler("requestLink")
 	public void onRequestListLinkClick(ClickEvent event) {
 		History.newItem(AppPlace.REQUEST.getToken());
 	}
 
 	@UiHandler("searchPanelHandle")
 	public void onSearchPanelHandleClick(ClickEvent event) {
 		if (searchPanel.isVisible()) {
 			searchPanel.setVisible(false);
 		} else {
 			searchPanel.setVisible(true);
 		}
 	}
 }
