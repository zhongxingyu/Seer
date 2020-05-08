 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.medic.log.impl;
 
 import java.io.PrintStream;
 import java.util.Locale;
 
 import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * A <code>LoggingPrintStreamWrapper</code> wraps a PrintStream instance and logs, via SLF4j,
  * all data that is written to it. 
  * <p />
  *
  * <strong>Concurrent Semantics</strong><br />
  * Thread-safe.
  *
  */
 public final class LoggingPrintStreamWrapper extends PrintStream {
 
     private static final class StringBuilderThreadLocal extends ThreadLocal<StringBuilder> {
 
         @Override
         public StringBuilder initialValue() {
             return new StringBuilder();
         }
     }
 
     private static final String LOGBACK_PACKAGE_NAME_PREFIX = "ch.qos.logback";
 
 	private final ThreadLocal<StringBuilder> entryBuilders;
     
    private final Logger logger;
     
     private final ExecutionStackAccessor executionStackAccessor;
     
     private final ConfigurationProvider configurationProvider;
     
     private final String configurationProperty;
     
     private final PrintStream originalPrintStream;
     
     private final LoggingLevel loggingLevel;
    
     private static final String NULL_STRING = "null";
     
     /**
      * Creates a new LoggingPrintStreamWrapper for the given PrintStream. Data written to
      * the stream is logged via SLF4j to a logger with the supplied <code>loggerName</code>.<br/>
      * The logging level is determined by the parameter of type {@link LoggingLevel}.
      * 
      * @param printStream The PrintStream instance to wrap
      * @param loggerName The name of the logger
      * @param loggingLevel The level of the log entries created
      * @param executionStackAccessor 
      * @param configurationProvider 
      * @param configurationProperty 
      */
     public LoggingPrintStreamWrapper(PrintStream printStream, String loggerName, LoggingLevel loggingLevel, ExecutionStackAccessor executionStackAccessor, ConfigurationProvider configurationProvider, String configurationProperty) {
         super(printStream);
         
        this.logger = LoggerFactory.getLogger(loggerName);
         this.loggingLevel = loggingLevel;
         
         this.executionStackAccessor = executionStackAccessor;
         
         this.entryBuilders = new StringBuilderThreadLocal();
         
         this.configurationProvider = configurationProvider;
         
         this.configurationProperty = configurationProperty;
         
         this.originalPrintStream = printStream;        
     }
 
     /**
      * Creates a new LoggingPrintStreamWrapper for the given PrintStream. Data written to
      * the stream is logged via SLF4j to a logger with the supplied <code>loggerName</code>.<br/>
      * (The logging level is DEBUG by default.)
      * 
      * @param printStream The PrintStream instance to wrap
      * @param loggerName The name of the logger
      * @param executionStackAccessor
      * @param configurationProvider
      * @param configurationProperty
      */
     public LoggingPrintStreamWrapper(PrintStream printStream, String loggerName, ExecutionStackAccessor executionStackAccessor, ConfigurationProvider configurationProvider, String configurationProperty) {
         this(printStream, loggerName, LoggingLevel.DEBUG, executionStackAccessor, configurationProvider, configurationProperty);
     }
     
     @Override
     public PrintStream append(char c) {
     	if (!isLoggingEnabled()) {
     		super.append(c);
     	} else {
 	    	this.internalAppend(c);
     	}
         return this;
     }
     
     private boolean internalAppend(char c) {    	
         if (c == '\n' || c == '\r') {
             createEntryAndLog(entryBuilders.get());
             return true;
         } else {
             entryBuilders.get().append(c);
             return false;
         }
     }
 
     @Override
     public PrintStream append(CharSequence csq, int start, int end) {
     	if (!isLoggingEnabled()) {
     		super.append(csq, start, end);
     	} else {
 	        this.internalAppend(csq, start, end);
     	}
 
         return this;
     }
 
     private void internalAppend(CharSequence csq, int start, int end) {
         for (int i = start; i < end; i++) {
             boolean loggedEntry = internalAppend(csq.charAt(i));
             if (loggedEntry && i < (end - 1)) {
                 char c = csq.charAt(i + 1);
                 if (c == '\n' || c == '\r') {
                     i++;
                 }
             }
         }
     }
 
     @Override
     public PrintStream append(CharSequence csq) {
     	if (!isLoggingEnabled()) {
     		super.append(csq);
     	} else {
 	        if(csq == null){
	        	throw new NullPointerException("Character Sequence to be added to the printStream from source '" + this.logger.getName() + "' is null");
 	        }
 	        this.internalAppend(csq, 0, csq.length());
     	}
     	return this;
     }
     
     @Override
     public boolean checkError() {
     	if (!isLoggingEnabled()) {
     		return super.checkError();
     	} else {
     		return false;
     	}
     }
 
     @Override
     public void close() {
     	if (!isLoggingEnabled()) {
     		super.close();
     	}
     }
 
     @Override
     public void flush() {
     	if (!isLoggingEnabled()) {
     		super.flush();
     	}
     }
 
     @Override
     public PrintStream format(Locale l, String format, Object... args) {
     	if (!isLoggingEnabled()) {
     		super.format(l, format, args);
     	} else {
     		this.internalPrint(String.format(l, format, args));
     	}
         return this;
     }
 
     @Override
     public PrintStream format(String format, Object... args) {
     	if (!isLoggingEnabled()) {
     		super.format(format, args);
     	} else {
     		this.internalPrint(String.format(format, args));
     	}
         return this;
     }
 
     @Override
     public void print(boolean b) {
     	if (!isLoggingEnabled()) {
     		super.print(b);
     	} else {
     		this.internalPrint(b);
     	}
     }
 
     private void internalPrint(boolean b) {
         entryBuilders.get().append(b);
     }
 
     @Override
     public void print(char c) {
     	if (!isLoggingEnabled()) {
     		super.print(c);
     	} else {
     		this.internalAppend(c);
     	}
     }
 
     @Override
     public void print(char[] ca) {
     	if (!isLoggingEnabled()) {
     		super.print(ca);
     	} else {
     	    this.internalPrint(ca);
     	}
     }
     
     private void internalPrint(char[] ca) {
         final String s = new String(ca);
         this.internalAppend(s, 0, s.length());
     }
 
     @Override
     public void print(double d) {
     	if (!isLoggingEnabled()) {
     		super.print(d);
     	} else {
     		this.internalPrint(d);
     	}
     }
 
     private void internalPrint(double d) {
         entryBuilders.get().append(d);
     }
 
     @Override
     public void print(float f) {
     	if (!isLoggingEnabled()) {
     		super.print(f);
     	} else {
     		this.internalPrint(f);
     	}
     }
 
     private void internalPrint(float f) {
         entryBuilders.get().append(f);
     }
 
     @Override
     public void print(int i) {
     	if (!isLoggingEnabled()) {
     		super.print(i);
     	} else {
     		this.internalPrint(i);
     	}
     }
 
     private void internalPrint(int i) {
         entryBuilders.get().append(i);
     }
 
     @Override
     public void print(long l) {
     	if (!isLoggingEnabled()) {
     		super.print(l);
     	} else {
     		this.internalPrint(l);
     	}
     }
 
     private void internalPrint(long l) {
         entryBuilders.get().append(l);
     }
 
     @Override
     public void print(Object obj) {
     	if (!isLoggingEnabled()) {
     		super.print(obj);
     	} else {
 	        this.internalPrint(obj);
     	}
     }
 
     private void internalPrint(Object obj) {
         if (obj == null) {
             entryBuilders.get().append(NULL_STRING);
         } else {
             internalPrint(obj.toString().toCharArray());
         }
     }
 
     @Override
     public void print(String s) {
     	if (!isLoggingEnabled()) {
     		super.print(s);
     	} else {
 	    	this.internalPrint(s);
     	}
     }
     private void internalPrint(String s) {
         if (s == null) {
             s = NULL_STRING;
         }
         this.internalAppend(s, 0, s.length());
     }
 
     @Override
     public PrintStream printf(Locale l, String format, Object... args) {
     	if (!isLoggingEnabled()) {
     		super.printf(l, format, args);
     	} else {  
             this.internalPrint(String.format(l, format, args));
     	}
     	return this;
     }
 
     @Override
     public PrintStream printf(String format, Object... args) {
     	if (!isLoggingEnabled()) {
     		super.printf(format, args);
     	} else {
     		this.internalPrint(String.format(format, args));
     	}
     	return this;
     }
 
     @Override
     public void println() {
     	if (!isLoggingEnabled()) {
     		super.println();
     	} else { 
     		createEntryAndLog(entryBuilders.get());
     	}
     }
 
     @Override
     public void println(boolean x) {
     	if (!isLoggingEnabled()) {
     		super.println(x);
     	} else {
 	        this.internalPrint(x);
 	        createEntryAndLog(entryBuilders.get());
     	}
     }
 
     @Override
     public void println(char x) {
     	if (!isLoggingEnabled()) {
     		super.println(x);
     	} else { 
 	        this.internalAppend(x);
 	        createEntryAndLog(entryBuilders.get());
     	}
     }
 
     @Override
     public void println(char[] x) {
     	if (!isLoggingEnabled()) {
     		super.println(x);
     	} else {
 	        this.internalPrint(x);
 	        createEntryAndLog(entryBuilders.get());
     	}
     }
 
     @Override
     public void println(double x) {
     	if (!isLoggingEnabled()) {
     		super.println(x);
     	} else {
 	        this.internalPrint(x);
 	        createEntryAndLog(entryBuilders.get());
     	}
     }
 
     @Override
     public void println(float x) {
     	if (!isLoggingEnabled()) {
     		super.println(x);
     	} else {
 	        this.internalPrint(x);
 	        createEntryAndLog(entryBuilders.get());
     	}
     }
 
     @Override
     public void println(int x) {
     	if (!isLoggingEnabled()) {
     		super.println(x);
     	} else {
     		this.internalPrint(x);
     		createEntryAndLog(entryBuilders.get());
     	}
     }
 
     @Override
     public void println(long x) {
     	if (!isLoggingEnabled()) {
     		super.println(x);
     	} else {
     		this.internalPrint(x);
     		createEntryAndLog(entryBuilders.get());
     	}
     }
 
     @Override
     public void println(Object x) {
     	if (!isLoggingEnabled()) {
     		super.println(x);
     	} else {
 	        this.internalPrint(x);
 	        createEntryAndLog(entryBuilders.get());
     	}
     }
 
     @Override
     public void println(String x) {
     	if (!isLoggingEnabled()) {
     		super.println(x);
     	} else {
     		this.internalPrint(x);
     		createEntryAndLog(entryBuilders.get());
     	}
     }
 
     @Override
     protected void setError() {
     	if (!isLoggingEnabled()) {
     		super.setError();
     	} 
     }
 
     @Override
     public void write(byte[] buf, int off, int len) {
     	if (!isLoggingEnabled()) {
     		super.write(buf, off, len);
     	} else {
 	        byte[] outputBytes = new byte[len];
 	        System.arraycopy(buf, off, outputBytes, 0, len);
 	        this.internalPrint(new String(outputBytes));
     	}
     }
 
     @Override
     public void write(int b) {
     	if (!isLoggingEnabled()) {
     		super.write(b);
     	} else {
 	        if (b == '\n' || b == '\r') {
 	            createEntryAndLog(entryBuilders.get());
 	        } else {
 	            entryBuilders.get().append(new String(new byte[] {(byte)b}));
 	        }
     	}
     }   
     
     private void createEntryAndLog(final StringBuilder stringBuilder) {
         final String string = stringBuilder.toString();
         switch (this.loggingLevel) {
            case DEBUG:     this.logger.debug(string); break;
            case ERROR:     this.logger.error(string); break;
            case INFO:      this.logger.info(string);  break;
            case WARNING:   this.logger.warn(string);  break;
         }
         entryBuilders.remove();
     }
     
     private boolean isLoggingEnabled() {
     	return isEnabledInConfiguration() && !isWithinLogback();
     }
 
 	private boolean isWithinLogback() {
 		Class<?>[] executionStack = this.executionStackAccessor.getExecutionStack();
     	
     	for (Class<?> clazz : executionStack) {
     		Package pkg = clazz.getPackage();
     		if (pkg != null) {
     			String pkgName = pkg.getName();
     			if (pkgName != null && pkgName.startsWith(LOGBACK_PACKAGE_NAME_PREFIX)) {
     				return true;
     			}
     		}
     	}
     	
     	return false;
 	}
 
 	private boolean isEnabledInConfiguration() {
 		return Boolean.valueOf((String)this.configurationProvider.getConfiguration().get(this.configurationProperty));
 	}
 	
 	public PrintStream getOriginalPrintStream() {
 	    return this.originalPrintStream;
 	}
 }
