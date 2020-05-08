 package yang.pc.tools.fontcreator;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 class FontDrawPanel extends JPanel {
 
     private String mFontName;
 	private int mWidth;
 	private int mHeight;
 	private int mBaseLine;
 	private int mFontSize;
 	private int mLeftStart;
 	private BufferedImage canvas;
 	private int mGlobalB;
 	private int mGlobalT;
 	private int mLetters;
 	private LetterBox mBoxes[];
 	private int mCharHeight;
 	private int mBorderHorizontal;
 	private int mBorderVertical;
 	private int mKernBoxes;
 	private boolean mShowTextureBoxes;
 	private boolean mShowKerningBoxes;
 	private float mSliceHeight;
 	private Color mKernColor;
 	private String mFilename;
 	private int mDebug;
 	private Color mTransparent;
 	private boolean savePixelCoords;
 	private boolean code;
 	private int mAvgWidth;
 	private String mArgs;
 	private Integer[] mCharIndicesToRender;
 	private boolean mReplaceUndefinedChars;
 
 	public FontDrawPanel() {
 		mGlobalB = 0;
     	mGlobalT = 10000;   
     	mBorderHorizontal = 2;
     	mBorderVertical = 2;
     	
     	mShowTextureBoxes = false;
     	mShowKerningBoxes = false; 
     	code 			  = false;
         savePixelCoords   = true;
     	
     	mKernColor = new Color(0,255,255,80);
     	mTransparent = new Color(0,0,0,0);
     }
     
     public void setParameters(String userInput,String fontName, int outputWidth, int outputHeight, Integer charIndicesToRender[], int kernBoxes, int fontSize, String filename, int pixelBorderHor, int pixelBorderVer, int debug, boolean replaceUndefinedChars){
     	mArgs = userInput;
     	mFontName = fontName;
     	mWidth	  = outputWidth;
     	mHeight	  = outputHeight;
     	mBorderHorizontal = pixelBorderHor;
     	mBorderVertical = pixelBorderVer;
     	
     	mBaseLine = mHeight / 2;
     	mFontSize = fontSize;
     	
     	mLeftStart = mWidth / 2;
     	
     	mCharIndicesToRender = charIndicesToRender;
     	mLetters = mCharIndicesToRender.length;
     	
     	mBoxes = new LetterBox[mLetters];
     	mKernBoxes = kernBoxes;
     	mReplaceUndefinedChars = replaceUndefinedChars;
     	
     	mFilename = filename;
     	
     	if(debug == 1) {
     		mShowKerningBoxes = true;
     		mShowTextureBoxes = true;
     	}
     	
     	mDebug = debug;
     }
 
     public Dimension getPreferredSize() {
         return new Dimension(mWidth,mHeight);
     }
 
     public void paintComponent(Graphics g) {
 
     	canvas = new BufferedImage(mWidth, mHeight, BufferedImage.TYPE_INT_ARGB);    	
     	Graphics2D g2 = canvas.createGraphics();    	
         super.paintComponent(g); 
         g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,        
         RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         
         ScanEachLetter(g2,g);
         
         ScanKernBoxes(g2, g);
         
         ArrangeLetter(g2,g);
         g.drawImage(canvas, 0,0,this);
         
         CalcAvgCenterWidth();
         
         
         if(mDebug == 0){
 	        File outputfile = new File(mFilename+".png");
 	        try {
 				ImageIO.write(canvas, "png", outputfile);
 				
 				FileWriter 	outFile = new FileWriter(mFilename+".txt");
 		        PrintWriter out 	= new PrintWriter(outFile);
 		        		        
 		        if(code)
 		        	WriteCodeSnippet(out);
 		        else
 		        	WritePropFile(out);
 		        	        
 		        out.close();
 			} catch (IOException e) { e.printStackTrace();	}	        
         	
 	        System.exit(0);
         }
     }  
     
     private void CalcAvgCenterWidth() {
     	int acc = 0;
     	int den = 0;
     	for(LetterBox box: mBoxes){    		
     		acc += box.w;
     		den ++;
     	}
     	
     	if(den == 0) mAvgWidth = (int)(mWidth/10.0f);
     	mAvgWidth = (int)((float)acc) / den;
 	}
 
 	private void WritePropFile(PrintWriter out) {
 		out.println("# "+mArgs);
     	if(savePixelCoords) out.println("textureW = "+ mWidth);
     	else				out.println("textureW = "+ 1);
     		
     	if(savePixelCoords) out.println("textureH = "+ mHeight);
     	else				out.println("textureH = "+ 1);
 		out.println("kernBoxes = "+ mKernBoxes);
 		
 		if(savePixelCoords)
 			out.println("charHeight = "+ (mGlobalB-mGlobalT));
 		else
 			out.println("charHeight = "+ ((float)(mGlobalB-mGlobalT))/mHeight);
 		
 		out.println("firstCharID = " + mCharIndicesToRender[0]);
 		out.println("lastCharID = " + (mCharIndicesToRender[mCharIndicesToRender.length-1]));
 		
 		if(savePixelCoords) out.println("spaceWidth = " + (mAvgWidth/2));
 		else out.println("spaceWidth = " + ((float)mAvgWidth/mWidth/2));
 		
 //		if(savePixelCoords) out.println("spacing = " + (int)((mGlobalB-mGlobalT)/69f * 18) );
 //		else out.println("spacing = " + ((mGlobalB-mGlobalT)/69f * 18));
 		
 		if(savePixelCoords) out.println("spacing = " + (int)(5/512f*mWidth));	//spacing is added to kerning
 		else out.println("spacing = " + 5/512f);
 		
 		out.println("# CHAR=minX minY maxX maxY [kernMin(1) kernMax(1) ... kernMin(kernBoxes) kernMax(kernBoxes)]");
 		
 		for(LetterBox box: mBoxes){
         	int x1 = box.outX1;
         	int y1 = box.outY1;
         	int x2 = box.outX2;
         	int y2 = box.outY2;
         	
         	if(savePixelCoords){
         		switch(box.c){
         		case ' ': case '#': case '=': case '!': case ':': case '\\':
         			out.print('\\'); break;
         		}
         		
 	        	out.print(box.c + "=" + x1 + " " + y1 + " " + x2 + " " + y2);
 	        	for(int s=0; s<mKernBoxes; s++){
 	        		if(s==0) out.print(" ");
 	        		else out.print(" ");
 	        		out.print(box.kBoxes[s].l+" "+box.kBoxes[s].r);        				        		
 	        	}
 	        	
 	        	out.println();
 	        	
         	} else {
 
 	        	float w = mWidth;
 	        	float h = mHeight;
 	        		        	
 	        	switch(box.c){
         		case ' ': case '#': case '=': case '!': case ':': case '\\':
         			out.print('\\'); break;
         		}
         		
 	        	out.print(box.c + "=" + x1/w + " " + y1/h + " " + x2/w + " " + y2/h);
 	        	for(int s=0; s<mKernBoxes; s++){
 	        		if(s==0) out.print(" ");
 	        		else out.print(" ");
 	        		out.print((box.kBoxes[s].l)/w+" "+(box.kBoxes[s].r)/h);        				        		
 	        	}
 	        	
 	        	out.println();
         	}
 		}
 	}
 
 	private void WriteCodeSnippet(PrintWriter out) {
     	out.println("public int mKernBoxes = "+mKernBoxes+";");
         out.println("public int mHeight = "+(mGlobalB-mGlobalT)+";");
         out.println("public int mFirstChar = " + mCharIndicesToRender[0] + ";"); 
         out.println("public TextureCoordinate mLetterCoords[] = {");
         		        
         int i=0;
         for(LetterBox box: mBoxes){
         	int x1 = box.outX1;
         	int y1 = box.outY1;
         	int x2 = box.outX2;
         	int y2 = box.outY2;
         	int w = mWidth;
         	int h = mHeight;	        	
         	
         	out.print("\tmGraphics.createTexCoords("+x1+","+y1+","+x2+","+y2+","+w+","+h+")");
         	System.out.println(i+ " char: "+box.c);
         	i++;
         	if(i == mBoxes.length) break;
         	out.println(",\t // char: "+box.c);
         	
         }
         out.print("};");
         
         out.println("\n\n //mKerningInfo[letterIndex][sliceIdx][left/right] = ...");
         
       //int val[letter][slice][side] = {{{1,2},{2,2}},{{3,3},{4,5}},{{1,2},{2,2}},{{3,3},{4,5}}};
         //write kerning info
         
         out.print("public int mKerningInfos[][][] = {\n");
         for(LetterBox box: mBoxes) {
         	out.print("{");
         	for(int s=0; s<mKernBoxes; s++){
         		out.print("{");
         		out.print(box.kBoxes[s].l+","+box.kBoxes[s].r);
         		out.print("}");
         		if(s<mKernBoxes-1)out.print(",");		        		
         	}
         	out.print("}");
         	i++;
         	if(i == mBoxes.length-1) break;
         	out.print(",\t // char: "+box.c + "\n\t" );
         	
         }
         out.print("};");
 	}
 
 	private void ArrangeLetter(Graphics2D g2, Graphics g) {
     	int x = 0;
     	int y = mCharHeight - mGlobalB;
     	
     	g2.setBackground(mTransparent);
     	g2.setColor(mTransparent);
         g2.clearRect(0, 0, mWidth, mHeight);           
     	
     	for(int i=0; i<mCharIndicesToRender.length; i++){
     		LetterBox box = mBoxes[i];
     		int res = placeLetter(g2,g, x,y, (char) mCharIndicesToRender[i].intValue(), box);
     		if(res == 0) {
     			x+= box.w;
     			
     		} else if(res == 1) {
     			i--;
     			x=0;
     			y+=mCharHeight;
     			
     		} else if(res == 2) {System.out.println("Not finished: bottom reached"); break;}
     	}
 	}
 
 	private int placeLetter(Graphics2D g2, Graphics g,int x, int y, char symbol, LetterBox box) {
 		char oldsym = symbol;
 		symbol = lookUpForReplacement(symbol);
 		
 		if(x/*-box.l*/+box.w > mWidth) return 1;
 		
 		g2.setColor(Color.WHITE);
 		g2.drawString( String.valueOf(symbol) , x - box.l, y);
 		
 		//This line adds the character to the image, which will be taken for kerning
 		//g2.drawString( String.valueOf(lookUpForKernReplacement(oldsym)) , x - box.l, y); 
 		
 		if(mShowTextureBoxes) {
 			if(((int)symbol%2) == 0)
 				g2.setColor(Color.RED);
 			else
 				g2.setColor(Color.GREEN);
 			g2.drawRect(x, y + mGlobalT, box.w, mCharHeight);			
 		}
 		
 		box.outX1 = x;
 		box.outY1 = y + mGlobalT;
 		box.outX2 = x+box.w;
 		box.outY2 = box.outY1 + mCharHeight;
 		
 		if(mShowKerningBoxes) {
 			g2.setColor(mKernColor);
 			for(int k=0; k<mKernBoxes; k++) {
 				g2.fillRect((int)(x+box.kBoxes[k].l),
 							(int)( y+mGlobalT+k*mSliceHeight),
 							(int)( box.kBoxes[k].r - box.kBoxes[k].l),
 							(int)( mSliceHeight)); 
 			}
 		}
 		
 		return 0;		
 	}
 
 	private char lookUpForReplacement(char symbol) {
 		if(!mReplaceUndefinedChars) return symbol;
 		
 		switch(symbol) {
			case 228 : return 'a';
			case 246 : return 'o';
			case 252 : return 'u';
			case 196 : return 'A';
			case 214 : return 'O';
			case 220 : return 'U';
 			default: return symbol;
 		}
 	}
 	
 	private char lookUpForKernReplacement(char symbol) {
 		if(!mReplaceUndefinedChars) return symbol;
 		
 		switch(symbol) {
			case 228 : return 'T';
			case 246 : return 'T';
			case 252 : return 'T';
			case 196 : return 'T';
			case 214 : return 'T';
			case 220 : return 'T';
 			default: return symbol;
 		}
 	}
 
 	private void ScanEachLetter(Graphics2D g2, Graphics g) {    	
 		
 		g2.setFont(new Font(FontCreator.mFontName, FontCreator.mFontStyle , mFontSize));
 		
     	for(int i=0; i<mCharIndicesToRender.length; i++){
     		System.out.println((char)mCharIndicesToRender[i].intValue() + " <-> " + mCharIndicesToRender[i]);
 	        LetterBox box = drawLetter((char)mCharIndicesToRender[i].intValue(),g2);
 	        mBoxes[i] = box;
 	        
 	        if((char)mCharIndicesToRender[i].intValue() == '.')
 	        	g.drawImage(canvas, 0,0,this);
 	        	        
 			if(box.t < mGlobalT) mGlobalT = box.t;
 			if(box.b > mGlobalB) mGlobalB = box.b;						
         }    	
     	
     	mCharHeight = mGlobalB - mGlobalT;
     	
         System.err.println("=======================================================");
 	}
 	
 	private void ScanKernBoxes(Graphics2D g2, Graphics g) {    	
 		
 		g2.setFont(new Font(FontCreator.mFontName, FontCreator.mFontStyle , mFontSize));
 		
     	for(int i=0; i<mCharIndicesToRender.length; i++){
     		g2.setColor(Color.WHITE);
             g2.fillRect(0, 0, mWidth, mHeight);
             g2.setColor(Color.BLACK);        
             
             // Draw Text
             if((char)mCharIndicesToRender[i].intValue() == 0x20) // SPACE sign
             	g2.drawString( "l" , mLeftStart ,mBaseLine);
             else {
             	g2.drawString( ""+(lookUpForReplacement((char)mCharIndicesToRender[i].intValue())) , mLeftStart ,mBaseLine);
             	g2.drawString( ""+(lookUpForKernReplacement((char)mCharIndicesToRender[i].intValue())) , mLeftStart ,mBaseLine);
             }
 	        
             KernBox box = getKernBox(canvas, mBoxes[i]);
 	        					
         }    	
     	
     	mCharHeight = mGlobalB - mGlobalT;
     	
         System.err.println("=======================================================");
 	}
 
 	
 	private LetterBox drawLetter(char c, Graphics2D g2) {
     	g2.setColor(Color.WHITE);
         g2.fillRect(0, 0, mWidth, mHeight);
         g2.setColor(Color.BLACK);        
         
         // Draw Text
         if(c == 0x20) //If SPACE sign
         	g2.drawString( "l" , mLeftStart ,mBaseLine);
         else {
         	g2.drawString( String.valueOf(lookUpForReplacement(c)) , mLeftStart ,mBaseLine);
         	g2.drawString( ""+(lookUpForKernReplacement(c)) , mLeftStart ,mBaseLine);
         }
         
         LetterBox box = getBoundingBox(canvas, c);
         
         g2.setColor(Color.GREEN);
         g2.drawLine(box.l, 0, box.l, mHeight);
         g2.drawLine(0, box.t, mWidth, box.t);
         g2.setColor(Color.RED);
         g2.drawLine(box.r, 0, box.r, mHeight);
         g2.drawLine(0, box.b, mWidth, box.b);
         
         g2.setColor(Color.ORANGE);
         g2.drawLine(0, mGlobalB, mWidth, mGlobalB);
         g2.setColor(Color.BLUE);
         g2.drawLine(0, mGlobalT, mWidth, mGlobalT);
         
         System.out.println("Letter = "+c+"\t l t r b: "+box.l + " , "+box.t+ " , " + box.r + " , " + box.b);
         
         return box;
 	}
 
 	private KernBox getKernBox(BufferedImage img, LetterBox box) {
 
 		int startX 	= box.l + mLeftStart;
 		int endX 	= startX+box.w; 
 		
 		int minY = mBaseLine + mGlobalT;
 		int maxY = mBaseLine + mGlobalB;
 		
 		int startY = 0;
 		int endY = 0;
 		
 		
 //    	int b = endY;
 //    	int t = startY; 
 		    	
     	mSliceHeight = ((float)mCharHeight) / mKernBoxes;
     	
 		for(int k=0; k<mKernBoxes; k++){	
 			int l = endX;
 	    	int r = startX;
 			startY 	= (int) (minY + k     * mSliceHeight);
 			endY 	= (int) (minY + (k+1) * mSliceHeight);
 			
 			for(int x=box.l+mLeftStart; x<endX; x++) 			
 	    	for(int y=startY; y<endY; y++){
 	    		int color = img.getRGB(x, y);
 	    		
 	    		int  red   = (color & 0x00ff0000) >> 16;
 	    		int  green = (color & 0x0000ff00) >> 8;
 	    		int  blue  =  color & 0x000000ff;
 	    		  
 	    		if(red != 0xff && blue != 0xff && green!=0xff){
 	    			if(x < l) l = x;
 	    			if(x > r) r = x;
 //	    			if(y < t) t = y;
 //	    			if(y > b) b = y;
 	    		}    
 	    	}
 			
 			KernBox kb = new KernBox();
 			kb.l = l - mLeftStart - box.l;
 			kb.r = r - mLeftStart - box.l;
 			
 			if(kb.l > kb.r) {
 				kb.l = (kb.l+kb.r)/2;
 				kb.r = kb.l;
 			}
 			
 			box.kBoxes[k] = kb;
     	}
 		
 		return null;
 	}
 
 	
 	private LetterBox getBoundingBox(BufferedImage img, char c){
     	
     	int l = mWidth;
     	int r = 0;
     	int b = 0;
     	int t = mHeight;    	
     	
     	for(int x=0; x<mWidth; x++)
     	for(int y=0; y<mHeight; y++){
     		int color = img.getRGB(x, y);
     		
     		int  red   = (color & 0x00ff0000) >> 16;
     		int  green = (color & 0x0000ff00) >> 8;
     		int  blue  =  color & 0x000000ff;
     		  
     		if(red != 0xff && blue != 0xff && green!=0xff){
     			if(x < l) l = x;
     			if(x > r) r = x;
     			if(y < t) t = y;
     			if(y > b) b = y;
     		}    		
     	}	    	 
     	
     	l -= mLeftStart + mBorderHorizontal;
     	r -= mLeftStart - mBorderHorizontal;
     	t -= mBaseLine + mBorderVertical;
     	b -= mBaseLine - mBorderVertical;
     	return new LetterBox(c,l,t,r,b, mKernBoxes);
     }
     
     
 }
