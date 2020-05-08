 /**
  *
  * consoles - Java based console terminals
  * Copyright (c) 2013, Sandeep Gupta
  * 
  * http://www.sangupta/projects/consoles
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.sangupta.consoles.core;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.sangupta.consoles.IConsole;
 
 /**
  * An implementation of the {@link InputStream} that works with any
  * implementation of the {@link IConsole}.
  * 
  * @author sangupta
  *
  */
 public class ConsoleInputStream extends InputStream {
 	
 	protected IConsole console;
 	
 	public ConsoleInputStream(IConsole console) {
 		this.console = console;
 	}
 
 	@Override
 	public int read() throws IOException {
		char ch = this.console.readChar();
		if(ch == 13) {
			ch = '\n';
		}
		this.console.print(ch);
		return ch;
 	}
 
 }
