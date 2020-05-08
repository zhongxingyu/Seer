 package nitrogene.util;
 
 import java.awt.FontFormatException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import nitrogene.core.AssetManager;
 import nitrogene.core.GlobalInformation;
 import nitrogene.core.HangarState;
 import nitrogene.weapon.EnumWeapon;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 
 
 public class BuyButton extends Button
 {
 	private int cost;
	private String finalrender = "normalimage";
 	private boolean selected;
 
 	public BuyButton(String name, int cost, float x, float y, float width, float height,Sound mouseover) throws FontFormatException, IOException,
 			SlickException {
 		super(name, x, y, width, height,mouseover);
 		this.cost = cost;
 		selected = false;
 		this.fontsize = 25f;
 	}
 
 	@Override
 	public void update (GameContainer gc, ArrayList<EnumWeapon> weapons, EnumWeapon enumwep)
      {
 		if(this.font==null){
     		try {
 				font = GlobalInformation.getMenuFont(fontsize);
 			} catch (SlickException e) {
 				e.printStackTrace();
 			}
     	}
 		
         buttonReleased = false;
         
         if (button.contains(gc.getInput().getMouseX(), gc.getInput().getMouseY()))
         {
         	
             if (gc.getInput().isMousePressed(Input.MOUSE_LEFT_BUTTON)){
             	if(finalrender == "pressedimage"){
             		HangarState.changeMoney(cost * 1);
             		finalrender = "normalimage";
             		weapons.remove(enumwep);
             		buttonDown = false;
             		return;
            	} else if(finalrender == "normalimage"){
             		if(HangarState.getMoney() - cost < 0){
             			return;
             		}
             		finalrender = "pressedimage";
             		HangarState.changeMoney(cost * -1);
             		weapons.add(enumwep);
             		buttonDown = true;
             	}
             }
         }
     }
 	
 	@Override
 	public void render(Graphics g, int scalefactor, Image normalImage, Image pressedImage){
 		Image temp = null;
 		if(finalrender == "pressedimage" && pressedImage != null){
 			temp = pressedImage;
 		} else{
 			temp = normalImage;
 		} 
 		temp.setFilter(Image.FILTER_NEAREST);
         temp.draw(position.x,position.y,width,height);
 
         g.setFont(font);
         
         //draw the name of the item
         g.drawString(text, button.getMinX() + (9*scalefactor), button.getMinY() + (1*scalefactor));
         
         float margin = Button.getTextWidth(String.valueOf(cost), font);
         g.drawString(String.valueOf(cost), button.getMinX()+ (100*scalefactor) - margin - (12*scalefactor), button.getMinY() + (1*scalefactor));
         Image coin = (Image)AssetManager.get().get("coins");
         coin.draw(button.getMinX() + (100*scalefactor) - (11.5f*scalefactor), button.getMinY() + (2*scalefactor), scalefactor/4);
 	}
 
 }
