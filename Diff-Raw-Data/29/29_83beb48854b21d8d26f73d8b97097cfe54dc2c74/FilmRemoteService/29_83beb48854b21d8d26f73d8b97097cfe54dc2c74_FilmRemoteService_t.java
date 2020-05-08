 package com.alextim.diskarchive.dwr.services.impl;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 
 import com.alextim.diskarchive.dao.factory.CoreDAOFactory;
 import com.alextim.diskarchive.dwr.services.IFilmRemoteService;
 import com.alextim.diskarchive.entity.Author;
 import com.alextim.diskarchive.entity.Film;
 import com.alextim.diskarchive.entity.FilmGroup;
 
 public class FilmRemoteService implements IFilmRemoteService{
 
 	private CoreDAOFactory coreDAOFactory;
 
 	public FilmRemoteService(CoreDAOFactory coreDAOFactory) {
 		this.coreDAOFactory = coreDAOFactory;
 	}
 	
 	@Override
 	public void addFilm() {
		//FilmGroup filmGroup = coreDAOFactory.getFilmGroupDAO().getFirst();
 		//Author author = coreDAOFactory.getAuthorDAO().getFirst();
 		
 		Film film = new Film();
 		
 		film.setDescription("");
		//film.setFilmGroup(filmGroup);
 		film.setName("");
 		//film.setAuthor(author);
 	
 		coreDAOFactory.getFilmDAO().saveOrUpdate(film);
 	}
 
 	@Override
 	public void deleteFilm(Long id) {
 		Film film = coreDAOFactory.getFilmDAO().getById(id);
 		coreDAOFactory.getFilmDAO().delete(film);
 	}
 
 	@Override
 	public void save(String jsonResult) {
 		JSONObject array = (JSONObject)JSONSerializer.toJSON(jsonResult);
 
 		Map<Long, JSONObject> result = new HashMap<Long, JSONObject>();
 		for (Object keyObj : array.keySet()) {
 			String key = keyObj.toString();
 			String[] parts = key.split("_");
 			
 			Long id = Long.parseLong(parts[1]);
 			JSONObject changes = (JSONObject)array.get(key);
 
 			result.put(id, changes);
 			
 			Film film = coreDAOFactory.getFilmDAO().getById(id);
 		}
 		
 		/*
 		Object jObj = JSONSerializer.toJava(changes);
 		jObj.
 		
 		*/
 	}
 	
 
 }
