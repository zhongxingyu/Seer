 package com.signove.health.servicetest;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.signove.health.database.HealthDAO;
 import com.signove.health.structures.HealthData;
 
 public class Handlers {
     private TextView menssage;
     private TextView device;
     private TextView data;
     private ListView list;
     private Map<String, String> map;
     private Handler tm;
     private ParserXML parser;
     private Activity frame;
     private List<Double> datas = new ArrayList<Double>();
     private Context context;
     
     public Handlers(TextView device, TextView data,TextView menssage, Map<String, String> map, Activity frame, ListView list, Context context) {
         this.menssage = menssage;
         this.data = data;
         this.device = device;
         this.map = map;
         this.tm = new Handler();
         this.parser = new ParserXML();
         this.frame = frame;
         this.list = list;
         this.context = context;
     }
 
     public void handle_packet_connected(String path, String dev)
     {
         map.put(path, dev);
         show_dev(path);
         show(menssage, "Connected");
     }
 
     public void handle_packet_disconnected(String path)
     {
         show(menssage, "Disconnected");
         show_dev(path);
     }
 
     public void handle_packet_associated(String path, String xml)
     {
         show(menssage, "Associated");
         show_dev(path);
     }
 
     public void handle_packet_disassociated(String path)
     {
         show(menssage, "Disassociated");
         show_dev(path);
     }
 
     public void handle_packet_description(String path, String xml)
     {
         show(menssage, "MDS received");
         show_dev(path);
     }
     
     public void handle_packet_measurement(String path, String xml)
     {
         String measurement = "";
         Document document = parser.parse_xml(xml);
 
         if (document == null) {
             return;
         }
 
         NodeList datalists = document.getElementsByTagName("data-list");
 
         for (int i = 0; i < datalists.getLength(); ++i) {
 
             Log.w("Antidote", "processing datalist " + i);
 
             Node datalist_node = datalists.item(i);
             NodeList entries = ((Element) datalist_node).getElementsByTagName("entry");
 
             for (int j = 0; j < 6; ++j) {
 
                 Log.w("Antidote", "processing entry " + j);
 
                 boolean ok = false;
                 String unit = "";
                 String value = "";
 
                 Node entry = entries.item(j);
 
                 // scan immediate children to dodge entry inside another entry
                 NodeList entry_children = entry.getChildNodes();
 
                 for (int k = 0; k < entry_children.getLength(); ++k) {                  
                     Node entry_child = entry_children.item(k);
 
                     Log.w("Antidote", "processing entry child " + entry_child.getNodeName());
 
                     if (entry_child.getNodeName().equals("simple")) {
                         // simple -> value -> (text)
                         NodeList simple = ((Element) entry_child).getElementsByTagName("value");
                         Log.w("Antidote", "simple.value count: " + simple.getLength());
                         if (simple.getLength() > 0) {
                             String text = parser.get_xml_text(simple.item(0));
                             if (text != null) {
                                 ok = true;
                                 value = text;
                             }
                         }
                     } else if (entry_child.getNodeName().equals("meta-data")) {
                         // meta-data -> meta name=unit
                         NodeList metas = ((Element) entry_child).getElementsByTagName("meta");
 
                         Log.w("Antidote", "meta-data.meta count: " + metas.getLength());
 
                         for (int l = 0; l < metas.getLength(); ++l) {
                             Log.w("Antidote", "Processing meta " + l);
                             NamedNodeMap attr = metas.item(l).getAttributes();
                             if (attr == null) {
                                 Log.w("Antidote", "Meta has no attributes");
                                 continue;
                             }
                             Node item = attr.getNamedItem("name");
                             if (item == null) {
                                 Log.w("Antidote", "Meta has no 'name' attribute");
                                 continue;
                             }
 
                             Log.w("Antidote", "Meta attr 'name' is " + item.getNodeValue());
 
                             if (item.getNodeValue().equals("unit")) {
                                 Log.w("Antidote", "Processing meta unit");
                                 String text = parser.get_xml_text(metas.item(l));
                                 if (text != null) {
                                     unit = text;
                                 }
                             }
                         }
 
                     }
                 }
 
                 if (ok) {
                     if (unit != ""){
                         measurement += value + " " + unit + "\n";
                    }else{
                         measurement += value + " ";
                        Log.w("AntidoteDatabase", unit + "--" + value);
                        datas.add(Double.parseDouble(value));
                    }
                 }
             }
         }
        show(data, "SYS: "+ datas.get(0) + "\n" + "DIS: "+ datas.get(1));
         show(menssage, "Measurement");
         show_dev(path);
         persistInDatabase(datas, path);
         updateHistoryList();
     }
     
     private void persistInDatabase(List<Double> datas, String path) {
         if (map.containsKey(path) && frame != null) {
             HealthDAO healthDao = HealthDAO.getInstance(frame);
            Log.w("AntidoteDatabase", "Insert " + map.get(path) + " - " + datas.size() + " ("+ new Date() + ")");
             HealthData healthObject = new HealthData(map.get(path), datas.get(0), datas.get(1), datas.get(2), new Date());
             healthDao.save(healthObject);
         }else{
             Log.w("AntidoteDatabase", "Cannot save this health data.");
         }
         
     }
 
     public void show(TextView field, String menssage)
     {
         final TextView text = field;
         final String alert = menssage;
         tm.post(new Runnable() {
             @Override
             public void run() {
                 text.setText(alert);
             }
         });
     }
     
     public void updateHistoryList()
     {
         tm.post(new Runnable() {
             @Override
             public void run() {
             	HealthDataAdapter adapter = new HealthDataAdapter(context, HealthDAO.getInstance(context).ListAll());
                 list.setAdapter(adapter);
             }
         });
     }
     
     
     public void show_dev(String path)
     {
         if (map.containsKey(path)) {
             show(device, "Device " + map.get(path));
         } else {
             show(device, "Unknown device " + path);
         }
     }
     
     public Handler getTm() {
         return tm;
     }
 }
