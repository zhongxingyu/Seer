 package dojo;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 public class Oceano {
   private boolean[][] matriz;
 
   public Oceano() {
     matriz = new boolean[101][101];
   }
 
   public Oceano(List<Rede> redes) {
     this();
     for (Rede rede : redes) {
       for (int i = rede.getXi(); i < rede.getXf(); i++) {
         for (int j = rede.getYi(); j < rede.getYf(); j++) {
           matriz[i][j] = true;
         }
       }
     }
   }
 
   public int areaCoberta() {
     int soma = 0;
     for (int i = 0; i < 101; i++) {
       for (int j = 0; j < 101; j++) {
         if (getMatriz()[i][j]) {
           soma++;
         }
       }
     }
 
     return soma;
   }
 
   public boolean[][] getMatriz() {
     return matriz;
   }
 
   public static void main(String[] args) {
     Scanner sc = new Scanner(System.in);
     List<Rede> redes = new ArrayList<>();
 
     for (int n = sc.nextInt(); n > 0; n--) {
       redes.add(new Rede(sc.nextInt(), sc.nextInt(), sc.nextInt(), sc.nextInt()));
     }
 
     sc.close();
     System.out.println(new Oceano(redes).areaCoberta());
   }
 }
