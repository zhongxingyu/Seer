 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.core.inject;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Iterables;
 import com.google.common.io.Closeables;
 import com.google.inject.TypeLiteral;
 import com.google.inject.spi.TypeConverter;
 
 import de.cosmocode.commons.Strings;
 
 /**
  * A {@link TypeConverter} which reads csv files and produces
  * {@link List}s of {@link String} arrays.
  * 
 * <h4>Defaults</h4>
  * 
  * <table>
  *   <tr>
  *     <th>Separator</th>
  *     <td>,</td>
  *   </tr>
  *   <tr>
  *     <th>Quote char</th>
  *     <td>"</td>
  *   </tr>
  *   <tr>
  *     <th>Escape char</th>
  *     <td>\</td>
  *   </tr>
  * </table>
 * 
 * <h4>Usage</h4>
  * <p>
  *   application.properties<br />
 *   {@code my.data = csv:file:data.csv}
  * </p>
  * 
 * <h4>Configuration</h4>
  * <p>
  *   Separator, quote char and escape char can be configured using the following
  *   syntax:<br />
  *   {@code csv:<separator>:<quote>:<escape>:<pathToFile>}<br />
  * </p>
  * 
  * <p>
  *   Example:<br />
 *   {@code my.data = csv:\t:':#:file:data.csv}<br />
  * </p>
  * 
  * <p>
  *   There is no need to specify every three characters, if you only want to change
 *   the separator, just leave the rest:<br />
 *   {@code my.data = csv:\t:file:data.csv}
  * </p>
  * 
  * <p>
  *   <strong>Note</strong>: The given file must fulfill the following requirements:
  *   <ul>
  *     <li>must contain at least one line</li>
  *     <li>must contain a header line (first line)</li>
  *     <li>must not contain any line that is longer than the header</li>
  *   </ul>
  *   Lines that are smaller than the header will be filled with empty strings,
  *   to match the header size.
  * </p>
  * 
  * @since 2.9
  * @author Willi Schoenborn
  */
 public final class CsvConverter implements TypeConverter {
 
     static final TypeLiteral<List<String[]>> LITERAL = new TypeLiteral<List<String[]>>() { };
     
     static final Pattern PATTERN = Pattern.compile("csv(?::(.))?(?::(.))?(?::(.))?:([^:].+)");
     
     private static final Logger LOG = LoggerFactory.getLogger(CsvConverter.class);
     
     private final URLConverter converter = new URLConverter();
     
     @Override
     public List<String[]> convert(String value, TypeLiteral<?> toType) {
         final Matcher matcher = PATTERN.matcher(value);
         Preconditions.checkArgument(matcher.matches(), "%s does not match %s", value, PATTERN.pattern());
         
         final char separator = extractChar(matcher, 1, ',');
         final char quote = extractChar(matcher, 2, '"');
         final char escape = extractChar(matcher, 3, '\\');
         final String location = matcher.group(4);
         
         final URL url = converter.convert(location, URLConverter.LITERAL);
         
         final InputStream stream;
         
         try {
             stream = url.openStream();
         } catch (IOException e) {
             throw new IllegalArgumentException(e);
         }
         
         try {
             final Reader reader = new InputStreamReader(stream);
             
             LOG.trace("Reading csv from {}, column separated by '{}', " +
                 "values quoted by '{}', special chars escaped with '{}'", new Object[] {
                     location, separator, quote, escape
                 });
             
             final CSVReader csv = new CSVReader(reader, separator, quote, escape);
             final List<String[]> lines = csv.readAll();
             Preconditions.checkArgument(lines.size() > 0, "%s is empty", value);
             
             final String[] header = lines.get(0);
             
             for (int index = 1; index < lines.size(); index++) {
                 final String[] line = lines.get(index);
                 
                 if (line.length == header.length) {
                     continue;
                 } else if (line.length < header.length) {
                     final String[] copy = new String[header.length];
                     Arrays.fill(copy, "");
                     System.arraycopy(line, 0, copy, 0, line.length);
                     lines.set(index, copy);
                 } else {
                     // we skip the header, but we count from line 1
                     throw new IllegalArgumentException(String.format("Line #%s is too long", index - 1));
                 }
             }
             
             return lines;
         } catch (IOException e) {
             throw new IllegalArgumentException(e);
         } finally {
             Closeables.closeQuietly(stream);
         }
     }
     
     private char extractChar(Matcher matcher, int group, char defaultValue) {
         final String s = matcher.group(group);
         if (s == null) {
             return defaultValue;
         } else {
             // guarded by regex
             assert s.length() == 1;
             return s.charAt(0);
         }
     }
     
 }
