 /*
  * ScreenUtil.java
  *
  * Created on August 24, 2005, 4:22 AM
  *
  * To change this template, choose Tools | Options and locate the template under
  * the Source Creation and Management node. Right-click the template and choose
  * Open. You can then make changes to the template in the Source Editor.
 
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.thin.base.screen.landf;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dialog;
 import java.awt.Font;
 import java.awt.Frame;
 import java.util.Map;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.border.LineBorder;
 import javax.swing.plaf.ColorUIResource;
 import javax.swing.plaf.FontUIResource;
 import javax.swing.plaf.metal.MetalLookAndFeel;
 import javax.swing.plaf.metal.MetalTheme;
 import javax.swing.plaf.metal.OceanTheme;
 
 import org.jbundle.model.PropertyOwner;
 import org.jbundle.thin.base.db.Params;
 import org.jbundle.thin.base.screen.BaseApplet;
 import org.jbundle.thin.base.screen.landf.theme.CustomTheme;
 import org.jbundle.util.osgi.finder.ClassServiceUtility;
 
 /**
  *
  * @author don
  */
 public class ScreenUtil 
 {
     
     public static final String LOOK_AND_FEEL = "lookandfeel";
     public static final String THEME = "theme";
     public static final String FONT_NAME = "font.fontname";
     public static final String FONT_SIZE = "font.size";
     public static final String FONT_STYLE = "font.style";
     public static final String CONTROL_COLOR = "color.control";
     public static final String TEXT_COLOR = "color.text";
     public static final String BACKGROUND_COLOR = Params.BACKGROUNDCOLOR;
     
     public static final String METAL_NAME = "Metal";
     public static final String SYSTEM = "system";
     public static final String DEFAULT = "default";
 
     /** Creates a new instance of ScreenUtil */
     public ScreenUtil() 
     {
         super();
     }
     /**
      * Set the look and feel.
      */
     public static void updateLookAndFeel(Container container, PropertyOwner propertyOwner, Map<String,Object> properties)
     {
         String lookAndFeelClassName = ScreenUtil.getPropery(ScreenUtil.LOOK_AND_FEEL, propertyOwner, properties, null);
         if (lookAndFeelClassName == null)
             lookAndFeelClassName = ScreenUtil.DEFAULT;
         if (ScreenUtil.DEFAULT.equalsIgnoreCase(lookAndFeelClassName))
         {
 //            lookAndFeelClassName = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
             lookAndFeelClassName = UIManager.getCrossPlatformLookAndFeelClassName();
         }
         if (ScreenUtil.SYSTEM.equalsIgnoreCase(lookAndFeelClassName))
             lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
 
         String themeClassName = ScreenUtil.getPropery(ScreenUtil.THEME, propertyOwner, properties, null);
         MetalTheme theme = null;
 
         FontUIResource font = ScreenUtil.getFont(propertyOwner, properties);
        if ((themeClassName == null) || (themeClassName.equalsIgnoreCase(ScreenUtil.DEFAULT)))
            if (font == null)
        {
            int MINIMUM_DEFAULT_FONT_SIZE = 16;
            int DEFAULT_FONT_SIZE = 20;
            Object defaultFont = UIManager.getDefaults().get("Panel.font");
            if (defaultFont instanceof Font)
                if (((Font)defaultFont).getSize() < MINIMUM_DEFAULT_FONT_SIZE)
                    font = new FontUIResource(((Font)defaultFont).deriveFont(((Font)defaultFont).getStyle(), DEFAULT_FONT_SIZE));
        }
         ColorUIResource colorText = ScreenUtil.getColor(ScreenUtil.TEXT_COLOR, propertyOwner, properties);
         ColorUIResource colorControl = ScreenUtil.getColor(ScreenUtil.CONTROL_COLOR, propertyOwner, properties);
         ColorUIResource colorBackground = ScreenUtil.getColor(ScreenUtil.BACKGROUND_COLOR, propertyOwner, properties);
         
         if ((font != null) || (colorControl != null) || (colorText != null))
         {
             if (!(theme instanceof CustomTheme))
                 theme = new CustomTheme();
             if (font != null)
                 ((CustomTheme)theme).setDefaultFont(font);
             if (colorControl != null)
                 ((CustomTheme)theme).setWhite(colorControl);
             if (colorText != null)
                 ((CustomTheme)theme).setBlack(colorText);
             if (colorBackground != null)
             {
                 ((CustomTheme)theme).setSecondaryColor(colorBackground);
                 ((CustomTheme)theme).setPrimaryColor(colorBackground);
             }
         }
         else
         {
             if ((themeClassName == null) || (themeClassName.equalsIgnoreCase(ScreenUtil.DEFAULT)))
                 theme = null;   //? createDefaultTheme();
             else
             	theme = (MetalTheme)ClassServiceUtility.getClassService().makeObjectFromClassName(themeClassName);
         }
         
         if (MetalLookAndFeel.class.getName().equals(lookAndFeelClassName))
         {
             if (theme == null)
                 theme = new OceanTheme();
             MetalLookAndFeel.setCurrentTheme(theme);
         }
         
         try   {
             UIManager.setLookAndFeel(lookAndFeelClassName);
         } catch (Exception ex)  {
             ex.printStackTrace();
         }
 
         SwingUtilities.updateComponentTreeUI(container);
     }
     /**
      *  Create the font saved in this key; null = Not found.
      * (Utility method).
      * Font is saved in three properties (font.fontname, font.size, font.style).
      * @return The registered font.
      */
     public static FontUIResource getFont(PropertyOwner propertyOwner, Map<String,Object> properties)
     {
         String strFontName = ScreenUtil.getPropery(ScreenUtil.FONT_NAME, propertyOwner, properties, null);
         String strFontSize = ScreenUtil.getPropery(ScreenUtil.FONT_SIZE, propertyOwner, properties, null);
         String strFontStyle = ScreenUtil.getPropery(ScreenUtil.FONT_STYLE, propertyOwner, properties, null);
 
         if ((strFontName == null) || (strFontName.length() == 0))
            return null;
        int iSize = 12;
         if ((strFontSize != null) && (strFontSize.length() > 0))
             iSize = Integer.parseInt(strFontSize);      
         int iStyle = Font.PLAIN;
         if ((strFontStyle != null) && (strFontStyle.length() > 0))
             iStyle = Integer.parseInt(strFontStyle);
         return new FontUIResource(strFontName, iStyle, iSize);
     }
     /**
      * Encode and set the font info.
      * (Utility method).
      * Font is saved in three properties (font.fontname, font.size, font.style).
      * @param The registered font.
      */
     public static void setFont(Font font, PropertyOwner propertyOwner, Map<String,Object> properties)
     {
         if (font != null)
         {
             ScreenUtil.setProperty(ScreenUtil.FONT_SIZE, Integer.toString(font.getSize()), propertyOwner,  properties);
             ScreenUtil.setProperty(ScreenUtil.FONT_STYLE, Integer.toString(font.getStyle()), propertyOwner,  properties);
             ScreenUtil.setProperty(ScreenUtil.FONT_NAME, font.getName(), propertyOwner,  properties);
         }
         else
         {
             ScreenUtil.setProperty(ScreenUtil.FONT_SIZE, null, propertyOwner,  properties);
             ScreenUtil.setProperty(ScreenUtil.FONT_STYLE, null, propertyOwner,  properties);
             ScreenUtil.setProperty(ScreenUtil.FONT_NAME, null, propertyOwner,  properties);
         }
     }
     /**
      *  Get this color.
      * (Utility method).
      * @return The registered color for this property key.
      */
     public static ColorUIResource getColor(String strProperty, PropertyOwner propertyOwner, Map<String,Object> properties)
     {
         String strColor = ScreenUtil.getPropery(strProperty, propertyOwner, properties, null);
         if (strColor != null)
             return new ColorUIResource(BaseApplet.nameToColor(strColor));
         return null;    // Error on decode or no such color
     }
     /**
      * Set this property to this color.
      * (Utility method).
      * @param strProperty The key to save this color as.
      * @param color The registered color for this property key.
      */
     public static void setColor(String strProperty, ColorUIResource color, PropertyOwner propertyOwner, Map<String,Object> properties)
     {
         if (color != null)
             ScreenUtil.setProperty(strProperty, "#" + Integer.toHexString(color.getRGB() & 0xFFFFFF), propertyOwner, properties);
         else
             ScreenUtil.setProperty(strProperty, null, propertyOwner, properties);
     }
     /**
      * A utility to get the property from the propertyowner or the property.
      */
     public static String getPropery(String key, PropertyOwner propertyOwner, Map<String,Object> properties, String defaultValue)
     {
         String returnValue = null;
         if (propertyOwner != null)
             returnValue = propertyOwner.getProperty(key);
         if (properties != null) if (returnValue == null)
             returnValue = (String)properties.get(key);
         if (returnValue == null)
             returnValue = defaultValue;
         return returnValue;
     }
     /**
      * A utility to set the property from the propertyowner or the property.
      */
     public static void setProperty(String key, String value, PropertyOwner propertyOwner, Map<String,Object> properties)
     {
         if (propertyOwner != null)
         {
             propertyOwner.setProperty(key, value);
         }
         if (properties != null)
         {
             if (value != null)
                 properties.put(key, value);
             else
                 properties.remove(key);
         }
     }
     /**
      * Center this dialog in the frame.
      */
     public static void centerDialogInFrame(Dialog dialog, Frame frame)
     {
         dialog.setLocation(frame.getX() + (frame.getWidth() - dialog.getWidth()) / 2, frame.getY() + (frame.getHeight() - dialog.getHeight()) / 2);
     }
     /**
      * Get the frame for this component.
      * @param component The component to get the frame for.
      * @return The frame (or null).
      */
     public static Frame getFrame(Component component)
     {
         while (component != null)
         {
             if (component instanceof Frame)
                 return (Frame)component;
             component = component.getParent();
         }
         return null;
     }
     /**
      * Fake disable a control.
      */
     public static void setEnabled(Component component, boolean bEnabled)
     {
         if (component instanceof JPanel)
         {
             for (int i = 0; i < ((JPanel)component).getComponentCount(); i++)
             {
                 JComponent componentSub = (JComponent)((JPanel)component).getComponent(i);
                 ScreenUtil.setEnabled(componentSub, bEnabled);
             }
         }
         else if (component instanceof JScrollPane)
         {
             JComponent componentSub = (JComponent)((JScrollPane)component).getViewport().getView();
             ScreenUtil.setEnabled(componentSub, bEnabled);
         }
         else
             component.setEnabled(bEnabled);
     }
     public static final Border GRAY_BORDER = new LineBorder(Color.LIGHT_GRAY);
     public static final Border BLACK_BORDER = new LineBorder(Color.BLACK);
 }
