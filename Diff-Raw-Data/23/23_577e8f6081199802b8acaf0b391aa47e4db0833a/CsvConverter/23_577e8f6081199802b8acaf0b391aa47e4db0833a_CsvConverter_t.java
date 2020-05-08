 /**
  * Copyright (C) 2009 Mads Mohr Christensen, <hr.mohr@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package dk.cubing.liveresults.utilities;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 import au.com.bytecode.opencsv.bean.CsvToBean;
 import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class CsvConverter {
 
     private SimpleDateFormat birthdayFormat, birthdayFormatWca;
     private final Map<String, String> wcaMap = new HashMap<String, String>();
     private final Map<String, String> compToolMap = new HashMap<String, String>();
 
     public CsvConverter() {
         birthdayFormat = new SimpleDateFormat("yyyy-MM-dd");
         birthdayFormat.setLenient(false);
         birthdayFormatWca = new SimpleDateFormat("yyyy-M-dd");
         birthdayFormatWca.setLenient(false);
 
         wcaMap.put("Status","status");
         wcaMap.put("Name","name");
         wcaMap.put("Country","country");
         wcaMap.put("WCA ID","wcaId");
         wcaMap.put("Birth Date","birthday");
         wcaMap.put("Gender","gender");
         wcaMap.put("Email","email");
         wcaMap.put("Guests","guests");
         wcaMap.put("IP","ip");
 
         wcaMap.put("333","event333");
         wcaMap.put("444","event444");
         wcaMap.put("555","event555");
         wcaMap.put("222","event222");
         wcaMap.put("333bf","event333bf");
         wcaMap.put("333oh","event333oh");
         wcaMap.put("333fm","event333fm");
         wcaMap.put("333ft","event333ft");
         wcaMap.put("minx","eventminx");
         wcaMap.put("pyram","eventpyram");
         wcaMap.put("sq1","eventsq1");
         wcaMap.put("clock","eventclock");
         wcaMap.put("666","event666");
         wcaMap.put("777","event555");
         wcaMap.put("magic","eventmagic");
         wcaMap.put("mmagic","eventmmagic");
         wcaMap.put("444bf","event444bf");
         wcaMap.put("555bf","event555bf");
         wcaMap.put("333mbf","event333mbf");
 
         wcaMap.put("333ni","event333ni");
         wcaMap.put("333sbf","event333sbf");
         wcaMap.put("333r3","event333r3");
         wcaMap.put("333ts","event333ts");
         wcaMap.put("333bts","event333bts");
         wcaMap.put("222bf","event222bf");
         wcaMap.put("333si","event333si");
         wcaMap.put("rainb","eventrainb");
         wcaMap.put("snake","eventsnake");
         wcaMap.put("skewb","eventskewb");
         wcaMap.put("mirbl","eventmirbl");
         wcaMap.put("222oh","event222oh");
         wcaMap.put("magico","eventmagico");
         wcaMap.put("360","event360");
 
         compToolMap.put("Name","name");
         compToolMap.put("country","country");
         compToolMap.put("WCA ID","wcaId");
         compToolMap.put("birthday","birthday");
         compToolMap.put("sex","gender");
 
         compToolMap.put("3","event333");
        compToolMap.put("4","event444");
         compToolMap.put("5","event555");
         compToolMap.put("2","event222");
         compToolMap.put("bf","event333bf");
         compToolMap.put("oh","event333oh");
         compToolMap.put("fm","event333fm");
         compToolMap.put("ft","event333ft");
         compToolMap.put("mx","eventminx");
         compToolMap.put("py","eventpyram");
         compToolMap.put("s1","eventsq1");
         compToolMap.put("cl","eventclock");
         compToolMap.put("6","event666");
         compToolMap.put("7","event555");
         compToolMap.put("m","eventmagic");
         compToolMap.put("mm","eventmmagic");
         compToolMap.put("4bf","event444bf");
         compToolMap.put("5bf","event555bf");
         compToolMap.put("mbf","event333mbf");
 
         compToolMap.put("ni","event333ni");
         compToolMap.put("sbf","event333sbf");
         compToolMap.put("r3","event333r3");
         compToolMap.put("ts","event333ts");
         compToolMap.put("bts","event333bts");
         compToolMap.put("2b","event222bf");
         compToolMap.put("si","event333si");
         compToolMap.put("rainb","eventrainb");
         compToolMap.put("snake","eventsnake");
         compToolMap.put("skewb","eventskewb");
         compToolMap.put("mirbl","eventmirbl");
         compToolMap.put("2o","event222oh");
         compToolMap.put("magico","eventmagico");
         compToolMap.put("360","event360");
     }
 
     public void wca2CompTool(Reader reader, Writer writer) throws IOException, ParseException {
         HeaderColumnNameTranslateMappingStrategy<WcaBean> strat;
         strat = new HeaderColumnNameTranslateMappingStrategy<WcaBean>();
         strat.setType(WcaBean.class);
         strat.setColumnMapping(wcaMap);
         CsvToBean<WcaBean> csv = new CsvToBean<WcaBean>();
         List<WcaBean> list = csv.parse(strat, new CSVReader(reader, ','));
 
         CSVWriter csvWriter = new CSVWriter(writer, ',', CSVWriter.NO_QUOTE_CHARACTER);
         List<String> header = new ArrayList<String>();
         header.add("Name"); header.add("country"); header.add("WCA ID");
         header.add("birthday"); header.add("sex"); header.add("");
         header.addAll(getSupportedEvents(list.get(0), reverse(compToolMap)));
         csvWriter.writeNext(header.toArray(new String[header.size()]));
         for (WcaBean b : list) {
             Date birthday = birthdayFormatWca.parse(b.getBirthday());
             List<String> entry = new ArrayList<String>();
             entry.add(b.getName()); entry.add(b.getCountry()); entry.add(b.getWcaId());
             entry.add(birthdayFormat.format(birthday)); entry.add(b.getGender()); entry.add("");
             entry.addAll(getRegisteredEvents(b));
             csvWriter.writeNext(entry.toArray(new String[entry.size()]));
         }
         csvWriter.close();
     }
 
     public void compTool2Wca(Reader reader, Writer writer) throws IOException, ParseException {
         HeaderColumnNameTranslateMappingStrategy<WcaBean> strat;
         strat = new HeaderColumnNameTranslateMappingStrategy<WcaBean>();
         strat.setType(WcaBean.class);
         strat.setColumnMapping(compToolMap);
         CsvToBean<WcaBean> csv = new CsvToBean<WcaBean>();
         List<WcaBean> list = csv.parse(strat, new CSVReader(reader, ','));
 
         CSVWriter csvWriter = new CSVWriter(writer, ',', CSVWriter.NO_QUOTE_CHARACTER);
         List<String> header = new ArrayList<String>();
         header.add("Status"); header.add("Name"); header.add("Country"); header.add("WCA ID");
         header.add("Birth Date"); header.add("Gender"); header.add("");
         header.addAll(getSupportedEvents(list.get(0), reverse(wcaMap)));
         header.add("Email"); header.add("Guests"); header.add("IP");
         csvWriter.writeNext(header.toArray(new String[header.size()]));
         for (WcaBean b : list) {
             Date birthday = birthdayFormat.parse(b.getBirthday());
             List<String> entry = new ArrayList<String>();
             entry.add(b.getStatus()); entry.add(b.getName()); entry.add(b.getCountry()); entry.add(b.getWcaId());
             entry.add(birthdayFormatWca.format(birthday)); entry.add(b.getGender()); entry.add("");
             entry.addAll(getRegisteredEvents(b));
             entry.add(b.getEmail()); entry.add(b.getGuests()); entry.add(b.getIp());
             csvWriter.writeNext(entry.toArray(new String[entry.size()]));
         }
         csvWriter.close();
     }
 
     private List<String> getRegisteredEvents(WcaBean wcaBean) {
         List<String> events = new ArrayList<String>();
         if (wcaBean.getEvent333() != null)      events.add(wcaBean.getEvent333());
         if (wcaBean.getEvent444() != null)      events.add(wcaBean.getEvent444());
         if (wcaBean.getEvent555() != null)      events.add(wcaBean.getEvent555());
         if (wcaBean.getEvent222() != null)      events.add(wcaBean.getEvent222());
         if (wcaBean.getEvent333bf() != null)    events.add(wcaBean.getEvent333bf());
         if (wcaBean.getEvent333oh() != null)    events.add(wcaBean.getEvent333oh());
         if (wcaBean.getEvent333fm() != null)    events.add(wcaBean.getEvent333fm());
         if (wcaBean.getEvent333ft() != null)    events.add(wcaBean.getEvent333ft());
         if (wcaBean.getEventminx() != null)     events.add(wcaBean.getEventminx());
         if (wcaBean.getEventpyram() != null)    events.add(wcaBean.getEventpyram());
         if (wcaBean.getEventsq1() != null)      events.add(wcaBean.getEventsq1());
         if (wcaBean.getEventclock() != null)    events.add(wcaBean.getEventclock());
         if (wcaBean.getEvent666() != null)      events.add(wcaBean.getEvent666());
         if (wcaBean.getEvent777() != null)      events.add(wcaBean.getEvent777());
         if (wcaBean.getEventmagic() != null)    events.add(wcaBean.getEventmagic());
         if (wcaBean.getEventmmagic() != null)   events.add(wcaBean.getEventmmagic());
         if (wcaBean.getEvent444bf() != null)    events.add(wcaBean.getEvent444bf());
         if (wcaBean.getEvent555bf() != null)    events.add(wcaBean.getEvent555bf());
         if (wcaBean.getEvent333mbf() != null)   events.add(wcaBean.getEvent333mbf());
 
         if (wcaBean.getEvent333ni() != null)    events.add(wcaBean.getEvent333ni());
         if (wcaBean.getEvent333sbf() != null)   events.add(wcaBean.getEvent333sbf());
         if (wcaBean.getEvent333r3() != null)    events.add(wcaBean.getEvent333sbf());
         if (wcaBean.getEvent333ts() != null)    events.add(wcaBean.getEvent333ts());
         if (wcaBean.getEvent333bts() != null)   events.add(wcaBean.getEvent333bts());
         if (wcaBean.getEvent222bf() != null)    events.add(wcaBean.getEvent222bf());
         if (wcaBean.getEvent333si() != null)    events.add(wcaBean.getEvent333si());
         if (wcaBean.getEventrainb() != null)    events.add(wcaBean.getEventrainb());
         if (wcaBean.getEventsnake() != null)    events.add(wcaBean.getEventsnake());
         if (wcaBean.getEventskewb() != null)    events.add(wcaBean.getEventskewb());
         if (wcaBean.getEventmirbl() != null)    events.add(wcaBean.getEventmirbl());
         if (wcaBean.getEvent222oh() != null)    events.add(wcaBean.getEvent222oh());
         if (wcaBean.getEventmagico() != null)   events.add(wcaBean.getEventmagico());
         if (wcaBean.getEvent360() != null)      events.add(wcaBean.getEvent360());
         return events;
     }
 
     private List<String> getSupportedEvents(WcaBean wcaBean, Map<String, String> map) {
         List<String> events = new ArrayList<String>();
         if (wcaBean.getEvent333() != null)      events.add(map.get("event333"));
         if (wcaBean.getEvent444() != null)      events.add(map.get("event444"));
         if (wcaBean.getEvent555() != null)      events.add(map.get("event555"));
         if (wcaBean.getEvent222() != null)      events.add(map.get("event222"));
         if (wcaBean.getEvent333bf() != null)    events.add(map.get("event333bf"));
         if (wcaBean.getEvent333oh() != null)    events.add(map.get("event333oh"));
         if (wcaBean.getEvent333fm() != null)    events.add(map.get("event333fm"));
         if (wcaBean.getEvent333ft() != null)    events.add(map.get("event333ft"));
         if (wcaBean.getEventminx() != null)     events.add(map.get("eventminx"));
         if (wcaBean.getEventpyram() != null)    events.add(map.get("eventpyram"));
         if (wcaBean.getEventsq1() != null)      events.add(map.get("eventsq1"));
         if (wcaBean.getEventclock() != null)    events.add(map.get("eventclock"));
         if (wcaBean.getEvent666() != null)      events.add(map.get("event666"));
         if (wcaBean.getEvent777() != null)      events.add(map.get("event777"));
         if (wcaBean.getEventmagic() != null)    events.add(map.get("eventmagic"));
         if (wcaBean.getEventmmagic() != null)   events.add(map.get("eventmmagic"));
         if (wcaBean.getEvent444bf() != null)    events.add(map.get("event444bf"));
         if (wcaBean.getEvent555bf() != null)    events.add(map.get("event555bf"));
         if (wcaBean.getEvent333mbf() != null)   events.add(map.get("event333mbf"));
 
         if (wcaBean.getEvent333ni() != null)    events.add(map.get("event333ni"));
         if (wcaBean.getEvent333sbf() != null)   events.add(map.get("event333sbf"));
         if (wcaBean.getEvent333r3() != null)    events.add(map.get("event333sbf"));
         if (wcaBean.getEvent333ts() != null)    events.add(map.get("event333ts"));
         if (wcaBean.getEvent333bts() != null)   events.add(map.get("event333bts"));
         if (wcaBean.getEvent222bf() != null)    events.add(map.get("event222bf"));
         if (wcaBean.getEvent333si() != null)    events.add(map.get("event333si"));
         if (wcaBean.getEventrainb() != null)    events.add(map.get("eventrainb"));
         if (wcaBean.getEventsnake() != null)    events.add(map.get("eventsnake"));
         if (wcaBean.getEventskewb() != null)    events.add(map.get("eventskewb"));
         if (wcaBean.getEventmirbl() != null)    events.add(map.get("eventmirbl"));
         if (wcaBean.getEvent222oh() != null)    events.add(map.get("event222oh"));
         if (wcaBean.getEventmagico() != null)   events.add(map.get("eventmagico"));
         if (wcaBean.getEvent360() != null)      events.add(map.get("event360"));
         return events;
     }
 
     private static <K,V> HashMap<V,K> reverse(Map<K,V> map) {
         HashMap<V,K> rev = new HashMap<V, K>();
         for(Map.Entry<K,V> entry : map.entrySet()) {
             rev.put(entry.getValue(), entry.getKey());
         }
         return rev;
     }
 }
