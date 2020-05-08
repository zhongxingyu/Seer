 package org.kvj.sierra5.plugins.impl.link;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.kvj.sierra5.common.data.Node;
 import org.kvj.sierra5.common.plugin.FormatSpan;
 import org.kvj.sierra5.common.plugin.PluginInfo;
 import org.kvj.sierra5.common.theme.Theme;
 import org.kvj.sierra5.plugins.App;
 import org.kvj.sierra5.plugins.R;
 import org.kvj.sierra5.plugins.WidgetController;
 import org.kvj.sierra5.plugins.impl.DefaultPlugin;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.RemoteException;
 import android.text.style.ForegroundColorSpan;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public class LinkPlugin extends DefaultPlugin {
 
 	private WidgetController controller = null;
 
 	public LinkPlugin(WidgetController controller) {
 		super();
 		this.controller = controller;
 	}
 
 	private static final String TAG = "LinkPlugin";
 	Pattern linkPattern = Pattern.compile("\\[\\[(.+)\\]\\]");
 
 	@Override
 	public String getName() throws RemoteException {
 		return "Link plugin";
 	}
 
 	@Override
 	public int[] getCapabilities() throws RemoteException {
 		return new int[] { PluginInfo.PLUGIN_CAN_FORMAT,
 				PluginInfo.PLUGIN_CAN_PARSE, PluginInfo.PLUGIN_CAN_RENDER };
 	}
 
 	@Override
 	public FormatSpan[] format(int index, Theme theme, Node node, String text,
 			boolean selected) throws RemoteException {
 		switch (index) {
 		case 0: // Name
 			return new FormatSpan[] { new FormatSpan(text,
 					new ForegroundColorSpan(theme.caLGreen)) };
 		case 1: // Activity
 			return new FormatSpan[] {
 					new FormatSpan(text.substring(0, text.length() - 1),
 							new ForegroundColorSpan(theme.ceLCyan)),
 					new FormatSpan(":",
 							new ForegroundColorSpan(theme.colorText)) };
 		}
 		return null;
 	}
 
 	@Override
 	public int getFormatterCount() throws RemoteException {
 		return 2;
 	}
 
 	@Override
 	public String getPattern(int index, Node node, boolean selected)
 			throws RemoteException {
 		if (Node.TYPE_TEXT != node.type) { // Only for texts
 			return null;
 		}
 		switch (index) {
 		case 0: // Name
 			return "@[A-Za-z0-9\\-]+";
 		case 1: // Activity
 			return "#[A-Za-z0-9\\-]+\\:";
 		}
 		return null;
 	}
 
 	@Override
 	public void parse(Node node, Node parent) throws RemoteException {
 		if (node.type == Node.TYPE_TEXT && node.text.endsWith(" /-")) { // Collapse
 			node.collapsed = true;
 		}
 	}
 
 	private File getLink(Node node) {
 		Matcher m = linkPattern.matcher(node.text);
 		if (m.find()) { // Have link
 			String link = m.group(1);
 			Log.i(TAG, "Found link: " + link);
 			File file = null;
 			if (link.startsWith("/")) { // From root
 				try {
 					String root = controller.getRootService().getRoot();
 					File rootFile = new File(root);
 					if (!rootFile.exists()) { // Invalid root
 						return null;
 					}
 					file = new File(rootFile, link.substring(1));
 				} catch (Exception e) {
 					return null;
 				}
 			} else {
 				// Relative path
 				File rootFile = new File(node.file);
 				if (!rootFile.exists()) { // Invalid root
 					return null;
 				}
 				file = new File(rootFile.getParent(), link);
 			}
 			if (null != file && file.exists() && file.isFile()) { // File is OK
 				return file;
 			}
 		}
 		return null;
 	}
 
 	private Bitmap decodeFile(File f, int width) {
 		try {
 			// Decode image size
 			BitmapFactory.Options o = new BitmapFactory.Options();
 			o.inJustDecodeBounds = true;
 			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
 
 			// The new size we want to scale to
 			// Find the correct scale value. It should be the power of 2.
 			int scale = 1;
			while (o.outWidth / scale / 2 >= width)
 				scale *= 2;

 			// Decode with inSampleSize
 			BitmapFactory.Options o2 = new BitmapFactory.Options();
 			o2.inSampleSize = scale;
 			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
 		} catch (FileNotFoundException e) {
 		}
 		return null;
 	}
 
 	@Override
 	public RemoteViews render(Node node, Theme theme, int width)
 			throws RemoteException {
 		File file = getLink(node);
 		// Log.i(TAG, "render: " + node.text + ", " + file);
 		if (null == file || !file.getName().toLowerCase().endsWith(".jpg")) {
 			// Not an image
 			return null;
 		}
 		Bitmap image = decodeFile(file, width);
 		if (null == image) { // Not a bitmap
 			return null;
 		}
 		RemoteViews rv = new RemoteViews(App.getInstance().getPackageName(),
 				R.layout.link_image);
 		rv.setImageViewBitmap(R.id.link_image_image, image);
 		// rv.setInt(R.id.link_image_image, "setMaxWidth", width - 10);
 		// Log.i(TAG, "render: image rendered: " + width);
 		return rv;
 	}
 
 }
