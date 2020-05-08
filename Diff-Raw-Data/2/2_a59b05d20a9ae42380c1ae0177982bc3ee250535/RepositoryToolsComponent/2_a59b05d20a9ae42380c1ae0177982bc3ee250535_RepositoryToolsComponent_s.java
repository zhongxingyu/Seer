 package org.kevoree.library.javase.webserver.collaborationToolsBasics.server;
 
 import org.kevoree.annotation.ComponentType;
 import org.kevoree.annotation.DictionaryAttribute;
 import org.kevoree.annotation.DictionaryType;
 import org.kevoree.annotation.Library;
 import org.kevoree.library.javase.webserver.AbstractPage;
 import org.kevoree.library.javase.webserver.FileServiceHelper;
 import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
 import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
 import org.kevoree.library.javase.webserver.servlet.LocalServletRegistry;
 import scala.collection.immutable.List;
 
 import javax.servlet.ServletContextListener;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pdespagn
  * Date: 5/31/12
  * Time: 11:45 AM
  * To change this template use File | Settings | File Templates.
  */
 @Library(name = "JavaSE")
 @ComponentType
 @DictionaryType({
         @DictionaryAttribute(name = "directoryPath" , defaultValue = "/tmp/"),
 })
 public class RepositoryToolsComponent extends AbstractPage {
 
     private LocalServletRegistry servletRepository = null;
 
 
     public void startPage() {
 
         servletRepository = new LocalServletRegistry(){
             @Override
             public String getCDefaultPath(){
                 return "/ihmcodemirror";
             }
 
             @Override
             public List<ServletContextListener> listeners() {
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void listeners_$eq(List<ServletContextListener> listeners) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         };
         super.startPage();
         servletRepository.registerServlet("/ihmcodemirror/htmleditor", new RepositoryToolsServicesImpl(this.getDictionary().get("directoryPath").toString()));
         servletRepository.registerServlet("/ihmcodemirror/systemFileServices",new StructureServiceImpl());
        servletRepository.registerServlet("/ihmcodemirror/upload",new UploadFileServer());
     }
 
     @Override
     public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {
         ClassLoader l = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(RepositoryToolsComponent.class.getClassLoader() );
         System.err.println("URL " + request.getUrl() + "\n" + request + "\n" );
 
         boolean res = servletRepository.tryURL(request.getUrl(),request,response);
 
         Thread.currentThread().setContextClassLoader(l);
         if ( res ){
 
             return response;
 
         }
 
 
         if (FileServiceHelper.checkStaticFile(request.getUrl(), this, request, response)) {
             return response;
         }
         if (FileServiceHelper.checkStaticFile("IHMcodeMirror.html", this, request, response)) {
 
             return response;
         }
         response.setContent("Bad request1");
         return response;
     }
 }
