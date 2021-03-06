 package br.com.caelum.vraptor.example;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.resource.ResourceMethod;
 import br.com.caelum.vraptor.view.jsp.JspView;
 
 @Resource
 public class ClientsController {
 
     private static final List<Client> clients = new ArrayList<Client>();
     static {
         clients.add(client("Guilherme"));
     }
     private static Client client(String name) {
         Client c = new Client();
         c.setName(name);
         return c;
     }
 
     
     
     
     private final Result result;
 
     public ClientsController(Result result) {
         this.result = result;
     }
 
     @Path("/clients")
     public void list() throws ServletException, IOException {
         result.include("clients", clients);
        result.use(JspView.jsp(new JspResolver() {
            public String pathFor(ResourceMethod method, String result) {
                return "/" + method.getResource().getType().getName() + "/" + method.getMethod().getName() + "." + result + ".jsp";
            }
        })).forward();
     }
 }
