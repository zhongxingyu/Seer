 /* This file is part of the University of Cambridge Web Authentication
  * System Java Toolkit
  *
  * Copyright 2005 University of Cambridge
  *
  * This toolkit is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * The toolkit is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this toolkit; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id: WebauthException.java,v 1.2 2005/03/30 13:17:06 jw35 Exp $
  *
  */
 
 package uk.ac.cam.ucs.webauth;
 
 /**
  * Represents exception conditions within the WebAuth system
  *
  * @version $Revision: 1.2 $ $Date: 2005/03/30 13:17:06 $
  */
 public class WebauthException extends Exception {

  private static final long serialVersionUID = 1L;

     /**
      * Default constrictor
      */
     public WebauthException() {
 	super();
     }
 
     /**
      * Alternate constructor
      *
      * @param desc  a string description of the exception
      */
     public WebauthException(String desc) {
 	super(desc);
     }
 
 }
