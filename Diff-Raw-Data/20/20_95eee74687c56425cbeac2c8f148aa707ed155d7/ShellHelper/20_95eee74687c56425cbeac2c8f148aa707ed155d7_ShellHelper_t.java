 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.swt.utils;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.jface.util.Geometry;
 import org.eclipse.jface.util.Util;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Monitor;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.core.Log4r;
 
 /**
  * This class helps to maximize or restore a shell.
  */
 public class ShellHelper {
 
 	private final static Logger LOGGER = Log4r.getLogger(ShellHelper.class);
 
 	private Rectangle restoreBounds;
 
 	/**
 	 * Maximizes or restores the shell of the active workbench.
 	 */
 	public void maximizeRestore() {
		maximizeRestore(RcpUtilities.getWorkbenchShell());
	}

	/**
	 * Maximizes or restores the given shell.
	 */
	public void maximizeRestore(final Shell shell) {
 		if (shell == null) {
 			LOGGER.log(LogService.LOG_WARNING, "No shell of the application found! Maximize/restore of shell canceled."); //$NON-NLS-1$
 			return;
 		}
		if (isTitleless(shell)) {
			if (isMaximzed(shell)) {
 				if (restoreBounds != null) {
 					shell.setBounds(restoreBounds);
 					shell.redraw(restoreBounds.x, restoreBounds.y, restoreBounds.width, restoreBounds.height, true);
 				} else {
 					LOGGER.log(LogService.LOG_WARNING, "No restore bounds exists! Restore of shell canceled."); //$NON-NLS-1$
 				}
 			} else {
 				restoreBounds = shell.getBounds();
 				final Rectangle clientBounds = calcMaxBounds(shell);
 				shell.setBounds(clientBounds);
 				shell.redraw(clientBounds.x, clientBounds.y, clientBounds.width, clientBounds.height, true);
 			}
 		} else {
 			shell.setMaximized(shell.getMaximized());
 		}
 	}
 
 	/**
 	 * Calculates the maximal bounds for the given shell.
 	 * <p>
 	 * If the task bar of the operation system (Windwos) is hidden, the maximal
 	 * bounds for the shell are reduced by one pixel a every side.
 	 * 
 	 * @param shell
 	 *            the shell which maximal bounds should be returned
 	 * @return maximal bounds of the shell
 	 */
 	public static Rectangle calcMaxBounds(final Shell shell) {
 		final Rectangle clientBounds = shell.getMonitor().getClientArea();
 		final Rectangle newBounds = new Rectangle(clientBounds.x, clientBounds.y, clientBounds.width,
 				clientBounds.height);
 		if (isTaskbarHidden()) {
 			newBounds.x += 1;
 			newBounds.y += 1;
 			newBounds.width -= 2;
 			newBounds.height -= 2;
 		}
 		return newBounds;
 	}
 
 	/**
 	 * Returns whether the task bar of the operation system (Windows) is hidden
 	 * or not.
 	 * <p>
 	 * Because there is no API to check these, the bounds of the client area and
 	 * the monitor will be compared. If they are identical, it will be expected
 	 * that the task bar is hidden.
 	 * 
 	 * @return {@code true} if task bar is hidden; otherwise {@code false}
 	 */
 	private static boolean isTaskbarHidden() {
 		if (!Util.isWindows()) {
 			return false;
 		}
 		final Shell shell = RcpUtilities.getWorkbenchShell();
 		if (shell == null) {
 			LOGGER.log(LogService.LOG_WARNING, "No shell of the application found!"); //$NON-NLS-1$
 			return false;
 		}
 		final Rectangle clientBounds = shell.getMonitor().getClientArea();
 		final Rectangle monitorBounds = shell.getMonitor().getBounds();
 		return clientBounds.equals(monitorBounds);
 	}
 
 	/**
 	 * Returns whether the shell is currently maximized or not.
 	 * 
 	 * @return {@code true} if the shell is maximized; {@code false} if the
 	 *         shell isn't maximized.
 	 */
 	public static boolean isShellMaximzed() {
 		final Shell shell = RcpUtilities.getWorkbenchShell();
 		if (shell == null) {
 			LOGGER.log(LogService.LOG_WARNING, "No shell of the application found!"); //$NON-NLS-1$
 			return false;
 		}
 		return isMaximzed(shell);
 	}
 
 	/**
 	 * Returns whether the given shell is currently maximized or not.
 	 * 
 	 * @param shell
 	 *            shell to check
 	 * @return {@code true} if the shell is maximized; {@code false} if the
 	 *         shell isn't maximized.
 	 */
 	private static boolean isMaximzed(final Shell shell) {
 		if ((shell == null) || shell.isDisposed()) {
 			LOGGER.log(LogService.LOG_WARNING, "shell equals null or is disposed!"); //$NON-NLS-1$
 			return false;
 		}
 		if (isShellTitleless()) {
 			Rectangle clientBounds = shell.getMonitor().getClientArea();
 			if (clientBounds.equals(shell.getBounds())) {
 				return true;
 			} else {
 				clientBounds = calcMaxBounds(shell);
 				return clientBounds.equals(shell.getBounds());
 			}
 		} else {
 			return shell.getMaximized();
 		}
 	}
 
 	/**
 	 * Returns whether the given shell has the default shell style with (title
 	 * bar, border, system menu etc.) or not.
 	 * 
 	 * @param shell
 	 *            shell to check
 	 * @return {@code true} not default shell style (no title bar etc.);
 	 *         {@code false] default shell style
 	 */
 	private static boolean isTitleless(final Shell shell) {
 		if ((shell == null) || shell.isDisposed()) {
 			LOGGER.log(LogService.LOG_WARNING, "shell equals null or is disposed!"); //$NON-NLS-1$
 			return false;
 		}
 		final int style = shell.getStyle();
 		return (style & SWT.NO_TRIM) == SWT.NO_TRIM;
 	}
 
 	/**
 	 * Returns whether the shell has the default shell style with (title bar,
 	 * border, system menu etc.) or not.
 	 * 
 	 * @return {@code true} not default shell style (no title bar etc.);
 	 *         {@code false] default shell style
 	 */
 	private static boolean isShellTitleless() {
 		final Shell shell = RcpUtilities.getWorkbenchShell();
 		if (shell == null) {
 			LOGGER.log(LogService.LOG_WARNING, "No shell of the application found!"); //$NON-NLS-1$
 			return false;
 		}
 		return isTitleless(shell);
 	}
 
 	/**
 	 * centers the given shell on the current screen
 	 * 
 	 * @param shell
 	 */
 	public static void center(final Shell shell) {
 		if (shell != null) {
 			final Rectangle surroundingBounds = getSurroundingBounds(shell);
 			final Rectangle shellBounds = shell.getBounds();
 			final int leftMargin = surroundingBounds.x + (surroundingBounds.width - shellBounds.width) / 2;
 			final int topMargin = surroundingBounds.y + (surroundingBounds.height - shellBounds.height) / 2;
 			shell.setLocation(leftMargin, topMargin);
 		}
 	}
 
 	private static Rectangle getSurroundingBounds(final Shell shell) {
 		return shell.getParent() != null ? shell.getParent().getBounds() : getClosestMonitor(shell.getDisplay(),
 				shell.getLocation()).getBounds();
 	}
 
 	public static Monitor getClosestMonitor(final Shell shell) {
 		return getClosestMonitor(shell.getDisplay(), shell.getLocation());
 	}
 
 	/**
 	 * This is a slightly modified version of @see Window.getClosestMonitor()
 	 */
 	private static Monitor getClosestMonitor(final Display toSearch, final Point toFind) {
 		int closest = Integer.MAX_VALUE;
 
 		Monitor result = toSearch.getPrimaryMonitor();
 
 		for (final Monitor current : toSearch.getMonitors()) {
 			final Rectangle clientArea = current.getBounds();
 
 			if (clientArea.contains(toFind)) {
 				return current;
 			}
 
 			final int distance = Geometry.distanceSquared(Geometry.centerPoint(clientArea), toFind);
 			if (distance < closest) {
 				closest = distance;
 				result = current;
 			}
 		}
 		return result;
 	}
 
 }
