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
 
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 
 import javax.swing.Icon;
 import javax.swing.SingleSelectionModel;
 import javax.swing.DefaultSingleSelectionModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.wings.plaf.*;
 import org.wings.style.*;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class STabbedPane
     extends SContainer
     implements SConstants, ActionListener
 {
     /**
      * @see #getCGClassID
      */
     private static final String cgClassID = "TabbedPaneCG";
 
     /**
      * Where the tabs are placed.
      * @see #setTabPlacement
      */
     protected int tabPlacement = TOP;
 
     /** The default selection model */
     protected SingleSelectionModel model = new DefaultSingleSelectionModel();
 
     ArrayList pages = new ArrayList(2);
 
     SCardLayout card = new SCardLayout();
     SContainer contents = new SContainer(card);
 
     protected SButtonGroup group;
 
     /**
      * TODO: documentation
      */
     protected Color buttonOrigBackground;
 
     /** the maximum tabs per line */
     protected int maxTabsPerLine = -1;
 
     SContainer buttons = new SContainer();
 
     ArrayList changeListener = new ArrayList(2);
 
     /**
      * TODO: documentation
      */
     protected Color selectionForeground;
 
     /**
      * TODO: documentation
      */
     protected Color selectionBackground;
 
     /**
      * TODO: documentation
      */
     protected Style selectionStyle;
 
     /**
      * Creates a new empty Tabbed Pane with the tabs at the top.
      * @see #addTab
      */
     public STabbedPane() {
         this(TOP);
     }
 
     /**
      * Creates an empty TabbedPane with the specified tab placement
      * of either: TOP, BOTTOM, LEFT, or RIGHT.
      * @param tabPlacement the placement for the tabs relative to the content
      * @see #addTab
      */
     public STabbedPane(int tabPlacement) {
         super();
 
         SBorderLayout layout = new SBorderLayout();
         layout.setBorder(1);
         setLayout(layout);
 
         setTabPlacement(tabPlacement);
 
         super.add(contents, SBorderLayout.CENTER);
 
         group = new SButtonGroup();
         group.addActionListener(this);
     }
 
     /**
      * TODO: documentation
      *
      * @param c
      */
     public void setSelectionForeground(Color c) {
         selectionForeground = c;
     }
 
     /**
      * TODO: documentation
      *
      * @param c
      */
     public void setSelectionBackground(Color c) {
         selectionBackground = c;
     }
 
     /**
      * TODO: documentation
      *
      * @param c
      */
     public void setSelectionStyle(Style s) {
         selectionStyle = s;
     }
 
     /**
      * TODO: documentation
      *
      * @return selection style
      */
     public Style getSelectionStyle() { return selectionStyle; }
 
     /**
      * TODO: documentation
      *
      * @param cl
      */
     public void addChangeListener(ChangeListener cl) {
         if ( !changeListener.contains(cl) )
             changeListener.add(cl);
     }
 
     /**
      * TODO: documentation
      *
      * @param cl
      */
     public void removeChangeListener(ChangeListener cl) {
         changeListener.remove(cl);
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void fireStateChanged() {
         ChangeEvent ce = new ChangeEvent(this);
         for ( int i=0; i<changeListener.size(); i++ )
             ((ChangeListener)changeListener.get(i)).stateChanged(ce);
     }
 
     /**
      * Returns the placement of the tabs for this tabbedpane.
      * @see #setTabPlacement
      */
     public int getTabPlacement() {
         return tabPlacement;
     }
 
     /**
      * Sets the tab placement for this tabbedpane.
      * Possible values are:<ul>
      * <li>SConstants.TOP
      * <li>SConstants.BOTTOM
      * <li>SConstants.LEFT
      * <li>SConstants.RIGHT
      * </ul>
      * The default value, if not set, is TOP.
      *
      * @param tabPlacement the placement for the tabs relative to the content
      *
      */
     public void setTabPlacement(int tabPlacement) {
         if ( tabPlacement != TOP && tabPlacement != LEFT &&
              tabPlacement != BOTTOM && tabPlacement != RIGHT ) {
             throw new IllegalArgumentException("illegal tab placement: must be TOP, BOTTOM, LEFT, or RIGHT");
         }
 
         this.tabPlacement = tabPlacement;
         super.remove(buttons);
 
         switch ( tabPlacement ) {
         case TOP:
             super.add(buttons, SBorderLayout.NORTH);
             break;
 
         case BOTTOM:
             super.add(buttons, SBorderLayout.SOUTH);
             break;
 
         case LEFT:
             super.add(buttons, SBorderLayout.WEST);
             break;
 
         case RIGHT:
             super.add(buttons, SBorderLayout.EAST);
             break;
         }
     }
 
     /**
      * Returns the model associated with this tabbedpane.
      *
      * @see #setModel
      */
     public SingleSelectionModel getModel() {
         return model;
     }
 
     /**
      * Sets the model to be used with this tabbedpane.
      * @param model the model to be used
      *
      * @see #getModel
      */
     public void setModel(SingleSelectionModel model) {
         this.model = model;
     }
 
     /**
      * Returns the currently selected index for this tabbedpane.
      * Returns -1 if there is no currently selected tab.
      *
      * @return the index of the selected tab
      * @see #setSelectedIndex
      */
     public int getSelectedIndex() {
         return model.getSelectedIndex();
     }
 
     /**
      * Sets the selected index for this tabbedpane.
      *
      * @see #getSelectedIndex
      * @see SingleSelectionModel#setSelectedIndex
      * @beaninfo
      *   preferred: true
      * description: The tabbedpane's selected tab index.
      */
     public void setSelectedIndex(int index) {
         model.setSelectedIndex(index);
 
         Page p = getPageAt(index);
         if (p != null) {
             p.button.setSelected(true);
             card.show(p.component);
         }
     }
 
     /**
      * Returns the currently selected component for this tabbedpane.
      * Returns null if there is no currently selected tab.
      *
      * @return the component corresponding to the selected tab
      * @see #setSelectedComponent
      */
     public SComponent getSelectedComponent() {
         int index = getSelectedIndex();
         if ( index == -1 ) {
             return null;
         }
         return getTabAt(index);
     }
 
     /**
      * Sets the selected component for this tabbedpane.  This
      * will automatically set the selectedIndex to the index
      * corresponding to the specified component.
      *
      * @see #getSelectedComponent
      * @beaninfo
      *   preferred: true
      * description: The tabbedpane's selected component.
      */
     public void setSelectedComponent(SComponent c) {
         int index = indexOfComponent(c);
         if ( index != -1 ) {
             setSelectedIndex(index);
         }
         else {
             throw new IllegalArgumentException("component not found in tabbed pane");
         }
     }
 
     /**
      * Returns the index of the tab for the specified component.
      * Returns -1 if there is no tab for this component.
      * @param component the component for the tab
      */
     public int indexOfComponent(SComponent component) {
         for ( int i = 0; i < getTabCount(); i++ ) {
             if ( contents.getComponentAt(i).equals(component) ) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Returns the number of tabs in this tabbedpane.
      *
      * @return an int specifying the number of tabbed pages
      */
     public int getTabCount() {
         return pages.size();
     }
 
     /**
      * Inserts a <i>component</i>, at <i>index</i>, represented by a
      * <i>title</i> and/or <i>icon</i>, either of which may be null.
      * Uses java.util.ArrayList internally, see insertElementAt()
      * for details of insertion conventions.
      * @param title the title to be displayed in this tab
      * @param icon the icon to be displayed in this tab
      * @param component The component to be displayed when this tab is clicked.
      * @param tip the tooltip to be displayed for this tab
      * @param index the position to insert this new tab
      *
      * @see #addTab
      * @see #removeTabAt
      */
     public void insertTab(String title, Icon icon,
                           SComponent component, String tip,
                           int index) {
 
         Icon disabledIcon = null;
 
         // if (icon != null && icon instanceof ImageIcon) {
         //    disabledIcon = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon)icon).getImage()));
         // }
 
         String t = (title != null) ? title : "";
 
         Page p = new Page(this, t, icon, disabledIcon, component, tip);
         pages.add(index, p);
 
         contents.addComponent(p.component, p.component.getUnifiedIdString());
 
         if ( pages.size() == 1 ) {
             setSelectedIndex(0);
         }
 
         updateButtons();
     }
 
     /**
      * Adds a <i>component</i> and <i>tip</i> represented by a <i>title</i>
      * and/or <i>icon</i>, either of which can be null.
      * Cover method for insertTab().
      * @param title the title to be displayed in this tab
      * @param icon the icon to be displayed in this tab
      * @param component The component to be displayed when this tab is clicked.
      * @param tip the tooltip to be displayed for this tab
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public void addTab(String title, Icon icon, SComponent component, String tip) {
         insertTab(title, icon, component, tip, pages.size());
     }
 
     /**
      * Adds a <i>component</i> represented by a <i>title</i> and/or <i>icon</i>,
      * either of which can be null.
      * Cover method for insertTab().
      * @param title the title to be displayed in this tab
      * @param icon the icon to be displayed in this tab
      * @param component The component to be displayed when this tab is clicked.
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public void addTab(String title, Icon icon, SComponent component) {
         insertTab(title, icon, component, null, pages.size());
     }
 
     /**
      * Adds a <i>component</i> represented by a <i>title</i> and no icon.
      * Cover method for insertTab().
      * @param title the title to be displayed in this tab
      * @param component The component to be displayed when this tab is clicked.
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public void addTab(String title, SComponent component) {
         insertTab(title, null, component, null, pages.size());
     }
 
     /**
      * Adds a <i>component</i> with a tab title defaulting to
      * the name of the component.
      * Cover method for insertTab().
      * @param component The component to be displayed when this tab is clicked.
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public SComponent add(SComponent component) {
         addTab(component.getName(), component);
         return component;
     }
 
     /**
      * Adds a <i>component</i> with the specified tab title.
      * Cover method for insertTab().
      * @param title the title to be displayed in this tab
      * @param component The component to be displayed when this tab is clicked.
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public SComponent add(String title, SComponent component) {
         addTab(title, component);
         return component;
     }
 
     /**
      * Adds a <i>component</i> at the specified tab index with a tab
      * title defaulting to the name of the component.
      * Cover method for insertTab().
      * @param component The component to be displayed when this tab is clicked.
      * @param index the position to insert this new tab
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public SComponent add(SComponent component, int index) {
         insertTab(component.getName(), null, component, null, index);
         return component;
     }
 
     /**
      * Adds a <i>component</i> to the tabbed pane.  If constraints
      * is a String or an Icon, it will be used for the tab title,
      * otherwise the component's name will be used as the tab title.
      * Cover method for insertTab().
      * @param component The component to be displayed when this tab is clicked.
      * @constraints the object to be displayed in the tab
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public void add(SComponent component, Object constraints) {
         if (constraints instanceof String) {
             addTab((String)constraints, component);
         }
         else if (constraints instanceof Icon) {
             addTab(null, (Icon)constraints, component);
         }
         else {
             add(component);
         }
     }
 
     /**
      * Adds a <i>component</i> at the specified tab index.  If constraints
      * is a String or an Icon, it will be used for the tab title,
      * otherwise the component's name will be used as the tab title.
      * Cover method for insertTab().
      * @param component The component to be displayed when this tab is clicked.
      * @constraints the object to be displayed in the tab
      * @param index the position to insert this new tab
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public void add(SComponent component, Object constraints, int index) {
         Icon icon = constraints instanceof Icon? (Icon)constraints : null;
         String title = constraints instanceof String? (String)constraints : null;
         insertTab(title, icon, component, null, index);
     }
 
     /**
      * Removes the tab at <i>index</i>.
      * After the component associated with <i>index</i> is removed,
      * its visibility is reset to true to ensure it will be visible
      * if added to other containers.
      * @param index the index of the tab to be removed
      *
      * @see #addTab
      * @see #insertTab
      */
     public void removeTabAt(int index) {
         // If we are removing the currently selected tab AND
         // it happens to be the last tab in the bunch, then
         // select the previous tab
         int tabCount = getTabCount();
         int selected = getSelectedIndex();
         if ( selected >= (tabCount - 1) ) {
             setSelectedIndex(selected - 1);
         }
 
         removePageAt(index);
         updateButtons();
     }
 
     /**
      * Removes the tab which corresponds to the specified component.
      *
      * @param component the component to remove from the tabbedpane
      * @see #addTab
      * @see #removeTabAt
      */
     public void remove(SComponent component) {
         int index = indexOfComponent(component);
         if ( index != -1 ) {
             removeTabAt(index);
         }
     }
 
     /**
      * Removes all the tabs from the tabbedpane.
      *
      * @see #addTab
      * @see #removeTabAt
      */
     public void removeAll() {
         setSelectedIndex(-1);
         buttons.removeAll();
         contents.removeAll();
         pages.clear();
     }
 
 
     /**
      * Sets the maximum tabs per line. tabs <= 0: No maximum.
      */
     public void setMaxTabsPerLine(int tabs) {
         maxTabsPerLine = tabs;
     }
 
     /**
      * Returns the maximum tabs per line.
      */
     public int getMaxTabsPerLine() {
         return maxTabsPerLine;
     }
 
     /**
      * Returns the tab title at <i>index</i>.
      *
      * @see #setTitleAt
      */
     public String getTitleAt(int index) {
         return getPageAt(index).title;
     }
 
     /**
      * Returns the tab icon at <i>index</i>.
      *
      * @see #setIconAt
      */
     public Icon getIconAt(int index) {
         return getPageAt(index).icon;
     }
 
     /**
      * Returns the tab disabled icon at <i>index</i>.
      *
      * @see #setDisabledIconAt
      */
     public Icon getDisabledIconAt(int index) {
         return getPageAt(index).disabledIcon;
     }
 
     /**
      * Returns the tab background color at <i>index</i>.
      *
      * @see #setBackgroundAt
      */
     public Color getBackgroundAt(int index) {
         return getPageAt(index).getBackground();
     }
 
     /**
      * Returns the tab foreground color at <i>index</i>.
      *
      * @see #setForegroundAt
      */
     public Color getForegroundAt(int index) {
         return getPageAt(index).getForeground();
     }
 
     /**
      * Returns the tab style at <i>index</i>.
      *
      * @see #setStyleAt
      */
     public Style getStyleAt(int index) {
         return getPageAt(index).getStyle();
     }
 
     /**
      * Returns whether or not the tab at <i>index</i> is
      * currently enabled.
      *
      * @see #setEnabledAt
      */
     public boolean isEnabledAt(int index) {
         return getPageAt(index).isEnabled();
     }
 
     /**
      * Returns the component at <i>index</i>.
      *
      * @see #setComponentAt
      */
     public SComponent getTabAt(int index) {
         return getPageAt(index).component;
     }
 
     /**
      * Sets the title at <i>index</i> to <i>title</i> which can be null.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the title should be set
      * @param title the title to be displayed in the tab
      *
      * @see #getTitleAt
      */
     public void setTitleAt(int index, String title) {
         getPageAt(index).title = title;
     }
 
     /**
      * Sets the icon at <i>index</i> to <i>icon</i> which can be null.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the icon should be set
      * @param icon the icon to be displayed in the tab
      *
      * @see #getIconAt
      */
     public void setIconAt(int index, Icon icon) {
         getPageAt(index).icon = icon;
     }
 
     /**
      * Sets the disabled icon at <i>index</i> to <i>icon</i> which can be null.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the disabled icon should be set
      * @param icon the icon to be displayed in the tab when disabled
      *
      * @see #getDisabledIconAt
      */
     public void setDisabledIconAt(int index, Icon disabledIcon) {
         getPageAt(index).disabledIcon = disabledIcon;
     }
 
     /**
      * Sets the background color at <i>index</i> to <i>background</i>
      * which can be null, in which case the tab's background color
      * will default to the background color of the tabbedpane.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the background should be set
      * @param background the color to be displayed in the tab's background
      *
      * @see #getBackgroundAt
      */
     public void setBackgroundAt(int index, Color background) {
         getPageAt(index).setBackground(background);
     }
 
     /**
      * Sets the foreground color at <i>index</i> to <i>foreground</i>
      * which can be null, in which case the tab's foreground color
      * will default to the foreground color of this tabbedpane.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the foreground should be set
      * @param foreground the color to be displayed as the tab's foreground
      *
      * @see #getForegroundAt
      */
     public void setForegroundAt(int index, Color foreground) {
         getPageAt(index).setForeground(foreground);
     }
 
     /**
      * Sets the style at <i>index</i> to <i>style</i>
      * which can be null, in which case the tab's style
      * will default to the style of this tabbedpane.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the style should be set
      * @param foreground the style to be used as the tab's style
      *
      * @see #getStyleAt
      */
     public void setStyleAt(int index, Style style) {
         getPageAt(index).setStyle(style);
     }
 
     /**
      * Sets whether or not the tab at <i>index</i> is enabled.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index which should be enabled/disabled
      * @param enabled whether or not the tab should be enabled
      *
      * @see #isEnabledAt
      */
     public void setEnabledAt(int index, boolean enabled) {
         getPageAt(index).setEnabled(enabled);
     }
 
     /**
      * Sets the component at <i>index</i> to <i>component</i>.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where this component is being placed
      * @param component the component for the tab
      *
      * @see #getComponentAt
      */
     public void setComponentAt(int index, SComponent component) {
         Page page = getPageAt(index);
        if ( component == page.component ) return;
        
        if ( page.component != null ) {
            contents.removeComponent(page.component);
        }
        page.component = component;
        contents.addComponent(page.component, page.component.getNamePrefix());
        if ( getSelectedIndex() == index ) {
            card.show(page.component);
         }
     }
 
     /**
      * Returns the first tab index with a given <i>title</i>,
      * Returns -1 if no tab has this title.
      * @param title the title for the tab
      */
     public int indexOfTab(String title) {
         for ( int i = 0; i < getTabCount(); i++ ) {
             if ( getTitleAt(i).equals( (title == null) ? "" : title) ) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Returns the first tab index with a given <i>icon</i>.
      * Returns -1 if no tab has this icon.
      * @param icon the icon for the tab
      */
     public int indexOfTab(Icon icon) {
         for ( int i = 0; i < getTabCount(); i++ ) {
             if ( getIconAt(i).equals(icon) ) {
                 return i;
             }
         }
         return -1;
     }
 
     private void removePageAt(int i) {
         Page p = getPageAt(i);
         group.remove(p.button);
         pages.remove(i);
         contents.removeComponent(p.component);
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void updateButtons() {
         buttons.removeAll();
         group.removeAll();
 
         for (int i=0; i < getTabCount(); i++) {
             Page p = getPageAt(i);
             buttons.add(p.button);
             group.add(p.button);
         }
     }
 
     private Page getPageAt(int i) {
         if ( i >= 0 && i < pages.size() ) {
             return (Page)pages.get(i);
         }
         else {
             return null;
         }
     }
 
     /**
      * TODO: documentation
      *
      * @param e
      */
     public void actionPerformed(ActionEvent e) {
         System.err.println("actionPerformed");
         SCheckBox checkbox = group.getSelection();
         for (int i=0; i < getTabCount(); i++) {
             Page page = getPageAt(i);
             if (checkbox == page.button) {
                 setSelectedIndex(i);
                 fireStateChanged();
                 return;
             }
         }
         /*
         int oldIndex = getSelectedIndex();
         // System.out.println("EVENT " +  e);
         for ( int i=0; i<getTabCount(); i++ ) {
             Page p = getPageAt(i);
             if ( p.button==e.getSource() && p.isEnabled() &&
                  p.isVisible() ) {
                 if ( i != oldIndex ) {
                     setSelectedIndex(i);
                     fireStateChanged();
                 }
 
                 // System.out.println("Select " +  i);
                 return;
             }
         }
         */
     }
 
     private class Page {
         String title;
         Color background;
         Color foreground;
         Style style;
         Icon icon;
         Icon disabledIcon;
         STabbedPane parent;
         SComponent component;
         SRadioButton button;
         String tip;
         boolean enabled = true;
 
         public Page(STabbedPane parent, String title, Icon icon,
                     Icon disabledIcon, SComponent component, String tip) {
             this.title = title;
             this.icon = icon;
             this.disabledIcon = disabledIcon;
             this.parent = parent;
             this.component = component;
             this.tip = tip;
 
             button = new SRadioButton(title);
             button.setShowAsFormComponent(false);
             button.setSelectedIcon(icon);
             button.setDisabledSelectedIcon(disabledIcon);
             button.setIcon(icon);
             button.setDisabledIcon(disabledIcon);
             button.setToolTipText(tip);
             button.setNoBreak(true);
         }
 
         /**
          * TODO: documentation
          *
          * @return
          */
         public Color getBackground() {
             return ((background != null) ? background : parent.getBackground());
         }
 
         /**
          * TODO: documentation
          *
          * @param c
          */
         public void setBackground(Color c) {
             background = c;
         }
         /**
          * TODO: documentation
          *
          * @return
          */
         public Color getForeground() {
             return ((foreground != null) ? foreground : parent.getForeground());
         }
 
         /**
          * TODO: documentation
          *
          * @param c
          */
         public void setForeground(Color c) {
             foreground = c;
         }
 
         /**
          * TODO: documentation
          *
          * @return
          */
         public Style getStyle() {
             return ((style != null) ? style : parent.getStyle());
         }
 
         /**
          * TODO: documentation
          *
          * @param c
          */
         public void setStyle(Style s) {
             style = s;
         }
 
         /**
          * TODO: documentation
          *
          * @return
          */
         public boolean isEnabled() {
             return enabled;
         }
 
         /**
          * TODO: documentation
          *
          * @param b
          */
         public void setEnabled(boolean b) {
             enabled = b;
             button.setEnabled(b);
         }
 
         /**
          * TODO: documentation
          *
          * @return
          */
         public boolean isVisible() {
             return parent.isVisible();
         }
 
         /**
          * TODO: documentation
          *
          * @param b
          */
         public void setVisible(boolean b) {
             parent.setVisible(b);
         }
     }
 
     /**
      * Returns the name of the CGFactory class that generates the
      * look and feel for this component.
      *
      * @return "TabbedPaneCG"
      * @see SComponent#getCGClassID
      * @see CGDefaults#getCG
      */
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(TabbedPaneCG cg) {
         super.setCG(cg);
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
