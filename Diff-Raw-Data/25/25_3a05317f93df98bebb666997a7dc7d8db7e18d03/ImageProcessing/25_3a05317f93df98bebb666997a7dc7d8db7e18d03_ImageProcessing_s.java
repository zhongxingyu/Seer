 package ru.bmstu.diplom;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 
 import com.googlecode.javacv.cpp.opencv_core.CvMat;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
 
 /**
 * Песочница. 
  * Базовый класс для загрузки, отображения и обработки изображения, построенный на JavaCV
  * 
 * Погрешность принимаем равное 25 единицам (получено экспериментальным путем).TODO: check??
 * 
  * @author Butenko Victor, BMSTU, Spring 2013
  */
 public  class ImageProcessing {
 	
 	private CvMat image;  // Изображение для обработки, экземпляр класса JavaCV
 
 	
 	//Конструктор  ( Инициализация) 
 	public ImageProcessing(IplImage thisImage) {
 		image = thisImage.asCvMat();
 	}
 	
 
 	/**
 	 * Метод выделяет красным цветом область на изображении,
 	 * ограниченную параметрами.
 	 */
 	public void allocatePart(int x1, int y1, int x2, int y2) {
 		// Цикл по всем строкам (i), столбцам (j)			
 		for (int i = 0; i < image.rows(); i ++) {
 			for ( int j = 0; j < image.cols(); j ++ ) {
 				// Выбираем целевые пиксели. 
 				if((i >= x1) && (i <= x2))
 					if ((j >= y1) && (j <= y2)) 
 						image.put(j, i, 2, 255); // Note! Координаты черед-ся (y,x). FIXME:
 				}
 			}
 	}
 	
 	/**
 	 * Метод выделяет красным цветом область на изображении,
 	 * ограниченную параметрами.
 	 * 
 	 * 1) Берем кусок дороги
      * 2) Считаем для него по всем точкам RBGср. 
 	 * 3) Запоминаем крайние точки. По ним считается погрешность E. 
 	 * 4) Проводим попиксельный просмотр, и если R, G и B попадает в диапазон E, 
 	 *    то перекрашиваем пиксель в красный цвет
 	 */
 	public void findArea(int x1, int y1, int x2, int y2, int error) {
 
 		int blueAvrg  = 0;
 		int greenAvrg = 0;
 		int redAvrg   = 0;
 		int currColorBlue = 0, currColorGreen = 0, currColorRed = 0;
 		int countPixels = (x2 - x1) * (y2 - y1); //Кол-во пикселей в данной области
 		System.out.println("pixels: " + (x2-x1) + " * " + (y2-y1) + " = " + countPixels);
 		
 		// Цикл по всем строкам (i), столбцам (j), и цветам (c)				
 		for (int i = 0; i < image.rows(); i ++) {
 			for ( int j = 0; j < image.cols(); j ++ ) {
 				
 				// Выбираем целевые пиксели. 
 				if((i >= x1) && (i <= x2))
 					if ((j >= y1) && (j <= y2)) {		
 						// Вытаскиваем текущие значения пикселей в RGB
 						currColorBlue  = (int) image.get(j, i, 0);
 						currColorGreen = (int) image.get(j, i, 1);
 						currColorRed   = (int) image.get(j, i, 2);
 						
 						//Накапливаем сумму значений цветовых компонент 
 						//для вычисления среднего значения
 						blueAvrg  += currColorBlue;
 						greenAvrg += currColorGreen;
 						redAvrg   += currColorRed;
 					}						
 				}
 			}
 		// Посчитать среднее значение каждой компоненты RGB 
 		if (countPixels != 0) {
 			blueAvrg  = blueAvrg / countPixels ;
 			greenAvrg = greenAvrg / countPixels;
 			redAvrg   = redAvrg   / countPixels;
 		} else {
 			System.err.println("Error! An empty area !!!");
 		}
 		System.out.println("blue average : " + blueAvrg + 
 			   "; green average : " + greenAvrg  + "; Red Average :  " + redAvrg);
 		
 		// Разрисовка пикселей. 
 		paintArea(blueAvrg, greenAvrg, redAvrg, error);
 	}
 	
 	/**
 	 * Вспомогательный метод. Производит попиксельный проход по изображению, 
 	 * проверяя цвет пикселя на принадлежность к усредненному диапазону значений 
 	 * для каждой цветовой компоненты. В случае успеха приравнивает компоненту 
 	 * Red этого пикселя к 255. (Разукрашивает дорогу в красный цвет)
 	 * 
 	 * @param blueAvrg
 	 * @param greenAvrg
 	 * @param redAvrg
 	 */
 	private void paintArea(int blueAvrg, int greenAvrg, int redAvrg, int error) {
 
 		int blueColor, greenColor, redColor;
 				
 		//К усредненым значениям добавляем погрешность error
 		int blueMin = blueAvrg - error;
 		int blueMax = blueAvrg + error;
 		int greenMin = greenAvrg - error;
 		int greenMax = greenAvrg + error;
 		int redMin = redAvrg - error;
 		int redMax = redAvrg + error;
 		
 		// Цикл по всем строкам (i), столбцам (j) 
         for (int i = 0; i < image.rows(); i++) {
         	for (int j = 0; j < image.cols(); j++) {
         		//Элементы проходим в порядке (y,x). (Не влияет на результат)
         		blueColor = (int) image.get(i, j, 0);
         		greenColor = (int) image.get(i, j, 1);
         		redColor =   (int) image.get(i, j , 2);
         		
         		// Если цвет пикселя попадает в диапазон значений с погрешностью 
         		if ( (blueColor < blueMax )  && (blueColor > blueMin)    &&
         			 (greenColor < greenMax) && (greenColor > greenMin)  &&
         			 (redColor < redMax)     && (redColor > redMin) ) {
         				image.put(i, j, 2, 255);  //Покрасить дорогу в красный цвет.
         				}
         		}
         	}
         }
 
 
 	/**
 	 * Простов вывод матричного представления изображения в файл.
 	 * Сохраняем вывод в файл. Формат следующий ( y,x,c) - 
 	 * y, x - координаты, c - цвет (BGR  - Blue, Green, Red (0..255))
 	 * |y11, y12, y13, .., y1N |
 	 * |y21, y22, y23, .., y2N |
 	 * |...................    |
 	 * |yN1, yN2, yN3, .., yNN |
 	 * где y(i,j) = [0..255][0..255][0..255] - BGR
 	 * 
 	 * Кроме того, вывод цифровых значений в этом методе
 	 * осуществляется в файл. Для этого перенаправляем 
 	 * поток стандартного ввода (System.out) в текстовый файл.
 	 *  
 	 */
 	public void doMatrix() {
 		
 		 //Создать текстовый файл для вывода
 		 //Перенаправить вывод из консоли в файл.
 		 try {
 			System.setOut(new PrintStream(new FileOutputStream(new File("out.txt"))));
 		} catch (FileNotFoundException e) {
 			System.err.println("Нужно создать файл out.txt!!!");
 			e.printStackTrace();
 		}
 
 		 //Обрабатываем изображение как матрицу
 		for(int i = 0; i < image.rows(); i++) {
 			System.out.println("");
 			for ( int j = 0; j < image.cols(); j++) {
 				System.out.print("[ ");
 				for (int c = 0; c < 3; c++) {
 					System.out.print(image.get(i,j,c) + ", ");
 				}
 				System.out.print("]");
 			}
 		}
 		//Перенаправить стандартный вывод назад в консоль.
 		System.setOut(System.out); 
 	}
 	
 }
