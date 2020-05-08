 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi.controller.converter;
 
 import br.octahedron.cotopaxi.controller.Converter;
 
 /**
  * A simple String converter that stripes HTML code.
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
 *
  */
 public class SafeStringConverter implements Converter<String> {
 
	/* (non-Javadoc)
 	 * @see br.octahedron.cotopaxi.controller.Converter#convert(java.lang.String)
 	 */
 	@Override
 	public String convert(String input) {
		if (input != null) {
			// TODO poor implementation, improve it.
			return input.replaceAll("\\<.*?\\>", "");
 		}
 		return "";
 	}
 
 }
