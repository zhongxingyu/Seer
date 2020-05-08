 /*
  * jPOS Project [http://jpos.org]
  * Copyright (C) 2000-2010 Alejandro P. Revilla
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.core;
 
 import org.jpos.ee.Constants;
 
 
 /**
  * This interface allows the programmer to defines some code to execute before
  * or after any operation execution.
  * 
  * @author jpaoletti jeronimo.paoletti@gmail.com
  * 
  */
 public interface OperationContext extends Constants{
 
    /**This method is excecuted at the very beggining of the process, before
      * converterting or replace any data on objects.
      */
     public void preConversion (PMContext ctx) throws PMException;
     
     /**This method is executed before trying to execute the main method of the operation, that is
      * before opening any transaction. 
      * @param ctx The context
      */
     public void preExecute  (PMContext ctx) throws PMException;
 
     /**This method is executed after the main method of the operation.
      * @param ctx The context
      */
     public void postExecute (PMContext ctx) throws PMException;
 }
 
