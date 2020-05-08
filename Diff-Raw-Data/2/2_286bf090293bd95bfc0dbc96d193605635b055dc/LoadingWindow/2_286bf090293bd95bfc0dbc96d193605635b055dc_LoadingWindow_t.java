 /**
  * 
  */
 package net.skyebook.betaville.pad;
 
 import org.fenggui.Container;
 import org.fenggui.FengGUI;
 import org.fenggui.Label;
 import org.fenggui.composite.Window;
 import org.fenggui.layout.BorderLayout;
 import org.fenggui.layout.BorderLayoutData;
 import org.fenggui.layout.RowExLayout;
 import org.fenggui.layout.RowExLayoutData;
 
 /**
  * @author Skye Book
  *
  */
 public class LoadingWindow extends Window {
 	
 	private Label dlLabel;
 	private Label dlStatus;
 	private Label dlProgress;
 	
 	private Label adrLabel;
 	private Label adrStatus;
 	private Label adrProgress;
 	
 	private Label bblLabel;
 	private Label bblStatus;
 	private Label bblProgress;
 
 	/**
 	 * 
 	 */
 	public LoadingWindow() {
 		super(true, true);
 		getContentContainer().setLayoutManager(new RowExLayout(false));
 		
 		Container dlContainerTop = FengGUI.createWidget(Container.class);
 		dlContainerTop.setLayoutManager(new BorderLayout());
 		dlContainerTop.setLayoutData(new RowExLayoutData(true, true));
 		dlLabel = FengGUI.createWidget(Label.class);
 		dlLabel.setText("Data Download");
 		dlLabel.setLayoutData(BorderLayoutData.WEST);
 		dlContainerTop.addWidget(dlLabel);
 		
 		Container dlContainerBottom = FengGUI.createWidget(Container.class);
 		dlContainerBottom.setLayoutManager(new BorderLayout());
 		dlContainerBottom.setLayoutData(new RowExLayoutData(true, true));
 		dlStatus = FengGUI.createWidget(Label.class);
 		dlStatus.setText("N/A");
 		dlStatus.setLayoutData(BorderLayoutData.WEST);
 		dlProgress = FengGUI.createWidget(Label.class);
 		dlProgress.setText("Not yet!");
 		dlProgress.setLayoutData(BorderLayoutData.EAST);
 		dlContainerBottom.addWidget(dlStatus, dlProgress);
 		
 		Container adrContainerTop = FengGUI.createWidget(Container.class);
 		adrContainerTop.setLayoutManager(new BorderLayout());
 		adrContainerTop.setLayoutData(new RowExLayoutData(true, true));
 		adrLabel = FengGUI.createWidget(Label.class);
 		adrLabel.setText("ADR Records");
 		adrLabel.setLayoutData(BorderLayoutData.WEST);
 		adrContainerTop.addWidget(adrLabel);
 		
 		Container adrContainerBottom = FengGUI.createWidget(Container.class);
 		adrContainerBottom.setLayoutManager(new BorderLayout());
 		adrContainerBottom.setLayoutData(new RowExLayoutData(true, true));
 		adrStatus = FengGUI.createWidget(Label.class);
 		adrStatus.setText("N/A");
 		adrStatus.setLayoutData(BorderLayoutData.WEST);
 		adrProgress = FengGUI.createWidget(Label.class);
 		adrProgress.setText("Not yet!");
 		adrProgress.setLayoutData(BorderLayoutData.EAST);
 		adrContainerBottom.addWidget(adrStatus, adrProgress);
 		
 		Container bblContainerTop = FengGUI.createWidget(Container.class);
 		bblContainerTop.setLayoutManager(new BorderLayout());
 		bblContainerTop.setLayoutData(new RowExLayoutData(true, true));
 		bblLabel = FengGUI.createWidget(Label.class);
 		bblLabel.setText("BBL Records");
 		bblLabel.setLayoutData(BorderLayoutData.WEST);
 		bblContainerTop.addWidget(bblLabel);
 		
 		Container bblContainerBottom = FengGUI.createWidget(Container.class);
 		bblContainerBottom.setLayoutManager(new BorderLayout());
 		bblContainerBottom.setLayoutData(new RowExLayoutData(true, true));
 		bblStatus = FengGUI.createWidget(Label.class);
 		bblStatus.setText("N/A");
 		bblStatus.setLayoutData(BorderLayoutData.WEST);
 		bblProgress = FengGUI.createWidget(Label.class);
 		bblProgress.setText("Not yet!");
 		bblProgress.setLayoutData(BorderLayoutData.EAST);
 		bblContainerBottom.addWidget(bblStatus, bblProgress);
 		
 		getContentContainer().addWidget(dlContainerTop, dlContainerBottom,
 				adrContainerTop,  adrContainerBottom, bblContainerTop, bblContainerBottom);
 		
 		layout();
 	}
 	
 	public void setDLStatus(String text){
 		dlStatus.setText(text);
 		layout();
 	}
 	
 	public void setADRStatus(String text){
 		adrStatus.setText(text);
 		layout();
 	}
 	
 	public void setBBLStatus(String text){
 		bblStatus.setText(text);
 		layout();
 	}
 	
 	public void setDLProgress(int current, int max){
 		dlProgress.setText(current+"kb/"+max+"kb");
 		layout();
 	}
 	
 	public void setADRProgress(int current, int max){
 		adrProgress.setText(current+"/"+max);
 		layout();
 	}
 	
	public void setBBLProgress(int current, int max){
 		bblProgress.setText(current+"/"+max);
 		layout();
 	}
 }
