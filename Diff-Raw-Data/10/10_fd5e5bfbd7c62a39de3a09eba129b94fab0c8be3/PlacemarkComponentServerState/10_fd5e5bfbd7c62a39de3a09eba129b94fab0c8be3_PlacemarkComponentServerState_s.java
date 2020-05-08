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
 
 package org.jdesktop.wonderland.modules.placemarks.common;
 
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
 import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.common.utils.jaxb.QuaternionAdapter;
import org.jdesktop.wonderland.common.utils.jaxb.Vector3fAdapter;
 
 /**
  * Server state for placemark cell component
  *
  * @author Jonathan Kaplan <kaplanj@dev.java.net>
  */
@XmlRootElement(name="portal-component")
 @ServerState
 public class PlacemarkComponentServerState extends CellComponentServerState {
     /** the name of the placemark to create */
     private String placemarkName;
 
     /** Default constructor */
     public PlacemarkComponentServerState() {
     }
 
     public PlacemarkComponentServerState(String placemarkName) {
         this.placemarkName = placemarkName;
     }
 
     @Override
     public String getServerComponentClassName() {
         return "org.jdesktop.wonderland.modules.placemarks.server.PlacemarkComponentMO";
     }
 
     @XmlElement
     public String getPlacemarkName() {
         return placemarkName;
     }
 
     public void setPlacemarkName(String placemarkName) {
         this.placemarkName = placemarkName;
     }
 }
