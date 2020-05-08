 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *          research group.
  *   
  *    Copyright 2005-2012 <e-UCM> research group.
  *  
  *     <e-UCM> is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://e-adventure.e-ucm.es> or
  *          <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  * This file is part of <e-Adventure>, version 1.4.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       <e-Adventure> is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      <e-Adventure> is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.editor.gui.elementpanels.scene;
 
 import java.awt.Dimension;
 
 import javax.swing.BorderFactory;
 
 import es.eucm.eadventure.common.data.chapter.Trajectory;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 import es.eucm.eadventure.editor.control.controllers.scene.ElementReferenceDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.NodeDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.SceneDataControl;
 import es.eucm.eadventure.editor.gui.elementpanels.general.LooksPanel;
 import es.eucm.eadventure.editor.gui.otherpanels.ScenePreviewEditionPanel;
 
 public class SceneLooksPanel extends LooksPanel {
 
     private SceneDataControl sceneDataControl;
 
     /**
      * Panel for the preview.
      */
     private ScenePreviewEditionPanel scenePreviewEditionPanel;
 
     public SceneLooksPanel( SceneDataControl control ) {
 
         super( control );
         // TODO Parche, arreglar
         lookPanel.setPreferredSize( new Dimension( 0, 600 ) );
 
     }
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     @Override
     protected void createPreview( ) {
 
         this.sceneDataControl = (SceneDataControl) this.dataControl;
 
         // Take the path of the background
         String scenePath = sceneDataControl.getPreviewBackground( );
 
         scenePreviewEditionPanel = new ScenePreviewEditionPanel( false, scenePath );
         scenePreviewEditionPanel.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "Scene.Preview" ) ) );
 
         // Add the item references first
         for( ElementReferenceDataControl elementReference : sceneDataControl.getReferencesList( ).getItemReferences( ) ) {
             scenePreviewEditionPanel.addElement( ScenePreviewEditionPanel.CATEGORY_OBJECT, elementReference );
         }
 
         // Add then the character references
         for( ElementReferenceDataControl elementReference : sceneDataControl.getReferencesList( ).getNPCReferences( ) ) {
             scenePreviewEditionPanel.addElement( ScenePreviewEditionPanel.CATEGORY_CHARACTER, elementReference );
         }
 
         // Add the atrezzo item references first
         for( ElementReferenceDataControl elementReference : sceneDataControl.getReferencesList( ).getAtrezzoReferences( ) ) {
             scenePreviewEditionPanel.addElement( ScenePreviewEditionPanel.CATEGORY_ATREZZO, elementReference );
         }
        if( !Controller.getInstance( ).isPlayTransparent( ) && sceneDataControl.getTrajectory( ).hasTrajectory( ) ) {
             scenePreviewEditionPanel.setTrajectory( (Trajectory) sceneDataControl.getTrajectory( ).getContent( ) );
             for( NodeDataControl nodeDataControl : sceneDataControl.getTrajectory( ).getNodes( ) )
                 scenePreviewEditionPanel.addNode( nodeDataControl );
             scenePreviewEditionPanel.setShowInfluenceArea( true );
         }
         else
 
         // Deleted the checking if player has layer
         if( !Controller.getInstance( ).isPlayTransparent( ) /*&& sceneDataControl.isAllowPlayer()*/)
             //scenePreviewEditionPanel.addPlayer(sceneDataControl, sceneDataControl.getReferencesList().getPlayerImage());
             scenePreviewEditionPanel.addPlayer( sceneDataControl, AssetsController.getImage( Controller.getInstance( ).getPlayerImagePath( ) ) );
 
         scenePreviewEditionPanel.setMovableCategory( ScenePreviewEditionPanel.CATEGORY_OBJECT, false );
         scenePreviewEditionPanel.setMovableCategory( ScenePreviewEditionPanel.CATEGORY_CHARACTER, false );
         scenePreviewEditionPanel.setMovableCategory( ScenePreviewEditionPanel.CATEGORY_ATREZZO, false );
         scenePreviewEditionPanel.setMovableCategory( ScenePreviewEditionPanel.CATEGORY_PLAYER, false );
         lookPanel.add( scenePreviewEditionPanel, cLook );
         //resourcesPanel.setPreviewUpdater( this );
 
     }
 
     public void addPlayer( ) {
 
         // Deleted the checking if player has layer
         if( !Controller.getInstance( ).isPlayTransparent( )/* && sceneDataControl.isAllowPlayer()*/)
             scenePreviewEditionPanel.addPlayer( sceneDataControl, sceneDataControl.getReferencesList( ).getPlayerImage( ) );
         scenePreviewEditionPanel.repaint( );
     }
 
     @Override
     public void updatePreview( ) {
 
         if( scenePreviewEditionPanel != null ) {
             System.out.println( sceneDataControl.getPreviewBackground( ) );
             scenePreviewEditionPanel.loadBackground( sceneDataControl.getPreviewBackground( ) );
             System.out.println( "BG UPDATED!!" );
         }
         if( getParent( ) != null && getParent( ).getParent( ) != null )
             getParent( ).getParent( ).repaint( );
     }
 
     @Override
     public void updateResources( ) {
 
         super.updateResources( );
         if( getParent( ) != null && getParent( ).getParent( ) != null )
             getParent( ).getParent( ).repaint( );
     }
 
     public SceneDataControl getSceneDataControl( ) {
 
         return sceneDataControl;
     }
 
 }
