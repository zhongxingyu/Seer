 /*
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
  * MA  02110-1301, USA.
  *
  * http://www.gnu.org/copyleft/gpl.html
  */
 package system.database;
 
 import system.database.mysql5.*;
 
 import com.aionemu.commons.database.dao.DAOManager;
 
 /**
  * @author Mr. Poke
  *
  */
 public class MySQL5Loader
 {
 	
 	/**
 	 * 
 	 */
 	public MySQL5Loader()
 	{
 		DAOManager.getInstance().registerDAO(new MySQL5AbyssRankDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5AnnouncementsDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5BlockListDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5BrokerDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5DropListDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5FriendListDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5GameTimeDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5InventoryDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5ItemCooldownsDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5ItemStoneListDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5LegionDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5LegionMemberDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5MailDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PetitionDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PlayerAppearanceDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PlayerDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PlayerEffectsDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PlayerLifeStatsDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PlayerPetsDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PlayerPunishmentsDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PlayerQuestListDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PlayerRecipesDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PlayerSettingsDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PlayerSkillListDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5PlayerTitleListDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5SiegeDAO());
 		DAOManager.getInstance().registerDAO(new MySQL5TaskFromDBDAO());
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		new MySQL5Loader();
 	}
 
 }
