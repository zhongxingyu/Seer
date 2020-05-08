 package com.github.croesch.partimana.controller;
 
 import static org.fest.assertions.Assertions.assertThat;
 
import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.edt.GuiTask;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.github.croesch.partimana.actions.ActionObserver;
 import com.github.croesch.partimana.actions.UserAction;
 import com.github.croesch.partimana.i18n.Text;
 import com.github.croesch.partimana.model.api.ICampModel;
 import com.github.croesch.partimana.model.api.IModel4View;
 import com.github.croesch.partimana.types.Camp;
 import com.github.croesch.partimana.types.CampParticipant;
 import com.github.croesch.partimana.types.CountyCouncil;
 import com.github.croesch.partimana.types.Denomination;
 import com.github.croesch.partimana.types.Gender;
 import com.github.croesch.partimana.types.Participant;
 import com.github.croesch.partimana.types.exceptions.RequiredFieldSetToNullException;
 import com.github.croesch.partimana.view.View;
 import com.github.croesch.partimana.view.api.ICampEditView;
 import com.github.croesch.partimana.view.api.IStatusView;
 
 /**
  * Provides test methods for {@link CampSaver}
  * 
  * @author croesch
  * @since Date: Sep 23, 2012
  */
 public class CampSaverTest {
 
   private Camp stored;
 
   private static ICampEditView editView;
 
   private static IStatusView statusView;
 
   private ICampModel model;
 
   private Camp dummyCamp;
 
   @BeforeClass
   public static void setUpOnce() {
 
     editView = GuiActionRunner.execute(new GuiQuery<ICampEditView>() {
       @Override
       protected ICampEditView executeInEDT() throws Throwable {
         return new View(new IModel4View() {
 
           @Override
           public Participant getParticipant(final long id) {
             return null;
           }
 
           @Override
           public List<Participant> getListOfParticipants() {
            return new ArrayList<Participant>();
           }
 
           @Override
           public Camp getCamp(final long id) {
             return null;
           }
 
           @Override
           public List<Camp> getListOfCamps() {
            return new ArrayList<Camp>();
           }
         }, new ActionObserver() {
 
           @Override
           public void performAction(final UserAction action) {
             // do nothing
           }
         }).getCampEditView();
       }
     });
     statusView = new IStatusView() {
 
       @Override
       public void showInformation(final Text info, final Object ... args) {}
 
       @Override
       public void showInformation(final Text info) {}
 
       @Override
       public void showError(final Text error) {}
     };
 
   }
 
   @Before
   public void setUp() {
     this.dummyCamp = new Camp("CAMP", new Date(82800000), new Date(182800000), "Entenhausen", "1 euro");
     this.dummyCamp.setRatePerDayChildren("2 euro");
 
     this.model = new ICampModel() {
 
       @Override
       public Camp getCamp(final long id) {
         return CampSaverTest.this.dummyCamp;
       }
 
       @Override
       public List<Camp> getListOfCamps() {
         return null;
       }
 
       @Override
       public void store(final Camp c) throws RequiredFieldSetToNullException {
         CampSaverTest.this.stored = c;
       }
 
       @Override
       public void deleteCamp(final long id) throws RequiredFieldSetToNullException {
         // do nothing
       }
     };
 
     this.stored = null;
   }
 
   @Test
   public final void testPerformSave_Name() {
     final Camp c = new Camp(this.dummyCamp);
     c.setName("a-name");
 
     save(c);
 
     assertThat(this.stored).isEqualTo(c);
   }
 
   private void save(final Camp c) {
     GuiActionRunner.execute(new GuiTask() {
       @Override
       protected void executeInEDT() throws Throwable {
         editView.setCamp(c);
         CampSaver.performSave(CampSaverTest.this.model, editView, statusView);
       }
     });
   }
 
   @Test
   public final void testPerformSave_Location() {
     final Camp c = new Camp(this.dummyCamp);
     c.setLocation("Nordpol");
 
     save(c);
 
     assertThat(this.stored).isEqualTo(c);
   }
 
   @Test
   public final void testPerformSave_FromDate() {
     final Camp c = new Camp(this.dummyCamp);
     c.setFromDate(new Date(12000000000l));
 
     save(c);
 
     assertThat(this.stored).isEqualTo(c);
   }
 
   @Test
   public final void testPerformSave_UntilDate() {
     final Camp c = new Camp(this.dummyCamp);
     c.setUntilDate(new Date(12000000000l));
 
     save(c);
 
     assertThat(this.stored).isEqualTo(c);
   }
 
   @Test
   public final void testPerformSave_RatePerParticipant() {
     final Camp c = new Camp(this.dummyCamp);
     c.setRatePerParticipant("RATE");
 
     save(c);
 
     assertThat(this.stored).isEqualTo(c);
   }
 
   @Test
   public final void testPerformSave_RatePerDay() {
     final Camp c = new Camp(this.dummyCamp);
     c.setRatePerDayChildren("daily rate");
 
     save(c);
 
     assertThat(this.stored).isEqualTo(c);
   }
 
   @Test
   public final void testPerformSave_Participants() {
     final Camp c = new Camp(this.dummyCamp);
     final Participant participant = new Participant("",
                                                     "",
                                                     Gender.FEMALE,
                                                     Denomination.ORTHODOX,
                                                     new Date(82800000),
                                                     "",
                                                     0,
                                                     "",
                                                     CountyCouncil.COUNTY_KUSEL);
 
     c.addParticipant(new CampParticipant(participant));
 
     save(c);
 
     assertThat(this.stored).isEqualTo(c);
   }
 }
