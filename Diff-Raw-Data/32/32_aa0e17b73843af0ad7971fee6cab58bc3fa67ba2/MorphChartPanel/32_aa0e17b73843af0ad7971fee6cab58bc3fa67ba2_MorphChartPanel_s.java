 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.honeybadgers.flltutorial.ui.main.content.stages;
 
 import com.honeybadgers.flltutorial.model.Option;
 import com.honeybadgers.flltutorial.model.OptionTracker;
 import com.honeybadgers.flltutorial.model.backend.TutorialManager;
 import com.honeybadgers.flltutorial.ui.main.content.OptionsPanel;
 import com.honeybadgers.flltutorial.ui.main.content.OptionsSelectorPanel;
 import com.honeybadgers.flltutorial.ui.main.content.utilities.OptionPanel;
 import com.honeybadgers.flltutorial.ui.main.content.utilities.PictureOptionPanel;
 import com.honeybadgers.flltutorial.ui.main.content.utilities.TextOptionPanel;
 import com.honeybadgers.flltutorial.ui.utilities.PanelsScrollPane;
 import java.awt.AWTEvent;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.RenderingHints;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.awt.image.ConvolveOp;
 import java.awt.image.Kernel;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 
 /**
  * This class display a morph chart, with a specified number of rows, where a selected row
  * can be chosen and then be filled in with the correct answers.
  * 
  * @author chingaman
  */
 public class MorphChartPanel extends StagePanel implements MouseListener{
     private final HashMap panelForIndexHashes;
     private BufferedImage scrollImage;
     private BufferedImage closeImage;
 
     
     protected enum BeaconType
     {
         //ChildPanel,
         SelectedParentPanel,
         ParentPanel
     };
     protected PanelsScrollPane scrollPane;
     protected OptionPanel selectedPanel;
     protected OptionPanel mainOptionPanel;
     /*used to look up the kind of panel that was clicked*/
     protected HashMap panelTypeHashes;
     protected HashMap indexesHashes;
     protected HashMap indexesChildrenHashes;
     protected List<List<OptionPanel>> listsOptionPanels;
     private int type = 0;
     public MorphChartPanel(Option rootOption)
     {
         super();
        this.stageName = "Morph Chart";
         //this.morphChartGenerator();
         this.problem = rootOption;
         this.solutionTracker = OptionTracker.generateOptionTrackerTree(rootOption);
         //show the options of the first child
         this.type = 1;
         List<OptionPanel> optionPanels = this.generateOptionPanels(this.solutionTracker.getCorrectChild(0), 1);
         this.optionsPanel = new OptionsSelectorPanel(optionPanels);
         this.panelTypeHashes = new HashMap();
         this.panelForIndexHashes = new HashMap();
         this.indexesHashes = new HashMap();
         this.indexesChildrenHashes = new HashMap();
         this.listsOptionPanels = new ArrayList<>();
         if(this.getClass() == MorphChartPanel.class)
         {
             this.initComponents();
         }
         try {
                 this.scrollImage = ImageIO.read(new File(TutorialManager.generalMediaPath+"scroll.png"));
                 this.closeImage = ImageIO.read(new File(TutorialManager.generalMediaPath+"click.png"));
         } catch (IOException ex) {
             Logger.getLogger(PictureOptionPanel.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     protected void initComponents()
     {
         //setup title of stage
         JTextArea titleLabel = new JTextArea(this.stageName);
         titleLabel.setLineWrap(true);
         titleLabel.setWrapStyleWord(true);
         titleLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
         //titleLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
         titleLabel.setBackground(Color.GREEN);
         
         //add title to selector problem
         this.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         c.gridx = 0;
         c.gridy = 0;
         c.insets = new Insets(10, 4, 4, 4);
         c.ipady = 0;
         c.weightx = 1.0;
         c.anchor = GridBagConstraints.PAGE_START;
         this.add(titleLabel, c);
         
         //add main option description option panel
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         c.gridx = 0;
         c.gridy = 1;
         c.insets = new Insets(10, 4, 4, 4);
         c.ipady = 0;
         c.weightx = 1.0;
         c.anchor = GridBagConstraints.PAGE_START;
         this.mainOptionPanel = new TextOptionPanel(this.problem);
         this.add(this.mainOptionPanel, c);
         
         //setup scroll pane
         this.scrollPane = new PanelsScrollPane(true);
         
             //add row panels to scroll pane
             int i = 0;
             this.type = 0;
             for(OptionPanel leftMostOptionPanel : this.generateOptionPanels(this.solutionTracker, 1))
             {
                 //setup beacon for top most elements
                 Component topBeacon = leftMostOptionPanel.getBeacon();
                 topBeacon.addMouseListener(this);
                 if(i == 0)
                 {
                     this.selectedPanel = leftMostOptionPanel;
                     this.panelTypeHashes.put(topBeacon, BeaconType.SelectedParentPanel);
                 }
                 else
                 {
                    this.panelTypeHashes.put(topBeacon, BeaconType.ParentPanel);
                 }
                 this.indexesHashes.put(leftMostOptionPanel, i);
                 this.panelForIndexHashes.put(i, leftMostOptionPanel);
                 if(this.solutionTracker.getCorrectChild(i).isFinished())
                 {
                     leftMostOptionPanel.setState(OptionPanel.OptionState.FINISHED);
                 }
                 //create a JPanel with one elements at the front, and then create empty
                 //option panels in a sub panel. All of the options panels in the subpanel
                 //will be equally sized
 
                 JPanel rowPanel = new BlurryPanel();
                 rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
                 rowPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
                 rowPanel.setBackground(Color.GRAY);
                 
                 rowPanel.add(Box.createRigidArea(new Dimension(0,100)));
                 
                     leftMostOptionPanel.setPreferredSize(new Dimension(100,100));
                     leftMostOptionPanel.setMaximumSize(new Dimension(100,100));
                     
                 rowPanel.add(leftMostOptionPanel);
                 
                     //setup the right children panel
                     PanelsScrollPane childrenPanel = new PanelsScrollPane(false);//new JPanel(new GridLayout(0,1,2,2));
                 
                 rowPanel.add(childrenPanel);
                 
                         //add panels to children panel
                         this.type = 1;
                         List<OptionPanel> optionPanelsChilds = this.generateOptionPanels(this.solutionTracker.getCorrectChild(i),0);
                         this.listsOptionPanels.add(optionPanelsChilds);
                         if(i == 0)
                         {
                             int j = 0;
                             for(OptionPanel childPanel : optionPanelsChilds)
                             {
                                 this.indexesChildrenHashes.put(childPanel.getBeacon(), j);
                                 j++;
                             }
                         }
                         for(OptionPanel childPanel : optionPanelsChilds)
                         {
                             childrenPanel.appendPanel(childPanel);
                         }
                         
                 this.scrollPane.appendPanel(rowPanel);
                 rowPanel.revalidate();
                 i++;
             }
     
         //add scroll pane 
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         c.gridx = 0;
         c.gridy = 2;
         c.insets = new Insets(4, 4, 4, 4);
         c.ipady = 0;
         c.weightx = 1.0;
         c.weighty = 1.0;
         c.anchor = GridBagConstraints.PAGE_START;
         this.add(this.scrollPane, c);
         
         this.setBackground(Color.GRAY);
         this.revalidate();
         this.scrollPane.revalidate();
         this.repaint();
         ((BlurryPanel)this.selectedPanel.getParent()).setBlurry(0);
     }
     
     @Override
     protected OptionPanel createOptionPanel(Option option)
     {
         //System.out.println("Generated in Morph State "+option.getImagePath());
         if(this.type == 0)
         {
             return new TextOptionPanel(option);
         }
         else
         {
             return new PictureOptionPanel(option);
         }
         //return new TextOptionPanel(option);
     } 
     
     private void morphChartGenerator()
     {
         Option morphChartOption = new Option("Click a row, then chose whichever options you think will enable you to do the given functionality", true);
         OptionTracker morphChartTracker;
         //create an array of rows for the chart
         for(int i = 0; i < 8; i++)
         {
             Option rowOption = new Option("functionality "+i, true);
             for(int j = 0; j < (((i % 4) == 0) ? 8 : 3); j++)
             {
                 Option childOption = new Option("you should clearly choose this options becuase all the others just don't make sense", ((j % 3) != 0));
                 rowOption.addChild(childOption);
             }
             morphChartOption.addChild(rowOption);
         }
         morphChartTracker = new OptionTracker(morphChartOption);
         int k = 0;
         for(Option rowOption : morphChartTracker.getOption().getOptions())
         {
             morphChartTracker.addOptionAt(k, rowOption);
             k++;
         }
         
         //assign problem and tracker
         this.problem = morphChartOption;
         this.solutionTracker = morphChartTracker;
     }
     /**
      * Returns the kind of options panel used to give the user multiple options as
      * possible answers.
      * 
      * @return 
      */
     @Override
     public OptionsPanel getOptionsPanel() {
         return this.optionsPanel;
     }
     /**
      * The panel will be dropped on the row which is currently selected, and checked
      * for correctness.
      * 
      * @param optionPanel
      * @return 
      */
     @Override
     public int dropOptionPanel(OptionPanel optionPanel) {
         //check whether panel falls in any of the child option panels, of the currently
         //selected panel.
         int x = (int)optionPanel.getBounds().getCenterX();
         int y = (int)optionPanel.getBounds().getCenterY();
         
         Component beacon = SwingUtilities.getDeepestComponentAt(this, x, y);
         if(beacon == null)
         {
             return 2;
         }
         
         //if it returns null it means the children are not childs of the the selection option
         Object index = this.indexesChildrenHashes.get(beacon);
         if(index == null)
         {
             return 2;
         }
         
         
         int childIndex = (int)index;
         int selectedIndex = (int)this.indexesHashes.get(this.selectedPanel);
 
         OptionPanel dropOptionPanel = OptionPanel.getOptionPanelFromBeacon(beacon);
         Option dropOption = optionPanel.getOption();
         OptionTracker parentOptionTracker = solutionTracker.getCorrectChild(selectedIndex);
         boolean addedCorrectly = parentOptionTracker.addOptionAt(childIndex, dropOption);
         if(addedCorrectly)
         {
             System.out.println("MorphChartPanel.mouseClicked : dropped correctly");
             if(dropOption.isCorrect())
             {
                 OptionTracker childOptionTracker = parentOptionTracker.getCorrectChild(childIndex);
                 dropOptionPanel.transferOption(dropOption);
                 if(childOptionTracker.isFinished())
                 {
                     dropOptionPanel.setState(OptionPanel.OptionState.FINISHED);
                     //update parent colors here
                 }
                 else
                 {
                     dropOptionPanel.setState(OptionPanel.OptionState.CORRECT);
                 }
                 
                 if(parentOptionTracker.isFinished())
                 {
                     OptionPanel parentPanel = (OptionPanel)this.panelForIndexHashes.get(selectedIndex);
                     parentPanel.setState(OptionPanel.OptionState.FINISHED);
                     parentPanel.repaint();
                     if(parentOptionTracker.getParent().isFinished())
                     {
                         this.mainOptionPanel.setState(OptionPanel.OptionState.FINISHED);
                         this.mainOptionPanel.repaint();
                     }
                    
                 }
                 
                 this.revalidate();
                 return 0;
             }
             else
             {
                 return 1;
             }
         }
         return 2;
     }
     
     @Override
     public void scrolled(AWTEvent e) {
         this.scrollPane.dispatchEvent(e);
     }
     
     @Override
     public void mouseClicked(MouseEvent e) {
         System.out.println("MorphChartPanel.mouseClicked : clicked something");
 
         Component beacon = (Component)e.getSource();
         if(beacon == null)
         {
             System.out.println("MorphChartPanel.mouseClicked : got a null beacon");
             return;
         }
         Object result = this.panelTypeHashes.get(beacon);
         if(result == null || !(result instanceof BeaconType))
         {
             System.out.println("MorphChartPanel.mouseClicked : no result for beacon");
             return;
         }
         BeaconType beaconType = (BeaconType)result;
         switch(beaconType)
         {
             case SelectedParentPanel:
                 //this does nothing, because the parent panel is already selected
                 //System.out.println("MorphChartPanel.mouseClicked : clicked selected parent panel");
                 break;
             case ParentPanel:
                 //this changes the options panel, and adds it's own children to it. Also All child
                 //panels are deleted from hash, and new ones are added. Parent becomes the selected parent panel.
                 this.panelTypeHashes.remove(this.selectedPanel.getBeacon());
                 this.panelTypeHashes.put(this.selectedPanel.getBeacon(), BeaconType.ParentPanel);
                 //make the previous selected panel blurry
                 ((BlurryPanel)this.selectedPanel.getParent()).setBlurry(1);
                 this.selectedPanel = OptionPanel.getOptionPanelFromBeacon(beacon);
                 //make the new panel not blurry
                 ((BlurryPanel)this.selectedPanel.getParent()).setBlurry(0);
                 this.panelTypeHashes.remove(beacon);
                 this.panelTypeHashes.put(beacon, BeaconType.SelectedParentPanel);
                 
                 int index = (int)this.indexesHashes.get(this.selectedPanel);
                 this.type = 1;
                 List<OptionPanel> optionPanels = this.generateOptionPanels(solutionTracker.getCorrectChild(index), 1);
                 ((OptionsSelectorPanel)this.optionsPanel).updateOptionPanels(optionPanels);
                 //System.out.println("MorphChartPanel.mouseClicked : clicked parent panel");
                 
                 //load new child indexes for fast checking
                 int j = 0;
                 this.indexesChildrenHashes.clear();
                 for(OptionPanel optionPanel : this.listsOptionPanels.get(index))
                 {
                     this.indexesChildrenHashes.put(optionPanel.getBeacon(), j);
                     j++;
                 }
                 break;
             default:
                 break;
         }
         
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
     
     private class BlurryPanel extends JPanel
     {
         int blurry = 0;
         private BufferedImage panelImage;
         public BlurryPanel()
         {
             blurry = 1;
             this.addComponentListener(new ComponentAdapter(){   
                 @Override
                 public void componentResized(ComponentEvent e){
                     repaint();
                 }
             });
         }
         public void setBlurry(int i)
         {
             blurry = i;
             this.repaint();
         }
         
         @Override
         public void paint(Graphics g)
         {
             int radius = 7;
             float scrollScale = 0.20f;
             float clickScale = 0.25f;
             if(this.blurry == 1)
             {
                 if(panelImage == null || panelImage.getWidth() != this.getWidth() || panelImage.getHeight() != this.getHeight())
                 {
                     this.panelImage = (BufferedImage)this.createImage(this.getWidth(), this.getHeight());
                 }
                 int x0 = 2*radius, y0 = 2*radius;
                 Graphics gPanel = this.panelImage.getGraphics();
                 gPanel.setClip(g.getClip());
                 //super.repaint(); this fixes but has horrible performance
                 super.paint(gPanel);
                 gPanel.dispose();
                 
                 BufferedImage preImage = ((BufferedImage)this.createImage(this.getWidth()+(2*x0), this.getHeight()+(2*y0)));
                 Graphics2D g2Pre = (Graphics2D)preImage.getGraphics();
                 g2Pre.setColor(Color.GRAY);
                 g2Pre.fillRect(0, 0, preImage.getWidth(), preImage.getHeight());
                 g2Pre.drawImage(this.panelImage, x0, y0, null);
                 this.panelImage = preImage;
                 this.panelImage = getGaussianBlurFilter(radius, true).filter(this.panelImage, null);
                 this.panelImage = getGaussianBlurFilter(radius, false).filter(this.panelImage, null);
                 
                 Graphics2D g2 = (Graphics2D)g;
                 g2.drawImage(this.panelImage, -x0, -y0, null);
                 
                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 //show scroll image
                 AffineTransform transformer0 = new AffineTransform();
                 transformer0.translate(47, 5);
                 transformer0.scale(scrollScale, scrollScale);
                 g2.drawImage(scrollImage, transformer0, null);
                 //show 
                 AffineTransform transformer1 = new AffineTransform();
                 transformer1.translate(104, 47);
                 transformer1.scale(clickScale, clickScale);
                 g2.drawImage(closeImage, transformer1, null);
             }
             else
             {
                 super.paint(g);
             }
         }
         private ConvolveOp getGaussianBlurFilter(int radius, boolean horizontal) {
             if (radius < 1) {
                 throw new IllegalArgumentException("Radius must be >= 1");
             }
 
             int size = radius * 2 + 1;
             float[] data = new float[size];
 
             float sigma = radius / 3.0f;
             float twoSigmaSquare = 2.0f * sigma * sigma;
             float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
             float total = 0.0f;
 
             for (int i = -radius; i <= radius; i++) {
                 float distance = i * i;
                 int index = i + radius;
                 data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
                 total += data[index];
             }
 
             for (int i = 0; i < data.length; i++) {
                 data[i] /= total;
             }        
 
             Kernel kernel = null;
             if (horizontal) {
                 kernel = new Kernel(size, 1, data);
             } else {
                 kernel = new Kernel(1, size, data);
             }
             return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
         }
     }
 }
