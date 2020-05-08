 /*
  * Blend.java
  * 
  * Copyright (C) 2013  Pavel Prokhorov (pavelvpster@gmail.com)
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
 package image.processing;
 
 import image.AbstractImageProcessor;
 
 import image.ImageProcessor;
 
 import image.blend.BlendOperation;
 
 import image.exceptions.BadParametersException;
 import image.exceptions.NoSourceImageException;
 import image.exceptions.UnsupportedImageTypeException;
 
 import image.util.ImageFactory;
 import image.util.ImageLoader;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBuffer;
 import java.awt.image.DataBufferByte;
 import java.awt.image.DataBufferInt;
 
 /**
  * Смешивание изображений.
  * 
  * @author pavelvpster
  * 
  */
 public final class Blend extends AbstractImageProcessor implements ImageProcessor {
 	
 	/**
 	 * Конструктор по умолчанию.
 	 * 
 	 */
 	public Blend() {
 	}
 	
 	// Параметризованные конструкторы
 
 	public Blend(BlendOperation operation) {
 		
 		this.operation = operation;
 	}
 
 	public Blend(BlendOperation operation, BufferedImage sourceImage) {
 
 		super(sourceImage);
 		
 		this.operation = operation;
 	}
 
 	public Blend(BlendOperation operation, BufferedImage sourceImage, BufferedImage secondSourceImage) {
 
 		super(sourceImage);
 		
 		this.operation = operation;
 
 		this.secondSourceImage = secondSourceImage;
 	}
 	
 	public Blend(BlendOperation operation, String secondSourceImageFilename) {
 		
 		this.operation = operation;
 
 		this.secondSourceImage = ImageLoader.load(secondSourceImageFilename);
 	}
 	
 	
 	/**
 	 * Операция смешивания компонент цвета.
 	 * 
 	 */
 	private BlendOperation operation = null;
 
 	/**
 	 * Этот метод возвращает операцию смешивания компонент цвета.
 	 * 
 	 * @return BlendOperation
 	 * 
 	 */
 	public BlendOperation getOperation() {
 		
 		return operation;
 	}
 	
 	/**
 	 * Этот метод устанавливает операцию смешивания компонент цвета.
 	 * 
 	 * @param operation операция смешивания.
 	 * 
 	 */
 	public void setOperation(BlendOperation operation) {
 		
 		this.operation = operation;
 	}
 	
 
 	/**
 	 * Второе исходное изображение.
 	 * 
 	 */
 	private BufferedImage secondSourceImage = null;
 	
 	/**
 	 * Этот метод возвращает второе исходное изображение.
 	 * 
 	 * @return BufferedImage
 	 * 
 	 */
 	public BufferedImage getSecondSourceImage() {
 		
 		return secondSourceImage;
 	}
 	
 	/**
 	 * Этот метод устанавливает второе исходное изображение.
 	 * 
 	 * @param secondSourceImage второе исходное изображение.
 	 * 
 	 */
 	public void setSecondSourceImage(BufferedImage secondSourceImage) {
 		
 		this.secondSourceImage = secondSourceImage;
 	}
 
 
 	/**
	 * @see IImageProcessor
 	 *
 	 */
 	@Override
 	public void process() {
 
 		if (operation == null) {
 
 			throw new BadParametersException("Blend operation");
 		}
 
 		if (sourceImage == null) {
 
 			throw new NoSourceImageException();
 		}
 
 		if (secondSourceImage == null) {
 
 			throw new NoSourceImageException();
 		}
 
 		// Создаем обработанное изображение
 
 		processedImage = ImageFactory.createImageAs(sourceImage);
 		
 		// Получаем данные исходного изображения
 
 		DataBuffer sourceBuffer = sourceImage.getRaster().getDataBuffer();
 
 		byte[] sourceImageData = null; int[] sourceImageDataInt = null;
 
 		if (sourceBuffer.getDataType() == DataBuffer.TYPE_BYTE) {
 
 			sourceImageData = ((DataBufferByte)sourceBuffer).getData();
 			
 		} else
 
 		if (sourceBuffer.getDataType() == DataBuffer.TYPE_INT) {
 
 			sourceImageDataInt = ((DataBufferInt)sourceBuffer).getData();
 			
 		} else {
 
 			throw new UnsupportedImageTypeException();
 		}
 
 		// Получаем данные второго исходного изображения
 
 		DataBuffer secondSourceBuffer = secondSourceImage.getRaster().getDataBuffer();
 
 		byte[] secondSourceImageData = null; int[] secondSourceImageDataInt = null;
 
 		if (secondSourceBuffer.getDataType() == DataBuffer.TYPE_BYTE) {
 
 			secondSourceImageData = ((DataBufferByte)secondSourceBuffer).getData();
 			
 		} else
 
 		if (secondSourceBuffer.getDataType() == DataBuffer.TYPE_INT) {
 
 			secondSourceImageDataInt = ((DataBufferInt)secondSourceBuffer).getData();
 			
 		} else {
 
 			throw new UnsupportedImageTypeException();
 		}
 
 		// Получаем данные обработанного изображения
 
 		DataBuffer processedBuffer = processedImage.getRaster().getDataBuffer();
 
 		byte[] processedImageData = null; int[] processedImageDataInt = null;
 
 		if (processedBuffer.getDataType() == DataBuffer.TYPE_BYTE) {
 
 			processedImageData = ((DataBufferByte)processedBuffer).getData();
 			
 		} else
 
 		if (processedBuffer.getDataType() == DataBuffer.TYPE_INT) {
 
 			processedImageDataInt = ((DataBufferInt)processedBuffer).getData();
 			
 		} else {
 
 			throw new UnsupportedImageTypeException();
 		}
 
 		// Blend
 
 		for (int y = 0, i = 0, j = 0; y < sourceImage.getHeight(); y ++) {
 
 			for (int x = 0; x < sourceImage.getWidth(); x ++) {
 
 				// Сохраняем положение в данных исходного изображения
 
 				int k = i;
 
 				// Получаем компоненты цвета RGB исходного изображения
 
 				int r1, g1, b1;
 
 				switch (sourceImage.getType()) {
 
 					case BufferedImage.TYPE_INT_RGB:
 						
 						int c = sourceImageDataInt[ i ++ ];
 
 						r1 = (c & 0x00FF0000) >> 16;
 						g1 = (c & 0x0000FF00) >> 8;
 						b1 = (c & 0x000000FF);
 
 						break;
 						
 					case BufferedImage.TYPE_3BYTE_BGR:
 
 						b1 = sourceImageData[ i ++ ] & 0xFF;
 						g1 = sourceImageData[ i ++ ] & 0xFF;
 						r1 = sourceImageData[ i ++ ] & 0xFF;
 
 						break;
 
 					case BufferedImage.TYPE_BYTE_GRAY:
 
 						r1 = g1 = b1 = sourceImageData[ i ++ ] & 0xFF;
 
 						break;
 
 					default:
 
 						throw new UnsupportedImageTypeException();
 				}
 
 				// Получаем компоненты цвета RGB маски
 
 				int r2 = 255, g2 = 255, b2 = 255;
 
 				if (x < secondSourceImage.getWidth() && y < secondSourceImage.getHeight()) {
 					
 					switch (secondSourceImage.getType()) {
 
 						case BufferedImage.TYPE_INT_RGB:
 
 							int c = secondSourceImageDataInt[ j ++ ];
 
 							r2 = (c & 0x00FF0000) >> 16;
 							g2 = (c & 0x0000FF00) >> 8;
 							b2 = (c & 0x000000FF);
 
 							break;
 
 						case BufferedImage.TYPE_3BYTE_BGR:
 
 							b2 = secondSourceImageData[ j ++ ] & 0xFF;
 							g2 = secondSourceImageData[ j ++ ] & 0xFF;
 							r2 = secondSourceImageData[ j ++ ] & 0xFF;
 
 							break;
 
 						case BufferedImage.TYPE_BYTE_GRAY:
 
 							r2 = g2 = b2 = secondSourceImageData[ j ++ ] & 0xFF;
 
 							break;
 
 						default:
 
 							throw new UnsupportedImageTypeException();
 					}
 				}
 
 				// Смешиваем цвета
 
 				int r = operation.process(r1, r2);
 				int g = operation.process(g1, g2);
 				int b = operation.process(b1, b2);
 				
 				// Сохраняем результат
 				
 				switch (processedImage.getType()) {
 
 					case BufferedImage.TYPE_INT_RGB:
 
 						int c = 0;
 
 						c |= (r << 16) & 0x00FF0000;
 						c |= (g << 8 ) & 0x0000FF00;
 						c |= (b      ) & 0x000000FF;
 
 						processedImageDataInt[ k ++ ] = c;
 
 						break;
 
 					case BufferedImage.TYPE_3BYTE_BGR:
 
 						processedImageData[ k ++ ] = (byte)b;
 						processedImageData[ k ++ ] = (byte)g;
 						processedImageData[ k ++ ] = (byte)r;
 
 						break;
 
 					case BufferedImage.TYPE_BYTE_GRAY:
 
 						processedImageData[ k ++ ] = (byte)r;
 
 						break;
 
 					default:
 
 						throw new UnsupportedImageTypeException();
 				}
 
 			}
 		}
 	}
 
 }
