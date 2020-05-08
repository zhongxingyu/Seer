 /*******************************************************************************
  * Copyright (c) 2012 David Harrison, Triptech Ltd.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  *
  * Contributors:
  *     David Harrison, Triptech Ltd - initial API and implementation
  ******************************************************************************/
 package net.triptech.buildulator.web;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import net.triptech.buildulator.DataParser;
 import net.triptech.buildulator.FlashScope;
 import net.triptech.buildulator.model.DataGrid;
 import net.triptech.buildulator.model.MaterialDetail;
 import net.triptech.buildulator.model.MaterialType;
 import net.triptech.buildulator.model.Preferences;
 import net.triptech.buildulator.model.Project;
 import net.triptech.buildulator.model.Person;
 import net.triptech.buildulator.model.UserRole;
 import net.triptech.buildulator.model.bom.BillOfMaterials;
 import net.triptech.buildulator.model.bom.Section;
 import net.triptech.buildulator.model.bom.Element;
 import net.triptech.buildulator.model.bom.Material;
 import net.triptech.buildulator.model.bom.SustainabilitySummary;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * The Class ProjectController.
  */
 @RequestMapping("/projects")
 @Controller
 public class ProjectController extends BaseController {
 
     /** The logger. */
     private static Logger logger = Logger.getLogger(ProjectController.class);
 
 
     /**
      * Index.
      *
      * @param request the request
      * @param allProjectsVal the all projects val
      * @param uiModel the ui model
      * @return the string
      */
     @RequestMapping(method = RequestMethod.GET)
     public String index(final HttpServletRequest request, @RequestParam(
             value = "all", required = false) Boolean allProjectsVal,
             Model uiModel) {
 
         String page = "projects/list";
 
         if (getProjectCount(request) == 0) {
             page = "redirect:/projects/new";
         } else {
             Person user = getUser(request);
 
             Preferences preferences = this.getPreferences(request);
             if (user != null && !user.isTocAccepted() && preferences.isTocEnabled()) {
                 // Redirect to the user details screen as they need to accept the TOC.
                 page = "redirect:/user";
             } else {
                 boolean allProjects = canViewAllProjects(allProjectsVal, user);
                 uiModel.addAttribute("allProjects", allProjects);
             }
         }
         return page;
     }
 
     /**
      * Display the edit project form.
      *
      * @param id the id
      * @param uiModel the ui model
      * @param request the request
      * @param response the response
      * @return the string
      */
     @RequestMapping(value = "/{id}", params = "edit", method = RequestMethod.GET)
     public String editForm(@PathVariable("id") Long id, Model uiModel,
             final HttpServletRequest request, final HttpServletResponse response) {
 
         String page = "projects/edit";
 
         Project project = Project.findProject(id);
 
         if (checkProjectPermission(project, request)) {
             uiModel.addAttribute("project", project);
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
             page = "resourceNotFound";
         }
         return page;
     }
 
     /**
      * Returns the project if the user has the rights to view it.
      * Otherwise a 404 error is returned.
      *
      * @param id the id
      * @param uiModel the ui model
      * @param request the request
      * @param response the response
      * @return the string
      */
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     public String show(@PathVariable("id") Long id, Model uiModel,
             HttpServletRequest request, final HttpServletResponse response) {
 
         String page = "projects/show";
 
         Project project = Project.findProject(id);
 
         if (checkProjectPermission(project, request)) {
             boolean noOperatingEnergyData = false;
             boolean noBOMData = false;
             if (testIfEmptyJson(project.getDataField(Project.OPERATING_ENERGY))) {
                 noOperatingEnergyData = true;
             }
             if (testIfEmptyJson(project.getDataField(Project.CONSTRUCTION))) {
                 noBOMData = true;
             }
 
             uiModel.addAttribute("project", project);
             uiModel.addAttribute("noOperatingEnergyData", noOperatingEnergyData);
             uiModel.addAttribute("noBOMData", noBOMData);
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
             page = "resourceNotFound";
         }
         return page;
     }
 
     /**
      * Returns the shared project. If none is found a 404 error is returned.
      *
      * @param hashId the hash id
      * @param uiModel the ui model
      * @return the string
      */
     @RequestMapping(value = "/share/{hashId}", method = RequestMethod.GET)
     public String share(@PathVariable("hashId") String hashId, Model uiModel) {
 
         String page = "projects/share";
 
         Project project = Project.findProject(hashId);
 
         if (project != null) {
             uiModel.addAttribute("project", project);
         } else {
             page = "resourceNotFound";
         }
         return page;
     }
 
     /**
      * Creates the project.
      *
      * @param definitionForm the definition form
      * @param bindingResult the binding result
      * @param uiModel the ui model
      * @param request the http servlet request
      * @return the string
      */
     @RequestMapping(method = RequestMethod.POST)
     public String create(@Valid Project project,
             @RequestParam(value = "templateId", required = false) Long templateId,
             BindingResult bindingResult, Model uiModel,
             HttpServletRequest request) {
 
         Person user = getUser(request);
 
         if (bindingResult.hasErrors()) {
             uiModel.addAttribute("project", project);
             FlashScope.appendMessage(
                     getMessage("buildulator_object_validation", Project.class), request);
             return "projects/create";
         }
         uiModel.asMap().clear();
 
         if (user == null) {
             String sessionId = request.getSession().getId();
 
             // Set the session id as the owner
             project.setSession(sessionId);
 
             // Store the existing session id in case the user logs in
             request.getSession().setAttribute("PreviousSessionId", sessionId);
         } else {
             // Set the logged in person as the owner
             project.setPerson(user);
         }
 
         // Check the privileges for the template functionality
         if (project.isTemplate() || project.isComparable()) {
             if (!Project.canViewOrEditTemplatesOrComparables(user)) {
                 project.setTemplate(false);
                 project.setComparable(false);
             }
         }
 
         if (templateId != null && templateId > 0) {
             // Try loading the template and assigning the operating energy data
             // to the new project.
             try {
                 Project template = Project.findProject(templateId);
                 if (template != null && template.isTemplate()) {
                     // Set the operating energy data for the new project
                     project.setDataField(Project.OPERATING_ENERGY,
                             template.getDataField(Project.OPERATING_ENERGY));
                 }
             } catch (Exception e) {
                 logger.error("Error loading template (" + templateId + "): "
                         + e.getMessage());
             }
         }
 
         project.persist();
         project.flush();
 
         FlashScope.appendMessage(
                 getMessage("buildulator_create_complete", Project.class), request);
 
         return "redirect:/projects/"
                 + encodeUrlPathSegment(project.getId().toString(), request);
     }
 
     /**
      * Update the project.
      *
      * @param project the project
      * @param bindingResult the binding result
      * @param uiModel the ui model
      * @param request the http servlet request
      * @param response the response
      * @return the string
      */
     @RequestMapping(method = RequestMethod.PUT)
     public String update(@Valid Project project,
             BindingResult bindingResult, Model uiModel,
             HttpServletRequest request, final HttpServletResponse response) {
 
         String page = "resourceNotFound";
 
         Project existingProject = Project.findProject(project.getId());
 
         if (checkProjectPermission(existingProject, request)) {
 
             if (bindingResult.hasErrors()) {
                 uiModel.addAttribute("project", project);
                 FlashScope.appendMessage(
                         getMessage("metahive_object_validation", Project.class), request);
                 page = "projects/edit";
             } else {
                 existingProject.update(project);
 
                 existingProject.persist();
                 existingProject.flush();
                 uiModel.asMap().clear();
 
                 FlashScope.appendMessage(
                         getMessage("buildulator_edit_complete", Project.class), request);
 
                 page = "redirect:/projects/" + encodeUrlPathSegment(
                         existingProject.getId().toString(), request);
             }
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         }
         return page;
     }
 
     /**
      * Clones the project.
      *
      * @param id the id
      * @param name the name
      * @param request the http servlet request
      * @param response the response
      * @return the string
      */
     @RequestMapping(value = "/clone", method = RequestMethod.POST)
     public String clone(
             @RequestParam(value = "projectId", required = false) Long id,
             @RequestParam(value = "projectName", required = false) String name,
             final HttpServletRequest request, final HttpServletResponse response) {
 
         String page = "resourceNotFound";
 
         Project existingProject = null;
 
         if (id != null) {
             existingProject = Project.findProject(id);
         }
 
         if (checkProjectPermission(existingProject, request)) {
 
             Project clonedProject = existingProject.clone();
 
             if (StringUtils.isNotBlank(name)) {
                 clonedProject.setName(name);
             }
             clonedProject.persist();
             clonedProject.flush();
 
             FlashScope.appendMessage(
                     getMessage("buildulator_clone_complete", Project.class), request);
 
             page = "redirect:/projects/" + encodeUrlPathSegment(
                         clonedProject.getId().toString(), request);
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         }
         return page;
     }
 
     /**
      * Delete the project.
      *
      * @param id the id
      * @param request the http servlet request
      * @param response the http servlet response
      * @return the string
      */
     @RequestMapping(value = "/{id}", method = RequestMethod.POST)
     public String delete(@PathVariable("id") Long id,
             final HttpServletRequest request, final HttpServletResponse response) {
 
         String page = "resourceNotFound";
 
         Project project = Project.findProject(id);
 
         if (checkProjectPermission(project, request)) {
             project.remove();
 
             FlashScope.appendMessage(
                     getMessage("buildulator_delete_complete", Project.class), request);
 
             page = "redirect:/projects";
 
             if (getProjectCount(request) == 0) {
                 page = "redirect:/projects/new";
             }
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         }
         return page;
     }
 
     /**
      * Add an item to the project's bill of materials.
      *
      * @param id the id
      * @param sid the sid
      * @param eid the eid
      * @param request the http servlet request
      * @param response the http servlet response
      * @return the string
      */
     @RequestMapping(value = "/{id}/newitem", method = RequestMethod.POST)
     public @ResponseBody String newItem(@PathVariable("id") Long id,
             @RequestParam(value = "type", required = false) String typeVal,
             @RequestParam(value = "sid", required = false) Integer sid,
             @RequestParam(value = "eid", required = false) Integer eid,
             @RequestParam(value = "name", required = false) String name,
             @RequestParam(value = "quantity", required = false) String quantity,
             @RequestParam(value = "units", required = false) String units,
             final HttpServletRequest request, final HttpServletResponse response) {
 
         String returnMessage = "";
 
         Project project = Project.findProject(id);
 
         String type = Project.CONSTRUCTION;
         if (StringUtils.isNotBlank(typeVal)) {
             type = typeVal.toLowerCase();
         }
 
         if (checkProjectPermission(project, request)) {
             returnMessage = newBOMItem(project, type, sid, eid, name, quantity, units);
         } else {
             returnMessage = this.getMessage("projects_bom_projectnotfound");
         }
         if (StringUtils.equals(returnMessage, "ok")) {
             // Return the bill of materials JSON
             returnMessage = BillOfMaterials.parseJson(
                     project.getDataField(type)).toJson();
         } else {
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
         return returnMessage;
     }
 
     /**
      * Edit an item within the project's bill of materials.
      *
      * @param id the id
      * @param sid the sid
      * @param eid the eid
      * @param mid the mid
      * @param request the http servlet request
      * @param response the http servlet response
      * @return the string
      */
     @RequestMapping(value = "/{id}/edititem", method = RequestMethod.POST)
     public @ResponseBody String editItem(@PathVariable("id") Long id,
             @RequestParam(value = "type", required = false) String typeVal,
             @RequestParam(value = "sid", required = false) Integer sid,
             @RequestParam(value = "eid", required = false) Integer eid,
             @RequestParam(value = "mid", required = false) Integer mid,
             @RequestParam(value = "name", required = false) String name,
             @RequestParam(value = "quantity", required = false) String quantity,
             @RequestParam(value = "units", required = false) String units,
             final HttpServletRequest request, final HttpServletResponse response) {
 
         String returnMessage = "";
 
         Project project = Project.findProject(id);
 
         String type = Project.CONSTRUCTION;
         if (StringUtils.isNotBlank(typeVal)) {
             type = typeVal.toLowerCase();
         }
 
         if (checkProjectPermission(project, request)) {
             returnMessage = editBOMItem(project, type, sid, eid, mid,
                     name, quantity, units);
         } else {
             returnMessage = this.getMessage("projects_bom_projectnotfound");
         }
         if (StringUtils.equals(returnMessage, "ok")) {
             // Return the bill of materials JSON
             returnMessage = BillOfMaterials.parseJson(
                     project.getDataField(type)).toJson();
         } else {
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
         return returnMessage;
     }
 
 
     /**
      * Delete an item from the project's bill of materials.
      *
      * @param id the id
      * @param sid the sid
      * @param eid the eid
      * @param mid the mid
      * @param request the http servlet request
      * @param response the http servlet response
      * @return the string
      */
     @RequestMapping(value = "/{id}/deleteitem", method = RequestMethod.POST)
     public @ResponseBody String deleteItem(@PathVariable("id") Long id,
             @RequestParam(value = "type", required = false) String typeVal,
             @RequestParam(value = "sid", required = false) Integer sid,
             @RequestParam(value = "eid", required = false) Integer eid,
             @RequestParam(value = "mid", required = false) Integer mid,
             final HttpServletRequest request, final HttpServletResponse response) {
 
         String returnMessage = "";
 
         Project project = Project.findProject(id);
 
         String type = Project.CONSTRUCTION;
         if (StringUtils.isNotBlank(typeVal)) {
             type = typeVal.toLowerCase();
         }
 
         if (checkProjectPermission(project, request)) {
             returnMessage = deleteBOMItem(project, type, sid, eid, mid);
         } else {
             returnMessage = this.getMessage("projects_bom_projectnotfound");
         }
 
         if (StringUtils.equals(returnMessage, "ok")) {
             // Return the bill of materials JSON
             returnMessage = BillOfMaterials.parseJson(
                     project.getDataField(type)).toJson();
         } else {
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
         return returnMessage;
     }
 
     /**
      * Returns the new project form.
      *
      * @param uiModel the ui model
      * @return the string
      */
     @RequestMapping(value = "/new", method = RequestMethod.GET)
     public String createForm(Model uiModel) {
         uiModel.addAttribute("project", new Project());
         return "projects/create";
     }
 
     /**
      * List the projects.
      *
     * @param allProjects the all projects
      * @param request the request
      * @return the string
      */
     @RequestMapping(value = "/list.json", method = RequestMethod.GET)
     public @ResponseBody String listProjects(
             @RequestParam(value = "all", required = false) Boolean allProjectsVal,
             final HttpServletRequest request) {
 
         List<Project> projects = new ArrayList<Project>();
 
         Person user = getUser(request);
 
         boolean allProjects = canViewAllProjects(allProjectsVal, user);
 
         if (user == null) {
             projects = Project.findAllProjects(request.getSession().getId());
         } else {
             if (allProjects) {
                 projects = Project.findAllProjects();
             } else {
                 projects = Project.findAllProjects(user);
             }
         }
         return Project.toJson(projects, getMessage("projects_list_anonymous"));
     }
 
     /**
      * List the materials.
      *
      * @return the string
      */
     @RequestMapping(value = "/materials.json", method = RequestMethod.GET)
     public @ResponseBody String listMaterials(
             @RequestParam(value = "type", required = true) String type,
             final HttpServletRequest request) {
 
         MaterialType mt = MaterialType.CONSTRUCTION;
 
         if (StringUtils.equalsIgnoreCase(type, Project.OPERATING_ENERGY)) {
             mt = MaterialType.ENERGY_SOURCE;
         }
         return MaterialDetail.toJson(MaterialDetail.findMaterialDetails(mt));
     }
 
 
     /**
      * Returns the project's bill of materials if the user has the rights to view it.
      * Otherwise a 404 error is returned.
      *
      * @param hashId the hash id
      * @param type the type
      * @param request the request
      * @param response the response
      * @return the string
      */
     @RequestMapping(value = "/share/{hashId}/bom.json", method = RequestMethod.GET)
     public @ResponseBody String jsonSharedBOM(@PathVariable("hashId") String hashId,
             @RequestParam(value = "type", required = true) String type,
             HttpServletRequest request, final HttpServletResponse response) {
 
         String result = "";
 
         Project project = Project.findProject(hashId);
 
         if (project != null) {
             BillOfMaterials bom = BillOfMaterials.parseJson(project.getDataField(type));
             result = bom.toJson();
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         }
         return result;
     }
 
 
     /**
      * Returns the project's summary. If no project is found a 404 error is returned.
      *
      * @param hashId the hash id
      * @param request the request
      * @param response the response
      * @return the string
      */
     @RequestMapping(value = "/share/{hashId}/summary.json", method = RequestMethod.GET)
     public @ResponseBody String jsonSharedSummary(@PathVariable("hashId") String hashId,
             HttpServletRequest request, final HttpServletResponse response) {
 
         String result = "";
 
         Project project = Project.findProject(hashId);
 
         if (project != null) {
             SustainabilitySummary ss = SustainabilitySummary.parseJson(
                     project.getDataField(Project.SUMMARY));
             result = ss.toJson();
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         }
         return result;
     }
 
     /**
      * Returns the project's summary if the user has the rights to view it.
      * Otherwise a 404 error is returned.
      *
      * @param id the id
      * @param uiModel the ui model
      * @param request the request
      * @param response the response
      * @return the string
      */
     @RequestMapping(value = "/{id}/summary.json", method = RequestMethod.GET)
     public @ResponseBody String jsonSummary(@PathVariable("id") Long id,
             HttpServletRequest request, final HttpServletResponse response) {
 
         String result = "";
 
         Project project = Project.findProject(id);
 
         if (project != null) {
             if (project.isComparable() || checkProjectPermission(project, request)) {
 
                 SustainabilitySummary ss = SustainabilitySummary.parseJson(
                         project.getDataField(Project.SUMMARY));
                 result = ss.toJson();
             }
         }
 
         if (StringUtils.isBlank(result)) {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         }
         return result;
     }
 
     /**
      * Returns the projects that this project can be compared to based on the user.
      * If the user is not allowed to view this project a 404 error is returned.
      *
      * @param id the id
      * @param uiModel the ui model
      * @param request the request
      * @param response the response
      * @return the string
      */
     @RequestMapping(value = "/share/{hashId}/compare.json", method = RequestMethod.GET)
     public @ResponseBody String jsonCompare(@PathVariable("hashId") String hashId,
             HttpServletRequest request, final HttpServletResponse response) {
 
         String result = "";
 
         Project project = Project.findProject(hashId);
 
         if (project != null) {
 
             Person user = getUser(request);
 
             List<Project> userProjects = new ArrayList<Project>();
 
             if (user == null) {
                 userProjects = Project.findAllProjects(request.getSession().getId());
             } else {
                 userProjects = Project.findAllProjects(user);
             }
             result = Project.toJson(project, userProjects);
 
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         }
         return result;
     }
 
     /**
      * Import the bill of materials.
      *
      * @param id the id
      * @param data the data
      * @param request the request
      * @param response the response
      * @return the string
      */
     @RequestMapping(value = "/{id}/import", method = RequestMethod.POST)
     public String importBOM(
             @PathVariable("id") Long id,
             @RequestParam(value = "bomData", required = true) String data,
             HttpServletRequest request, final HttpServletResponse response)  {
 
         String page = "resourceNotFound";
 
         Project project = Project.findProject(id);
 
         if (checkProjectPermission(project, request)) {
 
             String message = getMessage("projects_bom_import_nodata");
 
             if (StringUtils.isNotBlank(data)) {
                 project.setDataField(Project.CONSTRUCTION,
                         parseBillOfMaterials(data).toJson());
 
                 project.merge();
                 project.flush();
 
                 message = getMessage("projects_bom_import_complete");
             }
             FlashScope.appendMessage(message, request);
 
             page = "redirect:/projects/"
                     + encodeUrlPathSegment(project.getId().toString(), request);
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         }
         return page;
     }
 
     /**
      * Builds the template for importing a QS report.
      *
      * @param request the request
      * @param response the response
      * @return the model and view
      * @throws Exception the exception
      */
     @RequestMapping(value = "/qs-template.xls", method = RequestMethod.GET)
     public ModelAndView buildTemplate(HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         DataGrid dataGrid = new DataGrid();
         dataGrid.setTitle(this.getMessage("projects_bom_import_template_title"));
 
         dataGrid.addHeaderField(this.getMessage(
                 "projects_bom_import_template_item"));
         dataGrid.addHeaderField(this.getMessage(
                 "projects_bom_import_template_quantity"));
         dataGrid.addHeaderField(this.getMessage(
                 "projects_bom_import_template_units"));
 
         return new ModelAndView("ExcelTemplateView", "dataGrid", dataGrid);
     }
 
 
     @ModelAttribute("projectTemplates")
     public final List<Project> getProjectTemplates() {
         return Project.findProjectTemplates();
     }
 
     /**
      * Check the project permissions.
      *
      * @param project the project
      * @param request the request
      * @return true, if successful
      */
     private boolean checkProjectPermission(final Project project,
             final HttpServletRequest request) {
 
         boolean showProject = false;
 
         // Check that the user owns the project
         if (project != null) {
             Person user = getUser(request);
 
             if (user != null) {
                 if (user.getUserRole() != null) {
                     // Administrators can see everything.
                     if (user.getUserRole() == UserRole.ROLE_ADMIN) {
                         showProject = true;
                     }
                     // An editor can view a template project.
                     if (user.getUserRole() == UserRole.ROLE_EDITOR
                             && project.isTemplate()) {
                         showProject = true;
                     }
                 }
                 // If the user owns the project allow them to see it.
                 if (project.getPerson() != null
                         && user.getId() == project.getPerson().getId()) {
                     showProject = true;
                 }
             } else {
                 // If the sessions match allow them to see it.
                 if (StringUtils.equalsIgnoreCase(
                         request.getSession().getId(), project.getSession())) {
                     showProject = true;
                 }
             }
         }
         return showProject;
     }
 
     /**
      * Parses the bill of materials.
      *
      * @param data the data
      * @return the bill of materials
      */
     private BillOfMaterials parseBillOfMaterials(final String data) {
 
         BillOfMaterials bom = new BillOfMaterials();
 
         DataGrid parsedData = new DataGrid(data);
 
         int itemId = 0, quantityId = 0, unitsId = 0, headerCount = 0;
 
         for (String header : parsedData.getHeaderFields()) {
             if (StringUtils.equalsIgnoreCase(header, this.getMessage(
                     "projects_bom_import_template_item"))) {
                 itemId = headerCount;
             }
             if (StringUtils.equalsIgnoreCase(header, this.getMessage(
                     "projects_bom_import_template_quantity"))) {
                 quantityId = headerCount;
             }
             if (StringUtils.equalsIgnoreCase(header, this.getMessage(
                     "projects_bom_import_template_units"))) {
                 unitsId = headerCount;
             }
             headerCount++;
         }
 
         for (List<String> row : parsedData.getRows()) {
 
             String itemName = DataParser.stripHtml(row.get(itemId));
 
             if (StringUtils.isNotBlank(itemName)) {
                 // A field name exists, continue
                 String quantityValue = DataParser.stripHtml(row.get(quantityId));
                 String unitsValue = DataParser.stripHtml(row.get(unitsId));
 
                 if (StringUtils.isNotBlank(quantityValue)) {
                     Element element = new Element();
                     element.setName(itemName);
 
                     try {
                         element.setQuantity(Double.parseDouble(quantityValue));
                     } catch (Exception e) {
                         logger.info("Error parsing the quantity value: "
                                 + e.getMessage());
                         // Append to the unitsValue
                         unitsValue = quantityValue.trim() + " "
                                 + unitsValue.trim();
                     }
 
                     if (StringUtils.isNotBlank(unitsValue)) {
                         element.setUnits(unitsValue.trim());
                     }
 
                     // Add to the current section - add a new section if required
                     if (bom.getSections().size() == 0) {
                         Section section = new Section();
                         section.setName(
                                 getMessage("projects_bom_import_unnamedsection"));
                         bom.addSection(section);
                     }
 
                     bom.getSections().get(bom.getSections().size() - 1)
                             .addElement(element);
 
                 } else {
                     // This is a new section
                     Section section = new Section();
                     section.setName(itemName);
 
                     bom.addSection(section);
                 }
             }
         }
         return bom;
     }
 
 
     /**
      * Adds the item to the bill of materials.
      *
      * @param project the project
      * @param type the type
      * @param sid the sid
      * @param eid the eid
      * @param nameVal the name val
      * @param quantityVal the quantity val
      * @param unitsVal the units val
      * @return the string
      */
     private String newBOMItem(final Project project, final String type,
             final Integer sid, final Integer eid, final String nameVal,
             final String quantityVal, final String unitsVal) {
 
         String returnMessage = this.getMessage("projects_bom_new_cannotadd");
 
         double quantity = 0;
         try {
             quantity = Double.parseDouble(quantityVal);
         } catch (Exception e) {
             // Error casting the quantityVal to a double
         }
 
         BillOfMaterials bom = BillOfMaterials.parseJson(project.getDataField(type));
 
         if (StringUtils.isNotBlank(nameVal)) {
             if (sid != null && sid > 0) {
                 if (bom.getSections().size() >= sid) {
                     if (eid != null && eid > 0) {
                         if (bom.getSections().get(sid - 1).getElements().size() >= eid) {
                             // Add a new material
                             try {
                                 MaterialDetail m = MaterialDetail.findByName(nameVal);
                                 if (m != null) {
                                     Material material = new Material();
 
                                     material.setName(m.getName());
                                     material.setQuantity(quantity);
                                     material.setUnits(m.getUnitOfMeasure());
 
                                     double[] results = m.performCalculations(quantity);
                                     material.setTotalEnergy(results[0]);
                                     material.setTotalCarbon(results[1]);
 
                                     bom.getSections().get(sid - 1).getElements()
                                             .get(eid - 1).addMaterial(material);
 
                                     bom.recalculateTotals();
 
                                     returnMessage = "ok";
                                 }
                             } catch (Exception e) {
                                 logger.error("Could not add material '" + nameVal + "': "
                                         + e.getMessage());
                             }
                         }
                     } else {
                         // Add a new element
                         Element element = new Element();
                         element.setName(DataParser.stripHtml(nameVal));
                         element.setQuantity(quantity);
                         element.setUnits(DataParser.stripHtml(unitsVal));
 
                         bom.getSections().get(sid - 1).addElement(element);
                         returnMessage = "ok";
                     }
                 }
             } else {
                 // Add a new section to the BOM
                 Section section = new Section();
                 section.setName(DataParser.stripHtml(nameVal));
 
                 bom.addSection(section);
                 returnMessage = "ok";
             }
         } else {
             returnMessage = this.getMessage("projects_bom_new_noname");
         }
 
         if (StringUtils.equals(returnMessage, "ok")) {
             try {
                 project.setDataField(type, bom.toJson());
                 project.merge();
                 project.flush();
             } catch (Exception e) {
                 returnMessage = this.getMessage("projects_bom_new_error");
             }
         }
         return returnMessage;
     }
 
 
     /**
      * Edits an item within the project's bill of materials.
      *
      * @param project the project
      * @param type the type
      * @param sid the sid
      * @param eid the eid
      * @param mid the mid
      * @param nameVal the name val
      * @param quantityVal the quantity val
      * @param unitsVal the units val
      * @return the string
      */
     private String editBOMItem(final Project project, final String type,
             final Integer sid, final Integer eid, final Integer mid,
             final String nameVal, final String quantityVal, final String unitsVal) {
 
         String returnMessage = this.getMessage("projects_bom_edit_cannotedit");
 
         double quantity = 0;
         try {
             quantity = Double.parseDouble(quantityVal);
         } catch (Exception e) {
             // Error casting the quantityVal to a double
         }
 
         BillOfMaterials bom = BillOfMaterials.parseJson(project.getDataField(type));
 
         if (sid != null && sid > 0) {
             if (bom.getSections().size() >= sid) {
                 Section section = bom.getSections().get(sid - 1);
 
                 if (eid != null && eid > 0) {
                     if (section.getElements().size() >= eid) {
                         Element element = section.getElements().get(eid - 1);
 
                         if (mid != null && mid > 0) {
                             if (element.getMaterials().size() >= mid) {
                                 // Edit the material
                                 Material material = element.getMaterials().get(mid - 1);
 
                                 try {
                                     MaterialDetail m = MaterialDetail.findByName(nameVal);
                                     if (m != null) {
                                         material.setName(m.getName());
                                         material.setQuantity(quantity);
                                         material.setUnits(m.getUnitOfMeasure());
 
                                         double[] results = m.performCalculations(quantity);
                                         material.setTotalEnergy(results[0]);
                                         material.setTotalCarbon(results[1]);
 
                                         bom.recalculateTotals();
 
                                         returnMessage = "ok";
                                     }
                                 } catch (Exception e) {
                                     logger.error("Could not edit material '" + nameVal
                                             + "': " + e.getMessage());
                                 }
                             }
                         } else {
                             // Edit the element
                             element.setName(DataParser.stripHtml(nameVal));
                             element.setQuantity(quantity);
                             element.setUnits(DataParser.stripHtml(unitsVal));
 
                             returnMessage = "ok";
                         }
                     }
                 } else {
                     // Edit the section
                     section.setName(DataParser.stripHtml(nameVal));
 
                     returnMessage = "ok";
                 }
             }
         }
 
         if (StringUtils.equals(returnMessage, "ok")) {
             try {
                 project.setDataField(type, bom.toJson());
                 project.merge();
                 project.flush();
             } catch (Exception e) {
                 returnMessage = this.getMessage("projects_bom_edit_error");
             }
         }
         return returnMessage;
     }
 
     /**
      * Delete an item within the bill of materials.
      *
      * @param project the project
      * @param type the type
      * @param sid the sid
      * @param eid the eid
      * @param mid the mid
      * @return the string
      */
     private String deleteBOMItem(final Project project, final String type,
             final Integer sid, final Integer eid, final Integer mid) {
 
         String returnMessage = this.getMessage("projects_bom_delete_noitemfound");
 
         BillOfMaterials bom = BillOfMaterials.parseJson(project.getDataField(type));
 
         if (sid != null && sid > 0 && bom.getSections().size() >= sid) {
             if (eid != null && eid > 0) {
                 Section section = bom.getSections().get(sid - 1);
                 if (section.getElements().size() >= eid) {
                     if (mid != null && mid > 0) {
                         Element element = section.getElements().get(eid - 1);
                         if (element.getMaterials().size() >= mid) {
                             element.getMaterials().remove(mid - 1);
                             returnMessage = "ok";
                         }
                     } else {
                         section.getElements().remove(eid - 1);
                         returnMessage = "ok";
                     }
                 }
             } else {
                 bom.removeSection(sid - 1);
                 returnMessage = "ok";
             }
         }
 
         if (StringUtils.equals(returnMessage, "ok")) {
             try {
                 bom.recalculateTotals();
 
                 project.setDataField(type, bom.toJson());
                 project.merge();
                 project.flush();
             } catch (Exception e) {
                 returnMessage = this.getMessage("projects_bom_delete_error");
             }
         }
         return returnMessage;
     }
 
     /**
      * Can view all projects.
      *
      * @param allProjectsVal the all projects val
      * @param user the user
      * @return true, if successful
      */
     private boolean canViewAllProjects(final Boolean allProjectsVal, final Person user) {
 
         boolean allProjects = false;
 
         if (allProjectsVal != null && user != null && user.getUserRole() != null) {
             if (allProjectsVal && user.getUserRole() == UserRole.ROLE_ADMIN) {
                 allProjects = true;
             }
         }
         return allProjects;
     }
 
     /**
      * Test if empty json.
      *
      * @param jsonString the json string
      * @return true, if successful
      */
     private boolean testIfEmptyJson(final String jsonString) {
 
         boolean isEmpty = true;
 
         if (StringUtils.isNotBlank(jsonString)) {
             String tempString = StringUtils.replace(jsonString, "{", "");
             tempString = StringUtils.replace(tempString, "}", "");
             tempString = StringUtils.replace(tempString, "\"", "");
 
             if (StringUtils.isNotBlank(tempString)) {
                 isEmpty = false;
             }
         }
         return isEmpty;
     }
 }
