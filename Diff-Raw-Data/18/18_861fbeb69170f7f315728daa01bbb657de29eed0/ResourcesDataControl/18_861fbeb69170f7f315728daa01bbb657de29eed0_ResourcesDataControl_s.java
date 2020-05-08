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
 package es.eucm.eadventure.editor.control.controllers.general;
 
 import java.awt.Image;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import es.eucm.eadventure.common.auxiliar.AssetsConstants;
 import es.eucm.eadventure.common.data.animation.Animation;
 import es.eucm.eadventure.common.data.animation.Frame;
 import es.eucm.eadventure.common.data.animation.Transition;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 import es.eucm.eadventure.editor.control.controllers.ConditionsController;
 import es.eucm.eadventure.editor.control.controllers.DataControl;
 import es.eucm.eadventure.editor.control.controllers.Searchable;
 import es.eucm.eadventure.editor.control.tools.general.assets.DeleteAssetReferencesInResources;
 import es.eucm.eadventure.editor.control.tools.general.assets.DeleteResourceTool;
 import es.eucm.eadventure.editor.control.tools.general.assets.EditResourceTool;
 import es.eucm.eadventure.editor.control.tools.general.assets.SelectResourceTool;
 import es.eucm.eadventure.editor.data.AssetInformation;
 import es.eucm.eadventure.editor.data.support.VarFlagSummary;
 
 /**
  * Microcontroller for the resources data.
  * 
  * @author Javier Torrente
  */
 public class ResourcesDataControl extends DataControl {
 
     /**
      * Contained resources.
      */
     private Resources resources;
 
     /**
      * The assets information of the resources.
      */
     private AssetInformation[] assetsInformation;
 
     private int[][] assetsGroups = null;
 
     private String[] groupsInfo = null;
 
     /**
      * Conditions controller.
      */
     private ConditionsController conditionsController;
 
     private int resourcesType;
 
     private Map<String, String> imageIconMap;
 
     /**
      * Contructor.
      * 
      * @param resources
      *            Resources of the data control structure
      * @param resourcesType
      *            Type of the resources
      */
     public ResourcesDataControl( Resources resources, int resourcesType ) {
 
         this.resources = resources;
         this.resourcesType = resourcesType;
 
         // Initialize the assetsInformation, depending on the assets type
         switch( resourcesType ) {
             case Controller.SCENE:
                 assetsInformation = new AssetInformation[] { new AssetInformation("Resources.DescriptionSceneBackground" , "background", true, AssetsConstants.CATEGORY_BACKGROUND, AssetsController.FILTER_JPG ), new AssetInformation( "Resources.DescriptionSceneForeground" , "foreground", false, AssetsConstants.CATEGORY_BACKGROUND, AssetsController.FILTER_PNG ), /*new AssetInformation( TextConstants.getText( "Resources.DescriptionSceneHardMap" ), "hardmap", false, AssetsController.CATEGORY_BACKGROUND, AssetsController.FILTER_PNG ), */new AssetInformation( "Resources.DescriptionSceneMusic" , "bgmusic", false, AssetsConstants.CATEGORY_AUDIO, AssetsController.FILTER_NONE ) };
                 break;
             case Controller.CUTSCENE_SLIDES:
                 assetsInformation = new AssetInformation[] { new AssetInformation(  "Resources.DescriptionSlidesceneSlides" , "slides", true, AssetsConstants.CATEGORY_ANIMATION, AssetsController.FILTER_JPG ), new AssetInformation( "Resources.DescriptionSceneMusic" , "bgmusic", false, AssetsConstants.CATEGORY_AUDIO, AssetsController.FILTER_NONE ) };
                 break;
             case Controller.ACTION_CUSTOM:
             case Controller.ACTION_CUSTOM_INTERACT:
                assetsInformation = new AssetInformation[] { new AssetInformation(  "Resources.DescriptionButtonNormal" , "buttonNormal", true, AssetsConstants.CATEGORY_BUTTON, AssetsController.FILTER_PNG ), new AssetInformation(  "Resources.DescriptionButtonOver" , "buttonOver", true, AssetsConstants.CATEGORY_BUTTON, AssetsController.FILTER_PNG ), new AssetInformation( "Resources.DescriptionButtonPressed" , "buttonPressed", true, AssetsConstants.CATEGORY_BUTTON, AssetsController.FILTER_PNG ), new AssetInformation(  "Resources.DescriptionActionAnimation" , "actionAnimation", false, AssetsConstants.CATEGORY_ANIMATION, AssetsController.FILTER_NONE ), new AssetInformation( "Resources.DescriptionActionAnimationLeft" , "actionAnimationLeft", false, AssetsConstants.CATEGORY_ANIMATION, AssetsController.FILTER_NONE ) };
                assetsGroups = new int[][] { { 0, 1, 2 }, { 3, 4 } };
                 groupsInfo = new String[] { "Resources.Button" , "Resources.Animations"  };
                 break;
             case Controller.CUTSCENE_VIDEO:
                 assetsInformation = new AssetInformation[] { new AssetInformation(  "Resources.DescriptionVideoscenes" , "video", true, AssetsConstants.CATEGORY_VIDEO, AssetsController.FILTER_NONE ) };
                 break;
             case Controller.BOOK:
                 assetsInformation = new AssetInformation[] { new AssetInformation(  "Resources.DescriptionBookBackground" , "background", true, AssetsConstants.CATEGORY_BACKGROUND, AssetsController.FILTER_JPG ), 
                         new AssetInformation( "Resources.ArrowLeftNormal", "arrowLeftNormal", false, AssetsConstants.CATEGORY_ARROW_BOOK, AssetsController.FILTER_PNG),
                         new AssetInformation( "Resources.ArrowRightNormal", "arrowRightNormal", false, AssetsConstants.CATEGORY_ARROW_BOOK, AssetsController.FILTER_PNG),
                         new AssetInformation( "Resources.ArrowLeftOver", "arrowLeftOver", false, AssetsConstants.CATEGORY_ARROW_BOOK, AssetsController.FILTER_PNG),
                         new AssetInformation( "Resources.ArrowRightOver", "arrowRightOver", false, AssetsConstants.CATEGORY_ARROW_BOOK, AssetsController.FILTER_PNG) };
                 break;
             case Controller.ITEM:
                 assetsInformation = new AssetInformation[] { new AssetInformation(  "Resources.DescriptionItemImage" , "image", false, AssetsConstants.CATEGORY_IMAGE, AssetsController.FILTER_NONE ), new AssetInformation( "Resources.DescriptionItemIcon" , "icon", false, AssetsConstants.CATEGORY_ICON, AssetsController.FILTER_NONE ) };
                 imageIconMap = new HashMap<String, String>( );
                 imageIconMap.put( "icon", "image" );
                 break;
             case Controller.PLAYER:
             case Controller.NPC:
                 assetsInformation = new AssetInformation[] { new AssetInformation( "Resources.DescriptionCharacterAnimationStandUp" , "standup", false, AssetsConstants.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationStandDown" , "standdown", false, AssetsConstants.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationStandRight" , "standright", false, AssetsConstants.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationStandLeft" , "standleft", false, AssetsConstants.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationSpeakUp", "speakup", false, AssetsConstants.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationSpeakDown" , "speakdown", false, AssetsConstants.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationSpeakRight", "speakright", false, AssetsConstants.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationSpeakLeft" , "speakleft", false, AssetsConstants.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationUseRight" , "useright", false, AssetsController.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationUseLeft", "useleft", false, AssetsController.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationWalkUp", "walkup", false, AssetsController.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationWalkDown" , "walkdown", false, AssetsController.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationWalkRight" , "walkright", false, AssetsController.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ), 
                         new AssetInformation( "Resources.DescriptionCharacterAnimationWalkLeft" , "walkleft", false, AssetsController.CATEGORY_ANIMATION, AssetsController.FILTER_PNG ) };
                 assetsGroups = new int[][] { { 0, 1, 2, 3 }, { 4, 5, 6, 7 }, { 8, 9 }, { 10, 11, 12, 13 } };
                 groupsInfo = new String[] { "Resources.StandingAnimations" , "Resources.SpeakingAnimations" , "Resources.UsingAnimations" , "Resources.WalkingAnimations" };
                 break;
             case Controller.ATREZZO:
                 assetsInformation = new AssetInformation[] { new AssetInformation( "Resources.DescriptionItemImage" , "image", false, AssetsController.CATEGORY_IMAGE, AssetsController.FILTER_NONE ) };
                 break;
         }
 
         // Create subcontrollers
         conditionsController = new ConditionsController( resources.getConditions( ), Controller.RESOURCES, TC.getElement( resourcesType ) );
     }
 
     public int getResourcesType( ) {
 
         return resourcesType;
     }
 
     /**
      * Returns the conditions microcontroller.
      * 
      * @return Conditions microcontroller
      */
     public ConditionsController getConditions( ) {
 
         return conditionsController;
     }
 
     /**
      * Returns the number of assets that the resources block has.
      * 
      * @return Number of assets
      */
     public int getAssetCount( ) {
 
         return assetsInformation.length;
     }
 
     /**
      * Returns the name of the asset in the given position.
      * 
      * @param index
      *            Index of the asset
      * @return Name of the asset
      */
     public String getAssetName( int index ) {
         return assetsInformation[index].name;
     }
 
     /**
      * Returns the description of the asset in the given position.
      * 
      * @param index
      *            Index of the asset
      * @return Description of the asset
      */
     public String getAssetDescription( int index ) {
         return TC.get(assetsInformation[index].description);
         //return assetsInformation[index].description;
     }
 
     /**
      * Returns the category of the asset in the given position.
      * 
      * @param index
      *            Index of the asset
      * @return Category of the asset
      */
     public int getAssetCategory( int index ) {
 
         return assetsInformation[index].category;
     }
 
     /**
      * Returns the filter of the asset in the given position.
      * 
      * @param index
      *            Index of the asset
      * @return Filter of the asset
      */
     public int getAssetFilter( int index ) {
 
         return assetsInformation[index].filter;
     }
 
     /**
      * Returns the relative path of the given asset.
      * 
      * @param asset
      *            Name of the asset
      * @return The path to the resource if present, null otherwise
      */
     public String getAssetPath( String asset ) {
 
         return resources.getAssetPath( asset );
     }
 
     /**
      * Returns the relative path of the given asset (used for display).
      * 
      * @param index
      *            Index of the asset
      * @return The path to the resource if present, null otherwise
      */
     public String getAssetPath( int index ) {
 
         return resources.getAssetPath( assetsInformation[index].name );
     }
 
     /**
      * Shows a dialog to choose a new path for the given asset.
      * 
      * @param index
      *            Index of the asset
      * @throws CloneNotSupportedException
      */
     public void editAssetPath( int index ) {
 
         try {
             controller.addTool( new SelectResourceTool( resources, assetsInformation, resourcesType, index ) );
         }
         catch( CloneNotSupportedException e ) {
             e.printStackTrace( );
         }
 
     }
 
     /**
      * Deletes the path of the given asset.
      * 
      * @param index
      *            Index of the asset
      */
     public void deleteAssetPath( int index ) {
 
         try {
             controller.addTool( new DeleteResourceTool( resources, assetsInformation, index ) );
         }
         catch( CloneNotSupportedException e ) {
             e.printStackTrace( );
         }
     }
 
     @Override
     public Object getContent( ) {
 
         return resources;
     }
 
     @Override
     public int[] getAddableElements( ) {
 
         return new int[] {};
     }
 
     @Override
     public boolean canAddElement( int type ) {
 
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
 
         return false;
     }
 
     @Override
     public boolean deleteElement( DataControl dataControl, boolean askConfirmation ) {
 
         return false;
     }
 
     @Override
     public boolean moveElementUp( DataControl dataControl ) {
 
         return false;
     }
 
     @Override
     public boolean moveElementDown( DataControl dataControl ) {
 
         return false;
     }
 
     @Override
     public String renameElement( String name ) {
         resources.setName( name );
         return name;
     }
 
     @Override
     public void updateVarFlagSummary( VarFlagSummary varFlagSummary ) {
 
         // Update the flag summary with the conditions
         ConditionsController.updateVarFlagSummary( varFlagSummary, resources.getConditions( ) );
     }
 
     @Override
     public boolean isValid( String currentPath, List<String> incidences ) {
 
         boolean valid = true;
 
         // Check the assets that are necessary
         for( AssetInformation assetInformation : assetsInformation ) {
             // If the asset is necessary and the value is null, set to invalid
             if( assetInformation.assetNecessary && resources.getAssetPath( assetInformation.name ) == null ) {
                 valid = false;
 
                 // Store the incidence
                 if( incidences != null )
                     incidences.add( currentPath + " >> " + TC.get( "Operation.AdventureConsistencyErrorResources", assetInformation.name ) );
             }
         }
 
         return valid;
     }
 
     @Override
     public int countAssetReferences( String assetPath ) {
 
         int count = 0;
 
         // Search in the types of the resources
         for( String type : resources.getAssetTypes( ) )
             if( resources.getAssetPath( type ).equals( assetPath ) )
                 count++;
 
         return count;
     }
 
     @Override
     public void getAssetReferences( List<String> assetPaths, List<Integer> assetTypes ) {
 
         // Search in the assetsInformation
         for( int index = 0; index < assetsInformation.length; index++ ) {
             if( resources.getAssetPath( assetsInformation[index].name ) != null && !resources.getAssetPath( assetsInformation[index].name ).equals( "" ) ) {
                 String assetPath = resources.getAssetPath( assetsInformation[index].name );
                 int assetType = assetsInformation[index].category;
 
                 // Search that the assetPath has not been previously added
                 boolean add = true;
                 for( String asset : assetPaths ) {
                     if( asset.equals( assetPath ) ) {
                         add = false;
                         break;
                     }
                 }
                 if( add ) {
                     int last = assetPaths.size( );
                     assetPaths.add( last, assetPath );
                     assetTypes.add( last, assetType );
                 }
             }
         }
     }
 
     @Override
     public void deleteAssetReferences( String assetPath ) {
 
         try {
             controller.addTool( new DeleteAssetReferencesInResources( resources, assetPath ) );
         }
         catch( CloneNotSupportedException e ) {
             e.printStackTrace( );
         }
     }
 
     @Override
     public int countIdentifierReferences( String id ) {
 
         return this.conditionsController.countIdentifierReferences( id );
     }
 
     @Override
     public void replaceIdentifierReferences( String oldId, String newId ) {
 
         conditionsController.replaceIdentifierReferences( oldId, newId );
     }
 
     @Override
     public void deleteIdentifierReferences( String id ) {
 
         conditionsController.deleteIdentifierReferences( id );
     }
 
     @Override
     public boolean canBeDuplicated( ) {
 
         return true;
     }
 
     /**
      * Method that is invoked only by the "Edit" button
      * 
      * @param filename
      * @param index
      */
     public void setAssetPath( String filename, int index ) {
 
         try {
             controller.addTool( new EditResourceTool( resources, assetsInformation, index, filename ) );
         }
         catch( CloneNotSupportedException e ) {
             e.printStackTrace( );
         }
     }
 
     @Override
     public void recursiveSearch( ) {
 
         check( this.getConditions( ), TC.get( "Search.Conditions" ) );
         for( int i = 0; i < this.getAssetCount( ); i++ ) {
             check( this.getAssetDescription( i ), TC.get( "Search.AssetDescription" ) );
             check( this.getAssetPath( i ), TC.get( "Search.AssetPath" ) );
         }
     }
 
     public int getAssetGroupCount( ) {
 
         if( assetsGroups == null )
             return 1;
         else
             return assetsGroups.length;
     }
 
     public String getGroupInfo( int i ) {
 
         return TC.get(groupsInfo[i]);
     }
 
     public int getGroupAssetCount( int selectedIndex ) {
 
         return assetsGroups[selectedIndex].length;
     }
 
     public int getAssetIndex( int group, int asset ) {
 
         if( assetsGroups == null )
             return asset;
         return assetsGroups[group][asset];
     }
 
     public boolean isIconFromImage( int i ) {
 
         if( imageIconMap == null )
             return false;
         return imageIconMap.get( assetsInformation[i].name ) != null;
     }
 
     public int getOriginalImage( int i ) {
 
         String name = imageIconMap.get( assetsInformation[i].name );
         for( int j = 0; j < assetsInformation.length; j++ ) {
             if( assetsInformation[j].name.equals( name ) )
                 return j;
         }
         return -1;
     }
 
     @Override
     public List<Searchable> getPathToDataControl( Searchable dataControl ) {
 
         return null;
     }
 
     /**
      * This method creates the frames of the animation from the images belonging
      * to the previous animation format.
      * 
      * @param assetPath
      *            The path to the previous animation
      */
     public static void framesFromImages( Animation animation, String assetPath ) {
 
         animation.getFrames( ).clear( );
         animation.getTransitions( ).clear( );
         animation.getTransitions( ).add( new Transition( ) );
         animation.getTransitions( ).add( new Transition( ) );
 
         int i = 1;
         Image currentSlide = null;
         boolean end = false;
 
         while( !end ) {
             String file = assetPath + "_" + leadingZeros( i ) + ".jpg";
             currentSlide = AssetsController.getImage( file );
             if( currentSlide == null ) {
                 file = assetPath + "_" + leadingZeros( i ) + ".png";
                 currentSlide = AssetsController.getImage( file );
             }
             if( currentSlide == null )
                 end = true;
             else
                 animation.addFrame( -1, new Frame( animation.getImageLoaderFactory( ), file ) );
             i++;
         }
     }
 
     /**
      * @param n
      *            number to convert to a String
      * @return a 2 character string with value n
      */
     private static String leadingZeros( int n ) {
 
         String s;
         if( n < 10 )
             s = "0";
         else
             s = "";
         s = s + n;
         return s;
     }
 
     public String getName( ) {
         return this.resources.getName( );
     }
 
 }
