 package su.mctop.bot;
 
 import java.util.List;
 import su.mctop.bot.vk_api.ApiRequest;
 import su.mctop.bot.vk_api.VKToken;
 
 /**
  * Hello world!
  *
  */
 public class Bot {
 
     public static void main(String[] args)
     {
        ApiRequest api = new ApiRequest(3571081, "");
        api.DeleteWallMessage(1, 1);
     }
     
     public void AcceptNewbies( List<Integer> black_list )
     {
         System.out.printf("Accept newbies");
     }
 }
