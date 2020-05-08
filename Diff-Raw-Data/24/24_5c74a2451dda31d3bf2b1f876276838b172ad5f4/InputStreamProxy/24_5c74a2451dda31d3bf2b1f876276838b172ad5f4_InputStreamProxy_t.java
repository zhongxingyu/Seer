 /*
  * libxjava -- utility library for cross-Java-platform development
  *             ${project.name}
  *
  * Copyright (c) 2010 Marcel Patzlaff (marcel.patzlaff@gmail.com)
  *
  * This library is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.libxjava.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * @author Marcel Patzlaff
  * @version ${project.artifactId} - ${project.version}
  */
 public final class InputStreamProxy extends InputStream {
    public volatile InputStream concreteStream;
     
     
     public int available() throws IOException {
         return concreteStream.available();
     }
 
     public void close() throws IOException {
         concreteStream.close();
     }
 
     public synchronized void mark(int readlimit) {
         concreteStream.mark(readlimit);
     }
 
     public boolean markSupported() {
         return concreteStream.markSupported();
     }
 
     public int read() throws IOException {
         return concreteStream.read();
     }
 
     public int read(byte[] b, int off, int len) throws IOException {
         return concreteStream.read(b, off, len);
     }
 
     public int read(byte[] b) throws IOException {
         return concreteStream.read(b);
     }
 
     public synchronized void reset() throws IOException {
         concreteStream.reset();
     }
 
     public long skip(long n) throws IOException {
         return concreteStream.skip(n);
     }
 }
