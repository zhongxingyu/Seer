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
 
 package com.jsmadja.wall.projectwall.service;
 
 import static org.apache.commons.lang.StringUtils.isBlank;
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.Measure;
 import org.sonar.wsclient.services.Resource;
 import org.sonar.wsclient.services.ResourceQuery;
 
 import com.jsmadja.wall.projectwall.domain.Project;
 import com.jsmadja.wall.projectwall.domain.QualityMeasure;
 import com.jsmadja.wall.projectwall.domain.QualityMetric;
 import com.jsmadja.wall.projectwall.domain.QualityResult;
 import com.jsmadja.wall.projectwall.domain.SonarMetrics;
 
public final class SonarService implements QualityService {
 
     private String url;
     private String login;
     private String password;
 
     /**
      * http://docs.codehaus.org/display/SONAR/Web+Service+API
      */
     private Sonar sonar;
     private Map<String, QualityMetric> qualityMetrics = new HashMap<String, QualityMetric>();
 
     private static final Logger LOG = LoggerFactory.getLogger(SonarService.class);
 
     @Override
     public void init() {
         if (isBlank(url)) {
             throw new IllegalStateException("url can't be null.");
         }
         if (isNotBlank(login) && isNotBlank(password)) {
             sonar = Sonar.create(url);
         } else {
             sonar = Sonar.create(url, login, login);
         }
         if (LOG.isInfoEnabled()) {
             LOG.info("Initialize sonar with url " + url);
         }
         createMetricList();
     }
 
     private void createMetricList() {
         String metricUrl = url+"/api/metrics?format=xml";
         try {
             InputStream xmlStream = new URL(metricUrl).openStream();
             Unmarshaller unmarshaller = JAXBContext.newInstance(SonarMetrics.class).createUnmarshaller();
             SonarMetrics sonarMetrics = SonarMetrics.class.cast(unmarshaller.unmarshal(xmlStream));
             for (QualityMetric metric:sonarMetrics.metric) {
                 qualityMetrics.put(metric.getKey(), metric);
             }
         } catch (MalformedURLException e) {
             LOG.error("url: "+metricUrl,e);
             throw new RuntimeException(e);
         } catch (IOException e) {
             LOG.error("url: "+metricUrl,e);
             throw new RuntimeException(e);
         } catch (JAXBException e) {
             LOG.error("url: "+metricUrl,e);
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @param projectId
      * @return
      * @throws SonarProjectNotFoundException
      */
     public final Double getCoverage(String projectId) throws SonarProjectNotFoundException {
         Measure coverage = getMeasure(projectId, "coverage");
         if (LOG.isDebugEnabled()) {
             LOG.debug("Coverage measure for project #" + projectId + " is " + coverage);
         }
         return coverage.getValue();
     }
 
     /**
      * @param projectId
      * @return
      * @throws SonarProjectNotFoundException
      */
     public final Double getRulesCompliance(String projectId) throws SonarProjectNotFoundException {
         Measure rulesCompliance = getMeasure(projectId, "violations_density");
         if (LOG.isDebugEnabled()) {
             LOG.debug("Rules compliance measure for project #" + projectId + " is " + rulesCompliance);
         }
         return rulesCompliance.getValue();
     }
 
     private Measure getMeasure(String projectId, String measureKey) throws SonarProjectNotFoundException {
         if (StringUtils.isBlank(projectId)) {
             throw new SonarProjectNotFoundException("No projectId provided");
         }
         Resource resource = sonar.find(ResourceQuery.createForMetrics(projectId, measureKey));
         if (resource == null) {
             throw new SonarProjectNotFoundException("Project with id #" + projectId + " not found in sonar " + url);
         }
         return resource.getMeasure(measureKey);
     }
 
     @Override
     public void populateQuality(Project project, QualityResult quality, String... metrics)
     throws ProjectNotFoundException {
         try {
             for (String key : metrics) {
                 Measure measure = getMeasure(project.getId(), key);
                 if (measure != null) {
                     Double value = measure.getValue();
                     if (value != null) {
                         QualityMeasure qualityMeasure = new QualityMeasure();
                         qualityMeasure.setName(qualityMetrics.get(key).getName());
                         qualityMeasure.setValue(value);
                         qualityMeasure.setFormattedValue(measure.getFormattedValue());
                         quality.add(key, qualityMeasure);
                     }
                 }
             }
         } catch (SonarProjectNotFoundException e) {
             throw new ProjectNotFoundException(e);
         }
     }
 
     @Override
     public void setUrl(String url) {
         this.url = url;
     }
 
     @Override
     public void setLogin(String login) {
         this.login = login;
     }
 
     @Override
     public void setPassword(String password) {
         this.password = password;
     }
 
 }
