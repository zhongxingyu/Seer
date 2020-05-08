 package org.iplantc.de.diskResource.client.search.views.cells;
 
 import org.iplantc.de.client.models.search.DateInterval;
 import org.iplantc.de.client.models.search.DiskResourceQueryTemplate;
import org.iplantc.de.client.models.search.FileSizeRange.FileSizeUnit;
 
import com.google.common.collect.Lists;
 import com.google.gwt.editor.client.SimpleBeanEditorDriver;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.user.client.Element;
 import com.google.gwtmockito.GwtMockito;
 import com.google.gwtmockito.GxtMockitoTestRunner;
 import com.google.gwtmockito.fakes.FakeSimpleBeanEditorDriverProvider;
 
 import com.sencha.gxt.core.client.Style.AnchorAlignment;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 
 import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * This test verifies the functionality of the {@link org.iplantc.de.diskResource.client.search.views.cells.DiskResourceQueryForm} class when there are editor
  * errors.
  * 
  * @author jstroot
  * 
  */
 @RunWith(GxtMockitoTestRunner.class)
 public class DiskResourceQueryFormTest_WithEditorErrors {
 
    @Mock DiskResourceQueryFormNamePrompt namePrompt;
 
     @Mock DiskResourceQueryTemplate mockedTemplate;
 
     private DiskResourceQueryForm form;
 
     @Before public void setUp() {
         GwtMockito.useProviderForType(SimpleBeanEditorDriver.class, new FakeSimpleBeanEditorDriverProvider(true));
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
 
     /**
      * Verify the following when {@link DiskResourceQueryForm#createFilterLink} is selected;<br/>
      */
     @Test public void testOnCreateQueryTemplateClicked_withErrors() {
         form.onCreateQueryTemplateClicked(mock(ClickEvent.class));
 
         // Verify that name prompt is not shown
         verify(namePrompt, never()).show(any(DiskResourceQueryTemplate.class), any(Element.class), any(AnchorAlignment.class));
     }
 
     /**
      * Verify the following when {@link DiskResourceQueryForm#searchButton} is clicked;<br/>
      */
     @Test public void testOnSearchBtnSelected_withErrors() {
         DiskResourceQueryForm spy = spy(form);
         spy.onSearchBtnSelected(mock(SelectEvent.class));
 
         // Verify no events are fired
         verify(spy, never()).fireEvent(any(GwtEvent.class));
 
         // Verify that the form is not hidden
         verify(spy, never()).hide();
     }
 }
