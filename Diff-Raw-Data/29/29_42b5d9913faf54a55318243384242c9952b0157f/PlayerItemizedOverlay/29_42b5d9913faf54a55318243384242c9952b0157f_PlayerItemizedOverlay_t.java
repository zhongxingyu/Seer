 package com.example.android.apis.overlays;
 
 import android.graphics.drawable.Drawable;
 
 import com.example.android.apis.model.Player;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
public class PlayerItemizedOverlay extends ItemizedOverlay<OverlayItem> {
     private Player player;
 
     public PlayerItemizedOverlay(Drawable defaultMarker, Player player) {
         super(boundCenterBottom(defaultMarker));
         this.player = player;
         populate();
     }
 
     @Override
     protected OverlayItem createItem(int i) {
         return new OverlayItem(player.getLocation(), "", "");
     }
 
     @Override
     public int size() {
         return 1;
     }
 
 }
