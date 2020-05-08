 package ch.epfl.flamemaker.concurrent;
 
 import static org.bridj.Pointer.allocateFloats;
 import static org.bridj.Pointer.allocateInts;
 import static org.bridj.Pointer.allocateLongs;
 
 import java.nio.ByteOrder;
 import java.util.List;
 import java.util.Random;
 
 import org.bridj.Pointer;
 
 import ch.epfl.flamemaker.flame.FlameAccumulator;
 import ch.epfl.flamemaker.flame.FlameTransformation;
 import ch.epfl.flamemaker.geometry2d.AffineTransformation;
 import ch.epfl.flamemaker.geometry2d.Rectangle;
 
 import com.nativelibs4java.opencl.CLBuffer;
 import com.nativelibs4java.opencl.CLContext;
 import com.nativelibs4java.opencl.CLEvent;
 import com.nativelibs4java.opencl.CLKernel;
 import com.nativelibs4java.opencl.CLMem.Usage;
 import com.nativelibs4java.opencl.CLQueue;
 
 
 /**
  * Implémente une flame avec un rendu par OpenCL.
  * @author Hadrien
  *
  */
 public class OpenCLFlame extends Flame {
 	
	private static final int MAX_KERNEL_COUNT = 49152;
 	
 	private static final int INLINE_TRANSFO_LENGTH = 13;
 	
 	private CLContext m_context;
 	private CLKernel m_kernel;
 	private CLQueue m_queue;
 	
 	public OpenCLFlame(List<FlameTransformation> transforms, CLContext context, CLKernel computeKernel, CLQueue queue) {
 		super(transforms);
 
 		m_context = context;
 		m_kernel = computeKernel;
 		m_queue = queue;
 	}
 	
 	@Override
 	protected FlameAccumulator doCompute(final Rectangle frame, final int width, final int height,
 			final int density) {
 		
		int kernel_count = Math.min(MAX_KERNEL_COUNT, density*width*height);
		
         ByteOrder byteOrder = m_context.getByteOrder();
         
 		Pointer<Float> mapPtr = allocateFloats(width*height).order(byteOrder);
 		Pointer<Integer> intensitiesPtr = allocateInts(width*height).order(byteOrder);
 		Pointer<Float> trnsPtr = allocateFloats((getTransforms().size() + 1) * INLINE_TRANSFO_LENGTH).order(byteOrder);
 		Pointer<Float> ptsPtr = allocateFloats(kernel_count*3).order(byteOrder);
 		
 		float[] transforms = inlineTransforms(AffineTransformation.newScaling(
 				(double) width / frame.width(),
 				(double) height / frame.height()).composeWith(
 				AffineTransformation.newTranslation(-frame.left(),
 						-frame.bottom())));
 		
 		trnsPtr.setFloats(transforms);
 		
 		CLBuffer<Float> map = m_context.createFloatBuffer(Usage.InputOutput, mapPtr);
 		CLBuffer<Integer> intensities = m_context.createIntBuffer(Usage.InputOutput, intensitiesPtr);
 		CLBuffer<Float> transformsBuffer = m_context.createFloatBuffer(Usage.Input, trnsPtr);
 		CLBuffer<Float> pointsBuffer = m_context.createFloatBuffer(Usage.InputOutput, ptsPtr);
 		
 		Pointer<Long> seedsPtr = allocateLongs(kernel_count).order(byteOrder);
 		
 		generateSeeds(seedsPtr, kernel_count);
 		CLBuffer<Long> seeds = m_context.createBuffer(Usage.InputOutput, seedsPtr);
   
		int iterations = Math.max(1, density*width*height/kernel_count/50);
 		int kernel_iterations = density*width*height/kernel_count/iterations;
		System.out.println("it : "+iterations);
 		int percent = 0;
 		
 		CLEvent computeEvt = null;
 		//Calcul de la fractale avec l'algorithme du chaos
 		for(int i = 0 ; i < iterations && !isAborted() ; i++){
 			m_kernel.setArgs(
 	      		seeds, 
 	      		map, 
 	      		intensities, 
 	      		width, 
 	      		height, 
 	      		transformsBuffer, 
 	      		getTransforms().size(), 
 	      		kernel_iterations, 
 	      		pointsBuffer );
 			
 			computeEvt = m_kernel.enqueueNDRange(m_queue, new int[] { kernel_count });
 			
 			if(100*i/iterations > percent+4){
 				percent = 100*i/iterations;
 				triggerComputeProgress(percent);
 			}
 		}
 		
 		intensitiesPtr = intensities.read(m_queue, computeEvt);
 		
 		mapPtr = map.read(m_queue);
 		
 		//On libère les ressources inutiles
 		seeds.release();
 		seedsPtr.release();
 		transformsBuffer.release();
 		
 		if(isAborted()){
 			intensitiesPtr.release();
 			mapPtr.release();
 			return null;
 		}
 		
 		// préparation des valeurs obtenues pour créer un accumulateur
 		int[][] hitCountArray = new int[width][height];
 		double[][] colorArray = new double[width][height];
 		
 		for(int i = 0 ; i < width ; i++){
 			
 			for(int j = 0 ; j < height ; j++){
 	        	hitCountArray[i][height - j - 1] = intensitiesPtr.get(i+j*width);
 	        	colorArray[i][height - j - 1] = mapPtr.get(i+j*width);
 			}
         }
 		
 		
 		intensitiesPtr.release();
 		mapPtr.release();
 		
 		
 		triggerComputeProgress(100);
 		
 		
 		FlameAccumulator ret = new FlameAccumulator(hitCountArray, colorArray);		
 		
 		return ret;
 	}
 	
 	/**
 	 * Convertit les transformations en un tableau de floats utilisable par le kernel OpenCL.
 	 */
 	private float[] inlineTransforms(AffineTransformation cameraTransform){
 		
 		List<FlameTransformation> transforms = getTransforms();
 		
 		transforms.add(new FlameTransformation(cameraTransform, new double[] {1,0,0,0,0,0}));
 		
 		float[] ret = new float[transforms.size() * 13];
 		
 		for(int i = 0 ; i < transforms.size() ; i++){
 			int offset = i*13;
 			FlameTransformation trns = transforms.get(i);
 			
 			double[] coeffs = trns.affineTransformation().getMatrixCoeffs();
 			for(int j = 0 ; j < 6 ; j++){
 				ret[offset+j] = (float) coeffs[j];
 			}
 			
 			ret[offset+6] = (float) getColorIndex(i);
 			
 			double[] weights = trns.weights();
 			for(int j = 0 ; j < 6 ; j++){
 				ret[offset+7+j] = (float) weights[j];
 			}
 		}
 		
 		return ret;
 	}
     
 	private void generateSeeds(Pointer<Long> seedsPtr, int n){
 		Random rand = new Random(2013);
     	for (int j = 0; j < n; j++) {
             seedsPtr.set(j,rand.nextLong());
         }
     }
 }
