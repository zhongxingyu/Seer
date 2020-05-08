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
    public static final int codFicha = 3;
     
     public Muerte (String nombre, String col){
         super(nombre,col);
     }
     
     @Override
    public void setCantMovimientos(int cant){
         cantMovientos = cant;
     }
     
     @Override
    public void setCantLP(int lp){
         lifePoints = lp;
     }
    
    @Override
    public int setDamagePoints(int codFichaContraria){
        switch (codFichaContraria){
            case 1:
                return -3;
            case 2:
                return -2;
            case 3:
                return -1;
            case 4:
                return -2;
            default: return 0;     
        } 
    }
 }
