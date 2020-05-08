 // Fraunhofer Institute for Computer Graphics Research (IGD)
 // Department Graphical Information Systems (GIS)
 //
 // Copyright (c) 2010 Fraunhofer IGD
 //
 // This file is part of slf4j-plus.
 //
 // slf4j-plus is free software: you can redistribute
 // it and/or modify it under the terms of the GNU Lesser General Public License
 // as published by the Free Software Foundation, either version 3 of the
 // License, or (at your option) any later version.
 //
 // slf4j-plus is distributed in the hope that it will
 // be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public License
 // along with slf4j-plus.
 // If not, see <http://www.gnu.org/licenses/>.
 
 package de.fhg.igd.slf4jplus.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.MDC;
 import org.slf4j.Marker;
 import org.slf4j.MarkerFactory;
 import org.slf4j.ext.LoggerWrapper;
 import org.slf4j.helpers.MessageFormatter;
 import org.slf4j.spi.LocationAwareLogger;
 
 import de.fhg.igd.slf4jplus.AGroup;
 import de.fhg.igd.slf4jplus.ALogger;
 import de.fhg.igd.slf4jplus.ATransaction;
 
 
 /**
  * Enhanced logger implementation wrapping a {@link Logger} instance
  * 
  * @author Simon Templer
  */
 public class ALoggerImpl extends LoggerWrapper implements ALogger, LocationAwareLogger {
 	
 	private final String fqcn;
 	
 	private final boolean locationAware;
 	
 	private final Logger logger;
 	
 	private final Marker bundleMarker;
 	
 	/**
 	 * Create an enhanced logger
 	 * 
 	 * @param logger the internal logger
 	 * @param bundleName the symbolic name of the OSGi bundle originating the
 	 *   messages, may be <code>null</code> if unknown
 	 */
 	public ALoggerImpl(Logger logger, String bundleName) {
 		this(logger, bundleName, ALoggerImpl.class.getName());
 	}
 	
 	/**
 	 * Create an enhanced logger
 	 * 
 	 * @param logger the internal logger
 	 * @param bundleName the symbolic name of the OSGi bundle originating the
 	 *   messages, may be <code>null</code> if unknown
 	 * @param fqcn the fully qualified class name of the masking logger
 	 */
 	public ALoggerImpl(Logger logger, String bundleName, String fqcn) {
 		super(logger, LoggerWrapper.class.getName());
 		
 		this.fqcn = fqcn;
 		
 		this.locationAware = logger instanceof LocationAwareLogger;
 		this.logger = logger;
 		
 		// create bundle marker
 		if (bundleName != null && !bundleName.isEmpty()) {
 			// detached bundle marker (because there are different instances with this name, referencing different bundle name markers)
 			bundleMarker = MarkerFactory.getDetachedMarker(BUNDLE_MARKER_NAME);
 			// add reference to bundle name marker
 			bundleMarker.add(MarkerFactory.getMarker(BUNDLE_NAME_PREFIX + bundleName));
 		}
 		else {
 			bundleMarker = null;
 		}
 	}
 	
 	/**
 	 * @see ALogger#createMarker(boolean, AGroup)
 	 */
 	@Override
 	public Marker createMarker(boolean userMessage, AGroup parentGroup) {
 		return createMarker(userMessage, parentGroup, null, null);
 	}
 
 	/**
 	 * Create a log message marker
 	 * 
 	 * @param userMessage if the message is a user message
 	 * @param parentGroup the parent group, may be <code>null</code>
 	 * @param beginTransaction the transaction to begin
 	 * @param endTransaction the transaction to end
 	 * 
 	 * @return the marker, which may be <code>null</code>
 	 */
 	public Marker createMarker(boolean userMessage,	AGroup parentGroup, 
 			ATransaction beginTransaction, ATransaction endTransaction) {
 		if (bundleMarker == null && !userMessage
 				&& parentGroup == null && beginTransaction == null
 				&& endTransaction == null) {
 			// quick exit if there are no markers to be created
 			return null;
 		}
 		
 		List<Marker> markers = new ArrayList<Marker>();
 		
 		if (bundleMarker != null) {
 			markers.add(bundleMarker);
 		}
 		
 		if (userMessage) {
 			markers.add(USER_MESSAGE);
 		}
 		
 		if (parentGroup != null) {
 			// create group start marker
 			Marker groupMarker = MarkerFactory.getDetachedMarker(GROUP_MARKER_NAME);
 			// add reference to group name marker
 			groupMarker.add(MarkerFactory.getMarker(parentGroup.getName()));
 			
 			markers.add(groupMarker);
 		}
 		
 		if (beginTransaction != null) {
 			markers.add(TRANSACTION_BEGIN);
 		}
 		
 		if (endTransaction != null) {
 			markers.add(TRANSACTION_END);
 		}
 		
 		// return markers
 		switch (markers.size()) {
 		case 0:
 			return null;
 		case 1:
 			return markers.get(0);
 		default:
 			Marker wrapper = MarkerFactory.getDetachedMarker(WRAPPER_MARKER_NAME);
 			for (Marker marker : markers) {
 				wrapper.add(marker);
 			}
 			return wrapper;
 		}
 	}
 	
 	/**
 	 * Create a new transaction with a unique identifier
 	 * 
 	 * @return the new transaction
 	 */
 	protected ATransaction createTransaction() {
 		ATransaction trans = new ATransactionImpl(this, getName() + "_"
 				+ UUID.randomUUID().toString());
 		
 		// add transaction to MDC
 		String transactions = MDC.get(MDC_TRANSACTIONS);
 		if (transactions == null || transactions.isEmpty()) {
 			transactions = trans.getIdentifier();
 		}
 		else {
 			transactions = transactions + MDC_TRANSACTIONS_SEPARATOR 
 				+ trans.getIdentifier();
 		}
 		MDC.put(MDC_TRANSACTIONS, transactions);
 		
 		return trans;
 	}
 
 	/**
 	 * @see ALogger#begin(String)
 	 */
 	@Override
 	public ATransaction begin(String msg) {
 		ATransaction trans = createTransaction();
 		
 		log(createMarker(false, null, trans, null), fqcn, ERROR_INT, msg, null, null);
 		
 		return trans;
 	}
 	
 	/**
 	 * End a transaction
 	 * 
 	 * @param trans the transaction to end
 	 */
 	public void end(ATransaction trans) {
 		log(createMarker(false, null, null, trans), fqcn, ERROR_INT, null, null, null);
 		
 		// remove transaction
 		String transactions = MDC.get(MDC_TRANSACTIONS);
 		if (transactions != null && !transactions.isEmpty()) {
 			String[] transPath = transactions.split(MDC_TRANSACTIONS_SEPARATOR);
 			int foundAt = -1;
 			for (int i = 0; i < transPath.length && foundAt < 0; i++) {
 				if (transPath[i].equals(trans.getIdentifier())) {
 					foundAt = i;
 				}
 			}
 			
 			// create new transactions string from MDC
 			StringBuffer newTransactions = new StringBuffer();
 			for (int i = 0; i < foundAt; i++) {
 				if (i != 0) {
 					newTransactions.append(MDC_TRANSACTIONS_SEPARATOR);
 				}
 				newTransactions.append(transPath[i]);
 			}
 			
 			MDC.put(MDC_TRANSACTIONS, newTransactions.toString());
 		}
 	}
 
 	/**
 	 * @see LoggerWrapper#debug(String, Throwable)
 	 */
 	@Override
 	public void debug(String msg, Throwable t) {
 		log(createMarker(false, null, null, null), fqcn, DEBUG_INT, msg, null, t);
 	}
 
 	/**
 	 * @see LoggerWrapper#debug(String)
 	 */
 	@Override
 	public void debug(String msg) {
 		log(createMarker(false, null, null, null), fqcn, DEBUG_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#error(String, Throwable)
 	 */
 	@Override
 	public void error(String msg, Throwable t) {
 		log(createMarker(false, null, null, null), fqcn, ERROR_INT, msg, null, t);
 	}
 
 	/**
 	 * @see LoggerWrapper#error(String)
 	 */
 	@Override
 	public void error(String msg) {
 		log(createMarker(false, null, null, null), fqcn, ERROR_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#info(String, Throwable)
 	 */
 	@Override
 	public void info(String msg, Throwable t) {
 		log(createMarker(false, null, null, null), fqcn, INFO_INT, msg, null, t);
 	}
 
 	/**
 	 * @see LoggerWrapper#info(String)
 	 */
 	@Override
 	public void info(String msg) {
 		log(createMarker(false, null, null, null), fqcn, INFO_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#trace(String, Throwable)
 	 */
 	@Override
 	public void trace(String msg, Throwable t) {
 		log(createMarker(false, null, null, null), fqcn, TRACE_INT, msg, null, t);
 	}
 
 	/**
 	 * @see LoggerWrapper#trace(String)
 	 */
 	@Override
 	public void trace(String msg) {
 		log(createMarker(false, null, null, null), fqcn, TRACE_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#warn(String, Throwable)
 	 */
 	@Override
 	public void warn(String msg, Throwable t) {
 		log(createMarker(false, null, null, null), fqcn, WARN_INT, msg, null, t);
 	}
 
 	/**
 	 * @see LoggerWrapper#warn(String)
 	 */
 	@Override
 	public void warn(String msg) {
 		log(createMarker(false, null, null, null), fqcn, WARN_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LocationAwareLogger#log(Marker, String, int, String, Object[], Throwable)
 	 */
 	@Override
 	public void log(Marker marker, String fqcn, int level, String msg, Object[] args,
 			Throwable t) {
 		if (locationAware) {
 			((LocationAwareLogger) logger).log(marker, fqcn, level, msg, args, t);
 		}
 	}
 
 	/**
 	 * @see ALogger#debug(AGroup, String, Throwable)
 	 */
 	@Override
 	public void debug(AGroup parent, String msg, Throwable t) {
 		log(createMarker(false, parent, null, null), fqcn, DEBUG_INT, msg, null, t);
 	}
 
 	/**
 	 * @see ALogger#debug(AGroup, String)
 	 */
 	@Override
 	public void debug(AGroup parent, String msg) {
 		log(createMarker(false, parent, null, null), fqcn, DEBUG_INT, msg, null, null);
 	}
 
 	/**
 	 * @see ALogger#error(AGroup, String, Throwable)
 	 */
 	@Override
 	public void error(AGroup parent, String msg, Throwable t) {
 		log(createMarker(false, parent, null, null), fqcn, ERROR_INT, msg, null, t);
 	}
 
 	/**
 	 * @see ALogger#error(AGroup, String)
 	 */
 	@Override
 	public void error(AGroup parent, String msg) {
 		log(createMarker(false, parent, null, null), fqcn, ERROR_INT, msg, null, null);
 	}
 
 	/**
 	 * @see ALogger#info(AGroup, String, Throwable)
 	 */
 	@Override
 	public void info(AGroup parent, String msg, Throwable t) {
 		log(createMarker(false, parent, null, null), fqcn, INFO_INT, msg, null, t);
 	}
 
 	/**
 	 * @see ALogger#info(AGroup, String)
 	 */
 	@Override
 	public void info(AGroup parent, String msg) {
 		log(createMarker(false, parent, null, null), fqcn, INFO_INT, msg, null, null);
 	}
 
 	/**
 	 * @see ALogger#trace(AGroup, String, Throwable)
 	 */
 	@Override
 	public void trace(AGroup parent, String msg, Throwable t) {
 		log(createMarker(false, parent, null, null), fqcn, TRACE_INT, msg, null, t);
 	}
 
 	/**
 	 * @see ALogger#trace(AGroup, String)
 	 */
 	@Override
 	public void trace(AGroup parent, String msg) {
 		log(createMarker(false, parent, null, null), fqcn, TRACE_INT, msg, null, null);
 	}
 
 	/**
 	 * @see ALogger#userError(AGroup, String, Throwable)
 	 */
 	@Override
 	public void userError(AGroup parent, String msg, Throwable t) {
 		log(createMarker(true, parent, null, null), fqcn, ERROR_INT, msg, null, t);
 	}
 
 	/**
 	 * @see ALogger#userError(AGroup, String)
 	 */
 	@Override
 	public void userError(AGroup parent, String msg) {
 		log(createMarker(true, parent, null, null), fqcn, ERROR_INT, msg, null, null);
 	}
 
 	/**
 	 * @see ALogger#userError(String, Throwable)
 	 */
 	@Override
 	public void userError(String msg, Throwable t) {
 		log(createMarker(true, null, null, null), fqcn, ERROR_INT, msg, null, t);
 	}
 
 	/**
 	 * @see ALogger#userError(String)
 	 */
 	@Override
 	public void userError(String msg) {
 		log(createMarker(true, null, null, null), fqcn, ERROR_INT, msg, null, null);
 	}
 
 	/**
 	 * @see ALogger#userInfo(AGroup, String, Throwable)
 	 */
 	@Override
 	public void userInfo(AGroup parent, String msg, Throwable t) {
 		log(createMarker(true, parent, null, null), fqcn, INFO_INT, msg, null, t);
 	}
 
 	/**
 	 * @see ALogger#userInfo(AGroup, String)
 	 */
 	@Override
 	public void userInfo(AGroup parent, String msg) {
 		log(createMarker(true, parent, null, null), fqcn, INFO_INT, msg, null, null);
 	}
 
 	/**
 	 * @see ALogger#userInfo(String, Throwable)
 	 */
 	@Override
 	public void userInfo(String msg, Throwable t) {
 		log(createMarker(true, null, null, null), fqcn, INFO_INT, msg, null, t);
 	}
 
 	/**
 	 * @see ALogger#userInfo(String)
 	 */
 	@Override
 	public void userInfo(String msg) {
 		log(createMarker(true, null, null, null), fqcn, INFO_INT, msg, null, null);
 	}
 
 	/**
 	 * @see ALogger#userWarn(AGroup, String, Throwable)
 	 */
 	@Override
 	public void userWarn(AGroup parent, String msg, Throwable t) {
 		log(createMarker(true, parent, null, null), fqcn, WARN_INT, msg, null, t);
 	}
 
 	/**
 	 * @see ALogger#userWarn(AGroup, String)
 	 */
 	@Override
 	public void userWarn(AGroup parent, String msg) {
 		log(createMarker(true, parent, null, null), fqcn, WARN_INT, msg, null, null);
 	}
 
 	/**
 	 * @see ALogger#userWarn(String, Throwable)
 	 */
 	@Override
 	public void userWarn(String msg, Throwable t) {
 		log(createMarker(true, null, null, null), fqcn, WARN_INT, msg, null, t);
 	}
 
 	/**
 	 * @see ALogger#userWarn(String)
 	 */
 	@Override
 	public void userWarn(String msg) {
 		log(createMarker(true, null, null, null), fqcn, WARN_INT, msg, null, null);
 	}
 
 	/**
 	 * @see ALogger#warn(AGroup, String, Throwable)
 	 */
 	@Override
 	public void warn(AGroup parent, String msg, Throwable t) {
 		log(createMarker(false, parent, null, null), fqcn, WARN_INT, msg, null, t);
 	}
 
 	/**
 	 * @see ALogger#warn(AGroup, String)
 	 */
 	@Override
 	public void warn(AGroup parent, String msg) {
 		log(createMarker(false, parent, null, null), fqcn, WARN_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#debug(String, Object, Object)
 	 */
 	@Override
 	public void debug(String format, Object arg1, Object arg2) {
		String msg = MessageFormatter.format(format, arg1, arg2).getMessage();
 		log(createMarker(false, null), fqcn, DEBUG_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#debug(String, Object)
 	 */
 	@Override
 	public void debug(String format, Object arg1) {
		String msg = MessageFormatter.format(format, arg1).getMessage();
 		log(createMarker(false, null), fqcn, DEBUG_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#debug(String, Object[])
 	 */
 	@Override
 	public void debug(String format, Object... args) {
 		log(createMarker(false, null), fqcn, DEBUG_INT, format, args, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#error(String, Object, Object)
 	 */
 	@Override
 	public void error(String format, Object arg1, Object arg2) {
		String msg = MessageFormatter.format(format, arg1, arg2).getMessage();
 		log(createMarker(false, null), fqcn, ERROR_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#error(String, Object)
 	 */
 	@Override
 	public void error(String format, Object arg1) {
		String msg = MessageFormatter.format(format, arg1).getMessage();
 		log(createMarker(false, null), fqcn, ERROR_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#error(String, Object[])
 	 */
 	@Override
 	public void error(String format, Object... args) {
 		log(createMarker(false, null), fqcn, ERROR_INT, format, args, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#info(String, Object, Object)
 	 */
 	@Override
 	public void info(String format, Object arg1, Object arg2) {
		String msg = MessageFormatter.format(format, arg1, arg2).getMessage();
 		log(createMarker(false, null), fqcn, INFO_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#info(String, Object)
 	 */
 	@Override
 	public void info(String format, Object arg1) {
		String msg = MessageFormatter.format(format, arg1).getMessage();
 		log(createMarker(false, null), fqcn, INFO_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#info(String, Object[])
 	 */
 	@Override
 	public void info(String format, Object... args) {
 		log(createMarker(false, null), fqcn, INFO_INT, format, args, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#trace(String, Object, Object)
 	 */
 	@Override
 	public void trace(String format, Object arg1, Object arg2) {
		String msg = MessageFormatter.format(format, arg1, arg2).getMessage();
 		log(createMarker(false, null), fqcn, TRACE_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#trace(String, Object)
 	 */
 	@Override
 	public void trace(String format, Object arg1) {
		String msg = MessageFormatter.format(format, arg1).getMessage();
 		log(createMarker(false, null), fqcn, TRACE_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#trace(String, Object[])
 	 */
 	@Override
 	public void trace(String format, Object... args) {
 		log(createMarker(false, null), fqcn, TRACE_INT, format, args, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#warn(String, Object, Object)
 	 */
 	@Override
 	public void warn(String format, Object arg1, Object arg2) {
		String msg = MessageFormatter.format(format, arg1, arg2).getMessage();
 		log(createMarker(false, null), fqcn, WARN_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#warn(String, Object)
 	 */
 	@Override
 	public void warn(String format, Object arg1) {
		String msg = MessageFormatter.format(format, arg1).getMessage();
 		log(createMarker(false, null), fqcn, WARN_INT, msg, null, null);
 	}
 
 	/**
 	 * @see LoggerWrapper#warn(String, Object[])
 	 */
 	@Override
 	public void warn(String format, Object... args) {
 		log(createMarker(false, null), fqcn, WARN_INT, format, args, null);
 	}
 
 }
