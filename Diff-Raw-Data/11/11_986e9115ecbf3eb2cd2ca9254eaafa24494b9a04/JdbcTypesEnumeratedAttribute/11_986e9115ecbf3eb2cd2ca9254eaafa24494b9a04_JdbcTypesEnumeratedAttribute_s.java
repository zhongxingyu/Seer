 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: JdbcTypesEnumeratedAttribute.java,v 1.1 2004-07-30 16:37:17 aye.thu Exp $
  */
 package com.netspective.axiom.sql;
 
 import com.netspective.commons.xdm.XdmEnumeratedAttribute;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.lang.reflect.Field;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 public class JdbcTypesEnumeratedAttribute extends XdmEnumeratedAttribute
 {
     private static final Log log = LogFactory.getLog(JdbcTypesEnumeratedAttribute.class);
    private static Map map;
 
     public String[] getValues()
     {
        if (map == null)
            map = createJdbcTypeMap();
         Object[] keys = map.keySet().toArray();
         String[] jdbcTypeNames = new String[keys.length];
         for (int i=0; i < keys.length; i++)
         {
             jdbcTypeNames[i] = (String) keys[i];
         }
         return jdbcTypeNames;
     }
 
     /**
      * Gets the java.sql.Types value related to this attribute. 
      *
      * @return
      */
     public int getJdbcValue()
     {
         // Return the JDBC type name
         if (map.containsKey(getValue()))
             return  ((Integer) map.get(getValue())).intValue();
         else
             return java.sql.Types.OTHER;
     }
 
     /**
      *  Returns the name-value mapping map of all JDBC SQL types.
      *
      * @return jdbc sql type mappings
      */
    private Map createJdbcTypeMap()
     {
         // Use reflection to populate a map of int values to names
         Map map = new HashMap();
         // Get all field in java.sql.Types
         Field[] fields = java.sql.Types.class.getFields();
         for (int i = 0; i < fields.length; i++)
         {
             try
             {
                 // Get field name
                 String name = fields[i].getName();
                 // Get field value
                 Integer value = (Integer) fields[i].get(null);
                 System.out.println(name + " " + value);
                 // Add to map
                 map.put(name, value);
             }
             catch (IllegalAccessException e)
             {
                 log.error(e);
             }
         }
         return map;
     }
 
 }
