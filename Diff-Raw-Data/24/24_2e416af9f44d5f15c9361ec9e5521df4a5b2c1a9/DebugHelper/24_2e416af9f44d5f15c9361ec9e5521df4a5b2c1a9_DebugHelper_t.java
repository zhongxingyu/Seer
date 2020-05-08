 /*
  * Copyright (C) 2010 Daniel Jacobi
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package de.questmaster.fatremote;
 
 import android.os.Build;
 
 /**
  * Helper class to define global debug flags.
  * 
  * @author daniel
  *
  */
 public final class DebugHelper {
 
 	public static final boolean ON_EMULATOR = Build.PRODUCT.contains("sdk");
 	
 	public static final boolean SHOW_DEBUG_SCREEN = BuildConfig.DEBUG;
 		
	private DebugHelper() {
		throw new AssertionError();
	}
 }
