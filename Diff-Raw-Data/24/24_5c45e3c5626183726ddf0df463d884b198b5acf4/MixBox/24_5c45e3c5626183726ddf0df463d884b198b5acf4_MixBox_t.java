 package mmb.foss.aueb.icong.boxes;
 
 import mmb.foss.aueb.icong.R;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 
 public class MixBox extends Box {
 
 	public MixBox(Context context) {
 		// TODO Auto-generated constructor stub
 		super(context, R.drawable.mix);
 		buttonX = new int[4][2];
 		buttonY = new int[4][2];
 		buttonPressed = new boolean[4];
 		buttonX[0][0] = 5;
 		buttonX[0][1] = 26;
 		buttonX[1][0] = 5;
 		buttonX[1][1] = 26;
 		buttonX[2][0] = 5;
 		buttonX[2][1] = 26;
 		buttonX[3][0] = 95;
 		buttonX[3][1] = 116;
 		buttonY[0][0] = 5;
 		buttonY[0][1] = 27;
 		buttonY[1][0] = 31;
 		buttonY[1][1] = 52;
 		buttonY[2][0] = 58;
 		buttonY[2][1] = 79;
 		buttonY[3][0] = 31;
 		buttonY[3][1] = 52;
 		this.setNoOfInputs(3);
 		this.setNoOfOutpus(1);
 	}
 
 	@Override
 	public void function() {
 		
 		if(this.getOutput(0) != null)
 			return;
 		
 		Bitmap src1 = null;
 		Bitmap src2 = null;
		double bias = 127.5;
 		
 		if (this.getInput(0) == null || this.getInput(1) == null) {
 			return ;
 		} else {
 			
 			src1 = (Bitmap) this.getInput(0);
 			src2 = (Bitmap) this.getInput(1);
 			
 			if(this.getInput(2) != null)
 				bias = (Double) this.getInput(2);
 			
			if(bias>255)
 				return;
 		}
 		
		bias = bias/255.0;
		
 		int width = src1.getWidth();
 		int height = src1.getHeight();
 		
 		Bitmap out = Bitmap.createBitmap(width, height, src1.getConfig());
 		int pix1, pix2, A1, A2, Af, R1, G1, B1, R2, G2, B2, Rf, Gf, Bf;
 
 		for(int x=0; x<width; x++) 
 		{
 			for(int y=0; y<height; y++)
 			{
 				pix1 = src1.getPixel(x, y);
 				pix2 = src2.getPixel(x, y);
 				
 				A1 = Color.alpha(pix1);
 				A2 = Color.alpha(pix2);
 				
 				R1 = Color.red(pix1);
 				R2 = Color.red(pix2);
 				
 				G1 = Color.green(pix1);
 				G2 = Color.green(pix2);
 				
 				B1 = Color.blue(pix1);
 				B2 = Color.blue(pix2);
 				
 				Af = (int)(bias*A1 + (1-bias)*A2);
 				Rf = (int)(bias*R1 + (1-bias)*R2);
 				Gf = (int)(bias*G1 + (1-bias)*G2);
 				Bf = (int)(bias*B1 + (1-bias)*B2);
 				
 				out.setPixel(x, y, Color.argb(Af, Rf, Gf, Bf));
 			}
 		}
 		this.setOutput(out, 0);
 	}
 
 	@Override
 	public void showDialog(Context context) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
