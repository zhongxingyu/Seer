 
 /*
  *    SSW-file parser: Prints mech summaries
  *    Copyright (C) 2012  Christer Nyfält
  *
  *    This program is free software; you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation; either version 2 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License along
  *    with this program; if not, write to the Free Software Foundation, Inc.,
  *    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 
 public class Util {
 
 	public static double ceil_05(double value)
 	{
 		value *= 2.0;
 		value = Math.ceil(value);
 		value /= 2.0;
 		return value;
 	}
 
 	public static String getTagValue(String sTag, Element eElement) {
 		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
 
 		Node nValue = nlList.item(0);
 
 		return nValue.getNodeValue();
 	}
 
	public static Node getChild(Element parent, String name)
	{
		return parent.getElementsByTagName(name).item(0);
	}
 }
 
 
