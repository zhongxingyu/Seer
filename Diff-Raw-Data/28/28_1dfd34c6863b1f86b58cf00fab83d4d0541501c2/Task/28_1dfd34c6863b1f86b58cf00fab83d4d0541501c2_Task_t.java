 // vim:filetype=java:ts=4
 /*
 	Copyright (c) 2005, 2006, 2007
 	Conor McDermottroe.  All rights reserved.
 
 	Redistribution and use in source and binary forms, with or without
 	modification, are permitted provided that the following conditions
 	are met:
 	1. Redistributions of source code must retain the above copyright
 	   notice, this list of conditions and the following disclaimer.
 	2. Redistributions in binary form must reproduce the above copyright
 	   notice, this list of conditions and the following disclaimer in the
 	   documentation and/or other materials provided with the distribution.
 	3. Neither the name of the author nor the names of any contributors to
 	   the software may be used to endorse or promote products derived from
 	   this software without specific prior written permission.
 
 	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 	"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 	LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 	A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 	HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 	TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 	OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 	OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package com.mcdermottroe.exemplar.ui.ant;
 
 import org.apache.tools.ant.BuildException;
 
 import com.mcdermottroe.exemplar.Copyable;
 import com.mcdermottroe.exemplar.DBC;
 import com.mcdermottroe.exemplar.input.InputException;
 import com.mcdermottroe.exemplar.input.InputUtils;
 import com.mcdermottroe.exemplar.input.ParserException;
 import com.mcdermottroe.exemplar.model.XMLDocumentType;
 import com.mcdermottroe.exemplar.output.OutputException;
 import com.mcdermottroe.exemplar.output.OutputUtils;
 import com.mcdermottroe.exemplar.ui.Log;
 import com.mcdermottroe.exemplar.ui.Message;
 import com.mcdermottroe.exemplar.ui.MessageException;
 import com.mcdermottroe.exemplar.ui.Options;
 import com.mcdermottroe.exemplar.utils.Timer;
 
 import static com.mcdermottroe.exemplar.Constants.Character.SPACE;
 import static com.mcdermottroe.exemplar.Constants.EOL;
 
 /** The Ant task "UI" for Exemplar.
 
 	@author	Conor McDermottroe
 	@since	0.1
 */
 public class Task
 extends org.apache.tools.ant.Task
 implements Comparable<Task>, Copyable<Task>
 {
 	/** The execute method required by the Task. */
 	@Override public void execute() {
 		super.execute();
 
 		// Make a logger which will get picked up by the rest of the program.
 		Log.registerHandler(new TaskLogHandler(this));
 
 		// Localise the program, if possible.
 		try {
 			Message.localise();
 		} catch (MessageException e) {
 			Log.error(e, Message.ANT_LOCALISATION_ERROR());
 			return;
 		}
 
 		// All of the options should be set by now.
 		Options.setUIFinished();
 
 		// Make a timer for the task
 		Timer taskTimer = new Timer();
 
 		// Ant needs a non-empty String to make a blank line - lame.
 		String blankLine = Character.toString(SPACE);
 
 		// Print a header for the build
 		for (String line : Message.COPYRIGHT().split(EOL)) {
 			Log.info(line);
 		}
 
 		// Make sure that the mandatory options have been set
 		if (!Options.allMandatoryOptionsSet()) {
 			throw new BuildException(Message.MANDATORY_OPTIONS_NOT_SET());
 		}
 
 		// Get all of the options
 		Log.info(Message.UI_PROGRESS_OPTIONS());
 		String inputType = Options.getString("input-type");
 		DBC.ASSERT(inputType != null);
 		String input = Options.getString("input");
 		DBC.ASSERT(input != null);
 		String outputApi = Options.getString("output-api");
 		String outputLanguage = Options.getString("output-language");
 		DBC.ASSERT(outputLanguage != null);
 		String output = Options.getString("output");
 		DBC.ASSERT(output != null);
 
 		// Parse the input
 		Log.info(Message.UI_PROGRESS_PARSING_INPUT(input));
 		XMLDocumentType doctype;
 		try {
 			doctype = InputUtils.parse(input, inputType);
 		} catch (InputException e) {
 			throw new BuildException(
 				Message.UI_PROGRESS_INPUT_PARSE_FAILED(),
 				e
 			);
 		} catch (ParserException e) {
 			throw new BuildException(
 				Message.UI_PROGRESS_INPUT_PARSE_FAILED(),
 				e
 			);
 		}
 
 		// Generate the output
 		Log.info(Message.UI_PROGRESS_GENERATING_PARSER());
 		try {
 			OutputUtils.generateParser(
 				doctype,
 				output,
 				outputLanguage,
 				outputApi
 			);
 		} catch (OutputException e) {
 			throw new BuildException(
 				Message.UI_PROGRESS_FAILED_TO_CREATE_OUTPUT(),
 				e
 			);
 		}
 		Log.info(Message.UI_PROGRESS_DONE());
 
 		// Say that it's finished
 		Log.info(blankLine);
 		Log.info(
 			Message.UI_PROGRESS_FINISHED_TIME(taskTimer.getElapsedSeconds())
 		);
 	}
 
 	/** Setter for the debug attribute of the task.
 
 		@param	debug	Whether or not debugging is turned on.
 	*/
 	public static void setDebug(String debug) {
 		Options.set("debug", debug);
 	}
 
 	/** Setter for the debug-level attribute of the task.
 
 		@param	debugLevel	The debug level to set.
 	*/
 	public static void setDebug_level(String debugLevel) {
 		Options.set("debug-level", debugLevel);
 	}
 
 	/** Setter for the exclude attribute of the task.
 
 		@param	exclude	This is the string value of the exclude attribute.
 	*/
 	public static void setExclude(String exclude) {
 		Options.set("exclude", exclude);
 	}
 
 	/** Setter for the help attribute of the task.
 
 		@param	help	This is the string value of the help attribute.
 	*/
 	public static void setHelp(String help) {
 		Options.set("help", help);
 	}
 
 	/** Setter for the include attribute of the task.
 
 		@param	include	This is the string value of the include attribute.
 	*/
 	public static void setInclude(String include) {
 		Options.set("include", include);
 	}
 
 	/** Setter for the input attribute of the task.
 
 		@param	input	This is the string value of the input attribute.
 	*/
 	public static void setInput(String input) {
 		Options.set("input", input);
 	}
 
	/** Setter for the input_encoding attribute of the task.

		@param	inputEncoding	This is the string value of the input_encoding
								attribute.
	*/
	public static void setInput_encoding(String inputEncoding) {
		Options.set("input-encoding", inputEncoding);
	}

 	/** Setter for the input_type attribute of the task.
 
 		@param	inputType	This is the string value of the input_type
 							attribute.
 	*/
 	public static void setInput_type(String inputType) {
 		Options.set("input-type", inputType);
 	}
 
 	/** Setter for the output attribute of the task.
 
 		@param	output	This is the string value of the output attribute.
 	*/
 	public static void setOutput(String output) {
 		Options.set("output", output);
 	}
 
 	/** Setter for the output_api attribute of the task.
 
 		@param	outputAPI	This is the string value of the output_api
 							attribute.
 	*/
 	public static void setOutput_api(String outputAPI) {
 		Options.set("output-api", outputAPI);
 	}
 
 	/** Setter for the output_language attribute of the task.
 
 		@param	outputLanguage	This is the string value of the output_language
 								attribute.
 	*/
 	public static void setOutput_language(String outputLanguage) {
 		Options.set("output-language", outputLanguage);
 	}
 
 	/** Setter for the output_package attribute of the task.
 
 		@param	outputPackage	This is the string value of the output_package
 								attribute.
 	*/
 	public static void setOutput_package(String outputPackage) {
 		Options.set("output-package", outputPackage);
 	}
 
 	/** Setter for the verbose attribute of the task. This is actually a no-op
 		as the task is always verbose.
 
 		@param	verbose	Whether or not this {@link Task} should be verbose.
 	*/
 	public static void setVerbose(String verbose) {
 		Options.set("verbose", verbose);
 	}
 
 	/** Setter for the version attribute of the task.
 
 		@param	version	This is the string value of the version attribute.
 	*/
 	public static void setVersion(String version) {
 		Options.set("version", version);
 	}
 
 	/** Setter for the vocabulary attribute of the task.
 
 		@param	vocabulary	This is the string value of the vocabulary
 							attribute.
 	*/
 	public static void setVocabulary(String vocabulary) {
 		Options.set("vocabulary", vocabulary);
 	}
 
 	/** Implement {@link Comparable#compareTo(Object)}.
 		
 		@param	other	The {@link Task} to compare with.
 		@return			A result as defined by {@link
 						Comparable#compareTo(Object)}.
 	*/
 	public int compareTo(Task other) {
 		if (other == null) {
 			throw new NullPointerException();
 		}
 		return 0;
 	}
 
 	/** {@inheritDoc} */
 	public Task getCopy() {
 		return new Task();
 	}
 
 	/** See {@link Object#equals(Object)}.
 
 		@param	o	The object to compare against.
 		@return		True if <code>this</code> is equal to <code>o</code>.
 	*/
 	@Override public boolean equals(Object o) {
 		if (this == o) {
 			return true;
 		}
 		if (o == null) {
 			return false;
 		}
 		return o instanceof Task;
 	}
 
 	/** See {@link Object#hashCode()}.
 
 		@return	A hash code.
 	*/
 	@Override public int hashCode() {
 		return 0;
 	}
 
 	/** See {@link Object#toString()}.
 
 		@return	A descriptive {@link String} for this {@link Task}.
 	*/
 	@Override public String toString() {
 		return getClass().getName();
 	}
 }
