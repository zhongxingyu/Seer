 /**************************************************************************************************
  * This file is part of [SpringAtom] Copyright [kornicameister@gmail.com][2013]                   *
  *                                                                                                *
  * [SpringAtom] is free software: you can redistribute it and/or modify                           *
  * it under the terms of the GNU General Public License as published by                           *
  * the Free Software Foundation, either version 3 of the License, or                              *
  * (at your option) any later version.                                                            *
  *                                                                                                *
  * [SpringAtom] is distributed in the hope that it will be useful,                                *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
  * GNU General Public License for more details.                                                   *
  *                                                                                                *
  * You should have received a copy of the GNU General Public License                              *
  * along with [SpringAtom].  If not, see <http://www.gnu.org/licenses/gpl.html>.                  *
  **************************************************************************************************/
 
 package org.agatom.springatom.server.service.support.exceptions;
 
 import org.springframework.data.domain.Persistable;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 public class ServiceException
         extends Exception {
     private static final String MSG_PATTERN      = "For target=%s exception occurred\nmsg=%s";
     private static final String MSG_PATTERN_2    = "For target=%s exception occurred";
     private static final long   serialVersionUID = 5939562098957474953L;
 
    public ServiceException(final String message) {
        super(message);
    }

    public ServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }

     public ServiceException(final Class<? extends Persistable> target,
                             final String message) {
         super(String.format(MSG_PATTERN, target, message));
     }
 
     public ServiceException(final Class<? extends Persistable> target,
                             final String message,
                             final Throwable cause) {
         super(String.format(MSG_PATTERN, target, message), cause);
     }
 
     public ServiceException(final Class<? extends Persistable> target,
                             final Throwable cause) {
         super(String.format(MSG_PATTERN_2, target), cause);
     }
 
     public ServiceException(final Class<? extends Persistable> target,
                             final String message,
                             final Throwable cause,
                             final boolean enableSuppression,
                             final boolean writableStackTrace) {
         super(String.format(MSG_PATTERN, target, message), cause, enableSuppression, writableStackTrace);
     }

    public ServiceException(final Throwable cause) {
        super(cause);
    }
 }
