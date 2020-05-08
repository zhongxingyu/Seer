 package com.madpcgaming.mt.core.proxy;
 
 import com.madpcgaming.mt.tileentitys.CableTE;
 import com.madpcgaming.mt.tileentitys.renderers.CableRenderer;
 
 import cpw.mods.fml.client.registry.ClientRegistry;
 
 public class ClientProxy extends CommonProxy
 {
	
 	public void registerRenderings()
 	{
		ClientRegistry.bindTileEntitySpecialRenderer(CableTE.class, new CableRenderer());
 	}
 }
