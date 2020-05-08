 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.microphone.common;
 
 
 import java.io.Serializable;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
 
 import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
 
 /**
  * The MicrophoneCellServerState class is the cell that renders a microphone cell in
  * world.
  * 
  * @author jprovino
  */
 @XmlRootElement(name="microphone-cell")
 @ServerState
 public class MicrophoneCellServerState extends CellServerState
         implements Serializable {
 
     @XmlElement(name="name")
     private String name;
 
     @XmlElement(name="fullVolumeArea")
     private FullVolumeArea fullVolumeArea;
 
     @XmlElement(name="activeArea")
     private ActiveArea activeArea;
     
     /** Default constructor */
     public MicrophoneCellServerState() {
     }
     
     public MicrophoneCellServerState(String name, FullVolumeArea fullVolumeArea,
 	    ActiveArea activeArea) {
 
 	this.name = name;
 	this.fullVolumeArea = fullVolumeArea;
 	this.activeArea = activeArea;
 
	//logger.finer("fva " + fullVolumeArea.areaType
	//    + " x " + fullVolumeArea.xExtent
	//    + " y " + fullVolumeArea.yExtent
	//    + " z " + fullVolumeArea.zExtent);

	//logger.finer("active " + activeArea.origin
	//    + " x " + activeArea.xExtent
	//    + " y " + activeArea.yExtent
	//    + " z " + activeArea.zExtent);
     }
 
     public String getServerClassName() {
         return "org.jdesktop.wonderland.modules.microphone.server.cell.MicrophoneCellMO";
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @XmlTransient
     public String getName() {
         return name;
     }
 
     public void setFullVolumeArea(FullVolumeArea fullVolumeArea) {
         this.fullVolumeArea = fullVolumeArea;
     }
 
     @XmlTransient
     public FullVolumeArea getFullVolumeArea() {
 	return fullVolumeArea;
     }
 
     public void setActiveArea(ActiveArea activeArea) {
         this.activeArea = activeArea;
     }
 
     @XmlTransient
     public ActiveArea getActiveArea() {
 	return activeArea;
     }
 
     public static class FullVolumeArea implements Serializable {
 
 	@XmlElement(name="areaType") public String areaType = "BOX";
 	@XmlElement(name="xExtent") public double xExtent = 0;
 	@XmlElement(name="yExtent") public double yExtent = 0;
 	@XmlElement(name="zExtent") public double zExtent = 0;
 
 	/** Default constructor */
 	public FullVolumeArea() {
 	}
 
 	public FullVolumeArea(String areaType, double xExtent,
 	        double yExtent, double zExtent) {
 
 	    this.areaType = areaType;
 	    this.xExtent = xExtent;
 	    this.yExtent = yExtent;
 	    this.zExtent = zExtent;
 	}
 
     }
 
     public static class ActiveArea implements Serializable {
 
 	@XmlElement(name="origin") public Origin origin;
 	@XmlElement(name="xExtent") public double xExtent;
 	@XmlElement(name="yExtent") public double yExtent;
 	@XmlElement(name="zExtent") public double zExtent;
 
 	/** Default constructor */
 	public ActiveArea() {
 	}
 
 	public ActiveArea (Origin origin, double xExtent,
                 double yExtent, double zExtent) {
 
 	    this.xExtent = xExtent;
             this.yExtent = yExtent;
             this.zExtent = zExtent;
         }
 
     } 
 
 }
