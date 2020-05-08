 package cc.explain.server.api;
 
 import cc.explain.server.api.HashData;
 import cc.explain.server.api.SublightService;
 import cc.explain.server.xml.LoginResponse;
 import cc.explain.server.xml.SearchResponse;
 import cc.explain.server.xml.TicketResponse;
 import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.xml.bind.JAXBException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  * User: kzimnick
  * Date: 25.11.12
  * Time: 17:47
  */
@Ignore
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {
         "classpath:spring-context.xml",
         "classpath:spring-dao.xml",
         "classpath:test-spring-dataSource.xml",
         "classpath:spring-security.xml",
         "classpath:spring-tx.xml"
 })
 public class SublightServiceTest {
 
     @Autowired
     SublightService sublightService;
 
     @Test
     public void shouldLoginCorrectly() throws URISyntaxException {
 
         boolean result = sublightService.login();
 
         assertEquals(true, result);
     }
 
     @Test
     public void shouldReturnSearchedMoviesForHash() {
         String hash = "12163000";
         String fileSize = "742848512";
 
         sublightService.login();
         String subtitleId = sublightService.searchSubtitleId(hash, fileSize);
 
         assertEquals("cb652f06-cfb1-40e3-b9ce-c49d2bbb8679", subtitleId);
     }
 
     @Test
     public void shouldReturnSearchedMoviesForHash2() {
         String hash = "12163000";
         String fileSize = "742848512";
 
         sublightService.login();
         String subtitleId = sublightService.searchSubtitleId(hash, fileSize);
         subtitleId = sublightService.searchSubtitleId(hash, fileSize);
 
 
         assertEquals("cb652f06-cfb1-40e3-b9ce-c49d2bbb8679", subtitleId);
     }
 
     @Test
     public void shouldReturnEmptyForFailMovieHash() {
         String hash = "12163001";
         String fileSize = "742848512";
 
         sublightService.login();
         String subtitleId = sublightService.searchSubtitleId(hash, fileSize);
 
         assertEquals("", subtitleId);
     }
 
 
     @Test
     public void shouldReturnsSecondsToWaits() {
 
         sublightService.login();
         TicketResponse ticket = sublightService.getWaitTicket();
 
         assertNotNull(ticket);
     }
 
     @Test
     public void shouldReturnsDownloadedSubtitles() throws InterruptedException {
         String subtitleId = "cb652f06-cfb1-40e3-b9ce-c49d2bbb8679";
 
 
         sublightService.login();
         TicketResponse waitTicket = sublightService.getWaitTicket();
         Thread.sleep(waitTicket.getWaitTime() * 1000);
         String subtitles = sublightService.downloadSubtitlesZIP(subtitleId, waitTicket.getTicket());
 
         assertEquals("﻿100:00:44,87", subtitles.substring(0, 15).replaceAll("[\r\n]+", ""));
     }
 
     @Test
     public void shouldCalculateHashCorrectly() throws IOException {
         HashData data = new HashData();
         List<String> lines = IOUtils.readLines(Thread.currentThread().getContextClassLoader().getResourceAsStream("HashData.txt"));
         data.setSize(lines.get(0).split("=")[1]);
         data.setHead(lines.get(1));
         data.setTail(lines.get(2));
 
         long result = sublightService.calculateHash(data);
 
         assertEquals(1722545758, result);
     }
 
     @Test
     public void shouldLoginResponseUnmarshall() throws JAXBException, UnsupportedEncodingException {
         InputStream xml = this.getClass().getResourceAsStream("/xml/LoginResponse.xml");
 
         LoginResponse response = sublightService.unmarshal(xml, LoginResponse.class);
 
         assertEquals("30a16045-9fee-45aa-9a6d-b7539517d2b8", response.getSession());
     }
 
 
     @Test
     public void shouldTicketResponseUnmarshall() throws JAXBException, UnsupportedEncodingException {
         InputStream xml = this.getClass().getResourceAsStream("/xml/TicketResponse.xml");
 
         TicketResponse response = sublightService.unmarshal(xml, TicketResponse.class);
 
         assertEquals("548dab98e761458da447289807c9338a", response.getTicket());
         assertEquals(30, response.getWaitTime());
     }
 
     @Test
     public void shouldSearchResponseUnmarshall() throws JAXBException, UnsupportedEncodingException {
         InputStream xml = this.getClass().getResourceAsStream("/xml/SearchResponse.xml");
 
         SearchResponse response = sublightService.unmarshal(xml, SearchResponse.class);
 
         assertEquals(18, response.getSubtitle().length);
         assertEquals("cb652f06-cfb1-40e3-b9ce-c49d2bbb8679", response.getSubtitle()[0].getId());
     }
 
     @Test
     public void shouldDownloadSubtitles() throws IOException {
         HashData data = new HashData();
         List<String> lines = IOUtils.readLines(Thread.currentThread().getContextClassLoader().getResourceAsStream("HashData.txt"));
         data.setSize(lines.get(0).split("=")[1]);
         data.setHead(lines.get(1));
         data.setTail(lines.get(2));
 
         String subtitle = sublightService.downloadSubtitles(data);
 
         assertTrue(subtitle != null);
     }
 
     @Test
     public void shouldDownloadSubtitlesTwice() throws IOException {
         HashData data = new HashData();
         List<String> lines = IOUtils.readLines(Thread.currentThread().getContextClassLoader().getResourceAsStream("HashData.txt"));
         data.setSize(lines.get(0).split("=")[1]);
         data.setHead(lines.get(1));
         data.setTail(lines.get(2));
 
         sublightService.login();
         String subtitle = sublightService.downloadSubtitles(data);
         subtitle = sublightService.downloadSubtitles(data);
 
         assertTrue(subtitle != null);
     }
 
 }
