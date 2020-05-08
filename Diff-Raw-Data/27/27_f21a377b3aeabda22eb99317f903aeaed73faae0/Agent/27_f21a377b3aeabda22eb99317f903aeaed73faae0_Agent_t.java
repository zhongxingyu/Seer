 /*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */
 
 /**
  * @author awaterma@ecosur.mx
  */
 
 package mx.ecosur.multigame.model.interfaces;
 
import java.util.List;
 
 public interface Agent extends GamePlayer {
 
     public void initialize();
 
     public boolean ready();
 
    public List<Move> determineMoves(Game game);
 
     public Suggestion processSuggestion (Game game, Suggestion suggestion);
 }
