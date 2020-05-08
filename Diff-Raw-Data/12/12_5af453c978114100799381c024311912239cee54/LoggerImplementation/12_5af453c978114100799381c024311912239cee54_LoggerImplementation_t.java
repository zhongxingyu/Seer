 // Copyright (C) 2001, 2002 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.engine.process;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.util.Date;
 
 import net.grinder.common.Logger;
 import net.grinder.engine.EngineException;
 import net.grinder.util.DelayedCreationFileWriter;
 
 
 /**
  * Logger implementation.
  *
  * <p>Each thread should call {@link #createProcessLogger} or {@link
  * #createThreadLogger} to get a {@link net.grinder.common.Logger}.
  * </p>
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class LoggerImplementation
 {
     private static final PrintWriter s_stdoutWriter;
     private static final PrintWriter s_stderrWriter;
     private static final String s_lineSeparator =
 	System.getProperty("line.separator");
     private static final int s_lineSeparatorLength = s_lineSeparator.length();
     private static final DateFormat s_dateFormat =
 	DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
     private static long s_nextTime = System.currentTimeMillis();
     private static String s_dateString;
 
     static
     {
 	s_stdoutWriter = new PrintWriter(System.out);
 	s_stderrWriter = new PrintWriter(System.err);
     }
 
     /** Use our DateFormat at most once a second. **/
     private final synchronized static String getDateString()
     {
 	final long now = System.currentTimeMillis();
 	
 	if (now > s_nextTime) {
 	    s_nextTime = now + 1000;
 	    s_dateString = s_dateFormat.format(new Date());
 	}
 
 	return s_dateString;
     }
 
     private final String m_grinderID;
     private final boolean m_logProcessStreams;
     private final FilenameFactoryImplementation m_filenameFactory;
     private final PrintWriter m_outputWriter;
     private final PrintWriter m_errorWriter;
     private final PrintWriter m_dataWriter;
    private boolean m_errorOccurred = false;
 
     LoggerImplementation(String grinderID, String logDirectoryString, 
 			 boolean logProcessStreams, boolean appendLog)
 	throws EngineException
     {
 	m_grinderID = grinderID;
 	m_logProcessStreams = logProcessStreams;
 
 	final File logDirectory = new File(logDirectoryString, "");
 
 	try {
 	    logDirectory.mkdirs();
 	}
 	catch (Exception e) {
 	    throw new EngineException(e.getMessage(), e);
 	}
 
 	if (!logDirectory.canWrite()) {
 	    throw new EngineException("Cannot write to log directory '" +
 				      logDirectory.getPath() + "'");
 	}
 
 	m_filenameFactory =
 	    new FilenameFactoryImplementation(logDirectory, grinderID);
 
 	// Although we manage the flushing ourselves and don't call
 	// println, we set auto flush on these PrintWriters because
 	// clients can get direct access to them.
 	m_outputWriter = new PrintWriter(createWriter("out", appendLog), true);
 	m_errorWriter = 
 	    new PrintWriter(createWriter("error", appendLog), true);
 
 	// Don't autoflush, we explictly control flushing of this writer.
 	m_dataWriter = new PrintWriter(createWriter("data", appendLog), false);
     }
 
     private Writer createWriter(String prefix, boolean appendLog)
 	throws EngineException
     {
 	final File file = new File(m_filenameFactory.createFilename(prefix));
 
 	// Check we can write to the file and moan now. We won't see
 	// the problem later because PrintWriters eat exceptions. If
 	// the file doesn't exist, we're pretty sure we can create it
 	// because we checked we can write to the log directory.
 	if (file.exists() && !file.canWrite()) {
 	    throw new EngineException("Cannot write to '" + file.getPath() +
 				      "'");
 	}
 
 	return new BufferedWriter(
 	    new DelayedCreationFileWriter(file, appendLog));
     }
 
     final Logger createProcessLogger() throws EngineException
     {
 	return createThreadLogger(-1);
     }
 
     final ThreadLogger createThreadLogger(int threadID) throws EngineException
     {
 	return new ThreadState(threadID);
     }
 
     final FilenameFactoryImplementation getFilenameFactory()
     {
 	return m_filenameFactory;
     }
 
     final PrintWriter getDataWriter()
     {
 	return m_dataWriter;
     }
 
     private final void outputInternal(ThreadState state, String message,
 				      int where)
     {
 	if (!m_logProcessStreams) {
 	    where &= ~Logger.LOG;
 	}
 
 	if (where != 0) {
 	    final int lineLength = formatMessage(state, message);
 
 	    if ((where & Logger.LOG) != 0) {
 		m_outputWriter.write(state.m_outputLine, 0, lineLength);
 		m_outputWriter.flush();
 	    }
 
 	    if ((where & Logger.TERMINAL) != 0) {
 		s_stdoutWriter.write(state.m_outputLine, 0, lineLength);
 		s_stdoutWriter.flush();
 	    }
 	}
     }
     
     private final void errorInternal(ThreadState state, String message,
 				     int where) 
     {
 	if (!m_logProcessStreams) {
 	    where &= ~Logger.LOG;
 	}
 
 	if (where != 0) {
 	    final int lineLength = formatMessage(state, message);
 
 	    if ((where & Logger.LOG) != 0) {
 		m_errorWriter.write(state.m_outputLine, 0, lineLength);
 		m_errorWriter.flush();
 	    }
 
 	    if ((where & Logger.TERMINAL) != 0) {
 		s_stderrWriter.write(state.m_outputLine, 0, lineLength);
 		s_stderrWriter.flush();
 	    }
 
 	    final int summaryLength = 20;
 
 	    final String summary = 
 		message.length() > summaryLength ?
 		message.substring(0, summaryLength) + "..." : message;
 
 	    outputInternal(state,
 			   "ERROR (\"" + summary +
 			   "\"), see error log for details",
 			   Logger.LOG);

	    if (m_errorOccurred == false) {
		outputInternal(state,
			       "There were errors, see error log for details",
			       Logger.TERMINAL);

		m_errorOccurred = true;
	    }
 	}
     }
 
     private final int formatMessage(ThreadState state, String message)
     {
 	final StringBuffer buffer = state.m_buffer;
 	final char[] outputLine = state.m_outputLine;
 
 	buffer.setLength(0);
 
 	buffer.append(getDateString());
 	buffer.append(": ");
 
 	if (state.m_threadID == -1) {
 	    buffer.append("Grinder Process (");
 	    buffer.append(m_grinderID);
 	}
 	else {
 	    buffer.append("(thread ");
 	    buffer.append(state.m_threadID);
 
 	    if (state.m_currentRunNumber >= 0) {
 		buffer.append(" run " + state.m_currentRunNumber);
 	    }
 	
 	    if (state.m_currentTestNumber >= 0) {
 		buffer.append(" test " + state.m_currentTestNumber);
 	    }
 	}
 	
 
 	buffer.append(") ");
 	buffer.append(message);
 
 	// Sadly this is the most efficient way to get something we
 	// can println from the StringBuffer. getString() creates an
 	// extra string, getValue() is package scope.
 	final int bufferLength = buffer.length();
 	final int outputLineSpace = outputLine.length - s_lineSeparatorLength;
 	
 	final int lineLength =
 	    bufferLength > outputLineSpace ? outputLineSpace : bufferLength;
 
 	buffer.getChars(0, lineLength, outputLine, 0);
 	s_lineSeparator.getChars(0, s_lineSeparatorLength, outputLine,
 				 lineLength);
 
 	return lineLength + s_lineSeparatorLength;
     }
 
     /**
      * Thread specific state.
      **/
     private final class ThreadState implements ThreadLogger
     {
 	private final int m_threadID;
 	private int m_currentRunNumber = -1;
 	private int m_currentTestNumber = -1;
 
 	// Scratch space.
 	private final StringBuffer m_buffer = new StringBuffer();
 	private final char[] m_outputLine = new char[512];
 
 	public ThreadState(int threadID)
 	{
 	    m_threadID = threadID;
 	}
 
 	public final int getThreadID()
 	{
 	    return m_threadID;
 	}
 
 	public final int getCurrentRunNumber()
 	{
 	    return m_currentRunNumber;
 	}
 
 	public final void setCurrentRunNumber(int runNumber)
 	{
 	    m_currentRunNumber = runNumber;
 	}
 
 	public final int getCurrentTestNumber()
 	{
 	    return m_currentTestNumber;
 	}
 	
 	public final void setCurrentTestNumber(int testNumber)
 	{
 	    m_currentTestNumber = testNumber;
 	}
 
 	public final void output(String message)
 	{
 	    outputInternal(this, message, Logger.LOG);
 	}
 
 	public final void output(String message, int where)
 	{
 	    outputInternal(this, message, where);
 	}
 
 	public final void error(String message)
 	{
 	    errorInternal(this, message, Logger.LOG);
 	}
     
 	public final void error(String message, int where)
 	{
 	    errorInternal(this, message, where);
 	}
 
 	public final PrintWriter getOutputLogWriter()
 	{
 	    return m_outputWriter;
 	}
 
 	public final PrintWriter getErrorLogWriter()
 	{
 	    return m_errorWriter;
 	}
     }
 }
