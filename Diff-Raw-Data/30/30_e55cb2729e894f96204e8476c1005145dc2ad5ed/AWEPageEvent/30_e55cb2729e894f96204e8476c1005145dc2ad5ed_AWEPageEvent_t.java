 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.report.pdf;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.amanzi.awe.report.ReportPlugin;
import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 
 import com.lowagie.text.BadElementException;
 import com.lowagie.text.Document;
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.Image;
 import com.lowagie.text.pdf.PdfContentByte;
 import com.lowagie.text.pdf.PdfPageEventHelper;
 import com.lowagie.text.pdf.PdfWriter;
 
 /**
  * TODO Purpose of
  * <p>
  * </p>
  * 
  * @author Pechko_E
  * @since 1.0.0
  */
 public class AWEPageEvent extends PdfPageEventHelper {
    private static final Logger LOGGER = Logger.getLogger(AWEPageEvent.class);
     private Image logo;
 
     public AWEPageEvent() {
          final URL entry = Platform.getBundle(ReportPlugin.PLUGIN_ID).getEntry("icons/amanzi_tel_logo.png");
          String file;
         try {
             file = FileLocator.resolve(entry).getFile();
             logo=Image.getInstance(file);
             logo.setAbsolutePosition(0, 0);
        } catch (Exception e) {
           LOGGER.error(e);
         }
            
        
     }
 
     @Override
     public void onEndPage(PdfWriter writer, Document document) {
         PdfContentByte contentUnder = writer.getDirectContentUnder();
         try {
             contentUnder.addImage(logo);
        } catch (Exception e) {
            LOGGER.error(e);
         }
     }
 
 }
