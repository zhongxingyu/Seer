 package com.socialcomputing.wps.server.persistence;
 
 import java.util.Collection;
 
 public interface SwatchManager {
 
     public Collection<Swatch> findAll();
 
     public Swatch findByName(String name, String dictionaryName);
 
     public Swatch create(String name, String definition, String dictionaryName);
 
     public void update(Swatch swatch);
 
    public void remove(String name, String dicoName);
 }
