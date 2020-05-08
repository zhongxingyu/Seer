 /* Copyright 2009 British Broadcasting Corporation
    Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.feeds;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 /**
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class JsonOembedItem extends OembedItem implements OembedOutput {
 
 	public void writeTo(OutputStream outputStream) {
 		
 		Writer writer = new OutputStreamWriter(outputStream);
 		try {
 			writer.write("{\n");
			output("version", "1.0", writer, false);
			output("type", type, writer, false);
			output("provider_url", providerUrl, writer, false);
			output("width", String.valueOf(width), writer, false);
			output("height", String.valueOf(height), writer, false);
			output("title", title, writer, false);
			output("html", embedCode, writer, true);
 			writer.write("}\n");
 			writer.close();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
	private void output(String key, String value, Writer writer, boolean lastAttribute) throws IOException {
 		if (value != null) {
			writer.write("  " + QUOTE + key + QUOTE + ": " + QUOTE + value  + QUOTE);
			if (lastAttribute) {
			    writer.write("\n");
			} else {
			    writer.write(",\n");
			}
 		}
 	}
 
 }
