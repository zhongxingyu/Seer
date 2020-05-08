 /**
  * 
  * CabConverter: A Cabrillo generation tool for MacLoggerDX
  * Original concept and author: B. Scott Andersen (NE1RD)
  */
 package com.bsandersen.CabConverter;
 
 import java.util.Formatter;
 
 /**
  * This class holds the details for the Cabrillo QSO formatting as
  * presecribed by this contest's XML file. This also provides the
  * formatting capability to convert a set of ADIF QSO data into
  * a properly formatted QSO detail line.
  * 
  * @author B. Scott Andersen
  * 
  * CabConverter by B. Scott Andersen (NE1RD) is licensed under a 
  * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
  */
 public class CabrilloQSO {
 	CabFileNode qsoDetailHead = null;
 	CabFileNode qsoDetailTail = null;
 	
 	/**
 	 * addElement adds the essence of the element to the QSO description.
 	 * If the element is a Text item, the type "Text" and the text element's
 	 * value is added. If the element is a reference to some data item, then
 	 * the data item like "Frequency" is added.
 	 * @author B. Scott Andersen
 	 */
 	public void addElement(String elementType, String elementValue) {
 		CabFileNode e = new CabFileNode(elementType, elementValue);
 		if (qsoDetailHead != null) {
 			qsoDetailTail.setNext(e);
 			qsoDetailTail = e;
 		} else {
 			qsoDetailHead = e;
 			qsoDetailTail = e;
 		}
 	}
 	
 	/**
 	 * Using the QSO detail information supplied by the XML file, this
 	 * method will take the passed ADIF record (r) and format it into
 	 * a text string suitable for a Cabrillo file.
 	 * @param r The ADIF record to format
 	 * @return A properly formatted QSO: line for the Cabrillo file
 	 */
 	public String formatQSO(ADIFrecord r) {
 		String s;
 		String key;
 		CabFileNode e = qsoDetailHead;
 		Formatter f;
 		PersonalData personalData = PersonalData.getInstance();
 
 		s = "";
 		while (e != null) {
 			key = e.value();
 
 				if (key.compareToIgnoreCase("TheirCallsign") == 0) {
 					s = s.concat(r.call + " ");
 				} else if (key.compareToIgnoreCase("Callsign") == 0) {
 					// Special case: we get this from the personal data
 					s = s.concat(personalData.getCallSign() + " ");
 				} else if (key.compareToIgnoreCase("Date") == 0) {
 					s = s.concat(r.date + " ");
 				} else if (key.compareToIgnoreCase("Time") == 0) {
 					s = s.concat(r.time + " ");
 				} else if (key.compareToIgnoreCase("Frequency") == 0) {
 					f = new Formatter();
 					long v = new Double(r.frequency * 1000.0).longValue();
 					f.format("%05d ", v);
 					s = s.concat(f.toString());
 					f.close();
 				} else if (key.compareToIgnoreCase("Mode") == 0) {
 					if ((r.mode.compareToIgnoreCase("SSB") == 0) ||
 						(r.mode.compareToIgnoreCase("USB") == 0) ||
 						(r.mode.compareToIgnoreCase("LSB") == 0)) {
 						s = s.concat("PH ");
 					} else if (r.mode.compareTo("AM") == 0) {
 						s = s.concat("AM ");
 					} else if (r.mode.compareTo("FM") == 0) {
 						s = s.concat("FM ");
 					} else if ((r.mode.compareToIgnoreCase("CW") == 0) ||
 							   (r.mode.compareToIgnoreCase("CWR") == 0)) {
 						s = s.concat("CW ");
 					} else if (r.mode.compareTo("DV") == 0) {
 						s = s.concat("DV ");
					} else if ((r.mode.compareToIgnoreCase("PSK") == 0) ||
 						  (r.mode.compareToIgnoreCase("FSK") == 0) ||
 						  (r.mode.compareToIgnoreCase("FSK-R") == 0)) {
 						s = s.concat("PH ");
 					} else if ((r.mode.compareToIgnoreCase("RTTY") == 0) ||
 							   (r.mode.compareToIgnoreCase("RTTY-R") == 0)) {
 						s = s.concat("RT ");
 					} else {
 						s = s.concat("?? ");
 					}
 				} else if (key.compareToIgnoreCase("RSTs") == 0) {
 					s = s.concat(r.rstSent + " ");
 				} else if (key.compareToIgnoreCase("RSTr") == 0) {
 					s = s.concat(r.rstReceived + " ");
 				} else if (key.compareToIgnoreCase("STXn") == 0) {
 					f = new Formatter();
 					f.format("%3d", r.serialNumberSent);
 					s = s.concat(new String(f.toString()) + " ");
 					f.close();
 				} else if (key.compareToIgnoreCase("SRXn") == 0) {
 					f = new Formatter();
 					f.format("%3d", r.serialNumberReceived);
 					s = s.concat(new String(f.toString()) + " ");
 					f.close();
 				} else if (key.compareToIgnoreCase("STX") == 0) {
 					s = s.concat(r.exchangeSent + " ");
 				} else if (key.compareToIgnoreCase("SRX") == 0) {
 					s = s.concat(r.exchangeReceived + " ");
 				} else if (key.compareToIgnoreCase("STX-ARRL-SS-NOCALL") == 0) {
 					String goof;
 					String temp = r.exchangeSent;
 					goof = new String(temp.substring(0,1) + " ");
 					temp = temp.substring(2);
 					int space = temp.indexOf(' ');
 					goof = goof.concat(temp.substring(space+1));
 					s = s.concat(goof + " ");
 				} else if (key.compareToIgnoreCase("SRX-ARRL-SS-NOCALL") == 0) {
 					String goof;
 					String temp = r.exchangeReceived;
 					goof = new String(temp.substring(0,1) + " ");
 					temp = temp.substring(2);
 					int space = temp.indexOf(' ');
 					goof = goof.concat(temp.substring(space+1));
 					s = s.concat(goof + " ");
 				} else {
 					s = s.concat(key + " ");
 			}
 			e = e.getNext();
 		}
 		return s;
 	}
 }
