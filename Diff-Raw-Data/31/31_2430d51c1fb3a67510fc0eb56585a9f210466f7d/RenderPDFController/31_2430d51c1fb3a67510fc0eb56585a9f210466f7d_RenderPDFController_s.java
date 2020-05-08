 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop_app.controller;
 
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletResponse;
 import org.xhtmlrenderer.pdf.ITextRenderer;
 
 /**
  *
  * @author emilbogren
  */
 @Named("pdfbean")
 @RequestScoped
 public class RenderPDFController {
     
     
     
     public void createPDF(){
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ExternalContext externalContext = facesContext.getExternalContext();
         
        
        String url = "http://localhost:8080/Group4Shop_App/content/checkout/receiptframe.xhtml";  
         
         try {
             System.out.println("Trying to create receipt");
 
             ITextRenderer renderer = new ITextRenderer();
             renderer.setDocument(new URL(url).toString());
             
            //renderer.layout();
            //HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
            //response.reset();
            //response.setContentType("application/pdf");
            //response.setHeader("Content-Disposition", "inline; filename=\"" + "hejhopp" + "\"");
            
            //OutputStream browserStream = response.getOutputStream();
             OutputStream filepath = new FileOutputStream("/Users/emilbogren/Documents/"
                     + "WebbApp/Project/WebApplikationer/Group4Shop_App/src/main/webapp/"
                     + "content/checkout/receipts/Receipt#"+"orderID"+ ".pdf");
             renderer.layout();
             renderer.createPDF(filepath);
             filepath.close();
             System.out.println("SUCCESS FILE IS CREATED");
             
         } catch (Exception ex) {
             System.out.println("FAILED : " + ex.getMessage());
         }
     }
 }
