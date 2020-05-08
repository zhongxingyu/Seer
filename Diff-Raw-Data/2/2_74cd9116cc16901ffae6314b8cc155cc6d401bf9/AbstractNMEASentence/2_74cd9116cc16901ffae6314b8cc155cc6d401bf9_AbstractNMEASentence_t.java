 package com.buglabs.nmea2;
 
 import com.buglabs.nmea.sentences.NMEAParserException;
 
 /**
  * Base class of all NMEA sentences
  * 
  * @author kgilmer
  * 
  */
 public abstract class AbstractNMEASentence {
 	private String FIELD_SEPARATOR = ",";
 	private String CHECKSUM_SEPARATOR = "*";
 	protected String checksum = null;
 	private String name = null;
 
 	public AbstractNMEASentence(String sentence) {
 		initialize();
 		String[] fields = parseChecksum(sentence).split(FIELD_SEPARATOR);
 		name = fields[0];
 
 		for (int i = 1; i < fields.length; ++i) {
 			if (isEmpty(fields[i])) {
 				//Ignore empty values.
 				continue;
 			}
 
 			try {
 				parseField(i, fields[i], fields);
 			} catch (RuntimeException e) {
 				throw new NMEAParserException(e.getMessage());
 			}
 		}
 
 		validate();
 	}
 
 	/**
 	 * Initialize any member objects here.
 	 */
 	protected void initialize() {
 
 	}
 
 	private String parseChecksum(String sentence) {
		String[] splitSentence = sentence.split("\\" + CHECKSUM_SEPARATOR);
 
 		if (splitSentence.length > 1) {
 			checksum = splitSentence[1];
 		}
 
 		return splitSentence[0];
 	}
 
 	/**
 	 * @param sentence
 	 */
 	protected abstract void parseField(int index, String value, String[] fields);
 
 	/**
 	 * Invalid sentences should throw NMEAParserException
 	 */
 	protected abstract void validate();
 
 	/**
 	 * @param val
 	 * @return
 	 */
 	protected boolean isEmpty(String val) {
 		return val == null || val.length() == 0;
 	}
 
 	/**
 	 * @return Sentence Checksum
 	 */
 	public String getChecksum() {
 		return checksum;
 	}
 
 	/**
 	 * @return Sentence name
 	 */
 	public String getName() {
 		return name;
 	}
 }
