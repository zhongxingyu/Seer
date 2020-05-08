 package com.deev.interaction.touch;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 
 import javax.swing.JComponent;
 
 import eu.telecom_bretagne.uav3i.util.log.LoggerUtil;
 
 public class Animator
 {	
 	private static int _DELAY_ = 50;//en milliseconde = temps de rafraichissement de l'appli
 	public final static List<Animation> _animations = new ArrayList<Animation>();
 	public final static List<JComponent> _components = new ArrayList<JComponent>();
 
 	public Animator()
 	{
 
 	}
 	
 	public static void go() 
 	{
 		TimerTask task = new TimerTask()
 		{
 			public void run()
 			{
 				synchronized (_animations)
 				{					
 				    Iterator<Animation> itra = _animations.iterator();
 					while (itra.hasNext())
 						itra.next().tick(_DELAY_);
 				}
 
 				synchronized (_components)
 				{					
 				    Iterator<JComponent> itrc = _components.iterator();
 					while (itrc.hasNext())
 						itrc.next().repaint();
 				}
 			}
 		};
 		Timer timer = new Timer();
 		timer.schedule(task, 0, _DELAY_);
 		
 		// On regarde de temps en temps s'il y a une vieille Animation (life < 0) 
 		// Si oui, on la dégage
 		TimerTask cleanTask = new TimerTask()
 		{
 			public void run()
 			{
 				synchronized (_animations)
 				{
 				    Iterator<Animation> itr = _animations.iterator();
 					Animation anim = null;
 					Animation dead = null;
 					while (itr.hasNext())
 					{
 						anim = itr.next();
 						if (anim.life() < 0)
 						{
 							dead = anim;
 							break;
 						}
 					}
 					
 					if (dead != null)
 					{
 						_animations.remove(dead);		
 					}
 					
 				}
 			}
 		};
 		Timer cleanTimer = new Timer();
 		cleanTimer.schedule(cleanTask, 0, 10*_DELAY_);
 	}
 	
 
 
 	public static void addAnimation(Animation anim)
 	{
		LoggerUtil.LOG.log(Level.INFO, "Adding animation "+anim.getClass().toString());
 		synchronized(_animations)
 		{
 			if (!_animations.contains(anim))
 				_animations.add(anim);
 		}
 	}
 	
 	public static void removeAnimation(Animation anim)
 	{
 		synchronized (_animations)
 		{					
 			_animations.remove(anim);
 		}
 	}
 	
 	public static void addComponent(JComponent comp)
 	{
 		synchronized(_components)
 		{
 			if (!_components.contains(comp))
 				_components.add(comp);
 		}
 	}
 	
 	public static void removeComponent(JComponent comp)
 	{
 		synchronized(_components)
 		{
 			if (!_components.contains(comp))
 				_components.remove(comp);
 		}
 	}
 }
