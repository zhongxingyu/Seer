 package org.codehaus.xfire.client;
 
 import java.net.MalformedURLException;
 
 import org.codehaus.xfire.DefaultXFire;
 import org.codehaus.xfire.XFireFactory;
 import org.codehaus.xfire.demo.Book;
 import org.codehaus.xfire.demo.IBook;
 import org.codehaus.xfire.security.wssecurity.WSS4JOutSecurityHandler;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.util.dom.DOMOutHandler;
 
 public class BookClientEnc
     extends BookClientUTPP
 {
 
     public void executeClient(String url)
     throws MalformedURLException
 {
     DefaultXFire xfire = (DefaultXFire) XFireFactory.newInstance().getXFire();
 
    xfire.addOutHandler(new DOMOutHandler());

    xfire.addOutHandler(new WSS4JOutSecurityHandler("org/codehaus/xfire/client/outsecurity.properties"));

     Service serviceModel = new ObjectServiceFactory()
             .create(IBook.class, "BookService", "http://xfire.codehaus.org/BookService", null);
 
     IBook service = (IBook) new XFireProxyFactory().create(serviceModel, url);
     Book b = service.findBook("0123456789");
 
 }
     
     public static void main(String[] args)
         throws MalformedURLException
     {
         BookClientEnc client = new BookClientEnc();
         client.executeClient("http://localhost:8081/bookws/services/BookServiceENC");
 
     }
 
 }
