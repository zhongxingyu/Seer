 package controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.validation.Valid;
 
 import model.ErrorMessage;
 import model.ValidationResponse;
 import model.User;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 
 @Controller
 public class AjaxBootstrapController {
 	
 	@RequestMapping(value="/userAjaxBootstrap.json",method=RequestMethod.POST)
 	public @ResponseBody ValidationResponse processFormAjaxJson(Model model, @ModelAttribute(value="user") @Valid User user, BindingResult result ){
 		ValidationResponse res = new ValidationResponse();
 		if(!result.hasErrors()){
 			res.setStatus("SUCCESS");
 		}else{
 			res.setStatus("FAIL");
 			List<FieldError> allErrors = result.getFieldErrors();
 			List<ErrorMessage> errorMesages = new ArrayList<ErrorMessage>();
 			for (FieldError objectError : allErrors) {
 				errorMesages.add(new ErrorMessage(objectError.getField(), objectError.getField() + "  " + objectError.getDefaultMessage()));
 			}
 			res.setErrorMessageList(errorMesages);
 			
 		}
 		
 		return res;
 	}
 	
 	@RequestMapping(value="/userAjaxBootstrap",method=RequestMethod.GET)
 	public String showFormBootstrap(Model model){
 		model.addAttribute("user", new User());
 		return "03-bootstrap/userForm";
 	}
 
 	@RequestMapping(value="/userAjaxBootstrap.htm",method=RequestMethod.POST)
 	public String processFormBootstrap(@ModelAttribute(value="user") @Valid User user, BindingResult result ){
		return "03-bootstrap/userForm";
 	}
 	
 	@RequestMapping(value="/userAjaxCustomTag",method=RequestMethod.GET)
 	public String showFormCustomTag(Model model){
 		model.addAttribute("user", new User());
 		return "04-custom-tag/userForm";
 	}
 
 	@RequestMapping(value="/userAjaxCustomTag.htm",method=RequestMethod.POST)
 	public String processFormAjax(@ModelAttribute(value="user") @Valid User user, BindingResult result ){
		return "04-custom-tag/userForm";
 	}
 }
