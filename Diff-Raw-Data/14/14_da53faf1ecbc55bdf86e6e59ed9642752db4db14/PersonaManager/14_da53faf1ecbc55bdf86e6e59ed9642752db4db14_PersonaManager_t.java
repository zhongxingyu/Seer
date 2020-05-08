 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.crimsonrpg.personas.personasapi.persona;
 
 /**
  * Persona manager.
  */
 public interface PersonaManager {
    /**
     * Registers a persona.
     * 
     * @param persona 
     */
    public void registerPersona(Persona persona);
     
    /**
     * Gets a persona.
     * 
     * @param id
     * @return 
     */
     public Persona getPersona(String id);
 }
