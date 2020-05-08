 /*
  * (copyright) 2012 United States Government, as represented by the 
  * Secretary of Defense.  All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions 
  * are met:
  * 
  * - Redistributions of source code must retain the above copyright 
  * notice, this list of conditions and the following disclaimer. 
  * 
  * - Redistributions in binary form must reproduce the above copyright 
  * notice, this list of conditions and the following disclaimer in the 
  * documentation and/or other materials provided with the distribution. 
  * 
  * - Neither the name of the U.S. Government nor the names of its 
  * contributors may be used to endorse or promote products derived from 
  * this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
  * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
  * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY 
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE. 
  */
 package gov.niarl.hisAppraiser.integrityReport;
 
 import gov.niarl.his.xsd.JAXBContextIntegrity_Report_Manifest_v1_0String;
 import gov.niarl.his.xsd.integrity_Report_v1_0.org.trustedcomputinggroup.xml.schema.core_Integrity_v1_0_1.DigestValueType;
 import gov.niarl.his.xsd.integrity_Report_v1_0.org.trustedcomputinggroup.xml.schema.core_Integrity_v1_0_1.ValueType;
 import gov.niarl.his.xsd.integrity_Report_v1_0.org.trustedcomputinggroup.xml.schema.integrity_Report_v1_0.PcrCompositeType.PcrValue;
 import gov.niarl.his.xsd.integrity_Report_v1_0.org.trustedcomputinggroup.xml.schema.integrity_Report_v1_0.QuoteType;
 import gov.niarl.his.xsd.integrity_Report_v1_0.org.trustedcomputinggroup.xml.schema.integrity_Report_v1_0.ReportType;
 import gov.niarl.his.xsd.integrity_Report_v1_0.org.trustedcomputinggroup.xml.schema.integrity_Report_v1_0.SnapshotType;
 import gov.niarl.his.xsd.integrity_Report_v1_0.org.trustedcomputinggroup.xml.schema.simple_object_v1_0_.SimpleObjectType;
 import gov.niarl.his.xsd.integrity_Report_v1_0.org.trustedcomputinggroup.xml.schema.simple_object_v1_0_.ValuesType;
 
 import gov.niarl.hisAppraiser.hibernate.dao.HisAuditDao;
 import gov.niarl.hisAppraiser.hibernate.dao.HisMachineCertDao;
 import gov.niarl.hisAppraiser.hibernate.dao.MLEDao;
 import gov.niarl.hisAppraiser.hibernate.domain.AuditLog;
 
import gov.niarl.hisAppraiser.Constants;
 import gov.niarl.hisAppraiser.util.HisUtil;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.Signature;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.log4j.Logger;
 
 /**
  * This class does the main work with the report when it comes into the
  * web services or login modules.
  * @author syelama
  * @version Crossbow
  *
  */
 public class HisReportValidator {
 	private static Logger logger = Logger.getLogger(HisReportValidator.class);
 	static {
 		// For signature
 		HisUtil.loadBouncyCastleProvider();
 	}
 
 	/**
 	 * This is used in the database previous_differences field to separate PCR 
 	 * numbers.
 	 */
 	public final static String DIFFERENCE_SEPARATOR = "|";
 	/**
 	 * This is used in the report_errors field in the to separate errors 
 	 * generated below.
 	 */
 	public final static String ERROR_SEPARATOR = "----------------------------------------------------------------------";
 
 	public static final int PCR_HASH_SIZE = 20;
 	public static final int PCR_MAX_NUM = 24;
 
 	HisReportData hisReportData;
 	String reportString;
 	X509Certificate machineCertificate;
 
 	byte[] lastDigestData = null;
 
 	boolean nonceMatch = false;
 	boolean machineNameMatch = false;
 	boolean digestMatch = false;
 	boolean signatureVerified = false;
 	String previousReportDifferences = "";
 
 	ArrayList<String> errors = new ArrayList<String>();
 	ArrayList<String> compareErrors = new ArrayList<String>();
 	HisReportValidator previousReportValidator;
 	boolean FIRST_IR;
 	String currentPcrIMLMask;
 
 	/**
 	 * The constructor does all the work for verifying the report and returning 
 	 * useful information.
 	 * @param reportString Only required parameter to the constructor 
 	 * containing the XML integrity report.
 	 * @param nonceInput The nonce provided to the client in the first 
 	 * stage of submitting an integrity report.
 	 * @param pcrSelectInput The PCR select provided to the client in the 
 	 * first stage of submitting an integrity report.
 	 * @param machineNameInput The machine name provided by the client in the  
 	 * first stage of submitting an integrity report.
 	 * @param machineCertificate The X509Certificate stored in the database for
 	 * the machine.
 	 * @param previousReportString Previous XML integrity report, if any, 
 	 * stored in the database for the machine.
 	 */
 	public HisReportValidator(String reportString, byte[] nonceInput, byte[] pcrSelectInput, String machineNameInput, X509Certificate machineCertificate, String previousReportString) {
 		try {
 			// replace nulls for null pointer exceptions
 			if (nonceInput == null) {
 				nonceInput = new byte[1];
 			}
 			if (pcrSelectInput == null) {
 				pcrSelectInput = new byte[1];
 			}
 			if (machineNameInput == null) {
 				machineNameInput = "";
 			}
 			//Can't do this for machineCertificate
 			if (previousReportString == null) {
 				previousReportValidator = null;
 				previousReportString = "";
 			}
 
 			this.FIRST_IR = true;
 			this.reportString = reportString;
 			this.machineCertificate = machineCertificate;
 			this.currentPcrIMLMask = new MLEDao().getPcrIMLMask(machineNameInput);
 			try {
 				hisReportData = new HisReportData(reportString);
 				//drop null reports
 				QuoteType quote = hisReportData.getQuote();
 			} catch (Exception e) {
 				throw new HisReportException(e);
 			}
 
 			if (hisReportData.getQuoteData().size() > 1)
 				errors.add("Multiple quote data");
 
 			/*-------------------------------------------------------------------------------------------------------------------------*/
 			/*-------------------------------------------------Signature Verification -------------------------------------------------*/
 			/*-------------------------------------------------------------------------------------------------------------------------*/
 			digestMatch = false;
 			if (hisReportData.compareDigest(computeDigest())) {
 				digestMatch = true;
 				logger.debug("Found digest match location 1.");
 			}
 			//*****************************************************************************		
 			//*****************************************************************************		
 			//Linux and Windows compute this differently				
 			//*****************************************************************************		
 			//*****************************************************************************		
 
 			//Try size of select 2
 			if (digestMatch == false && hisReportData.getPcrSizeOfSelect() != 2) {
 				hisReportData.substitutePcrSizeOfSelect(2);
 
 				if (hisReportData.compareDigest(computeDigest())) {
 					digestMatch = true;
 					logger.debug("Found digest match location 2.");
 				}
 			}
 
 			//Try size of select 3
 			if (digestMatch == false && hisReportData.getPcrSizeOfSelect() != 3) {
 				hisReportData.substitutePcrSizeOfSelect(3);
 
 				if (hisReportData.compareDigest(computeDigest())) {
 					digestMatch = true;
 					logger.debug("Found digest match location 3.");
 				}
 			}
 
 			//Try to zero out the last byte 
 			if (digestMatch == false) {
 				hisReportData.substitutePcrSizeOfSelect(3);
 				byte[] generatedPcrSelect = hisReportData.generatePcrSelect();
 				generatedPcrSelect[2] = 0;
 				hisReportData.substitutePcrSelect(generatedPcrSelect);
 
 				if (hisReportData.compareDigest(computeDigest())) {
 					digestMatch = true;
 					logger.debug("Found digest match location 4.");
 				}
 			}
 
 			if (digestMatch == false) {
 				errors.add("Computed digest value does not match digest value in report.");
 				digestMatch = false;
 			}
 
 			if (HisUtil.hexString(hisReportData.getNonce()).equalsIgnoreCase(HisUtil.hexString(nonceInput))) {
 				nonceMatch = true;
 			} else {
 				errors.add("Nonce does not match nonce in report.");
 				nonceMatch = false;
 			}
 
 			if (hisReportData.getMachineName().equalsIgnoreCase(machineNameInput)) {
 				machineNameMatch = true;
 			} else {
 				errors.add("Report machine name does not equal given machine name. " + hisReportData.getMachineName() + " " + machineNameInput);
 				machineNameMatch = false;
 			}
 
 			if (machineCertificate == null) {
 				errors.add(machineNameInput + " certificate not uploaded. Machine must be reenrolled.");
 			} else {
 				Exception exception = null;
 				try {
 					X509Certificate privacyCaCert = new HisMachineCertDao().getPrivacyCaCert();
 					if (privacyCaCert != null)
 						machineCertificate.verify(privacyCaCert.getPublicKey());
 				} catch (Exception e) {
 					exception = e;
 				}
 				if (exception != null) {
 					errors.add("Machine certificate was not signed by the privacy CA.");
 					machineCertificate = null;
 				}
 			}
 
 			if (!digestMatch || !nonceMatch || !machineNameMatch || machineCertificate == null) {
 				errors.add("Signature not verified due to non-matching machine name, nonce or digest.");
 				signatureVerified = false;
 			} else {
 				if (verifySignature()) {
 					signatureVerified = true;
 				} else {
 					signatureVerified = false;
 					errors.add("Signature did not verify.");
 				}
 			}
 
 			/*-------------------------------------------------Appraiser Functionality-------------------------------------------------*/
 			if (previousReportString.length() > 0) {
 				comparePreviousReport(previousReportString);
 			} else {
 				logger.info("HisReportParser: No previous report for comparison. Report ID:" + hisReportData.getReportID());
 			}
 
 			validateMeasurements(reportString);
 		} catch (HisReportException hisReportException) {
 			logger.fatal(hisReportException, hisReportException);
 			throw hisReportException;
 		} catch (Exception e) {
 			logger.fatal(e, e);
 			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 			PrintStream printStream = new PrintStream(byteArrayOutputStream);
 			e.printStackTrace(printStream);
 			errors.add(byteArrayOutputStream.toString());
 		}
 	}
 
 	/**
 	 * Returns true if the validated report contains the first
 	 * part of measurements; returns false otherwise.
 	 * @return True/False depending on the type of report validated
 	 */
 	boolean isFirstIR() {
 		return FIRST_IR;
 	}
 
 	/**
 	 * Returns the value of the pcrIMLMask used to validate the PCRs.
 	 * @return pcrIMLMask used to validate the PCRs
 	 */
 	String getPcrIMLMask() {
 		return currentPcrIMLMask;
 	}
 
 	/**
 	 * Reads each SnapshoCollection from the integrity report,
 	 * extends measures inside them and compare the result with
 	 * the element PcrHash and the PCR value read from the
 	 * Quote. If values match it sets the field result of
 	 * attestation request to TRUSTED.
 	 * @param attestRequest The attestation request to be fulfilled
 	 */
 	public void validateMeasurements(String reportString) {
 		boolean[] snapFound = new boolean[PCR_MAX_NUM];
 		for (int i = 0; i < PCR_MAX_NUM; i++)
 			snapFound[i] = false;
 
 		
 		ReportType report = null;
 		Unmarshaller unmarshaller;
 		try {
 			InputStream stream = new ByteArrayInputStream(reportString.getBytes());
 			JAXBContext context = JAXBContext.newInstance(JAXBContextIntegrity_Report_Manifest_v1_0String.contextString + ":gov.niarl.his.xsd.integrity_Report_v1_0.org.trustedcomputinggroup.xml.schema.simple_object_v1_0_");
 			unmarshaller = context.createUnmarshaller();
 			report = ((JAXBElement<ReportType>) unmarshaller.unmarshal(stream)).getValue();
 
 			String[] splittedReportId = report.getID().split("-");
 			FIRST_IR = true;
 			if (splittedReportId.length > 2)
 				FIRST_IR = !splittedReportId[splittedReportId.length - 2].equals("continue");
 
 			if (!FIRST_IR && previousReportValidator == null) {
 				errors.add("Report type \"continue\" but no previous report found");
 				return;
 			}
 
 			String hostName = splittedReportId[0];
 			AuditLog lastAuditLog = new HisAuditDao().getLastAuditLog(hostName);
 
 			if (!FIRST_IR && !lastAuditLog.getPcrIMLMask().equals(this.currentPcrIMLMask)) {
 				errors.add("Report type \"continue\" but pcrIMLMask is changed");
 				return;
 			}
 
 			List<PcrValue> pcrValues = report.getQuoteData().get(0).getQuote().getPcrComposite().getPcrValue();
 
			byte[] pcrIMLMask = HisUtil.unHexString(this.currentPcrIMLMask);
			int intPcrIMLMask = (pcrIMLMask[2] & 0xFF) | ((pcrIMLMask[1] & 0xFF) << 8) | ((pcrIMLMask[0] & 0xFF) << 16);

 			for (SnapshotType snapCollection : report.getSnapshotCollection()) {
 				List<ValueType> values = snapCollection.getValues();
 
 				SimpleObjectType objects;
 				DigestValueType hash;
 
 				BigInteger pcrNumber = snapCollection.getPcrHash().get(0).getNumber();
				if ((intPcrIMLMask & (0x00800000 >> pcrNumber.intValue())) == 0)
					continue;

 				String hashString;
 
 				MessageDigest md = MessageDigest.getInstance("SHA-1");
 				byte[] pcr = snapCollection.getPcrHash().get(0).getStartHash();
 
 				boolean startHashMatch = true;
 				if (previousReportValidator != null)
 					startHashMatch = previousReportValidator.getPcrValue(pcrNumber.intValue()).equals(HisUtil.hexString(pcr));
 
 				if ((FIRST_IR && !HisUtil.hexString(pcr).equals("0000000000000000000000000000000000000000")) ||
 				    (!FIRST_IR && !startHashMatch)) {
 					errors.add("PCR " + pcrNumber + ": Unexpected value of StartHash");
 				}
 
 				for (int i = 0; i < values.size(); i++) {
 					objects = ((JAXBElement<SimpleObjectType>) values.get(i).getAny()).getValue();
 
 					ValuesType tmp = objects.getObjects().get(0);
 					hash = tmp.getHash().get(0);
 					hashString = HisUtil.hexString(hash.getValue()).toLowerCase();
 
 					md.reset();
 					md.update(pcr, 0, PCR_HASH_SIZE);
 
 					if (hashString.equals("0000000000000000000000000000000000000000"))
 						hashString = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
 
 					md.update(HisUtil.unHexString(hashString), 0, PCR_HASH_SIZE);
 					md.digest(pcr, 0, PCR_HASH_SIZE);
 				}
 
 				//Comparison with PCR value read from PcrHash element
 				if (!HisUtil.hexString(snapCollection.getPcrHash().get(0).getValue()).equals(HisUtil.hexString(pcr)))
 					errors.add("PCR " + pcrNumber + ": PcrHash does not match the extension of PCRs in report");
 
 				//Comparison with PCR value read from Quote
 				for (PcrValue pcrValue : pcrValues) {
 					if (pcrValue.getPcrNumber().equals(pcrNumber)) {
 						if (!HisUtil.hexString(pcrValue.getValue()).equals(HisUtil.hexString(pcr)))
 							errors.add("PCR " + pcrNumber + ": PCR value in the quote does not match the extension of PCRs in report");
 						break;
 					}
 				}
 
 				snapFound[pcrNumber.intValue()] = true;
 			}
 
 			for (PcrValue pcrValue : pcrValues) {
				if ((intPcrIMLMask & (0x00800000 >> pcrValue.getPcrNumber().intValue())) == 0)
 					continue;
 
 				boolean pcrMatch = true;
 				if (previousReportValidator != null)
 					pcrMatch = previousReportValidator.getPcrValue(pcrValue.getPcrNumber().intValue()).equals(getPcrValue(pcrValue.getPcrNumber().intValue()));
 
 				if (((FIRST_IR && !HisUtil.hexString(pcrValue.getValue()).equals("0000000000000000000000000000000000000000")) ||
 				    (!FIRST_IR && !pcrMatch)) && !snapFound[pcrValue.getPcrNumber().intValue()]) {
 					errors.add("PCR " + pcrValue.getPcrNumber() + ": SnapshotCollection expected but not found"); 
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			errors.add("An error occurred during measurements validation");
 		}
 	}
 
 	/**
 	 * Size of select + select + length of data + data  
 	 * @return resulting byte array.
 	 */
 	private byte[] computeDigest() {
 		try {
 			int sizeOfSelect = hisReportData.getPcrSizeOfSelect();
 			byte[] select = hisReportData.generatePcrSelect();
 			/*
 			 * WRONG!! According to TCG specs, ValueSize is the total size of the array of PcrValue structures
 			 *   int dataLength = hisReportData.generatePcrSelectedCount() * hisReportData.getPcrValueSize();
 			 */
 			int dataLength = hisReportData.getPcrValueSize();
 
 			//concatenate the values 
 			String stringDigest = HisUtil.hexString(HisUtil.intToByteArray(sizeOfSelect, 2));
 			stringDigest = stringDigest + HisUtil.hexString(select);
 			stringDigest = stringDigest + HisUtil.hexString(HisUtil.intToByteArray(dataLength, 4));
 			StringBuffer sb = new StringBuffer(stringDigest);
 			//concatenate the PCR data 
 			for (Integer integer : hisReportData.generatePcrSelectedPositions()) {
 				byte [] pcrValue = hisReportData.getPcrValue(integer);
 				if(pcrValue != null)
 				   stringDigest = sb.append(HisUtil.hexString(pcrValue)).toString();
 			}
 
 			lastDigestData = HisUtil.unHexString(stringDigest);
 
 			logger.debug("computeDigest:Digest before hash:" + stringDigest);
 			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
 			return messageDigest.digest(lastDigestData);
 		} catch (Exception e) {
 			logger.fatal(e, e);
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	/**
 	 * Create quote and verify signature.
 	 * @return True if signature verifies else false. 
 	 */
 	private boolean verifySignature() {
 		try {
 			//String values of hexadecimal representations of quote data 
 			String tpmQuoteVersion = HisUtil.hexString(hisReportData.getTpmQuoteVersion());
 			String quoteFixedString = HisUtil.hexString(hisReportData.getQuoteFixedString().getBytes());
 			String digest = HisUtil.hexString(hisReportData.getDigest());
 			String nonce = HisUtil.hexString(hisReportData.getNonce());
 
 			String quote = tpmQuoteVersion + quoteFixedString + digest + nonce;
 			logger.debug("Signature data:" + quote);
 
 			Signature signature = Signature.getInstance("SHA1withRSA", "BC");
 			signature.initVerify(machineCertificate);
 			signature.update(HisUtil.unHexString(quote));
 
 			if (!signature.verify(hisReportData.getSignature())) {
 				return false;
 			} else {
 				return true;
 			}
 
 		} catch (Exception e) {
 			logger.fatal(e, e);
 		}
 		return false;
 	}
 
 	/**
 	 * Compare the PCRs of the previous report.
 	 * @param previousReportString XML string of the previous report.
 	 */
 	private void comparePreviousReport(String previousReportString) {
 		logger.info("----------begin comparing to previous report----------");
 		HisReportValidator hisReportValidator = new HisReportValidator(previousReportString, null, null, null, null, null);
 		this.previousReportValidator = hisReportValidator;
 		StringBuffer sb = new StringBuffer();
 
 		SortedSet<Integer> possiblePcrs = new TreeSet<Integer>();
 		possiblePcrs.addAll(hisReportValidator.getHisReportData().getPossiblePcrs());
 		possiblePcrs.addAll(hisReportData.getPossiblePcrs());
 		for (Integer i : possiblePcrs) {
 			if (hisReportValidator.getPcrValue(i).length() == 0) {
 				previousReportDifferences = sb.append(DIFFERENCE_SEPARATOR).append(Integer.toString(i)).append(DIFFERENCE_SEPARATOR).toString();
 				compareErrors.add("PCR #" + Integer.toString(i) + " new. ");
 			} else if (getPcrValue(i).length() == 0) {
 				previousReportDifferences = sb.append(DIFFERENCE_SEPARATOR).append(Integer.toString(i)).append(DIFFERENCE_SEPARATOR).toString();
 				compareErrors.add("PCR #" + Integer.toString(i) + " absent. ");
 			} else if (!getPcrValue(i).equalsIgnoreCase(hisReportValidator.getPcrValue(i))) {
 				previousReportDifferences = sb.append(DIFFERENCE_SEPARATOR).append(Integer.toString(i)).append(DIFFERENCE_SEPARATOR).toString();
 				compareErrors.add("PCR #" + Integer.toString(i) + " differs. ");
 			}
 		}
 		logger.info("----------end comparing to previous report----------");
 	}
 
 	/**
 	 * Generate the contents of the previous differences
 	 * @return Delimited string containing PCRs which differed from the last report.
 	 */
 	public String getPreviousReportDifferences() {
 		return previousReportDifferences;
 	}
 
 	/**
 	 * Determine whether there was a difference for a PCR given a delimited 
 	 * PCR difference string.
 	 * @param previousReportDifferences Delimited string containing PCRs 
 	 * which differed from the last report.
 	 * @param i The PCR number for which you would like to know whether
 	 * it differed from the last report.
 	 * @return Whether or not the PCR indicated by i differed from this
 	 * report to the last report 
 	 */
 	public static boolean getPreviousReportDifference(String previousReportDifferences, int i) {
 		return !(previousReportDifferences.indexOf(DIFFERENCE_SEPARATOR + Integer.toString(i) + DIFFERENCE_SEPARATOR) < (1 - 1));
 	}
 
 	/** 
 	 * After the constructor generates and stores compararison errors
 	 * in a list this function concatenates and returns them.
 	 * @return The error string generated from compareErrors list.
 	 */
 	public String getCompareErrors() {
 		String errorsString = null;
 		StringBuffer sb = new StringBuffer();
 		Iterator<String> iterator = compareErrors.iterator();
 		
 		while (iterator.hasNext()) {
 			errorsString = sb.append(iterator.next()).toString();
 		}
 		return errorsString;
 	}
 
 	/**
 	 * After the constructor generates and stores errors in a list 
 	 * this function concatenates and returns them.
 	 * @return The errors generated from parsing an verification.
 	 */
 	public String getErrors() {
 		String errorsString = null;
 		StringBuffer sb = new StringBuffer();
 		for (Iterator<String> iterator = errors.iterator(); iterator.hasNext();) {
 			String string = (String) iterator.next();
 			if (errorsString != null) {
 				errorsString = sb.append("\n").append(ERROR_SEPARATOR).append("\n").toString();
 			}
 			errorsString = sb.append(string).toString();
 		}
 		return errorsString;
 	}
 
 	/**
 	 * Counts the number of errors given a generated error string.
 	 * @param errorsString An error string created from getErrors().
 	 * @return The number of errors within an error string.
 	 */
 	public static int getErrorsCount(String errorsString) {
 		if (errorsString == null) {
 			errorsString = "";
 		}
 		if (errorsString.length() < 1) {
 			return (1 - 1);
 		}
 		return new StringTokenizer(errorsString, ERROR_SEPARATOR).countTokens();
 	}
 
 	/**
 	 * Number of PCRs in the report.
 	 * @return The number of PCR values.
 	 */
 	public int getPcrValueCount() {
 		return hisReportData.getPcrValueCount();
 	}
 
 	/**
 	 * Generate a hexadecimal string for a PCR.
 	 * @param i PCR number
 	 * @return String with the hexadecimal value of the PCR or an empty
 	 * string for no entry.
 	 */
 	public String getPcrValue(int i) {
 		byte[] pcrValue = null;
 
 		pcrValue = hisReportData.getPcrValue(i);
 
 		if (pcrValue == null) {
 			return "";
 		} else {
 			return HisUtil.hexString(pcrValue);
 		}
 	}
 
 	/**
 	 * Used to populate the signature verified field stored with the 
 	 * integrity reports.
 	 * @return Whether the signature was verified during parsing.
 	 */
 	public boolean isSignatureVerified() {
 		return signatureVerified;
 	}
 
 	/**
 	 * Data related to the submitted report.
 	 * @return the hisReportData
 	 */
 	public HisReportData getHisReportData() {
 		return hisReportData;
 	}
 
 	/**
 	 * PCR descriptions for several PCRs in text and HTML format.
 	 * @param i PCR for number needing description
 	 * @param html if true return HTML formatted output else return plain text
 	 * @return HTML or plain text output 
 	 */
 	public static String getPcrDescription(int i, boolean html) {
 		int length = 77;
 		String pcrDescriptionString = "";
 
 		String[] pcrDescription = new String[length];
 		pcrDescription[0] = "BIOS";
 		pcrDescription[4] = "MBR, Boot Order";
 		pcrDescription[5] = "Partition Tables";
 
 		String[] pcrDescriptionHTML = new String[length];
 		pcrDescriptionHTML[0] = "BIOS";
 		pcrDescriptionHTML[4] = "MBR,<br>Boot Order";
 		pcrDescriptionHTML[5] = "Partition<br>Tables";
 
 		if (i < 0 || i >= length) {
 			return "";
 		} else {
 			pcrDescriptionString = html ? pcrDescriptionHTML[i] : pcrDescription[i];
 		}
 		if (pcrDescriptionString == null) {
 			return "";
 		} else {
 			return pcrDescriptionString;
 		}
 	}
 }
