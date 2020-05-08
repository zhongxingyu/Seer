 package org.hackystat.sensor.eclipse.addon;
 
 import java.net.URI;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IJavaModelMarker;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.hackystat.sensor.eclipse.EclipseSensor;
 
 /**
  * Provides an approach to find build error using problem markers.
  *  
  * @author Hongbing Kou
  * @version $Id: BuildErrorSensor.java,v 1.1.1.1 2005/10/20 23:56:56 johnson Exp $
  */
 public class BuildErrorSensor {
   /** Eclipse sensor. */
   private EclipseSensor sensor;
   
   /**
    * Constructs build error sensor object.
    * 
    * @param sensor Eclipse sensor.
    */
   public BuildErrorSensor(EclipseSensor sensor) {
     this.sensor = sensor;
   }
   
   /**
    * Installs a problem requestor to the current active editor if the working file is a java file.
    * 
    * @param delta Resouce delta change.
    */
   public void findBuildProblem(IResourceDelta delta) {    
     ITextEditor activeEditor = this.sensor.getActiveTextEditor(); 
     // Do nothing if there is no file edited.
     if (activeEditor == null) {
       return;
     }
       
     if (!(activeEditor.getEditorInput() instanceof IFileEditorInput)) {
       return;
     }
     
     IFileEditorInput fileEditorInput = (IFileEditorInput) activeEditor.getEditorInput();
     // Do nothing if it is not file edit
     if (fileEditorInput == null) {
       return;
     }
     
     // We are interested in java file only.
    IFile file = fileEditorInput.getFile();
    if (file == null) {
      return;
    }
    
    String fileName = file.getLocation().toString();
     if (!fileName.endsWith(".java")) {
       return;
     }
     
     IResource resource = delta.getResource();
     if (resource == null) {
       return;
     }    
     IPath location = resource.getLocation();
     if (location == null) {
       return;
     }    
     String deltaFileName = location.toString();
     if (!fileName.equals(deltaFileName)) {
       return;
     }
     
     URI fileResource = fileEditorInput.getFile().getLocationURI();
     IMarkerDelta markerDeltas[] = delta.getMarkerDeltas();
     if (markerDeltas != null && markerDeltas.length > 0) {
       // Message pool is used to filter out the repeated compilation error.
       HashSet<String> messagePool = new HashSet<String>();
       for (int i = 0; i < markerDeltas.length; i++) {
         IMarkerDelta markerDelta = (IMarkerDelta) markerDeltas[i];
         if (markerDelta.getType() == IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER) {
           String severity = markerDelta.getAttribute("severity").toString();
           String message = markerDelta.getAttribute("message").toString();
           
           // Only error will be processed, warning & info will be ignored.
           if ("2".equals(severity) &&  
               (markerDelta.getKind() == IResourceDelta.ADDED || 
                markerDelta.getKind() == IResourceDelta.CHANGED)) {
             Map<String, String> keyValueMap = new HashMap<String, String>();
             keyValueMap.put("Subtype", "Compile");
             keyValueMap.put("Success", "false");
             keyValueMap.put("Error", message);
             
             StringBuffer displayMessage = new StringBuffer();
             displayMessage.append("Build Error : ")
                           .append(this.sensor.extractFileName(fileResource))
                           .append(" [").append(message).append(']');
             
             String data = fileName + "#" + severity + 
                           "#" + (String) markerDelta.getAttribute("message");
             // Only sends out unrepeated data.
             if (!messagePool.contains(data)) {
               this.sensor.addDevEvent("Build", fileResource, keyValueMap,
                 displayMessage.toString());
               messagePool.add(data);
             }
           }
         }  
       }
     }      
   }
 }
