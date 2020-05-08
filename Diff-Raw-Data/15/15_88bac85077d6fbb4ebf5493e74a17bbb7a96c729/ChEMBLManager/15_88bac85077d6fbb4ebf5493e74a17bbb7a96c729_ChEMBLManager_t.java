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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.managers.business.IBioclipseManager;
 import net.bioclipse.rdf.Activator;
 import net.bioclipse.rdf.business.IJavaRDFManager;
 import net.bioclipse.rdf.model.IStringMatrix;
 
 public class ChEMBLManager implements IBioclipseManager {
 
     private IJavaRDFManager rdf = Activator.getDefault().getJavaManager();
 
     private String CHEMBL_SPARQL_ENDPOINT =
     	"http://rdf.farmbio.uu.se/chembl/sparql/";
     
     /**
      * Gives a short one word name of the manager used as variable name when
      * scripting.
      */
     public String getManagerName() {
         return "chembl";
     }
 
 	public Map<String, Double> getQSARData(Integer targetID, String activity)
 	throws BioclipseException {
 		Map<String, Double> results = new HashMap<String, Double>();
 
 	    String sparql =
 	    	"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/> "+
 	    	"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
 	    	"SELECT DISTINCT ?smiles ?val " +
 	    	"WHERE { " +
 	    	"  ?act chembl:type \"" + activity + "\"; " +
 	    	"  chembl:onAssay ?ass; " +
 	    	"  chembl:standardValue ?val; " +
 	    	"  chembl:forMolecule ?mol. " +
 	    	" ?mol bo:smiles ?smiles. " +
 	    	" ?ass chembl:hasTarget " + 
 	    	"<http://rdf.farmbio.uu.se/chembl/target/t" + targetID + ">. " +
 	    	"}";
 
 	    IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
 	    for (int i=0; i<matrix.getRowCount(); i++) {
 	    	try {
	    		String smiles = matrix.get(i, "smiles");
	    		if (smiles != null && smiles.length() != 0) {
	    			Double value = Double.valueOf(matrix.get(i, "val"));
	    			if (value != Double.NaN)
	    				results.put(matrix.get(i, "smiles"), value);
	    		}
 	    	} catch (NumberFormatException exception) {
 	    		results.put(matrix.get(i, "smiles"), Double.NaN);
 	    	}
 	    }
 
 	    return results;
 	}
 
 	public List<String> getActivities(Integer targetID)
 	throws BioclipseException {
 		List<String> activities = new ArrayList<String>();
 		
 	    String sparql =
 	    	"PREFIX blueobelisk: <http://www.blueobelisk.org/chemistryblogs/> "+
 	    	"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
 	    	"SELECT DISTINCT ?act " +
 	    	"WHERE { " +
 	    	" ?activ chembl:type ?act; " +
 	    	"    chembl:onAssay ?ass. " +
 	    	" ?ass chembl:hasTarget " + 
 	    	"    <http://rdf.farmbio.uu.se/chembl/target/t" + targetID + ">. " +
 	    	"}";
 
 	    IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
 	    if (matrix.getRowCount() > 0)
 	    	activities.addAll(matrix.getColumn("act"));
 	    
 	    return activities;
 	}
 
 }
