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
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import freenet.clients.http.ToadletContainer;
 import freenet.pluginmanager.FredPlugin;
 import freenet.pluginmanager.FredPluginFCP;
 import freenet.pluginmanager.PluginReplySender;
 import freenet.pluginmanager.PluginRespirator;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 
 /**
  * Main class of the shortener plugin.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class ShortenerPlugin implements FredPlugin, FredPluginFCP {
 
 	/** The node’s toadlet container. */
 	private ToadletContainer toadletContainer;
 
 	/** All toadlets. */
 	private Map<PageToadlet, String> pageToadlets = new HashMap<PageToadlet, String>();
 
 	//
 	// PRIVATE METHODS
 	//
 
 	/**
 	 * Registers all toadlets.
 	 */
 	private void registerToadlets() {
 		for (Entry<PageToadlet, String> toadletEntry : pageToadlets.entrySet()) {
 			String menuName = toadletEntry.getKey().getMenuName();
 			if (menuName != null) {
				toadletContainer.register(toadletEntry.getKey(), menuName, "/Shortener/" + toadletEntry.getValue(), true, false);
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
 	}
 
 	//
 	// INTERFACE FredPlugin
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void runPlugin(PluginRespirator pluginRespirator) {
 		toadletContainer = pluginRespirator.getToadletContainer();
 
 		PageToadletFactory pageToadletFactory = new PageToadletFactory(pluginRespirator.getHLSimpleClient());
 		pageToadlets.put(pageToadletFactory.createPageToadlet(new IndexPage(), "Shorten Key"), "Index");
 
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
 
 }
