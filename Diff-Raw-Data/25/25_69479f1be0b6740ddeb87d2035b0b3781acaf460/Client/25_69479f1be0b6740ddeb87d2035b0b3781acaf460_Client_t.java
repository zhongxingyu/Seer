 package talkingnet.examples.simpleconference;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import talkingnet.audiodevices.defaults.DefaultAudioSink;
 import talkingnet.audiodevices.defaults.DefaultAudioSource;
 import talkingnet.codecs.g711.G711UlawCompressor;
 import talkingnet.codecs.g711.G711UlawDecompressor;
 import talkingnet.core.Pool;
 import talkingnet.core.Pump;
 import talkingnet.net.udp.UdpBin;
 import talkingnet.net.udp.UdpDataFilter;
import talkingnet.utils.audio.DefaultAudioFormat;
 
 /**
  *
  * @author Alexander Oblovatniy <oblovatniy@gmail.com>
  */
 public class Client {
 
    private int bufferLengthInMillis = 48;
    private int bufferLength;
     
     private DefaultAudioSource src;
     private Pump pump;
     private G711UlawCompressor compressor;
     
     private UdpBin udpBin;
     
     private UdpDataFilter filter;    
     private G711UlawDecompressor decompressor;
     private Pool pool;
     private DefaultAudioSink sink;
 
     public Client(SocketAddress localAddress, SocketAddress remoteAddress) {
         
        bufferLength = DefaultAudioFormat.SAMPLING_RATE * DefaultAudioFormat.FRAME_SIZE;
        bufferLength /= (1000/bufferLengthInMillis);
         bufferLength -= bufferLength % 2;
         
         pool = new Pool("pool");
         sink = new DefaultAudioSink(bufferLength, pool, "audioSink");
         decompressor = new G711UlawDecompressor(pool, "decompressor");
        filter = new UdpDataFilter(remoteAddress, decompressor, "filter");
         
         int udpDataLength = bufferLength / G711UlawCompressor.COMPRESSION_RATE;        
         udpBin = new UdpBin(localAddress, remoteAddress, filter, udpDataLength, "udpBin");
         
         compressor = new G711UlawCompressor(udpBin, "compressor");
         pump = new Pump(compressor, "pump");
         src = new DefaultAudioSource(bufferLength, pump, "audioSrc");
         
         runPipeLine();
     }
     
     private void runPipeLine(){
         try {
             sink.open();
             src.open();
             
             pump.start();
             sink.start();
             
             udpBin.start();
             
             src.start();
         } catch (Exception ex) {
             System.out.println(ex);
         }
     }
 
     private static boolean argsValidationFail = false;
 
     public static void main(String[] args) {
         validateArgs(args);
         if (argsValidationFail) {
             return;
         }
         
         int port = Integer.parseInt(args[1]);
         SocketAddress localAddress = new InetSocketAddress(args[0], port);
         
         port = Integer.parseInt(args[3]);
         SocketAddress remoteAddress = new InetSocketAddress(args[2], port);
         
         new Client(localAddress, remoteAddress);
     }
     
     private static void validateArgs(String[] args){
         
         if (args.length<4){
             System.out.println("Not enough parameters! You must provide following:");
             System.out.println("\tlocal_address local_port remote address remote_port");
             argsValidationFail = true;
             return;
         }
         
         String tmp = args[0];
         
         try {
             InetAddress.getByName(tmp);
         } catch (Exception e) {
             System.out.println("'local_address' value is invalid: "+tmp);
             argsValidationFail = true;
         }
         
         tmp = args[1];
         
         try {
             Integer.parseInt(tmp);
         } catch (Exception e) {
             System.out.println("'local_port' value is invalid: "+tmp);
             argsValidationFail = true;
         }
         
         tmp = args[2];
         
         try {
             InetAddress.getByName(tmp);
         } catch (Exception e) {
             System.out.println("'remote_address' value is invalid: "+tmp);
             argsValidationFail = true;
         }
         
         tmp = args[3];
         
         try {
             Integer.parseInt(tmp);
         } catch (Exception e) {
             System.out.println("'remote_port' value is invalid: "+tmp);
             argsValidationFail = true;
         }
     }
     
     
 }
