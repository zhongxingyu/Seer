 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author yoga1290
  */
 
 
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Robot;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.HashSet;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 
 
 /**
  *
  * @author yoga1290
  */
 class FileRequest implements Runnable{
     public boolean send=true;
 
     FileRequest(Socket s){
      //   this.app=app;
         client = s;
     } //*/
     public void run(){
 
         if(requestRead()){
             if(fileOpened()){
                 constructHeader();
                 if(fileSent()){
 // app.display("*File: "+fileName+" File Transfer Complete*Bytes Sent:"+bytesSent+"\n");
                 }
             }
         }
           try{
             dis.close();
             client.close();
         }catch(Exception e){System.err.println(e);}
 
     }
 
 
     private boolean fileSent()
     {
         try{
 DataOutputStream clientStream = new DataOutputStream
 (new BufferedOutputStream(client.getOutputStream()));
             clientStream.writeBytes(header);
       //      app.display("******** File Request *********\n"+
           //              "******* "+ fileName +"*********\n"+header);
 //            int i;
 //            bytesSent = 0;
 //            while((i=requestedFile.read()) != -1){
 //                clientStream.writeByte(i);
 //                bytesSent++;
 //            }
             int i;
             byte buff[]=new byte[1000];
             while((i=requestedFile.read(buff))>-1)
                 clientStream.write(buff, 0, i);
             
             clientStream.flush();
             clientStream.close();
                  }catch(IOException e){System.err.println("SEND>"+e);return false;}
                  return true;
 
     }
     private boolean fileOpened()
     {
 //        ByteInputStream bis=new ByteInputStream("".getBytes(), 0);
   //      DataInputStream dis=new DataInputStream(bis);
         if(send)
         {
         try{
             if(fileName.indexOf(".")==-1)
                 requestedFile = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
             else //File inside the JAR file,contains .
                 requestedFile = new DataInputStream(new BufferedInputStream
                     ((this.getClass().getResourceAsStream("/"+fileName))));
             
             fileLength = requestedFile.available();
 
                 }catch(FileNotFoundException e){
                 if(fileName.equals("filenfound.html")){return false;}
                 fileName="filenfound.html";
                 if(!fileOpened()){return false;}
             }catch(Exception e){System.err.println("Open:"+fileName+">>"+e);return false;}
 
         }
         else
         {
             try{
             requestedFile = new DataInputStream(new ByteArrayInputStream("".getBytes()));
                 fileLength = requestedFile.available();
             }catch(Exception e){
                 System.err.println("Open:"+fileName+">>"+e);
                 return false;}
         }
                         return true;
 
     }
     private boolean requestRead()
     {
          try{
             //Open inputStream and read(parse) the request
             dis = new DataInputStream(client.getInputStream());
             String line;
             send=true;
             while((line=dis.readLine())!=null){
 
                 StringTokenizer tokenizer = new StringTokenizer(line," ");
                 if(!tokenizer.hasMoreTokens()){ break;}
 
                         if(tokenizer.nextToken().equals("GET")){
 
                             fileName = tokenizer.nextToken();
                             if(fileName.equals("/")){
                                 fileName = "index.html";
                             }else{
                                 fileName = fileName.substring(1);
                                 if(fileName.indexOf('?')!=-1)
                                 {
                                     send=false; //DISABLE SENDING ANY FILES
                                     //Same way as ScreenPointer,but,I won't need it anyway!
                                     fileName=fileName.substring(0,fileName.indexOf('?'));
                                 }
                             }
 
                         }
 
             }
 
          }catch(Exception e){
                 System.err.println("Read Request:"+e);
             return false;
          }
      //      app.display("finished file request");
 
          return true;
     }
 
 
     private void constructHeader(){
         String contentType;
         if((fileName.toLowerCase().endsWith(".jpg"))||(fileName.toLowerCase().endsWith(".jpeg"))
 ||(fileName.toLowerCase().endsWith(".jpe"))){contentType = "image/jpg";}
         else if((fileName.toLowerCase().endsWith(".gif"))){contentType = "image/gif";}
         else if((fileName.toLowerCase().endsWith(".htm"))||
                 (fileName.toLowerCase().endsWith(".html"))){contentType = "text/html";}
         else if((fileName.toLowerCase().endsWith(".qt"))||
                 (fileName.toLowerCase().endsWith(".mov"))){contentType = "video/quicktime";}
         else if((fileName.toLowerCase().endsWith(".class"))){contentType = "application/octet-stream";}
         else if((fileName.toLowerCase().endsWith(".mpg"))||
 (fileName.toLowerCase().endsWith(".mpeg"))||(fileName.toLowerCase().endsWith(".mpe")))
 {contentType = "video/mpeg";}
         else if((fileName.toLowerCase().endsWith(".au"))||(fileName.toLowerCase().endsWith(".snd")))
             {contentType = "audio/basic";}
         else if((fileName.toLowerCase().endsWith(".wav")))
             {contentType = "audio/x-wave";}
         else {contentType = "text/plain";} //default
 
         header = "HTTP/1.0 200 OK\n"+
                  "Allow: GET\n"+
                  "MIME-Version: 1.0\n"+
                  "Server : HMJ Basic HTTP Server\n"+
                  "Content-Type: "+contentType + "\n"+
                  "Content-Length: "+ fileLength +
                  "\n\n";
     }
 
   //  private Serve app;
     private Socket client;
     private String fileName,header;
     private DataInputStream requestedFile, dis;
         private int fileLength, bytesSent;
 
 }
 
 class tagTweets
 {
     private String name;
     private Vector<String> twts;
     public tagTweets(String tagName)
     {
         this.name=tagName;
         twts=new Vector<String>();
     }
     public void add(String txt)
     {
         twts.add(txt);
     }
     public String[] getTwts()
     {
         String res[]=new String[twts.size()];
         twts.toArray(res);
         return res;
     }
 }
 class data
 {
     private TreeMap<String,tagTweets> map;
     private Vector<String> tagnames;
     public static long sleepTime=30*60*1000;
     public data()
     {
         map=new TreeMap<String, tagTweets>();
         tagnames=new Vector<String>();
     }
     public void add(String tag,String txt)
     {
         if(!map.containsKey(tag.toLowerCase()))
         {
             tagnames.add(tag.toLowerCase());
             map.put(tag.toLowerCase(), new tagTweets(tag.toLowerCase()));
         }
         map.get(tag.toLowerCase()).add(txt);
     }
     public void save()
     {
         try{
             FileInputStream fin;
             FileOutputStream fout;
             String twt[],re[],all="";
             int i,j;
             HashSet<String> oldRe=new HashSet<String>();
             for(i=0;i<tagnames.size();i++)
             {
                 all="";
                 re=new String[]{};
                 if(new File(tagnames.get(i).toLowerCase()).exists())
                 {
                     fin=new FileInputStream(tagnames.get(i).toLowerCase());
                     byte res[]=new byte[fin.available()];
                     fin.read(res);
                     fin.close();
                    all=res.toString();
                     re=all.split("\n");
                 }
                 for(j=0;j<re.length;j+=2)
                     oldRe.add(re[j]);
                 twt=map.get(tagnames.get(i).toLowerCase()).getTwts();
                 for(j=0;j<twt.length;j++)
                     if(!oldRe.contains(twt[j].split("\n")[0]))
                     {
                         System.err.println(">>"+tagnames.get(i).toLowerCase()+":\n"+twt[j]+"\n\n");
                         all+=twt[j]+"\n";
                     }
                 fout=new FileOutputStream(tagnames.get(i).toLowerCase());
                 fout.write(all.getBytes());
                 fout.close();
             }
             
             String old[]=new String[]{};
             HashSet<String> oldTagNames=new HashSet<String>();
             if(new File("_tags").isFile())
             {
                     fin=new FileInputStream("_tags");
                     byte res[]=new byte[fin.available()];
                     fin.read(res);
                     fin.close();
                    all=res.toString();
                     old=all.split("\n");
             }
             //Add oldTags with the latest 1s
             for(i=0;i<old.length;i++)
                 if(!map.containsKey(old[i]))
                     tagnames.add(old[i].toLowerCase());
             String res="";
             for(i=0;i<tagnames.size();i++)
                 res+=tagnames.get(i).toLowerCase()+"\n";
             new FileOutputStream("_tags").write(res.getBytes());
         }catch(Exception e){System.err.println(e);}
     }
 }
 class Streaming extends Thread
 {
     private String url;
     private data d;
     public  boolean exit=false;
     public Streaming(String url,data d)
     {
         this.url=url;
         this.d=d;
     }
     @Override
     public void run()
     {
         String twt,re,entries[],inputLine,res,tags[];
         BufferedReader in;
         int n=0,i,j;
         while(!exit)
         {
             
             try {
                 in = new BufferedReader(
                                         new InputStreamReader(
                                         new URL("http://search.twitter.com/search.atom?q="+url).openStream() ));
                 res="";
                 while ((inputLine = in.readLine()) != null)
                     res+=inputLine;
                 in.close();
                 entries=res.split("</entry>");
                 res="";
                 for(i=0;i<entries.length-1;i++)
                 {
                     tags=new String[]{};
                     twt=entries[i].substring(entries[i].lastIndexOf("<title>")+"<title>".length(),entries[i].lastIndexOf("</title>") );
                     twt=twt.replace("\r\n","<br>");
                     twt=twt.replace("\n","<br>");
                     System.out.println(">>>"+twt);
                     re=entries[i].substring(0,entries[i].lastIndexOf("\" rel=\"alternate\"/>"));
                     re=re.substring(re.lastIndexOf("href=\"")+"href=\"".length());
                     tags=TwtBlog.SplitTags(twt);
      //               twt=twt.replace("'","\'");
     //                twt=twt.replace("\"","\\"+"\"");
                     for(j=0;j<tags.length;j++)
                         d.add(tags[j].toLowerCase(), re+"\n"+twt);
                 }
                 sleep(data.sleepTime);
             } catch (Exception ex) {System.err.println(ex);
             }
         }
     }
 }
 
 public class TwtBlog extends JFrame implements KeyListener,ActionListener
 {
     public static String[] SplitTags(String txt)
     {
         int eInd;
         String t;
         HashSet<String> hs=new HashSet<String>();
         Vector<String> v=new Vector<String>();
         while( ( eInd = txt.indexOf('#') ) >-1)
         {
             eInd++;
             if(
                     (eInd-2>=0 && !( 'a'<=txt.charAt(eInd-2)&&txt.charAt(eInd-2)<='z' )
                          && !( 'A'<=txt.charAt(eInd-2)&&txt.charAt(eInd-2)<='Z' )
                          && !( '0'<=txt.charAt(eInd-2)&&txt.charAt(eInd-2)<='9' )
                          && txt.charAt(eInd-2)!='&' )
                || eInd<=1     )
             {
                 while(eInd<txt.length()&&
                             (
                                     ('a'<=txt.charAt(eInd)&&txt.charAt(eInd)<='z' )
                                 ||  ('A'<=txt.charAt(eInd)&&txt.charAt(eInd)<='Z' )
                                 ||  ('0'<=txt.charAt(eInd)&&txt.charAt(eInd)<='9' )
                             )
                         )
                     eInd++;
                 
                 t=txt.substring(txt.indexOf('#')+1,eInd);
                if(eInd-txt.indexOf('#')+1>0 && !hs.contains(t))
                     {
                         hs.add(t);
                         v.add(t);
                     }
             }
             txt=txt.substring( eInd );
         }
         String res[]=new String[v.size()];
         v.toArray(res);
         return res;
     }
     
     
     public static void main(String[] args) {
         new TwtBlog();
     }
 
     private JTextField textField;
     private data d;
     private JButton ok;
     private boolean saving=false;
     private Streaming[] threads;
     public TwtBlog()
     {
         super("TwtBlog");
         setSize(200,100);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         textField=new JTextField("@yoga1290");
         textField.setHorizontalAlignment(0);
         textField.addKeyListener(this);
         textField.setFocusable(true);
         textField.setFont(null);
         Container c=getContentPane();
         c.setLayout(new BorderLayout());
         c.add(textField,BorderLayout.CENTER);
         ok=new JButton("OK!");
         ok.addActionListener(this);
 //        c.add(ok,BorderLayout.SOUTH);
         this.addKeyListener(this);
         d=new data();
         setVisible(true);
         
         
         try{
                 //Integer.parseInt(port.getText())
             ServerSocket ss = new ServerSocket(1290);
             while(true){
                // display("listening again ** port: "+port+"\n");
                 Socket s = ss.accept();
                 new Thread(new FileRequest(s)).start();
                 //display("got a file request \n");
             }
             }catch(Exception ex){System.err.println(ex);}
     }
     @Override
     public void keyTyped(KeyEvent ke) {
         
     }
 
     @Override
     public void keyPressed(KeyEvent ke) {
         if(ke.getKeyCode()==KeyEvent.VK_ENTER || ke.getKeyCode()==KeyEvent.VK_SPACE)
         {
             if(saving) return;
             String req[];
             if(textField.getText().indexOf("+")>-1)
                 req=textField.getText().split("+");
             else
                 req=new String[]{textField.getText()};
             
                 threads=new Streaming[req.length];
                 for(int i=0;i<req.length;i++)
                 {
                     req[i]=req[i].replace("&","+");
                     req[i]=req[i].replace("@", "from%3A");
                     // # %23haiku
                     //re %40mashable
                     
                     threads[i]=new Streaming(req[i], d);
                     threads[i].start();
                 }
                 saving=true;
                 ok.setText("Save & Exit");
                 setTitle("TwtBlog: (S)ave&Exit");
                 textField.setEditable(false);
 //                textField.setEnabled(false);
         }
         if(ke.getKeyCode()==KeyEvent.VK_S)
         {
             setTitle("Saving TwtBlog...");
             d.save();
             ok.setText("Saved!");
             setTitle("TwtBlog Saved :)");
             try {
                 Thread.sleep(500);
                 setTitle("TwtBlog Saved :))");
                 Thread.sleep(500);
                 setTitle("TwtBlog Saved :D");
                 Thread.sleep(500);
                 setTitle("TwtBlog Saved :D!");
                 Thread.sleep(1000);
             } catch (InterruptedException ex) {}
             System.exit(0);
         }
     }
     @Override
     public void keyReleased(KeyEvent ke) {
     }
 
     @Override
     public void actionPerformed(ActionEvent ae) {
         if(ae.getSource()==ok)
             if(!saving)
             {
                 String req[]=textField.getText().split("+");
                 threads=new Streaming[req.length];
                 for(int i=0;i<req.length;i++)
                 {
                     req[i]=req[i].replace("&","+");
                     req[i]=req[i].replace("@", "from%"+"3"+"A");
                     // # %23haiku
                     //re %40mashable
                     
                     threads[i]=new Streaming(req[i], d);
                     threads[i].start();
                 }
                 saving=true;
                 ok.setText("Save & Exit");
             }
             else
             {
                 ok.setText("Saving..");
                 d.save();
                 ok.setText("Saved!");
                 
                 System.exit(0);
             }
     }
 }
