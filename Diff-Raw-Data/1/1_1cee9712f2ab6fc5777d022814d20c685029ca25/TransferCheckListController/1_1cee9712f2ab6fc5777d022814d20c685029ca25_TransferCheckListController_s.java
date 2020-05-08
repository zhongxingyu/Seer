 package org.openmrs.module.dataintegrity.web.controller;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.Module;
 import org.openmrs.module.ModuleException;
 import org.openmrs.module.ModuleFactory;
 import org.openmrs.module.ModuleUtil;
 import org.openmrs.module.dataintegrity.DataIntegrityCheckTemplate;
 import org.openmrs.module.dataintegrity.DataIntegrityService;
 import org.openmrs.module.dataintegrity.DataIntegrityXmlFileParser;
 import org.openmrs.module.dataintegrity.IDataIntegrityCheckUpload;
 import org.openmrs.module.dataintegrity.IntegrityCheckUtil;
 import org.openmrs.web.WebConstants;
 import org.openmrs.web.WebUtil;
 import org.springframework.context.support.MessageSourceAccessor;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 import org.springframework.web.servlet.view.RedirectView;
 
 public class TransferCheckListController extends SimpleFormController {
 	private DataIntegrityService getDataIntegrityService() {
         return (DataIntegrityService)Context.getService(DataIntegrityService.class);
     }
 	
 	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
 		return "not used";
     }
 	
 	@Override
 	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
 		Map<String, Object> map = new HashMap<String, Object>();
 		if (Context.isAuthenticated()) {
 			map.put("existingChecks", getDataIntegrityService().getAllDataIntegrityCheckTemplates());
 		}
         return map; //return all existing integrity checks
 	}
 	
 	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
             BindException errors) throws Exception {
 		HttpSession httpSession = request.getSession(); 
 		MessageSourceAccessor msa = getMessageSourceAccessor();
 		String success = "";
 		String error = "";
 		String view = getFormView();
 		String[] checkList = request.getParameterValues("integrityCheckId"); //Get the list of integrity check IDs
 		if (checkList == null) { //when uploading checkList = null
 			if (Context.isAuthenticated() && request instanceof MultipartHttpServletRequest) {
 				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
 				MultipartFile multipartCheckFile = multipartRequest.getFile("checkFile");
 				if (multipartCheckFile != null && !multipartCheckFile.isEmpty()) {
 					String filename = WebUtil.stripFilename(multipartCheckFile.getOriginalFilename());
 					if (filename.toLowerCase().endsWith("xml")) {
 						InputStream inputStream = null;
 						File checkFile = null;
 						try {
 							inputStream = multipartCheckFile.getInputStream();
 							checkFile = IntegrityCheckUtil.uploadIntegrityCheckFile(inputStream, filename);
 							DataIntegrityXmlFileParser fileParser = new DataIntegrityXmlFileParser(checkFile);
 							List<IDataIntegrityCheckUpload> checksToUpload = fileParser.getChecksToAdd();
 							for (int i=0; i<checksToUpload.size(); i++) {
 								IDataIntegrityCheckUpload check = checksToUpload.get(i);
 								DataIntegrityCheckTemplate template = new DataIntegrityCheckTemplate();
 								template.setIntegrityCheckType(check.getCheckType());
 								template.setIntegrityCheckCode(check.getCheckCode());
 								template.setIntegrityCheckFailDirective(check.getCheckFailDirective());
 								template.setIntegrityCheckFailDirectiveOperator(check.getCheckFailDirectiveOperator());
 								template.setIntegrityCheckName(check.getCheckName());
 								template.setIntegrityCheckRepairDirective(check.getCheckRepairDirective());
 								template.setIntegrityCheckResultType(check.getCheckResultType());
 								template.setIntegrityCheckRepairType(check.getCheckRepairType());
 								template.setIntegrityCheckParameters(check.getCheckParameters());
 								getDataIntegrityService().saveDataIntegrityCheckTemplate(template);
 								success += check.getCheckName() + " " + msa.getMessage("dataintegrity.upload.success") + "<br />";
 							}
 						}
 						catch (Exception e) {
 							error = msa.getMessage("dataintegrity.upload.fail") + ". Message: " + e.getMessage();
 							if (checkFile != null) {
 								checkFile.delete();
 							}
 						}
 						finally {
 							// clean up the check repository folder
 							try {
 								if (inputStream != null)
 									inputStream.close();
 							}
 							catch (IOException io) {
 							}
 						}
 					} else {
 						error = msa.getMessage("dataintegrity.upload.xml");
 					}
 				}
 			}
 		} else {
 			//exporting integrity checks to a XML file 
 			try {
 				DataIntegrityService service = getDataIntegrityService();
 				File exportFile = IntegrityCheckUtil.getExportIntegrityCheckFile();
 				FileWriter fstream = new FileWriter(exportFile);
 		        BufferedWriter out = new BufferedWriter(fstream);
 				StringBuffer exportString = new StringBuffer();
 				exportString.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<checks>\r\n");
 				for (String checkId : checkList) {
 					DataIntegrityCheckTemplate template = service.getDataIntegrityCheckTemplate(Integer.valueOf(checkId));
 					exportString.append("\t<check type=\"" + template.getIntegrityCheckType() + "\">\r\n");
					exportString.append("\t\t<id>" + template.getIntegrityCheckId() + "</id>\r\n");
 					exportString.append("\t\t<name>" + template.getIntegrityCheckName() + "</name>\r\n");
 					exportString.append("\t\t<code>" + template.getIntegrityCheckCode() + "</code>\r\n");
 					exportString.append("\t\t<resultType>" + template.getIntegrityCheckResultType() + "</resultType>\r\n");
 					exportString.append("\t\t<fail operator=\"" + template.getIntegrityCheckFailDirectiveOperator() + "\">" + template.getIntegrityCheckFailDirective() + "</fail>\r\n");
 					if (!template.getIntegrityCheckRepairType().equals("none")) {
 						exportString.append("\t\t<repair type=\"" + template.getIntegrityCheckRepairType() + "\">" + template.getIntegrityCheckRepairDirective() + "</repair>\r\n");
 					}
 					if (!template.getIntegrityCheckParameters().equals("")) {
 						exportString.append("\t\t<parameters>" + template.getIntegrityCheckParameters() + "</parameters>\r\n");
 					}
 					exportString.append("\t</check>\r\n");
 				}
 				exportString.append("</checks>\r\n");
 				out.write(exportString.toString());
 				out.close();
 				
 				//Zip the file
 				File zipFile = zipExportFile(exportFile);
 				//Downloading the file
 				FileInputStream fileToDownload = new FileInputStream(zipFile);
 				ServletOutputStream output = response.getOutputStream();
 				response.setContentType("application/octet-stream");
 				response.setHeader("Content-Disposition", "attachment; filename=IntegrityChecks.zip");
 				response.setContentLength(fileToDownload.available());
 				int c;
 				while ((c = fileToDownload.read()) != -1)
 				{
 					output.write(c);
 				}
 				output.flush();
 				output.close();
 				fileToDownload.close();
 				zipFile.delete();
 				exportFile.delete();
 			} catch (Exception e) {
 				error = msa.getMessage("dataintegrity.upload.export.error") + ". Message: " + e.getMessage();
 			}
 		}
 		view = getSuccessView();
 		if (!success.equals(""))
 			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
 		
 		if (!error.equals(""))
 			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
 		return new ModelAndView(new RedirectView(view));
 	}
 	
 	private File zipExportFile(File file) throws Exception {
 	    byte[] buf = new byte[1024];
 	    
 	    try {
 	    	File zipFile = IntegrityCheckUtil.getZippedExportIntegrityCheckFile();
 	        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
 	    
 	        // Compress the files
             FileInputStream in = new FileInputStream(file);
     
             // Add ZIP entry to output stream.
             out.putNextEntry(new ZipEntry(file.getName()));
     
             // Transfer bytes from the file to the ZIP file
             int len;
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
     
             // Complete the entry
             out.closeEntry();
             in.close();
 	    
 	        // Complete the ZIP file
 	        out.close();
 	        return zipFile;
 	    } catch (Exception e) {
 	    	throw new Exception("Failed to zip file. " + e.getMessage());
 	    }
 		
 
 	}
 
 }
