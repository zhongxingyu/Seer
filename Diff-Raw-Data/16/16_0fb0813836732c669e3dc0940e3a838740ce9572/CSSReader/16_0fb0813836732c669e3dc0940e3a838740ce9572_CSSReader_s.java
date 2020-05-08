//#condition api.xparse
 /*
  * Copyright (C) 2010 France Telecom
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package memoplayer;
 
 // a CSSProp defines a single property  
 
 class CSSProp {
     final static int FONT_WEIGHT = 1;
     final static int FONT_STYLE = 2;
     final static int FONT_SIZE = 4;
     final static int FONT_FAMILY = 5;
     final static int FONT_COLOR = 6;
 
     final static int BG_COLOR = 11;
     final static int BG_IMAGE = 12;
     final static int BG_ATTACHEMENT = 13;
     final static int BG_POSITION = 14;
     final static int BG_REPEAT = 15;
 
     final static int FG_COLOR = 21;
     final static int TEXT_ALIGN = 22;
     final static int TEXT_INDENT = 23;
     final static int LINE_HEIGHT = 24;
     final static int TEXT_DECORATION = 25;
 
     final static int MARGIN_LEFT = 31;
     final static int MARGIN_RIGHT = 32;
     final static int MARGIN_TOP = 33;
     final static int MARGIN_BOTTOM = 34;
 
     final static int PADDING_LEFT = 35;
     final static int PADDING_RIGHT = 36;
     final static int PADDING_TOP = 37;
     final static int PADDING_BOTTOM = 38;
 
     final static int BORDER_COLOR = 41;
     final static int BORDER_WIDTH = 42;
     final static int BORDER_STYLE = 43;
 
     final static int WIDTH = 51;
     final static int HEIGHT = 52;
     final static int FLOAT = 53;
     
     final static int ROTATION = 61;
     final static int SIZE = 60;
     final static int SCALE = 62;
     final static int POSITION = 63;
     final static int TRANSLATION = 64;
     final static int FILLED = 65;
     final static int DISABLED_COLOR = 66;
     final static int IMAGE_POSITION = 67;
     final static int TEXT_POSITION = 68;
     final static int RECTANGLE_POSITION = 69;
     final static int IMAGE_SIZE = 70;
     final static int TEXT_SIZE = 71;
     final static int RECTANGLE_SIZE = 72;
     final static int ORIENTATION = 73;
     final static int VERTICAL_ALIGN = 74;
     final static int HORIZONTAL_ALIGN = 75;
     final static int FONT_FILTER = 76;
     final static int LIGHT = 77;
     final static int TRANSPARENCY = 78;
     final static int ANIMATION = 79;
     final static int DURATION = 80;
 
     final static int TYPE_NONE = 0;
     final static int TYPE_PIXEL = 1;
     final static int TYPE_EM = 2;
     final static int TYPE_PERCENT = 3;
     final static int TYPE_AUTO = 4;
     final static int TYPE_COLOR = 5;
     final static int TYPE_URL = 6;
     final static int TYPE_SIDE = 7;
     final static int TYPE_NUM = 8;
     final static int TYPE_STRING = 16;
 
     final static String s_colorNames [] = { "aqua", "black", "blue", "fuchsia", "gray", "green", "lime", "maroon", "navy", "olive", "purple", "red", "silver", "teal", "white", "yellow", "pink", "orange" };
     final static int s_colorValues [] = { 0XFFFF, 0, 0XFF, 0xFF00FF, 0x808080, 0X8000, 0xFF00, 0x800000, 0x80, 0x808000, 0x800080, 0xFF0000, 0xC0C0C0, 0x8080, 0XFFFFFF, 0xFFFF00, 0xFF8080, 0xFF6600 };
 
     int m_id;  // the index inside the static arrays (s_names and s_ids)
     String m_val; // the value when it can be stored in a string
     CSSProp m_next; // to chain the properties
     String m_sval;
     int m_value, m_value2;
     int m_type, m_type2;
 
     CSSProp (int id, String val, CSSProp next) {
         m_id = id;
         m_val = val;
         m_next = next;
         m_type = m_type2 = TYPE_NONE;
         int end = m_val.length () - 1;
         int newStart = parseValue (0, end, true);
         if (newStart > -1) {
             parseValue (newStart, end, false);
         }
     }
     
     void printValue (int type, int value) {
         switch (type) {
         case TYPE_NONE:
             Logger.print ("    NONE:"+m_val);
             break;
         case TYPE_PIXEL:
             Logger.print ("    "+(value/65536.0)+"px");
             break;
         case TYPE_EM:
             Logger.print ("    "+(value/65536.0)+"em");
             break;
         case TYPE_PERCENT:
             Logger.print ("    "+(value/65536.0)+"%");
             break;
         case TYPE_NUM:
             Logger.print ("    "+(value/65536.0));
             break;
         case TYPE_COLOR:
             Logger.print ("    rgb("+((value&0xFF0000)>>16)+", "+((value&0xFF00)>>8)+", "+(value&0xFF)+")");
             break;
         case TYPE_URL:
             Logger.print ("    url("+m_sval+")");
             break;
         case TYPE_STRING:
             Logger.print ("    '"+m_sval+"'");
             break;
         default:
             Logger.print ("    DEFAULT:"+m_val);
             break;
         }
     }
     void print () {
         Logger.print ("    "+CSSList.s_names[m_id]+":");
         printValue (m_type, m_value);
         if (m_type2 != TYPE_NONE) {
             printValue (m_type2, m_value2);
         }
         Logger.println (";");
         if (m_next != null) {
             m_next.print ();
         }
     }
 
     int parseUrl (int start, int end) {
         if ( (end = m_val.indexOf (')', start)) == -1) { return -1; } // missing ending parent
         if (m_val.charAt (start) == '\'' || m_val.charAt (start) == '"') {
             start++; end--;
         }
         m_type = TYPE_URL;
         m_sval = m_val.substring (start, end);
         return -1; // no more value to parse
     }
 
     int parseString (int start, int end, int sep) {
         int pos = start;
         char c = m_val.charAt (start);
         while (start < end && c != sep) {
              c = m_val.charAt (++start);
         }
         if (c == sep) {
             m_type = TYPE_STRING;
             m_sval = m_val.substring (pos, end);
         }
         return -1; // no more value to parse
     }
 
     int setValue (int value, int type, boolean first, int newStart) {
         if (first) {
             m_value = value;
             m_type = type;
         } else {
             m_value2 = value;
             m_type2 = type;
         }
         return newStart;
     }
 
     int parseColorHexa (int start, int end) {
         int value = parseColorComponent (start, end, 16);
         if ((end - start) == 2) { // if #RGB and not #RRGGBB
             int r = value & 0xF00;
             int g = value & 0xF0;
             int b = value & 0xF;
             value = (r<<12)+(r<<8) + (g<<8)+(g<<4) + (b<<4)+b;
         }
         return setValue (value, TYPE_COLOR, true, -1);
     }
 
 
     int parseColorComponent (int start, int end, int base) {
         int integer = 0, digit = 0;
         char c = m_val.charAt (start);
         // skipping white spaces
         while (start < end && (c == ' ' || c == '\t')) {
             c = m_val.charAt (++start);
         }
         // the integer part
         while (true) {
             if (c >= '0' && c <= '9') { 
                 digit = c - '0'; 
             } else if (c >= 'A' &&  c <= 'F') {
                 digit = 10 + c - 'A';
             } else if (c >= 'a' &&  c <= 'f') {
                 digit = 10 + c - 'a';
             }
             if (digit >= base) {
                 break;
             }
             integer = (integer * base) + digit;
             if (++start > end) {
                 break;
             }
             c = m_val.charAt (start);
         }
         return integer;
     }
 
     int parseColorRgb (int start, int end) {
         int i = start, j, r, g, b;
         if ( (j = m_val.indexOf (',', i)) == -1) { return -1; }
         r = parseColorComponent (i, j-1, 10);
         i = j+1;
         if ( (j = m_val.indexOf (',', i)) == -1) { return -1; }
         g = parseColorComponent (i, j-1, 10);
         i = j+1;
         if ( (j = m_val.indexOf (')', i)) == -1) { return -1; }
         b = parseColorComponent (i, j-1, 10);
         return setValue (r*65536+g*256+b, TYPE_COLOR, true, -1);
     }
 
     boolean parseColorName (int start, int end) {
         int nbColors = s_colorNames.length;
         for (int i = 0; i < nbColors; i++) {
             if (m_val.startsWith (s_colorNames[i], start)) {
                 setValue (s_colorValues[i], TYPE_COLOR, true, -1);
                 return true;
             }
         }
         return false;
     }
 
     int parseNumber (int start, int end, boolean first) {
         int sign = 1, integer = 0, fract = 0, powerOf10 = 1;
         //Logger.println ("parseNumber: "+start+", "+end+" = '"+m_val.substring(start, end+1)+"' start="+start);
         char c = m_val.charAt (start);
         // the sign
         if (c == '-') {
             sign = -1;
             c = m_val.charAt (++start);
         }
         // the integer part
         while (c >= '0' && c <= '9') { 
             integer = (integer * 10) + c - '0';
             if (++start > end) {
                 break;
             }
             c = m_val.charAt (start);
         }
         if (c == '.') {
             while (start < end) {
                 c = m_val.charAt (++start);
                 if (c >= '0' && c <= '9') { 
                     fract = (fract * 10) + c - '0';
                     powerOf10 *= 10;
                 } else {
                     break;
                 }
             }
         }
         while (start < end && (c == ' ' || c == '\t')) { //skip white
             c = m_val.charAt (++start);
         }
         //Logger.println ("ParseNumber: "+sign+" "+integer+" . "+fract+" / "+powerOf10+", new start="+start);
         int type = TYPE_NONE;
         if (c == 'p') { // pixel
             start += 2;
             type = TYPE_PIXEL;
         } else if (c == '%') {// percent
             start += 1;
             type = TYPE_PERCENT;
         } else if (c == 'e') {// em
             start += 2;
             type = TYPE_EM;
         } else {
             type = TYPE_NUM;
         }
         return setValue (sign * ((integer << 16) + (fract << 16) / powerOf10), type, first, start);
     }
 
     int parseValue (int start, int end, boolean first) {
         if ((end-start) < 1) { return -1; }
         char c = m_val.charAt (start);
         while (start < end && (c == ' ' || c == '\t')) { //skip white
             c = m_val.charAt (++start);
         }
         if (c == '-' || (c >= '0' && c <= '9'))  { // a number
             return parseNumber (start, end, first);
         } else if (c == '#') { // an hexa number
             return parseColorHexa (start+1, end);
         } else if (c == '\'' || c == '"') { // a string
             return parseString (start+1, end, c);
         } else if (m_val.startsWith ("rgb(", start)) { // a color as rgb(r, g, b)
             return parseColorRgb (start+4, end);
         } else if (m_val.startsWith ("url(", start)) { // an  url as url(http://)
             return parseUrl (start+4, end);
         } else if (c == '-' || (c >= '0' && c <= '9'))  { // a number
             return parseNumber (start, end, first);
         } else { // a color name ?
             parseColorName (start, end);
             return -1;
         }
     }
 
     // utility function that parse an integer
 //     static boolean isInteger (String val, int start, int end) {
 //         if ((end-start) < 1) { return false; }
 //         if (val.charAt (start) == '-') { start++; }
 //         char c;
 //         for (int i = start; i < end; i++) {
 //             c = val.charAt (i);
 //             if ( (c < '0' || c > '9') && c != '.') { return false; } 
 //         }
 //         return true;
 //     }
 
 //     static int parseInteger (String val, int start, int end, int base) {
 //         Logger.println ("parseInteger: "+val+" from "+start+" to "+end);
 
 //         if ((end-start) < 0) { return 0; }
 //         char c = val.charAt (start);
 //         int sign = 1;
 //         int cumul = 0;
 //         int digit = 0;
 //         while (c == ' ' || c == '\t') {
 //             c = val.charAt (++start);
 //         }
 //         if (c == '-') {
 //             sign = -1;
 //             c = val.charAt (++start);
 //         }
 //         while (true) {
 //             Logger.println ("  c="+c);
 //             if (c >= '0' && c <= '9') { 
 //                 digit = c - '0'; 
 //             } else if (c >= 'A' &&  c <= 'F') {
 //                 digit = 10 + c - 'A';
 //             } else if (c >= 'a' &&  c <= 'f') {
 //                 digit = 10 + c - 'a';
 //             }
 //             if (digit >= base) {
 //                 return 0;
 //             }
 //             cumul = cumul * base + digit;
 //             if (++start > end) {
 //                 break;
 //             }
 //             c = val.charAt (start);
 //         }
 //         Logger.println (" => "+(cumul*sign));
 //         return cumul*sign;
 //     }
 //     static int parseFloat (String val, int start, int end) {
 //         if ((end-start) < 1) { return 0; }
 //         char c = val.charAt (start);
 //         int sign = 1;
 //         int cumul = 0;
 //         int stop = -1;
 //         while (c == ' ' || c == '\t') {
 //             c = val.charAt (++start);
 //         }
 //         if (c == '-') {
 //             sign = -1;
 //             start++;
 //         }
 //         for (int i = start; i < end; i++) {
 //             c = val.charAt (i);
 //             if (c >= '0' && c <= '9') { 
 //                 cumul = cumul * 10 + c - '0';
 //                 if (--stop == 0) {
 //                     break;
 //                 }
 //             } else if (c == '.') {
 //                 stop=2;
 //             }
 //         }
 //         if (stop < 0) {
 //             cumul *= 100;
 //         } else {
 //             while (stop-- > 0) {
 //                 cumul *= 10;
 //             }
 //         }
 //         return cumul*sign;
 //     }
 
 //     static int parseFrac (String val, int start, int end) {
 //          int n = end-start;
 //          if (n < 1) { return 0; }
 //          if (n > 2) { n = 2; }
 //          int v = parseInteger (val, start, start+n, 10);
 //          Logger.println ("parseFrac: n="+n+", v="+v);
 //          return v * (n == 1 ? 10 : 1);
 //     }
  
 //     static int parseFloat (String val) {
 //         return parseFloat (val, 0, val.length());
 //     }
 
 //     static boolean isInteger (String val) {
 //         return isInteger (val, 0, val.length());
 //     }
 
 //     static int getInt (String val) {
 //         return parseInteger (val, 0, val.length(), 10);
 //     }
 
 //     static int getInt (String val, int base) {
 //         return parseInteger (val, 0, val.length(), base);
 //     }
 
 //     static boolean isLength (String val, int start, String unit) {
 //         int baseLen = val.length() - unit.length () - start;
 //         if (baseLen < 1 || val.indexOf (unit, start) < start ) { return false; }
 //         return isInteger (val, start, baseLen);
 //     }
 
 //     static int getLength (String val, int start, String unit) {
 //         int end = val.indexOf (unit, start);
 //         if (end < start) { return 0; }
 //         return parseInteger (val, start, end, 10);
 //     }
 
 //     static int getSecondLength (String val, String unit) {
 //         int start = val.indexOf (unit);
 //         if (start == -1) { return 0; }
 //         while (BaseReader.isWhite (val.charAt (start))) {
 //             start++;
 //         }
 //         return getLength (val, start, unit);
 //     }
 
 //     static int getFloatLength (String val, int start, String unit) {
 //         int end = val.indexOf (unit, start);
 //         if (end < start) { return 0; }
 //         return parseFloat (val, start, end);
 //     }
 
 //     static int getSecondFloatLength (String val, String unit) {
 //         int start = val.indexOf (unit);
 //         if (start == -1) { return 0; }
 //         while (BaseReader.isWhite (val.charAt (start))) {
 //             start++;
 //         }
 //         return getFloatLength (val, start, unit);
 //     }
 
 //     static boolean isPercent (String val) { return isLength (val, 0, "%"); }
 
 //     static int getPercent (String val) { return getFloatLength (val, 0, "%"); }
 
 //     static int getSecondPercent (String val) { return getSecondFloatLength (val, "%");  }
 
 //     static boolean isPixel (String val) { return isLength (val, 0, "px"); }
     
 //     static int getPixel (String val) { return getLength (val, 0, "px"); }
     
 //     static int getSecondPixel (String val) { return getSecondLength (val, "px");  }
 
 //     static boolean isEM (String val) { return isLength (val, 0, "em"); }
 
 //     static int getEM (String val) { return getFloatLength (val, 0, "em"); }
 
 //     static int getSecondEM (String val) { return getSecondLength (val, "em");  }
 
 //     static int getColor (String val) {
 //         if (val.charAt(0) == '#') {
 //             int c = parseInteger (val, 1, val.length()-1, 16);
 //             if (val.length() == 4) {
 //                 int r = c & 0xF00;
 //                 int g = c & 0xF0;
 //                 int b = c & 0xF;
 //                 c = (r<<12)+(r<<8)  + (g<<8)+(g<<4) + (b<<4)+b;
 //             } 
 //             return c;
 //         } else if (val.startsWith ("rgb(")) {
 //             int i = 4, j, r, g, b;
 //             if ( (j = val.indexOf (',', 4)) == -1) { return 0; }
 //             r = parseInteger (val, i, j-1, 10);
 //             i = j;
 //             if ( (j = val.indexOf (',', j+1)) == -1) { return 0; }
 //             g = parseInteger (val, i, j-1, 10);
 //             i = j;
 //             if ( (j = val.indexOf (')', j+1)) == -1) { return 0; }
 //             b = parseInteger (val, i, j-1, 10);
 //             return r*65536+g*256+b;
 //         } else { // a name
 //             int nbColors = s_colorNames.length;
 //             for (int i = 0; i < nbColors; i++) {
 //                 if (val.equals (s_colorNames[i])) {
 //                     return s_colorValues[i];
 //                 }
 //             }
 //         }
 //         return 0;
 //         //return m_type == TYPE_COLOR ? m_value : 0;
 //     }
 
 //     static String getUrl (String val) {
 //         if (val.startsWith ("url(")) {
 //             int start = 4, end;
 //             if ( (end = val.indexOf (')', start)) == -1) { return null; }
 //             if (val.charAt (start) == '\'' || val.charAt (start) == '"') {
 //                 start++; end--;
 //             }
 //             return val.substring (start, end);
 //         }
 //         // else either none or an error
 //         return null;
 //     }
 
 
 }
 
 class CSSList {
     final static String [] s_names = {
         "font-family", "font-size", "font-style", "font-weight", // 0 .. 3
         "background-color", "background-image", "background-repeat", "background-attachement", "background-position", // 4 .. 8
         "color", "text-align", "text-indent", "line-height", "text-decoration", // 9 .. 13
         "margin-left", "margin-right", "margin-top", "margin-bottom", // 14 .. 17
         "padding-left", "padding-right", "padding-top", "padding-bottom", // 18 .. 21
         "border-width", "border-style", "border-color", // 22 .. 24
         "width", "height", "float", // 25 .. 27
         // special for VRML
         "rotation", "size", "scale", "position", "translation", "filled", //28 .. 33
         "disabled-color", "image-position", "text-position", "rectangle-position", // 34 .. 37
         "image-size", "text-size", "rectangle-size", "orientation", // 38 .. 41
         "vertical-align", "horizontal-align", "font-filter", // 42 .. 44
         "light", "transparency", "animation", "duration"// 45 .. 48
     };
 
     final static int [] s_ids = {
         CSSProp.FONT_FAMILY, CSSProp.FONT_SIZE, CSSProp.FONT_STYLE, CSSProp.FONT_WEIGHT,
         CSSProp.BG_COLOR, CSSProp.BG_IMAGE, CSSProp.BG_REPEAT, CSSProp.BG_ATTACHEMENT, CSSProp.BG_POSITION,
         CSSProp.FG_COLOR, CSSProp.TEXT_ALIGN, CSSProp.TEXT_INDENT, CSSProp.LINE_HEIGHT, CSSProp.TEXT_DECORATION,
         CSSProp.MARGIN_LEFT, CSSProp.MARGIN_RIGHT, CSSProp.MARGIN_TOP, CSSProp.MARGIN_BOTTOM,
         CSSProp.PADDING_LEFT, CSSProp.PADDING_RIGHT, CSSProp.PADDING_TOP, CSSProp.PADDING_BOTTOM,
         CSSProp.BORDER_WIDTH, CSSProp.BORDER_STYLE, CSSProp.BORDER_COLOR, 
         CSSProp.WIDTH, CSSProp.HEIGHT, CSSProp.FLOAT,
         CSSProp.ROTATION, CSSProp.SIZE, CSSProp.SCALE, CSSProp.POSITION, CSSProp.TRANSLATION, CSSProp.FILLED, 
         CSSProp.DISABLED_COLOR, CSSProp.IMAGE_POSITION, CSSProp.TEXT_POSITION, CSSProp.RECTANGLE_POSITION,
         CSSProp.IMAGE_SIZE, CSSProp.TEXT_SIZE, CSSProp.RECTANGLE_SIZE, CSSProp.ORIENTATION,
         CSSProp.VERTICAL_ALIGN, CSSProp.HORIZONTAL_ALIGN, CSSProp.FONT_FILTER,
         CSSProp.LIGHT, CSSProp.TRANSPARENCY, CSSProp.ANIMATION, CSSProp.DURATION,
     };
     final static int BG_OFFSET = 4; // index of first BG attribute, WARNING modify here if you change tables above
     final static int MARGIN_OFFSET = 14; // index of first margin attribute, WARNING modify here if you change tables above
     final static int PADDING_OFFSET = 18; // index of first padding, WARNING modify here if you change tables above
     final static int BO_OFFSET = 22; // index of first border, WARNING modify here if you change tables above
  
     // list of tags specific to the backgroud attribute
     String m_name; // contains the name of the element like "p" or "h1" or "" if top level
     String m_class; // contains the name of the css class
     String m_pseudoClass; // contains the name of the pseudo class (ex ':link') or null
     CSSProp m_properties; // the property list defined for this definition
     CSSList m_next; // to chain the lists
 
     CSSList (String name, String aclass, CSSList next) {
         m_class = aclass;
         m_name = name;
         m_next = next;
     }
 
     static int findAttrIndex (String name) {
         int max = s_names.length;
         for (int i = 0; i < max; i++) {
             if (name.equalsIgnoreCase (s_names[i])) {
                 return i;
             }
         }
         Logger.println ("CssReader attribute not supported: "+name);
         return -1;
     }
 
     void addProp (String name, String val) {
         int index = findAttrIndex (name);
         if (index >= 0) {
             m_properties = new CSSProp (index, val, m_properties);
         } else {
             Logger.println ("CssList.addProp: ignoring: '"+name+"'");
         }
     }
 
     void addProp (int id, String val) {
         m_properties = new CSSProp (id, val, m_properties);
     }
 
     void print () {
         if (m_next != null) {
             m_next.print ();
         }
         Logger.println (""+m_name+"."+m_class);
         if (m_pseudoClass != null) {
             Logger.println (" : "+m_pseudoClass);
         }
         Logger.println (" {");
         if (m_properties != null) { m_properties.print (); }
         Logger.println ("}");
     }
 
     CSSList findByTag (String tagName) {
         if (tagName.equalsIgnoreCase(m_name) && m_class == null) {
             //Logger.println ("    > findByTag "+m_name+", "+m_class);
             return this;
         } else if (m_next != null) {
             return m_next.findByTag (tagName);
         } else  {
             return null;
         }
     }
 
     CSSList findByClass (String className) {
         if ( className.equalsIgnoreCase (m_class) && m_name == null) {            
             return this;
         } else if (m_next != null) {
             return m_next.findByClass (className);
         } else  {
             return null;
         }
     }
 
     CSSList findByTagAndClass (String tagName, String className) {
         if ( tagName.equalsIgnoreCase(m_name) && className.equalsIgnoreCase (m_class) ) {
             return this;
         } else if (m_next != null) {
             return m_next.findByTagAndClass (tagName, className);
         } else  {
             return null;
         }
     }
 }
 
 public class CSSReader extends BaseReader {
     final static int ERROR  = -1;
 
     CSSList m_root;
     boolean m_internal; // true if parse only a list of props, coming from a style *attribute* 
 
     // parse a whole CSS definition 
     public CSSReader (String buffer, int mode) {
         super (buffer, mode);
     }
     
     // parse a single definition
     public CSSReader () {
         super (null, 0);
     }
 
     CSSList parseInternal (String buffer) {
         m_buffer = buffer;
         m_pos = 0;
         m_len = buffer.length ();
         return parseProperties (new CSSList (null, null, null));
     }
     
     void closeSpecific () {
         m_root = null;
     }
     
     boolean parse () {
         try {
             parseElements ();
         } catch (Exception e) {
             m_root = null;
             out ("CssReader: Exception while parsing: "+e);
             e.printStackTrace ();
             return false;
         }
         //out ("----- CSSReader.parse got: ");
         if (m_root != null && m_debugMode) {
             m_root.print ();
         }
         //out ("----- CSSReader.parse end of parsing ");
         return true;
     }
 
     // the path string contains /nodeName[count]/...
     public boolean find (String path) {
         return false;
     }
     
     public void checkForComment () {
         skipSpaces ();
         if (eatChar ('/')) {
             if (eatChar ('/') == true) { // eat until end of line
                 char c = getChar ();
                 while (c != '\0' && c != '\n') {
                     c = getNextChar ();
                 }
                 m_nbLines++;
                 checkForComment ();
                 return ;
             }
             if (eatChar ('*') == false) { // definitevely not a comment 
                 m_pos--; // push back the /
                 return;
             }
             char c = getChar ();
             while (c != '\0') {
                 if (c == '*') {
                     c = getNextChar ();
                     if (c == '/') {
                         m_pos++;
                         checkForComment ();
                         return;
                     }
                 } else {
                     c = getNextChar ();
                 }
             }
             // comment not closed
             out ("comment not closed");
         }
     }
 
     protected boolean parseElements () {
         while (true) {
             checkForComment ();
             String name = getNextStrictToken ();
             String pseudoClass = null;
             /*if (name == null) {
                 break;
                 }*/
             String aclass = null; 
             skipSpaces ();
             if (eatChar ('.')) {
                 aclass = getNextToken ();
             }
             if (name == null && aclass == null) {
                 break;
             }
             // pseudo class ?
             if (eatChar (':')) {
                 pseudoClass = getNextToken ();
                 //Logger.println ("Got pseudoClass : "+pseudoClass+" for "+name+"."+aclass);
             }
             skipSpaces ();
             if (eatChar ('{') == false) {
                 Logger.println ("CSSReader: { expected line #"+m_nbLines);
                 return false;
             }
             m_root = new CSSList (name, aclass, m_root);
             m_root.m_pseudoClass = pseudoClass;
             if (parseProperties (m_root) == null || eatChar ('}') == false) {
                 Logger.println ("CSSReader: bad propertiy definition or } expected instead of '"+getChar()+"', line #"+m_nbLines);
                 return false;
             }
         }
         return true;
     }
     
     String getNextPropValue () {
         checkForComment ();
         char c = getChar ();
         if (c == '"') {
             return getString ();
         }
         int p = m_pos;
         if ( m_buffer.startsWith ("rgb(", p) || m_buffer.startsWith ("url(", p)) {
             while (c != '\0' && c != ';' && c != '}') {
                 c = getNextChar ();
             }
         } else {
             while (c != '\0' && c != ' ' && c != '\r' && c != '\n' && c != ';' && c != '}') {
                 c = getNextChar ();
             }
         }
         if (p == m_pos) {
             return null;
         }
         
         return m_buffer.substring (p, m_pos);
     }
 
     String getFullPropValue () {
         checkForComment ();
         char c = getChar ();
         if (c == '"') {
             return getString ();
         }
         int p = m_pos;
         while (c != '\0' && c != ';' && c != '}') {
             c = getNextChar ();
         }
         if (p == m_pos) {
             return null;
         }
         return m_buffer.substring (p, m_pos);
     }
 
     protected CSSList parseProperties (CSSList root) {
         while (true) {
             checkForComment ();
             String name = getNextStrictToken ();
             if (name == null || name.length() == 0) {
                 return root;
             }
             skipSpaces ();
             if (eatChar (':')) {
                 skipSpaces ();
                 String value;
                 // Manage special cases:
                 if (name.equalsIgnoreCase ("background")) {
                     int i = CSSList.BG_OFFSET;
                     int nb = 0;
                     value = getNextPropValue ();
                     while (value != null && nb++ < 5) {
                         root.addProp (i++, value);
                         value = getNextPropValue ();
                     }
                 } else if (name.equalsIgnoreCase ("margin")) {
                     int i = CSSList.MARGIN_OFFSET;
                     int nb = 0;
                     String [] vals = new String [4];
                     value = getNextPropValue ();
                     while (value != null && nb < 4) {
                         vals[nb++] = value;
                         value = getNextPropValue ();
                     }
                     if (nb == 1) {
                         root.addProp (i++, vals[0]);
                         root.addProp (i++, vals[0]);
                         root.addProp (i++, vals[0]);
                         root.addProp (i,   vals[0]);
                     } else if (nb == 2) {
                     }
                 } else if (name.equalsIgnoreCase ("padding")) {
                     int i = CSSList.PADDING_OFFSET;
                     int nb = 0;
                     String [] vals = new String [4];
                     value = getNextPropValue ();
                     while (value != null && nb < 4) {
                         vals[nb++] = value;
                         value = getNextPropValue ();
                     }
                     if (nb == 1) {
                         root.addProp (i++, vals[0]);
                         root.addProp (i++, vals[0]);
                         root.addProp (i++, vals[0]);
                         root.addProp (i,   vals[0]);
                     } else if (nb == 2) {
                     }
                 } else if (name.equalsIgnoreCase ("border")) {
                     int i = CSSList.BO_OFFSET;
                     int nb = 0;
                     value = getNextPropValue ();
                     while (value != null && nb++ < 3) {
                         root.addProp (i++, value);
                         value = getNextPropValue ();
                     }
                 } else { //signe value from : to ;
                     value = getFullPropValue ();
                     if (value == null || value.length() == 0) {
                         break;
                     }
                     root.addProp (name, value);
                 }
             } else {
                 Logger.println ("Syntax error ': value' expected after "+name);
                 break;
             }
             skipSpaces ();
             if (eatChar (';')) { 
                 continue;
             }
             return root;
         }
         return null;
     }
 
     CSSList getList () { return m_root; }
 
 }
