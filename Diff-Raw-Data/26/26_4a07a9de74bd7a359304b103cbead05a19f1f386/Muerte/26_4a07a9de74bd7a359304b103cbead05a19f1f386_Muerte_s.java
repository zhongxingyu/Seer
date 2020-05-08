 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package VampireWarGame;
 
 /**
  *
  * @author drako
  * 
  */
 public class Muerte extends Fichas{
    protected int lifePoints;
    protected int damagePoints;
    protected int cantMovientos;
     
     public Muerte (String nombre, String col){
         super(nombre,col);
     }
     
     @Override
    protected void setCantMovimientos(int cant){
         cantMovientos = cant;
     }
     
     @Override
    protected void setCantLP(int lp){
         lifePoints = lp;
     }
 }
