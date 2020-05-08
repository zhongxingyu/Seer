 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.preferences;
 
 import com.zapta.apps.maniana.R;
 
 /**
  * Widget predefined themes.
  * 
  * @author Tal Dayan
  */
 public class WidgetTheme extends Thumbnail {
 
     public static final WidgetTheme[] WIDGET_THEMES = {
             // Default
             new WidgetTheme("Paper Trail", R.drawable.widget_theme1_preview,
                     PreferenceConstants.DEFAULT_WIDGET_BACKGROUND_TYPE,
                     PreferenceConstants.DEFAULT_WIDGET_BACKGROUND_COLOR,
                     PreferenceConstants.DEFAULT_WIDGET_ITEM_FONT_SIZE,
                     PreferenceConstants.DEFAULT_WIDGET_TEXT_COLOR,
                     PreferenceConstants.DEFAULT_WIDGET_SHOW_TOOLBAR,
                     PreferenceConstants.DEFAULT_WIDGET_SINGLE_LINE),
             new WidgetTheme("Crystal Clear", R.drawable.widget_theme2_preview, WidgetBackgroundType.SOLID,
                    0x44000000, WidgetItemFontSize.MEDIUM, 0xffffff00, true, false),
             new WidgetTheme("Think Big", R.drawable.widget_theme3_preview, WidgetBackgroundType.SOLID,
                     0xff000000, WidgetItemFontSize.LARGE, 0xff00ff00, true, true),
             new WidgetTheme("Fine Print", R.drawable.widget_theme4_preview, WidgetBackgroundType.SOLID,
                     0x600000ff, WidgetItemFontSize.SMALL, 0xffffff00, false, true) };
 
     public final WidgetBackgroundType backgroundType;
     public final int backgroundColor;
     public final WidgetItemFontSize fontSize;
     public final int textColor;
     public final boolean showToolbar;
     public final boolean singleLine;
 
     public WidgetTheme(String name, int drawableId, WidgetBackgroundType backgroundType, int backgroundColor,
             WidgetItemFontSize fontSize, int textColor, boolean showToolbar, boolean singleLine) {
         super(name, drawableId);
         this.backgroundType = backgroundType;
         this.backgroundColor = backgroundColor;
         this.fontSize = fontSize;
         this.textColor = textColor;
         this.showToolbar = showToolbar;
         this.singleLine = singleLine;
     }
 }
