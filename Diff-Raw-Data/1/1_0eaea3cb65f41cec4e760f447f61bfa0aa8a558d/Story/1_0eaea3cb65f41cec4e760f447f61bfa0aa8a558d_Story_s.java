 
 package ualberta.g12.adventurecreator.data;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Models a story that would be created by an Author. Contains a list of
  * Fragments as well as an author and a title.
  * <p>
  * A stories id must be unique when publishing it online. When publishing a
  * story, if there already a story with an identical ID that story will be
  * overwritten by the newly published story.<br>
  * A story's id is made up of the hashcode of the storyTitle appended with its
  * author. Whenever a story's title or author is changed, its id is updated
  * appropriately.<br>
  * If a story has a null author or title, it will be assigned the ID of
  * {@literal Story.INVALID_ID} and it will be impossible to upload this story
  * through normal means. The design pattern Null Object was used in this class.
  * As can be seen when an object of this class is instantiated all fields are
  * initialized to non-null values.
  * 
  * @see Fragment
  */
 @SuppressWarnings({
         "serial", "rawtypes"
 })
 public class Story extends SModel implements Serializable {
 
     /**
      * Signifies that a story with this id is invalid, usually meaning that
      * either its Title or Author is null.<br>
      * If a story has this id then it will not be publishable.
      */
     public static final int INVALID_ID = -1;
 
     private String storyTitle;
     private String author;
     private int id;
     /** List of all fragments in the story in particular order */
     private List<Fragment> fragments;
     private int startFragPos;
 
     /**
      * Create an Empty Story. This story will have the default start Fragment:
      * "Story Start Fragment" and a title and author of a blank string.
      */
     public Story() {
         this("", "");
     }
 
     /**
      * Create an Empty Story. This story will have the default start Fragment
      * "Story Start Fragment" and the title and author defined.
      * 
      * @param title the title of the new Story
      * @param author the author of the new Story
      */
     public Story(String title, String author) {
         this.storyTitle = title;
         this.author = author;
         updateId();
         this.startFragPos = 0;
         
         Fragment frag = new Fragment();
         frag.setTitle("Story Start Fragment");
         this.fragments = new LinkedList<Fragment>();
     }
 
     private void updateId() {
 
         int newId;
         if (this.storyTitle != null && this.author != null) {
             newId = String.format("%s%s", this.storyTitle, this.author).hashCode();
         } else {
             newId = INVALID_ID;
         }
 
         this.id = newId;
     }
 
     /**
      * The title of a Story is used for a user to identify the Fragment.
      * 
      * @return storyTitle the title of the Story
      */
     public String getTitle() {
         return storyTitle;
     }
 
     /**
      * The title of a Story is used for a user to identify the Story.
      * 
      * @param storyTitle the string to change the title of the Story to
      */
     public void setTitle(String storyTitle) {
         this.storyTitle = storyTitle;
         updateId();
     }
 
     /**
      * startFragPos is the index of the first Fragment in a Story. This is used
      * to selected the first fragment for viewing when reading a story.
      * 
      * @return startFragPos the index of the first fragment in a story
      * @see Fragment
      */
     public int getStartFragPos() {
         return startFragPos;
     }
 
     /**
      * The author field of a Story is used for storing the name of the author of
      * the Story.
      * 
      * @return author the author of the Story
      */
     public String getAuthor() {
         return author;
     }
 
     /**
      * The author field of a Story is used for storing the name of the author of
      * the Story.
      * 
      * @param author the string to set as the author of the Story
      */
     public void setAuthor(String author) {
         this.author = author;
         updateId();
     }
 
     /**
      * The list of Fragment contains all of the Fragments in the Story.
      * Fragments are used for storying sections of the story. These section can
      * be thought of as chapters.
      * 
      * @return fragment The list of Fragments for the Story.
      * @see Fragment
      */
     public List<Fragment> getFragments() {
         return fragments;
     }
 
     /**
      * sets the Fragment list for a Story.
      * The list of Fragment contains all of the Fragments in the Story.
      * Fragments are used for storying sections of the story. These section can
      * be thought of as chapters.
      * 
      * @return fragments The list of Fragments to be set for the Story
      * @see Fragment
      */
     public void setFragments(List<Fragment> fragments) {
         this.fragments = fragments;
     }
 
     /**
      * Returns the fragment at the given position
      * 
      * @param fragPos the position of the fragment to return
      * @return Fragment the fragment at the position if there is one or null
      * @see Fragment
      */
     public Fragment getFragmentAtPos(int fragPos) {
         if (fragPos < fragments.size())
             return fragments.get(fragPos);
         return null;
     }
 
     /**
      * will get id of the story (should be unique). Users are not allowed to set
      * ids, it is done automatically by the system.
      * 
      * @return id of the story
      */
     public int getId() {
         return this.id;
     }
 
     /**
      * Writes the Choice to an ObjectOutputStream.
      * 
      * @param out the ObjectOutputStream to write to
      */
     private void writeObject(java.io.ObjectOutputStream out) throws IOException {
         out.writeObject(this.storyTitle);
         out.writeObject(this.author);
         out.writeObject(this.id);
         out.writeObject(this.fragments);
         out.writeObject(this.startFragPos);
     }
 
     /**
      * Loads the Story from an ObjectInputStream
      * 
      * @param in the ObjectInputStream to read from
      */
     @SuppressWarnings("unchecked")
     private void readObject(java.io.ObjectInputStream in) throws IOException,
             ClassNotFoundException {
         this.storyTitle = (String) in.readObject();
         this.author = (String) in.readObject();
         this.id = (Integer) in.readObject();
         this.fragments = (List<Fragment>) in.readObject();
         this.startFragPos = (Integer) in.readObject();
     }
 
     /**
      * Changes our story to a string (JSON) !
      */
     @Override
     public String toString() {
         return "Recipe [id=" + id + ", storyTitle=" + storyTitle + ", author=" + author
                 + ", fragments="
                 + fragments + ", startFragPos=" + startFragPos + "]";
 
     }
 
 }
