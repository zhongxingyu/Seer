 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.util;
 
 import static br.octahedron.cotopaxi.CotopaxiProperty.*;
 import br.octahedron.cotopaxi.CotopaxiProperty;
 import br.octahedron.figgo.modules.bank.data.BankAccount;
 
 /**
  * Some utilitary methods for Figgo.
  * 
  * @author Danilo Penna Queiroz
  */
 public class DomainUtil {
 	
	public static final String ADDRESS_SUFFIX = CotopaxiProperty.getProperty("APPLICATION_DOMAIN");
 
 	/**
 	 * Generates the {@link BankAccount} ID for the given domain.
 	 * 
 	 * @param domain
 	 *            The domain name.
 	 * @return The bank account id for the domain's account.
 	 */
 	public static String generateDomainUserID(String domain) {
 		return domain.trim() + ADDRESS_SUFFIX;
 	}
 	
 	/**
 	 * Gets the complete url for the given domain. Eg.: if the given domain is "octa" it should return
 	 * http://octa.figgo.com.br.
 	 * 
 	 * This method works for both testing and production environments
 	 * 
 	 * @param domain The domain 
 	 * @return The full url for the given domain
 	 */
 	public static String getDomainURL(String domain) {
 		String appURL = getProperty(APPLICATION_BASE_URL);
 		if (appURL.contains("www")) {
 			return appURL.replace("www", domain);
 		} else {
 			return appURL.replace("http://", "http://" + domain + ".");
 		}
 	}
 }
