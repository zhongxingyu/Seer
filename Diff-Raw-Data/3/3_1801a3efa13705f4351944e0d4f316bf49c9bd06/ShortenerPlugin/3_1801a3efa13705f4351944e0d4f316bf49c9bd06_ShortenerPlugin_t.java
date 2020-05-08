 /*
  * shortener - ShortenerPlugin.java - Copyright © 2010 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package plugin.shortener;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import freenet.clients.http.LinkEnabledCallback;
 import freenet.clients.http.ToadletContainer;
 import freenet.clients.http.ToadletContext;
 import freenet.l10n.PluginL10n;
 import freenet.l10n.BaseL10n.LANGUAGE;
 import freenet.pluginmanager.FredPlugin;
 import freenet.pluginmanager.FredPluginBaseL10n;
 import freenet.pluginmanager.FredPluginFCP;
 import freenet.pluginmanager.FredPluginL10n;
 import freenet.pluginmanager.FredPluginThreadless;
 import freenet.pluginmanager.PluginReplySender;
 import freenet.pluginmanager.PluginRespirator;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 
 /**
  * Main class of the shortener plugin.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class ShortenerPlugin implements FredPlugin, FredPluginFCP, FredPluginL10n, FredPluginBaseL10n, FredPluginThreadless {
 
 	/** L10n helper. */
 	public static PluginL10n l10n;
 
 	/** The node’s toadlet container. */
 	private ToadletContainer toadletContainer;
 
 	/** All toadlets. */
 	private Map<PageToadlet, String> pageToadlets = Collections.synchronizedMap(new HashMap<PageToadlet, String>());
 
 	/** The key shortener. */
 	private Shortener shortener;
 
 	//
 	// PRIVATE METHODS
 	//
 
 	/**
 	 * Registers all toadlets.
 	 */
 	private void registerToadlets() {
 		toadletContainer.getPageMaker().addNavigationCategory("/Shortener/", "Navigation.Menu.Name", "Navigation.Menu.Name.Tooltip", this);
 		for (Entry<PageToadlet, String> toadletEntry : pageToadlets.entrySet()) {
 			String menuName = toadletEntry.getKey().getMenuName();
 			if (menuName != null) {
 				toadletContainer.register(toadletEntry.getKey(), "Navigation.Menu.Name", "/Shortener/" + toadletEntry.getValue(), true, "Navigation.Menu.Item." + menuName + ".Name", "Navigation.Menu.Item." + menuName + ".Tooltip", false, new AlwaysEnabledCallback());
 			} else {
 				toadletContainer.register(toadletEntry.getKey(), null, "/Shortener/" + toadletEntry.getValue(), false, false);
 			}
 		}
 	}
 
 	/**
 	 * Unregisters all toadlets.
 	 */
 	private void unregisterToadlets() {
 		for (PageToadlet pageToadlet : pageToadlets.keySet()) {
 			toadletContainer.unregister(pageToadlet);
 		}
 		toadletContainer.getPageMaker().removeNavigationCategory("Navigation.Menu.Name");
 	}
 
 	//
 	// INTERFACE FredPlugin
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void runPlugin(PluginRespirator pluginRespirator) {
 		toadletContainer = pluginRespirator.getToadletContainer();
 
 		shortener = new Shortener(pluginRespirator.getNode().executor, pluginRespirator.getHLSimpleClient());
 
 		PageToadletFactory pageToadletFactory = new PageToadletFactory(pluginRespirator.getHLSimpleClient());
 		pageToadlets.put(pageToadletFactory.createPageToadlet(new IndexPage(shortener, toadletContainer.getFormPassword()), "Index"), "");
		pageToadlets.put(pageToadletFactory.createPageToadlet(new ShortenPage(shortener, toadletContainer.getFormPassword())), "Shorten");
		pageToadlets.put(pageToadletFactory.createPageToadlet(new ErrorPage.InvalidFormPasswordPage()), "InvalidFormPassword");
		pageToadlets.put(pageToadletFactory.createPageToadlet(new ErrorPage.InvalidKeyPage()), "InvalidKey");
 
 		registerToadlets();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void terminate() {
 		unregisterToadlets();
 	}
 
 	//
 	// INTERFACE FredPluginFCP
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void handle(PluginReplySender replySender, SimpleFieldSet parameters, Bucket data, int accessType) {
 		/* TODO - implements */
 	}
 
 	//
 	// INTERFACE FredPluginL10n
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getString(String key) {
 		return l10n.getBase().getString(key);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setLanguage(LANGUAGE newLanguage) {
 		ShortenerPlugin.l10n = new PluginL10n(this, newLanguage);
 	}
 
 	//
 	// INTERFACE FredPluginBaseL10n
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getL10nFilesBasePath() {
 		return "plugin/shortener/l10n/";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getL10nFilesMask() {
 		return "Shortener_${lang}.properties";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getL10nOverrideFilesMask() {
 		return "Shortener_${lang}.override.properties";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ClassLoader getPluginClassLoader() {
 		return ShortenerPlugin.class.getClassLoader();
 	}
 
 	/**
 	 * {@link LinkEnabledCallback} implementation that always returns {@code
 	 * true} when {@link LinkEnabledCallback#isEnabled(ToadletContext)} is
 	 * called.
 	 *
 	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 	 */
 	public class AlwaysEnabledCallback implements LinkEnabledCallback {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public boolean isEnabled(ToadletContext toadletContext) {
 			return true;
 		}
 	}
 
 }
