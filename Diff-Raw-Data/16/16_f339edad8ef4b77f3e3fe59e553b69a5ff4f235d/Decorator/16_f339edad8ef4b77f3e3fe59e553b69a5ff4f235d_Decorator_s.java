 package ca.ubc.cs.testcaseview10.decorator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
 import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.viewers.IDecoration;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.ui.PlatformUI;
 
 import ca.ubc.cs.testcaseview10.SelectionView;
import ca.ubc.cs.testcaseview10.marker.SampleMarker;
import ca.ubc.cs.testcaseview10.marker.SampleMarker10;
import ca.ubc.cs.testcaseview10.marker.SampleMarker2;
import ca.ubc.cs.testcaseview10.marker.SampleMarker20;
 
 public class Decorator implements ILightweightLabelDecorator{
 
 	@Override
 	public void addListener(ILabelProviderListener listener) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean isLabelProperty(Object element, String property) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void removeListener(ILabelProviderListener listener) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	SelectionView sv = new SelectionView();
 
 	@Override
 	public void decorate(Object element, IDecoration decoration) {
 		// TODO Auto-generated method stub
 		//decoration.addOverlay(ImageDescriptor.createFromFile(Decorator.class, 
 		//		"/icons/sample.gif"), IDecoration.TOP_RIGHT);
 		//decoration.addPrefix("My Prefix ");
 		//decoration.addSuffix(" My Suffix");
 		if (element instanceof IProject) {
 			if (((IProject) element).isOpen()) {
 				RGB rgb = new RGB(255, 200, 200);
 				Color color = new Color(PlatformUI.getWorkbench().getDisplay(), rgb);
 				decoration.setBackgroundColor(color);
 			}
 		}
 		if (element instanceof IFile) {
 			IFile file = (IFile)element;
 			try {
 				ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
 				if (unit != null) {					
 					RGB rgb = null;
 					sv.getOneMethodICompilationUnitInfo(unit, false);
 					int count = sv.largest();
 					if (count == 0) {
 						rgb = new RGB(255, 255, 0);
 					} else if (count == 1) {
 						rgb = new RGB(255, 255, 200);						
 					} else if (count == 2) {
 						rgb = new RGB(0,255,0);
 					} else {
 						rgb = new RGB(200, 255, 200);												
 					}
 					Color color = new Color(PlatformUI.getWorkbench().getDisplay(), rgb);
 					decoration.setBackgroundColor(color);
 					
 					//file.findMarkers(IM, includeSubtypes, depth);
 					// set for next survey.
 					sv.setCurrentProject(unit.getJavaProject().getElementName());
 				}
 			} catch (IllegalArgumentException e) {
 				// ignore
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		
 		/*
 		IStructuredSelection selection = 
 				  (IStructuredSelection) viewer.getSelection();
 				Person p = (Person) selection.getFirstElement();
 		*/
 	}
 }
