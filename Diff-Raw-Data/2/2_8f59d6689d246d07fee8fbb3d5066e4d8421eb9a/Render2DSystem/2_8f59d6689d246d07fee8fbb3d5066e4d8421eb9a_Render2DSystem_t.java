 /**
  * 
  */
 package com.hellocld.AGED.basicSystems;
 
 import java.util.Iterator;
 import java.util.Set;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import com.hellocld.AGED.basicComponents.*;
 import com.hellocld.AGED.basicComponents.Render;
 import com.hellocld.AGED.core.EntityManager;
 import com.hellocld.AGED.core.ASystem;
 
 
 /**
 * A very basic 2D Rendering System
  * @author CLD
  *
  */
 public class Render2DSystem implements ASystem {
 
 	/* (non-Javadoc)
 	 * @see com.hellocld.AGED.core.System#execute()
 	 */
 	@Override
 	public void execute(EntityManager em) {
 		//clear all buffers
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		
 		//get all the renderable Entities
 		Set<Integer> renderSet = em.getAllEntitiesPossessingComponent(Render.class);
 		//make an iterator from the set; faster than a standard for() loop
 		Iterator<Integer> renderInter = renderSet.iterator();
 		
 		//loop through all the renderable entities
 		while(renderInter.hasNext()) {
 			//double check just to make sure rendering is turned on; if it isn't, the entity is skipped
 			if(em.getComponent(renderInter.next(), Render.class).on) {
 				//gather all the necessary info from the components for rendering
 				float x = em.getComponent(renderInter.next(), Position2D.class).x;
 				float y = em.getComponent(renderInter.next(), Position2D.class).y;
 				float w = em.getComponent(renderInter.next(), Size2D.class).width;
 				float h = em.getComponent(renderInter.next(), Size2D.class).height;
 				
 				//set the color of the quad
 				glColor3f(1.0f, 1.0f, 1.0f);
 				
 				//draw the quad!
 				glBegin(GL_QUADS);
 					glVertex2f(x,y);
 					glVertex2f(x+w, y);
 					glVertex2f(x+w, y+h);
 					glVertex2f(x, y+h);
 				glEnd();
 			}
 		}
 	}
 
 }
