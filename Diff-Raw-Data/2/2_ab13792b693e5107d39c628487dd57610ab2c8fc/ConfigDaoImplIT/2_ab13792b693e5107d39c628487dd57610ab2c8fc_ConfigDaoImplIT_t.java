 /*****************************************************************************
  * 
  * Copyright (C) Zenoss, Inc. 2010, all rights reserved.
  * 
  * This content is made available according to terms specified in
  * License.zenoss under the directory where your Zenoss product is installed.
  * 
  ****************************************************************************/
 
 
 package org.zenoss.zep.dao.impl;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.ZepConfig;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.ConfigDao;
 
 import static org.junit.Assert.*;
 
 @ContextConfiguration({"classpath:zep-config.xml"})
 public class ConfigDaoImplIT extends AbstractTransactionalJUnit4SpringContextTests {
     @Autowired
     public ConfigDao configDao;
 
     @Test
     public void testConfig() throws ZepException {
         ZepConfig.Builder builder = ZepConfig.newBuilder();
         builder.setEventAgeDisableSeverity(EventSeverity.SEVERITY_CRITICAL);
         builder.setEventAgeSeverityInclusive(true);
         builder.setEventAgeIntervalMinutes(60);
         builder.setEventArchiveIntervalMinutes(7*24*60);
         builder.setEventArchivePurgeIntervalDays(30);
         builder.setEventTimePurgeIntervalDays(2);
         builder.setEventMaxSizeBytes(40000);
         builder.setIndexSummaryIntervalMilliseconds(5000);
         builder.setIndexArchiveIntervalMilliseconds(15000);
         builder.setIndexLimit(500);
         builder.setAgingLimit(600);
         builder.setArchiveLimit(750);
         builder.setAgingIntervalMilliseconds(30000);
         builder.setArchiveIntervalMilliseconds(45000);
        builder.setEnableEventFlappingDetection(false);
        builder.setFlappingEventClass("/Status/Flapping");
         ZepConfig cnf = builder.build();
         configDao.setConfig(cnf);
 
         assertEquals(cnf, configDao.getConfig());
 
         cnf = ZepConfig.newBuilder(cnf).setEventAgeIntervalMinutes(90).build();
         configDao.setConfigValue("event_age_interval_minutes", cnf);
         assertEquals(cnf, configDao.getConfig());
 
         assertEquals(1, configDao.removeConfigValue("event_age_interval_minutes"));
         assertEquals(0, configDao.removeConfigValue("event_age_interval_minutes"));
         assertEquals(ZepConfig.getDefaultInstance().getEventAgeIntervalMinutes(),
                      configDao.getConfig().getEventAgeIntervalMinutes());
     }
 }
