 package aj.hadoop.monitor.mapreduce;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.text.*;
 import java.lang.*;
 import java.lang.management.*;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.*;
 import org.apache.hadoop.mapreduce.Mapper.Context;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 import org.aspectj.lang.annotation.After;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.Signature;
 
 import com.github.karahiyo.hanoi_picker.*;
 
 @Aspect
 public class MapperMonitor {
 
 	private static final String HOST = "localhost";
 	private static final int PORT = 9999;
 
 	/** server socket timeout(ms) */
 	public static final int    TIMEOUT_SERVER_SOCKET     = 500;
 
 	@Pointcut ("call(void org.apache.hadoop.mapreduce.Mapper.Context+.write" +
 			"(" +
 			"java.lang.Object, " +
 			"java.lang.Object, " +
 			"))" +
			"&& args(key, value)")
 	public void pointcut_mapper_out(Object key, Object value){}
 
 
 	@Before ( value = "pointcut_mapper_out( key, value )")
 	public void logging( JoinPoint thisJoinPoint,
 			Object key,
 			Object value) {
 
 		Socket echoSocket = null;
 		DataOutputStream os = null;
 		BufferedReader is = null;
 
 		// Socketの準備
 		try {
 			echoSocket = new Socket(HOST, PORT);
 			echoSocket.setSoTimeout(TIMEOUT_SERVER_SOCKET);		
 			os = new DataOutputStream( echoSocket.getOutputStream());
 			is = new BufferedReader( new InputStreamReader(echoSocket.getInputStream()));
 		} catch ( UnknownHostException e) {
 			System.err.println("Don't know about host: localhost");
 		} catch ( IOException e) {
 			System.err.println("Could't get I/O for the connection to: localhost");
 		}
 
 		// テストメッセージの送信
 		if ( echoSocket != null && os != null && is != null) {
 			try {
 				// メッセージの送信
 				String message = URLEncoder.encode((String)key, "UTF-8");
 				os.writeBytes(message + "\n");
 
 				// サーバーからのメッセージを受け取り、出力
 				String response;
 				if ( (response = is.readLine()) != null) {
 					System.out.println( "Server: " + response);
 				}
 
 				// 開いたソケットをクローズ
 				os.close();
 				is.close();
 				echoSocket.close();
 			} catch (UnknownHostException e) {
 				System.err.println("Trying to connect to unknown host: " + e);
 			} catch (IOException e) {
 				System.err.println("IOException: " + e);
 			}
 		}
 	}
 }
