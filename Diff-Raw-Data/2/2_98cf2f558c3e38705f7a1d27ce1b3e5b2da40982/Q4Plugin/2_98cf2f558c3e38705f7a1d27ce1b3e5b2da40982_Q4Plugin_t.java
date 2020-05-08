 package org.kvj.sierra5.plugins.impl.quebec;
 
 import java.io.File;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.kvj.bravo7.ipc.RemoteServiceConnector;
 import org.kvj.quebec4.data.PointBean;
 import org.kvj.quebec4.data.Q4Constants;
 import org.kvj.quebec4.data.Quebec4Service;
 import org.kvj.quebec4.data.TaskBean;
 import org.kvj.sierra5.common.data.Node;
 import org.kvj.sierra5.common.plugin.MenuItemInfo;
 import org.kvj.sierra5.common.plugin.PluginInfo;
 import org.kvj.sierra5.common.root.Root;
 import org.kvj.sierra5.plugins.App;
 import org.kvj.sierra5.plugins.R;
 import org.kvj.sierra5.plugins.WidgetController;
 import org.kvj.sierra5.plugins.impl.DefaultPlugin;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class Q4Plugin extends DefaultPlugin {
 
 	private static final String TAG = "Q4Plugin";
 	WidgetController controller = null;
 	private RemoteServiceConnector<Quebec4Service> remote = null;
 
 	public Q4Plugin(WidgetController controller) {
 		this.controller = controller;
 		remote = new RemoteServiceConnector<Quebec4Service>(App.getInstance(),
 				Q4Constants.SERVICE_URI, null) {
 
 			@Override
 			public Quebec4Service castAIDL(IBinder binder) {
 				return Quebec4Service.Stub.asInterface(binder);
 			}
 		};
 	}
 
 	@Override
 	public int[] getCapabilities() throws RemoteException {
 		return new int[] { PluginInfo.PLUGIN_HAVE_MENU };
 	}
 
 	@Override
 	public MenuItemInfo[] getMenu(int id, Node node) throws RemoteException {
 		// Log.i(TAG, "Get menu: " + id + ", " + node.type);
 		if (node.type == Node.TYPE_FOLDER) { // Not for folders
 			return null;
 		}
 		Quebec4Service service = remote.getRemote();
 		if (null != service) { // Connected to quebec4
 			if (-1 == id) { // RootMenu
 				return new MenuItemInfo[] { new MenuItemInfo(0,
 						MenuItemInfo.MENU_ITEM_SUBMENU, "Import from Quebec4") };
 			} else { // Request tasks from Q4
 				List<TaskBean> tasks = service.getTasks();
 				// Log.i(TAG, "Tasks found: " + tasks.size());
 				MenuItemInfo[] menu = new MenuItemInfo[tasks.size()];
 				for (int i = 0; i < menu.length; i++) { // Create menu items
 					TaskBean task = tasks.get(i);
 					// Log.i(TAG, "Task: " + task.title + ", " + task.type +
 					// ", "
 					// + task.point);
 					menu[i] = new MenuItemInfo(task.id,
 							MenuItemInfo.MENU_ITEM_ACTION, task.title);
 				}
 				return menu;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public boolean executeAction(int id, Node node) throws RemoteException {
 		// Log.i(TAG, "Exec action: " + id);
 		try { // Remote errors
 			Quebec4Service service = remote.getRemote();
 			Root root = controller.getRootService();
 			if (null == root) { // Root service not ready
 				Log.w(TAG, "Root service not ready");
 				return false;
 			}
 			if (null == service) { // Not ready
 				Log.w(TAG, "Q4 service not ready");
 				return false;
 			}
 			TaskBean task = service.getTask(id);
 			if (null == task) { // Task not found
 				Log.w(TAG, "Task not found");
 				return false;
 			}
 			String mediaChild = null;
 			String geoChild = null;
 			File folder = new File(node.file).getParentFile();
 			if (!TextUtils.isEmpty(task.media)) { // Have media
 				File mediaFile = new File(task.media);
 				String link = "_files" + File.separator + mediaFile.getName();
 				File toFile = new File(folder, link);
 				if (!root.putFile(toFile.getAbsolutePath(),
 						mediaFile.getAbsolutePath(), null)) { // Copy failed
 					Log.w(TAG, "Copy failed: " + link + ", " + mediaFile + ", "
 							+ toFile);
 					return false;
 				}
 				mediaChild = "[[" + link + "]]";
 			}
 			if (null != task.point) { // Have point
 				StringBuilder sb = new StringBuilder(
 						String.format(Locale.ENGLISH, "%f,%f", task.point.lat,
 								task.point.lon));
 				if (task.point.altitude != 0) { // Have altitude
 					sb.append(",alt=" + Math.round(task.point.altitude));
 				}
 				if (task.point.speed > 0) { // Have speed
 					sb.append(",sp=" + Math.round(task.point.speed));
 				}
 				if (task.point.accuracy > 0) { // Have accuracy
 					sb.append(",acc=" + Math.round(task.point.accuracy));
 				}
 				geoChild = "[[geo:" + sb + "]]";
 			}
 			if (null != task.points) { // Create KML
 				String kml = createKML(task);
 				if (null == kml) { // No KML
 					Log.w(TAG, "KML not created");
 					return false;
 				}
 				String link = "_files" + File.separator
 						+ System.currentTimeMillis() + ".kml";
 				File toFile = new File(folder, link);
 				if (!root.putFile(toFile.getAbsolutePath(), null, kml)) {
 					// Copy failed
 					Log.w(TAG, "Copy failed: " + link + ", " + toFile);
 					return false;
 				}
 				geoChild = "[[" + link + "]]";
 			}
 			String dateTimeFormat = App.getInstance().getStringPreference(
 					R.string.template_insertDateTime,
 					R.string.template_insertDateTimeDefault);
 			SimpleDateFormat df = new SimpleDateFormat(dateTimeFormat,
 					Locale.ENGLISH);
 			Node child = root.append(node, df.format(new Date(task.created))
 					+ " " + task.title);
 			if (null == child) { // Append failed
 				Log.e(TAG, "Node create failed");
 				return false;
 			}
 			if (null != mediaChild) { // Have media
 				boolean childCreated = root.append(child, mediaChild) != null;
 			}
 			if (null != geoChild) { // Have geo point
 				boolean childCreated = root.append(child, geoChild) != null;
 			}
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	private Element createElement(Document doc, String name, String ns) {
 		String _name = name;
 		String _prefix = "";
 		int idx = _name.indexOf(":");
 		if (idx != -1) {
 			_prefix = _name.substring(0, idx);
 			_name = _name.substring(idx + 1);
 		}
 		if (ns != null && !"".equals(ns)) {
 			Element e = doc.createElementNS(ns, _name);
 			if (!"".equals(_prefix)) { // Have prefix
 				e.setPrefix(_prefix);
 			}
 			return e;
 		}
 		return doc.createElement(_name);
 	}
 
 	private Element append(Element to, Element what) {
 		to.appendChild(what);
 		return to;
 	}
 
 	private Element addText(Element node, String text) {
 		node.appendChild(node.getOwnerDocument().createTextNode(text));
 		return node;
 	}
 
 	private static String KML_NS = "http://www.opengis.net/kml/2.2";
 	private static String KML_GOOGLE_NS = "http://www.google.com/kml/ext/2.2";
 
 	private String createKML(TaskBean task) {
 		try { // XML related errors
 			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 			dbfac.setNamespaceAware(true);
 			DocumentBuilder docBuilder;
 
 			docBuilder = dbfac.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			Element root = createElement(doc, "kml", KML_NS);
 			doc.appendChild(root);
 			Element document = createElement(doc, "Document", null);
 			root.appendChild(document);
 			append(document,
 					addText(createElement(doc, "name", null), task.title));
 			Element style = createElement(doc, "Style", null);
 			style.setAttribute("id", "line");
 			Element lineStyle = createElement(doc, "LineStyle", null);
 			append(lineStyle,
 					addText(createElement(doc, "color", null), "ffff0000"));
 			append(lineStyle, addText(createElement(doc, "width", null), "3"));
 			append(style, lineStyle);
 			append(document, style);
 			Element placemark = createElement(doc, "Placemark", null);
 			document.appendChild(placemark);
 			append(placemark,
 					addText(createElement(doc, "name", null), task.title));
 			append(placemark,
 					addText(createElement(doc, "styleUrl", null), "#line"));
 			Element track = createElement(doc, "gx:Track", KML_GOOGLE_NS);
 			placemark.appendChild(track);
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
 					"yyyy-MM-dd'T'HH:mm:ss'Z'");
 			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
 			for (PointBean point : task.points) {
 				// Create when elements
 				Element when = createElement(doc, "when", null);
 				addText(when, dateFormat.format(new Date(point.created)));
 				track.appendChild(when);
 			}
 			for (PointBean point : task.points) {
 				// Create when elements
 				Element coords = createElement(doc, "gx:coord", KML_GOOGLE_NS);
 				StringBuffer sb = new StringBuffer(String.format(
						Locale.ENGLISH, "%f %f", point.lon, point.lat));
 				if (point.altitude != 0) { // Add altitude
 					sb.append(" " + point.altitude);
 				}
 				addText(coords, sb.toString());
 				track.appendChild(coords);
 			}
 			TransformerFactory transfac = TransformerFactory.newInstance();
 			Transformer trans = transfac.newTransformer();
 			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
 			trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
 			// create string from xml tree
 			StringWriter sw = new StringWriter();
 			StreamResult result = new StreamResult(sw);
 			DOMSource source = new DOMSource(root);
 			trans.transform(source, result);
 			String xmlString = sw.toString();
 			Log.i(TAG, "XML: " + xmlString);
 			return xmlString;
 		} catch (Exception e) {
 			Log.e(TAG, "Error in XML", e);
 		}
 		return null;
 	}
 
 	@Override
 	public String getName() throws RemoteException {
 		return "Quebec4";
 	}
 }
