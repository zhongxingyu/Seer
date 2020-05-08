 package eu.comexis.napoleon.client.core.owner;
 
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.logging.client.LogConfiguration;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
 import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.datepicker.client.DateBox;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.ViewImpl;
 
 import eu.comexis.napoleon.client.utils.UiHelper;
 import eu.comexis.napoleon.shared.model.Country;
 import eu.comexis.napoleon.shared.model.FeeUnit;
 import eu.comexis.napoleon.shared.model.MaritalStatus;
 import eu.comexis.napoleon.shared.model.MatrimonialRegime;
 import eu.comexis.napoleon.shared.model.Owner;
 import eu.comexis.napoleon.shared.model.Title;
 
 public class OwnerUpdateView extends ViewImpl implements OwnerUpdatePresenter.MyView {
   public interface Binder extends UiBinder<Widget, OwnerUpdateView> {
   }
 
   private static final Logger LOG = Logger.getLogger(OwnerUpdateView.class.getName());
 
   private final Widget widget;
 
   private OwnerUpdateUiHandlers presenter;
 
   @UiField
   TextBox name;
   @UiField
   TextBox firstName;
   @UiField
   TextBox email;
   @UiField
   TextBox phoneNumber;
   @UiField
   TextBox mobileNumber;
   @UiField
   TextBox addresse;
   @UiField
   SuggestBox city;
   @UiField
   SuggestBox country;
   @UiField(provided = true)
   ListBox maritalStatus;
   @UiField(provided = true)
   ListBox matrimonialRegime;
   @UiField
   DateBox birthDayDateBox;
   @UiField(provided = true)
   ListBox title;
   @UiField
   TextBox fee;
   @UiField(provided = true)
   ListBox unit;
   @UiField
   TextBox iban;
   @UiField
   TextBox bic;
   @UiField
   TextBox fax;
   @UiField
   TextBox placeOfBirth;
   @UiField(provided = true)
   SuggestBox nationality;
   @UiField(provided = true)
   SuggestBox job;
   @UiField
   TextBox nationalRegister;
   @UiField
   SuggestBox postalCode;
 
   @Inject
   public OwnerUpdateView(final Binder binder) {
     init();
     widget = binder.createAndBindUi(this);
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
   public void fillCityList(List<String> cities) {
     MultiWordSuggestOracle oracle = (MultiWordSuggestOracle) city.getSuggestOracle();
     oracle.clear();
     if (cities!=null){
       for (String sCity : cities) {
         oracle.add(sCity);
       }
     }
   }
   
   @Override
   public void fillPostalCodeList(List<String> postCdes) {
     MultiWordSuggestOracle oracle = (MultiWordSuggestOracle) postalCode.getSuggestOracle();
     oracle.clear();
     if (postCdes!=null){
       for (String sPC : postCdes) {
         if (sPC!=null){
           oracle.add(sPC);
         }
       }
     }
   }
 
   @Override
   public void fillCountryList(List<Country> countries) {
     MultiWordSuggestOracle oracle = (MultiWordSuggestOracle) country.getSuggestOracle();
     oracle.clear();
     if (countries!=null){
       for (Country cnty : countries) {
         oracle.add(cnty.getName());
       }
     }
   }
 
   @Override
   public String getSelectedCountry() {
     return country.getValue();
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
   public void setOwner(Owner o) {
 
     if (LogConfiguration.loggingIsEnabled()) {
       LOG.info("set owner " + o.getId());
     }
 
     UiHelper.selectTextItemBoxByValue(title, o.getTitle());
 
     name.setText(o.getLastName());
     firstName.setText(o.getFirstName());
     
     BigDecimal _fee = o.getFee();
     fee.setText((_fee != null ? _fee.toString() : ""));
     UiHelper.selectTextItemBoxByValue(unit, o.getUnit());
 
     bic.setText(o.getBic());
     iban.setText(o.getIban());
     email.setText(o.getEmail());
     phoneNumber.setText(o.getPhoneNumber());
     mobileNumber.setText(o.getMobilePhoneNumber());
     fax.setText(o.getFax());
     birthDayDateBox.setValue(o.getDateOfBirth());
     
     DateTimeFormat dateFormat = DateTimeFormat.getShortDateFormat();
     birthDayDateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
     
     placeOfBirth.setText(o.getPlaceOfBirth());
     country.setValue(o.getCountry());
     presenter.onCountrySelect(country.getValue());
     addresse.setText(o.getStreet());
     postalCode.setText(o.getPostalCode());
     presenter.onPostalCodeSelect(postalCode.getValue());
     city.setValue(o.getCity());
     nationality.setText(o.getNationality());
     job.setText(o.getJobTitle());
     nationalRegister.setText(o.getNationalRegisterNumber());
     
     UiHelper.selectTextItemBoxByValue(maritalStatus, o.getMaritalStatus());
     UiHelper.selectTextItemBoxByValue(matrimonialRegime, o.getMatrimonialRegime());
  
   }
 
   @Override
   public void setOwnerUpdateUiHandler(OwnerUpdateUiHandlers handler) {
     this.presenter = handler;
   }
 
 
   @Override
   public Owner updateOwner(Owner o) {
     o.setTitle(Title.valueOf(title.getValue(title.getSelectedIndex())));
     o.setFirstName(firstName.getValue());
     o.setLastName(name.getValue());
     o.setFee(fee.getValue());
     o.setUnit(FeeUnit.valueOf(unit.getValue(unit.getSelectedIndex())));
     o.setBic(bic.getValue());
     o.setIban(iban.getValue());
     o.setEmail(email.getValue());
     o.setPhoneNumber(phoneNumber.getValue());
     o.setMobilePhoneNumber(mobileNumber.getValue());
     o.setFax(fax.getValue());
     o.setDateOfBirth(birthDayDateBox.getValue());
     o.setPlaceOfBirth(placeOfBirth.getValue());
     o.setStreet(addresse.getValue());
     o.setPostalCode(postalCode.getValue());
     o.setCity(city.getValue());
     o.setCountry(country.getValue());
     o.setJobTitle(job.getValue());
     o.setNationality(nationality.getValue());
     o.setNationalRegisterNumber(nationalRegister.getValue());
     o.setMaritalStatus(MaritalStatus.fromStringToEnum(maritalStatus.getValue(maritalStatus
         .getSelectedIndex())));
     o.setMatrimonialRegime(MatrimonialRegime.fromStringToEnum(matrimonialRegime
         .getValue(matrimonialRegime.getSelectedIndex())));
     return o;
   }
 
   private void init() {
 
     title = UiHelper.createListBoxForEnum(Title.class, "Title_", false);
     maritalStatus = UiHelper.createListBoxForEnum(MaritalStatus.class, "MaritalStatus_", false);
     matrimonialRegime =
         UiHelper.createListBoxForEnum(MatrimonialRegime.class, "MatrimonialRegime_", false);
     MultiWordSuggestOracle oracleNationality = new MultiWordSuggestOracle();
     oracleNationality.add("Belge");
     oracleNationality.add("Français");
     nationality = new SuggestBox(oracleNationality);
     MultiWordSuggestOracle oracleJob = new MultiWordSuggestOracle();
     oracleJob.add("Enseignant");
     oracleJob.add("Ingénieur");
     oracleJob.add("Informaticien");
     job = new SuggestBox(oracleJob);
     unit = UiHelper.createListBoxForEnum(FeeUnit.class, "FeeUnit_", false);
 
   }
 
   @UiHandler("country")
   public void onCountryChange(ValueChangeEvent<String> event) {
    city.setValue("");
    postalCode.setValue("");
  }
  @UiHandler("country")
  public void onCountrySelect(SelectionEvent<SuggestOracle.Suggestion> event) {
    presenter.onCountrySelect(event.getSelectedItem().getReplacementString());
    city.setValue("");
    postalCode.setValue("");
   }
   @UiHandler("postalCode")
   public void onPostalCodeChange(ValueChangeEvent<String> event) {
    city.setValue("");
  }
  @UiHandler("postalCode")
  public void onPostalCodeSelect(SelectionEvent<SuggestOracle.Suggestion> event) {
    presenter.onPostalCodeSelect(event.getSelectedItem().getReplacementString());
    city.setValue("");
   }
 }
