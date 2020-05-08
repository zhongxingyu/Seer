 /**
  * 
  */
 package pt.ist.maidSyncher.api.activeCollab.interfaces;
 
 import java.io.IOException;
 
 import org.json.simple.JSONObject;
 
 import pt.ist.maidSyncher.api.activeCollab.ACObject;
 
 /**
  * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 3 de Abr de 2013
  * 
  * 
  *         Interface used to abstract who makes the requests
  */
 public interface RequestProcessor {
 
     Object processGet(String _url) throws IOException;
 
     JSONObject processPost(ACObject acObject, String relativePath) throws IOException;
 
     String getBasicUrlForPath(String string);
 
    JSONObject processPost(String content, String path) throws IOException;
 
 }
