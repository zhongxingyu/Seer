 /**
  * 
  */
 package org.ilaborie.osgi.notification.swt.dialog;
 
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseTrackAdapter;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * The Class ShellCloser.
  *
  * @author igor
  */
 public class ShellCloser implements Runnable {
 
 	/**
 	 * The listener interface for receiving shellOver events.
 	 * The class that is interested in processing a shellOver
 	 * event implements this interface, and the object created
 	 * with that class is registered with a component using the
 	 * component's <code>addShellOverListener<code> method. When
 	 * the shellOver event occurs, that object's appropriate
 	 * method is invoked.
 	 *
 	 * @author igor
 	 */
 	private final class ShellOverListener extends MouseTrackAdapter {
 
 		/** The shell. */
 		private final ShellCloser shellCloser;
 
 		/** The mouse over. */
 		private boolean mouseOver = false;
 
 		/** The need to close. */
 		private boolean needToClose = false;
 
 		/**
 		 * Instantiates a new shell over listener.
 		 *
 		 * @param shellCloser the shell closer
 		 */
 		public ShellOverListener(ShellCloser shellCloser) {
 			super();
 			this.shellCloser = shellCloser;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.swt.events.MouseTrackAdapter#mouseEnter(org.eclipse.swt.events.MouseEvent)
 		 */
 		@Override
 		public void mouseEnter(MouseEvent e) {
 			this.mouseOver = true;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.swt.events.MouseTrackAdapter#mouseExit(org.eclipse.swt.events.MouseEvent)
 		 */
 		@Override
 		public void mouseExit(MouseEvent e) {
			this.mouseOver = false;
 			if (this.needToClose) {
 				this.shellCloser.closeShell();
 			}
 		}
 
 		/**
 		 * Checks if is mouse over.
 		 *
 		 * @return the mouseOver
 		 */
 		public boolean isMouseOver() {
 			return this.mouseOver;
 		}
 
 		/**
 		 * Sets the need to close.
 		 *
 		 * @param needToClose the needToClose to set
 		 */
 		public void setNeedToClose(boolean needToClose) {
 			this.needToClose = needToClose;
 		}
 	}
 
 	// Attributes
 	/** The shell. */
 	private final Shell shell;
 
 	/** The shell over listener. */
 	private final ShellOverListener shellOverListener;
 
 	// Constructors
 	/**
 	 * Instantiates a new shell closer.
 	 *
 	 * @param shell the shell
 	 */
 	public ShellCloser(Shell shell) {
 		super();
 		if (shell == null) {
 			throw new IllegalArgumentException("Shell shouldn't being empty !"); //$NON-NLS-1$
 		}
 		this.shell = shell;
 		this.shellOverListener = new ShellOverListener(this);
 		this.shell.addMouseTrackListener(this.shellOverListener);
 	}
 
 	// Methods
 	/* (non-Javadoc)
 	 * @see java.lang.Runnable#run()
 	 */
 	@Override
 	public void run() {
 		if (!this.shell.isDisposed()) {
 			if (!this.shellOverListener.isMouseOver()) {
 				this.closeShell();
 			} else {
 				this.shellOverListener.setNeedToClose(true);
 			}
 		}
 	}
 
 	/**
 	 * Close shell.
 	 */
 	protected void closeShell() {
 		if (!this.shell.isDisposed()) {
 			this.shell.close();
 		}
 	}
 
 }
