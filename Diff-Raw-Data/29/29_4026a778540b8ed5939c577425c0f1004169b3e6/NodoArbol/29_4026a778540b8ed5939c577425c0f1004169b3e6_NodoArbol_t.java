 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.grupoia.main.tpdos.common;
 
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author Fanky10 <fanky10@gmail.com>
  */
 public class NodoArbol {
 
     protected List<NodoArbol> nodosHijos;
     protected Integer value = 0;
     protected Iterator<NodoArbol> nodosHijosIt;
 
     public NodoArbol(List<NodoArbol> nodosHijos) {
         this(0, nodosHijos);
     }
 
     public NodoArbol(Integer value) {
         this(value, new java.util.ArrayList<NodoArbol>());
     }
 
     public NodoArbol(Integer value, List<NodoArbol> nodosHijos) {
         this.value = value;
         this.nodosHijos = nodosHijos;
         this.nodosHijosIt = nodosHijos.iterator();
     }
 
     public NodoArbol getNextChild() {
         return nodosHijosIt.next();
     }
 
     public boolean hasChildren() {
         return nodosHijosIt.hasNext();
     }
    public void addChild(NodoArbol na){
        nodosHijos.add(na);
    }
 
     @Override
     public String toString() {
        String name = (nodosHijos.isEmpty()?"Hoja":"Rama");
        return name + " value: " + value;
     }
 
     @Override
     public boolean equals(Object object) {
         if (object instanceof NodoArbol) {
             NodoArbol objetivo = (NodoArbol) object;
             return this.getValue() == objetivo.getValue();
         }
         return false;
     }
 
     public List<NodoArbol> getNodosHijos() {
         return nodosHijos;
     }
 
     public void setNodosHijos(List<NodoArbol> nodosHijos) {
         this.nodosHijos = nodosHijos;
     }
 
     public Integer getValue() {
         return value;
     }
 
     public void setValue(Integer value) {
         this.value = value;
     }
 }
