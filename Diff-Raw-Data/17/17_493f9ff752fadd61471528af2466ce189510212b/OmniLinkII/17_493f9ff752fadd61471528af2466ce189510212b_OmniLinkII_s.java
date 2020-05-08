 package com.teqlabs.bryan.OmniLinkII;
 
import net.homeip.mleclerc.omnilink.messagebase.ReplyMessage;

 public interface OmniLinkII extends net.xeoh.plugins.base.Plugin {
 	void init();
	ReplyMessage executeCommand(String c) throws Exception;
 }
