 /*
  * junitflood - An automatic junit test generator
  * Copyright 2011 MeBigFatGuy.com
  * Copyright 2011 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.junitflood.classpath;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 public class DirectoryIterator implements Iterator<ClassPathItem> {
 
 	private final File directory;
 	private final List<File> directories;
 
 	public DirectoryIterator(File dir) {
 		directory = dir;
 		directories = new ArrayList<File>();
 
 		File[] items = dir.listFiles(new ClassFileFilter());
 		directories.addAll(Arrays.asList(items));
 	}
 
 	@Override
 	public boolean hasNext() {
 		return !directories.isEmpty();
 	}
 
 	@Override
 	public ClassPathItem next() {
		if (directories.isEmpty()) {
 			throw new NoSuchElementException();
 		}
 
 		File f = directories.remove(0);
 		while (f.isDirectory()) {
 			File[] items = f.listFiles(new ClassFileFilter());
 			directories.addAll(Arrays.asList(items));
			if (directories.isEmpty()) {
 				throw new NoSuchElementException("No more elements in " + directory);
 			}
 			f = directories.remove(0);
 		}
 
 		if (!f.isFile()) {
 			throw new NoSuchElementException("No more elements in " + directory);
 		}
 
 		return new ClassPathItem(directory, f);
 	}
 
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException("JarIterator doesn't support remove");
 	}
 
	private static class ClassFileFilter implements FileFilter {
 
 		@Override
 		public boolean accept(File f) {
 			return f.isDirectory() || f.getPath().endsWith(".class");
 		}
 	}
 }
