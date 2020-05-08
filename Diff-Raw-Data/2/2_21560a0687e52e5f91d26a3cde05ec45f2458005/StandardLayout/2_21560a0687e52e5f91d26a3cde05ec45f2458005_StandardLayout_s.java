 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.description.layouts;
 
 import com.jme.system.DisplaySystem;
 import com.jmex.bui.BImage;
 import com.jmex.bui.BLabel;
 import com.jmex.bui.icon.ImageIcon;
 import com.jmex.bui.text.HTMLView;
 import com.jmex.bui.util.Dimension;
 import edu.gatech.statics.modes.description.Description;
 import edu.gatech.statics.modes.description.DescriptionLayout;
 import edu.gatech.statics.modes.description.DescriptionUI;
 import edu.gatech.statics.ui.maintabbar.MainTabBar;
 
 /**
  * This layout requires that the description have a title, narrative, problem statement,
  * and two images.
  * @author Calvin Ashmore
  */
 public class StandardLayout implements DescriptionLayout {
 
     //private BLabel titleLabel;
     private HTMLView narrativeView;
     private HTMLView problemStatementView;
     private HTMLView goalsView;
     private BLabel imageLabel1;
     //private BLabel imageLabel2;
     private SlideshowControl slideshow;
 
     private static final int IMAGE_WIDTH = 550;
     private static final int IMAGE_HEIGHT = 325;
 
     public void addComponents(DescriptionUI ui) {
 
         imageLabel1 = new BLabel("");
         //imageLabel2 = new BLabel("");
         ui.add(imageLabel1);
         //ui.add(imageLabel2);
 
         slideshow = new SlideshowControl(IMAGE_WIDTH, IMAGE_HEIGHT);
         ui.add(slideshow);
 
 //        titleLabel = new BLabel("");
 //        ui.add(titleLabel);
 
         narrativeView = new HTMLView();
         ui.add(narrativeView);
 
         problemStatementView = new HTMLView();
         ui.add(problemStatementView);
 
         goalsView = new HTMLView();
         ui.add(goalsView);
     }
 
    @Override
     public void layout(DescriptionUI ui, Description description) {
 
         String title = description.getTitle();
         String goals = description.getGoals();
         String narrative = description.getNarrative();
         String problemStatement = description.getProblemStatement();
 
 
         int displayHeight = DisplaySystem.getDisplaySystem().getHeight();
         int displayWidth = DisplaySystem.getDisplaySystem().getWidth();
 
 
         Dimension preferredSize;
 
         int yOffset = displayHeight - MainTabBar.MAIN_TAB_BAR_HEIGHT;
         int xOffset = 600;
         int textWidth = 400;
 
 
         int spacing = 25;
 
 //        titleLabel.setText(title);
 //        preferredSize = titleLabel.getPreferredSize(-1, -1);
 //        yOffset -= preferredSize.height + spacing;
 //        titleLabel.setBounds(xOffset, yOffset, preferredSize.width, preferredSize.height);
 //        titleLabel.setBorder(new LineBorder(ColorRGBA.blue, 1));
 
         if (narrative != null) {
             narrativeView.setContents(narrative);
             narrativeView.getStyleSheet().addRule("body {font-size: 115%}");
             preferredSize = narrativeView.getPreferredSize(textWidth, -1);
             yOffset -= preferredSize.height + spacing;
             narrativeView.setBounds(xOffset, yOffset, textWidth, preferredSize.height);
             //narrativeView.setBorder(new LineBorder(ColorRGBA.blue, 1));
         }
 
         if (problemStatement != null) {
             problemStatementView.setContents(problemStatement);
             problemStatementView.getStyleSheet().addRule("body {font-size: 115%}");
             preferredSize = problemStatementView.getPreferredSize(textWidth, -1);
             yOffset -= preferredSize.height + spacing;
             problemStatementView.setBounds(xOffset, yOffset, textWidth, preferredSize.height);
             //problemStatementView.setBorder(new LineBorder(ColorRGBA.blue, 1));
         }
         if (goals != null) {
             goalsView.setContents(goals);
             goalsView.getStyleSheet().addRule("body {font-size: 115%}");
             preferredSize = goalsView.getPreferredSize(400, -1);
             yOffset -= preferredSize.height + spacing;
             goalsView.setBounds(xOffset, yOffset, textWidth, preferredSize.height);
             //goalsView.setBorder(new LineBorder(ColorRGBA.blue, 1));
         }
         preferredSize = ui.getButton().getPreferredSize(-1, -1);
         //yOffset -= preferredSize.height + 50;
 
         ui.getButton().setBounds(displayWidth - preferredSize.width - spacing, spacing, preferredSize.width, preferredSize.height);
 
         if (description.getImages() != null && description.getImages().size() >= 2) {
 
             imageLabel1.setIcon(new ImageIcon(new BImage(description.getImages().get(0))));
             preferredSize = imageLabel1.getPreferredSize(-1, -1);
             //imageLabel1.setBounds(25, 25, preferredSize.width, preferredSize.height);
             imageLabel1.setBounds(spacing, spacing, IMAGE_WIDTH, IMAGE_HEIGHT);
 
             slideshow.setImages(description.getImages().subList(1, description.getImages().size()));
             slideshow.setBounds(spacing, 2 * spacing + IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
 
 //            imageLabel2.setIcon(new ImageIcon(new BImage(description.getImages().get(1))));
 //            preferredSize = imageLabel2.getPreferredSize(-1, -1);
 //            //imageLabel2.setBounds(25, 400, preferredSize.width, preferredSize.height);
 //            imageLabel2.setBounds(spacing, 2 * spacing + IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
         }
     }
 }
