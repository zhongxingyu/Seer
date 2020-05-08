 package com.woreports.jasper;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Map;
 
 import net.sf.jasperreports.engine.JRDataSource;
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JRExporter;
 import net.sf.jasperreports.engine.JRExporterParameter;
 import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRVirtualizer;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.export.JRXlsExporter;
 import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
 import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.UnhandledException;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.webobjects.eocontrol.EOEditingContext;
 import com.webobjects.eocontrol.EOQualifier;
 import com.webobjects.eocontrol.EOSortOrdering;
 import com.webobjects.foundation.NSArray;
 import com.webobjects.foundation.NSMutableArray;
 import com.woreports.api.AbstractReportProcessor;
 import com.woreports.api.Format;
 import com.woreports.api.ReportModel;
 import com.woreports.api.ReportProcessingException;
 
 /**
  * @author <a href="mailto:alexandre.parreira@doit.com.br">Alexandre
  *         Parreira</a>
  */
 public class JasperReportProcessorForTemplate extends AbstractReportProcessor {
     private final Provider<EOEditingContext> editingContextProvider;
 
     @Inject
     public JasperReportProcessorForTemplate(Provider<EOEditingContext> editingContextProvider) {
         super();
 
         this.editingContextProvider = editingContextProvider;
     }
 
     @Override
     protected byte[] handleProcessing(Format format, ReportModel model, Map<String, Object> parameters, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) throws ReportProcessingException {
         byte[] data = null;
 
         JasperPrint print = null;
 
         URL url = model.templateLocation();
 
         if (url == null) {
             return null;
         }
 
         try {
             JasperReport jasperReport = (JasperReport) JRLoader.loadObject(url);
 
             JRField[] fields = jasperReport.getFields();
 
             NSMutableArray<String> keypaths = new NSMutableArray<String>();
 
             for (JRField field : fields) {
                 keypaths.add(field.getName());
             }
 
             JRDataSource dataSource = new JasperEofDataSource(editingContextProvider.get(), model.baseEntity().name(), keypaths, qualifier, model.sortOrderings().arrayByAddingObjectsFromArray(sortOrderings));
 
            JRSwapFile swapFile = new JRSwapFile("/tmp", 1024, 1024);

            JRVirtualizer virtualizer = new JRSwapFileVirtualizer(2, swapFile, true);

            parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

             print = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
 
             if (Format.XLS.equals(format)) {
                 JRExporter exporter = new JRXlsExporter();
 
                 exporter.setParameter(JRXlsExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
                 exporter.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, Boolean.TRUE);
                 exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
                 exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
                 exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
                 exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
                 exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
                 exporter.setParameter(JRXlsExporterParameter.IS_IGNORE_GRAPHICS, Boolean.TRUE);
                 exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
                 exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
 
                 try {
                     File file = File.createTempFile("report", ".xls");
 
                     exporter.setParameter(JRExporterParameter.OUTPUT_FILE, file);
 
                     exporter.exportReport();
 
                     return FileUtils.readFileToByteArray(file);
                 } catch (IOException exception) {
                     throw new UnhandledException(exception);
                 }
             }
 
             data = JasperExportManager.exportReportToPdf(print);
 
         } catch (JRException exception) {
             throw new ReportProcessingException(exception);
         }
 
         return data;
     }
 
     @Override
     protected byte[] handleProcessing(Format format, ReportModel model, Map<String, Object> parameters, JRDataSource dataSource) throws ReportProcessingException {
         byte[] data = null;
 
         JasperPrint print = null;
 
         URL url = model.templateLocation();
 
         if (url == null) {
             return null;
         }
 
         try {
             JasperReport jasperReport = (JasperReport) JRLoader.loadObject(url);
 
             print = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
 
             data = JasperExportManager.exportReportToPdf(print);
 
         } catch (JRException exception) {
             throw new ReportProcessingException(exception);
         }
 
         return data;
     }
 }
