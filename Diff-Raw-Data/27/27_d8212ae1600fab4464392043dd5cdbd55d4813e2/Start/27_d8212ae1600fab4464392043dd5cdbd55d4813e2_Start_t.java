/*
 *  Copyright 2011(c) The Ontario Institute for Cancer Research. All rights reserved.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the GNU Public License v3.0.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */


 package ca.on.oicr.ferv;
 
 
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.bio.SocketConnector;
 import org.mortbay.jetty.webapp.WebAppContext;
 import java.security.ProtectionDomain;
 import java.net.URL;
 
 public class Start {
 
   public static void main(String[] args) throws Exception {
     Server server = new Server();
     SocketConnector connector = new SocketConnector();
 
     // Set some timeout options to make debugging easier.
     connector.setMaxIdleTime(1000 * 60 * 60);
     connector.setSoLingerTime(-1);
     connector.setPort(8080);
     server.setConnectors(new Connector[] { connector });
 
     WebAppContext context = new WebAppContext();
     context.setServer(server);
     context.setContextPath("/capsid");
 
     ProtectionDomain protectionDomain = Start.class.getProtectionDomain();
     URL location = protectionDomain.getCodeSource().getLocation();
     context.setWar(location.toExternalForm());
 
     server.addHandler(context);
     try {
       server.start();
       System.in.read();
       server.stop();
       server.join();
     } catch (Exception e) {
       e.printStackTrace();
       System.exit(100);
     }
   }
 }
