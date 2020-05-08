 package outag.audioformats;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 /** Represents a structure for storing and retrieving information
  * about the codec respectively the encoding parameters. */
 public class EncodingInfo {
 
 	/** Key for the Bitrate.({@link Integer}) */
 	public final static String FIELD_BITRATE = "BITRATE";
 
 	/** Key for the number of audio channels.({@link Integer}) */
 	public final static String FIELD_CHANNEL = "CHANNB";
 
 	/** Key for the extra encoding information.({@link String}) */
 	public final static String FIELD_INFOS = "INFOS";
 
 	/** Key for the audio clip duration in seconds. ({@link java.lang.Float}) */
 	public final static String FIELD_LENGTH = "LENGTH";
 
 	/** Key for the audio sample rate in &quot;Hz&quot;. ({@link Integer}) */
 	public final static String FIELD_SAMPLERATE = "SAMPLING";
 
 	/** Key for the audio type.({@link String}) */
 	public final static String FIELD_TYPE = "TYPE";
 
 	/** Key for the VBR flag. ({@link Boolean}) */
 	public final static String FIELD_VBR = "VBR";
 
 	/** Contains the parameters. */
 	private Hashtable<String, Comparable> content;
 
 	/** Creates an instance with empty values. */
 	public EncodingInfo() {
 		content = new Hashtable<String, Comparable>(6);
 		content.put(FIELD_BITRATE, new Integer(-1));
 		content.put(FIELD_CHANNEL, new Integer(-1));
 		content.put(FIELD_TYPE, "");
 		content.put(FIELD_INFOS, "");
 		content.put(FIELD_SAMPLERATE, new Integer(-1));
 		content.put(FIELD_LENGTH, new Float(-1));
		content.put(FIELD_VBR, new Boolean(true));
 	}
 
 	public int getBitrate() { return (Integer) content.get(FIELD_BITRATE); }
 	public void setBitrate(int bitrate) { content.put(FIELD_BITRATE, new Integer(bitrate));	}
 	
 	public int getChannelNumber() {	return (Integer) content.get(FIELD_CHANNEL); }
 	public void setChannelNumber(int chanNb) { content.put(FIELD_CHANNEL, new Integer(chanNb));	}	
 	
 	public String getEncodingType() { return (String) content.get(FIELD_TYPE); }
 	public void setEncodingType(String encodingType) { content.put(FIELD_TYPE, encodingType); }	
 	
 	public String getExtraEncodingInfos() { return (String) content.get(FIELD_INFOS); }
 	public void setExtraEncodingInfos(String infos) { content.put(FIELD_INFOS, infos); }	
 
 	/** @return duration in seconds. */
 	public int getLength() { return (int) getPreciseLength(); }
 	public void setLength(int length) {	content.put(FIELD_LENGTH, new Float(length)); }	
 
 	/** @return duration in seconds. */
 	public float getPreciseLength() { return (Float) content.get(FIELD_LENGTH);	}
 	public void setPreciseLength(float seconds) { content.put(FIELD_LENGTH, new Float(seconds)); }	
 
 	/** @return sample rate in &quot;Hz&quot;. */
 	public int getSamplingRate() { return ((Integer) content.get(FIELD_SAMPLERATE)).intValue();	}
 	public void setSamplingRate(int samplingRate) {	content.put(FIELD_SAMPLERATE, new Integer(samplingRate)); }	
 
 	/** @return <code>true</code> if audio is encoded with VBR. */
 	public boolean isVbr() { return ((Boolean) content.get(FIELD_VBR)).booleanValue();	}
 	public void setVbr(boolean b) {	content.put(FIELD_VBR, new Boolean(b));	}
 
 	/** Pretty prints this encoding info */
 	public String toString() {
 		StringBuffer out = new StringBuffer(50);
 		out.append("Encoding infos content:\n");
 		Enumeration<String> en = content.keys();
 		while (en.hasMoreElements()) {
 			Object key = en.nextElement();
 			Object val = content.get(key);
 			out.append("\t");
 			out.append(key);
 			out.append(" : ");
 			out.append(val);
 			out.append("\n");
 		}
 		return out.toString().substring(0, out.length() - 1);
 	}
 }
