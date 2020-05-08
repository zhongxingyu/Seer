 package com.adudziec.defects;
 
 import java.util.Random;
 
 import org.opencv.core.Core;
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 import org.opencv.core.Point;
 import org.opencv.core.Scalar;
 import org.opencv.core.Size;
 import org.opencv.imgproc.Imgproc;
 
 public class ImageProcessor {
 	
 	private double a1, b1, c1, a2, b2, c2, inflection, tmpDouble, L, M, S;
 	private float[] 			buff;
 	private Mat					tmp;
 	private Mat					spotsMatrix = null;
 	private double[]			RGB_LMS =  {0.05059983, 0.08585369, 0.00952420,
 											0.01893033, 0.08925308, 0.01370054,
 											0.00292202, 0.00975732, 0.07145979};
 	private double[]			LMS_RGB =  {30.830854, -29.832659, 1.610474, 
 											-6.481468, 17.715578, -2.532642,
 											-0.375690, -1.199062, 14.273846};
	private Random r;
	
	public ImageProcessor(){
		r = new Random(System.currentTimeMillis());
	}
 
 	
 	public void setupColorBlindness(ViewModes viewMode){
 		double anchor_e[] = new double[3];
 		double anchor[] = new double[12];
 
 		anchor[0] = 0.08008;  anchor[1]  = 0.1579;    anchor[2]  = 0.5897;
 		anchor[3] = 0.1284;   anchor[4]  = 0.2237;    anchor[5]  = 0.3636;
 		anchor[6] = 0.9856;   anchor[7]  = 0.7325;    anchor[8]  = 0.001079;
 		anchor[9] = 0.0914;   anchor[10] = 0.007009;  anchor[11] = 0.0;
 
 		anchor_e[0] = 0.05059983 + 0.08585369 + 0.00952420;
 		anchor_e[1] = 0.01893033 + 0.08925308 + 0.01370054;
 		anchor_e[2] = 0.00292202 + 0.00975732 + 0.07145979;
 		switch (viewMode)
 		{
 		case VIEW_MODE_DEUTERANOPE:
 			a1 = anchor_e[1] * anchor[8] - anchor_e[2] * anchor[7];
 			b1 = anchor_e[2] * anchor[6] - anchor_e[0] * anchor[8];
 			c1 = anchor_e[0] * anchor[7] - anchor_e[1] * anchor[6];
 			a2 = anchor_e[1] * anchor[2] - anchor_e[2] * anchor[1];
 			b2 = anchor_e[2] * anchor[0] - anchor_e[0] * anchor[2];
 			c2 = anchor_e[0] * anchor[1] - anchor_e[1] * anchor[0];
 			inflection = (anchor_e[2] / anchor_e[0]);
 			break;
 		case VIEW_MODE_PROTANOPE:
 			a1 = anchor_e[1] * anchor[8] - anchor_e[2] * anchor[7];
 			b1 = anchor_e[2] * anchor[6] - anchor_e[0] * anchor[8];
 			c1 = anchor_e[0] * anchor[7] - anchor_e[1] * anchor[6];
 			a2 = anchor_e[1] * anchor[2] - anchor_e[2] * anchor[1];
 			b2 = anchor_e[2] * anchor[0] - anchor_e[0] * anchor[2];
 			c2 = anchor_e[0] * anchor[1] - anchor_e[1] * anchor[0];
 			inflection = (anchor_e[2] / anchor_e[1]);
 			break;
 		case VIEW_MODE_TRITANOPE:
 			a1 = anchor_e[1] * anchor[11] - anchor_e[2] * anchor[10];
 			b1 = anchor_e[2] * anchor[9]  - anchor_e[0] * anchor[11];
 			c1 = anchor_e[0] * anchor[10] - anchor_e[1] * anchor[9];
 			a2 = anchor_e[1] * anchor[5]  - anchor_e[2] * anchor[4];
 			b2 = anchor_e[2] * anchor[3]  - anchor_e[0] * anchor[5];
 			c2 = anchor_e[0] * anchor[4]  - anchor_e[1] * anchor[3];
 			inflection = (anchor_e[1] / anchor_e[0]);
 			break;
 		default:
 			break;
 		}
 	}
 	
 	public void setupPigmentosa(Mat source){
 		// only small circle of visible area
		if (spotsMatrix != null)
			spotsMatrix.release();
 		spotsMatrix = source.clone();
 		spotsMatrix.setTo(new Scalar(0));
 		int rows = (int) spotsMatrix.size().height;
 		int cols = (int) spotsMatrix.size().width;
 		int factor = 2;

 		// generate first (anchor) point
 		int x = cols/2;
 		int y = rows/2;
 		Mat tmpMat = spotsMatrix.submat(y, y+5, x, x+5);
 		addSpots(tmpMat, 1);
 		int xPrim, yPrim;
 		int distance = Math.min(cols/factor, rows/factor);
 		int iterations = 0;
 		while( iterations < spotsMatrix.size().area()/(factor*30)){
 			xPrim = x + r.nextInt((cols/factor)) - (cols/factor)/2;
 			yPrim = y + r.nextInt((rows/factor)) - (rows/factor/2);
 			if (Math.sqrt((xPrim - x)*(xPrim - x) + (yPrim - y)*(yPrim - y)) > distance/2)
 				continue;
 			tmpMat = spotsMatrix.submat(yPrim, yPrim+5, xPrim, xPrim+5);
 			addSpots(tmpMat, 1);
 			iterations++;
 		}
 		
 
 		Mat erosionElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2, 2));
 		Mat dilationElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
 		Imgproc.erode(spotsMatrix, spotsMatrix, erosionElement, new Point(-1, -1), 1);
 		Imgproc.dilate(spotsMatrix, spotsMatrix, dilationElement, new Point(-1, -1), 8);
 		Imgproc.erode(spotsMatrix, spotsMatrix, erosionElement, new Point(-1, -1), 3);
 		Imgproc.blur(spotsMatrix, spotsMatrix, new Size(15, 15));
 	}
 	
 	public void setupRetinopathy(Mat source){
 		// black spot in random place
		if (spotsMatrix != null)
			spotsMatrix.release();
 		spotsMatrix = source.clone();
		Core.pow(spotsMatrix, 0, spotsMatrix);
 		int rows = (int) spotsMatrix.size().height;
 		int cols = (int) spotsMatrix.size().width;
 		int factor = 6;
 		
 		// generate first (anchor) point
 		int x = r.nextInt(cols - 2*(cols/factor)) + (cols/factor);
 		int y = r.nextInt(rows - 2*(rows/factor)) + (rows/factor);
 		Mat tmpMat = spotsMatrix.submat(y, y+5, x, x+5);
 		addSpots(tmpMat, 0);
 		int xPrim, yPrim;
 		int distance = Math.min(cols/factor, rows/factor);
 		int iterations = 0;
 		while( iterations < spotsMatrix.size().area()/500){
 			xPrim = x + r.nextInt((cols/factor)) - (cols/factor)/2;
 			yPrim = y + r.nextInt((rows/factor)) - (rows/factor/2);
			if (Math.sqrt((xPrim - x)*(xPrim - x) + (yPrim - y)*(yPrim - y)) > distance)
 				continue;
 			tmpMat = spotsMatrix.submat(yPrim, yPrim+5, xPrim, xPrim+5);
 			addSpots(tmpMat, 0);
 			iterations++;
 		}
 		
 
 		Mat erosionElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
 		Mat dilationElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(1, 1));
 		Imgproc.dilate(spotsMatrix, spotsMatrix, dilationElement, new Point(-1, -1), 1);
 		Imgproc.erode(spotsMatrix, spotsMatrix, erosionElement, new Point(-1, -1), 20);
 		Imgproc.dilate(spotsMatrix, spotsMatrix, dilationElement, new Point(-1, -1), 20);
 	}
 	
 	private void addSpots(Mat tmpMat, int data){
 		// cross structuring element
 //		tmpMat.put(0, 0, 0, 0, 0, 1);
 //		tmpMat.put(1, 1, 0, 0, 0, 1);
 //		tmpMat.put(2, 2, 0, 0, 0, 1);
 //		tmpMat.put(3, 3, 0, 0, 0, 1);
 //		tmpMat.put(4, 4, 0, 0, 0, 1);
 //		tmpMat.put(4, 0, 0, 0, 0, 1);
 //		tmpMat.put(3, 1, 0, 0, 0, 1);
 //		tmpMat.put(1, 3, 0, 0, 0, 1);
 //		tmpMat.put(0, 4, 0, 0, 0, 1);
 		
 		// circle structuring element
 		tmpMat.put(0, 1, data, data, data, data);
 		tmpMat.put(0, 3, data, data, data, data);
 		tmpMat.put(1, 0, data, data, data, data);
 		tmpMat.put(1, 4, data, data, data, data);
 		tmpMat.put(2, 2, data, data, data, data);
 		tmpMat.put(3, 0, data, data, data, data);
 		tmpMat.put(3, 4, data, data, data, data);
 		tmpMat.put(4, 1, data, data, data, data);
 		tmpMat.put(4, 3, data, data, data, data);
 	}
 	
 	public Mat process(Mat src, ViewModes viewMode){
 		Mat result = src.clone();
 		switch (viewMode)
 		{
 		case VIEW_MODE_RGBA:
 			break;
 		case VIEW_MODE_DEUTERANOPE:
 			deuteranope(result);
 			break;
 		case VIEW_MODE_PROTANOPE:
 			protanope(result);
 			break;
 		case VIEW_MODE_TRITANOPE:
 			tritanope(result);
 			break;
 		case VIEW_MODE_CATARACT:
 			cataract(result);
 			break;
 		case VIEW_MODE_DIABETIC_RETINOPATHY:
 			diabeticRetinopathy(result);
 			break;
 		case VIEW_MODE_RETINIS_PIGMENTOSA:
 			retinisPigmentosa(result);
 			break;
 		default:
 			break;
 		}
 		return result;
 	}
 	
 	public void deuteranope(Mat mRgba){
 		if (tmp == null || (!tmp.size().equals(mRgba.size()) || tmp.total() != mRgba.total() || tmp.channels() != mRgba.channels())){
 			tmp = new Mat(mRgba.size(), CvType.CV_32F);
 			buff = new float[(int) (mRgba.total() * mRgba.channels())];
 		}
 		mRgba.convertTo(tmp, CvType.CV_32F);
 		// get image matrix into Java primitive
 		tmp.get(0, 0, buff);
 		for (int i = 0; i < (int) (tmp.total() * tmp.channels()); i+=4){
 
 
 			// convert RGB to LMS color space
 			L = RGB_LMS[0] * buff[i] + RGB_LMS[1] * buff[i+1] + RGB_LMS[2] * buff[i+2];
 			M = RGB_LMS[3] * buff[i] + RGB_LMS[4] * buff[i+1] + RGB_LMS[5] * buff[i+2];
 			S = RGB_LMS[6] * buff[i] + RGB_LMS[7] * buff[i+1] + RGB_LMS[8] * buff[i+2];
 
 			// apply color blindness 
 			tmpDouble = S / L;
 
 			if (tmpDouble < inflection)
 				M = -(a1 * L + c1 * S) / b1;
 			else
 				M = -(a2 * L + c2 * S) / b2;
 
 			// convert LMS to RGB color space
 			buff[i] = (float) (LMS_RGB[0] * L + LMS_RGB[1] * M + LMS_RGB[2] * S);
 			buff[i+1] = (float) (LMS_RGB[3] * L + LMS_RGB[4] * M + LMS_RGB[5] * S);
 			buff[i+2] = (float) (LMS_RGB[6] * L + LMS_RGB[7] * M + LMS_RGB[8] * S);
 		}
 		// put new data into image matrix
 		tmp.put(0, 0, buff);
 		tmp.convertTo(mRgba, mRgba.depth());	
 	}
 	
 	public void protanope(Mat mRgba){
 		if (tmp == null || (tmp.size() != mRgba.size() && tmp.total() != mRgba.total() && tmp.channels() != mRgba.channels())){
 			tmp = new Mat(mRgba.size(), CvType.CV_32F);
 			buff = new float[(int) (mRgba.total() * mRgba.channels())];
 		}
 		mRgba.convertTo(tmp, CvType.CV_32F);
 		// get image matrix into Java primitive
 		tmp.get(0, 0, buff);
 		for (int i = 0; i < (int) (tmp.total() * tmp.channels()); i+=4){
 			// convert RGB to LMS color space
 			L = RGB_LMS[0] * buff[i] + RGB_LMS[1] * buff[i+1] + RGB_LMS[2] * buff[i+2];
 			M = RGB_LMS[3] * buff[i] + RGB_LMS[4] * buff[i+1] + RGB_LMS[5] * buff[i+2];
 			S = RGB_LMS[6] * buff[i] + RGB_LMS[7] * buff[i+1] + RGB_LMS[8] * buff[i+2];
 
 			// apply color blindness 
 			tmpDouble = S / M;
 
 			if (tmpDouble < inflection)
 				L = -(b1 * M + c1 * S) / a1;
 			else
 				L = -(b2 * M + c2 * S) / a2;
 
 			// convert LMS to RGB color space
 			buff[i] = (float) (LMS_RGB[0] * L + LMS_RGB[1] * M + LMS_RGB[2] * S);
 			buff[i+1] = (float) (LMS_RGB[3] * L + LMS_RGB[4] * M + LMS_RGB[5] * S);
 			buff[i+2] = (float) (LMS_RGB[6] * L + LMS_RGB[7] * M + LMS_RGB[8] * S);
 		}
 		// put new data into image matrix
 		tmp.put(0, 0, buff);
 		tmp.convertTo(mRgba, mRgba.depth());
 	}
 	
 	public void tritanope(Mat mRgba){
 		if (tmp == null || (tmp.size() != mRgba.size() && tmp.total() != mRgba.total() && tmp.channels() != mRgba.channels())){
 			tmp = new Mat(mRgba.size(), CvType.CV_32F);
 			buff = new float[(int) (mRgba.total() * mRgba.channels())];
 		}
 		mRgba.convertTo(tmp, CvType.CV_32F);
 		// get image matrix into Java primitive
 		tmp.get(0, 0, buff);
 		for (int i = 0; i < (int) (tmp.total() * tmp.channels()); i+=4){
 			// convert RGB to LMS color space
 			L = RGB_LMS[0] * buff[i] + RGB_LMS[1] * buff[i+1] + RGB_LMS[2] * buff[i+2];
 			M = RGB_LMS[3] * buff[i] + RGB_LMS[4] * buff[i+1] + RGB_LMS[5] * buff[i+2];
 			S = RGB_LMS[6] * buff[i] + RGB_LMS[7] * buff[i+1] + RGB_LMS[8] * buff[i+2];
 
 			// apply color blindness 
 			tmpDouble = M / L;
 
 			if (tmpDouble < inflection)
 				S = -(a1 * L + b1 * M) / c1;
 			else
 				S = -(a2 * L + b2 * M) / c2;
 
 			// convert LMS to RGB color space
 			buff[i] = (float) (LMS_RGB[0] * L + LMS_RGB[1] * M + LMS_RGB[2] * S);
 			buff[i+1] = (float) (LMS_RGB[3] * L + LMS_RGB[4] * M + LMS_RGB[5] * S);
 			buff[i+2] = (float) (LMS_RGB[6] * L + LMS_RGB[7] * M + LMS_RGB[8] * S);
 		}
 		// put new data into image matrix
 		tmp.put(0, 0, buff);
 		tmp.convertTo(mRgba, mRgba.depth());
 		
 	}
 	
 	public void cataract(Mat mRgba){
 		Imgproc.blur(mRgba, mRgba, new Size(9, 9));
 	}
 	
 	public void diabeticRetinopathy(Mat mRgba){
 		Core.multiply(spotsMatrix, mRgba, mRgba);
 		Imgproc.blur(mRgba, mRgba, new Size(7, 7));
 	}
 	
 	public void retinisPigmentosa(Mat mRgba){
 		Core.multiply(spotsMatrix, mRgba, mRgba);
		Imgproc.blur(mRgba, mRgba, new Size(3, 3));
 	}
 	
 	
 
 }
