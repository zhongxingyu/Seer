 package sat.utils.crypto;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigInteger;
 
 /**
  * Flux de décryptage RSA.
  */
 public class RSAInputStream extends InputStream {
 	/**
 	 * Le flux interne depuis lequel les données cryptées seront lues.
 	 */
 	protected InputStream in;
 
 	/**
 	 * La paire de clés utilisée pour décrypter les données. Seule la composante
 	 * privée de clé sera utilisée.
 	 */
 	protected RSAKeyPair keys;
 
 	/**
 	 * La taille des blocs de données. Elle est déterminée en fonction de la
 	 * taille des clés utilisées.
 	 */
 	protected int blockSize;
 
 	/**
 	 * La taille du buffer de lecture. Le buffer est égale à la taille des blocs
 	 * moins 4.
 	 */
 	protected int bufferSize;
 
 	/**
 	 * La longueur effective du buffer de lecture.
 	 */
 	protected int bufferLength = 0;
 
 	/**
 	 * La position actuelle dans le buffer de lecture.
 	 */
 	protected int bufferPosition = 0;
 
 	/**
 	 * Le buffer de lecture. Il est utilisée pour stocker un bloc entier de
 	 * données décryptées.
 	 */
 	protected byte[] buffer;
 
 	/**
 	 * Crée un flux de décryptage RSA.
 	 * 
 	 * @param in
 	 *            Le flux d'entrée depuis lequel les données cryptées seront
 	 *            lues.
 	 * @param keys
 	 *            La paire de clés utilisée pour le décryptage.
 	 */
 	public RSAInputStream(InputStream in, RSAKeyPair keys) {
 		this.in = in;
 		this.keys = keys;
 
 		blockSize = (keys.keyLength() / 8) + 1;
 		bufferSize = blockSize - 4;
 
 		buffer = new byte[bufferSize];
 	}
 
 	/**
 	 * Indique le nombre de bytes restant dans le buffer de lecture avant de
 	 * nécessiter une lecture supplémentaire du flux interne.
 	 */
 	public int available() throws IOException {
 		return bufferLength - bufferPosition;
 	}
 
 	/**
 	 * Charge un bloc de données cryptées depuis le flux interne, le décrypte et
 	 * le place dans le tampon de lecture.
 	 * 
 	 * @throws IOException
 	 *             La lecture du flux interne peut déclancher une exception.
 	 *             Celle-ci est passée au code appelant. De plus, une erreur de
 	 *             décryptage provoque le rejet complet du paquet accompagné
 	 *             d'une exception.
 	 * 
 	 *             Si une exception est levée, le tampon de lecture ne change
 	 *             pas d'état et une nouvelle tentative de lecture déclanchera
 	 *             un nouveau chargement depuis le flux interne.
 	 */
 	protected void load() throws IOException {
 		byte[] block = new byte[blockSize];
 
 		for(int i = 0; i < blockSize; i++) {
 			block[i] = (byte) (in.read() & 0xff);
 		}
 
 		block = keys.decrypt(new BigInteger(block)).toByteArray();
 
 		// BigInteger outputs:
 		//
 		//      [----key---] -> l = n-1
 		//    [----block---] -> l = n
 		//
 		// 1) [0|pad|0|buff] -> l = n
 		// 2)   [pad|0|buff] -> l = n-1
 
 		int drop = block[0] == 0 ? 1 : 0;
 		int bufferOffset = -1;
 
 		for(int i = drop; i < block.length; i++) {
 			if(block[i] == 0) {
 				bufferOffset = i + 1;
 				break;
 			}
 		}
 
 		// No-match -> bad block
 		if(bufferOffset < 0)
 			throw new IOException("Invalid RSA block");
 
 		// Buffer-reset
 		bufferLength = block.length - bufferOffset;
 		bufferPosition = 0;
 
 		// Copy bytes
 		System.arraycopy(block, bufferOffset, buffer, 0, bufferLength);
 	}
 
 	/**
 	 * Lis un byte décrypté depuis le flux interne. Cette méthode utilise le
 	 * tampon de lecture du flux. Si le tampon est vide, un chargement depuis le
 	 * flux interne sera lancé.
 	 */
 	public int read() throws IOException {
 		// Si tous les bytes du buffer de lecture ont été lus.
 		if(bufferPosition >= bufferLength)
 			load();
 
		return buffer[bufferPosition++] & 0xff;
 	}
 }
