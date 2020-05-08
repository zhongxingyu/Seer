 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.client.tomo.alignment.view;
 
 import gda.device.DeviceException;
 import gda.images.camera.ImageListener;
 import gda.images.camera.MotionJpegOverHttpReceiverSwt;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentController;
 import uk.ac.gda.client.tomo.composites.FixedImageViewerComposite;
 import uk.ac.gda.client.tomo.composites.RotationSliderComposite;
 import uk.ac.gda.client.tomo.composites.RotationSliderComposite.SliderSelectionListener;
 import uk.ac.gda.client.tomo.composites.TomoCoarseRotationComposite;
 
 /**
  *
  */
 public class AlignmentConfigSaveDialog extends Dialog {
 
 	private static final String DEG_PLUS_180_lbl = "+180°";
 
 	private static final String DEG_PLUS_90_lbl = "+90°";
 
 	private static final String DEG_0_lbl = "0°";
 
 	private static final String DEG_MINUS_90_lbl = "-90°";
 
 	private static final String DEG_MINUS_180_lbl = "-180°";
 
 	private static final String THETA_90_lbl = "Theta + 90°";
 
 	private static final String THETA_lbl = "Theta";
 
 	private static final String SHELL_TITLE = "Save Tomography Alignment Configuration";
 
 	private ImageLocationRelTheta viewerButtonSelected = ImageLocationRelTheta.THETA;
 
 	private static final Logger logger = LoggerFactory.getLogger(AlignmentConfigSaveDialog.class);
 
 	private FormToolkit toolkit;
 
 	private final TomoAlignmentController tomoAlignmentViewController;
 
 	private TomoCoarseRotationComposite coarseRotation;
 
 	private Button btnTheta;
 
 	private Button btnThetaPlus90;
 
 	private FixedImageViewerComposite rightWindowViewer;
 
 	private FixedImageViewerComposite leftWindowViewer;
 
 	private final MotionJpegOverHttpReceiverSwt leftVideoReceiver;
 
 	private static final Color ENABLED = ColorConstants.lightGray;
 	private static final Color DISABLED = ColorConstants.darkGray;
 
 	protected AlignmentConfigSaveDialog(Shell parentShell, TomoAlignmentController tomoAlignmentViewController,
 			MotionJpegOverHttpReceiverSwt leftVideoReceiver) {
 		super(parentShell);
 		this.tomoAlignmentViewController = tomoAlignmentViewController;
 		this.leftVideoReceiver = leftVideoReceiver;
 	}
 
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText(SHELL_TITLE);
 		newShell.setSize(1500, 750);
 	}
 
 	private SelectionListener btnSelectionListener = new SelectionAdapter() {
 
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			if (e.getSource().equals(btnTheta)) {
 				viewerButtonSelected = ImageLocationRelTheta.THETA;
 				leftWindowViewer.setBackground(ENABLED);
 				rightWindowViewer.setBackground(DISABLED);
 				btnThetaPlus90.setEnabled(true);
 				btnTheta.setEnabled(false);
 				loadImageIntoViewer(rightWindowViewer);
 
 				moveRotationMotorBy(-90.0);
 			} else if (e.getSource().equals(btnThetaPlus90)) {
 				viewerButtonSelected = ImageLocationRelTheta.THETA_PLUS_90;
 				rightWindowViewer.setBackground(ENABLED);
 				leftWindowViewer.setBackground(DISABLED);
 				btnTheta.setEnabled(true);
 				btnThetaPlus90.setEnabled(false);
 				loadImageIntoViewer(leftWindowViewer);
 				moveRotationMotorBy(90.0);
 			}
 		}
 
 		private void loadImageIntoViewer(final FixedImageViewerComposite viewer) {
 
 			final Display display = getShell().getDisplay();
 			final Point size = viewer.getSize();
 			try {
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, false, new IRunnableWithProgress() {
 
 					@Override
 					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 
 						try {
 							String demandRawWithStreamOn = tomoAlignmentViewController.demandRawWithStreamOn(monitor,
 									false);
 							Image imgTemp = new Image(display, demandRawWithStreamOn);
 							final ImageData imgData = imgTemp.getImageData();
 							imgTemp.dispose();
 							final Dimension scaleValue = getScaleValue(imgData.width, imgData.height, size.x, size.y);
 							viewer.getDisplay().asyncExec(new Runnable() {
 
 								@Override
 								public void run() {
 									try {
 										viewer.loadMainImage(imgData.scaledTo(scaleValue.width, scaleValue.height));
 									} catch (Exception e) {
 										logger.error("Problem loading image into viewer", e);
 									}
 
 								}
 							});
 						} catch (Exception e) {
 							throw new InvocationTargetException(e);
 						} finally {
 							monitor.done();
 						}
 					}
 				});
 			} catch (InvocationTargetException e1) {
				logger.error("Unable to load image into viewer", e1);
 			} catch (InterruptedException e1) {
				logger.error("Process to load image into viewer interrupted, unable to load image into viewer", e1);
 			}
 		}
 	};
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		toolkit = new FormToolkit(parent.getDisplay());
 
 		Composite dialogAreaParent = (Composite) super.createDialogArea(parent);
 
 		Composite borderComposite = toolkit.createComposite(dialogAreaParent);
 		borderComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		borderComposite.setBackground(ColorConstants.black);
 		GridLayout gl = new GridLayout();
 		gl.marginWidth = 2;
 		gl.marginHeight = 2;
 		gl.horizontalSpacing = 0;
 		gl.verticalSpacing = 0;
 		borderComposite.setLayout(gl);
 
 		//
 		Composite rootComposite = toolkit.createComposite(borderComposite);
 
 		gl = new GridLayout();
 		gl.marginWidth = 0;
 		gl.marginHeight = 0;
 		gl.horizontalSpacing = 3;
 		gl.verticalSpacing = 3;
 		rootComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		rootComposite.setLayout(gl);
 		//
 		Composite imagesComposite = toolkit.createComposite(rootComposite);
 		imagesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		imagesComposite.setBackground(ColorConstants.black);
 		gl = new GridLayout(2, true);
 		gl.marginWidth = 0;
 		gl.marginHeight = 0;
 		gl.horizontalSpacing = 2;
 		gl.verticalSpacing = 0;
 		imagesComposite.setLayout(gl);
 
 		Composite leftImgComposite = toolkit.createComposite(imagesComposite);
 		leftImgComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		gl = new GridLayout();
 		gl.marginWidth = 0;
 		gl.marginHeight = 0;
 		gl.horizontalSpacing = 0;
 		gl.verticalSpacing = 2;
 		leftImgComposite.setLayout(gl);
 
 		leftWindowViewer = new FixedImageViewerComposite(leftImgComposite, SWT.None);
 		leftWindowViewer.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		btnTheta = toolkit.createButton(leftImgComposite, THETA_lbl, SWT.PUSH);
 		GridData layoutData = new GridData();
 		layoutData.horizontalAlignment = SWT.CENTER;
 		btnTheta.setLayoutData(layoutData);
 		btnTheta.setEnabled(false);
 		btnTheta.addSelectionListener(btnSelectionListener);
 		//
 		Composite rightImgComposite = toolkit.createComposite(imagesComposite);
 		rightImgComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		gl = new GridLayout();
 		gl.marginWidth = 0;
 		gl.marginHeight = 0;
 		gl.horizontalSpacing = 0;
 		gl.verticalSpacing = 2;
 		rightImgComposite.setLayout(gl);
 
 		rightWindowViewer = new FixedImageViewerComposite(rightImgComposite, SWT.None);
 		rightWindowViewer.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		btnThetaPlus90 = toolkit.createButton(rightImgComposite, THETA_90_lbl, SWT.PUSH);
 		layoutData = new GridData();
 		layoutData.horizontalAlignment = SWT.CENTER;
 		btnThetaPlus90.setLayoutData(layoutData);
 		btnThetaPlus90.addSelectionListener(btnSelectionListener);
 
 		coarseRotation = new TomoCoarseRotationComposite(rootComposite, SWT.DOWN, new String[] { DEG_MINUS_180_lbl,
 				DEG_MINUS_90_lbl, DEG_0_lbl, DEG_PLUS_90_lbl, DEG_PLUS_180_lbl }, true);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalAlignment = SWT.CENTER;
 
 		gd.heightHint = 55;
 		gd.widthHint = 1488;
 		coarseRotation.setLayoutData(gd);
 		//
 		leftWindowViewer.setBackground(ENABLED);
 		rightWindowViewer.setBackground(DISABLED);
 		coarseRotation.addSliderEventListener(sliderSelectionListener);
 		leftVideoReceiver.addImageListener(imgListener);
 		tomoAlignmentViewController.addRotationMotorListener(rotationMotorListener);
 		return dialogAreaParent;
 	}
 
 	private ImageListener<ImageData> imgListener = new ImageListener<ImageData>() {
 
 		private String name;
 
 		@Override
 		public void setName(String name) {
 			this.name = name;
 
 		}
 
 		@Override
 		public String getName() {
 			return name;
 		}
 
 		@Override
 		public void processImage(final ImageData image) {
 			if (!getShell().getDisplay().isDisposed()) {
 				getShell().getDisplay().asyncExec(new Runnable() {
 
 					@Override
 					public void run() {
 						if (viewerButtonSelected.equals(ImageLocationRelTheta.THETA)) {
 							try {
 
 								Rectangle bounds = leftWindowViewer.getBounds();
 								Dimension dim = getScaleValue(image.width, image.height, bounds.width, bounds.height);
 
 								leftWindowViewer.loadMainImage(image.scaledTo(dim.width, dim.height));
 							} catch (Exception e) {
								logger.error("Problem loading image into left window viewer", e);
 							}
 						} else if (viewerButtonSelected.equals(ImageLocationRelTheta.THETA_PLUS_90)) {
 							try {
 								Rectangle bounds = rightWindowViewer.getBounds();
 								Dimension dim = getScaleValue(image.width, image.height, bounds.width, bounds.height);
 								rightWindowViewer.loadMainImage(image.scaledTo(dim.width, dim.height));
 							} catch (Exception e) {
								logger.error("Problem loading image into right window viewer", e);
 							}
 						}
 					}
 
 				});
 			}
 		}
 	};
 
 	@Override
 	protected void initializeBounds() {
 		super.initializeBounds();
 
 		getContents().addPaintListener(new PaintListener() {
 			private boolean firstTime = true;
 
 			@Override
 			public void paintControl(PaintEvent e) {
 				if (firstTime) {
 					firstTime = false;
 					try {
 						coarseRotation.moveSliderTo(tomoAlignmentViewController.getRotationMotorDeg());
 					} catch (DeviceException de) {
						logger.error("Sample Stage Motor Error: unable to get the rotation motor position ", e);
 					}
 				}
 			}
 		});
 	}
 
 	private Dimension getScaleValue(int imageWidth, int imageHeight, int rectWidth, int rectHeight) {
 		if (imageWidth < rectWidth && imageHeight < rectHeight) {
 			return new Dimension(imageWidth, imageHeight);
 		}
 		if (imageWidth > rectWidth) {
 			double counter = 1;
 			while (counter > 0) {
 				counter = counter - 0.02;
 				double imageWidthScaled = imageWidth * counter;
 				if (imageWidthScaled < rectWidth) {
 					double imageHeightScaled = imageHeight * counter;
 					return new Dimension((int) imageWidthScaled, (int) imageHeightScaled);
 				}
 			}
 		} else if (imageHeight > rectHeight) {
 			double counter = 1;
 			while (counter > 0) {
 				counter = counter - 0.02;
 				double imageHeightScaled = imageHeight * counter;
 				if (imageHeightScaled < rectHeight) {
 					double imageWidthScaled = imageWidth * counter;
 					return new Dimension((int) imageWidthScaled, (int) imageHeightScaled);
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	protected void okPressed() {
 		super.okPressed();
 	}
 
 	@Override
 	public boolean close() {
 		coarseRotation.removeSliderEventListener(sliderSelectionListener);
 		tomoAlignmentViewController.removeRotationMotorListener(rotationMotorListener);
 		leftVideoReceiver.removeImageListener(imgListener);
 		return super.close();
 	}
 
 	private void moveRotationMotorTo(final double deg) {
 		try {
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, false, new IRunnableWithProgress() {
 
 				@Override
 				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 					try {
 						tomoAlignmentViewController.moveRotationMotorTo(monitor, deg);
 						coarseRotation.showButtonDeSelected();
 					} catch (Exception ex) {
 						logger.error("Problem using rotation motor:{}", ex);
 					} finally {
 						monitor.done();
 					}
 				}
 			});
 		} catch (InvocationTargetException e) {
			logger.error("Problem occured rotating the sample stage to a position", e);
 		} catch (InterruptedException e) {
			logger.error("Process to rotate the sample stage to a position interrupted", e);
 		}
 	}
 
 	private void moveRotationMotorBy(final double deg) {
 		try {
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, false, new IRunnableWithProgress() {
 
 				@Override
 				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 					try {
 						tomoAlignmentViewController.moveRotationMotorBy(monitor, deg);
 						coarseRotation.showButtonDeSelected();
 					} catch (Exception ex) {
 						logger.error("Problem using rotation motor:{}", ex);
 					} finally {
 						monitor.done();
 					}
 				}
 			});
 		} catch (InvocationTargetException e) {
			logger.error("Problem occured in rotating the sample stage by a given amount", e);
 		} catch (InterruptedException e) {
			logger.error("Process to rotate the sample stage by a given amount  interrupted", e);
 		}
 	}
 
 	private IRotationMotorListener rotationMotorListener = new IRotationMotorListener() {
 
 		@Override
 		public void updateRotationMotorBusy(boolean busy) {
 			coarseRotation.setMotorBusy(busy);
 		}
 
 		@Override
 		public void setRotationDeg(final Double rotationMotorDeg) {
 			getShell().getDisplay().syncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					coarseRotation.moveSliderTo(rotationMotorDeg);
 				}
 			});
 		}
 	};
 
 	private SliderSelectionListener sliderSelectionListener = new SliderSelectionListener() {
 		@Override
 		public void sliderMoved(final RotationSliderComposite sliderComposite, final double initialDegree,
 				int totalWidth) {
 			try {
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, false, new IRunnableWithProgress() {
 
 					@Override
 					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 						try {
 							logger.debug("Slider Moved to {}", sliderComposite.getCurrentSliderDegree());
 							logger.debug("initial degree {}", initialDegree);
 							double currentSliderDegree = coarseRotation.getCurrentSliderDegree();
 							tomoAlignmentViewController.moveRotationMotorBy(monitor, currentSliderDegree
 									- initialDegree);
 							coarseRotation.showButtonDeSelected();
 						} catch (Exception ex) {
 							logger.error("Problem using rotation motor:{}", ex);
 						} finally {
 							monitor.done();
 						}
 
 					}
 				});
 			} catch (InvocationTargetException e) {
				logger.error("Problem occured in rotating the sample stage by a given amount", e);
 			} catch (InterruptedException e) {
				logger.error("Process to rotate the sample stage by a given amount  interrupted", e);
 			}
 
 		}
 
 		@Override
 		public void sliderMovedTo(final double deg) {
 			moveRotationMotorTo(deg);
 		}
 	};
 
 	public ImageLocationRelTheta getViewerButtonSelected() {
 		return viewerButtonSelected;
 	}
 
 }
