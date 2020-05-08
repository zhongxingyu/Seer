 package nl.nikhef.jgridstart.gui.util;
 
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.lang.reflect.Method;
 import java.util.logging.Logger;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileFilter;
 
 
 /** Filechooser for PEM and PKCS#12 files */
 public class CertificateFileChooser extends JFileChooser {
     static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui.util");
     
     public CertificateFileChooser(boolean open) {
 	super();
 	
 	addChoosableFileFilter(
 		new FileFilterSuffix("PKCS#12 Certificate Bundle", ".p12,.pfx"));
 	addChoosableFileFilter(
 		new FileFilterSuffix("PEM Certificate Bundle", ".pem"));
 	if (open)
 	    addChoosableFileFilter(
 		    new FileFilterSuffix("Certificate Bundle", ".p12,.pfx,.pem"));
 	
 	setAcceptAllFileFilterUsed(false);
 	
 	// Make sure a filefilter is selected; needed on Mac OS X only
 	setFileFilter(getChoosableFileFilters()[0]);
 	
 	setFileSelectionMode(FILES_ONLY);
 	setDialogType(open ? OPEN_DIALOG : SAVE_DIALOG);
     }
     
     /** {@inheritDoc}
      * <p>
      * This method adds the selected extension for opening a file
      * if the name supplied didn't have an extension.
      */
     @Override
     public File getSelectedFile() {
 	File f = super.getSelectedFile();
 	if (f!=null && getDialogType() == SAVE_DIALOG) {
 	    if (!getFileFilter().accept(f)) {
 		// get file and selected filefilter
 		FileFilter ff = getFileFilter();
 		String path = f.getPath();
 		String[] exts = ((FileFilterSuffix)ff).getExtensions();
 		// check if the selected file has an extension of this filter
 		boolean hasext = false;
 		for (int i=0; i<exts.length; i++)
 		    if (path.endsWith(exts[i])) hasext = true;
 		// if not, add first extension
 		if (!hasext) path += exts[0];
 		// generate filename
 		f = new File(path);
 	    }
 	}
 	return f;
     }
 
     /** Create {@link JFileChooser} component in dialog
      * <p>
      * TODO explain
      * 
      * @param dlg dialog where component is part of
      * @param chooser filechooser to use
      * @param action default action to execute
      */
     public static JPanel customFileChooser(final JDialog dlg, final JFileChooser chooser, final Action action) {
 	Insets insets = null;
 	if (chooser.getBorder() instanceof EmptyBorder)
 	    insets = ((EmptyBorder)chooser.getBorder()).getBorderInsets();
 	// disable buttons because we'll roll our own
 	chooser.setControlButtonsAreShown(false);
 	chooser.setApproveButtonText((String)action.getValue(Action.NAME));
 	// dialog panel with chooser on top
 	JPanel panel = new JPanel();
 	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 	panel.add(chooser);
 	// then container panel for our extra elements
 	JPanel contentpane = new JPanel();
 	contentpane.setLayout(new BoxLayout(contentpane, BoxLayout.Y_AXIS));
 	if (insets!=null) contentpane.setBorder(BorderFactory.createEmptyBorder(0, insets.left, 0, insets.right));
 	panel.add(contentpane);
 	// and the bottom buttons
 	JPanel btns = new JPanel();
 	if (insets!=null) btns.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
 	btns.add(Box.createHorizontalGlue());
 	btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
 	final JButton activate = new JButton(new AbstractAction((String)action.getValue(Action.NAME)) {
 	    public void actionPerformed(ActionEvent e) {
 		chooser.approveSelection();
 	    }
 	});
 	btns.add(activate);
 	final JButton cancel = new JButton(new AbstractAction("Cancel") {
 	    public void actionPerformed(ActionEvent e) {
 		chooser.cancelSelection();
 	    }
 	});
 	btns.add(cancel);
 	panel.add(btns);
 
 	dlg.getContentPane().add(panel);
 	dlg.getRootPane().setDefaultButton(activate);
 	dlg.setModal(true);
 	
 	// hook filechooser actions to our own actions
 	chooser.addActionListener(new ActionListener() {
 	    public void actionPerformed(ActionEvent e) {
 		if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
 		    // workaround for JFileChooser bug, see
 		    //   http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4528663
 		    // On Linux&Windows chooser is a BasicFileChooserUI, while on Mac the
 		    // chooser is an AquaFileChooserUI. Both have the getFileName() method.
 		    try {
 			Method getFileName = chooser.getUI().getClass().getDeclaredMethod("getFileName", new Class[]{});
 			String fn = (String)getFileName.invoke(chooser.getUI(), new Object[]{});
 			if (!new File(fn).isAbsolute())
 			    chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), fn));
 			else
 			    chooser.setSelectedFile(new File(fn));
 		    } catch (Exception e1) {
 			logger.warning("Could not activate workaround for Sun bug #4528663 (Custom JFileChooser): "+e1);
 		    }
 		    action.actionPerformed(e);
 		}
 		dlg.removeAll();
 		dlg.dispose();
 	    }
 	});
 	
 	return contentpane;
     }
 }
