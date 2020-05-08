 package org.geworkbench.bison.datastructure.bioobjects.sequence;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.net.URL;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * A class to hold information about each individual hit of a Blast database
  * search.
  * 
  * @author zji
  * @version $Id$
  */
 public class BlastObj implements Serializable{
 	 
 	private static final long serialVersionUID = -7617429744234644109L;	 
 
 	private static Log log = LogFactory.getLog(BlastObj.class);
 	
 	/**
 	 * The Database ID of the protein sequence hit in this BlastObj.
 	 */
 	final private String databaseID;
 
 	/**
 	 * The accession number of the protein sequence hit in this BlastObj.
 	 */
 	final private String name;
 	/**
 	 * The description of the protein sequence hit in this BlastObj.
 	 */
 	final private String description;
 	/**
 	 * The percentage of the query that's aligned with protein sequence hit in
 	 * this BlastObj.
 	 */
 	final private int percentAligned;
 
 	/**
 	 * The score of the alignment of query and hit in this BlastObj.
 	 */
 	final private String evalue;
 
 	/**
 	 * whether this BlastObj is included in the MSA analysis
 	 */
 	private boolean include = false;
 
 	/**
 	 * URL of seq.
 	 */
 	private URL seqURL;
 
	final private String detailedAlignment;
 	final private int startPoint;
 	final private int alignmentLength;
 	final private String alignedParts;
 
	public BlastObj(String databaseID, String name, String description,
			String evalue, int startPoint, int alignmentLength, int percentage,
			String alignedParts, String detailedAlignment) {
 		this.databaseID = databaseID;
 		this.description = description;
 		this.name = name;
 		this.evalue = evalue;
 		this.startPoint = startPoint;
 		this.alignmentLength = alignmentLength;
 		this.percentAligned = percentage;
 
 		this.alignedParts = alignedParts;
		this.detailedAlignment = detailedAlignment;
 	}
 
 	/* Get methods for class variables. */
 
 	/**
 	 * Returns the database name of the hit sequence stored in this BlastObj.
 	 * 
 	 * @return the accession number as a String.
 	 */
 	public String getDatabaseID() {
 		return databaseID;
 	}
 
 	/**
 	 * Returns the accession number of the hit sequence stored in this BlastObj.
 	 * 
 	 * @return the accession number as a String.
 	 */
 
 	/**
 	 * Returns the description of the hit protein sequence stored in this
 	 * BlastObj.
 	 * 
 	 * @return the description as a String.
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * Returns the percentage of the query that's aligned with protein sequence
 	 * hit in this BlastObj.
 	 * 
 	 * @return percentAligned as an int.
 	 */
 	public int getPercentAligned() {
 		return percentAligned;
 	}
 
 	/**
 	 * Returns the e-value of the alignment of query and protein sequence hit in
 	 * this BlastObj.
 	 * 
 	 * @return evalue as a String.
 	 */
 	public String getEvalue() {
 		return evalue;
 	}
 
 	/**
 	 * Returns whether this BlastObj is included to add to the project.
 	 * 
 	 * @return include as a Boolean.
 	 */
 	public boolean getInclude() {
 		return include;
 	}
 
 	/**
 	 * Sets whether this BlastObj is included to add to the project.
 	 * 
 	 * @param b,
 	 *            the new include Boolean of this BlastObj.
 	 */
 	public void setInclude(boolean b) {
 		include = b;
 	}
 
 	public void setSeqURL(URL seqURL) {
 		if(this.seqURL!=null) {
 			log.warn("trying to set seqURL again for "+name);
 			return;
 		}
 		this.seqURL = seqURL;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getDetailedAlignment() {
 		return detailedAlignment;
 	}
 
 	public String getAlignedSeq() {
 		return alignedParts;
 	}
 
 	/**
 	 * Create Entrez Programming Utilities query URL from web query URL.
 	 * 
 	 * @param queryUrl
 	 * @return
 	 */
 	private static String EUtilsUrl(String queryUrl) {
 		// parse GI from the url
 		final String proteinUrl = "http://www.ncbi.nlm.nih.gov/protein/";
 		final String nucleotideUrl = "http://www.ncbi.nlm.nih.gov/nucleotide/";
 		int indexQ = queryUrl.indexOf("?");
 		String GI = null; 
 		String databaseName = null;
 		if(indexQ==-1) {
 			log.error("unexpected query URL format :"+queryUrl);
 			return null;
 		} else if(queryUrl.startsWith(proteinUrl)) {
 			GI = queryUrl.substring(proteinUrl.length(), indexQ);
 			databaseName = "protein";
 		} else if(queryUrl.startsWith(nucleotideUrl)) {
 			GI = queryUrl.substring(nucleotideUrl.length(), indexQ);
 			databaseName = "nucleotide";
 		} else {
 			log.error("unexpected query URL format :"+queryUrl);
 			return null;
 		}
 
 		return "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db="+databaseName+"&id="
 				+ GI + "&rettype=fasta";
 	}
 	
 	/**
 	 * getWholeSeq
 	 * 
 	 * @return Object
 	 */
 	public String getWholeSeq() {
 
 		if (seqURL == null)
 			return null;
 
 		log.debug("URL" + seqURL);
 		HttpClient client = new HttpClient();
 		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
 				10, true);
 		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
 				retryhandler);
 		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
 
 		GetMethod getMethod = new GetMethod(EUtilsUrl(seqURL.toString()));
 		String sequenceLabel = null, sequence = null;
 		try {
 			int statusCode = client.executeMethod(getMethod);
 
 			if (statusCode == HttpStatus.SC_OK) {
 				InputStream stream = getMethod.getResponseBodyAsStream();
 				BufferedReader in = new BufferedReader(new InputStreamReader(
 						stream));
 				sequenceLabel = in.readLine();
 				StringBuilder sb = new StringBuilder();
 				String line = null;
 				while ((line = in.readLine()) != null) {
 					if (line.trim().length() > 0)
 						sb.append(line).append('\n');
 				}
 				stream.close();
 				sequence = sb.toString();
 			} else {
 				log.error("E Utilties failed for " + seqURL);
 				log.error("status code=" + statusCode);
 				return null;
 			}
 		} catch (HttpException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			getMethod.releaseConnection();
 		}
 
 		log.debug("label\n" + sequenceLabel);
 		log.debug("sequence\n" + sequence);
 		return sequenceLabel+"\n"+sequence;
 	}
 
 	public int getStartPoint() {
 
 		return startPoint;
 	}
 
 	public int getAlignmentLength() {
 		return alignmentLength;
 	}
 	
 }
