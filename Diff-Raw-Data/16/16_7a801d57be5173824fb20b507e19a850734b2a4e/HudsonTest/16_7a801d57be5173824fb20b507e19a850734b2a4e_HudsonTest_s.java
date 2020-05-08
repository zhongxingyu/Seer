 /**
  * Copyright (C) 2010 Julien SMADJA <julien dot smadja at gmail dot com> - Arnaud LEMAIRE <alemaire at norad dot fr>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.jsmadja.wall.hudsonclient;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.bind.JAXBException;
 
 import org.junit.Test;
 
 import com.jsmadja.wall.hudsonclient.builder.HudsonUrlBuilder;
 import com.jsmadja.wall.hudsonclient.domain.HudsonBuild;
 import com.jsmadja.wall.hudsonclient.domain.HudsonProject;
 import com.jsmadja.wall.projectwall.domain.TestResult;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientHandler;
 import com.sun.jersey.api.client.config.ClientConfig;
 
 public class HudsonTest {
 
     private static final int FLUXX_BUILT_WITH_COMMITERS = 273;
 
     private static final String HUDSON_URL = "http://fluxx.fr.cr:8080/hudson";
 
     private static final int INVALID_XML = 0;
 
     HudsonUrlBuilder hudsonUrlBuilder = new HudsonUrlBuilder(HUDSON_URL);
 
     private Hudson hudson = new Hudson(HUDSON_URL) {
         @Override
         Client buildJerseyClient(ClientConfig clientConfig) {
             ClientHandler clientHandler = FileClientHandlerBuilder.newFileClientHandler()
             .withFile(hudsonUrlBuilder.getAllProjectsUrl(), "hudson/all_jobs.xml")
             .withFile(hudsonUrlBuilder.getProjectUrl("fluxx"), "hudson/fluxx.xml")
             .withFile(hudsonUrlBuilder.getBuildUrl("fluxx", 101), "hudson/fluxx_101.xml")
             .withFile(hudsonUrlBuilder.getBuildUrl("fluxx", 102), "hudson/fluxx_102.xml")
             .withFile(hudsonUrlBuilder.getProjectUrl("dev-radar"), "hudson/dev-radar.xml")
             .withFile(hudsonUrlBuilder.getBuildUrl("dev-radar", 107), "hudson/dev-radar_107.xml")
             .withFile(hudsonUrlBuilder.getBuildUrl("dev-radar", 108), "hudson/dev-radar_108.xml")
             .withFile(hudsonUrlBuilder.getBuildUrl("dev-radar", 74), "hudson/dev-radar_74.xml")
             .withFile(hudsonUrlBuilder.getTestResultUrl("dev-radar", 74), "hudson/dev-radar_74_surefire_aggregated_report.xml")
             .withFile(hudsonUrlBuilder.getProjectUrl("dev-radar"), "hudson/dev-radar.xml")
             .withFile(hudsonUrlBuilder.getBuildUrl("fluxx", FLUXX_BUILT_WITH_COMMITERS), "hudson/fluxx_built_with_commiters.xml")
             .withFile(hudsonUrlBuilder.getBuildUrl("jwall", INVALID_XML), "hudson/jwall_invalid.xml")
             .withHeader("Content-Type", "application/xml; charset=utf-8")
             .create();
             return new Client(clientHandler, clientConfig);
         }
     };
 
     @Test
     public void should_retrieve_projects_from_hudson() throws JAXBException {
         List<HudsonProject> projects = hudson.findAllProjects();
         assertFalse(projects.isEmpty());
         assertEquals("dev-radar", projects.get(0).getName());
         assertEquals("fluxx", projects.get(1).getName());
     }
 
     @Test
     public void should_retrieve_projects_with_building_status() {
         List<HudsonProject> projects = hudson.findAllProjects();
         assertFalse(projects.get(0).isBuilding());
         assertTrue(projects.get(1).isBuilding());
     }
 
     @Test
     public void should_retrieve_build_with_last_commiters() throws HudsonBuildNotFoundException {
         HudsonBuild build = hudson.findBuild("fluxx", FLUXX_BUILT_WITH_COMMITERS);
         assertEquals("Julien Smadja", build.getCommiters()[0]);
         assertEquals("Arnaud Lemaire", build.getCommiters()[1]);
     }
 
     @Test
     public void should_retrieve_build_with_status() throws HudsonProjectNotFoundException {
         HudsonBuild build = hudson.findProject("dev-radar").getCompletedBuild();
         assertTrue(build.isSuccessful());
     }
 
     @Test
     public void should_retrieve_build_start_time() throws HudsonBuildNotFoundException {
         HudsonBuild build = hudson.findBuild("fluxx", FLUXX_BUILT_WITH_COMMITERS);
         assertEquals(1298022037803L, build.getStartTime().getTime());
     }
 
     @Test
     public void should_retrieve_artifact_id() throws HudsonProjectNotFoundException {
         String artifactId = hudson.findProject("fluxx").getArtifactId();
         assertEquals("fr.fluxx:fluxx", artifactId);
     }
 
     @Test
     public void should_retrieve_projects_with_description() {
         List<HudsonProject> projects = hudson.findAllProjects();
         assertEquals("Dev Radar, un mur d'informations", projects.get(0).getDescription());
         assertEquals("Fluxx, aggrégez vos flux RSS!", projects.get(1).getDescription());
     }
 
     @Test
     public void should_retrieve_average_build_duration_time() throws HudsonProjectNotFoundException {
         long duration108 = 31953;
         long duration107 = 29261;
 
         float sumDuration = duration107 + duration108;
         long averageBuildDurationTime = (long)(sumDuration / 2);
 
         long duration = hudson.getAverageBuildDurationTime("dev-radar");
         assertEquals(averageBuildDurationTime, duration);
     }
 
     @Test
     public void should_return_successful_build_numbers() throws HudsonProjectNotFoundException {
         HudsonProject projects = hudson.findProject("fluxx");
         int[] successfullBuildNumbers = hudson.getSuccessfulBuildNumbers(projects);
         assertEquals(102, successfullBuildNumbers[0]);
         assertEquals(101, successfullBuildNumbers[1]);
     }
 
     @Test
     public void should_retrieve_estimated_remaining_time() throws HudsonProjectNotFoundException {
         Date estimatedFinishTime = hudson.getEstimatedFinishTime("fluxx");
        assertNotNull(estimatedFinishTime);
     }
 
     @Test
     public void should_retrieve_test_result() throws HudsonBuildNotFoundException {
         HudsonBuild build = hudson.findBuild("dev-radar", 74);
         TestResult testResult = build.getTestResult();
         assertEquals(1, testResult.getFailCount());
         assertEquals(13, testResult.getPassCount());
         assertEquals(2, testResult.getSkipCount());
         assertEquals(16, testResult.getTotalCount());
         assertEquals(2, testResult.getIntegrationTestCount());
     }
 

     @Test(expected = HudsonBuildNotFoundException.class)
     public void should_not_fail_while_loading_invalid_xml_build() throws HudsonBuildNotFoundException {
         hudson.findBuild("jwall", INVALID_XML);
     }
 }
