 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 
 package de.weltraumschaf.juberblog;
 
 import de.weltraumschaf.commons.ApplicationException;
 import de.weltraumschaf.commons.IO;
 import de.weltraumschaf.commons.system.ExitCode;
 import de.weltraumschaf.juberblog.cmd.SubCommands;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 import org.junit.Rule;
 import org.junit.rules.ExpectedException;
 import static org.mockito.Mockito.*;
 
 /**
  * Tests for {@link App}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class AppTest {
 
     @Rule
     //CHECKSTYLE:OFF
     public final ExpectedException thrown = ExpectedException.none();
     //CHECKSTYLE:ON
 
     private App createSut() {
         return createSut(new String[] {});
     }
 
     private App createSut(final String ... args) {
         final App app = new App(args);
         app.setIoStreams(mock(IO.class));
         return app;
     }
 
     @Test public void isDebug_returnFalseByDefault() {
         assertThat(createSut().isDebug(), is(false));
     }
 
     @Test
     public void createSubcommand_throwsExceptionIfNull() throws ApplicationException {
         thrown.expect(NullPointerException.class);
         createSut().createSubcommand(null);
     }
 
     @Test
     public void createSubcommand_throwsExceptionIfUnknown() throws ApplicationException {
         try {
             createSut().createSubcommand(SubCommands.NOT_IMPLEMENTED);
             fail("Expected exception not thrown!");
        } catch (final ApplicationException ex) {
             assertThat(ex.getExitCode(), is((ExitCode) ExitCodeImpl.UNKNOWN_COMMAND));
             assertThat(ex.getMessage(), is(equalTo("Unknown command type 'not-implemented'!")));
         }
     }
 
     @Test
     public void createSubcommand() throws ApplicationException {
         final App sut = createSut();
         assertThat(sut.createSubcommand(SubCommands.CREATE), is(not(nullValue())));
         assertThat(sut.createSubcommand(SubCommands.INSTALL), is(not(nullValue())));
         assertThat(sut.createSubcommand(SubCommands.PUBLISH), is(not(nullValue())));
     }
 
     @Test
     public void validateArguments_emptyArgs() {
         final App sut = createSut();
         try {
             sut.validateArguments();
             fail("Expected exception not thrown!");
         } catch (final ApplicationException ex) {
             assertThat(ex.getExitCode(), is((ExitCode) ExitCodeImpl.TOO_FEW_ARGUMENTS));
             assertThat(ex.getMessage(), is(equalTo("No sub comamnd given!"
                     + Constants.DEFAULT_NEW_LINE
                     + "Usage: juberblog create|publish|install")));
         }
     }
 
     @Test
     public void validateArguments_firstArgIsEmpty() {
         final App sut = createSut("");
 
         try {
             sut.validateArguments();
             fail("Expected exception not thrown!");
         } catch (final ApplicationException ex) {
             assertThat(ex.getExitCode(), is((ExitCode) ExitCodeImpl.TOO_FEW_ARGUMENTS));
             assertThat(ex.getMessage(), is(equalTo("No sub comamnd given!"
                     + Constants.DEFAULT_NEW_LINE
                     + "Usage: juberblog create|publish|install")));
         }
     }
 
     @Test
     public void validateArguments_allArgsAreEmpty() {
         final App sut = createSut("", "", "");
 
         try {
             sut.validateArguments();
             fail("Expected exception not thrown!");
         } catch (final ApplicationException ex) {
             assertThat(ex.getExitCode(), is((ExitCode) ExitCodeImpl.TOO_FEW_ARGUMENTS));
             assertThat(ex.getMessage(), is(equalTo("No sub comamnd given!"
                     + Constants.DEFAULT_NEW_LINE
                     + "Usage: juberblog create|publish|install")));
         }
     }
 
     @Test
     public void validateArguments_oneArgument() throws ApplicationException {
         final App sut = createSut("foo");
         final Arguments args = sut.validateArguments();
         assertThat(args, is(not(nullValue())));
         assertThat(args.getFirstArgument(), is(equalTo("foo")));
         assertThat(args.getTailArguments(), is(new String[] {}));
     }
 
     @Test
     public void validateArguments_multipleArggs() throws ApplicationException {
         final App sut = createSut("foo", "bar", "baz");
         final Arguments args = sut.validateArguments();
         assertThat(args, is(not(nullValue())));
         assertThat(args.getFirstArgument(), is(equalTo("foo")));
         assertThat(args.getTailArguments(), is(new String[] {"bar", "baz"}));
     }
 }
