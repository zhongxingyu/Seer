 package com.asgeirnilsen.blog.logging;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class WtfLoggerTest {
 
     @Test
     public void test() {
         Logger logger = LoggerFactory.getLogger(getClass());
         logger.error("Oh My GOD!");
         logger.warn("What The F*ck just happened?");
        logger.info("JYI it worked fine.");
         logger.debug("Oh by the way -- test this");
     }
 
 }
