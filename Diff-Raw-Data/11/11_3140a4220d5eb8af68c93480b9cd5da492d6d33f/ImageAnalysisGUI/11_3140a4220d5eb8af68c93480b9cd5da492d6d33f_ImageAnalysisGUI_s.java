 package plugins.nherve.toolbox.imageanalysis;
 
 import icy.gui.dialog.MessageDialog;
 import icy.gui.frame.IcyFrame;
 import icy.gui.util.GuiUtil;
 import icy.gui.util.WindowPositionSaver;
 import icy.sequence.Sequence;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import plugins.nherve.maskeditor.MaskEditor;
 import plugins.nherve.toolbox.concurrent.TaskManager;
 import plugins.nherve.toolbox.image.mask.MaskStack;
 import plugins.nherve.toolbox.plugin.BackupSingletonPlugin;
 
 public abstract class ImageAnalysisGUI extends BackupSingletonPlugin<ImageAnalysisContext> implements ActionListener, ItemListener, ImageAnalysisProcessListener {
 
 	private ImageAnalysisModule module;
 	private ImageAnalysisParameters defaultParameters;
 	private IcyFrame frame;
 	private JPanel mainPanel;
 	private JPanel moduleGUI;
 	private JButton btOpenMaskEditor;
 	private JButton btStart;
 	private JButton btClear;
 	private JButton btStop;
 	private JCheckBox cbDisplay;
 
 	private ImageAnalysisContext noSequenceContext;
 
 	public ImageAnalysisGUI() {
 		super();
 		setDefaultParameters(new ImageAnalysisParameters());
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		Object o = e.getSource();
 
 		if (o == null) {
 			return;
 		}
 
 		if (o instanceof JButton) {
 			JButton b = (JButton) o;
 			if (b == btOpenMaskEditor) {
 				MaskEditor.getRunningInstance(true);
 			} else if ((b == btStart) || (b == btClear) || (b == btStop)) {
 				if (module.needSequence()) {
 					if (hasCurrentSequence()) {
 						Sequence current = getCurrentSequence();
 						if (current != null) {
 							ImageAnalysisContext context = getBackupObject();
 
 							MaskEditor me = MaskEditor.getRunningInstance(true);
 							MaskStack stack = me.getBackupObject();
 							context.setStack(stack);
 
 							if (b == btStart) {
 								module.getParametersFromGui(moduleGUI, context);
 
 								try {
 									btStart.setEnabled(false);
 									if (!context.processAndNotify(module, this, isLogEnabled())) {
 										btStart.setEnabled(true);
 										MessageDialog.showDialog("Unable to launch", MessageDialog.INFORMATION_MESSAGE);
 									}
 								} catch (ImageAnalysisException e1) {
 									MessageDialog.showDialog(e1.getClass().getName(), e1.getMessage(), MessageDialog.ERROR_MESSAGE);
 								}
 							} else if (b == btClear) {
 								stack.removeMaskWithTag(module.getName());
 								module.setState(ImageAnalysisModule.STOPPED, true);
 							} else if (b == btStop) {
 								context.stopRunningProcesses();
 							}
 
 							me.refreshInterface();
 						}
 					}
 				} else {
 					if (b == btStart) {
 						noSequenceContext = new ImageAnalysisContext();
 						try {
 							noSequenceContext.addAllParameters(defaultParameters, true);
 						} catch (ImageAnalysisParameterException ex) {
 							throw new RuntimeException(ex);
 						}
 
 						module.getParametersFromGui(moduleGUI, noSequenceContext);
 
 						try {
 							btStart.setEnabled(false);
 							if (!noSequenceContext.processAndNotify(module, this, isLogEnabled())) {
 								btStart.setEnabled(true);
 								MessageDialog.showDialog("Unable to launch", MessageDialog.INFORMATION_MESSAGE);
 							}
 						} catch (ImageAnalysisException e1) {
 							MessageDialog.showDialog(e1.getClass().getName(), e1.getMessage(), MessageDialog.ERROR_MESSAGE);
 						}
 					} else if (b == btStop) {
 						if (noSequenceContext != null) {
 							noSequenceContext.stopRunningProcesses();
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void backupCurrentSequence() {
 		ImageAnalysisContext context = null;
 		if (!hasBackupObject()) {
 			context = new ImageAnalysisContext();
 
 			try {
 				context.addAllParameters(defaultParameters, true);
 			} catch (ImageAnalysisParameterException e) {
 				throw new RuntimeException(e);
 			}
 			context.setWorkingImage(getCurrentSequence().getFirstImage());
 
 			addBackupObject(context);
 		} else {
 			context = getBackupObject();
 		}
 
		MaskEditor me = MaskEditor.getRunningInstance(true);
		MaskStack stack = me.getBackupObject();
		context.setStack(stack);
 	}
 
 	protected ImageAnalysisParameters getDefaultParameters() {
 		return defaultParameters;
 	}
 
 	protected ImageAnalysisModule getModule() {
 		return module;
 	}
 
 	public String getName() {
 		return getDescriptor().getName();
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		Object o = e.getSource();
 
 		if (o == null) {
 			return;
 		}
 
 		if (o instanceof JCheckBox) {
 			JCheckBox c = (JCheckBox) e.getSource();
 
 			if (c == cbDisplay) {
 				setLogEnabled(cbDisplay.isSelected());
 				module.setLogEnabled(cbDisplay.isSelected());
 			}
 		}
 	}
 
 	@Override
 	public void restoreCurrentSequence(boolean refresh) {
 
 	}
 
 	@Override
 	public void sequenceHasChanged() {
 
 	}
 
 	@Override
 	public void sequenceWillChange() {
 
 	}
 
 	protected void setDefaultParameters(ImageAnalysisParameters defaultParameters) {
 		this.defaultParameters = defaultParameters;
 	}
 
 	protected void setModule(ImageAnalysisModule m) {
 		this.module = m;
 	}
 
 	@Override
 	public void startInterface() {
 		TaskManager.initAll();
 
 		mainPanel = GuiUtil.generatePanel();
 		frame = GuiUtil.generateTitleFrame(module.getName(), mainPanel, new Dimension(500, 100), true, true, true, true);
 		new WindowPositionSaver(frame, getPreferences().absolutePath(), new Point(0, 0), new Dimension(500, 800));
 		mainPanel.setLayout(new BorderLayout());
 
 		moduleGUI = module.createGUI(getDefaultParameters());
 		if (moduleGUI != null) {
 			mainPanel.add(new JScrollPane(GuiUtil.createPageBoxPanel(moduleGUI, Box.createVerticalGlue())), BorderLayout.CENTER);
 		}
 
 		btStart = new JButton("Launch");
 		btStart.setToolTipText("Launch " + getName());
 		btStart.addActionListener(this);
 
 		if (module.needSequence()) {
 			btClear = new JButton("Clear");
 			btClear.setToolTipText("Clear " + getName() + " results");
 			btClear.addActionListener(this);
 
 			btOpenMaskEditor = new JButton("Open MaskEditor");
 			btOpenMaskEditor.addActionListener(this);
 		}
 
 		btStop = new JButton("Stop");
 		btStop.setToolTipText("Stop current process");
 		btStop.addActionListener(this);
 
 		cbDisplay = new JCheckBox("Log");
 		cbDisplay.addItemListener(this);
 
 		JPanel buttons = null;
 		if (module.needSequence()) {
 			buttons = GuiUtil.createLineBoxPanel(Box.createHorizontalGlue(), cbDisplay, Box.createHorizontalGlue(), btStart, Box.createHorizontalGlue(), btClear, Box.createHorizontalGlue(), btStop, Box.createHorizontalGlue(), btOpenMaskEditor, Box.createHorizontalGlue());
 		} else {
 			buttons = GuiUtil.createLineBoxPanel(Box.createHorizontalGlue(), cbDisplay, Box.createHorizontalGlue(), btStart, Box.createHorizontalGlue(), btStop, Box.createHorizontalGlue());
 		}
 		mainPanel.add(buttons, BorderLayout.SOUTH);
 
 		frame.addFrameListener(this);
 		frame.setVisible(true);
 		frame.pack();
 		addIcyFrame(frame);
 
 		setLogEnabled(cbDisplay.isSelected());
 		if (module != null) {
 			module.setLogEnabled(cbDisplay.isSelected());
 		}
 
 		frame.requestFocus();
 	}
 
 	@Override
 	public void notifyProcessEnded(ImageAnalysisModule module) {
 		btStart.setEnabled(true);
 		if (hasCurrentSequence()) {
 			ImageAnalysisContext context = getBackupObject();
 			context.reInitProcessor();
 		}
 	}
 
 	@Override
 	public void stopInterface() {
	
 	}
 
 }
