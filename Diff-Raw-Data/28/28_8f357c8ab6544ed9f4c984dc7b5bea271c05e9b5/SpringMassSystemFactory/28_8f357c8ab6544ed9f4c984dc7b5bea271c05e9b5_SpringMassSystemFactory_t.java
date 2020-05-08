 /**
  * Copyright 2012 Tobias Gierke <tobias.gierke@code-sourcery.de>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.codesourcery.springmass.springmass;
 
 import java.awt.Color;
 
 import de.codesourcery.springmass.math.Vector4;
 
 public class SpringMassSystemFactory {
 
 	private Spring createSpring(Mass m1,Mass m2,double restLength , boolean doRender,Color color,SimulationParameters parameters) 
 	{
 		return new Spring(m1, m2, restLength, doRender, color, parameters.getSpringCoefficient(), parameters.getSpringDampening() );
 	}
 	
 	private Spring createSpring(Mass m1,Mass m2,double restLength , boolean doRender,SimulationParameters parameters) 
 	{
 		return createSpring(m1, m2, restLength, doRender, Color.green , parameters);
 	}	
 	
 	public SpringMassSystem create(SimulationParameters parameters) 
 	{
 		System.out.println("Point masses: "+(parameters.getGridRowCount()*parameters.getGridColumnCount()));
 		
 		final Mass[][] masses = new Mass[parameters.getGridColumnCount()][];
 		for ( int i = 0 ; i < parameters.getGridColumnCount() ; i++ ) {
 			masses[i] = new Mass[parameters.getGridRowCount()];
 		}
 		
 		int springCount = 0;
 
 		double scaleX = parameters.getXResolution() / (parameters.getGridColumnCount()+parameters.getGridColumnCount()*0.5);
 		double scaleY = parameters.getYResolution() / (parameters.getGridRowCount()+parameters.getGridRowCount()*0.5);
 		
		final double xOffset = parameters.getXResolution()*0.2;
 		final double yOffset = scaleY;
 		
 		for ( int x = 0 ; x < parameters.getGridColumnCount() ; x++ ) 
 		{
 			for ( int y = 0 ; y < parameters.getGridRowCount() ; y++ ) 
 			{
 				final Vector4 pos = new Vector4( xOffset + scaleX*x , yOffset + scaleY*y,0);
 				final Mass m = new Mass( Color.red  , pos , parameters.getParticleMass() );
 				if ( y == 0 ) {
 					m.setFixed( true );
 				}
 				masses[x][y] = m;
 			}
 		}
 		
 		final SpringMassSystem system = new SpringMassSystem(parameters,masses);
 
 		// connect masses horizontally
 		final double horizRestLength = scaleX*parameters.getHorizontalRestLengthFactor();
 		for ( int y = 0 ; y < parameters.getGridRowCount() ; y++ ) 
 		{
 			for ( int x = 0 ; x < (parameters.getGridColumnCount()-1) ; x++ ) 
 			{
 				system.addSpring( createSpring( masses[x][y] , masses[x+1][y] , horizRestLength , true , parameters ) );
 				springCount++;
 			}
 		}
 
 		// connect masses vertically
 		final double verticalRestLength = scaleY*parameters.getVerticalRestLengthFactor();
 		for ( int x = 0 ; x < parameters.getGridColumnCount() ; x++ ) 
 		{
 			for ( int y = 0 ; y < (parameters.getGridRowCount()-1) ; y++ ) 
 			{
 				system.addSpring( createSpring( masses[x][y] , masses[x][y+1] , verticalRestLength , true , parameters ) );
 				springCount++;
 			}
 		}	
 
 		// cross-connect masses
 		final double crossConnectRestLength = Math.sqrt( horizRestLength*horizRestLength + verticalRestLength*verticalRestLength);
 		for ( int x = 0 ; x < (parameters.getGridColumnCount()-1) ; x++ ) 
 		{
 			for ( int y = 0 ; y < (parameters.getGridRowCount()-1) ; y++ ) 
 			{
 				system.addSpring( createSpring( masses[x][y] , masses[x+1][y+1] , crossConnectRestLength , parameters.isRenderAllSprings() , Color.YELLOW , parameters ) );
 				system.addSpring( createSpring( masses[x][y+1] , masses[x+1][y] , crossConnectRestLength , parameters.isRenderAllSprings() , Color.YELLOW , parameters ) );
 				springCount+=2;
 			}
 		}	
 
 		// connect cloth outline
 		final double horizOutlineRestLength = 2 * horizRestLength;
 		for ( int y = 0 ; y < parameters.getGridRowCount() ; y++ ) {
 			for ( int x = 0 ; x < (parameters.getGridColumnCount()-2) ; x++ ) 
 			{
 				system.addSpring( createSpring( masses[x][y] , masses[x+2][y] , horizOutlineRestLength , parameters.isRenderAllSprings(), Color.BLUE , parameters ) );
 				springCount++;
 			}	
 		}
 
 		final double verticalOutlineRestLength = 2 * verticalRestLength;
 		for ( int x = 0 ; x < parameters.getGridColumnCount() ; x++ ) 
 		{ 
 			for ( int y = 0 ; y < (parameters.getGridRowCount()-2) ; y++ ) 
 			{
 				system.addSpring( createSpring( masses[x][y] , masses[x][y+2] , verticalOutlineRestLength , parameters.isRenderAllSprings(), Color.BLUE , parameters ) );
 				springCount++;				
 			}		
 		}
 		System.out.println("Springs: "+springCount);
 		return system;
 	}
 }
