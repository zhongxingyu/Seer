 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.modules.audiomanager.client;
 
 import java.io.File;
 import java.util.ResourceBundle;
 import javax.swing.Icon;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.SpinnerNumberModel;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.content.ContentBrowserManager;
 import org.jdesktop.wonderland.client.content.spi.ContentBrowserSPI;
 import org.jdesktop.wonderland.client.content.spi.ContentBrowserSPI.ContentBrowserListener;
 import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
 import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
 import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
 import org.jdesktop.wonderland.client.login.LoginManager;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState;
 import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState.PlayWhen;
 import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState.TreatmentType;
 import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;
 
 import org.jdesktop.wonderland.modules.audiomanager.client.AudioTreatmentComponent;
 
 import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
 import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
 import org.jdesktop.wonderland.modules.contentrepo.client.ui.modules.ModuleRootContentCollection;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
 import org.jdesktop.wonderland.modules.contentrepo.client.utils.ContentRepositoryUtils;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
 
 import com.jme.bounding.BoundingBox;
 import com.jme.bounding.BoundingSphere;
 import com.jme.bounding.BoundingVolume;
 
 import com.jme.math.Vector3f;
 
 /**
  *
  * @author  jp
  * @author Ronny Standtke <ronny.standtke@fhnw.ch>
  */
 @PropertiesFactory(AudioTreatmentComponentServerState.class)
 public class AudioTreatmentComponentProperties extends javax.swing.JPanel
         implements PropertiesFactorySPI, AudioTreatmentStatusListener {
 
     private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
             "org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle");
     private CellPropertiesEditor editor;
     private String originalGroupId = "";
     private TreatmentType originalTreatmentType = TreatmentType.FILE;
     private String originalTreatments = "";
     private int originalVolume;
     private PlayWhen originalPlayWhen;
     private boolean originalPlayOnce;
     private float originalExtentRadius;
     private boolean originalUseCellBounds;
     private float originalFullVolumeAreaPercent;
     private boolean originalDistanceAttenuated;
     private int originalFalloff;
     private boolean originalShowBounds;
     private SpinnerNumberModel fullVolumeAreaPercentModel;
     private SpinnerNumberModel extentRadiusModel;
     private float extentRadius = 0;
     private boolean useCellBounds;
     private TreatmentType treatmentType = TreatmentType.FILE;
     private PlayWhen playWhen;
     private boolean playOnce;
     private boolean distanceAttenuated;
 
     private Cell currentCell;
 
     private String lastFileTreatment;
     private String lastContentRepositoryTreatment;
     private String lastURLTreatment;
 
     private BoundsViewerEntity boundsViewerEntity;
 
     /** Creates new form AudioTreatmentComponentProperties */
     public AudioTreatmentComponentProperties() {
         initComponents();
 
         String diagramFileName = BUNDLE.getString("AudioCapabilitiesDiagram");
         String resourceName = "/org/jdesktop/wonderland/modules/audiomanager/" +
                 "client/resources/" + diagramFileName;
         Icon icon = new ImageIcon(getClass().getResource(resourceName));
         audioCapabilitiesLabel.setIcon(icon);
 
         // Set the maximum and minimum values for the spinners
         Float value = new Float(25);
         Float min = new Float(0);
         Float max = new Float(100);
         Float step = new Float(1);
         fullVolumeAreaPercentModel =
                 new SpinnerNumberModel(value, min, max, step);
         fullVolumeAreaPercentSpinner.setModel(fullVolumeAreaPercentModel);
 
         value = new Float(10);
         min = new Float(0);
         max = new Float(100);
         step = new Float(1);
         extentRadiusModel = new SpinnerNumberModel(value, min, max, step);
         extentRadiusSpinner.setModel(extentRadiusModel);
 
         // Listen for changes to the text fields and spinners
         audioGroupIdTextField.getDocument().addDocumentListener(
                 new AudioGroupTextFieldListener());
         treatmentTextField.getDocument().addDocumentListener(
                 new AudioTreatmentsTextFieldListener());
         fullVolumeAreaPercentModel.addChangeListener(
                 new FullVolumeAreaPercentChangeListener());
         extentRadiusModel.addChangeListener(new ExtentRadiusChangeListener());
     }
 
     /**
      * @{inheritDoc}
      */
     public String getDisplayName() {
         return BUNDLE.getString("Audio_Capabilities");
     }
 
     /**
      * @{inheritDoc}
      */
     public JPanel getPropertiesJPanel() {
         return this;
     }
 
     /**
      * @{inheritDoc}
      */
     public void setCellPropertiesEditor(CellPropertiesEditor editor) {
         this.editor = editor;
     }
 
     /**
      * @{inheritDoc}
      */
     public void open() {
         CellServerState state = editor.getCellServerState();
 
         AudioTreatmentComponentServerState compState =
                 (AudioTreatmentComponentServerState) state.getComponentServerState(
                 AudioTreatmentComponentServerState.class);
 
         if (state == null) {
             return;
         }
 
         originalGroupId = compState.getGroupId();
 
         String[] treatmentList = compState.getTreatments();
 
 	originalTreatmentType = compState.getTreatmentType();
 
 	treatmentType = originalTreatmentType;
 
         originalTreatments = "";
 
 	/*
 	 * XXX We only allow a single treatment to be specified
 	 */
         for (int i = 0; i < treatmentList.length; i++) {
             String treatment = treatmentList[i];
 
 	    if (treatment.length() == 0) {
 		break;
 	    }
 
             originalTreatments += treatment;
 	    break;	// XXX we only allow a single treatment for now
         }
 
         originalTreatments = originalTreatments.trim();
 
         originalVolume = VolumeUtil.getClientVolume(compState.getVolume());
 
         originalPlayWhen = compState.getPlayWhen();
         playWhen = originalPlayWhen;
 
         originalPlayOnce = compState.getPlayOnce();
 
         originalExtentRadius = (float) compState.getExtent();
         extentRadius = originalExtentRadius;
 
         originalFullVolumeAreaPercent = (float) compState.getFullVolumeAreaPercent();
 
         originalDistanceAttenuated = compState.getDistanceAttenuated();
         distanceAttenuated = originalDistanceAttenuated;
 
         originalFalloff = (int) compState.getFalloff();
 
 	originalUseCellBounds = compState.getUseCellBounds();
 
 	BoundingVolume bounds = editor.getCell().getLocalBounds();
 
 	if (originalUseCellBounds == true && bounds instanceof BoundingBox) {
 	    originalDistanceAttenuated = false;
 	    distanceAttenuated = false;
 	} 
 
 	originalShowBounds = compState.getShowBounds();
 	restore();
 
 	if (currentCell == null) {
 	    currentCell = editor.getCell();
 
             AudioTreatmentComponent component = currentCell.getComponent(AudioTreatmentComponent.class);
 
 	    component.addTreatmentStatusListener(this);
 	}
     }
 
     /**
      * @{inheritDoc}
      */
     public void close() {
         if (boundsViewerEntity != null) {
             boundsViewerEntity.dispose();
             boundsViewerEntity = null;
         }
 
 	if (currentCell != null) {
 	    AudioTreatmentComponent component = currentCell.getComponent(AudioTreatmentComponent.class);
 
 	    if (component != null) {
 	        component.removeTreatmentStatusListener(this);
 	    }
 
 	    currentCell = null;
 	}
     }
 
     /**
      * @{inheritDoc}
      */
     public void apply() {
         // Figure out whether there already exists a server state for the
         // component.
         CellServerState state = editor.getCellServerState();
 
         AudioTreatmentComponentServerState compState =
                 (AudioTreatmentComponentServerState) state.getComponentServerState(
                 AudioTreatmentComponentServerState.class);
 
         if (state == null) {
             return;
         }
 
         compState.setGroupId(audioGroupIdTextField.getText().trim());
 
         String treatments = treatmentTextField.getText().trim();
 
         // Update the component state, add to the list of updated states
 	compState.setTreatmentType(treatmentType);
         compState.setTreatments(treatments.split(";"));
         compState.setPlayWhen(playWhen);
         compState.setPlayOnce(playOnce);
         compState.setExtent((Float) extentRadiusModel.getValue());
 	compState.setUseCellBounds(useCellBounds);
         compState.setFullVolumeAreaPercent(
                 (Float) fullVolumeAreaPercentModel.getValue());
         compState.setDistanceAttenuated(distanceAttenuated);
         compState.setFalloff(falloffSlider.getValue());
         editor.addToUpdateList(compState);
 
 	if (currentCell != null && currentCell.getCellID().equals(editor.getCell().getCellID()) == false) {
 	    statusLabel.setText("");
 	}
 
 	if (treatments != null && treatments.length() > 0) {
 	    switch (treatmentType) {
 	    case FILE:
 		lastFileTreatment = treatments;
 		try {
 		    statusLabel.setText("Uploading " + treatments);
 		    uploadFileTreatments();
 		} catch (Exception e) {
 		    statusLabel.setText("Failed to upload " + treatments);
 		    break;	
 		}
 
 	        ServerSessionManager serverSessionManager = LoginManager.getPrimary();
 
 		contentRepositoryRadioButton.doClick();
 
 		compState.setTreatmentType(TreatmentType.CONTENT_REPOSITORY);
 
		int ix = treatments.lastIndexOf(File.separator);
 
 		if (ix >= 0) {
 		    treatments = treatments.substring(ix + 1);
 		}
 
 		String contentRepositoryTreatment = 
 		    "wlcontent://users/" + serverSessionManager.getUsername() + "/audio/" + treatments;
 
 	        compState.setTreatments(new String[] { contentRepositoryTreatment });
 		treatmentTextField.setText(contentRepositoryTreatment);
 	        lastContentRepositoryTreatment = treatmentTextField.getText();
 	        break;
 
 	    case CONTENT_REPOSITORY:
 	        lastContentRepositoryTreatment = treatmentTextField.getText();
 	        break;
 
 	    case URL:
 	        lastURLTreatment = treatmentTextField.getText();
 	        break;
 	    }
 	}
     }
 
     private void uploadFileTreatments() throws Exception {
         // make sure specified file exists, create an
         // entry in the content repository and upload the file.
 	String pattern = "file://";
 
 	String s = lastFileTreatment;
 
 	int ix = lastFileTreatment.indexOf(pattern);
 
 	if (ix >= 0) {
 	    s = s.substring(ix + pattern.length());
 	}
 
         File file = new File(s);
 
         if (file.exists() == false) {
 	    error("Non-existent file " + file);
 	    return;
 	}
 
         ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
 
         ContentRepository repo = registry.getRepository(LoginManager.getPrimary());
 	
         ContentCollection audioCollection;
 
         try {
 	    ContentCollection c = repo.getUserRoot();
 
 	    audioCollection = (ContentCollection) c.getChild("audio");
 
 	    if (audioCollection == null) {
 		audioCollection = (ContentCollection) c.createChild("audio", Type.COLLECTION);
   	    }
         } catch (ContentRepositoryException e) {
 	    error("ContentRepositoryException " + e.getMessage());
 	    return;
         }
 
         try {
 	    /*
 	     * Remove file if it exists.
 	     */
             ContentResource r = (ContentResource) audioCollection.removeChild(file.getName());
 	} catch (Exception e) {
 	}
 
         try {
             ContentResource r = (ContentResource) audioCollection.createChild(
                 file.getName(), ContentNode.Type.RESOURCE);
 
             r.put(file);
         } catch (Exception e) {
             error("Failed to upload " + file + " " + e.getMessage());
 	}
     }
 
     private void error(final String msg) throws Exception {
 	final javax.swing.JPanel panel = this;
 
 	java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
 		System.out.println(msg);
 		String title = "Content Upload Error";
 		javax.swing.JOptionPane.showMessageDialog(
             	    panel, msg, title, javax.swing.JOptionPane.ERROR_MESSAGE);
 	    }
 	});
 
 	throw new Exception(msg);
     }
 
     public void treatmentEstablished() {
 	java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
 		statusLabel.setText("Treatment started successfully");
 		close();
 		open();
 	    }
 	});
     }
 
     public void treatmentEnded(final String reason) {
 	java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
 		statusLabel.setText(reason);
 		close();
 		open();
 	    }
 	});
     }
 
     /**
      * @{inheritDoc}
      */
     public void restore() {
 	java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
 		restoreLater();
 	    }
 	});
     }
 
     private void restoreLater() {
         // Reset the GUI values to the original values
 
         audioGroupIdTextField.setText(originalGroupId);
 	
 	switch (originalTreatmentType) {
 	case FILE:
 	    fileRadioButton.setSelected(true);
 	    break;
 
 	case CONTENT_REPOSITORY:
 	    contentRepositoryRadioButton.setSelected(true);
 	    break;
 
 	case URL:
 	    URLRadioButton.setSelected(true);
 	    break;
 	}
 
         treatmentTextField.setText(originalTreatments);
 
         volumeSlider.setValue(originalVolume);
 
         switch (originalPlayWhen) {
             case ALWAYS:
                 alwaysRadioButton.setSelected(true);
                 break;
 
             case FIRST_IN_RANGE:
                 proximityRadioButton.setSelected(true);
                 break;
 
             case MANUAL:
                 manualRadioButton.setSelected(true);
                 break;
         }
 
         playOnceCheckBox.setSelected(originalPlayOnce);
 
         extentRadiusSpinner.setValue(originalExtentRadius);
         extentRadiusSpinner.setEnabled(useCellBounds == false);
 
         fullVolumeAreaPercentSpinner.setValue(originalFullVolumeAreaPercent);
 
 
         falloffSlider.setValue(originalFalloff);
         falloffSlider.setEnabled(originalDistanceAttenuated);
 
 	BoundingVolume bounds = editor.getCell().getLocalBounds();
 
 	if (originalUseCellBounds == true && bounds instanceof BoundingBox) {
             distanceAttenuatedRadioButton.setSelected(false);
             distanceAttenuatedRadioButton.setEnabled(false);
 	    distanceAttenuated = false;
 	} else {
             distanceAttenuatedRadioButton.setSelected(originalDistanceAttenuated);
 	}
 
 	falloffSlider.setEnabled(distanceAttenuatedRadioButton.isSelected());
 
 	useCellBoundsRadioButton.setEnabled(true);
 	useCellBoundsRadioButton.setSelected(originalUseCellBounds);
 
 	showBoundsCheckBox.setSelected(originalShowBounds);
 
 	showBounds();
     }
 
     private void showBounds() {
 	if (boundsViewerEntity != null) {
 	    boundsViewerEntity.dispose();
 	    boundsViewerEntity = null;
 	}
 
 	if (showBoundsCheckBox.isSelected() == false) {
 	    return;
 	}
 
 	boundsViewerEntity = new BoundsViewerEntity(editor.getCell());
 
 	if (useCellBoundsRadioButton.isSelected()) {
 	    boundsViewerEntity.showBounds(editor.getCell().getLocalBounds());
 	} else {
 	    boundsViewerEntity.showBounds(
 		new BoundingSphere((Float) extentRadiusSpinner.getValue(), new Vector3f()));
 	} 
     }
 
     private boolean isDirty() {
 	String audioGroupId = audioGroupIdTextField.getText().trim();
 
 	if (audioGroupId.equals(originalGroupId) == false) {
 	    return true;
 	}
 
 	if (treatmentType != null && treatmentType.equals(originalTreatmentType) == false) {
 	    return true;
 	}
 
 	String treatments = treatmentTextField.getText().trim();
 
 	if (treatments.equals(originalTreatments) == false) {
 	    return true;
 	}
 
 	Float fullVolumeAreaPercent = (Float) fullVolumeAreaPercentModel.getValue();
 
 	if (fullVolumeAreaPercent != originalFullVolumeAreaPercent) {
 	    return true;
 	}
 
 	if (useCellBounds != originalUseCellBounds) {
 	    return true;
 	}
 
 	if (useCellBounds == false) {
 	    Float extentRadius = (Float) extentRadiusModel.getValue();
 
 	    if (extentRadius != originalExtentRadius) {
 	        return true;
 	    }
 	}
 
 	if (playWhen != null && playWhen.equals(originalPlayWhen) == false) {
 	    return true;
 	}
 
 	if (playWhen.equals(PlayWhen.MANUAL) && playOnce != originalPlayOnce) {
 	    return true;
 	}
 
 	if (distanceAttenuated != originalDistanceAttenuated) {
 	    return true;
 	}
 
 	if (distanceAttenuated == true) {
 	    if (falloffSlider.getValue() != originalFalloff) {
 	        return true;
 	    }
 	} 
 
 	if (volumeSlider.getValue() != originalVolume) {
 	    return true;
 	}
 
         if (originalShowBounds != showBoundsCheckBox.isSelected()) {
             return true;
         }
 
 	return false;
     }
 
     /**
      * Inner class to listen for changes to the text field and fire off dirty
      * or clean indications to the cell properties editor.
      */
     class AudioGroupTextFieldListener implements DocumentListener {
 
         public void insertUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         public void removeUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         public void changedUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         private void checkDirty() {
             if (editor != null) {
                 editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
             }
         }
     }
 
     /**
      * Inner class to listen for changes to the text field and fire off dirty
      * or clean indications to the cell properties editor.
      */
     class AudioTreatmentsTextFieldListener implements DocumentListener {
 
         public void insertUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         public void removeUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         public void changedUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         private void checkDirty() {
             if (editor != null) {
                 editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
             }
         }
     }
 
     class FullVolumeAreaPercentChangeListener implements ChangeListener {
 
         public void stateChanged(ChangeEvent e) {
             if (editor != null) {
                 editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
             }
         }
     }
 
     class ExtentRadiusChangeListener implements ChangeListener {
 
         public void stateChanged(ChangeEvent e) {
             if (editor != null) {
                 editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
 
 		showBounds();
             }
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         buttonGroup3 = new javax.swing.ButtonGroup();
         buttonGroup2 = new javax.swing.ButtonGroup();
         buttonGroup4 = new javax.swing.ButtonGroup();
         jLabel5 = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         treatmentTextField = new javax.swing.JTextField();
         jLabel7 = new javax.swing.JLabel();
         audioGroupIdTextField = new javax.swing.JTextField();
         browseButton = new javax.swing.JButton();
         jLabel10 = new javax.swing.JLabel();
         alwaysRadioButton = new javax.swing.JRadioButton();
         proximityRadioButton = new javax.swing.JRadioButton();
         manualRadioButton = new javax.swing.JRadioButton();
         jLabel11 = new javax.swing.JLabel();
         extentRadiusSpinner = new javax.swing.JSpinner();
         jLabel12 = new javax.swing.JLabel();
         fullVolumeAreaPercentSpinner = new javax.swing.JSpinner();
         jLabel13 = new javax.swing.JLabel();
         jLabel14 = new javax.swing.JLabel();
         ambientRadioButton = new javax.swing.JRadioButton();
         distanceAttenuatedRadioButton = new javax.swing.JRadioButton();
         falloffSlider = new javax.swing.JSlider();
         jLabel3 = new javax.swing.JLabel();
         audioCapabilitiesLabel = new javax.swing.JLabel();
         jLabel15 = new javax.swing.JLabel();
         volumeSlider = new javax.swing.JSlider();
         playOnceCheckBox = new javax.swing.JCheckBox();
         showBoundsCheckBox = new javax.swing.JCheckBox();
         specifyRadiusRadioButton = new javax.swing.JRadioButton();
         useCellBoundsRadioButton = new javax.swing.JRadioButton();
         cellBoundsLabel = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         statusLabel = new javax.swing.JLabel();
         fileRadioButton = new javax.swing.JRadioButton();
         contentRepositoryRadioButton = new javax.swing.JRadioButton();
         URLRadioButton = new javax.swing.JRadioButton();
 
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle"); // NOI18N
         jLabel5.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel5.text")); // NOI18N
 
         jLabel1.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel1.text")); // NOI18N
 
         jLabel2.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel2.text")); // NOI18N
 
         jLabel7.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel7.text")); // NOI18N
 
         browseButton.setText(bundle.getString("AudioTreatmentComponentProperties.browseButton.text")); // NOI18N
         browseButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 browseButtonActionPerformed(evt);
             }
         });
 
         jLabel10.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel10.text")); // NOI18N
 
         buttonGroup1.add(alwaysRadioButton);
         alwaysRadioButton.setSelected(true);
         alwaysRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.alwaysRadioButton.text")); // NOI18N
         alwaysRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 alwaysRadioButtonActionPerformed(evt);
             }
         });
 
         buttonGroup1.add(proximityRadioButton);
         proximityRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.proximityRadioButton.text")); // NOI18N
         proximityRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 proximityRadioButtonActionPerformed(evt);
             }
         });
 
         buttonGroup1.add(manualRadioButton);
         manualRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.manualRadioButton.text")); // NOI18N
         manualRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 manualRadioButtonActionPerformed(evt);
             }
         });
 
         jLabel11.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel11.text")); // NOI18N
 
         extentRadiusSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 extentRadiusSpinnerStateChanged(evt);
             }
         });
 
         jLabel12.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel12.text")); // NOI18N
 
         fullVolumeAreaPercentSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 fullVolumeAreaPercentSpinnerStateChanged(evt);
             }
         });
 
         jLabel13.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel13.text")); // NOI18N
 
         jLabel14.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel14.text")); // NOI18N
 
         buttonGroup3.add(ambientRadioButton);
         ambientRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.ambientRadioButton.text")); // NOI18N
         ambientRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ambientRadioButtonActionPerformed(evt);
             }
         });
 
         buttonGroup3.add(distanceAttenuatedRadioButton);
         distanceAttenuatedRadioButton.setSelected(true);
         distanceAttenuatedRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.distanceAttenuatedRadioButton.text")); // NOI18N
         distanceAttenuatedRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 distanceAttenuatedRadioButtonActionPerformed(evt);
             }
         });
 
         falloffSlider.setMinorTickSpacing(10);
         falloffSlider.setPaintTicks(true);
         falloffSlider.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 falloffSliderStateChanged(evt);
             }
         });
 
         jLabel3.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel3.text")); // NOI18N
 
         audioCapabilitiesLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/AudioCapabilitiesDiagram_en.png"))); // NOI18N
 
         jLabel15.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel15.text")); // NOI18N
 
         volumeSlider.setMajorTickSpacing(1);
         volumeSlider.setMaximum(10);
         volumeSlider.setPaintLabels(true);
         volumeSlider.setPaintTicks(true);
         volumeSlider.setValue(5);
         volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 volumeSliderStateChanged(evt);
             }
         });
 
         playOnceCheckBox.setText(bundle.getString("AudioTreatmentComponentProperties.playOnceCheckBox.text")); // NOI18N
         playOnceCheckBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 playOnceCheckBoxActionPerformed(evt);
             }
         });
 
         showBoundsCheckBox.setText(bundle.getString("AudioTreatmentComponentProperties.showBoundsCheckBox.text")); // NOI18N
         showBoundsCheckBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 showBoundsCheckBoxActionPerformed(evt);
             }
         });
 
         buttonGroup2.add(specifyRadiusRadioButton);
         specifyRadiusRadioButton.setSelected(true);
         specifyRadiusRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.specifyRadiusRadioButton.text")); // NOI18N
         specifyRadiusRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 specifyRadiusRadioButtonActionPerformed(evt);
             }
         });
 
         buttonGroup2.add(useCellBoundsRadioButton);
         useCellBoundsRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.useCellBoundsRadioButton.text")); // NOI18N
         useCellBoundsRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 useCellBoundsRadioButtonActionPerformed(evt);
             }
         });
 
         cellBoundsLabel.setText(bundle.getString("AudioTreatmentComponentProperties.cellBoundsLabel.text")); // NOI18N
 
         jLabel4.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel4.text")); // NOI18N
 
         statusLabel.setText(bundle.getString("AudioTreatmentComponentProperties.statusLabel.text")); // NOI18N
 
         buttonGroup4.add(fileRadioButton);
         fileRadioButton.setSelected(true);
         fileRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.fileRadioButton.text")); // NOI18N
         fileRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fileRadioButtonActionPerformed(evt);
             }
         });
 
         buttonGroup4.add(contentRepositoryRadioButton);
         contentRepositoryRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.contentRepositoryRadioButton.text")); // NOI18N
         contentRepositoryRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 contentRepositoryRadioButtonActionPerformed(evt);
             }
         });
 
         buttonGroup4.add(URLRadioButton);
         URLRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.URLRadioButton.text")); // NOI18N
         URLRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 URLRadioButtonActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .addContainerGap()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(layout.createSequentialGroup()
                                 .add(23, 23, 23)
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                     .add(jLabel1)
                                     .add(jLabel2)
                                     .add(jLabel10))
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(alwaysRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                     .add(layout.createSequentialGroup()
                                         .add(fileRadioButton)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(contentRepositoryRadioButton)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                         .add(URLRadioButton))
                                     .add(layout.createSequentialGroup()
                                         .add(treatmentTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 241, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(browseButton))
                                     .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 255, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                     .add(proximityRadioButton)
                                     .add(manualRadioButton)))
                             .add(layout.createSequentialGroup()
                                 .add(151, 151, 151)
                                 .add(jLabel3)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                 .add(jLabel15)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(falloffSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 174, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(jLabel5))
                             .add(layout.createSequentialGroup()
                                 .add(13, 13, 13)
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                     .add(jLabel7)
                                     .add(jLabel11)
                                     .add(jLabel14)
                                     .add(jLabel4))
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(layout.createSequentialGroup()
                                         .add(specifyRadiusRadioButton)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(extentRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(useCellBoundsRadioButton))
                                     .add(layout.createSequentialGroup()
                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                             .add(distanceAttenuatedRadioButton)
                                             .add(ambientRadioButton))
                                         .add(22, 22, 22)
                                         .add(audioCapabilitiesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 157, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                     .add(layout.createSequentialGroup()
                                         .add(143, 143, 143)
                                         .add(cellBoundsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 104, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                     .add(showBoundsCheckBox)
                                     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                         .add(org.jdesktop.layout.GroupLayout.LEADING, statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                         .add(org.jdesktop.layout.GroupLayout.LEADING, audioGroupIdTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE))))))
                     .add(layout.createSequentialGroup()
                         .add(160, 160, 160)
                         .add(playOnceCheckBox))
                     .add(layout.createSequentialGroup()
                         .addContainerGap()
                         .add(jLabel12)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(fullVolumeAreaPercentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jLabel13)))
                 .add(52, 52, 52))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(33, 33, 33)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(fileRadioButton)
                     .add(jLabel1)
                     .add(URLRadioButton)
                     .add(contentRepositoryRadioButton))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(treatmentTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(browseButton))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(12, 12, 12)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(jLabel10)
                     .add(alwaysRadioButton))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(proximityRadioButton)
                 .add(12, 12, 12)
                 .add(manualRadioButton)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(playOnceCheckBox)
                 .add(13, 13, 13)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel11)
                     .add(specifyRadiusRadioButton)
                     .add(extentRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(useCellBoundsRadioButton))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(showBoundsCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(cellBoundsLabel)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(11, 11, 11)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel12)
                             .add(fullVolumeAreaPercentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(jLabel13))
                         .add(29, 29, 29)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(ambientRadioButton)
                             .add(jLabel14))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(distanceAttenuatedRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(audioCapabilitiesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(6, 6, 6)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                         .add(layout.createSequentialGroup()
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                             .add(falloffSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                 .add(jLabel15)
                                 .add(jLabel3))
                             .add(21, 21, 21)))
                     .add(layout.createSequentialGroup()
                         .add(jLabel5)
                         .add(18, 18, 18)))
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel7)
                     .add(audioGroupIdTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE)
                     .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE))
                 .add(54, 54, 54))
         );
     }// </editor-fold>//GEN-END:initComponents
 
 private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
     if (treatmentType.equals(TreatmentType.FILE)) {
         JFileChooser chooser = new JFileChooser(treatmentTextField.getText());
 
         int returnVal = chooser.showOpenDialog(this);
 
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             treatmentTextField.setText("file://" + chooser.getSelectedFile().getAbsolutePath());
         }
     } else if (treatmentType.equals(TreatmentType.CONTENT_REPOSITORY)) {
 	// display a GUI to browser the content repository. Wait until OK has been
         // selected and fill in the text field with the URI
 	// Fetch the browser for the webdav protocol and display it.
         // Add a listener for the result and update the value of the
         // text field for the URI
         ContentBrowserManager manager = ContentBrowserManager.getContentBrowserManager();
 	final ContentBrowserSPI browser = manager.getDefaultContentBrowser();
 	browser.addContentBrowserListener(new ContentBrowserListener() {
 
             public void okAction(String uri) {
                 treatmentTextField.setText(uri);
                 browser.removeContentBrowserListener(this);
             }
 
             public void cancelAction() {
                 browser.removeContentBrowserListener(this);
             }
         });
         browser.setVisible(true);
     }
 }//GEN-LAST:event_browseButtonActionPerformed
 
 private void alwaysRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alwaysRadioButtonActionPerformed
     playWhen = PlayWhen.ALWAYS;
 
     if (editor != null) {
         editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
     }
 }//GEN-LAST:event_alwaysRadioButtonActionPerformed
 
 private void proximityRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proximityRadioButtonActionPerformed
     playWhen = PlayWhen.FIRST_IN_RANGE;
 
     if (editor != null) {
         editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
     }
 }//GEN-LAST:event_proximityRadioButtonActionPerformed
 
 private void manualRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualRadioButtonActionPerformed
     playWhen = PlayWhen.MANUAL;
 
     if (editor != null) {
         editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
     }
 }//GEN-LAST:event_manualRadioButtonActionPerformed
 
 private void extentRadiusSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_extentRadiusSpinnerStateChanged
     if (editor != null) {
         editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
     }
 }//GEN-LAST:event_extentRadiusSpinnerStateChanged
 
 private void fullVolumeAreaPercentSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fullVolumeAreaPercentSpinnerStateChanged
     if (editor != null) {
         editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
     }
 }//GEN-LAST:event_fullVolumeAreaPercentSpinnerStateChanged
 
 private void falloffSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_falloffSliderStateChanged
     if (editor != null) {
         editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
     }
 }//GEN-LAST:event_falloffSliderStateChanged
 
 private void distanceAttenuatedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distanceAttenuatedRadioButtonActionPerformed
     falloffSlider.setEnabled(true);
 
     distanceAttenuated = distanceAttenuatedRadioButton.isSelected();
 
     if (editor != null) {
         editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
     }
 }//GEN-LAST:event_distanceAttenuatedRadioButtonActionPerformed
 
 private void ambientRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ambientRadioButtonActionPerformed
     falloffSlider.setEnabled(ambientRadioButton.isSelected() == false);
     distanceAttenuated = (ambientRadioButton.isSelected() == false);
 
     if (editor != null) {
         editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
     }
 }//GEN-LAST:event_ambientRadioButtonActionPerformed
 
 private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSliderStateChanged
     if (editor != null) {
         editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
     }
 }//GEN-LAST:event_volumeSliderStateChanged
 
 private void playOnceCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playOnceCheckBoxActionPerformed
     playOnce = playOnceCheckBox.isSelected();
 }//GEN-LAST:event_playOnceCheckBoxActionPerformed
 
 private void showBoundsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showBoundsCheckBoxActionPerformed
         if (editor == null) {
             return;
         }
 
         editor.setPanelDirty(ConeOfSilenceComponentProperties.class, isDirty());
 
         showBounds();
 }//GEN-LAST:event_showBoundsCheckBoxActionPerformed
 
 private void specifyRadiusRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specifyRadiusRadioButtonActionPerformed
     useCellBounds = specifyRadiusRadioButton.isSelected() == false;
 
     distanceAttenuatedRadioButton.setEnabled(useCellBounds == false);
 
     extentRadiusSpinner.setEnabled(useCellBounds == false);
 
     if (editor != null) {
         editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
 
 	showBounds();
     }
 }//GEN-LAST:event_specifyRadiusRadioButtonActionPerformed
 
 private void useCellBoundsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useCellBoundsRadioButtonActionPerformed
     
     useCellBounds = useCellBoundsRadioButton.isSelected();
 
     extentRadiusSpinner.setEnabled(useCellBounds == false);
 
     BoundingVolume bounds = editor.getCell().getLocalBounds();
 
     if (useCellBounds == true) {
 	if (bounds instanceof BoundingBox) {
 	    distanceAttenuatedRadioButton.setSelected(false);
 	    distanceAttenuatedRadioButton.setEnabled(false);
 	    ambientRadioButton.setSelected(true);
 	}
     } else {
 	distanceAttenuatedRadioButton.setEnabled(true);
     }
 
     if (editor != null) {
         editor.setPanelDirty(AudioTreatmentComponentProperties.class, isDirty());
 
 	showBounds();
     }
 }//GEN-LAST:event_useCellBoundsRadioButtonActionPerformed
 
 private void fileRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileRadioButtonActionPerformed
     if (fileRadioButton.isSelected() == false) {
 	return;
     }
 
     if (treatmentType.equals(TreatmentType.FILE) == false) {
 	if (lastFileTreatment != null) {
 	    treatmentTextField.setText(lastFileTreatment);
 	} else {
 	    treatmentTextField.setText("");
 	}
     }
 
     treatmentType = TreatmentType.FILE;
     browseButton.setEnabled(true);
 }//GEN-LAST:event_fileRadioButtonActionPerformed
 
 private void contentRepositoryRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentRepositoryRadioButtonActionPerformed
     if (contentRepositoryRadioButton.isSelected() == false) {
 	return;
     }
 
     if (treatmentType.equals(TreatmentType.CONTENT_REPOSITORY) == false) {
 	if (lastContentRepositoryTreatment != null) {
 	    treatmentTextField.setText(lastContentRepositoryTreatment);
 	} else {
             treatmentTextField.setText("");
 	}
     }
 
     treatmentType = TreatmentType.CONTENT_REPOSITORY;
 
     browseButton.setEnabled(true);
 }//GEN-LAST:event_contentRepositoryRadioButtonActionPerformed
 
 private void URLRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_URLRadioButtonActionPerformed
     if (URLRadioButton.isSelected() == false) {
 	return;
     }
 
     if (treatmentType.equals(TreatmentType.URL) == false) {
 	if (lastURLTreatment != null) {
 	    treatmentTextField.setText(lastURLTreatment);
 	} else {
             treatmentTextField.setText("");
 	}
     }
 
     treatmentType = TreatmentType.URL;
     browseButton.setEnabled(false);
 }//GEN-LAST:event_URLRadioButtonActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JRadioButton URLRadioButton;
     private javax.swing.JRadioButton alwaysRadioButton;
     private javax.swing.JRadioButton ambientRadioButton;
     private javax.swing.JLabel audioCapabilitiesLabel;
     private javax.swing.JTextField audioGroupIdTextField;
     private javax.swing.JButton browseButton;
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.ButtonGroup buttonGroup2;
     private javax.swing.ButtonGroup buttonGroup3;
     private javax.swing.ButtonGroup buttonGroup4;
     private javax.swing.JLabel cellBoundsLabel;
     private javax.swing.JRadioButton contentRepositoryRadioButton;
     private javax.swing.JRadioButton distanceAttenuatedRadioButton;
     private javax.swing.JSpinner extentRadiusSpinner;
     private javax.swing.JSlider falloffSlider;
     private javax.swing.JRadioButton fileRadioButton;
     private javax.swing.JSpinner fullVolumeAreaPercentSpinner;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JRadioButton manualRadioButton;
     private javax.swing.JCheckBox playOnceCheckBox;
     private javax.swing.JRadioButton proximityRadioButton;
     private javax.swing.JCheckBox showBoundsCheckBox;
     private javax.swing.JRadioButton specifyRadiusRadioButton;
     private javax.swing.JLabel statusLabel;
     private javax.swing.JTextField treatmentTextField;
     private javax.swing.JRadioButton useCellBoundsRadioButton;
     private javax.swing.JSlider volumeSlider;
     // End of variables declaration//GEN-END:variables
 }
