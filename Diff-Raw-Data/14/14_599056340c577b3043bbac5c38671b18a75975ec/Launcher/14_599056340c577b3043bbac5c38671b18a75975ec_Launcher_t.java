 package ch.epfl.bbcf.conversion.daemon;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Date;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.RollingFileAppender;
 import org.json.JSONException;
 
 import ch.epfl.bbcf.bbcfutils.Utility;
 import ch.epfl.bbcf.bbcfutils.access.gdv.RemoteAccess;
 import ch.epfl.bbcf.bbcfutils.access.genrep.MethodNotFoundException;
 import ch.epfl.bbcf.bbcfutils.conversion.json.ConvertToJSON;
 import ch.epfl.bbcf.bbcfutils.conversion.sqlite.ConvertToSQLite;
 import ch.epfl.bbcf.bbcfutils.conversion.sqlite.ConvertToSQLite.Extension;
 import ch.epfl.bbcf.bbcfutils.exception.ExtensionNotRecognisedException;
 import ch.epfl.bbcf.bbcfutils.parser.exception.ParsingException;
 import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteAccess;
 import ch.epfl.bbcf.conversion.conf.Configuration;
 import ch.epfl.bbcf.utility.file.FileManagement;
 
 
 
 
 /**
  * Class which choose what parsing to launch in function
  * of the parameters given
  * @author Yohan Jarosz
  *
  */
 public class Launcher extends Thread{
 
 	public static final Logger logger = initLogger(Launcher.class.getName());
 	/**
 	 * the track id in your database
 	 */
 	private String trackId;
 	/**
 	 * the extension of the file (must be part of Configuration.Extension)
 	 */
 	private String extension;
 	/**
 	 * the nr assembly id in Genrep
 	 */
 	private int nrAssemblyId;
 
 	/**
 	 * a directory to delete after use
 	 */
 	private String tmpdir;
 
 	/**
 	 * mail of an user (to send feedback when a track succeed)
 	 */
 	private String mail;
 	/**
 	 * the parh of the file to parse
 	 */
 	private String filePath;
 
 
 	public void process(String filePath,String trackId,String tmpdir,
 			String extension,String mail,String nrAssemblyId) {
 		this.filePath = filePath;
 		this.trackId = trackId;
 		this.tmpdir=tmpdir;
 		this.mail = mail;
 		this.extension = extension;
 		this.nrAssemblyId = Integer.parseInt(nrAssemblyId);
 		ManagerService.submitSQLiteProcess(this);
 	}
 
 	public void run(){
 		long start = System.currentTimeMillis();
 
 
 
 		//////////   GETTING MD5   //////////
 		logger.debug(this.getId()+" run : "+filePath);
 		File file = new File(filePath);
 		String md5;
 		try {
 			md5 = Utility.getFileMd5(file.getAbsolutePath());
 		} catch (IOException e2) {
 			logger.error(e2);
 			return;
 		}
 		if(null==md5){
 			try {
 				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),"cannot get md5","md5",trackId, filePath);
 			} catch (IOException e) {
 				logger.error(e);
 			}
 			return;
 		}
 
 
 		boolean doJSON = false;
 		boolean doSQLite = false;
 		//////////   EXTENSION   //////////
 		Extension ext = null;
 		String type = "";
 		if(extension.equalsIgnoreCase("wig")){
 			doSQLite = true;
 			ext = Extension.WIG;
 			type="quantitative";
 		} else if(extension.equalsIgnoreCase("bedgraph")){
 			ext = Extension.BEDGRAPH;
 			doSQLite = true;
 			type="quantitative";
 		}else if(extension.equalsIgnoreCase("bed")){
 			ext = Extension.BED;
 			doSQLite = true;
 			type="qualitative";
 			doJSON=true;
 		}else if(extension.equalsIgnoreCase("gff")){
 			ext = Extension.GFF;
 			doSQLite = true;
 			type="qualitative";
 			doJSON=true;
 		}else if(extension.equalsIgnoreCase("gtf")){
 			ext = Extension.GFF;
 			doSQLite = true;
 			doJSON=true;
 			type="qualitative";
 		}else if(extension.equalsIgnoreCase("db")){
 			doJSON=true;
 		}else if(extension.equalsIgnoreCase("bam")||
 				extension.equalsIgnoreCase("sam")){
 			ext = Extension.BAM;
 			type="qualitative";
 		}else {
 			try {
 				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),"extension not recognized : "+ext,"ext",trackId, filePath);
 			} catch (IOException e) {
 				logger.error(e);
 			}
 			return;
 		}
 		String database = md5+".db";
 		logger.debug(this.getId()+" start of convertion to SQLite :  "+filePath+" to "+Configuration.getSqliteOutput()+"/"+database);
 
 		boolean wellParsed = false;
 		String error ="";
 		//SQLite
 		if(doSQLite){
 			Exception ex = null;
 			try {
 				ConvertToSQLite convertor = new ConvertToSQLite(filePath, ext,nrAssemblyId);
 				wellParsed = convertor.convert(Configuration.getSqliteOutput()+"/"+database,type);
 			} catch (IOException e1) {
 				logger.error(e1);
 				ex=e1;
 			} catch (ParsingException e1) {
 				logger.error(e1);
 				ex=e1;
 			} catch (InstantiationException e1) {
 				logger.error(e1);
 				ex=e1;
 			} catch (IllegalAccessException e1) {
 				logger.error(e1);
 				ex=e1;
 			} catch (ClassNotFoundException e1) {
 				logger.error(e1);
 				ex=e1;
 			} catch (SQLException e1) {
 				logger.error(e1);
 				ex=e1;
 			} catch (MethodNotFoundException e1) {
 				logger.error(e1);
 				ex=e1;
 			} catch (ExtensionNotRecognisedException e) {
 				logger.error(e);
 				ex=e;
 			}
 			if(null!=ex){
 				for(StackTraceElement el : ex.getStackTrace()){
 					logger.error(el.getClassName()+"."+el.getClassName()+"."+el.getMethodName()+" at line "+el.getLineNumber());
 				}
 			}
 		}
 
 
 		
 
 
 		//JSON 
		if(false){
 			
 			logger.debug(this.getId()+" start of convertion to JSON :  "+database+" to "+Configuration.getJbrowseOutput()+"/"+database);
 			ConvertToJSON convertor = new ConvertToJSON(Configuration.getSqliteOutput()+"/"+database);
 			/**
 			 * String outputPath,String dbName,String ressourceUrl,String trackName
 			 */
 			try {
 				wellParsed = convertor.convert(Configuration.getJbrowseOutput(),database,Configuration.getRessourceURL(),file.getName());
 			} catch (InstantiationException e) {
 				logger.error(e);
 				wellParsed=false;
 				error+=e;
 			} catch (IllegalAccessException e) {
 				logger.error(e);
 				wellParsed=false;
 				error+=e;
 			} catch (ClassNotFoundException e) {
 				logger.error(e);
 				wellParsed=false;
 				error+=e;
 			} catch (SQLException e) {
 				logger.error(e);
 				wellParsed=false;
 				error+=e;
 			} catch (JSONException e) {
 				logger.error(e);
 				wellParsed=false;
 				error+=e;
 			} catch (IOException e) {
 				logger.error(e);
 				wellParsed=false;
 				error+=e;
 			}
 		}
 
 
 
 
 		//////////   FEEDBACK   //////////
 		if(wellParsed){
 			logger.debug(this.getId()+"well parsed");
 			try {
 				RemoteAccess.sendTrackSucceed(Configuration.getFeedbackUrl(),trackId,database,mail,type);
 			} catch (IOException e) {
 				logger.error(e);
 			}
 
 			//DELETING TMPDIR
 			logger.debug("delete tmpdir : "+tmpdir);
 			if(tmpdir!=null && !tmpdir.equalsIgnoreCase("") && !tmpdir.equalsIgnoreCase("tmp")){
 				logger.debug("deleting ...");
 				FileManagement.deleteInTMPDirectory(tmpdir);
 			}
 		} else {
 			try {
 				RemoteAccess.sendTrackErrorMessage(Configuration.getFeedbackUrl(),"parsing error "+error,"parsing", trackId,filePath);
 				return;
 			} catch (IOException e) {
 				logger.error(e);
 			}
 		}
 		logger.debug("end job on track "+trackId+" at "+new Date()+".");
 		long end = System.currentTimeMillis();
 		long time = (end-start);
 		logger.debug("time elapsed : ~"+time/1000+" sec.");
 	}
 
 
 
 
 
 
 
 
 
 	//LOGGER
 
 	public static Logger initLogger(String name) {
 		Logger out = Logger.getLogger(name);
 		out.setAdditivity(false);
 		out.setLevel(Level.DEBUG);
 		PatternLayout layout = new PatternLayout("%d [%t] %-5p %c - %m%n");
 		RollingFileAppender appender = null;
 		try {
 			appender = new RollingFileAppender(layout,Configuration.getLogger(),true);
 		} catch (IOException e) {
 			logger.error(e);
 		}
 		out.addAppender(appender);
 		return out;
 	}
 }
