 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2005  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 
 package net.sf.clirr.core;
 
 
 /**
  * Describes an API change.
  *
  * @author Lars
  */
 public final class ApiDifference
 {
     /**
      * Object representing the message text to be output (or null if
      * the constructor which takes a message string directly is used).
      */
     private Message message = null;
 
     /** human readable change report. */
     private String report;
 
     /**
      * severity of the change in terms of binary compatibility,
      * as determined by clirr.
      */
     private Severity binaryCompatibilitySeverity;
 
     /**
      * severity of the change in terms of source compatibility,
      * as determined by clirr.
      */
     private Severity sourceCompatibilitySeverity;
 
     /** The fully qualified class name that is affected by the API change. */
     private String affectedClass;
 
     /**
      * The method that is affected, if any.
      * <p/>
      * The content is the method name plus the fully qualified
      * parameter types separated by comma and space and enclosed in
      * brackets, e.g. "doStuff(java.lang.String, int)".
      * <p/>
      * This value is <code>null</code> if no single method is
      * affected, i.e. if the
      * api change affects a field or is global
      * (like "class is now final").
      */
     private String affectedMethod;
 
     /**
      * The field that is affected, if any.
      * <p/>
      * The content is the field name, e.g. "someValue".
      * Type information for the field is not available.
      * <p/>
      * This value is <code>null</code> if no single field is
      * affected, i.e. if the
      * api change affects a method or is global
      * (like "class is now final").
      */
     private String affectedField;
 
     /**
      * The set of additional parameters that are available for use
      * when building the actual message description. These vary depending
      * upon the actual difference being reported.
      */
     private String[] extraInfo;
 
     /**
      * Invokes the two-severity-level version of this constructor.
      */
     public ApiDifference(
         Message message,
         Severity severity,
         String clazz,
         String method,
         String field,
         String[] args)
     {
         this(message, severity, severity, clazz, method, field, args);
     }
 
     /**
      * Create a new API difference representation.
      *
      * @param message is the key of a human readable string describing the
      * change that was made.
      *
      * @param binarySeverity the severity in terms of binary compatibility,
      * must be non-null.
      *
      * @param sourceSeverity the severity in terms of source code compatibility,
      * must be non-null.
      *
      * @param clazz is the fully-qualified name of the class in which the
      * change occurred, must be non-null.
      *
      * @param method the method signature of the method that changed,
      * <code>null</code> if no method was affected.
      *
      * @param field the field name where the change occured, <code>null</code>
      * if no field was affected.
      *
      * @param args is a set of additional change-specific strings which are
      * made available for the message description string to reference via
      * the standard {n} syntax.
      */
     public ApiDifference(
         Message message,
         Severity binarySeverity, Severity sourceSeverity,
         String clazz, String method, String field,
         String[] args)
     {
         checkNonNull(message);
         checkNonNull(binarySeverity);
         checkNonNull(sourceSeverity);
         checkNonNull(clazz);
 
         this.message = message;
         this.binaryCompatibilitySeverity = binarySeverity;
         this.sourceCompatibilitySeverity = sourceSeverity;
         this.affectedClass = clazz;
         this.affectedField = field;
         this.affectedMethod = method;
         this.extraInfo = args;
     }
 
     /**
      * Trivial utility method to verify that a specific object is non-null.
      */
     private void checkNonNull(Object o)
     {
         if (o == null)
         {
             throw new IllegalArgumentException();
         }
     }
 
     /**
      * Return the message object (if any) associated with this difference.
      * <p>
      * Checks which support the "new" message API will provide ApiDifference
      * objects with non-null message objects.
      */
     public Message getMessage()
     {
         return message;
     }
 
     /**
      * The Severity of the API difference in terms of binary compatibility.
      * ERROR means that clients will definitely break, WARNING means that
      * clients may break, depending on how they use the library.
      * See the eclipse paper for further explanation.
      *
      * @return the severity of the API difference in terms of binary compatibility.
      */
     public Severity getBinaryCompatibilitySeverity()
     {
         return binaryCompatibilitySeverity;
     }
 
     /**
      * The Severity of the API difference in terms of source compatibility.
      * Sometimes this is different than {@link #getBinaryCompatibilitySeverity
      * binary compatibility severity}, for example adding a checked exception
      * to a method signature is binary compatible but not source compatible.
      * ERROR means that clients will definitely break, WARNING means that
      * clients may break, depending on how they use the library.
      * See the eclipse paper for further explanation.
      *
      * @return the severity of the API difference in terms of source code
      * compatibility.
      */
     public Severity getSourceCompatibilitySeverity()
     {
         return sourceCompatibilitySeverity;
     }
 
     /**
      * Return the maximum of the binary and source compatibility severities.
      */
     public Severity getMaximumSeverity()
     {
         final Severity src = getSourceCompatibilitySeverity();
         final Severity bin = getBinaryCompatibilitySeverity();
         return src.compareTo(bin) < 0 ? bin : src;
     }
 
     /**
      * Human readable api change description.
      *
      * @return a human readable description of this API difference.
      */
     public String getReport(MessageTranslator translator)
     {
         if (report != null)
         {
             return report;
         }
 
         String desc = translator.getDesc(message);
         int nArgs = 0;
         if (extraInfo != null)
         {
             nArgs = extraInfo.length;
         }
         String[] strings = new String[nArgs + 3];
         strings[0] = affectedClass;
         strings[1] = affectedMethod;
         strings[2] = affectedField;
         for (int i = 0; i < nArgs; ++i)
         {
             strings[i + 3] = extraInfo[i];
         }
 
         return java.text.MessageFormat.format(desc, strings);
     }
 
     /**
      * The fully qualified class name of the class that has changed.
      * @return fully qualified class name of the class that has changed.
      */
     public String getAffectedClass()
     {
         return affectedClass;
     }
 
     /**
      * Method signature of the method that has changed, if any.
      * @return method signature or <code>null</code> if no method is affected.
      */
     public String getAffectedMethod()
     {
         return affectedMethod;
     }
 
     /**
      * Field name of the field that has changed, if any.
      * @return field name or <code>null</code> if no field is affected.
      */
     public String getAffectedField()
     {
         return affectedField;
     }
 
     /**
      * {@inheritDoc}
      */
     public String toString()
     {
         StringBuffer buf = new StringBuffer();
         buf.append(message.getId());
         appendCommonData(buf);
         return buf.toString();
     }
 
     /**
      * Get a human-readable description of this object. Intended for use by
      * the unit tests.
      */
     public String toString(MessageTranslator translator)
     {
         StringBuffer buf = new StringBuffer();
         buf.append(getReport(translator));
         appendCommonData(buf);
         return buf.toString();
     }
 
     /**
      * Build a string containing a string representation of most of the
      * fields in this object, but not the message-id or the string
      * translation thereof.
      */
     private void appendCommonData(StringBuffer buf)
     {
         buf.append(" (");
         buf.append(binaryCompatibilitySeverity);
 
         if (sourceCompatibilitySeverity != binaryCompatibilitySeverity)
         {
             buf.append(",");
             buf.append(sourceCompatibilitySeverity);
         }
 
         buf.append(") - ");
         buf.append(affectedClass);
         buf.append("[");
         buf.append(affectedField);
         buf.append("/");
         buf.append(affectedMethod);
         buf.append("]");
     }
 }
