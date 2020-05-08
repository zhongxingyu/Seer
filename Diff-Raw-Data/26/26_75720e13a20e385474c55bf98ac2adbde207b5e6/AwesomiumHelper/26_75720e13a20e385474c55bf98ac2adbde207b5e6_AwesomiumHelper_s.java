 package browser;
 
 import game.Game;
 
 import java.io.File;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.List;
 
 import reflection.Reflection;
 import settings.Settings;
 import util.Worker;
 
 import com.jogamp.newt.event.KeyEvent;
 import com.sun.jna.Callback;
 import com.sun.jna.Library;
 import com.sun.jna.Native;
 import com.sun.jna.Pointer;
 import com.sun.jna.PointerType;
 import com.sun.jna.Structure;
 
 public class AwesomiumHelper {
 	public static Worker worker;
 	private static ConsoleCallback console = new ConsoleCallback();
 	private static awe_webview webview;
 	private static ByteBuffer buffer;
 	private static boolean mouseDirty;
 	private static int mouseY;
 	private static int mouseX;
 	private static final FungineCallback fungineCallback = new FungineCallback();
 
 	static {
 		if (Settings.IS_WINDOWS) {
 			// System.setProperty("jna.library.path", "libs\\\\awesomium");
 			INSTANCE = (Awesomium) Native.loadLibrary("Awesomium",
 					Awesomium.class);
 		} else {
			// System.setProperty("jna.library.path", "/usr/lib/awesomium-1.6.5");
 			INSTANCE = (Awesomium) Native.loadLibrary("awesomium-1.6.5",
 					Awesomium.class);
 		}
 	}
 
 	private final static Awesomium INSTANCE;
 
 	private interface Awesomium extends Library {
 
 		void awe_webcore_initialize_default();
 
 		void awe_webcore_initialize(boolean enable_plugins,
 				boolean enable_javascript, boolean enable_databases,
 				awe_string package_path, awe_string locale_path,
 				awe_string user_data_path, awe_string plugin_path,
 				awe_string log_path, int log_level,
 				boolean force_single_process, awe_string child_process_path,
 				boolean enable_auto_detect_encoding,
 				awe_string accept_language_override,
 				awe_string default_charset_override,
 				awe_string user_agent_override, awe_string proxy_server,
 				awe_string proxy_config_script,
 				awe_string auth_server_whitelist,
 				boolean save_cache_and_cookies, int max_cache_size,
 				boolean disable_same_origin_policy,
 				boolean disable_win_message_pump, awe_string custom_css);
 
 		void awe_webcore_shutdown();
 
 		awe_webview awe_webcore_create_webview(int width, int height,
 				boolean view_source);
 
 		awe_string awe_string_empty();
 
 		awe_string awe_string_create_from_ascii(String str, int len);
 
 		void awe_webview_load_url(awe_webview webview, awe_string url,
 				awe_string frame_name, awe_string username, awe_string password);
 
 		void awe_webview_load_file(awe_webview webview, awe_string file,
 				awe_string frame_name);
 
 		boolean awe_webview_is_loading_page(awe_webview webview);
 
 		void awe_webcore_update();
 
 		awe_renderbuffer awe_webview_render(awe_webview webview);
 
 		boolean awe_renderbuffer_save_to_jpeg(awe_renderbuffer renderbuffer,
 				awe_string file_path, int quality);
 
 		Pointer awe_renderbuffer_get_buffer(awe_renderbuffer renderbuffer);
 
 		void awe_webcore_set_base_directory(awe_string base_dir_path);
 
 		boolean awe_webview_is_dirty(awe_webview webview);
 
 		void awe_webview_set_transparent(awe_webview webview,
 				boolean is_transparent);
 
 		void awe_webview_inject_mouse_move(awe_webview webview, int x, int y);
 
 		void awe_webview_inject_mouse_down(awe_webview webview, int button);
 
 		void awe_webview_inject_mouse_up(awe_webview webview, int button);
 
 		void awe_webview_execute_javascript(awe_webview webview,
 				awe_string javascript, awe_string frame_name);
 
 		void awe_webview_set_callback_js_console_message(awe_webview webview,
 				Callback cp);
 
 		Pointer awe_string_get_utf16(awe_string awe_string);
 
 		int awe_string_get_length(awe_string awe_string);
 
 		void awe_webview_inject_keyboard_event(awe_webview webview,
 				awe_webkeyboardevent.ByValue key_event);
 
 		void awe_webview_create_object(awe_webview webview,
 				awe_string object_name);
 
 		void awe_webview_focus(awe_webview webview);
 
 		void awe_webview_set_object_callback(awe_webview webview,
 				awe_string object_name, awe_string callback_name);
 
 		void awe_webview_set_callback_js_callback(awe_webview webview,
 				Callback execute);
 
 		awe_jsvalue awe_jsarray_get_element(awe_jsarray jsarray, int index);
 
 		awe_string awe_jsvalue_to_string(awe_jsvalue jsvalue);
 	}
 
 	private static awe_string createString(String string) {
 		return INSTANCE.awe_string_create_from_ascii(string, string.length());
 	}
 
 	public static String createString(awe_string source) {
 		int length = INSTANCE.awe_string_get_length(source) * 2;
 		Pointer source2 = INSTANCE.awe_string_get_utf16(source);
 		return new String(source2.getByteArray(0, length),
 				Charset.forName("UTF-16LE"));
 	}
 
 	private static final class BrowserWorker extends Worker {
 
 		public BrowserWorker() {
 			super(20, "BrowserThread", false);
 		}
 
 		@Override
 		protected void executeRepeatedly() {
 			super.executeRepeatedly();
 			if (webview != null) {
 				if (mouseDirty) {
 					if (Game.INSTANCE.loop.renderer != null)
 						INSTANCE.awe_webview_inject_mouse_move(
 								webview,
 								(int) (mouseX * (float) Settings.WIDTH / Game.INSTANCE
 										.getWidth()),
 								(int) (mouseY * (float) Settings.HEIGHT / Game.INSTANCE
 										.getHeight()));
 					mouseDirty = false;
 				}
 				update();
 			}
 		}
 	}
 
 	public static void init(final boolean usePlugins) {
 		worker = new BrowserWorker();
 		worker.addJob(new Runnable() {
 			@Override
 			public void run() {
 				AwesomiumHelper.init(
 						new File(Settings.ENGINE_FOLDER).getAbsolutePath(),
 						usePlugins);
 			}
 		});
 		worker.start();
 	}
 
 	protected static void init(String baseDirectory, boolean usePlugins) {
 		INSTANCE.awe_webcore_initialize(usePlugins, true, false,
 				INSTANCE.awe_string_empty(), INSTANCE.awe_string_empty(),
 				INSTANCE.awe_string_empty(), INSTANCE.awe_string_empty(),
 				INSTANCE.awe_string_empty(), 1, false,
 				INSTANCE.awe_string_empty(), true, INSTANCE.awe_string_empty(),
 				INSTANCE.awe_string_empty(), INSTANCE.awe_string_empty(),
 				INSTANCE.awe_string_empty(), INSTANCE.awe_string_empty(),
 				INSTANCE.awe_string_empty(), true, 0, false, false,
 				INSTANCE.awe_string_empty());
 
 		INSTANCE.awe_webcore_set_base_directory(createString(baseDirectory));
 		webview = INSTANCE.awe_webcore_create_webview(Settings.WIDTH,
 				Settings.HEIGHT, false);
 		INSTANCE.awe_webview_create_object(webview,
 				createString("fungineCallback"));
 		INSTANCE.awe_webview_set_object_callback(webview,
 				createString("fungineCallback"), createString("execute"));
 		INSTANCE.awe_webview_set_callback_js_console_message(webview, console);
 		INSTANCE.awe_webview_set_callback_js_callback(webview, fungineCallback);
 		INSTANCE.awe_webview_set_transparent(webview, true);
 	}
 
 	public static void dispose() {
 		worker.addJob(new Runnable() {
 			@Override
 			public void run() {
 				INSTANCE.awe_webcore_shutdown();
 				webview = null;
 			}
 		});
 	}
 
 	public static void loadUrl(final String site, final Runnable... callBack) {
 		// Log.log(AwesomiumHelper.class, "Loading url: " + site);
 		worker.addJob(new Runnable() {
 			@Override
 			public void run() {
 				INSTANCE.awe_webview_load_url(webview, createString(site),
 						INSTANCE.awe_string_empty(),
 						INSTANCE.awe_string_empty(),
 						INSTANCE.awe_string_empty());
 				onLoad(callBack);
 			}
 		});
 	}
 
 	public static void loadFile(final String file, final Runnable... callBack) {
 		// Log.log(AwesomiumHelper.class, "Loading file: " + file);
 		worker.addJob(new Runnable() {
 			@Override
 			public void run() {
 				INSTANCE.awe_webview_load_file(webview, createString(file),
 						INSTANCE.awe_string_empty());
 				onLoad(callBack);
 			}
 		});
 	}
 
 	protected static void onLoad(Runnable... callBack) {
 		long timeoutStart = System.currentTimeMillis();
 		while (INSTANCE.awe_webview_is_loading_page(webview)
 				&& System.currentTimeMillis() - timeoutStart < 1000) {
 			update(100);
 			// Log.log(AwesomiumHelper.class, "loading page");
 		}
 		// Log.log(AwesomiumHelper.class, "Page loaded");
 		update(30);
 		INSTANCE.awe_webview_focus(webview);
 		for (Runnable r : callBack)
 			r.run();
 	}
 
 	private static void update(long waitTime) {
 		try {
 			Thread.sleep(waitTime);
 			INSTANCE.awe_webcore_update();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void update() {
 		INSTANCE.awe_webcore_update();
 		if (INSTANCE.awe_webview_is_dirty(webview)) {
 			awe_renderbuffer renderBuffer = INSTANCE
 					.awe_webview_render(webview);
 			Pointer str = INSTANCE.awe_renderbuffer_get_buffer(renderBuffer);
 			buffer = str.getByteBuffer(0, Settings.WIDTH * Settings.HEIGHT * 4);
 			/*
 			 * for (int i = 3; i < Settings.WIDTH; i += 4) {
 			 * System.out.print((buffer.getChar(i) & 0xFF) + ", "); }
 			 */
 		}
 	}
 
 	public static ByteBuffer getBuffer() {
 		return buffer;
 	}
 
 	public static void mouseMoved(final int x, final int y) {
 		mouseX = x;
 		mouseY = y;
 		mouseDirty = true;
 	}
 
 	public static void mouseButton(final int i, final boolean down) {
 		worker.addJob(new Runnable() {
 			@Override
 			public void run() {
 				if (down)
 					INSTANCE.awe_webview_inject_mouse_down(webview, i);
 				else
 					INSTANCE.awe_webview_inject_mouse_up(webview, i);
 			}
 		});
 	}
 
 	public static void executeJavascript(String js) {
 		INSTANCE.awe_webview_execute_javascript(webview, createString(js),
 				INSTANCE.awe_string_empty());
 	}
 
 	public static void keyTyped(final KeyEvent e) {
 		worker.addJob(new Runnable() {
 			@Override
 			public void run() {
 				awe_webkeyboardevent.ByValue event = new awe_webkeyboardevent.ByValue();
 				event.set(e);
 				INSTANCE.awe_webview_inject_keyboard_event(webview, event);
 			}
 		});
 
 	}
 
 	public static class awe_renderbuffer extends PointerType {
 
 	}
 
 	public static class awe_string extends PointerType {
 
 	}
 
 	public static class awe_webview extends PointerType {
 
 	}
 
 	public static class awe_jsvalue extends PointerType {
 
 	}
 
 	public static class awe_jsarray extends PointerType {
 
 	}
 
 	public static class awe_webkeyboardevent extends Structure {
 		public static class ByValue extends awe_webkeyboardevent implements
 				Structure.ByValue {
 		}
 
 		public int type;
 		public int modifiers;
 		public int virtual_key_code;
 		public int native_key_code;
 		public char text[];
 		public char unmodified_text[];
 		public boolean is_system_key;
 
 		@Override
 		protected List getFieldOrder() {
 			return Arrays.asList("type", "modifiers", "virtual_key_code",
 					"native_key_code", "text", "unmodified_text",
 					"is_system_key");
 		}
 
 		public awe_webkeyboardevent set(KeyEvent e) {
 			type = 2;
 			modifiers = e.getModifiers();
 			virtual_key_code = e.getKeyCode();
 			native_key_code = e.getKeyCode();
 			text = new char[] { e.getKeyChar(), 0, 0, 0, 0 };
 			unmodified_text = new char[] { e.getKeyChar(), 0, 0, 0, 0 };
 			is_system_key = false;
 			return this;
 		}
 
 	}
 
 	public static final class ConsoleCallback implements Callback {
 
 		public void callback(awe_webview caller, awe_string message,
 				int lineNumber, awe_string source) {
 			// Log.log(this, createString(message) + ":" + lineNumber + ":"
 			// + createString(source));
 		}
 	}
 
 	public static final class FungineCallback implements Callback {
 		public void callback(awe_webview caller, awe_string object_name,
 				awe_string callback_name, awe_jsarray arguments) {
 			awe_jsvalue element = INSTANCE
 					.awe_jsarray_get_element(arguments, 0);
 			String arg = createString(INSTANCE.awe_jsvalue_to_string(element));
 			// Log.log(this, createString(object_name) + "."
 			// + createString(callback_name) + "(" + arg + ")");
 			Reflection.execute(arg);
 		}
 	}
 }
