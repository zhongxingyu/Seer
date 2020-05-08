 // The Grinder
 // Copyright (C) 2000, 2001  Paco Gomez
 // Copyright (C) 2000, 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.plugininterface;
 
import java.io.PrintWriter;


 /**
  * @author Philip Aston
  * @version $Revision$
  */
 public class LogCounter implements Logger
 {
     private int m_numberOfErrors = 0;
     private int m_numberOfMessages = 0;
 
     public void logError(String message)
     {
 	++m_numberOfErrors;
     }
 
    public void logError(String message, int where)
    {
	++m_numberOfErrors;
    }

     public void logMessage(String message)
     {
 	++m_numberOfMessages;
     }
 
    public void logMessage(String message, int where)
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

    public PrintWriter getOutputLogWriter()
    {
	return new PrintWriter(System.out);
    }

    public PrintWriter getErrorLogWriter()
    {
	return new PrintWriter(System.err);
    }
 }
 
