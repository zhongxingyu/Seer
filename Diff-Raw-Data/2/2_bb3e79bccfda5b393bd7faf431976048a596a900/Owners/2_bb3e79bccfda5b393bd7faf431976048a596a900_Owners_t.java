 /******************************************************************************\
 |                                     ,,                                       |
 |                    db             `7MM                                       |
 |                   ;MM:              MM                                       |
 |                  ,V^MM.    ,pP"Ybd  MMpMMMb.  .gP"Ya `7Mb,od8                |
 |                 ,M  `MM    8I   `"  MM    MM ,M'   Yb  MM' "'                |
 |                 AbmmmqMA   `YMMMa.  MM    MM 8M""""""  MM                    |
 |                A'     VML  L.   I8  MM    MM YM.    ,  MM                    |
 |              .AMA.   .AMMA.M9mmmP'.JMML  JMML.`Mbmmd'.JMML.                  |
 |                                                                              |
 |                                                                              |
 |                                ,,    ,,                                      |
 |                     .g8"""bgd `7MM    db        `7MM                         |
 |                   .dP'     `M   MM                MM                         |
 |                   dM'       `   MM  `7MM  ,p6"bo  MM  ,MP'                   |
 |                   MM            MM    MM 6M'  OO  MM ;Y                      |
 |                   MM.    `7MMF' MM    MM 8M       MM;Mm                      |
 |                   `Mb.     MM   MM    MM YM.    , MM `Mb.                    |
 |                     `"bmmmdPY .JMML..JMML.YMbmd'.JMML. YA.                   |
 |                                                                              |
 \******************************************************************************/
 /******************************************************************************\
 | Copyright (c) 2012, Asher Glick                                              |
 | All rights reserved.                                                         |
 |                                                                              |
 | Redistribution and use in source and binary forms, with or without           |
 | modification, are permitted provided that the following conditions are met:  |
 |                                                                              |
 | * Redistributions of source code must retain the above copyright notice,     |
 |   this list of conditions and the following disclaimer.                      |
 | * Redistributions in binary form must reproduce the above copyright notice,  |
 |   this list of conditions and the following disclaimer in the documentation  |
 |   and/or other materials provided with the distribution.                     |
 |                                                                              |
 | THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  |
 | AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE    |
 | IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE   |
 | ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE    |
 | LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR          |
 | CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF         |
 | SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS     |
 | INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN      |
 | CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)      |
 | ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   |
 | POSSIBILITY OF SUCH DAMAGE.                                                  |
 \******************************************************************************/
 
 package iggy.Regions;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class Owners {
 	private Set <String> _owners = new HashSet<String>();
 	private Set <String> _builders = new HashSet<String>();
 	
 	/********************************** ADD OWNER *********************************\
 	|
 	\******************************************************************************/
 	public boolean addOwner(String player){
 		return _owners.add(player);
 	}
 	/********************************* ADD OWNERS *********************************\
 	|
 	\******************************************************************************/
 	public boolean addOwners(List<String> players){
 		boolean sucess = true;
 		for (String player : players){
 			sucess &= _owners.add(player);
 		}
 		return sucess;
 	}
 	/********************************* ADD BUILDER ********************************\
 	|
 	\******************************************************************************/
 	public boolean addBuilder (String player) {
 		return _builders.add(player);
 	}
 	/******************************** ADD BUILDERS ********************************\
 	|
 	\******************************************************************************/
 	public boolean addBuilders (List <String> players) {
 		boolean sucess = true;
 		for (String player : players) {
			sucess &= _builders.add(player);
 		}
 		return sucess;
 	}
 	/******************************* REMOVE BUILDER *******************************\
 	|
 	\******************************************************************************/
 	public boolean removeBuilder (String player) {
 		return _builders.remove(player);
 	}
 	/********************************** HAS OWNER *********************************\
 	|
 	\******************************************************************************/
 	public boolean hasOwner(String player){
 		return _owners.contains(player);
 	}
 	/********************************* HAS BUILDER ********************************\
 	|
 	\******************************************************************************/
 	public boolean hasBuilder (String player) {
 		return _builders.contains(player);
 	}
 	/********************************* HAS PLAYER *********************************\
 	|
 	\******************************************************************************/
 	public boolean hasPlayer (String player) {
 		return (_owners.contains(player) || _builders.contains(player));
 	}
 	/********************************* GET OWNERS *********************************\
 	|
 	\******************************************************************************/
 	public List<String> getOwners() {
 		List<String> owners = new ArrayList<String>();
 		for (String owner : _owners){ owners.add(owner); }
 		return owners;
 	}
 	/******************************** GET BUILDERS ********************************\
 	|
 	\******************************************************************************/
 	public List <String> getBuilders() {
 		List <String> builders = new ArrayList <String>();
 		for (String builder : _builders) { builders.add(builder); }
 		return builders;
 	}
 	/********************************* GET PLAYERS ********************************\
 	|
 	\******************************************************************************/
 	public List <String> getPlayers() {
 		List <String> players = new ArrayList <String> ();
 		for (String owner : _owners) { players.add(owner); }
 		for (String builder : _builders) {players.add(builder); }
 		return players;
 	}
 }
