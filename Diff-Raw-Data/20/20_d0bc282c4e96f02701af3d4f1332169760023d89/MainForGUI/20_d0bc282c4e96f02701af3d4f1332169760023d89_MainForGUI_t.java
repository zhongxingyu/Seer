 package main;
 
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 import javax.swing.JFileChooser;
 
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.errors.RevisionSyntaxException;
 import org.eclipse.jgit.revwalk.RevCommit;
 
 import visual.primary.Visualizer;
 
 import data.RepoFile;
 import data.RepoFileManager;
 
 public class MainForGUI extends Frame {
 
 	private static String gitPath;
 	Panel panel;
 	Button b1,b2;
 	public MainForGUI(ArrayList<String> authorList, 
 			ArrayList<RepoFile> repfiles)
 	{
 
 		// Set frame properties
 		setTitle("MainForGUI"); // Set the title
 		setSize(400,400); // Set size to the frame
 		//layout
 		this.setLayout(new BorderLayout());
 		setVisible(true); // Make the frame visible
 		setLocationRelativeTo(null);  // Center the frame
 
 		// Create the panel
 		panel=new Panel();
 
 		// Set panel background
 		panel.setBackground(Color.gray); 	 	
 
 		// Create buttons
 		b1=new Button(); // Create a button with default constructor
 		b1.setLabel("I am button 1"); // Set the text for button
 
 		b2=new Button("Button 2"); // Create a button with sample text
 		b2.setBackground(Color.lightGray); // Set the background to the button
 
 		// Add the buttons to the panel
 		panel.add(b1);
 		panel.add(b2);
 
 		// Add the panel to the frame
 		this.add(panel,BorderLayout.NORTH);
 
 		addWindowListener(new WindowAdapter(){
 			public void windowClosing(WindowEvent we)
 			{
 				System.exit(0);
 			}
 		});
 
 		//Test visualization
 		Visualizer vis = new Visualizer(authorList,repfiles);
 		this.add(vis,BorderLayout.CENTER);
 		vis.init();
 	}
 
 
 
 	public static void main(String args[])
 	{
 		JFileChooser chooser = new JFileChooser();
 		chooser.setDialogTitle("Select target directory");
 		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		int returnVal = chooser.showOpenDialog(new Frame());
 		if(returnVal == JFileChooser.APPROVE_OPTION) {
 			File myFile = chooser.getSelectedFile();
 
 			gitPath=myFile.getAbsolutePath();
 		}
 		try {
 
 			RepoFileManager rfm=new RepoFileManager(gitPath);
 
 			LinkedList<String> files=rfm.getRelevantPathofFilesByExetension("java");
 
 			Iterable<RevCommit> commits=rfm.getCommitList();
 
 			int i=0;
 
 			//visualizer
 			ArrayList<RepoFile> fileList = new ArrayList<RepoFile>();
 			
 			//for(RevCommit commit:commits){
			int count = 7;
			
 			for(String s:files){
 				System.out.println("the interation: "+i++);
 				//HashSet<String> commitlastIds= RepoFileManager.getAlteringCommitIDs(rfm.getRepo(), s);
 
 				//for(String commit:commitlastIds) {
 					//System.out.println("this is th file relevant path "+s);
 					//System.out.println("this commitId is "+ commit); 
 					RepoFile rf=new RepoFile(rfm.getRepo(), s, "HEAD" );
 					
 					//visualizer code
 					fileList.add(rf);
 				//}				
					if(count > 0) count --;
					else break;
 			}
 			
 			ArrayList<String> authors = new ArrayList<String>();
 			
 			authors.add("Bartek Przybylski");
 			authors.add("David A. Velasco");
 			authors.add("Espen Fossen");
 			authors.add("F.T.");
 			authors.add("Jenkins for ownCloud");
 			authors.add("Jrg Thalheim");
 			authors.add("Lennart Rosam");
 			authors.add("Luke Owncloud");
 			authors.add("Matthias Baumann");
 			authors.add("Mik");
 			authors.add("Mik-");
 			authors.add("Sven Amann");
 			authors.add("Thomas Mueller");
 			authors.add("Thomas Mller");
 			authors.add("Thorsten");
 			authors.add("jmiazga");
 			authors.add("masensio");
 			authors.add("zerginator");
 			
 			new MainForGUI(authors,fileList);
 					
 		} catch (RevisionSyntaxException | GitAPIException | IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 
 }
