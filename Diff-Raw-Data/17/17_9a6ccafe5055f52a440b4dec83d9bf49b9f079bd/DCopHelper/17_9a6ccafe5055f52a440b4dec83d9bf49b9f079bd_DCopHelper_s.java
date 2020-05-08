 /* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package tesla.app.command.helper;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class DCopHelper implements ICommandHelper {
 
 	public static final String MAGIC_MARKER = "[dcop]";
 	
 	public String compileMethodCall(String dest, String path, String command,
 			List<String> args, boolean includeMarker) {
 		StringBuilder builder = new StringBuilder();
 		if (includeMarker) {
 			builder.append("echo " + MAGIC_MARKER + ";");
 		}
 		builder.append("dcop --all-users " + dest + " " + path + " " + command);
 		if (args != null) {
 			for (String arg : args) {
 				builder.append(" " + arg);
 			}
 		}
 		return builder.toString();
 	}
 	
 	public String compileMethodCall(String dest, String path, String command, List<String> args) {
 		return compileMethodCall(dest, path, command, args, true);
 	}
 
 	public String compileMethodCall(String dest, String path, String command) {
 		return compileMethodCall(dest, path, command, null);
 	}
 
 	public String compileMethodCall(String dest, String path, String command, boolean includeMarker) {
 		return compileMethodCall(dest, path, command, null, includeMarker);
 	}
 	
 	public String evaluateOutputAsString(String rawOut) {
 		return evaluateOutputAsString(rawOut, true);
 	}
 	
 	private String evaluateOutputAsString(String rawOut, boolean primitive) {
 		return rawOut;
 	}
 	
 	public boolean evaluateOutputAsBoolean(String rawOut) {
 		String data = evaluateOutputAsString(rawOut, true);
 		return Boolean.parseBoolean(data);
 	}
 	
 	public Map<String, String> evaluateOutputAsMap(String rawOut) {
 		// This is currently fixed to the implementation imposed in AmarokConfig...
 		// format :- key:value \n key:value \n key:value ...
 		
 		Map<String, String> out = new HashMap<String, String>();
 		while (rawOut.contains("\n")) {
 			int sectionEnd = rawOut.indexOf("\n");
 			String section = rawOut.substring(0, sectionEnd);
 			String[] parts = section.split(":");
			parts[0] = parts[0].trim();
			parts[1] = parts[1].trim();
			
			// Get the key/value pair and store in the map
			String key = parts[0];
			String value = evaluateOutputAsString(parts[1], false);
			out.put(key, value);
			
 			// Trim off the section
 			rawOut = rawOut.substring(sectionEnd + 1);
 		}
 		return out;
 	}
 }
