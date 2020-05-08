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
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Provider;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.xhtmlrenderer.pdf.ITextRenderer;
 import org.xhtmlrenderer.pdf.ITextUserAgent;
 
 /**
  *
  * @author emilbogren
  */
 @Named("pdfbean")
 @RequestScoped
 public class RenderPDFController {
     
     @Inject
     private Provider<EmailController> emailController;
     
     private String filepath;
     
     public void createPDF(){
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ExternalContext externalContext = facesContext.getExternalContext();
         HttpSession session = (HttpSession) externalContext.getSession(true);
         
         String url = "http://localhost:8080/Group4Shop_App/content/checkout/receiptframe.xhtml"+";jsessionid=" + session.getId();  
         
         try {
             System.out.println("Trying to create receipt");
 
             ITextRenderer renderer = new ITextRenderer();
             renderer.setDocument(new URL(url).toString());
             //ITextUserAgent callback = new ITextUserAgent(renderer.getOutputDevice());
             //callback.
             
             filepath = "";
             OutputStream filepath = new FileOutputStream("/Users/emilbogren/Documents/"
                     + "WebbApp/Project/WebApplikationer/Group4Shop_App/src/main/webapp/"
                    + "content/checkout/receipts/Receipt#"+"orderID"+ ".pdf");
             renderer.layout();
             renderer.createPDF(filepath);
             filepath.close();
             
             System.out.println("SUCCESS FILE IS CREATED");
             
             sendReceiptAsMail();
             
         } catch (Exception ex) {
             System.out.println("FAILED : " + ex.getMessage());
         }
     }
     
     private void sendReceiptAsMail(){
         emailController.get().sendEmail(" ", "SPAM", filepath);
         
     }
     
 }
