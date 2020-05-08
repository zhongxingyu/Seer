 /**
  * Copyright (c) 2008, Damian Carrillo
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted 
  * provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice, this list of 
  *     conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of 
  *     conditions and the following disclaimer in the documentation and/or other materials 
  *     provided with the distribution.
  *   * Neither the name of the copyright holder's organization nor the names of its contributors 
  *     may be used to endorse or promote products derived from this software without specific 
  *     prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package agave.internal;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
  */
 public class MultipartParserImpl implements MultipartParser {
     
     private static class CoupledLine {
         List<Integer> bytes = new ArrayList<Integer>();
         StringBuilder text = new StringBuilder();
     }
 
     private static final String contentDisposition = "Content-Disposition:\\s*form-data;\\s*name=\"(.*)\"";
     private static final Pattern partPattern = Pattern.compile(contentDisposition + ";\\s*filename=\"(.+)\"");
     private static final Pattern filenamePattern = Pattern.compile("(.*)\\.(.*)");
     private static final Pattern parameterPattern = Pattern.compile(contentDisposition);
     private static final Pattern contentTypePattern = Pattern.compile("Content-Type:\\s*(.+)");
     private static final Pattern otherHeaderPattern = Pattern.compile("(\\S+):\\s*(.+?)");
 
     private Pattern boundaryPattern;
     private Pattern eosPattern;
     
     private boolean deleteTemporaryFilesOnExit;
     private String defaultSuffix;
 
     private Map<String, Collection<String>> parameters = new HashMap<String, Collection<String>>();
     private Map<String, Part> parts = new HashMap<String, Part>();
     
     public MultipartParserImpl() {
         this(true, "tmp");
     }
     
     public MultipartParserImpl(boolean deleteTemporaryFilesOnExit, String defaultSuffix) {
         this.deleteTemporaryFilesOnExit = deleteTemporaryFilesOnExit;
         this.defaultSuffix = defaultSuffix;
     }
 
     public void parseInput(InputStream in) throws IOException {
         if (in == null) {
             throw new NullPointerException("Supplied InputStream was not null");
         }
 
         String boundary = readLine(in).text.toString().trim();
         boundaryPattern = Pattern.compile(boundary);
         eosPattern = Pattern.compile(boundary + "--");
 
         while (true) {
             Part part = new PartImpl();
             readHeaders(in, part);
             if (part.getFilename() != null) {
                 if (readPart(in, part)) {
                     break;
                 }
             } else {
                 if (readParameter(in, part)) {
                     break;
                 }
             }
         }
     }
     
     public void readHeaders(InputStream in, Part part) throws IOException {
         CoupledLine line = null;
         while ((line = readLine(in)) != null) {
             String text = line.text.toString().trim();
             
             if ("".equals(text)) {
                 break;
             }
             
             Matcher matcher = partPattern.matcher(text);
             if (matcher.matches() && matcher.groupCount() >= 2) {
                 part.setName(matcher.group(1));
                 part.setFilename(matcher.group(2));
                 continue;
             }
             
             matcher = parameterPattern.matcher(text);
             if (matcher.matches() && matcher.groupCount() >= 1) {
                 part.setName(matcher.group(1));
                 continue;
             }
             
             matcher = contentTypePattern.matcher(text);
             if (matcher.matches() && matcher.groupCount() >= 1) {
                 part.setContentType(matcher.group(1));
                 continue;
             }
             
             matcher = otherHeaderPattern.matcher(text);
             if (matcher.matches() && matcher.groupCount() >= 2) {
                 part.addHeader(matcher.group(1), matcher.group(2));
                 continue;
             }
         }
     }
     
     public boolean readPart(InputStream in, Part part) throws IOException {
         boolean eos = false;
         
         String prefix = null;
         String suffix = null;
         
         Matcher matcher = filenamePattern.matcher(part.getFilename());
         if (matcher.matches() && matcher.groupCount() >= 2) {
             prefix = matcher.group(1);
             suffix = matcher.group(2);
         } else {
             prefix = part.getName();
             suffix = defaultSuffix;
         }
         
         File temporaryFile = File.createTempFile(prefix, suffix);
         if (deleteTemporaryFilesOnExit) {
            temporaryFile.deleteOnExit();
         }
         
         FileOutputStream out = new FileOutputStream(temporaryFile);
         
         CoupledLine line = null;
         while ((line = readLine(in)) != null) {
             String text = line.text.toString().trim();
             
             matcher = eosPattern.matcher(text);
             if (matcher.matches()) {
                 eos = true;
                 break;
             }
             
             matcher = boundaryPattern.matcher(text);
             if (matcher.matches()) {
                 break;
             }
             
             for (Integer b : line.bytes) {
                 out.write(b);
             }
         }
         
         out.close();
         part.setContents(temporaryFile);
         parts.put(part.getName(), part);
         return eos;
     }
     
     public boolean readParameter(InputStream in, Part part) throws IOException {
         boolean eos = false;
         StringBuilder parameterValue = new StringBuilder();
         
         CoupledLine line = null;
         while((line = readLine(in)) != null) {
             String text = line.text.toString().trim();
             
             Matcher matcher = eosPattern.matcher(text);
             if (matcher.matches()) {
                 eos = true;
                 break;
             }
             
             matcher = boundaryPattern.matcher(text);
             if (matcher.matches()) {
                 break;
             }
             
             parameterValue.append(line.text);
         }
         
         if (parameterValue != null && !"".equals(parameterValue)) {
             if (parameters.get(part.getName()) == null) {
                 parameters.put(part.getName(), new ArrayList<String>());
             }
             parameters.get(part.getName()).add(parameterValue.toString().trim());
         }
         
         return eos;
     }
 
     private CoupledLine readLine(InputStream in) throws IOException {
         CoupledLine line = new CoupledLine();
         int b = -1;
 
         while ((b = in.read()) != -1) {
             line.bytes.add(b);
             line.text.append((char) b);
 
             if ((char) b == '\n') {
                 break;
             }
         }
         
         return ((line.bytes.isEmpty()) ? null : line);
     }
 
     public Map<String, Collection<String>> getParameters() {
         return parameters;
     }
 
     public Map<String, Part> getParts() {
         return parts;
     }
 }
