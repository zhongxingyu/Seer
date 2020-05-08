 package org.berkelium.java.test;
 
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import junit.framework.Assert;
 
 import org.berkelium.java.api.Buffer;
 import org.berkelium.java.api.Rect;
 import org.berkelium.java.api.Window;
 import org.berkelium.java.api.WindowAdapter;
 import org.junit.Test;
 
 public class WindowDelegateTest extends AbstractBerkeliumTest {
 
 	@Test(timeout = 20000, expected=IllegalThreadStateException.class)
 	public void illegalThreadAccessTest() {
 		window.getRealWindow().canGoBack();
 	}
 
 	@Test(timeout = 20000)
 	public void onConsoleMessageTest() {
 		final String testMessage = createTestMessage();
 		final AtomicBoolean result = new AtomicBoolean(false);
 
 		window.addDelegate(new WindowAdapter() {
 			@Override
 			public void onConsoleMessage(Window win, String message,
 					String sourceId, int lineNo) {
 				Assert.assertEquals(win.getRealWindow(), window.getRealWindow());
 				if (testMessage.equals(message)) {
 					result.set(true);
 				}
 			}
 		});
 
 		window.executeJavascript("console.log('" + testMessage + "');");
 
 		runtime.sync(window);
 
 		Assert.assertTrue("onConsoleMessage not called!", result.get());
 	}
 
 	@Test(timeout = 20000)
 	public void onExternalHostTest() {
 		final String id = "urn:uuid:" + UUID.randomUUID().toString();
 		final String testMessage = createTestMessage();
 		final AtomicBoolean result = new AtomicBoolean(false);
 
 		window.addDelegate(new WindowAdapter() {
 			@Override
 			public void onExternalHost(Window win, String message,
 					String origin, String target) {
 				if (id.equals(target)) {
 					result.set(true);
 				}
 			}
 		});
 
 		window.executeJavascript("window.externalHost.postMessage('"
 				+ testMessage + "', '" + id + "');");
 
 		runtime.sync(window);
 
 		Assert.assertTrue("onExternalHost not called!", result.get());
 	}
 
 	@Test(timeout = 20000)
 	public void onPaintTest() throws InterruptedException {
 		final AtomicBoolean result = new AtomicBoolean(false);
 		
 		window.addDelegate(new WindowAdapter() {
 			@Override
 			public void onPaint(Window win, Buffer sourceBuffer,
 					Rect sourceBufferRect, Rect[] copyRects, int dx, int dy,
 					Rect scrollRect) {
 				Assert.assertEquals(win.getRealWindow(), window.getRealWindow());
 				result.set(true);
 			}
 		});
 
 		window.navigateTo("http://google.com/");
 
 		runtime.sync(window);
 
 		Assert.assertTrue("onPaint not called!", result.get());
 	}
 
 	@Test(timeout = 20000)
 	public void onResizeJavaTest() {
 		final AtomicInteger width = new AtomicInteger(0);
 
 		window.addDelegate(new WindowAdapter() {
 			@Override
 			public void onResize(Window win, int w, int h) {
 				Assert.assertEquals(win.getRealWindow(), window.getRealWindow());
 				width.set(w);
 			}
 		});
 
 		window.resize(640, 480);
 
 		runtime.sync(window);
 
 		Assert.assertEquals("onResize not called!", 640, width.get());
 	}
 
 	@Test(timeout = 20000)
 	public void onResizeRequested_ResizeTo_JavaScriptTest() {
 		final AtomicInteger width = new AtomicInteger(0);
 
 		window.addDelegate(new WindowAdapter() {
 			@Override
 			public void onResizeRequested(Window win, int x, int y, int w, int h) {
 				Assert.assertEquals(win.getRealWindow(), window.getRealWindow());
 				width.set(w);
 			}
 		});
 
 		window.executeJavascript("window.resizeTo(640, 480);");
 
 		runtime.sync(window);
 
 		Assert.assertEquals("onResizeRequested not called!", 640, width.get());
 	}
 
 	@Test(timeout = 20000)
 	public void onResizeRequested_MoveTo_JavaScriptTest() {
 		final AtomicInteger result = new AtomicInteger(0);
 
 		window.addDelegate(new WindowAdapter() {
 			@Override
 			public void onResizeRequested(Window win, int x, int y, int w, int h) {
 				Assert.assertEquals(win.getRealWindow(), window.getRealWindow());
 				System.err.printf("%d %d %d %d\n", x, y, w, h);
 				result.set(x);
 			}
 		});
 
 		window.executeJavascript("window.moveTo(123, 456);");
 
 		runtime.sync(window);
 
 		Assert.assertEquals("onResizeRequested not called!", 123, result.get());
 	}
 }
