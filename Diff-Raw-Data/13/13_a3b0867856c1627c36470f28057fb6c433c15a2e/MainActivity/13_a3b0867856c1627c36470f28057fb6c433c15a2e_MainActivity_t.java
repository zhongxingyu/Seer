 package pro.oneredpixel.deflektorclassic;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 
 public class MainActivity extends Activity implements OnClickListener, OnTouchListener {
 
 	final int field_width = 15;
 	final int field_height = 9;
 	int field[];
 	
 	final int FLD_NULL = 0;
 	final int FLD_LASER_GUN = 0x0100;
 	final int FLD_RECEIVER = 0x0200;
 	final int FLD_MIRROR = 0x0300;
 	final int FLD_WARPBOX = 0x0400;			//(teleport) warpbox will connect you with next warpbox
 	final int FLD_CELL = 0x500;
 	final int FLD_MINE = 0x600;				//Mine causes overload and increases overload meter
 	final int FLD_WALL_A = 0x700;			//reflects laser
 	final int FLD_WALL_B = 0x800;			//stops laser
 	final int FLD_PRISM = 0x900;			//prism turns laser at random
 	final int FLD_SLIT_A = 0xa00;			//reflects laser if angle is different - 
 	final int FLD_SLIT_B = 0xb00;			//if the angle is different the laser will stop - 
 	final int FLD_EXPLODE = 0xc00;			// CELL/WALL/MINE  -  
 	
 	final int FLD_AUTOROTATING = 0x2000;	//autorotate
 	final int FLD_EXPLODEONEND = 0x4000;		//kill this brick when all cells burned off
 	
 	
 	ImageView iv;
     Bitmap bm=null;
     Sprite spr;
 	
 	//  
 	int angleNodeSteps[][]={
 		{0,-2}, //0
 		{1,-2}, //1
 		{2,-2}, //2
 		{2,-1}, //3
 		{2,0},  //4
 		{2,1}, //5
 		{2,2}, //6
 		{1,2}, //7
 		{0,2}, //8
 		{-1,2}, //9
 		{-2,2}, //10
 		{-2,1}, //11
 		{-2,0}, //12
 		{-2,-1},//13
 		{-2,-2},//14
 		{-1,-2},//15
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		iv=(ImageView) findViewById(R.id.iv1);
         iv.setOnClickListener(this);
         iv.setOnTouchListener(this);
 		
 		bm=Bitmap.createBitmap(field_width*16, field_height*16, Bitmap.Config.ARGB_8888);
 		
 		spr=new Sprite(R.drawable.defsprites);
 		
 		
 		field=new int[field_width*field_height];
 		
 		field[0+0*field_width]=FLD_MIRROR|6;
 		field[2+2*field_width]=FLD_MIRROR|FLD_AUTOROTATING;
 		field[2+5*field_width]=FLD_MIRROR;
 		field[0+7*field_width]=FLD_MIRROR|12;
 		field[8+0*field_width]=FLD_MIRROR;
 		field[8+8*field_width]=FLD_MIRROR;
 		field[14+3*field_width]=FLD_MIRROR;
 		field[14+5*field_width]=FLD_MIRROR;
 
 		
 		field[1+1*field_width]=FLD_CELL;
 		field[1+2*field_width]=FLD_CELL;
 		field[1+3*field_width]=FLD_CELL;
 		field[2+1*field_width]=FLD_CELL;
 		field[2+3*field_width]=FLD_CELL;
 		field[3+1*field_width]=FLD_CELL;
 		field[3+2*field_width]=FLD_CELL;
 		field[3+3*field_width]=FLD_CELL;
 		
 		field[5+0*field_width]=FLD_CELL;
 		field[6+0*field_width]=FLD_CELL;
 		field[5+1*field_width]=FLD_CELL;
 		field[6+1*field_width]=FLD_CELL;
 		field[11+0*field_width]=FLD_CELL;
 		field[12+0*field_width]=FLD_CELL;
 		field[11+1*field_width]=FLD_CELL;
 		field[9+4*field_width]=FLD_CELL;
 		field[10+7*field_width]=FLD_CELL;
 		field[10+8*field_width]=FLD_CELL;
 		field[12+7*field_width]=FLD_CELL;
 		
 		field[11+6*field_width]=FLD_MINE;
 		field[11+7*field_width]=FLD_MINE;
 		
 		field[5+2*field_width]=FLD_PRISM;
 		
 		field[7+4*field_width]=FLD_SLIT_A|4|FLD_AUTOROTATING;
 		field[10+0*field_width]=FLD_SLIT_B|7|FLD_AUTOROTATING;
 		field[12+2*field_width]=FLD_SLIT_B|7;
 		field[12+6*field_width]=FLD_SLIT_B|7;
 		
 		field[10+3*field_width]=FLD_WARPBOX|1;
 		field[14+7*field_width]=FLD_WARPBOX|1;
 		
 		field[4+0*field_width]=FLD_WALL_B|0x0a;
 		field[4+1*field_width]=FLD_WALL_B|0x0a;
 		field[4+2*field_width]=FLD_WALL_B|0x0f;
 		field[4+3*field_width]=FLD_WALL_B|0x0f;
 		field[4+4*field_width]=FLD_WALL_B|0x0f;
 		field[1+4*field_width]=FLD_WALL_B|0x0c;
 		field[2+4*field_width]=FLD_WALL_B|0x0c;
 		field[3+4*field_width]=FLD_WALL_B|0x0c;
 		
 		field[2+8*field_width]=FLD_WALL_A|0x0a;
 		field[4+8*field_width]=FLD_WALL_A|0x05|FLD_EXPLODEONEND;
 		field[4+7*field_width]=FLD_WALL_A|0x05;
 		field[6+2*field_width]=FLD_WALL_A|0x0c;
 		field[7+2*field_width]=FLD_WALL_A|0x0c;
 		field[7+1*field_width]=FLD_WALL_A|0x05;
 		field[7+0*field_width]=FLD_WALL_A|0x05;
 		field[13+0*field_width]=FLD_WALL_A|0x05;
 		field[14+0*field_width]=FLD_WALL_A|0x0b;
 		field[14+1*field_width]=FLD_WALL_A|0x05;
 		field[14+8*field_width]=FLD_WALL_A|0x02;
 		field[13+8*field_width]=FLD_WALL_A|0x03;
 		field[12+8*field_width]=FLD_WALL_A|0x03;
 		field[9+8*field_width]=FLD_WALL_A|0x05;
 		field[9+7*field_width]=FLD_WALL_A|0x0d;
 		
 		field[3+7*field_width]=FLD_LASER_GUN|3;
 		field[3+8*field_width]=FLD_RECEIVER|1;
 		
 		drawField();
 		animateField();
 		
 		iv.setImageBitmap(bm);
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	void animateField() {
 		animateField(-1,-1);
 	}
 	
 	void animateField(int x, int y) {
 		int f=0;
 		boolean needToExplodeBarrier=true;
 		for (int i=0;i<field_width;i++) 
 			for (int j=0;j<field_height;j++) {
 				f=field[j*field_width+i];
 				if (((f&FLD_AUTOROTATING)!=0) && ((x!=i) || (y!=j)) ) rotateThing(i,j);
 				if ((f&0xf00)==FLD_EXPLODE) {
 					f++;
 					if ((f&0xf)>4) f=FLD_NULL;
 					else needToExplodeBarrier = false;
 					field[j*field_width+i]=f;
 				};
 				if ((f&0xF00)==FLD_CELL) needToExplodeBarrier = false;
 			};
 		if (needToExplodeBarrier)
 			for (int i=0;i<field_width*field_height;i++)
 				if ((field[i]&FLD_EXPLODEONEND)!=0) field[i]=FLD_EXPLODE;
 	}
 	
 	void rotateThing(int x, int y) {
 		int f=field[y*field_width+x];
 		field[y*field_width+x]=(++f)&0xFFFFFF1F;
 	}
 	
 	void drawField() {
 		int f_angle;
 		int beam_x=0;
 		int beam_y=0;
 		int beam_angle=0;
 
 		for (int i=0;i<field_width;i++) 
 			for (int j=0;j<field_height;j++) {
 				f_angle=field[j*field_width+i]&0x1f;
 				switch (field[j*field_width+i]&0xf00) {
 				case FLD_NULL:
 					spr.putRegion(bm, i*16, j*16, 8, 8, 7*16, 5*16+8);
 					spr.putRegion(bm, i*16+8, j*16, 8, 8, 7*16, 5*16+8);
 					spr.putRegion(bm, i*16, j*16+8, 8, 8, 7*16, 5*16+8);
 					spr.putRegion(bm, i*16+8, j*16+8, 8, 8, 7*16, 5*16+8);
 					break;
 				case FLD_LASER_GUN:
 					putLaser(i,j,f_angle);
 					spr.putRegion(bm, i*16, j*16, 16, 16, ((f_angle&3)*16), 4*16);
 					beam_x=i*4+2+angleNodeSteps[f_angle*4][0];
 					beam_y=j*4+2+angleNodeSteps[f_angle*4][1];
 					beam_angle=(f_angle&3)*4;
 					break;
 				case FLD_RECEIVER:
 					putReceiver(i,j,f_angle);
 					break;
 				case FLD_MIRROR:
 					putMirror(i,j,f_angle);
 					break;
 				case FLD_WARPBOX:
 					putWarpbox(i,j,f_angle);
 					break;
 				case FLD_CELL:
 					putCell(i,j);
 					break;
 				case FLD_MINE:
 					putMine(i,j);
 					break;
 				case FLD_WALL_A:
 					putWallA(i,j,f_angle);
 					break;
 				case FLD_WALL_B:
 					putWallB(i,j,f_angle);
 					break;
 				case FLD_PRISM:
 					putPrism(i,j);
 					break;
 				case FLD_SLIT_A:
 					putSlitA(i,j,f_angle);
 					break;
 				case FLD_SLIT_B:
 					putSlitB(i,j,f_angle);
 					break;
 				case FLD_EXPLODE:
 					spr.putRegion(bm, i*16, j*16, 16, 16, ((f_angle&7)*16), 6*16);
 					break;
 				}
 			};
 			drawBeam(beam_x,beam_y,beam_angle);
 	};
 
 	//angle=0..31
 	void putMirror(int x, int y, int angle) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, (angle&7)*16, ((angle>>3)&1)*16);
 	}
 	
 	//angle=0..3
 	void putLaser(int x,int y, int angle) {
 
 	}
 	
 	void putReceiver(int x,int y, int angle) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, (((angle&3)+4)*16), 4*16);
 	}
 	
 	void putCell(int x, int y) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, 0, 5*16);
 	}
 	
 	void putMine(int x, int y) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, 16, 5*16);
 	}
 	
 	void putPrism(int x, int y) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, 6*16, 5*16);
 	}
 	
 	void putWarpbox(int x, int y, int type) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, (((type&3)+2)*16), 5*16);
 	}
 	
 	void putSlitA(int x, int y, int angle) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, ((angle&7)*16), 3*16);
 	}
 
 	void putSlitB(int x, int y, int angle) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, ((angle&7)*16), 2*16);
 	}
 	
 	void putWallA(int x, int y, int type) {
 		if ((type&8)!=0)	spr.putRegion(bm, x*16, y*16, 8, 8, 7*16+8, 5*16);
 		else				spr.putRegion(bm, x*16, y*16, 8, 8, 7*16, 5*16+8);
 		if ((type&4)!=0)	spr.putRegion(bm, x*16+8, y*16, 8, 8, 7*16+8, 5*16);
 		else				spr.putRegion(bm, x*16+8, y*16, 8, 8, 7*16, 5*16+8);
 		if ((type&2)!=0)	spr.putRegion(bm, x*16, y*16+8, 8, 8, 7*16+8, 5*16);
 		else				spr.putRegion(bm, x*16, y*16+8, 8, 8, 7*16, 5*16+8);
 		if ((type&1)!=0)	spr.putRegion(bm, x*16+8, y*16+8, 8, 8, 7*16+8, 5*16);
 		else				spr.putRegion(bm, x*16+8, y*16+8, 8, 8, 7*16, 5*16+8);
 	}
 	
 	void putWallB(int x, int y, int type) {
 		if ((type&8)!=0)	spr.putRegion(bm, x*16, y*16, 8, 8, 7*16, 5*16);
 		else				spr.putRegion(bm, x*16, y*16, 8, 8, 7*16, 5*16+8);
 		if ((type&4)!=0)	spr.putRegion(bm, x*16+8, y*16, 8, 8, 7*16, 5*16);
 		else				spr.putRegion(bm, x*16+8, y*16, 8, 8, 7*16, 5*16+8);
 		if ((type&2)!=0)	spr.putRegion(bm, x*16, y*16+8, 8, 8, 7*16, 5*16);
 		else				spr.putRegion(bm, x*16, y*16+8, 8, 8, 7*16, 5*16+8);
 		if ((type&1)!=0)	spr.putRegion(bm, x*16+8, y*16+8, 8, 8, 7*16, 5*16);
 		else				spr.putRegion(bm, x*16+8, y*16+8, 8, 8, 7*16, 5*16+8);
 	}
 	
 	void drawBeam(int beam_x, int beam_y, int beam_angle) {
 		
 		int new_beam_x;
 		int new_beam_y;
 		int new_beam_angle;
 		boolean endBeam=false;
 		
 		while (!endBeam) {
 			
 			int sx=beam_x&3;
 			int sy=beam_y&3;
 			int fx=beam_x/4;
 			int fy=beam_y/4;
 			int f=field[fx+fy*field_width];
 			int f_angle=f&0x1f;
 			
 			new_beam_angle=beam_angle;
 			
 			if ((sx==2) && (sy==2)) {
 				switch (f&0x0f00) {
 				case FLD_NULL:
 					break;
 				case FLD_LASER_GUN:
 					//TODO:   
 					endBeam=true;
 					continue;
 				case FLD_RECEIVER:
 					break;
 				case FLD_MIRROR:
 					new_beam_angle =(((f_angle<<1)-beam_angle-beam_angle)>>1)&0xf;
 					break;
 				case FLD_WARPBOX:
 					for (int i=0;i<field.length;i++) {
						if ( (field[i]==f) && (i!=(fx+fy*field_width))) {
 							beam_y=(i/field_width)*4+2;
 							beam_x=(i-(((int)(beam_y/4))*field_width))*4+2;
 							break;
 						};
 					};
 					break;
 				case FLD_CELL:
 					field[fx+fy*field_width]=FLD_EXPLODE;
 					endBeam=true;
 					continue;
 				case FLD_MINE:
 					//TODO:   
 					endBeam=true;
 					continue;
				//case FLD_WALL_A:
				//	break;
				//case FLD_WALL_B:
				//	break;
 				case FLD_PRISM:
 					new_beam_angle= (((beam_angle+1)&0xc)-4+((int)((8*Math.random())+0.5)))&0xf;//;
 					break;
 				//case FLD_SLIT_A:
 				//	break;
 				//case FLD_SLIT_B:
 				//	break;
 				case FLD_EXPLODE:
 					if (f_angle>2) break;
 					endBeam=true;
 					continue;
 				}
 			}
 			
 			new_beam_x = beam_x+angleNodeSteps[new_beam_angle][0];
 			new_beam_y = beam_y+angleNodeSteps[new_beam_angle][1];
 			if (new_beam_x>=field_width*4 || new_beam_x<0 || new_beam_y>=field_height*4 || new_beam_y<0) {
 				endBeam=true;
 				continue;
 			};
 
 			int mp_beam_x = (beam_x+beam_x+angleNodeSteps[new_beam_angle][0])/2;
 			int mp_beam_y = (beam_y+beam_y+angleNodeSteps[new_beam_angle][1])/2; 	
 			
 			int f1=field[(mp_beam_x/4)+(mp_beam_y/4)*field_width];
 			switch (f1&0x0F00) {
 			case FLD_WALL_A:
 				if ((beam_x&3)==0)
 					beam_angle=(0-beam_angle)&0xf;
 				if ((beam_y&3)==0)
 					beam_angle=(4*2-beam_angle)&0xf;
 				continue;
 			case FLD_WALL_B:
				
 				endBeam=true;
 				continue;
 			case FLD_SLIT_A:
 				if ((f1&7)!=(beam_angle&7)) {
 					if ((beam_x&3)==0)
 						beam_angle=(0-beam_angle)&0xf;
 					if ((beam_y&3)==0)
 						beam_angle=(4*2-beam_angle)&0xf;
 					continue;
 				};
 				break;
 			case FLD_SLIT_B:
 				if ((f1&7)!=(beam_angle&7)) {
 					endBeam=true;
 					continue;
 				};
 				break;
 			}
 			
 			drawline (bm,beam_x*4,beam_y*4,new_beam_x*4,new_beam_y*4,0xFFFFFFFF);
 				
 			beam_x = new_beam_x;
 			beam_y = new_beam_y;
 			beam_angle = new_beam_angle;
 			
 		}
 	};
 	
 	void drawline(Bitmap bitmap, int x1, int y1, int x2, int y2, int colour1)
 	{
 		int x0;
 		int y0;
 		int sx = Math.abs(x2-x1);
 		int sy = Math.abs(y2-y1);
 		int zx = ((x2>x1)?1:(-1));
 		int zy = ((y2>y1)?1:(-1));
 		int err=0;
 		
 		if (sx>=sy) {
 			y0=y1;
 			for (x0=x1; x0!=x2; x0+=zx) {
 				bitmap.setPixel(x0, y0, colour1);
 				err+=sy;
 				if (2*err >= sx) {
 					y0+=zy;
 					err-=sx;
 				};
 			};
 			//     .
 			bitmap.setPixel(x2, y2, colour1);
 		} else if (sy>sx) {
 			x0=x1;
 			for (y0=y1; y0!=y2; y0+=zy) {
 				bitmap.setPixel(x0, y0, colour1);
 				err+=sx;
 				if (2*err >= sy) {
 					x0+=zx;
 					err-=sy;
 				};
 			};
 			//     .
 			bitmap.setPixel(x2, y2, colour1);
 		};
 	};
 	
 	class Sprite {
 		int width=0;
 		int height=0;
 		int data[]=null;
 		
 		Sprite (int res_id) {
 			Bitmap b=BitmapFactory.decodeResource(getResources(), res_id);
 			if (b!=null) { 
 				width = b.getWidth();
 				height = b.getHeight();
 				data = new int[width*height];
 				b.getPixels(data, 0, width, 0, 0, width, height);
 			};
 		};
 		
 		//x,y,sx,sy - space in bitmap to put sprite in,
 		//bx,by - coordinates in sprite where begins region
 		void putRegion(Bitmap bitmap, int x, int y, int sx, int sy, int bx, int by) {
 			if (width>0 && height>0 && data!=null) {
 				bitmap.setPixels(data, by*width+bx, width, x, y, sx, sy);
 			};
 		};
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean onTouch(View arg0, MotionEvent ev) {
 		if (ev.getAction()==MotionEvent.ACTION_DOWN) {
 			int x = (int)(ev.getX()/(arg0.getWidth()/field_width));
 			int y = (int)(ev.getY()/(arg0.getHeight()/field_height));
 			int f=field[y*field_width+x];
 			if ((f&0xFF00)==FLD_MIRROR) {
 				rotateThing(x,y);
 			};
 			drawField();
 			animateField();
 			iv.invalidate();
 			return true;
 		}
 		return false;
 	}
 
 }
