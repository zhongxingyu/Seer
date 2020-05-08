 /*
  * Copyright (c) 2013 David Hartveld
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package com.hartveld.commons.test.swing;
 
 import static com.google.common.base.Preconditions.checkState;
 import static javax.swing.SwingUtilities.isEventDispatchThread;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.util.concurrent.locks.ReentrantLock;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import org.junit.After;
 import org.junit.Before;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class AbstractSwingFrameTest {
 
 	private static final Logger LOG = LoggerFactory.getLogger(AbstractSwingFrameTest.class);
 
 	private ReentrantLock lock;
 
 	private JFrame frame;
 
 	@Before
 	public void setUp() {
 		createAndAcquireLock();
 		createAndShowFrame();
 		lookupComponents();
 	}
 
 	@After
 	public void tearDown() {
 		if (frame.isVisible()) {
 			closeFrame();
 		}
 
 		waitForFrameToClose();
 	}
 
 	/**
 	 * Create the main {@link JFrame} used during the test.
 	 * <p/>
 	 * This method is called on the EDT.
 	 *
 	 * @return A newly created {@link JFrame} for use in the test.
 	 */
 	protected abstract JFrame createFrame();
 
 	/**
 	 * Lookup all components that are needed by the test.
 	 * <p/>
 	 * Gives the test case a chance to lookup all necessary components in the
 	 * main window that are later needed in the test case itself.
 	 * <p/>
 	 * This method is called on the EDT.
 	 */
 	protected abstract void lookupComponents();
 
 	/**
 	 * Lookup a component of given type in the content pane of the main frame.
 	 * <p/>
 	 * The first component encountered in the content pane of the main frame is
 	 * returned.
 	 * <p/>
 	 * This method uses {@link JFrame#getContentPane()} and
 	 * {@link Container#getComponents()} to recurse over the component tree.
 	 *
 	 * @param <T>   The type of the component.
 	 * @param clazz The type of the component as {@link Class} instance.
 	 *
 	 * @return If found, the component is returned.
 	 *
 	 * @throws NoSuchComponentException If no component can be found of the
 	 *                                  supplied type, an exception is thrown.
 	 */
 	protected <T extends Component> T lookup(final Class<T> clazz) throws NoSuchComponentException {
 		LOG.trace("Looking up component of type: {}", clazz.getName());
 
 		final T component = lookup(this.frame.getContentPane(), clazz);
 
 		if (component == null) {
 			throw new NoSuchComponentException(this.frame, clazz);
 		} else {
 			LOG.trace("Found component: {}", component.getName());
 			return component;
 		}
 	}
 
 	private static <T> T lookup(final Container container, final Class<T> clazz) {
 		LOG.trace("Looking up component of type {} in container {} ...", clazz.getName(), container.getName());
 
 		for (final Component c : container.getComponents()) {
 			if (clazz.isAssignableFrom(c.getClass())) {
 				return (T)c;
 			} else if (c instanceof Container) {
 				final T nested = lookup((Container) c, clazz);
 				if (nested != null) {
 					return nested;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Close the main application frame, releasing all resources.
 	 * <p/>
 	 * Closing the frame causes the UI lock to be released.
 	 *
	 * @see #waitForFrameToClose()
 	 */
 	protected final void closeFrame() {
 		LOG.trace("Closing frame ...");
 
 		checkIsNotEDT();
 
 		LOG.trace("Delegating to EDT ...");
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				LOG.trace("Cleaning up UI resources ...");
 				frame.setVisible(false);
 				frame.dispose();
 
 				LOG.trace("Firing WindowEvent ...");
 				final WindowEvent event = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
 				frame.dispatchEvent(event);
 			}
 		});
 	}
 
 	/**
 	 * Wait for the UI lock to be released after calling {@link #closeFrame()}.
 	 *
 	 * @see #closeFrame()
 	 */
 	protected final void waitForFrameToClose() {
 		LOG.trace("Waiting for frame to close ...");
 
 		checkIsNotEDT();
 
 		lock.lock();
 		lock.unlock();
 	}
 
 	private void createAndAcquireLock() throws RuntimeException {
 		LOG.trace("Creating and acquiring lock ...");
 
 		checkIsNotEDT();
 
 		this.lock = new ReentrantLock();
 
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					LOG.trace("Attempting to acquire lock ...");
 					if (!lock.tryLock()) {
 						throw new RuntimeException("Failed to acquire UI lock");
 					}
 				}
 			});
 		} catch (InterruptedException | InvocationTargetException ex) {
 			throw new RuntimeException(ex.getMessage(), ex);
 		}
 	}
 
 	private void createAndShowFrame() {
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					frame = createFrame();
 					frame.addWindowListener(new WindowAdapter() {
 						@Override
 						public void windowClosing(WindowEvent e) {
 							LOG.trace("Unlocking ...");
 							lock.unlock();
 						}
 					});
 
 					frame.pack();
 					frame.setVisible(true);
 				}
 			});
 		} catch (InterruptedException | InvocationTargetException ex) {
 			throw new RuntimeException(ex.getMessage(), ex);
 		}
 	}
 
 	private static void checkIsNotEDT() {
 		checkState(!isEventDispatchThread(), "Must not be called from EDT");
 	}
 
 }
