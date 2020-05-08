 package gui.mainapp;
 
 import engine.expressions.ParboiledExpressionParser;
 import engine.expressions.ParsingException;
 import gui.mainapp.editor.RedLineHighlightPainter;
 import gui.mainapp.viewmodel.*;
 import gui.mainapp.viewport.EqualViewport;
 import gui.mainapp.viewport.FrameListener;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Highlighter;
 import java.awt.*;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.util.*;
 import java.util.List;
 
 /**
  * User: Oleksiy Pylypenko
  * Date: 3/5/13
  * Time: 7:31 AM
  */
 public class EqualAppPanel {
     private JTextArea equationPad;
     private JPanel root;
     private JPanel sidePanel;
     private JButton refreshButton;
     private JSlider timeSlider;
     private JButton playButton;
     private JButton upButton;
     private JButton leftButton;
     private JButton downButton;
     private JButton rightButton;
     private JButton zoomInButton;
     private JButton zoomOutButton;
     private JLabel constantsLabel;
     private JLabel variablesLabel;
 
     private EqualViewport equalViewport;
 
     private final EqualViewModel viewModel;
     private final Player player;
     private final EqualAppPanelViewListener viewListener;
 
     public EqualAppPanel(final EqualViewModel viewModel) {
         this.viewModel = viewModel;
 
         $$$setupUI$$$();
 
         equalViewport.setParser(new ParboiledExpressionParser());
 
         player = new Player();
         viewListener = new EqualAppPanelViewListener();
         viewModel.addViewListener(viewListener);
 
         bindButtonAction(viewModel, playButton, KeyStroke.getKeyStroke("F5"), ActionType.PLAY);
         bindButtonAction(viewModel, zoomInButton, KeyStroke.getKeyStroke("F8"), ActionType.ZOOM_IN);
         bindButtonAction(viewModel, zoomOutButton, KeyStroke.getKeyStroke("F7"), ActionType.ZOOM_OUT);
         bindButtonAction(viewModel, leftButton, KeyStroke.getKeyStroke("F9"), ActionType.LEFT);
         bindButtonAction(viewModel, upButton, KeyStroke.getKeyStroke("F10"), ActionType.UP);
         bindButtonAction(viewModel, downButton, KeyStroke.getKeyStroke("F11"), ActionType.DOWN);
         bindButtonAction(viewModel, rightButton, KeyStroke.getKeyStroke("F12"), ActionType.RIGHT);
 
         bindAction(viewModel,
                 KeyStroke.getKeyStroke("F3"),
                 ActionType.LOWER_T);
         bindAction(viewModel,
                 KeyStroke.getKeyStroke("F4"),
                 ActionType.RAISE_T);
 
         equationPad
                 .getDocument()
                 .addDocumentListener(
                         new EquationUpdater(viewModel));
 
         equalViewport.addComponentListener(new ViewportResizeUpdater());
         equalViewport.setRecalculateEachSubmit(false);
         equalViewport.setDelayedRecalculation(true);
 
         timeSlider.addChangeListener(new TimeSliderUpdater());
     }
 
     private void bindButtonAction(EqualViewModel viewModel,
                                   JButton button,
                                   KeyStroke key,
                                   ActionType actionType) {
         new ViewModelAction(viewModel, actionType)
                 .fillTextAndIcon(button)
                 .putActionMap(root)
                 .bindKey(root, key)
                 .bind(button);
     }
 
     private void bindAction(EqualViewModel viewModel,
                             KeyStroke key,
                             ActionType actionType) {
         new ViewModelAction(viewModel, actionType)
                 .putActionMap(root)
                 .bindKey(root, key);
     }
 
     private void resetParsingErrors() {
         Highlighter highlighter = equationPad.getHighlighter();
         highlighter.removeAllHighlights();
 
     }
     private void showParsingErrors(ParsingException e) {
         resetParsingErrors();
 
         List<ParsingException.SyntaxError> errors = e.getErrors();
         for (ParsingException.SyntaxError err : errors) {
             try {
                 Highlighter highlighter = equationPad.getHighlighter();
                 int idx = err.getStartIndex();
                if (idx >= equationPad.getText().length()) {
                    idx = equationPad.getText().length() - 1;
                 }
                 highlighter.addHighlight(
                         idx,
                         err.getEndIndex(),
                         new RedLineHighlightPainter());
             } catch (BadLocationException e1) {
                System.err.println(e1);
             }
         }
        equationPad.repaint();
     }
 
     public void createUIComponents() {
         equalViewport = new EqualViewport();
     }
 
     public void beAContentPaneOf(JFrame frame) {
         frame.setContentPane(root);
     }
 
     /**
      * Method generated by IntelliJ IDEA GUI Designer
      * >>> IMPORTANT!! <<<
      * DO NOT edit this method OR call it in your code!
      *
      * @noinspection ALL
      */
     private void $$$setupUI$$$() {
         createUIComponents();
         root = new JPanel();
         root.setLayout(new BorderLayout(0, 0));
         sidePanel = new JPanel();
         sidePanel.setLayout(new BorderLayout(0, 0));
         root.add(sidePanel, BorderLayout.WEST);
         final JPanel panel1 = new JPanel();
         panel1.setLayout(new GridBagLayout());
         sidePanel.add(panel1, BorderLayout.SOUTH);
         final JPanel panel2 = new JPanel();
         panel2.setLayout(new GridBagLayout());
         GridBagConstraints gbc;
         gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         panel1.add(panel2, gbc);
         panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Controls"));
         rightButton = new JButton();
         rightButton.setHorizontalTextPosition(11);
         rightButton.setIcon(new ImageIcon(getClass().getResource("/gui/mainapp/right-arrow.png")));
         rightButton.setText("F12");
         rightButton.setToolTipText("Move viewport right");
         gbc = new GridBagConstraints();
         gbc.gridx = 1;
         gbc.gridy = 3;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         panel2.add(rightButton, gbc);
         final JPanel spacer1 = new JPanel();
         gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 1;
         gbc.fill = GridBagConstraints.VERTICAL;
         gbc.ipady = 10;
         panel2.add(spacer1, gbc);
         zoomInButton = new JButton();
         zoomInButton.setHorizontalTextPosition(10);
         zoomInButton.setIcon(new ImageIcon(getClass().getResource("/gui/mainapp/zoom-in.png")));
         zoomInButton.setText("F8");
         zoomInButton.setToolTipText("Zoom in viewport");
         gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         panel2.add(zoomInButton, gbc);
         zoomOutButton = new JButton();
         zoomOutButton.setHorizontalTextPosition(11);
         zoomOutButton.setIcon(new ImageIcon(getClass().getResource("/gui/mainapp/zoom-out.png")));
         zoomOutButton.setText("F7");
         zoomOutButton.setToolTipText("Zoom out viewport");
         gbc = new GridBagConstraints();
         gbc.gridx = 1;
         gbc.gridy = 0;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         panel2.add(zoomOutButton, gbc);
         leftButton = new JButton();
         leftButton.setHorizontalTextPosition(10);
         leftButton.setIcon(new ImageIcon(getClass().getResource("/gui/mainapp/left-arrow.png")));
         leftButton.setText("F9");
         leftButton.setToolTipText("Move viewport left");
         gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 2;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         panel2.add(leftButton, gbc);
         upButton = new JButton();
         upButton.setHorizontalTextPosition(11);
         upButton.setIcon(new ImageIcon(getClass().getResource("/gui/mainapp/up-arrow.png")));
         upButton.setText("F10");
         upButton.setToolTipText("Move viewort up");
         gbc = new GridBagConstraints();
         gbc.gridx = 1;
         gbc.gridy = 2;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         panel2.add(upButton, gbc);
         downButton = new JButton();
         downButton.setHorizontalTextPosition(10);
         downButton.setIcon(new ImageIcon(getClass().getResource("/gui/mainapp/down-arrow.png")));
         downButton.setText("F11");
         downButton.setToolTipText("Move viewport down");
         gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 3;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         panel2.add(downButton, gbc);
         final JScrollPane scrollPane1 = new JScrollPane();
         sidePanel.add(scrollPane1, BorderLayout.CENTER);
         scrollPane1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Equations"));
         equationPad = new JTextArea();
         equationPad.setColumns(30);
         equationPad.setText("coords(5,1)\ny=\n");
         equationPad.putClientProperty("html.disable", Boolean.TRUE);
         scrollPane1.setViewportView(equationPad);
         final JPanel panel3 = new JPanel();
         panel3.setLayout(new BorderLayout(0, 0));
         root.add(panel3, BorderLayout.CENTER);
         final JPanel panel4 = new JPanel();
         panel4.setLayout(new GridBagLayout());
         panel3.add(panel4, BorderLayout.SOUTH);
         panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "'t' control"));
         timeSlider = new JSlider();
         timeSlider.setSnapToTicks(true);
         timeSlider.setValue(0);
         timeSlider.putClientProperty("JSlider.isFilled", Boolean.FALSE);
         timeSlider.putClientProperty("html.disable", Boolean.FALSE);
         timeSlider.putClientProperty("Slider.paintThumbArrowShape", Boolean.FALSE);
         gbc = new GridBagConstraints();
         gbc.gridx = 1;
         gbc.gridy = 0;
         gbc.weightx = 100.0;
         gbc.anchor = GridBagConstraints.WEST;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         panel4.add(timeSlider, gbc);
         playButton = new JButton();
         playButton.setHorizontalTextPosition(11);
         playButton.setIcon(new ImageIcon(getClass().getResource("/gui/mainapp/play.png")));
         playButton.setText(" F5");
         playButton.setToolTipText("Run time series of graphics by changing \"t\" from 0 to 1 ");
         gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         panel4.add(playButton, gbc);
         final JPanel panel5 = new JPanel();
         panel5.setLayout(new BorderLayout(0, 0));
         panel3.add(panel5, BorderLayout.CENTER);
         panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Viewport"));
         final JPanel panel6 = new JPanel();
         panel6.setLayout(new BorderLayout(0, 0));
         panel6.setBackground(Color.white);
         panel5.add(panel6, BorderLayout.CENTER);
         panel6.add(equalViewport, BorderLayout.CENTER);
         final JToolBar toolBar1 = new JToolBar();
         root.add(toolBar1, BorderLayout.SOUTH);
         constantsLabel = new JLabel();
         constantsLabel.setText("LEFT(-10) TOP(10) RIGHT(10) BOTTOM(-10) STEPS(100) WIDTH(800) HEIGHT(600)");
         toolBar1.add(constantsLabel);
         final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
         toolBar1.add(toolBar$Separator1);
         variablesLabel = new JLabel();
         variablesLabel.setText("t(0)");
         toolBar1.add(variablesLabel);
     }
 
     /**
      * @noinspection ALL
      */
     public JComponent $$$getRootComponent$$$() {
         return root;
     }
 
     private class EquationUpdater implements DocumentListener, Runnable {
         private final EqualViewModel viewModel;
 
         public EquationUpdater(EqualViewModel viewModel) {
             this.viewModel = viewModel;
         }
 
         @Override
         public void insertUpdate(DocumentEvent e) {
             updateModel();
         }
 
         @Override
         public void removeUpdate(DocumentEvent e) {
             updateModel();
         }
 
         @Override
         public void changedUpdate(DocumentEvent e) {
             updateModel();
         }
 
         private void updateModel() {
             viewListener.withDisabled(InterfacePart.EQUATION, this);
         }
 
         @Override
         public void run() {
             viewModel.setEquations(equationPad.getText());
         }
     }
 
     private class InterfaceUpdater implements InterfacePartVisitor {
         private final EqualViewModel viewModel;
 
         public InterfaceUpdater(EqualViewModel viewModel) {
             this.viewModel = viewModel;
         }
 
         @Override
         public void constants() {
             constantsLabel.setText(viewModel.getConstantsStatus());
         }
 
         @Override
         public void variables() {
             variablesLabel.setText(viewModel.getVariablesStatus());
         }
 
         @Override
         public void equation() {
             equationPad.setText(viewModel.getEquations());
         }
 
         @Override
         public void viewport() {
             try {
                 equalViewport.setSize(viewModel.getViewportSize());
                 equalViewport.setViewportBounds(viewModel.getViewportBounds());
                 equalViewport.setT(viewModel.getTAsVariable());
                 equalViewport.setExpression(viewModel.getEquations());
                 resetParsingErrors();
             } catch (ParsingException e) {
                 showParsingErrors(e);
             }
         }
 
         @Override
         public void timeControl() {
             timeSlider.setMinimum(0);
             timeSlider.setMaximum(viewModel.getSteps());
             timeSlider.setValue(viewModel.getT());
         }
     }
 
     private class EqualAppPanelViewListener implements ViewListener {
         private Set<InterfacePart> disabled;
 
         public void withDisabled(InterfacePart disabledPart, Runnable runnable) {
             Set<InterfacePart> prevDisabled = disabled;
             disabled = EnumSet.of(disabledPart);
             runnable.run();
             disabled = prevDisabled;
         }
 
         @Override
         public void onUpdate(Set<InterfacePart> parts) {
             for (InterfacePart part : parts) {
                 if (disabled != null && disabled.contains(part)) {
                     continue;
                 }
                 part.accept(new InterfaceUpdater(viewModel));
             }
         }
 
         @Override
         public void onPlayStateChange(PlayState state) {
             state.accept(player);
         }
     }
     private class Player implements PlayStateVisitor, FrameListener {
 
         @Override
         public void play() {
             equalViewport.addFrameListener(this);
             equalViewport.setRecalculateEachSubmit(true);
             equalViewport.setDelayedRecalculation(false);
             viewModel.setT(0);
         }
 
         @Override
         public void stop() {
             equalViewport.setRecalculateEachSubmit(false);
             equalViewport.setDelayedRecalculation(true);
             equalViewport.removeFrameListener(this);
         }
 
         @Override
         public void frameDone() {
             int t = viewModel.getT();
             if (t >= viewModel.getSteps()) {
                 viewModel.getPlayStateControl().stop();
             } else {
                 viewModel.setT(t + 1);
             }
         }
     }
 
     private class TimeSliderUpdater implements ChangeListener, Runnable {
 
         @Override
         public void stateChanged(ChangeEvent e) {
             viewListener.withDisabled(InterfacePart.TIME_CONTROL, this);
         }
 
         @Override
         public void run() {
             viewModel.setT(timeSlider.getValue());
         }
     }
 
     private class ViewportResizeUpdater extends ComponentAdapter implements Runnable {
         @Override
         public void componentResized(ComponentEvent e) {
             viewListener.withDisabled(InterfacePart.VIEWPORT, this);
         }
 
         @Override
         public void run() {
             viewModel.setViewportSize(equalViewport.getSize());
         }
     }
 }
