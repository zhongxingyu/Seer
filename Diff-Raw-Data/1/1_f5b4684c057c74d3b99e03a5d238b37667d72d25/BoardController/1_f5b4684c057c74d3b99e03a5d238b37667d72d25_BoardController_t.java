 package com.greenboard.board;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class BoardController {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(BoardController.class);
 
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/board", method = RequestMethod.GET)
 	public String home() {
 		logger.info("requesting home");
 		return "home";
 	}
 	
 	@RequestMapping(value="/boards", method=RequestMethod.GET)
 	public @ResponseBody List<String> getBoards() {
 	    ArrayList<String> boards = new ArrayList<String>();
 	    boards.add("board1");
 		return boards;
 	}
 
 }
