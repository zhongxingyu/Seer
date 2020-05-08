 package com.mpages.parsing.gui;
 
 import java.io.File;
 
 import org.junit.Test;
 
import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 import com.mpages.parsing.crawler.Crawler;
 
 public class ChunkExtractorGuiTest {
 	@Test
 	public void test() throws Exception
 	{
 		ChunkExtractorGui gui = new ChunkExtractorGui();
 		gui.init();
 		String defaultText = Files.toString(new File("1.html"), Crawler.CHARSET);
 		gui.setAnalysedText(defaultText);
 		gui.enterEventsDispatchLoop();
 	}
 }
