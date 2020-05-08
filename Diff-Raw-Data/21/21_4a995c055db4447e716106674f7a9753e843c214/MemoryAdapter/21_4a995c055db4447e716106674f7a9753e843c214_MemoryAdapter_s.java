 package se.chalmers.dat255.sleepfighter.adapter;
 
 import java.util.Random;
 
 import se.chalmers.dat255.sleepfighter.challenge.MemoryCardView;
 import se.chalmers.dat255.sleepfighter.challenge.MemoryChallenge;
 import se.chalmers.dat255.sleepfighter.model.Memory;
 import android.content.Context;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 
 public class MemoryAdapter extends BaseAdapter implements View.OnClickListener {
   
 	private Context context;
 	private final Memory mem;
 	
 	private String[] memoryCardImages;
 	
 	private MemoryChallenge challenge;
 	
 	private Random rng = new Random();
 
 	// assign each card type an image.
 	private void assignCards() {
 		memoryCardImages = new String[mem.getNumPairs()];
 		
 		for(int i = 0; i< memoryCardImages.length; ++i) {
 			memoryCardImages[i] = "";
 		}
 		
 		int cardI = 0;
 		
 		while(cardI != mem.getNumPairs()) {
 			
 			// find unassigned image
 			boolean unassigned;
 			String image;
 			do {
 		
 				
 				// get random memory card image from the database.
 				image = memoryCardDatabase[rng.nextInt(memoryCardDatabase.length)];
 				
 				// ensure that it hasn't already been assigned.
 				unassigned = true;
 				for(int i = 0; i < memoryCardImages.length; ++i) {
					if(memoryCardImages[i] == image) {
 						// the image has already been assigned to another card type
 						unassigned = false;
 						break;
 					}
 				}
 			
 			
 			}while(!unassigned);
 			
 			memoryCardImages[cardI] = image;
 			
 			++cardI;
 		}
 	}
 	
     public MemoryAdapter(final Context c, final Memory mem, MemoryChallenge challenge) {
         context = c;
         this.mem = mem;     
         this.challenge = challenge;
        
         assignCards();
     }
 
     public int getCount() {
         return this.mem.getNumCards();
     }
 
     public Object getItem(final int position) {
         return null;
     }
 
     public long getItemId(final int position) {
         return 0;
     }
 
     // create a new ImageView for each item referenced by the Adapter
     public View getView(int position, View convertView, ViewGroup parent) {
     	MemoryCardView view;
         if (convertView == null) { 
             view = new MemoryCardView(context);
             view.setLayoutParams(new GridView.LayoutParams(85, 85));
             view.setTextSize(TypedValue.COMPLEX_UNIT_SP,60);
             
             // TODO: make the memory cards look less like buttons, and more like cards. 
          
      
         } else {
             view = (MemoryCardView) convertView;
         }
       
         view.setOnClickListener(this);
         view.setPosition(position);
         String image = memoryCardImages[mem.getCard(position)];
         
         view.setImage(image);
         return view;
     }
     
     public void onClick(View v) {
         challenge.onItemClick(null, v, 0, 0);
     }
     
     // list of all the usable memory cards. 
     private String[] memoryCardDatabase = {
     		"a", "b",
     		"c", "d",
     		"e", "f",
     		"g", "h",
     		"h", "j",
     		"k", "l",
     		"m", "n",
     		"o", "p",
     		"q", "r",
     		"s", "t",
     		"u", "v",
     		"w", "x",
     		"y", "z",
     };
 
 }
