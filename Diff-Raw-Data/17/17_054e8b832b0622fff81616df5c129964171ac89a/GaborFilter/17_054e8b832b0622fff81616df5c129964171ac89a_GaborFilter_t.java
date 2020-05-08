 package com.vnexit.fingerprint.gaborfilter;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.Kernel;
 import java.awt.image.WritableRaster;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import com.vnexit.fingerprint.datamodel.CannyEdgeDetector;
 import com.vnexit.fingerprint.datamodel.ExtractFeatured;
 import com.vnexit.fingerprint.datamodel.ImageProcess;
 import com.vnexit.fingerprint.datamodel.Pixel;
 import com.vnexit.fingerprint.datamodel.SplitThreshold;
 import com.vnexit.fingerprint.datamodel.ThinningProcess;
 
 public class GaborFilter extends JPanel {
 	BufferedImage mFinger;
 	Pixel[][] pi;
 	ImageProcess imgProcess;
 	WritableRaster wr;
 	Graphics2D graphics;
 	double[][] gra;
 	String name;
 	int[][] ft = new int[100][100];
 
 	public GaborFilter() {
 		try {
 			name = "testfingerv2.jpg";
 			mFinger = ImageIO.read(getClass().getResource("../datatest/testfingerv2.jpg"));
 			imgProcess = new ImageProcess();
 			wr = mFinger.getRaster();
 			pi = imgProcess.getData(mFinger);
 			setFt();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		Graphics g = mFinger.getGraphics();
 		g.drawImage(mFinger, 0, 0, null);
 	}
 
 	public void setFt() {
 		ft = new int[pi.length][pi[0].length];
 		for (int i = 0; i < pi.length; i++) {
 			for (int j = 0; j < pi[0].length; j++) {
 				ft[i][j] = 0;
 			}
 		}
 	}
 
 	public void rePaintLink(String link) {
 		try {
 			name = link;
 			mFinger = ImageIO.read(getClass().getResource("../datatest/" + link));
 			imgProcess = new ImageProcess();
 			wr = mFinger.getRaster();
 			pi = imgProcess.getData(mFinger);
 			setFt();
 			repaint();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void paint(Graphics g) {
 
 		Graphics2D g2d = (Graphics2D) g;
 
 		this.graphics = g2d;
 
 		g2d.drawImage(mFinger, null, 0, 0);
 
 		for (int i = 8; i < ft.length - 8; i++)
 			for (int j = 8; j < ft[0].length - 8; j++) {
 				if (ft[i][j] == 2)
 					g2d.drawOval(i, j, 5, 5);
 			}
 	}
 
 	public void tests() {
 		pi = imgProcess.grayExchange(pi);
 		imgProcess.setData(wr, pi);
 		repaint();
 	}
 
 	// Tach nguong tu dong
 	public void threshold() {
 		// pi = imgProcess.getData(mFinger);
 		// pi = imgProcess.grayExchange(pi);
 		SplitThreshold sp = new SplitThreshold();
 		// pi=sp.newSplitThreshold(pi);
 		// int thre=sp.getThreshold(pi);
 		// pi=sp.splitThreshold(pi,thre);
 
 		// Tach nguong bang thuat toan dang dieu
 
 		int threshold = sp.getIterativeSelection(pi);
 		pi = sp.splitThreshold(pi, threshold);
 		imgProcess.setData(wr, pi);
 		repaint();
 	}
 
 	public void Convolution() {
 		// float[] sharpen = { 1.0f, -2.0f, 1.0f, -2.0f, 4.0f, -2.0f, 1.0f, -2.0f,
 		// 1.0f };
 		float[] gaussian = { 1 / 273f, 4 / 273f, 7 / 273f, 4 / 273f, 1 / 273f, 4 / 273f, 16 / 273f, 26 / 273f, 16 / 273f, 4 / 273f, 7 / 273f, 26 / 273f, 41 / 273f, 26 / 273f, 7 / 273f, 4 / 273f, 16 / 273f, 26 / 273f, 16 / 273f, 4 / 273f, 1 / 273f, 4 / 273f, 7 / 273f, 4 / 273f, 1 / 273f };
 		Kernel kernel = new Kernel(3, 3, gaussian);
 		BufferedImage img = imgProcess.ConvolutionImage(mFinger, kernel);
 		// pi = imgProcess.getData(img);
 		imgProcess.setData(wr, pi);
 		repaint();
 	}
 
 	// Tang cuong hinh anh
 	public void IncreaseFinger() {
 		// pi = imgProcess.grayExchange(pi);
 		// imgProcess.setData(wr, pi);
 		pi = imgProcess.IncreaseFinger(pi);
 		imgProcess.setData(wr, pi);
 		repaint();
 	}
 
 	public void Gabor() {
 		// Ma tran Sobel theo truc x
 		float[] hx = { 1.0f, 0.0f, -1.0f, 2.0f, 0f, -2.0f, 1.0f, 0.0f, -1.0f };
 		// Ma tran Sobel theo truc y
 		float[] hy = { 1.0f, 2.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, -2.0f, -1.0f };
 		// double[][]
 		// gaussian={{1/273,4/273,7/273,4/273f,1/273},{4/273,16/273,26/273,16/273,4/273},{7/273,26/273,41/273,26/273,7/273},{4/273,16/273,26/273,16/273,4/273},{1/273,4/273,7/273,4/273,1/273}};
 
 		// Khoi tao 2 ma tran Kernel voi dau vao la 2 ma tran Sobel da tao o tren
 		Kernel ke1 = new Kernel(3, 3, hx);
 		Kernel ke2 = new Kernel(3, 3, hy);
 
 		// Nhap chap anh dau vao lan luot voi 2 ma tran Sobel
 		BufferedImage imgx = imgProcess.ConvolutionImage(mFinger, ke1);
 		BufferedImage imgy = imgProcess.ConvolutionImage(mFinger, ke2);
 
 		// Doc du lieu tu anh sau khi da nhan chap voi ma tran Sobel
 		Pixel[][] sobelX = imgProcess.getData(imgx);
 		Pixel[][] sobelY = imgProcess.getData(imgy);
 
 		// Tinh huong gradien cua diem anh
 		double teta[][] = getDirection(sobelX, sobelY);
 		gra = teta;
 
 		bolocGabor(pi, teta);
 		repaint();
 
 	}
 
 	public double[][] getDirection(Pixel[][] sobelX, Pixel[][] sobelY) {
 		double[][] phi = new double[sobelX.length][sobelX[0].length];
 		double Gxy = 0;
 		double Gxx = 0;
 		double Gyy = 0;
 		double G = 0;
 		double count = 0;
 		for (int i = 8; i < sobelX.length - 8; i++) {
 			for (int j = 8; j < sobelX[0].length - 8; j++) {
 				for (int row = i - 8; row < i + 8; row++) {
 					for (int col = j - 8; col < j + 8; col++) {
 						Gxy += 2 * (sobelX[row][col].mBlue * sobelY[row][col].mBlue);
 						Gxx += Math.pow(sobelX[row][col].mBlue, 2);
 						Gyy += Math.pow(sobelY[row][col].mBlue, 2);
 						G += (Gxx - Gyy);
 					}
 				}
 				count = (Math.atan2(Gxy, G)) / 2;
 				phi[i][j] = count;
 				count = 0;
 				Gxy = 0;
 				Gxx = 0;
 				Gyy = 0;
 				G = 0;
 			}
 		}
 		return phi;
 	}
 
 	public double[][] filterGaussian(double[][] GauMatrix, double[][] omegaX, double[][] omegaY) {
 		double[][] phi = new double[omegaX.length][omegaX[0].length];
 		for (int i = 8; i < omegaX.length - 8; i++) {
 			for (int j = 8; j < omegaX[0].length - 8; j++) {
 				double phiX = 0;
 				double phiY = 0;
 				double count = 0;
 				for (int row = 0; row < 5; row++) {
 					for (int col = 0; col < 5; col++) {
 						phiX += GauMatrix[row][col] * omegaX[i - row / 2 * 5][j - col / 2 * 5];
 						phiY += GauMatrix[row][col] * omegaY[i - row / 2 * 5][j - col / 2 * 5];
 					}
 				}
 				count = Math.atan(phiY / phiX) / 2;
 				phi[i][j] = count;
 			}
 		}
 		return phi;
 	}
 
 	public void bolocGabor(Pixel[][] pi, double teta[][]) {
 		int[][] gabor = new int[pi.length][pi[0].length];
 		double sum = 0;
 		double f = 0.24; // 0.24
 		double deltaX = 1.3; // 1.3
 		double deltaY = 1.2; // 1.1
 		for (int i = 8; i < pi.length - 8; i++) {
 			for (int j = 8; j < pi[0].length - 8; j++) {
 				for (int x = i - 8; x < i + 8; x++) {
 					for (int y = j - 8; y < j + 8; y++) {
 						int x0 = x - i;
 						int y0 = y - j;
 						double X = x0 * Math.cos(teta[i][j]) + y0 * Math.sin(teta[i][j]);
 						double Y = -x0 * Math.sin(teta[i][j]) + y0 * Math.cos(teta[i][j]);
 						double gb = Math.exp(-0.5 * ((Math.pow(X, 2) / (Math.pow(deltaX, 2))) + (Math.pow(Y, 2) / (Math.pow(deltaY, 2))))) * Math.cos(2 * Math.PI * f * X);
 						sum = sum + gb * pi[x][y].mBlue;
 					}
 				}
 				gabor[i][j] = (int) sum;
 				sum = 0;
 			}
 		}
 		GaborFilter(pi, gabor);
 		// pi = imgProcess.getData(mFinger);
 		imgProcess.setData(wr, pi);
 	}
 
 	private void GaborFilter(Pixel[][] pix, int[][] gabor) {
 		for (int i = 0; i < gabor.length; i++) {
 			for (int j = 0; j < gabor[0].length; j++) {
 				int a = gabor[i][j];
 				if (a > 255) {
 					a = 255;
 				}
 				if (a < 0) {
 					a = 0;
 				}
 				pix[i][j] = new Pixel(a, a, a);
 			}
 		}
 	}
 
 	public void thinning() throws FileNotFoundException {
 		// pi = imgProcess.getData(mFinger);
 		ThinningProcess thinning = new ThinningProcess();
 		pi = thinning.thinning(pi);
 		pi = thinning.donsach(pi);
 		imgProcess.setData(wr, pi);
 		repaint();
 	}
 
 	public void CannyEdge() {
 		// create the detector
 		CannyEdgeDetector detector = new CannyEdgeDetector();
 
 		// adjust its parameters as desired
 		detector.setLowThreshold(0.5f);
 		detector.setHighThreshold(1f);
 
 		// apply it to an image
 		detector.setSourceImage(mFinger);
 		detector.process();
 		mFinger = detector.getEdgesImage();
 		// pi=imgProcess.getData(mFinger);
 		// wr=mFinger.getRaster();
 		// imgProcess.setData(wr, pi);
 		repaint();
 	}
 
 	public void writeFeatured(int[][] featured) {
 		for (int i = 8; i <= featured.length - 8; i++) {
 			System.out.print("\n");
 			for (int j = 8; j < featured[0].length - 8; j++) {
 				System.out.print(featured[i][j] + " ");
 			}
 		}
 	}
 
 	public void exportFile() throws FileNotFoundException {
 		FileOutputStream outFile = new FileOutputStream(new File(this.name + ".arff"));
 		PrintStream out = new PrintStream(outFile);
 		out.print(this.name + " ");
 		int k = 0;
 		for (int i = 0; i < pi.length; i++) {
 			for (int j = 0; j < pi[0].length; j++) {
 				out.print(k + ":" + ft[i][j] + " ");
 				k++;
 			}
 		}
 		out.close();
 	}
 
 	public void exportFile2() throws FileNotFoundException {
 		FileOutputStream outFile = new FileOutputStream(new File(this.name + ".arff"));
 		PrintStream out = new PrintStream(outFile);
 		out.print("@RELATION FingerPrint\n");
 		out.print("@attribute featureMatrix relational\n");
 		int k = 0;
 		for (int i = 0; i < pi.length; i++) {
 			for (int j = 0; j < pi[0].length; j++) {
 				out.print("@attribute " + k + " integer\n");
 				k++;
 			}
 		}
 		out.print("@end featureMatrix\n");
		out.print("@attribute gradienMatrix relational\n");
		k = 0;
		for (int i = 0; i < gra.length; i++) {
			for (int j = 0; j < gra[0].length; j++) {
				out.print("@attribute " + k + " integer\n");
				k++;
			}
		}
		out.print("@end featureMatrix\n");
 		out.print("@attribute class {A, NOT-A}\n");
 		out.print("@data\n");
 		k = 0;
 		for (int i = 0; i < pi.length; i++) {
 			for (int j = 0; j < pi[0].length; j++) {
 				out.print(ft[i][j] + ",");
 				k++;
 			}
 		}
		k = 0;
		for (int i = 0; i < gra.length; i++) {
			for (int j = 0; j < gra[0].length; j++) {
				out.print(gra[i][j] + ",");
				k++;
			}
		}
 		out.print(" A\n");
 		out.close();
 	}
 
 	public void extractFeatured() throws FileNotFoundException {
 		// pi = imgProcess.getData(mFinger);
 		ExtractFeatured ex = new ExtractFeatured();
 		int[][] featured = ex.getFeatured(pi);
 		ft = featured;
 		imgProcess.setData(wr, pi);
 		writeFeatured(featured);
 		repaint();
 		exportFile2();
 	}
 }
