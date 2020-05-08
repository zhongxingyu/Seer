 package jDistsim.ui.panel;
 
 import jDistsim.ui.control.MenuSeparator;
 import jDistsim.ui.control.button.ImageButton;
 import jDistsim.ui.panel.listener.ModulesViewListener;
 import jDistsim.utils.resource.Resources;
 import jDistsim.utils.ui.control.IconBackgroundColorHoverStyle;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 5.2.13
  * Time: 1:00
  */
 public class ModulesPanel extends InternalPanel {
 
     private JTree jTree;
     private JScrollPane scrollPane;
     private JPanel contentPane;
     private ModulesViewListener viewListener;
 
     private ImageButton expandButton;
     private ImageButton collapseButton;
     private ImageButton treeViewButton;
     private ImageButton listViewButton;
 
     public ModulesPanel(JTree jTree) {
         super("Modules on model", true, false);
         this.jTree = jTree;
 
         initialize();
     }
 
     private void initialize() {
         contentPane = new JPanel();
         contentPane.setLayout(new BorderLayout());
         contentPane.setBorder(null);
 
         initializeControlPanel();
         initializeContentPanel();
         add(contentPane, BorderLayout.CENTER);
     }
 
     private void initializeControlPanel() {
         JPanel controlPanel = new JPanel();
         controlPanel.setBackground(new Color(237, 237, 237));
         controlPanel.setPreferredSize(new Dimension(getWidth(), 26));
         controlPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 203, 203)));
         controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
 
         initializeButtons(controlPanel);
         contentPane.add(controlPanel, BorderLayout.NORTH);
     }
 
     private void initializeContentPanel() {
         jTree.setCellRenderer(new DefaultTreeCellRenderer() {
             public Component getTreeCellRendererComponent(final JTree tree, Object value,
                                                           boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
 
                 JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
 
                 if (((DefaultMutableTreeNode) value).isRoot())
                     label.setIcon(new ImageIcon(Resources.getImage("system/t_icon_model.png")));
                 else {
                    if (label.getText().contains("create") || label.getText().contains("receiver")) {
                         label.setIcon(new ImageIcon(Resources.getImage("system/t_icon_create.png")));
                     } else {
                         label.setIcon(new ImageIcon(Resources.getImage("system/t_icon_classic.png")));
                     }
                 }
                 return label;
             }
         });
         jTree.setDragEnabled(false);
         jTree.setEditable(false);
 
         int verticalScrollbarAsNeeded = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
         int horizontalScrollbarAsNeeded = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
         scrollPane = new JScrollPane(jTree, verticalScrollbarAsNeeded, horizontalScrollbarAsNeeded);
         scrollPane.setBorder(BorderFactory.createEmptyBorder());
 
         jTree.setBorder(new EmptyBorder(3, 3, 3, 3));
         contentPane.add(scrollPane, BorderLayout.CENTER);
     }
 
     private void initializeButtons(JPanel controlPanel) {
         IconBackgroundColorHoverStyle hoverStyle = new IconBackgroundColorHoverStyle();
         final int padding = 3;
 
         expandButton = new ImageButton(Resources.getImage("system/panels/mp_controls_expand.png"), hoverStyle, new Dimension(16, 16), padding);
         expandButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 viewListener.onExpandButtonClick(mouseEvent, expandButton);
             }
         });
 
         collapseButton = new ImageButton(Resources.getImage("system/panels/mp_controls_collapse.png"), hoverStyle, new Dimension(16, 16), padding);
         collapseButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 viewListener.onCollapseButtonClick(mouseEvent, collapseButton);
             }
         });
 
         treeViewButton = new ImageButton(Resources.getImage("system/panels/mp_controls_tree.png"), hoverStyle, new Dimension(16, 16), padding);
         treeViewButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 viewListener.onTreeViewSelected(mouseEvent, treeViewButton);
             }
         });
 
         listViewButton = new ImageButton(Resources.getImage("system/panels/mp_controls_list.png"), hoverStyle, new Dimension(16, 16), padding);
         listViewButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 viewListener.onListViewSelected(mouseEvent, listViewButton);
             }
         });
 
         controlPanel.add(expandButton);
         controlPanel.add(collapseButton);
         controlPanel.add(new MenuSeparator(14));
         controlPanel.add(treeViewButton);
         controlPanel.add(listViewButton);
     }
 
     public void setViewListener(ModulesViewListener viewListener) {
         this.viewListener = viewListener;
     }
 
     public ImageButton getExpandButton() {
         return expandButton;
     }
 
     public ImageButton getCollapseButton() {
         return collapseButton;
     }
 
     public ImageButton getTreeViewButton() {
         return treeViewButton;
     }
 
     public ImageButton getListViewButton() {
         return listViewButton;
     }
 
     @Override
     public void showNothing() {
         scrollPane.setVisible(false);
         contentPane.setOpaque(false);
         super.showNothing();
     }
 
     @Override
     public void hideNothing() {
         scrollPane.setVisible(true);
         contentPane.setOpaque(true);
         super.hideNothing();
     }
 }
