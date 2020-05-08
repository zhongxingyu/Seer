 package de.unihamburg.zbh.fishoracle.server.data;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.sql.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Date;
 
 import org.ensembl.datamodel.CoordinateSystem;
 import org.ensembl.datamodel.Gene;
 import org.ensembl.datamodel.KaryotypeBand;
 import org.ensembl.datamodel.Location;
 
 import org.ensembl.driver.AdaptorException;
 import org.ensembl.driver.CoreDriver;
 import org.ensembl.driver.CoreDriverFactory;
 import org.ensembl.driver.KaryotypeBandAdaptor;
 
 import com.csvreader.CsvReader;
 
 import de.unihamburg.zbh.fishoracle.client.data.Chip;
 import de.unihamburg.zbh.fishoracle.client.data.CopyNumberChange;
 import de.unihamburg.zbh.fishoracle.client.data.Gen;
 import de.unihamburg.zbh.fishoracle.client.data.MetaStatus;
 import de.unihamburg.zbh.fishoracle.client.data.Organ;
 import de.unihamburg.zbh.fishoracle.client.data.PathologicalGrade;
 import de.unihamburg.zbh.fishoracle.client.data.PathologicalStage;
 import de.unihamburg.zbh.fishoracle.client.data.User;
 import de.unihamburg.zbh.fishoracle.client.exceptions.DBQueryException;
 
 /**
  * Fetches various information from the fish oracle database an gene
  * information from the ensembl database using the ensembl Java API.
  * 
  * */
 public class DBQuery {
 
 	//ensembl connection parameters
 	private String ehost = null;
 	private int eport;
 	private String edb = null;
 	private String euser = null;
 	private String epw = null;
 	
 	//fish oracle connection parameters
 	private String fhost = null;
 	private String fdb = null;
 	private String fuser = null;
 	private String fpw = null;
 	
 	/**
 	 * Initializes the database object by fetching the database connection 
 	 * parameters from the database.conf file.
 	 * 
 	 * 
 	 * 
 	 * @param serverPath should contain the realPath of a servlet context to the 
 	 *         database.conf file. e.g.:
 	 *         <p> 
 	 *         <code>new DBQuery(getServletContext().getRealPath("/"));<code>
 	 * 
 	 * */
 	public DBQuery(String serverPath) {
 		
 		try{
 
 	    FileInputStream fStream = new FileInputStream(serverPath + "config" + System.getProperty("file.separator") + "database.conf");
 	    DataInputStream inStream = new DataInputStream(fStream);
 	    BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
 	    
 	    String strLine;
 	    String[] dataStr;
 	    
 	    Boolean ensmbl = false;
 	    Boolean fishoracle = false;   
 	    
 	    while ((strLine = br.readLine()) != null)   {
 		  
 	      Pattern pensmbl = Pattern.compile("^\\[ensembl\\]$");
 		  Matcher mensmbl = pensmbl.matcher(strLine);
 	      
 		  if(mensmbl.find()){
 			  ensmbl = true;  
 			  fishoracle = false; 
 		  }
 		  
 		  Pattern pforacle = Pattern.compile("^\\[fishoracle\\]$");
 		  Matcher mforacle = pforacle.matcher(strLine);
 		  
 		  if(mforacle.find()){
 			  fishoracle = true; 
 			  ensmbl = false; 
 		  }
 		  
 		  Pattern phost = Pattern.compile("^host");
 		  Matcher mhost = phost.matcher(strLine);
 	      
 		  Pattern pport = Pattern.compile("^port");
 		  Matcher mport = pport.matcher(strLine);
 		  
 		  Pattern pdb = Pattern.compile("^db");
 		  Matcher mdb = pdb.matcher(strLine);
 		  
 		  Pattern puser = Pattern.compile("^user");
 		  Matcher muser = puser.matcher(strLine);
 		  
 		  Pattern ppw = Pattern.compile("^pw");
 		  Matcher mpw = ppw.matcher(strLine);
 		  
 		  if(ensmbl){
 			  
 			  if(mhost.find()){
 				  dataStr = strLine.split("=");
 				  ehost = dataStr[1].trim();
 			  }
 			  if(mport.find()){
 				  dataStr = strLine.split("=");
 				  eport = Integer.parseInt(dataStr[1].trim());
 			  }
 			  if(mdb.find()){
 				  dataStr = strLine.split("=");
 				  edb = dataStr[1].trim();
 			  }
 			  if(muser.find()){
 				  dataStr = strLine.split("=");
 				  euser = dataStr[1].trim();
 			  }
 			  if(mpw.find()){
 				  dataStr = strLine.split("=");
 				  epw = dataStr[1].trim();
 			  }
 		  }
 		  if(fishoracle){
 			  
 			  if(mhost.find()){
 				  dataStr = strLine.split("=");
 				  fhost = dataStr[1].trim();
 			  }
 			  if(mdb.find()){
 				  dataStr = strLine.split("=");
 				  fdb = dataStr[1].trim();
 			  }
 			  if(muser.find()){
 				  dataStr = strLine.split("=");
 				  fuser = dataStr[1].trim();
 			  }
 			  if(mpw.find()){
 				  dataStr = strLine.split("=");
 				  fpw = dataStr[1].trim();
 			  }
 		  }
 	    }
 
 	    inStream.close();
 	    } catch (Exception e){
 	    	e.printStackTrace();
 	    	System.err.println("Error: " + e.getMessage());
 	    }
 	}
 	
 	/**
 	 * Looks location information (chromosome, start, end) for an amplicon stable id up.
 	 * 
 	 * @param copyNumberChangeId The stable id of an amplicon.
 	 * @return		An ensembl API location object storing chromosome, start and end of an amplicon. 
 	 * @throws Exception 
 	 * 
 	 * */
 	public Location getLocationForCNCId(String copyNumberChangeId) throws Exception{
 		
 		Connection conn = null;
 		Location loc = null;
 		try{
 			
 			int copyNumberChangeStart = 0;
 			int copyNumberChangeEnd = 0;
 			String copyNumberChangeChr = null;
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 				
 				s.executeQuery("SELECT cnc_segment_chromosome, cnc_segment_start, cnc_segment_end FROM " +
 								"cnc_segment WHERE cnc_segment_stable_id = '" + copyNumberChangeId + "'");
 				ResultSet copyNumberChangeRs = s.getResultSet();
 				
 				while(copyNumberChangeRs.next()){
 					copyNumberChangeChr = copyNumberChangeRs.getString(1);
 					copyNumberChangeStart = copyNumberChangeRs.getInt(2);
 					copyNumberChangeEnd = copyNumberChangeRs.getInt(3);
 					;
 				
 					String locStr = "chromosome:" + copyNumberChangeChr + ":" + copyNumberChangeStart + "-" + copyNumberChangeEnd;
 				
 					loc = new Location(locStr);
 				}
 				copyNumberChangeRs.close();
 				
 				if(loc == null){
 					
 					DBQueryException e = new DBQueryException("Couldn't find the CNC segment with the stable ID " + copyNumberChangeId);
 					throw e;
 				}
 				
 		} catch (DBQueryException e){
 			System.out.println(e.getMessage());
 			throw e;
 		} catch (Exception e) {
 			FishOracleConnection.printErrorMessage(e);
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 			
 		return loc;
 	}
 	
 	/**
 	 * Looks location information (chromosome, start, end) for a gene symbol up.
 	 * 
 	 * @param symbol The gene symbol, that was specified in the search query.
 	 * @return		An ensembl API location object storing chromosome, start and end of a gene. 
 	 * @throws DBQueryException 
 	 * 
 	 * */
 	public Location getLocationForGene(String symbol) throws DBQueryException{
 		Gene gene = null;
 		CoreDriver coreDriver;
 		try {
 			coreDriver = CoreDriverFactory.createCoreDriver(ehost, eport, edb, euser, epw);
 			coreDriver.getConnection();
 		
 			gene = (Gene) coreDriver.getGeneAdaptor().fetchBySynonym(symbol).get(0);
 			
 			coreDriver.closeAllConnections();
 		
 		} catch (AdaptorException e) {
 			e.printStackTrace();
 			System.out.println("Error: " + e.getMessage());
 			System.out.println(e.getCause());
 		} catch (Exception e) {
 			
 			if(e instanceof IndexOutOfBoundsException){
 				throw new DBQueryException("Couldn't find gene with gene symbol " + symbol, e.getCause());
 			}
 		}
 		return gene.getLocation();
 	}
 	
 	/** 
 	 * Looks location information (chromosome, start, end) for a karyoband up.
 	 * 
 	 * @param chr The chromosome number
 	 * @param band The karyoband
 	 * @return		An ensembl API location object storing chromosome, start and end of a chromosome and  karyoband. 
 	 * @throws DBQueryException 
 	 * 
 	 * */
 	public Location getLocationForKaryoband(String chr, String band) throws DBQueryException{
 		CoordinateSystem coordSys = null;
 		KaryotypeBand k = null;
 		CoreDriver coreDriver;
 		try {
 			coreDriver = CoreDriverFactory.createCoreDriver(ehost, eport, edb, euser, epw);
 			coreDriver.getConnection();
 			
 			KaryotypeBandAdaptor kband = coreDriver.getKaryotypeBandAdaptor();
 			
 			coordSys = coreDriver.getCoordinateSystemAdaptor().fetch("chromosome", null);
 			
 			k = (KaryotypeBand) kband.fetch(coordSys, chr, band).get(0);
 			
 			coreDriver.closeAllConnections();
 		
 		} catch (AdaptorException e) {
 			e.printStackTrace();
 			System.out.println("Error: " + e.getMessage());
 			System.out.println(e.getCause());
 		} catch (Exception e) {
 			
 			if(e instanceof IndexOutOfBoundsException){
 				throw new DBQueryException("Couldn't find karyoband " + chr + band, e.getCause());
 			}
 		}
 		return k.getLocation();
 	}
 	
 	
 	/**
 	 * Finds all amplicons that overlap with a given range on a chromosome and returns the 
 	 * maximum range over all overlapping amplicons as an ensembl location object.
 	 * 
 	 * @param chr chromosome number
 	 * @param start Starting position on the chromosome.
 	 * @param end Ending postion on the chromosome.
 	 * @return 		An ensembl API location object storing chromosome, start and end
 	 * 
 	 * */
 	public Location getMaxCNCRange(String chr, int start, int end, Double lowerTh, Double upperTh){
 		Location loc = null;
 		Connection conn = null;
 		String copyNumberChangeChr = chr;
 		int copyNumberChangeStart = start;
 		int copyNumberChangeEnd = end;
 		String qrystr = null;
 		
 		qrystr = "SELECT MIN(cnc_segment_start) as minstart, MAX(cnc_segment_end) as maxend FROM cnc_segment WHERE cnc_segment_chromosome = \"" + copyNumberChangeChr + 
 		"\" AND ((cnc_segment_start <= " + copyNumberChangeStart + " AND cnc_segment_end >= " + copyNumberChangeEnd + ") OR" +
 		" (cnc_segment_start >= " + copyNumberChangeStart + " AND cnc_segment_end <= " + copyNumberChangeEnd + ") OR" +
 	    " (cnc_segment_start >= " + copyNumberChangeStart + " AND cnc_segment_start <= " + copyNumberChangeEnd + ") OR" +
 	    " (cnc_segment_end >= " + copyNumberChangeStart + " AND cnc_segment_end <= " + copyNumberChangeEnd + "))";
 		
 		if(lowerTh == null && upperTh != null){
 			qrystr = qrystr + " AND cnc_segment_mean > '" + upperTh + "'"; 
 		} 
 		if (lowerTh != null && upperTh == null){
 			qrystr = qrystr + " AND cnc_segment_mean < '" + lowerTh + "'";
 		} 
 		if (lowerTh != null && upperTh != null){
 			qrystr = qrystr + " AND (cnc_segment_mean < '" + lowerTh + "' AND " +
 			"cnc_segment_mean > '" + upperTh + "')";
 		}
 			
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			
 			s.executeQuery(qrystr);
 			
 			String locStr;
 			
 			ResultSet rangeRs = s.getResultSet();
 				rangeRs.next();
 				int qstart = rangeRs.getInt(1);
 				int qend = rangeRs.getInt(2);
 			if(qstart == 0 && qend == 0){
 				locStr = "chromosome:" + copyNumberChangeChr + ":" + copyNumberChangeStart + "-" + copyNumberChangeEnd;	
 			} else {
 				locStr = "chromosome:" + copyNumberChangeChr + ":" + qstart + "-" + qend;
 			}
 				
 			loc = new Location(locStr);
 			
 			rangeRs.close();
 			s.close();
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		
 		return loc;
 	}
 	
 	/**
 	 * For a range on a chromosome an array with all overlapping amplicons is returned.
 	 * 
 	 * @param chr chromosome
 	 * @param start Starting position on the chromosome.
 	 * @param end Ending postion on the chromosome.
 	 * @return		Array containing amplicon objects
 	 * 
 	 * */
 	public CopyNumberChange[] getCNCData(String chr,
 											int start, 
 											int end, 
 											Double lowerTh, 
 											Double upperTh,
 											String[] organFilter){
 		
 		String qrystrc = null;
 		String qrystr = null;
 		int copyNumberChangeStart = start;
 		int copyNumberChangeEnd = end;
 		String copyNumberChangeChr = chr;
 		
 		qrystrc = "SELECT count(*) FROM cnc_segment " +
 		"LEFT JOIN microarraystudy ON microarraystudy_id = cnc_segment_microarraystudy_id " +
 		"LEFT JOIN sample_on_chip ON sample_on_chip_id = microarraystudy_sample_on_chip_id " +
 		"LEFT JOIN tissue_sample ON tissue_sample_id = sample_on_chip_tissue_sample_id " +
 		"LEFT JOIN organ ON organ_id = tissue_sample_organ_id " +
 		"WHERE cnc_segment_chromosome = \"" + copyNumberChangeChr + "\" " +
 		"AND ((cnc_segment_start <= " + copyNumberChangeStart + " AND cnc_segment_end >= " + copyNumberChangeEnd + ") OR" +
         " (cnc_segment_start >= " + copyNumberChangeStart + " AND cnc_segment_end <= " + copyNumberChangeEnd + ") OR" +
         " (cnc_segment_start >= " + copyNumberChangeStart + " AND cnc_segment_start <= " + copyNumberChangeEnd + ") OR" +
         " (cnc_segment_end >= " + copyNumberChangeStart + " AND cnc_segment_end <= " + copyNumberChangeEnd + "))";
 		
		qrystr = "SELECT cnc_segment_stable_id, cnc_segment_chromosome, cnc_segment_start, cnc_segment_end FROM " +
 		"cnc_segment LEFT JOIN microarraystudy ON microarraystudy_id = cnc_segment_microarraystudy_id " +
 		"LEFT JOIN sample_on_chip ON sample_on_chip_id = microarraystudy_sample_on_chip_id " +
 		"LEFT JOIN tissue_sample ON tissue_sample_id = sample_on_chip_tissue_sample_id " +
 		"LEFT JOIN organ ON organ_id = tissue_sample_organ_id " +
 		"WHERE cnc_segment_chromosome = \"" + copyNumberChangeChr + "\" " +
 		"AND ((cnc_segment_start <= " + copyNumberChangeStart + " AND cnc_segment_end >= " + copyNumberChangeEnd + ") OR" +
         " (cnc_segment_start >= " + copyNumberChangeStart + " AND cnc_segment_end <= " + copyNumberChangeEnd + ") OR" +
         " (cnc_segment_start >= " + copyNumberChangeStart + " AND cnc_segment_start <= " + copyNumberChangeEnd + ") OR" +
         " (cnc_segment_end >= " + copyNumberChangeStart + " AND cnc_segment_end <= " + copyNumberChangeEnd + "))";
 		
 		
 		if(lowerTh == null && upperTh != null){
 			qrystrc = qrystrc + " AND cnc_segment_mean > '" + upperTh + "'"; 
 			qrystr = qrystr + " AND cnc_segment_mean > '" + upperTh + "'"; 
 		} 
 		if (lowerTh != null && upperTh == null){
 			qrystrc = qrystrc + " AND cnc_segment_mean < '" + lowerTh + "'";
 			qrystr = qrystr + " AND cnc_segment_mean < '" + lowerTh + "'";
 		} 
 		if (lowerTh != null && upperTh != null){
 			qrystrc = qrystrc + " AND (cnc_segment_mean < '" + lowerTh + "' AND " +
 								"cnc_segment_mean > '" + upperTh + "')";
 			qrystr = qrystr + " AND (cnc_segment_mean < '" + lowerTh + "' AND " +
 			"cnc_segment_mean > '" + upperTh + "')";
 		}
 		
 		if(organFilter.length > 0){
 			String organFilterStr = "";
 			for(int i = 0; i < organFilter.length; i++){
 				if(i == 0){
 					organFilterStr = " organ_id = '" + (Integer.parseInt(organFilter[i]) + 1) + "'";
 				} else {
 					organFilterStr = organFilterStr + " OR organ_id = '" + (Integer.parseInt(organFilter[i]) + 1) + "'";
 				}
 			}
 		
 			organFilterStr = " AND (" + organFilterStr + ")";
 			
 			qrystrc = qrystrc + organFilterStr;
 			
 			qrystr = qrystr + organFilterStr;
 
 		}
 		
 		Connection conn = null;
 		CopyNumberChange[] cnc = null;
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			
 			s.executeQuery(qrystrc);
 			
 			ResultSet countRegRs = s.getResultSet();
 			countRegRs.next();
 			int cncCount = countRegRs.getInt(1);
 			
 			countRegRs.close();
 			
 			s.executeQuery(qrystr);
 			
 			ResultSet regRs = s.getResultSet();
 			
 			int count = 0;
 
 			cnc = new CopyNumberChange[cncCount];
 			
 			while(regRs.next()){
 				String newCNCStableId = regRs.getString(1);
 				String newChr = regRs.getString(2);
 				int newStart = regRs.getInt(3);
 				int newEnd = regRs.getInt(4);
 				String newStudyName = regRs.getString(5);
 				
 				cnc[count] = new CopyNumberChange(newCNCStableId, newChr, newStart, newEnd);
 
 				cnc[count].setMicroarrayStudy(newStudyName);
 				
 				count++;
 			}
 			
 			regRs.close();
 			s.close();
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		return cnc;
 	}
 	
 	/*
 	 *TODO apply to new database schema
 	public CopyNumberChange[] getAllCNCData(boolean isAmplicon){
 		
 		String qrystrc = null;
 		String qrystr = null;
 
 		String copyNumberChangeType = null;		
 		
 		if(isAmplicon){
 			copyNumberChangeType = "amplicon";
 			
 		} else {
 			copyNumberChangeType = "delicon";
 		}
 		
 		qrystrc = "SELECT count(*) from " + copyNumberChangeType;
 		
 		qrystr = "SELECT * from " + copyNumberChangeType;
 		
 		Connection conn = null;
 		CopyNumberChange[] cnc = null;
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			
 			s.executeQuery(qrystrc);
 			
 			ResultSet countRegRs = s.getResultSet();
 			countRegRs.next();
 			int cncCount = countRegRs.getInt(1);
 			
 			countRegRs.close();
 			
 			s.executeQuery(qrystr);
 			
 			ResultSet regRs = s.getResultSet();
 			
 			int count = 0;
 
 			cnc = new CopyNumberChange[cncCount];
 			
 			while(regRs.next()){
 				String newCNCStableId = regRs.getString(2);
 				String newChr = regRs.getString(3);
 				int newStart = regRs.getInt(4);
 				int newEnd = regRs.getInt(5);
 				String caseName = regRs.getString(6);
 				String tumorType = regRs.getString(7);
 				int contin = regRs.getInt(8);
 				int cnclevel = regRs.getInt(9);
 				
 				cnc[count] = new CopyNumberChange(newCNCStableId, newChr, newStart, newEnd, caseName, tumorType, contin, cnclevel, isAmplicon);
 				count++;
 			}
 			
 			regRs.close();
 			s.close();
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			//System.exit(1);
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		
 		return cnc;
 	}
 	*/
 	
 	/**
 	 * Fetch all data for a given Amplicon stable id
 	 * 
 	 * @param query Amplicon Stable ID
 	 * @return		Amplicon object conaiting all amplicon data.
 	 * @throws Exception 
 	 * @throws Exception 
 	 * 
 	 * */
 	public CopyNumberChange getCNCInfos(String query) throws Exception{
 		
 		String qrystr;
 		
 		qrystr = "SELECT cnc_segment_id," +
 				" cnc_segment_stable_id," +
 				" cnc_segment_chromosome," +
 				" cnc_segment_start," +
 				" cnc_segment_end," +
 				" cnc_segment_mean," +
 				" cnc_segment_markers," +
 				" microarraystudy_labelling," +
 				" microarraystudy_description, " +
 				" microarraystudy_date_inserted," +
 				" sample_on_chip_chip_name," +
 				" organ_label, " +
 				" patho_stage_label," +
 				" patho_grade_label," +
 				" meta_status_label," +
 				" tissue_sample_sample_id" +
 				" FROM cnc_segment" +
 				" LEFT JOIN microarraystudy ON cnc_segment.cnc_segment_microarraystudy_id = microarraystudy.microarraystudy_id" +
 				" LEFT JOIN sample_on_chip ON microarraystudy.microarraystudy_sample_on_chip_id = sample_on_chip.sample_on_chip_id" +
 				" LEFT JOIN tissue_sample ON sample_on_chip.sample_on_chip_tissue_sample_id = tissue_sample.tissue_sample_id" +
 				" LEFT JOIN organ ON tissue_sample.tissue_sample_organ_id = organ.organ_id" +
 				" LEFT JOIN patho_stage ON tissue_sample.tissue_sample_patho_stage_id = patho_stage.patho_stage_id" +
 				" LEFT JOIN patho_grade ON tissue_sample.tissue_sample_patho_grade_id = patho_grade.patho_grade_id" +
 				" LEFT JOIN meta_status ON tissue_sample.tissue_sample_meta_status_id = meta_status.meta_status_id" +
 				" WHERE cnc_segment_stable_id = '"  + query + "'";
 		
 		Connection conn = null;
 		CopyNumberChange cnc = null;
 		try{
 		
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			
 			s.executeQuery(qrystr);
 			
 			ResultSet cncRs = s.getResultSet();
 			
 			while(cncRs.next()){
 				
 				String cncStableId = cncRs.getString(2);
 				String chr = cncRs.getString(3);
 				int start = cncRs.getInt(4);
 				int end = cncRs.getInt(5);
 				double segmentMean = cncRs.getDouble(6);
 				int markers = cncRs.getInt(7);
 				String microarrayStudy = cncRs.getString(8);
 				String microarrayDescr = cncRs.getString(9);
 				Date importDate = cncRs.getDate(10);
 				String chipName = cncRs.getString(11);
 				String organ = cncRs.getString(12);
 				String pstage = cncRs.getString(13);
 				String pgrade = cncRs.getString(14);
 				String mstatus = cncRs.getString(15);
 				String sampleId = cncRs.getString(16);
 				
 				cnc = new CopyNumberChange(cncStableId,
 											chr,
 											start,
 											end,
 											segmentMean,
 											markers,
 											microarrayStudy,
 											microarrayDescr,
 											importDate,
 											chipName,
 											organ,
 											pstage,
 											pgrade,
 											mstatus,
 											sampleId);
 				
 			}
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 					throw e;
 				}
 			}
 		}
 		return cnc;
 	}
 	
 	/**
 	 * Fetch all data for a gene given by an ensembl stable id.
 	 * 
 	 * @param query Ensembl Stable ID
 	 * @return		Gen object conaiting all gene data.
 	 * @throws Exception 
 	 * 
 	 * */
 	public Gen getGeneInfos(String query) throws Exception {
 		
 		Gen gene = null;
 		
 		CoreDriver coreDriver;
 		try {
 			coreDriver = CoreDriverFactory.createCoreDriver(ehost, eport, edb, euser, epw);
 		
 			coreDriver.getConnection();
 			
 			Gene ensGene =  coreDriver.getGeneAdaptor().fetch(query);
 
 			gene = new Gen();
 				
 				gene.setGenName(ensGene.getDisplayName());
 				gene.setChr(ensGene.getLocation().getSeqRegionName());
 				gene.setStart(ensGene.getLocation().getStart());
 				gene.setEnd(ensGene.getLocation().getEnd());
 				gene.setStrand(Integer.toString(ensGene.getLocation().getStrand()));
 				gene.setAccessionID(ensGene.getAccessionID());
 				gene.setBioType(ensGene.getBioType());
 				
 				if(ensGene.getDescription() == null){
 					gene.setDescription("not available");
 				} else {
 					gene.setDescription(ensGene.getDescription());
 				}
 				
 				gene.setLength(ensGene.getLocation().getLength());
 				
 	    coreDriver.closeAllConnections();
 		
 		} catch (AdaptorException e) {
 			e.printStackTrace();			
 			System.out.println("Error: " + e.getMessage());
 			System.out.println(e.getCause());
 			throw e;
 		}
 
 		
 		return gene;
 	}
 	
 	/**
 	 * For a range on a chromosome an array with all overlapping genes is returned.
 	 * 
 	 * @param chr chromosome
 	 * @param start Starting position on the chromosome.
 	 * @param end ending postion on the chromosome.
 	 * @return 		Array containing gen objects
 	 * 
 	 * */
 	public Gen[] getEnsembleGenes(String chr, int start, int end){
 		
 		Gen[] genes = null;
 		
 		String loc = "chromosome:" + chr + ":" + Integer.toString(start) + "-" + Integer.toString(end);
 		
 		try {
 			
 			CoreDriver coreDriver =
 				CoreDriverFactory.createCoreDriver(ehost, eport, edb, euser, epw);
 
 			coreDriver.getConnection();
 			
 			List<?> ensGenes;
 			try {
 				ensGenes = coreDriver.getGeneAdaptor().fetch(new Location(loc));
 				
 				genes = new Gen[ensGenes.size()];
 				for (int j = 0; j < ensGenes.size(); j++) {
 					Gene g = (Gene) ensGenes.get(j);
 					
 					genes[j] = new Gen(g.getDisplayName(), 
 							           g.getLocation().getSeqRegionName(),
 							           g.getLocation().getStart(),
 							           g.getLocation().getEnd(),
 							           Integer.toString(g.getLocation().getStrand()));
 					
 					genes[j].setAccessionID(g.getAccessionID());
 					
 				}
 			} catch (ParseException e) {
 				e.printStackTrace();
 				System.out.println("Error: " + e.getMessage());
 				System.out.println(e.getCause());
 			}
 			
 			coreDriver.closeAllConnections();
 
 		} catch (AdaptorException e) {
 			e.printStackTrace();
 			System.out.println("Error: " + e.getMessage());
 			System.out.println(e.getCause());
 		}
 		return genes;
 	}
 	
 	/**
 	 * For a range on a chromosome an array with all overlapping karyobands is returned.
 	 * 
 	 * @param chr Chromosome
 	 * @param start Starting position on the chromosome.
 	 * @param end Ending postion on the chromosome.
 	 * @return 		Array containing karyoband objects.
 	 * 
 	 * */
 	public Karyoband[] getEnsemblKaryotypes(String chr, int start, int end){
 
 	        Karyoband[] karyoband = null;
 		
 			CoreDriver coreDriver;
 			try {
 				coreDriver = CoreDriverFactory.createCoreDriver(ehost, eport, edb, euser, epw);
 
 				coreDriver.getConnection();
 			
 				String loc = "chromosome:" + chr + ":" + Long.toString(start) + "-" + Long.toString(end);
 				
 				KaryotypeBandAdaptor ktba = coreDriver.getKaryotypeBandAdaptor();
 				
 				List<?> ensChrs; 
 				
 				ensChrs = ktba.fetch(loc);
 			
 				karyoband = new Karyoband[ensChrs.size()];
 				for (int i = 0; i < ensChrs.size(); i++) {
 				
 					KaryotypeBand k = (KaryotypeBand) ensChrs.get(i);
 								
 					karyoband[i] = new Karyoband(k.getLocation().getSeqRegionName(),
 												k.getBand(), 
 												k.getLocation().getStart(), 
 												k.getLocation().getEnd());
 				}
 				
 				coreDriver.closeAllConnections();
 			
 			} catch (AdaptorException e) {
 				e.printStackTrace();
 				System.out.println("Error: " + e.getMessage());
 				System.out.println(e.getCause());
 			}			
 			return karyoband;
 	}
 	
 	/* USER DATA */
 	
 	public User getUserData(String userName, String pw) throws Exception{
 		Connection conn = null;
 		User user = null;
 		
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			
 			s.executeQuery("SELECT * FROM user WHERE username = '" + userName + "' AND password = '" + SimpleSHA.SHA1(pw) + "'");
 			
 			ResultSet userRs = s.getResultSet();
 			
 			int id = 0;
 			String fistName = null;
 			String lastName = null;
 			String dbUserName = null;
 			String email = null;
 			Boolean isActive = null;
 			Boolean isAdmin = null;
 			
 			while(userRs.next()){
 				
 				id = userRs.getInt(1);
 				fistName = userRs.getString(2);
 				lastName = userRs.getString(3);
 				dbUserName = userRs.getString(4);
 				email = userRs.getString(5);
 				isActive = userRs.getBoolean(7);
 				isAdmin = userRs.getBoolean(8);
 			}
 			
 			if(id == 0){
 				
 				throw new DBQueryException("User name or password incorrect!");
 				
 			}
 			if(isActive == false){
 				
 				throw new DBQueryException("Your account has not been activated. If you registered recently" +
 						                    " this means that your acount has not been verified yet. Just try to log in later." +
 						                    " If your account has been deactivated or your registration was more than 3 days ago" +
 						                    " then contact the webmaster.");
 				
 			}
 			
 			user = new User(id, fistName, lastName, dbUserName, email, isActive, isAdmin);
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		return user;	
 	}
 	
 	public void insertUserData(User user) throws Exception{
 		Connection conn = null;
 		
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			
 			s.executeQuery("SELECT count(*) FROM user where username = '" + user.getUserName() + "'");
 			
 			ResultSet countRs = s.getResultSet();
 			countRs.next();
 			int userCount = countRs.getInt(1);
 			
 			if(userCount == 0){
 			
 			s.executeUpdate("INSERT INTO user (first_name, last_name, username, email, password, isactive, isadmin) VALUES" +
 							" ('" + user.getFirstName() + "', '" + user.getLastName() + "', '" + user.getUserName() +
 							"', '" + user.getEmail() + "', '" + SimpleSHA.SHA1(user.getPw()) + "', '" + user.getIsActive() + 
 							"', '"+ user.getIsAdmin() + "')");
 			} else {
 				
 				
 				 throw new DBQueryException("User name is already taken! Choose another one.");
 				
 			}
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 	}
 	
 	public User[] fetchAllUsers() throws Exception{
 		
 		Connection conn = null;
 		User[] users = null;
 		
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			s.executeQuery("SELECT count(*) FROM user");
 			
 			ResultSet countRs = s.getResultSet();
 			countRs.next();
 			int userCount = countRs.getInt(1);
 			
 			s.executeQuery("SELECT user_id, first_name, last_name, username, email, isActive, isadmin  FROM user");
 			
 			ResultSet userRs = s.getResultSet();
 			
 			int id = 0;
 			String fistName = null;
 			String lastName = null;
 			String dbUserName = null;
 			String email = null;
 			Boolean isActive = null;
 			Boolean isAdmin = null;
 			
 			users = new User[userCount];
 			int i = 0;
 			
 			while(userRs.next()){
 				
 				id = userRs.getInt(1);
 				fistName = userRs.getString(2);
 				lastName = userRs.getString(3);
 				dbUserName = userRs.getString(4);
 				email = userRs.getString(5);
 				isActive = userRs.getBoolean(6);
 				isAdmin = userRs.getBoolean(7);
 				
 				User user = new User(id, fistName, lastName, dbUserName, email, isActive, isAdmin);
 				
 				users[i] = user;
 				
 				i++;
 			}
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		
 		return users;
 	}
 	
 	public int setIsActive(int id, String isActiveOrIsAdmin, String activeOrAdmin) throws Exception{
 		
 		Connection conn = null;
 		String queryStr = null;
 		int activate = 0;
 		
 		try{
 		
 			if(isActiveOrIsAdmin.equals("true")){
 				
 				activate = 0;
 				
 			} 
 			if(isActiveOrIsAdmin.equals("false")){
 				
 				activate = 1;
 				
 			}
 			
 			if(activeOrAdmin.equalsIgnoreCase("isactive")){
 				
 				queryStr = "update user SET isactive = '" + activate + "' where user_id = '" + id + "'";
 				
 			} else if(activeOrAdmin.equalsIgnoreCase("isadmin")){
 				
 				queryStr = "update user SET isadmin = '" + activate + "' where user_id = '" + id + "'";
 				
 			}
 
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			
 			s.executeUpdate(queryStr);
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		
 		return activate;
 	}
 	
 	public Chip[] fetchAllChipData() throws Exception{
 		Connection conn = null;
 		Chip[] chips = null;
 		
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			s.executeQuery("SELECT count(*) FROM chip");
 			
 			ResultSet countRs = s.getResultSet();
 			countRs.next();
 			int chipCount = countRs.getInt(1);
 			
 			s.executeQuery("SELECT *  FROM chip");
 			
 			ResultSet chipRs = s.getResultSet();
 			
 			String ChipName = null;
 			String type = null;
 			String cdfFileName = null;
 			
 			chips = new Chip[chipCount];
 			int i = 0;
 			
 			while(chipRs.next()){
 				ChipName = chipRs.getString(1);
 				type = chipRs.getString(2);
 				cdfFileName = chipRs.getString(3);
 				
 				chips[i] = new Chip(ChipName, type, cdfFileName);
 				i++;
 			}
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		return chips;
 	}
 	
 	public Organ[] fetchAllEnabledOrganData() throws Exception{
 		Connection conn = null;
 		Organ[] organs = null;
 		
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			s.executeQuery("SELECT count(*) FROM organ WHERE organ_activity = 'enabled'");
 			
 			ResultSet countRs = s.getResultSet();
 			countRs.next();
 			int organCount = countRs.getInt(1);
 			
 			s.executeQuery("SELECT *  FROM organ WHERE organ_activity = 'enabled'");
 			
 			ResultSet organRs = s.getResultSet();
 			
 			int id;
 			String label = null;
 			String activity = null;
 			
 			organs = new Organ[organCount];
 			int i = 0;
 			
 			while(organRs.next()){
 				id = organRs.getInt(1);
 				label = organRs.getString(2);
 				activity = organRs.getString(3);
 				
 				organs[i] = new Organ(id, label, activity);
 				i++;
 			}
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		return organs;
 	}
 	
 	public PathologicalStage[] fetchAllEnabledPathologicalStageData() throws Exception{
 		Connection conn = null;
 		PathologicalStage[] pstages = null;
 		
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			s.executeQuery("SELECT count(*) FROM patho_stage WHERE patho_stage_activity = 'enabled'");
 			
 			ResultSet countRs = s.getResultSet();
 			countRs.next();
 			int pstageCount = countRs.getInt(1);
 			
 			s.executeQuery("SELECT *  FROM patho_stage WHERE patho_stage_activity = 'enabled'");
 			
 			ResultSet pstageRs = s.getResultSet();
 			
 			int id;
 			String label = null;
 			String activity = null;
 			
 			pstages = new PathologicalStage[pstageCount];
 			int i = 0;
 			
 			while(pstageRs.next()){
 				id = pstageRs.getInt(1);
 				label = pstageRs.getString(2);
 				activity = pstageRs.getString(3);
 				
 				pstages[i] = new PathologicalStage(id, label, activity);
 				i++;
 			}
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		return pstages;
 	}
 	
 	public PathologicalGrade[] fetchAllEnabledPathologicalGradeData() throws Exception{
 		Connection conn = null;
 		PathologicalGrade[] pgrades = null;
 		
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			s.executeQuery("SELECT count(*) FROM patho_grade WHERE patho_grade_activity = 'enabled'");
 			
 			ResultSet countRs = s.getResultSet();
 			countRs.next();
 			int pgradeCount = countRs.getInt(1);
 			
 			s.executeQuery("SELECT *  FROM patho_grade WHERE patho_grade_activity = 'enabled'");
 			
 			ResultSet pgradeRs = s.getResultSet();
 			
 			int id;
 			String label = null;
 			String activity = null;
 			
 			pgrades = new PathologicalGrade[pgradeCount];
 			int i = 0;
 			
 			while(pgradeRs.next()){
 				id = pgradeRs.getInt(1);
 				label = pgradeRs.getString(2);
 				activity = pgradeRs.getString(3);
 				
 				pgrades[i] = new PathologicalGrade(id, label, activity);
 				i++;
 			}
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		return pgrades;
 	}
 	
 	public MetaStatus[] fetchAllEnabledMetaStatusData() throws Exception{
 		Connection conn = null;
 		MetaStatus[] mstati = null;
 		
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement s = conn.createStatement();
 			s.executeQuery("SELECT count(*) FROM meta_status WHERE meta_status_activity = 'enabled'");
 			
 			ResultSet countRs = s.getResultSet();
 			countRs.next();
 			int mstatusCount = countRs.getInt(1);
 			
 			s.executeQuery("SELECT *  FROM meta_status WHERE meta_status_activity = 'enabled'");
 			
 			ResultSet mstatusRs = s.getResultSet();
 			
 			int id;
 			String label = null;
 			String activity = null;
 			
 			mstati = new MetaStatus[mstatusCount];
 			int i = 0;
 			
 			while(mstatusRs.next()){
 				id = mstatusRs.getInt(1);
 				label = mstatusRs.getString(2);
 				activity = mstatusRs.getString(3);
 				
 				mstati[i] = new MetaStatus(id, label, activity);
 				i++;
 			}
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		return mstati;
 	}
 	
 	public int createNewStudy(String studyName,
 								String chipType,
 								String tissue,	
 								String pstage,	
 								String pgrade,
 								String metaStatus,
 								String description,
 								String sampleId,
 								int userId) throws Exception{
 		
 		Connection conn = null;
 		
 		int mstudyId;
 		
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement idStatement = conn.createStatement();
 			idStatement.executeQuery("SELECT organ_id FROM organ WHERE organ_label = '" + tissue + "'");
 			
 			ResultSet idRs = idStatement.getResultSet();
 			idRs.next();
 			int organId = idRs.getInt(1);
 
 			idStatement.executeQuery("SELECT patho_stage_id FROM patho_stage WHERE patho_stage_label = '" + pstage + "'");
 			
 			idRs = idStatement.getResultSet();
 			idRs.next();
 			int pstageId = idRs.getInt(1);
 			
 			idStatement.executeQuery("SELECT patho_grade_id FROM patho_grade WHERE patho_grade_label = '" + pgrade + "'");
 			
 			idRs = idStatement.getResultSet();
 			idRs.next();
 			int pgradeId = idRs.getInt(1);
 			
 			idStatement.executeQuery("SELECT meta_status_id FROM meta_status WHERE meta_status_label = '" + metaStatus + "'");
 			
 			idRs = idStatement.getResultSet();
 			idRs.next();
 			int mstatusId = idRs.getInt(1);
 			
 			idStatement.close();
 			
 			Statement tissueStatement = conn.createStatement();
 			tissueStatement.executeUpdate("INSERT INTO tissue_sample (tissue_sample_sample_id," +
 										" tissue_sample_organ_id, tissue_sample_patho_stage_id," +
 										" tissue_sample_patho_grade_id, tissue_sample_meta_status_id) VALUES " +
 										"( '" + sampleId + "', " + organId + ", " + pstageId + ", " + pgradeId + ", " + mstatusId + ")");
 			
 			ResultSet tissueRs = tissueStatement.getGeneratedKeys();
 			tissueRs.next();
 			int tissueId = tissueRs.getInt(1);
 			
 			tissueStatement.close();
 
 			Calendar calendar = Calendar.getInstance();
 	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	        
 	        Statement mstudyStatement = conn.createStatement();
 			mstudyStatement.executeUpdate("INSERT INTO microarraystudy (microarraystudy_date_inserted," +
 										" microarraystudy_labelling, microarraystudy_description," +
 										" microarraystudy_link_preprocessed_data, microarraystudy_user_id," +
 										" microarraystudy_normalized_with, microarraystudy_sample_on_chip_id) VALUES " +
 										"('" + dateFormat.format(calendar.getTime()) + "', '" + studyName + "', '" + description + 
 										"', 'null', '" + userId + "', 'cna', -1)");
 			
 			ResultSet mstudyRs = mstudyStatement.getGeneratedKeys();
 			mstudyRs.next();
 			mstudyId = mstudyRs.getInt(1);
 			
 			Statement sampleOnChipStatement = conn.createStatement();
 			sampleOnChipStatement.executeUpdate("INSERT INTO sample_on_chip (sample_on_chip_chip_name, " +
 												"sample_on_chip_tissue_sample_id, sample_on_chip_user_id, " +
 												"sample_on_chip_date_inserted, sample_on_chip_celfile_name, " +
 												"sample_on_chip_preprocessed, sample_on_chip_microarray_study_id) VALUES " +
 												"('"+ chipType +"', '" + tissueId + "', '" + userId + "', '" + dateFormat.format(calendar.getTime()) + 
 												"', 'null', 'cna', '" + mstudyId + "')");
 			
 			ResultSet sampleOnChipRs = sampleOnChipStatement.getGeneratedKeys();
 			sampleOnChipRs.next();
 			int sampleOnChipId = sampleOnChipRs.getInt(1);
 			
 			sampleOnChipStatement.close();
 			
 			Statement mstudyUpdateStatement = conn.createStatement();
 			mstudyStatement.executeUpdate("UPDATE microarraystudy SET microarraystudy_sample_on_chip_id = '" + sampleOnChipId + 
 										"' WHERE microarraystudy_id = '" + mstudyId + "'");
 			mstudyUpdateStatement.close();
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		return mstudyId;
 	}
 	
 	public void insertCNCs(String fileName, String path,  int studyId) throws Exception{
 		Connection conn = null;
 		
 		try{
 			
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement cncidStatement = conn.createStatement();
 			cncidStatement.executeQuery("SELECT cnc_segment_stable_id, MID(cnc_segment_stable_id, 4) + 0 as id FROM cnc_segment " +
 										"ORDER BY id DESC LIMIT 1");
 			
 			ResultSet cncRs = cncidStatement.getResultSet();
 			
 			int sid;
 			
 			if(cncRs.next()){
 				sid = Integer.parseInt(cncRs.getString(1).substring(3));
 			} else {
 				sid = 0;
 			}
 			Statement cncStatement = conn.createStatement();
 			
 			Calendar calendar = Calendar.getInstance();
 	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	        
 			CsvReader reader = new CsvReader(path + fileName);
 			reader.setDelimiter('\t');
 			reader.readHeaders();
 
 			while (reader.readRecord())
 			{
 				//String ID = reader.get("ID");
 				String chr = reader.get("chr");
 				String start = reader.get("start");
 				String end = reader.get("end loc");
 				String markers = reader.get("markers");
 				String segmentMean = reader.get("segment mean");
 				
 				sid++;
 				
 				cncStatement.executeUpdate("INSERT INTO cnc_segment (cnc_segment_stable_id, cnc_segment_chromosome, cnc_segment_start, " +
 											"cnc_segment_end, cnc_segment_mean, cnc_segment_markers, " +
 											"cnc_segment_import_date, cnc_segment_microarraystudy_id) VALUES " +
 											"('CNC" + sid + "', '" + chr + "', '" + start + "', '" + end + "', '" + segmentMean + "', " +
 											"'" + markers + "', '" + dateFormat.format(calendar.getTime()) + "', '" + studyId + "')");
 			
 			}
 
 			reader.close();
 			
 			cncStatement.close();
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			throw e;
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 	}
 	
 	public boolean isAccessable(String page, User user){
 		Connection conn = null;
 		
 		boolean access = false;
 		
 		try {
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement pageAccessStatement = conn.createStatement();
 			pageAccessStatement.executeQuery("SELECT area_access_user_id, (NOW() - area_access_table_time) / 60 AS timeleft FROM area_access WHERE area_access_area_name = '" + page + "'");
 		
 			ResultSet pageAccessRs = pageAccessStatement.getResultSet();
 			//do we get a result? if yes, the page is occupied, else we can use it.
 			if(pageAccessRs.next()){
 				int paUserId = pageAccessRs.getInt(1);
 				
 				double minutesLeft = pageAccessRs.getDouble(2);
 				
 				// check if the timestamp is still valid. if not, delete the lock and use the page
 				if(minutesLeft > 1){
 					access = true;
 					pageAccessStatement.executeUpdate("DELETE FROM area_access WHERE area_access_area_name = '" + page + "' AND " +
 							"'" + user.getId() + "'");
 					
 					pageAccessStatement.executeUpdate("INSERT INTO area_access (area_access_area_name, area_access_user_id) " +
 							"VALUES ('" + page + "', '" + user.getId() + "')");
 				}
 				// check if the user has permission
 				if(user.getId() == paUserId){
 					access = true;
 				}
 				
 			} else {
 				access = true;
 				pageAccessStatement.executeUpdate("INSERT INTO area_access (area_access_area_name, area_access_user_id) " +
 												"VALUES ('" + page + "', '" + user.getId() + "')");
 			}
 			
 		} catch (Exception e) {
 			FishOracleConnection.printErrorMessage(e);
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		
 		return access;
 	}
 	
 	public void unlockPage(String page, User user){
 		Connection conn = null;
 		
 		try {
 			conn = FishOracleConnection.connect(fhost, fdb, fuser, fpw);
 			
 			Statement pageAccessStatement = conn.createStatement();
 			pageAccessStatement.executeUpdate("DELETE FROM area_access WHERE area_access_area_name = '" + page + "' AND " +
 											"area_access_user_id = '" + user.getId() + "'");
 			
 		} catch (Exception e) {
 			FishOracleConnection.printErrorMessage(e);
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 	}
 }
