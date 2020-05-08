 package maze.gui.mazeeditor;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.net.URL;
 
 import javax.swing.AbstractAction;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import maze.Main;
 import maze.gui.PrimaryFrame;
 import maze.model.MazeInfo;
 import maze.model.MazeInfoModel;
 
 /**
  * This panel creates a GUI to edit mazes.
  * @author John Smith
  */
 public class MazeEditor extends JPanel
 {
    private static final String NEXT_ORIENTATION_ACTION_KEY = "nextOrientation";
    private static final String TOP_HELP =
            "Left-Click: Add walls, Right-Click: " +
            "Remove walls, Middle-Click: Change template Orientation, "+
            "Wheelup: Grow template, Wheeldown: Shrink template";
    private static final String POPUP_HELP =
            "<html><b>Left-Click</b>: Add walls<br /><b>Right-Click</b>: " +
            "Remove walls<br /><b>Middle-Click</b>: Change template " +
            "Orientation<br /><b>Wheelup</b>: Grow template<br />" +
            "<b>Wheeldown</b>: Shrink template</btml>";
    private ImageIcon mPointIcon;
    private MazeTemplate mCurrentTemplate = null;
    private EditableMazeView mMazeView;
    private JList mOpenMazes;
    private MouseAdapter mMouseAdapter = null;
 
    private static final MazeTemplate[] mTemplates =
    {
       new BoxTemplate(),
       new StraightTemplate(),
       new CornerTemplate(),
       new CrossTemplate(),
       new ZigZagTemplate(),
       new WaveTemplate(),
       new StaggeredTemplate(),
       new JungleTemplate(),
       new RungTemplate(),
       new GappedTemplate()
    };
 
    public MazeEditor()
    {
       buildPanel();
 
    }
 
    private void buildPanel()
    {
       URL iconResource = BoxTemplate.class.getResource("images/Pointer.png");
       mPointIcon = new ImageIcon(iconResource);
 
       setLayout(new BorderLayout());
       JLabel instr = new JLabel(TOP_HELP);
       instr.setToolTipText(POPUP_HELP);
       add(instr, BorderLayout.NORTH);
 
       final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
       splitPane.setResizeWeight(.8);
       splitPane.setOneTouchExpandable(true);
 
       mMazeView = new EditableMazeView();
       mMazeView.setEditable(true);
       splitPane.setLeftComponent(mMazeView);
       mMazeView.setModel(null);
 
       mOpenMazes = createOpenMazeList();
       JPanel rightPanel = new JPanel();
       rightPanel.setLayout(new BorderLayout());
       rightPanel.add(mOpenMazes, BorderLayout.CENTER);
       
 
       rightPanel.add(makeNewMazeButton(), BorderLayout.SOUTH);
       splitPane.setRightComponent(rightPanel);
       splitPane.addPropertyChangeListener("dividerLocation",
                                           new PropertyChangeListener()
       {
          @Override
          public void propertyChange(PropertyChangeEvent evt)
          {
             mMazeView.componentResized(null);
          }
       }); // new PropertyChangeListener()
 
       add(splitPane, BorderLayout.CENTER);
       splitPane.setDividerLocation(.8);
 
       addComponentListener(new ComponentAdapter()
       {
          boolean notShown = true;
          @Override
          public void componentShown(ComponentEvent e)
          {
             if (notShown)
             {
                splitPane.setDividerLocation(.8);
                mMazeView.componentResized(null);
                notShown = false;
             } // if (notShown)
          } // public void componentShown(ComponentEvent e)
       }); // addComponentListener(new ComponentAdapter()
 
       JToolBar tBar = new JToolBar();
       tBar.setOrientation(JToolBar.VERTICAL);
       tBar.setFloatable(false);
       ButtonGroup bg = new ButtonGroup();
 
       ImageIcon scaled = new ImageIcon(mPointIcon.getImage().getScaledInstance(40, 40, 0));
       JToggleButton tb = new JToggleButton(scaled);
       tb.setToolTipText("No Template");
       tb.addActionListener(new TemplateActionListener(null));
       bg.add(tb);
       bg.setSelected(tb.getModel(), true);
       tBar.add(tb);
 
       for (MazeTemplate mt : mTemplates)
       {
          Image iconImage = mt.getTemplateIcon().getImage();
          scaled = new ImageIcon(iconImage.getScaledInstance(40, 40, 0));
          tb = new JToggleButton(scaled);
          tb.addActionListener(new TemplateActionListener(mt));
          tb.setToolTipText(mt.getTemplateDescription());
          bg.add(tb);
          tBar.add(tb);
       }
 
       add(tBar, BorderLayout.WEST);
       
       //Add an action for the 'r' key to change the template to the next orientation.
       this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke('r'),
                                                                           NEXT_ORIENTATION_ACTION_KEY);
       this.getActionMap().put(NEXT_ORIENTATION_ACTION_KEY, new AbstractAction()
       {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            if (mCurrentTemplate != null)
            {
               mCurrentTemplate.nextOrientation();
               mMazeView.repaint();
            }
          }
       });
       
 
       mMouseAdapter = new TemplateMouseAdapter();
       
       mMazeView.addMouseListener(mMouseAdapter);
       mMazeView.addMouseMotionListener(mMouseAdapter);
       mMazeView.addMouseWheelListener(mMouseAdapter);
    } // void buildPanel()
 
    private void setTemplate(MazeTemplate mt)
    {
       if (mt == mCurrentTemplate)
          return;
 
       mCurrentTemplate = mt;
       if (mCurrentTemplate == null)
          mMazeView.setEditable(true);
       else
       {
          mMazeView.setEditable(false);
          mt.reset();
       }
       mMazeView.setTemplate(mt);
    }
 
    private JButton makeNewMazeButton()
    {
       JButton newMaze = new JButton("New Maze");
       newMaze.setToolTipText("Create a new maze");
       newMaze.addActionListener(new ActionListener()
       {
          private String query = "What would you like to call your new maze?";
          private Box messagePanel;
          private JTextField input = new JTextField();
 
          {
             JLabel message = new JLabel(query);
             messagePanel = new Box(BoxLayout.Y_AXIS);
             messagePanel.add(message);
             messagePanel.add(input);
             input.addAncestorListener(new AncestorListener()
             {
                @Override
                public void ancestorAdded(AncestorEvent event)
                {
                   input.requestFocus();
                }
 
                @Override
                public void ancestorRemoved(AncestorEvent event){}
                @Override
                public void ancestorMoved(AncestorEvent event){}
             });
          }
          @Override
          public void actionPerformed(ActionEvent e)
          {
             input.setText("");
             PrimaryFrame instance = Main.getPrimaryFrameInstance();
             String newName = "";
             while (newName.equals(""))
             {
                int result = JOptionPane.showConfirmDialog(instance,
                         messagePanel, "New Maze", JOptionPane.OK_CANCEL_OPTION,
                         JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.OK_OPTION)
                   newName = input.getText();
                else
                   return;
             }
             MazeInfoModel mim = instance.getMazeInfoModel();
             mOpenMazes.setSelectedValue(mim.createNew(newName), true);
          }
       });
       return newMaze;
    }
 
    private JList createOpenMazeList()
    {
       final JList newList = new JList();
       ComboBoxModel cbm = Main.getPrimaryFrameInstance().getMazeInfoModel()
                               .getMazeInfoComboBoxModel();
       newList.setCellRenderer(new OpenMazeRender());
       newList.setModel(cbm);
       newList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
       newList.getSelectionModel().addListSelectionListener(
       new ListSelectionListener()
       {
          @Override
          public void valueChanged(ListSelectionEvent e)
          {
             int index = ((ListSelectionModel)e.getSource()).getMaxSelectionIndex();
             if (index == -1)
                return;
             Object o = newList.getModel().getElementAt(index);
             MazeInfo mi = (MazeInfo)o;
             mMazeView.setModel(mi.getModel());
          }
       });
       newList.setBorder(new TitledBorder("Mazes"));
       return newList;
    }
 
    class TemplateActionListener implements ActionListener
    {
       private MazeTemplate mt;
       public TemplateActionListener(MazeTemplate mt)
       {
          this.mt = mt;
       }
 
       @Override
       public void actionPerformed(ActionEvent e)
       {
          setTemplate(mt);
       }
    }
 
    private class TemplateMouseAdapter extends MouseAdapter
    {
       @Override
       public void mouseMoved(MouseEvent e)
       {
         if (mMazeView.getModel() != null && mCurrentTemplate != null)
          {
             mCurrentTemplate.updatePosition(e.getPoint(),
                                             mMazeView.getCellSize());
             mMazeView.repaint();
          }
       } // public void mouseMoved(MouseEvent e)
 
       @Override
       public void mouseDragged(MouseEvent e)
       {
         if (mMazeView.getModel() != null && mCurrentTemplate != null)
          {
             //boolean left = SwingUtilities.isLeftMouseButton(e);
             //boolean right = SwingUtilities.isRightMouseButton(e);
             mMazeView.repaint();
          }
       } // public void mouseDragged(MouseEvent e)
 
       @Override
       public void mousePressed(MouseEvent e)
       {
         if (mMazeView.getModel() != null && mCurrentTemplate != null)
          {
             if (e.getButton() == MouseEvent.BUTTON2)
                mCurrentTemplate.nextOrientation();
             else if (e.getButton() == MouseEvent.BUTTON1)
                mMazeView.applyTemplate(true);
             else if (e.getButton() == MouseEvent.BUTTON3)
                mMazeView.applyTemplate(false);
             mMazeView.repaint();
          }
       } // public void mousePressed(MouseEvent e)
       
       @Override
       public void mouseWheelMoved(MouseWheelEvent e)
       {
         if (mMazeView.getModel() == null || mCurrentTemplate == null)
             return;
          int amount = e.getWheelRotation();
          boolean neg = amount < 0;
          amount = Math.abs(amount);
          for (int i = 0; i < amount; i++)
          {
             if (neg)
                mCurrentTemplate.grow();
             else
                mCurrentTemplate.shrink();
          }
          mMazeView.repaint();
       }
    }
 
    private static class OpenMazeRender extends DefaultListCellRenderer
    {
 
       @Override
       public Component getListCellRendererComponent(JList list, Object value,
                                              int index, boolean isSelected,
                                              boolean cellHasFocus)
       {
          MazeInfo mi = (MazeInfo)value;
          String postfix = "";
          if (mi.isDirty())
             postfix = "*";
          Component c = super.getListCellRendererComponent(
                                                    list, mi.getName() + postfix,
                                                    index, isSelected,
                                                    cellHasFocus);
          JComponent jc = (JComponent)c;
          jc.setToolTipText(mi.getPath());
          return jc;
       }
 
    }
 
 }
