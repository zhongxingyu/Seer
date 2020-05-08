 package com.musicgame.PumpAndJump;
 
 import com.musicgame.PumpAndJump.Animation.Animated;
 import com.musicgame.PumpAndJump.Animation.Animation;
 import com.musicgame.PumpAndJump.Animation.AnimationQueue;
 import com.musicgame.PumpAndJump.Animation.PlayerAnimationFSM;
 import com.musicgame.PumpAndJump.Util.AnimationUtil.Point;
 import com.musicgame.PumpAndJump.Util.TextureMapping;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Polygon;
 
 public class Player extends GameObject implements Animated{
 
 	public PlayerHip hip;
 	Point pos;
 	Matrix4 before;
 	public boolean changed;
 	public Float[] pose;
 	public float origY;
 	float time = 0;
 	float done = 0;
 	int count = 0;
 	public final int WAMS_PLAYER_DOF = 16;
 	Animation ani[];
 	AnimationQueue aniQ;
 	PlayerAnimationFSM aniFSM;
 
 	public Player( Point pos, Point angle )
 	{
 		hip = new PlayerHip( new Point( 0.0f, 0.0f, 0.0f ), angle );
 		this.pos = pos;
 		origY = pos.y;
 
 		hip.scale( 1.0f, 1.0f, 1.0f );
 
 		changed = true;
 
 		pose = new Float[ WAMS_PLAYER_DOF ];
 
 		float[] fpose = new float[ pose.length ];
 		getPose( fpose );
 
 		//ani = new Animation[2];
 		//ani[ 0 ] = new Animation( "HandsTrans.txt" );
 		//ani[ 1 ] = new Animation( "HandsAnim.txt" );
 		//ani[1].isLooping=true;
 
 		done = (float) Math.random()*5.0f + 1.0f;
 		
 		aniFSM = new PlayerAnimationFSM( "playerStates.txt", "playerFSM.txt", "r" );
 		
 		aniQ = new AnimationQueue( aniFSM.getAni(), fpose );
 	}
 
 	//This method has to exist because java is bad
 	void actuallySetPose( Float[] a )
 	{
 		hip.angle.z = pose[ 0 ];
 
 		hip.leftThigh.angle.z = pose[ 1 ];
 		hip.leftThigh.shin.angle.z = pose[ 2 ];
 		hip.leftThigh.shin.foot.angle.z = pose[ 3 ];
 		hip.leftThigh.shin.foot.tuckles.angle.z = pose[ 4 ];
 
 		hip.rightThigh.angle.z = pose[ 5 ] ;
 		hip.rightThigh.shin.angle.z = pose[ 6 ];
 		hip.rightThigh.shin.foot.angle.z = pose[ 7 ];
 		hip.rightThigh.shin.foot.tuckles.angle.z = pose[ 8 ];
 
 		hip.torso.angle.z = pose[ 9 ];
 
 		hip.torso.leftArm.angle.z = pose[ 10 ];
 		hip.torso.leftArm.forearm.angle.z = pose[ 11 ] ;
 
 		hip.torso.rightArm.angle.z = pose[ 12 ];
 		hip.torso.rightArm.forearm.angle.z = pose[ 13 ];
 
 		hip.torso.head.angle.z = pose[ 14 ];
 
 		hip.p.y = pose[ 15 ];
 	}
 
 	//Same
 	void actuallyGetPose()
 	{
 		pose[ 0 ] = Float.valueOf( hip.angle.z );
 
 		pose[ 1 ] = Float.valueOf( hip.leftThigh.angle.z );
 		pose[ 2 ] = Float.valueOf( hip.leftThigh.shin.angle.z );
 		pose[ 3 ] = Float.valueOf( hip.leftThigh.shin.foot.angle.z );
 		pose[ 4 ] = Float.valueOf( hip.leftThigh.shin.foot.tuckles.angle.z );
 
 		pose[ 5 ] = Float.valueOf( hip.rightThigh.angle.z );
 		pose[ 6 ] = Float.valueOf( hip.rightThigh.shin.angle.z );
 		pose[ 7 ] = Float.valueOf( hip.rightThigh.shin.foot.angle.z );
 		pose[ 8 ] = Float.valueOf( hip.rightThigh.shin.foot.tuckles.angle.z );
 
 		pose[ 9 ] = Float.valueOf( hip.torso.angle.z );
 
 		pose[ 10 ] = Float.valueOf( hip.torso.leftArm.angle.z );
 		pose[ 11 ] = Float.valueOf( hip.torso.leftArm.forearm.angle.z );
 
 		pose[ 12 ] = Float.valueOf( hip.torso.rightArm.angle.z );
 		pose[ 13 ] = Float.valueOf( hip.torso.rightArm.forearm.angle.z );
 
 		pose[ 14 ] = Float.valueOf( hip.torso.head.angle.z );
 
 		pose[ 15 ] = Float.valueOf( hip.p.y );
 	}
 
 	void setPose( float[] a )
 	{
 		for( int i = 0; i < WAMS_PLAYER_DOF; i++ )
 		{
 			pose[ i ] = a[i];//temp;
 			//System.out.print( pose[i] );
 		}
 		actuallySetPose( pose );
 		changed = true;
 	}
 
 	void setPose( double[] a )
 	{
 		for( int i = 0; i < WAMS_PLAYER_DOF; i++ )
 		{
 			pose[ i ] = (float)a[i];
 		}
 		actuallySetPose( pose );
 		changed = true;
 	}
 
 	public void getPose( float[] a )
 	{
 		actuallyGetPose();
 		for( int i = 0; i < WAMS_PLAYER_DOF; i++ )
 		{
 			a[ i ] = pose[ i ];
 		}
 	}
 
 	public void getPose( double[] a )
 	{
 		actuallyGetPose();
 		for( int i = 0; i < WAMS_PLAYER_DOF; i++ )
 		{
 			a[ i ] = ( double ) ( pose[ i ] );
 		}
 	}
 
 	public void display( SpriteBatch sb )
 	{
 		Matrix4 mv = sb.getTransformMatrix();
 		before = new Matrix4( mv );
 
 		mv.translate( pos.x, pos.y, pos.z );
 		sb.setTransformMatrix( mv );
 
 		hip.display( sb );
 
 		sb.setTransformMatrix( before );
 	}
 
 	@Override
	public synchronized void draw( SpriteBatch sb )
 	{
 		display( sb );
 	}
 
 	@Override
	public synchronized void update( float delta )
 	{
 		time += delta;
 		if( aniQ.stop )
 		{
 			float[] f = new float[ pose.length ];
 			getPose( f );
 			aniQ.switchAnimation( aniFSM.getAni(), f );
 		}
		UpdatePose( aniQ.getPose( delta ) );
 	}
 
 	@Override
 	public void UpdatePose(float[] pose) {
 		setPose( pose );
 		Matrix4 m = new Matrix4();
 		m = m.translate( pos.x,  pos.y, pos.z );
 		hip.update( m );
 	}
 
 	@Override
	public synchronized boolean intersects( Polygon p )
 	{
 		return hip.intersects( p );
 	}
 
 	public void jump()
 	{
 		float[] f = new float[ pose.length ];
 		getPose( f ); 
 		Animation ta = aniFSM.startJump();
 		if( ta != null )
 			aniQ.switchAnimation( ta, f );
 	}
 
 	public void duck()
 	{
 		float[] f = new float[ pose.length ];
 		getPose( f );
 		Animation ta = aniFSM.startDuck();
 		if( ta != null )
 		aniQ.switchAnimation( ta, f );
 	}
 }
 
 enum Side{ LEFT, RIGHT };
 
 class PlayerForearm extends Model
 {
 	public final float width = 20.0f;
 	public final float height = 4.0f;
 
 	public PlayerForearm( Side a )
 	{
 		super(  new Point( 20.0f, 0.0f, 0.0f ), new Point( 0.0f, 0.0f, 0.0f ), new Point( 1.0f, 1.0f, 1.0f ) );
 
 		image = new Sprite( TextureMapping.staticGet( "BlackTemp.png" ) );
 		image.setBounds( 0.0f, 0.0f, width, height );
 		image.setPosition( 0.0f, -height/2.0f );
 
 		float[] points = new float[8];
 		points[ 0 ] =  0.0f; points[1] = -height/2.0f;
 		points[ 2 ] = 0.0f; points[3] =  height/2.0f;
 		points[ 4 ] = width; points[5] = height/2.0f;
 		points[ 6 ] = width; points[7] = -height/2.0f;
 
 		poly = new Polygon( points );
 
 		switch( a )
 		{
 			case LEFT:
 				angle.z = 90.0f; break;
 			case RIGHT:
 				angle.z = 60.0f; break;
 		}
 	}
 
 	public void display( SpriteBatch sb )
 	{
 		pushTransforms( sb );
 
 		/*glBegin(GL_POLYGON);
 			glVertex3f( 0.0f, -2.0f, 0.0f);
 			glVertex3f(	0.0f, 2.0f, 0.0f);
 			glVertex3f( 20.0f, 2.0f,  0.0f);
 			glVertex3f( 20.0f, -2.0f, 0.0f);
 		glEnd();*/
 		drawSprite( sb );
 
 		popTransforms( sb );
 	}
 
 }
 
 class PlayerShoulder extends Model
 {
 	public PlayerForearm forearm;
 	public Side side;
 	public final float width = 20.0f;
 	public final float height = 4.0f;
 
 	PlayerShoulder( Side a )
 	{
 		super( new Point( 35.0f, 0.0f, 0.0f ), new Point( 0.0f, 0.0f, 0.0f ), new Point( 1.0f, 1.0f, 1.0f ) );
 		side = a;
 
 		image = new Sprite( TextureMapping.staticGet( "BlackTemp.png" ) );
 		image.setBounds( 0.0f, 0.0f, width, height );
 		image.setPosition( 0.0f, -height/2.0f );
 
 		float[] points = new float[8];
 		points[ 0 ] =  0.0f; points[1] = -height/2.0f;
 		points[ 2 ] = 0.0f; points[3] =  height/2.0f;
 		points[ 4 ] = width; points[5] = height/2.0f;
 		points[ 6 ] = width; points[7] = -height/2.0f;
 
 		poly = new Polygon( points );
 
 		forearm = new PlayerForearm( a );
 
 		children.add( forearm );
 
 		switch( a )
 		{
 			case LEFT:
 				angle.z = 135.0f; break;
 			case RIGHT:
 				angle.z = -135.0f; break;
 		}
 	}
 
 	public void display( SpriteBatch sb )
 	{
 		pushTransforms( sb );
 
 		forearm.display( sb );
 
 		/*glBegin(GL_POLYGON);
 			glVertex3f( 0.0f, -2.0f, 0.0f);
 			glVertex3f(	0.0f, 2.0f, 0.0f);
 			glVertex3f( 20.0f, 2.0f,  0.0f);
 			glVertex3f( 20.0f, -2.0f, 0.0f);
 		glEnd();*/
 		drawSprite( sb );
 
 		popTransforms( sb );
 	}
 }
 
 class PlayerHead extends Model
 {
 	public final float width = 25.0f;
 	public final float height = 25.0f;
 
 	public PlayerHead()
 	{
 		super( new Point( 35.0f, 0.0f, 0.0f ), new Point( 0.0f, 0.0f, 0.0f ), new Point( 1.0f, 1.0f, 1.0f ) );
 
 		image = new Sprite( TextureMapping.staticGet( "BlackTemp.png" ) );
 		image.setBounds( 0.0f, 0.0f, width, height );
 		image.setPosition( 0.0f, -height/2.0f );
 
 		float[] points = new float[8];
 		points[ 0 ] =  0.0f; points[1] = -height/2.0f;
 		points[ 2 ] = 0.0f; points[3] =  height/2.0f;
 		points[ 4 ] = width; points[5] = height/2.0f;
 		points[ 6 ] = width; points[7] = -height/2.0f;
 
 		poly = new Polygon( points );
 	}
 
 	public void display( SpriteBatch sb )
 	{
 		pushTransforms( sb );
 
 		/*glBegin(GL_POLYGON);
 			glVertex3f( 0.0f, -2.0f, 0.0f);
 			glVertex3f(	0.0f, 2.0f, 0.0f);
 			glVertex3f( 15.0f, 2.0f,  0.0f);
 			glVertex3f( 15.0f, -2.0f, 0.0f);
 		glEnd();*/
 		drawSprite( sb );
 
 		//DrawCircle( 15.0f, 0.0f, 10.0f, 100 );
 
 		popTransforms( sb );
 	}
 
 };
 
 class PlayerTorso extends Model
 {
 	public PlayerShoulder leftArm;
 	public PlayerShoulder rightArm;
 	public PlayerHead head;
 	public final float width = 35.0f;
 	public final float height = 4.0f;
 
 	public PlayerTorso()
 	{
 		super( new Point( 0.0f, 0.0f, 0.0f ), new Point( 0.0f, 0.0f, 90.0f ),new Point( 1.0f, 1.0f, 1.0f ) );
 
 		image = new Sprite( TextureMapping.staticGet( "BlackTemp.png" ) );
 		image.setBounds( 0.0f, 0.0f, width, height );
 		image.setPosition( 0.0f, -height/2.0f );
 
 		float[] points = new float[8];
 		points[ 0 ] =  0.0f; points[1] = -height/2.0f;
 		points[ 2 ] = 0.0f; points[3] =  height/2.0f;
 		points[ 4 ] = width; points[5] = height/2.0f;
 		points[ 6 ] = width; points[7] = -height/2.0f;
 
 		poly = new Polygon( points );
 
 		leftArm = new PlayerShoulder( Side.LEFT );
 		rightArm = new PlayerShoulder( Side.RIGHT );
 		head = new PlayerHead();
 
 		children.add( leftArm );
 		children.add( rightArm );
 		children.add( head );
 	}
 
 	public void display( SpriteBatch sb )
 	{
 		pushTransforms( sb );
 
 		leftArm.display( sb );
 
 		head.display( sb );
 
 		/*glBegin(GL_POLYGON);
 			glVertex3f( 0.0f, -2.0f, 0.0f);
 			glVertex3f(	0.0f, 2.0f, 0.0f);
 			glVertex3f( 35.0f, 2.0f,  0.0f);
 			glVertex3f( 35.0f, -2.0f, 0.0f);
 		glEnd();*/
 		drawSprite( sb );
 
 		rightArm.display( sb );
 
 		popTransforms( sb );
 	}
 }
 
 class PlayerTuckles extends Model
 {
 	public final float width = 3.0f;
 	public final float height = 4.0f;
 
 	public PlayerTuckles( Side a )
 	{
 		super( new Point( 9.0f, 0.0f, 0.0f ), new Point( 0.0f, 0.0f, 0.0f ), new Point( 1.0f, 1.0f, 1.0f ) );
 
 		image = new Sprite( TextureMapping.staticGet( "BlackTemp.png" ) );
 		image.setBounds( 0.0f, 0.0f, width, height );
 		image.setPosition( 0.0f, -height/2.0f );
 
 		float[] points = new float[6];
 		points[ 0 ] =  0.0f; points[1] = -height/2.0f;
 		points[ 2 ] = 0.0f; points[3] =  height/2.0f;
 		points[ 4 ] = width; points[5] = -height/2.0f;
 
 		poly = new Polygon( points );
 	}
 
 	public void display( SpriteBatch sb )
 	{
 		pushTransforms( sb );
 
 		/*glBegin(GL_POLYGON);
 			glVertex3f( 0.0f, -2.0f, 0.0f);
 			glVertex3f(	0.0f, 2.0f, 0.0f);
 			glVertex3f( 3.0f, -2.0f, 0.0f);
 		glEnd();*/
 		drawSprite( sb );
 
 		popTransforms( sb );
 	}
 };
 
 class PlayerFoot extends Model
 {
 	public PlayerTuckles tuckles;
 	public final float width = 9.0f;
 	public final float height = 4.0f;
 
 	public PlayerFoot( Side a )
 	{
 		super( new Point( 20.0f, 0.0f, 0.0f ), new Point( 0.0f, 0.0f, 0.0f ), new Point( 1.0f, 1.0f ,1.0f ) );
 
 		image = new Sprite( TextureMapping.staticGet( "BlackTemp.png" ) );
 		image.setBounds( 0.0f, 0.0f, width, height );
 		image.setPosition( 0.0f, -height/2.0f );
 
 		float[] points = new float[8];
 		points[ 0 ] =  0.0f; points[1] = -height/2.0f;
 		points[ 2 ] = 0.0f; points[3] =  height/2.0f;
 		points[ 4 ] = width; points[5] = height/2.0f;
 		points[ 6 ] = width; points[7] = -height/2.0f;
 
 		poly = new Polygon( points );
 
 		tuckles = new PlayerTuckles( a );
 
 		children.add( tuckles );
 
 		switch( a )
 		{
 			case LEFT:
 				angle.z = 90.0f; break;
 			case RIGHT:
 				angle.z = 90.0f; break;
 		}
 	}
 
 	public void display( SpriteBatch sb )
 	{
 		pushTransforms( sb );
 
 		/*glBegin(GL_POLYGON);
 			glVertex3f( 0.0f, -2.0f, 0.0f);
 			glVertex3f(	0.0f, 2.0f, 0.0f);
 			glVertex3f( 9.0f, 2.0f,  0.0f);
 			glVertex3f( 9.0f, -2.0f, 0.0f);
 		glEnd();*/
 		drawSprite( sb );
 
 		tuckles.display( sb );
 
 		popTransforms( sb );
 	}
 }
 
 class PlayerShin extends Model
 {
 	public PlayerFoot foot;
 	public final float width = 20.0f;
 	public final float height = 4.0f;
 
 	PlayerShin( Side a )
 	{
 		super( new Point( 20.0f, 0.0f, 0.0f ), new Point( 0.0f, 0.0f, 0.0f ), new Point( 1.0f, 1.0f, 1.0f ) );
 
 		image = new Sprite( TextureMapping.staticGet( "BlackTemp.png" ) );
 		image.setBounds( 0.0f, 0.0f, width, height );
 		image.setPosition( 0.0f, -height/2.0f );
 
 		float[] points = new float[8];
 		points[ 0 ] =  0.0f; points[1] = -height/2.0f;
 		points[ 2 ] = 0.0f; points[3] =  height/2.0f;
 		points[ 4 ] = width; points[5] = height/2.0f;
 		points[ 6 ] = width; points[7] = -height/2.0f;
 
 		poly = new Polygon( points );
 
 		foot = new PlayerFoot( a );
 
 		children.add( foot );
 
 		switch( a )
 		{
 			case LEFT:
 				angle.z = -60.0f; break;
 			case RIGHT:
 				angle.z = -45.0f; break;
 		}
 	}
 
 	public void display( SpriteBatch sb )
 	{
 		pushTransforms( sb );
 
 		/*glBegin(GL_POLYGON);
 			glVertex3f( 0.0f, -2.0f, 0.0f);
 			glVertex3f(	0.0f, 2.0f, 0.0f);
 			glVertex3f( 20.0f, 2.0f,  0.0f);
 			glVertex3f( 20.0f, -2.0f, 0.0f);
 		glEnd();*/
 		drawSprite( sb );
 
 		foot.display( sb );
 
 		popTransforms( sb );
 	}
 
 }
 
 class PlayerThigh extends Model
 {
 	public PlayerShin shin;
 	public Side side;
 	public final float width = 20.0f;
 	public final float height = 4.0f;
 
 	public PlayerThigh( Side a )
 	{
 		super( new Point( 0.0f, 0.0f, 0.0f ), new Point( 0.0f, 0.0f, 0.0f ), new Point( 1.0f, 1.0f, 1.0f ) );
 
 		image = new Sprite( TextureMapping.staticGet( "BlackTemp.png" ) );
 		image.setBounds( 0.0f, 0.0f, width, height );
 		image.setPosition( 0.0f, -height/2 );
 
 		float[] points = new float[8];
 		points[ 0 ] =  0.0f; points[1] = -height/2.0f;
 		points[ 2 ] = 0.0f; points[3] =  height/2.0f;
 		points[ 4 ] = width; points[5] = height/2.0f;
 		points[ 6 ] = width; points[7] = -height/2.0f;
 
 		poly = new Polygon( points );
 
 		shin = new PlayerShin( a );
 
 		children.add( shin );
 
 		side = a;
 
 		switch( a )
 		{
 			case LEFT:
 				angle.z = -45.0f; break;
 			case RIGHT:
 				angle.z = -90.0f; break;
 		}
 	}
 
 	public void display( SpriteBatch sb )
 	{
 		pushTransforms( sb );
 
 		/*glBegin(GL_POLYGON);
 			glVertex3f( 0.0f, -2.0f, 0.0f);
 			glVertex3f(	0.0f, 2.0f, 0.0f);
 			glVertex3f( 20.0f, 2.0f,  0.0f);
 			glVertex3f( 20.0f, -2.0f, 0.0f);
 		glEnd();*/
 		drawSprite( sb );
 
 		shin.display( sb );
 
 		popTransforms( sb );
 	}
 
 }
 
 class PlayerHip extends Model
 {
 	public PlayerTorso torso;
 	public PlayerThigh leftThigh;
 	public PlayerThigh rightThigh;
 
 	public PlayerHip( Point pos, Point angle )
 	{
 		super( pos, angle, new Point( 1.0f, 1.0f, 1.0f ) );
 		torso = new PlayerTorso();
 		leftThigh = new PlayerThigh( Side.LEFT );
 		rightThigh = new PlayerThigh( Side.RIGHT );
 
 		children.add( torso );
 		children.add( leftThigh );
 		children.add( rightThigh );
 	}
 
 	public void display( SpriteBatch sb )
 	{
 		pushTransforms( sb );
 
 		leftThigh.display( sb );
 		torso.display( sb );
 		rightThigh.display( sb );
 
 		popTransforms( sb );
 	}
 }
