 /**
  * Copyright 2013 Alexis Lecanu
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package fr.ravenfeld.livewallpaper.library.objects.simple;
 
 import android.content.Context;
 import android.content.res.ColorStateList;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.PorterDuff;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 
 import fr.ravenfeld.livewallpaper.library.objects.Utils;
 import rajawali.materials.Material;
 import rajawali.materials.textures.ATexture.TextureException;
 import rajawali.materials.textures.Texture;
 import rajawali.primitives.Plane;
 
 public class Text extends AElement {
     protected Texture mTexture;
     protected TextView mText;
     private Bitmap mBitmap;
     private Canvas mCanvas;
     private final FrameLayout mWidgetGroup;
     private final Context mContext;
     private int mWidth;
     private int mHeight;
     private int mLayoutGravity;
     private int mWidthMax;
     public Text(Context context,String textureName, String text) {
         this(context,textureName, text, 14);
     }
 
     public Text(Context context,String textureName, String text, int textSize) {
         this(context,textureName, text, textSize, Gravity.CENTER);
     }
 
 
     public Text(Context context,String textureName, String text, int textSize, int layoutGravity) {
         this(context,textureName, text, textSize, getDisplayWidth(context), getDisplayHeight(context), layoutGravity);
     }
 
     public Text(Context context,String textureName, String text, int textSize, int width, int height, int layoutGravity) {
         mContext = context;
         mWidgetGroup = new FrameLayout(mContext);
         FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                 LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
         mLayoutGravity = layoutGravity;
         mWidgetGroup.setLayoutParams(params);
         mText = new TextView(mContext);
         mPlane = new Plane(1f, 1f, 1, 1);
         mPlane.setColor(Color.TRANSPARENT);
         mPlane.setTransparent(true);
         mPlane.setRotY(180);
 
         mWidth = Utils.nextPowerOfTwo(width);
         mHeight = Utils.nextPowerOfTwo(height);
         mWidthMax = width;
 
         mMaterial = new Material();
         mMaterial.setColorInfluence(0);
 
         mText.setText(text);
         mText.setTextSize(textSize);
         mText.setGravity(Gravity.LEFT);
         //mText.setBackgroundColor(Color.GREEN);
         mWidgetGroup.addView(mText);
         mWidgetGroup.setBackgroundColor(Color.TRANSPARENT);
         draw();
 
         mTexture = new Texture(textureName, mBitmap);
 
         mMaterial = new Material();
         try {
             mMaterial.addTexture(mTexture);
         } catch (TextureException e) {
             e.printStackTrace();
         }
         mPlane.setMaterial(mMaterial);
     }
 
 
 
 
     private static int getDisplayWidth(Context context) {
         Display display = ((WindowManager) context
                 .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
         return size.x;
     }
 
     private static int getDisplayHeight(Context context) {
         Display display = ((WindowManager) context
                 .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
         return size.y;
     }
 
     public void setWidgetBackgroundColor(int color) {
         mWidgetGroup.setBackgroundColor(color);
         draw();
     }
 
     public void setBackgroundColor(int color) {
         mText.setBackgroundColor(color);
         draw();
     }
 
     public void setBackground(Drawable drawable) {
         mText.setBackground(drawable);
         draw();
     }
 
     public void setBackgroundResource(int resId) {
         mText.setBackgroundResource(resId);
         draw();
     }
 
     public void setTextColor(int color) {
         mText.setTextColor(color);
         draw();
     }
 
     public void setTextColor(ColorStateList colors) {
         mText.setTextColor(colors);
         draw();
     }
 
     public void setText(String text) {
         mText.setText(text);
         draw();
     }
 
     public float getTextSize() {
         return mText.getTextSize();
     }
 
     public void setTextSize(float textSize) {
         mText.setTextSize(textSize);
         draw();
     }
 
     public String getText() {
         return mText.getText().toString();
     }
 
     public void setFont(String font) {
         Typeface type = Typeface.createFromAsset(mContext.getAssets(),
                 font);
         mText.setTypeface(type);
         draw();
     }
 
 
 
     public Plane getObject3D() {
         return mPlane;
     }
 
     @Override
     public int getWidth() {
         return mWidth;
     }
 
     @Override
     public int getHeight() {
         return mHeight;
     }
 
     public void getHeight(int height) {
         mHeight = height;
     }
 
     public void getWidth(int width) {
         mWidth = width;
     }
 
     public void surfaceChanged(int width, int height) {
         mPlane.setScaleX((float) mWidth / (float) width);
         mPlane.setScaleY((float) mHeight / (float) height);
     }
 
     public void setPosition(double x, double y) {
         mPlane.setPosition(x, y, mPlane.getZ());
     }
 
     public void setPosition(double x, double y, double z) {
         mPlane.setPosition(x, y, z);
     }
 
     public void setScale(double scaleX, double scaleY, double scaleZ) {
         mPlane.setScale(scaleX, scaleY, scaleZ);
     }
 
     private int nbLines() {
         String text = mText.getText().toString();
         String[] split = text.split("\n");
         return split.length;
     }
 
     private int getLineWidth() {
         Paint textPaint = mText.getPaint();
         String text = mText.getText().toString();
 
         String[] split = text.split("\n");
         int widthMax = 0;
 
         for (int i = 0; i < split.length; i++) {
             int width = Math.round(textPaint.measureText(split[i].trim()));
             if (width > widthMax) {
                 widthMax = width;
             }
         }
 
         return widthMax;
     }
 
 	private void splitLines(){
         int lineWidth = getLineWidth();
         while (lineWidth > mWidthMax) {
 
             String text = mText.getText().toString();
             String[] split = text.split("\n");
             Paint p = mText.getPaint();
             text = "";
             for (int i = 0; i < split.length; i++) {
                 String string = split[i];
                int nextPos = p.breakText(string, true, mWidthMax, null);

 
                 if (nextPos < string.length()) {
                     while(string.charAt(nextPos)!=' ' && nextPos>0){
                         nextPos--;
                     }
 					String stringLeft = string.substring(0, nextPos);
 					String stringRight= string.substring(nextPos, string.length());
                     string = stringLeft + "\n" + stringRight.trim();
                 }
                 if (text.equalsIgnoreCase("")) {
                     text = string;
                 } else {
                     text = text + "\n" + string;
                 }
             }
             mText.setText(text);
             lineWidth = getLineWidth();
         }
 	}
 
     private void draw() {
 		splitLines();
         int lineWidth = getLineWidth();
         int lineHeight = Math.round(mText.getLineHeight() * nbLines());
         mWidgetGroup.measure(mWidth, mHeight);
         mWidgetGroup.layout(0, 0, mWidth, mHeight);
 
         int posX =0;
         int posY = 0;
         switch (mLayoutGravity){
             case Gravity.LEFT:
                 posX =0;
                 break;
             case Gravity.RIGHT:
                 posX=(int) (mWidth - lineWidth);
                 break;
             case Gravity.CENTER_HORIZONTAL:
                 posX=(int) (mWidth / 2f - lineWidth / 2f);
                 break;
             case Gravity.LEFT|Gravity.TOP:
                 posX =0;
                 posY = 0;
                 break;
             case Gravity.RIGHT|Gravity.TOP:
                 posX=(int) (mWidth - lineWidth);
                 posY = 0;
                 break;
             case Gravity.CENTER_HORIZONTAL|Gravity.TOP:
                 posX=(int) (mWidth / 2f - lineWidth / 2f);
                 posY = 0;
                 break;
             case Gravity.LEFT|Gravity.BOTTOM:
                 posX =0;
                 posY=mHeight-lineHeight;
                 break;
             case Gravity.RIGHT|Gravity.BOTTOM:
                 posX=(int) (mWidth - lineWidth);
                 posY=mHeight-lineHeight;
                 break;
             case Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM:
                 posX=(int) (mWidth / 2f - lineWidth / 2f);
                 posY=mHeight-lineHeight;
                 break;
             case Gravity.LEFT|Gravity.CENTER_VERTICAL:
                 posX =0;
                 posY = (int) (mHeight / 2f - lineHeight / 2f);
                 break;
             case Gravity.RIGHT|Gravity.CENTER_VERTICAL:
                 posX=(int) (mWidth - lineWidth);
                 posY = (int) (mHeight / 2f - lineHeight / 2f);
                 break;
             case Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL:
                 posX=(int) (mWidth / 2f - lineWidth / 2f);
                 posY = (int) (mHeight / 2f - lineHeight / 2f);
                 break;
             case Gravity.TOP:
 				posY = 0;
 				break;
             case Gravity.BOTTOM:
                 posY=mHeight-lineHeight;
                 break;
             case Gravity.CENTER_VERTICAL:
                 posY = (int) (mHeight / 2f - lineHeight / 2f);
                 break;
         }
         mText.layout(posX, posY, posX + lineWidth, posY + lineHeight);
         if (mBitmap == null) {
             mBitmap = Bitmap.createBitmap(Utils.nextPowerOfTwo(mWidgetGroup.getWidth()),
                     Utils.nextPowerOfTwo(mWidgetGroup.getHeight()), Bitmap.Config.ARGB_8888);
         }
         if (mCanvas == null) {
             mCanvas = new Canvas(mBitmap);
         }
         mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
 
         mWidgetGroup.draw(mCanvas);
         surfaceChanged(getDisplayWidth(mContext), getDisplayHeight(mContext));
     }
 
     public Texture getTexture() {
         return mTexture;
     }
 
     public Material getMaterial() {
         return mMaterial;
     }
 
     public void surfaceDestroyed() throws TextureException {
         mMaterial.removeTexture(mTexture);
         mTexture.reset();
     }
 }
