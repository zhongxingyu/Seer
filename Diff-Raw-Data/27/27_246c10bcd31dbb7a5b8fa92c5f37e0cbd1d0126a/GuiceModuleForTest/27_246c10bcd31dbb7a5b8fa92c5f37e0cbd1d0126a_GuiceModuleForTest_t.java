 package com.christophdietze.jack.test.server;
 
import com.christophdietze.jack.server.ChessServiceImpl;
 import com.christophdietze.jack.server.CometServer;
 import com.google.inject.AbstractModule;
 
 class GuiceModuleForTest extends AbstractModule {
 	@Override
 	protected void configure() {
 		bind(CometServer.class).to(CometServerMock.class);
		bind(ChessServiceImpl.class).to(ChessServiceImplMock.class);
 	}
 }
