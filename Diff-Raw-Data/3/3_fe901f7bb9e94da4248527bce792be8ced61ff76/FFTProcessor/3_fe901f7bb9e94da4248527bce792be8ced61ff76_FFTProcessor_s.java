 package in.waslos.kneo.libsimplefft;
 
 public class FFTProcessor{
 	public static final byte FFT_MODE_INVERSE = 1;
 	public static final byte FFT_MODE_NORMAL  = 0;
 
 	public static final byte CPLX_TYPE_SP  = 0;
 	public static final byte CPLX_TYPE_DP  = 1;
 	public static final byte CPLX_TYPE_INT = 2;
 	
 	static{
 		System.loadLibrary("simplefft");
 		System.loadLibrary("simplefft4j");	
 	}
 
 	private static native int createFFTContext(int samples, byte mode, byte type);
 	private static native void destroyFFTContext(int handle);
 
 	private static native void performFFTf(int handle, float[]  re, float[]  im);
 	private static native void performFFTd(int handle, double[] re, double[] im);
 	private static native void performFFTi(int handle, short[]  re, short[]  im);
 



 	public static int initializeFFT(int samples,byte mode, byte type){
 		return FFTProcessor.createFFTContext(samples,mode,type);
 	}
 
 	public static void destroyFFT(int handle){
 		FFTProcessor.destroyFFTContext(handle);
 	}
 
 	public static void performFFT(int handle, float[] re, float[] im){
 		performFFTf(handle,re,im);
 	}
 
 	public static void performFFT(int handle, double[] re, double[] im){
 		performFFTd(handle,re,im);
 	}
 
 	public static void performFFT(int handle, short[] re, short[] im){
 		performFFTi(handle,re,im);
 	}
 	
 	public static void main(String[] argV){
 
 		//System.loadLibrary("simplefft");		
 
 	
 		int size = 8;
 	
 		int handle = FFTProcessor.initializeFFT(size,FFTProcessor.FFT_MODE_NORMAL,FFTProcessor.CPLX_TYPE_INT);
 
 		short[] re = new short[size];
 		short[] im = new short[size];
 		
 		for(int i = 0;i<size;i++){
 			re[i] = (short)i;
 		}
 		
 		long time = System.currentTimeMillis();
 		performFFT(handle,re,im);
 		time = System.currentTimeMillis() - time;
 		System.out.println("runtime : "+time+"ms");
 		
 		for(int i = 0;i<size;i++){
 			System.out.printf("%d + %d * i\n",re[i],im[i]);
 		}
 		
 		System.out.println("handle retrieved : "+handle);
 	}
 }
 
