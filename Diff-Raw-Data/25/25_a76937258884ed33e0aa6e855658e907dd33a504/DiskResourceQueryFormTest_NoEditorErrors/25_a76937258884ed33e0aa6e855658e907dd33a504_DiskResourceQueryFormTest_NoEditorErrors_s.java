 package org.iplantc.de.diskResource.client.search.views.cells;
 
 import org.iplantc.de.client.models.search.DateInterval;
 import org.iplantc.de.client.models.search.DiskResourceQueryTemplate;
 import org.iplantc.de.diskResource.client.search.events.SubmitDiskResourceQueryEvent;
 
 import com.google.gwt.editor.client.SimpleBeanEditorDriver;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.user.client.Element;
 import com.google.gwtmockito.GwtMockito;
 import com.google.gwtmockito.GxtMockitoTestRunner;
 import com.google.gwtmockito.fakes.FakeSimpleBeanEditorDriverProvider;
 
 import com.sencha.gxt.core.client.Style.AnchorAlignment;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.verify;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * This test verifies the functionality of the {@link org.iplantc.de.diskResource.client.search.views.cells.DiskResourceQueryForm} class when there are no
  * editor errors.
  * 
  * @author jstroot
  * 
  */
 @RunWith(GxtMockitoTestRunner.class)
 public class DiskResourceQueryFormTest_NoEditorErrors {
 
    @Mock
    DiskResourceQueryFormNamePrompt namePrompt;
 
     @Mock DiskResourceQueryTemplate mockedTemplate;
 
     private DiskResourceQueryForm form;
 
     @Before public void setUp() {
         GwtMockito.useProviderForType(SimpleBeanEditorDriver.class, new FakeSimpleBeanEditorDriverProvider(false));
         form = new DiskResourceQueryForm(mockedTemplate) {
 
             @Override
             DateInterval createDateInterval(Date from, Date to, String label) {
                 DateInterval ret = mock(DateInterval.class);
                 ret.setFrom(from);
                 ret.setTo(to);
                 ret.setLabel(label);
                 return ret;
             }
 
             @Override
             List<FileSizeUnit> createFileSizeUnits() {
                 return Lists.newArrayList();
             }
         };
         form.namePrompt = namePrompt;
     }
     
     @Test public void testDiskResourceQueryFormInit() {
         DiskResourceQueryForm spy = spy(form);
 
         spy.init(namePrompt);
         /* Verify that the class under test adds itself as a save DRQE handler */
         verify(namePrompt).addSaveDiskResourceQueryEventHandler(eq(spy));
     }
 
     /**
      * Verify the following when {@link DiskResourceQueryForm#createFilterLink} is selected;<br/>
      */
     @Test public void testOnCreateQueryTemplateClicked_noErrors() {
         form.onCreateQueryTemplateClicked(mock(ClickEvent.class));
 
         // Verify that the name prompt is shown
         verify(namePrompt).show(any(DiskResourceQueryTemplate.class), any(Element.class), any(AnchorAlignment.class));
     }
 
     /**
      * Verify the following when {@link DiskResourceQueryForm#searchButton} is clicked <br/>
      */
     @Test public void testOnSearchBtnSelected_noErrors() {
         DiskResourceQueryForm spy = spy(form);
         spy.onSearchBtnSelected(mock(SelectEvent.class));
 
         // Verify that the appropriate event is fired
         verify(spy).fireEvent(any(SubmitDiskResourceQueryEvent.class));
 
         // Verify that the form is hidden
         verify(spy).hide();
     }
 
 }
