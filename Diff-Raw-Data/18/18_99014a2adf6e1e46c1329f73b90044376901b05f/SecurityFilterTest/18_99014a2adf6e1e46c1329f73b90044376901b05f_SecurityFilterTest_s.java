 package tw.com.dsc.web.util;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import junit.framework.Assert;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.junit.Test;
 
 import tw.com.dsc.domain.ErrorType;
 
 public class SecurityFilterTest {
	public static final Pattern pattern = Pattern.compile("^/\\w*/(\\w*)!\\w*\\.action$");
 	@Test
 	public void test() {
 		Matcher matcher = pattern.matcher("/eits/edit!rating.action");
 		Assert.assertTrue(matcher.matches());
 		
 		System.out.println(matcher.group(1));
 		Assert.assertEquals("edit",matcher.group(1));
 	}
 
 	@Test
 	public void testMD5() {
 		String md5Hex = DigestUtils.md5Hex("123");
 		System.out.println(md5Hex);
 		
 		System.out.println(ErrorType.NotFound);
 	}
 }
