 import java.util.Collection;
 
 /*
  Copyright (c) 2012, The Staccato-Commons Team
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; version 3 of the License.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  */
 
 /**
  * @author flbulgarelli
  * 
  */
 public class Personaje {
 
   private String nombre;
   private int puntosDeAtaque;
   private int puntosDeDefensa;
   private int puntosDeVida;
   private Collection<Habilidad> habilidades;
 
  public Personaje(String nombre, int puntosDeAtaque, int puntosDeDefensa, int puntosDeVida,
    Collection<Habilidad> habilidades) {
    this.nombre = nombre;
    this.puntosDeAtaque = puntosDeAtaque;
    this.puntosDeDefensa = puntosDeDefensa;
    this.puntosDeVida = puntosDeVida;
    this.habilidades = habilidades;
  }
 
   // dont'care
 
   public interface Habilidad {
     // don't care
   }
 
   public enum HabilidadSimple implements Habilidad {
     CORRER, SANAR, CABALGAR
   }
 
 }
