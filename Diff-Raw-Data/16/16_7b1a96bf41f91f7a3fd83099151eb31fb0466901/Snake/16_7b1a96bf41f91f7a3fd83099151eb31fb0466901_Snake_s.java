 package client;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import java.io.*;
 
 /**
  * Developer: Eugene Shurupov
  * Date:      13.07.13
  * Time:      0:03
  */
 public class Snake {
 
    private static Logger logger = LoggerFactory.getLogger(Snake.class);

     public static void main(String[] args) throws IOException {
 
         new SnakeProcessor().process();
 
     }
 
 }
