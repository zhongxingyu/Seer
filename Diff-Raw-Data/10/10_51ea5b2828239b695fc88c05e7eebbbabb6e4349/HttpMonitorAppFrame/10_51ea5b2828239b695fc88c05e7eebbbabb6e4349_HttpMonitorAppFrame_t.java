 /**
  * 
  */
 package com.yogocodes.httpmonitor.gui.frames;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import com.yogocodes.httpmonitor.gui.form.HttpMonitorAppForm;
 import com.yogocodes.httpmonitor.gui.form.HttpMonitorAppFormFactory;
 
 /**
  * @author joukojo
  *
  */
 public class HttpMonitorAppFrame extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	protected HttpMonitorAppFrame() {
 		super("HttpMonitorApplication");
 		setSize(500, 500);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		
 		HttpMonitorAppForm appForm = HttpMonitorAppFormFactory.getAppFormInstance();
 		
 		JPanel configurationPanel = new JPanel(new GridLayout(1, 2));
 		JPanel configurationButtonPanel = new JPanel(new GridLayout(3,1));
 		configurationButtonPanel.add(appForm.getAddHostButton());
 		configurationButtonPanel.add(appForm.getEditHostButton());
 		configurationButtonPanel.add(appForm.getRemoveHostButton());
 		configurationPanel.add(appForm.getMonitoredHostList());
 		configurationPanel.add(configurationButtonPanel);
 		getContentPane().add(configurationPanel, BorderLayout.NORTH);
 		
		JScrollPane resultPane = new JScrollPane(appForm.getMonitorResultTable()); 
		
 		
 		JPanel executionButtonPanel = new JPanel(new GridLayout(1, 2));
 		executionButtonPanel.add(appForm.getStartMonitorButton());
 		executionButtonPanel.add(appForm.getStopMonitorButton());
 		getContentPane().add(resultPane, BorderLayout.CENTER);
 		getContentPane().add(executionButtonPanel, BorderLayout.SOUTH);
 		
 		setVisible(true);
 		setEnabled(true);
 	}
 	
 
 }
