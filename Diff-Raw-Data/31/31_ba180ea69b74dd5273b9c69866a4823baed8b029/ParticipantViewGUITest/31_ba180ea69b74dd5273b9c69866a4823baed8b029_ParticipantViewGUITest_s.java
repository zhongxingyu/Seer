 package com.github.croesch.partimana.view;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.util.Date;
 import java.util.List;
 
 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.edt.GuiTask;
 import org.fest.swing.fixture.Containers;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.fixture.JPanelFixture;
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.github.croesch.partimana.PartiManaDefaultGUITestCase;
 import com.github.croesch.partimana.actions.ActionObserver;
 import com.github.croesch.partimana.actions.UserAction;
 import com.github.croesch.partimana.model.api.IModel4View;
 import com.github.croesch.partimana.types.Camp;
 import com.github.croesch.partimana.types.CountyCouncil;
 import com.github.croesch.partimana.types.Denomination;
 import com.github.croesch.partimana.types.Gender;
 import com.github.croesch.partimana.types.Participant;
 
 /**
  * Provides gui tests for {@link ParticipantView}.
  * 
  * @author croesch
  * @since Date: Jul 11, 2011
  */
 public class ParticipantViewGUITest extends PartiManaDefaultGUITestCase {
 
   private JPanelFixture testView;
 
   private Participant participant;
 
   private final IModel4View model = new IModel4View() {
 
     @Override
     public Participant getParticipant(final long id) {
       return null;
     }
 
     @Override
     public List<Participant> getListOfParticipants() {
      return null;
     }
 
     @Override
     public Camp getCamp(final long id) {
       return null;
     }
 
     @Override
     public List<Camp> getListOfCamps() {
      return null;
     }
   };
 
   private ParticipantView pView;
 
   private boolean deleteActionPerformed;
 
   @Override
   protected void before() {
     Action.setObserver(new ActionObserver() {
 
       @Override
       public void performAction(final UserAction action) {
         if (action == UserAction.CREATE_PARTICIPANT) {
           ParticipantViewGUITest.this.pView.createParticipant();
         } else if (action == UserAction.DELETE_PARTICIPANT) {
           ParticipantViewGUITest.this.deleteActionPerformed = true;
         } else {
           Assert.fail();
         }
       }
     });
 
     this.pView = GuiActionRunner.execute(new GuiQuery<ParticipantView>() {
       @Override
       protected ParticipantView executeInEDT() throws Throwable {
         return new ParticipantView(ParticipantViewGUITest.this.model);
       }
     });
 
     this.participant = new Participant("Mustermann",
                                        "Max",
                                        Gender.FEMALE,
                                        Denomination.OTHER,
                                        new Date(),
                                        "Musterstrasse 12",
                                        12345,
                                        "Musterhausen",
                                        CountyCouncil.OTHER);
 
     final Date d1 = new Date(123);
 
     this.participant.setBank("bank");
     this.participant.setBankAccountNumber(1);
     this.participant.setBankCodeNumber(12);
     this.participant.setComment("comment");
     this.participant.setDateSinceInDataBase(d1);
     this.participant.setDateUpToInSystem(d1);
     this.participant.setDenomination(Denomination.JEWISH);
     this.participant.setFax("fax");
     this.participant.setMailAddress("mail");
     this.participant.setMobilePhone("mobile");
     this.participant.setPhone("phone");
     this.participant.setPhoneOfParents("phone");
     this.participant.setPossibleAGE(true);
     this.participant.setPossibleBoard(true);
     this.participant.setPossibleExtendedBoard(true);
     this.participant.setPossibleKitchen(true);
     this.participant.setPossibleMAK(true);
     this.participant.setPossibleMisc(true);
     this.participant.setPossibleParticipant(true);
     this.participant.setPossibleSeminar(true);
     this.participant.setPossibleStaff(true);
     this.participant.setPossibleStaffYouth(true);
     this.participant.setCityPostal("city");
     this.participant.setPostCodePostal(3124);
     this.participant.setStreetPostal("street");
 
     GuiActionRunner.execute(new GuiTask() {
 
       @Override
       protected void executeInEDT() throws Throwable {
         ParticipantViewGUITest.this.pView.getParticipantEditView()
                                          .setParticipant(ParticipantViewGUITest.this.participant);
       }
     });
     this.pView.setName("view");
 
     final FrameFixture window = new FrameFixture(robot(), Containers.frameFor(this.pView));
     window.show();
     this.testView = window.panel("view");
 
     this.deleteActionPerformed = false;
   }
 
   @Test
   public void testCreateNewParticipant_Button() {
 
     this.testView.button("newParticipant").click();
 
     this.testView.textBox("firstNameTF").requireEmpty();
     this.testView.textBox("lastNameTF").requireEmpty();
     this.testView.comboBox("genderCB").requireNoSelection();
     this.testView.textBox("birthTF").requireEmpty();
     this.testView.comboBox("denominationCB").requireNoSelection();
     this.testView.textBox("postalStreetTF").requireEmpty();
     this.testView.textBox("postalPostCodeTF").requireEmpty();
     this.testView.textBox("postalCityTF").requireEmpty();
     this.testView.comboBox("countyCouncilCB").requireNoSelection();
     this.testView.textBox("bankTF").requireEmpty();
     this.testView.textBox("bankAccountNumberTF").requireEmpty();
     this.testView.textBox("bankCodeNumberTF").requireEmpty();
     this.testView.textBox("commentTF").requireEmpty();
     this.testView.textBox("dateUpToInDBTF").requireEmpty();
     this.testView.textBox("faxTF").requireEmpty();
     this.testView.textBox("mailTF").requireEmpty();
     this.testView.textBox("mobilePhoneTF").requireEmpty();
     this.testView.textBox("phoneTF").requireEmpty();
     this.testView.textBox("phoneOfParentsTF").requireEmpty();
     this.testView.checkBox("possibleAGECB").requireNotSelected();
     this.testView.checkBox("possibleBoardCB").requireNotSelected();
     this.testView.checkBox("possibleExtendedBoardCB").requireNotSelected();
     this.testView.checkBox("possibleKitchenCB").requireNotSelected();
     this.testView.checkBox("possibleMAKCB").requireNotSelected();
     this.testView.checkBox("possibleMiscCB").requireNotSelected();
     this.testView.checkBox("possibleParticipantCB").requireNotSelected();
     this.testView.checkBox("possibleSeminarCB").requireNotSelected();
     this.testView.checkBox("possibleStaffCB").requireNotSelected();
     this.testView.checkBox("possibleStaffYouthCB").requireNotSelected();
     this.testView.textBox("livingStreetTF").requireEmpty();
     this.testView.textBox("livingPostCodeTF").requireEmpty();
     this.testView.textBox("livingCityTF").requireEmpty();
     final String n = null;
     this.testView.label("idLbl").requireText(n);
     this.testView.label("dateSinceInDBLbl").requireText(n);
   }
 
   @Test
   public void testCreateNewParticipant_Method() {
 
     GuiActionRunner.execute(new GuiTask() {
       @Override
       protected void executeInEDT() throws Throwable {
         ParticipantViewGUITest.this.pView.createParticipant();
       }
     });
 
     this.testView.textBox("firstNameTF").requireEmpty();
     this.testView.textBox("lastNameTF").requireEmpty();
     this.testView.comboBox("genderCB").requireNoSelection();
     this.testView.textBox("birthTF").requireEmpty();
     this.testView.comboBox("denominationCB").requireNoSelection();
     this.testView.textBox("postalStreetTF").requireEmpty();
     this.testView.textBox("postalPostCodeTF").requireEmpty();
     this.testView.textBox("postalCityTF").requireEmpty();
     this.testView.comboBox("countyCouncilCB").requireNoSelection();
     this.testView.textBox("bankTF").requireEmpty();
     this.testView.textBox("bankAccountNumberTF").requireEmpty();
     this.testView.textBox("bankCodeNumberTF").requireEmpty();
     this.testView.textBox("commentTF").requireEmpty();
     this.testView.textBox("dateUpToInDBTF").requireEmpty();
     this.testView.textBox("faxTF").requireEmpty();
     this.testView.textBox("mailTF").requireEmpty();
     this.testView.textBox("mobilePhoneTF").requireEmpty();
     this.testView.textBox("phoneTF").requireEmpty();
     this.testView.textBox("phoneOfParentsTF").requireEmpty();
     this.testView.checkBox("possibleAGECB").requireNotSelected();
     this.testView.checkBox("possibleBoardCB").requireNotSelected();
     this.testView.checkBox("possibleExtendedBoardCB").requireNotSelected();
     this.testView.checkBox("possibleKitchenCB").requireNotSelected();
     this.testView.checkBox("possibleMAKCB").requireNotSelected();
     this.testView.checkBox("possibleMiscCB").requireNotSelected();
     this.testView.checkBox("possibleParticipantCB").requireNotSelected();
     this.testView.checkBox("possibleSeminarCB").requireNotSelected();
     this.testView.checkBox("possibleStaffCB").requireNotSelected();
     this.testView.checkBox("possibleStaffYouthCB").requireNotSelected();
     this.testView.textBox("livingStreetTF").requireEmpty();
     this.testView.textBox("livingPostCodeTF").requireEmpty();
     this.testView.textBox("livingCityTF").requireEmpty();
     final String n = null;
     this.testView.label("idLbl").requireText(n);
     this.testView.label("dateSinceInDBLbl").requireText(n);
   }
 
   @Test
   public void testDeleteParticipant() {
     assertThat(this.deleteActionPerformed).isFalse();
     this.testView.button("deleteParticipant").click();
 
     assertThat(this.deleteActionPerformed).isTrue();
   }
 }
