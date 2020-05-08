 package matrix;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import filterData.FilterData;
 import filterData.ParserXml;
 import objectPos.Object;
 import objectPos.Position;
 
 public class MatrixCreator implements IMatrixCreator
 	{
 		private FilterData fd;
 		private static final int MAX_BLUE = 240;
 		private static final int MIN_BLUE = 10;
 
 		private int height = 150;
 		private int width = 150;
 		private int defaultColor = 100;
 		private int red;
 		private int green;
 		private int blue;
 		private int wheelwidth = 18;
 		private ArrayList<ArrayList<Color>> matrix;
 
 		public MatrixCreator()
 			{
 				matrix = new ArrayList<ArrayList<Color>>();
 				// matrix.add(new ArrayList<Color>());
 			}
 
 		public void setfilterData(FilterData fd)
 			{
 				this.fd = fd;
 			}
 
 		public void createMatrix()
 			{
 				for (int i = 0; i < width; i++)
 					{
 						matrix.add(new ArrayList<Color>());
 						for (int j = 0; j < height; j++)
 							{
 								matrix.get(i).add(new Color(defaultColor, defaultColor, defaultColor));
 							}
 					}
 			}
 
 		public void resetMatrix()
 			{
 				matrix.clear();
 				for (int i = 0; i < width; i++)
 					{
 						matrix.add(new ArrayList<Color>());
 						for (int j = 0; j < height; j++)
 							{
 								matrix.get(i).add(new Color(defaultColor, defaultColor, defaultColor));
 							}
 					}
 			}
 
 		public void setPixelinMatrix(int x, int y, int r, int g, int b)
 			{
 				matrix.get(x).set(y, new Color(r, g, b));
 			}
 
 		public void setNewPosition(Object obj)
 			{
 				if (obj.getOldPosition() == null)
 					{
 						setPixelinMatrix(obj.getPosition().getX(), obj.getPosition().getY(), 0, 0, obj.getColor());
 						obj.setOldPosition(obj.getPosition());
 					} else if (obj.getOldPosition().getX() == obj.getPosition().getX() && obj.getOldPosition().getY() == obj.getPosition().getY())
 					{
 
 					} else
 					{
 						// with delete old pos
 						setPixelinMatrix(obj.getPosition().getX(), obj.getPosition().getY(), 0, 0, obj.getColor());
 						setPixelinMatrix(obj.getOldPosition().getX(), obj.getOldPosition().getY(), defaultColor, defaultColor, defaultColor);
 						obj.setOldPosition(obj.getPosition());
 					}
 			}
 
 		public void setColorFromScannedPixel(int x, int y)
 			{
 				blue = matrix.get(x).get(y).getBlue();
 				green = matrix.get(x).get(y).getGreen();
 				red = matrix.get(x).get(y).getRed();
 			}
 
 		public void setScanData(ArrayList<int[]> data)
 			{
 				for (int i = 0; i < data.size(); i++)
 					{
 						int x = data.get(i)[0];
 						int y = data.get(i)[1];
 						setColorFromScannedPixel(x, y);
 
 						if (blue < MAX_BLUE)
 							{
 								blue += 10;
 								green += 10;
 								red += 10;
 							}
 						setPixelinMatrix(x, y, red, green, blue);
 						updateMatrix();
 
 					}
 				data.clear();
 			}
 
 		public ArrayList<ArrayList<Color>> getCreatedMatrix()
 			{
 				return matrix;
 			}
 
 		public void drawLine(Position scanPos, Position mypos)// in
 		// ver�nderter
 		// Form von
 		// http://www-lehre.informatik.uni-osnabrueck.de
 			{
 				// Jacks Algorithmus ist schneller als Geradengleichung
 				int x, y, error, delta, schritt, dx, dy, inc_x, inc_y;
 
 				x = mypos.getX(); // 
 				y = mypos.getY(); // As i am center
 
 				dx = scanPos.getX() - x;
 				dy = scanPos.getY() - y; // Hoehenzuwachs
 
 				// Schrittweite
 
 				if (dx > 0) // Linie nach rechts?
 					inc_x = 1; // x inkrementieren
 				else
 					// Linie nach links
 					inc_x = -1; // x dekrementieren
 
 				if (dy > 0) // Linie nach oben ?
 					inc_y = 1; // y inkrementieren
 				else
 					// Linie nach unten
 					inc_y = -1; // y dekrementieren
 
 				if (Math.abs(dy) < Math.abs(dx))
 					{ // flach nach oben oder unten
 						error = -Math.abs(dx); // Fehler bestimmen
 						delta = 2 * Math.abs(dy); // Delta bestimmen
 						schritt = 2 * error; // Schwelle bestimmen
 						while (x != scanPos.getX())
 							{
 
 								if (x != mypos.getX())
 									{
 										incPix(x + mypos.getX(), y + mypos.getY()); // Fuer
 										// jede
 										// x-Koordinate
 										// System.out.println("inc pos "+ (x +
 										// mypos.getX()));
 									}
 								// Pixel
 								x += inc_x; // naechste x-Koordinate
 								error = error + delta; // Fehler aktualisieren
 								if (error > 0)
 									{ // neue Spalte erreicht?
 										y += inc_y; // y-Koord. aktualisieren
 										error += schritt; // Fehler
 										// aktualisieren
 									}
 							}
 					} else
 					{ // steil nach oben oder unten
 						error = -Math.abs(dy); // Fehler bestimmen
 						delta = 2 * Math.abs(dx); // Delta bestimmen
 						schritt = 2 * error; // Schwelle bestimmen
 						while (y != scanPos.getY())
 							{
 								if (y != mypos.getY())
 									{// fuer jede y-Koordinate
 										incPix(x + mypos.getX(), y + mypos.getY());
 									}// setze
 								// Pixel
 								y += inc_y; // naechste y-Koordinate
 								error = error + delta; // Fehler aktualisieren
 								if (error > 0)
 									{ // neue Zeile erreicht?
 										x += inc_x; // x-Koord. aktualisieren
 										error += schritt; // Fehler
 										// aktualisieren
 									}
 							}
 					}
 				if ((x != scanPos.getX() && y != scanPos.getY()))
 					{
 						decPix(scanPos.getX() + x, scanPos.getY() + y);
 					}
 			}
 
 		public void decPix(int x, int y)
 			{
 				if (x > width - 1 || y > height - 1)
 					{
 						x = 149;
 						y = 149;
 					}
 
 				if (matrix.get(x).get(y).getBlue() > MIN_BLUE && matrix.get(x).get(y).getGreen() > MIN_BLUE)
 					{
 						// System.out.println("dec"+matrix.get(x).get(y).getGreen());
 						setPixelinMatrix(x, y, (matrix.get(x).get(y).getBlue() - 10), (matrix.get(x).get(y).getGreen() - 10), (matrix.get(x).get(y).getBlue() - 10));
 
 					}
 			}
 
 		public void incPix(int x, int y)
 			{
 				if (x > width - 1 || y > height - 1)
 					{
 						x = 149;
 						y = 149;
 					}
 				if (matrix.get(x).get(y).getBlue() < MAX_BLUE && matrix.get(x).get(y).getGreen() > MIN_BLUE)
 					{
 						// System.out.println("inc"+matrix.get(x).get(y).getGreen());
 						setPixelinMatrix(x, y, matrix.get(x).get(y).getBlue() + 10, matrix.get(x).get(y).getGreen() + 10, matrix.get(x).get(y).getBlue() + 10);
 					}
 			}
 
 		public void updateMatrix()
 			{
 
 				int[] actPos;
 				ArrayList<int[]> vectorHead;
 				Position myPos = new Position();
 				Position zielPos = new Position();
 				actPos = fd.getParsedInfos().getPos();
 
 				myPos.setX(actPos[0]);
 				myPos.setY(actPos[1]);
 				vectorHead = fd.getParsedInfos().getData();
 
 				if (actPos.length > 0 && !vectorHead.isEmpty())
 					{
 						for (int i = 0; i < vectorHead.size(); i++)
 							{
 								int[] scanPos = vectorHead.get(i);
 								zielPos.setX(scanPos[0]);
 								zielPos.setY(scanPos[1]);
 
 								drawLine(zielPos, myPos);
 								i++;
 							}
 					}
 			}
 		
 
 		public double getAngleToObstacle(Position scanpos)
 			{
 				double beta;
				double ax = 0, ay = 10; // vektor in lngsachse des fahrzeugs
 				double lengthveca = Math.sqrt(Math.pow(ax, 2) + Math.pow(ay, 2));
 				double lengthvecb = Math.sqrt(Math.pow(scanpos.getX(), 2) + Math.pow(scanpos.getY(), 2));
 				double z = (ax * scanpos.getX()) + (ay * scanpos.getY());
 				double n = (lengthveca * lengthvecb);
 				beta = z / n;
 				beta = Math.acos(beta) * 180 / Math.PI;
 				return beta;
 			}
 
 		public double getAngleToX()
 			{
 				double alpha;
 				//double lchain = 14;
 				//double rchain = -14;
 				double lchain =	fd.getParsedInfos().getDirection()[0];
 				double rchain =	fd.getParsedInfos().getDirection()[1];
 				alpha = ((lchain - rchain) / wheelwidth) * 180 / Math.PI;
 				System.out.println("alpha" + alpha);
 				return alpha;
 			}
 
 		public Position absolutePosofobstacle(Position myPos, Position scanPos)
 			{
 				Position absPos = new Position();
 
 				double gamma = getAngleToX() + getAngleToObstacle(scanPos);
 				System.out.println("Alpha: " + getAngleToX() + " Beta: " + getAngleToObstacle(scanPos) + " Gamma: " + gamma);
 				double length = Math.sqrt(Math.pow(scanPos.getX(), 2) + Math.pow(scanPos.getY(), 2));
 				System.out.println(Math.round((length * Math.cos(Math.toRadians(gamma)))));
 				absPos.setX((int) Math.round((length * Math.cos(Math.toRadians(gamma)))));
 				absPos.setY((int) Math.round((length * Math.sin(Math.toRadians(gamma)))));
 				System.out.println("relativePos:" + scanPos.getX() + "," + scanPos.getY());
 				System.out.println("absolutePos:" + absPos.getX() + "," + absPos.getY());
 				return scanPos;
 
 			}
 
 		/*
 		 * public static void main(String[] args) {
 		 * 
 		 * MatrixCreator mc = new MatrixCreator(); mc.createMatrix();
 		 * ArrayList<ArrayList<Color>> matrix = mc.getCreatedMatrix();
 		 * 
 		 * // System.out.println(matrix.get(1).get(1).getRGB());
 		 * 
 		 * }
 		 */
 
 	}
