 /*
  * Copyright (C) 2009 University of Washington
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.radicaldynamic.turboform.tasks;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.regex.Pattern;
 
 import org.ektorp.Attachment;
 import org.javarosa.core.model.FormDef;
 import org.javarosa.core.model.FormIndex;
 import org.javarosa.core.model.instance.FormInstance;
 import org.javarosa.core.services.transport.payload.ByteArrayPayload;
 import org.javarosa.form.api.FormEntryController;
 import org.javarosa.form.api.FormEntryModel;
 import org.javarosa.model.xform.XFormSerializingVisitor;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Base64;
 import android.util.Log;
 import android.webkit.MimeTypeMap;
 
 import com.radicaldynamic.turboform.application.Collect;
 import com.radicaldynamic.turboform.documents.InstanceDocument;
 import com.radicaldynamic.turboform.listeners.FormSavedListener;
 import com.radicaldynamic.turboform.utilities.FileUtils;
 
 /**
  * Background task for loading a form.
  * 
  * @author Carl Hartung (carlhartung@gmail.com)
  * @author Yaw Anokwa (yanokwa@gmail.com)
  */
 public class SaveToDiskTask extends AsyncTask<Void, String, Integer> {
     private final static String t = "SaveToDiskTask: ";
 
     private FormSavedListener mSavedListener;
     private Context mContext;
     private Boolean mSave;
     private Boolean mMarkCompleted;
     private String mInstanceId;
 
     public static final int SAVED = 500;
     public static final int SAVE_ERROR = 501;
     public static final int VALIDATE_ERROR = 502;
     public static final int VALIDATED = 503;
     public static final int SAVED_AND_EXIT = 504;
 
 
     /**
      * Initialize {@link FormEntryController} with {@link FormDef} from binary or from XML. If given
      * an instance, it will be used to fill the {@link FormDef}.
      */
     @Override
     protected Integer doInBackground(Void... nothing) {
         // Validation failed, pass specific failure
         int validateStatus = validateAnswers(mMarkCompleted);
         if (validateStatus != VALIDATED) {
             return validateStatus;
         }
 
         Collect.getInstance().getFormEntryController().getModel().getForm().postProcessInstance();
 
         if (mSave && exportData(mContext, mMarkCompleted)) {
             return SAVED_AND_EXIT;
         } else if (exportData(mContext, mMarkCompleted)) {
             return SAVED;
         }
 
         return SAVE_ERROR;
     }
 
 
     public boolean exportData(Context context, boolean markCompleted) {
         ByteArrayPayload payload;
         
         try {
             // Assume no binary data inside the model
             FormInstance datamodel = Collect.getInstance().getFormEntryController().getModel().getForm().getInstance();
             XFormSerializingVisitor serializer = new XFormSerializingVisitor();
             payload = (ByteArrayPayload) serializer.createSerializedPayload(datamodel);
 
             // Write out XML
             exportXmlFile(payload, markCompleted);
         } catch (IOException e) {
             Log.e(Collect.LOGTAG, t + "error creating serialized payload");
             e.printStackTrace();
             return false;
         }
 
         return true;
     }
 
 
     private boolean exportXmlFile(ByteArrayPayload payload, boolean markCompleted) {
         // Create data stream
         InputStream is = payload.getPayloadStream();
         int len = (int) payload.getLength();
 
         // Read from data stream
         byte[] data = new byte[len];
         
         try {
             int read = is.read(data, 0, len);
             
             if (read > 0) {
                 InstanceDocument instance = Collect.mDb.getDb().get(InstanceDocument.class, mInstanceId);   
                                 
                 if (markCompleted)
                     instance.setStatus(InstanceDocument.Status.complete);
                 else
                     instance.setStatus(InstanceDocument.Status.incomplete);
                 
                 // Save form data
                 instance.addInlineAttachment(new Attachment("xml", Base64.encodeToString(data, Base64.DEFAULT), "text/xml"));
                 
                 // Save media attachments
                 File cacheDir = new File(FileUtils.CACHE_PATH);
                 String[] fileNames = cacheDir.list();                           
                                             
                 for (String file : fileNames) {
                     Log.v(Collect.LOGTAG, t + mInstanceId + ": evaluating " + file + " for save to DB");
                     
                     if (Pattern.matches("^" + mInstanceId + "[.].*", file)) {                                
                         Log.d(Collect.LOGTAG, t + mInstanceId + ": attaching " + file);
                         
                         instance.addInlineAttachment(
                                 new Attachment(
                                         file, 
                                         Base64.encodeToString(FileUtils.getFileAsBytes(new File(FileUtils.CACHE_PATH + file)), Base64.DEFAULT), 
                                         MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.substring(file.lastIndexOf(".") + 1))));
                     }
                 }
                 
                 Collect.mDb.getDb().update(instance);   
                 
                 if (instance.getId().length() > 0) 
                     return true;
                 else 
                     return false;
             }
         } catch (IOException e) {
             Log.e(Collect.LOGTAG, t + "error reading from payload data stream");
             e.printStackTrace();
             return false;
         }
 
         return false;
     }
 
 
     @Override
     protected void onPostExecute(Integer result) {
         synchronized (this) {
             if (mSavedListener != null)
                 mSavedListener.savingComplete(result);
         }
     }
 
 
     public void setFormSavedListener(FormSavedListener fsl) {
         synchronized (this) {
             mSavedListener = fsl;
         }
     }
 
 
     public void setExportVars(Context context, String instanceId, Boolean saveAndExit, Boolean markCompleted) {        
         mContext = context;
         mSave = saveAndExit;
         mMarkCompleted = markCompleted;
         mInstanceId = instanceId;
     }
 
 
     /**
      * Goes through the entire form to make sure all entered answers comply with their constraints.
      * Constraints are ignored on 'jump to', so answers can be outside of constraints. We don't
      * allow saving to disk, though, until all answers conform to their constraints/requirements.
      * @param markCompleted
      * @return validatedStatus
      */
     private int validateAnswers(Boolean markCompleted) {
     	FormEntryController fec = Collect.getInstance().getFormEntryController();
         FormEntryModel fem = fec.getModel();
         FormIndex i = fem.getFormIndex();
 
         fec.jumpToIndex(FormIndex.createBeginningOfFormIndex());
 
         int event;
         
         while ((event = fec.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) {
             if (event != FormEntryController.EVENT_QUESTION) {
                 continue;
             } else {
                 int saveStatus = fec.answerQuestion(fem.getQuestionPrompt().getAnswerValue());
                 
                 if (markCompleted && saveStatus != FormEntryController.ANSWER_OK) { 
                     this.publishProgress(fem.getQuestionPrompt().getConstraintText(), Integer.toString(saveStatus));
         			return saveStatus;
                 }
             }
         }
 
         fec.jumpToIndex(i);
         
         return VALIDATED;
     }
 
     @Override
     protected void onProgressUpdate(String... values) 
     {
         Collect.getInstance().createConstraintToast(values[0], Integer.valueOf(values[1]).intValue());
     } 
 }
