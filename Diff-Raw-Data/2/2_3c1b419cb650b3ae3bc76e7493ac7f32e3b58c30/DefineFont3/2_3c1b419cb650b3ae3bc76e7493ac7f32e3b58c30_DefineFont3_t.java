 /*
  * DefineFont2.java
  * Transform
  *
  * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of Flagstone Software Ltd. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.flagstone.transform.font;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.flagstone.transform.DefineTag;
 import com.flagstone.transform.SWF;
 import com.flagstone.transform.coder.Coder;
 import com.flagstone.transform.coder.Context;
 import com.flagstone.transform.coder.MovieTypes;
 import com.flagstone.transform.coder.SWFDecoder;
 import com.flagstone.transform.coder.SWFEncoder;
 import com.flagstone.transform.datatype.Bounds;
 import com.flagstone.transform.exception.IllegalArgumentRangeException;
 import com.flagstone.transform.shape.Shape;
 import com.flagstone.transform.shape.ShapeData;
 
 //TODO(code) Implement with updated doc and same changes as DefineFont2
 
 /**
  * <p>
  * DefineFont2 defines the shapes and layout of the glyphs used in a font. It
  * extends the functionality provided by DefineFont and FontInfo by:
  * </p>
  *
  * <ul>
  * <li>allowing more than 65535 glyphs in a particular font.</li>
  * <li>including the functionality provided by the FontInfo class.</li>
  * <li>specifying ascent, descent and leading for the font.</li>
  * <li>specifying advances for each glyph.</li>
  * <li>specifying bounding rectangles for each glyph.</li>
  * <li>specifying kerning pairs defining the distance between glyph pairs.</li>
  * </ul>
  *
  * @see FontInfo
  * @see DefineFont
  */
 //TODO(class)
 @SuppressWarnings({"PMD.TooManyFields", "PMD.TooManyMethods" })
 public final class DefineFont3 implements DefineTag {
     /** Format string used in toString() method. */
     private static final String FORMAT = "DefineFont3: { identifier=%d;"
             + " encoding=%d; small=%b; italic=%b; bold=%b; language=%s;"
             + " name=%s; shapes=%s; codes=%s; ascent=%d; descent=%d;"
             + " leading=%d; advances=%s; bounds=%s; kernings=%s }";
 
     /** The unique identifier for this object. */
     private int identifier;
     private int encoding;
     private boolean small;
     private boolean italic;
     private boolean bold;
     private int language;
     private String name;
     private List<Shape> shapes;
     private List<Integer> codes;
     private int ascent;
     private int descent;
     private int leading;
     private List<Integer> advances;
     private List<Bounds> bounds;
     private List<Kerning> kernings;
 
     /** The length of the object, minus the header, when it is encoded. */
     private transient int length;
     private transient int[] table;
     private transient boolean wideOffsets;
     private transient boolean wideCodes;
 
     /**
      * Creates and initialises a DefineFont3 object using values encoded
      * in the Flash binary format.
      *
      * @param coder
      *            an SWFDecoder object that contains the encoded Flash data.
      *
      * @param context
      *            a Context object used to manage the decoders for different
      *            type of object and to pass information on how objects are
      *            decoded.
      *
      * @throws IOException
      *             if an error occurs while decoding the data.
      */
     public DefineFont3(final SWFDecoder coder, final Context context)
             throws IOException {
         length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
         if (length == Coder.IS_EXTENDED) {
             length = coder.readInt();
         }
         coder.mark();
         identifier = coder.readUnsignedShort();
         shapes = new ArrayList<Shape>();
         codes = new ArrayList<Integer>();
         advances = new ArrayList<Integer>();
         bounds = new ArrayList<Bounds>();
         kernings = new ArrayList<Kerning>();
 
         final int bits = coder.readByte();
         final boolean containsLayout = (bits & Coder.BIT7) != 0;
         final int format = (bits & 0x70) >> 4;
 
         encoding = 0;
 
         if (format == 1) {
             encoding = 1;
         } else if (format == 2) {
             small = true;
         } else if (format == 4) {
             encoding = 2;
         }
 
         wideOffsets = (bits & Coder.BIT3) != 0;
         wideCodes = (bits & Coder.BIT2) != 0;
         italic = (bits & Coder.BIT1) != 0;
         bold = (bits & Coder.BIT0) != 0;
 
         if (wideCodes) {
             context.put(Context.WIDE_CODES, 1);
         }
 
         language = coder.readByte();
         final int nameLength = coder.readByte();
         name = coder.readString(nameLength);
 
         if (name.length() > 0) {
             while (name.charAt(name.length() - 1) == 0) {
                 name = name.substring(0, name.length() - 1);
             }
         }
 
         final int glyphCount = coder.readUnsignedShort();
         final int[] offset = new int[glyphCount + 1];
 
         if (wideOffsets) {
             for (int i = 0; i < glyphCount; i++) {
                 offset[i] = coder.readInt();
             }
         } else {
             for (int i = 0; i < glyphCount; i++) {
                 offset[i] = coder.readUnsignedShort();
             }
         }
 
         if (wideOffsets) {
             offset[glyphCount] = coder.readInt();
         } else {
             offset[glyphCount] = coder.readUnsignedShort();
         }
 
         Shape shape;
         byte[] data;
 
         for (int i = 0; i < glyphCount; i++) {
             shape = new Shape();
             data = new byte[offset[i + 1] - offset[i]];
             shape.add(new ShapeData(coder.readBytes(data)));
             shapes.add(shape);
         }
 
         if (wideCodes) {
             for (int i = 0; i < glyphCount; i++) {
                 codes.add(coder.readUnsignedShort());
             }
         } else {
             for (int i = 0; i < glyphCount; i++) {
                 codes.add(coder.readByte());
             }
         }
 
        if (containsLayout || coder.bytesRead() < length) {
             ascent = coder.readSignedShort();
             descent = coder.readSignedShort();
             leading = coder.readSignedShort();
 
             for (int i = 0; i < glyphCount; i++) {
                 advances.add(coder.readSignedShort());
             }
 
             for (int i = 0; i < glyphCount; i++) {
                 bounds.add(new Bounds(coder));
             }
 
             final int kerningCount = coder.readUnsignedShort();
 
             for (int i = 0; i < kerningCount; i++) {
                 kernings.add(new Kerning(coder, context));
             }
         }
 
         context.remove(Context.WIDE_CODES);
         coder.unmark(length);
     }
 
     /**
      * Creates a DefineFont2 object specifying only the name of the font.
      *
      * If none of the remaining attributes are set the Flash Player will load
      * the font from the system on which it is running or substitute a suitable
      * font if the specified font cannot be found. This is particularly useful
      * when defining fonts that will be used to display text in DefineTextField
      * objects.
      *
      * The font will be defined to use Unicode encoding. The flags which define
      * the font's face will be set to false. The arrays of glyphs which define
      * the shapes and the code which map the character codes to a particular
      * glyph will remain empty since the font is loaded from the system on which
      * it is displayed.
      *
      * @param uid
      *            the unique identifier for this font object.
      * @param fontName
      *            the name of the font.
      */
     public DefineFont3(final int uid, final String fontName) {
         setIdentifier(uid);
         setName(fontName);
 
         encoding = 0;
         shapes = new ArrayList<Shape>();
         codes = new ArrayList<Integer>();
         advances = new ArrayList<Integer>();
         bounds = new ArrayList<Bounds>();
         kernings = new ArrayList<Kerning>();
     }
 
     /**
      * Creates and initialises a DefineFont3 object using the values copied
      * from another DefineFont3 object.
      *
      * @param object
      *            a DefineFont3 object from which the values will be
      *            copied.
      */
     public DefineFont3(final DefineFont3 object) {
         identifier = object.identifier;
         encoding = object.encoding;
         small = object.small;
         italic = object.italic;
         bold = object.bold;
         language = object.language;
         name = object.name;
         ascent = object.ascent;
         descent = object.descent;
         leading = object.leading;
         shapes = new ArrayList<Shape>(object.shapes.size());
         for (final Shape shape : object.shapes) {
             shapes.add(shape.copy());
         }
         codes = new ArrayList<Integer>(object.codes);
         advances = new ArrayList<Integer>(object.advances);
         bounds = new ArrayList<Bounds>(object.bounds);
         kernings = new ArrayList<Kerning>(object.kernings);
     }
 
     /** {@inheritDoc} */
     public int getIdentifier() {
         return identifier;
     }
 
     /** {@inheritDoc} */
     public void setIdentifier(final int uid) {
         if ((uid < SWF.MIN_IDENTIFIER) || (uid > SWF.MAX_IDENTIFIER)) {
             throw new IllegalArgumentRangeException(
                     SWF.MIN_IDENTIFIER, SWF.MAX_IDENTIFIER, uid);
         }
         identifier = uid;
     }
 
     /**
      * Add a character code and the corresponding glyph that will be displayed.
      * Character codes should be added to the font in ascending order.
      *
      * @param code
      *            the character code. Must be in the range 0..65535.
      * @param obj
      *            the shape that represents the glyph displayed for the
      *            character code.
      * @return this object.
      */
     public DefineFont3 addGlyph(final int code, final Shape obj) {
         if ((code < 0) || (code > SWF.MAX_CHARACTER)) {
             throw new IllegalArgumentRangeException(0, SWF.MAX_CHARACTER, code);
         }
         codes.add(code);
 
         if (obj == null) {
             throw new IllegalArgumentException();
         }
         shapes.add(obj);
 
         return this;
     }
 
     /**
      * Add an advance to the array of advances. The index position of the entry
      * in the advance array is also used to identify the corresponding glyph and
      * vice-versa.
      *
      * @param anAdvance
      *            an advance for a glyph. Must be in the range -32768..32767.
      * @return this object.
      */
     public DefineFont3 addAdvance(final int anAdvance) {
         if ((anAdvance < SWF.MIN_ADVANCE) || (anAdvance > SWF.MAX_ADVANCE)) {
             throw new IllegalArgumentRangeException(
                     SWF.MIN_ADVANCE, SWF.MAX_ADVANCE, anAdvance);
         }
         advances.add(anAdvance);
         return this;
     }
 
     /**
      * Add a bounds object to the array of bounds for each glyph. The index
      * position of the entry in the bounds array is also used to identify the
      * corresponding glyph and vice-versa.
      *
      * @param obj
      *            an Bounds. Must not be null.
      * @return this object.
      */
     public DefineFont3 add(final Bounds obj) {
         if (obj == null) {
             throw new IllegalArgumentException();
         }
         bounds.add(obj);
         return this;
     }
 
     /**
      * Add a kerning object to the array of kernings for pairs of glyphs.
      *
      * @param anObject
      *            an Kerning. Must not be null.
      * @return this object.
      */
     public DefineFont3 add(final Kerning anObject) {
         if (anObject == null) {
             throw new IllegalArgumentException();
         }
         kernings.add(anObject);
         return this;
     }
 
     /**
      * Returns the encoding scheme used for characters rendered in the font,
      * either ASCII, SJIS or UCS2.
      *
      * @return the encoding used for character codes.
      */
     public CharacterFormat getEncoding() {
         CharacterFormat value;
         switch(encoding) {
         case 0:
             value = CharacterFormat.UCS2;
             break;
         case 1:
             value = CharacterFormat.ANSI;
             break;
         case 2:
             value = CharacterFormat.SJIS;
             break;
         default:
             throw new IllegalStateException();
         }
         return value;
     }
 
     /**
      * Does the font have a small point size. This is used only with a Unicode
      * font encoding.
      *
      * @return a boolean indicating whether the font will be aligned on pixel
      *         boundaries.
      */
     public boolean isSmall() {
         return small;
     }
 
     /**
      * Sets the font is small. Used only with Unicode fonts.
      *
      * @param aBool
      *            a boolean flag indicating the font will be aligned on pixel
      *            boundaries.
      */
     public void setSmall(final boolean aBool) {
         small = aBool;
     }
 
     // End Flash 7
 
     /**
      * Is the font italicised.
      *
      * @return a boolean indicating whether the font is rendered in italics.
      */
     public boolean isItalic() {
         return italic;
     }
 
     /**
      * Is the font bold.
      *
      * @return a boolean indicating whether the font is rendered in a bold face.
      */
     public boolean isBold() {
         return bold;
     }
 
     // Flash 6
     /**
      * Returns the language code identifying the type of spoken language for the
      * font either Text.Japanese, Text.Korean, Text.Latin,
      * Text.SimplifiedChinese or Text.TraditionalChinese.
      *
      * @return the language code used to determine how line-breaks are inserted
      *         into text rendered using the font. Returns 0 if the object was
      *         decoded from a movie contains Flash 5 or less.
      */
     public int getLanguage() {
         return language;
     }
 
     /**
      * Sets the language code used to determine the position of line-breaks in
      * text rendered using the font.
      *
      * NOTE: The language attribute is ignored if the object is encoded in a
      * Flash 5 movie.
      *
      * @param code
      *            the code identifying the spoken language either Text.Japanese,
      *            Text.Korean, Text.Latin, Text.SimplifiedChinese or
      *            Text.TraditionalChinese.
      */
     public void setLanguage(final int code) {
         language = code;
     }
 
     // End Flash 6
 
     /**
      * Returns the name of the font family.
      *
      * @return the name of the font.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Returns the array of shapes used to define the outlines of each font
      * glyph.
      *
      * @return an array of Shape objects
      */
     public List<Shape> getShapes() {
         return shapes;
     }
 
     /**
      * Returns the array of codes used to identify each glyph in the font. The
      * ordinal position of each Integer representing a code identifies a
      * particular glyph in the shapes array.
      *
      * @return an array of Integer objects that contain the character codes for
      *         each glyph in the font.
      */
     public List<Integer> getCodes() {
         return codes;
     }
 
     /**
      * Returns the ascent for the font in twips.
      *
      * @return the ascent for the font.
      */
     public int getAscent() {
         return ascent;
     }
 
     /**
      * Returns the descent for the font in twips.
      *
      * @return the descent for the font.
      */
     public int getDescent() {
         return descent;
     }
 
     /**
      * Returns the leading for the font in twips.
      *
      * @return the leading for the font.
      */
     public int getLeading() {
         return leading;
     }
 
     /**
      * Returns the array of advances defined for each glyph in the font.
      *
      * @return an array of Integer objects that contain the advance for each
      *         glyph in the font.
      */
     public List<Integer> getAdvances() {
         return advances;
     }
 
     /**
      * Returns the array of bounding rectangles defined for each glyph in the
      * font.
      *
      * @return an array of Bounds objects.
      */
     public List<Bounds> getBounds() {
         return bounds;
     }
 
     /**
      * Returns the array of kerning records that define the spacing between
      * glyph pairs.
      *
      * @return an array of Kerning objects that define the spacing adjustment
      *         between pairs of glyphs.
      */
     public List<Kerning> getKernings() {
         return kernings;
     }
 
     /**
      * Sets the font character encoding.
      *
      * @param anEncoding
      *            the encoding used to identify characters, either ASCII, SJIS
      *            or UNICODE.
      */
     public void setEncoding(final CharacterFormat anEncoding) {
         switch(anEncoding) {
         case UCS2:
             encoding = 0;
             break;
         case ANSI:
             encoding = 1;
             break;
         case SJIS:
             encoding = 2;
             break;
         default:
             throw new IllegalArgumentException();
         }
     }
 
     /**
      * Set the font is italicised.
      *
      * @param aBool
      *            a boolean flag indicating whether the font will be rendered in
      *            italics
      */
     public void setItalic(final boolean aBool) {
         italic = aBool;
     }
 
     /**
      * Set the font is bold.
      *
      * @param aBool
      *            a boolean flag indicating whether the font will be rendered in
      *            bold face.
      */
     public void setBold(final boolean aBool) {
         bold = aBool;
     }
 
     /**
      * Set the name of the font.
      *
      * @param aString
      *            the name assigned to the font, identifying the font family.
      *            Must not be null.
      */
     public void setName(final String aString) {
         if (aString == null) {
             throw new IllegalArgumentException();
         }
         name = aString;
     }
 
     /**
      * Set the array of shape records that define the outlines of the characters
      * used from the font.
      *
      * @param anArray
      *            an array of Shape objects that define the glyphs for the font.
      *            Must not be null.
      */
     public void setShapes(final List<Shape> anArray) {
         if (anArray == null) {
             throw new IllegalArgumentException();
         }
         shapes = anArray;
     }
 
     /**
      * Sets the codes used to identify each glyph in the font.
      *
      * @param anArray
      *            sets the code table that maps a particular glyph to a
      *            character code. Must not be null.
      */
     public void setCodes(final List<Integer> anArray) {
         if (anArray == null) {
             throw new IllegalArgumentException();
         }
         codes = anArray;
     }
 
     /**
      * Sets the ascent for the font in twips.
      *
      * @param aNumber
      *            the ascent for the font in the range -32768..32767.
      */
     public void setAscent(final int aNumber) {
         if ((aNumber < SWF.MIN_ASCENT) || (aNumber > SWF.MAX_ASCENT)) {
             throw new IllegalArgumentRangeException(
                     SWF.MIN_ASCENT, SWF.MAX_ASCENT, aNumber);
         }
         ascent = aNumber;
     }
 
     /**
      * Sets the descent for the font in twips.
      *
      * @param aNumber
      *            the descent for the font in the range -32768..32767.
      */
     public void setDescent(final int aNumber) {
         if ((aNumber < SWF.MIN_DESCENT) || (aNumber > SWF.MAX_DESCENT)) {
             throw new IllegalArgumentRangeException(
                     SWF.MIN_DESCENT, SWF.MAX_DESCENT, aNumber);
         }
         descent = aNumber;
     }
 
     /**
      * Sets the leading for the font in twips.
      *
      * @param aNumber
      *            the descent for the font in the range -32768..32767.
      */
     public void setLeading(final int aNumber) {
         if ((aNumber < SWF.MIN_LEADING) || (aNumber > SWF.MAX_LEADING)) {
             throw new IllegalArgumentRangeException(
                     SWF.MIN_LEADING, SWF.MAX_LEADING, aNumber);
         }
         leading = aNumber;
     }
 
     /**
      * Sets the array of advances for each glyph in the font.
      *
      * @param anArray
      *            of Integer objects that define the spacing between glyphs.
      *            Must not be null.
      */
     public void setAdvances(final List<Integer> anArray) {
         if (anArray == null) {
             throw new IllegalArgumentException();
         }
         advances = anArray;
     }
 
     /**
      * Sets the array of bounding rectangles for each glyph in the font.
      *
      * @param anArray
      *            an array of Bounds objects that define the bounding rectangles
      *            that enclose each glyph in the font. Must not be null.
      */
     public void setBounds(final List<Bounds> anArray) {
         if (anArray == null) {
             throw new IllegalArgumentException();
         }
         bounds = anArray;
     }
 
     /**
      * Sets the array of kerning records for pairs of glyphs in the font.
      *
      * @param anArray
      *            an array of Kerning objects that define an adjustment applied
      *            to the spacing between pairs of glyphs. Must not be null.
      */
     public void setKernings(final List<Kerning> anArray) {
         if (anArray == null) {
             throw new IllegalArgumentException();
         }
         kernings = anArray;
     }
 
     /** {@inheritDoc} */
     public DefineFont3 copy() {
         return new DefineFont3(this);
     }
 
     @Override
     public String toString() {
         return String.format(FORMAT, identifier, encoding, small, italic, bold,
                 language, name, shapes, codes, ascent, descent, leading,
                 advances, bounds, kernings);
     }
 
     /** {@inheritDoc} */
     public int prepareToEncode(final Context context) {
         wideCodes = (context.get(Context.VERSION) > 5)
                 || encoding != 1;
 
         context.put(Context.FILL_SIZE, 1);
         context.put(Context.LINE_SIZE, context.contains(Context.POSTSCRIPT) ? 1
                 : 0);
         if (wideCodes) {
             context.put(Context.WIDE_CODES, 1);
         }
 
         int index = 0;
         int count = shapes.size();
         int tableEntry;
         int shapeLength;
 
         if (wideOffsets) {
             tableEntry = (count << 2) + 4;
         } else {
             tableEntry = (count << 1) + 2;
         }
 
         table = new int[count + 1];
 
         int glyphLength = 0;
 
         for (final Shape shape : shapes) {
             table[index++] = tableEntry;
             shapeLength = shape.prepareToEncode(context);
             glyphLength += shapeLength;
             tableEntry += shapeLength;
         }
 
         table[index++] = tableEntry;
 
         wideOffsets = (shapes.size() * 2 + glyphLength) > 65535;
 
         length = 5;
         length += context.strlen(name);
         length += 2;
         length += shapes.size() * (wideOffsets ? 4 : 2);
         length += wideOffsets ? 4 : 2;
         length += glyphLength;
         length += shapes.size() * (wideCodes ? 2 : 1);
 
         if (containsLayoutInfo()) {
             length += 6;
             length += advances.size() * 2;
 
             for (final Bounds bound : bounds) {
                 length += bound.prepareToEncode(context);
             }
 
             length += 2;
             length += kernings.size() * (wideCodes ? 6 : 4);
         }
 
         context.put(Context.FILL_SIZE, 0);
         context.put(Context.LINE_SIZE, 0);
         context.remove(Context.WIDE_CODES);
 
         return (length > Coder.SHORT_HEADER_LIMIT ? Coder.LONG_HEADER
                 : Coder.SHORT_HEADER) + length;
     }
 
     /** {@inheritDoc} */
     public void encode(final SWFEncoder coder, final Context context)
             throws IOException {
         int format;
         if (encoding == 1) {
             format = 1;
         } else if (small) {
             format = 2;
         } else if (encoding == 2) {
             format = 4;
         } else {
             format = 0;
         }
 
         if (length > Coder.SHORT_HEADER_LIMIT) {
             coder.writeShort((MovieTypes.DEFINE_FONT_3
                     << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
             coder.writeInt(length);
         } else {
             coder.writeShort((MovieTypes.DEFINE_FONT_3
                     << Coder.LENGTH_FIELD_SIZE) | length);
         }
         coder.mark();
         coder.writeShort(identifier);
         context.put(Context.FILL_SIZE, 1);
         context.put(Context.LINE_SIZE, context.contains(Context.POSTSCRIPT) ? 1
                 : 0);
         if (wideCodes) {
             context.put(Context.WIDE_CODES, 1);
         }
 
         int bits = 0;
         bits |= containsLayoutInfo() ? Coder.BIT7 : 0;
         bits |= format << 4;
         bits |= wideOffsets ? Coder.BIT3 : 0;
         bits |= wideCodes ? Coder.BIT2 : 0;
         bits |= italic ? Coder.BIT1 : 0;
         bits |= bold ? Coder.BIT0 : 0;
         coder.writeByte(bits);
 
         coder.writeByte(language);
         coder.writeByte(context.strlen(name));
 
         coder.writeString(name);
         coder.writeShort(shapes.size());
 
         if (wideOffsets) {
             for (int i = 0; i < table.length; i++) {
                 coder.writeInt(table[i]);
             }
         } else {
             for (int i = 0; i < table.length; i++) {
                 coder.writeShort(table[i]);
             }
         }
 
         for (final Shape shape : shapes) {
             shape.encode(coder, context);
         }
 
         if (wideCodes) {
             for (final Integer code : codes) {
                 coder.writeShort(code.intValue());
             }
         } else {
             for (final Integer code : codes) {
                 coder.writeByte(code.intValue());
             }
         }
 
         if (containsLayoutInfo()) {
             coder.writeShort(ascent);
             coder.writeShort(descent);
             coder.writeShort(leading);
 
             for (final Integer advance : advances) {
                 coder.writeShort(advance.intValue());
             }
 
             for (final Bounds bound : bounds) {
                 bound.encode(coder, context);
             }
 
             coder.writeShort(kernings.size());
 
             for (final Kerning kerning : kernings) {
                 kerning.encode(coder, context);
             }
         }
 
         context.put(Context.FILL_SIZE, 0);
         context.put(Context.LINE_SIZE, 0);
         context.remove(Context.WIDE_CODES);
         coder.unmark(length);
     }
 
     private boolean containsLayoutInfo() {
         final boolean layout = (ascent != 0) || (descent != 0)
                 || (leading != 0) || !advances.isEmpty() || !bounds.isEmpty()
                 || !kernings.isEmpty();
 
         return layout;
     }
 }
