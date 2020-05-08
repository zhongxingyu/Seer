 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.transform;
 
 import com.gooddata.Constants;
 import com.gooddata.exception.InvalidParameterException;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.util.DateUtil;
 import com.gooddata.util.StringUtil;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.jexl2.Expression;
 import org.apache.commons.jexl2.JexlContext;
 import org.apache.commons.jexl2.JexlEngine;
 import org.apache.commons.jexl2.MapContext;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * GoodData transformation
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class Transformer {
 
     private static Logger l = Logger.getLogger(Transformer.class);
 
     private Expression[] expressions;
     private SourceSchema schema;
 
     protected Transformer(SourceSchema schema) {
         setSchema(schema);
     }
 
     public static Transformer create(SourceSchema schema) {
         return new Transformer(schema);
     }
 
     private final String[] fk = new String[]{};
     private final DateArithmetics da = new DateArithmetics();
 
     public String[] getHeader(boolean transform) {
         List<SourceColumn> columns = schema.getColumns();
         List<String> header = new ArrayList<String>();
         for(int i = 0; i < columns.size(); i++) {
             SourceColumn c = columns.get(i);
             if(!SourceColumn.LDM_TYPE_IGNORE.equalsIgnoreCase(c.getLdmType())) {
                 if(transform) {
                     header.add(StringUtil.toIdentifier(c.getName()));
                 }
                 else {
                     String t = c.getTransformation();
                     if(t == null) {
                         header.add(StringUtil.toIdentifier(c.getName()));
                     }
                 }
             }
         }
         return header.toArray(fk);
     }
 
     protected DecimalFormat decf = new DecimalFormat(Constants.DEFAULT_DEC_FMT_STRING);
     protected DecimalFormat intf = new DecimalFormat(Constants.DEFAULT_INT_FMT_STRING);
 
     protected JexlEngine jexl = new JexlEngine();
 
     /**
      * Runs all the row transformations
      * @param row the row data
      * @param dateLength
      * @return
      */
     public String[] transformRow(Object[] row, int dateLength) {
         try {
             if(row != null) {
                 List<SourceColumn> columns = schema.getColumns();
                 boolean computeIdentity = (schema.getIdentityColumn() >=0);
                 int idx = 0;
                 String key = "";
                 JexlContext jc = new MapContext();
                 for(int i=0; i<columns.size(); i++) {
                     SourceColumn c = columns.get(i);
                     String t = c.getTransformation();
                     if( t == null) {
                         // this is core (non-transformed) column
                         if(idx < row.length) {
                             if(SourceColumn.LDM_TYPE_FACT.equalsIgnoreCase(c.getLdmType())) {
                                 row[idx] = handleFact(row[idx]);
                             }
                             if(SourceColumn.LDM_TYPE_DATE.equalsIgnoreCase(c.getLdmType())) {
                                 row[idx] = handleDate(row[idx], c);
                                 row[idx] = cutStringDate(row[idx], dateLength);
                             }
                             // compute identity if required
                             if(computeIdentity && SourceColumn.LDM_TYPE_ATTRIBUTE.equalsIgnoreCase(c.getLdmType()) ||
                                     SourceColumn.LDM_TYPE_DATE.equalsIgnoreCase(c.getLdmType()) ||
                                     SourceColumn.LDM_TYPE_REFERENCE.equalsIgnoreCase(c.getLdmType())) {
                                 key += row[idx] + "|";
                             }
                             jc.set(c.getName(), row[idx]);
                             idx++;
                         }
                         else {
                             throw new InvalidParameterException("Transform: The schema "+schema.getName()+" contains different" +
                                     " number of columns than the processed row. This can happen if you forget to migrate the IDENTITY in " +
                                     "your schema (<dataType>IDENTITY</dataType> => <transformation>IDENTITY</transformation>).");
                         }
                     }
                 }
                 // insert identity var
                 if(computeIdentity) {
                     String identity = DigestUtils.md5Hex(key);
                     jc.set("IDENTITY", identity);
                 }
 
                 jc.set("GdcDateArithmetics", da);
 
 
                 idx = 0;
                 int cntWithoutIgnored = columns.size();
                 List<SourceColumn> ignored = schema.getIgnored();
                 if(ignored != null)
                     cntWithoutIgnored -= ignored.size();
                 List<String> nrow = new ArrayList<String>(cntWithoutIgnored);
                 for(int i=0; i < columns.size(); i++) {
                     SourceColumn c = columns.get(i);
                     String t = c.getTransformation();
                    //System.err.println("Column "+c.getName()+" tx:"+c.getTransformation());
                     if(!SourceColumn.LDM_TYPE_IGNORE.equalsIgnoreCase(c.getLdmType())) {
                         if( t == null) {
                             nrow.add(jc.get(c.getName()).toString());
                         }
                         else {
                             Object result = expressions[i].evaluate(jc);
                             String value = (result != null)?(result.toString()):("");
                             nrow.add(value);
                             jc.set(c.getName(), result);
                         }
                     }
                 }
                 return nrow.toArray(fk);
             }
             else {
                 throw new InvalidParameterException("The number of columns in the transformed row is different than in the schema.");
             }
         }
         catch(Exception e) {
             throw new InvalidParameterException("Transformation expression error", e);
         }
     }
 
     private Object cutStringDate(Object o, int dateLength) {
         if(dateLength > 0) {
             if(o !=null) {
                 if(o instanceof String) {
                     String val = (String) o;
                     if(val.length()>dateLength) {
                         return val.substring(0,dateLength);
                     }
                 }
                 else {
                     l.debug("Attempt to truncate non-string date representation in Transformer.transformRow.");
                 }
             }
             else {
                 return "";
             }
         }
         return o;
     }
 
     private Object handleDate(Object o, SourceColumn c) {
         if(o == null)
             return "";
         if(o instanceof DateTime) {
             DateTime v = (DateTime) o;
             DateTimeFormatter dtf = DateUtil.getDateFormatter(c.getFormat(), c.isDatetime());
             return dtf.print(v);
         }
         if(Constants.UNIX_DATE_FORMAT.equalsIgnoreCase(c.getFormat())) {
             if(o instanceof String)
                 return DateUtil.convertUnixTimeToString((String)o);
             else if(o instanceof Number)
                 return DateUtil.convertUnixTimeToString((Number)o);
             else
                 throw new InvalidParameterException("Can't convert UNIX time to date.");
         }
         return o;
     }
 
     private Object handleFact(Object o) {
         if(o == null)
             return "";
         if(o instanceof Number) {
             if(o instanceof Integer) {
                 return intf.format(o);
             }
             else if(o instanceof BigInteger) {
                 return intf.format(o);
             }
             else if(o instanceof Long) {
                 return intf.format(o);
             }
             else if(o instanceof Double) {
                 return decf.format(o);
             }
             else if(o instanceof Float) {
                 return decf.format(o);
             }
             else if(o instanceof BigDecimal) {
                 return decf.format(o);
             }
         }
         return o;
     }
 
 
     public SourceSchema getSchema() {
         return schema;
     }
 
     public void setSchema(SourceSchema schema) {
         try {
             if(schema != null) {
                 List<SourceColumn> columns = schema.getColumns();
                 if(columns != null & columns.size() > 0) {
                     Expression[] es =  new Expression[columns.size()];
                     for(int i=0; i<columns.size(); i++) {
                         SourceColumn c = columns.get(i);
                         if(c != null) {
                             String t = c.getTransformation();
                             if( t != null) {
                                 es[i] = jexl.createExpression(t);
                             }
                         }
                     }
                     setExpressions(es);
                     this.schema = schema;
                 }
                 else {
                     throw new InvalidParameterException("The Transformer requires a non-empty schema to run.");
                 }
             }
             else {
                 throw new InvalidParameterException("The Transformer requires a schema to run.");
             }
         }
         catch (Exception e) {
             throw new InvalidParameterException("Can't create transformation.", e);
         }
     }
 
     protected Expression[] getExpressions() {
         return expressions;
     }
 
     protected void setExpressions(Expression[] expressions) {
         this.expressions = expressions;
     }
 
 }
