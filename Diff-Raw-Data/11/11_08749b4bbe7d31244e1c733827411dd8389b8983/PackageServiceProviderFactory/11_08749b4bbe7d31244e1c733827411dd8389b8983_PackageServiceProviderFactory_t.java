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
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 // XML parser
 import org.w3c.dom.*;
 import javax.xml.parsers.*;
 /**
  *
  * @author Reeen
  */
 public class PackageServiceProviderFactory {
 
     public PackageServiceProviderFactory() {
         
     }
     
     public Posten getServiceProviderPosten() {
         return new Posten();
     }
     
     public Schenker getServiceProviderSchenker() {
         return new Schenker();
     }
     
     
     public abstract class PackageServiceProvider {
 
         public String name;
         public String baseURL;
         
         // Room for addiotional functionality, like customized parsing based
         // on which data a service provider offers
         
         public abstract boolean isServiceProvider(String id);
 
         @Override
         public String toString() {
             return name + " (" + baseURL + ")";
         }
     }
     
     public class Posten extends PackageServiceProvider {
 
         public Posten() {
             name = "Posten AB";
             baseURL = "http://server.logistik.posten.se/servlet/PacTrack?lang=SE&kolliid=";
         }
 
         @Override
         public boolean isServiceProvider(String id) {
 
             try {
                 URL url = new URL(baseURL + id);
                 HttpURLConnection con = (HttpURLConnection)url.openConnection();
 
                 DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                 domFactory.setNamespaceAware(true);
                 DocumentBuilder builder = domFactory.newDocumentBuilder();
                 Document doc = builder.parse(con.getInputStream());
 
                 // An 'internalstatus' of 0 is returned both for erronous IDs and 
                 // IDs that havent been registered in the system yet
                 if(doc.getElementsByTagName("internalstatus").getLength() > 0) {
                     String status = doc.getElementsByTagName("internalstatus").item(0).getTextContent();
                     if(Integer.parseInt(status) == 0)
                         return false;
                     else
                         return true;
                 }
                 // A 'programevent' is returned when the ID is less than 9 characters
                 else if(doc.getElementsByTagName("programevent").getLength() > 0)
                     return false;
                 else
                     return true;
 
             } catch (Exception e) {
                 System.out.println("Failed while parsing " + id + " for " + this);
                 e.printStackTrace();
             }
 
             return false;
         }
     }
 
     public class Schenker extends PackageServiceProvider {
 
         public Schenker() {
             name = "Schenker PrivPak";
             baseURL = "http://privpakportal.schenker.nu/TrackAndTrace/packagexml.aspx?packageid=";
         }
 
         @Override
         public boolean isServiceProvider(String id) {
 
             try {
                 URL url = new URL(baseURL + id);
                 HttpURLConnection con = (HttpURLConnection)url.openConnection();
 
                 DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                 domFactory.setNamespaceAware(true);
                 DocumentBuilder builder = domFactory.newDocumentBuilder();
                 Document doc = builder.parse(con.getInputStream());
 
                 // A 'programevent' is returned both for erronous IDs and 
                 // IDs that havent been registered in the system yet 
                if(doc.getElementsByTagName("programevent").getLength() > 0)
                    return false;
                else
                    return true;
 
             } catch (Exception e) {
                 System.out.println("Failed while parsing " + id + " for " + this);
                 e.printStackTrace();
             }
 
             return false;
         }
     }
 }
