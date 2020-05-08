 package mobi.monaca.framework;
 
 import android.os.Bundle;
 
 /** Internal settings for monaca framework **/
 public class InternalSettings {
     
    public final String DISABLE_UI_CONTAINER_BORDER = "monaca.disableUIContainerBorder";
     public final String FORCE_DISABLE_WEBVIEW_GPU = "monaca.forceDisableWebviewGPU";
     
     public final boolean disableUIContainerBorder;
     public final boolean forceDisableWebviewGPU;
     
     public InternalSettings(Bundle settings) {
         disableUIContainerBorder = settings.containsKey(DISABLE_UI_CONTAINER_BORDER)
                 ? settings.getBoolean(DISABLE_UI_CONTAINER_BORDER)
                 : false;
                 
         forceDisableWebviewGPU = settings.containsKey(FORCE_DISABLE_WEBVIEW_GPU)
                 ? settings.getBoolean(FORCE_DISABLE_WEBVIEW_GPU)
                 : false;
     }
 
 }
