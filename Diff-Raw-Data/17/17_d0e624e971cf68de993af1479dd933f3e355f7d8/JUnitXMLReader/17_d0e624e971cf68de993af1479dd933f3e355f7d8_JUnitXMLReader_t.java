 /**
  * Copyright (c) 2010 MATSUFUJI Hideharu <matsufuji2008@gmail.com>,
  * All rights reserved.
  *
  * This file is part of MakeGood.
  *
  * This program and the accompanying materials are made available under
  * the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package com.piece_framework.makegood.core.run;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.piece_framework.makegood.core.result.ResultType;
 import com.piece_framework.makegood.core.result.TestCaseResult;
 import com.piece_framework.makegood.core.result.TestSuiteResult;
 
 public class JUnitXMLReader extends DefaultHandler {
     private File log;
     private TestSuiteResult result;
     private TestSuiteResult currentTestSuite;
     private TestCaseResult currentTestCase;
     private StringBuilder failureTrace;
     private List<JUnitXMLReaderListener> listeners = new ArrayList<JUnitXMLReaderListener>();
     private boolean stopped = false;
     private SynchronizedFileInputStream stream;
 
     public JUnitXMLReader(File log) {
         this.log = log;
     }
 
     public void read()
         throws ParserConfigurationException, SAXException, IOException {
         if (!log.exists()) {
             log.createNewFile();
         }
 
         SAXParserFactory factory = SAXParserFactory.newInstance();
         SAXParser parser = factory.newSAXParser();
         stream = new SynchronizedFileInputStream(log);
         parser.parse(stream, this);
     }
 
     public void stop() {
         stopped = true;
     }
 
     public void addListener(JUnitXMLReaderListener listener) {
         listeners.add(listener);
     }
 
     public void removeParserListener(JUnitXMLReaderListener listener) {
         listeners.remove(listener);
     }
 
     @Override
     public void startDocument() throws SAXException {
     }
 
     @Override
     public void startElement(String uri,
                              String localName,
                              String qualifiedName,
                              Attributes attributes
                              ) throws SAXException {
         if (qualifiedName.equalsIgnoreCase("testsuite")) { //$NON-NLS-1$
             startTestSuite(createTestSuite(attributes));
         } else if (qualifiedName.equalsIgnoreCase("testcase")) { //$NON-NLS-1$
             startTestCase(createTestCase(attributes));
         } else if (qualifiedName.equalsIgnoreCase("failure")) { //$NON-NLS-1$
             startFailure(createFailureTestCase(attributes));
         } else if (qualifiedName.equalsIgnoreCase("error")) { //$NON-NLS-1$
             startFailure(createErrorTestCase(attributes));
         }
     }
 
     @Override
     public void characters(char[] characters,
                            int start,
                            int length
                            ) throws SAXException {
         if (failureTrace != null) {
             failureTrace.append(new String(characters, start, length));
         }
     }
 
     @Override
     public void endElement(String uri,
                            String localName,
                            String qualifiedName
                            ) throws SAXException {
         if (qualifiedName.equalsIgnoreCase("testsuite")) { //$NON-NLS-1$
             endTestSuite();
         } else if (qualifiedName.equalsIgnoreCase("testcase")) { //$NON-NLS-1$
             endTestCase();
         } else if (qualifiedName.equalsIgnoreCase("failure")) { //$NON-NLS-1$
             endFailure();
         } else if (qualifiedName.equalsIgnoreCase("error")) { //$NON-NLS-1$
             endFailure();
         }
     }
 
     @Override
     public void endDocument() throws SAXException {
         stop();
 
         for (JUnitXMLReaderListener listener: listeners) {
             listener.endTest();
         }
     }
 
     @Override
     public void error(SAXParseException e) throws SAXException {
         if (stopped) {
             return;
         }
 
         super.error(e);
     }
 
     @Override
     public void fatalError(SAXParseException e) throws SAXException {
         if (stopped) {
             return;
         }
 
         super.fatalError(e);
     }
 
     public TestSuiteResult getResult() {
         return result;
     }
 
     public boolean isActive() {
         if (stream == null) {
             return false;
         }
         if (stream.closed) {
             return false;
         }
         if (stopped) {
             return false;
         }
         return true;
     }
 
     private void startTestSuite(TestSuiteResult suite) {
         if (result != null) {
             currentTestSuite.addChild(suite);
         } else {
             result = suite;
         }
 
         currentTestSuite = suite;
 
         for (JUnitXMLReaderListener listener: listeners) {
             listener.startTestSuite(suite);
         }
     }
 
     private void endTestSuite() {
         if (currentTestSuite != null) {
             currentTestSuite = (TestSuiteResult) currentTestSuite.getParent();
         }
 
         for (JUnitXMLReaderListener listener: listeners) {
             listener.endTestSuite();
         }
     }
 
     private void startTestCase(TestCaseResult testCase) {
         if (currentTestSuite != null) {
             currentTestSuite.addChild(testCase);
         }
 
         currentTestCase = testCase;
 
         for (JUnitXMLReaderListener listener: listeners) {
             listener.startTestCase(testCase);
         }
     }
 
     private void endTestCase() {
        currentTestCase.fix();
         currentTestCase = null;
 
         for (JUnitXMLReaderListener listener: listeners) {
             listener.endTestCase();
         }
     }
 
     private void startFailure(TestCaseResult failure) {
         failureTrace = new StringBuilder();
 
         for (JUnitXMLReaderListener listener: listeners) {
             listener.startFailure(failure);
         }
     }
 
     private void endFailure() {
         currentTestCase.setFailureTrace(failureTrace.toString());
 
         for (JUnitXMLReaderListener listener: listeners) {
             listener.endFailure();
         }
 
         if (currentTestCase.isArtificial()) {
             endTestCase();
         }
     }
 
     private TestSuiteResult createTestSuite(Attributes attributes) {
         TestSuiteResult suite = new TestSuiteResult(attributes.getValue("name")); //$NON-NLS-1$
         if (attributes.getIndex("file") != -1) { //$NON-NLS-1$
             suite.setFile(attributes.getValue("file")); //$NON-NLS-1$
         }
         if (attributes.getIndex("fullPackage") != -1) { //$NON-NLS-1$
             suite.setFullPackageName(attributes.getValue("fullPackage")); //$NON-NLS-1$
         }
         if (attributes.getIndex("package") != -1) { //$NON-NLS-1$
             suite.setPackageName(attributes.getValue("package")); //$NON-NLS-1$
         }
 
         if (result == null) {
             suite.setAllTestCount(Integer.parseInt(attributes.getValue("tests"))); //$NON-NLS-1$
         }
 
         return suite;
     }
 
     private TestCaseResult createTestCase(Attributes attributes) {
         TestCaseResult testCase = new TestCaseResult(attributes.getValue("name")); //$NON-NLS-1$
         if (attributes.getIndex("file") != -1) { //$NON-NLS-1$
             testCase.setFile(attributes.getValue("file")); //$NON-NLS-1$
         }
         if (attributes.getIndex("class") != -1) { //$NON-NLS-1$
             testCase.setClassName(attributes.getValue("class")); //$NON-NLS-1$
         }
         if (attributes.getIndex("line") != -1) { //$NON-NLS-1$
             testCase.setLine(Integer.parseInt(attributes.getValue("line"))); //$NON-NLS-1$
         }
 
         return testCase;
     }
 
     private TestCaseResult createFailureTestCase(Attributes attributes) {
         if (currentTestCase != null) {
             currentTestCase.setResultType(ResultType.FAILURE);
         } else {
             currentTestCase = new TestCaseResult("(Failure)"); //$NON-NLS-1$
             currentTestCase.setClassName(currentTestSuite.getName());
             currentTestCase.setFile(currentTestSuite.getFile());
             currentTestCase.setIsArtificial(true);
             currentTestCase.setResultType(ResultType.FAILURE);
             startTestCase(currentTestCase);
         }
 
         if (attributes.getIndex("type") != -1) { //$NON-NLS-1$
             currentTestCase.setFailureType(attributes.getValue("type")); //$NON-NLS-1$
         }
 
         return currentTestCase;
     }
 
     private TestCaseResult createErrorTestCase(Attributes attributes) {
         if (currentTestCase != null) {
             currentTestCase.setResultType(ResultType.ERROR);
         } else {
             currentTestCase = new TestCaseResult("(Error)"); //$NON-NLS-1$
             currentTestCase.setClassName(currentTestSuite.getName());
             currentTestCase.setFile(currentTestSuite.getFile());
             currentTestCase.setIsArtificial(true);
             currentTestCase.setResultType(ResultType.ERROR);
             startTestCase(currentTestCase);
         }
 
         if (attributes.getIndex("type") != -1) { //$NON-NLS-1$
             currentTestCase.setFailureType(attributes.getValue("type")); //$NON-NLS-1$
         }
 
         return currentTestCase;
     }
 
     private class SynchronizedFileInputStream extends FileInputStream{
         private static final int READ_NO_PARAM = 1;
         private static final int READ_ARRAY = 2;
         private static final int READ_OFFSET = 3;
         boolean closed = false;
 
         public SynchronizedFileInputStream(File file) throws FileNotFoundException {
             super(file);
         }
 
         @Override
         public int read() throws IOException {
             return read(READ_NO_PARAM, null, 0, 0);
         }
 
         @Override
         public int read(byte[] bytes,
                         int offset,
                         int length
                         ) throws IOException {
             return read(READ_OFFSET, bytes, offset, length);
         }
 
         @Override
         public int read(byte[] bytes) throws IOException {
             return read(READ_ARRAY, bytes, 0, 0);
         }
 
         @Override
         public void close() throws IOException {
             closed = true;
             super.close();
         }
 
         private int read(int readType,
                          byte[] bytes,
                          int offset,
                          int length
                          ) throws IOException {
             int result = -1;
             do {
                 if (readType == READ_NO_PARAM) {
                     result = super.read();
                 } else if (readType == READ_ARRAY) {
                     result = super.read(bytes);
                 } else if (readType == READ_OFFSET) {
                     result = super.read(bytes, offset, length);
                 }
                 if (result != -1) {
                     break;
                 }
 
                 if (stopped) {
                     break;
                 }
 
                 try {
                     Thread.sleep(200);
                 } catch (InterruptedException e) {}
             } while (true);
             return result;
         }
     }
 }
