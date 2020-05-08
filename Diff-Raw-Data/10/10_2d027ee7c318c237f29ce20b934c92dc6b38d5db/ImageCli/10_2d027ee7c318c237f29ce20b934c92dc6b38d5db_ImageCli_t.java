 /**
  * 
  */
 package test;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.image.Raster;
 import java.awt.image.WritableRaster;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import graphinfo.Node;
 import graphsearch.CFEuclideanColourRast;
 import graphsearch.CFPctconWidth;
 import graphsearch.CFWColSlopePct;
 import graphsearch.CostFunction;
 import graphsearch.GreedySearch;
 import graphsearch.NaiveSearcher;
 import graphtoolkit.*;
 
 /**
  * @author 100174454
  *
  */
 public class ImageCli {
 	
 	static ArrayList<Node> regression;
 	static float dx,dy,x0,y0;
 	static long startt,endt;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if(args.length < 3)
 			usage();
 		try{
 			startt = System.currentTimeMillis();
 			WritableRaster wr,wrbw,cwr;
 			Raster image = ImageFile.loadImage(args[0]);
 			cwr = image.createCompatibleWritableRaster();
 			
 			int p[] = new int[4];
 			
 			for(int i=0;i<image.getHeight();i++){
 				for(int j=0; j < image.getWidth();j++){
 					cwr.setPixel(j, i, image.getPixel(j, i, p));
 				}
 			}
 			
 			//ImageVisualization iv = new ImageVisualization(cwr);
 			
 			int x,y, iArray[];
 			iArray = new int[4];
 			x = Integer.parseInt(args[1]);
 			y = Integer.parseInt(args[2]);
 			int tol = Integer.parseInt(args[3]);
 			
 			//if(args[1].compareTo("bw") == 0)
 			wrbw = ImageFilter.isolateColour(image, image.getPixel(x, y, iArray), tol);
 			
 			/*
 			ImageFile.writeImage("x" + x + "-y" + y + "-tol" + tol +
 					"-r" + iArray[0] + "g" + iArray[1]
 							+ "b" + iArray[2] + ".png", wrbw);
 			*/
 			wr = ImageFilter.convertToGrayScale(wrbw);
 			wrbw = ImageFilter.convertToBW(wr, wr.getPixel(x, y, iArray)[0] + tol);
 			/*
 			ImageFile.writeImage("x" + x + "-y" + y + "-tol" + tol +
 					"-r" + iArray[0] + "g" + iArray[1]
 							+ "b" + iArray[2] + "-bw.png", wrbw);
 			*/
 			//ImageVisualization ivbw = new ImageVisualization(wrbw);
 			
 			Dimension d = PointTools.findOptimalKernel(wrbw, new Point(x,y),false);
 			
 			
 			
 			/**
 			 * Use the parameterized constructor for visualization
 			 */
 			//AdaptiveCrawler aC = new AdaptiveCrawler(cwr);
 			AdaptiveCrawler aC = new AdaptiveCrawler();
 			
 			//Vector<Point> points = aC.crawl(wrbw, new Point(x,y));
 			
 			Vector<Node> points = aC.newSmartCrawl(wrbw, new Point(x,y), image);
 			
 			Node n;
 			
 			
 			//CostFunction CF = new CFEuclideanColourRast((float) wrbw.getWidth());
 			
 			CostFunction CF = new CFPctconWidth();
 			
 			GreedySearch graphSearch = new GreedySearch(CF);
 			
 			ArrayList<Integer> line = graphSearch.search(points);
 			
 			endt = System.currentTimeMillis();
 			
 			System.out.println("Line regression took " + (endt - startt) + "ms\n");
 			
 			ImageVisualization finalIV;
 			finalIV = new ImageVisualization(wrbw,cwr.getWidth() + 30,0);
 			
 			n = points.get(line.get(0));
 			
 			regression = new ArrayList<Node>();
 			regression.add(n);
 
 			
 			int tmp_x0, tmp_y0;
 			for(int i=1; i < line.size(); i++){
 				tmp_x0 = n.p.x;
 				tmp_y0 = n.p.y;
 				n = points.get(line.get(i));
 				regression.add(n); // Add to final line
 				if(finalIV != null)
 					finalIV.drawLine(tmp_x0, tmp_y0, n.p.x, n.p.y, Color.cyan);
 				//System.out.printf("(%d,%d) -> (%d,%d)\n",tmp_x0, tmp_y0, n.p.x, n.p.y);
 			}
 			
 			System.out.println("Number of sample points found: " + regression.size());
 			
 			/*
 			 * Compare with results
 			 * 
 			 * file format:
 			 * <n number of entries>
 			 * <x1>,<y1>
 			 * ...
 			 * <xn>,<yn>
 			 */
 			if(args.length == 5){
 				try{
 					BufferedReader reader = new BufferedReader(new FileReader(args[4]));
 					int fargs = Integer.parseInt(reader.readLine().trim());
 					String lineinpt[];
 					float[][] realvalues = new float[fargs][2];
 					for(int i=0; i < fargs; i++){
 						lineinpt = reader.readLine().trim().split(",");
 						realvalues[i][0] = Float.parseFloat(lineinpt[0]);
 						realvalues[i][1] = Float.parseFloat(lineinpt[1]);
 					}
 					
 					// Calculate Pixel -> Calculated conversion
 					dx = Math.abs((float) (points.get(line.get(line.size() - 1)).p.x - points.get(line.get(0)).p.x) / (realvalues[fargs - 1][0] - realvalues[0][0]));
 					dy = ((float) (points.get(line.get(line.size() - 1)).p.y - points.get(line.get(0)).p.y) / (realvalues[fargs - 1][1] - realvalues[0][1]));
 					x0 = points.get(line.get(0)).p.x - (realvalues[0][0] * dx);
 					y0 = points.get(line.get(0)).p.y - (realvalues[0][1] * dy);
 					
 					//System.out.printf("dx: %f\tdy: %f\tx0: %f\ty0: %f\n",dx,dy,x0,y0);
 					
 					//Write results to CSV file
 					BufferedWriter writer = new BufferedWriter(new FileWriter(args[4] + ".res.csv"));
 					writer.write("t:" + (endt - startt));
 					
 					/*
 					 * Find the regression value Y for a given real value X along the line
 					 */
 					float est,err,errrange,pcterr;
 					errrange = 1.0f/Math.abs(dy);
 					System.out.printf("Error range (x,y): (%f,%f)\n",1.0f/Math.abs(dx),errrange);
 					System.out.printf("x\t\ty\t\ty\'\t\terr\t\tpct err rng\n");
 					System.out.printf("----\t\t----\t\t----\t\t----\t\t----\n");
 					for(int i=0; i < fargs; i++){
 						est = estimateY(realvalues[i][0]);
 						err = (Math.max(realvalues[i][1],est) - Math.min(realvalues[i][1],est));
 						pcterr = err / errrange;
 						System.out.printf("%f\t%f\t%f\t%f\t%f\n",realvalues[i][0],realvalues[i][1],est,err,pcterr);
 						writer.write(
 								realvalues[i][0] + "," +
 								realvalues[i][1] + "," +
 								est + "," +
 								err + "," + pcterr + "\n");
 					}
 					writer.close(); // close file
 				}catch(Exception e){
 					System.out.println("Compare Error:" + e);
 					e.printStackTrace();
 				}
 			}
 			/*
 			 * X values - 8 values are provided for the 2 axis and then a list of x values
 			 * are passed to find the y'.
 			 * 
 			 * *_im refers to a pixel value
 			 * *_val refers to the corresponding data value
 			 * 
 			 * file format:
 			 * x0_im,x0_val
 			 * x1_im,x1_val
 			 * y0_im,y0_val
 			 * y1_im,y1_val
 			 * <type 1=just x,2=x,y pair> 
 			 * <n number of entries>
 			 * <x1>[,<y1>]
 			 * ...
 			 * <xn>[,<yn>]
 			 */
 			else if(args.length == 6){
 				try{
 					
 					String lineinpt[], sline;
 					
 					BufferedReader reader = new BufferedReader(new FileReader(args[4]));
 					
 					BufferedWriter writer = new BufferedWriter(new FileWriter(args[5]));
 					writer.write("t:" + (endt - startt) + "\n");
 					
 					// READ AXIS INFORMATION
 					float x0_im, x0_val, x1_im, x1_val, y0_im, y0_val, y1_im, y1_val;
 					
 					lineinpt = reader.readLine().trim().split(",");
 					x0_im = Float.parseFloat(lineinpt[0]);
 					x0_val = Float.parseFloat(lineinpt[1]);
 					
 					lineinpt = reader.readLine().trim().split(",");
 					x1_im = Float.parseFloat(lineinpt[0]);
 					x1_val = Float.parseFloat(lineinpt[1]);
 					
 					lineinpt = reader.readLine().trim().split(",");
 					y0_im = Float.parseFloat(lineinpt[0]);
 					y0_val = Float.parseFloat(lineinpt[1]);
 					
 					lineinpt = reader.readLine().trim().split(",");
 					y1_im = Float.parseFloat(lineinpt[0]);
 					y1_val = Float.parseFloat(lineinpt[1]);
 					
 					int comptype = Integer.parseInt(reader.readLine().trim());
 					int fargs = Integer.parseInt(reader.readLine().trim());
 					
 					// Calculate Pixel -> Calculated conversion
 					
 					dx = (x1_im - x0_im) / (x1_val - x0_val);
 					dy = (y1_im - y0_im) / (y1_val - y0_val);
					//x0 = x0_im - dx * x
					x0 = x0_im - x0_val * dx; // x-offset
					y0 = y0_im - y0_val * dy ; // y-offset
 					
 					System.out.printf("dx: %f\tdy: %f\tx0: %f\ty0: %f\n",dx,dy,x0,y0);
 					
 					// ------------
 					//    X ONLY
 					// ------------
 					if(comptype == 1){
 						float[] realvalues = new float[fargs];
 						for(int i=0; i < fargs; i++){
 							realvalues[i] = Float.parseFloat(reader.readLine().trim());
 						}
 						
 						/*
 						 * Find the regression value Y for a given real value X along the line
 						 */
 						float est,err,errrange,pcterr;
 						errrange = 1.0f/Math.abs(dy);
 						System.out.printf("Error range (x,y): (%f,%f)\n",1.0f/Math.abs(dx),errrange);
 						System.out.printf("x\t\ty\'\n");
 						System.out.printf("----\t\t----\n");
 						for(int i=0; i < fargs; i++){
 							est = estimateY(realvalues[i]);
 							System.out.printf("%f\t%f\n",realvalues[i],est);
 							writer.write(
 									realvalues[i] + "," +
 									realvalues[i] + "," + "\n");
 						}
 					}
 					// ------------
 					//   X AND Y
 					// ------------
 					else{
 						float[][] realvalues = new float[fargs][2];
 						for(int i=0; i < fargs; i++){
 							lineinpt = reader.readLine().trim().split(",");
 							realvalues[i][0] = Float.parseFloat(lineinpt[0]);
 							realvalues[i][1] = Float.parseFloat(lineinpt[1]);
 						}
 						
 						/*
 						 * Find the regression value Y for a given real value X along the line
 						 */
 						float est,err,errrange,pcterr;
 						errrange = 1.0f/Math.abs(dy);
 						System.out.printf("Error range (x,y): (%f,%f)\n",1.0f/Math.abs(dx),errrange);
 						System.out.printf("x\t\ty\t\ty\'\t\terr\t\tpct err rng\n");
 						System.out.printf("----\t\t----\t\t----\t\t----\t\t----\n");
 						for(int i=0; i < fargs; i++){
 							est = estimateY(realvalues[i][0]);
 							err = (Math.max(realvalues[i][1],est) - Math.min(realvalues[i][1],est));
 							pcterr = err / errrange;
 							System.out.printf("%f\t%f\t%f\t%f\t%f\n",realvalues[i][0],realvalues[i][1],est,err,pcterr);
 							writer.write(
 									realvalues[i][0] + "," +
 									realvalues[i][1] + "," +
 									est + "," +
 									err + "," + pcterr + "\n");
 						}
 					}
 					
 					writer.close(); // close file
 				}catch(Exception e){
 					System.out.println("Compare Error:" + e);
 					e.printStackTrace();
 				}
 			}
 			
 			
 		}catch(Exception e){
 			System.out.println("Error: " + e);
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	static float estimateY(float x){
 		float xp,yp,ypct;
 		float estimate = 0.0f;
 		xp = dx * x + x0;
 		int i = 1, max = regression.size() - 1;
 		while(i < max){
 			if(regression.get(i).p.x >= xp)
 				break;
 			i++;
 		}
 		ypct = (xp - (float) regression.get(i - 1).p.x) / ((float) regression.get(i).p.x - (float) regression.get(i - 1).p.x);
 		yp = ypct * ((float) regression.get(i).p.y - (float) regression.get(i - 1).p.y) + (float) regression.get(i - 1).p.y;
 		estimate = (yp - y0) / dy;
 		return estimate;
 	}
 
 	
 	private static void usage(){
 		System.out.println("Usage:\n" +
 				"java test.ImageCli <filename> <pX> <pY> <tolerance> [<compare file> | <x vals file> <outpt>]");
 		System.exit(1);
 	}
 
 }
