 package de.hub.clickwatch.transformationLauncher.propertyTester;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jdt.internal.core.CompilationUnit;
 import org.eclipse.ui.IEditorInput;
 
 public class HasTransformationMain extends PropertyTester {
 
 	@Override
 	public boolean test(Object receiver, String property, Object[] args,
 			Object expectedValue) {
 		Boolean ret = false;
 
		if (receiver instanceof CompilationUnit) {
 			ret = true;					
 		} else if (receiver instanceof IFile) {			
 			if (((IFile) receiver).getName().endsWith(".xtend"))
 				ret = true;
 		} else if (receiver instanceof IEditorInput) {
 			ret = true;
 		}
 		System.out.println(receiver);
 
 		return ret;
 	}
 
 }
