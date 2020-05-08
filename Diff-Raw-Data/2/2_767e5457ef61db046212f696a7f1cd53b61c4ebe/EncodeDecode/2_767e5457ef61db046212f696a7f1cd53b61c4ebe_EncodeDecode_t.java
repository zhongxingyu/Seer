 // CS 576 - ASG 2
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.*;
 import java.io.*;
 import javax.swing.*;
 
 import java.lang.Math;
 
 
 public class EncodeDecode
 {  
    public static void main(String[] args) 
    {
 	   	String fileName = args[0];
    		int quantLevel = Integer.parseInt(args[1]);
    		if (quantLevel < 0 || quantLevel > 7) {
    			System.out.println("Quantization level should be b/w 0 & 7");
    			System.exit(0);
    		}
    		int deliveryMode = Integer.parseInt(args[2]);
    		if (deliveryMode < 1 || deliveryMode > 3) {
    			System.exit(0);
    		}
    		int latency = Integer.parseInt(args[3]);
 
    		//String fileName = "../image1.rgb";
    		
    		EncodeDecode ir = new EncodeDecode(quantLevel, deliveryMode, latency, fileName);
    		ir.displayImages();
    		System.out.println("1st bit in block: " + RBlocks[0].bytes[0]);
    		ir.calculateDCTsPerBlock();
    		System.out.println("1st DCT: " +RBlocks[0].dct[0][0]);
    		ir.quantizePerBlock();
    		System.out.println("1st qunt: " + RBlocks[0].quantizations[0][0]);
 //   		ir.dequantizPerBlock();
 //  		System.out.println("1st dequant'd DCT: " +RBlocks[0].dct[0][0]);
    		//ir.displayImages();
    		if (deliveryMode == 1) {
    	   		try {
    				ir.DecodeSequential();
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		} else if (deliveryMode == 2) {
    			try {
 				ir.DecodeProgressiveSpectral();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
    		} else if (deliveryMode == 3) {
    			try {
 				ir.DecodeProgessiveSequential();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
    		} else {
    			System.out.println("Not a mode!");
    			//System.exit(0);
    		}
    		System.out.println("comressed: " + RBlocks[0].bytes[0]);
 
    }
    
    
    // 8x8 block 
    class Block8x8 {
 	   byte[] bytes = new byte[64]; // pixels
 	   double[][] dct = new double[8][8]; // dct coefficients
 	   byte[][] quantizations = new byte[8][8]; // quantizations
 	   
 	   // calculate DCTs for this block
 	   public void calculateDCTs() {
 		   double c_u;
 		   double c_v;
 		   // for each frequency (u, v)
 		   for (int u = 0; u < 8; ++u) {
 			   c_u = (u == 0) ? (1/Math.sqrt(2)) : 1; 
 			   for (int v = 0; v < 8; ++v) {
 				   c_v = (v == 0) ? (1/Math.sqrt(2)) : 1;
 				   
 				   // sum with all f's
 				   double fsums = 0;
 				   for (int i = 0; i < 64; ++i) {
 					   int y = i / 8;
 					   int x = i - (8 * y);
 					   // convert bytes?
 					   //int f_xy = 0x00000000 | bytes[i]; 
 					   int f_xy = bytes[i];
 					   fsums += (double) f_xy * Math.cos( ((2.0*(double)x + 1.0) * (double)u * Math.PI) / 16.0 )
 							   	* Math.cos( ((2.0*(double)y + 1.0) * (double)v * Math.PI) / 16.0);
 				   }
 				   dct[u][v] = ((1.0 / 4.0) * c_u * c_v) * fsums; 
 			   }
 		   }
 	   }
 	   
 	   // quantize dct values
 	   public void Quantize(int n) {
 		   for (int u = 0; u < 8; ++u) {
 			   for (int v = 0; v < 8; ++v) {
 				   quantizations[u][v] = (byte) Math.round(dct[u][v] / Math.pow(2, n));
 			   }
 		   }
 	   }
 	   
 	   // Dequantize data
 	   public void Dequantize(int n) {
 		   for (int u = 0; u < 8; ++u) {
 			   for (int v = 0; v < 8; ++v) {
 				   dct[u][v] = (double) quantizations[u][v] * Math.pow(2.0, (double) n) ;
 			   }
 		   }
 	   }
 	   
 	   // decode sequential mode, just the block
 	   public void InverseDCTSequential() {
 		   double c_u;
 		   double c_v;
 		   for (int i = 0; i < 64; ++i) {
 			   int y = i/8;
 			   int x = i - (8 * y);
 			   double summed = 0;
 			   for (int u = 0; u < 8; ++u) {
 				   c_u = (u == 0) ? (1/Math.sqrt(2)) : 1; 
 				   for (int v = 0; v < 8; ++v) {
 					   c_v = (v == 0) ? (1/Math.sqrt(2)) : 1;
 					   summed += c_u * c_v * dct[u][v] 
 							   * Math.cos( ((2.0*(double)x+1.0)*(double)u*Math.PI) / 16.0 ) 
 							   	* Math.cos( ((2.0*(double)y+1.0)*(double)v*Math.PI) / 16.0);
 				   }
 			   }
 			   bytes[i] = (byte) (1.0/4.0 * summed);
 		   }
 	   }
 	   
 	   // decode progressive mode with spectral selection
 	   public void InverseDCTSpectral(int ac_count) {
 		   double c_u;
 		   double c_v;
 		   for (int i = 0; i < 64; ++i) { // each pixel in block
 			   int y = i / 8;
 			   int x = i - (8 * y);
 			   double summed = 0;
 			   int ac = 0;
 			   for (int u = 0; u < 8; ++u) {
 				   c_u = (u == 0) ? (1/Math.sqrt(2)) : 1; 
 				   for (int v = 0; v < 8; ++v) {
 					   c_v = (v == 0) ? (1/Math.sqrt(2)) : 1;
 					   summed += c_u * c_v * dct[u][v] 
 							   * Math.cos( ((2.0*(double)x+1.0)*(double)u*Math.PI) / 16.0 ) 
 							   	* Math.cos( ((2.0*(double)y+1.0)*(double)v*Math.PI) / 16.0);
 					   
 					   ++ac;
 					   if (ac > ac_count) {
 						   break;
 					   }
 				   }
 				   if (ac > ac_count) {
 					   break;
 				   }  
 			   }
 			   bytes[i] = (byte) (1.0/4.0 * summed);
 		   }
 	   }
 	   
 	   // decode block progressive mode with successive bit approximation
 	   public void InverseDCTSuccBit(int bit) {
 		   double c_u;
 		   double c_v;
 		   for (int i = 0 ; i < 64; ++i) { // each pixel in the block
 			   int y = i / 8;
 			   int x = i - (8 * y);
 			   double summed = 0;
 			   for (int u = 0; u < 8; ++u) {
 				   c_u = (u == 0) ? (1/Math.sqrt(2)) : 1; 
 				   for (int v = 0; v < 8; ++v) {
 					   c_v = (v == 0) ? (1/Math.sqrt(2)) : 1;
 					   int sigBitstemp = (int) dct[u][v];
 					   int sigBits =  sigBitstemp >> bit; // gets bits
 //					   int sigBits = (int) dct[u][v];
 //					   int mask = 0x8000;
 //					   for (int m = 1; m < bit; ++m) {
 //						   sigBits = sigBits & mask;
 //						   int oldmask = mask;
 //						   mask = mask >>> 1;
 //				   		   mask = mask | oldmask;
 //					   }
 					   summed += c_u * c_v * (double) sigBits
 							   * Math.cos( ((2.0*(double)x+1.0)*(double)u*Math.PI) / 16.0 ) 
 							   	* Math.cos( ((2.0*(double)y+1.0)*(double)v*Math.PI) / 16.0);
 				   }
 			   }
 			   bytes[i] = (byte) (1.0/4.0 * summed);
 		   }
 		   
 	   }
 	   
    } // end Block8x8 class
    
    
    // fields
    public static int width = 352; // width of image
    public static int height = 288; // height of image
    public static int quantizationLevel; // 0 - 7
    public static int  deliveryMode; // 1 || 2 || 3
    public static int latency; // in milliseconds
    public static Block8x8[] RBlocks; // blocks for R component
    public static Block8x8[] GBlocks; // blocks for G component
    public static Block8x8[] BBlocks; // blocks for B component
    public static BufferedImage img; //original image
    public static JLabel label2; // label that displays compressed image
    public static BufferedImage jpeg; // jpeg'd image
    
    public EncodeDecode(int quant, int mode, int lat, String fileName)
    {
 	
 	   quantizationLevel = quant;
 	   deliveryMode = mode;
 	   latency = lat;
 	   
 	    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 	
 	    //Reading File
 	    try {
 		    File file = new File(fileName);
 		    InputStream is = new FileInputStream(file);
 	
 		    long len = file.length();
 		    byte[] bytes = new byte[(int)len];
 		    
 		    
 		    int offset = 0;
 	        int numRead = 0;
 	        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
 	            offset += numRead;
 	        }
 	    
 	    	// read all the bytes
 	    	int ind = 0;
 			for(int y = 0; y < height; y++){
 		
 				for(int x = 0; x < width; x++){
 			 
 					//byte a = 0;
 					byte r = bytes[ind];
 					byte g = bytes[ind+height*width];
 					byte b = bytes[ind+height*width*2]; 
 					
 					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
 					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
 					img.setRGB(x,y,pix);
 					ind++;
 				}
 			}
 			
 			divideIntoBlocks(bytes);
 			
 			System.out.println("image divided");
 			
 	    } catch (FileNotFoundException e) {
 	      e.printStackTrace();
 	    } catch (IOException e) {
 	      e.printStackTrace();
 	    }
 	    	
    }
    public void displayImages() {
 	    // Use a label to display the image
 	    JFrame frame = new JFrame();
 	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    // Show original image
 	    JLabel label = new JLabel(new ImageIcon(img));
 	    label.setPreferredSize(new Dimension(width,height));
 	    frame.getContentPane().add(label, BorderLayout.WEST);
 	    
 	    
 	    // place holder for 2nd image
 	    
 	    // blank image
 	    jpeg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 	    for (int y = 0; y < height; ++y) {
 	    	for (int x = 0; x < width; ++x) {
 	    		jpeg.setRGB(x, y, 0x00FFFFFF);
 	    	}
 	    }
 	    
 	    label2 = new JLabel(new ImageIcon(jpeg));
 	    label2.setPreferredSize(new Dimension(width, height));
 	    frame.getContentPane().add(label2, BorderLayout.EAST);
 
 	    // Bottons
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setPreferredSize(new Dimension(width, 50));
 	    frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
 		
 		
 		MyButton closeButton = new MyButton("Close");
 		buttonPanel.add(closeButton, BorderLayout.WEST);	
 		
 	    frame.pack();
 	    frame.setVisible(true); 
    }
    
    // divide each component (RGB) into 8x8 blocks
    public void divideIntoBlocks(byte bytes[]) {  
 	   int ind; // track of the index in the block arrays
 	   int Ri, Gi, Bi; // keep track of compoent in byte array
 	   
 	   // make blocks
 	   RBlocks = new Block8x8[(height/8) * (width/8)];
 	   GBlocks = new Block8x8[(height/8)*(width/8)];
 	   BBlocks = new Block8x8[(height/8)*(width/8)];
 	   for (int i = 0; i < (height/8)*(width/8); ++i) {
 		   RBlocks[i] = new Block8x8();
 		   GBlocks[i] = new Block8x8();
 		   BBlocks[i] = new Block8x8();
 	   }
 	   
 	   // starting locations in byte array
 	   Ri = 0; 
 	   Gi = 0 + width * height;
 	   Bi = 0 + width * height * 2;
 	   ind = 0;
 	   
 	   int cornerR = 0; // saves upper left corner of a block
 	   int cornerG = 0;
 	   int cornerB = 0;
 	   
 	   // for each row of 8x8 blocks
 	   for (int i = 0; i < height; i = i + 8) {
 		   
 		   // for each block in a row
 		   for (int j = 0; j < width; j = j + 8) {
 			   // save block's upper left corner
 			   cornerR = Ri;
 			   cornerG = Gi;
 			   cornerB = Bi;
 			   
 			   int b = 0;
 			   // for each row of bytes in the block
 			   for (int k = 0; k < 8; ++k) {
 				   
 				   // for each byte in row in block
 				   for (int l = 0; l < 8; ++l) {
 					   //System.out.println(Ri + " " + Gi + " " + Bi);
 					   RBlocks[ind].bytes[b] = bytes[Ri];
 					   GBlocks[ind].bytes[b] = bytes[Gi];
 					   BBlocks[ind].bytes[b] = bytes[Bi];
 					   ++b; ++Ri; ++Gi; ++Bi;
 				   }
 				   
 				   //set Ri, Gi, Bi to first byte in next row in block
 				   Ri = Ri - 8 + width;
 				   Gi = Gi - 8 + width;
 				   Bi = Bi - 8 + width;
 			   }
 			   ++ind; // new block!
 			   // set Ri, Gi, Bi to upper left of next block in the row
 			   Ri = cornerR + 8;
 			   Gi = cornerG + 8;
 			   Bi = cornerB + 8;
 		   }
 		   // Set Ri, Gi, Bi to next row's first block's upper left
 		   Ri = cornerR - (width - 8) + (8 * width);
 		   Gi = cornerG - (width - 8) + (8 * width); 
 		   Bi = cornerB - (width - 8) + (8 * width);
 	   }
    }
    
    public void calculateDCTsPerBlock() {
 	   for (int i = 0; i < RBlocks.length; ++i) {
 		   RBlocks[i].calculateDCTs();
 		   GBlocks[i].calculateDCTs();
 		   BBlocks[i].calculateDCTs();
 	   }
    }
    
    public void quantizePerBlock() {
 	   for (int i = 0; i < RBlocks.length; ++i) {
 		   RBlocks[i].Quantize(quantizationLevel);
 		   GBlocks[i].Quantize(quantizationLevel);
 		   BBlocks[i].Quantize(quantizationLevel);
 	   }
    }
    
    public void dequantizPerBlock() {
 	   for (int i = 0; i < RBlocks.length; ++i) {
 		   RBlocks[i].Dequantize(quantizationLevel);
 		   GBlocks[i].Dequantize(quantizationLevel);
 		   BBlocks[i].Dequantize(quantizationLevel);
 	   }
    }
    
    // sequential decoding - each block one by one 
    public void DecodeSequential() throws InterruptedException {
 	   int corner_x = 0; // upper left corner of this block
 	   int corner_y = 0; // upper left corner of this block
 	   int ind = 0; // index of block in components
 	  
 	   while (corner_y < height) {
 		   // for each block
 		   
 		   // decode the block
 		   RBlocks[ind].Dequantize(quantizationLevel);
 		   RBlocks[ind].InverseDCTSequential();
 		   GBlocks[ind].Dequantize(quantizationLevel);
 		   GBlocks[ind].InverseDCTSequential();
 		   BBlocks[ind].Dequantize(quantizationLevel);
 		   BBlocks[ind].InverseDCTSequential();
 		   
 		   // copy values into BufferedImage
 		   int bi = 0;
 		  // System.out.println("corner: " + corner_x + " " + corner_y);
 		   for (int y = corner_y; y < corner_y + 8; ++y) {
 			   for (int x = corner_x; x < corner_x + 8; ++x) {
 				   byte r = RBlocks[ind].bytes[bi];
 				   byte g = GBlocks[ind].bytes[bi];
 				   byte b = BBlocks[ind].bytes[bi];
 				   int rgb = 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
 			//	   System.out.println(x + " " + y );
 				   jpeg.setRGB(x, y, rgb);
 				   ++bi;
 			   }
 		   }
 		   
 		   // Redraw Image
 		   label2.removeAll();
 		   label2.setIcon(new ImageIcon(jpeg));
 		   label2.setPreferredSize(new Dimension(width, height));
 		   label2.revalidate();
 		   label2.repaint();
 		   
 		   // wait for latency milliseconds
 		   Thread.sleep(latency);
 		   
 		   // update values
 		   ++ind;
 		   corner_x += 8;
 		   if (corner_x >= width) {
 			   corner_x = 0;
 			   corner_y += 8;
 		   }
 	   }
 	   
    }
    // Progressive Mode - Spectral Selection Decoding
    // Decode all blocks using only DC, then DC, AC....
    public void DecodeProgressiveSpectral() throws InterruptedException {
 	   for (int ac = 0; ac < 64; ++ac) { // keep decoding with the next AC coeff
 		   
 		   int corner_x = 0; // corner
 		   int corner_y = 0;
 		   // for all blocks
 		   for (int i = 0; i < RBlocks.length; ++i) {
 			   // decode the block
 			   RBlocks[i].Dequantize(quantizationLevel);
 			   RBlocks[i].InverseDCTSpectral(ac);
 			   GBlocks[i].Dequantize(quantizationLevel);
 			   GBlocks[i].InverseDCTSpectral(ac);
 			   BBlocks[i].Dequantize(quantizationLevel);
 			   BBlocks[i].InverseDCTSpectral(ac);
 		   
 			   // copy block into buffered image
 			   int bi = 0;
 			   for (int y = corner_y; y < corner_y + 8; ++y) {
 				   for (int x = corner_x; x < corner_x + 8; ++x) {
 					   byte r = RBlocks[i].bytes[bi];
 					   byte g = GBlocks[i].bytes[bi];
 					   byte b = BBlocks[i].bytes[bi];
 					   int rgb = 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
 					   jpeg.setRGB(x, y, rgb);
 					   ++bi;
 				   }
 			   }
 			   
 			   // update block's corner in the Buffered Image
 			   corner_x += 8;
 			   if (corner_x >= width) {
 				   corner_x = 0;
 				   corner_y += 8;
 			   }
 		   }
 		   
 		   // refresh image
 		   label2.removeAll();
 		   label2.setIcon(new ImageIcon(jpeg));
 		   label2.setPreferredSize(new Dimension(width, height));
 		   label2.revalidate();
 		   label2.repaint();
 		   
 		   // Sleep
 		   Thread.sleep(latency);
 	   }
    }
    
    // Progressive Mode - Sequential Bit Approx
    // decode all blocks with increasing significant bits
    public void DecodeProgessiveSequential() throws InterruptedException {
	   for (int bit = 15; bit >= 0; --bit) {
 		   int corner_x = 0;
 		   int corner_y = 0;
 		   // for all block
 		   for (int i = 0; i < RBlocks.length; ++i) {
 			   // decode block
 			   RBlocks[i].Dequantize(quantizationLevel);
 			   RBlocks[i].InverseDCTSuccBit(bit);
 			   GBlocks[i].Dequantize(quantizationLevel);
 			   GBlocks[i].InverseDCTSuccBit(bit);
 			   BBlocks[i].Dequantize(quantizationLevel);
 			   BBlocks[i].InverseDCTSuccBit(bit);
 			   
 			   // Copy block into Buffered Image
 			   int bi = 0;
 			   for (int y = corner_y; y < corner_y + 8; ++y) {
 				   for (int x = corner_x; x < corner_x + 8; ++x) {
 					   byte r = RBlocks[i].bytes[bi];
 					   byte g = GBlocks[i].bytes[bi];
 					   byte b = BBlocks[i].bytes[bi];
 					   int rgb = 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
 					   jpeg.setRGB(x, y, rgb);
 					   ++bi;
 				   }
 			   }
 			   
 			   // update block's corner in the Buffered Image
 			   corner_x += 8;
 			   if (corner_x >= width) {
 				   corner_x = 0;
 				   corner_y += 8;
 			   }
 		   }
 		   
 		   // refresh image
 		   label2.removeAll();
 		   label2.setIcon(new ImageIcon(jpeg));
 		   label2.setPreferredSize(new Dimension(width, height));
 		   label2.revalidate();
 		   label2.repaint();
 		   System.out.println("the " + bit + "significant bits");
 		   
 		   // sleep
 		   Thread.sleep(latency);	   
 	   }
    }
    
    // Function calls
 	public void buttonPressed(String name)
 	{
 		if (name.equals("Close"))
 		{
 			//System.out.println("Close");
 			System.exit(0);
 		}
 	}
 
 
 	
 	class MyButton extends JButton {
 		MyButton(String label){
 			setFont(new Font("Helvetica", Font.BOLD, 10));
 			setText(label);
 			addMouseListener(
 				new MouseAdapter() {
 	  				public void mousePressed(MouseEvent e) 
 	  				{
 						buttonPressed(getText());
 					}
 				}
 			);
 		}
 		
 		MyButton(String label, ImageIcon icon){
 			Image img = icon.getImage();
 			Image scaleimg = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
 			setIcon(new ImageIcon(scaleimg));
 			setText(label);
 			setFont(new Font("Helvetica", Font.PLAIN, 0));
 			addMouseListener(
 				new MouseAdapter() {
 	  				public void mousePressed(MouseEvent e) {
 						buttonPressed(getText());
 					}
 				}
 			);
 		}
 	}
 }
