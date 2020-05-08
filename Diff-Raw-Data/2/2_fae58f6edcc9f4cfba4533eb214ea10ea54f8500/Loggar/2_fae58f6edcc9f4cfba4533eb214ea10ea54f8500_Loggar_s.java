 /*
  * Copyright 2010 - 2013 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.ahs.util;
 
 import org.slf4j.*;
 
 /**
 * A drop-in replacement to <code>org.slf4j.Logger</code> that wraps an existing Logginer,
  * but all logging methods now always return boolean <code>true</code> (which means you
  * can use them with the <code>assert</code> keyword to remove them from runtime code
  * completely!).
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  */
 public final class Loggar {
 	public Loggar(Logger $toWrap) {
 		$wrapped = $toWrap;
 	}
 	
 	private final Logger	$wrapped;
 	
 	public boolean debug(Marker $arg0, String $arg1, Object $arg2, Object $arg3) {
 		this.$wrapped.debug($arg0, $arg1, $arg2, $arg3);
 		return true;
 	}
 	
 	public boolean debug(Marker $arg0, String $arg1, Object $arg2) {
 		this.$wrapped.debug($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean debug(Marker $arg0, String $arg1, Object[] $arg2) {
 		this.$wrapped.debug($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean debug(Marker $arg0, String $arg1, Throwable $arg2) {
 		this.$wrapped.debug($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean debug(Marker $arg0, String $arg1) {
 		this.$wrapped.debug($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean debug(String $arg0, Object $arg1, Object $arg2) {
 		this.$wrapped.debug($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean debug(String $arg0, Object $arg1) {
 		this.$wrapped.debug($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean debug(String $arg0, Object[] $arg1) {
 		this.$wrapped.debug($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean debug(String $arg0, Throwable $arg1) {
 		this.$wrapped.debug($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean debug(String $arg0) {
 		this.$wrapped.debug($arg0);
 		return true;
 	}
 	
 	public boolean error(Marker $arg0, String $arg1, Object $arg2, Object $arg3) {
 		this.$wrapped.error($arg0, $arg1, $arg2, $arg3);
 		return true;
 	}
 	
 	public boolean error(Marker $arg0, String $arg1, Object $arg2) {
 		this.$wrapped.error($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean error(Marker $arg0, String $arg1, Object[] $arg2) {
 		this.$wrapped.error($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean error(Marker $arg0, String $arg1, Throwable $arg2) {
 		this.$wrapped.error($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean error(Marker $arg0, String $arg1) {
 		this.$wrapped.error($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean error(String $arg0, Object $arg1, Object $arg2) {
 		this.$wrapped.error($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean error(String $arg0, Object $arg1) {
 		this.$wrapped.error($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean error(String $arg0, Object[] $arg1) {
 		this.$wrapped.error($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean error(String $arg0, Throwable $arg1) {
 		this.$wrapped.error($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean error(String $arg0) {
 		this.$wrapped.error($arg0);
 		return true;
 	}
 	
 	public String getName() {
 		return this.$wrapped.getName();
 	}
 	
 	public boolean info(Marker $arg0, String $arg1, Object $arg2, Object $arg3) {
 		this.$wrapped.info($arg0, $arg1, $arg2, $arg3);
 		return true;
 	}
 	
 	public boolean info(Marker $arg0, String $arg1, Object $arg2) {
 		this.$wrapped.info($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean info(Marker $arg0, String $arg1, Object[] $arg2) {
 		this.$wrapped.info($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean info(Marker $arg0, String $arg1, Throwable $arg2) {
 		this.$wrapped.info($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean info(Marker $arg0, String $arg1) {
 		this.$wrapped.info($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean info(String $arg0, Object $arg1, Object $arg2) {
 		this.$wrapped.info($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean info(String $arg0, Object $arg1) {
 		this.$wrapped.info($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean info(String $arg0, Object[] $arg1) {
 		this.$wrapped.info($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean info(String $arg0, Throwable $arg1) {
 		this.$wrapped.info($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean info(String $arg0) {
 		this.$wrapped.info($arg0);
 		return true;
 	}
 	
 	public boolean isDebugEnabled() {
 		return this.$wrapped.isDebugEnabled();
 	}
 	
 	public boolean isDebugEnabled(Marker $arg0) {
 		return this.$wrapped.isDebugEnabled($arg0);
 	}
 	
 	public boolean isErrorEnabled() {
 		return this.$wrapped.isErrorEnabled();
 	}
 	
 	public boolean isErrorEnabled(Marker $arg0) {
 		return this.$wrapped.isErrorEnabled($arg0);
 	}
 	
 	public boolean isInfoEnabled() {
 		return this.$wrapped.isInfoEnabled();
 	}
 	
 	public boolean isInfoEnabled(Marker $arg0) {
 		return this.$wrapped.isInfoEnabled($arg0);
 	}
 	
 	public boolean isTraceEnabled() {
 		return this.$wrapped.isTraceEnabled();
 	}
 	
 	public boolean isTraceEnabled(Marker $arg0) {
 		return this.$wrapped.isTraceEnabled($arg0);
 	}
 	
 	public boolean isWarnEnabled() {
 		return this.$wrapped.isWarnEnabled();
 	}
 	
 	public boolean isWarnEnabled(Marker $arg0) {
 		return this.$wrapped.isWarnEnabled($arg0);
 	}
 	
 	public boolean trace(Marker $arg0, String $arg1, Object $arg2, Object $arg3) {
 		this.$wrapped.trace($arg0, $arg1, $arg2, $arg3);
 		return true;
 	}
 	
 	public boolean trace(Marker $arg0, String $arg1, Object $arg2) {
 		this.$wrapped.trace($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean trace(Marker $arg0, String $arg1, Object[] $arg2) {
 		this.$wrapped.trace($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean trace(Marker $arg0, String $arg1, Throwable $arg2) {
 		this.$wrapped.trace($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean trace(Marker $arg0, String $arg1) {
 		this.$wrapped.trace($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean trace(String $arg0, Object $arg1, Object $arg2) {
 		this.$wrapped.trace($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean trace(String $arg0, Object $arg1) {
 		this.$wrapped.trace($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean trace(String $arg0, Object[] $arg1) {
 		this.$wrapped.trace($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean trace(String $arg0, Throwable $arg1) {
 		this.$wrapped.trace($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean trace(String $arg0) {
 		this.$wrapped.trace($arg0);
 		return true;
 	}
 	
 	public boolean warn(Marker $arg0, String $arg1, Object $arg2, Object $arg3) {
 		this.$wrapped.warn($arg0, $arg1, $arg2, $arg3);
 		return true;
 	}
 	
 	public boolean warn(Marker $arg0, String $arg1, Object $arg2) {
 		this.$wrapped.warn($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean warn(Marker $arg0, String $arg1, Object[] $arg2) {
 		this.$wrapped.warn($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean warn(Marker $arg0, String $arg1, Throwable $arg2) {
 		this.$wrapped.warn($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean warn(Marker $arg0, String $arg1) {
 		this.$wrapped.warn($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean warn(String $arg0, Object $arg1, Object $arg2) {
 		this.$wrapped.warn($arg0, $arg1, $arg2);
 		return true;
 	}
 	
 	public boolean warn(String $arg0, Object $arg1) {
 		this.$wrapped.warn($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean warn(String $arg0, Object[] $arg1) {
 		this.$wrapped.warn($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean warn(String $arg0, Throwable $arg1) {
 		this.$wrapped.warn($arg0, $arg1);
 		return true;
 	}
 	
 	public boolean warn(String $arg0) {
 		this.$wrapped.warn($arg0);
 		return true;
 	}
 	
 }
