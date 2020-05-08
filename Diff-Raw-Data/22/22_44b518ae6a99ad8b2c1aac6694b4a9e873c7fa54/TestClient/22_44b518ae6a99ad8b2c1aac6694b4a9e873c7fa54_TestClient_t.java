 import java.io.*;
 import java.rmi.*;
 
 public class TestClient {
     public static void main(String[] args) throws Exception {
         
         String url = "rmi://unix12.andrew.cmu.edu/masterServer";
        MasterFileServerInterface server = (MasterFileServerInterface) Naming.lookup(url);
         server.addNewFile("hello");
         
         File testFile = new File("out.txt");
         
         FileIO.download(server, testFile, new File("download.txt"));
        System.out.println("downloaded");
         
        FileIO.upload(server, new File("download.txt"), new File("in.txt"));
         System.out.println("uploaded");
     }
}
