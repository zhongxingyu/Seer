 package edu.ucdenver.ccp.iDecoder;
 
 /* for logging messages */
 import org.apache.log4j.Logger;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import edu.ucdenver.ccp.util.Debugger;
 import edu.ucdenver.ccp.util.FileHandler;
 import edu.ucdenver.ccp.util.ObjectHandler;
 import edu.ucdenver.ccp.util.sql.PropertiesConnection;
 import edu.ucdenver.ccp.util.sql.Results;
 import edu.ucdenver.ccp.PhenoGen.data.GeneList;
 import edu.ucdenver.ccp.PhenoGen.tools.idecoder.IDecoderClient;
 import edu.ucdenver.ccp.PhenoGen.tools.idecoder.Identifier;
 import edu.ucdenver.ccp.PhenoGen.util.DbUtils;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.sql.ResultSet;
 import java.util.Date;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 
 /*
 import java.lang.reflect.*;
 import edu.ucdenver.ccp.util.sql.*;
 import edu.ucdenver.ccp.util.*;
 import edu.ucdenver.ccp.PhenoGen.data.*;
 
 import java.awt.Graphics;
 import javax.swing.JApplet;
 import javax.servlet.*;
 import javax.servlet.http.*;
 */
 
 public class RefSetCreator {
 
 	private Logger log;
 	private static Connection dbConn;
 	private static String[] myChips;
 	private Debugger myDebugger = new Debugger();
 	private FileHandler myFileHandler = new FileHandler();
 	private static String referenceFilesDir = "/Users/smahaffey/Desktop/ReferenceFiles/"; 
 
 	public RefSetCreator() {
 		//log = Logger.getRootLogger();
 		log = Logger.getLogger("ReleaseLogger");
 		log.debug("just instantiated RefSetCreator");
 		System.out.println("just instantiated RefSetCreator");
 	}
     
 	public void setConnection(Connection conn) {
 		dbConn = conn;
 	}
 
 	public Connection getConnection(File propertiesFile) {
         	Connection dbConn = null;
 		try {
         		dbConn = new PropertiesConnection().getConnection(propertiesFile);
   			System.out.println("Got database Connection");
 		} catch (Exception e) {
   			System.out.println("Can't get Connection");
 		}
 		return dbConn;
 
 	} 
 
 	private void getChips() throws SQLException {
 
 		log.debug("in getChips");
 
 		String query = 
 			"select distinct array_name "+
 			"from identifier_arrays "+
 			//"where array_name in ('Affymetrix GeneChip Mouse Exon 1.0 ST Array.probeset', "+
 			//"'Affymetrix GeneChip Mouse Exon 1.0 ST Array.transcript') "+
 			"order by array_name";
 
 		myChips = new ObjectHandler().getResultsAsStringArray(new Results(query, dbConn), 0);
 		log.debug("myChips is this long: "+myChips.length);
 
 	}	
 
 	public static void main(String[] args) throws Exception {
   		System.out.println("In RefSetCreator");
 	
 		RefSetCreator myRefSetCreator = new RefSetCreator();
 
 		File propertiesFile = new File("/usr/share/tomcat/webapps/PhenoGen/web/common/dbProperties/Dev.properties");
 		myRefSetCreator.setConnection(myRefSetCreator.getConnection(propertiesFile));
 		Date start=new Date();
                 myRefSetCreator.getChips();
                 dbConn.close();
                 Date end=new Date();
                 long len=(end.getTime()-start.getTime())/(1000*60);
                 System.out.println("After getChips(): "+len+"min.");
 
                 ConcurrentLinkedQueue<String> queue=new ConcurrentLinkedQueue<String>();
                 for (String thisChip : myChips) {
                        queue.add(thisChip);
                 }
                 //myRefSetCreator.setConnection(myRefSetCreator.getConnection(propertiesFile));
                 ArrayList<ChipHandler> list=new ArrayList<ChipHandler>();
                for(int i=0;i<4;i++){
                     ChipHandler ch=new ChipHandler(referenceFilesDir,propertiesFile,queue,i);
                     ch.start();
                     list.add(ch);
                 }
                 while(list.size()>0){
                     for(int i=0;i<list.size();i++){
                         ChipHandler ch=list.get(i);
                         if(ch.isAlive()){
                             ch.join(120000);
                         }else{
                             list.remove(i);
                         }
                     }
                 }
 	} 
 }
 
 
 class ChipHandler extends Thread {
 
     ConcurrentLinkedQueue<String> queue = null;
     Connection dbConn = null;
     boolean done = false;
     int threadnum = 0;
     String referenceFilesDir="";
     private FileHandler myFileHandler = new FileHandler();
 
     public ChipHandler(String referenceFilesDir,File prop,ConcurrentLinkedQueue<String> queue,int num) {
         this.referenceFilesDir=referenceFilesDir;
         this.queue=queue;
         this.threadnum=num;
         this.dbConn=getConnection(prop);
     }
 
     public Connection getConnection(File propertiesFile) {
         	Connection dbConn = null;
 		try {
         		dbConn = new PropertiesConnection().getConnection(propertiesFile);
   			System.out.println("Got database Connection");
 		} catch (Exception e) {
   			System.out.println("Can't get Connection");
 		}
 		return dbConn;
 
 	} 
     
     public void run() {
          while (!queue.isEmpty()) {
                 //System.out.println("top of while:"+queue.isEmpty());
                 if (!queue.isEmpty()) {
                     String entry = (String) queue.poll();
                     if (entry != null) {
                         try{
                             try{
                             System.out.println("Thread:"+threadnum+"Array:"+entry);
                             // Run these one at a time.
                             Date start=new Date();
                             //createEntrezFiles(entry);
                             dbcreateEntrezFiles(entry);
                             Date end=new Date();
                             long len=(end.getTime()-start.getTime())/(1000*60);
                             System.out.println("Thread:"+threadnum+"After createEntrezFiles(): "+len+"min.");
 
                             start=new Date();
                             int geneListID=createGeneLists(entry);
                             end=new Date();
                             len=(end.getTime()-start.getTime())/(1000*60);
                             System.out.println("Thread:"+threadnum+"After createGeneLists(): "+len+"min.");
 
                             start=new Date();
                             createGeneSymbolFiles(entry,geneListID);
                             end=new Date();
                             len=(end.getTime()-start.getTime())/(1000*60);
                             System.out.println("Thread:"+threadnum+"After createGeneSymbolFiles(): "+len+"min.");
 
                             start=new Date();
                             createFinalFiles(entry);
                             end=new Date();
                             len=(end.getTime()-start.getTime())/(1000*60);
                             System.out.println("Thread:"+threadnum+"After createFinalFiles(): "+len+"min.");
 
                             start=new Date();
                             deleteGeneLists(geneListID);
                             end=new Date();
                             len=(end.getTime()-start.getTime())/(1000*60);
                             System.out.println("Thread:"+threadnum+"After deleteGeneLists(): "+len+"min.");
                             }
                             catch(SQLException er){
                                 er.printStackTrace(System.err);
                             }
                         }catch(IOException e){
                             e.printStackTrace(System.err);
                         }
                     }
                 }
          }
         try {
             dbConn.close();
         } catch (SQLException ex) {
             ex.printStackTrace(System.err);
         }
     }
     
     public void dbcreateEntrezFiles(String thisChip) throws IOException, SQLException {
 		//log.debug("in createEntrezFiles");
 		//log.debug("instantiating IDecoderClient");
 		IDecoderClient myIDecoderClient = new IDecoderClient();
 		Identifier myIdentifier = new Identifier();
 
 		String query = 
 			"insert into gene_lists "+
 			"(gene_list_id, gene_list_name, created_by_user_id, organism, create_date, gene_list_source) "+
 			"values "+
 			"(?, 'Temporary '||?||' Gene List', "+
 //WARNING -- THIS CREATES AS CKH USER
 			"1, ?, sysdate, 'Temp')";
 
 		String query2 = 
 			"insert into genes "+
 			"(gene_list_id, gene_id) "+
 			"select ?, id.identifier "+
 			"from identifiers id, identifier_arrays ia "+
 			"where id.id_number = ia.id_number "+
 			//"and rownum < 1000 "+
 			//"and id.identifier = '4304927' "+
 			"and ia.array_name = ? ";
 
 		//log.debug("query = "+query);
 		//log.debug("query2 = "+query2);
 
 		String [] targets = {
 				"Entrez Gene ID", 
 				//"Gene Symbol" 
 				};
 
 		myIDecoderClient.setNum_iterations(1);
 		
                 int geneListID = new DbUtils().getUniqueID("gene_lists_seq", dbConn);
 
 			String organism = getOrganism(thisChip);
 			//log.debug("chip = "+thisChip + ", organism = "+organism);
 
 			PreparedStatement pstmt = dbConn.prepareStatement(query);
 			// Create a gene list and corresponding genes for the set of probeset IDs
 			pstmt.setInt(1, geneListID);
 			pstmt.setString(2, thisChip);
 			pstmt.setString(3, organism);
                 	pstmt.executeQuery();
 
 			pstmt = dbConn.prepareStatement(query2);
 			pstmt.setInt(1, geneListID);
 			pstmt.setString(2, thisChip);
                 	pstmt.executeQuery();
 
                 	pstmt.close();
                         System.out.println("Thread:"+threadnum+" Gene list created");
 			Set<Identifier> iDecoderSet = myIDecoderClient.getIdentifiersByInputIDAndTarget(geneListID, targets, dbConn);
 			//log.debug("there are this many Identifiers in the Set: "+iDecoderSet.size());
 			//log.debug("here they are: "); myDebugger.print(iDecoderSet);
                         System.out.println("Thread:"+threadnum+" iDecoder Complete");
 			// Create a file containing the Entrez Gene IDs -- this needs to be uploaded manually next 
 			myIDecoderClient.writeRefSetFile(iDecoderSet, "Entrez Gene ID", 
 					getEntrezFileName(thisChip));
 			Set<Identifier> entrezSet = myIDecoderClient.getIdentifiersForTarget(iDecoderSet, targets);
 			myFileHandler.writeFile(myIDecoderClient.getValues(entrezSet), 
 					getEntrezOnlyFileName(thisChip));
                         System.out.println("Thread:"+threadnum+" files created.");
 			//log.debug("right before committing.  This should clear genelistgraph");
 			// Have to commit in order to clear out genelistgraph temporary table
 			dbConn.commit();
 		
 	}
     
     public void createEntrezFiles(String thisChip) throws IOException{
 
 		IDecoderClient myIDecoderClient = new IDecoderClient();
 		Identifier myIdentifier = new Identifier();
 		String query2 = "select id.identifier "+
 			"from identifiers id, identifier_arrays ia "+
 			"where id.id_number = ia.id_number "+
 			"and ia.array_name = ? ";
 
 		String [] targets = {
 				"Entrez Gene ID"
 				};
 
 		myIDecoderClient.setNum_iterations(1);
 		
                 	//int geneListID = new DbUtils().getUniqueID("gene_lists_seq", dbConn);
                     
 			String organism = getOrganism(thisChip);
                         System.out.println("chip = "+thisChip + ", organism = "+organism);
 			
 
 			BufferedWriter entrez = new BufferedWriter(new FileWriter(getEntrezFileName(thisChip)));
                         BufferedWriter entrezOnly = new BufferedWriter(new FileWriter(getEntrezOnlyFileName(thisChip)));
                         try{
 			PreparedStatement pstmt = dbConn.prepareStatement(query2);
 			pstmt.setString(1, thisChip);
                 	ResultSet rs=pstmt.executeQuery();
                         int count=0;
                         while(rs.next()){
                             count++;
                             String geneID=rs.getString(1);
                             Set<Identifier> iDecoderSet = myIDecoderClient.getIdentifiersByInputIDAndTarget(geneID,organism, targets, dbConn);
                             if (iDecoderSet != null && iDecoderSet.size() > 0) {
                                 Iterator startItr=iDecoderSet.iterator();
                                 while(startItr.hasNext()){
                                     Identifier start=(Identifier)startItr.next();
                                     Set<String> linksSet = (start.getTargetHashMap() != null && start.getTargetHashMap().size() > 0 ? 
                                                             myIDecoderClient.getValues((Set<Identifier>) start.getTargetHashMap().get("Entrez Gene ID")) : 
                                                             null);  
 
                                     if (linksSet != null && linksSet.size() > 0) {
                                             Iterator itr2 = linksSet.iterator();
                                             while (itr2.hasNext()) {
                                                     String target=(String) itr2.next();
                                                     entrez.write(geneID+"\t" + target);
                                                     entrez.newLine();
                                                     entrezOnly.write(target);
                                                     entrezOnly.newLine();
                                             }
                                     }
                                     if(count%1000==0){
                                         System.out.println("Thread:"+threadnum+"Processed:"+count);
                                     }
                                 }
                             }
                         }
                         rs.close();
                 	pstmt.close();
 			
                         }catch(SQLException e){
                             e.printStackTrace(System.err);
                         }
                         
                         entrez.flush();
                         entrez.close();
                         entrezOnly.flush();
                         entrezOnly.close();
 		
 	}
 
 	public String getEntrezFileName(String chipName) {
 		return referenceFilesDir + chipName + "_EntrezGeneIDs.txt";
 	}
 	public String getEntrezOnlyFileName(String chipName) {
 		return referenceFilesDir + chipName + "_EntrezGeneIDs_ONLY.txt";
 	}
         private String getOrganism (String chipName) {
 		return (chipName.indexOf("Rat") > -1 ? "Rn" : 
 			(chipName.indexOf("Human") > -1 ? "Hs" : 
 			(chipName.indexOf("Drosophila") > -1 ? "Dm" :
 			"Mm")));
 	}
         
         public int createGeneLists(String thisChip) throws SQLException, IOException {
 		int glID=0;
 		GeneList thisGeneList = new GeneList();
 		thisGeneList.setGene_list_name(thisChip + " Temporary Gene List");	
 		thisGeneList.setDescription(thisChip);	
 			//THIS CREATES GENELISTS AS THE CKH USER
 		thisGeneList.setCreated_by_user_id(1);
 		thisGeneList.setOrganism(getOrganism(thisChip));	
 		thisGeneList.setGene_list_source("Uploaded File");	
 		glID=thisGeneList.loadFromFile(0, getEntrezOnlyFileName(thisChip), dbConn);
 		dbConn.commit();
                 return glID;
 	}
         public void createGeneSymbolFiles(String thisChip,int geneListID) throws IOException, SQLException {
 		IDecoderClient myIDecoderClient = new IDecoderClient();
 
 		String [] targets = {
 				//"Entrez Gene ID", 
 				"Gene Symbol" 
 				};
 
 		myIDecoderClient.setNum_iterations(1);
 			Set<Identifier> iDecoderSet = myIDecoderClient.getIdentifiersByInputIDAndTarget(geneListID, targets, dbConn);
 			//log.debug("here they are: "); myDebugger.print(iDecoderSet);
 
 			// Create a file mapping the Entrez Gene IDs to Gene Symbols 
 			myIDecoderClient.writeRefSetFile(iDecoderSet, "Gene Symbol", 
 					referenceFilesDir + thisChip + "_EntrezGeneIDs_GeneSymbols.txt");
 			
 			// Have to commit in order to clear out genelistgraph temporary table
 			dbConn.commit();
 	}
         
         public void createFinalFiles(String thisChip) throws IOException, SQLException {
 			Hashtable<String, List<String>> entrezHashtable = myFileHandler.getFileAsHashtablePlusList( 
 					new File(getEntrezFileName(thisChip)));
 			Hashtable<String, List<String>> geneSymbolHashtable = myFileHandler.getFileAsHashtablePlusList( 
 					new File(referenceFilesDir + thisChip + "_EntrezGeneIDs_GeneSymbols.txt"));
 			//log.debug("entrezHashtable has this many entries: " +entrezHashtable.size());
 			//log.debug("geneSymbolHashtable has this many entries: " +geneSymbolHashtable.size());
 			List<String> abc = new ArrayList<String>();
 			
                 	Iterator probeItr = entrezHashtable.keySet().iterator();
 
 			int j=0;
                 	while (probeItr.hasNext()) {
                         	String thisProbeID = (String) probeItr.next(); 
 				if (j<5) {
 					//log.debug("thisProbeID = "+thisProbeID);
 				}
 				List<String> entrezList = entrezHashtable.get(thisProbeID);
 				if (entrezList != null) {
                 			Iterator entrezItr = entrezList.iterator();
                 			while (entrezItr.hasNext()) {
                         			String thisEntrez = (String) entrezItr.next(); 
 						List<String> gsList = geneSymbolHashtable.get(thisEntrez);
 						if (gsList != null) {
 							if (j<5) {
 								//log.debug("gsList is not null. it contains " + gsList.size() + " entries.");
 							}
 							// Create a new line for each gene symbol
                 					Iterator gsItr = gsList.iterator();
                 					while (gsItr.hasNext()) {
                         					String thisGs = (String) gsItr.next(); 
                         					abc.add(thisProbeID + "\t" + thisEntrez + "\t" + thisGs);
 							}
 						} else {
 							// if no gene symbols are found for this Entrez ID, then just repeat the Entrez ID
                         				abc.add(thisProbeID + "\t" + thisEntrez + "\t" + thisEntrez);
 						}
 					}
 				} else {
 					// if no Entrez IDs are found for this probeset ID, then just repeat the probeset ID
                         		abc.add(thisProbeID + "\t" + thisProbeID + "\t" + thisProbeID);
 				}
 				j++;
 			}
 	                String[] lineArray = (String[]) abc.toArray(new String [abc.size()] );
 			myFileHandler.writeFile(abc, referenceFilesDir + thisChip + "_Final.txt");
 		
 	}
         public void deleteGeneLists(int geneListID) throws SQLException, IOException {
 			new GeneList(geneListID).deleteGeneList(dbConn);
 			dbConn.commit();
 	}
 }
