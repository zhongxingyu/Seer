 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.app.server.routing;
 
 import static org.oobium.utils.CharStreamUtils.closer;
 import static org.oobium.utils.CharStreamUtils.find;
 import static org.oobium.utils.CharStreamUtils.findAny;
 import static org.oobium.utils.CharStreamUtils.isEqual;
 import static org.oobium.utils.StringUtils.tableName;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.oobium.http.constants.RequestType;
 
 public abstract class Route {
 
 	public static final int CONTROLLER = 0;
 	public static final int VIEW = 1;
 	public static final int ASSET = 2;
 	public static final int AUTHORIZATION = 3;
 	public static final int DYNAMIC_ASSET = 4;
 	public static final int REDIRECT = 5;
 	
 	
 	protected final int type;
 	protected final RequestType requestType;
 	protected String rule;
 	protected boolean matchOnFullPath;
 	protected Pattern pattern;
 	protected String path;
 	
 	protected String string;
 	
 	public Route(int type, RequestType requestType, String rule) {
 		this.type = type;
 		this.requestType = requestType;
 		this.rule = rule;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if(obj instanceof Route) {
 			return toString().equals(obj.toString());
 		}
 		return false;
 	}
 	
 	@Override
 	public int hashCode() {
 		return toString().hashCode();
 	}
 	
 	public boolean isFixed() {
 		return path != null;
 	}
 
 	public Matcher matcher(String input) {
 		return pattern.matcher(input);
 	}
 	
 	/**
 	 * @return the params array of this route; can be null
 	 */
 	protected abstract String[][] params();
 	
 	protected void parseRules(String rules, Class<?> clazz, List<String[]> params) {
 		StringBuilder regex = new StringBuilder(rules.length() + 30);
 		
 		char[] ca = rules.toCharArray();
 		int pix = find(ca, '?');
 		if(pix == -1) pix = ca.length;
 		int s0 = 0;
 		int s1 = find(ca, '{');
 		while(s1 != -1) {
 			regex.append(ca, s0, s1-s0);
 			int s2 = closer(ca, s1);
 			if(s2 == -1) {
 				throw new RoutingException("missing closer for variable starting at " + s1 + " in: " + rules);
 			} else {
 				String param = new String(ca, ++s1, s2-s1).trim();
 				if("models".equals(param)) {
 					if(clazz != null) {
 						regex.append(tableName(clazz));
 					} else {
 						throw new RoutingException("cannot resolve {model} variable with a null class");
 					}
 				} else if("id".equals(param)) {
 					params.add(new String[] { "id", null });
 					regex.append("(\\d+)");
 				} else {
 					int istart = find(ca, '[', s1, s2);
 					if(istart != -1) {
 						int iend = findAny(ca, s1, s2, ':', '=');
 						if(iend == -1) iend = s2;
 						if(iend == s2 || ca[iend] != '=') {
 							if(isEqual(ca, istart+1, iend-1, 'i','d')) {
 								params.add(new String[] { new String(ca, s1, iend-s1), null });
 								regex.append("(\\d+)");
 								s0 = s2 + 1;
 								s1 = find(ca, '{', s0);
 								continue;
 							}
 						}
 					}
 
 //					TODO make sure that the model actually has the given parameter (and of acceptable type...?)
 					
 					String[] sa = param.split("\\s*:\\s*");
 					if(sa.length == 2) {
 						params.add(new String[] { sa[0], null });
 						if(s1 < pix) {
 							regex.append('(').append(sa[1]).append(')');
 						} else { // in parameter section
 							regex.append(sa[0]).append('=').append('(').append(sa[1]).append(')').append('&');
 						}
 					} else {
 						sa = param.split("\\s*=\\s*");
 						if(sa.length == 2) {
 							
 //							TODO do we need to keep these constants in the regex at all? we have them in the params...
 							
 							for(int i = 0; i < sa[1].length(); i++) {
 								if(!Character.isLetterOrDigit(sa[1].charAt(i)) && sa[1].charAt(i) != '_') {
									throw new RoutingException("constants cann only contain letters, digets, and underscores");
 								}
 							}
 							if(!"models".equals(sa[0])) {
 								params.add(new String[] { sa[0], sa[1] });
 							}
 							if(s1 < pix) {
 								regex.append(sa[1]);
 							}
 						} else if(sa.length == 1) {
 							regex.append("(\\w+)");
 						} else { // >= 3 segments...
 							throw new RoutingException("invalid variable format starting at position " + s1); // TODO RouteFormatException...?
 						}
 					}
 				}
 				s0 = s2 + 1;
 			}
 			s1 = find(ca, '{', s0);
 		}
 		
 		if(s0 < ca.length) {
 			regex.append(ca, s0, ca.length-s0);
 		}
 		
 		char c = regex.charAt(regex.length()-1);
 		if(c == '?' || c =='&') {
 			regex.deleteCharAt(regex.length()-1);
 		}
 		
 		for(int i = 0; i < regex.length(); i++) {
 			if(regex.charAt(i) == '?' || regex.charAt(i) == '=') {
 				regex.insert(i++, '\\');
 			} else if(regex.charAt(i) == '(') {
 				while(regex.charAt(i) != ')') {
 					i++;
 				};
 			}
 		}
 		
 		setPattern(regex.toString());
 	}
 	
 	protected void setPattern(String pattern) {
 		if(pattern.indexOf('(') == -1) {
 			this.path = pattern;
 		} else {
 			try {
 				this.pattern = Pattern.compile(pattern);
 			} catch(PatternSyntaxException e) {
 				throw new RoutingException("invalid pattern format: " + pattern, e);
 			}
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return string;
 	}
 	
 }
