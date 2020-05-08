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
 
 package com.dmdirc.addons.ui_swing.dialogs.about;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.components.TreeScroller;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.plugins.PluginManager;
 
 import java.awt.Font;
 import java.awt.Rectangle;
 
 import javax.inject.Inject;
 import javax.swing.BorderFactory;
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.UIManager;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeSelectionModel;
 
 import net.miginfocom.layout.PlatformDefaults;
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Licences panel.
  */
 public class LicencesPanel extends JPanel implements TreeSelectionListener {
 
     /** Serial version UID. */
     private static final long serialVersionUID = 3;
     /** Config manager. */
     private final AggregateConfigProvider config;
     /** Licence scroll pane. */
     private JScrollPane scrollPane;
     /** Licence list model */
     private DefaultTreeModel listModel;
     /** Licence textpane. */
     private JEditorPane licence;
     /** Licence list. */
     private JTree list;
 
     /**
      * Creates a new instance of LicencesPanel.
      *
      * @param globalConfig The config to read settings from.
      * @param pluginManager The manager to use to find plugins (to display their licenses).
      */
     @Inject
     public LicencesPanel(
             @GlobalConfig final AggregateConfigProvider globalConfig,
             final PluginManager pluginManager) {
         super();
 
         this.config = globalConfig;
        initComponents();
        new LicenceLoader(pluginManager, list, listModel).executeInExecutor();
         addListeners();
         layoutComponents();
     }
 
     /**
      * Adds the listeners to the components.
      */
     private void addListeners() {
         list.addTreeSelectionListener(this);
     }
 
     /**
      * Lays out the components.
      */
     private void layoutComponents() {
         setLayout(new MigLayout("ins rel, fill"));
         add(new JScrollPane(list), "growy, pushy, w 150!");
         add(scrollPane, "grow, push");
     }
 
     /** Initialises the components. */
    private void initComponents() {
         setOpaque(UIUtilities.getTabbedPaneOpaque());
         listModel = new DefaultTreeModel(new DefaultMutableTreeNode());
         list = new JTree(listModel) {
 
             /**
             * A version number for this class.
              */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public void scrollRectToVisible(final Rectangle aRect) {
                 final Rectangle rect = new Rectangle(0, aRect.y, aRect.width,
                         aRect.height);
                 super.scrollRectToVisible(rect);
             }
         };
         list.setBorder(BorderFactory.createEmptyBorder(
                 (int) PlatformDefaults.getUnitValueX("related").getValue(),
                 (int) PlatformDefaults.getUnitValueX("related").getValue(),
                 (int) PlatformDefaults.getUnitValueX("related").getValue(),
                 (int) PlatformDefaults.getUnitValueX("related").getValue()));
         list.setCellRenderer(new LicenceRenderer());
         list.setRootVisible(false);
         list.setOpaque(false);
         list.getSelectionModel().setSelectionMode(TreeSelectionModel.
                 SINGLE_TREE_SELECTION);
         new TreeScroller(list);
         licence = new JEditorPane();
         licence.setEditorKit(new HTMLEditorKit());
         final Font font = UIManager.getFont("Label.font");
         ((HTMLDocument) licence.getDocument()).getStyleSheet().addRule("body " +
                 "{ font-family: " + font.getFamily() + "; " + "font-size: " +
                 font.getSize() + "pt; }");
         licence.setEditable(false);
         scrollPane = new JScrollPane(licence);
     }
 
     /** {@inheritDoc} */
     @Override
     public void valueChanged(final TreeSelectionEvent e) {
         if (list.getSelectionCount() == 0) {
             list.setSelectionPath(e.getOldLeadSelectionPath());
         }
         list.scrollPathToVisible(e.getPath());
         final Object userObject = ((DefaultMutableTreeNode) e.getPath().
                 getLastPathComponent()).getUserObject();
         if (userObject instanceof Licence) {
             licence.setText(((Licence) userObject).getBody());
         } else if (userObject instanceof PluginInfo) {
             final PluginInfo pi = (PluginInfo) userObject;
             licence.setText("<b>Name:</b> "
                     + pi.getMetaData().getFriendlyName() + "<br>"
                     + "<b>Version:</b> "
                     + pi.getMetaData().getFriendlyVersion() + "<br>"
                     + "<b>Author:</b> " + pi.getMetaData().getAuthor() + "<br>"
                     + "<b>Description:</b> "
                     + pi.getMetaData().getDescription() + "<br>");
         } else {
             licence.setText("<b>Name:</b> DMDirc<br>"
                     + "<b>Version:</b> " + config
                     .getOption("version", "version") + "<br>"
                     + "<b>Desciption:</b> The intelligent IRC client");
         }
         UIUtilities.resetScrollPane(scrollPane);
     }
 }
