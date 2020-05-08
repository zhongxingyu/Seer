 package notifier.parser;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.FileReader;
 import java.io.BufferedReader;
 
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
 		SRMCalendarParserMock parser = new SRMCalendarParserMock("http://community.topcoder.com/tc?module=Static&d1=calendar&d2=thisMonth");
		for(SRM srm : parser.getSRMs()){
			System.out.println(srm);
		}
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
