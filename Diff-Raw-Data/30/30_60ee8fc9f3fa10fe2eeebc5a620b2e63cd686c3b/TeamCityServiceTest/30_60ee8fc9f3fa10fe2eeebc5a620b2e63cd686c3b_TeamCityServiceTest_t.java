 package com.earth2me.essentials.service;
 
 import org.slim3.tester.AppEngineTestCase;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 public class TeamCityServiceTest extends AppEngineTestCase {
 
    private TeamCityService service = TeamCityService.get();
 
     @Test
     public void test() throws Exception {
         assertThat(service, is(notNullValue()));
     }
 }
