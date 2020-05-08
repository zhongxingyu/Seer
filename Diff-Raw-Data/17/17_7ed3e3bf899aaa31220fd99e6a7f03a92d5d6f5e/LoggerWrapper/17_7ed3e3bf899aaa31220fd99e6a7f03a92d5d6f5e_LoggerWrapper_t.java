 /**
  * Copyright (C) 2009 Mads Mohr Christensen, <hr.mohr@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package dk.cubing.liveresults.uploader;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Marker;
 import org.slf4j.helpers.MessageFormatter;
 
 import dk.cubing.liveresults.uploader.engine.ResultsEngine;
 
 public class LoggerWrapper implements Logger {
 
 	private final LoggerWrapperFactory factory;
 	private final Logger log;
 	
 	public LoggerWrapper(LoggerWrapperFactory factory, Class<?> clazz) {
 		this.factory = factory;
 		this.log = LoggerFactory.getLogger(clazz);
 	}
 
 	@Override
 	public void debug(String arg0) {
 		log.debug(arg0);
 	}
 
 	@Override
 	public void debug(String arg0, Object arg1) {
 		log.debug(arg0, arg1);
 	}
 
 	@Override
 	public void debug(String arg0, Object[] arg1) {
 		log.debug(arg0, arg1);
 	}
 
 	@Override
 	public void debug(String arg0, Throwable arg1) {
 		log.debug(arg0, arg1);
 	}
 
 	@Override
 	public void debug(Marker arg0, String arg1) {
 		log.debug(arg0, arg1);
 	}
 
 	@Override
 	public void debug(String arg0, Object arg1, Object arg2) {
 		log.debug(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void debug(Marker arg0, String arg1, Object arg2) {
 		log.debug(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void debug(Marker arg0, String arg1, Object[] arg2) {
 		log.debug(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void debug(Marker arg0, String arg1, Throwable arg2) {
 		log.debug(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void debug(Marker arg0, String arg1, Object arg2, Object arg3) {
 		log.debug(arg0, arg1, arg2, arg3);
 	}
 
 	@Override
 	public void error(String arg0) {
 		log.error(arg0);
 		if (log.isErrorEnabled()) {
 			ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.showGuiAlert("error", arg0);
 		}
 	}
 
 	@Override
 	public void error(String arg0, Object arg1) {
 		log.error(arg0, arg1);
 		if (log.isErrorEnabled()) {
 		    String msgStr = MessageFormatter.format(arg0, arg1);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.showGuiAlert("error", msgStr);
 		}
 	}
 
 	@Override
 	public void error(String arg0, Object[] arg1) {
 		log.error(arg0, arg1);
 		if (log.isErrorEnabled()) {
		    String msgStr = MessageFormatter.arrayFormat(arg0, arg1);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.showGuiAlert("error", msgStr);
 		}		
 	}
 
 	@Override
 	public void error(String arg0, Throwable arg1) {
 		log.error(arg0, arg1);
 		if (log.isErrorEnabled()) {
 		    String msgStr = MessageFormatter.format(arg0, arg1);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.showGuiAlert("error", msgStr, arg1);
 		}		
 	}
 
 	@Override
 	public void error(Marker arg0, String arg1) {
 		log.error(arg0, arg1);
 		if (log.isErrorEnabled()) {
 			ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.showGuiAlert("error", arg1);
 		}		
 	}
 
 	@Override
 	public void error(String arg0, Object arg1, Object arg2) {
 		log.error(arg0, arg1, arg2);
 		if (log.isErrorEnabled()) {
 		    String msgStr = MessageFormatter.format(arg0, arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.showGuiAlert("error", msgStr);
 		}
 	}
 
 	@Override
 	public void error(Marker arg0, String arg1, Object arg2) {
 		log.error(arg0, arg1, arg2);
 		if (log.isErrorEnabled()) {
 		    String msgStr = MessageFormatter.format(arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.showGuiAlert("error", msgStr);
 		}		
 	}
 
 	@Override
 	public void error(Marker arg0, String arg1, Object[] arg2) {
 		log.error(arg0, arg1, arg2);
 		if (log.isErrorEnabled()) {
		    String msgStr = MessageFormatter.arrayFormat(arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.showGuiAlert("error", msgStr);
 		}		
 	}
 
 	@Override
 	public void error(Marker arg0, String arg1, Throwable arg2) {
 		log.error(arg0, arg1, arg2);
 		if (log.isErrorEnabled()) {
 		    String msgStr = MessageFormatter.format(arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.showGuiAlert("error", msgStr, arg2);
 		}		
 	}
 
 	@Override
 	public void error(Marker arg0, String arg1, Object arg2, Object arg3) {
 		log.error(arg0, arg1, arg2, arg3);
 		if (log.isErrorEnabled()) {
 		    String msgStr = MessageFormatter.format(arg1, arg2, arg3);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.showGuiAlert("error", msgStr);
 		}		
 	}
 
 	@Override
 	public String getName() {
 		return log.getName();
 	}
 
 	@Override
 	public void info(String arg0) {
 		log.info(arg0);
 		if (log.isInfoEnabled()) {
 			ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("info", arg0);
 		}
 	}
 
 	@Override
 	public void info(String arg0, Object arg1) {
 		log.info(arg0, arg1);
 		if (log.isInfoEnabled()) {
 		    String msgStr = MessageFormatter.format(arg0, arg1);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("info", msgStr);
 		}
 	}
 
 	@Override
 	public void info(String arg0, Object[] arg1) {
 		log.info(arg0, arg1);
 		if (log.isInfoEnabled()) {
		    String msgStr = MessageFormatter.arrayFormat(arg0, arg1);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("info", msgStr);
 		}		
 	}
 
 	@Override
 	public void info(String arg0, Throwable arg1) {
 		log.info(arg0, arg1);
 		if (log.isInfoEnabled()) {
 		    String msgStr = MessageFormatter.format(arg0, arg1);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("info", msgStr, arg1);
 		}		
 	}
 
 	@Override
 	public void info(Marker arg0, String arg1) {
 		log.info(arg0, arg1);
 		if (log.isInfoEnabled()) {
 			ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("info", arg1);
 		}		
 	}
 
 	@Override
 	public void info(String arg0, Object arg1, Object arg2) {
 		log.info(arg0, arg1, arg2);
 		if (log.isInfoEnabled()) {
 		    String msgStr = MessageFormatter.format(arg0, arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("info", msgStr);
 		}		
 	}
 
 	@Override
 	public void info(Marker arg0, String arg1, Object arg2) {
 		log.info(arg0, arg1, arg2);
 		if (log.isInfoEnabled()) {
 		    String msgStr = MessageFormatter.format(arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("info", msgStr);
 		}		
 	}
 
 	@Override
 	public void info(Marker arg0, String arg1, Object[] arg2) {
 		log.info(arg0, arg1, arg2);
 		if (log.isInfoEnabled()) {
		    String msgStr = MessageFormatter.arrayFormat(arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("info", msgStr);
 		}
 	}
 
 	@Override
 	public void info(Marker arg0, String arg1, Throwable arg2) {
 		log.info(arg0, arg1, arg2);
 		if (log.isInfoEnabled()) {
 		    String msgStr = MessageFormatter.format(arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("info", msgStr, arg2);
 		}		
 	}
 
 	@Override
 	public void info(Marker arg0, String arg1, Object arg2, Object arg3) {
 		log.info(arg0, arg1, arg2, arg3);
 		if (log.isInfoEnabled()) {
 		    String msgStr = MessageFormatter.format(arg1, arg2, arg3);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("info", msgStr);
 		}		
 	}
 
 	@Override
 	public boolean isDebugEnabled() {
 		return log.isDebugEnabled();
 	}
 
 	@Override
 	public boolean isDebugEnabled(Marker arg0) {
 		return log.isDebugEnabled(arg0);
 	}
 
 	@Override
 	public boolean isErrorEnabled() {
 		return log.isErrorEnabled();
 	}
 
 	@Override
 	public boolean isErrorEnabled(Marker arg0) {
 		return log.isErrorEnabled(arg0);
 	}
 
 	@Override
 	public boolean isInfoEnabled() {
 		return log.isInfoEnabled();
 	}
 
 	@Override
 	public boolean isInfoEnabled(Marker arg0) {
 		return log.isInfoEnabled(arg0);
 	}
 
 	@Override
 	public boolean isTraceEnabled() {
 		return log.isTraceEnabled();
 	}
 
 	@Override
 	public boolean isTraceEnabled(Marker arg0) {
 		return log.isTraceEnabled(arg0);
 	}
 
 	@Override
 	public boolean isWarnEnabled() {
 		return log.isWarnEnabled();
 	}
 
 	@Override
 	public boolean isWarnEnabled(Marker arg0) {
 		return log.isWarnEnabled(arg0);
 	}
 
 	@Override
 	public void trace(String arg0) {
 		log.trace(arg0);
 	}
 
 	@Override
 	public void trace(String arg0, Object arg1) {
 		log.trace(arg0, arg1);
 	}
 
 	@Override
 	public void trace(String arg0, Object[] arg1) {
 		log.trace(arg0, arg1);
 	}
 
 	@Override
 	public void trace(String arg0, Throwable arg1) {
 		log.trace(arg0, arg1);
 	}
 
 	@Override
 	public void trace(Marker arg0, String arg1) {
 		log.trace(arg0, arg1);
 	}
 
 	@Override
 	public void trace(String arg0, Object arg1, Object arg2) {
 		log.trace(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void trace(Marker arg0, String arg1, Object arg2) {
 		log.trace(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void trace(Marker arg0, String arg1, Object[] arg2) {
 		log.trace(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void trace(Marker arg0, String arg1, Throwable arg2) {
 		log.trace(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void trace(Marker arg0, String arg1, Object arg2, Object arg3) {
 		log.trace(arg0, arg1, arg2, arg3);
 	}
 
 	@Override
 	public void warn(String arg0) {
 		log.warn(arg0);
 		if (log.isWarnEnabled()) {
 			ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("warn", arg0);
 		}
 	}
 
 	@Override
 	public void warn(String arg0, Object arg1) {
 		log.warn(arg0, arg1);
 		if (log.isWarnEnabled()) {
 		    String msgStr = MessageFormatter.format(arg0, arg1);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("warn", msgStr);
 		}	
 	}
 
 	@Override
 	public void warn(String arg0, Object[] arg1) {
 		log.warn(arg0, arg1);
 		if (log.isWarnEnabled()) {
		    String msgStr = MessageFormatter.arrayFormat(arg0, arg1);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("warn", msgStr);
 		}	
 	}
 
 	@Override
 	public void warn(String arg0, Throwable arg1) {
 		log.warn(arg0, arg1);
 		if (log.isWarnEnabled()) {
 		    String msgStr = MessageFormatter.format(arg0, arg1);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("warn", msgStr, arg1);
 		}	
 	}
 
 	@Override
 	public void warn(Marker arg0, String arg1) {
 		log.warn(arg0, arg1);
 		if (log.isWarnEnabled()) {
 			ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("warn", arg1);
 		}	
 	}
 
 	@Override
 	public void warn(String arg0, Object arg1, Object arg2) {
 		log.warn(arg0, arg1, arg2);
 		if (log.isWarnEnabled()) {
 		    String msgStr = MessageFormatter.format(arg0, arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("warn", msgStr);
 		}	
 	}
 
 	@Override
 	public void warn(Marker arg0, String arg1, Object arg2) {
 		log.warn(arg0, arg1, arg2);
 		if (log.isWarnEnabled()) {
 		    String msgStr = MessageFormatter.format(arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("warn", msgStr);
 		}	
 	}
 
 	@Override
 	public void warn(Marker arg0, String arg1, Object[] arg2) {
 		log.warn(arg0, arg1, arg2);
 		if (log.isWarnEnabled()) {
		    String msgStr = MessageFormatter.arrayFormat(arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("warn", msgStr);
 		}	
 	}
 
 	@Override
 	public void warn(Marker arg0, String arg1, Throwable arg2) {
 		log.warn(arg0, arg1, arg2);
 		if (log.isWarnEnabled()) {
 		    String msgStr = MessageFormatter.format(arg1, arg2);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("warn", msgStr, arg2);
 		}	
 	}
 
 	@Override
 	public void warn(Marker arg0, String arg1, Object arg2, Object arg3) {
 		log.warn(arg0, arg1, arg2, arg3);
 		if (log.isWarnEnabled()) {
 		    String msgStr = MessageFormatter.format(arg1, arg2, arg3);
 		    ResultsEngine engine = factory.getEngine();
 			if (engine != null) engine.appendGuiMessage("warn", msgStr);
 		}	
 	}
 
 }
