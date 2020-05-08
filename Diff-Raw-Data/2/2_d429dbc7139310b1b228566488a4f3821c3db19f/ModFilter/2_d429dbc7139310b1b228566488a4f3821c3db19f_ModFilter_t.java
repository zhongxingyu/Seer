 /*
  * utils - ModFilter.java - Copyright © 2011 David Roden
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
 
 package net.pterodactylus.util.template;
 
 import java.util.Map;
 
 import net.pterodactylus.util.number.Numbers;
 
 /**
 * The mod filter performs a modulo operation on the given number and the
  * parameter “divisor,” adding the optional parameter “offset” to the dividend,
  * and returns {@code true} if the module operation did not return a rest, i.e.
  * if {@code dividend + offset mod divisor} equals {@code 0}.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class ModFilter implements Filter {
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object format(TemplateContext templateContext, Object data, Map<String, Object> parameters) {
 		Long dividend = Numbers.safeParseLong(data);
 		Long divisor = Numbers.safeParseLong(parameters.get("divisor"));
 		long offset = Numbers.safeParseLong(parameters.get("offset"), 0L);
 		if ((dividend == null) || (divisor == null)) {
 			return false;
 		}
 		return ((dividend + offset) % divisor) == 0;
 	}
 
 }
