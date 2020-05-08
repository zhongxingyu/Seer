 package com.lbi.internetweek.view.components;
 
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 import processing.core.PConstants;
 import processing.core.PImage;
 import processing.core.PVector;
 import toxi.geom.Rect;
 import toxi.geom.Vec2D;
 import toxi.physics2d.VerletPhysics2D;
 import toxi.physics2d.behaviors.GravityBehavior;
 
 import com.lbi.internetweek.ApplicationFacade;
 import com.lbi.internetweek.Installation;
 import com.lbi.internetweek.view.PoofMediator;
 
 public class PoofView
 {
 	private PoofMediator			_mediator;
 	private Installation			_pa;
 
 	private int						_poofTotalFrames	=	4;
 	private PImage[]				_poofFrames			=	new PImage[_poofTotalFrames];
 	private PImage					_poofSprite;
 	
 	private PImage					_featherSprite;
 	
 	private ArrayList<Poof>			_poofList			=	new ArrayList<Poof>();
 	
 	private boolean					_canShowPoof;
 	
 	public PoofView(PoofMediator poofMediator)
 	{
 		_pa			=	ApplicationFacade.app;
 		_mediator 	=	poofMediator;
 		
 		setupImages();
 	}
 
 	private void setupImages()
 	{
 		_poofSprite			=	_pa.loadImage("poof_frames.png");
 		
 		int rows = _poofTotalFrames, 
 			bw = 128, bh = 128;
 
 		//flying frames
 		for (int i = 0; i < rows; i++)
 		{
 			_poofFrames[ i ] = _poofSprite.get(
 					i * bw,
 					0,
 					bw,
 					bh
 			);
 		}
 		
 		_featherSprite		=	_pa.loadImage("feather.png");
 	}
 
 	public void draw()
 	{
 		if( _poofList.size() > 0 )
 		{
 			for( int i = 0; i < _poofList.size(); ++i )
 			{
 				if( _poofList.get(i).hasLife )
 				{
 					_poofList.get(i)
 						.draw();
 				}
 				else
 				{
					Poof p = _poofList.remove(i);
					p.kill();
					p = null;
 				}				
 			}
 		}
 	}
 	
 	public void showPoof(PVector p)
 	{
 		//PApplet.println( "\tShow Poof at " + p );
 		
 		_poofList.add( new Poof(_poofFrames, _featherSprite, new PVector(p.x,p.y) ) );		
 	}
 
 }
