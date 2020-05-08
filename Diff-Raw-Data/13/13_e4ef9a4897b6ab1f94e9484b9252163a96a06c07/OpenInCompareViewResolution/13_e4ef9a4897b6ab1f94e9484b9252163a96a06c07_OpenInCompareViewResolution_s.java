 package com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.marker.quickfix;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareNavigator;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.compare.ICompareNavigator;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IMarkerResolution2;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.DatenmodellGeneratorVisualizerPlugin;
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.diff.compare.CompareInput;
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.dto.Complement;
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.job.ResourceWorker;
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.marker.MarkerFactory;
 
 public class OpenInCompareViewResolution implements IMarkerResolution2 {
 
     private static final Image image = 
             AbstractUIPlugin
                 .imageDescriptorFromPlugin(
                         DatenmodellGeneratorVisualizerPlugin
                             .getInstance()
                             .getBundle()
                             .getSymbolicName(), 
                         "icons/fontAwesome/columns.png").createImage();
     
     private static final String label = "Open Complement in a Compare View"; 
     private static final String description = "Opens a compare view. " +
             "On the left side the source file will be compared with the " +
             "generated original file on the right side."; 
 
     @Override
     public void run(IMarker marker) {
         IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
         String filePath = marker.getAttribute(MarkerFactory.MARKER_ATTR_GEN_FILE_URL, null);
         
         if (filePath != null) {
             if (marker.getResource() instanceof IFile) {
                 Complement toCompare = 
                         ResourceWorker
                             .findGeneratedComplement(
                                     (IFile) marker.getResource(), 
                                     marker.getResource().getProject());
                 
                 CompareInput input = new CompareInput(toCompare, getCompareConfig(), "Complement Compare View");
                 CompareUI.openCompareEditorOnPage(input, page);
 
                 CompareNavigator navigator = (CompareNavigator) input.getAdapter(ICompareNavigator.class);
                 
                 int indexDiff = Integer.valueOf(marker.getAttribute(MarkerFactory.MARKER_ATTR_DIFF_INDEX, null));
                 // I can't see a simpler way in order to select diff no. X
                 while (indexDiff > 0) {
                     navigator.selectChange(true);
                     indexDiff--;
                 }
                 
                 // it is possible to search (and replace)
                 // that's cool! But useless at the moment.
 //                IFindReplaceTarget finder = (IFindReplaceTarget) input.getAdapter(IFindReplaceTarget.class);
 //                finder.findAndSelect(0, "last line", true, false, true);
             }
         }
 
     }
 
     private CompareConfiguration getCompareConfig() {
         CompareConfiguration cc = new CompareConfiguration();
         cc.setLeftEditable(true);
         cc.setLeftLabel("Source file");
         cc.setRightLabel("Fully generated file");
        cc.setProperty(CompareConfiguration.IGNORE_WHITESPACE, true);
         return cc;
     }
 
     @Override
     public String getDescription() {
         return description;
     }
 
     @Override
     public Image getImage() {
         return image;
     }
 
     @Override
     public String getLabel() {
         return label;
     }
 
 }
