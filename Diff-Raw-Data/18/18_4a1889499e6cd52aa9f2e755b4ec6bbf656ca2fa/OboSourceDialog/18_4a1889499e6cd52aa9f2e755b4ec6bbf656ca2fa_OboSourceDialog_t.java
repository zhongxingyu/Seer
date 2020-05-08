 /**
  * 
  */
 package org.geworkbench.builtin.projects;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.io.File;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
 
 import org.geworkbench.builtin.projects.OboSourcePreference.Source;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 
 /**
  * @author zji
  * @version $Id$
  */
 public class OboSourceDialog extends JDialog {
 
 	private static final long serialVersionUID = 3418980725205404590L;
 
 	private JRadioButton remote = new JRadioButton("Remote");
 	private JRadioButton local = new JRadioButton("Local");;
 	private JTextField sourceLocation = new JTextField(40);
 	private JButton ok = new JButton("OK");
 	private JButton cancel = new JButton("Cancel");
 	
 	private static OboSourceDialog INSTANCE = new OboSourceDialog();
 	
 	static OboSourceDialog getInstance () {
 		return INSTANCE;
 	}
 	
 	private OboSourcePreference oboSourcePreference = OboSourcePreference.getInstance();
 	
 	void refresh() {
 		if(oboSourcePreference.getSourceType()==Source.REMOTE) {
 			remote.setSelected(true);
 			sourceLocation.setEnabled(false);
 			chooseFileButton.setEnabled(false);
 			sourceLocation.setText(OboSourcePreference.DEFAULT_REMOTE_LOCATION);
 		} else {
 			local.setSelected(true);
 			sourceLocation.setEnabled(true);
 			chooseFileButton.setEnabled(true);
 			String location = oboSourcePreference.getSourceLocation();
 			if(location==null || location.trim().length()==0 || location.startsWith("http:"))
 				location = OboSourcePreference.DEFAULT_LOCAL_LOCATION;
 			sourceLocation.setText(location);
 		}
 	}
 	
 	private JButton chooseFileButton = new JButton("...");
 	
 	private OboSourceDialog() {
 		super(GeawConfigObject.getGuiWindow(), "Choose OBO Source", true);
 		setLayout(new BorderLayout());
 		JPanel topPanel = new JPanel();
 		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
 		JPanel radioButtonPanel = new JPanel();
 		JPanel locationFieldPanel = new JPanel();
 		topPanel.add(radioButtonPanel);
 		topPanel.add(locationFieldPanel);
 		topPanel.add(new JLabel("(The change will not take effect until geWorkbench is restarted.)"));
 		radioButtonPanel.add(remote);
 		radioButtonPanel.add(local);
 		locationFieldPanel.add(sourceLocation);
 		locationFieldPanel.add(chooseFileButton);
 		JPanel bottomPanel = new JPanel();
 		bottomPanel.add(ok);
 		bottomPanel.add(cancel);
 		add(topPanel, BorderLayout.CENTER);
 		add(bottomPanel, BorderLayout.SOUTH);
 		pack();
 		this.setLocationRelativeTo(null);
 		
 		ButtonGroup group = new ButtonGroup();
 		group.add(remote);
 		group.add(local);
 		remote.setSelected(true);
 		sourceLocation.setText(OboSourcePreference.DEFAULT_REMOTE_LOCATION);
 		sourceLocation.setEnabled(false);
 		chooseFileButton.setEnabled(false);
 		
 		chooseFileButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String location = oboSourcePreference.getSourceLocation();
 				if(location==null || location.trim().length()==0)
 					location = OboSourcePreference.DEFAULT_LOCAL_LOCATION;
                 JFileChooser chooser = new JFileChooser(location);
                chooser.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File f) {
						if(f.getName().toLowerCase().endsWith(".obo")) return true;
						else return false;
					}

					@Override
					public String getDescription() {
						return "obo file (*.obo)";
					}
                	
                });
                 int returnVal = chooser.showOpenDialog(OboSourceDialog.this);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     sourceLocation.setText(chooser.getSelectedFile().getPath());
                 }
 			}});
 		
 		ActionListener listener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(remote.isSelected()) {
 					sourceLocation.setText(OboSourcePreference.DEFAULT_REMOTE_LOCATION);
 					sourceLocation.setEnabled(false);
 					chooseFileButton.setEnabled(false);
 				} else  {
 					String location = oboSourcePreference.getSourceLocation();
 					if(location==null || location.trim().length()==0 || location.startsWith("http:"))
 						location = OboSourcePreference.DEFAULT_LOCAL_LOCATION;
 					sourceLocation.setText(location);
 					sourceLocation.setEnabled(true);
 					chooseFileButton.setEnabled(true);
 				}
 			}
 			
 		};
 		remote.addActionListener(listener);
 		local.addActionListener(listener);
 		ok.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(remote.isSelected())
 					oboSourcePreference.setSourceType(Source.REMOTE);
 				else
 					oboSourcePreference.setSourceType(Source.LOCAL);
 
 				oboSourcePreference.setLocation( sourceLocation.getText() );
 
 				oboSourcePreference.save();
 				dispose();
 			}
 			
 		});
 		cancel.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 			
 		});
 	}
 }
