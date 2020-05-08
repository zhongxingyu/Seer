 /*
  * shortener - ShortenPage.java - Copyright © 2010 David Roden
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
 
 import java.net.MalformedURLException;
 
 /**
  * This page lists all keys that are currently being shortened.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class ShortenPage implements Page {
 
 	/** The key shortener. */
 	private final Shortener shortener;
 
 	/** The form password. */
 	private final String formPassword;
 
 	/**
 	 * Creates a new page that kicks off the shortening of a key.
 	 *
 	 * @param shortener
 	 *            The key shortener to use
 	 * @param formPassword
 	 *            The form password
 	 */
 	public ShortenPage(Shortener shortener, String formPassword) {
 		this.shortener = shortener;
 		this.formPassword = formPassword;
 	}
 
 	//
 	// INTERFACE Page
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getPath() {
		return "Shortening";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Response handleRequest(Request request) {
 		String formPassword = request.getHttpRequest().getParam("formPassword");
 		if (!formPassword.equals(this.formPassword)) {
 			return new RedirectResponse("/Shortener/InvalidFormPassword");
 		}
 		String keyString = request.getHttpRequest().getParam("key");
 		try {
 			shortener.shortenKey(keyString);
 		} catch (MalformedURLException mue1) {
 			return new RedirectResponse("/Shortener/InvalidKey");
 		}
 		return new RedirectResponse("/Shortener/");
 	}
 
 }
