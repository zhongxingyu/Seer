 package org.accesointeligente.client.views;
 
 import org.accesointeligente.client.AnchorCell;
 import org.accesointeligente.client.AnchorCellParams;
 import org.accesointeligente.client.presenters.RequestResponsePresenter;
 import org.accesointeligente.client.presenters.RequestResponsePresenterIface;
 import org.accesointeligente.model.Attachment;
 import org.accesointeligente.shared.RequestStatus;
 
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.*;
 import com.google.gwt.view.client.ListDataProvider;
 
 import java.util.Date;
 
 public class RequestResponseView extends Composite implements RequestResponsePresenter.Display {
 	private static RequestResponseViewUiBinder uiBinder = GWT.create(RequestResponseViewUiBinder.class);
 	interface RequestResponseViewUiBinder extends UiBinder<Widget, RequestResponseView> {}
 
 	// Fields
 	@UiField HTMLPanel navigationPanel;
 	@UiField Anchor requestListLink;
 	@UiField Image requestStatus;
 	@UiField Label requestTitle;
 	@UiField Label requestDate;
 	@UiField Label responseDate;
 	@UiField Label institutionName;
 	@UiField Label requestInfo;
 	@UiField Label requestContext;
 	@UiField HTMLPanel responsePanel;
 	@UiField Label responseInfo;
 	@UiField HTMLPanel responseAttachmentsPanel;
 	@UiField CellTable<Attachment> attachmentsTable;
 	@UiField HTMLPanel commentsPanel;
 
 
 	private RequestResponsePresenterIface presenter;
 
 	public RequestResponseView() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	@Override
 	public void setPresenter(RequestResponsePresenterIface presenter) {
 		this.presenter = presenter;
 	}
 
 	@Override
 	public void setStatus(RequestStatus status) {
 		requestStatus.setUrl(status.getUrl());
 		requestStatus.setTitle(status.getName());
 	}
 
 	@Override
 	public void setRequestTitle(String title) {
 		requestTitle.setText(title);
 	}
 
 	@Override
 	public void setRequestDate(Date date) {
 		requestDate.setText(DateTimeFormat.getFormat("dd/MM/yyyy HH:mm").format(date));
 	}
 
 	@Override
 	public void setResponseDate(Date date) {
 		responseDate.setText(DateTimeFormat.getFormat("dd/MM/yyyy HH:mm").format(date));
 	}
 
 	@Override
 	public void setInstitutionName(String name) {
 		institutionName.setText(name);
 	}
 
 	@Override
 	public void setRequestInfo(String info) {
 		requestInfo.setText(info);
 	}
 
 	@Override
 	public void setRequestContext(String context) {
 		requestContext.setText(context);
 	}
 
 	@Override
 	public void setResponseInfo(String info) {
 		responseInfo.setText(info);
 	}
 
 	@Override
 	public void setResponseAttachments(ListDataProvider<Attachment> data) {
 		data.addDataDisplay(attachmentsTable);
 	}
 
 	@Override
 	public void initTable() {
 		initTableColumns();
 	}
 
 	@Override
 	public void initTableColumns() {
 		// Name
 		Column<Attachment, String> nameColumn = new Column<Attachment, String>(new TextCell()) {
 			@Override
 			public String getValue(Attachment attachment) {
 				return attachment.getName();
 			}
 		};
 		attachmentsTable.addColumn(nameColumn, "Nombre");
 
 		// Type
 		Column<Attachment, String> typeColumn = new Column<Attachment, String>(new TextCell()) {
 			@Override
 			public String getValue(Attachment attachment) {
 				return attachment.getType().getName() + " " + attachment.getType().getExtension();
 			}
 		};
 		attachmentsTable.addColumn(typeColumn, "Tipo");
 
 		// Download
 		Column<Attachment, AnchorCellParams> statusColumn = new Column<Attachment, AnchorCellParams>(new AnchorCell()) {
 			@Override
 			public AnchorCellParams getValue(Attachment attachment) {
 				AnchorCellParams params = new AnchorCellParams();
 				params.setTitle("Descargar "+ attachment.getName());
 				params.setUrl(attachment.getUrl());
				params.setStyleNames("");
 				params.setValue(attachment.getName() + attachment.getType().getExtension());
 				return params;
 			}
 		};
 		attachmentsTable.addColumn(statusColumn, "Descarga");
 	}
 
 	@Override
 	public void displayMessage(String message) {
 		Window.alert(message);
 	}
 
 	@UiHandler("requestListLink")
 	public void onRequestListLinkClick(ClickEvent event) {
 		String link = presenter.getListLink();
 		if (link == null) {
 			link = "home";
 		}
 
 		History.newItem(link);
 	}
 }
