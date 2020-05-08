 package com.pingpong.core.dao.impl;
 
 import com.pingpong.core.dao.PlayerDAO;
 import com.pingpong.domain.Player;
import net.sf.oval.guard.Guarded;
 
 /**
  * @author Artur Zhurat
  * @version 3.0
  * @since 25/12/2011
  */
@Guarded
 public class PlayerDAOImpl extends AbstractDAO<Integer, Player> implements PlayerDAO{
 	public PlayerDAOImpl() {
 		super(Player.class);
 	}
 }
