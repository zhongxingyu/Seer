 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package es.eucm.eadventure.engine.core.control.functionaldata.functionaleffects;
 
 import java.util.Random;
 
 import es.eucm.eadventure.common.data.chapter.effects.RandomEffect;
 
 /**
  * Functional class for RandomEffect
  * 
  * @author Javier
  */
 public class FunctionalRandomEffect extends FunctionalEffect {
 
     private static final Random r = new Random( );
 
     /**
      * Functional positive effect
      */
     private FunctionalEffect positiveEffect;
 
     /**
      * Functional negative effect
      */
     private FunctionalEffect negativeEffect;
 
     private boolean positive;
 
     /**
      * Effect to be triggered: Points to positiveEffect or negativeEffect
      */
     private FunctionalEffect effectTriggered;
 
     public FunctionalRandomEffect( RandomEffect effect ) {
 
         super( effect );
         this.positiveEffect = FunctionalEffect.buildFunctionalEffect( effect.getPositiveEffect( ) );
         this.negativeEffect = FunctionalEffect.buildFunctionalEffect( effect.getNegativeEffect( ) );
     }
 
     private FunctionalEffect getEffectToBeTriggered( ) {
 
         int number = r.nextInt( 100 );
         positive = number < ( (RandomEffect) effect ).getProbability( );
         if( positive ) {
             //System.out.println(number+" < "+probability+" : Triggering positive effect" );
         }
         else {
             //System.out.println(number+" >= "+probability+" : Triggering negative effect" );
         }
 
         if( positive )
             return positiveEffect;
         return negativeEffect;
     }
 
     @Override
     public boolean isInstantaneous( ) {
 
         if( effectTriggered != null )
             return effectTriggered.isInstantaneous( );
         return false;
     }
 
     @Override
     public boolean isStillRunning( ) {
 
         if( effectTriggered != null )
             return effectTriggered.isStillRunning( );
         return false;
     }
 
     @Override
     public void triggerEffect( ) {
 
         effectTriggered = getEffectToBeTriggered( );
         if( effectTriggered != null ) {
             if( effectTriggered.isAllConditionsOK( ) )
                 effectTriggered.triggerEffect( );
         }
     }
    @Override
    public FunctionalEffect getTriggerEffect(){
        
      return  effectTriggered;
    }

    
 
 }
