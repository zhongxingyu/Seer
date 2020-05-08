 package org.jenkinsci.plugins.sharedobjects;
 
 import hudson.DescriptorExtensionList;
 import hudson.model.Hudson;
 import net.sf.json.JSON;
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 import org.jenkinsci.plugins.sharedobjects.service.SharedObjectDataStore;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.util.List;
 
 import static hudson.Functions.checkPermission;
 
 /**
  * @author Gregory Boissinot
  */
 public class SharedObjectManagementResult {
 
     private SharedObjectType[] types;
 
     public SharedObjectManagementResult(SharedObjectType[] types) {
         this.types = types;
     }
 
     @SuppressWarnings("unused")
     public SharedObjectType[] getTypes() {
         return types;
     }
 
     @SuppressWarnings("unchecked")
     public DescriptorExtensionList getListSharedObjectsDescriptors() {
         return DescriptorExtensionList.createDescriptorList(Hudson.getInstance(), SharedObjectType.class);
     }
 
     @SuppressWarnings("unused")
     public void doSaveConfig(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
         checkPermission(Hudson.ADMINISTER);
 
         JSONObject submittedForm = req.getSubmittedForm();
 
         JSON typesJSON;
         try {
             typesJSON = submittedForm.getJSONArray("types");
         } catch (JSONException jsone) {
             typesJSON = submittedForm.getJSONObject("types");
         }
 
         List<SharedObjectType> types = req.bindJSONToList(SharedObjectType.class, typesJSON);
         SharedObjectType[] typesArray = types.toArray(new SharedObjectType[types.size()]);
 
         SharedObjectDataStore store = new SharedObjectDataStore();
         try {
             store.writeSharedObjectsFile(typesArray);
         } catch (SharedObjectException e) {
             e.printStackTrace();
         }
 
        rsp.sendRedirect2("../manage");
     }
 
 }
