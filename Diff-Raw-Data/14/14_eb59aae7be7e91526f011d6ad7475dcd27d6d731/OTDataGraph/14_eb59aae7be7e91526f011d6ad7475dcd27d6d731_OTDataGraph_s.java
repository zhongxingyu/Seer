 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
 * $Revision: 1.10 $
 * $Date: 2007-09-17 17:44:01 $
  * $Author: imoncada $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.datagraph.state;
 
 import org.concord.framework.otrunk.OTObjectInterface;
 import org.concord.framework.otrunk.OTObjectList;
 
 
 /**
  * PfDataGraph
  * Class name and description
  *
  * Date created: Oct 27, 2004
  *
  * @author scott<p>
  *
  */
 public interface OTDataGraph extends OTObjectInterface
 {
     /**
      * The graphables are cloned when a new data store is 
      * added to the graph.  This could happen when the new button
      * is pressed to create a spot to store newly collected data
      * or this could happen when data is imported into the graph. 
      * @return
      */
     public OTObjectList getPrototypeGraphables();
 
     public OTObjectList getGraphables();
 	
 	public OTObjectList getLabels();
 	
 	public OTDataAxis getYDataAxis();
 
 	public OTDataAxis getXDataAxis();
 	
 	public boolean getShowToolbar();
     
 	/** This determines whether the list of graphables in shown to the left of the graph */
	public final static boolean DEFAULT_showGraphableList = true;
 	public boolean getShowGraphableList();
 }
