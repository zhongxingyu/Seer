 package org.h2o.autonomic.numonic.interfaces;
 
 import uk.ac.standrews.cs.numonic.data.Data;
 import uk.ac.standrews.cs.numonic.data.SystemInfoData;
 import uk.ac.standrews.cs.numonic.summary.SingleSummary;
 
 /**
  * Used to collate incoming monitoring data on a local database instance, then sends it to the System Table when enough data has been collected.
  *
  * @author Angus Macdonald (angus AT cs.st-andrews.ac.uk)
  */
 public interface ILocalDataCollector {
 
     /**
      * Wait for ranking data to be received for both file system data and for CPU and memory data, then send it to the
      * System Table.
      * @param summary New monitoring data.
      */
    public void collateRankingData(final SingleSummary<? extends Data> summary);
 
     /**
      * When static machine information is received, store it.
      * @param staticSysInfoData Static system information for the local machine.
      */
    public void setStaticSystemInfo(final SystemInfoData staticSysInfoData);

    public void setFsMonitoringEnabled();
 
 }
