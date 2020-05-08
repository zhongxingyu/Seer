 /*
  * Part of Fresco software under GPL licence
  * http://www.gnu.org/licenses/gpl-3.0.txt
  */
 package fresco.action;
 
 import file.CImageFile;
 import fresco.CData;
 import fresco.CImageContainer;
 import fresco.CImageContainer.Screen;
 import fresco.action.IAction.RegID;
 import fresco.swing.CContentPane;
 import fresco.swing.CContentPane.Structure;
 import fresco.swing.CDrawPanel;
 import fresco.swing.CInfoFrame;
 import fresco.swing.CPreviewBar;
 import java.awt.image.BufferedImage;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import support.regmarks.CPointPairsOverview;
 import workers.CImageWorker;
 import workers.registration.CPointPairs;
 import workers.registration.refpointga.CRefPointMarker;
 import workers.segmentation.CSegmentMap;
 
 /**
  * Holds all user actions in Fresco application
  * @author gimli
  */
 public class CActionManager {
 
 	CImageWorker imageWorker;
 	CStatusManager progressBar;
 	CDrawPanel[] imagePanel;
 	private CContentPane content;
 	private CPreviewBar previewBar;
 	private static final Logger logger = Logger.getLogger(CActionManager.class.getName());
 
 	/**
 	 * Constructor have to handle a lot of gui panels
 	 * @param imagePanel three panels for image painting
 	 * @param content main window with images
 	 * @param previewBar shortcut bar for image processing
 	 * @param progressBar handler of progress bar for showing image worker progress
 	 */
 	public CActionManager(CDrawPanel[] imagePanel, CContentPane content, CPreviewBar previewBar, CStatusManager progressBar) {
 		this.imagePanel = imagePanel;
 		this.previewBar = previewBar;
 		this.progressBar = progressBar;
 		this.content = content;
 	}
 
 	/**
 	 * Tool for image loading from files
 	 */
 	public void loadImage() {
 		CImageFile.openPicture();
 	}
 
 	public void fireImageListChanged() {
 		logger.info("Set of pictures changed ... update");
 
 		if (CData.showImage[0] == -1 && CData.imagesSize() != 0) {
 			CData.showImage[0] = 0;
 			loadContainer(0, 0);
 		}
 		previewBar.refreshBar();
 		refreshGUI();
 	}
 
 	/**
 	 * Every user action calls refreshGUI, images are repainted, sizes are
 	 * set once more etc.
 	 */
 	private static void refreshGUI() {
 		CData.mainFrame.checkEnabled();
 		CData.mainFrame.repaint();
 		((CContentPane) CData.mainFrame.getContentPane()).getInputs().repaint();
 	}
 
 	/**
 	 * Warning before image closing is generated there
 	 * @param id image specification
 	 */
 	public void closePicture(int id) {
 		if (CData.getImage(id).isChanged()) { // image is not saved
 			int userChoose = JOptionPane.showOptionDialog(new JFrame("Image closing"),
 					"Warning: Image is not saved. Do you want to save it before closing it?",
 					"Image closing", JOptionPane.YES_NO_CANCEL_OPTION,
 					JOptionPane.QUESTION_MESSAGE, null, null, null);
 
 			if (userChoose == JOptionPane.CANCEL_OPTION || userChoose == JOptionPane.CLOSED_OPTION) {
 				return;
 			} else if (userChoose == JOptionPane.OK_OPTION) {
 				savePicture(id);
 			}
 		}
 
 		CData.removeImage(id);
 		for (int i = 0; i < CData.showImage.length; i++) {
 			if (CData.showImage[i] > id) {
 				CData.showImage[i]--;
 			} else if (CData.showImage[i] == id) {
 				CData.showImage[i] = -1;
 			}
 		}
 		previewBar.refreshBar();
 		refreshGUI();
 	}
 
 	public void renamePicture(int id) {
 		if (id < 0 || id > CData.imagesSize()) {
 			return;
 		}
 
 		String filename = (String) JOptionPane.showInputDialog(
 				new JFrame("Rename ..."),
 				"Enter new name: ", "Rename ...",
 				JOptionPane.PLAIN_MESSAGE, null, null,
 				CData.getImage(id).getFilename());
 
 		if (filename != null && filename.length() > 0) {
 			CData.getImage(id).setFilename(filename);
 			previewBar.refreshBar();
 		}
 	}
 
 	public void saveAsPicture(int id) {
		String imageNewName = CImageFile.saveAsPicture(CData.getImage(id));
		if (imageNewName != null) {
			CData.getImage(id).setFilename(imageNewName);
			previewBar.refreshBar();
		}
 	}
 
 	public void savePicture(int id) {
 		throw new UnsupportedOperationException("Not yet implemented");
 	}
 
 	public void killProcess() {
 		imageWorker.cancel(true);
 		imageWorker.removePropertyChangeListener(progressBar);
 		imageWorker = null;
 	}
 
 	/**
 	 * User can choose from different types of view
 	 * @param id defines type of view (CData.View)
 	 */
 	public void switchView(Screen id) {
 		CData.view = id;
 		refreshGUI();
 	}
 
 	/**
 	 * This action is manager of CData.input2showed field
 	 */
 	public void show2ndInput() {
 		CData.input2Showed = !CData.input2Showed;
 
 		if (CData.input2Showed) {
 			content.setStructure(content.getTwoImageStructure());
 		} else {
 			content.setStructure(Structure.oneInput);
 		}
 
 		CData.mainFrame.getJMenuBar().getMenu(2).getItem(0).setSelected(CData.input2Showed);
 	}
 
 	/**
 	 * Action manages user defined zoom value
 	 * @param percentage specify size of showed image in percents of real size
 	 */
 	public void setZoom(int percentage) {
 		CData.setFocus(percentage);
 		for (int i = 0; i < imagePanel.length; i++) {
 			if (i < 3 && CData.showImage[i] != -1) {
 				imagePanel[i].setSizeByImage();
 				logger.log(Level.FINE, "Image size values: {0}, {1}", new Object[]{imagePanel[i].getWidth(), imagePanel[i].getHeight()});
 			} else if (i > 2 && ((CContentPane) CData.mainFrame.getContentPane()).getStructure() == CContentPane.Structure.blend) {
 				imagePanel[i].setSizeByImage();
 			}
 		}
 	}
 
 	/**
 	 * Put specified container into specified image panel
 	 * @param imagePanelID definition of image panel [0,1,2]
 	 * @param containerID definition of container
 	 */
 	public void loadContainer(int imagePanelID, int containerID) {
 		if (imagePanelID == 2 && !CData.input2Showed) {
 			show2ndInput();
 		}
 
 		if (CData.imagesSize() > containerID) {
 			CData.showImage[imagePanelID] = containerID;
 		}
 
 		imagePanel[imagePanelID].setSizeByImage();
 		refreshGUI();
 	}
 
 	private void loadOutput() {
 		if (CData.output == null) {
 			return;
 		}
 		if (CData.showImage[1] != -2) {
 			CData.showImage[1] = -2;
 		}
 		imagePanel[1].setSizeByImage();
 	}
 
 	/**
 	 * Simplified multi-action which manage image container
 	 * @param action defines type of user action
 	 * @param containerID defines which container will be managed
 	 */
 	public void containerDo(RegID action, int containerID) {
 		switch (action) {
 			case close:
 				closePicture(containerID);
 				break;
 			case rename:
 				renamePicture(containerID);
 				break;
 			case open:
 				loadContainer(0, containerID);
 				break;
 			case saveAs:
 				saveAsPicture(containerID);
 				break;
 			case load2ndInput:
 				loadContainer(2, containerID);
 				break;
 		}
 	}
 
 	public void showInfo() {
 		JFrame infoFrame = new CInfoFrame(CData.getImage(CData.showImage[0]));
 		infoFrame.setVisible(true);
 	}
 
 	public void runImageWorker(RegID id, Object[] params) {
 		imageWorker = CImageWorker.createWorker(id, params);
 
 		JDialog dialog = imageWorker.getParamSettingDialog();
 		if (dialog != null) {
 			dialog.setVisible(true);
 		}
 
 		imageWorker.addPropertyChangeListener(progressBar);
 		imageWorker.execute();
 	}
 
 	public void cleanImageWorker() {
 		if (imageWorker == null) {
 			return;
 		}
 		imageWorker.removePropertyChangeListener(progressBar);
 		try {
 			logger.log(Level.INFO, "{0}, {1}", new Object[]{imageWorker.getTypeName(), imageWorker.getWorkerName()});
 			switch (imageWorker.getType()) {
 				case ANALYSIS:
 					CData.output = new CImageContainer((BufferedImage) imageWorker.get(), true);
 					break;
 				case SEGMENTATION:
 					CData.output = new CImageContainer(CData.getImage(CData.showImage[0]).getImage(), false);
 					CData.output.setSegmentMap((CSegmentMap) imageWorker.get());
 					break;
 				case REGISTRATION:
 					CData.output = new CImageContainer((BufferedImage) imageWorker.get(), true);
 					break;
 				case CORRECTOR:
 					CData.output = new CImageContainer((BufferedImage) imageWorker.get(), true);
 					break;
 				case MORPH:
 					CData.output = new CImageContainer(CData.getImage(CData.showImage[0]).getImage(), false);
 					CData.output.setSegmentMap((CSegmentMap) imageWorker.get());
 					break;
 				case SUPPORT:
 					if (imageWorker instanceof CRefPointMarker) {
 						logger.info("Marks distribution here");
 						CPointPairs pairs = (CPointPairs) imageWorker.get();
 
 						CData.getImage(CData.showImage[0]).setMarks(pairs.getOrigins());
 						CData.getImage(CData.showImage[2]).setMarks(pairs.getProjected());
 
 						return;
 					} else if (imageWorker instanceof CPointPairsOverview) {
 						((CPointPairsOverview)imageWorker).get().setVisible(true);
 						return;
 					} else {
 						CData.output = new CImageContainer((BufferedImage) imageWorker.get(), true);
 					}
 					break;
 			}
 			CData.output.setFilename(CData.getImage(CData.showImage[0]).getFilename());
 			CData.showImage[1] = -2;
 			loadOutput();
 			refreshGUI();
 		} catch (NullPointerException npe) {
 			logger.log(Level.INFO, "NullPointerException: Bad input params for worker or worker bug.{0}", npe.getMessage());
 		} catch (CancellationException ce) {
 			logger.info("Operation cancelled");
 		} catch (InterruptedException ie) {
 			logger.info("Operation interrupted");
 		} catch (ExecutionException ee) {
 			logger.log(Level.INFO, "Other exception: {0}", ee.getMessage());
 		}
 		imageWorker = null;
 	}
 
 	public void fixImage() {
 		if (CData.showImage[0] != -1 && CData.output != null) {
 			CData.getImage(CData.showImage[0]).setImage(CData.output.getTransformedImage(CData.view));
 		} else if (CData.output != null) {
 			captureImage();
 		} else {
 			JOptionPane.showMessageDialog(new JFrame(), "There is no output image to fix.", "Warning", JOptionPane.WARNING_MESSAGE);
 		}
 
 		previewBar.refreshBar();
 		refreshGUI();
 	}
 
 	public void captureImage() {
 		if (CData.output != null) {
 			CData.addImage(CData.output);
 		}
 		previewBar.refreshBar();
 		refreshGUI();
 	}
 
 	public void setContentStructure(RegID regID) {
 		content.setStructure(regID);
 		refreshGUI();
 	}
 }
