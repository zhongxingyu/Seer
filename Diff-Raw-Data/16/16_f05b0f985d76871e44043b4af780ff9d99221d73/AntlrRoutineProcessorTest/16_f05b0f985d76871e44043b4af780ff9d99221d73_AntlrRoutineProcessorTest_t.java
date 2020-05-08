 /**
  * Copyright (C) 2013 Chris Barthauer <mumpsanalyzer@gmail.com>
  *
  * This file is part of MumpsAnalyzer.
  *
  * MumpsAnalyzer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MumpsAnalyzer is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MumpsAnalyzer.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Contributors:
  *     Chris Barthauer - Initial API and implementation.
  */
 package analyzer;
 
 import antlr.AntlrRoutineProcessorBuilder;
 import listener.MetricListener;
 import listener.LexerErrorListener;
 import listener.InMemoryLexerErrorListener;
 import java.util.List;
 import listener.InMemoryParserErrorListener;
 import listener.ParserErrorListener;
 import static org.hamcrest.Matchers.*;
 import org.jmock.Expectations;
 import org.jmock.integration.junit4.JUnitRuleMockery;
 import org.junit.Test;
 import org.junit.Rule;
 import static org.junit.Assert.*;
 
 public class AntlrRoutineProcessorTest {
     @Rule
     public JUnitRuleMockery context = new JUnitRuleMockery();
     
     @Test
     public void mockCountLines() {
         final MumpsRoutine routine = context.mock(MumpsRoutine.class);
         final MetricListener listener = context.mock(MetricListener.class);
         
         final AntlrRoutineProcessorBuilder builder = 
                 new AntlrRoutineProcessorBuilder();
         final RoutineProcessor processor = builder.setMetricListeners(listener)
                 .build();
         
         context.checking(new Expectations() {{
             oneOf(routine).asString();
             will(returnValue("HELLO ; This is a comment.\n"));
             oneOf(listener).asMListener();
             oneOf(listener).getMetric();
             will(returnValue(Metric.LOC));
             oneOf(listener).getValue();
             will(returnValue(1));
             oneOf(routine).identifier();
             will(returnValue("HELLO.m"));
             oneOf(listener).reset();
         }});
         
         processor.process(routine);
     }
     
     @Test
     public void shouldLogLexerErrors() {
         final String unicodeSigmaCharacter = "\u03A3";
         final String routineIdentifier = "HELLO";
         MumpsRoutine routine = new StringBasedMumpsRoutine(
                 routineIdentifier, "HELLO " + unicodeSigmaCharacter + "\n");
         LexerErrorListener errorListener = 
                 new InMemoryLexerErrorListener();
         AntlrRoutineProcessorBuilder builder = new AntlrRoutineProcessorBuilder();
         RoutineProcessor processor = builder.setLexerErrorListener(errorListener)
                 .build();
         
         processor.process(routine); 
         List<MumpsSyntaxError> errors = processor.syntaxErrors();
         
         assertThat(errors.size(), greaterThan(0));
         assertThat(
                 errors.get(0).getMessage(), 
                 equalTo("token recognition error at: '" 
                     + unicodeSigmaCharacter + "'"));
         assertThat(
                 errors.get(0).getLine(),
                 equalTo(1));
         assertThat(
                 errors.get(0).getCharPositionInLine(),
                 equalTo(6));
         assertThat(
                 errors.get(0).getIdentifier(),
                 equalTo(routineIdentifier));
     }
         
     @Test
     public void shouldLogParserErrors() {
         //Parser error: Double quote after S command.
         final MumpsRoutine routine = new StringBasedMumpsRoutine(
                 "HELLO",
                 "HELLO\n"
                     + " SET \"\n");
         final ParserErrorListener errorListener = new InMemoryParserErrorListener();
         final AntlrRoutineProcessorBuilder builder = 
                 new AntlrRoutineProcessorBuilder();
         final RoutineProcessor processor = 
                 builder.setParserErrorListener(errorListener)
                     .build();
         processor.process(routine);
         List<MumpsSyntaxError> errors = processor.syntaxErrors();
         assertThat(errors.size(), greaterThan(0));
         assertThat(
                 errors.get(0).getMessage(),
                 equalTo("no viable alternative at input ' \"'"));
         assertThat(
                 errors.get(0).getLine(),
                 equalTo(2));
         assertThat(
                 errors.get(0).getCharPositionInLine(),
                 equalTo(5));
         assertThat(
                 errors.get(0).getIdentifier(),
                 equalTo("HELLO"));
     }
     
     private final class StringBasedMumpsRoutine implements MumpsRoutine {
         private final String identifier;
         private final String source;
 
         private StringBasedMumpsRoutine(String identifier, String source) {
             this.identifier = identifier;
             this.source = source;
         }        
         
         @Override
         public final String asString() {
             return source;
         }
 
         @Override
         public final String identifier() {
             return identifier;
         }        
     }
 }
