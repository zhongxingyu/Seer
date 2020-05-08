 /**
  * Copyright 2013 Ben Navetta <ben.navetta@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.bennavetta.appsite2.filesystem.util;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import com.google.appengine.repackaged.com.google.common.base.Splitter;
 import com.google.appengine.repackaged.com.google.common.collect.Iterables;
 
 /**
  * Utility methods for manipulating paths.
  * @author ben
  *
  */
 public class PathUtils
 {
 	/**
 	 * The path separator character used by file system paths.
 	 * Value: {@value}
 	 */
 	public static final String SEPARATOR = "/";
 	
 	/**
 	 * Error message if a path passed to one of these methods is {@code null}.
 	 */
 	private static final String NULL_PATH_MSG = "Path must not be null";
 	
 	/**
 	 * An internal constructor to prevent instantiation.
 	 */
 	private PathUtils() {}
 	
 	/**
 	 * Ensure that a directory path ends with a trailing slash.
 	 * @param path the path string (cannot be {@code null})
 	 * @return the path, ending with a slash
 	 */
 	public static final String directoryPath(final String path)
 	{
 		checkNotNull(path, NULL_PATH_MSG);
 		return path.endsWith(SEPARATOR) ? path : path.concat(SEPARATOR);
 	}
 	
 	/**
 	 * Get the last component in a path string. For example, the last path component of {@code /foo/bar/baz.txt} is {@code baz.txt}. 
 	 * @param path the path string (cannot be {@code null})
 	 * @return the last component in the path
 	 */
 	public static final String lastPathComponent(final String path)
 	{
 		return Iterables.getLast(Splitter.on(SEPARATOR).split(checkNotNull(path, NULL_PATH_MSG)));
 	}
 	
 	/**
 	 * Remove the last component of a path. For example, this method would return {@code /foo/bar} given {@code /foo/bar/baz.txt}.
 	 * @param path the path string (cannot be {@code null})
 	 * @return the path minus its last component
 	 */
 	public static final String withoutLastComponent(final String path)
 	{
		checkNotNull(path, NULL_PATH_MSG);
		if(path.equals(SEPARATOR))
		{
			return path;
		}
		int end = 0;
		if(path.endsWith(SEPARATOR))
		{
			end = path.lastIndexOf(SEPARATOR, path.length()-2);
		}
		else
		{
			end = path.lastIndexOf(SEPARATOR);
		}
		return path.substring(0, end);
 	}
 }
