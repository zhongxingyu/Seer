 package org.kt3k.straw.plugin;
 
 import org.kt3k.straw.annotation.PluginAction;
 import org.kt3k.straw.StrawDrink;
 import org.kt3k.straw.StrawPlugin;
 
 import android.content.Intent;
 import android.net.Uri;
 
 public class BrowserPlugin extends StrawPlugin {
 
	final static String NOT_URL = "12001";
 
 	@Override
 	public String getName() {
 		return "browser";
 	}
 
 
 	@PluginAction
 	public void open(SingleStringParam param, StrawDrink drink) {
 		String url = param.value;
 
 		if (!url.startsWith("http://") && !url.startsWith("https://")) {
 			drink.fail(NOT_URL, "not url: " + url);
 			return;
 		}
 
 		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 
 		// send intent
 		this.activity.startActivity(browserIntent);
 
 		drink.success();
 	}
 
 }
