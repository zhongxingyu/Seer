 package org.joshy.html.css;
 
 import com.steadystate.css.*;
 import com.steadystate.css.parser.*;
 import org.w3c.dom.*;
 import org.w3c.dom.css.*;
 import org.w3c.css.sac.*;
 import java.util.*;
 import org.joshy.u;
 
 public class RuleNormalizer {
     private static final Map COLOR_MAP;
     private static final Map FONT_SIZES;
     private static final Map FONT_WEIGHTS;
     private static final Map BORDER_WIDTHS;
     private static final Map BACKGROUND_POSITIONS;
     private static final List BACKGROUND_REPEATS;
     private static final List BORDER_STYLES;
     private static final List LIST_TYPES;
     private static final List FONT_STYLES;
 
     static {
         COLOR_MAP = new HashMap();
         COLOR_MAP.put("black","#000000");
         COLOR_MAP.put("white","#FFFFFF");
         COLOR_MAP.put("red","#FF0000");
         COLOR_MAP.put("yellow","#FFFF00");
         COLOR_MAP.put("lime","#00ff00");
         COLOR_MAP.put("aqua","#00ffff");
         COLOR_MAP.put("blue","#0000ff");
         COLOR_MAP.put("fuchsia","#ff00ff");
         COLOR_MAP.put("gray","#808080");
         COLOR_MAP.put("silver","#c0c0c0");
         COLOR_MAP.put("maroon","#800000");
         COLOR_MAP.put("olive","#808000");
         COLOR_MAP.put("green","#008000");
         COLOR_MAP.put("teal","#008080");
         COLOR_MAP.put("navy","#000080");
         COLOR_MAP.put("purple","#800080");
         COLOR_MAP.put("transparent","transparent");
     
         FONT_SIZES = new HashMap();
         FONT_SIZES.put("xx-small", "6.9pt");
         FONT_SIZES.put("x-small",  "8.3pt");
         FONT_SIZES.put("small",    "10pt");
         FONT_SIZES.put("medium",   "12pt");
         FONT_SIZES.put("large",    "14.4pt");
         FONT_SIZES.put("x-large",  "17.3pt");
         FONT_SIZES.put("xx-large", "20.7pt");
 
         FONT_WEIGHTS = new HashMap();
         FONT_WEIGHTS.put("normal", "400");
         FONT_WEIGHTS.put("bold",   "700");
         FONT_WEIGHTS.put("100",   "100");
         FONT_WEIGHTS.put("200",   "200");
         FONT_WEIGHTS.put("300",   "300");
         FONT_WEIGHTS.put("400",   "400");
         FONT_WEIGHTS.put("500",   "500");
         FONT_WEIGHTS.put("600",   "600");
         FONT_WEIGHTS.put("700",   "700");
         FONT_WEIGHTS.put("800",   "800");
         FONT_WEIGHTS.put("900",   "900");
         FONT_WEIGHTS.put("bolder",   "bolder");
         FONT_WEIGHTS.put("lighter",   "lighter");
         // NOTE: 'bolder' and 'lighter' need to be handled programmatically
         
         BORDER_WIDTHS = new HashMap();
         BORDER_WIDTHS.put("thin",   "1px");
         BORDER_WIDTHS.put("medium", "2px");
         BORDER_WIDTHS.put("thick",  "3px");
 
         BACKGROUND_POSITIONS = new HashMap();
         
         BACKGROUND_POSITIONS.put("top left", "0% 0%");
         BACKGROUND_POSITIONS.put("left top", "0% 0%");
         
         BACKGROUND_POSITIONS.put("top", "50% 0%");
         BACKGROUND_POSITIONS.put("top center", "50% 0%");
         BACKGROUND_POSITIONS.put("center top", "50% 0%");
 
         BACKGROUND_POSITIONS.put("right top", "100% 0%");
         BACKGROUND_POSITIONS.put("top right", "100% 0%");
         
         BACKGROUND_POSITIONS.put("left", "0% 50%");
         BACKGROUND_POSITIONS.put("left center", "0% 50%");
         BACKGROUND_POSITIONS.put("center left", "0% 50%");
 
         BACKGROUND_POSITIONS.put("center", "50% 50%");
         BACKGROUND_POSITIONS.put("center center", "50% 50%");
         
         BACKGROUND_POSITIONS.put("right", "100% 50%");
         BACKGROUND_POSITIONS.put("right center", "100% 50%");
         BACKGROUND_POSITIONS.put("center right", "100% 50%");
 
         BACKGROUND_POSITIONS.put("bottom left", "0% 100%");
         BACKGROUND_POSITIONS.put("left bottom", "0% 100%");
 
         BACKGROUND_POSITIONS.put("bottom", "50% 100%");
         BACKGROUND_POSITIONS.put("bottom center", "50% 100%");
         BACKGROUND_POSITIONS.put("center bottom", "50% 100%");
         
         BACKGROUND_POSITIONS.put("bottom right", "100% 100%");
         BACKGROUND_POSITIONS.put("right bottom", "100% 100%");
         
         BACKGROUND_REPEATS = new ArrayList();
         BACKGROUND_REPEATS.add("repeat");
         BACKGROUND_REPEATS.add("repeat-x");
         BACKGROUND_REPEATS.add("repeat-y");
         BACKGROUND_REPEATS.add("no-repeat");
         
         BORDER_STYLES = new ArrayList();
         BORDER_STYLES.add( "none" );
         BORDER_STYLES.add( "hidden" );
         BORDER_STYLES.add( "dotted" );
         BORDER_STYLES.add( "dashed" );
         BORDER_STYLES.add( "solid" );
         BORDER_STYLES.add( "double" );
         BORDER_STYLES.add( "groove" );
         BORDER_STYLES.add( "ridge" );
         BORDER_STYLES.add( "inset" );
         BORDER_STYLES.add( "outset" );
 
         LIST_TYPES = new ArrayList();
         LIST_TYPES.add("disc");
         LIST_TYPES.add("circle");
         LIST_TYPES.add("square");
         LIST_TYPES.add("decimal");
         LIST_TYPES.add("decimal-leading-zero");
         LIST_TYPES.add("lower-roman");
         LIST_TYPES.add("upper-roman");
         LIST_TYPES.add("lower-greek");
         LIST_TYPES.add("lower-alpha");
         LIST_TYPES.add("lower-latin");
         LIST_TYPES.add("upper-alpha");
         LIST_TYPES.add("upper-latin");
         LIST_TYPES.add("hebrew");
         LIST_TYPES.add("armenian");
         LIST_TYPES.add("georgian");
         LIST_TYPES.add("cjk-ideographic");
         LIST_TYPES.add("hiragana");
         LIST_TYPES.add("katakana");
         LIST_TYPES.add("hiragana-iroha");
         LIST_TYPES.add("katakana-iroha");
         LIST_TYPES.add("none");
         
         FONT_STYLES = new ArrayList();
         FONT_STYLES.add("normal");
         FONT_STYLES.add("italic");
         FONT_STYLES.add("oblique");
     
     
     }
     
     public RuleNormalizer() {
         init_border_styles_map();
     }
     
     public CSSStyleRule normalize(CSSStyleRule rule) {
         int length = rule.getStyle().getLength();
         //HACK: we need to rewrite all properties in expandProperty
         //that way, we keep good cascade in case some fool redefined part of a shorthand-property
         //The good solution would be to create a new CSSStyleRule entirely
         for(int i=0; i<length; i++) {
            String prop = rule.getStyle().item(i);
             expandProperty(prop,rule);
 
         }
         return rule;
 
     }
     
     public static String convertIdent(String propName, String ident) {
         String val = null;
         if ( "font-size".equals(propName)) {
             String size = (String)FONT_SIZES.get(ident);
             val = ( size == null ? ident : size );
         } else if ( "font-weight".equals(propName)) {
             String size = (String)FONT_WEIGHTS.get(ident);
             val = ( size == null ? ident : size );
         } else if ( "background-position".equals(propName)) {
             String pos = (String)BACKGROUND_POSITIONS.get(ident);
             val = ( pos == null ? ident : pos );
         } else if ( propName.startsWith("border")) {
             if ( propName.endsWith("width")) {
                 String size = (String)BORDER_WIDTHS.get(ident);
                 val = ( size == null ? ident : size );
             } else if ( propName.endsWith("color")) {
                 val = getColorHex(ident);
             } else {
                 val = ident;
             }
         } else if ( propName.indexOf("color") >= 0) {
             val = getColorHex(ident);
         } else {
             val = ident;
         }
         return val;
     }
     
     /**
      * Description of the Method
      *
      * @param val  PARAM
      * @return     Returns
      */
     public static boolean looksLikeABorderStyle( String val ) {
         return BORDER_STYLES.contains( val );
     }
     
 
     /**
      * Description of the Method
      *
      * @param val  PARAM
      * @return     Returns
      */
     public static boolean looksLikeAColor( String val ) {
         return COLOR_MAP.get(val) != null || ( val.startsWith( "#" ) && ( val.length() == 7 || val.length() == 4 ) ) || val.startsWith( "rgb" );
     }
     
     public static boolean looksLikeANumber( String val ) {
         try {
             Float f = new Float(val);
         } catch ( Exception ex ) {
             return false;
         }
         return true;
     }
     
     public static boolean looksLikeALength( String val ) {
         if ( "0".equals(val)) {
             return true;
         }
         String n = null;
         String unitIDs[] = { "%", "em", "ex", "px", "in", "cm", "mm", "pt", "pc"};
         for ( int i=0; i < unitIDs.length; i++ ) {
             if ( val.endsWith(unitIDs[i])) {
                 n = val.substring(0, val.length() - unitIDs[i].length());
                 break;
             }
         }
         return looksLikeANumber(n);
     }
 
     public static boolean looksLikeAURI( String val ) {
         return val.startsWith("url(") && val.endsWith(")");
     }
     
     public static boolean looksLikeABGRepeat(String val) {
         return BACKGROUND_REPEATS.indexOf(val) >= 0;
     } 
     
     public static boolean looksLikeABGAttachment(String val) {
         return "scroll".equals(val) || "fixed".equals(val);
     }
     
     public static boolean looksLikeAListStyleType(String val) {
         return LIST_TYPES.indexOf(val) >= 0;
     }
     
     public static boolean looksLikeAListStyleImage(String val) {
         return "none".equals(val) || looksLikeAURI(val);
     }
     
     public static boolean looksLikeAListStylePosition(String val) {
         return "inside".equals(val) || "outside".equals(val);
     }
     
     public static boolean looksLikeAFontStyle(String val) {
         return FONT_STYLES.indexOf(val) >= 0;
     } 
     
     public static boolean looksLikeAFontVariant(String val) {
         return "normal".equals(val) || "small-caps".equals(val);
     } 
 
     public static boolean looksLikeAFontWeight(String val) {
         return FONT_WEIGHTS.get(val) != null;
     } 
 
     public static boolean looksLikeAFontSize(String val) {
         // TODO
         return FONT_SIZES.get(val) != null || 
                looksLikeALength(val) ||
                "larger".equals(val) || "smaller".equals(val);
     } 
 
     public static boolean looksLikeALineHeight(String val) {
         return "normal".equals(val) || looksLikeALength(val);
     } 
 
     private void expandProperty(String prop, CSSStyleRule rule) {
         if( prop.equals("color") ||
             prop.equals("background-color") ||
             prop.equals("border-color")) {
                 String value =  rule.getStyle().getPropertyValue(prop);
                 rule.getStyle().setProperty(prop,getColorHex(value),rule.getStyle().getPropertyPriority(prop));
             return;
         }
         else if(prop.equals("padding")) {
             expand(prop,rule);
         }
         else if(prop.equals("margin")) {
             expand(prop,rule);
         }
         else if(prop.equals("border-width")) {
             expand("border-width",rule,"border","-width");
         }
         else if(prop.equals("background-position")) {
             expandBackgroundPosition(rule);
         }
         else if(prop.equals("border")) {
             //u.p("normalizing: " + prop);
             expandBorder(rule);
         }
        else {//rewrite it. This is a HACK to keep the order.
             rule.getStyle().setProperty(prop,rule.getStyle().getPropertyValue(prop),rule.getStyle().getPropertyPriority(prop));
        }
     }
     
     
     private void expand(String prop, CSSStyleRule rule) {
         expand(prop,rule,prop,"");
     }
     
     public void expand(String prop, CSSStyleRule rule, String before, String after) {
         //u.p("rule = " + rule);
         //u.p("prop = " + prop);
         CSSStyleDeclaration dec = rule.getStyle();
         CSSValue val = dec.getPropertyCSSValue(prop);
         String prio = dec.getPropertyPriority(prop);
         //u.p("value = " + val);
         CSSValueList list = (CSSValueList)val;
         CSSValue top, bottom, left, right;
         top = bottom = left = right = null;

         if(val.getCssValueType() == val.CSS_VALUE_LIST) {
             if(list.getLength() == 2) {
                 //u.p("turning two into four");
                 top = list.item(0);
                 bottom = list.item(0);
                 left = list.item(1);
                 right = list.item(1);
             }
             if(list.getLength() == 3) {
                 //u.p("turning three into four");
                 top = list.item(0);
                 left = list.item(1);
                 right = list.item(1);
                 bottom = list.item(2);
             }
             if(list.getLength() == 4) {
                 //u.p("turning three into four");
                 top = list.item(0);
                 right = list.item(1);
                 bottom = list.item(2);
                 left = list.item(3);
             }
         } else {
             //u.p("only one to transform");
             top = list;
             bottom = list;//.item(0);
             left = list;//.item(0);
             right = list;//.item(0);
         }
         dec.setProperty(before+"-top"+after,top.getCssText(),prio);
         dec.setProperty(before+"-bottom"+after,bottom.getCssText(),prio);
         dec.setProperty(before+"-left"+after,left.getCssText(),prio);
         dec.setProperty(before+"-right"+after,right.getCssText(),prio);
     }
     
     private void expandBorder(CSSStyleRule rule) {
         CSSStyleDeclaration dec = rule.getStyle();
         CSSValue val = dec.getPropertyCSSValue("border");
         String prio = dec.getPropertyPriority("border");
         CSSValueList list = (CSSValueList)val;
         CSSValue a, b, c;
         if(val.getCssValueType() == val.CSS_VALUE_LIST) {
             for(int i=0; i<list.getLength(); i++) {
                 CSSValue v = list.item(i);
                 if(isDimension(v.getCssText())) {
                     dec.setProperty("border-width",v.getCssText(),prio);
                     expandProperty("border-width",rule);
                 }
                 else if(isColor(v.getCssText())) {
                     dec.setProperty("border-color",v.getCssText(),prio);
                     expandProperty("border-color",rule);
                 }
                 else if(isBorderStyle(v.getCssText())) {
                     dec.setProperty("border-style",v.getCssText(),prio);
                     expandProperty("border-style",rule);
                 }
                 else {
                     System.err.println("unhandled value type in RuleNormalizer.expandBorder");
                 }
             }
         }
     }
 
     private void expandBackgroundPosition(CSSStyleRule rule) {
         CSSStyleDeclaration dec = rule.getStyle();
         CSSValue val = dec.getPropertyCSSValue("background-position");
         String prio = dec.getPropertyPriority("background-position");
         if(val.getCssValueType() == val.CSS_VALUE_LIST) {
             //u.p("val = " + val);
             rule.getStyle().setProperty("background-position",rule.getStyle().getPropertyValue("background-position"),prio);
             return;
         }
         else if(val.getCssValueType() == val.CSS_PRIMITIVE_VALUE) {
             //u.p("val = " + val);
             String str = val.getCssText();
             if(str.startsWith("top")) { dec.setProperty("background-position","50% 0%",prio); }
             if(str.startsWith("bottom")) { dec.setProperty("background-position","50% 100%",prio); }
             if(str.startsWith("left")) { dec.setProperty("background-position","0% 50%",prio); }
             if(str.startsWith("right")) { dec.setProperty("background-position","100% 50%",prio); }
             return;
         }
         else System.err.println("unhandled value-type in RuleNormalizer.expandBackgroundPosition");
 
     }
     
     public static String getColorHex(String value) {
         if(value.indexOf("rgb")>=0) {
             return value;
         }
         String retval = (String)COLOR_MAP.get(value.toLowerCase());
         return retval;
     }
         
    private boolean isColor(String test) {
         if(COLOR_MAP.containsKey(test)) {
             return true;
         }
         //u.p("test = " + test);
         if(test.indexOf("rgb") >=0) {
             return true;
         }
         return false;
     }
     
     /*
     private boolean isBorderWidth(String test) {
         
         if(test.equals("thick")) { return true; }
         if(test.equals("medium")) { return true; }
         if(test.equals("thin")) { return true; }
         
         return isDimension(test);
     }
     */
     
     /** only supports px right now
     */
     private boolean isDimension(String test) {
         if(test.indexOf("px") >=0) {
             return true;
         }
         return false;
     }
     
     private HashMap border_map;
     private void init_border_styles_map() {
         border_map = new HashMap();
         border_map.put("none","none");
         border_map.put("hidden","hidden");
         border_map.put("dotted","dotted");
         border_map.put("dashed","dashed");
         border_map.put("solid","solid");
         border_map.put("double","double");
         border_map.put("groove","groove");
         border_map.put("ridge","ridge");
         border_map.put("inset","inset");
         border_map.put("outset","outset");
     }
     
     private boolean isBorderStyle(String test) {
         if(border_map.containsKey(test)) {
             return true;
         }
         return false;
     }
  
     public static void main(String args[]) {
         try {
             String vals[] = { "0", "1%", "2em", "3ex", "4px", "5in", "6cm", "7mm", "8pt", "9pc", "-1%", "-2em", "-3ex", "-4px", "-5in", "-6cm", "-7mm", "-8pt", "-9pc", "10%", "20em", "30ex", "40px", "50in", "60cm", "70mm", "80pt", "90pc", "N%", "Nem", "Nex", "Npx", "Nin", "Ncm", "Nmm", "Npt", "Npc" };
             for ( int i=0; i < vals.length; i++ ) {
                 System.out.println(vals[i] + " is a length " + RuleNormalizer.looksLikeALength(vals[i]));
             }
         } catch ( Exception ex ) {
             ex.printStackTrace();
         }
     } // end main()
 
 }
