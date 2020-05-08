 package nl.vu.cs.cn.transmission;
 
 
 import java.io.UnsupportedEncodingException;
 
 import nl.vu.cs.cn.TestBase;
 import nl.vu.cs.cn.tcp.TransmissionControlBlock;
 
 public class TestTransmit extends TestBase {
 
     protected static final String[] MESSAGES = {
             "This is the first message",
             "This is another message that I'm sending",
             "Ру́сский язы́к относится к восточной группе славянских языков, принадлежащих индоевропейской се" +
                     "мье языков. Он является официальным языком России, Беларуси, Казахстана и Кыргызстана, " +
                     "а также региональным в некоторых регионах Украины. Также является восьмым языком в мире по " +
                     "численности владеющих им как родным и пятым языком в мире по общей численности говорящих[18]. " +
                     "Русский язык — национальный язык русского народа, основной язык международного общения в " +
                     "центральной Евразии, в Восточной Европе, в странах бывшего Советского Союза, один из рабочих " +
                     "языков ООН. Он является наиболее распространённым славянским языком и самым распространённым " +
                     "языком в Европе — географически и по числу носителей языка как родного. Занимает четвёртое место " +
                     "среди самых переводимых языков, а также — седьмое место среди языков, на которые переводится " +
                     "большинство книг.[19] В 2013 году русский язык вышел на второе место среди самых популярных " +
                     "языков Интернета[20].",
             "འགྲོ་བ་མིའི་རིགས་རྒྱུད་ཡོངས་ལ་སྐྱེས་ཙམ་ཉིད་ནས་ཆེ་མཐོངས་དང༌། ཐོབ་ཐངགི་རང་དབང་འདྲ་མཉམ་དུ་ཡོད་ལ། ཁོང་ཚོར་རང་བྱུང་གི་བློ་རྩལ་དང་བསམ་ཚུལ་བཟང་པོ་འདོན་པའི་འོས་བབས་ཀྱང་ཡོད། " +
                     "དེ་བཞིན་ཕན་ཚུན་གཅིག་གིས་གཅིག་ལ་བུ་སྤུན་གྱི་འདུ་ཤེས་འཛིན་པའི་བྱ་སྤྱོད་ཀྱང་ལག་ལེན་བསྟར་དགོས་པ་ཡིན༎"};
 
     protected byte[][] data;
 
     public TestTransmit(){
         data = new byte[MESSAGES.length][];
         int i = 0;
         for(String msg : MESSAGES){
             try {
                 data[i] = msg.getBytes("UTF-8");
             } catch (UnsupportedEncodingException e) {
                 data[i] = msg.getBytes();
             }
             i++;
         }
     }
 
     public void testReadWrite() throws Exception {
         startServer(new ServerRunnable());
 
         // make sure we are connected (both client and server)
         boolean connected = clientSocket.connect(SERVER_IP_ADDR, SERVER_PORT);
 
         assertTrue("Expected clientSocket.connect() to return true", connected);
         assertEquals("After connect() client should be in the ESTABLISHED state",
                 TransmissionControlBlock.State.ESTABLISHED,
                 getClientState());
 
         Thread.sleep(1000);
 
         assertEquals("Server should never return before reaching the ESTABLISHED state",
                 TransmissionControlBlock.State.ESTABLISHED,
                 getServerState());
 
         for(byte[] buf : data){
             int bytesSent = clientSocket.write(buf, 0, buf.length);
             assertEquals("Expected all data to be sent", buf.length, bytesSent);
         }
 
         // start close procedure
         boolean closed = clientSocket.close();
 
         // test client part
         assertTrue("Expected clientSocket.close() to return true", closed);
         assertEquals("Client should be in CLOSED state",
                 TransmissionControlBlock.State.CLOSED,
                 getClientState());
 
         // test server part
         assertEquals("Server should be in CLOSED state",
                 TransmissionControlBlock.State.CLOSED,
                 getServerState());
     }
 
     protected class ServerRunnable implements Runnable {
 
         @Override
         public void run() {
             serverSocket.accept();
 
            // give server time to reply to client
             try {
                 Thread.sleep(5000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
 
             serverSocket.close();
         }
     }
 }
