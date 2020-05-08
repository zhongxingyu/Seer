 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.parser.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.jmock.Expectations;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.paxle.core.doc.ICrawlerDocument;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.ParserDocument;
 import org.paxle.core.doc.IParserDocument.Status;
 import org.paxle.core.queue.Command;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.ICommand.Result;
 import org.paxle.parser.ISubParser;
 import org.paxle.parser.ISubParserManager;
 import org.paxle.parser.ParserException;
 
 public class ParserWorkerTest extends MockObjectTestCase {
 	public static final File PLAIN_TEXT_FILE = new File("src/test/resources/test.txt");
 	public static final String PLAIN_TEXT_MIME = "text/plain";
 	
 	private ISubParserManager subParserManager = null;
 	private ParserWorker worker = null;
	private File tempFile = null;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		// creating some mocks
 		this.subParserManager = mock(ISubParserManager.class);
 		
 		// creating worker
 		this.worker = new ParserWorker(this.subParserManager);
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
		
		if (tempFile != null) tempFile.delete();
 	}
 	
 	public void testSkipNotOkCommand() {
 		// creating a test command
 		final ICommand cmd = Command.createCommand(URI.create("http://test.xyz"));
 		cmd.setResult(Result.Failure);
 		
 		// parsing data
 		this.worker.execute(cmd);
 		
 		// check result
 		assertEquals(ICommand.Result.Failure, cmd.getResult());
 	}
 	
 	public void testSkipCommandWithNullCrawlerDoc() {		
 		// creating a test command
 		final ICommand cmd = Command.createCommand(URI.create("http://test.xyz"));
 		
 		// parsing data
 		this.worker.execute(cmd);
 		
 		// check result
 		assertEquals(ICommand.Result.Rejected, cmd.getResult());
 	}
 	
 	public void testSkipCommandWithNotOkCrawlerDoc() {		
 		// creating a test command
 		final ICommand cmd = Command.createCommand(URI.create("http://test.xyz"));
 		final ICrawlerDocument crawlerDocument = mock(ICrawlerDocument.class);
 		cmd.setCrawlerDocument(crawlerDocument);
 		
 		// define expectations
 		checking(new Expectations(){{
 			// crawler-doc status is "Failure"
 			atLeast(1).of(crawlerDocument).getStatus();
 			will(returnValue(ICrawlerDocument.Status.UNKNOWN_FAILURE));
 			atLeast(1).of(crawlerDocument).getStatusText();
 			will(returnValue("testSkipCommandWithNotOkCrawlerDoc"));
 			
 			allowing(crawlerDocument).getMimeType();
 			will(returnValue("unknown"));
 			
 			// the worker must not access the crawler-doc content
 			never(crawlerDocument).getContent();
 		}});
 		
 		// parsing data
 		this.worker.execute(cmd);
 		
 		// check result
 		assertEquals(ICommand.Result.Rejected, cmd.getResult());
 	}
 	
 	public void testSkipCommandWithNoContent() {
 		// creating a test command
 		final ICommand cmd = Command.createCommand(URI.create("http://test.xyz"));
 		final ICrawlerDocument crawlerDocument = mock(ICrawlerDocument.class);
 		cmd.setCrawlerDocument(crawlerDocument);
 		
 		// define expectations
 		checking(new Expectations(){{
 			// crawler-doc status is "Failure"
 			atLeast(1).of(crawlerDocument).getStatus();
 			will(returnValue(ICrawlerDocument.Status.OK));
 			atLeast(1).of(crawlerDocument).getStatusText();
 			will(returnValue(""));
 			
 			// the worker must not access the crawler-doc content
 			atLeast(1).of(crawlerDocument).getContent();
 			will(returnValue(null));			
 			
 			// mime-type is null
 			allowing(crawlerDocument).getMimeType();
 			will(returnValue("unknown"));
 		}});
 		
 		// parsing data
 		this.worker.execute(cmd);
 		
 		// check result
 		assertEquals(ICommand.Result.Rejected, cmd.getResult());
 	}	
 	
 	public void testSkipCommandWithNullMimeType() {
 		// creating a test command
 		final ICommand cmd = Command.createCommand(URI.create("http://test.xyz"));
 		final ICrawlerDocument crawlerDocument = mock(ICrawlerDocument.class);
 		cmd.setCrawlerDocument(crawlerDocument);
 		
 		// define expectations
 		checking(new Expectations(){{
 			// crawler-doc status is "Failure"
 			atLeast(1).of(crawlerDocument).getStatus();
 			will(returnValue(ICrawlerDocument.Status.OK));
 			atLeast(1).of(crawlerDocument).getStatusText();
 			will(returnValue(""));
 			
 			// the crawler-doc content
 			atLeast(1).of(crawlerDocument).getContent();
 			will(returnValue(PLAIN_TEXT_FILE));
 						
 			// mime-type is null
 			allowing(crawlerDocument).getMimeType();
 			will(returnValue(null));
 		}});
 		
 		// parsing data
 		this.worker.execute(cmd);
 		
 		// check result
 		assertEquals(ICommand.Result.Rejected, cmd.getResult());
 	}
 	
 	public void testSkipCommandWithUnsupportedMimeType() {
 		// creating a test command
 		final ICommand cmd = Command.createCommand(URI.create("http://test.xyz"));
 		final ICrawlerDocument crawlerDocument = mock(ICrawlerDocument.class);
 		cmd.setCrawlerDocument(crawlerDocument);
 		
 		// define expectations
 		checking(new Expectations(){{
 			// crawler-doc status is "Failure"
 			atLeast(1).of(crawlerDocument).getStatus();
 			will(returnValue(ICrawlerDocument.Status.OK));
 			atLeast(1).of(crawlerDocument).getStatusText();
 			will(returnValue(""));
 			
 			// mime-type is text/plain
 			atLeast(1).of(crawlerDocument).getMimeType();
 			will(returnValue("text/plain"));
 			
 			// the crawler-doc content
 			atLeast(1).of(crawlerDocument).getContent();
 			will(returnValue(PLAIN_TEXT_FILE));
 			
 			// no parsers for the given mimetype found
 			one(subParserManager).getSubParsers("text/plain");
 			will(returnValue(Collections.EMPTY_LIST));
 		}});
 		
 		// parsing data
 		this.worker.execute(cmd);
 		
 		// check result
 		assertEquals(Result.Rejected, cmd.getResult());
 	}
 	
 	public void testSkipCommandDueToExceptionDuringParsing() throws ParserException, IOException {
 		// creating a test command
 		final URI cmdLocation = URI.create("http://test.xyz");
 		final ICommand cmd = Command.createCommand(cmdLocation);
 		final ICrawlerDocument crawlerDocument = mock(ICrawlerDocument.class);
 		cmd.setCrawlerDocument(crawlerDocument);
 		
 		final ISubParser parser = mock(ISubParser.class);
 		
 		// define expectations
 		checking(new Expectations(){{
 			// crawler-doc status is "Failure"
 			atLeast(1).of(crawlerDocument).getStatus();
 			will(returnValue(ICrawlerDocument.Status.OK));
 			atLeast(1).of(crawlerDocument).getStatusText();
 			will(returnValue(""));
 			
 			// mime-type is text/plain
 			atLeast(1).of(crawlerDocument).getMimeType();
 			will(returnValue("text/plain"));
 			
 			// charset is UTF-8
 			atLeast(1).of(crawlerDocument).getCharset();
 			will(returnValue("UTF-8"));
 			
 			// the worker must not access the crawler-doc content
 			atLeast(1).of(crawlerDocument).getContent();
 			will(returnValue(PLAIN_TEXT_FILE));
 			
 			// no parsers for the given mimetype found
 			one(subParserManager).getSubParsers("text/plain");
 			will(returnValue(Arrays.asList(new ISubParser[]{parser})));
 			
 			 // parsing fails
 			one(parser).parse(cmdLocation, "UTF-8", PLAIN_TEXT_FILE);
 			will(throwException(new RuntimeException("testSkipCommandDueToExceptionDuringParsing")));
 		}});
 		
 		// parsing data
 		this.worker.execute(cmd);
 		
 		// check result
 		assertEquals(ICommand.Result.Failure, cmd.getResult());
 	}
 	
 	public void testSkipCommandDueNotOkParserStatus() throws ParserException, IOException {
 		// creating a test command
 		final URI cmdLocation = URI.create("http://test.xyz");
 		final ICommand cmd = Command.createCommand(cmdLocation);
 		final ICrawlerDocument crawlerDocument = mock(ICrawlerDocument.class);
 		cmd.setCrawlerDocument(crawlerDocument);
 		
 		final ISubParser parser = mock(ISubParser.class);
 		final IParserDocument pDoc = new ParserDocument();
 		pDoc.setStatus(Status.FAILURE,"testSkipCommandDueNotOkParserStatus");
 		
 		// define expectations
 		checking(new Expectations(){{
 			// crawler-doc status is "Failure"
 			atLeast(1).of(crawlerDocument).getStatus();
 			will(returnValue(ICrawlerDocument.Status.OK));
 			atLeast(1).of(crawlerDocument).getStatusText();
 			will(returnValue(""));
 			
 			// mime-type is text/plain
 			atLeast(1).of(crawlerDocument).getMimeType();
 			will(returnValue("text/plain"));
 			
 			// charset is UTF-8
 			atLeast(1).of(crawlerDocument).getCharset();
 			will(returnValue("UTF-8"));
 			
 			// the worker must not access the crawler-doc content
 			atLeast(1).of(crawlerDocument).getContent();
 			will(returnValue(PLAIN_TEXT_FILE));
 			
 			// no parsers for the given mimetype found
 			one(subParserManager).getSubParsers("text/plain");
 			will(returnValue(Arrays.asList(new ISubParser[]{parser})));
 			
 			 // parsing fails
 			one(parser).parse(cmdLocation, "UTF-8", PLAIN_TEXT_FILE);
 			will(returnValue(pDoc));
 		}});
 		
 		// parsing data
 		this.worker.execute(cmd);
 		
 		// check result
 		assertEquals(ICommand.Result.Failure, cmd.getResult());
 	}
 	
 	public void testParseCommand() throws ParserException, IOException {
 		// creating a test command
 		final URI cmdLocation = URI.create("http://test.xyz");
 		final ICommand cmd = Command.createCommand(cmdLocation);
 		final ICrawlerDocument crawlerDocument = mock(ICrawlerDocument.class);
 		cmd.setCrawlerDocument(crawlerDocument);
 		
 		final ISubParser parser = mock(ISubParser.class);
 		final IParserDocument pDoc = new ParserDocument();
 		pDoc.setStatus(Status.OK);
 		pDoc.setTextFile(PLAIN_TEXT_FILE);
 		
 		// define expectations
 		checking(new Expectations(){{
 			// crawler-doc status is "Failure"
 			atLeast(1).of(crawlerDocument).getStatus();
 			will(returnValue(ICrawlerDocument.Status.OK));
 			atLeast(1).of(crawlerDocument).getStatusText();
 			will(returnValue(""));
 			
 			// mime-type is text/plain
 			atLeast(1).of(crawlerDocument).getMimeType();
 			will(returnValue("text/plain"));
 			
 			// charset is UTF-8
 			atLeast(1).of(crawlerDocument).getCharset();
 			will(returnValue("UTF-8"));
 			
 			// the worker must not access the crawler-doc content
 			atLeast(1).of(crawlerDocument).getContent();
 			will(returnValue(PLAIN_TEXT_FILE));
 			
 			// no parsers for the given mimetype found
 			one(subParserManager).getSubParsers("text/plain");
 			will(returnValue(Arrays.asList(new ISubParser[]{parser})));
 			
 			 // parsing fails
 			one(parser).parse(cmdLocation, "UTF-8", PLAIN_TEXT_FILE);
 			will(returnValue(pDoc));
 		}});
 		
 		// parsing data
 		this.worker.execute(cmd);
 		
 		// check result
 		assertEquals(ICommand.Result.Passed, cmd.getResult());
 		assertSame(pDoc, cmd.getParserDocument());
 		assertSame(PLAIN_TEXT_FILE, cmd.getParserDocument().getTextFile());
 	}
 }
