 package com.bencvt.minecraft.buildregion.ui;
 
 import java.util.LinkedHashMap;
 
 import libshapedraw.animation.trident.Timeline;
 import libshapedraw.primitive.Color;
 import libshapedraw.primitive.ReadonlyColor;
 import net.minecraft.client.Minecraft;
 
 import com.bencvt.minecraft.buildregion.lang.LocalizedString;
 
 /**
  * A multi-select Minecraft GUI control.
  * @param <T> an enum type
  * 
  * @author bencvt
  */
 public class GuiSelectEnum<T extends Enum> extends GuiLabeledControl {
     public static final int OPTION_SPACING = 2;
     public static final int PAD_LEFT = 2;
     public static final int PAD_RIGHT = 2;
     public static final int PAD_TOP = 2;
     public static final int PAD_BOTTOM = 1;
     public static final int ANIM_DURATION = 350;
     public static final double ALPHA_OFF = 0.0;
     public static final double ALPHA_ON = 1.0;
 
     public class Option {
         protected T value;
         protected String text;
         protected String textMouseOver;
         protected ReadonlyColor color;
         protected int xBegin; // relative to the control column's x coordinate
         protected int xEnd;   // relative to the control column's x coordinate
         protected Timeline alphaTimeline;
         protected double alpha;
 
         // getter/setter required for property interpolation
         public double getAlpha() { return alpha; }
         public void setAlpha(double alpha) { this.alpha = alpha; }
 
         public boolean isMouseOver(int xMouse, int yMouse) {
             int xOffset = getControlXOffset();
             return xMouse >= xOffset + xBegin && xMouse <= xOffset + xEnd &&
                     yMouse >= yPosition && yMouse <= yPosition + height;
         }
     }
     private final LinkedHashMap<T, Option> options;
     private int labelWidth;
     private boolean allowSetNull;
     private T selectedValue;
 
     public GuiSelectEnum(GuiScreenBase parent, String text, T[] values, ReadonlyColor color) {
         super(parent, text);
         options = new LinkedHashMap<T, Option>();
         int controlWidth = 0;
         for (T value : values) {
             Option option = new Option();
             option.value = value;
             option.text = LocalizedString.translate(value);
             option.textMouseOver = "\u00a7n" + option.text;
             option.color = color;
             option.setAlpha(ALPHA_OFF);
             option.alphaTimeline = null;
             if (controlWidth > 0) {
                 controlWidth += OPTION_SPACING;
             }
             option.xBegin = controlWidth;
             controlWidth += PAD_LEFT + parent.getFontRenderer().getStringWidth(option.text) + PAD_RIGHT;
             option.xEnd = controlWidth;
             options.put(value, option);
         }
         selectedValue = null;
         setControlWidth(controlWidth); // also sets width
         height = PAD_TOP + parent.getFontRenderer().FONT_HEIGHT + PAD_BOTTOM;
     }
 
     private Option getOptionForValue(T value) {
         Option option = options.get(value);
         if (value != null && option == null) {
             throw new IllegalArgumentException("invalid value: " + String.valueOf(value));
         }
         return option;
     }
 
     public boolean isAllowSetNull() {
         return allowSetNull;
     }
     public GuiSelectEnum<T> setAllowSetNull(boolean allowSetNull) {
         this.allowSetNull = allowSetNull;
         return this;
     }
 
     public GuiSelectEnum<T> setOptionColor(T value, ReadonlyColor color) {
         if (value == null) {
             throw new IllegalArgumentException();
         }
         getOptionForValue(value).color = color;
         return this;
     }
 
     public T getSelectedValue() {
         return selectedValue;
     }
     public GuiSelectEnum<T> setSelectedValue(T value, boolean animate) {
         Option prev = getOptionForValue(selectedValue);
         Option cur = getOptionForValue(value);
         selectedValue = value;
 
         if (prev == cur) {
             return this;
         }
 
         if (prev != null) {
             // Fade out previous selection
             if (prev.alphaTimeline != null && !prev.alphaTimeline.isDone()) {
                 prev.alphaTimeline.abort();
                 prev.alphaTimeline = null;
             }
             if (animate) {
                 prev.alphaTimeline = new Timeline(prev);
                 prev.alphaTimeline.addPropertyToInterpolate("alpha", prev.getAlpha(), ALPHA_OFF);
                 prev.alphaTimeline.setDuration(ANIM_DURATION);
                 prev.alphaTimeline.play();
             } else {
                 prev.setAlpha(ALPHA_OFF);
             }
         }
 
         if (cur != null) {
             // Fade in current selection
             if (cur.alphaTimeline != null && !cur.alphaTimeline.isDone()) {
                 cur.alphaTimeline.abort();
                 cur.alphaTimeline = null;
             }
             if (animate) {
                 cur.alphaTimeline = new Timeline(cur);
                 cur.alphaTimeline.addPropertyToInterpolate("alpha", cur.getAlpha(), ALPHA_ON);
                 cur.alphaTimeline.setDuration(ANIM_DURATION);
                 cur.alphaTimeline.play();
             } else {
                 cur.setAlpha(ALPHA_ON);
             }
         }
 
         return this;
     }
 
     @Override
     protected int getLabelYOffset() {
         return PAD_TOP;
     }
 
     @Override
     protected void drawControl(int xMouse, int yMouse) {
         int xOffset = getControlXOffset();
         for (Option option : options.values()) {
             // Draw background rectangle.
             tempColor.set(option.color).scaleAlpha(option.getAlpha());
             drawRect(
                     xOffset + option.xBegin,  yPosition,
                     xOffset + option.xEnd,    yPosition + height,
                     tempColor.getARGB());
 
             // Draw foreground text.
             if (option.isMouseOver(xMouse, yMouse)) {
                 parent.getFontRenderer().drawString(option.textMouseOver,
                         xOffset + option.xBegin + PAD_LEFT, yPosition + PAD_TOP,
                         CONTROL_MOUSEOVER_ARGB);
             } else {
                 parent.getFontRenderer().drawString(option.text,
                         xOffset + option.xBegin + PAD_LEFT, yPosition + PAD_TOP,
                         CONTROL_ENABLED_ARGB);
             }
         }
     }
     /** so we don't create a bunch of temporary objects when rendering */
     private static final Color tempColor = Color.BLACK.copy();
 
     @Override
     public boolean mousePressed(Minecraft minecraft, int xMouse, int yMouse) {
         if (!super.mousePressed(minecraft, xMouse, yMouse)) {
             return false;
         }
         // TODO: respond to mouse wheel
         int xOffset = getControlXOffset();
         for (Option option : options.values()) {
             if (xMouse >= option.xBegin + xOffset && xMouse <= option.xEnd + xOffset) {
                 if (allowSetNull && selectedValue == option.value) {
                     setSelectedValue(null, true);
                 } else {
                     setSelectedValue(option.value, true);
                 }
                 return true;
             }
         }
         // clicked in the label or the empty space between options
         return false;
     }
 }
