 /*
  * Copyright 2011 Eike Kettner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.eknet.swing.uploadfield;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
import java.net.URLDecoder;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRootPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.OverlayLayout;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /**
  * An input component for images.
  * <p/>
  * It consists of the following:
  * <ul>
  * <li>two text fields, one for the image url and one for an optional title for name</li>
  * <li>a preview button, that shows the image. onclick a open-file-dialog is presented to choose a file
  * from the file system</li>
  * <li>optional, a set of predefined images can be specified. If so, a small button opens a
  * component that allows the user to choose a image.</li>
  * <li>a button to reset the form</li>
  * </ul>
  *
  * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
  * @since 01.10.11 15:24
  */
 public class UploadField extends JPanel {
 
   private static final Logger log = LoggerFactory.getLogger(UploadField.class);
 
   private JPanel root;
   private JButton previewButton;
   private JTextField resourceField;
   private JTextField nameField;
   private JPanel previewPanel;
   private JButton proposalsButton;
   private JButton resetButton;
   private JLabel messageLabel;
 
   private static final ImageIcon folderIcon = new ImageIcon(UploadField.class.getResource("folder.png"));
   private static final ImageIcon resetIcon = new ImageIcon(UploadField.class.getResource("arrow_undo.png"));
   private static final ImageIcon imagesIcon = new ImageIcon(UploadField.class.getResource("images.png"));
   private static final ImageIcon successIcon = new ImageIcon(UploadField.class.getResource("tick.png"));
   private static final ImageIcon errorIcon = new ImageIcon(UploadField.class.getResource("error.png"));
 
   private final List<PreviewHandler> previewHandlers = new CopyOnWriteArrayList<PreviewHandler>();
 
   private Set<URL> proposals;
   private Dimension previewSize = new Dimension(50, 50);
   private final Color normalMessageColor;
   private Color errorMessageColor = Color.red;
   private IconsList iconViewer = new IconsList();
   private JDialog dialog;
   private boolean documentListenerMuted = false;
 
   private JComponent glassPane = new SimpleGlassPane();
 
   private final MouseAdapter emptyMouseListener = new MouseAdapter() {};
   private final KeyAdapter emptyKeyListener = new KeyAdapter() {};
 
   private UploadValue image;
 
   public UploadField() {
     setLayout(new OverlayLayout(this));
     this.normalMessageColor = messageLabel.getForeground();
 
     messageLabel.setFont(messageLabel.getFont().deriveFont(10f));
 
     iconViewer.addListSelectionListener(new ListSelectionListener() {
       @Override
       public void valueChanged(ListSelectionEvent e) {
         if (!e.getValueIsAdjusting()) {
           URL url = (URL) iconViewer.getSelectedValue();
           if (url != null) {
             UploadValue value = createNewOrCopy();
             value.setResource(url);
             setImage(value);
           }
           if (dialog != null) {
             dialog.setVisible(false);
           }
         }
       }
     });
 
     previewButton.setIcon(folderIcon);
     previewButton.setText("...");
     previewButton.addActionListener(newOpenImageAction());
     previewButton.setBackground(Color.white);
     previewButton.setToolTipText(getOpenButtonTooltip());
 
     proposalsButton.setIcon(imagesIcon);
     proposalsButton.setText(null);
     proposalsButton.setVisible(false);
     proposalsButton.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         if (dialog == null) {
           Window parent = Utils.findWindow(proposalsButton);
           dialog = new JDialog(parent, "Proposals", Dialog.ModalityType.APPLICATION_MODAL) {
             @Override
             protected JRootPane createRootPane() {
               JRootPane rootPane = super.createRootPane();
               KeyStroke escStroke = KeyStroke.getKeyStroke("ESCAPE");
               InputMap map = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
 
               map.put(escStroke, "ESCAPE");
               rootPane.getActionMap().put("ESCAPE", new AbstractAction() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                   dialog.setVisible(false);
                 }
               });
               return rootPane;
             }
           };
           dialog.getContentPane().setLayout(new BorderLayout());
           dialog.getContentPane().add(new JScrollPane(iconViewer), BorderLayout.CENTER);
         }
         Rectangle bounds = new Rectangle();
         Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
         bounds.x = mouseLoc.x;
         bounds.y = mouseLoc.y;
         bounds.width = 350;
         bounds.height = 250;
         dialog.setBounds(bounds);
         dialog.setVisible(!dialog.isVisible());
       }
     });
 
     resetButton.setIcon(resetIcon);
     resetButton.setText(null);
     resetButton.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         setImage(null);
       }
     });
 
     resourceField.addKeyListener(new KeyAdapter() {
       @Override
       public void keyTyped(KeyEvent e) {
         if (e.getKeyChar() == KeyEvent.VK_ENTER) {
           String url = resourceField.getText();
           if (url == null || "".equals(url.trim())) {
             setImage(null);
           } else {
             UploadValue value = createNewOrCopy();
             try {
               if (!url.contains(":/")) {
                 url = new File(url).toURI().toURL().toString();
               }
               value.setResource(new URL(url));
               setImage(value);
             } catch (MalformedURLException e1) {
               setMessage(getLoadingErrorMessage(value), true);
             }
           }
         }
       }
     });
     nameField.getDocument().addDocumentListener(new DocumentListener() {
       @Override
       public void insertUpdate(DocumentEvent e) {
         update(e);
       }
 
       @Override
       public void removeUpdate(DocumentEvent e) {
         update(e);
       }
 
       @Override
       public void changedUpdate(DocumentEvent e) {
         update(e);
       }
 
       private void update(DocumentEvent e) {
         if (documentListenerMuted) {
           return;
         }
         String text = nameField.getText();
         String url = resourceField.getText();
         if (url == null && (text == null || "".equals(text.trim()))) {
           setImage(null);
         } else {
           UploadValue value = createNewOrCopy();
           value.setName(text);
           UploadValue old = getImage();
           UploadField.this.image = value;
           UploadField.this.firePropertyChange("image", old, value);
         }
       }
     });
     add(root, -1);
     setGlassPane(new SimpleGlassPane());
   }
 
   public static UploadField forImageFiles() {
     UploadField fi = new UploadField();
     fi.addPreviewHandler(new ImagePreviewHandler());
     return fi;
   }
 
   public static UploadField forAllFiles() {
     UploadField fi = forImageFiles();
     fi.addPreviewHandler(Utils.allFileHandler());
     return fi;
   }
   
   public void setPreviewSize(int width, int height) {
     this.setPreviewSize(new Dimension(width, height));
   }
 
   /**
    * Sets the previewSize to the specified value. If the value is {@code null}
    * nothing changes.
    * @param dim
    */
   public void setPreviewSize(Dimension dim) {
     if (dim != null) {
       Dimension old = getPreviewSize();
       this.previewSize = dim;
       previewPanel.setPreferredSize(previewSize);
       previewPanel.setMaximumSize(previewSize);
       previewPanel.setMinimumSize(previewSize);
       firePropertyChange("previewSize", old, dim);
     }
   }
 
   @NotNull
   public Dimension getPreviewSize() {
     return this.previewSize;
   }
 
   public void addPreviewHandler(PreviewHandler handler) {
     if (handler != null) {
       previewHandlers.add(handler);
     }
   }
 
   public void removePreviewHandler(PreviewHandler handler) {
     if (handler != null) {
       previewHandlers.remove(handler);
     }
   }
 
   public JComponent getGlassPane() {
     return glassPane;
   }
 
   public void setGlassPane(JComponent glassPane) {
     boolean visible = false;
     if (this.glassPane != null) {
       visible = this.glassPane.isVisible();
       remove(this.glassPane);
     }
     this.glassPane = glassPane;
     if (this.glassPane != null) {
       add(this.glassPane, 0);
       this.glassPane.addMouseListener(emptyMouseListener);
       this.glassPane.addKeyListener(emptyKeyListener);
       this.glassPane.setVisible(visible);
     }
   }
 
   public void setProposals(Iterable<URL> images) {
     if (images != null && images.iterator().hasNext()) {
       this.proposals = new HashSet<URL>();
       for (URL iv : images) {
         this.proposals.add(iv);
       }
       this.iconViewer.setIconElements(proposals);
     } else {
       this.proposals = null;
       this.iconViewer.setIconElements(null);
     }
     proposalsButton.setVisible(this.proposals != null);
   }
 
   public Iterable<URL> getProposals() {
     return Collections.unmodifiableSet(this.proposals);
   }
 
   /**
    * Sets the specified image on this component. If the image cannot be read,
    * it updates the component with an error message. Never throws an exception.
    *
    * @param image
    */
   public void setImage(@Nullable UploadValue image) {
     UploadValue old = getImage();
 
     //if new image url is given, component must be updated by LoadingImageTask
     boolean loading = false;
     if (image != null && image.getResource() != null) {
       if (old == null || (!image.getResource().equals(old.getResource()))) {
         new ImageLoadingTask(image).execute();
         loading = true;
       }
     }
     if (!loading) {
       if (!Utils.nullSafeEquals(image, old)) {
         this.image = image;
         firePropertyChange("image", old, image);
       }
       updateComponent(this.image);
     }
   }
 
   public void setMessage(String message, boolean isError) {
     if (isError) {
       messageLabel.setForeground(errorMessageColor);
       messageLabel.setIcon(errorIcon);
     } else {
       messageLabel.setForeground(normalMessageColor);
       messageLabel.setIcon(successIcon);
     }
     this.messageLabel.setText(message);
   }
 
   public void clearMessage() {
     this.messageLabel.setText(null);
     this.messageLabel.setIcon(null);
   }
 
   @Nullable
   public UploadValue getImage() {
     return image;
   }
 
   protected Action newOpenImageAction() {
     return new FileOpenAction(this, previewHandlers);
   }
 
   protected String getImageDescription(@NotNull UploadValue value) {
     StringBuilder buf = new StringBuilder();
     if (value.getImage() != null && !value.isMissingImage()) {
       buf.append(value.getImage().getWidth())
               .append("x")
               .append(value.getImage().getHeight())
               .append("px");
     }
     if (value.getFile() != null) {
       if (value.getImage() != null && !value.isMissingImage()) {
         buf.append("; ");
       }
       buf.append(Utils.toSizeString(value.getFile().length()));
     }
     return buf.toString();
   }
 
   protected String getLoadingErrorMessage(UploadValue value) {
     String name = "";
     if (value == null || value.getResource() == null) {
       name = "<null>";
     } else {
       name = Utils.lastUrlPart(value.getResource());
     }
     return "Unable to load preview for: " + name;
   }
 
   protected String getOpenButtonTooltip() {
     return "Click to select a image from disk";
   }
 
 
   private UploadValue createNewOrCopy() {
     if (this.image == null) {
       return new UploadValue();
     }
     return new UploadValue(this.image);
   }
 
   private void updateComponent(UploadValue value) {
     documentListenerMuted = true;
     if (UploadValue.isNullOrEmpty(value)) {
       resourceField.setText(null);
       nameField.setText(null);
       previewButton.setIcon(folderIcon);
       previewButton.setText("...");
       clearMessage();
     } else {
       URL url = value.getResource();
       resourceField.setText(url != null ? url.toString() : null);
       nameField.setText(value.getName());
       if (value.getIcon() != null) {
         previewButton.setIcon(value.getIcon());
         previewButton.setText(null);
       }
       setMessage(getImageDescription(value), false);
     }
     documentListenerMuted = false;
   }
 
   public boolean isOptimizedDrawingEnabled() {
     return false;
   }
 
   private class ImageLoadingTask extends SwingWorker<UploadValue, Void> {
 
     private final UploadValue value;
 
     public ImageLoadingTask(UploadValue value) {
       this.value = value;
       SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
           if (getGlassPane() != null) {
             getGlassPane().setVisible(true);
           }
         }
       });
     }
 
 
     @Override
     protected UploadValue doInBackground() throws Exception {
       if (UploadValue.isNullOrEmpty(value)) {
         return null;
       }
       BufferedImage image = null;
       for (PreviewHandler handler : previewHandlers) {
         image = handler.createImage(value.getResource());
         if (image != null) {
           break;
         }
       }
       if (image == null) {
         image = Utils.getMissingImage();
       }
       value.setImage(image);
       if (!value.getResource().getProtocol().equals("file")) {
         File tempFile = File.createTempFile("fileInput", ".ext");
         tempFile.deleteOnExit();
         Utils.copy(value.getResource(), tempFile);
         value.setFile(tempFile);
       } else {
        value.setFile(new File(URLDecoder.decode(value.getResource().getPath(), "UTF-8")));
       }
       if (image != null) {
         value.setIcon(new ImageIcon(value.getScaledImage(previewSize)));
       }
       return value;
     }
 
     @Override
     protected void done() {
       try {
         UploadValue old = getImage();
         UploadValue newvValue = get();
         UploadField.this.image = newvValue;
         updateComponent(newvValue);
         if (newvValue.getImage() == null) {
           setMessage(getLoadingErrorMessage(value), true);
         }
         UploadField.this.firePropertyChange("image", old, newvValue);
       } catch (InterruptedException e) {
         setMessage(getLoadingErrorMessage(value), true);
         log.error("Error loading image!", e);
       } catch (ExecutionException e) {
         setMessage(getLoadingErrorMessage(value), true);
         log.error("Error loading image!", e);
       } finally {
         if (getGlassPane() != null) {
           getGlassPane().setVisible(false);
         }
       }
     }
 
   }
 
 }
