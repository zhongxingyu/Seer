 package com.mpower.controller.code;
 
 import javax.annotation.Resource;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import com.mpower.domain.model.customization.Code;
 import com.mpower.service.CodeService;
 
 public class CodeFormController extends SimpleFormController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Resource(name="codeService")
     private CodeService codeService;
 
     @Override
     protected Object formBackingObject(HttpServletRequest request) throws ServletException {
        String codeId = request.getParameter("codeId");
        String codeType = request.getParameter("codeType");
         if (codeId == null) {
             Code code = new Code();
            code.setCodeType(codeService.readCodeTypeByName(codeType));
             return code;
         } else {
            return codeService.readCodeById(new Long(codeId));
         }
     }
 
     @Override
     public ModelAndView onSubmit(Object command, BindException errors) throws ServletException {
         Code code = (Code) command;
        code.setCodeType(codeService.readCodeTypeByName(code.getCodeType().getName())); 
         Code newCode = codeService.maintainCode(code);
 
         ModelAndView mav = new ModelAndView(getSuccessView());
         mav.addObject("code", newCode);
         return mav;
     }
 }
