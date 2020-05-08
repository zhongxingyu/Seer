 import boofcv.abst.feature.associate.AssociateDescription;
 import boofcv.abst.feature.associate.ScoreAssociation;
 import boofcv.abst.feature.detdesc.DetectDescribePoint;
 import boofcv.abst.feature.disparity.StereoDisparity;
 import boofcv.abst.geo.Estimate1ofEpipolar;
 import boofcv.abst.geo.TriangulateTwoViewsCalibrated;
 import boofcv.alg.distort.ImageDistort;
 import boofcv.alg.distort.LensDistortionOps;
 import boofcv.alg.feature.UtilFeature;
 import boofcv.alg.geo.PerspectiveOps;
 import boofcv.alg.geo.RectifyImageOps;
 import boofcv.alg.geo.rectify.RectifyCalibrated;
 import boofcv.alg.sfm.robust.DistanceSe3SymmetricSq;
 import boofcv.alg.sfm.robust.Se3FromEssentialGenerator;
 import boofcv.core.image.ConvertBufferedImage;
 import boofcv.factory.feature.associate.FactoryAssociation;
 import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
 import boofcv.factory.feature.disparity.DisparityAlgorithms;
 import boofcv.factory.feature.disparity.FactoryStereoDisparity;
 import boofcv.factory.geo.EnumEpipolar;
 import boofcv.factory.geo.FactoryMultiView;
 import boofcv.factory.geo.FactoryTriangulate;
 import boofcv.gui.d3.PointCloudTiltPanel;
 import boofcv.gui.feature.AssociationPanel;
 import boofcv.gui.image.ShowImages;
 import boofcv.gui.image.VisualizeImageData;
 import boofcv.gui.stereo.RectifiedPairPanel;
 import boofcv.misc.BoofMiscOps;
 import boofcv.struct.FastQueue;
 import boofcv.struct.calib.IntrinsicParameters;
 import boofcv.struct.distort.PointTransform_F64;
 import boofcv.struct.feature.AssociatedIndex;
 import boofcv.struct.feature.SurfFeature;
 import boofcv.struct.geo.AssociatedPair;
 import boofcv.struct.image.ImageFloat32;
 import boofcv.struct.image.ImageSingleBand;
 import boofcv.struct.image.ImageUInt8;
 import georegression.struct.point.Point2D_F64;
 import georegression.struct.se.Se3_F64;
 import org.ddogleg.fitting.modelset.DistanceFromModel;
 import org.ddogleg.fitting.modelset.ModelGenerator;
 import org.ddogleg.fitting.modelset.ModelMatcher;
 import org.ddogleg.fitting.modelset.ransac.Ransac;
 import org.ejml.data.DenseMatrix64F;
 import org.ejml.ops.CommonOps;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import static boofcv.alg.geo.RectifyImageOps.transformRectToPixel_F64;
 
 /**
  * @author Peter Abeles
  */
 // TODO Push stereo into its own class to simplify
 public class AutoPointCloudApp {
 	final RovioControl control;
 
 	IntrinsicParameters intrinsic;
 
 	BufferedImage origLeft;
 	BufferedImage origRight;
 
 
 	ImageFloat32 left;
 	ImageFloat32 right;
 
 	// Detect image features
 	DetectDescribePoint<ImageFloat32,SurfFeature> detDesc;
 	// Associate image features
 	AssociateDescription<SurfFeature> associate;
 
 	List<AssociatedPair> matchedFeatures;
 
 	int minDisparity = 2;
 	int maxDisparity = 50;
 
 	double imageScale = 0.5;
 
 	// camera rectification matrices
 	DenseMatrix64F rect1;
 	DenseMatrix64F rect2;
 
 	public AutoPointCloudApp(final String ip ) {
 		control = new RovioControl(ip);
 
 		try {
 			// the MAC address uniquely identifies the robot
 			String MAC = null;
 			while( MAC == null)
 				MAC = control.getMacAddress();
 
			System.out.println("Robot MAC = "+MAC);

 			// see if the configuration files have already been computed
 			String configDir = RovioHelperOps.findRobotDirectory(MAC,true);
 			intrinsic = BoofMiscOps.loadXML(configDir+"/intrinsic.xml");
 			PerspectiveOps.scaleIntrinsic(intrinsic,imageScale);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		// configure image association
 		detDesc =  FactoryDetectDescribe.surf(1, 2, 200, 1, 9, 4, 4, true, ImageFloat32.class);
 		ScoreAssociation<SurfFeature> scorer = FactoryAssociation.defaultScore(detDesc.getDescriptorType());
 		associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, -1, true);
 	}
 
 	public void configureRobot() {
 		while( !control.setImageResolution(3) ){}
 		while( !control.setImageCompression(2) ){}
 	}
 
 	public void moveRobotCaptureImages() {
 		origLeft = origRight = null;
 
 		BoofMiscOps.pause(200);
 
 		origLeft = captureScaleImage();
 
 		control.movement(RovioManual.STRAIGHT_RIGHT,10);
 		BoofMiscOps.pause(50);
 		control.movement(RovioManual.STRAIGHT_RIGHT,10);
 
 		BoofMiscOps.pause(750);
 
 		origRight = captureScaleImage();
 
 		left = ConvertBufferedImage.convertFrom(origLeft, left);
 		right = ConvertBufferedImage.convertFrom(origRight, right);
 	}
 
 	private BufferedImage captureScaleImage() {
 		BufferedImage image = null;
 
 		while( image == null)
 			image = control.captureImage();
 
 		if( imageScale == 1 )
 			return image;
 
 		int w = (int)(image.getWidth()*imageScale);
 		int h = (int)(image.getHeight()*imageScale);
 
 
 		BufferedImage work = new BufferedImage(w,h,image.getType());
 		Graphics2D g2 = work.createGraphics();
 		g2.scale(imageScale,imageScale);
 		g2.drawImage(image,9,9,null);
 
 		return work;
 	}
 
 	public void associateImages() {
 
 		// stores the description of detected interest points
 		FastQueue<SurfFeature> descA = UtilFeature.createQueue(detDesc, 100);
 		FastQueue<SurfFeature> descB = UtilFeature.createQueue(detDesc,100);
 
 		// stores the location of detected interest points
 		List<Point2D_F64> pointsA = new ArrayList<Point2D_F64>();
 		List<Point2D_F64> pointsB = new ArrayList<Point2D_F64>();
 
 		// describe each image using interest points
 		describeImage(left,pointsA,descA);
 		describeImage(right,pointsB,descB);
 
 		// Associate features between the two images
 		associate.setSource(descA);
 		associate.setDestination(descB);
 		associate.associate();
 
 		// save matches
 		matchedFeatures = new ArrayList<AssociatedPair>();
 		FastQueue<AssociatedIndex> pairs = associate.getMatches();
 		for( AssociatedIndex a : pairs.toList() ) {
 			AssociatedPair p = new AssociatedPair(pointsA.get(a.src),pointsB.get(a.dst));
 			matchedFeatures.add( p );
 		}
 
 		// display the results
 //		AssociationPanel panel = new AssociationPanel(20);
 //		panel.setAssociation(pointsA,pointsB,associate.getMatches());
 //		panel.setImages(origLeft, origRight);
 //
 //		ShowImages.showWindow(panel, "Associated Features");
 	}
 
 
 	public void createPointCloud() {
 
 		// convert from pixel coordinates into normalized image coordinates
 		List<AssociatedPair> matchedCalibrated = convertToNormalizedCoordinates(matchedFeatures, intrinsic);
 
 		// Robustly estimate camera motion
 		List<AssociatedPair> inliers = new ArrayList<AssociatedPair>();
 		Se3_F64 leftToRight = estimateCameraMotion(intrinsic, matchedCalibrated, inliers);
 
 		leftToRight.print();
 
 		drawInliers(origLeft, origRight, intrinsic, inliers);
 
 		// Rectify and remove lens distortion for stereo processing
 		DenseMatrix64F rectifiedK = new DenseMatrix64F(3, 3);
 		ImageFloat32 rectifiedLeft = new ImageFloat32(left.width, left.height);
 		ImageFloat32 rectifiedRight = new ImageFloat32(right.width, right.height);
 
 		rectifyImages(left, right, leftToRight, intrinsic, rectifiedLeft, rectifiedRight, rectifiedK);
 
 		// compute disparity
 		StereoDisparity<ImageFloat32, ImageFloat32> disparityAlg =
 				FactoryStereoDisparity.regionSubpixelWta(DisparityAlgorithms.RECT_FIVE,
 						minDisparity, maxDisparity, 10, 10, 30, 1, 0.1, ImageFloat32.class);
 
 		// process and return the results
 		disparityAlg.process(rectifiedLeft, rectifiedRight);
 		ImageFloat32 disparity = disparityAlg.getDisparity();
 
 		// show results
 		BufferedImage visualized = VisualizeImageData.disparity(disparity, null, minDisparity, maxDisparity, 0);
 
 		BufferedImage outLeft = ConvertBufferedImage.convertTo(rectifiedLeft, null);
 		BufferedImage outRight = ConvertBufferedImage.convertTo(rectifiedRight, null);
 
 		ShowImages.showWindow(new RectifiedPairPanel(true, outLeft, outRight), "Rectification");
 		ShowImages.showWindow(visualized, "Disparity");
 
 		showPointCloud(disparity, origLeft, leftToRight, rectifiedK, minDisparity, maxDisparity);
 	}
 
 	/**
 	 * Estimates the camera motion robustly using RANSAC and a set of associated points.
 	 *
 	 * @param intrinsic   Intrinsic camera parameters
 	 * @param matchedNorm set of matched point features in normalized image coordinates
 	 * @param inliers     OUTPUT: Set of inlier features from RANSAC
 	 * @return Found camera motion.  Note translation has an arbitrary scale
 	 */
 	public static Se3_F64 estimateCameraMotion(IntrinsicParameters intrinsic,
 											   List<AssociatedPair> matchedNorm, List<AssociatedPair> inliers)
 	{
 		Estimate1ofEpipolar essentialAlg = FactoryMultiView.computeFundamental_1(EnumEpipolar.ESSENTIAL_5_NISTER, 5);
 		TriangulateTwoViewsCalibrated triangulate = FactoryTriangulate.twoGeometric();
 		ModelGenerator<Se3_F64, AssociatedPair> generateEpipolarMotion =
 				new Se3FromEssentialGenerator(essentialAlg, triangulate);
 
 		DistanceFromModel<Se3_F64, AssociatedPair> distanceSe3 =
 				new DistanceSe3SymmetricSq(triangulate,
 						intrinsic.fx, intrinsic.fy, intrinsic.skew,
 						intrinsic.fx, intrinsic.fy, intrinsic.skew);
 
 		// 1/2 a pixel tolerance for RANSAC inliers
 		double ransacTOL = 0.5 * 0.5 * 1.5;
 
 		ModelMatcher<Se3_F64, AssociatedPair> epipolarMotion =
 				new Ransac<Se3_F64, AssociatedPair>(2323, generateEpipolarMotion, distanceSe3,
 						500, ransacTOL);
 
 		if (!epipolarMotion.process(matchedNorm))
 			throw new RuntimeException("Motion estimation failed");
 
 		// save inlier set for debugging purposes
 		inliers.addAll(epipolarMotion.getMatchSet());
 
 		return epipolarMotion.getModel();
 	}
 
 	/**
 	 * Convert a set of associated point features from pixel coordinates into normalized image coordinates.
 	 */
 	public static List<AssociatedPair> convertToNormalizedCoordinates(List<AssociatedPair> matchedFeatures, IntrinsicParameters intrinsic) {
 
 		PointTransform_F64 tran = LensDistortionOps.transformRadialToNorm_F64(intrinsic);
 
 		List<AssociatedPair> calibratedFeatures = new ArrayList<AssociatedPair>();
 
 		for (AssociatedPair p : matchedFeatures) {
 			AssociatedPair c = new AssociatedPair();
 
 			tran.compute(p.p1.x, p.p1.y, c.p1);
 			tran.compute(p.p2.x, p.p2.y, c.p2);
 
 			calibratedFeatures.add(c);
 		}
 
 		return calibratedFeatures;
 	}
 
 	private void describeImage(ImageFloat32 input, List<Point2D_F64> points, FastQueue<SurfFeature> descs )
 	{
 		detDesc.detect(input);
 
 		for( int i = 0; i < detDesc.getNumberOfFeatures(); i++ ) {
 			points.add( detDesc.getLocation(i).copy() );
 			descs.grow().setTo(detDesc.getDescriptor(i));
 		}
 	}
 
 	/**
 	 * Remove lens distortion and rectify stereo images
 	 *
 	 * @param distortedLeft  Input distorted image from left camera.
 	 * @param distortedRight Input distorted image from right camera.
 	 * @param leftToRight    Camera motion from left to right
 	 * @param intrinsic      Intrinsic camera parameters
 	 * @param rectifiedLeft  Output rectified image for left camera.
 	 * @param rectifiedRight Output rectified image for right camera.
 	 * @param rectifiedK     Output camera calibration matrix for rectified camera
 	 */
 	public void rectifyImages(ImageFloat32 distortedLeft,
 							  ImageFloat32 distortedRight,
 							  Se3_F64 leftToRight,
 							  IntrinsicParameters intrinsic,
 							  ImageFloat32 rectifiedLeft,
 							  ImageFloat32 rectifiedRight,
 							  DenseMatrix64F rectifiedK) {
 		RectifyCalibrated rectifyAlg = RectifyImageOps.createCalibrated();
 
 		// original camera calibration matrices
 		DenseMatrix64F K = PerspectiveOps.calibrationMatrix(intrinsic, null);
 
 		rectifyAlg.process(K, new Se3_F64(), K, leftToRight);
 
 		// rectification matrix for each image
 		rect1 = rectifyAlg.getRect1();
 		rect2 = rectifyAlg.getRect2();
 
 		// New calibration matrix,
 		rectifiedK.set(rectifyAlg.getCalibrationMatrix());
 
 		// Adjust the rectification to make the view area more useful
 		RectifyImageOps.fullViewLeft(intrinsic, rect1, rect2, rectifiedK);
 
 		// undistorted and rectify images
 		ImageDistort<ImageFloat32> distortLeft =
 				RectifyImageOps.rectifyImage(intrinsic, rect1, ImageFloat32.class);
 		ImageDistort<ImageFloat32> distortRight =
 				RectifyImageOps.rectifyImage(intrinsic, rect2, ImageFloat32.class);
 
 		distortLeft.apply(distortedLeft, rectifiedLeft);
 		distortRight.apply(distortedRight, rectifiedRight);
 	}
 
 	/**
 	 * Show results as a point cloud
 	 */
 	public void showPointCloud(ImageSingleBand disparity, BufferedImage left,
 							   Se3_F64 motion, DenseMatrix64F rectifiedK ,
 							   int minDisparity, int maxDisparity) {
 		PointCloudTiltPanel gui = new PointCloudTiltPanel();
 
 		double baseline = motion.getT().norm();
 
 		PointTransform_F64 leftRectToPixel = transformRectToPixel_F64(intrinsic, rect1 );
 
 		gui.configure(baseline, rectifiedK, leftRectToPixel, minDisparity, maxDisparity);
 		gui.process(disparity, left);
 		gui.setPreferredSize(new Dimension(left.getWidth(), left.getHeight()));
 
 		ShowImages.showWindow(gui, "Point Cloud");
 	}
 
 	/**
 	 * Draw inliers for debugging purposes.  Need to convert from normalized to pixel coordinates.
 	 */
 	public static void drawInliers(BufferedImage left, BufferedImage right, IntrinsicParameters intrinsic,
 								   List<AssociatedPair> normalized) {
 		PointTransform_F64 tran = LensDistortionOps.transformNormToRadial_F64(intrinsic);
 
 		List<AssociatedPair> pixels = new ArrayList<AssociatedPair>();
 
 		for (AssociatedPair n : normalized) {
 			AssociatedPair p = new AssociatedPair();
 
 			tran.compute(n.p1.x, n.p1.y, p.p1);
 			tran.compute(n.p2.x, n.p2.y, p.p2);
 
 			pixels.add(p);
 		}
 
 		// display the results
 		AssociationPanel panel = new AssociationPanel(20);
 		panel.setAssociation(pixels);
 		panel.setImages(left, right);
 
 		ShowImages.showWindow(panel, "Inlier Features");
 	}
 
 	public static void main( String args[] ) {
		AutoPointCloudApp app = new AutoPointCloudApp("192.168.1.30");
 
 		app.configureRobot();
 		app.moveRobotCaptureImages();
 		app.associateImages();
 		app.createPointCloud();
 	}
 
 }
