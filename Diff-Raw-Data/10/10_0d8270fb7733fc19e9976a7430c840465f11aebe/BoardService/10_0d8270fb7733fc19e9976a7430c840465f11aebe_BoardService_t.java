 /*
  User: erez
  Date: 2/21/13
  Time: 9:28 PM
  */
 package com.moshavit.services;
 
 import com.moshavit.model.BoardMessage;
 import com.moshavit.persistence.BoardRepository;
 import org.jboss.resteasy.spi.NotFoundException;
 import org.springframework.stereotype.Service;
 
 import javax.inject.Inject;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import java.util.Collection;
 
 @Service
 @Path("/boardMessage")
 @Produces(MediaType.APPLICATION_JSON)
 @Consumes(MediaType.APPLICATION_JSON)
 public class BoardService {
 
 	private final BoardRepository repository;
 
 	@Inject
 	public BoardService(BoardRepository repository) {
 		this.repository = repository;
 	}
 
 	@GET
 	public Collection<BoardMessage> getBoardMessages() {
 		return repository.getBoardMessages();
 	}
 
 	@GET
 	@Path("/{id}")
 	public BoardMessage getBoardMessage(@PathParam("id") Long id) {
 		BoardMessage boardMessage = repository.getBoardMessage(id);
 		if (boardMessage == null) {
 			throw new NotFoundException("No such BoardMessage " + id);
 		}
 		return boardMessage;
 	}
 
 	@POST
	public void createNewBoardMessage(BoardMessage boardMessage) {
		repository.addBoardMessage(boardMessage);
 	}
 
 	@PUT
 	@Path("/{id}")
	public void updateUser(BoardMessage boardMessage, @PathParam("id") Long id) {
 		boardMessage.setId(id);
		repository.updateBoardMessage(boardMessage);
 	}
 }
