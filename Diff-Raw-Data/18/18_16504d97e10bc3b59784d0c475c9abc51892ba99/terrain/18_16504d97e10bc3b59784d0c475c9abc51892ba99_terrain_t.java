 package artlife;
 
 import java.awt.*;
import java.awt.image.BufferedImage;
 
 public enum terrain{
     LAND(0,40,5,10,new Color(222,184,135)),
     WATER(40,0,5,10,Color.blue),
     MOUNT(10,80,0,10,Color.gray),
     ICE(20,20,20,0,Color.white);
     private int wcost,scost,fcost,icost;
     private Color c;
    private BufferedImage img;
     terrain(int w, int s, int f, int i,Color col){
         wcost = w;
         scost = s;
         fcost = f;
         icost = i;
         c = col;
        img = new BufferedImage(50,50, BufferedImage.TYPE_4BYTE_ABGR);
         Graphics2D g = img.createGraphics();
         g.setColor(c);
         g.fillRect(0,0,img.getWidth(),img.getHeight());
     }
     public Color c(){return c;}
     public BufferedImage draw(int z) {
        return img.getSubimage(0,0,z,z);
     }
     public int cost(travel t){
         switch(t){
         case WALK:
            	return wcost;
         case SWIM:
             return scost;
     	case FLY:
             return fcost;
     	case ICE:
     		return icost;
         default:
             return 0;
         }
     }
 }
