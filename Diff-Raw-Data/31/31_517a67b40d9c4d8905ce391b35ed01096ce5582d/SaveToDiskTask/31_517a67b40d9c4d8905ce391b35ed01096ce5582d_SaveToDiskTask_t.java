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
 
 package com.radicaldynamic.groupinform.tasks;
 
import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.regex.Pattern;
 
 import org.ektorp.Attachment;
 import org.ektorp.AttachmentInputStream;
 import org.ektorp.DbAccessException;
 import org.javarosa.core.model.FormDef;
 import org.javarosa.core.model.FormIndex;
 import org.javarosa.core.model.instance.FormInstance;
 import org.javarosa.core.services.transport.payload.ByteArrayPayload;
 import org.javarosa.form.api.FormEntryController;
 import org.javarosa.form.api.FormEntryModel;
 import org.javarosa.model.xform.XFormSerializingVisitor;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import android.webkit.MimeTypeMap;
 
 import com.couchone.libcouch.Base64Coder;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.documents.FormInstanceDocument;
 import com.radicaldynamic.groupinform.listeners.FormSavedListener;
 import com.radicaldynamic.groupinform.utilities.FileUtils;
 
 /**
  * Background task for saving a form instance.
  * 
  * @author Carl Hartung (carlhartung@gmail.com)
  * @author Yaw Anokwa (yanokwa@gmail.com)
  */
 public class SaveToDiskTask extends AsyncTask<Void, String, Integer> 
 {
     private final static String t = "SaveToDiskTask: ";
 
     private FormSavedListener mSavedListener;
 
     private Boolean mSave;
     private Boolean mMarkCompleted;
 
     private FormInstanceDocument mFormInstanceDoc;
 
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
     protected Integer doInBackground(Void... nothing) 
     {
         // Validation failed, pass specific failure
         int validateStatus = validateAnswers(mMarkCompleted);
         
         if (validateStatus != VALIDATED) {
             return validateStatus;
         }
 
         Collect.getInstance().getFormEntryController().getModel().getForm().postProcessInstance();
 
         if (mSave && exportData(mMarkCompleted)) {
             return SAVED_AND_EXIT;
         } else if (exportData(mMarkCompleted)) {
             return SAVED;
         }
 
         return SAVE_ERROR;
     }
 
     @Override
     protected void onPostExecute(Integer result)
     {
         synchronized (this) {
             if (mSavedListener != null)
                 mSavedListener.savingComplete(result);
         }
     }
 
     @Override
     protected void onProgressUpdate(String... values) 
     {
         Collect.getInstance().createConstraintToast(values[0], Integer.valueOf(values[1]).intValue());
     }
 
     public boolean exportData(boolean markCompleted)
     {
         final String tt = t + "exportData(): ";
         
         ByteArrayPayload payload;
         
         try {
             // Assume no binary data inside the model
             FormInstance datamodel = Collect.getInstance().getFormEntryController().getModel().getForm().getInstance();
             XFormSerializingVisitor serializer = new XFormSerializingVisitor();
             payload = (ByteArrayPayload) serializer.createSerializedPayload(datamodel);
     
             // Write out XML
             return exportXmlFile(payload, markCompleted);
         } catch (IOException e) {
             Log.e(Collect.LOGTAG, tt + "error creating serialized payload");
             e.printStackTrace();
             return false;
         }
     }
 
     public void setExportVars(FormInstanceDocument formInstanceDoc, Boolean saveAndExit, Boolean markCompleted) 
     {
         mFormInstanceDoc = formInstanceDoc;
         mMarkCompleted = markCompleted;
         mSave = saveAndExit;
     }
 
     public void setFormSavedListener(FormSavedListener fsl)
     {
         synchronized (this) {
             mSavedListener = fsl;
         }
     }
 
     private boolean exportXmlFile(ByteArrayPayload payload, boolean markCompleted)
     {
         final String tt = t + "exportXmlFile(): ";
         
         boolean result = false;
         
         // Create data stream
         InputStream is = payload.getPayloadStream();
         int len = (int) payload.getLength();
     
         // Read from data stream
         byte[] data = new byte[len];
         
         try {
             int read = is.read(data, 0, len);
             
             if (read > 0) {
                 if (markCompleted)
                     mFormInstanceDoc.setStatus(FormInstanceDocument.Status.complete);
                 else
                     mFormInstanceDoc.setStatus(FormInstanceDocument.Status.draft);
                 
                 // Save form data
                 mFormInstanceDoc.addInlineAttachment(new Attachment("xml", new String(Base64Coder.encode(data)).toString(), "text/xml"));
                 Collect.getInstance().getDbService().getDb().update(mFormInstanceDoc);
                 
                 // Save media attachments one by one
                 File cacheDir = new File(FileUtils.EXTERNAL_CACHE);
                 String[] fileNames = cacheDir.list();                           
                                             
                 for (String file : fileNames) {
                     Log.v(Collect.LOGTAG, tt + mFormInstanceDoc.getId() + ": evaluating " + file + " for save to DB");
                     
                     if (Pattern.matches("^" + mFormInstanceDoc.getId() + "[.].*", file)) {
                         Log.d(Collect.LOGTAG, tt + mFormInstanceDoc.getId() + ": attaching " + file);
                         
                         // Make sure we have the most current revision number
                         FormInstanceDocument document = Collect.getInstance().getDbService().getDb().get(FormInstanceDocument.class, mFormInstanceDoc.getId());
 
                        File f = new File(FileUtils.EXTERNAL_CACHE, file);
                        FileInputStream fis = new FileInputStream(f);
                         String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.substring(file.lastIndexOf(".") + 1));
                         
                        AttachmentInputStream a = new AttachmentInputStream(file, fis, contentType, f.length());
                         
                         // Must use the revision number (why?) http://code.google.com/p/ektorp/issues/detail?id=28
                         Collect.getInstance().getDbService().getDb().createAttachment(document.getId(), document.getRevision(), a);
                         
                         a.close();
                         fis.close();
                     }
                 }
 
                 if (mFormInstanceDoc.getId().length() > 0) {
                     result = true;
                 }
             }
         } catch (DbAccessException e) {
             Log.e(Collect.LOGTAG, tt + "unable to access database: " + e.toString());
         } catch (IOException e) {
             Log.e(Collect.LOGTAG, tt + "error reading from payload data stream");
             e.printStackTrace();
         }
         
         return result;
     }
 
     /**
      * Goes through the entire form to make sure all entered answers comply with their constraints.
      * Constraints are ignored on 'jump to', so answers can be outside of constraints. We don't
      * allow saving to disk, though, until all answers conform to their constraints/requirements.
      * 
      * @param markCompleted
      * @return validatedStatus
      */
     private int validateAnswers(Boolean markCompleted)
     {
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
 }
