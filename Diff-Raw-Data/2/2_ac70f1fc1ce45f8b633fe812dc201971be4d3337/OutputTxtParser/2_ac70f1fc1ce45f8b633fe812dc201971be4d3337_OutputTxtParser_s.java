 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.parser;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.araqne.logdb.PartitionPlaceholder;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryCommandParser;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.PartitionPlaceholder.Source;
 import org.araqne.logdb.query.command.OutputTxt;
 
 /**
  * @since 1.6.7
  * @author darkluster
  * 
  */
 public class OutputTxtParser implements QueryCommandParser {
 
 	@Override
 	public String getCommandName() {
 		return "outputtxt";
 	}
 
 	@Override
 	public QueryCommand parse(QueryContext context, String commandString) {
 		if (commandString.trim().endsWith(","))
 			throw new QueryParseException("missing-field", commandString.length());
 
 		ParseResult r = QueryTokenizer.parseOptions(context, commandString, "outputtxt".length(),
 				Arrays.asList("delimiter", "overwrite", "gz", "encoding", "partition", "tmp"));
 
 		@SuppressWarnings("unchecked")
 		Map<String, String> options = (Map<String, String>) r.value;
 
 		String delimiter = options.get("delimiter");
 		if (delimiter == null)
 			delimiter = " ";
 
 		String encoding = options.get("encoding");
 		if (encoding == null)
 			encoding = "utf-8";
 
 		String tmpPath = options.get("tmp");
 
 		boolean overwrite = CommandOptions.parseBoolean(options.get("overwrite"));
 		boolean useCompression = CommandOptions.parseBoolean(options.get("gz"));
 		boolean usePartition = CommandOptions.parseBoolean(options.get("partition"));
 
 		File tmpFile = null;
 		if (tmpPath != null) {
 			tmpFile = new File(tmpPath);
 			if (!usePartition && tmpFile.exists())
 				throw new QueryParseException("tmp file exist: " + tmpPath, -1);
 		}
 
 		int next = r.next;
 		if (next < 0)
 			throw new QueryParseException("invalid-field", next);
 
 		String remainCommandString = commandString.substring(next);
 		QueryTokens tokens = QueryTokenizer.tokenize(remainCommandString);
 		if (tokens.size() < 1)
 			throw new QueryParseException("missing-field", tokens.size());
 
 		String filePath = tokens.token(0).token;
 		filePath = ExpressionParser.evalContextReference(context, filePath);
 
 		List<PartitionPlaceholder> holders = PartitionPlaceholder.parse(filePath);
 		if (!usePartition && holders.size() > 0)
 			throw new QueryParseException("use-partition-option", -1, holders.size() + " partition holders");
 
 		List<String> fields = new ArrayList<String>();
 
 		List<QueryToken> queryFields = tokens.subtokens(1, tokens.size());
 		for (QueryToken token : queryFields) {
 			if (!token.token.contains(",")) {
 				fields.add(token.token.trim());
 				continue;
 			}
 
 			StringTokenizer tok = new StringTokenizer(token.token, ",");
 			while (tok.hasMoreTokens())
 				fields.add(tok.nextToken().trim());
 		}
 
 		if (fields.size() == 0)
 			throw new QueryParseException("missing-field", remainCommandString.length());
 
 		File txtFile = new File(filePath);
 		if (txtFile.exists() && !overwrite)
 			throw new IllegalStateException("txt file exists: " + txtFile.getAbsolutePath());
 
 		try {
			if (txtFile.getParentFile() != null)
 				txtFile.getParentFile().mkdirs();
 
 			return new OutputTxt(txtFile, filePath, tmpPath, overwrite, delimiter, fields, useCompression, encoding,
 					usePartition, holders);
 		} catch (IOException e) {
 			throw new QueryParseException("io-error", -1, e.getMessage());
 		}
 	}
 }
