 /*
  *  Copyright (C) 2012 The Animo Project
  *  http://animotron.org
  *
  *  This file is part of Animotron.
  *
  *  Animotron is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of
  *  the License, or (at your option) any later version.
  *
  *  Animotron is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of
  *  the GNU Affero General Public License along with Animotron.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.animotron.animi.cortex;
 
 import org.animotron.animi.InitParam;
 
 /**
  * Projection description of the one zone to another
  * 
  * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
  * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  */
 public class Mapping {
 	public CortexZoneSimple zone;       // Projecting zone
 	
 	@InitParam(name="ns_links")
     public int ns_links;           // Number of synaptic connections for the zone
     
 	/** дисперсия связей **/
 	@InitParam(name="disp")
 	public double disp;      // Describe a size of sensor field
 
 	@InitParam(name="soft")
 	public boolean soft = true;
 
 	Mapping () {}
 	
     public Mapping(CortexZoneSimple zone, int ns_links, double disp, boolean soft) {
         this.zone = zone;
         this.disp = disp;
         this.ns_links = ns_links;
         this.soft = soft;
     }
 
     public String toString() {
     	return "mapping "+zone.toString();
     }
 
 	// Связи распределяются случайным образом.
 	// Плотность связей убывает экспоненциально с удалением от колонки.
 	public void map(CortexZoneComplex z) {
         for (int x = 0; x < zone.width(); x++) {
 			for (int y = 0; y < zone.height(); y++) {
 				zone.col[x][y].a_links.clear();
 				zone.col[x][y].a_Qs.clear();
 			}
         }
 
 		double fX = zone.width() / (double) z.width();
 		double fY = zone.height() / (double) z.height();
 
         double X, Y, S;
 		double x_in_nerv, y_in_nerv;
         double _sigma, sigma;
 
         boolean[][] nerv_links = new boolean[zone.width()][zone.height()];
 
         for (int x = 0; x < z.width(); x++) {
 			for (int y = 0; y < z.height(); y++) {
 //				System.out.println("x = "+x+" y = "+y);
 
 				// Определение координат текущего нейрона в масштабе
 				// проецируемой зоны
 				x_in_nerv = x * zone.width() / (double) z.width();
 				y_in_nerv = y * zone.height() / (double) z.height();
 //				System.out.println("x_in_nerv = "+x_in_nerv+" y_in_nerv = "+y_in_nerv);
 
                 _sigma = disp;// * ((m.zone.width() + m.zone.height()) / 2);
                 sigma = _sigma;
 
 				// Обнуление массива занятости связей
 				for (int n1 = 0; n1 < zone.width(); n1++) {
 					for (int n2 = 0; n2 < zone.height(); n2++) {
 						nerv_links[n1][n2] = false;
 					}
 				}
 
 				// преобразование Бокса — Мюллера для получения
 				// нормально распределенной величины
 				// DispLink - дисперсия связей
 				int count = 0;
 				for (int i = 0; i < ns_links; i++) {
                     int lx, ly;
                     do {
                         do {
                             if (count > ns_links * 3) {
                             	if (Double.isInfinite(sigma)) {
                             		System.out.println("initialization failed @ x = "+x+" y = "+y);
                             		System.exit(1);
                             	}
                            	sigma *= 0.05;//_sigma * 0.1;
     							System.out.println("\n"+i+" of "+ns_links+" ("+sigma+")");
                             	count = 0;
                             }
                             count++;
                             	
                             do {
                                 X = 2.0 * Math.random() - 1;
                                 Y = 2.0 * Math.random() - 1;
                                 S = X * X + Y * Y;
                             } while (S > 1 || S == 0);
                             S = Math.sqrt(-2 * Math.log(S) / S);
                             double dX = X * S * sigma;
                             double dY = Y * S * sigma;
                             lx = (int) Math.round(x_in_nerv + dX);
                             ly = (int) Math.round(y_in_nerv + dY);
 
                             //определяем, что не вышли за границы поля колонок
                             //колонки по периметру не задействованы
                         } while (!(soft || (lx >= 1 && ly >= 1 && lx < zone.width() - 1 && ly < zone.height() - 1)));
 
                     // Проверка на повтор связи
 					} while (
 							(!soft || (lx >= 1 && ly >= 1 && lx < zone.width() - 1 && ly < zone.height() - 1))
 							&& nerv_links[lx][ly]
 						);
 
                     System.out.print(".");
                     if (lx >= 1 && ly >= 1 && lx < zone.width() - 1 && ly < zone.height() - 1) {
 						nerv_links[lx][ly] = true;
 	
 						// Создаем синаптическую связь
 						new LinkQ(zone.getCol(lx, ly), z.col[x][y], 1 / (double)ns_links, fX, fY);
                     }
 				}
 				System.out.println();
 			}
 		}
 	}
 }
