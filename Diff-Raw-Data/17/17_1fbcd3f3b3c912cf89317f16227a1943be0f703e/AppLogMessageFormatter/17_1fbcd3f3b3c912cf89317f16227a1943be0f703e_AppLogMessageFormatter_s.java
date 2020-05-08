 /*******************************************************************************
  * Copyright 2012 Pradeep Nambiar,  Pexus LLC
  * 
  * Source File: src/org/perf/log/app/logger/AppLogMessageFormatter.java 
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.perf.log.app.logger;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.MessageFormat;
 import java.util.logging.Formatter;
 import java.util.logging.LogRecord;
 
/*******************************************************************************
 * Copyright 2012 Pradeep Nambiar,  Pexus LLC
 * 
 * Source File: src/org/perf/log/app/logger/AppLogMessageFormatter.java 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
 /**
  * This class is used to format the message as passed.
  * MessageFormat can be edited to add any prefix or suffix to log messages.
  *
  * */
 public class AppLogMessageFormatter extends Formatter
 {
 	private MessageFormat messageFormat = new MessageFormat("{0}\n");
 
 	@Override
 	public String format(LogRecord record) 
 	{
 		Object[] arguments = new Object[1];
 		
 		if(record.getThrown() != null) 
 		{
 			arguments[0] = record.getMessage()+"\n"+getStackTrace(record.getThrown());
 		} else 
 		{
 			arguments[0] = record.getMessage();
 		}
 		return messageFormat.format(arguments);
 	}
 
 	public AppLogMessageFormatter(MessageFormat mf) 
 	{
 		super();
 		messageFormat = mf;
 	}
 	public static String getStackTrace(Throwable t) 
 	{
 		StringWriter strWriter = new StringWriter();
 		PrintWriter prtWriter = new PrintWriter(strWriter, true);
 		t.printStackTrace(prtWriter);
 		prtWriter.flush();
 		strWriter.flush();
 		return strWriter.toString();
 	}
 
 }
