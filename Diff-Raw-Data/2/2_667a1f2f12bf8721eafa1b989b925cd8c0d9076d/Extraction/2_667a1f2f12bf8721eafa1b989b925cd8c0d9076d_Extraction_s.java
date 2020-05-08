 package mensonge.core;
 
 import it.sauronsoftware.jave.AudioAttributes;
 import it.sauronsoftware.jave.Encoder;
 import it.sauronsoftware.jave.EncoderException;
 import it.sauronsoftware.jave.EncodingAttributes;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 /**
  * Classe gérant l'extraction d'échantillons ou intervalle d'un flux audio d'un fichier multimédia
  * 
  */
 public class Extraction implements IExtraction
 {
 	private static final int BUFFER_LENGTH = 1024;
 	private static Logger logger = Logger.getLogger("logger");
 
 	public static void main(String args[])
 	{
 		Extraction ext = new Extraction();
 		logger.setLevel(Level.INFO);
 		logger.log(Level.INFO, "[+] Début de l'extraction de l'intervalle");
 
 		try
 		{
 			FileOutputStream dataOut = new FileOutputStream("sons/test_sortie.wav");
 			byte[] e = ext.extraireIntervalle("sons/test.wmv", 0, 20000);
 			dataOut.write(e, 0, e.length);
 			dataOut.close();
 
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (EncoderException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		logger.log(Level.INFO, "[+] Fin de l'extraction de l'intervalle");
 		/*
 		 * 
 		 * double sinusoid[] = new double[1024]; for(int i=0;i<1024;i++) { sinusoid[i] = Math.sin(i); } long start =
 		 * System.currentTimeMillis(); double intervalles[] = ext.extraireEchantillons(new File("sons/test.wav"));
 		 * //double intervalles[] = sinusoid;
 		 * 
 		 * 
 		 * 
 		 * FFTW3Library fftw = FFTW3Library.INSTANCE; System.out.println("Interval length: "+intervalles.length); int
 		 * n0_in = 2; int n1_in = intervalles.length/2; int n_in = intervalles.length;
 		 * System.out.println("N_in: "+n_in); System.out.println("N0_in: "+n0_in); System.out.println("N1_in: "+n1_in);
 		 * int n_out = n0_in*(2*(n1_in/2+1)); System.out.println("N_out: "+n_out);
 		 * 
 		 * int inBytes = (Double.SIZE/Byte.SIZE)*n_in; int outBytes = (Double.SIZE/Byte.SIZE)*n_out; Pointer in =
 		 * fftw.fftw_malloc(new NativeLong(inBytes)); Pointer out = fftw.fftw_malloc(new NativeLong(outBytes));
 		 * DoubleBuffer inbuf = in.getByteBuffer(0, inBytes).asDoubleBuffer(); DoubleBuffer outbuf =
 		 * out.getByteBuffer(0, outBytes).asDoubleBuffer(); int flags = FFTW3Library.FFTW_ESTIMATE;
 		 * 
 		 * FFTW3Library.fftw_plan planForward = fftw.fftw_plan_dft_r2c_2d(2,n1_in, inbuf, outbuf, flags); // Real to
 		 * complex
 		 * 
 		 * double dest[] = new double[n_out]; inbuf.put(intervalles);
 		 * fftw.fftw_execute_dft_r2c(planForward,inbuf,outbuf); outbuf.get(dest); for(int i =0;i<n_out;i++) {
 		 * System.out.println(Math.round(dest[i])); }
 		 */
 		/*
 		 * 
 		 * npts = number of pointsint 795 octave_fftw::fft (const double *in, Complex *out, size_t npts, 796 size_t
 		 * nsamples, octave_idx_type stride, octave_idx_type dist) 797 { 798 dist = (dist < 0 ? npts : dist); 799 800
 		 * dim_vector dv (npts, 1); 801 fftw_plan plan = octave_fftw_planner::create_plan (1, dv, nsamples, 802 stride,
 		 * dist, in, out); 803 804 fftw_execute_dft_r2c (plan, (const_cast<double *>(in)), 805
 		 * reinterpret_cast<fftw_complex *> (out)); 806 807 // Need to create other half of the transform. 808 809
 		 * convert_packcomplex_1d (out, nsamples, npts, stride, dist); 810 811 return 0; 812 }
 		 */
 		/*
 		 * System.out.println(intervalles[11000][0]);System.out.println(intervalles[11000][1]);
 		 */
 		// System.out.println("Done in "+(System.currentTimeMillis()-start)/1000.0+"s !");
 	}
 
 	/**
 	 * Extrait les échantillons audio d'un fichier multimédia
 	 * 
 	 * @param fichier
 	 *            Fichier multimédia où extraire les échantillons du premier flux audio trouvé
 	 * @return Un tableau de double contenant les échantillons
 	 * @throws IOException
 	 * @throws UnsupportedAudioFileException
 	 */
 	public double[][] extraireEchantillons(String filePath) throws IOException, UnsupportedAudioFileException
 	{
 		AudioInputStream inputAIS = AudioSystem.getAudioInputStream(new File(filePath));
 		AudioFormat audioFormat = inputAIS.getFormat();
 		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
 
 		int nBufferSize = BUFFER_LENGTH * audioFormat.getFrameSize();
 
 		byte[] abBuffer = new byte[nBufferSize];
 		int nBytesRead = -1;
 		while ((nBytesRead = inputAIS.read(abBuffer)) != -1)
 		{
 
 			byteOutput.write(abBuffer, 0, nBytesRead);
 		}
 
 		byte[] audioBytes = byteOutput.toByteArray();
 		int nbChannels = audioFormat.getChannels();
 		int nbSamples = audioBytes.length / nbChannels;
 		double doubleArray[] = new double[nbSamples];
 
 		for (int i = 0; i < nbSamples; i++)
 		{
 			int lsb = audioBytes[2 * i];
 			int msb = audioBytes[2 * i + 1];
 			doubleArray[i] = ((msb << 8) | (0xff & lsb)) / 32768.0d;
 			// Si c'est du 16bit ça ira de -32768 à +32767 donc pour avoir des double on divise par 32768 ça ira
 			// donc de -1 à +1
 		}
 
 		if ((nbSamples % nbChannels) != 0)
 		{
 			logger.log(Level.WARNING, "Les données audio ne correspondent pas au nombre de canaux");
 			return null;
 		}
 		int nbSamplesChannel = nbSamples / nbChannels;
		return reshape(doubleArray, nbChannels, nbSamplesChannel);
 	}
 
 	/**
 	 * Transforme un vecteur (tableau à une dimension) en tableau à 2 dimension
 	 * 
 	 * @param doubleArray
 	 *            Vecteur qui sera restructuré
 	 * @param n
 	 *            Nombre de lignes
 	 * @param m
 	 *            Nombre de colonnes
 	 * @return Un tableau à 2 dimensions fait à partir du vecteur en entrée
 	 */
 	private double[][] reshape(double doubleArray[], int n, int m)
 	{
 		double reshapeArray[][] = new double[n][m];
 		int k = 0;
 		for (int i = 0; i < n; i++)
 		{
 			for (int j = 0; j < m; j++)
 			{
 				reshapeArray[i][j] = doubleArray[k++];
 			}
 		}
 		return reshapeArray;
 	}
 
 	/**
 	 * Extrait le flux audio d'un fichier multimédia et le converti en WAV, format PCM Signé 16 bit little endian
 	 * 
 	 * @param fichier
 	 *            Fichier multimédia où extraire l'intervalle défini du premier flux audio trouvé
 	 * @param debut
 	 *            La borne de début de l'intervalle en millisecondes où commencer l'extraction
 	 * @param fin
 	 *            La borne de fin de l'intervalle en millisecondes où terminer l'extraction
 	 * @return Un tableau d'octet contenant le fichier WAV
 	 */
 	public byte[] extraireIntervalle(String filePath, float debut, float fin) throws IOException, EncoderException
 	{
 		File source = new File(filePath);
 		File target = File.createTempFile("tempFile", ".wav");
 		target.deleteOnExit();
 		AudioAttributes audio = new AudioAttributes();
 		audio.setCodec("pcm_s16le");
 
 		EncodingAttributes attrs = new EncodingAttributes();
 		attrs.setFormat("wav");
 		attrs.setAudioAttributes(audio);
 		attrs.setOffset(debut / 1000);
 		attrs.setDuration(fin / 1000);
 
 		Encoder encoder = new Encoder();
 		encoder.encode(source, target, attrs);
 
 		byte[] data = new byte[(int) target.length()];
 		FileInputStream fis = new FileInputStream(target);
 		fis.read(data);
 		fis.close();
 
 		return data;
 	}
 }
