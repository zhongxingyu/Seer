 package com.seitenbau.micgwaf.component;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class InputComponent extends HtmlElementComponent
 {
   public boolean submitted;
   
   public String value;
   
   public InputComponent(Component parent)
   {
     super(parent);
   }
 
   public InputComponent(String elementName, String id, Component parent)
   {
     super(elementName, id, parent);
   }
 
   @Override
   public Component processRequest(HttpServletRequest request)
   {
     System.out.println(request.getParameterMap());
     String nameAttr = attributes.get("name");
    if (value != null)
     {
       value = request.getParameter(nameAttr);
       if (value != null)
       {
         submitted = true;
       }
     }
     return super.processRequest(request);
   }
   
   public boolean isButton()
   {
     if ("button".equals(elementName) 
         || ("input".equals(elementName) 
             && "submit".equals(attributes.get("type"))))
     {
       return true;
     }
     return false;
   }
 }
