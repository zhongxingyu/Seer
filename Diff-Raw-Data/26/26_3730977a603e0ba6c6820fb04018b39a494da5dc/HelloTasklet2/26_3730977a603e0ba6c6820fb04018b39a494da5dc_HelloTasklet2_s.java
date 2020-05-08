 package com.googlecode.goodsamples.springbatch.hello;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.batch.core.StepContribution;
 import org.springframework.batch.core.scope.context.ChunkContext;
 import org.springframework.batch.core.step.tasklet.Tasklet;
 import org.springframework.batch.repeat.RepeatStatus;
 import org.springframework.stereotype.Component;
 
 @Component
 public class HelloTasklet2 implements Tasklet {
         Log log = LogFactory.getLog(HelloTasklet.class);
 
         public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                 log.info("Hello");
                 return RepeatStatus.FINISHED;
         }
         
           public int duplicateCode(){
           int i = 0;
             if(i==1){
               log.info("AAA");
             }else if(i != 0){
               if(i == 0){
                   log.info("AAA");
               }else{
                   log.info("BBB");
               }
             }
           }
 
           public int duplicateCode2(){
           int i = 0;
             if(i==1){
               log.info("AAA");
             }else if(i != 0){
               if(i == 0){
                   log.info("AAA");
               }else{
                   log.info("BBB");
               }
             }
          }
          
           
           public int duplicateCode3(){
           int i = 0;
             if(i==1){
               log.info("AAA");
             }else if(i != 0){
               if(i == 0){
                   log.info("AAA");
               }else{
                   log.info("BBB");
               }
             }
           }
 }
