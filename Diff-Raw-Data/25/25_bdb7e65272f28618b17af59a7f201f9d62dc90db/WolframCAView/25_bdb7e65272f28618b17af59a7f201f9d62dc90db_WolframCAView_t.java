 package net.nologin.meep.ca.view;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.util.Log;
 import net.nologin.meep.ca.model.WolframTileProvider;
 import net.nologin.meep.ca.util.Utils;
 import net.nologin.meep.tbv.TiledBitmapView;
 
 public class WolframCAView extends TiledBitmapView {
 
     public WolframCAView(Context context, AttributeSet attrs) {
 
         super(context, attrs);
 
     }
 
     public void setupForRule(int newRule){
 
         WolframTileProvider tp = (WolframTileProvider)getTileProvider();
 
         if(tp == null){
             Utils.log("Init new provider, rule = " + newRule);
             setTileProvider(new WolframTileProvider(getContext(),newRule));
         }
         else {
             Utils.log("Changing rule, rule = " + newRule);
             tp.changeRule(newRule);
         }
 
        jumpToOriginTile(true);
     }
 
 
     public int getCurrentRule(){
         WolframTileProvider tp = (WolframTileProvider)getTileProvider();
         if(tp == null){
             throw new IllegalStateException("No provider initialised, perhaps setupForRule hasn't been called yet");
         }
         return tp.getRule();
     }
 
 }
