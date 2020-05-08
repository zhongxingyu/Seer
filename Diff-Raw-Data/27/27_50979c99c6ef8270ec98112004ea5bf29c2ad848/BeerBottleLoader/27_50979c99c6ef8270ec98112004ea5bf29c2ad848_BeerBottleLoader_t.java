 package eu.silvere;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.res.Resources;
 import android.content.res.XmlResourceParser;
import android.util.Log;
 
 public class BeerBottleLoader extends Thread {
 
 	private ArrayList<BeerBottle> bottles = null;
 	private Resources resources;
 	private boolean running = true;
 
 	public BeerBottleLoader(Resources resources) {
 		this.resources = resources;
 		bottles = new ArrayList<BeerBottle>();
 	}
 
 	public void setResources(Resources resources) {
 		this.resources = resources;
 	}
 
 	public ArrayList<BeerBottle> getBottles() {
 
 		if (bottles.isEmpty()) {
 			load();
 		}
 		return bottles;
 	}
 
 	@Override
 	public void run() {
 		load();
		Log.d("Beer", "Beer" + "Bottles loaded");
		running = false;
 	}
 
 	public void load() {
 
 		XmlResourceParser parser = resources.getXml(R.xml.beer_bottles);
 
 		try {
 			synchronized (bottles) { // TODO check for *lock
 				int eventType = parser.getEventType();
 				BeerBottle currentBottle = null;
 				boolean done = false;
 				while (eventType != XmlPullParser.END_DOCUMENT && !done) {
 					String name = null;
 					switch (eventType) {
 					case XmlPullParser.START_DOCUMENT:
 						break;
 					case XmlPullParser.START_TAG:
 						name = parser.getName();
 						if (name.equalsIgnoreCase("bottle")) {
 							currentBottle = new BeerBottle();
 						} else if (currentBottle != null) {
 							if (name.equalsIgnoreCase("name")) {
 								currentBottle.setName(parser.nextText());
 							} else if (name.equalsIgnoreCase("image")) {
 								currentBottle.setBitmapPath(parser.nextText());
 							} else if (name.equalsIgnoreCase("description")) {
 								currentBottle.setDescription(parser.nextText());
 							} else if (name.equalsIgnoreCase("frequency")) {
 								float frequency = parser.getAttributeFloatValue(null, "frequency",
 										0);
 								float volume = parser.getAttributeFloatValue(null, "volume", 0);
 								currentBottle.addFrequency(volume, frequency);
 							}
 						}
 						break;
 					case XmlPullParser.END_TAG:
 						name = parser.getName();
 						if (name.equalsIgnoreCase("bottle") && currentBottle != null) {
 							currentBottle.computeCoef();
 							bottles.add(currentBottle);
 						} else if (name.equalsIgnoreCase("beer_bottle_list")) {
 							done = true;
 						}
 						break;
 					}
 					eventType = parser.next();
 				}
 			}
 
 		} catch (XmlPullParserException e) {
 			// TODO Auto-generated catch block
 			// e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			// e.printStackTrace();
 		}
 	}
 
 	public void kill() {
 		running = false;
 	}
 
 }
