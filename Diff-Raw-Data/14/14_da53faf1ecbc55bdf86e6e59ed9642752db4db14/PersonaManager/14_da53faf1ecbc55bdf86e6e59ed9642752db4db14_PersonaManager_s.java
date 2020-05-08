 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.crimsonrpg.personas.personasapi.persona;
 
 /**
  * Persona manager.
  */
 public interface PersonaManager {
    public void registerPersona(Class<? extends Persona> type);
     
     public Persona getPersona(String id);
 }
