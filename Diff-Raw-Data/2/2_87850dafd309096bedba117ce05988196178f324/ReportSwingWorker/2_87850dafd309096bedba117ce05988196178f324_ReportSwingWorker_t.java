 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.utils.jasperreports;
 
 import net.sf.jasperreports.engine.JRDataSource;
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.util.JRLoader;
 
 import java.awt.Frame;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.tools.BrowserLauncher;
 
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public class ReportSwingWorker extends SwingWorker<Boolean, Object> {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ReportSwingWorker.class);
 
     //~ Instance fields --------------------------------------------------------
 
 // private Collection<CidsBean> cidsBeans;
 // private String compiledReport;
     private final List<Collection<CidsBean>> cidsBeansList;
     private final List<String> compiledReportList;
     private final ReportSwingWorkerDialog dialog;
     private final boolean withDialog;
     private String directory;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ReportSwingWorker object.
      *
      * @param  cidsBeansList       map DOCUMENT ME!
      * @param  compiledReportList  DOCUMENT ME!
      * @param  directory           DOCUMENT ME!
      */
     public ReportSwingWorker(final List<Collection<CidsBean>> cidsBeansList,
             final List<String> compiledReportList,
             final String directory) {
         this(cidsBeansList, compiledReportList, false, null, directory);
     }
 
     /**
      * Creates a new ReportSwingWorker object.
      *
      * @param  cidsBeans       DOCUMENT ME!
      * @param  compiledReport  DOCUMENT ME!
      * @param  directory       DOCUMENT ME!
      */
     public ReportSwingWorker(final Collection<CidsBean> cidsBeans,
             final String compiledReport,
             final String directory) {
         this(cidsBeans, compiledReport, false, null, directory);
     }
 
     /**
      * Creates a new ReportSwingWorker object.
      *
      * @param  cidsBeansList       DOCUMENT ME!
      * @param  compiledReportList  DOCUMENT ME!
      * @param  parent              DOCUMENT ME!
      * @param  directory           DOCUMENT ME!
      */
     public ReportSwingWorker(final List<Collection<CidsBean>> cidsBeansList,
             final List<String> compiledReportList,
             final Frame parent,
             final String directory) {
         this(cidsBeansList, compiledReportList, true, parent, directory);
     }
 
     /**
      * Creates a new ReportSwingWorker object.
      *
      * @param  cidsBeans       DOCUMENT ME!
      * @param  compiledReport  DOCUMENT ME!
      * @param  parent          DOCUMENT ME!
      * @param  directory       DOCUMENT ME!
      */
     public ReportSwingWorker(final Collection<CidsBean> cidsBeans,
             final String compiledReport,
             final Frame parent,
             final String directory) {
         this(cidsBeans, compiledReport, true, parent, directory);
     }
 
     /**
      * Creates a new ReportSwingWorker object.
      *
      * @param  cidsBeansList       DOCUMENT ME!
      * @param  compiledReportList  DOCUMENT ME!
      * @param  withDialog          DOCUMENT ME!
      * @param  parent              DOCUMENT ME!
      * @param  directory           DOCUMENT ME!
      */
     public ReportSwingWorker(final List<Collection<CidsBean>> cidsBeansList,
             final List<String> compiledReportList,
             final boolean withDialog,
             final Frame parent,
             final String directory) {
         this.cidsBeansList = cidsBeansList;
         this.compiledReportList = compiledReportList;
         this.withDialog = withDialog;
         this.directory = directory;
         if (withDialog) {
             dialog = new ReportSwingWorkerDialog(parent, true);
         } else {
             dialog = null;
         }
     }
 
     /**
      * Creates a new ReportSwingWorker object.
      *
      * @param  cidsBeans       DOCUMENT ME!
      * @param  compiledReport  DOCUMENT ME!
      * @param  withDialog      DOCUMENT ME!
      * @param  parent          DOCUMENT ME!
      * @param  directory       DOCUMENT ME!
      */
     public ReportSwingWorker(final Collection<CidsBean> cidsBeans,
             final String compiledReport,
             final boolean withDialog,
             final Frame parent,
             final String directory) {
         this.cidsBeansList = new ArrayList<Collection<CidsBean>>();
         this.cidsBeansList.add(cidsBeans);
         this.compiledReportList = new ArrayList<String>();
         this.compiledReportList.add(compiledReport);
         this.withDialog = withDialog;
         this.directory = directory;
         if (withDialog) {
             dialog = new ReportSwingWorkerDialog(parent, true);
         } else {
             dialog = null;
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     @Override
     protected Boolean doInBackground() throws Exception {
         if (withDialog) {
             SwingUtilities.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         StaticSwingTools.showDialog(dialog);
                     }
                 });
         }
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
 
         FileOutputStream fos = null;
         try {
             final List<InputStream> ins = new ArrayList<InputStream>();
             for (int index = 0; index < compiledReportList.size(); index++) {
                 final String report = compiledReportList.get(index);
                 final Collection<CidsBean> beans = cidsBeansList.get(index);
 
                 // report holen
                 final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(ReportSwingWorker.class
                                 .getResourceAsStream(report));
                 // daten vorbereiten
                 final JRDataSource dataSource = new CidsBeanDataSource(beans);
                 // print aus report und daten erzeugen
                 final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap(), dataSource);
                 // quer- bzw hochformat übernehmen
                 jasperPrint.setOrientation(jasperReport.getOrientation());
 
                 // zum pdfStream exportieren und der streamliste hinzufügen
                 final ByteArrayOutputStream outTmp = new ByteArrayOutputStream();
                 JasperExportManager.exportReportToPdfStream(jasperPrint, outTmp);
                 ins.add(new ByteArrayInputStream(outTmp.toByteArray()));
                 outTmp.close();
             }
             // pdfStreams zu einem einzelnen pdfStream zusammenfügen
             ReportHelper.concatPDFs(ins, out, true);
 
             // zusammengefügten pdfStream in Datei schreiben
             File file = new File(directory, "report.pdf");
             int index = 0;
 
             while (file.exists()) {
                 file = new File(directory, "report" + (++index) + ".pdf");
             }
 
             file.getParentFile().mkdirs();
             fos = new FileOutputStream(file);
             fos.write(out.toByteArray());
 
             // Datei über Browser öffnen
            BrowserLauncher.openURL(file.toURI().toURL().toString());
             return true;
         } catch (IOException ex) {
             LOG.error("Export to PDF-Stream failed.", ex);
         } catch (JRException ex) {
             LOG.error("Export to PDF-Stream failed.", ex);
         } finally {
             try {
                 if (out != null) {
                     out.close();
                 }
                 if (fos != null) {
                     fos.close();
                 }
             } catch (IOException ex) {
                 LOG.error("error while closing streams", ex);
             }
         }
 
         return false;
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     protected void done() {
         boolean error = false;
         try {
             error = !get();
         } catch (InterruptedException ex) {
             // unterbrochen, nichts tun
         } catch (ExecutionException ex) {
             error = true;
             LOG.error("error while generating report", ex);
         }
         if (withDialog) {
             dialog.setVisible(false);
         }
         if (error) {
             JOptionPane.showMessageDialog(
                 dialog.getParent(),
                 "Beim Generieren des Reports ist ein Fehler aufgetreten.",
                 "Fehler!",
                 JOptionPane.ERROR_MESSAGE);
         }
     }
 }
