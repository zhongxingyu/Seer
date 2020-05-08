 package com.earth2me.essentials.service;
 
 import org.slim3.tester.AppEngineTestCase;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 public class ModuleServiceTest extends AppEngineTestCase {
 
    private ModuleService service = new ModuleService();
 
     @Test
     public void test() throws Exception {
         assertThat(service, is(notNullValue()));
     }
 }
