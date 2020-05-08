 package com.technophobia.substeps.junit.launcher.model;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.core.resources.IFolder;
 
 import com.technophobia.substeps.FeatureEditorPlugin;
 import com.technophobia.substeps.predicate.IsFeatureFolderPredicate;
 import com.technophobia.substeps.supplier.Predicate;
 
 public class FeatureFolderPropertyTester extends PropertyTester {
 
     private static final String PROPERTY_NAME = "isFeatureFolder";
 
     private final Predicate<IFolder> predicate;
 
 
     public FeatureFolderPropertyTester() {
         this(new IsFeatureFolderPredicate(FeatureEditorPlugin.instance().projectManager()));
     }
 
 
     public FeatureFolderPropertyTester(final Predicate<IFolder> predicate) {
         this.predicate = predicate;
     }
 
 
     @Override
     public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
         if (PROPERTY_NAME.equals(property)) {
            return predicate.forModel((IFolder) receiver);
         }
         return false;
     }
 }
