 package com.github.croesch.partimana.view;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import net.miginfocom.swing.MigLayout;
 
 import com.github.croesch.annotate.MayBeNull;
 import com.github.croesch.annotate.NotNull;
 import com.github.croesch.components.CCheckBox;
 import com.github.croesch.components.CComboBox;
 import com.github.croesch.components.CDateField;
 import com.github.croesch.components.CLabel;
 import com.github.croesch.components.CPanel;
 import com.github.croesch.components.CScrollPane;
 import com.github.croesch.components.CTextArea;
 import com.github.croesch.components.CTextField;
 import com.github.croesch.partimana.i18n.Text;
 import com.github.croesch.partimana.types.CountyCouncil;
 import com.github.croesch.partimana.types.Denomination;
 import com.github.croesch.partimana.types.Gender;
 import com.github.croesch.partimana.types.Participant;
 import com.github.croesch.partimana.view.api.IParticipantEditView;
 
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
   private final CComboBox countyCouncilCb = new CComboBox("countyCouncilCB", CountyCouncil.values());;
 
   /** the text area to edit the comment about the person */
   @NotNull
   private final CTextArea commentTa = new CTextArea("commentTF");
 
   /** the check box to mark that this person can be participant */
   @NotNull
   private final CCheckBox possParticipantCb = new CCheckBox("possibleParticipantCB",
                                                             Text.PARTICIPANT_CAMP_PARTICIPANT.text());
 
   /** the check box to mark that this person can be staff */
   @NotNull
   private final CCheckBox possStaffCb = new CCheckBox("possibleStaffCB", Text.PARTICIPANT_STAFF_GENERAL.text());
 
   /** the check box to mark that this person can be staff youth */
   @NotNull
   private final CCheckBox possStaffYouthCb = new CCheckBox("possibleStaffYouthCB", Text.PARTICIPANT_STAFF_YOUTH.text());
 
   /** the check box to mark that this person can be board member */
   @NotNull
   private final CCheckBox possBoardCb = new CCheckBox("possibleBoardCB", Text.PARTICIPANT_BOARD.text());
 
   /** the check box to mark that this person can be extended board member */
   @NotNull
   private final CCheckBox possExtendedBoardCb = new CCheckBox("possibleExtendedBoardCB",
                                                               Text.PARTICIPANT_EXTENDED_BOARD.text());
 
   /** the check box to mark that this person can be MAK */
   @NotNull
   private final CCheckBox possMakCb = new CCheckBox("possibleMAKCB", Text.PARTICIPANT_MAK.text());
 
   /** the check box to mark that this person can be AGE */
   @NotNull
   private final CCheckBox possAgeCb = new CCheckBox("possibleAGECB", Text.PARTICIPANT_AGE.text());
 
   /** the check box to mark that this person can be seminar member */
   @NotNull
   private final CCheckBox possSeminarCb = new CCheckBox("possibleSeminarCB", Text.PARTICIPANT_SEMINAR.text());
 
   /** the check box to mark that this person can be kitchen member */
   @NotNull
   private final CCheckBox possKitchenCb = new CCheckBox("possibleKitchenCB", Text.PARTICIPANT_CAMP_KITCHEN.text());
 
   /** the check box to mark that this person can be misc. */
   @NotNull
   private final CCheckBox possMiscCb = new CCheckBox("possibleMiscCB", Text.PARTICIPANT_MISC.text());
 
   /** the label that contains the id of the person */
   @NotNull
   private final CLabel idValueLbl = new CLabel("idLbl", "12345");
 
   /** the label that contains the date since the person is in data base */
   @NotNull
   private final CLabel sinceInDbValueLbl = new CLabel("dateSinceInDBLbl", "12.12.2003");
 
   /** panel that contains components to edit the different functions of the person */
   @NotNull
   private CPanel possibleFunctionsPanel;
 
   /**
    * Constructs a new panel, that contains all necessary components to edit the attributes of a {@link Participant}.
    * Also it is able to fill the content of the different components with the values fetched from a given
    * {@link Participant}. Therefore see {@link #setParticipant(Participant)}.
    * 
    * @author croesch
    * @since Date: Jun 28, 2011
   * @param the name of this component
    * @see #setParticipant(Participant)
    */
   public ParticipantEditView(final String name) {
     super(name);
 
     final int space = 15;
     setLayout(new MigLayout("fill, insets 0",
                             "[110px!, sg t, align trailing][grow, fill, sg f][sg t, align trailing][grow, fill, sg f][sg t, align trailing][grow, fill, sg f]",
                             "[sg]" + space + "[sg][sg]" + space + "[sg][sg][sg]" + space + "[sg][sg]" + space + "[sg]"
                                     + space + "[sg][sg][sg]" + space + "[][grow]"));
 
     initNames();
 
     addComponents();
 
     clear();
   }
 
   /**
    * Adds all components to this panel.
    * 
    * @author croesch
    * @since Date: Jun 28, 2011
    */
   private void addComponents() {
     final CLabel idLbl = new CLabel("id", Text.PARTICIPANT_ID.text());
     add(idLbl, "cell 0 0");
 
     add(this.idValueLbl, "cell 1 0");
 
     final CLabel sinceInDbLbl = new CLabel("sinceInDb", Text.PARTICIPANT_SINCE.text());
     add(sinceInDbLbl, "cell 2 0");
 
     add(this.sinceInDbValueLbl, "cell 3 0");
 
     final CLabel untilInDbLbl = new CLabel("untilInDb", Text.PARTICIPANT_UNTIL.text());
     add(untilInDbLbl, "cell 4 0");
 
     add(this.untilInDbTf, "cell 5 0");
 
     final CLabel firstNameLbl = new CLabel("firstName", Text.PARTICIPANT_FORENAME.text());
     add(firstNameLbl, "cell 0 1");
 
     add(this.firstNameTf, "cell 1 1");
 
     final CLabel lastNameLbl = new CLabel("lastName", Text.PARTICIPANT_LASTNAME.text());
     add(lastNameLbl, "cell 2 1");
 
     add(this.lastNameTf, "cell 3 1");
 
     final CLabel genderLbl = new CLabel("gender", Text.PARTICIPANT_GENDER.text());
     add(genderLbl, "cell 4 1");
 
     add(this.genderCb, "cell 5 1");
 
     final CLabel birthDayLbl = new CLabel("birthday", Text.PARTICIPANT_BIRTHDAY.text());
     add(birthDayLbl, "cell 0 2");
 
     add(this.birthDayTf, "cell 1 2");
 
     final CLabel denominationLbl = new CLabel("denomination", Text.PARTICIPANT_DENOMINTAION.text());
     add(denominationLbl, "cell 2 2");
 
     add(this.denominationCb, "cell 3 2");
 
     final CLabel countyCouncilLbl = new CLabel("countyCouncil", Text.PARTICIPANT_COUNTY_COUNCIL.text());
     add(countyCouncilLbl, "cell 4 2");
 
     add(this.countyCouncilCb, "cell 5 2");
 
     final CLabel streetLbl = new CLabel("street", Text.STREET.text());
     add(streetLbl, "cell 1 3, grow 0, alignx center");
 
     final CLabel postCodeLbl = new CLabel("postCode", Text.POST_CODE.text());
     add(postCodeLbl, "cell 3 3, grow 0, alignx center");
 
     final CLabel cityLbl = new CLabel("city", Text.CITY.text());
     add(cityLbl, "cell 5 3, grow 0, alignx center");
 
     final CLabel livingAddressLbl = new CLabel("livingAddress", Text.PARTICIPANT_ADDRESS_LIVING.text());
     add(livingAddressLbl, "cell 0 4");
 
     add(this.livStreetTf, "cell 1 4");
 
     add(this.livPostCodeTf, "cell 3 4");
 
     add(this.livCityTf, "cell 5 4");
 
     final CLabel postToAddressLbl = new CLabel("postalAddress", Text.PARTICIPANT_ADDRESS_POSTAL.text());
     add(postToAddressLbl, "cell 0 5");
 
     add(this.posStreetTf, "cell 1 5");
 
     add(this.posPostCodeTf, "cell 3 5");
 
     add(this.posCityTf, "cell 5 5");
 
     final CLabel phoneLbl = new CLabel("phone", Text.PARTICIPANT_PHONE.text());
     add(phoneLbl, "cell 0 6");
 
     add(this.phoneTf, "cell 1 6");
 
     final CLabel faxLbl = new CLabel("fax", Text.PARTICIPANT_FAX.text());
     add(faxLbl, "cell 2 6");
 
     add(this.faxTf, "cell 3 6");
 
     final CLabel mobilePhoneLbl = new CLabel("mobilePhone", Text.PARTICIPANT_MOBILE_PHONE.text());
     add(mobilePhoneLbl, "cell 4 6");
 
     add(this.mobilePhoneTf, "cell 5 6");
 
     final CLabel phoneParentsLbl = new CLabel("phoneOfParents", Text.PARTICIPANT_PHONE_OF_PARENTS.text());
     add(phoneParentsLbl, "cell 0 7");
 
     add(this.phoneParentsTf, "cell 1 7");
 
     final CLabel mailAddressLbl = new CLabel("mailAddress", Text.PARTICIPANT_MAIL_ADDRESS.text());
     add(mailAddressLbl, "cell 2 7");
 
     add(this.mailAddressTf, "cell 3 7 3 1");
 
     final CLabel bankAccNumberLbl = new CLabel("bankAccountNumber", Text.PARTICIPANT_BANK_ACCOUNT_NUMBER.text());
     add(bankAccNumberLbl, "cell 0 8");
 
     add(this.bankAccNumberTf, "cell 1 8");
 
     final CLabel bankeCodNumberLbl = new CLabel("bankCodeNumber", Text.PARTICIPANT_BANK_CODE_NUMBER.text());
     add(bankeCodNumberLbl, "cell 2 8");
 
     add(this.bankCodeNumberTf, "cell 3 8");
 
     final CLabel bankLbl = new CLabel("bank", Text.PARTICIPANT_BANK_NAME.text());
     add(bankLbl, "cell 4 8");
 
     add(this.bankTf, "cell 5 8");
 
     final CScrollPane commentScrollPane = new CScrollPane("comment");
     add(commentScrollPane, "cell 1 9 5 3,grow");
 
     commentScrollPane.setViewportView(this.commentTa);
 
     final CLabel commentLbl = new CLabel("comment", Text.PARTICIPANT_COMMENT.text());
     add(commentLbl, "cell 0 9");
 
     initPossibleFunctionsPanel();
 
     add(this.possibleFunctionsPanel, "cell 0 12 6 1, alignx leading");
   }
 
   /**
    * Initializes the {@link #possibleFunctionsPanel} and adds the components to it.
    * 
    * @author croesch
    * @since Date: Jun 28, 2011
    */
   private void initPossibleFunctionsPanel() {
     this.possibleFunctionsPanel = new CPanel("possibleFunctions");
     this.possibleFunctionsPanel.setLayout(new MigLayout("", "[110px!][][][][][]", "[sg][sg]"));
 
     final CLabel functionsLbl = new CLabel("functions", Text.PARTICIPANT_FUNCTIONS.text());
     this.possibleFunctionsPanel.add(functionsLbl, "cell 0 0");
 
     this.possibleFunctionsPanel.add(this.possParticipantCb, "cell 1 0");
 
     this.possibleFunctionsPanel.add(this.possStaffCb, "cell 2 0");
 
     this.possibleFunctionsPanel.add(this.possStaffYouthCb, "cell 3 0");
 
     this.possibleFunctionsPanel.add(this.possBoardCb, "cell 4 0");
 
     this.possibleFunctionsPanel.add(this.possExtendedBoardCb, "cell 5 0");
 
     this.possibleFunctionsPanel.add(this.possMakCb, "cell 1 1");
 
     this.possibleFunctionsPanel.add(this.possAgeCb, "cell 2 1");
 
     this.possibleFunctionsPanel.add(this.possSeminarCb, "cell 3 1");
 
     this.possibleFunctionsPanel.add(this.possKitchenCb, "cell 4 1");
 
     this.possibleFunctionsPanel.add(this.possMiscCb, "cell 5 1");
   }
 
   /**
    * Initializes the names of all components.
    * 
    * @author croesch
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
     this.possAgeCb.setSelected(false);
     this.possBoardCb.setSelected(false);
     this.possExtendedBoardCb.setSelected(false);
     this.possKitchenCb.setSelected(false);
     this.possMakCb.setSelected(false);
     this.possMiscCb.setSelected(false);
     this.possParticipantCb.setSelected(false);
     this.possSeminarCb.setSelected(false);
     this.possStaffCb.setSelected(false);
     this.possStaffYouthCb.setSelected(false);
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
       this.possAgeCb.setSelected(participant.isPossibleAGE());
       this.possBoardCb.setSelected(participant.isPossibleBoard());
       this.possExtendedBoardCb.setSelected(participant.isPossibleExtendedBoard());
       this.possKitchenCb.setSelected(participant.isPossibleKitchen());
       this.possMakCb.setSelected(participant.isPossibleMAK());
       this.possMiscCb.setSelected(participant.isPossibleMisc());
       this.possParticipantCb.setSelected(participant.isPossibleParticipant());
       this.possSeminarCb.setSelected(participant.isPossibleSeminar());
       this.possStaffCb.setSelected(participant.isPossibleStaff());
       this.possStaffYouthCb.setSelected(participant.isPossibleStaffYouth());
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
   public boolean getPossibleAGE() {
     return this.possAgeCb.isSelected();
   }
 
   @Override
   public boolean getPossibleBoard() {
     return this.possBoardCb.isSelected();
   }
 
   @Override
   public boolean getPossibleExtendedBoard() {
     return this.possExtendedBoardCb.isSelected();
   }
 
   @Override
   public boolean getPossibleKitchen() {
     return this.possKitchenCb.isSelected();
   }
 
   @Override
   public boolean getPossibleMAK() {
     return this.possMakCb.isSelected();
   }
 
   @Override
   public boolean getPossibleMisc() {
     return this.possMiscCb.isSelected();
   }
 
   @Override
   public boolean getPossibleParticipant() {
     return this.possParticipantCb.isSelected();
   }
 
   @Override
   public boolean getPossibleSeminar() {
     return this.possSeminarCb.isSelected();
   }
 
   @Override
   public boolean getPossibleStaff() {
     return this.possStaffCb.isSelected();
   }
 
   @Override
   public boolean getPossibleStaffYouth() {
     return this.possStaffYouthCb.isSelected();
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
