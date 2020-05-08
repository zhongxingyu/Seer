 // Copyright (C) 2011 Chan Wai Shing
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //      http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 package prettify.theme;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import prettify.gui.JTextComponentRowHeader;
 
 /**
  * Theme for the Java Prettify.
  * <p>To make a new theme, either extending this class of initiate this class 
  * and set the parameter by the setter. For the default value, please refer to 
  * the constructor.</p>
  * @author Chan Wai Shing <cws1989@gmail.com>
  */
 public class Theme {
 
   /**
    * Indicate whether it is in debug mode or not.
    */
   protected final static boolean debug;
 
   static {
     String debugMode = System.getProperty("PrettifyDebugMode");
     debug = debugMode == null || !debugMode.equals("true") ? false : true;
   }
   /**
    * The font of the script text.
    */
   protected Font font;
   /**
    * The background color of the script text area.
    */
   protected Color background;
   /**
    * The background color of the highlighted line of script text.
    */
   protected Color highlightedBackground;
   /**
    * Gutter (line number column on the left)
    */
   /**
    * The color of the gutter text.
    */
   protected Color gutterText;
   /**
    * The color of the border that joint the gutter and the script text panel.
    */
   protected Color gutterBorderColor;
   /**
    * The width of the border that joint the gutter and the script text panel.
    */
   protected int gutterBorderWidth;
   /**
    * The font of the gutter text.
    */
   protected Font gutterTextFont;
   /**
    * The minimum padding from 'the leftmost of the line number text' to 
    * 'the left margin'.
    */
   protected int gutterTextPaddingLeft;
   /**
    * The minimum padding from 'the rightmost of the line number text' to 
    * 'the right margin' (not to the gutter border).
    */
   protected int gutterTextPaddingRight;
   protected Style string;
   protected Style keyword;
   protected Style comment;
   protected Style type;
   protected Style literal;
   protected Style punctuation;
   protected Style plain;
   protected Style tag;
   protected Style declaration;
   protected Style source;
   protected Style attributeName;
   protected Style attributeValue;
   protected Style noCode;
   protected Style openBracket;
   protected Style closeBracket;
   protected Style variable;
   protected Style function;
 
   /**
    * Constructor.<br />
    * <p>
    * <b>Default value:</b><br />
    * <ul>
    * <li>font: Consolas 12pt</li>
    * <li>background: white</li>
    * <li>gutter text: black</li>
    * <li>gutter border: R: 184, G: 184, B: 184</li>
    * <li>gutter border width: 3px</li>
    * <li>gutter text font: Consolas 12pt</li>
    * <li>gutter text padding-left: 7px</li>
    * <li>gutter text padding-right: 7px</li>
    * <li>plain, comments, string, keyword, preprocessor, variable, value, 
    * functions, constants, script, scriptBackground, color1, color2, color3: 
    * no style set</li>
    * </ul>
    * </p>
    */
   public Theme() {
     font = new Font("Consolas", Font.PLAIN, 12);
     background = Color.white;
 
     highlightedBackground = Color.gray;
 
     gutterText = Color.black;
     gutterBorderColor = new Color(184, 184, 184);
     gutterBorderWidth = 3;
     gutterTextFont = new Font("Consolas", Font.PLAIN, 12);
     gutterTextPaddingLeft = 7;
     gutterTextPaddingRight = 7;
 
     string = new Style();
     keyword = new Style();
     comment = new Style();
     type = new Style();
     literal = new Style();
     punctuation = new Style();
     plain = new Style();
     tag = new Style();
     declaration = new Style();
     source = new Style();
     attributeName = new Style();
     attributeValue = new Style();
     noCode = new Style();
     openBracket = new Style();
     closeBracket = new Style();
     variable = new Style();
     function = new Style();
   }
 
   /**
    * Apply the theme to the row header panel.
    * @param rowHeader the row header to apply theme on
    */
   public void setTheme(JTextComponentRowHeader rowHeader) {
     rowHeader.setBackground(background);
     rowHeader.setHighlightedColor(background);
 
     rowHeader.setForeground(gutterText);
     rowHeader.setBorderColor(gutterBorderColor);
     rowHeader.setBorderWidth(gutterBorderWidth);
     rowHeader.setFont(gutterTextFont);
     rowHeader.setPaddingLeft(gutterTextPaddingLeft);
     rowHeader.setPaddingRight(gutterTextPaddingRight);
   }
 
   /**
   * Get the {@link prettify.theme.Style} by keyword.
    * @param key the keyword, possible values: plain, comments, string, keyword, 
    * preprocessor, variable, value, functions, constants, script, 
    * scriptBackground, color1, color2 or color3
   * @return the {@link prettify.theme.Style} related to the {@code key}; if 
    * the style related to the <code>key</code> not exist, the style of 'plain'
    * will return.
    */
   public Style getStyle(String key) {
     if (key == null) {
       return plain;
     }
     if (key.equals("str")) {
       return string;
     } else if (key.equals("kwd")) {
       return keyword;
     } else if (key.equals("com")) {
       return comment;
     } else if (key.equals("typ")) {
       return type;
     } else if (key.equals("lit")) {
       return literal;
     } else if (key.equals("pun")) {
       return punctuation;
     } else if (key.equals("pln")) {
       return plain;
     } else if (key.equals("tag")) {
       return tag;
     } else if (key.equals("dec")) {
       return declaration;
     } else if (key.equals("src")) {
       return source;
     } else if (key.equals("atn")) {
       return attributeName;
     } else if (key.equals("atv")) {
       return attributeValue;
     } else if (key.equals("nocode")) {
       return noCode;
     } else if (key.equals("opn")) {
       return openBracket;
     } else if (key.equals("clo")) {
       return closeBracket;
     } else if (key.equals("var")) {
       return variable;
     } else if (key.equals("fun")) {
       return function;
     } else {
       // key not exist
       return plain;
     }
   }
 
   /**
    * The font of the script text.
    * @return the font
    */
   public Font getFont() {
     return font;
   }
 
   /**
    * The font of the script text.
    * @param font the font
    */
   public void setFont(Font font) {
     if (font == null) {
       throw new NullPointerException("argument 'font' cannot be null");
     }
     this.font = font;
   }
 
   /**
    * The background color of the script text area.
    * @return the color
    */
   public Color getBackground() {
     return background;
   }
 
   /**
    * The background color of the script text area.
    * @param background the color
    */
   public void setBackground(Color background) {
     if (background == null) {
       throw new NullPointerException("argument 'background' cannot be null");
     }
     this.background = background;
   }
 
   /**
    * The background color of the highlighted line of script text.
    * @return the color
    */
   public Color getHighlightedBackground() {
     return highlightedBackground;
   }
 
   /**
    * The background color of the highlighted line of script text.
    * @param highlightedBackground the color
    */
   public void setHighlightedBackground(Color highlightedBackground) {
     if (highlightedBackground == null) {
       throw new NullPointerException("argument 'highlightedBackground' cannot be null");
     }
     this.highlightedBackground = highlightedBackground;
   }
 
   /**
    * The color of the gutter text.
    * @return the color
    */
   public Color getGutterText() {
     return gutterText;
   }
 
   /**
    * The color of the gutter text.
    * @param gutterText the color
    */
   public void setGutterText(Color gutterText) {
     if (gutterText == null) {
       throw new NullPointerException("argument 'gutterText' cannot be null");
     }
     this.gutterText = gutterText;
   }
 
   /**
    * The color of the border that joint the gutter and the script text panel.
    * @return the color
    */
   public Color getGutterBorderColor() {
     return gutterBorderColor;
   }
 
   /**
    * The color of the border that joint the gutter and the script text panel.
    * @param gutterBorderColor the color
    */
   public void setGutterBorderColor(Color gutterBorderColor) {
     if (gutterBorderColor == null) {
       throw new NullPointerException("argument 'gutterBorderColor' cannot be null");
     }
     this.gutterBorderColor = gutterBorderColor;
   }
 
   /**
    * The width of the border that joint the gutter and the script text panel.
    * @return the width in pixel
    */
   public int getGutterBorderWidth() {
     return gutterBorderWidth;
   }
 
   /**
    * The width of the border that joint the gutter and the script text panel.
    * @param gutterBorderWidth in pixel
    */
   public void setGutterBorderWidth(int gutterBorderWidth) {
     this.gutterBorderWidth = gutterBorderWidth;
   }
 
   /**
    * The font of the gutter text.
    * @return the font
    */
   public Font getGutterTextFont() {
     return gutterTextFont;
   }
 
   /**
    * The font of the gutter text.
    * @param gutterTextFont the font
    */
   public void setGutterTextFont(Font gutterTextFont) {
     if (gutterTextFont == null) {
       throw new NullPointerException("argument 'gutterTextFont' cannot be null");
     }
     this.gutterTextFont = gutterTextFont;
   }
 
   /**
    * The minimum padding from 'the leftmost of the line number text' to 'the left margin'.
    * @return the padding in pixel
    */
   public int getGutterTextPaddingLeft() {
     return gutterTextPaddingLeft;
   }
 
   /**
    * The minimum padding from 'the leftmost of the line number text' to 
    * 'the left margin'.
    * @param gutterTextPaddingLeft in pixel
    */
   public void setGutterTextPaddingLeft(int gutterTextPaddingLeft) {
     this.gutterTextPaddingLeft = gutterTextPaddingLeft;
   }
 
   /**
    * The minimum padding from 'the rightmost of the line number text' to 
    * 'the right margin' (not to the gutter border).
    * @return the padding in pixel
    */
   public int getGutterTextPaddingRight() {
     return gutterTextPaddingRight;
   }
 
   /**
    * The minimum padding from 'the rightmost of the line number text' to 
    * 'the right margin' (not to the gutter border).
    * @param gutterTextPaddingRight in pixel
    */
   public void setGutterTextPaddingRight(int gutterTextPaddingRight) {
     this.gutterTextPaddingRight = gutterTextPaddingRight;
   }
 
   public Style getString() {
     return string;
   }
 
   public void setString(Style string) {
     if (string == null) {
       throw new NullPointerException("argument 'string' cannot be null");
     }
     this.string = string;
   }
 
   public Style getKeyword() {
     return keyword;
   }
 
   public void setKeyword(Style keyword) {
     if (keyword == null) {
       throw new NullPointerException("argument 'keyword' cannot be null");
     }
     this.keyword = keyword;
   }
 
   public Style getComment() {
     return comment;
   }
 
   public void setComment(Style comment) {
     if (comment == null) {
       throw new NullPointerException("argument 'comment' cannot be null");
     }
     this.comment = comment;
   }
 
   public Style getType() {
     return type;
   }
 
   public void setType(Style type) {
     if (type == null) {
       throw new NullPointerException("argument 'type' cannot be null");
     }
     this.type = type;
   }
 
   public Style getLiteral() {
     return literal;
   }
 
   public void setLiteral(Style literal) {
     if (literal == null) {
       throw new NullPointerException("argument 'literal' cannot be null");
     }
     this.literal = literal;
   }
 
   public Style getPunctuation() {
     return punctuation;
   }
 
   public void setPunctuation(Style punctuation) {
     if (punctuation == null) {
       throw new NullPointerException("argument 'punctuation' cannot be null");
     }
     this.punctuation = punctuation;
   }
 
   public Style getPlain() {
     return plain;
   }
 
   public void setPlain(Style plain) {
     if (plain == null) {
       throw new NullPointerException("argument 'plain' cannot be null");
     }
     this.plain = plain;
   }
 
   public Style getTag() {
     return tag;
   }
 
   public void setTag(Style tag) {
     if (tag == null) {
       throw new NullPointerException("argument 'tag' cannot be null");
     }
     this.tag = tag;
   }
 
   public Style getDeclaration() {
     return declaration;
   }
 
   public void setDeclaration(Style declaration) {
     if (declaration == null) {
       throw new NullPointerException("argument 'declaration' cannot be null");
     }
     this.declaration = declaration;
   }
 
   public Style getSource() {
     return source;
   }
 
   public void setSource(Style source) {
     if (source == null) {
       throw new NullPointerException("argument 'source' cannot be null");
     }
     this.source = source;
   }
 
   public Style getAttributeName() {
     return attributeName;
   }
 
   public void setAttributeName(Style attributeName) {
     if (attributeName == null) {
       throw new NullPointerException("argument 'attributeName' cannot be null");
     }
     this.attributeName = attributeName;
   }
 
   public Style getAttributeValue() {
     return attributeValue;
   }
 
   public void setAttributeValue(Style attributeValue) {
     if (attributeValue == null) {
       throw new NullPointerException("argument 'attributeValue' cannot be null");
     }
     this.attributeValue = attributeValue;
   }
 
   public Style getNoCode() {
     return noCode;
   }
 
   public void setNoCode(Style noCode) {
     if (noCode == null) {
       throw new NullPointerException("argument 'noCode' cannot be null");
     }
     this.noCode = noCode;
   }
 
   public Style getOpenBracket() {
     return openBracket;
   }
 
   public void setOpenBracket(Style openBracket) {
     if (openBracket == null) {
       throw new NullPointerException("argument 'openBracket' cannot be null");
     }
     this.openBracket = openBracket;
   }
 
   public Style getCloseBracket() {
     return closeBracket;
   }
 
   public void setCloseBracket(Style closeBracket) {
     if (closeBracket == null) {
       throw new NullPointerException("argument 'closeBracket' cannot be null");
     }
     this.closeBracket = closeBracket;
   }
 
   public Style getVariable() {
     return variable;
   }
 
   public void setVariable(Style variable) {
     if (variable == null) {
       throw new NullPointerException("argument 'variable' cannot be null");
     }
     this.variable = variable;
   }
 
   public Style getFunction() {
     return function;
   }
 
   public void setFunction(Style function) {
     if (function == null) {
       throw new NullPointerException("argument 'function' cannot be null");
     }
     this.function = function;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Theme clone() {
     Theme object = null;
     try {
       object = (Theme) super.clone();
       object.font = font;
       object.background = background;
       object.highlightedBackground = highlightedBackground;
       object.gutterText = gutterText;
       object.gutterBorderColor = gutterBorderColor;
       object.gutterBorderWidth = gutterBorderWidth;
       object.gutterTextFont = gutterTextFont;
       object.gutterTextPaddingLeft = gutterTextPaddingLeft;
       object.gutterTextPaddingRight = gutterTextPaddingRight;
       object.string = string.clone();
       object.keyword = keyword.clone();
       object.comment = comment.clone();
       object.type = type.clone();
       object.literal = literal.clone();
       object.punctuation = punctuation.clone();
       object.plain = plain.clone();
       object.tag = tag.clone();
       object.declaration = declaration.clone();
       object.source = source.clone();
       object.attributeName = attributeName.clone();
       object.attributeValue = attributeValue.clone();
       object.noCode = noCode.clone();
       object.openBracket = openBracket.clone();
       object.closeBracket = closeBracket.clone();
       object.variable = variable.clone();
       object.function = function.clone();
     } catch (CloneNotSupportedException ex) {
       if (debug) {
         Logger.getLogger(Theme.class.getName()).log(Level.WARNING, null, ex);
       }
     }
     return object;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
     StringBuilder sb = new StringBuilder();
 
     sb.append(getClass().getName());
     sb.append(": ");
     sb.append("font: ").append(getFont());
     sb.append(", ");
     sb.append("background: ").append(getBackground());
     sb.append(", ");
     sb.append("highlightedBackground: ").append(getHighlightedBackground());
     sb.append(", ");
     sb.append("gutterText: ").append(getGutterText());
     sb.append(", ");
     sb.append("gutterBorderColor: ").append(getGutterBorderColor());
     sb.append(", ");
     sb.append("gutterBorderWidth: ").append(getGutterBorderWidth());
     sb.append(", ");
     sb.append("gutterTextFont: ").append(getGutterTextFont());
     sb.append(", ");
     sb.append("gutterTextPaddingLeft: ").append(getGutterTextPaddingLeft());
     sb.append(", ");
     sb.append("gutterTextPaddingRight: ").append(getGutterTextPaddingRight());
     sb.append(", ");
     sb.append("string: ").append(getString());
     sb.append(", ");
     sb.append("keyword: ").append(getKeyword());
     sb.append(", ");
     sb.append("comment: ").append(getComment());
     sb.append(", ");
     sb.append("type: ").append(getType());
     sb.append(", ");
     sb.append("literal: ").append(getLiteral());
     sb.append(", ");
     sb.append("punctuation: ").append(getPunctuation());
     sb.append(", ");
     sb.append("plain: ").append(getPlain());
     sb.append(", ");
     sb.append("tag: ").append(getTag());
     sb.append(", ");
     sb.append("declaration: ").append(getDeclaration());
     sb.append(", ");
     sb.append("source: ").append(getSource());
     sb.append(", ");
     sb.append("attributeName: ").append(getAttributeName());
     sb.append(", ");
     sb.append("attributeValue: ").append(getAttributeValue());
     sb.append(", ");
     sb.append("noCode: ").append(getNoCode());
     sb.append(", ");
     sb.append("openBracket: ").append(getOpenBracket());
     sb.append(", ");
     sb.append("closeBracket: ").append(getCloseBracket());
     sb.append(", ");
     sb.append("variable: ").append(getVariable());
     sb.append(", ");
     sb.append("function: ").append(getFunction());
 
     return sb.toString();
   }
 }
