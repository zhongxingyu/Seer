 public class Reina extends Pieza{
     
     public Reina(int x, int y, boolean blanca){
         super(x,y,blanca);
     }
     
     @Override
     public boolean move(int nx, int ny, Casilla tablero[][]){
         
         if (Math.abs(nx-x) != Math.abs(ny-y)){
             if (!((Math.abs(nx-x)>0 && ny-y==0) || (Math.abs(ny-y)>0 && nx-x==0)))
                 return false;
            }
                 
        if (tablero[nx][ny].getPieza() != null && tablero[nx][ny].
                getPieza().isBlanca() == isBlanca())
             return false;
         
         if (Math.abs(nx-x) == Math.abs(ny-y))
             for (int i=x; i!=nx;)
                 for (int j=y; j!=ny;){
                     if (tablero[i][j].getPieza() != null && i != x && j != y)
                         return false;
                 
                     if (nx>x)
                         i++;
                     else
                         i--;
 
                     if (ny>y)
                         j++;
                     else
                         j--;
 
                 }
         
         if (Math.abs(nx-x) > 0 && ny-y == 0)
             for (int i = x; i!=nx;){
                 if (tablero[i][ny].getPieza() != null && i != x)
                     return false;
                 
                 if (nx > x)
                     i++;
                 else
                     i--;
             }
         
         else if (Math.abs(ny-y) > 0 && nx-x == 0)
             for (int i = y; i!=ny;){
                 if (tablero[nx][i].getPieza() != null && i != y)
                     return false;
                 
                 if (ny > y)
                     i++;
                 else
                     i--;
             }
            
       		  System.out.println("Que gay!!! D:");
 
             return true;
         }
     
     @Override
     public Pieza getPieza(){
         return new Reina(x,y,blanca);
     }
     
     @Override
     public String ClassName(){
         if (isBlanca())
             return "Rb";
                 
         else
             return "Rn";
     }
     
}
