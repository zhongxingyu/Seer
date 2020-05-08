 package eu.comexis.napoleon.client.core.lease;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.ViewImpl;
 
 import eu.comexis.napoleon.client.core.lease.LeaseDetailUiHandlers;
 import eu.comexis.napoleon.client.utils.UiHelper;
 import eu.comexis.napoleon.shared.model.Lease;
 
 public class LeaseDetailsView extends ViewImpl implements LeaseDetailsPresenter.MyView {
 
   public interface Binder extends UiBinder<Widget, LeaseDetailsView> {
   }
   private static final Binder binder = GWT.create(Binder.class);
   private final Widget widget;
   private LeaseDetailUiHandlers presenter;
 
   @UiField
   Element reference;
   @UiField
   Element academicYear;
   @UiField
   Element tenantName;
   @UiField
   Element ownerName;
   @UiField
   Element startDate;
   @UiField
   Element endDate;
   @UiField
   Element eleDate;
   @UiField
   Element elsDate;
   @UiField
   Element depositDate;
   @UiField
   Element cash;
   @UiField
   Element bank;
   @UiField
   Element iban;
   @UiField
   Element bic;
   @UiField
   Element deposit;
   @UiField
   Element fee;
   @UiField
   Element feeOwner;
   @UiField
   Element rent;
   @UiField
   Element charges;
   @UiField
   Element type;
   @UiField
   Element bookkeepingRef;
   @UiField
   Element hasFurnituresRental;
   @UiField
   Element hasFurnituresWithContract;
   @UiField
   Element furnituresPayment;
   @UiField
   Element furnituresDate;
   @UiField
   Element furnituresAmount;
   @UiField
   Element coocuppant;
   @UiField
   SimplePanel documentsPanel;
   
   @Inject
   public LeaseDetailsView() {
     widget = binder.createAndBindUi(this);
   }
 
   @Override
   public Widget asWidget() {
     return widget;
   }
 
   @UiHandler("btnDelete")
   public void onDeleteClicked(ClickEvent e) {
     Window.alert("Supprimer");
   }
   
   @UiHandler("btnToList")
   public void onGoToListClicked(ClickEvent e) {
     presenter.onButtonBackToListClick();
   }
 
   @UiHandler("btnUpdate")
   public void onUpdateClicked(ClickEvent e) {
     presenter.onButtonUpdateClick();
   }
   
   @UiHandler("btnPayment")
   public void onPaymentClicked(ClickEvent e) {
     presenter.onButtonPaymentClick();
   }
   
   @UiHandler("btnPaymentOwner")
   public void onPaymentOwnerClicked(ClickEvent e) {
     presenter.onButtonPaymentOwnerClick();
   }
   
   @UiHandler("btnPaymentTenant")
   public void onPaymentTenantClicked(ClickEvent e) {
     presenter.onButtonPaymentTenantClick();
   }
   
   @Override
   public void setLease(Lease l) {
     // TODO improve and continue
 
     this.reference.setInnerText(l.getRealEstate().getReference());
     this.academicYear.setInnerText(l.getAcademicYear());
     this.coocuppant.setInnerHTML(l.getCooccupant().replace("\n","<br/>"));
     this.startDate.setInnerText(UiHelper.displayDate(l.getStartDate()));
     this.endDate.setInnerText(UiHelper.displayDate(l.getEndDate()));
     this.tenantName.setInnerText(l.getTenant().getName());
     this.ownerName.setInnerText(l.getRealEstate().getOwner());
    this.type.setInnerText(l.getType().name());
     this.fee.setInnerText(UiHelper.FloatToString(l.getFee()));
     this.feeOwner.setInnerText(UiHelper.FloatToString(l.getRent() - l.getFee()));
     this.charges.setInnerText(UiHelper.FloatToString(l.getServiceCharges()));
     this.deposit.setInnerText(UiHelper.FloatToString(l.getSecurityDeposit()));
     this.rent.setInnerText(UiHelper.FloatToString(l.getRent()));
     this.eleDate.setInnerText(UiHelper.displayDate(l.getEleDate()));
     this.elsDate.setInnerText(UiHelper.displayDate(l.getElsDate()));
     this.depositDate.setInnerText(UiHelper.displayDate(l.getDepositDate()));
     this.furnituresPayment.setInnerText((l.getFurnituresPaymentOK()!=null && l.getFurnituresPaymentOK().equals(true))? "Oui":"Non" );
     this.hasFurnituresWithContract.setInnerText((l.getHasFurnituresWithContract()!=null && l.getHasFurnituresWithContract().equals(true))?"Oui":"Non");
     this.hasFurnituresRental.setInnerText((l.getHasFurnituresRental()!=null && l.getHasFurnituresRental().equals(true))?"Oui":"Non");
     this.cash.setInnerText((l.getDepositInCash()!=null && l.getDepositInCash().equals(true))? "Oui":"Non");
     this.bank.setInnerText((l.getDepositInCash()!=null && l.getDepositInCash().equals(false))? "Oui":"Non");
     this.bookkeepingRef.setInnerText(l.getBookkeepingReference());
     this.iban.setInnerText(l.getIban());
     this.bic.setInnerText(l.getBic());
     this.furnituresAmount.setInnerText(UiHelper.FloatToString(l.getFurnituresAnnualAmount()));
     this.furnituresDate.setInnerText(UiHelper.displayDate(l.getFurnituresDate()));
   }
 
   @Override
   public void setPresenter(LeaseDetailUiHandlers handler) {
     this.presenter = handler;
   }
   @Override
   public void addDocumentWidget(Widget w) {
     documentsPanel.add(w);
     
   }
 }
