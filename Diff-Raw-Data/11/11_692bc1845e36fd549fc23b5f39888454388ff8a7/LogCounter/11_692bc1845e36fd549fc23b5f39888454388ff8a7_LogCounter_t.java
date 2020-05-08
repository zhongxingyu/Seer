 // Copyright (C) 2000 Paco Gomez
 // Copyright (C) 2000, 2001, 2002 Philip Aston
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
 
 package net.grinder.common;
 
 import java.io.PrintWriter;
 import java.io.Writer;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 public class LogCounter implements Logger
 {
     private int m_numberOfErrors = 0;
     private int m_numberOfMessages = 0;
 
     private CountingPrintWriter m_errorLineCounter =
 	new CountingPrintWriter(new NullWriter());
 
     private CountingPrintWriter m_outputLineCounter =
 	new CountingPrintWriter(new NullWriter());
 
    public void error(String message)
     {
 	++m_numberOfErrors;
     }
 
    public void error(String message, int where)
     {
 	++m_numberOfErrors;
     }
 
    public void output(String message)
     {
 	++m_numberOfMessages;
     }
 
    public void output(String message, int where)
     {
 	++m_numberOfMessages;
     }
 
     public int getNumberOfErrors() 
     {
 	return m_numberOfErrors;
     }
 
     public int getNumberOfMessages()
     {
 	return m_numberOfMessages;
     }
 
     public PrintWriter getErrorLogWriter()
     {
 	return m_errorLineCounter;
     }
 
     public int getNumberOfErrorLines()
     {
 	return m_errorLineCounter.getCount();
     }
 
     public PrintWriter getOutputLogWriter()
     {
 	return m_outputLineCounter;
     }
 
     public int getNumberOfOutputLines()
     {
 	return m_outputLineCounter.getCount();
     }
 
     private static class CountingPrintWriter extends PrintWriter
     {
 	private int m_count = 0;
 
 	public CountingPrintWriter(Writer delegate)
 	{
 	    super(delegate, true);
 	}
 
 	public int getCount()
 	{
 	    return m_count;
 	}
 
 	public void println()
 	{
 	    ++m_count;
 	}
     }
 
     private static class NullWriter extends Writer
     {
 	public void close() {}
 	public void flush() {}
 	public void write(char[] buffer, int offset, int length) {}
     }
 }
 
