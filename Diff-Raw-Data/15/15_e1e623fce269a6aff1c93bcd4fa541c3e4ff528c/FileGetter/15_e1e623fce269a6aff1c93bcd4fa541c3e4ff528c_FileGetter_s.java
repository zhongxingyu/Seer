 package edu.ucdenver.ccp.iDecoder;
 
 /* for logging messages */
 import org.apache.log4j.Logger;
 import java.lang.reflect.*;
 import java.util.*;
 import java.io.*;
 import java.sql.*;
 import edu.ucdenver.ccp.util.sql.*;
 import edu.ucdenver.ccp.util.*;
 import edu.ucdenver.ccp.iDecoder.*;
 import edu.ucdenver.ccp.PhenoGen.data.*;
 import edu.ucdenver.ccp.PhenoGen.tools.idecoder.*;
 
 import java.awt.Graphics;
 import javax.swing.JApplet;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 
 public class FileGetter extends HttpServlet {
 
   private Logger log;
 
   public FileGetter() {
 		log = Logger.getRootLogger();
 		log.debug("just instantiated FileGetter");
 		System.out.println("just instantiated FileGetter");
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
 
 
   public static void main(String[] args) throws Exception {
   	System.out.println("In FileGetter");
 	
 	FileGetter myFileGetter = new FileGetter();
 	File devPropertiesFile = new File("/usr/share/tomcat/webapps/PhenoGen/web/common/dbProperties/stan_halDev.properties");
 	Connection dbConn = myFileGetter.getConnection(devPropertiesFile);
 	ObjectHandler myObjectHandler = new ObjectHandler();
 	List<String[]> files = new ArrayList<String[]>();
 
 	String mainDir = "/Users/smahaffey/iDecoder/InputFiles/";
 	String flybaseMain = "ftp://ftp.flybase.net/releases/current/precomputed_files/genes/";
 	String ncbiMain = "ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/";
 	String mgiMain = "ftp://ftp.informatics.jax.org/pub/reports/";
 	String ncbiHomologene = "ftp://ftp.ncbi.nih.gov/pub/HomoloGene/current/";
 	String rgdMain = "ftp://rgd.mcw.edu/pub/data_release/";
 	String swissprotMain = "ftp://ftp.expasy.org/databases/uniprot/current_release/knowledgebase/complete/";
 /*
 	//String affyMain = "http://www.affymetrix.com/Auth/analysis/downloads/na31/ivt/";
 	//String affyExonMain = "http://www.affymetrix.com/Auth/analysis/downloads/na31/wtexon/";
 	files.add(new String[] {affyMain, mainDir + "Affymetrix/", "HG-U133_Plus_2.na31.annot.csv.zip"});
 	files.add(new String[] {affyMain, mainDir + "Affymetrix/", "RG_U34A.na31.annot.csv.zip"});
 	files.add(new String[] {affyMain, mainDir + "Affymetrix/", "RAE230A.na31..annot.csv.zip"});
 	files.add(new String[] {affyMain, mainDir + "Affymetrix/", "Mouse430A_2.na31.annot.csv.zip"});
 	files.add(new String[] {affyMain, mainDir + "Affymetrix/", "MOE430B.na31.annot.csv.zip"});
 	files.add(new String[] {affyMain, mainDir + "Affymetrix/", "Mouse430_2.na31.annot.csv.zip"});
 	files.add(new String[] {affyExonMain, mainDir + "Affymetrix/", "MoEx-1_0-st-v1.na31.mm9.probeset.csv.zip"});
 	files.add(new String[] {affyExonMain, mainDir + "Affymetrix/", "MoEx-1_0-st-v1.na31.mm9.transcript.csv.zip"});
 	files.add(new String[] {affyMain, mainDir + "Affymetrix/", "MOE430A.na31.annot.csv.zip"});
 	files.add(new String[] {affyMain, mainDir + "Affymetrix/", "DrosGenome1.na31.annot.csv.zip"});
 	files.add(new String[] {affyExonMain, mainDir + "Affymetrix/", "RaEx-1_0-st-v1.na31.rn4.probeset.csv.zip"});
 	files.add(new String[] {affyExonMain, mainDir + "Affymetrix/", "RaEx-1_0-st-v1.na31.rn4.probeset.csv.zip"});
 */
	files.add(new String[] {flybaseMain, mainDir + "FlyBase/", "fbgn_NAseq_Uniprot_fb_2013_03.tsv.gz"});
	files.add(new String[] {flybaseMain, mainDir + "FlyBase/", "gene_map_table_fb_2013_03.tsv.gz"}); 
 	files.add(new String[] {mgiMain, mainDir + "MGI/", "MGI_Coordinate_build37.rpt"});
         files.add(new String[] {ncbiMain, mainDir + "NCBI/", "gene2accession.gz"}); 
 	files.add(new String[] {ncbiMain, mainDir + "NCBI/", "gene2unigene"}); 
 	files.add(new String[] {ncbiMain, mainDir + "NCBI/", "gene_info.gz"}); 
 	files.add(new String[] {ncbiHomologene, mainDir + "NCBI/", "homologene.data"}); 
 	files.add(new String[] {rgdMain, mainDir + "RGD/", "GENES_RAT.txt"}); 
 	files.add(new String[] {swissprotMain, mainDir + "SwissProt/", "uniprot_sprot.dat.gz"}); 
 	FileHandler myFileHandler = new FileHandler();
 	for (Iterator itr = files.iterator(); itr.hasNext();) {
 		String[] info = (String[]) itr.next();
 		String remoteLocation = info[0];
 		String localDir = info[1];
 		String fileName = info[2];
 		System.out.println("downloading "+ fileName + " from "+remoteLocation);
 		myFileHandler.getFileFromURL(remoteLocation, localDir, fileName); 
 	}
 	System.out.println("getting MGIFile.txt");
 	new JacksonLab().run();
 	System.out.println("done getting MGIFile.txt");
 	
   } 
 
 }
