 package net.codjo.referential.gui;
 
 import net.codjo.mad.gui.framework.DefaultGuiContext;
 import net.codjo.mad.gui.request.DetailDataSource;
 import net.codjo.mad.gui.request.Preference;
 import net.codjo.referential.gui.api.Field;
 import net.codjo.referential.gui.api.Referential;
import static net.codjo.test.common.matcher.JUnitMatchers.*;
import javax.swing.JInternalFrame;
 import org.junit.Test;
 import org.uispec4j.Window;
 
 public class ReferentialDetailWindowBuilderTest {
 
     private Preference createPreference(Class detailWindowClass) {
         Preference preference = new Preference();
         preference.setDetailWindowClass(detailWindowClass);
         return preference;
     }
 
 
     private Referential createReferential(Field field) {
         Referential referential = new Referential();
         referential.addField(field);
         return referential;
     }
 
 
     private Field createField(String label) {
         Field field = new Field();
         field.setLabel(label);
         field.setType("string");
         return field;
     }
 
 
     @Test
     public void test_buildFrame() throws Exception {
         Referential referential = createReferential(createField("empereur"));
 
         ReferentialDetailWindowBuilder builder =
               new ReferentialDetailWindowBuilder(null, "my title", referential);
 
         Preference preference = createPreference(ReferentialDetailLogic.class);
 
         JInternalFrame internalFrame =
              builder.buildFrame(new DetailDataSource(new DefaultGuiContext()), preference);
 
         Window window = new Window(internalFrame);
         assertThat(window.getTitle(), equalTo("my title"));
         assertThat(window.getTextBox("empereur"), is(notNullValue()));
     }
 
 
     @Test
     public void test_buildFrame_detailClassIsAnInternalFrame() throws Exception {
         Referential referential = createReferential(createField("empereur"));
 
         ReferentialDetailWindowBuilder builder =
               new ReferentialDetailWindowBuilder(null, "my title", referential);
 
         Preference preference = createPreference(ReferentialDetailLogic.class);

         JInternalFrame internalFrame =
              builder.buildFrame(new DetailDataSource(new DefaultGuiContext()), preference);
 
         Window window = new Window(internalFrame);
         assertThat(window.getTitle(), equalTo("my title"));
         assertThat(window.getTextBox("empereur"), is(notNullValue()));
     }
 
 
     @Test
     public void test_buildFrame_specificFrame() throws Exception {
         Referential referential = createReferential(createField("empereur"));
 
         ReferentialDetailWindowBuilder builder =
               new ReferentialDetailWindowBuilder(null, "my title", referential);
 
         Preference preference = createPreference(MySpecificGui.class);
 
         DetailDataSource dataSource = new DetailDataSource(new DefaultGuiContext());
         MySpecificGui gui = (MySpecificGui)builder.buildFrame(dataSource, preference);
 
         assertThat(gui.getReferential(), sameInstance(referential));
         assertThat(gui.getDataSource(), sameInstance(dataSource));
     }
 
 
     public static class MySpecificGui extends JInternalFrame {
         private DetailDataSource dataSource;
         private Referential referential;
 
 
         public MySpecificGui(DetailDataSource dataSource, Referential referential) {
             this.dataSource = dataSource;
             this.referential = referential;
         }
 
 
         public Referential getReferential() {
             return referential;
         }
 
 
         public DetailDataSource getDataSource() {
             return dataSource;
         }
     }
 }
