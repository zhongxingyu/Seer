 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.services.captcha;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageFilter;
 import java.util.Locale;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 import com.jhlabs.image.WaterFilter;
 import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
 import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
 import com.octo.captcha.component.image.color.SingleColorGenerator;
 import com.octo.captcha.component.image.deformation.ImageDeformation;
 import com.octo.captcha.component.image.deformation.ImageDeformationByFilters;
 import com.octo.captcha.component.image.fontgenerator.FontGenerator;
 import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
 import com.octo.captcha.component.image.textpaster.DecoratedRandomTextPaster;
 import com.octo.captcha.component.image.textpaster.TextPaster;
 import com.octo.captcha.component.image.textpaster.textdecorator.BaffleTextDecorator;
 import com.octo.captcha.component.image.textpaster.textdecorator.TextDecorator;
 import com.octo.captcha.component.image.wordtoimage.DeformedComposedWordToImage;
 import com.octo.captcha.component.image.wordtoimage.WordToImage;
 import com.octo.captcha.component.word.FileDictionary;
 import com.octo.captcha.component.word.wordgenerator.ComposeDictionaryWordGenerator;
 import com.octo.captcha.component.word.wordgenerator.WordGenerator;
 import com.octo.captcha.engine.CaptchaEngine;
 import com.octo.captcha.engine.image.ListImageCaptchaEngine;
 import com.octo.captcha.image.gimpy.GimpyFactory;
 import com.octo.captcha.service.CaptchaServiceException;
 import com.octo.captcha.service.captchastore.FastHashMapCaptchaStore;
 import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
 import com.octo.captcha.service.image.ImageCaptchaService;
 
 import de.cosmocode.palava.core.Service;
 import de.cosmocode.palava.core.lifecycle.Initializable;
 
 /**
  * A {@link Service} which provides captcha methods.
  *
  * @author Willi Schoenborn
  */
 @Singleton
 final class CaptchaService implements Service, ImageCaptchaService, Initializable {
     
     @Inject(optional = true)
     @Named("captcha.amplitude")
     private double amplitude = 3d;
 
     @Inject(optional = true)
     @Named("captcha.antialias")
     private boolean antialias = true;
 
     @Inject(optional = true)
     @Named("captcha.phase")
     private double phase = 20d;
 
     @Inject(optional = true)
     @Named("captcha.wavelength")
     private double wavelength = 70d;
     
     private final Color textColor = Color.black;
     
     private Color bgColor = Color.white;
 
     @Inject(optional = true)
     @Named("captcha.width")
     private int width = 200;
 
     @Inject(optional = true)
     @Named("captcha.height")    
     private int height = 100;
 
     @Inject(optional = true)
     @Named("captcha.fontMinHeight")
     private int fontMinHeight = 30;
 
     @Inject(optional = true)
     @Named("captcha.fontMaxHeight")
     private int fontMaxHeight = 35;
 
     @Inject(optional = true)
     @Named("captcha.numberOfHoles")
     private int numberOfHoles = 1;
 
     private final int minGuarantedStorageDelayInSeconds = 180;
     
     private final int maxCaptchaStoreSize = 100000;
     
     private final int captchaStoreLoadBeforeGarbageCollection = 75000;
 
     private final CaptchaEngine engine = new ListImageCaptchaEngine() {
         
         @Override
         protected void buildInitialFactories() {
             addFactory(createFactory());
         }
         
     };
 
     private ImageCaptchaService service;
     
     @Override
     public void initialize() {
         engine.setFactories(new GimpyFactory[] {
             createFactory()
         });
         service = new DefaultManageableImageCaptchaService(
             new FastHashMapCaptchaStore(),
             engine,
             minGuarantedStorageDelayInSeconds,
             maxCaptchaStoreSize,
             captchaStoreLoadBeforeGarbageCollection
         );
     }
     
     private GimpyFactory createFactory() {
         final WaterFilter water = new WaterFilter();
 
         water.setAmplitude(amplitude);
         water.setAntialias(antialias);
         water.setPhase(phase);
         water.setWavelength(wavelength);
 
         final ImageDeformation backDef = new ImageDeformationByFilters(new ImageFilter[] {});
         final ImageDeformation textDef = new ImageDeformationByFilters(new ImageFilter[]{});
         final ImageDeformation postDef = new ImageDeformationByFilters(new ImageFilter[]{water});
 
         //word generator
         final WordGenerator dictionnaryWords = new ComposeDictionaryWordGenerator(new FileDictionary("toddlist"));
         
         //wordtoimage components
         final TextPaster randomPaster = new DecoratedRandomTextPaster(
             6, 7, 
             new SingleColorGenerator(textColor),
             new TextDecorator[] {
                 new BaffleTextDecorator(numberOfHoles, bgColor)
             }
         );
         
         final BackgroundGenerator back = new UniColorBackgroundGenerator(
             width, height, Color.white
         );
 
         final FontGenerator shearedFont = new RandomFontGenerator(fontMinHeight, fontMaxHeight);
         
         //word2image 1
         final WordToImage word2image = new DeformedComposedWordToImage(
                 shearedFont, 
                 back, 
                 randomPaster,
                 backDef,
                 textDef,
                 postDef
         );
 
         return new GimpyFactory(dictionnaryWords, word2image);
     }
 
     @Override
     public Object getChallengeForID(String id, Locale locale) throws CaptchaServiceException {
         return service.getChallengeForID(id, locale);
     }
 
     @Override
     public Object getChallengeForID(String id) throws CaptchaServiceException {
         return service.getChallengeForID(id);
     }
 
     @Override
     public BufferedImage getImageChallengeForID(String id, Locale locale) throws CaptchaServiceException {
         return service.getImageChallengeForID(id, locale);
     }
 
     @Override
     public BufferedImage getImageChallengeForID(String id) throws CaptchaServiceException {
         return service.getImageChallengeForID(id);
     }
 
     @Override
     public String getQuestionForID(String id, Locale locale) throws CaptchaServiceException {
         return service.getQuestionForID(id, locale);
     }
 
     @Override
     public String getQuestionForID(String id) throws CaptchaServiceException {
         return service.getQuestionForID(id);
     }
 
     @Override
     public Boolean validateResponseForID(String id, Object input) throws CaptchaServiceException {
         return service.validateResponseForID(id, input);
     }
     
 }
