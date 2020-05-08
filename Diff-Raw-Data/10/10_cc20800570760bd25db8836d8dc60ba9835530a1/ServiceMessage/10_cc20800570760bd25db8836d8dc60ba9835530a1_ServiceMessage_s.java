 /*
 Part of the NETLab Hub, which is part of the NETLab Toolkit project - http://netlabtoolkit.org
 
 Copyright (c) 2006-2013 Ewan Branda
 
 NETLab Hub is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 NETLab Hub is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with NETLab Hub.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package netlab.hub.core;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import netlab.hub.util.StringUtils;
 
 public class ServiceMessage {
 	
 	String original;
 	
 	LinkedList<String> absoluteAddress;
 	String absoluteAddressString;
 	
 	LinkedList<String> serviceAddress;
 	String serviceAddressString;
 	
 	String pathString;
 	String pathStringWithArgs;
 	LinkedList<String> path;
 	
 	LinkedList<String> arguments;
 	
 	public ServiceMessage(String str) {
 		parse(str);
 	}
 	
 	/**
 	 * Helper method to parse a message string received from client.
 	 */
 	public void parse(String str) {
 		if (str == null) {
 			return;
 		}
 		original = str.trim();
 		absoluteAddress = new LinkedList<String>();
 		serviceAddress = new LinkedList<String>();
 		pathString = null;
 		pathStringWithArgs = null;
 		path = new LinkedList<String>();
 		arguments = new LinkedList<String>();
 		
 		if (original.length() == 0) {
 			return;
 		}
 		
 		String absoluteAddressString;
 		String argsString = null;
 		if (original.indexOf(" ") > -1) {
 			int separatorIdx = original.indexOf(" ");
 			absoluteAddressString = original.substring(0, separatorIdx).trim();
 			argsString = original.substring(separatorIdx).trim();
 		} else {
 			absoluteAddressString = original;
 		}
 				
 		// Parse the path
 		if (absoluteAddressString.endsWith("/")) {
 			absoluteAddressString = absoluteAddressString.substring(0, absoluteAddressString.length() - 1);
 		}
 		this.absoluteAddressString = absoluteAddressString;
 		if (absoluteAddressString.startsWith("/")) {
 			absoluteAddressString = absoluteAddressString.substring(1);
 		}
 		String[] absAddrSegments = ServiceMessage.tokenize(absoluteAddressString, "/");
 		for (int i=0; i<absAddrSegments.length; i++) {
 			absoluteAddress.add(absAddrSegments[i]);
 		}
 		StringBuffer serviceAddressSb = new StringBuffer();
 		for (int i=0; i<Math.min(3, absAddrSegments.length); i++) {
 			serviceAddress.add(absAddrSegments[i]);
 			serviceAddressSb.append("/").append(absAddrSegments[i]);
 		}
 		serviceAddressString = serviceAddressSb.toString();
 		StringBuffer pathSb = new StringBuffer();
 		for (int i=3; i<absAddrSegments.length; i++) {
 			path.add(absAddrSegments[i]);
 			pathSb.append("/").append(absAddrSegments[i]);
 		}
 		this.pathString = pathSb.toString();
 		this.pathStringWithArgs = argsString == null ? this.pathString : pathSb.append(" ").append(argsString).toString();
 		if (argsString != null) {
 			arguments.clear();
 			String[] tokens = ServiceMessage.tokenize(argsString, " ");
 			for (int i=0; i<tokens.length; i++) {
 				arguments.add(tokens[i]);
 			}
 		}
 	}
 
 	public static String[] tokenize(String input, String delimiter) {
 		List<String> tokens = new ArrayList<String>();
 		String[] chunks = input.split(delimiter);
 		String chunk;
 		StringBuffer currentToken = new StringBuffer();
 		int escapeDepth = 0;
 		for (int i=0; i<chunks.length; i++) {
 			chunk = chunks[i];
 			if (chunk.length() == 0) continue;
 			currentToken.append(chunk);
 			escapeDepth += StringUtils.numberOfOccurences(chunk, '{') - StringUtils.numberOfOccurences(chunk, '}');
 			if (escapeDepth == 0 && currentToken.length() > 0) {
 				String token = currentToken.toString();
 				if (token.startsWith("{") && token.endsWith("}")) {
 					token = token.substring(1, token.length()-1);
 				}
 				tokens.add(token);
 				currentToken.setLength(0);
 			} else {
 				currentToken.append(delimiter);
 			}
 		}
 		String[] tokenArray = new String[tokens.size()];
 		for (int i=0; i<tokens.size(); i++) {
 			tokenArray[i] = tokens.get(i);
 		}
 		return tokenArray;
 	}
 	
 	public boolean isValid() {
 		return serviceAddress.size() == 3;
 	}
 	
 	public String toString() {
 		return original;
 	}
 	
 	public String debug() {
 		StringWriter buffer = new StringWriter();
 		PrintWriter out = new PrintWriter(buffer);
 		out.print("ORIGINAL: "); out.println(this);
 		out.print("PATH STRING: "); out.println(pathString);
 		out.println("PATH SEGMENTS: ");
 		for (Iterator<String> it = path.iterator(); it.hasNext();) {
 			out.print("  "); out.println(it.next());
 		}
 		out.println("ARGUMENTS: ");
 		for (Iterator<String> it = arguments.iterator(); it.hasNext();) {
 			out.print("  "); out.println(it.next());
 		}
 		return buffer.toString();
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean isConfig() {
 		try {
 			return "config".equals(path.getFirst()) ||
 					"nlhubconfig".equals(path.getFirst()); // nlhubconfig supported for legacy
 		} catch (NoSuchElementException e) {
 			return false;
 		}
 	}
 	
 	public boolean isHubConfig() {
 		try {
 			return isConfig() && getServiceAddress().get(1).equals("hub");
 		} catch (NoSuchElementException e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * Returns the segments corresponding to the address
 	 * portion of the client message (the first three segments). 
 	 * 
 	 * @return the address segments
 	 * @see getServiceAddressString
 	 */
 	public LinkedList<String> getServiceAddress() {
 		return this.serviceAddress;
 	}
 	
 	/**
 	 * Returns the address portion of the client message
 	 * as a String. For the client message
 	 * "/service/core/hello/say World", the address portion
 	 * would be "/service/core/hello".
 	 * 
 	 * @return the address
 	 */
 	public String getServiceAddressString() {
 		return this.serviceAddressString;
 	}
 	
 	
 	public LinkedList<String> getAbsoluteAddress() {
 		return this.absoluteAddress;
 	}
 	
 	/**
 	 * Returns the full address portion of the client message
 	 * as a String (without args). For the client message
 	 * "/service/core/hello/say World", the address portion
 	 * would be "/service/core/hello/say".
 	 * 
 	 * @return the address
 	 */
 	public String getAbsoluteAddressString() {
 		return this.absoluteAddressString;
 	}
 
 	/**
 	 * Returns the segments in the client message
 	 * after the service address.
 	 * 
 	 * @return the segments
 	 * @see getPathString
 	 */
 	public LinkedList<String> getPath() {
 		return this.path;
 	}
 	
 	/**
 	 * Returns the path element at the specified position.
 	 * For the client message "/service/core/hello/say/multiple"
 	 * the path would be "/say/multiple" and the path element 0
 	 * would be "say". If a negative value is passed in then
 	 * the value is used as an offset from the last element. 
 	 * A value of -1 would return the second-to-last 
 	 * element in the path segments list.
 	 * 
 	 * @param index
 	 * @return the element at the specified position
 	 */
 	public String getPathElement(int index) {
 		int idx = index;
 		if (index < 0) {
 			idx = path.size() + index - 1;
 		}
 		try {
 			return this.path.get(idx);
 		} catch (IndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 	
 	/**
 	 * Returns the path portion of the client message.
 	 * For the client message "/service/core/hello/say/multiple"
 	 * the path would be "/say/multiple".
 	 * 
 	 * @param includeArgs - specifies whether the message arguments should be included
 	 * @return
 	 */
 	public String getPathString(boolean includeArgs) {
 		return includeArgs ? this.pathStringWithArgs : this.pathString;
 	}
 	
 	public String getPathString() {
 		return getPathString(false);
 	}
 	
 	public String getPathString(boolean includeArgs, int offset) {
 		if (offset > 0) {
 			StringBuffer sb = new StringBuffer();
 			for (int i=offset; i<path.size(); i++) {
 				sb.append("/").append(path.get(i));
 			}
 			return sb.toString();
 		} else {
 			return getPathString(includeArgs);
 		}
 	}
 
 	/**
 	 * Returns the arguments sent with the original message.
 	 * 
 	 * @return the argument Strings
 	 */
 	public LinkedList<String> getArguments() {
 		return this.arguments;
 	}
 	
 	/**
 	 * Returns the argument at the specified position.
 	 * 
 	 * @return the argument String, or null of there is no argument at the specified position.
 	 */
 	public String getArgument(int index) {
 		try {
 			return this.arguments.get(index);
 		} catch (IndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 	
 	public String arg(int index) {
 		return getArgument(index);
 	}
 	
 	public int argInt(int index, int defaultValue) {
 		if (hasArgument(index)) {
 			try {
 				return (Integer)getArgument(index, Integer.class);
 			} catch (Exception e) {}
 		}
 		return defaultValue;
 	}
 	
 	public float argFloat(int index) throws ServiceException {
 		return (Float)getArgument(index, Float.class);
 	}
 	
 	public double argDouble(int index) throws ServiceException {
 		return (Double)getArgument(index, Double.class);
 	}
 	
	@SuppressWarnings("unchecked")
 	public Object getArgument(int index, Class cls) throws ServiceException {
 		String arg = getArgument(index);
 		if (arg == null) throw new ServiceException("No argument ["+index+"]");
 		try {
 			if (cls == Integer.class) {
 				return Integer.parseInt(arg);
 			} else if (cls == Float.class) {
 				return Float.parseFloat(arg);
 			} else if (cls == Double.class) {
 				return Double.parseDouble(arg);
 			} else {
 				return arg;
 			}
 		} catch (Throwable t) {
 			throw new ServiceException(t);
 		}
 	}
 	
 	public boolean hasArgument(int index) {
 		return getArgument(index) != null;
 	}
 	
 	/**
 	 * @return
 	 */
 	public String getArgumentsAsString() {
 		StringBuffer sb = new StringBuffer();
 		getArguments(sb);
 		return sb.toString();
 	}
 	
 	/**
 	 * @param sb
 	 */
 	public void getArguments(StringBuffer sb) {
 		for (Iterator<String> it=arguments.iterator(); it.hasNext();) {
 			sb.append(it.next());
 			if (it.hasNext()) {
 				sb.append(" ");
 			}
 		}
 	}
 	
 	/**
 	 * @return
 	 */
 	public String[] getArgumentsAsStringArray() {
 		String[] args = new String[arguments.size()];
 		for (int i=0; i<arguments.size(); i++) {
 			args[i] = getArgument(i);
 		}
 		return args;
 	}
 	
 	public int hashCode() {
 		return this.toString().hashCode();
 	}
 	
 	public boolean equals(Object other) {
 		if (other instanceof ServiceMessage) {
 			return ((ServiceMessage)other).toString().equals(toString());
 		}
 		return false;
 	}
 
 	public Object[] getArgumentsAsObjectArray() {
 		String[] strArgs = getArgumentsAsStringArray();
 		Object[] args = new Object[strArgs.length];
 		for (int i=0; i<strArgs.length; i++) {
 			args[i] = argumentToObject(strArgs[i]);
 		}
 		return args;
 	}
 	
 	public static Object argumentToObject(String str) {
 		if (str.endsWith("s")) {
 			String prefix = str.substring(0, str.length()-1);
 			if (argumentToObject(prefix) instanceof String) {
 				return str;
 			} else {
 				return prefix;
 			}
 		}
 		try {
 			return Integer.parseInt(str);
 		} catch (Exception e) {}
 		try {
 			return Float.parseFloat(str);
 		} catch (Exception e) {}
 		if (str.endsWith("i")) {
 			try {
 				return Integer.parseInt(str.substring(0, str.length()-1));
 			} catch (Exception e) {}
 		}
 		return str;
 	}
 	
 }
