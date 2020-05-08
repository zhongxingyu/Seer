 package com.fun.animator.input;
 
 import java.awt.image.BufferedImage;
 import java.util.LinkedList;
 import java.util.List;
 
 public class CompositeImageWithDepth implements Image {
 
     private final List<Image> images;
     private final int numOfImages;
 
     public CompositeImageWithDepth(final int numOfImages) {
         this.numOfImages = numOfImages;
         images = new LinkedList<Image>();
     }
 
     public void add(Image image) {
         images.add(0, image);
         if (images.size() > numOfImages) {
             images.remove(numOfImages);
         }
     }
 
    public boolean isEmpty() {
        return images.isEmpty();
    }

     @Override
     public int getDepth(int x, int y) {
         final int depth = images.get(0).getDepth(x, y);
         if (depth < DepthImageTransformer.DEPTH_MAX) {
             return depth;
         }
         for (Image image : images) {
             final int imageDepth = image.getDepth(x, y);
             if (imageDepth < DepthImageTransformer.DEPTH_MAX) {
                 return imageDepth;
             }
         }
         return DepthImageTransformer.DEPTH_MAX;
     }
 
     @Override
     public int getWidth() {
         return images.get(0).getWidth();
     }
 
     @Override
     public int getHeight() {
         return images.get(0).getHeight();
     }
 
     @Override
     public int getColorImageType() {
         return images.get(0).getColorImageType();
     }
 
     @Override
     public BufferedImage getColorImage() {
         throw new RuntimeException("method not implemented");
     }
 
     @Override
     public BufferedImage getDepthImage() {
         throw new RuntimeException("method not implemented");
     }
 
     @Override
     public Image deepCopy() {
         throw new RuntimeException("method not implemented");
     }
 }
