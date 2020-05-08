 package com.irr310.common.world.state;
 
 import com.irr310.common.network.NetworkClass;
 import com.irr310.common.network.NetworkField;
 import com.irr310.common.network.NetworkOptionalField;
 
 @NetworkClass
 public class PlayerState {
 
     @NetworkField
     public long id;
 
     @NetworkField
     public String login;
 
     @NetworkOptionalField
     public FactionState faction;

    public boolean isConnected() {
        return faction != null;
    }
     
 }
