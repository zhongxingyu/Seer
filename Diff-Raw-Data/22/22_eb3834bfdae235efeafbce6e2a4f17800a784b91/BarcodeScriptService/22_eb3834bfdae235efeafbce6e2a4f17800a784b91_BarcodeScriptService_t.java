 package com.celements.photo;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.krysalis.barcode4j.BarcodeException;
 import org.krysalis.barcode4j.BarcodeGenerator;
 import org.krysalis.barcode4j.BarcodeUtil;
 import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
 import org.xml.sax.SAXException;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.script.service.ScriptService;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.web.Utils;
 
 @Component("barcode")
 public class BarcodeScriptService implements ScriptService {
   private String MODULE_WIDTH = "0.33mm";
   private String MODULE_HEIGHT = "15mm";

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      BarcodeScriptService.class);
   
   @Requirement
   Execution execution;
   
   public void generate(String number, OutputStream out) {
     String moduleHeight = getContext().getRequest().get("moduleHeight");
     if((moduleHeight == null) || "".equals(moduleHeight.trim())) {
       moduleHeight = MODULE_HEIGHT;
     }
     String moduleWidth = getContext().getRequest().get("moduleWidth");
     if((moduleWidth == null) || "".equals(moduleWidth.trim())) {
       moduleWidth = MODULE_WIDTH;
     }
     BitmapCanvasProvider provider = new BitmapCanvasProvider(
         out, "image/png", 72, BufferedImage.TYPE_BYTE_GRAY, true, 0);
     DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
     String xmlConf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><barcode><ean8>" +
       "<height>" + moduleHeight + "</height>" +
       "<module-width>" + moduleWidth + "</module-width>" +
       "</ean8></barcode>";
     InputStream in = new ByteArrayInputStream(xmlConf.getBytes());
     try {
       Configuration cfg = builder.build(in);
       in.close();
       BarcodeGenerator gen = BarcodeUtil.getInstance().createBarcodeGenerator(cfg);
       gen.generateBarcode(provider, number);
       provider.finish();
     } catch (ConfigurationException e) {
      LOGGER.error("Exception in configuration for barcode module.", e);
     } catch (SAXException e) {
      LOGGER.error(e);
     } catch (IOException e) {
      LOGGER.error(e);
     } catch (BarcodeException e) {
      LOGGER.error("Exception in barcode module.", e);
     }
   }
   
   private XWikiContext getContext() {
     return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
         "xwikicontext");
   }
 }
