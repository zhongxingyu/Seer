 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 import javax.swing.origamist.BackgroundImageSupport;
 import javax.swing.origamist.BackgroundImageSupport.BackgroundRepeat;
 import javax.swing.origamist.JLocalizedLabel;
 import javax.swing.origamist.JPanelWithOverlay;
 import javax.swing.origamist.JToolBarWithBgImage;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 import cz.cuni.mff.peckam.java.origamist.gui.viewer.DisplayMode;
 import cz.cuni.mff.peckam.java.origamist.model.Origami;
 import cz.cuni.mff.peckam.java.origamist.model.Step;
 import cz.cuni.mff.peckam.java.origamist.utils.ParametrizedLocalizedString;
 
 /**
  * A component for rendering a set of StepRenderers.
  * 
  * @author Martin Pecka
  */
 public class DiagramRenderer extends JPanelWithOverlay
 {
     private static final long             serialVersionUID    = -7158217935566060260L;
 
     /** The origami which is this diagram from. */
     protected Origami                     origami;
 
     /** The first step to be rendered. */
     protected Step                        firstStep;
 
     /** A display pattern for displaying only a single step. */
     protected static final Dimension      SINGLE_STEP_PATTERN = new Dimension(1, 1);
 
     /** The number of steps that will be shown at once. */
     protected Dimension                   displayedStepsPattern;
 
     /** The mode the renderer actually displays the origami in. */
     protected DisplayMode                 mode;
 
     /** The listener that will be fired after the locale of the GUI changed. */
     protected PropertyChangeListener      localeListener;
 
     /** The actually displayed step renderers. */
     protected final List<StepRenderer>    stepRenderers       = new LinkedList<StepRenderer>();
 
     /** The basic zoom of all StepRenderers. */
     protected double                      zoom                = 100d;
 
     // COMPONENTS
 
     /** The label to be displayed over the renderer while it is loading. */
     protected final JLabel                overlayLabel;
 
     /** The panel holding all the StepRenderers. */
     protected final JPanel                diagramPane;
 
     /** The toolbar for diagram manipulations. */
     protected final JToolBarWithBgImage   toolbar;
 
     /** The toolbar of the currently displayed step if the display mode is DIAGRAM. */
     protected JToolBar                    stepToolbar;
 
     /** The panel holding all toolbars this renderer should show. */
     protected final JPanel                toolbarPane;
 
     /** Previous button in PAGE mode. */
     protected final JButton               prevButtonPage;
     /** Previous button in DIAGRAM mode. */
     protected final JButton               prevButtonDiagram;
     /** Next button in PAGE mode. */
     protected final JButton               nextButtonPage;
     /** Next button in DIAGRAM mode. */
     protected final JButton               nextButtonDiagram;
     /** First button in PAGE mode. */
     protected final JButton               firstButtonPage;
     /** First button in DIAGRAM mode. */
     protected final JButton               firstButtonDiagram;
     /** Last button in PAGE mode. */
     protected final JButton               lastButtonPage;
     /** Last button in DIAGRAM mode. */
     protected final JButton               lastButtonDiagram;
     /** The quick-jump combobox for navigating through pages. */
     protected final JComboBox             pageSelect;
     /** The model of pageSelect for PAGE mode. */
     protected ComboBoxModel               pageSelectPageModel;
     /** The model of pageSelect for DIAGRAM mode. */
     protected ComboBoxModel               pageSelectDiagramModel;
     /** The string saying "Step x of y" to be displayed in <code>pageSelect</code> */
     protected ParametrizedLocalizedString stepXofY;
     /** The string saying "Page x of y" to be displayed in <code>pageSelect</code> */
     protected ParametrizedLocalizedString pageXofY;
 
     /**
      * @param o
      * @param firstStep
      */
     public DiagramRenderer(Origami o, Step firstStep)
     {
         overlayLabel = new JLocalizedLabel("application", "DiagramRenderer.loading");
         overlayLabel.setFont(overlayLabel.getFont().deriveFont(Font.BOLD, 40));
         overlayLabel.setOpaque(false);
 
         getOverlay().setBackground(new Color(0, 0, 0, 64));
         // don't allow the content to respond to key events while the overlay is displayed
         getOverlay().addKeyListener(new KeyListener() {
             @Override
             public void keyTyped(KeyEvent e)
             {
                 e.consume();
             }
 
             @Override
             public void keyPressed(KeyEvent e)
             {
                 e.consume();
             }
 
             @Override
             public void keyReleased(KeyEvent e)
             {
                 e.consume();
             }
         });
         // don't allow the content to respond to mouse events while the overlay is displayed
         getOverlay().addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e)
             {
                 e.consume();
             }
 
             @Override
             public void mousePressed(MouseEvent e)
             {
                 e.consume();
             }
 
             @Override
             public void mouseReleased(MouseEvent e)
             {
                 e.consume();
             }
 
             @Override
             public void mouseMoved(MouseEvent e)
             {
                 e.consume();
             }
         });
 
         diagramPane = new JPanel();
 
         toolbar = new JToolBarWithBgImage();
         toolbar.setFloatable(false);
         toolbar.setBackground(new Color(231, 231, 184, 230));
         toolbar.setBackgroundImage(new BackgroundImageSupport(getClass()
                 .getResource("/resources/images/tooltip-bg.png"), toolbar, 0, 0, BackgroundRepeat.REPEAT_X));
 
         firstButtonPage = toolbar.createToolbarButton(new FirstAction(), "DiagramRenderer.first.page",
                 "first-page-24.png");
         firstButtonDiagram = toolbar.createToolbarButton(new FirstAction(), "DiagramRenderer.first.diagram",
                 "first-page-24.png");
         firstButtonDiagram.setVisible(false);
 
         prevButtonPage = toolbar.createToolbarButton(new PrevAction(), "DiagramRenderer.prev.page", "prev-page-24.png");
         prevButtonDiagram = toolbar.createToolbarButton(new PrevAction(), "DiagramRenderer.prev.diagram",
                 "prev-page-24.png");
         prevButtonDiagram.setVisible(false);
 
         pageSelect = new JComboBox();
         pageSelect.setEditable(false);
         pageSelect.setBackground(new Color(244, 244, 224));
         pageSelect.setAction(new PageSelectAction());
 
         nextButtonPage = toolbar.createToolbarButton(new NextAction(), "DiagramRenderer.next.page", "next-page-24.png");
         nextButtonDiagram = toolbar.createToolbarButton(new NextAction(), "DiagramRenderer.next.diagram",
                 "next-page-24.png");
         nextButtonDiagram.setVisible(false);
 
         lastButtonPage = toolbar.createToolbarButton(new LastAction(), "DiagramRenderer.last.page", "last-page-24.png");
         lastButtonDiagram = toolbar.createToolbarButton(new LastAction(), "DiagramRenderer.last.diagram",
                 "last-page-24.png");
         lastButtonDiagram.setVisible(false);
 
         toolbarPane = new JPanel();
         toolbarPane.setBorder(BorderFactory.createEmptyBorder());
 
         buildLayout();
 
         PropertyChangeListener l = new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 if (DisplayMode.DIAGRAM.equals(mode)) {
                     firstButtonDiagram.setEnabled(origami.getModel().getSteps().getStep()
                             .indexOf(DiagramRenderer.this.firstStep) > 0);
                     prevButtonDiagram.setEnabled(firstButtonDiagram.isEnabled());
                     lastButtonDiagram
                             .setEnabled(origami.getModel().getSteps().getStep().indexOf(DiagramRenderer.this.firstStep) < origami
                                     .getModel().getSteps().getStep().size() - 1);
                     nextButtonDiagram.setEnabled(lastButtonDiagram.isEnabled());
                 } else {
                     int numSteps = origami.getModel().getSteps().getStep().size();
                     int stepsPerPage = origami.getPaper().getCols() * origami.getPaper().getRows();
                     int numPages = (int) Math.ceil((double) numSteps / stepsPerPage);
 
                     int stepIndex = origami.getModel().getSteps().getStep().indexOf(DiagramRenderer.this.firstStep) + 1;
                     int pageIndex = (stepIndex - 1) / stepsPerPage + 1;
 
                     firstButtonPage.setEnabled(pageIndex > 1);
                     prevButtonPage.setEnabled(firstButtonPage.isEnabled());
                     lastButtonPage.setEnabled(pageIndex < numPages);
                     nextButtonPage.setEnabled(lastButtonPage.isEnabled());
                 }
             }
         };
         addPropertyChangeListener("mode", l);
         addPropertyChangeListener("firstStep", l);
 
         setOrigami(o, firstStep, false);
         setDisplayMode(DisplayMode.PAGE);
        getContent().setOpaque(false);
     }
 
     /**
      * Add the components to their containers.
      */
     protected void buildLayout()
     {
         getOverlay().setLayout(
                 new FormLayout("left:pref:grow, center:pref, right:pref:grow",
                         "top:pref:grow, center:pref, bottom:pref:grow"));
 
         getOverlay().add(overlayLabel, new CellConstraints(2, 2));
 
         getContent().setLayout(new BorderLayout(0, 1));
         getContent().add(diagramPane, BorderLayout.CENTER);
         getContent().add(toolbarPane, BorderLayout.PAGE_END);
 
         toolbar.add(firstButtonPage);
         toolbar.add(firstButtonDiagram);
         toolbar.add(prevButtonPage);
         toolbar.add(prevButtonDiagram);
         toolbar.add(pageSelect);
         toolbar.add(nextButtonPage);
         toolbar.add(nextButtonDiagram);
         toolbar.add(lastButtonPage);
         toolbar.add(lastButtonDiagram);
 
         toolbarPane.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 0));
         toolbarPane.add(toolbar);
 
     }
 
     /**
      * Instructs the renderer to display the given origami from now on.
      * 
      * Automatically chooses the first step to be displayed.
      * 
      * @param o The origami to display.
      */
     public synchronized void setOrigami(Origami o)
     {
         setOrigami(o, o.getModel().getSteps().getStep().get(0));
     }
 
     /**
      * Instructs the renderer to display the given origami from the given step.
      * 
      * @param o The origami to display.
      * @param firstStep The step to be displayed as the first one.
      */
     public synchronized void setOrigami(Origami o, Step firstStep)
     {
         setOrigami(o, firstStep, true);
     }
 
     /**
      * Instructs the renderer to display the given origami from the given step.
      * 
      * @param o The origami to display.
      * @param firstStep The step to be displayed as the first one.
      * @param updateSteps Whether to update the displayed steps, or delay it for later.
      */
     protected synchronized void setOrigami(Origami o, Step firstStep, boolean updateSteps)
     {
         this.origami = o;
         setStep(firstStep);
 
         int numSteps = o.getModel().getSteps().getStep().size();
         int stepsPerPage = o.getPaper().getCols() * o.getPaper().getRows();
         int numPages = (int) Math.ceil((double) numSteps / stepsPerPage);
 
         int stepIndex = o.getModel().getSteps().getStep().indexOf(firstStep) + 1;
         int pageIndex = (stepIndex - 1) / stepsPerPage + 1;
 
         Object[] steps = new Object[numSteps + 1];
         stepXofY = new ParametrizedLocalizedString("application", "DiagramRenderer.stepXofY", stepIndex, numSteps);
         steps[0] = stepXofY;
         for (int i = 1; i <= numSteps; i++)
             steps[i] = i;
 
         Object[] pages = new Object[numPages + 1];
         pageXofY = new ParametrizedLocalizedString("application", "DiagramRenderer.pageXofY", pageIndex, numPages);
         pages[0] = pageXofY;
         for (int i = 1; i <= numPages; i++)
             pages[i] = i;
 
         pageSelectDiagramModel = new DefaultComboBoxModel(steps);
         pageSelectPageModel = new DefaultComboBoxModel(pages);
 
         if (updateSteps)
             updateDisplayedSteps();

        setBackground(origami.getPaper().getColor().getBackground());
     }
 
     /**
      * Set the given step as the first (or the only) step to be displayed in this renderer.
      * 
      * @param firstStep The first (or the only) step to be displayed in this renderer.
      */
     public synchronized void setStep(Step firstStep)
     {
         Step oldStep = this.firstStep;
         this.firstStep = firstStep;
         firePropertyChange("firstStep", oldStep, firstStep);
 
         if (stepXofY != null && pageXofY != null) {
             int numSteps = origami.getModel().getSteps().getStep().size();
             int stepsPerPage = origami.getPaper().getCols() * origami.getPaper().getRows();
             int numPages = (int) Math.ceil((double) numSteps / stepsPerPage);
 
             int stepIndex = origami.getModel().getSteps().getStep().indexOf(firstStep) + 1;
             int pageIndex = (stepIndex - 1) / stepsPerPage + 1;
 
             stepXofY.setParameters(stepIndex, numSteps);
             pageXofY.setParameters(pageIndex, numPages);
         }
     }
 
     /**
      * Set the display mode to the one given. If the new mode is different from the actual one, a redraw will be forced.
      * 
      * @param mode The new mode to switch the renderer to.
      */
     public synchronized void setDisplayMode(DisplayMode mode)
     {
         if (mode.equals(this.mode) && displayedStepsPattern != null)
             return;
 
         DisplayMode oldMode = this.mode;
         this.mode = mode;
         firePropertyChange("mode", oldMode, mode);
 
         switch (mode) {
             case DIAGRAM:
                 displayedStepsPattern = SINGLE_STEP_PATTERN;
                 pageSelect.setModel(pageSelectDiagramModel);
 
                 prevButtonDiagram.setVisible(true);
                 prevButtonPage.setVisible(false);
                 nextButtonDiagram.setVisible(true);
                 nextButtonPage.setVisible(false);
                 firstButtonDiagram.setVisible(true);
                 firstButtonPage.setVisible(false);
                 lastButtonDiagram.setVisible(true);
                 lastButtonPage.setVisible(false);
                 break;
             case PAGE:
                 displayedStepsPattern = new Dimension(origami.getPaper().getCols(), origami.getPaper().getRows());
                 pageSelect.setModel(pageSelectPageModel);
 
                 // when switching back to PAGE mode, don't automatically display the previously displayed step as the
                 // first step in the page, but make the page begin with a step that corresponds to the first step of
                 // this page
                 int stepsPerPage = origami.getPaper().getCols() * origami.getPaper().getRows();
                 int stepIndex = origami.getModel().getSteps().getStep().indexOf(firstStep) + 1;
                 int pageIndex = (stepIndex - 1) / stepsPerPage + 1;
                 int newStepIndex = (pageIndex - 1) * stepsPerPage;
                 Step step = origami.getModel().getSteps().getStep().get(newStepIndex);
                 if (step != null)
                     setStep(step);
 
                 prevButtonDiagram.setVisible(false);
                 prevButtonPage.setVisible(true);
                 nextButtonDiagram.setVisible(false);
                 nextButtonPage.setVisible(true);
                 firstButtonDiagram.setVisible(false);
                 firstButtonPage.setVisible(true);
                 lastButtonDiagram.setVisible(false);
                 lastButtonPage.setVisible(true);
                 break;
         }
         updateDisplayedSteps();
     }
 
     /**
      * @return The mode the renderer actually displays the origami in.
      */
     public DisplayMode getDisplayMode()
     {
         return mode;
     }
 
     /**
      * Updates the displayed steps (eg. manages the number of them and repaints).
      */
     protected void updateDisplayedSteps()
     {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run()
             {
                 showOverlay();
 
                 diagramPane.removeAll();
                 stepRenderers.clear();
 
                 diagramPane.setLayout(new GridLayout(displayedStepsPattern.height, displayedStepsPattern.width));
 
                 new Thread() {
                     @Override
                     public void run()
                     {
                         int numSteps = displayedStepsPattern.width * displayedStepsPattern.height;
                         Step step = firstStep;
                         for (int i = 0; i < numSteps; i++) {
                             if (step != null) {
                                 StepRenderer r = new StepRenderer(origami, step);
                                 r.setDisplayMode(getDisplayMode());
                                 diagramPane.add(r);
                                 stepRenderers.add(r);
                                 r.setZoom(zoom);
                                 r = null;
                             } else {
                                JPanel panel = new JPanel();
                                panel.setBackground(origami.getPaper().getColor().getBackground());
                                diagramPane.add(panel);
                             }
                             if (step != null)
                                 step = step.getNext();
                         }
 
                         if (stepRenderers.size() == 0)
                             return; // TODO maybe tell the user that nothing is to be displayed
 
                         if (stepToolbar != null)
                             toolbarPane.remove(stepToolbar);
                         if (mode.equals(DisplayMode.DIAGRAM)) {
                             // in DIAGRAM view we want to add the step's toolbar to the toolbar of this renderer
                             stepToolbar = stepRenderers.get(0).getToolbar();
                             // also detaches the toolbar from the StepRenderer
                             toolbarPane.add(stepToolbar);
                             stepToolbar.setVisible(true);
                         }
                         SwingUtilities.invokeLater(new Runnable() {
                             @Override
                             public void run()
                             {
                                 // this is important!
                                 getContent().revalidate();
                                 hideOverlay();
                             }
                         });
                     }
                 }.start();
             }
         });
     }
 
     /**
      * @return the zoom
      */
     public double getZoom()
     {
         return zoom;
     }
 
     /**
      * @param zoom the zoom to set
      */
     public void setZoom(double zoom)
     {
         double zoomDelta = zoom - this.zoom;
         this.zoom = zoom;
         for (StepRenderer r : stepRenderers) {
             r.setZoom(r.getZoom() + zoomDelta);
         }
         revalidate();
     }
 
     /**
      * Increase zoom by 10%.
      */
     public void incZoom()
     {
         setZoom(getZoom() + 10d);
     }
 
     /**
      * Decrease zoom by 10%.
      */
     public void decZoom()
     {
         setZoom(getZoom() - 10d);
     }
 
     /**
      * Switch to the selected step/page (depends on the current display mode).
      * 
      * @author Martin Pecka
      */
     class PageSelectAction extends AbstractAction
     {
 
         /** */
         private static final long serialVersionUID = -3468061316473710494L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             Object selectedObject = pageSelect.getSelectedItem();
             if (selectedObject instanceof Integer) {
                 Integer index = (Integer) selectedObject;
                 if (getDisplayMode().equals(DisplayMode.DIAGRAM)) {
                     Step step = origami.getModel().getSteps().getStep().get(index - 1);
                     if (step != null) {
                         setStep(step);
                         updateDisplayedSteps();
                     }
                 } else {
                     int stepsPerPage = origami.getPaper().getCols() * origami.getPaper().getRows();
                     int pageIndex = (index - 1) / stepsPerPage + 1;
                     int newStepIndex = (pageIndex - 1) * stepsPerPage;
                     Step step = origami.getModel().getSteps().getStep().get(newStepIndex);
                     if (step != null) {
                         setStep(step);
                         updateDisplayedSteps();
                     }
                 }
             }
             pageSelect.setSelectedIndex(0);
         }
 
     }
 
     /**
      * Sets the first step/page as the step/page to be displayed.
      * 
      * @author Martin Pecka
      */
     class FirstAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = -4456533603941034141L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             if (getDisplayMode().equals(DisplayMode.DIAGRAM) && !firstButtonDiagram.isEnabled())
                 return;
             else if (getDisplayMode().equals(DisplayMode.PAGE) && !firstButtonPage.isEnabled())
                 return;
 
             setStep(origami.getModel().getSteps().getStep().get(0));
             updateDisplayedSteps();
         }
     }
 
     /**
      * Sets the last step/page as the step/page to be displayed.
      * 
      * @author Martin Pecka
      */
     class LastAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = -4456533603941034141L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             if (getDisplayMode().equals(DisplayMode.DIAGRAM)) {
                 if (!lastButtonDiagram.isEnabled())
                     return;
                 setStep(origami.getModel().getSteps().getStep().get(origami.getModel().getSteps().getStep().size() - 1));
             } else {
                 if (!lastButtonPage.isEnabled())
                     return;
                 int numSteps = origami.getModel().getSteps().getStep().size();
                 int stepsPerPage = origami.getPaper().getCols() * origami.getPaper().getRows();
                 int numPages = (int) Math.ceil((double) numSteps / stepsPerPage);
                 int stepIndex = stepsPerPage * numPages;
 
                 setStep(origami.getModel().getSteps().getStep().get(stepIndex));
             }
             updateDisplayedSteps();
         }
     }
 
     /**
      * Sets the previous step/page as the step/page to be displayed.
      * 
      * @author Martin Pecka
      */
     class PrevAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = -7796794346643659044L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             if (getDisplayMode().equals(DisplayMode.DIAGRAM)) {
                 if (!prevButtonDiagram.isEnabled())
                     return;
                 int index = origami.getModel().getSteps().getStep().indexOf(firstStep);
                 if (index > 0) {
                     setStep(origami.getModel().getSteps().getStep().get(index - 1));
                     updateDisplayedSteps();
                 }
             } else {
                 if (!prevButtonPage.isEnabled())
                     return;
                 int stepsPerPage = origami.getPaper().getCols() * origami.getPaper().getRows();
                 int stepIndex = origami.getModel().getSteps().getStep().indexOf(firstStep) + 1;
                 int pageIndex = (stepIndex - 1) / stepsPerPage;
 
                 if (pageIndex > 0) {
                     setStep(origami.getModel().getSteps().getStep().get((pageIndex - 1) * stepsPerPage));
                     updateDisplayedSteps();
                 }
             }
         }
     }
 
     /**
      * Sets the next step/page as the step/page to be displayed.
      * 
      * @author Martin Pecka
      */
     class NextAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = 6137420423152101455L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             if (getDisplayMode().equals(DisplayMode.DIAGRAM)) {
                 if (!nextButtonDiagram.isEnabled())
                     return;
                 int index = origami.getModel().getSteps().getStep().indexOf(firstStep);
                 if (index < origami.getModel().getSteps().getStep().size() - 1) {
                     setStep(origami.getModel().getSteps().getStep().get(index + 1));
                     updateDisplayedSteps();
                 }
             } else {
                 if (!nextButtonPage.isEnabled())
                     return;
                 int numSteps = origami.getModel().getSteps().getStep().size();
                 int stepsPerPage = origami.getPaper().getCols() * origami.getPaper().getRows();
                 int numPages = (int) Math.ceil((double) numSteps / stepsPerPage);
 
                 int stepIndex = origami.getModel().getSteps().getStep().indexOf(firstStep) + 1;
                 int pageIndex = (stepIndex - 1) / stepsPerPage;
 
                 if (pageIndex < numPages) {
                     setStep(origami.getModel().getSteps().getStep().get((pageIndex + 1) * stepsPerPage));
                     updateDisplayedSteps();
                 }
             }
         }
     }
 
 }
