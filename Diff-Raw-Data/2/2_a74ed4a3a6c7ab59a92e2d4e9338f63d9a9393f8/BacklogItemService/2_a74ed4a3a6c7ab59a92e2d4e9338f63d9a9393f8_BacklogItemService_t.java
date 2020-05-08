 package kanbannow;
 
 
 import com.yammer.dropwizard.Service;
 import com.yammer.dropwizard.config.Bootstrap;
 import com.yammer.dropwizard.config.Environment;
 import kanbannow.health.BacklogItemHealthCheck;
 
 import ch.qos.logback.classic.Logger;
 import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
 import kanbannow.logback.HealthCheckErrorRecordingAppender;
 import kanbannow.resources.HelloWorldResource;
 import org.slf4j.LoggerFactory;
 import ch.qos.logback.classic.LoggerContext;
 
 
 public class BacklogItemService extends Service<BacklogItemServiceConfiguration> {
 
 
     HealthCheckErrorRecordingAppender appender;
 
 
     public static void main(String[] args) throws Exception {
         new BacklogItemService().run(args);
     }
 
 
     @Override
     public void initialize(Bootstrap<BacklogItemServiceConfiguration> bootstrap) {
 
     }
 
     // Test change
     @Override
     public void run(BacklogItemServiceConfiguration configuration, Environment environment) throws Exception {
        environment.addResource(new HelloWorldResource());
         environment.addHealthCheck(new BacklogItemHealthCheck());
         setupExceptionThrowingLogger();
     }
 
     private void setupExceptionThrowingLogger() {
         LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
 
         appender = new HealthCheckErrorRecordingAppender();
         appender.setContext(loggerContext);
         PatternLayoutEncoder encoder = new PatternLayoutEncoder();
         encoder.setContext(loggerContext);
         encoder.setPattern("%-4relative [%thread] %-5level %logger{35} - %msg%n");
         encoder.start();
 
         appender.setEncoder(encoder);
 
         appender.start();
 
         // attach the rolling file appender to the logger of your choice
         Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
         logbackLogger.addAppender(appender);
 
     }
 
 
     public boolean warningOrErrorWasLogged() {
         return appender.wasWarningOrErrorLogged();
     }
 
 
 //    private void setupExampleLogger() {
 //        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
 //
 //        RollingFileAppender rfAppender = new RollingFileAppender();
 //        rfAppender.setContext(loggerContext);
 //        rfAppender.setFile("testFile.log");
 //        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
 //        rollingPolicy.setContext(loggerContext);
 //        // rolling policies need to know their parent
 //        // it's one of the rare cases, where a sub-component knows about its parent
 //        rollingPolicy.setParent(rfAppender);
 //        rollingPolicy.setFileNamePattern("testFile.%i.log.zip");
 //        rollingPolicy.start();
 //
 //        SizeBasedTriggeringPolicy triggeringPolicy = new SizeBasedTriggeringPolicy();
 //        triggeringPolicy.setMaxFileSize("5MB");
 //        triggeringPolicy.start();
 //
 //        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
 //        encoder.setContext(loggerContext);
 //        encoder.setPattern("%-4relative [%thread] %-5level %logger{35} - %msg%n");
 //        encoder.start();
 //
 //        rfAppender.setEncoder(encoder);
 //        rfAppender.setRollingPolicy(rollingPolicy);
 //        rfAppender.setTriggeringPolicy(triggeringPolicy);
 //
 //        rfAppender.start();
 //
 //        // attach the rolling file appender to the logger of your choice
 //        Logger logbackLogger = loggerContext.getLogger("Main");
 //        logbackLogger.addAppender(rfAppender);
 //
 //        // OPTIONAL: print logback internal status messages
 //        StatusPrinter.print(loggerContext);
 //
 //        // log something
 //        logbackLogger.debug("hello");
 //    }
 
 }
