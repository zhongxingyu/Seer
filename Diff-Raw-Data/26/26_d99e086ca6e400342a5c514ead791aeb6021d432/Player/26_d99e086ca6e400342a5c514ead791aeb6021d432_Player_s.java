 package org.zapto.boelsen.hang.gliding.hoops;
 
 import rajawali.BaseObject3D;
 import rajawali.lights.DirectionalLight;
 import rajawali.materials.DiffuseMaterial;
 import rajawali.materials.TextureManager;
 import rajawali.math.MathUtil;
 import rajawali.math.Quaternion;
 import rajawali.parser.ObjParser;
 import android.content.Context;
 import android.graphics.BitmapFactory;
 import android.util.FloatMath;
 
 public final class Player
 {
     private BaseObject3D model__;
     
     private AveragedValue rotation__;
     private AveragedValue pitch__;
     
     private float speed__   = 2.0f;
     private float heading__ = 0.0f;
     
     public  int   score__   = 0;
 
     public Player( Context          _context
                  , TextureManager   _tex_manager
                  , DirectionalLight _light)
     {
         rotation__ = new AveragedValue( 10 );
         pitch__ = new AveragedValue( 10 );
 
         try
         {
             ObjParser objParser = new ObjParser(_context.getResources(), _tex_manager, R.raw.glider);
             objParser.parse();
             
             model__ = objParser.getParsedObject();
             model__.setMaterial( new DiffuseMaterial() );
             model__.addLight( _light );
             model__.addTexture( _tex_manager.addTexture( BitmapFactory.decodeResource( _context.getResources(), R.drawable.glider_mtl ) ) );
             //mRaptor.setScale( 0.5f );
         }
         catch( Exception e )
         {
             e.printStackTrace();
         }
     }
 
     public void update( float _rotate
                       , float _pitch )
     {
         rotation__.add( _rotate * -5.5f );
         pitch__.add( _pitch * 5.5f + 22.5f );
         float rot = rotation__.getAverage();
         float pit = pitch__.getAverage();
         
         heading__ += rot / 20.0f;
         float head_rad = MathUtil.degreesToRadians( heading__ );
         float head_sin = FloatMath.sin( head_rad );
         float head_cos = FloatMath.cos( head_rad );
         float amount_rot = rot * head_cos + pit * head_sin;
         float amount_pit = pit * head_cos - rot * head_sin;
        model__.setOrientation( q_from_euler( heading__, amount_rot, amount_pit ) );
        //Quaternion quat = new Quaternion();
        //quat.fromEuler( heading__, amount_rot, amount_pit );
        //model__.setOrientation( quat );
         
         // @todo This function should not be linear - fix it! 
         speed__ += pit / 1500.0f;
         if( speed__ < 0.0f )
             speed__ = 0.0f;
 
         model__.setX( model__.getX() - speed__ * head_sin );
         model__.setY( model__.getY() - ( pit / 200.0f * speed__ ) );
         model__.setZ( model__.getZ() - speed__ * head_cos );
     }
     
     public BaseObject3D getObject()
     {
         return model__;
     }
    
    public Quaternion q_from_euler(final float heading, final float attitude, final float bank)
    {
        float x = MathUtil.degreesToRadians(heading);
        float y = MathUtil.degreesToRadians(attitude);
        float z = MathUtil.degreesToRadians(bank);
        float c1 = FloatMath.cos(x/2);
        float s1 = FloatMath.sin(x/2);
        float c2 = FloatMath.cos(y/2);
        float s2 = FloatMath.sin(y/2);
        float c3 = FloatMath.cos(z/2);
        float s3 = FloatMath.sin(z/2);
        float c1c2 = c1*c2;
        float s1s2 = s1*s2;
        float w1 =c1c2*c3 - s1s2*s3;
        float x1 =c1c2*s3 + s1s2*c3;
        float y1 =s1*c2*c3 + c1*s2*s3;
        float z1 =c1*s2*c3 - s1*c2*s3;
        return new Quaternion( w1, x1, y1, z1 );
    }
 }
