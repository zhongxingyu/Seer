 package org.fiteagle.interactors.certificates;
 
 import java.util.List;
 
 import java.io.IOException;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.fiteagle.core.aaa.KeyStoreManagement;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
@Path("/")
 public class RestCertificateManager implements RestCertificateManagement{
   
   private Logger log = LoggerFactory.getLogger(RestCertificateManager.class);
   private KeyStoreManagement manager = KeyStoreManagement.getInstance();
   
   @Override
   @GET
   @Path("list")
   @Produces(MediaType.APPLICATION_JSON)
   public List<String> getCertificates() {
     List<String> certnames;
     try {
       certnames = manager.getTrustedCertsCommonNames();
     } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
       log.error(e.getMessage());
       throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
     }
          
     return certnames;
   }
 
   @Override
   @GET
   @Path("download/{commonName}")
   @Produces(MediaType.TEXT_PLAIN)
   public String getCertificate(@PathParam("commonName") String commonName) {
     String cert;
     try {
       cert = manager.getTrustedCertificate(commonName);
     } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
       log.error(e.getMessage());
       throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
     }
     if(cert == null){
       throw new WebApplicationException(Response.Status.NOT_FOUND);
     }
     return cert;
   }
 }
