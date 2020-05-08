 /*
  * Copyright 2011 Jonathan Anderson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package me.footlights.api;
 
 import java.net.URI;
 import java.nio.ByteBuffer;
 
 import scala.Either;
 import scala.Option;
 
 
 /** An application's interface to the Footlights core. */
 public interface KernelInterface
 {
 	/**
 	 * Save data to a logical file.
 	 */
 	public Either<Exception,File> save(ByteBuffer data);
 
 	/** Open a file by its URN. */
 	public Either<Exception,File> open(URI name);
 
 	/**
 	 * Open a file using a hierarchical directory namespace.
 	 *
 	 * The name given can be rooted in either a URN (e.g. "urn:foo/some/path/to/file") or an
 	 * app-specific root (e.g. "/my/path/to/a/file").
 	 */
 	public Either<Exception,File> open(String name);
 
 	/**
 	 * Open a mutable directory.
 	 *
 	 * A mutable directory is actually a wrapper around an immutable construct, but the details
 	 * are hidden from applications.
 	 *
 	 * An application that wants a hierarchical directory structure can start with a call to
 	 * kernel.openDirectory("/"), which is the "virtual root" for the application.
 	 */
 	public Either<Exception,Directory> openDirectory(String name);
 
 	/** Open a file on the local machine (e.g. a photo to upload). */
 	public Either<Exception,File> openLocalFile();
 
 	/** Save data into a local file. */
 	public Either<Exception,File> saveLocalFile(File file);
 
 	/**
 	 * Ask the user a question.
 	 *
 	 * The mechanism (e.g. pop-up vs. more gentle prompt) is undefined, but this method is
 	 * synchronous.
 	 */
 	public Either<Exception,String> promptUser(String prompt, Option<String> defaultValue);
 
 	/** Convenience method with no default value. */
 	public Either<Exception,String> promptUser(String prompt);
 
 	/** Share a {@link Directory} with another user. */
 	public Either<Exception,Directory> share(Directory dir);
 
	/** Open a directory with another app. */
 	public Either<Exception,Directory> openWithApplication(Directory dir);
 }
