 /**
  * one line to give the program's name and an idea of what it does.
  Copyright (C) 2006  Thomas Walker
 
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 
 
 package net.sourceforge.x360mediaserve.upnpmediaserver.mediaDatabase.backends.java;
 
 import java.util.Comparator;
 import java.util.TreeSet;
 
 import net.sourceforge.x360mediaserve.upnpmediaserver.mediaDatabase.items.AlbumItem;
 import net.sourceforge.x360mediaserve.upnpmediaserver.mediaDatabase.items.AudioItem;
 
 
 public class Album extends Container{
 	private String name="";	
 
 	protected Artist artist;
 
 	protected TreeSet<AudioItem> songs=new TreeSet<AudioItem>(
 			new Comparator<AudioItem>(){
 				public int compare(AudioItem o1,
 						AudioItem o2){
 					int cmp=o1.albumOrder-o2.albumOrder;
 					if(cmp!=0) return cmp;
 					else{
 						if(o1.title==null) return 1;
 						else if(o2.title==null) return -1;
 						else return o1.title.compareToIgnoreCase(o2.title);
 					}
 				}
 			}
 	);
 
 
 	public Album(String name,Artist artist){
 		this.artist=artist;
 		this.name=name;					
 	}
 
 	public String getName(){
 		return name;
 	}
 
 
 	public void addSong(AudioItem song){
 		songs.add(song);
 	}	
 	
 	protected AlbumItem toAlbumItem()
 	{
 		AlbumItem albumItem=new AlbumItem();					
 		albumItem.numberOfSongs=this.songs.size();
 		albumItem.artist=this.artist.name;
 		albumItem.id=""+this.id;
 		albumItem.name=this.getName();
 		return albumItem;
 	}
 }
