 /*
  * File Name:  AbstractDatasetAgent.java
  * Created on: Dec 17, 2010
  */
 package net.ooici.eoi.datasetagent;
 
 import ion.core.IonException;
 import ion.core.messaging.IonMessage;
 import ion.core.messaging.MessagingName;
 import ion.core.utils.GPBWrapper;
 import ion.core.utils.ProtoUtils;
 import ion.core.utils.StructureManager;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.UUID;
 import net.ooici.cdm.syntactic.Cdmvariable;
 import net.ooici.core.message.IonMessage.IonMsg;
 import net.ooici.core.message.IonMessage.ResponseCodes;
 import net.ooici.eoi.netcdf.AttributeFactory;
 import net.ooici.eoi.netcdf.NcUtils;
 import net.ooici.eoi.proto.Unidata2Ooi;
 import net.ooici.services.dm.IngestionService.DataAcquisitionCompleteMessage;
 import net.ooici.services.dm.IngestionService.DataAcquisitionCompleteMessage.StatusCode;
 import net.ooici.services.sa.DataSource.EoiDataContextMessage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.constants.FeatureType;
 import ucar.nc2.dataset.NetcdfDataset;
 
 /**
  * The AbstractDatasetAgent provides the core functionality used in typical implementations of an <code>IDatasetAgent</code>.<br />
  * <br />
  * This class defines the standard implementation for the method {@link #doUpdate(net.ooici.core.container.Container.Structure, java.util.HashMap)}<br />
  * <br />
  * <b><code>doUpdate()</code></b> Performs a dataset update sequence by chaining
  * calls to the methods:
  * <ol>
  * <li>{@link #buildRequest()}</li>
  * <li>{@link #acquireData(String)}</li>
  * <li>{@link #_processDataset(Object)}</li>
  * </ol>
  * 
  * This class also defines the following helper methods for satisfying dataset update requests:
  * <ul>
  * <li>{@link #sendNetcdfDataset(NetcdfDataset, String)}</li>
  * <li>{@link #sendNetcdfDataset(NetcdfDataset, String, boolean)}</li>
  * <li>{@link #decompSendVariable(ucar.nc2.Variable, ucar.ma2.Section, int)}</li>
  * <li>{@link #addOoiciBoundsMetadata(NetcdfDataset)}</li>
  * <li>{@link #sendDatasetMsg(byte[])}</li>
  * <li>{@link #sendDataChunkMsg(byte[])}</li>
  * <li>{@link #sendDataDoneMsg()}</li>
  * </ul>
  * 
  * <b>Implementation Note:</b><br />
  * Concrete classes are required to implement the following methods:
  * <ul>
  * <li>{@link #buildRequest()}</li>
  * <li>{@link #acquireData(String)}</li>
  * <li>{@link #_processDataset(Object)}</li>
  * </ul>
  * 
  * @author cmueller
  * @author tlarocque
  * @version 1.0
  */
 public abstract class AbstractDatasetAgent implements IDatasetAgent {
 
     private static Logger log = LoggerFactory.getLogger(AbstractDatasetAgent.class);
     private static final String procName = "EOI_DatasetAgent";
 
     /**
      * Used to determine what the agent does after processing an update.
      */
     public enum AgentRunType {
 
         /**
          * The 'default' runtype which sends messages to the ingest service after processing the update
          */
         NORMAL,
         /**
          * Runs the agent in "test mode" - the update is processed normally, but the response is the 'cdl' dump for the dataset.  No messages or data are sent via messages or written to disk
          */
         TEST_NO_WRITE,
         /**
          * Runs the agent in "test mode" with results written to disk as NetCDF files - the update is processed normally, the response is the 'cdl' dump for the dataset, and the dataset is written to disk @ "{outputDir}/{ds_title}.nc"
          */
         TEST_WRITE_NC,
         /**
          * Runs the agent in "test mode" with results written to disk as "ooicdm" files - the update is processed normally, the response is the 'cdl' dump for the dataset, and the dataset is written to disk @ "{outputDir}/{dataset_title}/{ds_title}.ooicdm"
          * If the dataset is decomposed, multiple "cdm" files are written with an incremental numeral suffix
          */
         TEST_WRITE_OOICDM,}
     /**
      * This is to allow for testing without sending data messages (ii.e. to test agent implementations) - set to "TEST_NO_WRITE" or "TEST_WRITE_NC" to run in "test" mode
      */
     private AgentRunType runType = AgentRunType.NORMAL;
     /**
      * This is the value used to decompose the dataset when sending.
      * This is the maximum <i>total bytes</i> of <b>data</b> that will be sent in one message.
      * It does not include wrapper and message size.
      */
     private long maxSize = 5242880;//Default is 5 MB
     /**
      * This is the value used to divide the dimension length when decomposing within a given dimension.
      */
     private int decompDivisor = 2;//Default is to divide by 2
     /**
      * HashMap of Ranges (keyed by name) that will be applied to the appropriate dimensions
      */
     private HashMap<String, Range> subRanges = new HashMap<String, Range>();
     private ion.core.messaging.MsgBrokerClient cl = null;
     private ion.core.messaging.MessagingName toName = null;
     private ion.core.messaging.MessagingName fromName = null;
 //    private String ingest_op = "ingest";
     private final String RECV_DATASET_OP = "recv_dataset";
     private final String RECV_CHUNK_OP = "recv_chunk";
     private final String RECV_DONE_OP = "recv_done";
     private String recieverQueue = null;
 
     /* Local instance of the datasetName.  Obtained in "sendNetcdfDataset" and used if writing to disk rather than sending */
     private String datasetName = "not_set";
     /* For incrementing the suffix of the output when writing .ooicdm files */
     private int incrementor = 0;
     /* Output directory when writing files during testing - must have trailing "/" */
     private String outputDir = "out/";
 
     /* Instance storage of the EoiDataContext object - protected for availability from subclasses */
     protected net.ooici.services.sa.DataSource.EoiDataContextMessage context = null;
 
     /* Instance storage of the Container.Structure as a StructureManager - protected for availability from subclasses */
     protected StructureManager structManager = null;
     private boolean ingestError = false;
 
     /*
      * (non-Javadoc)
      * @see net.ooici.eoi.datasetagent.IDatasetAgent#setTesting(boolean)
      */
     @Override
     public void setAgentRunType(AgentRunType agentRunType) {
         runType = agentRunType;
     }
 
     /*
      * (non-Javadoc)
      * @see net.ooici.eoi.datasetagent.IDatasetAgent#setOutputDir(String)
      */
     @Override
     public void setOutputDir(String outputDir) {
         this.outputDir = (outputDir.endsWith("/")) ? outputDir : outputDir + "/";
     }
 
     /*
      * (non-Javadoc)
      * @see net.ooici.eoi.datasetagent.IDatasetAgent#setMaxSize(long)
      */
     @Override
     public void setMaxSize(long maxSize) {
         this.maxSize = maxSize;
     }
 
     /*
      * (non-Javadoc)
      * @see net.ooici.eoi.datasetagent.IDatasetAgent#setDecompDivisor(int)
      */
     @Override
     public void setDecompDivisor(int decompDivisor) {
         this.decompDivisor = decompDivisor;
     }
 
     /**
      * TODO:
      * @param rng
      */
     public void addSubRange(Range rng) {
         subRanges.put(rng.getName(), rng);
     }
 
     /**
      * TODO:
      * @param rng
      */
     public void removeSubRange(Range rng) {
         subRanges.remove(rng.getName());
     }
 
     /**
      * Performs a dataset update sequence per the requirements in the given <code>context</code>. <code>connectionInfo</code> provides the
      * necessary arguments to establish brokered communication with the service which should ingest the product of said update.<br />
      * <br />
      * Typical update sequences occur over the following steps:<br />
      * <ol>
      * <li>Build a data request for the given <code>context</code><br />
      * {@link #buildRequest()}</li>
      * <li>Acquire data from the previously built request as either <code>String</code> data (CSV, TSV, XML etc) or a <code>NetCdfDataset</code>
      * <br />
      * {@link #acquireData(String)}</li>
      * <li>Process and send the data in part or wholesale depending upon a subclasses implementation<br />
      * {@link #_processDataset(Object)} <br />
      * </li>
      * </ol>
      * 
      * @param context
      *            the current or required state of a given dataset providing context for performing updates upon it
      * @param connectionInfo
      *            mapped parameters used in establishing connectivity for sending the results of a dataset update
      * 
      * @return TODO:
      * 
      * @see #initMsgBrokerClient(HashMap)
      */
     @Override
     public final String[] doUpdate(net.ooici.core.container.Container.Structure structure, java.util.HashMap<String, String> connectionInfo) throws IonException {
         /* NOTE: Template method.  Do not reorder */
 
         String[] result = null;
 
 
         /** Step 1: Initialize the MsgBroker Client as appropriate */
         try {
 
             /* If the connectionInfo object is null, assume this is being called from a test */
             if (connectionInfo == null) {
                 runType = AgentRunType.TEST_NO_WRITE;
             }
             if (runType == AgentRunType.NORMAL) {
                 initMsgBrokerClient(connectionInfo);
             }
         } catch (Exception ex) {
 
             throw new IonException("Failure while initializing the Message Broker Client...", ex);
 
         }
 
 
 
         /* Overarching try/finally to ensure closure of MsgBrokerClient on exit */
         try {
 
             /** Step 2: Retrieve the Message Context -- to be passed into the processing methods:
              *          o  buildRequest()
              *          o  acquireData()
              *          o  _processDataset()
              */
             try {
 
                 /* Load the structure into the structManager object */
                 structManager = StructureManager.Factory(structure);
 
                 /* Store the EoiDataContext object */
                 IonMsg msg = (IonMsg) structManager.getObjectWrapper(structManager.getHeadId()).getObjectValue();
                 this.context = (EoiDataContextMessage) structManager.getObjectWrapper(msg.getMessageObject()).getObjectValue();
                 if (null == this.context) {
                     throw new NullPointerException("Structure manager returned a null Update Context.  Update cannot proceed.");
                 }
 
             } catch (Exception ex) {
 
                 throw new IonException("Failure while gathering update context", ex);
 
             }
 
 
             /** Step 3: Continue processing according to the template: */
             /* Build the request */
             String request = null;
             try {
                 request = buildRequest();
             } catch (Exception ex) {
                 throw new IonException("Failed to build a request from the given update context.", ex);
 
             }
 
             /* Acquire data from the request */
             Object data = null;
             try {
                 data = acquireData(request);
             } catch (Exception ex) {
                 throw new IonException("Failed to acquire data for the request built from the update context.", ex);
             }
 
             /* Process the acquired data */
             try {
                 result = _processDataset(data);
             } catch (Exception ex) {
                 if (!(ex instanceof IngestException)) {
                     throw new IonException("Failed to process the dataset acquired from the update context request.", ex);
                 }
             }
 
 
         } catch (IonException ex) {
 
             /* Send a notification of this exception to the ingest*/
             String trace = AgentUtils.getStackTraceString(ex).replaceAll("^", "\t");
             this.sendDataErrorMsg(StatusCode.AGENT_ERROR, trace);
 
             /* Rethrow the exception so the JAW can also be notified */
             throw ex;
 
         } finally {
 
             closeMsgBrokerClient();
 
         }
 
 
 
         return result;
     }
 
     /**
      * Processes data as from {@link #acquireData(String)} -- which may be defined by a subclasses implementation
      * 
      * @param data
      *            any arbitrary data to be processed typically the result from {@link #acquireData(String)}
      * 
      * @return TODO:
      */
     protected abstract String[] _processDataset(Object data);
 
     /**
      * Breaks the given <code>NetcdfDataset</code> into manageable chunks and sends them to an ingestion service using the brokered
      * connection established by {@link #initMsgBrokerClient(HashMap)}. Prior to sending, changes to the <code>NetcdfDataset</code> are
      * finalized by appending necessary metadata and applying {@link ucar.nc2.NetcdfFile#finish()}. This method ensures that dataset chunks are never
      * larger in size than then the maximum size, set by {@link #setMaxSize(long)}
      * 
      * @param ncds
      *            a {@link NetcdfDataset} object
      * @param op
      *            TODO: **not used**
      * 
      * @return TODO: unused?
      * 
      * @see #decompSendVariable(ucar.nc2.Variable, ucar.ma2.Section, int)
      * @see #initMsgBrokerClient(HashMap)
      * @see #sendDatasetMsg(byte[])
      * @see #sendDataChunkMsg(byte[])
      * @see #sendDataDoneMsg()
      */
     protected String sendNetcdfDataset(ucar.nc2.dataset.NetcdfDataset ncds, String op) {
         return sendNetcdfDataset(ncds, op, true);
     }
 
     /**
      * Breaks the given <code>NetcdfDataset</code> into manageable chunks and sends them to an ingestion service using the brokered
      * connection established by {@link #initMsgBrokerClient(HashMap)}. Prior to sending, changes to the <code>NetcdfDataset</code> are
      * finalized by appending necessary metadata and applying {@link NetcdfFile#finish()}. This method ensures that dataset chunks are never
      * larger in size than then the maximum size, set by {@link #setMaxSize(long)}
      * 
      * @param ncds
      *            a {@link NetcdfDataset} object
      * @param op
      *            TODO: **not used**
      * @param includeData
      *            a <code>boolean</code> specifying whether or not to include the data of the given dataset. If <code>false</code> only
      *            header information will be sent
      * 
      * @return TODO: unused?
      * 
      * @see #decompSendVariable(ucar.nc2.Variable, ucar.ma2.Section, int)
      * @see #initMsgBrokerClient(HashMap)
      * @see #sendDatasetMsg(byte[])
      * @see #sendDataChunkMsg(byte[])
      * @see #sendDataDoneMsg()
      * 
      */
     protected String sendNetcdfDataset(ucar.nc2.dataset.NetcdfDataset ncds, String op, boolean includeData) {
         assert ncds != null;
 
         /* Apply OOICI geospatial-temporal metadata */
         addOoiciBoundsMetadata(ncds);
 
         /* "finish" the dataset - applies any changes that have been applied to ensure they appear in the dataset as appropriate */
         ncds.finish();
 
         datasetName = ncds.findAttValueIgnoreCase(null, "title", "NO-TITLE");
         datasetName = datasetName.replace(":", "_").replace(",", "").replace(".nc", "");
 
         String ret = null;
         switch (runType) {
             case TEST_WRITE_OOICDM:
                 outputDir += "ooicdm/";
                 ret = ncds.toString();
                 break;
             case TEST_WRITE_NC:
                 try {
                     /* Dump the dataset locally */
                     new java.io.File(outputDir).mkdir();
 //                    datasetName = (datasetName.endsWith(".nc")) ? datasetName : datasetName + ".nc";
                     ucar.nc2.FileWriter.writeToFile(ncds, outputDir + datasetName + ".nc");
                 } catch (Exception ex) {
                     log.error("Error writing file during testing...", ex);
                 }
             case TEST_NO_WRITE:
                 ret = ncds.toString();
                 return ret;
         }
 
         StatusCode statusCode = StatusCode.OK;
         String statusBody = "";
 
         /* Package the dataset */
         /* Build the OOICI Canonical Representation of the dataset and serialize as a byte[] */
         byte[] dataMessageContent;
         try {
             /** Estimate the size of the dataset */
             long estSize = NcUtils.estimateSize(ncds, subRanges);
 
             if (estSize <= maxSize) {
                 /** Send the full dataset */
                 dataMessageContent = Unidata2Ooi.ncdfToByteArray(ncds, subRanges);
                 sendDatasetMsg(dataMessageContent);
             } else {
                 /** Send a "shell" of the dataset */
                 dataMessageContent = Unidata2Ooi.ncdfToByteArray(ncds, subRanges, false);
                 sendDatasetMsg(dataMessageContent);
                 // IonMessage reply = rpcDataMessage(op, dataMessageContent);
                 // ret = reply.getContent().toString();
 
                 /** Send the variables in "chunks" */
                 for (ucar.nc2.Variable v : ncds.getVariables()) {
                     if (log.isDebugEnabled()) {
                         log.debug("Processing Variable: {}", v.getName());
                     }
                     try {
 
                         /* Get the section for the complete variable - with subranges applied */
                         ucar.ma2.Section sec = NcUtils.getSubRangedSection(v, subRanges);
 
                         /* Decompose and send variable data */
                         decompSendVariable(v, sec, 0);
                     } catch (Exception ex) {
                         log.error("Error Processing Dataset", ex);
                         statusCode = StatusCode.AGENT_ERROR;
                         statusBody = AgentUtils.getStackTraceString(ex);
                     }
                 }
             }
 
         } catch (IOException ex) {
             log.error(ret = "Error converting NetcdfDataset to OOICI CDM::******\n{}\n******", ncds.toString());
             statusCode = StatusCode.AGENT_ERROR;
             statusBody = AgentUtils.getStackTraceString(ex);
         } finally {
             if (ingestError) {
                 throw new IngestException("Processing thread interrupted!!");
             }
 
             /* Send Message to signify the end of the ingest */
 //            IonMsg.Builder endMsgBldr = IonMsg.newBuilder();
 //            endMsgBldr.setResponseCode(respCode);
 //            endMsgBldr.setResponseBody(statusBody);
 //            GPBWrapper<IonMsg> msgWrap = GPBWrapper.Factory(endMsgBldr.build());
 //            log.debug(msgWrap.toString());
             switch (runType) {
                 case NORMAL:
                     if (statusCode == StatusCode.OK) {
                         sendDataDoneMsg();
                     } else {
                         sendDataErrorMsg(statusCode, statusBody);
                     }
                     break;
             }
         }
 
         return ret;
     }
 
     /**
      * Recursive method for decomposing the data content of a variable for sending to OOI in manageable pieces.
      * <p>
      * The variable {@code var} is traversed by dimension (from outermost to innermost) using the class-level field, {@code maxSize} to determine upper-most size limit.
      * As each dimension is encountered, it is set to a length of 1 and the data size for the remaining dimensions is checked.
      * If the remaining data is still to big, another recursion is invoked to traverse the next dimension.
      * Once the remaining data is < {@code maxSize}, the retrieval size of the outermost non-singleton dimension is incremented until the retrieval size is as close to {@code maxSize} as possible.
      * This is done until the remaining data is less than {@code maxSize}, or no more inner-dimensions remain (see text after example).
      * <p>
      * Example: assume a variable with dimensions {@code [5, 10, 10]}, that each of the 500 elements is 1 byte, and that the maximum size is 100 bytes.<br>
      * <pre>
      *
      * **first recursion**
      *  section is [5, 10, 10]
      *  if(section-size > maxsize) {
      *      set section to [1, 10, 10]
      *      recurse with new section
      *  } else {
      *      deal with data
      *  }
      *
      * **second recursion**
      *  section is [1, 10, 10]
      *  if(section-size > maxsize) {
      *      set section to [1, 1, 10]
      *      recurse with new section
      *  } else {
      *      deal with data
      *  }
      * </pre>
      * If the final dimension of the variable is reached and the data for the entire dimension is still > {@code maxSize}, the dimension is repeatedly halved until the section is small enough to allow each section of the dimension to be sent sequentially.
      * <p>
      * Continuing the first example:
      * <pre>
      *  section is [1, 1, 10]
      *  while(section_size > maxsize) {
      *      calculate end = section_length / 2
      *      set section to [1:1, 1:1, section_first:end] //half of total
      *      recurse with new section
      *      set section to [1:1, 1:1, end+1:section_last] //this enters back in the top of the while loop
      *  }
      * </pre>
      *
      *
      * @param var the variable to decompose and send
      * @param sec the section describing the current decomposition level
      * @param depth the current decomposition depth
      * @throws InvalidRangeException thrown when one of the ranges within the structure does not match the available data
      */
     protected void decompSendVariable(ucar.nc2.Variable var, ucar.ma2.Section sec, int depth) throws InvalidRangeException, IOException {
         /* If we've encountered an ingestion error, return immediately */
         if (ingestError) {
             return;
         }
 
         /* Setup indenting */
         String indent = "";
         for (int d = 0; d < depth; d++) {
             indent += " ";
         }
 
         long esize = var.getElementSize();
         long size = sec.computeSize() * esize;
         if (log.isDebugEnabled()) {
             log.debug("{}decomp-depth = {} :: sec = {} :: sec-size = {}", new Object[]{indent, depth, sec, size});
         }
         if (size > maxSize) {
             Range rng;
             Range.Iterator iter;
             int i;
             /**
              * If we're at the innermost dimension, so need to split up the dimension
              * Without this we end up sending element-by-element, which is not desirable
              **/
             if (sec.getRank() - 1 == depth) {
                 rng = sec.getRange(depth);
                 Range inrng;
                 while (size > maxSize) {
                     inrng = sec.getRange(depth);
                     size = sec.computeSize() * esize;
                     if (size > maxSize) {
                         int end = inrng.first() + (inrng.length() / decompDivisor);
                         end = (end > inrng.last()) ? inrng.last() : end;
                         sec.replaceRange(depth, new Range(rng.getName(), inrng.first(), end));
                         decompSendVariable(var, sec, depth);
                         sec.replaceRange(depth, new Range(rng.getName(), end + 1, inrng.last()));
                     } else {
                         decompSendVariable(var, sec, depth);
                     }
                 }
                 sec.replaceRange(depth, rng);
             } else {
                 rng = sec.getRange(depth);
                 iter = rng.getIterator();
                 int ii;
                 long ms;
                 while (iter.hasNext()) {
                     i = iter.next();
                     sec.replaceRange(depth, new Range(rng.getName(), i, i));
                     size = sec.computeSize() * esize;
                     ms = maxSize - size;
                     while (size < ms & iter.hasNext()) {
                         ii = iter.next();
                         sec.replaceRange(depth, new Range(rng.getName(), i, ii));
                         size = sec.computeSize() * esize;
                     }
                     decompSendVariable(var, sec, depth + 1);
                 }
                 sec.replaceRange(depth, rng);
             }
         } else {
             if (log.isDebugEnabled()) {
                 log.debug("{}--> proc-sec: {}", indent, sec.toString());
             }
             /* Build the array and the bounded array */
             ion.core.utils.GPBWrapper arrWrap = Unidata2Ooi.getOoiArray(var, sec);
             Cdmvariable.BoundedArray bndArr = Unidata2Ooi.getBoundedArray(sec, (arrWrap != null) ? arrWrap.getCASRef() : null);
             ion.core.utils.GPBWrapper<Cdmvariable.BoundedArray> baWrap = ion.core.utils.GPBWrapper.Factory(bndArr);
             if (log.isDebugEnabled()) {
                 log.debug("BoundedArrayWrapper:\n{}", baWrap);
             }
 
             /* Build the supplement message */
             net.ooici.services.dm.IngestionService.SupplementMessage.Builder supMsgBldr = net.ooici.services.dm.IngestionService.SupplementMessage.newBuilder();
             supMsgBldr.setDatasetId("");
             supMsgBldr.setVariableName(var.getName());
             supMsgBldr.setBoundedArray(baWrap.getCASRef());
             GPBWrapper<net.ooici.services.dm.IngestionService.SupplementMessage> supWrap = GPBWrapper.Factory(supMsgBldr.build());
 
             /* init the struct bldr */
             net.ooici.core.container.Container.Structure.Builder sbldr = net.ooici.core.container.Container.Structure.newBuilder();
             /* add the supplement wrapper */
             ProtoUtils.addStructureElementToStructureBuilder(sbldr, supWrap.getStructureElement());
             /* add the bounded array and ndarray as items */
             ProtoUtils.addStructureElementToStructureBuilder(sbldr, baWrap.getStructureElement());
             if (arrWrap == null) {
                 log.warn("The array for variable='{}' section='{}' is null", var.getName(), sec);
             } else {
                 ProtoUtils.addStructureElementToStructureBuilder(sbldr, arrWrap.getStructureElement());
             }
 
             /* Put in an IonMsg as the head pointing to the ds element */
             IonMsg ionMsg = IonMsg.newBuilder().setIdentity(UUID.randomUUID().toString()).setMessageObject(supWrap.getCASRef()).build();
             GPBWrapper ionMsgWrap = GPBWrapper.Factory(ionMsg);
             ProtoUtils.addStructureElementToStructureBuilder(sbldr, ionMsgWrap.getStructureElement(), true);// Set as head
 
             switch (runType) {
                 case NORMAL:
                     sendDataChunkMsg(sbldr.build().toByteArray());
                     break;
                 case TEST_WRITE_OOICDM:
                     writeChunkProto(sbldr.build().toByteArray());
                     break;
             }
         }
     }
 
     /**
      * Adds ION-specific global metadata to the given <code>NetcdfDataset</code> to specify the geospatial and temporal bounds of that
      * dataset
      * 
      * @param ncds
      *            a {@link NetcdfDataset} object
      */
     protected void addOoiciBoundsMetadata(NetcdfDataset ncds) {
         FeatureType ft = null;
 //        ft = NcUtils.determineFeatureType(ncds);
 
         /* Do Time */
         AttributeFactory.addTimeBoundsMetadata(ncds, subRanges);
 
         /* Do Lat */
         AttributeFactory.addLatBoundsMetadata(ncds, ft);
 
         /* Do Lon */
         AttributeFactory.addLonBoundsMetadata(ncds, ft);
 
         /* Do Vert */
         AttributeFactory.addVertBoundsMetadata(ncds, ft);
     }
 
 //  private double mbToMetersPosDown(double mbPressure) {
 //      /* From:  http://www.4wx.com/wxcalc/formulas/pressureAltitude.php  */
 //      double ft = (1 - (Math.pow(mbPressure / 1013.25, 0.190284))) * 145366.45;
 //      /* Convert feet to meters */
 //      return -ft * 0.3048;
 //  }
     /**
      * Sends the <code>byte[]</code> representation of a dataset to the remote operation specified by {@link #RECV_DATASET_OP} using the
      * currently active <code>MsgBrokerClient</code>. This message send is one way, that is, the message is NOT sent via RPC conventions
      * 
      * @param dataMessageContent
      *            A serialized <code>byte[]</code> representation of a dataset
      * 
      * @see #initMsgBrokerClient(HashMap)
      * @see Unidata2Ooi#ncdfToByteArray(NetcdfDataset)
      * @see Unidata2Ooi#ncdfToByteArray(NetcdfDataset, boolean)
      */
     protected void sendDatasetMsg(byte[] dataMessageContent) {
         if (Thread.currentThread().isInterrupted()) {
             ingestError = true;
             return;
         }
         switch (runType) {
             case NORMAL:
                 IonMessage dataMessage = constructNonRPCMessage(fromName, toName, RECV_DATASET_OP, dataMessageContent, ResponseCodes.OK);
 //                IonMessage dataMessage = cl.createMessage(fromName, toName, RECV_DATASET_OP, dataMessageContent);
 
                 if (log.isInfoEnabled()) {
                     log.info("@@@--->>> Send dataset message to ingest");
                     if (log.isDebugEnabled()) {
                         log.debug("\n{}", dataMessage);
                     }
                 }
                 cl.sendMessage(dataMessage);
                 break;
             case TEST_WRITE_OOICDM:
                 writeDatasetProto(dataMessageContent);
                 break;
         }
         // return cl.consumeMessage(recieverQueue);
     }
 
     /**
      * Sends the <code>byte[]</code> representation of a dataset "section" to the remote operation specified by {@link #RECV_CHUNK_OP} using
      * the currently active <code>MsgBrokerClient</code>. This message send is one way, that is, the message is NOT sent via RPC conventions
      * 
      * @param dataMessageContent
      *            A serialized <code>byte[]</code> representation of a dataset section
      * 
      * @see #initMsgBrokerClient(HashMap)
      * @see Unidata2Ooi#ncdfToByteArray(NetcdfDataset)
      * @see Unidata2Ooi#ncdfToByteArray(NetcdfDataset, boolean)
      */
     protected void sendDataChunkMsg(byte[] dataMessageContent) {
         if (Thread.currentThread().isInterrupted()) {
             ingestError = true;
             return;
         }
         switch (runType) {
             case NORMAL:
                 IonMessage dataMessage = constructNonRPCMessage(fromName, toName, RECV_CHUNK_OP, dataMessageContent, ResponseCodes.OK);
 //                IonMessage dataMessage = cl.createMessage(fromName, toName, RECV_CHUNK_OP, dataMessageContent);
 //                dataMessage.getIonHeaders().put("encoding", "ION R1 GPB");
 //                dataMessage.getIonHeaders().put("conv-id", "");
 //                dataMessage.getIonHeaders().put("sender-name", procName);
                 if (log.isInfoEnabled()) {
                     log.info("@@@--->>> Send data chunk message to ingest");
                     if (log.isDebugEnabled()) {
                         log.debug("\n{}", dataMessage);
                     }
                 }
                 cl.sendMessage(dataMessage);
                 break;
             case TEST_WRITE_OOICDM:
                 writeChunkProto(dataMessageContent);
                 break;
         }
     }
 
     /**
      * Signals the service ingesting dataset data that the most recent stream of "sends" is complete by sending notification to the remote
      * operation specified by {@link #RECV_DONE_OP} using the currently active <code>MsgBrokerClient</code>. This message send is one way,
      * that is, the message is NOT sent via RPC conventions
      * 
      * @see #initMsgBrokerClient(HashMap)
      * @see Unidata2Ooi#ncdfToByteArray(NetcdfDataset)
      * @see Unidata2Ooi#ncdfToByteArray(NetcdfDataset, boolean)
      */
     protected void sendDataDoneMsg() {
         /* Put in an IonMsg as the head pointing to the ds element */
         IonMsg.Builder ionMsgBldr = IonMsg.newBuilder();
         ionMsgBldr.setIdentity(UUID.randomUUID().toString());
         ionMsgBldr.setResponseCode(net.ooici.core.message.IonMessage.ResponseCodes.OK);
         /* MessageObject is an instance of DataAcquisitionComplete */
         GPBWrapper<DataAcquisitionCompleteMessage> dacmWrap = GPBWrapper.Factory(DataAcquisitionCompleteMessage.newBuilder().setStatus(DataAcquisitionCompleteMessage.StatusCode.OK).setStatusBody("Data acquisition completed normally").build());
         ionMsgBldr.setMessageObject(dacmWrap.getCASRef());
         GPBWrapper ionMsgWrap = GPBWrapper.Factory(ionMsgBldr.build());
 
         net.ooici.core.container.Container.Structure.Builder sbldr = net.ooici.core.container.Container.Structure.newBuilder();
         ProtoUtils.addStructureElementToStructureBuilder(sbldr, dacmWrap.getStructureElement());
         ProtoUtils.addStructureElementToStructureBuilder(sbldr, ionMsgWrap.getStructureElement(), true);
 
         IonMessage dataMessage = constructNonRPCMessage(fromName, toName, RECV_DONE_OP, sbldr.build().toByteArray(), ResponseCodes.OK);
 //        IonMessage dataMessage = cl.createMessage(fromName, toName, RECV_DONE_OP, sbldr.build().toByteArray());
 //        dataMessage.getIonHeaders().put("encoding", "ION R1 GPB");
 //        dataMessage.getIonHeaders().put("conv-id", "");
 //        dataMessage.getIonHeaders().put("sender-name", procName);
         if (log.isInfoEnabled()) {
             log.info("@@@--->>> Send data \"DONE\" message to ingest");
             if (log.isDebugEnabled()) {
                 log.debug("\n{}", dataMessage);
             }
         }
         cl.sendMessage(dataMessage);
     }
 
     /**
      * Sends a <code>DataAcquisitionCompleteMessage</code> to the ingest service indicating that an error occurred while processing the supplement
      * @param status the <code>DataAcquisitionCompleteMessage.StatusCode</code> that indicates the reason for failure
      * @param statusBody a statement describing the reason for failure
      */
     protected void sendDataErrorMsg(DataAcquisitionCompleteMessage.StatusCode status, String statusBody) {
         /* Put in an IonMsg as the head pointing to the ds element */
         IonMsg.Builder ionMsgBldr = IonMsg.newBuilder();
         ionMsgBldr.setIdentity(UUID.randomUUID().toString());
         ionMsgBldr.setResponseCode(net.ooici.core.message.IonMessage.ResponseCodes.OK);
 
         if (log.isDebugEnabled()) {
             log.debug("Error Message:: status={} : statusBody={}", status, statusBody);
         }
 
         /* MessageObject is an instance of DataAcquisitionComplete */
         GPBWrapper<DataAcquisitionCompleteMessage> dacmWrap = GPBWrapper.Factory(DataAcquisitionCompleteMessage.newBuilder().setStatus(status).setStatusBody(statusBody).build());
         ionMsgBldr.setMessageObject(dacmWrap.getCASRef());
         GPBWrapper ionMsgWrap = GPBWrapper.Factory(ionMsgBldr.build());
 
         net.ooici.core.container.Container.Structure.Builder sbldr = net.ooici.core.container.Container.Structure.newBuilder();
         ProtoUtils.addStructureElementToStructureBuilder(sbldr, dacmWrap.getStructureElement());
         ProtoUtils.addStructureElementToStructureBuilder(sbldr, ionMsgWrap.getStructureElement(), true);
 
         IonMessage dataMessage = constructNonRPCMessage(fromName, toName, RECV_DONE_OP, sbldr.build().toByteArray(), ResponseCodes.OK);
 //        IonMessage dataMessage = cl.createMessage(fromName, toName, RECV_DONE_OP, sbldr.build().toByteArray());
 //        dataMessage.getIonHeaders().put("encoding", "ION R1 GPB");
 //        dataMessage.getIonHeaders().put("conv-id", "");
 //        dataMessage.getIonHeaders().put("sender-name", procName);
         if (log.isInfoEnabled()) {
             log.info("@@@--->>> Send \"ERROR\" message to ingest");
             if (log.isDebugEnabled()) {
                 log.debug("\n{}", dataMessage);
             }
         }
         cl.sendMessage(dataMessage);
     }
 
     /**
      * Initializes this abstractDatasetAgent's internal MsgBrokerClient using the given connectionInfo
      * 
      * @param connectionInfo
      *            Parameters specifying how to initialize the MsgBrokerClient
      * 
      * @throws IonException
      *             When there is an issue initializing the MsgBrokerClient such as during attach(), bindQueue(), or attachConsumer()
      * @throws IllegalArgumentException
      *             When connectionInfo provides missing or invalid arguments
      */
     private void initMsgBrokerClient(HashMap<String, String> connectionInfo) throws IonException {
         String topic = connectionInfo.get("ion.ingest_topic");
         String host = connectionInfo.get("ion.host");
         String exchange = connectionInfo.get("ion.exchange");
 
         if (null == topic) {
             throw new IllegalArgumentException("Cannot initialize the MsgBrokerClient: The given connection info is missing an argument for 'ion.ingest_topic'");
         }
         if (null == host) {
             throw new IllegalArgumentException("Cannot initialize the MsgBrokerClient: The given connection info is missing an argument for 'ion.host'");
         }
         if (null == exchange) {
             throw new IllegalArgumentException("Cannot initialize the MsgBrokerClient: The given connection info is missing an argument for 'ion.exchange'");
         }
 
         /* Any of these can throw an IonException */
         toName = new ion.core.messaging.MessagingName(topic);
         fromName = ion.core.messaging.MessagingName.generateUniqueName();
 
         cl = new ion.core.messaging.MsgBrokerClient(host, com.rabbitmq.client.AMQP.PROTOCOL.PORT, exchange);
         cl.attach();
 
         recieverQueue = cl.declareQueue(null);
 
         cl.bindQueue(recieverQueue, fromName, null);
         cl.attachConsumer(recieverQueue);
         if (log.isDebugEnabled()) {
             log.debug("Message Broker Attached:: messagingName={} : host={} : exchange={}", new Object[]{toName, host, exchange});
         }
     }
 
     private void closeMsgBrokerClient() {
         if (cl != null) {
             cl.detach();
             cl = null;
             if (log.isDebugEnabled()) {
                 log.debug("Message Broker Detatched");
             }
         }
     }
 
     private IonMessage constructNonRPCMessage(MessagingName fromName, MessagingName toName, String op, byte[] content, ResponseCodes status) {
         IonMessage ret = cl.createMessage(fromName, toName, op, content);
         ret.getIonHeaders().put("encoding", "ION R1 GPB");
         ret.getIonHeaders().put("protocol", "");
         ret.getIonHeaders().put("conv-id", "");
         ret.getIonHeaders().put("sender-name", procName);
         ret.getIonHeaders().put("performative", "");
         ret.getIonHeaders().put("ontology", "");
         ret.getIonHeaders().put("language", "ion1");
         ret.getIonHeaders().put("status", status.toString());
         return ret;
     }
 
     private void writeDatasetProto(byte[] payload) {
        String datasetName = this.datasetName.replace(" ", "_");
         new java.io.File(outputDir, datasetName).mkdirs();
         datasetName = datasetName + "/" + datasetName + ".ooicdm";
         if (log.isDebugEnabled()) {
             log.debug("Write proto message to disk --> \"{}\"", datasetName);
         }
         writeBytes(payload, outputDir + datasetName);
     }
 
     private void writeChunkProto(byte[] payload) {
        String datasetName = this.datasetName.replace(" ", "_");
         new java.io.File(outputDir, datasetName).mkdirs();
         datasetName = datasetName + "/" + datasetName + "_" + incrementor++ + ".ooicdm";
         if (log.isDebugEnabled()) {
             log.debug("Write proto message to disk --> \"{}\"", datasetName);
         }
         writeBytes(payload, outputDir + datasetName);
     }
 
     private void writeBytes(byte[] bytes, String name) {
         java.io.FileOutputStream fos = null;
         try {
             fos = new java.io.FileOutputStream(name);
             fos.write(bytes);
             fos.flush();
         } catch (IOException ex) {
             log.error("Failed to write bytes", ex);
         } finally {
             if (fos != null) {
                 try {
                     fos.close();
                 } catch (IOException ex) {
                     // no op
                 }
             }
         }
     }
 //    protected String sendNetcdfVariable(String datasetID, ucar.nc2.Variable var, String op) {
 //        return sendNetcdfVariable(datasetID, var, var.getShapeAsSection(), op);
 //
 //
 //    }
 //
 //    protected String sendNetcdfVariable(String datasetID, ucar.nc2.Variable var, ucar.ma2.Section section, String op) {
 //        assert var != null;
 //
 //        String ret = null;
 //        /* Package the variable */
 //        /* Build the OOICI Canonical Representation of the variable and serialize as a byte[] */
 //
 //
 //        byte[] dataMessageContent;
 //
 //
 //        try {
 //            if (!runType) {
 //                dataMessageContent = Unidata2Ooi.varToByteArray(var, section);
 //                IonMessage reply = rpcDataMessage(op, dataMessageContent);
 //                ret = reply.getContent().toString();
 //
 //
 //            } else {
 //                ret = var.toString();
 //
 //
 //            }
 //        } catch (IOException ex) {
 //            log.error("Error converting NetcdfDataset to OOICI CDM");
 //            dataMessageContent = null;
 //
 //
 //        }
 //
 //        return ret;
 //
 //
 //    }
 }
