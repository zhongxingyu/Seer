 package com.icbat.game.tradesong.utils;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.icbat.game.tradesong.Tradesong;
 import com.icbat.game.tradesong.assetReferences.TextureAssets;
 
 public class UIStyles {
 
     private TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
     private TextButton.TextButtonStyle disabledButtonStyle = new TextButton.TextButtonStyle();
     private Label.LabelStyle labelStyle = new Label.LabelStyle();
     private Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
     private BitmapFont font = new BitmapFont();
    private Label.LabelStyle blankLabelStyle = new Label.LabelStyle();
 
     public UIStyles() {
         this.textButtonStyle.font = font;
         this.textButtonStyle.fontColor = Color.WHITE;
         this.textButtonStyle.overFontColor = Color.LIGHT_GRAY;
 
         this.disabledButtonStyle.font = font;
         this.disabledButtonStyle.fontColor = Color.DARK_GRAY;
 
        this.blankLabelStyle.font = font;
         this.labelStyle.font = font;
         this.labelStyle.fontColor = Color.GREEN;
 
         this.sliderStyle.knob = new TextureRegionDrawable(new TextureRegion(Tradesong.getTexture(TextureAssets.SLIDER_HEAD)));
         this.sliderStyle.background = new TextureRegionDrawable(new TextureRegion(Tradesong.getTexture(TextureAssets.SLIDER_BG), 100, 8));
     }
 
     public TextButton.TextButtonStyle getTextButtonStyle() {
         return textButtonStyle;
     }
 
    public Label.LabelStyle getBlankLabelStyle() { return blankLabelStyle; }

     public Label.LabelStyle getLabelStyle() {
         return labelStyle;
     }
 
     public Slider.SliderStyle getSliderStyle() {
         return sliderStyle;
     }
 
     public TextButton.TextButtonStyle getDisabledButtonStyle() {
         return disabledButtonStyle;
     }
 
     public BitmapFont getFont() {
         return font;
     }
 }
