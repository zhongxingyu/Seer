 package entropia.clubmonitor;
 
 import static entropia.clubmonitor.WebServer.disableCaching;
 import static entropia.clubmonitor.WebServer.replyWithBadRequest;
 import static entropia.clubmonitor.WebServer.replyWithInternalError;
 import static entropia.clubmonitor.WebServer.replyWithNotFound;
 import static entropia.clubmonitor.WebServer.setContentType;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.TimeUnit;
 
 import org.eclipse.jdt.annotation.Nullable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Charsets;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 
 import entropia.clubmonitor.TernaryStatusRegister.RegisterState;
 
 final class StatusServer implements HttpHandler {
     private static final Logger logger = LoggerFactory.getLogger(StatusServer.class);
     
     private static final ThreadLocal<Gson> gson = new ThreadLocal<Gson>() {
         @Override
         protected Gson initialValue() {
             return StatusServer.newGson();
         }
     };
     
     @Override
     public void handle(@Nullable HttpExchange exchange) throws IOException {
         if (exchange == null)
             return;
         try {
             // check request
             if (!"GET".equals(exchange.getRequestMethod())) {
                 replyWithBadRequest(exchange);
                 return;
             }
             final byte[] bytes;
             // backward compatibility, yay
             if ("/".equals(exchange.getRequestURI().getPath())) {
                 bytes = oldjson().getBytes(Charsets.UTF_8);
             } else if ("/json".equals(exchange.getRequestURI().getPath())) {
                 bytes = json().getBytes(Charsets.UTF_8);
             } else if ("/spaceapi".equals(exchange.getRequestURI().getPath())) {
                 bytes = spaceapi().getBytes(Charsets.UTF_8);
             } else {
                 replyWithNotFound(exchange);
                 return;
             }
             setContentType(exchange, "application/json; charset=UTF-8");
             disableCaching(exchange);
             exchange.sendResponseHeaders(200, bytes.length);
             try (final OutputStream responseStream =
                     exchange.getResponseBody()) {
                 responseStream.write(bytes);
             }
         } catch (Exception e) {
             logger.warn("exception while handling", e);
             replyWithInternalError(exchange);
         }
     }
     
     public static String oldjson() {
         final DateFormat timestampFormat = new SimpleDateFormat(
                 "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
         final Map<String, Object> map = new HashMap<>();
         map.put("raintropia", -1);
         map.put("generation", RandomUtils.generation());
         map.put("hardware_fehler",
                 TernaryStatusRegister.HW_FEHLER.status() == RegisterState.HIGH);
         map.put("club_offen",
                 TernaryStatusRegister.CLUB_OFFEN.status() == RegisterState.HIGH);
         final String timestamp = timestampFormat.format(new Date(
                 TimeUnit.SECONDS.toMillis(TernaryStatusRegister.lastEvent())));
         map.put("last_event", timestamp);
         map.put("fenster_offen",
                 TernaryStatusRegister.FENSTER_OFFEN.status() == RegisterState.HIGH);
         return gson.get().toJson(map) + "\n";
     }
 
     public static String spaceapi() {
         final Map<String, Object> map = new LinkedHashMap<>();
         map.put("api", "0.12");
         map.put("space", "Entropia");
         map.put("url", "https://entropia.de/");
         map.put("adress", "Entropia e.V., Gewerbehof, Steinstraße 23, 76133 Karlsruhe, Germany");
         map.put("lat", 49.0067);
      	map.put("lon", 8.407438);
         map.put("open",
                 TernaryStatusRegister.CLUB_OFFEN.status() == RegisterState.HIGH);
         final Map<String, Object> contact = new HashMap<>();
 	    	contact.put("phone", "+49 721 5604732");
 	    	contact.put("irc", "irc://irc.hackint.eu/#entropia");
 	    	contact.put("email", "info@entropia.de");
 	    	contact.put("ml", "news@entropia.de");
         map.put("contact", contact);
         map.put("logo", "https://entropia.de/wiki/images/c/ca/Tetrisknoten_Logo.png");
         final Map<String, Object> icon = new HashMap<>();
         	icon.put("open", "https://entropia.de/wiki/images/3/34/Entropia-wiki-logo-status-green.png");
         	icon.put("closed", "https://entropia.de/wiki/images/1/15/Entropia-wiki-logo-status-red.png");
         map.put("icon", icon);
         map.put("last_event", TernaryStatusRegister.lastEvent());
         return gson.get().toJson(map) + "\n";
     }
 
     
     private static Gson newGson() {
         return Null.assertNonNull(new GsonBuilder().disableHtmlEscaping()
         // .generateNonExecutableJson()
                 .serializeNulls().setPrettyPrinting().create());
     }
 
     public static String json() {
         final Map<String, Map<String, Object>> map = new TreeMap<>();
         for (final TernaryStatusRegister r : TernaryStatusRegister.values()) {
             if (r.isPublic()) { 
                 map.put(r.toString(), r.jsonStatusMap());
             }
         }
         for (final ADCRegister r : ADCRegister.values()) {
             map.put(r.toString(), r.jsonStatusMap());
         }
         return gson.get().toJson(map) + "\n";
     }
 }
