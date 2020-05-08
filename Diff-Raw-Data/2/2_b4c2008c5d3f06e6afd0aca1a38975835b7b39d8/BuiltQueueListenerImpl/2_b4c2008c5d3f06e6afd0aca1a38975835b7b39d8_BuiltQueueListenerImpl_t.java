 /*
  * Copyright © 2013 Turkcell Teknoloji Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.ttech.cordovabuild.domain.built;
 
 import com.ttech.cordovabuild.domain.application.ApplicationBuilt;
 import com.ttech.cordovabuild.domain.application.ApplicationService;
 import com.ttech.cordovabuild.domain.application.BuiltTarget;
 import com.ttech.cordovabuild.domain.application.BuiltType;
 import com.ttech.cordovabuild.infrastructure.queue.QueueListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
 import org.springframework.stereotype.Component;
 
 import javax.persistence.OptimisticLockException;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: capacman
  * Date: 9/1/13
  * Time: 4:46 PM
  * To change this template use File | Settings | File Templates.
  */
 @Component
 public class BuiltQueueListenerImpl implements QueueListener {
     private static final Logger LOGGER = LoggerFactory.getLogger(BuiltQueueListenerImpl.class);
     public static final int SLEEP_TIME_CONSTANT = 1000;
     @Autowired
     ApplicationService applicationService;
     @Autowired
     ApplicationBuilderFactory builderFactory;
 
     private Random randomGenerator = new Random(System.currentTimeMillis());
 
     public void onBuilt(ApplicationBuilt applicationBuilt, BuiltType builtType) {
         BuiltInfo builtInfo = null;
         try {
             applicationBuilt = updateApplicationBuilt(applicationBuilt, new BuiltInfo(builtType, BuiltTarget.Status.STARTED), 0);
             ApplicationBuilder applicationBuilder = builderFactory.getApplicationBuilder(builtType, applicationBuilt);
             try {
                 builtInfo = applicationBuilder.buildApplication();
             } catch (Exception e) {
                 //TODO should be more specific and should include reason
                 builtInfo = new BuiltInfo(builtType, BuiltTarget.Status.FAILED);
             }
             updateApplicationBuilt(applicationBuilt, builtInfo, 0);
             return;
         } catch (IllegalArgumentException e) {
             updateApplicationBuilt(applicationBuilt, BuiltInfo.failedFor(applicationBuilt.getApplication().getApplicationConfig().getApplicationName(), builtType), 0);
         }
     }
 
     private ApplicationBuilt updateApplicationBuilt(ApplicationBuilt applicationBuilt, BuiltInfo builtInfo, int count) {
         if (count > 0)
             LOGGER.info("retrying applicationBuilt update with count {}", count + 1);
         try {
             return applicationService.updateApplicationBuilt(applicationBuilt.update(builtInfo));
        } catch (javax.persistence.OptimisticLockException | JpaOptimisticLockingFailureException | org.eclipse.persistence.exceptions.OptimisticLockException e) {
             LOGGER.warn("optimisticLockingException for applicationBuilt");
         }
         int sleepTime = randomGenerator.nextInt(SLEEP_TIME_CONSTANT) + SLEEP_TIME_CONSTANT;
         LOGGER.info("sleep {} ms and retry", sleepTime);
         try {
             Thread.sleep(sleepTime);
         } catch (InterruptedException e) {
             LOGGER.warn("sleep interrupted");
         }
         return updateApplicationBuilt(applicationService.findApplicationBuilt(applicationBuilt.getId()), builtInfo, count + 1);
     }
 }
