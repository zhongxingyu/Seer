 //
 // ChargeResponse.java
 // Inner Fence Credit Card Terminal for Android
 // API 1.0.0
 //
 // You may license this source code under the MIT License, reproduced
 // below.
 //
 // Copyright (c) 2012 Inner Fence, LLC
 //
 // Permission is hereby granted, free of charge, to any person
 // obtaining a copy of this software and associated documentation
 // files (the "Software"), to deal in the Software without
 // restriction, including without limitation the rights to use,
 // copy, modify, merge, publish, distribute, sublicense, and/or sell
 // copies of the Software, and to permit persons to whom the
 // Software is furnished to do so, subject to the following
 // conditions:
 //
 // The above copyright notice and this permission notice shall be
 // included in all copies or substantial portions of the Software.
 //
 // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 // EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 // OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 // NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 // HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 // WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 // FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 // OTHER DEALINGS IN THE SOFTWARE.
 //
 package com.innerfence.chargeapi;
 
 import android.content.Intent;
 import android.os.Bundle;
 import java.util.regex.Pattern;
 
 public class ChargeResponse
 {
     public enum Code
     {
         APPROVED,
         CANCELLED,
         DECLINED,
         ERROR,
     }
 
     public static class Keys
     {
         public static final String AMOUNT               = "ifcc_amount";
         public static final String CARD_TYPE            = "ifcc_cardType";
         public static final String CURRENCY             = "ifcc_currency";
         public static final String ERROR_MESSAGE        = "ifcc_errorMessage";
         public static final String EXTRA_PARAMS         = "ifcc_extraParams";
         public static final String REDACTED_CARD_NUMBER = "ifcc_redactedCardNumber";
         public static final String RESPONSE_TYPE        = "ifcc_responseType";
         public static final String TAX_AMOUNT           = "ifcc_taxAmount";
         public static final String TAX_RATE             = "ifcc_taxRate";
         public static final String TIP_AMOUNT           = "ifcc_tipAmount";
         public static final String TRANSACTION_ID       = "ifcc_transactionId";
     }
 
     static class Patterns
     {
         public static final String AMOUNT               = "^(0|[1-9][0-9]*)[.][0-9][0-9]$";
         public static final String CARD_TYPE            = "^[A-Za-z ]{0,20}$";
         public static final String CURRENCY             = "^[A-Z]{3}$";
         public static final String ERROR_MESSAGE        = "^.*$";
         public static final String REDACTED_CARD_NUMBER = "^X*[0-9]{4}$";
         public static final String RESPONSE_TYPE        = "^[a-z]*$";
         public static final String TAX_AMOUNT           = "^(0|[1-9][0-9]*)[.][0-9][0-9]$";
        public static final String TAX_RATE             = "^[0-9]{1,2}([.][0-9]{1,3})?$";
         public static final String TIP_AMOUNT           = "^(0|[1-9][0-9]*)[.][0-9][0-9]$";
         public static final String TRANSACTION_ID       = "^.{1,255}";
     }
 
     public static class Type
     {
         public static final String APPROVED  = "approved";
         public static final String CANCELLED = "cancelled";
         public static final String DECLINED  = "declined";
         public static final String ERROR     = "error";
     }
 
     protected String _amount;
     protected String _cardType;
     protected String _currency;
     protected String _errorMessage;
     protected Bundle _extraParams;
     protected String _redactedCardNumber;
     protected Code   _responseCode;
     protected String _responseType;
     protected String _taxAmount;
     protected String _taxRate;
     protected String _tipAmount;
     protected String _transactionId;
 
     protected ChargeResponse() { }
 
     public ChargeResponse( Intent data )
     {
         if( null == data )
         {
             throw new IllegalArgumentException("data is null");
         }
 
         if( null == data.getExtras() )
         {
             throw new IllegalArgumentException("data has no extras bundle.");
         }
 
         Bundle bundle = data.getExtras();
 
         if( !bundle.containsKey(Keys.RESPONSE_TYPE) )
         {
             throw new IllegalArgumentException("data's extras bundle doesn't contain a response type");
         }
 
         _amount             = bundle.getString(Keys.AMOUNT);
         _cardType           = bundle.getString(Keys.CARD_TYPE);
         _currency           = bundle.getString(Keys.CURRENCY);
         _errorMessage       = bundle.getString(Keys.ERROR_MESSAGE);
         _extraParams        = bundle.getBundle(Keys.EXTRA_PARAMS);
         _redactedCardNumber = bundle.getString(Keys.REDACTED_CARD_NUMBER);
         _responseType       = bundle.getString(Keys.RESPONSE_TYPE);
         _taxAmount          = bundle.getString(Keys.TAX_AMOUNT);
         _taxRate            = bundle.getString(Keys.TAX_RATE);
         _tipAmount          = bundle.getString(Keys.TIP_AMOUNT);
         _transactionId      = bundle.getString(Keys.TRANSACTION_ID);
 
         validateFields();
 
         if( Type.APPROVED.equals(_responseType) )
         {
             _responseCode = Code.APPROVED;
         }
         else if( Type.CANCELLED.equals(_responseType) )
         {
             _responseCode = Code.CANCELLED;
         }
         else if( Type.DECLINED.equals(_responseType) )
         {
             _responseCode = Code.DECLINED;
         }
         else
         {
             _responseCode = Code.ERROR;
         }
     }
 
     // amount - The amount that was charged to the card. This is a
     // string, which is a currency value to two decimal places like
     // "50.00". This property will only be set if responseCode is
     // Accepted. This amount includes tax and tip.
     public String getAmount()
     {
         return _amount;
     }
 
     // cardType - The type of card that was charged. This will be
     // something like "Visa", "MasterCard", "American Express", or
     // "Discover". This property will only be set if responseCode is
     // Accepted. In the case that the card type is unknown, this
     // property will be nil.
     public String getCardType()
     {
         return _cardType;
     }
 
     // currency - The ISO 4217 currency code for the transaction
     // amount. For example, "USD" for US Dollars. This property will
     // be set when amount is set.
     public String getCurrency()
     {
         return _currency;
     }
 
     // errorMessage - If an error occured, more diagnostic info will
     // be provided in parameter
     public String getErrorMessage()
     {
         return _errorMessage;
     }
 
     // extraParams - This should be the same as the bundle you passed
     // in when creating the ChargeRequest. If there are no extra
     // parameters, this property will be an null.
     //
     // WARNING - The extra params is an attack vector to your Android
     // app, just like if it were a web app; you must be wary of SQL
     // injection and similar malicious data attacks. As such, you will
     // need to validate any parameters from the extraParams fields
     // that you will be using. For example, if you expect a numeric
     // value, you should ensure the field is comprised of digits.
     public Bundle getExtraParams()
     {
         return _extraParams;
     }
 
     // redactedCardNumber - This string is the credit card number with
     // all but the last four digits replaced by 'X'. This property
     // will only be set if responseCode is Accepted.
     public String getRedactedCardNumber()
     {
         return _redactedCardNumber;
     }
 
     // responseCode - One of the ChargeResponse.Code enum values.
     public Code getResponseCode()
     {
         return _responseCode;
     }
 
     // responseType - string value of responseCode
     public String getResponseType()
     {
         return _responseType;
     }
 
     // taxAmount - The tax portion of amount. This is a string, which
     // is a currency value to two decimal places like "4.06". This
     // property will only be set if sales tax was applied.
     public String getTaxAmount()
     {
         return _taxAmount;
     }
 
     // taxRate - The tax rate percentage that was applied to the
     // original amount. This is a string, which is a number between 0
     // and 99.999 with upto three decimal places like "8.125". This
     // property will only be set if sales tax was applied.
     public String getTaxRate()
     {
         return _taxRate;
     }
 
     // tipAmount - The tip portion of amount. This is a string, which
     // is a currency value to two decimal places like "10.00". This
     // property will only be set if a tip was provided.
     public String getTipAmount()
     {
         return _tipAmount;
     }
 
     // transactionId - The transaction ID of the transaction if the charge
     // was successful.
     //
     // Previous versions of the API didn't return a transaction ID, so null
     // is a possible return value even for a successful transaction. If
     // you do need the transaction ID, you may want to request the user to
     // update their version of Credit Card Terminal.
     //
     // WARNING - The transaction ID format differs between gateways and
     // we've provided a very relaxed validation limiting it to a max of
     // 255 characters; you must be wary of SQL injection and similar
     // malicious data attacks. As such, you will should ensure whatever
     // you use the transaction ID for properly escapes it.
     public String getTransactionId()
     {
         return _transactionId;
     }
 
     public void validateFields()
     {
         validateField( Patterns.AMOUNT,               _amount,             Keys.AMOUNT );
         validateField( Patterns.CARD_TYPE,            _cardType,           Keys.CARD_TYPE );
         validateField( Patterns.CURRENCY,             _currency,           Keys.CURRENCY );
         validateField( Patterns.ERROR_MESSAGE,        _errorMessage,       Keys.ERROR_MESSAGE );
         validateField( Patterns.REDACTED_CARD_NUMBER, _redactedCardNumber, Keys.REDACTED_CARD_NUMBER );
         validateField( Patterns.RESPONSE_TYPE,        _responseType,       Keys.RESPONSE_TYPE );
     }
 
     public void validateField( String pattern, String value, String fieldName )
     {
         if( null != value && !Pattern.matches( pattern, value ) )
         {
             throw new IllegalArgumentException( fieldName );
         }
     }
 }
