 package org.jenkinsci.plugins.recipe.ingredients;
 
 import hudson.Extension;
 import hudson.model.View;
 import jenkins.model.Jenkins;
 import jenkins.util.xstream.XStreamDOM;
 import org.jenkinsci.plugins.recipe.Ingredient;
 import org.jenkinsci.plugins.recipe.IngredientDescriptor;
 import org.jenkinsci.plugins.recipe.Recipe;
 
 import java.io.IOException;
 
 /**
 * View.
 *
  * @author Kohsuke Kawaguchi
  */
 public class ViewIngredient extends Ingredient {
     private String name;
     private XStreamDOM definition;
 
     public ViewIngredient(String name, XStreamDOM definition) {
         this.name = name;
         this.definition = definition;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public XStreamDOM getDefinition() {
         return definition;
     }
 
     @Override
     public void cook(Recipe recipe) throws IOException {
         XStreamDOM actual = recipe.createImportOptions().apply(definition);
 
        View v = (View)Jenkins.XSTREAM2.unmarshal(actual.newReader());
        Jenkins.getInstance().addView(v);
     }
 
     public static ViewIngredient fromView(View v) {
         XStreamDOM dom = XStreamDOM.from(Jenkins.XSTREAM2,v);
        // TODO: remove View.owner from DOM
         return new ViewIngredient(v.getViewName(),dom);
     }
 
     @Extension
     public static class DescriptorImpl extends IngredientDescriptor {
         @Override
         public String getDisplayName() {
             return "View";
         }
     }
 }
