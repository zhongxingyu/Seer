 package entropia.clubmonitor;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import entropia.clubmonitor.TernaryStatusRegister.RegisterState;
 
 public final class FhemTimerTask extends TimerTask {
     private static final Logger logger = LoggerFactory.getLogger(FhemTimerTask.class);
 
     private static final String RADIATOR_CENTRAL_NAME = Config.getRadiatorCentralName();
     private static final URL FHEM_CMD_URL = Config.getFhemCmdURL();
     
     private static final AtomicBoolean doUpdate = new AtomicBoolean(true);
     
     private static String getCmdDesiredTemp(final String temp) {
        return String.format("set %s desired-temp %s", RADIATOR_CENTRAL_NAME, temp);
     }
     
     private static Map<String,String> createCmdMap(final String cmd) {
         final Map<String,String> map = new HashMap<String,String>();
         map.put("XHR", "1");
         map.put("cmd", cmd);
         return Collections.unmodifiableMap(map);
     }
     
     // TODO: fill in reset cmds - beware %s for NAME
     private static final List<String> RESET_CMDS = Arrays.asList();
     
     private static void resetRadiatorCentral() {
         for (final String cmd : RESET_CMDS) {
             try {
                 final Map<String, String> resetcmd = createCmdMap(String.format(
                         cmd, RADIATOR_CENTRAL_NAME));
                 WebClient.post(FHEM_CMD_URL, resetcmd);
             } catch (Exception e) {
                 logger.warn("error while resetting radiator central attribute: "
                         + cmd, e);
             }
         }
     }
     
     private static void syncTime() {
         try {
             // TODO: implement
         } catch (Exception e) {
             logger.warn("error on syncing time to radiator central", e);
         }
     }
     
     @Override
     public void run() {
 	try {
 	    logger.info("FhemTrigger timer expired");
 	    setDesiredTemp();
 	    final String cmd = "jsonlist " + RADIATOR_CENTRAL_NAME;
 	    final URL url = WebClient.getURL(FHEM_CMD_URL,
 	            "cmd", cmd, "XHR","1");
 	    final JsonObject o = WebClient.getJsonElement(url);
 	    updateMeasuredTemp(o);
 	    updateDesiredTemp(o);
 	} catch (Throwable e) {
 	    logger.warn("FhemTrigger timer", e);
 	}
     }
 
     private static final String FHEM_OPEN_DESIRED_TEMP = Config.getFhemOpenDesiredTemp();
     private static final String FHEM_CLOSED_DESIRED_TEMP = Config.getFhemClosedDesiredTemp();
 
     private static void setDesiredTemp() {
         try {
             if (!doUpdate.get()) {
                 return;
             }
             final String cmd;
             if (TernaryStatusRegister.CLUB_OFFEN.status() == RegisterState.HIGH) {
                 cmd = getCmdDesiredTemp(FHEM_OPEN_DESIRED_TEMP);
             } else if (TernaryStatusRegister.CLUB_OFFEN.status() == RegisterState.LOW) {
                 cmd = getCmdDesiredTemp(FHEM_CLOSED_DESIRED_TEMP);
             } else {
                 logger.info("CLUB_OFFEN not initialized");
                 return;
             }
             logger.info(cmd);
             final Map<String, String> map = createCmdMap(cmd);
             WebClient.post(FHEM_CMD_URL, map);
             doUpdate.set(false);
         } catch (Exception e) {
             logger.warn("setDesiredTemp", e);
         }
     }
 
     private static JsonElement walkJson(JsonObject o, String... path) {
         JsonElement _o = o;
         for (final String k : path) {
             _o = _o.getAsJsonObject().get(k);
         }
         return _o;
     }
     
     private static void updateMeasuredTemp(JsonObject o) {
 	try {
 	    final double temp = walkJson(o, "ResultSet", "Results", "READINGS",
 	            "measured-temp", "VAL").getAsDouble();
 	    ADCRegister.Temperature.set(temp);
 	} catch (Exception e) {
 	    logger.warn("updateMeasuredTemp", e);
 	}
     }
 
     private static void updateDesiredTemp(JsonObject o) {
 	try {
 	    final double temp = walkJson(o, "ResultSet", "Results",
 		    "READINGS", "desired-temp", "VAL").getAsDouble();
 	    ADCRegister.DesiredTemperature.set(temp);
 	} catch (Exception e) {
 	    logger.warn("updateDesiredTemp", e);
 	}
     }
     
     static class Trigger extends PublicOnlyTrigger {
 
         @Override
         public void trigger(TernaryStatusRegister register) throws IOException {
             if (TernaryStatusRegister.CLUB_OFFEN == register) {
                 doUpdate.set(true);
             }
         }
         
     }
     
     private static final long DELAY = 0;
     private static final long RATE = TimeUnit.MINUTES.toMillis(Config.getFhemSyncMinutes());
     
     public static Timer startFhemTrigger() {
 	final Timer timer = new Timer();
 	timer.scheduleAtFixedRate(new FhemTimerTask(), DELAY, RATE);
 	logger.info("FhemTriggerThread started");
 	resetRadiatorCentral();
 	return timer;
     }
 }
