 package org.atomhopper.restconfig.resources.impl;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Response;

 import javax.ws.rs.core.Response.Status;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import org.atomhopper.restconfig.jaxb.generated.Configuration;
 import org.atomhopper.restconfig.jaxb.generated.ObjectFactory;
 import org.atomhopper.restconfig.resources.AtomHopperConfiguration;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 @Path("/configuration")
 public class AtomHopperConfigurationImpl implements AtomHopperConfiguration {
 
     private static final String ATOM_HOPPER_CONFIG_FILE_NAME = "atom-server.cfg.xml";
     private static final String ATOM_HOPPER_CONFIG_FILE_PATH = "/etc/atomhopper/";
     private static final Logger LOG = LoggerFactory.getLogger(AtomHopperConfigurationImpl.class);
 
     @Override
     public Response modifyConfiguration(Configuration config) {
         try {
             if (config != null) {
                 ObjectFactory factory = new ObjectFactory();
                 JAXBContext context = JAXBContext.newInstance("org.atomhopper.restconfig.jaxb.generated");
                 JAXBElement<Configuration> element = factory.createAtomHopperConfig(config);
                 Marshaller marshaller = context.createMarshaller();
                 marshaller.setProperty("jaxb.formatted.output",Boolean.TRUE);
                 marshaller.marshal(element, new FileOutputStream(ATOM_HOPPER_CONFIG_FILE_PATH + ATOM_HOPPER_CONFIG_FILE_NAME));
         
                 LOG.info("Sucessfully updated the " + ATOM_HOPPER_CONFIG_FILE_NAME + " file");
                 
                 return Response.status(Response.Status.CREATED).build();
             } else {
                 return Response.status(Response.Status.PRECONDITION_FAILED).build();
             }
 
         } catch (JAXBException e) {
             LOG.error("Failed to parse incoming xml: " + e.getMessage());
             return Response.status (Status.fromStatusCode(422)).build();
         } catch (FileNotFoundException e) {
             LOG.error("Error: " + e.getMessage());
             return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         }
     }
     
 }
