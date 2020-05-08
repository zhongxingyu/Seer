 /*   _______ __ __                    _______                    __
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  *
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav "Gussoh" Sohtell
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package silvertrout.plugins.packagetracker;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 // XML parser
 import org.w3c.dom.*;
 import javax.xml.parsers.*;
 import javax.xml.xpath.*;
 
 // URL and URL connection
 import java.net.URL;
 import java.net.HttpURLConnection;
 
 // Silvertrout internal
 import silvertrout.commons.EscapeUtils;
 import silvertrout.Channel;
 import silvertrout.User;
 
 /**
  * Tracks packages from the Swedish postal service (Posten).
  *
  * Keeps up to date with shipping information by using Posten's xml services
  * which is located at http://www.posten.se/c/online_steg_steg with additional
  * information on other pages linked from there.
  *
  * The plugin fetches updated information from their xml service and checks if
  * there are any new events. These are printed to the channel and stored for
  * users wanting to check the last status (e.g if missed the anouncement).
  *
  * Supplies commands: !listpackages, !addpackage ID, !statuspackage ID
  *                    and !removepackage ID.
  *s
  */
 public class PackageTracker extends silvertrout.Plugin {
 
     public class Package {
 
         public String  id;
         public String  customer        = "Unknown customer";
         public String  service         = "Unknown service";
 
         public String  recieverZipCode = "Unknown zip code";
         public String  recieverCity    = "Unknown city";
 
         public String  weight          = "?";
 
         public String  dateSent        = "Unknown send date";
         public String  dateDelivered   = "Unknown deliver date";
 
         public int     lastDate;
         public int     lastTime;
 
         public final ArrayList<PackageEvent> events = new ArrayList<PackageEvent>();
 
         public Channel channel;
 
         @Override
         public String toString() {
             return "Package (" + service + ", " + weight + " kg) "
                     + id + " from " + customer + " on route to "
                     + recieverZipCode + " " + recieverCity;
         }
     }
 
     public class PackageEvent {
 
         public String description;
         public String location;
 
         public int    date;
         public int    time;
 
         @Override
         public String toString()
         {
             return format(date, time) + ": " + description + ", " + location;
         }
     }
     private final ArrayList<Package> packages = new ArrayList<Package>();
 
     public PackageTracker() {
 
     }
 
     public boolean exists(String id)
     {
         for(Package p: packages)
         {
             if(p.id.equals(id))
             {
                 return true;
             }
         }
         return false;
     }
 
     public boolean add(String id, Channel channel) {
 
         if(exists(id))return false;
 
         Package p = new Package();
 
         p.id       = id;
         p.channel  = channel;
         p.lastDate = Integer.parseInt("19700101");
         p.lastTime = Integer.parseInt("0000");
 
         update(p);
 
         packages.add(p);
 
         return true;
     }
 
     public boolean remove(String id)
     {
         for(Package p: packages)
         {
             if(p.id.equals(id))
             {
                 packages.remove(p);
                 return true;
             }
         }
         return false;
     }
 
     public Package get(String id)
     {
         for(Package p: packages)
         {
             if(p.id.equals(id))
             {
                 return p;
             }
         }
         return null;
     }
 
     public String format(int date, int time)
     {
         int year   = date / 10000;
         int month  = (date % 10000) / 100;
         int day    = date % 100;
 
         int hour   = time / 100;
         int minute = time % 100;
 ;
         return String.format("%1$04d-%2$02d-%3$02d %4$02d:%5$02d", year, month, day, hour, minute);
     }
 
     public void update(Package p) {
 
         ArrayList<PackageEvent> events = fetch(p);
 
         if(events.size() > 0) {
 
             p.events.addAll(events);
             String chan = p.channel.getName();
 
             getNetwork().getConnection().sendPrivmsg(chan, p.toString());
 
             for(PackageEvent event: events) {
                 getNetwork().getConnection().sendPrivmsg(chan, " * " + event);
 
                 if(event.date > p.lastDate || (event.date == p.lastDate
                         && event.time > p.lastTime)) {
                     p.lastDate = event.date;
                     p.lastTime = event.time;
                 }
             }
         }
 
     }
 
     public ArrayList<PackageEvent> fetch(Package p) {
 
         ArrayList<PackageEvent> events = new ArrayList<PackageEvent>();
 
         // Connect and fetch package information:
         try {
 
             URL url = new URL("http://server.logistik.posten.se/servlet/PacTrack?lang=SE&kolliid=" + p.id);;
             HttpURLConnection con = (HttpURLConnection)url.openConnection();
 
             DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
             domFactory.setNamespaceAware(true);
             DocumentBuilder builder = domFactory.newDocumentBuilder();
             Document doc = builder.parse(con.getInputStream());
 
             // XXX, DEBUG!!
             //if(p.id.equals("1000"))
             //  doc = builder.parse(new java.io.FileInputStream("/home/tigge/Desktop/packet.xml"));
 
             // Update basic information:
             //if(doc.getElementsByTagName("parcel").getLength() > 0)
             //    p.id              = doc.getElementsByTagName("parcel").item(0).getAttributes().getNamedItem("id").getNodeValue();
             if(doc.getElementsByTagName("customername").getLength() > 0)
                 p.customer        = doc.getElementsByTagName("customername").item(0).getTextContent();
             if(doc.getElementsByTagName("servicename").getLength() > 0)
                 p.service         = doc.getElementsByTagName("servicename").item(0).getTextContent();
             if(doc.getElementsByTagName("receiverzipcode").getLength() > 0)
                 p.recieverZipCode = doc.getElementsByTagName("receiverzipcode").item(0).getTextContent();
             if(doc.getElementsByTagName("receivercity").getLength() > 0)
                 p.recieverCity    = doc.getElementsByTagName("receivercity").item(0).getTextContent();
             if(doc.getElementsByTagName("datesent").getLength() > 0)
                 p.dateSent        = doc.getElementsByTagName("datesent").item(0).getTextContent();
             if(doc.getElementsByTagName("datedelivered").getLength() > 0)
                 p.dateDelivered   = doc.getElementsByTagName("datedelivered").item(0).getTextContent();
             if(doc.getElementsByTagName("actualweight").getLength() > 0)
                 p.weight          = doc.getElementsByTagName("actualweight").item(0).getTextContent();
 
             NodeList eventList = doc.getElementsByTagName("event");
             System.out.println("Got " + eventList.getLength() + " events");
             for(int i = 0; i < eventList.getLength(); i++) {
                 PackageEvent pe = new PackageEvent();
                 NodeList eventListNodes = eventList.item(i).getChildNodes();
                 for(int j = 0; j < eventListNodes.getLength(); j++) {
                     Node n = eventListNodes.item(j);
                     if(n.getNodeName().equals("date")) {
                         pe.date = Integer.parseInt(n.getTextContent());
                     } else if(n.getNodeName().equals("time")) {
                         pe.time = Integer.parseInt(n.getTextContent());
                     } else if(n.getNodeName().equals("location")) {
                         pe.location = n.getTextContent();
                     } else if(n.getNodeName().equals("description")) {
                         pe.description = n.getTextContent();
                     }
                 }
                if(pe.date > p.lastDate || (pe.date == p.lastDate
                        && pe.time > p.lastTime)) {
                     events.add(pe);
                 }
             }
 
 
         } catch (Exception e) {
             System.out.println("Failed to update package " + p.id);
             e.printStackTrace();
             return new ArrayList<PackageEvent>();
         }
 
         return events;
     }
 
     @Override
     public void onPrivmsg(User from, Channel to, String message) {
 
         String[] parts = message.split("\\s");
         String command = parts[0].toLowerCase();
 
         // List packages:
         if (parts.length == 1 && command.equals("!listpackages")) {
             if(packages.size() > 0) {
                 for (int i = 0; i < packages.size(); i++) {
                     Package p = packages.get(i);
                     getNetwork().getConnection().sendPrivmsg(
                             to.getName(), " " + (i + 1) + ". " + p);
                 }
             } else {
                getNetwork().getConnection().sendPrivmsg(
                         to.getName(), " * There are no packages");
             }
         // Add package:
         } else if (parts.length == 2 && command.equals("!addpackage")) {
             if (add(parts[1], to)) {
                 Package p = packages.get(packages.size() - 1);
                 getNetwork().getConnection().sendPrivmsg(to.getName(),
                         "Added: " + p);
             } else {
                 getNetwork().getConnection().sendPrivmsg(to.getName(),
                         "Failed to add package");
             }
         // Remove package:
         } else if (command.equals("!removepackage")) {
             if (remove(parts[1])) {
                 getNetwork().getConnection().sendPrivmsg(to.getName(),
                         "Removed: Package (" + parts[1] + ")");
             } else {
                 getNetwork().getConnection().sendPrivmsg(to.getName(),
                         "Failed to remove package");
             }
         // Status on package:
         } else if (command.equals("!statuspackage")) {
             Package p = get(parts[1]);
             if(p != null) {
                 getNetwork().getConnection().sendPrivmsg(to.getName(),
                         p.toString());
                 if(p.events.size() > 0) {
                     getNetwork().getConnection().sendPrivmsg(to.getName(),
                         " * " + p.events.get(p.events.size() -1));
                 } else {
                     getNetwork().getConnection().sendPrivmsg(to.getName(),
                         " * No events for this package");
                 }
             } else {
                 getNetwork().getConnection().sendPrivmsg(to.getName(),
                         "Could not find package");
             }
 
         }
 
 
     }
 
     @Override
     public void onTick(int t) {
         //System.out.println("t = " + t + ", " + (t % (60 * 1)));
         if ((t % (60 * 5)) == 0) {
             //System.out.println("Trying to find updated pages...");
             for (Package p : packages) {
                 update(p);
             }
         }
     }
 }
