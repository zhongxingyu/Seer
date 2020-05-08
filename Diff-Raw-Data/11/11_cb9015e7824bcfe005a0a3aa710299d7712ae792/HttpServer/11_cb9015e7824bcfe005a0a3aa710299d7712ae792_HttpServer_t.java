 package server;
 
 import game.Ship;
 import game.World;
 
 import java.awt.geom.Point2D;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Properties;
 
 public class HttpServer extends NanoHTTPD {
 
 	public HttpServer(int port) throws IOException {
 		super(port, null);
 
 	}
 	
 	public Response serve( String uri, String method, Properties header, Properties parms, Properties files )
 	{
 		String mimeType = "text/plain";
 		String msg = "";
 		
 		if (uri.equalsIgnoreCase("/accelerate")) {
 			try {
 				double dx = Double.parseDouble(parms.getProperty("dx"));
 				double dy = Double.parseDouble(parms.getProperty("dy"));
 				double energy = Double.parseDouble(parms.getProperty("energy"));
 				int shipid = Integer.parseInt(parms.getProperty("id"));
 				World.w.WorldLock.lock();
 				game.World.w.accelerate_ship(new Point2D.Double(dx, dy), energy, shipid);
 				msg = "{\"id\" : " + shipid + " }";
 			}
 			catch (Exception e) {
 				// send error on out of energy exception
 				msg = "";
 			}
			finally {
				World.w.WorldLock.unlock();
			}
 		}
 		else if (uri.equalsIgnoreCase("/shoot")) {
 			try {
 				double dx = Double.parseDouble(parms.getProperty("dx"));
 				double dy = Double.parseDouble(parms.getProperty("dy"));
 				double energy = Double.parseDouble(parms.getProperty("energy"));
 				double mass = Double.parseDouble(parms.getProperty("mass"));
 				int shipid = Integer.parseInt(parms.getProperty("id"));
 				World.w.WorldLock.lock();
 				int p = game.World.w.shoot(new Point2D.Double(dx, dy), energy, mass, shipid);
 				msg = "{\"id\" : " + p + " }";
 			}
 			catch (Exception e) {
 				// send error on out of energy exception
 				msg = "";
 			}
			finally {
				World.w.WorldLock.unlock();
			}
 		}
 		else if (uri.equalsIgnoreCase("/login")) {
 			World.w.WorldLock.lock();
 			Ship s = World.w.new_ship();
 			World.w.WorldLock.unlock();
 			String name = parms.getProperty("playername");
 			if (null != name) {
 				s.playername(name);
 			}
 			if (s != null) {
 				msg = "{\"id\" : " + s.id + " }";
 			}
 		}
 		else if (uri.equalsIgnoreCase("/reset")) {
 			World.reset();
 			msg="World Reset";
 			System.out.println("World Reset");
 		}
 		else if (uri.equalsIgnoreCase("/testinterface")) {
 			mimeType = "text/html";
 			try {
 				msg = HttpServer.readFileAsString("action.html");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else if (uri.equalsIgnoreCase("/testinterface_")) {
 			mimeType = "text/html";
 			try {
 				msg = HttpServer.readFileAsString("actioninput.html");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			World.w.WorldLock.lock();
 			msg = game.World.w.json().WorldState();
 			World.w.WorldLock.unlock();
 		}
 		Response res = new NanoHTTPD.Response( HTTP_OK, MIME_HTML, msg );
 		res.addHeader("Access-Control-Allow-Origin", "*");
 		res.addHeader("Content-Type", mimeType);
 
 		return res;
 	}
 	
 	private static String readFileAsString(String filePath)
 		    throws java.io.IOException{
 		        StringBuffer fileData = new StringBuffer(1000);
 		        BufferedReader reader = new BufferedReader(
 		                new FileReader(filePath));
 		        char[] buf = new char[1024];
 		        int numRead=0;
 		        while((numRead=reader.read(buf)) != -1){
 		            String readData = String.valueOf(buf, 0, numRead);
 		            fileData.append(readData);
 		            buf = new char[1024];
 		        }
 		        reader.close();
 		        return fileData.toString();
 		    }
 
 }
