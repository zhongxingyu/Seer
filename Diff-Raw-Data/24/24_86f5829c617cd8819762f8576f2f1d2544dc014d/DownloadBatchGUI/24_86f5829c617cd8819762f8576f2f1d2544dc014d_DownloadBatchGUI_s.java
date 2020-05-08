 package client;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import models.FailedResult;
 import models.Fields;
 import server.ClientCommunicator;
 import communicator.DownloadBatchParams;
 import communicator.DownloadBatchResult;
 import communicator.GetProjectsResult;
 import communicator.ValidateUserParams;
 
 public class DownloadBatchGUI {
 	private static final long serialVersionUID = 8227044344356048883L;
 	private ClientGUI cg;
 	private Integer projectId;
 	private ArrayList<GetProjectsResult> projects;
 	private JComboBox projectList;
 	private JDialog dbDialog;
 	
 	public JDialog makeDownloadBatchGUI(){
 		JFrame frame = new JFrame("Download Batch");
 		this.dbDialog = new JDialog(frame, "Download Batch", true);
 		
 		this.dbDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		Dimension panelSize = new Dimension(400, 200);
 		this.dbDialog.setSize(panelSize);
 		this.dbDialog.setMinimumSize(panelSize);
 		this.dbDialog.setMaximumSize(panelSize);
 		Container pane = this.dbDialog.getContentPane();
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		pane.setLayout(new GridBagLayout());
 		
 		JLabel label = new JLabel("Project: ");
 		c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 0;
 		label.setVisible(true);
 		pane.add(label, c);
 		
 		projects = this.getProjects();
 		System.out.println(projects.size());
 		String[] names = new String[projects.size()];
 		for(int i = 0; i < projects.size(); i++){
 			names[i] = projects.get(i).getTitle();
 		}
 		
 		projectList = new JComboBox(names);
 		projectId = -1;
 		projectList.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(projectList.getSelectedIndex() == -1){
 					// nothing selected
 				}else{
 					projectId = projects.get(projectList.getSelectedIndex()).getId();
 					System.out.println("projectid is now: " + projectId);
 				}
 			}
 		});
 		projectList.setSelectedIndex(0);// load initial thing
 		c.weightx = 1;
 		c.gridx = 1;
 		c.gridy = 0;
 		pane.add(projectList, c);
 		
 		JButton viewSampleButton = new JButton("View Sample");
 		c.weightx = 1;
 		c.gridx = 2;
 		c.gridy = 0;
 		viewSampleButton.setVisible(true);
 		viewSampleButton.addMouseListener(new MouseListener(){
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				SampleImage si = new SampleImage(cg);
 				JDialog dialog = si.makeSampleImage(projects.get(projectList.getSelectedIndex()).getTitle(), projectId);
 				dialog.setVisible(true);
 			}
 			@Override public void mousePressed(MouseEvent e) {}
 			@Override public void mouseReleased(MouseEvent e) {}
 			@Override public void mouseEntered(MouseEvent e) {}
 			@Override public void mouseExited(MouseEvent e) {}
 		});
 		pane.add(viewSampleButton, c);
 		
		JButton cancelButton = new JButton("Download");
 		c.weightx = 1;
		c.gridx = 0;
 		c.gridy = 1;
		cancelButton.setVisible(true);
		cancelButton.addMouseListener(new MouseListener(){
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if(projectId != -1){
 					cg.downloadedBatch(downloadBatch(projectId));
 					dbDialog.setVisible(false);
 				}
 			}
 			@Override public void mousePressed(MouseEvent e) {}
 			@Override public void mouseReleased(MouseEvent e) {}
 			@Override public void mouseEntered(MouseEvent e) {}
 			@Override public void mouseExited(MouseEvent e) {}
 		});
		pane.add(cancelButton, c);
 		
		JButton downloadButton = new JButton("Cancel");
 		c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 1;
		downloadButton.setVisible(true);
		downloadButton.addMouseListener(new MouseListener(){
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				cg.closeDownloadBatchGUI();
 			}
 			@Override public void mousePressed(MouseEvent e) {}
 			@Override public void mouseReleased(MouseEvent e) {}
 			@Override public void mouseEntered(MouseEvent e) {}
 			@Override public void mouseExited(MouseEvent e) {}
 		});
		pane.add(downloadButton, c);
 		return this.dbDialog;
 	}
 
 	public DownloadBatchGUI(final ClientGUI cg){
 //		super("Download Batch");
 		this.cg = cg;
 	}
 	
 	@SuppressWarnings("finally")
 	private DownloadBatchResult downloadBatch(int projectId) {
 		ClientCommunicator cc = new ClientCommunicator();
 		DownloadBatchParams var = new DownloadBatchParams();
 		DownloadBatchResult result = null;
 		var.setUsername(cg.username);
 		var.setPassword(cg.password);
 		var.setProjectId(projectId);
 		try {
 			System.out.println("making request");
 			Object response = cc.downloadBatch(var, cg.host, cg.port);
 			System.out.println("got a response");
 			if(response instanceof DownloadBatchResult){
 				((DownloadBatchResult)response).setImageUrl("http://" + cg.host + ":" + cg.port + "/" + ((DownloadBatchResult)response).getImageUrl());
 				for(Fields f : ((DownloadBatchResult)response).getFields()){
 					if(f.getHelpHtml() != null)
 						f.setHelpHtml("http://" + cg.host + ":" + cg.port + "/" + f.getHelpHtml());
 					if(f.getKnownData() != null)
 						f.setKnownData("http://" + cg.host + ":" + cg.port + "/" + f.getKnownData());
 				}
 				System.out.println("setting response");
 				result = (DownloadBatchResult)response;
 			}else if(response instanceof FailedResult){
 				System.out.println("failed to get batch");
 			}else
 				System.out.println("something else" + response.getClass());
 		} catch (Exception e) {
 
 		}finally{
 			System.out.println("returning result");
 			return result;
 		}
 	}
 	
 	@SuppressWarnings({ "finally", "unchecked" })
 	private ArrayList<GetProjectsResult> getProjects() {
 		ArrayList<GetProjectsResult> result = null;
 		ClientCommunicator cc = new ClientCommunicator();
 		ValidateUserParams vup = new ValidateUserParams();
 		vup.setPassword(cg.password);
 		vup.setUsername(cg.username);
 		try {
 			Object response = cc.getProjects(vup, cg.host, cg.port);
 			if(response instanceof ArrayList){
 				System.out.println("is array list");
 				result = (ArrayList<GetProjectsResult>)response;
 			}else if(response instanceof FailedResult){
 				System.out.println("failedResult");
 //				result = ((FailedResult)response).toString();
 			}else
 				System.out.println("something selse");
 //			else
 //				result = "something else" + response.getClass();
 //			getView().setResponse(result);
 		} catch (Exception e) {
 			System.out.println("exception");
 			e.printStackTrace();
 //			result = "FAILED";
 		}finally{
 //			getView().setResponse(result);
 			System.out.println("finally");
 			return result;
 		}
 	}
 }
