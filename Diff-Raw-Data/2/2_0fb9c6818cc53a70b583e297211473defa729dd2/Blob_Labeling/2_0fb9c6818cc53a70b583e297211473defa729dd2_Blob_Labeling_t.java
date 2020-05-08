 package kr.hs.sshs.JavaPTS;
 
 import static com.googlecode.javacv.cpp.opencv_core.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Blob_Labeling {
 	
 	//static IplImage org;
 	
 	//static IplImage img;
 	
 	int[][] print;
 	
 	static int[][] val; 
 	
 	public static final int junk = -1;
 	
 	public static List<Info> blob;
 	public static List<Integer> table;
 	public static List<List<CvPoint>> points;
 	
 	static int width2;
 	static int height2;
 	
 	public static int check1(int x, int y) {
 		if(x==0)
 			return 0;
 		else
 			return val[x-1][y];
 	}
 	
 	public static int check2(int x, int y) {
 		if(x==0 || y==0)
 			return 0;
 		else
 			return val[x-1][y-1];
 	}
 	
 	public static int check3(int x, int y) { 
 		if(y==0)
 			return 0;
 		else
 			return val[x][y-1];
 	}
 	
 	public static int check4(int x, int y) { 
 		if(x==width2-1 || y==0)
 			return 0;
 		else
 			return val[x+1][y-1];
 	}
 	
 	public static int nmin(int a, int b) { 
 		if(a==0)
 			return b;
 		if(b==0)
 			return a;
 		if(a<=b)
 			return a;
 		else
 			return b;
 	}
 	
 	/*public static int nmin(int a, int b, int c) {
 		return nmin(nmin(a,b),c);
 	}*/
 	
 	public static int nmin(int a, int b, int c, int d) {
 		return nmin(nmin(a,b),nmin(c,d));
 	}
 	
 	public static void t_add(int a, int b) { 
 		if(a!=0)
 			table.set(a,nmin(table.get(a),table.get(b)));
 		if(b!=0)
 			table.set(b,nmin(table.get(a),table.get(b)));
 	}
 	
 	public static void t_add(int a, int b, int c, int d) {
 		if(a!=0)
 			table.set(a,nmin(table.get(a),table.get(b),table.get(c),table.get(d)));
 		if(b!=0)
 			table.set(b,nmin(table.get(a),table.get(b),table.get(c),table.get(d)));
 		if(c!=0)
 			table.set(c,nmin(table.get(a),table.get(b),table.get(c),table.get(d)));
 		if(d!=0)
 			table.set(d,nmin(table.get(a),table.get(b),table.get(c),table.get(d)));
 	}
 
 	/**
 	* Detect blobs and return result as an IplImage
 	*/
 	public List<Info> detectBlob(int[][] org, int width, int height) {
 		
 		//org = cvCreateImage(cvGetSize(org_in),IPL_DEPTH_8U,1);
 		//cvCopy(org_in, org);
 
 		//img = cvCreateImage(cvGetSize(org),IPL_DEPTH_8U,1);
 		height2=height;
 		width2=width;
 		
 		val = new int[width][height];
 		print = new int[width][height];
 		
 		// blobs = new ArrayList<List> ();
 		blob = new ArrayList<Info> ();
 		table = new ArrayList<Integer> ();
 		
 		blob.add(new Info(junk,junk,junk,junk,0));
 		points = new ArrayList<List<CvPoint>>();
 		points.add(new ArrayList<CvPoint> ());
 		table.add(0);
 		
 		// CanvasFrame canvas = new CanvasFrame("Frame");
 		
 		// cvSetImageROI(org, cvRect(100, 100, 400, 400));
 		
 		//cvCopy(org,img); 
 				
 		int x=0, y=0, label=1;
 		
 		while (x!=(width-1) || y!=(height-1)) {
 			if (org[x][y] > 245) { 
 				if ( check1(x,y)==0 && check2(x,y)==0 && check3(x,y)==0 && check4(x,y)==0 ) { 
 					val[x][y]=label; 
 					table.add(label,label);
 					blob.add(label,new Info(x,y,x,y,1)); 
 					points.add(label, new ArrayList<CvPoint>());
 					points.get(label).add(new CvPoint(x,y));
 					label++;
 				}
 				else {
 					val[x][y]=nmin(check1(x,y),check2(x,y),check3(x,y),check4(x,y));  
 					t_add(check1(x,y),check2(x,y),check3(x,y),check4(x,y));
 					addinfo(blob.get(val[x][y]), x, y);
 					points.get(val[x][y]).add(new CvPoint(x,y));
 				}
 			}
 			if( x != (width-1) ) {
 				x++;
 			}
 			else {
 				y++;
 				x=0;
 			}
 		}
 		
 		int w=table.size()-1;
 		
 		for(;w>0;w--) { 
 			if(w!=table.get(w)) {
 				addinfo(blob.get(w),blob.get(table.get(w)));
 				points.get(table.get(w)).addAll(points.get(w));
 				blob.remove(w);
 				points.remove(w);
 			}
 		}
 		
 		for(int p=0;p<width;p++) {
 			for(int q=0;q<height;q++) {
 				val[p][q]=0;
 			}
 		}
 		
 		// int count=0; 
 		for (int i = blob.size() - 1; i > 0 ; i--) {
 			if (ballcondition(i) == false) {
 				blob.remove(i);
 				points.remove(i);
 			}
 		}
 		
 		// Stamping
 		for(;w<points.size();w++) {
 			for(int k=0;k<points.get(w).size();k++){
 				print[points.get(w).get(k).x()][points.get(w).get(k).y()]=w;
 			}
 			// count++;
 		}
 		
 		System.out.println("[Blob_Labelling.java] Blob size = " + blob.size());
 		//cvReleaseImage(org);
 		//cvReleaseImage(img);
 		return blob;
 	}	
 	
 	public static boolean ballcondition(int w) {
 		int cthres = 200;
 		int wthres = 20;
 		int hthres = 20;
 		if( (blob.get(w).count<cthres) && (blob.get(w).bwidth()<wthres) && (blob.get(w).bheight()<hthres) ){
 			blob.get(w).condition=true;
 			return true;
 		}
 		else {
 			blob.get(w).condition=false;
 			return false;
 		}
 	}
 	
 	public static void addinfo(Info a, Info b) {
 		b.count+=a.count;
 		if(b.xmin>a.xmin)
 			b.xmin=a.xmin;
 		if(b.xmax<a.xmax)
 			b.xmax=a.xmax;
 		if(b.ymin>a.ymin)
 			b.ymin=a.ymin;
 		if(b.ymax<a.ymax)
 			b.ymax=a.ymax;
 	}
 	
 	public static void addinfo(Info b, int p, int q) {
 		b.count++;
 		if(b.xmin>p)
 			b.xmin=p;
 		if(b.xmax<p)
 			b.xmax=p;
 		if(b.ymin>q)
 			b.ymin=q;
 		if(b.ymax<q)
 			b.ymax=q;
 	}
 }
