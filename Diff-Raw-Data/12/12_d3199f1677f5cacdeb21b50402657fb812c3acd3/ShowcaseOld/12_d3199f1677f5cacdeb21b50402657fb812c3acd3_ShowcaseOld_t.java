 package com.katspow.caatjagwtdemos.client.showcase;
 
 import com.katspow.caatja.CAATKeyListener;
 import com.katspow.caatja.behavior.AlphaBehavior;
 import com.katspow.caatja.behavior.BaseBehavior;
 import com.katspow.caatja.behavior.BaseBehavior.Status;
 import com.katspow.caatja.behavior.BehaviorListener;
 import com.katspow.caatja.behavior.ContainerBehavior;
 import com.katspow.caatja.behavior.Interpolator;
 import com.katspow.caatja.behavior.PathBehavior;
 import com.katspow.caatja.behavior.RotateBehavior;
 import com.katspow.caatja.behavior.ScaleBehavior;
 import com.katspow.caatja.behavior.SetForTimeReturnValue;
 import com.katspow.caatja.core.CAAT;
 import com.katspow.caatja.core.Caatja;
 import com.katspow.caatja.core.canvas.CaatjaCanvas;
 import com.katspow.caatja.core.canvas.CaatjaContext2d;
 import com.katspow.caatja.core.canvas.CaatjaFillStrokeStyle;
 import com.katspow.caatja.core.image.CaatjaImageLoader;
 import com.katspow.caatja.core.image.CaatjaImageLoaderCallback;
 import com.katspow.caatja.core.image.CaatjaPreloader;
 import com.katspow.caatja.event.CAATKeyEvent;
 import com.katspow.caatja.event.CAATMouseEvent;
 import com.katspow.caatja.foundation.Director;
 import com.katspow.caatja.foundation.Scene;
 import com.katspow.caatja.foundation.actor.Actor;
 import com.katspow.caatja.foundation.actor.ActorContainer;
 import com.katspow.caatja.foundation.ui.ShapeActor;
 import com.katspow.caatja.foundation.ui.TextActor;
 import com.katspow.caatja.math.Pt;
 import com.katspow.caatja.math.matrix.Matrix;
 import com.katspow.caatja.pathutil.Path;
 import com.katspow.caatjagwtdemos.client.showcase.actor.SpecialActor;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.AnotherSpecialScene;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.Scene1;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.Scene10;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.Scene11;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.Scene12;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.Scene2;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.Scene3;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.Scene4;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.Scene5;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.Scene6;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.Scene7;
 import com.katspow.caatjagwtdemos.client.showcase.scenes.Scene8;
 
/**
 * Deprecated version of showcase entry point 
 *
 */
@Deprecated
 public class ShowcaseOld {
 	
     private void setupTRButton(Actor prev) {
         
         ScaleBehavior sb = new ScaleBehavior().
                 setPingPong().
                 setValues(1, 1.5, 1, 1.5);
         prev.addBehavior(sb);
         
         AlphaBehavior ab= new AlphaBehavior().
                 setPingPong().
                 setValues(1,.5).
                 setCycle(true);
         prev.addBehavior(ab);
         
     }
     
     private void setupTRButtonPaint(Actor prev) {
         prev.fillStyle =new CaatjaFillStrokeStyle("#0000ff");
     }
     
     private void setupTRButtonPaintIndex(Actor idx, int i) {
         // DO nothing
     }
     
     public void __CAAT_director_initialize(final Director director) throws Exception {
         
 //        List<ImageElement> images = loadImages();
 //        
 //        CaatjaCanvas canvas = Caatja.createCanvas();;
 //        final Director director = new Director();
 //        director.initialize(canvas, 680, 500);
         
         director.emptyScenes();
         
         director.addScene(Scene1.init(director));
         director.addScene(Scene2.init(director));
         director.addScene(Scene3.init(director));
         director.addScene(Scene4.init(director));
         director.addScene(Scene5.init(director));
         director.addScene(Scene6.init(director));
         director.addScene(Scene12.init(director));
         director.addScene(Scene7.init(director));
         director.addScene(Scene8.init(director));
         
         // Scene9 bugged
 //        director.addScene(Scene9.init(director));
         
         // Scene Experimental bugged
         // director.addScene(SceneExperimental.init(director));
 
         Scene10.init(director);
         
         director.addScene(Scene11.init(director));
         
 
 
         director.easeIn(
                 0,
                 Scene.Ease.SCALE,
                 2000,
                 false,
                 Actor.Anchor.CENTER,
                 new Interpolator().createElasticOutInterpolator(2.5, .4, false) );
         
         double buttonW= 22.5;
         double buttonX= (director.width - buttonW*director.getNumScenes())/2;
         
         for( int i=0; i<director.getNumScenes(); i++ ) {
 
             if ( i!=0 ) {
                 Actor prev = new Actor() {
                     @Override
                     public void mouseClick(CAATMouseEvent mouseEvent) throws Exception {
                         director.switchToPrevScene(1000,false,true);
                     }
 
                     @Override
                     public void mouseEnter(CAATMouseEvent mouseEvent) {
                       Actor actor= mouseEvent.source;
                       if( null==actor ) {
                           return;
                       }
                       
                       BaseBehavior behaviour = actor.behaviorList.get(0);
                       if( null==behaviour ) {
                           return;
                       }
                       
                       actor.pointed= true;
                       
                       if ( behaviour.status == Status.EXPIRED) {
                           actor.behaviorList.get(0).
                               setFrameTime( mouseEvent.source.time, 1000 ).
                               setCycle(true);
                           actor.behaviorList.get(1).
                               setFrameTime( mouseEvent.source.time, 1000 ).
                               setCycle(true);
                       }
                     }
 
                     @Override
                     public void mouseExit(CAATMouseEvent mouseEvent) {
                       Actor actor= mouseEvent.source;
                       if( null==actor ) {
                           return;
                       }
                       BaseBehavior behaviour = actor.behaviorList.get(0);
                       if( null==behaviour ) {
                           return;
                       }
                       
                       actor.pointed= false;
                       
                       actor.behaviorList.get(0).setExpired(actor,mouseEvent.source.time);
                       actor.behaviorList.get(1).setExpired(actor,mouseEvent.source.time);
                     }
 
                     @Override
                     public void paint(Director director, double time) {
                         CaatjaContext2d canvas= director.ctx;
                         
                         if ( null!=this.parent && null!=this.fillStyle ) {
                             canvas.beginPath();
                             canvas.setFillStyle(this.pointed ? new CaatjaFillStrokeStyle("orange") : (this.fillStyle!=null ? this.fillStyle : new CaatjaFillStrokeStyle("white"))); //'white';
                             canvas.arc(10,10,10,0,Math.PI*2,false );
                             canvas.fill();
                         }
 
                         if ( this.clip ) {
                             canvas.beginPath();
                             canvas.rect(0,0,this.width,this.height);
                             canvas.clip();
                         }
                             
                         canvas.setStrokeStyle(this.pointed ?  new CaatjaFillStrokeStyle("green") :  new CaatjaFillStrokeStyle("#ffff00"));
                         canvas.beginPath();
 
                         canvas.moveTo(3,10);
                         canvas.lineTo(17,10);
                         canvas.lineTo(13,5);
                         
                         canvas.moveTo(17,10);
                         canvas.lineTo(13,15);
                         
                         canvas.setLineWidth(2);
                         canvas.setLineJoin("round");
                         canvas.setLineCap("round");
 
                         canvas.stroke();
                     }
                     
                     
                 };
                 
                 prev.setBounds(5,470,20,20);
                 prev.setRotation( Math.PI );
                 prev.fillStyle= new CaatjaFillStrokeStyle("#0000ff");
                 
                 director.scenes.get(i).addChild(prev);
                 
                 setupTRButton(prev);
                 setupTRButtonPaint(prev);
             }
             if ( i!=director.getNumScenes()-1 ) {
                 Actor next= new Actor() {
 
                     @Override
                     public void mouseClick(CAATMouseEvent mouseEvent) throws Exception {
                         director.switchToNextScene(1000,false,true);
                     }
                     
                     @Override
                     public void mouseEnter(CAATMouseEvent mouseEvent) {
                       Actor actor= mouseEvent.source;
                       if( null==actor ) {
                           return;
                       }
                       
                       BaseBehavior behaviour = actor.behaviorList.get(0);
                       if( null==behaviour ) {
                           return;
                       }
                       
                       actor.pointed= true;
                       
                       if ( behaviour.status == Status.EXPIRED ) {
                           actor.behaviorList.get(0).setFrameTime( mouseEvent.source.time, 1000 );
                           actor.behaviorList.get(0).setCycle(true);
                           actor.behaviorList.get(1).setFrameTime( mouseEvent.source.time, 1000 );
                           actor.behaviorList.get(1).setCycle(true);
                       }
                     }
 
                     @Override
                     public void mouseExit(CAATMouseEvent mouseEvent) {
                       Actor actor= mouseEvent.source;
                       if( null==actor ) {
                           return;
                       }
                       BaseBehavior behaviour = actor.behaviorList.get(0);
                       if( null==behaviour ) {
                           return;
                       }
                       
                       actor.pointed= false;
                       
                       actor.behaviorList.get(0).setExpired(actor,mouseEvent.source.time);
                       actor.behaviorList.get(1).setExpired(actor,mouseEvent.source.time);
                     }
 
                     @Override
                     public void paint(Director director, double time) {
                         CaatjaContext2d canvas= director.ctx;
                         
                         if ( null!=this.parent && null!=this.fillStyle ) {
                             canvas.beginPath();
                             canvas.setFillStyle(this.pointed ? new CaatjaFillStrokeStyle("orange") : (this.fillStyle!=null ? this.fillStyle : new CaatjaFillStrokeStyle("white"))); //'white';
                             canvas.arc(10,10,10,0,Math.PI*2,false );
                             canvas.fill();
                         }
 
                         if ( this.clip ) {
                             canvas.beginPath();
                             canvas.rect(0,0,this.width,this.height);
                             canvas.clip();
                         }
                             
                         canvas.setStrokeStyle(this.pointed ?  new CaatjaFillStrokeStyle("green") :  new CaatjaFillStrokeStyle("#ffff00"));
                         canvas.beginPath();
 
                         canvas.moveTo(3,10);
                         canvas.lineTo(17,10);
                         canvas.lineTo(13,5);
                         
                         canvas.moveTo(17,10);
                         canvas.lineTo(13,15);
                         
                         canvas.setLineWidth(2);
                         canvas.setLineJoin("round");
                         canvas.setLineCap("round");
 
                         canvas.stroke();
                     }
                     
                 };
                 
                 next.setBounds(director.width-20-5,470,20,20);
                 next.fillStyle=new CaatjaFillStrokeStyle("#0000ff");
 
                 director.scenes.get(i).addChild(next);
                 
                 setupTRButton(next);
                 setupTRButtonPaint(next);
             }
             
             for( int j=0; j<director.getNumScenes(); j++ ) {
                 
                 SpecialActor idx = new SpecialActor() {
                     @Override
                     public void mouseClick(CAATMouseEvent mouseEvent) throws Exception {
                         director.switchToScene(((SpecialActor)mouseEvent.source).__sceneIndex,1000,false,true);
                     }
 
                     @Override
                     public void paint(Director director, double time) {
                         CaatjaContext2d canvas= director.ctx;
                         
                         if ( null!=this.parent && null!=this.fillStyle ) {
                             canvas.beginPath();
                             canvas.setFillStyle(this.pointed ? new CaatjaFillStrokeStyle("orange") : (this.fillStyle!=null ? this.fillStyle : new CaatjaFillStrokeStyle("white"))); //'white';
                             canvas.arc(10,10,10,0,Math.PI*2,false );
                             canvas.fill();
                         }
 
                         if ( this.clip ) {
                             canvas.beginPath();
                             canvas.rect(0,0,this.width,this.height);
                             canvas.clip();
                         }
                             
                         canvas.setFillStyle("#ffff00");
                         canvas.setTextBaseline("top");
                         canvas.setFont("18px sans-serif");
                         String str= ""+(this.__sceneIndex+1);
                         double w= canvas.measureTextWidth(str);
                         canvas.fillText( str, (this.width-w)/2, 0 );
 
                     }
                     
                     
                 };
                 
                 idx.name = "idx";
                 idx.__sceneIndex=j;
                 idx.setBounds(buttonX+j*buttonW,470,20,20);
                 if ( j!=i ) {
                     // TODO ???
 //                    idx.mouseClick = function(mouseEvent) {
 //                        director.switchToScene(mouseEvent.source.__sceneIndex,1000,false,true);
 //                    };
                     setupTRButton(idx);
                     idx.fillStyle=new CaatjaFillStrokeStyle("#0000ff");
                 } else {
                     idx.mouseEnabled= false;
                     idx.fillStyle= new CaatjaFillStrokeStyle("#c07f00");
                 }
                 director.scenes.get(i).addChild(idx);
                 
                 setupTRButtonPaintIndex(idx, j+1);
                         
             }
         }
 
 //        final double time = Caatja.getTime();
 ////        director.time = 0d;
 //
 //        Timer t = new Timer() {
 //            @Override
 //            public void run() {
 //                JsDate jsDate = JsDate.create();
 //                double t = jsDate.getTime();
 //                CAAT.director.render(t - CAAT.time);
 //                CAAT.time = t;
 ////                double time2 = jsDate.getTime() - time;
 ////                director.render(time2);
 //            }
 //        };
 //
 //        director.setScene(0);
 //        t.scheduleRepeating(30);
 
     }
     
     /**
      * Sample loading scene.
      * @param director
      * @throws Exception 
      */
     public AnotherSpecialScene __CAAT__loadingScene(final Director director) throws Exception {
 
         final AnotherSpecialScene scene= new AnotherSpecialScene();
 
         ActorContainer root= new ActorContainer() {
             
             @Override
             public void mouseEnter (CAATMouseEvent mouseEvent) {
                 
             }
             
             @Override
             public void mouseExit (CAATMouseEvent mouseEvent) {
                 
             }
             
             @Override
             public void mouseMove(CAATMouseEvent mouseEvent) throws Exception {
                 
                 int r = (int) (1+10*Math.random());
                 ShapeActor burbuja= new ShapeActor().
                         setLocation( mouseEvent.point.x, mouseEvent.point.y ).
                         enableEvents(false).
                         setCompositeOp("lighter").
                         setSize( 5+r, 5+r );
 
                 r = 192 + (int) (64 * Math.random()) >> 0;
                 int g = (int) (64 * Math.random()) >> 0;
                 int b = (int) (64 * Math.random()) >> 0;
                 int a = 255;
 
                 burbuja.fillStyle = new CaatjaFillStrokeStyle("rgba("+r+","+g+","+b+","+a+")");
 
                 this.addChild(burbuja);
 
                 ContainerBehavior cb= new ContainerBehavior();
                 cb.actor= burbuja;
 
                 cb.setFrameTime( scene.time+2000+1000*Math.random(), 500 );
                 cb.addListener(new BehaviorListener() {
                     @Override
                     public void behaviorExpired(BaseBehavior behaviour, double time, Actor actor) {
                         behaviour.actor.discardable= true;
                         behaviour.actor.setExpired(true);
                     }
 
                     @Override
                     public void behaviorApplied(BaseBehavior behavior, double time, double normalizeTime, Actor actor,
                             SetForTimeReturnValue value) {
                     }
 
                     @Override
                     public void behaviorStarted(BaseBehavior behavior, double time, Actor actor) {
                         
                     }
                     
                 });
                 
                 AlphaBehavior ab= new AlphaBehavior().
                     setFrameTime( 0, 500 ).
                     setValues(1,0);
                     
                     cb.addBehavior(ab);
 
                     PathBehavior tb= new PathBehavior().
                             setFrameTime( 0, 500 ).
                             setPath(
                             new Path().setLinear(
                                     burbuja.x, burbuja.y,
                                     burbuja.x, burbuja.y-100-100*Math.random() ) );
                     cb.addBehavior(tb);
 
                 burbuja.addBehavior( cb );
                 
             }
             
         };
         
         root.setBounds(0,0,director.canvas.getCoordinateSpaceWidth(),director.canvas.getCoordinateSpaceHeight());
         scene.addChild( root );
 
         root.fillStyle = new CaatjaFillStrokeStyle("#000000");
 
         TextActor textLoading= new TextActor() {
             @Override
             public void mouseClick(CAATMouseEvent mouseEvent) throws Exception {
                 __CAAT_director_initialize(director);
             }
         };
         
         textLoading.setFont("20px sans-serif").
             setTextBaseline("top").
             setText("Loading").
             calcTextSize(director).
             setTextFillStyle("white");
         root.addChild(textLoading);
         textLoading.setLocation(
                 (director.canvas.getCoordinateSpaceWidth()-textLoading.width)/2,
                 (director.canvas.getCoordinateSpaceHeight())/2);
 
         scene.loading= textLoading;
 
         RotateBehavior rb= new RotateBehavior().
                 setCycle(true).
                 setFrameTime( 0, 5000 ).
                 setValues( -Math.PI/4, Math.PI/4, .5, 0d ).
                 setInterpolator( new Interpolator().createCubicBezierInterpolator(new Pt().set(0, 0),new Pt().set(1, 0),new Pt().set(0, 1),new Pt().set(1, 1), true ) );
         textLoading.addBehavior(rb);
 
         /**
          * This method will be called after imagePreloader after loading each image resource.
          */
 //        scene.loadedImage= function(index,size) {
 //            textLoading.setText( "Loading "+index+"/"+size );
 //        }
 
         /**
          * This method will be called after imagePreloader ends loading resources.
          */
 //        scene.finishedLoading= function() {
 //            this.loading.setText("Start");
 //            this.loading.emptyBehaviourList();
 //
 //            ScaleBehaviour sb= new ScaleBehaviour();
 //            sb.setPingPong();
 //            sb.anchor= Actor.Anchor.CENTER;
 //            sb.minScaleX= 1;
 //            sb.maxScaleX= 4;
 //            sb.minScaleY= 1;
 //            sb.maxScaleY= 4;
 //            sb.setCycle(true);
 //            sb.setFrameTime( scene.time, 1000 );
 //
 //            this.loading.addBehaviour(sb);
 //
 //        };
 
         return scene;
     }
 
     /**
      * Entry point from document loading.
      * @throws Exception 
      */
     public void __CAAT_init() throws Exception {
         
         Matrix m= new Matrix().setRotation(Math.PI/4).multiply(
                 Matrix.scale(2,2) );
         
         
         final CaatjaCanvas canvas = Caatja.createCanvas();
 //        caatjaService.createCanvas();
         
         final Director director = new Director();
         director.initialize(680, 500,canvas);
         
         CAAT.registerKeyListener(new CAATKeyListener() {
             @Override
             public void call(CAATKeyEvent keyEvent) {
                 if (keyEvent.keyCode == 68 && keyEvent.action.equals("up")) {
                     director.debug = !director.debug;
                 }
             }
         });
         
         final AnotherSpecialScene scene_loading= __CAAT__loadingScene(director);
         director.addScene( scene_loading );
         director.setScene(0);
         
         
         final CaatjaPreloader preloader = Caatja.getCaatjaImagePreloader();
         preloader.addImage("fish", "anim1.png");
         preloader.addImage("fish2", "anim2.png");
         preloader.addImage("fish3", "anim3.png");
         preloader.addImage("fish4", "anim4.png");
         preloader.addImage("chapas", "chapas.jpg");
         preloader.addImage("buble1", "burbu1.png");
         preloader.addImage("buble2", "burbu2.png");
         preloader.addImage("buble3", "burbu3.png");
         preloader.addImage("buble4", "burbu4.png");
         preloader.addImage("plants", "plants.jpg");
         preloader.addImage("bumps", "3.jpg");
         
         CaatjaImageLoader caatjaImageLoader = Caatja.getCaatjaImageLoader();
         caatjaImageLoader.loadImages(preloader, new CaatjaImageLoaderCallback() {
             @Override
             public void onFinishedLoading() throws Exception {
                 director.imagesCache = preloader.getCaatjaImages();
                 scene_loading.finishedLoading();
                 
                 Caatja.addCanvas(canvas);
 
                 director.loop(60);
             }
         });
         
 //        Map<String, CaatjaImage> images = new HashMap<String, CaatjaImage>();
 //        
 //        final ImageResources INSTANCE = GWT.create(ImageResources.class);
 //        
 //        Image fishImage = new Image(INSTANCE.fish().getSafeUri());
 //        Image fishImage2 = new Image(INSTANCE.fish2().getSafeUri());
 //        Image fishImage3 = new Image(INSTANCE.fish3().getSafeUri());
 //        Image fishImage4 = new Image(INSTANCE.fish4().getSafeUri());
 //        Image chapas = new Image(INSTANCE.chapas().getSafeUri());
 //        Image burbu1 = new Image(INSTANCE.burbu1().getSafeUri());
 //        Image burbu2 = new Image(INSTANCE.burbu2().getSafeUri());
 //        Image burbu3 = new Image(INSTANCE.burbu3().getSafeUri());
 //        Image burbu4Image = new Image(INSTANCE.burbu4().getSafeUri());
 //        Image plantsImage = new Image(INSTANCE.plants().getSafeUri());
 //        Image bumpimage = new Image(INSTANCE.bump().getSafeUri());
 //        
 //        images.put("fish", ImageElement.as(fishImage.getElement()));
 //        images.put("fish2", ImageElement.as(fishImage2.getElement()));
 //        images.put("fish3", ImageElement.as(fishImage3.getElement()));
 //        images.put("fish4", ImageElement.as(fishImage4.getElement()));
 //        images.put("chapas", ImageElement.as(chapas.getElement()));
 //        images.put("buble1", ImageElement.as(burbu1.getElement()));
 //        images.put("buble2", ImageElement.as(burbu2.getElement()));
 //        images.put("buble3", ImageElement.as(burbu3.getElement()));
 //        images.put("buble4", ImageElement.as(burbu4Image.getElement()));
 //        images.put("plants", ImageElement.as(plantsImage.getElement()));
 //        images.put("bumps", ImageElement.as(bumpimage.getElement()));
         
 //        director.imagesCache = images;
         
 //        scene_loading.finishedLoading();
 
 //        new CAAT.ImagePreloader().loadImages(
 //            [
 //                {id:'fish',     url:'res/img/anim1.png'},
 //                {id:'fish2',    url:'res/img/anim2.png'},
 //                {id:'fish3',    url:'res/img/anim3.png'},
 //                {id:'fish4',    url:'res/img/anim4.png'},
 //                {id:'chapas',   url:'res/img/chapas.jpg'},
 //                {id:'buble1',   url:'res/img/burbu1.png'},
 //                {id:'buble2',   url:'res/img/burbu2.png'},
 //                {id:'buble3',   url:'res/img/burbu3.png'},
 //                {id:'buble4',   url:'res/img/burbu4.png'}
 //            ],
 //
 //            function( counter, images ) {
 //                scene_loading.loadedImage(counter, images.length);
 //                if ( counter==images.length ) {
 //                    director.imagesCache= images;
 //                    scene_loading.finishedLoading();
 //                }
 //            }
 //        );
         
 //        Caatja.addCanvas(canvas);
 //
 //        director.loop(60);
      
     }
 
 
 }
