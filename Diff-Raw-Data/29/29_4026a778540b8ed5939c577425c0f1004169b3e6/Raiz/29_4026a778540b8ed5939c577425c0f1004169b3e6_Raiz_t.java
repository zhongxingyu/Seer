 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.grupoia.main.tpdos.common;
 
 import java.util.List;
 
 /**
  *
  * @author Fanky10 <fanky10@gmail.com>
  */
 public class Raiz extends NodoArbol{
     public Raiz(List<NodoArbol> nodosHijos){
         super(nodosHijos);
     }
    public String toString(){
        return "Raiz - "+value;
    }
 }
