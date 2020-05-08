 package example.render;
 
 import example.datamodel.contact.Contact;
 import templ.apt.RendererProxy;
 
 public class PrintContact {
 
     public static void main(String[] args) {
         TestRenderer renderer = RendererProxy.mock(TestRenderer.class);
 
        System.out.println( renderer.renderContact(new Contact("John")) );
         System.out.println( renderer.renderContact(new Contact("John", "john@rambo.net")) );
     }
 }
