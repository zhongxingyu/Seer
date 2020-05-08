 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.client.views;
 
 import org.accesointeligente.client.*;
 import org.accesointeligente.client.CustomActionImageCell.Delegate;
 import org.accesointeligente.client.presenters.RequestListPresenter;
 import org.accesointeligente.client.uihandlers.RequestListUiHandlers;
 import org.accesointeligente.model.Request;
 import org.accesointeligente.model.Response;
 import org.accesointeligente.model.UserFavoriteRequest;
 import org.accesointeligente.shared.RequestStatus;
 
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.uibinder.client.*;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class RequestListView extends ViewWithUiHandlers<RequestListUiHandlers> implements RequestListPresenter.MyView {
 	private static RequestListViewUiBinder uiBinder = GWT.create(RequestListViewUiBinder.class);
 	interface RequestListViewUiBinder extends UiBinder<Widget, RequestListView> {}
 	private final Widget widget;
 
 	@UiField Anchor requestLink;
 	@UiField HTMLPanel searchToolTip;
 	@UiField Label listTitle;
 	@UiField Anchor searchPanelHandle;
 	@UiField FlowPanel searchPanel;
 	@UiField CellTable<Request> requestTable;
 	@UiField CustomSimplePager requestPager;
 
 	public RequestListView() {
 		widget = uiBinder.createAndBindUi(this);
 		ResourceBundle.INSTANCE.RequestListView().ensureInjected();
 		requestPager.setDisplay(requestTable);
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 
 	@Override
 	public void setInSlot(Object slot, Widget content) {
 		if (RequestListPresenter.SLOT_SEARCH_WIDGET.equals(slot)) {
 			searchPanel.clear();
 
 			if (content != null) {
 				searchPanel.add(content);
 			}
 		} else {
 			super.setInSlot(slot, content);
 		}
 	}
 
 	@Override
 	public void setListTitle(String title)  {
 		listTitle.setText(title);
 	}
 
 	@Override
 	public void setListTitleStyle(String style)  {
 		listTitle.setStyleName(listTitle.getStyleName() + " " + style);
 	}
 
 	@Override
 	public void initTableColumns() {
 		// Status
 		Column<Request, CustomImageCellParams> statusColumn = new Column<Request, CustomImageCellParams>(new CustomImageCell()) {
 			@Override
 			public CustomImageCellParams getValue(Request request) {
 				CustomImageCellParams params = new CustomImageCellParams();
 
 				if (RequestStatus.CLOSED.equals(request.getStatus()) && (request.getResponses() == null || request.getResponses().size() == 0)) {
 					params.setUrl(RequestStatus.PENDING.getUrl());
 					params.setTitle(RequestStatus.PENDING.getName());
 				} else {
 					params.setUrl(request.getStatus().getUrl());
 					params.setTitle(request.getStatus().getName());
 				}
 
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
				params.setUrl(getUiHandlers().getRequestBaseUrlPlace(request.getId()));
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
 				if (request.getConfirmationDate() == null) {
 					params.setText(DateTimeFormat.getFormat("dd/MM/yyyy").format(request.getCreationDate()));
 				} else {
 					params.setText(DateTimeFormat.getFormat("dd/MM/yyyy").format(request.getConfirmationDate()));
 				}
 				params.setStyleNames(ResourceBundle.INSTANCE.RequestListView().reqTableRequestDate());
 				return params;
 			}
 		};
 		requestTable.addColumn(requestDateColumn, "Consulta");
 
 		// Process Date
 		Column<Request, CustomTextCellParams> processDateColumn = new Column<Request, CustomTextCellParams>(new CustomTextCell()) {
 			@Override
 			public CustomTextCellParams getValue(Request request) {
 				CustomTextCellParams params = new CustomTextCellParams();
 				if (request.getProcessDate() != null) {
 					params.setText(DateTimeFormat.getFormat("dd/MM/yyyy").format(request.getProcessDate()));
 				} else {
 					params.setText("Pendiente de envío");
 				}
 				params.setStyleNames(ResourceBundle.INSTANCE.RequestListView().reqTableProcessDate());
 				return params;
 			}
 		};
 		requestTable.addColumn(processDateColumn, "Envío");
 
 		// Response Date
 		Column<Request, CustomTextCellParams> responseDateColumn = new Column<Request, CustomTextCellParams>(new CustomTextCell()) {
 			@Override
 			public CustomTextCellParams getValue(Request request) {
 				CustomTextCellParams params = new CustomTextCellParams();
 				params.setStyleNames(ResourceBundle.INSTANCE.RequestListView().reqTableResponseDate());
 				try {
 					String responseDate = "Esperando respuesta";
 
 					if (request.getResponses() != null && request.getResponses().size() > 0) {
 						List<Response> responses = new ArrayList<Response>(request.getResponses());
 						responseDate = DateTimeFormat.getFormat("dd/MM/yyyy").format(responses.get(0).getDate());
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
 				getUiHandlers().showRequest(((Request) params.getValue()).getId());
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
 				if (ClientSessionUtil.getUser() != null) {
 					getUiHandlers().requestToggleFavorite((Request) params.getValue());
 				} else {
 					getUiHandlers().gotoLogin();
 				}
 			}
 		})) {
 			@Override
 			public CustomActionImageCellParams getValue(Request request) {
 				CustomActionImageCellParams params = new CustomActionImageCellParams();
 				params.setUrl("images/reqList/no-favorite.png");
 				params.setTitle("Seguir");
 
 				if (ClientSessionUtil.getUser() != null) {
 					for (UserFavoriteRequest favorite : request.getFavorites()) {
 						if (ClientSessionUtil.getUser().equals(favorite.getUser())) {
 							params.setUrl("images/reqList/favorite.png");
 							params.setTitle("Dejar de seguir");
 							break;
 						}
 					}
 				}
 
 				params.setValue(request);
 				return params;
 			}
 		};
 		requestTable.addColumn(favButtonColumn, "Seguir");
 	}
 
 	@Override
 	public void initTableReceiptColumn() {
 		// Favorite Request
 		Column<Request, CustomActionImageCellParams> receiptButtonColumn = new Column<Request, CustomActionImageCellParams>(
 				new CustomActionImageCell<CustomActionImageCellParams>(new Delegate<CustomActionImageCellParams>() {
 
 			public void execute(CustomActionImageCellParams params) {
 				Request request = (Request) params.getValue();
 				Window.Location.replace("receipt?requestId=" + request.getId());
 			}
 		})) {
 			@Override
 			public CustomActionImageCellParams getValue(Request request) {
 				CustomActionImageCellParams params = new CustomActionImageCellParams();
 				params.setUrl("images/mandato.png");
 				params.setTitle("Descarga el mandato");
 				params.setValue(request);
 				return params;
 			}
 		};
 		requestTable.addColumn(receiptButtonColumn, "Mandato");
 	}
 
 	@Override
 	public void removeColumns() {
 		while (requestTable.getColumnCount() > 0) {
 			requestTable.removeColumn(0);
 		}
 	}
 
 	@Override
 	public void setSearchHandleVisible(Boolean visible) {
 		searchPanel.setVisible(visible);
 	}
 
 	@Override
 	public void setSearchButtonVisible(Boolean visible) {
 		searchPanelHandle.setVisible(visible);
 	}
 
 	@Override
 	public void setSearchToolTipVisible(Boolean visible) {
 		searchToolTip.setVisible(visible);
 	}
 
 	@UiFactory
 	CustomSimplePager getPager() {
 		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
 		requestPager = new CustomSimplePager(pagerResources);
 		return requestPager;
 	}
 
 	@UiHandler("requestLink")
 	public void onRequestListLinkClick(ClickEvent event) {
 		getUiHandlers().gotoRequest();
 	}
 
 	@UiHandler("searchPanelHandle")
 	public void onSearchPanelHandleClick(ClickEvent event) {
 		setSearchHandleVisible(!searchPanel.isVisible());
 	}
 
 	@Override
 	public CellTable<Request> getRequestTable() {
 		return requestTable;
 	}
 }
