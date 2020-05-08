 package net.azulite.Amanatsu;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.res.AssetManager;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.graphics.Color;
 import android.opengl.GLUtils;
 import android.opengl.Matrix;
 
 /**
  * @author Hiroki
  */
 
 
 // Library.
 // TODO:
 // * screen size
 // * auto change 1.1, 2.0
 
 public class AmanatsuDraw
 {
   private Amanatsu ama;
   private GL10 gl = null;
   private int width, height;
   private float basex, basey, screenwidth, screenheight;
   private Resources resource;
   private AssetManager assets;
   private static Map<Integer, Texture> textures = new Hashtable< Integer, Texture >( 50 );
   private static Map<Integer, Paint> paints = new Hashtable< Integer, Paint >( 50 );
 
   // String.
   private Bitmap stringbmp;
   private Texture stringtex;
   private int stringnum;
 
   // tmp.
   private float[] farr4, farr8, mat;
   private Texture ttex;
   private Paint tpaint;
 
   public AmanatsuDraw( Amanatsu ama )
   {
     this.ama = ama;
     resource = ama.getContext().getResources();
     assets = ama.getContext().getResources().getAssets();
     farr4 = new float[ 4 ];
     farr8 = new float[ 8 ];
     mat = new float[ 16 ];
 
     stringbmp = Bitmap.createBitmap( 1024, 1024, Config.ALPHA_8 );//Bitmap.Config.ARGB_8888);
     createFont( 0, 30 );
   }
 public String getGLVersion(){ return gl.glGetString( GL10.GL_VERSION ); }
   public void init()
   {
     createTextureFromBitmap( 0, stringbmp, false );
     stringtex = ttex;
     stringnum = ttex.texid[ 0 ];
     stringtex.col  = createFloatBuffer( new float[]{ 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f } );
   }
 
  public boolean SetScreenSize( float x, float y, float width, float height )
   {
     basex = x;
     basey = y;
     screenwidth = width;
     screenheight = height;
     width = (this.width * this.width) / width;
     height = (this.height * this.height) / height;
 
     gl.glViewport( (int)-basex, (this.height - (int)height) + (int)basey, (int)width, (int)height );
     //gl.glViewport( (int)basex, (this.height - (int)height) - (int)basey, (int)screenwidth, (int)screenheight );
     //gl.glMatrixMode( GL10.GL_PROJECTION );
     //gl.glLoadIdentity();    //gl.glViewport( (int)basex, (int)basey, (int)screenwidth, (int)screenheight );
     //gl.glOrthof( basex, screenwidth, screenheight, basey, 50.0f, -50.0f );//TODO
     //gl.glOrthof( 0.0f, width, height, 0.0f, 50.0f, -50.0f );//TODO
 
     return true;
   }
 
  public boolean MoveScreen( float x, float y )
   {
     basex = x;
     basey = y;
     gl.glViewport( (int)basex, (int)basey, (int)screenwidth, (int)screenheight );
     return true;
   }
 
   public void release()
   {
     stringbmp.recycle();
   }
 
   public boolean setGL( GL10 gl )
   {
     this.gl = gl;
     return true;
   }
 
   public float getFps()
   {
     return ama.render.getFps();
   }
 
   public float setFps( float fps )
   {
     return ama.render.setFps( fps );
   }
 
   public boolean setRender( int type )
   {
     switch ( type )
     {
     case Amanatsu.DRAW_ADD:
       gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
       break;
 /*    case Amanatsu.DRAW_SUB:
       gl.glBlendEquationEXT( GL11ExtensionPack.GL_FUNC_REVERSE_SUBTRACT_EXT );
       gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
       break;*/
     case Amanatsu.DRAW_MUL:
       gl.glBlendFunc( GL10.GL_ZERO, GL10.GL_SRC_COLOR );
       //dst = dst * src * alpha;
       //gl.glBlendFunc(GL10.GL_ZERO, GL10.GL_SRC_COLOR);
       //gl.glBlendFunc(GL10.GL_ZERO, GL10.GL_SRC_ALPHA);
       break;
     default:
       gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
     }
     return true;
   }
 
   /**
    * Clear Screen(black).
    */
   public boolean clearScreen()
   {
     return clearScreen( 0.0f, 0.0f, 0.0f );
   }
 
   /**
    * Clear Screen.
    * @param red Red color(0.0f-1.0f).
    * @param red Green color(0.0f-1.0f).
    * @param red Blue color(0.0f-1.0f).
    */
   public boolean clearScreen( GameColor color )
   {
     return clearScreen( color.color[ 0 ], color.color[ 1 ], color.color[ 2 ] );
   }
 
   /**
    * Clear Screen.
    * @param red Red color(0.0f-1.0f).
    * @param red Green color(0.0f-1.0f).
    * @param red Blue color(0.0f-1.0f).
    */
   public boolean clearScreen( float[] color )
   {
     return clearScreen( color[ 0 ], color[ 1 ], color[ 2 ] );
   }
 
   public boolean clearScreen( float red, float green, float blue )
   {
     gl.glClearColor( red, green, blue, 1.0f );
     gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
 
     return true;
   }
 
   /**
    * @param x Draw x oordinate.
    * @param y Draw y cordinate.
    * @param w Width.
    * @param h Height.
    * @param color [ red, green, blue, alpha ] array(value 0.0f-1.0f).
    * */
   public boolean drawBox( float x, float y, float w, float h, GameColor color )
   {
     return drawBox( x, y, w, h, color.color );
   }
   public boolean drawBox( float x, float y, float w, float h, float[] color )
   {
     setFloatArray8( x, y, x + w, y, x, y + h, x + w, y + h );
 
     gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, createFloatBuffer( farr8 ) );
     gl.glEnableClientState( GL10.GL_VERTEX_ARRAY );
 
     gl.glColorPointer( 4, GL10.GL_FLOAT, 0, createColor( color ) );
     gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
 
     gl.glDrawArrays( GL10.GL_TRIANGLE_STRIP, 0, 4 );
 
     return true;
   }
 
   /**
    * @param x Draw x oordinate(center).
    * @param y Draw y cordinate(center).
    * @param w Width.
    * @param h Height.
    * @param color [ red, green, blue, alpha ] array(value 0.0f-1.0f).
    * */
   public boolean drawBoxC( float x, float y, float w, float h, GameColor color )
   {
     return drawBoxC( x, y, w, h, color.color );
   }
 
   public boolean drawBoxC( float x, float y, float w, float h, float[] color )
   {
     setFloatArray8( x - w / 2.0f, y - h / 2.0f, x + w / 2.0f, y - h / 2.0f, x - w / 2.0f, y + h / 2.0f, x + w / 2.0f, y + h / 2.0f );
 
     gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, createFloatBuffer( farr8 ) );
     gl.glEnableClientState( GL10.GL_VERTEX_ARRAY );
 
     gl.glColorPointer( 4, GL10.GL_FLOAT, 0, createColor( color ) );
     gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
 
     gl.glDrawArrays( GL10.GL_TRIANGLE_STRIP, 0, 4 );
 
     return true;
   }
 
   public int createTexture( int rnum )
   {
     return createTextureFromBitmap( rnum, BitmapFactory.decodeResource( resource, rnum ), true );
   }
 
   public int createTexture( int tnum, int rnum )
   {
     return createTextureFromBitmap( tnum, BitmapFactory.decodeResource( resource, rnum ), true );
   }
 
   public int createTexture( int tnum, String path )
   {
     try
     {
       InputStream is = assets.open( path );
       return createTextureFromBitmap( tnum, BitmapFactory.decodeStream( is ), true );
     } catch (IOException e)
     {
     }
     return -1;
   }
 
   public int createTexture( int rnum, Bitmap bmp )
   {
     return createTextureFromBitmap( rnum, bmp, true );
   }
 
   private int createTextureFromBitmap( int rnum, Bitmap bmp, boolean regist )
   {
     if ( bmp == null )
     {
       return -1;
     }
 
     if ( regist )
     {
       if ( textures.containsKey( rnum ) )
       {
         releaseTexture( rnum );
         ttex = textures.get( rnum );
       } else
       {
         ttex = new Texture();
         ttex.texid = new int [ 1 ];
         textures.put( rnum, ttex );
       }
     } else
     {
       // Unregist.
       ttex = new Texture();
       ttex.texid = new int [ 1 ];
     }
 
     gl.glGenTextures( 1, ttex.texid, 0 );
     gl.glBindTexture( GL10.GL_TEXTURE_2D, ttex.texid[ 0 ] );
 
     if ( AmanatsuDraw.isPowerOf2( bmp.getWidth() ) == false ||
          AmanatsuDraw.isPowerOf2( bmp.getHeight() ) == false ||
          bmp.getWidth() != bmp.getHeight() )
     {
       int length = AmanatsuDraw.convertPowerOf2( bmp.getWidth() >= bmp.getHeight() ? bmp.getWidth() : bmp.getHeight() );
       bmp = AmanatsuDraw.resizeBitmap( bmp, length );
     }
     GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, bmp, 0 );
 
     gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR );
     //gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR );
     gl.glBindTexture( GL10.GL_TEXTURE_2D, 0 );
 
     ttex.width = bmp.getWidth();
     ttex.height = bmp.getHeight();
 
     if ( regist )
     {
       setColor( rnum );
       bmp.recycle();
     }
 
     return ttex.texid[ 0 ];
   }
 
   public boolean existTexture( int rnum ){ return (textures.containsKey( rnum ) && textures.get( rnum ).texid != null );}
 
   public void destroyTexture( int rnum )
   {
     if ( releaseTexture( rnum ) )
     {
       textures.remove( rnum );
     }
   }
 
   public boolean releaseTexture( int rnum )
   {
     if ( existTexture( rnum ) )
     {
       gl.glDeleteTextures( 1, textures.get( rnum ).texid, 0 );
       return true;
     }
     return false;
   }
 
   public void releaseTextureAll()
   {
     List<Integer> keys = new ArrayList<Integer>( textures.keySet() );
     for ( Integer key : keys )
     {
       releaseTexture( key );
     }
   }
 
   private boolean drawTexture( Texture tex )
   {
     gl.glEnable( GL10.GL_TEXTURE_2D );
     gl.glBindTexture( GL10.GL_TEXTURE_2D, tex.texid[ 0 ] );
 
     gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, tex.ver );
     gl.glEnableClientState( GL10.GL_VERTEX_ARRAY );
 
     gl.glColorPointer( 4, GL10.GL_FLOAT, 0, tex.col );
     gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
 
     gl.glTexCoordPointer( 2, GL10.GL_FLOAT, 0, tex.uv );
     gl.glEnableClientState( GL10.GL_TEXTURE_COORD_ARRAY );
 
     gl.glDrawArrays( GL10.GL_TRIANGLE_STRIP, 0, 4 );
 
     gl.glDisableClientState( GL10.GL_TEXTURE_COORD_ARRAY );
 
     gl.glDisable( GL10.GL_TEXTURE_2D );
 
     return true;
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param dx Draw x coordinate.
    * @param dy Draw y coordinate.
    * */
   public boolean drawTexture( int rnum, float dx, float dy )
   {
 
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     setFloatArray8( dx, dy, dx + ttex.width, dy, dx, dy + ttex.height, dx + ttex.width, dy + ttex.height );
     ttex.ver = createFloatBuffer( farr8 );
 
     setFloatArray8( 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f );
     ttex.uv   = createFloatBuffer( farr8 );
 
     return drawTexture( ttex );
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param dx Draw x coordinate(center).
    * @param dy Draw y coordinate(center).
    * */
   public boolean drawTextureC( int rnum, float dx, float dy )
   {
 
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     setFloatArray8(
       dx - ttex.width / 2.0f, dy - ttex.height / 2.0f,
       dx + ttex.width / 2.0f, dy - ttex.height / 2.0f,
       dx - ttex.width / 2.0f, dy + ttex.height / 2.0f,
       dx + ttex.width / 2.0f, dy + ttex.height );
     ttex.ver = createFloatBuffer( farr8 );
 
     setFloatArray8( 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f );
     ttex.uv   = createFloatBuffer( farr8 );
 
     return drawTexture( ttex );
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx Draw x coordinate.
    * @param dy Draw y coordinate.
    * */
   public boolean drawTexture( int rnum,
       float rx, float ry, float w, float h,
       float dx, float dy )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     setFloatArray8( dx, dy, dx + w, dy, dx, dy + h, dx + w, dy + h );
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     return drawTexture( ttex );
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx Draw x coordinate(center).
    * @param dy Draw y coordinate(center).
    * */
   public boolean drawTextureC( int rnum,
     float rx, float ry, float w, float h,
     float dx, float dy )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     setFloatArray8(
       dx - w / 2.0f, dy - h / 2.0f,
       dx + w / 2.0f, dy - h / 2.0f,
       dx - w / 2.0f, dy + h / 2.0f,
       dx + w / 2.0f, dy + h / 2.0f );
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     return drawTexture( ttex );
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx Draw x coordinate.
    * @param dy Draw y coordinate.
    * @param dw Draw width.
    * @param dh Draw height.
    * */
   public boolean drawTextureScaring( int rnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float dw, float dh )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     setFloatArray8( dx, dy, dx + dw, dy, dx, dy + dh, dx + dw, dy + dh );
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     return drawTexture( ttex );
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx Draw x coordinate(center).
    * @param dy Draw y coordinate(center).
    * @param dw Draw width.
    * @param dh Draw height.
    * */
   public boolean drawTextureScaringC( int rnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float dw, float dh )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     setFloatArray8(
       dx - dw / 2.0f, dy - dh / 2.0f,
       dx + dw / 2.0f, dy - dh / 2.0f,
       dx - dw / 2.0f, dy + dh / 2.0f,
       dx + dw / 2.0f, dy + dh / 2.0f );
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     return drawTexture( ttex );
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx Draw x coordinate.
    * @param dy Draw y coordinate.
    * @param scale Draw scale.
    * */
   public boolean drawTextureScaring( int rnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float scale )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     setFloatArray8( dx, dy, dx + w * scale, dy, dx, dy + h * scale, dx + w * scale, dy + h * scale );
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     return drawTexture( ttex );
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx Draw x coordinate(center).
    * @param dy Draw y coordinate(center).
    * @param scale Draw scale.
    * */
   public boolean drawTextureScaringC( int rnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float scale )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     scale /= 2.0f;
     setFloatArray8(
       dx - w * scale, dy - h * scale,
       dx + w * scale, dy - h * scale,
       dx - w * scale, dy + h * scale,
       dx + w * scale, dy + h * scale );
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     return drawTexture( ttex );
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx Draw x coordinate(center).
    * @param dy Draw y coordinate(center).
    * @param rad Rotation radian.
    * */
   public boolean drawTextureRotationC( int rnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float rad )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     AMatrix.identityMatrix( mat );
     AMatrix.translateM( mat, 0, dx, dy, 0.0f );
     AMatrix.rotateM( mat, 0, rad * 180.0f / (float)Math.PI, 0.0f, 0.0f, 1.0f );
 
     setFloatArray4( - w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 0 ] = farr4[ 0 ]; farr8[ 1 ] = farr4[ 1 ];
 
     setFloatArray4( w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 2 ] = farr4[ 0 ]; farr8[ 3 ] = farr4[ 1 ];
 
     setFloatArray4( - w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 4 ] = farr4[ 0 ]; farr8[ 5 ] = farr4[ 1 ];
 
     setFloatArray4( w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 6 ] = farr4[ 0 ]; farr8[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     return drawTexture( ttex );
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx Draw x coordinate(center).
    * @param dy Draw y coordinate(center).
    * @param angle Rotation angle.
    */
   public boolean drawTextureRotationAngleC( int rnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float angle )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     AMatrix.identityMatrix( mat );
     AMatrix.translateM( mat, 0, dx, dy, 0.0f );
     AMatrix.rotateM( mat, 0, angle, 0.0f, 0.0f, 1.0f );
 
     setFloatArray4( - w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 0 ] = farr4[ 0 ]; farr8[ 1 ] = farr4[ 1 ];
 
     setFloatArray4( w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 2 ] = farr4[ 0 ]; farr8[ 3 ] = farr4[ 1 ];
 
     setFloatArray4( - w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 4 ] = farr4[ 0 ]; farr8[ 5 ] = farr4[ 1 ];
 
     setFloatArray4( w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 6 ] = farr4[ 0 ]; farr8[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx    / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx Draw x coordinate(center).
    * @param dy Draw y coordinate(center).
    * @param scale Draw scale.
    * @param rad Rotation radian.
    * */
   public boolean drawTextureScaleRotateC( int rnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float scale, float rad )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     AMatrix.identityMatrix( mat );
     AMatrix.translateM( mat, 0, dx, dy, 0.0f );
     AMatrix.scaleM( mat, 0, scale, scale, 1.0f );
     AMatrix.rotateM( mat, 0, rad * 180.0f / (float)Math.PI, 0.0f, 0.0f, 1.0f );
 
     setFloatArray4( - w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 0 ] = farr4[ 0 ]; farr8[ 1 ] = farr4[ 1 ];
 
     setFloatArray4( w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 2 ] = farr4[ 0 ]; farr8[ 3 ] = farr4[ 1 ];
 
     setFloatArray4( - w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 4 ] = farr4[ 0 ]; farr8[ 5 ] = farr4[ 1 ];
 
     setFloatArray4( w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 6 ] = farr4[ 0 ]; farr8[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx Draw x coordinate(center).
    * @param dy Draw y coordinate(center).
    * @param scale Draw scale.
    * @param angle Rotation angle.
    * */
   public boolean drawTextureScaleRotateAngleC( int rnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float scale, float angle )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     AMatrix.identityMatrix( mat );
     AMatrix.translateM( mat, 0, dx, dy, 0.0f );
     AMatrix.scaleM( mat, 0, scale, scale, 1.0f );
     AMatrix.rotateM( mat, 0, angle, 0.0f, 0.0f, 1.0f );
 
     setFloatArray4( - w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 0 ] = farr4[ 0 ]; farr8[ 1 ] = farr4[ 1 ];
 
     setFloatArray4( w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 2 ] = farr4[ 0 ]; farr8[ 3 ] = farr4[ 1 ];
 
     setFloatArray4( - w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 4 ] = farr4[ 0 ]; farr8[ 5 ] = farr4[ 1 ];
 
     setFloatArray4( w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr8[ 6 ] = farr4[ 0 ]; farr8[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param matrix44 4 * 4 draw matrix.
    * */
   public boolean drawTextureMatrix( int rnum,
     float rx, float ry, float w, float h,
     float[] matrix44 )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     setFloatArray4( 0.0f, 0.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr8[ 0 ] = farr4[ 0 ]; farr8[ 1 ] = farr4[ 1 ];
 
     setFloatArray4( w, 0.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr8[ 2 ] = farr4[ 0 ]; farr8[ 3 ] = farr4[ 1 ];
 
     setFloatArray4( 0.0f, h, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr8[ 4 ] = farr4[ 0 ]; farr8[ 5 ] = farr4[ 1 ];
 
     setFloatArray4( w, h, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr8[ 6 ] = farr4[ 0 ]; farr8[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx Draw x coordinate(center).
    * @param dy Draw y coordinate(center).
    * @param matrix44 4 * 4 draw matrix.
    * */
   public boolean drawTextureMatrixC( int rnum,
     float rx, float ry, float w, float h,
     float[] matrix44 )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     setFloatArray4( - w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr8[ 0 ] = farr4[ 0 ]; farr8[ 1 ] = farr4[ 1 ];
 
     setFloatArray4( w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr8[ 2 ] = farr4[ 0 ]; farr8[ 3 ] = farr4[ 1 ];
 
     setFloatArray4( - w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr8[ 4 ] = farr4[ 0 ]; farr8[ 5 ] = farr4[ 1 ];
 
     setFloatArray4( w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr8[ 6 ] = farr4[ 0 ]; farr8[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }
 
   /**
    * @param rnum Resource or Texture number.
    * @param rx Read x coordinate.
    * @param ry Read y coordinate.
    * @param w Width.
    * @param h Height.
    * @param dx0 Draw x coordinate(left up).
    * @param dy0 Draw y coordinate(left up).
    * @param dx1 Draw x coordinate(right up).
    * @param dy1 Draw y coordinate(right up).
    * @param dx2 Draw x coordinate(left down).
    * @param dy2 Draw y coordinate(left down).
    * @param dx3 Draw x coordinate.
    * @param dy3 Draw y coordinate.
    * */
   public boolean drawTextureVertex( int rnum,
     float rx, float ry, float w, float h,
     float dx0, float dy0,
     float dx1, float dy1,
     float dx2, float dy2,
     float dx3, float dy3 )
   {
     if ( existTexture( rnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( rnum );
 
     setFloatArray8( dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3 );
     setVertex( ttex, farr8 );
 
     setFloatArray8(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr8 );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }
 
   public boolean createFont( int fnum, int size ){ return createFont( fnum, size, false, GameColor.WHITE ); }
 
   public boolean createFont( int fnum, int size, boolean antialias ){ return createFont( fnum, size, antialias, GameColor.WHITE ); }
 
   public boolean createFont( int fnum, int size, boolean antialias, GameColor color ){ return createFont( fnum, size, antialias, color.color ); }
 
   public boolean createFont( int fnum, int size, boolean antialias, float[] color )
   {
     if ( paints.containsKey( fnum ) == false )
     {
       tpaint = new Paint();
       paints.put( fnum, tpaint );
     } else
     {
       tpaint = paints.get( fnum );
     }
 
     tpaint.setTextSize( size );
     tpaint.setARGB( (int)(0xff * color[ 3 ]), (int)(0xff * color[ 0 ]), (int)(0xff * color[ 1 ]), (int)(0xff * color[ 2 ]) );
     tpaint.setAntiAlias( antialias );
 
     return true;
   }
 
   public boolean printf( int fnum, float dx, float dy, String str )
   {
     Canvas canvas = new Canvas( stringbmp );
 
     canvas.drawColor( Color.TRANSPARENT, PorterDuff.Mode.CLEAR );
 
     if ( paints.containsKey( fnum ) )
     {
       tpaint = paints.get( fnum );
     } else
     {
       tpaint = paints.get( 0 );
     }
 
     canvas.drawText( str, 0, tpaint.getTextSize(), tpaint );
 
     //gl.glEnable( GL10.GL_TEXTURE_2D );//
     gl.glBindTexture(GL10.GL_TEXTURE_2D, stringnum );
     //GLUtils.texSubImage2D( GL10.GL_TEXTURE_2D, 0, 0, 0, stringbmp );//
     //gl.glDisable( GL10.GL_TEXTURE_2D );//
     GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, stringbmp, 0 );
     //gl.glGenTextures( 1, ttex.texid, 0 );
 
     //gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR );
     //gl.glBindTexture( GL10.GL_TEXTURE_2D, 0 );
 
     setFloatArray8( dx, dy, dx + stringtex.width, dy, dx, dy + stringtex.height, dx + stringtex.width, dy + stringtex.height );
     stringtex.ver = createFloatBuffer( farr8 );
 
     setFloatArray8( 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f );
     stringtex.uv   = createFloatBuffer( farr8 );
 
     return drawTexture( stringtex );
   }
 
   private final void setFloatArray4( float f0, float f1, float f2, float f3 )
   {
     farr4[ 0 ] = f0; farr4[ 1 ] = f1;
     farr4[ 2 ] = f2; farr4[ 3 ] = f3;
   }
 
   private final void setFloatArray8( float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7 )
   {
     farr8[ 0 ] = f0; farr8[ 1 ] = f1;
     farr8[ 2 ] = f2; farr8[ 3 ] = f3;
     farr8[ 4 ] = f4; farr8[ 5 ] = f5;
     farr8[ 6 ] = f6; farr8[ 7 ] = f7;
   }
 
   private static final FloatBuffer createColor( float[] color )
   {
     if ( color.length >= 16 )
     {
       float[] col =
       {
         color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ],
         color[ 4 ], color[ 5 ], color[ 6 ], color[ 7 ],
         color[ 8 ], color[ 9 ], color[ 10 ], color[ 11 ],
         color[ 12 ], color[ 13 ], color[ 14 ], color[ 15 ],
       };
       return createFloatBuffer( col );
     } else if ( color.length >= 4 )
     {
       float[] col =
       {
         color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ],
         color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ],
         color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ],
         color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ],
       };
       return createFloatBuffer( col );
     } else if ( color.length > 0 )
     {
       float[] col =
       {
         color[ 0 ], color[ 0 ], color[ 0 ], color[ 0 ],
         color[ 0 ], color[ 0 ], color[ 0 ], color[ 0 ],
         color[ 0 ], color[ 0 ], color[ 0 ], color[ 0 ],
         color[ 0 ], color[ 0 ], color[ 0 ], color[ 0 ],
       };
       return createFloatBuffer( col );
     }
     float[] col =
     {
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
     };
     return createFloatBuffer( col );
   }
 
   public final void setColor( int rnum, float col )
   {
     float[] color =
     {
       col, col, col, col,
       col, col, col, col,
       col, col, col, col,
       col, col, col, col,
     };
     setColor( rnum, color );
   }
 
   public final void setColor( int rnum )
   {
     float[] color =
     {
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
     };
     setColor( rnum, color );
   }
 
   public final void setColor( int rnum, GameColor color )
   {
     setColor( rnum, color.color );
   }
 
   public final void setColor( int rnum, float[] color )
   {
     Texture tex;
 
     if ( existTexture( rnum ) == false )
     {
       return;
     }
 
     tex = textures.get( rnum );
     tex.col  = createFloatBuffer( color );
   }
 
   public final boolean setUV( Texture tex, float[] uv )
   {
     tex.uv  = createFloatBuffer( uv );
     return true;
   }
 
   public final boolean setVertex( Texture tex, float[] vert )
   {
     tex.ver = createFloatBuffer( vert );
     return true;
   }
 
   public final int getWidth(){ return width; }
   public final int getHeight(){ return height; }
   public boolean setWindowSize( int width, int height )
   {
     this.width = width;
     this.height = height;
     return true;
   }
   /*public final int setWidth( int width )
   {
     int ret = this.width;
     this.width = width;
     return ret;
   }
   public final int setHeight( int height )
   {
     int ret = this.height;
     this.height = height;
     return ret;
   }*/
 
   public final Texture getTexture( int rnum ){ return textures.get( rnum ); }
 
   // Support OpenGL.
   public static final Bitmap resizeBitmap( Bitmap bmp, int length )
   {
     Bitmap nbmp = Bitmap.createBitmap( length, length, Bitmap.Config.ARGB_8888);
 
     Canvas cv = new Canvas( nbmp );
 
     cv.drawBitmap( bmp, 0, 0, null );
 
     return nbmp;
   }
 
   public static final FloatBuffer createFloatBuffer( float[] arr )
   {
     ByteBuffer bb = ByteBuffer.allocateDirect( arr.length * 4 );
     bb.order( ByteOrder.nativeOrder() );
     FloatBuffer fb = bb.asFloatBuffer();
     fb.put( arr );
     fb.position( 0 );
     return fb;
   }
 
   public static final boolean isPowerOf2( int val ){ return val > 0 && ( val & (val - 1) ) == 0; }
 
   public static final int convertPowerOf2( int val )
   {
     if ( val < 0 ){ return 0; }
     int ret = 1;
     for( ; val > ret ; ret <<= 1 );
     return ret;
   }
 
 }
 
 class AMatrix extends Matrix
 {
   static public void identityMatrix( float[] mat )
   {
     mat[ 1 ] = mat[ 2 ] = mat[ 3 ] = mat[ 4 ] = mat[ 6 ] = mat[ 7 ] = mat[ 8 ] = mat[ 9 ] = mat[ 11 ] = mat[ 12 ] = mat[ 13 ] = mat[ 14 ] = 0.0f;
     mat[ 0 ] = mat[ 5 ] = mat[ 10 ] = mat[ 15 ] = 1.0f;
   }
 }
