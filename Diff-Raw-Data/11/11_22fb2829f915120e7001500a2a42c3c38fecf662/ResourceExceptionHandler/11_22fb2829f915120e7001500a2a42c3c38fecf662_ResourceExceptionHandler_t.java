 /*
  * Copyright (C) 2013 SLUB Dresden
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
 
 package de.qucosa.spring;
 
 import com.yourmediashelf.fedora.client.FedoraClientException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.ControllerAdvice;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 
 @ControllerAdvice
 class ResourceExceptionHandler {
 
    private static Log log = LogFactory.getLog(ResourceExceptionHandler.class);

     @ExceptionHandler(Exception.class)
    public ResponseEntity generalExceptionHandler(Exception ex) {
        log.error(ex);
         return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
     }
 
     @ExceptionHandler(FedoraClientException.class)
     public ResponseEntity fedoraClientExceptionHandler(FedoraClientException fe) {
        log.warn(fe);
         return new ResponseEntity(HttpStatus.valueOf(fe.getStatus()));
     }
 
 }
