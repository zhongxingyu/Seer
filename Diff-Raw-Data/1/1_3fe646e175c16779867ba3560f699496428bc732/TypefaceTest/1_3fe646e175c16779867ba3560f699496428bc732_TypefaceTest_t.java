 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 
 package android.graphics.cts;
 
 import com.android.cts.stub.R;
 
 import dalvik.annotation.KnownFailure;
 import dalvik.annotation.TestLevel;
 import dalvik.annotation.TestTargetClass;
 import dalvik.annotation.TestTargetNew;
 import dalvik.annotation.TestTargets;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.graphics.Bitmap.Config;
 import android.test.AndroidTestCase;
 
 @TestTargetClass(android.graphics.Typeface.class)
 public class TypefaceTest extends AndroidTestCase {
 
     // generic family name for monospaced fonts
     private static final String MONO = "monospace";
     private static final String DEFAULT = (String)null;
     private static final String INVALID = "invalid-family-name";
 
     // list of family names to try when attempting to find a typeface with a given style
     private static final String[] FAMILIES =
             { (String) null, "monospace", "serif", "sans-serif", "cursive", "arial", "times" };
 
     /**
      * Create a typeface of the given style. If the default font does not support the style,
      * a number of generic families are tried.
      * @return The typeface or null, if no typeface with the given style can be found.
      */
     private Typeface createTypeface(int style) {
         for (String family : FAMILIES) {
             Typeface tf = Typeface.create(family, style);
             if (tf.getStyle() == style) {
                 return tf;
             }
         }
         return null;
     }
 
 
     @TestTargets ({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "isBold",
             args = {}
         ),
         @TestTargetNew(
                 level = TestLevel.COMPLETE,
                 method = "isItalic",
                 args = {}
         ),
         @TestTargetNew(
                 level = TestLevel.COMPLETE,
                 method = "getStyle",
                 args = {}
         )
     })
     public void testIsBold() {
         Typeface typeface = createTypeface(Typeface.BOLD);
         if (typeface != null) {
             assertEquals(Typeface.BOLD, typeface.getStyle());
             assertTrue(typeface.isBold());
             assertFalse(typeface.isItalic());
         }
 
         typeface = createTypeface(Typeface.ITALIC);
         if (typeface != null) {
             assertEquals(Typeface.ITALIC, typeface.getStyle());
             assertFalse(typeface.isBold());
             assertTrue(typeface.isItalic());
         }
 
         typeface = createTypeface(Typeface.BOLD_ITALIC);
         if (typeface != null) {
             assertEquals(Typeface.BOLD_ITALIC, typeface.getStyle());
             assertTrue(typeface.isBold());
             assertTrue(typeface.isItalic());
         }
 
         typeface = createTypeface(Typeface.NORMAL);
         if (typeface != null) {
             assertEquals(Typeface.NORMAL, typeface.getStyle());
             assertFalse(typeface.isBold());
             assertFalse(typeface.isItalic());
         }
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "create",
             args = {java.lang.String.class, int.class}
         ),
         @TestTargetNew(
                 level = TestLevel.COMPLETE,
                 method = "create",
                 args = {android.graphics.Typeface.class, int.class}
         )
     })
     public void testCreate() {
         Typeface typeface = Typeface.create(DEFAULT, Typeface.NORMAL);
         assertNotNull(typeface);
         typeface = Typeface.create(MONO, Typeface.BOLD);
         assertNotNull(typeface);
         typeface = Typeface.create(INVALID, Typeface.ITALIC);
         assertNotNull(typeface);
 
         typeface = Typeface.create(typeface, Typeface.NORMAL);
         assertNotNull(typeface);
         typeface = Typeface.create(typeface, Typeface.BOLD);
         assertNotNull(typeface);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "defaultFromStyle",
         args = {int.class}
     )
     public void testDefaultFromStyle() {
         Typeface typeface = Typeface.defaultFromStyle(Typeface.NORMAL);
         assertNotNull(typeface);
         typeface = Typeface.defaultFromStyle(Typeface.BOLD);
         assertNotNull(typeface);
         typeface = Typeface.defaultFromStyle(Typeface.ITALIC);
         assertNotNull(typeface);
         typeface = Typeface.defaultFromStyle(Typeface.BOLD_ITALIC);
         assertNotNull(typeface);
     }
 
     public void testConstants() {
         assertNotNull(Typeface.DEFAULT);
         assertNotNull(Typeface.DEFAULT_BOLD);
         assertNotNull(Typeface.MONOSPACE);
         assertNotNull(Typeface.SANS_SERIF);
         assertNotNull(Typeface.SERIF);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "createFromAsset",
         args = {android.content.res.AssetManager.class, java.lang.String.class}
     )
     public void testCreateFromAsset() {
         // input abnormal params.
         try {
             Typeface.createFromAsset(null, null);
             fail("Should throw a NullPointerException.");
         } catch (NullPointerException e) {
             // except here
         }
 
         Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "samplefont.ttf");
         assertNotNull(typeface);
 
         Bitmap bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
         bitmap.eraseColor(Color.BLACK);
         Canvas canvas = new Canvas(bitmap);
         Paint p = new Paint();
         p.setTypeface(typeface);
         p.setColor(Color.WHITE);
         p.setTextAlign(Paint.Align.CENTER);
         p.setTextSize(50);
         p.setFlags(0); // clear all flags (not sure what defaults flags are set)
         canvas.drawText("test", bitmap.getWidth() / 2, 3 * bitmap.getHeight() / 4 , p);
 
         BitmapFactory.Options opt = new BitmapFactory.Options();
         opt.inScaled = false;
         Bitmap expected = BitmapFactory.decodeResource(
                 getContext().getResources(), R.drawable.typeface_test, opt);
         assertEquals(expected.getWidth(), bitmap.getWidth());
         assertEquals(expected.getHeight(), bitmap.getHeight());
         for (int y = 0; y < bitmap.getHeight(); y++) {
             for (int x = 0; x < bitmap.getWidth(); x++) {
                 assertEquals(expected.getPixel(x, y), bitmap.getPixel(x, y));
             }
         }
     }
 }
