 package com.earth2me.essentials.service;
 
 import org.slim3.tester.AppEngineTestCase;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 public class VersionCheckServiceTest extends AppEngineTestCase {
 
    private VersionCheckService service = VersionCheckService.get();
 
     @Test
     public void test() throws Exception {
         assertThat(service, is(notNullValue()));
     }
 }
