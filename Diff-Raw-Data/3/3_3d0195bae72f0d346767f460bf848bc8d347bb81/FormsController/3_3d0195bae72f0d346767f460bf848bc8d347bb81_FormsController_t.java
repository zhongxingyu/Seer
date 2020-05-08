 package com.appCore.personnel.User.Controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import javax.annotation.Resource;
 import java.util.List;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.log4j.Logger;
 import com.appCore.Requests.RequestStatus;
 import com.appCore.personnel.Core.Helpers.RequestStatusHelper;
 
 import com.appCore.personnel.User.Entity.FormMenuView;
 import com.appCore.personnel.User.Entity.Forms;
 import com.appCore.personnel.User.Entity.Forms_Actions_Role;
 import com.appCore.personnel.User.Entity.Users;
 
 import com.appCore.personnel.User.Service.FormsService;
 
 
 @Controller
 @RequestMapping("/User")
 public class FormsController
 { 
 
 	
 		protected static Logger logger = Logger.getLogger("controller");
 		@Resource(name="formsService")
 		private FormsService service;
 
 		@RequestMapping(value = "/Forms/list", method = RequestMethod.GET)		
 		public @ResponseBody List<Forms> listForms () 
 		{	
 				List<Forms> list = service.getAll();
 				return list;
 		}
 		
 		@RequestMapping(value = "/Forms/listMenu", method = RequestMethod.GET)		
 		public @ResponseBody List<FormMenuView> listFormsMenu() 
 		{	
			return service.getFormsMenu();
 		}
 		
 		@RequestMapping(value = "/Forms/listMenuByUser", method = RequestMethod.GET)		
 		public @ResponseBody List<FormMenuView> listFormsMenu(Users user) 
 		{	
 				return service.getFormsMenu();
 		}
 		
 		@RequestMapping(value = "/Forms/get", method = RequestMethod.GET)		
 		public @ResponseBody Forms_Actions_Role getForms (@RequestParam(value="id", required=true) String formId, @RequestParam(value="roleId", required=true) int[] rolesId) 
 		{		
 				Forms_Actions_Role forms=service.get(formId, rolesId);	
 				return forms;
 		}
 		
 		@RequestMapping(value = "/Forms/add", method = RequestMethod.GET)
 		public @ResponseBody RequestStatus renderAddForms (@RequestBody Forms forms)
 		{
 				service.add(forms);
 				return RequestStatusHelper.GenerateRequestStatusSaveOperation();
 		}
 
 		@RequestMapping(value = "/Forms/add", method = RequestMethod.POST)
 		public @ResponseBody RequestStatus addForms (@RequestBody Forms forms)
 		{
 				service.add(forms);
 				return RequestStatusHelper.GenerateRequestStatusSaveOperation();
 		}
 
 		@RequestMapping(value = "/Forms/saveOrUpdate", method = RequestMethod.POST)
 		public @ResponseBody RequestStatus saveOrUpdateForms (@RequestBody Forms forms)
 		{
 				service.saveOrUpdate(forms);
 				return RequestStatusHelper.GenerateRequestStatusSaveOperation();
 		}
 
 		@RequestMapping(value = "/Forms/delete", method = RequestMethod.GET)
 		public @ResponseBody RequestStatus deleteForms (@RequestParam(value="id", required=true) Integer id)
 		{
 				service.delete(id);
 				return RequestStatusHelper.GenerateRequestStatusDeleteOperation();
 		}
 
 
 }
