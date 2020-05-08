 package com.katspow.caatjagwtdemos.client.welcome.hypernumber.core;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.katspow.caatja.behavior.AlphaBehavior;
 import com.katspow.caatja.behavior.BaseBehavior;
 import com.katspow.caatja.behavior.BehaviorListener;
 import com.katspow.caatja.behavior.ContainerBehavior;
 import com.katspow.caatja.behavior.Interpolator;
 import com.katspow.caatja.behavior.PathBehavior;
 import com.katspow.caatja.behavior.RotateBehavior;
 import com.katspow.caatja.behavior.ScaleBehavior;
 import com.katspow.caatja.behavior.SetForTimeReturnValue;
 import com.katspow.caatja.core.canvas.CaatjaGradient;
 import com.katspow.caatja.core.canvas.CaatjaImage;
 import com.katspow.caatja.event.CAATMouseEvent;
 import com.katspow.caatja.foundation.Director;
 import com.katspow.caatja.foundation.Scene;
 import com.katspow.caatja.foundation.actor.Actor;
 import com.katspow.caatja.foundation.actor.ActorContainer;
 import com.katspow.caatja.foundation.actor.Button;
 import com.katspow.caatja.foundation.actor.ImageActor;
 import com.katspow.caatja.foundation.image.CompoundImage;
 import com.katspow.caatja.foundation.timer.Callback;
 import com.katspow.caatja.foundation.timer.TimerTask;
 import com.katspow.caatja.foundation.ui.ShapeActor;
 import com.katspow.caatja.foundation.ui.TextActor;
 import com.katspow.caatja.pathutil.CurvePath;
 import com.katspow.caatja.pathutil.LinearPath;
 import com.katspow.caatja.pathutil.Path;
 import com.katspow.caatjagwtdemos.client.welcome.hypernumber.core.brick.Brick;
 import com.katspow.caatjagwtdemos.client.welcome.hypernumber.core.brick.BrickActor;
 import com.katspow.caatjagwtdemos.client.welcome.hypernumber.core.context.Context;
 import com.katspow.caatjagwtdemos.client.welcome.hypernumber.core.context.ContextListener;
 import com.katspow.caatjagwtdemos.client.welcome.hypernumber.startmenu.AnimatedBackground;
 
 public class GameScene implements ContextListener {
     
     public static final int imageBricksW=   9;
     public static final int imageBricksH=   7;
 
     int gameRows=       15;
     int gameColumns=    20;
     
     int gap = 2;
 
     Context context=        null;
     public Scene directorScene=  null;
 
     SelectionPath selectionPath =  null;
     ActorContainer bricksContainer = null;
     
     //  TODO Check type !!!
     ActorContainer fallingBricksContainer=     null;
     
     BrickActor[][] brickActors;
     CompoundImage bricksImage=    null;
     CompoundImage buttonImage=    null;
     
     TextActor levelActor=                 null;
     Chrono chronoActor=                null;
     TimerTask timer=                      null;
     TimerTask scrollTimer =                null;
 
     ScoreActor scoreActor=                 null;
     ImageActor endGameActor =               null;
 
     Director director=       null;
 
     int actorInitializationCount=   0;  // flag indicating how many actors have finished initializing.
     
     AnimatedBackground backgroundContainer =         null;
     
     /**
      * Creates the main game Scene.
      * @param director a Director instance.
      * @throws Exception 
      */
     public GameScene create (final Director director,int rows,int columns, final Context context) throws Exception {
         this.gameRows= rows;
         this.gameColumns= columns;
 
         this.director= director;
 
         CaatjaImage image = director.getImage("bricks");
         
         this.bricksImage= new CompoundImage().initialize(
                 image,
                 this.imageBricksH,
                 this.imageBricksW );
         
         CaatjaImage image2 = director.getImage("buttons");
         this.buttonImage= new CompoundImage().initialize(
                image2, 7,4 );
 
 //        this.context= new Context().
 //                create( this.gameRows, this.gameColumns, this.imageBricksH ).
 //                addContextListener(this);
         
         context.addContextListener(this);
         this.context = context;
 
 //        this.directorScene= director.createScene();
         this.directorScene = new Scene() {
             @Override
             public void activated() {
                 context.initialize();
             }
         };
         
         director.addScene(directorScene);
         
         int dw= director.canvas.getCoordinateSpaceWidth();
         int dh= director.canvas.getCoordinateSpaceHeight();
         
         ////////////////////////animated background
             this.backgroundContainer= (AnimatedBackground) new AnimatedBackground().
             
             setBounds(0,0,dw,dh).
             setImage(director.getImage("background")).
             setOffsetY( -director.getImage("background").getHeight()+2*dh ).
             setClip(true);
             this.backgroundContainer.setScene(this.directorScene);
             
         this.directorScene.addChild(this.backgroundContainer);
         this.context.addContextListener(this.backgroundContainer);
 
         this.brickActors = new BrickActor[gameRows][gameColumns];
         
         ////////////////////////Number Bricks
         this.bricksContainer= (ActorContainer) new ActorContainer().
         
         setSize(
             this.gameColumns*this.getBrickWidth(),
             this.gameRows*this.getBrickHeight() );
 
         double bricksCY= (dh-this.bricksContainer.height)/2;
         double bricksCX= bricksCY;
         this.bricksContainer.setLocation( bricksCX, bricksCY );
 
         this.directorScene.addChild(this.bricksContainer);
 
         int i,j;
         for( i=0; i<this.gameRows; i++ ) {
 //            this.brickActors.push([]);
             for( j=0; j<this.gameColumns; j++ ) {
                 BrickActor brick= (BrickActor) new BrickActor();
                         brick.initialize( this.bricksImage, this.context.getBrick(i,j) ).
                         setLocation(-100,-100);
 
                 this.brickActors[i][j] = brick;
 
                 bricksContainer.addChild(brick);
             }
         }
         
         /////////////////////// game indicators.
         ActorContainer controls= (ActorContainer) new ActorContainer().
                 
                 setBounds(
                     this.bricksContainer.x + this.bricksContainer.width + bricksCX,
                     this.bricksContainer.x,
                     dw - bricksCX - (this.bricksContainer.x + this.bricksContainer.width) - bricksCX,
                     this.bricksContainer.height );
         this.directorScene.addChild( controls );
         
         /////////////////////// initialize selection path
         this.selectionPath= (SelectionPath) new SelectionPath().
                 
                 setBounds(
                     this.bricksContainer.x,
                     this.bricksContainer.y,
                     this.gameColumns*this.getBrickWidth(),
                     this.gameRows*this.getBrickHeight());
         this.selectionPath.enableEvents(false);
         this.directorScene.addChild(this.selectionPath);
         this.context.addContextListener(this.selectionPath);
 
         /////////////////////// initialize button
         Button restart= (Button) new Button() {
             @Override
             public void mouseClick(CAATMouseEvent mouseEvent) {
                 context.timeUp();
             }
         }.
                 
                 initialize( this.buttonImage, 20,21,22,23 ).
                 setLocation(
                         (controls.width-this.buttonImage.singleWidth)/2,
                         controls.height - this.buttonImage.singleHeight );
                 
        controls.addChild(restart);
         
         ///////////////////// Level indicator
         CaatjaGradient gradient= director.ctx.createLinearGradient(0,0,0,40);
             gradient.addColorStop(0,"#00ff00");
             gradient.addColorStop(0.5,"#ffff00");
             gradient.addColorStop(1,"#3f3f3f");
 
         this.levelActor= new TextActor().
                 
                 setFont("40px sans-serif").
                 setText("");
                 levelActor.setTextFillStyle( gradient ).
                 setOutline(true).
                 calcTextSize(director);
 
         this.levelActor.setLocation((controls.width-this.levelActor.textWidth)/2,5);
         controls.addChild(this.levelActor);
         
         ///////////////////// Guess Number
         GuessNumberActor guess= (GuessNumberActor) new GuessNumberActor().
                 
                 setBounds((controls.width-80)/2, 50, 80, 30 );
                 guess.setFont("80px sans-serif").
                 setText("").
                 setTextFillStyle("#000000").
                 setOutline(true).
                 setOutlineColor("#ffff00");
 
         this.context.addContextListener(guess);
         controls.addChild(guess);
         
         ///////////////////// chronometer
         this.chronoActor= (Chrono) new Chrono().
                 
                 setBounds( 2, 140, controls.width-4, 20 );
         this.context.addContextListener(this.chronoActor);
         controls.addChild(this.chronoActor);
 
         ///////////////////// score
         this.scoreActor= (ScoreActor) new ScoreActor().
                 
                 setBounds( 2, 180, controls.width-4, 30 );
                 this.scoreActor.setFont( "40px sans-serif" ).
                 setTextFillStyle( gradient ).
                 setOutline( true ).
                 setOutlineColor( "0x7f7f00" );
         controls.addChild( this.scoreActor );
         this.context.addContextListener(this.scoreActor);
         
         ////////////////////////////////////////////////
         this.create_EndGame(director);
 
         return this;
     }
     
     private void create_EndGame(final Director director ) throws Exception {
 
         this.endGameActor= (ImageActor) new ImageActor();
                 endGameActor.setImage(director.getImage("background_op")).
                 setAlpha( .9 ).
                 setGlobalAlpha(false);
 
         Button menu= new Button() {
             
             @Override
             public void mouseClick(CAATMouseEvent mouseEvent) throws Exception {
                 
                 Anchor a0 = Actor.Anchor.LEFT;
                 Anchor a1 = Actor.Anchor.RIGHT;
 
                 switch( context.difficulty ) {
                     case 0:
                         a0= Actor.Anchor.LEFT;
                         a1= Actor.Anchor.RIGHT;
                     break;
                     case 1:
                         a0= Actor.Anchor.BOTTOM;
                         a1= Actor.Anchor.TOP;
                     break;
                     case 2:
                         a0= Actor.Anchor.TOP;
                         a1= Actor.Anchor.BOTTOM;
                     break;
                 }
                 
                 director.easeInOut(
                         0,
                         Scene.Ease.TRANSLATE,
                         a0,
                         1,
                         Scene.Ease.TRANSLATE,
                         a1,
                         1000,
                         false,
                         new Interpolator().createExponentialInOutInterpolator(3,false),
                         new Interpolator().createExponentialInOutInterpolator(3,false) );
                 
             }
             
             
         }.
                 
                 initialize( this.buttonImage, 20,21,22,23 );
         
 
         Button restart= new Button() {
             @Override
             public void mouseClick(CAATMouseEvent mouseEvent) {
                 prepareSceneIn();
                 context.initialize();
             }
         }.
                 
                 initialize( this.buttonImage, 0,1,2,3 );
 
         double x= (this.endGameActor.width - 2*menu.width - 30)/2;
         double y= this.endGameActor.height-20-menu.height;
 
         menu.setLocation( x, y );
         restart.setLocation( x+menu.width+30, y );
 
         this.endGameActor.addChild(menu);
         this.endGameActor.addChild(restart);
 
         this.endGameActor.setFrameTime(-1,0);
 
         this.directorScene.addChild(this.endGameActor);
     }
     
     int getBrickWidth () {
         return (int) this.bricksImage.singleWidth + this.gap;
     }
     
     int getBrickHeight () {
         return (int) this.bricksImage.singleHeight + this.gap;
     }
     
     public void initializeActors () {
         
         this.selectionPath.initialize();
         
         int i, j;
 
         double radius= Math.max(
                 this.director.canvas.getCoordinateSpaceWidth(),
                 this.director.canvas.getCoordinateSpaceHeight() );
 
         double angle= Math.PI*2*Math.random();
         
         double p0= Math.random()*this.director.canvas.getCoordinateSpaceWidth();
         double p1= Math.random()*this.director.canvas.getCoordinateSpaceHeight();
         double p2= Math.random()*this.director.canvas.getCoordinateSpaceWidth();
         double p3= Math.random()*this.director.canvas.getCoordinateSpaceHeight();
 
         for( i=0; i<this.gameRows; i++ ) {
             for( j=0; j<this.gameColumns; j++ ) {
                 BrickActor brickActor= this.brickActors[i][j];
                 brickActor.setFrameTime( this.directorScene.time, Double.MAX_VALUE ).
                 setAlpha(1).
                 enableEvents(true).
                 resetTransform();
 
                 double random= Math.random()*1000;
 
                 PathBehavior moveB= (PathBehavior) new PathBehavior().
                         setFrameTime(this.directorScene.time, 1000+random);
                 moveB.setPath(
                             new CurvePath().
                             setCubic(
                                     radius/2 + Math.cos(angle)*radius,
                                     radius/2 + Math.sin(angle)*radius,
                                     p0, p1, p2, p3,
                                     j*this.bricksImage.singleWidth + j*2,
                                     i*this.bricksImage.singleHeight + i*2)
                                  ).
 
                         setInterpolator(
                                 new Interpolator().createExponentialInOutInterpolator(3,false) );
                 ScaleBehavior sb= (ScaleBehavior) new ScaleBehavior().
                         setFrameTime(this.directorScene.time , 1000+random);
                         sb.setValues( .1, 1, .1 , 1).
                         setInterpolator(
                                 new Interpolator().createExponentialInOutInterpolator(3,false) );
 
                         brickActor.emptyBehaviorList().
                         addBehavior(moveB).
                         addBehavior(sb).
                         enableEvents(false);
                 
                 actorCount = 0;
                 moveB.addListener(new BehaviorListener() {
                     @Override
                     public void behaviorExpired(BaseBehavior behaviour, double time, Actor actor) {
                         actorCount++;
                         if ( actorCount== gameRows * gameColumns ) {
                             if ( context.status==context.ST_INITIALIZING ) {
                                 context.setStatus( context.ST_RUNNNING );
                             }
                         }
                     }
 
                     public void behaviorApplied(BaseBehavior behavior, double time, double normalizeTime, Actor actor, SetForTimeReturnValue value) {
                         
                     }
 
                     @Override
                     public void behaviorStarted(BaseBehavior behavior, double time, Actor actor) {
                         
                     }
                     
                 });
 
             }
         }
 
         this.actorInitializationCount=0;
     }
     
     private int actorCount;
     
     private int count;
     
     public void contextEvent (Event event ) {
         int i, j;
         BrickActor brickActor;
 
         if ( event.source.equals("context")) {
             if ( event.event.equals("status")) {
                 if ( event.params==this.context.ST_INITIALIZING ) {
                     this.initializeActors();
                 } else if ( event.params==this.context.ST_RUNNNING) {
                     for( i=0; i<this.gameRows; i++ ) {
                         for( j=0; j<this.gameColumns; j++ ) {
                             brickActor= this.brickActors[i][j];
                             brickActor.enableEvents(true);
                         }
                     }
                     
                     this.cancelTimer();
                     this.enableTimer();
                     
                 } else if ( event.params==this.context.ST_LEVEL_RESULT ) {
                     this.cancelTimer();
                     this.context.nextLevel();
                 } else if ( event.params==this.context.ST_ENDGAME ) {
                     this.endGame();
                 }
                 
             } else if ( event.event.equals("levelchange")) {
                 this.levelActor.setText( "Level "+this.context.level );
                 this.levelActor.calcTextSize( this.director );
                 this.levelActor.x= (this.levelActor.parent.width-this.levelActor.width)/2;
             }
             
         } else if ( event.source.equals("brick")) {
             if ( event.event.equals("selection") ) {   // des/marcar un elemento.
                 this.brickSelectionEvent(event);
 
             } else if ( event.event.equals("selectionoverflow")) {  // seleccion error.
                     this.selectionOverflowEvent(event);
 
             } else if ( event.event.equals("selection-cleared")) {  // seleccion error.
                 this.selectionClearedEvent(event);
             }
 
             // rebuild selection path
             this.selectionPath.setup(
                     this.context,
                     this.getBrickWidth(),
                     this.getBrickHeight() );
         }
     }
     
     private void brickSelectionEvent(Event event) {
       Brick brick= event.brick;
       BrickActor brickActor= this.brickActors[brick.row][brick.column];
 
       if ( brick.selected ) {
           brickActor.emptyBehaviorList();
 
           ScaleBehavior sb= (ScaleBehavior) new ScaleBehavior().
                   setValues( 1, .5, 1, .5 ).
                   setFrameTime( 0, 1000 ).
                   setPingPong();
           AlphaBehavior ab= (AlphaBehavior) new AlphaBehavior().
                   setValues( 1, .25 ).
                   setFrameTime( 0, 1000 ).
                   setPingPong();
 
           ContainerBehavior cb= (ContainerBehavior) new ContainerBehavior().
                   setFrameTime( 0, 1000 ).
                   setCycle(true).
                   setPingPong();
                   cb.addBehavior( sb ).
                   addBehavior( ab );
 
           brickActor.addBehavior(cb);
       }
       else {
           brickActor.reset();
       }
     }
     
     private void selectionOverflowEvent(Event event) {
         int i, j;
       List<Brick> selectedContextBricks= event.bricks;
       for( i=0; i<selectedContextBricks.size(); i++ ) {
           this.brickActors[ selectedContextBricks.get(i).row ][ selectedContextBricks.get(i).column ].reset();
       }
 
       this.bricksContainer.enableEvents(false);
 
       // get all active actors on board
       List<BrickActor> activeActors = new ArrayList<BrickActor>();
       for( i=0; i<this.gameRows; i++ ) {
           for( j=0; j<this.gameColumns; j++ ) {
               BrickActor actor= this.brickActors[i][j];
               if ( !actor.brick.removed ) {
                   activeActors.add(actor);
               }
           }
       }
 
       // define animation callback
       count=0;
       final int maxCount= activeActors.size();
 
       // for each active actor, play a wrong-path.
       for( i=0; i<activeActors.size(); i++ ) {
           BrickActor actor= activeActors.get(i);
 
           double signo= Math.random()<.5 ? 1: -1;
 
           Path path = new Path().
               beginPath( actor.x, actor.y ).
               addLineTo(actor.x + signo*(5+5*Math.random()), actor.y, null ).
               addLineTo(actor.x - signo*(10+5*Math.random()), actor.y, null).
               closePath();
           
           PathBehavior pathBehavior = (PathBehavior) new PathBehavior().
               setFrameTime(this.directorScene.time, 200);
               pathBehavior.setPath(path).
               addListener(new BehaviorListener() {
                   @Override
                   public void behaviorExpired(BaseBehavior behaviour, double time, Actor actor) {
                       count++;
                       if ( count==maxCount ) {
                           bricksContainer.enableEvents(true);
                       }
                   }
 
                   @Override
                   public void behaviorApplied(BaseBehavior behavior, double time, double normalizeTime,
                           Actor actor, SetForTimeReturnValue value) {
                   }
 
                 @Override
                 public void behaviorStarted(BaseBehavior behavior, double time, Actor actor) {
                     
                 }
               }).
               setPingPong() ;
           
           actor.emptyBehaviorList().
               addBehavior(pathBehavior);
       }
     }
     
     private void selectionClearedEvent(Event event) {
       List<Brick> selectedContextBricks= event.bricks;
       int i, j;
       for( i=0; i<selectedContextBricks.size(); i++ ) {
 
           BrickActor actor= this.brickActors[ selectedContextBricks.get(i).row ][ selectedContextBricks.get(i).column ];
 
           double signo= Math.random()<.5 ? 1 : -1;
           double offset= 50+Math.random()*30;
           double offsetY= 60+Math.random()*30;
           
           actor.parent.setZOrder(actor, Integer.MAX_VALUE);
           
           Path path = (Path) new Path().
               beginPath( actor.x, actor.y ).
               addQuadricTo(
                       actor.x+offset*signo,   actor.y-300,
                       actor.x+offset*signo*2, actor.y+this.director.canvas.getCoordinateSpaceHeight()+20, null).
                       endPath();
           
           PathBehavior pathBehavior = (PathBehavior) new PathBehavior()
               .setFrameTime( this.directorScene.time, 800 );
               pathBehavior.setPath(path)
               .addListener(new BehaviorListener() {
                           @Override
                           public void behaviorExpired(BaseBehavior behavior, double time, Actor actor) {
                               actor.setExpired(true);
                           }
                           
                           @Override
                           public void behaviorApplied(BaseBehavior behavior, double time, double normalizeTime, Actor actor, SetForTimeReturnValue value) throws Exception {
 //                              System.out.println("behavior applied");
                               List<String> colors= Arrays.asList("#00ff00","#ffff00","#00ffff");
                               for(int i=0; i<3; i++ ) {
                                   double offset0= Math.random()*10*(Math.random()<.5?1:-1);
                                   double offset1= Math.random()*10*(Math.random()<.5?1:-1);
                                   directorScene.addChild(
                                       new ShapeActor().
                                           
                                           setBounds( offset0+actor.x-3, offset1+actor.y-3, 6, 6 ).
                                           setShape( ShapeActor.Shape.CIRCLE).
                                           setFillStyle( colors.get(i%3) ).
                                           setDiscardable(true).
                                           setFrameTime(directorScene.time, 300).
                                           addBehavior(
                                               new AlphaBehavior().
                                                   setFrameTime(directorScene.time, 300).
                                                   setValues( .6, .1 )
                                           ) );
                               }
                           }
 
                         @Override
                         public void behaviorStarted(BaseBehavior behavior, double time, Actor actor) {
                             
                         }
                       });
           
           
           actor.enableEvents(false).
               emptyBehaviorList().
               addBehavior(pathBehavior).
               addBehavior(
                   new RotateBehavior().
                       setFrameTime( this.directorScene.time, 800 ).
                       setAngles( 0, (Math.PI + Math.random()*Math.PI*2)*(Math.random()<.5?1:-1) )
               ).addBehavior(
                   new AlphaBehavior().
                       setFrameTime( this.directorScene.time, 800 ).
                       setValues( 1, .25 )
               ).setScale( 1.5, 1.5 );
       }
       
       this.timer.reset(this.directorScene.time);
       
     }
     
     void showLevelInfo() {
 //        ActorContainer container= (ActorContainer) new ActorContainer().
 //                
 //                setBounds( this.bricksContainer.x, this.bricksContainer.y,
 //                           this.bricksContainer.width, this.bricksContainer.height );
 //
 //        RotateBehavior rb= (RotateBehavior) new RotateBehavior().
 //                setCycle(true).
 //                setFrameTime( this.directorScene.time, 3000 );
 //                rb.setAngles( -Math.PI/8, Math.PI/8 ).
 //                setInterpolator( new Interpolator().createExponentialInOutInterpolator(3,true) );
 //                rb.setAnchor( Actor.Anchor.TOP );
 //
 //        CanvasGradient gradient= this.director.ctx.createLinearGradient(0,0,0,90);
 //        gradient.addColorStop(0,"#00ff00");
 //        gradient.addColorStop(0.5,"#ffff00");
 //        gradient.addColorStop(1,"#3f3f3f");
 //
 //        TextActor text= (TextActor) new TextActor().
 //                
 //                setFont("90px sans-serif").
 //                setText("Level "+this.context.level).
 //                setFillStrokeStyle( gradient ).
 //                setOutline(true).
 //                addBehavior( rb );
 //                text.calcTextSize(this.director);
 //
 //        text.setLocation((container.width-text.textWidth)/2,40);
 //        container.addChild(text);
 //
 //        this.directorScene.addChild(container);
     }
     
     public void prepareSceneIn () {
         // setup de actores
 
             this.bricksContainer.enableEvents(true);
 
             // fuera de pantalla
             for(int i=0; i<this.gameRows; i++ ) {
                 for(int j=0; j<this.gameColumns; j++ ) {
                     this.brickActors[i][j].setLocation(-100,-100);
                 }
             }
 
             this.selectionPath.initialize();
 
             this.chronoActor.tick(0,0);
             this.scoreActor.reset();
             
             this.endGameActor.setFrameTime(-1,0);
     }
     
     public void endGame () {
         // parar y eliminar cronometro.
         this.cancelTimer();
         
         // quitar contorl de mouse.
         this.bricksContainer.enableEvents(false);
         
      // mostrar endgameactor.
 
         double x= (this.directorScene.width - this.endGameActor.width)/2;
         double y= (this.directorScene.height - this.endGameActor.height)/2;
 
         final ImageActor me_endGameActor= this.endGameActor;
         
         LinearPath path = new LinearPath().
                 setInitialPosition( x, this.directorScene.height ).
                 setFinalPosition( x, y );
         
         PathBehavior pb = (PathBehavior) new PathBehavior().
                 setFrameTime( this.directorScene.time, 800 );
                 pb.setPath(
                     path);
         
         this.endGameActor.emptyBehaviorList().
             setFrameTime(this.directorScene.time, Double.MAX_VALUE).
             enableEvents(false).
             addBehavior(pb
                 .
             setInterpolator(
                 new Interpolator().createBounceOutInterpolator(false) ).
                 
             addListener(new BehaviorListener() {
                 
                 @Override
                 public void behaviorExpired(BaseBehavior behavior, double time, Actor actor) {
                     me_endGameActor.enableEvents(true);
                 }
                 
                 @Override
                 public void behaviorApplied(BaseBehavior behavior, double time, double normalizeTime, Actor actor, SetForTimeReturnValue value) {
                     
                 }
 
                 @Override
                 public void behaviorStarted(BaseBehavior behavior, double time, Actor actor) {
                     
                 }
            }));
                 
            
     }
     
     public void setDifficulty (int level) {
         this.context.difficulty=level;
     }
     
     public void cancelTimer (){
         if ( this.timer!=null ) {
             this.timer.cancel();
         }
         this.timer= null;
     }
     
     public void enableTimer () {
         
         this.timer= this.directorScene.createTimer(
             this.directorScene.time,
             this.context.turnTime,
             new Callback() {
                 @Override
                 public void call(double sceneTime, double ttime, TimerTask timerTask) {
                     context.timeUp();
                 }
             },
             new Callback() {
                 @Override
                 public void call(double sceneTime, double ttime, TimerTask timerTask) {
                     chronoActor.tick(ttime, timerTask.duration);
                 }
             },
             null);
 
     }
     
     
 //    public void startGame () {
 //        int iNewSceneIndex= this.director.getSceneIndex(this.directorScene);
 //        this.director.switchToScene( iNewSceneIndex, 2000, false, true );
 //    }
 
 }
