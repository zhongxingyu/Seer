 package com.genfersco.sepbas.app.services.impl;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.genfersco.sepbas.app.services.ClubManager;
 import com.genfersco.sepbas.domain.model.Club;
 import com.genfersco.sepbas.domain.repository.ClubRepository;
 
 @Service("clubManager")
 public class ClubManagerImpl implements ClubManager{
 	@Autowired
 	private ClubRepository clubRepository;
 	@Override
 	public List<Club> getClubes() {
 		return clubRepository.findAll();
 	}
 
 	@Override
 	public Club addClub(Club club) {
 		return clubRepository.save(club);
 	}
 	
 	@Override
 	public void deleteClub(Integer id){
		// TODO: eliminar solo aquellos clubes sin jugadores relacionados.
//		clubRepository.delete(id); 
 	}
 
 	@Override
 	public Club getClub(Integer id) {
 		return clubRepository.findOne(id);
 	}
 	
 }
