 /* AspectFitter finds the smallest rectangle with ASPECT_RATIO
  * proportions that will fit inside of the given dimensions.
  */
 public class AspectFitter {
     private static final float ASPECT_RATIO = 5f / 3f;
 
     public static void main(String[] args) {
         if(args.length != 2) {
             System.out.println("USAGE: AspectFitter <width> <height>");
             return;
         }
 
         int srcWidth = Integer.parseInt(args[0]);
         int srcHeight = Integer.parseInt(args[1]);
         float srcAspect = (float)srcWidth / (float)srcHeight;
         System.out.println("src dimensions: " + srcWidth + " x " + srcHeight);
         System.out.println("src aspect ratio: " + srcAspect);
 
         int destWidth = 0;
         int destHeight = 0; 
         float border = 0f;
         float destAspect = 0f;
         String borderPosition = "";
         if(srcAspect > ASPECT_RATIO) {
             // if given dimensions are too wide
             destWidth = (int)((float)srcHeight * ASPECT_RATIO);
             destHeight = srcHeight;
             destAspect = (float)destWidth / (float)destHeight;
             border = (srcWidth - destWidth) / 2f;
             borderPosition = "left/right";
         } else if(srcAspect < ASPECT_RATIO) {
             // if given dimensions are too tall
             destWidth = srcWidth;
             destHeight = (int)((float)srcWidth / ASPECT_RATIO);
             destAspect = (float)destWidth / (float)destHeight;
             border = (srcHeight - destHeight) / 2f;
             borderPosition = "top/bottom";
         } else {
             // if given dimensions are just right
             destWidth = srcWidth;
             destHeight = srcHeight;
             destAspect = srcAspect;
             border = 0f;
             borderPosition = "none";
         }
 
         System.out.println("dest dimensions: " + destWidth + " x " + destHeight);
         System.out.println("dest aspect: " + destAspect);
         System.out.println("border (" + borderPosition + "): " + border);
     }
 }
