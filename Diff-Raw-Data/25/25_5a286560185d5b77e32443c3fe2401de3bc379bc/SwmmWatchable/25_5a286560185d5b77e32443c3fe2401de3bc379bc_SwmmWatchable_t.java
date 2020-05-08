 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.sudplan.linz;
 
 import Sirius.navigator.connection.SessionManager;
 
 import at.ac.ait.enviro.sudplan.clientutil.SudplanSOSHelper;
 import at.ac.ait.enviro.sudplan.clientutil.SudplanSPSHelper;
 import at.ac.ait.enviro.tsapi.timeseries.TimeInterval;
 import at.ac.ait.enviro.tsapi.timeseries.TimeSeries;
 import at.ac.ait.enviro.tsapi.timeseries.TimeStamp;
 
 import org.apache.log4j.Logger;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.io.IOException;
 
 import java.net.MalformedURLException;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Properties;
 
 import de.cismet.cids.custom.sudplan.AbstractModelRunWatchable;
 import de.cismet.cids.custom.sudplan.commons.SudplanConcurrency;
 
 import de.cismet.cids.dynamics.CidsBean;
 
import de.cismet.commons.utils.ProgressEvent;
import de.cismet.commons.utils.ProgressListener;

 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public class SwmmWatchable extends AbstractModelRunWatchable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(SwmmWatchable.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private transient SudplanSPSHelper.Task spsTask;
     private transient SwmmRunInfo runInfo = null;
     private transient SwmmOutput swmmOutput;
     private transient SwmmModelManager swmmModelManager = null;
     // private EtaOutput etaOutput;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new SwmmWatchable object.
      *
      * @param  swmmModelManager  DOCUMENT ME!
      */
     public SwmmWatchable(final SwmmModelManager swmmModelManager) {
         this(swmmModelManager.getCidsBean());
         this.swmmModelManager = swmmModelManager;
     }
 
     /**
      * Creates a new SwmmWatchable object.
      *
      * @param  cidsBean  DOCUMENT ME!
      */
     public SwmmWatchable(final CidsBean cidsBean) {
         super(cidsBean);
         // this.spsTask = spsTask;
 
         final ObjectMapper mapper = new ObjectMapper();
         final String swmmRunInfo = (String)cidsBean.getProperty("runinfo"); // NOI18N
 
         try {
             runInfo = mapper.readValue(swmmRunInfo, SwmmRunInfo.class);
         } catch (final Exception ex) {
             final String message = "cannot read runInfo from run: " + cidsBean; // NOI18N
             LOG.error(message, ex);
             runInfo = new SwmmRunInfo();
             runInfo.setSpsTaskId("-1");
         }
 
         if ((runInfo.getSpsTaskId() != null) && !runInfo.getSpsTaskId().isEmpty()
                     && !runInfo.getSpsTaskId().equals("-1")) {
             try {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("connecting to SWMM Model SPS: " + runInfo.getSpsUrl());
                 }
                 final SudplanSPSHelper spsHelper = new SudplanSPSHelper(runInfo.getSpsUrl());
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("looking for task '" + runInfo.getSpsTaskId() + "'");
                 }
                 this.spsTask = spsHelper.findTask(runInfo.getSpsTaskId());
                 if (this.spsTask != null) {
                     // logger.swmmRunInfo("Task {}:\n{}\n\n", taskid, DumpTools.dumpTS(task.getTimeseries(), true,
                     // true));
                     final String status = this.spsTask.getStatus();
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Status message from task:" + status);
                         LOG.debug(this.spsTask.getTaskID() + ": status=" + status + "\n");
                     }
                     if (LOG.isDebugEnabled()) {
                         LOG.debug(this.spsTask.getTaskID() + ": errors=" + Arrays.toString(this.spsTask.getErros())
                                     + "\n");
                     }
                     if (LOG.isDebugEnabled()) {
                         LOG.debug(this.spsTask.getTaskID() + ": results="
                                     + Arrays.deepToString(this.spsTask.getResults()) + "\n");
                     }
                 }
 
                 this.setStatus(State.WAITING);
                 LOG.info("new watcheable for swmm run '" + this.runInfo.getSpsTaskId() + "' created");
             } catch (MalformedURLException ex) {
                 LOG.error("could not connect to SPS " + runInfo.getSpsUrl(), ex);
             } catch (IOException ex) {
                 LOG.error("could not restore task '" + runInfo.getSpsTaskId() + "'", ex);
             }
         } else {
             LOG.error("could not restore run information");
             this.setStatus(State.COMPLETED_WITH_ERROR);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public String getTitle() {
         return "Result of SWMM run '" + this.runInfo.getSpsTaskId() + "'"; // NOI18N
     }
 
     @Override
     public void startDownload() {
         final Runnable r = new Runnable() {
 
                 @Override
                 public void run() {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("model run '" + runInfo.getSpsTaskId()
                                     + "' finished, start downloading result from SOS");
                     }
                     setStatus(State.RUNNING);
 
                     try {
                         LOG.info("SWMM Model Run of SPS Task '" + runInfo.getSpsTaskId() + "' completed");
                         final Properties[] resultProperties = spsTask.getResults();
                         if (LOG.isDebugEnabled()) {
                             LOG.debug(resultProperties.length + " results of SPS Task '" + runInfo.getSpsTaskId()
                                         + "' retrieved");
                         }
                         for (final Properties resultProperty : resultProperties) {
                             final String serviceType = resultProperty.getProperty("ts:result_service_type");
                             if ("SOS".equals(serviceType)) {
                                 final String resultSOSEndoint = resultProperty.getProperty("ts:result_service_url");
                                 final String resultSOSOffering = resultProperty.getProperty("ts:offering");
 
                                 // Get result from a SOS
                                 if (((resultSOSEndoint == null) || (resultSOSOffering == null))) {
                                     LOG.error("Missing result properties in " + resultProperty);
                                     continue;
                                 }
 
                                 if (resultSOSOffering.lastIndexOf("_o") != -1) {
                                     swmmOutput = downloadSwmmOutput(resultSOSEndoint, resultSOSOffering);
                                 } else if (resultSOSOffering.lastIndexOf("_e") != -1) {
                                     downloadEtaOutput(swmmOutput, resultSOSEndoint, resultSOSOffering);
                                 } else {
                                     LOG.error("unrecognized offering type '" + resultSOSOffering + "'");
                                 }
                             } else {
                                 LOG.error("Getting the result from a '" + serviceType
                                             + "' service is not implemented!");
                             }
                         }
 
                         if ((swmmOutput != null)) {
                             final Date created = GregorianCalendar.getInstance().getTime();
                             final String user = SessionManager.getSession().getUser().getName();
 
                             swmmOutput.setCreated(created);
                             swmmOutput.setUser(user);
 
                             setStatus(State.COMPLETED);
                         } else {
                             LOG.error("could not download SWMM result!");
                             setStatus(State.COMPLETED_WITH_ERROR);
                         }
                     } catch (final Exception e) {
                         LOG.error("could not download SWMM run results", e); // NOI18N
 
                         setDownloadException(e);
                         setStatus(State.COMPLETED_WITH_ERROR);
 
                         // modelmanager -> fire broken ........
                     }
                 }
             };
 
         SudplanConcurrency.getSudplanDownloadPool().submit(r);
     }
 
     @Override
     public ProgressEvent requestStatus() throws IOException {
         // final String status = this.spsTask.getStatus();
         if (LOG.isDebugEnabled()) {
             LOG.debug("status of swmm model run SPS Task '" + this.runInfo.getSpsTaskId() + "' is '"
                         + spsTask.getStatus() + "'");
         }
 
         // FIXME: list of states
         if ((spsTask.getStatus() == null) || "null".equals(spsTask.getStatus())) {
             final String message = "status of swmm model run SPS Task '" + this.runInfo.getSpsTaskId()
                         + "' is null = broken";
             LOG.error(message);
             if ((this.spsTask.getErros() != null) && (this.spsTask.getErros().length > 0)) {
                 for (final String error : this.spsTask.getErros()) {
                     LOG.error(error);
                 }
             }
             return new ProgressEvent(this, ProgressEvent.State.BROKEN, message);
         } else if ("finished".equals(spsTask.getStatus())) {
             LOG.info("status of swmm model run SPS Task '" + this.runInfo.getSpsTaskId() + "' is finished");
             return new ProgressEvent(this, ProgressEvent.State.FINISHED);
         } else if ((this.spsTask.getErros() != null) && (this.spsTask.getErros().length > 0)) {
             final String message = "status of swmm model run SPS Task '" + this.runInfo.getSpsTaskId() + "' is broken";
             LOG.error(message);
             for (final String error : this.spsTask.getErros()) {
                 LOG.error(error);
             }
             return new ProgressEvent(this, ProgressEvent.State.BROKEN, message);
         } else if ("in operation".equals(spsTask.getStatus())) {
             LOG.info("status of swmm model run SPS Task '" + this.runInfo.getSpsTaskId() + "' is in operation");
             return new ProgressEvent(this, ProgressEvent.State.PROGRESSING);
         } else if ("not yet started".equals(spsTask.getStatus())) {
             LOG.info("status of swmm model run SPS Task '" + this.runInfo.getSpsTaskId() + "' not yet started");
             return new ProgressEvent(this, ProgressEvent.State.STARTED);
         } else {
             LOG.warn("unknown status of swmm model run SPS Task '" + this.runInfo.getSpsTaskId() + "': '"
                         + spsTask.getStatus()
                         + "'");
             return new ProgressEvent(this, ProgressEvent.State.PROGRESSING);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public SwmmOutput getSwmmOutput() {
         return swmmOutput;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public SwmmRunInfo getSwmmRunInfo() {
         return this.runInfo;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   swmmOutput         DOCUMENT ME!
      * @param   resultSOSEndoint   DOCUMENT ME!
      * @param   resultSOSOffering  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private void downloadEtaOutput(final SwmmOutput swmmOutput,
             final String resultSOSEndoint,
             final String resultSOSOffering) throws Exception {
         final TimeInterval allInterval = new TimeInterval(
                 TimeInterval.Openness.OPEN,
                 TimeStamp.NEGATIVE_INFINITY,
                 TimeStamp.POSITIVE_INFINITY,
                 TimeInterval.Openness.OPEN);
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Downloading ETA result offering '" + resultSOSOffering + "' from SOS '" + resultSOSEndoint
                         + "' for time interval '" + allInterval + "'");
         }
 
         final SudplanSOSHelper resultSOShelper = new SudplanSOSHelper(resultSOSEndoint);
         final TimeSeries resultTS = resultSOShelper.getTimeseries(resultSOSOffering, allInterval);
         LOG.info("ETA result downloaded: \n" + resultTS.getTSProperty(" ts:description"));
 
         if (resultTS.getTimeStamps().size() != 1) {
             throw new Exception("unexpected result lenght: " + resultTS.getTimeStamps().size());
         }
 
         final TimeStamp timeStamp = resultTS.getTimeStamps().first();
         final Object o = resultTS.getTSProperty(TimeSeries.VALUE_KEYS);
         if ((o == null) || !(o instanceof String[])) {
             throw new Exception("VALUE_KEYS of ETA result not defined not defined or VALUE_KEYS not of type String[]");
         }
 
         final String[] valueKeys = (String[])o;
         for (final String key : valueKeys) {
             final Object value = resultTS.getValue(timeStamp, key);
             if ((value == null) || !(value instanceof Float)) {
                 throw new Exception("VALUE of ETA result not found or not in expected format: " + value);
             }
 
             if (key.equals("r720_1")) {
                 swmmOutput.setR720((Float)value);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   resultSOSEndoint   DOCUMENT ME!
      * @param   resultSOSOffering  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private SwmmOutput downloadSwmmOutput(final String resultSOSEndoint, final String resultSOSOffering)
             throws Exception {
         final SwmmOutput swmmResult = new SwmmOutput();
         final TimeInterval allInterval = new TimeInterval(
                 TimeInterval.Openness.OPEN,
                 TimeStamp.NEGATIVE_INFINITY,
                 TimeStamp.POSITIVE_INFINITY,
                 TimeInterval.Openness.OPEN);
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Downloading SWMM result offering '" + resultSOSOffering + "' from SOS '" + resultSOSEndoint
                         + "' for time interval '" + allInterval + "'");
         }
 
         final SudplanSOSHelper resultSOShelper = new SudplanSOSHelper(resultSOSEndoint);
         final TimeSeries resultTS = resultSOShelper.getTimeseries(resultSOSOffering, allInterval);
         LOG.info("SWMM result downloaded: \n" + resultTS.getTSProperty(" ts:description"));
 
         if (resultTS.getTimeStamps().size() != 1) {
             throw new Exception("unexpected result lenght: " + resultTS.getTimeStamps().size());
         }
 
         final TimeStamp timeStamp = resultTS.getTimeStamps().first();
         final Object oKeys = resultTS.getTSProperty(TimeSeries.VALUE_KEYS);
         if ((oKeys == null) || !(oKeys instanceof String[]) || (((String[])oKeys).length != 4)) {
             throw new Exception("VALUE_KEYS of SWMM result not defined or VALUE_KEYS not of type String[]: " + oKeys);
         }
 
         try {
             final String[] vTypes = (String[])resultTS.getTSProperty(TimeSeries.VALUE_JAVA_CLASS_NAMES);
             for (final String type : vTypes) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug(type);
                 }
             }
         } catch (Exception e) {
             LOG.warn("cannot determine types of value keys: " + e.getMessage(), e);
         }
 
         final String[] nodes = (String[])resultTS.getValue(timeStamp, "nodes");
         final Float[] frequencys = (Float[])resultTS.getValue(timeStamp, "frequencys");
         final Float[] volumes = (Float[])resultTS.getValue(timeStamp, "volumes");
 
         if ((nodes.length == 0) || (nodes.length != volumes.length) || (nodes.length != frequencys.length)) {
             throw new Exception("VALUEs of SWMM result invalid: " + nodes.length + " nodes, "
                         + volumes.length + " volumes, " + frequencys.length + " frequencys");
         }
 
         int i = 0;
         for (final String node : nodes) {
             final CsoOverflow csoOverflow = new CsoOverflow(node, volumes[i], frequencys[i], -1.0f);
             swmmResult.getCsoOverflows().put(node, csoOverflow);
             i++;
         }
 
         final float totalRunoffVolume = (Float)resultTS.getValue(timeStamp, "wwi");
         swmmResult.setTotalRunoffVolume(totalRunoffVolume);
 
         return swmmResult;
     }
 
     @Override
     public String toString() {
         return (runInfo != null) ? (runInfo.getModelName() + ": " + runInfo.getSpsTaskId())
                                  : "ERROR: no run info attached to SWMM Run";
     }
 
     @Override
     public ProgressListener getStatusCallback() {
         if (this.swmmModelManager != null) {
             return this.swmmModelManager;
         } else {
             return super.getStatusCallback();
         }
     }
 }
