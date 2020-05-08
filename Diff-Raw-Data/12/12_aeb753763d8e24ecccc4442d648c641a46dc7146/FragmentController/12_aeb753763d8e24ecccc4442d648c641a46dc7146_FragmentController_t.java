 
 package ualberta.g12.adventurecreator;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.graphics.drawable.Drawable;
 
 
 public class FragmentController implements FController {
     
     @Override
     public void editTitle(Fragment frag, String newTitle){
         frag.setTitle(newTitle);
     }
 
     public static void addTextSegment(Fragment frag, String textSegment){
         List<String> textSegments = frag.getTextSegments();
         textSegments.add(textSegment);
         frag.setTextSegments(textSegments);
 
         //insert t into corresponding spot in display order
         List<String> displayOrder = frag.getDisplayOrder();
         
         int i=0;        
         while (i < displayOrder.size()){
             if (displayOrder.get(i).equals("c"))
                 break;
             i++;
         }
         if (displayOrder.size()==i){
             displayOrder.add("t");
         } else {
             displayOrder.add(i, "t");
         }
         
         frag.setDisplayOrder(displayOrder);
     }
     
     public static boolean addTextSegment(Fragment frag, String textSegment, int dispNum){
         List<String> textSegments = frag.getTextSegments();
         List<String> displayOrder = frag.getDisplayOrder();
         
         //check for invalid dispNum
         if (displayOrder.size()<=dispNum)
             return false;
         
         //Insert the text segment
         int textSegNum=0;
         for (int i=0; i<dispNum;i++){
             if (displayOrder.get(i).equals("t"))
                 textSegNum++;
         }
         if (textSegments.size()==textSegNum){
             textSegments.add(textSegment);
         } else {
             textSegments.add(textSegNum, textSegment);
         }
 
         //insert t into corresponding spot in display order
         displayOrder.add(dispNum, "t");
         
         frag.setDisplayOrder(displayOrder);
         frag.setTextSegments(textSegments);
         
         return true;
     }
     
 //  TODO  
 //    @Override
 //    public void editTextSegment(Fragment frag, String textSegment){
 //        //TODO
 //    }
 
     public void deleteTextSegment(Fragment frag, String textSegment){
         List<String> textSegments = frag.getTextSegments();
         List<String> displayOrder = frag.getDisplayOrder();
 
         //find the entry of display order that should be removed
         int occurence = textSegments.indexOf(textSegment);
         int i = 0;
         while(occurence >= 0){
             if (displayOrder.get(i) == "t"){
                 occurence--;
             }
             i++;
         }
         displayOrder.remove(1-1);
         frag.setDisplayOrder(displayOrder);
 
         textSegments.remove(textSegment);
         frag.setTextSegments(textSegments);
     }
     
     /**
      * Removes the text segment at the given dispNum
      * returns true if successful
      * @param frag
      * @param dispNum
      * @return
      */
     private static boolean deleteTextSegment(Fragment frag, int dispNum){
         List<String> textSegments = frag.getTextSegments();
         List<String> displayOrder = frag.getDisplayOrder();
 
         //not a text segment at dispNum
         if (!displayOrder.get(dispNum).equals("t"))
             return false;
         
         int textSegNum=0;
         for (int i=0; i<dispNum;i++){
             if (displayOrder.get(i).equals("t"))
                 textSegNum++;
         }
 
         textSegments.remove(textSegNum);
         frag.setTextSegments(textSegments);
         displayOrder.remove(dispNum);
         frag.setDisplayOrder(displayOrder);
         return true;
     }
 
     @Override
     public void addIllustration(Fragment frag, Drawable illustration){
         List<Drawable> illustrations = frag.getIllustrations();
         illustrations.add(illustration);
         frag.setIllustrations(illustrations);
 
         //insert t into corresponding spot in display order
         List<String> displayOrder = frag.getDisplayOrder();
         int i=0;
         
         while (i < displayOrder.size()){
             if (displayOrder.get(i).equals("c") || displayOrder.get(i).equals("e"))
                 break;
             i++;
         }
         if (displayOrder.size()==i){
             displayOrder.add("i");
         } else {
             displayOrder.add(i, "i");
         }
         frag.setDisplayOrder(displayOrder);
     }
     
     public static boolean addIllustration(Fragment frag, Drawable illustration, int dispNum){
         List<Drawable> illustrations = frag.getIllustrations();
         List<String> displayOrder = frag.getDisplayOrder();
         
         //check for invalid dispNum
         if (displayOrder.size()<=dispNum)
             return false;
         
         //Insert the text segment
         int illNum=0;
         for (int i=0; i<dispNum;i++){
            if (displayOrder.get(i).equals("i"))
                 illNum++;
         }
         if (illustrations.size()==illNum){
            illustrations.add(illustration);
         } else {
             illustrations.add(illNum, illustration);
         }
 
         //insert t into corresponding spot in display order
        displayOrder.add(dispNum, "i");
         
         frag.setDisplayOrder(displayOrder);
         frag.setIllustrations(illustrations);
         
         return true;
     }
 
     public void deleteIllustration(Fragment frag, Drawable illustration){
         List<Drawable> illustrations = frag.getIllustrations();
         List<String> displayOrder = frag.getDisplayOrder();
 
         //find the entry of display order that should be removed
         int occurence = illustrations.indexOf(illustration);
         int i = 0;
         while(occurence >= 0){
             if (displayOrder.get(i) == "i"){
                 occurence--;
             }
             i++;
         }
         displayOrder.remove(1-1);
         frag.setDisplayOrder(displayOrder);
 
         illustrations.remove(illustration);
         frag.setIllustrations(illustrations);
     }
     
     /**
      * Removes the illustration at the given dispNum
      * returns true if successful
      * @param frag
      * @param dispNum
      * @return
      */
     private static boolean deleteIllustration(Fragment frag, int dispNum){
         List<String> textSegments = frag.getTextSegments();
         List<String> displayOrder = frag.getDisplayOrder();
 
         //not a text segment at dispNum
         if (!displayOrder.get(dispNum).equals("i"))
             return false;
         
         int illustrationNum=0;
         for (int i=0; i<dispNum;i++){
             if (displayOrder.get(i).equals("i"))
                 illustrationNum++;
         }
 
         textSegments.remove(illustrationNum);
         frag.setTextSegments(textSegments);
         displayOrder.remove(dispNum);
         frag.setDisplayOrder(displayOrder);
         return true;
     }
 
 
     //  public void addSound(Fragment frag, Sound sound){
     //      LinkedList<Sound> sounds = frag.getSounds();
     //      sounds.add(sound);
     //      frag.setSounds(sounds);
     //      LinkedList<String> displayOrder = frag.getDisplayOrder();
     //      displayOrder.add("s");
     //      frag.setDisplayOrder(displayOrder);
     //  }
     //  
     //  public void addVideo(Fragment frag, Video video){
     //      LinkedList<Video> videos = frag.getVideos();
     //      videos.add(video);
     //      frag.setVideos(videos);
     //      LinkedList<String> displayOrder = frag.getDisplayOrder();
     //      displayOrder.add("v");
     //      frag.setDisplayOrder(displayOrder);
     //  }
     //  
     
     @Override
     public void addChoice(Fragment frag, Choice cho){
         List<Choice> choices = frag.getChoices();
         choices.add(cho);
         frag.setChoices(choices);
         List<String> displayOrder = frag.getDisplayOrder();
         displayOrder.add("c");
         frag.setDisplayOrder(displayOrder);
     }
 
     @Override
     public boolean deleteChoice(Fragment frag, Choice cho){
         List<String> displayOrder = frag.getDisplayOrder();
         List<Choice> choices = frag.getChoices();
 
         //find the entry of display order that should be removed
         int occurence = choices.indexOf(cho);
         int i = 0;
         while(occurence >= 0){
             if (displayOrder.get(i) == "c"){
                 occurence--;
             }
             i++;
         }
         displayOrder.remove(1-1);
         frag.setDisplayOrder(displayOrder);
         
         choices.remove(cho);
         frag.setChoices(choices);
         return true;
     }
     /**
      * Removes the Choice at the given dispNum
      * returns true if successful
      * @param frag
      * @param dispNum
      * @return
      */
     private static boolean deleteChoice(Fragment frag, int dispNum){
         List<String> textSegments = frag.getTextSegments();
         List<String> displayOrder = frag.getDisplayOrder();
 
         //not a text segment at dispNum
         if (!displayOrder.get(dispNum).equals("c"))
             return false;
         
         int choiceNum=0;
         for (int i=0; i<dispNum;i++){
             if (displayOrder.get(i).equals("c"))
                 choiceNum++;
         }
 
         textSegments.remove(choiceNum);
         frag.setTextSegments(textSegments);
         displayOrder.remove(dispNum);
         frag.setDisplayOrder(displayOrder);
         return true;
     }
      
     public static void addEmptyPart(Fragment frag){
         List<String> displayOrder = frag.getDisplayOrder();
         displayOrder.add("e");
         frag.setDisplayOrder(displayOrder);
     }
     
     public static void removeEmptyPart(Fragment frag){
         List<String> displayOrder = frag.getDisplayOrder();
         displayOrder.remove("e");
         frag.setDisplayOrder(displayOrder);
     }
 
     //  public void addAnnotation(Fragment frag, Annotation annotation){
     //      Annotation annotations = frag.getAnnotations();
     //      annotations.addAnnotation(annotation);
     //      frag.setAnnotations(annotations);
     //  }
     
     public static void deleteFragmentPart(Fragment frag, int partNum){
         List<String> displayOrder = frag.getDisplayOrder();
         
         //check for invalid partNum
         if (partNum >= displayOrder.size())
             return;
         
         String type = displayOrder.get(partNum);
         
         if (type.equals("t"))
             deleteTextSegment(frag, partNum);
         else if (type.equals("i"))
             deleteIllustration(frag, partNum);
         else if (type.equals("c"))
             deleteChoice(frag, partNum);
         else if (type.equals("e"))
             removeEmptyPart(frag);
 
     }
 }
