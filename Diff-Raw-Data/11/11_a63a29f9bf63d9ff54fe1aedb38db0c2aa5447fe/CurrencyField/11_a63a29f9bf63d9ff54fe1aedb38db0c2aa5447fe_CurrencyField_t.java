 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only
  *    (as Java .class files or a .jar file containing the .class files) and only
  *    as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software
  *    development kit, other library, or development tool without written consent of
  *    Netspective Corporation. Any modified form of The Software is bound by
  *    these same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective
  *    Corporation and may not be used to endorse products derived from The
  *    Software without without written consent of Netspective Corporation. "Sparx"
  *    and "Netspective" may not appear in the names of products derived from The
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind.
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: CurrencyField.java,v 1.10 2004-03-23 18:15:15 aye.thu Exp $
  */
 
 package com.netspective.sparx.form.field.type;
 
 import com.netspective.commons.value.exception.ValueException;
 import com.netspective.commons.xdm.XdmEnumeratedAttribute;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFieldValue;
 
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.util.Locale;
 
 /**
  * NOTE: JSDK 1.4 and above supports the java.util.Currency class. Right now, we're not using it
  * but we might want to use it.
  */
 public class CurrencyField extends TextField
 {
     public static class NegativePosLocation extends XdmEnumeratedAttribute
     {
         public static final String[] VALUES = new String[] { "before-symbol", "after-symbol" };
         public static final int BEFORE_SYMBOL = 0;
         public static final int AFTER_SYMBOL = 1;
 
         public NegativePosLocation()
         {
         }
 
         public NegativePosLocation(int valueIndex)
         {
             super(valueIndex);
         }
 
         public String[] getValues()
         {
             return VALUES;
         }
     }
 
     public class CurrencyFieldState extends State
     {
         public class CurrencyFieldValue extends BasicStateValue
         {
             public Class getValueHolderClass()
             {
                 return Double.class;
             }
 
             public String getTextValue()
             {
                 if (!isValid())
                     return getInvalidText();
 
                 if (getValue() instanceof Double)
                 {
                     // sets the format for the currency value bacause toString() of a double produces
                     // exponential formats when the value is large
                    DecimalFormat format = new DecimalFormat("###0.##");
                     return format.format(((Double)getValue()).doubleValue());
                 }
                 return getValue() != null ? getValue().toString() : null;
             }
 
             /**
              *
              * @param value the value passed here should have been verified already using either the display pattern or the submit pattern
              * @throws ValueException
              */
             public void setTextValue(String value) throws ValueException
             {
                 if(value == null || value.length() == 0)
                 {
                     setValue((Double) null);
                     return;
                 }
 
                 try
                 {
                     // NumberFormat's parse() method can be used if the 'value' was already in the Locale's format.
                     // e.g. English in US Locale dictates that a currency value format is in $123.11 for
                     // positive values and ($123.11) for negative values.   So, the following values -$123.11, $-123.11,
                     // 123.11, and -123.11 will not work and will throw a ParseException.
                     //Number number = format.parse(value);
                     if (value.indexOf(currencySymbol) >= 0)
                     {
                         // display value can contain the currency symbol
                         setValue(value);
                     }
                     else
                     {
                         // submit value does not contain the currency symbol
                         setValue(new Double(value));
                     }
                 }
                 catch (Exception e)
                 {
                     setInvalidText(value);
                     invalidate(getDialogContext(), getErrorCaption().getTextValue(getDialogContext()) + " requires a value in currency format ("+ e.getMessage() +").");
                 }
             }
         }
 
         public CurrencyFieldState(DialogContext dc, DialogField field)
         {
             super(dc, field);
         }
 
         public DialogFieldValue constructValueInstance()
         {
             return new CurrencyFieldValue();
         }
     }
 
     private Locale locale = Locale.getDefault() != null ? Locale.getDefault() : Locale.US;
     private NumberFormat format;
     private String currencySymbol;
     private char decimalSymbol;
     private int decimalsRequired = -1;
     private NegativePosLocation negativePos = new NegativePosLocation(NegativePosLocation.BEFORE_SYMBOL);
 
     public CurrencyField()
     {
         setDecimalsRequired(2);
         setLocale(Locale.getDefault());
         setNegativePos(new NegativePosLocation(NegativePosLocation.BEFORE_SYMBOL));
     }
 
     public DialogField.State constructStateInstance(DialogContext dc)
     {
         return new CurrencyFieldState(dc, this);
     }
 
     public Class getStateClass()
     {
         return CurrencyFieldState.class;
     }
 
     public Class getStateValueClass()
     {
         return CurrencyFieldState.CurrencyFieldValue.class;
     }
 
     protected void setupPatterns()
     {
         if(decimalsRequired < 0)
             setDecimalsRequired(0);
 
         format = NumberFormat.getCurrencyInstance(locale);
         if(decimalsRequired >= 0)
         {
             format.setMinimumFractionDigits(decimalsRequired);
             format.setMaximumFractionDigits(decimalsRequired);
         }
 
         DecimalFormatSymbols cSymbol = new DecimalFormatSymbols(locale);
         currencySymbol = cSymbol.getCurrencySymbol();
         decimalSymbol = cSymbol.getDecimalSeparator();
 
         String decimalExpr = "";
         if(decimalsRequired > 0)
             decimalExpr = "(["+ decimalSymbol +"][\\d]{1," + decimalsRequired + "})?";
         else
             decimalExpr = "";
 
         if(negativePos == null || negativePos.getValueIndex() == NegativePosLocation.BEFORE_SYMBOL)
         {
             setRegExpr("^([-])?([\\" + currencySymbol + "])?([\\d]+)" + decimalExpr + "$");
             setDisplayPattern("s/^([-])?([\\" + currencySymbol +
                     "])?([\\d]+)" + decimalExpr + "$/$1\\" + currencySymbol + "$3$4/g");
             setSubmitPattern("s/" + "^([-])?([\\" + currencySymbol + "])?([\\d]+)" + decimalExpr +
                     "$/$1$3$4/g");
             setInvalidRegExMessage("Currency values must have the format " +
                     currencySymbol + "xxx.xx for positive values and " +
                    "-" + currencySymbol + "xxx.xx for negative values. (decimals = " + decimalsRequired + ")");
         }
         else if(negativePos.getValueIndex() == NegativePosLocation.AFTER_SYMBOL)
         {
             setRegExpr("^([\\" + currencySymbol + "])?([-]?[\\d]+)" + decimalExpr + "$");
             setDisplayPattern("s/" + "^([\\" + currencySymbol + "])?([-]?[\\d]+)" + decimalExpr + "$/\\" + currencySymbol + "$2$3/g");
             setSubmitPattern("s/" + "^([\\" + currencySymbol + "])?([-]?[\\d]+)" + decimalExpr + "$" + "/$2$3/g");
             setInvalidRegExMessage("Currency values must have the format " +
                     currencySymbol + "xxx.xx for positive values and " +
                    currencySymbol + "-xxx.xx for negative values. (decimals = " + decimalsRequired + ")");
         }
     }
 
     public Locale getLocale()
     {
         return locale;
     }
 
     public void setLocale(Locale locale)
     {
         this.locale = locale;
         setupPatterns();
     }
 
     /**
      * Gets the number of decimal places allowed
      */
     public int getDecimalsRequired()
     {
         return decimalsRequired;
     }
 
     /**
      * Sets the number of decimal places allowed
      */
     public void setDecimalsRequired(int decimalsRequired)
     {
         this.decimalsRequired = decimalsRequired;
         setupPatterns();
     }
 
     public NegativePosLocation getNegativePos()
     {
         return negativePos;
     }
 
     public void setNegativePos(NegativePosLocation negativePos)
     {
         this.negativePos = negativePos;
         setupPatterns();
     }
 
     /**
      *  Passes on the phone format to the client side validations
      */
     public String getCustomJavaScriptDefn(DialogContext dc)
     {
         StringBuffer buf = new StringBuffer(super.getCustomJavaScriptDefn(dc));
         buf.append("field.currency_symbol = '" + this.currencySymbol + "';\n");
         buf.append("field.negative_pos = '" + this.negativePos + "';\n");
         buf.append("field.decimal = '" + this.decimalsRequired + "';\n");
         return buf.toString();
     }
 }
