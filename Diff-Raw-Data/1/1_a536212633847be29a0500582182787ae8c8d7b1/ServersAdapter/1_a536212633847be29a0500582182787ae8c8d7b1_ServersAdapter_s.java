 package com.novell.android.yastroid;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 
 public class ServersAdapter {
 	private ArrayList<Server> servers;
 	
 	public ServersAdapter(Context context, int textViewResourceId, ArrayList<Server> servers) {
		super(context, textViewResourceId, servers);
 		this.servers = servers;
 	}
 }
