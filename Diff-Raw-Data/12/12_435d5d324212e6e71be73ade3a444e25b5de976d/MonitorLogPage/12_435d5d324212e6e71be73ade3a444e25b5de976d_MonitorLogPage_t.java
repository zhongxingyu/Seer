 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following 
  * conditions are provided as a summary of the NSL but the NSL remains the 
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL. 
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only 
  *    (as Java .class files or a .jar file containing the .class files) and only 
  *    as part of an application that uses The Software as part of its primary 
  *    functionality. No distribution of the package is allowed as part of a software 
  *    development kit, other library, or development tool without written consent of 
  *    Netspective Corporation. Any modified form of The Software is bound by 
  *    these same restrictions.
  * 
  * 3. Redistributions of The Software in any form must include an unmodified copy of 
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective 
  *    Corporation and may not be used to endorse products derived from The 
  *    Software without without written consent of Netspective Corporation. "Sparx" 
  *    and "Netspective" may not appear in the names of products derived from The 
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the 
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind. 
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING 
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.      
  *
  * @author Shahid N. Shah
  */
  
 /**
 * $Id: MonitorLogPage.java,v 1.2 2002-03-31 14:07:33 snshah Exp $
  */
 
 package com.netspective.sparx.ace.page;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.RandomAccessFile;
 import java.lang.reflect.Method;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.servlet.ServletException;
 
 import org.apache.log4j.Appender;
 import org.apache.log4j.Category;
 import org.apache.log4j.FileAppender;
 
 import com.netspective.sparx.ace.AceServletPage;
 import com.netspective.sparx.xaf.page.PageContext;
 
 public class MonitorLogPage extends AceServletPage
 {
     public static final int MAX_BUFFER_SIZE = 32768;
 
     public static final int LOGSTYLE_TEXT = 0;
     public static final int LOGSTYLE_TAB_DELIMITED = 1;
 
     private String categoryName;
     private String logStyleStr;
     private int logStyle;
     private String[] delimitedHeaders;
     private File logFile;
 
     public MonitorLogPage()
     {
         super();
     }
 
     public MonitorLogPage(String categoryName, String logStyle)
     {
         this.categoryName = categoryName;
         this.logStyleStr = logStyle;
         if(logStyle != null)
         {
             if("style-text".equals(logStyle))
                 this.logStyle = LOGSTYLE_TEXT;
             {
                 StringTokenizer st = new StringTokenizer(logStyle, ",");
                 String styleName = st.nextToken();
                 if(styleName.equals("style-tab"))
                     this.logStyle = LOGSTYLE_TAB_DELIMITED;
 
                 List headers = new ArrayList();
                 while(st.hasMoreTokens())
                     headers.add(st.nextToken());
 
                 if(headers.size() > 0)
                     delimitedHeaders = (String[]) headers.toArray(new String[headers.size()]);
             }
         }
     }
 
     public final String getName()
     {
         return categoryName == null ? "monitor" : categoryName;
     }
 
     public final String getPageIcon()
     {
         return "monitor.gif";
     }
 
     public final String getCaption(PageContext pc)
     {
         return categoryName == null ? "Logs" : categoryName;
     }
 
     public final String getHeading(PageContext pc)
     {
         return getCaption(pc);
     }
 
     public void sendText(PageContext pc, byte[] contents, int startIndex) throws IOException
     {
         PrintWriter out = pc.getResponse().getWriter();
         out.write("<pre style='font-family: lucida-sans,courier'>");
         for(int i = startIndex; i < contents.length; i++)
             out.write(contents[i]);
         out.write("</pre>");
     }
 
     public void sendFormatted(PageContext pc, byte[] contents, int startIndex, String delim) throws IOException
     {
         PrintWriter out = pc.getResponse().getWriter();
         List lines = new ArrayList();
         StringBuffer line = new StringBuffer();
         for(int i = startIndex; i < contents.length; i++)
         {
             if(((char) contents[i]) == '\n')
             {
                 lines.add(line.toString());
                 line = new StringBuffer();
                 continue;
             }
 
             line.append((char) contents[i]);
         }
 
         int lineCount = lines.size();
 
         out.write("<div class='content'><table border=0 cellspacing=0>");
         out.write("<tr valign='top' class='data_table_header'>");
         if(delimitedHeaders != null)
         {
             for(int h = 0; h < delimitedHeaders.length; h++)
             {
                 out.write("<th class='data_table'>");
                 out.write(delimitedHeaders[h]);
                 out.write("</th>");
             }
         }
         out.write("</tr>");
 
         for(int l = 0; l < lineCount; l++)
         {
             out.write("<tr valign='top' class='data_table'>");
             String columns = (String) lines.get(l);
             StringTokenizer st = new StringTokenizer(columns, delim);
             while(st.hasMoreTokens())
             {
                 String data = st.nextToken();
                 char firstChar = data.charAt(0);
                 if(data.length() > 0 && Character.isDigit(firstChar) || firstChar == '-')
                     out.write("<td class='data_table' align='right'>");
                 else
                     out.write("<td class='data_table'>");
                 out.write(data);
                 out.write("</td>");
             }
 
             out.write("</tr>");
         }
         out.write("</table></div>");
     }
 
     public void handlePageBody(PageContext pc) throws ServletException, IOException
     {
         PrintWriter out = pc.getResponse().getWriter();
         if(categoryName == null)
             return;
 
         if(logFile == null)
         {
             // find the first "appender" that is going to a file
             Category category = Category.getInstance(categoryName);
             for(Enumeration appenders = category.getAllAppenders(); appenders.hasMoreElements();)
             {
                 Appender appender = (Appender) appenders.nextElement();
                 if(appender instanceof FileAppender)
                 {
                    String fileName = ((FileAppender) appender).getFile();
                    if(fileName == null)
                    {
                        out.write("<p>Log4J appender '" + appender.getName() +"' does not point to a valid file (please update log4j.properties).");
                        return;
                    }

                    logFile = new File(fileName);
                     break;
                 }
             }
         }
 
         if(logFile != null)
         {
             byte[] contents = new byte[MAX_BUFFER_SIZE];
             RandomAccessFile file = new RandomAccessFile(logFile.getAbsolutePath(), "r");
 
             long fileLen = file.length();
             long showing = MAX_BUFFER_SIZE;
             if(fileLen > MAX_BUFFER_SIZE)
                 file.seek(fileLen - MAX_BUFFER_SIZE);
             else
                 showing = fileLen;
 
             file.read(contents);
             file.close();
 
             NumberFormat fmt = NumberFormat.getInstance();
             out.write("<div class='page_source'>Source: " + logFile.getAbsolutePath() + " (showing " + fmt.format(showing) + " of " + fmt.format(fileLen) + " bytes)</div>");
 
             int i = 0;
 
             // if we're showing only the last LOG_SIZE bytes, then don't show a partial line (the first will likely be partial)
             if(fileLen > MAX_BUFFER_SIZE)
             {
                 while(i < contents.length && contents[i] != Character.LINE_SEPARATOR)
                     i++;
             }
 
             if(logStyle == LOGSTYLE_TEXT)
                 sendText(pc, contents, i);
             else
                 sendFormatted(pc, contents, i, "\t");
         }
         else
         {
             out.write("<p>No log file found for category '<b>" + categoryName + "</b>'.");
         }
 
         out.write("<div class='content'><hr size=2 color='#AAAADD'>category '<b>" + categoryName + "</b>' appenders:<ol>");
 
         Category category = Category.getInstance(categoryName);
         for(Enumeration appenders = category.getAllAppenders(); appenders.hasMoreElements();)
         {
             Appender appender = (Appender) appenders.nextElement();
             out.write("<li><b>" + appender.getName() + "</b></li>");
 
             out.write("<ul>");
             Method[] methods = appender.getClass().getMethods();
             for(int m = 0; m < methods.length; m++)
             {
                 Method method = methods[m];
                 if(method.getName().startsWith("get"))
                 {
                     try
                     {
                         String propertyName = method.getName().substring(3);
                         Object propertyValue = method.invoke(appender, null);
                         out.write("<li><code>" + propertyName + " = " + (propertyValue != null ? propertyValue.toString() : "") + "</code>");
                     }
                     catch(Exception e)
                     {
                         out.write("<li>" + e.toString());
                     }
                 }
             }
             out.write("</ul>");
         }
 
         out.write("</ol></div>");
     }
 }
