 /*
  * #%L
  * LabelUtils
  * %%
  * Copyright (C) 2012 INRIA
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 package org.praat;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.nio.charset.Charset;
 import java.text.NumberFormat;
 import java.util.Locale;
 
 import jregex.Matcher;
 import jregex.Pattern;
 
 import com.google.common.base.Strings;
 import com.google.common.io.Files;
 
 public class PraatTextFile extends PraatFile {
 
 	private BufferedReader reader;
 
 	protected BufferedWriter writer;
 	private String eol;
 	protected NumberFormat number;
 	final private int tabSize = 4;
 	private int indent = 0;
 
 	private static Pattern STRING_PATTERN = new Pattern("\"({target}.*)\"");
 	private static Pattern INTEGER_PATTERN = new Pattern("(?!\\[)({target}\\d+)(?!\\])");
 	private static Pattern DOUBLE_PATTERN = new Pattern("(?!\\[)({target}\\d+(\\.\\d+)?)(?![\\d\\]])");
 
 	public PraatTextFile() {
 		// TODO Auto-generated constructor stub
 	}
 
 	public PraatTextFile(File file, Charset charset, EOL eol) throws IOException {
 		writer = Files.newWriter(file, charset);
 		this.eol = eol.toString();
 		number = NumberFormat.getInstance(Locale.US);
		number.setMaximumFractionDigits(32);
 		writer.write("File type = \"ooTextFile\"");
 		writeLine();
 	}
 
 	public PraatObject read(File file) throws Exception {
 		return read(file, Charset.defaultCharset());
 	}
 
 	public PraatObject read(File file, Charset charset) throws Exception {
 		reader = Files.newReader(file, charset);
 		reader.readLine(); // discard first line
 
 		// determine payload class (and ignore missing name)
 		String className = readString();
 		return readPayload(className);
 	}
 
 	@Override
 	public PraatObject readPayLoad() throws Exception {
 		// determine payload class
 		String className = readString();
 
 		// read name
 		String name = readString();
 
 		// read payload
 		PraatObject object = readPayload(className);
 		object.setName(name);
 		return object;
 	}
 
 	private PraatObject readPayload(String className) throws Exception {
 		// use reflection to create payload instance
 		String packageName = getClass().getPackage().getName();
 		String fullyQuallifiedClassString = packageName + "." + className;
 		Class<?> praatClass;
 		try {
 			praatClass = Class.forName(fullyQuallifiedClassString);
 		} catch (ClassNotFoundException e) {
 			throw new ClassNotFoundException("Unsupported Praat class: " + className);
 		}
 		Class<?> superClass = getClass().getSuperclass();
 		Constructor<?> constructor = praatClass.getConstructor(superClass);
 		PraatObject payload;
 		try {
 			payload = (PraatObject) constructor.newInstance(this);
 		} catch (ClassCastException e) {
 			throw new ClassCastException("Could not cast " + praatClass + " to PraatObject");
 		}
 		return payload;
 	}
 
 	@Override
 	public String readString() throws IOException {
 		// get string from buffer
 		String target;
 		try {
 			target = readPattern(STRING_PATTERN);
 		} catch (IOException e) {
 			throw new IOException("Could not read from buffer");
 		} catch (IllegalArgumentException e) {
 			throw new IllegalArgumentException("Could not read string from buffer");
 		}
 		return target;
 	}
 
 	@Override
 	public int readInteger() throws IOException {
 		// get integer string from buffer
 		String target = null;
 		try {
 			target = readPattern(INTEGER_PATTERN);
 		} catch (IOException e) {
 			throw new IOException("Could not read from buffer");
 		} catch (IllegalArgumentException e) {
 			throw new IllegalArgumentException("Could not read integer from buffer");
 		}
 
 		// parse integer
 		int targetInt;
 		try {
 			targetInt = Integer.parseInt(target);
 		} catch (NumberFormatException e) {
 			throw new NumberFormatException("Could not parse integer from string: " + target);
 		}
 		return targetInt;
 	}
 
 	@Override
 	public double readDouble() throws IOException {
 		// get double string from buffer
 		String target = null;
 		try {
 			target = readPattern(DOUBLE_PATTERN);
 		} catch (IOException e) {
 			throw new IOException("Could not read from buffer");
 		} catch (IllegalArgumentException e) {
 			throw new IllegalArgumentException("Could not read double from buffer");
 		}
 
 		// parse double
 		double targetDouble;
 		try {
 			targetDouble = Double.parseDouble(target);
 		} catch (NumberFormatException e) {
 			throw new NumberFormatException("Could not parse double from string: " + target);
 		}
 		return targetDouble;
 	}
 
 	private String readPattern(Pattern pattern) throws IOException {
 		String line;
 		// keep reading line from BufferedReader until we match
 		while ((line = reader.readLine()) != null) {
 
 			// match pattern to line
 			Matcher matcher = pattern.matcher(line);
 
 			// find pattern in line
 			if (!matcher.find()) {
 				continue; // read next line if pattern not found
 			}
 
 			// get match target
 			String target = null;
 			try {
 				target = matcher.group("target");
 			} catch (IllegalArgumentException e) {
 				throw new IllegalArgumentException("Pattern /" + pattern + "/ does not contain required group");
 			}
 			return target;
 		}
 		throw new IllegalArgumentException("End of buffer reached without finding pattern /" + pattern + "/");
 	}
 
 	public void write(PraatObject object) throws IOException {
 		writer.write(String.format("Object class = \"%s\"", object.getClass().getSimpleName()));
 		writeLine();
 		writeLine();
 		writePayLoad(object);
 		writer.flush();
 		writer.close();
 	}
 
 	public void writePayLoad(PraatObject object) throws IOException {
 		object.write(this);
 	}
 
 	@Override
 	public void writeString(String decorator, String value) throws IOException {
 		writeLine("%s \"%s\" ", decorator, value);
 	}
 
 	@Override
 	public void writeInteger(String decorator, int value) throws IOException {
 		writeLine("%s %d ", decorator, value);
 	}
 
 	@Override
 	public void writeDouble(String decorator, double value) throws IOException {
 		writeLine("%s %s ", decorator, number.format(value));
 	}
 
 	@Override
 	public void writeLine(String format, Object... args) throws IOException {
 		writer.write(Strings.repeat(" ", tabSize * indent));
 		writer.write(String.format(Locale.US, format, args));
 		writeLine();
 	}
 
 	public void writeLine() throws IOException {
 		writer.write(eol);
 	}
 
 	@Override
 	public void increaseIndent() {
 		indent++;
 	}
 
 	@Override
 	public void decreaseIndent() {
 		indent--;
 	}
 
 	/**
 	 * Constants for end-of-line (EOL) encoding, named for their prevalent operating system.
 	 * 
 	 * @author ingmar
 	 * 
 	 */
 	public enum EOL {
 
 		/**
 		 * CRLF
 		 */
 		WINDOWS {
 			@Override
 			public String toString() {
 				return "\r\n";
 			}
 		},
 
 		/**
 		 * LF
 		 */
 		UNIX {
 			@Override
 			public String toString() {
 				return "\n";
 			}
 		},
 
 		/**
 		 * CR
 		 */
 		MAC {
 			@Override
 			public String toString() {
 				return "\r";
 			}
 		};
 
 	}
 
 }
