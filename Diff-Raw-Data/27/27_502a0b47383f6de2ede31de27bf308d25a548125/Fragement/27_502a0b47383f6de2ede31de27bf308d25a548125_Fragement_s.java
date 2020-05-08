 /*
 Adventure App - Allows you to create an Adventure Book, or Download
         books from other authors.
 Copyright (C) Fall 2013 Team 5 CMPUT 301 University of Alberta
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.uofa.adventure_app.model;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.SortedMap;
 import java.util.UUID;
 
 import com.uofa.adventure_app.interfaces.UniqueId;
 /**
  * Holds the Fragement object in the model.
  * @author Kevin Lafond, Chris Pavlicek, Joel Malina
  *
  */
 public class Fragement extends UniqueId implements Serializable  {
         
         private ArrayList<Media> media; // Key will be a line number
         private ArrayList<Annotation> annotations;
         private ArrayList<Choice> choices;
         private String body;
         private boolean randomFlag;
         private String title;
 
         public Fragement()
         {
             super();
             this.setAnnotations(new ArrayList<Annotation>());
             this.choices = new ArrayList<Choice>(); 
             this.media = new ArrayList<Media>();
             this.randomFlag = false;
             this.title = null;
             this.body = "";
         }      
 
         public Fragement(boolean flag)
         {
                 super();
                 this.setAnnotations(new ArrayList<Annotation>());
                 this.choices = new ArrayList<Choice>(); 
                 this.media = null;
                 this.randomFlag = flag;
         }    
         public Fragement(UUID id)
         {
         	this();
         	this._id = id;
         } 
         
         public Fragement(String body, boolean flag)
         {
                 this();
                 this.setBody(body);
                 this.randomFlag = flag;
         }       
         
         public Fragement(String title, String body, boolean flag)
         {
                 this();
                 this.title = title;
                 this.setBody(body);
                 this.randomFlag = flag;
         }   
         
         /**
          * adds a choice to the fragment
          * @param Choice choice
          */
         public void addChoice(Choice choice)
         {
         	// Don't add duplicate choices.
         	// Should be handled in ui but this is a double check
         	if(!this.choices().contains(choice)) {
                 this.choices.add(choice);
         	}
         }
 
         /**
          * @return String
          */
         public String body()
         {
                 return body;
         }
 
         /**
          * @param body the body to set
          */
         public void setBody(String body)
         {
                 this.body = body;
         }
         /**
          * Returns all of the choice for a fragement.
          * @return ArrayList<Choice>
          */
         public ArrayList<Choice> choices()
         {
         	return this.choices;
         }
         /**
          * sets all of the choices for a fragement
          * @param ArrayList<Choice> c
          */
         public void setChoices(ArrayList<Choice> c)
         {	
         	this.choices = c;
         }
         /**
          * sets a flag that allows the user to have a 
          * random choice when reading the story.
          * @param boolean flag
          */
         public void setRandomFlag(boolean flag)
         {
         	this.randomFlag = flag;
         }
         /**
          * gets the flag that allows the user to have a 
          * random choice when reading the story.
          * @return boolean
          */
         public boolean getRandomflag()
         {
         	return this.randomFlag;
         }
         /**
          * Sets the title of the Fragement.
          * @param String title
          */
         public void setTitle(String title)
         {
         	this.title = title;
         }
         /**
          * Gets the title of the fragement
          * @return String
          */
         public String getTitle()
         {
         	return this.title;
         }
         /**
          * Returns a list of media associated with a fragement.
          * @return ArrayList<Media>
          */
     	public ArrayList<Media> media() {
     		return this.media;
     	}
     	/**
     	 * sets a list of media to be associated with a Fragement.
     	 * @param ArrayList<Media> media
     	 */
     	public void setMedia(ArrayList<Media> media) {
     		this.media = media;
     	}
     	
     	/**
     	 * add media to the sorted set. Right now media will be added to the Start
     	 * Feature to add to any line later on.
     	 * @param Media media
     	 */
     	public void addMedia(Media media) {
     		this.media().add(media);
     	}
     	@Override
     	public boolean equals(Object o) {
     		// TODO Auto-generated method stub
     		if(o.getClass().equals(this.getClass())) {
     			Fragement frag = (Fragement) o;
     			if (this.uid().equals(frag.uid()))
     				return true;
     			else
     				return false;
     		} else {
     			return super.equals(o);
     		}
     		
  
     	}
         
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			// TODO Auto-generated method stub
 			return this.getTitle();
 		}   
         
 		/**
 		 * Always treat de-serialization as a full-blown constructor, by validating
 		 * the final state of the de-serialized object.
 		 */
 		private void readObject(ObjectInputStream aInputStream)
 				throws ClassNotFoundException, IOException {
 			// always perform the default de-serialization first
 			aInputStream.defaultReadObject();
 
 		}
 
 		/**
 		 * This is the default implementation of writeObject. Customise if
 		 * necessary.
 		 */
 		private void writeObject(ObjectOutputStream aOutputStream)
 				throws IOException {
 			// perform the default serialization for all non-transient, non-static
 			// fields
 			aOutputStream.defaultWriteObject();
 		}
         
 		public Fragement localCopy() {
 			Fragement f = new Fragement();
 			f.setBody(new String(this.body()));
 			f.setTitle(new String(this.getTitle()));
 			f.setRandomFlag(this.getRandomflag());
 			
 			ArrayList<Media> copyMedia = new ArrayList<Media>();
 			for (Media m : this.media()) 
 			{
 				copyMedia.add(m);
 			}
 			f.setMedia(copyMedia);
 			
 			return f;
 		}
 
 		/**
 		 * Returns a list of annotations associated with a fragement.
 		 * @return ArrayList<Annotation>
 		 */
 		public ArrayList<Annotation> annotations() {
 			return annotations;
 		}
 
 		/**
		 * Adds an Annotation to the Fragement
 		 * @param Annotiation a
 		 */
 		public void addAnnotation(Annotation a) {
 				this.annotations().add(a);
 		}
 		/**
 		 * Replaces an annotation with another updates one.
 		 * @param Annotation a
 		 */
 		public void replaceAnnotation(Annotation a) {
 			if(annotations.contains(a)) {
 				int index = this.annotations.indexOf(a);
 				this.annotations.set(index, a);
 			}
 		}
 		/**
 		 * adds a whole list of annotations to the Fragement
 		 * @param ArrayList<Annotation> annotations
 		 */
 		public void setAnnotations(ArrayList<Annotation> annotations) {
 			this.annotations = annotations;
 		}
 		/**
 		 * creates a new fragement with the same ID.
 		 * @return Fragement
 		 */
 		public Fragement stripFragement() {
 			return new Fragement(this.uid());
 		}
 
 }
