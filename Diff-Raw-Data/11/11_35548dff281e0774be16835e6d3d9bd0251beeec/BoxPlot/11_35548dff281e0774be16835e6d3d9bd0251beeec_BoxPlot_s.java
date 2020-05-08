 /** This file is part of Approach Avoidance Task.
  *
  * Approach Avoidance Task is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Approach Avoidance Task is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Approach Avoidance Task.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package views;
 
 import processing.core.PApplet;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Wilfried
  * Date: 30-10-11
  * Time: 19:35
  * To change this template use File | Settings | File Templates.
  */
 
 /**
  * In eerste instantie tot doel om boxPlot class te worden welke vanuit AATViewResults.java opgeroepen zou kunnen worden
  * Het laden van de boxplot classe, welke in processing is geschreven, in een processing class gaf problemen
  * vandaar dat de draw functie direct vanuit deze class wordt opgeroepen. Gevolg is dat er overbodige code in deze class
  * staat, maar ivm met tijd is dit bijgewerkt. Het werkt!!
  */
 public class BoxPlot extends PApplet {
     /**
      * Aanmaken van verschillende variabelen welke nodig zijn in de BoxPlot class
      */
     float rH, minV, maxV;
     float[] minValue, q1, median, q3, maxValue;
     float[] array1, array2, array3, array4;
     String[] labelsArray;
     int viewWidth, viewHeight;
 
     /**
      * Aanmaken van de DescriptiveStats objecten, welke de gegevens bevatten op basis waarvan de 4 boxplotten getekend
      * kunnen worden.
      */
     DescriptiveStats stat1;
     DescriptiveStats stat2;
     DescriptiveStats stat3;
     DescriptiveStats stat4;
 
 
     /**
      * @param array1      BoxPlot 1
      * @param array2      BoxPlot 2
      * @param array3      BoxPlot 3
      * @param array4      Boxplot 4
      * @param labelsArray de labelels voor BoxPlot 1, 2 ,3 en 4
      * @param viewWidth   Breedte van het scherm
      * @param viewHeight  Hoogte van het scherm
      */
     BoxPlot(float[] array1, float[] array2, float[] array3, float[] array4, String[] labelsArray, int viewWidth, int viewHeight) {
         this.labelsArray = labelsArray;
         this.viewHeight = viewHeight;
         this.viewWidth = viewWidth;
         this.array1 = array1;
         this.array2 =array2;
         this.array3 = array3;
         this.array4 = array4;
 
     }
 
 
     /**
      * Standaard setup() functie van processing
      */
     public void setup() {
         size(viewWidth, viewHeight);
        /**
         * hoeft allemaal niet zo rap, statisch plaat, bespaart stroom en groen is goed, vandaar!! En hoe smoother hoe
         * beter, dus altijd smooth() zijn waar  mogelijk :)
         */
        frameRate(1);
        //    smooth();
     }
 
 
     /**
      * Standaard draw() functie uit processing.
      */
     public void draw() {
        background(0);
         drawBoxPlot(0, 0, scaleFactor(), "Reaction Time (ms)", "Condition", labelsArray[0], labelsArray[1], labelsArray[2], labelsArray[3]);
     }
 
 
     /**
      * Scalefactor geeft vergrotings of verkleinings factor van het plaatje. Het plaat wordt getekend als 500x500 plaatje
      * Wanneer de scherm een hoogte heeft van 800 pixxels is is de schaalfactor 800 / 500 = 1.6 zodat plaatje
      * scherm vullend is.
      *
      * @return scaleFactor als float
      */
     public float scaleFactor() {
         return (float) viewHeight / 500f;
     }
 
 
     private void calculate() {
         stat1 = new DescriptiveStats(array1);
         stat2 = new DescriptiveStats(array2);
         stat3 = new DescriptiveStats(array3);
         stat4 = new DescriptiveStats(array4);
 
         /**
          * minValue, q1, median, q3 en maxValue bevatten de respectievelijke de waarden uit de 4 condities
          */
         float minValue[] = {
                 stat1.minValue(), stat2.minValue(), stat3.minValue(), stat4.minValue()
         };
         float q1[] = {
                 stat1.q1(), stat2.q1(), stat3.q1(), stat4.q1()
         };
         float median[] = {
                 stat1.median(), stat2.median(), stat3.median(), stat4.median()
         };
         float q3[] = {
                 stat1.q3(), stat2.q3(), stat3.q3(), stat4.q3()
         };
         float maxValue[] = {
                 stat1.maxValue(), stat2.maxValue(), stat3.maxValue(), stat4.maxValue()
         };
 
         /**
          * Grootste en kleinste waarde vinden die zich in de 4 array´s bevinden.
          */
         float[] minMaxArray = {
                 stat1.minValue(), stat2.minValue(), stat3.minValue(), stat4.minValue(),
                 stat1.maxValue(), stat2.maxValue(), stat3.maxValue(), stat4.maxValue()
         };
 
         /**
          * rH is de de range van de minimale reactietijd en maximale reactieit, genomen over de 4 condities die
          * in de test bestaan.
          * minV is de snelste reactietijd gebasseerd op q1 - 1.5 IKA
          * maxV is de snelste reactietijd gebasseerd op q3 + 1.5 IKA
          */
         rH = max(minMaxArray) - min(minMaxArray);
         minV = min(minMaxArray);
         maxV = max(minMaxArray);
 
         /**
          * The array´s, welke samen de 5 getallen samenvatting bevatten, publiek geschikt maken. Indien voldoende tijd
          * even fixxen, is in principe niet meer op deze manier nodig.
          */
         this.minValue = minValue;
         this.q1 = q1;
         this.median = median;
         this.q3 = q3;
         this.maxValue = maxValue;
     }
 
 
     /**
      * De drawBoxPlot functiie, zoals eerder aangeven bevat huidige class (BoxPlot.java) overbodige code omdat bedoelling
      * was om de BoxPlot class van AATViewResults aan te roepen. Met name de functie drawBoxPlot bevat daarom veel
      * overbosige code.
      *
      * @param x      coordinaat waar plaatje moet beginnen op scherm
      * @param y      coordinaat waar plaatje moet beginnen op scherm
      * @param s      scalefactor waarde
      * @param yText  label van Y as.
      * @param xText  label van X as.
      * @param c1Text label conditie 1
      * @param c2Text label conditie 2
      * @param c3Text label conditie 3
      * @param c4Text label conditie 4
      */
     public void drawBoxPlot(int x, int y, float s, String yText, String xText, String c1Text, String c2Text, String c3Text, String c4Text) {
         calculate();
         scale(s);
         translate(x, y);
 
         /**
          * X & Y as tekenen
          */
         fill(255);
         line(75, 40, 75, 455); //Y As
         line(75, 455, 475, 455); // X As
 
         /**
          * Code welke de BoxPlotten tekent
          * @resizeFac de hoogte van een boxplot is 400pixxels, echter bij een een range van 300 tot 600ms seconden is de
          * relatieve afstand per pixxels anders dan wanneer de range tussen 300 en 10000 ms is. In het laatste geval
          * is de range 700ms, 400 / 700 = .6, dus .6 pixxel representeert 1ms, en 6 pixxels 10ms etc.
          * @minFactor aantal pixels dat moet worden afgetrokken omdat de minv waarde nooit 0 is. B.v. snelste reactijd
          * is 300ms. en we namen een range van 700ms hebben we eerder gezien dat de 1 ms .6 pixxel representeer, zonder
          * deze min waarde zou 300ms 300*.6 = van 180 pixels (onderkant scherm) beginnen. Echter 300ms is de minimale
          * waarde in dit geval en zou daarom moet staren op Y coordinaat 0 pixxel (onderkant van het scherm). deze min
          * factor verzorgd deze laatste correctie
          * @hPosBP x coordinaat van de boxplot. midden van boxplot 1 licht op x coordinaat 100, aan eind van de for
          * loopt wordt xPosBP met 100 pixxels verhoogd, waardoor tweede boxplot een x coordinaat van 200, etc
          */
         float resizeFac = 400f / rH;
         float minFactor = (resizeFac * minV);
         int hPosBP = 100;
         for (int n = 0; n < 4; n++) {
             stroke(224, 3, 81);
             fill(62, 88, 172);
             rectMode(CORNERS);
             line(hPosBP + 25, 450 - (resizeFac * minValue[n]) + minFactor, hPosBP + 25, 450 - (resizeFac * maxValue[n]) + minFactor); //Verticale lijn
             rect(hPosBP, 450 - (resizeFac * q3[n]) + minFactor, hPosBP + 50, 450 - (resizeFac * q1[n]) + minFactor); //q1 & q3
             line(hPosBP, 450 - (resizeFac * minValue[n]) + minFactor, hPosBP + 50, 450 - (resizeFac * minValue[n]) + minFactor); // horizontale streep minValue
             line(hPosBP, 450 - (resizeFac * median[n]) + minFactor, hPosBP + 50, 450 - (resizeFac * median[n]) + minFactor); // horizontale streep mediaan
             line(hPosBP, 450 - (resizeFac * maxValue[n]) + minFactor, hPosBP + 50, 450 - (resizeFac * maxValue[n]) + minFactor); // horizontale streep maxValue
             hPosBP += 100;
         }
 
         /**
          * Volgende code blok is verantwoordelijk schrijven van waarden op de y as
          */
         fill(255);
         textAlign(RIGHT);
         textSize(9);
         int textY = 450;
         float msStepSize = rH / 8;
         float msStartStep = minV;
         for (int n = 0; n <= 8; n++) {
             text((int) msStartStep + " -", 76, textY);
             textY = textY - 50;
             msStartStep += round(msStepSize);
         }
 
         /**
          * De labels voor de 4 condities op de x as.
          */
         textAlign(CENTER);
         textSize(9);
         text(c1Text, 125, 470);
         text(c2Text, 225, 470);
         text(c3Text, 325, 470);
         text(c4Text, 425, 470);
 
         /**
          * Labels van X & Y as
          */
         textSize(15);
         text(xText, 250, 495); // Text X-as
         rotate(HALF_PI);
         text(yText, 250, -10); //Text Y-as
         text(c1Text, 125, 475);
     }
 }
