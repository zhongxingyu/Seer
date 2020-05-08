 package nobugs.nolife.mw.util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 import nobugs.nolife.mw.MWException;
 import nobugs.nolife.mw.entities.TaggedMaterial;
 
 public class StringUtil {
 	private static Logger logger = Logger.getGlobal();
 	/**
 	 * A^OԋpBA^Ǒ`[^O][^O]...
 	 * @param taggedMaterialList
 	 * @return
 	 */
 	public static String joinTagString(List<TaggedMaterial> taggedMaterialList) {
 		StringBuffer joinedTags=new StringBuffer();
 		for (TaggedMaterial tm:taggedMaterialList) {
 			if(tm.getTagState().equals(Constants.TAG_STATE_STAGED)){
 				joinedTags.append("["+tm.getId().getTag()+"]");
 			}
 		}
 		return joinedTags.toString();
 	}
 
 	/**
 	 * A^OStringzɕĕԋpB
 	 * @param joinedTagString
 	 * @return
 	 * @throws MWException 
 	 */
 	public static String[] splitTagString(String joinedTagString) throws MWException{
 		if(joinedTagString.startsWith("[") && joinedTagString.endsWith("]")){
 			return joinedTagString.substring(1, joinedTagString.lastIndexOf("]")).split("\\]\\[");
 		} else {
 			throw new MWException("Joined tag string is not valid.");
 		}
 	}
 
 	/**
 	 * w肳ꂽHTMLGR[h.
 	 * <p>Ώە &lt; &gt; &quot; &amp; </p>
 	 *
 	 * @param message HTMLGR[hΏۂ̕
 	 * @return HTMLGR[hς݂̕
 	 */
 	public static String htmlEncode(String message) {
 		StringBuffer ret = new StringBuffer();
 		for(int i=0; i<message.length(); i++){
 			switch(message.charAt(i)) {
 			case '<' :
 				ret.append("&lt;");
 				break;
 			case '>' :
 				ret.append("&gt;");
 				break;
 			case '&' :
 				ret.append("&amp;");
 				break;
 			case '"' :
 				ret.append("&quot;");
 				break;
 			default :
 				ret.append(message.charAt(i));
 				break;
 			}
 		}
 		return ret.toString();
 	}
 
 	/**
 	 * w肳ꂽNbRFC1123`(̈ꕔ)ɕϊ.
 	 * ) Sep 18 2009 00:00:00
 	 * DayOfWeek͊܂߂ȂB
 	 *
 	 * Unit test is available.
 	 *
 	 * @param year yyyy`̔N
 	 * @param month MM`̌
 	 * @param date dd`̓
 	 * @param hour HH`̎
 	 * @param minute mm`̕
 	 * @param second ss`̕b
 	 * @return RFC1123`(̈ꕔ)ɕϊꂽ
 	 * @throws MWException ʂŔOMWException ɕϊthrow B
 	 */
 	public static String formatChronicleDate(String year, String month, String date, String hour, String minute, String second) throws MWException {
 		// ҂Ă錅ɂ悤ɁA0߂{
 		year  =  String.format("%04d",Integer.parseInt(year));
 		month =  String.format("%02d",Integer.parseInt(month));
 		date  =  String.format("%02d",Integer.parseInt(date));
 		hour  =  String.format("%02d",Integer.parseInt(hour));
 		minute = String.format("%02d",Integer.parseInt(minute));
 		second = String.format("%02d",Integer.parseInt(second));
 
 		Date aDate = new Date();
 		try {
 			aDate = new SimpleDateFormat("yyyyMMddHHmmss").parse(year+month+date+hour+minute+second);
 		} catch(ParseException e) {
 			logger.severe("t`̕ϊɗO܂B"+e.getMessage());
 			throw new MWException(e);
 		}
 
 		logger.fine("was set : "+aDate);
 		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm:ss 'GMT'Z",Locale.US);
 		logger.fine("after format : "+sdf.format(aDate));
 		return sdf.format(aDate);
 	}
 }
