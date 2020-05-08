 /**
  * PromniCAT - Collection and Analysis of Business Process Models
  * Copyright (C) 2012 Cindy Fähnrich, Tobias Hoppe, Andrina Mascher
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.uni_potsdam.hpi.bpt.promnicat.util;
 
 import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Representation;
 import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.UnitChain;
 import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.UnitChainBuilder;
 
 /**
  * Class containing all necessary constants regarding the configuration of the {@link UnitChain} via the
  * {@link UnitChainBuilder}.
  * @author Tobias Hoppe, Andrina Mascher, Cindy Fähnrich
  */
 public interface Constants {
 	
 	/**
 	 * Default path for configuration file
 	 */
 	public static final String DEFAULT_CONFIG_PATH = "configuration.properties";
 	
 	/**
 	 * Default path for test database configuration file
 	 */
 	public static final String TEST_DB_CONFIG_PATH = "test/testConfig.properties";
 	
 	/**
 	 * Configuration file property for maximum number of threads used in {@link UnitChain} execution.
 	 */
 	public static final String MAX_NUMBER_OF_THREADS = "maxNumberOfThreads";
 	
 	/**
 	 * Configuration file property for database path.
 	 */
 	public static final String DB_Path = "db.dbPath";
 	
 	/**
 	 * Configuration file property for database user name.
 	 */
 	public static final String DB_USER = "db.user";
 	
 	/**
 	 * Configuration file property for database user password.
 	 */
 	public static final String DB_PASSWD = "db.password";
 	
 	/**
 	 * Database Orient DB
 	 */
 	public static final String ORIENT_DB = "OrientDb";
 	
 	/**
 	 * Name used for Business Process Model Academic Initiative.
 	 */
 	public static final String ORIGIN_BPMAI = "BPMAI";
 	
 	/**
 	 * Name used for so called 'National Process Library'.
 	 */
 	public static final String ORIGIN_NPB = "NPB";
 	
 	/**
 	 * Name used for SAP reference model.
 	 */
 	public static final String ORIGIN_SAP_RM = "SAP_RM";
 	
 	/**
 	 * Name used for AOK model collection.
 	 */
 	public static final String ORIGIN_AOK = "AOK";
 	
 	/**
 	 * Name used for IBM model collection.
 	 */
 	public static final String ORIGIN_IBM = "IBM";
 	
 	/**
 	 * Name used for SVG format.
 	 */
 	public static final String FORMAT_SVG = "Svg";
 	
 	/**
 	 * Name used for BPM AI JSON format.
 	 */
 	public static final String FORMAT_BPMAI_JSON = "BpmaiJson";
 	
 	/**
 	 * Name used for XML format.
 	 */
 	public static final String FORMAT_XML = "XML";
 	 * Name used for PNML format.
 	 */
 	public static final String FORMAT_PNML = "Pnml";
 	/**
 	 * Name used for EPC notation.
 	 */
 	public static final String NOTATION_EPC = "Epc";
 	
 	/**
 	 * Name used for PetriNet notation.
 	 */
 	public static final String NOTATION_PETRINET = "PetriNet";
 	
 	/**
 	 * Name used for Colored-PetriNet notation.
 	 */
 	public static final String NOTATION_COLORED_PETRINET = "ColoredPetriNet";
 	
 	/**
 	 * Name used for Processmap notation.
 	 */
 	public static final String NOTATION_PROCESSMAP = "Processmap";
 	
 	/**
 	 * Name used for Organigram notation.
 	 */
 	public static final String NOTATION_ORGANIGRAM = "Organigram";
 	
 	/**
 	 * Name used for BPMN 1.0 notation.
 	 */
 	public static final String NOTATION_BPMN1_1 = "Bpmn1.1";
 	
 	/**
 	 * Name used for BPMN 2.0 notation.
 	 */
 	public static final String NOTATION_BPMN2_0 = "Bpmn2.0";
 	
 	/**
 	 * Name used for BPMN 2.0 conversation notation.
 	 */
 	public static final String NOTATION_BPMN2_0_CONVERSATION = "Bpmn2.0conversation";
 	
 	/**
 	 * Name used for BPMN 2.0 choreography notation.
 	 */
 	public static final String NOTATION_BPMN2_0_CHOREOGRAPHY = "Bpmn2.0choreography";
 
 	/**
 	 * Name used for jbpm4 notation.
 	 */
 	public static final String NOTATION_JBPM4 = "jbpm4";
 	
 	/**
 	 * Name used for UML 2.2 class notation.
 	 */
 	public static final String NOTATION_UML2_2_CLASS = "UML2.2Class";
 	
 	/**
 	 * Name used for Xforms notation.
 	 */
 	public static final String NOTATION_XFORMS = "xforms";
 	
 	/**
 	 * The namespace attribute string of the XML-files from the 'National Process Library'
 	 */
 	public static final String NPB_XML_NAMESPACE = "http://prozessbibliothek.de/XProzess/Schema/0.1";
 
 	/**
 	 * An enumeration of all available process model repositories
 	 * (BPM Academic Initiative, National Process Library, SAP Reference Model, AOK Models).
 	 */
 	public enum ORIGINS{
 		BPMAI(Constants.ORIGIN_BPMAI),
 		NPB(Constants.ORIGIN_NPB),
 		SAP_RM(Constants.ORIGIN_SAP_RM),
 		AOK(Constants.ORIGIN_AOK);
 		
 		private String description;
 
 		ORIGINS(String description) {
 	        this.description = description;
 	    }
 
 	    public String toString() {
 	         return description;
 	    }
 	}
 	
 	/**
 	 * An enumeration of all available notation formats (e.g. EPC, BPMN [Version 1.1 and 2.0]).
 	 */
 	public enum NOTATIONS{
 		BPMN2_0(Constants.NOTATION_BPMN2_0),
 		BPMN1_1(Constants.NOTATION_BPMN1_1),
 		BPMN2_0CHOREOGRAPHY(Constants.NOTATION_BPMN2_0_CHOREOGRAPHY),
 		BPMN2_0CONVERSATION(Constants.NOTATION_BPMN2_0_CONVERSATION),
 		EPC(Constants.NOTATION_EPC),
 		PETRINET(Constants.NOTATION_PETRINET),
 		COLORED_PETRINET(Constants.NOTATION_COLORED_PETRINET),
 		JBPM4(Constants.NOTATION_JBPM4),
 		ORGANIGRAM(Constants.NOTATION_ORGANIGRAM),
 		PROCESSMAP(Constants.NOTATION_PROCESSMAP),
 		UML2_2Class(Constants.NOTATION_UML2_2_CLASS),
 		XFORMS(Constants.NOTATION_XFORMS);
 		
 		private String description;
 	     
 		NOTATIONS(String description) {
 			this.description = description;
 	    }
 
 		public String toString() {
 			return description;
 		}
 	}
 	
 	/**
 	 * An enumeration of all available {@link Representation}s (e.g. SVG, JSON).
 	 */
 	public enum FORMATS{
 		SVG(Constants.FORMAT_SVG),
 		BPMAI_JSON(Constants.FORMAT_BPMAI_JSON),
 		PNML(Constants.FORMAT_PNML);
 		
 		private String description;
 	     
 		FORMATS(String description) {
 	        this.description = description;
 	    }
 
 	    public String toString() {
 	         return description;
 	    }
 	}
 	
 	/**
 	 * An enumeration of all available database types (e.g. Orient DB).
 	 */
 	public enum DATABASE_TYPES{
 		ORIENT_DB(Constants.ORIENT_DB);
 		
 		private String description;
 	     
 		DATABASE_TYPES(String description) {
 	        this.description = description;
 	    }
 
 	    public String toString() {
 	         return description;
 	    }
 	}
 }
