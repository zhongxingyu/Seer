 package org.sukrupa.bigneeds;
 
 import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
 import org.eclipse.jetty.io.View;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.sukrupa.app.needs.BigNeedsController;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.CoreMatchers.any;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.junit.Assert.assertFalse;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.sukrupa.platform.hamcrest.CollectionMatchers.hasEntry;
 
 @SuppressWarnings("unchecked")
 public class BigNeedsControllerTest {
 
     private BigNeedsController controller;
 
     private HashMap<String, Object> model = new HashMap<String, Object>();
 
     @Mock
     private BigNeedRepository bigNeedRepository;
 
     @Before
     public void setUp() {
         initMocks(this);
         controller = new BigNeedsController(bigNeedRepository);
     }
 
     @Test
     public void shouldDisplayBigNeedsPage() {
         assertThat(controller.list(model), is("bigNeeds/list"));
     }
 
     @Test
     public void shouldRetrieveBigNeedListToModel() {
         List<BigNeed> bigNeedList = mock(List.class);
         when(bigNeedRepository.getList()).thenReturn(bigNeedList);
 
         String view = controller.list(model);
 
         Assert.assertThat(view, is("bigNeeds/list"));
         assertThat(model, hasEntry("bigNeedList", bigNeedList));
     }
 
     @Test
     public void shouldDisplayEditForm() {
         List<BigNeed> bigNeedList = mock(List.class);
         when(bigNeedRepository.getList()).thenReturn(bigNeedList);
 
         String view = controller.list(model);
 
         Assert.assertThat(view, is("bigNeeds/list"));
         assertThat(model, hasEntry("bigNeedList", bigNeedList));
     }
 
     @Test
     public void shouldCreateABigNeed() {
         ArgumentCaptor<BigNeed> bigNeedCaptor = ArgumentCaptor.forClass(BigNeed.class);
 
         String view = controller.create("sample", "60000", model);
 
         assertThat(view, is("redirect:/bigneeds"));
         assertThat((String) model.get("message"), is("Added Successfully"));
         verify(bigNeedRepository).put(bigNeedCaptor.capture());
         assertThat(bigNeedCaptor.getValue().getItemName(), is("sample"));
         assertThat(bigNeedCaptor.getValue().getCost(), is(60000));
     }
 
     @Test
     public void shouldDeleteABigNeed(){
         BigNeed bigNeed = mock(BigNeed.class);
         when(bigNeedRepository.getBigNeed(123)).thenReturn(bigNeed);
         when(bigNeed.getItemName()).thenReturn("Banana");
 
         String view = controller.delete(123, model);
 
         assertThat(view, is("redirect:/bigneeds"));
         assertThat(model, hasEntry("message", "Banana was deleted"));
         verify(bigNeedRepository).delete(bigNeed);
     }
 
    @Test
    public void shouldSaveAnEditedBigNeed(){
        BigNeed bigNeed = mock(BigNeed.class);
        when(bigNeedRepository.getBigNeed(123)).thenReturn(bigNeed);
        when(bigNeed.getItemName()).thenReturn("Banana");

        String view = controller.saveEdit(123, "Forks" , "9001" , model);

        assertThat(view, is("redirect:/bigneeds"));
        assertThat(model, hasEntry("message", "Saved changes to Forks"));
        //verify(bigNeedRepository).save(bigNeed);
    }
 }
