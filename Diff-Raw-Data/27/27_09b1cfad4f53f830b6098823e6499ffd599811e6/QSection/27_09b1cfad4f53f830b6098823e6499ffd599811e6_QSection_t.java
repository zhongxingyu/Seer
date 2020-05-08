 /*
  * libqcw - A library for parsing, manipulating, and writing QCharts (TM) 
  * workspace files.
  *
  * Copyright (C) 2008 Jon Nall
  * QCharts is a registered service mark of eSignal, Inc.
  * RagingBull is a registered service mark of eSignal, Inc.
  *
  * Licensed under the Open Software License version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You should
  * have received a copy of the License along with this software; if not, you
  * may obtain a copy of the License at
  *
  * http://opensource.org/licenses/osl-2.0.php
  *
  * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  * CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 
 package org.stuntaz.libqcw.blobs;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.stuntaz.libqcw.IWorkspaceVisitor;
 import org.stuntaz.libqcw.QCWException;
 import org.stuntaz.libqcw.blobs.workspace.SectionHeaderInfoBlob;
 import org.stuntaz.libqcw.defines.QWorkspaceSection;
 
 /**
  * This class forms the base class for all workspace sections. These are
  * the major sections of the workspace and do not have header information as
  * part of their data (it is specified in the {@link SectionHeaderInfoBlob}).
  * 
  * @author nall
  *
  */
 public abstract class QSection
     extends QPrimitive
 {
     @Override
     public int getSize()
     {
         // No headers on these guys
         return getInternalSize();
     }
 
     @Override
     public void write(final OutputStream output)
         throws IOException
     {
         // Do nothing -- no type or header to write
     }
 
     @Override
     public final char getHeaderType()
     {
         throw new QCWException("Sections do not have header types");
     }
 
     /**
      * Returns the type of this section
      * 
      * @return the section type
      */
     public abstract QWorkspaceSection getSectionType();
 
     /**
      * Accepts a workspace visitor per the visitor pattern.
      * 
      * @param visitor the visitor to accept
      */
     public abstract void accept(IWorkspaceVisitor visitor);
 }
