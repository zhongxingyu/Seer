 /**
  * EasyBeans
  * Copyright (C) 2011 Bull S.A.S.
  * Contact: easybeans@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  * --------------------------------------------------------------------------
  * $Id: IEnvEntry.java 5743 2011-02-28 16:02:42Z benoitf $
  * --------------------------------------------------------------------------
  */
 
 package org.ow2.easybeans.application.enventry;
 
 import javax.ejb.Remote;
 import javax.naming.NamingException;
 
 /**
  * Interface for the Env-Entry Bean.
  * @author Florent Benoit
  */
 @Remote
 public interface IEnvEntry {
 
 
    void checkCompEqualsModule() throws NamingException;
    void checkAppNotEqualsModule() throws NamingException;
 
     void checkInjectedFields();
 
 }
