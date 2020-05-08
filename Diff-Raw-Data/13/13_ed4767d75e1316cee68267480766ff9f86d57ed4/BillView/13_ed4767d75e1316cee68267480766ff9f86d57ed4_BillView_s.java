 package cl.votainteligente.inspector.client.views;
 
 import cl.votainteligente.inspector.client.presenters.BillPresenter;
 import cl.votainteligente.inspector.client.resources.BillDisplayCellTableResource;
 import cl.votainteligente.inspector.client.resources.BillSearchCellTableResource;
 import cl.votainteligente.inspector.client.uihandlers.BillUiHandlers;
import cl.votainteligente.inspector.model.Category;
import cl.votainteligente.inspector.model.Parlamentarian;
import cl.votainteligente.inspector.model.Society;
import cl.votainteligente.inspector.model.Stock;
 
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.client.ui.*;
 
 import java.util.Date;
 
 public class BillView extends ViewWithUiHandlers<BillUiHandlers> implements BillPresenter.MyView {
 	private static BillViewUiBinder uiBinder = GWT.create(BillViewUiBinder.class);
 	interface BillViewUiBinder extends UiBinder<Widget, BillView> {}
 	private final Widget widget;
 
 	@UiField Label billBulletinNumber;
 	@UiField Label billTitle;
 	@UiField Label billDescription;
 	@UiField FlowPanel billAuthors;
 	@UiField Label billEntryDate;
 	@UiField Label billInitiativeType;
 	@UiField Label billType;
 	@UiField Label billOriginChamber;
 	@UiField Label billUrgency;
 	@UiField Label billStage;
 	@UiField FlowPanel billCategories;
 	@UiField Image parlamentarianImage;
 	@UiField Label parlamentarianDisplay;
 	@UiField Label parlamentarianProfileLink;
 	@UiField HTMLPanel societyPanel;
 	@UiField HTMLPanel societyTableContainer;
 	@UiField HTMLPanel parlamentarianPanel;
 	@UiField HTMLPanel parlamentarianTableContainer;
 	@UiField Anchor billUrlToVotainteligente;
 	CellTable<Society> societyTable;
 	CellTable<Stock> stockTable;
 	CellTable<Parlamentarian> parlamentarianTable;
 
 	public BillView() {
 		widget = uiBinder.createAndBindUi(this);
 		BillSearchCellTableResource searchResource = GWT.create(BillSearchCellTableResource.class);
 		BillDisplayCellTableResource displayResource = GWT.create(BillDisplayCellTableResource.class);
 		parlamentarianTable = new CellTable<Parlamentarian>(15, searchResource);
 		parlamentarianTableContainer.add(parlamentarianTable);
 		societyTable = new CellTable<Society>(15, displayResource);
		stockTable = new CellTable<Stock>(15, displayResource);
 		societyTableContainer.add(societyTable);
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 
 	@Override
 	public void setBillBulletinNumber(String billBulletinNumber) {
 		this.billBulletinNumber.setText(billBulletinNumber);
 	}
 
 	@Override
 	public void setBillTitle(String billTitle) {
 		this.billTitle.setText(billTitle);
 	}
 
 	@Override
 	public void setBillDescription(String billDescription) {
 		this.billDescription.setText(billDescription);
 	}
 
 	@Override
 	public void addBillAuthors(Parlamentarian parlamentarian, String href, Boolean hasNext) {
 		InlineHyperlink link = new InlineHyperlink(parlamentarian.toString(), href);
 
 		InlineLabel punctuation = new InlineLabel();
 		if (hasNext) {
 			punctuation.setText(", ");
 		} else {
 			punctuation.setText(".");
 		}
 		billAuthors.add(link);
 		billAuthors.add(punctuation);
 	}
 
 	@Override
 	public void clearBillAuthors() {
 		billAuthors.clear();
 	}
 
 	@Override
 	public void setBillEntryDate(Date billEntryDate) {
 		this.billEntryDate.setText(DateTimeFormat.getFormat("dd/MM/yyyy HH:mm").format(billEntryDate));
 	}
 
 	@Override
 	public void setBillInitiativeType(String billInitiativeType) {
 		this.billInitiativeType.setText(billInitiativeType);
 	}
 
 	@Override
 	public void setBillType(String billType) {
 		this.billType.setText(billType);
 	}
 
 	@Override
 	public void setBillOriginChamber(String billOriginChamber) {
 		this.billOriginChamber.setText(billOriginChamber);
 	}
 
 	@Override
 	public void setBillUrgency(String billUrgency) {
 		this.billStage.setText(billUrgency);
 	}
 
 	@Override
 	public void setBillStage(String billStage) {
 		this.billStage.setText(billStage);
 	}
 
 	@Override
 	public void addBillCategories(Category category, String href, Boolean hasNext) {
 		InlineHyperlink link = new InlineHyperlink(category.getName(), href);
 
 		InlineLabel punctuation = new InlineLabel();
 		if (hasNext) {
 			punctuation.setText(", ");
 		} else {
 			punctuation.setText(".");
 		}
 		billCategories.add(link);
 		billCategories.add(punctuation);
 	}
 
 
 	@Override
 	public void clearBillCategories() {
 		billCategories.clear();
 	}
 
 	@Override
 	public void setParlamentarianImage(String parlamentarianImageUrl) {
 		parlamentarianImage.setUrl(parlamentarianImageUrl);
 	}
 
 	@Override
 	public void setParlamentarianDisplay(String parlamentarianName) {
 		parlamentarianDisplay.setText(parlamentarianName);
 	}
 
 	@Override
 	public CellTable<Parlamentarian> getParlamentarianTable() {
 		return parlamentarianTable;
 	}
 
 	@Override
 	public void setParlamentarianTable(CellTable<Parlamentarian> parlamentarianTable) {
 		this.parlamentarianTable = parlamentarianTable;
 	}
 
 	@Override
 	public CellTable<Society> getSocietyTable() {
 		return societyTable;
 	}
 
 	@Override
 	public void setSocietyTable(CellTable<Society> societyTable) {
 		this.societyTable = societyTable;
 	}
 
 	@Override
 	public CellTable<Stock> getStockTable() {
 		return stockTable;
 	}
 
 	@Override
 	public void setStockTable(CellTable<Stock> stockTable) {
 		this.stockTable = stockTable;
 	}
 
 	@Override
 	public void setbillUrlToVotainteligente(String hrefToVotainteligente, String messageToVotainteligente) {
 		billUrlToVotainteligente.setText(messageToVotainteligente);
 		billUrlToVotainteligente.setHref(hrefToVotainteligente);
 	}
 
 	@UiHandler("parlamentarianProfileLink")
 	public void onClickParlamentarianProfileLink(ClickEvent event) {
 		getUiHandlers().showParlamentarianProfile();
 	}
 }
