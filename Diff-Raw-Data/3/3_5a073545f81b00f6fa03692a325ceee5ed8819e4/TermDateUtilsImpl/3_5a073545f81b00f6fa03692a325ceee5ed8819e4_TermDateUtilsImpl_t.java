 package cz.cvut.fit.mi_mpr_dip.admission.util;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.time.DateFormatUtils;
 import org.springframework.stereotype.Service;
 
 @Service
 public class TermDateUtilsImpl implements TermDateUtils {
 
	private final DateFormat MYSQL_DATE_FORMAT = new SimpleDateFormat(DateFormatUtils.ISO_DATETIME_FORMAT.getPattern());
 
 	@Override
 	public Date fromUnderscoredIso(String text) throws ParseException {
 		String iso = StringUtils.replace(text, StringPool.UDERSCORE, StringPool.SPACE);
 		return fromIso(iso);
 	}
 
 	@Override
 	public Date fromIso(String text) throws ParseException {
 		return MYSQL_DATE_FORMAT.parse(text);
 	}
 
 	@Override
 	public String toUnderscoredIso(Date date) {
 		return StringUtils.replace(toIso(date), StringPool.SPACE, StringPool.UDERSCORE);
 	}
 
 	@Override
 	public String toIso(Date date) {
 		return MYSQL_DATE_FORMAT.format(date);
 	}
 
 }
