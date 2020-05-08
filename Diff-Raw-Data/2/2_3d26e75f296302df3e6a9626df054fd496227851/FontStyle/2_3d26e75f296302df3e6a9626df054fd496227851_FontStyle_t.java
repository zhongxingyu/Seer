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
 //#ifndef BlackBerry
 import javax.microedition.lcdui.Font;
 //#endif
 
 public class FontStyle extends Node {
     final static int LEFT = 0;
     final static int MIDDLE = 1;
     final static int RIGHT = 2;
     final static int TOP = 3;
     final static int BASELINE = 4;
     final static int BOTTOM = 5;
     ExternFont m_externFont;
     int size = Font.SIZE_MEDIUM;
     int style = Font.STYLE_BOLD;
     int family = Font.FACE_SYSTEM;
     int justifyH = MIDDLE;
     int justifyV = TOP;
 
     // initialize default font sizes
     static int s_fontLarge  = Font.SIZE_LARGE;
     static int s_fontMedium = Font.SIZE_MEDIUM;
     static int s_fontSmall  = Font.SIZE_SMALL;
 
     // check font size modifier from jad
     static {
         String p;
         if ((p = MiniPlayer.getJadProperty("MEMO-FONT_LARGE")) != null && p.length() > 0) {
             s_fontLarge = getFontToken(p);
         }
         if ((p = MiniPlayer.getJadProperty("MEMO-FONT_MEDIUM")) != null && p.length() > 0) {
             s_fontMedium = getFontToken(p);
         }
         if ((p = MiniPlayer.getJadProperty("MEMO-FONT_SMALL")) != null && p.length() > 0) {
             s_fontSmall = getFontToken(p);
         }
     }
 
     
     static private int getFontToken (String s) {
         if (s.equals ("LARGE")) return Font.SIZE_LARGE;
         if (s.equals ("SMALL")) return Font.SIZE_SMALL;
         return Font.SIZE_MEDIUM;
     }
     
     // Constructor
     FontStyle () {
         super (5);
         //System.out.println ("Text created");
         m_field[0] = new SFFloat (12, this); // size
         m_field[1] = new SFString ("plain", this); // style
         m_field[2] = new MFString (this); // justify
         m_field[3] = new MFString (this); // family
         m_field[4] = new SFBool (false, null); // family
     }
 
     boolean getFilterMode () {
         return ((SFBool)m_field[4]).getValue ();
     }
     void start (Context c) {
         fieldChanged (m_field[0]); // any field triggers the whole parsing
     }
 
     void stop (Context c) {
         if (m_externFont != null) {
             m_externFont.release ();
             m_externFont = null;
         }
     }
     
     public void fieldChanged (Field f) {
         if (f == m_field [0]) {
             int tmpSize = FixFloat.fix2int (((SFFloat)m_field[0]).getValue ());
             m_isUpdated = m_externFont == null || size != tmpSize;
         } else {
             m_isUpdated = true;
         }
     }
 
     final boolean compose (Context c, Region clip, boolean forceUpdate) {
         if (m_isUpdated) {
             openFont (c);
         }
         return isUpdated (forceUpdate);
     }
 
     static public int getNativeFontSize (int size) {
         if (size < 10) {
             return s_fontSmall;
         } else if (size > 14) {
             return s_fontLarge;
         } else {
             return s_fontMedium;
         }
     }
 
     public ExternFont getExternFont (Context c) {
         if (m_externFont == null || m_isUpdated) {
             openFont (c);
             m_isUpdated  = false;
         }
         return m_externFont;
     }
 
     public void openFont (Context c) {
         stop (c);
         // size
         int tmpSize = FixFloat.fix2int (((SFFloat)m_field[0]).getValue ());
         size = getNativeFontSize (tmpSize);
 
         // style
         String str = ((SFString)m_field[1]).getValue();
         if (str == null) {
             style = Font.STYLE_PLAIN;
         } else if (str.equals ("bold")) {
             style = Font.STYLE_BOLD;
         } else if (str.equals ("italic")) {
             style = Font.STYLE_ITALIC;
         } else if (str.equals ("bolditalic")) {
             style = Font.STYLE_ITALIC | Font.STYLE_BOLD;
         } else {
             style = Font.STYLE_PLAIN;
         }         
 
         // justify horizontally
         str = ((MFString)m_field[2]).getValue(0);
         if (str == null) {
             //System.err.println ("no style justif. using left");
             justifyH = LEFT;
         } else if (str.equalsIgnoreCase ("MIDDLE")) {
             justifyH = MIDDLE;
         } else if (str.equalsIgnoreCase ("RIGHT")) {
             justifyH = RIGHT;
         } else { // LEFT
             justifyH = LEFT;
         }
          
         // jutify vertically
         str = ((MFString)m_field[2]).getValue(1);
         if (str == null) {
             justifyV = TOP;
         } else if (str.equalsIgnoreCase ("BOTTOM")) {
             justifyV = BOTTOM;
         } else if (str.equalsIgnoreCase ("BASELINE")) {
             justifyV = BASELINE;
         } else if (str.equalsIgnoreCase ("MIDDLE")) {
             justifyV = MIDDLE;
         } else { // TOP
             justifyV = TOP;
         }
 
         // family
         int nbFamilies = ((MFString)m_field[3]).m_size;
         for (int i = 0; i < nbFamilies; i++) {
             str = ((MFString)m_field[3]).getValue(0);
             if (str == null) {
                 //System.err.println ("no style justif. using System");
                 family = Font.FACE_SYSTEM; break;
             } else if (str.equalsIgnoreCase ("SERIF")) {
                 family = Font.FACE_MONOSPACE; break;
             } else if (str.equalsIgnoreCase ("SANS")) {
                 family = Font.FACE_PROPORTIONAL; break;
             } else if (str.equalsIgnoreCase ("TYPEWRITER")) {
                 family = Font.FACE_SYSTEM; break;
             } else { // try to load extern font
                 family = -1;
                 // remove the part after the # like in times/ABCDE
                 int index = str.indexOf ('/');
                 if (index > 0) {
                     str = str.substring (0, index);
                 }
                 //Logger.println ("FontStyle.openFont: trying: "+str+"_"+tmpSize); 
                 if ( (m_externFont = ExternFont.open (c.decoder, str, tmpSize)) != null) {
                     break;
                 } else {
                    family = Font.FACE_SYSTEM;
                 }
             }
         }
 
         if (m_externFont == null) { // no external font, then open a system one
             m_externFont = ExternFont.open (family, style, size);
         }
     }
 
 }
