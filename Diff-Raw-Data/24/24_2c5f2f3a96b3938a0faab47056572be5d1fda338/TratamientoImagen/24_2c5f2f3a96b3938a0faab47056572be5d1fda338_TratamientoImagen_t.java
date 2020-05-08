 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lectorbmp;
 
 import java.awt.Color;
 
 /**
  *
  * @author jmanuel
  */
 public class TratamientoImagen {
     
     private int imgbytes[][][];
     private int angulocontraste = 80;
     private int pesoblur = 4;
     private int pesonitidez = 10;
     private int pesosuavizado = 4;
     private int pesorenovar = 4;
     
     public TratamientoImagen(int[][][] imgbytes){
         this.imgbytes = imgbytes;
     }
     
     public void brillo(){
         for (int i = 0; i < imgbytes.length; i++)
             for (int j = 0; j < imgbytes[i].length; j++) {
                 Color c = new Color(imgbytes[i][j][0],imgbytes[i][j][1],imgbytes[i][j][2]);
                 c = c.brighter();
                 imgbytes[i][j][0] = c.getRed();
                 imgbytes[i][j][1] = c.getGreen();
                 imgbytes[i][j][2] = c.getBlue();
             }
     }
     
     public void contraste(){
         for (int i = 0; i < imgbytes.length; i++)
             for (int j = 0; j < imgbytes[i].length; j++)
                 for (int k = 0; k < imgbytes[i][j].length; k++) {
                     double contraste = Math.tan( angulocontraste * Math.PI / 180.0 );
                     imgbytes[i][j][k] = (int) (128 + (imgbytes[i][j][k] - 128) * contraste);
                     if( imgbytes[i][j][k] < 0 ) imgbytes[i][j][k] = 0;
                     if( imgbytes[i][j][k] > 255 ) imgbytes[i][j][k] = 255;
                 }
     }
     
     public void blur(){
         int kernel[][] = {{1,2,1},{2,pesoblur,2},{1,2,1}};
         int skernel = 12 + pesoblur;
         aplicarFiltro(kernel, skernel);
     }
     
     public void nitidez(){
        int kernel[][] = {{0,-1,0},{-1,pesonitidez,-1},{0,-1,0}};
         int skernel = pesonitidez - 4;
         aplicarFiltro(kernel, skernel);
     }
     
     public void suavizado(){
         int kernel[][] = {{1,1,1},{1,pesosuavizado,1},{1,1,1}};
         int skernel = 8 + pesosuavizado;
         aplicarFiltro(kernel, skernel);
     }
     
     public void renovar(){
         int kernel[][] = {{-1,-1,-1},{-1,pesorenovar,-1},{-1,-1,-1}};
         int skernel = pesorenovar-8;
         aplicarFiltro(kernel, skernel);
     }
     
    private void aplicarFiltro(int[][] kernel, int skernel){
         for (int i = 1; i < imgbytes.length -1; i++)
             for (int j = 1; j < imgbytes[i].length-1; j++)
                 for (int k = 0; k < imgbytes[i][j].length; k++) {
                     imgbytes[i][j][k] *= kernel[1][1];
                     for (int l = 0; l < kernel.length; l++)
                         for (int m = 0; m < kernel[l].length; m++) {
                            if(l==1 && l==m) continue;
                            imgbytes[i][j][k]+=(imgbytes[i-1+l][j-1+m][k]*kernel[l][m]);
                         }
                     imgbytes[i][j][k] /= skernel;
                     if( imgbytes[i][j][k] < 0 ) imgbytes[i][j][k] = 0;
                     if( imgbytes[i][j][k] > 255 ) imgbytes[i][j][k] = 255;
                 }
     }
 
     /**
      * @return the imgbytes
      */
     public int[][][] getImgbytes() {
         return imgbytes;
     }
 
     /**
      * @return the angulocontraste
      */
     public int getAngulocontraste() {
         return angulocontraste;
     }
 
     /**
      * @param angulocontraste the angulocontraste to set
      */
     public void setAngulocontraste(int angulocontraste) {
         this.angulocontraste = angulocontraste;
     }
     
     
 }
