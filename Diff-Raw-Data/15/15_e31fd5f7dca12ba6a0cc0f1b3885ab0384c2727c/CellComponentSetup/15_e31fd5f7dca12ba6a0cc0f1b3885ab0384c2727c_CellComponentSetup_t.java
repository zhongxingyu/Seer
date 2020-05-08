 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 
 package org.jdesktop.wonderland.common.cell.setup;
 
import javax.xml.bind.annotation.XmlRootElement;

 /**
  * The CellComponentSetup class is the base class for the cell configuration
  * (setup) information for all cell components. Individual cell components may
  * be mixed into cells to give them added functionality: subclasses of this
  * class provide the necessary configuration information.
  * <p>
  * This mechanism used JAXB to serialize and deserialize objects to/from XML.
  * Each subclass of CellSetupComponent must be annotated with @XmlRootElement
  * which gives the name that encapsulates the component-specific XML setup
  * information. Within the root element, the subclass component is free to
  * design its own XML schema for its setup information, typically using the
  * @XmlElement and @XmlAttribute annotations.
  * <p>
  * Each subclass of CellComponentSetup must declare itself with the mechanism
  * annotated JAXB classes, by implementing the CellSetupAPI interface.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
@XmlRootElement(name="component")
public class CellComponentSetup {
 }
