 /*******************************************************************************
  * Copyright (c) 2013 Rene Schneider, GEBIT Solutions GmbH and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package de.gebit.integrity;
 
 import hudson.Launcher;
 import hudson.model.TaskListener;
 import hudson.tasks.test.DefaultTestResultParserImpl;
 import hudson.tasks.test.TestResult;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.SequenceInputStream;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 /**
  * The actual parser for parsing the Integrity result files and extraction of summary information.
  * 
  * @author Rene Schneider - initial API and implementation
  * 
  */
 public class IntegrityTestResultParser extends DefaultTestResultParserImpl {
 
 	/**
 	 * The serial version.
 	 */
 	private static final long serialVersionUID = 4841424533054027138L;
 
 	@Override
 	protected TestResult parse(List<File> someReportFiles, Launcher aLauncher, TaskListener aListener)
 			throws InterruptedException, IOException {
 		final IntegrityCompoundTestResult tempCompoundTestResult = new IntegrityCompoundTestResult();
 
 		for (File tempFile : someReportFiles) {
 			aListener.getLogger().println("Now parsing Integrity test result file: " + tempFile.getAbsolutePath());
 
 			FileReader tempFileReader = null;
 			try {
 				// Read the file into memory. Mainly done to archive it in the result, but the buffer is also fed into
 				// a SAX parser below to prevent reading the file twice.
 				FileInputStream tempInputStream = new FileInputStream(tempFile);
 				final byte[] tempBuffer = new byte[(int) tempFile.length()];
 				try {
 					int tempTotalRead = 0;
 					int tempRead = 0;
 					while (tempTotalRead < tempBuffer.length && tempRead >= 0) {
 						tempRead = tempInputStream.read(tempBuffer, tempTotalRead, tempBuffer.length - tempTotalRead);
 						if (tempRead > 0) {
 							tempTotalRead += tempRead;
 						}
 					}
 				} finally {
 					tempInputStream.close();
 				}
 
 				String tempContentType = null;
 				int tempXMLDataStartPos = 0;
 				int tempDoctypeEndPos = 0;
 				if (tempBuffer.length > 10) {
 					if (tempBuffer[0] == '<' && tempBuffer[1] == '?' && tempBuffer[2] == 'x' && tempBuffer[3] == 'm'
 							&& tempBuffer[4] == 'l') {
 						// This seems to be XML data
 						tempContentType = "text/xml;charset=UTF-8";
 					} else {
 						// This seems to be HTML
 						tempContentType = "text/html;charset=UTF-8";
 
 						// Find out where the DOCTYPE declaration ends
 						if ("<!DOCTYPE ".equals(new String(tempBuffer, 0, 10, "US-ASCII"))) {
 							do {
 								tempDoctypeEndPos++;
 							} while (tempDoctypeEndPos < tempBuffer.length && tempBuffer[tempDoctypeEndPos - 1] != '>');
 							tempXMLDataStartPos = tempDoctypeEndPos; // XML cannot start before the DOCTYPE
 						}
 
 						// To increase robustness, we forward the stream to the start of the actual XML data embedded in
 						// the HTML
 						while (tempXMLDataStartPos < tempBuffer.length - 10
 								&& !(tempBuffer[tempXMLDataStartPos] == '<'
 										&& tempBuffer[tempXMLDataStartPos + 1] == 'x'
 										&& tempBuffer[tempXMLDataStartPos + 2] == 'm'
 										&& tempBuffer[tempXMLDataStartPos + 3] == 'l'
 										&& tempBuffer[tempXMLDataStartPos + 4] == 'd'
 										&& tempBuffer[tempXMLDataStartPos + 5] == 'a'
 										&& tempBuffer[tempXMLDataStartPos + 6] == 't'
 										&& tempBuffer[tempXMLDataStartPos + 7] == 'a' && tempBuffer[tempXMLDataStartPos + 8] == ' ')) {
 							tempXMLDataStartPos++;
 						}
 					}
 				}
 
 				final IntegrityContentHandler tempContentHandler = new IntegrityContentHandler();
 
 				SAXParser tempParser;
 				try {
 					tempParser = SAXParserFactory.newInstance().newSAXParser();
 				} catch (ParserConfigurationException exc) {
 					throw new IOException(exc);
 				}
 				XMLReader tempXmlReader = tempParser.getXMLReader();
 				tempXmlReader.setContentHandler(tempContentHandler);
 				tempXmlReader.setFeature("http://xml.org/sax/features/validation", false);
 				tempXmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
 				tempXmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
 
 				InputStream tempFinalInputStream;
 				if (tempDoctypeEndPos > 0 && tempXMLDataStartPos < tempBuffer.length) {
 					// If we have an end position for the DOCTYPE declaration and a valid XML data start, just sequence
 					// the doctype declaration with the XML data, thereby eliminating everything in between that could
 					// cause trouble
 					tempFinalInputStream = new SequenceInputStream(new ByteArrayInputStream(tempBuffer, 0,
 							tempDoctypeEndPos), new ByteArrayInputStream(tempBuffer, tempXMLDataStartPos,
 							tempBuffer.length - tempXMLDataStartPos));
 				} else {
 					// Just start parsing where the XML begins
 					tempFinalInputStream = new ByteArrayInputStream(tempBuffer, tempXMLDataStartPos, tempBuffer.length
 							- tempXMLDataStartPos);
 				}
 
 				InputSource tempInputSource = new InputSource(tempFinalInputStream);
 
 				try {
 					tempXmlReader.parse(tempInputSource);
 				} catch (EndParsingException exc) {
 					// this isn't an error, but expected to abort parsing
 				}
 
 				tempCompoundTestResult.addChild(new IntegrityTestResult(tempCompoundTestResult, tempFile.getName(),
 						tempContentHandler.getTestName(), tempBuffer, tempContentType, tempContentHandler
 								.getSuccessCount(), tempContentHandler.getFailureCount(), tempContentHandler
 								.getTestExceptionCount(), tempContentHandler.getCallExceptionCount()));
 			} catch (SAXException exc) {
 				aListener.getLogger().println("Exception while parsing Integrity result: " + exc.getMessage());
 			} finally {
 				if (tempFileReader != null) {
 					tempFileReader.close();
 				}
 			}
 		}
 
 		return tempCompoundTestResult;
 	}
 
 	private static class EndParsingException extends SAXException {
 
 		/**
 		 * Serial version ID.
 		 */
 		private static final long serialVersionUID = 8064327549601301643L;
 
 		public EndParsingException() {
 			super();
 		}
 	}
 
 	private static class IntegrityContentHandler implements ContentHandler {
 
 		/**
 		 * The number of successful tests.
 		 */
 		private int successCount;
 
 		/**
 		 * The number of failed tests.
 		 */
 		private int failureCount;
 
 		/**
 		 * The number of exceptions in tests.
 		 */
 		private int testExceptionCount;
 
 		/**
 		 * The number of exceptions in calls.
 		 */
 		private int callExceptionCount;
 
 		/**
 		 * The depth of the stack of suites at the moment.
 		 */
 		private int suiteStackDepth;
 
 		/**
 		 * Whether we are currently inside the XSLT script part.
 		 */
 		private boolean insideXslt;
 
 		/**
 		 * The name of the test run.
 		 */
 		private String testName;
 
 		public int getSuccessCount() {
 			return successCount;
 		}
 
 		public int getFailureCount() {
 			return failureCount;
 		}
 
 		public int getTestExceptionCount() {
 			return testExceptionCount;
 		}
 
 		public int getCallExceptionCount() {
 			return callExceptionCount;
 		}
 
 		public String getTestName() {
 			return testName;
 		}
 
 		public void setDocumentLocator(Locator aLocator) {
 			// not used at the moment
 		}
 
 		public void startDocument() throws SAXException {
 			// not used at the moment
 		}
 
 		public void endDocument() throws SAXException {
 			// not used at the moment
 		}
 
 		public void startPrefixMapping(String aPrefix, String anUri) throws SAXException {
 			// not used at the moment
 		}
 
 		public void endPrefixMapping(String aPrefix) throws SAXException {
 			// not used at the moment
 		}
 
 		public void startElement(String anUri, String aLocalName, String aQualifiedName, Attributes someAttributes)
 				throws SAXException {
 			if (!insideXslt) {
 				if ("xsl:stylesheet".equals(aQualifiedName)) {
 					insideXslt = true;
 					return;
 				}
 
 				if ("suite".equals(aQualifiedName)) {
 					suiteStackDepth++;
 				} else if ("integrity".equals(aQualifiedName)) {
 					testName = someAttributes.getValue("name");
 				} else if ("result".equals(aQualifiedName)) {
 					if (suiteStackDepth == 1 && someAttributes.getValue("type") == null) {
 						// This seems to be the outermost suite result element (call results are also <result> elements,
 						// but they contain a result type instead of a summary). We simply fetch the execution totals
 						// from this one and rely on Integrity for summing them up correctly.
 
						String tempSuccessCount = someAttributes.getValue("successCount");
 						if (tempSuccessCount != null) {
 							successCount = Integer.parseInt(tempSuccessCount);
 						}
 
						String tempFailureCount = someAttributes.getValue("failureCount");
 						if (tempFailureCount != null) {
 							failureCount = Integer.parseInt(tempFailureCount);
 						}
 
						String tempTestExceptionCount = someAttributes.getValue("testExceptionCount");
 						if (tempTestExceptionCount != null) {
 							testExceptionCount = Integer.parseInt(tempTestExceptionCount);
 						}
 
						String tempCallExceptionCount = someAttributes.getValue("callExceptionCount");
 						if (tempTestExceptionCount != null) {
 							callExceptionCount = Integer.parseInt(tempCallExceptionCount);
 						}
 
 						// When we've arrived here, we have parsed everything necessary out of the file!
 						throw new EndParsingException();
 					}
 				}
 			}
 		}
 
 		public void endElement(String anUri, String aLocalName, String aQualifiedName) throws SAXException {
 			if (insideXslt) {
 				if ("xsl:stylesheet".equals(aQualifiedName)) {
 					insideXslt = false;
 				}
 			} else {
 				if ("suite".equals(aQualifiedName)) {
 					suiteStackDepth--;
 				}
 			}
 		}
 
 		public void characters(char[] someCharacters, int aStart, int aLength) throws SAXException {
 			// not used at the moment
 		}
 
 		public void ignorableWhitespace(char[] someCharacters, int aStart, int aLength) throws SAXException {
 			// not used at the moment
 		}
 
 		public void processingInstruction(String aTarget, String aData) throws SAXException {
 			// not used at the moment
 		}
 
 		public void skippedEntity(String aName) throws SAXException {
 			// not used at the moment
 		}
 	}
 
 }
