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
 package es.eucm.eadventure.editor.control.controllers.character;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 import es.eucm.eadventure.common.data.chapter.elements.NPC;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.common.loader.Loader;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 import es.eucm.eadventure.editor.control.controllers.DataControl;
 import es.eucm.eadventure.editor.control.controllers.DataControlWithResources;
 import es.eucm.eadventure.editor.control.controllers.EditorImageLoader;
 import es.eucm.eadventure.editor.control.controllers.Searchable;
 import es.eucm.eadventure.editor.control.controllers.general.ActionsListDataControl;
 import es.eucm.eadventure.editor.control.controllers.general.ResourcesDataControl;
 import es.eucm.eadventure.editor.control.tools.general.assets.AddResourcesBlockTool;
 import es.eucm.eadventure.editor.control.tools.generic.ChangeBooleanValueTool;
 import es.eucm.eadventure.editor.control.tools.generic.ChangeStringValueTool;
 import es.eucm.eadventure.editor.data.support.VarFlagSummary;
 
 public class NPCDataControl extends DataControlWithResources {
 
     
     /**
      * Constant for the empty animation
      */
     private static final String EMPTY_ANIMATION = "assets/special/EmptyAnimation";
     
     /**
      * Contained NPC data.
      */
     private NPC npc;
 
     /**
      * Actions list controller.
      */
     private ActionsListDataControl actionsListDataControl;
 
     /**
      * Constructor
      * 
      * @param npc
      *            Contained NPC data
      */
     public NPCDataControl( NPC npc ) {
 
         this.npc = npc;
         this.resourcesList = npc.getResources( );
 
         selectedResources = 0;
 
         // Add a new resource if the list is empty
         if( resourcesList.size( ) == 0 )
             resourcesList.add( new Resources( ) );
 
         // Create the subcontrollers
         resourcesDataControlList = new ArrayList<ResourcesDataControl>( );
         for( Resources resources : resourcesList )
             resourcesDataControlList.add( new ResourcesDataControl( resources, Controller.NPC ) );
 
         actionsListDataControl = new ActionsListDataControl( npc.getActions( ), this );
     }
 
     /**
      * Returns the actions list controller.
      * 
      * @return Actions list controller
      */
     public ActionsListDataControl getActionsList( ) {
 
         return actionsListDataControl;
     }
 
     /**
      * Returns the path to the selected preview image.
      * 
      * @return Path to the image, null if not present
      */
     public String getPreviewImage( ) {
 
         String previewImagePath = getExistingPreviewImagePath( );
 
         // Add the extension of the frame
         if( previewImagePath != null && !previewImagePath.toLowerCase( ).endsWith( ".eaa" ) )
             previewImagePath += "_01.png";
         else if( previewImagePath != null ) {
             return Loader.loadAnimation( AssetsController.getInputStreamCreator( ), previewImagePath, new EditorImageLoader() ).getFrame( 0 ).getUri( );
         }
 
         return previewImagePath;
     }
 
     /**
      * Look for one image path. If there no one, return empty animation path
      * 
      */
     private String getExistingPreviewImagePath( ) {
 
         String path = null;
         for( ResourcesDataControl resource : resourcesDataControlList ) {
            if (resource !=null && resource.getAssetPath( "standright" )!=null&&!resource.getAssetPath( "standright" ).equals( EMPTY_ANIMATION))
                 path = resource.getAssetPath( "standright" );
             else 
                 path = resource.getAssetPath( "standleft" );
             if( path != null ) {
                 return path;
             }
 
             for( int i = 0; i < resource.getAssetCount( ); i++ ) {
                 path = resource.getAssetPath( resource.getAssetName( i ) );
                 if( path != null ) {
                     return path;
                 }
             }
         }
 
         return EMPTY_ANIMATION;
     }
 
     /**
      * Returns the id of the NPC.
      * 
      * @return NPC's id
      */
     public String getId( ) {
 
         return npc.getId( );
     }
 
     /**
      * Returns the documentation of the NPC.
      * 
      * @return NPC's documentation
      */
     public String getDocumentation( ) {
 
         return npc.getDocumentation( );
     }
 
     /**
      * Returns the text front color for the player strings.
      * 
      * @return Text front color
      */
     public Color getTextFrontColor( ) {
 
         return new Color( Integer.valueOf( npc.getTextFrontColor( ).substring( 1 ), 16 ).intValue( ) );
     }
 
     /**
      * Returns the text border color for the player strings.
      * 
      * @return Text front color
      */
     public Color getTextBorderColor( ) {
 
         return new Color( Integer.valueOf( npc.getTextBorderColor( ).substring( 1 ), 16 ).intValue( ) );
     }
 
     /**
      * Returns the name of the item.
      * 
      * @return Character's name
      */
     public String getName( ) {
 
         return npc.getName( );
     }
 
     /**
      * Returns the brief description of the character.
      * 
      * @return Character's description
      */
     public String getBriefDescription( ) {
 
         return npc.getDescription( );
     }
 
     /**
      * Returns the detailed description of the character.
      * 
      * @return Character's detailed description
      */
     public String getDetailedDescription( ) {
 
         return npc.getDetailedDescription( );
     }
 
     /**
      * Check if the engine must synthesizer all current npc conversation lines
      * 
      * @return if npc must synthesizer all his lines
      */
     public boolean isAlwaysSynthesizer( ) {
 
         return npc.isAlwaysSynthesizer( );
     }
 
     /**
      * Sets the text front color for the player strings.
      * 
      * @param textFrontColor
      *            New text front color
      */
     public void setTextFrontColor( Color textFrontColor ) {
 
         String red = Integer.toHexString( textFrontColor.getRed( ) );
         String green = Integer.toHexString( textFrontColor.getGreen( ) );
         String blue = Integer.toHexString( textFrontColor.getBlue( ) );
 
         if( red.length( ) == 1 )
             red = "0" + red;
         if( green.length( ) == 1 )
             green = "0" + green;
         if( blue.length( ) == 1 )
             blue = "0" + blue;
 
         controller.addTool( new ChangeStringValueTool( npc, "#" + red + green + blue, "getTextFrontColor", "setTextFrontColor" ) );
     }
 
     /**
      * Sets the text border color for the player strings.
      * 
      * @param textBorderColor
      *            New text border color
      */
     public void setTextBorderColor( Color textBorderColor ) {
 
         String red = Integer.toHexString( textBorderColor.getRed( ) );
         String green = Integer.toHexString( textBorderColor.getGreen( ) );
         String blue = Integer.toHexString( textBorderColor.getBlue( ) );
 
         if( red.length( ) == 1 )
             red = "0" + red;
         if( green.length( ) == 1 )
             green = "0" + green;
         if( blue.length( ) == 1 )
             blue = "0" + blue;
 
         controller.addTool( new ChangeStringValueTool( npc, "#" + red + green + blue, "getTextBorderColor", "setTextBorderColor" ) );
     }
 
     public void setBubbleBkgColor( Color bubbleBkgColor ) {
 
         String red = Integer.toHexString( bubbleBkgColor.getRed( ) );
         String green = Integer.toHexString( bubbleBkgColor.getGreen( ) );
         String blue = Integer.toHexString( bubbleBkgColor.getBlue( ) );
 
         if( red.length( ) == 1 )
             red = "0" + red;
         if( green.length( ) == 1 )
             green = "0" + green;
         if( blue.length( ) == 1 )
             blue = "0" + blue;
 
         controller.addTool( new ChangeStringValueTool( npc, "#" + red + green + blue, "getBubbleBkgColor", "setBubbleBkgColor" ) );
     }
 
     public void setBubbleBorderColor( Color bubbleBorderColor ) {
 
         String red = Integer.toHexString( bubbleBorderColor.getRed( ) );
         String green = Integer.toHexString( bubbleBorderColor.getGreen( ) );
         String blue = Integer.toHexString( bubbleBorderColor.getBlue( ) );
 
         if( red.length( ) == 1 )
             red = "0" + red;
         if( green.length( ) == 1 )
             green = "0" + green;
         if( blue.length( ) == 1 )
             blue = "0" + blue;
 
         controller.addTool( new ChangeStringValueTool( npc, "#" + red + green + blue, "getBubbleBorderColor", "setBubbleBorderColor" ) );
     }
 
     /**
      * Set the possibility to all conversation lines to be read by synthesizer
      * 
      * @param always
      *            Boolean value
      */
     public void setAlwaysSynthesizer( boolean always ) {
 
         controller.addTool( new ChangeBooleanValueTool( npc, always, "isAlwaysSynthesizer", "setAlwaysSynthesizer" ) );
     }
 
     /**
      * Sets the new voice for the character
      * 
      * @param voice
      *            a string with the valid voice
      */
     public void setVoice( String voice ) {
 
         controller.addTool( new ChangeStringValueTool( npc, voice, "getVoice", "setVoice" ) );
     }
 
     /**
      * Gets the voice associated to character
      * 
      * @return string representing character voice
      */
     public String getVoice( ) {
 
         return npc.getVoice( );
     }
 
     @Override
     public Object getContent( ) {
 
         return npc;
     }
 
     @Override
     public int[] getAddableElements( ) {
 
         //return new int[] { Controller.RESOURCES };
         return new int[] {};
     }
 
     @Override
     public boolean canAddElement( int type ) {
 
         // It can always add new resources
         //return type == Controller.RESOURCES;
         return false;
     }
 
     @Override
     public boolean canBeDeleted( ) {
 
         return true;
     }
 
     @Override
     public boolean canBeMoved( ) {
 
         return true;
     }
 
     @Override
     public boolean canBeRenamed( ) {
 
         return true;
     }
 
     @Override
     public boolean addElement( int type, String id ) {
 
         boolean elementAdded = false;
 
         if( type == Controller.RESOURCES ) {
             elementAdded = Controller.getInstance( ).addTool( new AddResourcesBlockTool( resourcesList, resourcesDataControlList, Controller.NPC, this ) );
         }
 
         return elementAdded;
     }
 
     @Override
     public boolean moveElementUp( DataControl dataControl ) {
 
         boolean elementMoved = false;
         int elementIndex = resourcesList.indexOf( dataControl.getContent( ) );
 
         if( elementIndex > 0 ) {
             resourcesList.add( elementIndex - 1, resourcesList.remove( elementIndex ) );
             resourcesDataControlList.add( elementIndex - 1, resourcesDataControlList.remove( elementIndex ) );
             //controller.dataModified( );
             elementMoved = true;
         }
 
         return elementMoved;
     }
 
     @Override
     public boolean moveElementDown( DataControl dataControl ) {
 
         boolean elementMoved = false;
         int elementIndex = resourcesList.indexOf( dataControl.getContent( ) );
 
         if( elementIndex < resourcesList.size( ) - 1 ) {
             resourcesList.add( elementIndex + 1, resourcesList.remove( elementIndex ) );
             resourcesDataControlList.add( elementIndex + 1, resourcesDataControlList.remove( elementIndex ) );
             //controller.dataModified( );
             elementMoved = true;
         }
 
         return elementMoved;
     }
 
     @Override
     public String renameElement( String name ) {
 
         boolean elementRenamed = false;
         String oldNPCId = npc.getId( );
         String references = String.valueOf( controller.countIdentifierReferences( oldNPCId ) );
 
         // Ask for confirmation
         if( name != null || controller.showStrictConfirmDialog( TC.get( "Operation.RenameNPCTitle" ), TC.get( "Operation.RenameElementWarning", new String[] { oldNPCId, references } ) ) ) {
 
             // Show a dialog asking for the new npc id
             String newNPCId = name;
             if( name == null )
                 newNPCId = controller.showInputDialog( TC.get( "Operation.RenameNPCTitle" ), TC.get( "Operation.RenameNPCMessage" ), oldNPCId );
 
             // If some value was typed and the identifiers are different
             if( newNPCId != null && !newNPCId.equals( oldNPCId ) && controller.isElementIdValid( newNPCId ) ) {
                 npc.setId( newNPCId );
                 controller.replaceIdentifierReferences( oldNPCId, newNPCId );
                 controller.getIdentifierSummary( ).deleteNPCId( oldNPCId );
                 controller.getIdentifierSummary( ).addNPCId( newNPCId );
                 //controller.dataModified( );
                 elementRenamed = true;
             }
         }
 
         if( elementRenamed )
             return oldNPCId;
         else
             return null;
     }
 
     @Override
     public void updateVarFlagSummary( VarFlagSummary varFlagSummary ) {
 
         actionsListDataControl.updateVarFlagSummary( varFlagSummary );
         // Iterate through the resources
         for( ResourcesDataControl resourcesDataControl : resourcesDataControlList )
             resourcesDataControl.updateVarFlagSummary( varFlagSummary );
 
     }
 
     @Override
     public boolean isValid( String currentPath, List<String> incidences ) {
 
         boolean valid = true;
 
         // Iterate through the resources
         for( int i = 0; i < resourcesDataControlList.size( ); i++ ) {
             String resourcesPath = currentPath + " >> " + TC.getElement( Controller.RESOURCES ) + " #" + ( i + 1 );
             valid &= resourcesDataControlList.get( i ).isValid( resourcesPath, incidences );
         }
 
         // Spread the call to the actions
         valid &= actionsListDataControl.isValid( currentPath, incidences );
 
         return valid;
     }
 
     @Override
     public int countAssetReferences( String assetPath ) {
 
         int count = 0;
 
         // Iterate through the resources
         for( ResourcesDataControl resourcesDataControl : resourcesDataControlList )
             count += resourcesDataControl.countAssetReferences( assetPath );
 
         // Add the references in the actions
         count += actionsListDataControl.countAssetReferences( assetPath );
 
         return count;
     }
 
     @Override
     public void getAssetReferences( List<String> assetPaths, List<Integer> assetTypes ) {
 
         // Iterate through the resources
         for( ResourcesDataControl resourcesDataControl : resourcesDataControlList )
             resourcesDataControl.getAssetReferences( assetPaths, assetTypes );
 
         // Add the references in the actions
         actionsListDataControl.getAssetReferences( assetPaths, assetTypes );
     }
 
     @Override
     public void deleteAssetReferences( String assetPath ) {
 
         // Iterate through the resources
         for( ResourcesDataControl resourcesDataControl : resourcesDataControlList )
             resourcesDataControl.deleteAssetReferences( assetPath );
 
         // Delete the references from the actions
         actionsListDataControl.deleteAssetReferences( assetPath );
     }
 
     @Override
     public int countIdentifierReferences( String id ) {
 
         int count = 0;
         // Iterate through the resources
         for( ResourcesDataControl resourcesDataControl : resourcesDataControlList )
             resourcesDataControl.countIdentifierReferences( id );
 
         count += actionsListDataControl.countIdentifierReferences( id );
         return count;
     }
 
     @Override
     public void replaceIdentifierReferences( String oldId, String newId ) {
 
         // Iterate through the resources
         for( ResourcesDataControl resourcesDataControl : resourcesDataControlList )
             resourcesDataControl.replaceIdentifierReferences( oldId, newId );
 
         actionsListDataControl.replaceIdentifierReferences( oldId, newId );
     }
 
     @Override
     public void deleteIdentifierReferences( String id ) {
 
         // Iterate through the resources
         for( ResourcesDataControl resourcesDataControl : resourcesDataControlList )
             resourcesDataControl.deleteIdentifierReferences( id );
 
         actionsListDataControl.deleteIdentifierReferences( id );
     }
 
     public boolean buildResourcesTab( ) {
 
         return true;
     }
 
     @Override
     public boolean canBeDuplicated( ) {
 
         return true;
     }
 
     @Override
     public void recursiveSearch( ) {
 
         check( this.getBriefDescription( ), TC.get( "Search.BriefDescription" ) );
         check( this.getDetailedDescription( ), TC.get( "Search.DetailedDescription" ) );
         check( this.getDocumentation( ), TC.get( "Search.Documentation" ) );
         check( this.getId( ), "ID" );
         check( this.getName( ), TC.get( "Search.Name" ) );
         check( this.getVoice( ), TC.get( "Search.NPCVoice" ) );
         check( this.getPreviewImage( ), TC.get( "Search.PreviewImage" ) );
         getActionsList( ).recursiveSearch( );
     }
 
     public String getAnimationPath( String animation ) {
 
         return resourcesDataControlList.get( selectedResources ).getAssetPath( animation );
     }
 
     public Color getBubbleBorderColor( ) {
 
         return new Color( Integer.valueOf( npc.getBubbleBorderColor( ).substring( 1 ), 16 ).intValue( ) );
     }
 
     public Color getBubbleBkgColor( ) {
 
         return new Color( Integer.valueOf( npc.getBubbleBkgColor( ).substring( 1 ), 16 ).intValue( ) );
     }
 
     public Boolean getShowsSpeechBubbles( ) {
 
         return npc.getShowsSpeechBubbles( );
     }
 
     public void setShowsSpeechBubbles( Boolean showsSpeechBubbles ) {
 
         controller.addTool( new ChangeBooleanValueTool( npc, showsSpeechBubbles, "getShowsSpeechBubbles", "setShowsSpeechBubbles" ) );
     }
 
     @Override
     public List<Searchable> getPathToDataControl( Searchable dataControl ) {
 
         List<Searchable> path = getPathFromChild( dataControl, resourcesDataControlList );
         if( path != null )
             return path;
         return getPathFromChild( dataControl, actionsListDataControl );
     }
 
 }
