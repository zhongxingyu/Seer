 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 Myles Chippendale
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     Mylesc Chippendale <mylesc At paneris.org>
  *     http://paneris.org/
  *     29 Stanley Road, Oxford, OX4 1QY, UK
  */
 
 package org.melati.servlet;
 
 import java.io.File;
 
 import org.melati.Melati;
 import org.melati.util.UTF8URLEncoder;
 import org.melati.util.FileUtils;
 
 /**
  * The default way to save an uploaded file to disk.
  *
  * We tell it what directory to save it in and the base URL 
  * to that directory.
  */
 
 public class DefaultFileDataAdaptor extends BaseFileDataAdaptor {
 
   protected Melati melati;
   protected String uploadDir = null;
   protected String uploadURL = null;
   protected boolean makeUnique = true;
 
   /**
    * Constructor.
    * 
    * @param melatiP    The current melati
    * @param uploadDirP The directory to save this file in
    * @param uploadUrlP A URL pointing to this directory (null if there
    *                   isn't an appropriate URL)
    */
   public DefaultFileDataAdaptor(Melati melatiP, String uploadDirP, String uploadUrlP) {
     this.melati = melatiP;
     this.uploadDir = uploadDirP;
     this.uploadURL = uploadUrlP;
   }
 
   /**
    * Constructor.
    * 
    * @param melatiP    The current melati
    * @param uploadDir  The directory to save this file in
    * @param uploadURL  A URL pointing to this directory  (null if there
    *                   isn't an appropriate URL)
    * @param makeUnique Whether we should make sure the new file has a unique
    *                   name within the <code>uploadDir</code> directory
    */
 
   public DefaultFileDataAdaptor(Melati melatiP, String uploadDir, String uploadURL,
                                 boolean makeUnique) {
     this.uploadDir = uploadDir;
     this.uploadURL = uploadURL;
     this.makeUnique = makeUnique;
   }
 
   protected File calculateLocalFile() {
     // FIXME decode surely?
     File f = new File(uploadDir,
                       UTF8URLEncoder.encode(field.getUploadedFileName(), melati.getEncoding()));
     return makeUnique ? FileUtils.withUniqueName(f) : f;
   }
 
   protected String calculateURL() {
     return (uploadURL != null && getFile() != null)
              ? uploadURL + File.separatorChar + file.getName()
              : null;
   }
 }
