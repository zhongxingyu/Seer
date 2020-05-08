 /*
  * Copyright 2009 zaichu xiao
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtaa copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package zcu.xutil.sql;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Map;
 
 import zcu.xutil.utils.Accessor;
 
 /**
  * 命名参数SQL(named parameter SQL)是 用 : parameterName 代替 普通sql中的问号 ?.<br/>
  * 动态功能：当参数为null时 删除 原SQL和参数相关的某一部分.
  * 
  * <pre>
  * 语法元素 ：
  * string= 字符串
  * sqlpart=原语句部分
  * leftDeleteBound= #
  * rightDeleteBound= :#
  * parameterMark= :
  * parameterName= JavaIdentifier
  * 
  * 语法：&lt;sqlpart&gt; [leftDeleteBound] [sqlpart] parameterMark&lt;parameterName&gt; &lt;sqlpart&gt;[rightDeleteBound]
  * 
  * <b> 当 语句中存在leftDeleteBound 或    rightDeleteBound时 动态功能起作用.</b><br/>
  * 
  * Example1:   select * from tab where id=:id and #name=:name and ( #addr like :addr escape '/':#  or  level  in  (#:high, :low))
  *         当 high为null 时 产生: select * from tab where id=? and name=? and addr like ? escape '/' or  (level  in  (?))
  *         当 addr和 high为null 时 产生: select * from tab where id=? and name=? or  (level  in  (?))
  * 
  * 
  * Example2:  update … set #addr=:addr , #phone=:phone , #alias=:alias where id=:id  
  *         当 addr phone alias 的某一个为null 时将删除对应部分. 如 phone 为null 将产生：update … set  addr=？, alias=？ where id=？
  * </pre>
  * 
  *
  * @author <a href="mailto:zxiao@yeepay.com">xiao zaichu</a>
  */
 public final class NpSQL {
 	private static final int bitMask = 0xffffff;
 	public final String fullsql;
 	public final boolean insert;
 	private final String[] paramNames;
 	private final int[][] nullDeleteRange;
 
 	public NpSQL(String npsql) {
 		final int length = (npsql = npsql.trim()).length();
 		insert = npsql.regionMatches(true, 0, "insert", 0, 6);
 		ArrayList<String> list = new ArrayList<String>();
 		ArrayList<int[]> ranges = new ArrayList<int[]>();
 		StringBuilder parsed = new StringBuilder(length);
 		boolean inSingleQuote = false, inDoubleQuote = false, space = false;
 		int start = -1;
 		for (int i = 0; i < length; i++) {
 			char c = npsql.charAt(i);
 			if (inSingleQuote)
 				inSingleQuote = c != '\'';
 			else if (inDoubleQuote)
 				inDoubleQuote = c != '"';
 			else if (c <= ' ') {
 				if (space)
 					continue;
 				space = true;
 				c = ' ';
 			} else {
 				if (c == ':') {
 					int j = i + 1, ch = length > j ? npsql.charAt(j) : 0;
 					if (ch == ':')
 						++i;
 					else if (Character.isJavaIdentifierStart(ch)) {
 						do
 							j++;
 						while (j < length && Character.isJavaIdentifierPart(npsql.charAt(j)));
 						list.add(npsql.substring(i + 1, j));
 						ranges.add(start < 0 ? new int[] { parsed.length(), 0 } : new int[] {
 								start, parsed.length() + 1 });
 						start = -1;
 						c = '?'; // replace the parameter with a question mark
 						i = j - 1; // skip past the end of the parameter.
 					} else if (ch == '#') {
 						ranges.get(ranges.size() - 1)[1] = parsed.length();
 						++i;
 						continue;
 					}
 				} else if (c == '#') {
 					if (i + 1 >= length || npsql.charAt(i + 1) != '#') {
 						start = parsed.length();
 						continue;
 					}
 					++i;
 				} else {
 					inSingleQuote = c == '\'';
 					inDoubleQuote = c == '"';
 				}
 				space = false;
 			}
 			parsed.append(c);
 		}
 		this.fullsql = parsed.toString();
 		this.paramNames = list.toArray(new String[list.size()]);
 		space = true;
 		start = ranges.size();
 		while (--start >= 0) {
 			if (ranges.get(start)[1] <= 0)
 				ranges.set(start, null);
 			else
 				space = false;
 		}
 		nullDeleteRange = space ? null : ranges.toArray(new int[ranges.size()][]);
 	}
 
 	public HashSet<String> createNamesSet() {
 		return new HashSet<String>(Arrays.asList(paramNames));
 	}
 
 	public Object[] beanToParams(Object bean) {
 		int len = paramNames.length;
 		Object[] params = new Object[len];
 		EntityMap<Accessor> accessors = DBTool.getAllAccessor(bean.getClass());
 		Accessor accesssor;
 		while (--len >= 0) {
 			if ((accesssor = accessors.get(paramNames[len])) == null)
 				throw new IllegalArgumentException(bean.getClass().getName() + " hasn't property: " + paramNames[len]);
 			if ((params[len] = accesssor.getValue(bean)) == null)
 				params[len] = accesssor.getType();
 		}
 		return params;
 	}
 
 	public Object[] mapToParams(Map<String, ?> map) {
 		int len = paramNames.length;
 		Object[] params = new Object[len];
 		while (--len >= 0)
 			params[len] = map.get(paramNames[len]);
 		return params;
 	}
 
 	public SQLParams sqlFromBean(Object bean) {
 		Object[] params = beanToParams(bean);
 		return nullDeleteRange == null ? new SQLParams(fullsql, params) : build(params);
 	}
 
 	public SQLParams sqlFromMap(Map<String, ?> map) {
 		Object[] params = mapToParams(map);
 		return nullDeleteRange == null ? new SQLParams(fullsql, params) : build(params);
 	}
 
 	private SQLParams build(Object[] params) {
 		StringBuilder sqlbuilder = new StringBuilder(fullsql);
 		ArrayList<Object> objs = new ArrayList<Object>();
 		int len = params.length;
 		int[] mark;
 		while (--len >= 0) {
 			Object o = params[len];
 			if (!(o == null || o instanceof Class) || (mark = nullDeleteRange[len]) == null)
 				objs.add(params[len]);
 			else {
 				int[] ret = relocate(sqlbuilder, mark[0], mark[1]);
 				sqlbuilder.delete(ret[0], ret[1]);
 			}
 		}
 		Object[] array = new Object[len = objs.size()];
 		for (int i = 0; --len >= 0; i++)
 			array[i] = objs.get(len);
 		return new SQLParams(sqlbuilder.toString().trim(), array);
 	}
 
 	private int[] relocate(StringBuilder sqlbuilder, int start, int end) {
 		int i = leftOperBegin(sqlbuilder, start);
 		int j = rightOperEnd(sqlbuilder, end);
 		if (i < 0 && j < 0) {
 			if ((i &= bitMask) != 0 && (j &= bitMask) != 0) {
 				char c;
 				do {
 					if ((c = sqlbuilder.charAt(start)) > ' ' && c != '?')
 						return relocate(sqlbuilder, i, j);
 				} while (++start < end);
 			}
 			sqlbuilder.setLength(end);
			throw new IllegalArgumentException(sqlbuilder.append(" ...  can't remove at end.").toString());
 		}
 		if ((i & ~bitMask) >= (j & ~bitMask))
 			start = i & bitMask;
 		else
 			end = j & bitMask;
 		return new int[] { start, end };
 	}
 
 	private static int rightOperEnd(StringBuilder sb, int i) {
 		char c;
 		for (int len = sb.length(); i < len; i++) {
 			if ((c = sb.charAt(i)) > ' ') {
 				if (c == ')')
 					return ++i | Integer.MIN_VALUE;
 				if (c == ',')
 					return ++i | Integer.MIN_VALUE >>> 3;
 				if ((c |= 32) == 'o') {
 					if ((sb.charAt(++i) | 32) == 'r' && sb.charAt(++i) <= ' ')
 						return i | Integer.MIN_VALUE >>> 2;
 				} else if (c == 'a') {
 					if ((sb.charAt(++i) | 32) == 'n' && (sb.charAt(++i) | 32) == 'd' && sb.charAt(++i) <= ' ')
 						return i | Integer.MIN_VALUE >>> 1;
 				}
 				return Integer.MIN_VALUE;
 			}
 		}
 		return Integer.MIN_VALUE;
 	}
 
 	private static int leftOperBegin(StringBuilder sb, int i) {
 		char c;
 		while (--i > 0) {
 			if ((c = sb.charAt(i)) > ' ') {
 				if (c == '(')
 					return i | Integer.MIN_VALUE;
 				if (c == ',')
 					return i | Integer.MIN_VALUE >>> 3;
 				if ((c |= 32) == 'r') {
 					if ((sb.charAt(--i) | 32) == 'o' && sb.charAt(i - 1) <= ' ')
 						return i | Integer.MIN_VALUE >>> 2;
 				} else if (c == 'd') {
 					if ((sb.charAt(--i) | 32) == 'n' && (sb.charAt(--i) | 32) == 'a' && sb.charAt(i - 1) <= ' ')
 						return i | Integer.MIN_VALUE >>> 1;
 				}
 				return Integer.MIN_VALUE;
 			}
 		}
 		return Integer.MIN_VALUE;
 	}
 }
