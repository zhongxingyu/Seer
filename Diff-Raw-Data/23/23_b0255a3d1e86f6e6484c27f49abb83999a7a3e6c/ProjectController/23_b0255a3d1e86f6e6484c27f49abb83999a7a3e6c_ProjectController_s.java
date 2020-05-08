 /**
  *
  * @author sandy
  */
 package controller;
 
 import helper.general.BinderHelper;
 import helper.json.JSONException;
 import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
 import sands.dao.interfaces.ProjectDAO;
 import model.Project;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 import org.springframework.web.servlet.ModelAndView;
 import helper.json.JSONObject;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import javax.servlet.ServletOutputStream;
 import net.sf.jasperreports.engine.JRExporter;
 import net.sf.jasperreports.engine.JRExporterParameter;
 import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
 import net.sf.jasperreports.engine.export.JRPdfExporter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import sands.dao.implementors.ProjectFinancialDAOImpl;
 
 @Controller
 @RequestMapping(value = "/project")
 public class ProjectController extends BinderHelper {
 
     private ProjectDAO projectDAO;
     public static String TITLE = "Title";
 
     @Autowired
     public void setProjectDAO(ProjectDAO projectDAO) {
         this.projectDAO = projectDAO;
     }
 
     @RequestMapping(value = "/json", method = RequestMethod.POST)
     public void json(@RequestParam("limit") int limit,
             @RequestParam("start") int start,
             @RequestParam("sort") String sort,
             @RequestParam("dir") String dir,
             @ModelAttribute("Project") Project project, BindingResult result, HttpServletResponse response) throws JSONException, IOException {
         HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);
 
         ArrayList countProject = (ArrayList) (List) projectDAO.list(0).get(1);
         Integer count = (Integer) countProject.get(0);
 
         //limit
         projectDAO.setMaxResults(limit);
 
         //order by
         projectDAO.orderBy(sort, dir);
 
         //list
         ArrayList listProject = (ArrayList) (List) projectDAO.list(start).get(0);
 
         JSONObject json = new JSONObject();
         json.put("total", count);
         json.put("success", true);
         Iterator iterator = listProject.iterator();
 
         while (iterator.hasNext()) {
             Project p = (Project) iterator.next();
             JSONObject map = new JSONObject();
         
             Double financialValue;
             String financialSum = (String) ProjectFinancialDAOImpl.getSumByProjectId(p.getId()).get(0);
             if (financialSum != null) {
                 financialValue = Math.ceil((Double.parseDouble(financialSum) / p.getProjectValue()) * 100);
             } else {
                 financialValue = Math.ceil(0.0);
             }
 
             map.put("id", p.getId());
             map.put("name", p.getName());
             map.put("project_end_date", p.getProjectEndDate());
             map.put("project_start_date", p.getProjectStartDate());
             map.put("financial", financialValue);
             map.put("project_contract", p.getProjectContract());
             map.put("project_technical", p.getProjectTechnical());
             map.put("project_resource", p.getProjectResource());
             map.put("project_customer", p.getProjectCustomer());
             map.put("project_schedule", p.getProjectSchedule());
             map.put("account_manager", p.getAccountManager());
             map.put("project_manager", p.getProjectManager());
             map.put("project_value", p.getProjectValue());
             json.append("data", map);
            System.gc();
            //map = null;
            //financialValue = null;
         }
 
         Writer w = json.write(responseWrapper.getWriter());
         json = null;
         w.flush();
         w.close();
     }
 /*
     protected void finalize() {
         Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, "Final!");
     }
 */
     @RequestMapping(value = "/list")
     public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception {
         return new ModelAndView("project/projectList");
     }
 
     @RequestMapping(value = "/add/{id}", method = RequestMethod.POST)
     public void add(@PathVariable("id") long project_id, HttpServletResponse response) throws JSONException, IOException {
         HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);
         Project project = projectDAO.getById(project_id);
 
         JSONObject json = new JSONObject();
         json.put("total", 1);
         json.put("success", true);
         JSONObject map = new JSONObject();
         map.put("id", project.getId());
         map.put("name", project.getName());
         map.put("project_end_date", project.getProjectEndDate());
         map.put("project_start_date", project.getProjectStartDate());
         map.put("financial", project.getProjectFinancial());
         map.put("project_contract", project.getProjectContract());
         map.put("project_technical", project.getProjectTechnical());
         map.put("project_resource", project.getProjectResource());
         map.put("project_customer", project.getProjectCustomer());
         map.put("project_schedule", project.getProjectSchedule());
         map.put("account_manager", project.getAccountManager());
         map.put("project_manager", project.getProjectManager());
         map.put("project_value", project.getProjectValue());
         json.append("data", map);
         Writer w = json.write(responseWrapper.getWriter());
         w.flush();
         w.close();
 
     }
 
     @RequestMapping(value = "/addProcess", method = RequestMethod.POST)
     public void addProcess(@ModelAttribute("Project") Project project,
             BindingResult result,
             @RequestParam("id") String project_id,
             HttpServletResponse response) throws Exception {
 
         if (project_id != null && project_id.isEmpty() == false) {
             Long id = Long.parseLong(project_id);
             project.setId(id);
             projectDAO.update(project);
 
             /*
             Email email = new Email();
             email.setFrom("sandyharyono@yahoo.com");
             email.setMessage("test");
             email.setRecipients(new String[] {"sandyharyono@gmail.com"});
             email.setSubject("test");
             email.send();
              */
         } else {
             projectDAO.save(project);
         }
         Writer out = response.getWriter();
         out.write("{success:true}");
 
     }
 
     @RequestMapping(value = "/delete", method = RequestMethod.POST)
     public void delete(@RequestParam("id") String project_id, HttpServletResponse response)
             throws Exception {
 
         Writer out = response.getWriter();
         if (project_id.isEmpty()) {
             out.write("{success:false}");
             return;
         }
 
         String[] project_ids = project_id.split(",");
         for (int i = 0; i < project_ids.length; i++) {
             Project p = new Project();
             p.setId(Long.parseLong(project_ids[i]));
             this.projectDAO.delete(p);
         }
         out.write("{success:true}");
     }
 
     @RequestMapping(value = "/report")
     public String report(HttpServletRequest request, HttpServletResponse response) throws Exception {
 
         String FileName = "project";
         HashMap hashTitle = new HashMap();
         hashTitle.put(TITLE, "Test Report");
 
         File file = new File("projectreport.jrxml");
         String reportFileName = JasperCompileManager.compileReportToFile(request.getRealPath("WEB-INF/reports") + "/" + file);
 
         projectDAO.setMaxResults(0);
         List listUser = (List) projectDAO.list(0).get(0);
 
         JRBeanArrayDataSource dataSource = new JRBeanArrayDataSource(listUser.toArray());
         JasperPrint report = JasperFillManager.fillReport(reportFileName, hashTitle, dataSource);
 
 
         JRExporter exporter = new JRPdfExporter();
         exporter.setParameters(new HashMap());
         exporter.setParameter(JRExporterParameter.JASPER_PRINT, report);
         JasperExportManager.exportReportToPdfFile(report, request.getRealPath("WEB-INF/reports") + "/" + FileName + ".pdf");
 
         FileInputStream fileInputStream = new FileInputStream(new File(request.getRealPath("WEB-INF/reports") + "/" + FileName + ".pdf"));
         ServletOutputStream outputStream = response.getOutputStream();
 
         response.setContentType("application/pdf");
         response.setHeader("Content-Disposition", "attachment;filename=" + FileName + ".pdf");
 
         byte[] buffer = new byte[1024];
         int n = 0;
         while ((n = fileInputStream.read(buffer)) != -1) {
             outputStream.write(buffer, 0, n);
         }
         outputStream.flush();
         fileInputStream.close();
         outputStream.close();
 
         //back to original
         projectDAO.setMaxResults(10);
         return "project/projectReport";
 
     }
 }
