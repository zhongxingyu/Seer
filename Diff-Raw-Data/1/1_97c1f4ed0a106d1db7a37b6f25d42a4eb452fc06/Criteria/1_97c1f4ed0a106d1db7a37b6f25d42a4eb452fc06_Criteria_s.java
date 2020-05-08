 /* Copyright 2009 Yves Dubromelle, Thamer Louati @ LSIS(www.lsis.org)
  * 
  * This file is part of GenericANP.
  * 
  * GenericANP is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GenericANP is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with GenericANP.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.taeradan.ahp;
 
 import Jama.Matrix;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdom.Element;
import org.taeradan.ahp.Root;
 /**
  * This class represents the criterias of the AHP tree, it contains Indicators and it executes its part of the AHP algorithm.
  * @author Yves Dubromelle
  */
 public class Criteria {
 //	AHP static attributes
 	private String id;
 	private String name;
 	private PreferenceMatrix matrixIndInd;
         private DependanceMatrix matrixCrCr;
 	private PriorityVector vectorIndCr;
         private PriorityVector vectorCrCr;
 	private ArrayList<Indicator> indicators;
 //	AHP dynamic attributes
 	private PriorityVector vectorAltCr;
 	private Matrix matrixAltInd;
 	private ArrayList alternatives;
         private Root root;
 	/**
 	 * Class default constructor
 	 */
 	public Criteria() {
 		id = new String();
 		name = new String();
 		matrixIndInd = new PreferenceMatrix();
                 matrixCrCr = new DependanceMatrix();
 		indicators = new ArrayList<Indicator>();
 	}
 
 	/**
 	 * Simple constructor to initialize a criteria by its ID, name and preference matrix
 	 * @param id The criteria's ID
 	 * @param name The criteria's name
 	 * @param matrixIndInd The criteria's preference matrix
 	 */
 	public Criteria(String id, String name, PreferenceMatrix matrixInd, DependanceMatrix matrixCr) {
 		this.id = id;
 		this.name = name;
 		this.matrixIndInd = matrixInd;
                 this.matrixCrCr = matrixCr;
 	}
 
 	/**
 	 * Creates a AHP Criteria from a JDOM Element
 	 * @param xmlCriteria
 	 */
 	public Criteria(Element xmlCriteria) {
 //		Initialisation of the id of the criteria
 		id = xmlCriteria.getAttributeValue("id");
 //		System.out.println("\tCriteria.id="+id);
 		
 //		Initialisation of the name
 		name = xmlCriteria.getChildText("name");
 //		System.out.println("\tCriteria.name="+name);
 		
 //		Initialisation of the preference matrix
 		Element xmlPrefMatrix = xmlCriteria.getChild("prefmatrix");
 		matrixIndInd = new PreferenceMatrix(xmlPrefMatrix);
 //		System.out.println("\tCriteria.matrixIndInd="+matrixIndInd);
 		vectorIndCr = new PriorityVector(matrixIndInd);
 		
 //              Initialisation of the dependance matrix
                 Element xmlDepMatrix = xmlCriteria.getChild("depmatrix");
 		matrixCrCr = new DependanceMatrix(xmlDepMatrix);
 //		System.out.println("\tCriteria.matrixIndInd="+matrixIndInd);
 		vectorCrCr = new PriorityVector(matrixCrCr);
                 
 //		Consistency verification for the Perf Matrix
 		if(!ConsistencyChecker.isConsistent(matrixIndInd, vectorIndCr)){
 			System.err.println("Is not consistent (criteria "+id+")");
 		}
 		
 //              Consistency verification for the dependance Matrix
 		if(!ConsistencyChecker.isConsistent(matrixCrCr, vectorCrCr)){
 			System.err.println("Is not consistent (criteria "+id+")");
 		}
 //		Initialisation of the Indicators
 		List<Element> xmlIndicatorsList = xmlCriteria.getChildren("indicator");
 		List<Element> xmlRowsList = xmlPrefMatrix.getChildren("row");
 		indicators = new ArrayList<Indicator>(xmlIndicatorsList.size());
 //		Verification that the number of indicators matches the size of the matrix
 		if(xmlIndicatorsList.size()!=xmlRowsList.size()){
 			System.err.println("Error : the number of Indicators and the size of the preference matrix does not match !");
 		}
 //		For each indicator declared in the configuration file
 		for(int i=0; i<xmlIndicatorsList.size(); i++){
 				Element xmlIndicator = xmlIndicatorsList.get(i);
 //				System.out.println("\tCriteria.xmlIndicator="+xmlIndicator);
 //				System.out.println("\tCriteria.xmlIndicator.attValue="+xmlIndicator.getAttributeValue("id"));
 				String indName = "org.taeradan.ahp.ind.Indicator" + xmlIndicator.getAttributeValue("id");
 				try {
 //					Research of the class implementing the indicator , named "org.taeradan.ahp.ind.IndicatorCxIy"
 					Class indClass = Class.forName(indName);
 //					System.out.println("\t\tCriteria.indClass="+indClass);
 //					Extraction of its constructor
 					Constructor<Indicator> indConstruct = indClass.getConstructor(Element.class);
 //					System.out.println("\t\tCriteria.indConstruct="+indConstruct);
 //					Instanciation of the indicator with its constructor
 					indicators.add(indConstruct.newInstance(xmlIndicator));
 					
 //					System.out.println("\tCriteria.indicator="+indicators.get(i));
 				} catch (NoSuchMethodException e) {
 					System.err.println("Error : no such constructor :" + e);
 				} catch (SecurityException e) {
 					System.err.println("Error :" + e);
 				} catch (ClassNotFoundException e) {
 					System.err.println("Error : class " + indName + " not found :" + e);
 				} catch (InstantiationException e) {
 					System.err.println("Error :" + e);
 				} catch (IllegalAccessException e) {
 					System.err.println("Error :" + e);
 				} catch (IllegalArgumentException e) {
 					System.err.println("Error :" + e);
 				} catch (InvocationTargetException e) {
 					System.err.println("Error :" + e);
 				}
 		}
 	}
 	
 	public PriorityVector calculateAlternativesPriorityVector(ArrayList alts){
 		alternatives = alts;
 		matrixAltInd = new Matrix(alternatives.size(), indicators.size());
 //		Concatenation of the indicators' alternatives vectors
 		for(int i=0; i<indicators.size(); i++){
 			try {
 				Method calculateValue = indicators.get(i).getClass().getMethod("calculateAlternativeValue", int.class, ArrayList.class);
 				matrixAltInd.setMatrix(0, alternatives.size() - 1, i, i, indicators.get(i).calculateAlternativesPriorityVector(alternatives).getVector());
 			} catch (NoSuchMethodException ex) {
 				Logger.getLogger(Criteria.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (SecurityException ex) {
 				Logger.getLogger(Criteria.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 //		Calculation of the criteria's alternatives vector
 		vectorAltCr = new PriorityVector();
 		vectorAltCr.setVector(matrixAltInd.times(vectorIndCr.getVector()));
 		return vectorAltCr;
 	}
 	
 	/**
 	 * Returns a string describing the criteria, but not its children
 	 * @return Criteria as a String
 	 */
 	@Override
 	public String toString() {
 		String string = "Criteria "+id+" : "+name;
 		return string;
 	}
 	
 	/**
 	 * Returns a string describing the criteria and all its children
 	 * @return Criteria and children as a String
 	 */
 	public String toStringRecursive(){
 		String string = this.toString();
 		//string = string.concat("\n"+matrixIndInd.toString("\t"));
 		//string = string.concat("\n\n"+matrixCrCr.toString("\t"));
                 
                 string = string.concat("\n\n"+PreferenceMatrix.toString(vectorIndCr.getVector(), "\t"));
                 string = string.concat("\n\n"+PreferenceMatrix.toString(vectorCrCr.getVector(), "\t"));
                 //root.supermatrix.setMatrix(arg0, arg1, arg2, arg3, matrixAltInd)
 		DecimalFormat printFormat = new DecimalFormat("0.000");
 		for(int i=0; i<indicators.size(); i++){
 			string = string.concat("\n\t\t("+printFormat.format(vectorIndCr.getVector().get(i, 0))+") "+indicators.get(i).toStringRecursive());
 		}
 		return string;
 	}
 	
 	/**
 	 * Returns a JDOM element that represents the criteria and all its children
 	 * @return JDOM Element representing the criteria and children
 	 */
 	public Element toXml(){
 		Element xmlCriteria = new Element("criteria");
 		xmlCriteria.setAttribute("id", id);
 		xmlCriteria.addContent(new Element("name").setText(name));
 		xmlCriteria.addContent(matrixIndInd.toXml());
 		for(int i=0; i<indicators.size(); i++)
 			xmlCriteria.addContent(indicators.get(i).toXml());
 		return xmlCriteria;
 	}
 	
 	public String resultToString(){
 		String string = this.toString();
 		for(int i=0; i<indicators.size(); i++){
 			string = string.concat("\n\t\t"+indicators.get(i).resultToString());
 		}
 		string = string.concat("\n\tvectorAltCr=\n"+PreferenceMatrix.toString(vectorAltCr.getVector(),"\t"));
 		return string;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public PreferenceMatrix getMatrixInd() {
 		return matrixIndInd;
 	}
 
 	public void setMatrixInd(PreferenceMatrix matrixInd) {
 		this.matrixIndInd = matrixInd;
 	}
 
         public DependanceMatrix getMatrixCr() {
 		return matrixCrCr;
 	}
         public PriorityVector getVectorCr() {
 		return vectorCrCr;
 	}
         public PriorityVector getVectorIndCr() {
 		return vectorIndCr;
 	}
         public void setMatrixCr(DependanceMatrix matrixCr) {
 		this.matrixCrCr = matrixCr;
 	}
         
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public ArrayList<Indicator> getIndicators() {
 		return indicators;
 	}
 }
