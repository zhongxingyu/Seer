 package org.jdownloader.container;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import jd.controlling.linkcrawler.CrawledLink;
 import jd.gui.UserIO;
 import jd.plugins.ContainerStatus;
 import jd.plugins.DownloadLink;
 import jd.plugins.PluginsC;
 
 import org.appwork.utils.swing.dialog.Dialog;
 import org.jdownloader.container.sft.FileInfoDialog;
 import org.jdownloader.container.sft.sftBinary;
 import org.jdownloader.container.sft.sftContainer;
 
 public class SFT extends PluginsC {
 
     public SFT() {
        super("SFT", "file://.+\\.sft", "$Revision: 10009 $");
     }
 
     @Override
     public ContainerStatus callDecryption(File file) {
         ContainerStatus cs = new ContainerStatus(file);
 
         try {
             sftContainer container = sftBinary.load(file);
 
             FileInfoDialog dialog = new FileInfoDialog(container);
             dialog.displayDialog();
 
             if ((container.isDecrypted()) && ((dialog.getReturnmask() & Dialog.RETURN_OK) > 0)) {
                 ArrayList<String> linkList = container.getFormatedLinks();
                 for (String element : linkList) {
                     int pos = element.lastIndexOf('/');
                     String name = element.substring(pos).trim();
                     cls.add(new CrawledLink(new DownloadLink(null, name, null, element, false)));
                 }
                 cs.setStatus(cls.size() > 0 ? ContainerStatus.STATUS_FINISHED : ContainerStatus.STATUS_FAILED);
             } else
                 cs.setStatus(ContainerStatus.STATUS_FAILED);
 
         } catch (Exception e) {
             cs.setStatus(ContainerStatus.STATUS_FAILED);
 
             if ((e.getMessage() != null) | (e.getMessage().length() > 0))
                 UserIO.getInstance().requestMessageDialog(e.getMessage());
             else {
                 UserIO.getInstance().requestMessageDialog("sft decrypt error");
             }
         }
 
         return cs;
     }
 
     @Override
     public String[] encrypt(String plain) {
         return null;
     }
 
     // @Override
     public String getCoder() {
         return "Rushh0ur/RR-Member";
     }
 }
