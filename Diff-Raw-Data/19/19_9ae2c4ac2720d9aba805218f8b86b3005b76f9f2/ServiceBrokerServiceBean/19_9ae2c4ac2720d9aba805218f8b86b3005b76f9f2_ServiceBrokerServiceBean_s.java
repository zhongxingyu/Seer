 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uq.ilabs.servicebroker.service;
 
 import java.util.Arrays;
 import javax.ejb.LocalBean;
 import javax.ejb.Singleton;
 import uq.ilabs.library.lab.database.DBConnection;
 import uq.ilabs.library.lab.types.*;
 import uq.ilabs.library.lab.utilities.Logfile;
 import uq.ilabs.servicebroker.database.ExperimentsDB;
 import uq.ilabs.servicebroker.engine.ConfigProperties;
 import uq.ilabs.servicebroker.engine.types.LabServerInfo;
 import uq.ilabs.servicebroker.labserver.LabServerAPI;
 
 /**
  *
  * @author uqlpayne
  */
 @Singleton
 @LocalBean
 public class ServiceBrokerServiceBean {
 
     //<editor-fold defaultstate="collapsed" desc="Constants">
     private static final String STR_ClassName = ServiceBrokerServiceBean.class.getName();
     /*
      * String constants
      */
     private static final String STR_UserGroup = "DummyServiceBroker";
     /*
      * String constants for logfile messages
      */
     private static final String STRLOG_ServiceBrokerGuid_arg = "ServiceBrokerGuid: %s";
     private static final String STRLOG_LabServerGuid_arg = "LabServerGuid: %s";
     private static final String STRLOG_CouponIdPasskey_arg2 = "CouponId: %d  CouponPasskey: '%s'";
     private static final String STRLOG_ExperimentId_arg = " ExperimentId: %d";
     private static final String STRLOG_Success_arg = " Success: %s";
     private static final String STRLOG_LabStatus_arg2 = "Online: %s  Message: %s";
     //</editor-fold>
     //<editor-fold defaultstate="collapsed" desc="Variables">
     private ConfigProperties configProperties;
     private ExperimentsDB dbExperiments;
     //</editor-fold>
 
     /**
      * Constructor - Seems that this gets called when the project is deployed which is unexpected. To get around this,
      * check to see if the ServiceBroker has been initialised and the configuration properties set. Can't do logging
      * until the ServiceBroker has been initialised and the logger created.
      */
     public ServiceBrokerServiceBean() {
         final String STR_MethodName = "ServiceBrokerServiceBean";
 
         /*
          * Check if initialisation needs to be done
          */
         if (ServiceBrokerService.isInitialised() == true && this.configProperties == null) {
             Logfile.WriteCalled(STR_ClassName, STR_MethodName);
 
             /*
              * Save the configuration properties locally and process
              */
             this.configProperties = ServiceBrokerService.getConfigProperties();
 
             Logfile.Write(String.format(STRLOG_ServiceBrokerGuid_arg, this.configProperties.getServiceBrokerGuid()));
 
             try {
                 /*
                  * Create an instance of the database connection
                  */
                 DBConnection dbConnection = new DBConnection(this.configProperties.getDbDatabase());
                 if (dbConnection == null) {
                     throw new NullPointerException(DBConnection.class.getSimpleName());
                 }
                 dbConnection.setHost(this.configProperties.getDbHost());
                 dbConnection.setUser(this.configProperties.getDbUser());
                 dbConnection.setPassword(this.configProperties.getDbPassword());
 
                 /*
                  * Create instance of Experiments database API
                  */
                 this.dbExperiments = new ExperimentsDB(dbConnection);
                 if (this.dbExperiments == null) {
                     throw new NullPointerException(ExperimentsDB.class.getSimpleName());
                 }
             } catch (Exception ex) {
                 Logfile.WriteError(ex.toString());
             }
 
             Logfile.WriteCompleted(STR_ClassName, STR_MethodName);
         }
     }
 
     /**
      *
      * @param experimentId
      * @return
      */
     public boolean cancel(int experimentId) {
         final String STR_MethodName = "cancel";
         Logfile.WriteCalled(STR_ClassName, STR_MethodName,
                 String.format(STRLOG_ExperimentId_arg, experimentId));
 
         boolean success = false;
 
         try {
             /*
              * Get the LabServer for the specified experiment
              */
             String labServerGuid = this.dbExperiments.RetrieveByExperimentId(experimentId);
             if (labServerGuid != null) {
                 /*
                  * Pass to LabServer for processing
                  */
                 LabServerAPI labServerAPI = GetLabServerAPI(labServerGuid);
                 success = labServerAPI.Cancel(experimentId);
             }
         } catch (Exception ex) {
             Logfile.WriteError(ex.getMessage());
         }
 
         Logfile.WriteCompleted(STR_ClassName, STR_MethodName,
                 String.format(STRLOG_Success_arg, success));
 
         return success;
     }
 
     /**
      *
      * @param labServerGuid
      * @param priorityHint
      * @return
      */
     public edu.mit.ilab.WaitEstimate getEffectiveQueueLength(String labServerGuid, int priorityHint) {
         final String STR_MethodName = "getEffectiveQueueLength";
         Logfile.WriteCalled(STR_ClassName, STR_MethodName);
 
         edu.mit.ilab.WaitEstimate proxyWaitEstimate = null;
 
         try {
             /*
              * Pass to LabServer for processing
              */
             LabServerAPI labServerAPI = GetLabServerAPI(labServerGuid);
             WaitEstimate waitEstimate = labServerAPI.GetEffectiveQueueLength(STR_UserGroup, priorityHint);
 
             /*
              * Convert to return type
              */
             proxyWaitEstimate = new edu.mit.ilab.WaitEstimate();
             proxyWaitEstimate.setEffectiveQueueLength(waitEstimate.getEffectiveQueueLength());
             proxyWaitEstimate.setEstWait(waitEstimate.getEstWait());
         } catch (Exception ex) {
             Logfile.WriteError(ex.getMessage());
         }
 
         Logfile.WriteCompleted(STR_ClassName, STR_MethodName);
 
         return proxyWaitEstimate;
     }
 
     /**
      *
      * @param experimentId
      * @return
      */
     public edu.mit.ilab.LabExperimentStatus getExperimentStatus(int experimentId) {
         final String STR_MethodName = "getExperimentStatus";
         Logfile.WriteCalled(STR_ClassName, STR_MethodName,
                 String.format(STRLOG_ExperimentId_arg, experimentId));
 
         edu.mit.ilab.LabExperimentStatus proxyLabExperimentStatus = null;
 
         try {
             /*
              * Get the LabServer for the specified experiment
              */
             String labServerGuid = this.dbExperiments.RetrieveByExperimentId(experimentId);
             if (labServerGuid != null) {
                 /*
                  * Pass to LabServer for processing
                  */
                 LabServerAPI labServerAPI = GetLabServerAPI(labServerGuid);
                 LabExperimentStatus labExperimentStatus = labServerAPI.GetExperimentStatus(experimentId);
 
                 /*
                  * Convert to the return type
                  */
                 proxyLabExperimentStatus = new edu.mit.ilab.LabExperimentStatus();
                 proxyLabExperimentStatus.setMinTimetoLive(labExperimentStatus.getMinTimetoLive());
 
                 edu.mit.ilab.ExperimentStatus proxyExperimentStatus = new edu.mit.ilab.ExperimentStatus();
                 proxyExperimentStatus.setEstRemainingRuntime(labExperimentStatus.getExperimentStatus().getEstRemainingRuntime());
                 proxyExperimentStatus.setEstRuntime(labExperimentStatus.getExperimentStatus().getEstRuntime());
                 proxyExperimentStatus.setStatusCode(labExperimentStatus.getExperimentStatus().getStatusCode().ordinal());
 
                 edu.mit.ilab.WaitEstimate proxyWaitEstimate = new edu.mit.ilab.WaitEstimate();
                 proxyWaitEstimate.setEffectiveQueueLength(labExperimentStatus.getExperimentStatus().getWaitEstimate().getEffectiveQueueLength());
                 proxyWaitEstimate.setEstWait(labExperimentStatus.getExperimentStatus().getWaitEstimate().getEstWait());
                 proxyExperimentStatus.setWait(proxyWaitEstimate);
 
                 proxyLabExperimentStatus.setStatusReport(proxyExperimentStatus);
             }
         } catch (Exception ex) {
             Logfile.WriteError(ex.getMessage());
         }
 
         Logfile.WriteCompleted(STR_ClassName, STR_MethodName);
 
         return proxyLabExperimentStatus;
     }
 
     /**
      *
      * @param labServerGuid
      * @return
      */
     public java.lang.String getLabConfiguration(String labServerGuid) {
         final String STR_MethodName = "getLabConfiguration";
         Logfile.WriteCalled(STR_ClassName, STR_MethodName);
 
         String xmlLabConfiguration = null;
 
         try {
             /*
              * Pass to LabServer for processing
              */
             LabServerAPI labServerAPI = GetLabServerAPI(labServerGuid);
             xmlLabConfiguration = labServerAPI.GetLabConfiguration(STR_UserGroup);
         } catch (Exception ex) {
             Logfile.WriteError(ex.getMessage());
         }
 
         Logfile.WriteCompleted(STR_ClassName, STR_MethodName);
 
         return xmlLabConfiguration;
     }
 
     /**
      *
      * @param labServerGuid
      * @return
      */
     public java.lang.String getLabInfo(String labServerGuid) {
         final String STR_MethodName = "getLabInfo";
         Logfile.WriteCalled(STR_ClassName, STR_MethodName);
 
         String labInfo = null;
 
         try {
             /*
              * Pass to LabServer for processing
              */
             LabServerAPI labServerAPI = GetLabServerAPI(labServerGuid);
             labInfo = labServerAPI.GetLabInfo();
         } catch (Exception ex) {
             Logfile.WriteError(ex.getMessage());
         }
 
         Logfile.WriteCompleted(STR_ClassName, STR_MethodName);
 
         return labInfo;
     }
 
     /**
      *
      * @param labServerGuid
      * @return
      */
     public edu.mit.ilab.LabStatus getLabStatus(String labServerGuid) {
         final String STR_MethodName = "getLabStatus";
         Logfile.WriteCalled(STR_ClassName, STR_MethodName,
                 String.format(STRLOG_LabServerGuid_arg, labServerGuid));
 
         edu.mit.ilab.LabStatus proxyLabStatus = null;
 
         try {
             /*
              * Pass to LabServer for processing
              */
             LabServerAPI labServerAPI = GetLabServerAPI(labServerGuid);
             LabStatus labStatus = labServerAPI.GetLabStatus();
 
             /*
              * Convert to the return type
              */
             proxyLabStatus = new edu.mit.ilab.LabStatus();
             proxyLabStatus.setOnline(labStatus.isOnline());
             proxyLabStatus.setLabStatusMessage(labStatus.getLabStatusMessage());
         } catch (Exception ex) {
             Logfile.WriteError(ex.getMessage());
         }
 
         Logfile.WriteCompleted(STR_ClassName, STR_MethodName,
                 String.format(STRLOG_LabStatus_arg2, proxyLabStatus.isOnline(), proxyLabStatus.getLabStatusMessage()));
 
         return proxyLabStatus;
     }
 
     /**
      *
      * @param experimentId
      * @return
      */
     public edu.mit.ilab.ResultReport retrieveResult(int experimentId) {
         final String STR_MethodName = "retrieveResult";
         Logfile.WriteCalled(STR_ClassName, STR_MethodName,
                 String.format(STRLOG_ExperimentId_arg, experimentId));
 
         edu.mit.ilab.ResultReport proxyResultReport = null;
 
         try {
             /*
              * Get the LabServer for the specified experiment
              */
             String labServerGuid = this.dbExperiments.RetrieveByExperimentId(experimentId);
             if (labServerGuid != null) {
                 /*
                  * Pass to LabServer for processing
                  */
                 LabServerAPI labServerAPI = GetLabServerAPI(labServerGuid);
                 ResultReport resultReport = labServerAPI.RetrieveResult(experimentId);
 
                 /*
                  * Convert to the return type
                  */
                 proxyResultReport = new edu.mit.ilab.ResultReport();
                 proxyResultReport.setErrorMessage(resultReport.getErrorMessage());
                 proxyResultReport.setExperimentResults(resultReport.getXmlExperimentResults());
                 proxyResultReport.setStatusCode(resultReport.getStatusCode().ordinal());
                 if (resultReport.getWarningMessages() != null) {
                     edu.mit.ilab.ArrayOfString proxyWarningMessages = new edu.mit.ilab.ArrayOfString();
                     proxyWarningMessages.getString().addAll(Arrays.asList(resultReport.getWarningMessages()));
                     proxyResultReport.setWarningMessages(proxyWarningMessages);
                 }
                 proxyResultReport.setXmlBlobExtension(resultReport.getXmlBlobExtension());
                 proxyResultReport.setXmlResultExtension(resultReport.getXmlResultExtension());
             } else {
                 proxyResultReport = new edu.mit.ilab.ResultReport();
                 proxyResultReport.setStatusCode(StatusCodes.Unknown.ordinal());
             }
         } catch (Exception ex) {
             Logfile.WriteError(ex.getMessage());
         }
 
         Logfile.WriteCompleted(STR_ClassName, STR_MethodName);
 
         return proxyResultReport;
     }
 
     /**
      *
      * @param labServerGuid
      * @param experimentSpecification
      * @param priorityHint
      * @param emailNotification
      * @return
      */
     public edu.mit.ilab.ClientSubmissionReport submit(String labServerGuid, String experimentSpecification, int priorityHint, boolean emailNotification) {
         final String STR_MethodName = "submit";
         Logfile.WriteCalled(STR_ClassName, STR_MethodName);
 
         edu.mit.ilab.ClientSubmissionReport proxyClientSubmissionReport = null;
 
         try {
             /*
              * Get the next experiment Id from the experiment database
              */
             int experimentId = this.dbExperiments.GetNextExperimentId();
 
             /*
              * Pass to LabServer for processing
              */
             LabServerAPI labServerAPI = this.GetLabServerAPI(labServerGuid);
             SubmissionReport submissionReport = labServerAPI.Submit(experimentId, experimentSpecification, STR_UserGroup, priorityHint);
 
             /*
              * Check if experiment was accepted
              */
             if (submissionReport.getValidationReport().isAccepted() == true) {
                 /*
                  * Add LabServer to the experiment database
                  */
                 this.dbExperiments.Add(labServerGuid);
             }
 
             /*
              * Convert to the return type
              */
             proxyClientSubmissionReport = new edu.mit.ilab.ClientSubmissionReport();
             proxyClientSubmissionReport.setExperimentID(submissionReport.getExperimentId());
             proxyClientSubmissionReport.setMinTimeToLive(submissionReport.getMinTimeToLive());
 
             edu.mit.ilab.ValidationReport proxyValidationReport = new edu.mit.ilab.ValidationReport();
             proxyValidationReport.setAccepted(submissionReport.getValidationReport().isAccepted());
             proxyValidationReport.setErrorMessage(submissionReport.getValidationReport().getErrorMessage());
             proxyValidationReport.setEstRuntime(submissionReport.getValidationReport().getEstRuntime());
             if (submissionReport.getValidationReport().getWarningMessages() != null) {
                 edu.mit.ilab.ArrayOfString proxyWarningMessages = new edu.mit.ilab.ArrayOfString();
                 proxyWarningMessages.getString().addAll(Arrays.asList(submissionReport.getValidationReport().getWarningMessages()));
                 proxyValidationReport.setWarningMessages(proxyWarningMessages);
             }
             proxyClientSubmissionReport.setVReport(proxyValidationReport);
 
             edu.mit.ilab.WaitEstimate proxyWaitEstimate = new edu.mit.ilab.WaitEstimate();
             proxyWaitEstimate.setEffectiveQueueLength(submissionReport.getWaitEstimate().getEffectiveQueueLength());
             proxyWaitEstimate.setEstWait(submissionReport.getWaitEstimate().getEstWait());
             proxyClientSubmissionReport.setWait(proxyWaitEstimate);
         } catch (Exception ex) {
             Logfile.WriteError(ex.getMessage());
         }
 
         Logfile.WriteCompleted(STR_ClassName, STR_MethodName);
 
         return proxyClientSubmissionReport;
     }
 
     /**
      *
      * @param labServerGuid
      * @param experimentSpecification
      * @return
      */
     public edu.mit.ilab.ValidationReport validate(String labServerGuid, String experimentSpecification) {
         final String STR_MethodName = "validate";
         Logfile.WriteCalled(STR_ClassName, STR_MethodName);
 
         edu.mit.ilab.ValidationReport proxyValidationReport = null;
 
         try {
             /*
              * Pass to LabServer for processing
              */
             LabServerAPI labServerAPI = this.GetLabServerAPI(labServerGuid);
             ValidationReport validationReport = labServerAPI.Validate(experimentSpecification, labServerGuid);
 
             /*
              * Convert to the return type
              */
             proxyValidationReport = new edu.mit.ilab.ValidationReport();
             proxyValidationReport.setAccepted(validationReport.isAccepted());
             proxyValidationReport.setErrorMessage(validationReport.getErrorMessage());
             proxyValidationReport.setEstRuntime(validationReport.getEstRuntime());
             if (validationReport.getWarningMessages() != null) {
                 edu.mit.ilab.ArrayOfString proxyWarningMessages = new edu.mit.ilab.ArrayOfString();
                 proxyWarningMessages.getString().addAll(Arrays.asList(validationReport.getWarningMessages()));
                 proxyValidationReport.setWarningMessages(proxyWarningMessages);
             }
         } catch (Exception ex) {
             Logfile.WriteError(ex.getMessage());
         }
 
         Logfile.WriteCompleted(STR_ClassName, STR_MethodName);
 
         return proxyValidationReport;
     }
 
     /**
      *
      * @param experimentId
      */
     public void notify(int experimentId) {
         final String STR_MethodName = "notify";
         Logfile.WriteCalled(STR_ClassName, STR_MethodName,
                 String.format(STRLOG_ExperimentId_arg, experimentId));
 
         this.retrieveResult(experimentId);
 
         Logfile.WriteCompleted(STR_ClassName, STR_MethodName);
     }
 
     /**
      *
      * @param couponId
      * @param passkey
      * @return boolean
      */
     public boolean Authenticate(long couponId, String couponPasskey) {
         /*
          * Assume this will succeed
          */
         boolean success = true;
 
         /*
          * Check if authenticating
          */
         if (this.configProperties.isAuthenticating()) {
             if (this.configProperties.isLogAuthentication()) {
                 Logfile.Write(String.format(STRLOG_CouponIdPasskey_arg2, couponId, couponPasskey));
             }
 
             /*
              * Check the identifier and passkey
              */
             success = (couponId == this.configProperties.getCouponId())
                     && this.configProperties.getCouponPasskey().equalsIgnoreCase(couponPasskey);
         }
 
         return success;
     }
 
     /**
      *
      * @param labServerGuid
      * @return LabServerAPI
      * @throws Exception
      */
     private LabServerAPI GetLabServerAPI(String labServerGuid) throws Exception {
         LabServerAPI labServerAPI = null;
 
         /*
          * Get LabServer information
          */
         LabServerInfo labServerInfo = this.configProperties.GetLabServerInfo(labServerGuid);
         if (labServerInfo != null) {
             /*
              * Create an instance of the LabServer API for this service url
              */
             labServerAPI = new LabServerAPI(labServerInfo.getServiceUrl());
             labServerAPI.setIdentifier(this.configProperties.getServiceBrokerGuid());
             labServerAPI.setPasskey(labServerInfo.getOutgoingPasskey());
         }
 
         return labServerAPI;
     }
 }
