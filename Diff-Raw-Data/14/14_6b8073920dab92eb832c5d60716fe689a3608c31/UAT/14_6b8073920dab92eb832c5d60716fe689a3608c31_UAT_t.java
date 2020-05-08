 package net.baremodels.uat;
 
 import net.baremodels.model.Model;
 import net.baremodels.models.ModelFactory;
 import net.baremodels.text.FakeUser;
 import net.baremodels.text.TextRunner;
 import net.baremodels.text.TextUiState;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * A user acceptance test.
  */
 public final class UAT {
 
     final FakeUser user = new FakeUser() {
         @Override
         public Model pickModelFrom(TextUiState state) {
             UAT.this.state = state;
             return null;
         }
     };
     final ModelFactory modelFactory = ModelFactory.DEFAULT;
     final TextRunner runner = new TextRunner(user);
 
     TextUiState state;
     Object showing;
 
     /**
      * Show an initial object.
      */
     public void show(Object object) {
         showing = object;
         Model model = modelFactory.of(showing);
         runner.setModel(model,x->x==model);
     }
 
     /**
      * Select the given object.
      * This will fail if the given object is not visible.
      */
     public void select(Object object) {
         verifyShowing();
         if (isSelectable(object)) {
            show(object);
             return;
         }
         throw new IllegalStateException(String.format("[%s] is not on screen",object));
     }
 
     private boolean isSelectable(Object object) {
         Model objectModel = modelFactory.of(object);
         for (Model model : state.models) {
             if (model.equals(objectModel)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Assert that the screen contains the given value.
      */
     public void assertScreenContains(String value) {
         verifyShowing();
 
         assertTrue(value + " not found in " + state.text,screenContains(value));
     }
 
     public boolean screenContains(String text) {
         verifyShowing();
         return state.text.contains(text);
     }
 
     private void verifyShowing() {
         if (showing==null) {
             throw new IllegalStateException("This method is only valid after showing an object");
         }
     }
 }
