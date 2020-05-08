 import processing.core.*; 
 import processing.xml.*; 
 
 import processing.pdf.*; 
 
 import java.applet.*; 
 import java.awt.Dimension; 
 import java.awt.Frame; 
 import java.awt.event.MouseEvent; 
 import java.awt.event.KeyEvent; 
 import java.awt.event.FocusEvent; 
 import java.awt.Image; 
 import java.io.*; 
 import java.net.*; 
 import java.text.*; 
 import java.util.*; 
 import java.util.zip.*; 
 import java.util.regex.*; 
 
 public class eyediagram extends PApplet {
 
 /*  
 Eye diagram visualization code
 LICENCE: FreeBSD License
 Copyright (C) 2009-2012 Nils Gehlenborg (original author, nils@gehlenborg.com),
 Juuso Parkkinen (juuso.parkkinen@gmail.com), Jose Caldas, Ali Faisal,
 Alvis Brazma and Samuel Kaski. All rights reserved.
 */
 

 
 /* INITIALIZATION */
  
  
 PGraphics graphics;
 PFont font;
 PFont fontBold;
 float r;
 float theta;
 float min_theta; // = -HALF_PI;
 float max_theta; // = HALF_PI;
 
 Location[] wordLocations;
 Location[] documentLocations;
 Location[] topicLocations;
 
 float[][] d2t;
 float[][] w2t;
 
 String[] words;
 String[] documents;
 String[] topics;
 String[] topicNames;
 
 String filepath;
 String[] params;
 String folder;
 String outputfile;
 int topicCount;
 String[] infoText;
 int highlightedTopic;
//boolean docColoring;
//boolean wordColoring;
 String[] fontSizes;
 String[] radiusAdjs;
 String[] curveFactors;
 String[] docColors;
 String[] wordColors;
 
 int docFontSize;
 int wordFontSize;
 int topicFontSize;
 int infoFontSize;
 int topicWidth;
 int topicHeight;
 
 int rAdjustmentWords;
 int rAdjustmentDocs; 
 float topicSpreadFactor;
 float docCurveFactor;
 float wordCurveFactor;
 
int d2tCurveWidthFactor; // = 20; // original: 20?
int w2tCurveWidthFactor; // = 100; // original: 10, CCA paper: 50
int d2tCurveAlphaFactor; // = 10; // original: 5, slides: 10
int w2tCurveAlphaFactor; // = 50; // original: 20, slides: 50
 
 public void setup() { 
   size( screen.width, screen.height, JAVA2D); 
   hint(ENABLE_NATIVE_FONTS); 
 
 
   font = createFont("Arial", 24); 
   fontBold = createFont("Arial Bold", 24); 
//  font = createFont("Helvetica", 24); 
//  String[] fontList = PFont.list();
//  println(fontList);
 
   filepath = "./example/";
 
   params = readNames(filepath + "input/parameters.txt"); // Read general input information
   outputfile = params[0];
   topicCount = Integer.parseInt(params[1]);
   highlightedTopic = Integer.parseInt(params[2]);
 //  wordColoring = Boolean.parseBoolean(params[3]);
   infoText = readNames(filepath + "input/infotext.txt");
   
   words = readNames(filepath + "input/word_names.txt" ); // Read word, document, and topic data
   documents = readNames(filepath + "input/doc_names.txt" ); 
   topics = readNames(filepath + "input/topics.txt" );
   topicNames = readNames(filepath + "input/topic_names.txt" );
   d2t = readMatrix(filepath + "input/doc2topic.txt", "\t" );
   w2t = readMatrix(filepath + "input/word2topic.txt", "\t" );
 
   fontSizes = readNames(filepath + "input/font_sizes.txt"); // Read font sizes etc.
   docFontSize = Integer.parseInt(fontSizes[0]);
   wordFontSize = Integer.parseInt(fontSizes[1]);
   topicFontSize = Integer.parseInt(fontSizes[2]);
   infoFontSize = Integer.parseInt(fontSizes[3]);
   topicWidth = Integer.parseInt(fontSizes[4]);
   topicHeight = Integer.parseInt(fontSizes[5]);
   
   radiusAdjs = readNames(filepath + "input/radius_adjustments.txt"); // Read radius adjustments
   rAdjustmentDocs = Integer.parseInt(radiusAdjs[0]);
   rAdjustmentWords = Integer.parseInt(radiusAdjs[1]);
   topicSpreadFactor = Float.parseFloat(radiusAdjs[2]);
   docCurveFactor = Float.parseFloat(radiusAdjs[3]);
   wordCurveFactor = Float.parseFloat(radiusAdjs[4]);
   max_theta = Float.parseFloat(radiusAdjs[5])/90*HALF_PI; 
   min_theta = -max_theta;
 //  min_theta = Float.parseFloat(radiusAdjs[6])/90*(-HALF_PI); // -HALF_PI;
   
   curveFactors = readNames(filepath + "input/curve_factors.txt"); // Read curve factors
   d2tCurveWidthFactor = Integer.parseInt(curveFactors[0]);
   w2tCurveWidthFactor = Integer.parseInt(curveFactors[1]);
   d2tCurveAlphaFactor = Integer.parseInt(curveFactors[2]);
   w2tCurveAlphaFactor = Integer.parseInt(curveFactors[3]);
  
   docColors = readNames(filepath + "input/doc_colors.txt"); // Read word colors
   wordColors = readNames(filepath + "input/word_colors.txt"); // Read word colors
   
  
   wordLocations = new Location[words.length];
   documentLocations = new Location[documents.length];  
   topicLocations = new Location[topicCount];  
   
   graphics = createGraphics( (int)(2100), (int)(2950), PDF, outputfile +".pdf" ); //graphics.smooth();
   exportPDF( graphics );    // create PDF.
 } 
  
 public void draw() { 
   //smooth();
   //imageMode( CENTER );
   //float sf = (height/(float)graphics.height);
   //float sf = (width/(float)graphics.width);
   //image( graphics, width/2, height/2, sf*graphics.width, sf*graphics.height );
   exit();
 }  
  
 public void exportPDF( PGraphics pdf ) { 
   int fitWidth = 2100;
   int fitHeight = 2950;
   
   //PGraphics pdf = createGraphics( 2100, 2950, PDF, "test.pdf" ); 
   pdf.beginDraw(); 
   pdf.background(0xffffffff); 
   pdf.fill(0); 
   pdf.scale(1); 
   pdf.strokeCap( SQUARE );
   
   /* CALCULATE WORD LOCATIONS AND DRAW WORD NAMES */
   pdf.textFont(font, wordFontSize);
   float maxWidth = maxTextWidth( pdf, words );
   
   r = min( fitWidth, fitHeight)/2 - maxWidth + rAdjustmentWords;
   theta = min_theta;
   
   /* Auxiliary variables */
   float sum=0.0f;
   if (highlightedTopic != -1)
     sum = sum( w2t, highlightedTopic );  
   float ff = 0.9f;
   float sf;
   float step;
 
   for ( int i = 0; i < words.length; ++i ) { 
     
 
         
     if (highlightedTopic == -1) {
       step = (max_theta - min_theta)/(float)words.length;
       pdf.textFont(font, wordFontSize);
     }  else {
       sf = w2t[i][highlightedTopic]/sum * (float)words.length;
       step = ff * ((max_theta - min_theta)/ (float)words.length) + (1-ff) * sf * ((max_theta - min_theta)/(float)words.length);
       pdf.textFont(font, ff * wordFontSize + (1-ff) * wordFontSize * sf);
     }
     
     // WRITE WORD NAMES AS GIVEN (8.6.2011)
 //    String word; 
 //    if (toLower)
 //      word = ( new String( words[i] )).toLowerCase();
 //    else
 //    word = words[i];
     String word = words[i];
     
     pdf.pushMatrix();
     pdf.translate(pdf.width/2-200,pdf.height/2);
     pdf.rotate( theta + 0.5f * step );
     pdf.ellipseMode(CENTER);
     pdf.noStroke();
     
     wordLocations[i] = new Location( r * cos(theta + 0.5f*step) - 200, r * sin(theta + 0.5f*step));
                
     pdf.popMatrix();
     pdf.fill(0);
     pdf.stroke(0);
     pdf.pushMatrix();
     
     pdf.translate(pdf.width/2-200,pdf.height/2);
     pdf.rotate( theta + 0.5f*step );
     pdf.textAlign(LEFT,CENTER);
     // ADD WORD COLOR
 //    if (wordColoring) {
     if (wordColors[i].equals("black"))
       pdf.fill(0, 0, 0);
     if (wordColors[i].equals("red"))
       pdf.fill(255, 0, 0);
     if (wordColors[i].equals("green"))
       pdf.fill(0, 255, 0);
     if (wordColors[i].equals("blue"))
       pdf.fill(0, 0, 255);
 //    }
     pdf.text( word, r + 4, 0);    
     pdf.popMatrix();
     
     theta += step;
   }
       
   /* CALCULATE DOCUMENT LOCATIONS AND DRAW DOCUMENT NAMES */  
   pdf.textFont(font, docFontSize);   
   maxWidth = maxTextWidth( pdf, documents );
   
   r = min( fitWidth, fitHeight)/2 - maxWidth + rAdjustmentDocs;
 //  theta = min_theta;
   theta = min_theta - 2*(HALF_PI + min_theta);
   
   if (highlightedTopic != -1)
     sum = sum( d2t, highlightedTopic );  
   
   for ( int i = 0; i < documents.length; ++i ) {        
 
     
     if (highlightedTopic == -1) {
       step = (max_theta - min_theta) / (float)documents.length;
       pdf.textFont(font, docFontSize);
     } else {
       sf = d2t[i][highlightedTopic]/sum * (float)documents.length;
       step = ff * ((max_theta - min_theta)/(float)documents.length) + (1-ff) * sf * ((max_theta - min_theta)/(float)documents.length);
       pdf.textFont(font, ff * docFontSize + (1-ff) * docFontSize * sf);
     }
     
     String document = documents[i];
 
     pdf.pushMatrix();
     
     pdf.translate(pdf.width/2-100,pdf.height/2);
     pdf.rotate( theta - 0.5f * step );
     pdf.ellipseMode(CENTER);
     pdf.noStroke();
     
     documentLocations[i] = new Location( r * cos(theta - 0.5f * step ) -250, r * sin(theta - 0.5f * step ) );
                
     pdf.popMatrix();
     pdf.fill(0);
     pdf.stroke(0);
     pdf.pushMatrix();
     
     float textWidth = pdf.textWidth( document );
     Location textEnd = new Location( (r+textWidth+4) * cos(theta - 0.5f * step ) -250, (r+textWidth+4) * sin(theta - 0.5f * step ) );
 
     pdf.translate(pdf.width/2,pdf.height/2);
     pdf.translate( textEnd.x, textEnd.y );
     pdf.rotate( ( theta - 0.5f * step ) + radians( 180 ) );
     pdf.textAlign(LEFT,CENTER);
     // ADD DOC COLOR
 //    if (docColoring) {
     if (docColors[i].equals("black"))
       pdf.fill(0, 0, 0);
     if (docColors[i].equals("red"))
       pdf.fill(255, 0, 0);
     if (docColors[i].equals("green"))
       pdf.fill(0, 100, 0);
     if (docColors[i].equals("blue"))
       pdf.fill(0, 0, 255);
 //    }
     pdf.text( document, 0, 0 );    
     pdf.popMatrix();
     
     theta -= step;
   }
     
   pdf.translate( pdf.width/2, pdf.height/2 );
 //  drawTopics( pdf, topicCount, r, topicNames );
   calculateTopicLocations(topicCount);
   
   pdf.noFill();
   pdf.colorMode( HSB, 1 );
 
   /* DRAW TOPIC TO DOCUMENT -CURVES */
   for ( int j = 0; j < d2t.length; ++j ) {      
     for ( int i = 0; i < d2t[j].length; ++i ) {
 
       pdf.pushMatrix();
       pdf.translate( topicLocations[i].x, topicLocations[i].y );
       pdf.noFill();
 
       Location bundlePoint1 = new Location( - r/8 - ( r - 2 * abs( topicLocations[i].y ) ) * docCurveFactor, 0 );    
       
       if (highlightedTopic != -1) {
          if (i == highlightedTopic)
            pdf.stroke( 1f/(float)topicCount * (float)i, 0.8f, 0.8f, d2t[j][i] * d2tCurveAlphaFactor);
          else 
            pdf.stroke( 1f/(float)topicCount * (float)i, 0.0f, 0.8f, d2t[j][i] * d2tCurveAlphaFactor/5); //desaturate
         
       } else
         pdf.stroke( 1f/(float)topicCount * (float)i, 0.8f, 0.8f, d2t[j][i] * d2tCurveAlphaFactor);
  
     
       pdf.strokeWeight(d2tCurveWidthFactor * d2t[j][i]);
       
       pdf.bezier(0, 0, bundlePoint1.x, bundlePoint1.y,
         bundlePoint1.x, bundlePoint1.y, documentLocations[j].x - topicLocations[i].x, documentLocations[j].y - topicLocations[i].y );
       
       pdf.popMatrix();
     }        
   }
   
   /* DRAW TOPIC TO WORD -CURVES */
   for ( int j = 0; j < w2t.length; ++j ) {      
     for ( int i = 0; i < w2t[j].length; ++i ) {
 
       pdf.pushMatrix();
       pdf.translate( topicLocations[i].x, topicLocations[i].y );
       pdf.noFill();
 
       Location bundlePoint1 = new Location( r/8 + ( r - 2 * abs( topicLocations[i].y ) ) * wordCurveFactor, 0 );    
     
       if (highlightedTopic != -1) {
         if ( i == highlightedTopic )
           pdf.stroke( 1f/(float)topicCount * (float)i, 0.8f, 0.8f, w2t[j][i] * w2tCurveAlphaFactor);
         else
           pdf.stroke( 1f/(float)topicCount * (float)i, 0.0f, 0.8f, w2t[j][i] * w2tCurveAlphaFactor/5); //desaturate
         
       } else
         pdf.stroke( 1f/(float)topicCount * (float)i, 0.8f, 0.8f, w2t[j][i]* w2tCurveAlphaFactor);
 
       pdf.strokeWeight(w2tCurveWidthFactor * w2t[j][i]);
       
 
 
       pdf.bezier( 0, 0, bundlePoint1.x, bundlePoint1.y,
           bundlePoint1.x, bundlePoint1.y, wordLocations[j].x - topicLocations[i].x, wordLocations[j].y - topicLocations[i].y );
  
       pdf.popMatrix();
     }    
     
   }
 
   drawTopics( pdf, topicCount, r, topicNames );
   
   /* WRITE INFORMATION TEXTBOX */
   pdf.textAlign(LEFT);
   pdf.textFont(font, infoFontSize);   
   for (int i=0; i < infoText.length; ++i)
     pdf.text(infoText[i], -800, -1200+50*i);
      
   /* FINALIZE PDF */   
   pdf.dispose(); 
   pdf.endDraw(); 
 }
 
 
 public float maxTextWidth( PGraphics pdf, String[] lines ) {
  
   float max = 0;
   for ( int i = 0; i < lines.length; ++i ) {
     float width = pdf.textWidth( lines[i] );
     if ( max < width )
       max = width;
   }  
   return ( max );
 }
 
 public void calculateTopicLocations(int count) {
   
   for ( int i = 0; i < count; ++i ) {
     
     float yloc = ((float)i) * r/((float)count - topicSpreadFactor) - r/2 -topicSpreadFactor*count/2;
     topicLocations[i] = new Location( -200, yloc );
   }
 }
 
 public void drawTopics( PGraphics pdf, int count, float r, String[] labels ) {
   
   pdf.ellipseMode(CENTER);
   pdf.noStroke();
   pdf.colorMode( HSB, 1 );
 
   for ( int i = 0; i < count; ++i ) {
     
 //    float yloc = ((float)i) * r/((float)count - topicSpreadFactor) - r/2 -topicSpreadFactor*count/2;
 //    float yloc = ((float)i) * r/((float)count) - r/2; // ORIGINAL
     
     pdf.stroke( 1f/(float)count * (float)i, 0.8f, 0.8f, 1 );
     pdf.strokeWeight( 4 );
     pdf.fill( 1 );
 
 //    pdf.ellipse( -200, yloc , topicWidth, topicHeight);
     pdf.ellipse(topicLocations[i].x, topicLocations[i].y , topicWidth, topicHeight);
     pdf.fill( 0 );
     pdf.textFont( font, topicFontSize);
     pdf.textAlign( CENTER, CENTER );
     pdf.text( labels[PApplet.parseInt( topics[i] ) - 1],  topicLocations[i].x, topicLocations[i].y - 2 );
 //    topicLocations[i] = new Location( -200, yloc );
   }
 }  
 
 public float[][] readMatrix( String file, String sep ) {
   
   String[] strings = loadStrings( file );
   
   int rows = strings.length;
   int columns = 0;
   
   if ( rows > 0 )
     columns = strings[0].split( sep ).length;
   
   float matrix[][] = new float[rows][columns];
   
   for ( int i = 0; i < strings.length; ++i ) {
     String[] fields = strings[i].split( sep );
     
     for ( int j = 0; j < fields.length; ++j )
       matrix[i][j] = new Float( fields[j] );
   }
   return ( matrix );
 }
 
 public String[] readNames( String file  ) {
   String[] strings = loadStrings( file ); 
   return ( strings );
 }
 
 public float max( float[][] matrix, int column ) {
 
   float max = 0;
   for ( int i = 0; i < matrix.length; ++i ) {
     if ( matrix[i][column] > max )
       max = matrix[i][column];
   }
   return ( max );
 }
 
 public float sum( float[][] matrix, int column ) {
  
   float sum = 0;
   for ( int i = 0; i < matrix.length; ++i ) 
     sum += matrix[i][column];
 
   return ( sum );
 }
   static public void main(String args[]) {
     PApplet.main(new String[] { "--bgcolor=#FFFFFF", "eyediagram" });
   }
 }
