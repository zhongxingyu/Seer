 /*
  * RapidEnv: ExceptionMap.java
  *
  * Copyright (C) 2011 Martin Bluemel
  *
  * Creation Date: 12/25/2011
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the
  * GNU Lesser General Public License as published by the Free Software Foundation;
  * either version 3 of the License, or (at your option) any later version.
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  * You should have received a copies of the GNU Lesser General Public License and the
  * GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.rapidbeans.rapidenv.config.cmd;
 
 
 import org.rapidbeans.core.type.TypeRapidBean;
 import org.rapidbeans.datasource.Document;
 import org.rapidbeans.rapidenv.RapidEnvException;
 
 
 /**
  * The Exception Map.
  */
 public class ExceptionMap extends RapidBeanBaseExceptionMap {
 
 	public static final int ERRORCODE_HTTP_DOWNLOAD = 1001;
 	public static final int ERRORCODE_UNKNOWN_PROP_OR_UNIT = 1002;
 	public static final int ERRORCODE_AMBIGOUUS_NAME = 1003;
 	public static final int ERRORCODE_HTTP_DOWNLOAD_CONNECTION_TIMEOUT = 1004;
 	public static final int ERRORCODE_HTTP_DOWNLOAD_CONNECTION_PROBLEM = 1005;
 	public static final int ERRORCODE_DOWNLOAD_FORBIDDEN = 1007;
 	public static final int INFOCODE_DOWNLOAD_MANUAL_REQUIRED = 20001;
 
 	/**
 	 * default constructor.
 	 */
 	public ExceptionMap() {
 		super();
 	}
 
 	/**
 	 * constructor out of a string.
 	 * @param s the string
 	 */
 	public ExceptionMap(final String s) {
 		super(s);
 	}
 
 	/**
 	 * constructor out of a string array.
 	 * @param sa the string array
 	 */
 	public ExceptionMap(final String[] sa) {
 		super(sa);
 	}
 
 	/**
 	 * the bean's type (class variable).
 	 */
 	private static TypeRapidBean type = TypeRapidBean.createInstance(ExceptionMap.class);
 
 	/**
 	 * @return the RapidBean's type
 	 */
 	public TypeRapidBean getType() {
 		return type;
 	}
 
 	/**
 	 * Factory method to load the Exception map.
 	 *
 	 * @return a new Exception map instance loaded from resource file
 	 *         org/rapidbeans/rapidenv/commandlineErrorMappings.xml
 	 */
 	public static ExceptionMap load() {
 		Document doc = new Document("map",
 				TypeRapidBean.forName("org.rapidbeans.rapidenv.config.cmd.ExceptionMap"),
 				null,
 				ExceptionMap.class.getClassLoader().getResourceAsStream("org/rapidbeans/rapidenv/commandlineErrorMappings.xml"));
 		return (ExceptionMap) doc.getRoot();
 	}
 
 	/**
 	 * Find mapping for an RapidEnvException.
 	 *
 	 * @param e the exception to map
 	 *
 	 * @return the mapping or null if not mapped
 	 */
 	public ExceptionMapping map(final RapidEnvException e) {
 		for (final ExceptionMapping mapping : getExceptions()) {
 			if (mapping.getErrorcodeAsInteger() == e.getErrorcode()) {
 				mapping.setMappedException(e);
 				return mapping;
 			}
 		}
 		return null;
 	}
 }
