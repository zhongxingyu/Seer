 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2006 - 2012 Pentaho Corporation.  All rights reserved.
  *
  */
 package org.pentaho.platform.plugin.kettle;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.pentaho.commons.connection.IPentahoResultSet;
 import org.pentaho.commons.connection.memory.MemoryMetaData;
 import org.pentaho.commons.connection.memory.MemoryResultSet;
 import org.pentaho.di.core.exception.KettleException;
 import org.pentaho.di.core.exception.KettleSecurityException;
 import org.pentaho.di.core.exception.KettleStepException;
 import org.pentaho.di.core.exception.KettleValueException;
 import org.pentaho.di.core.logging.CentralLogStore;
 import org.pentaho.di.core.logging.Log4jBufferAppender;
 import org.pentaho.di.core.logging.LogLevel;
 import org.pentaho.di.core.logging.LogWriter;
 import org.pentaho.di.core.parameters.NamedParams;
 import org.pentaho.di.core.parameters.UnknownParamException;
 import org.pentaho.di.core.plugins.PluginRegistry;
 import org.pentaho.di.core.plugins.RepositoryPluginType;
 import org.pentaho.di.core.row.RowMeta;
 import org.pentaho.di.core.row.RowMetaInterface;
 import org.pentaho.di.core.row.ValueMetaInterface;
 import org.pentaho.di.core.variables.VariableSpace;
 import org.pentaho.di.core.xml.XMLHandler;
 import org.pentaho.di.core.xml.XMLHandlerCache;
 import org.pentaho.di.job.Job;
 import org.pentaho.di.job.JobConfiguration;
 import org.pentaho.di.job.JobExecutionConfiguration;
 import org.pentaho.di.job.JobMeta;
 import org.pentaho.di.repository.RepositoriesMeta;
 import org.pentaho.di.repository.Repository;
 import org.pentaho.di.repository.RepositoryMeta;
 import org.pentaho.di.trans.RowProducer;
 import org.pentaho.di.trans.Trans;
 import org.pentaho.di.trans.TransConfiguration;
 import org.pentaho.di.trans.TransExecutionConfiguration;
 import org.pentaho.di.trans.TransMeta;
 import org.pentaho.di.trans.step.RowListener;
 import org.pentaho.di.trans.step.StepMetaDataCombi;
 import org.pentaho.di.www.CarteSingleton;
 import org.pentaho.platform.api.action.IAction;
 import org.pentaho.platform.api.action.ILoggingAction;
 import org.pentaho.platform.api.action.IVarArgsAction;
 import org.pentaho.platform.api.engine.ActionExecutionException;
 import org.pentaho.platform.api.engine.ActionValidationException;
 import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
 import org.pentaho.platform.api.repository2.unified.RepositoryFile;
 import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
 import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
 import org.pentaho.platform.engine.core.system.PentahoSystem;
 import org.pentaho.platform.plugin.action.kettle.KettleSystemListener;
 import org.pentaho.platform.plugin.action.messages.Messages;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * An adaptation of KettleComponent to the lightweight PojoComponent/IAction framework
  * 
  * @author jdixon, mdamour, aphillips
  */
 
 /*
  * Legitimate outputs: EXECUTION_STATUS_OUTPUT - (execution-status) [JOB | TRANS] Returns the resultant execution status
  * 
  * EXECUTION_LOG_OUTPUT - (execution-log) [JOB | TRANS] Returns the resultant log
  * 
  * TRANSFORM_SUCCESS_OUTPUT - (transformation-written) [Requires MONITORSTEP to be defined] [TRANS] Returns a "result-set" for all successful rows written
  * (Unless error handling is not defined for the specified step, in which case ALL rows are returned here)
  * 
  * TRANSFORM_ERROR_OUTPUT - (transformation-errors) [Requires MONITORSTEP to be defined] [TRANS] Returns a "result-set" for all rows written that have caused an
  * error
  * 
  * TRANSFORM_SUCCESS_COUNT_OUTPUT - (transformation-written-count) [Requires MONITORSTEP to be defined] [TRANS] Returns a count of all rows returned in
  * TRANSFORM_SUCCESS_OUTPUT
  * 
  * TRANSFORM_ERROR_COUNT_OUTPUT - (transformation-errors-count) [Requires MONITORSTEP to be defined] [TRANS] Returns a count of all rows returned in
  * TRANSFORM_ERROR_OUTPUT
  * 
  * Legitimate inputs: MONITORSTEP Takes the name of the step from which success and error rows can be detected
  * 
  * KETTLELOGLEVEL Sets the logging level to be used in the EXECUTION_LOG_OUTPUT Valid settings: basic detail error debug minimal rowlevel
  */
 public class PdiAction implements IAction, IVarArgsAction, ILoggingAction, RowListener {
 
   private static final String SINGLE_DI_SERVER_INSTANCE = "singleDiServerInstance";
 
   private static final long serialVersionUID = 8217343898202366129L;
 
   private MemoryResultSet transformationOutputRows;
 
   private IPentahoResultSet injectorRows;
 
   private MemoryResultSet transformationOutputErrorRows;
 
   private int transformationOutputRowsCount;
 
   private int transformationOutputErrorRowsCount;
 
   private String directory; // the repository directory
 
   private String transformation; // the repository file
 
   private String job; // the repository file
 
   private String monitorStep = null;
 
   private String injectorStep = null;
 
   private Map<String, Object> varArgs = new HashMap<String, Object>();
 
   /** The name of the repository to use */
   private String repositoryName;
 
   private Log4jBufferAppender pdiUserAppender;
 
   private RowProducer rowInjector = null;
 
   private Job localJob = null;
 
   private Trans localTrans = null;
 
   private Log log = LogFactory.getLog(PdiAction.class);
 
   private Map<String, String> variables;
 
   private Map<String, String> parameters;
 
   private String[] arguments;
 
   private String logLevel;
   private String clearLog;
   private String runSafeMode;
   private String runClustered;
 
   public void setLogger(Log log) {
     this.log = log;
   }
 
   /**
    * Validates that the component has everything it needs to execute a transformation or job
    * 
    * @return
    * @throws ActionValidationException
    */
   public void validate() throws ActionValidationException {
     if (directory == null) {
       throw new ActionValidationException(org.pentaho.platform.plugin.kettle.messages.Messages.getInstance().getErrorString("PdiAction.ERROR_0001_DIR_NOT_SET")); //$NON-NLS-1$
     }
     if (transformation == null && job == null) {
       throw new ActionValidationException(org.pentaho.platform.plugin.kettle.messages.Messages.getInstance().getErrorString(
           "PdiAction.ERROR_0002_JOB_OR_TRANS_NOT_SET")); //$NON-NLS-1$
     }
 
     if (injectorStep != null && injectorRows == null) {
       throw new ActionValidationException(org.pentaho.platform.plugin.kettle.messages.Messages.getInstance().getErrorString(
           "PdiAction.ERROR_0003_INJECTOR_ROWS_NOT_SET", injectorStep)); //$NON-NLS-1$
     }
   }
 
   public void setVariables(Map<String, String> variables) {
     this.variables = variables;
   }
 
   public Map<String, String> getVariables() {
     return variables;
   }
 
   public void setParameters(Map<String, String> parameters) {
     this.parameters = parameters;
   }
 
   public Map<String, String> getParameters() {
     return parameters;
   }
 
   public void setArguments(String arguments[]) {
     this.arguments = arguments;
   }
 
   public String[] getArguments() {
     return arguments;
   }
 
   /**
    * Execute the specified transformation in the chosen repository.
    */
   public void execute() throws Exception {
 
     if (log.isDebugEnabled()) {
       log.debug(Messages.getInstance().getString("Kettle.DEBUG_START")); //$NON-NLS-1$
     }
 
     validate();
 
     TransMeta transMeta = null;
     JobMeta jobMeta = null;
 
     LogWriter logWriter = LogWriter.getInstance("Kettle-pentaho", false); //$NON-NLS-1$
 
     // initialize environment variables
     KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
 
     pdiUserAppender = CentralLogStore.getAppender();
     Repository repository = connectToRepository(logWriter);
     logWriter.addAppender(pdiUserAppender);
     try {
       if (transformation != null) {
         // try loading from internal repository before falling back onto kettle
         // the repository passed here is not used to load the transformation it is used
         // to populate available databases, etc in "standard" kettle fashion
     	  try {
     	    transMeta = createTransMetaJCR(repository);
     	  } catch (Throwable t) {
     	  }
 
         if (transMeta == null) {
           transMeta = createTransMeta(repository, logWriter);
         }
         if (transMeta == null) {
           throw new IllegalStateException(org.pentaho.platform.plugin.kettle.messages.Messages.getInstance().getErrorString(
               "PdiAction.ERROR_0004_FAILED_TRANSMETA_CREATION")); //$NON-NLS-1$
         }
         executeTransformation(transMeta, logWriter);
       } else if (job != null) {
 
         // try loading from internal repository before falling back onto kettle
         // the repository passed here is not used to load the job it is used
         // to populate available databases, etc in "standard" kettle fashion
     	  try {
     	    jobMeta = createJobMetaJCR(repository);
     	  } catch (Throwable t) {
     	  }
     	  
         if (jobMeta == null) {
           jobMeta = createJobMeta(repository, logWriter);
         }
         if (jobMeta == null) {
           throw new IllegalStateException(org.pentaho.platform.plugin.kettle.messages.Messages.getInstance().getErrorString(
               "PdiAction.ERROR_0005_FAILED_JOBMETA_CREATION")); //$NON-NLS-1$
         }
         executeJob(jobMeta, repository, logWriter);
       }
     } finally {
       logWriter.removeAppender(pdiUserAppender);
       if (repository != null) {
         if (log.isDebugEnabled())
           log.debug(Messages.getInstance().getString("Kettle.DEBUG_DISCONNECTING")); //$NON-NLS-1$
         repository.disconnect();
       }
     }
 
     XMLHandlerCache.getInstance().clear();
   }
 
   private TransMeta createTransMeta(Repository repository, LogWriter logWriter) throws ActionExecutionException {
     // TODO: do we need to set a parameter on the job or trans meta called
     // ${pentaho.solutionpath} to mimic the old in-line xml replacement behavior
     // (see scm history for an illustration of this)?
 
     // TODO: beware of BISERVER-50
 
     EngineMetaLoader engineMetaUtil = new EngineMetaLoader(repository);
 
     TransMeta transMeta;
     try {
       transMeta = engineMetaUtil.loadTransMeta(directory, transformation);
     } catch (FileNotFoundException e) {
       throw new ActionExecutionException(org.pentaho.platform.plugin.kettle.messages.Messages.getInstance().getErrorString(
           "PdiAction.ERROR_0006_FAILED_TRANSMETA_CREATION", directory, transformation), e); //$NON-NLS-1$
     }
     if (arguments != null) {
       transMeta.setArguments(arguments);
     }
     if (logLevel != null) {
       transMeta.setLogLevel(LogLevel.getLogLevelForCode(logLevel));
     }
 
     populateInputs(transMeta, transMeta);
 
     return transMeta;
   }
 
   private TransMeta createTransMetaJCR(Repository repository) throws ActionExecutionException {
     TransMeta transMeta = new TransMeta();
     try {
 
       IUnifiedRepository unifiedRepository = PentahoSystem.get(IUnifiedRepository.class, null);
       RepositoryFile transFile = unifiedRepository.getFile(idTopath(transformation));
 
       SimpleRepositoryFileData fileData = unifiedRepository.getDataForRead(transFile.getId(), SimpleRepositoryFileData.class);
       InputStream inputStream = fileData.getStream();
       Document doc = XMLHandler.loadXMLFile(inputStream);
 
       if (doc != null) {
         Node transnode = XMLHandler.getSubNode(doc, TransMeta.XML_TAG); //$NON-NLS-1$
 
         if (transnode == null) {
           throw new ActionExecutionException();
         }
 
         // Load from this node...
         transMeta.loadXML(transnode, repository, true, transMeta, null);
       } else {
         throw new ActionExecutionException();
       }
     } catch (Throwable e) {
       throw new ActionExecutionException(org.pentaho.platform.plugin.kettle.messages.Messages.getInstance().getErrorString(
           "PdiAction.ERROR_0006_FAILED_TRANSMETA_CREATION", directory, transformation), e); //$NON-NLS-1$
     }
     if (arguments != null) {
       transMeta.setArguments(arguments);
     }
     if (logLevel != null) {
       transMeta.setLogLevel(LogLevel.getLogLevelForCode(logLevel));
     }
 
     populateInputs(transMeta, transMeta);
 
     return transMeta;
   }
 
   private JobMeta createJobMeta(Repository repository, LogWriter logWriter) throws ActionExecutionException {
     // TODO: do we need to set a parameter on the job or trans meta called
     // ${pentaho.solutionpath} to mimic the old in-line xml replacement behavior
     // (see scm history for an illustration of this)?
 
     // TODO: beware of BISERVER-50
 
     EngineMetaLoader engineMetaUtil = new EngineMetaLoader(repository);
 
     JobMeta jobMeta;
     try {
       jobMeta = engineMetaUtil.loadJobMeta(directory, job);
     } catch (FileNotFoundException e) {
       throw new ActionExecutionException(org.pentaho.platform.plugin.kettle.messages.Messages.getInstance().getErrorString(
           "PdiAction.ERROR_0007_FAILED_JOBMETA_CREATION", directory, job), e); //$NON-NLS-1$
     }
     if (arguments != null) {
       jobMeta.setArguments(arguments);
     }
     if (logLevel != null) {
       jobMeta.setLogLevel(LogLevel.getLogLevelForCode(logLevel));
     }
 
     populateInputs(jobMeta, jobMeta);
 
     return jobMeta;
   }
 
   private JobMeta createJobMetaJCR(Repository repository) throws ActionExecutionException {
     JobMeta jobMeta = new JobMeta();
     try {
 
       IUnifiedRepository unifiedRepository = PentahoSystem.get(IUnifiedRepository.class, null);
       RepositoryFile transFile = unifiedRepository.getFile(idTopath(transformation));
 
       SimpleRepositoryFileData fileData = unifiedRepository.getDataForRead(transFile.getId(), SimpleRepositoryFileData.class);
       InputStream inputStream = fileData.getStream();
       Document doc = XMLHandler.loadXMLFile(inputStream);
 
       if (doc != null) {
         Node jobNode = XMLHandler.getSubNode(doc, JobMeta.XML_TAG); //$NON-NLS-1$
 
         if (jobNode == null) {
           throw new ActionExecutionException();
         }
 
         // Load from this node...
         jobMeta.loadXML(jobNode, repository, false, null);
       } else {
         throw new ActionExecutionException();
       }
     } catch (Throwable e) {
       throw new ActionExecutionException(org.pentaho.platform.plugin.kettle.messages.Messages.getInstance().getErrorString(
           "PdiAction.ERROR_0006_FAILED_TRANSMETA_CREATION", directory, transformation), e); //$NON-NLS-1$
     }
     if (arguments != null) {
       jobMeta.setArguments(arguments);
     }
     if (logLevel != null) {
       jobMeta.setLogLevel(LogLevel.getLogLevelForCode(logLevel));
     }
 
     populateInputs(jobMeta, jobMeta);
 
     return jobMeta;
   }
 
   private String idTopath(String id) {
     String path = id.replace(":", "/");
     if (path != null && path.length() > 0 && path.charAt(0) != '/') {
       path = "/" + path;
     }
     return path;
   }
 
   private void populateInputs(NamedParams paramHolder, VariableSpace varSpace) {
     if (parameters != null) {
       for (String paramKey : parameters.keySet()) {
         try {
           paramHolder.setParameterValue(paramKey, parameters.get(paramKey));
         } catch (UnknownParamException upe) {
           log.warn(upe);
         }
       }
     }
 
     if (variables != null) {
       for (String variableKey : variables.keySet()) {
         varSpace.setVariable(variableKey, variables.get(variableKey));
       }
     }
 
     for (Map.Entry<String, Object> entry : varArgs.entrySet()) {
       varSpace.setVariable(entry.getKey(), (entry.getValue() != null) ? entry.getValue().toString() : null);
     }
   }
 
   protected boolean customizeTrans(Trans trans, LogWriter logWriter) {
     // override this to customize the transformation before it runs
     // by default there is no transformation
     return true;
   }
 
   protected String getJobName(String carteObjectId) {
     // the name returned here is going to be specific to the user + job path + name
     // if we just used the name, we're likely to clobber more often
     //return directory + "/" + job + " [" + PentahoSessionHolder.getSession().getName() + ":" + carteObjectId + "]"; //$NON-NLS-1$ //$NON-NLS-2$
     return job;
   }
 
   protected String getTransformationName(String carteObjectId) {
     // the name returned here is going to be specific to the user + transformation path + name
     // if we just used the name, we're likely to clobber more often
     //return directory + "/" + transformation + " [" + PentahoSessionHolder.getSession().getName() + ":" + carteObjectId + "]"; //$NON-NLS-1$ //$NON-NLS-2$
     return transformation;
   }
 
   /**
    * Executes a PDI transformation
    * 
    * @param transMeta
    * @param logWriter
    * @return
    * @throws ActionExecutionException
    */
   protected void executeTransformation(final TransMeta transMeta, final LogWriter logWriter) throws ActionExecutionException {
     localTrans = null;
 
     if (transMeta != null) {
       TransExecutionConfiguration transExConfig = new TransExecutionConfiguration();
       if (logLevel != null) {
         transExConfig.setLogLevel(LogLevel.getLogLevelForCode(logLevel));
       }
       if (clearLog != null) {
         transExConfig.setClearingLog(Boolean.valueOf(clearLog));
       }
       if (runSafeMode != null) {
         transExConfig.setSafeModeEnabled(Boolean.valueOf(runSafeMode));
       }
 
       try {
         localTrans = new Trans(transMeta);
 
         String carteObjectId = UUID.randomUUID().toString();
         CarteSingleton
             .getInstance()
             .getTransformationMap()
             .addTransformation(getTransformationName(carteObjectId), carteObjectId, localTrans,
                 new TransConfiguration(localTrans.getTransMeta(), transExConfig));
 
       } catch (Exception e) {
         throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0010_BAD_TRANSFORMATION_METADATA"), e); //$NON-NLS-1$
       }
     }
 
     if (localTrans == null) {
       throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0010_BAD_TRANSFORMATION_METADATA")); //$NON-NLS-1$
     }
 
     if (localTrans != null) {
       // OK, we have the transformation, now run it!
       if (!customizeTrans(localTrans, logWriter)) {
         throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0028_CUSTOMIZATION_FUNCITON_FAILED")); //$NON-NLS-1$
       }
 
       if (log.isDebugEnabled())
         log.debug(Messages.getInstance().getString("Kettle.DEBUG_PREPARING_TRANSFORMATION")); //$NON-NLS-1$
 
       try {
         localTrans.setLogLevel(LogLevel.getLogLevelForCode(logLevel));
         localTrans.setSafeModeEnabled(Boolean.valueOf(runSafeMode));
         localTrans.prepareExecution(transMeta.getArguments());
       } catch (Exception e) {
         throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0011_TRANSFORMATION_PREPARATION_FAILED"), e); //$NON-NLS-1$
       }
 
       String stepName = null;
 
       try {
         if (log.isDebugEnabled())
           log.debug(Messages.getInstance().getString("Kettle.DEBUG_FINDING_STEP_IMPORTER")); //$NON-NLS-1$
 
         stepName = getMonitorStepName();
 
         if (stepName != null) {
           registerAsStepListener(stepName, localTrans);
         }
       } catch (Exception e) {
         throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0012_ROW_LISTENER_CREATE_FAILED"), e); //$NON-NLS-1$
       }
 
       try {
         if (log.isDebugEnabled())
           log.debug(Messages.getInstance().getString("Kettle.DEBUG_FINDING_STEP_IMPORTER")); //$NON-NLS-1$
 
         if (injectorStep != null) {
           registerAsProducer(injectorStep, localTrans);
         }
       } catch (Exception e) {
         throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0012_ROW_INJECTOR_CREATE_FAILED"), e); //$NON-NLS-1$
       }
 
       try {
         if (log.isDebugEnabled())
           log.debug(Messages.getInstance().getString("Kettle.DEBUG_STARTING_TRANSFORMATION")); //$NON-NLS-1$
         localTrans.startThreads();
       } catch (Exception e) {
         throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0013_TRANSFORMATION_START_FAILED"), e); //$NON-NLS-1$
       }
 
       // inject rows if necessary
       if (injectorRows != null) {
         // create a row meta
         try {
           if (log.isDebugEnabled())
             log.debug(Messages.getInstance().getString("Injecting rows")); //$NON-NLS-1$
           RowMeta rowMeta = new RowMeta();
           RowMetaInterface rowMetaInterface = transMeta.getStepFields(injectorStep);
           rowMeta.addRowMeta(rowMetaInterface);
 
           // inject the rows
           Object row[] = injectorRows.next();
           while (row != null) {
             rowInjector.putRow(rowMeta, row);
             row = injectorRows.next();
           }
           rowInjector.finished();
         } catch (Exception e) {
           throw new ActionExecutionException(Messages.getInstance().getErrorString("Row injection failed"), e); //$NON-NLS-1$
         }
       }
 
       try {
         // It's running in a separate thread to allow monitoring, etc.
         if (log.isDebugEnabled())
           log.debug(Messages.getInstance().getString("Kettle.DEBUG_TRANSFORMATION_RUNNING")); //$NON-NLS-1$
 
         localTrans.waitUntilFinished();
         localTrans.cleanup();
       } catch (Exception e) {
         int transErrors = localTrans.getErrors();
         throw new ActionExecutionException(org.pentaho.platform.plugin.kettle.messages.Messages.getInstance().getErrorString(
             "PdiAction.ERROR_0009_TRANSFORMATION_HAD_ERRORS", Integer.toString(transErrors)), e); //$NON-NLS-1$
       }
 
       // Dump the Kettle log...
       if (log.isDebugEnabled())
         log.debug(pdiUserAppender.getBuffer().toString());
 
       // Build written row output
       if (transformationOutputRows != null) {
         transformationOutputRowsCount = transformationOutputRows.getRowCount();
       }
 
       // Build error row output
       if (transformationOutputErrorRows != null) {
         transformationOutputErrorRowsCount = transformationOutputErrorRows.getRowCount();
       }
     }
   }
 
   /**
    * Registers this component as a step listener of a transformation. This allows this component to receive rows of data from the transformation when it
    * executes. These rows are made available to other components in the action sequence as a result set.
    * 
    * @param stepName
    * @param trans
    * @return
    * @throws KettleStepException
    */
   protected void registerAsStepListener(String stepName, Trans trans) throws KettleStepException {
     if (trans != null) {
       List<StepMetaDataCombi> stepList = trans.getSteps();
       // find the specified step
       for (StepMetaDataCombi step : stepList) {
         if (step.stepname.equals(stepName)) {
           if (log.isDebugEnabled())
             log.debug(Messages.getInstance().getString("Kettle.DEBUG_FOUND_STEP_IMPORTER")); //$NON-NLS-1$
           // this is the step we are looking for
           if (log.isDebugEnabled())
             log.debug(Messages.getInstance().getString("Kettle.DEBUG_GETTING_STEP_METADATA")); //$NON-NLS-1$
           RowMetaInterface row = trans.getTransMeta().getStepFields(stepName);
 
           // create the metadata that the Pentaho result sets need
           String fieldNames[] = row.getFieldNames();
           String columns[][] = new String[1][fieldNames.length];
           for (int column = 0; column < fieldNames.length; column++) {
             columns[0][column] = fieldNames[column];
           }
           if (log.isDebugEnabled())
             log.debug(Messages.getInstance().getString("Kettle.DEBUG_CREATING_RESULTSET_METADATA")); //$NON-NLS-1$
 
           MemoryMetaData metaData = new MemoryMetaData(columns, null);
           transformationOutputRows = new MemoryResultSet(metaData);
           transformationOutputErrorRows = new MemoryResultSet(metaData);
 
           // add ourself as a row listener
           step.step.addRowListener(this);
           break;
         }
       }
     }
   }
 
   /**
    * Registers this component as a row producer in a transformation. This allows this component to inject rows into a transformation when it is executed.
    * 
    * @param stepName
    * @param trans
    * @return
    * @throws KettleException
    */
   protected boolean registerAsProducer(String stepName, Trans trans) throws KettleException {
     if (trans != null) {
       rowInjector = trans.addRowProducer(stepName, 0);
       return true;
     }
 
     return false;
   }
 
   protected String getMonitorStepName() {
     String result = null;
 
     if (monitorStep != null) {
       result = monitorStep;
     }
     return result;
   }
 
   /**
    * Executes a PDI job
    * 
    * @param jobMeta
    * @param repository
    * @param logWriter
    * @return
    * @throws ActionExecutionException
    */
   protected void executeJob(final JobMeta jobMeta, final Repository repository, final LogWriter logWriter) throws ActionExecutionException {
     localJob = null;
 
     if (jobMeta != null) {
       JobExecutionConfiguration jobExConfig = new JobExecutionConfiguration();
       if (logLevel != null) {
         jobExConfig.setLogLevel(LogLevel.getLogLevelForCode(logLevel));
       }
       if (clearLog != null) {
         jobExConfig.setClearingLog(Boolean.valueOf(clearLog));
       }
       if (runSafeMode != null) {
         jobExConfig.setSafeModeEnabled(Boolean.valueOf(runSafeMode));
       }
 
       try {
         localJob = new Job(repository, jobMeta);
 
         String carteObjectId = UUID.randomUUID().toString();
         CarteSingleton.getInstance().getJobMap()
             .addJob(getJobName(carteObjectId), carteObjectId, localJob, new JobConfiguration(localJob.getJobMeta(), jobExConfig));
 
       } catch (Exception e) {
         throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0021_BAD_JOB_METADATA"), e); //$NON-NLS-1$
       }
 
     }
     if (localJob == null) {
       if (log.isDebugEnabled())
         log.debug(pdiUserAppender.getBuffer().toString());
       throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0021_BAD_JOB_METADATA")); //$NON-NLS-1$
     }
     if (localJob != null) {
       try {
         if (log.isDebugEnabled())
           log.debug(Messages.getInstance().getString("Kettle.DEBUG_STARTING_JOB")); //$NON-NLS-1$
 
         localJob.setLogLevel(LogLevel.getLogLevelForCode(logLevel));
         localJob.start();
 
       } catch (Throwable e) {
         throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0022_JOB_START_FAILED"), e); //$NON-NLS-1$
       }
 
       // It's running in a separate tread to allow monitoring, etc.
       if (log.isDebugEnabled())
         log.debug(Messages.getInstance().getString("Kettle.DEBUG_JOB_RUNNING")); //$NON-NLS-1$
      localJob.waitUntilFinished(5000000);
       int jobErrors = localJob.getErrors();
       long jobResultErrors = localJob.getResult().getNrErrors();
       if ((jobErrors > 0) || (jobResultErrors > 0)) {
         if (log.isDebugEnabled())
           log.debug(pdiUserAppender.getBuffer().toString());
         throw new ActionExecutionException(org.pentaho.platform.plugin.kettle.messages.Messages.getInstance().getErrorString(
             "PdiAction.ERROR_0008_JOB_HAD_ERRORS", //$NON-NLS-1$ 
             Integer.toString(jobErrors), Long.toString(jobResultErrors)));
       }
 
       // Dump the Kettle log...
       if (log.isDebugEnabled())
         log.debug(pdiUserAppender.getBuffer().toString());
     }
   }
 
   /**
    * Connects to the PDI repository
    * 
    * @param logWriter
    * @return
    * @throws KettleException
    * @throws KettleSecurityException
    * @throws ActionExecutionException
    */
   protected Repository connectToRepository(final LogWriter logWriter) throws KettleSecurityException, KettleException, ActionExecutionException {
 
     if (StringUtils.isEmpty(repositoryName)) {
       return null;
     }
 
     if (log.isDebugEnabled())
       log.debug(Messages.getInstance().getString("Kettle.DEBUG_META_REPOSITORY")); //$NON-NLS-1$
 
     RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
     if (repositoriesMeta == null) {
       if (log.isDebugEnabled())
         log.debug(pdiUserAppender.getBuffer().toString());
       throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0007_BAD_META_REPOSITORY")); //$NON-NLS-1$
     }
 
     if (log.isDebugEnabled())
       log.debug(Messages.getInstance().getString("Kettle.DEBUG_POPULATING_META")); //$NON-NLS-1$
 
     boolean singleDiServerInstance = "true".equals(PentahoSystem.getSystemSetting(SINGLE_DI_SERVER_INSTANCE, "true")); //$NON-NLS-1$ //$NON-NLS-2$
 
     try {
       if (singleDiServerInstance) {
         if (log.isDebugEnabled()) {
           log.debug("singleDiServerInstance=true, loading default repository"); //$NON-NLS-1$
         }
 
         // only load a default enterprise repository. If this option is set, then you cannot load
         // transformations or jobs from anywhere but the local server.
 
         String repositoriesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><repositories>" //$NON-NLS-1$
             + "<repository><id>PentahoEnterpriseRepository</id>" //$NON-NLS-1$
             + "<name>" + SINGLE_DI_SERVER_INSTANCE + "</name>" //$NON-NLS-1$ //$NON-NLS-2$
             + "<description>" + SINGLE_DI_SERVER_INSTANCE + "</description>" //$NON-NLS-1$ //$NON-NLS-2$
             + "<repository_location_url></repository_location_url>" //$NON-NLS-1$
             + "<version_comment_mandatory>N</version_comment_mandatory>" //$NON-NLS-1$
             + "</repository>" //$NON-NLS-1$
             + "</repositories>"; //$NON-NLS-1$
 
         ByteArrayInputStream sbis = new ByteArrayInputStream(repositoriesXml.getBytes("UTF8"));
         repositoriesMeta.readDataFromInputStream(sbis);
       } else {
         // TODO: add support for specified repositories.xml files...
         repositoriesMeta.readData(); // Read from the default $HOME/.kettle/repositories.xml file.
       }
     } catch (Exception e) {
       throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0018_META_REPOSITORY_NOT_POPULATED"), e); //$NON-NLS-1$
     }
 
     if (log.isDebugEnabled())
       log.debug(Messages.getInstance().getString("Kettle.DEBUG_FINDING_REPOSITORY")); //$NON-NLS-1$
     // Find the specified repository.
     RepositoryMeta repositoryMeta = null;
     try {
       if (singleDiServerInstance) {
         repositoryMeta = repositoriesMeta.findRepository(SINGLE_DI_SERVER_INSTANCE);
       } else {
         repositoryMeta = repositoriesMeta.findRepository(repositoryName);
       }
 
     } catch (Exception e) {
       throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0004_REPOSITORY_NOT_FOUND", repositoryName), e); //$NON-NLS-1$
     }
 
     if (repositoryMeta == null) {
       if (log.isDebugEnabled())
         log.debug(pdiUserAppender.getBuffer().toString());
       throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0004_REPOSITORY_NOT_FOUND", repositoryName)); //$NON-NLS-1$
     }
 
     if (log.isDebugEnabled())
       log.debug(Messages.getInstance().getString("Kettle.DEBUG_GETTING_REPOSITORY")); //$NON-NLS-1$
     Repository repository = null;
     try {
       repository = PluginRegistry.getInstance().loadClass(RepositoryPluginType.class, repositoryMeta.getId(), Repository.class);
       repository.init(repositoryMeta);
 
     } catch (Exception e) {
       throw new ActionExecutionException(Messages.getInstance().getErrorString("Kettle.ERROR_0016_COULD_NOT_GET_REPOSITORY_INSTANCE"), e); //$NON-NLS-1$
     }
 
     // OK, now try the username and password
     if (log.isDebugEnabled()) {
       log.debug(Messages.getInstance().getString("Kettle.DEBUG_CONNECTING")); //$NON-NLS-1$
     }
 
     // Two scenarios here: internal to server or external to server. If internal, you are already authenticated. If
     // external, you must provide a username and additionally specify that the IP address of the machine running this
     // code is trusted.
     repository.connect(PentahoSessionHolder.getSession().getName(), null);
 
     // OK, the repository is open and ready to use.
     if (log.isDebugEnabled())
       log.debug(Messages.getInstance().getString("Kettle.DEBUG_FINDING_DIRECTORY")); //$NON-NLS-1$
 
     return repository;
   }
 
   public void rowReadEvent(final RowMetaInterface row, final Object[] values) {
   }
 
   /**
    * Processes a row of data generated by the PDI transform. This is a RowListener method
    */
   public void rowWrittenEvent(final RowMetaInterface rowMeta, final Object[] row) throws KettleStepException {
     processRow(transformationOutputRows, rowMeta, row);
   }
 
   /**
    * Processes an error row of data generated by the PDI transform. This is a RowListener method
    */
   public void errorRowWrittenEvent(final RowMetaInterface rowMeta, final Object[] row) throws KettleStepException {
     processRow(transformationOutputErrorRows, rowMeta, row);
   }
 
   /**
    * Adds a row of data to the provided result set
    * 
    * @param memResults
    * @param rowMeta
    * @param row
    * @throws KettleStepException
    */
   public void processRow(MemoryResultSet memResults, final RowMetaInterface rowMeta, final Object[] row) throws KettleStepException {
     if (memResults == null) {
       return;
     }
     try {
       // create a new row object
       Object pentahoRow[] = new Object[memResults.getColumnCount()];
       for (int columnNo = 0; columnNo < memResults.getColumnCount(); columnNo++) {
         // process each column in this row
         ValueMetaInterface valueMeta = rowMeta.getValueMeta(columnNo);
 
         switch (valueMeta.getType()) {
         case ValueMetaInterface.TYPE_BIGNUMBER:
           pentahoRow[columnNo] = rowMeta.getBigNumber(row, columnNo);
           break;
         case ValueMetaInterface.TYPE_BOOLEAN:
           pentahoRow[columnNo] = rowMeta.getBoolean(row, columnNo);
           break;
         case ValueMetaInterface.TYPE_DATE:
           pentahoRow[columnNo] = rowMeta.getDate(row, columnNo);
           break;
         case ValueMetaInterface.TYPE_INTEGER:
           pentahoRow[columnNo] = rowMeta.getInteger(row, columnNo);
           break;
         case ValueMetaInterface.TYPE_NONE:
           pentahoRow[columnNo] = rowMeta.getString(row, columnNo);
           break;
         case ValueMetaInterface.TYPE_NUMBER:
           pentahoRow[columnNo] = rowMeta.getNumber(row, columnNo);
           break;
         case ValueMetaInterface.TYPE_STRING:
           pentahoRow[columnNo] = rowMeta.getString(row, columnNo);
           break;
         default:
           pentahoRow[columnNo] = rowMeta.getString(row, columnNo);
         }
       }
       // add the row to the result set
       memResults.addRow(pentahoRow);
     } catch (KettleValueException e) {
       throw new KettleStepException(e);
     }
   }
 
   /**
    * Sets the PDI repository (or filesystem) directory to load transformations and jobs from
    * 
    * @param directory
    */
   public void setDirectory(String directory) {
     this.directory = directory;
   }
 
   /**
    * Sets any named inputs that need to be provided to the transformation or job
    */
   public void setVarArgs(Map<String, Object> varArgs) {
     this.varArgs = varArgs;
   }
 
   public String getLog() {
     return pdiUserAppender.getBuffer().toString();
   }
 
   /**
    * Returns the result set of the successful output rows. This will only return data if setMonitorStepName() or setImportStepName() has been called
    * 
    * @return
    */
   public MemoryResultSet getTransformationOutputRows() {
     return transformationOutputRows;
   }
 
   /**
    * Returns the result set of the error output rows. This will only return data if setMonitorStepName() or setImportStepName() has been called
    * 
    * @return
    */
   public MemoryResultSet getTransformationOutputErrorRows() {
     return transformationOutputErrorRows;
   }
 
   /**
    * Returns the number of successful output rows. This will only return data if setMonitorStepName() or setImportStepName() has been called
    * 
    * @return
    */
   public int getTransformationOutputRowsCount() {
     return transformationOutputRowsCount;
   }
 
   /**
    * Returns the number of failed output rows. This will only return data if setMonitorStepName() or setImportStepName() has been called
    * 
    * @return
    */
   public int getTransformationOutputErrorRowsCount() {
     return transformationOutputErrorRowsCount;
   }
 
   /**
    * Sets the result set containing rows to be injected into the transformation. This data will only be used if setInjectorStep() is called.
    * 
    * @param injectorRows
    */
   public void setInjectorRows(IPentahoResultSet injectorRows) {
     this.injectorRows = injectorRows;
   }
 
   /**
    * Sets the name of the transformation to be loaded from the PDI repository. This is used in conjunction with setDirectory().
    * 
    * @param transformation
    */
   public void setTransformation(String transformation) {
     this.transformation = transformation;
   }
 
   /**
    * Sets the name of the job to be loaded from the PDI repository. This is used in conjunction with setDirectory().
    * 
    * @param job
    */
   public void setJob(String job) {
     this.job = job;
   }
 
   /**
    * Sets the name of the transformation step to accept rows from
    * 
    * @param monitorStep
    */
   public void setMonitorStep(String monitorStep) {
     this.monitorStep = monitorStep;
   }
 
   /**
    * Sets the name of the transformation step to inject rows into. Use this in conjunction with setInjectorRows().
    * 
    * @param injectorStep
    */
   public void setInjectorStep(String injectorStep) {
     this.injectorStep = injectorStep;
   }
 
   /**
    * Returns the status of the transformation or job
    * 
    * @return
    */
   public String getStatus() {
     if (localTrans != null) {
       return localTrans.getStatus();
     } else if (localJob != null) {
       return localJob.getStatus();
     } else
       return Messages.getInstance().getErrorString("Kettle.ERROR_0025_NOT_LOADED"); //$NON-NLS-1$;
   }
 
   /**
    * Returns the exit status of the transformation or job
    * 
    * @return
    */
   public int getResult() {
     if (localTrans != null) {
       return localTrans.getResult().getExitStatus();
     } else if (localJob != null) {
       return localJob.getResult().getExitStatus();
     } else
       return -1;
   }
 
   public String getRepositoryName() {
     return repositoryName;
   }
 
   public void setRepositoryName(String repositoryName) {
     this.repositoryName = repositoryName;
   }
 
   public String getLogLevel() {
     return logLevel;
   }
 
   public void setLogLevel(String logLevel) {
     this.logLevel = logLevel;
   }
 
   public String getClearLog() {
     return clearLog;
   }
 
   public void setClearLog(String clearLog) {
     this.clearLog = clearLog;
   }
 
   public String getRunSafeMode() {
     return runSafeMode;
   }
 
   public void setRunSafeMode(String runSafeMode) {
     this.runSafeMode = runSafeMode;
   }
 
   public String getRunClustered() {
     return runClustered;
   }
 
   public void setRunClustered(String runClustered) {
     this.runClustered = runClustered;
   }
 }
