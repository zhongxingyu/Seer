 /*
  * Created on 8 Jan 2008
  */
 package uk.org.ponder.rsf.test.selection;
 
 import java.util.Arrays;
 
 import uk.org.ponder.conversion.GeneralLeafParser;
 import uk.org.ponder.conversion.VectorCapableParser;
 import uk.org.ponder.rsf.bare.ActionResponse;
 import uk.org.ponder.rsf.bare.RenderResponse;
 import uk.org.ponder.rsf.bare.RequestLauncher;
 import uk.org.ponder.rsf.bare.junit.MultipleRSFTests;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.test.selection.params.CategoryViewParams;
 import uk.org.ponder.rsf.test.selection.params.MultipleViewParams;
 import uk.org.ponder.rsf.test.selection.producers.TestMultipleProducer;
 import uk.org.ponder.rsf.test.selection.producers.TestNullProducer;
 
 /**
  * Test for general operation of EL context, including fieldGetter, IDDefunnelingReshaper,
  * UISelect edge cases
  */
 
 public class TestSelection extends MultipleRSFTests {
 
   public TestSelection() {
     contributeRequestConfigLocation("classpath:uk/org/ponder/rsf/test/selection/selection-request-context.xml");
     contributeConfigLocation("classpath:uk/org/ponder/rsf/test/selection/selection-application-context.xml");
   }
 
   private void setCategorySize(RequestLauncher launch, int size) {
     CategoriesAll all = (CategoriesAll) launch.getRSACBeanLocator().getBeanLocator()
         .locateBean("&categories-all");
     all.setSize(1);
   }
 
   private void testOneSelection(int initsize, Integer initselection, int userindex,
       boolean nullview) {
     String viewID = nullview ? TestNullProducer.VIEW_ID
         : RequestLauncher.TEST_VIEW;
     RequestLauncher launch1 = getRequestLauncher();
     setCategorySize(launch1, initsize);
     RenderResponse response = launch1.renderView(new CategoryViewParams(viewID,
         initselection));
 
     UIForm form = (UIForm) response.viewWrapper.queryComponent(new UIForm());
     UISelect selection = (UISelect) response.viewWrapper.queryComponent(new UISelect());
 
     String userselection = selection.optionlist.getValue()[userindex];
     if (userselection == null) {
       // we need to fake up this natural operation of the renderer here
       userselection = GeneralLeafParser.NULL_STRING;
     }
 
     selection.selection.updateValue(userselection);
 
     ActionResponse response2 = getRequestLauncher().submitForm(form, null);
     Recipe recipe = (Recipe) response2.requestContext.locateBean("recipe");
 
     assertNoActionError(response2);
     
     if (userselection.equals(GeneralLeafParser.NULL_STRING)) {
       // if the user made the null selection, the effect will be to fetch the recipe,
       // but the nested category will remain null
       assertNotNull(recipe);
       assertNull(recipe.category);
     }
     else {
 
       boolean differ = !(initselection == null && userselection == null)
           && (initselection == null ^ userselection == null)
           || !userselection.equals(initselection.toString());
       if (differ) {
         assertEquals(recipe.category.id, userselection);
         assertNotNull(recipe.category.name);
       }
       else {
         assertNull(recipe);
       }
     }
   }
 
 
   private VectorCapableParser vcp;
 
   public void onSetUp() throws Exception {
     super.onSetUp();
     vcp = (VectorCapableParser) applicationContext.getBean("vectorCapableParser");
   }
 
   public void testOneMultipleSelection(int initsize, Integer[] initselection,
       int[] userindexes, boolean primitive) {
     RequestLauncher launch1 = getRequestLauncher();
     RenderResponse response = launch1.renderView(new MultipleViewParams(
         TestMultipleProducer.VIEW_ID, initsize, initselection, primitive));
 
     UIForm form = (UIForm) response.viewWrapper.queryComponent(new UIForm());
     UISelect selection = (UISelect) response.viewWrapper.queryComponent(new UISelect());
 
     String[] userselection = new String[userindexes.length];
     String[] optionlist = selection.optionlist.getValue();
     for (int i = 0; i < userselection.length; ++ i) {
       userselection[i] = optionlist[userindexes[i]];
       // matches fixer code in HTMLRenderer
       if (userselection[i] == null) {
         userselection[i] = GeneralLeafParser.NULL_STRING;
       }
     }
     
     selection.selection.updateValue(userselection);
 
     ActionResponse response2 = getRequestLauncher().submitForm(form, null);
     
     assertNoActionError(response2);
     
     IntBean intBean = (IntBean) response2.requestContext.locateBean("intBean");
 
     Integer[] converted = new Integer[userselection.length];
     vcp.parse(userselection, converted, null);
     boolean unchanged = Arrays.equals(initselection, converted);
 
     if (primitive) {
       if (unchanged) {
         assertNull(intBean.primitive);
       }
       else {
         assertNotNull(intBean.primitive);
         for (int i = 0; i < userselection.length; ++i) {
           assertEquals(intBean.primitive[i], Integer.parseInt(userselection[i]));
         }
       }
     }
     else {
       if (unchanged) {
         assertNull(intBean.reference);
       }
       else {
         assertNotNull(intBean.reference);
         for (int i = 0; i < userselection.length; ++i) {
           if (userselection[i].equals(GeneralLeafParser.NULL_STRING)) {
             assertNull(intBean.reference[i]);
           }
           else {
             assertEquals(intBean.reference[i], new Integer(userselection[i]));
           }
         }
       }
     }
   }
 
   public void testMultiple() {
    // test primitive list
    testOneMultipleSelection(3, new Integer[] { new Integer(1) }, new int[] {0, 1},
        true);
     // test empty list with empty selection
     testOneMultipleSelection(0, new Integer[] {}, new int [] {}, false);
     // unchanged list with null entry
     testOneMultipleSelection(3, new Integer[] { new Integer(1) , null}, new int[] {1, 2},
         false);
     // test list with null entry
     testOneMultipleSelection(3, new Integer[] { new Integer(1) }, new int[] {1, 2}
        , false);
     // test unchanged list
     testOneMultipleSelection(3, new Integer[] { new Integer(1) }, new int[] {1},
         false);
     testOneMultipleSelection(3, new Integer[] { new Integer(1) }, new int[] {0, 1},
         false);
   }
 
   public void testSelection() {
     testOneSelection(3, new Integer(1), 2, true);
 
     testOneSelection(1, null, 0, false);
     testOneSelection(1, new Integer(1), 0, false);
 
     testOneSelection(3, null, 0, true);
     testOneSelection(3, new Integer(1), 0, true);
 
   }
 
 }
