 /* SVN FILE: $Id: BuildBean.java 5970 2013-01-04 15:50:31Z jvuccolo $ */
 package edu.psu.iam.cpr.core.database.beans;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 /**
  *
  * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 United States License. To
  * view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/us/ or send a letter to Creative
  * Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  *
  * @package edu.psu.iam.cpr.core.database.beans
  * @author $Author: jvuccolo $
  * @version $Rev: 5970 $
  * @lastrevision $Date: 2013-01-04 10:50:31 -0500 (Fri, 04 Jan 2013) $
  */
 
 @Entity
 @Table(name="message_log_history")
 public class MessageLogHistory implements Serializable {
 
         /** Contains the serialized UID */
         private static final long serialVersionUID = 1L;
 
         /** Contains the messageReceivedTimestamp. */
         @Column(name="message_received_timestamp", nullable=true)
         private Date messageReceivedTimestamp;
 
         /** Contains the messageLogKey. */
         @Column(name="message_log_key", nullable=false)
         private Long messageLogKey;
 
         /** Contains the createdOn. */
         @Column(name="created_on", nullable=false)
         private Date createdOn;
 
         /** Contains the errorMessage. */
         @Column(name="error_message", nullable=true, length=1000)
         private String errorMessage;
 
         /** Contains the errorCode. */
         @Column(name="error_code", nullable=true, length=100)
         private String errorCode;
 
         /** Contains the messageId. */
         @Column(name="message_id", nullable=true, length=100)
         private String messageId;
 
         /** Contains the tryNumber. */
         @Column(name="try_number", nullable=true)
         private Long tryNumber;
 
         /** Contains the messageSentTimestamp. */
         @Column(name="message_sent_timestamp", nullable=false)
         private Date messageSentTimestamp;
 
         /** Contains the messageReceived. */
         @Column(name="message_received", nullable=true, length=1000)
         private String messageReceived;
 
         /** Contains the messageLogHistoryKey. */
         @Id
         @Column(name="message_log_history_key", nullable=false)
         @GeneratedValue(strategy=GenerationType.AUTO, generator="seq_message_log_history")
         @SequenceGenerator(name="seq_message_log_history", sequenceName="seq_message_log_history", allocationSize = 1, initialValue= 1)
         private Long messageLogHistoryKey;
 
         /** Contains the lastUpdateOn. */
         @Column(name="last_update_on", nullable=false)
         private Date lastUpdateOn;
 
         /**
          * Constructor
          */
         public MessageLogHistory() {
             super();
         }
 
         /**
          * @return the messageReceivedTimestamp
          */
         public Date getMessageReceivedTimestamp() {
                 return messageReceivedTimestamp;
         }
 
         /**
          * @param messageReceivedTimestamp the messageReceivedTimestamp to set.
          */
         public void setMessageReceivedTimestamp(Date messageReceivedTimestamp) {
                 this.messageReceivedTimestamp = messageReceivedTimestamp;
         }
 
         /**
          * @return the messageLogKey
          */
         public Long getMessageLogKey() {
                 return messageLogKey;
         }
 
         /**
          * @param messageLogKey the messageLogKey to set.
          */
         public void setMessageLogKey(Long messageLogKey) {
                 this.messageLogKey = messageLogKey;
         }
 
         /**
          * @return the createdOn
          */
         public Date getCreatedOn() {
                 return createdOn;
         }
 
         /**
          * @param createdOn the createdOn to set.
          */
         public void setCreatedOn(Date createdOn) {
                 this.createdOn = createdOn;
         }
 
         /**
          * @return the errorMessage
          */
         public String getErrorMessage() {
                 return errorMessage;
         }
 
         /**
          * @param errorMessage the errorMessage to set.
          */
         public void setErrorMessage(String errorMessage) {
                 this.errorMessage = errorMessage;
         }
 
         /**
          * @return the errorCode
          */
         public String getErrorCode() {
                 return errorCode;
         }
 
         /**
          * @param errorCode the errorCode to set.
          */
         public void setErrorCode(String errorCode) {
                 this.errorCode = errorCode;
         }
 
         /**
          * @return the messageId
          */
         public String getMessageId() {
                 return messageId;
         }
 
         /**
          * @param messageId the messageId to set.
          */
         public void setMessageId(String messageId) {
                 this.messageId = messageId;
         }
 
         /**
          * @return the tryNumber
          */
         public Long getTryNumber() {
                 return tryNumber;
         }
 
         /**
          * @param tryNumber the tryNumber to set.
          */
         public void setTryNumber(Long tryNumber) {
                 this.tryNumber = tryNumber;
         }
 
         /**
          * @return the messageSentDatetime
          */
         public Date getMessageSentTimestamp() {
                 return messageSentTimestamp;
         }
 
         /**
         * @param messageSentDatetime the messageSentDatetime to set.
          */
         public void setMessageSentTimestamp(Date messageSentTimestamp) {
                 this.messageSentTimestamp = messageSentTimestamp;
         }
 
         /**
          * @return the messageReceived
          */
         public String getMessageReceived() {
                 return messageReceived;
         }
 
         /**
          * @param messageReceived the messageReceived to set.
          */
         public void setMessageReceived(String messageReceived) {
                 this.messageReceived = messageReceived;
         }
 
         /**
          * @return the messageLogHistoryKey
          */
         public Long getMessageLogHistoryKey() {
                 return messageLogHistoryKey;
         }
 
         /**
          * @param messageLogHistoryKey the messageLogHistoryKey to set.
          */
         public void setMessageLogHistoryKey(Long messageLogHistoryKey) {
                 this.messageLogHistoryKey = messageLogHistoryKey;
         }
 
         /**
          * @return the lastUpdateOn
          */
         public Date getLastUpdateOn() {
                 return lastUpdateOn;
         }
 
         /**
          * @param lastUpdateOn the lastUpdateOn to set.
          */
         public void setLastUpdateOn(Date lastUpdateOn) {
                 this.lastUpdateOn = lastUpdateOn;
         }
 
 }
