 package info.harmia.polyglot.springapp.mvc.web.controller;
 
 import info.harmia.polyglot.springapp.mvc.core.model.Department;
 import info.harmia.polyglot.springapp.mvc.core.service.DepartmentService;
 import info.harmia.polyglot.springapp.mvc.core.service.EmployeeForm;
 import info.harmia.polyglot.springapp.mvc.core.service.EmployeeService;
 import info.harmia.polyglot.springapp.mvc.web.utils.FlashMessage;
 import org.json.JSONException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: harmia
  * Date: 16.4.2013
  * Time: 16:14
  * Copyright (C) 2013 Juhana "harmia" Harmanen
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 @Controller
 public class EmployeeController {
     @Autowired
     private EmployeeService employeeService;
     @Autowired
     private DepartmentService departmentService;
 
     @RequestMapping(value = "/employees/", method = RequestMethod.GET)
     public String listEmployees(@ModelAttribute("employee") EmployeeForm employeeForm, ModelMap model, HttpSession session) {
         List<Department> departments = departmentService.listDepartments();
         if (departments.isEmpty()) {
             FlashMessage.createInfoMessage(session, "employees.empty.departments");
         }
         model.addAttribute("employee", employeeForm);
         model.addAttribute("departments", departments);
         model.addAttribute("employees", employeeService.listEmployees());
         return "employees";
     }
 
    @RequestMapping(value = "/api/employees", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
     @ResponseBody
    public String listEmployeesJson() throws JSONException {
         return employeeService.listEmployeesJson().toString();
     }
 
     @RequestMapping(value = "/employees/add", method = RequestMethod.POST)
     public String addEmployee(@ModelAttribute("employee") @Valid EmployeeForm employeeForm, BindingResult result, ModelMap model, HttpSession session) {
         if(result.hasErrors()) {
             FlashMessage.createErrorMessage(session, "employees.add.error", employeeForm.getLastName(), employeeForm.getFirstName());
             return listEmployees(employeeForm, model, session);
         }
         employeeService.addEmployee(employeeForm);
         FlashMessage.createSuccessMessage(session, "employees.add.success", employeeForm.getLastName(), employeeForm.getFirstName());
         return "redirect:/employees/";
     }
 
     @RequestMapping(value = "/employees/delete/{employeeId}", method = RequestMethod.POST)
     public String deleteEmployee(@PathVariable("employeeId") Long employeeId, HttpSession session) {
         if (employeeService.deleteEmployee(employeeId)) {
             FlashMessage.createSuccessMessage(session, "employees.delete.success", employeeId);
         } else {
             FlashMessage.createErrorMessage(session, "employees.delete.error", employeeId);
         }
         return "redirect:/employees/";
     }
 
     @RequestMapping(value = "/employees/change/{employeeId}/{departmentId}", method = RequestMethod.POST)
     public String changeDepartment(@PathVariable("employeeId") Long employeeId, @PathVariable("departmentId") Long departmentId, HttpSession session) {
         if (employeeService.changeDepartment(employeeId, departmentId)) {
             FlashMessage.createSuccessMessage(session, "employees.change.success", employeeId, departmentId);
         } else {
             FlashMessage.createErrorMessage(session, "employees.change.error", employeeId, departmentId);
         }
         return "redirect:/employees/";
     }
 }
