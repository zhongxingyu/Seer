 /*******************************************************************************
  * The MIT License
  * 
  * Copyright (c) 2011 Florian Sellmayr
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  ******************************************************************************/
 
 package org.GAEChannel4j;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 
import org.GAEChannel4j.impl.BrowserThread;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.BrowserFunction;
 import org.eclipse.swt.browser.ProgressEvent;
 import org.eclipse.swt.browser.ProgressListener;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.jandroid2cloud.ui.GenericEventLoopThread;
 import org.jandroid2cloud.ui.notifications.NotificationAppender;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Connection {
     private static final Logger logger = LoggerFactory.getLogger(Connection.class);
     private IChannelHandler handler = new ChannelHandlerAdapter();
     private String token;
     private Display display;
     private boolean executed=false;
     private Shell shell;
 
     /**
      * Creates a new Connection with the given token. This is just
      * initialization. Communication will happen only after the connection is
      * opened() // TODO: link
      * 
      * @param token
      *            the channels token
      */
     public Connection(String token) {
 	this.token = token;
     }
 
     public Connection(String token, Display display) {
 	this(token);
 	this.display = display;
     }
 
     /**
      * Opens a new channel. This method will spawn a new background-thread that
      * handles all communication and returns immediately. The handler must be
      * set before a channel is opened. If you want to know if the connection was
      * successful, use respective handler.
      */
     public void open() {
 	if (display == null) {
 	    // there exists no external event loop. we create out own:
 	    GenericEventLoopThread eventLoop = new GenericEventLoopThread();
 	    eventLoop.start();
 	}
 	openConnectionInternal();
 	logger.debug("Started background handler to handle channelevents");
     }
 
     /**
      * Stops the background thread. After that, no more events will happen.
      */
     public void close() {
 	shell.dispose();
 	logger.debug("Disposed browser shell");
 	executed=false;
     }
 
     /**
      * Sets the handler which is responsible for handling all channel events.
      * @param handler the handler. cannot be null. If you need a quiet handler, use {@link ChannelHandlerAdapter}
      * @throws IllegalArgumentException if the handler is null. 
      */
     public void setHandler(IChannelHandler handler) {
 	if (handler == null) {
 	    throw new IllegalArgumentException("handler cannot be null");
 	}
 	this.handler = handler;
     }
     
     /**
      * This method really opens the connection to the server.
      */
     private void openConnectionInternal() {
 	if (display == null) {
 	    display = Display.getDefault();
 	}
 	display.syncExec(new Runnable() {
 	    @Override
 	    public void run() {
 		shell = new Shell(display);
 		final Browser browser = new Browser(shell, SWT.NONE);
 		String gaeChannelScript = copyChannelScriptToTmp();
 		logger.debug("main file at " + gaeChannelScript);
 		browser.setUrl(gaeChannelScript);
 		browser.addProgressListener(new ProgressListener() {
 
 		    public void completed(ProgressEvent arg0) {
 			if (!executed) {
 			    executed = browser.execute(getScript(token));
 			    logger.debug("executed channel-api calls to set up functionality. success?"
 				    + executed);
 			}
 		    }
 
 		    public void changed(ProgressEvent arg0) {
 			// TODO Auto-generated method stub
 
 		    }
 		});
 
 		new BrowserFunction(browser, "open") {
 		    public Object function(Object[] arg0) {
 			handler.open();
 			return super.function(arg0);
 		    }
 		};
 
 		new BrowserFunction(browser, "message") {
 		    public Object function(Object[] arguments) {
 			handler.message((String) arguments[0]);
 			return super.function(arguments);
 		    }
 		};
 
 		new BrowserFunction(browser, "error") {
 
 		    public Object function(Object[] arguments) {
 			logger.error("error msg from server. arguments: " + arguments);
 			String desc = "";
 			if (arguments.length > 0) {
 			    desc = (String) arguments[0];
 			}
 			logger.debug("error: got desc:" + desc);
 			double code = -1;
 			if (arguments.length > 1) {
 			    try {
 				code =(Double) arguments[1];
 			    } catch (Exception e) {
 				logger.error("somethings wrong with getting the error code.", e);
 			    }
 			}
 			logger.debug("got code:" + code);
 			handler.error(desc, code);
 			logger.debug("handeled error");
 			return super.function(arguments);
 		    }
 		};
 
 		new BrowserFunction(browser, "close") {
 		    public Object function(Object[] arguments) {
 			handler.close();
 			return super.function(arguments);
 		    }
 		};
 	    }
 	});
     }
     
     /**
      * Copys the channel script to a temproary file
      * 
      * @return the file name of the temporary file.
      */
     private String copyChannelScriptToTmp() {
 	InputStream is = Connection.class.getResourceAsStream("/mainfile.html");
 	BufferedReader bReader = new BufferedReader(new InputStreamReader(is));
 	try {
 	    File tmpFile = File.createTempFile("gaeChannelScript", ".html");
 	    tmpFile.deleteOnExit();
 	    PrintStream out = new PrintStream(tmpFile);
 	    while (bReader.ready()) {
 		out.println(bReader.readLine());
 	    }
 	    return tmpFile.getAbsolutePath();
 	} catch (IOException e) {
 	    logger.error(
 		    NotificationAppender.MARKER,
 		    "Could not read Channels Handler file.\nConnection will not work. See logs for details",
 		    e);
 	}
 	return null;
     }
 
     private String getScript(String token) {
 	return "channel = new goog.appengine.Channel(\"" + token + "\");\n"
 		+ "socket = channel.open();\n" + "socket.onopen = function() { open(); };\n"
 		+ "socket.onmessage = function(m) { message(m.data); };\n"
 		+ "socket.onerror = function(e) { error(e.description,e.code); };\n"
 		+ "socket.onclose = function() { close(); };";
     }
 
 }
