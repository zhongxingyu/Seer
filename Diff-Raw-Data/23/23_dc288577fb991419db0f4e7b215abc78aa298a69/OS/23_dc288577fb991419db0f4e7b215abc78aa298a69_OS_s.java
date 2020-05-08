 package com.waitwha.nmap;
 
 import java.util.ArrayList;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * <b>NmapTools</b>: OS<br/>
  * <small>Copyright (c)2013 Mike Duncan &lt;<a href="mailto:mike.duncan@waitwha.com">mike.duncan@waitwha.com</a>&gt;</small><p />
  *
  * <pre>
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  * </pre>
  *
  * Represents a OS element which is included when nmap detects a 
  * OS using ports found during the scan.
  *
  * @author Mike Duncan <mike.duncan@waitwha.com>
  * @version $Id$
  * @package com.waitwha.nmap
  */
 public class OS {
 
 	/**
 	 * Ports used during the os matching process.
 	 * 
 	 */
 	public class PortUsed  {
 		
 		private String state;
 		private String proto;
 		private int portId;
 		
 		public PortUsed(Element portused)  {
 			this.state = portused.getAttribute("state");
 			this.proto = portused.getAttribute("proto");
 			this.portId = 0;
 			try  {
 				this.portId = Integer.parseInt(portused.getAttribute("portid"));
 			}catch(NumberFormatException e) {}
 		}
 
 		/**
 		 * @return the state
 		 */
 		public String getState() {
 			return state;
 		}
 
 		/**
 		 * @return the proto
 		 */
 		public String getProto() {
 			return proto;
 		}
 
 		/**
 		 * @return the portId
 		 */
 		public int getPortId() {
 			return portId;
 		}
 		
 	}
 	
 	/**
 	 * OsClass element within a OsMatch element.
 	 * 
 	 */
 	public class OsClass extends ArrayList<String>  {
 		
 		private static final long serialVersionUID = 1L;
 		private String type;
 		private String vendor;
 		private String osFamily;
 		private String osGen;
 		private int accuracy;
 		
 		public OsClass(Element osclass)  {
 			super();
 			this.type = osclass.getAttribute("type");
 			this.vendor = osclass.getAttribute("vendor");
 			this.osFamily = osclass.getAttribute("osfamily");
 			this.osGen = osclass.getAttribute("osgen");
 			this.accuracy = Integer.parseInt(osclass.getAttribute("accuracy"));
 			
 			NodeList cpes = osclass.getElementsByTagName("cpe");
 			for(int i = 0; i < cpes.getLength(); i++)
 				this.add(((Element)cpes.item(i)).getTextContent());
 			
 		}
 
 		/**
 		 * @return the type
 		 */
 		public String getType() {
 			return type;
 		}
 
 		/**
 		 * @return the vendor
 		 */
 		public String getVendor() {
 			return vendor;
 		}
 
 		/**
 		 * @return the osFamily
 		 */
 		public String getOsFamily() {
 			return osFamily;
 		}
 
 		/**
 		 * @return the osGen
 		 */
 		public String getOsGen() {
 			return osGen;
 		}
 
 		/**
 		 * @return the accuracy
 		 */
 		public int getAccuracy() {
 			return accuracy;
 		}
 		
 	}
 	
 	/**
 	 * OsMatch element which represents information about the OS for this
 	 * host.
 	 * 
 	 */
 	public class OsMatch extends ArrayList<OsClass>  {
 
 		private static final long serialVersionUID = 1L;
 		private String name;
 		private int accuracy;
 		private int line;
 		
 		public OsMatch(Element osmatch)  {
 			super();
 			this.name = osmatch.getAttribute("name");
			this.accuracy = Integer.parseInt(osmatch.getAttribute("accuracy"));
			this.line = Integer.parseInt(osmatch.getAttribute("line"));
 			
 			NodeList osclasses = osmatch.getElementsByTagName("osclass");
 			for(int i = 0; i < osclasses.getLength(); i++)
 				this.add(new OsClass((Element)osclasses.item(i)));
 			
 		}
 
 		/**
 		 * @return the name
 		 */
 		public String getName() {
 			return name;
 		}
 
 		/**
 		 * @return the accuracy
 		 */
 		public int getAccuracy() {
 			return accuracy;
 		}
 
 		/**
 		 * @return the line
 		 */
 		public int getLine() {
 			return line;
 		}
 		
 	}
 	
 	private ArrayList<PortUsed> portsUsed;
 	private ArrayList<OsMatch> osMatches;
 	
 	public OS(Element os)  {
 		this.portsUsed = new ArrayList<PortUsed>();
 		this.osMatches = new ArrayList<OsMatch>();
 		
 		NodeList portsused = os.getElementsByTagName("osmatch");
 		for(int i = 0; i < portsused.getLength(); i++)
 			this.portsUsed.add(new PortUsed((Element)portsused.item(i)));
 		
 		NodeList osmatches = os.getElementsByTagName("osmatch");
 		for(int i = 0; i < osmatches.getLength(); i++)
 			this.osMatches.add(new OsMatch((Element)osmatches.item(i)));
 		
 	}
 
 	/**
 	 * @return the portsUsed
 	 */
 	public ArrayList<PortUsed> getPortsUsed() {
 		return portsUsed;
 	}
 
 	/**
 	 * @return the osMatches
 	 */
 	public ArrayList<OsMatch> getOsMatches() {
 		return osMatches;
 	}
 	
 	@Override
 	public String toString()  {
 		if(this.osMatches.size() == 0)
 			return "unknown";
 		
 		OsMatch bestMatch = this.osMatches.get(0); 
 		for(OsMatch match : this.osMatches)  {
 			if(match.getAccuracy() > bestMatch.getAccuracy())
 				bestMatch = match;
 			
 		}
 		
 		return bestMatch.getName();
 	}
 	
 }
