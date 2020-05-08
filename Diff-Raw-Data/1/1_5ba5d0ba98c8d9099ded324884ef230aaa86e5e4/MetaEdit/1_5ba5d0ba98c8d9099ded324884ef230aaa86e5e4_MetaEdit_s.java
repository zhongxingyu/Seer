 import javax.imageio.ImageIO;
 import javax.swing.*;
 import com.beaglebuddy.mp3.MP3;
 import com.beaglebuddy.mp3.enums.PictureType;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 
 public class MetaEdit {
 	static JFrame frame1;
 	static Container pane;
 	static JButton btnSave, btnPic;
 	static JLabel lblTitle, lblArtist, lblAlbum, lblYear, lblTrackNumber, lblGenre, lblComments, lblPicture, picPicture;
 	static JTextField txtTitle, txtArtist, txtAlbum, txtYear, txtTrackNumber, txtGenre, txtComments;
 	static String file, newPic;
 	static JFileChooser filePicker = new JFileChooser();
 	static Image newImg;
 	
 	public static void main (String args[]) {
 		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
 		catch (ClassNotFoundException e) {}
 		catch (InstantiationException e) {}
 		catch (IllegalAccessException e) {}
 		catch (UnsupportedLookAndFeelException e) {}
 		
 		frame1 = new JFrame("Meta Edit");
 		frame1.setSize(265, 480);
 		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pane = frame1.getContentPane();
 		pane.setLayout(null);
 
 		lblTitle = new JLabel("Title:"); pane.add(lblTitle); lblTitle.setBounds(10, 12, lblTitle.getPreferredSize().width, lblTitle.getPreferredSize().height);
 		txtTitle = new JTextField(20); pane.add(txtTitle); txtTitle.setBounds(70, 10, txtTitle.getPreferredSize().width, txtTitle.getPreferredSize().height);
 		lblArtist = new JLabel("Artist:"); pane.add(lblArtist); lblArtist.setBounds(10, 42, lblArtist.getPreferredSize().width, lblArtist.getPreferredSize().height);
 		txtArtist = new JTextField(20); pane.add(txtArtist); txtArtist.setBounds(70, 40, txtArtist.getPreferredSize().width, txtArtist.getPreferredSize().height);
 		lblAlbum = new JLabel("Album:"); pane.add(lblAlbum); lblAlbum.setBounds(10, 72, lblAlbum.getPreferredSize().width, lblAlbum.getPreferredSize().height);
 		txtAlbum = new JTextField(20); pane.add(txtAlbum); txtAlbum.setBounds(70, 70, txtAlbum.getPreferredSize().width, txtAlbum.getPreferredSize().height);
 		lblYear = new JLabel("Year"); pane.add(lblYear); lblYear.setBounds(10, 102, lblYear.getPreferredSize().width, lblYear.getPreferredSize().height);
 		txtYear = new JTextField(20); pane.add(txtYear); txtYear.setBounds(70, 100, txtYear.getPreferredSize().width, txtYear.getPreferredSize().height);
 		lblTrackNumber = new JLabel("Track:"); pane.add(lblTrackNumber); lblTrackNumber.setBounds(10, 132, lblTrackNumber.getPreferredSize().width, lblTrackNumber.getPreferredSize().height);
 		txtTrackNumber = new JTextField(20); pane.add(txtTrackNumber); txtTrackNumber.setBounds(70, 130, txtTrackNumber.getPreferredSize().width, txtTrackNumber.getPreferredSize().height);
 		lblGenre = new JLabel("Genre"); pane.add(lblGenre); lblGenre.setBounds(10, 162, lblGenre.getPreferredSize().width, lblGenre.getPreferredSize().height);
 		txtGenre = new JTextField(20); pane.add(txtGenre); txtGenre.setBounds(70, 160, txtGenre.getPreferredSize().width,txtGenre.getPreferredSize().height);
 		lblComments = new JLabel("Comments:"); pane.add(lblComments); lblComments.setBounds(10, 192, lblComments.getPreferredSize().width, lblComments.getPreferredSize().height);
 		txtComments = new JTextField(20); pane.add(txtComments); txtComments.setBounds(70, 190, txtComments.getPreferredSize().width, txtComments.getPreferredSize().height);
 		lblPicture = new JLabel("Picture:"); pane.add(lblPicture); lblPicture.setBounds(10, 222, lblPicture.getPreferredSize().width, lblPicture.getPreferredSize().height);
 		picPicture = new JLabel("Drag and Drop an MP3 to Edit."); pane.add(picPicture); picPicture.setBounds(70, 220, 165, 165);
 		btnSave = new JButton("Save"); pane.add(btnSave); btnSave.setBounds(120, 400, btnSave.getPreferredSize().width, btnSave.getPreferredSize().height);
 		filePicker = new JFileChooser();
 		
 		frame1.setVisible(true);
 		btnSave.addActionListener(new btnSaveAction());
 		
 		new FileDrop( System.out, pane, new FileDrop.Listener() {   
 			public void filesDropped( java.io.File[] files ) {   
 				for( int i = 0; i < files.length; i++ ) {
 					try {
 						file = (String) (files[i].getCanonicalPath().replace("\\", "/"));
 						try {
 							MP3 mp3 = new MP3(file);
 							if (mp3.hasErrors()) { mp3.displayErrors(System.out); mp3.save(); }
 							txtTitle.setText(mp3.getTitle());
 							txtArtist.setText(mp3.getBand());
 							txtAlbum.setText(mp3.getAlbum());
 							txtYear.setText(Integer.toString(mp3.getYear()));
 							txtTrackNumber.setText(Integer.toString(mp3.getTrack()));
 							txtGenre.setText(mp3.getMusicType());
 							txtComments.setText(mp3.getComments());
 							try {
 								Image picture = new ImageIcon(mp3.getPicture(PictureType.FRONT_COVER)).getImage().getScaledInstance(165, 165, java.awt.Image.SCALE_SMOOTH);							
 								picPicture.setIcon(new ImageIcon(picture));
 							} catch (NullPointerException ex) {
 								picPicture.setHorizontalAlignment(SwingConstants.CENTER);
 								picPicture.setText("Click to Browse...");
 								picPicture.setIcon(null);
 							}
 						} catch (IOException ex) {
 						    System.out.println("An error occurred while reading/saving the mp3 file.");
 						}
                     }
                     catch( java.io.IOException e ) {}
                 }
             }
         });
 		
 		picPicture.addMouseListener(new MouseAdapter() {
 			int answer;
 			public void mousePressed(MouseEvent e) {
 				if (picPicture.getIcon() != null) {
 					answer = JOptionPane.showConfirmDialog(
 							null, "Are you sure you want to delete this image?",
 							null, JOptionPane.YES_NO_OPTION
 					);
 					if (answer == JOptionPane.YES_OPTION) {
 						picPicture.setHorizontalAlignment(SwingConstants.CENTER);
 						picPicture.setText("Click to Browse...");
 						picPicture.setIcon(null);
 						try {
 							MP3 mp3 = new MP3(file);
 							if (mp3.hasErrors()) { mp3.displayErrors(System.out); mp3.save(); }
 							mp3.removePicture(PictureType.FRONT_COVER);
 						} catch (IOException e1) {}
 					}
 				} else {
 					if (! picPicture.getText().equals("Drag and Drop an MP3 to Edit.")) {
 						filePicker.showOpenDialog(pane);
 						try {
 							newImg = ImageIO.read(new File(filePicker.getSelectedFile().getCanonicalPath()));
 							Image picture = newImg.getScaledInstance(165, 165, java.awt.Image.SCALE_SMOOTH);
 							picPicture.setIcon(new ImageIcon(picture));
 						} catch (NullPointerException e1) {} catch (IOException e1) {}
 					}
 				}
 			}
 		});
 		
 	}
 	
 	public static class btnSaveAction implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			try {
 				MP3 mp3 = new MP3(file);
 				if (mp3.hasErrors()) { mp3.displayErrors(System.out); mp3.save(); }
 			    try {
 			    	mp3.setTitle(txtTitle.getText());
 			    } catch (IllegalArgumentException ex) {
 			    	mp3.removeTitle();
 			    }
 			    try {
 			    	mp3.setBand(txtArtist.getText());
 			    } catch (IllegalArgumentException ex) {
 			    	mp3.removeBand();
 			    }
 			    try {
 			    	mp3.setAlbum(txtAlbum.getText());
 			    } catch (IllegalArgumentException ex) {
 			    	mp3.removeAlbum();
 			    }
 			    try {
 			    	mp3.setYear(Integer.parseInt(txtYear.getText()));
 			    } catch (IllegalArgumentException ex) {
 			    	mp3.removeYear();
 			    }
 			    try {
 			    	mp3.setTrack(Integer.parseInt(txtTrackNumber.getText()));
 			    } catch (IllegalArgumentException ex) {
 			    	mp3.removeTrack();
 			    }
 			    try {
 			    	mp3.setMusicType(txtGenre.getText());
 			    } catch (IllegalArgumentException ex) {
 			    	mp3.removeMusicType();
 			    }
 			    try {
 			    	mp3.setComments(txtComments.getText()); 
 			    } catch (IllegalArgumentException ex) {
 			    	mp3.removeComments();
 			    }
 			    try {
 			    	mp3.setPicture(PictureType.FRONT_COVER, filePicker.getSelectedFile().getCanonicalPath());
 			    } catch (NullPointerException ex) {
 			    	if (picPicture.getIcon() == null) {
 			    		mp3.removePicture(PictureType.FRONT_COVER);
 			    	}
 			    }
 			    mp3.save();
 			} catch (IOException ex) {
 			    System.out.println("An error occurred while reading/saving the mp3 file.");
 			}
 		}	
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
