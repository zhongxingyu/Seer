 /*
  * WebPalette.java
  * Transform
  *
  * Copyright (c) 2009-2010 Flagstone Software Ltd. All rights reserved.
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
 package com.flagstone.transform.datatype;
 
 /**
  * WebPalette defined the set of colours from the Netscape Colour Table.
  *
  * <style type="text/css">
  * p.color {
  *     border: solid thin black;
  *     width:200px;
  *     padding:5px;
  *     text-align: center;
  * }
  * </style>
  */
 public enum WebPalette {
     /** <p class="color" style="background-color:#F0F8FF;">0xF0F8FF</p>. */
    ALICE_BLUE(0xF0, 0xF8, 0xFf),
     /** <p class="color" style="background-color:#FAEBD7;">0xFAEBD7</p>. */
     ANTIQUE_WHITE(0xFA, 0xEB, 0xD7),
     /** <p class="color" style="background-color:#00FFFF;">0x00FFFF</p>. */
     AQUA(0x00, 0xFF, 0xFF),
     /** <p class="color" style="background-color:#7FFFD4;">0x7FFFD4</p>. */
     AQUAMARINE(0x7F, 0xFF, 0xD4),
     /** <p class="color" style="background-color:#F0FFFF;">0xF0FFFF</p>. */
     AZURE(0xF0, 0xFF, 0xFF),
     /** <p class="color" style="background-color:#F5F5DC;">0xF5F5DC</p>. */
     BEIGE(0xF5, 0xF5, 0xDC),
     /** <p class="color" style="background-color:#FFE4C4;">0xFFE4C4</p>. */
     BISQUE(0xFF, 0xE4, 0xC4),
     /** <p class="color" style="background-color:#000000;">0x000000</p>. */
     BLACK(0x00, 0x00, 0x00),
     /** <p class="color" style="background-color:#FFEBCD;">0xFFEBCD</p>. */
     BLANCHED_ALMOND(0xFF, 0xEB, 0xCD),
     /** <p class="color" style="background-color:#0000FF;">0x0000FF</p>. */
     BLUE(0x00, 0x00, 0xFF),
     /** <p class="color" style="background-color:#8A2BE2;">0x8A2BE2</p>. */
     BLUE_VIOLET(0x8A, 0x2B, 0xE2),
     /** <p class="color" style="background-color:#A52A2A;">0xA52A2A</p>. */
     BROWN(0xA5, 0x2A, 0x2A),
     /** <p class="color" style="background-color:#DEB887;">0xDEB887</p>. */
     BURLYWOOD(0xDE, 0xB8, 0x87),
     /** <p class="color" style="background-color:#5F9EA0;">0x5F9EA0</p>. */
     CADET_BLUE(0x5F, 0x9E, 0xA0),
     /** <p class="color" style="background-color:#7FFF00;">0x7FFF00</p>. */
     CHARTREUSE(0x7F, 0xFF, 0x00),
     /** <p class="color" style="background-color:#D2691E;">0xD2691E</p>. */
     CHOCOLATE(0xD2, 0x69, 0x1E),
     /** <p class="color" style="background-color:#FF7F50;">0xFF7F50</p>. */
     CORAL(0xFF, 0x7F, 0x50),
     /** <p class="color" style="background-color:#6495ED;">0x6495ED</p>. */
     CORNFLOWER_BLUE(0x64, 0x95, 0xED),
     /** <p class="color" style="background-color:#FFF8DC;">0xFFF8DC</p>. */
     CORNSILK(0xFF, 0xF8, 0xDC),
     /** <p class="color" style="background-color:#DC143C;">0xDC143C</p>. */
    CRIMSON(0xDC, 0x14, 0x3C3C),
     /** <p class="color" style="background-color:#00FFFF;">0x00FFFF</p>. */
     CYAN(0x00, 0xFF, 0xFF),
     /** <p class="color" style="background-color:#00008B;">0x00008B</p>. */
     DARK_BLUE(0x00, 0x00, 0x8B),
     /** <p class="color" style="background-color:#008B8B;">0x008B8B</p>. */
     DARK_CYAN(0x00, 0x8B, 0x8B),
     /** <p class="color" style="background-color:#B8860B;">0xB8860B</p>. */
     DARK_GOLDENROD(0xB8, 0x86, 0x0B),
     /** <p class="color" style="background-color:#A9A9A9;">0xA9A9A9</p>. */
     DARK_GRAY(0xA9, 0xA9, 0xA9),
     /** <p class="color" style="background-color:#006400;">0x006400</p>. */
     DARK_GREEN(0x00, 0x64, 0x00),
     /** <p class="color" style="background-color:#BDB76B;">0xBDB76B</p>. */
     DARK_KHAKI(0xBD, 0xB7, 0x6B),
     /** <p class="color" style="background-color:#8B008B;">0x8B008B</p>. */
     DARK_MAGENTA(0x8B, 0x00, 0x8B),
     /** <p class="color" style="background-color:#556B2F;">0x556B2F</p>. */
     DARK_OLIVE_GREEN(0x55, 0x6B, 0x2F),
     /** <p class="color" style="background-color:#FF8C00;">0xFF8C00</p>. */
     DARK_ORANGE(0xFF, 0x8C, 0x00),
     /** <p class="color" style="background-color:#9932CC;">0x9932CC</p>. */
     DARK_ORCHID(0x99, 0x32, 0xCC),
     /** <p class="color" style="background-color:#8B0000;">0x8B0000</p>. */
     DARK_RED(0x8B, 0x00, 0x00),
     /** <p class="color" style="background-color:#E9967A;">0xE9967A</p>. */
     DARK_SALMON(0xE9, 0x96, 0x7A),
     /** <p class="color" style="background-color:#8FBC8F;">0x8FBC8F</p>. */
     DARK_SEA_GREEN(0x8F, 0xBC, 0x8F),
     /** <p class="color" style="background-color:#483D8B;">0x483D8B</p>. */
     DARK_SLATE_BLUE(0x48, 0x3D, 0x8B),
     /** <p class="color" style="background-color:#2F4F4F;">0x2F4F4F</p>. */
     DARK_SLATE_GRAY(0x2F, 0x4F, 0x4F),
     /** <p class="color" style="background-color:#00CED1;">0x00CED1</p>. */
     DARK_TURQUOISE(0x00, 0xCE, 0xD1),
     /** <p class="color" style="background-color:#9400D3;">0x9400D3</p>. */
     DARK_VIOLET(0x94, 0x00, 0xD3),
     /** <p class="color" style="background-color:#FF1493;">0xFF1493</p>. */
     DEEP_PINK(0xFF, 0x14, 0x93),
     /** <p class="color" style="background-color:#00BFFF;">0x00BFFF</p>. */
     DEEP_SKY_BLUE(0x00, 0xBF, 0xFF),
     /** <p class="color" style="background-color:#696969;">0x696969</p>. */
     DIM_GRAY(0x69, 0x69, 0x69),
     /** <p class="color" style="background-color:#1E90FF;">0x1E90FF</p>. */
     DODGER_BLUE(0x1E, 0x90, 0xFF),
     /** <p class="color" style="background-color:#B22222;">0xB22222</p>. */
     FIREBRICK(0xB2, 0x22, 0x22),
     /** <p class="color" style="background-color:#FFFAF0;">0xFFFAF0</p>. */
     FLORAL_WHITE(0xFF, 0xFA, 0xF0),
     /** <p class="color" style="background-color:#228B22;">0x228B22</p>. */
     FOREST_GREEN(0x22, 0x8B, 0x22),
     /** <p class="color" style="background-color:#FF00FF;">0xFF00FF</p>. */
     FUCHSIA(0xFF, 0x00, 0xFF),
     /** <p class="color" style="background-color:#DCDCDC;">0xDCDCDC</p>. */
     GAINSBORO(0xDC, 0xDC, 0xDC),
     /** <p class="color" style="background-color:#F8F8FB;">0xF8F8FB</p>. */
     GHOST_WHITE(0xF8, 0xF8, 0xFB),
     /** <p class="color" style="background-color:#FFD700;">0xFFD700</p>. */
     GOLD(0xFF, 0xD7, 0x00),
     /** <p class="color" style="background-color:#DAA520;">0xDAA520</p>. */
     GOLDENROD(0xDA, 0xA5, 0x20),
     /** <p class="color" style="background-color:#808080;">0x808080</p>. */
     GRAY(0x80, 0x80, 0x80),
     /** <p class="color" style="background-color:#008000;">0x008000</p>. */
     GREEN(0x00, 0x80, 0x00),
     /** <p class="color" style="background-color:#ADFF2F;">0xADFF2F</p>. */
     GREEN_YELLOW(0xAD, 0xFF, 0x2F),
     /** <p class="color" style="background-color:#F0FFF0;">0xF0FFF0</p>. */
     HONEYDEW(0xF0, 0xFF, 0xF0),
     /** <p class="color" style="background-color:#FF69B4;">0xFF69B4</p>. */
     HOT_PINK(0xFF, 0x69, 0xB4),
     /** <p class="color" style="background-color:#CD5C5C;">0xCD5C5C</p>. */
     INDIAN_RED(0xCD, 0x5C, 0x5C),
     /** <p class="color" style="background-color:#4B0082;">0x4B0082</p>. */
     INDIGO(0x4B, 0x00, 0x82),
     /** <p class="color" style="background-color:#FFFFF0;">0xFFFFF0</p>. */
     IVORY(0xFF, 0xFF, 0xF0),
     /** <p class="color" style="background-color:#F0E68C;">0xF0E68C</p>. */
     KHAKI(0xF0, 0xE6, 0x8C),
     /** <p class="color" style="background-color:#E6E6FA;">0xE6E6FA</p>. */
     LAVENDER(0xE6, 0xE6, 0xFA),
     /** <p class="color" style="background-color:#FFF0F5;">0xFFF0F5</p>. */
     LAVENDER_BLUSH(0xFF, 0xF0, 0xF5),
     /** <p class="color" style="background-color:#7CFC00;">0x7CFC00</p>. */
     LAWN_GREEN(0x7C, 0xFC, 0x00),
     /** <p class="color" style="background-color:#FFFACD;">0xFFFACD</p>. */
     LEMON_CHIFFON(0xFF, 0xFA, 0xCD),
     /** <p class="color" style="background-color:#ADD8E6;">0xADD8E6</p>. */
     LIGHT_BLUE(0xAD, 0xD8, 0xE6),
     /** <p class="color" style="background-color:#F08080;">0xF08080</p>. */
     LIGHT_CORAL(0xF0, 0x80, 0x80),
     /** <p class="color" style="background-color:#E0FFFF;">0xE0FFFF</p>. */
     LIGHT_CYAN(0xE0, 0xFF, 0xFF),
     /** <p class="color" style="background-color:#FAFAD2;">0xFAFAD2</p>. */
     LIGHT_GOLDENROD_YELLOW(0xFA, 0xFA, 0xD2),
     /** <p class="color" style="background-color:#90EE90;">0x90EE90</p>. */
     LIGHT_GREEN(0x90, 0xEE, 0x90),
     /** <p class="color" style="background-color:#D3D3D3;">0xD3D3D3</p>. */
     LIGHT_GREY(0xD3, 0xD3, 0xD3),
     /** <p class="color" style="background-color:#FFB6C1;">0xFFB6C1</p>. */
     LIGHT_PINK(0xFF, 0xB6, 0xC1),
     /** <p class="color" style="background-color:#FFA07A;">0xFFA07A</p>. */
     LIGHT_SALMON(0xFF, 0xA0, 0x7A),
     /** <p class="color" style="background-color:#20B2AA;">0x20B2AA</p>. */
     LIGHT_SEA_GREEN(0x20, 0xB2, 0xAA),
     /** <p class="color" style="background-color:#87CEFA;">0x87CEFA</p>. */
     LIGHT_SKY_BLUE(0x87, 0xCE, 0xFA),
     /** <p class="color" style="background-color:#778899;">0x778899</p>. */
     LIGHT_SLATE_GRAY(0x77, 0x88, 0x99),
     /** <p class="color" style="background-color:#B0C4DE;">0xB0C4DE</p>. */
     LIGHT_STEEL_BLUE(0xB0, 0xC4, 0xDE),
     /** <p class="color" style="background-color:#FFFFE0;">0xFFFFE0</p>. */
     LIGHT_YELLOW(0xFF, 0xFF, 0xE0),
     /** <p class="color" style="background-color:#00FF00;">0x00FF00</p>. */
     LIME(0x00, 0xFF, 0x00),
     /** <p class="color" style="background-color:#32CD32;">0x32CD32</p>. */
     LIME_GREEN(0x32, 0xCD, 0x32),
     /** <p class="color" style="background-color:#FAF0E6;">0xFAF0E6</p>. */
     LINEN(0xFA, 0xF0, 0xE6),
     /** <p class="color" style="background-color:#FF00FF;">0xFF00FF</p>. */
     MAGENTA(0xFF, 0x00, 0xFF),
     /** <p class="color" style="background-color:#800000;">0x800000</p>. */
     MAROON(0x80, 0x00, 0x00),
     /** <p class="color" style="background-color:#66CDAA;">0x66CDAA</p>. */
     MEDIUM_AQUAMARINE(0x66, 0xCD, 0xAA),
     /** <p class="color" style="background-color:#0000CD;">0x0000CD</p>. */
     MEDIUM_BLUE(0x00, 0x00, 0xCD),
     /** <p class="color" style="background-color:#BA55D3;">0xBA55D3</p>. */
     MEDIUM_ORCHID(0xBA, 0x55, 0xD3),
     /** <p class="color" style="background-color:#9370DB;">0x9370DB</p>. */
     MEDIUM_PURPLE(0x93, 0x70, 0xDB),
     /** <p class="color" style="background-color:#3CB371;">0x3CB371</p>. */
     MEDIUM_SEAGREEN(0x3C, 0xB3, 0x71),
     /** <p class="color" style="background-color:#7B68EE;">0x7B68EE</p>. */
     MEDIUM_SLATEBLUE(0x7B, 0x68, 0xEE),
     /** <p class="color" style="background-color:#00FA9A;">0x00FA9A</p>. */
     MEDIUM_SPRINGGREEN(0x00, 0xFA, 0x9A),
     /** <p class="color" style="background-color:#48D1CC;">0x48D1CC</p>. */
     MEDIUM_TURQUOISE(0x48, 0xD1, 0xCC),
     /** <p class="color" style="background-color:#C71585;">0xC71585</p>. */
     MEDIUM_VIOLETRED(0xC7, 0x15, 0x85),
     /** <p class="color" style="background-color:#191970;">0x191970</p>. */
     MIDNIGHT_BLUE(0x19, 0x19, 0x70),
     /** <p class="color" style="background-color:#F5FFFA;">0xF5FFFA</p>. */
     MINT_CREAM(0xF5, 0xFF, 0xFA),
     /** <p class="color" style="background-color:#FFE4E1;">0xFFE4E1</p>. */
     MISTY_ROSE(0xFF, 0xE4, 0xE1),
     /** <p class="color" style="background-color:#FFE4B5;">0xFFE4B5</p>. */
     MOCCASIN(0xFF, 0xE4, 0xB5),
     /** <p class="color" style="background-color:#FFDEAD;">0xFFDEAD</p>. */
     NAVAJO_WHITE(0xFF, 0xDE, 0xAD),
     /** <p class="color" style="background-color:#000080;">0x000080</p>. */
     NAVY(0x00, 0x00, 0x80),
     /** <p class="color" style="background-color:#FDF5E6;">0xFDF5E6</p>. */
     OLD_LACE(0xFD, 0xF5, 0xE6),
     /** <p class="color" style="background-color:#808000;">0x808000</p>. */
     OLIVE(0x80, 0x80, 0x00),
     /** <p class="color" style="background-color:#6B8E23;">0x6B8E23</p>. */
     OLIVE_DRAB(0x6B, 0x8E, 0x23),
     /** <p class="color" style="background-color:#FFA500;">0xFFA500</p>. */
     ORANGE(0xFF, 0xA5, 0x00),
     /** <p class="color" style="background-color:#FF4500;">0xFF4500</p>. */
     ORANGE_RED(0xFF, 0x45, 0x00),
     /** <p class="color" style="background-color:#DA70D6;">0xDA70D6</p>. */
     ORCHID(0xDA, 0x70, 0xD6),
     /** <p class="color" style="background-color:#EEE8AA;">0xEEE8AA</p>. */
     PALE_GOLDENROD(0xEE, 0xE8, 0xAA),
     /** <p class="color" style="background-color:#98FB98;">0x98FB98</p>. */
     PALE_GREEN(0x98, 0xFB, 0x98),
     /** <p class="color" style="background-color:#AFEEEE;">0xAFEEEE</p>. */
     PALE_TURQUOISE(0xAF, 0xEE, 0xEE),
     /** <p class="color" style="background-color:#DB7093;">0xDB7093</p>. */
     PALE_VIOLET_RED(0xDB, 0x70, 0x93),
     /** <p class="color" style="background-color:#FFEFD5;">0xFFEFD5</p>. */
     PAPAYA_WHIP(0xFF, 0xEF, 0xD5),
     /** <p class="color" style="background-color:#FFDAB9;">0xFFDAB9</p>. */
     PEACH_PUFF(0xFF, 0xDA, 0xB9),
     /** <p class="color" style="background-color:#CD853F;">0xCD853F</p>. */
     PERU(0xCD, 0x85, 0x3F),
     /** <p class="color" style="background-color:#FFC0CB;">0xFFC0CB</p>. */
     PINK(0xFF, 0xC0, 0xCB),
     /** <p class="color" style="background-color:#DDA0DD;">0xDDA0DD</p>. */
     PLUM(0xDD, 0xA0, 0xDD),
     /** <p class="color" style="background-color:#B0E0E6;">0xB0E0E6</p>. */
     POWDER_BLUE(0xB0, 0xE0, 0xE6),
     /** <p class="color" style="background-color:#800080;">0x800080</p>. */
     PURPLE(0x80, 0x00, 0x80),
     /** <p class="color" style="background-color:#FF0000;">0xFF0000</p>. */
     RED(0xFF, 0x00, 0x00),
     /** <p class="color" style="background-color:#BC8F8F;">0xBC8F8F</p>. */
     ROSY_BROWN(0xBC, 0x8F, 0x8F),
     /** <p class="color" style="background-color:#4169E1;">0x4169E1</p>. */
     ROYAL_BLUE(0x41, 0x69, 0xE1),
     /** <p class="color" style="background-color:#8B4513;">0x8B4513</p>. */
     SADDLE_BROWN(0x8B, 0x45, 0x13),
     /** <p class="color" style="background-color:#FA8072;">0xFA8072</p>. */
     SALMON(0xFA, 0x80, 0x72),
     /** <p class="color" style="background-color:#F4A460;">0xF4A460</p>. */
     SANDY_BROWN(0xF4, 0xA4, 0x60),
     /** <p class="color" style="background-color:#2E8B57;">0x2E8B57</p>. */
     SEA_GREEN(0x2E, 0x8B, 0x57),
     /** <p class="color" style="background-color:#FFF5EE;">0xFFF5EE</p>. */
     SEASHELL(0xFF, 0xF5, 0xEE),
     /** <p class="color" style="background-color:#A0522D;">0xA0522D</p>. */
     SIENNA(0xA0, 0x52, 0x2D),
     /** <p class="color" style="background-color:#C0C0C0;">0xC0C0C0</p>. */
     SILVER(0xC0, 0xC0, 0xC0),
     /** <p class="color" style="background-color:#87CEEB;">0x87CEEB</p>. */
     SKY_BLUE(0x87, 0xCE, 0xEB),
     /** <p class="color" style="background-color:#6A5ACD;">0x6A5ACD</p>. */
     SLATE_BLUE(0x6A, 0x5A, 0xCD),
     /** <p class="color" style="background-color:#708090;">0x708090</p>. */
     SLATE_GRAY(0x70, 0x80, 0x90),
     /** <p class="color" style="background-color:#FFFAFA;">0xFFFAFA</p>. */
     SNOW(0xFF, 0xFA, 0xFA),
     /** <p class="color" style="background-color:#00FF7F;">0x00FF7F</p>. */
     SPRING_GREEN(0x00, 0xFF, 0x7F),
     /** <p class="color" style="background-color:#4682B4;">0x4682B4</p>. */
     STEEL_BLUE(0x46, 0x82, 0xB4),
     /** <p class="color" style="background-color:#D2B48C;">0xD2B48C</p>. */
     TAN(0xD2, 0xB4, 0x8C),
     /** <p class="color" style="background-color:#008080;">0x008080</p>. */
     TEAL(0x00, 0x80, 0x80),
     /** <p class="color" style="background-color:#D8BFD8;">0xD8BFD8</p>. */
     THISTLE(0xD8, 0xBF, 0xD8),
     /** <p class="color" style="background-color:#FF6347;">0xFF6347</p>. */
     TOMATO(0xFF, 0x63, 0x47),
     /** <p class="color" style="background-color:#40E0D0;">0x40E0D0</p>. */
     TURQUOISE(0x40, 0xE0, 0xD0),
     /** <p class="color" style="background-color:#EE82EE;">0xEE82EE</p>. */
     VIOLET(0xEE, 0x82, 0xEE),
     /** <p class="color" style="background-color:#F5DEB3;">0xF5DEB3</p>. */
     WHEAT(0xF5, 0xDE, 0xB3),
     /** <p class="color" style="background-color:#FFFFFF;">0xFFFFFF</p>. */
     WHITE(0xFF, 0xFF, 0xFF),
     /** <p class="color" style="background-color:#F5F5F5;">0xF5F5F5</p>. */
     WHITE_SMOKE(0xF5, 0xF5, 0xF5),
     /** <p class="color" style="background-color:#FFFF00;">0xFFFF00</p>. */
     YELLOW(0xFF, 0xFF, 0x00),
     /** <p class="color" style="background-color:#9ACD32;">0x9ACD32</p>. */
     YELLOW_GREEN(0x9A, 0xCD, 0x32);
 
     /** Holds the colour from the palette. */
     private Color color;
 
     /**
      * Private constructor used to create the palette colours.
      *
      * @param red value for the red channel.
      * @param green value for the green channel.
      * @param blue value for the blue channel.
      */
     private WebPalette(final int red, final int green, final int blue) {
         color = new Color(red, green, blue);
     }
 
     /**
      * Gets an opaque Color object for this entry in the palette.
      *
      * @return
      *           the Color object representing this entry in the table.
      */
     public Color color() {
         return color;
     }
 
     /**
      * Gets a transparent Color object for this entry in the palette.
      *
      * @param alpha
      *           the level for the alpha channel.
      * @return
      *           the Color object representing this entry in the table.
      */
     public Color color(final int alpha) {
         return new Color(color.getRed(), color.getGreen(),
                 color.getBlue(), alpha);
     }
 }
