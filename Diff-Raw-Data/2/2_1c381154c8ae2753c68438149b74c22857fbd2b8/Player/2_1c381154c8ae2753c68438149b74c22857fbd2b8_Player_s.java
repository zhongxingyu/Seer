 package net.stuffrepos.tactics16.scenes.battle;
 
 import net.stuffrepos.tactics16.animation.GameImage;
 import net.stuffrepos.tactics16.animation.SpriteAnimation;
 import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeMap;
 import net.stuffrepos.tactics16.game.DataObject;
 import net.stuffrepos.tactics16.game.Job;
 import net.stuffrepos.tactics16.game.Job.GameAction;
 import net.stuffrepos.tactics16.game.JobSpriteActionGroup;
 import net.stuffrepos.tactics16.util.cache.CacheableMapValue;
 import net.stuffrepos.tactics16.util.cache.CacheableValue;
 import net.stuffrepos.tactics16.util.image.ColorUtil;
 import net.stuffrepos.tactics16.util.image.PixelImageIterator;
 
 /**
  *
  * @author Eduardo H. Bogoni <eduardobogoni@gmail.com>
  */
 public class Player extends DataObject {
 
     private static final int SELECTED_ACTION_CHANGE_FRAME_INTERVAL = 100;
     private CacheableMapValue<JobSpriteActionGroup, CacheableMapValue<GameImage, GameImage>> images =
             new CacheableMapValue<JobSpriteActionGroup, CacheableMapValue<GameImage, GameImage>>() {
 
                 @Override
                 protected CacheableMapValue<GameImage, GameImage> calculate(final JobSpriteActionGroup jobSpriteActionGroup) {
                     return new CacheableMapValue<GameImage, GameImage>() {
 
                         @Override
                         protected GameImage calculate(GameImage gameImage) {
                             GameImage valueImage = PLAYER_COLORS.get(index).getMaskedImage(jobSpriteActionGroup, gameImage);
                             if (valueImage.getScale() == 1.0) {
                                 return valueImage;
                             } else {
                                 return valueImage.getScaledImage();
                             }
                         }
                     };
                 }
             };
     private CacheableMapValue<Job, SpriteAnimation> selectedAnimations =
             new CacheableMapValue<Job, SpriteAnimation>() {
 
                 private Collection<GameImage> createImages(Job job, GameImage image) {
                     List<GameImage> list = new LinkedList<GameImage>();
                     final int COUNT = 4;
                     for (int i = 0; i <= COUNT; ++i) {
                         float factor = ((float) i / COUNT) * 0.4f + 0.8f;
                         list.add(createSelectedImage(Player.this.getImage(job.getSpriteActionGroup(), image), factor));
                     }
 
                     return list;
                 }
 
                 private GameImage createSelectedImage(GameImage image, final float factor) {
 
                     final BufferedImage newImage =
                             new BufferedImage(image.getImage().getWidth(), image.getImage().getHeight(), Transparency.BITMASK);
 
                     new PixelImageIterator(image.getImage()) {
 
                         @Override
                         public void iterate(int x, int y, int rgb) {
                             if ((rgb & 0x00FFFFFF) == 0x00FF00FF) {
                                 newImage.setRGB(x, y, 0);
                             } else {
                                 newImage.setRGB(x, y, ColorUtil.applyFactor(new java.awt.Color(rgb), factor).getRGB());
                             }
 
 
 
                         }
                     };
 
                     GameImage gameImage = new GameImage(newImage);
                     gameImage.getCenter().set(image.getCenter());
 
                     return gameImage;
                 }
 
                 @Override
                 protected SpriteAnimation calculate(Job key) {
                     SpriteAnimation animation = new SpriteAnimation();
                     SpriteAnimation stoppedAnimation = key.getSpriteActionGroup().getSpriteAction(GameAction.STOPPED);
                     animation.setChangeFrameInterval(SELECTED_ACTION_CHANGE_FRAME_INTERVAL);
 
                     for (GameImage sourceImage : stoppedAnimation.getImages()) {
                         for (GameImage selectedImage : createImages(key, sourceImage)) {
                             animation.addImage(selectedImage);
                         }
                     }
 
                     return animation;
                 }
             };
     private CacheableMapValue<Job, SpriteAnimation> usedAnimations =
             new CacheableMapValue<Job, SpriteAnimation>() {
 
                 private Collection<GameImage> createImages(Job job, GameImage image) {
                     List<GameImage> list = new LinkedList<GameImage>();
                     list.add(createSelectedImage(Player.this.getImage(job.getSpriteActionGroup(), image)));
 
                     return list;
                 }
 
                 private GameImage createSelectedImage(GameImage image) {
 
                     final BufferedImage newImage =
                             new BufferedImage(image.getImage().getWidth(), image.getImage().getHeight(), Transparency.BITMASK);
 
                     new PixelImageIterator(image.getImage()) {
 
                         @Override
                         public void iterate(int x, int y, int rgb) {
                            if (rgb == 0) {
                                 newImage.setRGB(x, y, 0);
                             } else {
                                 newImage.setRGB(x, y, ColorUtil.grayScale(new java.awt.Color(rgb)).getRGB());
                             }
                         }
                     };
 
                     GameImage gameImage = new GameImage(newImage);
                     gameImage.getCenter().set(image.getCenter());
 
                     return gameImage;
                 }
 
                 @Override
                 protected SpriteAnimation calculate(Job key) {
                     SpriteAnimation animation = new SpriteAnimation();
                     SpriteAnimation stoppedAnimation = key.getSpriteActionGroup().getSpriteAction(GameAction.STOPPED);
                     animation.setChangeFrameInterval(stoppedAnimation.getChangeFrameInterval());
 
                     for (GameImage sourceImage : stoppedAnimation.getImages()) {
                         for (GameImage selectedImage : createImages(key, sourceImage)) {
                             animation.addImage(selectedImage);
                         }
                     }
 
                     return animation;
                 }
             };
     private final int index;
     public static final ArrayList<PlayerColors> PLAYER_COLORS;
 
     static {
         PlayerColors player1Colors = new PlayerColors();
         player1Colors.setMapping(Color.MAIN, 0x400000, 0xFF0000);
         player1Colors.setMapping(Color.SECUNDARY, 0x603030, 0xFFDDDD);
 
         PlayerColors player2Colors = new PlayerColors();
         player2Colors.setMapping(Color.MAIN, 0x000040, 0x0000FF);
         player2Colors.setMapping(Color.SECUNDARY, 0x303060, 0xDDDDFF);
 
         PlayerColors player3Colors = new PlayerColors();
         player3Colors.setMapping(Color.MAIN, 0x004000, 0x00FF00);
         player3Colors.setMapping(Color.SECUNDARY, 0x306030, 0xDDFFDD);
 
         PlayerColors player4Colors = new PlayerColors();
         player4Colors.setMapping(Color.MAIN, 0x404000, 0xFFFF00);
         player4Colors.setMapping(Color.SECUNDARY, 0x606050, 0xFFFFDD);
 
         PLAYER_COLORS = new ArrayList<PlayerColors>();
         PLAYER_COLORS.add(player1Colors);
         PLAYER_COLORS.add(player2Colors);
         PLAYER_COLORS.add(player3Colors);
         PLAYER_COLORS.add(player4Colors);
     }
     private PlayerControl control = new HumanPlayerControl();
     private List<Person> persons = new ArrayList<Person>();
     private CacheableMapValue<Job, CacheableMapValue<Job.GameAction, SpriteAnimation>> jobAnimations = new CacheableMapValue<Job, CacheableMapValue<GameAction, SpriteAnimation>>() {
 
         @Override
         protected CacheableMapValue<GameAction, SpriteAnimation> calculate(final Job job) {
             return new CacheableMapValue<GameAction, SpriteAnimation>() {
 
                 @Override
                 protected SpriteAnimation calculate(GameAction gameAction) {
                     SpriteAnimation spriteAction;
                     switch (gameAction) {
                         case SELECTED:
                             spriteAction = getSelectedSpriteAnimation(job);
                             break;
 
                         case USED:
                             spriteAction = getUsedSpriteAnimation(job);
                             break;
 
                         default:
                             spriteAction = job.getSpriteActionGroup().getSpriteAction(gameAction);
                     }
 
                     SpriteAnimation playerJobAnimation = new SpriteAnimation();
                     playerJobAnimation.setChangeFrameInterval(spriteAction.getChangeFrameInterval());
 
                     for (GameImage image : spriteAction.getImages()) {
                         playerJobAnimation.addImage(getImage(job.getSpriteActionGroup(), image));
                     }
 
                     return playerJobAnimation;
                 }
             };
         }
     };
 
     public Player(String name, int index) {
         super(name);
         this.index = index;
     }
 
     private GameImage getImage(JobSpriteActionGroup jobSpriteActionGroup, GameImage spriteImage) {
         return images.getValue(jobSpriteActionGroup).getValue(spriteImage);
     }
 
     public List<Person> getPersons() {
         return persons;
     }
 
     private SpriteAnimation getSelectedSpriteAnimation(Job job) {
         return selectedAnimations.getValue(job);
     }
 
     private SpriteAnimation getUsedSpriteAnimation(Job job) {
         return usedAnimations.getValue(job);
     }
 
     public SpriteAnimation getSpriteAnimation(Job job, Job.GameAction gameAction) {
         return jobAnimations.getValue(job).getValue(gameAction);
     }
 
     public java.awt.Color getDefaultColor() {
         return getColors().getDefault();
     }
 
     private PlayerColors getColors() {
         return PLAYER_COLORS.get(index);
     }
 
     /**
      * @return the control
      */
     public PlayerControl getControl() {
         return control;
     }
 
     public static enum Color {
 
         MAIN,
         SECUNDARY,
     }
 
     public static class PlayerColors {
 
         private static class MaskedColor {
 
             private int min;
             private int max;
 
             public MaskedColor(int min, int max) {
                 this.min = min;
                 this.max = max;
             }
 
             public int getMin() {
                 return min;
             }
 
             public int getMax() {
                 return max;
             }
 
             public int getColor(float factor, float minLimit, float maxLimit) {
                 return ColorUtil.getBetweenColor(min, max, calculateRealFactor(factor, minLimit, maxLimit)).getRGB();
             }
 
             private static float calculateRealFactor(float factor, float minLimit, float maxLimit) {
                 return factor * (maxLimit - minLimit) + minLimit;
             }
         }
         private java.util.Map<Color, MaskedColor> mapping = new TreeMap<Color, MaskedColor>();
         private java.util.Map<GameImage, CacheableValue<GameImage>> maskedImages =
                 new HashMap<GameImage, CacheableValue<GameImage>>();
         private CacheableMapValue<JobSpriteActionGroup, CacheableMapValue<Integer, Integer>> jobsColors =
                 new CacheableMapValue<JobSpriteActionGroup, CacheableMapValue<Integer, Integer>>() {
 
                     @Override
                     protected CacheableMapValue<Integer, Integer> calculate(final JobSpriteActionGroup jobSpriteActionGroup) {
                         return new CacheableMapValue<Integer, Integer>() {
 
                             @Override
                             protected Integer calculate(Integer originalRgb) {
                                 Player.Color playerColor = jobSpriteActionGroup.getMapping(originalRgb);
                                 if (playerColor != null) {
                                     return mapping.get(playerColor).getColor(
                                             ColorUtil.getBetweenFactor(
                                             jobSpriteActionGroup.getPlayerColorMin(playerColor),
                                             jobSpriteActionGroup.getPlayerColorMax(playerColor),
                                             originalRgb),
                                             jobSpriteActionGroup.getColorMappingMin(),
                                             jobSpriteActionGroup.getColorMappingMax());
                                 } else {
                                     return originalRgb;
                                 }
                             }
                         };
                     }
                 };
 
         public void setMapping(Color playerColor, int minRgb, int maxRgb) {
             mapping.put(playerColor, new MaskedColor(minRgb, maxRgb));
         }
 
         public GameImage getMaskedImage(final JobSpriteActionGroup jobSpriteActionGroup, final GameImage image) {
             CacheableValue<GameImage> cachedMaskedImage = maskedImages.get(image);
 
             if (cachedMaskedImage == null) {
                 cachedMaskedImage = new CacheableValue<GameImage>() {
 
                     @Override
                     protected GameImage calculate() {
                         final GameImage maskedImage = image.clone();
 
                         new PixelImageIterator(image.getImage()) {
 
                             @Override
                             public void iterate(int x, int y, int rgb) {
                                 maskedImage.getImage().setRGB(x, y, jobsColors.getValue(jobSpriteActionGroup).getValue(rgb));
                             }
                         };
 
                         return maskedImage;
                     }
                 };
                 maskedImages.put(image, cachedMaskedImage);
             }
 
             return cachedMaskedImage.getValue();
         }
 
         public java.awt.Color getDefault() {
             return new java.awt.Color(mapping.get(Color.MAIN).getColor(0.5f, 0.0f, 1.0f));
         }
     }
 }
