 package org.bitstrings.maven.plugins.splasher;
 
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.bitstrings.maven.plugins.splasher.FlowLayout.Alignment;
 
 public class DrawImagesFromText
     extends Drawable
 {
     // - parameters --[
 
     private String text;
 
     private String imageNamePrefix;
 
     private int padding;
 
     private Alignment alignment = Alignment.HORIZONTAL;
 
     // ]--
 
     protected FlowLayout drawImagesFromText;
 
     public String getText()
     {
         return text;
     }
 
     public String getImageNamePrefix()
     {
         return imageNamePrefix;
     }
 
     public int getPadding()
     {
         return padding;
     }
 
     public Alignment getAlignment()
     {
         return alignment;
     }
 
     @Override
     public void init( Graphics2D g )
         throws MojoExecutionException
     {
        final FlowLayout flowLayout = new FlowLayout();
 
        flowLayout.setDrawingContext( dwContext );
        flowLayout.setPosition( getPosition() );
        flowLayout.setAlignment( alignment );
        flowLayout.setPadding( padding );
 
         final List<Drawable> drawables = new ArrayList<Drawable>();
 
         for ( int i = 0; i < text.length(); i++ )
         {
             DrawImage drawImage = new DrawImage();
 
             drawImage.setImageName( imageNamePrefix + text.charAt( i ) );
 
             drawables.add( drawImage );
         }
 
        flowLayout.setDraw( drawables );
 
         super.init( g );
 
         Graphics2D sg = (Graphics2D) g.create();
 
         try
         {
             drawImagesFromText.init( sg );
         }
         finally
         {
             sg.dispose();
         }
     }
 
     @Override
     public void draw( Graphics2D g )
     {
         super.draw( g );
 
         Graphics2D sg = (Graphics2D) g.create();
 
         try
         {
             drawImagesFromText.draw( sg );
         }
         finally
         {
             sg.dispose();
         }
     }
 }
