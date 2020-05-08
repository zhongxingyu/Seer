 /*
  * This file is part of mmoMinecraft (https://github.com/mmoMinecraftDev).
  *
  * mmoMinecraft is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
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
 package mmo.Chat;
 
 import com.avaje.ebean.validation.NotEmpty;
 import com.avaje.ebean.validation.NotNull;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
@Entity
@Table(name = "mmo_Chat")
 public class ChatDB {
 
 	@Id
 	@NotNull
 	@NotEmpty
 	private String player;
 
 	@NotNull
	private String channel = "";
 
 	@NotNull
	private String channelList = "";

	public ChatDB() {}

	public ChatDB(String player) {
		this.player = player;
	}
 
 	public String getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(String player) {
 		this.player = player;
 	}
 
 	public String getChannel() {
 		return channel;
 	}
 
 	public void setChannel(String channel) {
 		this.channel = channel;
 	}
 
 	public String getChannelList() {
 		return channelList;
 	}
 
 	public void setChannelList(String channelList) {
 		this.channelList = channelList;
 	}
 }
