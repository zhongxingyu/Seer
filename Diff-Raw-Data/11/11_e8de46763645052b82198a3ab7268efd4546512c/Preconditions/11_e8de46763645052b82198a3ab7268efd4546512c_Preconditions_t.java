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
 package me.footlights.core;
 
 
 /**
  * Utility class for expressing preconditions.
  * @author Jonathan Anderson (jon@footlights.me)
  */
 public class Preconditions
 {
 	/**
 	 * Checks that the supplied references are non-null.
 	 * @throws IllegalArgumentException if any supplied reference is null
 	 */
 	public static void notNull(Object... o) throws IllegalArgumentException
 	{
 		for (int i = 0; i < o.length; i++)
 			if (o[i] == null)
				throw new NullPointerException(
					"Precondition failed: argument " + i + " is null");
 	}
 
 	/** Non-instantiable utility class. */
 	private Preconditions() {}
 }
