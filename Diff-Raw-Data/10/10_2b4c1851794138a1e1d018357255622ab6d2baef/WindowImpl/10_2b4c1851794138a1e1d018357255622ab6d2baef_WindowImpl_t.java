 package org.berkelium.java.impl;
 
 import java.io.InputStream;
 import java.util.Scanner;
 
 import org.berkelium.java.api.Berkelium;
 import org.berkelium.java.api.MultiDelegate;
 import org.berkelium.java.api.Window;
 import org.berkelium.java.api.WindowDelegate;
 import org.json.simple.JSONObject;
 
 public class WindowImpl implements Window {
 	private final Berkelium berkelium;
 	private WindowThreadProxy threadProxy;
 
 	public WindowImpl(Berkelium berkelium) {
 		this.berkelium = berkelium;
 		Context ctx = Context.getDefault();
 		context = ctx;
 		handle = _init(ctx.handle);
 	}
 
 	public Context getContext() {
 		return context;
 	}
 
 	private native void _setDelegate(WindowDelegate delegate);
 
	public synchronized WindowDelegate getDelegate() {
 		if (multiDelegate == null) {
 			return null;
 		}
 		return multiDelegate.getProxy();
 	}
 
 	public void resize(int width, int height) {
 		_resize(width, height);
 		WindowDelegate d = getDelegate();
 		if(d != null) {
 			// TODO
 			// move this into c++ code
 			// also handle browser window resizes
 			d.onResize(width, height);
 		}
 	}
 
 	public native int getId();
 
 	public native void setTransparent(boolean istrans);
 
 	public native void focus();
 
 	public native void unfocus();
 
 	public native void mouseMoved(int xPos, int yPos);
 
 	public native void mouseButton(int buttonID, boolean down);
 
 	public native void mouseWheel(int xScroll, int yScroll);
 
 	public native void textEvent(String evt);
 
 	public native void keyEvent(boolean pressed, int mods, int vk_code,
 			int scancode);
 
 	public native void _resize(int width, int height);
 
 	public native void adjustZoom(int mode);
 
 	public native void executeJavascript(String javascript);
 
 	public native void insertCSS(String css, String elementId);
 
 	public native boolean navigateTo(String url);
 
 	public native void refresh();
 
 	public native void stop();
 
 	public native void goBack();
 
 	public native void goForward();
 
 	public native boolean canGoBack();
 
 	public native boolean canGoForward();
 
 	public native void cut();
 
 	public native void copy();
 
 	public native void paste();
 
 	public native void undo();
 
 	public native void redo();
 
 	public native void del();
 
 	public native void selectAll();
 
 	public native void filesSelected(String files[]);
 
 	public native void addEvalOnStartLoading(String script);
 
 	public native void clearStartLoading();
 
 	public synchronized void destroy() {
 		if (multiDelegate != null) {
 			multiDelegate.clear();
 		}
 		if (handle != 0) {
 			_destroy(handle);
 			handle = 0;
 		}
 		context = null;
 	}
 
 	private native long _init(long contextHandle);
 
 	private native void _destroy(long handle);
 
 	private Context context;
 	protected volatile long handle;
 
 	public synchronized Window getThreadProxyWindow() {
 		if (threadProxy == null) {
 			threadProxy = new WindowThreadProxy(this);
 		}
 		return threadProxy.getProxy();
 	}
 
 	public Window getRealWindow() {
 		return this;
 	}
 
 	private MultiDelegate multiDelegate;
 
 	public synchronized void addDelegate(WindowDelegate delegate) {
 		if (multiDelegate == null) {
 			multiDelegate = new MultiDelegate();
 			_setDelegate(multiDelegate.getProxy());
 		}
 		multiDelegate.addDelegate(delegate);
 	}
 
 	public synchronized void removeDelegate(WindowDelegate delegate) {
 		if (multiDelegate != null) {
 			multiDelegate.removeDelegate(delegate);
 		}
 	}
 
 	JSImpl jsImpl;
 
 	private synchronized JSImpl getJS() {
 		if (jsImpl == null) {
 			jsImpl = new JSImpl(this);
 		}
 		return jsImpl;
 	}
 
 	public void bind(String name, Object target, String method,
 			Class<?>... types) {
 		getJS().bind(name, target, method, types);
 	}
 
 	public Berkelium getBerkelium() {
 		return berkelium;
 	}
 
 	public void call(String name, Object... arguments) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(name);
 		sb.append('(');
 		for (int i = 0; i < arguments.length; i++) {
 			if (i != 0) {
 				sb.append(',');
 			}
 			Object o = arguments[i];
 			if (o == null) {
 				sb.append("null");
 			} else if (o instanceof String) {
 				sb.append('"');
 				sb.append(JSONObject.escape((String) o));
 				sb.append('"');
 			} else {
 				sb.append(o);
 			}
 		}
 		sb.append(");");
 		executeJavascript(sb.toString());
 	}
 
 	public void setHtml(InputStream html) {
 		setHtml(new Scanner(html).useDelimiter("\\A").next());
 	}
 
 	public void setHtml(String html) {
 		navigateTo("data:text/html;charset=utf-8;base64," + Base64Coder.encodeString(html));		
 	}
 }
