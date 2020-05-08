 /*
  * File Name:  AbstractAsciiAgent.java
  * Created on: Dec 17, 2010
  */
 package net.ooici.eoi.datasetagent;
 
 import net.ooici.eoi.netcdf.NcdsFactory;
 import net.ooici.eoi.datasetagent.obs.IObservationGroup;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TimeZone;
 
 import ucar.nc2.dataset.NetcdfDataset;
 
 /**
  * TODO Add class comments
  * 
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
 
     /* (non-Javadoc)
      * @see net.ooici.agent.abstraction.IDatasetAgent#acquireData(java.lang.String)
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
 
     /* (non-Javadoc)
      * @see net.ooici.agent.abstraction.AbstractDatasetAgent#processDataset(java.lang.Object)
      */
     @Override
     protected final String[] _processDataset(Object data) {
         if (!(data instanceof String)) {
             throw new IllegalArgumentException(new StringBuilder("Supplied data must an instance of ").append(String.class.getName()).append("; '").append(data.getClass().getName()).append("' type was received").toString());
         }
         IObservationGroup[] obs = null;
 
         obs = parseObs((String) data).toArray(new IObservationGroup[0]);
         return processDataset(obs);
     }
 
     /* TODO: Can we assume all requests from Ascii agents will be for URLs? */
     abstract protected List<IObservationGroup> parseObs(String asciiData);
 
 
     /* (non-Javadoc)
      * @see net.ooici.agent.abstraction.AbstractAsciiAgent#obs2Ncds(java.util.List)
      */
     protected NetcdfDataset obs2Ncds(IObservationGroup... obsList) {
         log.debug("Creating NC Dataset...");
         NetcdfDataset ncds = null;
 
        if (obsList.length == 0 || (obsList.length == 1 & obsList[0] == null)) {
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
 
         return ncds;
     }
 }
