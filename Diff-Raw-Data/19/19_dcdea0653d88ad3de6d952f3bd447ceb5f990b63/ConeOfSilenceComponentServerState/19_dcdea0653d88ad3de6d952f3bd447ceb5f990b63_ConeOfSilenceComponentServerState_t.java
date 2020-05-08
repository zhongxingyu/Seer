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
 package org.jdesktop.wonderland.modules.audiomanager.common;
 
 import java.util.ResourceBundle;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
 import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
 
 /**
  * The ConeOfSilenceCellServerState class is the cell that renders a coneofsilence cell in
  * world.
  * 
  * @author jprovino
  * @author Ronny Standtke <ronny.standtke@fhnw.ch>
  */
 @XmlRootElement(name = "cone-of-silence-component")
 @ServerState
 public class ConeOfSilenceComponentServerState
         extends CellComponentServerState {
 
     private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
             "org/jdesktop/wonderland/modules/audiomanager/common/Bundle");
     @XmlElement(name = "name")
     private String name = BUNDLE.getString("ConeOfSilence");
    @XmlElement(name = "useCellBounds")
    private boolean useCellBounds = true;
     @XmlElement(name = "fullVolumeRadius")
     private double fullVolumeRadius = 1.5;
     @XmlElement(name = "outsideAudioVolume")
     private double outsideAudioVolume = 0;
 
     /** Default constructor */
     public ConeOfSilenceComponentServerState() {
     }
 
     public ConeOfSilenceComponentServerState(
            String name, boolean useCellBounds, double fullVolumeRadius) {
         this.name = name;
	this.useCellBounds = useCellBounds;
         this.fullVolumeRadius = fullVolumeRadius;
     }
 
     public String getServerComponentClassName() {
         return "org.jdesktop.wonderland.modules.audiomanager." +
                 "server.ConeOfSilenceComponentMO";
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @XmlTransient
     public String getName() {
         return name;
     }
 
    public void setUseCellBounds(boolean useCellBounds) {
	this.useCellBounds = useCellBounds;
    }

    @XmlTransient
    public boolean getUseCellBounds() {
	return useCellBounds;
    }

     public void setFullVolumeRadius(double fullVolumeRadius) {
         this.fullVolumeRadius = fullVolumeRadius;
     }
 
     @XmlTransient
     public double getFullVolumeRadius() {
         return fullVolumeRadius;
     }
 
     public void setOutsideAudioVolume(double outsideAudioVolume) {
         this.outsideAudioVolume = outsideAudioVolume;
     }
 
     @XmlTransient
     public double getOutsideAudioVolume() {
         return outsideAudioVolume;
     }

 }
