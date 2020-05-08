 package edu.cs305.paintproject.client;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import javax.imageio.ImageIO;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import edu.cs305.paintproject.Constants;
 import edu.cs305.paintproject.util.Logger;
 
 @SuppressWarnings("serial")
 public class SidePanel extends JPanel implements ActionListener {
 	
 	enum Operation {Pencil, Bucket}; 
 	Operation op = Operation.Pencil;
 	
 	
 	JLabel currentToolLabel;
 	JButton pencilButton;
 	JButton bucketButton;
 	JButton logoutButton; 
 	
 	ObjectInputStream in;
 	ObjectOutputStream out;
 	ImageIcon pencilIcon;
 	ImageIcon bucketIcon;
 	PaintFrame frame;
 	public SidePanel(	ObjectInputStream in, 	ObjectOutputStream out, PaintFrame frame){
 		this.in = in;
 		this.out = out;
 		this.frame = frame;		
 		setOpaque(false);
 		loadTextures();
 		currentToolLabel = new JLabel();
 		onOperationChanged();
 		this.add(currentToolLabel);
 		
 		pencilButton = addNewButton("Paint");
 		bucketButton = addNewButton("Bucket");
 		logoutButton = addNewButton("Logout"); 
 		 
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		 
 		add(Box.createRigidArea(new Dimension(10, 10)));
 		
 	}
 	 
	private InputStream getResource(String name)
 	{
		return this.getClass().getClassLoader().getResourceAsStream(name);
 	} 
 	
 	private void loadTextures()
 	{ 
 		 try { 
 			pencilIcon = new ImageIcon(ImageIO.read(getResource("img/pencil.png"))); 
 			bucketIcon = new ImageIcon(ImageIO.read(getResource("img/bucket.png")));
 		} catch (IOException e) {
 			Logger.log(e, "while loading textures");
 		}
 	}
 	 
 	private JButton addNewButton(String name)
 	{
 		JButton btn = new JButton(name);
 		btn.addActionListener(this);
 		this.add(btn);
 		return btn;
 	}
 	
 	public void actionPerformed(ActionEvent e){
 		if(e.getSource() == logoutButton){
 			byte[] bytes = new byte[32];
 			bytes[0] = 0;
 			try{
 				out.writeObject(Constants.EXIT_TO_LOBBY);
 				frame.displayStartScreen();
 			}
 			catch(IOException ioe){
 				Logger.log(ioe, "from logout button");
 			}
 		}
 		else if(e.getSource() == pencilButton)
 		{
 			op = Operation.Pencil;
 			onOperationChanged();
 		}
 		else if(e.getSource() == bucketButton)
 		{
 			op = Operation.Bucket;
 			onOperationChanged();
 		}
 	}
 	
 	private void onOperationChanged()
 	{
 		if(op == Operation.Pencil)currentToolLabel.setIcon(pencilIcon);
 		else if(op == Operation.Bucket)currentToolLabel.setIcon(bucketIcon);
 	}
 	
 }
