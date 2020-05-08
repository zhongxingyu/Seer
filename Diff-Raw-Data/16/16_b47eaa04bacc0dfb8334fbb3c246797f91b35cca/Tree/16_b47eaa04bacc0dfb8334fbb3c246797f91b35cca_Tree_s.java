 package com.estudio.cheke.game.gstb.objects;
 /*
  * Created by Estudio Cheke, creative purpose.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 import com.estudio.cheke.game.gstb.Cache;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Picture;
 import android.graphics.Rect;
 
 public class Tree extends Object{
 	private int numMaxRamas=4;
 	private int numRamas=4;
 	private int stroke=numMaxRamas*2;
 	private int actualLevel=0;
 	private int maxB=200;
 	private int movX=15;
 	private int movY=15;
 	private int maxX=0,maxY=maxB;
 	private int bordeLeft=0, bordeTop=0;
 	private rama[] ramas;
 	private rama[] oldramas;
 	private Picture arbol;
 	Rect dst=new Rect();
 	private class rama{
 		public int x=0;
 		public int y=0;
 		public rama(final int xx,final int yy){
 			x=xx;
 			y=yy;
 		}
 	}
 	public void makeTree(Canvas canvas){
 		Paint paint=new Paint();
 		paint.setColor(Color.BLACK);
 		paint.setStrokeWidth(stroke);
 		paint.setFlags(1);
 		ramas= new rama[1];
 		ramas[0]= new rama(maxB/2, maxB-50);
 		canvas.drawLine(maxB/2, maxB, ramas[0].x, ramas[0].y, paint);
 		stroke-=2;
 		actualLevel++;
 		makeRamas(canvas,paint);
 	}
 	public Tree(){
 		MakeArbol(false);
 	}
 	public Tree(boolean grey){
 		MakeArbol(grey);
 	}
 	public void MakeArbol(final boolean grey) {
 		new Thread(new Runnable() {
 			public void run() {
 				int widthB=maxB;
 				int heightB=maxB;
				//Bitmap arbol2 = Bitmap.createBitmap(widthB, heightB, Bitmap.Config.ARGB_4444);
				//Canvas canvas = new Canvas();
 				arbol = new Picture();
 				Canvas canvas=arbol.beginRecording(width, height);
 				Paint paint=new Paint();
 				if(grey){
 					paint.setColor(Color.GRAY);
 				}else{
 					paint.setColor(Color.DKGRAY);
 				}
 				paint.setFlags(1);
 				paint.setStrokeWidth(stroke);
 				ramas= new rama[1];
 				ramas[0]= new rama(widthB/2, heightB-50);
 				canvas.drawLine(widthB/2, heightB, ramas[0].x, ramas[0].y, paint);
 				actualLevel++;
 				stroke-=2;
 				makeRamas(canvas,paint);
				//arbol=arbol2;
 				width=(int) (Cache.wpor*33);
 				height=(int) (Cache.wpor*33);
				//arbol=Bitmap.createScaledBitmap(arbol,width,height, true);
 				arbol.endRecording();
 				x=(int) (canvaswidth+800*Math.random());
 				y=(int) (15*Cache.hpor);
 			}
 		}).start();
 	}
 	public boolean contacto(int right, int bottom){
 		boolean touch=false;
 		if(right>=x+bordeLeft&&right<=((x+bordeLeft)+(width-bordeLeft*2))){ 
 			if(bottom>=canvasheight-(y+(height-bordeTop))+Cache.bagUp&&bottom<=canvasheight-(y+Cache.bagUp)){
 				touch=true;
 			}
 		}
 		return touch;
 	}
 	public void move(float timeDeltaSeconds){
 		moveBasic(timeDeltaSeconds);
 		if(x<-width){
 			reinit();
 		}
 	}
 	private void reinit(){
 		x=(int) (canvaswidth*1.5+(800*Math.random()));
 	}

 	public void draw(Canvas canvas) {
		//canvas.drawBitmap(arbol, x, canvasheight-(y + height)+Cache.bagUp, null);
		dst.set(x, canvasheight-(y + height)+Cache.bagUp, x+width, canvasheight-(y + height+height)+Cache.bagUp);
		canvas.drawPicture(arbol, dst);
 	}
 	private void makeRamas(Canvas canvas,Paint paint){
 		oldramas=ramas;
 		ramas=new rama[makeArrayNum()];
 		paint.setStrokeWidth(stroke);
 		movX=(int) (movX*Math.random()+movY/2);
 		movY=(int) (movY*Math.random()+movY/2);
 		for(int r=0;r<oldramas.length;r++){
 			switch(numRamas){
 			case 4:
 				canvas.drawLine(oldramas[r].x, oldramas[r].y, oldramas[r].x+movX/2, oldramas[r].y-movY*2, paint);
 			case 3:
 				canvas.drawLine(oldramas[r].x, oldramas[r].y, oldramas[r].x-movX/2, oldramas[r].y-movY*2, paint);
 			case 2:
 				canvas.drawLine(oldramas[r].x, oldramas[r].y, oldramas[r].x+movX, oldramas[r].y-movY, paint);
 				canvas.drawLine(oldramas[r].x, oldramas[r].y, oldramas[r].x-movX, oldramas[r].y-movY, paint);
 				break;
 			}
 			if(r==0){
 				switch(numRamas){
 				case 4:
 					ramas[3]= new rama(oldramas[r].x+movX*2, oldramas[r].y-movY*2);
 				case 3:
 					ramas[2]= new rama(oldramas[r].x-movX*2, oldramas[r].y-movY*2);
 				case 2:
 					ramas[0]= new rama(oldramas[r].x+movX, oldramas[r].y-movY);
 					ramas[1]= new rama(oldramas[r].x-movX, oldramas[r].y-movY);
 					break;
 				}
 			}else{
 				switch(numRamas){
 				case 4:
 					ramas[makeNumArray(r)+3]= new rama(oldramas[r].x+movX/2, oldramas[r].y-movY*2);
 					maxX=maxX<oldramas[r].x+movX/2?oldramas[r].x+movX/2:maxX;
 					maxY=maxY>oldramas[r].y-movY*2?oldramas[r].y-movY*2:maxY;
 				case 3:
 					ramas[makeNumArray(r)+2]= new rama(oldramas[r].x-movX/2, oldramas[r].y-movY*2);
 				case 2:
 					ramas[makeNumArray(r)]= new rama(oldramas[r].x+movX, oldramas[r].y-movY);
 					ramas[makeNumArray(r)+1]= new rama(oldramas[r].x-movX, oldramas[r].y-movY);
 					break;
 				}
 			}
 		}
 		actualLevel++;
 		stroke-=2;
 		if(actualLevel<numMaxRamas){
 			makeRamas(canvas,paint);
 		}else{
 			actualLevel=0;
 			makebordes(maxX,maxY);
 			oldramas=ramas=null;
 		}
 	}
 	private int makeNumArray(int r){
 		int array=0;
 		for(int a=0;a<r;a++){
 			array+=numRamas;
 		}
 		return array;
 	}
 	private int makeArrayNum(){
 		int array=1;
 		for(int a=0;a<actualLevel;a++){
 			array*=numRamas;
 		}
 		return array;
 	}
 	private void makebordes(int x, int maxY){
 		//bordeLeft=(300-x)/2;
 		bordeTop=maxY;
 		bordeLeft=(int) (((300-x)/2)*(Cache.wpor*33)/200);
 	}
 }
