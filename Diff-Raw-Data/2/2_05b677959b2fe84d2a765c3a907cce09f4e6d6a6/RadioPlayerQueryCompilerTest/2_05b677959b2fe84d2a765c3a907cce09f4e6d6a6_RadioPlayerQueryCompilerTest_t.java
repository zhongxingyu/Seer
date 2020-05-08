 package org.atlasapi.feeds.radioplayer;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Set;
 import java.util.regex.Matcher;
 
 import org.atlasapi.content.criteria.AtomicQuery;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.feeds.radioplayer.compilers.RadioPlayerFeedCompilers;
 import org.atlasapi.feeds.radioplayer.compilers.RadioPlayerProgrammeInformationFeedCompiler;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.junit.Test;
 
 public class RadioPlayerQueryCompilerTest {
 
 	RadioPlayerProgrammeInformationFeedCompiler compiler = RadioPlayerFeedCompilers.compilers.get(0);
 	
 	@Test
 	public void testCompileQuery(){
		String filename = "20100906_e1_ce15_c222_0_PI.xml";
 		Matcher matcher = compiler.getPattern().matcher(filename);
 		
 		assertTrue(matcher.matches());
 		assertEquals(2,matcher.groupCount());
 		
 		DateTime day = DateTimeFormat.forPattern("yyyyMMdd").parseDateTime(matcher.group(1));
 		
 		String stationId  = matcher.group(2);
 		RadioPlayerServiceIdentifier id = RadioPlayerIDMappings.all.get(stationId);
 		
 		ContentQuery query = compiler.queryFor(day, id.getBroadcastUri());
 		
 		assertNotNull(query);
 		Set<AtomicQuery> os = query.operands();
 		
 		assertEquals(3,os.size());
 		
 	}
 	
 }
