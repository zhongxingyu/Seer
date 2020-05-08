 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 public class JarStore extends Store {
 
     private final File _file;
     private final JarFile jar_file;
 
     public JarStore(File file) throws IOException, ClassNotFoundException {
         _file = file;
         jar_file = new JarFile(_file);
         for (final File f : getInfoFiles()) {
             loadTrace(f.getParentFile().getName());
         }
     }
 
     private Set<File> getInfoFiles() throws IOException {
         final Set<File> infoFiles = new HashSet<File>();
         final Enumeration<JarEntry> entries = jar_file.entries();
         while (entries.hasMoreElements()) {
             final JarEntry entry = entries.nextElement();
             if (entry.getName().endsWith(infoFile))
                 infoFiles.add(new File(entry.getName()));
         }
         return infoFiles;
     }
 
     @Override
     public InputStream getInputStream(String name) throws IOException {
         final JarEntry e = jar_file.getJarEntry(name);
         if (e == null)
             throw new IOException();
         return jar_file.getInputStream(e);
     }
 
     @Override
     public boolean hasFile(String name) {
         return (jar_file.getEntry(name) != null);
     }
 }
