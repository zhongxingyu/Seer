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
 
 import org.animotron.animi.Imageable;
 import org.animotron.animi.Params;
 import org.animotron.animi.Utils;
 import org.animotron.animi.acts.Activation;
 import org.animotron.animi.acts.Act;
 import org.animotron.animi.acts.Recognition;
 import org.animotron.animi.acts.Remember;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 
 /**
  * Complex cortex zone
  * 
  * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
  * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  */
 public class CortexZoneComplex extends CortexZoneSimple {
 
 	@Params
     private Activation activation = new Activation(0.6, 0.6);
 
 	@Params
     private Recognition recognition = new Recognition(0.1);
 
 	@Params
     private Remember remember = new Remember(0.05, 10, 2);
 	
     Mapping[] in_zones;
 	public int deep;
 	
 	/** Number of synaptic connections of the all simple neurons **/
 	public int ns_links;
 	/** Number of axonal connections of the all simple neurons **/
 	public int nas_links = 9;
 	/** Number of synaptic connections of the complex neuron **/
 	public int nsc_links;
 	/** Memory **/
 	public NeuronSimple[][][] s;
 
     CortexZoneComplex(String name, MultiCortex mc, int deep, Mapping[] in_zones) {
 
 		super(name, mc);
 		this.in_zones = in_zones;
 		this.deep = deep;
     }
     
     public void init() {
     	super.init();
     	
 		this.s = new NeuronSimple[width()][height()][deep];
 
 		this.ns_links = 0;
         for (Mapping i : in_zones) {
             this.ns_links += i.ns_links;
 		}
 
 		this.nsc_links = nas_links * deep;
 
 //	    activation = new Activation(0.6, 0.6);
 //	    recognition = new Recognition(0.1);
 //	    remember = new Remember(0.05, 10, 2);
 
 	    // Инициализация синаптических связей простых нейронов
 		for (int x = 0; x < width(); x++) {
 			for (int y = 0; y < height(); y++) {
 				for (int z = 0; z < deep; z++) {
 
 					NeuronSimple sn = s[x][y][z] = new NeuronSimple();
 					sn.s_links = new Link2dZone[ns_links];
 					for (int i = 0; i < ns_links; i++)
 						sn.s_links[i] = new Link2dZone();
 
 					sn.a_links = new Link2d[nas_links];
 					for (int i = 0; i < nas_links; i++)
 						sn.a_links[i] = new Link2d();
 
 					sn.occupy = sn.active = false;
 					sn.n1 = sn.n2 = 0;
 				}
 			}
 		}
 
 		// Создание синаптических связей симпл нейронов.
 		// Связи распределяются случайным образом.
 		// Плотность связей убывает экспоненциально с удалением от колонки.
 		double x_in_nerv, y_in_nerv;
 
 		for (Mapping m : in_zones) {
 
             boolean[][] nerv_links = new boolean[m.zone.width()][m.zone.height()];
 
 			for (int x = 0; x < width(); x++) {
 				for (int y = 0; y < height(); y++) {
 
 					// Определение координат текущего нейрона в масштабе
 					// проецируемой зоны
 					x_in_nerv = x * m.zone.width() / (double) width();
 					y_in_nerv = y * m.zone.height() / (double) height();
 
 					for (int z = 0; z < deep; z++) {
 						// Обнуление массива занятости связей
 						for (int n1 = 0; n1 < m.zone.width(); n1++) {
 							for (int n2 = 0; n2 < m.zone.height(); n2++) {
 								nerv_links[n1][n2] = false;
 							}
 						}
 
 						// преобразование Бокса — Мюллера для получения
 						// нормально распределенной величины
 						// DispLink - дисперсия связей
 
 						for (int i = 0; i < m.ns_links; i++) {
                             int lx, ly;
                             do {
                                 double X, Y, S;
                                 do {
                                     X = 2.0 * Math.random() - 1;
                                     Y = 2.0 * Math.random() - 1;
                                     S = X * X + Y * Y;
                                 } while (S > 1 || S == 0);
                                 S = Math.sqrt(-2 * Math.log(S) / S);
                                 double dX = X * S * m.sigma;
                                 double dY = Y * S * m.sigma;
                                 lx = (int) Math.round(x_in_nerv + dX);
                                 ly = (int) Math.round(y_in_nerv + dY);
 
                                 // колонки по периметру не задействованы
 
                                 if (lx < 1)
                                     lx = 1;
 
                                 if (ly < 1)
                                     ly = 1;
 
                                 if (lx > m.zone.width() - 2)
                                     lx = m.zone.width() - 2;
 
                                 if (ly > m.zone.height() - 2)
                                     ly = m.zone.height() - 2;
 
                             // Проверка на повтор связи
 							} while (nerv_links[lx][ly]);
 
 							nerv_links[lx][ly] = true;
 
 							// Создаем синаптическую связь
 							NeuronSimple n = s[x][y][z];
 							n.s_links[n.n1].x = lx;
 							n.s_links[n.n1].y = ly;
 							n.s_links[n.n1].zone = m.zone;
 							n.n1++;
 						}
 					}
 				}
 			}
 		}
 
 		// Инициализация аксонных связей простых нейронов
 		// и, соответственно, синаптических сложных нейронов.
 		// В простейшем случае каждый простой нейрон сязан с девятью колонками,
 		// образующими квадрат с центров в этом нейроне.
 		for (int x = 0; x < width(); x++) {
 			for (int y = 0; y < height(); y++) {
 				NeuronComplex sn = col[x][y];
 
 				sn.s_links = new Link3d[nsc_links];
 				for (int i = 0; i < nsc_links; i++)
 					sn.s_links[i] = new Link3d();
 
 				sn.active = false;
 			}
 		}
 
 		int n;
 		// колонки по периметру не задействованы
 		for (int x = 1; x < width() - 1; x++) {
 			for (int y = 1; y < height() - 1; y++) {
 
 				n = 0;
 
 				for (int i = x - 1; i <= x + 1; i++) {
 					for (int j = y - 1; j <= y + 1; j++) {
 						for (int k = 0; k < deep; k++) {
 
 							NeuronComplex cn = col[x][y];
 
 							cn.s_links[n].x = i;
 							cn.s_links[n].y = j;
 							cn.s_links[n].z = k;
 							n++;
 
 							NeuronSimple sn = s[i][j][k];
 							sn.a_links[sn.n2].x = x;
 							sn.a_links[sn.n2].y = y;
 							sn.n2++;
 						}
 					}
 				}
 			}
 		}
 	}
     
 	// Картинка активных нейронов по колонкам
 	public BufferedImage[] getSImage() {
 		BufferedImage[] a = new BufferedImage[deep];
 		for (int z = 0; z < deep; z++) {
 			BufferedImage image = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);
 			for (int x = 0; x < width(); x++) {
 				for (int y = 0; y < height(); y++) {
 					int c = s[x][y][z].active ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
 					image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
 				}
 			}
 			a[z] = image;
 		}
 		return a;
 	}
 	
 	ColumnRF_Image CRF = null;
 	
 	public Imageable getCRF() {
 		if (CRF == null)
 			CRF = new ColumnRF_Image();
 		
 		return CRF;
 	}
 
 	class ColumnRF_Image implements Imageable {
 		
 	    private int boxSize;
 	    private int maxX;
 	    private int maxY;
 	    private BufferedImage image;
 
 		ColumnRF_Image() {
 	        boxSize = 1;
 	        for (Mapping i : in_zones) {
 	            boxSize = (int) Math.max(boxSize, 6 * i.sigma);
 			}
 
 	        maxX = width() * boxSize;
 	        maxY = height() * boxSize;
 	        
 	        image = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
 		}
 	
 		public String getImageName() {
 			return "input from output";
 		}
 
 		public BufferedImage getImage() {
 			Graphics g = image.getGraphics();
 			g.setColor(Color.BLACK);
 			g.fillRect(0, 0, maxX, maxY);
 	
 			int pX, pY = 0;
 	
 	//		g.setColor(Color.YELLOW);
 	
 			for (int x = 1; x < width() - 1; x++) {
 				for (int y = 1; y < height() - 1; y++) {
 					
 	//				g.drawLine(x*boxSize, 0, x*boxSize, maxY);
 	//				g.drawLine(0, y*boxSize, maxX, y*boxSize);
 	
 					final NeuronComplex cn = col[x][y];
 	
 	                for (int i = 0; i < nsc_links; i++) {
 	                	final Link3d cl = cn.s_links[i];
 	                    if (s[cl.x][cl.y][cl.z].occupy) {
 	                    	
 	                    	final NeuronSimple sn = s[cl.x][cl.y][cl.z];
 	                    	if (sn.occupy) {
 	                            for (int j = 0; j < ns_links; j++) {
 	                                final Link2dZone sl = sn.s_links[j];
 	                                if (sl.cond) {
										pX = x*boxSize + (boxSize / 2) + (sl.x - cl.x);
										pY = y*boxSize + (boxSize / 2) + (sl.y - cl.y);
	                                	if (pX > x*boxSize && pX < (x*boxSize+boxSize) 
	                                			&& pY > y*boxSize && pY < (y*boxSize+boxSize)) {
 					                    	
 					                    	int c = Utils.calcGrey(image, pX, pY);
 											c += 50;
 											image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
 	                                	}
 	                                }
 	                            }
 	                    	}
 	                    }
 	                }
 				}
 			}
 			return image;
 		}
 
 		@Override
 		public Object whatAt(Point point) {
 			try {
 				return 
 					new Object[] {
 						CortexZoneComplex.this, new Point(
 								Math.round(point.x / boxSize), 
 								Math.round(point.y / boxSize)
 							)
 					};
 			} catch (Exception e) {
 			}
 			return null;
 		}
 	}
 
 	// Картинка суммы занятых нейронов в колонке
 	public BufferedImage[] getOccupyImage() {
 		BufferedImage[] a = new BufferedImage[deep];
 		for (int z = 0; z < deep; z++) {
 			BufferedImage image = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);
 			for (int x = 0; x < width(); x++) {
 				for (int y = 0; y < height(); y++) {
 					int c = s[x][y][z].occupy ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
 					image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
 				}
 			}
 			a[z] = image;
 		}
 		return a;
 	}
     
     public void cycle (int x1, int y1, int x2, int y2, Act<CortexZoneComplex> task) {
         for (int x = x1; x < x2; x++) {
             for (int y = y1; y < y2; y++) {
                 task.process(this, x, y);
             }
         }
     }
 
     //Граничные нейроны не задействованы.
     //Такт 1. Активация колонок (узнавание)
     public void cycle1() {
         cycle(1, 1, width() - 1, height() - 1, activation);
         cycle(1, 1, width() - 1, height() - 1, recognition);
     }
 
     //Граничные нейроны не задействованы.
     //Такт 2. Запоминание  и переоценка параметров стабильности нейрона
     public void cycle2() {
         cycle(1, 1, width() - 1, height() - 1, remember);
     }
 
 }
