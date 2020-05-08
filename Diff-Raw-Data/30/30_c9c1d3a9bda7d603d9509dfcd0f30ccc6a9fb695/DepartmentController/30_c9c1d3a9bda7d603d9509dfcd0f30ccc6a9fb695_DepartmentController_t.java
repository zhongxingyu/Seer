 package info.harmia.polyglot.springapp.mvc.web.controller;
 
 import info.harmia.polyglot.springapp.mvc.core.service.DepartmentForm;
 import info.harmia.polyglot.springapp.mvc.core.service.DepartmentService;
 import info.harmia.polyglot.springapp.mvc.web.utils.FlashMessage;
 import org.json.JSONException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 /**
  * Created with IntelliJ IDEA.
  * User: harmia
  * Date: 16.4.2013
  * Time: 21:01
  * Copyright (C) 2013 Juhana "harmia" Harmanen
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 @Controller
 public class DepartmentController {
     @Autowired
     private DepartmentService departmentService;
 
     @RequestMapping(value = "/departments/", method = RequestMethod.GET)
     public String listDepartments(@ModelAttribute("department") DepartmentForm departmentForm, ModelMap model) {
         model.addAttribute("department", departmentForm);
         model.addAttribute("departments", departmentService.listDepartments());
         return "departments";
     }
 
    @RequestMapping(value = "/api/departments", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
     @ResponseBody
    public String listDepartmentsJson() throws JSONException {
         return departmentService.listDepartmentsJson().toString();
     }
 
     @RequestMapping(value = "/departments/add", method = RequestMethod.POST)
     public String addDepartment(@ModelAttribute("department") @Valid DepartmentForm departmentForm, BindingResult result, ModelMap model, HttpSession session) {
         if(result.hasErrors()) {
             FlashMessage.createErrorMessage(session, "departments.add.error", departmentForm.getName());
             return listDepartments(departmentForm, model);
         }
         departmentService.addDepartment(departmentForm);
         FlashMessage.createSuccessMessage(session, "departments.add.success", departmentForm.getName());
         return "redirect:/departments/";
     }
 
     @RequestMapping(value = "/departments/delete/{departmentId}", method = RequestMethod.POST)
     public String deleteDepartment(@PathVariable("departmentId") Long departmentId, HttpSession session) {
         if (departmentService.deleteDepartment(departmentId)) {
             FlashMessage.createSuccessMessage(session, "departments.delete.success", departmentId);
         } else {
             FlashMessage.createErrorMessage(session, "departments.delete.error", departmentId);
             FlashMessage.createInfoMessage(session, "departments.delete.info");
         }
         return "redirect:/departments/";
     }
 }
