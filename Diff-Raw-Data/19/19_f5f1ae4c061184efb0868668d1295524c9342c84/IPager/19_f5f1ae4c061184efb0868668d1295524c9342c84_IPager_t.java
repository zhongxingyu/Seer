 /**
  * Copyright (C) 2010 Ian C. Smith <m4r35n357@gmail.com>
  *
  * This file is part of JavaEE6Webapp.
  *
  *     JavaEE6Webapp is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     JavaEE6Webapp is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with JavaEE6Webapp.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package uk.me.doitto.webapp.model;
 
 import javax.ejb.Local;
 
 /**
  *
  * @author super
  */
 @Local
 public interface IPager {
 
     Object firstPage ();
 
     Object previousPage ();
 
     Object nextPage ();
 
     Object lastPage ();
 
     Object changePageSize ();
    
    boolean isFirst ();
    
    boolean isLast ();
 }
