 package au.com.miskinhill.web.rdf;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Resource;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.web.HttpRequestHandler;
 import org.springframework.web.HttpRequestMethodNotSupportedException;
 
 import au.com.miskinhill.rdf.Representation;
 import au.com.miskinhill.rdf.RepresentationFactory;
 import au.com.miskinhill.web.util.AcceptHeader;
 
 @Component("rdfRequestHandler")
 public class RDFRequestHandler implements HttpRequestHandler {
     
     private final Model model;
     private final RepresentationFactory representationFactory;
     
     @Autowired
     public RDFRequestHandler(Model model, RepresentationFactory representationFactory) {
         this.model = model;
         this.representationFactory = representationFactory;
     }
     
     @Override
     public void handleRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         if (request.getMethod().equals("GET")) {
             try {
                 doGet(request, response);
             } catch (Exception e) {
                 throw new ServletException(e);
             }
         } else {
             throw new HttpRequestMethodNotSupportedException(request.getMethod());
         }
     }
     
     private void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String uri = "http://miskinhill.com.au" + req.getContextPath() + req.getServletPath() +
                 (req.getPathInfo() != null ? req.getPathInfo() : "");
         
         // figure out what URI they really wanted
         String extensionFormat = null;
         do {
             // try as is
             if (existsInModel(model, uri))
                 break;
             // does it end with a format extension like .xml?
             for (String format: representationFactory.getAllFormats()) {
                 if (uri.endsWith("." + format)) {
                     extensionFormat = format;
                     uri = uri.substring(0, uri.length() - format.length() - 1);
                     break;
                 }
             }
             if (existsInModel(model, uri))
                 break;
             // is it missing a trailing slash?
             if (!uri.endsWith("/")) {
                 uri = uri + "/";
                 if (existsInModel(model, uri)) {
                     resp.setHeader("Location", uri + (extensionFormat != null ? "." + extensionFormat : ""));
                     resp.sendError(HttpServletResponse.SC_FOUND);
                     return;
                 }
             }
             // couldn't match anything
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         } while (false);
         
         Resource resource = model.createResource(uri);
         Representation representation;
         if (extensionFormat != null) {
             representation = representationFactory.getRepresentationByFormat(extensionFormat);
             if (!representation.canRepresent(resource)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                 return;
             }
         } else {
             List<Representation> candidates = representationFactory.getRepresentationsForResource(resource);
             if (candidates.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND); // probably this should never happen
                 return;
             }
             representation = negotiate(candidates, req.getHeader("Accept"));
         }
         resp.setContentType(representation.getContentType().toString());
         resp.setCharacterEncoding("UTF-8");
         resp.getWriter().append(representation.render(resource));
     }
     
     private boolean existsInModel(Model model, String uri) {
         return model.createResource(uri).listProperties().hasNext();
     }
     
     private Representation negotiate(List<Representation> candidates, String acceptHeader) {
         List<String> candidateContentTypes = new ArrayList<String>(candidates.size());
         for (Representation representation: candidates) {
             candidateContentTypes.add(representation.getContentType().toString());
         }
         AcceptHeader accept = AcceptHeader.parse(acceptHeader);
         String bestMatch = accept.bestMatch(candidateContentTypes);
         if (bestMatch != null) {
             return representationFactory.getRepresentationByContentType(bestMatch);
         } else {
             return candidates.get(0);
         }
     }
 
 }
