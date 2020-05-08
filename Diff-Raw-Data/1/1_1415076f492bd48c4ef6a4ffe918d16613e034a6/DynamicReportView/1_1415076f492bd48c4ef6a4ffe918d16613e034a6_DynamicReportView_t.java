 package com.mpower.view;
 
 import java.awt.Color;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.Query;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 
 import net.sf.jasperreports.engine.JRExporter;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.export.JRHtmlExporter;
 import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
 
 import org.springframework.ui.jasperreports.JasperReportsUtils;
 import org.springframework.web.servlet.view.AbstractView;
 
 import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
 import com.mpower.domain.ReportField;
 import com.mpower.domain.ReportWizard;
 import com.mpower.domain.ReportFieldType;
 import com.mpower.service.ReportCustomFilterDefinitionService;
 import com.mpower.service.ReportFieldService;
 import com.mpower.util.ReportGenerator;
 
 import ar.com.fdvs.dj.core.DynamicJasperHelper;
 import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
 import ar.com.fdvs.dj.domain.AutoText;
 import ar.com.fdvs.dj.domain.ColumnsGroupVariableOperation;
 import ar.com.fdvs.dj.domain.DJQuery;
 import ar.com.fdvs.dj.domain.DynamicReport;
 import ar.com.fdvs.dj.domain.ImageBanner;
 import ar.com.fdvs.dj.domain.Style;
 import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
 import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
 import ar.com.fdvs.dj.domain.constants.Border;
 import ar.com.fdvs.dj.domain.constants.Font;
 import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
 import ar.com.fdvs.dj.domain.constants.Transparency;
 import ar.com.fdvs.dj.domain.constants.VerticalAlign;
 import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
 
 
 //import com.mpower.service.ReportWizardService;
 
 public class DynamicReportView extends AbstractView {
 	private DataSource jdbcDataSource;
 	private static final int OUTPUT_BYTE_ARRAY_INITIAL_SIZE = 4096;
 	private ReportWizard wiz;
 	ReportGenerator reportGenerator;
 	
 	
 
 	private ReportFieldService reportFieldService;
 	private ReportCustomFilterDefinitionService reportCustomFilterDefinitionService;
 	
 	public DynamicReportView() {
 		// TODO Auto-generated constructor stub
 	}
 
 	private JRExporter createExporter(HttpServletRequest request) {
 		JRHtmlExporter exporter = new JRHtmlExporter();
 
 		exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
 				Boolean.TRUE);
 		String realPath = request.getRealPath("images/report/");
 		exporter
 				.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, realPath);
 		exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
 				"/clementine/images/report/");
 		return exporter;
 	}
 
 	public DataSource getJdbcDataSource() {
 		return jdbcDataSource;
 	}
 
 	public ReportFieldService getReportFieldService() {
 		return reportFieldService;
 	}
 
 	public ReportWizard getReportWizard() {
 		return wiz;
 	}
 
 	@SuppressWarnings( { "deprecation", "unchecked" })
 	@Override
 	protected void renderMergedOutputModel(Map model,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		//
 		// Render the jasper report
 		//
 		@SuppressWarnings("unused")
 		Map params = new HashMap();
 		
 
 
 
 		DynamicReport dr = reportGenerator.Generate(wiz, jdbcDataSource, reportFieldService, reportCustomFilterDefinitionService);
 		String query = dr.getQuery().getText();
 		
 		//
 		// execute the query and pass it to generateJasperPrint
 		Connection connection = jdbcDataSource.getConnection();
 		Statement statement = connection.createStatement();
 		
 		File tempFile = File.createTempFile("wiz", ".jrxml");
 		DynamicJasperHelper.generateJRXML(dr,new ClassicLayoutManager(), params, null, tempFile.getPath());
 
 		//
 		// save the report to the server
 		reportGenerator.put(ResourceDescriptor.TYPE_REPORTUNIT, tempFile.getName(), tempFile.getName(), tempFile.getName(), "/Reports/Clementine/Temp", tempFile,reportGenerator.getParams(), "");
 
 		//
 		// redirect the user to the report on the jasper server
 		
 //		JasperPrint jp = reportGenerator.runReport("/Reports/Clementine/Temp/" + tempFile.getName(), new HashMap());
 //		JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr,
 //new ClassicLayoutManager(),  resultset);
 		tempFile.delete();
 		
 //		JRExporter exporter = createExporter(request);
 
 		// Apply the content type as specified - we don't need an encoding here.
 //		response.setContentType(getContentType());
 
 		// Render report into local OutputStream.
 		// IE workaround: write into byte array first.
 //		ByteArrayOutputStream baos = new ByteArrayOutputStream(
 //				OUTPUT_BYTE_ARRAY_INITIAL_SIZE);
 //		JasperReportsUtils.render(exporter, jp, baos);
 
 		// Write content length (determined via byte array).
 //		response.setContentLength(baos.size());
 
 		// Flush byte array to servlet output stream.
 //		ServletOutputStream out = response.getOutputStream();
 //		baos.writeTo(out);
 //		out.flush();
 
 	}
 	
 	private void initStyles() {
 
 	}
 
 	public void setJdbcDataSource(DataSource jdbcDataSource) {
 		this.jdbcDataSource = jdbcDataSource;
 	}
 
 	public void setReportFieldService(ReportFieldService reportFieldService) {
 		this.reportFieldService = reportFieldService;
 	}
 
 	public void setReportCustomFilterDefinitionService(
 			ReportCustomFilterDefinitionService reportCustomFilterDefinitionService) {
 		this.reportCustomFilterDefinitionService = reportCustomFilterDefinitionService;
 	}
 	
 	public void setReportWizard(ReportWizard wiz) {
 		this.wiz = wiz;
 	}
 
 	public ReportGenerator getReportGenerator() {
 		return reportGenerator;
 	}
 
 	public void setReportGenerator(ReportGenerator reportGenerator) {
 		this.reportGenerator = reportGenerator;
 	}
 
 
 }
