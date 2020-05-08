 /***************************************************************************
 *
 *	Copyright 2010 Quicker Team
 *
 *	Quicker Team is:
 *		Kirdeev Andrey (kirduk@yandex.ru)
 * 	Koritniy Ilya (korizzz230@bk.ru)
 * 	Kolchin Maxim	(kolchinmax@gmail.com)
 */
 /****************************************************************************
 *
 *	This file is part of Quicker.
 *
 *	Quicker is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Quicker is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *	GNU Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Quicker. If not, see <http://www.gnu.org/licenses/>
 
 
 ****************************************************************************/
 
 package Quicker;
 
 import java.util.LinkedList;
 
 public class Note {
     private int id;
     private String title;
     private String text;
     private LinkedList<String> video;
     private LinkedList<String> images;
     private LinkedList<String> audio;
     private String date;
 
     public Note() {
         this(0, "", "", new LinkedList<String>(), new LinkedList<String>(),
                 new LinkedList<String>(), "");
     }
 
     public Note(int id, String title, String text,
             LinkedList<String> video, LinkedList<String> image,
             LinkedList<String> audio, String date) {
         this.id = id;
         this.title = title;
         this.text = text;
         this.video = video;
         this.images = image;
         this.audio = audio;
         this.date = date;
     }
 
     public LinkedList<String> getAudios() {
         return audio;
     }
 
     public String getDate() {
         return date;
     }
 
     public int getId() {
         return id;
     }
 
     public LinkedList<String> getImages() {
         return images;
     }
 
     public String getText() {
 		String sNote = text;
 	//	int i1 = sNote.indexOf("<![CDATA[");
 	//	int i2 = sNote.indexOf("]]>");
 	//	char[] sTemp = sNote.substring(i1 + 9, i2).toCharArray();
 		char[] sTemp = sNote.toCharArray();
 		StringBuilder SB = new StringBuilder();
 		int it = 0;
 		while (it < sTemp.length) {
 			if (sTemp[it] == 0xA) {
 				SB.append("<br>");
 			} else if (sTemp[it] == 0x20) {
 				SB.append("&nbsp;");
 			} else {
 				SB.append(sTemp[it]);
 			}
 			it++;
 		}
         return SB.toString();
     }
 
    public String getRawText() {
        return text;
    }

     public String getTitle() {
         return title;
     }
 
     public LinkedList<String> getVideos() {
         return video;
     }
 
     public void addVideo(String vid) {
         this.video.add(vid);
     }
 
     public void addAudio(String audio) {
         this.audio.add(audio);
     }
 
     public void addImage(String img) {
         this.images.add(img);
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public void removeVideo(int position) {
         this.video.remove(position);
     }
 
     public void removeVideo(String s) {
         this.video.remove(s);
     }
 
     public void removeAudio(int position) {
         this.audio.remove(position);
     }
 
     public void removeAudio(String s) {
         this.audio.remove(s);
     }
 
     public void removeImage(int position) {
         this.images.remove(position);
     }
 
     public void removeImage(String s) {
         this.images.remove(s);
     }
 }
