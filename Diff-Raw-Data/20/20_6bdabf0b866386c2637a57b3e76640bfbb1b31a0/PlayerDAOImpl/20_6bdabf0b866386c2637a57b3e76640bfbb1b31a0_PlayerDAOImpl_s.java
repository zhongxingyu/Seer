 package com.pingpong.core.dao.impl;
 
 import com.pingpong.core.dao.PlayerDAO;
 import com.pingpong.domain.Player;
 
 /**
  * @author Artur Zhurat
  * @version 3.0
  * @since 25/12/2011
  */
 public class PlayerDAOImpl extends AbstractDAO<Integer, Player> implements PlayerDAO{
 	public PlayerDAOImpl() {
 		super(Player.class);
 	}
 }
