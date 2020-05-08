 /*
  * Copyright (C) 2009 Matthias Ableitner (http://abma.de/)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package agai.loader;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import com.springrts.ai.AIFloat3;
 import com.springrts.ai.oo.AbstractOOAI;
 import com.springrts.ai.oo.OOAI;
 import com.springrts.ai.oo.OOAICallback;
 import com.springrts.ai.oo.Unit;
 import com.springrts.ai.oo.WeaponDef;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class AGLoader.
  */
 public class AGLoader extends AbstractOOAI implements OOAI {
 
 	/** The clb. */
 	private OOAICallback clb;
 
 	/** The sub ai. */
 	private IAGAI subAI;
 
 	/** The team id. */
 	private int teamId;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#commandFinished(com.springrts.ai.oo.
 	 * Unit, int, int)
 	 */
 	@Override
 	public int commandFinished(Unit unit, int commandId, int commandTopicId) {
 		try {
 			subAI.commandFinished(unit, commandId, commandTopicId);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/**
 	 * Do reload.
 	 * 
 	 * @param teamId
 	 *            the team id
 	 * @param clb
 	 *            the clb
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	public void doReload(int teamId, OOAICallback clb) throws Exception {
 		subAI = null;
 		System.runFinalization();
 		System.gc();
 		System.gc();
 		this.subAI = reloadTheClass();
 		try{
 			subAI.init(teamId, clb);
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#enemyDamaged(com.springrts.ai.oo.Unit,
 	 * com.springrts.ai.oo.Unit, float, com.springrts.ai.AIFloat3,
 	 * com.springrts.ai.oo.WeaponDef, boolean)
 	 */
 	@Override
 	public int enemyDamaged(Unit enemy, Unit attacker, float damage,
 			AIFloat3 dir, WeaponDef weaponDef, boolean paralyzer) {
 		try {
 			subAI.enemyDamaged(enemy, attacker, damage, dir, weaponDef,
 					paralyzer);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#enemyDestroyed(com.springrts.ai.oo.Unit,
 	 * com.springrts.ai.oo.Unit)
 	 */
 	@Override
 	public int enemyDestroyed(Unit enemy, Unit attacker) {
 		try {
 			subAI.enemyDestroyed(enemy, attacker);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#enemyEnterLOS(com.springrts.ai.oo.Unit)
 	 */
 	@Override
 	public int enemyEnterLOS(Unit enemy) {
 		try {
 			subAI.enemyEnterLOS(enemy);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#enemyEnterRadar(com.springrts.ai.oo.
 	 * Unit)
 	 */
 	@Override
 	public int enemyEnterRadar(Unit enemy) { // never call
 												// enemy.getDef().getName()!!
 		try {
 			subAI.enemyEnterRadar(enemy);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#enemyLeaveLOS(com.springrts.ai.oo.Unit)
 	 */
 	@Override
 	public int enemyLeaveLOS(Unit enemy) {
 		try {
 			subAI.enemyLeaveLOS(enemy);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#enemyLeaveRadar(com.springrts.ai.oo.
 	 * Unit)
 	 */
 	@Override
 	public int enemyLeaveRadar(Unit enemy) { // never call
 												// enemy.getDef().getName()!!
 		try {
 			subAI.enemyLeaveRadar(enemy);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/**
 	 * Gets the uRL class loader.
 	 * 
 	 * @param jarURL
 	 *            the jar url
 	 * 
 	 * @return the uRL class loader
 	 */
 	private URLClassLoader getURLClassLoader(URL jarURL) {
 		ClassLoader baseClassLoader = AGLoader.class.getClassLoader();
 		if (baseClassLoader == null)
 			baseClassLoader = ClassLoader.getSystemClassLoader();
 		return new URLClassLoader(new URL[] { jarURL }, baseClassLoader);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.springrts.ai.oo.AbstractOOAI#init(int,
 	 * com.springrts.ai.oo.OOAICallback)
 	 */
 	@Override
 	public int init(int teamId, OOAICallback callback) {
 		this.clb = callback;
 		this.teamId = teamId;
 		try {
 			if (subAI == null)
 				doReload(teamId, callback);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.springrts.ai.oo.AbstractOOAI#message(int, java.lang.String)
 	 */
 	@Override
 	public int message(int player, String message) {
 		if (message.equalsIgnoreCase("reload"))
 			try {
 				doReload(teamId, clb);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		else {
 			try {
 				subAI.message(player, message);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return 0;
 	}
 
 	/**
 	 * Reload the class.
 	 * 
 	 * @return the iAGAI
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	public IAGAI reloadTheClass() throws Exception {
 		subAI = null;
 		String path = clb.getDataDirs().getConfigDir() + new String( new byte[]{ (byte)(clb.getDataDirs().getPathSeparator() ) } );
 		URLClassLoader urlLoader = getURLClassLoader(new URL("file", null,
				path + "/UnderlyingAI-src.jar"));
 		Class<?> cl = urlLoader.loadClass("agai.AGAI");
 		if (!IAGAI.class.isAssignableFrom(cl)) {
 			throw new RuntimeException("Invalid class");
 		}
 		Object newInstance = cl.newInstance();
 		return (IAGAI) newInstance;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#unitCreated(com.springrts.ai.oo.Unit,
 	 * com.springrts.ai.oo.Unit)
 	 */
 	@Override
 	public int unitCreated(Unit unit, Unit builder) {
 		try {
 			subAI.unitCreated(unit, builder);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#unitDamaged(com.springrts.ai.oo.Unit,
 	 * com.springrts.ai.oo.Unit, float, com.springrts.ai.AIFloat3,
 	 * com.springrts.ai.oo.WeaponDef, boolean)
 	 */
 	@Override
 	public int unitDamaged(Unit unit, Unit attacker, float damage,
 			AIFloat3 dir, WeaponDef weaponDef, boolean paralyzer) {
 		try {
 			subAI
 					.unitDamaged(unit, attacker, damage, dir, weaponDef,
 							paralyzer);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#unitDestroyed(com.springrts.ai.oo.Unit,
 	 * com.springrts.ai.oo.Unit)
 	 */
 	@Override
 	public int unitDestroyed(Unit unit, Unit attacker) {
 		try {
 			subAI.unitDestroyed(unit, attacker);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#unitFinished(com.springrts.ai.oo.Unit)
 	 */
 	@Override
 	public int unitFinished(Unit unit) {
 		try {
 			subAI.unitFinished(unit);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.springrts.ai.oo.AbstractOOAI#unitIdle(com.springrts.ai.oo.Unit)
 	 */
 	@Override
 	public int unitIdle(Unit unit) {
 		try {
 			subAI.unitIdle(unit);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.springrts.ai.oo.AbstractOOAI#unitMoveFailed(com.springrts.ai.oo.Unit)
 	 */
 	@Override
 	public int unitMoveFailed(Unit unit) {
 		try {
 			subAI.unitMoveFailed(unit);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.springrts.ai.oo.AbstractOOAI#update(int)
 	 */
 	@Override
 	public int update(int frame) {
 		try {
 			subAI.update(frame);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 }
