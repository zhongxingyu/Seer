 package org.onebusaway.king_county_metro.mybus_siri;
 
 import its.SQL.ContentsData;
 import its.backbone.sdd.SddReceiver;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.Duration;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.Parser;
 import org.onebusaway.siri.core.SiriCoreModule;
 import org.onebusaway.siri.core.SiriServer;
 import org.onebusaway.siri.core.guice.LifecycleService;
 import org.onebusaway.siri.core.subscriptions.server.SiriServerSubscriptionManager;
 import org.onebusaway.siri.jetty.SiriJettyModule;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.org.siri.siri.BlockRefStructure;
 import uk.org.siri.siri.DataFrameRefStructure;
 import uk.org.siri.siri.FramedVehicleJourneyRefStructure;
 import uk.org.siri.siri.LocationStructure;
 import uk.org.siri.siri.ProgressBetweenStopsStructure;
 import uk.org.siri.siri.ServiceDelivery;
 import uk.org.siri.siri.VehicleActivityStructure;
 import uk.org.siri.siri.VehicleActivityStructure.MonitoredVehicleJourney;
 import uk.org.siri.siri.VehicleMonitoringDeliveryStructure;
 import uk.org.siri.siri.VehicleMonitoringRefStructure;
 import uk.org.siri.siri.VehicleRefStructure;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 
 public class MyBusSiriMain {
 
   private static final String ARG_CONSUMER_ADDRESS_DEFAULT = "consumerAddressDefault";
 
   private static Logger _log = LoggerFactory.getLogger(MyBusSiriMain.class);
 
   private static DatatypeFactory _dataTypeFactory;
 
   private static final String TIMEPOINT_PREDICTION_SERVER_NAME = "carpool.its.washington.edu";
 
   private static final int TIMEPOINT_PREDICTION_SERVER_PORT = 9002;
 
   private TimepointPredictionReceiver _receiver;
 
   private String _serverName = TIMEPOINT_PREDICTION_SERVER_NAME;
 
   private int _serverPort = TIMEPOINT_PREDICTION_SERVER_PORT;
 
   private SiriServer _siriServer;
 
   private String _consumerAddressDefault;
 
   private boolean _skip = false;
 
   private LifecycleService _lifecycleService;
 
   public static void main(String[] args) throws IOException,
       DatatypeConfigurationException, ParseException {
 
     _dataTypeFactory = DatatypeFactory.newInstance();
 
     MyBusSiriMain m = new MyBusSiriMain();
 
     Options options = new Options();
     options.addOption(ARG_CONSUMER_ADDRESS_DEFAULT, true, "");
 
     Parser parser = new GnuParser();
     CommandLine cli = parser.parse(options, args);
 
     args = cli.getArgs();
 
     String serverUrl = null;
 
     if (args.length > 0)
       serverUrl = args[0];
 
     if (cli.hasOption(ARG_CONSUMER_ADDRESS_DEFAULT)) {
       String consumerAddressDefault = cli.getOptionValue(ARG_CONSUMER_ADDRESS_DEFAULT);
       m.setConsumerAddressDefault(consumerAddressDefault);
     }
 
     m.run(serverUrl);
   }
 
   private void setConsumerAddressDefault(String consumerAddressDefault) {
     _consumerAddressDefault = consumerAddressDefault;
   }
 
   public void run(String serverUrl) throws IOException {
 
     List<Module> modules = new ArrayList<Module>();
     modules.addAll(SiriCoreModule.getModules());
     modules.add(new SiriJettyModule());
     Injector injector = Guice.createInjector(modules);
 
     _siriServer = injector.getInstance(SiriServer.class);
 
     if (serverUrl != null)
       _siriServer.setUrl(serverUrl);
 
     if (_consumerAddressDefault != null) {
       SiriServerSubscriptionManager manager = injector.getInstance(SiriServerSubscriptionManager.class);
       manager.setConsumerAddressDefault(_consumerAddressDefault);
     }
 
     _lifecycleService = injector.getInstance(LifecycleService.class);
     _lifecycleService.start();
 
     _receiver = new TimepointPredictionReceiver(_serverName, _serverPort);
     _receiver.start();
   }
 
   private void parsePredictions(Hashtable<?, ?> ht) {
 
     if (_skip)
       return;
 
     Map<String, List<TimepointPrediction>> predictionsByVehicleId = new HashMap<String, List<TimepointPrediction>>();
 
     if (ht.containsKey("PREDICTIONS")) {
 
       ContentsData data = (ContentsData) ht.get("PREDICTIONS");
       data.resetRowIndex();
 
       while (data.next()) {
 
         TimepointPrediction prediction = new TimepointPrediction();
 
         prediction.setAgencyId(data.getString(0));
         prediction.setBlockId(data.getString(1));
         prediction.setTripId(data.getString(2));
         prediction.setVehicleId(data.getString(6));
         prediction.setScheduleDeviation(data.getInt(14));
         prediction.setTimepointId(data.getString(3));
         prediction.setTimepointScheduledTime(data.getInt(4));
         prediction.setTimepointPredictedTime(data.getInt(13));
         prediction.setTimeOfPrediction(data.getInt(9));
 
         // Indicates that we don't have any real-time predictions for this
         // record
         if (prediction.getTimepointPredictedTime() == -1)
           continue;
 
         List<TimepointPrediction> predictions = predictionsByVehicleId.get(prediction.getVehicleId());
         if (predictions == null) {
           predictions = new ArrayList<TimepointPrediction>();
           predictionsByVehicleId.put(prediction.getVehicleId(), predictions);
         }
 
         predictions.add(prediction);
       }
     }
 
     ServiceDelivery delivery = new ServiceDelivery();
 
     List<VehicleMonitoringDeliveryStructure> vms = delivery.getVehicleMonitoringDelivery();
 
     VehicleMonitoringDeliveryStructure vm = new VehicleMonitoringDeliveryStructure();
     vms.add(vm);
 
     List<VehicleActivityStructure> activity = vm.getVehicleActivity();
 
     for (List<TimepointPrediction> predictions : predictionsByVehicleId.values()) {
 
       VehicleActivityStructure va = new VehicleActivityStructure();
       activity.add(va);
 
       va.setRecordedAtTime(new Date(System.currentTimeMillis()));
       va.setValidUntilTime(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
 
       VehicleMonitoringRefStructure vmRef = new VehicleMonitoringRefStructure();
       vmRef.setValue("all");
       va.setVehicleMonitoringRef(vmRef);
 
       TimepointPrediction prediction = getRepresentativePrediction(predictions);
 
       MonitoredVehicleJourney mvj = new MonitoredVehicleJourney();
       va.setMonitoredVehicleJourney(mvj);
 
       FramedVehicleJourneyRefStructure fvjRef = new FramedVehicleJourneyRefStructure();
       DataFrameRefStructure dataFrameRef = new DataFrameRefStructure();
       dataFrameRef.setValue("data-frame-ref");
       fvjRef.setDataFrameRef(dataFrameRef);
       fvjRef.setDatedVehicleJourneyRef(prediction.getTripId());
       mvj.setFramedVehicleJourneyRef(fvjRef);
 
      Duration delay = _dataTypeFactory.newDuration(prediction.getScheduleDeviation());
       mvj.setDelay(delay);
 
       BlockRefStructure blockRef = new BlockRefStructure();
       blockRef.setValue(prediction.getBlockId());
       mvj.setBlockRef(blockRef);
 
       VehicleRefStructure vehicleRef = new VehicleRefStructure();
       vehicleRef.setValue(prediction.getVehicleId());
       mvj.setVehicleRef(vehicleRef);
 
       LocationStructure location = new LocationStructure();
       location.setLatitude(BigDecimal.valueOf(47.5955716126442));
       location.setLongitude(BigDecimal.valueOf(-122.33160460882567));
       mvj.setVehicleLocation(location);
 
       ProgressBetweenStopsStructure progress = new ProgressBetweenStopsStructure();
       va.setProgressBetweenStops(progress);
 
       progress.setLinkDistance(BigDecimal.valueOf(0));
       progress.setPercentage(BigDecimal.valueOf(0));
     }
 
     _siriServer.publish(delivery);
     /*
      * int rc = _siriServer.publish(delivery); if (rc > 0) _skip = true;
      */
   }
 
   private TimepointPrediction getRepresentativePrediction(
       List<TimepointPrediction> predictions) {
 
     TimepointPrediction prev = null;
     int prevDelta = -1;
 
     for (TimepointPrediction record : predictions) {
 
       int delta = record.getTimepointPredictedTime()
           - record.getTimeOfPrediction();
 
       if (prev != null) {
         if (!prev.getTimepointId().equals(record.getTimepointId())
             && prevDelta >= 0 && delta > prevDelta)
           break;
       }
 
       prev = record;
       prevDelta = delta;
     }
 
     return prev;
   }
 
   private class TimepointPredictionReceiver extends SddReceiver {
 
     public TimepointPredictionReceiver(String serverName, int serverPort)
         throws IOException {
       super(serverName, serverPort);
     }
 
     @Override
     public void extractedDataReceived(
         @SuppressWarnings("rawtypes") Hashtable ht, String serialNum) {
       super.extractedDataReceived(ht, serialNum);
 
       try {
         parsePredictions(ht);
       } catch (Throwable ex) {
         _log.error("error parsing predictions from sdd data stream", ex);
       }
     }
 
   }
 
 }
