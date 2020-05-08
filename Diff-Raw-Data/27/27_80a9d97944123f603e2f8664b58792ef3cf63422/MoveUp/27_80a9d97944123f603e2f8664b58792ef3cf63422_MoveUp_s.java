 package acties;
 
 import imageController.ImageModel;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 
 public class MoveUp extends AbstractAction{
 	private ImageModel imagemodel;
 	public MoveUp(ImageModel imagemodel){
 		this.imagemodel = imagemodel;
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		System.out.println("MoveUp");
 		
 	}

 }
