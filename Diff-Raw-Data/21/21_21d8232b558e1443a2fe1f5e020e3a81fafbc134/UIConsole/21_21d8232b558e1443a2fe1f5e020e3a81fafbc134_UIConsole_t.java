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
 
 package com.sangupta.consoles.ui;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 
 import com.sangupta.consoles.IConsole;
 import com.sangupta.consoles.core.ConsoleInputStream;
 import com.sangupta.consoles.core.ConsoleWriter;
import com.sangupta.consoles.core.InputKey;
 
 /**
  * An implementation of the UI console. Mimicks the default shell-based consoles
  * as provided in operating systems such as Windows, Mac and Linux.
  * 
  * @author sangupta
  *
  */
 public class UIConsole implements IConsole {
 	
 	protected final ConsoleWriter consoleWriter;
 	
 	protected final ConsoleInputStream consoleInputStream;
 	
 	/**
 	 * Reference to the internal {@link SwingTerminal} instance.
 	 * 
 	 */
 	protected final SwingTerminal terminal;
 	
 	/**
 	 * Default constructor
 	 */
 	public UIConsole() {
 		this.terminal = new SwingTerminal();
 		this.consoleWriter = new ConsoleWriter(this);
 		this.consoleInputStream = new ConsoleInputStream(this);
 	}
 
 	/**
 	 * Clear the screen
 	 */
 	@Override
 	public void clearScreen() {
 		this.terminal.clearTerminal();
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public void print(String string) {
 		this.terminal.writeString(string);
 	}
 
 	@Override
 	public void print(char[] cbuf, int off, int len) {
 		this.terminal.write(cbuf, off, len);
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public void println(String string) {
 		this.terminal.write(string);
 		this.terminal.write("\n");
 		this.terminal.refresh();
 	}
 	
 	/**
 	 * 
 	 */
 	@Override
 	public void print(char ch) {
 		this.terminal.writeChar(ch);
 	}
 	
 	/**
 	 * 
 	 */
 	@Override
 	public char readChar() {
		InputKey key = this.terminal.getKey();
		if(key != null) {
			return key.ch;
		}
		
		return (char) 0;
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public String readLine() {
 		return this.terminal.readString(true, (char) 0);
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public char[] readPassword() throws IOException {
 		return this.terminal.readString(false, (char) 0).toCharArray();
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public char[] readPassword(char mask) throws IOException {
 		return this.terminal.readString(false, mask).toCharArray();
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public void setWindowTitle(String title) {
 		this.terminal.setTitle(title);
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public void shutdown() {
 		this.terminal.closeTerminal();
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public Writer getOutputStream() {
 		return this.consoleWriter;
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public InputStream getInputStream() {
 		return this.consoleInputStream;
 	}
 
 	/**
 	 * 
 	 */
 	public void flush() throws IOException {
 		this.consoleWriter.flush();
 	}
 
 }
