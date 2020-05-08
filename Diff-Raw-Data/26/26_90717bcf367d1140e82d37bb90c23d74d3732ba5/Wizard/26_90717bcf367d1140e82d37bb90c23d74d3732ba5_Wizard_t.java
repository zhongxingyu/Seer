 package be.ac.ua.comp.scarletnebula.wizard;
 
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Stack;
 
 import javax.swing.JDialog;
 
 public class Wizard
 {
 	Stack<WizardPage> visitedPages = new Stack<WizardPage>();
 	DataRecorder recorder;
 	JDialog window;
 	WizardListener listener = null;
 
 	WizardTemplate wizardTemplate = null;
 
 	public Wizard(WizardPage startPage, DataRecorder recorder,
 			WizardTemplate wizardTemplate)
 	{
 		this.recorder = recorder;
 		this.wizardTemplate = wizardTemplate;
 		visitedPages.push(startPage);
 	}
 
 	/**
 	 * Start the Wizard in the window in parameter
 	 * 
 	 * @param window
 	 */
 	public void start(JDialog window)
 	{
 		this.window = window;
 
 		initializeButtons(window);
 		displayPage();
 	}
 
 	/**
 	 * Start the wizard, and do so in a window the Wizard creates itself
 	 * 
 	 * @param width
 	 *            Width of the window
 	 * @param height
 	 *            Height of the window
 	 * @param parent
 	 *            Owner of the window, null if no owner
 	 */
 	public void startModal(String title, int width, int height, Dialog parent)
 	{
 		JDialog dialog = new JDialog(parent, true);
 		configureDialog(title, width, height, dialog, parent);
 	}
 
 	/**
 	 * Start the wizard, and do so in a window the Wizard creates itself
 	 * 
 	 * @param width
 	 *            Width of the window
 	 * @param height
 	 *            Height of the window
 	 * @param parent
 	 *            Owner of the window, null if no owner
 	 */
 	public void startModal(String title, int width, int height, Frame parent)
 	{
 		JDialog dialog = new JDialog(parent, true);
 		configureDialog(title, width, height, dialog, parent);
 	}
 
 	private void configureDialog(String title, int width, int height,
 			JDialog dialog, Component parent)
 	{
 		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		dialog.setPreferredSize(new Dimension(width, height));
 		dialog.setLocation(0, 0);
 		dialog.setLocationRelativeTo(parent);
 		dialog.setTitle(title);
 		dialog.pack();
 
 		start(dialog);
 
 		dialog.setVisible(true);
 	}
 
 	private void displayPage()
 	{
 		renderPage(visitedPages.peek());
 	}
 
 	private void initializeButtons(JDialog window)
 	{
 		wizardTemplate.setupWindow(window);
 
 		wizardTemplate.previousButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				previous();
 			}
 		});
 
 		wizardTemplate.nextButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				next();
 			}
 		});
 
 		wizardTemplate.finishButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				finish();
 			}
 		});
 
 		wizardTemplate.cancelButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				cancel();
 			}
 		});
 	}
 
 	private void renderPage(WizardPage page)
 	{
 		if (page == null)
 			return;
 
 		wizardTemplate.nextButton.setEnabled(page.nextIsEnabled());
 		wizardTemplate.finishButton.setEnabled(page.finishIsEnabled());
 		wizardTemplate.previousButton.setEnabled(visitedPages.size() > 1);
 
 		for (Component c : wizardTemplate.container.getComponents())
 		{
 			wizardTemplate.container.remove(c);
 		}
 
 		for (Component c : page.getComponents())
 			c.setVisible(true);
 
 		wizardTemplate.container.add(page);
 		wizardTemplate.container.revalidate();
 		wizardTemplate.container.repaint();
 	}
 
 	private void next()
 	{
 		WizardPage nextPage = visitedPages.peek().next(recorder);
 
 		// If no next page is given, the wizard shouldn't do anything
 		if (nextPage == null)
 			return;
 
 		visitedPages.push(nextPage);
 		displayPage();
 	}
 
 	private void previous()
 	{
 		visitedPages.pop();
 		displayPage();
 	}
 
 	private void finish()
 	{
 		// Run this for the side effects to recorder
 		visitedPages.peek().next(recorder);
		window.dispose();

 		if (listener != null)
 			listener.onFinish(recorder);
 	}
 
 	private void cancel()
 	{
 		window.dispose();
 		if (listener != null)
 			listener.onCancel(recorder);
 	}
 
 	public void addWizardListener(WizardListener l)
 	{
 		listener = l;
 	}
 }
