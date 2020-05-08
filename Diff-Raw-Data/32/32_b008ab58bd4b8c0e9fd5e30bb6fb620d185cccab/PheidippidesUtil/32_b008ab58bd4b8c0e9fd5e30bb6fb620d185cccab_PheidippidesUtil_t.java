 package is.idega.idegaweb.pheidippides.util;
 
 import is.idega.idegaweb.pheidippides.RegistrationAnswerHolder;
 import is.idega.idegaweb.pheidippides.business.Currency;
 import is.idega.idegaweb.pheidippides.business.ShirtSizeGender;
 import is.idega.idegaweb.pheidippides.business.ShirtSizeSizes;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import com.ibm.icu.text.MessageFormat;
 import com.idega.util.IWTimestamp;
 import com.idega.util.text.Name;
 
 @Service("pheidippidesUtil")
 @Scope(BeanDefinition.SCOPE_SINGLETON)
 public class PheidippidesUtil {
 
 	public static String concat(String arg1, String arg2) {
 		return arg1.concat(arg2);
 	}
 
 	public static List<ShirtSizeSizes> getSizes() {
 		return Arrays.asList(ShirtSizeSizes.values());
 	}
 
 	public static List<ShirtSizeGender> getGenders() {
 		return Arrays.asList(ShirtSizeGender.values());
 	}
 	
 	public static List<Currency> getCurrencies() {
 		return Arrays.asList(Currency.values());
 	}
 
 	public static String formatDate(Date date, String pattern) {
 		if (date != null) {
 			return new IWTimestamp(date).getDateString(pattern);
 		}
 		return "";
 	}
 	
 	public static String getFullName(String firstName, String middleName, String lastName, Locale locale) {
 		return new Name(firstName, middleName, lastName).getName(locale, true);
 	}
 	
 	public static String escapeXML(String string) {
 		string = StringEscapeUtils.unescapeHtml(string.replaceAll("\\<[^>]*>", ""));
 		return string;
 	}
 	
 	public static boolean contains(List<?> objects, Object object) {
 		boolean contains = objects.contains(object);
 		return contains;
 	}
 	
 	public static String formatReceipt(String string, RegistrationAnswerHolder answer) {
 		String[] args = { String.valueOf(answer.getAmount()), answer.getBankReference().getReferenceNumber() };
 		return MessageFormat.format(string, args);
 	}
	
	public static boolean isEnglishLocale(Locale locale) {
		return locale.equals(Locale.ENGLISH);
	}
 }
