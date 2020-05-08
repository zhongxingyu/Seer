 package eu.comexis.napoleon.client.core.lease;
 
 import java.util.List;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.datepicker.client.DateBox;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.ViewImpl;
 
 import eu.comexis.napoleon.client.utils.UiHelper;
 import eu.comexis.napoleon.shared.model.FeeUnit;
 import eu.comexis.napoleon.shared.model.Lease;
 import eu.comexis.napoleon.shared.model.TypeOfRent;
 import eu.comexis.napoleon.shared.model.simple.SimpleRealEstate;
 import eu.comexis.napoleon.shared.model.simple.SimpleTenant;
 import eu.comexis.napoleon.shared.validation.ValidationMessage;
 
 public class LeaseUpdateView extends ViewImpl implements LeaseUpdatePresenter.MyView {
 
   public interface Binder extends UiBinder<Widget, LeaseUpdateView> {
   }
 
   private final Widget widget;
   private LeaseUpdateUiHandlers presenter;
 
   @UiField
   ListBox reference;
   @UiField
   SuggestBox academicYear;
   @UiField
   ListBox tenantName;
   @UiField
   DateBox startDate;
   @UiField
   DateBox endDate;
   @UiField
   DateBox eleDate;
   @UiField
   DateBox elsDate;
   @UiField
   DateBox depositDate;
   @UiField
   RadioButton depositInCashYes;
   @UiField
   RadioButton depositInCashNo;
   @UiField
   TextBox iban;
   @UiField
   TextBox bic;
   @UiField
   TextBox deposit;
   @UiField
   TextBox fee;
   @UiField
   TextBox feeOwner;
   @UiField
   TextBox rent;
   @UiField
   TextBox charges;
   @UiField(provided = true)
   ListBox type;
   @UiField
   TextBox bookkeepingRef;
   @UiField
   RadioButton hasFurnituresRentalYes;
   @UiField
   RadioButton hasFurnituresRentalNo;
   @UiField
   RadioButton hasFurnituresWithContractYes;
   @UiField
   RadioButton hasFurnituresWithContractNo;
   @UiField
   RadioButton furnituresPaymentOKYes;
   @UiField
   RadioButton furnituresPaymentOKNo;
   @UiField
   DateBox furnituresDate;
   @UiField
   TextBox furnituresAmount;
   @UiField
   TextArea coocuppant;
   
 
   @Inject
   public LeaseUpdateView(final Binder binder) {
     init();
     widget = binder.createAndBindUi(this);
     initNames();
   }
   
   protected void initNames() {
     academicYear.getTextBox().setName("academicYear");
   }
 
   @Override
   public Widget asWidget() {
     return widget;
   }
 
   @Override
   public void displayError(String error) {
     Window.alert(error);
   }
 
   @Override
   public void displayValidationMessage(List<ValidationMessage> validationMessages) {
     UiHelper.displayValidationMessage(validationMessages, asWidget());
   }
   
   @Override
   public void fillAcademicYearList(List<String> years) {
     MultiWordSuggestOracle oracle = (MultiWordSuggestOracle) academicYear.getSuggestOracle();
     oracle.clear();
     if (years != null) {
       for (String sVal : years) {
         if (sVal != null) {
           oracle.add(sVal);
         }
       }
     }
   }
 
   public SimpleTenant getTenant() {
     SimpleTenant t = null;
     String tenantId = tenantName.getValue(tenantName.getSelectedIndex());
     if (!tenantId.equals("-")){
       t = new SimpleTenant();
       t.setId(tenantId);
     }
     return t;
   }
   
   public SimpleRealEstate getEstate() {
     SimpleRealEstate e = null;
     String estateId = reference.getValue(reference.getSelectedIndex());
     if (!estateId.equals("-")){
       e = new SimpleRealEstate();
       e.setId(estateId);
     }
     return e;
   }
   @UiHandler("rent")
   public void onChangeRent(ChangeEvent e) {
     presenter.onRentChanged(UiHelper.stringToFloat(rent.getValue()));
     rent.setValue(UiHelper.FloatToString(UiHelper.stringToFloat(rent.getValue())));
     feeOwner.setValue(UiHelper.FloatToString(UiHelper.stringToFloat(rent.getValue()) - UiHelper.stringToFloat(fee.getValue())));
   }
   @UiHandler("fee")
   public void onChangeFee(ChangeEvent e) {
     feeOwner.setValue(UiHelper.FloatToString(UiHelper.stringToFloat(rent.getValue()) - UiHelper.stringToFloat(fee.getValue())));
   }
   @UiHandler("deposit")
   public void onChangeDeposit(ChangeEvent e) {
     this.deposit.setValue(UiHelper.FloatToString(UiHelper.stringToFloat(deposit.getValue())));
   }
   @UiHandler("charges")
   public void onChangeCharges(ChangeEvent e) {
     this.charges.setValue(UiHelper.FloatToString(UiHelper.stringToFloat(charges.getValue())));
   }
   @UiHandler("furnituresAmount")
   public void onChangeFurnituresAmount(ChangeEvent e) {
     this.furnituresAmount.setValue(UiHelper.FloatToString(UiHelper.stringToFloat(furnituresAmount.getValue())));
   }
   @UiHandler("depositInCashYes")
   public void onChangeDepositInCashYes(ClickEvent e) {
     if (depositInCashYes.getValue().equals(true)){
       disableDepositBank();
     }else{
       enableDepositBank();
     }
   }
   @UiHandler("depositInCashNo")
   public void onChangeDepositInCashNo(ClickEvent e) {
     if (depositInCashNo.getValue().equals(true)){
       enableDepositBank();
     }else{
       disableDepositBank();
     }
   }
   @UiHandler("hasFurnituresRentalYes")
   public void onChangehasFurnituresRentalYes(ClickEvent e) {
     if (hasFurnituresRentalYes.getValue().equals(true)){
       enableFurniture();
     }else{
       disableFurniture();
     }
   }
   @UiHandler("hasFurnituresRentalNo")
   public void onChangehasFurnituresRentalNo(ClickEvent e) {
     if (hasFurnituresRentalNo.getValue().equals(true)){
       disableFurniture();
     }else{
       enableFurniture();
     }
   }
   @UiHandler("hasFurnituresWithContractYes")
   public void onChangeFurnituresWithContractYes(ClickEvent e) {
     if (hasFurnituresWithContractYes.getValue().equals(true)){
       enableFurnitureContract();
     }else{
       disableFurnitureContract();
     }
   }
   
   @UiHandler("hasFurnituresWithContractNo")
   public void onChangeFurnituresWithContractNo(ClickEvent e) {
     if (hasFurnituresWithContractNo.getValue().equals(true)){
       disableFurnitureContract();
     }else{
       enableFurnitureContract();
     }
   }
   private void enableFurniture(){
     hasFurnituresWithContractYes.setEnabled(true);
     hasFurnituresWithContractNo.setEnabled(true);
     disableFurnitureContract();
   }
   private void enableDepositBank(){
     this.iban.setEnabled(true);
     this.bic.setEnabled(true);
   }
   private void disableDepositBank(){
     this.iban.setValue("");
     this.bic.setValue("");
     this.iban.setEnabled(false);
     this.bic.setEnabled(false);
   }
   private void disableFurniture(){
     hasFurnituresWithContractYes.setEnabled(false);
     hasFurnituresWithContractNo.setEnabled(false);
     disableFurnitureContract();
   }
   private void disableFurnitureContract(){
     furnituresAmount.setValue("");
     furnituresAmount.setEnabled(false);
     furnituresDate.setValue(null);
     furnituresDate.setEnabled(false);
     furnituresPaymentOKNo.setEnabled(false);
     furnituresPaymentOKYes.setEnabled(false);
   }
   private void enableFurnitureContract(){
     furnituresAmount.setEnabled(true);
     furnituresDate.setEnabled(true);
     furnituresPaymentOKNo.setEnabled(true);
     furnituresPaymentOKYes.setEnabled(true);
   }
   @UiHandler("btnCancel")
   public void onCancel(ClickEvent e) {
     presenter.onButtonCancelClick();
   }
 
   @UiHandler("btnSave")
   public void onSave(ClickEvent e) {
     presenter.onButtonSaveClick();
   }
   @Override
   public void setFee(Float fee) {
       this.fee.setValue(UiHelper.FloatToString(fee));
   }
   @Override
   public void setLease(Lease l) {
     // cleanup
     // set date format
     coocuppant.setSize("400px", "80px");
     DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd/MM/yyyy");
     this.startDate.setFormat(new DateBox.DefaultFormat(dateFormat));
     this.endDate.setFormat(new DateBox.DefaultFormat(dateFormat));
     this.eleDate.setFormat(new DateBox.DefaultFormat(dateFormat));
     this.elsDate.setFormat(new DateBox.DefaultFormat(dateFormat));
     this.depositDate.setFormat(new DateBox.DefaultFormat(dateFormat));
     this.furnituresDate.setFormat(new DateBox.DefaultFormat(dateFormat));
     this.coocuppant.setValue("");
     this.reference.setEnabled(true);
     this.academicYear.setText("");
     this.startDate.setValue(null);
     this.endDate.setValue(null);
     this.fee.setText("");
     this.feeOwner.setText("");
     this.charges.setText("");
     this.deposit.setText("");
     this.rent.setText("");
     this.eleDate.setValue(null);
     this.elsDate.setValue(null);
     this.depositDate.setValue(null);
     UiHelper.selectTextItemBoxByValue(this.reference, "-");
     UiHelper.selectTextItemBoxByValue(this.tenantName, "-");
     this.furnituresPaymentOKYes.setValue(false);
     this.furnituresPaymentOKNo.setValue(true);
     this.hasFurnituresWithContractYes.setValue(false);
     this.hasFurnituresWithContractNo.setValue(true);
     this.hasFurnituresRentalYes.setValue(false);
     this.hasFurnituresRentalNo.setValue(true);
     this.depositInCashYes.setValue(true);
     this.depositInCashNo.setValue(false);
     disableDepositBank();
     disableFurniture();
     
     if (l != null) {
       UiHelper.selectTextItemBoxByValue(this.reference, ((l.getRealEstate()!=null && l.getRealEstate().getId() != null) ? l.getRealEstate().getId() : "-"));
       UiHelper.selectTextItemBoxByValue(this.tenantName, ((l.getTenant()!=null && l.getTenant().getId() != null) ? l.getTenant().getId() : "-"));
       this.coocuppant.setValue(l.getCooccupant());
       this.academicYear.setText(l.getAcademicYear());
       this.startDate.setValue(l.getStartDate());
       this.endDate.setValue(l.getEndDate());
       if (!reference.getValue(reference.getSelectedIndex()).equals("-")){
         this.reference.setEnabled(false);
       }
       this.charges.setValue(UiHelper.FloatToString(l.getServiceCharges()));
       this.deposit.setValue(UiHelper.FloatToString(l.getSecurityDeposit()));
       this.depositDate.setValue(l.getDepositDate());
       this.depositInCashYes.setValue(l.getDepositInCash());
       this.iban.setValue(l.getIban());
       this.bic.setValue(l.getBic());
       this.eleDate.setValue(l.getEleDate());
       this.elsDate.setValue(l.getElsDate());
       this.rent.setValue(UiHelper.FloatToString(l.getRent()));
       this.fee.setValue(UiHelper.FloatToString(l.getFee()));
      if (l.getRent()!=null && l.getFee()!=null){
        this.feeOwner.setValue(UiHelper.FloatToString(l.getRent() - l.getFee()));
      }
       this.bookkeepingRef.setValue(l.getBookkeepingReference());
       UiHelper.selectTextItemBoxByValue(this.type, (l.getType() != null ? l.getType().name() : "-"));
       this.hasFurnituresRentalYes.setValue(l.getHasFurnituresRental()!=null ? l.getHasFurnituresRental(): false);
       if (this.hasFurnituresRentalYes.getValue()){
         enableFurniture();
       }
       this.hasFurnituresWithContractYes.setValue(l.getHasFurnituresWithContract()!=null ? l.getHasFurnituresWithContract(): false);
       if (this.hasFurnituresWithContractYes.getValue()){
         enableFurnitureContract();
       }
       this.furnituresPaymentOKYes.setValue(l.getFurnituresPaymentOK()!=null ? l.getFurnituresPaymentOK(): false);
       this.furnituresDate.setValue(l.getFurnituresDate());
       this.furnituresAmount.setValue(UiHelper.FloatToString(l.getFurnituresAnnualAmount()));
     }
   }
 
   @Override
   public void setLeaseUpdateUiHandler(LeaseUpdateUiHandlers handler) {
     this.presenter = handler;
   }
 
   @Override
   public Lease updateLease(Lease l) {
     l.setAcademicYear(academicYear.getValue());
     l.setTenant(getTenant());
     l.setRealEstate(getEstate());
     l.setStartDate(startDate.getValue());
     l.setEndDate(endDate.getValue());
     l.setServiceCharges(UiHelper.stringToFloat(charges.getValue()));
     l.setSecurityDeposit(UiHelper.stringToFloat(deposit.getValue()));
     l.setDepositInCash(depositInCashYes.getValue());
     l.setDepositDate(depositDate.getValue());
     l.setIban(iban.getValue());
     l.setBic(bic.getValue());
     l.setEleDate(eleDate.getValue());
     l.setElsDate(elsDate.getValue());
     l.setRent(UiHelper.stringToFloat(rent.getValue()));
     //l.setUnit(FeeUnit.LUMP_SUM);
     l.setFee(UiHelper.stringToFloat(fee.getValue()));
     l.setBookkeepingReference(bookkeepingRef.getValue());
     l.setType(TypeOfRent.valueOf(type.getValue(type.getSelectedIndex())));
     l.setHasFurnituresRental(hasFurnituresRentalYes.getValue());
     l.setHasFurnituresWithContract(hasFurnituresWithContractYes.getValue());
     l.setFurnituresPaymentOK(furnituresPaymentOKYes.getValue());
     l.setFurnituresDate(furnituresDate.getValue());
     l.setFurnituresAnnualAmount(UiHelper.stringToFloat(furnituresAmount.getValue()));
     l.setCooccupant(coocuppant.getValue());
     return l;
   }
 
   private void init() {
     type = UiHelper.createListBoxForEnum(TypeOfRent.class, "TypeOfRent_", false);
   }
 
   @Override
   public void reset() {
     UiHelper.resetForm(asWidget());
   }
 
   @Override
   public void fillEstateList(List<SimpleRealEstate> estates) {
     reference.clear();
     for (SimpleRealEstate e : estates) {
       reference.addItem(e.getReference(), e.getId());
     }
     reference.addItem("-", "-");
     
   }
 
   @Override
   public void fillTenantList(List<SimpleTenant> tenants) {
     tenantName.clear();
     for (SimpleTenant t : tenants) {
       tenantName.addItem(t.getName(), t.getId());
     }
     tenantName.addItem("-", "-");
     
   }
 }
