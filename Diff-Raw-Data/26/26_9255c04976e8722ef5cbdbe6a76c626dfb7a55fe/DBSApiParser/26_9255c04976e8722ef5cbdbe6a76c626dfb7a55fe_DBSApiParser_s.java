 /**
 $Revision: 1.19 $"
 $Id: DBSApiParser.java,v 1.19 2007/12/17 16:06:33 sekhri Exp $"
  *
 */
 
 
 package dbs.api.parser;
 import java.sql.Connection;
 import java.io.Writer;
 import java.util.Vector;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import xml.DBSXMLParser;
 import xml.Element;
 import xml.XMLException;
 import dbs.util.DBSUtil;
 import dbs.api.DBSApiFileLogic;
 import dbs.api.DBSApiData;
 
 /**
  * This class handles XML parsing for the input provided by the clients.
  * @author sekhri
  */
 public class DBSApiParser {
 	
 
 	/**
 	* Constructs a DBSApiPasrer object . All the methods in thhis class is static so there is no need to make the instance of this class. The constuctor does nothing.
 	*/
 	public DBSApiParser() {}
 
 	
 	public static Hashtable parse(String inputXml, String key) throws Exception {
 		//	checkXML(inputXml);
 		DBSXMLParser dbsParser = new DBSXMLParser();
 		dbsParser.parseString(inputXml); 
  		Vector allElement = dbsParser.getElements();
 		Hashtable table = null;
 		for (int i=0; i<allElement.size(); ++i) {
 			Element e = (Element)allElement.elementAt(i);
 			if (e.name.equals(key) ) {
 				table = e.attributes;
 			} 
 		}
 		return table;
 	}
 
 	public static Hashtable parseBlock(String inputXml) throws Exception {
 		DBSXMLParser dbsParser = new DBSXMLParser();
 		dbsParser.parseString(inputXml); 
 		Vector allElement = dbsParser.getElements();
 		Hashtable table = null;
 		for (int i=0; i<allElement.size(); ++i) {
 			Element e = (Element)allElement.elementAt(i);
 			String name = e.name;
 			if (name.equals("block") ) {
 				table = e.attributes;
 				table.put("storage_element", new Vector());
 			} 
 			if (name.equals("storage_element") ) 
 				((Vector)(get(table, "storage_element", "block"))).add(e.attributes);
 		}
 		return table;
 	}
 
 
         public static Hashtable parseBranchInfo(String inputXml) throws Exception {
                 DBSXMLParser dbsParser = new DBSXMLParser();
                 dbsParser.parseString(inputXml);
                 Vector allElement = dbsParser.getElements();
                 Hashtable table = null;
                 for (int i=0; i<allElement.size(); ++i) {
                         Element e = (Element)allElement.elementAt(i);
                         String name = e.name;
                         if (name.equals("branch_info") ) {
                                 table = e.attributes;
                                 table.put("branch_names", new Vector());
                         }
                         if (name.equals("branch") )
                                 ((Vector)(get(table, "branch_names", "branch_info"))).add(e.attributes);
                 }
                 return table;
         }
 
 
 	public static Hashtable parsePD(String inputXml) throws Exception {
 		DBSXMLParser dbsParser = new DBSXMLParser();
 		dbsParser.parseString(inputXml); 
 		Vector allElement = dbsParser.getElements();
 		Hashtable psDS = null;
 		for (int i=0; i<allElement.size(); ++i) {
 			Element e = (Element)allElement.elementAt(i);
 			String name = e.name;
 			if (name.equals("processed_dataset") ) {
 				psDS = e.attributes;
 				psDS.put("data_tier", new Vector());
 				psDS.put("parent", new Vector());
 				psDS.put("algorithm", new Vector());
 				psDS.put("run", new Vector());
 			} 
 			if (name.equals("data_tier") ) 
 				((Vector)(get(psDS, "data_tier", "processed_dataset"))).add(e.attributes);
 			if (name.equals("parent") ) 
 				((Vector)(get(psDS, "parent", "processed_dataset"))).add(e.attributes);
 			if (name.equals("algorithm") ) 
 				((Vector)(get(psDS, "algorithm", "processed_dataset"))).add(e.attributes);
 			if (name.equals("run") ) 
 				((Vector)(get(psDS, "run", "processed_dataset"))).add(e.attributes);
 		}
 		return psDS;
 	}
 
         public static Hashtable parseADSContents(String inputXml) throws Exception {
                 int index = -1;
                 int blockIndex = -1;
                 DBSXMLParser dbsParser = new DBSXMLParser();
                 dbsParser.parseString(inputXml);
                 ArrayList allElement = dbsParser.getArrayElements();
                 Hashtable table = new Hashtable();
                 Hashtable psDS = null;
                 ArrayList files = new ArrayList();
 
                 for (int i=0; i<allElement.size(); ++i) {
 
                         //Element e = (Element)allElement.elementAt(i);
                         Element e = (Element)allElement.get(i);
                         String name = e.name;
 
                         if (name.equals("analysis_dataset") ) {
                                 table.put("analysis_dataset", e.attributes);
                         }
 
                         if (name.equals("dataset") ) {
                                 table.put("dataset", e.attributes);
                         }
 
 
                         if (name.equals("analysis_dataset_def") ) {
                                 table.put("analysis_dataset_def", e.attributes);
                         }
 
                         if (name.equals("file") ) {
                                 Hashtable file = e.attributes;
                                 file.put("file_lumi_section", new ArrayList());
                                 files.add(file);
                                 ++index;
                         }
                         if (name.equals("file_lumi_section") ) {
                                 ((ArrayList)( get((Hashtable) get(files, index, "file"), "file_lumi_section", "file"))).add(e.attributes);
 			}
                 }
                 table.put("files", files);
                 return table;
         }
 
 	public static Hashtable parseDatasetContents(String inputXml) throws Exception {
 		int index = -1;
 		int blockIndex = -1;
 		//System.out.println("inputXml "+inputXml);
 		DBSXMLParser dbsParser = new DBSXMLParser();
 		dbsParser.parseString(inputXml); 
 		//Vector allElement = dbsParser.getElements();
     		ArrayList allElement = dbsParser.getArrayElements();
 		Hashtable table = null;
 		Hashtable psDS = null;
 		//Vector topLevel = new Vector();
 		ArrayList topLevel = new ArrayList();
 		for (int i=0; i<allElement.size(); ++i) {
 			//Element e = (Element)allElement.elementAt(i);
 			Element e = (Element)allElement.get(i);
 			String name = e.name;
 			if (name.equals("dataset") ) {
 				table = e.attributes;
 			}
 			if (name.equals("primary_dataset") ) {
 				table.put("primary_dataset", e.attributes);
 			}
 			if (name.equals("processed_dataset") ) {
 				psDS = e.attributes;
 				psDS.put("data_tier", new Vector());
 				psDS.put("parent", new Vector());
 				psDS.put("algorithm", new Vector());
 				psDS.put("run", new Vector());
 				psDS.put("block", new Vector());
 			} 
 			if (name.equals("data_tier") ) 
 				((Vector)(get(psDS, "data_tier", "processed_dataset"))).add(e.attributes);
 			if (name.equals("processed_dataset_parent") ) 
 				((Vector)(get(psDS, "parent", "processed_dataset"))).add(e.attributes);
			//if (name.equals("algorithm") ) 
			if (name.equals("processed_dataset_algorithm") ) 
 				((Vector)(get(psDS, "algorithm", "processed_dataset"))).add(e.attributes);
 			if (name.equals("run") ) 
 				((Vector)(get(psDS, "run", "processed_dataset"))).add(e.attributes);
 			if (name.equals("block") ) {
 				((Vector)(get(psDS, "block", "processed_dataset"))).add(e.attributes);
 			
 			//if (name.equals("block") ) {
 				Hashtable block = e.attributes;
 				block.put("storage_element", new Vector());
 				((Vector)(get(psDS, "block", "processed_dataset"))).add(block);
 				++blockIndex;
 			} 
 			if (name.equals("storage_element") ) 
 				((Vector)((Hashtable)(((Vector)(get(psDS, "block", "processed_dataset"))).get(blockIndex))).get("storage_element")).add(e.attributes);
 
 			if (name.equals("file") ) {
 				Hashtable file = e.attributes;
 				file.put("file_lumi_section", new ArrayList());
 				file.put("file_data_tier", new ArrayList());
 				file.put("file_parent", new ArrayList());
 				file.put("file_algorithm", new ArrayList());
 				file.put("file_branch", new ArrayList());
                                 file.put("file_trigger_tag", new ArrayList());
 				topLevel.add(file);
 				++index;
 			} 
 			if (name.equals("file_lumi_section") ) 
 				((ArrayList)( get((Hashtable) get(topLevel, index, "file"), "file_lumi_section", "file"))).add(e.attributes);
 			if (name.equals("file_data_tier") ) 
 				((ArrayList)( get((Hashtable) get(topLevel, index, "file"), "file_data_tier", "file"))).add(e.attributes);
 			if (name.equals("file_parent") ) 
 				((ArrayList)( get((Hashtable) get(topLevel, index, "file"), "file_parent", "file"))).add(e.attributes);
 			if (name.equals("file_algorithm") ) 
 				((ArrayList)( get((Hashtable) get(topLevel, index, "file"), "file_algorithm", "file"))).add(e.attributes);
 			//if (name.equals("file_branch") ) 
 			//	((Vector)( get((Hashtable) get(topLevel, index, "file"), "file_branch", "file"))).add(e.attributes);
 			if (name.equals("file_trigger_tag") )
 				((ArrayList)( get((Hashtable) get(topLevel, index, "file"), "file_trigger_tag", "file"))).add(e.attributes);
 
 
 		}
 		table.put("processed_dataset", psDS);
 		table.put("file", topLevel);
 		return table;
 	}
 
 	public static void insertFiles(DBSApiData data, Connection conn, Writer out, String primary, String proc, String inputXml, Hashtable dbsUser) throws Exception {
 		ArrayList topLevel = new ArrayList();
 		int index = -1;
 		DBSXMLParser dbsParser = new DBSXMLParser();
 		dbsParser.parseString(inputXml); 
     		ArrayList allElement = dbsParser.getArrayElements();
 		Hashtable psDS = null;
                 Hashtable block = null;
 
 		for (int i=0; i<allElement.size(); ++i) {
 			Element e = (Element)allElement.get(i);
 			//Element e = (Element)allElement.elementAt(i);
 			String name = e.name;
 			if (name.equals("file") ) {
 				Hashtable file = e.attributes;
 
                                 file.put("file_lumi_section", new ArrayList());
                                 file.put("file_data_tier", new ArrayList());
                                 file.put("file_parent", new ArrayList());
                                 file.put("file_child", new ArrayList());
                                 file.put("file_algorithm", new ArrayList());
                                 file.put("file_branch", new ArrayList());
                                 file.put("file_trigger_tag", new ArrayList());
 
 				topLevel.add(file);
 				++index;
 			} 
                         //ONLY One Block for this Procssed Dataset, as ALL files belong to same block
                         if (name.equals("block") ) {
                                 block = e.attributes;
                                 //Keep the storage_elements
                                 block.put("storage_element", new ArrayList());
                         }
                         if (name.equals("storage_element") )
                                 ((ArrayList) get(block, "storage_element", "block")).add(e.attributes);
 			if (name.equals("file_lumi_section") ) 
 				((ArrayList)( get((Hashtable) get(topLevel, index, "file"), "file_lumi_section", "file"))).add(e.attributes);
 			if (name.equals("file_data_tier") )
 				((ArrayList)( get((Hashtable) get(topLevel, index, "file"), "file_data_tier", "file"))).add(e.attributes);
 			if (name.equals("file_parent") ) 
 				((ArrayList)( get((Hashtable) get(topLevel, index, "file"), "file_parent", "file"))).add(e.attributes);
 			if (name.equals("file_child") ) 
 				((ArrayList)( get((Hashtable) get(topLevel, index, "file"), "file_child", "file"))).add(e.attributes);
 			if (name.equals("file_algorithm") ) 
 				((ArrayList)( get((Hashtable) get(topLevel, index, "file"), "file_algorithm", "file"))).add(e.attributes);
                         //if (name.equals("file_branch") ) 
                         //        ((Vector)( get((Hashtable) get(topLevel, index, "file"), "file_branch", "file"))).add(e.attributes);
                         if (name.equals("file_trigger_tag") )
                                 ((ArrayList)( get((Hashtable) get(topLevel, index, "file"), "file_trigger_tag", "file"))).add(e.attributes);
 			if (name.equals("processed_datatset") ) {
 				psDS = e.attributes;
 			}
 		}
                 (new DBSApiFileLogic(data)).insertFiles(conn, out, DBSUtil.get(psDS, "path"), primary, proc, block, topLevel, dbsUser);
 	}
 
 	public static Hashtable parseADD(String inputXml) throws Exception {
 		DBSXMLParser dbsParser = new DBSXMLParser();
 		dbsParser.parseString(inputXml); 
 		Vector allElement = dbsParser.getElements();
 		Hashtable table = null;
 		for (int i=0; i<allElement.size(); ++i) {
 			Element e = (Element)allElement.elementAt(i);
 			String name = e.name;
 			if (name.equals("analysis_dataset_definition") ) {
 				table = e.attributes;
 				table.put("lumi_section", new Vector());
 				table.put("run", new Vector());
 				table.put("algorithm", new Vector());
 				table.put("file", new Vector());
 			} 
 			if (name.equals("lumi_section") ) 
 				((Vector)(get(table, "lumi_section", "analysis_dataset_definition"))).add(e.attributes);
 			if (name.equals("run") ) 
 				((Vector)(get(table, "run", "analysis_dataset_definition"))).add(e.attributes);
 			if (name.equals("algorithm") ) 
 				((Vector)(get(table, "algorithm", "analysis_dataset_definition"))).add(e.attributes);
 			if (name.equals("file") ) 
 				((Vector)(get(table, "file", "analysis_dataset_definition"))).add(e.attributes);
 
 		}
 		return table;
 	}
 
 
 	public static Hashtable parseCompADS(String inputXml) throws Exception {
                 DBSXMLParser dbsParser = new DBSXMLParser();
                 dbsParser.parseString(inputXml);
                 Vector allElement = dbsParser.getElements();
                 Hashtable table = null;
                 for (int i=0; i<allElement.size(); ++i) {
                         Element e = (Element)allElement.elementAt(i);
                         String name = e.name;
                         if (name.equals("comp_analysisds") ) {
                                 table = e.attributes;
                                 table.put("analysis_datasets", new Vector());
                         } 
                         if (name.equals("analysis_dataset") )
                                 ((Vector)(get(table, "analysis_datasets", "analysis_dataset"))).add(e.attributes);
                 }
                 return table;
         }
 
 
 
 	public static void remapFiles(Connection conn, Writer out, String inputXml, Hashtable dbsUser) throws Exception {
 		Vector topLevel = new Vector();
 		DBSXMLParser dbsParser = new DBSXMLParser();
 		dbsParser.parseString(inputXml); 
 		Vector allElement = dbsParser.getElements();
 		Hashtable table = null;
 		for (int i=0; i<allElement.size(); ++i) {
 			Element e = (Element)allElement.elementAt(i);
 			String name = e.name;
 			if (name.equals("in_file") ) {
 				topLevel.add(DBSUtil.get(e.attributes, "lfn"));
 			} 
 			if (name.equals("out_file") ) {
 				table = e.attributes;
 			}
 		}
                 (new DBSApiFileLogic(new DBSApiData())).remapFiles(conn, out, topLevel, table,  dbsUser);
 	}
 
 
         public static Vector parseDQFlags(String inputXml) throws Exception {
                Vector topLevel = new Vector();
                 int subIndex = -1;
                 DBSXMLParser dbsParser = new DBSXMLParser();
                 dbsParser.parseString(inputXml);
                 Vector allElement = dbsParser.getElements();
 
                 for (int i=0; i<allElement.size(); ++i) {
                         Element e = (Element)allElement.elementAt(i);
                         String name = e.name;
                         if (name.equals("dq_sub_system") ) {
                                 Hashtable subsys = e.attributes;
 
                                 //Add a sub-sub system vector
                                 subsys.put("dq_sub_subsys", new Vector());
 
                                 // Add the sub-system to topLevel
                                 topLevel.add(subsys);
 
                                 //Increase the counter so next dq_sub_subsys, get THIS item
                                 ++subIndex;
                         }
                         if (name.equals("dq_sub_subsys") ) {
                                 Hashtable subsubsys = e.attributes;
 
                                 //Get the sub system
                                 Hashtable subsys = (Hashtable) get(topLevel, subIndex, "dq_sub_system");
                                 //Get the sub-sub system vector
                                 Vector subsubvec = (Vector) get (subsys, "dq_sub_subsys", "dq_sub_subsys");
                                 //Add to subsubvec current dq_sub_subsys
                                 subsubvec.add(subsubsys);
                         }
 
 
 
                 }
                 return topLevel;
         }
 
         public static Vector parseDQRunLumi(String inputXml) throws Exception {
 
                 Vector topLevel = new Vector();
                 int index = -1;
                 int subIndex = -1;
                 DBSXMLParser dbsParser = new DBSXMLParser();
                 dbsParser.parseString(inputXml);
                 Vector allElement = dbsParser.getElements();
 
                 for (int i=0; i<allElement.size(); ++i) {
                         Element e = (Element)allElement.elementAt(i);
                         String name = e.name;
 
                         if (name.equals("run") ) {
                                 Hashtable run = e.attributes;
                                 run.put("dq_sub_system", new Vector());
                                 topLevel.add(run);
                                 ++index;
                                 subIndex=-1;
 
                         }
                         if (name.equals("dq_sub_system") ) {
                                 Hashtable subsys = e.attributes;
 
                                 //Add a sub-sub system vector
                                 subsys.put("dq_sub_subsys", new Vector());
 
                                 // Add the sub-system to current run
                                 ((Vector)( get((Hashtable) get(topLevel, index, "run"), "dq_sub_system", "run"))).add(subsys);
 
                                 //Increase the counter so next dq_sub_subsys, get THIS item
                                 ++subIndex;
                         }
                         if (name.equals("dq_sub_subsys") ) {
                                 Hashtable subsubsys = e.attributes;
                                 //Get the Run
                                 Hashtable run = (Hashtable) get(topLevel, index, "run");
                                 //Get the Sub System vector
                                 Vector subvec = (Vector) get (run, "dq_sub_system", "run");
                                 //Get the sub system
                                 Hashtable subsys = (Hashtable) get(subvec, subIndex, "dq_sub_system");
                                 //Get the sub-sub system vector
                                 Vector subsubvec = (Vector) get (subsys, "dq_sub_subsys", "dq_sub_subsys");
                                 //Add to subsubvec current dq_sub_subsys
                                 subsubvec.add(subsubsys);
                         }
 
                 }
                 return topLevel;
         }
 
 
 	private static Object get(Hashtable table, String key, String missingKey) throws Exception {
 		if(key == null ||  table == null) 
 			throw new XMLException("Invalid XML", "3002", "The given xml is not a valid DBS XML. Most likely the tag " + missingKey + " is missing");
 		
 		if(!table.containsKey(key)) 
 			throw new XMLException("Invalid XML", "3003", "The given xml is not a valid DBS XML. The key " + key + " is missing from the XML and most likely the tag <" + missingKey + "> is missing also");
 		
 		Object tmp = table.get(key);
 		if(tmp == null) 
 			throw new XMLException("Invalid XML", "3002", "The given xml is not a valid DBS XML. Most likely the tag " + missingKey + " is missing");
 		
 		return tmp;
 	}
 
 
         private static Object get(java.util.ArrayList v, int index, String missingKey) throws Exception {
                 if((index < 0) || (v.size() <= index))
                         throw new XMLException("Invalid XML", "3002", "The given xml is not a valid DBS XML. Most likely the tag " + missingKey + " is missing");
 
                 Object tmp = v.get(index);
                 if(tmp == null)
                         throw new XMLException("Invalid XML", "3002", "The given xml is not a valid DBS XML. Most likely the tag " + missingKey + " is missing");
 
                 return tmp;
         }
 
 
 
 	private static Object get(Vector v, int index, String missingKey) throws Exception {
 		if((index < 0) || (v.size() <= index)) 
 			throw new XMLException("Invalid XML", "3002", "The given xml is not a valid DBS XML. Most likely the tag " + missingKey + " is missing");
 		
 		Object tmp = v.get(index);
 		if(tmp == null) 
 			throw new XMLException("Invalid XML", "3002", "The given xml is not a valid DBS XML. Most likely the tag " + missingKey + " is missing");
 		
 		return tmp;
 	}
 
 }
