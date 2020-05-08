 package mauroponce.pfi.recognition;
 import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
 import static com.googlecode.javacv.cpp.opencv_core.CV_32SC1;
 import static com.googlecode.javacv.cpp.opencv_core.CV_L1;
 import static com.googlecode.javacv.cpp.opencv_core.CV_STORAGE_READ;
 import static com.googlecode.javacv.cpp.opencv_core.CV_STORAGE_WRITE;
 import static com.googlecode.javacv.cpp.opencv_core.CV_TERMCRIT_ITER;
 import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
 import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
 import static com.googlecode.javacv.cpp.opencv_core.cvAttrList;
 import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
 import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
 import static com.googlecode.javacv.cpp.opencv_core.cvCreateMat;
 import static com.googlecode.javacv.cpp.opencv_core.cvNormalize;
 import static com.googlecode.javacv.cpp.opencv_core.cvOpenFileStorage;
 import static com.googlecode.javacv.cpp.opencv_core.cvReadByName;
 import static com.googlecode.javacv.cpp.opencv_core.cvReadIntByName;
 import static com.googlecode.javacv.cpp.opencv_core.cvReadStringByName;
 import static com.googlecode.javacv.cpp.opencv_core.cvRect;
 import static com.googlecode.javacv.cpp.opencv_core.cvReleaseFileStorage;
 import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
 import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
 import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
 import static com.googlecode.javacv.cpp.opencv_core.cvSize;
 import static com.googlecode.javacv.cpp.opencv_core.cvTermCriteria;
 import static com.googlecode.javacv.cpp.opencv_core.cvWrite;
 import static com.googlecode.javacv.cpp.opencv_core.cvWriteInt;
 import static com.googlecode.javacv.cpp.opencv_core.cvWriteString;
 import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
 import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
 import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
 import static com.googlecode.javacv.cpp.opencv_legacy.CV_EIGOBJ_NO_CALLBACK;
 import static com.googlecode.javacv.cpp.opencv_legacy.cvCalcEigenObjects;
 import static com.googlecode.javacv.cpp.opencv_legacy.cvEigenDecomposite;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import mauroponce.pfi.domain.IndexDistance;
 import mauroponce.pfi.utils.AppConstants;
 import mauroponce.pfi.utils.FileUtils;
 
 import com.googlecode.javacpp.FloatPointer;
 import com.googlecode.javacpp.Pointer;
 import com.googlecode.javacpp.PointerPointer;
 import com.googlecode.javacv.cpp.opencv_core.CvFileStorage;
 import com.googlecode.javacv.cpp.opencv_core.CvMat;
 import com.googlecode.javacv.cpp.opencv_core.CvRect;
 import com.googlecode.javacv.cpp.opencv_core.CvSize;
 import com.googlecode.javacv.cpp.opencv_core.CvTermCriteria;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
 public class FaceRecognizer {
 	
	final String DATA_PATH = "C:/Users/Mauro/Desktop/data";
 	IplImage[] trainingFaceImgArr;
 	private int nTrainFaces = 0;
 	CvMat projectedTrainFaceMat;
 	int nEigens = 0;
 	int nPersons = 0;
 	IplImage[] eigenVectArr;
 	CvMat eigenValMat;
 	IplImage pAvgTrainImg;
 	final List<String> personNames = new ArrayList<String>();
 	CvMat personNumTruthMat;
 	IplImage[] testFaceImgArr;
 
 	public void learn(final String [] imagesFoldersArray) {
 		trainingFaceImgArr = loadFaceImgArrayFromImagesFolders(imagesFoldersArray);
 		nTrainFaces = trainingFaceImgArr.length;
 		if (nTrainFaces < 3) {//Se necesitan al menos 3 imagenes de entrenamiento
 			return;
 		}
 		doPCA();
 		projectedTrainFaceMat = cvCreateMat(nTrainFaces, // rows
 				nEigens, // cols
 				CV_32FC1); // type, 32-bit float, 1 channel
 
 		for (int i1 = 0; i1 < nTrainFaces; i1++) {
 			for (int j1 = 0; j1 < nEigens; j1++) {
 				projectedTrainFaceMat.put(i1, j1, 0.0);
 			}
 		}
 		final FloatPointer floatPointer = new FloatPointer(nEigens);
 		for (int i = 0; i < nTrainFaces; i++) {
 			cvEigenDecomposite(trainingFaceImgArr[i],
 					nEigens,
 					new PointerPointer(eigenVectArr),
 					0,
 					null,
 					pAvgTrainImg,
 					floatPointer);
 			for (int j1 = 0; j1 < nEigens; j1++) {
 				projectedTrainFaceMat.put(i, j1, floatPointer.get(j1));
 			}
 		}
 		storeTrainingData();
 		storeEigenfaceImages();
 	}	
 	
 	private IplImage[] loadFaceImgArrayFromImagesFolders(
 			final String [] imagesFoldersArray) {
 		
 		IplImage[] faceImgArr;
 		
 		int iFace = 0;
 		int nFaces = 0;
 		int width = -1;
 		int height = -1;
 
 		nPersons = 0;
 		
 		
 		for(String folder : imagesFoldersArray){
 			File archivo = new File(AppConstants.TRAINING_IMAGES_ROOT_FOLDER + "/" + folder);
 			if(archivo.isDirectory()){
 				nFaces += archivo.listFiles().length;
 			}
 		}
 		
 		
 		/*for (final File personDir : topDir.listFiles()) {
 			if (personDir.isDirectory()) {
 				final File imagesDir = new File(imagesFolderPath + "/"
 						+ personDir.getName());
 				nFaces += imagesDir.listFiles().length;
 			}
 		}*/
 
 		// allocate the face-image array and person number matrix
 		faceImgArr = new IplImage[nFaces];
 		personNumTruthMat = cvCreateMat(1, // rows
 				nFaces, // cols
 				CV_32SC1); // type, 32-bit unsigned, one channel
 
 		// initialize the person number matrix - for ease of debugging
 		for (int j1 = 0; j1 < nFaces; j1++) {
 			personNumTruthMat.put(0, j1, 0);
 		}
 
 		personNames.clear(); // Make sure it starts as empty.
 
 		for(String imageFolderName : imagesFoldersArray){
 			File imageDirectory = new File(AppConstants.TRAINING_IMAGES_ROOT_FOLDER + "/" + imageFolderName);
 			int personNumber = 0;
 			if(imageDirectory.isDirectory()){
 				nPersons++;
 				String personName = imageFolderName;
 				personNumber = Integer.valueOf(personName);
 				personNames.add(personName);
 				
 				for (final File imgFile : imageDirectory.listFiles()) {
 					String imgFilename = imgFile.getAbsolutePath();
 					personNumTruthMat.put(0, iFace, personNumber);
 					final IplImage faceImage 
 						= cvLoadImage(imgFilename, CV_LOAD_IMAGE_GRAYSCALE);
 					if (faceImage == null) {
 						throw new RuntimeException("No se puede leer la imagen "
 								+ imgFilename);
 					}
 					if (width == -1) {
 						width = faceImage.width();
 						height = faceImage.height();
 					} else if (faceImage.width() != width
 							|| faceImage.height() != height) {
 						throw new RuntimeException("wrong size face in "
 								+ imgFilename + "\nwanted " + width + "x"
 								+ height + ", but found " + faceImage.width()
 								+ "x" + faceImage.height());
 					}
 					faceImgArr[iFace] = faceImage;
 					iFace++;
 				}
 			}
 		}
 		/*
 		// store the face images in an array
 		for (final File personDir : topDir.listFiles()) {
 			int personNumber = 0;
 			if (personDir.isDirectory()) {
 				nPersons++;
 				String personName = personDir.getName();
 				personNumber = Integer.valueOf(personName);
 				personNames.add(personName);
 				final File imagesDir = new File(imagesFolderPath + "/"
 						+ personDir.getName());
 				for (final File imgFile : imagesDir.listFiles()) {
 					String imgFilename = imgFile.getAbsolutePath();
 					personNumTruthMat.put(0, iFace, personNumber);
 					final IplImage faceImage 
 						= cvLoadImage(imgFilename, CV_LOAD_IMAGE_GRAYSCALE);
 					if (faceImage == null) {
 						throw new RuntimeException("No se puede leer la imagen "
 								+ imgFilename);
 					}
 					if (width == -1) {
 						width = faceImage.width();
 						height = faceImage.height();
 					} else if (faceImage.width() != width
 							|| faceImage.height() != height) {
 						throw new RuntimeException("wrong size face in "
 								+ imgFilename + "\nwanted " + width + "x"
 								+ height + ", but found " + faceImage.width()
 								+ "x" + faceImage.height());
 					}
 					faceImgArr[iFace] = faceImage;
 					iFace++;
 				}
 			}
 		}
 		*/
 		System.out.println("Images: " + nFaces);
 		System.out.println("Persons: " + nPersons);
 		if (nPersons > 0) {
 			for (String pName : personNames) {
 				System.out.println(pName);
 			}
 		}
 
 		return faceImgArr;
 	}
 
 	private void doPCA() {
 		int i;
 		CvTermCriteria calcLimit;
 		CvSize faceImgSize = new CvSize();
 
 		nEigens = nTrainFaces - 1;
 		System.out.println("Eigenvalues: " + nEigens);
 		
 		faceImgSize.width(trainingFaceImgArr[0].width());
 		faceImgSize.height(trainingFaceImgArr[0].height());
 		eigenVectArr = new IplImage[nEigens];
 		for (i = 0; i < nEigens; i++) {
 			eigenVectArr[i] = cvCreateImage(faceImgSize, // size
 					IPL_DEPTH_32F, // depth
 					1); // channels
 		}
 
 		// allocate the eigenvalue array
 		eigenValMat = cvCreateMat(1, // rows
 				nEigens, // cols
 				CV_32FC1); // type, 32-bit float, 1 channel
 
 		// allocate the averaged image
 		pAvgTrainImg = cvCreateImage(faceImgSize, // size
 				IPL_DEPTH_32F, // depth
 				1); // channels
 
 		// set the PCA termination criterion
 		calcLimit = cvTermCriteria(CV_TERMCRIT_ITER, // type
 				nEigens, // max_iter
 				1); // epsilon
 
 		cvCalcEigenObjects(nTrainFaces, // nObjects
 				new PointerPointer(trainingFaceImgArr), // input
 				new PointerPointer(eigenVectArr), // output
 				CV_EIGOBJ_NO_CALLBACK, // ioFlags
 				0, // ioBufSize
 				null, // userData
 				calcLimit, pAvgTrainImg, // avg
 				eigenValMat.data_fl()); // eigVals
 		
 		cvNormalize(eigenValMat, // src (CvArr)
 				eigenValMat, // dst (CvArr)
 				1, // a
 				0, // b
 				CV_L1, // norm_type
 				null); // mask
 	}
 
 	private void storeTrainingData() {
 		CvFileStorage fileStorage;
 		fileStorage = cvOpenFileStorage(DATA_PATH + "/facedata.xml",
 				null,
 				CV_STORAGE_WRITE,
 				null);
 
 		cvWriteInt(fileStorage, "nPersons", nPersons);
 
 		for (int i = 0; i < nPersons; i++) {
 			String varname = "personName_" + (i + 1);
 			cvWriteString(fileStorage,
 					varname,
 					personNames.get(i),
 					0);
 		}
 		cvWriteInt(fileStorage, "nEigens", nEigens);
 		cvWriteInt(fileStorage, "nTrainFaces", nTrainFaces);
 		cvWrite(fileStorage, "trainPersonNumMat", personNumTruthMat, cvAttrList());
 		cvWrite(fileStorage, "eigenValMat", eigenValMat, cvAttrList());
 		cvWrite(fileStorage, "projectedTrainFaceMat", projectedTrainFaceMat, cvAttrList());
 		cvWrite(fileStorage, "avgTrainImg", pAvgTrainImg, cvAttrList());
 
 		for (int i = 0; i < nEigens; i++) {
 			String varname = "eigenVect_" + i;
 			cvWrite(fileStorage,
 					varname,
 					eigenVectArr[i],
 					cvAttrList());
 		}
 		cvReleaseFileStorage(fileStorage);
 	}
 	
 	public String getFacesData(final String [] imagesFoldersArray){
 		learn(imagesFoldersArray);
 		return FileUtils.fileToString(DATA_PATH + "/facedata.xml");
 	}
 	
 	private void storeEigenfaceImages() {
 		cvSaveImage(DATA_PATH + "/averageImage.bmp", pAvgTrainImg);
 		if (nEigens > 0) {
 			int COLUMNS = 8;
 			int nCols = Math.min(nEigens, COLUMNS);
 			int nRows = 1 + (nEigens / COLUMNS);
 			int w = eigenVectArr[0].width();
 			int h = eigenVectArr[0].height();
 			CvSize size = cvSize(nCols * w, nRows * h);
 			final IplImage bigImg = cvCreateImage(size, IPL_DEPTH_8U, 1);
 			for (int i = 0; i < nEigens; i++) {
 				IplImage byteImg = FileUtils.convertFloatImageToUcharImage(eigenVectArr[i]);
 				int x = w * (i % COLUMNS);
 				int y = h * (i / COLUMNS);
 				CvRect ROI = cvRect(x, y, w, h);
 				cvSetImageROI(bigImg, ROI);
 				cvCopy(byteImg, bigImg, null);
 				cvResetImageROI(bigImg);
 				cvReleaseImage(byteImg);
 			}
 			cvSaveImage(DATA_PATH + "/eigenfaces.bmp", bigImg);
 			cvReleaseImage(bigImg);
 		}
 	}
 
 	public void recognize(final String testImagePath, int n) {
 		int i = 0;
 		int nTestFaces = 0;
 		CvMat trainPersonNumMat;
 		float[] projectedTestFace;
 		float confidence = 0.0f;
 
 		testFaceImgArr = loadFaceImgArrayFromImagePath(testImagePath);
 		nTestFaces = testFaceImgArr.length;
 		trainPersonNumMat = loadTrainingData();
 		if (trainPersonNumMat == null) {
 			return;
 		}
 		projectedTestFace = new float[nEigens];
 
 		for (i = 0; i < nTestFaces; i++) {
 			int iNearest;
 			int nearest;
 
 			cvEigenDecomposite(testFaceImgArr[i], // obj
 					nEigens, // nEigObjs
 					new PointerPointer(eigenVectArr), // eigInput (Pointer)
 					0, // ioFlags
 					null, // userData
 					pAvgTrainImg, // avg
 					projectedTestFace); // coeffs
 
 			final FloatPointer pConfidence = new FloatPointer(confidence);
 			iNearest = getKNN(projectedTestFace, new FloatPointer(
 					pConfidence), n)[0];
 			
 			confidence = pConfidence.get();
 			nearest = trainPersonNumMat.data_i().get(iNearest);
 
 			System.out.println("Nearest: " + nearest);
 		}
 	}
 
 	private CvMat loadTrainingData() {
 		CvMat pTrainPersonNumMat = null; // the person numbers during training
 		CvFileStorage fileStorage;
 		int i;
 		fileStorage = cvOpenFileStorage(DATA_PATH + "/facedata.xml", // filename
 				null, // memstorage
 				CV_STORAGE_READ, // flags
 				null); // encoding
 		if (fileStorage == null) {
 			return null;
 		}
 
 		personNames.clear(); // Make sure it starts as empty.
 		nPersons = cvReadIntByName(fileStorage, // fs
 				null, // map
 				"nPersons", // name
 				0); // default_value
 		if (nPersons == 0) {
 			return null;
 		}
 		for (i = 0; i < nPersons; i++) {
 			String sPersonName;
 			String varname = "personName_" + (i + 1);
 			sPersonName = cvReadStringByName(fileStorage, // fs
 					null, // map
 					varname, "");
 			personNames.add(sPersonName);
 		}
 		nEigens = cvReadIntByName(fileStorage, // fs
 				null, // map
 				"nEigens", 0); // default_value
 		nTrainFaces = cvReadIntByName(fileStorage, null, // map
 				"nTrainFaces", 0); // default_value
 		Pointer pointer = cvReadByName(fileStorage, // fs
 				null, // map
 				"trainPersonNumMat", // name
 				cvAttrList()); // attributes
 		pTrainPersonNumMat = new CvMat(pointer);
 
 		pointer = cvReadByName(fileStorage, // fs
 				null, // map
 				"eigenValMat", // nmae
 				cvAttrList()); // attributes
 		eigenValMat = new CvMat(pointer);
 
 		pointer = cvReadByName(fileStorage, // fs
 				null, // map
 				"projectedTrainFaceMat", // name
 				cvAttrList()); // attributes
 		projectedTrainFaceMat = new CvMat(pointer);
 
 		pointer = cvReadByName(fileStorage, null, // map
 				"avgTrainImg", cvAttrList()); // attributes
 		pAvgTrainImg = new IplImage(pointer);
 
 		eigenVectArr = new IplImage[nEigens];
 		for (i = 0; i < nEigens; i++) {
 			String varname = "eigenVect_" + i;
 			pointer = cvReadByName(fileStorage, null, // map
 					varname, cvAttrList()); // attributes
 			eigenVectArr[i] = new IplImage(pointer);
 		}
 		cvReleaseFileStorage(fileStorage);
 		
 		return pTrainPersonNumMat;
 	}
 
 	private IplImage[] loadFaceImgArrayFromImagePath(final String imagePath) {
 		IplImage[] faceImgArr;
 		int iFace = 0;
 		int nFaces = 1;
 		int width = -1;
 		int height = -1;
 		nPersons = 0;
 
 		faceImgArr = new IplImage[nFaces];
 		personNumTruthMat = cvCreateMat(1, // rows
 				nFaces, // cols
 				CV_32SC1); // type, 32-bit unsigned, one channel
 
 		for (int j1 = 0; j1 < nFaces; j1++) {
 			personNumTruthMat.put(0, j1, 0);
 		}
 		nPersons = 1;
 		int personNumber = 0;
 		personNumTruthMat.put(0, // i
 				iFace, // j
 				personNumber); // v
 		final IplImage faceImage = cvLoadImage(imagePath, // filename
 				CV_LOAD_IMAGE_GRAYSCALE); // isColor
 		if (faceImage == null) {
 			throw new RuntimeException("Can't load image from " + imagePath);
 		}
 		if (width == -1) {
 			width = faceImage.width();
 			height = faceImage.height();
 		} else if (faceImage.width() != width || faceImage.height() != height) {
 			throw new RuntimeException("wrong size face in " + imagePath
 					+ "\nwanted " + width + "x" + height + ", but found "
 					+ faceImage.width() + "x" + faceImage.height());
 		}
 		faceImgArr[iFace] = faceImage;
 		iFace++;		
 		return faceImgArr;
 	}
 	/**Get the K nearest neighbors.*/
 	private int [] getKNN(float projectedTestFace[],
 			FloatPointer pConfidencePointer, int k) {
 		double leastDistSq = Double.MAX_VALUE;
 		int i = 0;
 		int iNearest = 0;
 		int [] nNearestIndexes = new int[k];
 		Set<IndexDistance> indexDistances = new TreeSet<IndexDistance>();
 		double [] distances = new double [nTrainFaces];
 		
 		for (int iTrain = 0; iTrain < nTrainFaces; iTrain++) {
 			double distSq = 0;
 
 			for (i = 0; i < nEigens; i++) {
 				if (!Double.isNaN(projectedTestFace[i])) {
 					float projectedTrainFaceDistance = (float) projectedTrainFaceMat
 							.get(iTrain, i);
 					float d_i = projectedTestFace[i]
 							- projectedTrainFaceDistance;
 					distSq += d_i * d_i;
 				}
 			}
 
 			if (distSq < leastDistSq) {
 				leastDistSq = distSq;
 				iNearest = iTrain;
 			}
 			distances[iTrain] = distSq;
 			indexDistances.add(new IndexDistance(iTrain, distSq));
 			System.out.println("iTrain: " + iTrain + ", distance: " + distSq);
 		}
 		System.out.println("Least: " + iNearest + ", Distance: "+ leastDistSq);
 		System.out.println("\n\nTreeSet\n\n");
 		
 		for(IndexDistance indexDistance : indexDistances){
 			System.out.println("Index: " + indexDistance.getIndex()
 					+ ", Distance: " + indexDistance.getDistance());
 		}
 		int j = 0;
 		for(Iterator<IndexDistance> it = indexDistances.iterator(); it.hasNext() && j < k; j++){
 			nNearestIndexes[j] = it.next().getIndex();
 		}
 		float pConfidence = (float) (1.0f - Math.sqrt(leastDistSq
 				/ (float) (nTrainFaces * nEigens)) / 255.0f);
 		pConfidencePointer.put(pConfidence);
 		
 		return nNearestIndexes;
 	}
 }
