 package mensonge.core;
 
 import it.sauronsoftware.jave.AudioAttributes;
 import it.sauronsoftware.jave.Encoder;
 import it.sauronsoftware.jave.EncoderException;
 import it.sauronsoftware.jave.EncodingAttributes;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import mensonge.core.tools.ExtractionObservable;
 
 /**
  * Classe gérant l'extraction d'échantillons ou intervalle d'un flux audio d'un fichier multimédia
  * 
  */
 public class Extraction extends ExtractionObservable implements IExtraction
 {
 	private static final int BUFFER_LENGTH = 1024;
 	private static Logger logger = Logger.getLogger("extraction");
 
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
 		return reshape(doubleArray, nbSamplesChannel, nbChannels);
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
 		notifyInProgressAction("Extraction de l'enregistrement selon l'intervalle défini...");
 		File source = new File(filePath);
 		File target = File.createTempFile("tempFile", ".wav");
 		target.deleteOnExit();
 		AudioAttributes audio = new AudioAttributes();
 		audio.setCodec("pcm_s16le");

 		EncodingAttributes attrs = new EncodingAttributes();
 		attrs.setFormat("wav");
 		attrs.setAudioAttributes(audio);
 		attrs.setOffset(debut / 1000);
 		attrs.setDuration((fin - debut) / 1000);
 
 		Encoder encoder = new Encoder();
 		encoder.encode(source, target, attrs);
 
 		byte[] data = new byte[(int) target.length()];
 		FileInputStream fis = new FileInputStream(target);
 		fis.read(data);
 		fis.close();
 		notifyCompletedAction("Extraction terminée");
 		return data;
 	}
 }
