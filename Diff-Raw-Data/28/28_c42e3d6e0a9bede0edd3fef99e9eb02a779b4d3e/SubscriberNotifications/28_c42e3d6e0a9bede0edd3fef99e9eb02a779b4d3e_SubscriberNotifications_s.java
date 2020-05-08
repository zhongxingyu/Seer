 package ai.ilikeplaces.logic.sits9;
 
 import ai.hbase.Cell;
 import ai.hbase.HBaseCrudService;
 import ai.hbase.Row;
 import ai.hbase.RowResponse;
 import ai.ilikeplaces.entities.GeohashSubscriber;
 import ai.ilikeplaces.logic.mail.SendMailLocal;
 import ai.ilikeplaces.logic.modules.Modules;
 import ai.ilikeplaces.rbs.RBGet;
 import ai.ilikeplaces.servlets.Unsubscribe;
 import ai.ilikeplaces.util.*;
 import ai.scribble.License;
 import au.com.bytecode.opencsv.CSVReader;
 import com.google.gson.Gson;
 import org.apache.avro.io.BinaryDecoder;
 import org.apache.avro.io.DatumReader;
 import org.apache.avro.io.DecoderFactory;
 import org.apache.avro.specific.SpecificDatumReader;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 import sun.misc.BASE64Decoder;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import javax.sql.DataSource;
 import javax.xml.transform.TransformerException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 /**
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: Apr 29, 2010
  * Time: 4:10:12 PM
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @Stateless
 public class SubscriberNotifications extends AbstractSLBCallbacks implements SubscriberNotificationsRemote {
 
     public static final int TGIF_TIMING = Integer.parseInt(RBGet.getGlobalConfigKey("TGIF_TIMING"));
 
     public static final int TGIF_ON_STARTUP = Integer.parseInt(RBGet.getGlobalConfigKey("TGIF_ON_STARTUP"));
 
     private static final String SUBSCRIBER_EMAIL = "ai/ilikeplaces/logic/Listeners/widgets/subscribe/SubscriberEventEmail.html";
 
     private static final String SUBSCRIBER_EMAIL_EVENT = "ai/ilikeplaces/logic/Listeners/widgets/subscribe/SubscriberEventEmailEvent.html";
 
     private static final String NAME = "name";
 
     private static final String LATITUDE = "latitude";
 
     private static final String LONGITUDE = "longitude";
 
     @Resource
     private TimerService timerService;
 
     @EJB
     private SendMailLocal sendMailLocal;
 
     @Resource(name = "ilpUn")
     private DataSource dataSource;
 
     @Override
     public void startTimer() {
         final int timeout = TGIF_TIMING == 0
                 ? 7 //Days of Week
                 * 24 //Hour of Day
                 * 60 //Minutes of Hour
                 * 60 //Seconds of Minute
                 * 1000 //Milliseconds of Second
                 : TGIF_TIMING;
 
         final SmartLogger sl = SmartLogger.start(Loggers.LEVEL.INFO, Loggers.CODE_MEMC +
                 "HELLO, STARTING " + this.getClass().getSimpleName() + " TIMER  WITH TIMER INTERVAL:" + timeout,
                 60000,
                 null,
                 true);
 
         final Calendar _calendar = Calendar.getInstance();
         final int todayDay = _calendar.get(Calendar.DAY_OF_WEEK);
         if (todayDay != Calendar.FRIDAY) {
             _calendar.add(Calendar.DAY_OF_YEAR, (Calendar.SATURDAY - todayDay + 6) % 7);
         }
 
         if (TGIF_TIMING == 0) {
             final Date _time = _calendar.getTime();
             timerService.createTimer(_time, timeout, null);
             String format = new SimpleDateFormat("yyyy/MM/dd").format(_time);
             sl.appendToLogMSG("Starting timer on " + format);
         } else {
             timerService.createTimer(0, timeout, null);
         }
 
         if (TGIF_ON_STARTUP == 1) {
             final Calendar now = Calendar.getInstance();
             now.add(Calendar.MINUTE, 1);
             timerService.createTimer(now.getTime(), timeout, null);
         }
 
         sl.complete(Loggers.LEVEL.INFO, Loggers.DONE);
     }
 
     @Timeout
     synchronized public void timeout(final Timer timer) throws IOException, SAXException, TransformerException, JSONException, SQLException {
 
         final CSVReader reader = new CSVReader(new FileReader("/opt/java/db/db-derby-10.5.3.0-bin/bin/Location.sql"));
         String[] nextLine;
         while ((nextLine = reader.readNext()) != null) {
             // nextLine[] is an array of values from the line
             System.out.println(Arrays.toString(nextLine));
             if (nextLine.length == 7) {
                final Connection _connection = dataSource.getConnection();
                final Statement _statement = _connection.createStatement();
                String sql = "INSERT INTO Location " +
                        "VALUES ("
                        + nextLine[0] + ","
                        + 1 + ",'"
                        + nextLine[2] + "','"
                        + nextLine[3] + "','"
                        + nextLine[4] + "','"
                        + nextLine[5] + "',"
                        + nextLine[6] + ")";
                _statement.executeUpdate(sql);
             }
         }
 
 
         final HBaseCrudService<GeohashSubscriber> _geohashSubscriberHBaseCrudService = new HBaseCrudService<GeohashSubscriber>();
         final HBaseCrudService<GeohashSubscriber>.Scanner _scanner = _geohashSubscriberHBaseCrudService.scan(new GeohashSubscriber(), 1).returnValueBadly();
 
         while (_scanner.getNewValue() != null) {
             final String _newValue = _scanner.getNewValue();
             Loggers.debug("Scanned value:" + _newValue);
             final RowResponse _rowResponse = new Gson().fromJson(_newValue, RowResponse.class);
             Loggers.debug("Scanned as GSON:" + _rowResponse.toString());
 
             for (final Row _row : _rowResponse.Row) {
 
                 final BASE64Decoder _base64DecoderRowKey = new BASE64Decoder();
                 final byte[] _bytes = _base64DecoderRowKey.decodeBuffer(_row.key);
                 final String rowKey = new String(_bytes);
                 Loggers.debug("Decoded row key:" + rowKey);
 
                 for (final Cell _cell : _row.Cell) {
                     final BASE64Decoder _base64DecoderValue = new BASE64Decoder();
                     final byte[] _valueBytes = _base64DecoderValue.decodeBuffer(_cell.$);
                     final String _cellAsString = new String(_valueBytes);
                     Loggers.debug("Cell as string:" + _cellAsString);
                     final GeohashSubscriber _geohashSubscriber = new GeohashSubscriber();
 
                     final DatumReader<GeohashSubscriber> _geohashSubscriberSpecificDatumReader = new SpecificDatumReader<GeohashSubscriber>(_geohashSubscriber.getSchema());
                     final BinaryDecoder _binaryDecoder = DecoderFactory.get().binaryDecoder(_valueBytes, null);
                     final GeohashSubscriber _read = _geohashSubscriberSpecificDatumReader.read(_geohashSubscriber, _binaryDecoder);
                     Loggers.debug("Decoded value avro:" + _read.toString());
 
 
                     final Date now = new Date();
                     final Calendar _week = Calendar.getInstance();
                     _week.setTimeInMillis(now.getTime() + (7 * 24 * 60 * 60 * 1000));
                     final SimpleDateFormat _simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                     _simpleDateFormat.format(_week.getTime());
 
                     final StringBuffer eventList = new StringBuffer("");
 
 //                    try {
 //                        final JSONObject jsonObject = Modules.getModules().getYahooUplcomingFactory()
 //                                .getInstance("http://upcoming.yahooapis.com/services/rest/")
 //                                .get("",
 //                                        new HashMap<String, String>() {
 //                                            {//Don't worry, this is a static initializer of this map :)
 //                                                put("method", "event.search");
 //                                                put("location", "" + _read.getLatitude() + "," + _read.getLongitude());
 //                                                put("radius", "" + 100);
 //                                                put("format", "json");
 //                                                put("max_date", _simpleDateFormat.format(_week.getTime()));
 //                                            }
 //                                        }
 //
 //                                );
 //                        final JSONArray events = jsonObject.getJSONObject("rsp").getJSONArray("event");
 //
 //                        final Document eventTemplateDocument = HTMLDocParser.getDocument(RBGet.getGlobalConfigKey("PAGEFILES") + SUBSCRIBER_EMAIL_EVENT);
 //                        final String eventTemplate = HTMLDocParser.convertNodeToHtml(HTMLDocParser.$("content", eventTemplateDocument));
 //
 //                        for (int i = 0; i < events.length(); i++) {
 //                            final JSONObject eventJSONObject = new JSONObject(events.get(i).toString());
 //                            Double.parseDouble(eventJSONObject.getString(LATITUDE));
 //                            Double.parseDouble(eventJSONObject.getString(LONGITUDE));
 //                            final String eventName = eventJSONObject.getString(NAME);
 //                            final String eventUrl = eventJSONObject.getString("url");
 //                            final String eventDate = eventJSONObject.getString("start_date");
 //                            final String eventVenue = eventJSONObject.getString("venue_name");
 //                            Loggers.debug("Event name:" + eventName);
 //                            eventList.append(eventTemplate
 //                                    .replace("_name_link_", !(("" + eventUrl).isEmpty()) ? eventUrl : ("https://www.google.com/search?q=" + eventName.replaceAll(" ", "+").replaceAll("-", "+")))
 //                                    .replace("_place_link_", "https://maps.googleapis.com/maps/api/staticmap?sensor=false&size=600x600&markers=color:blue%7Clabel:S%7C" + _read.getLatitude().toString() + "," + _read.getLongitude().toString())
 //                                    .replace("_name_", eventName)
 //                                    .replace("_place_", eventVenue)
 //                                    .replace("_date_", eventDate));
 //                        }
 //                    } catch (final Throwable t) {
 //                        Loggers.error("Error appending Yahoo Upcoming data to Geohash Subscriber", t);
 //                    }
 
                     try {
 
                         final SimpleDateFormat eventfulDate = new SimpleDateFormat("yyyyMMdd00");
                         _simpleDateFormat.format(_week.getTime());
 
                         final JSONObject jsonObject = Modules.getModules().getEventulFactory()
                                 .getInstance("http://api.eventful.com/json/events/search/")
                                 .get("",
                                         new HashMap<String, String>() {
                                             {//Don't worry, this is a static initializer of this map :)
                                                 put("location", "" + _read.getLatitude() + "," + _read.getLongitude());
                                                 put("within", "" + 100);
                                                 put("date", eventfulDate.format(Calendar.getInstance().getTime()) + "-" + eventfulDate.format(_week.getTime()));
                                             }
                                         }
 
                                 );
 
                         Loggers.debug("Eventful Reply:" + jsonObject.toString());
 
                         final JSONArray events = jsonObject.getJSONObject("events").getJSONArray("event");
 
                         final Document eventTemplateDocument = HTMLDocParser.getDocument(RBGet.getGlobalConfigKey("PAGEFILES") + SUBSCRIBER_EMAIL_EVENT);
                         final String eventTemplate = HTMLDocParser.convertNodeToHtml(HTMLDocParser.$("content", eventTemplateDocument));
 
                         for (int i = 0; i < events.length(); i++) {
                             final JSONObject eventJSONObject = new JSONObject(events.get(i).toString());
                             Double.parseDouble(eventJSONObject.getString(LATITUDE));
                             Double.parseDouble(eventJSONObject.getString(LONGITUDE));
                             final String eventName = eventJSONObject.getString("title");
                             final String eventUrl = eventJSONObject.getString("url");
                             final String eventDate = eventJSONObject.getString("start_time");
                             final String eventVenue = eventJSONObject.getString("venue_name");
                             Loggers.debug("Event name:" + eventName);
                             eventList.append(eventTemplate
                                     .replace("_name_link_", !(("" + eventUrl).isEmpty()) ? eventUrl : ("https://www.google.com/search?q=" + eventName.replaceAll(" ", "+").replaceAll("-", "+")))
                                     .replace("_place_link_", "https://maps.googleapis.com/maps/api/staticmap?sensor=false&size=600x600&markers=color:blue%7Clabel:S%7C" + _read.getLatitude().toString() + "," + _read.getLongitude().toString())
                                     .replace("_name_", eventName)
                                     .replace("_place_", eventVenue)
                                     .replace("_date_", eventDate));
                         }
                     } catch (final Throwable t) {
                         Loggers.error("Error appending Eventful data to Geohash Subscriber", t);
                     }
 
                     final Document email = HTMLDocParser.getDocument(RBGet.getGlobalConfigKey("PAGEFILES") + SUBSCRIBER_EMAIL);
                     final Document event = HTMLDocParser.getDocument(RBGet.getGlobalConfigKey("PAGEFILES") + SUBSCRIBER_EMAIL_EVENT);
                     final String _content = HTMLDocParser.convertNodeToHtml(HTMLDocParser.$("content", email));
                     final Parameter _unsubscribeLink = new Parameter("http://www.ilikeplaces.com/unsubscribe/").append(Unsubscribe.TYPE, Unsubscribe.Type.GeohashSubscribe.name(), true)
                             .append(Unsubscribe.VALUE, rowKey);
                     final String finalEmail = _content.replace(" ___||_", eventList.toString())
                             .replace("_unsubscribe_link_", _unsubscribeLink.get());
                     Loggers.debug("Final email:" + finalEmail);
                     sendMailLocal.sendAsHTML(_read.getEmailId().toString(), "Thank God It's Friday!", finalEmail);
                 }
 
 
             }
 
 
             _geohashSubscriberHBaseCrudService.scan(new GeohashSubscriber(), _scanner);
         }
 
         Loggers.debug("Completed scanner");
 
     }
 }
 
