 /**
  * Class containing common utility functions.
  * <p>
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  * <p>
  * All other source code is copyright Tresys Technology and licensed as below.
  * <p>
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  * <p>
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  * <p>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p>
  *    http://www.apache.org/licenses/LICENSE-2.0
  * <p>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package com.tresys.jalop.common;
 
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

 public class JALUtils {

 	public static final String SCHEMA_LOCATION = "src/main/java/com/tresys/jalop/schemas/applicationMetadataTypes.xsd";

	/**
	 * Creates a calendar with the current date and time to set the timestamp
	 * 
	 * @return	XMLGregorianCalendar with the current date
	 * @throws Exception
	 */
	public static XMLGregorianCalendar getCurrentTime() throws Exception {
		GregorianCalendar gc = new GregorianCalendar();
		XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
		return xmlCal;
	}
 }
