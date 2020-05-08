 /*
  * Copyright (C) 2013 Project-Phoenix
  * 
  * This file is part of library.
  * 
  * library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.phoenix.rs.entity;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.ws.rs.core.GenericEntity;
 
 import org.joda.time.DateTime;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 
 import de.phoenix.rs.EntityUtil;
 import de.phoenix.rs.entity.PhoenixSubmissionResult.SubmissionStatus;
 import de.phoenix.rs.key.AddToEntity;
 import de.phoenix.rs.key.Key;
 import de.phoenix.rs.key.PhoenixEntity;
 
 public class PhoenixSubmission implements PhoenixEntity {
 
     /** URI of the submission resource */
     public static final String WEB_RESOURCE_ROOT = "submission";
     /**
      * SubURI of the submission resource to submit a submission for a task
      * 
      * @param {@link PhoenixSubmission} Submission to submit
      * 
      * @return {@link PhoenixSubmissionResult} <br>
      *         Result containing the {@link SubmissionStatus} and the status
      *         text as a string
      * @deprecated Use {@link PhoenixTask#WEB_RESOURCE_ADD_SUBMISSION} instead
      */
     public static final String WEB_RESOURCE_SUBMIT = "submit";
     /**
      * SubURI of the submission resource to get all submissions by one task
      * 
      * @param {@link PhoenixTask} PhoenixTask holding submissions
      * 
      * @return List<{@link PhoenixSubmission}> <br>
      *         Submissions for the task
      * 
      */
     public static final String WEB_RESOURCE_GET_TASK_SUBMISSIONS = "getByTask";
 
     @Key
     private DateTime date;
 
     @Key
     private int status;
 
     @Key
     private String statusText;
 
     private PhoenixTask task;
 
     private List<PhoenixAttachment> attachments;
     private List<PhoenixText> texts;
 
     /**
      * Empty constructor for json transport
      */
     public PhoenixSubmission() {
     }
 
     /**
      * Constructor for the client
      * 
      * @param task
      *            The task the submission is for
      * @param fileAttachments
      *            Binary files attached to this submission
      * @param fileTexts
      *            Text files attached to this submission
      * @throws IOException
      * @deprecated Use {@link #PhoenixSubmission(List, List)} in combination
      *             with {@link AddToEntity} and the
      *             {@link PhoenixTask#submitResource(Client, String)} resource
      *             to upload a submission. This will also send the complete task
      *             to the database, but only the title is neccessary for this!
      */
 
     public PhoenixSubmission(PhoenixTask task, List<File> fileAttachments, List<File> fileTexts) throws IOException {
 
         this.task = task;
         // Don't send the attachments via JSON
         this.task.setAttachments(Collections.<PhoenixAttachment> emptyList());
         this.task.setPattern(Collections.<PhoenixText> emptyList());
 
         this.attachments = new ArrayList<PhoenixAttachment>(fileAttachments.size());
         for (File attachment : fileAttachments) {
             attachments.add(new PhoenixAttachment(attachment, attachment.getName()));
         }
 
         this.texts = new ArrayList<PhoenixText>(fileTexts.size());
         for (File text : fileTexts) {
             texts.add(new PhoenixText(text, text.getName()));
         }
     }
 
     public PhoenixSubmission(List<File> fileAttachments, List<File> fileTexts) throws IOException {
         this.attachments = new ArrayList<PhoenixAttachment>(fileAttachments.size());
         for (File attachment : fileAttachments) {
             attachments.add(new PhoenixAttachment(attachment, attachment.getName()));
         }
 
         this.texts = new ArrayList<PhoenixText>(fileTexts.size());
         for (File text : fileTexts) {
             texts.add(new PhoenixText(text, text.getName()));
         }
     }
 
     /**
      * Constructor for the server
      * 
      * @param date
      * @param task
      * @param attachments
      * @param texts
      */
     public PhoenixSubmission(DateTime date, PhoenixTask task, int status, String statusText, List<PhoenixAttachment> attachments, List<PhoenixText> texts) {
         this.date = date;
         this.task = task;
         this.attachments = attachments;
         this.texts = texts;
         this.status = status;
         this.statusText = statusText;
     }
 
     /**
      * @return Copy of list to the texts
      */
     public List<PhoenixText> getTexts() {
         return new ArrayList<PhoenixText>(texts);
     }
 
     /**
      * @return Copy of list to the binary attachments
      */
     public List<PhoenixAttachment> getAttachments() {
         return new ArrayList<PhoenixAttachment>(attachments);
     }
 
     /**
      * @return The creation date
      */
     public DateTime getDate() {
         return date;
     }
 
     /**
      * @return The task the submission is for
      */
     public PhoenixTask getTask() {
         return task;
     }
 
     public int getStatus() {
         return status;
     }
 
     public String getStatusText() {
         return statusText;
     }
 
     /**
      * Generic Type for {@link PhoenixSubmission}
      * 
      * @deprecated No longer necessary for sending and receiving lists
      */
     private final static GenericType<List<PhoenixSubmission>> GENERIC_TYPE = new GenericType<List<PhoenixSubmission>>() {
     };
 
     /**
      * Convert a list to an generic entity to send it via JX-RS
      * 
      * @param list
      *            List containing {@link PhoenixSubmission}
      * @return Generic Entity to send via JX-RS
      * @deprecated No longer necessary for sending and receiving lists
      */
     public final static GenericEntity<List<PhoenixSubmission>> toSendableList(List<PhoenixSubmission> list) {
         return new GenericEntity<List<PhoenixSubmission>>(list, GENERIC_TYPE.getType());
     }
 
     /**
      * Extract the entity from the response and convert it to a normal list
      * 
      * @param response
      *            Response containg an list from JX-RS
      * @return List containg values as {@link PhoenixSubmission}
      * @deprecated No longer necessary for sending and receiving lists. Use
      *             instead {@link EntityUtil#extractEntityList(ClientResponse)}
      */
     public final static List<PhoenixSubmission> fromSendableList(ClientResponse response) {
         return response.getEntity(GENERIC_TYPE);
     }
 
     @Deprecated
     public static WebResource submitResource(Client client, String baseURL) {
         return base(client, baseURL).path(WEB_RESOURCE_SUBMIT);
     }
 
     public static WebResource getByTaskResource(Client client, String baseURL) {
         return base(client, baseURL).path(WEB_RESOURCE_GET_TASK_SUBMISSIONS);
     }
 
     private static WebResource base(Client client, String baseURL) {
         return client.resource(baseURL).path(WEB_RESOURCE_ROOT);
     }
 
     @Override
     public String toString() {
        return String.format("PhoenixSubmission={Date=%s;Status=%d;StatusText=%s;Task=%s;Attachments=%s;Texts=%s}", date, status, statusText, task, attachments, texts);
     }
 }
