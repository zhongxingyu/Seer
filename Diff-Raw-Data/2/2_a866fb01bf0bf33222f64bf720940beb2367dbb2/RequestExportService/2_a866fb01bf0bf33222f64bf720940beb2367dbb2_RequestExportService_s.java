 package fr.cg95.cvq.service.request.impl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlObject;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.service.request.IRequestExportService;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.service.users.IIndividualService;
 import fr.cg95.cvq.xml.common.IndividualType;
 import fr.cg95.cvq.xml.common.RequestType;
 import fr.cg95.cvq.xml.common.SubjectType;
 
 public class RequestExportService implements IRequestExportService {
 
     private static Logger logger = Logger.getLogger(RequestExportService.class);
     
     private IHomeFolderService homeFolderService;
     private IIndividualService individualService;
 
     @Override
     public XmlObject fillRequestXml(Request request)
         throws CvqException {
         XmlObject result = request.modelToXml();
         RequestType xmlRequestType = null;
         try {
             xmlRequestType = (RequestType) result.getClass()
                 .getMethod("get" + result.getClass().getSimpleName().replace("DocumentImpl", ""))
                     .invoke(result);
         } catch (IllegalAccessException e) {
             logger.error("fillRequestXml() Illegal access exception while filling request xml");
             throw new CvqException("Illegal access exception while filling request xml");
         } catch (InvocationTargetException e) {
             logger.error("fillRequestXml() Invocation target exception while filling request xml");
             throw new CvqException("Invocation target exception while filling request xml");
         } catch (NoSuchMethodException e) {
             logger.error("fillRequestXml() No such method exception while filling request xml");
             throw new CvqException("No such method exception while filling request xml");
         }
         if (request.getSubjectId() != null) {
             Individual individual = individualService.getById(request.getSubjectId());
             SubjectType subject = xmlRequestType.addNewSubject();
             if (individual instanceof Adult) {
                 subject.setAdult(Adult.modelToXml((Adult)individual));
             } else if (individual instanceof Child) {
                 subject.setChild(Child.modelToXml((Child)individual));
             }
         }
         if (request.getHomeFolderId() != null) {
             xmlRequestType.addNewHomeFolder()
                 .set(homeFolderService.getById(request.getHomeFolderId()).modelToXml());
             List<Individual> externalIndividuals =
                 homeFolderService.getExternalIndividuals(request.getHomeFolderId());
             if (externalIndividuals != null && !externalIndividuals.isEmpty()) {
                 IndividualType[] individualsArray = new IndividualType[externalIndividuals.size()];
                 int i = 0;
                 for (Individual externalIndividual : externalIndividuals) {
                     if (externalIndividual instanceof Adult) {
                         Adult adult = (Adult) externalIndividual;
                         individualsArray[i] = Adult.modelToXml(adult);
                     } else if (externalIndividual instanceof Child) {
                         Child child = (Child) externalIndividual;
                         individualsArray[i] = Child.modelToXml(child);
                     }
                     i++;
                 }
                xmlRequestType.getHomeFolder().setIndividualsArray(individualsArray);
             }
         }
         if (request.getRequesterId() != null) {
             xmlRequestType.addNewRequester().set(Adult.modelToXml(individualService.getAdultById(request.getRequesterId())));
         }
         return result;
     }
 
     public void setHomeFolderService(IHomeFolderService homeFolderService) {
         this.homeFolderService = homeFolderService;
     }
 
     public void setIndividualService(IIndividualService individualService) {
         this.individualService = individualService;
     }
 }
