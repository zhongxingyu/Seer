 package kea.kme.pullpit.client.UI;
 
 import kea.kme.pullpit.client.UI.widgets.ShowWidget;
 import kea.kme.pullpit.client.UI.widgets.TextBoxWidget;
 import kea.kme.pullpit.client.objects.PullPitFile;
 import kea.kme.pullpit.client.objects.ShowState;
 import kea.kme.pullpit.client.services.FileService;
 import kea.kme.pullpit.client.services.FileServiceAsync;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CaptionPanel;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FileUpload;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Displaying different types of items, could be shows, bands, documents
  * etc. receiving a Show object to generate the content
  * @author Mikkel Clement
  */
 public class DisplayItem {
 
 	char type;
 	private PullPitConstants constants = GWT.create(PullPitConstants.class);
 	private PullPitMessages messages = GWT.create(PullPitMessages.class);
 
 	@SuppressWarnings("unused")
 	private Label showIdLabel, bandNameLabel, venueLabel, dateLabel,
 			promoterLabel, bandIdLabel, bandCountryLabel, bandPromoterLabel,
 			bandAgentLabel;
 
 	@SuppressWarnings("unused")
 	private TextBoxWidget stateTextBox, feeTextBox, provisionTextBox,
 			profitSplitTextBox, ticketPriceTextBox, ticketsSoldTextBox,
 			commentsTextBox, kodaPctTextBox, VATTextBox, lastEditTextBox;
 
 	@SuppressWarnings("unused")
 	private Label stateLabel, feeLabel, feeCurrencyLabel, provisionLabel,
 			productionTypeLabel, profitSplitLabel, ticketPriceLabel,
 			ticketsSoldLabel, commentsLabel, provisionCurrencyLabel,
 			kodaPctLabel, VATLabel, lastEditLabel;
 
 	private CaptionPanel showCapPanel;
 	private CaptionPanel bandCapPanel;
 	private CaptionPanel editableCapPanel;
 	private CaptionPanel venuesCapPanel;
 
 	private FlexTable showTable;
 	private FlexTable bandTable;
 	private FlexTable editableTable;
 	private FlexTable venuesTable;
 
 	private DockPanel dockPanel;
 
 	private ShowWidget currentShow;
 
 	private ListBox provisionCurrencyListBox;
 	private ListBox feeCurrencyListBox;
 	private ListBox productionTypeListBox;
 	private FormPanel myForm;
 	private CaptionPanel fileCapPanel;
 	private FlexTable uploadedFiles;
 
 	FileServiceAsync fileService = GWT.create(FileService.class);
 
 	/**
 	 * @param type
 	 * @param currentShow
 	 */
 	public DisplayItem(char type, ShowWidget currentShow) {
 
 		// setting fields
 		this.currentShow = currentShow;
 		this.type = type;
 
 		// due to lack of implementation, determing the typ of the received type
 		// is required.
 		// calling the method according to the received type
 		if (type == 'D') {
 			displayDocument();
 		} else if (type == 'S') {
 			displayShow();
 		} else {
 			displayDocument();
 
 		}
 
 	}
 
 	private void displayDocument() {
 
 	}
 
 	/**
 	 * creating a panel containing show information, consisting of other panels
 	 */
 	private void displayShow() {
 		// creating captionpanel and setting header and stylename
 		showCapPanel = new CaptionPanel();
 		showCapPanel.setCaptionHTML(constants.Show());
 		showCapPanel.setStyleName("capPanel");
 		// creating captionpanel and setting header and stylename
 		bandCapPanel = new CaptionPanel();
 		bandCapPanel.setCaptionHTML(constants.Band());
 		bandCapPanel.setStyleName("capPanel");
 		// creating captionpanel and setting header and stylename
 		editableCapPanel = new CaptionPanel();
 		editableCapPanel.setCaptionHTML("Variable");
 		editableCapPanel.setStyleName("capPanel");
 		// creating captionpanel and setting header and stylename
 		fileCapPanel = new CaptionPanel();
 		fileCapPanel.setCaptionHTML(constants.files());
 		fileCapPanel.setStyleName("capPanel");
 
 		dockPanel = new DockPanel();
 
 		// creating header and adding stylename
 		Label header = new Label(currentShow.getShow().getBand().getBandName()
 				+ " (" + currentShow.getShow().getBand().getBandCountry()
 				+ ") - " + currentShow.getShow().getVenuesString() + " D.: "
 				+ currentShow.getShow().getDate());
 		header.setStyleName("header");
 
 		// adding panels to the dockPanel, and setting orientation
 		dockPanel.add(header, DockPanel.NORTH);
 		dockPanel.add(makeFileCaptionPanel(), DockPanel.NORTH);
 		dockPanel.add(makeShowCaptionPanel(), DockPanel.WEST);
 		dockPanel.add(makeBandCaptionPanel(), DockPanel.WEST);
 		dockPanel.add(makeEditableCaptionTable(), DockPanel.WEST);
 
 		// changing the content of the contentpanel to the dockPanel with the
 		// show fields
 		UIMain.getInstance().changeContentTo(dockPanel);
 	}
 
 	/**
 	 * returning a shop CaptionPanel containing show noneditable information
 	 * 
 	 * @return captionPanel
 	 */
 	public CaptionPanel makeShowCaptionPanel() {
 		// creating a table containing info about the show
 		showTable = new FlexTable();
 		showTable.setTitle(constants.Show());
 
 		// setting the labels of the columns containing a type and value
 		showTable.setWidget(1, 0, showIdLabel = new Label(constants.ShowID()
 				+ ": " + currentShow.getShow().getShowID()));
 		showTable.setWidget(2, 0, bandNameLabel = new Label(constants.Band()
 				+ ": " + currentShow.getShow().getBand().getBandName()));
 		showTable.setWidget(3, 0, venueLabel = new Label(constants.venue()
 				+ ": " + currentShow.getShow().getVenuesString()));
 		showTable.setWidget(4, 0, dateLabel = new Label(constants.date() + ": "
 				+ currentShow.getShow().getDate()));
 		showTable.setWidget(5, 0,
 				promoterLabel = new Label(constants.promoter() + ": "
 						+ currentShow.getShow().getPromoter().getPromoName()));
 		// adding the panel to showTable
 		showCapPanel.add(showTable);
 		return showCapPanel;
 	}
 
 	/**
 	 * returning a CaptionPanel containing information about the bands
 	 * associated with the show
 	 * 
 	 * @return capPanel
 	 */
 	public CaptionPanel makeBandCaptionPanel() {
 		// creating table and setting labels with the type and value
 		bandTable = new FlexTable();
 		bandTable.setTitle("band");
 		bandTable.setWidget(1, 0, bandIdLabel = new Label(constants.bandID()
 				+ ": " + currentShow.getShow().getBand().getBandID()));
 		bandTable.setWidget(2, 0, bandNameLabel = new Label(constants.Band()
 				+ ": " + currentShow.getShow().getBand().getBandName()));
 		bandTable.setWidget(3, 0,
 				bandCountryLabel = new Label(constants.country() + ": "
 						+ currentShow.getShow().getBand().getBandCountry()));
 		bandTable.setWidget(4, 0,
 				bandPromoterLabel = new Label(constants.promoter()
 						+ ": "
 						+ currentShow.getShow().getBand().getPromoter()
 								.getPromoName()));
 		bandTable.setWidget(5, 0, bandAgentLabel = new Label(constants.agent()
 				+ ": " + currentShow.getShow().getBand().getAgentsToString()));
 		// adding bandtable to the capPanel and returning it
 		bandCapPanel.add(bandTable);
 		return bandCapPanel;
 	}
 
 	/**
 	 * returning a CaptionPanel containing the editable content
 	 * 
 	 * @return capPanel
 	 */
 	public CaptionPanel makeEditableCaptionTable() {
 		// initializing flextable
 		editableTable = new FlexTable();
 		editableTable.setTitle("variable");
 
 		// creating content of listbox and setting values
 		feeCurrencyListBox = new ListBox();
 		feeCurrencyListBox.addItem(constants.dkk());
 		feeCurrencyListBox.addItem(constants.usd());
 		feeCurrencyListBox.addItem(constants.eur());
 		feeCurrencyListBox.addItem(constants.gbp());
 		feeCurrencyListBox.setSelectedIndex(currentShow.getShow()
 				.getFeeCurrency());
 		// creating content of listbox and setting values
 		provisionCurrencyListBox = new ListBox();
 		provisionCurrencyListBox.addItem(constants.dkk());
 		provisionCurrencyListBox.addItem(constants.usd());
 		provisionCurrencyListBox.addItem(constants.eur());
 		provisionCurrencyListBox.addItem(constants.gbp());
 		provisionCurrencyListBox.setSelectedIndex(currentShow.getShow()
 				.getProvisionCurrency());
 		// creating content of listbox and setting values
 		productionTypeListBox = new ListBox();
 		productionTypeListBox.addItem(constants.sale());
 		productionTypeListBox.addItem(constants.coProduction());
 		productionTypeListBox.addItem(constants.ownProduction());
 		productionTypeListBox.setSelectedIndex(currentShow.getShow()
 				.getProductionType());
 
 		// setting widgets of each row, label in first column and textbox in the
 		// second.
 		editableTable.setWidget(1, 0, stateLabel = new Label(constants.state()
 				+ ": "));
 		stateTextBox = new TextBoxWidget(
 				ShowState.getShowStateByInt(currentShow.getShow().getState()));
 		stateTextBox.setReadOnly(true);
 		stateTextBox.setStyleName("readOnlyTextBox");
 		editableTable.setWidget(1, 1, stateTextBox);
 
 		editableTable.setWidget(2, 0, feeLabel = new Label(constants.fee()
 				+ ": "));
 		feeTextBox = new TextBoxWidget(currentShow.getShow().getFee() + "");
 		editableTable.setWidget(2, 1, feeTextBox);
 
 		editableTable.setWidget(3, 0,
 				feeCurrencyLabel = new Label(constants.feeCurrency() + ": "));
 		editableTable.setWidget(3, 1, feeCurrencyListBox);
 		editableTable.setWidget(4, 0,
 				provisionLabel = new Label(constants.provision() + ": "));
 		editableTable.setWidget(4, 1, provisionTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getProvision()));
 		editableTable.setWidget(5, 0, provisionCurrencyLabel = new Label(
 				constants.provisionCurrency() + ": "));
 		editableTable.setWidget(5, 1, provisionCurrencyListBox);
 		editableTable.setWidget(6, 0,
 				productionTypeLabel = new Label(constants.productionType()
 						+ ": "));
 		editableTable.setWidget(6, 1, productionTypeListBox);
 		editableTable.setWidget(7, 0,
 				profitSplitLabel = new Label(constants.profitSplit() + ": "));
 		editableTable.setWidget(7, 1, profitSplitTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getProfitSplit()));
 		editableTable.setWidget(8, 0,
 				ticketPriceLabel = new Label(constants.ticketPrice() + ": "));
 		editableTable.setWidget(8, 1, ticketPriceTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getTicketPrice()));
 		editableTable.setWidget(9, 0,
 				kodaPctLabel = new Label(constants.kodaPct() + ": "));
 		editableTable.setWidget(9, 1, kodaPctTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getKodaPct()));
 		editableTable.setWidget(10, 0, VATLabel = new Label(constants.VAT()
 				+ ": "));
 		editableTable.setWidget(10, 1, VATTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getVAT()));
 		editableTable.setWidget(11, 0,
 				ticketsSoldLabel = new Label(constants.ticketsSold() + ": "));
 		editableTable.setWidget(11, 1, ticketsSoldTextBox = new TextBoxWidget(
 				"" + currentShow.getShow().getTicketsSold()));
 		editableTable.setWidget(12, 0,
 				commentsLabel = new Label(constants.comments() + ": "));
 		editableTable.setWidget(12, 1, commentsTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getComments()));
 		editableTable.setWidget(13, 0,
 				lastEditLabel = new Label(constants.lastEdit()));
 		editableTable.setWidget(13, 1, lastEditTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getLastEdit()));
 
 		// adding actions for each element in the table. a show, textbox/listbox
 		// and label is send as param
 		ItemActions.addFieldEventsTextBox(currentShow, feeTextBox, feeLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, provisionTextBox,
 				provisionLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, profitSplitTextBox,
 				profitSplitLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, ticketPriceTextBox,
 				ticketPriceLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, kodaPctTextBox,
 				kodaPctLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, VATTextBox, VATLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, ticketsSoldTextBox,
 				ticketsSoldLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, commentsTextBox,
 				commentsLabel);
 		ItemActions.addFieldEventsListBox(currentShow, feeCurrencyListBox,
 				feeCurrencyLabel);
 		ItemActions.addFieldEventsListBox(currentShow,
 				provisionCurrencyListBox, provisionCurrencyLabel);
 		ItemActions.addFieldEventsListBox(currentShow, productionTypeListBox,
 				productionTypeLabel);
 		editableCapPanel.add(editableTable);
 		return editableCapPanel;
 	}
 
 	/**
 	 * not implemented yet
 	 * @return capPanel
 	 */
 	public CaptionPanel makeVenueCaptionPanel() {
 		venuesTable = new FlexTable();
 		venuesTable.setTitle(constants.band());
 
 		// for (Venue v : currentShow.getShow().getVenues()){
 		//
 		// }
 
 		venuesCapPanel.add(venuesTable);
 		return venuesCapPanel;
 	}
 
 	/**
 	 * making the CaptionPanel for file upload, and getting download link
 	 * @return capPanel
 	 */
 	public CaptionPanel makeFileCaptionPanel() {
 		
 		//creating formpanel and setting encoding and method
 		myForm = new FormPanel();
 		myForm.setEncoding(FormPanel.ENCODING_MULTIPART);
 		myForm.setMethod(FormPanel.METHOD_POST);
 
 		// Creating panel to hold all of the form widgets.
 		VerticalPanel panel = new VerticalPanel();
 		myForm.setWidget(panel);
 
 		// Creating a ListBox, giving it a name and some values to be associated
 		// with its options.
 		ListBox lb = new ListBox();
 		lb.setName("docType");
 		lb.addItem(constants.dealmemo(), "deal-memo");
 		lb.addItem(constants.contract(), "contract");
 		lb.addItem(constants.budget(), "budget");
 		lb.addItem(constants.invoice(), "invoice");
 		panel.add(lb);
 		
 		//an invisible label to send showID with the file 
 		Hidden showID = new Hidden();
 		showID.setName("showID");
 		showID.setValue(currentShow.getShow().getShowID() + "");
 		panel.add(showID);
 		//an invisible label to send userName with the file
 		Hidden userName = new Hidden();
 		userName.setName("userName");
 		userName.setValue(UIMain.getInstance().getUserName());
 		panel.add(userName);
 		//an invisible label to send bandID with the file
 		Hidden bandID = new Hidden();
 		bandID.setName("bandID");
 		bandID.setValue(currentShow.getShow().getBand().getBandID() + "");
 		panel.add(bandID);
 
 		// Creating a FileUpload widget.
 		FileUpload upload = new FileUpload();
 		upload.setName("fileUploader");
 		panel.add(upload);
 
 		// Adding a submit button.
 		Button submit = new Button("Submit");
 		panel.add(submit);
 
 		//setting clickhandler on the submit button
 		submit.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				//creating async callback
 				AsyncCallback<String> callback = new AsyncCallback<String>() {
 
 					@Override
 					public void onSuccess(String result) {
 						//submitting the form, and resetting
 						myForm.setAction(result.toString());
 						myForm.submit();
 						myForm.reset();
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert(constants.formSubmitError());
 					}
 				};
 				//getting url for blobstore via the fileservice using a callback as param
 				fileService.getBlobStoreUploadUrl(callback);
 			}
 		});
 
 		// Add an event handler to the form.
 		myForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {
 
 			@Override
 			public void onSubmitComplete(SubmitCompleteEvent event) {
 				//the action when the form has been submitted correctly
 				//calling the method showLink with the result as param
 				showLink(event.getResults().trim());
 
 			}
 		});
 		HorizontalPanel filesPanel = new HorizontalPanel();
 		filesPanel.add(myForm);
 		filesPanel.add(showUploadedFiles());
 		//returning cappanel
 		fileCapPanel.add(filesPanel);
 		return fileCapPanel;
 	}
 
 	private FlexTable showUploadedFiles() {
 		uploadedFiles = new FlexTable();
 		uploadedFiles.setTitle(constants.associatedFiles());
 		uploadedFiles.getRowFormatter().addStyleName(0, "tableHeaderSmall");
 		uploadedFiles.setStyleName("dataTableSmall");
 		
 		uploadedFiles.setText(0, 0, constants.fileName());
 		uploadedFiles.setText(0, 1, constants.fileType());
 		uploadedFiles.setText(0, 2, constants.fileDate());
 		uploadedFiles.setText(0, 3, constants.fileUser());
 		uploadedFiles.setText(0, 4, constants.link());
 		
 		AsyncCallback<PullPitFile[]> callback = new AsyncCallback<PullPitFile[]>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(constants.errorGettingConnection());
 			}
 
 			@Override
 			public void onSuccess(PullPitFile[] result) {
 				int i = 1;
 				for (PullPitFile file : result) {
 					uploadedFiles.setText(i, 0, file.getFileName());
 					uploadedFiles.setText(i, 1, file.getType());
 					uploadedFiles.setText(i, 2, messages.fileDate(file.getUploadDate()));
					uploadedFiles.setText(i, 3, file.getUserName());
 					Anchor link = new Anchor();
 					link.setHref(file.getFileUrl());
 					link.setText(constants.link());
 					//setting target of the link
 					link.setTarget("_blank");
 					uploadedFiles.setWidget(i, 4, link);
 					i++;
 				}
 			}
 		};
 		fileService.getFilesByParameter("showID", currentShow.getShow().getShowID(), callback);
 		return uploadedFiles;
 	}
 
 	/**
 	 * a method to show the link to the file the user just uploaded
 	 * @param id
 	 */
 	protected void showLink(String id) {
 		//creating async callback
 		AsyncCallback<PullPitFile> callback = new AsyncCallback<PullPitFile>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(constants.errorGettingFileLink());
 			}
 
 			@Override
 			public void onSuccess(PullPitFile result) {
 				//creating a popup containing witht the url for the file just uploaded
 				PopupPanel filePop = new PopupPanel();
 				filePop.setStyleName("authPop");
 				//creating an <a> tag and setting the text and url of the tag
 				Anchor file = new Anchor();
 				file.setHref(result.getFileUrl());
 				file.setText("Link");
 				//setting target of the link
 				file.setTarget("_blank");
 				filePop.add(file);
 				filePop.setAutoHideEnabled(true);
 				//showing the popup
 				filePop.show();
 			}
 
 		};
 		//calling the fileservice to get the file with specific link via the given callback
 		fileService.getFile(id, callback);
 	}
 }
