 package org.nuxeo.adullact;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 import org.adullact.www.spring_ws.iparapheur._1_0.CreerDossierRequest;
 import org.adullact.www.spring_ws.iparapheur._1_0.TypeDoc;
 import org.adullact.www.spring_ws.iparapheur._1_0.Visibilite;
 import org.nuxeo.ecm.core.api.Blob;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.model.PropertyException;
 
 public class AdullactDossier {
 
     // private static Log log = LogFactory.getLog(AdullactDossier.class);
 
     public static final String IPARAPHEUR_DOSSIER_SCHEMA_PREFIX = "adudos";
 
     public static final String ANNOTATION_PRIVEE_XPATH_NAME = IPARAPHEUR_DOSSIER_SCHEMA_PREFIX
             + ":annotationPrivee";
 
     public static final String EMAIL_EMETTEUR_XPATH_NAME = IPARAPHEUR_DOSSIER_SCHEMA_PREFIX
             + ":emailEmetteur";
 
     public static final String STATUS_XPATH_NAME = IPARAPHEUR_DOSSIER_SCHEMA_PREFIX
             + ":currentState";
 
     public static final String VISIBILITE_XPATH_NAME = IPARAPHEUR_DOSSIER_SCHEMA_PREFIX
             + ":visibilite";
 
     private static final String SOUS_TYPE_XPATH_NAME = IPARAPHEUR_DOSSIER_SCHEMA_PREFIX
             + ":sousType";
 
     public static final String TYPE_XPATH_NAME = IPARAPHEUR_DOSSIER_SCHEMA_PREFIX
             + ":typeTechnique";
 
     public static final String ANNOTATION_PUBLIC_XPATH_NAME = IPARAPHEUR_DOSSIER_SCHEMA_PREFIX
             + ":annotationPublique";
 
     public static final String DOSSIERID_XPATH_NAME = IPARAPHEUR_DOSSIER_SCHEMA_PREFIX
             + ":dossierID";
 
     public static final String MAIN_FILE_XPATH_NAME = "file:content";
 
     public DocumentModel doc;
 
     public AdullactDossier(DocumentModel doc) {
         this.doc = doc;
     }
 
     public String getDossierID() throws ClientException {
         String value = (String) doc.getPropertyValue(DOSSIERID_XPATH_NAME);
         if (value == null) {
             value = doc.getId();
         }
         return value;
     }
 
     public void setDossierID(String dossierID) throws ClientException {
         doc.setPropertyValue(DOSSIERID_XPATH_NAME, dossierID);
     }
 
     public String getPublicAnnotation() throws ClientException {
         return (String) doc.getPropertyValue(ANNOTATION_PUBLIC_XPATH_NAME);
     }
 
     public void setPublicAnnotation(String publicAnnotation)
             throws ClientException {
         doc.setPropertyValue(ANNOTATION_PUBLIC_XPATH_NAME, publicAnnotation);
     }
 
     public String getPrivateAnnotation() throws ClientException {
         return (String) doc.getPropertyValue(ANNOTATION_PRIVEE_XPATH_NAME);
     }
 
     public void setPrivateAnnotation(String privateAnnotation)
             throws ClientException {
         doc.setPropertyValue(ANNOTATION_PRIVEE_XPATH_NAME, privateAnnotation);
     }
 
     public String getStatus() throws ClientException {
         return (String) doc.getPropertyValue(STATUS_XPATH_NAME);
     }
 
     public void setStatus(String status) throws ClientException {
         doc.setPropertyValue(STATUS_XPATH_NAME, status);
     }
 
     public String getEmailEmetteur() throws ClientException {
         return (String) doc.getPropertyValue(EMAIL_EMETTEUR_XPATH_NAME);
     }
 
     public void setEmailEmetteur(String emailEmetteur) throws ClientException {
         doc.setPropertyValue(EMAIL_EMETTEUR_XPATH_NAME, emailEmetteur);
     }
 
     public String getType() throws ClientException {
         return (String) doc.getPropertyValue(TYPE_XPATH_NAME);
     }
 
     public void setType(String type) throws ClientException {
         doc.setPropertyValue(TYPE_XPATH_NAME, type);
     }
 
     public String getSousType() throws ClientException {
         return (String) doc.getPropertyValue(SOUS_TYPE_XPATH_NAME);
     }
 
     public void setSousType(String sousType) throws ClientException {
         doc.setPropertyValue(SOUS_TYPE_XPATH_NAME, sousType);
     }
 
     public String getVisibilite() throws ClientException {
         return (String) doc.getPropertyValue(VISIBILITE_XPATH_NAME);
     }
 
     public void setVisibilite(String visibilite) throws ClientException {
         doc.setPropertyValue(VISIBILITE_XPATH_NAME, visibilite);
     }
 
     public Blob getMainFile() throws ClientException {
         return (Blob) doc.getPropertyValue(MAIN_FILE_XPATH_NAME);
     }
 
     public String getUrl() throws ClientException {
        return "http://iparapheur.demonstrations.adullact.org/alfresco/navigate/browse/workspace/SpacesStore/" + getDossierID();
     }
 
     public void setMainFile(Blob mainFile) throws ClientException {
         doc.setPropertyValue(MAIN_FILE_XPATH_NAME, (Serializable) mainFile);
     }
 
     public CreerDossierRequest getCreerDossierRequest() throws ClientException {
         CreerDossierRequest cdr = new CreerDossierRequest();
         cdr.setDossierID(doc.getId());
         try {
             cdr.setTypeTechnique(getType());
             cdr.setSousType(getSousType());
             cdr.setDossierID(getDossierID());
             cdr.setEmailEmetteur(getEmailEmetteur());
             String visibilite = getVisibilite();
             if (visibilite != null && !"".equals(visibilite.trim())) {
                 cdr.setVisibilite(Visibilite.fromString(visibilite));
             }
             cdr.setAnnotationPrivee(getPrivateAnnotation());
             cdr.setAnnotationPublique(getPublicAnnotation());
 
             Blob main = getMainFile();
             if (main == null) {
                 throw new ClientException(
                         "No file attach to document can't ask signature to iParapheur");
             }
             TypeDoc content = new TypeDoc(main.getByteArray());
             content.setContentType(main.getMimeType());
             cdr.setDocumentPrincipal(content);
             cdr.setNomDocPrincipal(main.getFilename());
 
         } catch (PropertyException e) {
             throw new ClientException(e);
         } catch (IOException e) {
             throw new ClientException(e);
         }
 
         return cdr;
     }
 
     public DocumentModel getDocumentModel() {
         return doc;
     }
 
 }
