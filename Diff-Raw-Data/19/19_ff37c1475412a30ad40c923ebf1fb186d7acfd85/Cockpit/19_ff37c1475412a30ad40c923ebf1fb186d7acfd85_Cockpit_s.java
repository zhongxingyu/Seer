 
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
 
 public class Cockpit implements Item {
 	String type;
 	String console;
 
 	Cockpit(Element sElement) {
		type = Util.getTagValue("type", sElement);

		// TODO: Fix command console
		//		el = (Element)nValue;
 
		//		console = el.getAttribute("commandconsole");
 		System.out.println("Cockpit Type : " + type);
 		System.out.println("Command Console : " + console);
 	}
 
 	public byte get_rules_level()
 	{
 		byte r_level;
 
 		if (type == "Standard Cockpit")
 			r_level = INTRO_TECH;
 		else if (type == "Small Cockpit")
 			r_level = TOURNAMENT_LEGAL;
 		else if (type == "Torso-Mounted Cockpit")
 			r_level = ADVANCED;
 		else if (type == "Primitive Cockpit")
 			r_level = PRIMITIVE;
 		else
 			{
 				System.out.println("Unknown cockpit:" + type);
 				System.exit(-1);
 				return -1;
 			}			
 		// Command console
 		// -- no experimental cockpits, so automatically ADVANCED
 
 		if (console == "TRUE")
 			r_level = ADVANCED;
 
 		return r_level;
 	}
 
 	public double get_weight()
 	{
 		double weight;
 
 		if (type == "Standard Cockpit")
 			weight = 3.0;
 		else if (type == "Small Cockpit")
 			weight = 2.0;
 		else if (type == "Torso-Mounted Cockpit")
 			weight = 4.0;
 		else if (type == "Primitive Cockpit")
 			weight = 5.0;
 		else
 			{
 				System.out.println("Unknown cockpit:" + type);
 				System.exit(-1);
 				weight = 0.0;
 			}			
 
 		// Command Console
 		if (console == "TRUE")
 			weight += 3.0;
 
 		return weight;
 	}
 
 	public long get_cost()
 	{
 		long cost;
 
 		if (type == "Standard Cockpit")
 			cost = 200000;
 		else if (type == "Small Cockpit")
 			cost = 175000;
 		else if (type == "Torso-Mounted Cockpit")
 			cost = 750000;
 		else if (type == "Primitive Cockpit")
 			cost = 100000;
 		else
 			{
 				System.out.println("Unknown cockpit:" + type);
 				System.exit(-1);
 				cost = 0;
 			}			
 
 		// Command Console
 		if (console == "TRUE")
 			cost += 750000;
 
 		return cost;
 	}
 }
 
 
