 package eu.comexis.napoleon.client.core.lease;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.ViewImpl;
 
 import eu.comexis.napoleon.client.utils.UiHelper;
 import eu.comexis.napoleon.shared.model.Lease;
 
 public class LeaseDetailsView extends ViewImpl implements LeaseDetailsPresenter.MyView {
 
   public interface Binder extends UiBinder<Widget, LeaseDetailsView> {
   }
 
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
   
 
   @Inject
   public LeaseDetailsView(final Binder binder) {
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
    this.coocuppant.setInnerText(l.getCooccupant());
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
   }
 
   @Override
   public void setLeaseDetailUiHandler(LeaseDetailUiHandlers handler) {
     this.presenter = handler;
 
   }
 }
