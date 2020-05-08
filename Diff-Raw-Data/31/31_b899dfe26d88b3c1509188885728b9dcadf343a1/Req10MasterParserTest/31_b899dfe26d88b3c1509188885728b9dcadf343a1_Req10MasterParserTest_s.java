 package com.sforce.parser;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sforce.intf.impl.SfSender;
 import com.sforce.soap.enterprise.sobject.SObject;
 
 public class Req10MasterParserTest {
 	private static final Logger logger = LoggerFactory.getLogger(Req10MasterParserTest.class);
 	@Test
 	public void testParse() {
 		try {
			List<String> lines = FileUtils.readLines(new File("/Users/elliot/mqfile/test/req10_mxic.txt"));
 			for (String s: lines) {
 				SfSender ss = new SfSender();
 				String[] split = ss.split(s, '\t');
 				logger.debug("Size is [{}]",split.length);
 				Req10MasterParser fp = new Req10MasterParser();
 				fp.init();
 				fp.analysis(split);
 				SObject target = fp.parse(split);
 				logger.debug("Find Source [{}]",target);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	@Test
 	public void testInit() {
 		Req10MasterParser rp = new Req10MasterParser();
 		rp.listColumnInfo();
 		
 		
 	}
 
 }
