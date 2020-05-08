 /*==========================================================================*\
  |  $Id$
  |*-------------------------------------------------------------------------*|
  |  Copyright (C) 2006-2009 Virginia Tech
  |
  |  This file is part of Web-CAT.
  |
  |  Web-CAT is free software; you can redistribute it and/or modify
  |  it under the terms of the GNU Affero General Public License as published
  |  by the Free Software Foundation; either version 3 of the License, or
  |  (at your option) any later version.
  |
  |  Web-CAT is distributed in the hope that it will be useful,
  |  but WITHOUT ANY WARRANTY; without even the implied warranty of
  |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  |  GNU General Public License for more details.
  |
  |  You should have received a copy of the GNU Affero General Public License
  |  along with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
 \*==========================================================================*/
 
 package net.sf.webcat.plugintester.util;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 //-------------------------------------------------------------------------
 /**
  * A subclass of the java.util.Properties class that must be initialized from
 * a file on disk, and which always keeps the in-memory property set in sync
 * with the contents on disk.
  * 
  * @author Tony Allevato
  * @version $Id$
  */
 public class ImmediateProperties extends Properties
 {
     //~ Constructors ..........................................................
 
     // ----------------------------------------------------------
     /**
      * Initializes a new instance of ImmediateProperties by loading them from
      * the specified file.
      * 
      * @param path the path to the file that backs this Properties instance.
      */
     public ImmediateProperties(String path)
     {
         super();
         
         this.path = path;
         
         try
         {
             FileInputStream stream = new FileInputStream(path);
             load(stream);
             stream.close();
         }
         catch (IOException e)
         {
             // Do nothing; start the property set empty.
         }
     }
 
 
     //~ Methods ...............................................................
 
     // ----------------------------------------------------------
     /**
      * Removes the specified property from the file.
      * 
      * @param key the name of the property to remove
      * @return the previous value of the property
      */
     @Override
     public Object remove(Object key)
     {
         Object previousValue = super.remove(key);
         writeToFile();
         return previousValue;
     }
     
 
     // ----------------------------------------------------------
     /**
      * Sets the value of the specified property.
      * 
      * @param key the name of the property to set
      * @param value the value of the property
      * @return the previous value of the property
      */
     @Override
     public Object put(Object key, Object value)
     {
         Object previousValue = super.put(key, value);
         writeToFile();
         return previousValue;
     }
     
     
     // ----------------------------------------------------------
     /**
      * Saves the current properties set to their associated file.
      */
     private void writeToFile()
     {
         try
         {
             FileOutputStream stream = new FileOutputStream(path);
             store(stream, null);
             stream.close();
         }
         catch (IOException e)
         {
             // Do nothing.
         }
     }
 
     
     //~ Instance/static variables .............................................
 
     /** The path to the file that backs this Properties instance. */
     private String path;
 
     private static final long serialVersionUID = -6139105936918525923L;
 }
