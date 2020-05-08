 /*
  * Created on Jan 22, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.vfny.geoserver.action.validation;
 
 import java.util.List;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.vfny.geoserver.action.ConfigAction;
 import org.vfny.geoserver.config.validation.TestConfig;
 import org.vfny.geoserver.config.validation.TestSuiteConfig;
 import org.vfny.geoserver.config.validation.ValidationConfig;
 import org.vfny.geoserver.form.validation.ValidationTestEditorForm;
 import org.vfny.geoserver.global.UserContainer;
 
 /**
  * ValidationTestEditorAction purpose.
  * <p>
  * Description of ValidationTestEditorAction ...
  * </p>
  * 
  * <p>
  * Capabilities:
  * </p>
  * <ul>
  * <li>
  * Feature: description
  * </li>
  * </ul>
  * <p>
  * Example Use:
  * </p>
  * <pre><code>
  * ValidationTestEditorAction x = new ValidationTestEditorAction(...);
  * </code></pre>
  * 
  * @author User, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: ValidationTestEditorAction.java,v 1.5 2004/04/08 21:58:25 dmzwiers Exp $
  */
 public class ValidationTestEditorAction extends ConfigAction {
     public ActionForward execute(ActionMapping mapping,
             ActionForm incomingForm, UserContainer user, HttpServletRequest request,
             HttpServletResponse response) {
         
         ValidationTestEditorForm form = (ValidationTestEditorForm) incomingForm;
         
         String name = form.getName();
         String description = form.getDescription();
         List attributeKeys = form.getAttributeKeys();
         List attributeValues = form.getAttributeValues();
        
         ServletContext context = getServlet().getServletContext();
         ValidationConfig validationConfig = (ValidationConfig) context.getAttribute(ValidationConfig.CONFIG_KEY);
         TestSuiteConfig suiteConfig = (TestSuiteConfig) request.getSession().getAttribute(TestSuiteConfig.CURRENTLY_SELECTED_KEY);
         TestConfig testConfig = (TestConfig) request.getSession().getAttribute(TestConfig.CURRENTLY_SELECTED_KEY);
         
         //this allows renaming. If they change the test's name, we just remove it add a new one
         suiteConfig.removeTest(testConfig.getName());
         
         testConfig.setName(name);
         testConfig.setDescription(description);
         
         for (int i = 0; i < attributeKeys.size(); i++) {
             System.out.println((String) attributeKeys.get(i)+"="+ (String) attributeValues.get(i));
             System.out.println(testConfig.getArgs());
            String val = (String) attributeValues.get(i);
            if(val != null && val != "")
            	testConfig.setArgStringValue((String) attributeKeys.get(i), val);
         }
        
         suiteConfig.addTest(testConfig);
         getApplicationState().notifyConfigChanged();
         
         request.getSession().removeAttribute(TestConfig.CURRENTLY_SELECTED_KEY);
         
         return mapping.findForward("validationTest");
     }
 }
