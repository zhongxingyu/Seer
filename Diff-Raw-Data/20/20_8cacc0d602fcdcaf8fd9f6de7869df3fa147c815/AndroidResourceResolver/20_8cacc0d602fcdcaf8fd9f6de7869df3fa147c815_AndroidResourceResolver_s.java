 package com.avona.games.towerdefence.android;
 
 import java.io.InputStream;
 
 import android.content.res.Resources;
 
 import com.avona.games.towerdefence.res.ResourceResolver;
 
 public class AndroidResourceResolver implements ResourceResolver {
 	public static String PACKAGE_NAME = "com.avona.games.towerdefence.android";
 	private Resources res;
 
 	public AndroidResourceResolver(Resources res) {
 		this.res = res;
 	}
 
 	@Override
 	public InputStream getRawResource(String name) {
		final int id = res.getIdentifier(PACKAGE_NAME + ":" + name, null, null);
		return res.openRawResource(id);
 	}
 }
