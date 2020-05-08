 package com.earth2me.essentials.service;
 
 import org.slim3.tester.AppEngineTestCase;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 public class TeamCityVersionServiceTest extends AppEngineTestCase {
 
    private TeamCityVersionService service = new TeamCityVersionService();
 
     @Test
     public void test() throws Exception {
         assertThat(service, is(notNullValue()));
     }
 }
