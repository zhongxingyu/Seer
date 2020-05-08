 package com.bytedistillers.payment.paypal.starter;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.bytedistillers.payment.common.form.FormData;
 import com.bytedistillers.payment.common.form.FormDataMergeUtil;
 import com.bytedistillers.payment.common.form.FormGeneratorUtil;
 
 public class PayPalStarterTemplate {
 
   private static final Logger logger = LoggerFactory.getLogger(PayPalStarterTemplate.class);
 
   private String formAction = "https://www.paypal.com/cgi-bin/webscr";
   private String formTarget;
   private String formName = "paypalStarterForm";
   private FormData staticFormData;
 
   private FormGeneratorUtil formGenUtil = new FormGeneratorUtil();
  private FormDataMergeUtil<FormData> formMergeUtil = new FormDataMergeUtil<FormData>();
 
   public PayPalStarterTemplate() {
   }
 
   public PayPalStarterTemplate(FormData staticFormData) {
     this.staticFormData = staticFormData;
   }
 
   public String generateHtmlForm(FormData dynamicFormData) {
    FormData mergedFormData = formMergeUtil.merge(dynamicFormData, staticFormData, staticFormData.getClass());
 
     String toAppend = formGenUtil.generateFormStartTag(formAction, formTarget, formName);
     StringBuilder result = new StringBuilder(toAppend);
 
     toAppend = formGenUtil.generateFieldValues(mergedFormData.getClass(), mergedFormData);
     result.append(toAppend);
     if (mergedFormData instanceof StarterCartFormData) {
       List<CartItem> items = ((StarterCartFormData) mergedFormData).getItems();
       int idx = 0;
       for (CartItem item : items) {
         toAppend = formGenUtil.generateFieldValues(item.getClass(), item, ("_" + ++idx));
         result.append(toAppend);
       }
     }
 
     result.append(formGenUtil.generateFormEndTag());
 
     return result.toString();
   }
 
   public FormData getStaticFormData() {
     return staticFormData;
   }
 
   public String getFormAction() {
     return formAction;
   }
 
   public void setFormAction(String formAction) {
     this.formAction = formAction;
   }
 
   public String getFormName() {
     return formName;
   }
 
   public void setFormName(String formName) {
     this.formName = formName;
   }
 
   public String getFormTarget() {
     return formTarget;
   }
 
   public void setFormTarget(String formTarget) {
     this.formTarget = formTarget;
   }
 }
