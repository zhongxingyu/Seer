 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.facades.internal;
 
 import java.util.concurrent.CountDownLatch;
 
 import org.pushingpixels.trident.Timeline;
 import org.pushingpixels.trident.Timeline.TimelineState;
 import org.pushingpixels.trident.callback.TimelineCallbackAdapter;
 import org.pushingpixels.trident.ease.Sine;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.ui.swt.InfoFlyout;
 import org.eclipse.riena.ui.swt.layout.DpiGridLayoutFactory;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.utils.ImageStore;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Platform specific {@link InfoFlyout} for RCP.
  * 
  * @see InfoFlyout
  */
 public class InfoFlyoutRCP extends InfoFlyout {
 
 	private static final Integer PAUSE_ANIMATION_TIME = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.INFO_FLYOUT_PAUSE_ANIMATION_TIME);
 	private static final Integer WAIT_ANIMATION_TIME = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.INFO_FLYOUT_WAIT_ANIMATION_TIME);
 	private static final Integer SHOW_HIDE_ANIMATION_TIME = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.INFO_FLYOUT_SHOW_AND_HIDE_ANIMATION_TIME);
 	private static final int WIDTH = getLnfSettingAndConvertX(LnfKeyConstants.INFO_FLYOUT_WIDTH);
 	private static final int HEIGHT = getLnfSettingAndConvertY(LnfKeyConstants.INFO_FLYOUT_HEIGHT);
 	private static final int SHELL_RIGHT_INDENT = getLnfSettingAndConvertX(LnfKeyConstants.INFO_FLYOUT_RIGHT_INDENT);
 	private static final int ICON_LEFT_MARGIN = getLnfSettingAndConvertX(LnfKeyConstants.INFO_FLYOUT_LEFT_MARGIN);
 	private static final int TEXT_LEFT_MARGIN = getLnfSettingAndConvertX(LnfKeyConstants.INFO_FLYOUT_ICON_TEXT_GAP);
 	private static final int INFO_FLYOUT_RIGHT_MARGIN = getLnfSettingAndConvertX(LnfKeyConstants.INFO_FLYOUT_RIGHT_MARGIN);
 	// private static final int RIGHT_LABEL_WIDTH = WIDTH - INFO_FLYOUT_RIGHT_MARGIN - TEXT_LEFT_MARGIN - ICON_LEFT_MARGIN - SwtUtilities.convertXToDpi(30);
 	private static final int RIGHT_LABEL_WIDTH = getLnfSetting(LnfKeyConstants.INFO_FLYOUT_WIDTH) - getLnfSetting(LnfKeyConstants.INFO_FLYOUT_RIGHT_MARGIN)
 			- getLnfSetting(LnfKeyConstants.INFO_FLYOUT_LEFT_MARGIN) - getLnfSetting(LnfKeyConstants.INFO_FLYOUT_LEFT_MARGIN) - 30;
 
 	private String message;
 	private String icon;
 
 	private Shell shell;
 	private final Composite parent;
 
 	private Timeline tShow;
 	private Timeline tWait;
 	private Timeline tHide;
 	private Timeline tWaitAtEnd;
 
 	private Label rightLabel;
 	private Label leftLabel;
 	private Rectangle topLevelShellBounds;
 	private int xPosition;
 	private int startY;
 	private int endY;
 
 	private CountDownLatch latch;
 	private final Color bgColor;
 
 	public InfoFlyoutRCP(final Composite parent) {
 		this.parent = parent;
 		message = ""; //$NON-NLS-1$
 		icon = null;
 		bgColor = LnfManager.getLnf().getColor(LnfKeyConstants.INFO_FLYOUT_BACKGROUND_COLOR);
 		latch = new CountDownLatch(0);
 		initializeLayout();
 	}
 
 	@Override
 	public void openFlyout() {
 		if (!isAnimationGoingOn() && !shell.isDisposed()) {
 			latch = new CountDownLatch(1);
 			updateIconAndMessage();
 			updateLocation();
 			updateLayoutData();
 			initializeTimelines();
 
 			shell.setVisible(true);
 			tShow.play();
 		}
 	}
 
 	@Override
 	public void setMessage(final String message) {
 		this.message = message;
 	}
 
 	@Override
 	public void setIcon(final String icon) {
 		this.icon = icon;
 	}
 
 	@Override
 	public void waitForClosing() {
 		try {
 			latch.await();
 		} catch (final InterruptedException e) {
 			Thread.currentThread().interrupt();
 		}
 	}
 
 	// helping methods
 	//////////////////
 
 	private static int getLnfSetting(final String lnfKey) {
 		return LnfManager.getLnf().getIntegerSetting(lnfKey);
 	}
 
 	private static int getLnfSettingAndConvertX(final String lnfKey) {
 		final int setting = getLnfSetting(lnfKey);
 		return SwtUtilities.convertXToDpi(setting);
 	}
 
 	private static int getLnfSettingAndConvertY(final String lnfKey) {
 		final int setting = getLnfSetting(lnfKey);
 		return SwtUtilities.convertYToDpi(setting);
 	}
 
 	private void initializeLayout() {
 		Assert.isTrue(shell == null); // only call once
 
 		final Shell parentShell = parent.getShell();
 		parentShell.addControlListener(new CloseOnParentMove());
 
 		shell = new Shell(parentShell, SWT.MODELESS | SWT.NO_TRIM);
 		shell.addPaintListener(new BorderPainter());
 		DpiGridLayoutFactory.fillDefaults().numColumns(2).applyTo(shell);
 
 		shell.setBackground(bgColor);
 
 		leftLabel = UIControlsFactory.createLabel(shell, ""); //$NON-NLS-1$
		leftLabel.setImage(ImageStore.getInstance().getImage("arrowRight")); //$NON-NLS-1$
 		leftLabel.setBackground(bgColor);
 
 		rightLabel = UIControlsFactory.createLabel(shell, message, SWT.WRAP);
 		rightLabel.setBackground(bgColor);
 		rightLabel.setFont(LnfManager.getLnf().getFont(LnfKeyConstants.INFO_FLYOUT_FONT));
 
 		updateLocation();
 		updateLayoutData();
 	}
 
 	private void updateIconAndMessage() {
 		rightLabel.setText(message);
 		leftLabel.setImage(ImageStore.getInstance().getImage(icon));
 		shell.layout(true);
 	}
 
 	private void initializeTimelines() {
 		tShow = new Timeline(shell);
 		tWait = new Timeline(shell);
 		tHide = new Timeline(shell);
 		tWaitAtEnd = new Timeline(shell);
 
 		tShow.addPropertyToInterpolate(Timeline.<Point> property("location").fromCurrent().to(new Point(xPosition, endY))); //$NON-NLS-1$
 		tShow.addPropertyToInterpolate(Timeline.<Point> property("size").fromCurrent() //$NON-NLS-1$
 				.to(new Point(WIDTH, HEIGHT)));
 
 		tHide.addPropertyToInterpolate(Timeline.<Point> property("location").fromCurrent().to(new Point(xPosition, startY))); //$NON-NLS-1$
 		tHide.addPropertyToInterpolate(Timeline.<Point> property("size").fromCurrent() //$NON-NLS-1$
 				.to(new Point(WIDTH, 0)));
 
 		tShow.addCallback(new TimelineCallbackAdapter() {
 			@Override
 			public void onTimelineStateChanged(final TimelineState oldState, final TimelineState newState, final float durationFraction,
 					final float timelinePosition) {
 				if (newState == TimelineState.DONE) {
 					tWait.play();
 				}
 			}
 		});
 
 		tWait.addCallback(new TimelineCallbackAdapter() {
 			@Override
 			public void onTimelineStateChanged(final TimelineState oldState, final TimelineState newState, final float durationFraction,
 					final float timelinePosition) {
 				if (newState == TimelineState.DONE) {
 					tHide.play();
 				}
 			}
 		});
 
 		tHide.addCallback(new TimelineCallbackAdapter() {
 			@Override
 			public void onTimelineStateChanged(final TimelineState oldState, final TimelineState newState, final float durationFraction,
 					final float timelinePosition) {
 				if (newState == TimelineState.DONE) {
 					tWaitAtEnd.play();
 
 				}
 			}
 		});
 
 		tWaitAtEnd.addCallback(new TimelineCallbackAdapter() {
 			@Override
 			public void onTimelineStateChanged(final TimelineState oldState, final TimelineState newState, final float durationFraction,
 					final float timelinePosition) {
 				if (newState == TimelineState.DONE) {
 					synchronized (this) {
 						latch.countDown();
 					}
 				}
 			}
 		});
 
 		tShow.setDuration(SHOW_HIDE_ANIMATION_TIME);
 		tWait.setDuration(WAIT_ANIMATION_TIME);
 		tHide.setDuration(SHOW_HIDE_ANIMATION_TIME);
 		tWaitAtEnd.setDuration(PAUSE_ANIMATION_TIME);
 
 		tHide.setEase(new Sine());
 		tShow.setEase(new Sine());
 
 	}
 
 	private void updateLayoutData() {
 		final int topIndent = (HEIGHT - rightLabel.getBounds().height) / 2;
 
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(ICON_LEFT_MARGIN, topIndent).applyTo(leftLabel);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(TEXT_LEFT_MARGIN, topIndent).hint(RIGHT_LABEL_WIDTH, SWT.DEFAULT).applyTo(rightLabel);
 	}
 
 	private void updateLocation() {
 		final Shell topLevelShell = parent.getShell();
 		topLevelShellBounds = topLevelShell.getBounds();
 		xPosition = topLevelShellBounds.x + topLevelShellBounds.width - WIDTH - SHELL_RIGHT_INDENT;
 		startY = parent.getDisplay().map(parent.getParent(), null, parent.getBounds()).y + positionCorrectionY;
 		endY = startY - HEIGHT;
 
 		shell.setSize(WIDTH, 0);
 		shell.setLocation(xPosition, startY);
 	}
 
 	private boolean isAnimationGoingOn() {
 		if (tShow == null || tWait == null || tHide == null) {
 			return false;
 		}
 		boolean isPlaying = tShow.getState() == TimelineState.PLAYING_FORWARD;
 		isPlaying |= tWait.getState() == TimelineState.PLAYING_FORWARD;
 		isPlaying |= tHide.getState() == TimelineState.PLAYING_FORWARD;
 		return isPlaying;
 	}
 
 	// helping classes
 	//////////////////
 
 	private static final class BorderPainter implements PaintListener {
 
 		public void paintControl(final PaintEvent e) {
 			final Control control = (Control) e.widget;
 			final GC gc = e.gc;
 
 			final Color oldFg = gc.getForeground();
 			final int oldWidth = gc.getLineWidth();
 			gc.setForeground(LnfManager.getLnf().getColor(LnfKeyConstants.INFO_FLYOUT_BORDER_COLOR));
 			gc.setLineWidth(1);
 
 			final Rectangle bounds = control.getBounds();
 			gc.drawLine(0, 0, bounds.width - 1, 0);
 			gc.drawLine(0, 0, 0, bounds.height - 1);
 			gc.drawLine(bounds.width - 1, 0, bounds.width - 1, bounds.height);
 
 			gc.setForeground(oldFg);
 			gc.setLineWidth(oldWidth);
 		}
 
 	}
 
 	/**
 	 * Closes the InfoFlyout, if the shell is moved or resized.
 	 */
 	private final class CloseOnParentMove implements ControlListener {
 
 		public void controlMoved(final ControlEvent e) {
 			close();
 		}
 
 		public void controlResized(final ControlEvent e) {
 			close();
 		}
 
 		// helping methods
 		//////////////////
 
 		private void close() {
 			if (!shell.isDisposed() && shell.isVisible()) {
 				shell.setVisible(false);
 				tShow.abort();
 				tWait.abort();
 				tHide.abort();
 				latch.countDown();
 			}
 		}
 	}
 
 }
