 /*
 Spectrometry_kit - a Processing.org-based interface for spectral analysis with a USB webcam-based spectrometer. Also a spectrometer-based musical instrument or guitar pedal
 
 >> You must install the "GSVideo" and "controlP5" libraries in a folder called "libraries" in your sketchbook.
 
 by the Public Laboratory for Open Technology and Science
 publiclaboratory.org
  
 (c) Copyright 2011 Public Laboratory
 
 This file is part of PLOTS Spectral Workbench.
 
 PLOTS Spectral Workbench is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 PLOTS Spectral Workbench is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with PLOTS Spectral Workbench.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import processing.video.*; //mac or windows
 import codeanticode.gsvideo.*; //linux
 import ddf.minim.analysis.*;
 import ddf.minim.*;
 
 //= require <models/spectrum>
 //= require <keyboard>
 //= require <models/system>
 System system;
 //= require <models/video>
 Video video;
 //= require <models/filter>
 Filter filter;
 
 String controller = "setup"; // this determines what controller is used, i.e. what mode the app is in
 String colortype = "combined";
 final static String defaultTypedText = "type to label spectrum";
 String typedText = defaultTypedText;
 PFont font;
 int audiocount = 0;
 int res = 1;
 int samplerow;
 int lastval = 0;
 // for rgb mode:
 int lastred = 0;
 int lastgreen = 0;
 int lastblue = 0;
 int[][][] spectrumbuf;
 int history = 150;
 int[] lastspectrum, absorption, contrastEnhancedAbsorption;
 int averageAbsorption = 0;
 int absorptionSum;
 
 public void setup() {
   system = new System();
   //size(640, 480, P2D);
   //size(1280, 720, P2D);
   // Or run full screen, more fun! Use with Sketch -> Present
   size(screen.width, screen.height-20, P2D);
   //video = new Video(this,640,480,0);
   video = new Video(this,1280,720,0);
  samplerow = int (height*(0.250));
   font = loadFont("Georgia-Italic-18.vlw");  
 
   spectrumbuf = new int[history][video.width][3];
   lastspectrum = new int[video.width];
   absorption = new int[video.width];
   contrastEnhancedAbsorption = new int[video.width];
   for (int x = 0;x < video.width;x++) { // is this necessary? initializing the spectrum buffer with zeroes? come on!
     absorption[x] = 0;
     contrastEnhancedAbsorption[x] = 0;
   }
   filter = new Filter(this);
 }
 
 public void captureEvent(Capture c) { //mac or windows
   c.read();
 }
 public void captureEvent(GSCapture c) { //linux
   c.read();
 }
 
 void draw() {
 
   // bump spectrum history by 1 place
   for (int i = history-1;i > 0;i--) {
     for (int x = 0;x < video.width;x++) {
       spectrumbuf[i][x] = spectrumbuf[i-1][x];
     }
   }
   // add new spectrum to spectrumbufferbuffer
   for (int x = 0;x < video.width;x++) { // is this necessary? initializing the spectrum buffer with zeroes? come on!
     //spectrumbuf[0][x] = 0;
   }
 
   loadPixels(); //load screen pixel buffer into pixels[]
   background(64);
 
   fill(255);
   noStroke();
   line(0,height-255,width,height-255); //100% mark for spectra
 
   textFont(font,18);
   text("PLOTS Spectral Workbench", 15, 25+history); //display current title
   text(typedText, 15, 55+history); //display current title
   //text("red=baseline, white=current, yellow=absorption",15,height-255+45);
 
   // re-zero intensity sum
   absorptionSum = 0;
 
   // preview video to align spectrum
   if (colortype == "rgb") {
     //video.image(0,height*3/4,width/4,height/4)
     for (int y = 0; y < int (video.height); y+=4) {
       for (int x = 0; x < int (video.width); x+=4) {
         if (x < width && y < height) {
          if (video.isLinux) {
            pixels[(height*3/4*width)+(y*width/4)+((x/4))] = video.gscapture.pixels[y*video.width+x];
           //pixels[(height*3/4*width)+(y*width/4)+((x/4))] = video.gscapture.pixels[int (y/video.scale()*video.width+x/video.scale())];
          } else {
            pixels[(height*3/4*width)+(y*width/4)+((x/4))] = video.capture.pixels[y*video.width+x];
          }
         }
       }
     }
     // draw the region of sampling with a rectangle:
     noFill();
     stroke(255,255,0);
     rect(0,height*3/4+samplerow/4,video.width/4,video.sampleHeight/4);
   }
   stroke(255);
   fill(255);
 
   ///////////////////////
   // read from video into a new row in the spectrumbuffer
 
   int index = int (video.width*samplerow); //the horizontal strip to sample
   for (int x = 0; x < int (video.width); x+=res) {
 
     int[] rgb = video.get_rgb(x);
 
     // draw direct output of averaged camera sampling, for "history" frames of history
     spectrumbuf[0][x] = rgb;
     ///////////////////////////////////
     // scale video.width to width!!
     if (x < width) {
       for (int y = 0; y < history; y++) {
         if (colortype == "heat") {
 		colorMode(HSB,255);
 		pixels[(history*width)-(y*width)+x] = color(255-(spectrumbuf[y][x][0]+spectrumbuf[y][x][1]+spectrumbuf[y][x][2])/3,255,255);
 		colorMode(RGB,255);
         } else {
 		pixels[(history*width)-(y*width)+x] = color(spectrumbuf[y][x][0],spectrumbuf[y][x][1],spectrumbuf[y][x][2]);
 	}
       }
       //= require <views/graph>
     }
     index++;
   }
 
   // indicate average with a line
   averageAbsorption = absorptionSum/width;
   stroke(128);
   line(0,averageAbsorption/3,width,averageAbsorption/3);
 
   updatePixels();
 }
 
 /**
  * POST pData to pUrl
  * @return the response
  */
 public String postData(URL pUrl, byte[] pData) {
     // http://wiki.processing.org/w/Saving_files_to_a_web_server
     try {
         URLConnection c = pUrl.openConnection();
         c.setDoOutput(true);
         c.setDoInput(true);
         c.setUseCaches(false);
     
         // set request headers
         final String boundary = "AXi93A";
         c.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
  
         // open a stream which can write to the url
         DataOutputStream dstream = new DataOutputStream(c.getOutputStream());
  
         // write content to the server, begin with the tag that says a content element is comming
         dstream.writeBytes(boundary+"\r\n");
  
         // discribe the content
         dstream.writeBytes("Content-Disposition: form-data; name=\"data\"; filename=\"whatever\" \r\nContent-Type: text/json\r\nContent-Transfer-Encoding: binary\r\n\r\n");
         dstream.write(pData ,0, pData.length);
  
         // close the multipart form request
         dstream.writeBytes("\r\n--"+boundary+"--\r\n\r\n");
         dstream.flush();
         dstream.close();
  
         // read the output from the URL
         BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
         StringBuilder sb = new StringBuilder(in.readLine());
         String s = in.readLine();
         while (s != null) {
             s = in.readLine();
             sb.append(s);
         }
         return sb.toString();
     } catch (Exception e) {
         e.printStackTrace();
         return null;
     }
 }
