 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.muni.fi.pa165.dao.service;
 
 import com.muni.fi.pa165.dto.MonsterDto;
 import java.util.List;
 
 
 /**
  *
  * @author Auron
  */
 public interface MonsterService {
 
  
  
 
     public MonsterDto save(MonsterDto dto);
     
 	/**
 	 * Updates WeaponDto
          * 
 	 * @param weapon object of type WeaponDto
 	 */
     public MonsterDto update(MonsterDto dto);
 	
     /**
 	 * Deletes WeaponDto
          * 
 	 * @param weapon object of type WeaponDto
 	 */
     public void delete(MonsterDto dto);
     
     public void delete(Long id);
 	/**
 	 * Finds WeaponDto by id
          * 
 	 * @param id ID of the weapon
 	 * @return Weapon object of type WeaponDto
 	 */
    
     public MonsterDto findById(Long id);
     
     public List<MonsterDto> findAll();
 }
     
 
