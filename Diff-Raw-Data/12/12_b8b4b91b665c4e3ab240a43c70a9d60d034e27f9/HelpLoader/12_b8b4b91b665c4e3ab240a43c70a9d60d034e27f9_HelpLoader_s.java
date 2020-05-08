 package pleocmd.itfc.gui.help;
 
 import java.net.URL;
 
 public final class HelpLoader {
 
 	private HelpLoader() {
 		// hidden - just static methods
 	}
 
 	/**
 	 * Loads a help file and returns an URL to it.
 	 * 
 	 * @param name
 	 *            name of the help file without any path and optionally without
 	 *            extension it it is an HTML file
 	 * @return an URL to the file or to a special "help-missing" file if the
 	 *         specified name could not be found
 	 */
 	public static URL getHelp(final String name) {
 		if (name == null) return getMissingHelp();
		final URL url = HelpLoader.class.getResource(name.contains(".") ? name
				: name + ".html");
		if (url == null) return getMissingHelp();
 		return url;
 	}
 
 	/**
 	 * Checks whether a help file is available (so that {@link #getHelp(String)}
 	 * would return an URL other than {@link #getMissingHelp()}).
 	 * 
 	 * @param name
 	 *            name of the help file without any path and optionally without
 	 *            extension it it is an HTML file
 	 * @return true if help file exists
 	 */
 	public static boolean isHelpAvailable(final String name) {
 		if (name == null) return false;
 		return HelpLoader.class.getResource(name.contains(".") ? name : name
 				+ ".html") != null;
 	}
 
 	public static URL getMissingHelp() {
 		final URL url = HelpLoader.class.getResource("help-missing.html");
 		if (url == null)
 			throw new RuntimeException("Could not find 'help-missing' file");
 		return url;
 	}
 
 }
