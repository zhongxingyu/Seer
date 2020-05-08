 package rebecca.jpeg;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class MCU {
 	
 	private static int components, units, current;
 	private static int[] dcHuffman, acHuffman, id, times, lastdc;
 	
 	public static int[] vComp, hComp;
 	
 	private Matrix[][] mat;
 	private Matrix[][] ycbcr;
 
 	
 	public MCU(BitStream s, HuffmanTables t, Component[] c, ArrayList<QTable> q) throws IOException {
 		
 		mat = new Matrix[components][];
 		ycbcr = new Matrix[components][];
 		
 		for(int i = 0; i < components; i++) {
 			mat[i] = new Matrix[times[i]];
 			ycbcr[i] = new Matrix[times[i]];
 			for(int j = 0; j < times[i]; j++)
 			{
 				QTable qtab = q.get(c[i].qTable);
 				
 				System.out.println(s.count+"");
 				mat[i][j] = readMatrix(s, t, i);
 				
 				System.out.println(mat[i][j]);
 				
 				ycbcr[i][j] = mat[i][j].inverse(qtab);
 			}
 			
 		}
 	
 		
 	}
 	
 	public static void setup(BitStream s, Component[] c) throws IOException {
 		components = s.read();
 		
 		dcHuffman = new int[components];
 		acHuffman = new int[components];
 		id = new int[components];
 		times = new int[components];
 		lastdc = new int[components];
 				
 		for(int i = 0; i < components; i++) {
 			lastdc[i] = 0;
 			id[i] = s.read();
 			int tmp = s.read();
 			dcHuffman[i] = (0xf0 & tmp)>>4;
 			acHuffman[i] = (0x0f & tmp);
 			
 			times[i] = hComp[i]*vComp[i];
 		}
 	}
 	
 	public static void setNumberOfComponents(int n) {
 		vComp = new int[n];
 		hComp = new int[n];
 	}
 	
 	public Matrix readMatrix(BitStream s, HuffmanTables t, int comp) throws IOException {
 		// int[] data = new int[64];
 		int tmp = t.decodeDc(dcHuffman[comp], s);
 		int value;
 		System.out.println("TMP: " + tmp);
 		
 		Matrix r = new Matrix();
 		r.reset();
 
 		value = calcValue(tmp, s);
 
 		value += lastdc[comp];
 		lastdc[comp] = value;
 		
 		r.setNext(value);
 		r.next();
 		
 		int count = 1;
 		while (count < 64) {
 			int start = 0;
 			tmp = t.decodeAc(dcHuffman[comp], s);
 			switch (tmp) {
 			case 0x00:
 				while(count < 64) {
 					r.setNext(0);
 					r.next();
 					count++;
 				}
 				break;
 			case 0x0f:
 				System.exit(0);
 				break;
 			case 0xf0:
 				for(int i=0; i<16 && count < 64; i++) {
 					r.setNext(0);
 					
 					r.next();
 					count++;
 				}
 				break;
 			default:
				System.out.println("TT" + tmp);
 				for(int i = 0; i < (tmp & 0xf0)>>4; i++) {
					//System.out.println("0");
 					//System.out.println(r.getX()+ "x" + r.getY());
 					count++;
 					r.setNext(0);
 					r.next();
 				}
 				
 				value = calcValue((tmp & 0x0f), s);
				System.out.println("value" + value);
 				//System.out.println(r.getX()+ "x" + r.getY());
 				r.setNext(value);
 				r.next();
 				break;
 			}
 			
 			
			count++;
 		}
 		return r;
 	}
 	
 	private static int calcValue(int count, BitStream s) throws IOException {
 		
 		if(count == 0) return 0;
 		
 		int start;
 		int value = 0;
 		if(s.getBit() == 0)
 			start = -1*(1<<(count))+1;
 		else
 			start = 1*(1<<(count-1));
 		
 		for(int i = 1; i<count; i++)
 			value = value*2 + s.getBit();
 		
 		return start+value;
 	}
 	
 	public void paint() {
 		for(int i=0; i<16; i++)
 			for(int j=0; j<16;j++);
 				//paint(i,j);
 	}
 	
 	public void paint(int offsetx, int offsety,Graphics g) {
 		for(int i=0; i<16; i++)
 			for(int j=0; j<16;j++)
 				paint(offsetx, offsety,i,j, g);
 	}
 	
 	public void paint(int offsetx, int offsety, int x, int y, Graphics g) {
 		int maxV=0, maxH=0;
 		for(int i=0; i <3; i++) {
 			maxV = Math.max(maxV, vComp[i]);
 			maxH = Math.max(maxH, hComp[i]);
 		}
 		
 		int ymat = hComp[0]*(y/(maxV*8/vComp[0]))+x/(maxH*8/hComp[0]);
 		int yx = x/(maxH/hComp[0])%8;
 		int yy = y/(maxV/vComp[0])%8;
 		
 		int cbmat = hComp[1]*(y/(maxV*8/vComp[1]))+x/(maxH*8/hComp[1]);
 		int cbx = x/(maxH/hComp[1])%8;
 		int cby = y/(maxV/vComp[1])%8;
 		
 		int crmat = hComp[2]*(y/(maxV*8/vComp[2]))+x/(maxH*8/hComp[2]);
 		int crx = x/(maxH/hComp[2])%8;
 		int cry = y/(maxV/vComp[2])%8;
 		
 		double yv = ycbcr[0][ymat].getValue(yy, yx);
 		double cbv = ycbcr[1][cbmat].getValue(cby, cbx);
 		double crv = ycbcr[2][crmat].getValue(cry, crx);
 		
 		//System.out.println(yv+" " + cbv + " " + crv);
 		
 		double red = yv + crv*1.402;
 		double blue = cbv * (2-2*0.114) + yv;
 		double green = (yv-0.114*blue-0.229*red)/0.587;
 		
 		red += 128;
 		green += 128;
 		blue += 128;
 		red = Math.max(0,Math.min(255,red));
 		green = Math.max(0,Math.min(255,green));
 		blue = Math.max(0,Math.min(255,blue));
 		//System.out.println(red + " " + green + " " + blue);
 		g.setColor(new Color((int)red, (int)green,(int)blue) );
 		g.fillRect(x+offsetx, y+offsety, 1, 1);
 		
 	}
 	
 	
 	
 
 }
