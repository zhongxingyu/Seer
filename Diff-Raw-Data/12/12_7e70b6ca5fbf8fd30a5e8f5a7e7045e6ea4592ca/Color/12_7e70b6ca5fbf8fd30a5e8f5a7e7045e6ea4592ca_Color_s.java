 package com.colorextractor;
 
 import javax.media.jai.JAI;
 import javax.media.jai.PlanarImage;
 
 public class Color { 
   
 	PlanarImage input;
 	Float []vec;
 	int nbbins;
 	int nb;
 	int width, height;
 	
 	public Color (String name, int nbbins) {
 		nb = (int)Math.pow((float)nbbins, (float)1/3);
 		//System.out.println("nbbins="+nbbins+ " nb="+nb);
 		// Read the image.
 		input = JAI.create("fileload", name);
 		width = input.getWidth();
 		height = input.getHeight();
 		
 		vec = new Float[nbbins];
 		this.nbbins = nbbins;
 		process();
 	}
 		
 	public String getString() {
 		String str = "";
 		for (int c=0; c<nbbins; c++) {
 			str += (" "+(float)vec[c]);
 		}
		return str;
 	}
 
 	private void process() {
 		int pb, pg, pr;
 		int xb, xg, xr, x;
 		
 		xr = xg = xb = -1;
 		for (int i=0; i<nbbins; i++) vec[i] = 0.0F;
 			
 		for (int i=0; i<width; i++) {
 			for (int j=0; j<height; j++) {
 				pr = (int)(input.getData().getSample(i, j, 0)+1);
 				pg = (int)(input.getData().getSample(i, j, 1)+1);
 				pb = (int)(input.getData().getSample(i, j, 2)+1);
 					
 				for (int b=0; b<nb; b++) 
 					if (pb >= (float)b*256.0F/(float)nb && pb <= (float)(b+1)*256.0F/(float)nb) {xb = b;break;}
 				for (int g=0; g<nb; g++)
 					if (pg >= (float)g*256.0F/(float)nb && pg <= (float)(g+1)*256.0F/(float)nb) {xg = g;break;}
 				for (int r=0; r<nb; r++)
 					if (pr >= (float)r*256.0F/(float)nb && pr <= (float)(r+1)*256.0F/(float)nb) {xr = r;break;}
 					
 				x = nb*nb*xb + nb*xg + xr;
 				vec[x] += 1.0F;
 			}
 		}
 		
 		for (int i=0; i<nbbins; i++) vec[i] = vec[i]/(width*height);
 	}
 		
 		
 	
 	public static void main(String[] args) {
 		Color h = new Color(System.getProperty("user.dir")+"/uploads/" + args[0], 64);
 		System.out.println(h.getString());
 	}
 
 }
 
