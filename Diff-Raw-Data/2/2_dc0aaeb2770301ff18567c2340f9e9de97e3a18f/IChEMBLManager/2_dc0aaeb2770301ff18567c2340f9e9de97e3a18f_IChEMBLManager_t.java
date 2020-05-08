 /*******************************************************************************
  * Copyright (c) 2010  Egon Willighagen <egonw@users.sf.net>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contact: http://www.bioclipse.net/
  ******************************************************************************/
 package net.bioclipse.chembl.business;
 
 import java.util.List;
 import java.util.Map;
 
 import net.bioclipse.core.PublishedClass;
 import net.bioclipse.core.PublishedMethod;
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.managers.business.IBioclipseManager;
 
 @PublishedClass(
     value="Manager to interact with ChEMBL."
 )
 public interface IChEMBLManager extends IBioclipseManager {
 
 	@PublishedMethod(
		params="Integer targetID, String activity",
 		methodSummary="Downloads the QSAR data for a certain target" +
 			"and activity. Automatically removes entries with NaN values."
 	)
 	public Map<String, Double> getQSARData(Integer targetID, String activity)
 	throws BioclipseException;
 
 	@PublishedMethod(
 		params="Integer targetID",
 		methodSummary="Returns the available activities."
 	)
 	public List<String> getActivities(Integer targetID)
 	throws BioclipseException;
 
 }
