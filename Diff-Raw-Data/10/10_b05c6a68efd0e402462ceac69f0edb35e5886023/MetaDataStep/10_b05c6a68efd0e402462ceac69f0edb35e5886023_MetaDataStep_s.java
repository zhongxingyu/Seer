 /* 
  * @(#)MetaDataStep.java
  * 
  * Copyright (c) 2005-2007 by Blip Networks, Inc.
  * 239 Centre St, 3rd Floor
  * New York, NY 10013
  * All rights reserved.
  *
  * This software is the confidential and
  * proprietary information of Blip Networks, Inc.
  */
 
 package com.blipnetworks.upperblip;
 
 import java.awt.*;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseEvent;
 import java.io.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 import com.blipnetworks.util.I18n;
 import com.blipnetworks.util.MetadataLoader;
 import com.blipnetworks.util.Command;
 
 import org.pietschy.wizard.AbstractWizardStep;
 import org.pietschy.wizard.WizardModel;
 
 /**
  *
  *
  * @author Jared Klett
 * @version $Id: MetaDataStep.java,v 1.32 2007/05/18 19:48:23 jklett Exp $
  */
 
 public class MetaDataStep extends AbstractWizardStep {
 
 // CVS info ////////////////////////////////////////////////////////////////////
 
    public static final String CVS_REV = "$Revision: 1.32 $";
 
 // Static variables ////////////////////////////////////////////////////////////
 
     /** blah */
     private static final String TITLE_KEY = "meta.title";
     /** blah */
     private static final String SUMMARY_KEY = "meta.summary";
     /** blah */
     private static final String FILE_LABEL_KEY = "meta.file.label";
     /** blah */
     private static final String TITLE_LABEL_KEY = "meta.titlefield.label";
     /** blah */
     private static final String DESC_LABEL_KEY = "meta.desc.label";
     /** blah */
     private static final String THUMB_LABEL_KEY = "meta.thumb.label";
     /** blah */
     private static final String LICENSE_LABEL_KEY = "meta.license.label";
     /** blah */
     private static final String RATING_LABEL_KEY = "meta.rating.label";
     /** blah */
     private static final String EXPLICIT_LABEL_KEY = "meta.explicit.label";
     /** blah */
     private static final String LANGUAGE_LABEL_KEY = "meta.language.label";
     /** blah */
     private static final String TAGS_LABEL_KEY = "meta.tags.label";
     /** blah */
     private static final String CATEGORY_LABEL_KEY = "meta.category.label";
     /** blah */
     private static final String APPLY_LABEL_KEY = "meta.apply.label";
     /** blah */
     private static final String DIST_LABEL_KEY = "meta.dist.label";
     /** blah */
     private static final String BLOGS_LABEL_KEY = "meta.blogs.label";
     /** blah */
     private static final String XUPLOADS_LABEL_KEY = "meta.xuploads.label";
 
 // Instance variables //////////////////////////////////////////////////////////
 
     /** */
     private JPanel view;
     /** */
     private JTextField[] titleList;
     /** */
     private JTextArea[] descList;
     /** */
     private JComboBox[] thumbList;
     /** */
     private JComboBox[] categoryList;
     /** */
     private JComboBox[] licenseList;
     /** */
     private JComboBox[] languageList;
     /** */
     private JComboBox[] ratingList;
     /** */
     private JCheckBox[] explicitList;
     /** */
     private JCheckBox[][] blogCheckboxList;
     /** */
     private JCheckBox[][] destCheckboxList;
     /** */
     private JTextField[] tagsList;
     /** */
     private UpperBlipModel model;
 
 // Constructor /////////////////////////////////////////////////////////////////
 
     public MetaDataStep() {
         super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));
         view = new JPanel();
         setIcon(Icons.metadataIcon);
         setComplete(true);
     }
 
     public void init(WizardModel model) {
         this.model = (UpperBlipModel)model;
     }
 
     public void prepare() {
         view.removeAll();
         // Create and layout components
         JPanel overall = new JPanel();
         GridBagLayout gbl = new GridBagLayout();
         GridBagConstraints gbc = new GridBagConstraints();
         overall.setLayout(gbl);
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.insets.top = 4;
         gbc.insets.bottom = 4;
 
         File[] files = model.getFiles();
         titleList = new JTextField[files.length];
         descList = new JTextArea[files.length];
         thumbList = new JComboBox[files.length];
         categoryList = new JComboBox[files.length];
         licenseList = new JComboBox[files.length];
         ratingList = new JComboBox[files.length];
         explicitList = new JCheckBox[files.length];
         languageList = new JComboBox[files.length];
         tagsList = new JTextField[files.length];
         // TODO: break type array out
         String[] blogNames = (String[])MetadataLoader.blogs.keySet().toArray(new String[0]);
         String[] destinations = (String[])MetadataLoader.crossuploads.keySet().toArray(new String[0]);
         blogCheckboxList = new JCheckBox[files.length][blogNames.length];
         destCheckboxList = new JCheckBox[files.length][destinations.length];
 
         for (int i = 0; i < files.length; i++) {
             JPanel panel = new JPanel();
             // Set up a border
             panel.setBorder(
                 BorderFactory.createCompoundBorder(
                     BorderFactory.createCompoundBorder(
                         BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                         //BorderFactory.createTitledBorder(files[i].getName()),
                         BorderFactory.createEmptyBorder(5, 5, 5, 5)
                     ),
                     panel.getBorder()
                 )
             );
             JLabel fileLabel = new JLabel(I18n.getString(FILE_LABEL_KEY));
             JLabel fileNameLabel = new JLabel(
                     files[i].getName(),
                     Icons.getIconForFilename(files[i].getName()),
                     SwingConstants.LEFT
             );
             JLabel titleLabel = new JLabel(I18n.getString(TITLE_LABEL_KEY));
             JLabel descLabel = new JLabel(I18n.getString(DESC_LABEL_KEY));
             JLabel thumbLabel = new JLabel(I18n.getString(THUMB_LABEL_KEY));
             JLabel licenseLabel = new JLabel(I18n.getString(LICENSE_LABEL_KEY));
             JLabel ratingLabel = new JLabel(I18n.getString(RATING_LABEL_KEY));
             JLabel languageLabel = new JLabel(I18n.getString(LANGUAGE_LABEL_KEY));
             JLabel tagsLabel = new JLabel(I18n.getString(TAGS_LABEL_KEY));
             JLabel categoryLabel = new JLabel(I18n.getString(CATEGORY_LABEL_KEY));
             final JTextField titleField = new JTextField(10);
             LinkLabel applyTitleLabel = new LinkLabel(
                     I18n.getString(APPLY_LABEL_KEY),
                     new Command() {
                         public void execute() {
                             String title = titleField.getText();
                             for (int i = 0; i < titleList.length; i++)
                                 titleList[i].setText(title);
                         }
                     }
             );
             final JTextArea descArea = new JTextArea(5, 10);
             LinkLabel applyDescLabel = new LinkLabel(
                     I18n.getString(APPLY_LABEL_KEY),
                     new Command() {
                         public void execute() {
                             String desc = descArea.getText();
                             for (int i = 0; i < descList.length; i++)
                                 descList[i].setText(desc);
                         }
                     }
             );
             final JComboBox thumbnails = new JComboBox(model.getImageFilenames());
             LinkLabel applyThumbnailLabel = new LinkLabel(
                     I18n.getString(APPLY_LABEL_KEY),
                     new Command() {
                         public void execute() {
                             int index = thumbnails.getSelectedIndex();
                             for (int i = 0; i < thumbList.length; i++)
                                 thumbList[i].setSelectedIndex(index);
                         }
                     }
             );
             final JComboBox categories = new JComboBox(MetadataLoader.categories.keySet().toArray());
             // TODO: load this from the config
             categories.setSelectedItem("Default Category");
             LinkLabel applyCatLabel = new LinkLabel(
                     I18n.getString(APPLY_LABEL_KEY),
                     new Command() {
                         public void execute() {
                             int index = categories.getSelectedIndex();
                             for (int i = 0; i < categoryList.length; i++)
                                 categoryList[i].setSelectedIndex(index);
                         }
                     }
             );
             final JComboBox licenses = new JComboBox(MetadataLoader.licenses.keySet().toArray());
             LinkLabel applyLicenseLabel = new LinkLabel(
                     I18n.getString(APPLY_LABEL_KEY),
                     new Command() {
                         public void execute() {
                             int index = licenses.getSelectedIndex();
                             for (int i = 0; i < licenseList.length; i++)
                                 licenseList[i].setSelectedIndex(index);
                         }
                     }
             );
             final JComboBox ratings = new JComboBox(MetadataLoader.ratings.keySet().toArray());
             LinkLabel applyRatingLabel = new LinkLabel(
                     I18n.getString(APPLY_LABEL_KEY),
                     new Command() {
                         public void execute() {
                             int index = ratings.getSelectedIndex();
                             for (int i = 0; i < ratingList.length; i++)
                                 ratingList[i].setSelectedIndex(index);
                         }
                     }
             );
             final JCheckBox explicit = new JCheckBox(I18n.getString(EXPLICIT_LABEL_KEY));
             LinkLabel applyExplicitLabel = new LinkLabel(
                     I18n.getString(APPLY_LABEL_KEY),
                     new Command() {
                         public void execute() {
                             boolean selected = explicit.isSelected();
                             for (int i = 0; i < explicitList.length; i++)
                                 explicitList[i].setSelected(selected);
                         }
                     }
             );
             final JComboBox languages = new JComboBox(MetadataLoader.languages.keySet().toArray());
             languages.setSelectedItem("English");
             LinkLabel applyLanguageLabel = new LinkLabel(
                     I18n.getString(APPLY_LABEL_KEY),
                     new Command() {
                         public void execute() {
                             int index = languages.getSelectedIndex();
                             for (int i = 0; i < languageList.length; i++)
                                 languageList[i].setSelectedIndex(index);
                         }
                     }
             );
             final JTextField tagsField = new JTextField(10);
             LinkLabel applyTagsLabel = new LinkLabel(
                     I18n.getString(APPLY_LABEL_KEY),
                     new Command() {
                         public void execute() {
                             String tags = tagsField.getText();
                             for (int i = 0; i < tagsList.length; i++)
                                 tagsList[i].setText(tags);
                         }
                     }
             );
             for (int j = 0; j < blogCheckboxList[i].length; j++)
                 blogCheckboxList[i][j] = new JCheckBox(blogNames[j], false);
             for (int j = 0; j < destCheckboxList[i].length; j++)
                 destCheckboxList[i][j] = new JCheckBox(destinations[j], false);
             descArea.setLineWrap(true);
             descArea.setWrapStyleWord(true);
             JScrollPane jsp = new JScrollPane(descArea);
             jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             // Track these components in lists
             titleList[i] = titleField;
             descList[i] = descArea;
             thumbList[i] = thumbnails;
             tagsList[i] = tagsField;
             licenseList[i] = licenses;
             categoryList[i] = categories;
             ratingList[i] = ratings;
             explicitList[i] = explicit;
             languageList[i] = languages;
             // Layout for the internal panel
             GridBagLayout gbl2 = new GridBagLayout();
             GridBagConstraints gbc2 = new GridBagConstraints();
             panel.setLayout(gbl2);
             gbc2.gridx = 0;
             gbc2.gridy = 0;
             gbc2.insets.top = 2;
             gbc2.insets.bottom = 2;
             gbc2.insets.left = 4;
             gbc2.insets.right = 4;
             gbc2.anchor = GridBagConstraints.NORTHEAST;
             gbl2.setConstraints(fileLabel, gbc2);
             panel.add(fileLabel);
             gbc2.gridx = 1;
             //gbc2.gridy = 0;
             gbc2.anchor = GridBagConstraints.NORTHWEST;
             gbl2.setConstraints(fileNameLabel, gbc2);
             panel.add(fileNameLabel);
             gbc2.gridx = 0;
             gbc2.gridy = 1;
             gbc2.anchor = GridBagConstraints.NORTHEAST;
             gbl2.setConstraints(titleLabel, gbc2);
             panel.add(titleLabel);
             gbc2.gridx = 1;
             //gbc2.gridy = 0;
             gbc2.anchor = GridBagConstraints.NORTHWEST;
             gbc2.fill = GridBagConstraints.HORIZONTAL;
             gbl2.setConstraints(titleField, gbc2);
             panel.add(titleField);
             gbc2.gridx = 2;
             gbl2.setConstraints(applyTitleLabel, gbc2);
             panel.add(applyTitleLabel);
             gbc2.gridx = 0;
             gbc2.gridy = 2;
             gbc2.anchor = GridBagConstraints.NORTHEAST;
             gbc2.fill = GridBagConstraints.NONE;
             gbl2.setConstraints(descLabel, gbc2);
             panel.add(descLabel);
             gbc2.gridx = 1;
             //gbc2.gridy = 1;
             gbc2.anchor = GridBagConstraints.NORTHWEST;
             gbc2.fill = GridBagConstraints.HORIZONTAL;
             gbl2.setConstraints(jsp, gbc2);
             panel.add(jsp);
             gbc2.gridx = 2;
             gbl2.setConstraints(applyDescLabel, gbc2);
             panel.add(applyDescLabel);
             gbc2.gridx = 0;
             gbc2.gridy = 3;
             gbc2.anchor = GridBagConstraints.EAST;
             gbc2.fill = GridBagConstraints.NONE;
             gbl2.setConstraints(thumbLabel, gbc2);
             panel.add(thumbLabel);
             gbc2.gridx = 1;
             //gbc2.gridy = 1;
             gbc2.anchor = GridBagConstraints.WEST;
             gbl2.setConstraints(thumbnails, gbc2);
             panel.add(thumbnails);
             gbc2.gridx = 2;
             gbl2.setConstraints(applyThumbnailLabel, gbc2);
             panel.add(applyThumbnailLabel);
             gbc2.gridx = 0;
             gbc2.gridy = 4;
             gbc2.anchor = GridBagConstraints.EAST;
             gbc2.fill = GridBagConstraints.NONE;
             gbl2.setConstraints(licenseLabel, gbc2);
             panel.add(licenseLabel);
             gbc2.gridx = 1;
             //gbc2.gridy = 1;
             gbc2.anchor = GridBagConstraints.WEST;
             gbl2.setConstraints(licenses, gbc2);
             panel.add(licenses);
             gbc2.gridx = 2;
             gbl2.setConstraints(applyLicenseLabel, gbc2);
             panel.add(applyLicenseLabel);
             gbc2.gridx = 0;
             gbc2.gridy = 5;
             gbc2.anchor = GridBagConstraints.NORTHEAST;
             gbl2.setConstraints(tagsLabel, gbc2);
             panel.add(tagsLabel);
             gbc2.gridx = 1;
             //gbc2.gridy = 1;
             gbc2.anchor = GridBagConstraints.NORTHWEST;
             gbc2.fill = GridBagConstraints.HORIZONTAL;
             gbl2.setConstraints(tagsField, gbc2);
             panel.add(tagsField);
             gbc2.gridx = 2;
             gbc2.anchor = GridBagConstraints.CENTER;
             gbl2.setConstraints(applyTagsLabel, gbc2);
             panel.add(applyTagsLabel);
             gbc2.gridx = 0;
             gbc2.gridy = 6;
             gbc2.anchor = GridBagConstraints.EAST;
             gbc2.fill = GridBagConstraints.NONE;
             gbl2.setConstraints(categoryLabel, gbc2);
             panel.add(categoryLabel);
             gbc2.gridx = 1;
             //gbc2.gridy = 1;
             gbc2.anchor = GridBagConstraints.WEST;
             gbl2.setConstraints(categories, gbc2);
             panel.add(categories);
             gbc2.gridx = 2;
             gbl2.setConstraints(applyCatLabel, gbc2);
             panel.add(applyCatLabel);
             gbc2.gridx = 0;
             gbc2.gridy = 7;
             gbc2.anchor = GridBagConstraints.EAST;
             gbc2.fill = GridBagConstraints.NONE;
             gbl2.setConstraints(ratingLabel, gbc2);
             panel.add(ratingLabel);
             gbc2.gridx = 1;
             //gbc2.gridy = 1;
             gbc2.anchor = GridBagConstraints.WEST;
             gbl2.setConstraints(ratings, gbc2);
             panel.add(ratings);
             gbc2.gridx = 2;
             gbl2.setConstraints(applyRatingLabel, gbc2);
             panel.add(applyRatingLabel);
 
             //gbc2.gridx = 0;
             gbc2.gridy = 8;
             //gbc2.anchor = GridBagConstraints.EAST;
             gbc2.fill = GridBagConstraints.NONE;
             //gbl2.setConstraints(ratingLabel, gbc2);
             //panel.add(ratingLabel);
             gbc2.gridx = 1;
             //gbc2.gridy = 1;
             gbc2.anchor = GridBagConstraints.WEST;
             gbl2.setConstraints(explicit, gbc2);
             panel.add(explicit);
             gbc2.gridx = 2;
             gbl2.setConstraints(applyExplicitLabel, gbc2);
             panel.add(applyExplicitLabel);
 
             gbc2.gridx = 0;
             gbc2.gridy = 9;
             gbc2.anchor = GridBagConstraints.EAST;
             gbc2.fill = GridBagConstraints.NONE;
             gbl2.setConstraints(languageLabel, gbc2);
             panel.add(languageLabel);
             gbc2.gridx = 1;
             //gbc2.gridy = 1;
             gbc2.anchor = GridBagConstraints.WEST;
             gbl2.setConstraints(languages, gbc2);
             panel.add(languages);
             gbc2.gridx = 2;
             gbl2.setConstraints(applyLanguageLabel, gbc2);
             panel.add(applyLanguageLabel);
 
             boolean noBlogs = blogNames.length == 0;
             boolean noDests = destinations.length == 0;
             if (!noBlogs || !noDests) {
                 final JPanel distPanel = new JPanel();
                 final JLabel distLabel = new JLabel(I18n.getString(DIST_LABEL_KEY), Icons.collapsedIcon, JLabel.HORIZONTAL);
                 distLabel.addMouseListener(
                         new MouseListener() {
                             public void mouseClicked(MouseEvent e) {
                                 distPanel.setVisible(!distPanel.isVisible());
                                 distLabel.setIcon(distLabel.getIcon().equals(Icons.collapsedIcon) ? Icons.expandedIcon : Icons.collapsedIcon);
                             }
                             public void mousePressed(MouseEvent e) { /* ignored */ }
                             public void mouseReleased(MouseEvent e) { /* ignored */ }
                             public void mouseEntered(MouseEvent e) { /* ignored */ }
                             public void mouseExited(MouseEvent e) { /* ignored */ }
                         }
                 );
                 //distPanel.setLayout(new GridLayout(noBlogs || noDests ? 1 : 2, 1));
                 GridBagLayout gbl3 = new GridBagLayout();
                 GridBagConstraints gbc3 = new GridBagConstraints();
                 distPanel.setLayout(gbl3);
                 gbc3.gridy = -1;
                 gbc3.fill = GridBagConstraints.HORIZONTAL;
                 if (!noBlogs) {
                     gbc3.gridx = 0;
                     gbc3.gridy += 1;
                     JPanel blogPanel = new JPanel();
                     blogPanel.setBorder(
                             BorderFactory.createCompoundBorder(
                                     BorderFactory.createCompoundBorder(
                                             BorderFactory.createTitledBorder(I18n.getString(BLOGS_LABEL_KEY)),
                                             BorderFactory.createEmptyBorder(5, 5, 5, 5)
                                     ),
                                     blogPanel.getBorder()
                             )
                     );
                     final JCheckBox[] boxes = blogCheckboxList[i];
                     LinkLabel applyBlogLabel = new LinkLabel(
                             I18n.getString(APPLY_LABEL_KEY),
                             new Command() {
                                 public void execute() {
                                     for (int x = 0; x < blogCheckboxList.length; x++)
                                         for (int y = 0; y < blogCheckboxList[x].length; y++)
                                             blogCheckboxList[x][y].setSelected(boxes[y].isSelected());
                                 }
                             }
                     );
                     int mod = blogCheckboxList[i].length % 2;
                     int div = blogCheckboxList[i].length / 2;
                     blogPanel.setLayout(new GridLayout(mod == 0 ? div : div + 1, 2));
                     for (int j = 0; j < blogCheckboxList[i].length; j++) {
                         blogPanel.add(blogCheckboxList[i][j]);
                     }
                     gbl3.setConstraints(blogPanel, gbc3);
                     distPanel.add(blogPanel);
                     gbc3.gridx = 1;
                     gbl3.setConstraints(applyBlogLabel, gbc3);
                     distPanel.add(applyBlogLabel);
                 }
                 if (!noDests) {
                     gbc3.gridx = 0;
                     gbc3.gridy += 1;
                     JPanel uploadPanel = new JPanel();
                     uploadPanel.setBorder(
                             BorderFactory.createCompoundBorder(
                                     BorderFactory.createCompoundBorder(
                                             BorderFactory.createTitledBorder(I18n.getString(XUPLOADS_LABEL_KEY)),
                                             BorderFactory.createEmptyBorder(5, 5, 5, 5)
                                     ),
                                     uploadPanel.getBorder()
                             )
                     );
                     final JCheckBox[] boxes = destCheckboxList[i];
                     LinkLabel applyDestLabel = new LinkLabel(
                             I18n.getString(APPLY_LABEL_KEY),
                             new Command() {
                                 public void execute() {
                                     for (int x = 0; x < destCheckboxList.length; x++)
                                         for (int y = 0; y < destCheckboxList[x].length; y++)
                                             destCheckboxList[x][y].setSelected(boxes[y].isSelected());
                                 }
                             }
                     );
                     int mod = destCheckboxList[i].length % 2;
                     int div = destCheckboxList[i].length / 2;
                     uploadPanel.setLayout(new GridLayout(mod == 0 ? div : div + 1, 2));
                     for (int j = 0; j < destCheckboxList[i].length; j++) {
                         uploadPanel.add(destCheckboxList[i][j]);
                     }
                     gbl3.setConstraints(uploadPanel, gbc3);
                     distPanel.add(uploadPanel);
                     gbc3.gridx = 1;
                     gbl3.setConstraints(applyDestLabel, gbc3);
                     distPanel.add(applyDestLabel);
                 }
 
                 // add to overall layout
                 gbc2.gridx = 0;
                 gbc2.gridy = 10;
                 gbc2.anchor = GridBagConstraints.NORTHEAST;
                 gbl2.setConstraints(distLabel, gbc2);
                 panel.add(distLabel);
                 gbc2.gridx = 1;
                 gbc2.anchor = GridBagConstraints.WEST;
                 gbl2.setConstraints(distPanel, gbc2);
                 panel.add(distPanel);
                 distPanel.setVisible(false);
             }
 
             gbc.gridx = 0;
             gbc.gridy += 1;
             gbc.fill = GridBagConstraints.HORIZONTAL;
             overall.add(panel, gbc);
         }
 
         JScrollPane scrollpane = new JScrollPane(overall);
 
         view.setLayout(new BorderLayout());
         view.add(scrollpane, BorderLayout.CENTER);
 
         setView(view);
     }
 
     public void applyState() {
         // this is called when the user clicks "Next"
         // Create string arrays
         String[] titles = new String[titleList.length];
         // TODO: why descList? why not match up?
         String[] descriptions = new String[descList.length];
         String[] tags = new String[descList.length];
         String[] categories = new String[descList.length];
         String[] licenses = new String[descList.length];
         String[] ratings = new String[descList.length];
         String[] explicitFlags = new String[descList.length];
         String[] languages = new String[descList.length];
         // TODO: this could result in an NPE, right? maybe not.
         String[][] blogs = new String[blogCheckboxList.length][blogCheckboxList[0].length];
         String[][] destinations = new String[destCheckboxList.length][destCheckboxList[0].length];
         File[] thumbnails = new File[descList.length];
         // Loop through components and populate the arrays
         for (int i = 0; i < titleList.length; i++) {
             // If the user didn't enter a title, set it to the name of the file
             // minus the dot extension
             if (titleList[i].getText() != null && titleList[i].getText().equals("")) {
                 String name = model.getFiles()[i].getName();
                 titles[i] = name.substring(0, name.lastIndexOf("."));
             } else {
                 titles[i] = titleList[i].getText();
             }
             descriptions[i] = descList[i].getText();
             tags[i] = tagsList[i].getText();
             languages[i] = (String)MetadataLoader.languages.get(languageList[i].getSelectedItem());
             categories[i] = (String)MetadataLoader.categories.get(categoryList[i].getSelectedItem());
             licenses[i] = (String)MetadataLoader.licenses.get(licenseList[i].getSelectedItem());
             // Content rating dropdowns
            ratings[i] = null;
             if (ratingList[i].getSelectedIndex() != 0) {
                 ratings[i] = (String)MetadataLoader.ratings.get(ratingList[i].getSelectedItem());
             }
             // Explicit checkboxes
             explicitFlags[i] = explicitList[i].isSelected() ? "1" : "0";
             // Thumbnail file dropdowns
             thumbnails[i] = null;
             if (thumbList[i].getSelectedIndex() != 0) {
                 String filename = (String)thumbList[i].getSelectedItem();
                 Object obj = model.thumbnailFileLookup.get(filename);
                 if (obj != null)
                     thumbnails[i] = (File)obj;
             }
             // Cross-post destination dropdowns
             for (int j = 0; j < blogs[i].length; j++)
                 if (blogCheckboxList[i][j].isSelected())
                     blogs[i][j] = (String)MetadataLoader.blogs.get(blogCheckboxList[i][j].getText());
             // Cross-upload destination dropdowns
             for (int j = 0; j < destinations[i].length; j++)
                 if (destCheckboxList[i][j].isSelected())
                     destinations[i][j] = (String)MetadataLoader.crossuploads.get(destCheckboxList[i][j].getText());
         }
         // Put the arrays in our model
         model.setTitles(titles);
         model.setDescriptions(descriptions);
         model.setTags(tags);
         model.setCategories(categories);
         model.setLicenses(licenses);
         model.setRatings(ratings);
         model.setExplicitFlags(explicitFlags);
         model.setLanguages(languages);
         model.setThumbnails(thumbnails);
         model.setCrossposts(blogs);
         model.setCrossuploads(destinations);
     }
 
     public Dimension getPreferredSize() {
         return view.getPreferredSize();
     }
 
 } // class MetaDataStep
