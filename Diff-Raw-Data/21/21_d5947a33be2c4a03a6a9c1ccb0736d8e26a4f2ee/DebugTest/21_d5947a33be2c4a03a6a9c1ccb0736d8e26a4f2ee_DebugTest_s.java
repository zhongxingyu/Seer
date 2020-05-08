 /*
  * Copyright (c) 2006-2012 DMDirc Developers
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.debug;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
 import com.dmdirc.commandparser.commands.context.CommandContext;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.config.InvalidIdentityFileException;
import static com.dmdirc.harness.CommandArgsMatcher.eqLine;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 
 public class DebugTest {
 
     @BeforeClass
     public static void setUp() throws InvalidIdentityFileException {
         // Command has a dependency on CommandManager (to get the command char)
         // And CommandManager has a dependency on IdentityManager for the same
         IdentityManager.getIdentityManager().initialise();
        CommandManager.getCommandManager().initCommands();
     }
 
     /** Checks the debug command with no arguments shows usage. */
     @Test
     public void testNoArgs() {
         final CommandArguments arguments = mock(CommandArguments.class);
         final FrameContainer container = mock(FrameContainer.class);
 
         when(arguments.isCommand()).thenReturn(true);
         when(arguments.getArguments()).thenReturn(new String[0]);
 
         final Debug debug = new Debug(null);
 
         debug.execute(container, arguments, null);
         verify(container).addLine(eq("commandUsage"), anyObject(),
                 eq("debug"), anyObject());
     }
 
     /** Checks the debug command with an invalid subcommand shows an error. */
     @Test
     public void testInvalidArg() {
         final DebugPlugin plugin = mock(DebugPlugin.class);
         final CommandArguments arguments = mock(CommandArguments.class);
         final FrameContainer container = mock(FrameContainer.class);
 
         when(plugin.getCommand("test")).thenReturn(null);
         when(arguments.isCommand()).thenReturn(true);
         when(arguments.getArguments()).thenReturn(new String[]{"test"});
 
         final Debug debug = new Debug(plugin);
 
         debug.execute(container, arguments, null);
         verify(container).addLine(eq("commandError"), anyObject());
     }
 
     /** Checks the debug command executes a subcommand with no args. */
     @Test
     public void testCommandNoArgs() {
         final DebugPlugin plugin = mock(DebugPlugin.class);
         final CommandArguments arguments = mock(CommandArguments.class);
         final FrameContainer container = mock(FrameContainer.class);
         final DebugCommand command = mock(DebugCommand.class);
         final CommandContext context = mock(CommandContext.class);
 
         when(plugin.getCommand("test")).thenReturn(command);
         when(arguments.isCommand()).thenReturn(true);
         when(arguments.getArguments()).thenReturn(new String[]{"test"});
         when(arguments.getArgumentsAsString(1)).thenReturn("");
         when(command.getName()).thenReturn("test");
 
         final Debug debug = new Debug(plugin);
 
         debug.execute(container, arguments, context);
 
         verify(container, never()).addLine(anyString(), anyObject());
         verify(command).execute(same(container), eqLine("/test"), same(context));
     }
 
     /** Checks the debug command executes a subcommand with args. */
     @Test
     public void testCommandWithArgs() {
         final DebugPlugin plugin = mock(DebugPlugin.class);
         final CommandArguments arguments = mock(CommandArguments.class);
         final FrameContainer container = mock(FrameContainer.class);
         final DebugCommand command = mock(DebugCommand.class);
         final CommandContext context = mock(CommandContext.class);
 
         when(plugin.getCommand("test")).thenReturn(command);
         when(arguments.isCommand()).thenReturn(true);
         when(arguments.getArguments()).thenReturn(new String[]{"test", "1", "2", "3"});
         when(arguments.getArgumentsAsString(1)).thenReturn("1 2 3");
         when(command.getName()).thenReturn("test");
 
         final Debug debug = new Debug(plugin);
 
         debug.execute(container, arguments, context);
 
         verify(container, never()).addLine(anyString(), anyObject());
         verify(command).execute(same(container), eqLine("/test 1 2 3"), same(context));
     }
 
 }
