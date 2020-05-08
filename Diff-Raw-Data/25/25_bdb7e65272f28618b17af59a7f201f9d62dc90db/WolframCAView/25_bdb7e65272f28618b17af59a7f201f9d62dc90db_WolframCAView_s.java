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
 
        jumpToOriginTile();
     }
 
    public void setupForRulePrev(){

        int rule = getCurrentRule();
        setupForRule(rule <= 0 ? 255 : rule-1); // rotate round to 255 when we pass 0

    }

    public void setForNextRuleNext(){

        int rule = getCurrentRule();
        setupForRule(rule >= 255 ? 0 : rule+1); // rotate round to 0 when we pass 255


    }
 
     public int getCurrentRule(){
         WolframTileProvider tp = (WolframTileProvider)getTileProvider();
         if(tp == null){
             throw new IllegalStateException("No provider initialised, perhaps setupForRule hasn't been called yet");
         }
         return tp.getRule();
     }
 
 }
