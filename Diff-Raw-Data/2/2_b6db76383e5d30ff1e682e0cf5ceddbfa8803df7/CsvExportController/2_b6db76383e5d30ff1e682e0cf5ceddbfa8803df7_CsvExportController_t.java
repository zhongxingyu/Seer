 package com.orangeleap.tangerine.controller.importexport;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.context.ApplicationContext;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 import com.orangeleap.tangerine.controller.importexport.exporters.EntityExporter;
 import com.orangeleap.tangerine.controller.importexport.exporters.EntityExporterFactory;
 
 public class CsvExportController extends SimpleFormController {
 
     protected final Log logger = LogFactory.getLog(getClass());
     
     
     @Override
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
         binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("MM/dd/yyyy"), true)); // TODO: custom date format
     }
 
     @Override
     protected Object formBackingObject(HttpServletRequest request) throws ServletException {
         return new ExportRequest();
     }
     
     private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
     private static Date FUTURE_DATE;
     private static Date PAST_DATE;
     static {
     	try {
     		FUTURE_DATE = sdf.parse("1/1/2100");
     		PAST_DATE = sdf.parse("1/1/1900");
     	} catch (Exception e) {}
     }
     private static final String LOW_ID = new String("0");
     private static final String HIGH_ID = new String("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
 
 	@Override
 	protected ModelAndView processFormSubmission(
 			HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
 			throws Exception {
 
 
 		
 		ExportRequest er = (ExportRequest)command;
 		if (er.getFromDate() == null) er.setFromDate(PAST_DATE);
 		if (er.getToDate() == null) er.setToDate(FUTURE_DATE);
 		if (StringUtils.trimToNull(er.getFromId()) == null) er.setFromId(LOW_ID);
 		if (StringUtils.trimToNull(er.getToId()) == null) er.setToId(HIGH_ID);
 		
 		if (!CsvImportController.importexportAllowed(request)) {
             return null;  // For security only, unauthorized users will not have the menu option to even get here normally.
         }
 		
 		try {
 			String exportData = getExport(er);
 			response.setContentType("application/x-download"); 
 			String entity = er.getEntity();
 			if (entity.equals("person")) entity = "constituent";
 			response.setHeader("Content-Disposition", "attachment; filename=" + entity + "-export.csv");
 			response.setContentLength(exportData.length());
 			PrintWriter out = response.getWriter();
 			out.print(exportData);
 			out.flush();
 			return null;
 		} catch (Exception e) {
			logger.debug(e);
			e.printStackTrace();
 			ModelAndView mav = new ModelAndView("redirect:/importexport.htm");
 			mav.addObject("exportmessage", e.getMessage());
 			return mav;
 		}
 
 	}
 
 	private String getExport(ExportRequest er) {
 
 
 		StringWriter sw = new StringWriter();
 		CSVWriter writer = new CSVWriter(sw);
 		
 		ApplicationContext applicationContext = getApplicationContext();
 
 		EntityExporter ex = new EntityExporterFactory().getEntityExporter(er, applicationContext);
 		if (ex == null) return "";
 		
 		List<List<String>> data = ex.exportAll();
 		List<String[]> csvdata = new ArrayList<String[]>();
 		for (List<String> line:data) {
 			String[] aline = line.toArray(new String[line.size()]);
 			csvdata.add(aline);
 		}
 
 		writer.writeAll(csvdata);
 
 		return sw.toString();
 	}
 
 }
