 package za.ac.sun.cs.intlola.processing;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 import za.ac.sun.cs.intlola.Intlola;
 import za.ac.sun.cs.intlola.file.Const;
 import za.ac.sun.cs.intlola.file.FileUtils;
 
 import com.google.gson.JsonObject;
 
 public class SessionEnder implements Runnable {
 	private final InputStream	rcv;
 	private final OutputStream	snd;
 	private final Socket		sock;
 
 	SessionEnder(final Socket sock, final OutputStream snd,
 			final InputStream rcv) {
 		this.sock = sock;
 		this.snd = snd;
 		this.rcv = rcv;
 	}
 
 	private void closeConnection() throws IOException {
 		if (snd != null) {
 			snd.close();
 		}
 		if (rcv != null) {
 			rcv.close();
 		}
 		if (sock != null) {
 			sock.close();
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			final JsonObject params = new JsonObject();
 			params.addProperty(Const.REQ, Const.LOGOUT);
 			snd.write(params.toString().getBytes());
			snd.write(Const.EOF);
 			snd.flush();
 			final byte[] buffer = new byte[FileUtils.BUFFER_SIZE];
 			rcv.read(buffer);
 			final String received = new String(buffer);
 			if (!received.startsWith(Const.OK)) {
 				Intlola.log(null, received);
 			}
 		} catch (final IOException e) {
 			Intlola.log(e);
 		} finally {
 			try {
 				closeConnection();
 			} catch (final IOException e) {
 				Intlola.log(e, "Logout error");
 			}
 		}
 	}
 }
