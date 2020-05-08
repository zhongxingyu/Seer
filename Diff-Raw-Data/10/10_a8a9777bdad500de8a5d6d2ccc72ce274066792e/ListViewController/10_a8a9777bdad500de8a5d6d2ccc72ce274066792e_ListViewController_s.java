 import java.util.Vector;
 
 public class ListViewController {
   private List list;
   private Grid grid;
   private Vector<Sound> soundModels;
 
   public ListViewController(List l, Vector<Sound> soundModels) {
     this.list = l;
     this.grid = new Grid();
     this.soundModels = soundModels;
     addPanelsToGrid();
   }
 
   private void addPanelsToGrid() {
     int count = 0;
     for(int i=0;i<Grid.numRows;i++) {
       for(int j=0;j<Grid.numColumns;j++) {
         if(count >= soundModels.size()) {
           return;
         }
         grid.setPanelAtLocation(new SoundPanel(soundModels.get(count)), i, j);
         count++;
       }
     }
   }
 
   public void addSound(Sound s) {
     int[] lastLocation = {-1,-1};
     if(soundModels.size() > 0) {
       lastLocation = gridLocationForSound(soundModels.lastElement());
     }
     soundModels.add(s);
     if(lastLocation[0] == -1 && lastLocation[1] == -1) {
       grid.setPanelAtLocation(new SoundPanel(s), 0, 0);
       return;
     }
     lastLocation[1] += 1;
     if(lastLocation[1] >= Grid.numColumns) {
       lastLocation[1] = 0;
       lastLocation[0] += 1;
     }
     grid.setPanelAtLocation(new SoundPanel(s), lastLocation[0], lastLocation[1]);
   }
 
   public void soundDidEdit(Sound newSound) {
     for(Sound s : soundModels) {
       if(s.getID() == newSound.getID()) {
         int[] location = gridLocationForSound(s);
         grid.setPanelAtLocation(new SoundPanel(s), location[0], location[1]);
       }
     }
   }
 
   public void soundWasRemoved(Sound s) {
     int index = soundModels.indexOf(s);
     int[] gridLocation = gridLocationForSound(s);
     soundModels.remove(s);
     int count = index;
     for(int i=gridLocation[0];i<Grid.numRows;i++) {
      for(int j=gridLocation[1];j<Grid.numColumns;j++) {
         if(count >= soundModels.size()) {
           grid.removePanelAtLocation(i,j); //remove the last one since we are iterating over fewer sounds now
           return;
         }
         grid.setPanelAtLocation(new SoundPanel(soundModels.get(count)), i, j);
         count++;
       }
     }
   }
 
   //returns all of the IDs of sounds that were in this list
   public int[] willDelete() {
     int[] soundIDs = new int[soundModels.size()];
     for(int i=0;i<soundModels.size();i++) {
       Sound s = soundModels.get(i);
       soundIDs[i] = s.getID();
       s.setListID(-1);
       s.save();
     }
     return soundIDs;
   }
 
   public int[] gridLocationForSound(Sound s) {
     int count = 0;
     int[] retval = {-1, -1};
     for(int i=0;i<Grid.numRows;i++) {
       for(int j=0;j<Grid.numColumns;j++) {
         if(count >= soundModels.size()) {
           return retval;
         }
         if(soundModels.get(count).getID() == s.getID()) {
           retval[0] = i;
           retval[1] = j;
           return retval;
         }
         count++;
       }
     }
     return retval;
   }
 
   private int indexForSoundID(int id) {
     for(int i=0;i<soundModels.size();i++) {
       if(soundModels.get(i).getID() == id) {
         return i;
       }
     }
     return -1;
   }
 
   public List getList() {
     return this.list;
   }
 
   public Grid getGrid() {
     return this.grid;
   }
 
   public String toString() {
     return list.toString();
   }
 }
