 /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  Copyright (C) 2008 CEJUG - Ceará Java Users Group
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  
  This file is part of the CEJUG-CLASSIFIEDS Project - an  open source classifieds system
  originally used by CEJUG - Ceará Java Users Group.
  The project is hosted https://cejug-classifieds.dev.java.net/
  
  You can contact us through the mail dev@cejug-classifieds.dev.java.net
  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
 package net.java.dev.cejug.classifieds.exception;
 
 import java.sql.SQLIntegrityConstraintViolationException;
 import javax.interceptor.AroundInvoke;
 import javax.interceptor.InvocationContext;
 import javax.persistence.PersistenceException;
 
 /**
  * @author $Author: rodrigolopes $
  * @version $Rev: 627 $ ($Date: 2008-09-22 20:14:57 +0200 (seg, 22 set 2008) $)
  */
 public class ExceptionInterceptor {
 
     @AroundInvoke
     public Object exceptionInterceptor(InvocationContext context) throws Exception {
 
         Object result = null;
 
         try {
             result = context.proceed();
         } catch (PersistenceException pe) {
             String methodName = context.getMethod().getName();
             if (methodName.startsWith("delete") && (pe.getCause().getCause() instanceof SQLIntegrityConstraintViolationException)) {
                 throw new ObjectInUseException("Could not delete because object is referenced by another entity.", pe);
             }
         }
 
         return result;
     }
 }
