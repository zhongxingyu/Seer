 import net.schmizz.sshj.SSHClient;
 import net.schmizz.sshj.common.IOUtils;
 import net.schmizz.sshj.connection.ConnectionException;
 import net.schmizz.sshj.connection.channel.direct.Session;
 import net.schmizz.sshj.transport.TransportException;
 import net.schmizz.sshj.transport.verification.HostKeyVerifier;
 import net.schmizz.sshj.userauth.UserAuthException;
 
 import java.io.IOException;
 import java.security.PublicKey;
 import java.util.concurrent.TimeUnit;
 
 /**
  * IDC, 10/05/2012
  */
 
 public class TestLinuxSrv {
 
	private static final int TIMEOUT = Messages.getInt("TestLinuxSrv.TIMEOUT");
 	private static final String HOST = Messages.getString("TestLinuxSrv.HOST"); 
 	private static final String PORT = Messages.getString("TestLinuxSrv.PORT"); 
 	private static final String USER = Messages.getString("TestLinuxSrv.USER"); 
 	private static final String PWD = Messages.getString("TestLinuxSrv.PWD"); 
 
 	public static void main(String... args) throws IOException {
 		final SSHClient ssh = init();
 		executeTest(ssh);
 	}
 
 	public static SSHClient init() {
 		final SSHClient ssh = new SSHClient();
 		ssh.addHostKeyVerifier(new HostKeyVerifier() {
 			public boolean verify(String arg0, int arg1, PublicKey arg2) {
 				return true; // don't bother verifying
 			}
 		});
 		try {
 			ssh.connect(HOST, Integer.parseInt(PORT));
 		} catch (NumberFormatException e) {
 			System.out.println(Messages.getString("TestLinuxSrv.PORT_NUMBER_FORMAT_EXP"));
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println(Messages.getString("TestLinuxSrv.CONNECTION_NOT_POSSIBLE"));
 			e.printStackTrace();
 		}
 		return ssh;
 	}
 
 	public static void executeTest(final SSHClient ssh)
			throws UserAuthException, TransportException, ConnectionException,
			IOException {
 		try {
 			ssh.authPassword(USER, PWD);
 			boolean exp = false;
 			do {
 				final Session session = ssh.startSession();
 				try {
 					final Session.Command cmd = session.exec(Messages.getString("TestLinuxSrv.LINUXCMD")); 
 					System.out.println(IOUtils.readFully(cmd.getInputStream())
 							.toString());
 					cmd.join(15, TimeUnit.SECONDS);
 					// System.out.println("\n** exit status: " +
 					// cmd.getExitStatus());
 					System.out.println(Messages.getString("TestLinuxSrv.END_OF_CYCLE_SIGN"));
 					try {
 						Thread.sleep(TIMEOUT);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				} catch (ConnectionException ex) {
 					System.out.println(Messages.getString("TestLinuxSrv.CONNEXP")); 
 					ex.printStackTrace();
 				} finally {
 					System.out.println(Messages.getString("TestLinuxSrv.FINAL_LOOP")); 
 					session.close();
 				}
 			} while (exp == false);
		} finally {
 			System.out.println(Messages.getString("TestLinuxSrv.FINAL_APP"));
			ssh.disconnect();
 		}
 	}
 }
