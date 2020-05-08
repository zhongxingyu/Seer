 
 package com.prunn.rfdynhud.widgets.prunn.rfe.weather;
 
 import java.awt.Font;
 import java.io.IOException;
 
 
 import net.ctdp.rfdynhud.gamedata.LiveGameData;
 import net.ctdp.rfdynhud.properties.ColorProperty;
 import net.ctdp.rfdynhud.properties.FontProperty;
 import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
 import net.ctdp.rfdynhud.properties.PropertiesContainer;
 import net.ctdp.rfdynhud.properties.PropertyLoader;
 import net.ctdp.rfdynhud.render.DrawnString;
 import net.ctdp.rfdynhud.render.DrawnString.Alignment;
 import net.ctdp.rfdynhud.render.DrawnStringFactory;
 import net.ctdp.rfdynhud.render.TextureImage2D;
 import net.ctdp.rfdynhud.util.PropertyWriter;
 import net.ctdp.rfdynhud.util.SubTextureCollector;
 import net.ctdp.rfdynhud.valuemanagers.Clock;
 import net.ctdp.rfdynhud.values.FloatValue;
 import net.ctdp.rfdynhud.widgets.base.widget.Widget;
 import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_RFE;
 /**
  * 
  * 
  * @author Prunn
  */
 public class WeatherWidget extends Widget
 {
     private DrawnString dsRainingPercent = null;
     private DrawnString dsOnPathWetness = null;
     private DrawnString dsOffPathWetness = null;
     private FloatValue rainingSeverity = new FloatValue();
     private FloatValue onPathWetness = new FloatValue();
     private FloatValue offPathWetness = new FloatValue();
     private FloatValue cloudDarkness = new FloatValue();
     //private BoolValue weatherChanged = new BoolValue();
     protected final FontProperty rfe_Font = new FontProperty("Main Font", PrunnWidgetSet_RFE.RFE_FONT_NAME);
     
     private ImagePropertyWithTexture imgSunny = new ImagePropertyWithTexture("imgSunny", "prunn/rfe/Sunny.png");
     private ImagePropertyWithTexture imgSlightDrizzle = new ImagePropertyWithTexture("SlightDrizzle", "prunn/rfe/SlightDrizzle.png");
     private ImagePropertyWithTexture imgDrizzle = new ImagePropertyWithTexture("imgDrizzle", "prunn/rfe/Drizzle.png");
     private ImagePropertyWithTexture imgThunderstorms = new ImagePropertyWithTexture("imgDrizzle", "prunn/rfe/Thunderstorms.png");
     private ImagePropertyWithTexture imgDark = new ImagePropertyWithTexture("imgDrizzle", "prunn/rfe/Dark.png");
     private final ColorProperty fontColor2 = new ColorProperty("fontColor2", PrunnWidgetSet_RFE.FONT_COLOR2_NAME);
     
     public String getDefaultNamedColorValue(String name)
     {
         if(name.equals("StandardBackground"))
             return "#00000000";
         
         return null;
     }
     @Override
     public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
     {
        super.onRealtimeEntered( gameData, isEditorMode );
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     protected void initSubTextures( LiveGameData arg0, boolean arg1, int arg2, int arg3, SubTextureCollector arg4 )
     {
         // TODO Auto-generated method stub
         
     }
     @Override
     protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
     {
         int fh = TextureImage2D.getStringHeight( "0%C", rfe_Font );
         //dsRainingPercent = drawnStringFactory.newDrawnString( "dsRainingPercent",width * 34 / 100 , height, getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "%" );
         dsRainingPercent = drawnStringFactory.newDrawnString( "dsRainingPercent", width/2, height - fh, Alignment.CENTER, false, rfe_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "%");
         //dsOnPathWetness = drawnStringFactory.newDrawnString( "dsOnPathWetness",width * 5 / 100 , height*10/100, rfe_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "%" );
         dsOnPathWetness = drawnStringFactory.newDrawnString( "dsOnPathWetness", 0, 0, Alignment.LEFT, false, rfe_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "%");
         //dsOffPathWetness = drawnStringFactory.newDrawnString( "dsOffPathWetness",width * 60 / 100 , height*10/100, rfe_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "%" );
         dsOffPathWetness = drawnStringFactory.newDrawnString( "dsOffPathWetness", width, 0, Alignment.RIGHT, false, rfe_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "%");
         imgSunny.updateSize( width-fh*8/5, height-fh*8/5, isEditorMode );
         imgSlightDrizzle.updateSize( width-fh*8/5, height-fh*8/5, isEditorMode );
         imgDrizzle.updateSize( width-fh*8/5, height-fh*8/5, isEditorMode );
         imgThunderstorms.updateSize( width-fh*8/5, height-fh*8/5, isEditorMode );
         imgDark.updateSize( width-fh*8/5, height-fh*8/5, isEditorMode );
     }
     
     @Override
     protected Boolean updateVisibility( LiveGameData gameData, boolean isEditorMode )
     {
         super.updateVisibility( gameData, isEditorMode );
         
            
        rainingSeverity.update( gameData.getScoringInfo().getRainingSeverity() );
        onPathWetness.update( gameData.getScoringInfo().getOnPathWetness() );
        offPathWetness.update( gameData.getScoringInfo().getOffPathWetness() );
        cloudDarkness.update( gameData.getScoringInfo().getCloudDarkness() );
         if((rainingSeverity.hasChanged() || cloudDarkness.hasChanged()) && !isEditorMode)
         {
             /*if(rainingSeverity.getValue() > 0f)
                 weatherChanged.update( true );
             else
                 weatherChanged.update( false );
             
             if(weatherChanged.hasChanged() && !isEditorMode)*/
                 forceCompleteRedraw( true );
         }
         return true;
         
     }
     @Override
     protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
     {
         super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
         int fh = TextureImage2D.getStringHeight( "0%C", rfe_Font );
         int off = fh*4/5;
         if(rainingSeverity.getValue() > 0.3f || isEditorMode)
             texture.clear( imgThunderstorms.getTexture(), offsetX + off, offsetY + off, false, null );
         else if(rainingSeverity.getValue() > 0.02f)
             texture.clear( imgDrizzle.getTexture(), offsetX + off, offsetY + off, false, null );
         else if(rainingSeverity.getValue() > 0.0f)
             texture.clear( imgSlightDrizzle.getTexture(), offsetX + off, offsetY + off, false, null );
         else if(cloudDarkness.getValue() > 0.8f)
             texture.clear( imgDark.getTexture(), offsetX + off, offsetY + off, false, null );
         else
             texture.clear( imgSunny.getTexture(), offsetX + off, offsetY + off, false, null );
     }
     @Override
     protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
     {
         int rain = (int)( rainingSeverity.getValue() * 100 );
         dsRainingPercent.draw( offsetX, offsetY, "Rain: " + rain, texture );
         int path = (int)( onPathWetness.getValue() * 100 );
         dsOnPathWetness.draw( offsetX, offsetY, "Path: " + path, texture );
         int offline = (int)( offPathWetness.getValue() * 100 );
         dsOffPathWetness.draw( offsetX, offsetY, "Offline: " + offline, texture );
     }
       
      
     @Override
     public void saveProperties( PropertyWriter writer ) throws IOException
     {
         super.saveProperties( writer );
         writer.writeProperty( fontColor2, "" );
         writer.writeProperty( rfe_Font, "" );
         
     }
     
     @Override
     public void loadProperty( PropertyLoader loader )
     {
         super.loadProperty( loader );
         if ( loader.loadProperty( fontColor2 ) );
         else if ( loader.loadProperty( rfe_Font ) );
         
     }
     @Override
     public void getProperties( PropertiesContainer propsCont, boolean forceAll )
     {
         super.getProperties( propsCont, forceAll );
         
         propsCont.addGroup( "Specific" );
         propsCont.addProperty( fontColor2 );
         propsCont.addProperty( rfe_Font );
         
     }
     /**
      * {@inheritDoc}
      */
     @Override
     public void prepareForMenuItem()
     {
         super.prepareForMenuItem();
         
         getFontProperty().setFont( "Dialog", Font.PLAIN, 9, false, true );
         
     }
     
     public WeatherWidget()
     {
         super( PrunnWidgetSet_RFE.INSTANCE, PrunnWidgetSet_RFE.WIDGET_PACKAGE_RFE, 14.0f, 18.0f );
         getBackgroundProperty().setColorValue( "#00000000" );
         
     }
 }
