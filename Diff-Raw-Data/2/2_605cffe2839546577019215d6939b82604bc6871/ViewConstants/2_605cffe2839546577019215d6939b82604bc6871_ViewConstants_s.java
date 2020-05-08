 package com.greenteam.huntjumper.utils;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 
 /**
  * User: GreenTea Date: 13.01.12 Time: 22:45
  */
 public final class ViewConstants
 {
    private ViewConstants()
    {
 
    }
 
    public static final int VIEW_WIDTH = 1024;
    public static final int VIEW_HEIGHT = 768;
    public static final String GAME_NAME = "Hunt the Jumper";
    public static final Color defaultMapColor = new Color(114, 114, 114);
    public static final Color defaultGroundColor = new Color(214, 214, 214);
    public static final float DRAW_NAME_MAX_RADIUS = GameConstants.JUMPER_RADIUS*7;
 
    public static final Color jumperBorderColor = Color.black;
 
    public static final int scoresBoxDistFromLeft = 5;
    public static final int scoresBoxDistFromTop = 5;
    public static final int scoresBoxTextLeftIndent = 15;
    public static final int scoresBoxTextTopIndent = 15;
    public static final int scoresBoxLineIndent = 10;
    public static final int scoresBoxRectRadius = 4;
    public static final float scoresBoxAlpha = 0.6f;
    public static final float scoresBoxJumpersAlpha = 0.5f;
    public static final int scoresBoxRightBorder = 310;
    public static final int scoresBoxScoresPosX = 200;
    public static final int scoresBoxBackTimerPosX = 260;
    public static final int scoresBoxBackTimerStartBlinkTime = 5000;
   public static final int scoresBoxBackTimerBlinkPeriod = 5000;
    public static final Font scoresBoxFont = TextUtils.Arial20Font;
 
    public static final int timerIndentFromTop = 10;
    public static final float timerEllipseVerticalRadius = 20f;
    public static final float timerEllipseHorizontalRadius = 70f;
    public static final float timerEllipseAlpha = 0.35f;
    public static final int timerEllipseIndentFromText = 5;
 }
