  /**
  * Class GameBrowser
  *
  * Displays and updates the user profile
  *
  * @author Leon Blakey/Lord.Quackstar
  */
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 
 import com.jgoodies.forms.layout.FormLayout;
 
 import java.awt.BorderLayout;
 import java.awt.Graphics;
 import java.awt.Image;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.awt.image.BufferedImage;
 
 import java.io.File;
 
 import java.net.URLEncoder;
 
 import java.nio.file.Path;
 
 import javax.imageio.ImageIO;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import org.json.me.JSONObject;
 
  
 
 
 public class Profile extends JPanel implements ActionListener {
 	
 	String newAvatar;
 	JTextField realName,gamersTag,grade,browseField;
 	JTextArea descPane,favGame,bestAt;
 	
     public JPanel generate() {
     	System.out.println("Profile Class initiated");
 		setLayout(new BorderLayout());
 
         // Column specs only, rows will be added dynamically.
         FormLayout layout = new FormLayout(
                 "left:[40dlu,pref], 3dlu, 150dlu, 16dlu, "
               + "left:[40dlu,pref], 3dlu, 150dlu");
         DefaultFormBuilder builder = new DefaultFormBuilder(layout);
         builder.setDefaultDialogBorder();
 
 		//Folder Area
         builder.appendSeparator("Basic Information");
         builder.append("Real Name",realName=new JTextField(GamersClub.realName,10));
         builder.append("Gamers Tag",gamersTag=new JTextField(GamersClub.gamersTag,10));
         builder.nextLine();
         builder.append("<HTML>Grade <BR>(number only, eg 12 for senior, <br>11 for junior, etc)</HTML>",grade=new JTextField(GamersClub.grade,10));
         builder.nextLine();
         bestAt = new JTextArea(GamersClub.bestAt,2,100);
 		bestAt.setBorder(null); 
 		bestAt.setLineWrap(true);
 		bestAt.setWrapStyleWord(true);
 		bestAt.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
         builder.append("Best At",new JScrollPane(bestAt));
        	favGame = new JTextArea(GamersClub.favGame,2,100);
 		favGame.setBorder(null); 
 		favGame.setLineWrap(true);
 		favGame.setWrapStyleWord(true);
 		favGame.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
         builder.append("Favorite Games",new JScrollPane(favGame));
         builder.nextLine();
 		
 		//Game Section
 		builder.appendSeparator("Avatar");
		String avatarLoc = (GamersClub.avatar == "NULL") ? "no_avatar.gif" : GamersClub.avatarDir+"/"+GamersClub.avatar;
 		ImageIcon avatarIcon = Globs.resizePic(avatarLoc,200,200);
 		JLabel avatarLabel = new JLabel(avatarIcon);
         builder.append(avatarLabel,7);
         builder.nextLine();
         JPanel browsePanel = new JPanel();
 		browsePanel.setLayout(new BorderLayout());
 		//add Current folder field
 		browseField=new JTextField(100);
 		browseField.setEditable(false);
 		browsePanel.add(browseField,BorderLayout.CENTER);
 		//add browse button
 		JButton browseButton = new JButton("Browse");
 		browseButton.setActionCommand("FILE_ONLY");
 		browseButton.addActionListener(this);
 		browsePanel.add(browseButton,BorderLayout.EAST);
         builder.append("Upload New Avatar", browsePanel,5);
 		builder.nextLine();
         
         //Description
         builder.appendSeparator("Short Description");
         descPane = new JTextArea("Type a description of yourself, favorite quotes, anything. Just watch the language.",3,100);
 		if(!GamersClub.desc.equals(""))
 			descPane.setText(GamersClub.desc);
 		descPane.setBorder(null); 
 		descPane.setLineWrap(true);
 		descPane.setWrapStyleWord(true);
 		descPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
 		JScrollPane descScroll = new JScrollPane(descPane);
         builder.append(descScroll,5);
         builder.nextLine();
         
         //Submit Button
 		builder.appendSeparator("Submit");
 		JButton submit = new JButton("Submit");
 		submit.addActionListener(this);
 		builder.append(submit);
 		
 		//Add to Panel
         add(builder.getPanel());
 		//add(debug);
 		
 		return this;
     }
     
 	public void actionPerformed(ActionEvent e) {
 		String cmd = e.getActionCommand();
     	if(cmd.equals("FILE_ONLY")) {
 			System.out.println("Generating File Chooser");
 			JFileChooser fc = new JFileChooser();
     		fc.setFileFilter(new FileNameExtensionFilter("All Pictures","tiff" ,"tif","gif","jpeg","jpg","bmp","png"));
 			int returnVal = fc.showOpenDialog(null);
             if (returnVal != JFileChooser.APPROVE_OPTION) {
 		       	System.out.println("User Canceled");					
 				return;
 		    }
 		    String gamePath = fc.getSelectedFile().getAbsolutePath();
 		    newAvatar = gamePath;
 		    System.out.println("Dialog Selection: " + gamePath);
 			browseField.setText(gamePath);
 		}
 		else if(cmd.equals("Submit")) {
 			//Quick checks
 			if(realName.getText().equals("") ||
 				gamersTag.getText().equals("") ||
 				grade.getText().equals("") ||
 				favGame.getText().equals("") ||
 				bestAt.getText().equals("") ||
 				descPane.getText().equals("Type a description of yourself, favorite quotes, anything. Just watch the language.")) {
 				
 				JOptionPane.showMessageDialog(null,"Please finish filling out the form!");
 				return;	
 			}
 			
 			try {
 				JOptionPane.showMessageDialog(null,"This program will most likley freeze /n while copying submitting; this is normal. /n Attempting to exit, log off, or starting this program again will damage your profile");
 				
 				
 				//Send to database
 				JSONObject formData = new JSONObject();
 				formData.put("realName",realName.getText());
 				formData.put("gamersTag",gamersTag.getText());
 				formData.put("grade",grade.getText());
 				formData.put("bestAt",bestAt.getText());
 				formData.put("favGame",favGame.getText());
 				formData.put("descPane",descPane.getText());
 				formData.put("userid",GamersClub.uid);
 				if(GamersClub.avatar != JSONObject.NULL && browseField.getText().equals("")) {
 					formData.put("avatar",JSONObject.NULL );
 				}
				else if(!browseField.getText().equals("") || GamersClub.avatar == null) {
 					String newName = Globs.obscurePath().toString();
 					formData.put("avatar",newName);
 					
 					//Copy Avatar
 		            System.out.println("Resizing and Saving Picture");
 					ImageIcon resizedImage = new ImageIcon(browseField.getText());
 		            BufferedImage resizedBImage = new BufferedImage (resizedImage.getIconWidth(), resizedImage.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
 					resizedBImage.getGraphics().drawImage(resizedImage.getImage(), 0 , 0, null);
					ImageIO.write(resizedBImage, "png",new File(GamersClub.avatarDir+"/"+newName)); 
 				}	
 				
 				String data = URLEncoder.encode("data", "UTF-8") + "=" + URLEncoder.encode(formData.toString(), "UTF-8"); 
 				
 				System.out.println("Buiding done, submitting to website");
 				String response = Globs.webTalk("mode=updateProfile",data,"Sucess");
 				if(response.indexOf("ERROR") != -1) {
 					System.err.println("Update Error!");
 					System.err.println(response);
 					return;
 				}
 			}
 			catch(Exception ex) {
 				ex.printStackTrace();
 			}
 			//Done submitting and no errors, switch to main menu
 			Globs.switchBody("MainMenu");
 		}
     }
 }
