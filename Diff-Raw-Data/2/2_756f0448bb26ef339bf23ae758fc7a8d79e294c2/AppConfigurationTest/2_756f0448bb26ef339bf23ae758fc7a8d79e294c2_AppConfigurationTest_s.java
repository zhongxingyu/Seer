 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.Resource;
 import javax.sql.DataSource;
 
 import org.h2.Driver;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.helpers.MarkerIgnoringBase;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Lazy;
 import org.springframework.jdbc.datasource.DataSourceTransactionManager;
 import org.springframework.jdbc.datasource.SimpleDriverDataSource;
 import org.springframework.scheduling.annotation.Async;
 import org.springframework.scheduling.annotation.AsyncConfigurer;
 import org.springframework.scheduling.annotation.EnableAsync;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 import org.springframework.transaction.annotation.Transactional;
 
 public class AppConfigurationTest {
 
   private static CountDownLatch errorLatch = new CountDownLatch(2);
   private static CountDownLatch transactionLatch = new CountDownLatch(1);
 
   @SuppressWarnings("serial")
   private static Logger errorLatchLogger = new SilentLoggerAdapter() {
     @Override
     public void error(String msg, Throwable t) {
       errorLatch.countDown();
     }
   };
 
   private static AnnotationConfigApplicationContext ctx;
 
   @Before
   public void setup() {
     ctx = new AnnotationConfigApplicationContext(TestConfiguration.class, DoSomethingAsync.class, CallAsyncAfterTransaction.class);
   }
 
   @After
   public void teardown() {
     ctx.close();
   }
 
   @Test
   public void should_log_exceptions_in_async_methods() throws InterruptedException {
     DoSomethingAsync doSomethingAsync = ctx.getBean(DoSomethingAsync.class);
 
     doSomethingAsync.asyncError();
     doSomethingAsync.asyncErrorWithResult();
 
     assertThat(errorLatch.await(100, TimeUnit.MILLISECONDS), is(true));
 
     ctx.close();
   }
 
   @Test
   public void should_commit_transaction_before_async_call() throws InterruptedException {
     CallAsyncAfterTransaction subject = ctx.getBean(CallAsyncAfterTransaction.class);
 
     boolean asyncTaskDidRun = subject.runInTransaction();
 
     assertThat(asyncTaskDidRun, is(false));
 
     ctx.close();
   }
 
   @Configuration
   @EnableTransactionManagement
   @EnableAsync
   public static class TestConfiguration implements AsyncConfigurer {
     @Override
     public Executor getAsyncExecutor() {
       LoggingExecutor executor = (LoggingExecutor) new AppConfiguration().getAsyncExecutor();
       executor.setLogger(errorLatchLogger);
       return executor;
     }
 
     @Bean
     @Lazy
     public PlatformTransactionManager transactionManager() {
      DataSource dataSource = new SimpleDriverDataSource(new Driver(), "jdbc:h2:mem:test_mem", "sa", "");
       return new DataSourceTransactionManager(dataSource);
     }
   }
 
   @Component
   public static class CallAsyncAfterTransaction {
 
     @Resource private DoSomethingAsync asyncComponent;
 
     @Transactional
     public boolean runInTransaction() throws InterruptedException {
       asyncComponent.decreaseLatch();
       return transactionLatch.await(100, TimeUnit.MILLISECONDS);
     }
   }
 
   @Component
   public static class DoSomethingAsync {
     @Async
     public void decreaseLatch() {
       transactionLatch.countDown();
     }
 
     @Async
     public void asyncError() {
       throw new RuntimeException("AAARRGGGHH.. wrong");
     }
 
     @Async
     public Future<Object> asyncErrorWithResult() {
       throw new RuntimeException("AAARRGGGHH.. wrong");
     }
   }
 
   @SuppressWarnings("serial")
   private static class SilentLoggerAdapter extends MarkerIgnoringBase {
     @Override
     public void warn(String format, Object arg1, Object arg2) {
     }
 
     @Override
     public void warn(String msg, Throwable t) {
     }
 
     @Override
     public void warn(String format, Object... arguments) {
     }
 
     @Override
     public void warn(String format, Object arg) {
     }
 
     @Override
     public void warn(String msg) {
     }
 
     @Override
     public void trace(String format, Object arg1, Object arg2) {
     }
 
     @Override
     public void trace(String msg, Throwable t) {
     }
 
     @Override
     public void trace(String format, Object... arguments) {
     }
 
     @Override
     public void trace(String format, Object arg) {
     }
 
     @Override
     public void trace(String msg) {
     }
 
     @Override
     public boolean isWarnEnabled() {
       return false;
     }
 
     @Override
     public boolean isTraceEnabled() {
       return false;
     }
 
     @Override
     public boolean isInfoEnabled() {
       return false;
     }
 
     @Override
     public boolean isErrorEnabled() {
       return false;
     }
 
     @Override
     public boolean isDebugEnabled() {
       return false;
     }
 
     @Override
     public void info(String format, Object arg1, Object arg2) {
     }
 
     @Override
     public void info(String msg, Throwable t) {
     }
 
     @Override
     public void info(String format, Object... arguments) {
     }
 
     @Override
     public void info(String format, Object arg) {
     }
 
     @Override
     public void info(String msg) {
     }
 
     @Override
     public void error(String format, Object arg1, Object arg2) {
     }
 
     @Override
     public void error(String msg, Throwable t) {
     }
 
     @Override
     public void error(String format, Object... arguments) {
     }
 
     @Override
     public void error(String format, Object arg) {
     }
 
     @Override
     public void error(String msg) {
     }
 
     @Override
     public void debug(String format, Object arg1, Object arg2) {
     }
 
     @Override
     public void debug(String msg, Throwable t) {
     }
 
     @Override
     public void debug(String format, Object... arguments) {
     }
 
     @Override
     public void debug(String format, Object arg) {
     }
 
     @Override
     public void debug(String msg) {
     }
   };
 }
