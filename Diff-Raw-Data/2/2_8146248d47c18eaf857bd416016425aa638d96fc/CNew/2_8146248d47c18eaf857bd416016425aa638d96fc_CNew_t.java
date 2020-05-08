 package tw.cycuice.drawwithme.ui;
 
 import tw.cycuice.drawwithme.CConstant;
 import tw.cycuice.drawwithme.DrawSurface;
 import tw.cycuice.drawwithme.Main;
 import tw.cycuice.drawwithme.R;
 import tw.cycuice.drawwithme.widget.CSelectColor;
 import tw.cycuice.drawwithme.widget.CSelectSize;
 import tw.kin.android.KinView;
 import tw.kin.android.widget.KinButton;
 import tw.kin.android.widget.KinImage;
 import tw.kin.android.widget.KinSeekBar;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 public class CNew extends KinView implements IUI {
   KinImage mBackground;
   KinButton mBOK;
   KinImage mOK;
   KinButton mBReset;
   KinImage mReset;
   KinButton mBSelectColor;
   KinImage mDefaultSize;
   KinSeekBar mSizeBarX;
   KinSeekBar mSizeBarY;
   CSelectSize mUISelectSize;
   CSelectColor mUISelectColor;
   Paint mTextPaint;
 
   public CNew() {
   }
 
   public void LoadContent() {
     mBackground = new KinImage();
     mBackground.AddImage( R.drawable.menu_bg, -1 );
     mBackground.SetAlignment( Alignment.FILL, Alignment.FILL );
     mOK = new KinImage();
     mOK.AddImage( R.drawable.new_ok, -1 );
     mBOK = new KinButton( mOK );
     mBOK.SetAlignment( Alignment.RIGHT, Alignment.BOTTOM );
     mBOK.SetOnClickRun( new Runnable() {
       @Override
       public void run() {
         if ( mSizeBarX.GetSeekValue() < 1 || mSizeBarY.GetSeekValue() < 1 ) {
           Toast.makeText( Main.sInstance, "Create Fail!!", Toast.LENGTH_SHORT ).show();
           return;
         }
         DrawSurface.GetInstance().SetPage( CConstant.PAGECANVAS );
       }
     } );
     mDefaultSize = new KinImage();
     mDefaultSize.AddImage( R.drawable.new_select_bg, -1 );
     mBSelectColor = new KinButton( mDefaultSize );
     mBSelectColor.SetOnClickRun( new Runnable() {
       @Override
       public void run() {
         mUISelectColor.Show();
       }
     } );
     mReset = new KinImage();
     mReset.AddImage( R.drawable.new_reset, -1 );
     mBReset = new KinButton( mReset );
     mBReset.SetAlignment( Alignment.LEFT, Alignment.BOTTOM );
     mBReset.SetOnClickRun( new Runnable() {
       @Override
       public void run() {
         mSizeBarX.SetSeekValue( GetWidth() );
         mSizeBarY.SetSeekValue( GetHeight() );
         mUISelectColor.SetColor( Color.WHITE );
       }
     } );
     mSizeBarX = new KinSeekBar();
     mSizeBarX.SetMinValue( 0 );
     mSizeBarX.SetMaxValue( CConstant.MaxWidth );
     mSizeBarY = new KinSeekBar();
     mSizeBarY.SetVertical( true );
     mSizeBarY.SetReverse( true );
     mSizeBarY.SetMinValue( 0 );
     mSizeBarY.SetMaxValue( CConstant.MaxHeight );
     mUISelectSize = new CSelectSize();
     mUISelectColor = new CSelectColor();
     mTextPaint = new Paint();
     mTextPaint.setStyle( Style.FILL_AND_STROKE );
     mTextPaint.setTextSize( 20 );
     mTextPaint.setARGB( 0xff, 220, 220, 255 );
 
     AddChild( mBackground );
     AddChild( mSizeBarX );
     AddChild( mSizeBarY );
     AddChild( mBOK );
     AddChild( mBReset );
     AddChild( mBSelectColor );
     AddChild( mUISelectSize );
     AddChild( mUISelectColor );
 
   }
 
   @Override
   public void Draw( Canvas canvas ) {
     int height = mSizeBarY.GetSeekValue();
     int width = mSizeBarX.GetSeekValue();
 
     mUISelectSize.SetSeekValueX( mSizeBarX.GetSeekValue() );
     mUISelectSize.SetSeekValueY( mSizeBarY.GetSeekValue() );
     mUISelectSize.SetColor( mUISelectColor.GetColor() );
     super.Draw( canvas );
    canvas.drawText( height + " x " + width, mUISelectSize.GetX(), mUISelectSize.GetY() - 3, mTextPaint );
   }
 
   @Override
   public void CompatibleWith( double windowWidth, double windowHeight ) {
     SetPos( 0, 0, (int) windowWidth, (int) windowHeight );
     int bWidth = (int) ( windowWidth * 0.5 );
     int bHeight = (int) ( bWidth / 200.0 * 100.0 );
     mBOK.SetSize( bWidth, bHeight );
     mBReset.SetSize( bWidth, bHeight );
     mBSelectColor.SetPos( (int) ( windowWidth * 0.05 ), (int) ( windowWidth * 0.825 ), (int) ( windowWidth * 0.175 ), (int) ( windowWidth * 0.95 ) );
     mUISelectSize.SetPos( (int) ( windowWidth * 0.175 ), (int) ( windowWidth * 0.05 ), (int) ( windowWidth * 0.95 ), (int) ( windowWidth * 0.825 ) );
     mSizeBarX.SetSeekValue( GetWidth() );
     mSizeBarY.SetSeekValue( GetHeight() );
     mSizeBarX.SetPos( mUISelectSize.GetX(), (int) ( windowWidth * 0.825 ), mUISelectSize.GetX() + mUISelectSize.GetWidth(),
         (int) ( windowWidth * 0.95 ) );
     mSizeBarY.SetPos( (int) ( windowWidth * 0.05 ), mUISelectSize.GetY(), (int) ( windowWidth * 0.175 ),
         mUISelectSize.GetY() + mUISelectSize.GetHeight() );
 
     mUISelectColor.CompatibleWith( windowWidth, windowHeight );
   }
 
   @Override
   public void onStart( IUI from ) {
     mUISelectColor.SetColor( Color.WHITE );
     mUISelectColor.Hide();
     mHasUpdate = true;
 
   }
 
   @Override
   public void onQuit( IUI to ) {
     if ( to instanceof CDrawBoard ) {
       CDrawBoard canvas = (CDrawBoard) to;
       int width = mSizeBarX.GetSeekValue();
       int height = mSizeBarY.GetSeekValue();
       int color = mUISelectColor.GetColor();
       canvas.mUICanvas.New( width, height, color );
     }
 
   }
 
   @Override
   public boolean onKeyDown( int keycode, KeyEvent event ) {
     if ( keycode == KeyEvent.KEYCODE_BACK ) {
       DrawSurface.GetInstance().SetPage( CConstant.PAGEMENU );
       return true;
     }
     return false;
   }
 }
