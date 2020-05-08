 /*
  * Copyright (c) 2006-2014 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.components.addonbrowser;
 
 import javax.swing.ButtonModel;
 import javax.swing.JTextField;
 import javax.swing.RowFilter;
 import javax.swing.table.DefaultTableModel;
 
 /**
  * Addon filter.
  */
 public class AddonFilter extends RowFilter<DefaultTableModel, Integer> {
 
     private final ButtonModel verifiedBox;
     private final ButtonModel unverifiedBox;
     private final ButtonModel installedBox;
     private final ButtonModel notinstalledBox;
     private final ButtonModel pluginsBox;
     private final ButtonModel themesBox;
     private final ButtonModel actionsBox;
     private final JTextField searchBox;
 
     /**
      * Creates a new addon filter.
      *
      * @param verifiedBox     Verified checkbox
      * @param unverifiedBox   Unverified checkbox
      * @param installedBox    Installed checkbox
      * @param notinstalledBox Not installed checkbox
      * @param pluginsBox      Plugins checkbox
      * @param themesBox       Themes checkbox
      * @param actionsBox      Actions checkbox
      * @param searchBox       Search field
      */
     public AddonFilter(final ButtonModel verifiedBox,
             final ButtonModel unverifiedBox, final ButtonModel installedBox,
             final ButtonModel notinstalledBox, final ButtonModel pluginsBox,
             final ButtonModel themesBox, final ButtonModel actionsBox,
             final JTextField searchBox) {
         super();
 
         this.verifiedBox = verifiedBox;
         this.unverifiedBox = unverifiedBox;
         this.installedBox = installedBox;
         this.notinstalledBox = notinstalledBox;
         this.pluginsBox = pluginsBox;
         this.themesBox = themesBox;
         this.actionsBox = actionsBox;
         this.searchBox = searchBox;
     }
 
     @Override
     public boolean include(
             final Entry<? extends DefaultTableModel, ? extends Integer> entry) {
         final AddonInfo info = ((AddonInfoLabel) entry.getModel().getValueAt(
                 entry.getIdentifier(), 0)).getAddonInfo();
         return !((!verifiedBox.isSelected() && info.isVerified())
                 || (!unverifiedBox.isSelected() && !info.isVerified())
                 || (!installedBox.isSelected() && info.isInstalled())
                 || (!notinstalledBox.isSelected() && !info.isInstalled())
                || (!pluginsBox.isSelected() && info.getType()
                == AddonType.TYPE_PLUGIN)
                || (!themesBox.isSelected() && info.getType()
                == AddonType.TYPE_THEME)
                || (!actionsBox.isSelected() && info.getType()
                == AddonType.TYPE_ACTION_PACK)
                 || (!searchBox.getText().isEmpty()
                 && !info.matches(searchBox.getText())));
     }
 
 }
