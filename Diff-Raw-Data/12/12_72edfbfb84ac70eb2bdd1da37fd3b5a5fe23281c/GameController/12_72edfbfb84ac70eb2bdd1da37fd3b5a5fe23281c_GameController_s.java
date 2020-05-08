 package com.rightmove.es.controller;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.rightmove.es.domain.Game;
 import com.rightmove.es.dto.GameDto;
 import com.rightmove.es.dto.converter.GameDtoConverter;
 import com.rightmove.es.exception.GameScoreBusinessException;
 import com.rightmove.es.service.GameService;
 
 @Controller
 @RequestMapping("/games")
 public class GameController {
 
 	@Autowired
 	private GameService	gameService;
 	@Autowired
 	private GameDtoConverter gameDtoConverter;
 
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
 	public @ResponseBody
 	GameDto getGame(@PathVariable("id") long id) {
 		Game game = gameService.findByPk(id);
 
 		// TODO how to return errors in SOA?
 		if (game == null) { 
 			throw new GameScoreBusinessException("Game #" + id + " doesn't exist"); 
 		}
 		return gameDtoConverter.convert(game);
 	}
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public @ResponseBody
 	Collection<GameDto> listAll() {
 		Collection<Game> games = gameService.listAll();
		games = new ArrayList<Game>();
 		return gameDtoConverter.convert(games);
 	}
 }
