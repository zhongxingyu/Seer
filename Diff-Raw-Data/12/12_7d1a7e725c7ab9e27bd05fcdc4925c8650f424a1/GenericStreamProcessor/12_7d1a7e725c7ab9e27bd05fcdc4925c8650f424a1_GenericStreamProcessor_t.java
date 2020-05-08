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
 package de.flapdoodle.logparser;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.Lists;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 
 public class GenericStreamProcessor<T> {
 
 	private final Collection<IMatcher<T>> _matcher;
 	private final ILineProcessor _defaultLineProcessor;
 	private final IStreamListener<T> _listener;
 
    private static final int OFFSET = 1000;

    public GenericStreamProcessor(Collection<IMatcher<T>> matcher, ILineProcessor defaultLineProcessor,IStreamListener<T> listener) {
 		_matcher = matcher;
 		_defaultLineProcessor = defaultLineProcessor;
 		_listener = listener;
 	}
 
 	public void process(IRewindableReader reader) throws IOException {
 		boolean eof = false;
 
 		do {
 			Optional<IMatch<T>> match = firstMatch(reader);
 			if (match.isPresent()) {
 				process(reader, match);
 			} else {
 				Optional<String> nextLine = reader.nextLine();
 				if (nextLine.isPresent()) {
 					_defaultLineProcessor.processLine(nextLine.get());
 				} else {
 					eof = true;
 				}
 			}
 		} while (!eof);
 	}
 
 	private void process(IRewindableReader reader, Optional<IMatch<T>> match) throws IOException {
 		List<String> nonMatchingLines = Lists.newArrayList();
 
 		boolean readDone = false;
         int lines=0;
        int showNumberAt=lines+OFFSET;
 
 		do {
 			Optional<IMatch<T>> nextMatch = firstMatch(reader);
 			if (nextMatch.isPresent()) {
 				T result=match.get().process(nonMatchingLines);
 				_listener.entry(result);
 				nonMatchingLines = Lists.newArrayList();
 			} else {
 				Optional<String> nextLine = reader.nextLine();
 				if (nextLine.isPresent()) {
 					nonMatchingLines.add(nextLine.get());
 				} else {
 					T result=match.get().process(nonMatchingLines);
 					_listener.entry(result);
 					nonMatchingLines = Lists.newArrayList();
 					readDone = true;
 				}
 			}
             lines++;
            if (lines>=showNumberAt) {
                System.out.print("\r"+lines);
                showNumberAt=lines+OFFSET;
            }
         } while (!readDone);
 	}
 
 	private Optional<IMatch<T>> firstMatch(IRewindableReader reader) throws IOException {
 		reader.setMarker();
 		for (IMatcher<T> matcher : _matcher) {
 			Optional<IMatch<T>> match = matcher.match(reader);
 			if (match.isPresent()) {
 				return match;
 			} else {
 				reader.jumpToMarker();
 			}
 		}
 		return Optional.absent();
 	}
 }
