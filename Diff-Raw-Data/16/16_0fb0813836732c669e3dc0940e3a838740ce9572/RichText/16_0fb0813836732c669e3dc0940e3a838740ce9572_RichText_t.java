//#condition api.richText
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
 
 /**
    Functionality of RichText content.
    This node providesa a mean to display formatted text defined usiong a syntax 
    very similar to XHTML. The supported elements are:
    <HTML></HTML> opens the document
    <HEAD></HEAD> opens the head section. currently not used
    <BODY></BODY> opens the body section
    <P width="100%" align="LEFT"></P> opens a paragraph. width defines the percentage of maximum width to use
    <B></B> opens a bold section
    <I></I> opens an italic section
    <IMG src=""/ > defines an image
    <A href=""></A>opens a linked section
    <FONT color="#RRGGBB"></FONT>
    <BR/> forces a line break
    <HR/> draws an horizontal line
  **/
 
 package memoplayer;
 //#ifndef BlackBerry
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Image;
 //#endif
 
 class Patch {
     final static int PATCH_COLOR = 1;
     final static int PATCH_BGCOLOR = 2;
     final static int PATCH_LAST = 4; // Also used for PSEUDO_CLASS_*
     int m_propID;
     int m_ival;
     Patch m_next;
 
     Patch (int val, int propID) {
         m_ival = val;
         m_propID = propID;
     }
 
 }
 
 
 class HtmlContext {
     final static int MOD_FONT = 1;
     final static int MOD_BACKGROUND = 2;
     final static int MOD_TEXT = 4;
     final static int MOD_MARGIN = 8;
     final static int MOD_BORDER = 16;
 
     final static int FONT_SERIF = 1;
     final static int FONT_SANS = 2;
     final static int FONT_COUR = 4;
 
     final static int BG_SCROLL = 1;
     final static int BG_FIXED = 2;
 
     final static int BG_REPEAT_NONE = 0;
     final static int BG_REPEAT_X = 1;
     final static int BG_REPEAT_Y = 2;
     final static int BG_REPEAT_BOTH = 3;
 
     final static int BG_POS_SIDE = 1;
     final static int BG_POS_LEFT = 1;
     final static int BG_POS_CENTER = 2;
     final static int BG_POS_RIGHT = 3;
     final static int BG_POS_TOP = 4;
     final static int BG_POS_BOTTOM = 5;
 
     final static int ALIGN_LEFT = 1;
     final static int ALIGN_RIGHT = 2;
     final static int ALIGN_BOTH = 4;
     final static int ALIGN_CENTER = 8;
 
     final static int BORDER_LEFT = 1;
     final static int BORDER_RIGHT = 2;
     final static int BORDER_SIDE = 4;
     final static int BORDER_ALL = 7;
 
     final static int PSEUDO_CLASS_NONE    = 0;
     final static int PSEUDO_CLASS_LINK    = 1;
     final static int PSEUDO_CLASS_VISITED = 2;
     final static int PSEUDO_CLASS_HOVER   = 3;
     final static int PSEUDO_CLASS_ACTIVE  = 4;
 
     final static int INHERIT_AZIMUT            = 1<<1;
     final static int INHERIT_BORDER_COLLAPSE   = 1<<2;
     final static int INHERIT_BORDER_SPACING    = 1<<3;
     final static int INHERIT_CAPTION_SIZE      = 1<<4;
     final static int INHERIT_COLOR             = 1<<5;
     final static int INHERIT_CURSOR            = 1<<6;
     final static int INHERIT_DIRECTION         = 1<<7;
     final static int INHERIT_ELEVATION         = 1<<8;
     final static int INHERIT_EMPTY_CELLS       = 1<<9;
     final static int INHERIT_FONT_FAMILY       = 1<<10;
     final static int INHERIT_FONT_SIZE         = 1<<11;
     final static int INHERIT_FONT_SIZE_ADJUST  = 1<<12;
     final static int INHERIT_FONT_STRETCH      = 1<<13;
     final static int INHERIT_FONT_STYLE        = 1<<14;
     final static int INHERIT_FONT_VARIANT      = 1<<15;
     final static int INHERIT_FONT_WEIGHT       = 1<<16; // done
     final static int INHERIT_LETTER_SPACING    = 1<<17;
     final static int INHERIT_LINE_HEIGHT       = 1<<18;
     final static int INHERIT_LIST_STYLE        = 1<<19;
     final static int INHERIT_LIST_STYLE_IMAGE  = 1<<20;
     final static int INHERIT_LIST_STYLE_POS    = 1<<21;
     final static int INHERIT_LIST_STYLE_TYPE   = 1<<22;
     final static int INHERIT_PAGE              = 1<<23;
     final static int INHERIT_PAGE_BREAK_INSIDE = 1<<24;
     final static int INHERIT_TEXT_ALIGN        = 1<<25;
     final static int INHERIT_TEXT_INDENT       = 1<<26;
     final static int INHERIT_TEXT_TRANSFORM    = 1<<27;
     final static int INHERIT_WHITE_SPACE       = 1<<28;
     final static int INHERIT_WIDOWS            = 1<<29;
     final static int INHERIT_WORD_SPACING      = 1<<30;
     final static int INHERIT_SPARE_TOKEN1      = 1<<31;
 
     final static String [] borderTypeNames = {
         "none", "hidden", "dotted", "dashed", "solid", "double", "groove", "ridge", "inset", "outset"
     };
 
     int m_modif;
     int m_inherit;
     CSSList m_style;
     CSSList m_default;
 
     int m_color = 0;
     boolean m_bold; // True if font is bold
     boolean m_italic;
     boolean m_underline;
     int m_size = 12; // font size: small if s <= 12, medium if  12 < s < 20 and large id s >= 20 
     int m_actualSize; // the actual font size returned by the font engine, in thery very close to m_size;
     int m_sizeIncr = 0; // if not 0, must be used intead of m_size in addition to the previous font size (i.e. smaller, larger)
     String m_src;
     String m_href;
     int m_imgWidth = -1, m_imgHeight = -1;
     int m_align = ALIGN_LEFT; // ALIGN_LEFT.. ALIGN_CENTER
     int m_percent; // 1 .. 100 percent of the maximum width
     Font m_font; 
     boolean m_rtl; // LTR or RTL direction 
 
     // HR
     boolean m_noshade;
 
     // background
     int m_bgColor = -1;
     String m_bgUrl;
     int m_bgAttachement;
     int m_bgPositionX, m_bgPositionY, m_bgPositionType;
     int m_bgRepeat;
     Image m_bgImage;
     
     // border
     final static int I_WIDTH = 0;
     final static int I_WIDTH_TYPE = 1;
     final static int I_COLOR = 2;
     final static int I_STYLE = 3;
     int [] m_borders;
     //int m_boWidth, m_boWidthType, m_boColor, m_boStyle;
 
     // margins
     final static int I_TOP = 0;
     final static int I_LEFT = 1;
     final static int I_BOTTOM = 2;
     final static int I_RIGHT = 3;
     final static int I_TYPE = 4;
     int [] m_margins;
     int [] m_paddings;
 
     // width
     int m_width, m_widthType;
 
     // the pseudo classes for links
     int m_pseudoClassmode = 0; // used for CSS decoding, then for link state
     Patch [] m_patches;
 
     // the next context in a list
     HtmlContext m_next;
 
     HtmlContext () {
         //Logger.println ("creating new HtmlContext: "+this);
     }
     HtmlContext dup () {
         HtmlContext c = new HtmlContext ();
         c.m_inherit = m_inherit;
         c.m_font = m_font;
         c.m_color = m_color;
         // font section is inherited
         c.m_bold = m_bold;
         c.m_italic = m_italic;
         c.m_underline = m_underline;
         c.m_size = m_size;
         c.m_actualSize = m_actualSize;
         c.m_sizeIncr = m_sizeIncr;
         c.m_underline = m_underline;
         // background is not inherited
         c.m_bgColor = -1;
         //c.m_bgPositionType = m_bgPositionX = m_bgPositionY = 0;
         //c.m_bgUrl = null;
         c.m_bgAttachement = BG_SCROLL;
         c.m_bgRepeat = BG_REPEAT_BOTH;
 
         // text
         c.m_align = m_align;
         c.m_rtl = m_rtl;
 
         // patches for pseudo classes
         m_pseudoClassmode = PSEUDO_CLASS_NONE;
         m_patches = null;
 
         // private
         c.m_next = this;
         return c;
     }
 
     public String toString () {
         return ("color:"+Integer.toHexString(m_color)+", bold:"+m_bold+", ital:"+m_italic+" / "+super.toString ());
     }
 
     void addDefaultProp (int id, String val) {
         if (m_default == null) {
             m_default = new CSSList ("", "", null);
         }
         int max = CSSList.s_ids.length;
         for (int i = 0; i < max; i++) {
             if (CSSList.s_ids[i] == id) {
                 m_default.addProp (i, val);
                 return;
             }
         }
     }
 
     //int getModif () { return m_modif; }
 
     void setPseudoClassMode (int mode) {
         Logger.println ("setPseudoClassmode "+mode+" for "+this);
         m_pseudoClassmode = mode;
         if (mode != PSEUDO_CLASS_NONE && m_patches == null) {
             m_patches = new Patch [Patch.PATCH_LAST];
         }
     }
 
 
     void addPatch (Patch patch) {
         Logger.println ("addPatch "+patch+" for "+this);
         patch.m_next = m_patches [m_pseudoClassmode-1];
         m_patches [m_pseudoClassmode-1] = patch;
     }
 
     void setColor (CSSProp p) {
         int color = p.m_type == CSSProp.TYPE_COLOR ? p.m_value : 0;
         if (m_pseudoClassmode == PSEUDO_CLASS_NONE) {
             //Logger.println ("setColor: setting the color "+p.m_val+" to "+this);
             m_color = color;
             m_inherit |= INHERIT_COLOR;
         } else {
             Logger.println ("setColor: adding a color patch to "+this);
             addPatch (new Patch (color, Patch.PATCH_COLOR));
         }
     }
     // background section
     void setBgColor (CSSProp p) {
         int color;
         if (p.m_val.equals ("transparent")) {
             color = -1;
         } else {
             color = p.m_type == CSSProp.TYPE_COLOR ? p.m_value : 0;
         }
         if (m_pseudoClassmode == PSEUDO_CLASS_NONE) {
             m_bgColor = color;
             m_modif |= MOD_BACKGROUND;
         } else {
             addPatch (new Patch (color, Patch.PATCH_BGCOLOR));
         }
     }
     void setBgImageUrl (CSSProp p) {
         m_bgUrl = p.m_type == CSSProp.TYPE_URL ? p.m_sval : "";
         m_bgImage = null;
         m_modif |= MOD_BACKGROUND;
         //Logger.println ("HtmlContext.setBGUrl: "+val+" => "+m_bgUrl);
     }
     void setBgRepeat (CSSProp p) {
         String val = p.m_val;
         if (val.equals ("repeat")) {
             m_bgRepeat = BG_REPEAT_BOTH;
         } else if (val.equals ("repeat-x")) {
             m_bgRepeat = BG_REPEAT_X;
         } else if (val.equals ("repeat-y")) {
             m_bgRepeat = BG_REPEAT_Y;
         } else {
             m_bgRepeat = BG_REPEAT_NONE;
         }
         //Logger.println ("HtmlContext.setBGRepeat: "+val+" => "+m_bgRepeat);
     }
     void setBgAttachement (CSSProp p) {
         String val = p.m_val;
         m_bgAttachement = val.equals ("fixed") ? BG_FIXED : BG_SCROLL;
         //Logger.println ("HtmlContext.setBgAttachement: "+val+" => "+m_bgAttachement);
     }
     void setBgPosition (CSSProp p) {
         String val = p.m_val;
         if (val.startsWith ("top")) {
             m_bgPositionType = BG_POS_SIDE;
             m_bgPositionY = BG_POS_TOP;
         } else if (val.startsWith ("center")) {
             m_bgPositionType = BG_POS_SIDE;
             m_bgPositionY = BG_POS_CENTER;
         } else if (val.startsWith ("bottom")) {
             m_bgPositionType = BG_POS_SIDE;
             m_bgPositionY = BG_POS_BOTTOM;
         } else if (p.m_type == CSSProp.TYPE_PERCENT) {
             m_bgPositionType = CSSProp.TYPE_PERCENT;
             m_bgPositionX = p.m_value;
             m_bgPositionY = p.m_value2;
         } else if (p.m_type == CSSProp.TYPE_PIXEL) {
             m_bgPositionType = CSSProp.TYPE_PIXEL;
             m_bgPositionX = p.m_value;
             m_bgPositionY = p.m_value2;
         } else if (p.m_type == CSSProp.TYPE_EM) {
             m_bgPositionType = CSSProp.TYPE_EM;
             m_bgPositionX = p.m_value;
             m_bgPositionY = p.m_value2;
         }
         if (m_bgPositionType == BG_POS_SIDE) { // have to check the second arg
             if (val.indexOf ("left", 3) != -1) {
                 m_bgPositionY = BG_POS_LEFT;
             } else if (val.indexOf ("right", 3) != -1) {
                 m_bgPositionY = BG_POS_RIGHT;
             } else {
                 m_bgPositionY = BG_POS_CENTER;
             }
         }
         //Logger.println ("HtmlContext.setBgAttachement: "+val+" => "+m_bgPositionType+"/"+m_bgPositionX+","+m_bgPositionY);
     }
 
     // font section
     void setFontWeight (CSSProp p) {
         String val = p.m_val;
         m_bold = val.equals ("bold") || val.equals ("bolder")  || p.m_value  > (500 << 16);
         m_modif |= HtmlContext.MOD_FONT;
         m_inherit |= INHERIT_FONT_WEIGHT;
     }
 
     void setFontStyle (CSSProp p) {
         String val = p.m_val;
         m_italic = val.equals ("italic") || val.equals ("oblique");
         m_modif |= HtmlContext.MOD_FONT;
         m_inherit |= INHERIT_FONT_STYLE;
     }
 
     void setFontFamily (CSSProp p) {
         String val = p.m_val;
         if (val.startsWith ("times") || val.equals ("serif")) {
         } else if (val.startsWith ("helvetica") || val.startsWith ("geneva") || val.equals ("sans")) {
         } if (val.equals ("courrier") || val.equals ("")) {
         }
         m_modif |= HtmlContext.MOD_FONT;
     }
 
     void setFontSize (CSSProp p) {
         m_sizeIncr = 0;
         String val = p.m_val;
         if (p.m_type == CSSProp.TYPE_NUM) {
             m_size = p.m_value >> 16;
         } else if (p.m_type == CSSProp.TYPE_PERCENT) {
             m_sizeIncr = p.m_value;
         } else if (val.equals ("medium")) {
             m_size = 16;
         } else if (val.endsWith ("small")) {
             m_size = 10;
         } else if (val.endsWith ("large")) {
             m_size = 20;
         } else if (val.equals ("larger")) {
             m_sizeIncr = 1;
         } else if (val.equals ("smaller")) {
             m_sizeIncr = -1;
         } else {
             return; // unknown size
         }
         m_modif |= HtmlContext.MOD_FONT;
     }
 
     // border
     void setBorderWidth (CSSProp p) {
         String val = p.m_val;
         if (m_borders == null) { m_borders = new int [4]; }
         m_borders[I_WIDTH_TYPE] = CSSProp.TYPE_PIXEL;
         if (val.equals ("thin")) {
             m_borders[I_WIDTH] = 1;
         } else if (val.equals ("medium")) {
             m_borders[I_WIDTH] = 2;
         } else if (val.equals ("thick")) {
             m_borders[I_WIDTH] = 4;
         } else if (p.m_type == CSSProp.TYPE_PERCENT) {
             m_borders[I_WIDTH_TYPE] = CSSProp.TYPE_PERCENT;
             m_borders[I_WIDTH] = p.m_value;
         } else if (p.m_type == CSSProp.TYPE_PIXEL) {
             m_borders[I_WIDTH_TYPE] = CSSProp.TYPE_PIXEL;
             m_borders[I_WIDTH] = p.m_value;
         } else if (p.m_type == CSSProp.TYPE_EM ) {
             m_borders[I_WIDTH_TYPE] = CSSProp.TYPE_EM;
             m_borders[I_WIDTH] = p.m_value;
         } else {
             m_borders[I_WIDTH] = 0;
         }
         m_modif |= MOD_BORDER;
         //Logger.println ("setBorderWidth: "+val+" => "+m_boWidth+" / "+m_boWidthType);
     }
 
     void setBorderStyle (CSSProp p) {
         String val = p.m_val;
         if (m_borders == null) { m_borders = new int [4]; }
         m_borders[I_STYLE] = 0;
         for (int i = 0; i < 10; i++) {
             if (val.equals (borderTypeNames[i])) {
                 m_borders[I_STYLE] = i; break;
             }
         }
         m_modif |= MOD_BORDER;
         //Logger.println ("setBorderStyle: "+val+" => "+borderTypeNames[m_boStyle]);
     }
 
     void setBorderColor (CSSProp p) {
         if (m_borders == null) { m_borders = new int [4]; }
         m_borders[I_COLOR] = p.m_type == CSSProp.TYPE_COLOR ? p.m_value : 0;
         m_modif |= MOD_BORDER;
         //Logger.println ("setBorderColor: "+val+" => "+Integer.toHexString (m_boColor));
     }
 
 // text
     void setTextAlign (CSSProp p) {
         setTextAlign (p.m_val);
     }
     void setTextAlign (String val) {
         switch (val.charAt(0)) {
         case 'l' :
         case 'L' : m_align = ALIGN_LEFT; break;
         case 'r' :
         case 'R' : m_align = ALIGN_RIGHT; break;
         case 'c' :
         case 'C' :
         case 'm' :
         case 'M' : m_align = ALIGN_CENTER; break;
         case 'j' :
         case 'J' : m_align = ALIGN_BOTH; break;
         }
     }
     
     void setTextDir (boolean rtl) {
         m_rtl = rtl;
     }
 
     void setTextDecoration (CSSProp p) {
         String val = p.m_val;
         if (val.equalsIgnoreCase ("underline")) {
             m_underline = true;
         } else {
             m_underline = false;
         }
     }
 
     void setMargin (CSSProp p, int edge) {
         if (m_margins == null) { m_margins = new int [8]; }
         int type = 0;
         int amount = 0;
         //Logger.println ("setMargin of '"+val+"' for "+edge);
         if (p.m_type == CSSProp.TYPE_PERCENT) {
             type = CSSProp.TYPE_PERCENT;
             amount = p.m_value;
             //Logger.println ("setMargin by %: "+amount);
         } else if (p.m_type == CSSProp.TYPE_PIXEL) {
             type = CSSProp.TYPE_PIXEL;
             amount = p.m_value;
             //Logger.println ("setMargin by pixel: "+amount);
         } else if (p.m_type == CSSProp.TYPE_EM) {
             type = CSSProp.TYPE_EM;
             amount = p.m_value;
             //Logger.println ("setMargin by EM: "+amount);
         }
         switch (edge) {
         case CSSProp.MARGIN_LEFT:
             m_margins[I_LEFT] = amount;
             m_margins[I_TYPE+I_LEFT] = type;
             break;
         case CSSProp.MARGIN_RIGHT:
             m_margins[I_RIGHT] = amount;
             m_margins[I_TYPE+I_RIGHT] = type;
             break;
         case CSSProp.MARGIN_TOP:
             m_margins[I_TOP] = amount;
             m_margins[I_TYPE+I_TOP] = type;
             break;
         case CSSProp.MARGIN_BOTTOM:
             m_margins[I_BOTTOM] = amount;
             m_margins[I_TYPE+I_BOTTOM] = type;
             break;
         }
     }
 
     void setPadding (CSSProp p, int edge) {
         if (m_paddings == null) { m_paddings = new int [8]; }
         int type = CSSProp.TYPE_PIXEL;
         int amount = 0;
         if (p.m_type == CSSProp.TYPE_PERCENT) {
             type = CSSProp.TYPE_PERCENT;
             amount = p.m_value;
         } else if (p.m_type == CSSProp.TYPE_PIXEL) {
             type = CSSProp.TYPE_PIXEL;
             amount = p.m_value;
         } else if (p.m_type == CSSProp.TYPE_EM) {
             type = CSSProp.TYPE_EM;
             amount = p.m_value;
         }
         switch (edge) {
         case CSSProp.PADDING_LEFT:
             m_paddings[I_LEFT] = amount;
             m_paddings[I_TYPE+I_LEFT] = type;
             break;
         case CSSProp.PADDING_RIGHT:
             m_paddings[I_RIGHT] = amount;
             m_paddings[I_TYPE+I_RIGHT] = type;
             break;
         case CSSProp.PADDING_TOP:
             m_paddings[I_TOP] = amount;
             m_paddings[I_TYPE+I_TOP] = type;
             break;
         case CSSProp.PADDING_BOTTOM:
             m_paddings[I_BOTTOM] = amount;
             m_paddings[I_TYPE+I_BOTTOM] = type;
             break;
         }
     }
 
     void setWidth (CSSProp p) {
         if (p.m_type == CSSProp.TYPE_PERCENT) {
             m_widthType = CSSProp.TYPE_PERCENT;
             m_width = p.m_value;
         } else if (p.m_type == CSSProp.TYPE_PIXEL) {
             m_widthType = CSSProp.TYPE_PIXEL;
             m_width = p.m_value;
         } else if (p.m_type == CSSProp.TYPE_EM) {
             m_widthType = CSSProp.TYPE_EM;
             m_width = p.m_value;
         } else {
             m_widthType = CSSProp.TYPE_NONE;
             m_width = 0;
         }
         //Logger.println ("setWidth "+val+" -> "+m_widthType+" / "+m_width);
     }
 
     boolean checkInheritance (HtmlContext hc, int prop) {
         if ( (m_inherit & prop) != prop) {
             if ( (hc.m_inherit & prop) == prop) {
                 m_inherit |= prop;
                 m_font = null;
                 return true;
             }
         }
         return false;
     }
 
     void updateContext (HtmlContext hc) {
         m_font = hc.m_font;
         //Logger.println ("updateContext prev: "+hc+", this: "+this);
         if (checkInheritance (hc, INHERIT_FONT_WEIGHT)) {
             //Logger.println ("updateContext inherit bold: ");
             m_bold = hc.m_bold;
         }
         if (checkInheritance (hc, INHERIT_COLOR)) {
             //Logger.println ("updateContext inherit color: from "+hc+" to "+this);
             m_color = hc.m_color;
         }
         if (checkInheritance (hc, INHERIT_FONT_STYLE)) {
             //Logger.println ("updateContext inherit italic: ");
             m_italic = hc.m_italic;
         }
 
         //Logger.println ("updateContext modif: "+m_modif+" bold:"+m_bold+", ital:"+m_italic);
         if (m_font == null || (m_modif & MOD_FONT) == MOD_FONT) {
             m_font = Font.getFont (
                 Font.FACE_PROPORTIONAL,
                 (m_bold ? Font.STYLE_BOLD : 0)|(m_italic ? Font.STYLE_ITALIC : 0)|((m_underline || m_href != null) ? Font.STYLE_UNDERLINED : 0),
                 FontStyle.getNativeFontSize (m_size)
                 ); 
             m_actualSize = m_font.getHeight ();
         }
         //m_mod = 0;
     }
 
     int findIntPatch (Patch patch, int propID, int defval) {
         while (patch != null) {
             if (patch.m_propID == propID) {
                 return patch.m_ival;
             }
             patch = patch.m_next;
         }
         return defval;
     }
 
     void updateGraphic (Graphics g, HtmlContext hc) {
         if (hc == null || hc.m_href != m_href || hc.m_color != m_color) {
             if (m_href != null) { // a context for an anchor
                 if (m_patches != null && m_pseudoClassmode != 0) {
                     g.setColor (findIntPatch (m_patches [m_pseudoClassmode-1], Patch.PATCH_COLOR, 0x0000FF));
                 } else {
                     g.setColor (0x0000FF);
                 }
             } else {
                 g.setColor (m_color);
             }
         }
         g.setFont (m_font);
     }
 
 
     int getMargin (int t, int width) {
         int val = 0, type = CSSProp.TYPE_NONE;
         switch (t) {
         case CSSProp.MARGIN_LEFT:
             if (m_margins != null) {
                 val = m_margins[I_LEFT]; type = m_margins[I_TYPE+I_LEFT]; break;
             }
         case CSSProp.MARGIN_RIGHT:
             if (m_margins != null) {
                 val = m_margins[I_RIGHT]; type = m_margins[I_TYPE+I_RIGHT]; break;
             }
         case CSSProp.MARGIN_TOP:
             if (m_margins != null) {
                 val = m_margins[I_TOP]; type = m_margins[I_TYPE+I_TOP]; break;
             }
         case CSSProp.MARGIN_BOTTOM:
             if (m_margins != null) {
                 val = m_margins[I_BOTTOM]; type = m_margins[I_TYPE+I_BOTTOM]; break;
             }
         case CSSProp.PADDING_LEFT:
             if (m_paddings != null) {
                 val = m_paddings[I_LEFT]; type = m_paddings[I_TYPE+I_LEFT]; break;
             }
         case CSSProp.PADDING_RIGHT:
             if (m_paddings != null) {
                 val = m_paddings[I_RIGHT]; type = m_paddings[I_TYPE+I_RIGHT]; break;
             }
         case CSSProp.PADDING_TOP:
             if (m_paddings != null) {
                 val = m_paddings[I_TOP]; type = m_paddings[I_TYPE+I_TOP]; break;
             }
         case CSSProp.PADDING_BOTTOM:
             if (m_paddings != null) {
                 val = m_paddings[I_BOTTOM]; type = m_paddings[I_TYPE+I_BOTTOM]; break;
             }
         case CSSProp.BORDER_WIDTH:
             if (m_borders != null) {
                 val = m_borders[I_WIDTH]; type = m_borders[I_WIDTH_TYPE]; break;
             }
         default:
             return 0;
         }
         if (type == CSSProp.TYPE_EM) {
             return (val * m_actualSize) >> 16;
         } else if (type == CSSProp.TYPE_PERCENT) {
             return ((val * width) / 100) >> 16;
         } else if (type == CSSProp.TYPE_PIXEL) {
             return val >> 16;
         } else { // not defined
             return 0;
         }
     }
 
     int getWidth (int width) {
         if (m_widthType == CSSProp.TYPE_EM) {
             return (m_width * m_actualSize) >> 16;
         } else if (m_widthType == CSSProp.TYPE_PERCENT) {
             //Logger.println ("getWidth by %: "+m_width+" * "+width+" / 100");
             return ((m_width * width) / 100) >> 16;
         } else if (m_widthType == CSSProp.TYPE_PIXEL) {
             //Logger.println ("getWidth by pixel: "+m_width);
             return m_width >> 16;
         } else { // not defined
             //Logger.println ("getWidth not defined: "+width);
             return width;
         }
     }
 
 }
 
 // class to manage the properties defined in the various places inside an HTML document. 
 // It provides handy functions to retrieve values
 class PropertiesManager {
     CSSList m_inline; // style coming from a style section in the document
     CSSList m_extern; // style coming from an external style shit
     CSSList m_browser; // style coming from the browser
 
     CSSReader m_reader; // the reader used to parse various style definitions
 
     PropertiesManager () {
         m_reader = new CSSReader ();
     }
 
     void parseInline (String data) {
         CSSReader reader = new CSSReader (data, CSSReader.BUFFER); //+CSSReader.DEBUG);
         m_inline = reader.getList ();
         // if (m_inline != null) { m_inline.print (); } 
     }
 
     void parseBrowser (String data) {
         CSSReader reader = new CSSReader (data, CSSReader.BUFFER); //+CSSReader.DEBUG);
         m_browser = reader.getList ();
         // if (m_inline != null) { m_inline.print (); } 
     }
 
     void parseExternal (String url) {
         CSSReader reader = new CSSReader (url, CSSReader.BUFFER); //+CSSReader.DEBUG);
         m_extern = reader.getList ();
         // if (m_extern != null) { m_extern.print (); }
     }
 
     CSSList parseInternal (String value) {
         return m_reader.parseInternal (value);
     }
     
     void iterateProperty (CSSProp prop, Fragment f, String pseudoClass) {
         if (pseudoClass != null) {
             if (pseudoClass.equalsIgnoreCase ("link")) {
                 f.m_hcontext.setPseudoClassMode (HtmlContext.PSEUDO_CLASS_LINK);
             } else if (pseudoClass.equalsIgnoreCase ("visited")) {
                 f.m_hcontext.setPseudoClassMode (HtmlContext.PSEUDO_CLASS_VISITED);
             } else if (pseudoClass.equalsIgnoreCase ("hover")) {
                 f.m_hcontext.setPseudoClassMode (HtmlContext.PSEUDO_CLASS_HOVER);
             } else if (pseudoClass.equalsIgnoreCase ("active")) {
                 f.m_hcontext.setPseudoClassMode (HtmlContext.PSEUDO_CLASS_ACTIVE);
             }
         }
         while (prop != null) {
             f.useProperty (prop);  
             prop = prop.m_next;
         }
         if (pseudoClass != null) {
             f.m_hcontext.setPseudoClassMode (HtmlContext.PSEUDO_CLASS_NONE);
         }
     }
 
     void checkList (CSSList list, String tagName, String className, Fragment f) {
         //Logger.println ("checkList : "+tagName+", "+className);
         if (list != null) {
             CSSProp prop;
             CSSList tmp;
             if (tagName != null) {
                 tmp = list.findByTag (tagName);
                 while (tmp != null) {
                     //Logger.println ("    > found "+tmp+" / "+tmp.m_pseudoClass);
                     iterateProperty (tmp.m_properties, f, tmp.m_pseudoClass);
                     tmp = tmp.m_next;
                     if (tmp != null) {
                         tmp = tmp.findByTag (tagName);
                     }
                 }
             }
             if (className != null) {
                 tmp = list.findByClass (className);
                 while (tmp != null) {
                     iterateProperty (tmp.m_properties, f, tmp.m_pseudoClass);
                     tmp = tmp.m_next;
                     if (tmp != null) {
                         tmp = tmp.findByClass (className);
                     }
                 }
             }
             if (tagName != null && className != null) {
                 tmp = list.findByTagAndClass (tagName, className);
                 while (tmp != null) {
                     iterateProperty (tmp.m_properties, f, tmp.m_pseudoClass);
                     tmp = tmp.m_next;
                     if (tmp != null) {
                         tmp = tmp.findByTagAndClass (tagName, className);
                     }
                 }
             }
         }
     }
 
     void parseProperty (String tagName, String className, Fragment f) {
         if (f.m_hcontext.m_default != null) {
             iterateProperty (f.m_hcontext.m_default.m_properties, f, null);
         }
         checkList (m_extern, tagName, className, f);
         checkList (m_inline, tagName, className, f);
         if (f.m_hcontext.m_style != null) {
             iterateProperty (f.m_hcontext.m_style.m_properties, f, null);
         }
         checkList (m_browser, tagName, className, f);
     }
 }
 
 class FragStack {
     protected Fragment m_fragment;
     protected FragStack m_next;
 
     protected static FragStack s_root;
     protected static FragStack s_pool;
 
     static void push (Fragment f) {
         FragStack fs;
         if (s_pool == null) {
             fs = new FragStack ();
         } else {
             fs = s_pool;
             s_pool = fs.m_next;
         }
         fs.m_fragment = f;
         fs.m_next = s_root;
         s_root = fs;
     }
 
     static Fragment pop () {
         if (s_root == null) {
             return null;
         }
         FragStack fs = s_root;
         s_root = fs.m_next;
         fs.m_next = s_pool;
         s_pool = fs;
         return fs.m_fragment;
     }
 
 }
 
 // base class of all fragments, contains a bounding box and a compose abstract method
 class Fragment {
     final static int LINE_HEIGHT  = 0;
     final static int LEFT_OFFSET  = 1;
     final static int RIGHT_OFFSET = 2;
     final static int DISPLAY_MODE = 3;
     final static int FIRST        = 4;
     final static int LAST         = 5;
     final static int BORDER_WIDTH = 6;
     final static int MAX_SLOTS    = 7;
 
     static int INLINE = 1;
     static int BLOCK = 2;
     static int HIDDEN = 3;
 
     int m_dxy; //// the position of the fragment dx = dxy & 0xFFFF; dy = dxy >>16
     int m_size; //m_width, m_height; // the size of the fragment width = m_size & 0xFFFF; height = m_size >> 16
     byte [] m_data; // place holder for line height, left and right offset, display mode, first and last offset, border width... see constants above.
     HtmlContext m_hcontext;
     Fragment m_next; // to permanently link fragment together
 
     Fragment (HtmlContext context) {
         m_hcontext = context;
         m_data = new byte [MAX_SLOTS];
     }
     
     int getFullWidth () { 
         return (m_size & 0xFFFF) + m_data[RIGHT_OFFSET] + m_data[LEFT_OFFSET];
     }
 
     int getHeight () { return m_size >> 16; }
     
     boolean isVisible (int start, int end) {
         int y = m_dxy >> 16;
         int h = getHeight ();
         return (y >= start && y+h <= end);
     }
 
     static void pushFragment (Fragment f) {
 //         f.m_link = s_stack;
 //         s_stack = f;
         FragStack.push (f);
     }
 
 
     static Fragment popFragment () {
 //         Fragment f;
 //         if (s_stack != null) {
 //             f = s_stack;
 //             s_stack = f.m_link;
 //         } else {
 //             f = null;
 //         }
 //         return f;
         return FragStack.pop ();
     }
 
     Fragment getFirst () { 
         //Logger.println ("getFirst: return base "+this);
         return this;
     }
 
     Fragment getNext () {
         if (m_next != null) {
             return m_next.getFirst ();
         }
         Fragment f = popFragment ();
         f = f == null ? null : f.getNext ();
         return f;
     }
 
     void computeBox (Context c, int width) { m_dxy = m_size = 0; }
 
     void updateContext (HtmlContext hc) {
         if (m_hcontext.m_font == null) {
             m_hcontext.updateContext (hc);
         }
     }
     boolean hasStartingSpace () { return false; }
 
     boolean hasEndingSpace () { return false; }
 
     boolean isSelectedAt (int x, int y) {
         int x0 = m_dxy & 0xFFFF;
         int y0 = m_dxy >> 16;
         int w0 = getFullWidth ();
         int h0 = getHeight ();
         //Logger.println ("Comparing "+x+"x"+y+" to "+x0+"x"+y0+"+"+w0+":"+h0+": "+this);
         return (x0<x && x0+w0>x && y0<y && y0+h0>y);
     }
 
     int computeBorderWidth (int width) {
         if (m_hcontext.m_borders == null || m_hcontext.m_borders[HtmlContext.I_STYLE] < 2)  {
             return m_data[BORDER_WIDTH] = 0;
         } else {
             return m_data[BORDER_WIDTH] = (byte) (m_hcontext.getMargin (CSSProp.BORDER_WIDTH, width));
         }
     }
 
     void updateFromStyles (PropertiesManager propMgr) {
         m_data[LEFT_OFFSET] = m_data[RIGHT_OFFSET] = 0;
         if (m_data[DISPLAY_MODE] == INLINE) {
             int width = m_size & 0xFFFF;
             int bw = computeBorderWidth (width);
             if (m_data[FIRST] > 0) {
                 m_data[LEFT_OFFSET] = (byte)(bw+m_hcontext.getMargin (CSSProp.MARGIN_LEFT, width) + m_hcontext.getMargin (CSSProp.PADDING_LEFT, width));
             } 
             if (m_data[LAST] > 0) {
                 m_data[RIGHT_OFFSET] = (byte)(bw+m_hcontext.getMargin (CSSProp.MARGIN_RIGHT, width) + m_hcontext.getMargin (CSSProp.PADDING_RIGHT, width));
             }
         }
         
     }
 
     void useProperty (CSSProp prop) {
         //Logger.println ("    > "+CSSList.s_names[prop.m_id]+" : "+prop.m_val);
         int propId = CSSList.s_ids[prop.m_id];
         switch (propId) {
         case CSSProp.FONT_WEIGHT :
             m_hcontext.setFontWeight (prop);
             break;
         case CSSProp.FONT_STYLE :
             m_hcontext.setFontStyle (prop);
             break;
         case CSSProp.FONT_SIZE :
             m_hcontext.setFontSize (prop);
             break;
         case CSSProp.FONT_FAMILY :
             m_hcontext.setFontFamily (prop);
             break;
         case CSSProp.BG_COLOR :
             m_hcontext.setBgColor (prop);
             break;
         case CSSProp.BG_IMAGE :
             m_hcontext.setBgImageUrl (prop);
             break;
         case CSSProp.BG_ATTACHEMENT :
             m_hcontext.setBgAttachement (prop);
             break;
         case CSSProp.BG_POSITION :
             m_hcontext.setBgPosition (prop);
             break;
         case CSSProp.BG_REPEAT :
             m_hcontext.setBgRepeat (prop);
             break;
         case CSSProp.FG_COLOR :
             m_hcontext.setColor (prop);
             break;
         case CSSProp.TEXT_ALIGN :
             m_hcontext.setTextAlign (prop);
             break;
         case CSSProp.TEXT_DECORATION :
             m_hcontext.setTextDecoration (prop);
             break;
         case CSSProp.TEXT_INDENT :
             break;
         case CSSProp.LINE_HEIGHT :
             break;
         case CSSProp.MARGIN_LEFT :
         case CSSProp.MARGIN_RIGHT :
         case CSSProp.MARGIN_TOP :
         case CSSProp.MARGIN_BOTTOM :
             m_hcontext.setMargin (prop, propId);
             break;
         case CSSProp.PADDING_LEFT :
         case CSSProp.PADDING_RIGHT :
         case CSSProp.PADDING_TOP :
         case CSSProp.PADDING_BOTTOM :
             m_hcontext.setPadding (prop, propId);
             break;
         case CSSProp.BORDER_COLOR :
             m_hcontext.setBorderColor (prop);
             break;
         case CSSProp.BORDER_WIDTH :
             m_hcontext.setBorderWidth (prop);
             break;
         case CSSProp.BORDER_STYLE :
             m_hcontext.setBorderStyle (prop);
             break;
         case CSSProp.WIDTH :
             m_hcontext.setWidth (prop);
             break;
         }
     }
 
     int getSpace () { return m_hcontext.m_font.charWidth (' '); }
 
     void setOffset (int dx, int dy, int lh) { 
         //Logger.println ("Fragment.setOffset: "+dx+", "+dy+" for "+this);
         m_dxy = dx + (dy<<16); //m_dx = dx; m_dy = dy; 
         m_data[LINE_HEIGHT] = (byte)lh;
     }
     void cumulOffset (int dx, int dy) { 
         //Logger.println ("Fragment.cumulOffset: "+dx+", "+dy+" for "+this+" / "+m_dx+", "+m_dy);
         m_dxy += dx + (dy<<16); //m_dx += dx; m_dy += dy;
     }
 
     void drawBackground (Graphics g, int x, int y, int w, int h) {
         if ( (m_hcontext.m_modif & HtmlContext.MOD_BACKGROUND) == HtmlContext.MOD_BACKGROUND) {
             //Logger.println ("drawBackground: x:"+x+", y:"+y+", w:"+w+", h:"+h);
             if (m_hcontext.m_bgColor != -1) {
                 g.setColor (m_hcontext.m_bgColor);
                 g.fillRect (x, y, w, h);
             }
         }
     }
     void drawBorder (Graphics g, int x, int y, int w, int h, int sides) {
         int bw = m_data[BORDER_WIDTH];
         if ( (m_hcontext.m_modif & HtmlContext.MOD_BORDER) == HtmlContext.MOD_BORDER && bw > 0) {
             g.setColor (m_hcontext.m_borders[HtmlContext.I_COLOR]);
             //Logger.println ("drawBorder: style="+m_hcontext.m_borders[HtmlContext.I_STYLE]+" color: "+Integer.toHexString(m_hcontext.m_borders[HtmlContext.I_COLOR])+
             //    ", border width"+bw);
             if (m_hcontext.m_borders[HtmlContext.I_STYLE] == 2 || m_hcontext.m_borders[HtmlContext.I_STYLE] == 3) {
                 g.setStrokeStyle (Graphics.DOTTED);
             }
             int x1, y1, x2, y2, t;
             if ( (sides & HtmlContext.BORDER_SIDE) == HtmlContext.BORDER_SIDE) {
                 x1 = x+bw; y1 = y; x2 = x+w-bw-1; y2 = y; t = h-bw;
                 //Logger.println ("drawBorder: x:"+x+", y:"+y+", w:"+w+", h:"+h);
                 for (int i = 0; i < bw; i++) {
                     g.drawLine (x1, y1, x2, y2);
                     g.drawLine (x1, y1+t, x2, y2+t);
                     y1++; y2++;
                 }
 //             }
 //             if ( (sides & HtmlContext.BORDER_BOTTOM) == HtmlContext.BORDER_BOTTOM) {
 //                 x1 = x+bw; y1 = y+h-1; x2 = x+w-bw-1; y2 = y+h-1; 
 //                 for (int i = 0; i < bw; i++) {
 //                     g.drawLine (x1, y1, x2, y2);
 //                     y1--; y2--;
 //                 }
             }
             if ( (sides & HtmlContext.BORDER_LEFT) == HtmlContext.BORDER_LEFT) {
                 x1 = x; y1 = y; x2 = x; y2 = y+h-1; 
                 for (int i = 0; i < bw; i++) {
                     g.drawLine (x1, y1, x2, y2);
                     x1++; x2++;
                 }
             }
             if ( (sides & HtmlContext.BORDER_RIGHT) == HtmlContext.BORDER_RIGHT) {
                 x1 = x+w-1; y1 = y; x2 = x1; y2 = y+h-1; 
                 for (int i = 0; i < bw; i++) {
                     g.drawLine (x1, y1, x2, y2);
                     x1--; x2--;
                 }
             }
             g.setStrokeStyle (Graphics.SOLID);
         }
     }
 
     void draw (Graphics g, HtmlContext hc, int x, int y, int start, int end) {
         // draw element
         Logger.println ("unexpected call to Fragment.draw");
     }
 
     void print (String decal) {
         Logger.println (decal+this);
         if (m_next != null) {
             m_next.print (decal);
         }
     }
 
 }
 
 class SpecialFragment extends Fragment {
     final static int BREAK = 1;
     final static int RULER = 2;
     final static int SPACER = 3;
     int m_mode; // which kind of special fragment is it
     int m_lastColor = -1, m_dark, m_light;
 
     SpecialFragment (HtmlContext context, int mode) {
         super (context);
         m_mode = mode;
         m_dark = 0;
         m_light = 0X808080;
     }
 
     int getSpace () { return 0; }
 
     void computeBox (Context c, int width) {
         m_dxy = 0; //m_dx = 0; m_dy = 0; 
         if (m_mode == BREAK) {
             m_size = width;
         } else if (m_mode == RULER) {
             m_size = (m_hcontext.m_font.getHeight () << 16) + width;
         } else if (m_mode == SPACER) {
             //computeBorderWidth (width);
         } else {
             m_size = width;
         }
     }
     
     int mkColor (int color, int coef) {
         int r = (((color & 0xFF0000) >> 16) * coef) >> 8;
         int g = (((color & 0xFF00) >> 8) * coef) >> 8;
         int b = ((color & 0xFF) * coef) >> 8;
         
         if (r < 0) r = 0; else if (r > 255) r = 255;
         if (g < 0) g = 0; else if (g > 255) g = 255;
         if (b < 0) b = 0; else if (b > 255) b = 255;    
         return (r<<16) + (g<<8) + b;
     }
 
     int getFullWidth () {
         if (m_mode == SPACER) {
             return 0;
         } else {
             return super.getFullWidth ();
         }
     }
 
     void draw (Graphics g, HtmlContext hc, int x, int y, int start, int end) {
         if (m_mode == BREAK) {
             ; // nothing to draw
         } else if (m_mode == SPACER) { // draw background and border
             int width = m_size & 0xFFFF;
             int height = m_size >> 16;
             int x0 = x + (m_dxy&0xFFFF);
             int y0 = y + (m_dxy>>16);// - height;
             int bw = m_data [BORDER_WIDTH];
             //Logger.println ("---------- SpecialFragment.draw border: modif:"+m_hcontext.m_modif);
             drawBackground (g, x0, y0, width, height);
             drawBorder (g,     x0-bw, y0-bw, width+bw*2, height+bw*2, HtmlContext.BORDER_SIDE);
             //Logger.println ("---------- SpecialFragment.draw done");
         } else if (m_mode == RULER) {
             // an horizontal line
             if (hc != null && hc.m_color != m_lastColor) {
                 m_lastColor = hc.m_color;
                 m_dark  = mkColor (m_lastColor, 0x80);
                 m_light = mkColor (m_lastColor, 0x180);
             }
             g.setColor (m_dark);
             x += m_dxy & 0xFFFF; // x += m_dx;
             y += (m_dxy>>16)+(m_size >> 16)/2; //y += m_dy+m_height/2;
             g.drawLine (x, y, x+(m_size&0xFFFF)-1,y); 
             g.setColor (m_light);
             g.drawLine (x, y+1, x+(m_size&0xFFFF)-1,y+1); 
         }
     }
     public String toString () { return "SpecialFragment: "+(m_mode==BREAK ? "break" : "ruler"); }
 }
 
 class TextFragment extends Fragment {
     String m_string;
     int m_start, m_end; // start and end indices of the fragement in the global string
     boolean m_ss, m_es; // starting and ending spaces
 
     TextFragment (String s, int start, int end, HtmlContext hc, boolean ss, boolean es) {
         super (hc);
         m_string = s; m_start = start; m_end = end; m_ss = ss; m_es = es;
     }
 
     boolean hasStartingSpace () { return m_ss; }
     boolean hasEndingSpace () { return m_es; }
 
     void computeBox (Context c, int width) {
         m_dxy = 0; //m_dx = 0; m_dy = 0; 
         //Logger.println ("Update context for "+this);
         //m_hcontext.updateContext (hc);
         int w = m_hcontext.m_font.substringWidth (m_string, m_start, m_end-m_start);
         int h = m_hcontext.m_font.getHeight ()+2;
         m_size = (h << 16) + w;
     }
 
     void draw (Graphics g, HtmlContext hc, int x, int y, int start, int end) {
         // draw background
         int width = m_size & 0xFFFF;
         int height = m_size >> 16;
         int x0 = x+(m_dxy&0xFFFF)+m_data[LEFT_OFFSET];
         //int y0 = y+(m_dxy>>16)+m_data[LINE_HEIGHT]-height;
         int y0 = (m_dxy>>16)+m_data[LINE_HEIGHT]-height;
         int bw = m_data [BORDER_WIDTH];
         if (y0 > end || (y0+height) < start) {
             return;
         }
         y0 += y;
         if (m_data[DISPLAY_MODE] == INLINE) {
             drawBackground (g, x0, y0, width, height);
             drawBorder     (g, x0-bw, y0-bw, width+bw*2, height+bw*2,
                             (m_data[FIRST] > 0 ? HtmlContext.BORDER_LEFT : 0) | (m_data[LAST] > 0 ? HtmlContext.BORDER_RIGHT : 0) | HtmlContext.BORDER_SIDE);
         }
         m_hcontext.updateGraphic (g, hc);
         g.drawSubstring (m_string, m_start, m_end-m_start, x0, y0+1, Graphics.TOP|Graphics.LEFT);
         //Logger.println ("Clip: drawing "+this);
     }
 
     public String toString () { return "TextFragment: "+m_string.substring (m_start, m_end); }
 }
 
 class ImageFragment extends Fragment implements ImageRequester {
     String m_src = "";
     Image m_image;
     FragmentEngine m_fe;
     boolean m_imageRequested = false;
 
     ImageFragment (HtmlContext context, FragmentEngine fe) {
         super (context);
         m_fe = fe;
     }
     
     void addFragment (Fragment f) { }
 
     void computeBox (Context c, int width) {
         m_dxy = 0; //m_dx = 0; m_dy = 0; 
         //m_hcontext.updateContext (hc);
         //image size
         int w = m_hcontext.m_imgWidth == -1 ? (m_image == null ? 16 : m_image.getWidth ()) : m_hcontext.m_imgWidth;
         int h = m_hcontext.m_imgHeight == -1 ? (m_image == null ? 16 : m_image.getHeight ()) : m_hcontext.m_imgHeight;
         m_size = (h << 16) + w;
         // try to load image
         if (!m_imageRequested && m_hcontext.m_src != null) {
             m_imageRequested = true;
             new DataLoader (m_hcontext.m_src, (ImageRequester)this, c);
         }
     }
 
     public void imageReady (Image image) {
         m_image = image;
         m_fe.setNeedRedraw ();
     }
 
     void draw (Graphics g, HtmlContext hc, int x, int y, int start, int end) {
         if (m_image != null) {
             g.drawImage (m_image, x+(m_dxy&0xFFFF), y+(m_dxy>>16)+m_data[LINE_HEIGHT]-(m_size >> 16), 0);
         }
     }
 }
 
 class Formater {
     final static int MAX_ELEMENTS = 64;
     int m_nbElements, m_maxElements;
     int m_nbSpaces, m_totalSpaceLength;
     int m_width, m_height, m_remaining;
     boolean m_flowing;
     Fragment [] m_elements;
     Fragment m_previous;
     Formater () {
         m_elements = new Fragment [m_maxElements = MAX_ELEMENTS];
         newLine (0);
     }
     void newLine (int width) {
         m_nbElements = 0;
         m_nbSpaces = m_totalSpaceLength = 0;
         m_width = m_remaining = width;
         m_height = 0;
         m_flowing = false;
         m_previous = null;
     }
 
     boolean addElement (Fragment f) {
         checkSize ();
         int space = 0;
         int fragLen = f.getFullWidth ();
         if (fragLen == 0) { // probably a spacer
             return true;
         }
         if (m_previous != null && (m_previous.hasEndingSpace () || f.hasStartingSpace ())) {
             space = f.getSpace ();
             fragLen += space;
         }
         if ((m_remaining < fragLen) && (m_nbElements > 0)) { // no more space and not the first word
             m_flowing = true;
             return false;
         }
         m_nbSpaces += space > 0 ? 1 : 0; // we can safely add this space in the gloal count of the line
         m_totalSpaceLength += space;
         m_remaining -= fragLen;
         if (f.getHeight() > m_height) {
             m_height = f.getHeight ();
         }
         m_elements[m_nbElements++] = f;
         m_previous = f;
         return true;
     }
 
     void checkSize () {
         if (m_nbElements >= m_maxElements) {
             m_maxElements += MAX_ELEMENTS;
             Fragment [] tmp = new Fragment [m_maxElements];
             System.arraycopy (m_elements, 0, tmp, 0, m_nbElements);
             m_elements = tmp;
         }
     }
 
     void checkSpacer (Fragment p, Fragment f, int x, int y, int w, int h) {
         if (p.m_hcontext == f.m_hcontext && f.m_data[Fragment.DISPLAY_MODE] == Fragment.INLINE) {
             Fragment s;
             if (p.m_next == f) { // insert a fragment spacer
                 s = new SpecialFragment (f.m_hcontext, SpecialFragment.SPACER);
                 s.m_next = f;
                 p.m_next = s;
             } else {
                 s = p.m_next;
             }
             s.setOffset (x, y, h);
             s.m_size = (h << 16) + w;
             s.m_data [Fragment.BORDER_WIDTH] = f.m_data [Fragment.BORDER_WIDTH];
         }
     }
 
     int format (int dx, int y, int way, boolean rtl) {
         int x = 0, sep;
         m_previous = null;
         if (way == HtmlContext.ALIGN_BOTH && m_flowing == true) {
             sep = m_nbSpaces == 0 ? 0 : (m_remaining+m_totalSpaceLength)/m_nbSpaces;
             for (int i = 0; i < m_nbElements; i++) {
                 Fragment f = m_elements[i];
                 if (m_previous != null && (m_previous.hasEndingSpace() || f.hasStartingSpace ())) {
                     // insert a spacer if both types == inline
                     checkSpacer (m_previous, f, dx+x, y, sep, m_height);
                     x += sep;
                 }
                 f.setOffset (dx+x, y, m_height);
                 x += f.getFullWidth ();
                 m_previous = f;
             }
         } else {
             if (way == HtmlContext.ALIGN_RIGHT) {
                 x += m_remaining;
             } else if (way == HtmlContext.ALIGN_CENTER) {
                 x += m_remaining/2;
             }
             for (int i = 0; i < m_nbElements; i++) {
                 Fragment f = m_elements[i];
                 sep = f.getSpace();
                 if (m_previous != null && (m_previous.hasEndingSpace() || f.hasStartingSpace ())) {
                     checkSpacer (m_previous, f, dx+x, y, sep, m_height);
                     x += sep;
                 }
                 int w = f.getFullWidth();
                 f.setOffset (dx + (rtl ? m_width - w - x : x), y, m_height);
                 x += w;
                 m_previous = f;
             }
         }
         return m_height;
     }
 }
 
 class BlockFragment extends Fragment {
     static final int ANCHOR_NOT_VISITED = 1;
     static final int ANCHOR_VISITED     = 2;
     BlockFragment m_link;
     Fragment m_root, m_current; 
     int m_display, m_index, m_innerWidth, m_innerHeight;
     //int m_mt, m_mb, m_ml, m_mr; // margin top, bottom, left and right
     //int m_pt, m_pb, m_pl, m_pr; // padding top, bottom, left and right
     int m_x0, m_y0, m_w0, m_h0;
     String m_class;
     Formater m_formater;
     int m_anchorState = 0; // for anchor: ANCHOR_NOT_VISITED or ANCHOR_VISITED
     
 
     BlockFragment (HtmlContext context, int width, boolean solid) {
         super (context);
         m_size = width; //m_width = width; height = 0
         m_index = 0;
         m_display = solid ? BLOCK : INLINE;
         m_formater = new Formater ();
     }
 
     BlockFragment (HtmlContext context, int width, boolean solid, int index) {
         this (context, width, solid);
         m_index = index;
     }
 
     Fragment getFirst () {
         if (m_display == BLOCK) { 
             return this;
         } // else display == inline
         if (m_root == null) { // empty tag
             return null;
         }
         pushFragment (this);
         return m_root.getFirst ();
     }
 
     void addFragment (Fragment f) {
         f.m_next = null;
         if (m_root == null) {
             m_root = f;
             f.m_data[FIRST] = f.m_data[LAST] = 1;
         } else {
             m_current.m_data[LAST] = 0;
             m_current.m_next = f;
             f.m_data[LAST] = 1;
         }
         m_current = f;
     }
 
     void cumulOffset (int dx, int dy) { 
         //Logger.println ("BlockFragment.cumulOffset: "+dx+", "+dy+" for "+this+" / "+m_dx+", "+m_dy);
         if (m_display == INLINE) {
             m_dxy = dx + (dy<<16); //m_dx = dx; m_dy = dy;
         } else {
             m_dxy += dx + (dy <<16); //m_dx += dx; m_dy += dy;
         }
         Fragment f = m_root;
         dx = m_dxy & 0xFFFF;
         dy = m_dxy >> 16;
         while (f != null) {
             f.cumulOffset (dx, dy); //f.cumulOffset (m_dx, m_dy);
             f = f.m_next;
         }
     }
 
     void updateFromStyles (PropertiesManager propMgr) {
         //Logger.println ("updateFromStyles for "+FragmentEngine.s_tagNames[m_index]);
         propMgr.parseProperty (FragmentEngine.s_tagNames[m_index], m_class, this);
         //boolean inline = m_display == INLINE;
         Fragment f = m_root;
         while (f != null) {
             f.m_data[DISPLAY_MODE] = (byte)m_display;
             f.updateFromStyles (propMgr);
             f = f.m_next;
         }
     }
 
     void updateContext (HtmlContext hc) {
         m_hcontext.updateContext (hc);
         Fragment f = m_root;
         while (f != null) {
             f.updateContext (m_hcontext);
             f = f.m_next;
         }
     }
 
     void draw (Graphics g, HtmlContext hc, int x, int y, int start, int end) {
         // draw background
         int y0 = m_dxy >> 16;
         // int y1 = y0+(m_size >> 16);
         if (m_display == BLOCK && (y0 > end || (y0+(m_size >> 16)) < start)) {
             return;
         }
         //Logger.println ("Clip: from "+start+" to "+end+" against block of "+
         //                y0+" to "+(y0+(m_size >> 16))+" / "+this);
         // int bw = m_data[BORDER_WIDTH];
         int x0 = x+(m_dxy&0xFFFF)+m_x0;
         y0 += y + m_y0;
         drawBackground (g, x0, y0, m_w0, m_h0);
         drawBorder (g,     x0, y0, m_w0, m_h0, HtmlContext.BORDER_ALL);
         Fragment f = m_root;
         while (f != null) {
             f.draw (g, hc, x, y, start, end);
             f = f.m_next;
         }
     }
 
     void computeBox (Context c, int width) {
         m_dxy = 0; //m_dx = 0; m_dy = 0; 
         int w = m_hcontext.getWidth (width);
         int ml = m_hcontext.getMargin (CSSProp.MARGIN_LEFT, width);
         int mr = m_hcontext.getMargin (CSSProp.MARGIN_RIGHT, width);
         int mt = m_hcontext.getMargin (CSSProp.MARGIN_TOP, width);
         int mb = m_hcontext.getMargin (CSSProp.MARGIN_BOTTOM, width);
         int pl = m_hcontext.getMargin (CSSProp.PADDING_LEFT, width);
         int pr = m_hcontext.getMargin (CSSProp.PADDING_RIGHT, width);
         int pt = m_hcontext.getMargin (CSSProp.PADDING_TOP, width);
         int pb = m_hcontext.getMargin (CSSProp.PADDING_BOTTOM, width);
         int bw = computeBorderWidth (width);
         
         m_innerWidth = w - (ml+mr+pl+pr+bw*2);
 
         // int lastMargin = mt;
         // Logger.println ("lastMargin: "+lastMargin);
 
         Fragment f = m_root == null ? null : m_root.getFirst ();
         if (f == null) { return; }
         int h = mt+pt+bw;
         if (f != null) {
             f.m_dxy = 0; //f.m_dx = f.m_dy = 0;
             f.computeBox (c, m_innerWidth);
         }
         while (f != null) { // fit a line
             m_formater.newLine (m_innerWidth);
             while (f != null) { 
                 if (!m_formater.addElement (f)) {
                     break;
                 }
                 f = f.getNext ();
                 if (f != null) {
                     f.m_dxy = 0; 
                     f.computeBox (c, m_innerWidth);
                 }
             }
             // here we should justify the fragments from line to f (excluding)
             h += m_formater.format (ml+pl+bw, h, m_hcontext.m_align, m_hcontext.m_rtl);
         }
         h += mb + pb + bw;
         m_innerHeight = h - (mt + mb + pt + pb + bw*2);
         m_size = (h<<16)+w;
 
         m_x0 = ml;
         m_y0 = mt;
         m_w0 = m_innerWidth + pr + pl + bw*2;
         m_h0 = m_innerHeight + pt + pb + bw*2;
         
     }
 
     boolean isSelectedAt (int x, int y) {
         int y0 = m_dxy >> 16;
         if (m_display == HIDDEN ||
            (m_display == BLOCK && (y0 > y || (y0+(m_size >> 16)) < y))) {
             return false;
         }
         Fragment f = m_root == null ? null : m_root.getFirst ();
         while (f != null) {
             if (f.isSelectedAt (x, y)) {
                 return true;
             }
             f = f.getNext();
         }
         return false;
     }
     
     boolean isVisible (int start, int end) {
         Fragment f = m_root == null ? null : m_root.getFirst ();
         while (f != null) {
             if (f.isVisible (start, end)) {
                 return true;
             }
             f = f.getNext();
         }
         return false;
     }
     
     public String toString () { return "BlockFragment: "+FragmentEngine.s_tagNames[m_index]+" / "+(m_display == 1 ? "INLINE" : "BLOCK")+"["+(m_dxy&0xFFFF)+", "+(m_dxy>>16)+" x "+(m_size & 0xFFFF)+", "+(m_size>>16)+"] : "+super.toString (); }
 
     void print (String decal) {
         Logger.println (decal+this);
         if (m_root != null) {
             m_root.print (decal+"    ");
         }
         if (m_next != null) {
             m_next.print (decal);
         }
     }
 
 }
 
 class FragmentEngine implements XmlVisitor, TextRequester {
     final static int NONE      = 0;
     final static int HTML      = 1;
     final static int HEAD      = 2;
     final static int TITLE     = 3;
     final static int BODY      = 4;
     final static int FONT      = 5;
     final static int IMG       = 6;
     final static int A         = 7;
     final static int DIV       = 8;
     final static int STYLE     = 9;
     final static int BR        = 10;
     final static int HR        = 11;
     final static int LINK      = 12;
     final static int P         = 13;
     final static int SPAN      = 14;
     final static int B         = 15;
     final static int U         = 16;
     final static int I         = 17;
     final static int H1        = 18;
     final static int H2        = 19;
     final static int H3        = 20;
     final static int H4        = 21;
     final static int H5        = 22;
     final static int H6        = 23;
     final static int CENTER    = 24;
 
     final static String s_tagNames [] = {
         "none", "html", "head", "title", "body",
         "font", "img", "a", "div", "style", 
         "br", "hr", "link", "p", "span",
         "b", "u", "i", "h1", "h2",
         "h3", "h4", "h5", "h6", "center"
     };
     final static int s_tagIds [] = {
         NONE, HTML, HEAD, TITLE, BODY,
         FONT, IMG, A, DIV, STYLE, 
         BR, HR, LINK, P, SPAN,
         B, U, I, H1, H2,
         H3, H4, H5, H6, CENTER
     };
     int [] m_modeStack;
     int m_modeCount;
     int m_width = 0;
     HtmlContext m_hcontext = null;
     BlockFragment m_block = null;
     boolean m_needRedraw = true;
     Context m_context;
     PropertiesManager m_propMgr;
 
     // the anchors
     int m_nbAnchors = 0;
     BlockFragment [] m_anchors = null;
     int m_currentAnchor = -1;
     boolean m_isAnchorFocused = false;
     int m_lastFocusAttempt = -13;
 
 
     FragmentEngine (int width, Context c) { 
         m_hcontext = new HtmlContext ();
         m_block = new BlockFragment (m_hcontext, m_width = width, false, NONE);
         
         m_modeStack = new int [256];
         m_modeCount = 0;
         pushMode (NONE);
         m_context = c;
         m_propMgr = new PropertiesManager ();
         // sort tag names and ids
         boolean again = true;
         int max = s_tagNames.length-1;
         while (again) {
             again = false;
             for (int i = 0; i < max; i++) {
                 if (s_tagNames[i].compareTo (s_tagNames[i+1]) > 0) {
                     String tmp = s_tagNames[i+1];
                     s_tagNames[i+1] = s_tagNames[i];
                     s_tagNames[i] = tmp;
                     int k = s_tagIds[i+1];
                     s_tagIds[i+1] = s_tagIds[i];
                     s_tagIds[i] = k;
                     again = true;
                 }
             }
         }
         m_anchors = new BlockFragment [16];
     }
 
     int findIndex (String name) {
         int left = 0;
         int right = s_tagNames.length-1;
         int pivot, way;
         while (left <= right) {
             pivot = left + (right - left) / 2;
             way = name.toLowerCase().compareTo(s_tagNames[pivot]);
             if (way == 0) {
                 return pivot;
             } else if (way < 0) {
                 right = pivot-1;
             } else { //way > 0  
                 left = pivot+1;
             }
         }
         return -1;
     }
 
     synchronized void setNeedRedraw () {
         m_needRedraw = true;
     }
 
     synchronized boolean isNeedRedraw () {
         return m_needRedraw;
 //         if (m_needRedraw) {
 //             m_needRedraw = false;
 //             return true;
 //         }
 //         return false;
     }
 
     void pushContext () { 
         m_hcontext = m_hcontext.dup (); 
     }
 
     void popContext () { 
         m_hcontext = m_hcontext.m_next;
     }
 
     void pushBlock (BlockFragment b) { 
         m_block.addFragment (b);
         b.m_link = m_block; 
         m_block = b; 
     }
 
     void popBlock () {
         m_block = m_block.m_link; 
     }
 
     void popBlock (String name) {
         if (name.equalsIgnoreCase (s_tagNames [m_block == null ? 0 : m_block.m_index])) {
             //Logger.println ("popBlock for "+name+", removing current block");
             m_block = m_block.m_link; 
             //} else {
             //Logger.println ("popBlock for "+name+", different from stacked "+s_tagNames [m_block == null ? 0 : m_block.m_index]);
         }
     }
 
     void pushMode (int mode) {
         m_modeStack[m_modeCount++] = mode;
     }
 
     int getMode () {
         return m_modeStack[m_modeCount-1];
     }
     int popMode () {
         return m_modeStack[--m_modeCount];
     }
 
     void addAnchor (BlockFragment b) {
         m_nbAnchors++;
         if (m_nbAnchors >= m_anchors.length) {
             BlockFragment [] tmp = new BlockFragment [m_nbAnchors + 16];
             System.arraycopy (m_anchors, 0, tmp, 0, m_nbAnchors);
             m_anchors = tmp;
         }
         m_anchors[m_nbAnchors-1] = b;
         Logger.println ("adding anchor "+b+"at index "+(m_nbAnchors-1)+" with context "+m_block.m_hcontext);
     }
 
     // Select (mark active) anchor based on (x,y) coordinates
     BlockFragment selectAnchorAt (int x, int y) {
         unfocusAnchor();
         for (int i=0; i<m_nbAnchors; i++) {
             BlockFragment bf = m_anchors[i];
             if (bf.isSelectedAt (x,y)) {
                 m_currentAnchor = i;
                 m_isAnchorFocused = true;
                 activateAnchor();
                 return bf;
             }
         }
         return null;
     }
     
     // Each time the start field changes,
     // check if current anchor is still visible.
     void checkAnchorOnNewStart (int start, int end) {
         if (m_currentAnchor != -1 && !m_anchors[m_currentAnchor].isVisible (start, end)) {
             if (m_isAnchorFocused) {
                 unfocusAnchor ();
             }
             m_currentAnchor = -1;
             m_lastFocusAttempt = -13;
         }
     }
     
     // Focus (mark hover) next anchor on the screen.
     BlockFragment focusNextAnchor (int step, int start, int end) {
         BlockFragment bf = null;
         if (m_currentAnchor == -1) { // search first visible link
             unfocusAnchor ();
             for (int i=0; i<m_nbAnchors; i++) {
                bf = focusAnchor (i, start, end);
                if (bf != null) break; // anchor found !
             }
         } else { // search incrementaly
             int newAnchor = m_currentAnchor;
             if (m_isAnchorFocused || m_lastFocusAttempt == m_currentAnchor + step) {
                 newAnchor += step;
             }
             if (newAnchor >= 0 && newAnchor < m_nbAnchors) {
                 unfocusAnchor ();
                 m_lastFocusAttempt = newAnchor;
                 bf = focusAnchor (newAnchor, start, end);
             }
         }
         return bf;
     }
     
     // Focus anchor if visible
     BlockFragment focusAnchor (int newAnchor, int start, int end) {
         BlockFragment bf = m_anchors[newAnchor];
         if (bf.isVisible (start, end)) {
             m_isAnchorFocused = true;
             m_currentAnchor = newAnchor;
             bf.m_hcontext.m_pseudoClassmode = HtmlContext.PSEUDO_CLASS_HOVER;
             setNeedRedraw ();
             return bf;
         }
         return null;
     }
     
     // Mark an active anchor as visited or an hover anchor as regular link.
     void unfocusAnchor () {
         if (m_isAnchorFocused) {
             BlockFragment bf = m_anchors[m_currentAnchor];
             if (bf.m_anchorState == BlockFragment.ANCHOR_VISITED) {
                 bf.m_hcontext.m_pseudoClassmode = HtmlContext.PSEUDO_CLASS_VISITED;
             } else {
                 bf.m_hcontext.m_pseudoClassmode = HtmlContext.PSEUDO_CLASS_LINK;
             }
             setNeedRedraw ();
             m_isAnchorFocused = false;
         }
     }
     
     // Mark focused anchor as visited
     BlockFragment activateAnchor () {
         BlockFragment bf = null;
         if (m_isAnchorFocused) {
             bf = m_anchors[m_currentAnchor];
             bf.m_anchorState = BlockFragment.ANCHOR_VISITED;
             bf.m_hcontext.m_pseudoClassmode = HtmlContext.PSEUDO_CLASS_ACTIVE;
             setNeedRedraw ();
         }
         return bf;
     }
     
     boolean isWhite (char c) { return c == ' ' || c == '\n' || c == '\t' || c == '\r'; }
 
     public void setLeave (String l, boolean startingSpace, boolean trailingSpace) {
         if (getMode() == TITLE) {
             //Logger.println ("Title: '"+l+"'");
         } else if (getMode() == STYLE) {
             //Logger.println ("style: '"+l+"'");
             m_propMgr.parseInline (l);
         } else {
             //Logger.println ("setleave: '"+l+"'");
             boolean ss, es = false;
             // create new fragments for each word with current context
             int len = l.length ();
             for (int i = 0; i < len; i++) {
                 ss = es;
                 // skip spaces
                 while (i < len && isWhite (l.charAt (i))) { i++; ss = true; }
                 int start = i;
                 while (i < len && !isWhite (l.charAt (i))) { i++; }
                 es = i < len && isWhite (l.charAt (i));
                 if (start < len) {
                     m_block.addFragment (new TextFragment (l, start, i, m_hcontext, 
                                                            start == 0 ? startingSpace : ss, 
                                                            i >= len ? trailingSpace : es
                                              ));
                 }
             }
         }
     }
 
     // callback from XML visitor: called when a new tag is found
     public void open (String t) {
         // push a new context
         pushContext ();
         int index = findIndex (t);
         if (index < 0) {
             //Logger.println ("findIndex for "+t+" cannot be found ");
             pushMode (NONE);
             return;
         }
         //Logger.println ("findIndex for "+t+" is "+index+" / "+s_tagNames[index]);
         int id = s_tagIds[index];
         pushMode (id);
         switch (id) {
         case BODY:
             pushBlock (new BlockFragment (m_hcontext, m_width, true, index));
 //            m_hcontext.addDefaultProp (CSSProp.BG_COLOR, "white");
 //             m_hcontext.addDefaultProp (CSSProp.MARGIN_TOP, "8px");
 //             m_hcontext.addDefaultProp (CSSProp.MARGIN_BOTTOM, "8px");
 //             m_hcontext.addDefaultProp (CSSProp.MARGIN_LEFT, "8px");
 //             m_hcontext.addDefaultProp (CSSProp.MARGIN_RIGHT, "8px");
             break;
         case B:
             pushBlock (new BlockFragment (m_hcontext, m_width, false, index));
             m_hcontext.addDefaultProp (CSSProp.FONT_WEIGHT, "bold");
             break;
         case U:
             pushBlock (new BlockFragment (m_hcontext, m_width, false, index));
             m_hcontext.addDefaultProp (CSSProp.TEXT_DECORATION, "underline");
             break;
         case H1:
             pushBlock (new BlockFragment (m_hcontext, m_width, true, index));
             break;
         case H2:
             pushBlock (new BlockFragment (m_hcontext, m_width, true, index));
             break;
         case H3:
             pushBlock (new BlockFragment (m_hcontext, m_width, true, index));
             break;
         case H4:
             pushBlock (new BlockFragment (m_hcontext, m_width, true, index));
             break;
         case H5:
             pushBlock (new BlockFragment (m_hcontext, m_width, true, index));
             break;
         case H6:
             pushBlock (new BlockFragment (m_hcontext, m_width, true, index));
             break;
         case I:
             pushBlock (new BlockFragment (m_hcontext, m_width, false, index));
             m_hcontext.addDefaultProp (CSSProp.FONT_STYLE, "italic");
             break;
         case TITLE:
             break;
         case FONT:
             pushBlock (new BlockFragment (m_hcontext, m_width, false, index));
             break;
         case IMG:
             m_block.addFragment (new ImageFragment (m_hcontext, this)); //pushBlock (new index ());
             break;
         case A:
             pushBlock (new BlockFragment (m_hcontext, m_width, false, index));
             m_block.m_anchorState = BlockFragment.ANCHOR_NOT_VISITED;
             m_block.m_hcontext.m_pseudoClassmode = HtmlContext.PSEUDO_CLASS_LINK;
             addAnchor (m_block);
             break;
         case STYLE:
             break;
         case LINK:
             break;
         case P:
             pushBlock (new BlockFragment (m_hcontext, m_width, true, index));
             m_hcontext.addDefaultProp (CSSProp.MARGIN_TOP, "1em");
             m_hcontext.addDefaultProp (CSSProp.MARGIN_BOTTOM, "1em");
             m_hcontext.addDefaultProp (CSSProp.MARGIN_LEFT, "8px");
             m_hcontext.addDefaultProp (CSSProp.MARGIN_RIGHT, "8px");
             break;
         case SPAN:
             pushBlock (new BlockFragment (m_hcontext, m_width, false, index));
             break;
         case DIV:
             pushBlock (new BlockFragment (m_hcontext, m_width, true, index));
             break;
         case CENTER:
             pushBlock (new BlockFragment (m_hcontext, m_width, true, index));
             m_hcontext.addDefaultProp (CSSProp.TEXT_ALIGN, "center");
             break;
         case BR:
             m_block.addFragment (new SpecialFragment (m_hcontext, SpecialFragment.BREAK));
             break;
         case HR:
             m_block.addFragment (new SpecialFragment (m_hcontext, SpecialFragment.RULER));
             break;
         }
     }
 
     // callback from XML visitor: called for each attribute of a tag (open has been called previously)
 
     public void addAttribute (String name, String value) {
         // standard attributes
         if (name.equalsIgnoreCase ("class")) {
             m_block.m_class = value;
         } else if (name.equalsIgnoreCase ("style")) {
             m_block.m_hcontext.m_style = m_propMgr.parseInternal (value);
             //if (m_block.m_hcontext.m_style != null) { m_block.m_hcontext.m_style.print (); }
         } else if (getMode () == FONT) {
             if (name.equalsIgnoreCase ("color")) {
                 try {
                     m_hcontext.m_color = Integer.parseInt (value.substring (1), 16);
                     m_hcontext.m_inherit |= HtmlContext.INHERIT_COLOR;
                     ///Logger.println ("adding a color "+Integer.toHexString(m_hcontext.m_color)+" to context "+m_hcontext);
                 } catch (Exception e) {
                     m_hcontext.m_color = 0xFF0000;
                 }
             }
             if (name.equalsIgnoreCase ("size")) {
                 try {
                     m_hcontext.m_size = Integer.parseInt (value);
                 } catch (Exception e) {
                     m_hcontext.m_size = 12;
                 }
             }
         } else if (getMode () == IMG) {
             if (name.equalsIgnoreCase ("src")) {
                 m_hcontext.m_src = value;
             } else if (name.equalsIgnoreCase ("width")) {
                 try {
                     m_hcontext.m_imgWidth = Integer.parseInt (value);
                 } catch (Exception e) {
                     m_hcontext.m_imgWidth = 16;
                 }
             } else if (name.equalsIgnoreCase ("height")) {
                 try {
                     m_hcontext.m_imgHeight = Integer.parseInt (value);
                 } catch (Exception e) {
                     m_hcontext.m_imgHeight = 16;
                 }
             }
         } else if (getMode () == A) {
             if (name.equalsIgnoreCase ("href")) {
                 m_hcontext.m_href = value;
             }
         } else if (getMode () == LINK) {
             if (name.equalsIgnoreCase ("href")) {
               m_hcontext.m_href = value;
             }
         } else if (getMode () == P) {
             if (name.equalsIgnoreCase ("align")) {
                 if (value != null && value.length () > 0) {
                     m_hcontext.setTextAlign (value);
                 }
             } else if (name.equalsIgnoreCase("dir")) {
                 m_hcontext.setTextDir ("rtl".equalsIgnoreCase (value));
             }
         }
     }
     // callback from XML visitor: called when teh list of attributes is reached
     public void endOfAttributes (boolean selfClosing) {
         if (getMode () == LINK) {
             String href = m_hcontext.m_href;
             new DataLoader (href, this, m_context);
         }
         // if selfClosing, pop context
         if (selfClosing) {
             popContext ();
             popMode ();
         }
     }
 
     // callback from XML visitor: called when the tag is closed (the </xx> form)
     public void close (String t) {
         popBlock (t); // pop block only if popped name is equal to t
         popContext ();
         popMode ();
     }
 
     // callback
     public void textReady (String s) {
         m_propMgr.parseExternal (s);
         updateFromStyles ();
         setNeedRedraw ();
     }
 
     void setBrowserStyle (String style) {
         m_propMgr.parseBrowser (style);
         setNeedRedraw ();
     }
 
     void updateFromStyles () {
         //Logger.println ("----{ updateFromStyles }----");
         m_block.updateFromStyles (m_propMgr);
         //Logger.println ("----------------------------");
     }
 
     void draw (Graphics g, int x, int y, int start, int end) {
         m_block.draw (g, null, x, y-start, start, end);
     }
 
     void print () {
         m_block.print ("");
     }
 
     int computeBox (Context c, int width) {
         m_width = width;
         m_needRedraw = false;
         if (m_block != null) {
             m_block.updateContext (m_hcontext);
             m_block.computeBox (c, m_width);
             m_block.cumulOffset (0, 0);
             return (m_block.m_size >> 16); 
         } 
         return 0; 
     }
 }
 
 public class RichText extends Node {
     final static int STRING_CHANGED     = 1<<0;
     final static int SIZE_CHANGED       = 1<<1;
     final static int START_CHANGED      = 1<<3;
     final static int STYLE_CHANGED      = 1<<4;
     final static int JUMP_CHANGED       = 1<<5;
     final static int POSITION_CHANGED   = 1<<6;
     final static int SELECTTIME_CHANGED = 1<<7;
     
     int m_fieldChanged;
     String m_style;
     int m_width, m_height, m_htmlHeight;
     int m_start; // the vertical starting position within the text (shoudl be between 0 and (html_height - m_height)
     Region m_backup;
     Region [] m_box;
     int m_prevFgColor = -1;
     Image m_img; // RC 13/10/07 the offscreen image
     ImageContext m_imgCtx; // RC 13/10/07: used for text scalling/rotation (draw in an offscreen buffer)
     boolean m_offscreen; // RC 13/10/07 when resized or rotated
     int m_rotation; // RC 13/10/07 the rotation quadrant  
     Region m_textBox = new Region (); //MCP: original text box is only refreshed on computeDims
     
     FragmentEngine m_fragmentEngine;
     
 
     RichText () {
         super (10);
         m_field[0] = new MFString (this); // string
         m_field[1] = new SFVec2f (128<<16, 128<<16, this); // size
         m_field[2] = new SFFloat (0); // height
         m_field[3] = new SFFloat (0, this); // start
         m_field[4] = new SFString ("", this); // style
         m_field[5] = new SFFloat (0, this); // jump
         m_field[6] = new SFVec2f (0, 0, this); // position
         m_field[7] = new SFTime (0, this); // selectTime
         m_field[8] = new SFString (""); // linkSelected
         m_field[9] = new SFFloat (0); // start_changed
         m_backup = new Region ();
     }
 
     void start (Context c) {
         fieldChanged (m_field[0]);
         fieldChanged (m_field[1]);
         fieldChanged (m_field[4]);
     }
 
     void draw (Graphics g, int fgColor, int x, int y) {
         if (m_fragmentEngine != null) {
             m_fragmentEngine.draw (g, x, y, m_start, m_start+m_height);
         }
     }
 
     void render (Context c) {//RCA
         if (m_offscreen && m_imgCtx != null) {
             m_imgCtx.drawImage (c.gc, m_region.x0, m_region.y0, m_region.getWidth(), m_region.getHeight(), m_ac.m_transparency, m_rotation);
         } else {
             m_backup.x0 = c.gc.getClipX ();
             m_backup.y0 = c.gc.getClipY ();
             m_backup.x1 = c.gc.getClipWidth ();
             m_backup.y1 = c.gc.getClipHeight ();
             c.gc.clipRect (m_region.x0, m_region.y0, m_region.x1-m_region.x0-1,m_region.y1-m_region.y0-1);
             draw (c.gc, m_ac.m_color, m_region.x0-m_textBox.x0, m_region.y0-m_textBox.y0);
             c.gc.setClip (m_backup.x0, m_backup.y0, m_backup.x1, m_backup.y1);
         }
     }
 
     void computeRegion (Context c) {
         int w = m_width;
         int h = m_height;
         m_region.setFloat (-w/2, h/2, w/2+(w+1)%2, -h/2-(h+1)%2);
         c.matrix.transform (m_region);
         m_region.toInt ();
         m_region.getRotationAndNormalize ();
     }
 
     final boolean compose (Context c, Region clip, boolean forceUpdate) {
         boolean updated = (m_fieldChanged != 0) || forceUpdate;
         m_ac = c.ac;
         //computeRegion (c);
         if ((m_fieldChanged & STRING_CHANGED) != 0) { // string
             m_fieldChanged &= ~STRING_CHANGED;
             m_fragmentEngine = null;
             int len = ((MFString)m_field[0]).m_size;
             if (len > 0) {
                 String[] string = ((MFString)m_field[0]).getValues();
                 // concatenate strings
                 StringBuffer sb = new StringBuffer(string[0]);
                 for (int i = 1; i < len; i++) {
                     sb.append(string [i]);
                 }
                 if (sb.length() > 0) {
                     XmlReader r = new XmlReader (sb.toString(), XmlReader.BUFFER+XmlReader.HTML);
                     m_fragmentEngine = new FragmentEngine (m_width, c);
                     r.visit ((XmlVisitor)m_fragmentEngine);
                     r.close ();
                     r = null;
                     m_fieldChanged |= STYLE_CHANGED; // force style update
                     //m_fragmentEngine.print ();
                 }
             }
         }
         if (m_fragmentEngine != null) {
             if ((m_fieldChanged & STYLE_CHANGED) != 0) { // style
                 m_fieldChanged &= ~STYLE_CHANGED;
                 m_fragmentEngine.setBrowserStyle (m_style);
                 m_fragmentEngine.updateFromStyles ();
             }
             if ((m_fieldChanged & SIZE_CHANGED) != 0) { // size
                 m_fieldChanged &= ~SIZE_CHANGED;
                 // Correct start if size changes and start goes out of bound
                 int limit = m_htmlHeight - m_height;
                 if (limit > 0 && m_start > limit) {
                     m_start = limit;
                     m_fieldChanged |= START_CHANGED; // notify start
                 }
                 m_fragmentEngine.setNeedRedraw ();
             }
             if ((m_fieldChanged & START_CHANGED) != 0) { // start
                 m_fieldChanged &= ~START_CHANGED;
                 ((SFFloat)m_field[9]).setValue (FixFloat.int2fix(m_start)); // start_changed
                 // Check current anchor, each time start field changes.
                 m_fragmentEngine.checkAnchorOnNewStart (m_start, m_start+m_height);
             }
             if ((m_fieldChanged & POSITION_CHANGED) != 0) { // position
                 m_fieldChanged &= ~POSITION_CHANGED;
                 // TODO: Use Point and Matrix tranform to get orig coord ?
                 int x = m_width/2 + FixFloat.fix2int (((SFVec2f)m_field[6]).m_x);
                 int y = m_start + m_height / 2 - FixFloat.fix2int (((SFVec2f)m_field[6]).m_y);
                 Logger.println("Check position at "+x+"x"+y);
                 BlockFragment bf = m_fragmentEngine.selectAnchorAt (x, y);
                 if (bf != null) { // found a link !
                     ((SFString)m_field[8]).setValue (bf.m_hcontext.m_href);
                 }
             } 
             if ((m_fieldChanged & JUMP_CHANGED) != 0) { // jump
                 m_fieldChanged &= ~JUMP_CHANGED;
                 int i = FixFloat.fix2int (((SFFloat)m_field[5]).getValue ());
                 BlockFragment bf = m_fragmentEngine.focusNextAnchor (i>=0?1:-1, m_start, m_start+m_height);
                 if (bf == null) { // no links or link out of box => just scroll
                     int oldStart = m_start;
                     m_start += i;
                     // Limit jumps
                     if (m_start < 0) {
                         m_start = 0;
                     } else if (m_start > m_htmlHeight - m_height) {
                         m_start = m_htmlHeight - m_height;
                     }
                     if (m_start != oldStart) {
                         ((SFFloat)m_field[9]).setValue (FixFloat.int2fix(m_start)); // start_changed
                     }
                 }
             }
             if ((m_fieldChanged & SELECTTIME_CHANGED) != 0) { // selectTime
                 m_fieldChanged &= ~SELECTTIME_CHANGED;
                 BlockFragment bf = m_fragmentEngine.activateAnchor();
                 if (bf != null) { // an anchor was activated
                     ((SFString)m_field[8]).setValue (bf.m_hcontext.m_href);
                 }
             }
             if (m_fragmentEngine.isNeedRedraw ()) {
                 m_htmlHeight = m_fragmentEngine.computeBox (c, m_width);
                 ((SFFloat)m_field[2]).setValue (FixFloat.int2fix (m_htmlHeight));
                 updated = true;
             }
             if (updated) {
                 m_region.setFloat (-m_width/2, m_height/2, m_width/2, -m_height/2);
                 c.matrix.transform (m_region);
                 m_region.toInt ();
                 //m_region.getRotationAndNormalize ();
                 c.ac.addClip (clip, m_region);
             }
             c.addRenderNode (this);
         }
         /*if (false && m_string != null) {
             m_ac = c.ac;
 
             if (updated) {
                 m_img = null; // MCP: force refresh of the offscreen buffer 
             }
             
             int w = m_textBox.getWidth();
             int h = m_textBox.getHeight();
             
             m_region.set(m_textBox);
             m_region.y0 *= -1; // invert the y coords to match VRML way
             m_region.y1 *= -1;
             m_region.toFloat();
             c.matrix.transform(m_region);
             m_region.toInt();
             m_rotation = m_region.getRotationAndNormalize ();
             
             if (!isVisible(clip, c.bounds)) {
                 return updated;
             }
             
             m_offscreen = false; //m_ac.m_transparency > 0 || m_region.getWidth() != w || m_region.getHeight() != h || m_rotation != 0;
             if (m_offscreen && h > 0 && w > 0) { // render in an offscreen image and using an ImageContext
                 boolean updateCtx = false;
                 if (m_img == null) {
                     m_img = Image.createImage (w, h);
                     updateCtx = true;
                 }
                 if (m_imgCtx == null) {
                     m_imgCtx = new ImageContext ();
                     updateCtx = true;
                 }
                 if (updateCtx) {
                     m_imgCtx.setImage (m_img, false);
                 }
                 int fgColor = m_ac.m_color;
                 if (updateCtx || fgColor != m_prevFgColor) {
                     int bgColor = 0xFF000000 + ((fgColor & 0xFF) > 128 ? 0 : 255);
                     m_prevFgColor = fgColor;
                     Graphics g = m_img.getGraphics();
                     g.setColor (bgColor);
                     g.fillRect (0, 0, w, h);
                     draw (g, fgColor, -m_textBox.x0, -m_textBox.y0);
                     m_imgCtx.makeTransparency (bgColor, fgColor);
                 }
             }
 
             updated |= m_ac.isUpdated (m_region) | forceUpdate;
             if (updated) {
                 m_ac.addClip (clip, m_region);
             }
             c.addRenderNode (this);
             m_isUpdated = false;
         }*/
         return updated;
     }
 
     public void fieldChanged (Field f) {
         if (f == m_field[0]) { // string
             m_fieldChanged |= STRING_CHANGED;
         } else if (f == m_field [1]) { // size
             m_width  = FixFloat.fix2int (((SFVec2f)m_field[1]).m_x);
             m_height = FixFloat.fix2int (((SFVec2f)m_field[1]).m_y);
             m_fieldChanged |= SIZE_CHANGED;
         } else if (f == m_field [3]) { // start
             m_start = FixFloat.fix2int ( ((SFFloat)m_field[3]).m_f);
             m_fieldChanged |=START_CHANGED;
         } else if (f == m_field [4]) { // style
             m_style = ((SFString)m_field[4]).getValue();
             m_fieldChanged |= STYLE_CHANGED;
         } else if (f == m_field [5]) { // jump
             m_fieldChanged |= JUMP_CHANGED;
         } else if (f == m_field [6]) { // position
             m_fieldChanged |= POSITION_CHANGED;
         } else if (f == m_field [7]) { // selectTime
             m_fieldChanged |= SELECTTIME_CHANGED;
         }
     }
 
 }
