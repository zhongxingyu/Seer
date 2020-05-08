 package sikulix.fixture;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.sikuli.basics.Debug;
 import org.sikuli.basics.FileManager;
 import org.sikuli.script.App;
 import org.sikuli.script.Env;
 import org.sikuli.script.FindFailed;
 import org.sikuli.script.Key;
 import org.sikuli.script.KeyModifier;
 import org.sikuli.script.Match;
 import org.sikuli.script.Pattern;
 import org.sikuli.script.Region;
 import org.sikuli.script.Screen;
 import fit.Counts;
 import fitlibrary.CommentFixture;
 import fitlibrary.DoFixture;
 
 /**
  * @author szukov
  * 
  */
 @SuppressWarnings("deprecation")
 public class SikuliXDoFixture extends DoFixture {
 	private float maxTimeout = 30;
 	private Region _lastRegion;
 	private Match _lastMatch;
 	private Pattern _lastPattern;
 	private String _lastRegionSnapshot;
 	private App _lastApp;
 	private Env _lastEnv;
 	public Object SystemUnderTest;
 	private Field[] keys = Key.class.getDeclaredFields();
 	private Field[] modifiers = KeyModifier.class.getDeclaredFields();
 
 	/**
 	 * SikuliX do fixture
 	 */
 	public SikuliXDoFixture() {
 		useScreen(); // Screen is default region
 		List<Field> l = Arrays.asList(keys);
 		Collections.sort(l,Collections.reverseOrder(new Comparator<Field>() {
             @Override
             public int compare(final Field field1, final Field field2) {
                 return Integer.compare(field1.getName().length(),field2.getName().length());
             }
            } ));
         keys = (Field[]) l.toArray();
 		// maxTimeout = Settings.AutoWaitTimeout;
 	}
 
 	/**
 	 * Retrieve current context (SystemUnderTests) object
 	 */
 	@Override
 	public Object getSystemUnderTest() {
 		return SystemUnderTest;
 	}
 
 	/**
 	 * Set current context (SystemUnderTests) object
 	 */
 	@Override
 	public void setSystemUnderTest(Object SystemUnderTests) {
 		this.SystemUnderTest = SystemUnderTests;
 	}
 
 	/**
 	 * wait for the window with Given title
 	 * 
 	 * @param title
 	 * @return Region of the retrieved window
 	 */
 	public Region waitForWindow(String title) {
 		int i = 0;
 		Region r = null;
 		while ((isWindowPresent(title) == false) && (i++ < maxTimeout)) {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		r = useRegion(title);
 		return r;
 	}
 
 	/**
 	 * Sets context to the last used Region
 	 * 
 	 * @return Region
 	 */
 	public Region useRegion() {
 		useRegion(_lastRegion);
 		return _lastRegion;
 	}
 
 	/**
 	 * Sets context to the Region passed as parameter
 	 * 
 	 * @param r
 	 *            - Region
 	 * @return - Region
 	 */
 	private Region useRegion(Region r) {
 		_lastRegion = (r == null) ? new Screen() : new Region(r);
 		// _lastRegion.setLocation(_lastRegion.getCenter());
 		_lastRegionSnapshot = captureRegion(_lastRegion);
 		setSystemUnderTest(_lastRegion);
 		return _lastRegion;
 	}
 
 	/**
 	 * Captures region in temporary file and returns file path. File is not
 	 * being kept after class deleted.
 	 * 
 	 * @param r
 	 *            - Region
 	 * @return - Region
 	 */
 	private String captureRegion(Region r) {
 		String filename = null;
 		try {
 			// Debug.info(r.toString());
 			Screen scr = r.getScreen();
 			filename = scr.capture(r.getX(), r.getY(), r.getW(), r.getH())
 					.getFile();
 			Debug.info(filename);
 		} catch (Exception e) {
 			e.printStackTrace();
 			filename = "Failed to capture region.";
 		}
 		return filename;
 	}
 
 	/**
 	 * Capture Current Region in the temporary file. File gets deleted after
 	 * test run.
 	 * 
 	 * @return Full path to the captured region snapshot.
 	 */
 	public String captureRegion() {
 		return captureRegion(_lastRegion);
 	}
 
 	/**
 	 * Returns true if last used region is still visible. Could be used to
 	 * identify hidden window.
 	 * 
 	 * @param r
 	 *            - Region
 	 * @return - Region
 	 */
 	public boolean isRegionVisible() {
 		Pattern p = new Pattern(_lastRegionSnapshot).exact();
 		Region searchRegion = new Region(_lastRegion);
 		boolean result = (searchRegion.exists(p, 0) != null);
 		return result;
 	}
 
 	/**
 	 * Sets context to the Region of application for given title
 	 * 
 	 * @param s
 	 *            - window title (substring)
 	 * @return Window region
 	 */
 	public Region useRegion(String s) {
 		useApplication(s);
 		Region r = useRegion(_lastApp.window());
 		return r;
 	}
 
 	/**
 	 * Sets context to the last Match
 	 * 
 	 * @return Match
 	 */
 	public Match useMatch() {
 		Match m = _lastMatch;
 		m = useMatch(m);
 		return m;
 	}
 
 	/**
 	 * Sets context to the given Match
 	 * 
 	 * @param m
 	 *            - Match object to be used as a new context
 	 * @return - Match
 	 */
 	private Match useMatch(Match m) {
 		_lastMatch = m;
 		SystemUnderTest = m;
 		return _lastMatch;
 	}
 
 	/**
 	 * Set context to the Match using find in the lastRegion for given Pattern
 	 * 
 	 * @param p
 	 *            - Pattern
 	 * @return - Match
 	 */
 	private Match useMatch(Pattern p) {
 		Match m = null;
 		try {
 			m = _lastRegion.find(p);
 			return useMatch(m);
 		} catch (FindFailed e) {
 			e.printStackTrace();
 		}
 		return m;
 	}
 
 	/**
 	 * Set context to the Match using find in the lastRegion for Pattern defined
 	 * by input string
 	 * 
 	 * @param s
 	 *            - image file
 	 * @return - Match
 	 */
 	public Match useMatch(String s) {
 		Pattern p = usePattern(s);
 		Match m = useMatch(p);
 		return m;
 	}
 
 	/**
 	 * Same as givenMatch(s)
 	 * 
 	 * @param s
 	 *            - Image file
 	 * @return - Match
 	 */
 	public Match findMatch(String s) {
 		return useMatch(s);
 	}
 
 	/**
 	 * Finds last used pattern in region
 	 * 
 	 * @return - Match for last used pattern
 	 */
 	public Match findMatch() {
 		return useMatch(_lastPattern);
 	}
 
 	/**
 	 * Set context to the last known Pattern
 	 * 
 	 * @return - Pattern
 	 */
 	public Pattern usePattern() {
 		Pattern p = _lastPattern;
 		return usePattern(p);
 	}
 
 	/**
 	 * Set context to the Pattern
 	 * 
 	 * @return - Pattern
 	 */
 	private Pattern usePattern(Pattern p) {
 		_lastPattern = p;
 		setSystemUnderTest(p);
 		return p;
 	}
 
 	/**
 	 * Set context to the Pattern created from string
 	 * 
 	 * @param s
 	 *            - image file
 	 * @return - Pattern
 	 */
 	public Pattern usePattern(String s) {
 		Pattern p = new Pattern(s);
 		usePattern(p);
 		return p;
 	}
 
 	/**
 	 * Set context to the last used Application
 	 * 
 	 * @return App object
 	 */
 	public App useApplication() {
 		useApplication(_lastApp);
 		return _lastApp;
 	}
 
 	/**
 	 * Set context to the given Application
 	 * 
 	 * @param a
 	 *            App object
 	 * @return App object
 	 */
 	private App useApplication(App a) {
 		_lastApp = a;
 		useRegion(_lastApp.window());
 		setSystemUnderTest(_lastApp);
 		return _lastApp;
 	}
 
 	/**
 	 * Set context to the Application with given title
 	 * 
 	 * @param s
 	 *            - Application title (substring)
 	 * @return App object
 	 */
 	public App useApplication(String s) {
 		App a = new App(s);
 		useApplication(a);
 		return a;
 	}
 
 	/**
 	 * Set Context to the full screen (default Region)
 	 * 
 	 * @return Region
 	 */
 	public Region useScreen() {
 		Region r = new Screen();
 		useRegion(r);
 		return _lastRegion;
 	};
 
 	/**
 	 * Sets context to the Monitor #
 	 * 
 	 * @param scrNumber
 	 *            - number of monitor
 	 * @return Region of the given monitor
 	 */
 	public Region useScreen(int scrNumber) {
 		try {
 			_lastRegion = new Screen(scrNumber);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		useRegion(_lastRegion);
 		return _lastRegion;
 	};
 
 	/**
 	 * Set context to the Env object Env is useful to call getClipboard() Note:
 	 * Env object is marked as depricated.
 	 * 
 	 * @return Env object
 	 */
 	public Env useEnvironment() {
 		_lastEnv = new Env();
 		setSystemUnderTest(_lastEnv);
 		return _lastEnv;
 	}
 
 	/**
 	 * Print string to the log - appears in the captured output
 	 * 
 	 * @param s
 	 *            String to be recorded to the log
 	 */
 	public void print(Object s) {
 		Debug.info("SDF:" + s.toString());
 	}
 
 	/**
 	 * Pause execution for number of seconds
 	 * 
 	 * @param seconds
 	 * @throws InterruptedException
 	 */
 	public void pauseForSeconds(double seconds) throws InterruptedException {
 		Thread.sleep((long) (seconds * 1000.0));
 	};
 
 	private int reverseModifier(String s) {
 		int result = 0;
 		for (Field f : modifiers) {
 			String keyName = f.getName();
 			if ((f.getType().equals(int.class))
 					&& (s.contains(keyName) == true)) {
 				try {
 					result += f.getInt(null);
 				} catch (IllegalArgumentException | IllegalAccessException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return result;
 	};
 
 	private String reverseKey(String s) {
 		String result = s;
 		for (Field f : keys) {
 			String keyName = f.getName();
 			if (result.contains(keyName) == true) {
 				try {
 					result = result.replaceAll(keyName, (String) f.get(null));
 				} catch (IllegalArgumentException | IllegalAccessException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return result;
 	};
 
 	/**
 	 * Press key[s]. Unlike {@link #enter(String)} resolves key names into
 	 * actual keystrokes i.e. ENTER turns into actual line feed.
 	 * 
 	 * @param s
 	 *            - string to be interpret into key press i.e.: ENTER TAB ESC
 	 *            RIGHT
 	 * @return 1 - was possible; 0 - was not possible
 	 */
 	public int press(String s) {
 		int result;
 		s = reverseKey(s);
 		result = typeInRegion(s);
 		return result;
 	};
 
 	/**
 	 * Press key[s] combined with special key (modifiers) such as CTRL+C
 	 * 
 	 * @param modifier
 	 *            - string to be interpret into modifier: ALT CTRL SHIFT
 	 * @param s
 	 *            - string to be interpret into key press i.e.: C F4
 	 * @return 1 - was possible; 0 - was not possible examples:
 	 * 
 	 *         <pre>
 	 * |press |CTRL|Plus|C|
 	 * |press |ALT|Plus|F4|
 	 * |press |SHIFT|Plus|bob|
 	 * </pre>
 	 */
 	public int pressPlus(String modifier, String s) {
 		int km = reverseModifier(modifier);
 		String text = reverseKey(s);
 		Debug.info("pressPlus(int " + modifier + ", String " + s.toString()
 				+ ")");
 		Region r = _lastRegion;
 		return r.type(text, km);
 	};
 
 	/**
 	 * <pre>
 	 * Type text into the current region
 	 * 
 	 * Unlike {@link #press(String)} does not resolve keys (i.e. string "ENTER" will be interpret as text not as line feed)
 	 * Note that "was possible" does not mean "text was entered".
 	 * type string into region
 	 * |Type |Some Text| in Region|
 	 * or
 	 * |Type in Region|Some Text|
 	 * </pre>
 	 * 
 	 * @param s
 	 *            - string of text to be entered
 	 * @return 1 - was possible; 0 - was not possible
 	 */
 	public int typeInRegion(String s) {
 		// Debug.info("typeInRegion(String "+s.toString()+")");
 		return _lastRegion.type(s);
 	};
 
 	/**
 	 * Same as typeInRegion
 	 * 
 	 * @param s
 	 *            - string of text to be entered
 	 * @return 1 - was possible; 0 - was not possible
 	 */
 	public int enter(String s) {
 		return typeInRegion(s);
 	};
 
 	/**
 	 * Highlight last match
 	 * 
 	 * @param i
 	 *            - seconds
 	 */
 	public void highlightMatch(int i) {
 		_lastMatch.highlight(i);
 	};
 
 	/**
 	 * Highlight last region
 	 * 
 	 * @param i
 	 *            - seconds
 	 */
 	public void highlightRegion(int i) {
 		_lastRegion.highlight(i);
 	};
 
 	/**
 	 * Click in the middle of last match
 	 */
 	public void clickOn() {
 		_lastMatch.click();
 	};
 
 	/**
 	 * Click in the middle of found match
 	 * 
 	 * @param s
 	 *            - image file
 	 */
 	public void clickOn(String s) {
 		Match m = useMatch(s);
 		m.click();
 	};
 
 	/**
 	 * double Click in the found match
 	 * 
 	 * @param s
 	 *            - image file
 	 */
 	public void doubleClickOn(String s) {
 		Match m = useMatch(s);
 		m.doubleClick();
 	};
 
 	/**
 	 * right Click in the found match
 	 * 
 	 * @param s
 	 *            - image file
 	 */
 	public void rightClickOn(String s) {
 		Match m = useMatch(s);
 		m.rightClick();
 	};
 
 	/**
 	 * Sets search region to the whole application window
 	 */
 	public Region inTheApplicationWindow() {
 		return useRegion(_lastApp.window());
 	}
 
 	/**
 	 * Sets search region to the top half of application window
 	 */
 	public Region inTheTop() {
 		Region r = _lastApp.window();
 		r.setH(r.getH() / 2);
 		useRegion(r);
 		return r;
 	}
 
 	/**
 	 * Sets search region to the left half of application window
 	 */
 	public Region onTheLeft() {
 		Region r = _lastApp.window();
 		r.setW(r.getW() / 2);
 		useRegion(r);
 		return r;
 	}
 
 	/**
 	 * Sets search region to the bottom half of application window
 	 */
 	public Region inTheBottom() {
 		Region r = _lastApp.window();
 		r.setH(r.getH() / 2);
 		r.setY(r.getY() + r.getH());
 		useRegion(r);
 		return r;
 	}
 
 	/**
 	 * Sets search region to the right half of application window
 	 */
 	public Region onTheRight() {
 		Region r = _lastApp.window();
 		r.setW(r.getW() / 2);
 		r.setX(r.getX() + r.getW());
 		useRegion(r);
 		return r;
 	}
 
 	/**
 	 * Sets search region on the right from the found match within current
 	 * application window Useful for controls where label is on the left from
 	 * the entry area. Height of region is defined by height of pattern
 	 * 
 	 * @param imageFile
 	 *            - image file path or URL
 	 */
 	public Region onTheRightFromMatch(String imageFile) {
 		Match m = useMatch(imageFile);
 		Region w = _lastApp.window();
		int maxWidth = w.getW() - m.getX();
 		Region r = m.right(maxWidth);
 		useRegion(r);
 		return r;
 	};
 
 	/**
 	 * Sets search region on the right from the found match within current
 	 * application window Useful for fields located on the left from some
 	 * pattern (i.e. edit-box followed by drop-down) Height of region is defined
 	 * by height of pattern
 	 * 
 	 * @param imageFile
 	 *            - image file path or URL
 	 */
 	public Region onTheLeftFromMatch(String imageFile) {
 		Match m = useMatch(imageFile);
 		Region w = _lastApp.window();
 		int maxWidth = m.getX() - w.getX();
 		Region r = m.left(maxWidth);
 		useRegion(r);
 		return r;
 	};
 
 	/**
 	 * Sets search region below the found match within current application
 	 * window Useful for controls where label is above the entry area
 	 * 
 	 * @param s
 	 *            - image file
 	 */
 	public Region belowMatch(String s) {
 		Match m = useMatch(s);
 		Region w = _lastApp.window();
 		int maxHight = w.getH() - m.getY();
 		Region r = m.below(maxHight);
 		useRegion(r);
 		return r;
 	};
 
 	/**
 	 * Returns true (or false) if given application title present. If true then
 	 * changes context to the region.
 	 * 
 	 * @param windowTitle
 	 *            (substring)
 	 * @return true if exists; false otherwise
 	 */
 	public Boolean isWindowPresent(String windowTitle) {
 		App a = new App(windowTitle);
 		Region window = a.window();
 		if ((window == null) || (captureRegion(window) == null)) {
 			return false;
 		} else {
 			useRegion(window);
 			return true;
 		}
 	};
 
 	/**
 	 * Return true (or false) if last used pattern exists on the screen
 	 * 
 	 * @return true if exists; false otherwise Does not change context to the
 	 *         match unlike findMatch
 	 */
 	public Boolean isMatchPresent() {
 		try {
 			Pattern p = _lastPattern;
 			_lastRegion.find(p);
 			return true;
 		} catch (FindFailed e) {
 			return false;
 		}
 	};
 	/**
 	 * Return true (or false) if given image exists on the screen
 	 * 
 	 * @param imageFile
 	 * @return true if exists; false otherwise Does not change context to the
 	 *         match unlike findMatch
 	 */
 	public Boolean isMatchPresent(String imageFile) {
 		Boolean result;
 		_lastPattern = new Pattern(imageFile);
 		result = isMatchPresent();
 		return result; 
 	};
 
 	/**
 	 * Set current context to the region limited between two sample images.
 	 * Search performed in the current region.
 	 * 
 	 * @param imageFrom
 	 *            - image of the top left corner
 	 * @param imageTo
 	 *            - image of the bottom right corner
 	 * @return New region between imageFrom and imageTo
 	 * @throws FindFailed
 	 */
 	public Region useRegionBetweenAnd(String imageFrom, String imageTo)
 			throws FindFailed {
 		int x, y, w, h;
 		Region r;
 		Pattern p;
 		r = new Region(_lastRegion);
 		p = new Pattern(imageFrom);
 		Match mFrom = _lastRegion.find(p);
 		p = new Pattern(imageTo);
 		Match mTo = _lastRegion.find(p);
 		x = Math.min(mFrom.getX() + mFrom.getW(), mTo.getX());
 		y = Math.min(mFrom.getY() + mFrom.getH(), mTo.getY());
 		w = Math.max(mFrom.getX() + mFrom.getW(), mTo.getX()) - x;
 		h = Math.max(mFrom.getY() + mFrom.getH(), mTo.getY()) - y;
 		r = new Region(x, y, w, h);
 		return useRegion(r);
 	}
 
 	/**
 	 * Gets a region which starts with imageFrom in the top left corner and ends with imageTo in the bottom right corner (both inclusive).  
 	 * @param imageFrom
 	 * @param imageTo
 	 * @return Match
 	 * @throws FindFailed
 	 */
 	public Region useRegionFromTo(String imageFrom, String imageTo)
 			throws FindFailed {
 		int x, y, w, h;
 		Region r;
 		Pattern p;
 		r = new Region(_lastRegion);
 		p = new Pattern(imageFrom);
 		Match mFrom = _lastRegion.find(p);
 		p = new Pattern(imageTo);
 		Match mTo = _lastRegion.find(p);
 		x = Math.min(mFrom.getX(), mTo.getX());
 		y = Math.min(mFrom.getY(), mTo.getY());
 		w = Math.max(mFrom.getX() + mFrom.getW(), mTo.getX() + mTo.getW())-x;
 		h = Math.max(mFrom.getY() + mFrom.getH(), mTo.getY() + mTo.getH())-y;
 		r = new Region(x, y, w, h);
 		return useRegion(r);
 	}
 	/**
 	 * <pre>
 	 * Will not execute any action below "onError" method to the end of table unless test failed.
 	 * Common usage - add to tear down page to restart an application in case of error.
 	 * Failed test will remain failed but further tests may have chance to use recovered environment.
 	 * </pre>
 	 */
 	public void onError() {
 		int errorCount = 0;
 		Counts cnt = super.getRuntimeContext().getTestResults().getCounts();
 		errorCount += cnt.wrong;
 		errorCount += cnt.exceptions;
 		// if there is no error - abandon test
 		if (errorCount == 0) {
 			abandon();
 		}
 		;
 
 	};
 
 	/**
 	 * Captures screenshot into a temporary file, logs and returns filename.
 	 * 
 	 * @return PNG filename in temporary folder.
 	 */
 	public String getScreenshot() {
 		String log, from;
 		try {
 			from = captureRegion(_lastRegion.getScreen()); // current file. will
 															// be deleted by
 															// sikuli engine
 															// upon completion
 			log = from.replace("sikuli", "screenshot");
 			FileManager.xcopy(from, log, null);
 			Debug.info(log);
 			return log;
 		} catch (Exception e) {
 			e.printStackTrace();
 			log = "Failed to capture screenshot.";
 		}
 		return log;
 	};
 
 	/**
 	 * Waits for match to appear in the app window;
 	 * 
 	 * @param imageFile
 	 *            - string to file
 	 * @param title
 	 *            - Window title
 	 * @return Match
 	 */
 	public Match waitForMatchInWindow(String imageFile, String title) {
 		int i = 0;
 		while ((isWindowPresent(title) == false)
 				&& (isMatchPresent(imageFile) == false) && (i++ < maxTimeout)) {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		useApplication(title);
 		useMatch(imageFile);
 		return _lastMatch;
 	}
 
 	/**
 	 * If parameter not true then table execution will be stopped. Test can
 	 * continue with another table.
 	 * 
 	 * @param boolean Criteria
 	 */
 	public Object onTrue(Boolean Criteria) {
 		Debug.info((Criteria) ? "Processed table" : "Skipped table");
 		if (Criteria != true) {
 			return new CommentFixture(true);
 		} else {
 			return this.SystemUnderTest;
 		}
 	};
 
 	/**
 	 * If Parameter not false then table execution will be stopped. Test can
 	 * continue with another table.
 	 * 
 	 * @param Criteria
 	 */
 	public Object onFalse(Boolean Criteria) {
 		return onTrue(!Criteria);
 	};
 
 	/**
 	 * Windows only isAppIdle. Returns true when CPU consumption by app goes
 	 * down to zero.
 	 * 
 	 * @return true if CPU usage is zero, false otherwise. Returns NULL is there is no such process.
 	 */
 	public Boolean isProcessIdle(String Process) throws Exception {
 		String cmd = "typeperf.exe \"\\Process(*" + Process
 				+ "*)\\% Processor Time\" -sc 1";
 		Runtime r = Runtime.getRuntime();
 		Boolean result = null;
 		Process p;
 		String out = "";
 		p = r.exec(cmd);
 		BufferedReader is = new BufferedReader(new InputStreamReader(
 				p.getInputStream()));
 		String line;
 		while ((line = is.readLine()) != null) {
 			out += line;
 		}
 		;
 		Debug.info(out);
 		p.waitFor(); // wait for process to complete
 		if (out.contains("\"0.000")) {
 			result = true;
 		}else{
 			result = false;
 		}
 		return result;
 	}
 
 	/**
 	 * Wait for process to use 0 CPU
 	 * 
 	 * @param Process
 	 * @throws Exception
 	 */
 	public void waitForProcessIdle(String Process) throws Exception {
 		int i = 0;
 		while ((isProcessIdle(Process) == false) && (i++ < maxTimeout)) {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
