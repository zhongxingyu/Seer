 /*
  * File Name:  AbstractAsciiAgent.java
  * Created on: Dec 17, 2010
  */
 package net.ooici.eoi.datasetagent;
 
 import net.ooici.eoi.netcdf.NcdsFactory;
 import net.ooici.eoi.datasetagent.obs.IObservationGroup;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TimeZone;
 
 import ucar.nc2.dataset.NetcdfDataset;
 
 
 /**
  * The AbstractAsciiAgent provides the core functionallity used in typical implementations of dataset agents which act on services that
  * produce ascii data observations.<br />
  * <br />
  * This class defines the standard implementation for the method {@link #acquireData(String)} and {@link #_processDataset(Object)}. See
  * javadocs for more info. <br />
  * <br />
  * This class also defines the helper method {@link #obs2Ncds(IObservationGroup...)} for satisfying dataset update requests<br />
  * <br />
  * <b>Implementation Note:</b><br />
  * Concrete classes are required to implement the following methods:
  * <ul>
  * <li>{@link #buildRequest(net.ooici.services.sa.DataSource.EoiDataContextMessage)}</li>
  * <li>{@link #parseObs(String)}</li>
  * <li>{@link #processDataset(IObservationGroup...)}</li>
  * </ul>
  * and may optionally override the method {@link #validateData(String)}
  * 
  * @author cmueller
  * @author tlarocque
  * @version 1.0
  */
 public abstract class AbstractAsciiAgent extends AbstractDatasetAgent implements IAsciiAgent {
 
     /** Static Fields */
     static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractAsciiAgent.class);
     protected static final SimpleDateFormat outSdf;
 
     static {
         outSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         outSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
     }
 
     
     public static class AsciiValidationException extends RuntimeException {
 
         private static final long serialVersionUID = -637461134752222692L;
 
         public AsciiValidationException() {
             super();
         }
 
         public AsciiValidationException(String message, Throwable cause) {
             super(message, cause);
         }
 
         public AsciiValidationException(String message) {
             super(message);
         }
 
         public AsciiValidationException(Throwable cause) {
             super(cause);
         }
         
     }
 
     /**
      * Satisfies the given <code>request</code> by interpreting it as a URL and then, by procuring <code>String</code> data from that URL.
      * Typically, requests are built dynamically, and this method is a convenience for retrieving CSV, TSV, or XML data from REST webservices and
      * the like.
      * 
      * @param request
      *            a URL request as built from {@link IDatasetAgent#buildRequest(net.ooici.services.sa.DataSource.EoiDataContextMessage)}
      * @return the response of the given <code>request</code> as a <code>String</code>
      * 
      * @see IDatasetAgent#buildRequest(net.ooici.services.sa.DataSource.EoiDataContextMessage)
      * @see AgentUtils#getDataString(String)
      */
     @Override
     public Object acquireData(String request) {
         /* ASCII data requests are assumed to be basic HTTP post requests, or references to local files */
         log.debug("");
         log.info("Acquiring data for request [" + request + "]");
 
         String data = AgentUtils.getDataString(request);
         log.debug("... acquired raw data: [" + data.substring(0, Math.min(1000, data.length())) + "...]");
 
         return data;
     }
 
     /**
      * Processes the given <code>data</code> as from {@link #acquireData(String)}. The argument <code>data</code> is assumed to be in
      * instance of a <code>String</code>. If this is not the case an <code>IllegalArgumentException</code> will be thrown.<br />
      * <br />
      * Dataset processing occurs in the following steps:<br />
      * <ol>
      * <li>The given <code>data</code> is validated<br />
      * {@link #validateData(String)}</li>
      * <li>The <code>String data</code> is parsed into a list of <code>IObservationGroup</code> objects<br />
      * {@link #parseObs(String)}</li>
      * <li>The resultant observation list is processed<br />
      * {@link #processDataset(IObservationGroup...)}</li>
      * </ol>
      * 
      * @param data
      *            a CSV, TSV, XML result from {@link #acquireData(String)}
      * 
      * @return TODO:
      * 
      * @throws IllegalArgumentException
      *             When the given <code>data</code> is not an instance of <code>String</code>
      */
     @Override
     protected final String[] _processDataset(Object data) {
         if (!(data instanceof String)) {
             throw new IllegalArgumentException(new StringBuilder("Supplied data must an instance of ").append(String.class.getName()).append("; '").append(data.getClass().getName()).append("' type was received").toString());
         }
         IObservationGroup[] obs = null;
 
         validateData((String)data);
         obs = parseObs((String) data).toArray(new IObservationGroup[0]);
         return processDataset(obs);
     }
 
     /**
      * Subclasses of AbstractAsciiAgent may optionally override this method, throwing an AsciiValidationException where appropriate to
      * designate validation failure.
      * 
      * @param asciiData
      *            The ascii data to be validated before it is parsed for observational data.
      * 
      * @throws AsciiValidationException
      *             When the given <code>asciiData</code> is understood to be invalid
      */
     protected void validateData(String asciiData) {
         /* NO-OP */
     }
 
     /**
      * Parses the given <code>String</code> data as a list of <code>IObservationGroup</code> objects
      * 
      * @param asciiData
      *            <code>String</code> data passed to this method from {@link #acquireData(String)}
      * 
      * @return a list of <code>IObservationGroup</code> objects representing the observations parsed from the given <code>asciiData</code>
      */
     abstract protected List<IObservationGroup> parseObs(String asciiData);
 
 
     /**
      * Constructs a <code>NetcdfDataset</code> representation of the given list of <code>IObservationGroup</code> objects.<br />
      * <br />
      * <b>Dataset Feature Type decisions:</b><br />
      * The type of <code>NetcdfDataset</code> produced is dependent on the number of items in <code>obsList</code>, the
      * number of unique stations and unique depth values specified by those observations. The following table hightlights the underlying methods
      * used in creating the <code>NetcdfDataset</code> outputs based on the aforementioned criteria:<br />
      * <table border=1><tr>
      * <th>Build Method</th>
      * <th># of Obs Groups</th>
      * <th># of Station IDs</th>
      * <th># of Depth Values</th>
      * </tr><tr>
      * <td>{@link NcdsFactory#buildStation(IObservationGroup)}</td>
      * <td>1</td>
      * <td>1</td>
      * <td>1</td>
      * </tr><tr>
      * <td>{@link NcdsFactory#buildStationProfile(IObservationGroup)}</td>
      * <td>1</td>
      * <td>1</td>
      * <td>2+</td>
      * </tr><tr>
      * <td>{@link NcdsFactory#buildTrajectory(IObservationGroup[])}</td>
      * <td>2+</td>
      * <td>1</td>
      * <td>1</td>
      * </tr><tr>
      * <td>{@link NcdsFactory#buildTrajectoryProfile(IObservationGroup[])}</td>
      * <td>2+</td>
      * <td>1</td>
      * <td>2+</td>
      * </tr><tr>
      * <td>{@link NcdsFactory#buildStationMulti(IObservationGroup[])}</td>
      * <td>2+</td>
      * <td>2+</td>
      * <td>1</td>
      * </tr><tr>
      * <td>{@link NcdsFactory#buildStationProfileMulti(IObservationGroup[])}</td>
      * <td>2+</td>
      * <td>2+</td>
      * <td>2+</td>
      * </tr></table>
      * 
      * @param obsList
      * @return
      */
     protected NetcdfDataset obs2Ncds(IObservationGroup... obsList) {
         log.debug("Creating NC Dataset...");
         NetcdfDataset ncds = null;
 
         if (obsList.length == 0 || (obsList.length == 1  & obsList[0].isEmpty())) {
             return ncds;
         }
         IObservationGroup obs;
         /* Figure out what type of dataset to generate */
         if (obsList.length == 1) {
             obs = obsList[0];
             /* Only one station - just deal with depth */
             if (obs.getDepths().length > 1) {
                 ncds = NcdsFactory.buildStationProfile(obs);
             } else {
                 ncds = NcdsFactory.buildStation(obs);
             }
         } else {
             /* For SOS - if there is more than 1 observation group, and only 1 station ID, it's a trajectory */
             boolean isTraj = false;
             boolean isProfile = false;
             List<String> ids = new ArrayList<String>();
             List<Integer> dcs = new ArrayList<Integer>();
             obs = obsList[0];
             for (int i = 1; i < obsList.length; i++) {
                 obs = obsList[i];
                 /* Trajectory check */
                 if(!ids.contains(obs.getStnid())) {
                     ids.add(obs.getStnid());
                 }
                 /* Profile check */
                 if(!dcs.contains(obs.getDepths().length)) {
                     dcs.add(obs.getDepths().length);
                 }
             }
             isTraj = ids.size() == 1;
             isProfile = dcs.size() > 1;
 
             if (isTraj) {
                 if(isProfile) {
                     ncds = NcdsFactory.buildTrajectoryProfile(obsList);
                 } else {
                     ncds = NcdsFactory.buildTrajectory(obsList);
                 }
             } else {
                 /* If not a trajectory - it's a multistation */
                 if (isProfile) {
                     ncds = NcdsFactory.buildStationProfileMulti(obsList);
                 } else {
                     ncds = NcdsFactory.buildStationMulti(obsList);
                 }
             }
         }
 
         ncds.finish();
         
         return ncds;
     }
 }
