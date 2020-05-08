// Copyright (C) 2005 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.console.distribution;
 
 import java.io.File;
 
 import net.grinder.console.communication.DistributionControl;
 import net.grinder.util.FileContents;
 
 
 /**
  * File Distribution Handler implementation.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class FileDistributionHandlerImplementation
   implements FileDistributionHandler {
 
   private final File m_directory;
   private final File[] m_files;
   private final DistributionControl m_distributionControl;
   private final UpdateableAgentCacheState m_updateableCacheState;
 
   private int m_fileIndex = 0;
 
   FileDistributionHandlerImplementation(
     File directory,
     File[] files,
     DistributionControl distributionControl,
     UpdateableAgentCacheState updateableCacheState) {
 
     m_directory = directory;
     m_files = files;
     m_distributionControl = distributionControl;
     m_updateableCacheState = updateableCacheState;
   }
 
   public Result sendNextFile() throws FileContents.FileContentsException {
 
     if (m_fileIndex < m_files.length) {
       if (m_fileIndex == 0) {
         long latestFileTime = 0;
 
         for (int i = 0; i < m_files.length; ++i) {
          final long fileTime = m_files[i].lastModified();
 
           if (fileTime > latestFileTime) {
             latestFileTime = fileTime;
           }
         }
 
         m_updateableCacheState.updateStarted(latestFileTime);
       }
 
       try {
         final int index = m_fileIndex;
         final File file = m_files[index];
 
         m_distributionControl.sendFile(new FileContents(m_directory, file));
 
         return new Result() {
             public int getProgressInCents() {
               return ((index + 1) * 100) / m_files.length;
             }
 
             public String getFileName() {
               return file.getPath();
             }
           };
       }
       finally {
         ++m_fileIndex;
       }
     }
 
     m_updateableCacheState.updateComplete();
 
     return null;
   }
 }
