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
 * $Id: ValidationUtils.java,v 1.2 2003-05-13 02:12:20 shahid.shah Exp $
  */
 
 package com.netspective.commons.validate;
 
 import java.util.Locale;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import org.apache.oro.text.perl.Perl5Util;
 
 import com.netspective.commons.value.ValueContext;
 
 public class ValidationUtils
 {
     public static final ValidationContext createValidationContext()
     {
         return new BasicValidationContext();
     }
 
     public static final ValidationContext createValidationContext(ValueContext vc)
     {
         return new BasicValidationContext(vc);
     }
 
     /**
      * Delimiter to put around a regular expression
      * following Perl 5 syntax.
      */
     public final static String REGEXP_DELIM = "/";
 
     /**
      * <p>Checks if the field isn't null and length of the field is greater than zero not
      * including whitespace.</p>
      *
      * @param 	value 		The value validation is being performed on.
      */
     public static boolean isBlankOrNull(String value)
     {
         return ((value == null) || (value.trim().length() == 0));
     }
 
     /**
      * <p>Checks if the value matches the regular expression.</p>
      *
      * @param 	value 		The value validation is being performed on.
      * @param 	regexp		The regular expression.
      */
     public static boolean matchRegexp(String value, String regexp)
     {
         boolean match = false;
 
         if (regexp != null && regexp.length() > 0)
         {
             Perl5Util r = new Perl5Util();
             match = r.match(regexp, value);
         }
 
         return match;
     }
 
 
     /**
      * <p>Checks if the value can safely be converted to a byte primitive.</p>
      *
      * @param 	value 		The value validation is being performed on.
      */
     public static boolean isByte(String value)
     {
         return (FormattingUtils.formatByte(value) != null);
     }
 
     /**
      * <p>Checks if the value can safely be converted to a short primitive.</p>
      *
      * @param 	value 		The value validation is being performed on.
      */
     public static boolean isShort(String value)
     {
         return (FormattingUtils.formatShort(value) != null);
     }
 
     /**
      * <p>Checks if the value can safely be converted to a int primitive.</p>
      *
      * @param 	value 		The value validation is being performed on.
      */
     public static boolean isInt(String value)
     {
         return (FormattingUtils.formatInt(value) != null);
     }
 
     /**
      * <p>Checks if the value can safely be converted to a long primitive.</p>
      *
      * @param 	value 		The value validation is being performed on.
      */
     public static boolean isLong(String value)
     {
         return (FormattingUtils.formatLong(value) != null);
     }
 
     /**
      * <p>Checks if the value can safely be converted to a float primitive.</p>
      *
      * @param 	value 		The value validation is being performed on.
      */
     public static boolean isFloat(String value)
     {
         return (FormattingUtils.formatFloat(value) != null);
     }
 
     /**
      * <p>Checks if the value can safely be converted to a double primitive.</p>
      *
      * @param 	value 		The value validation is being performed on.
      */
     public static boolean isDouble(String value)
     {
         return (FormattingUtils.formatDouble(value) != null);
     }
 
     /**
      * <p>Checks if the field is a valid date.  The <code>Locale</code> is
      * used with <code>java.text.DateFormat</code>.  The setLenient method
      * is set to <code>false</code> for all.</p>
      *
      * @param 	value 		The value validation is being performed on.
      * @param 	locale	    The locale to use for the date format, defaults to the default system default if null.
      */
     public static boolean isDate(String value, Locale locale)
     {
         boolean bValid = true;
 
         if (value != null)
         {
             try
             {
                 DateFormat formatter = null;
                 if (locale != null)
                 {
                     formatter = DateFormat.getDateInstance(DateFormat.SHORT, locale);
                 }
                 else
                 {
                     formatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
                 }
 
                 formatter.setLenient(false);
 
                 formatter.parse(value);
             }
             catch (ParseException e)
             {
                 bValid = false;
             }
         }
         else
         {
             bValid = false;
         }
 
         return bValid;
     }
 
     /**
      * <p>Checks if the field is a valid date.  The pattern is used with
      * <code>java.text.SimpleDateFormat</code>.  If strict is true, then the
      * length will be checked so '2/12/1999' will not pass validation with
      * the format 'MM/dd/yyyy' because the month isn't two digits.
      * The setLenient method is set to <code>false</code> for all.</p>
      *
      * @param 	value 		The value validation is being performed on.
      * @param 	datePattern	The pattern passed to <code>SimpleDateFormat</code>.
      * @param 	strict	        Whether or not to have an exact match of the datePattern.
      */
     public static boolean isDate(String value, String datePattern, boolean strict)
     {
 
         boolean bValid = true;
 
         if (value != null && datePattern != null && datePattern.length() > 0)
         {
             try
             {
                 SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
                 formatter.setLenient(false);
 
                 formatter.parse(value);
 
                 if (strict)
                 {
                     if (datePattern.length() != value.length())
                     {
                         bValid = false;
                     }
                 }
 
             }
             catch (ParseException e)
             {
                 bValid = false;
             }
         }
         else
         {
             bValid = false;
         }
 
         return bValid;
     }
 
 
     /**
      * <p>Checks if a value is within a range (min &amp; max specified
      * in the vars attribute).</p>
      *
      * @param 	value 		The value validation is being performed on.
      * @param 	min		The minimum value of the range.
      * @param 	max		The maximum value of the range.
      */
     public static boolean isInRange(int value, int min, int max)
     {
         return ((value >= min) && (value <= max));
     }
 
     /**
      * <p>Checks if a value is within a range (min &amp; max specified
      * in the vars attribute).</p>
      *
      * @param 	value 		The value validation is being performed on.
      * @param 	min		The minimum value of the range.
      * @param 	max		The maximum value of the range.
      */
     public static boolean isInRange(float value, float min, float max)
     {
         return ((value >= min) && (value <= max));
     }
 
     /**
      * <p>Checks if a value is within a range (min &amp; max specified
      * in the vars attribute).</p>
      *
      * @param 	value 		The value validation is being performed on.
      * @param 	min		The minimum value of the range.
      * @param 	max		The maximum value of the range.
      */
     public static boolean isInRange(short value, short min, short max)
     {
         return ((value >= min) && (value <= max));
     }
 
     /**
      * <p>Checks if a value is within a range (min &amp; max specified
      * in the vars attribute).</p>
      *
      * @param 	value 		The value validation is being performed on.
      * @param 	min		The minimum value of the range.
      * @param 	max		The maximum value of the range.
      */
     public static boolean isInRange(double value, double min, double max)
     {
         return ((value >= min) && (value <= max));
     }
 
     /**
      * <p>Checks if the field is a valid credit card number.</p>
      * <p>Translated to Java by Ted Husted (<a href="mailto:husted@apache.org">husted@apache.org</a>).<br>
      * &nbsp;&nbsp;&nbsp; Reference Sean M. Burke's script at http://www.ling.nwu.edu/~sburke/pub/luhn_lib.pl</p>
      *
      * @param 	value 		The value validation is being performed on.
      */
     public static boolean isCreditCard(String value)
     {
         return (validateCreditCardLuhnCheck(value) && validateCreditCardPrefixCheck(value));
     }
 
     /**
      * <p>Checks for a valid credit card number.</p>
      * <p>Translated to Java by Ted Husted (<a href="mailto:husted@apache.org">husted@apache.org</a>).<br>
      * &nbsp;&nbsp;&nbsp; Reference Sean M. Burke's script at http://www.ling.nwu.edu/~sburke/pub/luhn_lib.pl</p>
      *
      * @param 	cardNumber 		Credit Card Number
      */
     protected static boolean validateCreditCardLuhnCheck(String cardNumber)
     {
         // number must be validated as 0..9 numeric first!!
         int no_digit = cardNumber.length();
         int oddoeven = no_digit & 1;
         long sum = 0;
         for (int count = 0; count < no_digit; count++)
         {
             int digit = 0;
             try
             {
                 digit = Integer.parseInt(String.valueOf(cardNumber.charAt(count)));
             }
             catch (NumberFormatException e)
             {
                 return false;
             }
             if (((count & 1) ^ oddoeven) == 0)
             { // not
                 digit *= 2;
                 if (digit > 9)
                 {
                     digit -= 9;
                 }
             }
             sum += digit;
         }
         if (sum == 0)
         {
             return false;
         }
 
         if (sum % 10 == 0)
         {
             return true;
         }
 
         return false;
     }
 
     /**
      * <p>Checks for a valid credit card number.</p>
      * <p>Translated to Java by Ted Husted (<a href="mailto:husted@apache.org">husted@apache.org</a>).<br>
      * &nbsp;&nbsp;&nbsp; Reference Sean M. Burke's script at http://www.ling.nwu.edu/~sburke/pub/luhn_lib.pl</p>
      *
      * @param 	cardNumber 		Credit Card Number
      */
     protected static boolean validateCreditCardPrefixCheck(String cardNumber)
     {
         final String AX_PREFIX = "34,37,";
         final String VS_PREFIX = "4";
         final String MC_PREFIX = "51,52,53,54,55,";
         final String DS_PREFIX = "6011";
 
         int length = cardNumber.length();
         if (length < 13)
         {
             return false;
         }
 
         boolean valid = false;
         int cardType = 0;
 
         String prefix2 = cardNumber.substring(0, 2) + ",";
 
         if (AX_PREFIX.indexOf(prefix2) != -1)
         {
             cardType = 3;
         }
         if (cardNumber.substring(0, 1).equals(VS_PREFIX))
         {
             cardType = 4;
         }
         if (MC_PREFIX.indexOf(prefix2) != -1)
         {
             cardType = 5;
         }
         if (cardNumber.substring(0, 4).equals(DS_PREFIX))
         {
             cardType = 6;
         }
 
         if ((cardType == 3) && (length == 15))
         {
             valid = true;
         }
         if ((cardType == 4) && ((length == 13) || (length == 16)))
         {
             valid = true;
         }
         if ((cardType == 5) && (length == 16))
         {
             valid = true;
         }
         if ((cardType == 6) && (length == 16))
         {
             valid = true;
         }
 
         return valid;
     }
 
     /**
      * <p>Checks if a field has a valid e-mail address.</p>
      * <p>Based on a script by Sandeep V. Tamhankar (stamhankar@hotmail.com),
      * http://javascript.internet.com</p>
      *
      * @param 	value 		The value validation is being performed on.
      */
     public static boolean isEmail(String value)
     {
         boolean bValid = true;
 
         try
         {
             String specialChars = "\\(\\)<>@,;:\\\\\\\"\\.\\[\\]";
             String validChars = "[^\\s" + specialChars + "]";
             String quotedUser = "(\"[^\"]*\")";
             String atom = validChars + '+';
             String word = "(" + atom + "|" + quotedUser + ")";
 
             // Each pattern must be surrounded by /
            String emailPat = getDelimittedRegexp("^(.+)@(.+)$");
            String ipDomainPat = getDelimittedRegexp("^(\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})$");
            String userPat = getDelimittedRegexp("^" + word + "(\\." + word + ")*$");
            String domainPat = getDelimittedRegexp("^" + atom + "(\\." + atom + ")*$");
            String atomPat = getDelimittedRegexp("(" + atom + ")");
 
             Perl5Util matchEmailPat = new Perl5Util();
             Perl5Util matchUserPat = new Perl5Util();
             Perl5Util matchIPPat = new Perl5Util();
             Perl5Util matchDomainPat = new Perl5Util();
             Perl5Util matchAtomPat = new Perl5Util();
 
             boolean ipAddress = false;
             boolean symbolic = false;
 
             // Check the whole email address structure
             bValid = matchEmailPat.match(emailPat, value);
 
             // Check the user component of the email address
             if (bValid)
             {
                 String user = matchEmailPat.group(1);
 
                 // See if "user" is valid
                 bValid = matchUserPat.match(userPat, user);
             }
 
             // Check the domain component of the email address
             if (bValid)
             {
                 String domain = matchEmailPat.group(2);
 
                 // check if domain is IP address or symbolic
                 ipAddress = matchIPPat.match(ipDomainPat, domain);
 
                 if (ipAddress)
                 {
                     // this is an IP address so check components
                     for (int i = 1; i <= 4; i++)
                     {
                         String ipSegment = matchIPPat.group(i);
                         if (ipSegment != null && ipSegment.length() > 0)
                         {
                             int iIpSegment = 0;
                             try
                             {
                                 iIpSegment = Integer.parseInt(ipSegment);
                             }
                             catch (Exception e)
                             {
                                 bValid = false;
                             }
 
                             if (iIpSegment > 255)
                             {
                                 bValid = false;
                             }
                         }
                         else
                         {
                             bValid = false;
                         }
                     }
                 }
                 else
                 {
                     // Domain is symbolic name
                     symbolic = matchDomainPat.match(domainPat, domain);
                 }
 
                 if (symbolic)
                 {
                     // this is a symbolic domain so check components
                     String[] domainSegment = new String[10];
                     boolean match = true;
                     int i = 0;
                     int l = 0;
 
                     while (match)
                     {
                         match = matchAtomPat.match(atomPat, domain);
                         if (match)
                         {
                             domainSegment[i] = matchAtomPat.group(1);
                             l = domainSegment[i].length() + 1;
                             domain = (l >= domain.length()) ? "" : domain.substring(l);
                             i++;
                         }
                     }
 
                     int len = i;
                     if (domainSegment[len - 1].length() < 2 || domainSegment[len - 1].length() > 4)
                     {
                         bValid = false;
                     }
 
                     // Make sure there's a host name preceding the domain.
                     if (len < 2)
                     {
                         bValid = false;
                     }
                 }
             }
         }
         catch (Exception e)
         {
             bValid = false;
         }
 
         return bValid;
     }
 
     /**
      * <p>Checks if the value's length is less than or equal to the max.</p>
      *
      * @param 	value 		The value validation is being performed on.
      * @param 	max		The maximum length.
      */
     public static boolean maxLength(String value, int max)
     {
         return (value.length() <= max);
     }
 
     /**
      * <p>Checks if the value's length is greater than or equal to the min.</p>
      *
      * @param 	value 		The value validation is being performed on.
      * @param 	min		The minimum length.
      */
     public static boolean minLength(String value, int min)
     {
         return (value.length() >= min);
     }
 
 
     /**
      * Adds a '/' on either side of the regular expression.
      */
    protected static String getDelimittedRegexp(String regexp)
     {
         return (REGEXP_DELIM + regexp + REGEXP_DELIM);
     }
 
 }
