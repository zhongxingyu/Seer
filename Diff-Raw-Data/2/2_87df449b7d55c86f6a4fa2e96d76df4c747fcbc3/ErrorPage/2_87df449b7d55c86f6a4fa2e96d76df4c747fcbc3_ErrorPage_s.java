 /*
  * shortener - ErrorPage.java - Copyright © 2010 David Roden
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
 
 import freenet.clients.http.PageMaker;
 import freenet.clients.http.PageNode;
 import freenet.clients.http.ToadletContext;
 import freenet.l10n.BaseL10n;
 import freenet.support.HTMLNode;
 
 /**
  * Base implementation of an error page.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class ErrorPage implements Page {
 
 	/** The name of the error. */
 	private final String errorName;
 
 	/**
 	 * Creates a new error page with the given name. The name is used to get
 	 * l10n display properties.
 	 *
 	 * @param errorName
 	 *            The name of the error
 	 */
 	public ErrorPage(String errorName) {
 		this.errorName = errorName;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getPath() {
 		return errorName;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Response handleRequest(Request request) {
 		ToadletContext toadletContext = request.getToadletContext();
 		PageMaker pageMaker = toadletContext.getPageMaker();
 		BaseL10n pluginL10n = ShortenerPlugin.l10n.getBase();
 		PageNode pageNode = pageMaker.getPageNode(pluginL10n.getString("Page." + errorName + ".Title"), toadletContext);
 
		HTMLNode errorBox = pageNode.content.addChild("div", "class", "infobox error");
 		errorBox.addChild("div", "class", "infobox-header", pluginL10n.getString("Page." + errorName + ".Title"));
 		errorBox.addChild("div", "class", "infobox-content", pluginL10n.getString("Page." + errorName + ".Text"));
 
 		return new Response(200, "OK", "text/html", pageNode.outer.generate());
 	}
 
 	/**
 	 * An error page that shows an “invalid form password” error.
 	 *
 	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 	 */
 	public static class InvalidFormPasswordPage extends ErrorPage {
 
 		/**
 		 * Creates a new “invalid form password” error page.
 		 */
 		public InvalidFormPasswordPage() {
 			super("InvalidFormPassword");
 		}
 
 	}
 
 	/**
 	 * An error page that shows an “invalid key” error.
 	 *
 	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 	 */
 	public static class InvalidKeyPage extends ErrorPage {
 
 		/**
 		 * Creates a new “invalid key” error page.
 		 */
 		public InvalidKeyPage() {
 			super("InvalidKey");
 		}
 
 	}
 
 }
