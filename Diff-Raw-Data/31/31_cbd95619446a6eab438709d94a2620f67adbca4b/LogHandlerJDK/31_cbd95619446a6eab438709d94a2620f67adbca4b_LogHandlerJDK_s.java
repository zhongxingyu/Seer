 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.applications.rainbow.logger;
 
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import net.sf.okapi.applications.rainbow.lib.ILog;
 
 class LogHandlerJDK extends Handler implements ILogHandler {
 	private ILog       log;
 
 	protected LogHandlerJDK(){}
 
 	public void initialize(ILog log) {
 		if( log == null ) return;
 
 		this.log = log;
 		this.setLevel(Level.INFO);
 		Logger.getLogger("").addHandler(this); //$NON-NLS-1$
 	}
 
 	public void setLogLevel(int level) {
 		switch ( level ) {
 			case LogLevel.DEBUG:
 				this.setLevel(Level.FINE);
 				break;
 			case LogLevel.TRACE:
 				this.setLevel(Level.FINEST);
 				break;
 			default:
 				this.setLevel(Level.INFO);
 				break;
 		}
 	}
 
 	@Override
 	public void close() {
 		// Do nothing
 	}
 
 	@Override
 	public void flush() {
 		// Do nothing
 	}
 
 	@Override
 	public void publish(LogRecord record) {
 		if( log == null ) return;
 
 		/*
 		 * JDK native levels:
 		 *    Level.OFF     = Integer.MAX_VALUE;
 		 *    Level.SEVERE  = 1000;
 		 *    Level.WARNING =  900;
 		 *    Level.INFO    =  800;
 		 *    Level.CONFIG  =  700;
 		 *    Level.FINE    =  500;
 		 *    Level.FINER   =  400;
 		 *    Level.FINEST  =  300;
 		 *    Level.ALL     = Integer.MIN_VALUE;
 		 */
 
		if ( record.getLevel() == Level.SEVERE ) {
 			log.error(record.getMessage());
 			Throwable e = record.getThrown();
 			if ( e != null ) {
				log.message(e.getMessage());
 				log.message(" @ "+e.toString()); //$NON-NLS-1$
 			}
 		}
		else if ( record.getLevel() == Level.WARNING ) {
 			// Filter out Axis warnings
 			if ( "org.apache.axis.utils.JavaUtils".equals(record.getLoggerName()) ) return;
 			// Otherwise print
 			log.warning(record.getMessage());
 		}
 		else // INFO and below
 			log.message(record.getMessage());
 	}
 }
