 package net.paissad.waqtsalat.ui.util;
 
 import java.util.Locale;
 
 import net.paissad.waqtsalat.ui.WaqtSalatUIPlugin;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.graphics.Image;
 
 public class WaqtSalatUIHelper {
 
     private WaqtSalatUIHelper() {
     }
 
     public static Image getFlagForCountryCode(final String countryCode) {
         ImageRegistry imageRegistry = WaqtSalatUIPlugin.getImageRegistry();
        String key = "__flag__" + countryCode; //$NON-NLS-1$
         if (imageRegistry.get(key) == null) {
             String code = (countryCode == null || countryCode.trim().isEmpty()) ? "-" : countryCode.toLowerCase(Locale.ENGLISH); //$NON-NLS-1$
             ImageDescriptor imageDescriptor = WaqtSalatUIPlugin
                     .getImageDescriptor("icons/custom/flags/" + code + ".gif"); //$NON-NLS-1$ //$NON-NLS-2$
             Image flagImage = imageDescriptor.createImage();
             imageRegistry.put(key, flagImage);
         }
         return imageRegistry.get(key);
     }
 }
