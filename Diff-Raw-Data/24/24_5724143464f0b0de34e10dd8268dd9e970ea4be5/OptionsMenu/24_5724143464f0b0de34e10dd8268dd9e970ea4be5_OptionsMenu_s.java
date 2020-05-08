 /*
  *  Wezzle
  *  Copyright (c) 2007-2009 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.menu;
 
 import ca.couchware.wezzle2d.Game;
 import ca.couchware.wezzle2d.IWindow;
 import ca.couchware.wezzle2d.ManagerHub;
 import ca.couchware.wezzle2d.ResourceFactory;
 import ca.couchware.wezzle2d.ResourceFactory.LabelBuilder;
 import ca.couchware.wezzle2d.audio.MusicPlayer;
 import ca.couchware.wezzle2d.audio.Sound;
 import ca.couchware.wezzle2d.graphics.IEntity;
 import ca.couchware.wezzle2d.manager.LayerManager;
 import ca.couchware.wezzle2d.manager.LayerManager.Layer;
 import ca.couchware.wezzle2d.manager.Settings.Key;
 import ca.couchware.wezzle2d.ui.ITextLabel;
 import ca.couchware.wezzle2d.ui.RadioGroup;
 import ca.couchware.wezzle2d.ui.RadioItem;
 import ca.couchware.wezzle2d.ui.Box;
 import ca.couchware.wezzle2d.ui.Button;
 import ca.couchware.wezzle2d.ui.IButton;
 import ca.couchware.wezzle2d.ui.SliderBar;
 import ca.couchware.wezzle2d.util.CouchLogger;
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 import javazoom.jlgui.basicplayer.BasicPlayerException;
 
 /**
  * The play now group, which holds all the configuration options for playing
  * a Wezzle game.
  * 
  * @author cdmckay
  */
 public class OptionsMenu extends AbstractMenu
 {
     final private static int MIN_QUALITY = 0;       
     final private static int MAX_QUALITY = 4;
 
     final private static int MIN_VOLUME = 0;
     final private static int MAX_VOLUME = 100;
 
     final private static int ON = 0;
     final private static int OFF = 1;
 
     private MusicPlayer menuPlayer;          
             
     private RadioGroup autoPauseRadio;
     private RadioGroup fullscreenRadio;
 
     private RadioGroup piecePreviewBoxRadio;        
     private RadioGroup piecePreviewShadowRadio;   
 
     private RadioGroup musicRadio;
     private ITextLabel musicVolumeValueLabel;    
     private int musicVolumeValue;    
     private SliderBar musicVolumeValueSlider;    
 
     private RadioGroup soundRadio;
     private ITextLabel soundVolumeValueLabel;    
     private int soundVolumeValue;    
     private SliderBar soundVolumeValueSlider;
     private IButton soundTestButton;
 
     private enum Page
     {
         GAME("Game"),
         AUDIO("Audio");
 
         Page(String description)
         { this.description = description; }
 
         private String description;
 
         public String getDescription()
         { return description; }
     }
 
     private Page currentPage = Page.GAME;
 
     private IButton gameButton;
     private IButton audioButton;
     
     final private List<IEntity> gamePageEntities  = new ArrayList<IEntity>();
     final private List<IEntity> audioPageEntities = new ArrayList<IEntity>();
                     
     public OptionsMenu(IMenu parentMenu,
             IWindow win, ManagerHub hub, LayerManager menuLayerMan)
     {                
         // Invoke the super.
         super(parentMenu, win, hub, menuLayerMan);
                
         // The colors.
         final Color LABEL_COLOR  = hub.settingsMan.getColor(Key.GAME_COLOR_PRIMARY);        
         final Color OPTION_COLOR = hub.settingsMan.getColor(Key.GAME_COLOR_SECONDARY);
 
         this.menuPlayer = ((MainMenu) this.parent).getPlayer();
         hub.soundMan.setPaused( false );
    
         createMenuEntities(win, hub, LABEL_COLOR, OPTION_COLOR);
         createEntitiesForGamePage(win, hub, LABEL_COLOR, OPTION_COLOR);
         createEntitiesForAudioPage(win, hub, LABEL_COLOR, OPTION_COLOR);
            
         // Add them all to the layer manager.
         for (IEntity entity : this.entityList)
         {
             this.menuLayerMan.add(entity, Layer.UI);
         }
 
         // Add all the starting page entities.
         for ( IEntity entity : this.gamePageEntities )
         {
             this.menuLayerMan.add(entity, Layer.UI);
             this.entityList.add(entity);
         }      
     }                   
 
     private void switchPage(Page page)
     {
         if (this.currentPage == page) return;
         this.currentPage = page;
 
         switch (page)
         {
             case GAME:
 
                 for ( IEntity entity : this.gamePageEntities )                
                     addEntity(entity);
 
                 for ( IEntity entity : this.audioPageEntities )                
                     removeEntity(entity);
 
                 break;
 
             case AUDIO:
 
                 for ( IEntity entity : this.gamePageEntities )
                     removeEntity(entity);
 
                 for ( IEntity entity : this.audioPageEntities )
                     addEntity(entity);
 
                 break;
 
             default:
                 throw new IllegalArgumentException("Invalid options page");
         }
     }
 
     private void addEntity(IEntity entity)
     {
         this.menuLayerMan.add(entity, Layer.UI);
         this.entityList.add(entity);
         entity.setVisible(true);
         entity.setDisabled(false);
     }
 
     private void removeEntity(IEntity entity)
     {
         this.menuLayerMan.remove(entity, Layer.UI);
         this.entityList.remove(entity);
         entity.setVisible(false);
         entity.setDisabled(true);
     }
 
     private void createMenuEntities(IWindow win, ManagerHub hub,
             Color labelColor, Color optionColor)
     {
         Box optionBox = new Box.Builder(win, 68, 122)
                 .width(400).height(398)
                 .border(Box.Border.MEDIUM)
                 .opacity(80)
                 .visible(false).build();
 
         this.entityList.add(optionBox);
 
         // <editor-fold defaultstate="collapsed" desc="Title Label">
         
         ITextLabel titleLabel = new LabelBuilder(74, 97)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                 .color(labelColor)
                 .text("Options").size(20)
                 .visible(false)
                 .build();
         
         this.entityList.add(titleLabel);
         
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="Audio Tab Button">
 
         this.audioButton = new Button.Builder(
                     win, optionBox.getX() + optionBox.getWidth() - 6, 97)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.RIGHT))
                 .color(labelColor)
                 .normalOpacity( 90 )
                 .activeOpacity( 90 )
                 .hoverOpacity( 100 )
                 .pressedOpacity( 100 )
                 .visible(false)
                 .width(80)
                 .text(Page.AUDIO.getDescription()).textSize(12)
                 .build();
 
         this.audioButton.addButtonListener(new Button.IButtonListener()
         {
             public void buttonClicked()
             {
                 switchPage(Page.AUDIO);
             }
         });
 
         this.entityList.add(this.audioButton);
 
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="Game Tab Button">
 
         this.gameButton = new Button.Builder(
                     win,
                     this.audioButton.getX() - this.audioButton.getWidth() - 5,
                     this.audioButton.getY())
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.RIGHT))
                 .color(labelColor)
                 .normalOpacity( 90 )
                 .activeOpacity( 90 )
                 .hoverOpacity( 100 )
                 .pressedOpacity( 100 )
                 .visible(false)
                 .width(80)
                 .text(Page.GAME.getDescription()).textSize(12)
                 .build();
 
         this.gameButton.addButtonListener(new Button.IButtonListener()
         {
             public void buttonClicked()
             { 
                 switchPage(Page.GAME);
             }
         });
 
         this.entityList.add(this.gameButton);
 
         // </editor-fold>
     }
 
     private void createEntitiesForGamePage(IWindow win, ManagerHub hub,
             Color labelColor, Color optionColor)
     {       
         // <editor-fold defaultstate="collapsed" desc="Fullscreen">
         ITextLabel fullscreenLabel = new LabelBuilder(110, 190)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                 .color(labelColor)
                 .text("Fullscreen").size(20)
                 .visible(false)
                 .build();
 
         this.gamePageEntities.add(fullscreenLabel);
 
         if (Game.isApplet())
         {
             ITextLabel noFullScreenLabel = new LabelBuilder(
                    268 + 90,
                     fullscreenLabel.getY())
                    .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                     .color(Color.DARK_GRAY)
                    .text("Not Available").size(16)
                     .visible(false)
                     .build();
 
             this.gamePageEntities.add(noFullScreenLabel);
         }
         else
         {
             RadioItem fullscreenOn = new RadioItem.Builder(win).color(optionColor)
                 .text("On").build();
 
             RadioItem fullscreenOff = new RadioItem.Builder(win).color(optionColor)
                     .text("Off").build();
 
             final boolean fullscreenSetting = hub.settingsMan.getBool(Key.USER_GRAPHICS_FULLSCREEN);
 
             this.fullscreenRadio = new RadioGroup.Builder(
                     win, 268 + 150, fullscreenLabel.getY())
                     .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.RIGHT))
                     .add(fullscreenOn, fullscreenSetting)
                     .add(fullscreenOff, !fullscreenSetting)
                     .visible(false)
                     .build();
 
             this.gamePageEntities.add(fullscreenRadio);
         }
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="Auto Pause">        
         ITextLabel autoPauseLabel = new LabelBuilder(
                 fullscreenLabel.getX(),
                 fullscreenLabel.getY() + 60)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                 .color(labelColor)
                 .text("Auto-Pause").size(20)
                 .visible(false)
                 .build();
 
         this.gamePageEntities.add(autoPauseLabel);
 
         if (Game.isApplet())
         {
              ITextLabel noAutoPauseButton = new LabelBuilder(
                    268 + 90,
                     autoPauseLabel.getY())
                    .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                     .color(Color.DARK_GRAY)
                    .text("Not Available").size(16)
                     .visible(false)
                     .build();
 
             this.gamePageEntities.add(noAutoPauseButton);
         }
         else
         {
             final RadioItem autoPauseOn = new RadioItem.Builder(win).color(optionColor)
                 .text("On").build();
 
             final RadioItem autoPauseOff = new RadioItem.Builder(win).color(optionColor)
                     .text("Off").build();
 
             final boolean autoPauseSetting = hub.settingsMan.getBool(Key.USER_AUTO_PAUSE);
             this.autoPauseRadio = new RadioGroup.Builder(
                     win, 268 + 150, autoPauseLabel.getY())
                     .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.RIGHT))
                     .add(autoPauseOn, autoPauseSetting)
                     .add(autoPauseOff, !autoPauseSetting)
                     .visible(false)
                     .build();
 
             this.gamePageEntities.add(autoPauseRadio);
         }                        
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="Piece Preview Box">
         ITextLabel piecePreviewBoxLabel = new LabelBuilder(
                 autoPauseLabel.getX(),
                 autoPauseLabel.getY() + 65)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                 .color(labelColor).text("Piece Preview Box")
                 .size(20).visible(false)
                 .build();
         
         this.gamePageEntities.add(piecePreviewBoxLabel);
 
         // Creat the level limit radio group.
         RadioItem boxItem1 = new RadioItem.Builder(win).color(optionColor)
                 .text("On").build();
 
         RadioItem boxItem2 = new RadioItem.Builder(win).color(optionColor)
                 .text("Off").build();
 
         // Attempt to get the user's music preference.
         final boolean piecePreviewBoxSetting = hub.settingsMan.getBool(Key.USER_PIECE_PREVIEW_TRADITIONAL);
 
         this.piecePreviewBoxRadio = new RadioGroup.Builder(
                 win, 268, piecePreviewBoxLabel.getY() + 35)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                 .add(boxItem1, piecePreviewBoxSetting)
                 .add(boxItem2, !piecePreviewBoxSetting)
                 .itemSpacing(20)
                 .visible(false)
                 .build();
         
         this.gamePageEntities.add(piecePreviewBoxRadio);
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="Piece Preview Shadow">
         ITextLabel piecePreviewShadow = new LabelBuilder(
                 piecePreviewBoxLabel.getX(),
                 piecePreviewBoxLabel.getY() + 95)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                 .color(labelColor)
                 .text("Piece Preview Shadow").size(20)
                 .visible(false)
                 .build();
         this.gamePageEntities.add(piecePreviewShadow);
 
         // Creat the level limit radio group.
         RadioItem shadowItem1 = new RadioItem.Builder(win).color(optionColor)
                 .text("On").build();
 
         RadioItem shadowItem2 = new RadioItem.Builder(win).color(optionColor)
                 .text("Off").build();
 
         // Attempt to get the user's music preference.
         final boolean piecePreviewShadowSetting = hub.settingsMan.getBool(Key.USER_PIECE_PREVIEW_SHADOW);
 
         this.piecePreviewShadowRadio = new RadioGroup.Builder(
                     win, 268, piecePreviewShadow.getY() + 35)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                 .add(shadowItem1, piecePreviewShadowSetting)
                 .add(shadowItem2, !piecePreviewShadowSetting)
                 .itemSpacing(20)
                 .visible(false)
                 .build();
 
         this.gamePageEntities.add(piecePreviewShadowRadio);
         // </editor-fold>
     }
 
     private void createEntitiesForAudioPage(IWindow win, ManagerHub hub,
             Color labelColor, Color optionColor)
     {
         // <editor-fold defaultstate="collapsed" desc="Music On/Off">
         ITextLabel musicLabel = new LabelBuilder(110, 190)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                 .color(labelColor)
                 .text("Music").size(20)
                 .visible(false)
                 .build();
         this.audioPageEntities.add(musicLabel);
 
         RadioItem musicOn = new RadioItem.Builder(win).color(optionColor)
                 .text("On").build();
         
         RadioItem musicOff = new RadioItem.Builder(win).color(optionColor)
                 .text("Off").build();
 
         final boolean musicSetting = hub.settingsMan.getBool(Key.USER_MUSIC);
         this.musicRadio = new RadioGroup.Builder(
                     win, 310, musicLabel.getY())
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                 .add(musicOn, musicSetting)
                 .add(musicOff, !musicSetting)
                 .visible(false)
                 .build();
         this.audioPageEntities.add(musicRadio);
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="Music Volume">
         // Get the user set level and make sure it's within range.
         this.musicVolumeValue = hub.settingsMan.getInt(Key.USER_MUSIC_VOLUME);
         this.musicVolumeValue = Math.max(MIN_VOLUME, this.musicVolumeValue);
         this.musicVolumeValue = Math.min(MAX_VOLUME, this.musicVolumeValue);
 
         ITextLabel musicVolumeLabel = new LabelBuilder(
                     musicLabel.getX(),
                     musicLabel.getY() + 40)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                 .color(labelColor).text("Music Volume").size(20)
                 .visible(false)
                 .build();
         this.audioPageEntities.add(musicVolumeLabel);
 
         this.musicVolumeValueLabel = new LabelBuilder(
                     musicVolumeLabel.getX() + musicVolumeLabel.getWidth() + 20,
                     musicVolumeLabel.getY())
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                 .color(optionColor)
                 .size(20).visible(false)
                 .text("" + this.musicVolumeValue)
                 .build();
         this.audioPageEntities.add(this.musicVolumeValueLabel);
 
         this.musicVolumeValueSlider = new SliderBar.Builder(
                     win, 268, musicVolumeLabel.getY() + 35)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                 .width(340)
                 .virtualRange(MIN_VOLUME, MAX_VOLUME)
                 .virtualValue(this.musicVolumeValue)
                 .visible(false)
                 .build();
         this.audioPageEntities.add(musicVolumeValueSlider);
         // </editor-fold>     
 
         // <editor-fold defaultstate="collapsed" desc="Sound On/Off">
         ITextLabel soundLabel = new LabelBuilder(
                     musicVolumeLabel.getX(),
                     musicVolumeValueSlider.getY() + 55)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                 .color(labelColor)
                 .text("Sound").size(20)
                 .visible(false)
                 .build();
         this.audioPageEntities.add(soundLabel);
 
         RadioItem soundOn = new RadioItem.Builder(win).color(optionColor)
                 .text("On").build();
 
         RadioItem soundOff = new RadioItem.Builder(win).color(optionColor)
                 .text("Off").build();
 
         final boolean soundSetting = hub.settingsMan.getBool(Key.USER_SOUND);
         this.soundRadio = new RadioGroup.Builder(
                     win, 310, soundLabel.getY())
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                 .add(soundOn, soundSetting)
                 .add(soundOff, !soundSetting)
                 .visible(false)
                 .build();
         this.audioPageEntities.add(soundRadio);
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="Sound Volume">
         // Get the user set level and make sure it's within range.
         this.soundVolumeValue = hub.settingsMan.getInt(Key.USER_SOUND_VOLUME);
         this.soundVolumeValue = Math.max(MIN_VOLUME, this.soundVolumeValue);
         this.soundVolumeValue = Math.min(MAX_VOLUME, this.soundVolumeValue);
 
         ITextLabel soundVolumeLabel = new LabelBuilder(
                     soundLabel.getX(),
                     soundLabel.getY() + 40)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                 .color(labelColor).text("Sound Volume").size(20)
                 .visible(false)
                 .build();
         this.audioPageEntities.add(soundVolumeLabel);
 
         this.soundVolumeValueLabel = new LabelBuilder(
                     soundVolumeLabel.getX() + soundVolumeLabel.getWidth() + 20,
                     soundVolumeLabel.getY())
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                 .color(optionColor)
                 .size(20).visible(false)
                 .text("" + this.soundVolumeValue)
                 .build();
         this.audioPageEntities.add(this.soundVolumeValueLabel);
 
         this.soundVolumeValueSlider = new SliderBar.Builder(
                     win, 268, soundVolumeLabel.getY() + 35)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                 .width(340)
                 .virtualRange(MIN_VOLUME, MAX_VOLUME)
                 .virtualValue(this.soundVolumeValue)
                 .visible(false)
                 .build();
         this.audioPageEntities.add(soundVolumeValueSlider);
         // </editor-fold>        
 
         this.soundTestButton = new Button
                 .Builder( win, 268, soundVolumeValueSlider.getY() + 60 )
                 .alignment( EnumSet.of(Alignment.MIDDLE, Alignment.CENTER) )
                 .color( labelColor )
                 .normalOpacity(90)
                 .activeOpacity(90)
                 .visible(false)
                 .text("Test Sound").textSize(16)
                 .width( 140 )
                 .build();
         
         this.audioPageEntities.add(this.soundTestButton);
     }
 
     @Override
     public void updateLogic(Game game, ManagerHub hub)
     {
         super.updateLogic( game, hub );
 
         if ( this.fullscreenRadio != null && this.fullscreenRadio.changed() )
         {
             int selected = this.fullscreenRadio.getSelectedIndex();
             boolean isOn = selected == ON;
             
             win.setFullscreen( isOn );
             hub.settingsMan.setBool( Key.USER_GRAPHICS_FULLSCREEN, win.isFullscreen() );
         }
 
         if ( this.autoPauseRadio != null && this.autoPauseRadio.changed() )
         {
             int selected = this.autoPauseRadio.getSelectedIndex();
             boolean isOn = selected == ON;
 
             hub.settingsMan.setBool(Key.USER_AUTO_PAUSE, isOn);
         }
 
         if ( this.piecePreviewBoxRadio.changed() )
         {
             int selected = this.piecePreviewBoxRadio.getSelectedIndex();
             boolean isOn = selected == ON;
 
             hub.settingsMan.setBool(Key.USER_PIECE_PREVIEW_TRADITIONAL, isOn);
             game.getUI().setTraditionalPiecePreviewVisible(isOn);
         }
 
         if ( this.piecePreviewShadowRadio.changed() )
         {
             int selected = this.piecePreviewShadowRadio.getSelectedIndex();
             boolean isOn = selected == ON;
 
             hub.settingsMan.setBool(Key.USER_PIECE_PREVIEW_SHADOW, isOn);
             //game.getUI().setOverlayPiecePreviewVisible(isOn);
         }
 
         if ( this.musicRadio.changed() )
         {
             int selected = this.musicRadio.getSelectedIndex();
             boolean isSwitchedOn = selected == ON;
 
             try
             {
                 if (isSwitchedOn)
                 {
                     this.menuPlayer.resume();
                 }
                 else
                 {
                     this.menuPlayer.pause();
                 }
             }
             catch ( BasicPlayerException e )
             {
                 CouchLogger.get().recordException(this.getClass(), e);
             }
 
             hub.settingsMan.setBool(Key.USER_MUSIC, isSwitchedOn);
         }
 
         if ( this.soundRadio.changed() )
         {
             int selected = this.soundRadio.getSelectedIndex();
             boolean isSwitchedOn = selected == ON;
 
             hub.settingsMan.setBool(Key.USER_SOUND, isSwitchedOn);
         }
 
         if ( this.musicVolumeValueSlider.changed() )
         {
             musicVolumeValue = musicVolumeValueSlider.getVirtualValue();
             this.musicVolumeValueLabel.setText("" + musicVolumeValue);
             hub.settingsMan.setInt(Key.USER_MUSIC_VOLUME, this.musicVolumeValue);
             //hub.musicMan.setNormalizedGain( musicVolumeValueSlider.getVirtualPercent() );
 
             try
             {             
                 this.menuPlayer.setNormalizedGain(
                         musicVolumeValueSlider.getVirtualPercent() );
             }
             catch ( BasicPlayerException e )
             {
                 CouchLogger.get().recordException(this.getClass(), e);
             }
         }
 
         if ( this.soundVolumeValueSlider.changed() )
         {
             soundVolumeValue = soundVolumeValueSlider.getVirtualValue();
             this.soundVolumeValueLabel.setText("" + soundVolumeValue);
             hub.settingsMan.setInt(Key.USER_SOUND_VOLUME, this.soundVolumeValue);
             hub.soundMan.setNormalizedGain( soundVolumeValueSlider.getVirtualPercent() );
         }       
 
         if ( this.soundTestButton.clicked() )
         {            
             hub.soundMan.play( Sound.ROCKET );            
         }
 
         // Clear the clicked flag.
         gameButton.clicked();
         audioButton.clicked();
 
     }     
 
 } // end class
