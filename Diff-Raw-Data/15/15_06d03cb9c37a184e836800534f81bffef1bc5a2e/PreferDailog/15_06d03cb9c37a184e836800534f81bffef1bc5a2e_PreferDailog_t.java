 package org.ndaguan.micromanager.mmtracker;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 public class PreferDailog extends JFrame {
 	private static final long serialVersionUID = 1L;
 	private int ITEMWIDTH = 140;
 	private int ITEMHEIGHT = 25;
 	final private int ITEMROW = 24;
 	private JFrame instance_;
 
 	private  JTextField[] jTextField;
 
 	private JLabel[] jLabel;
 
 	private ActionListener DialogListener;
 	private int preferencesLen;
 	private int columnNum = 4;
 	String userDataDir_ = "";
 	public static void main(String[] arg){
 
 		PreferDailog pre = new PreferDailog();
 		pre.setVisible(true);
 
 	}
 	public void enableEdit(boolean flag){
 		 
 		for (int i = 0; i < MMT.unEditAfterCalbration.length; i++) {
 			this.jTextField[MMT.unEditAfterCalbration[i]].setEnabled(flag);
 		}
 	}
 	public PreferDailog() {
 		preferencesLen = MMT.VariablesNUPD.values().length;
 		jTextField = new JTextField[preferencesLen];
 		jLabel = new JLabel[preferencesLen];
 		MMT.Coefficients = new double[3][2];
 		instance_ = this;
		userDataDir_ = System.getProperty("user.home");
 		initialize();
 		onDataChange(getUserData());
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				UpdateData(false);//GUI  update
 			}
 		});
 
 	}
 	public void onDataChange(double[] data) {
 		if(data != null){
 			for (int i = 0; i < data.length; i++) {
 				MMT.VariablesNUPD.values()[i].value(data[i]);
 			}
 			//PID
 			MMT.Coefficients[0][0] = MMT.VariablesNUPD.pTerm_x.value();
 			MMT.Coefficients[0][1] = MMT.VariablesNUPD.iTerm_x.value();
 			MMT.Coefficients[1][0] = MMT.VariablesNUPD.pTerm_y.value();
 			MMT.Coefficients[1][1] = MMT.VariablesNUPD.iTerm_y.value();
 			MMT.Coefficients[2][0] = MMT.VariablesNUPD.pTerm_z.value();
 			MMT.Coefficients[2][1] = MMT.VariablesNUPD.iTerm_z.value();
 			
 			MMTracker mmt = MMTracker.getInstance();
 			if(mmt != null){
 				List<RoiItem> roilist = mmt.getRoiList();
 				for(RoiItem item: roilist){
 					item.setCalcForceWidowSize(MMT.VariablesNUPD.frameToCalcForce.value());
 					item.setChartWidth(MMT.VariablesNUPD.chartWidth.value());
 					item.setChartRangeWidowSize(MMT.VariablesNUPD.chartStatisWindow.value());
 					item.setFeedbackWidowSize(MMT.VariablesNUPD.feedBackWindowSize.value());
 				}
 			}
 		}
 		saveUserData();
 	}
 
 	public double[] getUserData() {
 		try {
 			File loginDataFile = new File(System.getProperty("user.home")+"/MMTracker/"+MMT.currentUser+"_userPreferences.txt");
 			if(!loginDataFile.exists())
 				return null;
 			BufferedReader in;
 			in = new BufferedReader(new FileReader(loginDataFile));
 			String line;
 			if((line = in.readLine()) == null)
 			{
 				in.close();
 				return null;
 			}
 			String[] temp = line.split(","); 
 			
 			if((line = in.readLine()) != null)
 				userDataDir_ = line;
 			
 			double[] userDataSet = new double[MMT.VariablesNUPD.values().length];
 			if((temp.length-1) < MMT.VariablesNUPD.values().length){
 				
 				for (int i = 0; i < temp.length-1; i++) {//old var
 					userDataSet[i] = Double.parseDouble(temp[i]);				
 				}
 				
 				for (int i = temp.length-1; i < MMT.VariablesNUPD.values().length; i++) {//new var,user default
 					 userDataSet[i]  = MMT.VariablesNUPD.values()[i].value();
 				}
 				onDataChange(userDataSet);
 				saveUserData();
 			}
 			else{
 			for (int i = 0; i < userDataSet.length; i++) {
 				userDataSet[i] = Double.parseDouble(temp[i]);				
 			}
 			}
 			
 			in.close();
 			return userDataSet;
 		} catch (IOException e) {
 			MMT.logError("read user data false");
 			return null;
 		} 
 	}
 
 
 	public void saveUserData(){
 		try {
 			File dir = new File(System.getProperty("user.home"),"MMTracker");
 			if(!dir.isFile())
 				dir.mkdirs();
 
 			File loginDataFile = new File(System.getProperty("user.home")+"/MMTracker/"+MMT.currentUser+"_userPreferences.txt");
 			FileWriter out = new FileWriter((loginDataFile)); 
 			String sData = "";
 			for (int i = 0; i < MMT.VariablesNUPD.values().length; i++) {
 				sData +=  Double.toString(MMT.VariablesNUPD.values()[i].value())+ " , ";
 			}
 			sData += "\r\n"+ userDataDir_;
 			out.write(sData);
 			out.close(); 
 		} catch (IOException e) {
 			MMT.logError("save user data err");
 		}
 	}
 	/**
 	 * @param flag true save to file,false: update gui
 	 */
 	public void UpdateData(boolean flag) {
 		if(flag){//flush
 			double[] preferences = new double[preferencesLen];
 			for (int i = 0; i <preferencesLen; i++) {
 				preferences[i] = Double.parseDouble(jTextField[i].getText());
 			}			
 			onDataChange(preferences);
 		}
 		else{//refresh GUI
 			for (int i = 0; i <preferencesLen; i++) {
 				double presicion = MMT.VariablesNUPD.values()[i].getPresicion();
 				String str;
 				if((presicion < 1/1e10)){
 					str = String.format("%s.%df","%" , 0);
 				}
 				else{
 				str = String.format("%s.%df","%" ,(int)(Math.log10(1/presicion)));
 				}
 				jTextField[i].setText(String.format(str,getUserData()[i]));
 			}	
 		}
 
 	}
 
 	private void initialize(){
 		DialogListener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) 
 			{PhraseActionEvent(e);}};
 
 			Toolkit kit = Toolkit.getDefaultToolkit();
 			Dimension screen = kit.getScreenSize();
 			getContentPane().setLayout(null);
 			int frameWidth = (int)(ITEMWIDTH*columnNum);
 			int frameHeight = 2*ITEMHEIGHT*preferencesLen/columnNum + ITEMHEIGHT*ITEMROW/3 ;
 			setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 			final JTabbedPane tabbedPane = new JTabbedPane();
 	 
 			int classifyLen = MMT.VariablesClassify.values().length;
 			JPanel tab[] = new JPanel[classifyLen];
 			for(int i = 0;i<classifyLen;i++){
 				tab[i] = new JPanel();
 				tab[i].setLayout(null);
 				tabbedPane.addTab(MMT.VariablesClassify.values()[i].name(),null,tab[i],null);
 			}
 			int tabY[] = new int[classifyLen];
 			int tabX[] = new int[classifyLen];
 			for (int i = 0; i < preferencesLen; i++) {//edit & label
 				int tabIndex = MMT.VariablesNUPD.values()[i].getTabIndex();
 				if(tabIndex == -1)continue;
 				jLabel[i] = new JLabel();
 				jLabel[i].setText(String.format("%s%s", MMT.VariablesNUPD.values()[i].name(),MMT.VariablesNUPD.values()[i].getUnit()));
 				jLabel[i].setBounds(tabX[tabIndex],tabY[tabIndex],ITEMWIDTH,ITEMHEIGHT);
 				
 				tabY[tabIndex] += ITEMHEIGHT;
 				jTextField[i] = new JTextField();
 				jTextField[i].setBounds(tabX[tabIndex], tabY[tabIndex], ITEMWIDTH,ITEMHEIGHT);
 				jTextField[i].setToolTipText(MMT.VariablesNUPD.values()[i].getToolTip());
 				if(MMT.VariablesNUPD.values()[i].getImp()==1)
 				jTextField[i].setForeground(new Color(255,0,0));
 				tab[tabIndex].add(jLabel[i]);
 				tab[tabIndex].add(jTextField[i]);
 				tabX[tabIndex] += ITEMWIDTH;
 				tabY[tabIndex] -= ITEMHEIGHT;
 				if(tabX[tabIndex]/ITEMWIDTH  == columnNum ){
 					tabY[tabIndex] += 2*ITEMHEIGHT;
 					tabX[tabIndex] = 0;
 				}
 			}			
 			int x = 0;
 			int y = 0;
 			for(int i=0;i<classifyLen;i++){
 				y = y<tabY[i]?tabY[i]:y;
 			}
 			y += ITEMHEIGHT;
 			frameHeight = y+ITEMHEIGHT*4;
 			setBounds((int)(screen.width -frameWidth)/2,(int)(screen.height-frameHeight)/2,frameWidth ,frameHeight);
 			
 			tabbedPane.setBounds(0,0,(int)(ITEMWIDTH*(columnNum+0.2)), y);
 			getContentPane().add(tabbedPane);
 			final JPanel buttonBox = new JPanel();
 			buttonBox.setLayout(null);
 			buttonBox.setBounds(0, 10,  frameWidth,frameHeight);
 			getContentPane().add(buttonBox);
 			
 			final JSeparator separator2 = new JSeparator();
 			separator2.setBounds(0,y, ITEMWIDTH*4, 50);
 			buttonBox.add(separator2);
 			
 			ITEMWIDTH = ITEMWIDTH*3/4;
 			
 			final JButton OK = new JButton("OK");
 			OK.setBounds(0,  y,ITEMWIDTH,(int)(ITEMHEIGHT*1.5));
 			buttonBox.add(OK);
 			x += ITEMWIDTH;
 			
 			final JButton Apply = new JButton("Apply");
 			Apply.setBounds(x,y,ITEMWIDTH,(int)(ITEMHEIGHT*1.5));
 			buttonBox.add(Apply);
 			x += ITEMWIDTH;
 
 			final JButton Cancel = new JButton("Cancel");
 			Cancel.setBounds(x,y, ITEMWIDTH,(int)(ITEMHEIGHT*1.5));
 			buttonBox.add(Cancel);
 			x += ITEMWIDTH;
 
 			final JButton SelectDir = new JButton("SelectDir");
 			SelectDir.setBounds(x,y, ITEMWIDTH,(int)(ITEMHEIGHT*1.5));
 			buttonBox.add(SelectDir);
 			x += ITEMWIDTH;
 
 			final JButton OpenDir = new JButton("OpenDir");
 			OpenDir.setBounds(x,y, ITEMWIDTH,(int)(ITEMHEIGHT*1.5));
 			buttonBox.add(OpenDir);
 
 			OK.addActionListener(DialogListener);
 			Apply.addActionListener(DialogListener);
 			Cancel.addActionListener(DialogListener);
 			SelectDir.addActionListener(DialogListener);
 			OpenDir.addActionListener(DialogListener);
 			
 			
 			
 	}
 
 	private void PhraseActionEvent(ActionEvent e){
 		if (e.getActionCommand().equals("OK")) {
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					UpdateData(true);//flush
 					instance_.setVisible(false);
 				}
 			});
 
 		}
 		if (e.getActionCommand().equals("Apply")) {
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					UpdateData(true);//flush
 				}
 			});
 			
 		}
 		if (e.getActionCommand().equals("Cancel")) {
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					instance_.setVisible(false);
 				}
 			});
 
 		}
 
 		if (e.getActionCommand().equals("SelectDir")) {
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					JFileChooser fileChooser = new JFileChooser(".");		 
 					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 					fileChooser.setDialogTitle("Select a data save path");
 					int ret = fileChooser.showOpenDialog(null);
 					if (ret == JFileChooser.APPROVE_OPTION) {
 						userDataDir_=fileChooser.getSelectedFile().getAbsolutePath();
 						saveUserData();
 					}
 				}
 			});
 
 		}
 
 		if (e.getActionCommand().equals("OpenDir")) {
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						Runtime.getRuntime().exec("explorer /select, "+userDataDir_);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					instance_.setVisible(false);
 				}
 			});
 
 		}
 
 
 	}
 
 }
