 package acties;
 
 import imageController.ImageModel;
 import imageController.MyImage;
 
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 
 import javax.swing.AbstractAction;
 
 public class MoveDown extends AbstractAction{
 	private ImageModel imagemodel;
 	public MoveDown(ImageModel imagemodel){
 		this.imagemodel = imagemodel;
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		System.out.println("MoveDown");
 		ArrayList<MyImage> lijst = new ArrayList<MyImage>();
 		lijst = imagemodel.getLijst();
 		MyImage image = imagemodel.getCurrentMyImage();
		if(lijst.size() > 0 && lijst.indexOf(image) > lijst.size())
 			imagemodel.setSelectedImage(lijst.get(lijst.indexOf(image)+1));
		else if(lijst.size() > 0)
 			imagemodel.setSelectedImage(lijst.get(0));
 		
 	}
 
 }
