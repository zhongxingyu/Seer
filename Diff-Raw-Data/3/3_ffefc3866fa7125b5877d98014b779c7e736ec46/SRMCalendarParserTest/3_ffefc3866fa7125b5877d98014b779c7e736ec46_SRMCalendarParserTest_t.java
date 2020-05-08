 package notifier.parser;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.util.List;
import java.util.Locale;
 
 import notifier.SRM;
 
 import org.junit.Test;
 
 public class SRMCalendarParserTest {
 	@Test
 	public void instance() throws Exception {
 		new SRMCalendarParserMock(null);
 	}
 
 	@Test
 	public void getUrl() throws Exception {
 		SRMCalendarParserMock parser = new SRMCalendarParserMock("http://community.topcoder.com/tc?module=Static&d1=calendar&d2=thisMonth");
 		assertSame("http://community.topcoder.com/tc?module=Static&d1=calendar&d2=thisMonth", parser.getUrl());
 	}
 
 	@Test
 	public void printSRMs() throws Exception {
		Locale.setDefault(Locale.JAPAN);
 		SRMCalendarParserMock parser = new SRMCalendarParserMock("http://community.topcoder.com/tc?module=Static&d1=calendar&d2=thisMonth");
 		List<SRM> srms = parser.getSRMs();
 
 		String expected1 = "notifier.SRM[name=SRM 548,url=/tc?module=MatchDetails&rd=15170,competisionTime=Tue Jul 03 00:10:00 JST 2012,regiserTime=Mon Jul 02 21:00:00 JST 2012,count=0]";
 		String actual1 = srms.get(0).toString();
 		System.out.println(expected1);
 		System.out.println(actual1);
 		assertEquals(expected1, actual1);
 
 		String expected2 = "notifier.SRM[name=SRM 549,url=/tc?module=MatchDetails&rd=15171,competisionTime=Mon Jul 09 20:10:00 JST 2012,regiserTime=Mon Jul 09 17:00:00 JST 2012,count=0]";
 		String actual2 = srms.get(1).toString();
 		assertEquals(expected2, actual2);
 
 		String expected3 = "notifier.SRM[name=SRM 550,url=/tc?module=MatchDetails&rd=15172,competisionTime=Sun Jul 22 01:00:00 JST 2012,regiserTime=Sat Jul 21 22:00:00 JST 2012,count=0]";
 		String actual3 = srms.get(2).toString();
 		assertEquals(expected3, actual3);
 	}
 }
 
 class SRMCalendarParserMock extends SRMCalendarParser{
 	public SRMCalendarParserMock(String url) {
 		super(url);
 	}
 
 	@Override
 	protected String getContent(String url) throws IOException{
 		FileReader reader = null;
 		if (url.equals("http://community.topcoder.com/tc?module=Static&d1=calendar&d2=thisMonth"))
 		{
 			reader = new FileReader("src/notifier/parser/testdata/calendar.html");
 		}else if(url.equals("http://community.topcoder.com/tc?module=MatchDetails&rd=15170")){
 			reader = new FileReader("src/notifier/parser/testdata/srm1.html");
 		}else if(url.equals("http://community.topcoder.com/tc?module=MatchDetails&rd=15171")){
 			reader = new FileReader("src/notifier/parser/testdata/srm2.html");
 		}else if(url.equals("http://community.topcoder.com/tc?module=MatchDetails&rd=15172")){
 			reader = new FileReader("src/notifier/parser/testdata/srm3.html");
 		}
 		BufferedReader br = new BufferedReader(reader);
 		StringBuilder sb = new StringBuilder();
 		for(String line; (line = br.readLine()) != null;) {
 			sb.append(line);
 			sb.append("\n");
 		}
 		return sb.toString();
 	}
 }
