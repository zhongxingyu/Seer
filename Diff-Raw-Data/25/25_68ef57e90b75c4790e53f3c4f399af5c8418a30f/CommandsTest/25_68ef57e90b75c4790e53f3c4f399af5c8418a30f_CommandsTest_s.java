 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
  *
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.command;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 @SuppressWarnings("nls")
 public class CommandsTest {
 	public final static String[] SPLIT_CALL_ARGS = new String[] {
 			"TestCommands/split",
 			"portfolio=Regulatory Reporting,Non-Regulatory-Reporting",
 			"destination=folder/subfolder",
 			"stress=23,43,53" };
 
 	public final static String[] DELETE_CALL_ARGS = new String[] {
 			"deleteProcessed",
 			"destination=folder/subfolder",
 			"destination=anotherFolder/something" };
 
 	public final static String[] LIST_FILES_ARGS = new String[] {
 			"listFiles",
			"input=*" };
 
 	@Test
 	public void testArgsParsin() {
		CommandCall split = CommandCall.create(CommandsTest.SPLIT_CALL_ARGS);
		CommandCall delete = CommandCall.create(CommandsTest.DELETE_CALL_ARGS);
 		System.out.println("COMMAND> " + split);
 		System.out.println("COMMAND> " + delete);
 	}
 
 	@Test
 	public void testCommandExecution() throws IOException {
		CommandExecutor e = new CommandExecutorBuilder().add(CommandClassExample.class).build();
 		e.printCommand(System.out);
		CommandCall split = CommandCall.create(CommandsTest.SPLIT_CALL_ARGS);
		CommandCall delete = CommandCall.create(CommandsTest.DELETE_CALL_ARGS);
		CommandCall listFiles = CommandCall.create(CommandsTest.LIST_FILES_ARGS);
 		e.execute(split);
 		e.execute(delete);
 		e.execute(listFiles);
 	}
 
 }
