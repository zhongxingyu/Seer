 // Generated from JSONTokenizer.mirah
 package org.xinuc.jason;
 public class JSONTokenizer extends java.lang.Object {
   private int index;
   private java.lang.String source;
   public  JSONTokenizer(java.lang.String str) {
     this.index = 0;
     this.source = str;
   }
   public void back() {
     if ((this.index > 0)) {
       this.index = (this.index - 1);
     }
   }
   public boolean more() {
     return (this.index < this.source.length());
   }
   public char next() {
     char c = 0;
     if (this.more()) {
       c = this.source.charAt(this.index);
       this.index = (this.index + 1);
       return c;
     }
     else {
       return 0;
     }
   }
   public char next(char c) throws org.xinuc.jason.JSONException {
     char n = 0;
     n = this.next();
     if ((c == n)) {
     }
     else {
       throw this.error("Expected '" + c + "' and instead saw '" + n + "'");
     }
     return n;
   }
   public java.lang.String next(int n) throws org.xinuc.jason.JSONException {
     int start = 0;
     if (((this.index + n) > this.source.length())) {
       throw this.error("Substring bounds error");
     }
     start = this.index;
     this.index = (this.index + n);
     return this.source.substring(start, this.index);
   }
   public char nextClean() {
     char c = 0;
     boolean __xform_tmp_1 = false;
     c = this.next();
     boolean temp$1 = false;
     __xform_tmp_1 = (c == 0);
     temp$1 = __xform_tmp_1 ? (__xform_tmp_1) : ((c > 32));
     if (temp$1) {
       return c;
     }
     else {
       return this.nextClean();
     }
   }
   public java.lang.String nextString() throws org.xinuc.jason.JSONException {
     java.lang.StringBuffer buffer = null;
     char c = 0;
     boolean __xform_tmp_2 = false;
     boolean __xform_tmp_3 = false;
     buffer = new java.lang.StringBuffer();
     label1:
     while (true) {
       label2:
        {
         c = this.next();
         boolean temp$3 = false;
         boolean temp$4 = false;
         __xform_tmp_3 = (c == 0);
         temp$4 = __xform_tmp_3 ? (__xform_tmp_3) : ((c == 10));
         __xform_tmp_2 = temp$4;
         temp$3 = __xform_tmp_2 ? (__xform_tmp_2) : ((c == 13));
         if (temp$3) {
           throw this.error("Unterminated string");
         }
         else {
           if ((c == 92)) {
             c = this.next();
             if ((c == 98)) {
               buffer.append(8);
             }
             else {
               if ((c == 116)) {
                 buffer.append(9);
               }
               else {
                 if ((c == 110)) {
                   buffer.append(10);
                 }
                 else {
                   if ((c == 102)) {
                     buffer.append(12);
                   }
                   else {
                     if ((c == 114)) {
                       buffer.append(13);
                     }
                     else {
                       if ((c == 34)) {
                        buffer.append(34);
                       }
                       else {
                         if ((c == 117)) {
                           buffer.append(((char)(java.lang.Integer.parseInt(this.next(4), 16))));
                         }
                         else {
                           throw this.error("Unexpected token '" + ((char)(c)) + "'");
                         }
                       }
                     }
                   }
                 }
               }
             }
           }
           else {
             if ((c == 34)) {
               break label1;
             }
             else {
               buffer.append(c);
             }
           }
         }
       }
     }
     return buffer.toString();
   }
   public java.lang.String nextTo(char delimiter) {
     java.lang.StringBuffer buffer = null;
     char c = 0;
     boolean __xform_tmp_4 = false;
     boolean __xform_tmp_5 = false;
     boolean __xform_tmp_6 = false;
     buffer = new java.lang.StringBuffer();
     label1:
     while (true) {
       label2:
        {
         c = this.next();
         boolean temp$3 = false;
         boolean temp$4 = false;
         boolean temp$5 = false;
         __xform_tmp_6 = (c == delimiter);
         temp$5 = __xform_tmp_6 ? (__xform_tmp_6) : ((c == 0));
         __xform_tmp_5 = temp$5;
         temp$4 = __xform_tmp_5 ? (__xform_tmp_5) : ((c == 10));
         __xform_tmp_4 = temp$4;
         temp$3 = __xform_tmp_4 ? (__xform_tmp_4) : ((c == 13));
         if (temp$3) {
           if ((c != 0)) {
             this.back();
           }
           break label1;
         }
         else {
           buffer.append(c);
         }
       }
     }
     return buffer.toString().trim();
   }
   public java.lang.String nextTo(java.lang.String delimiter) {
     java.lang.StringBuffer buffer = null;
     char c = 0;
     boolean __xform_tmp_7 = false;
     boolean __xform_tmp_8 = false;
     boolean __xform_tmp_9 = false;
     buffer = new java.lang.StringBuffer();
     label1:
     while (true) {
       label2:
        {
         c = this.next();
         boolean temp$3 = false;
         boolean temp$4 = false;
         boolean temp$5 = false;
         __xform_tmp_9 = (delimiter.indexOf(c) >= 0);
         temp$5 = __xform_tmp_9 ? (__xform_tmp_9) : ((c == 0));
         __xform_tmp_8 = temp$5;
         temp$4 = __xform_tmp_8 ? (__xform_tmp_8) : ((c == 10));
         __xform_tmp_7 = temp$4;
         temp$3 = __xform_tmp_7 ? (__xform_tmp_7) : ((c == 13));
         if (temp$3) {
           if ((c != 0)) {
             this.back();
           }
           break label1;
         }
         else {
           buffer.append(c);
         }
       }
     }
     return buffer.toString().trim();
   }
   public java.lang.Object nextValue() throws org.xinuc.jason.JSONException {
     char c = 0;
     java.lang.StringBuffer buffer = null;
     char iter = 0;
     boolean __xform_tmp_10 = false;
     c = this.nextClean();
     if ((c == 34)) {
       return this.nextString();
     }
     if ((c == 123)) {
       org.xinuc.jason.JSONTokenizer temp$1 = this;
       temp$1.back();
       return org.xinuc.jason.JSONObject.parse(this);
     }
     if ((c == 91)) {
       org.xinuc.jason.JSONTokenizer temp$2 = this;
       temp$2.back();
       return org.xinuc.jason.JSONArray.parse(this);
     }
     buffer = new java.lang.StringBuffer();
     iter = c;
     label3:
     while ((iter >= 32) ? ((",:]}/\\\"[{;=#".indexOf(iter) < 0)) : (false)) {
       label4:
        {
         buffer.append(iter);
         iter = this.next();
       }
     }
     this.back();
     java.lang.String str = buffer.toString().trim();
     if (str.equals("")) {
       throw this.error("Missing value.");
     }
     if (str.equals("true")) {
       return java.lang.Boolean.TRUE;
     }
     if (str.equals("false")) {
       return java.lang.Boolean.FALSE;
     }
     if (str.equals("null")) {
       return org.xinuc.jason.JSONNull.NULL();
     }
     boolean temp$5 = false;
     __xform_tmp_10 = (c >= 48) ? ((c <= 57)) : (false);
     temp$5 = __xform_tmp_10 ? (__xform_tmp_10) : ((c == 45));
     if (temp$5) {
       try {
         return java.lang.Integer.valueOf(str);
       }
       catch (java.lang.Exception tmp$ex$2032) {
         try {
           return java.lang.Double.valueOf(str);
         }
         catch (java.lang.Exception tmp$ex$2034) {
           throw this.error("Invalid value '" + str + "'");
         }
       }
     }
     throw this.error("Invalid value '" + str + "'");
   }
   public char skipTo(char to) {
     char c = 0;
     int idx = 0;
     c = this.next();
     idx = this.index;
     label1:
     while ((c != to)) {
       label2:
        {
         if ((c == 0)) {
           this.index = idx;
           return c;
         }
         c = this.next();
       }
     }
     this.back();
     return c;
   }
   public void skipPast(java.lang.String to) {
     this.index = this.source.indexOf(to, this.index);
     if ((this.index < 0)) {
       this.index = this.source.length();
     }
     else {
       this.index = (this.index + to.length());
     }
   }
   public java.lang.String toString() {
     return " at character " + this.index + " of " + this.source;
   }
   public org.xinuc.jason.JSONException error(java.lang.String message) {
     return new org.xinuc.jason.JSONException((message + this.toString()));
   }
 }
