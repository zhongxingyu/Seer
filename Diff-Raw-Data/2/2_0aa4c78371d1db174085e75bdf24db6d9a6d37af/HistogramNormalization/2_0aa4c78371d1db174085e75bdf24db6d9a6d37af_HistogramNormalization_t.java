 /*
  * HistogramNormalization.java
  * 
  * Copyright (C) 2012  Pavel Prokhorov (pavelvpster@gmail.com)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package image.processing.experimental;
 
 import image.AbstractImageProcessor;
 
 import image.ImageProcessor;
 
 import image.color.RGB;
 
 import java.awt.image.BufferedImage;
 
 /**
  * Нормализация гистограммы.
  *
  * @author pavelvpster
  * 
  */
 public class HistogramNormalization extends AbstractImageProcessor implements ImageProcessor {
 
 	/**
 	 * Конструктор по умолчанию.
 	 * 
 	 */
 	public HistogramNormalization() {
 	}
 
 	// Параметризованные конструкторы
 
 	public HistogramNormalization(BufferedImage sourceImage) {
 
 		super(sourceImage);
 	}
 
 	
 	/**
 	 * @see IImageProcessor
 	 *
 	 */
 	@Override
 	public void process() {
 
 		if (sourceImage == null) {
 
 			throw new RuntimeException("Source image undefined!");
 		}
 		
 		// Для нормализации нам понадобятся интенсивности точек
 		
 		Grayscale grayscale = new Grayscale(sourceImage);
 		
 		grayscale.process();
 		
 		BufferedImage intensityImage = grayscale.getProcessedImage();
 		
 		// Строим гистограмму интенсивностей
 		
 		int[] histogram = new int [256];
 		
 		for (int y = 0; y < intensityImage.getHeight(); y ++) {
 			
 			for (int x = 0; x < intensityImage.getWidth(); x ++) {
 				
 				RGB a = RGB.get(intensityImage, x, y);
 				
 				histogram[ a.I ] ++ ;
 			}
 		}
 		
 		// Строим CDF
 		
 		int[] CDF = new int [256];
 		
 		int sum = 0;
 		
 		for (int i = 0; i < 256; i ++) {
 			
 			sum += histogram[i];
 			
 			CDF[i] = sum;
 		}
 		
 		// Расчитываем минимум и максимум
 
 		int min = Integer.MAX_VALUE;
 		int max = Integer.MIN_VALUE;
 		
 		for (int i = 0; i < 256; i ++) {
 			
 			if (CDF[i] != 0) {
 				
 				if (CDF[i] < min) min = CDF[i];
 				if (CDF[i] > max) max = CDF[i];
 			}
 		}
 		
 		// Нормализуем гистограмму
 		
 		int[] map = new int [256];
 		
 		double s = intensityImage.getWidth() * intensityImage.getHeight();
 		
 		for (int i = 0; i < 256; i ++) {
 			
			map[i] = (int)(((double)(CDF[i] - min) / (s - min)) * 255.0);
 		}
 		
 		// Создаем обработанное изображение
 		
 		processedImage = new BufferedImage
 				(sourceImage.getWidth(), sourceImage.getHeight(), sourceImage.getType());
 		
 		// Color Remapping
 		
 		for (int y = 0; y < processedImage.getHeight(); y ++) {
 			
 			for (int x = 0; x < processedImage.getWidth(); x ++) {
 				
 				RGB a = RGB.get(sourceImage, x, y);
 
 				int i = RGB.get(intensityImage, x, y).I;
 				
 				int c = map[i];
 				
 				double f = (double)c / (double)i;
 				
 				a.R *= f;
 				a.G *= f;
 				a.B *= f;
 				
 				a.clamp();
 
 				a.set(processedImage, x, y);
 			}
 		}
 	}
 
 }
