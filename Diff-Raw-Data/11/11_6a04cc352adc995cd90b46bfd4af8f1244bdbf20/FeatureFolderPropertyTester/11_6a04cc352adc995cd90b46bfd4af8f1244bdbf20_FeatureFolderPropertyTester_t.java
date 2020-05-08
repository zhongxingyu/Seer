 package com.technophobia.substeps.junit.launcher.model;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IPackageFragment;
 
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
             final IFolder folder = asFolder(receiver);
             if (folder != null) {
                 return predicate.forModel(folder);
             }
         }
         return false;
     }
 
 
     private IFolder asFolder(final Object receiver) {
         if (receiver instanceof IFolder) {
             return (IFolder) receiver;
        } else if (receiver instanceof IPackageFragment) {
            return asFolder(((IPackageFragment) receiver).getResource());
         } else if (receiver instanceof IAdaptable) {
             return (IFolder) ((IAdaptable) receiver).getAdapter(IFolder.class);
         }
 
         FeatureEditorPlugin.instance().error(
                 "Unable to convert object " + receiver + " of type " + receiver.getClass().getName() + " to type "
                         + IFolder.class.getName() + ". Returning null");
         return null;
     }
 }
