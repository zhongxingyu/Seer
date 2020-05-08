 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings;
 
 //import javax.swing.*;
 
 import org.wings.plaf.*;
 import org.wings.io.Device;
 import org.wings.externalizer.ExternalizeManager;
 
 /**
  * A display area for a short text string or an image, or both.
  * You can specify where in the label's display area  the label's contents
  * are aligned by setting the vertical and horizontal alignment.
  * You can also specify the position of the text relative to the image.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SLabel
     extends SComponent
     implements SConstants, ClickableRenderComponent
 {
     private static final String cgClassID = "LabelCG";
 
     /**
      * The text to be displayed
      */
     protected String text;
 
     /**
      * The icon to be displayed
      */
     protected SIcon icon = null;
 
     /**
      * TODO: documentation
      */
     protected SIcon disabledIcon = null;
 
     private int verticalTextPosition = CENTER;
     private int horizontalTextPosition = RIGHT;
     private int iconTextGap = 1;
     private boolean noBreak = false;
     private boolean imageAbsBottom = false;
 
     private boolean escapeSpecialChars = true;
 
     /**
      * Creates a new <code>SLabel</code> instance with the specified text
      * (left alligned) and no icon.
      *
      * @param text The text to be displayed by the label.
      */
     public SLabel(String text) {
         this(text, null, LEFT);
     }
 
     /**
      * Creates a new <code>SLabel</code> instance with no text and no icon.
      */
     public SLabel() {
         this((String)null);
     }
 
     /**
      * Creates a new <code>SLabel</code> instance with the specified icon
      * (left alligned) and no text.
      *
      * @param icon The image to be displayed by the label.
      */
     public SLabel(SIcon icon) {
         this(icon, LEFT);
     }
 
     /**
      * Creates a new <code>SLabel</code> instance with the specified icon
      * (alligned as specified) and no text.
      *
      * @param icon The image to be displayed by the label.
      * @param horizontalAlignment One of the following constants defined in
      *        <code>SConstants</code>:
      *        <code>LEFT</code>, <code>CENTER</code>, <code>RIGHT</code>.
      * @see SConstants
      */
     public SLabel(SIcon icon, int horizontalAlignment) {
         this(null, icon, horizontalAlignment);
     }
 
     /**
      * Creates a new <code>SLabel</code> instance with the specified icon
      * and the specified text (left alligned).
      *
      * @param text The text to be displayed by the label.
      * @param icon The image to be displayed by the label.
      */
     public SLabel(String text, SIcon icon) {
         setText(text);
         setIcon(icon);
         setHorizontalAlignment(LEFT);
     }
 
     /**
      * Creates a new <code>SLabel</code> instance with the specified icon
      * and the specified text (alligned as specified).
      *
      * @param text The text to be displayed by the label.
      * @param icon The image to be displayed by the label.
      * @param horizontalAlignment One of the following constants defined in
      *        <code>SConstants</code>:
      *        <code>LEFT</code>, <code>CENTER</code>, <code>RIGHT</code>.
      * @see SConstants
      */
     public SLabel(String text, SIcon icon, int horizontalAlignment) {
         setText(text);
         setIcon(icon);
         setHorizontalAlignment(horizontalAlignment);
     }
 
     /**
      * Creates a new <code>SLabel</code> instance with the specified text
      * (alligned as specified) and no icon.
      *
      * @param text The text to be displayed by the label.
      * @param horizontalAlignment One of the following constants defined in
      *        <code>SConstants</code>:
      *        <code>LEFT</code>, <code>CENTER</code>, <code>RIGHT</code>.
      * @see SConstants
      */
     public SLabel(String text, int horizontalAlignment) {
         this(text, null, horizontalAlignment);
     }
 
     /**
      *
      * @param t
      */
     public void setImageAbsBottom(boolean t) {
         imageAbsBottom = t;
     }
 
     /**
      *
      * @param t
      */
     public boolean isImageAbsBottom() {
         return imageAbsBottom;
     }
 
 
     /**
      * Render the Text in this Label non-breakable. Usually, this means
      * that the content ist enclosed in a &lt;nobr&gt;&lt;/nobr&gt;
      * area.
      *
      * @param breakable flag to indicate, whether the text-output should
      *        be rendered non-breakable.
      */
     public void setNoBreak(boolean breakable) {
        noBreak = breakable;
     }
 
     /**
      * Return the state of the <code>noBreak</code> flag.
      *
      * @return a flag that states, if the output should be rendered
      *         non-breakbable
      */
     public boolean isNoBreak() { return noBreak; }
 
     /**
      * Returns the horizontal position of the lable's text
      *
      * @return the position
      * @see SConstants
      * @see #setHorizontalTextPosition
      */
     public int getHorizontalTextPosition() {
         return horizontalTextPosition;
     }
 
     /**
      * Sets the horizontal position of the lable's text, relative to its icon.
      * <p>
      * The default value of this property is CENTER.
      *
      * @param textPosition One of the following constants defined in
      *        <code>SConstants</code>:
      *        <code>LEFT</code>, <code>CENTER</code>, <code>RIGHT</code>.
      */
     public void setHorizontalTextPosition(int textPosition) {
         horizontalTextPosition = textPosition;
     }
 
     /**
      * Sets the vertical position of the lable's text, relative to its icon.
      * <p>
      * The default value of this property is CENTER.
      *
      * @param textPosition One of the following constants defined in
      *        <code>SConstants</code>:
      *        <code>TOP</code>, <code>CENTER</code>, <code>BOTTOM</code>.
      */
     public void setVerticalTextPosition(int textPosition) {
         verticalTextPosition = textPosition;
     }
 
     /**
      * Returns the vertical position of the label's text
      *
      * @return the position
      * @see SConstants
      * @see #setVerticalTextPosition
      */
     public int getVerticalTextPosition() {
         return verticalTextPosition;
     }
 
     /**
      * TODO: documentation
      *
      * @param gap
      */
     public void setIconTextGap(int gap) {
         iconTextGap = gap;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public int getIconTextGap() {
         return iconTextGap;
     }
 
     /**
      * TODO: documentation
      *
      * @param i
      */
     public void setIcon(SIcon i) {
         reloadIfChange(ReloadManager.RELOAD_CODE, icon, i);
         icon = i;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public SIcon getIcon() {
         return icon;
     }
 
     /**
      * TODO: documentation
      *
      * @param i
      */
     public void setDisabledIcon(SIcon i) {
         reloadIfChange(ReloadManager.RELOAD_CODE, disabledIcon, i);
         disabledIcon = i;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public SIcon getDisabledIcon() {
         return disabledIcon;
     }
 
     /**
      * Returns the text of the label
      *
      * @return
      */
     public String getText() {
         return text;
     }
 
     /**
      * Sets the text of the label. If the value of text is null or an empty
      * string, nothing is displayed.
      *
      * @param t The new text
      */
     public void setText(String t) {
         reloadIfChange(ReloadManager.RELOAD_CODE, text, t);
         text = t;
     }
 
     /**
      * returns the setting of the escape character property
      *
      * @see #setEscapeSpecialChars(boolean)
      * @return 'true', if characters are quoted, 'false' if they
      *         are passed raw to the backend Device.
      * @deprecated please use the html tag instead
      */
     public boolean isEscapeSpecialChars() {
 	return escapeSpecialChars;
     }
 
     /**
      * By default, all special characters are quoted in the
      * output. This means for *ML like languages, that special 
      * characters like &lt; &gt; or &amp; are replaced by their
      * appropriate entities. Note, that the decision, what is
      * quoted is done by the CG. If you set this to 'false', then
      * they are not quoted - you might use this, if you want to
      * sneak in HTML (XML, WML..PDF) formatting information in the
      * raw String. Note, that in that case, your application might
      * not be portable accross different backend CG's (think of
      * WML).
      *
      * @param escape boolean 'true', if characters are to be escaped
      *               (the default), or 'false' if any character you
      *               write here is passed 'raw' to the Device.
      * @deprecated  please use the <html> tag instead
      */
     public void setEscapeSpecialChars(boolean escape) {
 	escapeSpecialChars = escape;
     }
 
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(LabelCG cg) {
         super.setCG(cg);
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
