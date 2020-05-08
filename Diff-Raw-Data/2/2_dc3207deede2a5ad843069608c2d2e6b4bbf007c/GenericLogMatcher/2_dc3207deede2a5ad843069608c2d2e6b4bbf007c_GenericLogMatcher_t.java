 /**
  * Copyright (C) 2013
  *   Michael Mosmann <michael@mosmann.de>
  *
  * with contributions from
  * 	${lic.developers}
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.flapdoodle.logparser.matcher.generic;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import de.flapdoodle.logparser.GenericStreamProcessor;
 import de.flapdoodle.logparser.IMatch;
 import de.flapdoodle.logparser.IMatcher;
 import de.flapdoodle.logparser.IReader;
 import de.flapdoodle.logparser.LogEntry;
 import de.flapdoodle.logparser.io.StringListReaderAdapter;
 import de.flapdoodle.logparser.io.WriteToListLineProcessor;
 import de.flapdoodle.logparser.matcher.stacktrace.StackTraceMatcher;
 import de.flapdoodle.logparser.regex.Patterns;
 import de.flapdoodle.logparser.stacktrace.StackTrace;
 import de.flapdoodle.logparser.streamlistener.OnceAndOnlyOnceStreamListener;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 /**
  * generic log matcher
  * <p/>
  *
  * @author mmosmann
  */
 public class GenericLogMatcher implements IMatcher<LogEntry> {
 
     private final ImmutableList<Pattern> firstLinesPatterns;
     private final Map<Pattern,Set<String>> names= Maps.newHashMap();
 
     public GenericLogMatcher(Pattern firstLine, Pattern... additionalLines) {
         this.firstLinesPatterns = ImmutableList.<Pattern>builder().add(firstLine).add(additionalLines).build();
         for (Pattern p : firstLinesPatterns) {
             names.put(p,Patterns.names(p));
         }
     }
 
     @Override
     public Optional<IMatch<LogEntry>> match(IReader reader) throws IOException {
         List<LineWithMatch> lines= Lists.newArrayList();
         for (Pattern p : firstLinesPatterns) {
             Optional<String> possibleLine = reader.nextLine();
             if (possibleLine.isPresent()) {
                 Optional<Map<String, String>> match = Patterns.match(p.matcher(possibleLine.get()),names.get(p));
                 if (match.isPresent()) {
                     lines.add(new LineWithMatch(possibleLine.get(),match.get()));
                 } else {
                     return Optional.absent();
                 }
             } else {
                 return Optional.absent();
             }
         }
         return Optional.<IMatch<LogEntry>>of(new Match(lines));
     }
 
     static class LineWithMatch {
         private final String line;
         private final ImmutableMap<String, String> attributes;
 
         public LineWithMatch(String line, Map<String, String> attributes) {
             this.line = line;
             this.attributes = ImmutableMap.copyOf(attributes);
         }
 
         public String line() {
             return line;
         }
 
         public ImmutableMap<String, String> attributes() {
             return attributes;
         }
     }
 
     static class Match implements IMatch<LogEntry> {
 
         private final ImmutableList<LineWithMatch> lines;
 
         public Match(List<LineWithMatch> lines) {
             this.lines = ImmutableList.copyOf(lines);
         }
 
         @Override
         public LogEntry process(List<String> lines) throws IOException {
             ImmutableList.Builder<String> builder = ImmutableList.<String>builder();
             builder.addAll(Lists.transform(this.lines, new Function<LineWithMatch, String>() {
                 @Override
                 public String apply(LineWithMatch input) {
                     return input.line();
                 }
             }));
             builder.addAll(lines);
             List<String> allLines= builder.build();
 
             List<Map<String, String>> attributes = Lists.transform(this.lines, new Function<LineWithMatch, Map<String, String>>() {
                 @Override
                 public Map<String, String> apply(LineWithMatch input) {
                     return input.attributes();
                 }
             });
 
             Optional<StackTrace> stackTrace = Optional.absent();
             List<String> messages = ImmutableList.of();
 
             if (!lines.isEmpty()) {
                 OnceAndOnlyOnceStreamListener<StackTrace> stackTraceListener = new OnceAndOnlyOnceStreamListener<StackTrace>();
                 WriteToListLineProcessor contentListener = new WriteToListLineProcessor();
 
                 GenericStreamProcessor<StackTrace> contentProcessor = new GenericStreamProcessor<StackTrace>(
                         Lists.<IMatcher<StackTrace>> newArrayList(new StackTraceMatcher()), contentListener, stackTraceListener);
                 try {
                     contentProcessor.process(new StringListReaderAdapter(lines));
                } catch (RuntimeException iax) {
                     System.out.println("-----------------------------------");
                     for (String line : lines) {
                         System.out.println(line);
                     }
                     System.out.println("-----------------------------------");
                     throw iax;
                 }
 
                 stackTrace = stackTraceListener.value();
                 messages = contentListener.lines();
             }
 
             return new LogEntry(allLines,LogEntry.join(attributes),stackTrace,messages);
         }
     }
 
 }
