 package alpv_ws1112.ub1.webradio.communication;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
 import alpv_ws1112.ub1.webradio.communication.tcp.ServerTCP;
 
 public class ServerStreamer implements Runnable {
 
 	private static final int BUFFER_SIZE = 64;
 
 	private AudioInputStream _ais;
 	private AudioFormat _audioFormat;
 	private byte[] _musicBuffer;
 	private ServerTCP _server;
 	private String _path;
 
 	public ServerStreamer(ServerTCP server, String path) throws IOException,
 			UnsupportedAudioFileException {
 		_server = server;
 		_path = path;
 		_musicBuffer = new byte[BUFFER_SIZE];
 		changePath(path);
 	}
 
 	/**
 	 * Runs the streamer
 	 */
 	public void run() {
 		try {
 			if (_ais.read(_musicBuffer) <= 0) {
				System.out.println("Repeat.");
 				_ais = AudioPlayer.getAudioInputStream(_path);
 				_ais.read(_musicBuffer);
 			}
 			_server.resetBarrier();
 		} catch (IOException e) {
 			System.err.println("IO-Error while reading the file.");
 			_server.close();
 		} catch (UnsupportedAudioFileException e) {
 			System.err.println("Unsupported file type.");
 			_server.close();
 		}
 	}
 
 	/**
 	 * Returns the current buffer
 	 * 
 	 * @return
 	 */
 	public byte[] getBuffer() {
 		return _musicBuffer;
 	}
 
 	/**
 	 * Returns the current audio format
 	 * 
 	 * @return
 	 */
 	public AudioFormat getAudioFormat() {
 		return _audioFormat;
 	}
 
 	/**
 	 * Sets the close flag
 	 */
 	public void close() {
 		try {
 			_ais.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Change the path of the current stream
 	 * 
 	 * @param path
 	 * @throws IOException
 	 * @throws UnsupportedAudioFileException
 	 * @throws MalformedURLException
 	 */
 	public void changePath(String path) throws MalformedURLException,
 			UnsupportedAudioFileException, IOException {
 		AudioInputStream ais = AudioPlayer.getAudioInputStream(path);
 		if(_audioFormat != null && !ais.getFormat().equals(_audioFormat)) {
 			System.err.println("It is not possible at the moment to change the audio format of the stream.");
 			System.err.println("Old format: " + _audioFormat.toString());
 			System.err.println("New format: " + ais.getFormat().toString());
 		}
 		else {
 			_ais = ais;
 			_audioFormat = ais.getFormat();
 			_ais.read(_musicBuffer);
 		}
 	}
 }
