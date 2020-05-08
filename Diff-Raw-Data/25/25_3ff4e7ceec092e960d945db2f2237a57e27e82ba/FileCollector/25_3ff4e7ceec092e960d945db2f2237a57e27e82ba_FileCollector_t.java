 /**
  * JBackup is a software managing backups.
  *
  * Copyright (C) 2013-2013 Fabien DUMINY (fabien [dot] duminy [at] webmails [dot] com)
  *
  * JBackup is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * JBackup is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
  * USA.
  */
 package org.jbackup.core.archive;
 
 import org.apache.commons.io.DirectoryWalker;
 import org.apache.commons.io.filefilter.IOFileFilter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 
 import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;
 
 /**
  * Class collecting files in a directory. Files are filtered with a directory filter and a file filter.
  */
public class FileCollector extends DirectoryWalker<File> {
     private static final Logger LOG = LoggerFactory.getLogger(FileCollector.class);
 
     private long totalSize = 0L;
 
     public FileCollector() {
         super(trueFileFilter(), trueFileFilter(), -1);
     }
 
     public FileCollector(IOFileFilter directoryFilter, IOFileFilter fileFilter) {
         super(directoryFilter, fileFilter, -1);
     }
 
     public long collect(Collection<File> results, File startDirectory) throws IOException {
         totalSize = 0L;
         walk(startDirectory, results);
         return totalSize;
     }
 
     @Override
    protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
         LOG.trace("handleFile {}", file.getAbsolutePath());
         results.add(file);
         totalSize += file.length();
     }
 }
