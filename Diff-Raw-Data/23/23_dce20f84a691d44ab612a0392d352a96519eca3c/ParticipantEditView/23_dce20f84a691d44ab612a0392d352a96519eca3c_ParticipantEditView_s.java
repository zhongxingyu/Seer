 package com.github.croesch.partimana.view;
 
 import com.github.croesch.annotate.MayBeNull;
 import com.github.croesch.annotate.NotNull;
 import com.github.croesch.components.*;
 import com.github.croesch.partimana.i18n.Text;
 import com.github.croesch.partimana.types.CountyCouncil;
 import com.github.croesch.partimana.types.Denomination;
 import com.github.croesch.partimana.types.Gender;
 import com.github.croesch.partimana.types.Participant;
 import com.github.croesch.partimana.view.api.IParticipantEditView;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import net.miginfocom.swing.MigLayout;
 
 /**
  * A panel to edit an {@link Participant}. Provides text fields to edit the different attributes.
  *
  * @author croesch
  * @since Date: Jun 8, 2011
  */
 class ParticipantEditView extends CPanel implements IParticipantEditView {
   /**
    * TODO i18n of the labels <br>
    * TODO improve check of numeric text fields
    */
 
   /** generated */
   private static final long serialVersionUID = 5388465740510568296L;
 
   /** the date format to parse the dates entered in the text fields */
   @NotNull
   private final DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
 
   /** the text field to edit the persons name */
   @NotNull
   private final CTextField firstNameTf = new CTextField("firstNameTF");
 
   /** the text field to edit the persons name */
   @NotNull
   private final CTextField lastNameTf = new CTextField("lastNameTF");
 
   /** the text field to edit the persons birth day */
   @NotNull
   private final CDateField birthDayTf = new CDateField(Locale.GERMAN);
 
   /** the text field to edit the persons living street */
   @NotNull
   private final CTextField livStreetTf = new CTextField("livingStreetTF");
 
   /** the text field to edit the persons living post code */
   @NotNull
   private final CTextField livPostCodeTf = new CTextField("livingPostCodeTF");
 
   /** the text field to edit the persons living city */
   @NotNull
   private final CTextField livCityTf = new CTextField("livingCityTF");
 
   /** the text field to edit the persons postal street */
   @NotNull
   private final CTextField posStreetTf = new CTextField("postalStreetTF");
 
   /** the text field to edit the persons postal post code */
   @NotNull
   private final CTextField posPostCodeTf = new CTextField("postalPostCodeTF");
 
   /** the text field to edit the persons postal city */
   @NotNull
   private final CTextField posCityTf = new CTextField("postalCityTF");
 
   /** the text field to edit the persons phone number */
   @NotNull
   private final CTextField phoneTf = new CTextField("phoneTF");
 
   /** the text field to edit the persons fax number */
   @NotNull
   private final CTextField faxTf = new CTextField("faxTF");
 
   /** the text field to edit the persons mobile phone number */
   @NotNull
   private final CTextField mobilePhoneTf = new CTextField("mobilePhoneTF");
 
   /** the text field to edit the phone of the persons parents */
   @NotNull
   private final CTextField phoneParentsTf = new CTextField("phoneOfParentsTF");
 
   /** the text field to edit the persons mail address */
   @NotNull
   private final CTextField mailAddressTf = new CTextField("mailTF");
 
   /** the text field to edit the persons bank account number */
   @NotNull
   private final CTextField bankAccNumberTf = new CTextField("bankAccountNumberTF");
 
   /** the text field to edit the persons bank code number */
   @NotNull
   private final CTextField bankCodeNumberTf = new CTextField("bankCodeNumberTF");
 
   /** the text field to edit the persons bank */
   @NotNull
   private final CTextField bankTf = new CTextField("bankTF");
 
   /** the text field to edit the persons until-in-db-date */
   @NotNull
   private final CDateField untilInDbTf = new CDateField(Locale.GERMAN);
 
   /** the combo box to edit the persons gender */
   @NotNull
   private final CComboBox genderCb = new CComboBox("genderCB", Gender.values());
 
   /** the combo box to edit the persons denomination */
   @NotNull
   private final CComboBox denominationCb = new CComboBox("denominationCB", Denomination.values());
 
   /** the combo box to edit the persons county council */
   @NotNull
   private final CComboBox countyCouncilCb = new CComboBox("countyCouncilCB", CountyCouncil.values());
 
   /** the text area to edit the comment about the person */
   @NotNull
   private final CTextArea commentTa = new CTextArea("commentTF");
 
   /** the label that contains the id of the person */
   @NotNull
   private final CLabel idValueLbl = new CLabel("idLbl", "12345");
 
   /** the label that contains the date since the person is in data base */
   @NotNull
   private final CLabel sinceInDbValueLbl = new CLabel("dateSinceInDBLbl", "12.12.2003");
 
   /**
    * Constructs a new panel, that contains all necessary components to edit the attributes of a {@link Participant}.
    * Also it is able to fill the content of the different components with the values fetched from a given {@link
    * Participant}. Therefore see {@link #setParticipant(Participant)}.
    *
    * @param name the name of this component
    * @see #setParticipant(Participant)
    * @since Date: Jun 28, 2011
    */
   public ParticipantEditView(final String name) {
     super(name);
 
     final int space = 15;
     setLayout(new MigLayout("fill, insets 0",
                             "[110px!, sg t, align trailing][grow, fill, sg f][sg t, align trailing][grow, fill, "
                             + "sg f][sg t, align trailing][grow, fill, sg f]",
                             "[sg]" + space + "[sg][sg]" + space + "[sg][sg][sg]" + space + "[sg][sg]" + space + "[sg]"
                             + space + "[sg][sg][sg][grow]"));
 
     initNames();
 
     addComponents();
 
     clear();
   }
 
   /**
    * Adds all components to this panel.
    *
    * @since Date: Jun 28, 2011
    */
   private void addComponents() {
     add(new CLabel("id", Text.PARTICIPANT_ID.text()), "cell 0 0");
     add(this.idValueLbl, "cell 1 0");
 
     add(new CLabel("sinceInDb", Text.PARTICIPANT_SINCE.text()), "cell 2 0");
     add(this.sinceInDbValueLbl, "cell 3 0");
 
     add(new CLabel("untilInDb", Text.PARTICIPANT_UNTIL.text()), "cell 4 0");
     add(this.untilInDbTf, "cell 5 0");
 
     add(new CLabel("lastName", Text.PARTICIPANT_LASTNAME.text()), "cell 0 1");
     add(this.lastNameTf, "cell 1 1");
 
     add(new CLabel("firstName", Text.PARTICIPANT_FORENAME.text()), "cell 2 1");
     add(this.firstNameTf, "cell 3 1");
 
     add(new CLabel("birthday", Text.PARTICIPANT_BIRTHDAY.text()), "cell 4 1");
     add(this.birthDayTf, "cell 5 1");
 
     add(new CLabel("gender", Text.PARTICIPANT_GENDER.text()), "cell 0 2");
     add(this.genderCb, "cell 1 2");
 
    add(new CLabel("denomination", Text.PARTICIPANT_DENOMINTAION.text()), "cell 2 2");
     add(this.denominationCb, "cell 3 2");
 
     add(new CLabel("countyCouncil", Text.PARTICIPANT_COUNTY_COUNCIL.text()), "cell 4 2");
     add(this.countyCouncilCb, "cell 5 2");
 
     add(new CLabel("street", Text.STREET.text()), "cell 1 3, grow 0, alignx center");
     add(new CLabel("postCode", Text.POST_CODE.text()), "cell 3 3, grow 0, alignx center");
     add(new CLabel("city", Text.CITY.text()), "cell 5 3, grow 0, alignx center");
 
     add(new CLabel("livingAddress", Text.PARTICIPANT_ADDRESS_LIVING.text()), "cell 0 4");
 
     add(this.livStreetTf, "cell 1 4");
     add(this.livPostCodeTf, "cell 3 4");
     add(this.livCityTf, "cell 5 4");
 
     add(new CLabel("postalAddress", Text.PARTICIPANT_ADDRESS_POSTAL.text()), "cell 0 5");
 
     add(this.posStreetTf, "cell 1 5");
     add(this.posPostCodeTf, "cell 3 5");
     add(this.posCityTf, "cell 5 5");
 
     add(new CLabel("phone", Text.PARTICIPANT_PHONE.text()), "cell 0 6");
     add(this.phoneTf, "cell 1 6");
 
     add(new CLabel("fax", Text.PARTICIPANT_FAX.text()), "cell 2 6");
     add(this.faxTf, "cell 3 6");
 
     add(new CLabel("mobilePhone", Text.PARTICIPANT_MOBILE_PHONE.text()), "cell 4 6");
     add(this.mobilePhoneTf, "cell 5 6");
 
     add(new CLabel("phoneOfParents", Text.PARTICIPANT_PHONE_OF_PARENTS.text()), "cell 0 7");
     add(this.phoneParentsTf, "cell 1 7");
 
     add(new CLabel("mailAddress", Text.PARTICIPANT_MAIL_ADDRESS.text()), "cell 2 7");
     add(this.mailAddressTf, "cell 3 7 3 1");
 
     add(new CLabel("bankAccountNumber", Text.PARTICIPANT_BANK_ACCOUNT_NUMBER.text()), "cell 0 8");
     add(this.bankAccNumberTf, "cell 1 8");
 
     add(new CLabel("bankCodeNumber", Text.PARTICIPANT_BANK_CODE_NUMBER.text()), "cell 2 8");
     add(this.bankCodeNumberTf, "cell 3 8");
 
     add(new CLabel("bank", Text.PARTICIPANT_BANK_NAME.text()), "cell 4 8");
     add(this.bankTf, "cell 5 8");
 
     final CScrollPane commentScrollPane = new CScrollPane("comment");
     commentScrollPane.setViewportView(this.commentTa);
     add(commentScrollPane, "cell 1 9 5 3,grow");
 
     add(new CLabel("comment", Text.PARTICIPANT_COMMENT.text()), "cell 0 9");
   }
 
   /**
    * Initializes the names of all components.
    *
    * @since Date: Jun 28, 2011
    */
   private void initNames() {
     this.birthDayTf.setName("birthTF");
     this.untilInDbTf.setName("dateUpToInDBTF");
   }
 
   @Override
   public final void clear() {
     this.firstNameTf.setText(null);
     this.lastNameTf.setText(null);
     this.genderCb.setSelectedItem(null);
     this.denominationCb.setSelectedItem(null);
     this.birthDayTf.setText(null);
     this.posStreetTf.setText(null);
     this.posPostCodeTf.setText(null);
     this.posCityTf.setText(null);
     this.countyCouncilCb.setSelectedItem(null);
     this.bankTf.setText(null);
     this.bankAccNumberTf.setText(null);
     this.bankCodeNumberTf.setText(null);
     this.commentTa.setText(null);
     this.untilInDbTf.setText(null);
     this.sinceInDbValueLbl.setText(null);
     this.faxTf.setText(null);
     this.mailAddressTf.setText(null);
     this.mobilePhoneTf.setText(null);
     this.phoneTf.setText(null);
     this.phoneParentsTf.setText(null);
     this.livCityTf.setText(null);
     this.livPostCodeTf.setText(null);
     this.livStreetTf.setText(null);
     this.idValueLbl.setText(null);
   }
 
   @Override
   public void setParticipant(final Participant participant) {
     if (participant == null) {
       clear();
     } else {
 
       this.firstNameTf.setText(participant.getForeName());
       this.lastNameTf.setText(participant.getLastName());
       this.genderCb.setSelectedItem(participant.getGender());
       this.denominationCb.setSelectedItem(participant.getDenomination());
       this.birthDayTf.setDateAndDisplay(participant.getBirthDate());
       this.posStreetTf.setText(participant.getStreetPostal());
       this.posPostCodeTf.setText(String.valueOf(participant.getPostCodePostal()));
       this.posCityTf.setText(participant.getCityPostal());
       this.countyCouncilCb.setSelectedItem(participant.getCountyCouncil());
       this.bankTf.setText(participant.getBank());
       this.bankAccNumberTf.setText(String.valueOf(participant.getBankAccountNumber()));
       this.bankCodeNumberTf.setText(String.valueOf(participant.getBankCodeNumber()));
       this.commentTa.setText(participant.getComment());
       if (participant.getDateUpToInSystem() != null) {
         this.untilInDbTf.setDateAndDisplay(participant.getDateUpToInSystem());
       } else {
         this.untilInDbTf.setText(null);
       }
       if (participant.getDateSinceInDataBase() != null) {
         this.sinceInDbValueLbl.setText(this.dateFormat.format(participant.getDateSinceInDataBase()));
       } else {
         this.sinceInDbValueLbl.setText(null);
       }
       this.faxTf.setText(participant.getFax());
       this.mailAddressTf.setText(participant.getMailAddress());
       this.mobilePhoneTf.setText(participant.getMobilePhone());
       this.phoneTf.setText(participant.getPhone());
       this.phoneParentsTf.setText(participant.getPhoneOfParents());
       this.livCityTf.setText(participant.getCity());
       this.livPostCodeTf.setText(String.valueOf(participant.getPostCode()));
       this.livStreetTf.setText(participant.getStreet());
       this.idValueLbl.setText(String.valueOf(participant.getId()));
     }
   }
 
   @Override
   public String getFirstName() {
     return this.firstNameTf.getText();
   }
 
   @Override
   public String getLastName() {
     return this.lastNameTf.getText();
   }
 
   @Override
   public Gender getGender() {
     return (Gender) this.genderCb.getSelectedItem();
   }
 
   @Override
   public Denomination getDenomination() {
     return (Denomination) this.denominationCb.getSelectedItem();
   }
 
   @Override
   @MayBeNull
   public Date getBirthDate() {
     return this.birthDayTf.getDateWithoutTimeOrNull();
   }
 
   @Override
   public String getPostalStreet() {
     return this.posStreetTf.getText();
   }
 
   @Override
   public int getPostalPostCode() {
     final String text = this.posPostCodeTf.getText();
     if (text == null || text.trim().equals("")) {
       return 0;
     }
     return Integer.parseInt(text);
   }
 
   @Override
   public String getPostalCity() {
     return this.posCityTf.getText();
   }
 
   @Override
   public CountyCouncil getCountyCouncil() {
     return (CountyCouncil) this.countyCouncilCb.getSelectedItem();
   }
 
   @Override
   public String getBank() {
     return this.bankTf.getText();
   }
 
   @Override
   public int getBankAccountNumber() {
     final String text = this.bankAccNumberTf.getText();
     if (text == null || text.trim().equals("")) {
       return 0;
     }
     return Integer.parseInt(text);
   }
 
   @Override
   public int getBankCodeNumber() {
     final String text = this.bankCodeNumberTf.getText();
     if (text == null || text.trim().equals("")) {
       return 0;
     }
     return Integer.parseInt(text);
   }
 
   @Override
   public String getComment() {
     return this.commentTa.getText();
   }
 
   @Override
   @MayBeNull
   public Date getDateUpToInDataBase() {
     return this.untilInDbTf.getDateWithoutTimeOrNull();
   }
 
   @Override
   public String getFax() {
     return this.faxTf.getText();
   }
 
   @Override
   public String getMailAddress() {
     return this.mailAddressTf.getText();
   }
 
   @Override
   public String getMobilePhone() {
     return this.mobilePhoneTf.getText();
   }
 
   @Override
   public String getPhone() {
     return this.phoneTf.getText();
   }
 
   @Override
   public String getPhoneOfParents() {
     return this.phoneParentsTf.getText();
   }
 
   @Override
   public String getLivingStreet() {
     return this.livStreetTf.getText();
   }
 
   @Override
   public int getLivingPostCode() {
     final String text = this.livPostCodeTf.getText();
     if (text == null || text.trim().equals("")) {
       return 0;
     }
     return Integer.parseInt(text);
   }
 
   @Override
   public String getLivingCity() {
     return this.livCityTf.getText();
   }
 
   @Override
   public long getId() {
     final String text = this.idValueLbl.getText();
     if (text == null || text.trim().equals("")) {
       return 0;
     }
     return Long.parseLong(text);
   }
 }
