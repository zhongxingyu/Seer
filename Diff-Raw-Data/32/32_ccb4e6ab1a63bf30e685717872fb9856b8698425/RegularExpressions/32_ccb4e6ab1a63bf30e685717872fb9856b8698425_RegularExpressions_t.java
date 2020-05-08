 package minimud_shared;
 import java.util.HashMap;
 import java.util.Map;
 
 public class RegularExpressions
 {
 	public enum RegExID
 	{
 		USERNAME,
 		PASSWORD,
 		PORT,
 		IP,
 		DOMAIN,
 		ID,
 		DESCRIPTION,
 		NAME,
 		DIRECTION,
 		NPCTEXT,
 		ACTION_TYPE,
 		RESULT_TYPE,
 		HINT,
 		ACTION_RESULT_DESC,
         QUEST_DEP_COMPLETE,
         POSITIVE_INT,
         WEAPON,
         DAMAGE_TYPE,
         EFFECT,
        STACKABLE,
        RESPAWN_TIMER
 	}
 	
 	private Map<RegExID, String> m_regExMap = new HashMap<RegExID, String>();
 	
 	public RegularExpressions()
 	{
 		m_regExMap.put(RegExID.USERNAME, "^[A-Za-z][A-Za-z0-9]{1,29}$");
 		m_regExMap.put(RegExID.PASSWORD, "^[A-Za-z0-9!@#\\$%,\\.;:\\?<>]{8,20}$");
 		m_regExMap.put(RegExID.PORT, "^[1-9][0-9]{1,4}$");
 		m_regExMap.put(RegExID.IP, "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
 		m_regExMap.put(RegExID.DOMAIN, "^[a-z0-9_-]+(\\.[a-z0-9_-]+)*$");
 		m_regExMap.put(RegExID.ID, "^[1-9][0-9]*$");
 		m_regExMap.put(RegExID.DESCRIPTION, "^[a-zA-Z0-9\\-\\.\\,\\:\\?\\!\\'\\\"\\$\\\\ ]{0,2000}$");
 		m_regExMap.put(RegExID.NAME, "^[a-zA-Z ]{1,50}$");
 		m_regExMap.put(RegExID.DIRECTION, "^(north|south|east|west|northeast|northwest|southeast|southwest|up|down)$");
 		m_regExMap.put(RegExID.NPCTEXT, "^[a-zA-Z0-9\\-\\.\\,\\:\\?\\!\\'\\\"\\$\\\\ ]{1,1000}$");
 		m_regExMap.put(RegExID.ACTION_TYPE, "^(talk|give|kick|punch|stab|slash|push|shoot|take|use)$");
 		m_regExMap.put(RegExID.RESULT_TYPE, "^(text_only|xp_reward|give_quest|update_quest|complete_quest|gold_reward|item_reward|give_health|spawn_monster)$");
 		m_regExMap.put(RegExID.HINT, "^[a-zA-Z0-9\\-\\.\\,\\:\\?\\!\\'\\\"\\$\\\\ ]{1,200}$");
 		m_regExMap.put(RegExID.ACTION_RESULT_DESC, "^[a-zA-Z0-9\\-\\.\\,\\:\\?\\!\\'\\\"\\$\\\\ ]{1,200}$");
         m_regExMap.put(RegExID.QUEST_DEP_COMPLETE, "^[1-9][0-9]*$");
         m_regExMap.put(RegExID.POSITIVE_INT, "^[1-9][0-9]*$");
        m_regExMap.put(RegExID.RESPAWN_TIMER, "^(0|[1-9][0-9]*)$");
         m_regExMap.put(RegExID.WEAPON, "^(0|1)$");
         m_regExMap.put(RegExID.STACKABLE, "^(0|1)$");
         m_regExMap.put(RegExID.DAMAGE_TYPE, "^(piercing|slashing|bashing|magic)$");
         m_regExMap.put(RegExID.EFFECT, "^(give_health|teleport)$");
 	}
 	
 	public boolean stringMatchesRegEx(String str, RegExID id)
 	{
 		boolean bRet = false;
 		
 		String strRegEx = m_regExMap.get(id);
 		
 		if(null != strRegEx)
 		{
 			bRet = str.matches(strRegEx);
 		}
 		
 		return bRet;
 	}
 }
