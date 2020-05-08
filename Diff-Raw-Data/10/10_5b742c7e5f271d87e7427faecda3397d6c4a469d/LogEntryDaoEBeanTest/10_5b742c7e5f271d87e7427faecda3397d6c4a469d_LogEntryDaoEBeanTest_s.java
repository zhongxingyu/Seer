 /*
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 *
 * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * $HeadURL$
 * $Id$
 */
 package dk.nsi.minlog.server.dao.ebean;
 
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Answers;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.avaje.ebean.EbeanServer;
 
 import dk.nsi.minlog.domain.LogEntry;
 import dk.nsi.minlog.server.dao.LogEntryDao;
 
 import static org.junit.Assert.*;
 
 @RunWith(MockitoJUnitRunner.class)
 public class LogEntryDaoEBeanTest {
 
 	@InjectMocks
 	LogEntryDao logEntryDao = new LogEntryDaoEBean();
 	
 	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
 	EbeanServer ebeanServer;
 		
 	public List<LogEntry> createResult(){
 		List<LogEntry> entries = new ArrayList<LogEntry>();
 		entries.add(new LogEntry());
 		entries.add(new LogEntry());
 		entries.add(new LogEntry());		
 		return entries;
 	}
 	
 	@Test
 	public void findByCpr() throws Exception {			
 		List<LogEntry> entries = createResult();
 		when(ebeanServer.find(LogEntry.class).where().eq("cprNrBorger", "1234").findList()).thenReturn(entries);
 		List<LogEntry> result = logEntryDao.findLogEntriesByCPRAndDates("1234", null, null);
 		
		assertEquals(entries, result);		
 	}
 
 	@Test
 	public void findByCprAndFrom() throws Exception {
 		List<LogEntry> entries = createResult();
 		when(ebeanServer.find(LogEntry.class).where().eq("cprNrBorger", "1234").ge(eq("tidspunkt"), any()).findList()).thenReturn(entries);		
 		List<LogEntry> result = logEntryDao.findLogEntriesByCPRAndDates("1234", DateTime.now(), null);
 		
		assertEquals(entries, result);		
 	}
 
 	@Test
 	public void findByCprAndTo() throws Exception {
 		List<LogEntry> entries = createResult();
 		when(ebeanServer.find(LogEntry.class).where().eq("cprNrBorger", "1234").le(eq("tidspunkt"), any()).findList()).thenReturn(entries);		
 		List<LogEntry> result = logEntryDao.findLogEntriesByCPRAndDates("1234", null, DateTime.now());
 		
		assertEquals(entries, result);		
 	}

 }
