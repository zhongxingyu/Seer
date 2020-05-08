 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.myfaces.extension.scripting.doccompiler;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.nio.charset.Charset;
 import java.util.Iterator;
 
 /**
  * A simple cross compile solution for the xslt mdtext conversion
  * for the apache cms. It uses xslt as transformation language
  *
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class XSLTDocCompiler
 {
     static final String docRootPath = "../../../src/site/xdoc";
     static final String strTargetDir = "../../../src/site/mdtext";
     static final String strXsltFile = "../../src/main/resources/xdocconversion.xslt";
 
     static String currentPath = null;
 
     static
     {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
 
         try
         {
             currentPath = URLDecoder.decode(loader.getResource("./").getPath(),
                     Charset.defaultCharset().toString());
         }
         catch (UnsupportedEncodingException e)
         {
 
         }
     }
 
     public static void main(String... argv)
     {
         String[] fileEndings = {"xml"};
 
         Iterator fileIter = FileUtils.iterateFiles(new File(FilenameUtils.normalize(currentPath + File.separator + docRootPath)), fileEndings, true);
         while (fileIter.hasNext())
         {
             File toProcess = (File) fileIter.next();
             transformFile(toProcess);
         }
 
     }
 
     //code blocks must be at least 4 chars into the
     static int handleCode(StringBuilder target, int pos, String[] lines)
     {
         pos++;
         while (!lines[pos].contains("</code>"))
         {
             target.append("    ");
             target.append(lines[pos]);
             target.append("\n");
             pos++;
         }
         return pos;
     }
 
     static int determinePrecedingBlanks(String in)
     {
         //we determine how many blanks are before the first < and we cut that from the rest
         //of the table, that way we keep the formatting but do not run into the code issue mdtext has
         char[] tokenizer = in.toCharArray();
         int pos = 0;
         for (; pos < tokenizer.length; pos++)
         {
             char token = tokenizer[pos];
             if (!(token == ' '))
             {
                 break;
             }
         }
         return pos;
     }
 
     static String cutPrecedingBlanks(String in, int maxBlanks)
     {
         char[] tokenizer = in.toCharArray();
         Character token = null;
         int cnt = 0;
         for (; cnt <= maxBlanks && cnt < tokenizer.length; cnt++)
         {
             token = tokenizer[cnt];
             if (!token.equals(' '))
             {
                 break;
             }
         }
         StringBuilder finalStr = new StringBuilder(in.length());
         while (cnt < tokenizer.length)
         {
             finalStr.append(tokenizer[cnt]);
             cnt++;
         }
         return finalStr.toString();
     }
 
     static int handleUL(StringBuilder target, int pos, String[] lines, int indendation)
     {
         int precedingBlanks = Math.max(0, determinePrecedingBlanks(lines[pos]) - indendation);
         target.append(cutPrecedingBlanks(lines[pos], precedingBlanks));
         target.append("\n");
         //special case same line ul is closed
         if (lines[pos].contains("</ul>"))
         {
             return pos;
         }
 
         pos++;
         //TODO nesting
 
         while (pos < lines.length && !lines[pos].contains("</ul>"))
         {
             if (lines[pos].contains("<ul>"))
             {
                 pos = handleUL(target, pos, lines, precedingBlanks);
             } else
             {
                 target.append(cutPrecedingBlanks(lines[pos], precedingBlanks));
                 target.append("\n");
             }
             pos++;
 
         }
         if (pos < lines.length)
         {
             target.append(cutPrecedingBlanks(lines[pos], precedingBlanks));
             target.append("\n");
         }
         return pos;
     }
 
     static int handleTable(StringBuilder target, int pos, String[] lines, int indentation)
     {
         int precedingBlanks = Math.max(0, determinePrecedingBlanks(lines[pos]) - indentation);
         target.append(cutPrecedingBlanks(lines[pos], precedingBlanks));
         target.append("\n");
         if (lines[pos].contains("</table>"))
         {
             return pos;
         }
         pos++;
 
         while (pos < lines.length && !lines[pos].contains("</table>"))
         {
             if (lines[pos].contains("<table>"))
             {
                 pos = handleTable(target, pos, lines, precedingBlanks);
             } else
             {
                 target.append(cutPrecedingBlanks(lines[pos], precedingBlanks));
                 target.append("\n");
             }
             pos++;
         }
         target.append(cutPrecedingBlanks(lines[pos], precedingBlanks));
         target.append("\n");
         return pos;
     }
 
     static String formatter(String in)
     {
         StringBuilder target = new StringBuilder();
        target.append("<!--\n" +
                "    Licensed to the Apache Software Foundation (ASF) under one\n" +
                "    or more contributor license agreements.  See the NOTICE file\n" +
                "    distributed with this work for additional information\n" +
                "    regarding copyright ownership.  The ASF licenses this file\n" +
                "    to you under the Apache License, Version 2.0 (the\n" +
                "    \"License\"); you may not use this file except in compliance\n" +
                "    with the License.  You may obtain a copy of the License at\n" +
                "\n" +
                "    http://www.apache.org/licenses/LICENSE-2.0\n" +
                "\n" +
                "    Unless required by applicable law or agreed to in writing,\n" +
                "    software distributed under the License is distributed on an\n" +
                "    \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" +
                "    KIND, either express or implied.  See the License for the\n" +
                "    specific language governing permissions and limitations\n" +
                "    under the License.\n" +
                "-->");

         String[] lines = in.split("\n+");
 
         for (int cnt = 0; cnt < lines.length; cnt++)
         {
             String line = lines[cnt];
             //no inline xml/html formatting
             if (line.contains("<code>"))
             {
                 cnt = handleCode(target, cnt, lines);
             } else if (line.contains("<table>"))
             {
                 cnt = handleTable(target, cnt, lines, 0);
             } else if (line.contains("<ul>"))
             {
                 cnt = handleUL(target, cnt, lines, 0);
             } else if (line.matches("\\s+[^#]+"))
             {
                 line = line.replaceAll("\\s+", " ");
 
             } else if (line.matches("\\s+#.+"))
             {
                 line = line.replaceAll("^\\s+", "");
             }
             if (!line.contains("<code>") && !line.contains("<table>") && !line.contains("<ul>"))
             {
                 target.append(line);
                 target.append("\n");
             }
         }
         return target.toString();
     }
 
     private static void transformFile(File xmlFile)
     {
         //File xmlFile = new File(strXmlFile);
         File xsltFile = new File(FilenameUtils.normalize(currentPath + File.separator + strXsltFile));
 
         // JAXP liest Daten über die Source-Schnittstelle
         Source xmlSource = new StreamSource(xmlFile);
         Source xsltSource = new StreamSource(xsltFile);
 
         // das Factory-Pattern unterstützt verschiedene XSLT-Prozessoren
         TransformerFactory transFact =
                 TransformerFactory.newInstance();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
         try
         {
             Transformer trans = transFact.newTransformer(xsltSource);
             trans.transform(xmlSource, new StreamResult(baos));
             String result = baos.toString("UTF-8");
 
             String fileName = FilenameUtils.normalize(currentPath + File.separator + strTargetDir + File.separator + xmlFile.getName().split("\\.")[0] + ".mdtext");
             File target = new File(fileName);
             FileUtils.deleteQuietly(target);
             result = formatter(result);
 
             FileUtils.writeStringToFile(target, result);
         }
         catch (TransformerException e)
         {
             e.printStackTrace();
         }
         catch (UnsupportedEncodingException e)
         {
             e.printStackTrace();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 }
