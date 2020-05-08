 /*==========================================================================*\
  |  $Id$
  |*-------------------------------------------------------------------------*|
  |  Copyright (C) 2006-2009 Virginia Tech
  |
  |  This file is part of Web-CAT Electronic Submitter.
  |
  |  Web-CAT is free software; you can redistribute it and/or modify
  |  it under the terms of the GNU General Public License as published by
  |  the Free Software Foundation; either version 2 of the License, or
  |  (at your option) any later version.
  |
  |  Web-CAT is distributed in the hope that it will be useful,
  |  but WITHOUT ANY WARRANTY; without even the implied warranty of
  |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  |  GNU General Public License for more details.
  |
  |  You should have received a copy of the GNU General Public License along
  |  with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
 \*==========================================================================*/
 
 package org.webcat.submitter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 //--------------------------------------------------------------------------
 /**
  * A concrete implementation of ISubmittableItem that is based on
  * {@link java.io.File}, to support packaging and submitting files and
  * directories on the local file system.
  *
  * @author  Tony Allevato (Virginia Tech Computer Science)
  * @author  latest changes by: $Author$
  * @version $Revision$ $Date$
  */
 public class SubmittableFile implements ISubmittableItem
 {
     //~ Constructors ..........................................................
 
     // ----------------------------------------------------------
     /**
      * Initializes a new SubmittableFile as a root with the specified path.
      *
      * @param file the path to the file
      */
     public SubmittableFile(File file)
     {
         this(null, file);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Initializes a new SubmittableFile with the specified parent and path.
      *
      * @param parent the SubmittableFile that is the parent of this one
      * @param file the path to the file
      */
     private SubmittableFile(SubmittableFile parent,
             File file)
     {
         this.parent = parent;
         this.file = file;
     }
 
 
     //~ Methods ...............................................................
 
     // ----------------------------------------------------------
     /**
      * @see ISubmittableItem#getFilename()
      */
     public String getFilename()
     {
         if (parent == null)
         {
             return file.getName();
         }
         else
         {
             String parentName = parent.getFilename();
 
             if (parentName.length() > 0)
             {
                parentName += File.separator;
             }
 
             return parentName + file.getName();
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * @see ISubmittableItem#getKind()
      */
     public SubmittableItemKind getKind()
     {
         if (file.isDirectory())
         {
             return SubmittableItemKind.FOLDER;
         }
         else
         {
             return SubmittableItemKind.FILE;
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * @see ISubmittableItem#getStream()
      */
     public InputStream getStream() throws IOException
     {
         if (getKind() == SubmittableItemKind.FILE)
         {
             return new FileInputStream(file);
         }
         else
         {
             return null;
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * @see ISubmittableItem#getChildren()
      */
     public ISubmittableItem[] getChildren()
     {
         if (getKind() == SubmittableItemKind.FOLDER)
         {
             File[] childFiles = file.listFiles();
             SubmittableFile[] entries =
                 new SubmittableFile[childFiles.length];
 
             for (int i = 0; i < childFiles.length; i++)
             {
                 entries[i] = new SubmittableFile(
                         this, childFiles[i].getAbsoluteFile());
             }
 
             return entries;
         }
         else
         {
             return new ISubmittableItem[0];
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets a human-readable string representation of this item.
      *
      * @return a human-readable string representation of the item
      */
     @Override
     public String toString()
     {
         return "<SubmittableFile: " + getFilename() + ">";
     }
 
 
     //~ Static/instance variables .............................................
 
     /* The parent of this submittable item. */
     private SubmittableFile parent;
 
     /* The path to this submittable item on the file system. */
     private File file;
 }
