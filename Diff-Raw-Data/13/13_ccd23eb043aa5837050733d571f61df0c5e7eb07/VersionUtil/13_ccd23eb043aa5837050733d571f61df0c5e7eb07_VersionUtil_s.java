 package util.misc;
 
 import java.io.File;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 
 import om.OmException;
 
 public class VersionUtil {
 
 	public final static int VERSION_UNSPECIFIED=-1;
 
 	public static String suffix = ".([0-9]+).([0-9]+).jar";
 
 	public static String xmlSuffix = ".([0-9]+).([0-9]+).omxml";
 
 	public static String DOT_JAR = ".jar";
 
 	public static String DOT = ".";
 
 	/**
 	 * Looks through the provided File[] arguments for the latest version of
 	 *  a question based on the sQuestionID argument.
 	 * <br /><br />
 	 * Refactored from the NavigatorServlet for reuse.
 	 * @param sQuestionID
 	 * @param iRequiredVersion
 	 * @param qv
 	 * @param af
 	 * @return
 	 * @throws OmException
 	 */
 	public static boolean findLatestVersion(String sQuestionID,
 		int iRequiredVersion, QuestionVersion qv, File[] af)
 		throws OmException {
 		boolean bFound = false;
 		Pattern p = Pattern.compile(sQuestionID + suffix);
 		Pattern xp = Pattern.compile(sQuestionID + xmlSuffix);
 		String found = null;
 		for (int i = 0; i < af.length; i++) {
 			Matcher toUse = null;
 			// See if it's the question we're looking for
 			Matcher m = p.matcher(af[i].getName());
 			Matcher xm = xp.matcher(af[i].getName());
 			boolean mMatched = m.matches();
 			boolean xmMatched = xm.matches();
 			if (!mMatched && !xmMatched) {
 				continue;
 			} else {
 				if (mMatched) {
 					toUse = m;
 				} else if (xmMatched) {
 					toUse = xm;
 				}
 			}
 			if (null != toUse) {
 				//bFound = isGreater(m, qv, iRequiredVersion);
 				int iMajor = Integer.parseInt(toUse.group(1)), iMinor = Integer
 					.parseInt(toUse.group(2));
 				if (
 				// Major version is better than before and either matches version or
 				// unspec
 				(iMajor > qv.iMajor && (iRequiredVersion == iMajor || iRequiredVersion == VERSION_UNSPECIFIED))
 					||
 					// Same major version as before, better minor version
 					(iMajor == qv.iMajor && iMinor > qv.iMinor)) {
 					qv.iMajor = iMajor;
 					qv.iMinor = iMinor;
 					found = af[i].getName();
 					bFound = true;
 				}
 			}
 		}
 		System.out.println("FOUND : " + found);
 		return bFound;
 	}
 
 	/**
 	 * Looks through the provided Set<String> names in order to identify the
 	 *  latest version. 
 	 * @param sQuestionID
 	 * @param iRequiredVersion
 	 * @param qv
 	 * @param names
 	 * @return
 	 */
 	public static boolean findLatestVersion(String sQuestionID,
 		int iRequiredVersion, QuestionVersion qv, Set<String> names) {
 		boolean bFound = false;
 		Pattern p = Pattern.compile(sQuestionID + suffix);
 		for (String name : names) {
 			Matcher m = p.matcher(name);
 			if (!m.matches())
 				continue;
 			
 			int iMajor = Integer.parseInt(m.group(1)), iMinor = Integer
 				.parseInt(m.group(2));
 			if (
 			// Major version is better than before and either matches version or
 			// unspec
 			(iMajor > qv.iMajor && (iRequiredVersion == iMajor || iRequiredVersion == VERSION_UNSPECIFIED))
 				||
 				// Same major version as before, better minor version
 				(iMajor == qv.iMajor && iMinor > qv.iMinor)) {
 				qv.iMajor = iMajor;
 				qv.iMinor = iMinor;
 				bFound = true;
 			}
 		}
 		return bFound;
 	}
 
 	public static QuestionName represented(String fileName) {
 		QuestionName qn = null;
 		if (StringUtils.isNotEmpty(fileName) ? fileName.contains(DOT) : false) {
 			if (fileName.endsWith(DOT_JAR)) {
 				String questionName = GeneralUtils.questionNamePrefix(fileName);
 				if (StringUtils.isNotEmpty(questionName)) {
 					String remainder = fileName.substring(questionName.length() + 1,
 						fileName.length() - DOT_JAR.length());
 					if (StringUtils.isNotEmpty(remainder)) {
 						String[] bits = remainder.split("\\.");
 						if (null != bits ? bits.length == 2 : false) {
							QuestionVersion ver = new QuestionVersion();
							ver.iMajor = new Integer(bits[0]);
							ver.iMinor = new Integer(bits[1]);
							qn = new QuestionName(questionName, ver);
 						}
 					}
 				}
 			}
 		}
 		return qn;
 	}
 
 
 }
