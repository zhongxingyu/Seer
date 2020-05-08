 package com.sun.facelets.tag.core;
 
 import java.util.Date;
 import java.util.Locale;
 
 import javax.faces.component.UICommand;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIData;
import javax.faces.component.UIGraphic;
 import javax.faces.component.UIInput;
 import javax.faces.component.UIOutput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.component.html.HtmlGraphicImage;
 import javax.faces.context.FacesContext;
import javax.faces.convert.DateTimeConverter;
 import javax.faces.event.ActionListener;
 import javax.faces.validator.Validator;
 
 import com.sun.facelets.Facelet;
 import com.sun.facelets.FaceletFactory;
 import com.sun.facelets.FaceletTestCase;
 
 public class CoreTestCase extends FaceletTestCase {
 
     public void testActionListenerHandler() throws Exception {
         ActionListener listener = new ActionListenerImpl();
         FacesContext faces = FacesContext.getCurrentInstance();
         faces.getExternalContext().getRequestMap().put("actionListener",
                 listener);
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("actionListener.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
 
         UICommand action1 = (UICommand) root.findComponent("action1");
         UICommand action2 = (UICommand) root.findComponent("action2");
 
         assertNotNull("action1", action1);
         assertNotNull("action2", action2);
 
         assertEquals("action1 listeners", 1,
                 action1.getActionListeners().length);
         assertEquals("action2 listeners", 1,
                 action2.getActionListeners().length);
 
         assertEquals("action2 binding", listener,
                 action2.getActionListeners()[0]);
     }
 
     public void testAttributeHandler() throws Exception {
         String title = "Dog in a Funny Hat";
         FacesContext faces = FacesContext.getCurrentInstance();
         faces.getExternalContext().getRequestMap().put("title", title);
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("attribute.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
 
         HtmlGraphicImage graphic1 = (HtmlGraphicImage) root
                 .findComponent("graphic1");
         HtmlGraphicImage graphic2 = (HtmlGraphicImage) root
                 .findComponent("graphic2");
 
         assertNotNull("graphic1", graphic1);
         assertNotNull("graphic2", graphic2);
 
         assertEquals("graphic1 title", "literal", graphic1.getTitle());
         assertEquals("graphic2 title", title, graphic2.getTitle());
     }
 
     public void testConvertDateTimeHandler() throws Exception {
         Date now = new Date(1000 * 360 * 60 * 24 * 7);
         FacesContext faces = FacesContext.getCurrentInstance();
         faces.getExternalContext().getRequestMap().put("now", now);
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("convertDateTime.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
 
         UIOutput out1 = (UIOutput) root.findComponent("form:out1");
         UIOutput out2 = (UIOutput) root.findComponent("form:out2");
         UIOutput out3 = (UIOutput) root.findComponent("form:out3");
         UIOutput out4 = (UIOutput) root.findComponent("form:out4");
         UIOutput out5 = (UIOutput) root.findComponent("form:out5");
 
         assertNotNull("out1", out1);
         assertNotNull("out2", out2);
         assertNotNull("out3", out3);
         assertNotNull("out4", out4);
         assertNotNull("out5", out5);
 
         assertNotNull("out1 converter", out1.getConverter());
         assertNotNull("out2 converter", out2.getConverter());
         assertNotNull("out3 converter", out3.getConverter());
         assertNotNull("out4 converter", out4.getConverter());
         assertNotNull("out5 converter", out5.getConverter());
 
         assertEquals("out1 value", "12/24/69", out1.getConverter().getAsString(
                 faces, out1, now));
         assertEquals("out2 value", "12/24/69 6:57:12 AM", out2.getConverter()
                 .getAsString(faces, out2, now));
         assertEquals("out3 value", "Dec 24, 1969", out3.getConverter()
                 .getAsString(faces, out3, now));
         assertEquals("out4 value", "6:57:12 AM", out4.getConverter()
                 .getAsString(faces, out4, now));
         assertEquals("out5 value", "0:57 AM, CST", out5.getConverter()
                 .getAsString(faces, out5, now));
     }
     
     public void testConvertDelegateHandler() throws Exception {
         FacesContext faces = FacesContext.getCurrentInstance();
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("converter.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
         
         UIOutput out1 = (UIOutput) root.findComponent("out1");
         
         assertNotNull("out1", out1);
         
         assertNotNull("out1 converter", out1.getConverter());
         
         assertEquals("out1 value", new Double(42.5), out1.getConverter().getAsObject(faces, out1, out1.getLocalValue().toString()));
     }
     
     public void testConvertNumberHandler() throws Exception {
         FacesContext faces = FacesContext.getCurrentInstance();
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("convertNumber.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
         
         UIOutput out1 = (UIOutput) root.findComponent("out1");
         UIOutput out2 = (UIOutput) root.findComponent("out2");
         UIOutput out3 = (UIOutput) root.findComponent("out3");
         UIOutput out4 = (UIOutput) root.findComponent("out4");
         UIOutput out5 = (UIOutput) root.findComponent("out5");
 
         assertNotNull("out1", out1);
         assertNotNull("out2", out2);
         assertNotNull("out3", out3);
         assertNotNull("out4", out4);
         assertNotNull("out5", out5);
 
         assertNotNull("out1 converter", out1.getConverter());
         assertNotNull("out2 converter", out2.getConverter());
         assertNotNull("out3 converter", out3.getConverter());
         assertNotNull("out4 converter", out4.getConverter());
         assertNotNull("out5 converter", out5.getConverter());
 
         assertEquals("out1 value", "12", out1.getConverter().getAsString(
                faces, out1, 12.001));
         assertEquals("out2 value", "$12.00", out2.getConverter()
                .getAsString(faces, out2, 12.00));
         assertEquals("out3 value", "00,032", out3.getConverter()
                .getAsString(faces, out3, 32));
         assertEquals("out4 value", "0.67", out4.getConverter()
                .getAsString(faces, out4, 2.0/3.0));
         assertEquals("out5 value", "67%", out5.getConverter()
                .getAsString(faces, out5, 0.67));
     }
     
     public void testFacetHandler() throws Exception {
         FacesContext faces = FacesContext.getCurrentInstance();
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("facet.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
         
         UIData data = (UIData) root.findComponent("table");
         
         assertNotNull("data", data);
         
         UIComponent footer = data.getFooter();
         
         assertNotNull("footer", footer);
     }
     
     public void testLoadBundleHandler() throws Exception {
         FacesContext faces = FacesContext.getCurrentInstance();
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("loadBundle.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
         
         Object value = faces.getExternalContext().getRequestMap().get("foo");
         
         assertNotNull("bundle loaded into request", value);
     }
     
     public void testValidateDelegateHandler() throws Exception {
         FacesContext faces = FacesContext.getCurrentInstance();
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("validator.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
         
         UIInput input = (UIInput) root.findComponent("form:input");
         
         assertNotNull("input", input);
         
         assertEquals("input validator", 1, input.getValidators().length);
         
         Validator v = input.getValidators()[0];
         
         v.validate(faces, input, "4333");
     }
     
     public void testValidateDoubleRangeHandler() throws Exception {
         FacesContext faces = FacesContext.getCurrentInstance();
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("validateDoubleRange.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
         
         UIInput input = (UIInput) root.findComponent("form:input");
         
         assertNotNull("input", input);
         
         assertEquals("input validator", 1, input.getValidators().length);
         
         Validator v = input.getValidators()[0];
         
         v.validate(faces, input, new Double(1.8));
     }
     
     public void testValidateLengthHandler() throws Exception {
         FacesContext faces = FacesContext.getCurrentInstance();
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("validateLength.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
         
         UIInput input = (UIInput) root.findComponent("form:input");
         
         assertNotNull("input", input);
         
         assertEquals("input validator", 1, input.getValidators().length);
         
         Validator v = input.getValidators()[0];
         
         v.validate(faces, input, "beans");
     }
     
     public void testValidateLongRangeHandler() throws Exception {
         FacesContext faces = FacesContext.getCurrentInstance();
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("validateLongRange.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
         
         UIInput input = (UIInput) root.findComponent("form:input");
         
         assertNotNull("input", input);
         
         assertEquals("input validator", 1, input.getValidators().length);
         
         Validator v = input.getValidators()[0];
         
         v.validate(faces, input, new Long(2000));
     }
     
     public void testValueChangeListenerHandler() throws Exception {
         FacesContext faces = FacesContext.getCurrentInstance();
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("valueChangeListener.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
         
         UIInput input = (UIInput) root.findComponent("form:input");
         
         assertNotNull("input", input);
         
         assertEquals("input listener", 1, input.getValueChangeListeners().length);
     }
     
     public void testViewHandler() throws Exception {
         
         FacesContext faces = FacesContext.getCurrentInstance();
 
         FaceletFactory f = FaceletFactory.getInstance();
         Facelet at = f.getFacelet("view.xml");
 
         UIViewRoot root = faces.getViewRoot();
         at.apply(faces, root);
         
         assertEquals("german locale", Locale.GERMAN, root.getLocale());
     }
 
 }
