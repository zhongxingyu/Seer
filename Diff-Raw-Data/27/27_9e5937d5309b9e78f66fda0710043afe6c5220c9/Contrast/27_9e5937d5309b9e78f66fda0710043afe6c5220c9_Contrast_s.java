 /**
  * A class containing a single method that calculates the contrast of a given grayscale image. 
  * based on the 2.5% quantile distance in the histogram of the image
  * 
  * @author Markus Iser
  * @version 1.0
  */
 class Contrast {
     /**
      * Calculates the contrast of a given grayscale image 
      * based on the 2.5% quantile distance in the histogram of the 
      * image. Every image needs to be well-formed.
      * @param image
      */
     int getContrast(byte[][] image) {
         int[] histogram = new int[256];
        for (int i = 0; i < 256; i++) {
            histogram[i] = 0;
        }
         for (int i = 0; i < image.length; i++) {
             for (int j = 0; j < image[i].length; j++) {
                 histogram[image[i][j]]++;
             }
         }
         int lowQuantile = 0;
         int sum = 0;
         while (sum < image.length * image[0].length * 0.025) {
             sum += histogram[lowQuantile++];
         }
         int highQuantile = 255;
         sum = 0;
         while (sum < image.length * image[0].length * 0.025) {
             sum += histogram[highQuantile--];
         }
         
         return highQuantile - lowQuantile;
     }
 }
