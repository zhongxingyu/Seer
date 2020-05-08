 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.ooici.netcdf.iosp;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 import com.rabbitmq.client.AMQP;
 import ion.core.IonException;
 import ion.core.messaging.IonMessage;
 import ion.core.messaging.MessagingName;
 import ion.core.messaging.MsgBrokerClient;
 import ion.core.utils.GPBWrapper;
 import ion.core.utils.IonConstants;
 import ion.core.utils.IonUtils;
 import ion.core.utils.ProtoUtils;
 import ion.core.utils.StructureManager;
 import java.io.File;
 import java.io.IOException;
 import java.nio.channels.WritableByteChannel;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import net.ooici.cdm.syntactic.Cdmarray;
 import net.ooici.cdm.syntactic.Cdmattribute;
 import net.ooici.cdm.syntactic.Cdmdatatype;
 import net.ooici.cdm.syntactic.Cdmdimension;
 import net.ooici.cdm.syntactic.Cdmgroup;
 import net.ooici.cdm.syntactic.Cdmvariable;
 import net.ooici.core.link.Link;
 import net.ooici.core.message.IonMessage.IonMsg;
 import net.ooici.core.message.IonMessage.ResponseCodes;
 import net.ooici.core.workbench_messages.DataAccess;
 import net.ooici.data.cdm.Cdmdataset;
 import net.ooici.services.coi.ResourceFramework;
 import ucar.ma2.Array;
 import ucar.ma2.ArrayChar;
 import ucar.ma2.DataType;
 import ucar.ma2.Index;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.ma2.Section;
 import ucar.ma2.StructureDataIterator;
 import ucar.nc2.Attribute;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.ParsedSectionSpec;
 import ucar.nc2.Structure;
 import ucar.nc2.Variable;
 import ucar.nc2.dataset.NetcdfDataset;
 import ucar.nc2.iosp.IospHelper;
 import ucar.nc2.util.CancelTask;
 import ucar.unidata.io.RandomAccessFile;
 
 /**
  *  IOSP implementation for direct communication with the OOI-CI Exchange
  * <p>
  *
  * Unidata implementation tutorial: {@link http://www.unidata.ucar.edu/software/netcdf-java/tutorial/IOSPdetails.html}
  *
  * @author cmueller
  */
 public class OOICIiosp implements ucar.nc2.iosp.IOServiceProvider {
 
     private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OOICIiosp.class);
     private NetcdfFile ncfile;
     private String datasetResourceId;
     private String treeish;
     private static String ooiciHost = "";
     private static String ooiciExchange = "";
     private static String ooiciSysname = "";
     private static String ooiciTopic = "";
     private MsgBrokerClient mainBroker = null;
     private MsgBrokerClient dataBroker = null;
     private ion.core.messaging.MessagingName ooiDatastoreName;
     private ion.core.messaging.MessagingName ooiMyName;
     private String mainQueue;
     private StructureManager datasetManager;
     private HashMap<Variable, Cdmvariable.Variable> _varMap;
     private HashMap<String, Cdmdimension.Dimension> _dimMap;
     private static boolean initialized = false;
 
     public OOICIiosp() throws InstantiationException {
         if (!initialized) {
             try {
                 init();
             } catch (IOException ex) {
                 throw new InstantiationException(new StringBuilder("Error: Missing OOICI connection information.  An \"").append(IonConstants.OOICI_CONN_FILENAME).append("\" file is required to use this functionality.  The file should be located in the current users home directory: \"").append(System.getProperty("user.home")).append("\"").toString());
             }
         }
     }
 
     private static void _init() {
         ooiciHost = System.getProperty(IonConstants.HOSTNAME_KEY, IonConstants.HOSTNAME_DEFAULT);
         ooiciExchange = System.getProperty(IonConstants.EXCHANGE_KEY, IonConstants.EXCHANGE_DEFAULT);
         ooiciSysname = System.getProperty(IonConstants.SYSNAME_KEY, IonConstants.SYSNAME_DEFAULT);
         ooiciTopic = System.getProperty(IonConstants.DATASTORE_TOPIC_KEY, IonConstants.DATASTORE_TOPIC_DEFAULT);
         if (log.isInfoEnabled()) {
             log.info("OOICI IOSP Connection Parameters:: host={} : exchange={} : topic={} : sysname={}", new Object[]{ooiciHost, ooiciExchange, ooiciTopic, ooiciSysname});
         }
         initialized = true;
     }
 
     public static boolean init() throws IOException {
         if (!initialized) {
             IonUtils.parseProperties();
             _init();
         }
         return initialized;
     }
 
     public static boolean init(final File ooiciConnFile) throws IOException {
         if (!initialized) {
             IonUtils.parseProperties(ooiciConnFile);
             _init();
         }
         return initialized;
     }
 
     @Deprecated
     public static boolean init(final java.util.HashMap<String, String> connInfo) {
         if (!initialized) {
             ooiciHost = connInfo.get("host");
             ooiciExchange = connInfo.get("xp_name");
             ooiciSysname = connInfo.get("sysname");
             ooiciTopic = connInfo.get("datastore_topic");
             if (log.isDebugEnabled()) {
                 log.debug("OOICI IOSP Connection Parameters:: host={} : exchange={} : topic={} : sysname={}", new Object[]{ooiciHost, ooiciExchange, ooiciTopic, ooiciSysname});
             }
             initialized = true;
         }
         return initialized;
     }
 
     public boolean isValidFile(RandomAccessFile raf) throws IOException {
         /**
          * Claim the file as "OOI-CI"
          */
        return raf.getLocation().toLowerCase().startsWith("ooici://");
     }
 
     private void initBroker() {
     }
 
     public void open(RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask) throws IOException {
         if (ooiciHost == null || ooiciHost.isEmpty()) {
             StringBuilder sb = new StringBuilder("Error: Missing OOICI connection information.  An \"").append(IonConstants.OOICI_CONN_FILENAME).append("\" file is required to use this functionality.  The file should be located in the current users home directory: \"").append(System.getProperty("user.home")).append("\"");
             throw new IOException(sb.toString());
         }
         /**
          * Open the connection to OOI-CI, register data objects as necessary, attach the broker
          */
         ooiDatastoreName = new MessagingName(ooiciSysname, ooiciTopic);
         ooiMyName = MessagingName.generateUniqueName();
         mainBroker = new MsgBrokerClient(ooiciHost, AMQP.PROTOCOL.PORT, ooiciExchange);
         try {
             mainBroker.attach();
             if (log.isInfoEnabled()) {
                 log.info("Main Broker Attached:: binding_key={}", ooiMyName);
             }
         } catch (IonException ex) {
             throw new IOException("Error opening file: Could not connect to broker", ex);
         }
         mainQueue = mainBroker.declareQueue(null);
         mainBroker.bindQueue(mainQueue, ooiMyName, null);
         mainBroker.attachConsumer(mainQueue);
 
         /* Set local variables and prepare ncfile object with all metadata: dimensions, variables, attributes, etc */
         this.ncfile = ncfile;
        this.datasetResourceId = raf.getLocation().replaceFirst("ooici://", "");//trim the "ooici://" from the front to obtain the UUID
         /* Use regex to capture any 'treeish' characters that might be at the end of the id */
         Pattern p = Pattern.compile("([@~^].*)$");
         Matcher m = p.matcher(this.datasetResourceId);
         treeish = "";
         if (m.find()) {// Only can be one, so no need to iterate
             treeish = this.datasetResourceId.substring(m.start(1), m.end(1));
             this.datasetResourceId = this.datasetResourceId.replace(treeish, "");
         }
         if (log.isInfoEnabled()) {
             log.info("OOI Dataset Resource ID={} : treeish={}", this.datasetResourceId, treeish);
         }
         retrieveDatasetFromDatastore();
         buildNetcdfDataset();
 
         this.ncfile.finish();
     }
 
     private void retrieveDatasetFromDatastore() throws IOException {
         if (log.isInfoEnabled()) {
             log.info("Retrieving header information for dataset '{}' with treeish '{}'", datasetResourceId, treeish);
         }
         /* Get the dataset "shell" by calling the "get_object" op in the datastore */
         /* Build the IDRef */
         net.ooici.core.link.Link.IDRef idref = net.ooici.core.link.Link.IDRef.newBuilder().setKey(datasetResourceId).setTreeish(treeish).build();
         GPBWrapper idWrap = GPBWrapper.Factory(idref);
         if (log.isDebugEnabled()) {
             log.debug("IdRef:\n{}", idWrap.toString());
         }
 
         /* Exclude StructureArray (10025) type */
         net.ooici.core.type.Type.GPBType excludeType = net.ooici.core.type.Type.GPBType.newBuilder().setObjectId(10025).setVersion(1).build();
         GPBWrapper exWrap = GPBWrapper.Factory(excludeType);
         if (log.isDebugEnabled()) {
             log.debug("Exclude Type(s):\n{}", exWrap.toString());
         }
 
         /* Build the GetObjectRequestMessage */
         net.ooici.core.workbench_messages.DataAccess.GetObjectRequestMessage objReq = net.ooici.core.workbench_messages.DataAccess.GetObjectRequestMessage.newBuilder().setObjectId(idWrap.getCASRef()).addExcludedObjectTypes(exWrap.getCASRef()).build();
         GPBWrapper objReqWrap = GPBWrapper.Factory(objReq);
         if (log.isDebugEnabled()) {
             log.debug("DataAccess.GetObjectRequestMessage:\n{}", objReqWrap.toString());
         }
 
         /* Make an IonMsg */
         net.ooici.core.message.IonMessage.IonMsg ionMsg = net.ooici.core.message.IonMessage.IonMsg.newBuilder().setMessageObject(objReqWrap.getCASRef()).build();
         GPBWrapper ionWrap = GPBWrapper.Factory(ionMsg);
         if (log.isDebugEnabled()) {
             log.debug("IonMsg:\n{}", ionWrap.toString());
         }
 
         /* Load everything into a structure */
         net.ooici.core.container.Container.Structure.Builder sbldr = net.ooici.core.container.Container.Structure.newBuilder();
         ProtoUtils.addStructureElementToStructureBuilder(sbldr, idWrap.getStructureElement());
         ProtoUtils.addStructureElementToStructureBuilder(sbldr, objReqWrap.getStructureElement());
         ProtoUtils.addStructureElementToStructureBuilder(sbldr, exWrap.getStructureElement());
         ProtoUtils.addStructureElementToStructureBuilder(sbldr, ionWrap.getStructureElement(), true);// IonMsg is head
 
         IonMessage sendMessage = mainBroker.createMessage(ooiMyName, ooiDatastoreName, "get_object", sbldr.build().toByteArray());
         sendMessage.getIonHeaders().put("encoding", "ION R1 GPB");
         sendMessage.getIonHeaders().put("user-id", "ANONYMOUS");
         sendMessage.getIonHeaders().put("expiry", "0");
         sendMessage.getIonHeaders().put("performative", "request");
         if (log.isDebugEnabled()) {
             log.debug("Sending Message:\n{}", sendMessage.toString());
         }
 
         /* Send the message to the datastore*/
         mainBroker.sendMessage(sendMessage);
         IonMessage repMessage = mainBroker.consumeMessage(mainQueue, 30000);//set timeout to 30 seconds
         if (log.isDebugEnabled()) {
             log.debug("Received Message:\n{}", repMessage.toString());
         }
 
         /* Parse the response into a StructureManager */
         if (repMessage != null) {
             datasetManager = StructureManager.Factory(repMessage);
             if (log.isDebugEnabled()) {
                 log.debug("Dataset Structure:\n{}", datasetManager.toString());
             }
         } else {
             throw new IonException("Error retrieving dataset with ID=" + datasetResourceId + " from datastore 'op_get_object' ==> the request probably timed out.  Check your internet connection and ooici connection settings.");
         }
         if (log.isInfoEnabled()) {
             log.info("Dataset headers retrieved successfully!");
         }
     }
 
     private void buildNetcdfDataset() throws IOException {
         if (log.isInfoEnabled()) {
             log.info("Building NetCDF dataset from ION CDM structure");
         }
         /* Get Head IonMsg */
         GPBWrapper<IonMsg> ionMsgWrap = datasetManager.getObjectWrapper(datasetManager.getHeadId());
         if (log.isDebugEnabled()) {
             log.debug("IonMsg:\n{}", ionMsgWrap.toString());
         }
         IonMsg ionMsg = ionMsgWrap.getObjectValue();
         if (ionMsg.getResponseCode() != ResponseCodes.OK) {
             throw new IOException("Error retrieving the requested resource: " + ionMsg.getResponseCode() + " ==> " + ionMsg.getResponseBody());
         }
 
         GPBWrapper<DataAccess.GetObjectReplyMessage> objRepWrap = datasetManager.getObjectWrapper(ionMsg.getMessageObject());
         if (log.isDebugEnabled()) {
             log.debug("DataAccess.GetObjectReplyMessage:\n{}", objRepWrap.toString());
         }
         DataAccess.GetObjectReplyMessage objReply = objRepWrap.getObjectValue();
 
         GPBWrapper<ResourceFramework.OOIResource> resWrap = datasetManager.getObjectWrapper(objReply.getRetrievedObject());
         if (log.isDebugEnabled()) {
             log.debug("ResourceFramework.OOIResource:\n{}", resWrap.toString());
         }
         ResourceFramework.OOIResource resource = resWrap.getObjectValue();
 
         GPBWrapper<Cdmdataset.Dataset> dsWrap = datasetManager.getObjectWrapper(resource.getResourceObject());
         if (log.isDebugEnabled()) {
             log.debug("Cdmdataset.Dataset:\n{}", dsWrap.toString());
         }
         Cdmdataset.Dataset dataset = dsWrap.getObjectValue();
 
         GPBWrapper<Cdmgroup.Group> rootWrap = datasetManager.getObjectWrapper(dataset.getRootGroup());
         Cdmgroup.Group rootGroup = rootWrap.getObjectValue();
         Dimension dim;
         /* Initialize the dimMap */
         _dimMap = new HashMap<String, Cdmdimension.Dimension>();
         for (Link.CASRef cref : rootGroup.getDimensionsList()) {
             dim = getNcDimension(cref);
             if (log.isDebugEnabled()) {
                 log.debug("Add Dim = {}", dim.getName());
             }
             ncfile.addDimension(null, dim);
         }
         Attribute att;
         for (Link.CASRef cref : rootGroup.getAttributesList()) {
             att = getNcAttribute(cref);
             if (log.isDebugEnabled()) {
                 log.debug("Add Att = {}", att.getName());
             }
             ncfile.addAttribute(null, att);
         }
         /* Initialize the varMap */
         _varMap = new HashMap<Variable, Cdmvariable.Variable>();
         /* Process the variables */
         Variable v;
         for (Link.CASRef cref : rootGroup.getVariablesList()) {
             try {
                 v = getNcVariable(cref);
                 if (log.isDebugEnabled()) {
                     log.debug("Add Var = {}", v.getName());
                 }
                 ncfile.addVariable(null, v);
             } catch (InvalidRangeException ex) {
                 throw new IOException("Error building variable, cannot continue", ex);
             }
         }
         if (log.isInfoEnabled()) {
             log.info("NetCDF dataset built successfully!");
         }
     }
 
     public Array readData(Variable v2, Section section) throws IOException, InvalidRangeException {
         if (log.isInfoEnabled()) {
             log.info("Retrieve data for variable '{}'", v2.getName());
         }
         Array arr = null;
         DataType varDataType = v2.getDataType();
 
         /* Get reference to the variable */
         Cdmvariable.Variable ooiVar = _varMap.get(v2);
 
         MessagingName dataName = MessagingName.generateUniqueName();
         try {
             /* Generate a new broker for dealing with the data messages */
             dataBroker = new MsgBrokerClient(ooiciHost, AMQP.PROTOCOL.PORT, ooiciExchange);
             try {
                 dataBroker.attach();
                 if (log.isInfoEnabled()) {
                     log.info("Data Broker Attached:: binding_key={}", dataName);
                 }
             } catch (IonException ex) {
                 throw new IOException("Error connecting to broker for data transfer", ex);
             }
             String dataQueue = dataBroker.declareQueue(null);
             dataBroker.bindQueue(dataQueue, dataName, null);
             dataBroker.attachConsumer(dataQueue);
             if (log.isDebugEnabled()) {
                 log.debug("DataBroker || binding_key={} : \"to\" name={} : queue_name = {}", new Object[]{dataName, ooiDatastoreName, dataQueue});
             }
 
             /* Build the dataRequestMessage */
             net.ooici.core.workbench_messages.DataAccess.DataRequestMessage.Builder drmBldr = net.ooici.core.workbench_messages.DataAccess.DataRequestMessage.newBuilder();
             drmBldr.setDataRoutingKey(dataName.getName());
             drmBldr.setStructureArrayRef(ooiVar.getContent().getKey());
             long dimOffset;
             Range r;
             String dimName;
             for (int i = 0; i < section.getRank(); i++) {
                 r = section.getRange(i);
                 dimName = r.getName();
                 if (dimName == null) {
                     dimName = v2.getDimension(i).getName();
                 }
                 dimOffset = _dimMap.get(dimName).getMinOffset();
                 drmBldr.addRequestBounds(Cdmvariable.Bounds.newBuilder().setOrigin(r.first() + dimOffset).setSize(r.last() - r.first() + 1).setStride(r.stride()).build());
             }
             GPBWrapper drmWrap = GPBWrapper.Factory(drmBldr.build());
             if (log.isDebugEnabled()) {
                 log.debug("DataAccess.DataRequestMessage:\n{}", drmWrap);
             }
 
             /* Make an IonMsg */
             net.ooici.core.message.IonMessage.IonMsg ionMsg = net.ooici.core.message.IonMessage.IonMsg.newBuilder().setMessageObject(drmWrap.getCASRef()).build();
             GPBWrapper ionWrap = GPBWrapper.Factory(ionMsg);
             if (log.isDebugEnabled()) {
                 log.debug("IonMsg:\n{}", ionWrap.toString());
             }
 
             /* Load everything into a structure */
             net.ooici.core.container.Container.Structure.Builder sbldr = net.ooici.core.container.Container.Structure.newBuilder();
             ProtoUtils.addStructureElementToStructureBuilder(sbldr, drmWrap.getStructureElement());
             ProtoUtils.addStructureElementToStructureBuilder(sbldr, ionWrap.getStructureElement(), true);// IonMsg is head
 
             /* Send the request using the dataClient, go into loop to receive reply(ies) */
             IonMessage request = mainBroker.createMessage(ooiMyName, ooiDatastoreName, "extract_data", sbldr.build().toByteArray());
             request.getIonHeaders().put("encoding", "ION R1 GPB");
             request.getIonHeaders().put("user-id", "ANONYMOUS");
             request.getIonHeaders().put("expiry", "0");
             request.getIonHeaders().put("performative", "request");
             if (log.isDebugEnabled()) {
                 log.debug("Sending Message:\n{}", request.toString());
             }
             mainBroker.sendMessage(request);
 
             /* Initialize the ucar array and get the index */
             arr = Array.factory(varDataType, section.getShape());
             Index index = arr.getIndex();
 
             IonMessage rep;
             StructureManager dataManager;
             boolean done = false;
             while (!done) {
                 rep = dataBroker.consumeMessage(dataQueue, 120000);// 2 minute timeout
                 if (rep != null) {
                     dataManager = StructureManager.Factory(rep);
                     /* Get the IonMsg (head) */
                     GPBWrapper<IonMsg> ionMsgWrap = dataManager.getObjectWrapper(dataManager.getHeadId());
                     IonMsg dataMsg = ionMsgWrap.getObjectValue();
                     if (!rep.isErrorMessage()) {
                         /* Get the DataChunk message */
                         GPBWrapper<DataAccess.DataChunkMessage> dataChunkWrap = dataManager.getObjectWrapper(dataMsg.getMessageObject());
                         DataAccess.DataChunkMessage dataChunk = dataChunkWrap.getObjectValue();
 
                         /* Set the "doneness" */
                         done = dataChunk.getDone();
                         /* Get the start index */
                         int si = dataChunk.getStartIndex();
 
                         /* Get the dataWrapper */
                         GPBWrapper dataWrap = dataManager.getObjectWrapper(dataChunk.getNdarray());
                         if (log.isDebugEnabled()) {
                             log.debug(dataWrap.toString());
                         }
                         /* Set the current counter on the index to the startIndex */
                         index.setCurrentCounter(si);
 
                         switch (varDataType) {
                             case BYTE:
                                 Cdmarray.int32Array i32arrByte = (Cdmarray.int32Array) dataWrap.getObjectValue();
                                 for (int i = 0; i < i32arrByte.getValueCount(); i++) {
                                     arr.setByte(index.currentElement(), (byte) i32arrByte.getValue(i));
                                     index.incr();
                                 }
                                 break;
                             case SHORT:
                                 Cdmarray.int32Array i32arrShort = (Cdmarray.int32Array) dataWrap.getObjectValue();
                                 for (int i = 0; i < i32arrShort.getValueCount(); i++) {
                                     arr.setShort(index.currentElement(), (short) i32arrShort.getValue(i));
                                     index.incr();
                                 }
                                 break;
                             case INT:
                                 Cdmarray.int32Array i32arr = (Cdmarray.int32Array) dataWrap.getObjectValue();
                                 for (int i = 0; i < i32arr.getValueCount(); i++) {
                                     arr.setInt(index.currentElement(), i32arr.getValue(i));
                                     index.incr();
                                 }
                                 break;
                             case LONG:
                                 Cdmarray.int64Array i64arr = (Cdmarray.int64Array) dataWrap.getObjectValue();
                                 for (int i = 0; i < i64arr.getValueCount(); i++) {
                                     arr.setLong(index.currentElement(), i64arr.getValue(i));
                                     index.incr();
                                 }
                                 break;
                             case FLOAT:
                                 Cdmarray.f32Array f32arr = (Cdmarray.f32Array) dataWrap.getObjectValue();
                                 for (int i = 0; i < f32arr.getValueCount(); i++) {
                                     arr.setFloat(index.currentElement(), f32arr.getValue(i));
                                     index.incr();
                                 }
                                 break;
                             case DOUBLE:
                                 Cdmarray.f64Array f64arr = (Cdmarray.f64Array) dataWrap.getObjectValue();
                                 for (int i = 0; i < f64arr.getValueCount(); i++) {
                                     arr.setDouble(index.currentElement(), f64arr.getValue(i));
                                     index.incr();
                                 }
                                 break;
                             case CHAR:
                                 Cdmarray.stringArray sarr = (Cdmarray.stringArray) dataWrap.getObjectValue();
                                 ArrayChar arrC = (ArrayChar) arr;
                                 for (int i = 0; i < sarr.getValueCount(); i++) {
                                     arrC.setString(index.currentElement(), sarr.getValue(i));
                                     index.incr();
                                 }
                                 break;
                             default:
                                 throw new IonException("Unsupported datatype = " + varDataType.name());
                         }
                     } else {
                         throw new IonException("The reply was an error:\n************************************\n" + dataMsg + "************************************\n");
                     }
                 } else {
                     throw new IonException("The reply was null or timed out");
                 }
             }
 
         } catch (Exception ex) {
             throw new IOException("Error encountered while attempting to retrieve data", ex);
         } finally {
             if (dataBroker != null) {
                 dataBroker.detach();
                 dataBroker = null;
                 if (log.isInfoEnabled()) {
                     log.info("Data Broker Detached:: binding_key={}", dataName);
                 }
             }
             IonMessage repMessage = mainBroker.consumeMessage(mainQueue, 10000);//10 second timeout
             if (repMessage != null) {
                 mainBroker.ackMessage(repMessage);
             }
         }
         if (log.isInfoEnabled()) {
             log.info("Data for variable '{}' retrieved successfully!", v2.getName());
         }
         return arr;
     }
 
     public long readToByteChannel(Variable v2, Section section, WritableByteChannel channel) throws IOException, InvalidRangeException {
         Array data = readData(v2, section);
         return IospHelper.copyToByteChannel(data, channel);
     }
 
     public Array readSection(ParsedSectionSpec cer) throws IOException, InvalidRangeException {
         return IospHelper.readSection(cer);
     }
 
     public StructureDataIterator getStructureIterator(Structure s, int bufferSize) throws IOException {
         return null;
     }
 
     /* TODO: Should the broker attach/detach for each operation...? */
     public void close() throws IOException {
         /**
          * Close the connection to OOI-CI, detach the broker
          */
         if (mainBroker != null) {
             mainBroker.detach();
             mainBroker = null;
             if (log.isInfoEnabled()) {
                 log.info("Main Broker Detached:: binding_key={}", ooiMyName);
             }
         }
     }
 
     @Override
     protected void finalize() throws Throwable {
         /* Ensure the broker is detached if the object is abandonded before "close" has been called */
         close();
         super.finalize();
     }
 
     public boolean syncExtend() throws IOException {
         return false;
     }
 
     public boolean sync() throws IOException {
         return false;
     }
 
     public Object sendIospMessage(Object message) {
         return null;
     }
 
     public String toStringDebug(Object o) {
         return "";
     }
 
     public String getDetailInfo() {
         StringBuilder sb = new StringBuilder();
         sb.append("OOI-CI Exchange: ").append(ooiciHost);
         return sb.toString();
     }
 
     public String getFileTypeId() {
         return "OOI-CI";
     }
 
     public String getFileTypeVersion() {
         return "N/A";
     }
 
     public String getFileTypeDescription() {
         return "OOI-CI distributed format";
     }
 
     // <editor-fold defaultstate="collapsed" desc="OOI-CI Helper Methods">
 //    private ion.core.messaging.IonMessage getResource(String ooiResourceID) {
 ////        mainBroker.createSendMessage(ooiMyName, ooiDatastoreName, "retrieve", ooiResourceID);
 //        return mainBroker.consumeMessage(mainQueue);
 //    }
     private Dimension getNcDimension(Link.CASRef ref) {
         GPBWrapper<Cdmdimension.Dimension> dimWrap = datasetManager.getObjectWrapper(ref);
         Cdmdimension.Dimension ooiDim = dimWrap.getObjectValue();
         _dimMap.put(ooiDim.getName(), ooiDim);
         return new Dimension(ooiDim.getName(), (int) ooiDim.getLength());
     }
 
     private Attribute getNcAttribute(Link.CASRef ref) throws IOException {
         GPBWrapper<Cdmattribute.Attribute> attWrap = datasetManager.getObjectWrapper(ref);
         Cdmattribute.Attribute ooiAtt = attWrap.getObjectValue();
         Attribute ncAtt = null;
         int cnt = 0;
         GPBWrapper arrWrap = datasetManager.getObjectWrapper(ooiAtt.getArray());
         switch (ooiAtt.getDataType()) {
             case STRING:
                 Cdmarray.stringArray sarr = (Cdmarray.stringArray) arrWrap.getObjectValue();
 
                 if (sarr.getValueCount() == 1) {
                     ncAtt = new Attribute(ooiAtt.getName(), sarr.getValue(0));
                 } else {
                     StringBuilder sb = new StringBuilder();
                     for (int i = 0; i < sarr.getValueCount() - 1; i++) {
                         sb.append(sarr.getValue(i)).append("\n");
                     }
                     sb.append(sarr.getValue(sarr.getValueCount() - 1));
                     ncAtt = new Attribute(ooiAtt.getName(), sb.toString());
                 }
                 break;
             case BYTE:
                 Cdmarray.int32Array i32arrB = (Cdmarray.int32Array) arrWrap.getObjectValue();
                 ucar.ma2.ArrayByte barr = new ucar.ma2.ArrayByte(new int[]{i32arrB.getValueCount()});
                 for (Integer val : i32arrB.getValueList()) {
                     barr.setByte(cnt++, val.byteValue());
                 }
                 if (barr.getSize() == 1) {
                     ncAtt = new Attribute(ooiAtt.getName(), barr.getByte(0));
                 } else {
                     ncAtt = new Attribute(ooiAtt.getName(), barr);
                 }
                 break;
             case SHORT:
                 Cdmarray.int32Array i32arrS = (Cdmarray.int32Array) arrWrap.getObjectValue();
                 ucar.ma2.ArrayShort sharr = new ucar.ma2.ArrayShort(new int[]{i32arrS.getValueCount()});
                 for (Integer val : i32arrS.getValueList()) {
                     sharr.setShort(cnt++, val.shortValue());
                 }
                 if (sharr.getSize() == 1) {
                     ncAtt = new Attribute(ooiAtt.getName(), sharr.getShort(0));
                 } else {
                     ncAtt = new Attribute(ooiAtt.getName(), sharr);
                 }
                 break;
             case INT:
                 Cdmarray.int32Array i32arr = (Cdmarray.int32Array) arrWrap.getObjectValue();
                 ucar.ma2.ArrayInt iarr = new ucar.ma2.ArrayInt(new int[]{i32arr.getValueCount()});
                 for (Integer val : i32arr.getValueList()) {
                     iarr.setInt(cnt++, val);
                 }
                 if (iarr.getSize() == 1) {
                     ncAtt = new Attribute(ooiAtt.getName(), iarr.getInt(0));
                 } else {
                     ncAtt = new Attribute(ooiAtt.getName(), iarr);
                 }
                 break;
             case LONG:
                 Cdmarray.int64Array i64arr = (Cdmarray.int64Array) arrWrap.getObjectValue();
                 ucar.ma2.ArrayLong larr = new ucar.ma2.ArrayLong(new int[]{i64arr.getValueCount()});
                 for (Long val : i64arr.getValueList()) {
                     larr.setLong(cnt++, val);
                 }
                 if (larr.getSize() == 1) {
                     ncAtt = new Attribute(ooiAtt.getName(), larr.getLong(0));
                 } else {
                     ncAtt = new Attribute(ooiAtt.getName(), larr);
                 }
                 break;
             case FLOAT:
                 Cdmarray.f32Array f32arr = (Cdmarray.f32Array) arrWrap.getObjectValue();
                 ucar.ma2.ArrayFloat farr = new ucar.ma2.ArrayFloat(new int[]{f32arr.getValueCount()});
                 for (Float val : f32arr.getValueList()) {
                     farr.setFloat(cnt++, val);
                 }
                 if (farr.getSize() == 1) {
                     ncAtt = new Attribute(ooiAtt.getName(), farr.getFloat(0));
                 } else {
                     ncAtt = new Attribute(ooiAtt.getName(), farr);
                 }
                 break;
             case DOUBLE:
                 Cdmarray.f64Array f64arr = (Cdmarray.f64Array) arrWrap.getObjectValue();
                 ucar.ma2.ArrayDouble darr = new ucar.ma2.ArrayDouble(new int[]{f64arr.getValueCount()});
                 for (Double val : f64arr.getValueList()) {
                     darr.setDouble(cnt++, val);
                 }
                 if (darr.getSize() == 1) {
                     ncAtt = new Attribute(ooiAtt.getName(), darr.getDouble(0));
                 } else {
                     ncAtt = new Attribute(ooiAtt.getName(), darr);
                 }
                 break;
             /* TODO: Complete for other data types*/
         }
 
         if (ncAtt == null) {
             throw new IOException("Error reading OOICI attribute: attWrapper:" + attWrap.toString() + " arrWrapper: " + arrWrap.toString());
         }
         return ncAtt;
     }
 
     private Variable getNcVariable(Link.CASRef ref) throws InvalidProtocolBufferException, InvalidRangeException, IOException {
         GPBWrapper<Cdmvariable.Variable> varWrap = datasetManager.getObjectWrapper(ref);
         Cdmvariable.Variable ooiVar = varWrap.getObjectValue();
         Variable ncVar = new Variable(ncfile, null, null, ooiVar.getName());
         ncVar.setDataType(IospUtils.getNcDataType(ooiVar.getDataType()));
         ncVar.setDimensions(getDimString(ooiVar.getShapeList()));
         ncVar.resetShape();
         for (Link.CASRef vAttRef : ooiVar.getAttributesList()) {
             ncVar.addAttribute(getNcAttribute(vAttRef));
         }
         _varMap.put(ncVar, ooiVar);
         return ncVar;
     }
 
     /**
      * 
      * @param ref
      * @param dt
      * @param shape
      * @return
      * @throws InvalidProtocolBufferException
      * @throws InvalidRangeException
      * @deprecated Not used by the current retrieval mechanisms
      */
     @Deprecated
     private ucar.ma2.Array getNcArray(Link.CASRef ref, Cdmdatatype.DataType dt, int[] shape) throws InvalidProtocolBufferException, InvalidRangeException {
         GPBWrapper<Cdmvariable.BoundedArray> barrWrap = datasetManager.getObjectWrapper(ref);
         Cdmvariable.BoundedArray bndArr = barrWrap.getObjectValue();
 
         GPBWrapper<Link.CASRef> arrRefWrap = GPBWrapper.Factory(bndArr.getNdarray());
         GPBWrapper arrWrap = datasetManager.getObjectWrapper(arrRefWrap.getObjectValue());
         if (arrWrap == null) {
             /* TODO Don't have this data!! Must retrieve it from the exchange...but how??? Possibly use the bounded array, variable, and dataset to retrieve the array from the exchange. */
             throw new InvalidProtocolBufferException("OOICI : Incremental data retrieval not yet supported. Need CASRef: " + arrRefWrap.getObjectValue());
         }
         List<Cdmvariable.Bounds> bndsList = bndArr.getBoundsList();
 
         boolean isScalar = shape.length == 0;
 
         ucar.ma2.Section sec = new ucar.ma2.Section();
         for (Cdmvariable.Bounds bnds : bndsList) {
             sec.appendRange((int) bnds.getOrigin(), (int) (bnds.getOrigin() + bnds.getSize() - 1));
         }
         ucar.ma2.Section.Iterator secIter = sec.getIterator(sec.getShape());
         ucar.ma2.Array arr = null;
         int cnt = 0;
         switch (dt) {
 //            case STRING:
 //                Cdmarray.stringArray sarr = Cdmarray.stringArray.parseFrom(elementMap.get(ooiAtt.getArray().getKey()).getValue());
 //                if (sarr.getValueCount() == 1) {
 //                    ncAtt = new Attribute(ooiAtt.getName(), sarr.getValue(0));
 //                } else {
 //                    /* Is this possible?? */
 //                }
 //                break;
             case BYTE:
                 Cdmarray.int32Array i32arrB = (Cdmarray.int32Array) arrWrap.getObjectValue();
                 if (isScalar) {
                     arr = new ucar.ma2.ArrayByte.D0();
                     arr.setByte(arr.getIndex(), i32arrB.getValueList().get(0).byteValue());
                 } else {
                     arr = ucar.ma2.Array.factory(IospUtils.getNcDataType(dt), shape);
                     while (secIter.hasNext()) {
                         arr.setLong(secIter.next(), Integer.valueOf(i32arrB.getValue(cnt++)).longValue());
                     }
                 }
                 break;
             case SHORT:
                 Cdmarray.int32Array i32arrS = (Cdmarray.int32Array) arrWrap.getObjectValue();
                 if (isScalar) {
                     arr = new ucar.ma2.ArrayShort.D0();
                     arr.setShort(arr.getIndex(), i32arrS.getValueList().get(0).shortValue());
                 } else {
                     arr = ucar.ma2.Array.factory(IospUtils.getNcDataType(dt), shape);
                     while (secIter.hasNext()) {
                         arr.setShort(secIter.next(), Integer.valueOf(i32arrS.getValue(cnt++)).shortValue());
                     }
                 }
                 break;
             case INT:
                 Cdmarray.int32Array i32arr = (Cdmarray.int32Array) arrWrap.getObjectValue();
                 if (isScalar) {
                     arr = new ucar.ma2.ArrayInt.D0();
                     arr.setInt(arr.getIndex(), i32arr.getValue(0));
                 } else {
                     arr = ucar.ma2.Array.factory(IospUtils.getNcDataType(dt), shape);
                     while (secIter.hasNext()) {
                         arr.setInt(secIter.next(), i32arr.getValue(cnt++));
                     }
                 }
                 break;
             case LONG:
                 Cdmarray.int64Array i64arr = (Cdmarray.int64Array) arrWrap.getObjectValue();
                 if (isScalar) {
                     arr = new ucar.ma2.ArrayLong.D0();
                     arr.setLong(arr.getIndex(), i64arr.getValue(0));
                 } else {
                     arr = ucar.ma2.Array.factory(IospUtils.getNcDataType(dt), shape);
                     while (secIter.hasNext()) {
                         arr.setLong(secIter.next(), i64arr.getValue(cnt++));
                     }
                 }
                 break;
             case FLOAT:
                 Cdmarray.f32Array f32arr = (Cdmarray.f32Array) arrWrap.getObjectValue();
                 if (isScalar) {
                     arr = new ucar.ma2.ArrayFloat.D0();
                     arr.setFloat(arr.getIndex(), f32arr.getValue(0));
                 } else {
                     arr = ucar.ma2.Array.factory(IospUtils.getNcDataType(dt), shape);
                     while (secIter.hasNext()) {
                         arr.setFloat(secIter.next(), f32arr.getValue(cnt++));
                     }
                 }
                 break;
             case DOUBLE:
                 Cdmarray.f64Array f64arr = (Cdmarray.f64Array) arrWrap.getObjectValue();
                 if (isScalar) {
                     arr = new ucar.ma2.ArrayDouble.D0();
                     arr.setDouble(arr.getIndex(), f64arr.getValue(0));
                 } else {
                     arr = ucar.ma2.Array.factory(IospUtils.getNcDataType(dt), shape);
                     while (secIter.hasNext()) {
                         arr.setDouble(secIter.next(), f64arr.getValue(cnt++));
                     }
                 }
                 break;
             /* TODO: Complete for other data types*/
         }
 
         return arr;
     }
 
     private String getDimString(List<Link.CASRef> shapeList) throws InvalidProtocolBufferException {
         StringBuilder dimString = new StringBuilder();
         for (Link.CASRef shpRef : shapeList) {
             dimString.append(((Cdmdimension.Dimension) datasetManager.getObjectWrapper(shpRef).getObjectValue()).getName());
             dimString.append(" ");
         }
         return dimString.toString().trim();
     }
     // </editor-fold>
 
     public static void main(String[] args) throws InstantiationException, IllegalAccessException, InvalidRangeException {
 //        ucar.nc2.dataset.NetcdfDataset.registerIOProvider(OOICIiosp.class);
         ucar.nc2.dataset.NetcdfDataset ncds = null;
         try {
             Section sec = new Section();
            String ds = "ooici://3319A67F-81F3-424F-8E69-4F28C4E04801";//HYCOM sample
//            ds = "ooici://3319A67F-81F3-424F-8E69-4F28C4E04800";//HYCOM_split sample
             String var = "MT";
 //            var = "u_velocity";
 //            sec.appendRange(0, 0, 1);
 //            sec.appendRange(0, 0, 1);
 //            sec.appendRange(5, 10, 2);
 //            sec.appendRange(0, 11, 5);
 
//            ds = "ooici://3319A67F-81F3-424F-8E69-4F28C4E04808";//CT_Station sample
 //            var = "lat";
 //            sec.appendRange(0, 11, 5);
 
            ds = "ooici://4490D06D-B9E0-42FB-8514-E377F190868C";
             var = "time";
 //            var = "u";
 //            sec.appendRange(0, 0, 1);
 //            sec.appendRange(0, 459, 1);
 //            sec.appendRange(0, 700, 1);
 
 
 
             NetcdfDataset.disableNetcdfFileCache();
             ncds = ucar.nc2.dataset.NetcdfDataset.openDataset(ds);
             if (log.isDebugEnabled()) {
                 log.debug(ncds.toString());
             }
 
 
 //            Array a = ncds.findVariable(var).read(sec);
             Array a = ncds.findVariable(var).read();
             if (log.isInfoEnabled()) {
                 log.info("{} Array: {}", var, a);
             }
         } catch (IOException ex) {
             log.error("Error: ", ex);
         } finally {
             if (ncds != null) {
                 try {
                     ncds.close();
                 } catch (IOException ex) {
                     log.error("Error: ", ex);
                 }
             }
         }
     }
 }
