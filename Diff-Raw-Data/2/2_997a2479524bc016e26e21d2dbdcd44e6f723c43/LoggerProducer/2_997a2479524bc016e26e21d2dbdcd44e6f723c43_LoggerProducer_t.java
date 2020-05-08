package com.ctp.javaone.archiver.log;
 
 import javax.enterprise.inject.Produces;
 import javax.enterprise.inject.spi.InjectionPoint;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class LoggerProducer {
     
     @Produces
     public Logger createLogger(InjectionPoint injection) {
         return LoggerFactory.getLogger(injection.getBean().getBeanClass());
     }
 
 }
