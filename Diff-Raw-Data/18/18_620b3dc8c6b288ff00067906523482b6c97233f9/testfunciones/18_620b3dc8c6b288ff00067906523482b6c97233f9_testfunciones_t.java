 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Pruebas;
 
 import Auxiliares.Pair;
 import elementos.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import mapa.*;
 import restricciones.*;
 
 
 /**
  *
  * @author ivanmartinez
  */
 public class testfunciones {
     
     
     
     public testfunciones(){
         
     }
     /**
      * 
      * @param v
      * @param p
      * @param eAct
      * @param lastVisited
      * @return
      * @throws Exception 
      */
     
     private void AjustaInicio(Pair p,int k){
         
         if(p.getFirst()!=0 && p.getSecond()!=0){
             if((int)p.getSecond()< k-1){
                 p.setSecond((int)p.getSecond()+1);
             }
             else{
                 p.setSecond(0);
                 p.setFirst((int)p.getFirst()+1);
             }
         }
         
     }
     private Pair cabeEnMapa(Integer v,Plano p,int eAct,Pair lastVisited[],ArrayList<Restriccion> res) throws Exception{
        System.out.println("A ver si el elemento cabe");
        if(lastVisited[eAct+1].getFirst()!=0 && lastVisited[eAct+1].getSecond()!=0){
            System.out.println("Desexpando");
             p.expande((int)lastVisited[eAct].getFirst(),(int)lastVisited[eAct].getSecond(), v, res, false);
             AjustaInicio(lastVisited[eAct],p.tamb());
         }
            AjustaInicio(lastVisited[eAct+1],p.tamb());
         
        for(int i=(int)lastVisited[eAct+1].getFirst();i<p.tama();++i){
            for(int j=(int) lastVisited[eAct+1].getSecond();j<p.tamb();++j){
                 if(!p.consultaPar(v, i, j)){
                   Pair ret = new Pair<Integer,Integer>(i,j);
                  lastVisited[eAct+1] = ret;
                   return ret;
                 } 
             }
         }
         Pair ret = new Pair<Integer,Integer>(-1,-1);
         
         return ret;
     }
     
     public boolean bactracking(int k,ArrayList<Integer> cjt,Pair lastVisited[],HashMap<Integer,ArrayList<Restriccion>> res,Plano p) throws Exception{
         
        System.out.println("Backtracking con"+ k);
         if(k==cjt.size()){
             return true;
         }
         
         else if(k==-1){
             return false;
         }
         
         else {
             Integer valor = cjt.get(k);
             Pair pos = cabeEnMapa(valor,p,k,lastVisited,res.get(valor));//Esta funcion debe desexpandirte en caso de que tus ultimos
                                                          // valores visitados sean difentes a 0 (puesto previamente)
             if(pos.getFirst()!=-1){
                 p.expande((int)pos.getFirst(), (int)pos.getSecond(), valor, res.get(valor), true);
                 return bactracking(k+1,cjt,lastVisited,res,p);
             }
             else{
                 lastVisited[k].setFirst(0);
                 lastVisited[k].setSecond(0);
                 //suponemos que cabeEnMapa desexpande !!!!TODO¡¡¡¡
                 return bactracking(k-1,cjt,lastVisited,res,p);
                 
             }
             
             
         }
     }
 }
