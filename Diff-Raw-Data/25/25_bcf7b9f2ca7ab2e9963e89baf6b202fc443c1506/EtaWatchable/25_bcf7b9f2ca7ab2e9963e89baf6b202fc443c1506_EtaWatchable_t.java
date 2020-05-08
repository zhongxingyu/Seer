 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.sudplan.linz;
 
 import org.apache.log4j.Logger;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.io.IOException;
 import java.io.StringWriter;
 
 import de.cismet.cids.custom.sudplan.*;
 import de.cismet.cids.custom.sudplan.linz.model.EtaComputationModel;
 
 import de.cismet.cids.dynamics.CidsBean;
 
import de.cismet.commons.utils.ProgressEvent;
import de.cismet.commons.utils.ProgressListener;

 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public class EtaWatchable extends AbstractModelRunWatchable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(EtaWatchable.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient String title;
     private transient EtaRunInfo etaRunInfo;
     private transient EtaModelManager etaModelManager;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new EtaWatchable object.
      *
      * @param  etaModelManager  DOCUMENT ME!
      */
     public EtaWatchable(final EtaModelManager etaModelManager) {
         this(etaModelManager.getCidsBean());
         this.etaModelManager = etaModelManager;
     }
 
     /**
      * Creates a new SwmmWatchable object.
      *
      * @param  cidsBean  DOCUMENT ME!
      */
     public EtaWatchable(final CidsBean cidsBean) {
         super(cidsBean);
         this.title = cidsBean.toString();
         if (LOG.isDebugEnabled()) {
             LOG.debug("EtaWatchable for ETA Run '" + title + "' created");
         }
 
         try {
             final ObjectMapper mapper = new ObjectMapper();
             final String info = (String)this.getCidsBean().getProperty("runinfo");     // NOI18N
             etaRunInfo = mapper.readValue(info, EtaRunInfo.class);
             this.setStatus(State.RUNNING);
         } catch (final Exception exp) {
             final String message = "cannot read runInfo from ETA Run '" + title + "'"; // NOI18N
             LOG.error(message, exp);
             etaRunInfo = new EtaRunInfo();
             etaRunInfo.setSwmmRunId(-1);
             this.setStatus(State.RUNNING_WITH_ERROR);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public ProgressEvent requestStatus() throws IOException {
         if (etaRunInfo.getSwmmRunId() <= 0) {
             final String message = "ETA run '" + title + "' without SWMM run";
             LOG.error(message);
             throw new IOException(message);
         }
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("loading associated SWMM Run #" + etaRunInfo.getSwmmRunId() + " of ETA Run '" + title + "'");
         }
 
         final CidsBean swmmRunBean = SMSUtils.fetchCidsBean(etaRunInfo.getSwmmRunId(), SMSUtils.TABLENAME_MODELRUN);
         if (swmmRunBean == null) {
             final String message = "could not restore SWMM Run #" + this.etaRunInfo.getSwmmRunId() + " of ETA Run '"
                         + title + "'";
             LOG.error(message);
             // this.setStatus(State.RUNNING_WITH_ERROR);
             throw new IOException(message);
         }
 
         final SwmmModelManager swmmModelManager = (SwmmModelManager)SMSUtils.loadManagerFromRun(
                 swmmRunBean,
                 ManagerType.MODEL);
         swmmModelManager.setCidsBean(swmmRunBean);
         final SwmmRunInfo swmmRunInfo = swmmModelManager.getRunInfo();
 
         if (swmmRunInfo == null) {
             LOG.warn("RunInfo of SWMM Run #" + this.etaRunInfo.getSwmmRunId() + " of ETA Run '" + title + "' is null, "
                         + " probably the the SWMM Run hasnt't started yet!");
             return new ProgressEvent(this, ProgressEvent.State.PROGRESSING);
         }
 
         if (swmmRunInfo.isBroken() || swmmRunInfo.isCanceled()) {
             final String message = "SWMM Run #" + this.etaRunInfo.getSwmmRunId() + " of ETA Run '" + title + "' "
                         + "is brocken or canceled!";
             LOG.error(message);
             return new ProgressEvent(this, ProgressEvent.State.BROKEN, message);
         }
 
         if (!swmmRunInfo.isFinished()) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("SWMM Run #" + this.etaRunInfo.getSwmmRunId() + " of ETA Run '" + title
                             + "' is still running!");
             }
             return new ProgressEvent(this, ProgressEvent.State.PROGRESSING);
         } else {
             LOG.info("SWMM Run #" + this.etaRunInfo.getSwmmRunId() + " of ETA Run '" + title + "' is finished!");
             return new ProgressEvent(this, ProgressEvent.State.FINISHED);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public EtaOutput getEtaOutput() throws Exception {
         final CidsBean swmmRunBean = SMSUtils.fetchCidsBean(etaRunInfo.getSwmmRunId(), SMSUtils.TABLENAME_MODELRUN);
         if (swmmRunBean == null) {
             final String message = "could not restore SWMM Run #" + this.etaRunInfo.getSwmmRunId() + " of ETA Run '"
                         + title + "'";
             LOG.error(message);
             throw new Exception(message);
         }
 
         final Manager etaInputManager = SMSUtils.loadManagerFromRun(this.getCidsBean(), ManagerType.INPUT);
         assert swmmRunBean.getProperty("modelinput") != null : "Model Input of ETA Run '" + this.getCidsBean()
                     + "' is null!";
         etaInputManager.setCidsBean((CidsBean)this.getCidsBean().getProperty("modelinput"));
         final EtaInput etaInput = (EtaInput)etaInputManager.getUR();
 
         if ((etaInput.getTotalOverflowVolume() < 0) || (etaInput.getTotalRunoffVolume() < 0)
                     || (etaInput.getR720() < 0)) {
             LOG.warn("Overflow Volumes and r720,1 not set in ETA Input of ETA Run '" + title
                         + "', trying to obtain them form the associated "
                         + "SWMM Run #" + this.etaRunInfo.getSwmmRunId());
 
             final Manager swmmOutputManager = SMSUtils.loadManagerFromRun(swmmRunBean,
                     ManagerType.OUTPUT);
             assert swmmRunBean.getProperty("modeloutput") != null : "Model Output of SWMM Run '" + swmmRunBean
                         + "' is null!";
             swmmOutputManager.setCidsBean((CidsBean)swmmRunBean.getProperty("modeloutput"));
             final SwmmOutput swmmOutput = (SwmmOutput)swmmOutputManager.getUR();
 
             if (swmmOutput.getTotalRunoffVolume() < 0) {
                 final String message = "ETA values for SWMM Run #" + this.etaRunInfo.getSwmmRunId()
                             + " could not be computed: "
                             + "totalRunoffVolume (" + swmmOutput.getTotalRunoffVolume() + ") could not be determined";
                 LOG.error(message);
                 throw new Exception(message);
             }
 
             if (swmmOutput.getR720() < 0) {
                 final String message = "ETA values for SWMM Run #" + this.etaRunInfo.getSwmmRunId()
                             + " could not be computed: "
                             + "r720,1 (" + swmmOutput.getR720() + ") could not be determined";
                 LOG.error(message);
                 throw new Exception(message);
             }
 
             // update the input for the computation
             etaInput.fromSwmmOutput(swmmOutput);
 
             try {
                 final ObjectMapper mapper = new ObjectMapper();
                 final StringWriter writer = new StringWriter();
                 mapper.writeValue(writer, etaInput);
                 etaInputManager.getCidsBean().setProperty("ur", writer.toString());
                 etaInputManager.getCidsBean().persist();
             } catch (final Exception ex) {
                 final String message = "Cannot store updated eta input for ETA Run '" + title + "'"; // NOI18N
                 LOG.error(message, ex);
                 throw new Exception(message, ex);
             }
         }
 
         final EtaComputationModel etaComputationModel = new EtaComputationModel();
         final EtaOutput etaOutput = etaComputationModel.computateEta(etaInput);
 
         return etaOutput;
     }
 
     @Override
     public void startDownload() {
         LOG.warn("nothing to download!");
     }
 
     @Override
     public String getTitle() {
         return this.title;
     }
 
     @Override
     public String toString() {
         return title;
     }
 
     @Override
     public ProgressListener getStatusCallback() {
         if (this.etaModelManager != null) {
             return this.etaModelManager;
         } else {
             return super.getStatusCallback();
         }
     }
 }
