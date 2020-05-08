 /*
  *
  * This file is part of three4g.
  *
  * three4g is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesse General Public License as 
  * published by the Free Software Foundation, either version 3 of 
  * the License, or (at your option) any later version.
  *
  * three4g is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public 
  * License along with three4g. If not, see <http://www.gnu.org/licenses/>.
  *
  * (c) 2012 by Oliver Damm, Am Wasserberg 8, 22869 Schenefeld
  *
  * mail: oliver [dot] damm [at] gmx [dot] de
  * web: http://www.blimster.net 
  */
 package net.blimster.gwt.threejs.extras;
 
import net.blimster.gwt.threejs.textures.Mapping;
 import net.blimster.gwt.threejs.textures.Texture;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 /**
  * @author Oliver Damm
  */
 public final class ImageUtils extends AbstractImageUtils
 {
 
     protected ImageUtils()
     {
 	super();
     }
 
     public native static Texture loadTexture(String path)
     /*-{
 
 		return $wnd.THREE.ImageUtils.loadTexture(path);
 
     }-*/;
 
     public native static Texture loadTexture(String path, Mapping mapping)
     /*-{
 
 		return $wnd.THREE.ImageUtils.loadTexture(path, mapping);
 
     }-*/;
 
     public static Texture loadTexture(String path, Mapping mapping, LoadTextureCallback callback)
     {
 	if (callback == null)
 	{
 	    throw new IllegalArgumentException("callback is null");
 	}
 
 	return loadTexture(path, mapping, createJavaScriptCallback(callback));
     }
 
     private static native Texture loadTexture(String path, Mapping mapping, JavaScriptObject callback)
     /*-{
 
 		return $wnd.THREE.ImageUtils.loadTexture(path, mapping, callback);
 
     }-*/;
 
     private native static JavaScriptObject createJavaScriptCallback(LoadTextureCallback callback)
     /*-{
 
 		return function(image) {
 			callback.@net.blimster.gwt.threejs.extras.LoadTextureCallback::onTextureLoaded(Ljava/lang/String;)(image.src);
 		};
 
     }-*/;
 
 }
